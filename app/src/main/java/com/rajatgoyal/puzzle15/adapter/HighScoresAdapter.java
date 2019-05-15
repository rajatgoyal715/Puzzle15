package com.rajatgoyal.puzzle15.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.model.GamePlay;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScoresAdapter extends RecyclerView.Adapter<HighScoresAdapter.HighScoreViewHolder> {

    private ArrayList<GamePlay> gamePlays;
    private int count;

    private Resources resources;

    @NotNull
    @Override
    public HighScoreViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        resources = context.getResources();
        View view = LayoutInflater.from(context).inflate(R.layout.high_score_list_item, parent, false);
        return new HighScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull HighScoreViewHolder holder, int position) {
        GamePlay gamePlay = gamePlays.get(position);
        String movesString = Integer.toString(gamePlay.getMoves());
        String scoreString = Integer.toString(gamePlay.getScore());

        holder.score.setText(scoreString);
        holder.moves.setText(movesString);
        holder.timer.setText(gamePlay.getTime().toString());

        if (position % 2 == 0) {
            holder.layout.setBackgroundColor(resources.getColor(R.color.colorPrimary));
        } else {
            holder.layout.setBackgroundColor(resources.getColor(R.color.light));
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setGamePlays(ArrayList<GamePlay> gamePlays) {
        this.gamePlays = gamePlays;
        this.count = gamePlays.size();
    }

    class HighScoreViewHolder extends RecyclerView.ViewHolder {

        View layout;
        TextView moves, timer, score;

        HighScoreViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.high_score_list_item_layout);
            moves = itemView.findViewById(R.id.moves_hs);
            timer = itemView.findViewById(R.id.timer_hs);
            score = itemView.findViewById(R.id.score);
        }
    }
}
