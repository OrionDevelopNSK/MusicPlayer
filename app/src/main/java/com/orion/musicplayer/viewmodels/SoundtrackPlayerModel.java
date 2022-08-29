package com.orion.musicplayer.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orion.musicplayer.AudioPlayerFocus;
import com.orion.musicplayer.SoundtrackPlayer;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.StateMode;

import java.util.List;

public class SoundtrackPlayerModel extends ViewModel {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private final MutableLiveData<StateMode> stateModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentDurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Soundtrack>> soundtracksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoaded = new MutableLiveData<>();
    private final MutableLiveData<Action> playerAction = new MutableLiveData<>();

    public MutableLiveData<StateMode> getStateModeLiveData() {
        return stateModeLiveData;
    }

    public MutableLiveData<Boolean> getIsPlayingLiveData() {
        return isPlayingLiveData;
    }

    public MutableLiveData<Integer> getCurrentDurationLiveData() {
        return currentDurationLiveData;
    }

    public MutableLiveData<Integer> getCurrentPositionLiveData() {
        return positionLiveData;
    }

    public MutableLiveData<List<Soundtrack>> getSoundtracksLiveData() {
        return soundtracksLiveData;
    }

    public MutableLiveData<Boolean> getIsLoaded() {
        return isLoaded;
    }

    public MutableLiveData<Action> getPlayerAction() {
        return playerAction;
    }
}
