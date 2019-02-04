package com.rajatgoyal.puzzle15.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.model.HighScore;

import java.util.ArrayList;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScoresAdapter extends RecyclerView.Adapter<HighScoresAdapter.HighScoreViewHolder> {

    private ArrayList<HighScore> highScores;
    private int count;
    private Context context;

    @Override
    public HighScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.high_score_list_item, parent, false);
        return new HighScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HighScoreViewHolder holder, int position) {
        position = count - 1 - position;
        String movesString = Integer.toString(highScores.get(position).getMoves());
        holder.moves.setText(movesString);
        holder.timer.setText(highScores.get(position).getTime().toString());
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setHighScores(ArrayList<HighScore> highScores) {
        this.highScores = highScores;
        this.count = highScores.size();
    }

    class HighScoreViewHolder extends RecyclerView.ViewHolder {

        TextView moves, timer;

        public HighScoreViewHolder(View itemView) {
            super(itemView);

            moves = (TextView) itemView.findViewById(R.id.moves_hs);
            timer = (TextView) itemView.findViewById(R.id.timer_hs);
        }
    }
}
