package com.orion.musicplayer.utils;

import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.viewmodels.SoundtracksModel;

public class MediaScannerObserver extends ContentObserver {

    private final FragmentActivity fragmentActivity;

    public MediaScannerObserver(Handler handler, FragmentActivity fragmentActivity) {
        super(handler);
        this.fragmentActivity = fragmentActivity;

        fragmentActivity.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, this
        );
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        SoundtracksModel soundtracksModel = new ViewModelProvider(fragmentActivity).get(SoundtracksModel.class);
        AsyncTask.execute(()-> soundtracksModel.execute());
    }


}
