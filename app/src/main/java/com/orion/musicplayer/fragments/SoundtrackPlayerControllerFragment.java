package com.orion.musicplayer.fragments;

import static android.os.Looper.getMainLooper;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.orion.musicplayer.R;
import com.orion.musicplayer.SoundtrackPlayer;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.utils.TimeConverter;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

import java.util.List;

public class SoundtrackPlayerControllerFragment extends Fragment {
    private boolean isTouch;

    public static SoundtrackPlayerControllerFragment newInstance() {
        return new SoundtrackPlayerControllerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_control_panel, container, false);
        TextView textSoundtrackTitle = currentView.findViewById(R.id.text_soundtrack_title);
        TextView textArtistTitle = currentView.findViewById(R.id.text_artist_title);
        TextView textTimeDuration = currentView.findViewById(R.id.text_time_duration);
        TextView textCurrentDuration = currentView.findViewById(R.id.text_current_duration);
        Slider slider = currentView.findViewById(R.id.slider_soundtrack);

        Button buttonToStart = currentView.findViewById(R.id.button_to_start);
        Button buttonPlayOrPause = currentView.findViewById(R.id.button_play_pause);
        Button buttonPrevious = currentView.findViewById(R.id.button_previous);
        Button buttonNext = currentView.findViewById(R.id.button_next);
        Button buttonChangeStateMode = currentView.findViewById(R.id.button_change_state_mode);

        SoundtracksModel soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);
        SoundtrackPlayerModel soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);

        slider.setLabelFormatter(value -> TimeConverter.toMinutesAndSeconds((int) value));

        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
//                isTouch = true;

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                soundtrackPlayerModel.setCurrentDuration((int) slider.getValue());
//                isTouch = false;
            }
        });

        buttonPlayOrPause.setOnClickListener(view -> soundtrackPlayerModel.playOrPause(
                soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));


        buttonPrevious.setOnClickListener(view -> soundtrackPlayerModel.previous(
                soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));

        buttonNext.setOnClickListener(view -> soundtrackPlayerModel.next(soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));

        buttonChangeStateMode.setOnClickListener(view -> soundtrackPlayerModel.switchMode());

        soundtrackPlayerModel.getCurrentDurationLiveData().observe(requireActivity(), integer -> {
            textCurrentDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrackPlayerModel.getCurrentDurationLiveData().getValue()));
//                if (isTouch == true) return;
            slider.setValue(soundtrackPlayerModel.getCurrentDurationLiveData().getValue());
        });

        soundtrackPlayerModel.getStateModeLiveData().observe(requireActivity(), stateMode -> {
            //TODO
            if (stateMode == StateMode.LOOP) {
                //buttonChangeStateMode.setBackground("drawable_loop");
            } else if (stateMode == StateMode.REPEAT) {
                //buttonChangeStateMode.setBackground("drawable_repeat");
            } else {
                //buttonChangeStateMode.setBackground("drawable_random");
            }
        });


        soundtrackPlayerModel.getPositionLiveData().observe(requireActivity(), position -> {
            List<Soundtrack> soundtrackList = soundtracksModel.getSoundtracks().getValue();

            Soundtrack soundtrack = soundtrackList.get(position);
            textSoundtrackTitle.setText(soundtrack.getTitle());
            textArtistTitle.setText(soundtrack.getArtist());
            textTimeDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrack.getDuration()));
            slider.setValueTo(soundtrack.getDuration());
            slider.setValueFrom(0);
            soundtrackPlayerModel.playOrPause(position, soundtrackList);

        });


        return currentView;
    }
}
