package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    private ArrayList<SimpleContentObject> downloadedContent;
    private Context context;

    public RecyclerViewAdapter(Context context, ArrayList<SimpleContentObject> objects) {
        this.downloadedContent = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_arcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Picasso.with(context).load(downloadedContent.get(position).getImageURL())
                .placeholder(R.drawable.ezgif_crop)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(holder.imageView);

        holder.textView.setText(downloadedContent.get(position).getContName());

        holder.imageView.setOnClickListener(view -> Common.model = downloadedContent.get(position).getFile());
    }

    @Override
    public int getItemCount() {
        return downloadedContent.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
