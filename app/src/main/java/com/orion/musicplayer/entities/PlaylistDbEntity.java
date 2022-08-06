package com.orion.musicplayer.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.orion.musicplayer.models.Playlist;

@Entity(tableName = "playlist",
        indices = {@Index(value = {"playlist_name"}, unique = true)},
        foreignKeys = {
                @ForeignKey(
                        entity = SoundtrackDbEntity.class,
                        parentColumns = {"soundtrack_id"},
                        childColumns = {"playlist_id"},
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        })
public class PlaylistDbEntity {
    @PrimaryKey
    @ColumnInfo(name = "playlist_id")
    public long playlistId;
    @ColumnInfo(name = "playlist_name")
    public String playlistName;

    public Playlist toPlaylist(){
        Playlist playlist = new Playlist();
        playlist.playlistName = playlistName;
        return playlist;
    }
}
