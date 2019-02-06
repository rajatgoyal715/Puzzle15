package com.rajatgoyal.puzzle15.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rajatgoyal715 on 6/2/19.
 */
public class SharedPref {

	private static final String GAME_PREF = "GAME_PREF";
	private static final String PLAYED_GAMES = "PLAYED_GAMES";

	private static SharedPreferences gamePref;

	private SharedPref() {}

	public static void init(Context context) {
		gamePref = context.getSharedPreferences(GAME_PREF, Context.MODE_PRIVATE);
	}

	// -------------------------- Getters and Setters --------------------------------

	/**
	 * Get number of already played games using gamePref
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
}
