package com.orion.musicplayer.repositories;

import android.util.Log;

import com.orion.musicplayer.dao.PlaylistDao;
import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;
import java.util.Map;

public class RoomPlaylistRepository {
    private static final String TAG = RoomPlaylistRepository.class.getSimpleName();

    private final PlaylistDao playlistDao;

    public RoomPlaylistRepository(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }


    public void insertPlaylistAndSoundTrack(PlaylistDbEntity playlistDbEntity, List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntityList){
        playlistDao.insertAllPlaylist(playlistDbEntity);
        playlistDao.insertAllPlaylist(playlistSoundtrackDbEntityList);
    }

    public Map<Playlist, List<Soundtrack>> getPlaylistWithSoundTrack(){
        return playlistDao.getPlaylistWithSoundTrack();
    }

    public void deletePlaylists(PlaylistDbEntity... playlists) {
        Log.d(TAG, "Удаление данных из базы данных");
        playlistDao.deletePlaylists(playlists);
    }

    public void updatePlaylists(PlaylistDbEntity... playlists) {
        Log.d(TAG, "Обновление данных в базе данных");
        playlistDao.updatePlaylists(playlists);
    }

}
