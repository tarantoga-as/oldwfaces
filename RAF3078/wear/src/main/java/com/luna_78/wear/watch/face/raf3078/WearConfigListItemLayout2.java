package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.support.wearable.view.CircledImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.luna_78.wear.watch.face.raf3078.R;

/**
 * Created by buba on 13/03/15.
 */
public class WearConfigListItemLayout2 extends FrameLayout /*LinearLayout*/
        implements WearableListView.OnCenterProximityListener
{

    private static final String TAG = "WLI";

    private CircledImageView mOptionIcon;
    private TextView mOptionName;


    public WearConfigListItemLayout2(Context context) {
        this(context, null);
    }

    public WearConfigListItemLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearConfigListItemLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public void onCenterPosition(boolean b) {
        mOptionName.setAlpha(1f);
        //((GradientDrawable) mOptionIcon.getDrawable()).setColor(0xffff4737);
        mOptionIcon.setCircleRadius(35f);
        mOptionIcon.setAlpha(1f);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        //((GradientDrawable) mOptionIcon.getDrawable()).setColor(0xff999999);
        mOptionName.setAlpha(0.5f);
        mOptionIcon.setCircleRadius(25f);
        mOptionIcon.setAlpha(0.5f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Log.i(TAG, "=== onFinishInflate");
        //
        mOptionIcon = (CircledImageView) findViewById(R.id.option_image);
        mOptionName = (TextView) findViewById(R.id.option_text);

//        Log.i(TAG, "=== mOptionIcon height=" + mOptionIcon.getHeight() + ", width=" + mOptionIcon.getWidth());
//        Log.i(TAG, "=== mOptionIcon max height=" + mOptionIcon.getMaxHeight() + ", max width=" + mOptionIcon.getMaxWidth());
    }

} // class WearConfigListItemLayout2
