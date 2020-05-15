package com.example.chamikanandasiri.interactivebookreader;

public class SimpleBookObject {

    private String id;
    private String title;
    private String author;
    private String isbn;
    private String cover;

    private String TAG = "Test";

    public SimpleBookObject(String id, String title, String author, String isbn, String cover) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.cover = cover;
    }


    public String getBookId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCover() {
        return cover;
    }
}
