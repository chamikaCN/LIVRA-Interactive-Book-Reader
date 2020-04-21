package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class WordArrayAdapter extends ArrayAdapter<WordObject> {
    private Context context;
    private  int resource;

    public WordArrayAdapter(Context context, int resource, ArrayList<WordObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String word = getItem(position).getWord();
        String definition = getItem(position).getDefinition();
        String pos = getItem(position).getPartOfSpeech();

        WordObject wo = new WordObject(word,definition,pos);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource,parent,false);

        TextView tvWord = convertView.findViewById(R.id.WordCardWord);
        TextView tvDef = convertView.findViewById(R.id.WordCardDefinition);
        TextView tvPos = convertView.findViewById(R.id.WordCardPOS);

        tvWord.setText(word);
        tvDef.setText(definition);
        tvPos.setText(pos);

        return convertView;
    }

}
