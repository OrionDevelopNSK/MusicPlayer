package com.orion.musicplayer.deprecated;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqlOpenDatabaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "sound_properties.db";
    public static final int VERSION = 1;
    public static final String TABLE_SOUNDTRACKS = "soundtracks";

    private static final String DATA = "DATA";
    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String ARTIST = "ARTIST";
    private static final String DURATION = "DURATION";
    private static final String RATING = "RATING";
    private static final String COUNT_OF_LAUNCHES = "COUNT_OF_LAUNCHES";
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLISTS_DATA = "TABLE_PLAYLISTS_DATA";
    private static final String NAME = "NAME";
    private static final String TABLE_SOUNDTRACKS_PLAYLISTS = "soundtracks_playlists";
    private static final String TABLE_SOUNDTRACKS_DATA = "TABLE_SOUNDTRACKS_DATA";




    public SqlOpenDatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTablesDatabase(sqLiteDatabase);
    }

    private void createTablesDatabase(SQLiteDatabase sqLiteDatabase) {
        createTableSoundtracks(sqLiteDatabase);
        createTablePlaylists(sqLiteDatabase);
        createTableRelationshipSoundtracksPlaylists(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private void createTableSoundtracks(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SOUNDTRACKS + " (" +
                DATA + " TEXT," +
                ID + " INTEGER, " +
                TITLE + " TEXT," +
                ARTIST + " TEXT, " +
                DURATION + " INTEGER, " +
                RATING + " INTEGER, " +
                COUNT_OF_LAUNCHES + " INTEGER, " +
                "UNIQUE "  + "(" + DATA +", " + TITLE + ") " +
                ");"
        );
    }

    private void createTablePlaylists(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_PLAYLISTS + " (" +
                ID + " INTEGER, " +
                NAME + " TEXT " +
                ");"
        );
    }

    private void createTableRelationshipSoundtracksPlaylists (SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SOUNDTRACKS_PLAYLISTS + " (" +
                ID + " INTEGER, " +
                TABLE_SOUNDTRACKS_DATA + " TEXT, " +
                TABLE_PLAYLISTS_DATA + " TEXT, " +
                "FOREIGN KEY (" + TABLE_PLAYLISTS_DATA + ") REFERENCES " + TABLE_PLAYLISTS + " (" + DATA +" )," +
                "FOREIGN KEY (" + TABLE_SOUNDTRACKS_DATA + ") REFERENCES " + TABLE_SOUNDTRACKS + " (" + DATA +" )" +
                ");"
        );
    }

}
