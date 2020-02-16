package com.luna_78.wear.watch.face.raf3078;

//import android.app.ActionBar;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.Inscription;
import com.luna_78.wear.watch.face.raf3078.common.Layout;
import com.luna_78.wear.watch.face.raf3078.common.WatchAppearance;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

//import com.astuetz.PagerSlidingTabStrip;


public class HandheldCompanionConfigActivity extends /*ActionBarActivity*/ FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        DataApi.DataListener
{
    private static final String TAG = "CCA";

    public String mPeerId;

    private GoogleApiClient mGoogleApiClient;
    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient; }

//    private int mSettingsWakeDelay = 5;
//    public void setSettingsWakeDelay(int delay) { mSettingsWakeDelay = delay; }
//    public int getSettingsWakeDelay() { return mSettingsWakeDelay; }

    public APhoneService mService;
    public APhoneService getBoundService() { return mService; }
    boolean mServiceBound = false;
    boolean isServiceBound() { return mServiceBound; }


    public String productId;
    
    
    
    
    
//    public Integer mColors[];
//    public String mElementName[];
//    public int mElementIndex = 0;
//    public boolean isInAmbientRange() {
//        if (mElementIndex >= ACommon.CFG_AMBIENT_RANGE1_BEGIN
//                && mElementIndex <= ACommon.CFG_AMBIENT_RANGE1_END) return true;
//        return false;
//    }








    class IndexedColor {
        
        //private Integer mColors[];
        //private int mElementIndex;

        IndexedColor() {
            //resetElementIndex();
            mCurrent = 0;
            mTotalLoaded = 0;
        }

        public void nextElement() {
//            mElementIndex++;
//            if (mElementIndex >= mColors.length) mElementIndex = 0;

            mCurrent++;
            if (mCurrent >= mTotalLoaded) mCurrent = 0;
        }
        public void previousElement() {
//            mElementIndex--;
//            if (mElementIndex < 0) mElementIndex = mColors.length - 1;

            mCurrent--;
            if (mCurrent < 0) mCurrent = mTotalLoaded - 1;
        }
//        public void resetElementIndex() {
//            mElementIndex = 0;
//        }

        public int getCurrentIndex() {
            //return mElementIndex;
            return mCurrent;
        }

        public int getIndexToSend() {
            //return mElementIndex;
            return mData[mCurrent].mIndexToSend;
        }

        public String getKeyToSend() {
            return mData[mCurrent].mKey;
        }

        public boolean isInAmbientRange() {
//            if (mElementIndex >= ACommon.CFG_AMBIENT_RANGE1_BEGIN
//                    && mElementIndex <= ACommon.CFG_AMBIENT_RANGE1_END) return true;
            if (isColorsLoaded()) {
                int send = mData[mCurrent].mIndexToSend;
                if (send >= ACommon.CFG_AMBIENT_RANGE1_BEGIN
                        && send <= ACommon.CFG_AMBIENT_RANGE1_END) return true;
                if (send >= ACommon.CFG_AMBIENT_RANGE2_BEGIN
                        && send <= ACommon.CFG_AMBIENT_RANGE2_END) return true;
            }
            return false;
        } // isInAmbientRange

        String getCurrentDescription() {
            String s = "";
//            if (null != mColors) {
//                s = mElementName[mElementIndex] + "  [" + (mElementIndex + 1) + " из " + mColors.length + "]";
//            }
            if (isColorsLoaded()) s = mData[mCurrent].mDescription;
            return s;
        } // getCurrentDescription

        public int getCurrentColor() {
            int retv = 0;
            //if (null != mColors) retv = mColors[mElementIndex];
            if (isColorsLoaded()) retv = mData[mCurrent].mColor;
            return retv;
        }

        public void setCurrentColor(int color) {
            //mColors[mElementIndex] = color;

            if ((isColorsLoaded())) mData[mCurrent].mColor = color;
        }

        boolean isColorsLoaded() {
            //return mColors != null;

            return (mTotalLoaded > 0);
        }

        public void loadColors(Bundle config) {

            loadData(config);

        } // loadColors



        private String mElementName[];

        class DataRow {
            int mIndexToSend;
            String mDescription;
            String mKey;
            int mColor;

            DataRow(int index, String description, String key, int value) {
                mIndexToSend = index;
                mDescription = description;
                mKey = key;
                mColor = value;
            }
        } // class DataRow

        private DataRow[] mData = new DataRow[ACommon.NUM_CFG_COLORS * 2];
        private int mTotalLoaded = 0;
        private int mCurrent = 0;

        public void loadData(Bundle config) {
            int value, ind, send, hh, hha;
            //String descr;

            mCurrent = 0;
            mTotalLoaded = 0;

//            mColors[3] = config.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827);
            send = 3;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_BACKGROUND, value);

//            mColors[0] = config.getInt(ACommon.CFG_COLOR_MAIN_DIGITS, 0xffffffff);
            send = 0;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_DIGITS, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_DIGITS, value);

            send = ACommon.CFG_MAIN_HOURMARK_OUTLINE_COLOR; // 29
            value = config.getInt(ACommon.CFG_COLOR_MAIN_HOURMARK_OUTLINE, WatchAppearance.DEFAULT_HOURMARK_OUTLINE_COLOR);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_HOURMARK_OUTLINE, value);

//            mColors[1] = config.getInt(ACommon.CFG_COLOR_MAIN_HOURHAND, 0xffffffff);
            send = 1;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_HOURHAND, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_HOURHAND, value);

//            mColors[2] = config.getInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, 0xffffffff);
            send = 2;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_MINUTEHAND, value);

//            mColors[4] = config.getInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, 0xfff0f0d2);
            send = 4;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, 0xfff0f0d2);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_MAINHANDS, value);

//            public static final String CFG_COLOR_MAIN_DECORUPPER = String.valueOf(CFG_MAIN_DECORUPPER_COLOR); 28
            send = 28;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_DECORUPPER,
                    config.getInt(ACommon.CFG_COLOR_MAIN_HOURHAND, 0xffffffff));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_DECORUPPER, value);

//            mColors[5] = config.getInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, 0xffc6514b);
            send = 5;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, 0xffc6514b);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_SECONDSHAND, value);

//            mColors[6] = config.getInt(ACommon.CFG_COLOR_MAIN_DOMBACK, 0xffe46f69);
            send = 6;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_DOMBACK, 0xffe46f69);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_DOMBACK, value);

//            mColors[7] = config.getInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, 0xdcf0f0f0);
            send = 7;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, 0xdcf0f0f0);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_DOMFRONT, value);

            send = ACommon.CFG_MAIN_DOM_FRAME_COLOR; //30;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_DOM_FRAME, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_DOM_FRAME, value);

//            mColors[8] = config.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff);
            send = 8;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TICK, value);

//            mColors[9] = config.getInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, 0xc8ffffff);
            send = 9;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, 0xc8ffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TICKDIGIT, value);

//            mColors[10] = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, 0xff252827);
            send = 10;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, 0xff252827);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, value);

//            mColors[11] = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, 0xffffffff);
            send = 11;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, value);

//            mColors[12] = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, 0xffffffff);
            send = 12;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, value);

//            mColors[13] = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, 0xff252827);
            send = 13;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, 0xff252827);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, value);

