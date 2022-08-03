package com.orion.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final String IS_ALIVE = "IS_ALIVE";

    private final SQLiteDatabase database;
    private final AudioReader audioReader;
    private List<Soundtrack> soundtracksCustomStorage;
    private List<Soundtrack> soundtracksMediaStore;

    public DatabaseManipulator(Context context) {
        SqlOpenDatabaseHelper helper = new SqlOpenDatabaseHelper(context);
        database = helper.getWritableDatabase();
        audioReader = new AudioReader(context);
        List<Soundtrack> soundtracks = audioReader.readMediaData();
        insert(soundtracks);
        readDatabaseOrderBy(null);
        checkingRelevanceDatabase();
    }

    public List<Soundtrack> getSoundtracksCustomStorage() {
        return soundtracksCustomStorage;
    }

    private void insert(List<Soundtrack> soundtrackList){
        soundtracksMediaStore = soundtrackList;
        for (Soundtrack soundtrack : soundtrackList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DATA, soundtrack.getData());
            //contentValues.put(ID, soundtrack.getId());
            contentValues.put(TITLE, soundtrack.getTitle());
            contentValues.put(ARTIST, soundtrack.getArtist());
            contentValues.put(DURATION, soundtrack.getDuration());
            contentValues.put(RATING, soundtrack.getRating());
            contentValues.put(COUNT_OF_LAUNCHES, soundtrack.getCountOfLaunches());
            contentValues.put(IS_ALIVE, soundtrack.isAlive());
            database.insertWithOnConflict(SqlOpenDatabaseHelper.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void delete(Soundtrack soundtrack){
        database.delete(SqlOpenDatabaseHelper.TABLE_NAME,
                DATA + "=? AND " + TITLE + "=?" ,
                new String[]{soundtrack.getData(), soundtrack.getTitle()});
    }

    public void update(Soundtrack soundtrack){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATA, soundtrack.getData());
        contentValues.put(ID, soundtrack.getId());
        contentValues.put(TITLE, soundtrack.getTitle());
        contentValues.put(RATING, soundtrack.getRating());
        contentValues.put(COUNT_OF_LAUNCHES, soundtrack.getCountOfLaunches());
        contentValues.put(IS_ALIVE, soundtrack.isAlive());
        database.update(SqlOpenDatabaseHelper.TABLE_NAME,
                contentValues, DATA + "=? AND" + soundtrack.getData(),
                null);
    }

    void checkingRelevanceDatabase(){
        for (Soundtrack s: soundtracksCustomStorage) {
            if (!soundtracksMediaStore.contains(s)) {
                System.out.println("Not found ****************************************************************************** " + s.getData());
                delete(s);
                soundtracksCustomStorage.remove(s);
            }
        }

    }

    @SuppressLint("Range")
    private void readDatabaseOrderBy(String orderBy){
        soundtracksCustomStorage = new ArrayList<>();
        Cursor cursor = database.query(SqlOpenDatabaseHelper.TABLE_NAME,
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
        final int columnIndexIsAlive = cursor.getColumnIndex(IS_ALIVE);

        while (cursor.moveToNext()) {
            Soundtrack soundtrack = new Soundtrack();
            soundtrack.setData(cursor.getString(columnIndexData));
            soundtrack.setId(cursor.getString(columnIndexId));
            soundtrack.setTitle(cursor.getString(columnIndexTitle));
            soundtrack.setArtist(cursor.getString(columnIndexArtist));
            soundtrack.setDuration(cursor.getInt(columnIndexDuration));
            soundtrack.setRating(cursor.getInt(columnIndexRating));
            soundtrack.setCountOfLaunches(cursor.getInt(columnIndexCountOfLaunches));
            soundtrack.setAlive(cursor.getInt(columnIndexIsAlive));
            soundtracksCustomStorage.add(soundtrack);
        }
        cursor.close();
    }






}
