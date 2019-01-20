package com.rajatgoyal.puzzle15;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by rajatgoyal715 on 20/1/19.
 */
public class MainApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Timber.plant(new Timber.DebugTree());
	}
}
