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

class DeleteContentArrayAdapter extends ArrayAdapter<SimpleContentObject> {
    private Context context;
    private int resource;
    private ArrayList<SimpleContentObject> objects;
    private ArrayList<SimpleContentObject> selectedObjects;

    private String TAG = "Test";

    public DeleteContentArrayAdapter(Context context, int resource, ArrayList<SimpleContentObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        selectedObjects = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String id = getItem(position).getContId();
        String imageURL = getItem(position).getImageURL();
        String name = getItem(position).getContName();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageView = convertView.findViewById(R.id.ContentListImageView);
        TextView sizeView = convertView.findViewById(R.id.ContentListSizeView);
        ImageView animatedView = convertView.findViewById(R.id.ContentListAnimatedView);
        TextView tvName = convertView.findViewById(R.id.ContentListNameView);
        CheckBox check = convertView.findViewById(R.id.ContentCheckBox);

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
        sizeView.setVisibility(View.GONE);
        animatedView.setVisibility(objects.get(position).isAnimated()? (View.VISIBLE):(View.GONE));
        check.setChecked(false);

        return convertView;
    }

    public ArrayList<SimpleContentObject> getSelectedObjects() {
        return selectedObjects;
    }

    public boolean isAllSelected(){return objects.size() == selectedObjects.size();}

    public boolean isAnySelected(){return selectedObjects.size() > 0; }


}
