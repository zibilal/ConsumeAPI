package com.zibilal.consumeapi.lib.rawbyte;

import com.zibilal.consumeapi.lib.network.Response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by bmuhamm on 4/22/14.
 */
public class ResponseRaw implements Response {

    private byte[] rawBytes;

    public ResponseRaw(){}

    public Response getResponse(InputStream inputStream) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];

        int byteReads = bufferedInputStream.read(buff);
        while(byteReads > 0) {
            baos.write(buff, 0, byteReads);
            byteReads = bufferedInputStream.read(buff);
        }
        bufferedInputStream.close();
        rawBytes=baos.toByteArray();

        return this;
    }

    @Override
    public Object responseData() {
        return rawBytes;
    }
}
