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

	private static int[] PLAYED_IDS = {
        R.string.achievement_newbie,
    };
	private static int[] COMPLETED_IDS = {
        R.string.achievement_novice__1_game,
        R.string.achievement_beginner__5_games,
        R.string.achievement_skilled__10_games,
        R.string.achievement_proficient__15_games,
        R.string.achievement_experienced__20_games,
        R.string.achievement_advanced__30_games,
        R.string.achievement_expert__50_games,
        R.string.achievement_master__80_games,
        R.string.achievement_legend__100_games
    };

	public AchievementHandler(AchievementsClient achievementsClient) {
		this.client = achievementsClient;
	}

	private void unlockOnUIThread(final Activity activity, final int achievementId) {
        if (this.client == null) {
            Timber.d("Please login first");
            return;
        }
		final AchievementsClient achievementsClient = this.client;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				achievementsClient.unlock(activity.getString(achievementId));
			}
		});
	}

	private void incrementAchievement(final Activity activity, final int achievementId) {
        if (this.client == null) {
            Timber.d("Please login first");
            return;
        }
	    this.client.increment(activity.getString(achievementId), 1);
    }

	public void unlockPlayedGamesAchievements(Activity activity) {
		int playedGames = SharedPref.getPlayedGames();
		if (playedGames >= 1) {
		    unlockOnUIThread(activity, PLAYED_IDS[0]);
		}
	}

    public void unlockCompletedGamesAchievements(Activity activity) {
        int completedGames = SharedPref.getCompletedGames();
        if (completedGames >= 1) {
            unlockOnUIThread(activity, COMPLETED_IDS[0]);
        }
        for (int id = 0; id < COMPLETED_IDS.length; id++) {
            incrementAchievement(activity, COMPLETED_IDS[id]);
        }
    }

	public void unlockRulesAchievements(Activity activity) {
		unlockOnUIThread(activity, R.string.achievement_knower);
	}
}
