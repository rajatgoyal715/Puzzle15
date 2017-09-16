package com.rajatgoyal.puzzle15.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.data.GameContract;
import com.rajatgoyal.puzzle15.model.HighScore;
import com.rajatgoyal.puzzle15.model.Time;
import com.rajatgoyal.puzzle15.task.LatestHighScoreFetchTask;
import com.rajatgoyal.puzzle15.widget.Widget;

import java.util.Random;

/**
 * Created by rajat on 15/9/17.
 */

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private int size, moves, hours, minutes, seconds;
    private boolean gameOver;
    private int m[][], id[][];
    private Button buttons[][];
    private int pos_x = 0;

    private Handler handler;
    private TextView timerTextView, movesTextView;
    private long startTime, currTime, lastTime;

    public static final String TAG = "GameActivity";

    private AdView mAdView;

    private String uid, name;
    private int highScoreMoves, highScoreTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate: ");

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("F94FB05401064F99C85F9BECABDC8E59").build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        if (intent != null) {
            uid = intent.hasExtra("uid") ? intent.getStringExtra("uid") : null;
            name = intent.hasExtra("name") ? intent.getStringExtra("name") : null;
            highScoreMoves = intent.getIntExtra("highScoreMoves", 0);
            highScoreTime = intent.getIntExtra("highScoreTime", 0);
        }

        init();
        if (savedInstanceState == null) {
            fillMatrix();
        } else {
            m[0] = savedInstanceState.getIntArray("matrix_row_0");
            m[1] = savedInstanceState.getIntArray("matrix_row_1");
            m[2] = savedInstanceState.getIntArray("matrix_row_2");
            m[3] = savedInstanceState.getIntArray("matrix_row_3");

            updateUI();

            moves = savedInstanceState.getInt("moves");
            updateMoves(moves);

            lastTime = savedInstanceState.getLong("currTime");
            resumeTimer();
        }

    }


    public void init() {

        size = 4;
        moves = 0;
        gameOver = false;
        m = new int[size][size];
        id = new int[size][size];

        timerTextView = (TextView) findViewById(R.id.timer);
        movesTextView = (TextView) findViewById(R.id.moves);

        fillIdMatrix();

        buttons = new Button[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = (Button) findViewById(id[i][j]);
                buttons[i][j].setOnClickListener(this);
            }
        }
    }

    public void fillIdMatrix() {
        id[0][0] = R.id.btn00;
        id[0][1] = R.id.btn01;
        id[0][2] = R.id.btn02;
        id[0][3] = R.id.btn03;

        id[1][0] = R.id.btn10;
        id[1][1] = R.id.btn11;
        id[1][2] = R.id.btn12;
        id[1][3] = R.id.btn13;

        id[2][0] = R.id.btn20;
        id[2][1] = R.id.btn21;
        id[2][2] = R.id.btn22;
        id[2][3] = R.id.btn23;

        id[3][0] = R.id.btn30;
        id[3][1] = R.id.btn31;
        id[3][2] = R.id.btn32;
        id[3][3] = R.id.btn33;
    }

    public void fillMatrix() {
        shuffle();
//        seriesFill();
    }

    // A method to fill the matrix in ascending order
    // This is used for debugging.
    public void seriesFill() {
        int temp;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp = (i * size + j + 1) % 16;
                m[i][j] = temp;
            }
        }

        updateUI();
        postInit();
    }

    public void shuffle() {
        Random rand = new Random();

        //a local array, used to store values from 1 to 15 which have been already assigned.
        int a[] = new int[size * size];
        int temp;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp = rand.nextInt(size * size);
                while (a[temp] == 1) {
                    temp = rand.nextInt(size * size);
                }
                a[temp] = 1;
                m[i][j] = temp;
            }
        }

        updateUI();
        postInit();
    }

    public void updateUI() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (m[i][j] == 0) {
                    pos_x = i;
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));
                } else {
                    String num = m[i][j] + "";
                    buttons[i][j].setText(num);
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        }
    }

    // find inversions in the matrix by first creating a 1D matrix
    // and then compare each number with every number ahead of it and check if it is smaller.
    public int findInversions() {
        int arr[] = new int[size * size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(m[i], 0, arr, size * i, size);
        }
        int count = 0;
        for (int i = 0; i < size * size; i++) {
            if (arr[i] == 0) continue;
            for (int j = i + 1; j < size * size; j++) {
                if (arr[j] != 0 && arr[j] < arr[i]) count++;
            }
        }
        return count;
    }

    // If n is even, then the matrix is solvable if;
    // 1. blank is on even row counting from the bottom and no of inversions is odd.
    // 2 blank is on odd row from the bottom and no of inversions is even.
    // else puzzle is not solvable
    public boolean isValid() {
        int inv = findInversions();
        if (pos_x % 2 == 0 && inv % 2 != 0) return true;
        else if (pos_x % 2 != 0 && inv % 2 == 0) return true;
        return false;
    }

    // if puzzle is not solvable, make it solvable by decreasing one inversion
    // which can be done easily by swapping two last positions
    public void makeValidMatrix() {
        if (!isValid()) {
            if (m[size - 1][size - 1] != 0) {
                if (m[size - 1][size - 2] != 0) {
                    int temp = m[size - 1][size - 2];
                    m[size - 1][size - 2] = m[size - 1][size - 1];
                    buttons[size - 1][size - 2].setText(m[size - 1][size - 2]);
                    m[size - 1][size - 1] = temp;
                    buttons[size - 1][size - 1].setText(m[size - 1][size - 1]);
                } else {
                    int temp = m[size - 1][size - 3];
                    m[size - 1][size - 3] = m[size - 1][size - 1];
                    buttons[size - 1][size - 3].setText(m[size - 1][size - 3]);
                    m[size - 1][size - 1] = temp;
                    buttons[size - 1][size - 1].setText(m[size - 1][size - 1]);
                }
            } else {
                int temp = m[size - 1][size - 3];
                m[size - 1][size - 3] = m[size - 1][size - 2];
                buttons[size - 1][size - 3].setText(m[size - 1][size - 3]);
                m[size - 1][size - 2] = temp;
                buttons[size - 1][size - 2].setText(m[size - 1][size - 2]);
            }
        }
    }

    public void postInit() {
        handler = new Handler();
        startTimer();
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currTime = SystemClock.uptimeMillis() - startTime + lastTime;
            // Log.d(TAG, "run: " + currTime);
            long time = currTime;
            time /= 1000;
            seconds = (int) time % 60;
            time /= 60;
            minutes = (int) time % 60;
            time /= 60;
            hours = (int) time % 24;

            timerTextView.setText(new Time((int) (currTime / 1000)).toString());

            handler.postDelayed(this, 0);
        }
    };

    public void startTimer() {
        if (handler == null) {
            handler = new Handler();
        }
        startTime = SystemClock.uptimeMillis();
        lastTime = 0;
        handler.postDelayed(runnable, 0);
    }

    public void resumeTimer() {
        if (handler == null) {
            handler = new Handler();
        }
        startTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);
    }

    public void pauseTimer() {
        lastTime = currTime;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        pauseTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (!gameOver) {
            resumeTimer();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray("matrix_row_0", m[0]);
        outState.putIntArray("matrix_row_1", m[1]);
        outState.putIntArray("matrix_row_2", m[2]);
        outState.putIntArray("matrix_row_3", m[3]);

        outState.putInt("moves", moves);

        outState.putLong("currTime", currTime);
    }

    @Override
    public void onClick(View v) {
        int i, j = 0;

        // Get button's coordinates using id matrix
        label:
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (v.getId() == id[i][j])
                    break label;
            }
        }

        String n = buttons[i][j].getText().toString();
        if (TextUtils.isEmpty(n)) {
            // user clicked on the empty tile
            return;
        }

        int num = Integer.parseInt(n);
        int i1 = i, j1 = j;

        if (isEmpty(i - 1, j))
            i--;
        else if (isEmpty(i + 1, j))
            i++;
        else if (isEmpty(i, j - 1))
            j--;
        else if (isEmpty(i, j + 1))
            j++;
        else {
            // Invalid move
            return;
        }
        updateMoves(++moves);
        v.playSoundEffect(SoundEffectConstants.CLICK);

        // swapping of tiles

        m[i][j] = num;
        buttons[i][j].setText(num + "");
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));

        m[i1][j1] = 0;
        buttons[i1][j1].setText("");
        buttons[i1][j1].setBackgroundColor(getResources().getColor(R.color.light));

        if (checkIfGameOver()) {
            wonGame();
        }
    }

    public void wonGame() {
        // play win sound
        MediaPlayer mp = MediaPlayer.create(this, R.raw.tada);
        mp.start();

        // stop the timer
        gameOver = true;
        handler.removeCallbacks(runnable);

        // Compare with high score
        if (isHighScore()) {
            //show user that he got high score
            addHighScore();
            if (uid != null) {
                uploadHighScore();
            }
        }

        Toast.makeText(this, "Game Won !!", Toast.LENGTH_LONG).show();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(R.layout.dialog_game_won);

        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
                startNewGame();
            }
        }, 2000);
    }

    public boolean isHighScore() {
        Time currentTime = new Time(hours, minutes, seconds);
        return (currentTime.isLessThan(new Time(highScoreTime))) && (moves < highScoreMoves);
    }

    public void addHighScore() {
        int moves = this.moves;
        int time = new Time(hours, minutes, seconds).toSeconds();

        ContentValues contentValues = new ContentValues();
        contentValues.put(GameContract.GameEntry.COLUMN_MOVES, moves);
        contentValues.put(GameContract.GameEntry.COLUMN_TIME, time);

        Uri uri = getContentResolver().insert(GameContract.GameEntry.CONTENT_URI, contentValues);
        if (uri != null) {
            Log.d(TAG, "addHighScore: High Score added");
        }
    }

    public void uploadHighScore() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = database.getReference();

        DatabaseReference userRef = dbRef.child("high_scores").child(uid);
        userRef.child("name").setValue(name);
        userRef.child("moves").setValue(moves);
        userRef.child("time").setValue(new Time(hours, minutes, seconds).toSeconds());

        updateWidgets();
    }

    public void updateWidgets() {
        Intent intent = new Intent(this, Widget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(
                getApplication()).getAppWidgetIds(new ComponentName(getApplication(), Widget.class)
        );
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public void startNewGame() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Want to play a new game?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                fillMatrix();

                //updating the moves
                updateMoves(0);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public void updateMoves(int move) {
        movesTextView.setText(Integer.toString(move));
        moves = move;
    }

    public boolean isEmpty(int i, int j) {
        return i >= 0 && i < size && j >= 0 && j < size && m[i][j] == 0;
    }

    public boolean checkIfGameOver() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (m[i][j] != ((size * i + j + 1) % (size * size)))
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Are you sure you want to quit the game?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }
}
