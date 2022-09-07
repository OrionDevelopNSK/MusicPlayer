package com.orion.musicplayer;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.orion.musicplayer.dao.PlaylistDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomPlaylistRepository;

import java.util.ArrayList;
import java.util.List;

public class PlaylistController {
    private static final String TAG = PlaylistController.class.getSimpleName();
    private final Application application;

    public PlaylistController(Application application) {
        this.application = application;
    }

    public void insertPlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Вставка в базу данных плейлиста");
            new RoomPlaylistRepository(database.playlistDao())
                    .insertPlaylist(playlist.toPlaylistDbEntity());
            Log.d(TAG, String.format("Плейлист :%s вставлен в базу данных", playlist.getPlaylistName()));
        });
    }

    public void updatePlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Обновление в базе данных плейлиста");
            new RoomPlaylistRepository(database.playlistDao())
                    .updatePlaylists(playlist.toPlaylistDbEntity());
            Log.d(TAG, String.format("Плейлист :%s обновлен", playlist.getPlaylistName()));
        });
    }

    public void deletePlaylist(Playlist playlist) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Удаление из базы данных плейлиста");
            new RoomPlaylistRepository(database.playlistDao())
                    .deletePlaylists(playlist.toPlaylistDbEntity());
            Log.d(TAG, String.format("Плейлист :%s удален", playlist.getPlaylistName()));
        });
    }

    public void getAllPlaylist(){
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            PlaylistDao playlistSoundtrackDao = database.playlistDao();
            List<PlaylistDbEntity> all = playlistSoundtrackDao.getAll();
            List<Playlist> playlists = new ArrayList<>();
            for (PlaylistDbEntity pl: all){
                playlists.add(pl.toPlaylist());

            }

            for (Playlist pl : playlists){
                List<Soundtrack> soundtracks = pl.getSoundtracks();
                for (Soundtrack s : soundtracks){
                    System.out.println(s.getTitle());
                }
            }

            //TODO

//            Log.d(TAG, "Удаление из базы данных плейлиста");
//            PlaylistSoundtrackDao playlistSoundtrackDao = database.playlistSoundtrackDao();
//            Map<Playlist, List<Soundtrack>> playlistListMap = playlistSoundtrackDao.loadPlaylistWithSoundTrack();
//            Set<Playlist> playlists = playlistListMap.keySet();
//            playlists.forEach(System.out::println);
        });
    }

}
