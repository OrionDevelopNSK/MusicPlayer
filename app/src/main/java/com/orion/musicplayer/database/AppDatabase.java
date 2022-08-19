package com.orion.musicplayer.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
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
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                INSTANCE = Room.databaseBuilder(context,
                                AppDatabase.class,
                                "database")
                        .build();
            }
        }
        return INSTANCE;
    }

}
