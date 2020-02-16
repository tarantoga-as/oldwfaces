package com.luna_78.wear.watch.face.raf3078;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.AppPreferences;
import com.luna_78.wear.watch.face.raf3078.common.DemoPackData;
import com.luna_78.wear.watch.face.raf3078.common.Inscription;
import com.luna_78.wear.watch.face.raf3078.common.Layout;
import com.luna_78.wear.watch.face.raf3078.common.LayoutsPalette;

import org.acra.ACRA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class APhoneService
        extends Service
        implements
            DataApi.DataListener,
            NodeApi.NodeListener,
            MessageApi.MessageListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "SRVC";

    public String productId;

    private final IBinder mServiceBinder = new LocalBinder();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private String smPeerId = null;
    private Object mLockPeerId = new Object();
    private String mLocalPeerId = null;
    //public void setPeerId(String peerId) { smPeerId = peerId; }
    private static final String PEERID_PERSISTANT_FILE = "peerid";
    private void savePeerIdToFile(String peerId) {
        try {
            FileOutputStream fos = this.openFileOutput(PEERID_PERSISTANT_FILE, MODE_PRIVATE);
            OutputStreamWriter out = new OutputStreamWriter(fos);
            //Log.i(TAG, "((( savePeerIdToFile, peerId=" + peerId);
            out.write(peerId, 0, peerId.length());
            String strNew = "\n";
            out.write(strNew, 0, strNew.length());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String restorePeerIdFromFile() {
        String result = null;
        try {
            InputStream in = this.openFileInput(PEERID_PERSISTANT_FILE);
            if (null == in) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String strVal;
            if ((strVal = reader.readLine()) != null) {
                result = strVal;
                //Log.i(TAG, "((( restorePeerIdFromFile, peerId=" + result);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private void saveColorPalette(Context context) {
//        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(STORETEXT, 0));
//        out.("");
//        out.close();
        //Log.i(TAG, "((( saveColorPalette");
    }
    private void restoreColorPalette(Context context) {
//        InputStream in = openFileInput(STORETEXT);
//        if (in != null) {
//            InputStreamReader tmp = new InputStreamReader(in);
//            BufferedReader reader = new BufferedReader(tmp);
//            String str;
//            StringBuilder buf = new StringBuilder();
//            while ((str = reader.readLine()) != null) {
//                buf.append(str + "n");
//            }
//            in.close();
//        }
        //Log.i(TAG, "((( restoreColorPalette");
    }



    boolean mRegisteredBatteryReceiver = false;
    private float mPhoneBattery = ACommon.BATTERY_LEVEL_UNSPECIFIED;
    //
    boolean mShowHandheldBattery = false;
    //
    public boolean getShowHandheldBatteryTrigger() { return mShowHandheldBattery; }
    public boolean setShowHandheldBatteryTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setShowHandheldBatteryTrigger, newval=" + newval + ", mShowHandheldBattery=" + mShowHandheldBattery);
        boolean oldval = mShowHandheldBattery;
        mShowHandheldBattery = newval;
        if (oldval != newval) {
            if (newval == true) {
                registerBatteryReceiver();
            } else {
                unregisterBatteryReceiver();
            }
        }
        return oldval;
    }
    //
    boolean mShowAnimation = false;
    public boolean getShowAnimationTrigger() { return mShowAnimation; }
    public boolean setShowAnimationTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setShowAnimationTrigger, newval=" + newval + ", mShowAnimation=" + mShowAnimation);
        boolean oldval = mShowAnimation;
        mShowAnimation = newval;
//        if (oldval != newval) {
//            if (newval == true) {
//                registerBatteryReceiver();
//            } else {
//                unregisterBatteryReceiver();
//            }
//        }
        return oldval;
    }
    //
    boolean mShowHrDigitsRelief = true;
    public boolean getShowHrDigitsReliefTrigger() { return mShowHrDigitsRelief; }
    public boolean setShowHrDigitsReliefTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setShowHrDigitsReliefTrigger, newval=" + newval + ", mShowHrDigitsRelief=" + mShowHrDigitsRelief);
        boolean oldval = mShowHrDigitsRelief;
        mShowHrDigitsRelief = newval;
        return oldval;
    }
    //
    boolean mShowInscriptionsRelief = true;
    public boolean getShowInscriptionsReliefTrigger() { return mShowInscriptionsRelief; }
    public boolean setShowInscriptionsReliefTrigger(boolean newval, final String srcTag) {
        boolean oldval = mShowInscriptionsRelief;
        mShowInscriptionsRelief = newval;
        return oldval;
    }
    //
    boolean mShowDialGradient = true;
    public boolean getShowDialGradientTrigger() { return mShowDialGradient; }
    public boolean setShowDialGradientTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setShowDialGradientTrigger, newval=" + newval + ", mShowDialGradient=" + mShowDialGradient);
        boolean oldval = mShowDialGradient;
        mShowDialGradient = newval;
        return oldval;
    }
    //
    boolean mRespectBurnInTrigger = true;
    public boolean getRespectBurnInTrigger() { return mRespectBurnInTrigger; }
    public boolean setRespectBurnInTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setRespectBurnInTrigger, newval=" + newval + ", mRespectBurnInTrigger=" + mRespectBurnInTrigger);
        boolean oldval = mRespectBurnInTrigger;
        mRespectBurnInTrigger = newval;
        return oldval;
    }
    //
    boolean mRespectLowBitTrigger = true;
    public boolean getRespectLowBitTrigger() { return mRespectLowBitTrigger; }
    public boolean setRespectLowBitTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setRespectLowBitTrigger, newval=" + newval + ", oldval=" + mRespectLowBitTrigger);
        boolean oldval = mRespectLowBitTrigger;
        mRespectLowBitTrigger = newval;
        return oldval;
    }
    //
    // CFG_RESPECT_LOWBIT   EVT_HHCFG_SET_RESPECT_LOWBIT    EVT_WEARCFG_SET_RESPECT_LOWBIT
    // CFG_SWEEP_SECONDS    EVT_HHCFG_SET_SWEEP             EVT_WEARCFG_SET_SWEEP
    //
    boolean mSweepTrigger = true;
    public boolean getSweepTrigger() { return mSweepTrigger; }
    public boolean setSweepTrigger(boolean newval, final String srcTag) {
        //Log.i(TAG, "===" + srcTag + "=== setSweepTrigger, newval=" + newval + ", oldval=" + mSweepTrigger);
        boolean oldval = mSweepTrigger;
        mSweepTrigger = newval;
        return oldval;
    }
    //
    int mAuxBevelColor = ACommon.BEVEL_FROM_AUX;
    public int getAuxBevelColor() { return mAuxBevelColor; }
    public int setAuxBevelColor(int newval, final String srcTag) {
        int oldval = mAuxBevelColor;
        mAuxBevelColor = newval;
        return oldval;
    }
    //
    int mInvertGradient = ACommon.GD_INVERT_NONE;
    public int getInvertGradient() { return mInvertGradient; }
    public int setInvertGradient(int newval, final String srcTag) {
        int oldval = mInvertGradient;
        mInvertGradient = newval;
        return oldval;
    }
    //
    public void setCurrentToggles(Bundle config) {
        final String tag = "setCurrentToggles";
        //setShowHandheldBatteryTrigger(config.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false), tag);
        setShowAnimationTrigger(config.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false), tag);
        setShowHrDigitsReliefTrigger(config.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true), tag);
        setShowDialGradientTrigger(config.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true), tag);
        setAuxBevelColor(config.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX), tag);
        setInvertGradient(config.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE), tag);
    }

    private int mSettingsWakeDelay = 5;
    public void setSettingsWakeDelay(int delay) { mSettingsWakeDelay = delay; }
    public int getSettingsWakeDelay() { return mSettingsWakeDelay; }



    //private NotificationManager mNotificationManager;

    GoogleApiClient mGoogleApiClient;

    private Bitmap lastDenseScreenshot;
    public Bitmap getLastDenseScreenshot() { return lastDenseScreenshot; }
    public void setLastDenseScreenshot(Bitmap bmp) {
        if (null == bmp && lastDenseScreenshot != null) lastDenseScreenshot.recycle();
        lastDenseScreenshot = bmp;
    }
    //
    private Bitmap lastAmbientScreenshot;
    public Bitmap getLastAmbientScreenshot() { return lastAmbientScreenshot; }
    public void setLastAmbientScreenshot(Bitmap bmp) {
        if (null == bmp && lastAmbientScreenshot != null) lastAmbientScreenshot.recycle();
        lastAmbientScreenshot = bmp;
    }

    private Bitmap lastPlateBitmap;
    public void setPlateBitmap(Bitmap bmp) {
        if (null == bmp && lastPlateBitmap != null) lastPlateBitmap.recycle();
        lastPlateBitmap = bmp;
    }
    public Bitmap getPlateBitmap() { return lastPlateBitmap; }


    private Bundle mCurrentConfig;
    public Bundle getCurrentConfig() { return mCurrentConfig; }
    public int getWatchLayoutIndex() {
        if (null != mCurrentConfig) return mCurrentConfig.getInt(ACommon.CFG_LAYOUT_INDEX);
        return Inscription.DEFAULT_LAYOUTINDEX_UNDEFINED;
    }
    // values for background (main circle dial) gradient
    public float getDgFirstStop() {
        if (null != mCurrentConfig) return mCurrentConfig.getFloat(ACommon.CFG_DG_FIRST_STOP);
        else return 0f;
    }
    public float getDgHalfEdgeStop() {
        if (null != mCurrentConfig) return mCurrentConfig.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP);
        else return 0f;
    }
    public int getDgEdgeAlpha() {
        if (null != mCurrentConfig) return mCurrentConfig.getInt(ACommon.CFG_DG_EDGE_ALPHA);
        else return 0;
    }
    public void putDgFirstStop(float value) {
        if (null != mCurrentConfig) mCurrentConfig.putFloat(ACommon.CFG_DG_FIRST_STOP, value);
    }
    public void putDgHalfEdgeStop(float value) {
        if (null != mCurrentConfig) mCurrentConfig.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP, value);
    }
    public void putDgEdgeAlpha(int value) {
        if (null != mCurrentConfig) mCurrentConfig.putInt(ACommon.CFG_DG_EDGE_ALPHA, value);
    }
    // values for aux dials gradient
    public float getDgFirstStop1() {
        if (null != mCurrentConfig) return mCurrentConfig.getFloat(ACommon.CFG_DG_FIRST_STOP_1);
        else return 0f;
    }
    public float getDgHalfEdgeStop1() {
        if (null != mCurrentConfig) return mCurrentConfig.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1);
        else return 0f;
    }
    public int getDgEdgeAlpha1() {
        if (null != mCurrentConfig) return mCurrentConfig.getInt(ACommon.CFG_DG_EDGE_ALPHA_1);
        else return 0;
    }
    public void putDgFirstStop1(float value) {
        if (null != mCurrentConfig) mCurrentConfig.putFloat(ACommon.CFG_DG_FIRST_STOP_1, value);
    }
    public void putDgHalfEdgeStop1(float value) {
        if (null != mCurrentConfig) mCurrentConfig.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, value);
    }
    public void putDgEdgeAlpha1(int value) {
        if (null != mCurrentConfig) mCurrentConfig.putInt(ACommon.CFG_DG_EDGE_ALPHA_1, value);
    }



    private AppPreferences mWearAppPreference;
    public AppPreferences getWearAppPreference() { return mWearAppPreference; }

    private AppPreferences.TzList[] mTzList;
    public AppPreferences.TzList[] getTzList() { return mTzList; }


    public DemoPackData[] demoPackData = new DemoPackData[DemoPackData.NUM_DEMOPACK_PARAMETERS];
    //
    public void setDemoPackDataValues(DemoPackData from[]) {
        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
            demoPackData[i].trigger = from[i].trigger;
            demoPackData[i].value = from[i].value;
        }
    }


    public boolean testServiceMethod(String arg) {
        //Log.i(TAG, "((( testServiceMethod, arg = " + arg);
        return true;
    }



    public volatile LayoutsPalette gLayoutsPalette = new LayoutsPalette(this);
    public Integer gLayoutsPaletteIndex = null;
    public void RestoreLayoutsPalette() {
        //Log.i(TAG, "((( RestoreLayoutsPalette");
        String fileName = getString(R.string.configFileName);
        gLayoutsPalette.loadFromXmlFile(fileName);
        broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
    } // RestoreLayoutsPalette
    //


    ArrayList<String> mIconFileToDelete = new ArrayList<>();
    Object mLockIconFileToDelete = new Object();
    //
    public void clearLayoutElementFiles(Layout element) {
        if (null != element.iconDenseFileName) {
            synchronized (mLockIconFileToDelete) {
                if (null != element.iconDenseFileName) mIconFileToDelete.add(element.iconDenseFileName);
                if (null != element.iconAmbientFileName) mIconFileToDelete.add(element.iconAmbientFileName);
            }
            mServiceHandler.post(taskClearLayoutElementFiles);
        }
    }
    private Runnable taskClearLayoutElementFiles = new Runnable() {

        @Override
        public void run() {
            ArrayList<String> filesToDelete;
            synchronized (mLockIconFileToDelete) {
                filesToDelete = new ArrayList<>(mIconFileToDelete);
                mIconFileToDelete.clear();
            }
            for (String fileName : filesToDelete) {
                //Log.i(TAG, "((( taskClearLayoutElementFiles, delete = " + fileName);
                deleteFile(fileName);
            }
        }
    };



    private Runnable taskRestoreLayoutsPalette = new Runnable() {
        @Override
        public void run() {
            //Log.i(TAG, "((( taskRestoreLayoutsPalette");
            String fileName = getString(R.string.configFileName);

            ACommon.createLayoutPaletteFile(getApplicationContext(), fileName);

            gLayoutsPalette.loadFromXmlFile(fileName);
            broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
        }
    };

    private Runnable taskSignalHoldOff = new Runnable() {
        @Override
        public void run() {
            //Log.i(TAG, "((( taskSignalHoldOff");
            broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF, System.currentTimeMillis());
        }
    };

    public void activityConnected(String peerId) {
        //Log.i(TAG, "((( activityConnected, peerId=" + peerId);
        //
        //todo: if HandheldCompanionConfigActivity is called by ArrivedDataImporterActivity, THERE IS NO PEERID (peerId is null) !!!!!!!
        //
        if (null != peerId) {
            synchronized (mLockPeerId) {
                smPeerId = peerId;
            }
            mServiceHandler.post(taskSavePeerId);
            //savePeerIdToFile(peerId);
            requestCurrentConfig();
        }
        mServiceHandler.post(taskSignalHoldOff);
    }



    boolean mIncludeIcons = false;
    Object mLockIncludeIcons = new Object();
    //
    public void requestSaveLayoutPalette(boolean includeIcons) {
        //Log.i(TAG, "((( requestSaveLayoutPalette");
        synchronized (mLockIncludeIcons) {
            mIncludeIcons = includeIcons;
        }
        mServiceHandler.post(taskSaveLayoutsPalette);
    }
    //
    private Runnable taskSaveLayoutsPalette = new Runnable() {
        @Override
        public void run() {
            boolean includeIcons;
            //Log.i(TAG, "((( taskSaveLayoutsPalette");
            synchronized (mLockIncludeIcons) {
                includeIcons = mIncludeIcons;
            }
            String fileName = getString(R.string.configFileName);
            boolean result = gLayoutsPalette.saveToXmlFile(fileName, includeIcons);
//            if (true == result) {
            broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_UPDATE, System.currentTimeMillis());
//            } else {
//                broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF, System.currentTimeMillis());
//            }
        }
    };





    public ArrayList<String> mLayoutsPalette = new ArrayList<>();
    public Integer mLayoutsPaletteIndex = null;
    //
    public void RestoreConfigPalette() {
        String cfn = getString(R.string.configFileName);

        ACommon.createLayoutPaletteFile(this, cfn);

        Bundle configPalette = ACommon.readPersistentDataFromFile(this, cfn);
        if (null == configPalette) return;
        //mLayoutsPalette = configPalette.getStringArrayList(ACommon.KEY_CONFIG_PALETTE);
        mLayoutsPalette = configPalette.getStringArrayList(ACommon.KEY_LAYOUTS_PALETTE);
        //Log.i(TAG, "%%%%% RestoreConfigPalette(), mLayoutsPalette.size() = " + mLayoutsPalette.size());
    } // RestoreConfigPalette
    //
    private void removeDedicatedIcons(Context context) {
        String[] files = context.fileList();
        int count=0;
        for (String file : files) {
            count++;
            //Log.i(TAG, "((( removeDedicatedIcons, file[" + count + "] = " + file);
            if (file.matches("^[0-9]+_[AD]$")) {
                //Log.i(TAG, "((( removeDedicatedIcons, DEDICATED ICON found");
                context.deleteFile(file);
            }
        }
    }


    String          mPaletteFilePath;
    InputStream     mPaletteInputStream;
    Object          mLockArrivedDataParameter = new Object();
    //
    public void requestReplaceLayoutsPalette(String paletteFilePath) {
        synchronized (mLockArrivedDataParameter) {
            mPaletteFilePath = paletteFilePath;
            mPaletteInputStream = null;
        }
        mServiceHandler.post(taskReplaceLayoutsPalette);
    }
    public void requestReplaceLayoutsPalette(InputStream fis) {
        synchronized (mLockArrivedDataParameter) {
            mPaletteFilePath = null;
            mPaletteInputStream = fis;
        }
        mServiceHandler.post(taskReplaceLayoutsPalette);
    }
    Runnable taskReplaceLayoutsPalette = new Runnable() {
        @Override
        public void run() {
            String paletteFilePath;
            InputStream paletteInputStream;
            synchronized (mLockArrivedDataParameter) {
                paletteFilePath = mPaletteFilePath;
                paletteInputStream = mPaletteInputStream;
            }
            if (null != paletteFilePath) {
                replaceLayoutsPalette(paletteFilePath);
            } else if (null != paletteInputStream) {
                replaceLayoutsPalette(paletteInputStream);
            } else {

            }
        }
    };
    //
    public void replaceLayoutsPalette(String paletteFilePath) {
        //Log.i(TAG, "#URI replaceLayoutsPalette file=" + paletteFilePath);
        LayoutsPalette arrivedLayoutsPalette = new LayoutsPalette(getApplicationContext());

        gLayoutsPalette.clear(getApplicationContext());
        //removeDedicatedIcons(this);

        if (arrivedLayoutsPalette.loadFromXmlFile(paletteFilePath)) {

            System.gc();
            gLayoutsPalette.addAll(arrivedLayoutsPalette.mPalette);
            gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
            broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
        }
        broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_2, System.currentTimeMillis());
    } // replaceLayoutsPalette
    public void replaceLayoutsPalette(InputStream fis) {
        //Log.i(TAG, "#URI replaceLayoutsPalette stream=" + fis);
        LayoutsPalette arrivedLayoutsPalette = new LayoutsPalette(getApplicationContext());

        gLayoutsPalette.clear(getApplicationContext());
        //removeDedicatedIcons(this);

        if (arrivedLayoutsPalette.loadFromXmlFile(fis, false)) {
            System.gc();
            gLayoutsPalette.addAll(arrivedLayoutsPalette.mPalette);
            gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
            broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
        }
        broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_2, System.currentTimeMillis());
    } // replaceLayoutsPalette
    //
    public void requestConcatenateLayoutsPalette(String paletteFilePath) {
        synchronized (mLockArrivedDataParameter) {
            mPaletteFilePath = paletteFilePath;
            mPaletteInputStream = null;
        }
        mServiceHandler.post(taskConcatenateLayoutsPalette);
    }
    public void requestConcatenateLayoutsPalette(InputStream fis) {
        synchronized (mLockArrivedDataParameter) {
            mPaletteFilePath = null;
            mPaletteInputStream = fis;
        }
        mServiceHandler.post(taskConcatenateLayoutsPalette);
    }
    Runnable taskConcatenateLayoutsPalette = new Runnable() {
        @Override
        public void run() {
            String paletteFilePath;
            InputStream paletteInputStream;
            synchronized (mLockArrivedDataParameter) {
                paletteFilePath = mPaletteFilePath;
                paletteInputStream = mPaletteInputStream;
            }
            if (null != paletteFilePath) {
                concatenateLayoutsPalette(paletteFilePath);
            } else if (null != paletteInputStream) {
                concatenateLayoutsPalette(paletteInputStream);
            } else {

            }
        }
    };
    public void concatenateLayoutsPalette(String paletteFilePath) {
        //Log.i(TAG, "#URI concatenateLayoutsPalette file=" + paletteFilePath);
        LayoutsPalette arrivedLayoutsPalette = new LayoutsPalette(getApplicationContext());
        if (arrivedLayoutsPalette.loadFromXmlFile(paletteFilePath, true)) {
            //gLayoutsPalette.clear();
            //System.gc();
            gLayoutsPalette.addAll(arrivedLayoutsPalette.mPalette);
            gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
            broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
        }
        broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_2, System.currentTimeMillis());
    } // concatenateLayoutsPalette
    public void concatenateLayoutsPalette(InputStream fis) {
        //Log.i(TAG, "#URI concatenateLayoutsPalette stream=" + fis);
        LayoutsPalette arrivedLayoutsPalette = new LayoutsPalette(getApplicationContext());
        if (arrivedLayoutsPalette.loadFromXmlFile(fis, true)) {
            //gLayoutsPalette.clear();
            //System.gc();
            gLayoutsPalette.addAll(arrivedLayoutsPalette.mPalette);
            gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
            broadcastEmptyToActivity(ACommon.EVT_LAYOUTS_PALETTE_CHANGED, System.currentTimeMillis());
        }
        broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_2, System.currentTimeMillis());
    } // concatenateLayoutsPalette






