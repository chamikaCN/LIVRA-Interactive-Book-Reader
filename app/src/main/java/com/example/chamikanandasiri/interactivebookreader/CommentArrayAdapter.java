package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class CommentArrayAdapter extends ArrayAdapter<CommentObject> {
    private Context context;
    private int resource;

    private String TAG = "Test";

    public CommentArrayAdapter(Context context, int resource, ArrayList<CommentObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String title = getItem(position).getTitle();
        String phrase = getItem(position).getPhrase();
        String comment = getItem(position).getComment();

        CommentObject wo = new CommentObject(title, phrase, comment);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView tvTitle = convertView.findViewById(R.id.CommentCardTitle);
        TextView tvPhrase = convertView.findViewById(R.id.CommentCardPhrase);
        TextView tvComment = convertView.findViewById(R.id.CommentCardComment);

        tvTitle.setText(title);
        tvPhrase.setText(phrase);
        tvComment.setText(comment);

        return convertView;
    }

}
