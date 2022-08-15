package com.orion.musicplayer;

import android.media.MediaPlayer;

import com.orion.musicplayer.models.Soundtrack;

import java.io.IOException;

public class SoundtrackPlayer {
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private Soundtrack currentPlayingSong;

    public void play(Soundtrack soundtrack) {
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

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.seekTo(0);
        });

    }

    public void pause() {
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
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
