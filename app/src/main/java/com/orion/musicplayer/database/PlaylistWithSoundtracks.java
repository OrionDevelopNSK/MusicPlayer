package com.orion.musicplayer.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.PlaylistSoundtrackDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;

import java.util.List;

public class PlaylistWithSoundtracks {
    @Embedded
    public PlaylistDbEntity playlist;
    @Relation(
            parentColumn = "playlistId",
            entityColumn = "soundtrackId",
            associateBy = @Junction(value = PlaylistSoundtrackDbEntity.class,
                    parentColumn = "playlistId",
                    entityColumn = "soundtrackId"
            )
    )
    public List<SoundtrackDbEntity> songs;
}