/*
    public void sendConfigFileByMail(String fname) {
        Context context = getApplicationContext();

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
//        File newFile = new File(rootPath, fname);
        File attachmentFileName = new File(context.getFilesDir(), fname);
        Uri contentUri = FileProvider.getUriForFile(context, "com.luna_78.airforceru.fileprovider", attachmentFileName);
        Log.i(TAG, "##### contentUri = " + contentUri.toString());

        grantUriPermissionToMailPackages(context, contentUri, emailIntent);

//        String to[] = {"buba@luna-78.com"};
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Russian Air Force Watchface Layouts Palette File");
        emailIntent.putExtra(Intent.EXTRA_STREAM, */
/*attachmentFileName*//*
 */
/*Uri.fromFile(file)*//*
 contentUri);
        //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }
    private void grantUriPermissionToMailPackages(Context context, Uri contentUri, Intent emailIntent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                Log.i(TAG, "##### packageName = " + packageName);
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }
*/




    boolean wfvIsWearRTL;
    int wfvScreenWidth, wfvScreenHeight;
    float wfvBurninMargin, wfvScreenCenterX, wfvScreenCenterY, wfvScreenRadius, wfvDialRadius;
    //
    boolean getWFVIsWearRTL() { return wfvIsWearRTL; }
    int getWFVScreenWidth() { return wfvScreenWidth; }
    int getWFVScreenHeight() { return wfvScreenHeight; }
    float getWFVBurninMargin() { return wfvBurninMargin; }
    float getWFVScreenCenterX() { return wfvScreenCenterX; }
    float getWFVScreenCenterY() { return wfvScreenCenterY; }
    float getWFVScreenRadius() { return wfvScreenRadius; }
    float getWFVDialRadius() { return wfvDialRadius; }
    //
    float wfvAuxAcx, wfvAuxAcy, wfvAuxAdim, wfvAuxBcx, wfvAuxBcy, wfvAuxBdim, wfvAuxCcx, wfvAuxCcy, wfvAuxCdim;
    //
    float getWFVauxAcx() { return wfvAuxAcx; }
    float getWFVauxAcy() { return wfvAuxAcy; }
    float getWFVauxAdim() { return wfvAuxAdim; }
    float getWFVauxBcx() { return wfvAuxBcx; }
    float getWFVauxBcy() { return wfvAuxBcy; }
    float getWFVauxBdim() { return wfvAuxBdim; }
    float getWFVauxCcx() { return wfvAuxCcx; }
    float getWFVauxCcy() { return wfvAuxCcy; }
    float getWFVauxCdim() { return wfvAuxCdim; }


    public class LocalBinder extends Binder {
        APhoneService getService() {
            //Log.i(TAG, "SvcBinder");
            return APhoneService.this;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // Handler
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
        }
    } // class ServiceHandler

    class SendThroughWearNetworkThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendThroughWearNetworkThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
                if (result.getStatus().isSuccess()) {
                    //Log.i(TAG, "**** DataMap: " + dataMap + " sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    //Log.i(TAG, "**** ERROR: failed to send DataMap");
                }
            }
        }
    } // class SendThroughWearNetworkThread

    final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level<0 || scale<0) {
                mPhoneBattery = ACommon.BATTERY_LEVEL_UNSPECIFIED;
            } else {
                mPhoneBattery = ((float)level / (float)scale) * 100.0f;
            }
            //Log.i(TAG, "*** PhoneBattery: int=" + level + ", float=" + mPhoneBattery);
            DataMap dataMap = new DataMap();
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_PHONE_BATTERY_SAMPLE);
            dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
            dataMap.putFloat(ACommon.KEY_LEVEL, mPhoneBattery);
            //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, null).start();
        }
    };
    private void unregisterBatteryReceiver () {
        //Log.i(TAG, "=== unregisterBatteryReceiver, mShowHandheldBattery=" + mShowHandheldBattery + ", mRegisteredBatteryReceiver=" + mRegisteredBatteryReceiver);
        if (mRegisteredBatteryReceiver) {
            //Log.i(TAG, "!!! unregisterBatteryReceiver");
            mRegisteredBatteryReceiver = false;
            APhoneService.this.unregisterReceiver(mBatteryInfoReceiver);
        }
    }
    private void registerBatteryReceiver () {
        //Log.i(TAG, "=== registerBatteryReceiver, mShowHandheldBattery=" + mShowHandheldBattery + ", mRegisteredBatteryReceiver=" + mRegisteredBatteryReceiver);
        mPhoneBattery = shotBatteryLevel();
        if (!mRegisteredBatteryReceiver) {
            if (mShowHandheldBattery == true) {
                //Log.i(TAG, "!!! registerBatteryReceiver");
                mRegisteredBatteryReceiver = true;
                APhoneService.this.registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }
    }
    private float shotBatteryLevel () {
        Intent batteryIntent = APhoneService.this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level<0 || scale<0) return ACommon.BATTERY_LEVEL_UNSPECIFIED;
        return ((float)level / (float)scale) * 100.0f;
    }
    //
    Object mLockCrashReport = new Object();
    //boolean mConnected = false;
    boolean mConnectivityReceiverRegistered = false;
    private void registerConnectivityReceiver() {
        synchronized (mLockCrashReport) {
            if (!mConnectivityReceiverRegistered) {
                APhoneService.this.registerReceiver(mConnectivityInfoReceiver,
                        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                );
                mConnectivityReceiverRegistered = true;
            }
            //mConnected = checkConnected();
        }
    }
    private void unregisterConnectivityReceiver() {
        synchronized (mLockCrashReport) {
            if (!mConnectivityReceiverRegistered) return;
            APhoneService.this.unregisterReceiver(mConnectivityInfoReceiver);
            mConnectivityReceiverRegistered = false;
            //mConnected = false;
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
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }
    private boolean checkCrashReportPresent() {
        // examples: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
        for (String fileName : getApplicationContext().fileList()) {
            if (fileName.matches("^[0-9]+-approved.stacktrace")) {
                return true;
            }
        }
        return false;
    }
    int mPeriodicCheckCrashInterval = 60 * 1000; // milliseconds
    Runnable taskPeriodicCheckCrashReport = new Runnable() {
        @Override
        public void run() {
            if (checkCrashReportPresent()) {
                if (checkConnected()) {
                    forceSendCrashReports();
                    unregisterConnectivityReceiver();
                } else {
                    registerConnectivityReceiver();
                }
            } else {
                unregisterConnectivityReceiver();
            }
            mServiceHandler.postDelayed(taskPeriodicCheckCrashReport, mPeriodicCheckCrashInterval);
        }
    };
    private void forceSendCrashReports() {
        for (String fileName : getApplicationContext().fileList()) {
            if (fileName.matches("^[0-9]+-IS_SILENT.stacktrace")) {
                //return;
                File file = new File(getApplicationContext().getFilesDir(), fileName);
                file.delete();
            }
        }
        ACRA.getErrorReporter().reportBuilder().forceSilent().message(ACommon.PENDING_CRASH_REPORT).send();
    }

    int mPeriodicCheckSharedFiles = 600 * 1000; // milliseconds
    Runnable taskPeriodicDeleteOldSharedFiles = new Runnable() {
        @Override
        public void run() {
            long timeStamp = System.currentTimeMillis();
            //Log.i(TAG, "#OLD timeStamp=" + timeStamp);

            for (String fileName : getApplicationContext().fileList()) {

                if (fileName.matches("^[0-9]+-approved.stacktrace") || fileName.matches("^[0-9]+-IS_SILENT.stacktrace")) {
                    File file = new File(getApplicationContext().getFilesDir(), fileName);
                    long lastMod = file.lastModified();
                    long maxDiff = 3600*24*7*1000;
                    //Log.i(TAG, "#OLD file=" + file + ", lastMod=" + lastMod + ", diff=" + (timeStamp - lastMod) / 1000);
                    if ((timeStamp - lastMod) > maxDiff) file.delete();
                }
                if (fileName.matches("^shared_[0-9]+.png")) {
                    File file = new File(getApplicationContext().getFilesDir(), fileName);
                    long lastMod = file.lastModified();
                    long maxDiff = 3600*24*1000;
                    //Log.i(TAG, "#OLD file=" + file + ", lastMod=" + lastMod + ", diff=" + (timeStamp - lastMod) / 1000);
                    if ((timeStamp - lastMod) > maxDiff) file.delete();
                }
                String template = String.format("^layout_[0-9]+.hex.%s", productId);
                if (fileName.matches(template)) {
                    File file = new File(getApplicationContext().getFilesDir(), fileName);
                    long lastMod = file.lastModified();
                    long maxDiff = 3600*24*1000;
                    //Log.i(TAG, "#OLD file=" + file + ", lastMod=" + lastMod + ", diff=" + (timeStamp - lastMod) / 1000);
                    if ((timeStamp - lastMod) > maxDiff) file.delete();
                }
            }

            mServiceHandler.postDelayed(taskPeriodicDeleteOldSharedFiles, mPeriodicCheckSharedFiles);
        }
    };

    private void disconnectWearNetworkClient () {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                // remove listeners which was added in onConnected
                Wearable.DataApi.removeListener(mGoogleApiClient, APhoneService.this);
                Wearable.NodeApi.removeListener(mGoogleApiClient, APhoneService.this);
                Wearable.MessageApi.removeListener(mGoogleApiClient, APhoneService.this);
            }
            mGoogleApiClient.disconnect();
        }
    }
    private void connectWearNetworkClient () {
        mGoogleApiClient = new GoogleApiClient.Builder(APhoneService.this)
                .addConnectionCallbacks(APhoneService.this)
                .addOnConnectionFailedListener(APhoneService.this)
                .addApi(Wearable.API)
                .build();
        if (mGoogleApiClient != null) {
            Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(getLocalNodeCallback);
            mGoogleApiClient.connect(); //see onConnected
        }
    }


    /**
     * Show a notification while this service is running.
     */
