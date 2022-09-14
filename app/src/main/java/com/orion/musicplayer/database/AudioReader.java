package com.orion.musicplayer.database;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.orion.musicplayer.models.Song;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.ArrayList;
import java.util.List;

public class AudioReader {
    private static final String TAG = DataModel.class.getSimpleName();

    private final Context context;

    public AudioReader(Context context) {
        this.context = context;
    }

    @SuppressLint("Range")
    public List<Song> readMediaData() {
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

        List<Song> songs = new ArrayList<>();
        Log.d(TAG, "Чтение данных из курсора, добавление в коллекцию");
        while (cursorAudio.moveToNext()) {
            Song song = new Song();
            song.setId(cursorAudio.getString(columnIndexData));
            song.setTitle(cursorAudio.getString(columnIndexTitle));
            song.setArtist(cursorAudio.getString(columnIndexArtist));
            song.setDuration(cursorAudio.getInt(columnIndexDuration));
            song.setData(cursorAudio.getString(columnIndexData));
            songs.add(song);
        }

        cursorAudio.close();
        Log.d(TAG, String.format("Размер коллекции песен: %d", songs.size()));
        return songs;
    }
}
