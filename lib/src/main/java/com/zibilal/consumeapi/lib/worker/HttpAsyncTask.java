package com.zibilal.consumeapi.lib.worker;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;

import com.zibilal.consumeapi.lib.network.HttpClient;
import com.zibilal.consumeapi.lib.network.Response;


/**
 * Created by bmuhamm on 3/30/14.
 */
public class HttpAsyncTask extends AsyncTask<String, Integer, Response> {

    public static final String TAG="HttpAsyncTask";
    public static final String REQUEST_METHOD_GET="GET";
    public static final String REQUEST_METHOD_POST="POST";
    private OnPostExecute callback;
    private Class<? extends Response> mResponseClass;

    private boolean mNeedAuthentication;

    private String mUserAgent;

    private String mUser;
    private String mPassword;
    private String mUrl;

    private Handler errorHandler; // handling exception

    public HttpAsyncTask(OnPostExecute postExecute, Class<? extends Response> responseClass, boolean needAuthentication, String userAgent){
        callback = postExecute;
        mResponseClass = responseClass;
        mNeedAuthentication = needAuthentication;
        mUserAgent=userAgent;
    }

    public void setAuthentication(String user, String password) {
        if(mNeedAuthentication) {
            mUser = user;
            mPassword = password;
        }
    }

    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    protected Response doInBackground(String... strings) {
        String url;
        String type;
        String requestMethod=null;
        String dataPost=null;
        if(strings.length == 2) {
            url = strings[0];
            type = strings[1];
        } else if(strings.length == 4) {
            url = strings[0];
            type = strings[1];
            requestMethod = strings[2];
            dataPost = strings[3];
        } else {
            url = strings[0];
            type = HttpClient.JSON_TYPE;
        }
        mUrl = url;

        try {
            HttpClient httpClient = new HttpClient(mUserAgent, mNeedAuthentication);

            if(mNeedAuthentication && ( mUser != null && mUser.length()>0 && mPassword != null && mPassword.length() > 0 )) {
                httpClient.setAuthentication(mUser, mPassword);
            }
            Response response;

            if(requestMethod!= null && dataPost != null && requestMethod.equals(REQUEST_METHOD_POST)) {
                if(type.equals(HttpClient.JSON_TYPE)) {
                    response = (Response) httpClient.postJson(url, mResponseClass, dataPost);
                } else if(type.equals(HttpClient.PARAM_PAIR)) {
                    response = (Response) httpClient.postData(url, mResponseClass, dataPost);
                } else {
                    throw new Exception("Exception is occured, type = " + type + " has not impelemented.");
                }
            } else {
                if (type.equals(HttpClient.XML_TYPE)) {
                    response = (Response) httpClient.getXml(url, mResponseClass);
                } else if (type.equals(HttpClient.BYTE_TYPE)) {
                    response = (Response) httpClient.getBytes(url, mResponseClass);
                } else {
                    response = (Response) httpClient.getJson(url, mResponseClass);
                }
            }
            return response;
        } catch (Exception e) {
            //Log.e(TAG, "Exception occured: " + e.getMessage());
            if(errorHandler != null) {
                Message msg = new Message();
                msg.obj = e;
                errorHandler.sendMessage(msg);
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        callback.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Response response) {
        callback.onUpdate(response, mUrl);
    }

    public static interface OnPostExecute {
        public void onProgress(Integer i);
        public void onUpdate(Response response, String url);
    }
}
