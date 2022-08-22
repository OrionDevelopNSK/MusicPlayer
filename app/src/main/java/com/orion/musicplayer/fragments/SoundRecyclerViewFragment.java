package com.orion.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.SoundtrackAdapter;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

import java.util.Objects;


public class SoundRecyclerViewFragment extends Fragment {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();
    private RecyclerView recyclerView;
    private SoundtracksModel soundtracksModel;
    private SoundtrackPlayerModel soundtrackPlayerModel;



    public static SoundRecyclerViewFragment newInstance() {
        return new SoundRecyclerViewFragment();
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
        soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);

        @SuppressWarnings("unused")
        MediaScannerObserver mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                requireActivity());

        createSoundtracksObserver((soundtrack, position) -> {
            soundtrackPlayerModel.getPositionLiveData().setValue(position);
            soundtrackPlayerModel.playOrPause(position, soundtracksModel.getSoundtracks().getValue());
        });

        createPositionObserver();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера текущей песни");
        soundtrackPlayerModel.getPositionLiveData().observe(
                requireActivity(), integer -> Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged());
    }

    private void createSoundtracksObserver(SoundtrackAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        soundtracksModel.getSoundtracks().observe(requireActivity(), soundtracks -> {
            SoundtrackAdapter soundtrackAdapter = new SoundtrackAdapter(
                    SoundRecyclerViewFragment.this.getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
        });
    }




}