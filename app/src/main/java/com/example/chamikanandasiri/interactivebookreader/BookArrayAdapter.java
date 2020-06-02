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

class BookArrayAdapter extends RecyclerView.Adapter<BookArrayAdapter.ViewHolder> {
    private Context context;
    private int selectedPos;
    private String TAG = "Test";
    private ArrayList<BookObject> books;

    public BookArrayAdapter(Context context, ArrayList<BookObject> objects) {
        this.context = context;
        this.books = objects;
        selectedPos = RecyclerView.NO_POSITION;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_searchbook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.itemView.setSelected(selectedPos == position);
        holder.textView.setTextColor(holder.itemView.isSelected()?context.getResources().getColor(R.color.commonAccentText) : context.getResources().getColor(R.color.commonPrimaryText));
        holder.itemView.setBackgroundColor(holder.itemView.isSelected() ? context.getResources().getColor(R.color.commonAccent) : context.getResources().getColor(R.color.commonPrimary));

        Picasso.with(context).load(books.get(position).getCovers()[0])
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(holder.imageView);

        holder.textView.setText(books.get(position).getTitle());

        holder.itemView.setOnClickListener(view -> {
            notifyItemChanged(selectedPos);
            selectedPos = holder.getLayoutPosition();
            notifyItemChanged(selectedPos);
            MenuActivity.setBook(books.get(position));
        });
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
            imageView = itemView.findViewById(R.id.SearchBookArrayImageView);
            textView = itemView.findViewById(R.id.SearchBookArrayTitleView);
        }
    }

}









