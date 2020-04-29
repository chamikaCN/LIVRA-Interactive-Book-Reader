package com.example.chamikanandasiri.interactivebookreader;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class LibraryActivity extends AppCompatActivity {

    private ImageButton lib_searchButton;
    private EditText lib_searchText;
    private GridView lib_gridView;

    private DataBaseHelper dataBaseHelper;
    private BookHandler bookHandler;

    private String TAG = "Test";

    private ArrayList<SimpleBookObject> displayingBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        dataBaseHelper = new DataBaseHelper(this);
        bookHandler = new BookHandler(dataBaseHelper,this);
        displayingBooks = new ArrayList<>();
        lib_gridView = findViewById(R.id.LibraryGridView);
        lib_searchButton = findViewById(R.id.LibrarySearchButton);
        lib_searchText = findViewById(R.id.LibraryEditText);

        loadBookDetails();

//        BookObject book = (BookObject)getIntent().getSerializableExtra("Book");
//        ArrayList<BookObject> booksTest = new ArrayList<>();
//        booksTest.add(book);
//        booksTest.add(book);
//        booksTest.add(book);
//        booksTest.add(book);
//        booksTest.add(book);



    }

    private void loadBookDetails() {
        ArrayList<String> bookIDs = bookHandler.getAllBookIDs();
        for (String id : bookIDs) {
            ArrayList<String> bookdet = bookHandler.getBooksByID(id);
            String title = bookdet.get(0);
            String auth = bookdet.get(1);
            String isbn = bookdet.get(2);
            String img = bookdet.get(3);
            displayingBooks.add(new SimpleBookObject(id,title.toUpperCase(), auth, isbn, img));
        }
        loadView();
    }

    private void loadView() {
        SimpleBookArrayAdapter adapter = new SimpleBookArrayAdapter(this, R.layout.listitem_book, displayingBooks);
        lib_gridView.setAdapter(adapter);
    }


}
