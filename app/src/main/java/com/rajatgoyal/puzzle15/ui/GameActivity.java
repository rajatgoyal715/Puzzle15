package com.rajatgoyal.puzzle15.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.rajatgoyal.puzzle15.R;

import java.util.Random;

/**
 * Created by rajat on 15/9/17.
 */

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    public int size, move, hours, minutes, seconds;
    public int m[][], id[][];
    public Button buttons[][];
    public int pos_x = 0, pos_y = 0;

    Handler handler;
    TextView timer, moves;
    long startTime, currTime, lastTime;

    public static final String TAG = "rajat";

    SharedPreferences sharedPref;

    int hs_moves, hs_hours, hs_minutes, hs_seconds;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate: ");

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("F94FB05401064F99C85F9BECABDC8E59").build();
        mAdView.loadAd(adRequest);

        init();
    }

    public void init() {
        Log.d(TAG, "init: start");

        getHighScore();

        size = 4;
        move = 0;
        m = new int[size][size];
        id = new int[size][size];

        timer = (TextView) findViewById(R.id.timer);
        moves = (TextView) findViewById(R.id.moves);

        fillIdMatrix();

        //buttons
        buttons = new Button[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = (Button) findViewById(id[i][j]);
                buttons[i][j].setOnClickListener(this);
            }
        }

        Log.d(TAG, "init: end");
        fillMatrix();
    }

    public void fillMatrix() {
        shuffle();

        makeValidMatrix();
    }

    public void getHighScore() {
        Context context = getApplicationContext();
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        hs_moves = sharedPref.getInt(getString(R.string.moves), Integer.MAX_VALUE);
        hs_hours = sharedPref.getInt(getString(R.string.hours), Integer.MAX_VALUE);
        hs_minutes = sharedPref.getInt(getString(R.string.minutes), Integer.MAX_VALUE);
        hs_seconds = sharedPref.getInt(getString(R.string.seconds), Integer.MAX_VALUE);
        Log.d(TAG, "getHighScore: " + hs_moves);
    }

    public void setHighScore() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.moves), move);
        editor.putInt(getString(R.string.hours), hours);
        editor.putInt(getString(R.string.minutes), minutes);
        editor.putInt(getString(R.string.seconds), seconds);
        editor.apply();
        Log.d(TAG, "getHighScore: " + move);
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

