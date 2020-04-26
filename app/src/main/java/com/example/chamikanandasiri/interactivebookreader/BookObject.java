package com.example.chamikanandasiri.interactivebookreader;

import java.io.Serializable;
import java.util.ArrayList;


//Serializable for testing
public class BookObject implements Serializable {

    private String id;
    private String title;
    private String[] authors;
    private String[] isbns;
    private String[] covers;
    private boolean active;
    private ArrayList<DownloadContentObject> content;
    private String publisherId;
    private String publisherName;

    public BookObject(String id, String title, String[] authors, String[] isbns, String[] covers, boolean active, ArrayList<DownloadContentObject> content, String publisherId, String publisherName) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.isbns = isbns;
        this.covers = covers;
        this.active = active;
        this.content = content;
        this.publisherId = publisherId;
        this.publisherName = publisherName;
    }

    public String getBookId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String[] getIsbns() {
        return isbns;
    }

    public String[] getCovers() {
        return covers;
    }

    public boolean isActive() {
        return active;
    }

    public ArrayList<DownloadContentObject> getDownloadContent() {
        return content;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }
}
