package com.orion.musicplayer.data.repositories;

import android.util.Log;

import com.orion.musicplayer.data.dao.SongDao;
import com.orion.musicplayer.data.entities.SongEntity;
import com.orion.musicplayer.data.models.Song;

import java.util.ArrayList;
import java.util.List;

public class RoomSongRepository {
    private static final String TAG = RoomSongRepository.class.getSimpleName();

    private final SongDao songDao;

    public RoomSongRepository(SongDao songDao) {
        this.songDao = songDao;
    }

    public void insertAllSongs(List<Song> songs) {

        Log.d(TAG, "Вставка данных в базу данных, если они отстутвуют");
        List<SongEntity> soundtrackDbEntities = new ArrayList<>();
        for (Song s: songs) {
            SongEntity songEntity = new SongEntity();
            songEntity.artist = s.getArtist();
            songEntity.countOfLaunches = s.getCountOfLaunches();
            songEntity.data = s.getData();
            songEntity.duration = s.getDuration();
            songEntity.rating = s.getRating();
            songEntity.title = s.getTitle();
            soundtrackDbEntities.add(songEntity);
        }
        songDao.insertAllSongs(soundtrackDbEntities);
    }

    public void deleteSoundtracks(SongEntity... songs) {
        Log.d(TAG, "Удаление данных из базы данных");
        songDao.deleteSoundtracks(songs);
    }

    public void updateSoundtrack(SongEntity... songs) {
        Log.d(TAG, "Обновление данных в базе данных");
        songDao.updateSoundtrack(songs);
    }

    public List<Song> getAll() {
        List<SongEntity> all = songDao.getAll();
        List<Song> songs = new ArrayList<>();
        for (SongEntity se: all) {
            Song song = se.toSoundtrack();
            songs.add(song);
        }
        return songs;
    }
}
