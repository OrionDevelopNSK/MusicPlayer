package com.orion.musicplayer.dagger.modules;

import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.models.PlayerServiceConnection;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {
    private MainActivity activity;

    public MainActivityModule(MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    MainActivity activity() {
        return activity;
    }

    @Provides
    @Singleton
    public DataModel dataModel(MainActivity activity) {
        return new ViewModelProvider(activity).get(DataModel.class);
    }

    @Provides
    @Singleton
    public PlaylistDatabaseHelper playlistDatabaseHelper(MainActivity activity) {
        return new PlaylistDatabaseHelper(activity);
    }

    @Provides
    @Singleton
    public SharedPreferencesController sharedPreferencesController(DataModel dataModel, MainActivity mainActivity) {
        return new SharedPreferencesController(dataModel, mainActivity);
    }

    @Provides
    @Singleton
    public PlayerServiceConnection playerServiceConnection(
            DataModel dataModel,
            PlaylistDatabaseHelper playlistDatabaseHelper,
            MainActivity mainActivity,
            SharedPreferencesController sharedPreferencesController
    ) {
        return new PlayerServiceConnection(
                dataModel,
                playlistDatabaseHelper,
                mainActivity,
                sharedPreferencesController);
    }


}
