package com.fortidast;
import hudson.model.Run;
import jenkins.model.RunAction2;
import net.sf.json.JSONObject;
import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**.
 * generates scan report
 */
public class ScanSummaryAction implements  RunAction2 {

    /**.
     * Object that used while the build is executed, to keep track of things that are needed only during the build.
     */
    private transient Run run;
    /**.
     * variable for storing vulnerablity json data
     */
    private ArrayList<JSONObject> vulnJson;
    /**.
     * variable for storing scevrity json data
     */
    private HashMap<String,Integer> sevJson;
    /**.
     * gets count of number of builds
     */
    private  String count;
    /**.
     * Scan URL
     */
    private String target;
    /**.
     * constructor
     * @param vulnSevJson contains vulnerability json data
     * @param count count
     * @param targetName Scan URL
     * @throws IOException throws IO Exceptiom
     */
    public ScanSummaryAction(List<Object> vulnSevJson,String count,String targetName) throws IOException {
       this.count = count;
       this.vulnJson=(ArrayList<JSONObject>)vulnSevJson.get(0);
       this.sevJson=(HashMap<String,Integer>)vulnSevJson.get(1);
       this.target=targetName;
    }
    /**.
     * @return returns vuln json
     */
    public ArrayList<JSONObject> getVulnJson() {
        return vulnJson;
    }
    /**
     * @return return everity json
     */
    public HashMap<String,Integer> getSevJson() {
        return sevJson;
    }
    /**.
     * @return return Scan URL
     */
    public String getTarget() {
        return target;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
       return "Scan Summary Report";

    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "fortipentestscansummaryreport"+"-" +  this.count;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }
    /**
     * @return returns hudson.model.Run object
     */
    public Run getRun() {
        return run;
    }
}
