package com.rajatgoyal.puzzle15.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.rajatgoyal.puzzle15.R;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by rajatgoyal715 on 26/1/19.
 */
public class Tile extends AppCompatButton {

	public Tile(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Tile);

		int backgroundColor = a.getColor(R.styleable.Tile_android_background, getResources().getColor(R.color.background));
		this.setBackgroundColor(backgroundColor);

		int textColor = a.getColor(R.styleable.Tile_android_textColor, getResources().getColor(R.color.white));
		this.setTextColor(textColor);

		float textSize = a.getDimensionPixelSize(R.styleable.Tile_android_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, context.getResources().getDisplayMetrics()));
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

		CharSequence text = this.getText();
		if (TextUtils.isEmpty(text)) text = "Empty Tile";
		else text = "Tile Number " + text;
		this.setContentDescription(text);

		a.recycle();
	}
}
