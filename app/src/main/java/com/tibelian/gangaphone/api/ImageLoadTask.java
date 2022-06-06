package com.tibelian.gangaphone.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * CURL connection to download image
 */
public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    // url to access
    private String url;

    // image to show
    private ImageView imageView;

    /**
     * Constructor
     * @param url
     * @param imageView
     */
    public ImageLoadTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    /**
     * Connect and obtain image as bitmap
     * @param params
     * @return Bitmap
     */
    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            // target url
            URL urlConnection = new URL(url);
            // stat conn
            HttpsURLConnection connection = (HttpsURLConnection) urlConnection
                    .openConnection();
            // receive data
            connection.setDoInput(true);
            // do not check the ssl
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            // execute
            connection.connect();
            // interpret input data as bitmap
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            Log.e("ImageLoadTask", "url --> " + url);
        }
        return null;
    }

    /**
     * when this task is finished update the view
     * @param result
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (imageView != null)
            imageView.setImageBitmap(result);
    }

}