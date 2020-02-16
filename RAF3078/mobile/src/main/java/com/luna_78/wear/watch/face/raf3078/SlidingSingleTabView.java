package com.luna_78.wear.watch.face.raf3078;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by buba on 10/03/15.
 */
public class SlidingSingleTabView extends RelativeLayout {

    private static final String TAG = "TCV";

    TextView textView;
    ValueAnimator mColorAnimator;



    public SlidingSingleTabView(Context context) {
        super(context);
    }

    public SlidingSingleTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingSingleTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public TabCollectionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    public boolean isInEditMode() {
        //return super.isInEditMode();
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int dimMin = Math.min(width, height);
//        Log.i(TAG, "+++++ draw, width=" + width + ", height=" + height);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //
//        textView = (TextView) findViewById(R.id.textView);
//        //Log.i(TAG, "+++++ onFinishInflate, textView=" + textView);
//        View parent = (View) textView.getParent();
//        //Log.i(TAG, "+++++ onFinishInflate, parent=" + parent + ", height=" + parent.getHeight() + ", width=" + parent.getWidth());
//
//        mColorAnimator = ObjectAnimator.ofInt(textView, "textColor", 0xffffff00, 0xffffffff);
//        mColorAnimator.setDuration(500);
//        mColorAnimator.setEvaluator(new ArgbEvaluator());
//            //colorAnimator.setRepeatCount(1);
//            //colorAnimator.setRepeatMode(Va);
//        //mColorAnimator.start();

    }



    public void setTitle(String title) {

        //LayoutInflater inflator  = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View v = inflator.inflate(R.layout.action_bar, null);
        //View v = getActionbar().getCustomView();

        //TextView txtView = (TextView) v.findViewById(R.id.actionbarTitle);
        String newTitle = title.toUpperCase();
        textView.setText(newTitle);
        //textView.setTextColor(0xffffffff);

        View parent = (View) textView.getParent();
        //Log.i(TAG, "+++++ setTitle, parent=" + parent + ", height=" + parent.getHeight() + ", width=" + parent.getWidth());

        //textView.invalidate();
    }


//    public void Flash() {
//        mColorAnimator.start();
//        //new AnimationThread(mColorAnimator).start();
//    }


} // class TabCollectionView
