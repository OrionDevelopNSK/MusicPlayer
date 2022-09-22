package com.orion.musicplayer.utils;

import com.orion.musicplayer.data.models.Song;

import java.util.Collections;
import java.util.List;

public class Sorting {
    public static List<Song> byDate(final List<Song> songList){
        Collections.reverse(songList);
        return songList;
    }

    public static List<Song> byRating(final List<Song> songList){
        songList.sort((soundtrack, t1) -> t1.getRating() - soundtrack.getRating());
        return songList;
    }

    public static List<Song> byRepeatability(final List<Song> songList){
        songList.sort((soundtrack, t1) -> t1.getCountOfLaunches() - soundtrack.getCountOfLaunches());
        return songList;
    }

    public static List<Song> byDefault(final List<Song> songList){
        return songList;
    }

    private Sorting() {}
}
