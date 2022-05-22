package com.tibelian.gangaphone.database;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CurrentFilter {

    public static ArrayList<String> status = new ArrayList<>();
    public static String keyword = "";
    public static String location = "";
    public static double minPrice = -1;
    public static double maxPrice = -1;
    public static String orderBy = "";

    public static void clear() {
        CurrentFilter.status = new ArrayList<>();
        CurrentFilter.keyword = "";
        CurrentFilter.location = "";
        CurrentFilter.minPrice = -1;
        CurrentFilter.maxPrice = -1;
        CurrentFilter.orderBy = "";
    }

    public static String toJson() {
        JSONObject filter = new JSONObject();
        try {
            if (keyword.length() > 0) filter.put("keyword", keyword);
            if (status.size() > 0) filter.put("status", new JSONArray(status));
            if (location.length() > 0) filter.put("location", location);
            if (minPrice != -1) filter.put("minPrice", minPrice);
            if (maxPrice != -1) filter.put("maxPrice", maxPrice);
            if (orderBy.length() > 0) filter.put("orderBy", orderBy);
        }
        catch (Exception e) {}
        Log.e("CurrentFilter@toJson", filter.toString());
        return filter.toString();
    }

    public static String getSearch() {
        String sKeyword = "", sStatus = "", sLocation = "", sMinPrice = "", sMaxPrice = "", sOrder = "";
        if (keyword.length() > 0) sKeyword = "\nKeyword: " + keyword;
        if (status.size() > 0) sStatus = "\nStatus: " + status.toString();
        if (location.length() > 0) sLocation = "\nLocation: " + location;
        if (minPrice != -1 && minPrice < maxPrice) sMinPrice = "\nMin price: " + minPrice;
        if (maxPrice != -1 && maxPrice > minPrice ) sMaxPrice += "\nMax price: " + minPrice;
        if (orderBy.length() > 0) {
            sOrder = "\nOrdered by: ";
            switch (orderBy) {
                case "date.asc": sOrder += "Oldest posts"; break;
                case "date.desc": sOrder += "Newest posts"; break;
                case "price.asc": sOrder += "Lowest price"; break;
                case "price.desc": sOrder += "Highest price"; break;
                case "featured": sOrder += "Featured"; break;
            }
        }
        return sKeyword + sStatus + sLocation + sMinPrice + sMaxPrice + sOrder;
    }

}
