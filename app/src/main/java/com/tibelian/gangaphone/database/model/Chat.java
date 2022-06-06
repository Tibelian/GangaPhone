package com.tibelian.gangaphone.database.model;

import java.util.ArrayList;

/**
 * Chat Model
 */
public class Chat {

    // target
    private User user;

    // all messages
    private ArrayList<Message> messages = new ArrayList<>();


    /////////////////////////
    // GETTERS AND SETTERS //
    /////////////////////////

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
