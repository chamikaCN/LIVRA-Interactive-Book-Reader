package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class DownloadContentArrayAdapter extends ArrayAdapter<DownloadContentObject> {
    private Context context;
    private  int resource;

    public DownloadContentArrayAdapter (Context context, int resource, ArrayList<DownloadContentObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String imageURL = getItem(position).getImageURLs()[0];
        String name = getItem(position).getContName();
        String size = getItem(position).getContSize();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource,parent,false);

        ImageView imageView = convertView.findViewById(R.id.ContentListImageView);
        TextView tvName = convertView.findViewById(R.id.ContentListNameView);
        TextView tvSize = convertView.findViewById(R.id.ContentListSizeView);
        CheckBox check = convertView.findViewById(R.id.ContentCheckBox);

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.ezgif_crop)
                .into(imageView);
        tvName.setText(name);
        tvSize.setText(size);
        check.setChecked(false);

        return convertView;
    }

}
