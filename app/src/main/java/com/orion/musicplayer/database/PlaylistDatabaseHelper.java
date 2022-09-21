package com.orion.musicplayer.database;

import android.app.Application;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.PlaylistSongEntity;
import com.orion.musicplayer.entities.SongEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Song;
import com.orion.musicplayer.repositories.RoomPlaylistRepository;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistDatabaseHelper {
    private static final String TAG = PlaylistDatabaseHelper.class.getSimpleName();

    private final Application application;
    private final DataModel dataModel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PlaylistDatabaseHelper(FragmentActivity activity) {
        this.application = activity.getApplication();
        dataModel = new ViewModelProvider(activity).get(DataModel.class);
    }

    public void insertOrUpdatePlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        executor.execute(() -> {
            Log.d(TAG, "Вставка в базу данных плейлиста");
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            PlaylistEntity playlistDbEntity = playlist.toPlaylistDbEntity();
            List<PlaylistSongEntity> playlistSongEntityList = createPlaylistSoundtrackDbEntityList(playlistDbEntity);
            roomPlaylistRepository.insertOrUpdatePlaylistAndSoundTrack(playlistDbEntity, playlistSongEntityList);
            Log.d(TAG, String.format("Плейлист :%s вставлен в базу данных", playlist.getPlaylistName()));
            dataModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
        });
    }

    public void loadPlaylistWithSoundtrack() {
        executor.execute(() -> {
            AppDatabase database = AppDatabase.getDatabase(application);
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            try {
                Map<Playlist, List<Song>> playlistWithSoundTrack = roomPlaylistRepository.getPlaylistWithSoundTrack();
                dataModel.getPlaylistLiveData().postValue(playlistWithSoundTrack);
            }catch (IllegalStateException e){
                Log.d(TAG, "На устройстве отсутствуют песни");
            }
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

    public void deletePlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        executor.execute(() -> {
            Log.d(TAG, "Удаление из базы данных плейлиста");
            RoomPlaylistRepository roomPlaylistRepository = new RoomPlaylistRepository(database.playlistDao());
            roomPlaylistRepository.deletePlaylists(playlist.toPlaylistDbEntity());
            dataModel.getPlaylistLiveData().postValue(roomPlaylistRepository.getPlaylistWithSoundTrack());
            Log.d(TAG, String.format("Плейлист :%s удален", playlist.getPlaylistName()));
        });
    }


}
