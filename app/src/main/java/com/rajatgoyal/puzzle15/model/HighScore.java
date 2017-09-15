package com.rajatgoyal.puzzle15.model;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScore {
    private int moves;
    private Time time;

    public HighScore(int moves, Time time) {
        this.moves = moves;
        this.time = time;
    }

    public int getMoves() {
        return moves;
    }

    public Time getTime() {
        return time;
    }
}
