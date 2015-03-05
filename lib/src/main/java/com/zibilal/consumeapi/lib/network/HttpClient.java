package com.zibilal.consumeapi.lib.network;

import android.util.Base64;
//import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import gsonxml.GsonXml;
import gsonxml.GsonXmlBuilder;
import gsonxml.XmlParserCreator;

import com.zibilal.consumeapi.lib.rawbyte.RawParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bmuhamm on 4/2/14.
 */
public class HttpClient {

    private static final String TAG="HttpClient";

    public static final String JSON_TYPE="json";
    public static final String XML_TYPE="xml";
    public static final String BYTE_TYPE="byte";

    public static final String PARAM_PAIR="post param pair";

    private static final String DEFAULT_USER="secret";
    private static final String DEFAULT_PASSWORD="very2secret";

    private static final int DEFAULT_CONNECTION_TIMEOUT=5000;

    private String mUser;
    private String mPassword;

    private boolean mNeedAuthentication;
    private boolean mCached;

    private String mUserAgent;

    public HttpClient(){
        mNeedAuthentication=false;
    }

    public HttpClient(boolean needAuthentication) {
        mNeedAuthentication=needAuthentication;
    }

    public HttpClient(String userAgent, boolean needAuthentication) {
        mUserAgent=userAgent;
        mNeedAuthentication = needAuthentication;
    }

    public void setAuthentication(String user, String password) {
        mUser=user;
        mPassword=password;
    }

    public void initRequestProperty(HttpURLConnection conn) {
        if(mNeedAuthentication) {

            if(mUser == null || mPassword == null) {
                mUser = DEFAULT_USER;
                mPassword = DEFAULT_PASSWORD;
            }

            conn.setRequestProperty(HttpDefault.HEADER_AUTHENTICATION, getBasicAuthentication(mUser, mPassword));
            if(mUserAgent != null && mUserAgent.length() > 0)
                conn.setRequestProperty(HttpDefault.HEADER_USER_AGENT, mUserAgent);
            // conn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        }
    }


    public Object getJson(String url, Class < ? extends Response> clss) throws Exception{

        HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
        initRequestProperty(conn);
        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
            Reader reader = new InputStreamReader(inputStream);

            /*ResponseRaw obj = (ResponseRaw) RawParser.parse(clss, inputStream);
            byte[] bytes = (byte[]) obj.responseData();
            String str = new String(bytes);*/

            BufferedReader buff = new BufferedReader(reader);
            String str = "";
            StringBuffer buffer = new StringBuffer();
            while((str = buff.readLine()) != null) {
                buffer.append(str + "\n");
            }

            String json = buffer.toString();

            Gson gson = new GsonBuilder().create();
            JsonReader jreader = new JsonReader(new StringReader(json));
            jreader.setLenient(true);
            Response response = gson.fromJson(jreader, clss);
            return response;
        } else {
            throw new Exception(conn.getResponseMessage());
        }

    }

    public Object postData(String url, Class<? extends Response> responseCls, String postData) throws Exception {
        OutputStream out = null;
        try{
            HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
            initRequestProperty(conn);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setFixedLengthStreamingMode(postData.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            out = new BufferedOutputStream(conn.getOutputStream());
            out.write(postData.getBytes());
            out.flush();

            Reader reader = new InputStreamReader(conn.getInputStream());
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Response response = gson.fromJson(reader, responseCls);

            return response;
        } finally {
            if(out != null){
                out.close();
            }
        }
    }

    public Object postJson(String url, Class<? extends Response> responseCls, String jsonPost) throws Exception {
        OutputStream out = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
            initRequestProperty(conn);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setFixedLengthStreamingMode(jsonPost.getBytes("UTF-8").length);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            out = new BufferedOutputStream(conn.getOutputStream());
            out.write(jsonPost.getBytes("UTF-8"));
            out.flush();

            Reader reader = new InputStreamReader(conn.getInputStream());
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Response response = gson.fromJson(reader, responseCls);

            return response;
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    public String getBasicAuthentication(String user, String pass) {
        String creds = String.format("%s:%s", user, pass);
        return "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
    }

    public Object getXml(String url, Class < ? extends Response> clss) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
        initRequestProperty(conn);
        int responseCode = conn.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());

            GsonXml gsonXml=new GsonXmlBuilder().setXmlParserCreator(new XmlParserCreator() {
                @Override
                public XmlPullParser createParser() {
                    try {
                        return XmlPullParserFactory.newInstance().newPullParser();
                    } catch (XmlPullParserException e) {
                        //Log.e(TAG, e.getMessage());
                    }
                    return null;
                }
            }).create();
            Reader reader = new InputStreamReader(inputStream);
            return gsonXml.fromXml(reader, clss);
        } else {
            throw new Exception(conn.getResponseMessage());
        }
    }

    public Object getBytes(String url, Class < ? extends Response> clss) throws Exception {

        if(url.startsWith("https")) {
            HttpsURLConnection conn = (HttpsURLConnection) (new URL(url).openConnection());
            InputStream inputStream = conn.getInputStream();
            Object response = RawParser.parse(clss, inputStream);
            return response;
        } else {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
            InputStream inputStream = conn.getInputStream();
            Object response = RawParser.parse(clss, inputStream);
            return response;
        }
    }


}
