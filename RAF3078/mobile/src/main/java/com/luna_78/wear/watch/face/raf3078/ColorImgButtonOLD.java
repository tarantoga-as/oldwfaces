package com.luna_78.wear.watch.face.raf3078;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by buba on 25/02/15.
 */
public class ColorImgButtonOLD extends ImageButton implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "CIBT";

    private HandheldCompanionConfigActivity mActivity;

    private int mColor = 0;

    public void setColor(int c) {mColor = c;}
    public int getColor() {return mColor;}


    Paint mPaint = new Paint();

    LinearLayout mColorPaletteLinearLayout;
    List<ColorImgButtonOLD> mColorPaletteViewList;

    public void setGlobals(LinearLayout layout, List<ColorImgButtonOLD> list) {
        mColorPaletteLinearLayout = layout;
        mColorPaletteViewList = list;
    }


    private void setPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
    }

    public ColorImgButtonOLD(HandheldCompanionConfigActivity context) {
        super(context);
        mActivity = context;
        setPaint();
    }

    public ColorImgButtonOLD(HandheldCompanionConfigActivity context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = context;
        setPaint();
    }

    public ColorImgButtonOLD(HandheldCompanionConfigActivity context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = context;
        setPaint();
    }
/*

    public ColorImgButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
*/

/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
*/

    @Override
    public void draw(Canvas canvas) {
        //super.draw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radiusMax = Math.min(centerX, centerY);
        //
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
        canvas.drawCircle(centerX, centerY, radiusMax-1f, mPaint);
        //
        int frameY = rgbToY();
        int color = Color.argb(255, 255 - frameY, 255 - frameY, 255 - frameY);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        canvas.drawCircle(centerX, centerY, radiusMax-2f, mPaint);
    }

    private static final int DEFAULT_SIZE = 100;

    private int calculateMeasure(int measureSpec) {
        int result = (int) (DEFAULT_SIZE * getResources().getDisplayMetrics().density);
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasure(widthMeasureSpec), calculateMeasure(heightMeasureSpec));
    }


    public int rgbToY() {
        float a, r, g, b, y, rgby;
        a = ((mColor & 0xff000000) >>> 24) / 255f;
        r = ((mColor & 0x00ff0000) >> 16) / 255f;
        g = ((mColor & 0x0000ff00) >> 8) / 255f;
        b = ((mColor & 0x000000ff)) / 255f;
        rgby = (float) (255f * (0.21 * r + 0.72 * g + 0.07 * b));
        if (rgby < 128f) {
            y = (float) (255f * (0.35 * a + 0.65 * (rgby / 255f)));
        } else {
            y = (float) (255f * (0.65 * a + 0.35 * (rgby / 255f)));
        }
        //Log.i(TAG, "***** rgbToY = " + (int) y);
        return (int) y;
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

} // class ColorImgButton

