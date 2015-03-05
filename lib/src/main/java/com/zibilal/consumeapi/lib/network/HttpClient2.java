package com.zibilal.consumeapi.lib.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
//import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by bmuhamm on 3/24/14.
 */
public class HttpClient2 {

    private static HttpClient2 mClient;
    private static final String TAG="HttpClient2";

    private String mSession;
    private String mAuthentication;
    private String mContentType;

    private Context mContext;

    private boolean mAuthenticated;

    private HttpClient2(Context context) {
        mContext=context;
    }

    public String getSession(){
        return mSession;
    }

    public void setSession(String session) {
        mSession=session;
    }

    public String getAuthentication(){
        return mAuthentication;
    }

    public void setAuthentication(String authentication){
        mAuthentication=authentication;
    }

    public String getContentType(){
        return mContentType;
    }

    public void setContentType(String contentType){
        mContentType=contentType;
    }

    public boolean isAuthenticated() { return mAuthenticated; }

    public void setAuthenticated(boolean authenticated) { mAuthenticated=authenticated; }

    public static HttpClient2 instance(Context context){
        if(mClient == null) {
            mClient = new HttpClient2(context);
        }

        return mClient;
    }

    public synchronized boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected()) {
            return true;
        }

        return false;
    }

    public synchronized byte[] fetchData(String strUrl) throws Exception{
        URL  url = new URL(strUrl);

        InputStream inputStream = url.openConnection().getInputStream();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            StringBuilder strBuff = new StringBuilder();
            int readSize = inputStream.read(buff);
            strBuff.append(buff);

            while (readSize > 0) {
                outputStream.write(buff, 0, readSize);
                readSize = inputStream.read(buff);
            }

            return outputStream.toByteArray();

        }finally {
            try {
                inputStream.close();
            } catch(IOException e){
                //Log.e(TAG, "Failed close stream due to " + e.getMessage());
            }
        }
    }

    private byte[] fetchData(String zip, InputStream inputStream) throws IOException {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            if (zip != null && zip.equals("gzip")) {
                GZIPInputStream gz = new GZIPInputStream(inputStream);
                int readBytes = gz.read(buff);
                while (readBytes > 0) {
                    baos.write(buff, 0, readBytes);
                    readBytes = gz.read(buff);
                }
                gz.close();
            } else {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                int readBytes = bufferedInputStream.read(buff);
                while (readBytes > 0) {
                    baos.write(buff, 0, readBytes);
                    readBytes = inputStream.read(buff);
                }
                bufferedInputStream.close();
            }
            return baos.toByteArray();

        } finally {
            try {
                inputStream.close();
            } catch(IOException e) {
                //Log.e(TAG, "Failed close stream due to " + e.getMessage());
            }
        }
    }

    public String getBasicAuthentication(String user, String pass) {
        String creds = String.format("%s:%s", user, pass);
        return "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
    }

    public synchronized byte[] getHttpData(String strUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) (new URL(strUrl)).openConnection();

        try{
            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                return fetchData(null, conn.getInputStream());
            } else {
                return fetchData(null, conn.getErrorStream());
            }
        } finally {
            conn.disconnect();
        }
    }



    /*

    public synchronized byte[] postHttpData(String strUrl, DataHttpModel model) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) (new URL(strUrl)).openConnection();

        try {

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestMethod(HttpDefault.HTTP_METHOD_POST);
            conn.setRequestProperty(HttpDefault.HEADER_USER_AGENT, HttpDefault.HEADER_USER_AGENT_VALUE);
            conn.setRequestProperty(HttpDefault.HEADER_ACCEPT, HttpDefault.HEADER_ACCEPT_VALUE);

            if (mContentType != null && mContentType.length() > 0) {
                conn.setRequestProperty(HttpDefault.HEADER_CONTENT_TYPE, mContentType);
            } else {
                conn.setRequestProperty(HttpDefault.HEADER_CONTENT_TYPE, HttpDefault.DEFAULT_CONTENT_TYPE);
            }
            if (mSession != null && mSession.length() > 0) {
                conn.setRequestProperty(HttpDefault.HEADER_SESSION, mSession);
            }

            if (mAuthenticated && (mAuthentication != null && mAuthentication.length() > 0)) {
                conn.setRequestProperty(HttpDefault.HEADER_AUTHENTICATION, mAuthentication);
            }

            DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
            printout.writeBytes(model.toJSON().toString());
            printout.flush();
            printout.close();

            int httpResponseCode = conn.getResponseCode();

            InputStream inputStream=null;
            try {
                if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }
            } finally{
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    }catch (IOException e) {
                        //Log.e(TAG, "Failed to close stream due to " + e.getMessage());
                    }
                }
            }

            return fetchData(conn.getHeaderField("content-encoding"), inputStream);

        } finally {
            conn.disconnect();
        }
    } */
}
