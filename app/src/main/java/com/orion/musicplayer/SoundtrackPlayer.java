package com.orion.musicplayer;

import android.media.MediaPlayer;

import com.orion.musicplayer.models.Soundtrack;

import java.io.IOException;

public class SoundtrackPlayer {

    public interface OnSoundtrackFinishedListener {
        void onSoundtrackFinish();
    }


    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private Soundtrack currentPlayingSong;
    private OnSoundtrackFinishedListener onSoundtrackFinishedListener;


    public void setOnSoundtrackFinishedListener(OnSoundtrackFinishedListener onSoundtrackFinishedListener){
        this.onSoundtrackFinishedListener = onSoundtrackFinishedListener;
    }

    public SoundtrackPlayer() {
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
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
            if (mediaPlayer.isPlaying()) pause();
            else {
                mediaPlayer.start();
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
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    private void stop() {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
    }

    public long getCurrentTime(){
        return mediaPlayer.getCurrentPosition();
    }

    public void setCurrentDuration(int position){
        mediaPlayer.seekTo(position);
    }


    private void start() {
        mediaPlayer.setOnPreparedListener(mp -> mp.start());
        mediaPlayer.prepareAsync();
    }

    private void setData(Soundtrack soundtrack) {
        String s = soundtrack.getData();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }








}
