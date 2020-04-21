package com.example.chamikanandasiri.interactivebookreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "InteractiveBookReader.db";
    private static final String TABLE_COMMENT = "comment_table";
    private static final String TABLE_WORD = "word_table";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_COMMENT + " (TimeStamp TEXT Primary key, Title TEXT, Phrase TEXT, Comment TEXT)");
        sqLiteDatabase.execSQL("create table " + TABLE_WORD + " (TimeStamp TEXT Primary key, Word TEXT, PartOfSpeech TEXT, Definition TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENT);
        sqLiteDatabase.execSQL("DROP tABLE IF EXISTS " + TABLE_WORD);
        onCreate(sqLiteDatabase);
    }

    public boolean insertRowComment(String timestamp, String title, String phrase, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TimeStamp", timestamp);
        cv.put("Title", title);
        cv.put("Phrase", phrase);
        cv.put("Comment", comment);
        Log.d("Test","came here");
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
        return db.query(true, TABLE_COMMENT, new String[]{"Title"}, "Title" + " LIKE ?", new String[] {"%"+ title+ "%" }, null, null, "TimeStamp", null);
    }

    public Cursor getAllDistinctWords() {
        SQLiteDatabase db = this.getReadableDatabase();
        //return db.rawQuery("select Comment from " + TABLE_COMMENT, null);
        return db.query(true, TABLE_WORD, new String[]{"Word"}, null, null, null, null, "TimeStamp", null);
    }

    public Cursor getSimilarWords (String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        //return db.rawQuery("select Comment from " + TABLE_COMMENT, null);
        return db.query(true, TABLE_WORD, new String[]{"Word"},  "Word" + " LIKE ?",new String[] {"%"+ word+ "%" }, null, null, null, "TimeStamp", null);
    }

    public Cursor getDefinitionPoSByWord(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        //return db.rawQuery("select * from " + TABLE_COMMENT + " where Title = \'"+ title +"\'",null);
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
}


//    public Cursor getAllTransactions() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor transactions = db.rawQuery("select * from " + TABLE_TRANSACTION, null);
//        return transactions;
//    }
//
//    public Cursor getLimitedTransactions(int limit) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor limitTransactions = db.rawQuery("select * from " + TABLE_TRANSACTION + " limit " + limit, null);
//        return limitTransactions;
//    }
//
//    public Cursor getAllAccountNumbers() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor accountNumbers = db.rawQuery("select AccountNo from " + TABLE_ACCOUNT, null);
//        System.out.println(accountNumbers);
//        return accountNumbers;
//    }
//
//    public Cursor getAllAccounts() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor accounts = db.rawQuery("select * from " + TABLE_ACCOUNT, null);
//        return accounts;
//    }
//
//    public Cursor getAccount(String AccNo) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor account = db.rawQuery("select * from " + TABLE_ACCOUNT + " where AccountNo = \'" + AccNo + "\'", null);
//        return account;
//    }
//
//    public void removeAccount(String AccNo) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor account = db.rawQuery("delete from " + TABLE_ACCOUNT + " where AccountNo = \'" + AccNo + "\'", null);
//    }

//    public void updateAccount(String AccNo, String Bank, String Holder, Double Balance){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put("AccountNo", AccNo);
//        cv.put("Bank", Bank);
//        cv.put("AccountHolder", Holder);
//        cv.put("Balance",Balance);
//        db.update(TABLE_ACCOUNT,null,"AccNo=" + AccNo ,null);
//    }

