package com.orion.musicplayer;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.repositories.RoomPlaylistRepository;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistController {
    private static final String TAG = PlaylistController.class.getSimpleName();

    private final Application application;
    private final SoundtrackPlayerModel soundtrackPlayerModel;

    public PlaylistController(FragmentActivity activity) {
        this.application = activity.getApplication();
        soundtrackPlayerModel = new ViewModelProvider(activity).get(SoundtrackPlayerModel.class);
        loadPlaylistWithSoundtrack();
    }

    public void insertPlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Вставка в базу данных плейлиста");
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            PlaylistDbEntity playlistDbEntity = playlist.toPlaylistDbEntity();
            List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntityList = createPlaylistSoundtrackDbEntityList(playlistDbEntity);
            roomPlaylistRepository.insertPlaylistAndSoundTrack(playlistDbEntity, playlistSoundtrackDbEntityList);
            Log.d(TAG, String.format("Плейлист :%s вставлен в базу данных", playlist.getPlaylistName()));
            soundtrackPlayerModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
        });
    }

    public void loadPlaylistWithSoundtrack(){
        AsyncTask.execute(() -> {
            AppDatabase database = AppDatabase.getDatabase(application);
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            soundtrackPlayerModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
        });
    }

    public List<PlaylistSoundtrackDbEntity> createPlaylistSoundtrackDbEntityList(PlaylistDbEntity playlistDbEntity){
        List<SoundtrackDbEntity> soundtrackDbEntities = playlistDbEntity.getSoundtrackDbEntityList();
        List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntities = new ArrayList<>();
        for (SoundtrackDbEntity s: soundtrackDbEntities){
            PlaylistSoundtrackDbEntity pl = new PlaylistSoundtrackDbEntity();
            pl.data = s.data;
            pl.playlistName = playlistDbEntity.playlistName;
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
            new RoomPlaylistRepository(database.playlistDao())
                    .deletePlaylists(playlist.toPlaylistDbEntity());
            Log.d(TAG, String.format("Плейлист :%s удален", playlist.getPlaylistName()));
        });
    }




}
