package com.orion.musicplayer;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.List;

public class SoundtrackPlayer {
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public void playSoundtrack(List<String> mediaData, int i) {

        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer = new MediaPlayer();

        //TO DO
        String s = mediaData.get(i);
        int pos = s.lastIndexOf("\n", s.lastIndexOf("\n") - 1);
        String s2 = s.substring(pos);

        try {
            mediaPlayer.setDataSource(s2.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer playerM){
                playerM.start();
            }
        });

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

}
