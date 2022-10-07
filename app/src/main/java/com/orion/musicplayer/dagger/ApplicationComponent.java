package com.orion.musicplayer.dagger;


import com.orion.musicplayer.dagger.modules.SessionServiceModule;
import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.models.ActionBinder;
import com.orion.musicplayer.models.DefaultDescriptionControllerFragmentCreator;
import com.orion.musicplayer.models.NotificationController;
import com.orion.musicplayer.models.PlayerServiceConnection;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.viewmodels.DataModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        SessionServiceModule.class
})
public interface ApplicationComponent {
    DataModel datamodel();

    SharedPreferencesController sharedPreferencesController();

    PlaylistDatabaseHelper playlistDatabaseHelper();

    MediaSessionService mediaSessionService();

    PlayerServiceConnection playerServiceConnection();

    ActionBinder actionBinder();

    DefaultDescriptionControllerFragmentCreator defaultDescriptionControllerFragmentCreator();

    NotificationController notificationController();

    void inject(MainActivity mainActivity);
}
