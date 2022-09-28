package com.orion.musicplayer.utils;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.orion.musicplayer.services.MediaSessionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaScannerObserver extends ContentObserver {
    private static final String TAG = MediaScannerObserver.class.getSimpleName();

    private final MediaSessionService mediaSessionService;
    private SortingType sortingType;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MediaScannerObserver(Handler handler, Activity activity, MediaSessionService mediaSessionService, SortingType sortingType) {
        super(handler);
        this.mediaSessionService = mediaSessionService;
        this.sortingType = sortingType;

        Log.d(TAG, "Регистрация обсервера хранилища мультимедиа");
        activity.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, this
        );
    }

    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "Изменение хранилища аудиофайлов");
        executor.execute(() -> mediaSessionService.getDataLoader().execute(sortingType));
    }


}
