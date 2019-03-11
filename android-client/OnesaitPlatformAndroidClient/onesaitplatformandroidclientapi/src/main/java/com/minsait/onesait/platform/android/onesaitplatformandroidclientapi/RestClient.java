package com.minsait.onesait.platform.android.onesaitplatformandroidclientapi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by mbriceno on 12/03/2018.
 */

public class RestClient {

    protected URI baseUri = null;
    protected String token;
    protected String clientPlatform;
    protected String clientPlatformId;
    public String sessionKey;

    public RestClient(String url, String token, String clientPlatform, String clientPlatformId) throws MalformedURLException, URISyntaxException {
        this.baseUri = new URL(url).toURI();
        this.token = token;
        this.clientPlatform = clientPlatform;
        this.clientPlatformId = clientPlatformId;
    }

    /**
     * JOIN MESSAGE
     *
     * @return responseCode from HTTP POST operation (OK=200)
     * @throws IOException
     * @throws JSONException
     */
    public int join() throws IOException, JSONException {
        int responseCode = -1;

        URL joinUrl = new URL(baseUri.toString() + "client/join" +
                "?token="+token+"&clientPlatform="+clientPlatform+"&clientPlatformId="+clientPlatformId);
        HttpURLConnection connection = get(joinUrl);

        responseCode = connection.getResponseCode();

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            final StringBuilder output = new StringBuilder("Request URL " + joinUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();
            connection.disconnect();

            JSONObject jsonJOIN = new JSONObject(responseOutput.toString());
            sessionKey = jsonJOIN.getString("sessionKey");
        }

        connection.disconnect();
        return responseCode;
    }

    /**
     * LEAVE MESSAGE
     * @return HTTP return code, 200 if OK
     */
    public int leave() throws IOException {
        if (sessionKey == "") {
            return HttpURLConnection.HTTP_OK;
        }

        int responseCode = -1;
        URL leaveUrl = new URL(baseUri.toString() + "client/leave");

        HttpURLConnection connection = getAuth(leaveUrl, sessionKey);
        responseCode = connection.getResponseCode();
        connection.disconnect();

        return responseCode;
    }

    /**
     * INSERT MESSAGE
     *
     * @param ontology
     * @param message
     * @return String with the insert response and the ObjectId returned from MongoDB, empty string otherwise
     * @throws IOException
     * @throws JSONException
     */
    public String insert(String ontology, String message) throws IOException, JSONException {
        String msg = "";
        String line = "";

        if(sessionKey==""){
            return msg;
        }

        URL leaveUrl = new URL(baseUri.toString() + "ontology/"+ontology);

        HttpURLConnection connection = postAuth(leaveUrl,sessionKey);

        DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
        dStream.writeBytes(message);
        dStream.flush();
        dStream.close();

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            DataInputStream iStream = new DataInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            while ((line = br.readLine()) != null) {
                msg = msg + line;
            }
        }
        return msg;
    }


    //Utils

    private HttpURLConnection post(URL url){
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setAllowUserInteraction(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();
            return connection;
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpURLConnection postAuth(URL url, String authToken){
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setAllowUserInteraction(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authToken);
            connection.connect();
            return connection;
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpURLConnection get(URL url){
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setAllowUserInteraction(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();
            return connection;
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpURLConnection getAuth(URL url, String authToken){
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setAllowUserInteraction(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization",authToken);
            connection.connect();
            return connection;
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
