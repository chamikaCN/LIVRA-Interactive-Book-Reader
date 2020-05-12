package com.example.chamikanandasiri.interactivebookreader;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadContentObject implements Serializable {

    private String id;
    private String[] imageURLs;
    private String name;
    private String description;
    private String size;
    private String bookID;
    private Timestamp timestamp;
    private String fileURL;
    private int timeStampUniqueCount;

    private String TAG ="Test";

    public DownloadContentObject(String id, String[] imageURLs, String name, String bookID, String description, String size, String fileURL, int timeCount) {
        this.id = id;
        this.imageURLs = imageURLs;
        this.name = name;
        this.description = description;
        this.size = size;
        this.bookID =bookID;
        this.fileURL = fileURL;
        this.timeStampUniqueCount = timeCount;
        this.timestamp = addTimeStamp();
    }

    private Timestamp addTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String stamp = formatter.format(date) +  "." + timeStampUniqueCount;
        return Timestamp.valueOf(stamp);
    }

    public String getContId() {
        return id;
    }

    public String[] getImageURLs() {
        return imageURLs;
    }

    public String getContName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContSize() {
        return size;
    }

    public String getBookID() {
        return bookID;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public String getFileURL() {
        return fileURL;
    }
}
