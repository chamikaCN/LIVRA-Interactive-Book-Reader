package com.example.chamikanandasiri.interactivebookreader;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryActivity extends AppCompatActivity {

    private ImageButton lib_searchButton, lib_contentRemoveButton, del_closeButton;
    private Button del_deleteButton, con_confirmButton, con_cancelButton;
    private ListView del_listView;
    private EditText lib_searchText;
    private Dialog deletePopup, confirmPopup;

    private static String selectedBook;

    private DataBaseHelper dataBaseHelper;
    private BookHandler bookHandler;
    private ContentHandler contentHandler;
    private ToastManager toastManager;

    private String TAG = "Test";
    String currentTheme;
    private ArrayList<SimpleBookObject> displayingBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentTheme = sharedPreferences.getString("Theme", "Light");
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightTheme);
        } else if (currentTheme.equals("Dark")) {
            setTheme(R.style.DarkTheme);
        }

        setContentView(R.layout.activity_library);

        dataBaseHelper = new DataBaseHelper(this);
        bookHandler = new BookHandler(dataBaseHelper, this);
        contentHandler = new ContentHandler(dataBaseHelper, this);
        toastManager = new ToastManager(getApplicationContext());

        deletePopup = new Dialog(this);
        deletePopup.setContentView(R.layout.popup_delete);
        confirmPopup = new Dialog(this);
        confirmPopup.setContentView(R.layout.popup_confirm);
        setupDeletePopup();
        setupConfirmPopup();

        lib_searchButton = findViewById(R.id.LibrarySearchButton);
        lib_searchText = findViewById(R.id.LibraryEditText);
        lib_contentRemoveButton = findViewById(R.id.LibraryBookDetailsButton);
        //lib_commentButton = findViewById(R.id.LibraryCommentsButton);
        lib_searchButton.setOnClickListener(v -> searchBooks());
        lib_contentRemoveButton.setOnClickListener(v -> {
            if (selectedBook != null) {
                showDeletePopup(v);
            } else {
                toastManager.showShortToast("Select a book to remove");
            }
        });
//        lib_commentButton.setOnClickListener(v->{
//            Intent intent = new Intent(this,StorageActivity.class);
//            intent.putExtra("bookComment",true);
//            intent.putExtra("type","comment");
//            startActivity(intent);
//        });
//        lib_commentButton.setVisibility(View.GONE);
        setSelectedBook(null);
        loadBookDetails();
    }

    private void setupDeletePopup() {
        del_closeButton = deletePopup.findViewById(R.id.DeleteCloseButton);
        del_deleteButton = deletePopup.findViewById(R.id.DeleteOKButton);
        del_listView = deletePopup.findViewById(R.id.DeleteListView);
    }

    private void setupConfirmPopup() {
        con_confirmButton = confirmPopup.findViewById(R.id.ConfirmOKButton);
        con_cancelButton = confirmPopup.findViewById(R.id.ConfirmCancelButton);
    }

    public void showDeletePopup(View v2) {
        loadDeleteDetails();
        del_closeButton.setOnClickListener(v -> {
            deletePopup.dismiss();
        });
        deletePopup.show();
    }

    public void showConfirmPopup(View v2) {
        con_cancelButton.setOnClickListener(v -> confirmPopup.dismiss());
        confirmPopup.show();
    }

    private void loadDeleteDetails() {
        ArrayList<String[]> DBcontents = contentHandler.getContentsByBookID(selectedBook);
        ArrayList<SimpleContentObject> contents = new ArrayList<>();
        for (String[] a : DBcontents) {
            contents.add(new SimpleContentObject(a[0], a[2], a[1], selectedBook, a[3], a[4].equals("1")));
        }
        DeleteContentArrayAdapter adapter = new DeleteContentArrayAdapter(this, R.layout.listitem_content, contents);
        del_listView.setAdapter(adapter);

        del_deleteButton.setOnClickListener(v -> {
            if (!adapter.isAnySelected()) {
                toastManager.showShortToast("select at least one to delete");
            } else {
                showConfirmPopup(v);
            }
        });
        con_confirmButton.setOnClickListener(v -> {
            if (adapter.isAllSelected()) {
                deleteBook();
                setSelectedBook(null);
                loadBookDetails();
                deletePopup.dismiss();
            } else {
                ArrayList<SimpleContentObject> selected = adapter.getSelectedObjects();
                boolean delSuccess = true;
                for (SimpleContentObject s : selected) {
                    delSuccess = delSuccess && deleteContent(s.getContId());
                }
                if (delSuccess) {
                    toastManager.showShortToast("deleted Successfully");
                } else {
                    toastManager.showShortToast("failed to delete");
                }
                loadDeleteDetails();
            }
            confirmPopup.dismiss();
        });
    }

    private void deleteBook() {
        boolean dbSuccess = bookHandler.deleteBook(selectedBook);
        boolean fileSuccess = false;
        if (dbSuccess) {
            File f = new File(this.getFilesDir(), selectedBook);
            File ar = new File(f, "ar");
            File[] conts = ar.listFiles();
            if (conts != null) {
                for (File m : conts) {
                    fileSuccess = m.delete();
                }
            }
            fileSuccess = fileSuccess && ar.delete();
            fileSuccess = fileSuccess && f.delete();
        }
        if (fileSuccess) {
            toastManager.showShortToast("deleted Successfully");
        } else {
            toastManager.showShortToast("failed to delete");
        }
    }

    private boolean deleteContent(String contentId) {
        boolean dbSuccess = contentHandler.deleteContent(contentId);
        boolean fileSuccess = false;
        File f = new File(this.getFilesDir(), selectedBook);
        File ar = new File(f, "ar");
        File content = new File(ar, contentId + ".sfb");
        if (content.exists() && dbSuccess) {
            fileSuccess = content.delete();
        }
        return fileSuccess;
    }


    private void loadBookDetails() {
        displayingBooks = new ArrayList<>();
        ArrayList<String> bookIDs = bookHandler.getAllBookIDs();
        for (String id : bookIDs) {
            ArrayList<String> bookdet = bookHandler.getBooksByID(id);
            String title = bookdet.get(0);
            String auth = bookdet.get(1);
            String isbn = bookdet.get(2);
            String img = bookdet.get(3);
            displayingBooks.add(new SimpleBookObject(id, title.toUpperCase(), auth, isbn, img));
        }
        loadView();
    }

    private void searchBooks() {
        String q = lib_searchText.getText().toString();
        if (q.length() > 0) {
            displayingBooks = new ArrayList<>();
            ArrayList<String> bookIDs = bookHandler.getSimilarBookIDs(q);
            for (String id : bookIDs) {
                ArrayList<String> bookdet = bookHandler.getBooksByID(id);
                String title = bookdet.get(0);
                String auth = bookdet.get(1);
                String isbn = bookdet.get(2);
                String img = bookdet.get(3);
                displayingBooks.add(new SimpleBookObject(id, title.toUpperCase(), auth, isbn, img));
            }
            loadView();
        } else {
            toastManager.showShortToast("Search query Can not be empty");
            loadBookDetails();
        }
    }

    private void loadView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        RecyclerView recyclerView = findViewById(R.id.LibraryGridView);
        recyclerView.setLayoutManager(layoutManager);
        SimpleBookArrayAdapter adapter = new SimpleBookArrayAdapter(this, displayingBooks);
        recyclerView.setAdapter(adapter);

    }

    public static void setSelectedBook(String bookID) {
        selectedBook = bookID;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
