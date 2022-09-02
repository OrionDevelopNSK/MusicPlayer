package com.orion.musicplayer.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.StateMode;

import java.util.ArrayList;
import java.util.List;

public class SoundtrackPlayerModel extends ViewModel {

    private final MutableLiveData<StateMode> stateModeLiveData = new MutableLiveData<>(StateMode.LOOP);
    private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Long> currentDurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Soundtrack>> soundtracksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoaded = new MutableLiveData<>();
    private final MutableLiveData<Action> playerAction = new MutableLiveData<>(Action.UNKNOWN);

    public MutableLiveData<StateMode> getStateModeLiveData() {
        return stateModeLiveData;
    }

    public MutableLiveData<Boolean> getIsPlayingLiveData() {
        return isPlayingLiveData;
    }

    public MutableLiveData<Long> getCurrentDurationLiveData() {
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
