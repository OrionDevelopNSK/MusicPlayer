package com.orion.musicplayer.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.models.Song;

@Entity(tableName = "song",
        indices = {
        @Index(value = {"data"},
                unique = true)}
)
public class SongEntity {
    @PrimaryKey
    public String data;
    public String title;
    public String artist;
    public int duration;
    public int rating;
    public int countOfLaunches;

    public Song toSoundtrack(){
        Song song = new Song();
        song.setData(data);
        song.setTitle(title);
        song.setArtist(artist);
        song.setDuration(duration);
        song.setRating(rating);
        song.setCountOfLaunches(countOfLaunches);
        return song;
    }


}
