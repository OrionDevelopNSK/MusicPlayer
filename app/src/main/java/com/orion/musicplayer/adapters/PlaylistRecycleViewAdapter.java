package com.orion.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.orion.musicplayer.models.Playlist;

import java.util.List;

public class PlaylistRecycleViewAdapter extends RecyclerView.Adapter<PlaylistRecycleViewAdapter.ViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textViewPlaylistName;
        final TextView textViewPlayCapacity;
        final Button playlistButton;
        final Button playlistSettings;
        final String countOfSoundtracks;

        ViewHolder(View view){
            super(view);
            textViewPlaylistName = view.findViewById(R.id.playlist_name);
            textViewPlayCapacity = view.findViewById(R.id.playlist_capacity);
            playlistButton = view.findViewById(R.id.open_playlist);
            playlistSettings = view.findViewById(R.id.playlist_settings);
            countOfSoundtracks = view.getResources().getString(R.string.count_of_soundtracks);
        }
    }

    private final LayoutInflater layoutInflater;
    private final List<Playlist> playlistList;
    private final OnPlaylistClickListener onClickListener;
    private final Animation buttonAnimationClick;
    private final List<String> capacityList;

    public PlaylistRecycleViewAdapter(
            Context context,
            List<Playlist> playlistList,
            OnPlaylistClickListener onClickListener,
            List<String> capacityList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.playlistList = playlistList;
        this.onClickListener = onClickListener;
        this.capacityList = capacityList;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
    }

    @NonNull
    @Override
    public PlaylistRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.playlist_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlaylistRecycleViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Playlist playlist = playlistList.get(position);
        holder.textViewPlaylistName.setText(playlist.getPlaylistName());
        holder.textViewPlayCapacity.setText(holder.countOfSoundtracks + "" + capacityList.get(position));
        holder.playlistButton.setOnClickListener(view -> {
            holder.playlistButton.startAnimation(buttonAnimationClick);
            //TODO

        });


        holder.playlistSettings.setOnClickListener(view -> {
            holder.playlistSettings.startAnimation(buttonAnimationClick);
            //TODO
        });

    }



    @Override
    public int getItemCount() {
        return playlistList.size();
    }
}
