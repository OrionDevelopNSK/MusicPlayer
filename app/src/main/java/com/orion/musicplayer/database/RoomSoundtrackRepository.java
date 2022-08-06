package com.orion.musicplayer.database;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;

import java.util.ArrayList;
import java.util.List;

public class RoomSoundtrackRepository{

    private SoundtrackDao soundtrackDao;

    public RoomSoundtrackRepository(SoundtrackDao soundtrackDao) {
        this.soundtrackDao = soundtrackDao;
    }

    public void insertAllSoundtracks(List<Soundtrack> soundtracks) {

        List<SoundtrackDbEntity> soundtrackDbEntities = new ArrayList<>();
        for (Soundtrack s: soundtracks) {
            SoundtrackDbEntity soundtrackDbEntity = new SoundtrackDbEntity();
            soundtrackDbEntity.artist = s.getArtist();
            soundtrackDbEntity.countOfLaunches = s.getCountOfLaunches();
            soundtrackDbEntity.data = s.getData();
            soundtrackDbEntity.duration = s.getDuration();
            soundtrackDbEntity.rating = s.getRating();
            soundtrackDbEntity.title = s.getTitle();
            soundtrackDbEntities.add(soundtrackDbEntity);
        }

        soundtrackDao.insertAllSoundtracks(soundtrackDbEntities);
    }

    public void deleteSoundtracks(SoundtrackDbEntity... soundtracks) {
        soundtrackDao.deleteSoundtracks(soundtracks);
    }

    public void updateSoundtrack(SoundtrackDbEntity... soundtracks) {
        soundtrackDao.updateSoundtrack(soundtracks);
    }

    public List<Soundtrack> getAll() {
        List<SoundtrackDbEntity> all = soundtrackDao.getAll();
        List<Soundtrack> soundtracks = new ArrayList<>();
        for (SoundtrackDbEntity se: all) {
            Soundtrack soundtrack = se.toSoundtrack();
            soundtracks.add(soundtrack);

        }
        return soundtracks;

    }
}
