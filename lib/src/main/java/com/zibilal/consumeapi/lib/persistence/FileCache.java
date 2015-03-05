package com.zibilal.consumeapi.lib.persistence;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.zibilal.consumeapi.lib.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by bmuhamm on 4/3/14.
 */
public class FileCache {

    private static final String TAG="FileCache";

    private File cacheDir;
    private Context mContext;

    public FileCache(Context context) throws IOException{

        mContext = context;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && isExternalStorageWritable()) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "thumbnails");
        } else {
            cacheDir = mContext.getCacheDir();
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public void setCacheDir(File file) {
        cacheDir=file;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else
            return false;
    }

    public boolean fileExists(String url) {
        File file = getFile(url);
        return file.exists();
    }

    public void save(String filename, byte[] input) throws IOException {
        File file = getFile(filename);
        if(!file.exists()) {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            try {
                outputStream.write(input);
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } else {
            Log.d(TAG, "File --> " + file.getName() + " already exists");
        }
    }

    public byte[] getByteData(String url) throws IOException {
        File file = getFile(url);
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[512];
        int readByte = inputStream.read(buffer);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(buffer, 0, readByte);
        while(readByte > 0) {
            readByte = inputStream.read(buffer);
            baos.write(buffer, 0, readByte);
        }

        return baos.toByteArray();
    }

    public File getFile(String filename) {
        //String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
        return new File(cacheDir, filename);
    }

    public void clear(){
        File[] files = cacheDir.listFiles();
        if(files == null) return;

        for(File f: files)
            f.delete();

        // also delete the directory
        if(cacheDir.exists())
            cacheDir.delete();
    }
}
