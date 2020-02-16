package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.TimeZone;

/**
 * Created by buba on 24/08/15.
 */
public class AppPreferences {

    public static final int NUM_ALT_TZS = 9;

    public static final int TZ_SOURCE_DEVICE = 0;
    public static final int TZ_SOURCE_UTC = 1;
    public static final int DEFAULT_SOURCE = TZ_SOURCE_DEVICE;
    public static final String KEY_SOURCE = "tzsrc";

    public static final int TZ_HEMISPHERE_UPPER = 0;
    public static final int TZ_HEMISPHERE_LOWER = 1;
    public static final int DEFAULT_HEMISPHERE = TZ_HEMISPHERE_UPPER;
    public static final String KEY_HEMISPHERE = "tzhms";

    public static final String DEFAULT_DEVICE_LABEL = "HHD";
    public static final String KEY_DEVICE_LABEL = "tzdevl";

    public static final String DEFAULT_UTC_LABEL = "UTC";
    public static final String KEY_UTC_LABEL = "tzutcl";

    public static final String DEFAULT_ALT_SSOURCE = "UTC";
    public static final String KEY_ALT_SSOURCE = "tzasrcs_";

    public static final String DEFAULT_ALT_LABEL = "UTC";
    public static final String KEY_ALT_LABEL = "tzalbl_";

    public static final boolean DEFAULT_ALT_ACTIVE = false;
    public static final String KEY_ALT_ACTIVE = "tzaact_";

    public static final String KEY_TZARR_NAME = "tzarrnm";
    public static final String KEY_TZARR_OFFS = "tzarroff";
    public static final String KEY_TZARR_DST = "tzarrdst";

    public static final String KEY_RESPECT_BURN_IN = "respburnin";
    public static final String KEY_SWEEP_SECONDS = "sweepsec";
    public static final String KEY_SHOW_PHONE_BATTERY = "kshphbat";

    //public static final String KEY_PREFERENCES_PACK = "wprefpack";


    static final String TAG = "PRF";


    //String mLocalFileName;

    public class AltTz {
        public String systemSource;
        public String label;
        public boolean isActive;
        public TimeZone tz;

        public AltTz() {
            systemSource = DEFAULT_ALT_SSOURCE;
            label = DEFAULT_ALT_LABEL;
            isActive = DEFAULT_ALT_ACTIVE;
            tz = null;
        }
    }


    // next methods called on watches from class WatchTime
    public void wtInflateTzArray() { //TimeZone tzWatchSource
        //altTzs[0].tz = tzWatchSource;
        for (int i=1; i<NUM_ALT_TZS+1; i++) {
            if (!altTzs[i].isActive) continue;
            altTzs[i].tz = TimeZone.getTimeZone(altTzs[i].systemSource);
        }
    }
    public void wtSetWatchSource(TimeZone tzWatchSource) {
        altTzs[0].tz = tzWatchSource;
        altTzs[0].isActive = true;
        altTzs[0].systemSource = altTzs[0].tz.getID();
        if (getTzSourceForWatch() == TZ_SOURCE_DEVICE) {
            altTzs[0].label = tzLabelForDevice;
        } else {
            altTzs[0].label = tzLabelForUtc;
        }
    }
    public TimeZone wtGetTz(int index) {
        return altTzs[index].tz;
    }
    public String wtGetLabel(int index) {
        return altTzs[index].label;
    }
    public int wtGetNextTz(int index) {
        for (int i=index+1; i<NUM_ALT_TZS+1; i++) {
            if (altTzs[i].isActive) {
                return i;
            }
        }
        return 0;
    }



    int             tzSourceForWatch; // UTC(=1) or device TZ(=0)
    int             tzShowInHemisphere; // upper(=0) or lower(=1)
    String          tzLabelForDevice; // LCL ? DEV ? HHD ? ***
    String          tzLabelForUtc; // UTC
    public AltTz[]  altTzs;
    boolean         mRespectBurnIn;
    boolean         mIsSweep;
    boolean         mShowHandheldBattery;



    public void setRespectBurnIn(Context context, boolean respectBurnIn) {
        String preferenceFileName = buildLocalFileName(context);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = preferences.edit();

        mRespectBurnIn = respectBurnIn;
        prefEditor.putBoolean(KEY_RESPECT_BURN_IN, mRespectBurnIn);

        prefEditor.commit();
    }
    public boolean getRespectBurnIn() { return mRespectBurnIn; }

    public void setSweepSeconds(Context context, boolean sweep) {
        String preferenceFileName = buildLocalFileName(context);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = preferences.edit();

        mIsSweep = sweep;
        prefEditor.putBoolean(KEY_SWEEP_SECONDS, mIsSweep);

        prefEditor.commit();
    }
    public boolean getSweepSeconds() { return mIsSweep; }

    public void setShowHandheldBattery(Context context, boolean showBatt) {
        String preferenceFileName = buildLocalFileName(context);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = preferences.edit();

        mShowHandheldBattery = showBatt;
        prefEditor.putBoolean(KEY_SHOW_PHONE_BATTERY, mShowHandheldBattery);

        prefEditor.commit();
    }
    public boolean getShowHandheldBattery() { return mShowHandheldBattery; }



