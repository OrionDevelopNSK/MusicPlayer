package com.orion.musicplayer.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.PlaylistSongEntity;
import com.orion.musicplayer.entities.SongEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.repositories.RoomPlaylistRepository;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDatabaseHelper {
    private static final String TAG = PlaylistDatabaseHelper.class.getSimpleName();

    private final Application application;
    private final DataModel dataModel;

    public PlaylistDatabaseHelper(FragmentActivity activity) {
        this.application = activity.getApplication();
        dataModel = new ViewModelProvider(activity).get(DataModel.class);
    }

    public void insertPlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Вставка в базу данных плейлиста");
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            PlaylistEntity playlistDbEntity = playlist.toPlaylistDbEntity();
            List<PlaylistSongEntity> playlistSongEntityList = createPlaylistSoundtrackDbEntityList(playlistDbEntity);
            roomPlaylistRepository.insertPlaylistAndSoundTrack(playlistDbEntity, playlistSongEntityList);
            Log.d(TAG, String.format("Плейлист :%s вставлен в базу данных", playlist.getPlaylistName()));
            dataModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
        });
    }

    public void loadPlaylistWithSoundtrack() {
        AsyncTask.execute(() -> {
            AppDatabase database = AppDatabase.getDatabase(application);
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            dataModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
        });
    }

    public List<PlaylistSongEntity> createPlaylistSoundtrackDbEntityList(PlaylistEntity playlistEntity) {
        List<SongEntity> soundtrackDbEntities = playlistEntity.getSongEntityList();
        List<PlaylistSongEntity> playlistSoundtrackDbEntities = new ArrayList<>();
        for (SongEntity s : soundtrackDbEntities) {
            PlaylistSongEntity pl = new PlaylistSongEntity();
            pl.data = s.data;
            pl.playlistName = playlistEntity.playlistName;
            playlistSoundtrackDbEntities.add(pl);
        }
        return playlistSoundtrackDbEntities;
    }


    public void updatePlaylist(Playlist playlist) {
        //TODO
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Обновление в базе данных плейлиста");
            new RoomPlaylistRepository(database.playlistDao())
                    .updatePlaylists(playlist.toPlaylistDbEntity());
            Log.d(TAG, String.format("Плейлист :%s обновлен", playlist.getPlaylistName()));
        });
    }

    public void deletePlaylist(Playlist playlist) {
        //TODO
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Удаление из базы данных плейлиста");
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            roomPlaylistRepository.deletePlaylists(playlist.toPlaylistDbEntity());
            dataModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
            Log.d(TAG, String.format("Плейлист :%s удален", playlist.getPlaylistName()));
        });
    }


}
