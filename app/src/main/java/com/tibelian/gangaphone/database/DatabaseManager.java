package com.tibelian.gangaphone.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.database.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
    private Connection conn;

    /**
     * Create connection. Obtain credentials using the static Database object
     */
    private void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    Configuration.URL, Configuration.DB_USER, Configuration.DB_PASS);
        }
        catch(Exception e) {
            Log.e("DatabaseManager", "connection error: " + e.getMessage());
        }
    }

    /**
     * Close connection
     */
    private void closeConnection() {
        try { conn.close(); }
        catch(Exception e) {}
    }

    private DatabaseManager(Context context) {
        mContext = context.getApplicationContext();
        connect();
    }

    public static DatabaseManager get(Context context) {
        if (sDatabaseManager == null)
            sDatabaseManager = new DatabaseManager(context);
        return sDatabaseManager;
    }

    public List<Product> getProducts(boolean useMainFilter) {

        // list of products
        ArrayList<Product> list = new ArrayList<>();

        // prepare query str
        String query = "SELECT p.*, p.id as pid, u.id as uid, pp.url AS thumbnail " +
                "FROM product p LEFT JOIN user u ON p.user_id = u.id " +
                "LEFT JOIN product_picture pp ON p.id = pp.product_id " +
                "WHERE p.sold = 0" + (useMainFilter ? getFilterQuery() : "") +
                " GROUP BY p.id;";

        try {
            // create statement
            Statement stm = conn.createStatement();

            // execute
            if (stm.execute(query)) {
                ResultSet result = stm.getResultSet();
                while (result.next()) {
                    User pu = new User();
                    Product p = new Product();
                    ProductPicture pp = new ProductPicture();
                    ArrayList<ProductPicture> ppList = new ArrayList<>();
                    ppList.add(pp);
                    p.setId(result.getInt("id"));
                    p.setName(result.getString("name"));
                    p.setPrice(result.getFloat("price"));
                    p.setStatus(result.getString("status"));
                    p.setSold(result.getBoolean("sold"));
                    p.setVisits(result.getInt("visits"));
                    p.setOwner(pu);
                    pu.setId(result.getInt("uid"));
                    pu.setUsername(result.getString("username"));
                    pu.setLocation(result.getString("location"));
                    pp.setUrl(result.getString("thumbnail"));
                    p.setPictures(ppList);
                    list.add(p);
                }
            }
            stm.close();
        } catch(Exception e) {
            Log.e("DatabaseManager", "exception: " + e.getMessage());
        }

        return list;
    }

    private String getFilterQuery() {
        String query = "";

        // status
        if (CurrentFilter.status.size() > 0) {
            query += " AND p.status IN (";
            for (int i = 0; i < CurrentFilter.status.size(); i++) {
                if (i != 0) query += ",";
                query += CurrentFilter.status.get(i);
            }
            query += ")";
        }

        // keyword
        if (CurrentFilter.keyword.length() > 2)
            query += " AND (p.name LIKE = '%" + CurrentFilter.keyword + "%' OR " +
                    "p.description LIKE = '%" + CurrentFilter.keyword + "%')";

        // location
        if (CurrentFilter.location.length() > 0)
            query += " AND u.location = '"+ CurrentFilter.location +"'";

        // price
        if (CurrentFilter.maxPrice > -1)
            query += " AND p.price < " + CurrentFilter.maxPrice;
        if (CurrentFilter.minPrice > -1)
            query += " AND p.price > " + CurrentFilter.minPrice;

        // order
        switch (CurrentFilter.orderBy) {
            case "date.asc":
                query += " ORDER BY p.date ASC"; break;
            case "date.desc":
                query += " ORDER BY p.date DESC"; break;
            case "price.asc":
                query += " ORDER BY p.price ASC"; break;
            case "price.desc":
                query += " ORDER BY p.price DESC"; break;
            case "featured":
                query += " ORDER BY p.visits DESC"; break;
        }

        return query;
    }

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

    public Product getProduct(int productId) {

        // @todo select form database

        // test
        List<Product> products = getProducts(true);
        for(Product p:products) {
            if (p.getId() == productId) return p;
        }

        return null;
    }

    public User getUser(String username) {

        // @todo select form database

        User yo = new User();
        yo.setUsername("tiberiu");

        return yo;
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

    public List<ProductPicture> getProductPictures() {

        ArrayList<ProductPicture> imgs = new ArrayList<>();

        ProductPicture p1 = new ProductPicture();
        p1.setUrl("https://www.journaldugeek.com/wp-content/blogs.dir/1/files/2017/02/blackberry-keyone-02.png");

        imgs.add(p1);


        return imgs;

    }
}
