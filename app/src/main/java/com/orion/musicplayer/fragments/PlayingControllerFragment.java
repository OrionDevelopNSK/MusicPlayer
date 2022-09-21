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
import com.orion.musicplayer.models.Song;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.utils.TimeConverter;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayingControllerFragment extends Fragment {
    private static final String TAG = PlayingControllerFragment.class.getSimpleName();

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
    private Animation animationButtonClick;
    private DataModel dataModel;
    private ExecutorService executorService;
    private boolean isTouch;


    public static PlayingControllerFragment newInstance() {
        return new PlayingControllerFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_control_panel, container, false);
        bindViews(currentView);
        createDefaultButtonsDrawables();
        animationButtonClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        executorService = Executors.newSingleThreadExecutor();
        changeLabelFormat();
        subscribeListenerSliderTouch();
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

    private void createDefaultButtonsDrawables() {
        Log.d(TAG, "Создание дефолтных картинок");
        buttonChangeStateMode.setBackgroundResource(R.drawable.ic_loop);
        buttonPlayOrPause.setBackgroundResource(R.drawable.ic_play);
        buttonToStart.setBackgroundResource(R.drawable.ic_to_start);
        buttonPrevious.setBackgroundResource(R.drawable.ic_previous);
        buttonNext.setBackgroundResource(R.drawable.ic_next);
    }

    private void bindViews(View currentView) {
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
    }

    private void subscribeListenerSliderTouch() {
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                Log.d(TAG, "Касание слайдера прокрутки");
                isTouch = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                Log.d(TAG, String.format("Установка позиции слайдера: %d", (int) slider.getValue()));
                dataModel.getDurationLiveData().setValue((long) slider.getValue());
                Log.d(TAG, "Отпускание слайдера прокрутки");
                dataModel.getPlayerActionLiveData().setValue(Action.SLIDER_MANIPULATE);
                isTouch = false;
            }
        });
    }

    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера воспроизведения песни");
        dataModel.getCurrentPositionLiveData().observe(requireActivity(), position -> {
            List<Song> songList;
            dataModel.getIsFromPlaylist().getValue();
            if (!dataModel.getIsFromPlaylist().getValue()){
                songList = dataModel.getSongsLiveData().getValue();
            }else{
                songList = dataModel.getPlaylistLiveData().getValue().get(dataModel.getCurrentPlaylist().getValue());
            }

            if (songList.size() == 0) return;
            Song song = songList.get(dataModel.getCurrentPositionLiveData().getValue());
            Log.d(TAG, "Изменение значений элементов UI");
            stylizedData(song);
            textTimeDuration.setText(TimeConverter.toMinutesAndSeconds(song.getDuration()));
            //Защита от IllegalStateException размера слайдера
            slider.setValue(0);
            slider.setValueTo(song.getDuration());
        });
    }

    private void createDurationObserver() {
        Log.d(TAG, "Создание обсервера изменения текущего времени воспроизведения песни");
        dataModel.getDurationLiveData().observe(requireActivity(), integer -> {
            textCurrentDuration.setText(TimeConverter.toMinutesAndSeconds(dataModel.getDurationLiveData().getValue()));
            if (isTouch) return;
            int value = dataModel.getDurationLiveData().getValue().intValue();
            if (value > slider.getValueTo()) {
                slider.setValue(slider.getValueTo());
            } else if (value < slider.getValueTo()) {
                slider.setValue(value);
            }
        });
    }

    private void createStateModeObserver() {
        Log.d(TAG, "Создание обсервера изменения состояния режима воспроизведения");
        dataModel.getStateModeLiveData().observe(requireActivity(), stateMode -> {
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

    private void stylizedData(Song song) {
        if (song.getArtist().equalsIgnoreCase("<unknown>")) {
            textArtistTitle.setText(song.getTitle());
            textSoundtrackTitle.setText("********");
        } else {
            textArtistTitle.setText(song.getArtist());
            textSoundtrackTitle.setText(song.getTitle());
        }
    }

    private void createPlayingStateObserver() {
        dataModel.getIsPlayingLiveData().observe(requireActivity(), isPlay -> {
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
        dataModel.getSongsLiveData().observe(requireActivity(), soundtracks -> {
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
            executorService.execute(() -> buttonToStart.startAnimation(animationButtonClick));
            dataModel.getPlayerActionLiveData().setValue(Action.TO_START);
        });
    }

    private void subscribeListenerButtonChangeStateMode() {
        Log.d(TAG, "Установка слушателя ButtonChange");
        buttonChangeStateMode.setOnClickListener(view -> {
            executorService.execute(() -> buttonChangeStateMode.startAnimation(animationButtonClick));
            dataModel.getPlayerActionLiveData().setValue(Action.SWITCH_MODE);
        });
    }

    private void subscribeListenerButtonNext() {
        Log.d(TAG, "Установка слушателя ButtonNext");
        buttonNext.setOnClickListener(view -> {
            executorService.execute(() -> buttonNext.startAnimation(animationButtonClick));
            dataModel.getPlayerActionLiveData().setValue(Action.NEXT);
        });
    }

    private void subscribeListenerButtonPrevious() {
        Log.d(TAG, "Установка слушателя ButtonPrevious");
        buttonPrevious.setOnClickListener(view -> {
            executorService.execute(() -> buttonPrevious.startAnimation(animationButtonClick));
            dataModel.getPlayerActionLiveData().setValue(Action.PREVIOUS);
        });
    }

    private void subscribeListenerButtonPlayOrPause() {
        Log.d(TAG, "Установка слушателя ButtonPlayOrPause");
        buttonPlayOrPause.setOnClickListener(view -> {
            executorService.execute(() -> buttonPlayOrPause.startAnimation(animationButtonClick));
            if (!dataModel.getIsPlayingLiveData().getValue()) {
                dataModel.getPlayerActionLiveData().setValue(Action.PLAY);
                dataModel.getIsPlayingLiveData().setValue(true);
            } else {
                dataModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                dataModel.getIsPlayingLiveData().setValue(false);
            }
        });
    }

    private void changeLabelFormat() {
        Log.d(TAG, "Установка формата отображения времени на бейдже ползунка");
        slider.setLabelFormatter(value -> TimeConverter.toMinutesAndSeconds((int) value));
    }

}
