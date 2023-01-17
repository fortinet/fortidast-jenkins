package com.fortidast;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.List;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * . Fields in the config.jelly should match with parameters passing in the
 * DataBoundConstructor this class method will get called when saving local
 * config in Jenkins
 */
public class ScanBuilder extends hudson.tasks.Builder implements SimpleBuildStep {
    /**
     * . target on which scan will get triggered
     */
    private String target;

    /**
     * . scanType - either Quick or Full Scan
     */
    private String scanType;

    /**
     * . ncApiToken - API Token
     */
    private String ncApiToken;

    /**
     * @param scanType   either Quick Scan or Full Scan.
     * @param target     Scan target
     * @param ncApiToken API Token
     */
    @DataBoundConstructor
    public ScanBuilder(String scanType, String target, String ncApiToken) {
        this.target = target;
        this.scanType = scanType;
        this.ncApiToken = ncApiToken;
    }

    /**
     * @return returns scan target
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @return returns scan type
     */
    public final String getScanType() {
        return scanType;
    }

    /**
     * @return returns API Key
     */
    public final String getNcApiToken() {
        return ncApiToken;
    }

    /**
     * @return returns Plain API Key credentials
     */
    public final String getncApiKeyPlainCredentials() {
        StandardCredentials credentials = null;
        try {
            credentials = CredentialsMatchers.firstOrNull(lookupCredentials(StandardCredentials.class, (Item) null,
                    ACL.SYSTEM, new ArrayList<DomainRequirement>()), CredentialsMatchers.withId(ncApiToken));
        } catch (NullPointerException e) {
            throw new ConnectionException(MessageManager.getString("api.key.not.set"));
        }
        if (credentials != null) {
            if (credentials instanceof StringCredentials) {
                return ((StringCredentials) credentials).getSecret().getPlainText();
            }
        }
        throw new IllegalStateException("Could not find FortiDAST API Key ID: " + ncApiToken);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
            @Nonnull TaskListener listener) throws hudson.AbortException, InterruptedException {
        final PrintStream listenerLogger = listener.getLogger();
        ScanEngine scanEngine = ScanEngine.getInstance();
        scanEngine.initGlobalConfigValues(getncApiKeyPlainCredentials(), getDescriptor().getuserName());
        ScanEngine.Resp resp = null;
        int scantype = -1;
        String apiUrl = getDescriptor().gApiUrl;
        Boolean isStopped = false;
        final int time_sleep = 10;
        final int time_sleep_for_cont_req = 30;

        try {
            Integer i = ScanEngine.getCount();
            if (target == null) {
                listenerLogger.println(MessageManager.getString("aborting.the.build"));
                throw new hudson.AbortException(MessageManager.getString("invalid.target"));
            }
            target = target.contains("http") ? target : "";
            String uuid = scanEngine.getTargetUuid(apiUrl + "/query/allassets", target);
            if (scanType.equals("QuickScan") || scanType.equals("FullScan")) {
                scantype = scanType.equals("QuickScan") ? 0 : 1;
                resp = scanEngine.assetScanStatus(apiUrl + "/query/scanstatus", target, uuid);
                if (resp.jso.toString().contains("In Progress")) {
                    resp = scanEngine.startStopScan(apiUrl + "/cmdb/scan/stop", target, uuid, -1);
                    if (!resp.jso.toString().contains("Stop Request Successfull")
                            && resp.jso.toString().contains("In Progress")) {
                        listenerLogger.println(MessageManager.getString("aborting.the.build"));
                        throw new hudson.AbortException(
                                MessageManager.getString("Fail.to.trigger.stop.scan.on.0", target));
                    }
                    TimeUnit.SECONDS.sleep(time_sleep);
                    isStopped = true;
                    listenerLogger.println(MessageManager.getString("stop.current.active.scan"));
                }
                resp = scanEngine.startStopScan(apiUrl + "/cmdb/scan/start", target, uuid, scantype);
                if (!resp.jso.toString().contains("Scan Request Successfull")) {
                    listenerLogger.println(MessageManager.getString("aborting.the.build"));
                    throw new hudson.AbortException(MessageManager.getString("Fail.to.trigger.scan.on.0", target));
                }
                listenerLogger.println(MessageManager.getString("starting.scan.on.target.0", target));
            }
            TimeUnit.SECONDS.sleep(time_sleep);
            resp = scanEngine.assetScanStatus(apiUrl + "/query/scanstatus", target, uuid);
            String errorCode = scanEngine.getErrorCode(resp.jso.toString());
            if (!errorCode.isEmpty() && !errorCode.equals("0") && !isStopped) {
                String errMesg = String.join("", "errstring", errorCode);
                throw new hudson.AbortException(MessageManager.getString(errMesg));
            }
            if (resp.jso.toString().contains("Scan Request in Queue")) {
                listenerLogger.println(MessageManager.getString("scan.request.in.queue"));
            } else if (resp.jso.toString().contains("Authorization Failed")) {
                throw new hudson.AbortException(MessageManager.getString("Authorization.failed"));
            }
            while (!resp.jso.toString().contains("Scan Complete")) {
                resp = scanEngine.assetScanStatus(apiUrl + "/query/scanstatus", target, uuid);
                if (resp.jso.toString().contains("Stopped")) {
                    listenerLogger.println(MessageManager.getString("aborting.the.build.the.scan.was.stopped.externally"));
                    throw new hudson.AbortException(MessageManager.getString("Fail.to.generate.scan.summary.table"));
                }
                TimeUnit.SECONDS.sleep(time_sleep_for_cont_req);
            }
            listenerLogger.println(MessageManager.getString("scan.completed.successfully"));
            listenerLogger.println(MessageManager.getString("generating.scan.summary.result"));
            List<Object> vulnJson = scanEngine.getScanResults(apiUrl + "/query/scansummaryresults", uuid, target);

            build.addAction(new ScanSummaryAction(vulnJson, i.toString(), getTarget()));
            listenerLogger.println(MessageManager.getString("scan.summary.report.generated.successfully"));
        } catch (SSLHandshakeException ex) {
            throw new hudson.AbortException(MessageManager.getString("certificate.to.the.java.ca.store"));
        } catch (hudson.AbortException ex) {
            listenerLogger.println(MessageManager.getString("Exception.found.while.scanning.the.target.0.1",
                    getTarget(), ex.toString()));
            build.setResult(hudson.model.Result.FAILURE);
        } catch (java.net.ConnectException ex) {
            listenerLogger.println(MessageManager.getString("aborting.the.build"));
            throw new hudson.AbortException(MessageManager.getString("cannot.connect.to.application"));
        } catch (ConnectionException e) {
            listenerLogger.println(MessageManager.getString("aborting.the.build"));
            throw new hudson.AbortException(MessageManager.getString("cannot.connect.to.application"));
        } catch (Exception ex) {
            throw new hudson.AbortException(MessageManager.getString("Exception.found.while.scanning.the.target.0.1",getTarget(), ex.toString()));
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * . gets called when global configuration svae method is called
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Builder> {
        /**
         * . API UL
         */
        private String gApiUrl;
        /**
         * API TOKEN.
         */
        private String ncApiToken;
        /**
         * . User Name
         */
        private String userName;
        /**
         * Scan engine Object.
         */
        private ScanEngine scanEngine = ScanEngine.getInstance();

        /**
         * allows to load global config data.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Validates Global Config data.
         * @param apiUrl   contains API URL that gets from Jenkins global config
         * @param userName contains username that gets from Jenkins global config
         * @return FormValidation returns Form Validation error/success message
         */
        public FormValidation doTestValidation(@QueryParameter("gApiUrl") final String apiUrl,
                @QueryParameter("userName") final String userName) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            if (apiUrl.length() == 0) {
                return FormValidation.error(MessageManager.getString("please.set.the.api.url"));
            } else if (!apiUrl.contains("/api/v1.0")) {
                return FormValidation.error(MessageManager.getString("invalid.api.url"));
            } else if (userName.length() == 0) {
                return FormValidation.error(MessageManager.getString("please.set.the.user.name"));
            }
            return FormValidation.ok(MessageManager.getString("validated.successfully"));
        }

        /**
         * @param aClass AbstractProject
         * @return returns true
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * @return return FortiDAST which is used as Jenkins Plugin name
         */
        public String getDisplayName() {
            return "FortiDAST";
        }

        /**
         * @param req      StaplerRequest for communicating with Jenkins.
         * @param formData contains global config data in the form of json.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            gApiUrl = formData.getString("gApiUrl");
            userName = formData.getString("userName");
            save();
            return super.configure(req, formData);
        }

        /**
         * @return return API URL
         */
        public String getgApiUrl() {
            return gApiUrl;
        }

        /**
         * @return returns username
         */
        public String getuserName() {
            return userName;
        }

        /**
         * @return returns API Token
         */
        private String getncApiKey() {
            StandardCredentials credentials = null;
            try {
                credentials = CredentialsMatchers.firstOrNull(lookupCredentials(StandardCredentials.class, (Item) null,
                        ACL.SYSTEM, new ArrayList<DomainRequirement>()), CredentialsMatchers.withId(ncApiToken));
            } catch (NullPointerException e) {
                throw new ConnectionException(MessageManager.getString("api.key.not.set"));
            }
            if (credentials != null) {
                if (credentials instanceof StringCredentials) {
                    return ((StringCredentials) credentials).getSecret().getPlainText();
                }
            }
            throw new IllegalStateException("Could not find FortiDAST API Key ID: " + ncApiToken);
        }

        /**
         * @param ncApiToken receives API Key from Jenkins local config
         * @return return ListBoxModel items
         * @throws IOException throw IO Exception
         */
        public ListBoxModel doFillTargetItems(@QueryParameter("ncApiToken") String ncApiToken) throws IOException {
            ListBoxModel assetItems = new ListBoxModel();
            if (!ncApiToken.isEmpty()) {
                this.ncApiToken = ncApiToken;
                scanEngine.initGlobalConfigValues(getncApiKey(), getuserName());
            } else {
                assetItems.clear();
                assetItems.add("Please provide the API KEY in the below text box to see Scan URLS");
                return assetItems;
            }
            try {
                JSONArray jsa = scanEngine.getTargets(getgApiUrl() + "/query/allassets");
                for (int i = 0; i < jsa.size(); i++) {
                    JSONObject jsonObject = (JSONObject) jsa.get(i);
                    String assetname = jsonObject.getString("asset_name");
                    assetItems.add(assetname, assetname);
                }
            } catch (Exception ex) {
                assetItems.clear();
                assetItems.add("Please provide the API KEY in the below text box to see Scan URLS");
                throw ex;
            } finally {
                return assetItems;
            }
        }

        /**
         * @return ListBoxModel items
         * @throws IOException throw IOException
         */
        public ListBoxModel doFillScanTypeItems() throws IOException {
            ListBoxModel items = new ListBoxModel();
            items.add("Quick Scan", "QuickScan");
            items.add("Full Scan", "FullScan");
            return items;
        }

        /**
         * @param item       AncestorInPath Object
         * @param ncApiToken API Token
         * @return ListBoxModel items
         */
        public ListBoxModel doFillNcApiTokenItems(@AncestorInPath Item item, @QueryParameter String ncApiToken) {
            StandardListBoxModel result = new StandardListBoxModel();
            result.includeEmptyValue();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(ncApiToken);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(ncApiToken);
                }
            }
            if (ncApiToken != null) {
                result.includeMatchingAs(ACL.SYSTEM, Jenkins.get(), StringCredentials.class,
                        Collections.<DomainRequirement>emptyList(),
                        CredentialsMatchers.allOf(CredentialsMatchers.withId(ncApiToken)));
            }
            return result.includeMatchingAs(ACL.SYSTEM, Jenkins.get(), StringCredentials.class,
                    Collections.<DomainRequirement>emptyList(),
                    CredentialsMatchers.allOf(CredentialsMatchers.instanceOf(StringCredentials.class)));
        }
    }
}
