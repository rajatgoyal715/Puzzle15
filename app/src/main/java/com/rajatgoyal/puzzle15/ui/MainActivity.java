package com.rajatgoyal.puzzle15.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.adapter.HighScoresAdapter;
import com.rajatgoyal.puzzle15.model.HighScore;
import com.rajatgoyal.puzzle15.model.Time;
import com.rajatgoyal.puzzle15.task.HighScoreFetchTask;
import com.rajatgoyal.puzzle15.task.LatestHighScoreFetchTask;
import com.rajatgoyal.puzzle15.util.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

/**
 * Created by rajat on 15/9/17.
 */

public class MainActivity extends AppCompatActivity {

    private ArrayList<HighScore> highScores;
    private HighScore latestHighScore;

    private GoogleSignInClient googleSignInClient = null;
    private static final int RC_SIGN_IN = 9001;

    private Button signInSignOutButton, resumeBtn;
    private boolean signedIn = false;

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseInit();
        init();
        getLatestHighScore();
    }

    private void firebaseInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean resumeGame = SharedPref.getResumeFlag();
        if (resumeGame) {
            resumeBtn.setVisibility(View.VISIBLE);
        } else {
            resumeBtn.setVisibility(View.GONE);
        }
        if (isSignedIn()) return;

        signInSilently();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    public void signInSilently() {
        Timber.d("signInSilently()");

        googleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Timber.d("signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Timber.d("signInSilently(): failure");
                            onDisconnected();
                        }
                    }
                });
    }

    private void startSignInIntent() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void signOut() {
        Timber.d("signOut()");

        if (!isSignedIn()) {
            Timber.d("signOut() called, but was not signed in!");
            return;
        }

        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        Timber.d("signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "Sign In Failed Message";
                }
                Timber.d(message);
                onDisconnected();
            }
        }
    }

    GoogleSignInAccount signedInAccount;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Timber.d("onConnected(): connected to Google APIs");
        signedIn = true;
        updateSignInSignOutButton();

        if (signedInAccount != googleSignInAccount) {
            signedInAccount = googleSignInAccount;
        }

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            displayName = "???";
                        }
                        Toast.makeText(MainActivity.this, displayName, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onDisconnected() {
        Timber.d("onDisconnected()");
        signedIn = false;
        updateSignInSignOutButton();

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mEventsClient = null;
        mPlayersClient = null;
    }

    public void updateSignInSignOutButton() {
        signInSignOutButton.setText(signedIn ? R.string.signOut : R.string.signIn);
    }

    @SuppressLint("StaticFieldLeak")
    public void getLatestHighScore() {
        new LatestHighScoreFetchTask(this) {
            @Override
            protected void onPostExecute(HighScore highScore) {
                super.onPostExecute(highScore);
                setLatestHighScore(highScore);
            }
        }.execute();
    }

    public void setLatestHighScore(HighScore latestHighScore) {
        if (latestHighScore == null) {
            this.latestHighScore = new HighScore(Integer.MAX_VALUE, new Time(Integer.MAX_VALUE));
        } else {
            this.latestHighScore = latestHighScore;
        }
    }

    public void init() {
        SharedPref.init(this);

        resumeBtn = findViewById(R.id.resume_game);
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameActivity();
            }
        });


        Button newGame = findViewById(R.id.new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean resumeGame = SharedPref.getResumeFlag();
                if (resumeGame) {
                    Resources resources = getResources();

                    String dialogTitle = resources.getString(R.string.restart_game);
                    String dialogYesText = resources.getString(R.string.yes);
                    String dialogNoText = resources.getString(R.string.no);
                    DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPref.setResumeFlag(false);
                            startGameActivity();
                        }
                    };
                    DialogInterface.OnClickListener noListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    };
                    showAlert(dialogTitle, dialogYesText, yesListener, dialogNoText, noListener);

                } else {
                    startGameActivity();
                }

            }
        });

        Button highScore = findViewById(R.id.high_score);
        highScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScore();
            }
        });

        Button help = findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRules();
            }
        });

        signInSignOutButton = findViewById(R.id.signin_signout);
        signInSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signedIn) signOut();
                else startSignInIntent();
            }
        });
    }

    public void startGameActivity() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("highScoreMoves", latestHighScore.getMoves());
        intent.putExtra("highScoreTime", latestHighScore.getTime().toSeconds());
        startActivity(intent);
    }

    private void showAlert(String title, String yesText,
                           DialogInterface.OnClickListener yesListener, String noText,
                           DialogInterface.OnClickListener noListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(yesText, yesListener);
        alertDialogBuilder.setNegativeButton(noText, noListener);

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    public void showHighScore() {
        new HighScoreFetchTask(this) {
            @Override
            protected void onPostExecute(ArrayList<HighScore> highScores) {
                super.onPostExecute(highScores);
                fillHighScores(highScores);
                if (highScores == null) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_high_score), Toast.LENGTH_SHORT).show();
                } else {
                    openDialog();
                }
            }
        }.execute();
    }

    private void fillHighScores(ArrayList<HighScore> highScores) {
        this.highScores = highScores;
    }

    public void openDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_high_score, null);

        RecyclerView highScoresList = view.findViewById(R.id.high_scores_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        highScoresList.setLayoutManager(layoutManager);

        HighScoresAdapter adapter = new HighScoresAdapter();
        adapter.setHighScores(highScores);

        highScoresList.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showRules() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_game_rules);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
