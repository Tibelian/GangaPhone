package com.tibelian.gangaphone.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Manage the product's list filter
 */
public class CurrentFilter {

    // all possible variations
    public static ArrayList<String> status = new ArrayList<>();
    public static String keyword = "";
    public static String location = "";
    public static double minPrice = -1;
    public static double maxPrice = -1;
    public static String orderBy = "";

    /**
     * reset the filter to default
     */
    public static void clear() {
        CurrentFilter.status = new ArrayList<>();
        CurrentFilter.keyword = "";
        CurrentFilter.location = "";
        CurrentFilter.minPrice = -1;
        CurrentFilter.maxPrice = -1;
        CurrentFilter.orderBy = "";
    }

    /**
     * Parse this object to json
     * @return String
     */
    public static String toJson() {
        JSONObject filter = new JSONObject();
        try {
            if (keyword.length() > 0)   filter.put("keyword", keyword);
            if (status.size() > 0)      filter.put("status", new JSONArray(status));
            if (location.length() > 0)  filter.put("location", location);
            if (minPrice != -1)         filter.put("minPrice", minPrice);
            if (maxPrice != -1)         filter.put("maxPrice", maxPrice);
            if (orderBy.length() > 0)   filter.put("orderBy", orderBy);
        }
        catch (Exception e) {}
        return filter.toString();
    }

}
