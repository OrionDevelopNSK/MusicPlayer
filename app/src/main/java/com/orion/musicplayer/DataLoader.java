package com.orion.musicplayer;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;

import java.io.File;
import java.util.List;

public class DataLoader {

    interface OnDatabaseLoadListener {
        void onDatabaseLoad(List<Soundtrack> soundtracks);
    }

    interface OnDatabaseLoadCompleteListener {
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
    private List<Soundtrack> soundtracksFromRepo;

    public DataLoader(Application application) {
        this.application = application;
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

    public void execute() {
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
            soundtracksFromRepo = roomSoundtrackRepository.getAll();
            onDatabaseLoadListener.onDatabaseLoad(soundtracksFromRepo);
            onDatabaseLoadCompleteListener.onDatabaseLoadComplete();
            onDatabaseChangeListener.onDatabaseChange(soundtracksFromRepo);
        });
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

    public List<Soundtrack> getSoundtracksFromRepo() {
        return soundtracksFromRepo;
    }
}
