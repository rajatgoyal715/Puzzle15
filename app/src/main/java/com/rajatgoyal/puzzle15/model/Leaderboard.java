package com.rajatgoyal.puzzle15.model;

/**
 * Created by rajat on 16/9/17.
 */

public class Leaderboard {
    String name;
    int moves;
    int time;

    public Leaderboard(String name, int moves, int time) {
        this.name = name;
        this.moves = moves;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public int getMoves() {
        return moves;
    }

    public int getTime() {
        return time;
    }
}