//            mColors[14] = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, 0xc8ffffff);
            send = 14;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, 0xc8ffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, value);

//            mColors[15] = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, 0xc8ffffff);
            send = 15;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, 0xc8ffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, value);

//            mColors[16] = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, 0xc8ff0000);
            send = 16;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, 0xc8ff0000);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, value);

//            //
//            mColors[17] = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, 0xffc16161);
            send = 17;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, 0xffc16161);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, value);

//            //
//            mColors[18] = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, 0xffffffff);
            send = 18;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, 0xffffffff);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, value);

//            mColors[19] = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, 0xffc16161);
            send = 19;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, 0xffc16161);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, value);

//            mColors[20] = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, 0xffc16161);
            send = 20;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, 0xffc16161);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, value);





            send = ACommon.CFG_MAIN_TZ_SCRIPTS_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TZ_SCRIPTS,
                    config.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TZ_SCRIPTS, value);
            //
            send = ACommon.CFG_MAIN_TZ_CIRCLES_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TZ_CIRCLES,
                    config.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TZ_CIRCLES, value);
            //
            send = ACommon.CFG_MAIN_TZ_SIGN_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TZ_SIGN,
                    config.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TZ_SIGN, value);
            //
            send = ACommon.CFG_MAIN_TZ_POINT_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_TZ_POINT,
                    config.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_TZ_POINT, value);







//            //
//            mColors[21] = config.getInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, 0xFF9DC775);
            send = 21;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, 0xFF9DC775);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DIGITS, value);

//            mColors[22] = config.getInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, 0xFF9DC775);
            send = 22;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, 0xFF9DC775);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_HOURHAND, value);

//            mColors[23] = config.getInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, 0xFF9DC775);
            send = 23;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, 0xFF9DC775);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, value);

//            public static final String CFG_COLOR_AMBIENT_DECORUPPER = String.valueOf(CFG_AMBIENT_DECORUPPER_COLOR); 27
            send = 27;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DECORUPPER,
                    config.getInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, 0xFF9DC775));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DECORUPPER, value);

//            mColors[24] = config.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878);
            send = 24;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TICK, value);

//            mColors[25] = config.getInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, 0xC8787878);
            send = 25;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, 0xC8787878);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, value);


////            mColors[26] = config.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898);
//            send = 26;
//            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898);
//            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, value);
//
            //public static final int CFG_AMBIENT_DOMBACK_COLOR = 31;
            send = ACommon.CFG_AMBIENT_DOMBACK_COLOR; //31;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DOMBACK,
                    config.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DOMBACK, value);

            //public static final int CFG_AMBIENT_DOMFRONT_COLOR = 32;
            send = 32;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DOMFRONT, 0xFF000000);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DOMFRONT, value);
            //public static final int CFG_AMBIENT_AUXHANDS_COLOR = 33;

            send = ACommon.CFG_AMBIENT_DOM_FRAME_COLOR; //34;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_FRAME, 0xFF787878);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_DOM_FRAME, value);

            send = 33;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_AUXHANDS,
                    config.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_AUXHANDS, value);

            send = ACommon.CFG_AMBIENT_TZ_SCRIPTS_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_SCRIPTS,
                    config.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TZ_SCRIPTS, value);
            //
            send = ACommon.CFG_AMBIENT_TZ_CIRCLES_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_CIRCLES,
                    config.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878));
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TZ_CIRCLES, value);
            //
            send = ACommon.CFG_AMBIENT_TZ_SIGN_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_SIGN, Color.BLACK);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TZ_SIGN, value);
            //
            send = ACommon.CFG_AMBIENT_TZ_POINT_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_POINT, Color.BLACK);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_AMBIENT_TZ_POINT, value);












            //
            send = ACommon.CFG_MAIN_INSCRIPTION_1_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_1, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_1, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_2_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_2, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_2, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_3_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_3, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_3, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_4_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_4, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_4, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_5_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_5, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_5, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_6_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_6, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_6, value);
            //
            send = ACommon.CFG_MAIN_INSCRIPTION_7_COLOR;
            value = config.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_7, 0xFFFFFFFF);
            mData[mTotalLoaded++] = new DataRow(send, null, ACommon.CFG_COLOR_MAIN_INSCRIPTION_7, value);





            Inscription inscription = new Inscription(HandheldCompanionConfigActivity.this);
            Inscription.unBundleInscription(config, inscription);


            if (null == mElementName) mElementName = getResources().getStringArray(R.array.layout_element_name);
            for (ind=0; ind < mTotalLoaded; ind++) {
                //descr = mElementName[ind] + "  [" + (ind + 1) + " из " + mColors.length + "]";
                send = mData[ind].mIndexToSend;
                if (send >= ACommon.CFG_FIRST_INSCRIPTION_COLOR && send <= ACommon.CFG_LAST_INSCRIPTION_COLOR) {
                    String inscriptionText = inscription.text[send - ACommon.CFG_FIRST_INSCRIPTION_COLOR];
                    mData[ind].mDescription = mElementName[send] + " (" + inscriptionText + ") " + "  [" + (ind + 1) + " " +
                            getResources().getString(R.string.from) + " " + mTotalLoaded + "]";

                } else {
                    mData[ind].mDescription = mElementName[send] + "  [" + (ind + 1) + " " +
                            getResources().getString(R.string.from) + " " + mTotalLoaded + "]";
                }
            }

        } // loadData

    } // IndexedColor
    IndexedColor mIndexedColor;
    




    

//    android.support.v7.app.ActionBar mActionBar;
//    android.support.v7.app.ActionBar.Tab mTabLayout, mTabCollection, mTabSettings;
//    SingleTabView mTabCollectionView;

    //FragmentSettings fragmentSettings;
    //PageFragmentSettings fragmentSettings;

    Vibrator mVibrator;

    //public ArrayList<String> mLayoutsPalette = new ArrayList<>();
    //public int mLayoutsPaletteIndex = 0;

//    public void SaveConfigPalette(ArrayList<String> configPaletteList, String cfn) {
//        if (null == configPaletteList) return;
//        //if (mLayoutsPalette.size() == 0) return;
//        //String cfn = getString(R.string.configFileName);
//        Bundle configPalette = new Bundle();
//        configPalette.putStringArrayList(ACommon.KEY_CONFIG_PALETTE, configPaletteList);
//        ACommon.savePersistentDataToFile(this, cfn, configPalette);
//    } // SaveConfigPalette

