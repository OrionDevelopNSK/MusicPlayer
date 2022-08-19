package com.orion.musicplayer.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.models.Soundtrack;

@Entity(tableName = "soundtrack",
        indices = {
        @Index(value = {"data" , "title"},
                unique = true)}
)
public class SoundtrackDbEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "soundtrack_id")
    public long soundtrackId;
    public String data;
    public String title;
    public String artist;
    public int duration;
    public int rating;
    @ColumnInfo(name = "count_of_launches")
    public int countOfLaunches;

    public Soundtrack toSoundtrack(){
        Soundtrack soundtrack = new Soundtrack();
        soundtrack.setId(soundtrackId);
        soundtrack.setData(data);
        soundtrack.setTitle(title);
        soundtrack.setArtist(artist);
        soundtrack.setDuration(duration);
        soundtrack.setRating(rating);
        soundtrack.setCountOfLaunches(countOfLaunches);
        return soundtrack;
    }


}
