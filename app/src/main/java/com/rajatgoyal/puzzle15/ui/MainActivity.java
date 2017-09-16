package com.rajatgoyal.puzzle15.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.adapter.HighScoresAdapter;
import com.rajatgoyal.puzzle15.adapter.LeaderboardAdapter;
import com.rajatgoyal.puzzle15.model.HighScore;
import com.rajatgoyal.puzzle15.model.Leaderboard;
import com.rajatgoyal.puzzle15.model.Time;
import com.rajatgoyal.puzzle15.task.HighScoreFetchTask;
import com.rajatgoyal.puzzle15.task.LatestHighScoreFetchTask;

import java.util.ArrayList;

/**
 * Created by rajat on 15/9/17.
 */

public class MainActivity extends AppCompatActivity{

    private ArrayList<HighScore> highScores;
    private ArrayList<Leaderboard> leaderboard;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 101;

    private static final String TAG = "MainActivity";

    private TextView loginMessage, welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            firebaseInit();
            init();

            if (isOnline()) {
                fetchLeaderboard();
            }
        }
    }

    public void firebaseInit() {

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(MainActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };
    }

    public void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            welcomeMessage.setVisibility(View.GONE);
            loginMessage.setText(Html.fromHtml(getResources().getString(R.string.signIn)));
        } else {
            String msg = "Welcome " + currentUser.getDisplayName();
            welcomeMessage.setText(msg);
            welcomeMessage.setVisibility(View.VISIBLE);
            loginMessage.setText(Html.fromHtml(getResources().getString(R.string.signOut)));
        }
    }

    private void signIn() {
        if (isOnline()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            Toast.makeText(this, "PLease check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            // send the latest high score to the firebase account
                            uploadHighScore();
                        } else {
                            Toast.makeText(MainActivity.this, "PLease check your internet connection.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signOut() {

        FirebaseAuth.getInstance().signOut();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(null);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    public void init() {
        Button newGame = (Button) findViewById(R.id.newGame);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GameActivity.class);
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    i.putExtra("uid", user.getUid());
                    i.putExtra("name", user.getDisplayName());
                }
                startActivity(i);
            }
        });

        Button highscore = (Button) findViewById(R.id.highScore);
        highscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScore();
            }
        });

        Button leaderboard = (Button) findViewById(R.id.leaderboard);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaderboard();
            }
        });

        Button help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRules();
            }
        });

        loginMessage = (TextView) findViewById(R.id.login_message);
        loginMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    signOut();
                } else {
                    signIn();
                }
            }
        });

        welcomeMessage = (TextView) findViewById(R.id.welcome_message);
    }

    public void showLeaderboard() {
        if (leaderboard == null) {
            Toast.makeText(this, "No Leaderboard yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_leaderboard, null);

        RecyclerView leaderboardList = (RecyclerView) view.findViewById(R.id.leaderboard_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        leaderboardList.setLayoutManager(layoutManager);

        LeaderboardAdapter adapter = new LeaderboardAdapter();
        adapter.setLeaderboard(leaderboard);

        leaderboardList.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void fetchLeaderboard() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("high_scores");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Leaderboard> list = new ArrayList<>();

                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                int i = 1;
                for (DataSnapshot snapshot : snapshots) {
                    if (i > 20) break;
                    String name = "";
                    int moves = 0, time = 0;
                    Iterable<DataSnapshot> items = snapshot.getChildren();
                    int j = 0;
                    for (DataSnapshot item : items) {
                        if(j==0) moves = Integer.parseInt(item.getValue().toString());
                        else if(j==1) name = item.getValue().toString();
                        else time = Integer.parseInt(item.getValue().toString());
                        j++;
                    }
                    list.add(new Leaderboard(name, moves, time));
                    i++;
                }

                fillLeaderboard(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void fillLeaderboard(ArrayList<Leaderboard> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void showHighScore() {
        new HighScoreFetchTask(this) {
            @Override
            protected void onPostExecute(ArrayList<HighScore> highScores) {
                super.onPostExecute(highScores);
                fillHighScores(highScores);
                if (highScores == null ){
                    Toast.makeText(MainActivity.this, "No High Score yet", Toast.LENGTH_SHORT).show();
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

        RecyclerView highScoresList = (RecyclerView) view.findViewById(R.id.high_scores_list);

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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void uploadHighScore() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = database.getReference();

        new LatestHighScoreFetchTask(this) {
            @Override
            protected void onPostExecute(HighScore highScore) {
                super.onPostExecute(highScore);

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && highScore != null) {
                    DatabaseReference userRef = dbRef.child("high_scores").child(user.getUid());
                    userRef.child("name").setValue(user.getDisplayName());
                    userRef.child("moves").setValue(highScore.getMoves());
                    userRef.child("time").setValue(highScore.getTime().toSeconds());
                }
            }
        }.execute();
    }
}