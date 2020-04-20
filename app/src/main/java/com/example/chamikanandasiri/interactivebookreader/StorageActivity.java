package com.example.chamikanandasiri.interactivebookreader;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class StorageActivity extends AppCompatActivity {

    DataBaseHelper dataBaseHelper;
    CommentHandler commentHandler;
    WordHandler wordHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        dataBaseHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dataBaseHelper, this);
        wordHandler = new WordHandler(dataBaseHelper, this);

        Log.d("Test", wordHandler.getWords().toString());
    }
}
