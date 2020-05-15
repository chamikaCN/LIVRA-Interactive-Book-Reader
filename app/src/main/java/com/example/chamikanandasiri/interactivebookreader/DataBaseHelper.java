package com.example.chamikanandasiri.interactivebookreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "InteractiveBookReader.db";
    private static final String TABLE_COMMENT = "comment_table";
    private static final String TABLE_WORD = "word_table";
    private static final String TABLE_BOOK = "book_table";
    private static final String TABLE_CONTENT = "content_table";

    private String TAG = "Test";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_COMMENT + " (TimeStamp TEXT Primary key, Title TEXT, Phrase TEXT, Comment TEXT)");
        sqLiteDatabase.execSQL("create table " + TABLE_WORD + " (TimeStamp TEXT Primary key, Word TEXT UNIQUE, PartOfSpeech TEXT, Definition TEXT)");
        sqLiteDatabase.execSQL("create table " + TABLE_BOOK + " (BookID TEXT Primary key, TimeStamp TEXT, Title TEXT, Author TEXT, ISBN TEXT, CoverURL TEXT, PublisherID TEXT, PublisherName TEXT)");
        sqLiteDatabase.execSQL("create table " + TABLE_CONTENT + " (ContentID TEXT Primary key, TimeStamp TEXT, BookID TEXT, Name TEXT, Size TEXT, ImageURL TEXT, FileURL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENT);
        sqLiteDatabase.execSQL("DROP tABLE IF EXISTS " + TABLE_WORD);
        sqLiteDatabase.execSQL("DROP tABLE IF EXISTS " + TABLE_BOOK);
        sqLiteDatabase.execSQL("DROP tABLE IF EXISTS " + TABLE_CONTENT);
        onCreate(sqLiteDatabase);
    }

    public boolean insertRowComment(String timestamp, String title, String phrase, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TimeStamp", timestamp);
        cv.put("Title", title);
        cv.put("Phrase", phrase);
        cv.put("Comment", comment);
        long result = db.insert(TABLE_COMMENT, null, cv);
        return result != -1;
    }

    public boolean insertRowWord(String timestamp, String word, String definition, String partofspeech) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TimeStamp", timestamp);
        cv.put("Word", word);
        cv.put("Definition", definition);
        cv.put("PartOfSpeech", partofspeech);
        long result = db.insert(TABLE_WORD, null, cv);
        return result != -1;
    }

    public boolean insertRowBook(String id, String timestamp, String name, String author, String isbn, String cover, String pid, String pname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("BookID", id);
        cv.put("TimeStamp", timestamp);
        cv.put("Title", name);
        cv.put("Author", author);
        cv.put("ISBN", isbn);
        cv.put("CoverURL", cover);
        cv.put("PublisherID", pid);
        cv.put("PublisherName", pname);
        long result = db.insert(TABLE_BOOK, null, cv);
        return result != -1;
    }

    public boolean insertRowContent(String id, String timestamp, String bookID, String name, String size, String imageURL, String fileURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ContentID", id);
        cv.put("TimeStamp", timestamp);
        cv.put("BookID", bookID);
        cv.put("Name", name);
        cv.put("Size", size);
        cv.put("ImageURL", imageURL);
        cv.put("FileURL", fileURL);
        long result = db.insert(TABLE_CONTENT, null, cv);
        return result != -1;
    }

    public Cursor getAllComments() {
        SQLiteDatabase db = this.getReadableDatabase();
        //return db.rawQuery("select Comment from " + TABLE_COMMENT, null);
        return db.query(TABLE_COMMENT, new String[]{"Comment"}, null, null, null, null, "TimeStamp");
    }

    public Cursor getPhraseCommentByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        //return db.rawQuery("select * from " + TABLE_COMMENT + " where Title = \'"+ title +"\'",null);
        return db.query(TABLE_COMMENT, new String[]{"Phrase", "Comment"}, "Title = ?", new String[]{title}, null, null, null);

    }

    public Cursor getAllDistinctTitles() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_COMMENT, new String[]{"Title"}, null, null, null, null, "TimeStamp", null);
    }

    public Cursor getSimilarTitles(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_COMMENT, new String[]{"Title"}, "Title" + " LIKE ?", new String[]{"%" + title + "%"}, null, null, "TimeStamp", null);
    }

    public Cursor getAllDistinctWords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_WORD, new String[]{"Word"}, null, null, null, null, "TimeStamp", null);
    }

    public Cursor getSimilarWords(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_WORD, new String[]{"Word"}, "Word" + " LIKE ?", new String[]{"%" + word + "%"}, null, null, "TimeStamp", null);
    }

    public Cursor getDefinitionPoSByWord(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORD, new String[]{"PartOfSpeech", "Definition"}, "Word = ?", new String[]{word}, null, null, null);
    }

    public Cursor getWordsByPoS(String pos) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_WORD, new String[]{"Word"}, "PartOfSpeech = ?", new String[]{pos}, null, null, null, null);
    }

    public Cursor getWordsPoSDefinitions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORD, new String[]{"Word", "PartOfSpeech", "Definition"}, null, null, null, null, null, null);
    }

    public Cursor getAllBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOK, new String[]{"BookID"}, null, null, null, null, "TimeStamp");
    }

    public Cursor getAllBookDetailsByID(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOK, new String[]{"Title", "Author", "ISBN", "CoverURL"}, "BookID = ?", new String[]{id}, null, null, null, null);
    }

    public Cursor getBookIDByISBN(String ISBN) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOK, new String[]{"BookID"}, "ISBN = ?", new String[]{ISBN}, null, null, null);
    }

    public Cursor getContentDetailsByBooKID(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CONTENT, new String[]{"ContentID", "Name", "ImageURL", "FileURL"}, "BookID = ?", new String[]{id}, null, null, null, null);
    }

    public Cursor getContentIDsByBooKID(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CONTENT, new String[]{"ContentID"}, "BookID = ?", new String[]{id}, null, null, null, null);
    }


}