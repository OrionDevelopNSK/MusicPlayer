package com.orion.musicplayer.utils;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.orion.musicplayer.MediaSessionService;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

public class MediaScannerObserver extends ContentObserver {
    private static final String TAG = MediaScannerObserver.class.getSimpleName();

    private final MediaSessionService mediaSessionService;

    public MediaScannerObserver(Handler handler, Activity activity, MediaSessionService mediaSessionService) {
        super(handler);
        this.mediaSessionService = mediaSessionService;

        Log.d(TAG, "Регистрация обсервера хранилища мультимедиа");
        activity.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, this
        );
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "Изменение хранилища аудиофайлов");
        AsyncTask.execute(()-> mediaSessionService.getDataLoader().execute());
    }


}
