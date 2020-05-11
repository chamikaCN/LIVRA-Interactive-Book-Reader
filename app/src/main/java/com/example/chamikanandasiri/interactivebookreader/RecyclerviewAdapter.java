package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder>{

    private ArrayList<String> textNames = new ArrayList<>();
    private ArrayList<Integer> imagesPath = new ArrayList<>();
    private Context context;
    private ArrayList<String> modelNames = new ArrayList<>();

    private  ArrayList<File> arImagesPath = new ArrayList<>();
    private ArrayList<File> arModel = new ArrayList<>();

    /*public RecyclerviewAdapter(Context context, ArrayList<String> textNames, ArrayList<Integer> imagesPath, ArrayList<String> modelNames) {
        this.textNames = textNames;
        this.imagesPath = imagesPath;
        this.modelNames = modelNames;
        this.context = context;
    }*/

    public RecyclerviewAdapter(Context context, ArrayList<File> arImagesPath, ArrayList<File> arModel) {
        this.arImagesPath=arImagesPath;
        this.arModel=arModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_arcard,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.imageView.setImageResource(imagesPath.get(position));
//        holder.textView.setText(textNames.get(position));
//
//        holder.imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });
//        holder.imageView.setImageResource(imagesPath.get(position));
        holder.imageView.setImageURI(Uri.fromFile(arImagesPath.get(position)));

        holder.textView.setText(arImagesPath.get(position).getName());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.model = arModel.get(position);
            }
        });
    }


    @Override
    public int getItemCount() {
//        return imagesPath.size();
        return  arModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
