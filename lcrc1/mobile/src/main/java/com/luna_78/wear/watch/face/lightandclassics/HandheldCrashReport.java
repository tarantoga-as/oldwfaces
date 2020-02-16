package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;

import org.acra.ACRA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Created by buba on 30/10/15.
 */
public class HandheldCrashReport {

    private static final String TAG = ACommon.TAG_PREFIX + "HH_CRASH_REPORT";
    //private static final boolean L = ACommon.L;

    int                 mPeriodicCheckCrashInterval = 600 * 1000; // milliseconds
    Object              mLockCrashReport = new Object();
    boolean             mConnectivityReceiverRegistered = false;
    Context             mContext;
    Handler             mHandler;


    public HandheldCrashReport(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        // initial delay: to let ACRA to send pending reports by itself - is it working???
        mHandler.postDelayed(taskPeriodicCheckCrashReport, 60000);
    }


    public void quitWork() {
        registerConnectivityReceiver(false);
        mHandler.removeCallbacks(taskPeriodicCheckCrashReport);
    }


    public void registerConnectivityReceiver(boolean register) {
        if (register) {
            synchronized (mLockCrashReport) {
                if (!mConnectivityReceiverRegistered) {
                    mContext.registerReceiver(mConnectivityInfoReceiver,
                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                    );
                    mConnectivityReceiverRegistered = true;
                }
            }
        } else {
            synchronized (mLockCrashReport) {
                if (!mConnectivityReceiverRegistered) return;
                mContext.unregisterReceiver(mConnectivityInfoReceiver);
                mConnectivityReceiverRegistered = false;
            }
        }
    }


    final BroadcastReceiver mConnectivityInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if(intent == null || intent.getExtras() == null) return;
            //mConnected = checkConnected();
            if (checkConnected() && checkCrashReportPresent()) forceSendCrashReports();
        }
    };

    private boolean checkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    private boolean checkCrashReportPresent() {
        if (ACommon.L) Log.i(TAG, "checkCrashReportPresent()");
        // examples: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
        for (String fileName : mContext.fileList()) {
            if (fileName.matches("^[0-9]+-approved.stacktrace")) {
                return true;
            }
        }
        return false;
    }


    Runnable taskPeriodicCheckCrashReport = new Runnable() {
        @Override
        public void run() {
            if (checkCrashReportPresent()) {
                if (checkConnected()) {
                    forceSendCrashReports();
                    registerConnectivityReceiver(false);
                } else {
                    registerConnectivityReceiver(true);
                }
            } else {
                registerConnectivityReceiver(false);
            }
            mHandler.postDelayed(taskPeriodicCheckCrashReport, mPeriodicCheckCrashInterval);
        }
    };

    private void forceSendCrashReports() {
        if (ACommon.L) Log.i(TAG, "forceSendCrashReports()");
        for (String fileName : mContext.fileList()) {
            if (fileName.matches("^[0-9]+-IS_SILENT.stacktrace")) {
                //return;
                File file = new File(mContext.getFilesDir(), fileName);
                file.delete();
            }
        }
        //ACRA.getErrorReporter().reportBuilder().forceSilent().message(ACommon.PENDING_CRASH_REPORT).send();
        askToSendPendingReports();
    }

    public static void askToSendPendingReports() {
        if (ACommon.L) Log.i(TAG, "askToSendPendingReports()");
        ACRA.getErrorReporter().reportBuilder().forceSilent().message(ACommon.PENDING_CRASH_REPORT).send();
    }

}
