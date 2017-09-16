package com.rajatgoyal.puzzle15.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.model.Leaderboard;
import com.rajatgoyal.puzzle15.model.Time;

import java.util.ArrayList;

/**
 * Created by rajat on 16/9/17.
 */

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>{

    private ArrayList<Leaderboard> leaderboard;
    private int count;
    private Context context;

    @Override
    public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false);
        return new LeaderboardAdapter.LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
        holder.name.setText(leaderboard.get(position).getName());
        String movesString = "Moves : " + leaderboard.get(position).getMoves();
        String timeString = "Time : " + new Time(leaderboard.get(position).getTime()).toString();
        holder.moves.setText(movesString);
        holder.time.setText(timeString);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setLeaderboard(ArrayList<Leaderboard> leaderboard) {
        this.leaderboard = leaderboard;
        this.count = leaderboard.size();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        TextView name, moves, time;

        public LeaderboardViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.username);
            moves = (TextView) itemView.findViewById(R.id.moves);
            time = (TextView) itemView.findViewById(R.id.time);
        }
    }
}
