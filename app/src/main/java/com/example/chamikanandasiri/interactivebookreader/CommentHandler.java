package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class CommentHandler {
    private DataBaseHelper dbHelper;
    private Context context;

    private String TAG = "Test";

    public CommentHandler(DataBaseHelper dbHelper, Context context) {
        this.dbHelper = dbHelper;
        this.context = context;
    }

    public ArrayList<String> getComments() {
        Cursor res = dbHelper.getAllComments();
        ArrayList<String> Comments = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            Comments.add(Ac);
        }
        return Comments;
    }

    public ArrayList<String[]> getPhraseCommentBookIDbyTitle(String title) {
        Cursor res = dbHelper.getPhraseCommentBookIDByTitle(title);
        ArrayList<String[]> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            String Bc = res.getString(1);
            String Cc = res.getString(2);

//            String[] cat = new String[]{Ac,Bc};
//            Log.d(TAG, "getPhraseCommentByTitle: "+ Arrays.toString(cat));
            results.add(new String[]{Ac, Bc, Cc});

        }
        return results;
    }

    public ArrayList<String[]> getPhraseCommentTitlesbyBookID(String id) {
        Cursor res = dbHelper.getPhraseCommentTitlesByBookID(id);
        ArrayList<String[]> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            String Bc = res.getString(1);
            String Cc = res.getString(2);

            results.add(new String[]{Ac, Bc, Cc});
        }
        return results;
    }

    public ArrayList<String> getAllDistinctTitles() {
        Cursor res = dbHelper.getAllDistinctTitles();
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public ArrayList<String> getSimilarTitles(String title) {
        Cursor res = dbHelper.getSimilarTitles(title);
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public boolean addComment(CommentObject commentObject) {
        Log.d(TAG, "addComment: "+ commentObject.getBookID());
        return dbHelper.insertRowComment(commentObject.getTimeStamp(), commentObject.getTitle(), commentObject.getPhrase(), commentObject.getComment(), commentObject.getBookID());
    }


}
