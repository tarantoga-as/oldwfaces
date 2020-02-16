package com.luna_78.wear.watch.face.raf3078;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class APhoneServiceAutostart
        extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, APhoneService.class);
        context.startService(service);
    }
} // class APhoneServiceAutostart
