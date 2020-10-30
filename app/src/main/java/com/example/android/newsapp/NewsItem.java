package com.example.android.newsapp;

import org.json.JSONException;
import org.json.JSONObject;

public class NewsItem {
    final private String title;
    final private String sectionName;
    final private String datePublished;
    final private String authorName;
    final private String webUrlString;
    final private String bodyTextSummary;

    /**
     * Standard constructor
     * @param title Title of the {@link NewsItem}
     * @param sectionName Name of the section, the {@link NewsItem} is related to
     * @param datePublished A string representing the date {@link NewsItem} was published
     * @param authorName Name of the author of the {@link NewsItem}
     * @param webURL String representation of the URL leading to the {@link NewsItem}
     * @param bodyTextSummary The textual summary of the {@link NewsItem}
     */
    public NewsItem(String title, String sectionName, String datePublished, String authorName, String webURL, String bodyTextSummary) {
        this.title = title;
        this.authorName = authorName;
        this.sectionName = sectionName;
        this.webUrlString = webURL;
        this.datePublished = datePublished;
        this.bodyTextSummary = bodyTextSummary;
    }

    /**
     * This constructor accepts a {@link JSONObject} that is parsed into a {@link NewsItem}.
     * This constructor assumes that the given JSON object is valid
     * @param jsonObject {@link JSONObject} representation of the {@link NewsItem}
     * @throws JSONException JSONException
     */
    public NewsItem(JSONObject jsonObject) throws JSONException {

        /* In the Guardian API, author names are sometimes added at the end of the title,
         * separated by "|". The following section of code check the title to see if the author's
         * name is provided, splitting the string and assigning it to the correct field, or
         * assigns the whole string to the title field if no author name is provided.
         */
        String titleAuthorString = jsonObject.getString("webTitle");
        if(titleAuthorString.contains("|")){
            this.title = titleAuthorString.substring(0, titleAuthorString.indexOf("|") - 1);
            this.authorName = titleAuthorString.substring(titleAuthorString.indexOf("|") + 1, titleAuthorString.length() - 1);
        } else {
            this.title = titleAuthorString;
            this.authorName = null;
        }

        this.sectionName = jsonObject.getString("sectionName");
        this.webUrlString = jsonObject.getString("webUrl");
        this.datePublished = jsonObject.getString("webPublicationDate");
        this.bodyTextSummary = jsonObject.getJSONObject("blocks").getJSONArray("body")
                                .getJSONObject(1).getString("bodyTextSummary");
    }

    //TODO change webPublicationDate to long and implement parseDate method
//    private long parseDate(String webPublicationDate) {
//
//    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getWebUrlString() {
        return webUrlString;
    }

    public String getDatePublished() {
        return datePublished;
    }
}