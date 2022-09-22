package com.orion.musicplayer.ui.adapters;

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
import com.orion.musicplayer.data.models.Playlist;

import java.util.List;

public class PlaylistDetailListAdapter extends RecyclerView.Adapter<PlaylistDetailListAdapter.ViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist, int position);
    }

    public interface OnSettingsPlaylistClickListener {
        void onSettingsPlaylistClick(View view, Playlist playlist);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textPlaylistName;
        final TextView textPlayCapacity;
        final Button buttonOpenPlaylist;
        final Button buttonPlaylistSettings;
        final String countOfSoundtracks;

        ViewHolder(View view){
            super(view);
            textPlaylistName = view.findViewById(R.id.playlist_name);
            textPlayCapacity = view.findViewById(R.id.playlist_capacity);
            buttonOpenPlaylist = view.findViewById(R.id.open_playlist);
            buttonPlaylistSettings = view.findViewById(R.id.playlist_settings);
            countOfSoundtracks = view.getResources().getString(R.string.count_of_soundtracks);
        }
    }

    private final LayoutInflater layoutInflater;
    private final List<Playlist> playlistList;
    private final OnPlaylistClickListener onPlaylistClickListener;
    private final OnSettingsPlaylistClickListener onSettingsPlaylistClickListener;
    private final Animation buttonAnimationClick;
    private final List<String> capacityList;

    public PlaylistDetailListAdapter(
            Context context,
            List<Playlist> playlistList,
            List<String> capacityList,
            OnPlaylistClickListener onPlaylistClickListener,
            OnSettingsPlaylistClickListener onSettingsPlaylistClickListener
            ) {
        this.layoutInflater = LayoutInflater.from(context);
        this.playlistList = playlistList;
        this.capacityList = capacityList;
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.onSettingsPlaylistClickListener = onSettingsPlaylistClickListener;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
    }

    @NonNull
    @Override
    public PlaylistDetailListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_playlist_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlaylistDetailListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Playlist playlist = playlistList.get(position);
        holder.textPlaylistName.setText(playlist.getPlaylistName());
        holder.textPlayCapacity.setText(holder.countOfSoundtracks + "" + capacityList.get(position));
        holder.buttonOpenPlaylist.setOnClickListener(view -> {
            holder.buttonOpenPlaylist.startAnimation(buttonAnimationClick);
            onPlaylistClickListener.onPlaylistClick(playlist, position);
        });
        holder.buttonPlaylistSettings.setOnClickListener(view -> {
            holder.buttonPlaylistSettings.startAnimation(buttonAnimationClick);
            onSettingsPlaylistClickListener.onSettingsPlaylistClick(holder.buttonPlaylistSettings, playlist);
        });

    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }
}
