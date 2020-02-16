package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.luna_78.wear.watch.face.raf3078.R;

/**
 * Created by buba on 06/03/15.
 */
public class WearConfigListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private static final String TAG = "WLI";

    private ImageView mOptionIcon;
    private TextView mOptionName;


    public WearConfigListItemLayout(Context context) {
        this(context, null);
    }

    public WearConfigListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearConfigListItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public void onCenterPosition(boolean b) {
        mOptionName.setAlpha(1f);
        //((GradientDrawable) mOptionIcon.getDrawable()).setColor(0xffff4737);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        //((GradientDrawable) mOptionIcon.getDrawable()).setColor(0xff999999);
        mOptionName.setAlpha(0.5f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Log.i(TAG, "=== onFinishInflate");
        //
        mOptionIcon = (ImageView) findViewById(R.id.option_icon);
        mOptionName = (TextView) findViewById(R.id.option_name);

//        Log.i(TAG, "=== mOptionIcon height=" + mOptionIcon.getHeight() + ", width=" + mOptionIcon.getWidth());
//        Log.i(TAG, "=== mOptionIcon max height=" + mOptionIcon.getMaxHeight() + ", max width=" + mOptionIcon.getMaxWidth());
    }


} // class WearConfigListItemLayout
