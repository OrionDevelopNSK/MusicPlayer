package com.orion.musicplayer.fragments;

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
import com.orion.musicplayer.adapters.SoundRecycleViewAdapter;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.Objects;


public class SoundtrackListFragment extends Fragment {
    private static final String TAG = SoundtrackListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private SoundtrackPlayerModel soundtrackPlayerModel;


    public static SoundtrackListFragment newInstance() {
        return new SoundtrackListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_view, container, false);
        recyclerView = view.findViewById(R.id.list_songs);

        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);

        createSoundtracksObserver((soundtrack, position) -> {
            soundtrackPlayerModel.getCurrentPositionLiveData().setValue(position);
            if (!soundtrackPlayerModel.getIsPlayingLiveData().getValue()) {
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PLAY);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(true);
            } else {
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(false);
            }
        });
        createPositionObserver();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера текущей песни");
        soundtrackPlayerModel.getCurrentPositionLiveData().observe(
                requireActivity(), integer -> Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged());
    }

    private void createSoundtracksObserver(SoundRecycleViewAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        soundtrackPlayerModel.getSoundtracksLiveData().observe(requireActivity(), soundtracks -> {
            SoundRecycleViewAdapter soundRecycleViewAdapter = new SoundRecycleViewAdapter(
                    SoundtrackListFragment.this.getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundRecycleViewAdapter);
        });
    }




}