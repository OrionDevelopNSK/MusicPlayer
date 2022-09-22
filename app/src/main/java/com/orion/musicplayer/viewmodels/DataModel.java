package com.orion.musicplayer.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orion.musicplayer.data.models.Playlist;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.SortingType;
import com.orion.musicplayer.utils.StateMode;

import java.util.List;
import java.util.Map;

public class DataModel extends ViewModel {
    private final MutableLiveData<StateMode> stateModeLiveData = new MutableLiveData<>(StateMode.LOOP);
    private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Long> durationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> songsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Action> playerActionLiveData = new MutableLiveData<>(Action.UNKNOWN);
    private final MutableLiveData<SortingType> sortingTypeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<Playlist, List<Song>>> playlistLiveData = new MutableLiveData<>();
    private final MutableLiveData<Playlist> currentPlaylist = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFromPlaylist = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isReadPlaylist = new MutableLiveData<>(false);


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
        return currentPositionLiveData;
    }

    public MutableLiveData<List<Song>> getSongsLiveData() {
        return songsLiveData;
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

    public MutableLiveData<Map<Playlist, List<Song>>> getPlaylistLiveData() {
        return playlistLiveData;
    }

    public MutableLiveData<Playlist> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public MutableLiveData<Boolean> getIsFromPlaylist() {
        return isFromPlaylist;
    }

    public MutableLiveData<Boolean> getIsReadPlaylist() {
        return isReadPlaylist;
    }
}
