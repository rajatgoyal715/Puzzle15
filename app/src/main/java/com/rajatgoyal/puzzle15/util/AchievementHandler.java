package com.rajatgoyal.puzzle15.util;

import android.app.Activity;

import com.google.android.gms.games.AchievementsClient;
import com.rajatgoyal.puzzle15.R;

import timber.log.Timber;

/**
 * Created by rajatgoyal715 on 20/2/19.
 */
public class AchievementHandler {

	private AchievementsClient client;

	public AchievementHandler(AchievementsClient achievementsClient) {
		this.client = achievementsClient;
	}

	private void unlockOnUIThread(final Activity activity, final int achievementId) {
		final AchievementsClient achievementsClient = this.client;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				achievementsClient.unlock(activity.getString(achievementId));
			}
		});
	}

	public void checkPlayedGames(Activity activity) {
		if(this.client == null) {
			Timber.d("Please login first");
			return;
		}
		int playedGames = SharedPref.getPlayedGames();
		if (playedGames >= 1) {
			unlockOnUIThread(activity, R.string.achievement_play_your_first_game);
		}
	}
}
