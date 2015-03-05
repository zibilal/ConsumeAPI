package com.zibilal.consumeapi.lib.rawbyte;

//import android.util.Log;

import com.zibilal.consumeapi.lib.network.Response;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bmuhamm on 4/22/14.
 */
public class RawParser {
    private static final String TAG="RawParser";

    public static Object parse(Class<? extends Response> clss, InputStream input) throws XmlPullParserException, IOException {
        try {
            ResponseRaw raw = (ResponseRaw) clss.newInstance();

            return raw.getResponse(input);
        } catch (InstantiationException e) {
            //Log.e(TAG, "Exception : " + e.getMessage());
        } catch (IllegalAccessException e) {
            //Log.e(TAG, "Exception : " + e.getMessage());
        } catch (Exception e) {
            //Log.e(TAG, "Exception : " + e.getMessage());
        } finally {
            input.close();
        }

        return null;
    }
}
