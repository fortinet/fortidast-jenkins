package com.fortidast;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.io.InputStreamReader;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;


/**.
 *ScanEngine object.
 */
public class ScanEngine {
    /**.
     * variable for storing API KEY
     */
    private String apiKey;
    /**.
     * variable for storing usr name.
     */
    private String userName;
    /**.
     * class variable for storing ScanEngine object
     */
    private static ScanEngine scanEngineInstance = null;
    /**.
     * variable for storing count of builds
     */
    private static Integer count=0;
    /**.
     * gets count of number of builds
     * @return return count of number of builds
     */
    public static Integer getCount(){
        count=count+1;
        return count;
    }
    /**.
     * Creating singleton object for ScanEngine class
     * @return ScanEngine class object
     */
    public static ScanEngine getInstance() {
        if (scanEngineInstance == null) {
            scanEngineInstance = new ScanEngine();
        }
        return scanEngineInstance;
    }
    /**.
     * defining static class for storing response json and response status code
     */
    public static class Resp {
        /**.
         * Response Status Code
         */
        int statusCode;
        /**.
         * JsonObject for storing respons json
         */
        JSONObject jso = null;
    }
    /**
     * opens the connection with the given remote url.
     * @param endpoint api endpoint
     * @param method httpmethod
     * @param contentType httpheader
     * @return HttpsURLConnection object
     * @throws IOException tHrow IOException
     */
    private HttpsURLConnection openConnection(String endpoint, String method, String contentType) throws IOException {
        URL url = new URL(endpoint);
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        if(contentType!=null){
            connection.setRequestProperty("Content-Type", contentType);
        }
        connection.setRequestProperty("X-API-Key", this.apiKey);
        connection.setRequestProperty("Accept", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * makes post request and get response.
     * @param urlStr api endpoint
     * @param urlParams post request data
     * @return Resp class object
     * @throws IOException throw IOException
     * @throws NullPointerException throw NullPointerException
     */
    private Resp doPostRequest(String urlStr, String urlParams) throws IOException, NullPointerException {
        Resp resp = new Resp();
        BufferedReader in = null;
        HttpsURLConnection connection = openConnection(urlStr, "POST","application/json");
        final int badReq = 400;
        final int internalError = 599;
        final int unauthStatusCode = 401;
        try{
            byte[] input = urlParams.getBytes(StandardCharsets.UTF_8);
            int length = input.length;
            connection.setFixedLengthStreamingMode(length);
            connection.setInstanceFollowRedirects(false);
            try(DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())){
                outputStream.write(input);
            }
            resp.statusCode = connection.getResponseCode();
            if(connection.getErrorStream()!=null){
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream(),Charset.defaultCharset()));
            }else{
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(),Charset.defaultCharset()));
            }
            String output=null;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null){
                response.append(output);
            }
            in.close();
            resp.jso = JSONObject.fromObject(response.toString());
            JSONObject statusObj;
            String status = null;
            if (badReq <= resp.statusCode && resp.statusCode <= internalError) {
                status = resp.jso.getString("Status");
                if(status.contains("status")){
                    statusObj=resp.jso.getJSONObject("Status");
                    if(statusObj.has("status")){
                        status = statusObj.getString("status");
                    }
                }
                throw new hudson.AbortException(status);
            }
            return resp;
        }catch(HttpRetryException ex){
            if(resp.statusCode==unauthStatusCode){
                throw new hudson.AbortException("Unauthorized Error: No permission for Basic Key");
            }
            throw ex;
        }catch(IOException ex){
            throw ex;
        }finally{
            connection.disconnect();
        }
    }
    /**
     * makes Get request and get response.
     * @param urlStr url concatenated with query params
     * @return Resp object
     * @throws IOException throw IOException
     */
    private Resp doGetRequest(String urlStr) throws IOException {
        BufferedReader in = null;
        Resp resp = new Resp();
        HttpsURLConnection connection = openConnection(urlStr, "GET", null);
        final int badReq = 400;
        final int internalError = 599;
        final int unauthStatusCode = 401;
        try{
            resp.statusCode = connection.getResponseCode();
            if(connection.getErrorStream()!=null){
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream(),Charset.defaultCharset()));
            }else{
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(),Charset.defaultCharset()));
            }
            String output=null;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null){
                response.append(output);
            }
            in.close();
            resp.jso = JSONObject.fromObject(response.toString());
            if (badReq <= resp.statusCode && resp.statusCode <= internalError) {
                JSONObject statusObj;
                String status = null;
                status = resp.jso.getString("Status");
                if(status.contains("status")){
                    statusObj=resp.jso.getJSONObject("Status");
                    if(statusObj.has("status")){
                        status = statusObj.getString("status");
                    }
                }
                throw new hudson.AbortException(status);
            }
            return resp;
        }catch(HttpRetryException ex){
            if(resp.statusCode==unauthStatusCode){
                throw new hudson.AbortException("Unauthorized Error:No permission for Basic Key");
            }
            throw ex;
        }catch(Exception ex){
            throw ex;
        }finally{
            connection.disconnect();
        }
    }
    /**
     * get scan summary results.
     * @param urlStr scan summary results api endpoint
     * @param uuid unique id
     * @param target scan url
     * @return List<object>
     * @throws IOException throw IOException
     */
    public List<Object> getScanResults(String urlStr, String uuid, String target)throws IOException{
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        final int successRespCode = 200;
        params.add(new NameValuePair("uuid", uuid));
        params.add(new NameValuePair("url", target));
        String queryParams = getQueryParameters(params);
        urlStr = urlStr + queryParams;
        Resp resp = doGetRequest(urlStr);
        if (resp.statusCode != successRespCode) {
            throw new IOException(MessageManager.getString("bad.response.0", resp.statusCode));
        }
        JSONArray jsonrray = JSONArray.fromObject(resp.jso.getString("Status"));
        HashMap<String,Integer> map=new HashMap<String,Integer>();
        map.put("Critical",0);
        map.put("High",0);
        map.put("Medium",0);
        map.put("Low",0);
        ArrayList<JSONObject> vulnList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonrray.size(); i++){
            JSONObject jsonObject = (JSONObject)jsonrray.get(i);
            JSONObject severityJson= JSONObject.fromObject(jsonObject.getString("severitycount"));
            jsonObject.put("severitycount",severityJson);
            int critical = Integer.parseInt(severityJson.get("Critical").toString());
            int high = Integer.parseInt(severityJson.get("High").toString());
            int medium = Integer.parseInt(severityJson.get("Medium").toString());
            int low = Integer.parseInt(severityJson.get("Low").toString());
            map.put("Critical",map.get("Critical")+critical);
            map.put("High",map.get("High")+high);
            map.put("Medium",map.get("Medium")+medium);
            map.put("Low",map.get("Low")+low);
            vulnList.add(jsonObject);
        }
        return Arrays.asList(vulnList, map);
    }
    /**
     * constructs query parameters.
     * @param params Name Value pair
     * @return string
     * @throws IOException throw IOException
     */
    private String getQueryParameters(List<NameValuePair> params) throws IOException{
        StringBuilder queryParams = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params){
            if (first){
                queryParams.append("?");
                first = false;
            }else{
                queryParams.append("&");
            }
            queryParams.append(pair.getName());
            queryParams.append("=");
            queryParams.append(pair.getValue());
        }
        return queryParams.toString();
    }

     /**
     * Initialising userName and apiKey.
     * @param userName username
     * @param apiKey api key
     */
    public void initGlobalConfigValues(String apiKey,String userName) {
        this.apiKey = apiKey;
        this.userName = userName;
    }

    /**
     * checking scan status, return false if scan is in progress otherwise false.
     * @param urlStr api endpoint.
     * @param target scan url
     * @param uuid asset uuid
     * @return returns Resp Object
     * @throws IOException throw IOException
     */
    public Resp assetScanStatus(String urlStr,String target,String uuid) throws IOException{
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        final int successRespCode = 200;
        params.add(new NameValuePair("uuid", uuid));
        params.add(new NameValuePair("url", target));
        String queryParams = getQueryParameters(params);
        urlStr = urlStr + queryParams;
        Resp resp = doGetRequest(urlStr);
        if (resp.statusCode != successRespCode){
            throw new IOException(MessageManager.getString("bad.response.0", resp.statusCode));
        }
        return resp;

    }
    /**
     * stops the scan.
     * @param urlStr start or stops the scan based on endpoint
     * @param target scan Url
     * @param uuid asset uuid
     * @param scanType Quick Scan or Full Scan
     * @return response status code
     * @throws IOException throw IOException
     */
    public Resp startStopScan(String urlStr,String target,String uuid,int scanType) throws IOException{
        JSONObject jso = new JSONObject();
        final int successRespCode = 200;
        jso.put("url", target);
        jso.put("uuid", uuid);
        if(scanType==0 || scanType==1){
            jso.put("scan_type", scanType);
        }
        Resp resp = doPostRequest(urlStr, jso.toString());
        if (resp.statusCode != successRespCode){
            throw new IOException(MessageManager.getString("bad.response.0", resp.statusCode));
        }
        return resp;
    }
    /**
     * gets all the assets for the given apikey.
     * @param allAssetsApi all assets api endpoint
     * @return response json array
     * @throws IOException throw IOException
     */
    public JSONArray getTargets(String allAssetsApi) throws IOException {
        final int successRespCode = 200;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user", this.userName));
        String queryParams = getQueryParameters(params);
        allAssetsApi = allAssetsApi + queryParams;
        Resp resp = doGetRequest(allAssetsApi);
        if (resp.statusCode != successRespCode){
            throw new IOException(MessageManager.getString("bad.response.0", resp.statusCode));
        }
        JSONArray jsonarray = null;
        jsonarray =  JSONArray.fromObject(resp.jso.getString("Status"));
        return jsonarray;
    }
    /**
     * output the error code.
     * @param jsonResponse startscan response json
     * @return errorcode
     * @throws Exception throw Exception
     */
    public String getErrorCode(String jsonResponse) throws Exception{
        String errorCode = null;
        JSONObject jsonObject = JSONObject.fromObject(jsonResponse);
        JSONArray jsonrray = JSONArray.fromObject(jsonObject.getString("Status"));
        for (int i = 0; i < jsonrray.size(); i++){
            jsonObject = (JSONObject)jsonrray.get(i);
            errorCode = jsonObject.getString("scan_error");
        }
        return errorCode;
    }
    /**
     * outputs target uuid.
     * @param apiUrl api endpointt
     * @param target scan url
     * @return target uuid
     * @throws Exception throw Exception
     */
    public String getTargetUuid(String apiUrl,String target) throws Exception{
        JSONArray jsa = getTargets(apiUrl);
        for (int i = 0; i < jsa.size(); i++){
            JSONObject jsonObject = (JSONObject)jsa.get(i);
            String assetname= jsonObject.getString("asset_name");
            String uuid= jsonObject.getString("uuid");
            if(assetname.equals(target)){
                return uuid;
            }
        }
        return null;
    }
}


