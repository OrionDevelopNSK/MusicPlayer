package com.orion.musicplayer.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orion.musicplayer.entities.PlaylistDbEntity;
import com.orion.musicplayer.entities.SoundtrackDbEntity;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.SortingType;
import com.orion.musicplayer.utils.StateMode;

import java.util.List;
import java.util.Map;

public class SoundtrackPlayerModel extends ViewModel {
    private final MutableLiveData<StateMode> stateModeLiveData = new MutableLiveData<>(StateMode.LOOP);
    private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Long> durationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Soundtrack>> soundtracksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Action> playerActionLiveData = new MutableLiveData<>(Action.UNKNOWN);
    private final MutableLiveData<SortingType> sortingTypeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<Playlist, List<Soundtrack>>> playlistLiveData = new MutableLiveData<>();


    public MutableLiveData<StateMode> getStateModeLiveData() {
        return stateModeLiveData;
    }

    public MutableLiveData<Boolean> getIsPlayingLiveData() {
        return isPlayingLiveData;
    }

    public MutableLiveData<Long> getDurationLiveData() {
        return durationLiveData;
    }

    public MutableLiveData<Integer> getCurrentPositionLiveData() {
        return positionLiveData;
    }

    public MutableLiveData<List<Soundtrack>> getSoundtracksLiveData() {
        return soundtracksLiveData;
    }

    public MutableLiveData<Boolean> getIsLoadedLiveData() {
        return isLoadedLiveData;
    }

    public MutableLiveData<Action> getPlayerActionLiveData() {
        return playerActionLiveData;
    }

    public MutableLiveData<SortingType> getSortingTypeLiveData() {
        return sortingTypeLiveData;
    }

    public MutableLiveData<Map<Playlist, List<Soundtrack>>> getPlaylistLiveData() {
        return playlistLiveData;
    }
}
