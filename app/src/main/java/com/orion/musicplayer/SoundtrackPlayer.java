package com.orion.musicplayer;

import android.media.MediaPlayer;
import android.util.Log;

import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.io.IOException;

public class SoundtrackPlayer {

    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    public interface OnSoundtrackFinishedListener {
        void onSoundtrackFinish();
    }

    public interface OnPlayingStatusSoundtrackListener {
        void onPlayingStatusSoundtrack(boolean isPlay);
    }


    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private Soundtrack currentPlayingSong;
    private OnSoundtrackFinishedListener onSoundtrackFinishedListener;
    private OnPlayingStatusSoundtrackListener statusSoundtrackListener;

    public void setOnSoundtrackFinishedListener(OnSoundtrackFinishedListener onSoundtrackFinishedListener){
        this.onSoundtrackFinishedListener = onSoundtrackFinishedListener;
    }

    public void setOnPlayingStatusSoundtrackListener(OnPlayingStatusSoundtrackListener statusSoundtrackListener){
        this.statusSoundtrackListener = statusSoundtrackListener;
    }

    public SoundtrackPlayer() {
        Log.d(TAG, "Установка слушателя на событие окончания песни");
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            mediaPlayer.seekTo(0);
            onSoundtrackFinishedListener.onSoundtrackFinish();
        });
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }


    public void playOrPause(Soundtrack soundtrack) {

        if (currentPlayingSong != null && currentPlayingSong.equals(soundtrack)) {
            if (mediaPlayer.isPlaying()) {
                Log.d(TAG, "Пауза");
                pause();
            }
            else {
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


    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            statusSoundtrackListener.onPlayingStatusSoundtrack(false);
        }
    }

    private void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            statusSoundtrackListener.onPlayingStatusSoundtrack(false);
        }
    }

    public long getCurrentTime(){
        return mediaPlayer.getCurrentPosition();
    }

    public void setCurrentDuration(int position){
        mediaPlayer.seekTo(position);
    }


    private void start() {
        Log.d(TAG, "Регистрация обратного вызова готовности к воспроизведению");
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "Старт");
                mp.start();
                statusSoundtrackListener.onPlayingStatusSoundtrack(true);
            }
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
