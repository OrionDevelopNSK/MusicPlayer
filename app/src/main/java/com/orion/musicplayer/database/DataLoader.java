package com.orion.musicplayer.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;
import com.orion.musicplayer.utils.Sorting;
import com.orion.musicplayer.utils.SortingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public interface OnDatabaseLoadListener {
        void onDatabaseLoad(List<Soundtrack> soundtracks);
    }


    public interface OnDatabaseChangeListener {
        void onDatabaseChange(List<Soundtrack> soundtracks);
    }


    private static final String TAG = DataLoader.class.getSimpleName();

    private AppDatabase database;
    private final Application application;
    private OnDatabaseLoadListener onDatabaseLoadListener;
    private OnDatabaseChangeListener onDatabaseChangeListener;
    private List<Soundtrack> soundtrackListSorted;
    private List<Soundtrack> soundtracksCashed;

    public DataLoader(Application application) {
        this.application = application;
    }

    public List<Soundtrack> getSoundtracksCashed() {
        return soundtrackListSorted;
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

        AsyncTask.execute(() -> {
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            List<Soundtrack> soundtracks = audioReader.readMediaData();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.insertAllSoundtracks(soundtracks);
            Log.d(TAG, "Получение всех сущностей из базы данных");
            List<SoundtrackDbEntity> all = soundtrackDao.getAll();
            deleteNotValidDataFromDatabase(roomSoundtrackRepository, all);
            //чтобы изначально была сортировка: последние добавленные песни сначала
            soundtracksCashed = Sorting.byDate(roomSoundtrackRepository.getAll());
            soundtrackListSorted = (sortingType == SortingType.DATE) ? Sorting.byDefault(soundtracksCashed) : sort(soundtracksCashed, sortingType);
            onDatabaseLoadListener.onDatabaseLoad(soundtrackListSorted);
            onDatabaseChangeListener.onDatabaseChange(soundtrackListSorted);
        });
    }

    public void refresh(SortingType sortingType){
        if (soundtracksCashed == null) return;
        soundtrackListSorted = sort(soundtracksCashed, sortingType);
        onDatabaseLoadListener.onDatabaseLoad(soundtrackListSorted);
        onDatabaseChangeListener.onDatabaseChange(soundtrackListSorted);
    }

    private List<Soundtrack> sort(final List<Soundtrack> soundtracks, SortingType sortingType){
        List<Soundtrack> soundtracksBySort = new ArrayList<>(soundtracks);
        switch (sortingType) {
            case REPEATABILITY:
                return Sorting.byRepeatability(soundtracksBySort);
            case RATING:
                return Sorting.byRating(soundtracksBySort);
            default:
                return Sorting.byDefault(soundtracksBySort);
        }
    }

    private void deleteNotValidDataFromDatabase(RoomSoundtrackRepository roomSoundtrackRepository, List<SoundtrackDbEntity> all) {
        Log.d(TAG, "Удаление отсутствующих песен из базы данных");
        for (SoundtrackDbEntity soundtrackDbEntity : all) {
            File f = new File(soundtrackDbEntity.data);
            if (!f.exists()) {
                Log.d(TAG, String.format("%s отсутствует в базе данных", soundtrackDbEntity.data));
                roomSoundtrackRepository.deleteSoundtracks(soundtrackDbEntity);
            }
        }
    }
}
