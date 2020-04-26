package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class BookArrayAdapter extends ArrayAdapter<BookObject> {
    private Context context;
    private int resource;

    public BookArrayAdapter(Context context, int resource, ArrayList<BookObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String imageURL = getItem(position).getCovers()[0];
        String name = getItem(position).getTitle();
        StringBuilder other = new StringBuilder();
        other.append(getItem(position).getAuthors()[0]).append(" ").append(getItem(position).getIsbns()[0]);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageView = convertView.findViewById(R.id.BookImageView);
        TextView tvName = convertView.findViewById(R.id.BookTitleView);
        TextView tvOther = convertView.findViewById(R.id.BookOtherView);

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.ezgif_crop)
                .into(imageView);
        tvName.setText(name);
        tvOther.setText(other);

        return convertView;
    }



}
