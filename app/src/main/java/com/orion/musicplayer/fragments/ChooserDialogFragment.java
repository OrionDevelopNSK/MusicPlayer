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
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.PlaylistController;
import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.SoundtrackDialogAdapter;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.ArrayList;
import java.util.List;

public class ChooserDialogFragment extends androidx.fragment.app.DialogFragment {
    private static final String TAG = ChooserDialogFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private SoundtrackPlayerModel soundtrackPlayerModel;
    private Button saveButton;
    private Button closeButton;
    private TextView textPlaylistName;
    private Animation buttonAnimationClick;
    private String playlistName;
    private SoundtrackDialogAdapter soundtrackAdapter;

    public static ChooserDialogFragment newInstance() {
        return new ChooserDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_dialog_chooser, container, false);
        recyclerView = view.findViewById(R.id.list_songs_dialog);
        saveButton = view.findViewById(R.id.save_playlist);
        closeButton = view.findViewById(R.id.close_dialog);
        textPlaylistName = view.findViewById(R.id.text_view_playlist_name);
        textPlaylistName.setText(playlistName);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        subscribeDialogCloseButtonClickListener();
        subscribeSaveButtonClickListener();
        setListenerPlaylistSave();
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
        return view;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }


    private final List<Integer> currentChosePositionList = new ArrayList<>();

    private void subscribeCheckBoxChooseListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        soundtrackAdapter.setOnSoundTrackChoseListener((position, isSelected) -> {
            if (isSelected) {
                currentChosePositionList.add(position);
            } else {
                currentChosePositionList.remove((Object) position);
            }
        });
    }

    private void subscribeDialogCloseButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        closeButton.setOnClickListener(view -> {
            closeButton.startAnimation(buttonAnimationClick);
            ChooserDialogFragment.this.dismiss();
        });
    }

    private void subscribeSaveButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        saveButton.setOnClickListener(view -> {
            saveButton.startAnimation(buttonAnimationClick);
            Playlist playlist = new Playlist();
            playlist.setPlaylistName(playlistName);
            playlist.setSoundtracks(getItemsPlaylist());

            //TODO не работает внесение в базу данных
//            new PlaylistController(getActivity().getApplication()).insertPlaylist(playlist);
            ChooserDialogFragment.this.dismiss();
        });
    }

    private List<Soundtrack> getItemsPlaylist() {
        List<Soundtrack> soundtrackPlaylist = new ArrayList<>();
        for (int position : currentChosePositionList) {
            soundtrackPlaylist.add(
                    soundtrackPlayerModel.getSoundtracksLiveData().getValue().get(position));
        }
        return soundtrackPlaylist;
    }

    private void setListenerPlaylistSave() {
        Log.d(TAG, "Установка слушателя PlaylistSave");
        //TODO
    }

    private void createSoundtracksObserver(SoundtrackDialogAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        soundtrackPlayerModel.getSoundtracksLiveData().observe(requireActivity(), soundtracks -> {
            soundtrackAdapter = new SoundtrackDialogAdapter(
                    this.getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
            subscribeCheckBoxChooseListener();
        });
    }


}
