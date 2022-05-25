package com.tibelian.gangaphone.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.database.CurrentFilter;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.http2.Hpack;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Source;

public class RestApi {

    public static final String API_AUTH                       = "secret";
    public static final String API_URL                        = "https://gangaphone.tibelian.com";

    public static final String API_URL_SEARCH_PRODUCTS        = API_URL + "/product/search";
    public static final String API_URL_FIND_PRODUCT           = API_URL + "/product/{id}";
    public static final String API_URL_INSERT_PRODUCT         = API_URL + "/product/new";
    public static final String API_URL_UPDATE_PRODUCT         = API_URL + "/product/{id}";
    public static final String API_URL_UPDATE_PRODUCT_VISITS  = API_URL + "/product/{id}/visits";
    public static final String API_URL_INSERT_USER            = API_URL + "/user/new";
    public static final String API_URL_FIND_USER              = API_URL + "/user/find";


    // MANAGE PRODUCTS
    public ArrayList<Product> searchProducts(boolean useMainFilter) throws IOException {

        //
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "searchProducts error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                ArrayList<Product> list = new ArrayList<>();
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                // check if all is ok and return the product list
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    JsonArray jpa = jsonRes.get("data").getAsJsonArray();
                    for (JsonElement jpe:jpa)
                        list.add(
                                JsonMapper.mapProduct(jpe.getAsJsonObject(), true));
                }

                // save result to be returned
                syncResult.setResult(list);

            }
        });

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        if (useMainFilter)
            params.put("filter", CurrentFilter.toJson());

        // execute
        handler.postForm(API_URL_SEARCH_PRODUCTS, params);

        return (ArrayList<Product>) syncResult.getResult();

    }

    public Product findProduct(int productId) throws IOException {

        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                // check if all is ok and return the product list
                if (jsonRes.get("status").getAsString().equals("ok"))
                    syncResult.setResult(JsonMapper.mapProduct(
                            jsonRes.get("data").getAsJsonObject(), true));
            }
        });

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        String url = API_URL_FIND_PRODUCT.replace("{id}", ""+productId);

        // execute
        handler.get(url);

        return (Product) syncResult.getResult();

    }

    public Product createProduct(Product created) throws IOException {

        //
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "createProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the product's id
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    syncResult.setResult(
                            JsonMapper.mapProduct(jsonRes.get("data").getAsJsonObject(), true));
                    return;
                }
                syncResult.setResult(null);

            }
        });

        // prepare files to upload
        ArrayList<FileHandler> files = new ArrayList<>();
        ArrayList<ProductPicture> pics = created.getPictures();
        for(int i = 0; i < pics.size(); i++) {
            if (pics.get(i).getUri() == null)
                continue;
            FileHandler f = new FileHandler();
            f.setFile(new File(pics.get(i).getRealpath()));
            f.setFileKey("picture" + i);
            f.setFileName(f.getFile().getName());
            files.add(f);
        }

        // clean data which is not necessary
        Product toSend = created.clone();
        toSend.setPictures(null);
        User owner = new User();
        owner.setId(toSend.getOwner().getId());
        owner.setUsername(toSend.getOwner().getUsername());
        owner.setLocation(toSend.getOwner().getLocation());
        toSend.setOwner(owner);

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        params.put("product", new Gson().toJson(toSend));

        // execute
        handler.postMulti(API_URL_INSERT_PRODUCT, params, files);

        return (Product) syncResult.getResult();
    }

    public void addProductVisit(int productId) throws IOException  {

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "addProductVisit error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // obtain response as json
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok
                if (jsonRes.get("status").getAsString().equals("ok"))
                    Log.i("addProductVisit", "onResponse visits updated successfully");
                else
                    Log.e("addProductVisit", "onResponse unexpected status");
            }
        });

        // execute
        String url = API_URL_UPDATE_PRODUCT_VISITS
                .replace("{id}", "" + productId);
        handler.postForm(url, new HashMap<>());

    }

    public Product updateProduct(Product updated) throws IOException {

        //
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "updateProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the product's id
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    syncResult.setResult(
                            JsonMapper.mapProduct(jsonRes.get("data").getAsJsonObject(), true));
                    return;
                }
                syncResult.setResult(null);

            }
        });

        // prepare data to send
        Product toSend = new Product();
        toSend.setName(updated.getName());
        toSend.setDescription(updated.getDescription());
        toSend.setStatus(updated.getStatus());
        toSend.setPrice(updated.getPrice());
        toSend.setSold(updated.isSold());

        int upCount = 0;
        ArrayList<FileHandler> files = new ArrayList<>();   // pics to upload
        ArrayList<ProductPicture> pics = new ArrayList<>(); // pics to keep
        for(ProductPicture pic:updated.getPictures()) {
            if (pic.getUri() != null) {
                // pictures that we are uploading
                FileHandler f = new FileHandler();
                f.setFile(new File(pic.getRealpath()));
                f.setFileKey("picture" + upCount);
                f.setFileName(f.getFile().getName());
                files.add(f);
                upCount++;
            } else {
                // pictures that we will keep
                ProductPicture toKeep = new ProductPicture();
                toKeep.setId(pic.getId());
            }
        }
        toSend.setPictures(pics);

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        params.put("product", new Gson().toJson(toSend));

        // execute
        String url = API_URL_UPDATE_PRODUCT
                .replace("{id}", "" + updated.getId());
        handler.postMulti(url, params, files);

        // the product updated is returned
        return (Product) syncResult.getResult();
    }


    // MANAGE MESSAGES



    // MANAGE USER
    public User findUser(String json) throws IOException {

        //
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findUser error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                // check if all is ok and return the user
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    try {
                        syncResult.setResult(JsonMapper.mapUser(
                                jsonRes.get("data").getAsJsonObject(), true));
                        Log.e("onResponse", "findUser syncResult done ");
                    } catch (IllegalStateException e) {
                        Log.e("onResponse", "findUser error --> " + e);
                        // if not found
                        syncResult.setResult(new User());
                    }
                }
            }
        });

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        params.put("user", json);

        // execute
        handler.postForm(API_URL_FIND_USER, params);

        return (User) syncResult.getResult();

    }

    public User findUserById(int id) throws IOException {
        return findUser("{\"id\":"+id+"}");
    }

    public User findUserByUsername(String username) throws IOException {
        return findUser("{\"username\":\""+username+"\"}");
    }

    public User findUserByLogin(String username, String password) throws IOException {
        return findUser("{\"username\":\""+username+"\", \"password\":\""+password+"\"}");
    }

    public int createUser(User created) throws IOException {

        //
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "createUser error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the user's id
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    syncResult.setResult(jsonRes.get("data").getAsInt());
                    return;
                }
                syncResult.setResult(-1);

            }
        });

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        params.put("user", new Gson().toJson(created));

        // execute
        handler.postForm(API_URL_INSERT_USER, params);

        return (int) syncResult.getResult();
    }


}
