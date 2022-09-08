package com.orion.musicplayer.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.orion.musicplayer.database.PlaylistWithSoundtracks;
import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllPlaylist(PlaylistDbEntity playlists);

    @Delete
    void deletePlaylists(PlaylistDbEntity... playlists);

    @Update
    void updatePlaylists(PlaylistDbEntity... playlists);

    @Query("SELECT * FROM playlist")
    List<PlaylistDbEntity> getAll();

    @Transaction
    @Query("SELECT * FROM playlist")
    List<PlaylistWithSoundtracks> getPlaylistsWithSongs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPlaylist(List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntityList);


}