    public int getTzHemisphere() { return tzShowInHemisphere; }
    public void setTzHemisphere(int hemi) { tzShowInHemisphere = hemi; }
    public void toggleTzHemisphere() {
        if (AppPreferences.TZ_HEMISPHERE_LOWER == tzShowInHemisphere) {
            tzShowInHemisphere = AppPreferences.TZ_HEMISPHERE_UPPER;
        } else if (AppPreferences.TZ_HEMISPHERE_UPPER == tzShowInHemisphere) {
            tzShowInHemisphere = AppPreferences.TZ_HEMISPHERE_LOWER;
        }
    }
    public int getTzSourceForWatch() { return tzSourceForWatch; }
    public void setTzSourceForWatch(int src) { tzSourceForWatch = src; }
    public void toggleTzSourceForWatch() { tzSourceForWatch = (TZ_SOURCE_DEVICE==tzSourceForWatch ? TZ_SOURCE_UTC : TZ_SOURCE_DEVICE); }
    public String getTzLabelForDevice() { return tzLabelForDevice; }
    public void setTzLabelForDevice(String newLabel) { tzLabelForDevice = String.format("%.3s", newLabel); }
    public String getTzLabelForUtc() { return tzLabelForUtc; }
    public void setTzLabelForUtc(String newLabel) { tzLabelForUtc = String.format("%.3s", newLabel); }
    public AltTz[] getAltTzs() { return altTzs; }
    public void setAltTzLabel(int index, String newLabel) {
        altTzs[index+1].label = String.format("%.3s", newLabel);
    }
    public void setAltTzSystemSource(int index, String newSource) {
        altTzs[index+1].systemSource = newSource;
    }
    public void setAltTzActive(int index, boolean active) {
        altTzs[index+1].isActive = active;
        //Log.i(TAG, "active[" + index + "] = " + active);
    }
    public boolean isSourceUtc() {
        return tzSourceForWatch == TZ_SOURCE_UTC;
    }


    private void initMembersToDefaults() {
        //Log.i(TAG, "initMembersToDefaults");
        tzSourceForWatch = DEFAULT_SOURCE;
        tzShowInHemisphere = DEFAULT_HEMISPHERE;
        tzLabelForDevice = DEFAULT_DEVICE_LABEL;
        tzLabelForUtc = DEFAULT_UTC_LABEL;
        altTzs = new AltTz[NUM_ALT_TZS+1];
        for (int i=0; i<NUM_ALT_TZS+1; i++) {
            altTzs[i] = new AltTz();
        }
        mRespectBurnIn = true;
        mIsSweep = false;
        mShowHandheldBattery = false;
//        for (String tz: TimeZone.getAvailableIDs()) {
//            Log.i(TAG, "TZ = " + tz);
//        }
    }

    private String buildLocalFileName(Context context) {
        return context.getPackageName() + "." + context.getResources().getString(R.string.default_shared_preferences_name);
    }

    public AppPreferences() {
        //Log.i(TAG, "constructor empty");
        //init(context);
        initMembersToDefaults();
    }

    public AppPreferences(Context context, boolean initial) {
        //Log.i(TAG, "constructor with context, initial=" + initial);
        initMembersToDefaults();
        if (initial) {
            save(context);
        } else {
            load(context);
        }
    }

    public void save(Context context) {
        String preferenceFileName = buildLocalFileName(context);
        //Log.i(TAG, "save(), fileName = " + preferenceFileName);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putInt(KEY_SOURCE, tzSourceForWatch);
        prefEditor.putInt(KEY_HEMISPHERE, tzShowInHemisphere);
        prefEditor.putString(KEY_DEVICE_LABEL, tzLabelForDevice);
        prefEditor.putString(KEY_UTC_LABEL, tzLabelForUtc);
        String key, index;
        for (int i=0; i<NUM_ALT_TZS+1; i++) {
            index = String.valueOf(i);
            key = KEY_ALT_SSOURCE + index;
            prefEditor.putString(key, altTzs[i].systemSource);
            key = KEY_ALT_LABEL + index;
            prefEditor.putString(key, altTzs[i].label);
            key = KEY_ALT_ACTIVE + index;
            prefEditor.putBoolean(key, altTzs[i].isActive);
        }
        prefEditor.putBoolean(KEY_RESPECT_BURN_IN, mRespectBurnIn);
        prefEditor.putBoolean(KEY_SWEEP_SECONDS, mIsSweep);
        prefEditor.putBoolean(KEY_SHOW_PHONE_BATTERY, mShowHandheldBattery);

        prefEditor.commit();
    }

