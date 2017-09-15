package com.rajatgoyal.puzzle15.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rajatgoyal.puzzle15.R;

import java.util.Locale;

/**
 * Created by rajat on 15/9/17.
 */

public class MainActivity extends AppCompatActivity {

    private Button newGame, highscore, help;
    private TextView moves, timer;

    public static final String TAG = "MainActivity";

    private int hs_moves, hs_hours, hs_minutes, hs_seconds;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init() {
        newGame = (Button) findViewById(R.id.newGame);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GameActivity.class);
                startActivity(i);
            }
        });

        highscore = (Button) findViewById(R.id.highScore);
        highscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScore();
            }
        });

        help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRules();
            }
        });
    }

    public void showHighScore() {
        getHighScore();

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_high_score, null);
        timer = (TextView) view.findViewById(R.id.timer_hs);
        moves = (TextView) view.findViewById(R.id.moves_hs);

        String time = "" + String.format(Locale.US, "%02d", hs_hours)
                + ":" + String.format(Locale.US, "%02d", hs_minutes)
                + ":" + String.format(Locale.US, "%02d", hs_seconds);
        timer.setText(time);

        String movesString = Integer.toString(hs_moves);
        moves.setText(movesString);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void getHighScore() {
        Context context = getApplicationContext();
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        hs_moves = sharedPref.getInt(getString(R.string.moves), 0);
        hs_hours = sharedPref.getInt(getString(R.string.hours), 0);
        hs_minutes = sharedPref.getInt(getString(R.string.minutes), 0);
        hs_seconds = sharedPref.getInt(getString(R.string.seconds), 0);
    }

    public void showRules() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_game_rules);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
