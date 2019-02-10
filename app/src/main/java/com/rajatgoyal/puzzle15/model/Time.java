package com.rajatgoyal.puzzle15.model;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by rajat on 15/9/17.
 */

public class Time {
    private int hours;
    private int minutes;
    private int seconds;

    public Time(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public Time(long millis) {
        this((int) millis / 1000);
    }

    public Time(int seconds) {
        hours = seconds / (60 * 60);
        seconds %= 60 * 60;

        minutes = seconds / 60;
        seconds %= 60;

        this.seconds = seconds;
    }

    public int toSeconds() {
        return hours * 3600 + minutes * 60 + seconds;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%02d", hours)
                + ":" + String.format(Locale.US, "%02d", minutes)
                + ":" + String.format(Locale.US, "%02d", seconds);
    }

    public boolean isLessThan(Time t) {
        if (hours > t.hours)
            return false;
        else if (hours == t.hours) {
            if (minutes > t.minutes)
                return false;
            else if (minutes == t.minutes) {
                return seconds <= t.seconds;
            }
        }
        return true;
    }
}
