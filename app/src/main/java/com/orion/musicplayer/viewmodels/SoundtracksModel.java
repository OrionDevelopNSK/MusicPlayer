package com.orion.musicplayer.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;

import java.io.File;
import java.util.List;

public class SoundtracksModel extends AndroidViewModel {
    private final MutableLiveData<List<Soundtrack>> soundtracksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> position = new MutableLiveData<>();


    public SoundtracksModel(@NonNull Application application) {
        super(application);
        execute();
    }

    public void execute() {
        AudioReader audioReader = new AudioReader(getApplication());
        AppDatabase database = Room.databaseBuilder(getApplication(),
                        AppDatabase.class,
                        "database")
                .build();

        AsyncTask.execute(() -> {
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            List<Soundtrack> soundtracks = audioReader.readMediaData();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.insertAllSoundtracks(soundtracks);
            List<SoundtrackDbEntity> all = soundtrackDao.getAll();

            for (SoundtrackDbEntity soundtrackDbEntity : all) {
                File f = new File(soundtrackDbEntity.data);
                if (!f.exists()) roomSoundtrackRepository.deleteSoundtracks(soundtrackDbEntity);
            }
            soundtracksLiveData.postValue(roomSoundtrackRepository.getAll());
        });
    }

    public LiveData<List<Soundtrack>> getSoundtracks() {
        return soundtracksLiveData;
    }

    public MutableLiveData<Integer> getPosition() {
        return position;
    }
}
