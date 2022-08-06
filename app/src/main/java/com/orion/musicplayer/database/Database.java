package com.orion.musicplayer.database;

import androidx.room.Room;

import com.orion.musicplayer.MainActivity;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.dao.PlaylistDao;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.entities.PlaylistDbEntity;

import java.util.List;

public class Database {

    public void init(){
        AppDatabase database = Room.databaseBuilder(MainActivity.getContext(),
                AppDatabase.class,
                "database").
                build();

        SoundtrackDao soundtrackDao = database.soundtrackDao();
        PlaylistDao playlistDao = database.playlistDao();

        List<Soundtrack> soundtracks = soundtrackDao.getAll();
        List<PlaylistDbEntity> playlists = playlistDao.getAll();

        database.close();


    }
}
