package com.orion.musicplayer.dagger.modules;

import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {MainActivityModule.class})
public class PlaylistDatabaseHelperModule {

    @Provides
    @Singleton
    public PlaylistDatabaseHelper playlistDatabaseHelper(MainActivity activity){
        return new PlaylistDatabaseHelper(activity);
    }
}