//    public void RestoreConfigPalette() {
//        String cfn = getString(R.string.configFileName);
//        Bundle configPalette = ACommon.readPersistentDataFromFile(this, cfn);
//        if (null == configPalette) return;
//        mLayoutsPalette = configPalette.getStringArrayList(ACommon.KEY_CONFIG_PALETTE);
//    } // RestoreConfigPalette


    public void removeConfigPaletteElement(int pos) {

        //if (true) return;

        mAdapter.lock();
        mAdapter.notifyDataSetChanged();

        Layout layoutElement = mService.gLayoutsPalette.remove(pos);
        mService.clearLayoutElementFiles(layoutElement);
        String title = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
        if (mScreenWideEnough == false) {
            //mSampleFragmentPagerAdapter.doTabTitleFlash(ACommon.TAB_PAGE_COLLECTION_IND);
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
        } else {
            String titleText = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
            TextView titleView;
            titleView = (TextView) findViewById(R.id.titleCollections);
            titleView.setText(titleText.toUpperCase());
            //
            //doTabTitleFlash(titleView);
        }
        flashCollectionTabTitle.run();

        //mService.gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
        //new AsyncFileSerialization(mService, this).execute(AsyncFileSerialization.SAVE, getString(R.string.configFileName));
        requestSaveLayoutPalette();
    }
    public void fireLayoutsPaletteChanged() {
        //Log.i(TAG, "((( fireLayoutsPaletteChanged, mAdapter locked=" + mAdapter.mLocked);
        if (null == mService || null == mService.gLayoutsPalette) return;
        if (true != mLayoutInflated) return;
        String title = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
        mAdapter.notifyDataSetChanged();
        if (mScreenWideEnough == false) {
            //mSampleFragmentPagerAdapter.doTabTitleFlash(ACommon.TAB_PAGE_COLLECTION_IND);
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
        } else {
            //String titleText = getString(R.string.tab_collection) + " (" + mService.mLayoutsPalette.size() + ")";
            TextView titleView;
            titleView = (TextView) findViewById(R.id.titleCollections);
            titleView.setText(title.toUpperCase());
            //
            //doTabTitleFlash(titleView);
        }
    } // fireLayoutsPaletteChanged
    //
    public void addConfigPaletteElement(String cpel) {
        mService.mLayoutsPalette.add(cpel);
        String title = getString(R.string.tab_collection) + " (" + mService.mLayoutsPalette.size() + ")";
        mAdapter.notifyDataSetChanged();
        //mTabCollection.setText(title);
        //mTabCollectionView.setTitle(title);
        //mTabCollectionView.Flash();

        if (mScreenWideEnough == false) {
            mSlidingFragmentPagerAdapter.doTabTitleFlash(ACommon.TAB_PAGE_COLLECTION_IND);
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
            //mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_COLLECTION_IND, 0xff000000);
        } else {
            String titleText = getString(R.string.tab_collection) + " (" + mService.mLayoutsPalette.size() + ")";
            TextView titleView;
            titleView = (TextView) findViewById(R.id.titleCollections);
            titleView.setText(titleText.toUpperCase());
            //
            doTabTitleFlash(titleView);
        }
    }
    //
    public void addLayoutsPaletteElement(Layout element) {

        mAdapter.lock();
        mAdapter.notifyDataSetChanged();

        mService.gLayoutsPalette.add(element);

        String title = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
        if (mScreenWideEnough == false) {
            //mSampleFragmentPagerAdapter.doTabTitleFlash(ACommon.TAB_PAGE_COLLECTION_IND);
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
            //mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_COLLECTION_IND, 0xff000000);
        } else {
            String titleText = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
            TextView titleView;
            titleView = (TextView) findViewById(R.id.titleCollections);
            titleView.setText(titleText.toUpperCase());
            //
            //doTabTitleFlash(titleView);
        }
        flashCollectionTabTitle.run();

        //mService.gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
        //new AsyncFileSerialization(mService, this).execute(AsyncFileSerialization.SAVE, getString(R.string.configFileName));
        requestSaveLayoutPalette();
    }
    //
    public void setConfigPaletteElementName(int position, String name) {
        //String cpName = String.valueOf(position + 1) + ". " + name;
        //ConfigPaletteSetName(String base64, String newName)
//        String base64 = ACommon.ConfigPaletteSetName(mService.mLayoutsPalette.get(position), name);
//        mService.mLayoutsPalette.set(position, base64);
//        mAdapter.notifyDataSetChanged();
//        ACommon.SaveConfigPalette(mService.mLayoutsPalette, getString(R.string.configFileName), this);

        Layout element = mService.gLayoutsPalette.get(position);
        element.name = name;
        mService.gLayoutsPalette.set(position, element);

        mAdapter.lock();
        mAdapter.notifyDataSetChanged();

        //mService.gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
        //new AsyncFileSerialization(mService, this).execute(AsyncFileSerialization.SAVE, getString(R.string.configFileName));
        requestSaveLayoutPalette();
    }
    //
    void doTabTitleFlash(TextView titleView) {
        ValueAnimator mColorAnimator;
        //TextView txtView = tabTitleView[ind];
        int color = titleView.getCurrentTextColor();
        //Log.i(TAG, "@@@ doTabTitleFlash, color=" + color);
        mColorAnimator = ObjectAnimator.ofInt(titleView, "textColor", 0xffffff00, color);
        mColorAnimator.setDuration(500);
        mColorAnimator.setEvaluator(new ArgbEvaluator());
        mColorAnimator.start();
    }
    Runnable flashCollectionTabTitle = new Runnable() {
        @Override
        public void run() {
            if (mScreenWideEnough == false) {
                mSlidingFragmentPagerAdapter.doTabTitleFlash(ACommon.TAB_PAGE_COLLECTION_IND);
            } else {
                TextView titleView = (TextView) findViewById(R.id.titleCollections);
                doTabTitleFlash(titleView);
            }
        }
    };


    class ConfigForFile {
        Bundle config;
        //Bitmap icon;
        Bitmap iconDense;
        Bitmap iconAmbient;
        Context mContext;

        ConfigForFile(Context context) {
            config = null;
            iconDense = null;
            iconAmbient = null;
            mContext = context;
        }

        void addConfig(Bundle cnf) {
            config = new Bundle();
            //config.clear();
            config.putAll(cnf);
            //Log.i(TAG, "### addConfig, config=" + (config!=null) + ", mLayoutsPalette.size()="  + mService.mLayoutsPalette.size());
            if (iconDense != null) buildConfigPaletteElement();
        }

        void addDenseIcon() {
            if (mService == null) return;
            Bitmap bmp = mService.getLastDenseScreenshot();
            //Log.i(TAG, "### addDenseIcon, mLayoutsPalette.size()=" + mService.mLayoutsPalette.size() + ", icon=" + bmp );
            if (bmp == null) return;

            float iconSide = ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION;//, height = 210f;
            Bitmap sb = Bitmap.createScaledBitmap(bmp, (int) iconSide, (int) iconSide, true);
            Bitmap mb = Bitmap.createBitmap((int) iconSide, (int) iconSide, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(mb);
            //Path clippingPath = new Path();
            //clippingPath.addCircle(width/2F, height/2F, width/2f-5f, Path.Direction.CW);
            //canvas.clipPath(clippingPath);
            canvas.drawBitmap(sb, 0, 0, /*new Paint(*/null);
            iconDense = mb.copy(Bitmap.Config.ARGB_8888, true);

            bmp = mService.getLastAmbientScreenshot();
            if (null != bmp) {
                sb = Bitmap.createScaledBitmap(bmp, (int) iconSide, (int) iconSide, true);
                mb = Bitmap.createBitmap((int) iconSide, (int) iconSide, Bitmap.Config.ARGB_8888);
                //Canvas canvas = new Canvas();
                canvas.setBitmap(mb);
                //Path clippingPath = new Path();
                //clippingPath.addCircle(width/2F, height/2F, width/2f-5f, Path.Direction.CW);
                //canvas.clipPath(clippingPath);
                canvas.drawBitmap(sb, 0, 0, /*new Paint(*/null);
                iconAmbient = mb.copy(Bitmap.Config.ARGB_8888, true);
            }

            if (config != null) buildConfigPaletteElement();
        }

        void buildConfigPaletteElementOld() {
            int bmpSize;
            ByteArrayOutputStream baos;
            byte byteArray[];
            Bundle configPaletteElement = new Bundle();

            bmpSize = iconDense.getByteCount();
            baos = new ByteArrayOutputStream(bmpSize);
            iconDense.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byteArray = baos.toByteArray();
            configPaletteElement.putByteArray(ACommon.KEY_CFGPAL_ICON, byteArray);
            if (null != iconAmbient) {
                bmpSize = iconAmbient.getByteCount();
                baos = new ByteArrayOutputStream(bmpSize);
                iconAmbient.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byteArray = baos.toByteArray();
                configPaletteElement.putByteArray(ACommon.KEY_CFGPAL_ICON_AMBIENT, byteArray);
            }
            //
            String tmpName = DateFormat.getDateTimeInstance().format(new Date());
            configPaletteElement.putString(ACommon.KEY_CFGPAL_NAME, tmpName);
            //
            configPaletteElement.putBundle(ACommon.KEY_CFGPAL_CONFIG, config);
            String cpel = ACommon.serializeBundle(configPaletteElement);
            addConfigPaletteElement(cpel);
            //Log.i(TAG, "### buildConfigPaletteElement, mLayoutsPalette.size()=" + mService.mLayoutsPalette.size());
            mInGetConfigTransaction = false;
            ACommon.SaveConfigPalette(mService.mLayoutsPalette, getString(R.string.configFileName), mContext);
        }

        void buildConfigPaletteElementNew() {
            Layout layoutElement = new Layout();
            layoutElement.iconDense = iconDense;
            layoutElement.iconAmbient = iconAmbient;
//            long timeStampMs = System.currentTimeMillis();
//            layoutElement.iconDenseFileName = String.valueOf(timeStampMs) + "_D";
//            layoutElement.iconAmbientFileName = String.valueOf(timeStampMs) + "_A";
            layoutElement.config = config;
            layoutElement.name = DateFormat.getDateTimeInstance().format(new Date());
            addLayoutsPaletteElement(layoutElement);
            mInGetConfigTransaction = false;
            //mService.gLayoutsPalette.saveToXmlFile(getString(R.string.configFileName));
        }

        void buildConfigPaletteElement() {
            buildConfigPaletteElementNew();
        }

    } // ConfigForFile
    ConfigForFile mConfigForFile;

    boolean mInGetConfigTransaction = false;
    boolean isInGetConfigTransaction() { return mInGetConfigTransaction; }
    void beginGetConfigTransaction() {
        mInGetConfigTransaction = true;
        mConfigForFile = new ConfigForFile(this);
        mConfigForFile.addDenseIcon();
    }











    //ConfigPaletteAdapter mAdapter;
    //mAdapter = new ConfigPaletteAdapter(getActivity());
    LayoutsPaletteAdapter mAdapter;





    ArrayList<Integer> gColorPalette = new ArrayList<>();

    private void saveColorPalette(Context context) {
//        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(STORETEXT, 0));
//        out.("");
//        out.close();
        //Log.i(TAG, "((( saveColorPalette");
        try {
            //File file = new File("colors");
            FileOutputStream fos = context.openFileOutput("colors", MODE_PRIVATE);
            OutputStreamWriter out = new OutputStreamWriter(fos);
            //BufferedWriter out = new BufferedWriter(new FileWriter(file)); //openFileOutput("colors", MODE_PRIVATE)
            //Log.i(TAG, "((( saveColorPalette, out=" + out);
            for (Integer color : gColorPalette) {
                String strVal = String.valueOf(color);
                String strNew = "\n";
                out.write(strVal, 0, strVal.length());
                out.write(strNew, 0, strNew.length());
                //out.newLine();
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            InputStream in = context.openFileInput("colors");
            //Log.i(TAG, "((( restoreColorPalette, in=" + in);
            if (null == in) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String strVal;
            while ((strVal = reader.readLine()) != null) {
                Integer color = Integer.parseInt(strVal);
                gColorPalette.add(color);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private BroadcastReceiver mDataFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(ACommon.EVENT_ACTION)) return;
            Bundle bundle = intent.getExtras();
            //Log.i(TAG, "*** onReceive");
            if (bundle != null) {
                int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                switch (event) {
                    case ACommon.EVT_CURRENT_CONFIG_FOR_FILE:
                        //Log.i(TAG, "### EVT_CURRENT_CONFIG_FOR_FILE = " + bundle);
                        if (mConfigForFile != null && isInGetConfigTransaction()) mConfigForFile.addConfig(bundle);
                        break;
                    case ACommon.EVT_DENSE_SCREENSHOT:
                        //Log.i(TAG, "### FRAME_SCREENSHOT READY IN SERVICE");
                        if (mConfigForFile != null && isInGetConfigTransaction()) mConfigForFile.addDenseIcon();
                        break;
                    case ACommon.EVT_CURRENT_CONFIG:
                        //Log.i(TAG, "### CURRENT_CONFIG=" + bundle);
                        setCurrentColors(bundle);
                        break;

//                    private void broadcastBooleanToActivity(int event, long time, boolean value) {
//                        Intent intent = new Intent();
//                        intent.setAction(ACommon.EVENT_ACTION);
//                        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
//                        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
//                        intent.putExtra(ACommon.CFG_SHOW_HANDHELD_BATTERY, value);
//                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//                    }
                    case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
                        boolean trgb = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_PHONE_BATTERY=" + trgb);
                        break;

                    case ACommon.EVT_WEARCFG_TOGGLE_LAYOUT:
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_LAYOUT");
                        break;

                    case ACommon.EVT_LAYOUTS_PALETTE_CHANGED:
                        //Log.i(TAG, "((( EVT_LAYOUTS_PALETTE_CHANGED");
                        fireLayoutsPaletteChanged();
                        break;


                    case ACommon.EVT_SIGNAL_HOLDOFF:
                        //Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF");
                        if (mSplash != null) {
                            mSplash.dismissSplash(getSupportFragmentManager(), mScreenWideEnough);
                            mSplash = null;
                        }
                        setSlidingTabLayout();
                        break;

                    case ACommon.EVT_SIGNAL_HOLDOFF_UPDATE:
                        //Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF_UPDATE");
                        if (mSplash != null) {
                            mSplash.dismissSplash(getSupportFragmentManager(), mScreenWideEnough);
                            mSplash = null;
                        }
                        mAdapter.unlock();
                        mAdapter.notifyDataSetChanged();
                        break;


                    default:
                        break;
                }
            }
        }
    }; // BroadcastReceiver mDataFromService



    private void requestSaveLayoutPalette() {
//        mSplash = new SplashDialogFragment();
//        mSplash.setType(SplashDialogFragment.FragmentType.ORDINAL);
//        mSplash.showSplash(getSupportFragmentManager(), mScreenWideEnough);
//        mService.requestSaveLayoutPalette();
        requestSaveLayoutPalette(false);
    }
    public void requestSaveLayoutPalette(boolean includeIcons) {
        mSplash = new SplashDialogFragment();
        mSplash.setType(SplashDialogFragment.FragmentType.ORDINAL);
        mSplash.showSplash(getSupportFragmentManager(), mScreenWideEnough);
        mService.requestSaveLayoutPalette(includeIcons);
    }



    private boolean isDataFromServiceReceiverRegistered = false;
    //
    private void registerDataFromServiceReceiver() {
        if (!isDataFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(HandheldCompanionConfigActivity.this).
                    registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));
            isDataFromServiceReceiverRegistered = true;
        }
    } // registerDataFromServiceReceiver
    private void unregisterDataFromServiceReceiver() {
        if (isDataFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(HandheldCompanionConfigActivity.this).unregisterReceiver(mDataFromService);
        }
        isDataFromServiceReceiverRegistered = false;
    } // unregisterDataFromServiceReceiver


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APhoneService.LocalBinder binder = (APhoneService.LocalBinder) service;
            mService = binder.getService();
            mServiceBound = true;
            //Log.i(TAG, "((( onServiceConnected: mService=" + mService);

            //setSlidingTabLayout();

            //mService.setPeerId(mPeerId); // !!!

            mService.activityConnected(mPeerId);


            //setPagerSlidingTabStrip();

            //registerDataFromServiceReceiver();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "((( onServiceDisconnected");
            mServiceBound = false;
            mService = null;

            //unregisterDataFromServiceReceiver();
        }
    };





    @Override
    protected void onStop() {
        //Log.i(TAG, "((( onStop");
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        if (mServiceBound) {
            unbindService(mServiceConnection);
            //mServiceBound = false;
        }

//        // hack !!!
        //mAdapter.setLayoutsPalette(null);
        //mAdapter.notifyDataSetInvalidated();
        //System.gc();
//        finish();

        unregisterDataFromServiceReceiver();

        //mLayoutInflated = false;

        saveColorPalette(this);

    } // onStop

    @Override
    protected void onStart() {
        //Log.i(TAG, "((( onStart");
        super.onStart();
        mGoogleApiClient.connect();
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

        Intent intent = new Intent(this, APhoneService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        registerDataFromServiceReceiver();

//        mProgressBar.setVisibility(View.VISIBLE);
//        mProgressBar.postInvalidate();
//        mProgressBar.setProgress(30);
//        mProgressBar.postInvalidate();


    } // onStart

    @Override
    protected void onResume() {
        //Log.i(TAG, "((( onResume, isServiceBound()=" + isServiceBound());
        super.onResume();
        new QuitIfNoConnectedNodesThread(this).start();
        //LocalBroadcastManager.getInstance(this).registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));

        SplashDialogFragment splash = (SplashDialogFragment) getSupportFragmentManager().findFragmentByTag(SplashDialogFragment.SPLASH_TAG);
        if (null != splash && (splash.getType() == SplashDialogFragment.FragmentType.ORDINAL)) {
            splash.dismissSplashUnconditionally(getSupportFragmentManager());
            mAdapter.unlock();
        }
        fireLayoutsPaletteChanged(); //mAdapter.notifyDataSetChanged();
    }

    class QuitIfNoConnectedNodesThread extends Thread {
        //String path;
        //DataMap dataMap;
        int count = 0;
        Activity mActivity;

        // Constructor for sending data objects to the data layer
        QuitIfNoConnectedNodesThread(Activity activity) {
            //path = p;
            //dataMap = data;
            count = 0;
            mActivity = activity;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                count++;
                //Log.i(TAG, "((( QuitIfNoConnectedNodesThread, peer[" + count + "] = " + node.getDisplayName() + " (hostId = " + node.getId() + ")");
            }
            if (0 == count) {
                //todo: add toast to inform user
                mActivity.finish();
            }
        }
    } // class QuitIfNoConnectedNodesThread

    @Override
    protected void onPause() {
        //Log.i(TAG, "((( onPause, isServiceBound()=" + isServiceBound());
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataFromService);
    }

