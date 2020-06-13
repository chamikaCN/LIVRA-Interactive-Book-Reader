package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class CommentArrayAdapter extends ArrayAdapter<CommentObject> {
    private Context context;
    private int resource;
    private  BookHandler bookHandler;

    private String TAG = "Test";

    public CommentArrayAdapter(Context context, int resource, ArrayList<CommentObject> objects, BookHandler bookHandler) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.bookHandler = bookHandler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String title = getItem(position).getTitle();
        String phrase = getItem(position).getPhrase();
        String comment = getItem(position).getComment();
        String book = getItem(position).getBookID();
        if(!book.equals("NONE")){
            book = bookHandler.getBooksByID(getItem(position).getBookID()).get(0);
        }

        CommentObject wo = new CommentObject(title, phrase, comment);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView tvTitle = convertView.findViewById(R.id.CommentCardTitle);
        TextView tvPhrase = convertView.findViewById(R.id.CommentCardPhrase);
        TextView tvComment = convertView.findViewById(R.id.CommentCardComment);
        TextView tvBook = convertView.findViewById(R.id.CommentCardBook);

        String s = "("+ book +")";

        tvTitle.setText(title);
        tvPhrase.setText(phrase);
        tvComment.setText(comment);
        tvBook.setText(s);

        return convertView;
    }

}
