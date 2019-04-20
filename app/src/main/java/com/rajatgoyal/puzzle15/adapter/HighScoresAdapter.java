package com.rajatgoyal.puzzle15.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
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

    @NotNull
    @Override
    public HighScoreViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
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
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setHighScores(ArrayList<GamePlay> gamePlays) {
        this.gamePlays = gamePlays;
        this.count = gamePlays.size();
    }

    class HighScoreViewHolder extends RecyclerView.ViewHolder {

        TextView moves, timer, score;

        HighScoreViewHolder(View itemView) {
            super(itemView);

            moves = itemView.findViewById(R.id.moves_hs);
            timer = itemView.findViewById(R.id.timer_hs);
            score = itemView.findViewById(R.id.score);
        }
    }
}
