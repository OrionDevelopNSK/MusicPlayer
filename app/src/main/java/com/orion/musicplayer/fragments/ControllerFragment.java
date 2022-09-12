package com.orion.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.orion.musicplayer.R;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.utils.TimeConverter;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerFragment extends Fragment {
    private final static String TAG = ControllerFragment.class.getSimpleName();


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
    private Animation buttonAnimationClick;
    private SoundtrackPlayerModel soundtrackPlayerModel;
    private ExecutorService executorService;

    public static ControllerFragment newInstance() {
        return new ControllerFragment();
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
        textArtistTitle = currentView.findViewById(R.id.text_soundtrack_artist);
        textTimeDuration = currentView.findViewById(R.id.text_time_duration);
        textCurrentDuration = currentView.findViewById(R.id.text_current_duration);
        slider = currentView.findViewById(R.id.slider_soundtrack);
        buttonToStart = currentView.findViewById(R.id.button_to_start);
        buttonPlayOrPause = currentView.findViewById(R.id.button_play_pause);
        buttonPrevious = currentView.findViewById(R.id.button_previous);
        buttonNext = currentView.findViewById(R.id.button_next);
        buttonChangeStateMode = currentView.findViewById(R.id.button_change_state_mode);
        buttonAnimationClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        buttonChangeStateMode.setBackgroundResource(R.drawable.ic_loop);
        buttonPlayOrPause.setBackgroundResource(R.drawable.ic_play);
        buttonToStart.setBackgroundResource(R.drawable.ic_to_start);
        buttonPrevious.setBackgroundResource(R.drawable.ic_previous);
        buttonNext.setBackgroundResource(R.drawable.ic_next);
        executorService = Executors.newSingleThreadExecutor();
        changeLabelFormat();
        setListenerSliderTouch();
        subscribeListenerButtonToStart();
        subscribeListenerButtonPlayOrPause();
        subscribeListenerButtonPrevious();
        subscribeListenerButtonNext();
        subscribeListenerButtonChangeStateMode();
        createPositionObserver();
        createDurationObserver();
        createStateModeObserver();
        createPlayingStateObserver();
        createDataValidateObserver();
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
                soundtrackPlayerModel.getDurationLiveData().setValue((long) slider.getValue());
                Log.d(TAG, "Отпускание слайдера прокрутки");
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.SLIDER_MANIPULATE);
                isTouch = false;
            }
        });
    }

    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера воспроизведения песни");
        soundtrackPlayerModel.getCurrentPositionLiveData().observe(requireActivity(), position -> {
            List<Soundtrack> soundtrackList = soundtrackPlayerModel.getSoundtracksLiveData().getValue();
            if (soundtrackList.size() == 0) return;
            Soundtrack soundtrack = soundtrackList.get(position);
            Log.d(TAG, "Изменение значений элементов UI");
            stylizedData(soundtrack);
            textTimeDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrack.getDuration()));
            //Защита от IllegalStateException размера слайдера
            slider.setValue(0);
            slider.setValueTo(soundtrack.getDuration());
        });
    }

    private void createDurationObserver() {
        Log.d(TAG, "Создание обсервера изменения текущего времени воспроизведения песни");
        soundtrackPlayerModel.getDurationLiveData().observe(requireActivity(), integer -> {
            textCurrentDuration.setText(TimeConverter.toMinutesAndSeconds(soundtrackPlayerModel.getDurationLiveData().getValue()));
            if (isTouch) return;
            int value = soundtrackPlayerModel.getDurationLiveData().getValue().intValue();
            if (value > slider.getValueTo()) {
                slider.setValue(slider.getValueTo());
            } else if (value < slider.getValueTo()) {
                slider.setValue(value);
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
                buttonChangeStateMode.setBackgroundResource(R.drawable.ic_repeat_one);
            } else if (stateMode == StateMode.RANDOM) {
                Log.d(TAG, "Установить на кнопку картинку \"drawable_random\"");
                buttonChangeStateMode.setBackgroundResource(R.drawable.ic_random);
            }
        });
    }


    private void stylizedData(Soundtrack soundtrack) {
        if (soundtrack.getArtist().equalsIgnoreCase("<unknown>")) {
            textArtistTitle.setText(soundtrack.getTitle());
            textSoundtrackTitle.setText("********");
        } else {
            textArtistTitle.setText(soundtrack.getArtist());
            textSoundtrackTitle.setText(soundtrack.getTitle());
        }
    }

    private void createPlayingStateObserver() {
        soundtrackPlayerModel.getIsPlayingLiveData().observe(requireActivity(), isPlay -> {
            if (isPlay == null) return;
            if (isPlay) {
                Log.d(TAG, "Начало воспроизведения");
                buttonPlayOrPause.setBackgroundResource(R.drawable.ic_pause);
            } else {
                Log.d(TAG, "Остановка воспроизведения");
                buttonPlayOrPause.setBackgroundResource(R.drawable.ic_play);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void createDataValidateObserver() {
        soundtrackPlayerModel.getSoundtracksLiveData().observe(requireActivity(), soundtracks -> {
            if (soundtracks.isEmpty()) {
                Log.d(TAG, "Список пуст");
                slider.setEnabled(false);
                buttonToStart.setEnabled(false);
                buttonPlayOrPause.setEnabled(false);
                buttonPrevious.setEnabled(false);
                buttonNext.setEnabled(false);
                buttonChangeStateMode.setEnabled(false);
                textCurrentDuration.setText("");
            } else {
                Log.d(TAG, "Список не пуст");
                textCurrentDuration.setText("00:00");
                if (buttonToStart.isEnabled()) return;
                slider.setEnabled(true);
                buttonToStart.setEnabled(true);
                buttonPlayOrPause.setEnabled(true);
                buttonPrevious.setEnabled(true);
                buttonNext.setEnabled(true);
                buttonChangeStateMode.setEnabled(true);
            }
        });
    }

    private void subscribeListenerButtonToStart() {
        Log.d(TAG, "Установка слушателя ButtonToStart");
        buttonToStart.setOnClickListener(view -> {
            executorService.execute((Runnable) () -> buttonToStart.startAnimation(buttonAnimationClick));
            soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.TO_START);
        });
    }

    private void subscribeListenerButtonChangeStateMode() {
        Log.d(TAG, "Установка слушателя ButtonChange");
        buttonChangeStateMode.setOnClickListener(view -> {
            executorService.execute((Runnable) () -> buttonChangeStateMode.startAnimation(buttonAnimationClick));
            soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.SWITCH_MODE);
        });
    }

    private void subscribeListenerButtonNext() {
        Log.d(TAG, "Установка слушателя ButtonNext");
        buttonNext.setOnClickListener(view -> {
            executorService.execute((Runnable) () -> buttonNext.startAnimation(buttonAnimationClick));
            soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.NEXT);
        });
    }

    private void subscribeListenerButtonPrevious() {
        Log.d(TAG, "Установка слушателя ButtonPrevious");
        buttonPrevious.setOnClickListener(view -> {
            executorService.execute((Runnable) () -> buttonPrevious.startAnimation(buttonAnimationClick));
            soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PREVIOUS);
        });
    }

    private void subscribeListenerButtonPlayOrPause() {
        Log.d(TAG, "Установка слушателя ButtonPlayOrPause");
        buttonPlayOrPause.setOnClickListener(view -> {
            executorService.execute((Runnable) () -> buttonPlayOrPause.startAnimation(buttonAnimationClick));
            if (!soundtrackPlayerModel.getIsPlayingLiveData().getValue()) {
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PLAY);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(true);
            } else {
                soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(false);
            }
        });
    }

    private void changeLabelFormat() {
        Log.d(TAG, "Установка формата отображения времени на бейдже ползунка");
        slider.setLabelFormatter(value -> TimeConverter.toMinutesAndSeconds((int) value));
    }

}
