package com.rajatgoyal.puzzle15.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rajatgoyal.puzzle15.model.GameMatrix;

/**
 * Created by rajatgoyal715 on 6/2/19.
 */
public class SharedPref {

    private static final String GAME_PREF = "GAME_PREF";
    private static final String PLAYED_GAMES = "PLAYED_GAMES";
    private static final String GAME_MATRIX = "GAME_MATRIX";
    private static final String GAME_MOVES = "GAME_MOVES";
    private static final String GAME_TIME = "GAME_TIME";
    private static final String RESUME_GAME = "RESUME_GAME";


    private static SharedPreferences gamePref;

    private SharedPref() {
    }

    public static void init(Context context) {
        gamePref = context.getSharedPreferences(GAME_PREF, Context.MODE_PRIVATE);
    }

    // -------------------------- Getters and Setters --------------------------------

    /**
     * Get number of already played games using gamePref
     *
     * @return number of played games
     */
    private static int getPlayedGames() {
        return gamePref.getInt(PLAYED_GAMES, 0);
    }

    /**
     * Increment number of played games
     */
    public static void incrementPlayedGames() {
        int alreadyPlayedGames = getPlayedGames();
        SharedPreferences.Editor editor = gamePref.edit();
        editor.putInt(PLAYED_GAMES, alreadyPlayedGames + 1);
        editor.apply();
    }

    /**
     * @return saved game matrix
     */
    public static GameMatrix getGameMatrix() {
        return new GameMatrix(gamePref.getString(GAME_MATRIX, ""));
    }

    /**
     * save game matrix
     *
     * @param gameMatrix game matrix
     */
    public static void setGameMatrix(GameMatrix gameMatrix) {
        gamePref.edit().putString(GAME_MATRIX, gameMatrix.toString()).apply();
    }

    /**
     * @return saved game moves
     */
    public static int getMoves() {
        return gamePref.getInt(GAME_MOVES, 0);
    }

    /**
     * save game moves
     *
     * @param moves game moves
     */
    public static void setMoves(int moves) {
        gamePref.edit().putInt(GAME_MOVES, moves).apply();
    }

    /**
     * @return saved game time
     */
    public static long getGameTime() {
        return gamePref.getLong(GAME_TIME, 0);
    }

    /**
     * save game time
     *
     * @param gameTime game time
     */
    public static void setGameTime(long gameTime) {
        gamePref.edit().putLong(GAME_TIME, gameTime).apply();
    }

    public static boolean getResumeGame() {
        return gamePref.getBoolean(RESUME_GAME, false);
    }

    public static void setResumeGame(boolean resumeGame) {
        gamePref.edit().putBoolean(RESUME_GAME, resumeGame).apply();
    }
}
