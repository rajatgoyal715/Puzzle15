package com.rajatgoyal.puzzle15.util;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.gms.games.AchievementsClient;
import com.rajatgoyal.puzzle15.R;

import timber.log.Timber;

/**
 * Created by rajatgoyal715 on 20/2/19.
 */
public class AchievementHandler {

    private AchievementsClient client;

    private static @StringRes int[] PLAYED_IDS = {
            R.string.achievement_newbie,
    };
    private static @StringRes int[] COMPLETED_IDS = {
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

    private static int[] TIMES = {600, 300, 120, 90, 60};
    private static @StringRes int[] TIME_IDS = {
            R.string.achievement_snail__10_min,
            R.string.achievement_tortoise__5_min,
            R.string.achievement_rabbit__2_min,
            R.string.achievement_deer__1_min_30_secs,
            R.string.achievement_cheetah__1_min,
    };


    private static int[] MOVES = {300, 250, 200, 150, 100};
    private static @StringRes int[] MOVES_IDS = {
            R.string.achievement_wood__300_moves,
            R.string.achievement_copper__250_moves,
            R.string.achievement_silver__200_moves,
            R.string.achievement_gold__150_moves,
            R.string.achievement_diamond__100_moves,
    };

    public AchievementHandler(AchievementsClient achievementsClient) {
        this.client = achievementsClient;
    }

    private void unlockOnUIThread(final Activity activity, final int achievementId) {
        if (this.client == null) {
            Toast.makeText(activity, "Achievement Client is null.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(activity, "Achievement Client is null.", Toast.LENGTH_SHORT).show();
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
        for (int completedId : COMPLETED_IDS) {
            incrementAchievement(activity, completedId);
        }
    }

    public void unlockRulesAchievements(Activity activity) {
        unlockOnUIThread(activity, R.string.achievement_knower);
    }

    public void unlockTimeBasedAchievements(Activity activity, int timeInSeconds) {
        int index = 0;
        while (index < TIMES.length && timeInSeconds < TIMES[index]) {
            unlockOnUIThread(activity, TIME_IDS[index]);
            index++;
        }
    }

    public void unlockMovesBasedAchievements(Activity activity, int moves) {
        int index = 0;
        while (index < MOVES.length && moves < MOVES[index]) {
            unlockOnUIThread(activity, MOVES_IDS[index]);
            index++;
        }
    }
}
