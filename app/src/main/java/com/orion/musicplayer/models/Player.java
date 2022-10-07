package com.orion.musicplayer.models;

import android.media.MediaPlayer;
import android.util.Log;

import com.orion.musicplayer.data.models.Song;

import java.io.IOException;
import java.util.List;

public class Player {

    public interface OnSoundtrackFinishedListener {
        void onSoundtrackFinish();
    }

    public interface OnPlayingStatusSoundtrackListener {
        void onPlayingStatusSoundtrack(boolean isPlay);
    }


    private static final String TAG = Player.class.getSimpleName();

    private Song currentPlayingSong;
    private OnSoundtrackFinishedListener onSoundtrackFinishedListener;
    private OnPlayingStatusSoundtrackListener statusSoundtrackListener;
    private final MediaPlayer mediaPlayer;

    public Player() {
        mediaPlayer = new MediaPlayer();
        Log.d(TAG, "Установка слушателя на событие окончания песни");
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (!isFirstPlay) return;
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            onSoundtrackFinishedListener.onSoundtrackFinish();
        });
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setOnSoundtrackFinishedListener(OnSoundtrackFinishedListener onSoundtrackFinishedListener) {
        this.onSoundtrackFinishedListener = onSoundtrackFinishedListener;
    }

    public void setOnPlayingStatusSoundtrackListener(OnPlayingStatusSoundtrackListener statusSoundtrackListener) {
        this.statusSoundtrackListener = statusSoundtrackListener;
    }

    private int currentTime = -1;
    private boolean isFirstPlay = false;

    public void setCurrentTime(long position) {
        mediaPlayer.seekTo((int) position);
        if (isFirstPlay) return;
        currentTime = (int) position;
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public long getCurrentTime() {
        return !isFirstPlay && currentTime > 0 ? currentTime : mediaPlayer.getCurrentPosition();
    }

    public void initSoundtrackPlayer(int position, List<Song> songs) {
        if (!isFirstPlay){
            setData(songs.get(position));
            mediaPlayer.prepareAsync();
        }
    }

    public void playOrPause(Song song) {
        if (currentPlayingSong != null && currentPlayingSong.equals(song)) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                Log.d(TAG, "Старт");
                mediaPlayer.start();
                statusSoundtrackListener.onPlayingStatusSoundtrack(true);
            }
        } else if (currentPlayingSong != null && !currentPlayingSong.equals(song)) {
            stop();
            setData(song);
            start();
        } else if (currentPlayingSong == null) {
            setData(song);
            start();
        }
        currentPlayingSong = song;
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
            Log.e(TAG, "Старт");
            mp.start();
            if (currentTime > 0 && !isFirstPlay) {
                mediaPlayer.seekTo(currentTime);
                isFirstPlay = true;
            } else if (!isFirstPlay){
                isFirstPlay = true;
            }
            statusSoundtrackListener.onPlayingStatusSoundtrack(true);
        });
        Log.d(TAG, "Асинхронная подготовка проигрывателя к воспроизведению");
        mediaPlayer.prepareAsync();
    }

    private void setData(Song song) {
        String s = song.getData();
        Log.d(TAG, "Сброс плайера в его неинициализированное состояние");
        mediaPlayer.reset();
        try {
            Log.d(TAG, "Установка источника данных для воспроизведения");
            mediaPlayer.setDataSource(s);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }
}
