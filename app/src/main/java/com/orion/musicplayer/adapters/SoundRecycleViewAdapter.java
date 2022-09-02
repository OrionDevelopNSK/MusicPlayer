package com.orion.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;

public class SoundRecycleViewAdapter extends RecyclerView.Adapter<SoundRecycleViewAdapter.ViewHolder> {

    public interface OnSoundtrackClickListener{
        void onSoundtrackClick(Soundtrack soundtrack, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textViewSoundtrackTitle;
        final TextView textViewSoundtrackArtist;
        final Button musicButton;

        ViewHolder(View view){
            super(view);
            textViewSoundtrackTitle = view.findViewById(R.id.soundtrack_title_list);
            textViewSoundtrackArtist = view.findViewById(R.id.soundtrack_artist_list);
            musicButton = view.findViewById(R.id.playMusicButton);
        }
    }

    private final LayoutInflater layoutInflater;
    private final List<Soundtrack> soundtrackList;
    private final OnSoundtrackClickListener onClickListener;

    public SoundRecycleViewAdapter(
            Context context,
            List<Soundtrack> soundtrackList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.soundtrackList = soundtrackList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SoundRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SoundRecycleViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Soundtrack soundtrack = soundtrackList.get(position);


        if (soundtrack.getArtist().equalsIgnoreCase("<unknown>")){
            holder.textViewSoundtrackArtist.setText(soundtrack.getTitle());
            holder.textViewSoundtrackTitle.setText("********");
        }
        else{
            holder.textViewSoundtrackArtist.setText(soundtrack.getArtist());
            holder.textViewSoundtrackTitle.setText(soundtrack.getTitle());
        }



        holder.musicButton.setOnClickListener(view -> onClickListener.onSoundtrackClick(soundtrack, position));

        holder.itemView.setOnClickListener(view -> {
            //onClickListener.onSoundtrackClick(soundtrack, position);
        });
    }

    @Override
    public int getItemCount() {
        return soundtrackList.size();
    }
}
