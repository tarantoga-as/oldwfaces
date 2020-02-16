package com.luna_78.wear.watch.face.lightandclassics.common;

/**
 * Created by buba on 30/10/15.
 */
public class SynBoolean {

    /*volatile */final Object   mLock = new Object();
    /*volatile */boolean        mValue;


    public SynBoolean(boolean initValue) { mValue = initValue; }


    public boolean getValue() {
        boolean result;
        synchronized (mLock) {
            result = mValue ? true : false;
        }
        return result;
    }


    public void setValue(boolean newValue) {
        synchronized (mLock) {
            mValue = newValue ? true : false;
        }
    }
}
