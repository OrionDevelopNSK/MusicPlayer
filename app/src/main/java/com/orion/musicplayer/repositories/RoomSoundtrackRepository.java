package com.orion.musicplayer.repositories;

import android.util.Log;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;

import java.util.ArrayList;
import java.util.List;

public class RoomSoundtrackRepository{
    private static final String TAG = RoomSoundtrackRepository.class.getSimpleName();

    private final SoundtrackDao soundtrackDao;

    public RoomSoundtrackRepository(SoundtrackDao soundtrackDao) {
        this.soundtrackDao = soundtrackDao;
    }

    public void insertAllSoundtracks(List<Soundtrack> soundtracks) {

        Log.d(TAG, "Вставка данных в базу данных, если они отстутвуют");
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
        Log.d(TAG, "Удаление данных из базы данных");
        soundtrackDao.deleteSoundtracks(soundtracks);
    }

    public void updateSoundtrack(SoundtrackDbEntity... soundtracks) {
        Log.d(TAG, "Обновление данных в базе данных");
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
