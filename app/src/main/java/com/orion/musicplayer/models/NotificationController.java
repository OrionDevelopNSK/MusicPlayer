package com.orion.musicplayer.models;

import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;

public class NotificationController {
    private final DataModel dataModel;
    private final MediaSessionService mediaSessionService;


    public NotificationController(DataModel dataModel, MediaSessionService mediaSessionService) {
        this.dataModel = dataModel;
        this.mediaSessionService = mediaSessionService;
    }

    public void createOrRefreshNotification() {
        int position = dataModel.getCurrentPositionLiveData().getValue();
        StateMode currentMode = dataModel.getStateModeLiveData().getValue();
        List<Song> songs = dataModel.getSongsLiveData().getValue();
        int rating = songs.get(position).getRating();
        mediaSessionService.createNotification(position,currentMode,rating);
    }
}
