package com.orion.musicplayer.dagger;


import com.orion.musicplayer.dagger.modules.DataModelModule;
import com.orion.musicplayer.dagger.modules.PlayerServiceConnectionModule;
import com.orion.musicplayer.dagger.modules.PlaylistDatabaseHelperModule;
import com.orion.musicplayer.dagger.modules.SharedPreferencesControllerModule;
import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.models.PlayerServiceConnection;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        DataModelModule.class,
        SharedPreferencesControllerModule.class,
        PlaylistDatabaseHelperModule.class,
        PlayerServiceConnectionModule.class
})
public interface ApplicationComponent {
    DataModel datamodel();

    SharedPreferencesController sharedPreferencesController();

    PlaylistDatabaseHelper playlistDatabaseHelper();

    PlayerServiceConnection playerServiceConnection();

    public void inject(MainActivity mainActivity);
}
