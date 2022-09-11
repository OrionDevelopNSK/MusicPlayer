package com.orion.musicplayer.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dao
public abstract class PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAllPlaylist(PlaylistDbEntity playlists);

    @Delete
    public abstract void deletePlaylists(PlaylistDbEntity... playlists);

    @Update
    public abstract void updatePlaylists(PlaylistDbEntity... playlists);

    @Query("SELECT * FROM playlist")
    public abstract List<PlaylistDbEntity> getAll();

    @Transaction
    @Query("SELECT * FROM playlist_soundtrack WHERE playlistName =:name")
    public abstract List<PlaylistSoundtrackDbEntity> getListPlaylistSoundtrackDbEntity(String name);



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAllPlaylist(List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntityList);


    @Transaction
    public void insertPlaylistAndSoundTrack(PlaylistDbEntity playlistDbEntity, List<PlaylistSoundtrackDbEntity> playlistSoundtrackDbEntityList){
        insertAllPlaylist(playlistDbEntity);
        insertAllPlaylist(playlistSoundtrackDbEntityList);
    }

    @Query("SELECT * FROM soundtrack WHERE data =:data")
    public abstract List<SoundtrackDbEntity> getListSoundtrackDbEntity(String data);

    @Query("SELECT * FROM soundtrack WHERE data =:data")
    public abstract SoundtrackDbEntity getSoundtrackDbEntity(String data);


    @Transaction
    public Map<Playlist, List<Soundtrack>> getPlaylistWithSoundTrack(){
        Map<Playlist, List<Soundtrack>> tmpPlaylistWithSoundTrack = new HashMap<>();
//        Map<PlaylistDbEntity, List<Soundtrack>> tmpPlaylistWithSoundTrackTT = new HashMap<>();
        List<PlaylistDbEntity> all = getAll();
        for (PlaylistDbEntity playlistDbEntity : all){
            List<PlaylistSoundtrackDbEntity> playlistsWithSongs = getListPlaylistSoundtrackDbEntity(playlistDbEntity.playlistName);
            List<SoundtrackDbEntity> listSoundtrackDbEntity = new ArrayList<>();
            List<Soundtrack> tmpSoundtrack = new ArrayList<>();
            for (PlaylistSoundtrackDbEntity p : playlistsWithSongs){
                SoundtrackDbEntity soundtrackDbEntity = this.getSoundtrackDbEntity(p.data);
                listSoundtrackDbEntity.add(soundtrackDbEntity);
                tmpSoundtrack.add(soundtrackDbEntity.toSoundtrack());
            }
            playlistDbEntity.setSoundtrackDbEntityList(listSoundtrackDbEntity);
            tmpPlaylistWithSoundTrack.put(playlistDbEntity.toPlaylist(), tmpSoundtrack);
        }
        return tmpPlaylistWithSoundTrack;
    }


}
