package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class BookHandler {

    private DataBaseHelper dbHelper;
    private Context context;

    private String TAG = "Test";

    public BookHandler(DataBaseHelper dbHelper, Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
    }


    public ArrayList<String> getBooksByID(String id) {
        Cursor res = dbHelper.getAllBookDetailsByID(id);
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            results.add(res.getString(0));
            results.add(res.getString(1));
            results.add(res.getString(2));
            results.add(res.getString(3));
        }
        return results;
    }

    public ArrayList<String> getAllBookIDs() {
        Cursor res = dbHelper.getAllBooks();
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public ArrayList<String> getRecentBookIDs() {
        Cursor res = dbHelper.getRecentBookIDs();
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public ArrayList<String> getSimilarBookIDs(String similar) {
        Cursor res = dbHelper.getSimilarBookIDs(similar);
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public String getBookIDByISBN(String ISBN) {
        Cursor res = dbHelper.getBookIDByISBN(ISBN);
        String Ac = "empty";
        while (res.moveToNext()) {
            Ac = res.getString(0);
        }
        return Ac;
    }

    public boolean addBook(BookObject bookObject) {
        return dbHelper.insertRowBook(bookObject.getBookId(), bookObject.getTimeStamp(), bookObject.getTitle(), bookObject.getAuthors()[0],
                bookObject.getIsbns()[0], bookObject.getCovers()[0], bookObject.getPublisherId(), bookObject.getPublisherName());
    }

    public boolean deleteBook(String id){
        return dbHelper.deleteRowBook(id);
    }
}