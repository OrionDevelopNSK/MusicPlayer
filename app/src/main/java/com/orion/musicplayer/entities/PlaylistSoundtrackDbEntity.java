package com.orion.musicplayer.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;


@Entity(tableName = "playlist_soundtrack",
        primaryKeys = {"playlistName", "data"},
        foreignKeys= {
        @ForeignKey(entity = SoundtrackDbEntity.class,
                parentColumns = "data",
                childColumns = "data"
        ),
        @ForeignKey(
                entity = PlaylistDbEntity.class,
                parentColumns = "playlistName",
                childColumns = "playlistName"
        )
},
        indices = {@Index(value = {"playlistName", "data"}, unique = true)}
)
public class PlaylistSoundtrackDbEntity {
    @NonNull
    public String playlistName;
    @NonNull
    public String data;






}
