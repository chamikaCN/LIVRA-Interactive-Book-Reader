package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder> {

    private Context context;
    private  String[] textViewValues;
        public GridViewAdapter(Context context, String[] textViewValues) {
        this.context = context;
        this.textViewValues = textViewValues;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_textview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(textViewValues[position]);
        holder.textView.setOnClickListener
                (view-> TextDetectionActivity.setSelectedText(textViewValues[position]));
    }

    @Override
    public int getItemCount() {
        return textViewValues.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.grid_item_label);

        }
    }

//    private Context context;
//    private final String[] textViewValues;
//
//    public GridViewAdapter(Context context, String[] textViewValues) {
//        this.context = context;
//        this.textViewValues = textViewValues;
//    }
//
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        LayoutInflater inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        View gridView;
//
//        if (convertView == null) {
//
//            gridView = new View(context);
//
//            // get layout from mobile.xml
//            gridView = inflater.inflate(R.layout.custom_textview, null);
//
//            // set value into textview
//            TextView textView = (TextView) gridView
//                    .findViewById(R.id.grid_item_label);
//            textView.setText(textViewValues[position]);
//        } else {
//            gridView = (View) convertView;
//        }
//
//        return gridView;
//    }
//
//    @Override
//    public int getCount() {
//        return textViewValues.length;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }

}