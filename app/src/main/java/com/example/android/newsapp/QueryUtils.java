package com.example.android.newsapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QueryUtils {

    private static final String TAG = "QueryUtils";

    //This URL is used to connect to The Guardians API
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search";
    //Registered API Key given to the developer of this app
    private static final String GUARDIAN_API_KEY = "28a18d57-54b5-4f0e-af60-0c3abc118e2c";

    //region Query Parameters

    //The query used. Search queries can separated by AND/OR/NOT, with %20 instead of spaces
    private static String query = "";

    //The from and to date from which the news items are taken from. Format: yyyy-MM-dd (DEFAULT = 2014-01-01, current day)
    private static String fromDate;
    private static String toDate;

    //The order with which the queries are organized by. Values: newest, oldest or relevance (DEFAULT = relevance)
    private static String queryOrderBy;

    //The amount of items sent in 1 page. Range: 1-50 (DEFAULT = 10)
    private static int queryPageSize;

    //endregion

    /**
     * The culmination of most of the methods in the {@link QueryUtils} class.
     * Uses {@link #getRequestUrl()} to get current URL used to request The Guardian API
     * Then requests a JSON response using {@link #makeHttpRequest(URL)}
     * And finally parses the JSON response into a list of {@link NewsItem}s
     *
     * @return A list of {@link NewsItem}s
     */
    public static List<NewsItem> getNewsList() {
        URL url = getRequestUrl();
        String jsonResponse;
        List<NewsItem> newsItems = new ArrayList<>();

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            //Output a log error message and return early with an empty list
            Log.e(TAG, "Error while getting News List (HTTP Error): ", e);
            e.printStackTrace();
            return newsItems;
        }

        try {
            newsItems = parseJsonResponse(jsonResponse);
        } catch (JSONException e) {
            Log.e(TAG, "Error while getting News List (JSON Error): ", e);
            e.printStackTrace();
        }

        return newsItems;

    }

    /**
     * Utility method used to build the URL based on the user preferences
     *
     * @return URL object used to initiate an HTTP request from The Guardian API
     */
    public static URL getRequestUrl() {
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", query);
        uriBuilder.appendQueryParameter("from-date", fromDate);
        uriBuilder.appendQueryParameter("to-date", toDate);
        uriBuilder.appendQueryParameter("page-size", Integer.toString(queryPageSize));
        uriBuilder.appendQueryParameter("order-by", queryOrderBy);
        uriBuilder.appendQueryParameter("show-fields", "trailText");
        uriBuilder.appendQueryParameter("api-key", GUARDIAN_API_KEY);

        URL url = null;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null)
            Log.i(TAG, "Final Url = " + url.toString());
        else
            Log.e(TAG, "Url was equal to null");

        return url;
    }

    /**
     * IMPORTANT: This method came from the Soonami app (Lesson 2, Course 5 of the "Android Basics
     * by Google" Nanodegree)
     * Utility method used to initiate an HTTP request from The Guardian API
     *
     * @param url URL object built but the {@link #getRequestUrl()} method
     * @return JSON Response from The Guardian API
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //return early if provided url is null
        if (url == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else
                Log.e(TAG, "Error! Response code: " + urlConnection.getResponseCode());

        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the JSON Response", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (inputStream != null)
                inputStream.close();
        }

        return jsonResponse;
    }

    /**
     * IMPORTANT: This method came from the Soonami app (Lesson 2, Course 5 of the "Android Basics
     * by Google" Nanodegree)
     * Private utility method used to get a string from an {@link InputStream}
     *
     * @param inputStream Input stream
     * @return a string containing the content of the input stream
     * @throws IOException Input output exception
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                strBuilder.append(line);
                line = reader.readLine();
            }
        }
        return strBuilder.toString();
    }

    /**
     * Accepts a JSON Response in the form of a string, then parses it into a list of
     * {@link NewsItem}s
     *
     * @param jsonResponse The JSON Response provided by {@link #makeHttpRequest(URL)}
     * @return A list of {@link NewsItem}s if the JSON Response is valid,
     * or null otherwise
     */
    private static List<NewsItem> parseJsonResponse(String jsonResponse) throws JSONException {

        //Checks if the response is null or empty, and returns early if it's true
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            Log.e(TAG, "JSON Response is null or empty");
            return null;
        }

        JSONObject response = new JSONObject(jsonResponse).getJSONObject("response");

        //Checks the status of the response and returns early if it's not "ok"
        String status = response.getString("status");
        if(!status.equals("ok")){
            Log.e(TAG, "Status not OK: " + status +
                             "\n Message: " + response.getString("message"));
            return null;
        }

        List<NewsItem> newsItems = new ArrayList<>(queryPageSize);
        JSONArray results = response.getJSONArray("results");
        for (int i = 0; i < results.length(); i++)
            newsItems.add(new NewsItem(results.getJSONObject(i)));

        return newsItems;
    }

    /**
     * IMPORTANT:- Source of this method: https://stackoverflow.com/questions/226910/how-to-sanity-check-a-date-in-java
     * Simple method to check if the given date string is in the format we need
     *
     * @param date String representation of a date
     * @return true if the date follows our standard, false otherwise
     */
    private static boolean isValidDate(String date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    //region Setters for Preference Changes
    public static void setQuery(String query) {
        QueryUtils.query = query;
    }

    /**
     * Checks to see if the given date is valid using {@link #isValidDate(String)}.
     * if it's valid, sets the {@link #fromDate} to the passed value. Otherwise sets {@link #toDate} to 2014-01-01
     *
     * @param fromDate String representation of the "from" date as in "from *date* to *date*"
     */
    public static void setFromDate(String fromDate) {
        if (isValidDate(fromDate))
            QueryUtils.fromDate = fromDate;
        else
            QueryUtils.fromDate = "2014-01-01";
    }

    /**
     * Checks to see if the given date is valid using {@link #isValidDate(String)}.
     * if it's valid, sets the {@link #toDate} to the passed value. Otherwise sets {@link #toDate} to the current day
     *
     * @param toDate String representation of the "to" date as in "from *date* to *date*"
     */
    public static void setToDate(String toDate) {
        if (isValidDate(toDate))
            QueryUtils.toDate = toDate;
        else
            QueryUtils.toDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static void setQueryOrderBy(String queryOrderBy) {
        QueryUtils.queryOrderBy = queryOrderBy;
    }

    public static void setQueryPageSize(int queryPageSize) {
        QueryUtils.queryPageSize = queryPageSize;
    }
    //endregion
}
