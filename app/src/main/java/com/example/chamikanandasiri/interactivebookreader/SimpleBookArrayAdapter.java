package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class SimpleBookArrayAdapter extends ArrayAdapter<SimpleBookObject> {
    private Context context;
    private int resource;
    private String TAG ="Test";
    public SimpleBookArrayAdapter(Context context, int resource, ArrayList<SimpleBookObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String imageURL = getItem(position).getCover();
        String name = getItem(position).getTitle();
        StringBuilder other = new StringBuilder();
        other.append(getItem(position).getAuthor()).append("\n").append(getItem(position).getIsbn());

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageView = convertView.findViewById(R.id.BookImageView);
        TextView tvName = convertView.findViewById(R.id.BookTitleView);
        TextView tvOther = convertView.findViewById(R.id.BookOtherView);

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(imageView);
        tvName.setText(name);
        tvOther.setText(other);

        imageView.setOnClickListener(v -> displayAr(getItem(position).getBookId()));

        return convertView;
    }

    private void displayAr(String bookID) {
        Intent intent = new Intent(context, ArViewActivity.class);
        intent.putExtra("bookID",bookID);
        context.startActivity(intent);
    }


}
