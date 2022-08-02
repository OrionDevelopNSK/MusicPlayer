package com.orion.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SoundtrackAdapter extends RecyclerView.Adapter<SoundtrackAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final List<Soundtrack> soundtrackList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textView;

        ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.item);
        }
    }

    public SoundtrackAdapter(Context context, List<Soundtrack> soundtrackList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.soundtrackList = soundtrackList;
    }

    @NonNull
    @Override
    public SoundtrackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SoundtrackAdapter.ViewHolder holder, int position) {
        Soundtrack soundtrack = soundtrackList.get(position);
        holder.textView.setText(soundtrack.getData() + "\n"
                                + soundtrack.getId() + "\n"
                                + soundtrack.getTitle() + "\n"
                                + soundtrack.getArtist() + "\n"
                                + soundtrack.getDuration() + "\n"
                                + soundtrack.getRating() + "\n"
                                + soundtrack.getCountOfLaunches() + "\n");
    }

    @Override
    public int getItemCount() {
        return soundtrackList.size();
    }
}
