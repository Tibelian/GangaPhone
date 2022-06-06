package com.tibelian.gangaphone.database.model;

import android.util.Log;

import com.tibelian.gangaphone.messenger.socket.MessengerManager;

import java.util.ArrayList;

/**
 * User Model
 */
public class User {

    // info and data about one user
    private int id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String location;
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Chat> chats = new ArrayList<>();

    // control variables to
    // detect if this user is connected
    // to the TCP server
    private boolean isOnline;
    private long lastConnUpdate;


    /////////////////////////
    // GETTERS AND SETTERS //
    /////////////////////////

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public ArrayList<Chat> getChats() {
        Log.e("User", "getChats() returned " + chats.size() + " items");
        return chats;
    }


    /**
     * Getter - Specific chat
     * @param uid
     * @return
     */
    public Chat getChatFrom(int uid) {
        for (Chat chat:chats)
            if (chat.getUser().getId() == uid)
                return chat;
        return null;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Set online the user just for 60 seconds
     * @param online
     */
    public void setOnline(boolean online) {
        isOnline = online;
        lastConnUpdate = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {

                final long timeToWait = 60000; // 60 seconds

                try { Thread.sleep(timeToWait); }
                catch (InterruptedException e) {}

                final long now = System.currentTimeMillis();
                Log.e("setOnline", now + " <= " + (lastConnUpdate+timeToWait));

                if (now <= (lastConnUpdate + timeToWait)) {
                    isOnline = false;
                    MessengerManager.notifyActivities(false);
                }

            }
        }).start();
    }

}
