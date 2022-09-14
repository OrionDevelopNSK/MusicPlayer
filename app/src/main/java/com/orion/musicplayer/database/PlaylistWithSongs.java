package com.orion.musicplayer.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.orion.musicplayer.entities.PlaylistEntity;
import com.orion.musicplayer.entities.PlaylistSongEntity;
import com.orion.musicplayer.entities.SongEntity;

import java.util.List;

public class PlaylistWithSongs {
    @Embedded
    public PlaylistEntity playlist;
    @Relation(
            parentColumn = "playlistName",
            entityColumn = "data",
            associateBy = @Junction(value = PlaylistSongEntity.class)
    )
    public List<SongEntity> songs;
}
