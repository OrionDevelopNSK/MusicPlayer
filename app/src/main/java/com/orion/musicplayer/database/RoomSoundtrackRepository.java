package com.orion.musicplayer.database;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;

public class RoomSoundtrackRepository implements SoundtrackDao{

    private SoundtrackDao soundtrackDao;

    public RoomSoundtrackRepository(SoundtrackDao soundtrackDao) {
        this.soundtrackDao = soundtrackDao;
    }

    public void insertAllSoundtracks(Soundtrack... soundtracks) {
        soundtrackDao.insertAllSoundtracks(soundtracks);
    }

    public void deleteSoundtracks(Soundtrack... soundtracks) {
        soundtrackDao.deleteSoundtracks(soundtracks);
    }

    public void updateSoundtrack(Soundtrack... soundtracks) {
        soundtrackDao.updateSoundtrack(soundtracks);
    }

    public List<Soundtrack> getAll() {
        return soundtrackDao.getAll();
    }
}
