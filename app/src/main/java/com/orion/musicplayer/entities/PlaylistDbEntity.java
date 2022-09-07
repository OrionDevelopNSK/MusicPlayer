package com.orion.musicplayer.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "playlist",
        indices = {@Index(value = {"playlistName"}, unique = true)},
        foreignKeys = {
                @ForeignKey(
                        entity = SoundtrackDbEntity.class,
                        parentColumns = {"soundtrackId"},  //"soundtrack_id"
                        childColumns = {"playlistId"},     //"playlist_id"
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        })
public class PlaylistDbEntity {
    @PrimaryKey
    public long playlistId = 1;
    public String playlistName;

    @Ignore
    public List<SoundtrackDbEntity> soundtrackDbEntityList;

    public Playlist toPlaylist(){
        Playlist playlist = new Playlist();
        playlist.playlistName = playlistName;
        List<Soundtrack> soundtracks = new ArrayList<>();
        for (SoundtrackDbEntity s: soundtrackDbEntityList){
            soundtracks.add(s.toSoundtrack());
        }
        playlist.setSoundtracks(soundtracks);
        return playlist;
    }

    public List<SoundtrackDbEntity> getSoundtrackDbEntityList() {
        return soundtrackDbEntityList;
    }

    public void setSoundtrackDbEntityList(List<SoundtrackDbEntity> soundtrackDbEntityList) {
        this.soundtrackDbEntityList = soundtrackDbEntityList;
    }
}
