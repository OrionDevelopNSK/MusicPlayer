package com.orion.musicplayer;

import static android.os.Looper.getMainLooper;

import android.app.Application;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.orion.musicplayer.dao.SongDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.models.Song;
import com.orion.musicplayer.repositories.RoomSongRepository;
import com.orion.musicplayer.utils.AudioPlayerFocus;
import com.orion.musicplayer.utils.StateMode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;


public class PlayerController {

    interface OnChangeStateModeListener {
        void onChangeStateMode(StateMode stateMode);
    }

    public interface OnPlayingStatusListener {
        void onPlayingStatus(boolean isPlay);
    }

    interface OnCurrentDurationListener {
        void onCurrentDuration(long duration);
    }

    interface OnCurrentPositionListener {
        void onCurrentPosition(int position);
    }


    private static final String TAG = PlayerController.class.getSimpleName();

    private final Player player;
    private final AudioPlayerFocus audioPlayerFocus;
    private final Application application;
    private final Deque<Integer> directOrder;
    private final Deque<Integer> reverseOrder;
    private List<Song> songs;
    private int currentPosition = -1;
    private StateMode stateMode = StateMode.LOOP;
    private OnChangeStateModeListener onChangeStateModeListener;
    private OnPlayingStatusListener onPlayingStatusListener;
    private OnPlayingStatusListener onPlayingStatusForServiceListener;
    private OnCurrentDurationListener onCurrentDurationListener;
    private OnCurrentPositionListener onCurrentPositionListener;


    public PlayerController(Application app) {
        application = app;
        player = new Player();
        audioPlayerFocus = new AudioPlayerFocus(app);
        setSoundtrackFinishListener();
        setPlayingStatusSoundtrackListener();
        setAudioFocusChangeStateListener();
        directOrder = new ArrayDeque<>();
        reverseOrder = new ArrayDeque<>();
    }

    public void clearDequeSoundtrack(){
        directOrder.clear();
        reverseOrder.clear();
    }

    public Player getSoundtrackPlayer() {
        return player;
    }

    public List<Song> getSoundtracks() {
        return songs;
    }

