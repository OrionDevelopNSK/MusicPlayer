package com.orion.musicplayer;

import android.provider.MediaStore;

public class Soundtrack {

    private String data;
    private String id;
    private String title;
    private String artist;
    private int duration;
    private int rating;
    private int countOfLaunches;
    private int isAlive;

    public void setData(String data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCountOfLaunches(int countOfLaunches) {
        this.countOfLaunches = countOfLaunches;
    }

    public void setAlive(int alive) {
        isAlive = alive;
    }

    public String getData() {
        return data;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public int getRating() {
        return rating;
    }

    public int getCountOfLaunches() {
        return countOfLaunches;
    }

    public int isAlive() {
        return isAlive;
    }
}
