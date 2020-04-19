package com.example.chamikanandasiri.interactivebookreader;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentObject {

    private Timestamp timestamp;
    private String title;
    private String phrase;
    private String comment;

    public CommentObject(String title, String phrase, String comment) {
        this.title = title;
        this.phrase = phrase;
        this.comment = comment;
        this.timestamp = addTimeStamp();
    }

    private Timestamp addTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String stamp = formatter.format(date) + ".0";
        return Timestamp.valueOf(stamp);
    }

    public String getTitle() {
        return title;
    }

    public String getPhrase() {
        return phrase;
    }

    public String getComment() {
        return comment;
    }

    public String getTimeStamp(){
        return timestamp.toString();
    }


}
