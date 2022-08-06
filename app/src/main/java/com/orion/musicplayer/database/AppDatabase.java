package com.orion.musicplayer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.orion.musicplayer.dao.PlaylistDao;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;

@Database(entities = {
        SoundtrackDbEntity.class,
        PlaylistDbEntity.class,
        PlaylistSoundtrackDbEntity.class},
        version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SoundtrackDao soundtrackDao();
    public abstract PlaylistDao playlistDao();

}
