package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;

public class APhoneServiceAutostart extends BroadcastReceiver {

    //private static final boolean L = true;
    private static final String TAG = ACommon.TAG_PREFIX + "AUTOEXEC";

//    public APhoneServiceAutostart() {
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACommon.L) Log.i(TAG, "BOOT COMPLETE BROADCAST RECEIVED");
        context.startService(new Intent(context, APhoneService.class));
    }
}
