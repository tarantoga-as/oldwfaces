package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;

/**
 * Created by buba on 30/10/15.
 */
public class DelayedOps {

    private static final String TAG = ACommon.TAG_PREFIX + "DELAYED_OPS";

    private Handler mDelayedTasksHandler;



    public DelayedOps(Handler handler) {
        if (null != handler) mDelayedTasksHandler = handler;
        else mDelayedTasksHandler = new Handler();
    }



    private PowerManager.WakeLock mWakeLock;

    Runnable taskRunReleaseLock = new Runnable() {
        @Override
        public void run() {
            if (ACommon.L) Log.i(TAG, "***** taskRunReleaseLock enters run()");
            mWakeLock.release();
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    };

    public long wakeUpScreen(long delayMS, Context context) {
        if (delayMS == 0) return 0L;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(mWakeLock == null) {
            //mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP,"CubeWatch");
                /*
                https://developer.android.com/training/scheduling/wakelock.html
                    "One legitimate case for using a wake lock might be a background service that needs
                    to grab a wake lock to keep the CPU running to do work while the screen is off.
                    Again, though, this practice should be minimized because of its impact on battery life."
                */
            mWakeLock = powerManager.newWakeLock(
                    (PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "wakeLock");
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            //AWearFaceService.this
            mWakeLock.acquire();
        }
        if(!mWakeLock.isHeld()) mWakeLock.acquire();    //if called a second time and isn't locked
        mDelayedTasksHandler.removeCallbacks(taskRunReleaseLock);  //if already waiting, then we'll start the time over
        mDelayedTasksHandler.postDelayed(taskRunReleaseLock, delayMS);
        if (ACommon.L) Log.i(TAG, "***** wakeUpScreen post taskRunReleaseLock delayed by " + delayMS + " ms");

        //Log.i(TAG, "wakeUpScreen: " + delay);
        return System.currentTimeMillis(); //appearanceModificationTimeMs = System.currentTimeMillis();
    }

}
