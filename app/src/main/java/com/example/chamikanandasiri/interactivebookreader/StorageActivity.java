package com.example.chamikanandasiri.interactivebookreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class StorageActivity extends AppCompatActivity {

    DataBaseHelper dataBaseHelper;
    CommentHandler commentHandler;
    WordHandler wordHandler;
    BookHandler bookHandler;
    ListView str_listView;
    EditText str_searchText;
    ImageButton str_searchButton;

    ArrayList<WordObject> wordObjects;
    ArrayList<CommentObject> commentObjects;

    private String TAG = "Test";
    String currentTheme;


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

        setContentView(R.layout.activity_storage);

        dataBaseHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dataBaseHelper, this);
        wordHandler = new WordHandler(dataBaseHelper, this);
        bookHandler = new BookHandler(dataBaseHelper,this);

        str_listView = findViewById(R.id.StorageListView);
        str_searchText = findViewById(R.id.StorageEditText);
        str_searchButton = findViewById(R.id.StorageSearchButton);

        String type = getIntent().getStringExtra("type");
        if (type == null) {
            type = "word";
        }

        if (type.equals("word")) {
            str_searchText.setHint("search by word");
            str_searchButton.setOnClickListener(v -> {
                buttonAnimation1(str_searchButton);
                searchWord(v);
            });
            loadAllWords();
        } else {
            str_searchText.setHint("search by Title");
            str_searchButton.setOnClickListener(v -> {
                buttonAnimation1(str_searchButton);
                searchTitle(v);
            });
            loadAllComments();
        }
    }

    public void searchTitle(View v1) {
        String q = str_searchText.getText().toString();
        if (!q.equals("")) {
            commentObjects = new ArrayList<>();
            ArrayList<String> titles = commentHandler.getSimilarTitles(q);
            for (String title : titles) {
                ArrayList<String[]> phrcmt = commentHandler.getPhraseCommentBookIDbyTitle(title);
                for (String[] a : phrcmt) {
                    commentObjects.add(new CommentObject(title, "\u2606 " + a[0], "\u2606 " + a[1],a[2]));
                }
            }
            commentListGenerate();
        } else {
            loadAllComments();
        }
    }

    public void searchWord(View v1) {
        String q = str_searchText.getText().toString();
        if (!q.equals("")) {
            wordObjects = new ArrayList<>();
            ArrayList<String> words = wordHandler.getSimilarWords(q);
            for (String word : words) {
                ArrayList<String[]> defpos = wordHandler.getDefinitionPosByWord(word);
                StringBuilder definitions = new StringBuilder();
                StringBuilder POSs = new StringBuilder();
                for (String[] a : defpos) {
                    definitions.append("\u2606 ").append("( ").append(a[0].toUpperCase()).append(" ) -  ").append(a[1]).append("\n");
                    POSs.append(a[0]).append("\n");
                }
                wordObjects.add(new WordObject(word.toUpperCase(), definitions.toString(), POSs.toString(), 1));
            }
            wordListGenerate();
        } else {
            loadAllWords();
        }
    }

    private void loadAllWords() {
        wordObjects = new ArrayList<>();
        ArrayList<String> words = wordHandler.getWords();
        for (String word : words) {
            ArrayList<String[]> defpos = wordHandler.getDefinitionPosByWord(word);
            StringBuilder definitions = new StringBuilder();
            StringBuilder POSs = new StringBuilder();
            for (String[] a : defpos) {
                definitions.append("\u2606 ").append("( ").append(a[0].toUpperCase()).append(" ) -  ").append(a[1]).append("\n");
                POSs.append(a[0]).append("\n");
            }
            wordObjects.add(new WordObject(word.toUpperCase(), definitions.toString(), POSs.toString(), 1));
        }
        wordListGenerate();
    }

    private void loadAllComments() {
        commentObjects = new ArrayList<>();
        ArrayList<String> titles = commentHandler.getAllDistinctTitles();
        for (String title : titles) {
            ArrayList<String[]> phrcmt = commentHandler.getPhraseCommentBookIDbyTitle(title);
            for (String[] a : phrcmt) {
                commentObjects.add(new CommentObject(title, "\u2606 " + a[0], "\u2606 " + a[1],a[2]));
            }
        }
        commentListGenerate();
    }

    private void wordListGenerate() {
        WordArrayAdapter adapter = new WordArrayAdapter(this, R.layout.listitem_word, wordObjects);
        str_listView.setAdapter(adapter);
    }

    private void commentListGenerate() {
        CommentArrayAdapter adapter = new CommentArrayAdapter(this, R.layout.listitem_comment, commentObjects,bookHandler);
        str_listView.setAdapter(adapter);
    }

    private void buttonAnimation1(ImageButton button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bi = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(bi);
        button.startAnimation(animation);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

}
