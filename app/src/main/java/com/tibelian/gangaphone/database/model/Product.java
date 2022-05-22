package com.tibelian.gangaphone.database.model;

import java.util.ArrayList;
import java.util.Date;

public class Product implements Cloneable {

    private int id;
    private String name;
    private String description;
    private Date date;
    private String status;
    private float price;
    private boolean sold;
    private int visits;
    private ArrayList<ProductPicture> pictures = new ArrayList<>();
    private User owner;

    public Product clone() {
        try {
            return (Product) super.clone();
        } catch(CloneNotSupportedException ce){
            return null;
        }
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public ArrayList<ProductPicture> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<ProductPicture> pictures) {
        this.pictures = pictures;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
