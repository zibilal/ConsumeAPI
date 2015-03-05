package com.zibilal.consumeapi.lib.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;
//import android.util.Log;

import com.zibilal.consumeapi.lib.network.HttpClient;
import com.zibilal.consumeapi.lib.network.Response;

/**
 * Created by bmuhamm on 5/14/14.
 */
public class HttpAsyncTaskLoader extends AsyncTaskLoader<Response> {

    private static final String TAG="HttpAsyncTaskLoader";

    private boolean run;
    private boolean loaded;

    private Class<? extends Response> mClass;
    private String mResponseType;
    private boolean mNeedAuthentication;
    private String mUserAgent;
    private String mUrl;

    private String mUser;
    private String mPassword;

    private Response response;

    private Handler errorHandler; // handling exception.

    private Object syncObject = new Object();

    public HttpAsyncTaskLoader(Context context) {
        super(context);

        loaded=false;
    }

    public HttpAsyncTaskLoader setRequest(String url) {
        mUrl = url;
        return this;
    }

    public HttpAsyncTaskLoader setResponse(Class<? extends Response> clss, String responseType) {
        mClass = clss;
        mResponseType = responseType;
        return this;
    }

    public HttpAsyncTaskLoader needAuthentication(boolean needAuthentication) {
        mNeedAuthentication=needAuthentication;
        return this;
    }

    public HttpAsyncTaskLoader setAuthentication(String user, String password) {
        if(mNeedAuthentication) {
            mUser=user;
            mPassword=password;
        }

        return this;
    }

    public HttpAsyncTaskLoader setUserAgent(String userAgent) {
        mUserAgent=userAgent;
        return this;
    }

    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler=errorHandler;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public void start(){
        run=true;
    }

    public void stop(){
        run=false;
    }

    public boolean isRun() {
        return run;
    }

    @Override
    public Response loadInBackground() {
        //Log.d(TAG, "HttpAsyncTaskLoader.loadInBackground");
        //if(run) {
        synchronized (syncObject) {
            try {
                HttpClient httpClient = new HttpClient(mUserAgent, mNeedAuthentication);

                if (mNeedAuthentication && (mUser != null && mUser.length() > 0 && mPassword != null && mPassword.length() > 0)) {
                    httpClient.setAuthentication(mUser, mPassword);
                }

                //Log.d(TAG, "loading --> " + mUrl + " ...");
                response = null;
                if (mResponseType.equals(HttpClient.XML_TYPE)) {
                    response = (Response) httpClient.getXml(mUrl, mClass);
                } else if (mResponseType.equals(HttpClient.JSON_TYPE)) {
                    response = (Response) httpClient.getJson(mUrl, mClass);
                    //Log.d(TAG, "Response " + response);
                }

                return response;
            } catch (Exception e) {
                //Log.e(TAG, "Exception is occured: " + e.getMessage());
                if (errorHandler != null) {
                    Message msg = new Message();
                    msg.obj = e;
                    errorHandler.sendMessage(msg);
                }
            }
        }
        //}
        return null;
    }

    @Override
    public void deliverResult(Response data) {

        synchronized (syncObject) {
            if (isReset()) {
                return;
            }

            if (response != null && isStarted()) {
                //Log.d(TAG, "Delivery result -->" + data + " url = " + mUrl);
                super.deliverResult(data);
            }

            if (response != null && response != data) {
                response = data;
            }
        }
        //Log.d(TAG, "End of Delivery result -->" + data + " url = " + mUrl);

    }

    @Override
    protected void onStartLoading() {
        //Log.d(TAG, "On start loading .... url = " + mUrl);
        if(response != null) {
            //Log.d(TAG, "On start loading , delivering result");
            deliverResult(response);
        } else {
            //Log.d(TAG, "On start loading, forceLoad");
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        //Log.d(TAG, "HttpAsyncTaskLoader.onStopLoading ... url = " + mUrl);
        stop();
        //super.onStopLoading();
    }
}
