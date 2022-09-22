package com.orion.musicplayer.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.ui.adapters.SongListAdapter;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.DataModel;


public class SongListFragment extends Fragment {
    private static final String TAG = SongListFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DataModel dataModel;
    private SongListAdapter songListAdapter;

    public static SongListFragment newInstance() {
        return new SongListFragment();
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_view, container, false);
        recyclerView = view.findViewById(R.id.list_songs);
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        createSoundtracksObserver((song, position) -> {
            dataModel.getIsFromPlaylist().setValue(false);
            dataModel.getCurrentPositionLiveData().setValue(position);
            if (!dataModel.getIsPlayingLiveData().getValue()) {
                dataModel.getPlayerActionLiveData().setValue(Action.PLAY);
                dataModel.getIsPlayingLiveData().setValue(true);
            } else {
                dataModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                dataModel.getIsPlayingLiveData().setValue(false);
            }
        });
        return view;
    }

    private void subscribe(){
        dataModel.getIsPlayingLiveData().observe(requireActivity(), aBoolean -> {
            if (dataModel.getCurrentPositionLiveData().getValue() != null){
                songListAdapter.changePlayingStatus(dataModel.getCurrentPositionLiveData().getValue(), aBoolean);
            }
        });
    }

    private void createSoundtracksObserver(SongListAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        dataModel.getSongsLiveData().observe(requireActivity(), soundtracks -> {
            songListAdapter = new SongListAdapter(
                    getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(songListAdapter);
            subscribe();
        });
    }




}