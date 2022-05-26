package com.tibelian.gangaphone.database.model;

import java.util.ArrayList;

public class Chat {

    private User user;
    private ArrayList<Message> messages = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
