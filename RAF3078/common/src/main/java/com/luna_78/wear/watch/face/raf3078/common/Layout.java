package com.luna_78.wear.watch.face.raf3078.common;

import android.graphics.Bitmap;
import android.os.Bundle;

/**
 * Created by buba on 27/05/15.
 */
public class Layout {
    public Bundle config;
    public Bitmap iconDense;
    public Bitmap iconAmbient;
    public String iconDenseFileName;
    public String iconAmbientFileName;
    public String name;
    public long deleteRequestMs;
    volatile static int count = 0;

    public Layout() {
        long timeStampMs = System.currentTimeMillis();
        count++;
//        String denseFileName, ambientFileName;
//        ambientFileName = String.valueOf(timeStampMs + count) + "_A";
//        denseFileName = String.valueOf(timeStampMs + count) + "_D";
        config = null;
        iconAmbient = null;
        iconDense = null;
        iconDenseFileName = String.valueOf(timeStampMs + count) + "_D";
        iconAmbientFileName = String.valueOf(timeStampMs + count) + "_A";
        name = null;
        deleteRequestMs = 0;
    }
}
