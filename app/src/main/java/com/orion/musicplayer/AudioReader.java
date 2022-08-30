package com.orion.musicplayer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.ArrayList;
import java.util.List;

public class AudioReader {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private final Context context;

    public AudioReader(Context context) {
        this.context = context;
    }

    @SuppressLint("Range")
    public List<Soundtrack> readMediaData() {
        Log.d(TAG, "Чтение аудиофайла с внутреннего хранилища");

        String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Log.d(TAG, "Создание курсора аудиофайлов");
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        Cursor cursorAudio = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media.DATA + " like ? OR " + MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%mp3", "%wav"},
                null);
        cursorAudio.moveToFirst();

        Log.d(TAG, "Получение индексов столбцов по имени");
        int columnIndexTitle = cursorAudio.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int columnIndexArtist = cursorAudio.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int columnIndexDuration = cursorAudio.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int columnIndexData = cursorAudio.getColumnIndex(MediaStore.Audio.Media.DATA);

        List<Soundtrack> soundtracks = new ArrayList<>();
        Log.d(TAG, "Чтение данных из курсора, добавление в коллекцию");
        while (cursorAudio.moveToNext()) {
            Soundtrack soundtrack = new Soundtrack();
            soundtrack.setTitle(cursorAudio.getString(columnIndexTitle));
            soundtrack.setArtist(cursorAudio.getString(columnIndexArtist));
            soundtrack.setDuration(cursorAudio.getInt(columnIndexDuration));
            soundtrack.setData(cursorAudio.getString(columnIndexData));
            soundtracks.add(soundtrack);
        }

        cursorAudio.close();
        Log.d(TAG, String.format("Размер коллекции песен: %d", soundtracks.size()));
        return soundtracks;
    }
}
