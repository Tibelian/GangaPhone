package com.tibelian.gangaphone.database;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.HttpRequest;
import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseManager {

    public List<Product> getProducts(boolean useMainFilter) {

        // list of products
        ArrayList<Product> list = new ArrayList<>();

        try {
            // http request to api for products
            HttpRequest req = new HttpRequest(HttpRequest.API_URL_SEARCH_PRODUCTS);
            req.addParam("filter", CurrentFilter.toJson());
            req.init();
            req.start();
            req.join();

            String response = req.getResponse();
            Log.e("getProducts", "response --> " + response);

            // mapping result
            // { status: ok, data: [] }
            JsonObject jsonRes = new Gson().fromJson(response, JsonObject.class);
            if (jsonRes.get("status").getAsString().equals("ok")) {

                JsonArray jProducts = jsonRes.get("data").getAsJsonArray();
                for(JsonElement jp:jProducts) {

                    User pu = new User();
                    Product p = new Product();
                    ArrayList<ProductPicture> ppList = new ArrayList<>();
                    ProductPicture pp = new ProductPicture();
                    p.setOwner(pu);
                    p.setPictures(ppList);
                    ppList.add(pp);

                    p.setId(jp.getAsJsonObject().get("pid").getAsInt());
                    p.setName(jp.getAsJsonObject().get("name").getAsString());
                    p.setPrice(jp.getAsJsonObject().get("price").getAsFloat());
                    p.setStatus(jp.getAsJsonObject().get("status").getAsString());
                    p.setSold(jp.getAsJsonObject().get("sold").getAsBoolean());
                    p.setVisits(jp.getAsJsonObject().get("visits").getAsInt());

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
                    formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

                    try {
                        p.setDate(formatter.parse(jp.getAsJsonObject().get("date").getAsString()));
                    } catch (ParseException e) {}

                    pu.setId(jp.getAsJsonObject().get("uid").getAsInt());
                    pu.setLocation(jp.getAsJsonObject().get("location").getAsString());

                    try {
                        pp.setUrl(jp.getAsJsonObject().get("thumbnail").getAsString());
                        // catch exception if thumbnail is null
                    } catch(Exception e) {};

                    list.add(p);
                }

            } else {
                Log.e("api", "error status");
            }
        }
        catch (InterruptedException | IOException e) {
            Log.e("getProducts", "error --> " + e);
        }

        return list;
    }

    public Product getProduct(int productId) {
        // the product object
        Product product = new Product();

        // http request to api
        try {
            HttpRequest req = new HttpRequest(HttpRequest.API_URL_FIND_PRODUCT);
            req.replaceRoute("{id}", productId);
            req.init();
            req.start();
            req.join();

            String response = req.getResponse();

            // mapping result
            // { status: ok, data: {} }
            JsonObject jsonRes = new Gson().fromJson(response, JsonObject.class);
            if (jsonRes.get("status").getAsString().equals("ok")) {

                JsonObject jp = jsonRes.get("data").getAsJsonObject();
                product.setId(jp.get("id").getAsInt());
                product.setName(jp.get("name").getAsString());
                product.setDescription(jp.get("description").getAsString());
                product.setStatus(jp.get("status").getAsString());
                product.setPrice(jp.get("price").getAsFloat());
                product.setSold(jp.get("sold").getAsBoolean());
                product.setVisits(jp.get("visits").getAsInt());

                SimpleDateFormat f = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
                f.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                try { product.setDate(f.parse(jp.get("date").getAsString())); }
                catch (ParseException e) {}

                ArrayList<ProductPicture> productPictures = new ArrayList<>();
                product.setPictures(productPictures);
                JsonArray jpp = jp.get("pictures").getAsJsonArray();
                for(JsonElement jPic:jpp) {
                    ProductPicture ppP = new ProductPicture();
                    ppP.setUrl(jPic.getAsJsonObject().get("url").getAsString());
                    ppP.setId(jPic.getAsJsonObject().get("id").getAsInt());
                    productPictures.add(ppP);
                }

                User productOwner = new User();
                product.setOwner(productOwner);
                JsonObject jpu = jp.get("user").getAsJsonObject();
                productOwner.setId(jpu.get("id").getAsInt());
                productOwner.setUsername(jpu.get("username").getAsString());
                productOwner.setEmail(jpu.get("email").getAsString());
                productOwner.setPhone(jpu.get("phone").getAsString());
                productOwner.setLocation(jpu.get("location").getAsString());

            } else {
                Log.e("getProduct api", "error status");
            }
        }
        catch (IOException | InterruptedException e) {
            Log.e("getProduct", "error --> " + e);
        }

        return product;
    }

    public List<Chat> getChats() {

        ArrayList<Chat> chats = new ArrayList<>();

        User pepe = new User();
        pepe.setUsername("pepe");
        pepe.setOnline(true);

        User juan = new User();
        juan.setUsername("juan");

        Chat c1 = new Chat();
        c1.setUser(pepe);
        c1.setLastDate(new Date());
        c1.setLastMessage("Hola quiero comprar tu iphone");

        Chat c2 = new Chat();
        c2.setUser(juan);
        c2.setLastDate(new Date());
        c2.setLastMessage("Ok");

        chats.add(c1);
        chats.add(c2);

        return chats;
    }

    public List<Message> getMessages(User receiver) {

        ArrayList<Message> messages = new ArrayList<>();

        User paco = new User();
        paco.setUsername("paco");

        User yo = new User();
        yo.setUsername("tiberiu");

        Message m1 = new Message();
        m1.setFrom(paco);
        m1.setDate(new Date());
        m1.setContent("Hola quiero comprar tu iphone");

        Message m2 = new Message();
        m2.setFrom(yo);
        m2.setDate(new Date());
        m2.setContent("Hola");
        m2.setRead(true);

        Message m3 = new Message();
        m3.setFrom(paco);
        m3.setDate(new Date());
        m3.setContent("aún lo tienes?");

        Message m4 = new Message();
        m4.setFrom(yo);
        m4.setDate(new Date());
        m4.setContent("sí, nuevo sin abrir 900€");

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);
        messages.add(m4);

        return messages;
    }

    public boolean sendMessage(Message msg) {

        return false;
    }

    public int saveProduct(Product product, boolean isNew) {
        try {

            HttpRequest req = new HttpRequest(
                    isNew ? HttpRequest.API_URL_INSERT_PRODUCT
                            : HttpRequest.API_URL_UPDATE_PRODUCT);

            if (!isNew)
                product.setOwner(Session.get().getUser());

            int pCounter = 0;
            ArrayList<ProductPicture> pics = product.getPictures();
            for(int i = 0; i < pics.size(); i++) {
                if (pics.get(i).getUri() != null) {
                    req.addFile("picture" + pCounter, new File(pics.get(i).getRealpath()));
                    product.getPictures().remove(i);
                    i--; pCounter++;
                }
            }
            req.addParam("product", new Gson().toJson(product));

            req.init();
            req.start();
            req.join();

            String response = req.getResponse();
            Log.e("saveProduct", "response --> " + response);

            JsonObject jsonRes = new Gson().fromJson(response, JsonObject.class);
            if (jsonRes.get("status").getAsString().equals("ok")) {
                return jsonRes.get("data").getAsInt(); // product's id
            }
        }
        catch (IOException | InterruptedException e) {
            Log.e("saveProduct", "error --> " + e);
        }
        return -1;
    }


    public boolean deleteProduct(Product mProduct) {
        return false;
    }







    public int createUser(User user) {
        try {
            HttpRequest req = new HttpRequest(HttpRequest.API_URL_INSERT_USER);
            req.addParam("user", new Gson().toJson(user));

            req.init();
            req.start();
            req.join();

            String response = req.getResponse();
            Log.e("createUser", "response --> " + response);

            JsonObject jsonRes = new Gson().fromJson(response, JsonObject.class);
            if (jsonRes.get("status").getAsString().equals("ok")) {
                return jsonRes.get("data").getAsInt(); // users's id
            }
        }
        catch (IOException | InterruptedException e) {
            Log.e("createUser", "error --> " + e);
        }
        return -1;
    }

    private User getUser(String json) {
        User u = new User();
        try {
            HttpRequest req = new HttpRequest(HttpRequest.API_URL_FIND_USER);
            req.addParam("user", json);

            req.init();
            req.start();
            req.join();

            String response = req.getResponse();
            Log.e("getUser", "response --> " + response);

            JsonObject jsonRes = new Gson().fromJson(response, JsonObject.class);
            if (jsonRes.get("status").getAsString().equals("ok")) {

                try {
                    if (jsonRes.get("data").getAsJsonObject().get("id") == null)
                        return null; // not found
                } catch (IllegalStateException ie) { return null; }

                JsonObject udObj = jsonRes.get("data").getAsJsonObject();
                u.setId(udObj.get("id").getAsInt());
                u.setUsername(udObj.get("username").getAsString());
                //u.setPassword(udObj.get("password").getAsString());
                u.setEmail(udObj.get("email").getAsString());
                u.setLocation(udObj.get("location").getAsString());
                u.setPhone(udObj.get("phone").getAsString());

                ArrayList<Product> upList = new ArrayList<>();
                u.setProducts(upList);
                JsonArray upa = udObj.get("products").getAsJsonArray();
                for (JsonElement upe:upa) {
                    JsonObject upeObj = upe.getAsJsonObject();
                    Product up = new Product();
                    upList.add(up);

                    up.setId(upeObj.get("id").getAsInt());
                    up.setName(upeObj.get("name").getAsString());
                    up.setDescription(upeObj.get("description").getAsString());
                    up.setStatus(upeObj.get("status").getAsString());
                    up.setPrice(upeObj.get("price").getAsFloat());
                    up.setSold(upeObj.get("sold").getAsBoolean());
                    up.setVisits(upeObj.get("visits").getAsInt());

                    SimpleDateFormat f = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
                    f.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                    try { up.setDate(f.parse(upeObj.get("date").getAsString())); }
                    catch (ParseException e) {}

                    ArrayList<ProductPicture> uppList = new ArrayList<>();
                    up.setPictures(uppList);

                    JsonArray uppa = upeObj.get("pictures").getAsJsonArray();
                    for(JsonElement uppe:uppa) {
                        JsonObject uppObj = uppe.getAsJsonObject();
                        ProductPicture upp = new ProductPicture();
                        uppList.add(upp);
                        upp.setId(uppObj.get("id").getAsInt());
                        upp.setUrl(uppObj.get("url").getAsString());
                    }
                }
            } // end if status == ok
        }
        catch (IOException | InterruptedException e) {
            Log.e("getUser", "error --> " + e);
        }
        return u;
    }

    public User getUserById(int id) {
        return getUser("{\"id\":"+id+"}");
    }
    public User getUserByUsername(String username) {
        return getUser("{\"username\":\""+username+"\"}");
    }
    public User getUserByLogin(String username, String password) {
        return getUser("{\"username\":\""+username+"\", \"password\":\""+password+"\"}");
    }










}
