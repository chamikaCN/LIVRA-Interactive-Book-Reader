package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ArCardViewAdapter extends RecyclerView.Adapter<ArCardViewAdapter.ViewHolder> {

    private int selectedPos;
    private ArrayList<SimpleContentObject> downloadedContent;
    private Context context;
    private  String TAG = "Test";

    public ArCardViewAdapter(Context context, ArrayList<SimpleContentObject> objects) {
        this.downloadedContent = objects;
        this.context = context;
        selectedPos = RecyclerView.NO_POSITION;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_arcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setSelected(selectedPos == position);
        holder.textView.setTextColor(holder.itemView.isSelected()?context.getResources().getColor(R.color.commonAccentText) : context.getResources().getColor(R.color.commonPrimaryText));
        holder.animatedView.setColorFilter(holder.itemView.isSelected()?context.getResources().getColor(R.color.commonAccentText) : context.getResources().getColor(R.color.commonPrimaryText));
        holder.cardView.setCardBackgroundColor(holder.itemView.isSelected() ? context.getResources().getColor(R.color.commonAccent) : context.getResources().getColor(R.color.commonPrimary));
        Picasso.with(context).load(downloadedContent.get(position).getImageURL())
                .placeholder(R.drawable.bookcover_loading_anim)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(holder.imageView);

        Log.d(TAG, "onBindViewHolder: " + downloadedContent.get(position).getContName() + downloadedContent.get(position).isAnimated() );
        holder.animatedView.setVisibility(downloadedContent.get(position).isAnimated()? View.VISIBLE: View.GONE);

        holder.textView.setText(downloadedContent.get(position).getContName());

        holder.itemView.setOnClickListener(view -> {
            if(position != selectedPos) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);
                ArViewActivity.setSelectedARModel(downloadedContent.get(position).getFile());
            }else{
                notifyItemChanged(selectedPos);
                selectedPos = RecyclerView.NO_POSITION;
                ArViewActivity.setSelectedARModel(null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloadedContent.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imageView,animatedView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.ARcardView);
            imageView = itemView.findViewById(R.id.ARcardImageView);
            textView = itemView.findViewById(R.id.ARcardTextView);
            animatedView = itemView.findViewById(R.id.ARcardAnimatedView);
        }
    }
}
