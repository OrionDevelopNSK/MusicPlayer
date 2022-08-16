package com.orion.musicplayer.fragments;

import static android.os.Looper.getMainLooper;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.orion.musicplayer.utils.TimeConverter;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

public class SoundtrackPlayerControllerFragment extends Fragment {

    public static SoundtrackPlayerControllerFragment newInstance() {
        return new SoundtrackPlayerControllerFragment();
    }

    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_panel, container, false);
        TextView textSoundtrackTitle = view.findViewById(R.id.text_soundtrack_title);
        TextView textArtistTitle = view.findViewById(R.id.text_artist_title);
        TextView textTimeDuration = view.findViewById(R.id.text_time_duration);
        TextView textCurrentDuration = view.findViewById(R.id.text_current_duration);
        Slider slider = view.findViewById(R.id.slider_soundtrack);

        SoundtracksModel soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);

        slider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return TimeConverter.toMinutesAndSeconds((int) value);
            }
        });

        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                soundtrackPlayer.setCurrentTime((int) slider.getValue());
            }
        });

        Handler handler = new Handler(getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textCurrentDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrackPlayer.getCurrentTime()));
                slider.setValue(soundtrackPlayer.getCurrentTime());
                handler.postDelayed(this, 1000);
            }
        };

        soundtracksModel.getPosition().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                Soundtrack soundtrack = soundtracksModel.getSoundtracks().getValue().get(position);
                textSoundtrackTitle.setText(soundtrack.getTitle());
                textArtistTitle.setText(soundtrack.getArtist());
                textTimeDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrack.getDuration()));
                slider.setValueTo(soundtrack.getDuration());
                slider.setValueFrom(0);
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);
                soundtrackPlayer.play(soundtrack);
            }
        });

        return view;
    }
}
