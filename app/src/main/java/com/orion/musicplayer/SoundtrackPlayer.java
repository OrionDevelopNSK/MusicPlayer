package com.orion.musicplayer;

import android.media.MediaPlayer;

import java.io.IOException;

public class SoundtrackPlayer {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Soundtrack currentPlayingSong;

    public void play(Soundtrack soundtrack) {
        if (currentPlayingSong != null && currentPlayingSong.equals(soundtrack)) {
            if (mediaPlayer.isPlaying()) pause();
            else {
                start();
            }
        } else if (currentPlayingSong != null && !currentPlayingSong.equals(soundtrack)) {
            stop();
            setData(soundtrack);
        } else if (currentPlayingSong == null) {
            setData(soundtrack);
        }
        currentPlayingSong = soundtrack;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.seekTo(0);
            }
        });

    }

    public void pause() {
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
    }

    private void start() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
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