//    private void showNotification() {
//        CharSequence text = getText(R.string.watchface_service_running);
//        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
//        // Note android:launchMode="singleInstance" in activity manifest!!!
//        //Intent intent = new Intent(this, AirForceRuPhoneCompanionConfigActivity.class);
//        Intent intent = new Intent(this, HandheldCompanionConfigActivity.class);
//        //intent.setAction("com.luna_78.airforceru.CONFIG_AIRFORCERU");
//        //intent.addCategory("com.google.android.wearable.watchface.category.COMPANION_CONFIGURATION");
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        notification.setLatestEventInfo(this, "AirForceRus", text, pendingIntent);
//        mNotificationManager.notify(R.string.watchface_service_running, notification);
//    }

    //public APhoneService() {}


    @Override // Service
    public IBinder onBind(Intent intent) {
        //Log.i(TAG, "onBind");
        return mServiceBinder;
    }

    private void listLocalFiles() {
        String[] files = fileList();
        int count=0;
        for (String fileName : files) {
            count++;
            File file = getFileStreamPath(fileName);
            //Log.i(TAG, "((( listLocalFiles, file[" + count + "] = " + fileName + ", size=" + file.length());

//            if (file.matches("^[0-9]+_[AD]$")) {
//                Log.i(TAG, "((( removeDedicatedIcons, DEDICATED ICON found");
//                deleteFile(file);
//            }

            // example: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
//            if (fileName.matches("^[0-9]+-approved.stacktrace") || fileName.matches("^ACRA-INSTALLATION")) {
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                File fileOut = new File(path, fileName);
//                Log.i(TAG, "((((( listLocalFiles, file=" + path + "/" + fileName);
//                //adb -s 0a3d818c pull /sdcard/Pictures/1436193606000-approved.stacktrace
//                //adb -s 0a3d818c pull /sdcard/Pictures/ACRA-INSTALLATION
//                try {
//                    ACommon.copyFile(file, fileOut);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

        }
    } // listLocalFiles

    @Override // Service
    public void onCreate() {
        super.onCreate();

//        int pid = android.os.Process.myPid();
//        int tid = android.os.Process.myTid();
//        Log.i(TAG, "((( onCreate, PID=" + pid + ", TID=" + tid);

        String peerId = restorePeerIdFromFile();
        synchronized (mLockPeerId) {
            smPeerId = peerId;
        }
        //Log.i(TAG, "((( onCreate, restored from file peerId=" + peerId);

        mWearAppPreference = new AppPreferences();
        mWearAppPreference.load(getApplicationContext());

        //temporary test
        //listLocalFiles();

        productId = getResources().getString(R.string.product_id);

        HandlerThread thread = new HandlerThread("AirForceRuService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mPhoneBattery = shotBatteryLevel();
        //Log.i(TAG, "*** BatteryLevel: float=" + mPhoneBattery);
        registerBatteryReceiver();

        if (mGoogleApiClient!=null) {
            //Log.i(TAG, "*** mGoogleApiClient not NULL, connected=" + mGoogleApiClient.isConnected());
        }
        connectWearNetworkClient();

        //mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //showNotification();




        //RestoreLayoutsPalette();
        mServiceHandler.postAtFrontOfQueue(taskRestoreLayoutsPalette);

        int fdow = Calendar.getInstance().getFirstDayOfWeek();
        //Log.i(TAG, "((( FDOW=" + fdow);


        // initial delay: to let ACRA to send pending reports by itself - is it working???
        mServiceHandler.postDelayed(taskPeriodicCheckCrashReport, 60000);

        mServiceHandler.postDelayed(taskPeriodicDeleteOldSharedFiles, 30000);

        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
            demoPackData[i] = new DemoPackData();
        }

    }


    @Override // Service
    public void onDestroy() {
        //super.onDestroy();
        //Log.i(TAG, "onDestroy");
        unregisterBatteryReceiver();
        unregisterConnectivityReceiver();
        disconnectWearNetworkClient();
        //mNotificationManager.cancel(R.string.watchface_service_running);
        //ACommon.SaveConfigPalette(mLayoutsPalette, getString(R.string.configFileName), this);
        gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
        mServiceHandler.removeCallbacks(taskPeriodicCheckCrashReport);
        mServiceHandler.removeCallbacks(taskPeriodicDeleteOldSharedFiles);
        mServiceLooper.quitSafely();
        super.onDestroy();
    }


    @Override // Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i(TAG, "((( onStartCommand, flags=" + flags + ", intent=" + intent);
        if (null==intent) {
            //we are started by Android
            //todo: remove: ACRA.getErrorReporter().reportBuilder().forceSilent().message("SERVICE STARTED BY ANDROID").send();
        } else {
            //we are started by activity
        }
        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }



    ResultCallback<NodeApi.GetConnectedNodesResult> getConnectedNodesCallback = new ResultCallback<NodeApi.GetConnectedNodesResult>() {
        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            List<Node> nodeList = getConnectedNodesResult.getNodes();
            //int numNodes = nodeList.size();
            int count = 0;
            for (Node node : nodeList) {
                count++;
                String peerId = node.getId();
                //Log.i(TAG, "((( Remote peer id["+ count + "]: " + peerId + ", " + node.getDisplayName());
            }
        }
    };
    //
    ResultCallback<NodeApi.GetLocalNodeResult> getLocalNodeCallback = new ResultCallback<NodeApi.GetLocalNodeResult>() {
        @Override
        public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
            String peerId = getLocalNodeResult.getNode().getId();
            //Log.i(TAG, "((( Local peer id: " + peerId + ", " + getLocalNodeResult.getNode().getDisplayName());
            mLocalPeerId = peerId;
        }
    };


    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        //Log.i(TAG, "((( onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, APhoneService.this);
        Wearable.NodeApi.addListener(mGoogleApiClient, APhoneService.this);
        Wearable.MessageApi.addListener(mGoogleApiClient, APhoneService.this);


        //Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(getLocalNodeCallback);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(getConnectedNodesCallback);


        sendWakeup(3);
        requestCurrentConfig();
    }




    private void sendWakeup(long delay) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WAKEUP);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putLong(ACommon.KEY_DELAY, delay * 1000);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
        String peerId; synchronized (mLockPeerId) { peerId = smPeerId; }
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, peerId).start();
    }
    private void requestCurrentConfig() {
        String peerId; synchronized (mLockPeerId) { peerId = smPeerId; }
//        DataMap dataMap = new DataMap();
//        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CURRENT_CONFIG);
//        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
//        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start(); //, mGoogleApiClient
//        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, peerId).start();

        ACommon.requestCurrentConfig(mGoogleApiClient, peerId);
    }




    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "onConnectionSuspended");
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "onConnectionFailed");
    }





    // mServiceHandler.post(taskSavePeerId);
