package com.orion.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqlOpenDatabaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "sound_properties.sqlite";
    public static final int VERSION = 1;
    public static final String TABLE_NAME = "sound_properties";

    private static final String DATA = "DATA";
    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String ARTIST = "ARTIST";
    private static final String DURATION = "DURATION";
    private static final String RATING = "RATING";
    private static final String COUNT_OF_LAUNCHES = "COUNT_OF_LAUNCHES";
    private static final String IS_ALIVE = "IS_ALIVE";

    public SqlOpenDatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createDatabase(sqLiteDatabase);
    }

    private void createDatabase(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                DATA + " TEXT, " +
                ID + " TEXT, " +
                TITLE + " TEXT, " +
                ARTIST + " TEXT, " +
                DURATION + " INTEGER, " +
                RATING + " INTEGER, " +
                COUNT_OF_LAUNCHES + " INTEGER, " +
                IS_ALIVE + " INTEGER, " +
                "UNIQUE (" + DATA + ", " + TITLE + ")" +
                ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
