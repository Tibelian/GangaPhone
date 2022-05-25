package com.tibelian.gangaphone.database;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.HttpRequest;
import com.tibelian.gangaphone.api.RestApi;
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

    public boolean deleteProduct(Product mProduct) {
        return false;
    }













}
