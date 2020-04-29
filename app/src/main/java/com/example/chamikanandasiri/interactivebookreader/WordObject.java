package com.example.chamikanandasiri.interactivebookreader;

import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class WordObject {

    private Timestamp timestamp;
    private String word;
    private String definition;
    private String partOfSpeech;
    private int timeStampUniqueCount;

    private String TAG ="Test";

    public WordObject(String word, String definition, String pos, int timeCount) {
        this.word = word;
        this.definition = definition;
        this.partOfSpeech = pos;
        this.timeStampUniqueCount = timeCount;
        this.timestamp = addTimeStamp();
    }

    private Timestamp addTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String stamp = formatter.format(date) + "." + timeStampUniqueCount;
        return Timestamp.valueOf(stamp);
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getTimeStamp() {
        return timestamp.toString();
    }



}
