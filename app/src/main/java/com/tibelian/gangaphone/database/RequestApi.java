package com.tibelian.gangaphone.database;

import android.util.Log;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpRequestBase;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.entity.FileEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.entity.StringEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RequestApi extends Thread {

    private final String API_URL = "http://gangaphone.tibelian.com/";
    private final String SECRET_KEY = "test";

    private String route;
    private String body;
    private String response;
    private boolean done;

    public RequestApi(String route, String body) {
        this.route = route;
        this.body = body;
    }

    public RequestApi(String route) {
        this.route = route;
    }

    public void run() {

        done = false;
        response = execute();
        done = true;

    }



    private String execute() {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpRequestBase request;
            if (body == null) {
                request = new HttpGet(API_URL+route);
            }
            else {
                request = new HttpPost(API_URL + route);
                StringEntity params = new StringEntity(body);
                ((HttpPost) request).setEntity(params);
            }
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.addHeader("authorization", SECRET_KEY);
            HttpResponse response = httpClient.execute(request);
            return readResponse(response);
        }
        catch (Exception ex) {
            Log.e("HttpClient API", "exception execute: " + ex);
            return "";
        }
    }

    private String readResponse(HttpResponse response) {
        String content = "";
        try {
            InputStream input = response.getEntity().getContent();
            int i = input.read();
            while (i != -1) {
                content += (char) i;
                i = input.read();
            }
        } catch(IOException io) {
            Log.e("HttpClient API", "exception response: " + io);
        }
        Log.e("readResponse", content);
        return content;
    }

    public String getResponse() {
        return response;
    }

    public boolean isDone() {
        return this.done;
    }

}
