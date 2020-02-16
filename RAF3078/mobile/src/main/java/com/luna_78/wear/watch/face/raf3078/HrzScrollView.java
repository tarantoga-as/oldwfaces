package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by buba on 25/02/15.
 */
public class HrzScrollView extends HorizontalScrollView {

    private static final String TAG = "HSCR";

    public HrzScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HrzScrollView(Context context) {
        super(context);
    }

    public HrzScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

/*
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float curX, curY;

        //return super.onTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                curX = ev.getX();
                curY = ev.getY();
                Log.i(TAG, "*** onTouchEvent, curX=" + curX + ", curY" + curY);
                break;
        }
        return false;
    }
*/

    @Override
    public boolean isInEditMode() {
        //return super.isInEditMode();
        return true;
    }

} // class HrzScrollView
// com.luna_78.airforceru.FragmentLayouts.HrzScrollView
