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
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {

    public static Bitmap getResizedBitmap(String urlString, int targetWidth, int targetHeight) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();

            // Check if we successfully obtained the dimensions
            if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
                return null;
            }

            // Calculate the inSampleSize
            int inSampleSize = 1;
            if (onlyBoundsOptions.outHeight > targetHeight || onlyBoundsOptions.outWidth > targetWidth) {
                final int heightRatio = Math.round((float) onlyBoundsOptions.outHeight / (float) targetHeight);
                final int widthRatio = Math.round((float) onlyBoundsOptions.outWidth / (float) targetWidth);
                inSampleSize = Math.min(heightRatio, widthRatio);
            }

            // Decode bitmap with inSampleSize set
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = inSampleSize;
            bitmapOptions.inDither = true;
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private InputStream getInputStreamFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return urlConnection.getInputStream();
        } else {
            throw new IOException("Failed to fetch data from URL: " + urlString + " with response code: " + urlConnection.getResponseCode());
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
