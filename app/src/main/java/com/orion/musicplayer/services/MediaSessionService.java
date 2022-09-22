package com.orion.musicplayer.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

import com.orion.musicplayer.ui.MainActivity;
import com.orion.musicplayer.notifications.MediaNotificationManager;
import com.orion.musicplayer.models.Player;
import com.orion.musicplayer.models.PlayerController;
import com.orion.musicplayer.data.database.DataLoader;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.utils.StateMode;

public class MediaSessionService extends Service {

    class NoisyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                soundsController.playOrPause();
            }
        }
    }


    class ScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                Log.e(TAG, "Погасание экрана");
                isScreenOn = false;
            }else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                Log.e(TAG, "Включение экрана");
                isScreenOn = true;
                createNotification(pos, stateMode, ratingCurrentSoundtrack);
            }
        }
    }


    private static final String TAG = MediaSessionService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 888;

    private MediaNotificationManager mediaNotificationManager;
    private MediaSessionCompat mediaSession;
    private PlayerController soundsController;
    private DataLoader dataLoader;
    private final BinderService binderService = new BinderService();
    private final NoisyBroadcastReceiver noisyBroadcastReceiver = new NoisyBroadcastReceiver();
    private final IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final ScreenBroadcastReceiver screenBroadcastReceiver = new ScreenBroadcastReceiver();
    private final IntentFilter screenIntentFilter = new IntentFilter();
    private boolean isScreenOn = true;


    public PlayerController getSoundsController() {
        return soundsController;
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "Создание сервиса");
        super.onCreate();
        dataLoader = new DataLoader(getApplication());
        soundsController = new PlayerController(getApplication());
        dataLoader.setOnDatabaseChangeListener(soundtracks -> soundsController.setSoundtracks(soundtracks));
        mediaNotificationManager = new MediaNotificationManager(this);
        mediaSession = new MediaSessionCompat(this, "PlayerService", null,
                PendingIntent.getActivity(getApplicationContext(),
                        0,
                        new Intent(getApplicationContext(), MainActivity.class),
                        PendingIntent.FLAG_IMMUTABLE));

        MediaControllerCompat controller = mediaSession.getController();
        controller.registerCallback(new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
            }


        });

        subscribePlayingStatusListener();
        registerReceiver(noisyBroadcastReceiver, noisyIntentFilter);
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenBroadcastReceiver, screenIntentFilter);
    }

    public void initMediaPlayer() {
        soundsController.initSoundtrackPlayer();
    }

    public MediaMetadataCompat getMetadata(int position) {
        Song song = dataLoader.getSongsCashed().get(position);
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
                .build();
        mediaSession.setMetadata(metadata);
        return metadata;
    }

    public PlaybackStateCompat.Builder getBuilderState() {
        Player player = soundsController.getSoundtrackPlayer();
        return new PlaybackStateCompat.Builder()
                .setBufferedPosition(player.getCurrentTime())
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE
                );
    }

    @SuppressLint("WrongConstant")
    public PlaybackStateCompat getState(PlaybackStateCompat.Builder builder) {
        Player player = soundsController.getSoundtrackPlayer();
        currentDuration = player.getCurrentTime();
        PlaybackStateCompat state = builder.setState(
                        getPlaybackState(player),
                        currentDuration,
                        1,
                        SystemClock.elapsedRealtime())
                .build();
        mediaSession.setPlaybackState(state);
        return state;
    }

    private void subscribePlayingStatusListener() {
        soundsController.setOnPlayingStatusForServiceListener(isPlay -> {
            if (!isPlay) {
                Log.e(TAG, "Остановка воспроизведения");
                stopUpdateNotification();
            }
        });
    }

    private int getPlaybackState(Player player) {
        if (player.isPlaying()) return PlaybackStateCompat.STATE_PLAYING;
        return PlaybackStateCompat.STATE_PAUSED;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        changeStateMode(intent);
        changeRating(intent);
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void changeRating(Intent intent) {
        if ("RATING_LIKE".equals(intent.getAction())) {
            soundsController.changeRating();
            Log.e(TAG, "Текущий рейтинг: Like");
            createNotification(pos, stateMode, 1);
        } else if ("RATING_UNLIKE".equals(intent.getAction())) {
            soundsController.changeRating();
            Log.e(TAG, "Текущий рейтинг: Unlike");
            createNotification(pos, stateMode, 0);
        }
    }


    private void changeStateMode(Intent intent) {
        if (StateMode.REPEAT.toString().equals(intent.getAction()) ||
                StateMode.LOOP.toString().equals(intent.getAction()) ||
                StateMode.RANDOM.toString().equals(intent.getAction())) {
            Log.d(TAG, "Смена режима воспроизведения");
            stateMode = soundsController.switchMode();
            soundsController.clearDequeSoundtrack();
            createNotification(pos, stateMode, ratingCurrentSoundtrack);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Событие привязки к сервису");
        return binderService;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Событие отвязки от сервиса");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "Событие повторной привязки к сервису");
        super.onRebind(intent);
    }

    private Handler handler = new Handler();
    private Runnable runnable;
    private int pos;
    private int ratingCurrentSoundtrack;
    private StateMode stateMode;

    public void createNotification(int position, StateMode mode, int rating) {
        Log.d(TAG, "Создание/обновление Notification");
        handler.removeCallbacks(runnable);
        pos = position;
        ratingCurrentSoundtrack = rating;
        stateMode = mode;
        MediaMetadataCompat mediaMetadataCompat = getMetadata(position);
        PlaybackStateCompat.Builder builderState = getBuilderState();
        runnable = new Runnable() {
            @Override
            public void run() {
                Notification notification = mediaNotificationManager.getNotification(
                        mediaMetadataCompat,
                        getState(builderState),
                        mediaSession.getSessionToken(),
                        mode,
                        ratingCurrentSoundtrack);
                startForeground(NOTIFICATION_ID, notification);
                handler.postDelayed(this, 500);
                if (!isScreenOn) stopUpdateNotification();
            }
        };
        handler.postDelayed(runnable, 0);
        createCallbacksMediaSession(position, mode);
    }

    public void stopUpdateNotification() {
        Log.d(TAG, "Остановка обновлений Notification");
        handler.removeCallbacks(runnable);
    }

    long currentDuration;

    private void createCallbacksMediaSession(int position, StateMode mode) {
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                Log.d(TAG, "onSeekTo");
                soundsController.getSoundtrackPlayer().setCurrentTime((int) pos);
            }

            @Override
            public void onPlay() {
                Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PLAY");
                soundsController.playOrPause();
                createNotification(position, mode, ratingCurrentSoundtrack);
                super.onPlay();
            }

            @Override
            public void onPause() {
                Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PAUSE");
                soundsController.playOrPause();
                createNotification(position, mode, ratingCurrentSoundtrack);
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_NEXT");
                createNotification(soundsController.next(), mode, ratingCurrentSoundtrack);
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PREVIOUS");
                createNotification(soundsController.previous(), mode, ratingCurrentSoundtrack);
                super.onSkipToPrevious();
            }
        });
        mediaSession.setActive(true);
    }

    public class BinderService extends Binder {
        public MediaSessionService getService() {
            return MediaSessionService.this;
        }
    }

    @Override
    public void onDestroy() {
        if (soundsController.getSoundtrackPlayer().isPlaying()) {
            soundsController.loseAudioFocusAndStopPlayer();
            Log.d(TAG, "Уничтожение службы");
        }
        unregisterReceiver(noisyBroadcastReceiver);
        unregisterReceiver(screenBroadcastReceiver);
        super.onDestroy();
    }
}
