package com.orion.musicplayer.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.SoundtrackAdapterDialog;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

public class SoundTrackListDialogFragment extends DialogFragment {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private RecyclerView recyclerView;
    private SoundtracksModel soundtracksModel;
    private SoundtrackPlayerModel soundtrackPlayerModel;
    private Button saveButton;
    private Button closeButton;

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
        soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        setListenerDialogClose();
        setListenerPlaylistSave();

        createSoundtracksObserver((soundtrack, position) -> {
            soundtrackPlayerModel.getPositionLiveData().setValue(position);
            soundtrackPlayerModel.playOrPause(position, soundtracksModel.getSoundtracks().getValue());
        });

        return view;
    }

    private void setListenerDialogClose() {
        Log.d(TAG, "Установка слушателя DialogClose");
        closeButton.setOnClickListener(view -> dismiss());
    }

    private void setListenerPlaylistSave(){
        Log.d(TAG, "Установка слушателя PlaylistSave");
    }

    private void createSoundtracksObserver(SoundtrackAdapterDialog.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        soundtracksModel.getSoundtracks().observe(requireActivity(), soundtracks -> {
            SoundtrackAdapterDialog soundtrackAdapter = new SoundtrackAdapterDialog(
                    this.getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
        });
    }
}
