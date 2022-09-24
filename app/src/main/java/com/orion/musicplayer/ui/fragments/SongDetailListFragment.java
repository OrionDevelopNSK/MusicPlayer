package com.orion.musicplayer.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.data.models.Playlist;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.ui.adapters.SongDetailListAdapter;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;
import java.util.Map;


public class SongDetailListFragment extends Fragment {


    public interface OnClickBackToPlaylistsListener {
        void onClickBackToPlaylists();
    }


    private static final String TAG = SongDetailListFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DataModel dataModel;
    private Button backToPlaylist;
    private TextView playlistName;
    private OnClickBackToPlaylistsListener onClickBackToPlaylistsListener;
    private SongDetailListAdapter songDetailListAdapter;

    public void setOnClickBackToSongsListener(OnClickBackToPlaylistsListener onClickBackToPlaylistsListener) {
        this.onClickBackToPlaylistsListener = onClickBackToPlaylistsListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_detail, container, false);
        recyclerView = view.findViewById(R.id.list_soundtrack_detail);
        playlistName = view.findViewById(R.id.playlist_name_detail);
        backToPlaylist = view.findViewById(R.id.back_to_playlist);

        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        Map<Playlist, List<Song>> playlistListMap = dataModel.getPlaylistLiveData().getValue();
        List<Song> songs = playlistListMap.get(dataModel.getCurrentPlaylist().getValue());
        songDetailListAdapter = new SongDetailListAdapter(
                getContext(),
                songs,
                null
        );
        recyclerView.setAdapter(songDetailListAdapter);
        createSoundtracksObserver(this::clickButtonSong);
        defaultSettings();
        return view;
    }

    private void defaultSettings() {
        if (dataModel.getCurrentPlayingPlaylist().getValue() != null) {
            String currentPlayingPlaylistName = dataModel.getCurrentPlayingPlaylist().getValue().getPlaylistName();
            String currentPlaylistName = dataModel.getCurrentPlaylist().getValue().getPlaylistName();
            if (currentPlayingPlaylistName.equals(currentPlaylistName)) {
                songDetailListAdapter.changePlayingStatus(dataModel.getCurrentPositionLiveData().getValue(), true);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void createSoundtracksObserver(SongDetailListAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Map<Playlist, List<Song>> playlistListMap = dataModel.getPlaylistLiveData().getValue();
        List<Song> songs = playlistListMap.get(dataModel.getCurrentPlaylist().getValue());
        songDetailListAdapter = new SongDetailListAdapter(
                getContext(),
                songs,
                onSoundtrackClickListener
        );
        playlistName.setText("[" + dataModel.getCurrentPlaylist().getValue().getPlaylistName() + "]");
        backToPlaylist.setOnClickListener(view -> {
            Log.d(TAG, "Вернуться к списку плейлистов");
            onClickBackToPlaylistsListener.onClickBackToPlaylists();
        });
        recyclerView.setAdapter(songDetailListAdapter);
        subscribeSongsListUpdate();
        subscribeChangeCurrentPlaylist();
    }

    private void clickButtonSong(Song song, int position) {
        dataModel.getIsFromPlaylist().setValue(true);
        dataModel.getCurrentPositionLiveData().setValue(position);
        if (!dataModel.getIsPlayingLiveData().getValue()) {
            dataModel.getPlayerActionLiveData().setValue(Action.PLAY);
            dataModel.getIsPlayingLiveData().setValue(true);
        } else {
            dataModel.getPlayerActionLiveData().setValue(Action.PAUSE);
            dataModel.getIsPlayingLiveData().setValue(false);
        }
    }

    private void subscribeSongsListUpdate() {
        dataModel.getIsPlayingLiveData().observe(requireActivity(), aBoolean -> {
            if (dataModel.getCurrentPositionLiveData().getValue() != null && dataModel.getIsFromPlaylist().getValue()) {
                songDetailListAdapter.changePlayingStatus(dataModel.getCurrentPositionLiveData().getValue(), aBoolean);
            } else if (!dataModel.getIsFromPlaylist().getValue()) {
                songDetailListAdapter.changePlayingStatus();
            }
        });
    }

    private void subscribeChangeCurrentPlaylist() {
        dataModel.getCurrentPlaylist().observe(requireActivity(), playlist -> {
                    songDetailListAdapter.changePlayingStatus();
                    if (dataModel.getCurrentPlayingPlaylist().getValue() == null) return;
                    if (playlist.getPlaylistName().equals(dataModel.getCurrentPlayingPlaylist().getValue().getPlaylistName()) &&
                            dataModel.getIsPlayingLiveData().getValue()) {
                        songDetailListAdapter.changePlayingStatus(dataModel.getCurrentPositionLiveData().getValue(), true);
                    }
                }
        );
    }
}