package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.content.Intent;
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

public class RecentBookAdapter extends RecyclerView.Adapter<RecentBookAdapter.ViewHolder> {


    private ArrayList<SimpleBookObject> books;
    private Context context;

    public RecentBookAdapter(Context context, ArrayList<SimpleBookObject> books) {
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_recentbook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Picasso.with(context).load(books.get(position).getCover())
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(holder.imageView);

        holder.textView.setText(books.get(position).getTitle());

        holder.imageView.setOnClickListener(view -> displayAr(books.get(position).getBookId()));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.RecentBookImageView);
            textView = itemView.findViewById(R.id.RecentBookTitleView);
        }
    }

    private void displayAr(String bookID) {
        Intent intent = new Intent(context, ArViewActivity.class);
        intent.putExtra("bookID", bookID);
        context.startActivity(intent);
    }
}
