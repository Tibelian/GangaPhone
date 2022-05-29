package com.tibelian.gangaphone.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.database.model.Chat;
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

    public static Message mapMessage(JsonObject data) {
        Message m = new Message();

        m.setContent(data.get("content").getAsString());
        m.setRead(data.get("is_read").getAsBoolean());

        SimpleDateFormat f = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        try { m.setDate(f.parse(data.get("date").getAsString())); }
        catch (ParseException e) {}

        User from = new User();
        from.setId(data.get("sender_uid").getAsInt());
        from.setUsername(data.get("sender_uname").getAsString());
        m.setFrom(from);

        User to = new User();
        to.setId(data.get("receiver_uid").getAsInt());
        to.setUsername(data.get("receiver_uname").getAsString());
        m.setTo(to);

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
        f.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        try { p.setDate(f.parse(data.get("date").getAsString())); }
        catch (ParseException e) {}
        // check for relations
        if (parseRelations) {
            // map also the owner
            if (data.get("owner") != null && data.get("owner").getAsJsonObject() != null)
                p.setOwner(mapUser(data.get("owner").getAsJsonObject(), false));
        }

        //
        // always check for pictures
        //
        ArrayList<ProductPicture> pp = new ArrayList<>();
        p.setPictures(pp);
        if (data.get("pictures") != null && data.get("pictures").getAsJsonArray() != null) {
            JsonArray jpp = data.get("pictures").getAsJsonArray();
            for(JsonElement jPic:jpp)
                pp.add(mapProductPicture(
                        jPic.getAsJsonObject(), false));
        }
        return p;
    }

    public static User mapUser(JsonObject data, boolean parseRelations) {
        User u = new User();
        u.setId(data.get("id").getAsInt());
        try {
            u.setUsername(data.get("username").getAsString());
        } catch(Exception e) {}
        try {
            u.setPassword(data.get("password").getAsString());
        } catch(Exception e) {}
        try {
            u.setEmail(data.get("email").getAsString());
        } catch(Exception e) {}
        try {
            u.setPhone(data.get("phone").getAsString());
        } catch(Exception e) {}
        try {
            u.setLocation(data.get("location").getAsString());
        } catch(Exception e) {}

        if (parseRelations) {
            // map products
            if (data.get("products") != null && data.get("products").getAsJsonArray() != null) {
                ArrayList<Product> pList = new ArrayList<>();
                u.setProducts(pList);
                JsonArray jp = data.get("products").getAsJsonArray();
                for(JsonElement p:jp)
                    pList.add(mapProduct(p.getAsJsonObject(), true));
            }
            // map messages
            if (data.get("messages") != null && data.get("messages").getAsJsonArray() != null) {
                ArrayList<Message> mList = new ArrayList<>();
                JsonArray jm = data.get("messages").getAsJsonArray();
                for(JsonElement m:jm)
                    mList.add(mapMessage(m.getAsJsonObject()));
                u.setChats(mapChats(mList, u));
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

    public static ArrayList<Chat> mapChats(ArrayList<Message> messages, User currentUser) {
        ArrayList<Chat> chats = new ArrayList<>();
        for(Message msg:messages)
        {
            boolean added = false;
            for (Chat chat:chats)
            {
                // the prev user sent a message
                boolean fromPrev = msg.getFrom().getId() == chat.getUser().getId();

                // the prev user received a message
                boolean toPrev = msg.getTo().getId() == chat.getUser().getId();

                // insert message to the prev chat
                // if one of the previous options is true
                if (toPrev || fromPrev) {
                    chat.getMessages().add(msg);
                    added = true;
                    break;
                }
            }
            if (added == false) {
                Chat newChat = new Chat();
                chats.add(newChat);
                newChat.getMessages().add(msg);
                // detect the target
                // logged in user cant be the target
                if (msg.getFrom().getId() == currentUser.getId())
                    newChat.setUser(msg.getTo());
                else
                    newChat.setUser(msg.getFrom());
            }
        }
        return chats;
    }
}
