package com.orion.musicplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.utils.StateMode;


/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
public class MediaNotificationManager {
    private static final String TAG = MediaNotificationManager.class.getSimpleName();
    private static final String CHANNEL_ID = "com.example.android.musicplayer.channel";
    private static final int REQUEST_CODE = 501;
    public static final int NOTIFICATION_ID = 412;

    private final MediaSessionService service;

    private final NotificationCompat.Action playAction;
    private final NotificationCompat.Action pauseAction;
    private final NotificationCompat.Action nextAction;
    private final NotificationCompat.Action previousAction;
    private final NotificationCompat.Action switchModeRatingAction;
    private final NotificationCompat.Action switchModeLoopAction;
    private final NotificationCompat.Action switchModeRepeatAction;
    private final NotificationCompat.Action switchModeRandomAction;

    private final NotificationManager notificationManager;

    @SuppressLint("RestrictedApi")
    public MediaNotificationManager(MediaSessionService musicContext) {
        service = musicContext;
        notificationManager = (NotificationManager) service.getSystemService(Service.NOTIFICATION_SERVICE);

        playAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_play_24,
                "play",
                getPendingIntentStart()).build();

        pauseAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_pause_24,
                "pause",
                getPendingIntentPause()).build();

        nextAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_next_24,
                "Previous",
                getPendingIntentNext()).build();

        previousAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_previous_24,
                "Next",
                getPendingIntentPrevious()).build();

        switchModeLoopAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_loop_24,
                "LoopMode",
                getPendingIntentLoop()).build();

        switchModeRatingAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_unlike_24,
                "Rating",
                getPendingIntentRating()).build();

        switchModeRepeatAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_repeat_one_24,
                "RepeatMode",
                getPendingIntentRepeatOne()).build();

        switchModeRandomAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_random_24,
                "RandomMode",
                getPendingIntentRandom()).build();

        notificationManager.cancelAll();
    }

    private PendingIntent getPendingIntentPause() {
        return createPendingIntent(PlaybackStateCompat.ACTION_PAUSE);
    }

    private PendingIntent getPendingIntentStart() {
        return createPendingIntent(PlaybackStateCompat.ACTION_PLAY);
    }

    private PendingIntent getPendingIntentStop() {
        return createPendingIntent(PlaybackStateCompat.ACTION_STOP);
    }

    private PendingIntent getPendingIntentNext() {
        return createPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
    }

    private PendingIntent getPendingIntentPrevious() {
        return createPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    }

    @SuppressLint("RestrictedApi")
    private PendingIntent getPendingIntentRandom() {
        Intent intent = new Intent(service, MediaSessionService.class);
        intent.setAction(StateMode.RANDOM.toString());
        return  PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent getPendingIntentLoop() {
        Intent intent = new Intent(service, MediaSessionService.class);
        intent.setAction(StateMode.LOOP.toString());
        return  PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent getPendingIntentRepeatOne() {
        Intent intent = new Intent(service, MediaSessionService.class);
        intent.setAction(StateMode.REPEAT.toString());
        return  PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent getPendingIntentRating() {
        Intent intent = new Intent(service, MediaSessionService.class);
        intent.setAction("RATING");
        return  PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }


    private NotificationCompat.Action changeStateMode(StateMode stateMode){
        switch (stateMode) {
            case REPEAT:
                return switchModeRepeatAction;
            case RANDOM:
                return switchModeRandomAction;
            default:
                return switchModeLoopAction;
        }
    }


    private PendingIntent createPendingIntent(Long actionCode) {
        int keyCode = PlaybackStateCompat.toKeyCode(actionCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        return PendingIntent.getService(service, keyCode, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public Notification getNotification(MediaMetadataCompat metadata,
                                        @NonNull PlaybackStateCompat state,
                                        MediaSessionCompat.Token token,
                                        StateMode stateMode) {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder = buildNotification(token, isPlaying, description, stateMode);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(MediaSessionCompat.Token token,
                                                         boolean isPlaying,
                                                         MediaDescriptionCompat description,
                                                         StateMode stateMode) {

        // Create the (mandatory) notification channel when running on Android Oreo.
        if (isAndroidOOrHigher()) {
            createChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, CHANNEL_ID);
        builder.setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(token)
                                .setShowActionsInCompactView(0)
                                .setShowCancelButton(true)
                                .setCancelButtonIntent(getPendingIntentStop()))
                .setColor(ContextCompat.getColor(service, com.google.android.material.R.color.design_default_color_primary))
                .setSmallIcon(R.drawable.ic_play_24)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setDeleteIntent(getPendingIntentPause());

        builder
                .addAction(switchModeRatingAction)
                .addAction(previousAction)
                .addAction(isPlaying ? pauseAction : playAction)
                .addAction(nextAction)
                .addAction(changeStateMode(stateMode));
        return builder;
    }

    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            CharSequence name = "MediaSession";
            // The user-visible description of the channel.
            String description = "MediaSession and MediaPlayer";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "createChannel: New channel created");
        } else {
            Log.d(TAG, "createChannel: Existing channel reused");
        }
    }

    private boolean isAndroidOOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(service, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                service, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

}
