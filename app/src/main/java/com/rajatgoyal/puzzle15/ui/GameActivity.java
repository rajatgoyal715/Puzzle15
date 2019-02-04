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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rajatgoyal.puzzle15.R;
import com.rajatgoyal.puzzle15.data.GameContract;
import com.rajatgoyal.puzzle15.model.Time;

import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

import static android.view.GestureDetector.SimpleOnGestureListener;

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
                buttons[i][j].setOnTouchListener(new OnSwipeTouchListener(this, buttons[i][j]));
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
        int pos_x = size-1, pos_y = size-1;
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

	public boolean isEmpty(int i, int j) {
		return i >= 0 && i < size && j >= 0 && j < size && m[i][j] == 0;
	}

	private void playClickSound() {
        if(clickMP != null) {
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
        playClickSound();

        // swapping of tiles
        m[i][j] = num;
        buttons[i][j].setText(String.format("%s", num));
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
        if (!TextUtils.isEmpty(buttonText)) {
            // user clicked on a non-empty tile
            return;
        }


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
        view.playSoundEffect(SoundEffectConstants.CLICK);

        // swapping of tiles

        m[i1][j1] = m[i][j];
        m[i][j] = 0;
        buttons[i][j].setText("");
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));

        buttons[i1][j1].setText(String.format("%s", m[i1][j1]));
        buttons[i1][j1].setBackgroundColor(getResources().getColor(R.color.background));

        if (checkIfGameOver()) {
            wonGame();
        }
    }

    private boolean isSwipeValid(int i, int j) {
        return i >= 0 && i < size && j >= 0 && j < size && m[i][j] != 0;
    }

    public enum SWIPE {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        View view;

        OnSwipeTouchListener(Context ctx, View view) {
            gestureDetector = new GestureDetector(ctx, new SwipeGestureListener());
            this.view = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        void onSwipeRight() {
            swipeHandler(view, SWIPE.RIGHT);
        }

        void onSwipeLeft() {
            swipeHandler(view, SWIPE.LEFT);
        }

        void onSwipeTop() {
            swipeHandler(view, SWIPE.TOP);
        }

        void onSwipeBottom() {
            swipeHandler(view, SWIPE.BOTTOM);
        }

        private final class SwipeGestureListener extends SimpleOnGestureListener {

            private static final int MAX_SWIPE_DISTANCE = 100;
            private static final int MAX_SWIPE_SPEED = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    // Horizontal Swipes
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > MAX_SWIPE_DISTANCE &&
                                Math.abs(velocityX) > MAX_SWIPE_SPEED) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    // Vertical Swipes
                    else if (Math.abs(diffY) > MAX_SWIPE_DISTANCE &&
                            Math.abs(velocityY) > MAX_SWIPE_SPEED) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    Timber.e(exception);
                }
                return result;
            }
        }
    }
}
