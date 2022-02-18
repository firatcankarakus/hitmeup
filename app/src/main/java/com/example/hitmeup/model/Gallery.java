package com.example.hitmeup.model;

import com.google.firebase.database.util.GAuthToken;

public class Gallery {

    private String imageURL;
    private String author;

    public Gallery(String imageURL, String author) {
        this.imageURL = imageURL;
        this.author = author;
    }

    public Gallery(){

    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
