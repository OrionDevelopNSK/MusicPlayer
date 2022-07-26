package com.orion.musicplayer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    private final Context context;
    private SQLiteDatabase database;

    public DatabaseManipulator(Context context) {
        this.context = context;
    }

    private void initDatabase(){
        SqlOpenDatabaseHelper helper = new SqlOpenDatabaseHelper(context);
        database = helper.getWritableDatabase();
    }

    private void insert(Soundtrack soundtrack){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATA, soundtrack.getData());
        contentValues.put(ID, soundtrack.getData());
        contentValues.put(TITLE, soundtrack.getData());
        contentValues.put(ARTIST, soundtrack.getData());
        contentValues.put(DURATION, soundtrack.getData());
        contentValues.put(RATING, soundtrack.getData());
        contentValues.put(COUNT_OF_LAUNCHES, soundtrack.getData());
        contentValues.put(IS_ALIVE, soundtrack.getData());

        database.insert(SqlOpenDatabaseHelper.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    private List<Soundtrack> readDatabaseOrderBy(String orderBy){
        List<Soundtrack> soundtracks = new ArrayList<>();
        Cursor cursor = database.query(SqlOpenDatabaseHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                orderBy);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            Soundtrack soundtrack = new Soundtrack();
            soundtrack.setData(cursor.getString(cursor.getColumnIndex(DATA)));
            soundtrack.setId(cursor.getString(cursor.getColumnIndex(ID)));
            soundtrack.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
            soundtrack.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
            soundtrack.setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)));
            soundtrack.setRating(cursor.getInt(cursor.getColumnIndex(RATING)));
            soundtrack.setCountOfLaunches(cursor.getInt(cursor.getColumnIndex(COUNT_OF_LAUNCHES)));
            soundtrack.setAlive(cursor.getInt(cursor.getColumnIndex(IS_ALIVE)));
            soundtracks.add(soundtrack);
        }
        cursor.close();
        return soundtracks;
    }


}
