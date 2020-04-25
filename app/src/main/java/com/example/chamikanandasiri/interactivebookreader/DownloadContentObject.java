package com.example.chamikanandasiri.interactivebookreader;

public class DownloadContentObject {

    private String id;
    private String[] imageURLs;
    private String name;
    private String description;
    private String size;
    private String fileURL;

    public DownloadContentObject(String id, String[] imageURLs, String name, String description, String size, String fileURL) {
        this.id = id;
        this.imageURLs = imageURLs;
        this.name = name;
        this.description = description;
        this.size = size;
        this.fileURL = fileURL;
    }

    public String getContId() {
        return id;
    }

    public String[] getImageURLs() {
        return imageURLs;
    }

    public String getContName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContSize() {
        return size;
    }

    public String getFileURL() {
        return fileURL;
    }
}
