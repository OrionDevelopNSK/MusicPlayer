package com.orion.musicplayer.deprecated;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.models.Soundtrack;

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
            database.insertWithOnConflict(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void delete(Soundtrack soundtrack){
        database.delete(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS,
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
        database.update(SqlOpenDatabaseHelper.TABLE_SOUNDTRACKS,
                contentValues, DATA + "=? AND" + soundtrack.getData(),
                null);
    }

    void checkingRelevanceDatabase(){
        for (Soundtrack s: soundtracksCustomStorage) {
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
            Soundtrack soundtrack = new Soundtrack();
            soundtrack.setData(cursor.getString(columnIndexData));
            soundtrack.setId(cursor.getLong(columnIndexId));
            soundtrack.setTitle(cursor.getString(columnIndexTitle));
            soundtrack.setArtist(cursor.getString(columnIndexArtist));
            soundtrack.setDuration(cursor.getInt(columnIndexDuration));
            soundtrack.setRating(cursor.getInt(columnIndexRating));
            soundtrack.setCountOfLaunches(cursor.getInt(columnIndexCountOfLaunches));
            soundtracksCustomStorage.add(soundtrack);
        }
        cursor.close();
    }






}
