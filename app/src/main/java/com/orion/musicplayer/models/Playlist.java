package com.orion.musicplayer.models;

import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist {
    private String playlistName;
    private List<Soundtrack> soundtracks;

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public List<Soundtrack> getSoundtracks() {
        return soundtracks;
    }

    public void setSoundtracks(List<Soundtrack> soundtracks) {
        this.soundtracks = soundtracks;
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

    public PlaylistDbEntity toPlaylistDbEntity() {
        PlaylistDbEntity playlistDbEntity = new PlaylistDbEntity();
        List<SoundtrackDbEntity> soundtrackDbEntityList = new ArrayList<>();
        for (Soundtrack s: soundtracks){
            SoundtrackDbEntity soundtrackDbEntity = s.toSoundtrackDbEntity();
            soundtrackDbEntityList.add(soundtrackDbEntity);
        }
        playlistDbEntity.playlistName = playlistName;
        playlistDbEntity.setSoundtrackDbEntityList(soundtrackDbEntityList);
        return playlistDbEntity;
    }
}
