package com.example.emergencyapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static Bitmap getResizedBitmap(Uri uri, int targetWidth, int targetHeight, ContentResolver contentResolver) {
        try {
            InputStream input = contentResolver.openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();

            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
                return null;

            int inSampleSize = 1;
            if (onlyBoundsOptions.outHeight > targetHeight || onlyBoundsOptions.outWidth > targetWidth) {
                final int heightRatio = Math.round((float) onlyBoundsOptions.outHeight / (float) targetHeight);
                final int widthRatio = Math.round((float) onlyBoundsOptions.outWidth / (float) targetWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = inSampleSize;
            bitmapOptions.inDither = true;
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            input = contentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Uri byteArrayToTempUri(Context context, byte[] byteArray, String fileName) {
        try {
            // Create a temporary file in the external cache directory
            File tempFile = new File(context.getExternalCacheDir(), fileName);
            tempFile.createNewFile();

            // Write the byte array to the newly created file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(byteArray);
            fos.flush();
            fos.close();

            // Return a URI using FileProvider
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
