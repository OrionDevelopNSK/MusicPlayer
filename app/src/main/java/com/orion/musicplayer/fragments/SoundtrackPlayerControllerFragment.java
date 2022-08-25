package com.orion.musicplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
    private final static String KEY_DATA = "currentSoundtrackTitle";
    private static final String KEY_DURATION = "currentSoundtrackDuration";

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
    private SharedPreferences defaultsSharedPreferences;
    private String soundTitle;
    private int currentDuration;

    public static SoundtrackPlayerControllerFragment newInstance() {
        return new SoundtrackPlayerControllerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        load();
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

    public void defaultDescription() {
        soundtracksModel.getIsLoaded().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                List<Soundtrack> soundtracks = soundtracksModel.getSoundtracks().getValue();

                int position = 0;
                for (int i = 0; i < soundtracks.size(); i++) {
                    if (soundtracks.get(i).getData().equals(soundTitle)){
                        position = i;
                        Log.d(TAG, "Найдена последняя воиспроизводимая песня");
                        break;
                    }
                }
                soundtrackPlayerModel.getPositionLiveData().setValue(position);
                textCurrentDuration.setText("00:00");
            }
        });
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

    private void createPositionObserver() {
        Log.d(TAG, "Создание обсервера изменения номера воспроизведения песни");
        soundtrackPlayerModel.getPositionLiveData().observe(requireActivity(), position -> {
            List<Soundtrack> soundtrackList = soundtracksModel.getSoundtracks().getValue();
            if (soundtrackList.size() == 0) return;
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
        buttonPlayOrPause.setOnClickListener(view -> soundtrackPlayerModel.playOrPause(
                soundtrackPlayerModel.getPositionLiveData().getValue(),
                soundtracksModel.getSoundtracks().getValue()));
    }

    private void changeLabelFormat() {
        Log.d(TAG, "Установка формата отображения времени на бейдже ползунка");
        slider.setLabelFormatter(value -> TimeConverter.toMinutesAndSeconds((int) value));
    }

    private void save(){
        Log.d(TAG, "Сохранить состояние " + soundTitle);
        defaultsSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultsSharedPreferences.edit();
        currentDuration = soundtrackPlayerModel.getCurrentDurationLiveData().getValue();
        soundTitle = soundtracksModel.getSoundtracks().getValue().get(soundtrackPlayerModel.getPositionLiveData().getValue()).getData();
        editor.putString(KEY_DATA, soundTitle);
        editor.putInt(KEY_DURATION, currentDuration);
        editor.commit();
    }

    private void load(){
        defaultsSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        currentDuration = defaultsSharedPreferences.getInt(KEY_DURATION, 0);
        soundTitle = defaultsSharedPreferences.getString(KEY_DATA, "");
        Log.d(TAG, "Получить состояния " + soundTitle);
    }



    @Override
    public void onStop() {
        super.onStop();
        save();
    }
}
