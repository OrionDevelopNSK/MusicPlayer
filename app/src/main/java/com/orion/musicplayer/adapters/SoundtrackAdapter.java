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
import com.orion.musicplayer.viewmodels.SoundtracksModel;

import java.util.List;

public class SoundtrackAdapter extends RecyclerView.Adapter<SoundtrackAdapter.ViewHolder> {

    public interface OnSoundtrackClickListener{
        void onSoundtrackClick(Soundtrack soundtrack, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textView;
        final Button musicButton;

        ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.item);
            musicButton = view.findViewById(R.id.playMusicButton);
        }
    }

    private final LayoutInflater layoutInflater;
    private final List<Soundtrack> soundtrackList;
    private final OnSoundtrackClickListener onClickListener;

    public SoundtrackAdapter(
            Context context,
            List<Soundtrack> soundtrackList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.soundtrackList = soundtrackList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SoundtrackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SoundtrackAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Soundtrack soundtrack = soundtrackList.get(position);
        holder.textView.setText(soundtrack.getData() + "\n"
                                + soundtrack.getTitle() + "\n"
                                + soundtrack.getArtist() + "\n"
                                + soundtrack.getRating() + "\n"
                                + soundtrack.getCountOfLaunches() + "\n");


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
