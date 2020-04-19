package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class WordHandler {
    private DataBaseHelper dbHelper;
    private Context context;

    public WordHandler(DataBaseHelper dbHelper, Context context){
        this.dbHelper = dbHelper;
        this.context = context;
    }

    public ArrayList<String> getWords() {
        Cursor res = dbHelper.getAllDistinctWords();
        ArrayList<String> words = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            words.add(Ac);
        }
        return words;
    }

    public ArrayList<String[]> getDefinitionPosByWord(String word) {
        Cursor res = dbHelper.getDefinitionPoSByWord(word);
        ArrayList<String[]> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            String Bc = res.getString(1);
            results.add(new String[]{Ac,Bc});

        }return results;
    }

    public ArrayList<String> getWordsByPos(String pos){
        Cursor res = dbHelper.getWordsByPoS(pos);
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);

        }return results;
    }

    public boolean addWord (WordObject wordObject) {
        return dbHelper.insertRowWord(wordObject.getTimeStamp(), wordObject.getWord(),wordObject.getDefinition(),wordObject.getPartOfSpeech());
    }


}
