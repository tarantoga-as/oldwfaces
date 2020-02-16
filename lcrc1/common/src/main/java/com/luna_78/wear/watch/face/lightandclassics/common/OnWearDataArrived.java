package com.luna_78.wear.watch.face.lightandclassics.common;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by buba on 31/10/15.
 */
public interface OnWearDataArrived {
    boolean onWearData(String uriHost, String uriPath, DataMap dataMap);
}
