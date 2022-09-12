package com.orion.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundRecycleViewAdapter extends RecyclerView.Adapter<SoundRecycleViewAdapter.ViewHolder> {

    public interface OnSoundtrackClickListener {
        void onSoundtrackClick(Soundtrack soundtrack, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewSoundtrackTitle;
        final TextView textViewSoundtrackArtist;
        final Button musicButton;
        final Button soundtrackSettings;

        ViewHolder(View view) {
            super(view);
            textViewSoundtrackTitle = view.findViewById(R.id.soundtrack_title_list);
            textViewSoundtrackArtist = view.findViewById(R.id.soundtrack_artist_list);
            musicButton = view.findViewById(R.id.play_music_list);
            soundtrackSettings = view.findViewById(R.id.soundtrack_settings);
        }
    }

    private static final String TAG = SoundRecycleViewAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private final List<Soundtrack> soundtrackList;
    private final OnSoundtrackClickListener onClickListener;
    private final Animation buttonAnimationClick;
    private final ExecutorService executorService;
    private boolean[] isPlaying;

    public SoundRecycleViewAdapter(
            Context context,
            List<Soundtrack> soundtrackList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.soundtrackList = soundtrackList;
        this.onClickListener = onClickListener;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
        isPlaying = new boolean[soundtrackList.size()];
        executorService = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changePlayingStatus(int position, boolean bool) {
        isPlaying = new boolean[soundtrackList.size()];
        isPlaying[position] = bool;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public SoundRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.soundtrack_list_item, parent, false);
        return new ViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SoundRecycleViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (soundtrackList != null && soundtrackList.size() > 0){
            Soundtrack soundtrack = soundtrackList.get(position);
            updateRecycleView(holder, position);
            holder.musicButton.setOnClickListener(view -> {
                executorService.execute((Runnable) () -> holder.musicButton.startAnimation(buttonAnimationClick));
                onClickListener.onSoundtrackClick(soundtrack, position);
                if (isPlaying[position]) {
                    holder.musicButton.setBackgroundResource(R.drawable.ic_pause);
                } else {
                    holder.musicButton.setBackgroundResource(R.drawable.ic_play);
                }
            });

            stylizedData(holder, soundtrack);
        }


        holder.itemView.setOnClickListener(view -> {
            //TODO открытие подробной справки
            //onClickListener.onSoundtrackClick(soundtrack, position);
        });

        holder.soundtrackSettings.setOnClickListener(view -> {
            holder.soundtrackSettings.startAnimation(buttonAnimationClick);
            //TODO открытие меню действий
        });



    }

    private void updateRecycleView(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Обновить RecycleView");
        if (isPlaying[position]) {
            holder.musicButton.setBackgroundResource(R.drawable.ic_pause);
            holder.musicButton.startAnimation(buttonAnimationClick);
        } else {
            holder.musicButton.setBackgroundResource(R.drawable.ic_play);
        }
    }


    private void stylizedData(@NonNull ViewHolder holder, Soundtrack soundtrack) {
        Log.d(TAG, "Стилизовать TextViews");
        if (soundtrack.getArtist().equalsIgnoreCase("<unknown>")) {
            holder.textViewSoundtrackArtist.setText(soundtrack.getTitle());
            holder.textViewSoundtrackTitle.setText("********");
        } else {
            holder.textViewSoundtrackArtist.setText(soundtrack.getArtist());
            holder.textViewSoundtrackTitle.setText(soundtrack.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return soundtrackList.size();
    }
}
