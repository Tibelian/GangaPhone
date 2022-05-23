package com.tibelian.gangaphone.api;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HttpRequest extends Thread {

    public static final String API_URL = "https://gangaphone.tibelian.com";
    public static final String API_URL_SEARCH_PRODUCTS = API_URL + "/product/search";
    public static final String API_URL_FIND_PRODUCT = API_URL + "/product/{id}";
    public static final String API_URL_INSERT_PRODUCT = API_URL + "/product/new";
    public static final String API_URL_UPDATE_PRODUCT = API_URL + "/product/{id}";
    public static final String API_URL_INSERT_USER = API_URL + "/user/new";
    public static final String API_URL_FIND_USER = API_URL + "/user/find";
    public static final String API_AUTHORIZATION = "secret";

    private String url;
    private String charset = "UTF-8";
    private String boundary = Long.toHexString(System.currentTimeMillis());
    private String CRLF = "\r\n";

    private HttpsURLConnection connection;
    private OutputStream output;
    private String response;
    private int responseCode;

    private HashMap<String, String> params = new HashMap<>();
    private HashMap<String, Integer> filesToSend = new HashMap<>();
    private ArrayList<File> files = new ArrayList<>();

    public HttpRequest(String url) {
        this.url = url;
    }

    public void init() throws IOException {
        connection = (HttpsURLConnection) new URL(url).openConnection();

        // enable POST method
        if (files.size() != 0 || params.size() != 0)
            connection.setDoOutput(true);

        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Accept", "*/*");
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                // return always true
                // so we will prevent this error
                // javax.net.ssl.SSLPeerUnverifiedException
                return true;
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        try {

            execute();

        } catch (IOException e) {
            Log.e("thread.run", "exception: " + e);
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void execute() throws IOException {

        // multipart
        if (files.size() > 0)
        {
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            output = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, charset));
            for (Map.Entry<String, String> param: params.entrySet())
                writerAppend(writer, param.getKey(), param.getValue());
            for (Map.Entry<String, Integer> file: filesToSend.entrySet())
                writerAppend(writer, file.getKey(), files.get(file.getValue()));
            // mark the end of the multipart http request
            writer.append("--" + boundary).append(CRLF);
            writer.flush();
            writer.close();
        }

        // only post method
        else if(params.size() > 0)
        {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param: params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            output = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, charset));
            writer.write(postData.toString());
            writer.flush();
            writer.close();
        }

        responseCode = connection.getResponseCode();
        if (responseCode == 200) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder res = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;)
                res.append(line).append("\n");
            response = res.toString();
            reader.close();

        } else {
            Log.e("HttpRequest error", "RESPONSE CODE --> " + responseCode);
            Log.e("HttpRequest error", "url --> " + url);
        }

        connection.disconnect();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writerAppend(BufferedWriter writer, String fileName, File theFile) throws IOException {
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\""+fileName+"\"; filename=\"" + theFile.getName() + "\"").append(CRLF);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(theFile.getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();

        // write the actual file contents
        FileInputStream inputStreamFile = new FileInputStream(theFile);
        int bytesRead;
        byte[] dataBuffer = new byte[(int) theFile.length()];

        while((bytesRead = inputStreamFile.read(dataBuffer)) != -1) {
            output.write(dataBuffer, 0, bytesRead);
        }
        output.flush();
        Log.e("writing file", Uri.fromFile(theFile).getPath());

        //IOUtils.copy(new FileInputStream(theFile), output);
        //Files.copy(theFile.toPath(), output);
    }
    private void writerAppend(BufferedWriter writer, String paramName, String param) throws IOException {
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\""+paramName+"\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).append(param).append(CRLF).flush();
    }

    private byte[] getBytes(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        return bytes;
    }


    public void replaceRoute(String key, String value) {
        url = url.replace(key, value);
    }
    public void replaceRoute(String key, int value) {
        url = url.replace(key, String.valueOf(value));
    }
    public void addParam(String key, String value) {
        params.put(key, value);
    }
    public void addFile(String key, File file) {
        files.add(file);
        filesToSend.put(key, files.size() - 1);
    }

    public String getResponse() {
        return response;
    }
    public int getResponseCode() {
        return responseCode;
    }

}
