package com.orion.musicplayer.entities;

import androidx.room.Entity;

@Entity(tableName = "playlist_soundtrack",
        primaryKeys = {"playlistId", "soundtrackId"}
        )
public class PlaylistSoundtrackDbEntity {
    public long playlistId;
    public long soundtrackId;
}
