package com.example.chamikanandasiri.interactivebookreader;

import java.io.File;
import java.io.Serializable;

public class SimpleContentObject implements Serializable {

    private String id;
    private String imageURL;
    private String name;
    private String bookID;
    private String fileURL;
    private File file;

    private String TAG = "Test";

    public SimpleContentObject(String id, String imageURL, String name, String bookID, String fileURL) {
        this.id = id;
        this.imageURL = imageURL;
        this.name = name;
        this.bookID = bookID;
        this.fileURL = fileURL;
    }

    public String getContId() {
        return id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getContName() {
        return name;
    }

    public String getContentBookID() {
        return bookID;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFile(File f) {
        this.file = f;
    }

    public File getFile() {
        return file;
    }
}
