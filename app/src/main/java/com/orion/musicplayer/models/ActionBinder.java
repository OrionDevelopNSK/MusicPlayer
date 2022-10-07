package com.orion.musicplayer.models;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;

public class ActionBinder {
    private static final String TAG = ActionBinder.class.getSimpleName();

    private final DataModel dataModel;
    private final MediaSessionService mediaSessionService;
    private final AppCompatActivity activity;
    private final NotificationController notificationController;

    public ActionBinder(DataModel dataModel,
                        MediaSessionService mediaSessionService,
                        NotificationController notificationController,
                        AppCompatActivity activity) {
        this.dataModel = dataModel;
        this.mediaSessionService = mediaSessionService;
        this.notificationController = notificationController;
        this.activity = activity;
        bindActions();
    }

    private void bindActions() {
        Log.d(TAG, "Создание обсервера нажатия кнопок плеера");
        dataModel.getPlayerActionLiveData().observe(activity, action -> {
            if (action == Action.UNKNOWN) return;
            List<Song> songs = getSongsForCurrentState();
            int position = dataModel.getCurrentPositionLiveData().getValue();

            switch (action) {
                case PLAY:
                    if (!dataModel.getIsPlayingLiveData().getValue()) {
                        mediaSessionService.getSoundsController().playOrPause(position, songs);
                        notificationController.createOrRefreshNotification();
                    }
                    break;
                case PAUSE:
                    if (dataModel.getIsPlayingLiveData().getValue()){
                        mediaSessionService.getSoundsController().playOrPause(position, songs);
                        notificationController.createOrRefreshNotification();
                    }
                    break;
                case PREVIOUS:
                    mediaSessionService.getSoundsController().previous(position, songs);
                    break;
                case NEXT:
                    mediaSessionService.getSoundsController().next(position, songs);
                    break;
                case SWITCH_MODE:
                    mediaSessionService.getSoundsController().switchMode();
                    break;
                case TO_START:
                    mediaSessionService.getSoundsController().playOrPause(0, songs);
                    dataModel.getCurrentPositionLiveData().setValue(0);
                    break;
                case SLIDER_MANIPULATE:
                    mediaSessionService.getSoundsController().setCurrentDuration(
                            dataModel.getDurationLiveData().getValue());
                    break;
            }
            Log.d(TAG, "Выбрано действие: " + action);
            //чтобы команды не проходили повторно при смене ориентации экрана
            dataModel.getPlayerActionLiveData().setValue(Action.UNKNOWN);
        });
    }

    @Nullable
    private List<Song> getSongsForCurrentState() {
        //воспроизведение из общего хранилища или плейлиста
        if (!dataModel.getIsFromPlaylist().getValue()){
            return dataModel.getSongsLiveData().getValue();
        }else{
            dataModel.getCurrentPlayingPlaylist().setValue(dataModel.getCurrentPlaylist().getValue());
            return dataModel.getPlaylistLiveData().getValue().get(dataModel.getCurrentPlaylist().getValue());
        }
    }
}
