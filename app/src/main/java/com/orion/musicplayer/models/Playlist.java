package com.orion.musicplayer.models;

import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.SongEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist {
    private String playlistName;
    private List<Song> songs;

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public List<Song> getSoundtracks() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(playlistName, playlist.playlistName);
    }


    @Override
    public int hashCode() {
        return Objects.hash(playlistName);
    }

    public PlaylistEntity toPlaylistDbEntity() {
        PlaylistEntity playlistEntity = new PlaylistEntity();
        List<SongEntity> songEntityList = new ArrayList<>();
        for (Song s: songs){
            SongEntity songEntity = s.toSoundtrackDbEntity();
            songEntityList.add(songEntity);
        }
        playlistEntity.playlistName = playlistName;
        playlistEntity.setSoundtrackDbEntityList(songEntityList);
        return playlistEntity;
    }
}
