package com.orion.musicplayer.ui.fragments;

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

import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.R;
import com.orion.musicplayer.ui.adapters.ChooserDialogAdapter;
import com.orion.musicplayer.data.models.Playlist;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.ArrayList;
import java.util.List;

public class ChooserDialogFragment extends androidx.fragment.app.DialogFragment {
    private static final String TAG = ChooserDialogFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DataModel dataModel;
    private Button saveButton;
    private Button closeButton;
    private Animation buttonAnimationClick;
    private String playlistName;
    private ChooserDialogAdapter soundtrackAdapter;
    private PlaylistDatabaseHelper playlistDatabaseHelper;

    public ChooserDialogFragment(PlaylistDatabaseHelper playlistDatabaseHelper){
        this.playlistDatabaseHelper = playlistDatabaseHelper;
    }

    public void setPlaylistDatabaseHelper(PlaylistDatabaseHelper playlistDatabaseHelper) {
        this.playlistDatabaseHelper = playlistDatabaseHelper;
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
        TextView textPlaylistName = view.findViewById(R.id.text_view_playlist_name);
        textPlaylistName.setText(playlistName);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        subscribeDialogCloseButtonClickListener();
        subscribeSaveButtonClickListener();
        createSoundtracksObserver((soundtrack, position) -> {
            dataModel.getCurrentPositionLiveData().setValue(position);
            dataModel.getIsFromPlaylist().setValue(false);
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
        Log.d(TAG, "Установка слушателя сохранения DialogClose");
        saveButton.setOnClickListener(view -> {
            saveButton.startAnimation(buttonAnimationClick);
            Playlist playlist = new Playlist();
            playlist.setPlaylistName(playlistName);
            playlist.setSongs(getItemsPlaylist());
            playlistDatabaseHelper.insertOrUpdatePlaylist(playlist);
            ChooserDialogFragment.this.dismiss();
        });
    }

    private List<Song> getItemsPlaylist() {
        List<Song> songPlaylist = new ArrayList<>();
        for (int position : currentChosePositionList) {
            songPlaylist.add(
                    dataModel.getSongsLiveData().getValue().get(position));
        }
        return songPlaylist;
    }

    private void createSoundtracksObserver(ChooserDialogAdapter.OnSoundtrackClickListener onSoundtrackClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка песен");
        dataModel.getSongsLiveData().observe(requireActivity(), soundtracks -> {
            soundtrackAdapter = new ChooserDialogAdapter(
                    getContext(),
                    soundtracks,
                    onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
            subscribeCheckBoxChooseListener();

            if (dataModel.getIsReadPlaylist().getValue()){
                refreshRecycleView();
                dataModel.getIsReadPlaylist().setValue(false);
            }
        });
    }
    
    public void refreshRecycleView(){
        List<Song> allSongs = dataModel.getSongsLiveData().getValue();
        List<Song> songsFromPlaylist = dataModel.getPlaylistLiveData().getValue()
                .get(dataModel.getCurrentPlaylist().getValue());
        boolean[] checked = new boolean[allSongs.size()];

        for (Song song : songsFromPlaylist) {
            int i = allSongs.indexOf(song);
            checked[i] = true;
        }
        soundtrackAdapter.setChecked(checked);
    }
}
