package com.orion.musicplayer.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.data.models.Song;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChooserDialogAdapter extends RecyclerView.Adapter<ChooserDialogAdapter.ViewHolder> {
    public interface OnSoundtrackClickListener {
        void onSoundtrackClick(Song song, int position);
    }

    public interface OnSoundTrackChoseListener {
        void onSoundTrackChose(int position, boolean isSelected);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textSongTitle;
        final Button buttonSongPlay;
        final CheckBox checkBoxSongChoice;

        ViewHolder(View view) {
            super(view);
            textSongTitle = view.findViewById(R.id.soundtrack_title_list);
            buttonSongPlay = view.findViewById(R.id.play_music_dialog);
            checkBoxSongChoice = view.findViewById(R.id.checkbox_soundtrack);
        }
    }

    private static final String TAG = ChooserDialogAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private final List<Song> songList;
    private final OnSoundtrackClickListener onClickListener;
    private final Animation buttonAnimationClick;
    private final ExecutorService executorService;
    private boolean[] checked;
    private boolean[] isPlaying;
    private OnSoundTrackChoseListener onSoundTrackChoseListener;

    public ChooserDialogAdapter(
            Context context,
            List<Song> songList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.songList = songList;
        this.onClickListener = onClickListener;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
        checked = new boolean[songList.size()];
        isPlaying = new boolean[songList.size()];
        executorService = Executors.newSingleThreadExecutor();

    }

    public void setChecked(boolean[] checked) {
        this.checked = checked;
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) onSoundTrackChoseListener.onSoundTrackChose(i, true);
        }
        notifyDataSetChanged();
    }

    public void setOnSoundTrackChoseListener(OnSoundTrackChoseListener onSoundTrackChoseListener) {
        this.onSoundTrackChoseListener = onSoundTrackChoseListener;
    }

    @NonNull
    @Override
    public ChooserDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_dialog_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ChooserDialogAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Song song = songList.get(position);
        holder.textSongTitle.setText(song.getTitle() + "\n"
                + song.getArtist());
        updateRecycleView(holder, position);
        holder.buttonSongPlay.setOnClickListener(view -> {
            executorService.execute(() -> holder.buttonSongPlay.startAnimation(buttonAnimationClick));
            onClickListener.onSoundtrackClick(song, position);
            isPlaying[position] = !isPlaying[position];
            if (isPlaying[position]) {
                isPlaying = new boolean[songList.size()];
                isPlaying[position] = true;
                notifyDataSetChanged();
                holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_pause);
            } else {
                holder.buttonSongPlay.setBackgroundResource(R.drawable.ic_play);
            }
        });

        holder.checkBoxSongChoice.setChecked(checked[position]);
        holder.checkBoxSongChoice.setOnClickListener(view -> {
            if (holder.checkBoxSongChoice.isChecked()) {
                onSoundTrackChoseListener.onSoundTrackChose(position, true);
                Log.d(TAG, String.format("Позиция %d добавлена в исписок", position));
            } else {
                onSoundTrackChoseListener.onSoundTrackChose(position, false);
                Log.d(TAG, String.format("Позиция %d удалена из исписка", position));
            }
            checked[position] = !checked[position];
            holder.checkBoxSongChoice.startAnimation(buttonAnimationClick);
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

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
