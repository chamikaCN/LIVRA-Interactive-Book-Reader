package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class ContentHandler {

    private DataBaseHelper dbHelper;
    private Context context;

    private String TAG = "Test";

    public ContentHandler(DataBaseHelper dbHelper, Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
    }

    public ArrayList<String[]> getContentsByBookID(String bookID) {
        Cursor res = dbHelper.getContentDetailsByBooKID(bookID);
        ArrayList<String[]> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            String Bc = res.getString(1);
            String Cc = res.getString(2);
            String Dc = res.getString(3);
            results.add(new String[]{Ac, Bc, Cc, Dc});

        }
        return results;
    }

    public ArrayList<String> getContentIDsByBookIDs(String id) {
        Cursor res = dbHelper.getContentIDsByBooKID(id);
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            results.add(res.getString(0));
        }
        return results;
    }

    public boolean addContent(DownloadContentObject contentObject) {
        return dbHelper.insertRowContent(contentObject.getContId(), contentObject.getTimestamp(), contentObject.getBookID(),
                contentObject.getContName(), contentObject.getContSize(), contentObject.getImageURLs()[0], contentObject.getFileURL());
    }
}