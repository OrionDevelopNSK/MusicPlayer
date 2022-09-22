package com.orion.musicplayer.deprecated;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.orion.musicplayer.data.database.AudioReader;
import com.orion.musicplayer.data.models.Song;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManipulator {
    private static final String DATA = "DATA";
    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String ARTIST = "ARTIST";
    private static final String DURATION = "DURATION";
    private static final String RATING = "RATING";
    private static final String COUNT_OF_LAUNCHES = "COUNT_OF_LAUNCHES";

    private final SQLiteDatabase database;
    private final AudioReader audioReader;
    private List<Song> soundtracksCustomStorage;
    private List<Song> soundtracksMediaStore;

    public DatabaseManipulator(Context context) {
        SqlOpenDatabaseHelper helper = new SqlOpenDatabaseHelper(context);
        database = helper.getWritableDatabase();
        audioReader = new AudioReader(context);
        List<Song> songs = audioReader.readMediaData();
        insert(songs);
        readDatabaseOrderBy(null);
        checkingRelevanceDatabase();
    }

    public List<Song> getSoundtracksCustomStorage() {
        return soundtracksCustomStorage;
    }

    private void insert(List<Song> songList){
        soundtracksMediaStore = songList;
        for (Song song : songList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DATA, song.getData());
            //contentValues.put(ID, soundtrack.getId());
            contentValues.put(TITLE, song.getTitle());
            contentValues.put(ARTIST, song.getArtist());
            contentValues.put(DURATION, song.getDuration());
            contentValues.put(RATING, song.getRating());
            contentValues.put(COUNT_OF_LAUNCHES, song.getCountOfLaunches());
            database.insertWithOnConflict(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void delete(Song song){
        database.delete(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS,
                DATA + "=? AND " + TITLE + "=?" ,
                new String[]{song.getData(), song.getTitle()});
    }

    public void update(Song song){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATA, song.getData());
        contentValues.put(ID, song.getId());
        contentValues.put(TITLE, song.getTitle());
        contentValues.put(RATING, song.getRating());
        contentValues.put(COUNT_OF_LAUNCHES, song.getCountOfLaunches());
        database.update(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS,
                contentValues, DATA + "=? AND" + song.getData(),
                null);
    }

    void checkingRelevanceDatabase(){
        for (Song s: soundtracksCustomStorage) {
            if (!soundtracksMediaStore.contains(s)) {
                delete(s);
                soundtracksCustomStorage.remove(s);
            }
        }
    }

    @SuppressLint("Range")
    private void readDatabaseOrderBy(String orderBy){
        soundtracksCustomStorage = new ArrayList<>();
        Cursor cursor = database.query(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS,
                null,
                null,
                null,
                null,
                null,
                orderBy);
        cursor.moveToFirst();

        final int columnIndexData = cursor.getColumnIndex(DATA);
        final int columnIndexId = cursor.getColumnIndex(ID);
        final int columnIndexTitle = cursor.getColumnIndex(TITLE);
        final int columnIndexArtist = cursor.getColumnIndex(ARTIST);
        final int columnIndexDuration = cursor.getColumnIndex(DURATION);
        final int columnIndexRating = cursor.getColumnIndex(RATING);
        final int columnIndexCountOfLaunches = cursor.getColumnIndex(COUNT_OF_LAUNCHES);

        while (cursor.moveToNext()) {
            Song song = new Song();
            song.setData(cursor.getString(columnIndexData));
            song.setId(cursor.getString(columnIndexId));
            song.setTitle(cursor.getString(columnIndexTitle));
            song.setArtist(cursor.getString(columnIndexArtist));
            song.setDuration(cursor.getInt(columnIndexDuration));
            song.setRating(cursor.getInt(columnIndexRating));
            song.setCountOfLaunches(cursor.getInt(columnIndexCountOfLaunches));
            soundtracksCustomStorage.add(song);
        }
        cursor.close();
    }






}
