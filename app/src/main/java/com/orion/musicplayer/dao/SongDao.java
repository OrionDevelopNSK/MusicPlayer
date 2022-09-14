package com.orion.musicplayer.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.orion.musicplayer.entities.SongEntity;

import java.util.List;

@Dao
public interface SongDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllSongs(List<SongEntity> soundtracks);

    @Delete
    void deleteSoundtracks(SongEntity... soundtrack);

    @Update
    void updateSoundtrack(SongEntity... soundtracks);

    @Query("SELECT * FROM song")
    List<SongEntity> getAll();


}
