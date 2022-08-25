package com.orion.musicplayer.viewmodels;

import static android.os.Looper.getMainLooper;

import android.app.Application;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.orion.musicplayer.AudioPlayerFocus;
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
    private final AudioPlayerFocus audioPlayerFocus = new AudioPlayerFocus(getApplication());
    private List<Soundtrack> countSoundtracks;
    private final Deque<Integer> directOrder;
    private final Deque<Integer> reverseOrder;
    private int currentPosition = -1;
    private StateMode stateMode = StateMode.LOOP;

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

    public SoundtrackPlayerModel(@NonNull Application application) {
        super(application);
        setSoundtrackFinishListener();
        setPlayingStatusSoundtrackListener();
        directOrder = new ArrayDeque<>();
        reverseOrder = new ArrayDeque<>();
        setAudioFocusChangeStateListener();
    }

    private void setAudioFocusChangeStateListener() {
        Log.d(TAG, "Установка слушателя состояния аудиофокуса");
        audioPlayerFocus.setOnAudioFocusChangeStateListener(eventCode -> {
            switch (eventCode) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "Фокус потерян. Запрос на долгое воспроизведение. Приостановка воспроизведения");
                    soundtrackPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "Фокус потерян. Запрос на короткое воспроизведение. Приостановка воспроизведения");
                    soundtrackPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "Фокус потерян. Запрос на короткое воспроизведение. Приглушение воспроизведения");
                    soundtrackPlayer.setVolume(.5f, .5f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "Другое приложение закончило воспроизведение. Возврат фокуса");
                    soundtrackPlayer.setVolume(1, 1);
                    break;
            }
        });
    }

    private void setPlayingStatusSoundtrackListener() {
        Log.d(TAG, "Установка слушателя начала воспроизведения");
        soundtrackPlayer.setOnPlayingStatusSoundtrackListener(isPlay -> {

            isPlayingLiveData.setValue(isPlay);

            if (isPlay) audioPlayerFocus.gainAudioFocus();
            else audioPlayerFocus.loseAudioFocus();
        });
    }

    private void setSoundtrackFinishListener() {
        Log.d(TAG, "Установка слушателя конца воспроизведения песни");
        soundtrackPlayer.setOnSoundtrackFinishedListener(() -> {
            if (stateMode == StateMode.REPEAT) {
                setCountOfLaunches(currentPosition, countSoundtracks);
                playOrPause(currentPosition, countSoundtracks);
            } else {
                setCountOfLaunches(currentPosition, countSoundtracks);
                next(currentPosition, countSoundtracks);
            }
        });
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
        if (reverseOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = reverseOrder.pop();
            playOrPause(pop, countSoundtracks);
            positionLiveData.setValue(pop);
            return;
        }

        Log.d(TAG, "Запуск следующей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position + 1 > countSoundtracks.size() - 1) ? 0 : position + 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            positionLiveData.setValue(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) directOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSoundtracks);
            positionLiveData.setValue(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void previous(int position, List<Soundtrack> countSoundtracks) {
        if (directOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = directOrder.pop();
            playOrPause(pop, countSoundtracks);
            positionLiveData.setValue(pop);
            return;
        }

        Log.d(TAG, "Запуск предыдущей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position - 1 < 0) ? countSoundtracks.size() - 1 : position - 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            positionLiveData.setValue(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) reverseOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSoundtracks);
            positionLiveData.setValue(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void switchMode() {
        switch (stateMode) {
            case LOOP:
                stateMode = StateMode.RANDOM;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        audioPlayerFocus.loseAudioFocus();
    }
}
