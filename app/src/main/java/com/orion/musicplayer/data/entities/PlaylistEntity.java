package com.orion.musicplayer.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.data.models.Playlist;
import com.orion.musicplayer.data.models.Song;

import java.util.ArrayList;
import java.util.List;


@Entity(tableName = "playlist")
public class PlaylistEntity {
    @PrimaryKey
    @NonNull
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