//    private Runnable taskSaveLayoutsPalette = new Runnable() {
//        @Override
//        public void run() {
//            boolean includeIcons;
//            Log.i(TAG, "((( taskSaveLayoutsPalette");
//            synchronized (mLockIncludeIcons) {
//                includeIcons = mIncludeIcons;
//            }
//            String fileName = getString(R.string.configFileName);
//            boolean result = gLayoutsPalette.saveToXmlFile(fileName, includeIcons);
//    //            if (true == result) {
//            broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF_UPDATE, System.currentTimeMillis());
//    //            } else {
//    //                broadcastEmptyToActivity(ACommon.EVT_SIGNAL_HOLDOFF, System.currentTimeMillis());
//    //            }
//        }
//    };
    private Runnable taskSavePeerId = new Runnable() {
        @Override
        public void run() {
            String peerId; synchronized (mLockPeerId) { peerId = "" + smPeerId; }
            savePeerIdToFile(peerId);
        }
    };


    private boolean fromPeerId(String peerId, String host) {
//        if (null != host && null != peerId && !host.equals(peerId)) { //&& null != peerId
//            Log.i(TAG, "((( *** !!! WEAR DATA IGNORED, uri.hostId[ " + host + " ] != peerId[ " + peerId + " ]");
//            dataEvents.release();
//            return;
//        }
        boolean result = (null != host && null != peerId && host.equals(peerId));
        //if (!result) Log.i(TAG, "((( *** !!! WEAR DATA IGNORED, uri.hostId[ " + host + " ] != peerId[ " + peerId + " ]");
        return result;
    }

    @Override // DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Log.i(TAG, "onDataChanged");
        //super.onDataChanged(dataEvents);
        DataMap dataMap;
        boolean needConsume, trigger;
        int intVal;
        for (DataEvent event : dataEvents) {
            needConsume = false;
            Uri uri = event.getDataItem().getUri();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            String host = uri.getHost(); // may be null

            long serialSeq = DataMapItem.fromDataItem(event.getDataItem()).getDataMap().getLong(ACommon.KEY_SERIAL_SEQUENCE);
            //Log.i(TAG, "#TOGGLE, event=" + event.getType() + ", path=" + path + ", host=" + host + ", serialSeq=" + serialSeq);
//            if (0L != serialSeq) {
//                Log.i(TAG, "#TOGGLE_LAYOUT serialSeq=" + serialSeq);
//            }

            // слектор по host (uri.getHost(), smPeerId) - часов может быть подключено несколько!!!
            if (null != host && null != mLocalPeerId && host.equals(mLocalPeerId)) {
                //Log.i(TAG, "((( *** !!! SELF DATA IGNORED, uri.hostId[ " + host + " ] == peerId[ " + mLocalPeerId + " ]");
                //Log.i(TAG, "#TOGGLE_LAYOUT SELF DATA IGNORED");
                dataEvents.release();
                return;
            }
            String peerId; synchronized (mLockPeerId) { peerId = smPeerId; }
            if (null == peerId) {
                //если сервис (пере)запущен без активности, или не через Google Android Wear proxy (при импорте, например)
                if (null != host) {
                    synchronized (mLockPeerId) { smPeerId = host; }
                    peerId = host;
                    mServiceHandler.post(taskSavePeerId);
                } else {
                    //todo: а здесь что делать?
                }
            }

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                dataMap = new DataMap();
                dataMap.putAll(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
                //Log.i(TAG, "onDataChanged: dataMap: " + dataMap + "; scheme=" + scheme + ", node=" + host + ", path=" + path);
                int evtType = dataMap.getInt(ACommon.KEY_EVENT);
                long time = dataMap.getLong(ACommon.KEY_TIME);
                Asset asset;

                // we need to accept ANY crash report, from any watch
                if (path.equals(ACommon.WEAR_CRASH_PATH)) {
                    switch (evtType) {
                        case ACommon.EVT_WEAR_CRASHREPORT:
                            byte[] crashContent = dataMap.getByteArray(ACommon.KEY_CRASHREPORT_CONTENT);
                            long crashTime = dataMap.getLong(ACommon.KEY_CRASHREPORT_TIME, 0);
                            //Log.i(TAG, "((( EVT_WEAR_CRASHREPORT, time=" + crashTime + ", len=" + crashContent.length);
                            if (0 == crashTime) crashTime = System.currentTimeMillis();
                            if (null != crashContent && crashContent.length > 0) {
                                new SaveWearCrashReport(getApplicationContext(), crashTime, crashContent).start();
                            }
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by WEAR_CRASH_PATH !!!");
                            break;
                    }
                }

                // we need to accept ANY toggle request, from any watch
                //todo: все TOGGLE тоже выеести сюда, предварительно сменив им path !!!
                if (path.equals(ACommon.WEAR_TOGGLE_PATH)) {
                    //Log.i(TAG, "#TOGGLE type=" + evtType);
                    switch (evtType) {
                        case ACommon.EVT_WEARCFG_TOGGLE_LAYOUT:
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_LAYOUT");
                            //broadcastEmptyToActivity(evtType, time);
                            //Log.i(TAG, "#TOGGLE wear data request received");
                            toggleLayout(host);
                            needConsume = true;
                            break;
//                        //sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY, mShowHandheldBattery);
//                        private void sendWearConfigBooleanOption(int event, boolean option) {
//                            DataMap dataMap = new DataMap();
//                            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                            dataMap.putInt(ACommon.KEY_EVENT, event);
//                            dataMap.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, option);
//                            new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap).start();
//                        }
                        case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
                            //todo: несколько подключенных часов !!!
                            trigger = dataMap.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_PHONE_BATTERY=" + trigger + ", mRegisteredBatteryReceiver=" + mRegisteredBatteryReceiver);
                            setShowHandheldBatteryTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_SHOW_HANDHELD_BATTERY, trigger);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_ANIMATION:
                            trigger = dataMap.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_ANIMATION=" + trigger);
                            setShowAnimationTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_SHOW_RIM_ANIMATION, trigger);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF:
                            trigger = dataMap.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF");
                            setShowHrDigitsReliefTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_SHOW_HRDIGITS_RELIEF, trigger);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_DIAL_GRADIENT:
                            trigger = dataMap.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_DIAL_GRADIENT");
                            setShowDialGradientTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_SHOW_DIAL_GRADIENT, trigger);
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by WEAR_TOGGLE_PATH !!!");
                            break;
                    }
                }

                if (!fromPeerId(peerId, host)) needConsume = true;

                if (fromPeerId(peerId, host) && path.equals(ACommon.FROM_WEAR_PATH)) {
                    //DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    //int evtType = dataMapItem.getDataMap().getInt(ACommon.KEY_EVENT);
                    //long time = dataMapItem.getDataMap().getLong(ACommon.KEY_TIME);
                    switch (evtType) {
                        case ACommon.EVT_REQUEST_CREATE_CONFIG:
                            //Log.i(TAG, "*** EVT_REQUEST_CREATE_CONFIG, datamap=" + dataMap);
                            long cfg_time = System.currentTimeMillis();
                            dataMap.putLong(ACommon.CFG_TIME, cfg_time);
                            dataMap.putLong(ACommon.KEY_TIME, cfg_time);
                            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_STORE_CONFIG);
                            new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
                            needConsume = true;
                            break;
//                        case ACommon.EVT_CURRENT_CONFIG:
//                            Log.i(TAG, "*** EVT_CURRENT_CONFIG, datamap=" + dataMap);
//                            mCurrentConfig = dataMap.toBundle();
//                            setCurrentToggles(mCurrentConfig);
//                            broadcastConfigToActivity(evtType, time, mCurrentConfig);
//                            needConsume = true;
//                            break;
                        //
                        case ACommon.EVT_WEARCFG_SET_INVERT_GRADIENT:
                            intVal = dataMap.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE);
                            setInvertGradient(intVal, TAG);
                            broadcastIntegerToActivity(evtType, time, ACommon.CFG_INVERT_GRADIENT, intVal);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_SET_AUX_BEVEL_COLOR:
                            intVal = dataMap.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
                            setAuxBevelColor(intVal, TAG);
                            broadcastIntegerToActivity(evtType, time, ACommon.CFG_AUX_BEVEL_COLOR, intVal);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_SET_RESPECT_BURNIN:
                            trigger = dataMap.getBoolean(ACommon.CFG_RESPECT_BURNIN, true);
                            //Log.i(TAG, "=== EVT_WEARCFG_SET_RESPECT_BURNIN");
                            setRespectBurnInTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_RESPECT_BURNIN, trigger);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_SET_SWEEP:
                            trigger = dataMap.getBoolean(ACommon.CFG_SWEEP_SECONDS, true);
                            //Log.i(TAG, "=== EVT_WEARCFG_SET_SWEEP");
                            setSweepTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_SWEEP_SECONDS, trigger);
                            needConsume = true;
                            break;
                        //
                        case ACommon.EVT_WEARCFG_SET_RESPECT_LOWBIT:
                            trigger = dataMap.getBoolean(ACommon.CFG_RESPECT_LOWBIT, true);
                            //Log.i(TAG, "=== EVT_WEARCFG_SET_RESPECT_LOWBIT");
                            setRespectLowBitTrigger(trigger, TAG);
                            broadcastBooleanToActivity(evtType, time, ACommon.CFG_RESPECT_LOWBIT, trigger);
                            needConsume = true;
                            break;


                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by FROM_WEAR_PATH !!!");
                            break;
                    }
                } else if (fromPeerId(peerId, host) && path.equals(ACommon.ASYNC_REPLY_PATH)) { // ASYNC_REPLY_PATH
                    switch (evtType) {
                        case ACommon.EVT_DENSE_SCREENSHOT:
                            //Asset asset = dataMapItem.getDataMap().getAsset(ACommon.KEY_SCREENSHOT);
                            asset = dataMap.getAsset(ACommon.KEY_SCREENSHOT);
                            //lastDenseScreenshot = loadBitmapFromAsset(asset);
                            setLastDenseScreenshot(loadBitmapFromAsset(asset));
                            //Log.i(TAG, "### DENSE_SCREENSHOT = " + getLastDenseScreenshot());
                            //Log.i(TAG, "((( DENSE_SCREENSHOT; scheme=" + scheme + ", node=" + host + ", path=" + path);
                            broadcastEmptyToActivity(evtType, time);
                            needConsume = true;
                            break;
                        case ACommon.EVT_AMBIENT_SCREENSHOT:
                            asset = dataMap.getAsset(ACommon.KEY_SCREENSHOT);
                            //lastAmbientScreenshot = loadBitmapFromAsset(asset);
                            setLastAmbientScreenshot(loadBitmapFromAsset(asset));
                            //Log.i(TAG, "### AMBIENT_SCREENSHOT = " + getLastAmbientScreenshot());
                            broadcastEmptyToActivity(evtType, time);
                            needConsume = true;
                            break;
                        case ACommon.EVT_CURRENT_CONFIG_FOR_FILE:
                            //Log.i(TAG, "### EVT_CURRENT_CONFIG_FOR_FILE = " + dataMap);
                            Bundle config;
                            config = dataMap.toBundle();
                            broadcastConfigToActivity(evtType, time, config);
                            needConsume = true;
                            break;
                        case ACommon.EVT_CURRENT_CONFIG:
                            //Log.i(TAG, "((( EVT_CURRENT_CONFIG, datamap=" + dataMap);
                            mCurrentConfig = dataMap.toBundle();
                            setCurrentToggles(mCurrentConfig);
                            broadcastConfigToActivity(evtType, time, mCurrentConfig);
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by ASYNC_REPLY_PATH !!!");
                            break;
                    }
                } else if (fromPeerId(peerId, host) && path.equals(ACommon.ASYNC_REPLY_PATH_2)) { // ASYNC_REPLY_PATH_2
                    switch (evtType) {
                        case ACommon.EVT_CURRENT_WATCHFACE_VALUES:
                            //Log.i(TAG, "((( EVT_CURRENT_WATCHFACE_VALUES, datamap=" + dataMap);
                            //
                            wfvBurninMargin = dataMap.getFloat(ACommon.WFVALUE_BURNIN_MARGIN);
                            wfvScreenWidth = dataMap.getInt(ACommon.WFVALUE_SCREEN_WIDTH);
                            wfvScreenHeight = dataMap.getInt(ACommon.WFVALUE_SCREEN_HEIGHT);
                            wfvScreenCenterX = dataMap.getFloat(ACommon.WFVALUE_SCREEN_CENTERX);
                            wfvScreenCenterY = dataMap.getFloat(ACommon.WFVALUE_SCREEN_CENTERY);
                            wfvScreenRadius = dataMap.getFloat(ACommon.WFVALUE_SCREEN_RADIUS);
                            wfvDialRadius = dataMap.getFloat(ACommon.WFVALUE_DIAL_RADIUS);
                            wfvIsWearRTL = dataMap.getBoolean(ACommon.WFVALUE_RTL);
                            //
                            wfvAuxAcx = dataMap.getFloat(ACommon.WFVALUE_AUXA_CX);
                            wfvAuxAcy = dataMap.getFloat(ACommon.WFVALUE_AUXA_CY);
                            wfvAuxAdim = dataMap.getFloat(ACommon.WFVALUE_AUXA_DIM);
                            wfvAuxBcx = dataMap.getFloat(ACommon.WFVALUE_AUXB_CX);
                            wfvAuxBcy = dataMap.getFloat(ACommon.WFVALUE_AUXB_CY);
                            wfvAuxBdim = dataMap.getFloat(ACommon.WFVALUE_AUXB_DIM);
                            wfvAuxCcx = dataMap.getFloat(ACommon.WFVALUE_AUXC_CX);
                            wfvAuxCcy = dataMap.getFloat(ACommon.WFVALUE_AUXC_CY);
                            wfvAuxCdim = dataMap.getFloat(ACommon.WFVALUE_AUXC_DIM);
                            //
                            //Log.i(TAG, "((( rtl=" + Inscription.isRTL());
                            broadcastEmptyToActivity(evtType, time);
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by ASYNC_REPLY_PATH_2 !!!");
                            break;
                    }

                } else if (fromPeerId(peerId, host) && path.equals(ACommon.ASYNC_REPLY_PATH_4)) { // ASYNC_REPLY_PATH_4
                    switch (evtType) {
                        case ACommon.EVT_CURRENT_TZ_ARRAY:
                            String[] tzName = dataMap.getStringArray(AppPreferences.KEY_TZARR_NAME);
                            long[] tzOffs = dataMap.getLongArray(AppPreferences.KEY_TZARR_OFFS);
                            long[] tzDst = dataMap.getLongArray(AppPreferences.KEY_TZARR_DST);
                            int numTz = tzName.length;
                            //Log.i(TAG, "#TZ SIZE = " + numTz);
                            //
                            mTzList = new AppPreferences.TzList[numTz];
                            for (int i=0; i < numTz; i++) {
                                mTzList[i] = new AppPreferences.TzList();
                                mTzList[i].tzName = tzName[i];
                                mTzList[i].tzOffs = (int) tzOffs[i];
                                mTzList[i].tzDst = (tzDst[i]==1) ? true : false;
                                //Log.i(TAG, "#TZ: " + mTzList[i].tzName + " " + mTzList[i].tzOffs + " " + mTzList[i].tzDst);
                            }
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by ASYNC_REPLY_PATH_3 !!!");
                            break;
                    }
                } else if (fromPeerId(peerId, host) && path.equals(ACommon.ASYNC_REPLY_PATH_3)) { // ASYNC_REPLY_PATH_3
                    switch (evtType) {
                        case ACommon.EVT_PLATE_BITMAP:
                            //Asset asset = dataMapItem.getDataMap().getAsset(ACommon.KEY_SCREENSHOT);
                            asset = dataMap.getAsset(ACommon.KEY_SCREENSHOT);
                            //lastDenseScreenshot = loadBitmapFromAsset(asset);
                            setPlateBitmap(loadBitmapFromAsset(asset));
                            //Log.i(TAG, "((( EVT_PLATE_BITMAP = " + getPlateBitmap());
                            broadcastEmptyToActivity(evtType, time);
                            needConsume = true;
                            break;

                        case ACommon.EVT_CURRENT_PREFERENCES:
                            //Log.i(TAG, "((( EVT_CURRENT_PREFERENCES, datamap=" + dataMap);
                            mWearAppPreference = AppPreferences.unBundlePreferences(dataMap.toBundle());
                            mWearAppPreference.save(getApplicationContext());
                            //
//                            String[] tzName = dataMap.getStringArray(AppPreferences.KEY_TZARR_NAME);
//                            long[] tzOffs = dataMap.getLongArray(AppPreferences.KEY_TZARR_OFFS);
//                            long[] tzDst = dataMap.getLongArray(AppPreferences.KEY_TZARR_DST);
//                            int numTz = tzName.length;
//                            //Log.i(TAG, "#TZ SIZE = " + numTz);
//                            //
//                            mTzList = new AppPreferences.TzList[numTz];
//                            for (int i=0; i < numTz; i++) {
//                                mTzList[i] = new AppPreferences.TzList();
//                                mTzList[i].tzName = tzName[i];
//                                mTzList[i].tzOffs = (int) tzOffs[i];
//                                mTzList[i].tzDst = (tzDst[i]==1) ? true : false;
//                                //Log.i(TAG, "#TZ: " + mTzList[i].tzName + " " + mTzList[i].tzOffs + " " + mTzList[i].tzDst);
//                            }
                            //
                            setRespectBurnInTrigger(mWearAppPreference.getRespectBurnIn(), TAG);
                            //broadcastBooleanToActivity(evtType, time, ACommon.CFG_RESPECT_BURNIN, mWearAppPreference.getRespectBurnIn());
                            setSweepTrigger(mWearAppPreference.getSweepSeconds(), TAG);
                            //broadcastBooleanToActivity(evtType, time, ACommon.CFG_SWEEP_SECONDS, mWearAppPreference.getSweepSeconds());
                            setShowHandheldBatteryTrigger(mWearAppPreference.getShowHandheldBattery(), TAG);
                            //
                            broadcastEmptyToActivity(evtType, time);
                            needConsume = true;
                            break;

                        default:
                            needConsume = true;
                            //Log.i(TAG, "### UNCATCHED EVENT by ASYNC_REPLY_PATH_3 !!!");
                            break;
                    }
                } else {
                    //Log.i(TAG, "### UNKNOWN PATH !!!, path=" + path);
                }

                // consume dataItem
                if (needConsume == true) Wearable.DataApi.deleteDataItems(mGoogleApiClient, event.getDataItem().getUri());

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                //Log.i(TAG, "onDataChanged: deleted URI: " + uri);
            }
        }
        dataEvents.release();
    } // onDataChanged


    private static class SaveWearCrashReport extends Thread {

        long mCrashTime;
        byte[] mCrashContent;
        Context mContext;

        public SaveWearCrashReport(Context context, long crashTime, byte[] reportContent) {
            mCrashTime = crashTime;
            mCrashContent = reportContent;
            mContext = context;
        }

        public void run() {
            //extract content of wear crash report and put it in "stacktrace" file for ACRA later sending
            // example ACRA name: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis;
            int len = 0;

            try {
                zis = new GZIPInputStream(new ByteArrayInputStream(mCrashContent));
                while ((len = zis.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                zis.close();

                ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.toByteArray());
                String fileName = String.valueOf(mCrashTime) + "-approved.stacktrace";
                File file = new File(mContext.getFilesDir(), fileName);
                ACommon.copyFile(bis, file);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // class SaveWearCrashReport


//    public void sendFullConfigToSetOld(Bundle config) {
//        boolean trigger;
//        //
//        config.remove(ACommon.KEY_CFGPAL_NAME);
//        config.remove(ACommon.KEY_CFGPAL_ICON);
//        config.remove(ACommon.KEY_CFGPAL_ICON_AMBIENT);
//        //
//        DataMap dataMap = DataMap.fromBundle(config);
//        DataMap dataMapToSend = dataMap.getDataMap(ACommon.KEY_CFGPAL_CONFIG);
//        //
//        trigger = getShowAnimationTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, trigger);
//        trigger = getShowHandheldBatteryTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, trigger);
//        trigger = getShowHrDigitsReliefTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, trigger);
//        trigger = getShowDialGradientTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, trigger);
//        //
//        dataMapToSend.putInt(ACommon.KEY_EVENT, ACommon.EVT_SET_FULL_CONFIG);
//        dataMapToSend.putLong(ACommon.KEY_TIME, new Date().getTime());
//        new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMapToSend).start();
//        //
//        setLastDenseScreenshot(null);
//        setLastAmbientScreenshot(null);
//        setPlateBitmap(null);
//        //
//        broadcastEmptyToActivity(ACommon.EVT_NEW_CONFIG_SENT, System.currentTimeMillis());
//    }
    //
    public void sendFullConfigToSet(Bundle config) {
        String peerId; synchronized (mLockPeerId) { peerId = smPeerId; }
        sendFullConfigToSet(peerId, config);
    }
    //
    public void sendFullConfigToSet(String peerId, Bundle config) {
        boolean trigger;
        //
//        config.remove(ACommon.KEY_CFGPAL_NAME);
//        config.remove(ACommon.KEY_CFGPAL_ICON);
//        config.remove(ACommon.KEY_CFGPAL_ICON_AMBIENT);
        //
//        DataMap dataMap = DataMap.fromBundle(config);
//        DataMap dataMapToSend = dataMap.getDataMap(ACommon.KEY_CFGPAL_CONFIG);
        //
        DataMap dataMapToSend = DataMap.fromBundle(config);
        //
        trigger = getShowAnimationTrigger();
        dataMapToSend.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, trigger);
        trigger = getShowHandheldBatteryTrigger();
        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, trigger);
        trigger = getShowHrDigitsReliefTrigger();
        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, trigger);
        trigger = getShowDialGradientTrigger();
        dataMapToSend.putBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, trigger);
        //
        dataMapToSend.putInt(ACommon.KEY_EVENT, ACommon.EVT_SET_FULL_CONFIG);
        dataMapToSend.putLong(ACommon.KEY_TIME, new Date().getTime());
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMapToSend).start();
        //String peerId; synchronized (mLockPeerId) { peerId = smPeerId; }
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMapToSend, mGoogleApiClient, peerId).start();
        //
        setLastDenseScreenshot(null);
        setLastAmbientScreenshot(null);
        setPlateBitmap(null);
        System.gc();
        //
        broadcastEmptyToActivity(ACommon.EVT_NEW_CONFIG_SENT, System.currentTimeMillis());
    }
    //
    private void toggleLayout(String peerId) {
        int position;
        int numConfigs = gLayoutsPalette.size();
        if (numConfigs == 0) return;
        if (null == gLayoutsPaletteIndex) {
            position = 0;
        } else {
            position = gLayoutsPaletteIndex;
            if (++position >= numConfigs) {
                position = 0;
            }
        }
        gLayoutsPaletteIndex = position;
//        Bundle configPaletteElement = ACommon.deserializeBundle(mLayoutsPalette.get(position));
//        sendFullConfigToSet(configPaletteElement);
        Layout element = gLayoutsPalette.get(position);
        sendFullConfigToSet(peerId, element.config);
    }


    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        /*ConnectionResult result =
                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }*/
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();
        //mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            //Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    } // loadBitmapFromAsset


    private void broadcastEmptyToActivity(int event, long time) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AirForceRuWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.KEY_VALUE, 0);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastConfigToActivity(int event, long time, Bundle config) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AirForceRuWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        //intent.putExtra(ACommon.KEY_VALUE, config);
        intent.putExtras(config);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastBooleanToActivity(int event, long time, String key, boolean value) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastIntegerToActivity(int event, long time, String key, int value) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override // NodeApi.NodeListener
    public void onPeerConnected(Node peer) {
        //Log.i(TAG, "((( onPeerConnected: getId()=" + peer.getId() + ", getDisplayName()=" + peer.getDisplayName());
    }

    @Override // NodeApi.NodeListener
    public void onPeerDisconnected(Node peer) {
        //Log.i(TAG, "((( onPeerDisconnected: getId()=" + peer.getId() + ", getDisplayName()=" + peer.getDisplayName());
        //todo: broadcst to activity
    }

    @Override // MessageApi.MessageListener
    public void onMessageReceived(MessageEvent messageEvent) {
        //Log.i(TAG, "((( onMessageReceived");
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        //int dropped = 0;
        switch (level) {
            case TRIM_MEMORY_COMPLETE:
                //dropped = gLayoutsPalette.dropIcons();
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_COMPLETE"); //, dropped = " + dropped
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                //dropped = gLayoutsPalette.dropIcons();
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_RUNNING_CRITICAL"); //, dropped = " + dropped
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                //dropped = gLayoutsPalette.dropIcons();
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_RUNNING_LOW"); //, dropped = " + dropped
                break;
            case TRIM_MEMORY_BACKGROUND:
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_BACKGROUND");
                break;
            case TRIM_MEMORY_MODERATE:
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_MODERATE");
                break;
            case TRIM_MEMORY_UI_HIDDEN:
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_UI_HIDDEN");
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:
                //Log.i(TAG, "((( onTrimMemory, TRIM_MEMORY_RUNNING_MODERATE");
                break;
        }
    } // onTrimMemory

} // class APhoneService