    public void load(Context context) {
        String preferenceFileName = buildLocalFileName(context);
        //Log.i(TAG, "load(), fileName = " + preferenceFileName);
//        String[] files = context.fileList();
//        for (String file : files) {
//            Log.i(TAG, "file = " + file);
//        }

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        if (!preferences.contains(KEY_SOURCE)) {
            initMembersToDefaults();
            save(context);
            return;
        }

        tzSourceForWatch = preferences.getInt(KEY_SOURCE, DEFAULT_SOURCE);
        tzShowInHemisphere = preferences.getInt(KEY_HEMISPHERE, DEFAULT_HEMISPHERE);
        tzLabelForDevice = preferences.getString(KEY_DEVICE_LABEL, DEFAULT_DEVICE_LABEL);
        tzLabelForUtc = preferences.getString(KEY_UTC_LABEL, DEFAULT_UTC_LABEL);
        String key, index;
        for (int i=0; i<NUM_ALT_TZS+1; i++) {
            index = String.valueOf(i);
            key = KEY_ALT_SSOURCE + index;
            altTzs[i].systemSource = preferences.getString(key, DEFAULT_ALT_SSOURCE);
            key = KEY_ALT_LABEL + index;
            altTzs[i].label = preferences.getString(key, DEFAULT_ALT_LABEL);
            key = KEY_ALT_ACTIVE + index;
            altTzs[i].isActive = preferences.getBoolean(key, DEFAULT_ALT_ACTIVE);
        }
        mRespectBurnIn = preferences.getBoolean(KEY_RESPECT_BURN_IN, false);
        mIsSweep = preferences.getBoolean(KEY_SWEEP_SECONDS, false);
        mShowHandheldBattery = preferences.getBoolean(KEY_SHOW_PHONE_BATTERY, false);

    }

    public static Bundle bundlePreferences(AppPreferences appPreferences) {
        Bundle result = new Bundle();

        result.putInt(KEY_SOURCE, appPreferences.tzSourceForWatch);
        result.putInt(KEY_HEMISPHERE, appPreferences.tzShowInHemisphere);
        result.putString(KEY_DEVICE_LABEL, appPreferences.tzLabelForDevice);
        result.putString(KEY_UTC_LABEL, appPreferences.tzLabelForUtc);
        String key, index;
        for (int i=0; i<NUM_ALT_TZS+1; i++) {
            index = String.valueOf(i);
            key = KEY_ALT_SSOURCE + index;
            result.putString(key, appPreferences.altTzs[i].systemSource);
            key = KEY_ALT_LABEL + index;
            result.putString(key, appPreferences.altTzs[i].label);
            key = KEY_ALT_ACTIVE + index;
            result.putBoolean(key, appPreferences.altTzs[i].isActive);
        }
        result.putBoolean(KEY_RESPECT_BURN_IN, appPreferences.mRespectBurnIn);
        result.putBoolean(KEY_SWEEP_SECONDS, appPreferences.mIsSweep);
        result.putBoolean(KEY_SHOW_PHONE_BATTERY, appPreferences.mShowHandheldBattery);

        return result;
    }

    public static AppPreferences unBundlePreferences(Bundle pack) {
        AppPreferences appPreferences = new AppPreferences();

        appPreferences.tzSourceForWatch = pack.getInt(KEY_SOURCE, DEFAULT_SOURCE);
        appPreferences.tzShowInHemisphere = pack.getInt(KEY_HEMISPHERE, DEFAULT_HEMISPHERE);
        appPreferences.tzLabelForDevice = pack.getString(KEY_DEVICE_LABEL, DEFAULT_DEVICE_LABEL);
        appPreferences.tzLabelForUtc = pack.getString(KEY_UTC_LABEL, DEFAULT_UTC_LABEL);
        String key, index;
        for (int i=0; i<NUM_ALT_TZS+1; i++) {
            index = String.valueOf(i);
            key = KEY_ALT_SSOURCE + index;
            appPreferences.altTzs[i].systemSource = pack.getString(key, DEFAULT_ALT_SSOURCE);
            key = KEY_ALT_LABEL + index;
            appPreferences.altTzs[i].label = pack.getString(key, DEFAULT_ALT_LABEL);
            key = KEY_ALT_ACTIVE + index;
            appPreferences.altTzs[i].isActive = pack.getBoolean(key, DEFAULT_ALT_ACTIVE);
        }
        appPreferences.mRespectBurnIn = pack.getBoolean(KEY_RESPECT_BURN_IN, false);
        appPreferences.mIsSweep = pack.getBoolean(KEY_SWEEP_SECONDS, false);
        appPreferences.mShowHandheldBattery = pack.getBoolean(KEY_SHOW_PHONE_BATTERY, false);

        return appPreferences;
    }


    public static class TzList {
        public String      tzName;
        public int         tzOffs;
        public boolean     tzDst;

        //public TzList() {}
    }

    public TzList[] getTzArray() {
        String[] tzName = TimeZone.getAvailableIDs();
        int numTz = tzName.length;

        TzList[] list = new TzList[numTz];
        TimeZone timeZone;
        for (int i=0; i < numTz; i++) {
            timeZone = TimeZone.getTimeZone(tzName[i]);
            list[i] = new TzList();
            list[i].tzName = tzName[i];
            list[i].tzOffs = timeZone.getRawOffset();
            list[i].tzDst = timeZone.useDaylightTime();
        }

        return list;
    }

} //class AppPreferences
