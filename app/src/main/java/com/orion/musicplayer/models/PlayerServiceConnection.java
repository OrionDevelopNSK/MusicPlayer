package com.orion.musicplayer.models;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.orion.musicplayer.data.database.DataLoader;
import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.Objects;

public class PlayerServiceConnection implements ServiceConnection {
    private static final String TAG = PlayerServiceConnection.class.getSimpleName();
    private ActionBinder actionBinder;
    private NotificationController notificationController;
    private DefaultDescriptionControllerFragmentCreator defaultDescriptionControllerFragmentCreator;
    private MediaSessionService mediaSessionService;
    private MediaScannerObserver mediaScannerObserver;


    private DataModel dataModel;
    private PlaylistDatabaseHelper playlistDatabaseHelper;
    private MainActivity activity;
    private SharedPreferencesController sharedPreferencesController;

    public PlayerServiceConnection(DataModel dataModel,
                                   PlaylistDatabaseHelper playlistDatabaseHelper,
                                   MainActivity activity,
                                   SharedPreferencesController sharedPreferencesController) {
        this.dataModel = dataModel;
        this.playlistDatabaseHelper = playlistDatabaseHelper;
        this.activity = activity;
        this.sharedPreferencesController = sharedPreferencesController;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        Log.d(TAG, "Подключение сервиса");
        mediaSessionService = ((MediaSessionService.BinderService) binder).getService();
        subscribeDatabaseLoadListeners();
        mediaSessionService.getDataLoader().execute(dataModel.getSortingTypeLiveData().getValue());
        playlistDatabaseHelper.loadPlaylistWithSoundtrack();
        subscribeSoundsControllerListeners();
        createMediaScannerObserver();
        notificationController = new NotificationController(dataModel, mediaSessionService);
        actionBinder = new ActionBinder(dataModel, mediaSessionService, notificationController, activity);
        actionBinder.bindActions();
        defaultDescriptionControllerFragmentCreator =
                new DefaultDescriptionControllerFragmentCreator(
                        dataModel,
                        mediaSessionService,
                        sharedPreferencesController.getSoundTitle(),
                        activity);
        defaultDescriptionControllerFragmentCreator.defaultDescription();
        createDataValidateObserver();
        createStateModeObserver();
        createSortingTypeObserver();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    private void subscribeDatabaseLoadListeners() {
        DataLoader dataLoader = mediaSessionService.getDataLoader();
        dataLoader.setOnDatabaseLoadListener(soundtracks -> {
            dataModel.getSongsLiveData().postValue(soundtracks);
            dataModel.getIsLoadedLiveData().postValue(true);
        });
    }

    private void subscribeSoundsControllerListeners() {
        PlayerController soundsController = mediaSessionService.getSoundsController();
        soundsController.setOnChangeStateModeListener(stateMode -> dataModel.getStateModeLiveData().setValue(stateMode));
        soundsController.setOnCurrentDurationListener(duration -> dataModel.getDurationLiveData().setValue(duration));
        soundsController.setOnCurrentPositionListener(position -> {
            dataModel.getCurrentPositionLiveData().setValue(position);
            notificationController.createOrRefreshNotification();
        });
        soundsController.setOnPlayingStatusListener(isPlay -> dataModel.getIsPlayingLiveData().setValue(isPlay));
    }

    private void createMediaScannerObserver() {
        mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                activity, mediaSessionService, sharedPreferencesController.getSortingType());
    }

    private void createDataValidateObserver() {
        dataModel.getSongsLiveData().observe(activity, soundtracks -> {
            if (!Objects.requireNonNull(dataModel.getSongsLiveData().getValue()).isEmpty()) return;
            TextView textView = new TextView(activity.getApplicationContext());
            textView.setText("Песни отсутствуют");
            textView.setGravity(Gravity.CENTER);
            activity.setContentView(textView);
        });
    }

    private void createStateModeObserver() {
        dataModel.getStateModeLiveData().observe(activity, stateMode -> {
            mediaSessionService.getSoundsController().setStateMode(stateMode);
            mediaSessionService.getSoundsController().clearDequeSoundtrack();
        });
    }

    private void createSortingTypeObserver() {
        dataModel.getSortingTypeLiveData().observe(activity, thisSortingType -> {
            mediaScannerObserver.setSortingType(thisSortingType);
            mediaSessionService.getDataLoader().refresh(thisSortingType);
            mediaSessionService.getSoundsController().clearDequeSoundtrack();
        });
    }
}
