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
        this.hours = seconds / (60 * 60);
        seconds %= 60 * 60;

        this.minutes = seconds / 60;
        seconds %= 60;

        this.seconds = seconds;
    }

    public int toSeconds() {
        return this.hours * 3600 + this.minutes * 60 + this.seconds;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%02d", this.hours)
                + ":" + String.format(Locale.US, "%02d", this.minutes)
                + ":" + String.format(Locale.US, "%02d", this.seconds);
    }

    public boolean isLessThan(Time time) {
        if (this.hours > time.hours)
            return false;
        else if (this.hours == time.hours) {
            if (this.minutes > time.minutes)
                return false;
            else if (this.minutes == time.minutes) {
                return this.seconds <= time.seconds;
            }
        }
        return true;
    }
}
