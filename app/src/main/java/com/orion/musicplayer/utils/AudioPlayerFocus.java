package com.orion.musicplayer.utils;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;

public class AudioPlayerFocus implements AudioManager.OnAudioFocusChangeListener {

    public interface OnAudioFocusChangeStateListener{
        void  onAudioFocusChangeState(int eventCode);
    }

    private static final String TAG = AudioPlayerFocus.class.getSimpleName();

    private final AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private OnAudioFocusChangeStateListener onAudioFocusChangeStateListener;

    public AudioPlayerFocus(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setOnAudioFocusChangeStateListener(OnAudioFocusChangeStateListener onAudioFocusChangeStateListener) {
        this.onAudioFocusChangeStateListener = onAudioFocusChangeStateListener;
    }

    public void loseAudioFocus(){
        Log.d(TAG, "Потеря аудиофокуса");
        audioManager.abandonAudioFocusRequest(focusRequest);
    }

    public void gainAudioFocus(){
        Log.d(TAG, "Получение аудиофокуса");
        focusRequest = new AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .build();
        audioManager.requestAudioFocus(focusRequest);
    }

    @Override
    public void onAudioFocusChange(int i) {
        Log.d(TAG, "Смена фокуса " + i);
        onAudioFocusChangeStateListener.onAudioFocusChangeState(i);
    }
}
