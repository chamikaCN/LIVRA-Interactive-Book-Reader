package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class CommentHandler {
    private DataBaseHelper dbHelper;
    private Context context;

    public CommentHandler(DataBaseHelper dbHelper, Context context){
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
            results.add(new String[]{Ac,Bc});

        }return results;
    }

    public ArrayList<String> getAllDistinctTitles(){
        Cursor res = dbHelper.getAllDistinctTitles();
        ArrayList<String> results = new ArrayList<>();

        while (res.moveToNext()){
            String Ac = res.getString(0);
            results.add(Ac);
        }
        return results;
    }

    public boolean addComment (CommentObject commentObject) {
        return dbHelper.insertRowComment(commentObject.getTimeStamp(),commentObject.getTitle(),commentObject.getPhrase(),commentObject.getComment());
    }


}
