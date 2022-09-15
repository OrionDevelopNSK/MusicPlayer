package com.orion.musicplayer.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;


@Entity(tableName = "playlist_song",
        primaryKeys = {"playlistName", "data"},
        foreignKeys= {
        @ForeignKey(entity = SongEntity.class,
                parentColumns = "data",
                childColumns = "data"
        ),
        @ForeignKey(
                entity = PlaylistEntity.class,
                parentColumns = "playlistName",
                childColumns = "playlistName"
        )
},
        indices = {@Index(value = {"playlistName", "data"}, unique = true)}
)
public class PlaylistSongEntity {
    @NonNull
    public String playlistName;
    @NonNull
    public String data;
}
