package com.orion.musicplayer;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;

import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

public class AudioPlayerFocus implements AudioManager.OnAudioFocusChangeListener {

    public interface OnAudioFocusChangeStateListener{
        void  onAudioFocusChangeState(int eventCode);
    }

    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();


    private OnAudioFocusChangeStateListener onAudioFocusChangeStateListener;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;

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
                .Builder(AudioManager.AUDIOFOCUS_GAIN | AudioManager.STREAM_MUSIC)
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
