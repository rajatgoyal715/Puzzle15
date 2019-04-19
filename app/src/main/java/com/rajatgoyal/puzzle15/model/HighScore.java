package com.rajatgoyal.puzzle15.model;

/**
 * Created by rajat on 15/9/17.
 */

public class HighScore {
    private int moves;
    private Time time;
    private int score;

    public HighScore(int moves, Time time) {
        this.moves = moves;
        this.time = time;
        this.score = (int)((double)100 * Math.exp(- (double) (moves + time.toSeconds()) / (double) 100));
    }

    public int getMoves() {
        return moves;
    }

    public Time getTime() {
        return time;
    }

    public int getScore() {
        return score;
    }
}
