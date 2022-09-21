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
import com.orion.musicplayer.models.Song;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
    public interface OnSoundtrackClickListener {
        void onSoundtrackClick(Song song, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textSongTitle;
        final TextView textSongArtist;
        final Button buttonSongPlay;
        final Button buttonSongSettings;

        ViewHolder(View view) {
            super(view);
            textSongTitle = view.findViewById(R.id.soundtrack_title_list);
            textSongArtist = view.findViewById(R.id.soundtrack_artist_list);
            buttonSongPlay = view.findViewById(R.id.play_music_list);
            buttonSongSettings = view.findViewById(R.id.soundtrack_settings);
        }
    }

    private static final String TAG = SongListAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private final List<Song> songList;
    private final OnSoundtrackClickListener onClickListener;
    private final Animation buttonAnimationClick;
    private final ExecutorService executorService;
    private boolean[] isPlaying;

    public SongListAdapter(
            Context context,
            List<Song> songList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.songList = songList;
        this.onClickListener = onClickListener;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
        isPlaying = new boolean[songList.size()];
        executorService = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changePlayingStatus(int position, boolean bool) {
        isPlaying = new boolean[songList.size()];
        isPlaying[position] = bool;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.soundtrack_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (songList != null && songList.size() > 0){
            Song song = songList.get(position);
            updateRecycleView(holder, position);
            holder.buttonSongPlay.setOnClickListener(view -> {
                executorService.execute(() -> holder.buttonSongPlay.startAnimation(buttonAnimationClick));
                onClickListener.onSoundtrackClick(song, position);
                if (isPlaying[position]) {
                    holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_pause);
                } else {
                    holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_play);
                }
            });
            stylizedData(holder, song);
        }

        holder.itemView.setOnClickListener(view -> {
            //TODO открытие подробной справки
            //onClickListener.onSoundtrackClick(soundtrack, position);
        });

        holder.buttonSongSettings.setOnClickListener(view -> {
            holder.buttonSongSettings.startAnimation(buttonAnimationClick);
            //TODO открытие меню действий
        });

    }

    private void updateRecycleView(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Обновить RecycleView");
        if (isPlaying[position]) {
            holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_pause);
            holder.buttonSongPlay.startAnimation(buttonAnimationClick);
        } else {
            holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void stylizedData(@NonNull ViewHolder holder, Song song) {
        Log.d(TAG, "Стилизовать TextViews");
        if (song.getArtist().equalsIgnoreCase("<unknown>")) {
            holder.textSongArtist.setText(song.getTitle());
            holder.textSongTitle.setText("********");
        } else {
            holder.textSongArtist.setText(song.getArtist());
            holder.textSongTitle.setText(song.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
