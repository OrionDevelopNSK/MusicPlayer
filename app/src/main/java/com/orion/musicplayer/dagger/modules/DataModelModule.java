package com.orion.musicplayer.dagger.modules;

import androidx.lifecycle.ViewModelProvider;

import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {MainActivityModule.class})
public class DataModelModule {

    @Provides
    @Singleton
    public DataModel dataModel(MainActivity activity){
        return new ViewModelProvider(activity).get(DataModel.class);
    }


}
