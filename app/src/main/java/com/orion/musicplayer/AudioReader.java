package com.orion.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AudioReader {

    @SuppressLint("Range")
    public List<String> getMediaData(Context context) {
        String[] projection = new String[]{
                //MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        ArrayList<String> audio = new ArrayList<>();

        Cursor cursorAudio = context.getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media.DATA + " like ? OR " + MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%mp3", "%wav"},
                null);

        cursorAudio.moveToFirst();

        while (cursorAudio.moveToNext()) {
            StringBuilder sound = new StringBuilder();
            for (String s : projection) {

                if (s.equals(MediaStore.Audio.Media.DURATION)){
                    int durationMSec = cursorAudio.getInt(cursorAudio.getColumnIndex(s));
                    String dur = formattedTime(durationMSec);
                    sound.append(dur);
                }else {
                    sound.append(cursorAudio.getString(cursorAudio.getColumnIndex(s)));
                }
                sound.append("\n");
            }
            audio.add(sound.toString());
        }

        cursorAudio.close();
        return audio;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    private String formattedTime(int durationMSec) {
        int min = durationMSec / (60 * 1000);
        int sec = Math.round((durationMSec - (min * 60 * 1000)) / 1000f);
        return String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    @SuppressLint("Range")
    public List<Soundtrack> getMediaData2(Context context) {
        String[] projection = new String[]{
                //MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursorAudio = context.getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media.DATA + " like ? OR " + MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%mp3", "%wav"},
                null);

        cursorAudio.moveToFirst();

        List<Soundtrack> soundtracks = new ArrayList<>();

        while (cursorAudio.moveToNext()) {
            Soundtrack soundtrack = new Soundtrack();
            soundtrack.setTitle(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            soundtrack.setArtist(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

            soundtrack.setDuration(cursorAudio.getInt(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            soundtrack.setData(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DATA)));
            soundtracks.add(soundtrack);
        }

        cursorAudio.close();
        return soundtracks;
    }
}
