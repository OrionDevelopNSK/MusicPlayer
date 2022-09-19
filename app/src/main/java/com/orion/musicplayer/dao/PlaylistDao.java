package com.orion.musicplayer.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.PlaylistSongEntity;
import com.orion.musicplayer.entities.SongEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dao
public abstract class PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAllPlaylist(PlaylistEntity playlists);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAllPlaylist(List<PlaylistSongEntity> playlistSongEntityList);

    @Query("SELECT * FROM playlist")
    public abstract List<PlaylistEntity> getAll();

    @Transaction
    @Query("SELECT * FROM playlist_song WHERE playlistName =:name")
    public abstract List<PlaylistSongEntity> getListPlaylistSoundtrackDbEntity(String name);

    @Query("SELECT * FROM song WHERE data =:data")
    public abstract SongEntity getSongEntity(String data);

    @Update
    public abstract void updatePlaylists(PlaylistEntity... playlists);

    @Query("DELETE FROM playlist_song WHERE playlistName = :name")
    public abstract void deleteByPlaylistName(String name);

    @Query("DELETE FROM playlist WHERE playlistName = :name")
    public abstract void deleteByPlaylistNameFromPlaylistTable(String name);

    @Transaction
    public void deletePlaylist(PlaylistEntity playlist){
        deleteByPlaylistName(playlist.playlistName);
        deleteByPlaylistNameFromPlaylistTable(playlist.playlistName);
    }

    @Transaction
    public void insertPlaylistAndSoundTrack(PlaylistEntity playlistEntity, List<PlaylistSongEntity> playlistSongEntityList) {
        insertAllPlaylist(playlistEntity);
        insertAllPlaylist(playlistSongEntityList);
    }

    @Transaction
    public Map<Playlist, List<Song>> getPlaylistWithSoundTrack() {
        Map<Playlist, List<Song>> tmpPlaylistWithSoundTrack = new HashMap<>();
        List<PlaylistEntity> all = getAll();
        for (PlaylistEntity playlistEntity : all) {
            List<PlaylistSongEntity> playlistsWithSongs = getListPlaylistSoundtrackDbEntity(playlistEntity.playlistName);
            List<SongEntity> listSongEntity = new ArrayList<>();
            List<Song> tmpSong = new ArrayList<>();
            for (PlaylistSongEntity p : playlistsWithSongs) {
                SongEntity songEntity = this.getSongEntity(p.data);
                listSongEntity.add(songEntity);
                tmpSong.add(songEntity.toSoundtrack());
            }
            playlistEntity.setSoundtrackDbEntityList(listSongEntity);
            tmpPlaylistWithSoundTrack.put(playlistEntity.toPlaylist(), tmpSong);
        }
        return tmpPlaylistWithSoundTrack;
    }

}
