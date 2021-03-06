package com.tibelian.gangaphone.database.model;

import java.util.Date;

/**
 * Message Model
 */
public class Message {

    // all message data and info
    private int id;
    private String content;
    private Date date;
    private boolean read;
    private User to;
    private User from;


    /////////////////////////
    // GETTERS AND SETTERS //
    /////////////////////////

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }
}
