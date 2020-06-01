package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.util.Log;
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
    private int resource;
    private ArrayList<DownloadContentObject> objects;
    private ArrayList<DownloadContentObject> selectedObjects;
    private ArrayList<String> contentIDsInDatabase;

    private String TAG = "Test";

    public DownloadContentArrayAdapter(Context context, int resource, ArrayList<DownloadContentObject> objects, ArrayList<String> contentIDsInDatabase) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        this.contentIDsInDatabase = contentIDsInDatabase;
        selectedObjects = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String id = getItem(position).getContId();
        String imageURL = getItem(position).getImageURLs()[0];
        String name = getItem(position).getContName();
        String size = getItem(position).getContSize();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageView = convertView.findViewById(R.id.ContentListImageView);
        TextView tvName = convertView.findViewById(R.id.ContentListNameView);
        TextView tvSize = convertView.findViewById(R.id.ContentListSizeView);
        CheckBox check = convertView.findViewById(R.id.ContentCheckBox);

        if (contentIDsInDatabase.contains(id)) {
            check.setChecked(true);
            check.setEnabled(false);
        }

        check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if ((isChecked)) {
                selectedObjects.add(objects.get(position));
                Log.d("Test", "added one " + selectedObjects.size());

            } else {
                selectedObjects.remove(objects.get(position));
                Log.d("Test", " removed one" + selectedObjects.size());
            }
        });

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.bookcover_loading_anim)
                .into(imageView);
        tvName.setText(name);
        tvSize.setText(size);
        check.setChecked(false);

        return convertView;
    }

    public ArrayList<DownloadContentObject> getSelectedObjects() {
        return selectedObjects;
    }


}
