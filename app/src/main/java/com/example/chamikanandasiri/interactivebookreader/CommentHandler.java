package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;

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

    public ArrayList<String[]> getPhraseCommentbyTitle(String title) {
        Cursor res = dbHelper.getPhraseCommentByTitle(title);
        ArrayList<String[]> results = new ArrayList<>();

        while (res.moveToNext()) {
            String Ac = res.getString(0);
            String Bc = res.getString(1);

//            String[] cat = new String[]{Ac,Bc};
//            Log.d(TAG, "getPhraseCommentByTitle: "+ Arrays.toString(cat));
            results.add(new String[]{Ac, Bc});

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
        return dbHelper.insertRowComment(commentObject.getTimeStamp(), commentObject.getTitle(), commentObject.getPhrase(), commentObject.getComment());
    }


}
