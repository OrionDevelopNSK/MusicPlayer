package com.orion.musicplayer.data.database;

import android.app.Application;
import android.util.Log;

import com.orion.musicplayer.data.dao.SongDao;
import com.orion.musicplayer.data.entities.SongEntity;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.data.repositories.RoomSongRepository;
import com.orion.musicplayer.utils.Sorting;
import com.orion.musicplayer.utils.SortingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataLoader {
    public interface OnDatabaseLoadListener {
        void onDatabaseLoad(List<Song> songs);
    }


    public interface OnDatabaseChangeListener {
        void onDatabaseChange(List<Song> songs);
    }


    private static final String TAG = DataLoader.class.getSimpleName();

    private final Application application;
    private AppDatabase database;
    private OnDatabaseLoadListener onDatabaseLoadListener;
    private OnDatabaseChangeListener onDatabaseChangeListener;
    private List<Song> songListSorted;
    private List<Song> songsCashed;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DataLoader(Application application) {
        this.application = application;
    }

    public List<Song> getSongsCashed() {
        return songListSorted;
    }

    public void setOnDatabaseLoadListener(OnDatabaseLoadListener onDatabaseLoadListener) {
        this.onDatabaseLoadListener = onDatabaseLoadListener;
    }

    public void setOnDatabaseChangeListener(OnDatabaseChangeListener onDatabaseChangeListener) {
        this.onDatabaseChangeListener = onDatabaseChangeListener;
    }

    public void execute(SortingType sortingType) {
        Log.d(TAG, "Манипуляции с базой данных");
        AudioReader audioReader = new AudioReader(application);
        database = AppDatabase.getDatabase(application);
        executor.execute(() -> {
            SongDao songDao = database.soundtrackDao();
            List<Song> songs = audioReader.readMediaData();
            //Защита от NPE при отсутствии песен
            if (songs.isEmpty()) return;
            RoomSongRepository roomSongRepository = new RoomSongRepository(songDao);
            roomSongRepository.insertAllSongs(songs);
            Log.d(TAG, "Получение всех сущностей из базы данных");
            List<SongEntity> all = songDao.getAll();
            deleteNotValidDataFromDatabase(roomSongRepository, all);
            //чтобы изначально была сортировка: последние добавленные песни сначала
            songsCashed = Sorting.byDate(roomSongRepository.getAll());
            songListSorted = (sortingType == SortingType.DATE) ? Sorting.byDefault(songsCashed) : sort(songsCashed, sortingType);
            onDatabaseLoadListener.onDatabaseLoad(songListSorted);
            onDatabaseChangeListener.onDatabaseChange(songListSorted);
        });
    }

    public void refresh(SortingType sortingType){
        if (songsCashed == null) return;
        songListSorted = sort(songsCashed, sortingType);
        onDatabaseLoadListener.onDatabaseLoad(songListSorted);
        onDatabaseChangeListener.onDatabaseChange(songListSorted);
    }

    private List<Song> sort(final List<Song> songs, SortingType sortingType){
        List<Song> soundtracksBySort = new ArrayList<>(songs);
        switch (sortingType) {
            case REPEATABILITY:
                return Sorting.byRepeatability(soundtracksBySort);
            case RATING:
                return Sorting.byRating(soundtracksBySort);
            default:
                return Sorting.byDefault(soundtracksBySort);
        }
    }

    private void deleteNotValidDataFromDatabase(RoomSongRepository roomSongRepository, List<SongEntity> all) {
        Log.d(TAG, "Удаление отсутствующих песен из базы данных");
        for (SongEntity songEntity : all) {
            File f = new File(songEntity.data);
            if (!f.exists()) {
                Log.d(TAG, String.format("%s отсутствует в базе данных", songEntity.data));
                roomSongRepository.deleteSoundtracks(songEntity);
            }
        }
    }
}
