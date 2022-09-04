package com.orion.musicplayer.utils;

import com.orion.musicplayer.models.Soundtrack;

import java.util.Collections;
import java.util.List;

public class Sorting {
    public static List<Soundtrack> byDate(final List<Soundtrack> soundtrackList){
        Collections.reverse(soundtrackList);
        return soundtrackList;
    }

    public static List<Soundtrack> byRating(final List<Soundtrack> soundtrackList){
        Collections.sort(soundtrackList, (soundtrack, t1) -> t1.getRating() - soundtrack.getRating());
        return soundtrackList;
    }

    public static List<Soundtrack> byRepeatability(final List<Soundtrack> soundtrackList){
        Collections.sort(soundtrackList, (soundtrack, t1) -> t1.getCountOfLaunches() - soundtrack.getCountOfLaunches());
        return soundtrackList;
    }

    public static List<Soundtrack> byDefault(final List<Soundtrack> soundtrackList){
        return soundtrackList;
    }

}
