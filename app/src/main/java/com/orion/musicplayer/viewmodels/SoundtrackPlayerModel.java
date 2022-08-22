package com.orion.musicplayer.viewmodels;

import static android.os.Looper.getMainLooper;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.orion.musicplayer.SoundtrackPlayer;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;
import com.orion.musicplayer.utils.StateMode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class SoundtrackPlayerModel extends AndroidViewModel {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private final MutableLiveData<StateMode> stateModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentDurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();

    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();
    private List<Soundtrack> countSoundtracks;
    private Deque<Integer> order;
    private int currentPosition;
    private StateMode stateMode = StateMode.LOOP;
    private boolean isPlaying;

    public SoundtrackPlayerModel(@NonNull Application application) {
        super(application);

        soundtrackPlayer.setOnSoundtrackFinishedListener(() -> {
            int position = currentPosition;
            if (stateMode == StateMode.REPEAT) {
                positionLiveData.setValue(currentPosition);
                setCountOfLaunches(position, countSoundtracks);
                playOrPause(currentPosition, countSoundtracks);
                return;
            }
            int i = (currentPosition + 1 > countSoundtracks.size() - 1) ? 0 : currentPosition + 1;
            positionLiveData.setValue(i);
            setCountOfLaunches(position, countSoundtracks);
            playOrPause(i, countSoundtracks);

        });
    }

    public MutableLiveData<StateMode> getStateModeLiveData() {
        return stateModeLiveData;
    }

    public MutableLiveData<Boolean> getIsPlayingLiveData() {
        return isPlayingLiveData;
    }

    public MutableLiveData<Integer> getCurrentDurationLiveData() {
        return currentDurationLiveData;
    }

    public MutableLiveData<Integer> getPositionLiveData() {
        return positionLiveData;
    }

    public void playOrPause(int position, List<Soundtrack> countSoundtracks) {
        Log.d(TAG, "Начало или пауза песни");
        this.countSoundtracks = countSoundtracks;
        this.currentPosition = position;
        soundtrackPlayer.playOrPause(countSoundtracks.get(position));
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    Handler handler = new Handler(getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currentDurationLiveData.setValue((int) soundtrackPlayer.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    public void next(int position, List<Soundtrack> countSoundtracks) {
        this.countSoundtracks = countSoundtracks;
        this.currentPosition = position;
        Log.d(TAG, "Запуск следующей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position + 1 > countSoundtracks.size() - 1) ? 0 : position + 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            positionLiveData.setValue(i);
        } else if (stateMode == StateMode.RANDOM) {
            int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSoundtracks);
            positionLiveData.setValue(randomPosition);
            order.push(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void previous(int position, List<Soundtrack> countSoundtracks) {
        this.countSoundtracks = countSoundtracks;
        this.currentPosition = position;
        Log.d(TAG, "Запуск предыдущей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position - 1 < 0) ? countSoundtracks.size() - 1 : position - 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            positionLiveData.setValue(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (order.peek() != null) {
                Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", order.size()));
                int pop = order.pop();
                playOrPause(pop, countSoundtracks);
                positionLiveData.setValue(pop);
            } else {
                int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
                Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
                playOrPause(randomPosition, countSoundtracks);
                positionLiveData.setValue(randomPosition);
                order.push(randomPosition);
            }
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void switchMode() {
        switch (stateMode) {
            case LOOP:
                stateMode = StateMode.RANDOM;
                order = new ArrayDeque<>();
                break;
            case RANDOM:
                stateMode = StateMode.REPEAT;
                break;
            case REPEAT:
                stateMode = StateMode.LOOP;
                break;
        }
        Log.d(TAG, "Переключение режима воспроизведения, текущее : " + stateMode.name());
        stateModeLiveData.setValue(stateMode);
    }

    public void setCurrentDuration(int position) {
        soundtrackPlayer.setCurrentDuration(position);
    }

    public void setRating(int position, List<Soundtrack> countSoundtracks, int rating) {
        AppDatabase database = AppDatabase.getDatabase(getApplication());

        AsyncTask.execute(() -> {
            Log.d(TAG, "Запись в базу данных оценки песни");
            Soundtrack soundtrack = countSoundtracks.get(position);
            soundtrack.setRating(rating);
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.updateSoundtrack(soundtrack.toSoundtrackDbEntity());
            Log.d(TAG, String.format("Рейтинг песни под номером :%d равен %d", position, rating));
        });

    }

    private void setCountOfLaunches(int position, List<Soundtrack> countSoundtracks) {
        AppDatabase database = AppDatabase.getDatabase(getApplication());

        AsyncTask.execute(() -> {
            Log.d(TAG, "Запись в базу данных количества раз прослушивания");
            Soundtrack soundtrack = countSoundtracks.get(position);
            int countOfLaunchesOld = soundtrack.getCountOfLaunches();
            soundtrack.setCountOfLaunches(countOfLaunchesOld + 1);
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.updateSoundtrack(soundtrack.toSoundtrackDbEntity());
            Log.d(TAG, String.format("Количество прослушивания песни под номером :%d равно %d", position, countOfLaunchesOld + 1));
        });
    }


}
