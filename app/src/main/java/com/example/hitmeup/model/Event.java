package com.example.hitmeup.model;

import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {

    private String author;
    private String eventName;
    private String eventLocation;
    private String eventDate;
    private String imageURL;
    private int minAge;
    private List<String> participants;

    public Event(String author, String eventName, String eventLocation, String eventDate, int minAge, String imageURL, List<String> participants) {
        this.author = author;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventDate = eventDate;
        this.minAge = minAge;
        this.imageURL = imageURL;
        this.participants = participants;
    }

    public Event() {}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public int getMaxAge() {
        return minAge;
    }

    public void setMaxAge(int minAge) {
        this.minAge = minAge;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public List<String> getParticipants() {
        return participants;
    }

    private void addParticipant(String participantId) {
        participants.add(participantId);
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}
