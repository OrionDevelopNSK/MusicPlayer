package com.orion.musicplayer.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.SoundtrackDialogAdapter;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

public class SoundTrackListDialogFragment extends DialogFragment {
    private static final String TAG = SoundTrackListDialogFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private SoundtrackPlayerModel soundtrackPlayerModel;
    private Button saveButton;
    private Button closeButton;
    private Animation buttonAnimationClick;

    public static SoundTrackListDialogFragment newInstance() {
        return new SoundTrackListDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        recyclerView = view.findViewById(R.id.list_songs_dialog);
        saveButton = view.findViewById(R.id.save_playlist);
        closeButton = view.findViewById(R.id.close_dialog);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        subscribeDialogCloseButtonClickListener();
        subscribeSaveButtonClickListener();
        setListenerPlaylistSave();

        createSoundtracksObserver((soundtrack, position) -> {
            soundtrackPlayerModel.getCurrentPositionLiveData().setValue(position);
            if (!soundtrackPlayerModel.getIsPlayingLiveData().getValue()){
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PLAY);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(true);
            }
            else {
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(false);
            }

        });
        return view;
    }

    private void subscribeDialogCloseButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        closeButton.setOnClickListener(view -> {
            closeButton.startAnimation(buttonAnimationClick);
            SoundTrackListDialogFragment.this.dismiss();
        });
    }

    private void subscribeSaveButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        saveButton.setOnClickListener(view -> {
            saveButton.startAnimation(buttonAnimationClick);
            //TODO
        });
    }

    private void setListenerPlaylistSave(){
        Log.d(TAG, "Установка слушателя PlaylistSave");
        //TODO
    }

    private void createSoundtracksObserver(SoundtrackDialogAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        soundtrackPlayerModel.getSoundtracksLiveData().observe(requireActivity(), soundtracks -> {
            SoundtrackDialogAdapter soundtrackAdapter = new SoundtrackDialogAdapter(
                    this.getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
        });
    }
}
