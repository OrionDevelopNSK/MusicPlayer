package com.orion.musicplayer.models;

import java.util.Objects;

public class Soundtrack {

    private String data;
    private String id;
    private String title;
    private String artist;
    private int duration;
    private int rating;
    private int countOfLaunches;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Soundtrack that = (Soundtrack) o;
        return data.equals(that.data) && title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, title);
    }
}
