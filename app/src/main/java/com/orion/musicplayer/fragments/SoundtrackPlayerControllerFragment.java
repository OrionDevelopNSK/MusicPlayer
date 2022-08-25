package com.orion.musicplayer.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.orion.musicplayer.R;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.utils.TimeConverter;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;
import com.orion.musicplayer.viewmodels.SoundtracksModel;

import java.util.List;

public class SoundtrackPlayerControllerFragment extends Fragment {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    private boolean isTouch;
    private TextView textSoundtrackTitle;
    private TextView textArtistTitle;
    private TextView textTimeDuration;
    private TextView textCurrentDuration;
    private Slider slider;
    private Button buttonToStart;
    private Button buttonPlayOrPause;
    private Button buttonPrevious;
    private Button buttonNext;
    private Button buttonChangeStateMode;
    private SoundtracksModel soundtracksModel;
    private SoundtrackPlayerModel soundtrackPlayerModel;

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

        Log.d(TAG, "Биндинг Views");
        textSoundtrackTitle = currentView.findViewById(R.id.text_soundtrack_title);
        textArtistTitle = currentView.findViewById(R.id.text_artist_title);
        textTimeDuration = currentView.findViewById(R.id.text_time_duration);
        textCurrentDuration = currentView.findViewById(R.id.text_current_duration);
        slider = currentView.findViewById(R.id.slider_soundtrack);
        buttonToStart = currentView.findViewById(R.id.button_to_start);
        buttonPlayOrPause = currentView.findViewById(R.id.button_play_pause);
        buttonPrevious = currentView.findViewById(R.id.button_previous);
        buttonNext = currentView.findViewById(R.id.button_next);
        buttonChangeStateMode = currentView.findViewById(R.id.button_change_state_mode);
        soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        buttonChangeStateMode.setBackgroundResource(R.drawable.ic_loop);
        buttonPlayOrPause.setBackgroundResource(R.drawable.ic_play);
        changeLabelFormat();
        setListenerSliderTouch();
        setListenerButtonToStart();
        setListenerButtonPlayOrPause();
        setListenerButtonPrevious();
        setListenerButtonNext();
        setListenerButtonChangeStateMode();
        createDurationObserver();
        createPositionObserver();
        createStateModeObserver();
        createPlayingStateObserver();
        defaultDescription();
        return currentView;
    }

    private void setListenerSliderTouch() {
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                Log.d(TAG, "Касание слайдера прокрутки");
                isTouch = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                Log.d(TAG, String.format("Установка позиции слайдера: %d", (int) slider.getValue()));
                soundtrackPlayerModel.setCurrentDuration((int) slider.getValue());
                Log.d(TAG, "Отпускание слайдера прокрутки");
                isTouch = false;
            }
        });
    }

    private void createStateModeObserver() {
        Log.d(TAG, "Создание обсервера изменения состояния режима воспроизведения");
        soundtrackPlayerModel.getStateModeLiveData().observe(requireActivity(), stateMode -> {
            if (stateMode == StateMode.LOOP) {
                Log.d(TAG, "Установить на кнопку картинку \"drawable_loop\"");
                buttonChangeStateMode.setBackgroundResource(R.drawable.ic_loop);
            } else if (stateMode == StateMode.REPEAT) {
                Log.d(TAG, "Установить на кнопку картинку \"drawable_repeat\"");
                buttonChangeStateMode.setBackgroundResource(R.drawable.ic_repeat);
            } else if (stateMode == StateMode.RANDOM) {
                Log.d(TAG, "Установить на кнопку картинку \"drawable_random\"");
                buttonChangeStateMode.setBackgroundResource(R.drawable.ic_shake);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    public void defaultDescription() {
        soundtracksModel.getIsLoaded().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                soundtrackPlayerModel.getPositionLiveData().setValue(0);
            }
        });
    }

    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера воспроизведения песни");
        soundtrackPlayerModel.getPositionLiveData().observe(requireActivity(), position -> {
            List<Soundtrack> soundtrackList = soundtracksModel.getSoundtracks().getValue();
            Soundtrack soundtrack = soundtrackList.get(position);
            Log.d(TAG, "Изменение значений элементов UI");
            textSoundtrackTitle.setText(soundtrack.getTitle());
            textArtistTitle.setText(soundtrack.getArtist());
            textTimeDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrack.getDuration()));
            slider.setValueTo(soundtrack.getDuration());
            slider.setValueFrom(0);
        });
    }


    private void createPlayingStateObserver() {
        soundtrackPlayerModel.getIsPlayingLiveData().observe(requireActivity(), aBoolean -> {
            if (aBoolean == true) {
                Log.d(TAG, "Начало воспроизведения");
                buttonPlayOrPause.setBackgroundResource(R.drawable.ic_pause);
            } else {
                Log.d(TAG, "Остановка воспроизведения");
                buttonPlayOrPause.setBackgroundResource(R.drawable.ic_play);
            }
        });
    }


    private void createDurationObserver() {
        Log.d(TAG, "Создание обсервера изменения текущего времени воспроизведения песни");
        soundtrackPlayerModel.getCurrentDurationLiveData().observe(requireActivity(), integer -> {
            textCurrentDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrackPlayerModel.getCurrentDurationLiveData().getValue()));
            if (isTouch) return;
            int value = soundtrackPlayerModel.getCurrentDurationLiveData().getValue();
            if (value > slider.getValueTo()) return;
            slider.setValue(soundtrackPlayerModel.getCurrentDurationLiveData().getValue());
        });
    }

    private void setListenerButtonToStart() {
        Log.d(TAG, "Установка слушателя ButtonToStart");
        buttonToStart.setOnClickListener(view -> soundtrackPlayerModel.playOrPause(
                0,
                soundtracksModel.getSoundtracks().getValue()));
    }


    private void setListenerButtonChangeStateMode() {
        Log.d(TAG, "Установка слушателя ButtonChange");
        buttonChangeStateMode.setOnClickListener(view -> soundtrackPlayerModel.switchMode());
    }

    private void setListenerButtonNext() {
        Log.d(TAG, "Установка слушателя ButtonNext");
        buttonNext.setOnClickListener(view -> soundtrackPlayerModel.next(
                soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));
    }

    private void setListenerButtonPrevious() {
        Log.d(TAG, "Установка слушателя ButtonPrevious");
        buttonPrevious.setOnClickListener(view -> soundtrackPlayerModel.previous(
                soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));
    }

    private void setListenerButtonPlayOrPause() {
        Log.d(TAG, "Установка слушателя ButtonPlayOrPause");
        buttonPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundtrackPlayerModel.playOrPause(
                        soundtrackPlayerModel.getPositionLiveData().getValue(),
                        soundtracksModel.getSoundtracks().getValue());
            }
        });
    }

    private void changeLabelFormat() {
        Log.d(TAG, "Установка формата отображения времени на бейдже ползунка");
        slider.setLabelFormatter(value -> TimeConverter.toMinutesAndSeconds((int) value));
    }
}
