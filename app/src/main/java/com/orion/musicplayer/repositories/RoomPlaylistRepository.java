package com.orion.musicplayer.repositories;

import android.util.Log;

import com.orion.musicplayer.dao.PlaylistDao;
import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.PlaylistSongEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Song;

import java.util.List;
import java.util.Map;

public class RoomPlaylistRepository {
    private static final String TAG = RoomPlaylistRepository.class.getSimpleName();

    private final PlaylistDao playlistDao;

    public RoomPlaylistRepository(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    public void insertOrUpdatePlaylistAndSoundTrack(PlaylistEntity playlistEntity, List<PlaylistSongEntity> playlistSongEntityList) {
        Log.d(TAG, "Внесение данных в базу данных");
        playlistDao.deleteByPlaylistName(playlistEntity.playlistName);
        playlistDao.insertPlaylistAndSoundTrack(playlistEntity, playlistSongEntityList);
    }

    public Map<Playlist, List<Song>> getPlaylistWithSoundTrack() {
        Log.d(TAG, "Предоставление словаря плейлистов с песнями");
        return playlistDao.getPlaylistWithSoundTrack();
    }

    public void deletePlaylists(PlaylistEntity playlist) {
        Log.d(TAG, "Удаление данных из базы данных");
        playlistDao.deletePlaylist(playlist);
    }

}
