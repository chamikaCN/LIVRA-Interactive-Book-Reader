package com.example.chamikanandasiri.interactivebookreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;

import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {

    private ImageButton lib_searchButton;
    private EditText lib_searchText;
    private GridView lib_gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        BookObject book = (BookObject)getIntent().getSerializableExtra("Book");
        ArrayList<BookObject> booksTest = new ArrayList<>();
        booksTest.add(book);
        booksTest.add(book);
        booksTest.add(book);
        booksTest.add(book);
        booksTest.add(book);

        lib_gridView = findViewById(R.id.LibraryGridView);
        lib_searchButton = findViewById(R.id.LibrarySearchButton);
        lib_searchText = findViewById(R.id.LibraryEditText);

        BookArrayAdapter adapter = new BookArrayAdapter(this,R.layout.listitem_book,booksTest);
        lib_gridView.setAdapter(adapter);
    }


}
