package com.orion.musicplayer.dagger.modules;

import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {MainActivityModule.class, DataModelModule.class})

public class SharedPreferencesControllerModule {

    @Provides
    @Singleton
    public SharedPreferencesController sharedPreferencesController(DataModel dataModel, MainActivity mainActivity){
        return new SharedPreferencesController(dataModel, mainActivity);
    }
}
