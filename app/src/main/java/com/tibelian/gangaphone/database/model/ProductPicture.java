package com.tibelian.gangaphone.database.model;

import android.net.Uri;

public class ProductPicture {

    private int id;
    private String url;
    private String description;
    private Product product;

    //
    private Uri uri;
    private String realpath;
    //

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setRealpath(String realpath) {
        this.realpath = realpath;
    }

    public String getRealpath() {
        return realpath;
    }
}
