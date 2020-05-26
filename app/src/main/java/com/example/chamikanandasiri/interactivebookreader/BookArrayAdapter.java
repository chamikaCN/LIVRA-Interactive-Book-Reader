package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class BookArrayAdapter extends ArrayAdapter<BookObject> {
    private Context context;
    private int resource;
    private String TAG = "Test";

    public BookArrayAdapter(Context context, int resource, ArrayList<BookObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String imageURL = getItem(position).getCovers()[0];
        String name = getItem(position).getTitle();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageView = convertView.findViewById(R.id.SearchBookArrayImageView);
        TextView tvName = convertView.findViewById(R.id.SearchBookArrayTitleView);

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(imageView);
        tvName.setText(name);

        imageView.setOnClickListener(v -> {
            MenuActivity.setBook(getItem(position));
        });

        return convertView;
    }
}
