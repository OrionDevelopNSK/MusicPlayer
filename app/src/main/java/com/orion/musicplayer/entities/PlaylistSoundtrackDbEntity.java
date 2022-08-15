package com.orion.musicplayer.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "playlist_soundtrack",
        primaryKeys = {"playlist_id", "soundtrack_id"}
        )
@SuppressWarnings("unused")
public class PlaylistSoundtrackDbEntity {
    @ColumnInfo(name = "playlist_id") public long playlistId;
    @ColumnInfo(name = "soundtrack_id") public long soundtrackId;
}
