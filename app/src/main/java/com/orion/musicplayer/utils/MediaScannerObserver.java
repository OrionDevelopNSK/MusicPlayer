package com.orion.musicplayer.utils;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

public class MediaScannerObserver extends ContentObserver {

    final SoundtracksModel soundtracksModel;
    final AppDatabase database;
    final AudioReader audioReader;

    public MediaScannerObserver(Handler handler,
                                SoundtracksModel soundtracksModel,
                                AppDatabase database,
                                AudioReader audioReader,
                                Activity activity) {
        super(handler);
        this.soundtracksModel = soundtracksModel;
        this.database = database;
        this.audioReader = audioReader;

        activity.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                false, this

        );
    }

    @Override
    public void onChange(boolean selfChange) {
        System.err.println("Change*************************************************");
        super.onChange(selfChange);
        AsyncTask.execute(()-> soundtracksModel.execute(database, audioReader));
    }
}
