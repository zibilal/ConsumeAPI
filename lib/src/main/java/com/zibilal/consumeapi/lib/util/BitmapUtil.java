package com.zibilal.consumeapi.lib.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;

/**
 * Created by bmuhamm on 4/3/14.
 */
public class BitmapUtil {
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height/ 2;
            final int halfWidth = width / 2;

            while ( (halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(byte[] bitmapArray, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length, options);

        options.inSampleSize=calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds=false;

        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length, options);

        return bitmap;
    }

    public static Bitmap decodeSampledBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        int bytes = bitmap.getRowBytes() * bitmap.getHeight(); //bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] array = buffer.array();

        return decodeSampledBitmap(array, reqWidth, reqHeight);
    }
}
