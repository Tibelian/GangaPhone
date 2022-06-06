package com.tibelian.gangaphone.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.database.CurrentFilter;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.utils.FileHandler;
import com.tibelian.gangaphone.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * The database manager
 */
public class RestApi {

    // JWT AUTHORIZATION and TARGET
    public static final String API_AUTH_TOKEN                 = "g6vckky+GFwl50kJ";
    public static final String API_AUTH_SECRET                = "ExNCbWex68pdxiF+mXtSPnbLsow7JTWe";
    public static final String API_URL                        = "https://gangaphone.tibelian.com";

    // each petition
    public static final String URI_SEARCH_PRODUCTS        = API_URL + "/product/search";
    public static final String URI_FIND_PRODUCT           = API_URL + "/product/{id}";
    public static final String URI_INSERT_PRODUCT         = API_URL + "/product/new";
    public static final String URI_UPDATE_PRODUCT         = API_URL + "/product/{id}";
    public static final String URI_UPDATE_PRODUCT_VISITS  = API_URL + "/product/{id}/visits";
    public static final String URI_DELETE_PRODUCT         = API_URL + "/product/delete/{id}";
    public static final String URI_INSERT_USER            = API_URL + "/user/new";
    public static final String URI_FIND_USER              = API_URL + "/user/find";
    public static final String URI_FIND_ALL_MESSAGES      = API_URL + "/message/all/{userId}";
    public static final String URI_FIND_MESSAGES          = API_URL + "/message/from/{from}/to/{to}";
    public static final String URI_FIND_MESSAGE           = API_URL + "/message/{id}";
    public static final String URI_INSERT_MESSAGE         = API_URL + "/message/new";

    // MANAGE PRODUCTS

    /**
     * Obtain the full list of products, you can apply the main filter
     * @param useMainFilter
     * @return ArrayList<Product>
     * @throws IOException
     */
    public ArrayList<Product> searchProducts(boolean useMainFilter) throws IOException {

        // synchronize the result
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "searchProducts error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // list of products
                ArrayList<Product> list = new ArrayList<>();

                // interpret the result
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
        handler.postForm(URI_SEARCH_PRODUCTS, params);

        return (ArrayList<Product>) syncResult.getResult();

    }

    /**
     * Obtain one single product knowing his id
     * @param productId
     * @return Product
     * @throws IOException
     */
    public Product findProduct(int productId) throws IOException {

        // wait for the result
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // interpret the result as char stream
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
        String url = URI_FIND_PRODUCT.replace("{id}", ""+productId);

        // execute
        handler.get(url);

        // send the result synchronized
        return (Product) syncResult.getResult();

    }

    /**
     * Insert new product to the database
     * @param created
     * @return Product
     * @throws IOException
     */
    public Product createProduct(Product created) throws IOException {

        // we will wait for the result
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "createProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // interpret result as char stream
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();

                // obtain response as json
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the product's id
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    syncResult.setResult(
                            JsonMapper.mapProduct(jsonRes.get("data").getAsJsonObject(), true));
                    return;
                }

                // if error the result is null
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
        handler.postMulti(URI_INSERT_PRODUCT, params, files);

