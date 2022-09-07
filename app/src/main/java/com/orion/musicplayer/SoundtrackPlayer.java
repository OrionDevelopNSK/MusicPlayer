package com.orion.musicplayer;

import android.media.MediaPlayer;
import android.util.Log;

import com.orion.musicplayer.models.Soundtrack;

import java.io.IOException;

public class SoundtrackPlayer {
    public interface OnSoundtrackFinishedListener {
        void onSoundtrackFinish();
    }

    public interface OnPlayingStatusSoundtrackListener {
        void onPlayingStatusSoundtrack(boolean isPlay);
    }


    private static final String TAG = SoundtrackPlayer.class.getSimpleName();

    private Soundtrack currentPlayingSong;
    private OnSoundtrackFinishedListener onSoundtrackFinishedListener;
    private OnPlayingStatusSoundtrackListener statusSoundtrackListener;
    private final MediaPlayer mediaPlayer;

    public SoundtrackPlayer() {
        mediaPlayer = new MediaPlayer();
        Log.d(TAG, "Установка слушателя на событие окончания песни");
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            onSoundtrackFinishedListener.onSoundtrackFinish();
        });
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void setOnSoundtrackFinishedListener(OnSoundtrackFinishedListener onSoundtrackFinishedListener) {
        this.onSoundtrackFinishedListener = onSoundtrackFinishedListener;
    }

    public void setOnPlayingStatusSoundtrackListener(OnPlayingStatusSoundtrackListener statusSoundtrackListener) {
        this.statusSoundtrackListener = statusSoundtrackListener;
    }

    public void setCurrentTime(long position) {
        mediaPlayer.seekTo((int)position);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public long getCurrentTime() {
        return mediaPlayer.getCurrentPosition();
    }

    public void playOrPause(Soundtrack soundtrack) {
        if (currentPlayingSong != null && currentPlayingSong.equals(soundtrack)) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                Log.d(TAG, "Старт");
                mediaPlayer.start();
                statusSoundtrackListener.onPlayingStatusSoundtrack(true);
            }
        } else if (currentPlayingSong != null && !currentPlayingSong.equals(soundtrack)) {
            stop();
            setData(soundtrack);
        } else if (currentPlayingSong == null) {
            setData(soundtrack);
        }
        currentPlayingSong = soundtrack;
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            Log.d(TAG, "Пауза");
            mediaPlayer.pause();
            statusSoundtrackListener.onPlayingStatusSoundtrack(false);
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            Log.d(TAG, "Стоп");
            mediaPlayer.stop();
            statusSoundtrackListener.onPlayingStatusSoundtrack(false);
        }
    }

    private void start() {
        Log.d(TAG, "Регистрация обратного вызова готовности к воспроизведению");
        mediaPlayer.setOnPreparedListener(mp -> {
            Log.d(TAG, "Старт");
            mp.start();
            statusSoundtrackListener.onPlayingStatusSoundtrack(true);
        });
        Log.d(TAG, "Асинхронная подготовка проигрывателя к воспроизведению");
        mediaPlayer.prepareAsync();
    }

    private void setData(Soundtrack soundtrack) {
        String s = soundtrack.getData();
        Log.d(TAG, "Сброс плайера в его неинициализированное состояние");
        mediaPlayer.reset();
        try {
            Log.d(TAG, "Установка источника данных для воспроизведения");
            mediaPlayer.setDataSource(s);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        start();
    }
}
