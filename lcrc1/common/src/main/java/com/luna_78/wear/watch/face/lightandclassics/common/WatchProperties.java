package com.luna_78.wear.watch.face.lightandclassics.common;

/**
 * Created by buba on 26/10/15.
 */
public class WatchProperties {
    boolean         mDetermined;
    int             mFullness;
    private final static int EMPTY                  = 0x0;
    private final static int AMBIENT_IS_SET         = 0x1;
    private final static int TAPPABLE_IS_SET        = 0x10;
    private final static int INSETS_IS_SET          = 0x100;
    private final static int DIMENSIONS_IS_SET      = 0x1000;
    private final static int FULL                   = 0x1111;

    boolean         mLowBit, mBurnIn, mIsRound, mTappable;
    int             mChinSize, mWidth, mHeight;
    float           centerX, centerY;

    public WatchProperties() {
        mFullness = EMPTY;
        mDetermined = false;
    }

    public boolean isDetermined() { return mDetermined; }
    public boolean isSetAmbient() { return (AMBIENT_IS_SET == (mFullness & AMBIENT_IS_SET)); }
    public boolean isSetInsets() { return (INSETS_IS_SET == (mFullness & INSETS_IS_SET)); }
    public boolean isSetDimensions() { return (DIMENSIONS_IS_SET == (mFullness & DIMENSIONS_IS_SET)); }
    public boolean isSetTappable() { return (TAPPABLE_IS_SET == (mFullness & TAPPABLE_IS_SET)); }

    public boolean getLowBit() { return mLowBit; }
    public boolean isRound() { return mIsRound; }
    public int getWidth() { return mWidth; }
    public int getHeight() { return mHeight; }
    public float getCenterX() { return centerX; }
    public float getCenterY() { return centerY; }
    public boolean isTappable() { return mTappable; }

    public void setTapEnabled(boolean tapEnabled) {
        mTappable = tapEnabled;
        applyFullness(TAPPABLE_IS_SET);
    }

    public void setAmbientValues(boolean burnIn, boolean lowBit) {
        mBurnIn = burnIn;
        mLowBit = lowBit;
        applyFullness(AMBIENT_IS_SET);
    }

    public void setInsetsValues(boolean isRound, int chinSize) {
        mIsRound = isRound;
        mChinSize = chinSize;
        applyFullness(INSETS_IS_SET);
    }

    public void setDimensionsValues(int width, int height) {
        mWidth = width;
        mHeight = height;
        centerX = width / 2f;
        centerY = height / 2f;
        applyFullness(DIMENSIONS_IS_SET);
    }

    private void applyFullness(int set) {
        mFullness |= set;
        if (FULL == mFullness) mDetermined = true;
    }

}
