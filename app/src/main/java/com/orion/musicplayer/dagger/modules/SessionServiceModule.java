package com.orion.musicplayer.dagger.modules;

import com.orion.musicplayer.models.ActionBinder;
import com.orion.musicplayer.models.DefaultDescriptionControllerFragmentCreator;
import com.orion.musicplayer.models.NotificationController;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {
        MainActivityModule.class
})
public class SessionServiceModule {
    private MediaSessionService mediaSessionService;

    public SessionServiceModule(MediaSessionService mediaSessionService) {
        this.mediaSessionService = mediaSessionService;
    }

    @Provides
    @Singleton
    MediaSessionService mediaSessionService(){
        return mediaSessionService;
    }

    @Provides
    @Singleton
    public NotificationController notificationController(DataModel dataModel, MediaSessionService mediaSessionService){
        return new NotificationController(dataModel, mediaSessionService);
    }

    @Provides
    @Singleton
    public DefaultDescriptionControllerFragmentCreator defaultDescriptionControllerFragmentCreator(
            DataModel dataModel,
            MediaSessionService mediaSessionService,
            SharedPreferencesController sharedPreferencesController,
            MainActivity mainActivity) {
        return new DefaultDescriptionControllerFragmentCreator(
                dataModel,
                mediaSessionService,
                sharedPreferencesController.getSoundTitle(),
                mainActivity);
    }

    @Provides
    @Singleton
    public ActionBinder actionBinder(DataModel dataModel,
                                     MediaSessionService mediaSessionService,
                                     NotificationController notificationController,
                                     MainActivity mainActivity) {
        return new ActionBinder(
                dataModel,
                mediaSessionService,
                notificationController,
                mainActivity);
    }
}
