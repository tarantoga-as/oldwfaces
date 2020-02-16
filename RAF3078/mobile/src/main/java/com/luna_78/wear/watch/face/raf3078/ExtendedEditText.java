package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by buba on 19/06/15.
 * https://boltingupandroid.wordpress.com/2013/02/22/detect-dismissal-of-the-soft-keyboard/
 * http://stackoverflow.com/questions/3425932/detecting-when-user-has-dismissed-the-soft-keyboard
 */
public class ExtendedEditText extends EditText {
    //com.luna_78.airforceru.ExtendedEditText

    private static final String TAG = "EET";

    private void init() {
        //Log.i(TAG, "((( init, text=[" + this.getText().toString() + "]");
        this.setEnabled(true);
        this.setText("");
        this.setTag(R.id.tag_element_name, null);
        this.setEnabled(false);
    }

    public ExtendedEditText(Context context) {
        super(context);
        init();
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    private ListView mListView;
    public void setListView(ListView listView) { mListView = listView; }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        //Log.i(TAG, "((( onKeyPreIme, event=" + event);
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            clearEditElementNameTuple();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void clearEditElementNameTuple() {
        //Log.i(TAG, "((( clearEditElementNameTuple");
        if (mListView != null) mListView.requestFocus();
        this.setEnabled(true);
        this.setTag(R.id.tag_element_name, null);
        this.setText("");
        this.setEnabled(false);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
//        if (visibility != VISIBLE) {
//            clearEditElementNameTuple();
//        }
        //Log.i(TAG, "((( onVisibilityChanged, visibility=" + visibility);
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        //Log.i(TAG, "((( onScreenStateChanged, screenState=" + screenState);
        super.onScreenStateChanged(screenState);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //Log.i(TAG, "((( onGenericMotionEvent, event=" + event);
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.i(TAG, "((( onTouchEvent, event=" + event);
        return super.onTouchEvent(event);

//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        return false;

    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        //Log.i(TAG, "((( onFocusChanged, focused=" + focused + ", direction=" + direction + ", rect=" + previouslyFocusedRect);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        //Log.i(TAG, "((( onWindowFocusChanged, hasWindowFocus=" + hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }
} // class ExtendedEditText
