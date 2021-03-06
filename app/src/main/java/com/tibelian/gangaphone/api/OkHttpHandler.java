package com.tibelian.gangaphone.api;


import static com.tibelian.gangaphone.api.RestApi.API_AUTH_SECRET;
import static com.tibelian.gangaphone.api.RestApi.API_AUTH_TOKEN;

import com.tibelian.gangaphone.utils.FileHandler;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Manage basic REST API requests
 */
public class OkHttpHandler {

    // the 'browser'
    private OkHttpClient client;

    // the method to execute when task is finished
    private Callback callback;

    // json header
    public static final MediaType JSON= MediaType.get("application/json; charset=utf-8");

    /**
     * Generate the secret authorization JWT
     * @return String
     */
    private String getAuth() {
        SecretKey key = Keys.hmacShaKeyFor(API_AUTH_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claim("token", API_AUTH_TOKEN)
                .claim("stamp", new Date().getTime())
                .signWith(key)
                .compact();
    }

    /**
     * Constructor
     * @param callback
     */
    public OkHttpHandler(Callback callback) {
        // set the callback
        this.callback = callback;
        // set the most basic interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // append interceptor to the client builder
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(logging);
        // do not verify the ssl
        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        // build the 'browser'
        client = clientBuilder.build();
    }

    /**
     * Send POST request as multipart data
     * @param url
     * @param params
     * @param files
     * @return
     * @throws IOException
     */
    public void postMulti(String url, HashMap<String, String> params, List<FileHandler> files) throws IOException {

        MultipartBody.Builder body = new MultipartBody.Builder();
        body.setType(MultipartBody.FORM); // important

        // insert args
        for (Map.Entry<String, String> param: params.entrySet())
            body.addFormDataPart(param.getKey(), param.getValue());

        // insert files
        for (FileHandler file:files) {
            String type = URLConnection.guessContentTypeFromName(file.getFile().getName());
            body.addFormDataPart(file.getFileKey(), file.getFileName(),
                    RequestBody.create(file.getFile(), MediaType.parse(type)));
        }

        // build the request
        Request request = new Request.Builder()
                .header("Authorization", "Brearer " + getAuth())
                .url(url)
                .post(body.build())
                .build();
        // execute and obtain a response
        client.newCall(request).enqueue(callback);
    }

    /**
     * Send POST request as simple form
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public void postForm(String url, HashMap<String, String> params) throws IOException {
        // create form
        FormBody.Builder formBody = new FormBody.Builder();
        // append params
        for (Map.Entry<String, String> param: params.entrySet())
            formBody.add(param.getKey(), param.getValue());
        // build the request
        Request request = new Request.Builder()
                .header("Authorization", "Brearer " + getAuth())
                .url(url)
                .post(formBody.build())
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Send POST request as JSON
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    public void postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .header("Authorization", "Brearer " + getAuth())
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Send GET request
     * @param url
     * @return
     * @throws IOException
     */
    public void get(String url) throws IOException {
        Request request = new Request.Builder()
                .header("Authorization", "Brearer " + getAuth())
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