//    LocalBroadcastManager.getInstance(this).registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));
//    LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataFromService);


    @Override
    protected void onDestroy() {
        //Log.i(TAG, "((( onDestroy");
        super.onDestroy();
//        ACommon.SaveConfigPalette(mLayoutsPalette, getString(R.string.configFileName), this);




    } // onDestroy

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Log.i(TAG, "((( onSaveInstanceState");

        //this.finish();

        super.onSaveInstanceState(outState);


    }

    class CustomToastView extends View {
        private int m_nColor;
        private Typeface m_tTypeface;
        private int m_nSize;
        private int m_nRotationAngle, mPivotX, mPivotY;
        private String m_szText;
        private Context mContext;

        public CustomToastView(Context context) {
            super(context);
            // set default parameters
            mContext = context;
            m_nColor = Color.WHITE;
            m_nSize = 28;
            m_nRotationAngle = 0;
            mPivotX = 0;
            mPivotY = 0;
            m_tTypeface = Typeface.create("arial", Typeface.NORMAL);
        }

        public void SetColor(int newcolor) {
            m_nColor = newcolor;
            this.invalidate();
        }

        public void SetTextSize(int newsize) {
            m_nSize = newsize;
            this.invalidate();
        }

        //style: normal-0,bold-1,italic-2,bold-italic-3,
        public void SetFont(String newfontface, int style) {
            m_tTypeface = Typeface.create(newfontface, style);
            this.invalidate();
        }
        public void SetRotation(int newangle) {
            m_nRotationAngle = newangle;
            this.invalidate();
        }
        public void SetRotationXY(int newangle, int neww, int newh) {
            m_nRotationAngle = newangle;
            mPivotX = neww;
            mPivotY = newh;
            this.invalidate();
        }
        public void SetText(String newtext) {
            m_szText = newtext;
            this.invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int height = canvas.getHeight();
            int width = canvas.getWidth();
            //Log.i(TAG, "!!!!! onDraw, canvas prerotate, H=" + height + ", W=" + width);
            canvas.rotate(m_nRotationAngle, width / 2, height / 2);
            //Log.i(TAG, "!!!!! onDraw, canvas postrotate, H=" + canvas.getHeight() + ", W=" + canvas.getWidth());
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.setTranslate(canvas.getWidth()/2, canvas.getHeight()/2);
            //
            Rect txtBounds = new Rect();
            Paint paint = new Paint();
            paint.setTextSize(m_nSize);
            paint.setTypeface(m_tTypeface);
            //
            paint.getTextBounds(m_szText, 0, m_szText.length(), txtBounds);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(0xff787878);
            Rect substrate = new Rect(width/2-txtBounds.width()/2-20, height/2-txtBounds.height()/2-20,
                    width/2+txtBounds.width()/2+20, height/2+txtBounds.height()/2+20);
            canvas.drawRect(substrate, paint);
            //

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(m_nColor);
            //paint.setShadowLayer(1, 0, 1, Color.parseColor("#000000"));

            //canvas.rotate(m_nRotationAngle, mPivotX, mPivotY);
            //canvas.rotate(m_nRotationAngle, width/2, height/2);
            canvas.drawText(m_szText, width/2-txtBounds.width()/2, height/2+txtBounds.height()/2, paint);
            super.onDraw(canvas);
        }
    } // class CustomToastView




    private int lockOrientation() {
        boolean orientationLandscape, orientationReverse;
        //http://stackoverflow.com/questions/3611457/android-temporarily-disable-orientation-changes-in-an-activity/26601009
        Display display = getWindowManager().getDefaultDisplay();
        //int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int density = displayMetrics.densityDpi;
        //Log.i(TAG, "!!!!! lockOrientation(), W=" + width + ", H=" + height + ", DPI=" + density);
        int widthDP = width * 160 / density;
        int heightDP = height * 160 / density;
        //Log.i(TAG, "((( lockOrientation(), Wdp=" + widthDP + ", Hdp=" + heightDP);
        //
//        Point size = new Point();
//        display.getSize(size);
//        height = size.y;
//        width = size.x;
//        Log.i(TAG, "!!!!! lockOrientation(), W=" + width + ", H=" + height + ", DPI=" + density);
        //
        int rotation = display.getRotation();
        int orientation = getResources().getConfiguration().orientation;
        int rotationAngle = 0;

        switch (rotation) {
            case Surface.ROTATION_90:
                rotationAngle = 90;
                if (width > height) {
                    orientationLandscape = true; orientationReverse = false;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    orientationLandscape = false; orientationReverse = true;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                }
                break;
            case Surface.ROTATION_180:
                rotationAngle = 180;
                if (height > width) {
                    orientationLandscape = false; orientationReverse = true;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else {
                    orientationLandscape = true; orientationReverse = true;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                break;
            case Surface.ROTATION_270:
                rotationAngle = 270;
                if (width > height) {
                    orientationLandscape = true; orientationReverse = true;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else {
                    orientationLandscape = false; orientationReverse = false;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case Surface.ROTATION_0:
            default :
                rotationAngle = 0;
                if (height > width) {
                    orientationLandscape = false; orientationReverse = false;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    orientationLandscape = true; orientationReverse = false;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
        }

        if (orientationLandscape == true) {
            if (widthDP > 900) {
//                if (orientationReverse == true) {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                } else {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                }

                mScreenWideEnough = true;
            } else {
                if (orientationReverse == true) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                // rotated TOAST!!!

                // Creating a new toast object
                Toast myToast = new Toast(this);
                // Creating our custom text view, and setting text/rotation
                CustomToastView text = new CustomToastView(this);
                text.SetText(getResources().getString(R.string.screen_rotate_warning));
                text.SetRotation(90);
                myToast.setView(text);
                // Setting duration and displaying the toast
                myToast.setDuration(Toast.LENGTH_LONG);
                myToast.show();

                mScreenWideEnough = false;
            }
        } else {
            if (widthDP > 900) {
//                if (orientationReverse == true) {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//                } else {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                }
            } else {
//                if (orientationReverse == true) {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//                } else {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                }
            }
            mScreenWideEnough = false;
        }

        return rotationAngle;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        int pid = android.os.Process.myPid();
        int tid = android.os.Process.myTid();
        //Log.i(TAG, "((( onCreate, PID=" + pid + ", TID=" + tid);

        super.onCreate(savedInstanceState);


// http://stackoverflow.com/questions/13305861/fool-proof-way-to-handle-fragment-on-orientation-change
//        FragmentManager fm = getSupportFragmentManager();
//        //
//        PageFragmentLayout fragment_layout = (PageFragmentLayout) fm.findFragmentById(R.id.frame_layout);
//        PageFragmentSettings fragment_settings = (PageFragmentSettings) fm.findFragmentById(R.id.frame_settings);
//        PageFragmentCollection fragment_collection = (PageFragmentCollection) fm.findFragmentById(R.id.frame_collection);
//        Log.i(TAG, "((( onCreate, fragment_layout=" + fragment_layout + ", fragment_settings=" + fragment_settings +
//                ", fragment_collection=" + fragment_collection);
//        //
//        if (fragment_layout != null) fm.beginTransaction().detach(fragment_layout).remove(fragment_layout).commitAllowingStateLoss();
//        if (fragment_settings != null) fm.beginTransaction().detach(fragment_settings).remove(fragment_settings).commitAllowingStateLoss();
//        if (fragment_collection != null) fm.beginTransaction().detach(fragment_collection).remove(fragment_collection).commitAllowingStateLoss();


        setContentView(R.layout.main);

        Intent intent = new Intent(this, APhoneService.class);
        startService(intent);

/*
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(false);
        }
*/

        productId = getResources().getString(R.string.product_id);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        ComponentName name = getIntent().getParcelableExtra(WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        //Log.i(TAG, "((((( onCreate, mPeerId=" + mPeerId + ", ComponentName=" + name);

//        Uri uri = getIntent().getData();
//
//        // when activity started with file from email attachment or just file with encoded layouts palette
//        if (null != uri) {
//            // see here: http://richardleggett.co.uk/blog/2013/01/26/registering_for_file_types_in_android/
//            String filePath = uri.getEncodedPath();
//            Log.i(TAG, "((((( onCreate, arrived file nmae = " + filePath);
////            //String filePath = getIntent().getDataString();
////            ArrayList<String> arrivedConfigPalette; // = new ArrayList<>();
////            Bundle bundleArrived = ACommon.readPersistentDataFromFile(this, filePath);
////            if (null != bundleArrived) {
////                arrivedConfigPalette = bundleArrived.getStringArrayList(ACommon.KEY_LAYOUTS_PALETTE);
////                if (null != arrivedConfigPalette) {
////                    // dialog with ADD arrived to current layouts palette or completely REPLACE current layouts palette by arrived
////                    for (int i = 0; i < arrivedConfigPalette.size(); i++) {
////                        String base64row = arrivedConfigPalette.get(i);
////                        //addConfigPaletteElement(base64row);
////                    }
////                }
////            }
//        }




        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();



        mScreenWideEnough = false;
        int r = 0;
        r = lockOrientation();





        //
//        // Creating a new toast object
//        Toast myToast = new Toast(this);
//        // Creating our custom text view, and setting text/rotation
//        CustomToastView text = new CustomToastView(this);
//        text.SetText("ROTATION = " + r);
//        text.SetRotation(90, 120, 90);
//        myToast.setView(text);
//        // Setting duration and displaying the toast
//        myToast.setDuration(Toast.LENGTH_LONG);
//        myToast.show();
        //
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);




        //mAdapter = new ConfigPaletteAdapter(this);
        mAdapter = new LayoutsPaletteAdapter(this);



//        RestoreConfigPalette();
//        Log.i(TAG, "*** mLayoutsPalette.size() = " + mLayoutsPalette.size());

/*
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            //actionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);

            ActionBarTabListener tabListener;

            mTabLayout = mActionBar.newTab();
            mTabLayout.setText(R.string.tab_layout);
            tabListener = new ActionBarTabListener<FragmentLayout>(this, getString(R.string.tab_layout), FragmentLayout.class);
            mTabLayout.setTabListener((android.support.v7.app.ActionBar.TabListener) tabListener);
            mActionBar.addTab(mTabLayout);

            mTabSettings = mActionBar.newTab();
            mTabSettings.setText(R.string.tab_settings);
            tabListener = new ActionBarTabListener<FragmentSettings>(this, getString(R.string.tab_settings), FragmentSettings.class);
            mTabSettings.setTabListener((android.support.v7.app.ActionBar.TabListener) tabListener);
            mActionBar.addTab(mTabSettings);

            mTabCollection = mActionBar.newTab();
            String title = getString(R.string.tab_collection) + " (" + mLayoutsPalette.size() + ")";
            //mTabCollection.setText(title);
            // ***
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mTabCollectionView = (TabCollectionView) inflater.inflate(R.layout.single_tab_view, null);
            mTabCollection.setCustomView(mTabCollectionView);
                //mTabCollection.setCustomView(R.layout.single_tab_view);
                //mTabCollectionView = (TabCollectionView) mTabCollection.getCustomView();
            mTabCollectionView.setTitle(title);
            //mActionBar.getHeight()
            //Log.i(TAG, "+++++ mActionBar.getHeight()=" + mActionBar.getHeight());
            // ***
            tabListener = new ActionBarTabListener<FragmentCollection>(this, getString(R.string.tab_collection), FragmentCollection.class);
            mTabCollection.setTabListener((android.support.v7.app.ActionBar.TabListener) tabListener);
            mActionBar.addTab(mTabCollection);

            mActionBar.setDisplayShowCustomEnabled(true);

        } else {
            CharSequence text = "ActionBar missing!";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }
*/

        //setSlidingTabLayout();
        mLayoutInflated = false;

        //mElementName = getResources().getStringArray(R.array.layout_element_name);
        mIndexedColor = new IndexedColor();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);



//        View windowDecorView = getWindow().getDecorView();
//        Log.i(TAG, "((((( onCreate, windowDecorView=" + windowDecorView + ", height=" + windowDecorView.getHeight() + ", width=" + windowDecorView.getWidth());
//        //getWindowManager();

        // splsh view: http://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen
        // spinning circle: http://stackoverflow.com/questions/15585749/progressdialog-spinning-circle
        //
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ViewGroup vg = (ViewGroup) getWindow().getDecorView();
//        mSplashView = inflater.inflate(R.layout.circle_progress_splash, vg, true);

//        setContentView(R.layout.circle_progress_splash);
//        mSplashView = findViewById(R.id.rootSplashView);
//        mSplashView.postInvalidate();
//        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        Log.i(TAG, "((((( onCreate, mSplashView=" + mSplashView + ", mProgressBar=" + mProgressBar);
//        mProgressBar.setVisibility(View.VISIBLE);
//        mProgressBar.setProgress(10);
//        mProgressBar.postInvalidate();


//        if (mScreenWideEnough == false) {
//            setContentView(R.layout.activity_handheld_companion_config);
//        } else {
//            setContentView(R.layout.activity_handheld_wide);
//        }


        //
        mAdapter.lock();
        mSplash = (SplashDialogFragment) getSupportFragmentManager().findFragmentByTag(SplashDialogFragment.SPLASH_TAG);
        if (null==mSplash) {
            mSplash = new SplashDialogFragment();
            if (mSplash != null) mSplash.showSplash(getSupportFragmentManager(), mScreenWideEnough);
        }

    } // onCreate


    SplashDialogFragment mSplash;
//    View mSplashView;
//    ProgressBar mProgressBar;


    boolean mLayoutInflated = false;
    SlidingTabLayout mSlidingTabLayout;
    SlidingFragmentPagerAdapter mSlidingFragmentPagerAdapter;
    boolean mScreenWideEnough = false;


    private void setSlidingTabLayout() {

        //Log.i(TAG, "((( setSlidingTabLayout(), mLayoutInflated=" + mLayoutInflated + ", mSplash=" + mSplash);


        if(true == mLayoutInflated) return;
        //if (true) return;

        restoreColorPalette(this);

        //SystemClock.sleep(30000);

        if (mSplash != null) {
            mSplash.dismissSplash(getSupportFragmentManager(), mScreenWideEnough);
            mSplash = null;
        }

        mAdapter.unlock();
        mAdapter.setLayoutsPalette(mService.gLayoutsPalette);
//        Log.i(TAG, "((( setSlidingTabLayout(), mPaletteState=" + mService.gLayoutsPalette.mPaletteState +
//                        ", size=" + mService.gLayoutsPalette.size()
//        );

        if (mScreenWideEnough == false) {

            setContentView(R.layout.activity_handheld_companion_config);

            // Get the ViewPager and set it's PagerAdapter so that it can display items
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            mSlidingFragmentPagerAdapter = new SlidingFragmentPagerAdapter(getSupportFragmentManager(), HandheldCompanionConfigActivity.this);
            viewPager.setAdapter(mSlidingFragmentPagerAdapter);
            //mSampleFragmentPagerAdapter = (SampleFragmentPagerAdapter) viewPager.getAdapter();

            // Give the SlidingTabLayout the ViewPager
            mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
//        mSlidingTabLayout.setCustomTabView(R.layout.single_tab_view, R.id.textView);
            // Center the tabs in the layout
            mSlidingTabLayout.setDistributeEvenly(true);
//        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//            @Override
//            public int getIndicatorColor(int position) {
//                return Color.RED;
//            }
//        });
            mSlidingTabLayout.setViewPager(viewPager);

            String title = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
//            mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_COLLECTION_IND, Color.BLACK);
//            mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_COLLECTION_IND, 0x78ffffff);
            //
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_SETTINGS_IND, getString(R.string.tab_settings));
//            mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_SETTINGS_IND, Color.BLACK);
//            mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_SETTINGS_IND, 0x78ffffff);
            //
            mSlidingFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_LAYOUT_IND, getString(R.string.tab_layout));
//            mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_LAYOUT_IND, Color.BLACK);
//            mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_LAYOUT_IND, 0x78ffffff);
        } else {

            setContentView(R.layout.activity_handheld_wide);

            PageFragmentLayout fragment_layout = new PageFragmentLayout();
            PageFragmentSettings fragment_settings = new PageFragmentSettings();
            PageFragmentCollection fragment_collection = new PageFragmentCollection();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_layout, fragment_layout)
                    .add(R.id.frame_settings, fragment_settings)
                    .add(R.id.frame_collection, fragment_collection)
                    .commit();

            String titleText = getString(R.string.tab_collection) + " (" + mService.gLayoutsPalette.size() + ")";
            TextView titleView;
            titleView = (TextView) findViewById(R.id.titleCollections);
            titleView.setText(titleText.toUpperCase());
            //
            titleText = getString(R.string.tab_settings);
            titleView = (TextView) findViewById(R.id.titleSettings);
            titleView.setText(titleText.toUpperCase());
            //
            titleText = getString(R.string.tab_layout);
            titleView = (TextView) findViewById(R.id.titleLayout);
            titleView.setText(titleText.toUpperCase());
        }


        mLayoutInflated = true;

        Bundle config = mService.getCurrentConfig();
        if (null != config) {
            setCurrentColors(config);
            //setCurrentToggles(config);
        }

    }

    private void setCurrentColors(Bundle config) {
//        ArrayList<Integer> colors;
//        colors = config.getIntegerArrayList(ACommon.CFG_COLORS);
//        mColors = colors.toArray(new Integer[colors.size()]);
        
        mIndexedColor.loadColors(config);
        broadcastEmptyToFragment(ACommon.EVT_CONFIG_CHANGED);
    }



/*

    PagerSlidingTabStrip mPagerSlidingTabStrip;

    private void setPagerSlidingTabStrip() {
        if(true == mLayoutInflated) return;

        setContentView(R.layout.activity_handheld_companion_config);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), HandheldCompanionConfigActivity.this));
        mSampleFragmentPagerAdapter = (SampleFragmentPagerAdapter) viewPager.getAdapter();

        // Give the SlidingTabLayout the ViewPager
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Center the tabs in the layout
        //mSlidingTabLayout.setDistributeEvenly(true);

        mPagerSlidingTabStrip.setViewPager(viewPager);

        String title = getString(R.string.tab_collection) + " (" + mService.mLayoutsPalette.size() + ")";
//        mSampleFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_COLLECTION_IND, title);
//        mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_COLLECTION_IND, Color.BLACK);
//        mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_COLLECTION_IND, 0x78ffffff);
//        //
//        mSampleFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_SETTINGS_IND, getString(R.string.tab_settings));
//        mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_SETTINGS_IND, Color.BLACK);
//        mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_SETTINGS_IND, 0x78ffffff);
//        //
//        mSampleFragmentPagerAdapter.setTabTitleText(ACommon.TAB_PAGE_LAYOUT_IND, getString(R.string.tab_layout));
//        mSampleFragmentPagerAdapter.setTabTitleColor(ACommon.TAB_PAGE_LAYOUT_IND, Color.BLACK);
//        mSampleFragmentPagerAdapter.setTabTitleBackgroundColor(ACommon.TAB_PAGE_LAYOUT_IND, 0x78ffffff);

        mLayoutInflated = true;

        Bundle config = mService.getCurrentConfig();
        if (null != config) {
            setCurrentColors(config);
            //setCurrentToggles(config);
        }
    }
*/




/*
    // ConfigPaletteGetBitmap(mLayoutsPalette.get(INDEX))
    private Bitmap ConfigPaletteGetBitmap(String base64) {
        Bundle configPaletteElement = ACommon.deserializeBundle(base64);
        byte byteArray[] = configPaletteElement.getByteArray(ACommon.KEY_CFGPAL_ICON);
        //configPaletteElement.putString(ACommon.KEY_CFGPAL_NAME, "Т-50");
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        Bitmap bmp = BitmapFactory.decodeStream(bais);
        //mService.setLastDenseScreenshot(bmp);
        return bmp;
    }
*/


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(TAG, "onCreateOptionsMenu" );

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
        } else {
            getMenuInflater().inflate(R.menu.menu_handheld_companion_config, menu);
            CharSequence text = "ActionBar missing 2!";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.i(TAG, "onOptionsItemSelected" );

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */


    public class ActionBarTabListener<T extends Fragment> implements android.support.v7.app.ActionBar.TabListener {

        private android.support.v4.app.Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;


        public ActionBarTabListener(Activity activity, String tag, Class<T> cls) {
            mActivity = activity;
            mTag = tag;
            mClass = cls;
        }

        @Override
        public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = android.support.v4.app.Fragment.instantiate(mActivity, mClass.getName());
                //fragmentTransaction.add(android.R.id.content, mFragment, mTag);
                ft.add(android.R.id.content,mFragment,mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        @Override
        public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        }
    } // class ActionBarTabListener<T extends Fragment>




/*
    public class FragmentLayoutsOld extends android.support.v4.app.Fragment {

        GoogleApiClient GoogleApiClient;

        public FragmentLayoutsOld() {
            //super();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            mGoogleApiClient = getGoogleApiClient();
            View view = inflater.inflate(R.layout.fragment_layout, container, false);
            return view;
        }
    } // class FragmentLayoutsOld
*/







/*
    public static void sendWakeupCmd(long delay) {
        HandheldCompanionConfigActivity.
    }
*/
/*

    public void sendWakeup(long delay) {
        DataMap dataMap = new DataMap();
        //dataMap.putLong("delay", 10000);
        //new SendThroughWearNetworkThread(ACommon.WEAR_WAKEUP_PATH, dataMap).start();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WAKEUP);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putLong(ACommon.KEY_DELAY, delay*1000);
        new SendThroughWearNetworkThread(ACommon.PHONE_BATTERY_PATH, dataMap).start();
    }
    private void sendDigitsColor(int color) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_COLOR_DIGITS);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_COLOR, color);
        new SendThroughWearNetworkThread(ACommon.PHONE_BATTERY_PATH, dataMap).start();
    }
    private void sendHandsColor(int color) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_COLOR_HANDS);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_COLOR, color);
        new SendThroughWearNetworkThread(ACommon.PHONE_BATTERY_PATH, dataMap).start();
    }
    private void sendBackgroundColor(int color) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_COLOR_BACKGROUND);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_COLOR, color);
        //dataMap.put
        new SendThroughWearNetworkThread(ACommon.PHONE_BATTERY_PATH, dataMap).start();
    }

*/


/*
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
                    Log.i(TAG, "SendThroughWearNetworkThread DataMap: " + dataMap + " sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.i(TAG, "SendThroughWearNetworkThread ERROR: failed to send DataMap");
                }
            }
        }
    } // class SendTroughWearNetworkThread
*/


    @Override // DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            String host = uri.getHost(); // may be null
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                //Log.i(TAG, "onDataChanged: dataMap: " + dataMap);
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                //Log.i(TAG, "onDataChanged: deleted URI: " + uri);
            }
        }
        dataEvents.release();
    }


    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        //Log.i(TAG, "((( onConnected, bundle=" + bundle);
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "((( onConnectionSuspended, i=" + i);
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "onConnectionFailed");
    }

    @Override // NodeApi.NodeListener
    public void onPeerConnected(Node peer) {
        //Log.i(TAG, "((( onPeerDisconnected: getId()=" + peer.getId() + ", getDisplayName()=" + peer.getDisplayName());
    }

    @Override // NodeApi.NodeListener
    public void onPeerDisconnected(Node peer) {
        //Log.i(TAG, "((( onPeerDisconnected: getId()=" + peer.getId() + ", getDisplayName()=" + peer.getDisplayName());
        this.finish();
    }


/*

    public class ConfirmResetToDefaultsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.confirm_reset_to_defaults_title)
                    .setMessage(R.string.confirm_reset_to_defaults_message)
                    .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // do the job here
                            CharSequence text = "Reset confirmed";
                            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    })
                    .setNegativeButton(R.string.string_cancel, null)
                    .create();
        }

        public ConfirmResetToDefaultsDialogFragment() {
            //super();
        }
    } // class ConfirmResetToDefaultsDialogFragment
*/

/*

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        float curX, curY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                Log.i(TAG, "*** onTouchEvent, curX=" + curX + ", curY=" + curY);
                break;
        }
        return true;
    }
*/

    private void broadcastEmptyToFragment(int event) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, System.currentTimeMillis());
        intent.putExtra(ACommon.KEY_VALUE, 0);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "((( onActivityResult");
//        if (requestCode == ACommon.PICK_SENDER_REQUEST) {
//            if (resultCode == Activity.RESULT_CANCELED) {
//
//            }
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.i(TAG, "((( onTouchEvent");
//        return super.onTouchEvent(event);
//    }


} // class HandheldCompanionConfigActivity
