package com.orion.musicplayer.viewmodels;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;

import java.io.File;
import java.util.List;

public class SoundtracksModel extends AndroidViewModel {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private final MutableLiveData<List<Soundtrack>> soundtracksLiveData = new MutableLiveData<>();
    private AppDatabase database;

    public SoundtracksModel(@NonNull Application application) {
        super(application);
        execute();
    }

    public LiveData<List<Soundtrack>> getSoundtracks() {
        return soundtracksLiveData;
    }

    public void execute() {
        Log.d(TAG, "Манипуляции с базой данных");
        AudioReader audioReader = new AudioReader(getApplication());
        database = AppDatabase.getDatabase(getApplication());

        AsyncTask.execute(() -> {
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            List<Soundtrack> soundtracks = audioReader.readMediaData();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.insertAllSoundtracks(soundtracks);
            Log.d(TAG, "Получение всех сущностей из базы данных");
            List<SoundtrackDbEntity> all = soundtrackDao.getAll();
            deleteNotValidDataFromDatabase(roomSoundtrackRepository, all);
            soundtracksLiveData.postValue(roomSoundtrackRepository.getAll());
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

    @Override
    protected void onCleared() {
        super.onCleared();
        database.close();
    }
}
