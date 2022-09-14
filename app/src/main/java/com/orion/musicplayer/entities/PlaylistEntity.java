package com.orion.musicplayer.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

//@Entity(tableName = "playlist",
//        indices = {@Index(value = {"playlistName"}, unique = true)},
//        foreignKeys = {
//                @ForeignKey(
//                        entity = SoundtrackDbEntity.class,
//                        parentColumns = {"soundtrackId"},  //"soundtrackId"
//                        childColumns = {"playlistId"},     //"playlistId"
//                        onDelete = ForeignKey.CASCADE,
//                        onUpdate = ForeignKey.CASCADE)
//        })


@Entity(tableName = "playlist")
public class PlaylistEntity {
    @PrimaryKey
    public String playlistName;
    @Ignore
    public List<SongEntity> songEntityList;

    public Playlist toPlaylist(){
        Playlist playlist = new Playlist();
        playlist.setPlaylistName(playlistName);
        List<Song> songs = new ArrayList<>();
        for (SongEntity s: songEntityList){
            songs.add(s.toSoundtrack());
        }
        playlist.setSongs(songs);
        return playlist;
    }

    public List<SongEntity> getSongEntityList() {
        return songEntityList;
    }

    public void setSoundtrackDbEntityList(List<SongEntity> songEntityList) {
        this.songEntityList = songEntityList;
    }
}
