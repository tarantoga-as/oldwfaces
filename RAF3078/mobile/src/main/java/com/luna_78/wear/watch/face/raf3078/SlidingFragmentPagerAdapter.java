package com.luna_78.wear.watch.face.raf3078;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.TextView;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;

/**
 * Created by buba on 12/03/15.
 */
public class SlidingFragmentPagerAdapter extends FragmentStatePagerAdapter { // FragmentPagerAdapter

    private static final String TAG = "SFPA";

//    final int TAB_PAGE_COUNT = 3;
//    final int TAB_PAGE_LAYOUT_IND = 0;
//    final int TAB_PAGE_SETTINGS_IND = 1;
//    final int TAB_PAGE_COLLECTION_IND = 2;

    private String tabTitles[] = new String[] { "LAYOUT", "SETTINGS", "COLLECTION (000)" };
    //private Context mContext;

    public TextView tabTitleView[] = new TextView[ACommon.TAB_PAGE_COUNT];

    //public PageFragmentCollection mCollectionFragment;

    public SlidingFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        //this.mContext = context;
    }

    @Override
    public int getCount() {
        return ACommon.TAB_PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frg = null;

        switch (position) {
            case 0:
                frg = PageFragmentLayout.newInstance(position + 1);
                break;
            case 1:
                frg = PageFragmentSettings.newInstance(position + 1);
                break;
            case 2:
                frg = PageFragmentCollection.newInstance(position + 1);
                //mCollectionFragment = (PageFragmentCollection) frg;
                break;
        }

        return frg;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    public void setTabTitleView(int ind, TextView txtView) {
        tabTitleView[ind] = txtView;
    }

    public void setTabTitleText(int ind, String text) {
        String txtUpper = text.toUpperCase();
        tabTitleView[ind].setText(txtUpper);
        tabTitles[ind] = txtUpper;
    }
    public void setTabTitleColor(int ind, int color) {
        tabTitleView[ind].setTextColor(color);
    }
    public void setTabTitleBackgroundColor(int ind, int color) {
        tabTitleView[ind].setBackgroundColor(color);
    }
    public void doTabTitleFlash(int ind) {
        ValueAnimator mColorAnimator;
        TextView txtView = tabTitleView[ind];
        int color = txtView.getCurrentTextColor();
        //Log.i(TAG, "@@@ doTabTitleFlash, color=" + color);
        mColorAnimator = ObjectAnimator.ofInt(txtView, "textColor", 0xffffff00, color);
        mColorAnimator.setDuration(500);
        mColorAnimator.setEvaluator(new ArgbEvaluator());
        mColorAnimator.start();
    }
} // class SampleFragmentPagerAdapter
