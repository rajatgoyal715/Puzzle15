package com.rajatgoyal.puzzle15.util;

import android.app.Activity;

import com.google.android.gms.games.AchievementsClient;
import com.rajatgoyal.puzzle15.R;

/**
 * Created by rajatgoyal715 on 20/2/19.
 */
public class AchievementHandler {

	private void unlockOnUIThread(final AchievementsClient achievementsClient, Activity activity, final String achievementSring) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				achievementsClient.unlock(achievementSring);
			}
		});
	}

	public void checkPlayedGames(AchievementsClient achievementsClient, Activity activity) {
		int playedGames = SharedPref.getPlayedGames();
		if (playedGames >= 1){
			unlockOnUIThread(achievementsClient, activity, activity.getString(R.string.achievement_play_your_first_game));
		}
	}
}
