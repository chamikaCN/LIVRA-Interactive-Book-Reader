package com.example.chamikanandasiri.interactivebookreader;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WordObject {

    private Timestamp timestamp;
    private String word;
    private String definition;
    private String partOfSpeech;

    public WordObject(String word, String definition, String pos) {
        this.word = word;
        this.definition = definition;
        this.partOfSpeech = pos;
        this.timestamp = addTimeStamp();
    }

    private Timestamp addTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String stamp = formatter.format(date) + ".0";
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