    public void setStateMode(StateMode stateMode) {
        this.stateMode = stateMode;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setSoundtracks(List<Song> songs) {
        this.songs = songs;
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

    public void setOnPlayingStatusForServiceListener(OnPlayingStatusListener onPlayingStatusForServiceListener) {
        this.onPlayingStatusForServiceListener = onPlayingStatusForServiceListener;
    }

    public void loseAudioFocusAndStopPlayer() {
        Log.d(TAG, "отдача аудиофокуса, остановка плайера");
        audioPlayerFocus.loseAudioFocus();
        player.stop();
    }

    private void setAudioFocusChangeStateListener() {
        Log.d(TAG, "Установка слушателя состояния аудиофокуса");
        audioPlayerFocus.setOnAudioFocusChangeStateListener(eventCode -> {
            switch (eventCode) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "Фокус потерян. Запрос на долгое воспроизведение. Приостановка воспроизведения");
                    player.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "Фокус потерян. Запрос на короткое воспроизведение. Приостановка воспроизведения");
                    player.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "Фокус потерян. Запрос на короткое воспроизведение. Приглушение воспроизведения");
                    player.setVolume(.5f, .5f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "Другое приложение возвратило фокус");
                    player.setVolume(1, 1);
                    playOrPause(currentPosition, songs);
                    break;
            }
        });
    }

    private void setPlayingStatusSoundtrackListener() {
        Log.d(TAG, "Установка слушателя начала воспроизведения");
        player.setOnPlayingStatusSoundtrackListener(isPlay -> {
            onPlayingStatusListener.onPlayingStatus(isPlay);
            if (isPlay) {
                audioPlayerFocus.gainAudioFocus();
            }
            onPlayingStatusForServiceListener.onPlayingStatus(isPlay);
        });
    }

    private void setSoundtrackFinishListener() {
        Log.d(TAG, "Установка слушателя конца воспроизведения песни");
        player.setOnSoundtrackFinishedListener(() -> {
            if (stateMode == StateMode.REPEAT) {
                setCountOfLaunches(currentPosition, songs);
                playOrPause(currentPosition, songs);
            } else {
                setCountOfLaunches(currentPosition, songs);
                next(currentPosition, songs);
            }
        });
    }

    public void initSoundtrackPlayer(){
        player.initSoundtrackPlayer(currentPosition, songs);
    }

    public void playOrPause() {
        playOrPause(currentPosition, songs);
    }

    public int next() {
        next(currentPosition, songs);
        return currentPosition;
    }

    public int previous() {
        previous(currentPosition, songs);
        return currentPosition;
    }

    public void playOrPause(int position, List<Song> songs) {
        if (songs != null && songs.isEmpty()) return;
        if (songs.get(position) != null){
            Log.d(TAG, "Начало или пауза песни " + position);
            this.songs = songs;
            this.currentPosition = position;
            player.playOrPause(songs.get(position));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 0);
        }
    }

    Handler handler = new Handler(getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            onCurrentDurationListener.onCurrentDuration(player.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    public void next(int position, List<Song> countSongs) {
        if (countSongs.isEmpty()) return;

        if (reverseOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = reverseOrder.pop();
            playOrPause(pop, countSongs);
            onCurrentPositionListener.onCurrentPosition(pop);
            return;
        }

        Log.d(TAG, "Запуск следующей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position + 1 > countSongs.size() - 1) ? 0 : position + 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSongs);
            onCurrentPositionListener.onCurrentPosition(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) directOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSongs.size() - 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSongs);
            onCurrentPositionListener.onCurrentPosition(randomPosition);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    public void previous(int position, List<Song> countSongs) {
        if (countSongs.isEmpty()) return;

        if (directOrder.peek() != null) {
            Log.d(TAG, String.format("Получение порядкового номера песни из очереди. В очереди %d элементов", directOrder.size()));
            int pop = directOrder.pop();
            playOrPause(pop, countSongs);
            onCurrentPositionListener.onCurrentPosition(pop);
            return;
        }

        Log.d(TAG, "Запуск предыдущей песни");
        if (stateMode == StateMode.LOOP || stateMode == StateMode.REPEAT) {
            int i = (position - 1 < 0) ? countSongs.size() - 1 : position - 1;
            Log.d(TAG, "Порядковый номер песни: " + i);
            playOrPause(i, countSongs);
            onCurrentPositionListener.onCurrentPosition(i);
        } else if (stateMode == StateMode.RANDOM) {
            if (currentPosition != -1) reverseOrder.push(currentPosition);
            int randomPosition = new Random().nextInt(countSongs.size() + 1);
            Log.d(TAG, "Порядковый номер случайной песни: " + randomPosition);
            playOrPause(randomPosition, countSongs);
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

    public void setCurrentDuration(long position) {
        player.setCurrentTime(position);
    }

    public void changeRating() {
        AppDatabase database = AppDatabase.getDatabase(application);
        final int likeStatus = 1;
        final int unlikeStatus = 0;
        AsyncTask.execute(() -> {
            Log.d(TAG, "Запись в базу данных оценки песни");
            Song song = songs.get(currentPosition);
            song.setRating(song.getRating() == 0 ? likeStatus : unlikeStatus);
            SongDao songDao = database.soundtrackDao();
            RoomSongRepository roomSongRepository = new RoomSongRepository(songDao);
            roomSongRepository.updateSoundtrack(song.toSoundtrackDbEntity());
            Log.d(TAG, String.format("Рейтинг песни под номером :%d равен %d", currentPosition, song.getRating()));
        });
    }

    private void setCountOfLaunches(int position, List<Song> countSongs) {
        AppDatabase database = AppDatabase.getDatabase(application);
        AsyncTask.execute(() -> {
            Log.d(TAG, "Запись в базу данных количества раз прослушивания");
            Song song = countSongs.get(position);
            int countOfLaunchesOld = song.getCountOfLaunches();
            song.setCountOfLaunches(countOfLaunchesOld + 1);
            SongDao songDao = database.soundtrackDao();
            RoomSongRepository roomSongRepository = new RoomSongRepository(songDao);
            roomSongRepository.updateSoundtrack(song.toSoundtrackDbEntity());
            Log.d(TAG, String.format("Количество прослушивания песни под номером :%d равно %d", position, countOfLaunchesOld + 1));
        });
    }
}
