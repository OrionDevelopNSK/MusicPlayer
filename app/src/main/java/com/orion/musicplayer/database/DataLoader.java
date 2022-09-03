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
import java.util.List;

public class DataLoader {

    public interface OnDatabaseLoadListener {
        void onDatabaseLoad(List<Soundtrack> soundtracks);
    }

    public interface OnDatabaseLoadCompleteListener {
        void onDatabaseLoadComplete();
    }


    public interface OnDatabaseChangeListener {
        void onDatabaseChange(List<Soundtrack> soundtracks);
    }


    private static final String TAG = DataLoader.class.getSimpleName();

    private AppDatabase database;
    private final Application application;
    private OnDatabaseLoadListener onDatabaseLoadListener;
    private OnDatabaseLoadCompleteListener onDatabaseLoadCompleteListener;
    private OnDatabaseChangeListener onDatabaseChangeListener;
    private List<Soundtrack> soundtrackListCashed;

    public DataLoader(Application application) {
        this.application = application;
    }

    public List<Soundtrack> getSoundtracksCashed() {
        return soundtrackListCashed;
    }

    public void setOnDatabaseLoadListener(OnDatabaseLoadListener onDatabaseLoadListener) {
        this.onDatabaseLoadListener = onDatabaseLoadListener;
    }

    public void setOnDatabaseLoadCompleteListener(OnDatabaseLoadCompleteListener onDatabaseLoadCompleteListener) {
        this.onDatabaseLoadCompleteListener = onDatabaseLoadCompleteListener;
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
            soundtrackListCashed = sort(roomSoundtrackRepository.getAll(), sortingType);
            onDatabaseLoadListener.onDatabaseLoad(soundtrackListCashed);
            onDatabaseLoadCompleteListener.onDatabaseLoadComplete();
            onDatabaseChangeListener.onDatabaseChange(soundtrackListCashed);
        });
    }

    public void refresh(SortingType sortingType){
        if (soundtrackListCashed == null) return;
        List<Soundtrack> sorted = sort(soundtrackListCashed, sortingType);
        onDatabaseLoadListener.onDatabaseLoad(sorted);
        onDatabaseLoadCompleteListener.onDatabaseLoadComplete();
        onDatabaseChangeListener.onDatabaseChange(sorted);
    }

    private List<Soundtrack> sort(final List<Soundtrack> soundtracks, SortingType sortingType){
        switch (sortingType) {
            case REPEATABILITY:
                return Sorting.byRepeatability(soundtracks);
            case RATING:
                return Sorting.byRating(soundtracks);
            default:
                return Sorting.byDate(soundtracks);
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