        // wait for the synchronized result
        return (Product) syncResult.getResult();
    }

    /**
     * Update the product's visits
     * @param productId
     * @throws IOException
     */
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
        String url = URI_UPDATE_PRODUCT_VISITS
                .replace("{id}", "" + productId);
        handler.postForm(url, new HashMap<>());

    }

    /**
     * Save the modified product to the database
     * @param updated
     * @return Product
     * @throws IOException
     */
    public Product updateProduct(Product updated) throws IOException {

        // result's synchronizer
        final SyncResult syncResult = new SyncResult();

        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "updateProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // interpret result as char stream
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
                pics.add(toKeep);
            }
        }
        toSend.setPictures(pics);

        // prepare params
        HashMap<String, String> params = new HashMap<>();
        params.put("product", new Gson().toJson(toSend));

        // execute
        String url = URI_UPDATE_PRODUCT
                .replace("{id}", "" + updated.getId());
        handler.postMulti(url, params, files);

        // the product updated is returned
        return (Product) syncResult.getResult();
    }

    /**
     * Remove one product from database
     * @param productId
     * @return boolean
     * @throws IOException
     */
    public boolean deleteProduct(int productId) throws IOException {
        // result's synchronizer
        SyncResult syncResult = new SyncResult();

        // init the browser with the following callback
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "searchProducts error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // interpret result
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                // check if the result is a valid json
                // and if the status is OK
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                syncResult.setResult(
                        jsonRes.get("status").getAsString().equals("ok"));
            }
        });
        // execute the request
        handler.postForm(
                URI_DELETE_PRODUCT.replace("{id}", ""+productId), new HashMap<>());
        return (boolean) syncResult.getResult();
    }


    // MANAGE MESSAGES

    /**
     * Select user's messages
     * @param userId
     * @return ArrayList<Message>
     * @throws IOException
     */
    public ArrayList<Message> findMessages(int userId) throws IOException {

        // result synchronizer
        final SyncResult syncResult = new SyncResult();

        // init the request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findProduct error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // obtain response as json
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the messages
                ArrayList<Message> msgs = new ArrayList<>();
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    JsonArray jma = jsonRes.get("data").getAsJsonArray();
                    for (JsonElement jme : jma)
                        msgs.add(JsonMapper.mapMessage(jme.getAsJsonObject()));
                }
                syncResult.setResult(msgs);
            }
        });

        // prepare params
        String url = URI_FIND_ALL_MESSAGES
                .replace("{userId}", "" + userId);
        // execute
        handler.get(url);

        // wait for the result
        return (ArrayList<Message>) syncResult.getResult();
    }

    /**
     * Select all messages where receiver and sender is specified
     * @param from
     * @param to
     * @return ArrayList<Message>
     * @throws IOException
     */
    public ArrayList<Message> findMessages(int from, int to) throws IOException {
        // result synchronizer
        final SyncResult syncResult = new SyncResult();

        // init the request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findMessages from/to error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // obtain response as json
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);

                // check if all is ok and return the messages
                ArrayList<Message> msgs = new ArrayList<>();
                if (jsonRes.get("status").getAsString().equals("ok")) {
                    JsonArray jma = jsonRes.get("data").getAsJsonArray();
                    for (JsonElement jme : jma)
                        msgs.add(JsonMapper.mapMessage(jme.getAsJsonObject()));
                }
                syncResult.setResult(msgs);
            }
        });

        // prepare params
        String url = URI_FIND_MESSAGES
                .replace("{from}", "" + from)
                .replace("{to}", "" + to);
        // execute
        handler.get(url);

        // wait for the result
        return (ArrayList<Message>) syncResult.getResult();
    }

    /**
     * Select one single message where ID
     * @param id
     * @return Message
     * @throws IOException
     */
    public Message findMessage(int id) throws IOException {
        // result synchronizer
        final SyncResult syncResult = new SyncResult();
        // init request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "findMessage error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // obtain response as json
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                // check if all is ok and return the messages
                if (jsonRes.get("status").getAsString().equals("ok"))
                    syncResult.setResult(JsonMapper.mapMessage(jsonRes.get("data").getAsJsonObject()));
                else
                    syncResult.setResult(null);
            }
        });

        // prepare params
        String url = URI_FIND_MESSAGE
                .replace("{id}", "" + id);
        // execute
        handler.get(url);
        // wait for result
        return (Message) syncResult.getResult();
    }

    /**
     * Insert new message
     * @param created
     * @return int
     * @throws IOException
     */
    public int createMessage(Message created) throws IOException {
        // result synchronizer
        final SyncResult syncResult = new SyncResult();
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "createMessage error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Reader resReader = response.peekBody(Integer.MAX_VALUE).charStream();
                JsonObject jsonRes = new Gson().fromJson(resReader, JsonObject.class);
                if (jsonRes.get("status").getAsString().equals("ok"))
                    syncResult.setResult(jsonRes.get("data").getAsInt());
                else
                    syncResult.setResult(null);
            }
        });
        // the request is the new message data as json
        HashMap<String, String> params = new HashMap<>();
        params.put("message", new Gson().toJson(created));
        handler.postForm(URI_INSERT_MESSAGE, params);
        return (int) syncResult.getResult();
    }


    // MANAGE USER

    /**
     * Select one user where data is parsed as JSON
     * @param json
     * @return User
     * @throws IOException
     */
    public User findUser(String json) throws IOException {
        // result synchronizer
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
        handler.postForm(URI_FIND_USER, params);
        // wait for result
        return (User) syncResult.getResult();

    }

    /**
     * select user by id
     * @param id
     * @return User
     * @throws IOException
     */
    public User findUserById(int id) throws IOException {
        return findUser("{\"id\":"+id+"}");
    }

    /**
     * select user by username
     * @param username
     * @return
     * @throws IOException
     */
    public User findUserByUsername(String username) throws IOException {
        return findUser("{\"username\":\""+username+"\"}");
    }

    /**
     * select user by login credentials
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public User findUserByLogin(String username, String password) throws IOException {
        return findUser("{\"username\":\""+username+"\", \"password\":\""+password+"\"}");
    }

    /**
     * Insert user into the database
     * @param created
     * @return
     * @throws IOException
     */
    public int createUser(User created) throws IOException {
        // result synchronizer
        final SyncResult syncResult = new SyncResult();
        // create request
        OkHttpHandler handler = new OkHttpHandler(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure", "createUser error --> " + e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // result as char stream
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
        handler.postForm(URI_INSERT_USER, params);

        // wait for result
        return (int) syncResult.getResult();
    }

}
