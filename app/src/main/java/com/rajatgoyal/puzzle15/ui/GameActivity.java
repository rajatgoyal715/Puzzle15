package com.rajatgoyal.puzzle15.ui;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.data.GameContract;
import com.rajatgoyal.puzzle15.listener.SwipeGestureListener;
import com.rajatgoyal.puzzle15.model.GameMatrix;
import com.rajatgoyal.puzzle15.model.Time;
import com.rajatgoyal.puzzle15.util.SharedPref;

import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

/**
 * Created by rajat on 15/9/17.
 */

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int size = 4;
    private int moves, hours, minutes, seconds;
    private boolean gameOver;
    private GameMatrix gameMatrix;
    private int[][] id;
    private Button[][] buttons;

    private Handler handler;
    private TextView timerTextView, movesTextView;
    private long startTime, currTime, prevTime;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currTime = SystemClock.uptimeMillis() - startTime + prevTime;
            long time = currTime;
            time /= 1000;
            seconds = (int) time % 60;
            time /= 60;
            minutes = (int) time % 60;
            time /= 60;
            hours = (int) time % 24;

            timerTextView.setText(new Time(hours, minutes, seconds).toString());

            handler.postDelayed(this, 1000);
        }
    };

    private int highScoreMoves, highScoreTime;
    private MediaPlayer clickMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Timber.d("onCreate: ");

        Intent intent = getIntent();
        boolean resumeGame = false;
        if (intent != null) {
            highScoreMoves = intent.getIntExtra("highScoreMoves", 0);
            highScoreTime = intent.getIntExtra("highScoreTime", 0);
            resumeGame = intent.getBooleanExtra("resumeGame", false);
        }

        init();
        if (resumeGame) {
            updateBoard(SharedPref.getGameMatrix());
            updateMoves(SharedPref.getMoves());
            startTimer(SharedPref.getGameTime());
        } else {
            updateBoard(new GameMatrix(size));
            updateMoves(0);
            startTimer(0);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        moves = 0;
        gameOver = false;
        id = new int[size][size];

        timerTextView = findViewById(R.id.timer);
        movesTextView = findViewById(R.id.moves);

        fillIdMatrix(id);

        buttons = new Button[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = findViewById(id[i][j]);
                buttons[i][j].setOnClickListener(this);
                buttons[i][j].setOnTouchListener(new OnSwipeTouchListener(this, buttons[i][j]));
            }
        }
        prevTime = 0;
    }

    public void fillIdMatrix(int[][] id) {
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


    /**
     * Update the board according to the matrix
     *
     * @param gameMatrix game matrix
     */
    public void updateBoard(GameMatrix gameMatrix) {
        this.gameMatrix = gameMatrix;
        int size = gameMatrix.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = gameMatrix.get(i, j);
                if (value == 0) {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));
                } else {
                    String text = value + "";
                    buttons[i][j].setText(text);
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        }
    }

    public void startTimer(long prevTime) {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        this.startTime = SystemClock.uptimeMillis();
        this.prevTime = prevTime;
        this.handler.postDelayed(runnable, 0);
    }

    public void pauseTimer() {
        prevTime = currTime;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameOver) {
            startTimer(prevTime);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPref.setGameMatrix(gameMatrix);
        SharedPref.setMoves(moves);
        SharedPref.setGameTime(currTime);
    }

    private void playClickSound() {
        if (clickMP != null) {
            clickMP.release();
        }
        clickMP = MediaPlayer.create(this, R.raw.click);
        clickMP.start();
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

        if (isSwipeValid(i - 1, j))
            i--;
        else if (isSwipeValid(i + 1, j))
            i++;
        else if (isSwipeValid(i, j - 1))
            j--;
        else if (isSwipeValid(i, j + 1))
            j++;
        else {
            // Invalid move
            return;
        }
        updateMoves(++moves);
        playClickSound();

        // swapping of tiles
        gameMatrix.swap(i, j, i1, j1);
        buttons[i][j].setText(String.format("%s", num));
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));

        buttons[i1][j1].setText("");
        buttons[i1][j1].setBackgroundColor(getResources().getColor(R.color.light));

        if (gameMatrix.isSolved()) {
            wonGame();
        }
    }

    public void wonGame() {
        // play win sound
        MediaPlayer mp = MediaPlayer.create(this, R.raw.tada);
        mp.start();

        // invalidate save game data
        SharedPref.setGameTime(0);

        // stop the timer
        gameOver = true;
        handler.removeCallbacks(runnable);

        // Compare with high score
        if (isHighScore()) {
            //show user that he got high score
            addHighScore();
        }

        Toast.makeText(this, getResources().getString(R.string.game_won), Toast.LENGTH_LONG).show();

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
            Timber.d("addHighScore: High Score added");
        }
    }

    public void startNewGame() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.play_new_game));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                updateBoard(new GameMatrix(size));
                //updating the moves
                updateMoves(0);
                startTimer(0);
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public void updateMoves(int moves) {
        movesTextView.setText(String.format(Locale.US, "%d", moves));
        this.moves = moves;
    }

    private void swipeHandler(View view, SWIPE DIR) {
        int i, j = 0;

        // Get button's coordinates using id matrix
        label:
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (view.getId() == id[i][j])
                    break label;
            }
        }

        String buttonText = buttons[i][j].getText().toString();
        if (TextUtils.isEmpty(buttonText)) {
            // user clicked on the empty tile
            return;
        }

        int num = Integer.parseInt(buttonText);
        int i1 = i, j1 = j;

        if (DIR.equals(SWIPE.RIGHT) && isSwipeValid(i, j + 1)) {
            Timber.d("Swiped Right");
            j++;
        } else if (DIR.equals(SWIPE.LEFT) && isSwipeValid(i, j - 1)) {
            Timber.d("Swiped Left");
            j--;
        } else if (DIR.equals(SWIPE.BOTTOM) && isSwipeValid(i + 1, j)) {
            Timber.d("Swiped Bottom");
            i++;
        } else if (DIR.equals(SWIPE.TOP) && isSwipeValid(i - 1, j)) {
            Timber.d("Swiped Top");
            i--;
        } else {
            // Invalid move
            return;
        }

        updateMoves(++moves);
        playClickSound();

        // swapping of tiles
        gameMatrix.swap(i, j, i1, j1);
        buttons[i][j].setText(String.format("%s", num));
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));

        buttons[i1][j1].setText("");
        buttons[i1][j1].setBackgroundColor(getResources().getColor(R.color.light));

        if (gameMatrix.isSolved()) {
            wonGame();
        }
    }

    private boolean isSwipeValid(int i, int j) {
        return i >= 0 && i < size && j >= 0 && j < size && gameMatrix.get(i, j) == 0;
    }

    public enum SWIPE {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    public class OnSwipeTouchListener implements View.OnTouchListener,
            SwipeGestureListener.OnSwipeInterface {

        private final GestureDetector gestureDetector;
        View view;

        OnSwipeTouchListener(Context ctx, View view) {
            gestureDetector = new GestureDetector(ctx, new SwipeGestureListener(this));
            this.view = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        @Override
        public void onSwipeRight() {
            swipeHandler(view, SWIPE.RIGHT);
        }

        @Override
        public void onSwipeLeft() {
            swipeHandler(view, SWIPE.LEFT);
        }

        @Override
        public void onSwipeTop() {
            swipeHandler(view, SWIPE.TOP);
        }

        @Override
        public void onSwipeBottom() {
            swipeHandler(view, SWIPE.BOTTOM);
        }
    }
}
