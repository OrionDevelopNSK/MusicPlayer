package com.orion.musicplayer.dagger.modules;

import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.models.PlayerServiceConnection;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {DataModelModule.class,
        PlaylistDatabaseHelperModule.class,
        MainActivityModule.class,
        SharedPreferencesControllerModule.class
})
public class PlayerServiceConnectionModule {

    @Provides
    @Singleton
    public PlayerServiceConnection playerServiceConnection(DataModel dataModel,
                                                           PlaylistDatabaseHelper playlistDatabaseHelper,
                                                           MainActivity activity,
                                                           SharedPreferencesController sharedPreferencesController){
        return new PlayerServiceConnection(dataModel, playlistDatabaseHelper, activity, sharedPreferencesController);
    }
}
