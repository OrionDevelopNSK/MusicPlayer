package com.orion.musicplayer.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.orion.musicplayer.data.dao.PlaylistDao;
import com.orion.musicplayer.data.dao.SongDao;
import com.orion.musicplayer.data.entities.PlaylistEntity;
import com.orion.musicplayer.data.entities.PlaylistSongEntity;
import com.orion.musicplayer.data.entities.SongEntity;

@Database(entities = {
        SongEntity.class,
        PlaylistEntity.class,
        PlaylistSongEntity.class},
        version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao soundtrackDao();
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
