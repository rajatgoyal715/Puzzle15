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
    private int moves;
    private boolean gameOver;
    private GameMatrix gameMatrix;
    private int[][] id;
    private Button[][] buttons;

    private Handler handler;
    private TextView timerTextView, movesTextView;
    private Time currTime;
    private long startTimeMillis, prevTimeMillis;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long currTimeMillis = SystemClock.uptimeMillis() - startTimeMillis + prevTimeMillis;
            currTime = new Time(currTimeMillis);

            timerTextView.setText(currTime.toString());

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
        boolean resumeGame = true;
        if (intent != null) {
            highScoreMoves = intent.getIntExtra("highScoreMoves", 0);
            highScoreTime = intent.getIntExtra("highScoreTime", 0);
            resumeGame = intent.getBooleanExtra("resumeGame", true);
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
                buttons[i][j].setOnTouchListener(new OnSwipeTouchListener(this));
            }
        }
        prevTimeMillis = 0;
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
                if (gameMatrix.isEmpty(i, j)) {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));
                } else {
                    String text = gameMatrix.get(i, j) + "";
                    buttons[i][j].setText(text);
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        }
    }

    public void startTimer(long prevTimeMillis) {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        this.startTimeMillis = SystemClock.uptimeMillis();
        this.prevTimeMillis = prevTimeMillis;
        this.handler.postDelayed(runnable, 0);
    }

    public void pauseTimer() {
        prevTimeMillis = currTime.toMillis();
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
            startTimer(prevTimeMillis);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("on stop");

        if(!gameOver) {
            SharedPref.setGameMatrix(gameMatrix);
            SharedPref.setMoves(moves);
            SharedPref.setGameTime(currTime.toMillis());
        }
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
        makeMove(i - gameMatrix.getEmptyCellRow(), j - gameMatrix.getEmptyCellCol());
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
        return (currTime.isLessThan(new Time(highScoreTime))) && (moves < highScoreMoves);
    }

    public void addHighScore() {
        int moves = this.moves;
        int time = currTime.toSeconds();

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
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void updateMoves(int moves) {
        movesTextView.setText(String.format(Locale.US, "%d", moves));
        this.moves = moves;
    }

    /**
     * @param rowIndex row index
     * @param colIndex column index
     * @return if rowIndex and columnIndex are within bounds
     */
    private boolean isValidPosition(int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < size && colIndex >= 0 && colIndex < size;
    }

    public class OnSwipeTouchListener implements View.OnTouchListener,
            SwipeGestureListener.OnSwipeInterface {
        private final GestureDetector gestureDetector;

        private int[] rowMoves = {0, 0, 1, -1};
        private int[] colMoves = {-1, 1, 0, 0};

        OnSwipeTouchListener(Context ctx) {
            Timber.d("Set OnSwipeTouchListener");
            gestureDetector = new GestureDetector(ctx, new SwipeGestureListener(this));
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        @Override
        public void onSwipeRight() {
            makeMove(rowMoves[0], colMoves[0]);
        }

        @Override
        public void onSwipeLeft() {
            makeMove(rowMoves[1], colMoves[1]);
        }

        @Override
        public void onSwipeTop() {
            makeMove(rowMoves[2], colMoves[2]);
        }

        @Override
        public void onSwipeBottom() {
            makeMove(rowMoves[3], colMoves[3]);
        }
    }

    /**
     * Move empty tile
     * @param rowMove number of rows to move
     * @param colMove number of columns to move
     */
    private void makeMove(int rowMove, int colMove) {
        // abs sum ensures that only one of the rowMove or colMove is 1 or -1
        if (Math.abs(rowMove) + Math.abs(colMove) != 1) return;
      
        int newEmptyRowIndex = gameMatrix.getEmptyCellRow() + rowMove;
        int newEmptyColIndex = gameMatrix.getEmptyCellCol() + colMove;

        if (!isValidPosition(newEmptyRowIndex, newEmptyColIndex)) return;
        updateMoves(++moves);

        // TODO Make this click sound optional, give option from the setting to turn this on or off
        playClickSound();

        // swapping of tiles
        int i = gameMatrix.getEmptyCellRow(), j = gameMatrix.getEmptyCellCol();

        buttons[i][j].setText(String.format("%s", gameMatrix.get(newEmptyRowIndex, newEmptyColIndex)));
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));

        buttons[newEmptyRowIndex][newEmptyColIndex].setText("");
        buttons[newEmptyRowIndex][newEmptyColIndex].setBackgroundColor(getResources().getColor(R.color.light));

        gameMatrix.swap(i, j, newEmptyRowIndex, newEmptyColIndex);

        if (gameMatrix.isSolved()) {
            wonGame();
        }
    }
}
