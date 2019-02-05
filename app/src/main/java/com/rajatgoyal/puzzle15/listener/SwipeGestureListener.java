package com.rajatgoyal.puzzle15.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;

import timber.log.Timber;

/**
 * Created by rajatgoyal715 on 4/2/19.
 */
public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
	private static final int MAX_SWIPE_DISTANCE = 100;
	private static final int MAX_SWIPE_SPEED = 100;

	private OnSwipeInterface onSwipeInterface;

	public SwipeGestureListener(OnSwipeInterface swipeInterface) {
		onSwipeInterface = swipeInterface;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		boolean result = false;
		try {
			float diffY = e2.getY() - e1.getY();
			float diffX = e2.getX() - e1.getX();
			// Horizontal Swipes
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (Math.abs(diffX) > MAX_SWIPE_DISTANCE &&
						Math.abs(velocityX) > MAX_SWIPE_SPEED) {
					if (diffX > 0) {
						onSwipeInterface.onSwipeRight();
					} else {
						onSwipeInterface.onSwipeLeft();
					}
					result = true;
				}
			}
			// Vertical Swipes
			else if (Math.abs(diffY) > MAX_SWIPE_DISTANCE &&
					Math.abs(velocityY) > MAX_SWIPE_SPEED) {
				if (diffY > 0) {
					onSwipeInterface.onSwipeBottom();
				} else {
					onSwipeInterface.onSwipeTop();
				}
				result = true;
			}
		} catch (Exception exception) {
			Timber.e(exception);
		}
		return result;
	}

	public interface OnSwipeInterface {
		void onSwipeRight();
		void onSwipeLeft();
		void onSwipeTop();
		void onSwipeBottom();
	}
}
