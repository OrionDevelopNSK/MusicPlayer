package com.orion.musicplayer.models;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;

public class DefaultDescriptionControllerFragmentCreator {
    private static final String TAG = DefaultDescriptionControllerFragmentCreator.class.getSimpleName();

    private final DataModel dataModel;
    private final MediaSessionService mediaSessionService;
    private final String soundTitle;
    private final AppCompatActivity activity;

    public DefaultDescriptionControllerFragmentCreator(DataModel dataModel,
                                                       MediaSessionService mediaSessionService,
                                                       String soundTitle,
                                                       AppCompatActivity activity) {
        this.dataModel = dataModel;
        this.mediaSessionService = mediaSessionService;
        this.soundTitle = soundTitle;
        this.activity = activity;
    }

    public void defaultDescription() {
        dataModel.getIsLoadedLiveData().observe(activity, aBoolean -> {
            if (dataModel.getSongsLiveData().getValue() == null
                    || dataModel.getSongsLiveData().getValue().isEmpty()) return;
            List<Song> songs = dataModel.getSongsLiveData().getValue();
            int position = 0;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).getData().equals(soundTitle)) {
                    position = i;
                    Log.d(TAG, "Найдена последняя воиспроизводимая песня, номер: " + position);
                    break;
                }
            }
            dataModel.getCurrentPositionLiveData().setValue(position);
            mediaSessionService.getSoundsController().setCurrentPosition(position);
            mediaSessionService.initMediaPlayer();
        });
    }
}
