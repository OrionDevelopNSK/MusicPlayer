package com.orion.musicplayer.data.database;

import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.orion.musicplayer.data.models.Song;

import java.util.ArrayList;
import java.util.List;

public class AudioReader {
    private static final String TAG = AudioReader.class.getSimpleName();

    private final Context context;

    public AudioReader(Context context) {
        this.context = context;
    }

    @SuppressLint("Range")
    public List<Song> readMediaData() {
        Log.d(TAG, "Чтение аудиофайла с внутреннего хранилища");
        String[] projection = new String[]{
                MediaColumns.TITLE,
                MediaColumns.ARTIST,
                MediaColumns.DURATION,
                MediaColumns.DATA
        };

        Log.d(TAG, "Создание курсора аудиофайлов");
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        Cursor cursorAudio = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                projection,
                MediaColumns.DATA + " like ? OR " + MediaColumns.DATA + " like ? ",
                new String[]{"%mp3", "%wav"},
                null);
        cursorAudio.moveToFirst();

        Log.d(TAG, "Получение индексов столбцов по имени");
        int columnIndexTitle = cursorAudio.getColumnIndex(MediaColumns.TITLE);
        int columnIndexArtist = cursorAudio.getColumnIndex(MediaColumns.ARTIST);
        int columnIndexDuration = cursorAudio.getColumnIndex(MediaColumns.DURATION);
        int columnIndexData = cursorAudio.getColumnIndex(MediaColumns.DATA);

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
