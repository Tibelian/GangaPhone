package com.tibelian.gangaphone.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {

    private static DatabaseManager sDatabaseManager;
    private Context mContext;

    private DatabaseManager(Context context) {
        mContext = context.getApplicationContext();
        // @todo open conn
    }

    public static DatabaseManager get(Context context) {
        if (sDatabaseManager == null)
            sDatabaseManager = new DatabaseManager(context);
        return sDatabaseManager;
    }

    public List<Product> getProducts(boolean useMainFilter) {

        ArrayList<Product> list = new ArrayList<>();

        User tiberiu = new User();
        tiberiu.setUsername("tiberiu");
        tiberiu.setLocation("albacete");

        ProductPicture pp1 = new ProductPicture();
        pp1.setUrl("https://www.muycomputer.com/wp-content/uploads/2019/10/dise%C3%B1o-del-Phone-12.jpg");

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        Product p1 = new Product();
        p1.setName("xiaomi pocophone f1");
        try {
            p1.setDate(formatter.parse("25-04-2022 23:30:12"));
        } catch(ParseException pe) {}
        p1.setPrice(250.50f);
        p1.setStatus("NEW");
        p1.setOwner(tiberiu);
        p1.setDescription("product's description here");
        p1.setPictures(new ArrayList<ProductPicture>());
        p1.getPictures().add(pp1);
        Log.e("Pictures`",p1.getPictures().size() + "");

        Product p2 = p1.clone();
        p2.setPrice(200);
        p2.setName("samsung a40");
        try {
            p2.setDate(formatter.parse("26-04-2022 17:30:12"));
        } catch(ParseException pe) {}
        p2.setPictures(new ArrayList<>());


        Product p3 = p1.clone();
        p3.setPrice(120);
        p3.setName("iphone 13");
        p2.setPictures(new ArrayList<>());
        try {
            p3.setDate(formatter.parse("26-04-2022 18:06:02"));
        } catch(ParseException pe) {}

        p1.setId(1);
        p2.setId(2);
        p3.setId(3);

        list.add(p1);
        list.add(p2);
        list.add(p3);

        return list;
    }

    public Product getProduct(int productId) {

        // @todo select form database

        // test
        List<Product> products = getProducts(true);
        for(Product p:products) {
            if (p.getId() == productId) return p;
        }

        return null;
    }

}
