package com.example.hitmeup.model;

import java.util.List;

public class User {

    private String id;
    private String name;
    private String surname;
    private String imageURL;

    private List<String> follows;
    private List<String> participatedEvents;

    public User(String id, String name, String surname, String imageURL, List<String> follows, List<String> participatedEvents) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.imageURL = imageURL;
        this.follows = follows;
        this.participatedEvents = participatedEvents;
    }

    public User(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getFollows() {
        return follows;
    }

    public void setFriends(List<String> follows) {
        this.follows = follows;
    }

    public void setFollows(List<String> follows) {
        this.follows = follows;
    }

    public List<String> getParticipatedEvents() {
        return participatedEvents;
    }

    public void setParticipatedEvents(List<String> participatedEvents) {
        this.participatedEvents = participatedEvents;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }
}
