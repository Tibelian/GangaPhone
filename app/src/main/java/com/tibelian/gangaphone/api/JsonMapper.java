package com.tibelian.gangaphone.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class JsonMapper {

    public static Message mapMessage(JsonObject data, boolean parseRelations) {
        Message m = new Message();

        m.setContent(data.get("content").getAsString());
        m.setRead(data.get("read").getAsBoolean());

        SimpleDateFormat f = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        try { m.setDate(f.parse(data.get("date").getAsString())); }
        catch (ParseException e) {}

        if (parseRelations) {
            if (data.get("to") != null && data.get("to").getAsJsonObject() != null)
                m.setTo(mapUser(data.get("to").getAsJsonObject(), false));
            if (data.get("from") != null && data.get("from").getAsJsonObject() != null)
                m.setFrom(mapUser(data.get("from").getAsJsonObject(), false));
        }
        return m;
    }

    public static Product mapProduct(JsonObject data, boolean parseRelations) {
        Product p = new Product();
        p.setId(data.get("id").getAsInt());
        p.setName(data.get("name").getAsString());
        p.setDescription(data.get("description").getAsString());
        p.setStatus(data.get("status").getAsString());
        p.setPrice(data.get("price").getAsFloat());
        p.setSold(data.get("sold").getAsBoolean());
        p.setVisits(data.get("visits").getAsInt());
        // date format
        SimpleDateFormat f = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        try { p.setDate(f.parse(data.get("date").getAsString())); }
        catch (ParseException e) {}
        // check for relations
        if (parseRelations) {
            ArrayList<ProductPicture> pp = new ArrayList<>();
            p.setPictures(pp);
            // map also pictures
            if (data.get("pictures") != null && data.get("pictures").getAsJsonArray() != null) {
                JsonArray jpp = data.get("pictures").getAsJsonArray();
                for(JsonElement jPic:jpp)
                    pp.add(mapProductPicture(
                            jPic.getAsJsonObject(), false));
            }
            // map also the owner
            if (data.get("owner") != null && data.get("owner").getAsJsonObject() != null)
                p.setOwner(mapUser(data.get("owner").getAsJsonObject(), false));
        }
        return p;
    }

    public static User mapUser(JsonObject data, boolean parseRelations) {
        User u = new User();
        u.setId(data.get("id").getAsInt());
        u.setUsername(data.get("username").getAsString());
        try {
            u.setPassword(data.get("password").getAsString());
        } catch(Exception e) {}
        u.setEmail(data.get("email").getAsString());
        u.setPhone(data.get("phone").getAsString());
        u.setLocation(data.get("location").getAsString());
        if (parseRelations) {
            // map products
            if (data.get("products") != null && data.get("products").getAsJsonArray() != null) {
                ArrayList<Product> pList = new ArrayList<>();
                u.setProducts(pList);
                JsonArray jp = data.get("products").getAsJsonArray();
                for(JsonElement p:jp)
                    pList.add(mapProduct(
                            p.getAsJsonObject(), true));
            }
            // map messages
            if (data.get("messages") != null && data.get("messages").getAsJsonArray() != null) {
                ArrayList<Message> mList = new ArrayList<>();
                u.setMessages(mList);
                JsonArray jm = data.get("messages").getAsJsonArray();
                for(JsonElement m:jm)
                    mList.add(mapMessage(
                            m.getAsJsonObject(), true));
            }
        }
        return u;
    }

    public static ProductPicture mapProductPicture(JsonObject data, boolean parseRelations) {
        ProductPicture pp = new ProductPicture();
        try { pp.setUrl(data.get("url").getAsString()); }
        catch (UnsupportedOperationException n) {}
        try { pp.setId(data.get("id").getAsInt()); }
        catch (UnsupportedOperationException n) {}
        if (parseRelations) {
            Product p = new Product();
            p.setId(data.get("product_id").getAsInt());
            pp.setProduct(p);
        }
        return pp;
    }

}