//        printLogMatrix();
    }

    public void printLogMatrix() {
        for(int i=0; i<size; i++){
            for(int j=0; j<size ;j++) {
                Log.d(TAG, "printLogMatrix: " + id[i][j]);
            }
        }
    }

    public void shuffle() {
        Log.d(TAG, "shuffle: start");
        Random rand = new Random();
        //an array to store which value has been assigned.
        int a[] = new int[size*size];
        int temp;
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                temp = rand.nextInt(size*size);
                while(a[temp]==1) {
                    temp = rand.nextInt(size*size);
                }
                a[temp] = 1;
                m[i][j] = temp;
                if(temp == 0) {
                    pos_x = i;
                    pos_y = j;
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));
                } else {
                    buttons[i][j].setText(temp + "");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        }
        Log.d(TAG, "shuffle: end");

        postInit();
    }

    // find inversions in the matrix by first creating a 1D matrix
    // and then compare each number with every number ahead of it and check if it is smaller.
    public int findInversions() {
        int arr[] = new int[size*size];
        for (int i = 0;i < size; i++) {
            System.arraycopy(m[i],0,arr,size*i,size);
//            for (int j = 0; j < size; j++) {
//                arr[size*i+j] = m[i][j];
//            }
        }
        int count = 0;
        for(int i = 0; i < size * size; i++) {
            if(arr[i]==0) continue;
            for(int j = i+1; j < size * size; j++) {
                if(arr[j]!=0 && arr[j]<arr[i]) count++;
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
        if(pos_x % 2 == 0 && inv % 2 != 0) return true;
        else if(pos_x % 2 != 0 && inv % 2 == 0) return true;
        return false;
    }

    // if puzzle is not solvable, make it solvable by decreasing one inversion
    // which can be done easily by swapping two last positions
    public void makeValidMatrix() {
        if(!isValid()){
            if(m[size-1][size-1]!=0){
                if(m[size-1][size-2]!=0){
                    int temp = m[size-1][size-2];
                    m[size-1][size-2] = m[size-1][size-1];
                    buttons[size-1][size-2].setText(m[size-1][size-2] + "");
                    m[size-1][size-1] = temp;
                    buttons[size-1][size-1].setText(m[size-1][size-1] + "");
                } else{
                    int temp = m[size-1][size-3];
                    m[size-1][size-3] = m[size-1][size-1];
                    buttons[size-1][size-3].setText(m[size-1][size-3] + "");
                    m[size-1][size-1] = temp;
                    buttons[size-1][size-1].setText(m[size-1][size-1] + "");
                }
            } else {
                int temp = m[size-1][size-3];
                m[size-1][size-3] = m[size-1][size-2];
                buttons[size-1][size-3].setText(m[size-1][size-3] + "");
                m[size-1][size-2] = temp;
                buttons[size-1][size-2].setText(m[size-1][size-2] + "");
            }
        }
    }

    // A method to fill the matrix in ascending order
    // This is used for debugging.
    public void seriesFill() {
        int temp;
        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp = (i*size + j + 1)%16;
                m[i][j] = temp;
                if(temp != 0) {
                    buttons[i][j].setText(temp + "");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
                } else {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.light));
                }
            }
        }

        postInit();
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
            time/=1000;
            seconds = (int)time%60;
            time/=60;
            minutes = (int)time%60;
            time/=60;
            hours = (int)time%24;

            timer.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", minutes) +
                    ":" + String.format("%02d", seconds));

            handler.postDelayed(this, 0);
        }
    };

    public void startTimer() {
        startTime = SystemClock.uptimeMillis();;
        lastTime = 0;
        handler.postDelayed(runnable, 0);
    }

    public void resumeTimer() {
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
        resumeTimer();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "check: start " + v.getId());
        int i, j=0;
        label:
        for(i = 0; i < size; i++) {
            for(j = 0; j < size; j++) {
                if(v.getId() == id[i][j])
                    break label;
            }
        }

        String n = buttons[i][j].getText().toString();
        if(n.isEmpty()) {
//            Toast.makeText(this, "Select a valid tile.", Toast.LENGTH_SHORT).show();
            return;
        }
        int num = Integer.parseInt(n);
        int i1 = i, j1 = j;

        if(isEmpty(i-1, j))
            i--;
        else if(isEmpty(i+1, j))
            i++;
        else if(isEmpty(i, j-1))
            j--;
        else if(isEmpty(i, j+1))
            j++;
        else {
//            Toast.makeText(this, "Invalid move", Toast.LENGTH_SHORT).show();
            return;
        }
        updateMoves(++move);
        // v.playSoundEffect(SoundEffectConstants.CLICK);

        m[i][j] = num;
        buttons[i][j].setText(num + "");
        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.background));
        m[i1][j1] = 0;
        buttons[i1][j1].setText("");
        buttons[i1][j1].setBackgroundColor(getResources().getColor(R.color.light));

        if(gameWon()){
            wonGame();
        }
        Log.d(TAG, "check: end");
    }

    public void wonGame() {
        // play win sound
        MediaPlayer mp = MediaPlayer.create(this, R.raw.tada);
        mp.start();

        // stop the timer
        handler.removeCallbacks(runnable);
        // save the score and compare with high score
        if(isLessTime()) {
            //show user that he got high score
            setHighScore();
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

    public boolean isLessTime() {
        if(hours > hs_hours)
            return false;
        else if(hours == hs_hours) {
            if(minutes > hs_minutes)
                return false;
            else if(minutes == hs_minutes) {
                if(seconds > hs_seconds)
                    return false;
            }
        }
        return true;
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
        moves.setText(move + "");
    }

    public boolean isEmpty(int i, int j) {
        return i>=0 && i<size && j>=0 && j<size && m[i][j]==0;
    }

    public boolean gameWon() {
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(m[i][j]!=((size*i+j+1)%(size*size)))
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
