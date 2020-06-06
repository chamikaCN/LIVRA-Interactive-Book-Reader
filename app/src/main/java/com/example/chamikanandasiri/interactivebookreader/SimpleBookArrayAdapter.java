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

class SimpleBookArrayAdapter extends RecyclerView.Adapter<com.example.chamikanandasiri.interactivebookreader.SimpleBookArrayAdapter.ViewHolder> {
    private Context context;
    private ArrayList<SimpleBookObject> books;
    private String TAG = "Test";
    private int selectedPos;

    public SimpleBookArrayAdapter(Context context, ArrayList<SimpleBookObject> objects) {
        this.context = context;
        this.books = objects;
        selectedPos = RecyclerView.NO_POSITION;
    }

    @NonNull
    @Override
    public com.example.chamikanandasiri.interactivebookreader.SimpleBookArrayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_book, parent, false);
        return new com.example.chamikanandasiri.interactivebookreader.SimpleBookArrayAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.chamikanandasiri.interactivebookreader.SimpleBookArrayAdapter.ViewHolder holder, int position) {

        holder.itemView.setSelected(selectedPos == position);
        holder.otherView.setTextColor(holder.itemView.isSelected() ? context.getResources().getColor(R.color.commonAccentText) : context.getResources().getColor(R.color.commonPrimaryText));
        holder.textView.setTextColor(holder.itemView.isSelected() ? context.getResources().getColor(R.color.commonAccentText) : context.getResources().getColor(R.color.commonPrimaryText));
        holder.itemView.setBackgroundColor(holder.itemView.isSelected() ? context.getResources().getColor(R.color.commonAccent) : context.getResources().getColor(R.color.commonPrimary));

        Picasso.with(context).load(books.get(position).getCover())
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(holder.imageView);

        holder.textView.setText(books.get(position).getTitle());
        String s = books.get(position).getAuthor() + "\n" + books.get(position).getIsbn();
        holder.otherView.setText(s);

        holder.itemView.setOnClickListener(v -> displayAr(books.get(position).getBookId()));
        holder.itemView.setOnLongClickListener(view -> {
            if (selectedPos != holder.getLayoutPosition()) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                LibraryActivity.setSelectedBook(books.get(position).getBookId());
                notifyItemChanged(selectedPos);
                return true;
            } else {
                notifyItemChanged(selectedPos);
                selectedPos = RecyclerView.NO_POSITION;
                LibraryActivity.setSelectedBook(null);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView, otherView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.BookImageView);
            textView = itemView.findViewById(R.id.BookTitleView);
            otherView = itemView.findViewById(R.id.BookOtherView);
        }
    }

    private void displayAr(String bookID) {
        Intent intent = new Intent(context, ArViewActivity.class);
        intent.putExtra("bookID", bookID);
        context.startActivity(intent);
    }
}













