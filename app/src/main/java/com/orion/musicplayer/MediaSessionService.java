package com.orion.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

public class MediaSessionService extends Service {

    interface OnStartClickListener {
        void onStartClick();
    }

    interface OnPauseClickListener {
        void onPauseClick();
    }

    interface OnNextClickListener {
        void onNextClick();
    }

    interface OnPreviousClickListener {
        void onPreviousClick();
    }

    private static final String TAG = MediaSessionService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 888;

    private MediaNotificationManager mediaNotificationManager;
    private MediaSessionCompat mediaSession;
    private BinderService binderService = new BinderService();

    private OnStartClickListener onStartClickListener;
    private OnPauseClickListener onPauseClickListener;
    private OnNextClickListener onNextClickListener;
    private OnPreviousClickListener onPreviousClickListener;

    public void setOnStartClickListener(OnStartClickListener onStartClickListener) {
        this.onStartClickListener = onStartClickListener;
    }

    public void setOnPauseClickListener(OnPauseClickListener onPauseClickListener) {
        this.onPauseClickListener = onPauseClickListener;
    }

    public void setOnNextClickListener(OnNextClickListener onNextClickListener) {
        this.onNextClickListener = onNextClickListener;
    }

    public void setOnPreviousClickListener(OnPreviousClickListener onPreviousClickListener) {
        this.onPreviousClickListener = onPreviousClickListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaNotificationManager = new MediaNotificationManager(this);
        mediaSession = new MediaSessionCompat(this, "PlayerService", null,
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE));

        Notification notification = mediaNotificationManager.getNotification(
                getMetadata(), getState(), mediaSession.getSessionToken());
        mediaSession.setCallback(new MediaSessionCompat.Callback() {

            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                System.out.println("onMediaButtonEvent called: ****************************************************** " + mediaButtonIntent);
                return false;
            }

            @Override
            public void onPlay() {
                System.out.println("onPlay");

            }

            @Override
            public void onPause() {
                System.out.println("onPause");

            }

            @Override
            public void onSkipToNext() {
                System.out.println("onSkipToNext");
            }

            @Override
            public void onSkipToPrevious() {
                System.out.println("onSkipToPrevious");
            }

            @Override
            public void onSeekTo(long pos) {
//                soundtrackPlayer.setCurrentDuration((int) pos);
            }
        });

        mediaSession.setActive(true);
        startForeground(NOTIFICATION_ID, notification);
    }

    public MediaMetadataCompat getMetadata() {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "artist");
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "title");
//        builder.putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING, newUnratedRating(RatingCompat.RATING_5_STARS));
//        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, soundtrackPlayer.getCurrentTime());
        return builder.build();
    }

    private PlaybackStateCompat getState() {

//        boolean value = soundtrackPlayerModel.getIsPlayingLiveData().getValue();


//        long actions = true ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY;
//        int state = true ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;


        long actions = PlaybackStateCompat.ACTION_PLAY;
        int state = PlaybackStateCompat.STATE_PAUSED;

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(actions);
        stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
        return stateBuilder.build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PAUSE");
                    onPauseClickListener.onPauseClick();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PLAY");
                    onStartClickListener.onStartClick();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_NEXT");
                    onNextClickListener.onNextClick();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Log.d(TAG, "KeyEvent.KEYCODE_MEDIA_PREVIOUS");
                    onPreviousClickListener.onPreviousClick();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
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
        stopForeground(true);
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "Событие повторной привязки к сервису");
        super.onRebind(intent);
    }

    class BinderService extends Binder {
        MediaSessionService getService() {
            return MediaSessionService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Уничтожение службы");
        super.onDestroy();
    }
}
