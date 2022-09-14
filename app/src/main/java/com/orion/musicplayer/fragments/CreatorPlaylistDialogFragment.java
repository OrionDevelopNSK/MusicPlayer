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

import com.google.android.material.textfield.TextInputEditText;
import com.orion.musicplayer.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.R;

public class CreatorPlaylistDialogFragment extends androidx.fragment.app.DialogFragment {
    private static final String TAG = CreatorPlaylistDialogFragment.class.getSimpleName();

    private Button buttonSave;
    private Button buttonClose;
    private TextInputEditText playlistName;
    private Animation buttonAnimationClick;
    private final PlaylistDatabaseHelper playlistDatabaseHelper;


    public CreatorPlaylistDialogFragment(PlaylistDatabaseHelper playlistDatabaseHelper){
        this.playlistDatabaseHelper = playlistDatabaseHelper;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_creater, container, false);
        buttonSave = view.findViewById(R.id.save_playlist);
        buttonClose = view.findViewById(R.id.close_dialog);
        playlistName = view.findViewById(R.id.text_playlist_name);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        subscribeDialogCloseButtonClickListener();
        subscribeCreatePlaylistButtonClickListener();
        return view;
    }

    private void subscribeDialogCloseButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        buttonClose.setOnClickListener(view -> {
            buttonClose.startAnimation(buttonAnimationClick);
            CreatorPlaylistDialogFragment.this.dismiss();
        });
    }

    private void subscribeCreatePlaylistButtonClickListener() {
        Log.d(TAG, "Установка слушателя CreatePlaylist");
        buttonSave.setOnClickListener(view -> {
            buttonSave.startAnimation(buttonAnimationClick);
            CreatorPlaylistDialogFragment.this.dismiss();
            ChooserDialogFragment fragment = new ChooserDialogFragment(playlistDatabaseHelper);
            fragment.setPlaylistName(playlistName.getText().toString());
            fragment.setStyle(ChooserDialogFragment.STYLE_NO_TITLE, R.style.Dialog);
            fragment.show(requireActivity().getSupportFragmentManager(), "Выберите песни");
        });
    }

}
