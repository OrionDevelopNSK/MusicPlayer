package com.orion.musicplayer.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;

@Dao
public interface SoundtrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllSoundtracks(List<SoundtrackDbEntity> soundtracks);

    @Delete
    void deleteSoundtracks(SoundtrackDbEntity[] soundtrack);

    @Update
    void updateSoundtrack(SoundtrackDbEntity[] soundtracks);

    @Query("SELECT * FROM soundtrack")
    List<SoundtrackDbEntity> getAll();
}
