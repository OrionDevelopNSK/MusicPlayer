package com.orion.musicplayer.models;

import java.util.Objects;

public class Playlist {
    public long playlistId;
    public String playlistName;

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
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
}
