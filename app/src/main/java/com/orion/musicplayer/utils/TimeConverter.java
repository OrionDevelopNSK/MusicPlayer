package com.orion.musicplayer.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class TimeConverter {

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String toMinutesAndSeconds(long durationMSec) {
        int min = (int)(durationMSec / (60 * 1000));
        int sec = Math.round((durationMSec - (min * 60 * 1000)) / 1000f);
        return String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    private TimeConverter() {}
}
