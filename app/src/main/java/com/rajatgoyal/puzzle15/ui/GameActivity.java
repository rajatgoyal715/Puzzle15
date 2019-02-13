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
import com.rajatgoyal.puzzle15.model.Time;

import java.util.Locale;
import java.util.Random;

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
    private int m[][], id[][];
    private Button buttons[][];

    private Handler handler;
    private TextView timerTextView, movesTextView;
    private long startTime, currTime, lastTime;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currTime = SystemClock.uptimeMillis() - startTime + lastTime;
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

    private int highScoreMoves, highScoreTime;
    private MediaPlayer clickMP;
    /**
     * Empty Row Index: stores the row number of the empty cell
     * Empty Col Index: stores the column number of the empty cell
     */
    private int emptyRowIndex = 0;
    private int emptyColIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Timber.d("onCreate: ");

        Intent intent = getIntent();
        if (intent != null) {
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

            updateBoard();

            moves = savedInstanceState.getInt("moves");
            updateMoves(moves);

            lastTime = savedInstanceState.getLong("currTime");
            resumeTimer();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        moves = 0;
        gameOver = false;
        m = new int[size][size];
        id = new int[size][size];

        timerTextView = findViewById(R.id.timer);
        movesTextView = findViewById(R.id.moves);

        fillIdMatrix();

        buttons = new Button[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = findViewById(id[i][j]);
                buttons[i][j].setOnClickListener(this);
                buttons[i][j].setOnTouchListener(new OnSwipeTouchListener(this));
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
        seriesFill();
        shuffle();
        makeValidMatrix();

        updateBoard();
        postInit();
    }

    /**
     * Fill the matrix in ascending order
     */
    public void seriesFill() {
        int temp;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp = (i * size + j + 1) % 16;
                m[i][j] = temp;
            }
        }
    }

    /**
     * Fill the matrix in random order
     */
    public void shuffle() {
        int pos_x = size - 1, pos_y = size - 1;
        int temp, temp_x, temp_y, swap;

        Random rand = new Random();

        for (int index = size * size - 1; index > 1; index--) {
            temp = rand.nextInt(index);
            temp_x = temp / size;
            temp_y = (temp + size) % size;

            swap = m[temp_x][temp_y];
            m[temp_x][temp_y] = m[pos_x][pos_y];
            m[pos_x][pos_y] = swap;

            if (pos_y == 0) {
                pos_x--;
                pos_y = size - 1;
            } else {
                pos_y--;
            }
        }
    }

    /**
     * Update the board according to the matrix
     */
    public void updateBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (m[i][j] == 0) {
                    emptyRowIndex = i;
                    emptyColIndex = j;
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

    /**
     * Calculate number of inversions in the matrix
     *
     * @return number of inversions
     */
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

    private int findEmptyCellPosition() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (m[i][j] == 0) return i;
            }
        }
        return -1;
    }

    /**
     * Check if the matrix is valid according to following rules:
     * If n is even, then the matrix is solvable if:
     * 1. blank is on even row counting from the bottom and no of inversions is odd.
     * 2. blank is on odd row from the bottom and no of inversions is even.
     * If n is odd, then the matrix is solvable if no. of inversions is even.
     *
     * @return validity of matrix
     */
    public boolean isValid() {
        int inv = findInversions();
        int empty_cell_pos_x = findEmptyCellPosition();

        return (empty_cell_pos_x % 2 == 0 && inv % 2 != 0) || (empty_cell_pos_x % 2 != 0 && inv % 2 == 0);
    }

    /**
     * If puzzle is not solvable, make it solvable by decreasing one inversion
     * which can be done easily by swapping two last positions
     */
    public void makeValidMatrix() {
        if (!isValid()) {
            if (m[size - 1][size - 1] != 0) {
                if (m[size - 1][size - 2] != 0) {
                    int temp = m[size - 1][size - 2];
                    m[size - 1][size - 2] = m[size - 1][size - 1];
                    buttons[size - 1][size - 2].setText(String.format("%s", m[size - 1][size - 2]));
                    m[size - 1][size - 1] = temp;
                    buttons[size - 1][size - 1].setText(String.format("%s", m[size - 1][size - 1]));
                } else {
                    int temp = m[size - 1][size - 3];
                    m[size - 1][size - 3] = m[size - 1][size - 1];
                    buttons[size - 1][size - 3].setText(String.format("%s", m[size - 1][size - 3]));
                    m[size - 1][size - 1] = temp;
                    buttons[size - 1][size - 1].setText(String.format("%s", m[size - 1][size - 1]));
                }
            } else {
                int temp = m[size - 1][size - 3];
                m[size - 1][size - 3] = m[size - 1][size - 2];
                buttons[size - 1][size - 3].setText(String.format("%s", m[size - 1][size - 3]));
                m[size - 1][size - 2] = temp;
                buttons[size - 1][size - 2].setText(String.format("%s", m[size - 1][size - 2]));
            }
        }
    }

    public void postInit() {
        handler = new Handler();
        startTimer();
    }

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
        pauseTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        makeMove(i - emptyRowIndex, j - emptyColIndex);
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
                fillMatrix();

                //updating the moves
                updateMoves(0);
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

    public void updateMoves(int move) {
        movesTextView.setText(String.format(Locale.US, "%d", move));
        moves = move;
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
        alertDialogBuilder.setTitle(getResources().getString(R.string.quit_the_game));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
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
        int newEmptyRowIndex = emptyRowIndex + rowMove;
        int newEmptyColIndex = emptyColIndex + colMove;

        if (!isValidPosition(newEmptyRowIndex, newEmptyColIndex)) return;

        updateMoves(++moves);

        // TODO Make this click sound optional, give option from the setting to turn this on or off
        playClickSound();

        // swapping of tiles
        int i = emptyRowIndex, j = emptyColIndex;
        m[i][j] = m[newEmptyRowIndex][newEmptyColIndex];
        buttons[i][j].setText(String.format("%s", m[i][j]));
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));

        m[newEmptyRowIndex][newEmptyColIndex] = 0;
        buttons[newEmptyRowIndex][newEmptyColIndex].setText("");
        buttons[newEmptyRowIndex][newEmptyColIndex].setBackgroundColor(getResources().getColor(R.color.light));

        emptyRowIndex = newEmptyRowIndex;
        emptyColIndex = newEmptyColIndex;

        if (checkIfGameOver()) {
            wonGame();
        }
    }
}
