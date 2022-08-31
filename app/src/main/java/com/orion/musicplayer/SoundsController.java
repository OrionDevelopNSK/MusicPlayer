package com.orion.musicplayer;

import static android.os.Looper.getMainLooper;

import android.app.Application;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.repositories.RoomSoundtrackRepository;
import com.orion.musicplayer.utils.AudioPlayerFocus;
import com.orion.musicplayer.utils.StateMode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;


public class SoundsController {

    interface OnChangeStateModeListener {
        void onChangeStateMode(StateMode stateMode);
    }

    interface OnPlayingStatusListener {
        void onPlayingStatus(boolean isPlay);
    }

    interface OnCurrentDurationListener {
        void onCurrentDuration(int duration);
    }

    interface OnCurrentPositionListener {
        void onCurrentPosition(int position);
    }


    private static final String TAG = SoundsController.class.getSimpleName();

    private final SoundtrackPlayer soundtrackPlayer;
    private final AudioPlayerFocus audioPlayerFocus;
    private final Application application;
    private List<Soundtrack> soundtracks;
    private final Deque<Integer> directOrder;
    private final Deque<Integer> reverseOrder;
    private int currentPosition = -1;
    private StateMode stateMode = StateMode.LOOP;
    private OnChangeStateModeListener onChangeStateModeListener;
    private OnPlayingStatusListener onPlayingStatusListener;
    private OnCurrentDurationListener onCurrentDurationListener;
    private OnCurrentPositionListener onCurrentPositionListener;

    public SoundsController(Application app) {
        application = app;
        soundtrackPlayer = new SoundtrackPlayer();
        audioPlayerFocus = new AudioPlayerFocus(app);
        setSoundtrackFinishListener();
        setPlayingStatusSoundtrackListener();
        setAudioFocusChangeStateListener();
        directOrder = new ArrayDeque<>();
        reverseOrder = new ArrayDeque<>();
    }

    public SoundtrackPlayer getSoundtrackPlayer() {
        return soundtrackPlayer;
    }

    public List<Soundtrack> getSoundtracks() {
        return soundtracks;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setSoundtracks(List<Soundtrack> soundtracks) {
        this.soundtracks = soundtracks;
    }

    public void setOnChangeStateModeListener(OnChangeStateModeListener onChangeStateModeListener) {
        this.onChangeStateModeListener = onChangeStateModeListener;
    }

    public void setOnPlayingStatusListener(OnPlayingStatusListener onPlayingStatusListener) {
        this.onPlayingStatusListener = onPlayingStatusListener;
    }

    public void setOnCurrentDurationListener(OnCurrentDurationListener onCurrentDurationListener) {
        this.onCurrentDurationListener = onCurrentDurationListener;
    }

    public void setOnCurrentPositionListener(OnCurrentPositionListener onCurrentPositionListener) {
        this.onCurrentPositionListener = onCurrentPositionListener;
    }



    public void loseAudioFocusAndStopPlayer() {
        Log.d(TAG, "отдача аудиофокуса, остановка плайера");
        audioPlayerFocus.loseAudioFocus();
        soundtrackPlayer.stop();
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
                    Log.d(TAG, "Другое приложение возвратило фокус");
                    soundtrackPlayer.setVolume(1, 1);
                    playOrPause(currentPosition, soundtracks);
                    break;
            }
        });
    }

    private void setPlayingStatusSoundtrackListener() {
        Log.d(TAG, "Установка слушателя начала воспроизведения");
        soundtrackPlayer.setOnPlayingStatusSoundtrackListener(isPlay -> {
            onPlayingStatusListener.onPlayingStatus(isPlay);
            if (isPlay) audioPlayerFocus.gainAudioFocus();
        });
    }

    private void setSoundtrackFinishListener() {
        Log.d(TAG, "Установка слушателя конца воспроизведения песни");
        soundtrackPlayer.setOnSoundtrackFinishedListener(() -> {
            if (stateMode == StateMode.REPEAT) {
                setCountOfLaunches(currentPosition, soundtracks);
                playOrPause(currentPosition, soundtracks);
            } else {
                setCountOfLaunches(currentPosition, soundtracks);
                next(currentPosition, soundtracks);
            }
        });
    }

    public void playOrPause() {
        playOrPause(currentPosition, soundtracks);
    }

    public int next() {
        next(currentPosition, soundtracks);
        return currentPosition;
    }

    public int previous() {
        previous(currentPosition, soundtracks);
        return currentPosition;
    }

    public void playOrPause(int position, List<Soundtrack> soundtracks) {
        if (soundtracks != null && soundtracks.isEmpty()) return;
        Log.d(TAG, "Начало или пауза песни " + position);
        this.soundtracks = soundtracks;
        this.currentPosition = position;
        soundtrackPlayer.playOrPause(soundtracks.get(position));
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    Handler handler = new Handler(getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            onCurrentDurationListener.onCurrentDuration((int) soundtrackPlayer.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    public void next(int position, List<Soundtrack> countSoundtracks) {
        if (countSoundtracks.isEmpty()) return;

        if (reverseOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = reverseOrder.pop();
            playOrPause(pop, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(pop);
            return;
        }

        Log.d(TAG, "Запуск следующей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position + 1 > countSoundtracks.size() - 1) ? 0 : position + 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) directOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void previous(int position, List<Soundtrack> countSoundtracks) {
        if (countSoundtracks.isEmpty()) return;

        if (directOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = directOrder.pop();
            playOrPause(pop, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(pop);
            return;
        }

        Log.d(TAG, "Запуск предыдущей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position - 1 < 0) ? countSoundtracks.size() - 1 : position - 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) reverseOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSoundtracks.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSoundtracks);
            onCurrentPositionListener.onCurrentPosition(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public StateMode switchMode() {
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
        onChangeStateModeListener.onChangeStateMode(stateMode);
        return stateMode;
    }

    public void setCurrentDuration(int position) {
        soundtrackPlayer.setCurrentDuration(position);
    }

    public void setRating(int position, List<Soundtrack> countSoundtracks, int rating) {
        AppDatabase database = AppDatabase.getDatabase(application);

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
        AppDatabase database = AppDatabase.getDatabase(application);

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
