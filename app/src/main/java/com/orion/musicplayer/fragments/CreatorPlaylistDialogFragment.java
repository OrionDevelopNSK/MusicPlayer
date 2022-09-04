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

import com.orion.musicplayer.R;

public class CreatorPlaylistDialogFragment extends androidx.fragment.app.DialogFragment {
    private static final String TAG = CreatorPlaylistDialogFragment.class.getSimpleName();

    private Button saveButton;
    private Button closeButton;
    private Animation buttonAnimationClick;

    public static CreatorPlaylistDialogFragment newInstance() {
        return new CreatorPlaylistDialogFragment();
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
        saveButton = view.findViewById(R.id.save_playlist);
        closeButton = view.findViewById(R.id.close_dialog);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        subscribeDialogCloseButtonClickListener();
        subscribeCreatePlaylistButtonClickListener();
        return view;
    }

    private void subscribeDialogCloseButtonClickListener() {
        Log.d(TAG, "Установка слушателя DialogClose");
        closeButton.setOnClickListener(view -> {
            closeButton.startAnimation(buttonAnimationClick);
            CreatorPlaylistDialogFragment.this.dismiss();
        });
    }

    private void subscribeCreatePlaylistButtonClickListener() {
        Log.d(TAG, "Установка слушателя CreatePlaylist");
        saveButton.setOnClickListener(view -> {
            saveButton.startAnimation(buttonAnimationClick);
            //TODO
        });
    }

}
