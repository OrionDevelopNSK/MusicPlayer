package com.orion.musicplayer.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.orion.musicplayer.utils.SortingType;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.viewmodels.DataModel;

public class SharedPreferencesController {
    private static final String TAG = SharedPreferencesController.class.getSimpleName();
    private static final String KEY_DATA = "currentSoundtrackTitle";
    private static final String KEY_DURATION = "currentSoundtrackDuration";
    private static final String KEY_STATE_MODE = "currentStateModePlaying";
    private static final String KEY_SORTING_TYPE = "currentSortingType";

    private final DataModel dataModel;
    private final Activity activity;
    private SharedPreferences defaultsSharedPreferences;
    private StateMode currentState;
    private long currentDuration;
    private String soundTitle;
    private SortingType sortingType;

    public SharedPreferencesController(DataModel dataModel, Activity activity) {
        this.dataModel = dataModel;
        this.activity = activity;
    }

    public String getSoundTitle() {
        return soundTitle;
    }

    public SortingType getSortingType() {
        return sortingType;
    }

    @SuppressLint("ApplySharedPref")
    public void saveDefaultsSharedPreferences() {
        Log.d(TAG, "Сохранить состояние " + soundTitle);
        defaultsSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultsSharedPreferences.edit();
        //Защита от NPE при уничтожении активити
        if (dataModel.getDurationLiveData().getValue() == null) return;
        currentDuration = dataModel.getDurationLiveData().getValue();
        soundTitle = dataModel.getSongsLiveData().getValue()
                .get(dataModel.getCurrentPositionLiveData().getValue()).getData();
        currentState = dataModel.getStateModeLiveData().getValue();
        editor.putString(KEY_DATA, soundTitle);
        editor.putString(KEY_SORTING_TYPE, dataModel.getSortingTypeLiveData().getValue().toString());
        editor.putLong(KEY_DURATION, currentDuration);
        editor.putString(KEY_STATE_MODE, dataModel.getStateModeLiveData().getValue().toString());
        editor.commit();
    }

    public void loadDefaultsSharedPreferences() {
        defaultsSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        currentDuration = defaultsSharedPreferences.getLong(KEY_DURATION, 0);
        soundTitle = defaultsSharedPreferences.getString(KEY_DATA, "");
        currentState = StateMode.valueOf(defaultsSharedPreferences.getString(KEY_STATE_MODE, "LOOP"));
        sortingType = SortingType.valueOf(defaultsSharedPreferences.getString(KEY_SORTING_TYPE, "DATE"));
        dataModel.getSortingTypeLiveData().setValue(sortingType);
        dataModel.getStateModeLiveData().setValue(currentState);
        Log.d(TAG, String.format("Получить состояния soundTitle: %s currentDuration: %d currentState: %s",
                soundTitle, currentDuration, currentState));
    }
}
