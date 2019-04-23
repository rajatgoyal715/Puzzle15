package com.rajatgoyal.puzzle15.model;

/**
 * @author rajatgoyal715
 */
public class GamePlay {
    private int score;
    private int moves;
    private Time time;

    public GamePlay(int moves, Time time) {
        this.moves = moves;
        this.time = time;
        this.score = (int)((double)100 * Math.exp(- (double) (moves + time.toSeconds()) / (double) 100));
    }

    public GamePlay(int score, int moves, Time time) {
        this.score = score;
        this.moves = moves;
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public int getMoves() {
        return moves;
    }

    public Time getTime() {
        return time;
    }
}
