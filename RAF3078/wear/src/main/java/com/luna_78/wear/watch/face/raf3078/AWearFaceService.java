package com.luna_78.wear.watch.face.raf3078;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.renderscript.ScriptIntrinsicConvolve5x5;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.AppPreferences;
import com.luna_78.wear.watch.face.raf3078.common.DemoPackData;
import com.luna_78.wear.watch.face.raf3078.common.Inscription;
import com.luna_78.wear.watch.face.raf3078.common.WatchAppearance;
import com.luna_78.wear.watch.face.raf3078.common.WatchTime;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buba on 31/01/15.
 */
public class AWearFaceService extends CanvasWatchFaceService {

    public interface MonoSegmentProcessor {
        //void onSegment(int pathIndex, PathPoint start, PathPoint end, PathMeasure pathMeasure);
        void onSegment(boolean emboss, PathPoint start, PathPoint end, PathMeasure pathMeasure);
    }


    //private static final String TAG = "AWearFaceService";
    private static final String TAG = "WFS";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    //private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1) / 5;
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1) / 1;

    volatile int mFirstDayOfWeek = 2;

    //private static final String WEARABLE_DATA_PATH = ACommon.WEARABLE_DATA_PATH; //"/rus_airforce_wf/test";

    // dense
    long appearanceScreenshotTimeMs = 0L;
    long appearanceModificationTimeMs = 0L;
    // ambient
    long ambientAppearanceModificationTimeMs = 0L;
    long ambientAppearanceScreenshotTimeMs = 0L;



    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    } // createAssetFromBitmap


    @Override
    public Engine onCreateEngine() {

        return new Engine();
    }


    private class Engine extends CanvasWatchFaceService.Engine
            implements
                DataApi.DataListener,
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                ResultCallback<DataItemBuffer>
    {

        private static final boolean E = true;

        public String productId;

        private String mPeerId;

        int mDisplayWidth, mDisplayHeight;
        int mDisplayDimension; // min(mDisplayWidth, mDisplayHeight)

        static final int MSG_UPDATE_TIME = 0;

        //float mBurnInMargin;// = 10f;//10f;
        //boolean mIsSweep = false;
        //int mDigitTextHeightInPix;// = 12;

        boolean mTapSupported = false; // interactive support

//        int mMainDigitsColor = 0xffffffff;
//        int mMainHourHandColor = 0xffffffff;
//        int mMainMinuteHandColor = 0xffffffff;
//        int mMainBackgroundColor = 0xff252827;
//        int mMainMainHandsColor = 0xff000000;
//        int mMainSecondsHandColor = 0xffc6514b;
//        int mMainDomBackColor = 0xffe46f69;
//        int mMainDomFrontColor = 0xdcf0f0f0;
//        int mMainTickDigitColor = 0xc8ffffff;
//        int mMainTickColor = 0xffffffff;
//        //
//        int mMainCalendarDialBackgroundColor = 0xff252827;
//        int mMainCalendarDialDigitsColor = 0xffffffff;
//        int mMainCalendarDialTicksColor = 0xffffffff;
//        //
//        int mMainSmallAuxDialBackgroundColor = 0xff252827;
//        int mMainSmallAuxDialDigitsColor = 0xc8ffffff;
//        int mMainSmallAuxDialTick1Color = 0xc8ffffff;
//        int mMainSmallAuxDialTick2Color = 0xc8ff0000;
//        //
//        int mMainAuxHandWeekdayColor = 0xffc16161;
//        //
//        int mMainAuxHandWearBattColor = 0xffffffff;
//        int mMainAuxHandPhoneBattColor = 0xffc16161;
//        int mMainAuxHandMonthColor = 0xffc16161;
//        //
//        boolean mShowHandheldBattery = false;
//        boolean mShowRimAnimation = false;


        class WatchMainHandBmp {
            //Bitmap mHours;                    // hand from resource
            Bitmap mHoursHandBlack;             // hand scaled
            Bitmap mHoursHandColorized;         // hand outline colorized scaled mutable
            Bitmap mHoursShd;                   // hand's shadow
            Bitmap mHoursHandShadow;            // hand's shadow scaled
            Bitmap mHoursShdSM;                 // hand's shadow scaled mutable
            Bitmap mHoursHandDecor;             // hand's decor dense
            Bitmap mHoursHandDecorAmbient;      // hand's decor ambient
            //
            //Bitmap mMinutes;
            Bitmap mMinutesHandBlack;
            Bitmap mMinutesHandColorized;
            Bitmap mMinutesShd;
            Bitmap mMinutesHandShadow;
            Bitmap mMinutesShdSM;
            Bitmap mMinutesHandDecor;
            Bitmap mMinutesHandDecorAmbient;
            //
            //Bitmap mSeconds;
            Bitmap mSecondsHandBlack;
            Bitmap mSecondsHandColorized;
            Bitmap mSecondsShd;
            Bitmap mSecondsHandShadow;
            Bitmap mSecondsShdSM;
        }
        WatchMainHandBmp watchMainHandsBmp[] = new WatchMainHandBmp[ACommon.NUM_MAIN_HAND_SETS];
        //int watchMainHandsIndex = 0;

        class WatchAuxHandBmp {
            //Bitmap mMonth;
            Bitmap mAuxHandMonthBlack, mAuxHandMonthColorized, mAuxHandMonthAmbient, mAuxHandMonthShadow;
            //Bitmap mWearBatt;
            Bitmap mAuxHandWearBattBlack, mAuxHandWearBattColorized, mAuxHandWearBattAmbient, mAuxHandWearBattShadow;
            //Bitmap mPhoneBatt;
            Bitmap mAuxHandPhoneBattBlack, mAuxHandPhoneBattColorized, mAuxHandPhoneBattAmbient, mAuxHandPhoneBattShadow;
            //Bitmap mWeekday;
            Bitmap mAuxHandWeekdayBlack, mAuxHandWeekdayColorized, mAuxHandWeekdayAmbient, mAuxHandWeekdayShadow;
        }
        WatchAuxHandBmp watchAuxHandsBmp[] = new WatchAuxHandBmp[ACommon.NUM_AUX_HAND_SETS];
        //int watchAuxHandsIndex = 0;

        class WatchBackgroundBmp {
            //Bitmap mBackground;
            Bitmap mBackgroundBlack, mBackgroundColorized;
            Bitmap mBckgrShadowS; //, mBckgrShadowSM; mBckgrShadow,
            Bitmap mDomShadow, mDomShadowS, mDomShadowSM;

            WatchBackgroundBmp() {
                //Log.i(TAG, "WatchBackgroundBmp()");
                //mBackground = null;
                mBackgroundBlack = null;        // not mutable!, ambient, translucent
                mBackgroundColorized = null;    // mutable!, dense, translucent
                //mDomShadow = null;
                mDomShadowS = null;
                mDomShadowSM = null;
            } // WatchBackgroundBmp
        }
        WatchBackgroundBmp watchBackgroundsBmp[] = new WatchBackgroundBmp[ACommon.NUM_BACKGROUNDS];
        //int watchBackgroundIndex = 0;
        //int watchDomIndex = 0;

        class WatchAuxPosition {
            float cX, cY; // центр фигуры
            float dimension; // "размер" фигуры: радиус или длина диагонали параллелепипеда
            boolean trigger; // сoобщает методу о необходимости выполнить какое-либо действие (действие зависит от метода)

            WatchAuxPosition() { cX=0; cY=0; dimension=0; }
        }
        WatchAuxPosition watchAuxPositions[] = new WatchAuxPosition[] {
                new WatchAuxPosition(), // 0: день месяца (в вырезе циферблата)
                new WatchAuxPosition(), // 1: over 12
                new WatchAuxPosition(), // 2: over 6
                new WatchAuxPosition(), // 3: между центром и 3, чуть ниже центра
                new WatchAuxPosition(), // 4: между центром и 9, чуть ниже центра
                new WatchAuxPosition(), // 5: over 8
                new WatchAuxPosition(), // 6: over 4
                new WatchAuxPosition(), // 7: over 8, ближе к центру циферблата
                new WatchAuxPosition(), // 8: over 4, ближе к центру циферблата
                new WatchAuxPosition(), // 9: over 8, смещено по гризонтали к центральной оси циферблата
                new WatchAuxPosition(), // 10: over 4, смещено по гризонтали к центральной оси циферблата
        };



        public void inflateAuxPositions() {

            float r, tickRotRad, rotDeg, rotRad;
            float oneHalfMainRadius = mVars.mMainRadius/2f;
            float oneThirdMainRadius = mVars.mMainRadius/3f;

            // *** день месяца (в вырезе циферблата), drawDateTriple()
            watchAuxPositions[0].cX = mVars.centerX;
            watchAuxPositions[0].cY = mVars.centerY;
            watchAuxPositions[0].dimension = mVars.mMainRadius * 0.81f; //mVars.mMainRadius * mVars.scaleK1 - 18f; //mVars.mDigitsRadiusPathInner - 18f;
//            r = mVars.mMainRadius * mVars.scaleK1;
//            Log.i(TAG, "((((( mMainRadius=" + mVars.mMainRadius + ", r=" + r + ", dimension(-18)=" + watchAuxPositions[0].dimension);
//            Log.i(TAG, "((((( mMainRadius=" + mVars.mMainRadius + ", r=" + r + ", dimension(-17)=" + (r-17f));
//            float k18 = (r-18f) / r, k17 = (r-17f) / r;
//            Log.i(TAG, "((((( k18=" + k18);
//            Log.i(TAG, "((((( r=" + r + ", dimension(*k18)=" + mVars.mMainRadius * mVars.scaleK1 * k18);
//            Log.i(TAG, "((((( k17=" + k17);
//            Log.i(TAG, "((((( r=" + r + ", dimension(*k17)=" + mVars.mMainRadius * mVars.scaleK1 * k17);
//            Log.i(TAG, "((((( r=" + r + ", dimension(*0.807)=" + mVars.mMainRadius * 0.807f);
            watchAuxPositions[0].trigger = false; // дополнительное действие не требуется
            //
            // *** over 12, "время полёта"
            watchAuxPositions[1].cX = mVars.centerX;
//            watchAuxPositions[1].cY = mVars.centerY - (mVars.mMainRadius/2f - 11f) - 21f; //-22f;
//            watchAuxPositions[1].dimension = mVars.mMainRadius/2f - 11f;
            //watchAuxPositions[1].dimension = mVars.mMainRadius * 0.43f;
            //watchAuxPositions[1].cY = mVars.centerY - mVars.mMainRadius + watchAuxPositions[1].dimension;
            watchAuxPositions[1].cY = mVars.centerY - mVars.mMainRadius/2f - mVars.pixelDim(10f);
            watchAuxPositions[1].dimension = mVars.mMainRadius/2f - mVars.pixelDim(11f);
//            Log.i(TAG, "((((( over 12, cY=" + watchAuxPositions[1].cY + ", dimension=" +
//                    watchAuxPositions[1].dimension + ", mMainRadius=" + mVars.mMainRadius);
            watchAuxPositions[1].trigger = false; // дополнительное действие не требуется
            //
            // *** over 6
            watchAuxPositions[2].cX = mVars.centerX;
//            watchAuxPositions[2].cY = mVars.centerY + mVars.mMainRadius - mVars.mDigitTextHeightInPix - (mVars.mMainRadius/3f - 3f) - 2f;
//            watchAuxPositions[2].dimension = mVars.mMainRadius/3f - 3f - 1f;
            watchAuxPositions[2].dimension = mVars.mMainRadius/3f - mVars.pixelDim(5f);
            watchAuxPositions[2].cY = mVars.centerY + mVars.mMainRadius - mVars.mDigitTextHeightInPix - mVars.mMainRadius/3f + mVars.pixelDim(3f);
            watchAuxPositions[2].trigger = false; // дополнительное действие не требуется
            //
            // *** между центром и 3, чуть ниже центра
//            watchAuxPositions[3].cX = mVars.centerX + (mVars.mMainRadius/3f - 4f) + 12f;
//            watchAuxPositions[3].cY = mVars.centerY + 12f;
//            watchAuxPositions[3].dimension = mVars.mMainRadius/3f - 4f;
            watchAuxPositions[3].dimension = mVars.mMainRadius/3f - mVars.pixelDim(5f);
            //watchAuxPositions[3].cX = mVars.centerX + mVars.mMainRadius/3f + mVars.pixelEquivalent(7f);
            //watchAuxPositions[3].cY = mVars.centerY + mVars.pixelEquivalent(14.5f);
            rotDeg = 105.15f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = watchAuxPositions[3].dimension + mVars.pixelDim(14.6f);
            watchAuxPositions[3].cX = mVars.centerX + (float) Math.sin(rotRad) * r;
            watchAuxPositions[3].cY = mVars.centerY + (float) -Math.cos(rotRad) * r;
            //
//            Log.i(TAG, "((((( C-to-3, cY=" + watchAuxPositions[3].cY +
//                    ", cX=" + watchAuxPositions[3].cX +
//                    ", dimension=" + watchAuxPositions[3].dimension +
//                    ", mMainRadius=" + mVars.mMainRadius);
            watchAuxPositions[3].trigger = false; // дополнительное действие не требуется
            //
            // *** между центром и 9, чуть ниже центра
//            watchAuxPositions[4].cX = mVars.centerX - (mVars.mMainRadius/3f - 4f) - 12f;
//            watchAuxPositions[4].cY = mVars.centerY + 12f;
//            watchAuxPositions[4].dimension = mVars.mMainRadius/3f - 4f;
            watchAuxPositions[4].dimension = mVars.mMainRadius/3f - mVars.pixelDim(5f);
            rotDeg = 254.85f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = watchAuxPositions[4].dimension + mVars.pixelDim(14.6f);
            watchAuxPositions[4].cX = mVars.centerX + (float) Math.sin(rotRad) * r;
            watchAuxPositions[4].cY = mVars.centerY + (float) -Math.cos(rotRad) * r;
            watchAuxPositions[4].trigger = false; // дополнительное действие не требуется
            //
            // *** перекрыта 8
            rotDeg = 241.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(53f);
            watchAuxPositions[5].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[5].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            //watchAuxPositions[5].dimension = (mVars.mMainRadius/3f - 6f);
            watchAuxPositions[5].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
            watchAuxPositions[5].trigger = true; // действие: затереть остатки цифры 8
            //
            // *** перекрыта 4
            rotDeg = 118.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(53f);
            watchAuxPositions[6].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[6].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[6].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
            watchAuxPositions[6].trigger = true; // действие: затереть остатки цифры 4
            //
            // *** перекрыта 8, ближе к центру циферблата
            rotDeg = 241.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(58f);
            watchAuxPositions[7].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[7].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[7].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
            watchAuxPositions[7].trigger = true; // действие: затереть остатки цифры 8 перед отрисовкой
            //
            // *** перекрыта 4, ближе к центру циферблата
            rotDeg = 118.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(58f);
            watchAuxPositions[8].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[8].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[8].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
            watchAuxPositions[8].trigger = true; // действие: затереть остатки цифры 4 перед отрисовкой
            //
            // *** перекрыта 8, смещено по гризонтали к центральной оси циферблата
            rotDeg = 241.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(58f);
            watchAuxPositions[9].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r) + mVars.pixelDim(10f);
            watchAuxPositions[9].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[9].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
//            rotDeg = 241.5f;
//            rotRad = (float) (Math.PI * rotDeg / 180f);
//            r = 58f;
//            watchAuxPositions[9].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r) + 8f;
//            watchAuxPositions[9].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
//            watchAuxPositions[9].dimension = (mVars.mMainRadius/3f - 6f);
            watchAuxPositions[9].trigger = true; // действие: затереть остатки цифры 8 перед отрисовкой
            //
            // *** перекрыта 4, смещено по гризонтали к центральной оси циферблата
            rotDeg = 118.5f;
            rotRad = (float) (Math.PI * rotDeg / 180f);
            r = mVars.pixelDim(58f);
            watchAuxPositions[10].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r) - mVars.pixelDim(10f);
            watchAuxPositions[10].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
            watchAuxPositions[10].dimension = (mVars.mMainRadius/3f - mVars.pixelDim(4f));
//            rotDeg = 118.5f;
//            rotRad = (float) (Math.PI * rotDeg / 180f);
//            r = 58f;
//            watchAuxPositions[10].cX = mVars.centerX + (float) Math.sin(rotRad) * (mVars.mDigitsRadiusPathInner - r) - 8f;
//            watchAuxPositions[10].cY = mVars.centerY + (float) -Math.cos(rotRad) * (mVars.mDigitsRadiusPathInner - r);
//            watchAuxPositions[10].dimension = (mVars.mMainRadius/3f - 6f);
            watchAuxPositions[10].trigger = true; // действие: затереть остатки цифры 4 перед отрисовкой
        } // inflateAuxPositions


        public DemoPackData[] demoPackData = new DemoPackData[DemoPackData.NUM_DEMOPACK_PARAMETERS];


        public class Variables {
//            public long now = 0;
//            public int milliseconds = 0;
//            long currSeconds = 0;
            int width = 0;
            int height = 0;
            float mScreenRadius;
            float PIXELDIM;
            float centerX = 0;
            float centerY = 0;
            //float radiusClockDialOuterMax = 0;
            float mMainRadius; // внешний радиус циферблата

            float screenRatio;
            float pivotDialX;
            float pivotDialY;
            float pivotHandX;
            float pivotHandY;
            int handBitmapHeight;

            float mDigitsRadiusPathInner;// = 146.f; // 146.f is max to fit LG Watch R
            float mDigitsRadiusPathOuter;// = 158.f; // 158.f is max to fit LG Watch R
            int mDigitTextHeightInPix;
            float mDigitTextSize = 0f;
            Rect textBounds = new Rect();
            float innerTickRadius = 0f;//mDigitsRadiusPathInner;
            float outerTickRadius = 0f;//mDigitsRadiusPathOuter;
            float tickCenterRadius = 0f;
            float tickSegment1Length = 0f;
            float tickSegment5Length = 0f;
            Matrix hrMatrix = new Matrix();
            Path mPathInner = new Path(), mPathOuter = new Path();
            int corrCount = 0;
            int tickIndex = 0;
            float digitPos = 0;
            int corrIndex = 0;
            float tickRot = 0;
            float innerX = 0;
            float innerY = 0;
            float outerX = 0;
            float outerY = 0;
            int[] rimColors = new int[]{0xff000000, 0xff000000, 0xffffffff, 0xffffffff, 0xff000000, 0xff000000};
            //int[] rimColors = new int[]{0xff202020, 0xff202020, 0xffdfdfdf, 0xffdfdfdf, 0xff202020, 0xff202020};
            float[] rimColorOnPathPos = new float[]{0f, 0.20f, 0.40f, 0.60f, 0.80f, 1.0f};
            Path rimPath = new Path();
            Path handPath = new Path();
            Bitmap backgroundBitmap;
            Bitmap frameBitmapDense; // scaled mutable!!!
            Canvas frameCanvasDense;
            Bitmap frameBitmapAmbient; // scaled mutable!!!
            Canvas frameCanvasAmbient;
            //
            float mBurnInMargin;
            //float mScaleEffective = 1.0f;
            //
            float scaleK1 = 0.92f;

            BlurMaskFilter blurMaskFilterR3N;// = new BlurMaskFilter(pixelEquivalent(3f), BlurMaskFilter.Blur.NORMAL);
            BlurMaskFilter blurMaskFilterR2N;// = new BlurMaskFilter(pixelEquivalent(2f), BlurMaskFilter.Blur.NORMAL);

            //BlurMaskFilter blurMaskFilterR3S;
            //BlurMaskFilter blurMaskFilterR3I;
            //BlurMaskFilter blurMaskFilterR3O;


            Path handOutline;
            Matrix offsetMatrix = new Matrix();

            //Bitmap auxBevelShadowBitmap;

            int[] dayTriplet = new int[3];

            Rect rect = new Rect();


            Variables(Canvas canvas, Rect bounds) {
                super();

                // globals
                //
                //this.mDigitTextHeightInPix = 12;
                this.width = bounds.width();
                this.height = bounds.height();
                //
                if (DemoPackData.isActive(demoPackData)) {
                    int res = DemoPackData.getResolution(demoPackData);
                    if (DemoPackData.RESOLUTION_NATURAL != res) {
                        //canvas.drawColor(Color.BLACK);
                        this.width = res;
                        this.height = res;
                    }
                }
                //
//                this.width = 500;
//                this.height = 500;
//                this.width = 400;
//                this.height = 400;
//                this.width = 280;
//                this.height = 280;
                //
                // Find the center. Ignore the window insets so that, on round watches with a
                // "chin", the watch face is centered on the entire screen, not just the usable
                // portion.
                this.centerX = this.width / 2f;
                this.centerY = this.height / 2f;

//sendWearConfigBooleanOption(
// ACommon.EVT_WEARCFG_BURNIN_MARGIN,
// ACommon.CFG_SHOW_RIM_ANIMATION, denseAppearance.mShowRimAnimation);

                //
                this.mScreenRadius = Math.min(this.centerX, this.centerY);
                //
                this.PIXELDIM = ((1f / 160f) * this.mScreenRadius);
                //
                this.mBurnInMargin = 0f; // 10f recommended by google
                if ((true == mAmbientProp.getBurnIn() && mAppPreferences.getRespectBurnIn()) || /*mAmbientProp.mRespectBurnIn*/
                        // дать возможность показать BurnIn Margin Ring на устройствах без этой защиты
                        (false == mAmbientProp.getBurnIn() && !mAppPreferences.getRespectBurnIn())) { /*mAmbientProp.mRespectBurnIn*/
                    this.mBurnInMargin = WatchAppearance.RECOMMENDED_BURNIN_MARGIN * this.PIXELDIM; //10f;
                }
                //
                this.mMainRadius = this.mScreenRadius - this.mBurnInMargin;
                //this.mMainRadius = 100f - this.mBurnInMargin;
                //float scl = this.mMainRadius / Math.min(this.centerX, this.centerY);
                //this.mScaleEffective = scl;
                //
                this.screenRatio = this.width / dialElements.baseDialWidth;
                this.pivotDialX = (dialElements.baseDialDim / 2.0f) * screenRatio;
                this.pivotDialY = (dialElements.baseDialDim / 2.0f) * screenRatio;
                this.pivotHandX = (dialElements.baseHandWidth / 2.0f) * screenRatio;
                this.pivotHandY = (dialElements.baseHandHeight / 2.0f) * screenRatio;
                this.handBitmapHeight = (int) (dialElements.baseHandHeight * screenRatio);
                //
//                mDigitsRadiusPathInner = 146.f; // 146.f is max to fit LG Watch R
//                mDigitsRadiusPathOuter = 158.f; // 158.f is max to fit LG Watch R
                //float scaleK1 = 0.92f; //this.mDigitsRadiusPathInner / this.mDigitsRadiusPathOuter;
                this.mDigitsRadiusPathOuter = this.mMainRadius;
                this.mDigitsRadiusPathInner = this.mMainRadius * this.scaleK1; //- this.mDigitTextHeightInPix;
                this.mDigitTextHeightInPix = (int) (this.mDigitsRadiusPathOuter - this.mDigitsRadiusPathInner);
                //
                float scaleK2 = 0.93333334f; //this.innerTickRadius / this.outerTickRadius
                this.outerTickRadius = this.mMainRadius; //this.mDigitsRadiusPathOuter;
                this.innerTickRadius = this.mMainRadius * scaleK2; //this.mDigitsRadiusPathInner + 2f;
                this.tickCenterRadius = (this.outerTickRadius - (this.outerTickRadius - this.innerTickRadius) / 2f) * 0.993f;
                this.tickSegment1Length = (float) ((Math.PI * this.tickCenterRadius) / 30f);
                this.tickSegment5Length = (float) ((Math.PI * this.tickCenterRadius) / 6f);
                //Log.i(TAG, "((((( scaleK2=" + scaleK2);
                //
//                inflateAuxPositions();
                //
                if (0f == this.mDigitTextSize) {
                    //todo: усложнить алгоритм установки размера шрифта
                    for (int i=30; i > 10; i--) {
                        this.mDigitTextSize = i;
                        //Rect textBounds = new Rect();
                        mTickDigitPaint.setTextSize(this.mDigitTextSize);
                        mTickDigitPaint.getTextBounds("0", 0, 1, this.textBounds);
                        if (this.textBounds.height() == this.mDigitTextHeightInPix) break;
                    }
//                    Log.i(TAG, "((((( mDigitTextSize=" + this.mDigitTextSize + ", mDigitTextHeightInPix="
//                            + this.mDigitTextHeightInPix + ", textBounds=" + this.textBounds);
//                    Log.i(TAG, "((((( textBounds: bottom=" + this.textBounds.bottom + ", left=" + this.textBounds.left + ", right=" + this.textBounds.right + ", top=" + this.textBounds.top);
//                    Paint.FontMetrics fontMetrics = mTickDigitPaint.getFontMetrics();
//                    mTickDigitPaint.getFontMetrics(fontMetrics);
//                    Log.i(TAG, "((((( fontMetrics: top=" + fontMetrics.top + ", bottom=" + fontMetrics.bottom + ", ascent=" + fontMetrics.ascent + ", descent=" + fontMetrics.descent);
                    // mDigitTextSize=15.0, mDigitTextHeightInPix=12
                    // textBounds: bottom=1, left=0, right=8, top=-11
                    // fontMetrics: top=-15.842285, bottom=4.0649414, ascent=-13.916016, descent=3.6621094
                }
//                if (this.frameBitmapDense == null) {
//                    this.frameBitmapDense = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
//                }
//                if (this.frameCanvasDense == null) this.frameCanvasDense = new Canvas(this.frameBitmapDense);
//                if (this.frameBitmapAmbient == null) {
//                    this.frameBitmapAmbient = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
//                }
//                if (this.frameCanvasAmbient == null) this.frameCanvasAmbient = new Canvas(this.frameBitmapAmbient);
                this.frameBitmapDense = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
                //this.frameBitmapDense.eraseColor(Color.BLACK);
                this.frameBitmapDense.eraseColor(Color.TRANSPARENT);
                this.frameCanvasDense = new Canvas(this.frameBitmapDense);
                //this.frameCanvasDense.drawColor(Color.BLACK); // !!!
                //this.frameCanvasDense.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // !!!
                //
                this.frameBitmapAmbient = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
                this.frameBitmapAmbient.eraseColor(Color.BLACK);
                this.frameCanvasAmbient = new Canvas(this.frameBitmapAmbient);
                //this.frameCanvasAmbient.drawColor(Color.BLACK); // !!!
                //this.frameCanvasAmbient.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // !!!

                //this.blurMaskFilterR3I = new BlurMaskFilter(3f, BlurMaskFilter.Blur.INNER);
                //this.blurMaskFilterR3O = new BlurMaskFilter(3f, BlurMaskFilter.Blur.OUTER);
                //this.blurMaskFilterR3N
                //this.blurMaskFilterR3S = new BlurMaskFilter(3f, BlurMaskFilter.Blur.SOLID);

                //this.auxBevelShadowBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);

                blurMaskFilterR3N = new BlurMaskFilter(pixelDim(3f), BlurMaskFilter.Blur.NORMAL);
                blurMaskFilterR2N = new BlurMaskFilter(pixelDim(2f), BlurMaskFilter.Blur.NORMAL);

            }

            float pixelDim(float nPixels) {
                // nPixels - количество пикселей при размере экрана 320x320
                return nPixels * ((1f / 160f) * this.mScreenRadius);
            }

        } // class Vars
        //
        Variables mVars;






/*
                float tickRotRad = angleWkdStartRad + tickIndex * angleWkdayTickRad; // 0-tickIndex; //
                float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius-1.5f);
                float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius-1.5f);
*/


//        static final int WAPI_DOM = 0;
//        static final int WAPI_AUXA = 1;
//        static final int WAPI_AUXB = 2;

        class WatchLayout {
            // todo: указать диапазон индексов bitmap-цифрблатов для этой компановки
            // todo: добавить циферблаты без цифр
            boolean isVertical;
            //WatchAuxPosition dayOfMonth;
            //WatchAuxPosition auxA; // big, "время полёта" (день недели)
            Integer dayOfMonth;
            Integer auxA;           // big, "время полёта" (день недели)
            Integer auxB;           // small, "секундомер" (батареи)
            Integer auxC;           // (месяц)
            Integer auxD;           // надпись 1
            Integer auxE;           // надпись 2?

            WatchLayout(boolean v, /*WatchAuxPosition*/ Integer dom, /*WatchAuxPosition*/Integer a,
                        Integer b, Integer c, Integer d, Integer e) {
                isVertical = v;
                dayOfMonth = dom;
                auxA = a;
                auxB = b;
                auxC = c;
                auxD = d;
                auxE = e;
            } // WatchLayout constructor
        } // WatchLayout
        WatchLayout[] watchLayouts = new WatchLayout[] {
                //new WatchLayout(true, null, null, null, null, null, null), // empty vertical
                //new WatchLayout(false, null, null, null, null, null, null), // empty horizontal
//                new WatchLayout(true, watchAuxPositions[0], null, null, null, null, null),
//                new WatchLayout(false, watchAuxPositions[0], null, null, null, null, null),
                new WatchLayout(false, 0, 1, 6,  5, null, null),
                new WatchLayout(false, 0, 1, 8,  7, null, null),
                new WatchLayout(false, 0, 1, 10, 9, null, null),
                new WatchLayout(true,  0, 1, 2,  3, null, null),
                new WatchLayout(true,  0, 1, 3,  2, null, null),
                new WatchLayout(true,  0, 1, 2,  4, null, null),
                new WatchLayout(true,  0, 1, 4,  2, null, null),
                new WatchLayout(true,  0, 1, 4,  3, null, null),
                new WatchLayout(true,  0, 1, 3,  4, null, null),
                new WatchLayout(false, 0, 1, 4,  3, null, null),
                new WatchLayout(false, 0, 1, 3,  4, null, null),
                new WatchLayout(false, 0, null, null, null, null, null), // empty horizontal with DOM
                new WatchLayout(true,  0, null, null, null, null, null), // empty vertical with DOM
//                new WatchLayout(false, watchAuxPositions[0], watchAuxPositions[1], null, null, null, null),
        };
        //int watchLayoutIndex = 0;

        boolean mVisible;
        boolean mAmbient;
        //
        //boolean mRespectBurnIn = true;
        //boolean mNeedInitDrawingAssets = true;

        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mSecondPaint;
        Paint mTickPaint;
        Paint mTickDigitPaint;
        Paint mHrTickPaint;
        Paint mHandPaint;
        Paint mBackgroundPaint;
        Paint mCalendarDialPaint;
        Paint mCalendarWkdayPaint;
        Paint mCalendarMonthPaint;
        Paint mBattDialPaint;
        Paint mDatePaint;
        Paint mScriptPaint;
        Paint mDigitsPaint;
        Paint mMountingHolePaint;
        Paint mTzLabelPaint;
        Paint mTzDotPaint;
        //
        boolean mMute;

        //Time mTime;
        volatile WatchTime wTime;

        //float mMainRadius; // внешний радиус циферблата
        //float mDigitsRadiusPathInner;// = 146.f; // 146.f is max to fit LG Watch R
        //float mDigitsRadiusPathOuter;// = 158.f; // 158.f is max to fit LG Watch R

        //Rect mDigitTextBounds;
        //float mDigitTextHeight;
        //float mDigitTextSize = 0f;

        float mWatchesBattery = 0f;
        float mPhoneBattery = 0f;
        long mPhoneBatteryLastSampleTime = 0;

        private SensorManager mSensMan = null;


        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        //boolean mAmbientLowBit, mAmbientBurnInProtection;
        class AmbientProperties {
            boolean mSet = false;
            boolean mAmbientLowBit;
            boolean mAmbientBurnInProtection;
            //
            boolean mTransition = false;
            //boolean mRespectBurnIn = true;
            boolean mRespectLowBit = true;
            int mPhase = 0;
            //
            boolean mPlateTransition = false;

            boolean getLowBit() {
                if (mSet) return mAmbientLowBit;
                return false;
            }
            boolean getBurnIn() {
                if (mSet) return mAmbientBurnInProtection;
                return false;
            }
            void setValues(boolean lowBit, boolean burnIn) {
                mAmbientLowBit = lowBit;
                mAmbientBurnInProtection = burnIn;
                mSet = true;
            }
//            void setRespectBurnIn(Context context, boolean respectBurnIn) {
//                mRespectBurnIn = respectBurnIn;
//            }
            boolean isSet() { return mSet; }
            //void requestTransition(boolean respectBurnIn) { mTransition = true; mPhase = 1; mRespectBurnIn = respectBurnIn; }
            void requestTransition() { mTransition = true; mPhase = 1; }
            void clearTransition() { mTransition = false; mPhase = 0; }
            boolean inTransition() { return  mTransition; }
            //
            void requestPlateTransition() { mPlateTransition = true; }
            boolean inPlateTransition() { return  mPlateTransition; }
            void clearPlateTransition() { mPlateTransition = false; }
        } // AmbientProperties
        AmbientProperties mAmbientProp = new AmbientProperties();

/*
        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundBitmapScaled;
        Bitmap mBackgroundBitmapScaledMutable;
*/

//        Bitmap mTriangleBitmap, mTriangleBmpBlack, mTriangleBmpColorized;
//        Bitmap mTriangleBitmapShd, mTriangleBitmapShdS;

        Bitmap mDecorUpperBlack, mDecorUpperColorized, mDecorUpperShadow, mDecorUpperDecor, mDecorUpperDecorAmbient;

        Bitmap mDensePlateBitmap, mAmbientPlateBitmap;


        //Bitmap mCircleGradient;
        Bitmap mCircleGradientS;
        Bitmap mCircleGradientNoTransparent;
        Bitmap mBigAuxDialGradient, mSmallAuxDialGradient;
        Bitmap mBigAuxDialGradientNoTransparent, mSmallAuxDialGradientNoTransparent;
        Bitmap mFxPlateTexture;
        // mBigAuxDialGradientNoTransparent currentAppearance.mMainCalendarDialBackgroundColor
        // mSmallAuxDialGradientNoTransparent currentAppearance.mMainSmallAuxDialBackgroundColor




//        Bitmap mHrHandBitmap;
//        Bitmap mHrHandScaledBitmap;
//
//        Bitmap mScHandBitmap;
//        Bitmap mScHandScaledBitmap;
//
//        Bitmap mMnHandBitmap;
//        Bitmap mMnHandScaledBitmap;

//        Bitmap mWBHandBitmap;
//        Bitmap mWBHandScaledBitmap, mWBHandScaledBitmapMutable;
//        Bitmap mWBHandBitmapUpper;
//        Bitmap mWBHandScaledBitmapUpper, mWBHandScaledBitmapMutableUpper;

        //Bitmap mWkdayHandBitmap;
        //Bitmap mWkdayHandScaledBitmap;

        //Bitmap mMonthHandBitmap;
        //Bitmap mMonthHandScaledBitmap;

        //Bitmap mRimSmallBitmap;
        //Bitmap mRimSmallScaledBitmap;

//        Bitmap mDomShadowBitmap;
//        Bitmap mDomShadowScaledBitmap;

        Bitmap mWingsBitmap;
        //Bitmap mWingsScaledBitmap;

        //Bitmap mPhBHandBitmap;
        //Bitmap mPhBHandScaledBitmap;


        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(AWearFaceService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


        @Override // DataApi.DataListener
        public void onDataChanged(DataEventBuffer dataEvents) {
            //Log.i(TAG, "DataApi.onDataChanged");
            DataMap dataMap;
            for (DataEvent event : dataEvents) {
                Uri uri = event.getDataItem().getUri();
                String scheme = uri.getScheme();
                String path = uri.getPath();
                String host = uri.getHost(); // may be null
                // Check the data type
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // Check the data path
                    //if (path.equals(WEARABLE_DATA_PATH)) {
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        //Log.i(TAG, "DataApi.onDataChanged: dataMap: " + dataMap + "; scheme=" + scheme + ", node=" + host + ", path=" + path);
                    //}
                } else if (event.getType() == DataEvent.TYPE_DELETED) {
                    //Log.i(TAG, "DataApi.onDataChanged: deleted URI: " + uri);
                }
            }
            dataEvents.release();
        }

        @Override // Wearable.DataApi.getDataItems ResultCallback<DataItemBuffer>
        public void onResult(DataItemBuffer dataItems) {
            for (int i=0; i<dataItems.getCount(); i++) {
                //Log.i(TAG, "getDataItems DataItem URI: " + dataItems.get(i).getUri());
/*
                if (dataItems.get(i).getUri().getPath().equals("/rus_airforce")) {
                    Log.i(TAG, "getDataItems DataItem path=" + dataItems.get(i).getUri().getPath() + " need to be deleted!!!");
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient,dataItems.get(i).getUri());
                }
*/
            }
            dataItems.release();
        }

        @Override // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            //Log.i(TAG, "GoogleApiClient.onConnected");
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);

            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(Engine.this);
            /*Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
                @Override
                public void onResult(DataItemBuffer dataItems) {
                    for (int i=0; i<dataItems.getCount(); i++) {
                        Log.i(TAG, "WatchfaceService: DataItem: " + dataItems.get(i).getUri());
                    }
                }
            });
            */

            sendWearConfigSettings();

            ((WearApplication) getApplication()).setGoogleApiClient(mGoogleApiClient);

            new WearCrashReport.WearSendCrashReports(getApplicationContext(), mGoogleApiClient, null).start();
        }

        private void sendWearConfigSettings() {
            // todo: вызывать не только из onConnected, но и ещё откуда-то
            sendWearConfigBooleanToggle(ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY, ACommon.CFG_SHOW_HANDHELD_BATTERY,
                    mAppPreferences.getShowHandheldBattery());
            sendWearConfigBooleanToggle(ACommon.EVT_WEARCFG_TOGGLE_ANIMATION, ACommon.CFG_SHOW_RIM_ANIMATION,
                    denseAppearance.mShowRimAnimation);
            sendWearConfigBooleanToggle(ACommon.EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF, ACommon.CFG_SHOW_HRDIGITS_RELIEF,
                    denseAppearance.isShowHrDigitsRelief());
            sendWearConfigBooleanToggle(ACommon.EVT_WEARCFG_TOGGLE_DIAL_GRADIENT, ACommon.CFG_SHOW_DIAL_GRADIENT,
                    denseAppearance.mShowDialGradient);
            //
            sendWearConfigIntegerOption(ACommon.EVT_WEARCFG_SET_AUX_BEVEL_COLOR,
                    ACommon.CFG_AUX_BEVEL_COLOR, denseAppearance.mAuxBevelColor);
            //
//            sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_SET_RESPECT_BURNIN,
//                    ACommon.CFG_RESPECT_BURNIN, mAmbientProp.mRespectBurnIn);
            sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_SET_RESPECT_BURNIN,
                    ACommon.CFG_RESPECT_BURNIN, mAppPreferences.getRespectBurnIn());

            // CFG_RESPECT_LOWBIT   EVT_HHCFG_SET_RESPECT_LOWBIT    EVT_WEARCFG_SET_RESPECT_LOWBIT
            // CFG_SWEEP_SECONDS    EVT_HHCFG_SET_SWEEP             EVT_WEARCFG_SET_SWEEP
            sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_SET_RESPECT_LOWBIT,
                    ACommon.CFG_RESPECT_LOWBIT, mAmbientProp.mRespectLowBit);
            //
            sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_SET_SWEEP,
                    ACommon.CFG_SWEEP_SECONDS, mAppPreferences.getSweepSeconds()); //mIsSweep


        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
            //Log.i(TAG, "onConnectionSuspended");
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult connectionResult) {
            //Log.i(TAG, "onConnectionFailed");
        }



/*
        private boolean readPersistentDataFromFile() {
            Log.i(TAG, "try readPersistentDataFromFile");

            Parcel parcel = Parcel.obtain();
            Bundle bundle = null;
            try {
                String cfgFileName = getString(R.string.configFileName);
                FileInputStream fis = openFileInput(cfgFileName);
                byte[] array = new byte[(int) fis.getChannel().size()];
                fis.read(array, 0, array.length);
                fis.close();
                parcel.unmarshall(array, 0, array.length);
                parcel.setDataPosition(0);
                bundle = parcel.readBundle();
                bundle.putAll(bundle);
            } catch (FileNotFoundException fnfe) {
                return false;
            } catch (IOException ioe) {
                return false;
            } finally {
                parcel.recycle();
            }

            return unBundleConfig(bundle);
        } // readPersistentDataFromFile
*/


//
//        class WatchAppearance {
//            int watchMainHandsIndex = 0;
//            int watchAuxHandsIndex = 0;
//            int watchBackgroundIndex = 0;
//            int watchDomIndex = 0;
//            int watchLayoutIndex = 0;
//            //
//            boolean mShowHandheldBattery = false;
//            boolean mShowRimAnimation = false;
//            boolean mShowHrDigitsRelief = true;
//            //
//            boolean mShowDialGradient = true;
//            float dgFirstStop = 0.75f;
//            float dgHalfEdgeStop = 0.97f;
//            int dgEdgeAlpha = 100;
//            float dgFirstStop1 = 0.75f;
//            float dgHalfEdgeStop1 = 0.97f;
//            int dgEdgeAlpha1 = 100;
//            int dgInvert = ACommon.GD_INVERT_NONE;
//            //
//            int mAuxBevelColor = ACommon.BEVEL_FROM_AUX;
//            //
//            int mMainDigitsColor = 0xffffffff;
//            int mMainHourHandColor = 0xffffffff;
//            int mMainMinuteHandColor = 0xffffffff;
//            int mMainBackgroundColor = 0xff252827;
//            int mMainMainHandsColor = 0xfff0f0d2;
//            int mMainSecondsHandColor = 0xffc6514b;
//            int mMainDomBackColor = 0xffe46f69;
//            int mMainDomFrontColor = 0xdcf0f0f0;
//            int mMainTickDigitColor = 0xc8ffffff;
//            int mMainTickColor = 0xffffffff;
//            //
//            int mMainCalendarDialBackgroundColor = 0xff252827;
//            int mMainCalendarDialDigitsColor = 0xffffffff;
//            int mMainCalendarDialTicksColor = 0xffffffff;
//            //
//            int mMainSmallAuxDialBackgroundColor = 0xff252827;
//            int mMainSmallAuxDialDigitsColor = 0xc8ffffff;
//            int mMainSmallAuxDialTick1Color = 0xc8ffffff;
//            int mMainSmallAuxDialTick2Color = 0xc8ff0000;
//            //
//            int mMainAuxHandWeekdayColor = 0xffc16161;
//            //
//            int mMainAuxHandWearBattColor = 0xffffffff;
//            int mMainAuxHandPhoneBattColor = 0xffc16161;
//            int mMainAuxHandMonthColor = 0xffc16161;
//            //
//            // NEW!!!
//            int mAmbientDigitsColor = getResources().getColor(R.color.Phosphor);
//            int mAmbientHourHandColor = getResources().getColor(R.color.Phosphor);
//            int mAmbientMinuteHandColor = getResources().getColor(R.color.Phosphor);
//            int mAmbientTicksColor = getResources().getColor(R.color.PureGrey);
//            int mAmbientTickDigitColor = getResources().getColor(R.color.PureGreyC8);
//            int mAmbientDomAndAuxHandsColor = getResources().getColor(R.color.BBGreen);
//
//            WatchAppearance(WatchAppearance org) {
//                if (null != org) {
//                    this.watchMainHandsIndex = org.watchMainHandsIndex;
//                    this.watchAuxHandsIndex = org.watchAuxHandsIndex;
//                    this.watchBackgroundIndex = org.watchBackgroundIndex;
//                    this.watchDomIndex = org.watchDomIndex;
//                    this.watchLayoutIndex = org.watchLayoutIndex;
//                    //
//                    this.mShowRimAnimation = false;
//                    this.mShowHandheldBattery = false;
//                    this.mShowHrDigitsRelief = org.mShowHrDigitsRelief;
//                    //
//                    this.mShowDialGradient = org.mShowDialGradient;
//                    this.dgFirstStop = org.dgFirstStop;
//                    this.dgHalfEdgeStop = org.dgHalfEdgeStop;
//                    this.dgEdgeAlpha = org.dgEdgeAlpha;
//                    this.dgFirstStop1 = org.dgFirstStop1;
//                    this.dgHalfEdgeStop1 = org.dgHalfEdgeStop1;
//                    this.dgEdgeAlpha1 = org.dgEdgeAlpha1;
//                    this.dgInvert = org.dgInvert;
//                    //
//                    this.mAuxBevelColor = org.mAuxBevelColor;
//                    //
//                    this.mMainDigitsColor = org.mAmbientDigitsColor; // getResources().getColor(R.color.Phosphor);
//                    this.mMainHourHandColor = org.mAmbientHourHandColor;
//                    this.mMainMinuteHandColor = org.mAmbientMinuteHandColor;
//                    //
//                    this.mMainBackgroundColor = getResources().getColor(R.color.PureBlack);
//                    this.mMainMainHandsColor = getResources().getColor(R.color.PureBlack); //0xff787878;
//                    //this.mMainSecondsHandColor = 0xffc6514b;
//                    this.mMainDomBackColor = org.mAmbientDomAndAuxHandsColor;
//                    this.mMainDomFrontColor = getResources().getColor(R.color.PureBlack);
//                    this.mMainTickDigitColor = org.mAmbientTickDigitColor;
//                    this.mMainTickColor = org.mAmbientTicksColor;
//                    //
//                    this.mMainCalendarDialBackgroundColor = getResources().getColor(R.color.PureBlack);
//                    this.mMainCalendarDialDigitsColor = org.mAmbientTicksColor;
//                    this.mMainCalendarDialTicksColor = org.mAmbientTicksColor;
//                    //
//                    this.mMainSmallAuxDialBackgroundColor = getResources().getColor(R.color.PureBlack);
//                    this.mMainSmallAuxDialDigitsColor = org.mAmbientTicksColor;
//                    this.mMainSmallAuxDialTick1Color = org.mAmbientTicksColor;
//                    this.mMainSmallAuxDialTick2Color = getResources().getColor(R.color.PureRed);
//                    //
//                    this.mMainAuxHandWeekdayColor = org.mAmbientDomAndAuxHandsColor;
//                    //
//                    this.mMainAuxHandWearBattColor = org.mAmbientDomAndAuxHandsColor;
//                    //this.mMainAuxHandPhoneBattColor = 0xffc16161;
//                    this.mMainAuxHandMonthColor = org.mAmbientDomAndAuxHandsColor; //R.color.BBGreen;
//                }
//            }
//
//
//
//
//
//        } // class WatchAppearance


        WatchAppearance denseAppearance;// = new WatchAppearance(null);
        //WatchAppearance currentAppearance = denseAppearance;

        private AppPreferences mAppPreferences;




        private boolean unBundleConfig(WatchAppearance appearance, Bundle bundle, boolean initial) {

            //Log.i(TAG, "((( unBundleConfig, initial=" + initial);

            if (null == bundle) return false;

            long cfgTime;
            boolean retVal = true;

            retVal = appearance.unBundleConfig(bundle);

            cfgTime = bundle.getLong(ACommon.CFG_TIME);
            if (cfgTime == 0L) retVal = false;
            else appearanceModificationTimeMs = System.currentTimeMillis(); //cfgTime; // todo: а может текущее время???????


//            int i;
//            float f;
//            boolean boolv;
//            //ArrayList<Integer> colors;
//
//
//            i = bundle.getInt(ACommon.CFG_LAYOUT_INDEX, -1);
//            if (-1 == i) retVal = false;
//            else appearance.watchLayoutIndex = i;
//            i = bundle.getInt(ACommon.CFG_MAIN_HANDS_INDEX, -1);
//            if (-1 == i) retVal = false;
//            else appearance.watchMainHandsIndex = i;
//            i = bundle.getInt(ACommon.CFG_AUX_HANDS_INDEX, -1);
//            if (-1 == i) retVal = false;
//            else appearance.watchAuxHandsIndex = i;
//            i = bundle.getInt(ACommon.CFG_DOM_INDEX, -1);
//            if (-1 == i) retVal = false;
//            else appearance.watchDomIndex = i;
//            i = bundle.getInt(ACommon.CFG_BACKGROUND_INDEX, -1);
//            if (-1 == i) retVal = false;
//            else appearance.watchBackgroundIndex = i;
//
//            boolv = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
//            appearance.mShowHandheldBattery = boolv;
//            //
//            boolv = bundle.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
//            appearance.mShowRimAnimation = boolv;
//            // mShowHrDigitsRelief CFG_SHOW_HRDIGITS_RELIEF
//            boolv = bundle.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
//            appearance.setShowHrDigitsRelief(boolv);
//
//            boolv = bundle.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
//            appearance.mShowDialGradient = boolv;
//            i = bundle.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE);
//            appearance.dgInvert = i;
//            //
//            f = bundle.getFloat(ACommon.CFG_DG_FIRST_STOP, -1f);
//            if (-1f == f) retVal = false;
//            else appearance.dgFirstStop = f;
//            f = bundle.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP, -1f);
//            if (-1f == f) retVal = false;
//            else appearance.dgHalfEdgeStop = f;
//            i = bundle.getInt(ACommon.CFG_DG_EDGE_ALPHA, -1);
//            if (-1 == i) retVal = false;
//            else appearance.dgEdgeAlpha = i;
//            //
//            f = bundle.getFloat(ACommon.CFG_DG_FIRST_STOP_1, -1f);
//            if (-1f == f) retVal = false;
//            else appearance.dgFirstStop1 = f;
//            f = bundle.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, -1f);
//            if (-1f == f) retVal = false;
//            else appearance.dgHalfEdgeStop1 = f;
//            i = bundle.getInt(ACommon.CFG_DG_EDGE_ALPHA_1, -1);
//            if (-1 == i) retVal = false;
//            else appearance.dgEdgeAlpha1 = i;
//            //
//            i = bundle.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
//            appearance.mAuxBevelColor = i;
//
//
//
//
//
//            appearance.mMainDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DIGITS, 0xffffffff);
//            appearance.mMainHourHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_HOURHAND, 0xffffffff);
//            appearance.mMainMinuteHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, 0xffffffff);
//            appearance.mMainBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827);
//            appearance.mMainMainHandsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, 0xfff0f0d2);
//            appearance.mMainSecondsHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, 0xffc6514b);
//            appearance.mMainDomBackColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DOMBACK, 0xffe46f69);
//            appearance.mMainDomFrontColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, 0xdcf0f0f0);
//            appearance.mMainTickColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff);
//            appearance.mMainTickDigitColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, 0xc8ffffff);
//            appearance.mMainCalendarDialBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, 0xff252827);
//            appearance.mMainCalendarDialDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, 0xffffffff);
//            appearance.mMainCalendarDialTicksColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, 0xffffffff);
//            appearance.mMainSmallAuxDialBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, 0xff252827);
//            appearance.mMainSmallAuxDialDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, 0xc8ffffff);
//            appearance.mMainSmallAuxDialTick1Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, 0xc8ffffff);
//            appearance.mMainSmallAuxDialTick2Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, 0xc8ff0000);
//            //
//            appearance.mMainAuxHandWeekdayColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, 0xffc16161);
//            //
//            appearance.mMainAuxHandWearBattColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, 0xffffffff);
//            appearance.mMainAuxHandPhoneBattColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, 0xffc16161);
//            appearance.mMainAuxHandMonthColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, 0xffc16161);
//            //
//            appearance.mAmbientDigitsColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, 0xFF9DC775);
//            appearance.mAmbientHourHandColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, 0xFF9DC775);
//            appearance.mAmbientMinuteHandColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, 0xFF9DC775);
//            appearance.mAmbientTicksColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878);
//            appearance.mAmbientTickDigitColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, 0xC8787878);
//            appearance.mAmbientDomAndAuxHandsColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898);
//            //
//            appearance.mAmbientDecorUpperColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DECORUPPER, 0xFF9DC775);
//            appearance.mMainDecorUpperColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DECORUPPER, 0xffffffff);
//
//
//
//
//
//
////            colors = bundle.getIntegerArrayList(ACommon.CFG_COLORS);
////            if (null != colors) {
//////                retVal = false;
//////            } else if (colors.size() != ACommon.NUM_CFG_COLORS) {
//////                retVal = false;
//////                //todo: конфиг от другой версии!!!
//////            } else {
////                appearance.mMainDigitsColor = colors.get(ACommon.CFG_MAIN_DIGITS_COLOR) | 0xff000000;
////                appearance.mMainHourHandColor = colors.get(ACommon.CFG_MAIN_HOURHAND_COLOR);
////                appearance.mMainMinuteHandColor = colors.get(ACommon.CFG_MAIN_MINUTEHAND_COLOR);
////                appearance.mMainBackgroundColor = colors.get(ACommon.CFG_MAIN_BACKGROUND_COLOR) | 0xff000000;
////                appearance.mMainMainHandsColor = colors.get(ACommon.CFG_MAIN_MAINHANDS_COLOR) | 0xff000000;
////                appearance.mMainSecondsHandColor = colors.get(ACommon.CFG_MAIN_SECONDSHAND_COLOR) | 0xff000000;
////                appearance.mMainDomBackColor = colors.get(ACommon.CFG_MAIN_DOMBACK_COLOR) | 0xff000000;
////                appearance.mMainDomFrontColor = colors.get(ACommon.CFG_MAIN_DOMFRONT_COLOR);
////                //
////                appearance.mMainTickColor = colors.get(ACommon.CFG_MAIN_TICK_COLOR);
////                //if (initial == false)
////                mTickPaint.setColor(appearance.mMainTickColor);
////                //Log.i(TAG, "((( mTickPaint #3 color=" + mTickPaint.getColor());
////                appearance.mMainTickDigitColor = colors.get(ACommon.CFG_MAIN_TICKDIGIT_COLOR);
////                //if (initial == false)
////                mTickDigitPaint.setColor(appearance.mMainTickDigitColor);
////                //
////                appearance.mMainCalendarDialBackgroundColor = colors.get(ACommon.CFG_MAIN_BIGAUX_BACKGROUND_COLOR) | 0xff000000;
////                appearance.mMainCalendarDialDigitsColor = colors.get(ACommon.CFG_MAIN_BIGAUX_DIGITS_COLOR);
////                appearance.mMainCalendarDialTicksColor = colors.get(ACommon.CFG_MAIN_BIGAUX_TICKS_COLOR);
////                //
////                appearance.mMainSmallAuxDialBackgroundColor = colors.get(ACommon.CFG_MAIN_SMALLAUX_BACKGROUND_COLOR) | 0xff000000;
////                appearance.mMainSmallAuxDialDigitsColor = colors.get(ACommon.CFG_MAIN_SMALLAUX_DIGITS_COLOR);
////                appearance.mMainSmallAuxDialTick1Color = colors.get(ACommon.CFG_MAIN_SMALLAUX_TICKS1_COLOR);
////                appearance.mMainSmallAuxDialTick2Color = colors.get(ACommon.CFG_MAIN_SMALLAUX_TICKS2_COLOR);
////                //
////                appearance.mMainAuxHandWeekdayColor = colors.get(ACommon.CFG_MAIN_AUXHANDS_WEEKDAY_COLOR) | 0xff000000;
////                //
////                appearance.mMainAuxHandWearBattColor = colors.get(ACommon.CFG_MAIN_AUXHANDS_WEARBATT_COLOR) | 0xff000000;
////                appearance.mMainAuxHandPhoneBattColor = colors.get(ACommon.CFG_MAIN_AUXHANDS_PHONEBATT_COLOR) | 0xff000000;
////                appearance.mMainAuxHandMonthColor = colors.get(ACommon.CFG_MAIN_AUXHANDS_MONTH_COLOR) | 0xff000000;
////                //
////                appearance.mAmbientDigitsColor = colors.get(ACommon.CFG_AMBIENT_DIGITS_COLOR);// | 0xff000000;
////                appearance.mAmbientHourHandColor = colors.get(ACommon.CFG_AMBIENT_HOURHAND_COLOR);
////                appearance.mAmbientMinuteHandColor = colors.get(ACommon.CFG_AMBIENT_MINUTEHAND_COLOR);
////                appearance.mAmbientTicksColor = colors.get(ACommon.CFG_AMBIENT_TICK_COLOR);
////                appearance.mAmbientTickDigitColor = colors.get(ACommon.CFG_AMBIENT_TICKDIGIT_COLOR);
////                appearance.mAmbientDomAndAuxHandsColor = colors.get(ACommon.CFG_AMBIENT_DOM_AUXHANDS_COLOR) | 0xff000000;
////            }
//


            if (initial == false) setAppearanceColors(appearance);
            mTickPaint.setColor(appearance.mMainTickColor);
            mTickDigitPaint.setColor(appearance.mMainTickDigitColor);

            return retVal;
        } // unBundleConfig

        private void setAppearanceColors(WatchAppearance appearance) {
            //Log.i(TAG, "((( setAppearanceColors");
            changeHourHandDecorColor(appearance.mMainHourHandColor, false);
            changeHourHandDecorColor(appearance.mAmbientHourHandColor, true);
            changeUpperDecorColor(appearance.mMainDecorUpperColor, false);
            changeUpperDecorColor(appearance.mAmbientDecorUpperColor, true);
            changeMinuteHandDecorColor(appearance.mMainMinuteHandColor, false);
            changeMinuteHandDecorColor(appearance.mAmbientMinuteHandColor, true);
            changeBackgroundColor(appearance);
            changeMainHandsOutlineColor(appearance.mMainMainHandsColor);
            changeSecondsHandColor(appearance.mMainSecondsHandColor);
            changeBigAuxDialColor(appearance);
            changeSmallAuxDialColor(appearance);
            changeAuxHandWeekdayColor(appearance.mMainAuxHandWeekdayColor, false);
//            changeAuxHandWeekdayColor(appearance.mAmbientDomAndAuxHandsColor, true);
            changeAuxHandWeekdayColor(appearance.mAmbientAuxHandsColor, true);
            changeAuxHandWearBattColor(appearance.mMainAuxHandWearBattColor, false);
//            changeAuxHandWearBattColor(appearance.mAmbientDomAndAuxHandsColor, true);
            changeAuxHandWearBattColor(appearance.mAmbientAuxHandsColor, true);
            changeAuxHandPhoneBattColor(appearance.mMainAuxHandPhoneBattColor, false);
            //changeAuxHandPhoneBattColor(appearance.mAmbientDomAndAuxHandsColor, true);
            //changeAuxHandPhoneBattColor(appearance.mAmbientAuxHandsColor, true);
            changeAuxHandMonthColor(appearance.mMainAuxHandMonthColor, false);
//            changeAuxHandMonthColor(appearance.mAmbientDomAndAuxHandsColor, true);
            changeAuxHandMonthColor(appearance.mAmbientAuxHandsColor, true);
        }
//        private void setAppearanceColors(WatchAppearance appearance, boolean ambient) {
//            Log.i(TAG, "((( setAppearanceColors, ambient=" + ambient);
//            if (!ambient) changeBackgroundColor(appearance);
//            if (!ambient) changeBigAuxDialColor(appearance);
//            if (!ambient) changeSmallAuxDialColor(appearance);
//            if (!ambient) changeMainHandsOutlineColor(appearance.mMainMainHandsColor);
//            if (!ambient) changeHourHandDecorColor(appearance.mMainHourHandColor, false);
//            if (!ambient) changeUpperDecorColor(appearance.mMainHourHandColor, false);
//            if (!ambient) changeMinuteHandDecorColor(appearance.mMainMinuteHandColor, false);
//            if (!ambient) changeSecondsHandColor(appearance.mMainSecondsHandColor);
//            mTickPaint.setColor(appearance.mMainTickColor);
//            //Log.i(TAG, "((( mTickPaint #4 color=" + mTickPaint.getColor());
//            mTickDigitPaint.setColor(appearance.mMainTickDigitColor);
//            //
//            changeAuxHandWeekdayColor(appearance.mMainAuxHandWeekdayColor);
//            //
//            changeAuxHandWearBattColor(appearance.mMainAuxHandWearBattColor);
//            if (!ambient) changeAuxHandPhoneBattColor(appearance.mMainAuxHandPhoneBattColor);
//            changeAuxHandMonthColor(appearance.mMainAuxHandMonthColor);
//        }
        private void switchAppearance(boolean toAmbient) {
            //Log.i(TAG, "((((( switchAppearance, toAmbient=" + toAmbient + ", !!! mVars(" + (mVars != null) + ")");
            //
            if (null == mVars) {
                // todo: может быть вызван ДО первой отрисовки, когда ещё нет никаких drawAssets !!!
                return;
            }
            //
            if (toAmbient == true) {
                //currentAppearance = new WatchAppearance(denseAppearance);
//                currentAppearance = denseAppearance;
                // setup colors for ambient mode
                //setAppearanceColors(currentAppearance, true);
            } else {
//                currentAppearance = denseAppearance;
                // restore colors for dense mode
                //setAppearanceColors(denseAppearance, true);
            }
        } // switchAppearance

        private Bundle bundleConfig(WatchAppearance appearance) {

            Bundle bundle = appearance.bundleConfig(appearanceModificationTimeMs);

//            Bundle bundle = new Bundle();
//            bundle.putLong(ACommon.CFG_TIME, appearanceModificationTimeMs);
//            bundle.putInt(ACommon.CFG_LAYOUT_INDEX, appearance.watchLayoutIndex);                     //
//            bundle.putInt(ACommon.CFG_MAIN_HANDS_INDEX, appearance.watchMainHandsIndex);              //
//            bundle.putInt(ACommon.CFG_AUX_HANDS_INDEX, appearance.watchAuxHandsIndex);                //
//            bundle.putInt(ACommon.CFG_DOM_INDEX, appearance.watchDomIndex);                           //
//            bundle.putInt(ACommon.CFG_BACKGROUND_INDEX, appearance.watchBackgroundIndex);             //
//            //
//            bundle.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, appearance.mShowHandheldBattery);    //
//            bundle.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, appearance.mShowRimAnimation);          //
//            bundle.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, appearance.isShowHrDigitsRelief());      //
//            //
//            bundle.putBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, appearance.mShowDialGradient);
//            bundle.putInt(ACommon.CFG_INVERT_GRADIENT, appearance.dgInvert);
//            bundle.putFloat(ACommon.CFG_DG_FIRST_STOP, appearance.dgFirstStop);
//            bundle.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP, appearance.dgHalfEdgeStop);
//            bundle.putInt(ACommon.CFG_DG_EDGE_ALPHA, appearance.dgEdgeAlpha);
//            bundle.putFloat(ACommon.CFG_DG_FIRST_STOP_1, appearance.dgFirstStop1);
//            bundle.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, appearance.dgHalfEdgeStop1);
//            bundle.putInt(ACommon.CFG_DG_EDGE_ALPHA_1, appearance.dgEdgeAlpha1);
//            //
//            bundle.putInt(ACommon.CFG_AUX_BEVEL_COLOR, appearance.mAuxBevelColor);
//            //
//
//
//
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_DIGITS, appearance.mMainDigitsColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_HOURHAND, appearance.mMainHourHandColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, appearance.mMainMinuteHandColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, appearance.mMainBackgroundColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, appearance.mMainMainHandsColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, appearance.mMainSecondsHandColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_DOMBACK, appearance.mMainDomBackColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, appearance.mMainDomFrontColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_TICK, appearance.mMainTickColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, appearance.mMainTickDigitColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, appearance.mMainCalendarDialBackgroundColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, appearance.mMainCalendarDialDigitsColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, appearance.mMainCalendarDialTicksColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, appearance.mMainSmallAuxDialBackgroundColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, appearance.mMainSmallAuxDialDigitsColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, appearance.mMainSmallAuxDialTick1Color);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, appearance.mMainSmallAuxDialTick2Color);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, appearance.mMainAuxHandWeekdayColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, appearance.mMainAuxHandWearBattColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, appearance.mMainAuxHandPhoneBattColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, appearance.mMainAuxHandMonthColor);
//            //
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, appearance.mAmbientDigitsColor);
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, appearance.mAmbientHourHandColor);
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, appearance.mAmbientMinuteHandColor);
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TICK, appearance.mAmbientTicksColor);
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, appearance.mAmbientTickDigitColor);
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, appearance.mAmbientDomAndAuxHandsColor);
//
//            bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DECORUPPER, appearance.mAmbientDecorUpperColor);
//            bundle.putInt(ACommon.CFG_COLOR_MAIN_DECORUPPER, appearance.mMainDecorUpperColor);
//
//
////            ArrayList<Integer> colors = new ArrayList<>(ACommon.NUM_CFG_COLORS);
////            //
////            colors.add(ACommon.CFG_MAIN_DIGITS_COLOR, appearance.mMainDigitsColor);
////            colors.add(ACommon.CFG_MAIN_HOURHAND_COLOR, appearance.mMainHourHandColor);
////            colors.add(ACommon.CFG_MAIN_MINUTEHAND_COLOR, appearance.mMainMinuteHandColor);
////            colors.add(ACommon.CFG_MAIN_BACKGROUND_COLOR, appearance.mMainBackgroundColor);
////            colors.add(ACommon.CFG_MAIN_MAINHANDS_COLOR, appearance.mMainMainHandsColor);
////            colors.add(ACommon.CFG_MAIN_SECONDSHAND_COLOR, appearance.mMainSecondsHandColor);
////            colors.add(ACommon.CFG_MAIN_DOMBACK_COLOR, appearance.mMainDomBackColor);
////            colors.add(ACommon.CFG_MAIN_DOMFRONT_COLOR, appearance.mMainDomFrontColor);
////            colors.add(ACommon.CFG_MAIN_TICK_COLOR, appearance.mMainTickColor);
////            colors.add(ACommon.CFG_MAIN_TICKDIGIT_COLOR, appearance.mMainTickDigitColor);
////            colors.add(ACommon.CFG_MAIN_BIGAUX_BACKGROUND_COLOR, appearance.mMainCalendarDialBackgroundColor);
////            colors.add(ACommon.CFG_MAIN_BIGAUX_DIGITS_COLOR, appearance.mMainCalendarDialDigitsColor);
////            colors.add(ACommon.CFG_MAIN_BIGAUX_TICKS_COLOR, appearance.mMainCalendarDialTicksColor);
////            colors.add(ACommon.CFG_MAIN_SMALLAUX_BACKGROUND_COLOR, appearance.mMainSmallAuxDialBackgroundColor);
////            colors.add(ACommon.CFG_MAIN_SMALLAUX_DIGITS_COLOR, appearance.mMainSmallAuxDialDigitsColor);
////            colors.add(ACommon.CFG_MAIN_SMALLAUX_TICKS1_COLOR, appearance.mMainSmallAuxDialTick1Color);
////            colors.add(ACommon.CFG_MAIN_SMALLAUX_TICKS2_COLOR, appearance.mMainSmallAuxDialTick2Color);
////            colors.add(ACommon.CFG_MAIN_AUXHANDS_WEEKDAY_COLOR, appearance.mMainAuxHandWeekdayColor);
////            colors.add(ACommon.CFG_MAIN_AUXHANDS_WEARBATT_COLOR, appearance.mMainAuxHandWearBattColor);
////            colors.add(ACommon.CFG_MAIN_AUXHANDS_PHONEBATT_COLOR, appearance.mMainAuxHandPhoneBattColor);
////            colors.add(ACommon.CFG_MAIN_AUXHANDS_MONTH_COLOR, appearance.mMainAuxHandMonthColor);
////            colors.add(ACommon.CFG_AMBIENT_DIGITS_COLOR, appearance.mAmbientDigitsColor);
////            colors.add(ACommon.CFG_AMBIENT_HOURHAND_COLOR, appearance.mAmbientHourHandColor);
////            colors.add(ACommon.CFG_AMBIENT_MINUTEHAND_COLOR, appearance.mAmbientMinuteHandColor);
////            colors.add(ACommon.CFG_AMBIENT_TICK_COLOR, appearance.mAmbientTicksColor);
////            colors.add(ACommon.CFG_AMBIENT_TICKDIGIT_COLOR, appearance.mAmbientTickDigitColor);
////            colors.add(ACommon.CFG_AMBIENT_DOM_AUXHANDS_COLOR, appearance.mAmbientDomAndAuxHandsColor);
////            //
////            bundle.putIntegerArrayList(ACommon.CFG_COLORS, colors);

            return bundle;
        } // bundleConfig

        private void createConfigFile(boolean sendToHandheld) {
            Bundle bundle = bundleConfig(denseAppearance);
            //Log.i(TAG, "*** createConfigFile, config=" + bundle);
            if (null == bundle) return;
            long ctime = System.currentTimeMillis();
            bundle.putLong(ACommon.CFG_TIME, ctime);
            DataMap dataMap = DataMap.fromBundle(bundle);
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CREATE_CONFIG);
            dataMap.putLong(ACommon.KEY_TIME, ctime);
            //Asset asset = createAssetFromBitmap(frame);
            //dataMap.putAsset(ACommon.KEY_VALUE, asset);
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.FROM_WEAR_PATH, dataMap, mGoogleApiClient, null).start();
            if (true == sendToHandheld) {
//                DataMap dataMap2 = DataMap.fromBundle(bundle);
//                dataMap2.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_CONFIG);
//                //dataMap2.putLong(ACommon.KEY_TIME, ctime);
//                dataMap2.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                Log.i(TAG, "(((^ createConfigFile *** SEND, config=" + dataMap2);
//                //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap2).start();
//                new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMap2).start();
//                //ASYNC_REPLY_PATH
                sendCurrentConfig(bundle);
            }
        } // createConfigFile

        private void sendCurrentConfig(Bundle config) {
            if (config == null) config = bundleConfig(denseAppearance);
            DataMap dataMap2 = DataMap.fromBundle(config);
            dataMap2.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_CONFIG);
            //dataMap2.putLong(ACommon.KEY_TIME, ctime);
            dataMap2.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            //Log.i(TAG, "(((^ sendCurrentConfig *** SEND, config=" + dataMap2);
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap2).start();
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMap2).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH, dataMap2, mGoogleApiClient, null).start();
            //ASYNC_REPLY_PATH

        }

        private void sendPreferences() {
            Bundle prefBundle = AppPreferences.bundlePreferences(mAppPreferences);
            DataMap dataMap = DataMap.fromBundle(prefBundle);
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_PREFERENCES);
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());

//            AppPreferences.TzList tzArray[] = mAppPreferences.getTzArray();
//            int numTz = tzArray.length;
//            //Log.i(TAG, "#TZ SIZE = " + numTz);
//            String[] tzName = new String[numTz];
//            long[] tzOffs = new long[numTz];
//            long[] tzDst = new long[numTz];
//            for (int i=0; i < numTz; i++) {
//                //Log.i(TAG, "#TZ: " + tzArray[i].tzName + " " + tzArray[i].tzOffs + " " + tzArray[i].tzDst);
//                tzName[i] = tzArray[i].tzName;
//                tzOffs[i] = tzArray[i].tzOffs;
//                tzDst[i] = (tzArray[i].tzDst) ? 1 : 0;
//            }
//            dataMap.putStringArray(AppPreferences.KEY_TZARR_NAME, tzName);
//            dataMap.putLongArray(AppPreferences.KEY_TZARR_OFFS, tzOffs);
//            dataMap.putLongArray(AppPreferences.KEY_TZARR_DST, tzDst);

            //Log.i(TAG, "((( sendPreferences, dataMap=" + dataMap);
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH_3, dataMap).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH_3, dataMap, mGoogleApiClient, null).start();
        }
        private Runnable taskSendTzArray = new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "#TZ taskSendTzArray");
                sendTzArray();
            }
        };
        private void sendTzArray() {
            //Log.i(TAG, "#TZ sendTzArray");
            DataMap dataMap = new DataMap();
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_TZ_ARRAY);
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());

            AppPreferences.TzList tzArray[] = mAppPreferences.getTzArray();
            int numTz = tzArray.length;
            //Log.i(TAG, "#TZ SIZE = " + numTz);
            String[] tzName = new String[numTz];
            long[] tzOffs = new long[numTz];
            long[] tzDst = new long[numTz];
            for (int i=0; i < numTz; i++) {
                //Log.i(TAG, "#TZ: " + tzArray[i].tzName + " " + tzArray[i].tzOffs + " " + tzArray[i].tzDst);
                tzName[i] = tzArray[i].tzName;
                tzOffs[i] = tzArray[i].tzOffs;
                tzDst[i] = (tzArray[i].tzDst) ? 1 : 0;
            }
            dataMap.putStringArray(AppPreferences.KEY_TZARR_NAME, tzName);
            dataMap.putLongArray(AppPreferences.KEY_TZARR_OFFS, tzOffs);
            dataMap.putLongArray(AppPreferences.KEY_TZARR_DST, tzDst);

            //Log.i(TAG, "((( sendTzArray, dataMap=" + dataMap);
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH_4, dataMap).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH_4, dataMap, mGoogleApiClient, null).start();
        }

        private WatchAppearance resetConfigToDefaults(boolean initial) {
            //Log.i(TAG, "((( resetConfigToDefaults, initial=" + initial);

            WatchAppearance appearance = new WatchAppearance(null, getApplicationContext());

            if (initial == false) setAppearanceColors(appearance);
            mTickDigitPaint.setColor(appearance.mMainTickDigitColor);
            mTickPaint.setColor(appearance.mMainTickColor);
            //Log.i(TAG, "((( mTickPaint #0 color=" + mTickPaint.getColor());
            //
            appearanceModificationTimeMs = new Date().getTime();

            return appearance;
        }

        private void sendActualWatchfaceValues() {
            //Log.i(TAG, "((( sendActualWatchfaceValues CALLED");
            Bundle values = new Bundle();
            //
            values.putFloat(ACommon.WFVALUE_BURNIN_MARGIN, mVars.mBurnInMargin);
            values.putBoolean(ACommon.WFVALUE_RTL, Inscription.isRTL());
            values.putInt(ACommon.WFVALUE_SCREEN_WIDTH, mVars.width);
            values.putInt(ACommon.WFVALUE_SCREEN_HEIGHT, mVars.height);
            values.putFloat(ACommon.WFVALUE_SCREEN_CENTERX, mVars.centerX);
            values.putFloat(ACommon.WFVALUE_SCREEN_CENTERY, mVars.centerY);
            values.putFloat(ACommon.WFVALUE_SCREEN_RADIUS, mVars.mScreenRadius);
            values.putFloat(ACommon.WFVALUE_DIAL_RADIUS, mVars.mMainRadius);
            //
            if (null != watchLayouts[denseAppearance.watchLayoutIndex].auxA) {
                values.putFloat(ACommon.WFVALUE_AUXA_CX, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxA].cX);
                values.putFloat(ACommon.WFVALUE_AUXA_CY, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxA].cY);
                values.putFloat(ACommon.WFVALUE_AUXA_DIM, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxA].dimension);
            } else {
                values.putFloat(ACommon.WFVALUE_AUXA_CX, mVars.centerX);
                values.putFloat(ACommon.WFVALUE_AUXA_CY, mVars.centerY);
                values.putFloat(ACommon.WFVALUE_AUXA_DIM, mVars.mMainRadius);
            }
            if (null != watchLayouts[denseAppearance.watchLayoutIndex].auxB) {
                values.putFloat(ACommon.WFVALUE_AUXB_CX, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB].cX);
                values.putFloat(ACommon.WFVALUE_AUXB_CY, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB].cY);
                values.putFloat(ACommon.WFVALUE_AUXB_DIM, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB].dimension);
            } else {
                values.putFloat(ACommon.WFVALUE_AUXB_CX, mVars.centerX);
                values.putFloat(ACommon.WFVALUE_AUXB_CY, mVars.centerY);
                values.putFloat(ACommon.WFVALUE_AUXB_DIM, mVars.mMainRadius);
            }
            if (null != watchLayouts[denseAppearance.watchLayoutIndex].auxC) {
                values.putFloat(ACommon.WFVALUE_AUXC_CX, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC].cX);
                values.putFloat(ACommon.WFVALUE_AUXC_CY, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC].cY);
                values.putFloat(ACommon.WFVALUE_AUXC_DIM, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC].dimension);
            } else {
                values.putFloat(ACommon.WFVALUE_AUXC_CX, mVars.centerX);
                values.putFloat(ACommon.WFVALUE_AUXC_CY, mVars.centerY);
                values.putFloat(ACommon.WFVALUE_AUXC_DIM, mVars.mMainRadius);
            }
            //
            DataMap dataMapValues = DataMap.fromBundle(values);
            dataMapValues.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMapValues.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_WATCHFACE_VALUES);
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMapValues).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH_2, dataMapValues, mGoogleApiClient, null).start(); //mPeerId
        } // sendActualWatchfaceValues
        private void sendCurrentConfigForFile() {
            Bundle config = bundleConfig(denseAppearance);
            //Log.i(TAG, "(((^ sendCurrentConfigForFile, config=" + config);
            if (null != config) {
                DataMap dataMapConfig = DataMap.fromBundle(config);
                long cfg_time = System.currentTimeMillis();
                //dataMapConfig.putLong(ACommon.CFG_TIME, cfg_time);
                dataMapConfig.putLong(ACommon.KEY_TIME, cfg_time);
                dataMapConfig.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_CONFIG_FOR_FILE);
                //Log.i(TAG, "(((^ sendCurrentConfigForFile, config=" + dataMapConfig);
                //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMapConfig).start();
                //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMapConfig).start();
                new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH, dataMapConfig, mGoogleApiClient, null).start();
            }
        }
        //sendWearConfigBooleanOption(ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY, mShowHandheldBattery);
        private void sendWearConfigBooleanOption(int event, String key, boolean option) {
            //Log.i(TAG, "=== sendWearConfigBooleanOption=" + option);
            DataMap dataMap = new DataMap();
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, event);
            dataMap.putBoolean(key, option); //ACommon.CFG_SHOW_HANDHELD_BATTERY
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.FROM_WEAR_PATH, dataMap, mGoogleApiClient, null).start();
        }
        private void sendWearConfigBooleanToggle(int event, String key, boolean option) {
            //Log.i(TAG, "=== sendWearConfigBooleanToggle=" + option);
            DataMap dataMap = new DataMap();
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, event);
            dataMap.putBoolean(key, option); //ACommon.CFG_SHOW_HANDHELD_BATTERY
            //new SendThroughWearNetworkThread(ACommon.WEAR_TOGGLE_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.WEAR_TOGGLE_PATH, dataMap, mGoogleApiClient, null).start();
        }
        private void sendWearConfigIntegerOption(int event, String key, int option) {
            //Log.i(TAG, "=== sendWearConfigIntegerOption=" + option);
            DataMap dataMap = new DataMap();
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, event);
            dataMap.putInt(key, option);
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.FROM_WEAR_PATH, dataMap, mGoogleApiClient, null).start();
        }
        private void sendToggleLayoutRequest() {
            //Log.i(TAG, "#TOGGLE_LAYOUT sendToggleLayoutRequest()");
            DataMap dataMap = new DataMap();
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMap).start();
            //new SendThroughWearNetworkThread(ACommon.WEAR_TOGGLE_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.WEAR_TOGGLE_PATH, dataMap, mGoogleApiClient, null).start();
        }


        private void makeMainHandsBmpArray() {
//            Resources resources = AWearFaceService.this.getResources();
//            Drawable drawable;

            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                watchMainHandsBmp[i] = new WatchMainHandBmp();
            }
            
            
            
            
            // SECONDS HAND SET
//            drawable = resources.getDrawable(R.drawable.sc_hand);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSeconds = ((BitmapDrawable) drawable).getBitmap();
            //
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandBlack =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSeconds,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandBlack =
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightPathScaled[MHP_SECOND], false);
            //
//            drawable = resources.getDrawable(R.drawable.sc_hand_shd);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsShd = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandShadow =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightOutlinePathScaled[MHP_SECOND], null, false);

//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mSeconds = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSeconds;
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mSecondsHandBlack = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandBlack;
//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mSecondsShd = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsShd;
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mSecondsHandShadow = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandShadow;
            //
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mSecondsHandBlack = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandBlack;
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mSecondsHandShadow = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandShadow;
            //
            watchMainHandsBmp[ACommon.HANDS_ARROW].mSecondsHandBlack = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandBlack;
            watchMainHandsBmp[ACommon.HANDS_ARROW].mSecondsHandShadow = watchMainHandsBmp[ACommon.HANDS_STRAGHT].mSecondsHandShadow;




            // HOURS HAND SET
//            drawable = resources.getDrawable(R.drawable.hr_hand_straght);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHours = ((BitmapDrawable) drawable).getBitmap();
            //
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHoursHandBlack =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHours,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightPathScaled[MHP_HOUR], false);
            //
//            drawable = resources.getDrawable(R.drawable.hr_hand_straght_shd);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHoursShd = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHoursHandShadow =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mHoursShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightOutlinePathScaled[MHP_HOUR], null, false);

//            drawable = resources.getDrawable(R.drawable.hr_hand_rhomb);
//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mHours = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mHoursHandBlack =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_RHOMB].mHours,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandRhombusPathScaled[MHP_HOUR], false);
//            drawable = resources.getDrawable(R.drawable.hr_hand_rhomb_shd);
//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mHoursShd = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mHoursHandShadow =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_RHOMB].mHoursShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandRhombusOutlinePathScaled[MHP_HOUR], null, false);
            //
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mHoursHandBlack =
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandCurlHeadPathScaled[MHP_HOUR], false);
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mHoursHandShadow =
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandCurlHeadOutlinePathScaled[MHP_HOUR], null, false);
            //
            watchMainHandsBmp[ACommon.HANDS_ARROW].mHoursHandBlack =
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandArrowPathScaled[MHP_HOUR], false);
            watchMainHandsBmp[ACommon.HANDS_ARROW].mHoursHandShadow =
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandArrowOutlinePathScaled[MHP_HOUR], null, false);

            
            
            
            // MINUTES HAND SET
//            drawable = resources.getDrawable(R.drawable.mn_hand_straght);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutes = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutesHandBlack =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutes,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightPathScaled[MHP_MINUTE], false);
//            drawable = resources.getDrawable(R.drawable.mn_hand_straght_shd);
//            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutesShd = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutesHandShadow =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_STRAGHT].mMinutesShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandStraightOutlinePathScaled[MHP_MINUTE], null, false);

//            drawable = resources.getDrawable(R.drawable.mn_hand_rhomb);
//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutes = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutesHandBlack =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutes,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandRhombusPathScaled[MHP_MINUTE], false);
//            drawable = resources.getDrawable(R.drawable.mn_hand_rhomb_shd);
//            watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutesShd = ((BitmapDrawable) drawable).getBitmap();
            watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutesHandShadow =
//                    Bitmap.createScaledBitmap(watchMainHandsBmp[ACommon.HANDS_RHOMB].mMinutesShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandRhombusOutlinePathScaled[MHP_MINUTE], null, false);
            //
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mMinutesHandBlack =
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandCurlHeadPathScaled[MHP_MINUTE], false);
            watchMainHandsBmp[ACommon.HANDS_CURLHEAD].mMinutesHandShadow =
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandCurlHeadOutlinePathScaled[MHP_MINUTE], null, false);
            //
            watchMainHandsBmp[ACommon.HANDS_ARROW].mMinutesHandBlack =
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandArrowPathScaled[MHP_MINUTE], false);
            watchMainHandsBmp[ACommon.HANDS_ARROW].mMinutesHandShadow =
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mMainHandArrowOutlinePathScaled[MHP_MINUTE], null, false);

        } // makeMainHandsBmpArray


        private void makeAuxHandsBmpArray() {
//            Resources resources = AWearFaceService.this.getResources();
//            Drawable drawable;

            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                watchAuxHandsBmp[i] = new WatchAuxHandBmp();
            }

//            drawable = resources.getDrawable(R.drawable.wkday_hand);
//            watchAuxHandsBmp[0].mWeekday = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandWeekdayBlack =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWeekday,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWeekdayPathScaled[0], true);
//            drawable = resources.getDrawable(R.drawable.wkday_hand_shd);
//            watchAuxHandsBmp[0].mWeekdayShd = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandWeekdayShadow =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWeekdayShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWeekdayPathScaled[0], null, false);

//            drawable = resources.getDrawable(R.drawable.wrb_hand);
//            watchAuxHandsBmp[0].mWearBatt = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandWearBattBlack =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWearBatt,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWearbattPathScaled[0], true);
            watchAuxHandsBmp[0].mAuxHandMonthBlack =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWearBatt,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWearbattPathScaled[0], true);
//            drawable = resources.getDrawable(R.drawable.wrb_hand_shd);
//            watchAuxHandsBmp[0].mWearBattShd = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandWearBattShadow =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWearBattShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWearbattPathScaled[0], null, false);
            watchAuxHandsBmp[0].mAuxHandMonthShadow =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mWearBattShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandWearbattPathScaled[0], null, false);

//            drawable = resources.getDrawable(R.drawable.phb_hand);
//            watchAuxHandsBmp[0].mPhoneBatt = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandPhoneBattBlack =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mPhoneBatt,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeBitmapEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandPhonebattPathScaled[0], true);
//            drawable = resources.getDrawable(R.drawable.phb_hand_shd);
//            watchAuxHandsBmp[0].mPhoneBattShd = ((BitmapDrawable) drawable).getBitmap();
            watchAuxHandsBmp[0].mAuxHandPhoneBattShadow =
//                    Bitmap.createScaledBitmap(watchAuxHandsBmp[0].mPhoneBattShd,
//                            ACommon.ALL_HANDS_BMP_WIDTH, ACommon.ALL_HANDS_BMP_HEIGHT, true);
                    makeShadowEffScaled(mVars.width, mVars.handBitmapHeight, dialElements.mAuxHandPhonebattPathScaled[0], null, false);

        } // makeAuxHandsBmpArray

        private void createBackgroundGradientORG(WatchAppearance appearance) {
            //
            int color = appearance.mMainBackgroundColor;
            //int w = 320, h = 320;
            mCircleGradientS = Bitmap.createBitmap(mDisplayDimension, mDisplayDimension, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(mCircleGradientS);
            Paint paint = new Paint();

//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
//            canvas.drawBitmap(mFxPlateTexture, 0, 0, paint);

            //int[] colors = new int[]{0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000};
            //float[] stops = new float[]{0f,0.30f,0.45f,0.55f,0.70f,1.0f};
            int pixR = Color.red(color);
            int pixG = Color.green(color);
            int pixB = Color.blue(color);
            //
            //int edgeAlpha = 100; //128;
            //float firstStop = 0.75f;
            //float halfEdgeStop = 0.97f; //0.85f;
            //int centerOffsetY = 0; //30;

            if (appearance.dgInvert == ACommon.GD_INVERT_ALL || appearance.dgInvert == ACommon.GD_INVERT_MAIN_DIAL) {
                // from edge to center, square law
                float coeff, R;
//                int[] colors = new int[4];
//                float[] stops = new float[4];
//                //
//                stops[3] = 1.0f;
//                colors[3] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
//                //
//                stops[2] = appearance.dgHalfEdgeStop;
//                R = mDisplayDimension/2 * (1.0f - appearance.dgHalfEdgeStop);
//                coeff = 1.0f / (R * R);
//                colors[2] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[1] = appearance.dgFirstStop;
//                R = mDisplayDimension/2 * (1.0f - appearance.dgFirstStop);
//                coeff = 1.0f / (R * R);
//                colors[1] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[0] = 0f;
//                R = mDisplayDimension/2;
//                coeff = 1.0f / (R * R);
//                colors[0] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
                //
//                int[] colors = new int[6];
//                float[] stops = new float[6];
//                //
//                stops[5] = 1.0f;
//                colors[5] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
//                //
//                stops[4] = 0.98f;
//                //R = mDisplayDimension/2 * 0.02f;
//                coeff = stops[4] * stops[4];
//                colors[4] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[3] = 0.9f;
//                R = mDisplayDimension/2 * 0.1f;
//                coeff = stops[3] * stops[3];
//                colors[3] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[2] = 0.8f;
//                R = mDisplayDimension/2 * 0.2f;
//                coeff = stops[2] * stops[2];
//                colors[2] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[1] = 0.5f;
//                R = mDisplayDimension/2 * 0.5f;
//                coeff = stops[1] * stops[1];
//                colors[1] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
//                //
//                stops[0] = 0f;
//                R = mDisplayDimension/2;
//                coeff = 0.1f * 0.1f;
//                colors[0] = Color.argb((int) (appearance.dgEdgeAlpha * coeff), pixR, pixG, pixB);
                int[] colors = new int[4];
                float[] stops = new float[4];
                int alpha;
                int range = 255 - appearance.dgEdgeAlpha;
                colors[0] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
                stops[0] = 0f;
                //
                alpha = (int) (appearance.dgEdgeAlpha + range * (1.0f - appearance.dgFirstStop));
                colors[1] = Color.argb(alpha, pixR, pixG, pixB);
                stops[1] = appearance.dgFirstStop;
                //
                alpha = (int) (appearance.dgEdgeAlpha + range * (1.0f - appearance.dgHalfEdgeStop));
                colors[2] = Color.argb(alpha, pixR, pixG, pixB);
                stops[2] = appearance.dgHalfEdgeStop;
                //
                colors[3] = Color.argb(255, pixR, pixG, pixB);
                stops[3] = 1.0f;
                //
                Shader grdShader = new RadialGradient(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, colors, stops, Shader.TileMode.CLAMP);
                paint.setShader(grdShader);
                paint.setDither(true);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, paint);
            } else {
                // from center to edge
                int[] colors = new int[4];
                float[] stops = new float[4];
                colors[0] = Color.argb(255, pixR, pixG, pixB);
                stops[0] = 0f;
                colors[1] = Color.argb(250, pixR, pixG, pixB);
                stops[1] = appearance.dgFirstStop;
                colors[2] = Color.argb(255 - (255-appearance.dgEdgeAlpha)/2, pixR, pixG, pixB);
                stops[2] = appearance.dgHalfEdgeStop;
                colors[3] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
                stops[3] = 1.0f;
                //
                Shader grdShader = new RadialGradient(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, colors, stops, Shader.TileMode.CLAMP);
                paint.setShader(grdShader);
                paint.setDither(true);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, paint);
            }


            //
            //paint.setShader(null);
            //
//            mCircleGradientNoTransparent = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            //mCircleGradientNoTransparent.eraseColor(Color.BLACK);
            //canvas = new Canvas(mCircleGradientNoTransparent);
            //canvas.drawBitmap(mCircleGradientS, 0f, 0f, null);
        }
        //
        private void createBackgroundGradient(WatchAppearance appearance) {
            int color = appearance.mMainBackgroundColor;
            //mCircleGradientS = Bitmap.createBitmap(mDisplayDimension, mDisplayDimension, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            mCircleGradientS = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(mCircleGradientS);
            Paint paint = new Paint();
            int pixR = Color.red(color);
            int pixG = Color.green(color);
            int pixB = Color.blue(color);

            // from center to edge
            int[] colors = new int[4];
            float[] stops = new float[4];
            colors[0] = Color.argb(255, pixR, pixG, pixB);
            stops[0] = 0f;
            colors[1] = Color.argb(250, pixR, pixG, pixB);
            stops[1] = appearance.dgFirstStop;
            colors[2] = Color.argb(255 - (255-appearance.dgEdgeAlpha)/2, pixR, pixG, pixB);
            stops[2] = appearance.dgHalfEdgeStop;
            colors[3] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
            stops[3] = 1.0f;

            if (appearance.dgInvert == ACommon.GD_INVERT_ALL || appearance.dgInvert == ACommon.GD_INVERT_MAIN_DIAL) {
                // from edge to center
                colors[0] = Color.argb(appearance.dgEdgeAlpha, pixR, pixG, pixB);
                colors[1] = Color.argb(Math.min((int) (appearance.dgEdgeAlpha + 255 * 0.02f), 255), pixR, pixG, pixB);
                colors[3] = Color.argb(255, pixR, pixG, pixB);
            }

            float radius = mVars.mScreenRadius; // ??????? experiment: was mVars.mMainRadius
            //Shader grdShader = new RadialGradient(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, colors, stops, Shader.TileMode.CLAMP);
            Shader grdShader = new RadialGradient(mVars.centerX, mVars.centerY, radius, colors, stops, Shader.TileMode.CLAMP);
            paint.setShader(grdShader);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(mDisplayDimension/2, mDisplayDimension/2, mDisplayDimension/2, paint);
            canvas.drawCircle(mVars.centerX, mVars.centerY, radius, paint);
        }

        private void createBigAuxDialGradient(WatchAppearance appearance) {
            int color = appearance.mMainCalendarDialBackgroundColor;
            //int w = 320, h = 320;
            int d = mDisplayDimension;
            //mBigAuxDialGradient = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            mBigAuxDialGradient = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(mBigAuxDialGradient);
            Paint paint = new Paint();
            int pixR = Color.red(color);
            int pixG = Color.green(color);
            int pixB = Color.blue(color);
            int[] colors = new int[4];
            float[] stops = new float[4];
            //
            // from center to edge
            colors[0] = Color.argb(255, pixR, pixG, pixB);
            stops[0] = 0f;
            colors[1] = Color.argb(250, pixR, pixG, pixB);
            stops[1] = appearance.dgFirstStop1;
            colors[2] = Color.argb(255 - (255-appearance.dgEdgeAlpha1)/2, pixR, pixG, pixB);
            stops[2] = appearance.dgHalfEdgeStop1;
            colors[3] = Color.argb(appearance.dgEdgeAlpha1, pixR, pixG, pixB);
            stops[3] = 1.0f;
            if (appearance.dgInvert == ACommon.GD_INVERT_ALL || appearance.dgInvert == ACommon.GD_INVERT_AUX_DIALS) {
                // from edge to center
                colors[0] = Color.argb(appearance.dgEdgeAlpha1, pixR, pixG, pixB);
                colors[1] = Color.argb(Math.min((int) (appearance.dgEdgeAlpha1 + 255 * 0.02f), 255), pixR, pixG, pixB);
                colors[3] = Color.argb(255, pixR, pixG, pixB);
            }
            //
            //Shader grdShader = new RadialGradient(d/2, d/2, d/2, colors, stops, Shader.TileMode.CLAMP);
            Shader grdShader = new RadialGradient(mVars.centerX, mVars.centerY, mVars.mMainRadius, colors, stops, Shader.TileMode.CLAMP);
            paint.setShader(grdShader);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(d/2, d/2, d/2, paint);
            canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.mMainRadius, paint);
        }

        private void createSmallAuxDialGradient(WatchAppearance appearance) {
            int color = appearance.mMainSmallAuxDialBackgroundColor;
            //int w = 320, h = 320;
            int d = mDisplayDimension;
            //mSmallAuxDialGradient = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            mSmallAuxDialGradient = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(mSmallAuxDialGradient);
            Paint paint = new Paint();
            int pixR = Color.red(color);
            int pixG = Color.green(color);
            int pixB = Color.blue(color);
            int[] colors = new int[4];
            float[] stops = new float[4];
            //
            // from center to edge
            colors[0] = Color.argb(255, pixR, pixG, pixB);
            stops[0] = 0f;
            colors[1] = Color.argb(250, pixR, pixG, pixB);
            stops[1] = appearance.dgFirstStop1;
            colors[2] = Color.argb(255 - (255-appearance.dgEdgeAlpha1)/2, pixR, pixG, pixB);
            stops[2] = appearance.dgHalfEdgeStop1;
            colors[3] = Color.argb(appearance.dgEdgeAlpha1, pixR, pixG, pixB);
            stops[3] = 1.0f;
            if (appearance.dgInvert == ACommon.GD_INVERT_ALL || appearance.dgInvert == ACommon.GD_INVERT_AUX_DIALS) {
                // from edge to center
                colors[0] = Color.argb(appearance.dgEdgeAlpha1, pixR, pixG, pixB);
                colors[1] = Color.argb(Math.min((int) (appearance.dgEdgeAlpha1 + 255 * 0.02f), 255), pixR, pixG, pixB);
                colors[3] = Color.argb(255, pixR, pixG, pixB);
            }
            //
            //Shader grdShader = new RadialGradient(d/2, d/2, d/2, colors, stops, Shader.TileMode.CLAMP);
            Shader grdShader = new RadialGradient(mVars.centerX, mVars.centerY, mVars.mMainRadius, colors, stops, Shader.TileMode.CLAMP);
            paint.setShader(grdShader);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(d/2, d/2, d/2, paint);
            canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.mMainRadius, paint);
        }

        float rgbToY(int color) {
            return (float) (0.21 * Color.red(color) + 0.72 * Color.green(color) + 0.07 * Color.blue(color));
            //return (int) rgby;
        }

        private void changeBigAuxDialColor(WatchAppearance appearance) {
            createBigAuxDialGradient(appearance);
            int color = appearance.mMainCalendarDialBackgroundColor;
            Bitmap destBmp = mBigAuxDialGradientNoTransparent;
            //
            int bmpwidth = destBmp.getWidth();
            int bmpheight = destBmp.getHeight();
            int[] gradientRow = new int[bmpwidth];
            int[] auxRow = new int[bmpwidth];
            int[] fxRow = new int[bmpwidth];
            //
            float coeff;
            float fxk = appearance.mFxAuxDialTextureStrength; // (1f - fxk)
            for (int y=0; y<bmpheight; y++) {
                mBigAuxDialGradient.getPixels(gradientRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                mFxPlateTexture.getPixels(fxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                for (int x=0; x<bmpwidth; x++) {
                    if ((color & 0x00FFFFFF) != 0) {
                        if (appearance.mShowDialGradient == true) {
                            int grdA = Color.alpha(gradientRow[x]);
                            coeff = (float)(grdA) / 255f;
                        } else coeff = 1.0f;
                        //float coeff = ((float)(grdA) * 0.3f + rgbToY(color) * 0.7f) / 255f;
                        float r, g, b;
                        r = Color.red(color) * coeff * (1f - fxk) + (float)(Color.red(fxRow[x])) * coeff * fxk;
                        g = Color.green(color) * coeff * (1f - fxk) + (float)(Color.green(fxRow[x])) * coeff * fxk;
                        b = Color.blue(color) * coeff * (1f - fxk) + (float)(Color.blue(fxRow[x])) * coeff * fxk;
                        int newcolor = Color.rgb((int)(r), (int)(g), (int)(b));
                        auxRow[x] = (0xFF000000) | (newcolor & 0x00FFFFFF);
                    } else {
                        auxRow[x] = (0xFF000000) | (color & 0x00FFFFFF);
                    }
                }
                destBmp.setPixels(auxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
            }

            denseAppearance.mDialPlateReady = false;
        } // changeBigAuxDialColor
        private void changeSmallAuxDialColor(WatchAppearance appearance) {
            createSmallAuxDialGradient(appearance);
            int color = appearance.mMainSmallAuxDialBackgroundColor;
            Bitmap destBmp = mSmallAuxDialGradientNoTransparent;
            //
            int bmpwidth = destBmp.getWidth();
            int bmpheight = destBmp.getHeight();
            int[] gradientRow = new int[bmpwidth];
            int[] auxRow = new int[bmpwidth];
            int[] fxRow = new int[bmpwidth];
            float coeff;
            float fxk = appearance.mFxAuxDialTextureStrength; // (1f - fxk)
            //
            for (int y=0; y<bmpheight; y++) {
                mSmallAuxDialGradient.getPixels(gradientRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                mFxPlateTexture.getPixels(fxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                for (int x=0; x<bmpwidth; x++) {
                    if ((color & 0x00FFFFFF) != 0) {
                        if (appearance.mShowDialGradient == true) {
                            int grdA = Color.alpha(gradientRow[x]);
                            coeff = (float)(grdA) / 255f;
                        } else coeff = 1.0f;
                        //float coeff = ((float)(grdA) * 0.3f + rgbToY(color) * 0.7f) / 255f;
                        float r, g, b;
                        r = Color.red(color) * coeff * (1f - fxk) + (float)(Color.red(fxRow[x])) * coeff * fxk;
                        g = Color.green(color) * coeff * (1f - fxk) + (float)(Color.green(fxRow[x])) * coeff * fxk;
                        b = Color.blue(color) * coeff * (1f - fxk) + (float)(Color.blue(fxRow[x])) * coeff * fxk;
                        int newcolor = Color.rgb((int)(r), (int)(g), (int)(b));
                        auxRow[x] = (0xFF000000) | (newcolor & 0x00FFFFFF);
                    } else {
                        auxRow[x] = (0xFF000000) | (color & 0x00FFFFFF);
                    }
                }
                destBmp.setPixels(auxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
            }

            denseAppearance.mDialPlateReady = false;
        } // changeSmallAuxDialColor

        private void changeBackgroundColor(WatchAppearance appearance) {

            int color = appearance.mMainBackgroundColor;
            float fxk = appearance.mFxPlateTextureStrength; // (1f - fxk)

            createBackgroundGradient(appearance);

            //Log.i(TAG, "((((( changeBackgroundColor, color=" + color);

            for (int i=0; i< ACommon.NUM_BACKGROUNDS; i++) {
                if (watchBackgroundsBmp[i].mBackgroundColorized == null) continue;
                int bmpwidth = watchBackgroundsBmp[i].mBackgroundColorized.getWidth();
                int bmpheight = watchBackgroundsBmp[i].mBackgroundColorized.getHeight();
                int[] watchBmpRow = new int[bmpwidth];
                int[] gradientRow = new int[bmpwidth];
                int[] fxRow = new int[bmpwidth];
                int[] opaqueRow = new int[bmpwidth];
                int pixR = Color.red(color);
                int pixG = Color.green(color);
                int pixB = Color.blue(color);
//                Log.i(TAG, "§§§§§ R=" + pixR + ", G=" + pixG + ", B=" + pixB);
                float coeff;
                float r, g, b;
                int newcolor;
                for (int y=0; y<bmpheight; y++) {
                    mCircleGradientS.getPixels(gradientRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    //watchBackgroundsBmp[i].mBackgroundColorized.getPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    watchBackgroundsBmp[i].mBackgroundBlack.getPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    mFxPlateTexture.getPixels(fxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    for (int x=0; x<bmpwidth; x++) {
                        int grdA = Color.alpha(gradientRow[x]);
                        if ((color & 0x00FFFFFF) != 0) {
//                            if (appearance.mShowDialGradient == true) {
//                                int grdA = Color.alpha(gradientRow[x]);
//                                coeff = (float)(grdA) / 255f;
//                            } else {
//                                coeff = 1.0f;
//                            }
                            if (appearance.mShowDialGradient == true) {
                                coeff = (float)(grdA) / 255f;
                            } else {
//                                if (grdA == 0) coeff = 0.0f;
//                                else
                                    coeff = 1.0f;
                            }
                            r = pixR * coeff * (1f - fxk) + (float)(Color.red(fxRow[x])) * coeff * fxk;
                            g = pixG * coeff * (1f - fxk) + (float)(Color.green(fxRow[x])) * coeff * fxk;
                            b = pixB * coeff * (1f - fxk) + (float)(Color.blue(fxRow[x])) * coeff * fxk;
                            //
                            newcolor = Color.rgb((int)(r), (int)(g), (int)(b));
                            watchBmpRow[x] = (watchBmpRow[x] & 0xFF000000) | (newcolor & 0x00FFFFFF);
                            if (i == 0) opaqueRow[x] = (0xFF000000) | (newcolor & 0x00FFFFFF);
                        } else {
                            watchBmpRow[x] = (watchBmpRow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                            if (i == 0) opaqueRow[x] = (0xFF000000) | (color & 0x00FFFFFF);
                        }
//                        if (y == 160 && x > 210) {
//                            Log.i(TAG, "§§§§§ x: " + x + ", grdA=" + grdA + ", coeff=" + coeff + ", r=" + r + ", g=" + g + ", b=" + b);
//                        }
                    }
                    watchBackgroundsBmp[i].mBackgroundColorized.setPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    if (i == 0) mCircleGradientNoTransparent.setPixels(opaqueRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                }

                if (i == 0) { /*System.gc();*/ applyFxPlateRelief(mCircleGradientNoTransparent, true); /*System.gc();*/ }
                /*System.gc();*/ applyFxPlateRelief(watchBackgroundsBmp[i].mBackgroundColorized, false); /*System.gc();*/
            }

            denseAppearance.mDialPlateReady = false;
        } // changeBackgroundColor

        private void applyFxPlateRelief(Bitmap plateBitmap, boolean noTransparent) {

            Path pathMaxR = new Path(), pathMinR = new Path(), pathMinRplus = new Path(), pathMinRminus = new Path();
            float breakRadius = mVars.innerTickRadius - mVars.pixelDim(3.5f);
            pathMaxR.addCircle(mVars.centerX, mVars.centerY, mVars.mScreenRadius, Path.Direction.CW);
            pathMinR.addCircle(mVars.centerX, mVars.centerY, breakRadius, Path.Direction.CW);
            pathMinRminus.addCircle(mVars.centerX, mVars.centerY, breakRadius - mVars.pixelDim(0.2f), Path.Direction.CW);
            pathMinRplus.addCircle(mVars.centerX, mVars.centerY, breakRadius + mVars.pixelDim(0.2f), Path.Direction.CW);

            Path pathOutsideRing = new Path(), pathInsideCircle = new Path(pathMinR);
            pathOutsideRing.op(pathMaxR, pathMinR, Path.Op.DIFFERENCE); //pathMinR pathMinRplus pathMinRminus

            int colorAlpha = (int) (255f * 0.5f);
            float ringStops[] = new float[] {
                    0.0f,
                    0.25f,
                    1.0f,
            };
            int ringColors[] = new int[] {
                    Color.argb(colorAlpha, 32, 32, 32),
                    Color.argb(colorAlpha, 32, 32, 32),
                    Color.argb(255, 255, 255, 255), //colorAlpha
            };
            float circleStops[] = new float[] {
                    0.0f,
                    0.15f,
                    1.0f,
            };
            int circleColors[] = new int[] {
//                    Color.argb(colorAlpha, 250, 250, 250),
//                    Color.argb(colorAlpha, 242, 242, 242),
                    Color.argb(colorAlpha, 230, 230, 230),
                    Color.argb(colorAlpha, 222, 222, 222),
                    Color.argb(colorAlpha, 0, 0, 0), //96 127
            };
            Shader shaderRing = new RadialGradient(mVars.centerX, 0, mVars.height, ringColors, ringStops, Shader.TileMode.CLAMP);
            float cY = mVars.mScreenRadius * 0.5f;
            float radius = mVars.height; //mVars.innerTickRadius + mVars.mScreenRadius - cY;
            Shader shaderCircle = new RadialGradient(mVars.centerX, cY, radius, circleColors, circleStops, Shader.TileMode.CLAMP);



            //String timeStamp = String.valueOf(System.currentTimeMillis());

            Bitmap result = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);

            paint.setShader(shaderCircle);
            canvas.drawPath(pathMaxR, paint); //pathInsideCircle
            Bitmap working = blur(result, 25f);
            //if (noTransparent) ACommon.bmpToPicturesDir(AWearFaceService.this, working, "fxr_", "_blur", timeStamp);

//            canvas.drawColor(Color.TRANSPARENT);
//            canvas.save();
//            canvas.clipPath(pathInsideCircle);
//            canvas.drawBitmap(working, 0, 0, null);
//            canvas.restore();
//            // убираем лесенку от clipPath
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(2f);
//            canvas.drawPath(pathInsideCircle, paint);

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint.setShader(null);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(0f);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(pathMinRplus, paint); //pathInsideCircle
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(working, 0, 0, paint);
            //if (noTransparent) ACommon.bmpToPicturesDir(AWearFaceService.this, result, "fxr_", "_i1", timeStamp);

            paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            paint.setShader(shaderRing);
            paint.setStrokeWidth(0f);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(pathOutsideRing, paint);
            working = blur(result, 2f); // 1f
            //if (noTransparent) ACommon.bmpToPicturesDir(AWearFaceService.this, working, "fxr_", "_i2", timeStamp);

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            PorterDuffColorFilter colorFilter =
                    new PorterDuffColorFilter(denseAppearance.mMainBackgroundColor, PorterDuff.Mode.MULTIPLY);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(working, 0, 0, paint);
            //if (noTransparent) ACommon.bmpToPicturesDir(AWearFaceService.this, result, "fxr_", "_out", timeStamp);

            canvas.setBitmap(working);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(plateBitmap, 0, 0, null);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(result, 0, 0, paint);
            //ACommon.bmpToPicturesDir(AWearFaceService.this, working, "fxr_", "_out", timeStamp);

            canvas.setBitmap(plateBitmap);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            paint.setAlpha(denseAppearance.mFxPlateReliefStrength);
            canvas.drawBitmap(working, 0, 0, paint);
            //if (noTransparent) ACommon.bmpToPicturesDir(AWearFaceService.this, working, "fxr_", "_out", timeStamp);

        } // applyFxPlateRelief


        // new float[] {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f}; // /9: box blur
        // new float[] {0, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f}; // sharpen
        // new float[] {-1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f}; // edge detection
        // new float[] {0f, 20f, 0f, 20f, -59f, 20f, 1f, 13f, 0f}; // /7: fuzzy glass
        // new float[] {2f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f}; // emboss, divisor 2, offset 127
        // new float[] {1.0f, 2.0f, 1.0f, 2.0f, 4.0f, 2.0f, 1.0f, 2.0f, 1.0f}; // gaussian, divisor 16, offset 0
        //sharpen: 0.0,    -1.0,    0.0, -1.0,    5.0,    -1.0, 0.0,    -1.0,    0.0
        //edge: 0.0,    1.0,    0.0, 1.0,    -4.0,    1.0, 0.0,    1.0,    0.0
        //find edges: -1.0,    -1.0,    -1.0, -2.0,    8.0,    -1.0, -1.0,    -1.0,    -1.0
        // new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f}; // identity
        private Bitmap convolve3(Bitmap original, float[] coefficients) {
            Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

            RenderScript rs = RenderScript.create(AWearFaceService.this);

            Allocation allocIn = Allocation.createFromBitmap(rs, original);
            Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

            ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
            convolution.setInput(allocIn);
            convolution.setCoefficients(coefficients);
            convolution.forEach(allocOut);

            allocOut.copyTo(bitmap);
            rs.destroy();
            return bitmap;
        } // convolve3
        //
        private Bitmap convolve5(Bitmap original, float[] coefficients) {
            Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

            RenderScript rs = RenderScript.create(AWearFaceService.this);

            Allocation allocIn = Allocation.createFromBitmap(rs, original);
            Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

            ScriptIntrinsicConvolve5x5 convolution = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
            convolution.setInput(allocIn);
            convolution.setCoefficients(coefficients);
            //float test[] = new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f};
            //convolution.setCoefficients(test);
            convolution.forEach(allocOut);

            allocOut.copyTo(bitmap);
            rs.destroy();
            return bitmap;
        } // convolve5
        //
        private Bitmap blur(Bitmap original, float radius) {
            Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

            RenderScript rs = RenderScript.create(AWearFaceService.this);

            Allocation allocIn = Allocation.createFromBitmap(rs, original);
            Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blur.setInput(allocIn);
            blur.setRadius(radius);
            blur.forEach(allocOut);

            allocOut.copyTo(bitmap);
            rs.destroy();
            return bitmap;
        }
        //
        private Bitmap makeShadowEffScaled_ORG(int bitmapWidth, int bitmapHeight, Path outline, Path decor, boolean makeMutable) {
//            Shader shdBitmap = new BitmapShader(mVars.backgroundBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                    mTickPaint.getColor(), mTickPaint.getColor(), Shader.TileMode.CLAMP);
//            Shader shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
//            //
//            // draw frame around date cut hole
//            mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mTickPaint.setStrokeWidth(3.0f);
//            mTickPaint.setShader(shdSumm);
//            mTickPaint.setAntiAlias(true);
//            if (true) canvas.drawPath(dialElements.mDateCutPathsScaled[currentAppearance.watchBackgroundIndex], mTickPaint);
//            mTickPaint.setShader(null);
//
//
//            // experiment: draw shadow blur around digits
//            if (true) {
//                //mDigitsPaint.setColor(0xaf000000);
//                mDigitsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//                mDigitsPaint.setStrokeWidth(3f);
//                //
//                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                        Color.argb(127, 0, 0, 0), Color.argb(127, 0, 0, 0),
//                        Shader.TileMode.CLAMP);
//                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP); // GOOD!
//                mDigitsPaint.setShader(shdSumm);
//                //
//                BlurMaskFilter blurMaskFilter = new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL);
//                mDigitsPaint.setMaskFilter(blurMaskFilter);
//                //
//                canvas.drawPath(dialElements.mHourMarkPathsScaled[10], mDigitsPaint);
//            }

            if (null == outline) return null;

            Bitmap result, bmpRelaxed = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas cnvRelaxed = new Canvas(bmpRelaxed);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            paint.setColor(Color.argb(220, 0, 0, 0));
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//            paint.setStrokeWidth(1f);
            paint.setStyle(Paint.Style.FILL);
            //paint.setStrokeWidth(1f);
//            BlurMaskFilter blurMaskFilter = new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL);
//            paint.setMaskFilter(blurMaskFilter);
            cnvRelaxed.drawPath(outline, paint);

            result = bmpRelaxed.copy(Bitmap.Config.ARGB_8888, makeMutable);
            return blur(result, 7);
            //return result;

        } // makeShadowEffScaled

        private Bitmap makeShadowEffScaled(int bitmapWidth, int bitmapHeight, Path outline, Path decor, boolean makeMutable) {
//            Shader shdBitmap = new BitmapShader(mVars.backgroundBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                    mTickPaint.getColor(), mTickPaint.getColor(), Shader.TileMode.CLAMP);
//            Shader shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
//            //
//            // draw frame around date cut hole
//            mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mTickPaint.setStrokeWidth(3.0f);
//            mTickPaint.setShader(shdSumm);
//            mTickPaint.setAntiAlias(true);
//            if (true) canvas.drawPath(dialElements.mDateCutPathsScaled[currentAppearance.watchBackgroundIndex], mTickPaint);
//            mTickPaint.setShader(null);
//
//
//            // experiment: draw shadow blur around digits
//            if (true) {
//                //mDigitsPaint.setColor(0xaf000000);
//                mDigitsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//                mDigitsPaint.setStrokeWidth(3f);
//                //
//                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                        Color.argb(127, 0, 0, 0), Color.argb(127, 0, 0, 0),
//                        Shader.TileMode.CLAMP);
//                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP); // GOOD!
//                mDigitsPaint.setShader(shdSumm);
//                //
//                BlurMaskFilter blurMaskFilter = new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL);
//                mDigitsPaint.setMaskFilter(blurMaskFilter);
//                //
//                canvas.drawPath(dialElements.mHourMarkPathsScaled[10], mDigitsPaint);
//            }

            if (null == outline) return null;

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            if (null != decor) {
                path.addCircle(mVars.centerX, mVars.centerY, mVars.mMainRadius, Path.Direction.CW);
                path.addPath(decor);
            } else {
                path.addPath(outline);
            }

            float blurRadius;
            int color1 = Color.argb(220, 0, 0, 0), color2 = Color.argb(240, 0, 0, 0);
            Bitmap blurred, result = null, bmpRelaxed = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas cnvRelaxed = new Canvas(bmpRelaxed);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            if (null != decor) {
                paint.setColor(color2);
//                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStyle(Paint.Style.FILL);
//                paint.setStrokeWidth(1f);
                blurRadius = 7f;
            } else {
                paint.setColor(color1);
                paint.setStyle(Paint.Style.FILL);
                blurRadius = 7f;
            }

            cnvRelaxed.drawPath(path, paint);

            if (null != decor) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2f);
                cnvRelaxed.drawPath(decor, paint);
            }

            blurred = blur(bmpRelaxed/*.copy(Bitmap.Config.ARGB_8888, makeMutable)*/, blurRadius);

            if (null != decor) {
                Shader shdBlurred = new BitmapShader(blurred, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height, color2, color2, Shader.TileMode.CLAMP);
                Shader shdSumm = new ComposeShader(shdBlurred, shdColor, PorterDuff.Mode.SRC_ATOP);
                cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(shdSumm);
                    //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                cnvRelaxed.drawPath(outline, paint);
                result = bmpRelaxed;
                blurred.recycle(); blurred = null;
                //result = blurred;
            } else {
                result = blurred;
                bmpRelaxed.recycle(); bmpRelaxed = null;
            }

            return result;
        } // makeShadowEffScaled

        private Bitmap makeHourMarksShadowEffScaled(int bitmapWidth, int bitmapHeight, Path[] digits, int index, boolean makeMutable) {

            if (null == digits) return null;

            float blurRadius = 2f;
            Bitmap blurred, result, bmpRelaxed = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas cnvRelaxed = new Canvas(bmpRelaxed);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

//            public static final int DIAL_SINGLE_HOLE_VRT = 0;
//            public static final int DIAL_TRIPLE_HOLE_VRT = 1;
//            public static final int DIAL_SINGLE_HOLE_HRZ = 2;
//            public static final int DIAL_TRIPLE_HOLE_HRZ = 3;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.argb(127, 16, 16, 16));
//            int color1 = 0x7f000000 | (currentAppearance.mMainBackgroundColor & 0x00ffffff);
//            paint.setColor(color1);
            paint.setStrokeWidth(1.5f);

            for (int i=0; i<digits.length; i++) {

                if (null == digits[i]) continue;

                if (i == 3 && (index == ACommon.DIAL_SINGLE_HOLE_VRT || index == ACommon.DIAL_TRIPLE_HOLE_VRT)) continue;
                if (i == 6 && (index == ACommon.DIAL_SINGLE_HOLE_HRZ || index == ACommon.DIAL_TRIPLE_HOLE_HRZ)) continue;
                cnvRelaxed.drawPath(digits[i], paint);
            }



//            // filters
//            float[] boxBlur = new float[] {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f}; // /9: box blur
//            // new float[] {0, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f}; // sharpen
//            // new float[] {-1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f}; // edge detection
//            // new float[] {0f, 20f, 0f, 20f, -59f, 20f, 1f, 13f, 0f}; // /7: fuzzy glass
//            // new float[] {2f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f}; // emboss, divisor 2, offset 127
//            //float[] gaussian =  new float[] {1.0f, 2.0f, 1.0f, 2.0f, 4.0f, 2.0f, 1.0f, 2.0f, 1.0f}; // gaussian, divisor 16, offset 0
//            float[] gaussian1 =  new float[] {0.0f, 0.1667f, 0.0f,  0.1667f, 0.3333f, 0.1667f,  0.0f, 0.1667f, 0.0f};
//            //sharpen: 0.0,    -1.0,    0.0, -1.0,    5.0,    -1.0, 0.0,    -1.0,    0.0
//            //edge: 0.0,    1.0,    0.0, 1.0,    -4.0,    1.0, 0.0,    1.0,    0.0
//            //find edges: -1.0,    -1.0,    -1.0, -2.0,    8.0,    -1.0, -1.0,    -1.0,    -1.0
//            float[] identity = new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f}; // identity
//
//            blurred = convolve3(bmpRelaxed.copy(Bitmap.Config.ARGB_8888, makeMutable), gaussian1);
//            for (int i=0; i<3; i++) {
//                blurred = convolve3(blurred.copy(Bitmap.Config.ARGB_8888, makeMutable), gaussian1);
//            }

            blurred = blur(bmpRelaxed/*.copy(Bitmap.Config.ARGB_8888, makeMutable)*/, blurRadius);


            result = blurred;
            bmpRelaxed.recycle(); bmpRelaxed = null;





//            Path path = new Path();
//            path.setFillType(Path.FillType.EVEN_ODD);
//            if (null != decor) {
//                path.addCircle(mVars.centerX, mVars.centerY, mVars.mMainRadius, Path.Direction.CW);
//                path.addPath(decor);
//            } else {
//                path.addPath(outline);
//            }
//
//
//            if (null != decor) {
//                paint.setColor(color2);
////                paint.setStyle(Paint.Style.FILL_AND_STROKE);
//                paint.setStyle(Paint.Style.FILL);
////                paint.setStrokeWidth(1f);
//                blurRadius = 7f;
//            } else {
//                paint.setColor(color1);
//                paint.setStyle(Paint.Style.FILL);
//                blurRadius = 7f;
//            }
//
//            cnvRelaxed.drawPath(path, paint);
//
//            if (null != decor) {
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(2f);
//                cnvRelaxed.drawPath(decor, paint);
//            }
//
//            blurred = blur(bmpRelaxed.copy(Bitmap.Config.ARGB_8888, makeMutable), blurRadius);
//
//            if (null != decor) {
//                Shader shdBlurred = new BitmapShader(blurred, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height, color2, color2, Shader.TileMode.CLAMP);
//                Shader shdSumm = new ComposeShader(shdBlurred, shdColor, PorterDuff.Mode.SRC_ATOP);
//                cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                paint.setStyle(Paint.Style.FILL);
//                paint.setShader(shdSumm);
//                //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//                cnvRelaxed.drawPath(outline, paint);
//                result = bmpRelaxed;
//                blurred.recycle();
//                //result = blurred;
//            } else {
//                result = blurred;
//                bmpRelaxed.recycle();
//            }

            return result;
        } // makeHourMarksShadowEffScaled

        //
        private Bitmap makeBitmapEffScaled(int bitmapWidth, int bitmapHeight, Path path, boolean makeMutable) {
            Bitmap result, bmpRelaxed = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas cnvRelaxed = new Canvas(bmpRelaxed);
            Paint paint = new Paint();
//
//            //watchBackgroundsBmp[i].mBackground
//            Bitmap srcBitmap = watchBackgroundsBmp[index].mBackground;
//
            //bmpRelaxed = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            //cnvRelaxed = new Canvas(bmpRelaxed);
            //cnvRelaxed.
            //paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
//            //paint.setAlpha(255);
//
////            bmpScaled = Bitmap.createScaledBitmap(srcBitmap,
////                    ((int) (mVars.width * mVars.mScaleEffective)), ((int) (mVars.height * mVars.mScaleEffective)), true);
//
//            Log.i(TAG, "((((( makeBitmapEffScaled, mVars.mScaleEffective=" + mVars.mScaleEffective +
//                    ", width=" + ((int) (mVars.width * mVars.mScaleEffective)) +
//                    ", height=" + ((int) (mVars.height * mVars.mScaleEffective)));
//
//            bmpScaled = makeScaledBitmap(srcBitmap,
//                    ((int) (mVars.width * mVars.mScaleEffective)), ((int) (mVars.height * mVars.mScaleEffective)));
//
            //cnvRelaxed.drawColor(0x00000000);
            cnvRelaxed.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//            //cnvRelaxed.drawBitmap(bmpScaled, mVars.mBurnInMargin, mVars.mBurnInMargin, paint);
//
//
//
//            Path tmpPath = new Path();
//            Matrix matrix = new Matrix();
//            int scaledWidth = ((int) (mVars.width * mVars.mScaleEffective));
//            int scaledHeight = ((int) (mVars.height * mVars.mScaleEffective));
//            float ratioX = scaledWidth / (float) srcBitmap.getWidth();
//            float ratioY = scaledHeight / (float) srcBitmap.getHeight();
//            float middleX = scaledWidth / 2.0f;
//            float middleY = scaledHeight / 2.0f;
//            //matrix.postScale(ratioX, ratioY, middleX, middleY);
//            //matrix.postScale(ratioX, ratioY, mVars.width / 2f, mVars.height / 2f);
//            matrix.postScale(ratioX, ratioY);
//            matrix.postTranslate(mVars.mBurnInMargin, mVars.mBurnInMargin);
//            //matrix.postS
//            dialElements.mDialPlatesPaths[index].transform(matrix, tmpPath);
//
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            //cnvRelaxed.drawPath(tmpPath, paint);
            cnvRelaxed.drawPath(path, paint);

            result = bmpRelaxed; //bmpRelaxed.copy(Bitmap.Config.ARGB_8888, makeMutable);
            return result;
        } // makeBitmapEffScaled
        //
//        private Bitmap makeScaledBitmap(Bitmap bmpOriginal, int scaledWidth, int scaledHeight) {
//            Bitmap bmpScaled;
//            //int scaledWidth, scaledHeight;
//
////            bmpScaled = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
//
//            float ratioX = scaledWidth / (float) bmpOriginal.getWidth();
//            float ratioY = scaledHeight / (float) bmpOriginal.getHeight();
//            float middleX = scaledWidth / 2.0f;
//            float middleY = scaledHeight / 2.0f;
//
//            Log.i(TAG, "((((( makeScaledBitmap, bmpOriginal.getWidth=" + bmpOriginal.getWidth()
//                    + ", bmpOriginal.getHeight=" + bmpOriginal.getHeight() + ", ratioX=" + ratioX + ", ratioY=" + ratioY);
//
////            Matrix scaleMatrix = new Matrix();
////            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
////
////            Canvas canvas = new Canvas(bmpScaled);
////            canvas.setMatrix(scaleMatrix);
////            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
////            canvas.drawBitmap(bmpOriginal,
////                    middleX - bmpOriginal.getWidth() / 2,
////                    middleY - bmpOriginal.getHeight() / 2,
////                    new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG|Paint.ANTI_ALIAS_FLAG));
//
//            Matrix matrix = new Matrix();
//            matrix.postScale(ratioX, ratioY);
//            Bitmap scaledBitmap = Bitmap.createBitmap(bmpOriginal, 0, 0,
//                    bmpOriginal.getWidth(), bmpOriginal.getHeight(), matrix, true);
//
//
//            return scaledBitmap; //bmpScaled;
//        } // makeScaledBitmap
        //
        private void createBackgroundSM(int width, int height) {
            for (int i=0; i< ACommon.NUM_BACKGROUNDS; i++) { //if (denseAppearance.watchBackgroundIndex != i) continue;
                if (watchBackgroundsBmp[i].mDomShadowS != null ) { watchBackgroundsBmp[i].mDomShadowS.recycle(); watchBackgroundsBmp[i].mDomShadowS = null; /*System.gc();*/ }
                watchBackgroundsBmp[i].mDomShadowS =
                        makeShadowEffScaled(mVars.width, mVars.height,
                                dialElements.mDateCutOutlinePathsScaled[i], dialElements.mDateCutPathsScaled[i], false); /*System.gc();*/
                //Bitmap.createScaledBitmap(watchBackgroundsBmp[i].mDomShadow, width, height, true);
//                if (watchBackgroundsBmp[i].mDomShadowS == null
//                        || watchBackgroundsBmp[i].mDomShadowS.getWidth() != width
//                        || watchBackgroundsBmp[i].mDomShadowS.getHeight() != height) {
//                    watchBackgroundsBmp[i].mDomShadowS =
//                            Bitmap.createScaledBitmap(watchBackgroundsBmp[i].mDomShadow, width, height, true);
//                }
                //
                if (watchBackgroundsBmp[i].mBackgroundBlack != null ) { watchBackgroundsBmp[i].mBackgroundBlack.recycle(); watchBackgroundsBmp[i].mBackgroundBlack = null; /*System.gc();*/ }
                if (watchBackgroundsBmp[i].mBackgroundColorized != null ) { watchBackgroundsBmp[i].mBackgroundColorized.recycle(); watchBackgroundsBmp[i].mBackgroundColorized = null; /*System.gc();*/ }
                watchBackgroundsBmp[i].mBackgroundBlack = makeBitmapEffScaled(mVars.width, mVars.height,
                        dialElements.mDialPlatesPathsScaled[i], false);
                //Bitmap.createScaledBitmap(watchBackgroundsBmp[i].mBackground, width, height, true);
                watchBackgroundsBmp[i].mBackgroundColorized =
                        watchBackgroundsBmp[i].mBackgroundBlack.copy(Bitmap.Config.ARGB_8888, true);
                //
//                if (watchBackgroundsBmp[i].mBckgrShadowS == null
//                        || watchBackgroundsBmp[i].mBckgrShadowS.getWidth() != width
//                        || watchBackgroundsBmp[i].mBckgrShadowS.getHeight() != height) {
//                    if (watchBackgroundsBmp[i].mBckgrShadow != null) {
//                        watchBackgroundsBmp[i].mBckgrShadowS =
//                                Bitmap.createScaledBitmap(watchBackgroundsBmp[i].mBckgrShadow, width, height, true);
//                    }
//                }
                //makeHourMarksShadowEffScaled
                if (watchBackgroundsBmp[i].mBckgrShadowS != null) { watchBackgroundsBmp[i].mBckgrShadowS.recycle(); watchBackgroundsBmp[i].mBckgrShadowS = null; /*System.gc();*/ }
                watchBackgroundsBmp[i].mBckgrShadowS = makeHourMarksShadowEffScaled(mVars.width, mVars.height,
                        dialElements.mHourMarkPathsScaled, i, false); /*System.gc();*/
            }
            changeBackgroundColor(denseAppearance);
        } // createBackgroundSM

        private void createMainHandsSM() {
            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                if (watchMainHandsBmp[i].mMinutesHandColorized == null) {
                    watchMainHandsBmp[i].mMinutesHandColorized = watchMainHandsBmp[i].mMinutesHandBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                if (watchMainHandsBmp[i].mHoursHandColorized == null) {
                    watchMainHandsBmp[i].mHoursHandColorized = watchMainHandsBmp[i].mHoursHandBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                if (watchMainHandsBmp[i].mSecondsHandColorized == null) {
                    watchMainHandsBmp[i].mSecondsHandColorized = watchMainHandsBmp[i].mSecondsHandBlack.copy(Bitmap.Config.ARGB_8888, true);
                    changeSecondsHandColor(denseAppearance.mMainSecondsHandColor);
                }
            }
            changeMainHandsOutlineColor(denseAppearance.mMainMainHandsColor);
            changeHourHandDecorColor(denseAppearance.mMainHourHandColor, false);
            changeHourHandDecorColor(denseAppearance.mAmbientHourHandColor, true);
            changeMinuteHandDecorColor(denseAppearance.mMainMinuteHandColor, false);
            changeMinuteHandDecorColor(denseAppearance.mAmbientMinuteHandColor, true);
        } // createMainHandsSM

        private void changeSecondsHandColor(int color) {
            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                if (watchMainHandsBmp[i].mSecondsHandColorized != null) {
                    int bmpwidth = watchMainHandsBmp[i].mSecondsHandColorized.getWidth();
                    int bmpheight = watchMainHandsBmp[i].mSecondsHandColorized.getHeight();
                    int[] pixrow = new int[bmpwidth];
                    for (int y = 0; y < bmpheight; y++) {
                        watchMainHandsBmp[i].mSecondsHandBlack.getPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                        for (int x = 0; x < bmpwidth; x++) {
                            pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                        }
                        watchMainHandsBmp[i].mSecondsHandColorized.setPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    }
                }
            }
        } // changeSecondsHandColor

        private void changeHourHandDecorColor(int color, boolean ambient) {
//            // draw decor
//            mVars.handPath.reset();
//            if (currentAppearance.watchMainHandsIndex == ACommon.HANDS_STRAGHT) {
//                mVars.handPath.set(dialElements.mMainHandStraightDecorPathScaled[MHP_HOUR]);
//            } else {
//                mVars.handPath.set(dialElements.mMainHandRhombusDecorPathScaled[MHP_HOUR]);
//            }
//            mVars.handPath.transform(mVars.hrMatrix);
//            mHourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mHourPaint.setColor(currentAppearance.mMainHourHandColor);
//            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
//            mHourPaint.setStrokeWidth(2f);
//            canvas.drawPath(mVars.handPath, mHourPaint);
//            // draw decor blurred outline
//            mHourPaint.setARGB(255, 0, 0, 0);
//            mHourPaint.setStyle(Paint.Style.STROKE);
//            mHourPaint.setStrokeWidth(0f);
//            mHourPaint.setMaskFilter(mVars.blurMaskFilterR3O);
//            canvas.drawPath(mVars.handPath, mHourPaint);
//            mHourPaint.setMaskFilter(null);

            Bitmap bitmap, result;
            Canvas canvas;
//            Paint paint = new Paint();
            Path path = null, clip = null;

//            canvas = new Canvas();

            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                result = Bitmap.createBitmap(mVars.width, mVars.handBitmapHeight, Bitmap.Config.ARGB_8888);

                canvas = new Canvas(result);
                //canvas.setBitmap(result);

                //canvas.drawColor(Color.TRANSPARENT);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                switch (i) {
                    case ACommon.HANDS_STRAGHT:
                        path = dialElements.mMainHandStraightDecorPathScaled[MHP_HOUR];
                        clip = dialElements.mMainHandStraightOutlinePathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_RHOMB:
                        path = dialElements.mMainHandRhombusDecorPathScaled[MHP_HOUR];
                        clip = dialElements.mMainHandRhombusOutlinePathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_CURLHEAD:
                        path = dialElements.mMainHandCurlHeadDecorPathScaled[MHP_HOUR];
                        clip = dialElements.mMainHandCurlHeadOutlinePathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_ARROW:
                        path = dialElements.mMainHandArrowDecorPathScaled[MHP_HOUR];
                        clip = dialElements.mMainHandArrowOutlinePathScaled[MHP_HOUR];
                        break;
                }

                if (!ambient) {
                    if (watchMainHandsBmp[i].mHoursHandDecor != null) { watchMainHandsBmp[i].mHoursHandDecor.recycle(); watchMainHandsBmp[i].mHoursHandDecor = null; /*System.gc();*/ }
                } else {
                    if (watchMainHandsBmp[i].mHoursHandDecorAmbient != null) { watchMainHandsBmp[i].mHoursHandDecorAmbient.recycle(); watchMainHandsBmp[i].mHoursHandDecorAmbient = null; /*System.gc();*/ }
                }

                bitmap = changeInnerDecorColor(mVars.width, mVars.handBitmapHeight, path, color);
                canvas.clipPath(clip);
                //canvas.clipPath(path);
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle(); bitmap = null; /*System.gc();*/

                if (!ambient) {
                    watchMainHandsBmp[i].mHoursHandDecor = result;
                } else {
                    watchMainHandsBmp[i].mHoursHandDecorAmbient = result;
                }
            }
        } // changeHourHandDecorColor
        //
        private void changeUpperDecorColor(int color, boolean ambient) {
            Bitmap bitmap, result;//, decor;
            Canvas canvas;
            Path path, clip;

//            if (ambient) decor = mDecorUpperDecorAmbient;
//            else decor = mDecorUpperDecor;

            result = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(result);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            path = dialElements.mDecorUpperPathScaled[DO_DECOR];
            clip = dialElements.mDecorUpperPathScaled[DO_OUTLINE];

            if (ambient) {
                if (mDecorUpperDecorAmbient != null) { mDecorUpperDecorAmbient.recycle(); mDecorUpperDecorAmbient = null; /*System.gc();*/ }
            } else {
                if (mDecorUpperDecor != null) { mDecorUpperDecor.recycle(); mDecorUpperDecor = null; /*System.gc();*/ }
            }

            bitmap = changeInnerDecorColor(mVars.width, mVars.height, path, color);
            canvas.clipPath(clip);
            //canvas.clipPath(path);
            canvas.drawBitmap(bitmap, 0, 0, null);
            bitmap.recycle(); bitmap = null; /*System.gc();*/

            if (ambient) {
                mDecorUpperDecorAmbient = result;
            } else {
                mDecorUpperDecor = result;
            }

            denseAppearance.mDialPlateReady = false;
        } // changeUpperDecorColor
        //
        private void changeMinuteHandDecorColor(int color, boolean ambient) {
//            // draw decor
//            mVars.handPath.reset();
//            if (currentAppearance.watchMainHandsIndex == ACommon.HANDS_STRAGHT) {
//                mVars.handPath.set(dialElements.mMainHandStraightDecorPathScaled[MHP_MINUTE]);
//            } else {
//                mVars.handPath.set(dialElements.mMainHandRhombusDecorPathScaled[MHP_MINUTE]);
//            }
//            mVars.handPath.transform(mVars.hrMatrix);
//            mMinutePaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mMinutePaint.setStrokeWidth(2f);
//            mMinutePaint.setColor(currentAppearance.mMainMinuteHandColor);
//            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
//            canvas.drawPath(mVars.handPath, mMinutePaint);
//            // draw decor blurred outline
//            mMinutePaint.setARGB(255, 0, 0, 0);
//            mMinutePaint.setStyle(Paint.Style.STROKE);
//            mMinutePaint.setStrokeWidth(0f);
//            mMinutePaint.setMaskFilter(mVars.blurMaskFilterR3O);
//            canvas.drawPath(mVars.handPath, mMinutePaint);
//            mMinutePaint.setMaskFilter(null);

            Bitmap bitmap, result;
            Canvas canvas;
//            Paint paint = new Paint();
            Path path = null, clip = null;

//            canvas = new Canvas();

            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                result = Bitmap.createBitmap(mVars.width, mVars.handBitmapHeight, Bitmap.Config.ARGB_8888);

                canvas = new Canvas(result);
                //canvas.setBitmap(result);

                //canvas.drawColor(Color.TRANSPARENT);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                switch (i) {
                    case ACommon.HANDS_STRAGHT:
                        path = dialElements.mMainHandStraightDecorPathScaled[MHP_MINUTE];
                        clip = dialElements.mMainHandStraightOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_RHOMB:
                        path = dialElements.mMainHandRhombusDecorPathScaled[MHP_MINUTE];
                        clip = dialElements.mMainHandRhombusOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_CURLHEAD:
                        path = dialElements.mMainHandCurlHeadDecorPathScaled[MHP_MINUTE];
                        clip = dialElements.mMainHandCurlHeadOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_ARROW:
                        path = dialElements.mMainHandArrowDecorPathScaled[MHP_MINUTE];
                        clip = dialElements.mMainHandArrowOutlinePathScaled[MHP_MINUTE];
                        break;
                }
//                paint.setStyle(Paint.Style.FILL_AND_STROKE);
//                paint.setStrokeWidth(1f);
//                paint.setColor(color);
//                paint.setStrokeCap(Paint.Cap.SQUARE);
//                canvas.drawPath(path, paint);
//
//                paint.setARGB(80, 0, 0, 0);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(2f);
//                //paint.setMaskFilter(mVars.blurMaskFilterR3O);
//                canvas.drawPath(path, paint);
//                //paint.setMaskFilter(null);

                if (!ambient) {
                    if (watchMainHandsBmp[i].mMinutesHandDecor != null) { watchMainHandsBmp[i].mMinutesHandDecor.recycle(); watchMainHandsBmp[i].mMinutesHandDecor = null; /*System.gc();*/ }
                } else {
                    if (watchMainHandsBmp[i].mMinutesHandDecorAmbient != null) { watchMainHandsBmp[i].mMinutesHandDecorAmbient.recycle(); watchMainHandsBmp[i].mMinutesHandDecorAmbient = null; /*System.gc();*/ }
                }

                bitmap = changeInnerDecorColor(mVars.width, mVars.handBitmapHeight, path, color);
                //canvas.clipPath(clip);
                //canvas.clipPath(path);
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle(); bitmap = null; /*System.gc();*/

                if (!ambient) {
                    watchMainHandsBmp[i].mMinutesHandDecor = result;
                } else {
                    watchMainHandsBmp[i].mMinutesHandDecorAmbient = result;
                }
            }
        } // changeMinuteHandDecorColor
        //
        //
        private void changeMinuteHandDecorColor_NEW(int color) {
            Bitmap bitmap, result;
            Canvas canvas;
            Path path = null, clip = null;
            int width = (int) dialElements.baseHandWidth, height = (int) dialElements.baseHandHeight;

            canvas = new Canvas();

            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                result = Bitmap.createBitmap(mVars.width, mVars.handBitmapHeight, Bitmap.Config.ARGB_8888);

                canvas.setBitmap(result);
                //canvas.drawColor(Color.TRANSPARENT);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                switch (i) {
                    case ACommon.HANDS_STRAGHT:
                        path = dialElements.mMainHandStraightDecorPathScaled[MHP_MINUTE];
                        //clip = dialElements.mMainHandStraightOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_RHOMB:
                        path = dialElements.mMainHandRhombusDecorPathScaled[MHP_MINUTE];
                        //clip = dialElements.mMainHandRhombusOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_CURLHEAD:
                        path = dialElements.mMainHandCurlHeadDecorPathScaled[MHP_MINUTE];
                        //clip = dialElements.mMainHandCurlHeadOutlinePathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_ARROW:
                        path = dialElements.mMainHandArrowDecorPathScaled[MHP_MINUTE];
                        //clip = dialElements.mMainHandArrowOutlinePathScaled[MHP_MINUTE];
                        break;
                }

                if (watchMainHandsBmp[i].mMinutesHandDecor != null) { watchMainHandsBmp[i].mMinutesHandDecor.recycle(); watchMainHandsBmp[i].mMinutesHandDecor = null; /*System.gc();*/ }

                bitmap = changeInnerDecorColor(mVars.width, mVars.handBitmapHeight, path, color);
                //canvas.clipPath(clip);
                //canvas.clipPath(path);
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle(); bitmap = null; /*System.gc();*/

                watchMainHandsBmp[i].mMinutesHandDecor = result;
            }
        } // changeMinuteHandDecorColor_NEW
        //
//        Shader shdBitmap = new BitmapShader(mVars.backgroundBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                mTickPaint.getColor(), mTickPaint.getColor(), Shader.TileMode.CLAMP);
//        Shader shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
//        //
//        // draw frame around date cut hole
//        mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        mTickPaint.setStrokeWidth(3.0f);
//        mTickPaint.setShader(shdSumm);
//        mTickPaint.setAntiAlias(true);
//        if (true) canvas.drawPath(dialElements.mDateCutPathsScaled[currentAppearance.watchBackgroundIndex], mTickPaint);
//        mTickPaint.setShader(null);
        //
        private Bitmap changeInnerDecorColor(int width, int height, Path path, int color) {
            Bitmap result, bitmap, blurred;
            Canvas canvas;
            Paint paint = new Paint();
            int colorAlpha;

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);

//            canvas.drawColor(Color.TRANSPARENT);
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//            paint.setStrokeWidth(5f);
//            paint.setColor(color);
//            paint.setStrokeCap(Paint.Cap.SQUARE);
//            canvas.drawPath(path, paint);
            //
            //canvas.drawColor(Color.TRANSPARENT);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(color);

            colorAlpha = Color.alpha(color);
            paint.setARGB(colorAlpha, 96, 96, 96);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStrokeWidth(2f);
            canvas.drawPath(path, paint);

            blurred = blur(bitmap, 3f);
            //bitmap.recycle();
            //return blurred;

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint.setColor(color);
            paint.setAlpha(colorAlpha);
            //paint.setColor(Color.WHITE);
            //paint.setAlpha(255);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStrokeWidth(2f);
            canvas.drawPath(path, paint);
            //
            Shader shdBlurred = new BitmapShader(blurred, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Shader shdPath = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Shader shdSumm = new ComposeShader(shdPath, shdBlurred, PorterDuff.Mode.SRC_ATOP);
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(result);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint.setShader(shdSumm);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(1.5f);
            canvas.drawPath(path, paint);

            bitmap.recycle(); bitmap = null;
            blurred.recycle(); blurred = null; /*System.gc();*/

            return result;
        } // changeInnerDecorColor
        //
        private void changeMainHandsOutlineColor(int color) {
            for (int i=0; i< ACommon.NUM_MAIN_HAND_SETS; i++) {
                if (watchMainHandsBmp[i].mMinutesHandColorized != null) {
                    int bmpwidth = watchMainHandsBmp[i].mMinutesHandColorized.getWidth();
                    int bmpheight = watchMainHandsBmp[i].mMinutesHandColorized.getHeight();
                    int[] pixrow = new int[bmpwidth];
                    for (int y = 0; y < bmpheight; y++) {
                        watchMainHandsBmp[i].mMinutesHandBlack.getPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                        for (int x = 0; x < bmpwidth; x++) {
                            pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                        }
                        watchMainHandsBmp[i].mMinutesHandColorized.setPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    }
                }
                if (watchMainHandsBmp[i].mHoursHandColorized != null) {
                    int bmpwidth = watchMainHandsBmp[i].mHoursHandColorized.getWidth();
                    int bmpheight = watchMainHandsBmp[i].mHoursHandColorized.getHeight();
                    int[] pixrow = new int[bmpwidth];
                    for (int y = 0; y < bmpheight; y++) {
                        watchMainHandsBmp[i].mHoursHandBlack.getPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                        for (int x = 0; x < bmpwidth; x++) {
                            pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                        }
                        watchMainHandsBmp[i].mHoursHandColorized.setPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                    }
                }
            }
            //changeTriangleColor(color);
            changeBitmapColor(mDecorUpperBlack, mDecorUpperColorized, color);

        } // changeMainHandsOutlineColor

//        private void changeTriangleColor(int color) {
//            if (mTriangleBmpColorized != null) {
//                int bmpwidth = mTriangleBmpColorized.getWidth();
//                int bmpheight = mTriangleBmpColorized.getHeight();
//                int[] pixrow = new int[bmpwidth];
//                for (int y = 0; y < bmpheight; y++) {
//                    mTriangleBmpBlack.getPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
//                    for (int x = 0; x < bmpwidth; x++) {
//                        pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
//                    }
//                    mTriangleBmpColorized.setPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
//                }
//            }
//        } // changeTriangleColor

        private void changeBitmapColor(Bitmap black, Bitmap colorized, int color) {
            if (black == null) return;
            if (colorized == null) return;
            int bmpWidth = black.getWidth();
            int bmpHeight = black.getHeight();
            int[] pixrow = new int[bmpWidth];
            for (int y = 0; y < bmpHeight; y++) {
                black.getPixels(pixrow, 0, bmpWidth, 0, y, bmpWidth, 1);
                for (int x = 0; x < bmpWidth; x++) {
                    pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                }
                colorized.setPixels(pixrow, 0, bmpWidth, 0, y, bmpWidth, 1);
            }
        } // changeBitmapColor

        private void createAuxHandsSM() {
            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                if (watchAuxHandsBmp[i].mAuxHandWeekdayColorized == null) {
                    watchAuxHandsBmp[i].mAuxHandWeekdayColorized = watchAuxHandsBmp[i].mAuxHandWeekdayBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWeekdayColorized, denseAppearance.mMainAuxHandWeekdayColor);
                if (watchAuxHandsBmp[i].mAuxHandWeekdayAmbient == null) {
                    watchAuxHandsBmp[i].mAuxHandWeekdayAmbient = watchAuxHandsBmp[i].mAuxHandWeekdayBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
//                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWeekdayAmbient, denseAppearance.mAmbientDomAndAuxHandsColor);
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWeekdayAmbient, denseAppearance.mAmbientAuxHandsColor);
                //
                if (watchAuxHandsBmp[i].mAuxHandWearBattColorized == null) {
                    watchAuxHandsBmp[i].mAuxHandWearBattColorized = watchAuxHandsBmp[i].mAuxHandWearBattBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWearBattColorized, denseAppearance.mMainAuxHandWearBattColor);
                if (watchAuxHandsBmp[i].mAuxHandWearBattAmbient == null) {
                    watchAuxHandsBmp[i].mAuxHandWearBattAmbient = watchAuxHandsBmp[i].mAuxHandWearBattBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
//                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWearBattAmbient, denseAppearance.mAmbientDomAndAuxHandsColor);
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWearBattAmbient, denseAppearance.mAmbientAuxHandsColor);
                //
                if (watchAuxHandsBmp[i].mAuxHandPhoneBattColorized == null) {
                    watchAuxHandsBmp[i].mAuxHandPhoneBattColorized = watchAuxHandsBmp[i].mAuxHandPhoneBattBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandPhoneBattColorized, denseAppearance.mMainAuxHandPhoneBattColor);
                if (watchAuxHandsBmp[i].mAuxHandPhoneBattAmbient == null) {
                    watchAuxHandsBmp[i].mAuxHandPhoneBattAmbient = watchAuxHandsBmp[i].mAuxHandPhoneBattBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
//                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandPhoneBattAmbient, denseAppearance.mAmbientDomAndAuxHandsColor);
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandPhoneBattAmbient, denseAppearance.mAmbientAuxHandsColor);
                //
                if (watchAuxHandsBmp[i].mAuxHandMonthColorized == null) {
                    watchAuxHandsBmp[i].mAuxHandMonthColorized = watchAuxHandsBmp[i].mAuxHandMonthBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandMonthColorized, denseAppearance.mMainAuxHandMonthColor);
                if (watchAuxHandsBmp[i].mAuxHandMonthAmbient == null) {
                    watchAuxHandsBmp[i].mAuxHandMonthAmbient = watchAuxHandsBmp[i].mAuxHandMonthBlack.copy(Bitmap.Config.ARGB_8888, true);
                }
//                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandMonthAmbient, denseAppearance.mAmbientDomAndAuxHandsColor);
                changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandMonthAmbient, denseAppearance.mAmbientAuxHandsColor);
            }
//            changeAuxHandWeekdayColor(denseAppearance.mMainAuxHandWeekdayColor);
//            changeAuxHandWearBattColor(denseAppearance.mMainAuxHandWearBattColor);
//            changeAuxHandPhoneBattColor(denseAppearance.mMainAuxHandPhoneBattColor);
//            changeAuxHandMonthColor(denseAppearance.mMainAuxHandMonthColor);
        } // createAuxHandsSM

        private void changeAuxHandColor(Bitmap bitmap, int color) {
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] pixrow = new int[width];
                for (int y = 0; y < height; y++) {
                    bitmap.getPixels(pixrow, 0, width, 0, y, width, 1);
                    for (int x = 0; x < width; x++) {
                        pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                    }
                    bitmap.setPixels(pixrow, 0, width, 0, y, width, 1);
                }
            }
        } // changeAuxHandColor

        private void changeAuxHandMonthColor(int color, boolean ambient) {
            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                if (!ambient) changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandMonthColorized, color);
                else changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandMonthAmbient, color);
            }
        } // changeAuxHandMonthColor

        private void changeAuxHandPhoneBattColor(int color, boolean ambient) {
            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                if (watchAuxHandsBmp[i].mAuxHandPhoneBattColorized != null) {
                    if (!ambient) changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandPhoneBattColorized, color);
                    else changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandPhoneBattAmbient, color);
                }
            }
        } // changeAuxHandPhoneBattColor

        private void changeAuxHandWearBattColor(int color, boolean ambient) {
            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                if (watchAuxHandsBmp[i].mAuxHandWearBattColorized != null) {
                    if (!ambient) changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWearBattColorized, color);
                    else changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWearBattAmbient, color);
                }
            }
        } // changeAuxHandWearBattColor

        private void changeAuxHandWeekdayColor(int color, boolean ambient) {
            for (int i=0; i< ACommon.NUM_AUX_HAND_SETS; i++) {
                if (watchAuxHandsBmp[i].mAuxHandWeekdayColorized != null) {
                    if (!ambient) changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWeekdayColorized, color);
                    else changeAuxHandColor(watchAuxHandsBmp[i].mAuxHandWeekdayAmbient, color);
                }
            }
        } // changeAuxHandWeekdayColor



        Bitmap composePlateTexture(Bitmap texture) {
            Paint paint;

            Bitmap result = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);

            //String timeStamp = String.valueOf(System.currentTimeMillis());
//            ACommon.bmpToPicturesDir(AWearFaceService.this, texture, "fx_", "_org", timeStamp);

            Canvas canvas = new Canvas(result);





//            Shader upper, lower;
//            float lightnessStops[] = new float[] {
//                    0.0f,
//                    1.0f,
//            };
//            int lightnessColors[] = new int[] {
//                    Color.argb(255, 255, 255, 255),
//                    Color.argb(255, 0, 0, 0),
//            };
//            float darknessStops[] = new float[] {
//                    0.0f,
//                    0.5f,
//                    1.0f,
//            };
//            int darknessColors[] = new int[] {
//                    Color.argb(255, 0, 0, 0),
//                    Color.argb(255, 0, 0, 0),
//                    Color.argb(255, 255, 255, 255),
//            };
//            Shader lightness = new RadialGradient(160, 80, 160, lightnessColors, lightnessStops, Shader.TileMode.CLAMP);
//            Shader darkness = new RadialGradient(160, 640, 520, darknessColors, darknessStops, Shader.TileMode.CLAMP);
//            upper = lightness;
//            lower = darkness;
//            Shader summ = new ComposeShader(upper, lower, PorterDuff.Mode.XOR);
//
//            paint = new Paint(); paint.setAntiAlias(true);
//            //paint.setColorFilter(colorFilter);
//
//            paint.setAlpha(127);
//            paint.setShader(upper);
//            canvas.drawCircle(160, 160, 160, paint);
//            paint.setShader(lower);
//            canvas.drawCircle(160, 160, 160, paint);
//            Bitmap blr = blur(result, 20f);
//            ACommon.bmpToPicturesDir(AWearFaceService.this, blr, "fx_", "_out", timeStamp);



            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint = new Paint(); paint.setAntiAlias(true);
            PorterDuffColorFilter colorFilter =
                    new PorterDuffColorFilter(denseAppearance.mMainBackgroundColor, PorterDuff.Mode.MULTIPLY);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(texture, 0, 0, paint);

//            paint.setAlpha(127);
//            canvas.drawBitmap(blr, 0, 0, paint);

//            ACommon.bmpToPicturesDir(AWearFaceService.this, result, "fx_", "_out", timeStamp);

            return result;//texture;//; //result
        }

        private void prepareBitmaps() {
            Resources resources = AWearFaceService.this.getResources();
            Drawable drawable;

            mCircleGradientNoTransparent = null; /*System.gc();*/ mCircleGradientNoTransparent = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            mBigAuxDialGradientNoTransparent = null; /*System.gc();*/ mBigAuxDialGradientNoTransparent = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
            mSmallAuxDialGradientNoTransparent = null; /*System.gc();*/ mSmallAuxDialGradientNoTransparent = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap

            Bitmap tmpbmp;
//            drawable = ContextCompat.getDrawable(AWearFaceService.this, R.mipmap.main_dial_fx);
//            tmpbmp = ((BitmapDrawable) drawable).getBitmap();
            //
            int density = mCircleGradientNoTransparent.getDensity();
            BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            bmfOptions.inDensity = density; // !!!!!!!
            bmfOptions.inScaled = false;
            bmfOptions.inDither = false;
            bmfOptions.inSampleSize = 1;
            bmfOptions.inJustDecodeBounds = false;
            //bmfOptions.inPreferQualityOverSpeed = true;
            //bmfOptions.inPremultiplied = false;
            bmfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //bmfOptions.outWidth = 564;
            //bmfOptions.outHeight = 564;
            tmpbmp = BitmapFactory.decodeResource(resources, R.drawable.main_dial_fx, bmfOptions);
            //
            mFxPlateTexture = null; /*System.gc();*/ mFxPlateTexture = composePlateTexture(
                    (Bitmap.createScaledBitmap(tmpbmp, mVars.width, mVars.height, true)).copy(Bitmap.Config.ARGB_8888, true)
            );

            createBackgroundGradient(denseAppearance);
            changeSmallAuxDialColor(denseAppearance);
            changeBigAuxDialColor(denseAppearance);

            for (int i=0; i< ACommon.NUM_BACKGROUNDS; i++) {
                watchBackgroundsBmp[i] = new WatchBackgroundBmp();
            } /*System.gc();*/
//            drawable = resources.getDrawable(R.drawable.bg_trpl_vrt_black);
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_VRT].mBackground = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dig_shd_trpl_vrt);
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_VRT].mBckgrShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dom_shd_trpl_vrt);
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_VRT].mDomShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_sngl_vrt_black);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_VRT].mBackground = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dig_shd_sngl_vrt);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_VRT].mBckgrShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dom_shd_sngl_vrt);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_VRT].mDomShadow = ((BitmapDrawable) drawable).getBitmap();
            //
            //drawable = resources.getDrawable(R.drawable.bg_trpl_hrz_black);
            //watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_HRZ].mBackground = ((BitmapDrawable) drawable).getBitmap();
            bmfOptions = new BitmapFactory.Options();
            bmfOptions.inScaled = false;
            bmfOptions.inDither = false;
            bmfOptions.inSampleSize = 1;
            bmfOptions.inJustDecodeBounds = false;
            //bmfOptions.inPreferQualityOverSpeed = true;
            //bmfOptions.inPremultiplied = false;
            bmfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_HRZ].mBackground =
//                    BitmapFactory.decodeResource(resources, R.drawable.bg_trpl_hrz_black, bmfOptions);
//            Log.i(TAG, "((((( bmfOptions, outHeight=" + bmfOptions.outHeight + ", outWidth=" + bmfOptions.outWidth);
            //
//            drawable = resources.getDrawable(R.drawable.bg_dig_shd_trpl_hrz);
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_HRZ].mBckgrShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dom_shd_trpl_hrz);
//            watchBackgroundsBmp[ACommon.DIAL_TRIPLE_HOLE_HRZ].mDomShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_sngl_hrz_black);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_HRZ].mBackground = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dig_shd_sngl_hrz);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_HRZ].mBckgrShadow = ((BitmapDrawable) drawable).getBitmap();
            //
//            drawable = resources.getDrawable(R.drawable.bg_dom_shd_sngl_hrz);
//            watchBackgroundsBmp[ACommon.DIAL_SINGLE_HOLE_HRZ].mDomShadow = ((BitmapDrawable) drawable).getBitmap();
            //
            //
            //createBackgroundSM(mDisplayDimension, mDisplayDimension);
            /*System.gc();*/ createBackgroundSM(mVars.width, mVars.height);










//            //drawable = resources.getDrawable(R.drawable.triangle_1);
//            drawable = resources.getDrawable(R.drawable.triangle_2);
//            mTriangleBitmap = ((BitmapDrawable) drawable).getBitmap();
//            // TRIANGLE_BMP_DIM TRIANGLE_BMP_SCALED_DIM
//            mTriangleBmpBlack = Bitmap.createScaledBitmap(mTriangleBitmap,
//                    ACommon.TRIANGLE_BMP_SCALED_DIM, ACommon.TRIANGLE_BMP_SCALED_DIM, true);
//            mTriangleBmpColorized = mTriangleBmpBlack.copy(Bitmap.Config.ARGB_8888, true);
//            drawable = resources.getDrawable(R.drawable.triangle_2_shd);
//            mTriangleBitmapShd = ((BitmapDrawable) drawable).getBitmap();
//            mTriangleBitmapShdS = Bitmap.createScaledBitmap(mTriangleBitmapShd,
//                    ACommon.TRIANGLE_BMP_SCALED_DIM, ACommon.TRIANGLE_BMP_SCALED_DIM, true);
//            changeTriangleColor(denseAppearance.mMainMainHandsColor);


            mDecorUpperBlack =
                    makeBitmapEffScaled(mVars.width, mVars.height, dialElements.mDecorUpperPathScaled[DO_COMPOSITE], false);
            mDecorUpperShadow =
                    makeShadowEffScaled(mVars.width, mVars.height, dialElements.mDecorUpperPathScaled[DO_OUTLINE], null, false);
            mDecorUpperColorized = mDecorUpperBlack.copy(Bitmap.Config.ARGB_8888, true);
//            changeMainHandsOutlineColor(denseAppearance.mMainMainHandsColor);
//            changeHourHandDecorColor(denseAppearance.mMainHourHandColor);
            changeUpperDecorColor(denseAppearance.mMainDecorUpperColor, false);
            changeUpperDecorColor(denseAppearance.mAmbientDecorUpperColor, true);





            mountingHoleBitmap = drawHandMountingHoleBitmap();






            makeMainHandsBmpArray();
            createMainHandsSM();
            //
            makeAuxHandsBmpArray();
            createAuxHandsSM();

//            drawable = resources.getDrawable(R.drawable.wings);
//            mWingsBitmap = ((BitmapDrawable) drawable).getBitmap();


        } // prepareBitmaps




        public void createPaints() {
            mHourPaint = new Paint();
            mHourPaint.setARGB(255, 200, 200, 200);
            mHourPaint.setStrokeWidth(5.f);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);

            mMinutePaint = new Paint();
            mMinutePaint.setARGB(255, 200, 200, 200);
            mMinutePaint.setStrokeWidth(3.f);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);

            mSecondPaint = new Paint();
            mSecondPaint.setARGB(255, 255, 0, 0);
            mSecondPaint.setStrokeWidth(2.f);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);

            mTickPaint = new Paint();
            //mTickPaint.setARGB(255/*100*/, 255, 255, 255); // 0xffffffff
            mTickPaint.setColor(denseAppearance.mMainTickColor);
            //Log.i(TAG, "((( mTickPaint #1 color=" + mTickPaint.getColor());
            mTickPaint.setStrokeWidth(1.5f);
            mTickPaint.setAntiAlias(true);
            mTickPaint.setStrokeCap(Paint.Cap.SQUARE);

            mTickDigitPaint = new Paint();
            //mTickDigitPaint.setARGB(200, 255, 255, 255); // 0xc8ffffff
            mTickDigitPaint.setColor(denseAppearance.mMainTickDigitColor);
            mTickDigitPaint.setStrokeWidth(1.f);
            mTickDigitPaint.setAntiAlias(true);
            mTickDigitPaint.setStrokeCap(Paint.Cap.SQUARE);

            mHrTickPaint = new Paint();
            mHrTickPaint.setARGB(100, 255, 255, 255);
            mHrTickPaint.setStrokeWidth(5.f);
            mHrTickPaint.setAntiAlias(true);
            mHrTickPaint.setStrokeCap(Paint.Cap.ROUND);

            mHandPaint = new Paint();
            mHandPaint.setAntiAlias(true);
            mHandPaint.setFilterBitmap(true);
            mHandPaint.setDither(true);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setARGB(255, 30, 30, 30);

            mCalendarDialPaint = new Paint();
            mCalendarDialPaint.setARGB(255, 255, 255, 255);
            mCalendarDialPaint.setAntiAlias(true);
            mCalendarDialPaint.setStyle(Paint.Style.STROKE);

            mCalendarWkdayPaint = new Paint();
            mCalendarWkdayPaint.setARGB(255, 255, 255, 255);
            mCalendarWkdayPaint.setAntiAlias(true);
            mCalendarWkdayPaint.setStyle(Paint.Style.STROKE);

            mCalendarMonthPaint = new Paint();
            mCalendarMonthPaint.setARGB(255, 255, 255, 255);
            mCalendarMonthPaint.setAntiAlias(true);
            mCalendarMonthPaint.setStyle(Paint.Style.STROKE);

            mBattDialPaint = new Paint();
            mBattDialPaint.setARGB(255, 255, 255, 255);
            mBattDialPaint.setAntiAlias(true);
            mBattDialPaint.setStrokeWidth(2f);
            mBattDialPaint.setStyle(Paint.Style.STROKE);

            mDatePaint = new Paint();
            mDatePaint.setARGB(255, 198, 81, 75);
            mDatePaint.setAntiAlias(true);
            mDatePaint.setStyle(Paint.Style.FILL);

            mScriptPaint = new Paint();

            //mDigitsPaint - контуры цифр главного циферблата
            mDigitsPaint = new Paint();
            mDigitsPaint.setAntiAlias(true);
            mDigitsPaint.setDither(true);
            mDigitsPaint.setFilterBitmap(true);

            mMountingHolePaint = new Paint();
            mMountingHolePaint.setAlpha(255);
            mMountingHolePaint.setAntiAlias(true);
            mMountingHolePaint.setDither(true);

            mTzLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            mTzDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
        }

        private void getDisplayDimensions(Context context) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            //Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            //Log.i(TAG, "((((( Display W=" + width + ", Display H=" + height);
            mDisplayWidth = width;
            mDisplayHeight = height;
//            if (width >= height) {
//                // chin moto 360
//                mDisplayDimension = mDisplayWidth;
//            } else {
//                mDisplayDimension = mDisplayWidth;
//            }
            mDisplayDimension = mDisplayWidth;
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            //Log.i(TAG, "Engine.onCreate");

            //tmp test
            //ACommon.listLocalFiles(getApplicationContext());

            productId = getResources().getString(R.string.product_id);

            Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                @Override
                public void onResult(NodeApi.GetLocalNodeResult result) {
                    String peerId = result.getNode().getId();
                    //Log.i(TAG, "Local peer id: " + peerId);
                    WearApplication application = (WearApplication) getApplication();
                    application.setLocalPeerId(peerId);
                    mPeerId = peerId;
                }
            });


            for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
                demoPackData[i] = new DemoPackData();
            }


//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "onCreate");
//            }

            denseAppearance = new WatchAppearance(null, getApplicationContext());

            getDisplayDimensions(getApplicationContext());



            try {
                PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo("com.google.android.wearable.app", 0);
                if (packageInfo.versionCode > 720000000) {
                    // Supports taps - cache this result to avoid calling PackageManager again
                    //Log.i(TAG, "((( wearable.app tap supported");
                    mTapSupported = true;
                } else {
                    // Device does not support taps yet
                    //Log.i(TAG, "((( wearable.app tap NOT supported");
                    mTapSupported = false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }



            if (mTapSupported) {
                setWatchFaceStyle(new WatchFaceStyle.Builder(AWearFaceService.this)
                        .setAcceptsTapEvents(true)
                        .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                        .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                        .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
//                        .setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                        .setStatusBarGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL)
                        .setShowSystemUiTime(false)
                        .build());
            } else {
                setWatchFaceStyle(new WatchFaceStyle.Builder(AWearFaceService.this)
                        .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                        .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                        .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                        .setStatusBarGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL)
                        .setShowSystemUiTime(false)
                        .build());
            }

            createPaints();

            String cfn = getString(R.string.configFileName);
            Bundle config = null;
            config = ACommon.readPersistentDataFromFile(AWearFaceService.this, cfn);
            if (null == config) {
                //Log.i(TAG, "((( CONFIG EMPTY");
                mAppPreferences = new AppPreferences(getApplicationContext(), true);
                denseAppearance = resetConfigToDefaults(true); //???
                createConfigFile(false);
            } else {
                mAppPreferences = new AppPreferences(getApplicationContext(), false);
                boolean res = unBundleConfig(denseAppearance, config, true);
                //Log.i(TAG, "((( CONFIG LOADED FROM FILE AND UNBUNDLED: " + res);
                if (true != res) {
                    denseAppearance = resetConfigToDefaults(true); //???
                    createConfigFile(false);
                }
            }

            mSensMan = (SensorManager)getSystemService(SENSOR_SERVICE);
            List<Sensor> mSensorList = mSensMan.getSensorList(Sensor.TYPE_ALL);
            //int i, x;
            Sensor tmp;
            //String[] mSensors = new String[50];
            String sensName;
            for (int i=0; i<mSensorList.size(); i++) {
                //if (i>=50) break;
                mSensCount++;
                tmp = mSensorList.get(i);
                sensName = tmp.getName();
                //Log.i(TAG, "((((( sensor(" + i + ")=" + sensName);
                if (tmp.getType() == Sensor.TYPE_LIGHT) {
                    mSensorLightMaxValue = tmp.getMaximumRange();
                    //Log.i(TAG, "((((( mSensorLightMaxValue=" + mSensorLightMaxValue);
                }
            }


            //AWearFaceService.this.registerReceiver(mDataFromListenerServiceReceiver,
            //        new IntentFilter(ACommon.EVENT_ACTION));
            LocalBroadcastManager.getInstance(AWearFaceService.this).registerReceiver(mDataFromListenerServiceReceiver,
                    new IntentFilter(ACommon.EVENT_ACTION));

            //mTime = new Time();
            wTime = new WatchTime(TimeZone.getDefault(), mAppPreferences); //setTimeZone

            //mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
            mFirstDayOfWeek = wTime.getFirstDayOfWeek();
            //Log.i(TAG, "((( FDOW=" + mFirstDayOfWeek);
            //mLocaleReceiver

//            pthDgt9 = PathFromPathDataSVG.doPath(svgDgt9);
//            Log.i(TAG, "((((( pthDgt9=" + pthDgt9);

        } //onCreate


        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {

            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    //Log.i(TAG, "((( tap x=" + x + ", y=" + y);
                    wTime.eventSwapTz();
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }

        }







        class ElementsSVG {

            float baseDialDim = 640.0f; // 640x640
            float baseDialWidth = baseDialDim;
            float baseDialHeight = baseDialDim;
            //
            float baseHandWidth = baseDialDim; //640x80
            float baseHandHeight = 80.0f;

            // ACommon.NUM_BACKGROUNDS
            String[] mDateCutSVG = new String[] {
                    // DIAL_SINGLE_HOLE_VRT = 0
                    "M605,336c0,5.5-4.5,10-10,10h-32c-5.5,0-10-4.5-10-10v-32c0-5.5,4.5-10,10-10h32c5.5,0,10,4.5,10,10V336z",
                    // DIAL_TRIPLE_HOLE_VRT = 1
                    "M605,320c0,30.86-11.611,78.682-11.611,78.682" +
                            "c-1.298,5.345-6.638,8.322-11.866,6.617l-29.486-9.618c-5.229-1.706-8.445-7.474-7.148-12.819c0,0,9.111-37.532,9.111-62.862" +
                            "c0-25.15-8.968-62.368-8.968-62.368c-1.288-5.347,1.938-11.108,7.171-12.803l29.504-9.557c5.232-1.695,10.567,1.293,11.856,6.64" +
                            "C593.563,241.912,605,289.36,605,320z",
                    // DIAL_SINGLE_HOLE_HRZ = 2
                    "M346,595c0,5.5-4.5,10-10,10h-32" +
                            "c-5.5,0-10-4.5-10-10v-32c0-5.5,4.5-10,10-10h32c5.5,0,10,4.5,10,10V595z",
                    // DIAL_TRIPLE_HOLE_HRZ = 3
                    "M404.42,581.803" +
                            "c1.688,5.234-1.306,10.568-6.654,11.852c0,0-47.247,11.345-77.767,11.345c-30.64,0-78.088-11.437-78.088-11.437" +
                            "c-5.347-1.289-8.335-6.624-6.64-11.856l9.557-29.504c1.695-5.232,7.457-8.459,12.803-7.171c0,0,37.218,8.968,62.368,8.968" +
                            "c25.06,0,62.106-8.896,62.106-8.896c5.348-1.284,11.105,1.948,12.793,7.183L404.42,581.803z",
            };

            String[] mDateCutOutlineSVG = new String[] {
                    // DIAL_SINGLE_HOLE_VRT = 0
                    "M626.333,378l-104-0.666V262.667" +
                            "l103.333,1.667c0,0,5.1,39.887,5.333,53.333C631.257,332.482,626.333,378,626.333,378z",
                    // DIAL_TRIPLE_HOLE_VRT = 1
                    "M609.333,430.667L574.667,423l-10.333-9.333" +
                            "l-11.334-8l-39.666-12l0.333-151.334l44-6l19-11.333l2.667-8.667L610,215c0,0,21.349,39.311,20.333,106" +
                            "C629.333,386.667,609.333,430.667,609.333,430.667z",
//                    new String(
//                            "M609.333,430.667L574.667,423l-10.333-8.333" +
//                                    "L549.667,414l-36.333-11.333L513,229l57.999-1.333l5.667-2.667l2.667-8.667L610,215c0,0,21.349,39.311,20.333,106" +
//                                    "C629.333,386.667,609.333,430.667,609.333,430.667z"
//                    ),
                    // DIAL_SINGLE_HOLE_HRZ = 2
                    "M396.332,618.334" +
                            "c0,0-34.667,9.312-77,9C273.999,627,248.999,618,248.999,618l0.667-88.333l144-0.667L396.332,618.334z",
                    // DIAL_TRIPLE_HOLE_HRZ = 3
                    "M434.666,607c0,0-44.664,21.347-114,21.667" +
                            "c-72,0.333-113.667-22-113.667-22l28.667-90l161.667-1.333L407.666,544l8,23.333L431.999,583L434.666,607z",
//                    new String(
//                            "M434.666,607" +
//                                    "c0,0-44.664,21.347-114,21.667c-72,0.333-113.667-22-113.667-22l23.667-90l177.667-1.333L417.666,544l-1,23.333L431.999,583" +
//                                    "L434.666,607z"
//                    ),
            };



            String[] mDigitsSVG = new String[] {
                    // 0: 12
                    // 1
                    "M295.025,93c0,0,0.006-39.272,0-41.183c-1.244,0.951-3.47,2.027-4.4,2.409" +
                            "c-0.917,0.376-2.709,1.334-4.624,1.591c-0.001-2.215,0-10.764,0-10.764c2.124-0.243,5.449-2.836,6.457-3.66" +
                            "c0.917-0.75,4.333-3.5,4.763-5.724h9.204V93H295.025z" +
                    // 2
                    "M317.063,48.217c0.806-2.812,2.016-5.269,3.629-7.371" +
                            "c1.612-2.102,3.641-3.739,6.085-4.914c2.445-1.174,5.254-1.761,8.43-1.761c2.42,0,4.727,0.409,6.918,1.229" +
                            "s4.121,1.994,5.783,3.521c1.662,1.529,2.986,3.413,3.969,5.651c0.982,2.239,1.475,4.75,1.475,7.535" +
                            "c0,2.895-0.43,5.378-1.285,7.452c-0.857,2.076-1.992,3.919-3.402,5.528c-1.41,1.611-3.012,3.071-4.801,4.382" +
                            "s-3.59,2.608-5.406,3.891c-1.812,1.283-3.578,2.661-5.291,4.136c-1.713,1.474-3.225,3.194-4.535,5.159h25.023v9.992h-38.858" +
                            "c0-3.33,0.44-6.225,1.322-8.682s2.079-4.654,3.592-6.593c1.512-1.938,3.288-3.727,5.329-5.364" +
                            "c2.043-1.638,4.195-3.303,6.465-4.996c1.158-0.873,2.393-1.761,3.703-2.662c1.311-0.9,2.508-1.896,3.592-2.989" +
                            "c1.084-1.091,1.99-2.319,2.721-3.686c0.73-1.363,1.098-2.92,1.098-4.668c0-2.784-0.746-4.955-2.23-6.511" +
                            "c-1.488-1.557-3.391-2.334-5.707-2.334c-1.564,0-2.887,0.396-3.971,1.188c-1.084,0.792-1.953,1.829-2.607,3.112" +
                            "c-0.656,1.283-1.121,2.702-1.398,4.259c-0.277,1.556-0.416,3.099-0.416,4.627h-10.282" +
                            "C315.903,54.072,316.256,51.029,317.063,48.217z",
//                    new String(
//                            // 1
//                            "M297.025,90c0,0,0.006-39.272,0-41.183c-1.244,0.951-3.47,2.027-4.4,2.409" +
//                            "c-0.917,0.376-2.709,1.334-4.624,1.591c-0.001-2.215,0-10.764,0-10.764c2.124-0.243,5.449-2.836,6.457-3.66" +
//                            "c0.917-0.75,4.333-3.5,4.763-5.724h9.204V90H297.025z" +
//                            // 2
//                            "M314.063,45.217c0.806-2.812,2.016-5.269,3.629-7.371" +
//                            "c1.612-2.102,3.641-3.739,6.085-4.914c2.445-1.174,5.254-1.761,8.43-1.761c2.42,0,4.727,0.409,6.918,1.229" +
//                            "s4.121,1.994,5.783,3.521c1.662,1.529,2.986,3.413,3.969,5.651c0.982,2.239,1.475,4.75,1.475,7.535" +
//                            "c0,2.895-0.43,5.378-1.285,7.452c-0.857,2.076-1.992,3.919-3.402,5.528c-1.41,1.611-3.012,3.071-4.801,4.382" +
//                            "s-3.59,2.608-5.406,3.891c-1.812,1.283-3.578,2.661-5.291,4.136c-1.713,1.474-3.225,3.194-4.535,5.159h25.023v9.992h-38.858" +
//                            "c0-3.33,0.44-6.225,1.322-8.682s2.079-4.654,3.592-6.593c1.512-1.938,3.288-3.727,5.329-5.364" +
//                            "c2.043-1.638,4.195-3.303,6.465-4.996c1.158-0.873,2.393-1.761,3.703-2.662c1.311-0.9,2.508-1.896,3.592-2.989" +
//                            "c1.084-1.091,1.99-2.319,2.721-3.686c0.73-1.363,1.098-2.92,1.098-4.668c0-2.784-0.746-4.955-2.23-6.511" +
//                            "c-1.488-1.557-3.391-2.334-5.707-2.334c-1.564,0-2.887,0.396-3.971,1.188c-1.084,0.792-1.953,1.829-2.607,3.112" +
//                            "c-0.656,1.283-1.121,2.702-1.398,4.259c-0.277,1.556-0.416,3.099-0.416,4.627h-10.282" +
//                            "C312.903,51.072,313.256,48.029,314.063,45.217z"
//                    ),
                    // 1: 1
                    "M463.598,134.665c0,0,0.008-39.272,0-41.183c-1.242,0.951-3.469,2.027-4.398,2.409" +
                            "c-0.918,0.376-2.711,1.334-4.625,1.591c0-2.215,0-10.764,0-10.764c2.125-0.243,5.449-2.836,6.457-3.66" +
                            "c0.918-0.75,4.332-3.5,4.762-5.724H475v57.33H463.598z",
                    // 2: 2
                    "M555.42,176.081c-1.641-1.54-3.76-2.32-6.34-2.32c-1.74,0-3.201,0.38-4.4,1.18" +
                            "s-2.18,1.84-2.9,3.12c-0.719,1.28-1.24,2.7-1.561,4.26c-0.299,1.54-0.459,3.101-0.459,4.62h-11.42" +
                            "c-0.121-3.279,0.279-6.319,1.18-9.14c0.9-2.8,2.24-5.26,4.02-7.36c1.801-2.1,4.061-3.739,6.76-4.92" +
                            "c2.721-1.18,5.842-1.76,9.381-1.76c2.68,0,5.24,0.42,7.68,1.22c2.439,0.82,4.58,2,6.42,3.54c1.861,1.521,3.32,3.4,4.42,5.641" +
                            "c1.08,2.239,1.641,4.76,1.641,7.54c0,2.899-0.48,5.38-1.439,7.439c-0.941,2.08-2.201,3.92-3.781,5.54" +
                            "c-1.559,1.62-3.34,3.06-5.32,4.38c-2,1.32-4,2.601-6.02,3.9c-2.02,1.279-3.959,2.66-5.879,4.12" +
                            "c-1.9,1.479-3.58,3.199-5.041,5.159h27.82v10H527c0-3.34,0.48-6.22,1.459-8.68c0.98-2.46,2.32-4.66,4-6.6" +
                            "c1.682-1.94,3.641-3.721,5.92-5.36c2.262-1.64,4.66-3.3,7.182-5c1.279-0.88,2.658-1.76,4.119-2.66" +
                            "c1.461-0.899,2.779-1.899,3.98-2.979c1.199-1.101,2.219-2.32,3.02-3.7c0.82-1.36,1.221-2.92,1.221-4.66" +
                            "C557.9,179.82,557.08,177.64,555.42,176.081z",
                    // 3: 3
                    "M590.148,314.612c1.262-0.519,2.297-1.27,3.109-2.253" +
                            "c0.811-0.982,1.219-2.293,1.219-3.931c0-2.457-0.84-4.341-2.52-5.651c-1.682-1.311-3.773-1.652-5.957-1.652" +
                            "c-3.023,0-5.25,0.75-6.688,2.593c-1.522,1.953-2.312,5.532-2.225,7.33h-11.34c0.111-2.948,0.658-5.637,1.639-8.066" +
                            "c0.977-2.429,2.352-4.518,4.115-6.266c1.764-1.747,3.877-3.099,6.342-4.054s5.207-1.434,8.23-1.434" +
                            "c2.354,0,4.705,0.342,7.059,1.023c2.352,0.684,4.465,1.707,6.34,3.071c1.877,1.365,3.402,3.03,4.578,4.996" +
                            "s1.766,4.232,1.766,6.798c0,2.784-0.688,5.241-2.059,7.371c-1.373,2.129-1.758,2.761-4.008,4.386l0.125,0.5" +
                            "c2.875,1.125,3.504,1.857,5.352,4.205s2.773,5.16,2.773,8.436c0,3.004-0.602,5.68-1.807,8.025" +
                            "c-1.205,2.348-2.814,4.314-4.83,5.898c-2.016,1.582-4.34,2.785-6.973,3.602c-2.633,0.82-5.375,1.23-8.23,1.23" +
                            "c-3.305,0-6.316-0.465-9.031-1.393s-5.027-2.279-6.93-4.055c-1.904-1.773-3.375-3.943-4.41-6.51s-1.527-5.516-1.469-8.848h11.34" +
                            "c0.055,1.531,0.215,5.409,2.688,7.986c2.488,2.593,4.535,2.901,7.402,2.922c2.688,0.02,5.25-0.75,7.047-2.022" +
                            "c2-1.416,2.77-3.809,2.77-6.592c0-2.184-0.434-3.85-1.301-4.996c-0.869-1.146-1.973-1.979-3.318-2.498" +
                            "c-1.344-0.52-5.744-1.017-7.197-1.017l0.125-8.375C585.16,315.485,588.891,315.131,590.148,314.612z",
                    // 4: 4
                    "M524,465.333V456l20.667-34.5l17.537-0.164l-0.037,33.831h6.667l0.182,10.232" +
                            "h-6.812v13.266h-11.34v-13.266L524,465.333z M536.333,455h14.5v-24.167L536.333,455z",
                    // 5: 5
                    "M467.92,562.061c-1.182,2.541-2.76,4.74-4.74,6.6c-2,1.861-4.301,3.281-6.941,4.301" +
                            "c-2.619,1-5.42,1.48-8.398,1.42c-2.861,0-5.58-0.359-8.18-1.1c-2.602-0.74-4.922-1.859-6.941-3.4c-2-1.52-3.619-3.42-4.82-5.68" +
                            "c-1.199-2.279-1.84-4.9-1.898-7.92h11.939c0.279,2.619,1.279,4.719,3.02,6.279s3.939,2.32,6.641,2.32" +
                            "c1.561,0,2.98-0.301,4.24-0.939c1.26-0.621,2.32-1.441,3.18-2.461c0.879-1,1.539-2.18,2.02-3.52s0.721-2.699,0.721-4.119" +
                            "c0-1.48-0.221-2.881-0.68-4.182c-0.441-1.318-1.121-2.459-2-3.439c-0.9-0.98-1.961-1.76-3.201-2.299" +
                            "c-1.24-0.541-2.66-0.82-4.279-0.82c-2.141,0-3.861,0.359-5.221,1.1c-1.34,0.74-2.6,1.9-3.779,3.48h-10.74L432.25,516h34.25v9.58" +
                            "h-24.281l-1.344,9.545V536c2.625-1.5,5.375-1.75,10.664-1.658c2.919,0.051,5.48,0.52,7.74,1.559" +
                            "c2.221,1.041,4.119,2.42,5.66,4.18c1.539,1.74,2.721,3.801,3.52,6.182c0.82,2.359,1.221,4.898,1.221,7.58" +
                            "C469.68,556.781,469.1,559.521,467.92,562.061z",
                    // 6: 6
                    "M325.963,559.891c-1.588-1.266-3.018-1.766-5.088-1.766" +
                            "c-2.129,0-4.25,0.375-5.453,1.48c-1.388,1.275-2.562,2.73-3.402,4.504c-0.84,1.775-1.457,3.672-1.848,5.691" +
                            "c-0.393,2.021-0.617,1.824-0.672,3.406l0.168,0.164c1.582-1.621,2.275-2.145,3.832-2.871c1.875-0.875,3.5-1.625,7.125-1.625" +
                            "c2.52,0,7.299,0.641,9.707,1.705c2.406,1.064,4.451,2.498,6.131,4.299c1.566,1.75,2.715,3.742,3.445,5.98" +
                            "c0.727,2.238,1.092,4.504,1.092,6.797c0,2.949-0.49,5.691-1.471,8.23s-2.381,4.75-4.199,6.633c-1.82,1.885-3.99,3.359-6.512,4.424" +
                            "c-2.52,1.064-5.32,1.596-8.398,1.596c-4.425,0-8.093-0.859-11.004-2.578c-2.913-1.721-5.237-3.973-6.973-6.758" +
                            "c-1.736-2.785-2.939-5.938-3.611-9.459c-0.673-3.521-1.009-7.111-1.009-10.77c0-3.549,0.42-7.098,1.261-10.646" +
                            "c0.84-3.549,2.184-6.758,4.031-9.625c1.849-2.865,4.228-5.199,7.141-7c2.911-1.805,6.468-2.703,10.668-2.703" +
                            "c2.52,0,4.871,0.355,7.055,1.062c2.184,0.711,4.117,1.734,5.797,3.072s3.051,2.99,4.117,4.955" +
                            "c1.062,1.965,1.762,4.232,2.098,6.797l-10.615-0.012C328.875,563.125,327.522,561.134,325.963,559.891z M315.926,578.605" +
                            "c-1.205,0.574-2.198,1.352-2.982,2.336c-0.784,0.98-1.373,2.115-1.764,3.398c-0.393,1.281-0.588,2.633-0.588,4.053" +
                            "c0,1.311,0.21,2.607,0.63,3.891s1.035,2.43,1.848,3.439c0.812,1.012,1.807,1.816,2.982,2.416c1.176,0.602,2.52,0.9,4.032,0.9" +
                            "c1.455,0,2.73-0.299,3.82-0.9c1.094-0.6,2.031-1.393,2.816-2.375c0.781-0.982,1.371-2.1,1.762-3.357" +
                            "c0.393-1.256,0.59-2.539,0.59-3.85c0-1.365-0.184-2.689-0.547-3.971c-0.363-1.283-0.91-2.43-1.637-3.441" +
                            "c-0.73-1.01-1.668-1.828-2.816-2.457c-1.148-0.627-2.477-0.941-3.988-0.941C318.515,577.746,317.129,578.031,315.926,578.605z",
                    // 7: 7
                    "M210.57,524.33c-5.862,8.583-10.806,16.67-13.473,24.003c-3.173,8.726-5.707,20.516-5.766,24.336" +
                            "H178.57c0.681-8.58,2.945-16.734,6.181-24.74c2.989-7.395,6.68-14.93,11.68-21.781h-26V515.33h40.14V524.33z",
//                    new String(
//                            "M209.14,524.33c-5.862,8.583-10.806,16.67-13.473,24.003c-3.173,8.726-5.707,20.516-5.766,24.336" +
//                            "H177.14c0.681-8.58,2.945-16.734,6.181-24.74c2.989-7.395,6.68-14.93,11.68-21.781h-26V515.33h40.14V524.33z"
//                    ),
                    // 8: 8
                    "M75.875,428.375c3.625-4.625,5.125-5.708,10.245-7.162" +
                            "c2.35-0.667,4.731-0.984,7.14-0.984c3.64,0,6.795,0.389,9.24,1.639c4.5,2.299,5.167,2.633,7.833,6.299" +
                            "c1.117,1.535,2.292,5.458,2.333,7.833c0.05,2.839-0.739,5.496-2.167,7.625s-1.375,2-3.125,3.75V448" +
                            "c2.875,1.625,3.217,2.219,5.121,4.703c1.903,2.484,2.856,5.529,2.856,9.131c0,3.059-0.63,5.707-1.891,7.945" +
                            "c-1.26,2.238-2.927,4.094-4.998,5.57c-2.072,1.473-4.424,2.578-7.056,3.316c-2.633,0.736-5.321,1.105-8.064,1.105" +
                            "c-2.855,0-5.614-0.344-8.273-1.023c-2.66-0.684-5.04-1.762-7.14-3.236c-2.101-1.475-3.78-3.33-5.04-5.568" +
                            "C71.63,467.705,71,465.029,71,461.916c0-3.656,0.966-6.715,2.898-9.172c1.932-2.457,1.953-3.202,4.977-4.744L79,447.5" +
                            "c-1.25-1.125-1.924-2.045-3.38-4.174c-1.457-2.129-1.787-4.486-1.787-7.326C73.833,433.434,75.075,429.396,75.875,428.375z" +
                            "M83.769,465.357c0.559,1.199,1.302,2.225,2.226,3.07c0.924,0.848,2.029,1.502,3.318,1.965c1.287,0.465,2.631,0.695,4.031,0.695" +
                            "c1.456,0,2.785-0.23,3.99-0.695c1.204-0.463,2.269-1.117,3.192-1.965c0.924-0.846,1.638-1.871,2.142-3.07" +
                            "c0.504-1.201,0.756-2.512,0.756-3.934c0-1.363-0.266-2.605-0.798-3.727c-0.532-1.117-1.26-2.086-2.184-2.906" +
                            "c-0.924-0.818-1.988-1.445-3.192-1.883c-1.205-0.438-2.507-0.656-3.906-0.656c-2.912,0-5.376,0.777-7.392,2.336" +
                            "c-2.016,1.555-3.024,3.807-3.024,6.754C82.928,462.818,83.208,464.156,83.769,465.357z M86.96,442.547" +
                            "c1.669,1.439,3.892,1.924,6.3,1.924c2.521,0,4.62-0.641,6.301-1.924c1.68-1.283,2.005-2.925,2.106-5.38" +
                            "c0.086-2.104-0.582-4.489-1.224-5.307c-0.645-0.818-1.541-1.502-2.688-2.049c-1.148-0.543-2.646-0.816-4.494-0.816" +
                            "c-1.176,0-2.31,0.176-3.401,0.531s-2.059,0.859-2.898,1.516c-0.84,0.654-2.232,3.188-2.293,6.125" +
                            "C84.615,439.623,85.167,441,86.96,442.547z",
                    // 9: 9
                    "M47.036,339.96c1.589,1.415,3.267,1.79,5.339,1.79c2.185,0,3.625-0.25,5.245-1.503" +
                            "c1.473-1.139,2.521-2.73,3.36-4.506c0.84-1.773,2.157-6.322,2.27-7.908l-0.083-0.5c-1.957,1.771-5.083,3.5-11.417,3.542" +
                            "c-4.167,0.027-6.675-0.538-9.082-1.603c-2.408-1.066-4.451-2.498-6.132-4.301c-1.568-1.746-2.717-3.738-3.443-5.979" +
                            "c-0.729-2.237-1.093-4.504-1.093-6.797c0-2.949,0.49-5.692,1.471-8.231c0.979-2.539,2.379-4.75,4.199-6.634" +
                            "c1.819-1.884,3.99-3.371,6.511-4.464c2.52-1.091,5.319-1.638,8.399-1.638c4.479,0,8.161,0.86,11.046,2.58" +
                            "c2.884,1.72,5.194,3.986,6.931,6.798c1.734,2.812,2.939,5.992,3.611,9.541c0.672,3.55,1.009,7.125,1.009,10.729" +
                            "c0,3.55-0.421,7.099-1.261,10.647s-2.184,6.756-4.031,9.623c-1.849,2.867-4.229,5.188-7.141,6.961" +
                            "c-2.912,1.773-6.468,2.662-10.668,2.662c-2.52,0-4.872-0.342-7.056-1.023c-2.185-0.682-4.116-1.705-5.796-3.072" +
                            "c-1.681-1.363-3.053-3.029-4.116-4.996c-1.064-1.965-1.764-4.203-2.1-6.715l10.866-0.089" +
                            "C44.5,336.75,45.548,338.634,47.036,339.96z M57.116,321.245c1.176-0.572,2.155-1.351,2.94-2.333" +
                            "c0.783-0.983,1.371-2.116,1.764-3.399c0.392-1.282,0.588-2.634,0.588-4.054c0-1.311-0.21-2.607-0.63-3.891" +
                            "c-0.42-1.282-1.037-2.429-1.848-3.439c-0.812-1.01-1.807-1.814-2.982-2.416c-1.176-0.601-2.52-0.901-4.032-0.901" +
                            "c-1.4,0-2.66,0.301-3.779,0.901c-1.121,0.602-2.073,1.392-2.856,2.375c-0.785,0.982-1.373,2.103-1.764,3.357" +
                            "c-0.393,1.257-0.588,2.539-0.588,3.85c0,1.365,0.181,2.689,0.546,3.973c0.363,1.283,0.909,2.43,1.638,3.439" +
                            "c0.728,1.011,1.666,1.83,2.814,2.456c1.146,0.629,2.478,0.941,3.989,0.941C54.54,322.104,55.94,321.819,57.116,321.245z",
                    // 10: 10
                    // 1
                    "M79.025,222.059c0,0,0.006-39.272,0-41.183c-1.244,0.951-3.47,2.027-4.4,2.409c-0.917,0.376-2.709,1.334-4.624,1.591" +
                            "c-0.001-2.215,0-10.764,0-10.764c2.124-0.243,5.449-2.836,6.457-3.66c0.917-0.75,4.333-3.5,4.763-5.724h9.204v57.33H79.025z" +
                    // 0
                    "M101.345,178.749c1.032-3.794,2.431-6.838,4.195-9.132" +
                    "c1.764-2.293,3.818-3.931,6.161-4.913c2.344-0.983,4.826-1.475,7.447-1.475c2.671,0,5.179,0.491,7.522,1.475" +
                    "c2.343,0.982,4.409,2.62,6.199,4.913c1.788,2.294,3.199,5.338,4.233,9.132c1.032,3.796,1.55,8.478,1.55,14.046" +
                    "c0,5.733-0.518,10.524-1.55,14.374c-1.034,3.85-2.445,6.92-4.233,9.214c-1.79,2.293-3.856,3.931-6.199,4.914" +
                    "c-2.344,0.982-4.852,1.474-7.522,1.474c-2.621,0-5.104-0.491-7.447-1.474c-2.343-0.983-4.397-2.621-6.161-4.914" +
                    "c-1.765-2.294-3.163-5.364-4.195-9.214c-1.034-3.85-1.55-8.641-1.55-14.374C99.795,187.227,100.311,182.545,101.345,178.749z" +
                    "M110.681,198.733c0.101,2.321,0.416,4.56,0.945,6.716c0.529,2.157,1.373,4,2.532,5.528c1.159,1.529,3.466,2.272,4.99,2.293" +
                    "c1.476,0.02,3.918-0.764,5.103-2.293c1.184-1.528,2.041-3.371,2.57-5.528c0.529-2.156,0.844-4.395,0.945-6.716" +
                    "c0.101-2.32,0.151-4.3,0.151-5.938c0-0.982-0.013-2.17-0.038-3.562c-0.025-1.392-0.126-2.825-0.303-4.3" +
                    "c-0.177-1.474-0.429-2.934-0.756-4.381c-0.328-1.446-0.819-2.744-1.474-3.891c-0.656-1.146-3.098-3.789-6.199-3.85" +
                    "c-3.149-0.062-5.456,2.703-6.086,3.85c-0.631,1.146-1.122,2.444-1.475,3.891c-0.353,1.447-0.604,2.907-0.756,4.381" +
                    "c-0.151,1.475-0.239,2.908-0.265,4.3c-0.025,1.393-0.037,2.58-0.037,3.562C110.531,194.434,110.58,196.413,110.681,198.733z",
                    // 11: 11
                    // R1
                    "M163.188,134.665c0,0,0.006-39.272,0-41.183" +
                    "c-1.244,0.951-3.47,2.027-4.4,2.409c-0.917,0.376-2.709,1.334-4.624,1.591c-0.001-2.215,0-10.764,0-10.764" +
                    "c2.124-0.243,5.449-2.836,6.457-3.66c0.917-0.75,4.333-3.5,4.763-5.724h9.204v57.33H163.188z" +
                    // L1
                    "M185.601,134.665c0,0,0.006-39.272,0-41.183" +
                    "c-1.244,0.951-3.47,2.027-4.4,2.409c-0.917,0.376-2.709,1.334-4.624,1.591c-0.001-2.215,0-10.764,0-10.764" +
                    "c2.124-0.243,5.449-2.836,6.457-3.66c0.917-0.75,4.333-3.5,4.763-5.724H197v57.33H185.601z",
            };
            //

/*
		<path id="rdash11" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M188.678,77.247l20.5,35.507
			c2.21,3.828,0.899,8.718-2.928,10.928c-1.914,1.105-4.096,1.325-6.072,0.804c-1.972-0.536-3.751-1.818-4.856-3.732l-20.5-35.507
			c-2.21-3.828-0.899-8.718,2.928-10.928c1.914-1.105,4.096-1.326,6.072-0.804C185.794,74.051,187.573,75.333,188.678,77.247z"/>
		<path id="rdash10" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M84.493,175.5L120,196
			c3.828,2.21,5.138,7.101,2.928,10.928c-1.105,1.914-2.885,3.196-4.856,3.732c-1.976,0.522-4.158,0.301-6.072-0.804l-35.507-20.5
			c-3.828-2.21-5.138-7.101-2.928-10.928c1.105-1.914,2.884-3.196,4.856-3.732C80.397,174.174,82.579,174.395,84.493,175.5z"/>
		<path id="rdash9" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M43.5,312.5h41c4.42,0,8,3.58,8,8
			c0,2.21-0.9,4.21-2.34,5.66c-1.45,1.44-3.45,2.34-5.66,2.34h-41c-4.42,0-8-3.58-8-8c0-2.21,0.9-4.21,2.34-5.66
			C39.29,313.4,41.29,312.5,43.5,312.5z"/>
		<path id="rdash8_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M119.753,445.178l-35.507,20.5
			c-3.828,2.21-8.718,0.899-10.928-2.928c-1.105-1.914-1.325-4.096-0.804-6.072c0.536-1.972,1.818-3.751,3.732-4.856l35.507-20.5
			c3.828-2.21,8.718-0.899,10.928,2.928c1.105,1.914,1.326,4.096,0.804,6.072C122.949,442.294,121.667,444.073,119.753,445.178z"/>
		<path id="rdash7_2_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M209.178,527.747l-20.5,35.507
			c-2.21,3.828-7.101,5.138-10.928,2.928c-1.914-1.105-3.196-2.885-3.732-4.856c-0.522-1.976-0.301-4.158,0.804-6.072l20.5-35.507
			c2.21-3.828,7.101-5.138,10.928-2.928c1.914,1.105,3.196,2.884,3.732,4.856C210.504,523.65,210.283,525.833,209.178,527.747z"/>
		<path id="rdash6_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M328,556v41c0,4.42-3.58,8-8,8
			c-2.21,0-4.21-0.9-5.66-2.34c-1.44-1.45-2.34-3.45-2.34-5.66v-41c0-4.42,3.58-8,8-8c2.21,0,4.21,0.9,5.66,2.34
			C327.1,551.79,328,553.79,328,556z"/>
		<path id="rdash5_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M444.678,520.247l20.5,35.507
			c2.21,3.828,0.899,8.718-2.928,10.928c-1.914,1.105-4.096,1.325-6.072,0.804c-1.972-0.536-3.751-1.818-4.856-3.732l-20.5-35.507
			c-2.21-3.828-0.899-8.718,2.928-10.928c1.914-1.105,4.096-1.326,6.072-0.804C441.794,517.051,443.573,518.333,444.678,520.247z"/>
		<path id="rdash4_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M528.247,431.322l35.507,20.5
			c3.828,2.21,5.138,7.101,2.928,10.928c-1.105,1.914-2.885,3.196-4.856,3.732c-1.976,0.522-4.158,0.301-6.072-0.804l-35.507-20.5
			c-3.828-2.21-5.138-7.101-2.928-10.928c1.105-1.914,2.884-3.196,4.856-3.732C524.15,429.996,526.333,430.217,528.247,431.322z"/>
		<path id="rdash3_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M555.5,311.5h41c4.42,0,8,3.58,8,8
			c0,2.21-0.9,4.21-2.34,5.66c-1.45,1.44-3.45,2.34-5.66,2.34h-41c-4.42,0-8-3.58-8-8c0-2.21,0.9-4.21,2.34-5.66
			C551.29,312.4,553.29,311.5,555.5,311.5z"/>
		<path id="rdash2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M563.753,189.178l-35.507,20.5
			c-3.828,2.21-8.718,0.899-10.928-2.928c-1.105-1.914-1.325-4.096-0.804-6.072c0.536-1.972,1.818-3.751,3.732-4.856l35.507-20.5
			c3.828-2.21,8.718-0.899,10.928,2.928c1.105,1.914,1.326,4.096,0.804,6.072C566.949,186.294,565.667,188.073,563.753,189.178z"/>
		<path id="rdash1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M465.178,84.747l-20.5,35.507
			c-2.21,3.828-7.101,5.138-10.928,2.928c-1.914-1.105-3.196-2.885-3.732-4.856c-0.522-1.976-0.301-4.158,0.804-6.072l20.5-35.507
			c2.21-3.828,7.101-5.138,10.928-2.928c1.914,1.105,3.196,2.884,3.732,4.856C466.504,80.65,466.283,82.833,465.178,84.747z"/>
		<path id="rdash12" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M328,43v41c0,4.42-3.58,8-8,8
			c-2.21,0-4.21-0.9-5.66-2.34C312.9,88.21,312,86.21,312,84V43c0-4.42,3.58-8,8-8c2.21,0,4.21,0.9,5.66,2.34
			C327.1,38.79,328,40.79,328,43z"/>
*/
            String[] mRoundedDashSVG = new String[] {
                    // 12
                    "M328,43v41c0,4.42-3.58,8-8,8" +
                            "c-2.21,0-4.21-0.9-5.66-2.34C312.9,88.21,312,86.21,312,84V43c0-4.42,3.58-8,8-8c2.21,0,4.21,0.9,5.66,2.34" +
                            "C327.1,38.79,328,40.79,328,43z",
                    // 1
                    "M465.178,84.747l-20.5,35.507" +
                            "c-2.21,3.828-7.101,5.138-10.928,2.928c-1.914-1.105-3.196-2.885-3.732-4.856c-0.522-1.976-0.301-4.158,0.804-6.072l20.5-35.507" +
                            "c2.21-3.828,7.101-5.138,10.928-2.928c1.914,1.105,3.196,2.884,3.732,4.856C466.504,80.65,466.283,82.833,465.178,84.747z",
                    // 2
                    "M563.753,189.178l-35.507,20.5" +
                            "c-3.828,2.21-8.718,0.899-10.928-2.928c-1.105-1.914-1.325-4.096-0.804-6.072c0.536-1.972,1.818-3.751,3.732-4.856l35.507-20.5" +
                            "c3.828-2.21,8.718-0.899,10.928,2.928c1.105,1.914,1.326,4.096,0.804,6.072C566.949,186.294,565.667,188.073,563.753,189.178z",
                    // 3
                    "M555.5,311.5h41c4.42,0,8,3.58,8,8" +
                            "c0,2.21-0.9,4.21-2.34,5.66c-1.45,1.44-3.45,2.34-5.66,2.34h-41c-4.42,0-8-3.58-8-8c0-2.21,0.9-4.21,2.34-5.66" +
                            "C551.29,312.4,553.29,311.5,555.5,311.5z",
                    // 4
                    "M528.247,431.322l35.507,20.5" +
                            "c3.828,2.21,5.138,7.101,2.928,10.928c-1.105,1.914-2.885,3.196-4.856,3.732c-1.976,0.522-4.158,0.301-6.072-0.804l-35.507-20.5" +
                            "c-3.828-2.21-5.138-7.101-2.928-10.928c1.105-1.914,2.884-3.196,4.856-3.732C524.15,429.996,526.333,430.217,528.247,431.322z",
                    // 5
                    "M444.678,520.247l20.5,35.507" +
                            "c2.21,3.828,0.899,8.718-2.928,10.928c-1.914,1.105-4.096,1.325-6.072,0.804c-1.972-0.536-3.751-1.818-4.856-3.732l-20.5-35.507" +
                            "c-2.21-3.828-0.899-8.718,2.928-10.928c1.914-1.105,4.096-1.326,6.072-0.804C441.794,517.051,443.573,518.333,444.678,520.247z",
                    // 6
                    "M328,556v41c0,4.42-3.58,8-8,8" +
                            "c-2.21,0-4.21-0.9-5.66-2.34c-1.44-1.45-2.34-3.45-2.34-5.66v-41c0-4.42,3.58-8,8-8c2.21,0,4.21,0.9,5.66,2.34" +
                            "C327.1,551.79,328,553.79,328,556z",
                    // 7
                    "M209.178,527.747l-20.5,35.507" +
                            "c-2.21,3.828-7.101,5.138-10.928,2.928c-1.914-1.105-3.196-2.885-3.732-4.856c-0.522-1.976-0.301-4.158,0.804-6.072l20.5-35.507" +
                            "c2.21-3.828,7.101-5.138,10.928-2.928c1.914,1.105,3.196,2.884,3.732,4.856C210.504,523.65,210.283,525.833,209.178,527.747z",
                    // 8
                    "M119.753,445.178l-35.507,20.5" +
                            "c-3.828,2.21-8.718,0.899-10.928-2.928c-1.105-1.914-1.325-4.096-0.804-6.072c0.536-1.972,1.818-3.751,3.732-4.856l35.507-20.5" +
                            "c3.828-2.21,8.718-0.899,10.928,2.928c1.105,1.914,1.326,4.096,0.804,6.072C122.949,442.294,121.667,444.073,119.753,445.178z",
                    // 9
                    "M43.5,312.5h41c4.42,0,8,3.58,8,8" +
                            "c0,2.21-0.9,4.21-2.34,5.66c-1.45,1.44-3.45,2.34-5.66,2.34h-41c-4.42,0-8-3.58-8-8c0-2.21,0.9-4.21,2.34-5.66" +
                            "C39.29,313.4,41.29,312.5,43.5,312.5z",
                    // 10
                    "M84.493,175.5L120,196" +
                            "c3.828,2.21,5.138,7.101,2.928,10.928c-1.105,1.914-2.885,3.196-4.856,3.732c-1.976,0.522-4.158,0.301-6.072-0.804l-35.507-20.5" +
                            "c-3.828-2.21-5.138-7.101-2.928-10.928c1.105-1.914,2.884-3.196,4.856-3.732C80.397,174.174,82.579,174.395,84.493,175.5z",
                    // 11
                    "M188.678,77.247l20.5,35.507" +
                            "c2.21,3.828,0.899,8.718-2.928,10.928c-1.914,1.105-4.096,1.325-6.072,0.804c-1.972-0.536-3.751-1.818-4.856-3.732l-20.5-35.507" +
                            "c-2.21-3.828-0.899-8.718,2.928-10.928c1.914-1.105,4.096-1.326,6.072-0.804C185.794,74.051,187.573,75.333,188.678,77.247z",
            };

            
/*
		<path id="rt-12" style="fill:#FFFFFF;" d="M302.22,34c0-2.49,2.01-4.5,4.5-4.5h26.58c2.49,0,4.5,2.01,4.5,4.5
			c0,0.78-0.2,1.51-0.54,2.14l-13.04,24.49c0,0-0.825,3.37-4.2,3.37c-3.125,0-4.2-3.37-4.2-3.37l-12.91-24.24l-0.34-0.64
			C302.34,35.21,302.22,34.62,302.22,34z"/>
		<path id="rt-11" style="fill:#FFFFFF;" d="M161.229,80.603c-1.245-2.157-0.509-4.902,1.647-6.147l23.019-13.29
			c2.156-1.245,4.902-0.509,6.147,1.647c0.39,0.675,0.582,1.407,0.603,2.123l0.952,27.729c0,0,0.97,3.332-1.953,5.019
			c-2.706,1.562-5.322-0.819-5.322-0.819l-23.3-14.537l-0.615-0.384C161.937,81.591,161.539,81.14,161.229,80.603z"/>
		<path id="rt-10" style="fill:#FFFFFF;" d="M63.073,192.781c-2.157-1.245-2.892-3.99-1.647-6.147l13.29-23.019
			c1.245-2.157,3.991-2.892,6.147-1.647c0.675,0.39,1.208,0.928,1.583,1.537l14.688,23.538c0,0,2.506,2.399,0.819,5.322
			c-1.562,2.707-5.019,1.953-5.019,1.953l-27.447-0.94l-0.725-0.025C64.181,193.283,63.61,193.091,63.073,192.781z"/>
		<path id="rt-9" style="fill:#FFFFFF;" d="M34.26,338.54c-2.49,0-4.5-2.01-4.5-4.5v-26.58c0-2.49,2.01-4.5,4.5-4.5
			c0.78,0,1.51,0.2,2.141,0.54l24.489,13.04c0,0,3.37,0.825,3.37,4.2c0,3.125-3.37,4.2-3.37,4.2L36.65,337.85l-0.641,0.341
			C35.47,338.42,34.88,338.54,34.26,338.54z"/>
		<path id="rt-8" style="fill:#FFFFFF;" d="M80.863,478.031c-2.157,1.245-4.902,0.51-6.147-1.647l-13.29-23.019
			c-1.245-2.157-0.51-4.902,1.647-6.147c0.675-0.39,1.407-0.582,2.123-0.603l27.729-0.952c0,0,3.331-0.971,5.019,1.952
			c1.562,2.707-0.818,5.323-0.818,5.323l-14.538,23.3l-0.384,0.615C81.851,477.323,81.4,477.721,80.863,478.031z"/>
		<path id="rt-7" style="fill:#FFFFFF;" d="M191.542,578.187c-1.245,2.157-3.991,2.893-6.147,1.647l-23.019-13.29
			c-2.156-1.245-2.892-3.99-1.647-6.147c0.39-0.675,0.928-1.208,1.538-1.583l23.538-14.689c0,0,2.4-2.506,5.323-0.818
			c2.706,1.562,1.952,5.019,1.952,5.019l-0.939,27.447l-0.025,0.725C192.043,577.079,191.852,577.65,191.542,578.187z"/>
		<path id="rt-6" style="fill:#FFFFFF;" d="M337.8,606.5c0,2.49-2.01,4.5-4.5,4.5h-26.58c-2.49,0-4.5-2.01-4.5-4.5
			c0-0.779,0.2-1.51,0.54-2.141l13.04-24.489c0,0,0.825-3.37,4.2-3.37c3.125,0,4.2,3.37,4.2,3.37l12.91,24.239l0.34,0.641
			C337.68,605.29,337.8,605.88,337.8,606.5z"/>
		<path id="rt-5" style="fill:#FFFFFF;" d="M479.292,559.897c1.245,2.157,0.509,4.902-1.647,6.147l-23.019,13.29
			c-2.156,1.245-4.902,0.509-6.147-1.647c-0.39-0.675-0.582-1.407-0.603-2.123l-0.952-27.729c0,0-0.97-3.332,1.953-5.019
			c2.706-1.562,5.322,0.819,5.322,0.819l23.3,14.537l0.615,0.384C478.583,558.909,478.981,559.36,479.292,559.897z"/>
		<path id="rt-4" style="fill:#FFFFFF;" d="M577.485,447.257c2.156,1.245,2.893,3.991,1.647,6.147l-13.29,23.019
			c-1.246,2.156-3.99,2.892-6.147,1.647c-0.675-0.39-1.208-0.928-1.583-1.538l-14.689-23.538c0,0-2.506-2.4-0.818-5.323
			c1.562-2.706,5.019-1.952,5.019-1.952l27.447,0.939l0.725,0.025C576.378,446.756,576.949,446.947,577.485,447.257z"/>
		<path id="rt-3" style="fill:#FFFFFF;" d="M605.76,301.96c2.49,0,4.5,2.01,4.5,4.5v26.58c0,2.49-2.01,4.5-4.5,4.5
			c-0.779,0-1.51-0.2-2.14-0.54l-24.49-13.04c0,0-3.37-0.825-3.37-4.2c0-3.125,3.37-4.2,3.37-4.2l24.24-12.91l0.64-0.34
			C604.55,302.08,605.14,301.96,605.76,301.96z"/>
		<path id="rt-2" style="fill:#FFFFFF;" d="M559.157,161.969c2.157-1.245,4.902-0.51,6.147,1.647l13.29,23.019
			c1.245,2.157,0.51,4.902-1.647,6.147c-0.675,0.39-1.407,0.582-2.123,0.603l-27.729,0.952c0,0-3.331,0.971-5.019-1.952
			c-1.562-2.707,0.818-5.323,0.818-5.323l14.538-23.3l0.384-0.615C558.169,162.677,558.62,162.279,559.157,161.969z"/>
		<path id="rt-1" style="fill:#FFFFFF;" d="M447.979,62.813c1.245-2.157,3.991-2.893,6.147-1.647l23.019,13.29
			c2.156,1.245,2.892,3.99,1.647,6.147c-0.39,0.675-0.928,1.208-1.538,1.583l-23.538,14.689c0,0-2.4,2.506-5.323,0.818
			c-2.706-1.562-1.952-5.019-1.952-5.019l0.939-27.447l0.025-0.725C447.477,63.921,447.668,63.351,447.979,62.813z"/>
			
			
		<path id="rt-12" style="fill:#FFFFFF;" d="M302.22,57c0-2.49,2.01-4.5,4.5-4.5h26.58c2.49,0,4.5,2.01,4.5,4.5
			c0,0.78-0.2,1.51-0.54,2.14l-13.04,24.49c0,0-0.825,3.37-4.2,3.37c-3.125,0-4.2-3.37-4.2-3.37l-12.91-24.24l-0.34-0.64
			C302.34,58.21,302.22,57.62,302.22,57z"/>
		<path id="rt-11" style="fill:#FFFFFF;" d="M173.229,100.603c-1.245-2.157-0.509-4.902,1.647-6.147l23.019-13.29
			c2.156-1.245,4.902-0.509,6.147,1.647c0.39,0.675,0.582,1.407,0.603,2.123l0.952,27.729c0,0,0.97,3.332-1.953,5.019
			c-2.706,1.562-5.322-0.819-5.322-0.819l-23.3-14.537l-0.615-0.384C173.937,101.591,173.539,101.14,173.229,100.603z"/>
		<path id="rt-10" style="fill:#FFFFFF;" d="M83.073,203.781c-2.157-1.245-2.892-3.99-1.647-6.147l13.29-23.019
			c1.245-2.157,3.991-2.892,6.147-1.647c0.675,0.39,1.208,0.928,1.583,1.537l14.688,23.538c0,0,2.506,2.399,0.819,5.322
			c-1.562,2.707-5.019,1.953-5.019,1.953l-27.447-0.94l-0.725-0.025C84.181,204.283,83.61,204.091,83.073,203.781z"/>
		<path id="rt-9" style="fill:#FFFFFF;" d="M57.26,338.54c-2.49,0-4.5-2.01-4.5-4.5v-26.58c0-2.49,2.01-4.5,4.5-4.5
			c0.78,0,1.51,0.2,2.141,0.54l24.489,13.04c0,0,3.37,0.825,3.37,4.2c0,3.125-3.37,4.2-3.37,4.2L59.65,337.85l-0.641,0.341
			C58.47,338.42,57.88,338.54,57.26,338.54z"/>
		<path id="rt-8" style="fill:#FFFFFF;" d="M101.863,467.031c-2.157,1.245-4.902,0.51-6.147-1.647l-13.29-23.019
			c-1.245-2.157-0.51-4.902,1.647-6.147c0.675-0.39,1.407-0.582,2.123-0.603l27.729-0.952c0,0,3.331-0.971,5.019,1.952
			c1.562,2.707-0.818,5.323-0.818,5.323l-14.538,23.3l-0.384,0.615C102.851,466.323,102.4,466.721,101.863,467.031z"/>
		<path id="rt-7" style="fill:#FFFFFF;" d="M204.542,555.187c-1.245,2.157-3.991,2.893-6.147,1.647l-23.019-13.29
			c-2.156-1.245-2.892-3.99-1.647-6.147c0.39-0.675,0.928-1.208,1.538-1.583l23.538-14.689c0,0,2.4-2.506,5.323-0.818
			c2.706,1.562,1.952,5.019,1.952,5.019l-0.939,27.447l-0.025,0.725C205.043,554.079,204.852,554.65,204.542,555.187z"/>
		<path id="rt-6" style="fill:#FFFFFF;" d="M337.8,581.5c0,2.49-2.01,4.5-4.5,4.5h-26.58c-2.49,0-4.5-2.01-4.5-4.5
			c0-0.779,0.2-1.51,0.54-2.141l13.04-24.489c0,0,0.825-3.37,4.2-3.37c3.125,0,4.2,3.37,4.2,3.37l12.91,24.239l0.34,0.641
			C337.68,580.29,337.8,580.88,337.8,581.5z"/>
		<path id="rt-5" style="fill:#FFFFFF;" d="M466.292,538.897c1.245,2.157,0.509,4.902-1.647,6.147l-23.019,13.29
			c-2.156,1.245-4.902,0.509-6.147-1.647c-0.39-0.675-0.582-1.407-0.603-2.123l-0.952-27.729c0,0-0.97-3.332,1.953-5.019
			c2.706-1.562,5.322,0.819,5.322,0.819l23.3,14.537l0.615,0.384C465.583,537.909,465.981,538.36,466.292,538.897z"/>
		<path id="rt-4" style="fill:#FFFFFF;" d="M556.485,436.257c2.156,1.245,2.893,3.991,1.647,6.147l-13.29,23.019
			c-1.246,2.156-3.99,2.892-6.147,1.647c-0.675-0.39-1.208-0.928-1.583-1.538l-14.689-23.538c0,0-2.506-2.4-0.818-5.323
			c1.562-2.706,5.019-1.952,5.019-1.952l27.447,0.939l0.725,0.025C555.378,435.756,555.949,435.947,556.485,436.257z"/>
		<path id="rt-3" style="fill:#FFFFFF;" d="M581.76,301.96c2.49,0,4.5,2.01,4.5,4.5v26.58c0,2.49-2.01,4.5-4.5,4.5
			c-0.779,0-1.51-0.2-2.14-0.54l-24.49-13.04c0,0-3.37-0.825-3.37-4.2c0-3.125,3.37-4.2,3.37-4.2l24.24-12.91l0.64-0.34
			C580.55,302.08,581.14,301.96,581.76,301.96z"/>
		<path id="rt-2" style="fill:#FFFFFF;" d="M539.157,172.969c2.157-1.245,4.902-0.51,6.147,1.647l13.29,23.019
			c1.245,2.157,0.51,4.902-1.647,6.147c-0.675,0.39-1.407,0.582-2.123,0.603l-27.729,0.952c0,0-3.331,0.971-5.019-1.952
			c-1.562-2.707,0.818-5.323,0.818-5.323l14.538-23.3l0.384-0.615C538.169,173.677,538.62,173.279,539.157,172.969z"/>
		<path id="rt-1" style="fill:#FFFFFF;" d="M435.979,83.813c1.245-2.157,3.991-2.893,6.147-1.647l23.019,13.29
			c2.156,1.245,2.892,3.99,1.647,6.147c-0.39,0.675-0.928,1.208-1.538,1.583l-23.538,14.689c0,0-2.4,2.506-5.323,0.818
			c-2.706-1.562-1.952-5.019-1.952-5.019l0.939-27.447l0.025-0.725C435.477,84.921,435.668,84.351,435.979,83.813z"/>

*/
            String[] mRoundedTriangleSVG = new String[] {
                    // 12
                    "M302.22,57c0-2.49,2.01-4.5,4.5-4.5h26.58c2.49,0,4.5,2.01,4.5,4.5" +
                            "c0,0.78-0.2,1.51-0.54,2.14l-13.04,24.49c0,0-0.825,3.37-4.2,3.37c-3.125,0-4.2-3.37-4.2-3.37l-12.91-24.24l-0.34-0.64" +
                            "C302.34,58.21,302.22,57.62,302.22,57z",
                    // 1
                    "M435.979,83.813c1.245-2.157,3.991-2.893,6.147-1.647l23.019,13.29" +
                            "c2.156,1.245,2.892,3.99,1.647,6.147c-0.39,0.675-0.928,1.208-1.538,1.583l-23.538,14.689c0,0-2.4,2.506-5.323,0.818" +
                            "c-2.706-1.562-1.952-5.019-1.952-5.019l0.939-27.447l0.025-0.725C435.477,84.921,435.668,84.351,435.979,83.813z",
                    // 2
                    "M539.157,172.969c2.157-1.245,4.902-0.51,6.147,1.647l13.29,23.019" +
                            "c1.245,2.157,0.51,4.902-1.647,6.147c-0.675,0.39-1.407,0.582-2.123,0.603l-27.729,0.952c0,0-3.331,0.971-5.019-1.952" +
                            "c-1.562-2.707,0.818-5.323,0.818-5.323l14.538-23.3l0.384-0.615C538.169,173.677,538.62,173.279,539.157,172.969z",
                    // 3
                    "M581.76,301.96c2.49,0,4.5,2.01,4.5,4.5v26.58c0,2.49-2.01,4.5-4.5,4.5" +
                            "c-0.779,0-1.51-0.2-2.14-0.54l-24.49-13.04c0,0-3.37-0.825-3.37-4.2c0-3.125,3.37-4.2,3.37-4.2l24.24-12.91l0.64-0.34" +
                            "C580.55,302.08,581.14,301.96,581.76,301.96z",
                    // 4
                    "M556.485,436.257c2.156,1.245,2.893,3.991,1.647,6.147l-13.29,23.019" +
                            "c-1.246,2.156-3.99,2.892-6.147,1.647c-0.675-0.39-1.208-0.928-1.583-1.538l-14.689-23.538c0,0-2.506-2.4-0.818-5.323" +
                            "c1.562-2.706,5.019-1.952,5.019-1.952l27.447,0.939l0.725,0.025C555.378,435.756,555.949,435.947,556.485,436.257z",
                    // 5
                    "M466.292,538.897c1.245,2.157,0.509,4.902-1.647,6.147l-23.019,13.29" +
                            "c-2.156,1.245-4.902,0.509-6.147-1.647c-0.39-0.675-0.582-1.407-0.603-2.123l-0.952-27.729c0,0-0.97-3.332,1.953-5.019" +
                            "c2.706-1.562,5.322,0.819,5.322,0.819l23.3,14.537l0.615,0.384C465.583,537.909,465.981,538.36,466.292,538.897z",
                    // 6
                    "M337.8,581.5c0,2.49-2.01,4.5-4.5,4.5h-26.58c-2.49,0-4.5-2.01-4.5-4.5" +
                            "c0-0.779,0.2-1.51,0.54-2.141l13.04-24.489c0,0,0.825-3.37,4.2-3.37c3.125,0,4.2,3.37,4.2,3.37l12.91,24.239l0.34,0.641" +
                            "C337.68,580.29,337.8,580.88,337.8,581.5z",
                    // 7
                    "M204.542,555.187c-1.245,2.157-3.991,2.893-6.147,1.647l-23.019-13.29" +
                            "c-2.156-1.245-2.892-3.99-1.647-6.147c0.39-0.675,0.928-1.208,1.538-1.583l23.538-14.689c0,0,2.4-2.506,5.323-0.818" +
                            "c2.706,1.562,1.952,5.019,1.952,5.019l-0.939,27.447l-0.025,0.725C205.043,554.079,204.852,554.65,204.542,555.187z",
                    // 8
                    "M101.863,467.031c-2.157,1.245-4.902,0.51-6.147-1.647l-13.29-23.019" +
                            "c-1.245-2.157-0.51-4.902,1.647-6.147c0.675-0.39,1.407-0.582,2.123-0.603l27.729-0.952c0,0,3.331-0.971,5.019,1.952" +
                            "c1.562,2.707-0.818,5.323-0.818,5.323l-14.538,23.3l-0.384,0.615C102.851,466.323,102.4,466.721,101.863,467.031z",
                    // 9
                    "M57.26,338.54c-2.49,0-4.5-2.01-4.5-4.5v-26.58c0-2.49,2.01-4.5,4.5-4.5" +
                            "c0.78,0,1.51,0.2,2.141,0.54l24.489,13.04c0,0,3.37,0.825,3.37,4.2c0,3.125-3.37,4.2-3.37,4.2L59.65,337.85l-0.641,0.341" +
                            "C58.47,338.42,57.88,338.54,57.26,338.54z",
                    // 10
                    "M83.073,203.781c-2.157-1.245-2.892-3.99-1.647-6.147l13.29-23.019" +
                            "c1.245-2.157,3.991-2.892,6.147-1.647c0.675,0.39,1.208,0.928,1.583,1.537l14.688,23.538c0,0,2.506,2.399,0.819,5.322" +
                            "c-1.562,2.707-5.019,1.953-5.019,1.953l-27.447-0.94l-0.725-0.025C84.181,204.283,83.61,204.091,83.073,203.781z",
                    // 11
                    "M173.229,100.603c-1.245-2.157-0.509-4.902,1.647-6.147l23.019-13.29" +
                            "c2.156-1.245,4.902-0.509,6.147,1.647c0.39,0.675,0.582,1.407,0.603,2.123l0.952,27.729c0,0,0.97,3.332-1.953,5.019" +
                            "c-2.706,1.562-5.322-0.819-5.322-0.819l-23.3-14.537l-0.615-0.384C173.937,101.591,173.539,101.14,173.229,100.603z",
            };
            

/*
		<path id="rptr-12" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M310.846,40.462c1-1.156,2.156-0.938,2.156-0.938
			h13.906c0,0,1.126-0.156,2.178,0.938c1.042,1.083,0.916,2.263,0.916,2.263v31.3c0,0-0.033,2.094-0.47,3.375
			c-0.357,1.047-1.821,2.937-1.821,2.937l-4.734,5.688c0,0-1.413,1.8-2.975,1.8c-1.625,0-3.075-1.8-3.075-1.8l-4.634-5.688
			c0,0-1.39-1.707-1.821-2.937c-0.408-1.164-0.47-3.375-0.47-3.375v-31.3C310.002,42.725,309.84,41.625,310.846,40.462z"/>
		<path id="rptr-11" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M172.803,82.086
			c0.288-1.501,1.399-1.891,1.399-1.891l12.043-6.953c0,0,0.897-0.699,2.355-0.277c1.443,0.417,1.924,1.501,1.924,1.501
			l15.65,27.106c0,0,1.019,1.83,1.28,3.158c0.214,1.085-0.108,3.454-0.108,3.454l-1.255,7.292c0,0-0.324,2.266-1.677,3.046
			c-1.407,0.812-3.563-0.021-3.563-0.021l-6.857-2.609c0,0-2.057-0.783-3.045-1.633c-0.935-0.804-2.095-2.688-2.095-2.688
			l-15.65-27.106C173.204,84.467,172.514,83.597,172.803,82.086z"/>
		<path id="rptr-10" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M73.328,187.659
			c-0.501-1.444,0.266-2.336,0.266-2.336l6.953-12.043c0,0,0.428-1.054,1.901-1.417c1.458-0.361,2.417,0.338,2.417,0.338
			l27.106,15.65c0,0,1.797,1.075,2.688,2.095c0.728,0.833,1.633,3.045,1.633,3.045l2.559,6.943c0,0,0.853,2.124,0.071,3.477
			c-0.812,1.407-3.096,1.763-3.096,1.763l-7.243,1.169c0,0-2.172,0.35-3.454,0.108c-1.212-0.229-3.158-1.28-3.158-1.28
			l-27.106-15.65C74.865,189.521,73.832,189.112,73.328,187.659z"/>
		<path id="rptr-9" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M40.465,330.154c-1.156-1-0.938-2.156-0.938-2.156
			v-13.906c0,0-0.156-1.126,0.938-2.178c1.083-1.042,2.263-0.916,2.263-0.916h31.3c0,0,2.094,0.033,3.375,0.47
			c1.047,0.357,2.937,1.821,2.937,1.821l5.688,4.734c0,0,1.8,1.413,1.8,2.975c0,1.625-1.8,3.075-1.8,3.075l-5.688,4.634
			c0,0-1.707,1.39-2.937,1.821c-1.164,0.408-3.375,0.47-3.375,0.47h-31.3C42.728,330.998,41.628,331.16,40.465,330.154z"/>
		<path id="rptr-8" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M82.481,468.196c-1.501-0.288-1.89-1.398-1.89-1.398
			l-6.953-12.043c0,0-0.699-0.897-0.277-2.355c0.417-1.443,1.501-1.924,1.501-1.924l27.106-15.65c0,0,1.83-1.019,3.158-1.28
			c1.085-0.214,3.454,0.108,3.454,0.108l7.293,1.256c0,0,2.265,0.323,3.046,1.677c0.812,1.407-0.021,3.563-0.021,3.563l-2.609,6.857
			c0,0-0.783,2.057-1.633,3.045c-0.804,0.935-2.688,2.095-2.688,2.095l-27.106,15.65C84.863,467.796,83.992,468.486,82.481,468.196z
			"/>
		<path id="rptr-7" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M188.325,566.006
			c-1.444,0.501-2.336-0.266-2.336-0.266l-12.043-6.953c0,0-1.054-0.428-1.418-1.901c-0.36-1.458,0.339-2.417,0.339-2.417
			l15.649-27.106c0,0,1.075-1.797,2.095-2.688c0.833-0.728,3.045-1.633,3.045-1.633l6.944-2.559c0,0,2.123-0.853,3.477-0.071
			c1.407,0.812,1.763,3.096,1.763,3.096l1.169,7.243c0,0,0.35,2.172,0.108,3.454c-0.229,1.212-1.28,3.158-1.28,3.158l-15.65,27.106
			C190.188,564.468,189.778,565.501,188.325,566.006z"/>
		<path id="rptr-6_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M329.254,600.734
			c-1,1.156-2.156,0.938-2.156,0.938h-13.906c0,0-1.126,0.156-2.178-0.938c-1.042-1.083-0.916-2.263-0.916-2.263v-31.3
			c0,0,0.033-2.094,0.47-3.375c0.357-1.047,1.821-2.937,1.821-2.937l4.734-5.688c0,0,1.413-1.8,2.975-1.8
			c1.625,0,3.075,1.8,3.075,1.8l4.634,5.688c0,0,1.39,1.707,1.821,2.937c0.408,1.164,0.47,3.375,0.47,3.375v31.3
			C330.098,598.472,330.26,599.571,329.254,600.734z"/>
		<path id="rptr-5" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M467.529,558.185
			c-0.288,1.501-1.398,1.89-1.398,1.89l-12.043,6.953c0,0-0.897,0.699-2.355,0.277c-1.443-0.417-1.924-1.501-1.924-1.501
			l-15.65-27.106c0,0-1.019-1.83-1.28-3.158c-0.214-1.085,0.108-3.454,0.108-3.454l1.256-7.293c0,0,0.323-2.265,1.677-3.046
			c1.407-0.812,3.563,0.021,3.563,0.021l6.857,2.609c0,0,2.057,0.783,3.045,1.633c0.935,0.804,2.095,2.688,2.095,2.688l15.65,27.106
			C467.129,555.804,467.819,556.675,467.529,558.185z"/>
		<path id="rptr-4" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M566.672,452.341
			c0.501,1.444-0.266,2.336-0.266,2.336l-6.953,12.043c0,0-0.428,1.054-1.901,1.418c-1.458,0.36-2.417-0.339-2.417-0.339
			l-27.106-15.649c0,0-1.797-1.076-2.688-2.095c-0.729-0.833-1.633-3.046-1.633-3.046l-2.559-6.943c0,0-0.853-2.123-0.071-3.477
			c0.812-1.407,3.096-1.763,3.096-1.763l7.243-1.169c0,0,2.173-0.35,3.454-0.108c1.211,0.229,3.158,1.28,3.158,1.28l27.106,15.65
			C565.134,450.479,566.168,450.888,566.672,452.341z"/>
		<path id="rptr-3" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M599.204,310.846c1.156,1,0.938,2.156,0.938,2.156
			v13.906c0,0,0.156,1.126-0.938,2.178c-1.083,1.042-2.263,0.916-2.263,0.916h-31.3c0,0-2.094-0.033-3.375-0.47
			c-1.047-0.357-2.937-1.821-2.937-1.821l-5.688-4.734c0,0-1.8-1.413-1.8-2.975c0-1.625,1.8-3.075,1.8-3.075l5.688-4.634
			c0,0,1.707-1.39,2.937-1.821c1.164-0.408,3.375-0.47,3.375-0.47h31.3C596.941,310.002,598.041,309.84,599.204,310.846z"/>
		<path id="rptr-2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M558.185,171.804c1.501,0.288,1.89,1.398,1.89,1.398
			l6.953,12.043c0,0,0.699,0.897,0.277,2.355c-0.417,1.443-1.502,1.924-1.502,1.924l-27.106,15.65c0,0-1.83,1.019-3.158,1.28
			c-1.086,0.214-3.454-0.108-3.454-0.108l-7.293-1.256c0,0-2.265-0.323-3.046-1.677c-0.812-1.407,0.021-3.563,0.021-3.563
			l2.609-6.857c0,0,0.783-2.057,1.633-3.045c0.804-0.935,2.688-2.095,2.688-2.095l27.106-15.65
			C555.803,172.205,556.675,171.515,558.185,171.804z"/>
		<path id="rptr-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M452.341,73.328
			c1.444-0.501,2.336,0.267,2.336,0.267l12.043,6.953c0,0,1.054,0.428,1.417,1.901c0.361,1.458-0.338,2.417-0.338,2.417
			l-15.65,27.106c0,0-1.075,1.797-2.095,2.688c-0.833,0.729-3.045,1.633-3.045,1.633l-6.943,2.559c0,0-2.124,0.853-3.477,0.071
			c-1.407-0.812-1.763-3.096-1.763-3.096l-1.169-7.243c0,0-0.35-2.173-0.108-3.454c0.229-1.211,1.28-3.158,1.28-3.158l15.65-27.106
			C450.479,74.866,450.888,73.832,452.341,73.328z"/>
*/

            String[] mSmallPointerSVG = new String[] {
                    // 0: 12
                    "M310.846,40.462c1-1.156,2.156-0.938,2.156-0.938" +
                            "h13.906c0,0,1.126-0.156,2.178,0.938c1.042,1.083,0.916,2.263,0.916,2.263v31.3c0,0-0.033,2.094-0.47,3.375" +
                            "c-0.357,1.047-1.821,2.937-1.821,2.937l-4.734,5.688c0,0-1.413,1.8-2.975,1.8c-1.625,0-3.075-1.8-3.075-1.8l-4.634-5.688" +
                            "c0,0-1.39-1.707-1.821-2.937c-0.408-1.164-0.47-3.375-0.47-3.375v-31.3C310.002,42.725,309.84,41.625,310.846,40.462z",
                    // 1
                    "M452.341,73.328" +
                            "c1.444-0.501,2.336,0.267,2.336,0.267l12.043,6.953c0,0,1.054,0.428,1.417,1.901c0.361,1.458-0.338,2.417-0.338,2.417" +
                            "l-15.65,27.106c0,0-1.075,1.797-2.095,2.688c-0.833,0.729-3.045,1.633-3.045,1.633l-6.943,2.559c0,0-2.124,0.853-3.477,0.071" +
                            "c-1.407-0.812-1.763-3.096-1.763-3.096l-1.169-7.243c0,0-0.35-2.173-0.108-3.454c0.229-1.211,1.28-3.158,1.28-3.158l15.65-27.106" +
                            "C450.479,74.866,450.888,73.832,452.341,73.328z",
                    // 2
                    "M558.185,171.804c1.501,0.288,1.89,1.398,1.89,1.398" +
                            "l6.953,12.043c0,0,0.699,0.897,0.277,2.355c-0.417,1.443-1.502,1.924-1.502,1.924l-27.106,15.65c0,0-1.83,1.019-3.158,1.28" +
                            "c-1.086,0.214-3.454-0.108-3.454-0.108l-7.293-1.256c0,0-2.265-0.323-3.046-1.677c-0.812-1.407,0.021-3.563,0.021-3.563" +
                            "l2.609-6.857c0,0,0.783-2.057,1.633-3.045c0.804-0.935,2.688-2.095,2.688-2.095l27.106-15.65" +
                            "C555.803,172.205,556.675,171.515,558.185,171.804z",
                    // 3
                    "M599.204,310.846c1.156,1,0.938,2.156,0.938,2.156" +
                            "v13.906c0,0,0.156,1.126-0.938,2.178c-1.083,1.042-2.263,0.916-2.263,0.916h-31.3c0,0-2.094-0.033-3.375-0.47" +
                            "c-1.047-0.357-2.937-1.821-2.937-1.821l-5.688-4.734c0,0-1.8-1.413-1.8-2.975c0-1.625,1.8-3.075,1.8-3.075l5.688-4.634" +
                            "c0,0,1.707-1.39,2.937-1.821c1.164-0.408,3.375-0.47,3.375-0.47h31.3C596.941,310.002,598.041,309.84,599.204,310.846z",
                    // 4
                    "M566.672,452.341" +
                            "c0.501,1.444-0.266,2.336-0.266,2.336l-6.953,12.043c0,0-0.428,1.054-1.901,1.418c-1.458,0.36-2.417-0.339-2.417-0.339" +
                            "l-27.106-15.649c0,0-1.797-1.076-2.688-2.095c-0.729-0.833-1.633-3.046-1.633-3.046l-2.559-6.943c0,0-0.853-2.123-0.071-3.477" +
                            "c0.812-1.407,3.096-1.763,3.096-1.763l7.243-1.169c0,0,2.173-0.35,3.454-0.108c1.211,0.229,3.158,1.28,3.158,1.28l27.106,15.65" +
                            "C565.134,450.479,566.168,450.888,566.672,452.341z",
                    // 5
                    "M467.529,558.185" +
                            "c-0.288,1.501-1.398,1.89-1.398,1.89l-12.043,6.953c0,0-0.897,0.699-2.355,0.277c-1.443-0.417-1.924-1.501-1.924-1.501" +
                            "l-15.65-27.106c0,0-1.019-1.83-1.28-3.158c-0.214-1.085,0.108-3.454,0.108-3.454l1.256-7.293c0,0,0.323-2.265,1.677-3.046" +
                            "c1.407-0.812,3.563,0.021,3.563,0.021l6.857,2.609c0,0,2.057,0.783,3.045,1.633c0.935,0.804,2.095,2.688,2.095,2.688l15.65,27.106" +
                            "C467.129,555.804,467.819,556.675,467.529,558.185z",
                    // 6
                    "M329.254,600.734" +
                            "c-1,1.156-2.156,0.938-2.156,0.938h-13.906c0,0-1.126,0.156-2.178-0.938c-1.042-1.083-0.916-2.263-0.916-2.263v-31.3" +
                            "c0,0,0.033-2.094,0.47-3.375c0.357-1.047,1.821-2.937,1.821-2.937l4.734-5.688c0,0,1.413-1.8,2.975-1.8" +
                            "c1.625,0,3.075,1.8,3.075,1.8l4.634,5.688c0,0,1.39,1.707,1.821,2.937c0.408,1.164,0.47,3.375,0.47,3.375v31.3" +
                            "C330.098,598.472,330.26,599.571,329.254,600.734z",
                    // 7
                    "M188.325,566.006" +
                            "c-1.444,0.501-2.336-0.266-2.336-0.266l-12.043-6.953c0,0-1.054-0.428-1.418-1.901c-0.36-1.458,0.339-2.417,0.339-2.417" +
                            "l15.649-27.106c0,0,1.075-1.797,2.095-2.688c0.833-0.728,3.045-1.633,3.045-1.633l6.944-2.559c0,0,2.123-0.853,3.477-0.071" +
                            "c1.407,0.812,1.763,3.096,1.763,3.096l1.169,7.243c0,0,0.35,2.172,0.108,3.454c-0.229,1.212-1.28,3.158-1.28,3.158l-15.65,27.106" +
                            "C190.188,564.468,189.778,565.501,188.325,566.006z",
                    // 8
                    "M82.481,468.196c-1.501-0.288-1.89-1.398-1.89-1.398" +
                            "l-6.953-12.043c0,0-0.699-0.897-0.277-2.355c0.417-1.443,1.501-1.924,1.501-1.924l27.106-15.65c0,0,1.83-1.019,3.158-1.28" +
                            "c1.085-0.214,3.454,0.108,3.454,0.108l7.293,1.256c0,0,2.265,0.323,3.046,1.677c0.812,1.407-0.021,3.563-0.021,3.563l-2.609,6.857" +
                            "c0,0-0.783,2.057-1.633,3.045c-0.804,0.935-2.688,2.095-2.688,2.095l-27.106,15.65C84.863,467.796,83.992,468.486,82.481,468.196z",
                    // 9
                    "M40.465,330.154c-1.156-1-0.938-2.156-0.938-2.156" +
                            "v-13.906c0,0-0.156-1.126,0.938-2.178c1.083-1.042,2.263-0.916,2.263-0.916h31.3c0,0,2.094,0.033,3.375,0.47" +
                            "c1.047,0.357,2.937,1.821,2.937,1.821l5.688,4.734c0,0,1.8,1.413,1.8,2.975c0,1.625-1.8,3.075-1.8,3.075l-5.688,4.634" +
                            "c0,0-1.707,1.39-2.937,1.821c-1.164,0.408-3.375,0.47-3.375,0.47h-31.3C42.728,330.998,41.628,331.16,40.465,330.154z",
                    // 10
                    "M73.328,187.659" +
                            "c-0.501-1.444,0.266-2.336,0.266-2.336l6.953-12.043c0,0,0.428-1.054,1.901-1.417c1.458-0.361,2.417,0.338,2.417,0.338" +
                            "l27.106,15.65c0,0,1.797,1.075,2.688,2.095c0.728,0.833,1.633,3.045,1.633,3.045l2.559,6.943c0,0,0.853,2.124,0.071,3.477" +
                            "c-0.812,1.407-3.096,1.763-3.096,1.763l-7.243,1.169c0,0-2.172,0.35-3.454,0.108c-1.212-0.229-3.158-1.28-3.158-1.28" +
                            "l-27.106-15.65C74.865,189.521,73.832,189.112,73.328,187.659z",
                    // 11
                    "M172.803,82.086" +
                            "c0.288-1.501,1.399-1.891,1.399-1.891l12.043-6.953c0,0,0.897-0.699,2.355-0.277c1.443,0.417,1.924,1.501,1.924,1.501" +
                            "l15.65,27.106c0,0,1.019,1.83,1.28,3.158c0.214,1.085-0.108,3.454-0.108,3.454l-1.255,7.292c0,0-0.324,2.266-1.677,3.046" +
                            "c-1.407,0.812-3.563-0.021-3.563-0.021l-6.857-2.609c0,0-2.057-0.783-3.045-1.633c-0.935-0.804-2.095-2.688-2.095-2.688" +
                            "l-15.65-27.106C173.204,84.467,172.514,83.597,172.803,82.086z",
            };


/*
		<path id="blt-12" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M303,43.167c0-3.313,2.736-5.429,6-6
			c2.625-0.459,8.239-1,11-1s8.25,0.416,11,1c3.241,0.688,6,2.687,6,6c0,0-1.475,8.198-2,11c-2,10.666-9.959,33.666-15,33.666
			c-5.084,0-12.75-22.375-15-33.666C304.443,51.372,303,43.167,303,43.167z"/>
		<path id="blt-11" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M166.528,88.19
			c-1.657-2.869-0.346-6.069,2.196-8.196c2.044-1.71,6.635-4.985,9.026-6.366c2.391-1.38,7.353-3.765,10.026-4.634
			c3.151-1.024,6.54-0.673,8.196,2.196c0,0,2.822,7.837,3.768,10.526c3.601,10.237,8.208,34.135,3.843,36.656
			c-4.403,2.542-22.229-13.002-29.823-21.656C171.88,94.574,166.528,88.19,166.528,88.19z"/>
		<path id="blt-10" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M71.19,195.973c-2.87-1.657-3.333-5.084-2.196-8.196
			c0.915-2.503,3.253-7.635,4.634-10.026s4.485-6.937,6.366-9.026c2.217-2.462,5.326-3.853,8.196-2.196c0,0,6.363,5.376,8.526,7.232
			c8.237,7.065,24.176,25.458,21.656,29.823c-2.542,4.403-25.752-0.146-36.656-3.843C79.017,198.825,71.19,195.973,71.19,195.973z"
			/>
		<path id="blt-9" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M43.167,337c-3.313,0-5.429-2.736-6-6
			c-0.459-2.625-1-8.239-1-11s0.416-8.25,1-11c0.688-3.241,2.687-6,6-6c0,0,8.198,1.475,11,2c10.666,2,33.666,9.959,33.666,15
			c0,5.084-22.375,12.75-33.666,15C51.372,335.557,43.167,337,43.167,337z"/>
		<path id="blt-8" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M88.856,473.472
			c-2.869,1.657-6.069,0.345-8.196-2.196c-1.71-2.044-4.985-6.635-6.366-9.026c-1.38-2.391-3.765-7.353-4.634-10.026
			c-1.024-3.151-0.673-6.54,2.196-8.196c0,0,7.837-2.822,10.526-3.768c10.237-3.601,34.135-8.208,36.656-3.843
			c2.542,4.403-13.002,22.229-21.656,29.823C95.241,468.12,88.856,473.472,88.856,473.472z"/>
		<path id="blt-7" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M195.972,568.144
			c-1.657,2.87-5.084,3.333-8.196,2.196c-2.503-0.915-7.635-3.253-10.026-4.634c-2.391-1.381-6.937-4.485-9.026-6.366
			c-2.462-2.217-3.853-5.326-2.196-8.196c0,0,5.376-6.363,7.232-8.526c7.065-8.237,25.458-24.176,29.823-21.656
			c4.403,2.542-0.146,25.752-3.843,36.656C198.825,560.316,195.972,568.144,195.972,568.144z"/>
		<path id="blt-6_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M337,595.666c0,3.313-2.736,5.429-6,6
			c-2.625,0.459-8.239,1-11,1s-8.25-0.416-11-1c-3.241-0.688-6-2.687-6-6c0,0,1.475-8.198,2-11C307,574,314.959,551,320,551
			c5.084,0,12.75,22.375,15,33.666C335.557,587.461,337,595.666,337,595.666z"/>
		<path id="blt-" style="display:none;fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M337,338.833c0,3.313-2.736,5.429-6,6
			c-2.625,0.459-8.239,1-11,1s-8.25-0.416-11-1c-3.241-0.688-6-2.687-6-6c0,0,1.475-8.198,2-11c2-10.666,9.959-33.666,15-33.666
			c5.084,0,12.75,22.375,15,33.666C335.557,330.628,337,338.833,337,338.833z"/>
		<path id="blt-5" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M472.806,551.143c1.657,2.87,0.345,6.069-2.196,8.196
			c-2.044,1.71-6.635,4.985-9.026,6.366c-2.391,1.38-7.353,3.765-10.026,4.634c-3.151,1.024-6.54,0.674-8.196-2.196
			c0,0-2.822-7.837-3.768-10.526c-3.601-10.237-8.208-34.135-3.843-36.656c4.403-2.542,22.229,13.002,29.823,21.656
			C467.454,544.759,472.806,551.143,472.806,551.143z"/>
		<path id="blt-4" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M568.81,444.694c2.87,1.656,3.333,5.083,2.196,8.196
			c-0.915,2.503-3.253,7.635-4.634,10.026s-4.485,6.937-6.366,9.026c-2.217,2.462-5.326,3.853-8.196,2.196
			c0,0-6.363-5.376-8.526-7.232c-8.237-7.065-24.176-25.458-21.656-29.823c2.542-4.403,25.752,0.146,36.656,3.843
			C560.983,441.841,568.81,444.694,568.81,444.694z"/>
		<path id="blt-3" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M596.833,303c3.313,0,5.429,2.736,6,6
			c0.459,2.625,1,8.239,1,11s-0.416,8.25-1,11c-0.688,3.241-2.687,6-6,6c0,0-8.198-1.475-11-2c-10.666-2-33.666-9.959-33.666-15
			c0-5.084,22.375-12.75,33.666-15C588.628,304.443,596.833,303,596.833,303z"/>
		<path id="blt-2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M551.81,166.527c2.87-1.657,6.069-0.345,8.196,2.196
			c1.71,2.044,4.985,6.635,6.366,9.026c1.38,2.391,3.765,7.353,4.634,10.026c1.024,3.151,0.674,6.54-2.196,8.196
			c0,0-7.837,2.822-10.526,3.768c-10.237,3.601-34.135,8.208-36.656,3.843c-2.542-4.403,13.002-22.229,21.656-29.823
			C545.426,171.879,551.81,166.527,551.81,166.527z"/>
		<path id="blt-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M444.027,72.523c1.656-2.869,5.083-3.333,8.196-2.196
			c2.503,0.915,7.635,3.253,10.026,4.634c2.391,1.381,6.937,4.485,9.026,6.366c2.462,2.217,3.853,5.327,2.196,8.196
			c0,0-5.376,6.363-7.232,8.526c-7.065,8.237-25.458,24.176-29.823,21.656c-4.403-2.542,0.146-25.752,3.843-36.656
			C441.174,80.351,444.027,72.523,444.027,72.523z"/>
*/

//            String[] mBulletSVG = new String[] {
//                    // 0: 12
//                    "M303,43.167c0-3.313,2.736-5.429,6-6" +
//                            "c2.625-0.459,8.239-1,11-1s8.25,0.416,11,1c3.241,0.688,6,2.687,6,6c0,0-1.475,8.198-2,11c-2,10.666-9.959,33.666-15,33.666" +
//                            "c-5.084,0-12.75-22.375-15-33.666C304.443,51.372,303,43.167,303,43.167z",
//                    // 1
//                    "M444.027,72.523c1.656-2.869,5.083-3.333,8.196-2.196" +
//                            "c2.503,0.915,7.635,3.253,10.026,4.634c2.391,1.381,6.937,4.485,9.026,6.366c2.462,2.217,3.853,5.327,2.196,8.196" +
//                            "c0,0-5.376,6.363-7.232,8.526c-7.065,8.237-25.458,24.176-29.823,21.656c-4.403-2.542,0.146-25.752,3.843-36.656" +
//                            "C441.174,80.351,444.027,72.523,444.027,72.523z",
//                    // 2
//                    "M551.81,166.527c2.87-1.657,6.069-0.345,8.196,2.196" +
//                            "c1.71,2.044,4.985,6.635,6.366,9.026c1.38,2.391,3.765,7.353,4.634,10.026c1.024,3.151,0.674,6.54-2.196,8.196" +
//                            "c0,0-7.837,2.822-10.526,3.768c-10.237,3.601-34.135,8.208-36.656,3.843c-2.542-4.403,13.002-22.229,21.656-29.823" +
//                            "C545.426,171.879,551.81,166.527,551.81,166.527z",
//                    // 3
//                    "M596.833,303c3.313,0,5.429,2.736,6,6" +
//                            "c0.459,2.625,1,8.239,1,11s-0.416,8.25-1,11c-0.688,3.241-2.687,6-6,6c0,0-8.198-1.475-11-2c-10.666-2-33.666-9.959-33.666-15" +
//                            "c0-5.084,22.375-12.75,33.666-15C588.628,304.443,596.833,303,596.833,303z",
//                    // 4
//                    "M568.81,444.694c2.87,1.656,3.333,5.083,2.196,8.196" +
//                            "c-0.915,2.503-3.253,7.635-4.634,10.026s-4.485,6.937-6.366,9.026c-2.217,2.462-5.326,3.853-8.196,2.196" +
//                            "c0,0-6.363-5.376-8.526-7.232c-8.237-7.065-24.176-25.458-21.656-29.823c2.542-4.403,25.752,0.146,36.656,3.843" +
//                            "C560.983,441.841,568.81,444.694,568.81,444.694z",
//                    // 5
//                    "M472.806,551.143c1.657,2.87,0.345,6.069-2.196,8.196" +
//                            "c-2.044,1.71-6.635,4.985-9.026,6.366c-2.391,1.38-7.353,3.765-10.026,4.634c-3.151,1.024-6.54,0.674-8.196-2.196" +
//                            "c0,0-2.822-7.837-3.768-10.526c-3.601-10.237-8.208-34.135-3.843-36.656c4.403-2.542,22.229,13.002,29.823,21.656" +
//                            "C467.454,544.759,472.806,551.143,472.806,551.143z",
//                    // 6
//                    "M337,595.67c0,3.313-2.736,5.429-6,6" +
//                            "c-2.625,0.459-8.239,1-11,1s-8.25-0.416-11-1c-3.241-0.688-6-2.687-6-6c0,0,1.475-8.198,2-11C307,574,314.959,551,320,551" +
//                            "c5.084,0,12.75,22.375,15,33.666C335.557,587.461,337,595.666,337,595.67z",
//                    // 7
//                    "M195.972,568.144" +
//                            "c-1.657,2.87-5.084,3.333-8.196,2.196c-2.503-0.915-7.635-3.253-10.026-4.634c-2.391-1.381-6.937-4.485-9.026-6.366" +
//                            "c-2.462-2.217-3.853-5.326-2.196-8.196c0,0,5.376-6.363,7.232-8.526c7.065-8.237,25.458-24.176,29.823-21.656" +
//                            "c4.403,2.542-0.146,25.752-3.843,36.656C198.825,560.316,195.972,568.144,195.972,568.144z",
//                    // 8
//                    "M88.856,473.472" +
//                            "c-2.869,1.657-6.069,0.345-8.196-2.196c-1.71-2.044-4.985-6.635-6.366-9.026c-1.38-2.391-3.765-7.353-4.634-10.026" +
//                            "c-1.024-3.151-0.673-6.54,2.196-8.196c0,0,7.837-2.822,10.526-3.768c10.237-3.601,34.135-8.208,36.656-3.843" +
//                            "c2.542,4.403-13.002,22.229-21.656,29.823C95.241,468.12,88.856,473.472,88.856,473.472z",
//                    // 9
//                    "M43.167,337c-3.313,0-5.429-2.736-6-6" +
//                            "c-0.459-2.625-1-8.239-1-11s0.416-8.25,1-11c0.688-3.241,2.687-6,6-6c0,0,8.198,1.475,11,2c10.666,2,33.666,9.959,33.666,15" +
//                            "c0,5.084-22.375,12.75-33.666,15C51.372,335.557,43.167,337,43.167,337z",
//                    // 10
//                    "M71.19,195.973c-2.87-1.657-3.333-5.084-2.196-8.196" +
//                            "c0.915-2.503,3.253-7.635,4.634-10.026s4.485-6.937,6.366-9.026c2.217-2.462,5.326-3.853,8.196-2.196c0,0,6.363,5.376,8.526,7.232" +
//                            "c8.237,7.065,24.176,25.458,21.656,29.823c-2.542,4.403-25.752-0.146-36.656-3.843C79.017,198.825,71.19,195.973,71.19,195.973z",
//                    // 11
//                    "M166.528,88.19" +
//                            "c-1.657-2.869-0.346-6.069,2.196-8.196c2.044-1.71,6.635-4.985,9.026-6.366c2.391-1.38,7.353-3.765,10.026-4.634" +
//                            "c3.151-1.024,6.54-0.673,8.196,2.196c0,0,2.822,7.837,3.768,10.526c3.601,10.237,8.208,34.135,3.843,36.656" +
//                            "c-4.403,2.542-22.229-13.002-29.823-21.656C171.88,94.574,166.528,88.19,166.528,88.19z",
//            };
            

            
/*
		<path id="br-11" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M165.083,79.083l23.729-13.7l26,45.033l-7.778,13.729
			l-15.951-0.028L165.083,79.083z"/>
		<path id="br-10_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M65.572,188.98l13.7-23.729l45.033,26
			l0.128,15.778l-13.828,7.951L65.572,188.98z"/>
		<path id="br-9_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M34.07,333.815l0-27.4h52l8,13.6l-8,13.8H34.07z"/>
		<path id="br-8_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M79.068,474.987l-13.7-23.729l45.033-26
			l13.729,7.778l-0.028,15.951L79.068,474.987z"/>
		<path id="br-7_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M188.964,574.831l-23.729-13.7l26-45.033
			l15.778-0.128l7.951,13.828L188.964,574.831z"/>
		<path id="br-6_2_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M333.8,606h-27.4v-52l13.6-8l13.8,8V606z"/>
		<path id="br-5_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M474.631,560.798l-23.729,13.7l-26-45.034
			l7.777-13.728l15.952,0.028L474.631,560.798z"/>
		<path id="br-4_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M574.14,450.931l-13.7,23.729l-45.033-26
			l-0.129-15.777l13.829-7.952L574.14,450.931z"/>
		<path id="br-3_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M605.432,306.73v27.4l-52,0l-8-13.599l8-13.801
			H605.432z"/>
		<path id="br-2_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M560.895,165.232l13.7,23.729l-45.033,26
			l-13.729-7.777l0.028-15.952L560.895,165.232z"/>
		<path id="br-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M451.695,65.723l23.729,13.701l-26,45.032
			l-15.778,0.13l-7.952-13.829L451.695,65.723z"/>
		<path id="br-12_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M306.066,34.333h27.4v52l-13.6,8l-13.8-8V34.333z"
*/

            String[] mBigPointerSVG = new String[]{
                    // 0: 12
                    "M306.066,34.333h27.4v52l-13.6,8l-13.8-8V34.333z",
                    // 1
                    "M451.695,65.723l23.729,13.701l-26,45.032" +
                            "l-15.778,0.13l-7.952-13.829L451.695,65.723z",
                    // 2
                    "M560.895,165.232l13.7,23.729l-45.033,26" +
                            "l-13.729-7.777l0.028-15.952L560.895,165.232z",
                    // 3
                    "M605.432,306.73v27.4l-52,0l-8-13.599l8-13.801" +
                            "H605.432z",
                    // 4
                    "M574.14,450.931l-13.7,23.729l-45.033-26" +
                            "l-0.129-15.777l13.829-7.952L574.14,450.931z",
                    // 5
                    "M474.631,560.798l-23.729,13.7l-26-45.034" +
                            "l7.777-13.728l15.952,0.028L474.631,560.798z",
                    // 6
                    "M333.8,606h-27.4v-52l13.6-8l13.8,8V606zz",
                    // 7
                    "M188.964,574.831l-23.729-13.7l26-45.033" +
                            "l15.778-0.128l7.951,13.828L188.964,574.831z",
                    // 8
                    "M79.068,474.987l-13.7-23.729l45.033-26" +
                            "l13.729,7.778l-0.028,15.951L79.068,474.987z",
                    // 9
                    "M34.07,333.815l0-27.4h52l8,13.6l-8,13.8H34.07z",
                    // 10
                    "M65.572,188.98l13.7-23.729l45.033,26" +
                            "l0.128,15.778l-13.828,7.951L65.572,188.98z",
                    // 11
                    "M165.083,79.083l23.729-13.7l26,45.033l-7.778,13.729" +
                            "l-15.951-0.028L165.083,79.083z",
            };


            
/*            
		<path id="pt-11" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M186.083,104.325c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C191.903,103.519,189.085,104.325,186.083,104.325z"/>
		<path id="pt-10" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M87.962,203.104c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C93.781,202.298,90.964,203.104,87.962,203.104z"/>
		<path id="pt-9" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M52.75,336.5c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C58.569,335.694,55.752,336.5,52.75,336.5z"/>
		<path id="pt-8" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M87.827,471.18c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C93.646,470.374,90.829,471.18,87.827,471.18z"/>
		<path id="pt-7" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M185.843,567.44c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C191.662,566.634,188.845,567.44,185.843,567.44z"/>
		<path id="pt-6" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M319.844,605c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C325.664,604.193,322.846,605,319.844,605z"/>
		<path id="pt-5" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M453.614,567.44c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C459.433,566.634,456.616,567.44,453.614,567.44z"/>
		<path id="pt-4" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M551.842,471.18c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C557.661,470.374,554.844,471.18,551.842,471.18z"/>
		<path id="pt-3" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M588.5,336.5c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C594.319,335.694,591.502,336.5,588.5,336.5z"/>
		<path id="pt-2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M551.842,202.291c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C557.661,201.484,554.844,202.291,551.842,202.291z"/>
		<path id="pt-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M454.109,104.325c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C459.928,103.519,457.111,104.325,454.109,104.325z"/>
		<path id="pt-12" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M320,69.25c-3,0-5.815-0.805-8.242-2.209
			c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247
			c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209
			c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247
			c-1.45,2.502-3.539,4.59-6.041,6.04C325.819,68.444,323.002,69.25,320,69.25z"/>
*/          
            String[] mDotSVG = new String[]{
                // 0: 12
                "M320,69.25c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C325.819,68.444,323.002,69.25,320,69.25z",
                // 1
                "M454.109,104.325c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C459.928,103.519,457.111,104.325,454.109,104.325z",
                // 2
                "M551.842,202.291c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C557.661,201.484,554.844,202.291,551.842,202.291z",
                // 3
                "M588.5,336.5c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C594.319,335.694,591.502,336.5,588.5,336.5z",
                // 4
                "M551.842,471.18c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C557.661,470.374,554.844,471.18,551.842,471.18z",
                // 5
                "M453.614,567.44c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C459.433,566.634,456.616,567.44,453.614,567.44z",
                // 6
                "M319.844,605c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C325.664,604.193,322.846,605,319.844,605z",
                // 7
                "M185.843,567.44c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C191.662,566.634,188.845,567.44,185.843,567.44z",
                // 8
                "M87.827,471.18c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C93.646,470.374,90.829,471.18,87.827,471.18z",
                // 9
                "M52.75,336.5c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C58.569,335.694,55.752,336.5,52.75,336.5z",
                // 10
                "M87.962,203.104c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C93.781,202.298,90.964,203.104,87.962,203.104z",
                // 11
                "M186.083,104.325c-3,0-5.815-0.805-8.242-2.209" +
                        "c-2.503-1.449-4.593-3.538-6.044-6.041c-1.408-2.429-2.214-5.247-2.214-8.25c0-3.002,0.806-5.819,2.213-8.247" +
                        "c1.45-2.502,3.538-4.59,6.04-6.04c2.428-1.407,5.245-2.213,8.247-2.213c3,0,5.815,0.805,8.242,2.209" +
                        "c2.503,1.449,4.593,3.538,6.044,6.041c1.408,2.429,2.214,5.247,2.214,8.25c0,3.002-0.806,5.819-2.212,8.247" +
                        "c-1.45,2.502-3.539,4.59-6.041,6.04C191.903,103.519,189.085,104.325,186.083,104.325z",
            };
            
            

/*
			<g id="addgt11">
				<path id="addgt11-R1_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M204.67,128V91l-4.74-3.05l-0.68-0.43
					c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3
					c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L204.67,128z"/>
				<path id="addgt11-L1_1_" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M170.67,128V91l-4.74-3.05l-0.68-0.43
					c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3
					c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L170.67,128z"/>
			</g>
			<g id="addgt10">
				<path id="addgt10-0" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M157.67,174.01v54.32c0,2.76-2.24,5-5,5h-38.5
					c-2.77,0-5-2.24-5-5v-54.32c0-2.76,2.23-5,5-5h38.5C155.43,169.01,157.67,171.25,157.67,174.01z M144.22,187.71
					c0-2.76-2.39-5-5.34-5h-10.79c-2.95,0-5.34,2.24-5.34,5v27.5c0,2.76,2.39,5,5.34,5h10.79c2.95,0,5.34-2.24,5.34-5V187.71z"/>
				<path id="addgt10-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M80.67,224v-37l-4.74-3.05l-0.68-0.43
					c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3
					c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V224l4,8.5h-25L80.67,224z"/>
			</g>
			<path id="addgt9" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M59.933,333.828v1.5c0,2.64,2.061,4.81,4.66,4.979
				c0.111,0.021,10.67,0.021,10.781,0c2.6-0.17,4.659-2.34,4.659-4.979v-2.05c-0.28-2.24-2.06-4.021-4.3-4.301h-18.33
				c-6.1,0-11.279-4.75-11.67-10.76v-18.57c0-6.359,5.15-11.5,11.5-11.5h25.5c6.359,0,11.5,5.141,11.5,11.5v42
				c0,6.351-5.141,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.149-11.5-11.5v-7.819H59.933z M80.033,310.678v-3.6
				c0.038-2.825-2.087-4.763-4.71-4.771H64.603c-2.781,0.008-4.656,2.07-4.67,4.61v4.4c0.301,2.34,2.221,4.18,4.611,4.329h11
				C78.063,315.397,80.033,313.268,80.033,310.678z"/>
			<path id="addgt8" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M130.283,436.867l-2.449,3.21l2.67,3.62l0.66,0.88
				c0.42,0.729,0.67,1.59,0.67,2.5v22.09c0,2.76-2.24,5-5,5h-38.5c-2.76,0-5-2.24-5-5v-22.09c0-0.91,0.25-1.771,0.67-2.5l0.66-0.88
				l2.67-3.62l-2.451-3.21l-0.949-1.24c0,0,0,0-0.01-0.01c-0.37-0.7-0.59-1.51-0.59-2.37v-18.26c0-2.761,2.24-5,5-5h38.5
				c2.76,0,5,2.239,5,5v18.26c0,0.86-0.221,1.67-0.59,2.37c-0.011,0.01-0.011,0.01-0.011,0.01L130.283,436.867z M117.633,451.167
				c-0.299-2.34-2.219-4.18-4.609-4.33h-10.88c-2.39,0.15-4.31,1.99-4.61,4.33v4.61c-0.01,2.89,2.051,5.14,4.86,5.229h10.431
				c2.699-0.03,4.76-1.78,4.809-4.87V451.167z M117.644,428.296c0-2.76-2.24-5-5-5h-10.101c-2.76,0-5,2.24-5,5v3.5
				c0,2.76,2.24,5,5,5h10.101c2.76,0,5-2.24,5-5V428.296z"/>
			<path id="addgt7" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M220.804,506.484v9.9l-20.5,49.8h-17.5l21.67-50
				h-26l-6.17,13.8h-4v-23.5c0-2.76,2.239-5,5-5h42.5C218.573,501.484,220.804,503.725,220.804,506.484z"/>
			<path id="addgt6" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M330.701,555.8v-1.5c0-2.64-2.061-4.81-4.66-4.979
				c-0.111-0.021-0.57-0.021-0.68,0h-9.421c-0.11-0.021-0.569-0.021-0.681,0c-2.6,0.17-4.659,2.34-4.659,4.979v2.05
				c0.28,2.24,2.06,4.021,4.3,4.301h18.33c6.1,0,11.279,4.75,11.67,10.76v18.57c0,6.359-5.15,11.5-11.5,11.5h-25.5
				c-6.359,0-11.5-5.141-11.5-11.5v-42c0-6.351,5.141-11.5,11.5-11.5h25.5c6.35,0,11.5,5.149,11.5,11.5v7.819H330.701z
				 M310.601,578.95v3.6c-0.038,2.825,2.087,4.763,4.71,4.771h10.721c2.781-0.008,4.656-2.07,4.67-4.61v-4.4
				c-0.301-2.34-2.221-4.18-4.611-4.329h-11C312.57,574.23,310.601,576.36,310.601,578.95z"/>
			<path id="addgt5" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M432.333,517.24c-0.01,0.14-0.02,0.29-0.02,0.43
				s0.01,0.29,0.02,0.43c0.21,2.03,1.88,3.631,3.94,3.73c0.069,0.01,0.14,0.01,0.21,0.01c0.069,0,0.149,0,0.22-0.01h19.47
				c5.73,0.66,10.16,5.52,10.16,11.42v20.25c0,6.36-5.14,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.14-11.5-11.5v-11.16
				c0,0,14.43,0.04,14.5,0.061c-0.062,5.662,2.312,8.912,7.99,8.939c0.332,0.002,0.67,0.061,1.01,0.061s3.06-0.021,3.38-0.061
				c4.351-0.479,7.72-4.17,7.72-8.64s-3.38-8.16-7.729-8.641c-0.32-0.04-0.64-0.06-0.97-0.06h-25.9v-34h49.333
				c0,0,0.772,0.062,0.959,0.469c0.287,0.623,0.027,0.981,0.027,0.981l-9.819,12.05h-21.85
				C434.323,513.5,432.553,515.14,432.333,517.24z"/>
			<path id="addgt4" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M562.79,442.305l0.616,0.193v13.815
				c-0.26,2.039-1.75,3.699-3.721,4.189h-9.779v5.83l4,8.17h-21.83l4-8.17v-5.83h-33.67v-12.67l33.67-38.33h13.83v38.83h3.76
				l0.01-0.01c0.141,0.01,0.271,0.01,0.41,0.01C558.668,448.333,562.79,442.305,562.79,442.305z M538.746,434.624
				c-0.04-2.41-2.2-4.17-4.311-4.19c-2.6-0.02-4.71,2.65-4.71,2.65c-0.01,0.01-0.01,0.02-0.02,0.03l-5.55,6.34
				c0,0-2.28,2.51-2.53,3.92c-0.68,3.83,2.12,4.92,4.23,4.96h8.909c1.96-0.34,3.521-1.82,3.98-3.73V434.624z"/>
			<path id="addgt3" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M596.637,292.933v8.9l-4.97,14.5l2.32,2.26
				c0.029,0.021,0.05,0.04,0.069,0.061l0.44,0.439c0.04,0.03,0.07,0.07,0.11,0.11c1.1,1.21,1.84,2.76,2.029,4.479v17.73
				c0,6.35-5.149,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.15-11.5-11.5v-10.74h13.75v0.76c0,4.24,3.62,8.21,7.87,8.24h4.271
				c4.27-0.47,7.64-3.939,7.96-8.25v-1.399c-0.04-0.5-0.12-0.99-0.24-1.471c-0.18-0.779-0.48-1.52-0.86-2.2
				c-1.41-2.539-4.02-4.329-7.06-4.6h-5.69v-10.38h7.07c0.229,0,0.46-0.04,0.67-0.1c2.271-0.36,4.01-2.341,4.01-4.71
				c-0.01-1.03-0.35-1.99-0.92-2.771c-0.84-1.16-2.189-1.92-3.72-1.96h-10.771c-1.399,0-2.689,0.38-3.75,0.88
				c-1.159,0.54-3.13,1.93-3.13,1.93l-9.46,6.19v-17.24c0-2.76,2.24-5,5-5h38.721C594.327,288.673,596.337,290.534,596.637,292.933z
				"/>
			<path id="addgt2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M548.292,219.542
				c1.464-0.488,3.688-2.188,3.688-2.188l7.428-5.188v16.5c0,2.76-2.24,5-5,5h-38.5c-2.77,0-5-2.24-5-5v-9.6
				c0-0.77,0.25-1.51,0.77-2c0.2-0.19,0.86-0.67,0.86-0.67s29.693-23.146,29.942-23.355c1.562-1.312,3.125-2.685,3.062-5.062
				c-0.062-2.375-0.751-4.312-5.125-4.312h-11.85c-1.881,0-3.41,1.48-3.49,3.33c-0.01,0.06-0.01,0.11-0.01,0.17v2.31
				c-0.591,4.14-4,7.37-8.22,7.69h-5.94v-23.5c0-2.76,2.23-5,5-5h38.5c2.76,0,5,2.24,5,5v22.17c0,0,0.01,0.685-0.47,1.435
				c-0.331,0.518-1.112,1.093-1.112,1.093l-20.599,15.632c-0.079,0.03-0.319,0.23-0.369,0.28c-0.58,0.53-0.881,1.21-0.95,2.08
				c-0.01,0.13-0.01,0.63,0,0.73c0.14,1.5,1.266,2.704,2.74,2.91c0.098,0.014,0.52-0.002,0.64,0h6.048
				C545.334,219.997,546.885,220.011,548.292,219.542z"/>
			<path id="addgt1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M437.67,128V91l-4.74-3.05l-0.68-0.43
				c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3
				c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L437.67,128z"/>
			<g id="addgt12">
				<path id="addgt12-2" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M351.292,90.542
					c1.464-0.488,3.688-2.188,3.688-2.188l7.428-5.188v16.5c0,2.76-2.24,5-5,5h-38.5c-2.77,0-5-2.24-5-5v-9.6
					c0-0.77,0.25-1.51,0.77-2c0.2-0.19,0.86-0.67,0.86-0.67s29.693-23.146,29.942-23.355c1.562-1.312,3.125-2.685,3.062-5.062
					c-0.062-2.375-0.751-4.312-5.125-4.312h-11.85c-1.881,0-3.41,1.48-3.49,3.33c-0.01,0.06-0.01,0.11-0.01,0.17v2.31
					c-0.591,4.14-4,7.37-8.22,7.69h-5.94v-23.5c0-2.76,2.23-5,5-5h38.5c2.76,0,5,2.24,5,5v22.17c0,0,0.01,0.685-0.47,1.435
					c-0.331,0.518-1.112,1.093-1.112,1.093l-20.599,15.632c-0.079,0.03-0.319,0.23-0.369,0.28c-0.58,0.53-0.881,1.21-0.95,2.08
					c-0.01,0.13-0.01,0.63,0,0.73c0.14,1.5,1.266,2.704,2.74,2.91c0.098,0.014,0.52-0.002,0.64,0h6.048
					C348.334,90.997,349.885,91.011,351.292,90.542z"/>
				<path id="addgt12-1" style="fill:none;stroke:#231F20;stroke-miterlimit:10;" d="M284.67,96V59l-4.74-3.05l-0.68-0.43
					c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3
					c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V96l4,8.5h-25L284.67,96z"/>
			</g>
*/

            String[] mDigitsArtDecoSVG = new String[]{
                    // 0: 12
                    // 0: 12-1
                    "M284.67,96V59l-4.74-3.05l-0.68-0.43" +
                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V96l4,8.5h-25L284.67,96z" +
                    // 0: 12-2
                    "M351.292,90.542" +
                            "c1.464-0.488,3.688-2.188,3.688-2.188l7.428-5.188v16.5c0,2.76-2.24,5-5,5h-38.5c-2.77,0-5-2.24-5-5v-9.6" +
                            "c0-0.77,0.25-1.51,0.77-2c0.2-0.19,0.86-0.67,0.86-0.67s29.693-23.146,29.942-23.355c1.562-1.312,3.125-2.685,3.062-5.062" +
                            "c-0.062-2.375-0.751-4.312-5.125-4.312h-11.85c-1.881,0-3.41,1.48-3.49,3.33c-0.01,0.06-0.01,0.11-0.01,0.17v2.31" +
                            "c-0.591,4.14-4,7.37-8.22,7.69h-5.94v-23.5c0-2.76,2.23-5,5-5h38.5c2.76,0,5,2.24,5,5v22.17c0,0,0.01,0.685-0.47,1.435" +
                            "c-0.331,0.518-1.112,1.093-1.112,1.093l-20.599,15.632c-0.079,0.03-0.319,0.23-0.369,0.28c-0.58,0.53-0.881,1.21-0.95,2.08" +
                            "c-0.01,0.13-0.01,0.63,0,0.73c0.14,1.5,1.266,2.704,2.74,2.91c0.098,0.014,0.52-0.002,0.64,0h6.048" +
                            "C348.334,90.997,349.885,91.011,351.292,90.542z",
                    // 1: 1
//                    "M437.67,128V91l-4.74-3.05l-0.68-0.43" +
//                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
//                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L437.67,128z",
                    "M451.67,128V91l-4.74-3.05l-0.68-0.43" +
                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L451.67,128z",
                    // 2: 2
                    "M548.292,219.542" +
                            "c1.464-0.488,3.688-2.188,3.688-2.188l7.428-5.188v16.5c0,2.76-2.24,5-5,5h-38.5c-2.77,0-5-2.24-5-5v-9.6" +
                            "c0-0.77,0.25-1.51,0.77-2c0.2-0.19,0.86-0.67,0.86-0.67s29.693-23.146,29.942-23.355c1.562-1.312,3.125-2.685,3.062-5.062" +
                            "c-0.062-2.375-0.751-4.312-5.125-4.312h-11.85c-1.881,0-3.41,1.48-3.49,3.33c-0.01,0.06-0.01,0.11-0.01,0.17v2.31" +
                            "c-0.591,4.14-4,7.37-8.22,7.69h-5.94v-23.5c0-2.76,2.23-5,5-5h38.5c2.76,0,5,2.24,5,5v22.17c0,0,0.01,0.685-0.47,1.435" +
                            "c-0.331,0.518-1.112,1.093-1.112,1.093l-20.599,15.632c-0.079,0.03-0.319,0.23-0.369,0.28c-0.58,0.53-0.881,1.21-0.95,2.08" +
                            "c-0.01,0.13-0.01,0.63,0,0.73c0.14,1.5,1.266,2.704,2.74,2.91c0.098,0.014,0.52-0.002,0.64,0h6.048" +
                            "C545.334,219.997,546.885,220.011,548.292,219.542z",
                    // 3: 3
                    "M596.637,292.933v8.9l-4.97,14.5l2.32,2.26" +
                            "c0.029,0.021,0.05,0.04,0.069,0.061l0.44,0.439c0.04,0.03,0.07,0.07,0.11,0.11c1.1,1.21,1.84,2.76,2.029,4.479v17.73" +
                            "c0,6.35-5.149,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.15-11.5-11.5v-10.74h13.75v0.76c0,4.24,3.62,8.21,7.87,8.24h4.271" +
                            "c4.27-0.47,7.64-3.939,7.96-8.25v-1.399c-0.04-0.5-0.12-0.99-0.24-1.471c-0.18-0.779-0.48-1.52-0.86-2.2" +
                            "c-1.41-2.539-4.02-4.329-7.06-4.6h-5.69v-10.38h7.07c0.229,0,0.46-0.04,0.67-0.1c2.271-0.36,4.01-2.341,4.01-4.71" +
                            "c-0.01-1.03-0.35-1.99-0.92-2.771c-0.84-1.16-2.189-1.92-3.72-1.96h-10.771c-1.399,0-2.689,0.38-3.75,0.88" +
                            "c-1.159,0.54-3.13,1.93-3.13,1.93l-9.46,6.19v-17.24c0-2.76,2.24-5,5-5h38.721C594.327,288.673,596.337,290.534,596.637,292.933z",
                    // 4: 4
                    "M562.79,442.305l0.616,0.193v13.815" +
                            "c-0.26,2.039-1.75,3.699-3.721,4.189h-9.779v5.83l4,8.17h-21.83l4-8.17v-5.83h-33.67v-12.67l33.67-38.33h13.83v38.83h3.76" +
                            "l0.01-0.01c0.141,0.01,0.271,0.01,0.41,0.01C558.668,448.333,562.79,442.305,562.79,442.305z M538.746,434.624" +
                            "c-0.04-2.41-2.2-4.17-4.311-4.19c-2.6-0.02-4.71,2.65-4.71,2.65c-0.01,0.01-0.01,0.02-0.02,0.03l-5.55,6.34" +
                            "c0,0-2.28,2.51-2.53,3.92c-0.68,3.83,2.12,4.92,4.23,4.96h8.909c1.96-0.34,3.521-1.82,3.98-3.73V434.624z",
                    // 5: 5
//                    "M432.333,517.24c-0.01,0.14-0.02,0.29-0.02,0.43" +
//                            "s0.01,0.29,0.02,0.43c0.21,2.03,1.88,3.631,3.94,3.73c0.069,0.01,0.14,0.01,0.21,0.01c0.069,0,0.149,0,0.22-0.01h19.47" +
//                            "c5.73,0.66,10.16,5.52,10.16,11.42v20.25c0,6.36-5.14,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.14-11.5-11.5v-11.16" +
//                            "c0,0,14.43,0.04,14.5,0.061c-0.062,5.662,2.312,8.912,7.99,8.939c0.332,0.002,0.67,0.061,1.01,0.061s3.06-0.021,3.38-0.061" +
//                            "c4.351-0.479,7.72-4.17,7.72-8.64s-3.38-8.16-7.729-8.641c-0.32-0.04-0.64-0.06-0.97-0.06h-25.9v-34h49.333" +
//                            "c0,0,0.772,0.062,0.959,0.469c0.287,0.623,0.027,0.981,0.027,0.981l-9.819,12.05h-21.85" +
//                            "C434.323,513.5,432.553,515.14,432.333,517.24z",
                    "M439.333,517.24c-0.01,0.14-0.02,0.29-0.02,0.43" +
                            "s0.01,0.29,0.02,0.43c0.21,2.03,1.88,3.631,3.94,3.73c0.069,0.01,0.14,0.01,0.21,0.01c0.069,0,0.149,0,0.22-0.01h19.47" +
                            "c5.73,0.66,10.16,5.52,10.16,11.42v20.25c0,6.36-5.14,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.14-11.5-11.5v-11.16" +
                            "c0,0,14.43,0.04,14.5,0.061c-0.062,5.662,2.312,8.912,7.99,8.939c0.332,0.002,0.67,0.061,1.01,0.061s3.06-0.021,3.38-0.061" +
                            "c4.351-0.479,7.72-4.17,7.72-8.64s-3.38-8.16-7.729-8.641c-0.32-0.04-0.64-0.06-0.97-0.06h-25.9v-34h49.333" +
                            "c0,0,0.772,0.062,0.959,0.469c0.287,0.623,0.027,0.981,0.027,0.981l-9.819,12.05h-21.85" +
                            "C441.323,513.5,439.553,515.14,439.333,517.24z",
                    // 6: 6
                    "M330.701,555.8v-1.5c0-2.64-2.061-4.81-4.66-4.979" +
                            "c-0.111-0.021-0.57-0.021-0.68,0h-9.421c-0.11-0.021-0.569-0.021-0.681,0c-2.6,0.17-4.659,2.34-4.659,4.979v2.05" +
                            "c0.28,2.24,2.06,4.021,4.3,4.301h18.33c6.1,0,11.279,4.75,11.67,10.76v18.57c0,6.359-5.15,11.5-11.5,11.5h-25.5" +
                            "c-6.359,0-11.5-5.141-11.5-11.5v-42c0-6.351,5.141-11.5,11.5-11.5h25.5c6.35,0,11.5,5.149,11.5,11.5v7.819H330.701z" +
                            " M310.601,578.95v3.6c-0.038,2.825,2.087,4.763,4.71,4.771h10.721c2.781-0.008,4.656-2.07,4.67-4.61v-4.4" +
                            "c-0.301-2.34-2.221-4.18-4.611-4.329h-11C312.57,574.23,310.601,576.36,310.601,578.95z",
                    // 7: 7
//                    "M227.804,505.484v9.9l-20.5,49.8h-17.5l21.67-50" +
//                            "h-26l-6.17,13.8h-4v-23.5c0-2.76,2.239-5,5-5h42.5C225.573,500.484,227.804,502.725,227.804,505.484z",
                    "M216.804,505.484v9.9l-20.5,49.8h-17.5l21.67-50" +
                            "h-26l-6.17,13.8h-4v-23.5c0-2.76,2.239-5,5-5h42.5C214.573,500.484,216.804,502.725,216.804,505.484z",
                    // 8: 8
                    "M130.283,436.867l-2.449,3.21l2.67,3.62l0.66,0.88" +
                            "c0.42,0.729,0.67,1.59,0.67,2.5v22.09c0,2.76-2.24,5-5,5h-38.5c-2.76,0-5-2.24-5-5v-22.09c0-0.91,0.25-1.771,0.67-2.5l0.66-0.88" +
                            "l2.67-3.62l-2.451-3.21l-0.949-1.24c0,0,0,0-0.01-0.01c-0.37-0.7-0.59-1.51-0.59-2.37v-18.26c0-2.761,2.24-5,5-5h38.5" +
                            "c2.76,0,5,2.239,5,5v18.26c0,0.86-0.221,1.67-0.59,2.37c-0.011,0.01-0.011,0.01-0.011,0.01L130.283,436.867z M117.633,451.167" +
                            "c-0.299-2.34-2.219-4.18-4.609-4.33h-10.88c-2.39,0.15-4.31,1.99-4.61,4.33v4.61c-0.01,2.89,2.051,5.14,4.86,5.229h10.431" +
                            "c2.699-0.03,4.76-1.78,4.809-4.87V451.167z M117.644,428.296c0-2.76-2.24-5-5-5h-10.101c-2.76,0-5,2.24-5,5v3.5" +
                            "c0,2.76,2.24,5,5,5h10.101c2.76,0,5-2.24,5-5V428.296z",
                    // 9: 9
                    "M59.933,333.828v1.5c0,2.64,2.061,4.81,4.66,4.979" +
                            "c0.111,0.021,10.67,0.021,10.781,0c2.6-0.17,4.659-2.34,4.659-4.979v-2.05c-0.28-2.24-2.06-4.021-4.3-4.301h-18.33" +
                            "c-6.1,0-11.279-4.75-11.67-10.76v-18.57c0-6.359,5.15-11.5,11.5-11.5h25.5c6.359,0,11.5,5.141,11.5,11.5v42" +
                            "c0,6.351-5.141,11.5-11.5,11.5h-25.5c-6.35,0-11.5-5.149-11.5-11.5v-7.819H59.933z M80.033,310.678v-3.6" +
                            "c0.038-2.825-2.087-4.763-4.71-4.771H64.603c-2.781,0.008-4.656,2.07-4.67,4.61v4.4c0.301,2.34,2.221,4.18,4.611,4.329h11" +
                            "C78.063,315.397,80.033,313.268,80.033,310.678z",
                    // 10: 10
                    // 10: 10-1
                    "M80.67,224v-37l-4.74-3.05l-0.68-0.43" +
                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V224l4,8.5h-25L80.67,224z" +
                    // 10: 10-0
                    "M157.67,174.01v54.32c0,2.76-2.24,5-5,5h-38.5" +
                            "c-2.77,0-5-2.24-5-5v-54.32c0-2.76,2.23-5,5-5h38.5C155.43,169.01,157.67,171.25,157.67,174.01z M144.22,187.71" +
                            "c0-2.76-2.39-5-5.34-5h-10.79c-2.95,0-5.34,2.24-5.34,5v27.5c0,2.76,2.39,5,5.34,5h10.79c2.95,0,5.34-2.24,5.34-5V187.71z",
                    // 11: 11
                    // 11: 11-L1
                    "M170.67,128V91l-4.74-3.05l-0.68-0.43" +
                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L170.67,128z" +
                    // 11: 11-R1
                    "M204.67,128V91l-4.74-3.05l-0.68-0.43" +
                            "c-0.23-0.26-0.37-0.6-0.37-0.97c0-0.48,0.23-0.92,0.6-1.18l0.19-0.12l17.97-12.23c0.01-0.01,0.01-0.01,0.02-0.02l0.5-0.3" +
                            "c0.33-0.17,0.7-0.26,1.09-0.26c1.17,0,2.15,0.79,2.42,1.86V128l4,8.5h-25L204.67,128z",
            };





//            String[][] mHourMarksSVG = new String[][] {
//                    // 0: 12
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[0],
//                            mRoundedTriangleSVG[0],
//                            mRoundedDashSVG[0],
//                            mSmallPointerSVG[0],
//                            mDigitsSVG[0],
//                            mBigPointerSVG[0],
//                            mDotSVG[0],
//                    },
//                    // 1
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[1],
//                            mRoundedTriangleSVG[1],
//                            mRoundedDashSVG[1],
//                            mSmallPointerSVG[1],
//                            mDigitsSVG[1],
//                            mBigPointerSVG[1],
//                            mDotSVG[1],
//                    },
//                    // 2
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[2],
//                            mRoundedTriangleSVG[2],
//                            mRoundedDashSVG[2],
//                            mSmallPointerSVG[2],
//                            mDigitsSVG[2],
//                            mBigPointerSVG[2],
//                            mDotSVG[2],
//                    },
//                    // 3
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[3],
//                            mRoundedTriangleSVG[3],
//                            mRoundedDashSVG[3],
//                            mSmallPointerSVG[3],
//                            mDigitsSVG[3],
//                            mBigPointerSVG[3],
//                            mDotSVG[3],
//                    },
//                    // 4
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[4],
//                            mRoundedTriangleSVG[4],
//                            mRoundedDashSVG[4],
//                            mSmallPointerSVG[4],
//                            mDigitsSVG[4],
//                            mBigPointerSVG[4],
//                            mDotSVG[4],
//                    },
//                    // 5
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[5],
//                            mRoundedTriangleSVG[5],
//                            mRoundedDashSVG[5],
//                            mSmallPointerSVG[5],
//                            mDigitsSVG[5],
//                            mBigPointerSVG[5],
//                            mDotSVG[5],
//                    },
//                    // 6
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[6],
//                            mRoundedTriangleSVG[6],
//                            mRoundedDashSVG[6],
//                            mSmallPointerSVG[6],
//                            mDigitsSVG[6],
//                            mBigPointerSVG[6],
//                            mDotSVG[6],
//                    },
//                    // 7
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[7],
//                            mRoundedTriangleSVG[7],
//                            mRoundedDashSVG[7],
//                            mSmallPointerSVG[7],
//                            mDigitsSVG[7],
//                            mBigPointerSVG[7],
//                            mDotSVG[7],
//                    },
//                    // 8
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[8],
//                            mRoundedTriangleSVG[8],
//                            mRoundedDashSVG[8],
//                            mSmallPointerSVG[8],
//                            mDigitsSVG[8],
//                            mBigPointerSVG[8],
//                            mDotSVG[8],
//                    },
//                    // 9
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[9],
//                            mRoundedTriangleSVG[9],
//                            mRoundedDashSVG[9],
//                            mSmallPointerSVG[9],
//                            mDigitsSVG[9],
//                            mBigPointerSVG[9],
//                            mDotSVG[9],
//                    },
//                    // 10
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[10],
//                            mRoundedTriangleSVG[10],
//                            mRoundedDashSVG[10],
//                            mSmallPointerSVG[10],
//                            mDigitsSVG[10],
//                            mBigPointerSVG[10],
//                            mDotSVG[10],
//                    },
//                    // 11
//                    new String[] {
//                            /*mDigitsSVG*/mDigitsArtDecoSVG[11],
//                            mRoundedTriangleSVG[11],
//                            mRoundedDashSVG[11],
//                            mSmallPointerSVG[11],
//                            mDigitsSVG[11],
//                            mBigPointerSVG[11],
//                            mDotSVG[11],
//                    },
//            };


            String[][] mHourMarksSVG = new String[][] {
                    // 0: 12
                    new String[] {
                            mDigitsSVG[0],
                            mRoundedTriangleSVG[0],
                            mRoundedDashSVG[0],
                            mSmallPointerSVG[0],
                            /*mBulletSVG*/mDigitsArtDecoSVG[0],
                            mBigPointerSVG[0],
                            mDotSVG[0],
                    },
                    // 1
                    new String[] {
                            mDigitsSVG[1],
                            mRoundedTriangleSVG[1],
                            mRoundedDashSVG[1],
                            mSmallPointerSVG[1],
                            /*mBulletSVG*/mDigitsArtDecoSVG[1],
                            mBigPointerSVG[1],
                            mDotSVG[1],
                    },
                    // 2
                    new String[] {
                            mDigitsSVG[2],
                            mRoundedTriangleSVG[2],
                            mRoundedDashSVG[2],
                            mSmallPointerSVG[2],
                            /*mBulletSVG*/mDigitsArtDecoSVG[2],
                            mBigPointerSVG[2],
                            mDotSVG[2],
                    },
                    // 3
                    new String[] {
                            mDigitsSVG[3],
                            mRoundedTriangleSVG[3],
                            mRoundedDashSVG[3],
                            mSmallPointerSVG[3],
                            /*mBulletSVG*/mDigitsArtDecoSVG[3],
                            mBigPointerSVG[3],
                            mDotSVG[3],
                    },
                    // 4
                    new String[] {
                            mDigitsSVG[4],
                            mRoundedTriangleSVG[4],
                            mRoundedDashSVG[4],
                            mSmallPointerSVG[4],
                            /*mBulletSVG*/mDigitsArtDecoSVG[4],
                            mBigPointerSVG[4],
                            mDotSVG[4],
                    },
                    // 5
                    new String[] {
                            mDigitsSVG[5],
                            mRoundedTriangleSVG[5],
                            mRoundedDashSVG[5],
                            mSmallPointerSVG[5],
                            /*mBulletSVG*/mDigitsArtDecoSVG[5],
                            mBigPointerSVG[5],
                            mDotSVG[5],
                    },
                    // 6
                    new String[] {
                            mDigitsSVG[6],
                            mRoundedTriangleSVG[6],
                            mRoundedDashSVG[6],
                            mSmallPointerSVG[6],
                            /*mBulletSVG*/mDigitsArtDecoSVG[6],
                            mBigPointerSVG[6],
                            mDotSVG[6],
                    },
                    // 7
                    new String[] {
                            mDigitsSVG[7],
                            mRoundedTriangleSVG[7],
                            mRoundedDashSVG[7],
                            mSmallPointerSVG[7],
                            /*mBulletSVG*/mDigitsArtDecoSVG[7],
                            mBigPointerSVG[7],
                            mDotSVG[7],
                    },
                    // 8
                    new String[] {
                            mDigitsSVG[8],
                            mRoundedTriangleSVG[8],
                            mRoundedDashSVG[8],
                            mSmallPointerSVG[8],
                            /*mBulletSVG*/mDigitsArtDecoSVG[8],
                            mBigPointerSVG[8],
                            mDotSVG[8],
                    },
                    // 9
                    new String[] {
                            mDigitsSVG[9],
                            mRoundedTriangleSVG[9],
                            mRoundedDashSVG[9],
                            mSmallPointerSVG[9],
                            /*mBulletSVG*/mDigitsArtDecoSVG[9],
                            mBigPointerSVG[9],
                            mDotSVG[9],
                    },
                    // 10
                    new String[] {
                            mDigitsSVG[10],
                            mRoundedTriangleSVG[10],
                            mRoundedDashSVG[10],
                            mSmallPointerSVG[10],
                            /*mBulletSVG*/mDigitsArtDecoSVG[10],
                            mBigPointerSVG[10],
                            mDotSVG[10],
                    },
                    // 11
                    new String[] {
                            mDigitsSVG[11],
                            mRoundedTriangleSVG[11],
                            mRoundedDashSVG[11],
                            mSmallPointerSVG[11],
                            /*mBulletSVG*/mDigitsArtDecoSVG[11],
                            mBigPointerSVG[11],
                            mDotSVG[11],
                    },
            };






            String[] mPlatesSVG = new String[] {
                    // 0: square
                    "M0,640h640V0H0V640z",
                    // 1: round
                    "M320,0C143.27,0,0,143.27,0,320s143.27,320,320,320s320-143.27,320-320S496.73,0,320,0z",
            };




            String[] mDialPlatesSVG;
//            = new String[] {
//                    // DIAL_SINGLE_HOLE_VRT = 0
//                    new String(
//                            mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_VRT]
//                            + mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] +  mDigitsSVG[4] + mDigitsSVG[5]
//                            + mDigitsSVG[6] + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11]
//                    ),
//                    // DIAL_TRIPLE_HOLE_VRT = 1
//                    new String(
//                            mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_VRT]
//                            + mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[4] + mDigitsSVG[5]
//                            + mDigitsSVG[6] + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11]
//                    ),
//                    // DIAL_SINGLE_HOLE_HRZ = 2
//                    new String(
//                            mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_HRZ]
//                            + mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[3] + mDigitsSVG[4] + mDigitsSVG[5]
//                            + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11]
//                    ),
//                    // DIAL_TRIPLE_HOLE_HRZ = 3
//                    new String(
//                            mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ]
//                            + mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[3] + mDigitsSVG[4] + mDigitsSVG[5]
//                            + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11]
//                    ),
//            };


// DIAL_SINGLE_HOLE_VRT = 0;
// DIAL_TRIPLE_HOLE_VRT = 1;
// DIAL_SINGLE_HOLE_HRZ = 2;
// DIAL_TRIPLE_HOLE_HRZ = 3;




            Path[] mDialPlatesPaths;
//            = new Path[] {
//                    new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_SINGLE_HOLE_VRT])), // 0 DIAL_SINGLE_HOLE_VRT
//                    new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_TRIPLE_HOLE_VRT])), // 1 DIAL_TRIPLE_HOLE_VRT
//                    new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_SINGLE_HOLE_HRZ])), // 2 DIAL_SINGLE_HOLE_HRZ
//                    new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ])), // 3 DIAL_TRIPLE_HOLE_HRZ
//            };
            //
            Path[] mDialPlatesPathsScaled;


            Path[] mHourMarkPaths, mHourMarkPathsScaled;
//            = new Path[] {
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[0])), // 12
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[1])), // 1
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[2])), // 2
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[3])), // 3
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[4])), // 4
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[5])), // 5
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[6])), // 6
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[7])), // 7
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[8])), // 8
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[9])), // 9
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[10])), // 10
//                    new Path(PathFromPathDataSVG.doPath(mDigitsSVG[11])), // 11
//            };
            //
//            public Path[] mEffectiveHourMarksPath, mEffectiveHourMarksPathScaled;

            Path[] mDateCutPaths = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_VRT])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_VRT])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_HRZ])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ])),
            };
            //
            Path[] mDateCutPathsScaled;

            Path[] mDateCutOutlinePaths = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mDateCutOutlineSVG[ACommon.DIAL_SINGLE_HOLE_VRT])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutOutlineSVG[ACommon.DIAL_TRIPLE_HOLE_VRT])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutOutlineSVG[ACommon.DIAL_SINGLE_HOLE_HRZ])),
                    new Path(PathFromPathDataSVG.doPath(mDateCutOutlineSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ])),
            };
            //
            Path[] mDateCutOutlinePathsScaled;



            public void buildDialPaths(WatchAppearance appearance) {

//                mDialPlatesSVG = new String[] {
//                        // DIAL_SINGLE_HOLE_VRT = 0
//                        mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_VRT] +
//                            mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[4] + mDigitsSVG[5] +
//                            mDigitsSVG[6] + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11],
//                        // DIAL_TRIPLE_HOLE_VRT = 1
//                        mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_VRT] +
//                            mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[4] + mDigitsSVG[5] +
//                            mDigitsSVG[6] + mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11],
//                        // DIAL_SINGLE_HOLE_HRZ = 2
//                        mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_SINGLE_HOLE_HRZ] +
//                            mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[3] + mDigitsSVG[4] + mDigitsSVG[5] +
//                            mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11],
//                        // DIAL_TRIPLE_HOLE_HRZ = 3
//                        mPlatesSVG[1] + mDateCutSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ] +
//                            mDigitsSVG[0] + mDigitsSVG[1] + mDigitsSVG[2] + mDigitsSVG[3] + mDigitsSVG[4] + mDigitsSVG[5] +
//                            mDigitsSVG[7] + mDigitsSVG[8] + mDigitsSVG[9] + mDigitsSVG[10] + mDigitsSVG[11],
//                };
                mDialPlatesSVG = new String[ACommon.NUM_BACKGROUNDS];
                for (int i=0; i < ACommon.NUM_BACKGROUNDS; i++) {
                    int markIndex;
                    String dialPlateSVG = mPlatesSVG[1] + mDateCutSVG[i];

                    for (int hm=0; hm < WatchAppearance.NUM_HOUR_MARKS; hm++) {
                        if (3 == hm &&
                                (ACommon.DIAL_SINGLE_HOLE_VRT == i || ACommon.DIAL_TRIPLE_HOLE_VRT == i)) continue;
                        if (6 == hm &&
                                (ACommon.DIAL_SINGLE_HOLE_HRZ == i || ACommon.DIAL_TRIPLE_HOLE_HRZ == i)) continue;
                        markIndex = (int) appearance.mHourMarksIndex[hm] - 1; // -1 == none hour mark !!!
                        if (-1 == markIndex) continue;
                        else dialPlateSVG += mHourMarksSVG[hm][markIndex];
                    }

                    mDialPlatesSVG[i] = dialPlateSVG;
                    //Log.i(TAG, "((( buildDialPaths, mDialPlatesSVG[" + i + "] = " + mDialPlatesSVG[i]);
                }

//                mHourMarkPaths = new Path[] {
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[0])), // 12
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[1])), // 1
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[2])), // 2
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[3])), // 3
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[4])), // 4
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[5])), // 5
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[6])), // 6
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[7])), // 7
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[8])), // 8
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[9])), // 9
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[10])), // 10
//                        new Path(PathFromPathDataSVG.doPath(mDigitsSVG[11])), // 11
//                };
                String effectiveHourMarksSVG = "";
                mHourMarkPaths = new Path[WatchAppearance.NUM_HOUR_MARKS];
                for (int i=0; i < WatchAppearance.NUM_HOUR_MARKS; i++) {
                    int markIndex = (int) appearance.mHourMarksIndex[i];
                    markIndex--; // -1 == none hour mark !!!
                    if (-1 == markIndex) {
                        mHourMarkPaths[i] = null;
                    } else {
                        mHourMarkPaths[i] = new Path(PathFromPathDataSVG.doPath(mHourMarksSVG[i][markIndex]));
                        effectiveHourMarksSVG += mHourMarksSVG[i][markIndex];
                    }
//                    Log.i(TAG, "((( buildDialPaths, mHourMarksIndex[" + i + "]=" + appearance.mHourMarksIndex[i] +
//                            ", mHourMarkPaths[" + i + "] = " + mHourMarkPaths[i]);
                }

                mDialPlatesPaths = new Path[] {
                        new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_SINGLE_HOLE_VRT])), // 0 DIAL_SINGLE_HOLE_VRT
                        new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_TRIPLE_HOLE_VRT])), // 1 DIAL_TRIPLE_HOLE_VRT
                        new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_SINGLE_HOLE_HRZ])), // 2 DIAL_SINGLE_HOLE_HRZ
                        new Path(PathFromPathDataSVG.doPath(mDialPlatesSVG[ACommon.DIAL_TRIPLE_HOLE_HRZ])), // 3 DIAL_TRIPLE_HOLE_HRZ
                };

//                mEffectiveHourMarksPath = new Path[1];
//                mEffectiveHourMarksPath[0] = new Path(PathFromPathDataSVG.doPath(effectiveHourMarksSVG));

            } // buildDialPaths

            private Path[] scalePathArray(Path[] srcArr, Matrix matrix, Path.FillType fillType) {
                if (null == srcArr) return null;

                int arrSize = srcArr.length;
                Path[] resultArr = new Path[arrSize];

                for (int i=0; i < arrSize; i++) {
                    Path row = null;
                    if (null != srcArr[i]) {
                        row = new Path();
                        srcArr[i].transform(matrix, row);
                    }
                    resultArr[i] = row;
                    if (null != fillType) {
                        resultArr[i].setFillType(fillType);
                    }
                }

                return resultArr;
            } // scalePathArray

            public void scaleDialPaths(float targetWidth) {
//                Path tmpPath = new Path();
//                //Matrix matrix = new Matrix();
//                int scaledWidth = ((int) (mVars.width * mVars.mScaleEffective));
//                int scaledHeight = ((int) (mVars.height * mVars.mScaleEffective));
//                float ratioX = scaledWidth / (float) srcBitmap.getWidth();
//                float ratioY = scaledHeight / (float) srcBitmap.getHeight();
//                float middleX = scaledWidth / 2.0f;
//                float middleY = scaledHeight / 2.0f;
//                //matrix.postScale(ratioX, ratioY, middleX, middleY);
//                //matrix.postScale(ratioX, ratioY, mVars.width / 2f, mVars.height / 2f);
//                matrix.postScale(ratioX, ratioY);
//                matrix.postTranslate(mVars.mBurnInMargin, mVars.mBurnInMargin);
//                //matrix.postS
//                dialElements.mDialPlatesPaths[index].transform(matrix, tmpPath);

                float bitmapRatio = targetWidth / baseDialWidth;
                Matrix matrix = new Matrix();
                float bitmapCenterX = (baseDialWidth / 2.0f) * bitmapRatio;
                float bitmapCenterY = (baseDialHeight / 2.0f) * bitmapRatio;
                float screenRatio = mVars.width / baseDialWidth;
                float pivotX = (baseDialWidth / 2.0f) * screenRatio;
                float pivotY = (baseDialHeight / 2.0f) * screenRatio;
                matrix.postScale(bitmapRatio, bitmapRatio);
                //matrix.postTranslate(mVars.mBurnInMargin, mVars.mBurnInMargin);
                matrix.postTranslate((pivotX - bitmapCenterX), (pivotY - bitmapCenterY));

                this.mDateCutPathsScaled = scalePathArray(this.mDateCutPaths, matrix, null);
                this.mDateCutOutlinePathsScaled = scalePathArray(this.mDateCutOutlinePaths, matrix, null);
                //this.mEffectiveHourMarksPathScaled = scalePathArray(this.mEffectiveHourMarksPath, matrix, null);
                //
                this.mHourMarkPathsScaled = scalePathArray(this.mHourMarkPaths, matrix, null); // null
                this.mDialPlatesPathsScaled = scalePathArray(this.mDialPlatesPaths, matrix, null);
                //
                this.mDecorUpperPathScaled = scalePathArray(this.mDecorUpperPath, matrix, null);
            } // scaleDialPaths

            public void scaleHandPaths(float targetWidth) {

//                hrMatrix.reset();
//                hrMatrix.setTranslate(-160f, -20f); // hand bitmap is 320x40
//                hrMatrix.postRotate(handRotDeg);
//                hrMatrix.postTranslate(centerX, centerY);

                float bitmapRatio = targetWidth / baseHandWidth;
                Matrix matrix = new Matrix();
                //matrix.setTranslate(-(baseHandWidth / 2.0f), -(baseHandHeight / 2.0f));
//                matrix.postScale(bitmapRatio, bitmapRatio);//, -(baseHandWidth / 2.0f), -(baseHandHeight / 2.0f));
                float bitmapCenterX = (baseHandWidth / 2.0f) * bitmapRatio;
                float bitmapCenterY = (baseHandHeight / 2.0f) * bitmapRatio;
                //Log.i(TAG, "((((( scaleMainHandPaths, bitmapCenterX=" + bitmapCenterX + ", bitmapCenterY=" + bitmapCenterY);
                float screenRatio = mVars.width / baseHandWidth;
                float pivotX = (baseHandWidth / 2.0f) * screenRatio;
                float pivotY = (baseHandHeight / 2.0f) * screenRatio;
                //Log.i(TAG, "((((( scaleMainHandPaths, bitmapRatio=" + bitmapRatio + ", screenRatio=" + screenRatio);
                //Log.i(TAG, "((((( scaleMainHandPaths, pivotX=" + pivotX + ", pivotY=" + pivotY);
                matrix.postScale(bitmapRatio, bitmapRatio);//, -(pivotX-bitmapCenterX) /*/ 2f*/, -(pivotY - bitmapCenterY) /*/ 2f*/);
                matrix.postTranslate((pivotX - bitmapCenterX), (pivotY - bitmapCenterY));

                this.mMainHandRhombusPathScaled = scalePathArray(this.mMainHandRhombusPath, matrix, Path.FillType.EVEN_ODD);
                this.mMainHandRhombusOutlinePathScaled = scalePathArray(this.mMainHandRhombusOutlinePath, matrix, null);
                this.mMainHandRhombusDecorPathScaled = scalePathArray(this.mMainHandRhombusDecorPath, matrix, null);
                //
                this.mMainHandStraightPathScaled = scalePathArray(this.mMainHandStraightPath, matrix, Path.FillType.EVEN_ODD);
                this.mMainHandStraightOutlinePathScaled = scalePathArray(this.mMainHandStraightOutlinePath, matrix, null);
                this.mMainHandStraightDecorPathScaled = scalePathArray(this.mMainHandStraightDecorPath, matrix, null);
                //
                this.mMainHandCurlHeadPathScaled = scalePathArray(this.mMainHandCurlHeadPath, matrix, Path.FillType.EVEN_ODD);
                this.mMainHandCurlHeadOutlinePathScaled = scalePathArray(this.mMainHandCurlHeadOutlinePath, matrix, null);
                this.mMainHandCurlHeadDecorPathScaled = scalePathArray(this.mMainHandCurlHeadDecorPath, matrix, null);
                //
                this.mMainHandArrowPathScaled = scalePathArray(this.mMainHandArrowPath, matrix, Path.FillType.EVEN_ODD);
                this.mMainHandArrowOutlinePathScaled = scalePathArray(this.mMainHandArrowOutlinePath, matrix, null);
                this.mMainHandArrowDecorPathScaled = scalePathArray(this.mMainHandArrowDecorPath, matrix, null);

                this.mAuxHandWeekdayPathScaled = scalePathArray(this.mAuxHandWeekdayPath, matrix, Path.FillType.EVEN_ODD);
                this.mAuxHandWearbattPathScaled = scalePathArray(this.mAuxHandWearbattPath, matrix, null);
                this.mAuxHandPhonebattPathScaled = scalePathArray(this.mAuxHandPhonebattPath, matrix, null);
            } // scaleHandPaths


//            String mainHandRhombusMinuteOutlineSVG = "M616,40l-5.32,2.74L493,54l-155.5-5.39c-3.18,6.449-9.82,10.89-17.5,10.89" +
//                    "c-10.77,0-19.5-8.73-19.5-19.5s8.73-19.5,19.5-19.5c7.68,0,14.32,4.44,17.5,10.89L493,26l117.84,11.28L616,40z";
//            String mainHandRhombusMinuteDecorSVG = "M493,50l-134.66-4c0,0,0-4.5,0-6s0-6,0-6L493,30l105.961,9.9L493,50z";
//            String mainHandRhombusHourOutlineSVG = "M481,40c0,2.16-3.32,2.74-3.32,2.74L415,60l-72.47-9.15" +
//                    "C338.49,59.22,329.92,65,320,65c-13.81,0-25-11.19-25-25s11.19-25,25-25c9.92,0,18.49,5.78,22.53,14.15L415,20l62.84,17.28" +
//                    "C477.84,37.28,481,37.84,481,40z";
//            String mainHandRhombusHourDecorSVG = "M465,41.6l-50,12.74l-66.66-8.5c0,0,0-4.38,0-5.84c0-1.455,0-5.82,0-5.82L415,25.6" +
//                    "l50,13V41.6z";
            String mainHandRhombusMinuteOutlineSVG = "M605,40l-2.32,2.74L499,57l-161.5-8.39c-3.18,6.449-9.82,10.89-17.5,10.89c-10.77,0-19.5-8.73-19.5-19.5" +
                    "s8.73-19.5,19.5-19.5c7.68,0,14.32,4.44,17.5,10.89L499,23l103.84,14.28L605,40z";
            String mainHandRhombusMinuteDecorSVG = "M498,52l-123.66-7c0,0,0-3.5,0-5s0-5,0-5L498,28l86,11v2L498,52z";
            String mainHandRhombusHourOutlineSVG = "M508,40l-2.32,2.74L440,60l-97.47-9.15C338.49,59.22,329.92,65,320,65" +
                    "c-13.81,0-25-11.19-25-25s11.19-25,25-25c9.92,0,18.49,5.78,22.53,14.15L440,20l65.84,17.28L508,40z";
            String mainHandRhombusHourDecorSVG = "M491,41.6l-52,12.74l-76.66-7.5c0,0,0-5.38,0-6.84c0-1.455,0-6.82,0-6.82L439,25.6" +
                    "l52,13V41.6z";
            //
            String mainHandStraightMinuteOutlineSVG = "M618,40l-28,12H335.37c-3.57,4.57-9.13,7.5-15.37,7.5" +
                    "c-10.77,0-19.5-8.73-19.5-19.5s8.73-19.5,19.5-19.5c6.24,0,11.8,2.93,15.37,7.5H590L618,40z";
            String mainHandStraightMinuteDecorSVG = "M589,48H356c0,0-6.24-7.895-6.24-8c0-0.124,6.24-8,6.24-8h233l19,8L589,48z";
//            String mainHandStraightHourOutlineSVG1 = "M487,40l-19,15H340c-4.56,6.07-11.82,10-20,10c-13.81,0-25-11.2-25-25c0-13.81,11.19-25,25-25" +
//                    "c8.18,0,15.44,3.93,20,10h128L487,40z";
//            String mainHandStraightHourDecorSVG1 = "M466,51H356.667c0,0-5.667-10.75-5.667-11c0-0.295,6.333-11,6.333-11H466l14,11L466,51z";
            String mainHandStraightHourOutlineSVG = "M505,40l-19,15H340c-4.56,6.07-11.82,10-20,10c-13.81,0-25-11.2-25-25c0-13.81,11.19-25,25-25" +
                    "c8.18,0,15.44,3.93,20,10h146L505,40z";
            String mainHandStraightHourDecorSVG = "M484,51H356.667c0,0-5.667-10.438-5.667-11s6.333-11,6.333-11H484l14,11L484,51z";
            //








            String mainHandSecondsOutlineSVG = "M624,40l-6,3l-285.63,0.98C330.7,49.21,325.79,53,320,53" +
                    "c-5.75,0-10.62-3.73-12.33-8.9L238,47V33l69.67,2.9c1.71-5.17,6.58-8.9,12.33-8.9c5.79,0,10.7,3.79,12.37,9.02L618,37L624,40z";
//            String mainHandRhombusMinuteSVG = new String(mainHandRhombusMinuteOutlineSVG + mainHandRhombusMinuteDecorSVG);
//            String mainHandRhombusHourSVG = new String(mainHandRhombusHourOutlineSVG + mainHandRhombusHourDecorSVG);
//            String mainHandStraightMinuteSVG = new String(mainHandStraightMinuteOutlineSVG + mainHandStraightMinuteDecorSVG);
//            String mainHandStraightHourSVG = new String(mainHandStraightHourDecorSVG  + " " + mainHandStraightHourOutlineSVG);
            String mainHandSecondsSVG = new String(mainHandSecondsOutlineSVG);

            

            
            
            
            //
            Path[] mMainHandRhombusOutlinePath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandRhombusHourOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandRhombusMinuteOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsOutlineSVG)),
            };
            Path[] mMainHandRhombusOutlinePathScaled;
            //
            Path[] mMainHandRhombusDecorPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandRhombusHourDecorSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandRhombusMinuteDecorSVG)),
                    null,
            };
            Path[] mMainHandRhombusDecorPathScaled;
            //
            Path[] mMainHandRhombusPath = new Path[] {
                    new Path(), //PathFromPathDataSVG.doPath(mainHandRhombusHourSVG)
                    new Path(), //PathFromPathDataSVG.doPath(mainHandRhombusMinuteSVG)
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsSVG)),
            };
            Path[] mMainHandRhombusPathScaled;


            //
            Path[] mMainHandStraightOutlinePath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandStraightHourOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandStraightMinuteOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsOutlineSVG)),
            };
            Path[] mMainHandStraightOutlinePathScaled;
            //
            Path[] mMainHandStraightDecorPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandStraightHourDecorSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandStraightMinuteDecorSVG)),
                    null,
            };
            Path[] mMainHandStraightDecorPathScaled;
            //
            Path[] mMainHandStraightPath = new Path[] {
                    new Path(), //PathFromPathDataSVG.doPath(mainHandStraightHourSVG)
                    new Path(), //PathFromPathDataSVG.doPath(mainHandStraightMinuteSVG)
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsSVG)), //
            };
            Path[] mMainHandStraightPathScaled;




            String auxHandBigOutlineSVG = "M426.34,40c0,2.75-5.31,3-5.31,3l-89.16,0.92c-1.64,4.98-6.34,8.58-11.87,8.58" +
                    "c-5.47,0-10.13-3.52-11.82-8.42L280,45l1-5l-1-5l28.18,0.92c1.69-4.9,6.35-8.42,11.82-8.42c5.53,0,10.23,3.6,11.87,8.58L421.03,37" +
                    "C421.03,37,426.34,37.25,426.34,40z";
            String auxHandSmallOutlineSVG = "M398,40l-9,4l-57.84,1.63c-2.06,4.07-6.28,6.87-11.16,6.87" +
                    "c-6.9,0-12.5-5.6-12.5-12.5s5.6-12.5,12.5-12.5c4.88,0,9.1,2.8,11.16,6.87L389,36L398,40z";
            String auxHandHHBattOutlineSVG = 
//                    "M398,40l-9,4l-54.52,1.54c-2.23,5.82-7.87,9.96-14.48,9.96c-6.2,0-11.56-3.65-14.03-8.91L297,47V33" +
//                    "l8.97,0.41c2.47-5.26,7.83-8.91,14.03-8.91c6.61,0,12.26,4.14,14.48,9.97L389,36L398,40z";
//                    "M400,40l-9,4.5l-55.17,1.69C333.36,52.52,327.21,57,320,57c-7.53,0-13.91-4.89-16.14-11.66L288,46l1-6l-1-6" +
//                            "l15.86,0.66C306.09,27.89,312.47,23,320,23c7.21,0,13.37,4.49,15.84,10.82L391,35.5L400,40z";
                    "M400,40l-11,3.5l-53.03,2.34C333.59,52.35,327.34,57,320,57c-7.53,0-13.91-4.89-16.15-11.67L286,46l2-6" +
                            "l-2-6l17.85,0.67C306.09,27.89,312.47,23,320,23c7.34,0,13.59,4.65,15.97,11.16L389,36.5L400,40z";

            Path[] mAuxHandWeekdayPathScaled, mAuxHandWeekdayPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(auxHandBigOutlineSVG)),
            };
            Path[] mAuxHandWearbattPathScaled, mAuxHandWearbattPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(auxHandSmallOutlineSVG)),
            };
            Path[] mAuxHandPhonebattPathScaled, mAuxHandPhonebattPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(auxHandHHBattOutlineSVG)),
            };



            String decorUpperCompositeSVG = "M320,9c-13.03,0-25.61,1.9-37.48,5.44l7.87,28.37c2.28-2.81,5.28-5.81,8.58-7.37" +
                    "L320,69.5l21.03-34.06c2.97,1.56,6.3,4.39,8.78,7.43l7.93-28.35C345.79,10.93,333.12,9,320,9z M347.33,32.33l-7.66-4.16L320,60" +
                    "l-19.17-31.83L293,32.33l-4-13.83c0,0,11.67-4.17,31-4.17c20.33,0,31.33,4,31.33,4L347.33,32.33z";
            String decorUpperOutlineSVG = "M357.74,14.52l-7.93,28.35c-2.48-3.04-5.81-5.87-8.78-7.43L320,69.5l-21.03-34.06" +
                    "c-3.3,1.56-6.3,4.56-8.58,7.37l-7.87-28.37C294.39,10.9,306.97,9,320,9C333.12,9,345.79,10.93,357.74,14.52z";
            String decorUpperDecorSVG = "M339.667,28.167L320,60l-19.167-31.833L293,32.333L289,18.5" +
                    "c0,0,11.667-4.167,31-4.167c20.333,0,31.333,4,31.333,4l-4,14L339.667,28.167z";
            //
            Path[] mDecorUpperPathScaled, mDecorUpperPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(decorUpperCompositeSVG)),
                    new Path(PathFromPathDataSVG.doPath(decorUpperOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(decorUpperDecorSVG)),
            };






/*
			<g id="decorPath2">
				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M423,40l-2.7,6c-10.301,0-20.153,0-28.8,0
					c-18.517,0-31.5,0-31.5,0l-3-6l3-6c0,0,27.94,0,60.3,0L423,40z"/>
				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M597,40l-3,1.11
					c-9,3.559-58.667,10.726-75.28,10.439c-3.345-3.425-13.595-5.675-21.05-5.55h-9.3l3.3-6l-3.3-6h9.3
					c7.33,0,17.83-2.125,21.02-5.56c15.976,0.062,66.976,7.562,75.31,10.449L597,40z"/>
				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M488,40l-3.3,6c0,0-31.2,0-32.7,0
					c-1.75,0-28.03,0-28.03,0l2.7-6l-2.7-6h60.73L488,40z"/>
			</g>
			<path id="outlinePath2" style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M612,40l-4,2
				c-17,7.002-72.334,14.336-91.58,14.57c-2.087-3.901-10.17-6.155-18.88-6.15L336.49,50.4c-3.45,5.47-9.55,9.1-16.49,9.1
				c-10.77,0-19.5-8.73-19.5-19.5s8.73-19.5,19.5-19.5c6.94,0,13.04,3.63,16.49,9.1l161.05-0.02
				c8.877-0.082,17.126-2.244,18.88-6.15C534.333,23.336,589.333,30.669,608,38L612,40z"/>

				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M426,40l-2.7,6c-10.301,0-23.153,0-31.8,0
					c-18.517,0-31.5,0-31.5,0l-3-6l3-6c0,0,30.94,0,63.3,0L426,40z"/>
				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M597,40l-3,1.11
					c-9,3.559-58.667,10.726-75.28,10.439c-3.345-3.425-13.595-5.675-21.05-5.55h-1.3l3.3-6l-3.3-6h1.3
					c7.33,0,17.83-2.125,21.02-5.56c15.976,0.062,66.976,7.562,75.31,10.449L597,40z"/>
				<path style="fill:none;stroke:#000000;stroke-width:0.5;stroke-miterlimit:10;" d="M496,40l-3.3,6c0,0-39.2,0-40.7,0
					c-1.75,0-25.03,0-25.03,0l2.7-6l-2.7-6h65.73L496,40z"/>
*/
            String mainHandArrowHourDecorSVG = "M451.83,42.35l11.11,7.4c-11.54,3.9-19.93,7.61-25.29,12.06" +
                    "c-3.93-5.35-9.63-9.42-16.22-11.92l5.4-7.54H451.83z " +
                    "M422.67,40l-6.01,8.39c-3.61-0.909-7.41-1.39-11.28-1.39" +
                    "h-45.55l-4-7l4-7h45.55c3.87,0,7.67-0.48,11.28-1.39L422.67,40z " +
                    "M501.54,40c-10.85,1.76-21.03,4.19-30.08,7" +
                    "c-1.05,0.33-2.09,0.65-3.1,0.98L456.57,40l11.79-7.98c1.01,0.33,2.05,0.65,3.1,0.98C480.51,35.81,490.69,38.24,501.54,40z " +
                    "M462.94,30.25l-11.11,7.4h-25l-5.4-7.54" +
                    "c6.59-2.5,12.29-6.57,16.22-11.92C443.01,22.64,451.4,26.35,462.94,30.25z";
            String mainHandArrowHourOutlineSVG = "M527,37.72l2,2.28l-2,2.28c-20.34,1.029-39.68,4.8-55.54,9.72" +
                    "c-16.02,4.98-28.48,11.12-34.81,16.81C429.94,58.68,418.44,52,405.38,52h-63.45c-4.24,7.75-12.47,13-21.93,13" +
                    "c-13.81,0-25-11.19-25-25s11.19-25,25-25c9.46,0,17.69,5.25,21.93,13h63.45c13.06,0,24.56-6.68,31.27-16.81" +
                    "c6.33,5.689,18.79,11.829,34.81,16.81C487.32,32.92,506.66,36.69,527,37.72z";
            //
            String mainHandArrowMinuteDecorSVG =
                    "M426,40l-2.7,6c-10.301,0-23.153,0-31.8,0" +
                            "c-18.517,0-31.5,0-31.5,0l-3-6l3-6c0,0,30.94,0,63.3,0L426,40z " +
                    "M597,40l-3,1.11" +
                            "c-9,3.559-58.667,10.726-75.28,10.439c-3.345-3.425-13.595-5.675-21.05-5.55h-1.3l3.3-6l-3.3-6h1.3" +
                            "c7.33,0,17.83-2.125,21.02-5.56c15.976,0.062,66.976,7.562,75.31,10.449L597,40z " +
                    "M496,40l-3.3,6c0,0-39.2,0-40.7,0" +
                            "c-1.75,0-25.03,0-25.03,0l2.7-6l-2.7-6h65.73L496,40z";
//                    "M423,40l-2.7,6c-10.301,0-20.153,0-28.8,0" +
//                    "c-18.517,0-31.5,0-31.5,0l-3-6l3-6c0,0,27.94,0,60.3,0L423,40z " +
//                    "M579,40l-2,1.11" +
//                    "c-9.47,1.14-25.65,3.34-41.28,8.939c-3.47-1.67-12.14-4.05-18.05-4.05h-29.3l-2.7-6l2.7-6h29.3c5.89,0,14.08-2,18.02-4.06" +
//                    "c14.9,5.569,32.58,7.81,41.31,8.949L579,40z " +
//                    "M482,40l2.7,6c0,0-31.2,0-32.7,0" +
//                    "c-1.75,0-28.03,0-28.03,0l2.7-6l-2.7-6h60.73L482,40z";
            String mainHandArrowMinuteOutlineSVG = 
                    "M612,40l-4,2" +
                            "c-17,7.002-72.334,14.336-91.58,14.57c-2.087-3.901-10.17-6.155-18.88-6.15L336.49,50.4c-3.45,5.47-9.55,9.1-16.49,9.1" +
                            "c-10.77,0-19.5-8.73-19.5-19.5s8.73-19.5,19.5-19.5c6.94,0,13.04,3.63,16.49,9.1l161.05-0.02" +
                            "c8.877-0.082,17.126-2.244,18.88-6.15C534.333,23.336,589.333,30.669,608,38L612,40z";
//                    "M611,40l-3,2" +
//                    "c-20.05,0.99-55.06,5.87-72.58,13.57c-3.795-2.57-13.29-4.95-18.88-5.15L336.49,50.4c-3.45,5.47-9.55,9.1-16.49,9.1" +
//                    "c-10.77,0-19.5-8.73-19.5-19.5s8.73-19.5,19.5-19.5c6.94,0,13.04,3.63,16.49,9.1l180.05-0.02c5.59-0.2,15.21-2.705,18.88-5.15" +
//                    "C552.94,32.13,587.95,37.01,608,38L611,40z";

            Path[] mMainHandArrowOutlinePath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandArrowHourOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandArrowMinuteOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsOutlineSVG)),
            };
            Path[] mMainHandArrowOutlinePathScaled;
            //
            Path[] mMainHandArrowDecorPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandArrowHourDecorSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandArrowMinuteDecorSVG)),
                    null,
            };
            Path[] mMainHandArrowDecorPathScaled;
            //
            Path[] mMainHandArrowPath = new Path[] {
                    new Path(), //PathFromPathDataSVG.doPath(mainHandArrowHourSVG)
                    new Path(), //PathFromPathDataSVG.doPath(mainHandArrowMinuteSVG)
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsSVG)),
            };
            Path[] mMainHandArrowPathScaled;








/*
			<g id="MHcHpath">
				<path id="outlinepath" style="fill:none;stroke:#000000;stroke-miterlimit:10;" d="M527,37.62v4.76
					c-16.32,1.521-31.29,7.65-43.61,17.09L342.3,51.29C338.19,59.43,329.75,65,320,65c-13.81,0-25-11.19-25-25s11.19-25,25-25
					c9.75,0,18.19,5.57,22.3,13.71l141.09-8.18C495.71,29.97,510.68,36.1,527,37.62z"/>
				<path id="decorpath" style="fill:none;stroke:#000000;stroke-miterlimit:10;" d="M503,38.74l1,1.26l-1,1.26
					c-7.71,2.87-14.93,6.76-21.5,11.51L363,46V34l118.5-6.77C488.07,31.98,495.29,35.87,503,38.74z"/>
			</g>
			<g id="MHcMpath">
				<path id="decorpath_1_" style="fill:none;stroke:#000000;stroke-miterlimit:10;" d="M575,38.89v2.221
					c-8.42,2.13-16.58,4.909-24.43,8.279L375,45V35l175.57-4.39C558.42,33.98,566.58,36.76,575,38.89z"/>
				<path id="outlinepath_1_" style="fill:none;stroke:#000000;stroke-miterlimit:10;" d="M608,37.8v4.4
					c-20.1,0.99-39.22,5.689-56.7,13.42L336.49,50.4c-3.45,5.47-9.55,9.1-16.49,9.1c-10.77,0-19.5-8.73-19.5-19.5
					s8.73-19.5,19.5-19.5c6.94,0,13.04,3.63,16.49,9.1l214.81-5.22C568.78,32.11,587.9,36.81,608,37.8z"/>
			</g>
*/

            String mainHandCurlHeadMinuteOutlineSVG = "M608,37.8v4.4" +
                    "c-20.1,0.99-39.22,5.689-56.7,13.42L336.49,50.4c-3.45,5.47-9.55,9.1-16.49,9.1c-10.77,0-19.5-8.73-19.5-19.5" +
                    "s8.73-19.5,19.5-19.5c6.94,0,13.04,3.63,16.49,9.1l214.81-5.22C568.78,32.11,587.9,36.81,608,37.8z";
            String mainHandCurlHeadMinuteDecorSVG = "M575,38.89v2.221" +
                    "c-8.42,2.13-16.58,4.909-24.43,8.279L375,45V35l175.57-4.39C558.42,33.98,566.58,36.76,575,38.89z";
            String mainHandCurlHeadHourOutlineSVG = "M527,37.62v4.76" +
                    "c-16.32,1.521-31.29,7.65-43.61,17.09L342.3,51.29C338.19,59.43,329.75,65,320,65c-13.81,0-25-11.19-25-25s11.19-25,25-25" +
                    "c9.75,0,18.19,5.57,22.3,13.71l141.09-8.18C495.71,29.97,510.68,36.1,527,37.62z";
            String mainHandCurlHeadHourDecorSVG = "M503,38.74l1,1.26l-1,1.26" +
                    "c-7.71,2.87-14.93,6.76-21.5,11.51L363,46V34l118.5-6.77C488.07,31.98,495.29,35.87,503,38.74z";

            Path[] mMainHandCurlHeadOutlinePath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandCurlHeadHourOutlineSVG)), //mainHandCurlHeadHourOutlineSVG
                    new Path(PathFromPathDataSVG.doPath(mainHandCurlHeadMinuteOutlineSVG)),
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsOutlineSVG)),
            };
            Path[] mMainHandCurlHeadOutlinePathScaled;
            //
            Path[] mMainHandCurlHeadDecorPath = new Path[] {
                    new Path(PathFromPathDataSVG.doPath(mainHandCurlHeadHourDecorSVG)), //mainHandCurlHeadHourDecorSVG
                    new Path(PathFromPathDataSVG.doPath(mainHandCurlHeadMinuteDecorSVG)),
                    null,
            };
            Path[] mMainHandCurlHeadDecorPathScaled;
            //
            Path[] mMainHandCurlHeadPath = new Path[] {
                    new Path(), //PathFromPathDataSVG.doPath(mainHandCurlHeadHourSVG)
                    new Path(), //PathFromPathDataSVG.doPath(mainHandCurlHeadMinuteSVG)
                    new Path(PathFromPathDataSVG.doPath(mainHandSecondsSVG)),
            };
            Path[] mMainHandCurlHeadPathScaled;





























            ElementsSVG() {
                //super();

                //Log.i(TAG, "((((( ElementsSVG, mainHandStraightHourSVG=" + mainHandStraightHourSVG);
                //Log.i(TAG, "((((( ElementsSVG, mMainHandStraightPath[0]=" + mMainHandStraightPath[0]);

                // cut hand decor from hand outline
                mMainHandStraightPath[MHP_HOUR].setFillType(Path.FillType.EVEN_ODD);
                mMainHandStraightPath[MHP_HOUR].addPath(mMainHandStraightOutlinePath[MHP_HOUR]);
                mMainHandStraightPath[MHP_HOUR].addPath(mMainHandStraightDecorPath[MHP_HOUR]);
                //
                mMainHandStraightPath[MHP_MINUTE].setFillType(Path.FillType.EVEN_ODD);
                mMainHandStraightPath[MHP_MINUTE].addPath(mMainHandStraightOutlinePath[MHP_MINUTE]);
                mMainHandStraightPath[MHP_MINUTE].addPath(mMainHandStraightDecorPath[MHP_MINUTE]);

                //
                mMainHandRhombusPath[MHP_HOUR].setFillType(Path.FillType.EVEN_ODD);
                mMainHandRhombusPath[MHP_HOUR].addPath(mMainHandRhombusOutlinePath[MHP_HOUR]);
                mMainHandRhombusPath[MHP_HOUR].addPath(mMainHandRhombusDecorPath[MHP_HOUR]);
                //
                mMainHandRhombusPath[MHP_MINUTE].setFillType(Path.FillType.EVEN_ODD);
                mMainHandRhombusPath[MHP_MINUTE].addPath(mMainHandRhombusOutlinePath[MHP_MINUTE]);
                mMainHandRhombusPath[MHP_MINUTE].addPath(mMainHandRhombusDecorPath[MHP_MINUTE]);

                //
                mMainHandCurlHeadPath[MHP_HOUR].setFillType(Path.FillType.EVEN_ODD);
                mMainHandCurlHeadPath[MHP_HOUR].addPath(mMainHandCurlHeadOutlinePath[MHP_HOUR]);
                mMainHandCurlHeadPath[MHP_HOUR].addPath(mMainHandCurlHeadDecorPath[MHP_HOUR]);
                //
                mMainHandCurlHeadPath[MHP_MINUTE].setFillType(Path.FillType.EVEN_ODD);
                mMainHandCurlHeadPath[MHP_MINUTE].addPath(mMainHandCurlHeadOutlinePath[MHP_MINUTE]);
                mMainHandCurlHeadPath[MHP_MINUTE].addPath(mMainHandCurlHeadDecorPath[MHP_MINUTE]);

                //
                mMainHandArrowPath[MHP_HOUR].setFillType(Path.FillType.EVEN_ODD);
                mMainHandArrowPath[MHP_HOUR].addPath(mMainHandArrowOutlinePath[MHP_HOUR]);
                mMainHandArrowPath[MHP_HOUR].addPath(mMainHandArrowDecorPath[MHP_HOUR]);
                //
                mMainHandArrowPath[MHP_MINUTE].setFillType(Path.FillType.EVEN_ODD);
                mMainHandArrowPath[MHP_MINUTE].addPath(mMainHandArrowOutlinePath[MHP_MINUTE]);
                mMainHandArrowPath[MHP_MINUTE].addPath(mMainHandArrowDecorPath[MHP_MINUTE]);


            }

        } // class ElementsSVG
        //
        ElementsSVG dialElements = new ElementsSVG();
        //
        public final static int MHP_HOUR = 0;
        public final static int MHP_MINUTE = 1;
        public final static int MHP_SECOND = 2;
        //
        public final static int DO_COMPOSITE = 0;
        public final static int DO_OUTLINE = 1;
        public final static int DO_DECOR = 2;
















        int mSensCount = 0;


        @Override
        public void onDestroy() {
            //Log.i(TAG, "Engine.onDestroy");
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            //AWearFaceService.this.unregisterReceiver(mDataFromListenerServiceReceiver);
            LocalBroadcastManager.getInstance(AWearFaceService.this).unregisterReceiver(mDataFromListenerServiceReceiver);
            Bundle config = bundleConfig(denseAppearance);
            if (null != config) {
                String cfn = getString(R.string.configFileName);
                ACommon.savePersistentDataToFile(AWearFaceService.this, cfn, config);
            }
            super.onDestroy();
        }

        @Override //
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            boolean lowBit, burnIn;
            lowBit = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            burnIn = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mAmbientProp.setValues(lowBit, burnIn);
            //Log.i(TAG, "((((( onPropertiesChanged: low-bit ambient=" + mAmbientLowBit + ", burn-in protection=" + mAmbientBurnInProtection);
//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "onPropertiesChanged: low-bit ambient=" + mAmbientLowBit + ", burn-in protection=" + mAmbientBurnInProtection);
//            }
            //Log.i(TAG, "((((( properties=" + properties);
//            if (null != mVars) {
//                Log.i(TAG, "((((( onPropertiesChanged AFTER FIRST onDraw !!!");
//            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            //Log.i(TAG, "#TIME onTimeTick: ambient=" + isInAmbientMode() + ", visible=" + mVisible);
//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
//            }
            if (!(!isInAmbientMode() && mVisible)) wTime.onTimeTick(TimeZone.getDefault());
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }


/*
        //drawCalendarDial(canvas, cx, cy, r, 0f, 12f); // 7f, 2f
        private void drawCalendarDial(Canvas canvas, float centerX, float centerY, float radiusOuter,
                                      float angleStartPointRad, float angleGapDeg) {

            //RectF dialRect = new RectF(centerX-radiusOuter, centerY-radiusOuter, centerX+radiusOuter, centerY+radiusOuter);
            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            //float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
            float angleSweepDeg = 360f - angleGapDeg;
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);

            float numArcs, numSubarcs;
            float divisorMain;
            //float angleTickRad;

            mCalendarDialPaint.setAntiAlias(true);
            mCalendarDialPaint.setFilterBitmap(true);
            mCalendarDialPaint.setDither(true);
            mCalendarDialPaint.setAlpha(255);
//            mCalendarDialPaint.setStrokeWidth(2f);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mCalendarDialPaint.setARGB(255, 30, 30, 30);
//            canvas.drawCircle(centerX, centerY, radiusOuter, mCalendarDialPaint);
//            //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mBattDialPaint);

//            mCalendarDialPaint.setARGB(200,255,255,255);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL);
//            //mCalendarDialPaint.setStrokeWidth(1f);

            Matrix hrMatrix = new Matrix();
            //Path pathInner = new Path(), pathOuter = new Path();
            Path pathOuterCW = new Path(), pathOuterCCW = new Path();
            float outerTickRadius, innerTickRadius;
            float lblTextSize;
            String[] descrLbl;
            int[] descrLen;




            // *** draw weekdays ticks
            numArcs = 7f; numSubarcs = 2f;
            float heightWkdCircle = 11f;
            divisorMain = numArcs * numSubarcs;
            float angleWkdayTickRad = angleSweepRad / (divisorMain);
            float angleWkdayFullTickRad = angleSweepRad / numArcs;
            outerTickRadius = radiusOuter;

            mCalendarDialPaint.setStrokeWidth(2f);
            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            //mCalendarDialPaint.setARGB(255, 30, 30, 30);
            //mCalendarDialPaint.setARGB(255, 100, 100, 95);
            mCalendarDialPaint.setARGB(255, 75, 75, 68);

            //int centerColor=0xfff0f0f0, edgeColor=0x801e1e1e;
            //Shader dialShader = new RadialGradient(centerX,centerY,outerTickRadius,centerColor,edgeColor,Shader.TileMode.CLAMP);
            //mCalendarDialPaint.setShader(dialShader);
            canvas.drawCircle(centerX, centerY, outerTickRadius, mCalendarDialPaint);
            //mCalendarDialPaint.setShader(null);

            hrMatrix.setRotate(-90f, centerX, centerY);
//            pathInner.addCircle(centerX, centerY, outerTickRadius-heightWkdCircle, Path.Direction.CW);
//            //hrMatrix.setRotate(-96.f, centerX, centerY);
//            pathInner.transform(hrMatrix);
//            pathOuter.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
//            //hrMatrix.reset();
//            //hrMatrix.setRotate(-84.f,centerX, centerY);
//            pathOuter.transform(hrMatrix);
            pathOuterCW.reset();
            pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
            pathOuterCW.transform(hrMatrix);
            pathOuterCCW.reset();
            pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
            pathOuterCCW.transform(hrMatrix);

            descrLbl = new String[]{"ПНД","ВТР","СРД","ЧТВ","ПТН","СБТ","ВСК"};
            descrLen = new int[]   {    3,    3,    3,    3,    3,    3,    3};

            mCalendarWkdayPaint.setTextScaleX(1.2f);

            for (int i=30; i > 10; i--) {
                lblTextSize = i;
                Rect textBounds = new Rect();
                mCalendarWkdayPaint.setTextSize(lblTextSize);
                mCalendarWkdayPaint.getTextBounds("0", 0, 1, textBounds);
                if (textBounds.height() == (int)heightWkdCircle) break;
            }

            mCalendarWkdayPaint.setARGB(255,230,230,210);
            mCalendarWkdayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCalendarWkdayPaint.setStrokeWidth(1f);

            mCalendarDialPaint.setARGB(255,30,30,30);
            mCalendarDialPaint.setStyle(Paint.Style.FILL);
            mCalendarDialPaint.setStrokeWidth(4f);

            for (int tickIndex = 0; tickIndex < divisorMain+1; tickIndex++) {
                if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - heightWkdCircle;
                else innerTickRadius = outerTickRadius - 3f;
                //float tickRot = (float) ((tickIndex / 12f) * Math.PI * 2f);
                float tickRotRad = angleStartRad + tickIndex * angleWkdayTickRad; // 0-tickIndex; //
                float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius-1.5f);
                float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius-1.5f);
                float outerX = (float) Math.sin(tickRotRad) * (outerTickRadius+1f);
                float outerY = (float) -Math.cos(tickRotRad) * (outerTickRadius+1f);
                if (tickIndex % numSubarcs == 0) {
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
                    continue;
                }

                int descrInd = (int) (tickIndex / numSubarcs);
                float descrWidth = 0f, descrHeight = 0f;
                Rect descrRect = new Rect();
//                for (int i = 0; i < descrLen[descrInd]; i++) {
//                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],i,i+1,descrRect);
//                    descrWidth += descrRect.width() + 0.5f;
//                }
                mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],0,descrLen[descrInd],descrRect);
                descrWidth = descrRect.width();
                mCalendarWkdayPaint.getTextBounds("0",0,1,descrRect);
                descrHeight = descrRect.height();

                float hOffset;
                if (descrInd>1 && descrInd<5) {
                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -2f, mCalendarWkdayPaint);
                } else {
                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarWkdayPaint);
                }
            }





            //draw months ticks
            numArcs = 12f; numSubarcs = 2f;
            float heightMnthCircle = 12f;
            divisorMain = numArcs * numSubarcs;
            float angleMonthTickRad = angleSweepRad / (divisorMain);
            float angleMonthFullTickRad = angleSweepRad / numArcs;
            outerTickRadius = radiusOuter - heightWkdCircle - 2f; // !!! heightWkdCircle !!!

            mCalendarDialPaint.setStrokeWidth(2f);
            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCalendarDialPaint.setARGB(255, 30, 30, 30);
            canvas.drawCircle(centerX, centerY, outerTickRadius - 1f, mCalendarDialPaint);

            hrMatrix.reset();
            hrMatrix.setRotate(-90f, centerX, centerY);
            pathOuterCW.reset();
            pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
            pathOuterCW.transform(hrMatrix);
            pathOuterCCW.reset();
            pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
            pathOuterCCW.transform(hrMatrix);

            //descrLbl = new String[]{"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
            //descrLen = new int[]   {  1,   2,    3,   2,  1,   2,    3,     4,   2,  1,   2,    3};
            descrLbl = new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
            descrLen = new int[]   {   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2};

            for (int i=30; i > 10; i--) {
                lblTextSize = i;
                Rect textBounds = new Rect();
                mCalendarMonthPaint.setTextSize(lblTextSize);
                mCalendarMonthPaint.getTextBounds("0", 0, 1, textBounds);
                if (textBounds.height() == (int)heightMnthCircle) break;
            }

            mCalendarMonthPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCalendarMonthPaint.setStrokeWidth(1f);
            mCalendarMonthPaint.setARGB(200, 255,255,185);

            //mCalendarDialPaint.setARGB(200,255,255,255);
            mCalendarDialPaint.setARGB(200, 255,255,185);
            mCalendarDialPaint.setStyle(Paint.Style.FILL);
            mCalendarDialPaint.setStrokeWidth(2f);

            //for (int tickIndex = 0; tickIndex < divisorMain+1; tickIndex++) {
            for (int tickIndex = 1; tickIndex < divisorMain; tickIndex++) {
                innerTickRadius = outerTickRadius - heightMnthCircle;
                //if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - 10;
                //else innerTickRadius = outerTickRadius - 2;
                float tickRotRad = angleStartRad + tickIndex * angleMonthTickRad;
                float innerX = (float) Math.sin(tickRotRad) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRotRad) * innerTickRadius;
                float outerX = (float) Math.sin(tickRotRad) * (innerTickRadius+2f); // (outerTickRadius-8)
                float outerY = (float) -Math.cos(tickRotRad) * (innerTickRadius+2f); // (outerTickRadius-8)
                if (tickIndex % numSubarcs == 0) {
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
                }

                if (tickIndex % numSubarcs == 0) continue;

                int descrInd = (int) (tickIndex / numSubarcs);
                float descrWidth = 0f, descrHeight = 0f;
                Rect descrRect = new Rect();
//                for (int i = 0; i < descrLen[descrInd]; i++) {
//                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],i,i+1,descrRect);
//                    descrWidth += descrRect.width() + 0.5f;
//                }
                mCalendarMonthPaint.getTextBounds(descrLbl[descrInd],0,descrLen[descrInd],descrRect);
                descrWidth = descrRect.width();
                mCalendarMonthPaint.getTextBounds("0",0,1,descrRect);
                descrHeight = descrRect.height();

                float hOffset;
                if (descrInd>2 && descrInd<9) {
                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -1f, mCalendarMonthPaint);
                } else {
                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarMonthPaint);
                }
            }


            // draw hands: month + weekday
            boolean isDialClockwise;
            float handRot, handRotDeg;
            Time cTime = new Time(mTime);
            cTime.normalize(true);
            mCalendarDialPaint.setAlpha(255);

            //draw month hand
            if (mMonthHandScaledBitmap == null
                    || mMonthHandScaledBitmap.getWidth() != 320
                    || mMonthHandScaledBitmap.getHeight() != 40) {
                mMonthHandScaledBitmap = Bitmap.createScaledBitmap(mMonthHandBitmap, 320, 40, true);
            }

            isDialClockwise = true;
            int month = cTime.month;
            int day = cTime.monthDay;
            int maxDaysInMonth = cTime.getActualMaximum(Time.MONTH_DAY);
            handRot = angleStartRad + angleSweepRad / 12f * (isDialClockwise?month:12f-month) //;
                    + angleMonthFullTickRad / maxDaysInMonth * (isDialClockwise?day:maxDaysInMonth-day);
            handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
            hrMatrix.reset();
            hrMatrix.setTranslate(-160f, -20f); // hand bitmap is 320x40
            hrMatrix.postRotate(handRotDeg);
            hrMatrix.postTranslate(centerX, centerY);
            canvas.drawBitmap( mMonthHandScaledBitmap, hrMatrix, mCalendarDialPaint);

            // draw weekday hand
            if (mWkdayHandScaledBitmap == null
                    || mWkdayHandScaledBitmap.getWidth() != 320
                    || mWkdayHandScaledBitmap.getHeight() != 40) {
                mWkdayHandScaledBitmap = Bitmap.createScaledBitmap(mWkdayHandBitmap, 320, 40, true);
            }

            isDialClockwise = true;
            int wday = mTime.weekDay; // 0-sunday,1-monday...
            if (--wday<0) wday = 6;
            int hour = mTime.hour;
            //float handRot = angleStartRad + angleSweepRad / 100f * (isDialClockwise?mWatchesBattery:100f-mWatchesBattery);
            handRot = angleStartRad + angleSweepRad / 7f * (isDialClockwise?wday:7f-wday) +
                    angleWkdayFullTickRad / 24f * (isDialClockwise?hour:24f-hour);
            handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
            //Matrix hrMatrix = new Matrix();
            hrMatrix.reset();
            hrMatrix.setTranslate(-160f, -20f); // hand bitmap is 320x40
            hrMatrix.postRotate(handRotDeg);
            hrMatrix.postTranslate(centerX, centerY);
            //mBattDialPaint.setARGB(255, 255,255,255);
            //canvas.drawBitmap(mWBHandScaledBitmap, hrMatrix, mBattDialPaint);
            //mCalendarMonthPaint.setARGB(255, 255, 255, 255);
            canvas.drawBitmap( mWkdayHandScaledBitmap, hrMatrix, mCalendarDialPaint);

        } //drawCalendarDial

*/


/*

        //drawCalendarDial(canvas, cx, cy, r, 0f, 12f); // 7f, 2f
        private void drawCalendarDial2(Canvas canvas, float centerX, float centerY, float radiusOuter,
                                       float angleStartPointRad, float angleGapDeg, boolean drawMonth) {

            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
            float angleSweepDeg = 360f - angleGapDeg;
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);

            float angleWkdGapDeg = 28f;
            float angleWkdStartRad = (float) ((angleWkdGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleWkdStartDeg = (float) (angleWkdStartRad * 180f / Math.PI);
            float angleWkdSweepDeg = 360f - angleWkdGapDeg;
            float angleWkdSweepRad = (float) (angleWkdSweepDeg * Math.PI / 180f);

            float numArcs, numSubarcs;
            float divisorMain;
            //float angleTickRad;

            mCalendarDialPaint.setAntiAlias(true);
            mCalendarDialPaint.setFilterBitmap(true);
            mCalendarDialPaint.setDither(true);
            mCalendarDialPaint.setAlpha(255);
//            mCalendarDialPaint.setStrokeWidth(2f);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mCalendarDialPaint.setARGB(255, 30, 30, 30);
//            canvas.drawCircle(centerX, centerY, radiusOuter, mCalendarDialPaint);
//            //RectF dialRect = new RectF(centerX-radiusOuter, centerY-radiusOuter, centerX+radiusOuter, centerY+radiusOuter);
//            //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mBattDialPaint);

//            mCalendarDialPaint.setARGB(200,255,255,255);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL);
//            //mCalendarDialPaint.setStrokeWidth(1f);

            Matrix hrMatrix = new Matrix();
            //Path pathInner = new Path(), pathOuter = new Path();
            Path pathOuterCW = new Path(), pathOuterCCW = new Path(), pathWkdOuter = new Path();
            float outerTickRadius, innerTickRadius;
            float lblTextSize = 10f;
            String[] descrLbl;
            int[] descrLen;
            RectF dialRect;




            // *** draw weekdays ticks
            numArcs = 7f; numSubarcs = 2f;
            float heightWkdCircle = 11f;
            divisorMain = numArcs * numSubarcs;
            float angleWkdayTickRad = angleWkdSweepRad / (divisorMain);
            float angleWkdayFullTickRad = angleWkdSweepRad / numArcs;
            outerTickRadius = radiusOuter;

            mCalendarDialPaint.setStrokeWidth(2f);
            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            //mCalendarDialPaint.setARGB(255, 30, 30, 30);
            mCalendarDialPaint.setARGB(255, 0, 0, 0);
            //mCalendarDialPaint.setARGB(255, 100, 100, 95);
            //mCalendarDialPaint.setARGB(255, 75, 75, 68);
            canvas.drawCircle(centerX, centerY, outerTickRadius, mCalendarDialPaint);

//            //int centerColor=0xfff0f0f0, edgeColor=0x801e1e1e;
//            //Shader dialShader = new RadialGradient(centerX,centerY,outerTickRadius,centerColor,edgeColor,Shader.TileMode.CLAMP);
//            //mCalendarDialPaint.setShader(dialShader);
//            canvas.drawCircle(centerX, centerY, outerTickRadius, mCalendarDialPaint);
//            //mCalendarDialPaint.setShader(null);

            hrMatrix.setRotate(-90f, centerX, centerY);
            pathOuterCW.reset();
            pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
            pathOuterCW.transform(hrMatrix);
            pathOuterCCW.reset();
            pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
            pathOuterCCW.transform(hrMatrix);

            descrLbl = new String[]{"ПНД","ВТР","СРД","ЧТВ","ПТН","СБТ","ВСК"};
            descrLen = new int[]   {    3,    3,    3,    3,    3,    3,    3};

            mCalendarWkdayPaint.setTextScaleX(1.2f);

            for (int i=30; i > 10; i--) {
                lblTextSize = i;
                Rect textBounds = new Rect();
                mCalendarWkdayPaint.setTextSize(lblTextSize);
                mCalendarWkdayPaint.getTextBounds("0", 0, 1, textBounds);
                if (textBounds.height() == (int)heightWkdCircle) break;
            }

            //mCalendarWkdayPaint.setARGB(255,230,230,210);
            mCalendarWkdayPaint.setColor(0xffffffff);
            mCalendarWkdayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCalendarWkdayPaint.setStrokeWidth(1f);

            //mCalendarDialPaint.setARGB(255,30,30,30);
            mCalendarDialPaint.setColor(0xffffffff);
            mCalendarDialPaint.setStyle(Paint.Style.FILL);
            mCalendarDialPaint.setStrokeWidth(2f);

            dialRect = new RectF(centerX-outerTickRadius, centerY-outerTickRadius,
                    centerX+outerTickRadius, centerY+outerTickRadius);
            mCalendarDialPaint.setStyle(Paint.Style.STROKE);
            mCalendarDialPaint.setAlpha(170);
            //canvas.drawArc(dialRect, angleWkdStartDeg-90f, angleWkdSweepDeg, false, mCalendarDialPaint);
            pathWkdOuter.reset();
            pathWkdOuter.addArc(dialRect, angleWkdStartDeg - 90f, angleWkdSweepDeg);
            float[] intervals = new float[]{2f,2f}; // array of ON and OFF distances
            float phase = 0;
            DashPathEffect dashPathEffect = new DashPathEffect(intervals,phase);
            mCalendarDialPaint.setPathEffect(dashPathEffect);
            canvas.drawPath(pathWkdOuter,mCalendarDialPaint);
            mCalendarDialPaint.setPathEffect(null);

            for (int tickIndex = 0; tickIndex < divisorMain+1; tickIndex++) {
                if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - heightWkdCircle;
                else innerTickRadius = outerTickRadius - 3f;
                //float tickRot = (float) ((tickIndex / 12f) * Math.PI * 2f);
                float tickRotRad = angleWkdStartRad + tickIndex * angleWkdayTickRad; // 0-tickIndex; //
                float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius-1.5f);
                float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius-1.5f);
                float outerX = (float) Math.sin(tickRotRad) * (outerTickRadius);
                float outerY = (float) -Math.cos(tickRotRad) * (outerTickRadius);
                if (tickIndex % numSubarcs == 0) {
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
                    continue;
                }

                int descrInd = (int) (tickIndex / numSubarcs);
                float descrWidth = 0f, descrHeight = 0f;
                Rect descrRect = new Rect();
//                for (int i = 0; i < descrLen[descrInd]; i++) {
//                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],i,i+1,descrRect);
//                    descrWidth += descrRect.width() + 0.5f;
//                }
                mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],0,descrLen[descrInd],descrRect);
                descrWidth = descrRect.width();
                mCalendarWkdayPaint.getTextBounds("0",0,1,descrRect);
                descrHeight = descrRect.height();

                mCalendarWkdayPaint.setAlpha(170);

                float hOffset;
                if (descrInd>1 && descrInd<5) {
                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -2f, mCalendarWkdayPaint);
                } else {
                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarWkdayPaint);
                }
            }





            //draw months ticks


            drawRimShadowsAndLights(centerX,centerY,radiusOuter,2f,canvas,mCalendarDialPaint);

            //if (drawMonth) drawMonthDial(canvas,centerX,centerY,radiusOuter - heightWkdCircle - 2f,angleStartPointRad,angleGapDeg);

            // draw hands: month + weekday
            boolean isDialClockwise;
            float handRot, handRotDeg;
            Time cTime = new Time(mTime);
            cTime.normalize(true);
            mCalendarDialPaint.setAlpha(255);

            // draw weekday hand
            if (mWkdayHandScaledBitmap == null
                    || mWkdayHandScaledBitmap.getWidth() != 320
                    || mWkdayHandScaledBitmap.getHeight() != 40) {
                mWkdayHandScaledBitmap = Bitmap.createScaledBitmap(mWkdayHandBitmap, 320, 40, true);
            }

            isDialClockwise = true;
            int wday = mTime.weekDay; // 0-sunday,1-monday...
            if (--wday<0) wday = 6;
            int hour = mTime.hour;
            //float handRot = angleStartRad + angleSweepRad / 100f * (isDialClockwise?mWatchesBattery:100f-mWatchesBattery);
            handRot = angleWkdStartRad + angleWkdSweepRad / 7f * (isDialClockwise?wday:7f-wday) +
                    angleWkdayFullTickRad / 24f * (isDialClockwise?hour:24f-hour);
            handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
            //Matrix hrMatrix = new Matrix();
            hrMatrix.reset();
            hrMatrix.setTranslate(-160f, -20f); // hand bitmap is 320x40
            hrMatrix.postRotate(handRotDeg);
            hrMatrix.postTranslate(centerX, centerY);
            //mBattDialPaint.setARGB(255, 255,255,255);
            //canvas.drawBitmap(mWBHandScaledBitmap, hrMatrix, mBattDialPaint);
            //mCalendarMonthPaint.setARGB(255, 255, 255, 255);
            canvas.drawBitmap( mWkdayHandScaledBitmap, hrMatrix, mCalendarDialPaint);

        } //drawCalendarDial2

*/

//        private void drawWeekdayDial(Canvas canvas, float centerX, float centerY, float radiusOuter,
//                                       float angleStartPointRad, float angleGapDeg) {
        private void drawWeekdayDial(Canvas canvas, WatchAuxPosition aux,
                                     float angleStartPointRad, float angleGapDeg,
                                     boolean drawCircle, boolean drawHand, boolean ambient) {

            float centerX = aux.cX;
            float centerY = aux.cY;
            float radiusOuter = aux.dimension;

            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
            float angleSweepDeg = 360f - angleGapDeg;
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);

            float angleWkdGapDeg = angleGapDeg; //28f;
            float angleWkdStartRad = (float) ((angleWkdGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleWkdStartDeg = (float) (angleWkdStartRad * 180f / Math.PI);
            float angleWkdSweepDeg = 360f - angleWkdGapDeg;
            float angleWkdSweepRad = (float) (angleWkdSweepDeg * Math.PI / 180f);

            float numArcs, numSubarcs;
            float divisorMain;
            //float angleTickRad;

            mCalendarDialPaint.setAntiAlias(true);
            mCalendarDialPaint.setFilterBitmap(true);
            mCalendarDialPaint.setDither(true);
            mCalendarDialPaint.setAlpha(255);
            /*
            mCalendarDialPaint.setStrokeWidth(2f);
            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCalendarDialPaint.setARGB(255, 30, 30, 30);
            canvas.drawCircle(centerX, centerY, radiusOuter, mCalendarDialPaint);
            //RectF dialRect = new RectF(centerX-radiusOuter, centerY-radiusOuter, centerX+radiusOuter, centerY+radiusOuter);
            //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mBattDialPaint);
            */

            /*
            mCalendarDialPaint.setARGB(200,255,255,255);
            mCalendarDialPaint.setStyle(Paint.Style.FILL);
            //mCalendarDialPaint.setStrokeWidth(1f);
            */

            Matrix hrMatrix = new Matrix();
            //Path pathInner = new Path(), pathOuter = new Path();
            Path pathOuterCW = new Path(), pathOuterCCW = new Path(), pathWkdOuter = new Path();
            float outerTickRadius, innerTickRadius;
            float lblTextSize = mVars.pixelDim(10f);
            String[] descrLbl;
            int[] descrLen;
            RectF dialRect;


            numArcs = 7f; numSubarcs = 4f;
            float angleWkdayFullTickRad = angleWkdSweepRad / numArcs;



            if (drawCircle) { // draw dial circle elements
                float heightWkdCircle = mVars.pixelDim(7f);
                divisorMain = numArcs * numSubarcs;
                float angleWkdayTickRad = angleWkdSweepRad / (divisorMain /*- 1*/);
                outerTickRadius = radiusOuter - mVars.pixelDim(4f);

                mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2f));
                mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                //
////            if ( currentAppearance.mShowDialGradient == false || currentAppearance.mMainCalendarDialBackgroundColor != currentAppearance.mMainBackgroundColor) {
////                mCalendarDialPaint.setColor(currentAppearance.mMainCalendarDialBackgroundColor);
////                canvas.drawCircle(centerX, centerY, radiusOuter, mCalendarDialPaint);
////            } else {
//                //Shader shd = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                Shader shd = new BitmapShader(mBigAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                // mBigAuxDialGradientNoTransparent currentAppearance.mMainCalendarDialBackgroundColor
//                mCalendarDialPaint.setShader(shd);
//                canvas.drawCircle(centerX, centerY, radiusOuter - 1f, mCalendarDialPaint);
//                mCalendarDialPaint.setShader(null);
////            }
                if (ambient) {
                    mCalendarDialPaint.setColor(Color.BLACK);
                } else {
                    Shader shd = new BitmapShader(mBigAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    mCalendarDialPaint.setShader(shd);
                }
                canvas.drawCircle(centerX, centerY, radiusOuter - mVars.pixelDim(1f), mCalendarDialPaint);
                mCalendarDialPaint.setShader(null);

                if (ambient) {
                    mCalendarWkdayPaint.setColor(denseAppearance.mAmbientTicksColor);
                } else {
                    mCalendarWkdayPaint.setColor(denseAppearance.mMainCalendarDialDigitsColor);
                }
                mCalendarWkdayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mCalendarWkdayPaint.setStrokeWidth(mVars.pixelDim(1f));

               if (ambient) {
                    mCalendarDialPaint.setColor(denseAppearance.mAmbientTicksColor);
                } else {
                    mCalendarDialPaint.setColor(denseAppearance.mMainCalendarDialTicksColor);
                }
                mCalendarDialPaint.setStyle(Paint.Style.FILL);
                mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2f));


                //draw arc
//            mVars.mPathInner.reset();
//            mVars.hrMatrix.reset();
//            float left, top, right, bottom, diff = 16f;
//            left = aux.cX - aux.dimension + diff;
//            top = aux.cY - aux.dimension + diff;
//            right = aux.cX + aux.dimension - diff;
//            bottom = aux.cY + aux.dimension - diff;
//            mVars.mPathInner.addArc(left, top, right, bottom, angleWkdStartDeg, angleWkdSweepDeg);
//            mVars.hrMatrix.setRotate(-90f, aux.cX, aux.cY);
//            mVars.mPathInner.transform(mVars.hrMatrix);
//            mCalendarDialPaint.setStrokeWidth(1f);
//            mCalendarDialPaint.setStyle(Paint.Style.STROKE);
//            mCalendarDialPaint.setAlpha(178);
//            //canvas.drawPath(mVars.mPathInner, mCalendarDialPaint);
//            canvas.drawCircle(aux.cX, aux.cY, aux.dimension - diff, mCalendarDialPaint);
//            mCalendarDialPaint.setAlpha(255);

            /*
            //int centerColor=0xfff0f0f0, edgeColor=0x801e1e1e;
            //Shader dialShader = new RadialGradient(centerX,centerY,outerTickRadius,centerColor,edgeColor,Shader.TileMode.CLAMP);
            //mCalendarDialPaint.setShader(dialShader);
            canvas.drawCircle(centerX, centerY, outerTickRadius, mCalendarDialPaint);
            //mCalendarDialPaint.setShader(null);
            */

                hrMatrix.reset();
                hrMatrix.setRotate(-90f, centerX, centerY);
                pathOuterCW.reset();
                pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
                pathOuterCW.transform(hrMatrix);
                pathOuterCCW.reset();
                pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
                pathOuterCCW.transform(hrMatrix);

                //descrLbl = new String[]{"ПНД","ВТР","СРД","ЧТВ","ПТН","СБТ","ВСК"};
                //descrLen = new int[]   {    3,    3,    3,    3,    3,    3,    3};
                descrLbl = new String[]{"1","2","3","4","5","6","7"};
                descrLen = new int[]   {    1,    1,    1,    1,    1,    1,    1};

                //mCalendarWkdayPaint.setTextScaleX(1.2f);

                for (int i=30; i > 10; i--) {
                    lblTextSize = i;
                    Rect textBounds = new Rect();
                    mCalendarWkdayPaint.setTextSize(lblTextSize);
                    mCalendarWkdayPaint.getTextBounds("0", 0, 1, textBounds);
                    if (textBounds.height() == (int)heightWkdCircle) break;
                }
                mCalendarWkdayPaint.setTextSize(mVars.pixelDim(lblTextSize+/*3*/5f));

            /*
            dialRect = new RectF(centerX-outerTickRadius, centerY-outerTickRadius,
                    centerX+outerTickRadius, centerY+outerTickRadius);
            mCalendarDialPaint.setStyle(Paint.Style.STROKE);
            mCalendarDialPaint.setAlpha(170);
            //canvas.drawArc(dialRect, angleWkdStartDeg-90f, angleWkdSweepDeg, false, mCalendarDialPaint);
            pathWkdOuter.reset();
            pathWkdOuter.addArc(dialRect, angleWkdStartDeg - 90f, angleWkdSweepDeg);
            float[] intervals = new float[]{2f,2f}; // array of ON and OFF distances
            float phase = 0;
            DashPathEffect dashPathEffect = new DashPathEffect(intervals,phase);
            mCalendarDialPaint.setPathEffect(dashPathEffect);
            canvas.drawPath(pathWkdOuter,mCalendarDialPaint);
            mCalendarDialPaint.setPathEffect(null);
            */

                for (int tickIndex = 0; tickIndex < divisorMain+1; tickIndex++) {
                    if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - heightWkdCircle - mVars.pixelDim(5f);
                    else innerTickRadius = outerTickRadius - heightWkdCircle;
                    //innerTickRadius = outerTickRadius - heightWkdCircle;
                    //float tickRot = (float) ((tickIndex / 12f) * Math.PI * 2f);
                    float tickRotRad = angleWkdStartRad + tickIndex * angleWkdayTickRad; // 0-tickIndex; //
                    float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius);
                    float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius);
                    float outerX = (float) Math.sin(tickRotRad) * (outerTickRadius);
                    float outerY = (float) -Math.cos(tickRotRad) * (outerTickRadius);
                    if (tickIndex % numSubarcs == 0) {
                        mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2.1f));
                    } else {
                        mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(1.2f));
                    }

                    //if (tickIndex % numSubarcs == 0 || tickIndex % 2 != 0) {
                    mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
                    //}

                    //boolean needDrawTick = false;
                    if (tickIndex % numSubarcs == 0) continue;
                    if (tickIndex % 2 != 0) continue;


                    int descrInd = (int) (tickIndex / numSubarcs);
                    float descrWidth = 0f, descrHeight = 0f;
                    Rect descrRect = new Rect();
                /*for (int i = 0; i < descrLen[descrInd]; i++) {
                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],i,i+1,descrRect);
                    descrWidth += descrRect.width() + 0.5f;
                }*/
                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd], 0, descrLen[descrInd], descrRect);
                    descrWidth = descrRect.width();
                    mCalendarWkdayPaint.getTextBounds("0", 0, 1, descrRect);
                    descrHeight = descrRect.height();

                /*
                float hOffset;
                if (descrInd>2 && descrInd<9) {
                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -1f, mCalendarMonthPaint);
                } else {
                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarMonthPaint);
                }
                */

                    float cpX, cpY;
                    //cpX = centerX + (float) Math.sin(tickRotRad) * (innerTickRadius + ((outerTickRadius - innerTickRadius) / 2));
                    //cpY = centerY + (float) -Math.cos(tickRotRad) * (innerTickRadius + ((outerTickRadius - innerTickRadius) / 2));
                    //
//                cpX = centerX + (float) Math.sin(tickRotRad) * (innerTickRadius - (outerTickRadius - innerTickRadius));
//                cpY = centerY + (float) -Math.cos(tickRotRad) * (innerTickRadius - (outerTickRadius - innerTickRadius));
                    cpX = centerX + (float) Math.sin(tickRotRad) * (innerTickRadius * 0.8f);
                    cpY = centerY + (float) -Math.cos(tickRotRad) * (innerTickRadius * 0.8f);
                    cpX -= descrWidth / 2f;
                    cpY += descrHeight / 2f;
                    //cpX = (float) Math.sin(tickRotRad) * (innerTickRadius);
                    //cpY = (float) -Math.cos(tickRotRad) * (innerTickRadius);
                    mCalendarWkdayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawText(descrLbl[descrInd], cpX, cpY, mCalendarWkdayPaint);



                /*
                int descrInd = (int) (tickIndex / numSubarcs);
                float descrWidth = 0f, descrHeight = 0f;
                Rect descrRect = new Rect();
                mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],0,descrLen[descrInd],descrRect);
                descrWidth = descrRect.width();
                mCalendarWkdayPaint.getTextBounds("0",0,1,descrRect);
                descrHeight = descrRect.height();
                mCalendarWkdayPaint.setAlpha(170);
                float hOffset;
                if (descrInd>1 && descrInd<5) {
                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -2f, mCalendarWkdayPaint);
                } else {
                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarWkdayPaint);
                }
                */
                }





                //draw months ticks


                // todo: draw blurred shadow from rim bevel


                drawRimShadowsAndLightsNewNew(centerX, centerY, radiusOuter, 3f, canvas, mCalendarDialPaint, RIM_BIG_AUX, ambient);
                drawRimShadowsAndLightsNew(centerX, centerY, radiusOuter, 3f, canvas, mCalendarDialPaint, RIM_BIG_AUX, ambient);
                //drawRimShadowsAndLightsOrg(centerX, centerY, radiusOuter, 2f, canvas, mCalendarDialPaint);

                //if (drawMonth) drawMonthDial(canvas,centerX,centerY,radiusOuter - heightWkdCircle - 2f,angleStartPointRad,angleGapDeg);


/*
            //draw wings logo
            if (mWingsScaledBitmap == null
                    || mWingsScaledBitmap.getWidth() != 320
                    || mWingsScaledBitmap.getHeight() != 40) {
                mWingsScaledBitmap = Bitmap.createScaledBitmap(mWingsBitmap, 320, 40, true);
            }
            hrMatrix.reset();
            hrMatrix.setTranslate(-160f, -20f); // hand bitmap is 320x40
            //hrMatrix.postRotate(handRotDeg);
            hrMatrix.postTranslate(centerX, centerY);
            mCalendarDialPaint.setAlpha(110);
            mCalendarDialPaint.setAntiAlias(true);
            mCalendarDialPaint.setFilterBitmap(true);
            mCalendarDialPaint.setDither(true);
            canvas.drawBitmap(mWingsScaledBitmap, hrMatrix, mCalendarDialPaint);
*/


                // draw upper decor
                if (!ambient) {
                    canvas.drawBitmap(mDecorUpperShadow, 0, 0, null);
                    canvas.drawBitmap(mDecorUpperDecor, 0, 0, null);
                    drawPathBevelLights(canvas, 0, dialElements.mDecorUpperPathScaled[DO_COMPOSITE], null, 0x7fffffff, 0x7f000000, 0.0f);
                    canvas.drawBitmap(mDecorUpperColorized, 0, 0, null);
                } else {
                    canvas.drawBitmap(mDecorUpperDecorAmbient, 0, 0, null);
                    canvas.drawBitmap(mDecorUpperBlack, 0, 0, null);
                }

            } // if(drawCircle) - draw dial circle elements





            if (drawHand) { // draw weekday hand
                boolean isDialClockwise;
                float handRot, handRotDeg;
//                Time cTime = new Time(mTime);
//                cTime.normalize(true);
                mCalendarDialPaint.setAlpha(255);

                isDialClockwise = true;

                int wday = wTime.getWeekday();//mTime.weekDay;   // mTime.weekDay:   0=вс 1=пн 2=вт 3=ср 4=чт 5=пт 6=сб
                //mFirstDayOfWeek           // mFirstDayOfWeek: 1=вс 2=пн 3=вт 4=ср 5=чт 6=пт 7=сб
                //Log.i(TAG, "((( wTime.weekDay="+wTime.getWeekday() + ", mTime.weekDay="+mTime.weekDay);

                int tickIndex = wday + 1 - mFirstDayOfWeek;
                if (tickIndex < 0) tickIndex = 7 + tickIndex;
                //if (--wday<0) wday = 6;

                //int hour = wTime.getHour(); //mTime.hour;
                float hour = wTime.getHour() + (((float) wTime.getMinute()) / 60f); //mTime.hour;
                //Log.i(TAG, "((( wTime.hour="+wTime.getHour() + ", mTime.hour="+mTime.hour);

                //Log.i(TAG, "((( WDAY=" + wday + ", hour="+hour + ", FDOW="+mFirstDayOfWeek + ", tickIndex="+tickIndex);

                //float handRot = angleStartRad + angleSweepRad / 100f * (isDialClockwise?mWatchesBattery:100f-mWatchesBattery);
                handRot = angleWkdStartRad + angleWkdSweepRad / 7f * (isDialClockwise?tickIndex:7f-tickIndex) +
                        angleWkdayFullTickRad / 24f * (isDialClockwise?hour:24f-hour);
                handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
                //Matrix hrMatrix = new Matrix();
                hrMatrix.reset();
                hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
                hrMatrix.postRotate(handRotDeg);
                hrMatrix.postTranslate(centerX, centerY);
                //mBattDialPaint.setARGB(255, 255,255,255);
                //canvas.drawBitmap(mWBHandScaledBitmap, hrMatrix, mBattDialPaint);
                //mCalendarMonthPaint.setARGB(255, 255, 255, 255);
//            canvas.drawBitmap( mWkdayHandScaledBitmap, hrMatrix, mCalendarDialPaint);
                //if (watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandWeekdayColorized == null) createAuxHandsSM();
                //
                if (!ambient) {
                    mVars.offsetMatrix.set(hrMatrix);
                    mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(3f));
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWeekdayShadow, mVars.offsetMatrix, mCalendarDialPaint);
                    //
                    drawPathBevelLights(canvas, 0, dialElements.mAuxHandWeekdayPathScaled[0], hrMatrix, 0x7fffffff, 0x7f000000, 0f);
                    //
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWeekdayColorized, hrMatrix, mCalendarDialPaint);
                } else {
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWeekdayAmbient, hrMatrix, mCalendarDialPaint);
                }

                drawHandMountingHole(canvas, centerX, centerY, ambient);
            } // if(drawHand) - draw weekday hand

        } //drawWeekdayDial


        private void drawScript(Canvas canvas, float centerX, float centerY, float width, float height) {
            String upperScrpt = "БОРТОВЫЕ";
            String lowerScrpt = "АЧХВ";
            Rect textBounds = new Rect();
            float lettersWidth, lettersHeight, spacesWidth, meanSpaceWidth;
            float posX, posY;

            mScriptPaint.setAntiAlias(true);

            /*
            mScriptPaint.setARGB(150,255,255,240);
            mScriptPaint.setStyle(Paint.Style.STROKE);
            mScriptPaint.setStrokeWidth(0.9f);
            canvas.drawRect(centerX-width/2,centerY-height/2,centerX+width/2,centerY+height/2,mScriptPaint);
            */

            mScriptPaint.setTextSize(10f);
            mScriptPaint.setTextScaleX(1.4f);
            lettersWidth = 0;
            mScriptPaint.getTextBounds(upperScrpt,0,1,textBounds);
            lettersHeight = textBounds.height();
            for (int i=0; i<upperScrpt.length(); i++) {
                mScriptPaint.getTextBounds(upperScrpt,i,i+1,textBounds);
                lettersWidth += textBounds.width();
            }
            spacesWidth = width - lettersWidth;
            meanSpaceWidth = spacesWidth / (upperScrpt.length() - 1);
            //posX = centerX - (lettersWidth + spacesWidth) / 2f;
            posX = centerX - width / 2f;
            posY = centerY - height / 2f + lettersHeight;
            mScriptPaint.setStyle(Paint.Style.FILL); //_AND_STROKE
            mScriptPaint.setStrokeWidth(0.9f);
            mScriptPaint.setARGB(190,255,255,255);
            for (int i=0; i<upperScrpt.length(); i++) {
                float tw;
                canvas.drawText(upperScrpt,i,i+1,posX,posY,mScriptPaint);
                mScriptPaint.getTextBounds(upperScrpt, i, i + 1, textBounds);
                tw = textBounds.width();
                posX += tw + meanSpaceWidth;
                if (i==3) posX -= 2f;
            }

            mScriptPaint.setTextSize(17f);
            mScriptPaint.setTextScaleX(1.2f);
            lettersWidth = 0;
            mScriptPaint.getTextBounds(lowerScrpt, 0, 1, textBounds);
            lettersHeight = textBounds.height();
            posX = centerX - width / 2f;
            posY = centerY + height / 2f;
            mScriptPaint.setStyle(Paint.Style.FILL); //_AND_STROKE
            mScriptPaint.setStrokeWidth(0.8f);
            mScriptPaint.setARGB(170,255,255,255);
            for (int i=0; i<lowerScrpt.length(); i++) {
                float tw;
                mScriptPaint.getTextBounds(lowerScrpt,i,i+1,textBounds);
                tw = textBounds.width();
                if (i==3) {
                    posX = centerX + width / 2f - tw - 2f;
                    mScriptPaint.setARGB(170,255,190,190);
                }
                canvas.drawText(lowerScrpt,i,i+1,posX,posY,mScriptPaint);
                posX += tw + ((i == 0) ? -1f : 4f);
            }




        } //drawScript


////        private void drawMonthDial(Canvas canvas, float centerX, float centerY, float radiusOuter,
////                                   float angleStartPointRad, float angleGapDeg) {
//        private void drawMonthDial1(Canvas canvas, WatchAuxPosition aux,
//                           float angleStartPointRad, float angleGapDeg) {
//
//            float centerX = aux.cX;
//            float centerY = aux.cY;
//            float radiusOuter = aux.dimension;
//
//            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
//            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
//            float angleSweepDeg = 360f - angleGapDeg;
//            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);
//
//            float angleWkdGapDeg = 28f;
//            float angleWkdStartRad = (float) ((angleWkdGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
//            float angleWkdStartDeg = (float) (angleWkdStartRad * 180f / Math.PI);
//            float angleWkdSweepDeg = 360f - angleWkdGapDeg;
//            float angleWkdSweepRad = (float) (angleWkdSweepDeg * Math.PI / 180f);
//
//            float numArcs, numSubarcs;
//            float divisorMain;
//
//            Matrix hrMatrix = new Matrix();
//            //Path pathInner = new Path(), pathOuter = new Path();
//            Path pathOuterCW = new Path(), pathOuterCCW = new Path(), pathWkdOuter = new Path();
//            float outerTickRadius, innerTickRadius;
//            float lblTextSize = 10f;
//            String[] descrLbl;
//            int[] descrLen;
//            RectF dialRect;
//
//            numArcs = 12f; numSubarcs = 2f;
//            float heightMnthCircle = 12f;
//            divisorMain = numArcs * numSubarcs;
//            float angleMonthTickRad = angleSweepRad / (divisorMain /*- 1*/);
//            float angleMonthFullTickRad = angleSweepRad / numArcs;
//            //outerTickRadius = radiusOuter - heightWkdCircle - 2f; // !!! heightWkdCircle !!!
//            outerTickRadius = radiusOuter;
//
//            mCalendarDialPaint.setStrokeWidth(2f);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            //mCalendarDialPaint.setARGB(255, 30, 30, 30);
//            //mCalendarDialPaint.setARGB(255, 0, 0, 0);
//            mCalendarDialPaint.setColor(currentAppearance.mMainSmallAuxDialBackgroundColor);
//            //mCalendarDialPaint.setColor(0xff753837);
//            canvas.drawCircle(centerX, centerY, outerTickRadius - 1f, mCalendarDialPaint);
//
//            hrMatrix.reset();
//            hrMatrix.setRotate(-90f, centerX, centerY);
//            pathOuterCW.reset();
//            pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
//            pathOuterCW.transform(hrMatrix);
//            pathOuterCCW.reset();
//            pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
//            pathOuterCCW.transform(hrMatrix);
//
//            //descrLbl = new String[]{"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
//            //descrLen = new int[]   {  1,   2,    3,   2,  1,   2,    3,     4,   2,  1,   2,    3};
//            descrLbl = new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
//            descrLen = new int[]   {   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2};
//
//            for (int i=30; i > 10; i--) {
//                lblTextSize = i;
//                Rect textBounds = new Rect();
//                mCalendarMonthPaint.setTextSize(lblTextSize);
//                mCalendarMonthPaint.getTextBounds("0", 0, 1, textBounds);
//                if (textBounds.height() == (int)heightMnthCircle) break;
//            }
//            lblTextSize -= 2f;
//            mCalendarMonthPaint.setTextSize(lblTextSize);
//
//            mCalendarMonthPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            mCalendarMonthPaint.setStrokeWidth(1f);
//            //mCalendarMonthPaint.setARGB(200, 255,255,185);
//            mCalendarMonthPaint.setColor(currentAppearance.mMainSmallAuxDialDigitsColor);
//            //mCalendarMonthPaint.setARGB(180,255,255,255);
//
//            //mCalendarDialPaint.setARGB(180,255,255,255);
//            //mCalendarDialPaint.setARGB(200, 255,255,185);
//            mCalendarDialPaint.setColor(currentAppearance.mMainSmallAuxDialTick1Color);
//            mCalendarDialPaint.setStyle(Paint.Style.FILL);
//            mCalendarDialPaint.setStrokeWidth(2f);
//
//            innerTickRadius = outerTickRadius - heightMnthCircle - 5;
//            dialRect = new RectF(centerX-innerTickRadius+2, centerY-innerTickRadius+2,
//                    centerX+innerTickRadius-2, centerY+innerTickRadius-2);
//            mCalendarDialPaint.setStyle(Paint.Style.STROKE);
//            canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mCalendarDialPaint);
//
//            for (int tickIndex = 0; tickIndex < divisorMain+1; tickIndex++) {
//                //for (int tickIndex = 1; tickIndex < divisorMain; tickIndex++) {
//                //if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - 10;
//                //else innerTickRadius = outerTickRadius - 2;
//                float tickRotRad = angleStartRad + tickIndex * angleMonthTickRad;
//                float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius-2);
//                float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius-2);
//                float outerX = (float) Math.sin(tickRotRad) * /*(outerTickRadius-8)*/ (innerTickRadius+2f);
//                float outerY = (float) -Math.cos(tickRotRad) * /*(outerTickRadius-8)*/ (innerTickRadius+2f);
//                if (tickIndex % numSubarcs == 0) {
//                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
//                }
//
//                if (tickIndex % numSubarcs == 0) continue;
//
//                int descrInd = (int) (tickIndex / numSubarcs);
//                float descrWidth = 0f, descrHeight = 0f;
//                Rect descrRect = new Rect();
//                /*for (int i = 0; i < descrLen[descrInd]; i++) {
//                    mCalendarWkdayPaint.getTextBounds(descrLbl[descrInd],i,i+1,descrRect);
//                    descrWidth += descrRect.width() + 0.5f;
//                }*/
//                mCalendarMonthPaint.getTextBounds(descrLbl[descrInd],0,descrLen[descrInd],descrRect);
//                descrWidth = descrRect.width();
//                mCalendarMonthPaint.getTextBounds("0",0,1,descrRect);
//                descrHeight = descrRect.height();
//
//                /*
//                float hOffset;
//                if (descrInd>2 && descrInd<9) {
//                    hOffset = (float) ((Math.PI * 2f - tickRotRad) * (outerTickRadius) - descrWidth / 2f);
//                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCCW, hOffset, -1f, mCalendarMonthPaint);
//                } else {
//                    hOffset = (tickRotRad * outerTickRadius) - (descrWidth / 2f);
//                    canvas.drawTextOnPath(descrLbl[descrInd], pathOuterCW, hOffset, descrHeight, mCalendarMonthPaint);
//                }
//                */
//
//                float cpX, cpY;
//                cpX = centerX + (float) Math.sin(tickRotRad) * (innerTickRadius + ((outerTickRadius - innerTickRadius) / 2));
//                cpY = centerY + (float) -Math.cos(tickRotRad) * (innerTickRadius + ((outerTickRadius - innerTickRadius) / 2));
//                cpX -= descrWidth / 2f;
//                cpY += descrHeight / 2f;
//                //cpX = (float) Math.sin(tickRotRad) * (innerTickRadius);
//                //cpY = (float) -Math.cos(tickRotRad) * (innerTickRadius);
//                //mCalendarDialPaint.setColor(0xffffffff);
//                canvas.drawText(descrLbl[descrInd],cpX,cpY,mCalendarMonthPaint);
//
//            }
//
//            drawRimShadowsAndLightsNewNew(centerX, centerY, radiusOuter, 3f, canvas, mCalendarMonthPaint, RIM_SMALL_AUX);
//            //drawRimShadowsAndLightsOrg(centerX, centerY, radiusOuter, 2f, canvas, mCalendarMonthPaint);
//
//            boolean isDialClockwise;
//            float handRot, handRotDeg;
//            Time cTime = new Time(mTime);
//            cTime.normalize(true);
//
//
//            //draw month hand
////            if (mMonthHandScaledBitmap == null
////                    || mMonthHandScaledBitmap.getWidth() != 320
////                    || mMonthHandScaledBitmap.getHeight() != 40) {
////                mMonthHandScaledBitmap = Bitmap.createScaledBitmap(mMonthHandBitmap, 320, 40, true);
////            }
//
//            isDialClockwise = true;
//            int month = cTime.month;
//            int day = cTime.monthDay;
//            int maxDaysInMonth = cTime.getActualMaximum(Time.MONTH_DAY);
//            handRot = angleStartRad + angleSweepRad / 12f * (isDialClockwise?month:12f-month) //;
//                    + angleMonthFullTickRad / maxDaysInMonth * (isDialClockwise?day:maxDaysInMonth-day);
//            handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
//            hrMatrix.reset();
//            hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
//            hrMatrix.postRotate(handRotDeg);
//            hrMatrix.postTranslate(centerX, centerY);
//
//            mCalendarDialPaint.setAlpha(255);
//            //canvas.drawBitmap( mMonthHandScaledBitmap, hrMatrix, mCalendarDialPaint);
//            //if (watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandMonthColorized == null) createAuxHandsSM();
//            //
//            mVars.offsetMatrix.set(hrMatrix);
//            mVars.offsetMatrix.postTranslate(0f, 3f);
//            canvas.drawBitmap(watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandMonthShadow,
//                    mVars.offsetMatrix, mCalendarDialPaint);
//            //
//            drawPathBevelLights(canvas, 0, dialElements.mAuxHandWearbattPathScaled[0], hrMatrix, 0x7fffffff, 0x7f000000, 0f);
//            //
//            canvas.drawBitmap(watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandMonthColorized, hrMatrix, mCalendarDialPaint);
//
//
//        } //drawMonthDial1
//
        private void drawMonthDial(Canvas canvas, WatchAuxPosition aux,
                                   float angleStartPointRad, float angleGapDeg, boolean drawDial, boolean drawHand, boolean ambient) {

            float centerX = aux.cX;
            float centerY = aux.cY;
            float radiusOuter = aux.dimension;

            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
            float angleSweepDeg = 360f - angleGapDeg;
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);

            float angleWkdGapDeg = 28f;
            float angleWkdStartRad = (float) ((angleWkdGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleWkdStartDeg = (float) (angleWkdStartRad * 180f / Math.PI);
            float angleWkdSweepDeg = 360f - angleWkdGapDeg;
            float angleWkdSweepRad = (float) (angleWkdSweepDeg * Math.PI / 180f);

            float numArcs, numSubarcs;
            float divisorMain;

            Path txtPath = new Path();

            Matrix hrMatrix = new Matrix();
            //Path pathInner = new Path(), pathOuter = new Path();
            Path pathOuterCW = new Path(), pathOuterCCW = new Path(), pathWkdOuter = new Path();
            float outerTickRadius, innerTickRadius;
            float lblTextSize = mVars.pixelDim(10f);
            String[] descrLbl;
            int[] descrLen;
            RectF dialRect;

            numArcs = 12f; numSubarcs = 4f;
            float heightMnthCircle = mVars.pixelDim(12f);
            divisorMain = numArcs * numSubarcs;
            float angleMonthTickRad = angleSweepRad / (divisorMain /*- 1*/);
            float angleMonthFullTickRad = angleSweepRad / numArcs;
            //outerTickRadius = radiusOuter - heightWkdCircle - 2f; // !!! heightWkdCircle !!!
            outerTickRadius = radiusOuter;


            if (drawDial) { // draw dial
                mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2f));
                mCalendarDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                //mCalendarDialPaint.setARGB(255, 30, 30, 30);
                //mCalendarDialPaint.setARGB(255, 0, 0, 0);
////            if (currentAppearance.mShowDialGradient == false || currentAppearance.mMainSmallAuxDialBackgroundColor != currentAppearance.mMainBackgroundColor) {
////                mCalendarDialPaint.setColor(currentAppearance.mMainSmallAuxDialBackgroundColor);
////                canvas.drawCircle(centerX, centerY, outerTickRadius - 1f, mCalendarDialPaint);
////            } else {
////                Shader shd = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                Shader shd = new BitmapShader(mSmallAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                // mSmallAuxDialGradientNoTransparent currentAppearance.mMainSmallAuxDialBackgroundColor
//                mCalendarDialPaint.setShader(shd);
//                canvas.drawCircle(centerX, centerY, outerTickRadius - 1f, mCalendarDialPaint);
//                mCalendarDialPaint.setShader(null);
////            }
                if (ambient) {
                    mCalendarDialPaint.setColor(Color.BLACK);
                } else {
                    Shader shd = new BitmapShader(mSmallAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    mCalendarDialPaint.setShader(shd);
                }
                mCalendarDialPaint.setAlpha(255);
                canvas.drawCircle(centerX, centerY, outerTickRadius - mVars.pixelDim(1f), mCalendarDialPaint);
                mCalendarDialPaint.setShader(null);

                hrMatrix.reset();
                hrMatrix.setRotate(-90f, centerX, centerY);
                pathOuterCW.reset();
                pathOuterCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CW);
                pathOuterCW.transform(hrMatrix);
                pathOuterCCW.reset();
                pathOuterCCW.addCircle(centerX, centerY, outerTickRadius, Path.Direction.CCW);
                pathOuterCCW.transform(hrMatrix);

                //descrLbl = new String[]{"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
                //descrLen = new int[]   {  1,   2,    3,   2,  1,   2,    3,     4,   2,  1,   2,    3};
                descrLbl = new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
                descrLen = new int[]   {   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2};

                for (int i=30; i > 10; i--) {
                    lblTextSize = i;
                    Rect textBounds = new Rect();
                    mCalendarMonthPaint.setTextSize(lblTextSize);
                    mCalendarMonthPaint.getTextBounds("0", 0, 1, textBounds);
                    if (textBounds.height() == (int)heightMnthCircle) break;
                }
                lblTextSize -= mVars.pixelDim(4f);
                mCalendarMonthPaint.setTextSize(lblTextSize);
                //Log.i(TAG, "((( txtSize=" + lblTextSize);
                float ascent = mCalendarMonthPaint.ascent();
                float descent = mCalendarMonthPaint.descent();
                float txtHeight = descent - ascent;
                float digitsCenterRadius = radiusOuter - txtHeight * 0.9f;

                mCalendarMonthPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mCalendarMonthPaint.setStrokeWidth(mVars.pixelDim(1f));
                //mCalendarMonthPaint.setARGB(200, 255,255,185);
                if (!ambient) mCalendarMonthPaint.setColor(denseAppearance.mMainSmallAuxDialDigitsColor);
                else mCalendarMonthPaint.setColor(denseAppearance.mAmbientTicksColor);;
                //mCalendarMonthPaint.setARGB(180,255,255,255);

                //mCalendarDialPaint.setARGB(180,255,255,255);
                //mCalendarDialPaint.setARGB(200, 255,255,185);
                if (!ambient) mCalendarDialPaint.setColor(denseAppearance.mMainSmallAuxDialTick1Color);
                else mCalendarDialPaint.setColor(denseAppearance.mAmbientTicksColor);
                mCalendarDialPaint.setStyle(Paint.Style.FILL);
                mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2f));

                innerTickRadius = outerTickRadius - mVars.pixelDim(2f);
//                dialRect = new RectF(centerX-innerTickRadius+2, centerY-innerTickRadius+2,
//                        centerX+innerTickRadius-2, centerY+innerTickRadius-2);
                mCalendarDialPaint.setStyle(Paint.Style.STROKE);
                //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mCalendarDialPaint);

                int descrInd = 0;
                for (int tickIndex = 0; tickIndex < divisorMain/*+1*/; tickIndex++) {
                    float tickRotRad = angleStartRad + tickIndex * angleMonthTickRad;
                    if (tickIndex % numSubarcs == 0) {
                        float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius - mVars.pixelDim(6f));
                        float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius - mVars.pixelDim(6f));
                        float outerX = (float) Math.sin(tickRotRad) * (innerTickRadius);
                        float outerY = (float) -Math.cos(tickRotRad) * (innerTickRadius);
                        if (tickIndex == 0) {
                            int color = mCalendarDialPaint.getColor();
                            if (!ambient) mCalendarDialPaint.setColor(denseAppearance.mMainSmallAuxDialTick2Color);
                            else mCalendarDialPaint.setColor(Color.RED);
                            mCalendarDialPaint.setStyle(Paint.Style.FILL);
                            mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                            canvas.drawLine(centerX + innerX, centerY + innerY + mVars.pixelDim(1f), centerX + outerX, centerY + outerY, mCalendarDialPaint);
                            mCalendarDialPaint.setColor(color);
                        } else {
                            mCalendarDialPaint.setStyle(Paint.Style.FILL);
                            mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(1.5f));
                            canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);
                        }
                    } else {
                        float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius - mVars.pixelDim(2f));
                        float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius - mVars.pixelDim(2f));
                        float outerX = (float) Math.sin(tickRotRad) * (innerTickRadius);
                        float outerY = (float) -Math.cos(tickRotRad) * (innerTickRadius);
                        mCalendarDialPaint.setStyle(Paint.Style.FILL);
                        mCalendarDialPaint.setStrokeWidth(mVars.pixelDim(1.5f));
                        canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mCalendarDialPaint);

                        if (tickIndex % 2 == 0) {
                            //int descrInd = (int) (tickIndex / numSubarcs);
                            float descrWidth, descrHeight, exactCenterX, exactCenterY;
                            Rect descrRect = new Rect();
                            //Paint.FontMetrics fontMetrics = mCalendarMonthPaint.getFontMetrics();
                            mCalendarMonthPaint.getTextBounds(descrLbl[descrInd], 0, descrLen[descrInd], descrRect);
                            descrWidth = descrRect.width();
                            //mCalendarMonthPaint.getTextBounds("0",0,1,descrRect);
                            descrHeight = descrRect.height();
                            exactCenterX = descrRect.exactCenterX();
                            exactCenterY = descrRect.exactCenterY();


                            float cpX, cpY;
                              //float cpX1, cpY1, cpX2, cpY2;
                            cpX = centerX + (float) Math.sin(tickRotRad) * (digitsCenterRadius);
                            cpY = centerY + (float) -Math.cos(tickRotRad) * (digitsCenterRadius);
                              //cpX2 = cpX;
                              //cpY2 = cpY;
                            //cpX -= (descrWidth / 2f); //7; //
                            //cpY += (descrHeight / 2f); //6; //
    //                          RectF dr = new RectF(cpX2 - (descrWidth / 2f), cpY2 - (descrHeight / 2f), cpX2 + (descrWidth / 2f), cpY2 + (descrHeight / 2f));
    //                          RectF dr2 = new RectF(cpX2 - (txtHeight / 2f), cpY2 - (txtHeight / 2f), cpX2 + (txtHeight / 2f), cpY2 + (txtHeight / 2f));
    //                          mCalendarMonthPaint.setColor(0xffff8050);
    //                          canvas.drawRect(dr2, mCalendarMonthPaint);
                              //mCalendarMonthPaint.setColor(0xff000000);
                            //canvas.drawText(descrLbl[descrInd], cpX, cpY, mCalendarMonthPaint);
                            //canvas.drawText(descrLbl[descrInd], cpX2 /*- (txtHeight / 2f)*/, cpY2 + (txtHeight / 2f), mCalendarMonthPaint);
    //                        txtPath.reset();
    //                        txtPath.moveTo(cpX2 - exactCenterX /*- 1f*/, cpY2 + -exactCenterY);
    //                        txtPath.lineTo(cpX2 + exactCenterX + 1f, cpY2 + -exactCenterY);
                            //mCalendarMonthPaint.setTextAlign(Paint.Align.CENTER);
                            //canvas.drawTextOnPath(descrLbl[/*descrInd*/0], txtPath, 0, 0, mCalendarMonthPaint);
                            ACommon.drawHvAlignedText(canvas, cpX, cpY, descrLbl[descrInd], mCalendarMonthPaint,
                                    Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

    //                          cpX1 = centerX + (float) Math.sin(tickRotRad) * (digitsCenterRadius - 1.5f);
    //                          cpY1 = centerY + (float) -Math.cos(tickRotRad) * (digitsCenterRadius - 1.5f);
    //                          canvas.drawLine(cpX2, cpY2, cpX1, cpY1, mCalendarDialPaint);
    //                          Log.i(TAG, ">>> " + descrLbl[descrInd] +  ": H=" + descrHeight + ", W=" + descrWidth +
    //                                  ", A=" + mCalendarMonthPaint.ascent() + ", D=" + mCalendarMonthPaint.descent() +
    //                                  ", txtHeight=" + txtHeight +
    //                                  ", exCenterX=" + exactCenterX + ", exCenterY=" + exactCenterY
    //                          );
    //
    //01: H=11.0, W=12.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=6.0, exCenterY=-4.5
    //02: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //03: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //04: H=11.0, W=15.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.5, exCenterY=-4.5
    //05: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //06: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //07: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //08: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //09: H=11.0, W=14.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.0, exCenterY=-4.5
    //10: H=11.0, W=13.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.5, exCenterY=-4.5
    //11: H=10.0, W=11.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=6.5, exCenterY=-5.0
    //12: H=10.0, W=13.0, A=-12.060547, D=3.1738281, txtHeight=15.234375, exCenterX=7.5, exCenterY=-5.0
    //
                            descrInd++;
                        }
                    }


                }


                drawRimShadowsAndLightsNewNew(centerX, centerY, radiusOuter, 3f, canvas, mCalendarMonthPaint, RIM_SMALL_AUX, ambient);
                //drawRimShadowsAndLightsNew(centerX, centerY, radiusOuter, 3f, canvas, mCalendarMonthPaint, RIM_SMALL_AUX, ambient);
                //drawRimShadowsAndLightsOrg(centerX, centerY, radiusOuter, 2f, canvas, mCalendarMonthPaint);

            } // if (drawDial) - draw dial


            if (drawHand) {
                boolean isDialClockwise;
                float handRot, handRotDeg;

                //draw month hand
//            if (mMonthHandScaledBitmap == null
//                    || mMonthHandScaledBitmap.getWidth() != 320
//                    || mMonthHandScaledBitmap.getHeight() != 40) {
//                mMonthHandScaledBitmap = Bitmap.createScaledBitmap(mMonthHandBitmap, 320, 40, true);
//            }

                isDialClockwise = true;

                //Time cTime = new Time(mTime);
                //cTime.normalize(true);
                int month = wTime.getMonth();//cTime.month;
                int day = wTime.getDayOfMonth();//cTime.monthDay;
                int maxDaysInMonth = wTime.getMaxDaysInMonth();//cTime.getActualMaximum(Time.MONTH_DAY);
                //Log.i(TAG, "((( wTime.month="+wTime.getMonth() + ", cTime.month="+month);
                //Log.i(TAG, "((( wTime.monthDay="+wTime.getDayOfMonth() + ", cTime.month="+day);
                //Log.i(TAG, "((( wTime.maxDaysInMonth="+wTime.getMaxDaysInMonth() + ", cTime.maxDaysInMonth="+maxDaysInMonth);

                handRot = angleStartRad + angleSweepRad / 12f * (isDialClockwise?month:12f-month) //;
                        + angleMonthFullTickRad / maxDaysInMonth * (isDialClockwise?day:maxDaysInMonth-day);
                handRotDeg =  (float) (handRot * 180f / Math.PI) - 90f;
                hrMatrix.reset();
                hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
                hrMatrix.postRotate(handRotDeg);
                hrMatrix.postTranslate(centerX, centerY);

                mCalendarDialPaint.setAlpha(255);
                //canvas.drawBitmap( mMonthHandScaledBitmap, hrMatrix, mCalendarDialPaint);
                //if (watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandMonthColorized == null) createAuxHandsSM();
                //
                if (!ambient) {
                    mVars.offsetMatrix.set(hrMatrix);
                    mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(3f));
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandMonthShadow, mVars.offsetMatrix, mCalendarDialPaint);
                    //
                    drawPathBevelLights(canvas, 0, dialElements.mAuxHandWearbattPathScaled[0], hrMatrix, 0x7fffffff, 0x7f000000, 0f);
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandMonthColorized, hrMatrix, mCalendarDialPaint);
                } else {
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandMonthAmbient, hrMatrix, mCalendarDialPaint);
                }

                drawHandMountingHole(canvas, centerX, centerY, ambient);
            }

        } //drawMonthDial



        private void drawHourDigits(Canvas canvas, float centerX, float centerY, float radiusOuter,
                                    float angleStartPointRad, float angleGapDeg, float numArcs, float numSubarcs) {

            float divisorMain = numArcs * numSubarcs;
            RectF dialRect = new RectF(centerX-radiusOuter, centerY-radiusOuter, centerX+radiusOuter, centerY+radiusOuter);
            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad;
            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI);
            float angleSweepDeg = 360f - angleGapDeg;
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);
            float angleTickRad = angleSweepRad / divisorMain;

            mHrTickPaint.setTextSize(39f);
            mHrTickPaint.setARGB(200, 255, 255, 255);
            mHrTickPaint.setStrokeWidth(2f);
            mHrTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mHrTickPaint.setTextScaleX(1f);
            //mHrTickPaint.setFontFeatureSettings("font-weight: 900; font-stretch: expanded;");

            //Shader shd = new LinearGradient(0,0,0,320f, Color.BLUE,Color.GREEN,Shader.TileMode.CLAMP);
            //mHrTickPaint.setShader(shd);

            for (int tickIndex = 0, chind = 0; tickIndex < divisorMain+1; tickIndex++) {
                float outerTickRadius = radiusOuter;
                //float innerTickRadius;
                String chmarks = "123456789101112";
                float chmargin = -4f;

                float tickRot = angleStartRad + tickIndex * angleTickRad; // 0-tickIndex; //

                if (tickIndex > 0 && tickIndex < divisorMain+1) {
                    float posX, posY;
                    Rect txtBounds = new Rect();
                    int chindlen = (tickIndex<10)?1:2;
                    float tw, th;

                    mHrTickPaint.getTextBounds(chmarks, chind, chind + 1, txtBounds);
                    tw = txtBounds.width();
                    th = txtBounds.height();

                    if (tickIndex == 3)  {
                        chind += chindlen;
                        continue;
                    }
                    if (tickIndex == 8 || tickIndex == 9)  {
                        chind += chindlen;
                        continue;
                    }

                    if (chindlen == 2) {
                        float tw2, th2, txtspace = 3f;
                        mHrTickPaint.getTextBounds(chmarks, chind + 1, chind + 2, txtBounds);
                        tw2 = txtBounds.width();
                        th2 = txtBounds.height();
                        posX = centerX + (float) (Math.sin(tickRot) * (outerTickRadius + chmargin)) - (tw + txtspace + tw2) / 2f;
                        posY = centerY + (float) (-Math.cos(tickRot) * (outerTickRadius + chmargin)) + (th /*+ txtspace + th2*/) / 2f;
                        canvas.drawText(chmarks, chind, chind + 1, posX, posY, mHrTickPaint);
                        canvas.drawText(chmarks, chind + 1, chind + 2, posX + tw + txtspace, posY, mHrTickPaint);
                    } else {
                        posX = centerX + (float) (Math.sin(tickRot) * (outerTickRadius + chmargin)) - tw / 2f;
                        posY = centerY + (float) (-Math.cos(tickRot) * (outerTickRadius + chmargin)) + th / 2f;
                        canvas.drawText(chmarks, chind, chind + chindlen, posX, posY, mHrTickPaint);
                    }
                    chind += chindlen;
                }
            }

        }


        private float correctDomDigitsWidth(int dom) {
            final float dom1WidthCorrection = 5f;
            final float dom11WidthCorrection = 5f;
            final float dom1XWidthCorrection = 5f;
            final float dom21WidthCorrection = 5f;
            final float dom31WidthCorrection = 5f;
            switch (dom) {
                case 1: return dom1WidthCorrection;
                case 11: return dom11WidthCorrection;
                case 21: return dom21WidthCorrection;
                case 31: return dom31WidthCorrection;
                default:
                    if (dom==10 || (dom>11 && dom<20)) return dom1XWidthCorrection;
            }
            return 0f;
        } // correctDomWidth

        //drawDateSingle(canvas, centerX + mDigitsRadiusPathInner - 18f, centerY, 25f, 25f);
        //drawDateTriple(canvas, centerX, centerY, mDigitsRadiusPathInner - 38f, 25f);
        //drawDateTriple(canvas, mVars.centerX, mVars.centerY, mDigitsRadiusPathInner - 18f, 25f, mVars.now);
        //private void drawDateTriple(Canvas canvas, float centerX, float centerY, float mainRadius, float boxDim, long millis) {

//        drawDateTriple(canvas, watchLayouts[currentAppearance.watchLayoutIndex].dayOfMonth,
//                       watchLayouts[currentAppearance.watchLayoutIndex].isVertical, 25f, mVars.now);

        //private void drawDateTriple(Canvas canvas, WatchAuxPosition aux, boolean isVertical, float boxDim, long millis) {
        private void drawDateTriple(Canvas canvas, int layoutIndex, boolean ambient) { //long millis,
//            // *** день месяца (в вырезе циферблата)
//            watchAuxPositions[0].cX = mVars.centerX;
//            watchAuxPositions[0].cY = mVars.centerY;
//            watchAuxPositions[0].dimension = mDigitsRadiusPathInner - 18f;
//            watchAuxPositions[0].trigger = false; // дополнительное действие не требуется
//
//            int mDigitTextHeightInPix = 12;
//
//            mVars.centerX = mVars.width / 2f;
//            mVars.centerY = mVars.height / 2f;
//            mVars.radiusClockDialOuterMax = Math.min(mVars.centerX, mVars.centerY);
//            mMainRadius = mVars.radiusClockDialOuterMax - mBurnInMargin;
//            mDigitsRadiusPathOuter = mMainRadius;
//            mDigitsRadiusPathInner = mDigitsRadiusPathOuter - mDigitTextHeightInPix;

            //WatchAuxPosition aux = watchLayouts[layoutIndex].dayOfMonth;
            //boolean isVertical = watchLayouts[layoutIndex].isVertical;
            float boxDim = mVars.mMainRadius * 0.167f; //25f;

//            float mainRadius = watchLayouts[layoutIndex].dayOfMonth.dimension; //aux
//            float centerX = watchLayouts[layoutIndex].dayOfMonth.cX; //aux
//            float centerY = watchLayouts[layoutIndex].dayOfMonth.cY; //aux
            //watchAuxPositions
            float mainRadius = watchAuxPositions[watchLayouts[layoutIndex].dayOfMonth].dimension; //aux
            float centerX = watchAuxPositions[watchLayouts[layoutIndex].dayOfMonth].cX; //aux
            float centerY = watchAuxPositions[watchLayouts[layoutIndex].dayOfMonth].cY; //aux

            float angleStartPointRad;
            if (watchLayouts[layoutIndex].isVertical) {
                angleStartPointRad = (float) (Math.PI / 2f); // +90 deg
            } else {
                angleStartPointRad = (float) (Math.PI); // +180 deg
            }

            float boxCenterTickRotAbsRad = boxDim / mainRadius;
            float boxCenterTickRotAbsDeg = (float) (boxCenterTickRotAbsRad * 180f / Math.PI);
            //float boxCenterTickRotCwRad = angleStartPointRad + boxCenterTickRotAbsRad;
            //float boxCenterTickRotCcwRad = angleStartPointRad - boxCenterTickRotAbsRad;
            //float boxCenterTickRotCcwDeg = (float) (boxCenterTickRotCcwRad * 180f / Math.PI);
            float innerRadius = mainRadius - boxDim / 2f;
            float outerRadius = mainRadius + boxDim * 2f;

            //mDatePaint.setColor(0xffe46f69);
            if (!ambient) {
                mDatePaint.setColor(denseAppearance.mMainDomBackColor);
            } else {
                //mDatePaint.setColor(denseAppearance.mAmbientDomAndAuxHandsColor);
                mDatePaint.setColor(denseAppearance.mAmbientDomBackColor);
            }
            mDatePaint.setStyle(Paint.Style.STROKE);
            mDatePaint.setStrokeWidth(boxDim);
            mDatePaint.setStrokeCap(Paint.Cap.BUTT);
            // drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint)
            if (watchLayouts[layoutIndex].isVertical) {
                canvas.drawArc(centerX - mainRadius, centerY - mainRadius, centerX + mainRadius, centerY + mainRadius, -20f, 40f, false, mDatePaint);
            } else {
                canvas.drawArc(centerX - mainRadius, centerY - mainRadius, centerX + mainRadius, centerY + mainRadius, 70f, 40f, false, mDatePaint);
            }


//            mDatePaint.setTextSize(24f);
//            mDatePaint.setStrokeWidth(1.4f);
            mDatePaint.setTextSize(mVars.pixelDim(24f));
            mDatePaint.setStrokeWidth(mVars.pixelDim(1.4f));
            mDatePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            //mDatePaint.setARGB(220, 240,240,240); // 220, 240,240,240
            if (!ambient) {
                mDatePaint.setColor(denseAppearance.mMainDomFrontColor); // 0xdcf0f0f0
            } else {
                //mDatePaint.setColor(Color.BLACK);
                mDatePaint.setColor(denseAppearance.mAmbientDomFrontColor);
            }
            mDatePaint.setTextScaleX(0.8f);
            //
//            MaskFilter embossFilter = new EmbossMaskFilter(new float[] {10,10,10}, 0.5f, 5f, 2f);
//            mDatePaint.setMaskFilter(embossFilter);
//            mDatePaint.setFilterBitmap(true);
//            mDatePaint.setDither(true);

            int domToday, domTomorrow, domYesterday;
            int julianDayToday, julianDayTomorrow, julianDayYesterday;
            wTime.get3DayOfMonth(mVars.dayTriplet);
            domToday = mVars.dayTriplet[WatchTime.TODAY];//mTime.monthDay;
//            julianDayToday = mTime.getJulianDay(millis,mTime.gmtoff);
//            julianDayTomorrow = julianDayToday + 1;
//            julianDayYesterday = julianDayToday - 1;
            //Log.i(TAG, "((( wTime.doy=" + wTime.getDayOfYear(0) + ", +1=" + wTime.getDayOfYear(1) + ", -1=" + wTime.getDayOfYear(-1));
            //Log.i(TAG, "((( mTime.jd=" + julianDayToday + ", +1=" + julianDayTomorrow + ", -1=" + julianDayYesterday);

            //Time domTime = new Time(mTime);
            //domTime.setJulianDay(julianDayTomorrow);
            domTomorrow = mVars.dayTriplet[WatchTime.TOMORROW];//domTime.monthDay;
            //domTime.setJulianDay(julianDayYesterday);
            domYesterday = mVars.dayTriplet[WatchTime.YESTERDAY];//domTime.monthDay;
            String dom = String.valueOf(domYesterday);
            //mVars.dayTriplet
            //Log.i(TAG, "((( wTime.dom=" + mVars.dayTriplet[WatchTime.TODAY] + ", -1=" + mVars.dayTriplet[WatchTime.YESTERDAY] + ", +1=" + mVars.dayTriplet[WatchTime.TOMORROW]);
            //Log.i(TAG, "((( mTime.dom="+domToday + ", -1="+domYesterday + ", +1="+domTomorrow);

            Rect typeBounds = new Rect();
            float tw, th, tw2;
            mDatePaint.getTextBounds(dom, 0, 1, typeBounds);
            tw = typeBounds.width();
            th = typeBounds.height();

            float fromX, fromY, toX, toY;

            if (watchLayouts[layoutIndex].isVertical) {
                fromX = centerX + (float) Math.sin(angleStartPointRad) * innerRadius;
                fromY = centerY + (float) -Math.cos(angleStartPointRad) * innerRadius;
                toX = centerX + (float) Math.sin(angleStartPointRad) * outerRadius;
                toY = centerY + (float) -Math.cos(angleStartPointRad) * outerRadius;
                //fromX -= boxDim / 2f; toX -= boxDim / 2f;
                fromY += boxDim / 2f;
                toY += boxDim / 2f;
                fromY -= (boxDim - th) / 2f;
                toY -= (boxDim - th) / 2f;
            } else {
                fromX = centerX + (float) Math.sin(angleStartPointRad) * (innerRadius + boxDim - (boxDim - th) / 2f - 1f);
                fromY = centerY + (float) -Math.cos(angleStartPointRad) * (innerRadius + boxDim - (boxDim - th) / 2f - 1f);
                toX = fromX + boxDim / 2f;
                toY = fromY;
                fromX -= boxDim / 2f;
            }

            Path typePath = new Path();
            Matrix typeMatrix = new Matrix();

            typePath.moveTo(fromX, fromY);
            typePath.lineTo(toX, toY);

            if (watchLayouts[layoutIndex].isVertical) {
                typeMatrix.setRotate(-boxCenterTickRotAbsDeg, centerX, centerY);
            } else {
                typeMatrix.setRotate(boxCenterTickRotAbsDeg, centerX, centerY);
            }
            typePath.transform(typeMatrix);
            //dom = String.valueOf(domYesterday);
            mDatePaint.getTextBounds(dom, 0, (domYesterday>9)?2:1, typeBounds);
            tw = typeBounds.width();
            tw += correctDomDigitsWidth(domYesterday);
            canvas.drawTextOnPath(dom, typePath, (boxDim - tw) / 2f, 0f, mDatePaint);

            typeMatrix.reset();
            if (watchLayouts[layoutIndex].isVertical) {
                typeMatrix.setRotate(boxCenterTickRotAbsDeg, centerX, centerY);
            } else {
                typeMatrix.setRotate(-boxCenterTickRotAbsDeg, centerX, centerY);
            }
            typePath.transform(typeMatrix);
            dom = String.valueOf(domToday);
            mDatePaint.getTextBounds(dom, 0, (domToday>9)?2:1, typeBounds);
            tw = typeBounds.width();
            tw += correctDomDigitsWidth(domToday);
            canvas.drawTextOnPath(dom, typePath, (boxDim - tw) / 2f, 0f, mDatePaint);

            //typeMatrix.reset();
            //typeMatrix.setRotate(boxCenterTickRotAbsDeg,centerX,centerY);
            typePath.transform(typeMatrix);
            dom = String.valueOf(domTomorrow);
            mDatePaint.getTextBounds(dom, 0, (domTomorrow>9)?2:1, typeBounds);
            tw = typeBounds.width();
            tw += correctDomDigitsWidth(domTomorrow);
            canvas.drawTextOnPath(dom, typePath, (boxDim - tw) / 2f, 0f, mDatePaint);

        } // drawDateTriple





        //drawDateSingle(canvas, centerX + mDigitsRadiusPathInner - 18f, centerY, 25f, 25f);
        private void drawDateSingle(Canvas canvas, float centerX, float centerY, float width, float height) {
            Rect txtBounds = new Rect();
            float tw, th, tw2 = 0f, th2 = 0f, posX, posY;

            //mDatePaint.setARGB(255, 198,81,75);
            //mDatePaint.setARGB(255, 208,91,85);
            //mDatePaint.setARGB(255, 218,101,95);
            mDatePaint.setARGB(255, 228, 111, 105); //0xffe46f69
            canvas.drawRoundRect(centerX - width / 2f, centerY - width / 2f, centerX + width / 2f, centerY + width / 2f, 3f, 3f, mDatePaint);

            mDatePaint.setTextSize(24f);
            mDatePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mDatePaint.setStrokeWidth(1.4f);
            mDatePaint.setARGB(255, 255, 255, 255);
            mDatePaint.setTextScaleX(0.8f);

            int dayofmonth = wTime.getDayOfMonth();//mTime.monthDay;
            String dom = String.valueOf(dayofmonth);

            mDatePaint.getTextBounds(dom, 0, 1, txtBounds);
            tw = txtBounds.width();
            th = txtBounds.height();
            if (dayofmonth > 9) {
                mDatePaint.getTextBounds(dom, 1, 2, txtBounds);
                tw2 = txtBounds.width();
                th2 = txtBounds.height();
                posX = centerX - (tw+tw2+1f)/2f - 1f;
                posY = centerY + th/2f - 0.75f;
                canvas.drawText(dom,0,1,posX,posY,mDatePaint);
                canvas.drawText(dom,1,2,posX+tw+1f,posY,mDatePaint);
            } else {
                posX = centerX - tw/2f;
                posY = centerY + th/2f - 0.75f;
                canvas.drawText(dom,0,1,posX,posY,mDatePaint);
            }
        }


        //drawBattDial(canvas, cx, cy-1, r, (float)Math.PI, 20f, 10f, 5f);
        //drawBattDial(canvas, watchLayouts[currentAppearance.watchLayoutIndex].auxB, (float)Math.PI, 20f, 10f, 5f);
//        private void drawBattDial(Canvas canvas, float centerX, float centerY, float radiusOuter,
//                                  float angleStartPointRad, float angleGapDeg, float numArcs, float numSubarcs) {
        private void drawBattDial(Canvas canvas, WatchAuxPosition aux,
                                  float angleStartPointRad, float angleGapDeg, float numArcs, float numSubarcs,
                                  boolean drawDial, boolean drawHand, boolean ambient) {

            float centerX = aux.cX;
            float centerY = aux.cY;
            float radiusOuter = aux.dimension;

            float divisorMain = numArcs * numSubarcs;

            RectF dialRect = new RectF(centerX-radiusOuter, centerY-radiusOuter, centerX+radiusOuter, centerY+radiusOuter);

            float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad; //(float) (angleStartDeg * Math.PI / 180f);
            float angleStartDeg = (float) (angleStartRad * 180f / Math.PI); // angleGapDeg / 2f; // /*-60f*/ angleStartPointDeg + angleGapDeg / 2f;
            float angleSweepDeg = /*300f*/ 360f - angleGapDeg; // 360f - angleGapDeg
            float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);
            float angleTickRad = angleSweepRad / (divisorMain /*- 1*/);

            //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mBattDialPaint);
            ////canvas.drawCircle(centerX, centerY, radiusOuter, mCalendarDialPaint);

            mBattDialPaint.setTextSize(mVars.pixelDim(12f));
            mBattDialPaint.setStrokeWidth(mVars.pixelDim(2f));
            mBattDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            //mBattDialPaint.setARGB(255, 30, 30, 30);
            //mBattDialPaint.setARGB(255, 0, 0, 0);
            //mBattDialPaint.setARGB(255, 145, 138, 57);
            mBattDialPaint.setAntiAlias(true);
            mBattDialPaint.setFilterBitmap(true);
            mBattDialPaint.setDither(true);

////            if (currentAppearance.mShowDialGradient == false || currentAppearance.mMainSmallAuxDialBackgroundColor != currentAppearance.mMainBackgroundColor) {
////                mBattDialPaint.setColor(currentAppearance.mMainSmallAuxDialBackgroundColor); //mMainBackgroundColor
////                canvas.drawCircle(centerX, centerY, radiusOuter, mBattDialPaint);
////            } else {
//                //Shader shd = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                Shader shd = new BitmapShader(mSmallAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                // mSmallAuxDialGradientNoTransparent currentAppearance.mMainSmallAuxDialBackgroundColor
//                mBattDialPaint.setShader(shd);
//                canvas.drawCircle(centerX, centerY, radiusOuter - 1f, mBattDialPaint);
//                mBattDialPaint.setShader(null);
////            }


            if (drawDial) { // draw dial elements
                if (ambient) {
                    mBattDialPaint.setColor(Color.BLACK);
                } else {
                    Shader shd = new BitmapShader(mSmallAuxDialGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    mBattDialPaint.setShader(shd);
                }
                mBattDialPaint.setAlpha(255);
                canvas.drawCircle(centerX, centerY, radiusOuter - mVars.pixelDim(1f), mBattDialPaint);
                mBattDialPaint.setShader(null);

                for (int tickIndex = 0, chind = 0; tickIndex < divisorMain+1; tickIndex++) {
                    float outerTickRadius = radiusOuter - mVars.pixelDim(1.5f);
                    float innerTickRadius;
                    String chmarks = "102030405060708090";
                    float chmargin = mVars.pixelDim(-12f);

                    if (tickIndex % numSubarcs == 0) innerTickRadius = outerTickRadius - mVars.pixelDim(5f);
                    else innerTickRadius = outerTickRadius - mVars.pixelDim(2f);
                    if (tickIndex <= 5) innerTickRadius -= mVars.pixelDim(3f);
                    float tickRot = angleStartRad + tickIndex * angleTickRad; // 0-tickIndex; //
                    float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                    float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                    float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                    float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                    if (tickIndex <= 5) {
                        if (!ambient) mBattDialPaint.setColor(denseAppearance.mMainSmallAuxDialTick2Color);
                        else mBattDialPaint.setColor(Color.RED);
                    } else {
                        if (!ambient) mBattDialPaint.setColor(denseAppearance.mMainSmallAuxDialTick1Color);
                        else mBattDialPaint.setColor(denseAppearance.mAmbientTicksColor);
                    }
                    mBattDialPaint.setStrokeWidth(mVars.pixelDim(2f));
                    mBattDialPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, mBattDialPaint);
                    if (tickIndex > 0 && tickIndex % 5 == 0 && tickIndex < divisorMain-1 /*&& chind < 8*/) {
                        float posX, posY;
                        Rect txtBounds = new Rect();
                        mBattDialPaint.getTextBounds(chmarks, chind, chind+2, txtBounds);
                        float tw = txtBounds.width(), th = txtBounds.height();
                        //if (chind < chmiddle) posX = centerX + outerX - tw;
                        //else posX = centerX + outerX;
                        posX = centerX + (float) (Math.sin(tickRot) * (outerTickRadius + chmargin)) - tw / 2f;
                        posY = centerY + (float) (-Math.cos(tickRot) * (outerTickRadius + chmargin)) + th / 2f;
                        //mBattDialPaint.setARGB(200, 255, 255, 255); // 0xc8ffffff
                        if (!ambient) mBattDialPaint.setColor(denseAppearance.mMainSmallAuxDialDigitsColor);
                        else mBattDialPaint.setColor(denseAppearance.mAmbientTicksColor);
                        mBattDialPaint.setStrokeWidth(mVars.pixelDim(1f));
                        mBattDialPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawText(chmarks, chind, chind + 2, posX, posY, mBattDialPaint);
                        chind += 2;
                    }
                }

                drawRimShadowsAndLightsNewNew(centerX, centerY, radiusOuter, 3f, canvas, mBattDialPaint, RIM_SMALL_AUX, ambient);
                //drawRimShadowsAndLightsNew(centerX, centerY, radiusOuter, 3f, canvas, mBattDialPaint, RIM_SMALL_AUX, ambient);
            } // if (drawDial) - draw dial elements


            if (drawHand) { // draw hands
                boolean isDialClockwise;
                float secRot;
                float secRotDeg;
                float battValue;
                //Matrix hrMatrix = new Matrix();

                //draw phone battery hand
                if (!ambient && mAppPreferences.getShowHandheldBattery() == true) {
                    isDialClockwise = true;
                    battValue = mPhoneBattery;
                    if (DemoPackData.isActive(demoPackData)) battValue = DemoPackData.getPhoneBattery(demoPackData);
                    secRot = angleStartRad + angleSweepRad / 100f * (isDialClockwise?battValue:100f-battValue);
                    secRotDeg =  (float) (secRot * 180f / Math.PI) - 90f;
                    mVars.hrMatrix.reset();
                    mVars.hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
                    mVars.hrMatrix.postRotate(secRotDeg);
                    mVars.hrMatrix.postTranslate(centerX, centerY);
                    mBattDialPaint.setARGB(255, 255, 255, 255);
                    //canvas.drawBitmap( mPhBHandScaledBitmap, mVars.hrMatrix, mBattDialPaint);
                    //if (watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandPhoneBattColorized == null) createAuxHandsSM();
                    //
                    mVars.offsetMatrix.set(mVars.hrMatrix);
                    mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(2f));
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandPhoneBattShadow,
                            mVars.offsetMatrix, mBattDialPaint);
                    //
                    drawPathBevelLights(canvas, 0, dialElements.mAuxHandPhonebattPathScaled[0], mVars.hrMatrix, 0x7fffffff, 0x7f000000, 0f);
                    //
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandPhoneBattColorized, mVars.hrMatrix, mBattDialPaint);
                }

                //draw wear battery hand
                isDialClockwise = true;
                battValue = mWatchesBattery;
                if (DemoPackData.isActive(demoPackData)) battValue = DemoPackData.getWearBattery(demoPackData);
                secRot = angleStartRad + angleSweepRad / 100f * (isDialClockwise?battValue:100f-battValue);
                secRotDeg = (float) (secRot * 180f / Math.PI) - 90f;

                mVars.hrMatrix.reset();
                mVars.hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
                mVars.hrMatrix.postRotate(secRotDeg);
                mVars.hrMatrix.postTranslate(centerX, centerY);
                //mBattDialPaint.setARGB(255, 255,255,255);
                //canvas.drawBitmap(mWBHandScaledBitmap, hrMatrix, mBattDialPaint);

                mBattDialPaint.setARGB(255, 255, 255, 255);
                //canvas.drawBitmap( mWBHandScaledBitmapMutableUpper, mVars.hrMatrix, mBattDialPaint);
                //if (watchAuxHandsBmp[currentAppearance.watchAuxHandsIndex].mAuxHandWearBattColorized == null) createAuxHandsSM();
                //
                mVars.offsetMatrix.set(mVars.hrMatrix);
                if (!ambient) {
                    if (mAppPreferences.getShowHandheldBattery() == true) mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(4f));
                    else mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(3f));
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWearBattShadow,
                            mVars.offsetMatrix, mBattDialPaint);
                    //
                    drawPathBevelLights(canvas, 0, dialElements.mAuxHandWearbattPathScaled[0], mVars.hrMatrix, 0x7fffffff, 0x7f000000, 0f);
                    //
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWearBattColorized, mVars.hrMatrix, mBattDialPaint);
                } else {
                    canvas.drawBitmap(watchAuxHandsBmp[denseAppearance.watchAuxHandsIndex].mAuxHandWearBattAmbient, mVars.hrMatrix, mBattDialPaint);
                }

                drawHandMountingHole(canvas, centerX, centerY, ambient);
            } // if (drawHand) { // draw hands

        } //drawBattDial



        private void setStaticProjection180dgr() {
            mPrjP = 0.36728433f; // используется для вычисления смещения тени, при вычислении координаты X
            mPrjR = -1.5049046f; // используется для вычисления смещения тени, при вычислении координаты Y
            mPrjAngleNorm = 3.14f;
            mPrjLenNorm = 93.54681f;
        }

        private void conditionallyCorrectProjectionValues(float centerX, float centerY) {

            if (true) return;

            float angle;
            //Log.i(TAG, "((((( conditionallyCorrectProjectionValues, centerX=" + centerX + ", centerY=" + centerY);
            angle = (float) Math.atan((centerX - 160f) / (centerY - 160f));
            //Log.i(TAG, "((((( angle=" + angle);
            if (!denseAppearance.mShowRimAnimation) {
                setStaticProjection180dgr();
                if (denseAppearance.mShowDialGradient) {
                    // вычислить новое значение mPrjAngleNorm для кольца с центром в (centerX, centerY)
                    if (centerX == 160) {
                        if (centerY < 160) { // big aux dial
                            mPrjAngleNorm = 0f;
                        } else {
                            mPrjAngleNorm = 3.14f;
                        }
                    } else {
                        mPrjAngleNorm = (float) (Math.PI - angle);
                    }
                }
            }
        }

        private void drawRimShadowsAndLightsOrg(float centerX, float centerY, float radiusOuter, float rimDim,
                                             Canvas canvas, Paint rimPaint) {
            //int[] rimColors = new int[]{0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000};
            //float[] rimColorOnPathPos = new float[]{0f,0.30f,0.45f,0.55f,0.70f,1.0f};
            //Matrix hrMatrix = new Matrix();
            //Path rimPath = new Path();

            if (isInAmbientMode() == true) return;

            conditionallyCorrectProjectionValues(centerX, centerY);

            mVars.rimPath.reset();
            mVars.rimPath.addCircle(centerX, centerY, radiusOuter, Path.Direction.CW);
            mVars.hrMatrix.reset();
            Shader rimShader = new SweepGradient(centerX, centerY, mVars.rimColors, mVars.rimColorOnPathPos);
            rimShader.getLocalMatrix(mVars.hrMatrix);
            mVars.hrMatrix.postRotate(mPrjAngleNorm * RAD2DEG + 90f, centerX, centerY);
            rimShader.setLocalMatrix(mVars.hrMatrix);
            rimPaint.setShader(rimShader);
            rimPaint.setStyle(Paint.Style.STROKE);
            rimPaint.setStrokeWidth(rimDim);
            //rimPaint.setAlpha((int) (170*mPrjLenNorm/mShdStickHeight)); //127
            int alpha = (int) (170*mPrjLenNorm/mShdStickHeight);
            int r, g, b;
            r = Color.red(denseAppearance.mMainBackgroundColor);
            g = Color.green(denseAppearance.mMainBackgroundColor);
            b = Color.blue(denseAppearance.mMainBackgroundColor);
            rimPaint.setARGB(alpha, r, g, b);
            canvas.drawPath(mVars.rimPath, rimPaint);
            rimPaint.setShader(null);
            //Log.i(TAG, "=== mPrjP=" + mPrjP + ", mPrjR=" + mPrjR);
            //mPrjAngleNorm=3.074732, mPrjLenNorm=93.54681
        } // drawRimShadowsAndLights
        //
        Path auxBevelShadowPath = new Path();
        Paint auxBevelShadowPaint = new Paint();
        Path auxBevelShadowPathSmall = new Path(), auxBevelShadowPathBig = new Path();
        Canvas auxBevelShadowCanvas = new Canvas();
        int auxBevelColors[] = new int[] {0x00000000, 0x00000000, 0x7f000000};
        float[] auxBevelStops = new float[] {0.0f, 0.9f, 1.0f};
        Shader auxBevelShadowShader; // = new RadialGradient();
        Matrix auxBevelShadowMatrix = new Matrix();
        private void drawAuxBevelShadow(Canvas canvas, float centerX, float centerY, float radiusOuter) {

            auxBevelShadowShader = new RadialGradient(centerX, centerY, radiusOuter, auxBevelColors, auxBevelStops, Shader.TileMode.CLAMP);

//            auxBevelShadowCanvas.setBitmap(mVars.auxBevelShadowBitmap);
//            auxBevelShadowCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            //auxBevelShadowPath.setFillType(Path.FillType.EVEN_ODD);

            auxBevelShadowPath.reset();
            auxBevelShadowPathSmall.reset();
            auxBevelShadowPathBig.reset();

            auxBevelShadowPathSmall.addCircle(centerX, centerY, radiusOuter, Path.Direction.CW);
            auxBevelShadowPathBig.addCircle(centerX, centerY + mVars.pixelDim(5f), radiusOuter, Path.Direction.CW);
            auxBevelShadowPath.op(auxBevelShadowPathSmall, auxBevelShadowPathBig, Path.Op.DIFFERENCE);
            auxBevelShadowMatrix.reset();
            auxBevelShadowMatrix.postTranslate(0f, mVars.pixelDim(1f));
            auxBevelShadowPath.transform(auxBevelShadowMatrix);

            //auxBevelShadowPaint.setColor(Color.argb(180, 0, 0, 0));
            auxBevelShadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            auxBevelShadowPaint.setStrokeWidth(mVars.pixelDim(1f));
            auxBevelShadowPaint.setAntiAlias(true);
            auxBevelShadowPaint.setDither(true);
            auxBevelShadowPaint.setFilterBitmap(true);

//            auxBevelShadowCanvas.drawPath(auxBevelShadowPath, auxBevelShadowPaint);
//            mVars.auxBevelShadowBitmap = blur(mVars.auxBevelShadowBitmap, 7f);
//
//            canvas.drawBitmap(mVars.auxBevelShadowBitmap, 0, 0, null);


            auxBevelShadowPaint.setMaskFilter(mVars.blurMaskFilterR2N);

            auxBevelShadowPaint.setShader(auxBevelShadowShader);
            canvas.drawPath(auxBevelShadowPath, auxBevelShadowPaint);
            auxBevelShadowPaint.setShader(null);
        } // drawAuxBevelShadow
        //
        static final int RIM_BIG_AUX = 1;
        static final int RIM_SMALL_AUX = 2;
//        int[] rimColors = new int[]{0x3f000000, 0x3f000000, 0x3fffffff, 0x3fffffff, 0x3f000000, 0x3f000000};
//        float[] rimColorOnPathPos = new float[]{0f, 0.20f, 0.40f, 0.60f, 0.80f, 1.0f};
        int[] rimColors = new int[]{0x3f000000, 0x3f000000, 0x3fcfcfcf, 0x3fffffff, 0x3fcfcfcf, 0x3f000000, 0x3f000000};
        float[] rimColorOnPathPos = new float[]{0f, 0.20f, 0.35f, 0.50f, 0.65f, 0.80f, 1.0f};
        //
        private void drawRimShadowsAndLightsNew(float centerX, float centerY, float radiusOuter, float rimDim,
                                             Canvas canvas, Paint rimPaint, int kind, boolean ambient) {

            float rotAngleDeg = mPrjAngleNorm * RAD2DEG + 90f;
            int alpha = (int) (170 * mPrjLenNorm / mShdStickHeight);

            if (ambient == true) return;

            conditionallyCorrectProjectionValues(centerX, centerY);


            drawAuxBevelShadow(canvas, centerX, centerY, radiusOuter);


            mVars.rimPath.reset();
            mVars.rimPath.addCircle(centerX, centerY, radiusOuter, Path.Direction.CW);
            //
            mVars.hrMatrix.reset();
            //
            for (int i=0; i<rimColors.length; i++) {
                rimColors[i] = (rimColors[i] & 0x00ffffff) | (alpha << 24);
            }
            //
            Shader rimShaderA = new SweepGradient(centerX, centerY, /*mVars.*/rimColors, /*mVars.*/rimColorOnPathPos);
            rimShaderA.getLocalMatrix(mVars.hrMatrix);
            //mPitchRad, mRollRad
            //float rotAngleRad = (float) Math.atan((-Math.sin(mPitchRad)) / (Math.cos(mPitchRad) * Math.sin(mRollRad)));
            float rotAngleRad = (float) Math.atan(mPitchRad / mRollRad);
            //Log.i(TAG, "((((( Pitch=" + mPitchRad + ", Roll=" + mRollRad + ", rotAngleRad=" +rotAngleRad * RAD2DEG);
            //Log.i(TAG, "((((( rotAngleDeg(" + kind + ", " + rotAngleRad + ")=" + rotAngleDeg + ", mPrjAngleNorm=" + mPrjAngleNorm);
            mVars.hrMatrix.postRotate(rotAngleDeg, centerX, centerY);
            rimShaderA.setLocalMatrix(mVars.hrMatrix);
            //
            Bitmap bmp;
            if (denseAppearance.mAuxBevelColor == ACommon.BEVEL_FROM_AUX) {
                if (kind == RIM_BIG_AUX) bmp = mBigAuxDialGradientNoTransparent;
                else bmp = mSmallAuxDialGradientNoTransparent;
            } else bmp = mCircleGradientNoTransparent;
            //
            Shader rimShaderB = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            // Yes! mCircleGradientNoTransparent
            // ? mCircleGradientS
            // ? mCircleGradient
            // NO! mFxPlateTexture
            // ? mBigAuxDialGradientNoTransparent
            // ? mBigAuxDialGradient
            // ? mSmallAuxDialGradientNoTransparent
            // ? mSmallAuxDialGradient
            //
            Shader rimShaderC = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.OVERLAY); // GOOD!
            //Shader rimShader = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.SCREEN); // no
            //Shader rimShader = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.MULTIPLY); // no
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.OVERLAY); // so so...
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.SCREEN); // no
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.MULTIPLY); // no
            //Shader rimShaderC = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.DST_OVER);
            //
            rimPaint.setShader(rimShaderC);
            rimPaint.setStyle(Paint.Style.STROKE);
            //rimPaint.setStrokeWidth(rimDim);
            rimPaint.setStrokeWidth(mVars.pixelDim(rimDim));
//            int alpha = (int) (170*mPrjLenNorm/mShdStickHeight);
//            int r, g, b;
//            r = Color.red(currentAppearance.mMainBackgroundColor);
//            g = Color.green(currentAppearance.mMainBackgroundColor);
//            b = Color.blue(currentAppearance.mMainBackgroundColor);
//            rimPaint.setARGB(alpha, r, g, b);
            //
            //rimPaint.setAlpha((int) (170*mPrjLenNorm/mShdStickHeight)); //127
            ////rimPaint.setAlpha((int) 255); //127
            //
            if (true) canvas.drawPath(mVars.rimPath, rimPaint);
            rimPaint.setShader(null);
            //Log.i(TAG, "=== mPrjP=" + mPrjP + ", mPrjR=" + mPrjR);
            //mPrjAngleNorm=3.074732, mPrjLenNorm=93.54681
        } // drawRimShadowsAndLights
        //
        private void drawRimShadowsAndLightsNewNew(float centerX, float centerY, float radiusOuter, float rimDim,
                                                Canvas canvas, Paint rimPaint, int kind, boolean ambient) {

            float rotAngleDeg = mPrjAngleNorm * RAD2DEG + 90f;
            int alpha = (int) (170 * mPrjLenNorm / mShdStickHeight);

            if (ambient == true) return;

            conditionallyCorrectProjectionValues(centerX, centerY);


            Bitmap bmpLocal = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cnvLocal = new Canvas(bmpLocal);




            drawAuxBevelShadow(cnvLocal, centerX, centerY, radiusOuter);


            mVars.rimPath.reset();
            mVars.rimPath.addCircle(centerX, centerY, radiusOuter, Path.Direction.CW);
            //
            mVars.hrMatrix.reset();
            //
            for (int i=0; i<rimColors.length; i++) {
                rimColors[i] = (rimColors[i] & 0x00ffffff) | (alpha << 24);
            }
            //
            Shader rimShaderA = new SweepGradient(centerX, centerY, /*mVars.*/rimColors, /*mVars.*/rimColorOnPathPos);
            rimShaderA.getLocalMatrix(mVars.hrMatrix);
            //mPitchRad, mRollRad
            //float rotAngleRad = (float) Math.atan((-Math.sin(mPitchRad)) / (Math.cos(mPitchRad) * Math.sin(mRollRad)));
            float rotAngleRad = (float) Math.atan(mPitchRad / mRollRad);
            //Log.i(TAG, "((((( Pitch=" + mPitchRad + ", Roll=" + mRollRad + ", rotAngleRad=" +rotAngleRad * RAD2DEG);
            //Log.i(TAG, "((((( rotAngleDeg(" + kind + ", " + rotAngleRad + ")=" + rotAngleDeg + ", mPrjAngleNorm=" + mPrjAngleNorm);
            mVars.hrMatrix.postRotate(rotAngleDeg, centerX, centerY);
            rimShaderA.setLocalMatrix(mVars.hrMatrix);
            //
            Bitmap bmp;
            if (denseAppearance.mAuxBevelColor == ACommon.BEVEL_FROM_AUX) {
                if (kind == RIM_BIG_AUX) bmp = mBigAuxDialGradientNoTransparent;
                else bmp = mSmallAuxDialGradientNoTransparent;
            } else bmp = mCircleGradientNoTransparent;
            //
            Shader rimShaderB = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            // Yes! mCircleGradientNoTransparent
            // ? mCircleGradientS
            // ? mCircleGradient
            // NO! mFxPlateTexture
            // ? mBigAuxDialGradientNoTransparent
            // ? mBigAuxDialGradient
            // ? mSmallAuxDialGradientNoTransparent
            // ? mSmallAuxDialGradient
            //
            Shader rimShaderC = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.OVERLAY); // GOOD!
            //Shader rimShader = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.SCREEN); // no
            //Shader rimShader = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.MULTIPLY); // no
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.OVERLAY); // so so...
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.SCREEN); // no
            //Shader rimShader = new ComposeShader(rimShaderB, rimShaderA, PorterDuff.Mode.MULTIPLY); // no
            //Shader rimShaderC = new ComposeShader(rimShaderA, rimShaderB, PorterDuff.Mode.DST_OVER);
            //
            rimPaint.setShader(rimShaderC);
            rimPaint.setStyle(Paint.Style.STROKE);
            //rimPaint.setStrokeWidth(rimDim);
            rimPaint.setStrokeWidth(mVars.pixelDim(rimDim));
//            int alpha = (int) (170*mPrjLenNorm/mShdStickHeight);
//            int r, g, b;
//            r = Color.red(currentAppearance.mMainBackgroundColor);
//            g = Color.green(currentAppearance.mMainBackgroundColor);
//            b = Color.blue(currentAppearance.mMainBackgroundColor);
//            rimPaint.setARGB(alpha, r, g, b);
            //
            //rimPaint.setAlpha((int) (170*mPrjLenNorm/mShdStickHeight)); //127
            ////rimPaint.setAlpha((int) 255); //127
            //
            if (true) cnvLocal.drawPath(mVars.rimPath, rimPaint);
            rimPaint.setShader(null);
            //Log.i(TAG, "=== mPrjP=" + mPrjP + ", mPrjR=" + mPrjR);
            //mPrjAngleNorm=3.074732, mPrjLenNorm=93.54681



            bmpLocal = blur(bmpLocal, 1.5f);
            canvas.drawBitmap(bmpLocal, 0f, 0f, null);


        } // drawRimShadowsAndLightsNewNew











/*
        private void changeBackgroundColorOld(int color) {
            if (null == mBackgroundBitmapScaledMutable) return;;
            int bmpwidth = mBackgroundBitmapScaledMutable.getWidth(), bmpheight = mBackgroundBitmapScaledMutable.getHeight();
            int[] pixrow = new int[bmpwidth];
            for (int y=0; y<bmpheight; y++) {
                mBackgroundBitmapScaledMutable.getPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
                for (int x=0; x<bmpwidth; x++) {
                    pixrow[x] = (pixrow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                }
                mBackgroundBitmapScaledMutable.setPixels(pixrow, 0, bmpwidth, 0, y, bmpwidth, 1);
            }
        } // changeBackgroundColorOld
*/







        long mSeconds = 0;
        int mFramePerSecond = 0;
        int mFrameCount = 0;





        private void drawOuterTicksAndDigitsORG(Canvas canvas) {

            // Draw the minutes/seconds ticks.
            //float innerTickRadius = centerX - 12/*15*/;
            //float outerTickRadius = centerX;
//            mVars.innerTickRadius = mDigitsRadiusPathInner + 2f;
//            mVars.outerTickRadius = mDigitsRadiusPathOuter;
            mTickPaint.setAntiAlias(true);
            mTickPaint.setStrokeWidth(2f);
            //mTickPaint.setAntiAlias(true);
            mTickPaint.setStrokeCap(Paint.Cap.ROUND);
            mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            MaskFilter embossFilter = new EmbossMaskFilter(new float[] {1,1,1}, 0.5f, 5f, 2f);
//            mTickPaint.setMaskFilter(embossFilter);
            for (mVars.tickIndex = 1/*0*/; mVars.tickIndex < 60; mVars.tickIndex++) {
                if (mVars.tickIndex % 5 == 0) continue;
                mVars.tickRot = (float) (mVars.tickIndex * Math.PI * 2 / 60);
                mVars.innerX = (float) Math.sin(mVars.tickRot) * mVars.innerTickRadius;
                mVars.innerY = (float) -Math.cos(mVars.tickRot) * mVars.innerTickRadius;
                mVars.outerX = (float) Math.sin(mVars.tickRot) * mVars.outerTickRadius;
                mVars.outerY = (float) -Math.cos(mVars.tickRot) * mVars.outerTickRadius;
                canvas.drawLine(mVars.centerX + mVars.innerX, mVars.centerY + mVars.innerY, mVars.centerX + mVars.outerX, mVars.centerY + mVars.outerY, mTickPaint);
            }

            //if (true) return;

            // Draw minute/second digits
            // float mDigitsRadiusPathInner = 146.f;
            // float mDigitsRadiusPathOuter = 158.f;
            //Matrix hrMatrix = new Matrix();
            mVars.hrMatrix.reset();
            //Path mPathInner = new Path(), mPathOuter = new Path();
            mVars.mPathInner.reset();
            mVars.mPathOuter.reset();
            mVars.mPathInner.addCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathInner, Path.Direction.CW);
            mVars.hrMatrix.setRotate(-96.f, mVars.centerX, mVars.centerY);
            mVars.mPathInner.transform(mVars.hrMatrix);
            mVars.mPathOuter.addCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathOuter, Path.Direction.CCW);
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setRotate(-84.f, mVars.centerX, mVars.centerY);
            mVars.mPathOuter.transform(mVars.hrMatrix);
            mTickDigitPaint.setStyle(Paint.Style.FILL_AND_STROKE/*FILL*//*FILL_AND_STROKE*//*STROKE*/);
            /*canvas.drawPath(mPathInner, mTickDigitPaint);*/
            /*canvas.drawPath(mPathOuter, mTickDigitPaint);*/
            mTickDigitPaint.setTextSize(mVars.mDigitTextSize);
            mTickDigitPaint.setAntiAlias(true);
            mVars.corrCount = 4;
            for (mVars.tickIndex = 0; mVars.tickIndex < 12; mVars.tickIndex++) {
                //float digitPos;
                //int corrIndex;
                String firstDigit, secondDigit;

                if (mVars.tickIndex > 3 && mVars.tickIndex < 9) {
                    mVars.corrIndex = mVars.tickIndex + mVars.corrCount;
                    mVars.corrCount -= 2;
                } else mVars.corrIndex = mVars.tickIndex;

                if (mVars.corrIndex/*tickIndex*/ % 2 == 0) secondDigit = "0";
                else secondDigit = "5";
                if (mVars.corrIndex/*tickIndex*/ == 0) firstDigit = "6";
                else if (mVars.corrIndex/*tickIndex*/ == 1) firstDigit = "0";
                else firstDigit = String.valueOf(mVars.tickIndex/*corrIndex*//*tickIndex*/ / 2);


                mTickDigitPaint.getTextBounds(firstDigit, 0, 1, mVars.textBounds);
                float fw = mVars.textBounds.width();
                mTickDigitPaint.getTextBounds(secondDigit, 0, 1, mVars.textBounds);
                float sw = mVars.textBounds.width();
                float a, a1, a2, b, m1, m2;
                b = fw + sw;

                if (mVars.tickIndex > 3 && mVars.tickIndex < 9) {
                    a2 = (float) Math.PI * mVars.mDigitsRadiusPathOuter * (((float) mVars.corrIndex * 30.f) + 12f) / 180.f;
                    a1 = (float) Math.PI * mVars.mDigitsRadiusPathOuter * (((float) mVars.corrIndex * 30.f) - 0f) / 180.f;
                    a = a2 - a1;
                    m1 = (a - b) / 2;
                    m2 = m1 + fw;
                    //digitPos = (float) (/*8.f +*/ ((Math.PI * mDigitsRadiusPathOuter * (float) corrIndex/*tickIndex*/ * 30.f) / 180.f));
                    mVars.digitPos = a1 + m1;
                    canvas.drawTextOnPath(firstDigit, mVars.mPathOuter, mVars.digitPos, -1f, mTickDigitPaint);
                    //digitPos += 9.f;
                    mVars.digitPos = a1 + m2;
                    canvas.drawTextOnPath(secondDigit, mVars.mPathOuter, mVars.digitPos, -1f, mTickDigitPaint);
                } else {
                    a2 = (float) Math.PI * mVars.mDigitsRadiusPathInner * (((float) mVars.corrIndex * 30.f) + 12f) / 180.f;
                    a1 = (float) Math.PI * mVars.mDigitsRadiusPathInner * (((float) mVars.corrIndex * 30.f) - 0f) / 180.f;
                    a = a2 - a1;
                    m1 = (a - b) / 2;
                    m2 = m1 + fw;
                    //digitPos = (float) (/*7.f*/ + ((Math.PI * mDigitsRadiusPathInner * (float) corrIndex/*tickIndex*/ * 30.f) / 180.f));
                    mVars.digitPos = a1 + m1;
                    if (mVars.corrIndex == 10 || mVars.corrIndex == 11) mVars.digitPos -= 0.5f; // 50 and 55
                    if (mVars.corrIndex > 1 && mVars.corrIndex < 4) mVars.digitPos -= 2f; // 10 and 15
                    canvas.drawTextOnPath(firstDigit, mVars.mPathInner, mVars.digitPos, -1f, mTickDigitPaint);
                    //digitPos += 9.f;
                    mVars.digitPos = a1 + m2;
                    if (mVars.corrIndex == 10 || mVars.corrIndex == 11) mVars.digitPos += 0.5f; // 50 and 55
                    if (mVars.corrIndex == 9) mVars.digitPos -= 1; // 45
                    canvas.drawTextOnPath(secondDigit, mVars.mPathInner, mVars.digitPos, -1f, mTickDigitPaint);
                }
            }
            //canvas.drawTextOnPath("60", mPath, 5f, 0f, mTickDigitPaint);

            //drawHourDigits(canvas,centerX,centerY,mDigitsRadiusPathInner-20f,0f,0,12,1);
        } // drawOuterTicksAndDigits
        //
        private void drawOuterTicksAndDigits(Canvas canvas, boolean ambient, boolean forBlur) {

            int forBlurColor = Color.argb(127, 0, 0, 0);

            // Draw the minutes/seconds ticks.
            mTickPaint.setAntiAlias(true);
            if (!forBlur) mTickPaint.setStrokeWidth(mVars.pixelDim(2f));
            else mTickPaint.setStrokeWidth(mVars.pixelDim(3f));
            //mTickPaint.setAntiAlias(true);
            mTickPaint.setStrokeCap(Paint.Cap.ROUND);
            mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            MaskFilter embossFilter = new EmbossMaskFilter(new float[] {1,1,1}, 0.5f, 5f, 2f);
//            mTickPaint.setMaskFilter(embossFilter);
            if (ambient) {
                mTickPaint.setColor(denseAppearance.mAmbientTicksColor);
            } else {
                if (!forBlur) mTickPaint.setColor(denseAppearance.mMainTickColor);
                else mTickPaint.setColor(forBlurColor);
            }
            for (mVars.tickIndex = 1/*0*/; mVars.tickIndex < 60; mVars.tickIndex++) {
                if (mVars.tickIndex % 5 == 0) continue;
                boolean tzCondition;
                if (AppPreferences.TZ_HEMISPHERE_UPPER == mAppPreferences.getTzHemisphere()) {
                    tzCondition = mVars.tickIndex > 50 && mVars.tickIndex < 55 || mVars.tickIndex > 5 && mVars.tickIndex < 10;
                } else {
                    tzCondition = mVars.tickIndex > 25 && mVars.tickIndex < 35;
                }
                if (tzCondition) {
                    mVars.tickRot = (float) (mVars.tickIndex * Math.PI * 2 / 60);
                    mVars.innerX = (float) Math.sin(mVars.tickRot) * (mVars.innerTickRadius - mVars.pixelDim(5f));
                    mVars.innerY = (float) -Math.cos(mVars.tickRot) * (mVars.innerTickRadius - mVars.pixelDim(5f));
                    mVars.outerX = (float) Math.sin(mVars.tickRot) * (mVars.innerTickRadius - mVars.pixelDim(3f));
                    mVars.outerY = (float) -Math.cos(mVars.tickRot) * (mVars.innerTickRadius - mVars.pixelDim(3f));
                    canvas.drawLine(mVars.centerX + mVars.innerX, mVars.centerY + mVars.innerY,
                            mVars.centerX + mVars.outerX, mVars.centerY + mVars.outerY, mTickPaint);
                } else {
                    mVars.tickRot = (float) (mVars.tickIndex * Math.PI * 2 / 60);
                    mVars.innerX = (float) Math.sin(mVars.tickRot) * mVars.innerTickRadius;
                    mVars.innerY = (float) -Math.cos(mVars.tickRot) * mVars.innerTickRadius;
                    mVars.outerX = (float) Math.sin(mVars.tickRot) * (mVars.outerTickRadius - mVars.pixelDim(1.5f));
                    mVars.outerY = (float) -Math.cos(mVars.tickRot) * (mVars.outerTickRadius - mVars.pixelDim(1.5f));
                    canvas.drawLine(mVars.centerX + mVars.innerX, mVars.centerY + mVars.innerY,
                            mVars.centerX + mVars.outerX, mVars.centerY + mVars.outerY, mTickPaint);
                }
            }

            //if (true) return;

            // Draw minute/second digits
//            float tickCenterRadius = (mVars.outerTickRadius - (mVars.outerTickRadius - mVars.innerTickRadius) / 2f) * 0.993f;
            //Log.i(TAG, "((((( centerRadius=" + centerRadius + ", k=" + (centerRadius) / (centerRadius+1f));
            mVars.hrMatrix.reset();
            //Path mPathInner = new Path(), mPathOuter = new Path();
            mVars.mPathInner.reset();
            mVars.mPathOuter.reset();
            mVars.mPathInner.addCircle(mVars.centerX, mVars.centerY, mVars.tickCenterRadius, Path.Direction.CW);
            mVars.hrMatrix.setRotate(-96.f, mVars.centerX, mVars.centerY);
            mVars.mPathInner.transform(mVars.hrMatrix);
            mVars.mPathOuter.addCircle(mVars.centerX, mVars.centerY, mVars.tickCenterRadius, Path.Direction.CCW);
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setRotate(-84.f, mVars.centerX, mVars.centerY);
            mVars.mPathOuter.transform(mVars.hrMatrix);
            mTickDigitPaint.setStyle(Paint.Style.FILL_AND_STROKE/*FILL*//*FILL_AND_STROKE*//*STROKE*/);
            /*canvas.drawPath(mPathInner, mTickDigitPaint);*/
            /*canvas.drawPath(mPathOuter, mTickDigitPaint);*/
            mTickDigitPaint.setTextSize(mVars.mDigitTextSize);
            mTickDigitPaint.setAntiAlias(true);
            mTickDigitPaint.setStrokeWidth(mVars.pixelDim(1f));
            mVars.corrCount = 4;
            if (ambient) {
                mTickDigitPaint.setColor(denseAppearance.mAmbientTickDigitColor);
            } else {
                if (!forBlur) mTickDigitPaint.setColor(denseAppearance.mMainTickDigitColor);
                else mTickDigitPaint.setColor(forBlurColor);
            }
            for (mVars.tickIndex = 0; mVars.tickIndex < 12; mVars.tickIndex++) {
                //float digitPos;
                //int corrIndex;
                String firstDigit, secondDigit;

                if (mVars.tickIndex > 3 && mVars.tickIndex < 9) {
                    mVars.corrIndex = mVars.tickIndex + mVars.corrCount;
                    mVars.corrCount -= 2;
                } else mVars.corrIndex = mVars.tickIndex;

                if (mVars.corrIndex/*tickIndex*/ % 2 == 0) secondDigit = "0";
                else secondDigit = "5";
                if (mVars.corrIndex/*tickIndex*/ == 0) firstDigit = "6";
                else if (mVars.corrIndex/*tickIndex*/ == 1) firstDigit = "0";
                else firstDigit = String.valueOf(mVars.tickIndex/*corrIndex*//*tickIndex*/ / 2);

                mTickDigitPaint.getTextBounds(firstDigit, 0, 1, mVars.textBounds);
                float fw = mVars.textBounds.width();
                mTickDigitPaint.getTextBounds(secondDigit, 0, 1, mVars.textBounds);
                float sw = mVars.textBounds.width();
                float offsetY = -(mVars.textBounds.top / 2f);
                float a, a1, a2, b, m1, m2;
                b = fw + sw;

                a2 = (float) Math.PI * mVars.tickCenterRadius * (((float) mVars.corrIndex * 30.f) + 12f) / 180.f;
                a1 = (float) Math.PI * mVars.tickCenterRadius * (((float) mVars.corrIndex * 30.f) - 0f) / 180.f;
                a = a2 - a1;
                m1 = (a - b) / 2;
                m2 = m1 + fw;

                if (mVars.tickIndex > 3 && mVars.tickIndex < 9) {
                    mVars.digitPos = a1 + m1;
                    canvas.drawTextOnPath(firstDigit, mVars.mPathOuter, mVars.digitPos, offsetY - mVars.pixelDim(0.5f), mTickDigitPaint);
                    mVars.digitPos = a1 + m2;
                    canvas.drawTextOnPath(secondDigit, mVars.mPathOuter, mVars.digitPos, offsetY - mVars.pixelDim(0.5f), mTickDigitPaint);
                } else {
//                    2: 10, k2=0.988264
//                    3: 15, k2=0.9920424
//                    9: 45, k4=0.9986502
//                    10: 50, k1=0.9993848
//                    11: 55, k1=0.9994405
//                    10: 50, k3=1.00061
//                    11: 55, k3=1.0005552
                    mVars.digitPos = a1 + m1;
                    if (mVars.corrIndex == 10 || mVars.corrIndex == 11) {
                        //mVars.digitPos -= 0.5f; // 50 and 55
                        mVars.digitPos *= 0.99941f; // 50 and 55
                        //Log.i(TAG, "((((( " + mVars.tickIndex + ": " + firstDigit + secondDigit + ", " + "k1=" + mVars.digitPos / (a1 + m1));
                    }
                    if (mVars.corrIndex > 1 && mVars.corrIndex < 4) {
                        //mVars.digitPos -= 2f; // 10 and 15
                        mVars.digitPos *= 0.99f; // 10 and 15
                        //Log.i(TAG, "((((( " + mVars.tickIndex + ": " + firstDigit + secondDigit + ", " + "k2=" + mVars.digitPos / (a1 + m1));
                    }
                    canvas.drawTextOnPath(firstDigit, mVars.mPathInner, mVars.digitPos, offsetY, mTickDigitPaint);
                    mVars.digitPos = a1 + m2;
                    if (mVars.corrIndex == 10 || mVars.corrIndex == 11) {
                        //mVars.digitPos += 0.5f; // 50 and 55
                        mVars.digitPos *= 1.00055f; // 50 and 55
                        //Log.i(TAG, "((((( " + mVars.tickIndex + ": " + firstDigit + secondDigit + ", " + "k3=" + mVars.digitPos / (a1 + m2));
                    }
                    if (mVars.corrIndex == 9) {
                        //mVars.digitPos -= 1; // 45
                        mVars.digitPos *= 0.9986f; // 45
                        //Log.i(TAG, "((((( " + mVars.tickIndex + ": " + firstDigit + secondDigit + ", " + "k4=" + mVars.digitPos / (a1 + m2));
                    }
                    canvas.drawTextOnPath(secondDigit, mVars.mPathInner, mVars.digitPos, offsetY, mTickDigitPaint);
                }
            }

            // correct paths rotation for later using in "drawTzInfo()"
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setRotate(6.f, mVars.centerX, mVars.centerY);
            mVars.mPathInner.transform(mVars.hrMatrix);
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setRotate(174.f, mVars.centerX, mVars.centerY);
            mVars.mPathOuter.transform(mVars.hrMatrix);

        } // drawOuterTicksAndDigits



        // mVars.mPathInner=CW, mVars.mPathOuter=CCW, mVars.tickCenterRadius
        //canvas.drawCircle(cx, cy, radius, paint);
        //mTzLabelPaint.measureText()
        //mVars.mDigitTextSize
        //
        private void drawTzInfoORG(Canvas canvas, boolean ambient) {
            String textTzLabel = wTime.getEffectiveTzLabel();
            int tzOffset = wTime.getEffectiveTzOffset();
            boolean isDstActive = wTime.isDstActiveInEffectiveTz();
            boolean isDeviceTz = wTime.isDeviceTz();
            boolean isUtcTz = wTime.isUtcTz();

            float signHalfDim = ((mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(7f)) / 2f;
            Path signPath;// = new Path();
            Path plusSignPath = new Path();
            Path minusSignPath = new Path();
            minusSignPath.moveTo(mVars.centerX - signHalfDim, mVars.centerY);
            minusSignPath.lineTo(mVars.centerX + signHalfDim, mVars.centerY);
            plusSignPath.moveTo(mVars.centerX, mVars.centerY + signHalfDim);
            plusSignPath.lineTo(mVars.centerX, mVars.centerY - signHalfDim);
            plusSignPath.addPath(minusSignPath);
            //
//            String textSign = "\u2063"; // invisible separator
//            if (tzOffset > 0) textSign = "\uFE62";//"+";//"\u2295"; //"+";
//            if (tzOffset < 0) textSign = "\u002D";//"-";//"\u2296"; //"-";
            //
            int hourOffs = Math.abs(tzOffset) / 3600000;
            int minuteOffs = Math.abs(tzOffset) / 60000;
            int nMinutes = minuteOffs - hourOffs * 60;
            String textTzOffset = String.format("%02d%02d", hourOffs, nMinutes);

            float strokeWidth = 1.f;
            mTzLabelPaint.setTextSize(mVars.mDigitTextSize); // - strokeWidth
            mTzLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTzLabelPaint.setStrokeWidth(strokeWidth);
            mTzDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTzDotPaint.setStrokeWidth(0f);
            mTzLabelPaint.setSubpixelText(true);
            mTzLabelPaint.setStrokeCap(Paint.Cap.SQUARE);
            if (!ambient) {
                mTzDotPaint.setColor(denseAppearance.mMainTzCircles);
                mTzLabelPaint.setColor(denseAppearance.mMainTzScripts);
            } else {
                mTzDotPaint.setColor(denseAppearance.mAmbientTzCircles);
                mTzLabelPaint.setColor(denseAppearance.mAmbientTzScripts);
            }

            //Rect rect = new Rect();
//            mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), rect);
//            float textWidth = mTzLabelPaint.measureText(textTzOffset); //rect.width();// + rect.left;
//            float textHeight = -(rect.top); // + strokeWidth * 2f;//rect.height() + strokeWidth;// + rect.bottom;
//            float indRadius = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) / 2f - mVars.pixelDim(1.5f);
//            float grpLen = indRadius * 2f + mVars.pixelDim(2f) + textWidth;
//            float ang = (float) ((2f * Math.PI) / 60f) * 34f;
//            float cX = (float) (mVars.centerX + Math.sin(ang) * mVars.tickCenterRadius);
//            float cY = (float) (mVars.centerY - Math.cos(ang) * mVars.tickCenterRadius);

            float lgthB, lgthI, lgthT;//, lgthS;
            float sizeL, sizeTw, sizeTh, sizeG, sizeI;//, sizeSw;
            float anglI;
            float cxI, cyI;
            float stroke;




            if (AppPreferences.TZ_HEMISPHERE_UPPER == mAppPreferences.getTzHemisphere()) {
                // tz label in upper hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 21f);
                sizeTw = mTzLabelPaint.measureText(textTzLabel);
                mTzLabelPaint.getTextBounds(textTzLabel, 0, textTzLabel.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f); //1.5f
                sizeG = mVars.pixelDim(5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = lgthI / mVars.tickCenterRadius;
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                if (!isDeviceTz) {
                    if (isUtcTz) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                    } else {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzDotPaint.getColor();
                        mTzDotPaint.setColor(ambient ? denseAppearance.mAmbientTzPoint : denseAppearance.mMainTzPoint);
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                        mTzDotPaint.setColor(color);
                    }
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathInner, lgthT,
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathInner, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                }
                //

                // tz offset in upper hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 3f);
                sizeTw = mTzLabelPaint.measureText(textTzOffset);
                mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(1f); //1.5f
                sizeG = mVars.pixelDim(2.5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = lgthI / mVars.tickCenterRadius;
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                //sizeSw = mTzLabelPaint.measureText(textSign);
                //lgthS = lgthI - (sizeSw / 2f);
                if (0 != tzOffset) {
                    if (tzOffset > 0) signPath = new Path(plusSignPath);
                    else signPath = new Path(minusSignPath);
                    mVars.hrMatrix.reset();
                    mVars.hrMatrix.setTranslate(cxI - mVars.centerX, cyI - mVars.centerY);
                    mVars.hrMatrix.postRotate(RAD2DEG * anglI, cxI, cyI);
                    signPath.transform(mVars.hrMatrix);
                    stroke = mTzLabelPaint.getStrokeWidth();
                    mTzLabelPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                    if (isDstActive) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzLabelPaint.getColor();
                        mTzLabelPaint.setColor(ambient ? denseAppearance.mAmbientTzSign : denseAppearance.mMainTzSign);
                        //canvas.drawTextOnPath(textSign, mVars.mPathInner, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                        if (0 != tzOffset) canvas.drawPath(signPath, mTzLabelPaint);
                        mTzLabelPaint.setColor(color);
                    } else {
                        //canvas.drawTextOnPath(textSign, mVars.mPathInner, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                        if (0 != tzOffset) canvas.drawPath(signPath, mTzLabelPaint);
                    }
                    mTzLabelPaint.setStrokeWidth(stroke);
                    //
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathInner, lgthT,
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathInner, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                }
            } else {
                // tz label in lower hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 23f);
                sizeTw = mTzLabelPaint.measureText(textTzLabel);
                mTzLabelPaint.getTextBounds(textTzLabel, 0, textTzLabel.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f);
                sizeG = mVars.pixelDim(5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = (float) (Math.PI - (lgthI / mVars.tickCenterRadius));
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                if (!isDeviceTz) {
                    if (isUtcTz) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                    } else {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzDotPaint.getColor();
                        mTzDotPaint.setColor(ambient ? denseAppearance.mAmbientTzPoint : denseAppearance.mMainTzPoint);
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                        mTzDotPaint.setColor(color);
                    }
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathOuter, lgthT,
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathOuter, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                }

                // tz offset in lower hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f));
                sizeTw = mTzLabelPaint.measureText(textTzOffset);
                mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f);
                sizeG = mVars.pixelDim(2.5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = (float) (Math.PI - (lgthI / mVars.tickCenterRadius));
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                //sizeSw = mTzLabelPaint.measureText(textSign);
                //lgthS = lgthI - (sizeSw / 2f);
                if (0 != tzOffset) {
                    if (tzOffset > 0) signPath = new Path(plusSignPath);
                    else signPath = new Path(minusSignPath);
                    mVars.hrMatrix.reset();
                    mVars.hrMatrix.setTranslate(cxI - mVars.centerX, cyI - mVars.centerY);
                    mVars.hrMatrix.postRotate(RAD2DEG * anglI, cxI, cyI);
                    signPath.transform(mVars.hrMatrix);
                    stroke = mTzLabelPaint.getStrokeWidth();
                    mTzLabelPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                    if (isDstActive) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzLabelPaint.getColor();
                        mTzLabelPaint.setColor(ambient ? denseAppearance.mAmbientTzSign : denseAppearance.mMainTzSign);
                        //canvas.drawTextOnPath(textSign, mVars.mPathOuter, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                        if (0 != tzOffset) canvas.drawPath(signPath, mTzLabelPaint);
                        mTzLabelPaint.setColor(color);
                    } else {
                        //canvas.drawTextOnPath(textSign, mVars.mPathOuter, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                        if (0 != tzOffset) canvas.drawPath(signPath, mTzLabelPaint);
                    }
                    mTzLabelPaint.setStrokeWidth(stroke);
                    //
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathOuter, lgthT,
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathOuter, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                }
            }
        } // drawTzInfo
        //
        private void drawTzInfo(Canvas canvas, boolean ambient) {
            String textTzLabel = wTime.getEffectiveTzLabel();
            int tzOffset = wTime.getEffectiveTzOffset();
            boolean isDstActive = wTime.isDstActiveInEffectiveTz();
            boolean isDeviceTz = wTime.isDeviceTz();
            boolean isUtcTz = wTime.isUtcTz();

            float signHalfDim = ((mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(7f)) / 2f;
            Path signPath;// = new Path();
            Path plusSignPath = new Path();
            Path minusSignPath = new Path();
            minusSignPath.moveTo(mVars.centerX - signHalfDim, mVars.centerY);
            minusSignPath.lineTo(mVars.centerX + signHalfDim, mVars.centerY);
            plusSignPath.moveTo(mVars.centerX, mVars.centerY + signHalfDim);
            plusSignPath.lineTo(mVars.centerX, mVars.centerY - signHalfDim);
            plusSignPath.addPath(minusSignPath);
            Path noSignPath = new Path();
            noSignPath.addCircle(mVars.centerX, mVars.centerY, mVars.pixelDim(1.5f), Path.Direction.CW);
            //
//            String textSign = "\u2063"; // invisible separator
//            if (tzOffset > 0) textSign = "\uFE62";//"+";//"\u2295"; //"+";
//            if (tzOffset < 0) textSign = "\u002D";//"-";//"\u2296"; //"-";
            //
            int hourOffs = Math.abs(tzOffset) / 3600000;
            int minuteOffs = Math.abs(tzOffset) / 60000;
            int nMinutes = minuteOffs - hourOffs * 60;
            String textTzOffset = String.format("%02d%02d", hourOffs, nMinutes);

            float strokeWidth = 1.f;
            mTzLabelPaint.setTextSize(mVars.mDigitTextSize); // - strokeWidth
            mTzLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTzLabelPaint.setStrokeWidth(strokeWidth);
            mTzDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTzDotPaint.setStrokeWidth(0f);
            mTzLabelPaint.setSubpixelText(true);
            mTzLabelPaint.setStrokeCap(Paint.Cap.SQUARE);
            if (!ambient) {
                mTzDotPaint.setColor(denseAppearance.mMainTzCircles);
                mTzLabelPaint.setColor(denseAppearance.mMainTzScripts);
            } else {
                mTzDotPaint.setColor(denseAppearance.mAmbientTzCircles);
                mTzLabelPaint.setColor(denseAppearance.mAmbientTzScripts);
            }

            //Rect rect = new Rect();
//            mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), rect);
//            float textWidth = mTzLabelPaint.measureText(textTzOffset); //rect.width();// + rect.left;
//            float textHeight = -(rect.top); // + strokeWidth * 2f;//rect.height() + strokeWidth;// + rect.bottom;
//            float indRadius = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) / 2f - mVars.pixelDim(1.5f);
//            float grpLen = indRadius * 2f + mVars.pixelDim(2f) + textWidth;
//            float ang = (float) ((2f * Math.PI) / 60f) * 34f;
//            float cX = (float) (mVars.centerX + Math.sin(ang) * mVars.tickCenterRadius);
//            float cY = (float) (mVars.centerY - Math.cos(ang) * mVars.tickCenterRadius);

            float lgthB, lgthI, lgthT;//, lgthS;
            float sizeL, sizeTw, sizeTh, sizeG, sizeI;//, sizeSw;
            float anglI;
            float cxI, cyI;
            float stroke;
            boolean centeringTimeOffset = true;




            if (AppPreferences.TZ_HEMISPHERE_UPPER == mAppPreferences.getTzHemisphere()) {
                // tz label in upper hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 21f);
                sizeTw = mTzLabelPaint.measureText(textTzLabel);
                mTzLabelPaint.getTextBounds(textTzLabel, 0, textTzLabel.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f); //1.5f
                sizeG = mVars.pixelDim(5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = lgthI / mVars.tickCenterRadius;
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                if (!isDeviceTz) {
                    if (isUtcTz) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                    } else {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzDotPaint.getColor();
                        mTzDotPaint.setColor(ambient ? denseAppearance.mAmbientTzPoint : denseAppearance.mMainTzPoint);
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                        mTzDotPaint.setColor(color);
                    }
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathInner, lgthT,
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathInner, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                }
                //

                // tz offset in upper hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 3f);
                sizeTw = mTzLabelPaint.measureText(textTzOffset);
                mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(1f); //1.5f
                sizeG = mVars.pixelDim(2.5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = lgthI / mVars.tickCenterRadius;
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                //sizeSw = mTzLabelPaint.measureText(textSign);
                //lgthS = lgthI - (sizeSw / 2f);
//                if (0 != tzOffset) {
//                } else {
//                    canvas.drawTextOnPath(textTzOffset, mVars.mPathInner, lgthB - (sizeTw / 2f),
//                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
//                }
                if (tzOffset > 0) signPath = new Path(plusSignPath);
                else if (tzOffset < 0) signPath = new Path(minusSignPath);
                else signPath = noSignPath;
                mVars.hrMatrix.reset();
                mVars.hrMatrix.setTranslate(cxI - mVars.centerX, cyI - mVars.centerY);
                mVars.hrMatrix.postRotate(RAD2DEG * anglI, cxI, cyI);
                signPath.transform(mVars.hrMatrix);
                stroke = mTzLabelPaint.getStrokeWidth();
                mTzLabelPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                if (isDstActive) {
                    centeringTimeOffset = false;
                    canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                    int color = mTzLabelPaint.getColor();
                    mTzLabelPaint.setColor(ambient ? denseAppearance.mAmbientTzSign : denseAppearance.mMainTzSign);
                    //canvas.drawTextOnPath(textSign, mVars.mPathInner, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                    /*if (0 != tzOffset)*/ canvas.drawPath(signPath, mTzLabelPaint);
                    mTzLabelPaint.setColor(color);
                } else {
                    //canvas.drawTextOnPath(textSign, mVars.mPathInner, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                    if (0 != tzOffset) {
                        centeringTimeOffset = false;
                        canvas.drawPath(signPath, mTzLabelPaint);
                    }
                }
                mTzLabelPaint.setStrokeWidth(stroke);
                //
                if (centeringTimeOffset) {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathInner, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathInner, lgthT,
                            (sizeTh / 2f) + mVars.pixelDim(0.5f), mTzLabelPaint);
                }
            } else {
                // tz label in lower hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f) * 23f);
                sizeTw = mTzLabelPaint.measureText(textTzLabel);
                mTzLabelPaint.getTextBounds(textTzLabel, 0, textTzLabel.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f);
                sizeG = mVars.pixelDim(5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = (float) (Math.PI - (lgthI / mVars.tickCenterRadius));
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                if (!isDeviceTz) {
                    if (isUtcTz) {
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                    } else {
                        canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                        int color = mTzDotPaint.getColor();
                        mTzDotPaint.setColor(ambient ? denseAppearance.mAmbientTzPoint : denseAppearance.mMainTzPoint);
                        canvas.drawCircle(cxI, cyI, (sizeI / 4f), mTzDotPaint);
                        mTzDotPaint.setColor(color);
                    }
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathOuter, lgthT,
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzLabel, mVars.mPathOuter, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                }

                // tz offset in lower hemisphere
                lgthB = (float) (mVars.tickCenterRadius * (Math.PI / 12f));
                sizeTw = mTzLabelPaint.measureText(textTzOffset);
                mTzLabelPaint.getTextBounds(textTzOffset, 0, textTzOffset.length(), mVars.rect);
                sizeTh = -(mVars.rect.top);
                sizeI = (mVars.mDigitsRadiusPathOuter - mVars.mDigitsRadiusPathInner) - mVars.pixelDim(0.5f);
                sizeG = mVars.pixelDim(2.5f);
                sizeL = sizeI + sizeG + sizeTw;
                lgthI = lgthB - (sizeL / 2f) + (sizeI / 2f);
                anglI = (float) (Math.PI - (lgthI / mVars.tickCenterRadius));
                cxI = (float) (mVars.centerX + Math.sin(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                cyI = (float) (mVars.centerY - Math.cos(anglI) * (mVars.tickCenterRadius - mVars.pixelDim(1f)));
                lgthT = lgthI + (sizeI / 2f) + sizeG;
                //
                //sizeSw = mTzLabelPaint.measureText(textSign);
                //lgthS = lgthI - (sizeSw / 2f);
//                if (0 != tzOffset) {
//                } else {
//                    canvas.drawTextOnPath(textTzOffset, mVars.mPathOuter, lgthB - (sizeTw / 2f),
//                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
//                }
                if (tzOffset > 0) signPath = new Path(plusSignPath);
                else if (tzOffset < 0) signPath = new Path(minusSignPath);
                else signPath = noSignPath;
                mVars.hrMatrix.reset();
                mVars.hrMatrix.setTranslate(cxI - mVars.centerX, cyI - mVars.centerY);
                mVars.hrMatrix.postRotate(RAD2DEG * anglI, cxI, cyI);
                signPath.transform(mVars.hrMatrix);
                stroke = mTzLabelPaint.getStrokeWidth();
                mTzLabelPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                if (isDstActive) {
                    centeringTimeOffset = false;
                    canvas.drawCircle(cxI, cyI, (sizeI / 2f), mTzDotPaint);
                    int color = mTzLabelPaint.getColor();
                    mTzLabelPaint.setColor(ambient ? denseAppearance.mAmbientTzSign : denseAppearance.mMainTzSign);
                    //canvas.drawTextOnPath(textSign, mVars.mPathOuter, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                    /*if (0 != tzOffset)*/ canvas.drawPath(signPath, mTzLabelPaint);
                    mTzLabelPaint.setColor(color);
                } else {
                    //canvas.drawTextOnPath(textSign, mVars.mPathOuter, lgthS, (sizeTh / 2f) - mVars.pixelDim(1f), mTzLabelPaint);
                    if (0 != tzOffset) {
                        centeringTimeOffset = false;
                        canvas.drawPath(signPath, mTzLabelPaint);
                    }
                }
                mTzLabelPaint.setStrokeWidth(stroke);
                //
                if (centeringTimeOffset) {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathOuter, lgthB - (sizeTw / 2f),
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                } else {
                    canvas.drawTextOnPath(textTzOffset, mVars.mPathOuter, lgthT,
                            (sizeTh / 2f) - mVars.pixelDim(0.5f), mTzLabelPaint);
                }
            }
        } // drawTzInfo




        //        private void clearDigitTails(WatchAuxPosition auxPosition, Canvas canvas, Paint paint) {
//            paint.setColor(currentAppearance.mMainBackgroundColor);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(30f);
//            paint.setStrokeCap(Paint.Cap.BUTT);
//            float offset = mDigitsRadiusPathInner - 18f; //mDigitsRadiusPathInner - 18f; 25f
//            if (auxPosition == watchAuxPositions[7] || auxPosition == watchAuxPositions[9]) {
//                // цифра 8
//                canvas.drawArc(mVars.centerX - offset, mVars.centerY - offset,
//                        mVars.centerX + offset, mVars.centerY + offset, 135f, 30f, false, paint);
//            }
//            if (auxPosition == watchAuxPositions[8] || auxPosition == watchAuxPositions[10]) {
//                // цифра 4
//                canvas.drawArc(mVars.centerX - offset, mVars.centerY - offset,
//                        mVars.centerX + offset, mVars.centerY + offset, 15f, 30f, false, paint);
//            }
//        } // clearDigitTails
        private void clearDigitTails(boolean ambient, WatchAuxPosition auxPosition, Canvas canvas, Paint paint) {
//            if (currentAppearance.mShowDialGradient == false) {
//                paint.setColor(currentAppearance.mMainBackgroundColor);
//            } else {
            if (ambient) {
                paint.setColor(Color.BLACK);
            } else {
                Shader bmpShader = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                paint.setShader(bmpShader);
            }
//            }
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(31f);
            paint.setStrokeCap(Paint.Cap.BUTT);
            float offset = mVars.mDigitsRadiusPathInner - 18f; //mDigitsRadiusPathInner - 18f; 25f
            if (auxPosition == watchAuxPositions[5] || auxPosition == watchAuxPositions[7] || auxPosition == watchAuxPositions[9]) {
                // цифра 8
                canvas.drawArc(mVars.centerX - offset, mVars.centerY - offset,
                        mVars.centerX + offset, mVars.centerY + offset, 135f, 30f, false, paint);
            }
            if (auxPosition == watchAuxPositions[6] ||auxPosition == watchAuxPositions[8] || auxPosition == watchAuxPositions[10]) {
                // цифра 4
                canvas.drawArc(mVars.centerX - offset, mVars.centerY - offset,
                        mVars.centerX + offset, mVars.centerY + offset, 15f, 30f, false, paint);
            }
//            if (currentAppearance.mShowDialGradient == true) {
            if (!ambient) {
                paint.setShader(null);
            }
//            }
        } // clearDigitTails


        private void colorizeBurnInRing(Canvas canvas) {

            if (!denseAppearance.mColorizeBurnInMargin) return;

            Paint paint = new Paint();
            Path path = new Path();

            float strokeWidth;// = mVars.pixelDim(WatchAppearance.RECOMMENDED_BURNIN_MARGIN * 2f + 2f);
            float pathRadius;

            //if (!(strokeWidth > 0)) return;

            pathRadius = mVars.mScreenRadius - mVars.mBurnInMargin / 2f - 2f;
            strokeWidth = mVars.mBurnInMargin + 1f;//(mVars.mScreenRadius - mVars.mMainRadius);

            //mCircleGradientS
            Shader shdBitmap = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            path.addCircle(mVars.centerX, mVars.centerY, pathRadius, Path.Direction.CW);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setShader(shdBitmap);
            paint.setAntiAlias(true);
            if (true) canvas.drawPath(path, paint);
        }


        private void drawMainDialBitmap(Canvas canvas, boolean ambient) {

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);

            // Correct background index (for day of moth on 3 or on 6 o'clock)
            if (watchLayouts[denseAppearance.watchLayoutIndex].isVertical) {
                denseAppearance.watchBackgroundIndex = denseAppearance.watchDomIndex;
            } else {
                denseAppearance.watchBackgroundIndex = denseAppearance.watchDomIndex + ACommon.NUM_DOM_VARIANTS;
            }
            // Draw the background
            //if (watchBackgroundsBmp[currentAppearance.watchBackgroundIndex].mBackgroundBlack == null) createBackgroundSM(mVars.width, mVars.height);
            if (ambient) mVars.backgroundBitmap = watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mBackgroundBlack;
            else mVars.backgroundBitmap = watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mBackgroundColorized;
            //
            mVars.hrMatrix.reset();
            if (!ambient) {
                //mVars.hrMatrix.setTranslate(-ACommon.TRIANGLE_BMP_SCALED_DIM/2, -1f); // triangle original bitmap is 40x40
                mVars.hrMatrix.postTranslate(
                        mVars.pixelDim( ((mPrjP / mShdStickHeight) * 2f) ),
                        mVars.pixelDim( ((mPrjR / mShdStickHeight) * 2f) )
                );
                if (true) {
                    mVars.offsetMatrix.set(mVars.hrMatrix);
                    mVars.offsetMatrix.setTranslate(0f, mVars.pixelDim(2f));
                    canvas.drawBitmap(watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mDomShadowS, mVars.offsetMatrix, paint);
                }
            } else {
                if (true) canvas.drawBitmap(watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mDomShadowS, mVars.hrMatrix, paint);
            }
            //
            if (watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mBckgrShadowS != null) {
                if (denseAppearance.isShowHrDigitsRelief() == true) {
                    if (true)
                        canvas.drawBitmap(watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mBckgrShadowS, 0, 0, paint);
                }
            }
            //
            if (!ambient && mVars.mBurnInMargin > 0f) {
                if (true) colorizeBurnInRing(canvas);
            }
            //
            if (true) canvas.drawBitmap(mVars.backgroundBitmap, 0, 0, paint);
            //
            // затереть цифры 4 и/или 8, если нужно
            if (null != watchLayouts[denseAppearance.watchLayoutIndex].auxB &&
                    watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB].trigger == true) {
                if (true) clearDigitTails(ambient, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB], canvas, paint);
            }
            if (null != watchLayouts[denseAppearance.watchLayoutIndex].auxC &&
                    watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC].trigger == true) {
                if (true) clearDigitTails(ambient, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC],
                        canvas, paint); // mBackgroundPaint
            }


            //if (true) return;




            // draw frame around date cut hole
            Shader shdBitmap, shdColor, shdSumm;
            Bitmap domFrameRelief = mDomRelief.drawRelief();
            if (!ambient) {

                //Bitmap relief = drawDomRelief();

                mDomRelief.getBitmap(domFrameRelief, denseAppearance.mMainDomFrameColor, 0.5f);

                shdBitmap = new BitmapShader(
                        //watchBackgroundsBmp[denseAppearance.watchBackgroundIndex].mBackgroundColorized,
                        domFrameRelief,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
                        denseAppearance.mMainDomFrameColor, denseAppearance.mMainDomFrameColor, Shader.TileMode.CLAMP);

                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
                mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mTickPaint.setStrokeWidth(mVars.pixelDim(3.5f));
                mTickPaint.setShader(shdSumm);
                mTickPaint.setAntiAlias(true);
                canvas.drawPath(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex], mTickPaint);
                mTickPaint.setShader(null);

//                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
//                        denseAppearance.mMainTickColor, denseAppearance.mMainTickColor, Shader.TileMode.CLAMP);
//                shdColor = new BitmapShader(relief, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
//                mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//                mTickPaint.setStrokeWidth(mVars.pixelDim(3.0f));
//                mTickPaint.setShader(shdSumm);
                mTickPaint.setAntiAlias(true);
                int alf = mTickPaint.getAlpha();
                mTickPaint.setAlpha(/*255*/denseAppearance.mHourMarksReliefStrength);
//                canvas.drawPath(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex], mTickPaint);
                canvas.drawBitmap(domFrameRelief, 0, 0, mTickPaint);
                mTickPaint.setAlpha(alf);

            } else {

                Bitmap domFrameClrBlr = mDomRelief.getBitmap(domFrameRelief, denseAppearance.mAmbientDomFrameColor, 0.5f);
                shdBitmap = new BitmapShader(domFrameClrBlr, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
                        denseAppearance.mAmbientDomFrameColor, denseAppearance.mAmbientDomFrameColor, Shader.TileMode.CLAMP);
                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
                mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mTickPaint.setStrokeWidth(mVars.pixelDim(5f));
                int saveA = mTickPaint.getAlpha();
                //mTickPaint.setAlpha(saveA / 2);
                mTickPaint.setShader(shdSumm);
                mTickPaint.setAntiAlias(true);
                canvas.drawPath(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex], mTickPaint);
                mTickPaint.setShader(null);
                //mTickPaint.setAlpha(saveA);

                shdBitmap = new BitmapShader(/*domFrameRelief*/domFrameClrBlr, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height,
                        denseAppearance.mAmbientDomFrameColor, denseAppearance.mAmbientDomFrameColor, Shader.TileMode.CLAMP);
                shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_ATOP);
                //
                mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mTickPaint.setStrokeWidth(mVars.pixelDim(2.5f));
                mTickPaint.setShader(shdSumm);
                mTickPaint.setAntiAlias(true);
                mTickPaint.setAlpha(saveA);
                canvas.drawPath(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex], mTickPaint);
                mTickPaint.setShader(null);
                //mTickPaint.setAlpha(saveA);

                mTickPaint.setStyle(Paint.Style.STROKE);
                mTickPaint.setStrokeWidth(mVars.pixelDim(1.5f));
                mTickPaint.setColor(denseAppearance.mAmbientDomFrameColor);
                canvas.drawPath(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex], mTickPaint);

            }



            // draw relief around hour marks
            if (!ambient) {
                if (true && denseAppearance.isShowHrDigitsRelief()) {
                    Bitmap hmRelief = mHmRelief.drawRelief();
                    int saveA = mDigitsPaint.getAlpha();
                    mDigitsPaint.setAlpha(denseAppearance.mHourMarksReliefStrength);
                    canvas.drawBitmap(hmRelief, 0f, 0f, mDigitsPaint);
                    mDigitsPaint.setAlpha(saveA);
                    if (0 != Color.alpha(denseAppearance.mMainHourMarkOutlineColor)) mHmRelief.outlineHourMarks(canvas);
                }
            }

        } // drawMainDialBitmap




        class DomReliefBuilder {
            private static final boolean L = true;

            Canvas canvas;
            Bitmap bitmap;
            Paint paint;
            int colorSide;
            int colorLight;
            int colorDark;
            float lightSourceAngle;
            Path pathOutside, pathInside;

            public DomReliefBuilder(int width, int height, float lightSourceAngle) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint = new Paint();
                paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                colorSide = Color.argb(255, 127, 127, 127); //denseAppearance.mMainTickColor;
                colorLight = Color.WHITE;
                colorDark = Color.BLACK;
                this.lightSourceAngle = lightSourceAngle;
//                pathOutside = new Path(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex]); //watchBackgroundIndex
//                pathOutside.setFillType(Path.FillType.INVERSE_EVEN_ODD);
//                pathInside = new Path(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex]); //watchBackgroundIndex
                if (L) {
                    Writer writer = new StringWriter(); (new Exception()).printStackTrace(new PrintWriter(writer)); String sTrace = writer.toString();
                    //Log.i(TAG, "DomReliefBuilder, watchBackgroundIndex = " + denseAppearance.watchBackgroundIndex);
                    //Log.i(TAG, "DomReliefBuilder, calling stack trace = " + sTrace);
                }
            }
            //watchDomIndex EVT_CHLAYOUT_DATE


            MonoSegmentProcessor segmentCallback = new MonoSegmentProcessor() {
                @Override
                //public void onSegment(int pathIndex, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
                public void onSegment(boolean emboss, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
//                    float tanDiff = Math.abs(end.pointTangent - start.pointTangent);
//                    float spaceDist = (float) Math.hypot(Math.abs(end.pointX - start.pointX), Math.abs(end.pointY - start.pointY));
//                    String logMsg = String.format(
//                            "sgm <%3.2f %3.2f> XY: %3.2f (%3.2f) = [%3.2f, %3.2f] - [%3.2f, %3.2f], Tan: {%3.2f, %3.2f}.",
//                            start.getPathDistance(), end.getPathDistance(),
//                            spaceDist, tanDiff,
//                            start.getPointX(), start.getPointY(),
//                            end.getPointX(), end.getPointY(),
//                            start.getPointTangent(), end.getPointTangent()
//                    );
//                    Log.i(TAG, "(((( DOM Contour " + logMsg);

                    Path segment = new Path();
                    if (pathMeasure.getSegment(start.getPathDistance(), end.getPathDistance(), segment, true)) {
//                        int startColor = getTangentColor(pathIndex, start.getPointTangent());
//                        int endColor = getTangentColor(pathIndex, end.getPointTangent());
//                        int startColor = getTangentColor(emboss, start.getPointTangent());
//                        int endColor = getTangentColor(emboss, end.getPointTangent());
                        int startColor = PathPoint.tangentColor(emboss, start.getPointTangent(), colorSide, colorLight, colorDark, lightSourceAngle);
                        int endColor = PathPoint.tangentColor(emboss, end.getPointTangent(), colorSide, colorLight, colorDark, lightSourceAngle);
                        Shader shader = new LinearGradient(
                                start.getPointX(), start.getPointY(), end.getPointX(), end.getPointY(),
                                startColor, endColor, Shader.TileMode.CLAMP
                        );
                        paint.setShader(shader);
                        paint.setStrokeWidth(mVars.pixelDim(4f));
                        canvas.drawPath(segment, paint);
                        paint.setShader(null);
                    }
                }
            };

            public void saveBitmap() {
                ACommon.bmpToPicturesDir(AWearFaceService.this, bitmap, "dom_", "_rlf");
            }

            public Bitmap getBitmap() { return bitmap; }

            public Bitmap getBitmap(Bitmap reliefBitmap, int color, float blurRadius) {

                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());;

                //Bitmap reliefBitmap = drawRelief();

                Bitmap result, workingBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas();
                canvas.setBitmap(workingBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                paint = new Paint();
                paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);

////                PorterDuffColorFilter colorFilter =
////                        new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
////                paint.setColorFilter(colorFilter);
//                Shader colorShader = new LinearGradient(0, 0, mVars.width, mVars.height, color, color, Shader.TileMode.CLAMP);
//                paint.setShader(colorShader);
//                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//                canvas.drawBitmap(reliefBitmap, 0, 0, paint);
//                ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_E1_out", timeStamp);

                Shader shdBitmap = new BitmapShader(reliefBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdColor = new LinearGradient(0f, 0f, mVars.width, mVars.height, color, color, Shader.TileMode.CLAMP);
                Shader shdSumm = new ComposeShader(shdBitmap, shdColor, PorterDuff.Mode.SRC_IN);
                //
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(mVars.pixelDim(5f));
                paint.setShader(shdSumm);
                paint.setAntiAlias(true);
                canvas.drawPath(pathInside, paint);

                result /*workingBitmap*/ = blur(workingBitmap, blurRadius);
                //ACommon.bmpToPicturesDir(AWearFaceService.this, result /*workingBitmap*/, "dom_", "_E2_out", timeStamp);
                workingBitmap.recycle(); workingBitmap = null; /*System.gc();*/


                return result /*workingBitmap*/;
            }


            public Bitmap drawRelief() {    // close to etalon !!!
                //ACommon.bmpToPicturesDir(AWearFaceService.this, );
                //              adb -s 412KPFX0143611 pull /sdcard/Pictures/1436943904146.png
                //adb -s 412KPFX0143611 shell ls -l /sdcard/Pictures/*.png
                //adb -s 412KPFX0143611 pull /sdcard/Pictures/
                //adb -s 412KPFX0143611 shell rm /sdcard/Pictures/*.png

                pathOutside = new Path(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex]); //watchBackgroundIndex
                pathOutside.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                pathInside = new Path(dialElements.mDateCutPathsScaled[denseAppearance.watchBackgroundIndex]); //watchBackgroundIndex

                if (L) {
                    Writer writer = new StringWriter(); (new Exception()).printStackTrace(new PrintWriter(writer)); String sTrace = writer.toString();
                    //Log.i(TAG, "DomReliefBuilder.drawRelief; watchBackgroundIndex = " + denseAppearance.watchBackgroundIndex);
                    //Log.i(TAG, "DomReliefBuilder.drawRelief; calling stack trace = " + sTrace);
                }

                //PathPoint.traversePath(0, 0, domOutside, mDomRelief.segmentCallback);
                PathPoint.traversePath(pathOutside, false, mDomRelief.segmentCallback);




                Bitmap reliefBitmap = this.getBitmap();
                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f)); /*System.gc();*/

                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());;

                canvas = new Canvas();
                paint = new Paint();
                paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);

                Bitmap maskBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(maskBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(0f);
                paint.setColor(Color.BLACK);
                canvas.drawPath(pathOutside, paint);
                //Bitmap maskAlpha = PixelUtil.alphaChannel(maskBitmap);

                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, maskBitmap, "dom_", "_mask", timeStamp);
                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, maskAlpha, "dom_", "_alpha", timeStamp);

                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
                paint.setShader(shdrOp);

                Bitmap workingBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workingBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(mVars.pixelDim(3f));
                canvas.drawPath(pathInside, paint);
                paint.setShader(null);

                Bitmap outputBitmap = blur(workingBitmap, mVars.pixelDim(1f)); /*System.gc();*/ //1f
                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, outputBitmap, "dom_", "_1_out", timeStamp);

//                canvas.setBitmap(workingBitmap);
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                Xfermode xfermode1 = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
//                canvas.drawBitmap(maskAlpha, 0, 0, null); // DST
//                paint.setXfermode(xfermode1);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                paint.setXfermode(null);
//                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, outputBitmap, "dom_", "_2_out", timeStamp);


                //
//                canvas.setBitmap(workingBitmap);
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                paint = new Paint();
//                paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);
//                PorterDuffColorFilter colorFilter =
//                        new PorterDuffColorFilter(denseAppearance.mMainTickColor, PorterDuff.Mode.MULTIPLY);
//                paint.setColorFilter(colorFilter);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_3_out", timeStamp);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_4_out", timeStamp);
                //



//            canvas.setBitmap(workingBitmap);
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            shdrRelief = new BitmapShader(outputBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            //shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
//            paint.setShader(shdrOp);
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//            paint.setStrokeWidth(mVars.pixelDim(3f));
//            canvas.drawPath(domInside, paint);
//            //
//            ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_2_rlf");
//
//            canvas.setBitmap(workingBitmap);
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            //shdrRelief = new BitmapShader(outputBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            //shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
//            paint.setShader(shdrMask);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(outputBitmap, 0, 0, paint);
//            //
//            ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_3_rlf");

                return outputBitmap;
            }
        }
        //
        DomReliefBuilder mDomRelief;
        //


        class HourMarkReliefBuilder {

            private static final boolean L = false;

            Canvas canvas;
            Bitmap bitmap;
            Paint paint;
            int colorSide;
            int colorLight;
            int colorDark;
            float lightSourceAngle;

            public HourMarkReliefBuilder(int width, int height, float lightSourceAngle) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint = new Paint();
                paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
                paint.setStyle(Paint.Style.STROKE);
                //paint.setStrokeWidth(3f);
                paint.setStrokeCap(Paint.Cap.ROUND);

                //float light[] = new float[3]; light[0] = 0f; light[1] = -1; light[2] = 1;
                //colorSide = Color.argb(127, 127, 127, 127); //denseAppearance.mMainBackgroundColor;
                colorSide = Color.argb(255, 127, 127, 127); //denseAppearance.mMainTickColor;
                colorLight = Color.WHITE;
                colorDark = Color.BLACK;
                this.lightSourceAngle = lightSourceAngle;
            }


            MonoSegmentProcessor segmentCallback = new MonoSegmentProcessor() {
                @Override
                //public void onSegment(int pathIndex, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
                public void onSegment(boolean emboss, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
//                    float tanDiff = Math.abs(end.pointTangent - start.pointTangent);
//                    float spaceDist = (float) Math.hypot(Math.abs(end.pointX - start.pointX), Math.abs(end.pointY - start.pointY));
//                    String logMsg = String.format(
//                            "sgm <%3.2f %3.2f> XY: %3.2f (%3.2f) = [%3.2f, %3.2f] - [%3.2f, %3.2f], Tan: {%3.2f, %3.2f}.",
//                            start.getPathDistance(), end.getPathDistance(),
//                            spaceDist, tanDiff,
//                            start.getPointX(), start.getPointY(),
//                            end.getPointX(), end.getPointY(),
//                            start.getPointTangent(), end.getPointTangent()
//                    );
//                    Log.i(TAG, "(((( Contour " + logMsg);

                    Path segment = new Path();
                    if (pathMeasure.getSegment(start.getPathDistance(), end.getPathDistance(), segment, true)) {
//                        int startColor = getTangentColor(pathIndex, start.getPointTangent());
//                        int endColor = getTangentColor(pathIndex, end.getPointTangent());
//                        int startColor = getTangentColor(emboss, start.getPointTangent());
//                        int endColor = getTangentColor(emboss, end.getPointTangent());
                        int startColor = PathPoint.tangentColor(emboss, start.getPointTangent(), colorSide, colorLight, colorDark, lightSourceAngle);
                        int endColor = PathPoint.tangentColor(emboss, end.getPointTangent(), colorSide, colorLight, colorDark, lightSourceAngle);
                        Shader shader = new LinearGradient(
                                start.getPointX(), start.getPointY(), end.getPointX(), end.getPointY(),
                                startColor, endColor, Shader.TileMode.CLAMP
                        );
                        paint.setShader(shader);
                        paint.setStrokeWidth(mVars.pixelDim(3f));
                        canvas.drawPath(segment, paint);
                        paint.setShader(null);
                    }
                }
            };

            public void saveBitmap() {
                ACommon.bmpToPicturesDir(AWearFaceService.this, bitmap);
            }

            public Bitmap getBitmap() { return bitmap; }


            public void outlineHourMarks(Canvas canvas) {
                //Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1f);
                paint.setColor(denseAppearance.mMainHourMarkOutlineColor);


                for (int i=0; i < dialElements.mHourMarkPathsScaled.length; i++) {
                    if (null == dialElements.mHourMarkPathsScaled[i]) continue;
                    if (watchLayouts[denseAppearance.watchLayoutIndex].isVertical) { if (i == 3) continue;
                    } else { if (i == 6) continue; }
                    Integer ind; // 5,7,9 - over 8; 6,8,10 - over 4
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxB;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxC;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }

                    canvas.drawPath(dialElements.mHourMarkPathsScaled[i], paint);

                }

            }


            public void traversePath() {
                for (int i=0; i < dialElements.mHourMarkPathsScaled.length; i++) {
                    if (null == dialElements.mHourMarkPathsScaled[i]) continue;
                    if (watchLayouts[denseAppearance.watchLayoutIndex].isVertical) { if (i == 3) continue;
                    } else { if (i == 6) continue; }
                    Integer ind; // 5,7,9 - over 8; 6,8,10 - over 4
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxB;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxC;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }
                    Path marksOutside = new Path(dialElements.mHourMarkPathsScaled[i]);
                    marksOutside.setFillType(Path.FillType.INVERSE_EVEN_ODD);

                    if (WatchAppearance.HM_RELIEF_NONE != ((int) denseAppearance.mHourMarksReliefIndex[i])) {
                        boolean emboss;
                        if (WatchAppearance.HM_RELIEF_EMBOSS == ((int) denseAppearance.mHourMarksReliefIndex[i])) emboss = true;
                        else emboss = false;

                        //PathPoint.traversePath(i, (int) denseAppearance.mHourMarksIndex[i], marksOutside, mHmRelief.segmentCallback);
                        PathPoint.traversePath(marksOutside, emboss, mHmRelief.segmentCallback);
                    }
                }
            } // traversePath

            /*
            Paint redP = new Paint();
            redP.setShader(new BitmapShader(red, TileMode.CLAMP, TileMode.CLAMP));
            redP.setColorFilter(new PorterDuffColorFilter(Color.RED, Mode.MULTIPLY));
            redP.setXfermode(new PorterDuffXfermode(Mode.SCREEN));

            Paint greenP = new Paint();
            greenP.setShader(new BitmapShader(green, TileMode.CLAMP,TileMode.CLAMP));
            greenP.setColorFilter(new PorterDuffColorFilter(Color.GREEN,Mode.MULTIPLY));
            greenP.setXfermode(new PorterDuffXfermode(Mode.SCREEN));

            Paint blueP = new Paint();
            blueP.setShader(new BitmapShader(blue, TileMode.CLAMP, TileMode.CLAMP));
            blueP.setColorFilter(new PorterDuffColorFilter(Color.BLUE,Mode.MULTIPLY));
            blueP.setXfermode(new PorterDuffXfermode(Mode.SCREEN));

            Canvas c = new Canvas(result);
            c.drawRect(0, 0, width, height, redP);
            c.drawRect(0, 0, width, height, greenP);
            c.drawRect(0, 0, width, height, blueP);
            */


            private Bitmap drawVariant_1(Path pathInside, Path pathOutside) {
                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());;

                Bitmap reliefBitmap = this.getBitmap();
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, reliefBitmap, "hm_", "_00_rlf", timeStamp);
                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f));
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, reliefBitmap, "hm_", "_01_rlf", timeStamp);

                canvas = new Canvas();
                paint = new Paint(); paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);
                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

                Bitmap workingBmp = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workingBmp);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                canvas.save();
                canvas.clipPath(pathOutside);
                canvas.drawBitmap(reliefBitmap, 0f, 0f, null);
                canvas.restore();
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "hm_", "_1_rlf", timeStamp);

                paint.setShader(shdrRelief);
                paint.setStyle(Paint.Style.STROKE);
                //canvas = new Canvas();
                //canvas.setBitmap(workingBmp);
                paint.setStrokeWidth(mVars.pixelDim(1f));
                paint.setAlpha(160);
                canvas.drawPath(pathInside, paint);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "hm_", "_2_rlf", timeStamp);

                workingBmp = blur(workingBmp, 0.5f);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "hm_", "_3_rlf", timeStamp);
                canvas.setBitmap(workingBmp);
                paint.setShader(null);
                Xfermode xfr = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
                paint.setXfermode(xfr);
                canvas.drawBitmap(reliefBitmap, 0, 0, paint);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "hm_", "_4_rlf", timeStamp);


                return workingBmp;
            }



            private Bitmap drawVariant_0(Path pathInside, Path pathOutside) { // start etalon
                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());;

                Bitmap reliefBitmap = this.getBitmap();
                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f));
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, reliefBitmap, "hm_", "_0_rlf", timeStamp);

                paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                paint.setShader(shdrRelief);
                paint.setStyle(Paint.Style.STROKE);

                canvas = new Canvas();
                Bitmap bitmap3 = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap3);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                canvas.clipPath(pathOutside);
                canvas.drawBitmap(reliefBitmap, 0f, 0f, null);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, bitmap3, "hm_", "_1_rlf", timeStamp);

                canvas = new Canvas();
                canvas.setBitmap(bitmap3);
                paint.setStrokeWidth(mVars.pixelDim(1f));
                paint.setAlpha(127);
                canvas.drawPath(pathInside, paint);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, bitmap3, "hm_", "_2_rlf", timeStamp);

                return bitmap3;
            }


            public Bitmap drawRelief() {
//            Shader bmpShader = new BitmapShader(mCircleGradientNoTransparent, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//            paint.setShader(bmpShader);
                //ACommon.bmpToPicturesDir(AWearFaceService.this, );
                //adb -s 412KPFX0143611 shell ls -l /sdcard/Pictures/*.png
                //adb -s 412KPFX0143611 pull /sdcard/Pictures/1436943904146.png

                Path clipPath = new Path();
                for (int i=0; i < dialElements.mHourMarkPathsScaled.length; i++) {
                    if (null == dialElements.mHourMarkPathsScaled[i]) continue;
                    if (watchLayouts[denseAppearance.watchLayoutIndex].isVertical) { if (i == 3) continue;
                    } else { if (i == 6) continue; }
                    Integer ind; // 5,7,9 - over 8; 6,8,10 - over 4
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxB;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }
                    ind = watchLayouts[denseAppearance.watchLayoutIndex].auxC;
                    if (ind != null) {
                        if (i==8 && (ind==5 || ind==7 || ind==9)) continue; if (i==4 && (ind==6 || ind==8 || ind==10)) continue;
                    }
                    clipPath.addPath(dialElements.mHourMarkPathsScaled[i]);
                }
                Path pathInside = new Path(clipPath), pathOutside = new Path(clipPath);
                pathOutside.setFillType(Path.FillType.INVERSE_EVEN_ODD);

                this.traversePath();
                //mHmRelief.saveBitmap();


                //return drawVariant_0(pathInside, pathOutside);
                return drawVariant_1(pathInside, pathOutside);


//                Canvas canvas;
//                Paint paint;
//                String timeStamp = String.valueOf(System.currentTimeMillis());;
////            Bitmap bitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
////            canvas = new Canvas(bitmap);
////            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
////            paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
////            canvas.clipPath(digitsOutside);
////            drawPathBevelLights(canvas, 1f, digitsInside, null, 0xff000000, 0xffffffff, 0f);
////            bitmap = blur(bitmap, mVars.pixelDim(2f));
//
//                Bitmap reliefBitmap = this.getBitmap();
//                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f));
//
//                paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
//                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//                paint.setShader(shdrRelief);
//                paint.setStyle(Paint.Style.STROKE);
//
////                canvas = new Canvas();
////                Bitmap bitmap2 = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
////                //canvas.save();
////                canvas.setBitmap(bitmap2);
////                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
////                //canvas.clipPath(clipPath);
//////                paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
//////                Shader shader2 = new BitmapShader(reliefBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
////                //BlurMaskFilter blurMaskFilter = new BlurMaskFilter(3f, BlurMaskFilter.Blur.OUTER);
////                //paint.setMaskFilter(blurMaskFilter);
//////                paint.setStyle(Paint.Style.STROKE);
//////                paint.setShader(shader2);
////                paint.setStrokeWidth(mVars.pixelDim(2f));
////                for (int i=0; i < dialElements.mHourMarkPathsScaled.length; i++) {
////                    if (null == dialElements.mHourMarkPathsScaled[i]) continue;
////                    canvas.drawPath(dialElements.mHourMarkPathsScaled[i], paint);
////                }
////                bitmap2 = blur(bitmap2, mVars.pixelDim(3f));
//
//                canvas = new Canvas();
//                Bitmap bitmap3 = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
//                canvas.setBitmap(bitmap3);
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//                //clipPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
//                canvas.clipPath(pathOutside);
//                //canvas.drawBitmap(bitmap2, 0f, 0f, null);
//                canvas.drawBitmap(reliefBitmap, 0f, 0f, null);
//
//                canvas = new Canvas();
//                canvas.setBitmap(bitmap3);
//                paint.setStrokeWidth(mVars.pixelDim(1f));
//                paint.setAlpha(127);
//                if (false) {
//                    for (int i=0; i < dialElements.mHourMarkPathsScaled.length; i++) {
//                        if (null == dialElements.mHourMarkPathsScaled[i]) continue;
//                        canvas.drawPath(dialElements.mHourMarkPathsScaled[i], paint);
//                    }
//                } else {
//                    canvas.drawPath(pathInside, paint);
//                }
//
//                return bitmap3;
            }

        }
        //
        HourMarkReliefBuilder mHmRelief;


        private void drawFrame(Canvas canvas) {

            canvas.drawColor(Color.BLACK); // !!!!!!!

            // draw color for dial circle hour digits
            if (isInAmbientMode()) {
                mDigitsPaint.setColor(denseAppearance.mAmbientDigitsColor);
            } else {
                mDigitsPaint.setColor(denseAppearance.mMainDigitsColor);
            } if (true) canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathInner, mDigitsPaint);


            //if (!isInAmbientMode()) drawHourMarksRelief1(canvas);


            if (watchLayouts[denseAppearance.watchLayoutIndex].dayOfMonth != null) {
                if (true) drawDateTriple(canvas, denseAppearance.watchLayoutIndex, isInAmbientMode()); // mVars.now wTime.getRawNow(),
            }

//            drawMainDialBitmap(canvas, true, isInAmbientMode());
//            drawOuterTicksAndDigits(canvas);


            if (isInAmbientMode()) {
                canvas.drawBitmap(mAmbientPlateBitmap, 0, 0, null /*mBackgroundPaint*/);
            } else {
                canvas.drawBitmap(mDensePlateBitmap, 0, 0, null /*mBackgroundPaint*/);
            }


            //if (!isInAmbientMode()) drawHourMarksRelief2(canvas);


            //if (true) return;


            if (watchLayouts[denseAppearance.watchLayoutIndex].auxA != null) {
                drawWeekdayDial(canvas,
                        watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxA],
                        0f, 56f, false, true, isInAmbientMode());
            }


            drawTzInfo(canvas, isInAmbientMode());



//            // draw black circle around dial
//            mBackgroundPaint.setAntiAlias(true);
//            mBackgroundPaint.setStrokeWidth(mVars.mBurnInMargin +1f);
//            mBackgroundPaint.setStyle(Paint.Style.STROKE);
//            mBackgroundPaint.setARGB(255,0,0,0);
//            canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathOuter+mVars.mBurnInMargin /2f+1f, mBackgroundPaint);



//            mBackgroundPaint.setColor(0xffffffff);
//            mBackgroundPaint.setStyle(Paint.Style.FILL);
//            mBackgroundPaint.setTextSize(30f);
//            canvas.drawText(String.valueOf(mFramePerSecond),145f,85f,mBackgroundPaint);
//            if (true) { invalidate(); return; }



            //
            if (watchLayouts[denseAppearance.watchLayoutIndex].auxB != null) {
                drawBattDial(canvas, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB],
                        (float) Math.PI, 20f, 10f, 5f, false, true, isInAmbientMode());
            }


            //
            if (watchLayouts[denseAppearance.watchLayoutIndex].auxC != null) {
                drawMonthDial(canvas,
                        watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC],
                        0f, 0f, false, true, isInAmbientMode());
            }

            //
//            if (watchLayouts[denseAppearance.watchLayoutIndex].auxD != null) {
//                drawScript(canvas, 100, mVars.centerY+10f, 75f, 30f);
//            }



            //float seconds = mTime.second + mVars.milliseconds / 1000f;
//            float seconds = wTime.getSecond() + ((mIsSweep) ? mVars.milliseconds / 1000f : 0); //(denseAppearance.)
            float seconds = wTime.getSecond() + ((mAppPreferences.getSweepSeconds()) ? wTime.getMilliSecond() / 1000f : 0); //(denseAppearance.)
            int minutes = wTime.getMinute();//mTime.minute;
            int hours = wTime.getHour();//mTime.hour;
            //Log.i(TAG, "((( wTime.h="+wTime.getHour() + ", min="+wTime.getMinute() + ", sec="+wTime.getSecond());
            //Log.i(TAG, "((( mTime.h="+hours + ", min="+minutes + ", sec="+seconds);

            //int seconds = mTime.second;
            float secRot = seconds / 30f * (float) Math.PI;
            float secRotDeg = (seconds * 6f) - 90f;
            float minRot = (minutes + (seconds / 60f)) / 30f * (float) Math.PI;
            float minRotDeg = ((minutes + (seconds / 60f)) * 6f) - 90f;
            float hrRot = ((hours + (minutes / 60f)) / /*12f*/6f ) * (float) Math.PI; // 6f for 12hr or 12f for 24hr circle
            float hrRotDeg = ((hours + (minutes / 60f)) * 30f ) - 90f; /*15f for 24hr or 30f for 12hr circle*/

            //float secLength = mVars.centerX - 20;


//            float screenRatio = mVars.width / dialElements.baseHandWidth;
//            float pivotX = (dialElements.baseHandWidth / 2.0f) * screenRatio;
//            float pivotY = (dialElements.baseHandHeight / 2.0f) * screenRatio;


//            BlurMaskFilter blurMaskFilter = new BlurMaskFilter(3f, BlurMaskFilter.Blur.INNER);
            //
            // Draw hour hand
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY); // hand bitmap is 320x40
            mVars.hrMatrix.postRotate(hrRotDeg);
            mVars.hrMatrix.postTranslate(mVars.centerX, mVars.centerY);
            // draw hand bevel lights
            if (!isInAmbientMode()) {
                // Draw hour hand shadow
                mVars.offsetMatrix.set(mVars.hrMatrix);
                mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(4f));
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mHoursHandShadow, mVars.offsetMatrix, mHandPaint);
                // draw hour hand decor
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mHoursHandDecor, mVars.hrMatrix, mHandPaint);
                // draw hand bevel lights
                switch (denseAppearance.watchMainHandsIndex) {
                    case ACommon.HANDS_STRAGHT:
                        mVars.handOutline = dialElements.mMainHandStraightPathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_RHOMB:
                        mVars.handOutline = dialElements.mMainHandRhombusPathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_CURLHEAD:
                        mVars.handOutline = dialElements.mMainHandCurlHeadPathScaled[MHP_HOUR];
                        break;
                    case ACommon.HANDS_ARROW:
                        mVars.handOutline = dialElements.mMainHandArrowPathScaled[MHP_HOUR];
                        break;
                } drawPathBevelLights(canvas, 0, mVars.handOutline, mVars.hrMatrix, 0x7fffffff, 0x7f000000, 0.5f);
                // draw hour hand bitmap
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mHoursHandColorized, mVars.hrMatrix, mHandPaint);
            } else {
                // draw hour hand decor
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mHoursHandDecorAmbient, mVars.hrMatrix, mHandPaint);
                // draw hour hand bitmap
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mHoursHandBlack, mVars.hrMatrix, mHandPaint);
            }


            // Draw minute hand
            mVars.hrMatrix.reset();
            mVars.hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY);
            mVars.hrMatrix.postRotate(minRotDeg);
            mVars.hrMatrix.postTranslate(mVars.centerX, mVars.centerY);
            // draw minute hand bitmap
            if (!isInAmbientMode()) {
                // Draw the minute hand shadow
                mVars.offsetMatrix.set(mVars.hrMatrix);
                mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(6f));
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mMinutesHandShadow, mVars.offsetMatrix, mHandPaint);
                // draw minute hand decor
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mMinutesHandDecor, mVars.hrMatrix, mHandPaint);
                // draw hand bevel lights
                switch (denseAppearance.watchMainHandsIndex) {
                    case ACommon.HANDS_STRAGHT:
                        mVars.handOutline = dialElements.mMainHandStraightPathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_RHOMB:
                        mVars.handOutline = dialElements.mMainHandRhombusPathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_CURLHEAD:
                        mVars.handOutline = dialElements.mMainHandCurlHeadPathScaled[MHP_MINUTE];
                        break;
                    case ACommon.HANDS_ARROW:
                        mVars.handOutline = dialElements.mMainHandArrowPathScaled[MHP_MINUTE];
                        break;
                } drawPathBevelLights(canvas, 0, mVars.handOutline, mVars.hrMatrix, 0x7fffffff, 0x7f000000, 0.5f);
                // draw hand
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mMinutesHandColorized, mVars.hrMatrix, mHandPaint);
            } else {
                // draw minute hand decor
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mMinutesHandDecorAmbient, mVars.hrMatrix, mHandPaint);
                // draw hand
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mMinutesHandBlack, mVars.hrMatrix, mHandPaint);
            }

            // Draw the seconds hand
            if (!isInAmbientMode()) {
/*
                float secX = (float) Math.sin(secRot) * secLength;
                float secY = (float) -Math.cos(secRot) * secLength;
                //canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mSecondPaint);
*/
                mVars.hrMatrix.reset();
                mVars.hrMatrix.setTranslate(-mVars.pivotHandX, -mVars.pivotHandY);
                mVars.hrMatrix.postRotate(secRotDeg);
                mVars.hrMatrix.postTranslate(mVars.centerX, mVars.centerY);
                mVars.offsetMatrix.set(mVars.hrMatrix);
                mVars.offsetMatrix.postTranslate(0f, mVars.pixelDim(8f));
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mSecondsHandShadow, mVars.offsetMatrix, mHandPaint);
                //
                // experiment: draw hand bevel lights
                drawPathBevelLights(canvas, 0, dialElements.mMainHandStraightOutlinePathScaled[MHP_SECOND],
                        mVars.hrMatrix, 0x7fffffff, 0x7f000000, 0f);
                //
                canvas.drawBitmap(watchMainHandsBmp[denseAppearance.watchMainHandsIndex].mSecondsHandColorized, mVars.hrMatrix, mHandPaint);
            }

            /*
            mHrTickPaint.setTextSize(15);
            mHrTickPaint.setColor(0xffffffff);

            canvas.drawLine(mVars.centerX, mVars.centerY, mVars.centerX+mPrjP, mVars.centerY+mPrjR, mHrTickPaint);

            // debug draw orientation
            String tmpOrnt;
            //tmpOrnt = "A: " + String.valueOf((int)mAzimuthDeg);
            tmpOrnt = "A: " + String.valueOf((int) (mPrjAngleNorm * RAD2DEG)) + ", L: " + String.valueOf((int) mPrjLenNorm );
            //tmpOrnt = "A: " + String.valueOf((int) mPrjLenNorm );
            canvas.drawText(tmpOrnt, 195, 150, mHrTickPaint);
            //tmpOrnt = "P: " + String.valueOf((int)mPitchDeg);
            tmpOrnt = "P: " + String.valueOf((int)mPrjP);
            canvas.drawText(tmpOrnt, 195, 170, mHrTickPaint);
            //tmpOrnt = "R: " + String.valueOf((int)mRollDeg);
            tmpOrnt = "R: " + String.valueOf((int)mPrjR);
            canvas.drawText(tmpOrnt, 195, 190, mHrTickPaint);
            */

            if (isInAmbientMode()) {
                // draw black hole in center of screen
                mBackgroundPaint.setARGB(255,0,0,0);
                mBackgroundPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.pixelDim(10f), mBackgroundPaint);
            } else {
                drawHandMountingHole(canvas, mVars.centerX, mVars.centerY, isInAmbientMode());
            }


/*
            // draw FPS
            mBackgroundPaint.setColor(0xffffffff);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setTextSize(30f);
            canvas.drawText(String.valueOf(mFramePerSecond),145f,85f,mBackgroundPaint);
            //if (false) { invalidate(); return; }
*/

        } // drawFrame

        Bitmap mountingHoleBitmap;
        //
        private Bitmap drawHandMountingHoleBitmap() {
            Bitmap result;

//            int[] holeColors = new int[]{0x7f333333, 0x7f555555, 0x7fcfcfcf, 0x7fffffff, 0x7fcfcfcf, 0x7f555555, 0x7f333333};
//            int[] holePinColors = new int[]{0x7f333333, 0x7f555555, 0x7fcfcfcf, 0x7fffffff, 0x7fcfcfcf, 0x7f555555, 0x7f333333};
            int[] holeColors = new int[]{
                    Color.argb((int)(190 * 0.20f), 255, 204, 0),
                    Color.argb((int)(190 * 0.50f), 255, 204, 0),
                    Color.argb((int)(190 * 0.80f), 255, 204, 0),
                    Color.argb((int)(190 * 1.00f), 255, 204, 0),
                    Color.argb((int)(190 * 0.80f), 255, 204, 0),
                    Color.argb((int)(190 * 0.50f), 255, 204, 0),
                    Color.argb((int)(190 * 0.20f), 255, 204, 0)
            };
            int[] holePinColors = new int[]{
                    Color.argb((int)(150 * 0.20f), 203, 202, 201),
                    Color.argb((int)(150 * 0.50f), 203, 202, 201),
                    Color.argb((int)(150 * 0.80f), 203, 202, 201),
                    Color.argb((int)(150 * 1.00f), 203, 202, 201),
                    Color.argb((int)(150 * 0.80f), 203, 202, 201),
                    Color.argb((int)(150 * 0.50f), 203, 202, 201),
                    Color.argb((int)(150 * 0.20f), 203, 202, 201)
            };
            float[] holeColorOnPathPos = new float[]{0f, 0.20f, 0.35f, 0.50f, 0.65f, 0.80f, 1.0f};
            Shader holeHandRingShader = null, holePinRingShader = null;

            int dim;
            if (mVars.width == 280) {
                dim = (int) mVars.pixelDim(7f); // even for 280x280 display!!!
            } else {
                dim = (int) mVars.pixelDim(8f); // odd for other display!!!
            }
            result = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            float centerX = dim / 2f, centerY = dim / 2f;

            mVars.rimPath.reset();
            mVars.rimPath.addCircle(centerX, centerY, mVars.pixelDim(2.5f), Path.Direction.CW);

            mMountingHolePaint.setARGB(255, 0, 0, 0);
            mMountingHolePaint.setStrokeWidth(mVars.pixelDim(1.3f));
            mMountingHolePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(mVars.rimPath, mMountingHolePaint);

//            for (int i=0; i<holeColors.length; i++) {
//                holeColors[i] = (holeColors[i] & 0x00ffffff) | (alpha << 24);
//            }
            //
            if(null == holeHandRingShader) {
                holeHandRingShader = new SweepGradient(centerX, centerY, holeColors, holeColorOnPathPos);
                mVars.hrMatrix.reset();
                holeHandRingShader.getLocalMatrix(mVars.hrMatrix);
                mVars.hrMatrix.postRotate(-90f, centerX, centerY);
                holeHandRingShader.setLocalMatrix(mVars.hrMatrix);
            }
            mMountingHolePaint.setShader(holeHandRingShader);
            mMountingHolePaint.setStyle(Paint.Style.STROKE);
            mMountingHolePaint.setStrokeWidth(mVars.pixelDim(1.0f));
            canvas.drawPath(mVars.rimPath, mMountingHolePaint);
            mMountingHolePaint.setShader(null);


            result = blur(result, 1f);
            canvas = new Canvas(result);

            mMountingHolePaint.setShader(holeHandRingShader);
            mMountingHolePaint.setStyle(Paint.Style.STROKE);
            mMountingHolePaint.setStrokeWidth(mVars.pixelDim(1.0f));
            canvas.drawPath(mVars.rimPath, mMountingHolePaint);
            mMountingHolePaint.setShader(null);


//            mMountingHolePaint.setARGB(80, 255, 145, 0);
//            mMountingHolePaint.setStyle(Paint.Style.FILL);
//            canvas.drawCircle(centerX, centerY, mVars.pixelDim(1f), mMountingHolePaint);
            //
            mVars.rimPath.reset();
            mVars.rimPath.addCircle(centerX, centerY, mVars.pixelDim(1.0f), Path.Direction.CW);
            if (null == holePinRingShader) {
                holePinRingShader = new SweepGradient(centerX, centerY, holePinColors, holeColorOnPathPos);
                mVars.hrMatrix.reset();
                holePinRingShader.getLocalMatrix(mVars.hrMatrix);
                mVars.hrMatrix.postRotate(90f, centerX, centerY);
                holePinRingShader.setLocalMatrix(mVars.hrMatrix);
            }
            mMountingHolePaint.setShader(holePinRingShader);
            mMountingHolePaint.setStyle(Paint.Style.STROKE);
            mMountingHolePaint.setStrokeWidth(mVars.pixelDim(1.0f));
            canvas.drawPath(mVars.rimPath, mMountingHolePaint);
            mMountingHolePaint.setShader(null);

            return result;
        } // drawHandMountingHoleBitmap
        private void drawHandMountingHole(Canvas canvas, float centerX, float centerY, boolean ambient) {
            float mountingHoleBitmapHalfDim;
//            if (null == mountingHoleBitmap) {
//                mountingHoleBitmap = drawHandMountingHoleBitmap();
//            }
            mountingHoleBitmapHalfDim = mountingHoleBitmap.getWidth() / 2f;
            if (!ambient) {
                canvas.drawBitmap(mountingHoleBitmap, centerX - mountingHoleBitmapHalfDim, centerY - mountingHoleBitmapHalfDim, null);
            } else {
                mMountingHolePaint.setARGB(255, 0, 0, 0);
                //mMountingHolePaint.setStrokeWidth(mVars.pixelDim(1.3f));
                mMountingHolePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, centerY, mVars.pixelDim(3f), mMountingHolePaint);
            }
        } // drawHandMountingHole

        // colorUpper - рисуется первым, чуть выше
        // colorLower - рисуется вторым, чуть ниже
        private void drawPathBevelLights(Canvas canvas, float strokeW, Path outline, Matrix matrix, int colorUpper, int colorLower, float more) {
            Paint paint = new Paint();
            //paint.setARGB(127, 255, 0, 0);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            //paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if (0 == strokeW) {
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(mVars.pixelDim(0.0f + more));
            } else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(mVars.pixelDim(strokeW));
            }
            Path path = new Path(outline);
            //path.transform(matrix);
            if (null != matrix) mVars.offsetMatrix.set(matrix);
            else mVars.offsetMatrix.reset();
            mVars.offsetMatrix.postTranslate(0.0f, mVars.pixelDim(-(1.0f + more)));
            path.transform(mVars.offsetMatrix);
//                mVars.rimPath.reset();
//                mVars.rimPath.addCircle(mVars.centerX, mVars.centerY, radiusOuter, Path.Direction.CW);
//                mVars.hrMatrix.reset();
            //int[] rimColors = new int[]{color, color, color, color};
            //float[] rimColorOnPathPos = new float[]{0.0f, 0.5f, 0.5f, 1.0f};
            //Shader rimShader = new SweepGradient(mVars.centerX, mVars.centerY, rimColors, rimColorOnPathPos);
//                rimShader.getLocalMatrix(mVars.hrMatrix);
//                mVars.hrMatrix.postRotate(mPrjAngleNorm * RAD2DEG + 90f, centerX, centerY);
//                rimShader.setLocalMatrix(mVars.hrMatrix);
//                rimPaint.setShader(rimShader);
//                rimPaint.setStyle(Paint.Style.STROKE);
//                rimPaint.setStrokeWidth(rimDim);
//                //rimPaint.setAlpha((int) (170*mPrjLenNorm/mShdStickHeight)); //127
//                int alpha = (int) (170*mPrjLenNorm/mShdStickHeight);
//                int r, g, b;
//                r = Color.red(currentAppearance.mMainBackgroundColor);
//                g = Color.green(currentAppearance.mMainBackgroundColor);
//                b = Color.blue(currentAppearance.mMainBackgroundColor);
//                rimPaint.setARGB(alpha, r, g, b);
//                canvas.drawPath(mVars.rimPath, rimPaint);
//                rimPaint.setShader(null);
            //paint.setShader(rimShader);
            paint.setColor(colorUpper);
            canvas.drawPath(path, paint);
            //paint.setShader(null);
            //
            path = new Path(outline);
            if (null != matrix) mVars.offsetMatrix.set(matrix);
            else mVars.offsetMatrix.reset();
            mVars.offsetMatrix.postTranslate(0.0f, mVars.pixelDim(1.0f + more));
            path.transform(mVars.offsetMatrix);
            paint.setColor(colorLower);
            canvas.drawPath(path, paint);
        } // drawPathBevelLights


//        private void sendDenseScreenshot(Bitmap frame) {
//            DataMap dataMap = new DataMap();
//            //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_FRAME_SCREENSHOT);
//            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_DENSE_SCREENSHOT);
//            dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
//            Asset asset = createAssetFromBitmap(frame);
//            dataMap.putAsset(ACommon.KEY_SCREENSHOT, asset);
//            new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMap).start();
//        } // sendDenseScreenshot
//        //
//        private void sendAmbientScreenshot(Bitmap frame) {
//            DataMap dataMap = new DataMap();
//            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_AMBIENT_SCREENSHOT);
//            dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
//            Asset asset = createAssetFromBitmap(frame);
//            dataMap.putAsset(ACommon.KEY_SCREENSHOT, asset);
//            new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMap).start();
//        } // sendAmbientScreenshot
        //
        private void sendScreenshot(Bitmap frame, int type) {
            DataMap dataMap = new DataMap();
            dataMap.putInt(ACommon.KEY_EVENT, type);
            dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
            Asset asset = createAssetFromBitmap(frame);
            dataMap.putAsset(ACommon.KEY_SCREENSHOT, asset);
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMap).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH, dataMap, mGoogleApiClient, null).start();
        } // sendScreenshot

        private void sendPlateBitmap() {
            //Log.i(TAG, "((( sendPlateBitmap, mDensePlateBitmap=" + mDensePlateBitmap);
            if (null == mDensePlateBitmap) return;
            DataMap dataMap = new DataMap();
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_PLATE_BITMAP);
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            Asset asset = createAssetFromBitmap(mDensePlateBitmap);
            dataMap.putAsset(ACommon.KEY_SCREENSHOT, asset);
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH_3, dataMap, mGoogleApiClient, mPeerId).start();
        } // sendPlateBitmap


//        class SendThroughWearNetworkThread extends Thread {
//            String path;
//            DataMap dataMap;
//
//            // Constructor for sending data objects to the data layer
//            SendThroughWearNetworkThread(String p, DataMap data) {
//                path = p;
//                dataMap = data;
//            }
//
//            public void run() {
//                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//                //Log.i(TAG, "*** SendThroughWearNetworkThread dataMap=" + dataMap + ", path=" + path);
//                //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//                List<Node> nodeList;
//                int numNodes = 0;
//                if (nodes != null) {
//                    nodeList = nodes.getNodes();
//                    if (nodeList != null) numNodes = nodeList.size();
//                }
//                for (int i=0; i<numNodes; i++) {
//                    PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                    putDMR.getDataMap().putAll(dataMap);
//                    PutDataRequest request = putDMR.asPutDataRequest();
//                    DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
//                }
//            }
//        } // class SendTroughWearNetworkThread


        private void drawFitInToast(Canvas canvas, Rect bounds) {
            int width = bounds.width();
            int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;
            Rect textBounds = new Rect();
            String text = getResources().getString(R.string.fit_in_toast); //"Адаптация";//"Fit in...";
            Paint paint = new Paint();
            paint.setColor(0xffbdebde);
            paint.setTextSize(height / 12);
            paint.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawColor(0xff000000);
            canvas.drawText(text, centerX - textBounds.exactCenterX(), centerY - textBounds.exactCenterY(), paint);
        } // drawFitInToast


        private void drawClipShotToast(Canvas canvas, Rect bounds) {
            int width = bounds.width();
            int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;
            Rect textBounds = new Rect();
            String text = "Clip shot...";//getResources().getString(R.string.fit_in_toast); //"Адаптация";//"Fit in...";
            Paint paint = new Paint();
            paint.setColor(0xffbdebde);
            paint.setTextSize(height / 12);
            paint.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawColor(0xff000000);
            canvas.drawText(text, centerX - textBounds.exactCenterX(), centerY - textBounds.exactCenterY(), paint);
        } // drawClipShotToast


        private boolean readyToFit() {

            if (isInAmbientMode()) {
//                Log.i(TAG, "((( readyToFit, inTransition=" + mAmbientProp.inTransition() +
//                ", inPlateTransition=" + mAmbientProp.inPlateTransition() +
//                ", mDialPlateReady=" + denseAppearance.mDialPlateReady +
//                ", isSet=" + mAmbientProp.isSet());
            }

            if (mAmbientProp.inTransition()) {
                if (mAmbientProp.mPhase == 1) {
                    // to let draw fitin toast on watch
                    mAmbientProp.mPhase = 2;
                    return false;
                }
            }
            if (!mAmbientProp.inPlateTransition()) {
                if (!denseAppearance.mDialPlateReady) {
                    mAmbientProp.requestPlateTransition();
                    //if (isInAmbientMode()) postInvalidate();
                    return false;
                }
            }
            return mAmbientProp.isSet();
        } // readyToFit

        ReentrantLock mLockDrawingAssets = new ReentrantLock();
        private void initDrawingAssets(Canvas canvas, Rect bounds) {
            //Log.i(TAG, "((((( initDrawingAssets");

            //drawFitInToast(canvas, bounds);
            //wakeUpScreen(7000);

            mLockDrawingAssets.lock(); try { mVars = new Variables(canvas, bounds);
                dialElements.buildDialPaths(denseAppearance);
                dialElements.scaleDialPaths((mVars.mMainRadius * 2.0f));
                dialElements.scaleHandPaths((mVars.mMainRadius * 2.0f));
                inflateAuxPositions(); } finally { mLockDrawingAssets.unlock(); }
            prepareBitmaps();
            initPlateBitmaps();

            //if (isInAmbientMode()) switchAppearance(true); //!!!

            mAmbientProp.clearTransition();
        } // initDrawingAssets


        private void initPlateBitmap(Canvas canvas, boolean ambient) {

//            // draw color for dial circle hour digits
//            mDigitsPaint.setColor(currentAppearance.mMainDigitsColor);
//            canvas.drawCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathInner, mDigitsPaint);

            mHmRelief = new HourMarkReliefBuilder(mVars.width, mVars.height, 0f);
            mDomRelief = new DomReliefBuilder(mVars.width, mVars.height, 0f);
            textStringRelief = new TextStringReliefBuilder(mVars.width, mVars.height, 0f);

            drawMainDialBitmap(canvas, ambient);

            //if (true) return;

            drawOuterTicks(canvas, ambient);

            if (watchLayouts[denseAppearance.watchLayoutIndex].auxA != null) {
                drawWeekdayDial(canvas,
                        watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxA], 0f, 56f, true, false, ambient);
            }

            if (watchLayouts[denseAppearance.watchLayoutIndex].auxB != null) {
                drawBattDial(canvas, watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxB],
                        (float)Math.PI, 20f, 10f, 5f, true, false, ambient);
            }

            if (watchLayouts[denseAppearance.watchLayoutIndex].auxC != null) {
                drawMonthDial(canvas,
                        watchAuxPositions[watchLayouts[denseAppearance.watchLayoutIndex].auxC],
                        0f, 0f, true, false, ambient);
            }

//            if (watchLayouts[denseAppearance.watchLayoutIndex].auxD != null) {
//                drawScript(canvas, 100, mVars.centerY+10f, 75f, 30f);
//            }

            drawInscriptions(canvas, ambient);

        } // initPlateBitmap


        private void drawOuterTicks(Canvas canvas, boolean ambient) {

            if (ambient) {
                drawOuterTicksAndDigits(canvas, ambient, false);
                return;
            }

            Canvas tickCanvas = new Canvas();
            Bitmap tickBitmap;
            tickBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            tickBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
            tickCanvas.setBitmap(tickBitmap);

            drawOuterTicksAndDigits(tickCanvas, ambient, true);
            tickBitmap = blur(tickBitmap, mVars.pixelDim(2.5f));
            canvas.drawBitmap(tickBitmap, 0f, 0f, null);

            drawOuterTicksAndDigits(canvas, ambient, false);

            tickBitmap.recycle(); tickBitmap = null; /*System.gc();*/

        } // drawOuterTicks








        class TextStringReliefBuilder {

            private static final boolean L = false;

            Canvas      canvas;
            Bitmap      bitmap;
            Paint       paint;
            int         colorSide;
            int         colorLight;
            int         colorDark;
            float       lightSourceAngle;
            Path        pathOutside, pathInside;
            float       strokeWidth;

            private void initBitmap() {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }

            private void initPaint() {
                paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
            }

            public TextStringReliefBuilder(int width, int height, float lightSourceAngle) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                initBitmap();
//                paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeCap(Paint.Cap.ROUND);
                initPaint();
                colorSide = Color.argb(255, 127, 127, 127); //denseAppearance.
                colorLight = Color.WHITE;
                colorDark = Color.BLACK;
                this.lightSourceAngle = lightSourceAngle;
                strokeWidth = 3f;
            }

            MonoSegmentProcessor segmentCallback = new MonoSegmentProcessor() {
                @Override
                //public void onSegment(int pathIndex, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
                public void onSegment(boolean emboss, PathPoint start, PathPoint end, PathMeasure pathMeasure) {
//                    float tanDiff = Math.abs(end.pointTangent - start.pointTangent);
//                    float spaceDist = (float) Math.hypot(Math.abs(end.pointX - start.pointX), Math.abs(end.pointY - start.pointY));
//                    String logMsg = String.format(
//                            "sgm <%3.2f %3.2f> XY: %3.2f (%3.2f) = [%3.2f, %3.2f] - [%3.2f, %3.2f], Tan: {%3.2f, %3.2f}.",
//                            start.getPathDistance(), end.getPathDistance(),
//                            spaceDist, tanDiff,
//                            start.getPointX(), start.getPointY(),
//                            end.getPointX(), end.getPointY(),
//                            start.getPointTangent(), end.getPointTangent()
//                    );
//                    Log.i(TAG, "(((( DOM Contour " + logMsg);

                    Path segment = new Path();
                    if (pathMeasure.getSegment(start.getPathDistance(), end.getPathDistance(), segment, true)) {
                        int startColor = PathPoint.tangentColor(emboss, start.getPointTangent(),
                                colorSide, colorLight, colorDark, lightSourceAngle);
                        int endColor = PathPoint.tangentColor(emboss, end.getPointTangent(),
                                colorSide, colorLight, colorDark, lightSourceAngle);
                        Shader shader = new LinearGradient(
                                start.getPointX(), start.getPointY(), end.getPointX(), end.getPointY(),
                                startColor, endColor, Shader.TileMode.CLAMP
                        );
                        paint.setShader(shader);
                        paint.setStrokeWidth(mVars.pixelDim(strokeWidth));
                        canvas.drawPath(segment, paint);
                        paint.setShader(null);
                    }
                }
            };

            public void traversePath(Path path, boolean outside, boolean emboss) {
                pathOutside = new Path();
                pathOutside.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                pathOutside.addPath(path);
                pathInside = new Path();
                pathInside.addPath(path);

                initBitmap();
                initPaint();

                //boolean emboss = true;
                PathPoint.traversePath((outside) ? pathOutside : pathInside, emboss, this.segmentCallback);
            }

            public Bitmap getBitmap() { return bitmap; }



            private Bitmap drawVariant_1() { //Path pathInside, Path pathOutside
                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());

                Bitmap reliefBitmap = this.getBitmap();
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, reliefBitmap, "text_", "_00_rlf", timeStamp);
                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f));
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, reliefBitmap, "text_", "_01_rlf", timeStamp);

                canvas = new Canvas();
                paint = new Paint(); paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);
                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

                Bitmap workingBmp = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workingBmp);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                canvas.save();
                canvas.clipPath(pathOutside);
                canvas.drawBitmap(reliefBitmap, 0f, 0f, null);
                canvas.restore();
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "text_", "_1_rlf", timeStamp);

                paint.setShader(shdrRelief);
                paint.setStyle(Paint.Style.STROKE);
                //canvas = new Canvas();
                //canvas.setBitmap(workingBmp);
                paint.setStrokeWidth(mVars.pixelDim(1f));
                paint.setAlpha(160);
                canvas.drawPath(pathInside, paint);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "text_", "_2_rlf", timeStamp);

                workingBmp = blur(workingBmp, 0.5f);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "text_", "_3_rlf", timeStamp);
                canvas.setBitmap(workingBmp);
                paint.setShader(null);
                Xfermode xfr = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
                paint.setXfermode(xfr);
                canvas.drawBitmap(reliefBitmap, 0, 0, paint);
                if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, workingBmp, "text_", "_4_rlf", timeStamp);
                //return workingBmp; // как у часовых отметок

                Bitmap workingBmp2 = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workingBmp2);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint.setColorFilter(new PorterDuffColorFilter(denseAppearance.mMainBackgroundColor, PorterDuff.Mode.MULTIPLY));
                canvas.drawBitmap(workingBmp, 0, 0, paint);
                //return workingBmp2; // мало светлого

                canvas.setBitmap(reliefBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
                paint.setAlpha(160);
                canvas.drawBitmap(workingBmp, 0, 0, paint); // ч/б
                paint.setAlpha(96);
                canvas.drawBitmap(workingBmp2, 0, 0, paint); // colorized
                return reliefBitmap;
            }



            public Bitmap drawVariant_0() {
                Bitmap reliefBitmap = this.getBitmap();
                reliefBitmap = blur(reliefBitmap, mVars.pixelDim(2f));

                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());

                canvas = new Canvas();
                paint = new Paint();
                paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);

                Bitmap maskBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(maskBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(0f);
                paint.setColor(Color.BLACK);
                canvas.drawPath(pathOutside, paint);
                //Bitmap maskAlpha = PixelUtil.alphaChannel(maskBitmap);

                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, maskBitmap, "dom_", "_mask", timeStamp);
                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, maskAlpha, "dom_", "_alpha", timeStamp);

                Shader shdrRelief = new BitmapShader(reliefBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Shader shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
                paint.setShader(shdrOp);

                Bitmap workingBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workingBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(mVars.pixelDim(3f));
                canvas.drawPath(pathInside, paint);
                paint.setShader(null);

                Bitmap outputBitmap = blur(workingBitmap, mVars.pixelDim(1f)); //1f
                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, outputBitmap, "dom_", "_1_out", timeStamp);

//                canvas.setBitmap(workingBitmap);
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                Xfermode xfermode1 = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
//                canvas.drawBitmap(maskAlpha, 0, 0, null); // DST
//                paint.setXfermode(xfermode1);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                paint.setXfermode(null);
//                //if (L) ACommon.bmpToPicturesDir(AWearFaceService.this, outputBitmap, "dom_", "_2_out", timeStamp);


                //
//                canvas.setBitmap(workingBitmap);
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                paint = new Paint();
//                paint.setAntiAlias(true); //paint.setDither(true); paint.setFilterBitmap(true);
//                PorterDuffColorFilter colorFilter =
//                        new PorterDuffColorFilter(denseAppearance.mMainTickColor, PorterDuff.Mode.MULTIPLY);
//                paint.setColorFilter(colorFilter);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_3_out", timeStamp);
//                canvas.drawBitmap(outputBitmap, 0, 0, paint); // SRC
//                ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_4_out", timeStamp);
                //



//            canvas.setBitmap(workingBitmap);
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            shdrRelief = new BitmapShader(outputBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            //shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
//            paint.setShader(shdrOp);
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//            paint.setStrokeWidth(mVars.pixelDim(3f));
//            canvas.drawPath(domInside, paint);
//            //
//            ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_2_rlf");
//
//            canvas.setBitmap(workingBitmap);
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            //shdrRelief = new BitmapShader(outputBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            //shdrMask = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            shdrOp = new ComposeShader(shdrMask, shdrRelief, PorterDuff.Mode.SRC_ATOP);
//            paint.setShader(shdrMask);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(outputBitmap, 0, 0, paint);
//            //
//            ACommon.bmpToPicturesDir(AWearFaceService.this, workingBitmap, "dom_", "_3_rlf");

                return outputBitmap;
            }

            public Bitmap drawRelief() {
                return drawVariant_1();
            }



            public static final int OUTER_RELIEF = 1;
            public static final int INNER_COLOR = 2;
            //
            public Bitmap composeFx(int fxType, int index, float pathOffsetX, float pathOffsetY, Paint paint, int color) {
                String text = denseAppearance.mPrint.text[index];
                float pathIncline = denseAppearance.mPrint.incline[index];
                Path textShape = new Path();
                //String timeStamp = String.valueOf(System.currentTimeMillis());
                paint.getTextPath(text, 0, text.length(), 0f, 0f, textShape); //mVars.centerX, mVars.centerY
                Matrix matrix = new Matrix();
                matrix.postTranslate(pathOffsetX, pathOffsetY);
                matrix.postRotate(pathIncline, pathOffsetX, pathOffsetY);
                textShape.transform(matrix);

                if (OUTER_RELIEF == fxType) {
                    boolean emboss = (Inscription.FX_EMBOSS == denseAppearance.mPrint.fx[index]) ? true : false;
                    this.traversePath(textShape, true, emboss);
                    return this.drawRelief();
                }
                if (INNER_COLOR == fxType) return this.drawInnerColor(textShape, color);

                return null;
            }




            private Bitmap drawInnerColor(Path textShape, int textColor) {
                Bitmap result = null;

                Canvas canvas;
                Paint paint;
                String timeStamp = String.valueOf(System.currentTimeMillis());

                canvas = new Canvas();
                paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);

                Bitmap maskBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(maskBitmap);

                paint.setColor(textColor);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(0f);
                canvas.drawPath(textShape, paint);
                //ACommon.bmpToPicturesDir(AWearFaceService.this, maskBitmap, "fxc_", "_1_M", timeStamp);

                Bitmap workBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(workBitmap);

                int colorDense, colorSemiDense;
                if (ACommon.argbToY(textColor) < 78) {
                    colorDense = Color.argb(255, 255,255,255);
                    colorSemiDense = Color.argb(127, 255,255,255);
                } else {
                    colorDense = Color.argb(255, 0,0,0);
                    colorSemiDense = Color.argb(127, 0,0,0);
                }

                paint.setColor(colorDense);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(mVars.pixelDim(3f));
                canvas.drawPath(textShape, paint);
                canvas.drawBitmap(maskBitmap, 0, 0, null);
                paint.setColor(colorSemiDense);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(mVars.pixelDim(1f));
                canvas.drawPath(textShape, paint);

                Bitmap workBitmap2 = blur(workBitmap, mVars.pixelDim(2f));
                //ACommon.bmpToPicturesDir(AWearFaceService.this, workBitmap2, "fxc_", "_2_W", timeStamp);

                canvas.setBitmap(workBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(maskBitmap, 0, 0, null);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                paint.setAlpha(96);
                canvas.drawBitmap(workBitmap2, 0, 0, paint);
                //ACommon.bmpToPicturesDir(AWearFaceService.this, workBitmap, "fxc_", "_3_W", timeStamp);

                result = workBitmap;
//                result = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
//                canvas.setBitmap(result);


                return result;
                //return null;
            }

        }
        //
        TextStringReliefBuilder textStringRelief;





        private void drawInscriptions(Canvas canvas, boolean ambient) {

            if (ambient) return;

            if (denseAppearance.watchLayoutIndex != denseAppearance.mPrint.watchLayoutIndex) return;

            Canvas inscrCanvas = new Canvas();
            Bitmap inscrBitmap;
            //if (null != mDensePlateBitmap) mDensePlateBitmap.recycle();
            inscrBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            inscrBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
            inscrCanvas.setBitmap(inscrBitmap);

            if (!ambient) {
                drawInscriptionPack(inscrCanvas, canvas, true, ambient);
                inscrBitmap = blur(inscrBitmap, mVars.pixelDim(3f));
                canvas.drawBitmap(inscrBitmap, 0f, 0f, null);
            }

            drawInscriptionPack(inscrCanvas, canvas, false, ambient);

            inscrBitmap.recycle(); inscrBitmap = null; /*System.gc();*/

        } // drawInscriptionPack

        private void drawInscriptionPack(Canvas inscriptionCanvas, Canvas plateCanvas, boolean forBlur, boolean ambient) {

            for (int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
                if (Inscription.DEFAULT_NO == denseAppearance.mPrint.enabled[i]) continue;

                int textColor;
                switch (i) {
                    case 0:
                        textColor = denseAppearance.mMainInscription_1_Color; break;
                    case 1:
                        textColor = denseAppearance.mMainInscription_2_Color; break;
                    case 2:
                        textColor = denseAppearance.mMainInscription_3_Color; break;
                    case 3:
                        textColor = denseAppearance.mMainInscription_4_Color; break;
                    case 4:
                        textColor = denseAppearance.mMainInscription_5_Color; break;
                    case 5:
                        textColor = denseAppearance.mMainInscription_6_Color; break;
                    case 6:
                        textColor = denseAppearance.mMainInscription_7_Color; break;
                    default:
                        textColor = Color.WHITE; break;
                } if (0 == Color.alpha(textColor)) continue;

                float pathRadiusCenterX, pathRadiusCenterY, pathRadius100, pathRadius;
                int layoutIndex = denseAppearance.mPrint.watchLayoutIndex; // denseAppearance.watchLayoutIndex
                pathRadius100 = mVars.mMainRadius;
                pathRadiusCenterX = mVars.centerX;
                pathRadiusCenterY = mVars.centerY;
                switch (((int) denseAppearance.mPrint.bend[i])) {
                    case Inscription.BEND_STRAIGHT:
                    case Inscription.BEND_ROUND_MC:
                        pathRadius100 = mVars.mMainRadius;
                        pathRadiusCenterX = mVars.centerX;
                        pathRadiusCenterY = mVars.centerY;
                        break;
                    case Inscription.BEND_ROUND_AC:
                        if (null != watchLayouts[layoutIndex].auxA) {
                            pathRadius100 = watchAuxPositions[watchLayouts[layoutIndex].auxA].dimension;
                            pathRadiusCenterX = watchAuxPositions[watchLayouts[layoutIndex].auxA].cX;
                            pathRadiusCenterY = watchAuxPositions[watchLayouts[layoutIndex].auxA].cY;
                        }
                        break;
                    case Inscription.BEND_ROUND_BC:
                        if (null != watchLayouts[layoutIndex].auxB) {
                            pathRadius100 = watchAuxPositions[watchLayouts[layoutIndex].auxB].dimension;
                            pathRadiusCenterX = watchAuxPositions[watchLayouts[layoutIndex].auxB].cX;
                            pathRadiusCenterY = watchAuxPositions[watchLayouts[layoutIndex].auxB].cY;
                        }
                        break;
                    case Inscription.BEND_ROUND_CC:
                        if (null != watchLayouts[layoutIndex].auxC) {
                            pathRadius100 = watchAuxPositions[watchLayouts[layoutIndex].auxC].dimension;
                            pathRadiusCenterX = watchAuxPositions[watchLayouts[layoutIndex].auxC].cX;
                            pathRadiusCenterY = watchAuxPositions[watchLayouts[layoutIndex].auxC].cY;
                        }
                        break;
                }
                pathRadius = pathRadius100 * denseAppearance.mPrint.radius[i] / 100f;



                float pathOffsetX = 0f, pathOffsetY = 0f;
                float left, top, right, bottom;
                Path path = new Path();
                path.reset();
                Matrix matrix = new Matrix();
                switch (((int) denseAppearance.mPrint.bend[i])) {
                    case Inscription.BEND_STRAIGHT:
                        pathOffsetX = pathRadiusCenterX +
                                (float) Math.sin(denseAppearance.mPrint.angle[i] * Math.PI / 180f) * pathRadius;
                        pathOffsetY = pathRadiusCenterY +
                                (float) -Math.cos(denseAppearance.mPrint.angle[i] * Math.PI / 180f) * pathRadius;
                        path.moveTo(pathOffsetX, pathOffsetY);
                        path.rLineTo(mVars.width, 0f);
                        matrix.reset();
                        matrix.postRotate(denseAppearance.mPrint.incline[i], pathOffsetX, pathOffsetY);
                        path.transform(matrix);
                        break;
                    case Inscription.BEND_ROUND_MC:
                    case Inscription.BEND_ROUND_AC:
                    case Inscription.BEND_ROUND_BC:
                    case Inscription.BEND_ROUND_CC:
                        left = pathRadiusCenterX - pathRadius;
                        top = pathRadiusCenterY - pathRadius;
                        right = pathRadiusCenterX + pathRadius;
                        bottom = pathRadiusCenterY + pathRadius;
                        RectF oval = new RectF(left, top, right, bottom);
                        path.addOval(oval,
                                (denseAppearance.mPrint.direction[i] == Inscription.DIRECTION_CCW) ?
                                        Path.Direction.CCW : Path.Direction.CW);

                        matrix.reset();
                        matrix.postRotate(denseAppearance.mPrint.angle[i], pathRadiusCenterX, pathRadiusCenterY);
                        path.transform(matrix);
                        break;
                }

                Paint paintText = new Paint();
                paintText.setAntiAlias(true);
                paintText.setDither(true);
                paintText.setFilterBitmap(true);
                paintText.setSubpixelText(true);
                paintText.setTypeface(Typeface.create(denseAppearance.mPrint.fontFamily[i],
                        Inscription.getStyleValue(denseAppearance.mPrint.fontStyle[i])));
                paintText.setTextSize(mVars.pixelDim(denseAppearance.mPrint.textSize[i]));
                paintText.setTextScaleX(denseAppearance.mPrint.textScaleX[i]);
                paintText.setTextAlign(Paint.Align.LEFT);

                // todo: добавить проверку на выход за пределы экрана - если выходим, то не рисовать!!!
                //paintText.measureText();

                if (forBlur) {
                    if (!ambient && Inscription.FX_NONE != ((int) denseAppearance.mPrint.fx[i])) {
                        if (false == Inscription.fxRelief(denseAppearance, i)) {
                            if (Inscription.FX_DARK_SHADOW == ((int) denseAppearance.mPrint.fx[i])) paintText.setARGB(127, 0, 0, 0);
                            else paintText.setARGB(127, 255, 255, 255);
                            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
                            paintText.setStrokeWidth(2.0f);
                            inscriptionCanvas.drawTextOnPath(denseAppearance.mPrint.text[i], path, 0f, 0f, paintText);
                        }
                    }
                } else {
                    if (!ambient && Inscription.fxRelief(denseAppearance, i)) {
                        if (denseAppearance.isShowInscriptionsRelief()) {
                            //testTextRelief(canvas, i, pathOffsetX, pathOffsetY);
                            //Bitmap relief = textStringRelief.composeRelief(i, pathOffsetX, pathOffsetY, paintText);
                            Bitmap relief = textStringRelief.composeFx(
                                    TextStringReliefBuilder.OUTER_RELIEF, i, pathOffsetX, pathOffsetY, paintText, textColor);
                            if (null != relief) {
                                int savA = paintText.getAlpha();
                                paintText.setAlpha(denseAppearance.mInscriptionsReliefStrength);
                                plateCanvas.drawBitmap(relief, 0, 0, paintText);
                                paintText.setAlpha(savA);
                            }
                        }

                        Bitmap innerColor = textStringRelief.composeFx(
                                TextStringReliefBuilder.INNER_COLOR, i, pathOffsetX, pathOffsetY, paintText, textColor);
                        if (null != innerColor) {
                            plateCanvas.drawBitmap(innerColor, 0, 0, null);
                        } else {
                            paintText.setColor(textColor);
                            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
                            paintText.setStrokeWidth(0f); //0.5f
                            plateCanvas.drawTextOnPath(denseAppearance.mPrint.text[i], path, 0f, 0f, paintText);
                        }
                    } else {
                        if (!ambient) {
                            paintText.setColor(textColor);
                            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
                            paintText.setStrokeWidth(0.5f); //0.5f
                            plateCanvas.drawTextOnPath(denseAppearance.mPrint.text[i], path, 0f, 0f, paintText);
                        } else {
                            paintText.setColor(textColor);
                            paintText.setStyle(Paint.Style.STROKE);
                            paintText.setStrokeWidth(1f); //0.5f
                            plateCanvas.drawTextOnPath(denseAppearance.mPrint.text[i], path, 0f, 0f, paintText);
                        }
                    }
                }

            } // for every inscription
        } // drawInscriptions

        private void initPlateBitmaps() {
            //Log.i(TAG, "((( initPlateBitmaps, transparent=" + Color.TRANSPARENT);

            new WatchAppearance.SaveAppearance(
                    ((WearApplication) getApplication()).mLockConfigFile,
                    getApplicationContext(),
                    getString(R.string.configFileName),
                    denseAppearance
            ).run();

            Canvas canvas = new Canvas();

            if (null != mDensePlateBitmap) { mDensePlateBitmap.recycle(); mDensePlateBitmap = null; /*System.gc();*/ }
            mDensePlateBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            mDensePlateBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
            canvas.setBitmap(mDensePlateBitmap);
            //canvas.drawColor(Color.BLACK);
            initPlateBitmap(canvas, false);

            if (null != mAmbientPlateBitmap) { mAmbientPlateBitmap.recycle(); mAmbientPlateBitmap = null; /*System.gc();*/ }
            mAmbientPlateBitmap = Bitmap.createBitmap(mVars.width, mVars.height, Bitmap.Config.ARGB_8888);
            mAmbientPlateBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
            canvas.setBitmap(mAmbientPlateBitmap);
            initPlateBitmap(canvas, true);

            mAmbientProp.clearPlateTransition();
            denseAppearance.mDialPlateReady = true;
        } // initPlateBitmaps



        volatile boolean clipShot = false;


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            if (readyToFit()) {
                // draw watch face
                if (null == mVars || mAmbientProp.inTransition()) {
                    //Log.i(TAG, "((((( FIRST onDraw");
                    initDrawingAssets(canvas, bounds);

                    sendActualWatchfaceValues();
                    wakeUpScreen(1000);

                }
                if (mAmbientProp.inPlateTransition()) {
                    initPlateBitmaps();

                    sendActualWatchfaceValues(); // todo: сделать дополнительно отдельный вызов sendActualAuxValues ???
                    wakeUpScreen(1000);

                }




                if (DemoPackData.needStartClipShot(wTime.demoPackData)) {
                    // run clip shot thread here
                    wakeUpScreen(
                            wTime.demoPackData[DemoPackData.INDEX_CLIP_SHOT_DURATION].value
                            * wTime.demoPackData[DemoPackData.INDEX_CLIP_SHOT_FPS].value
                            * 1000 * 2);
                    new DrawClipShot(wTime.demoPackData[DemoPackData.INDEX_CLIP_SHOT_FPS].value,
                            wTime.demoPackData[DemoPackData.INDEX_CLIP_SHOT_DURATION].value).start();
                    //wTime.setClipShot(true);
                    //wTime.clipShot = true;
                    clipShot = true;
                }

                if (clipShot) { //wTime.getClipShot() wTime.clipShot

                    drawClipShotToast(canvas, bounds);

                } else {

                    wTime.set(System.currentTimeMillis()); //mVars.now

                    //mVars.currSeconds = mVars.now / 1000;
                    if (wTime.getNowInSeconds() == mSeconds) { //mVars.currSeconds
                        mFrameCount++;
                    } else {
                        mFramePerSecond = mFrameCount;
                        //Log.i(TAG, "((( FPS=" + mFramePerSecond);
                        mFrameCount = 0;
                        mSeconds = wTime.getNowInSeconds(); //mVars.currSeconds;
                    }


                    if (!isInAmbientMode()) {
                        drawFrame(mVars.frameCanvasDense);
                        canvas.drawBitmap(mVars.frameBitmapDense, 0, 0, null);
                        //
                        if (appearanceScreenshotTimeMs < appearanceModificationTimeMs) {
                            sendScreenshot(mVars.frameBitmapDense, ACommon.EVT_DENSE_SCREENSHOT);
                            appearanceScreenshotTimeMs = wTime.getRawNow();//mVars.now;
                        }
                    } else {
                        drawFrame(mVars.frameCanvasAmbient);
                        canvas.drawBitmap(mVars.frameBitmapAmbient, 0, 0, null);
                        //
                        if (ambientAppearanceScreenshotTimeMs <= ambientAppearanceModificationTimeMs) {
                            //Log.i(TAG, "*************");
                            sendScreenshot(mVars.frameBitmapAmbient, ACommon.EVT_AMBIENT_SCREENSHOT);
                            ambientAppearanceScreenshotTimeMs = wTime.getRawNow();//mVars.now;
                        }
                    }

                }
            } else {
                // draw conforming toast
                drawFitInToast(canvas, bounds);
                invalidate();
            }

            // Draw every frame as long as we're visible and in interactive mode. Uncomment if sweep.
            if (mAppPreferences.getSweepSeconds() && isVisible() && !isInAmbientMode()) { //mIsSweep
                invalidate();
            }
        } // onDraw




        class DrawClipShot extends Thread {

            int fps, duration;
            Bitmap frameIcon;

            public DrawClipShot(int fps, int duration) {
                this.fps = fps;
                this.duration = duration;
                frameIcon = null;
                //Log.i(TAG, "((( DrawClipShot START, fps=" + fps + ", duration=" + duration);
            }

            public void run() {
                int i;
                int year, month, day, hour, minute, second;
                long millis;

                wTime.set(System.currentTimeMillis());
                year = wTime.getYear();
                month = wTime.getMonth();
                day = wTime.getDayOfMonth();
                hour = wTime.getHour();
                minute = wTime.getMinute();
                second = wTime.getSecond();

                if (DemoPackData.isTime(wTime.demoPackData)) {
                    hour = wTime.demoPackData[DemoPackData.INDEX_HOUR].value;
                    minute = wTime.demoPackData[DemoPackData.INDEX_MINUTES].value;
                    second = wTime.demoPackData[DemoPackData.INDEX_SECONDS].value;
                }
                if (DemoPackData.isDate(wTime.demoPackData)) {
                    month = wTime.demoPackData[DemoPackData.INDEX_MONTH].value;
                    day = wTime.demoPackData[DemoPackData.INDEX_DAYOFMONTH].value;
                }
                wTime.set(year, month, day, hour, minute, second);
                millis = wTime.getRawNow();
                //Log.i(TAG, "((( DrawClipShot, now=" + millis);

                wTime.setClipShot(true);
                String timeStamp = "shot"; //String.valueOf(System.currentTimeMillis());

                long newMillis;
                for (i=0; i < duration * fps; i++) {
                    newMillis = millis + (1000 / fps) * i;
                    wTime.set(newMillis);
                    //Log.i(TAG, "((( DrawClipShot, frame " + (i + 1) + " from " + (duration * fps));
                    //Log.i(TAG, "((( DrawClipShot, now=" + wTime.getRawNow() + ", newMillis=" + newMillis);
                    drawFrame(mVars.frameCanvasDense);
                    Bitmap clipShot = drawInFrame(mVars.frameBitmapDense);
                    String suff = "" + (i+1);
                    ACommon.bmpToPicturesDir(AWearFaceService.this, clipShot, "clip", suff, timeStamp);
                }

                //wTime.setClipShot(false);
                //wTime.clipShot = false;
                clipShot = false;
                wTime.setClipShot(false);
                //Log.i(TAG, "((( DrawClipShot FINISH, frames=" + (i));
            }

            Bitmap drawInFrame(Bitmap icon) {
                Bitmap dialBitmap, dialIcon, frameBitmap, resultBitmap; //frameIcon,
                final float BASE_DIMENSION = 500f;

                dialIcon = icon;

                int density = dialIcon.getDensity();
                float dialWidth = dialIcon.getWidth();
                float dialHeight = dialIcon.getHeight();
                float scale = (dialWidth / BASE_DIMENSION);

                if (null == frameIcon) {
                    Resources resources = AWearFaceService.this.getApplicationContext().getResources();
                    BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
                    bmfOptions.inDensity = density; // !!!!!!!
                    bmfOptions.inScaled = false;
                    bmfOptions.inDither = false;
                    bmfOptions.inSampleSize = 1;
                    bmfOptions.inJustDecodeBounds = false;
                    //bmfOptions.inPreferQualityOverSpeed = true;
                    //bmfOptions.inPremultiplied = false;
                    bmfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                bmfOptions.outWidth = 564;
//                bmfOptions.outHeight = 564;
                    frameIcon = BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions);
                    //if (1f != scale) {
                    frameIcon = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions),
                            (int) (frameIcon.getWidth() * scale) - 2, (int) (frameIcon.getHeight() * scale) - 2, true
                    );
                }

                float resultWidth, resultHeight;
                resultWidth = frameIcon.getWidth();
                resultHeight = frameIcon.getHeight();
                resultBitmap = Bitmap.createBitmap((int) resultWidth, (int) resultHeight, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas();
                Path clip = new Path();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true); paint.setFilterBitmap(true);

                dialBitmap = Bitmap.createBitmap((int) dialWidth, (int) dialHeight, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(dialBitmap);
                dialBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!

                canvas.save();
                clip.addCircle(dialWidth / 2f, dialHeight / 2f, dialWidth / 2f, Path.Direction.CW);
                canvas.clipPath(clip);
                canvas.drawBitmap(dialIcon, 0f, 0f, null);
                canvas.restore();
                //
                paint.setColor(Color.argb(255, 127, 127, 127));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f);
                canvas.drawPath(clip, paint);

                resultBitmap.eraseColor(Color.BLACK); //Color.TRANSPARENT
                //canvas = new Canvas();
                canvas.setBitmap(resultBitmap);
                canvas.drawBitmap(dialBitmap, (resultWidth - dialWidth) / 2f, (resultHeight - dialHeight) / 2f, null);
                canvas.drawBitmap(frameIcon, 0, 0, null);


                float resultDim = resultBitmap.getWidth();
                int resDim = findClosestMinPowerOf(resultDim, 16f);
                Bitmap resBitmap = Bitmap.createScaledBitmap(resultBitmap, resDim, resDim, true);

                return resBitmap;//resultBitmap;
            }

            int findClosestMinPowerOf(float dim, float powerof) {
                int res = (int) (dim / powerof);
                return (int) (res * powerof);
            }
        }




        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

//            Log.i(TAG, "((((( onAmbientModeChanged: ambient=" + inAmbientMode + ", visible=" + mVisible
//                    + ", mAmbientProp(" + mAmbientProp.isSet() + "): "
//                    + "LowBit=" + mAmbientProp.getLowBit() + ", BurnIn=" + mAmbientProp.getBurnIn());
//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
//            }

            //switchAppearance(mAmbient);

            if (mAmbientProp.getLowBit()) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mTickPaint.setAntiAlias(antiAlias);
                mHrTickPaint.setAntiAlias(antiAlias);
                mHandPaint.setAntiAlias(antiAlias);
            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();

        } // onAmbientModeChanged


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            mVisible = visible;

            //Log.i(TAG, "((((( onVisibilityChanged: visible=" + visible + ", (ambient=" + isInAmbientMode() + ")");

//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "onVisibilityChanged: " + visible);
//            }

            if (visible) {
                //if (!isInAmbientMode()) {
                    mGoogleApiClient.connect();
                //}

                registerReceivers();

                // Update time zone in case it changed while we weren't visible.
                //mTime.clear(TimeZone.getDefault().getID());
                //mTime.setToNow();

                //wTime.setTimeZone(TimeZone.getDefault());
                wTime.checkHandHeldTimeZone(TimeZone.getDefault());
                wTime.set(System.currentTimeMillis());

                // Let send screenshots to handheld service
                appearanceScreenshotTimeMs = 0l;
                ambientAppearanceScreenshotTimeMs = 0L;

                if (mAppPreferences.getSweepSeconds()) invalidate(); // uncomment if sweep ???

            } else {
                unregisterReceivers();

                //System.gc();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            if (!mAppPreferences.getSweepSeconds()) updateTimer(); // comment if sweep ???

        } // onVisibilityChanged

        private void registerReceivers() {
            if (!mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                AWearFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
            }
            registerBatteryReceiver();
            if (true==denseAppearance.mShowRimAnimation) {
                registerSensorsReceiver();
            } else {
                setStaticProjection180dgr();
            }
            registerLocaleReceiver();
        }

        private void registerBatteryReceiver() {
            if (!mRegisteredBatteryReceiver) {
                mRegisteredBatteryReceiver = true;
                AWearFaceService.this.registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }
        private void unregisterBatteryReceiver() {
            if (mRegisteredBatteryReceiver) {
                mRegisteredBatteryReceiver = false;
                AWearFaceService.this.unregisterReceiver(mBatteryInfoReceiver);
            }
        }
        private void registerLocaleReceiver() {
            if (!mRegisteredLocaleReceiver) {
                mRegisteredLocaleReceiver = true;
                AWearFaceService.this.registerReceiver(mLocaleReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
            }
        }
        private void unregisterLocaleReceiver() {
            if (mRegisteredLocaleReceiver) {
                mRegisteredLocaleReceiver = false;
                AWearFaceService.this.unregisterReceiver(mLocaleReceiver);
            }
        }
        private void registerSensorsReceiver() {
            if (mSensMan!=null && !mOrientationSensorListenerRegistered) {
                mOrientationSensorListenerRegistered = true;
                mSensMan.registerListener(mOrientationSensorListener,mSensMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
                mSensMan.registerListener(mOrientationSensorListener,mSensMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_UI);
                //
                //mSensMan.registerListener(mLightSensorListener, mSensMan.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
            }
        }
        private void unregisterSensorsReceiver() {
            if (mSensMan!=null && mOrientationSensorListenerRegistered) {
                mOrientationSensorListenerRegistered = false;
                mSensMan.unregisterListener(mOrientationSensorListener);
                //
                //mSensMan.unregisterListener(mLightSensorListener);
            }
        }

        private void unregisterReceivers() {
            if (mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = false;
                AWearFaceService.this.unregisterReceiver(mTimeZoneReceiver);
            }
            unregisterBatteryReceiver();
            unregisterSensorsReceiver();
            unregisterLocaleReceiver();
        }


        /** Handler to update the time once a second in interactive mode. */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
//                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                            //Log.v(TAG, "updating time");
//                        }
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mTime.clear(intent.getStringExtra("time-zone"));
                //mTime.setToNow();
                wTime.setHandHeldTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                wTime.set(System.currentTimeMillis());
                appearanceScreenshotTimeMs = 0l;
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;
        //
        final BroadcastReceiver mLocaleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wTime.setLocale(Locale.getDefault());
                mFirstDayOfWeek = wTime.getFirstDayOfWeek();
                //mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
                //Log.i(TAG, "((( LOCALE CHANGED, FDOW=" + mFirstDayOfWeek);
            }
        };
        boolean mRegisteredLocaleReceiver = false;

        final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                //mWatchesBattery = level / (float) scale;
                mWatchesBattery = (float) level;
            }
        };
        boolean mRegisteredBatteryReceiver = false;

        private PowerManager.WakeLock mWakeLock;
        Handler mWakeHandler = new Handler();
        Runnable mRunReleaseLock= new Runnable() {
            @Override
            public void run() {
                mWakeLock.release();
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        };
        private void wakeUpScreen(long delay) {
            if (delay==0) return;
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
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
            mWakeHandler.removeCallbacks(mRunReleaseLock);  //if already waiting, then we'll start the time over
            mWakeHandler.postDelayed(mRunReleaseLock, delay);
            //Log.i(TAG, "wakeUpScreen: " + delay);
            appearanceModificationTimeMs = System.currentTimeMillis();
        }


        private void broadcastBundleToConfigActivity(int event, long time, Bundle bundle) {
            Intent intent = new Intent();
            intent.setAction(ACommon.EVENT_ACTION);
            intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
            intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
            intent.putExtra(ACommon.KEY_CFGPAL_CONFIG, bundle);
            LocalBroadcastManager.getInstance(AWearFaceService.this).sendBroadcast(intent);
        }



        // communication with listener service
        // LocalBroadcastManager; registered in Engine.onCreate, unregistered in Engine.onDestroy
        private BroadcastReceiver mDataFromListenerServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int color, strength;
                //boolean trigger;
                if (!action.equals(ACommon.EVENT_ACTION)) return;
                Bundle bundle = intent.getExtras();
                //Log.i(TAG, "onReceive" );
                if (bundle != null) {
                    Bundle pack;
                    int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                    long time = bundle.getLong(ACommon.BCAST_EXTRA_BATTERY_TIME);
//                    Log.i(TAG, "*** broadcast, time=" + time + ", modTime=" + appearanceModificationTimeMs +
//                            ", shotTime=" + appearanceScreenshotTimeMs);
                    switch (event) {
                        case ACommon.EVT_PHONE_BATTERY_SAMPLE:
                            mPhoneBatteryLastSampleTime = bundle.getLong(ACommon.BCAST_EXTRA_BATTERY_TIME);
                            mPhoneBattery = bundle.getFloat(ACommon.BCAST_EXTRA_BATTERY_LEVEL);
                            //Log.i(TAG, "event: BatteryInfo, level=" + mPhoneBattery + ", time=" + mPhoneBatteryLastSampleTime);
                            break;

                        case ACommon.EVT_WAKEUP:
                            long delay = bundle.getLong(ACommon.KEY_DELAY);
                            //Log.i(TAG, "event: Wakeup, delay=" + delay);
                            //
                            //sendWearConfigSettings();
                            //
                            wakeUpScreen(delay);
                            break;
                        //
                        case ACommon.EVT_WAKEUP_AMBIENT_ELEMENT:
                            long delay1 = bundle.getLong(ACommon.KEY_DELAY);
                            //Log.i(TAG, "event: Wakeup AMBIENT, delay=" + delay1);
                            ambientAppearanceScreenshotTimeMs = 0L;
                            if (isVisible()) {
                                postInvalidate();
                            } else {
                                wakeUpScreen(1000);
                            }
                            break;

                        case ACommon.EVT_RESET:
                            denseAppearance = resetConfigToDefaults(false);
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            wakeUpScreen(1000);
                            createConfigFile(true);
                            break;

                        case ACommon.EVT_REQUEST_CURRENT_CONFIG_FOR_FILE:
                            //Log.i(TAG, "### REQUEST_CURRENT_CONFIG_FOR_FILE");
                            appearanceModificationTimeMs = time;
                            wakeUpScreen(1000);
                            sendCurrentConfigForFile();
                            break;
                        case ACommon.EVT_REQUEST_CURRENT_CONFIG:
                            //Log.i(TAG, "### EVT_REQUEST_CURRENT_CONFIG");
                            appearanceModificationTimeMs = time;
                            wakeUpScreen(5000);
                            sendCurrentConfig(null);
                            sendActualWatchfaceValues();
                            //sendTzArray();
                            //Log.i(TAG, "#TZ POST taskSendTzArray");
                            mWakeHandler.postDelayed(taskSendTzArray, 1500); // mUpdateTimeHandler
                            sendPreferences();
                            break;

                        case ACommon.EVT_REQUEST_PLATE_BITMAP:
                            sendPlateBitmap();
                            wakeUpScreen(1000); // to push bitmap though wear network !!!!!!
                            break;

                        case ACommon.EVT_SET_FULL_CONFIG:
                            Bundle config = bundle.getBundle(ACommon.KEY_CFGPAL_CONFIG);
                            boolean val = unBundleConfig(denseAppearance, config, false);
                            //Log.i(TAG, "((( EVT_SET_FULL_CONFIG(" + val + ") = " + config);
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
                            //todo: remove: ACRA.getErrorReporter().reportBuilder().message("REMOVE_ME: SET_FULL_CONFIG").send();
                            break;



                        case ACommon.EVT_REQUEST_WEARCFG_PARAMETERTS:
                            Bundle toggles = new Bundle();
                            toggles.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, mAppPreferences.getShowHandheldBattery());
                            toggles.putBoolean(AppPreferences.KEY_SWEEP_SECONDS, mAppPreferences.getSweepSeconds());
                            toggles.putBoolean(AppPreferences.KEY_RESPECT_BURN_IN, mAppPreferences.getRespectBurnIn());
                            toggles.putBoolean(AppPreferences.KEY_SOURCE, mAppPreferences.isSourceUtc());
                            toggles.putInt(AppPreferences.KEY_HEMISPHERE, mAppPreferences.getTzHemisphere());
                            //
//                            toggles.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, denseAppearance.mShowRimAnimation);
//                            toggles.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, denseAppearance.isShowHrDigitsRelief());
//                            toggles.putBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, denseAppearance.mShowDialGradient);
                            //
                            broadcastBundleToConfigActivity(ACommon.EVT_REPLY_WEARCFG_PARAMETERTS, System.currentTimeMillis(), toggles);
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
                            mAppPreferences.setShowHandheldBattery(getApplicationContext(), !mAppPreferences.getShowHandheldBattery());
                            //denseAppearance.mShowHandheldBattery = !denseAppearance.mShowHandheldBattery;
                            sendWearConfigBooleanToggle(event, ACommon.CFG_SHOW_HANDHELD_BATTERY, mAppPreferences.getShowHandheldBattery());
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_PHONE_BATTERY");
                            break;
                        case ACommon.EVT_HHCFG_SET_PHONE_BATTERY:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            if (mAppPreferences.getShowHandheldBattery() != ((1==color)?true:false)) {
                                mAppPreferences.setShowHandheldBattery(getApplicationContext(), ((1==color)?true:false));
                                //denseAppearance.mShowHandheldBattery = (1==color)?true:false;
                                appearanceModificationTimeMs = time;
                                if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            }
                            break;
                        //

                        case ACommon.EVT_WEARCFG_TOGGLE_TIME_SOURCE:
                            mAppPreferences.toggleTzSourceForWatch();
                            wTime.setWatchSource(mAppPreferences);
                            sendPreferences();
                            break;
                        //

                        case ACommon.EVT_WEARCFG_TOGGLE_RESPECT_BURN_IN:
                            mAppPreferences.setRespectBurnIn(getApplicationContext(), !mAppPreferences.getRespectBurnIn());
                            sendPreferences();
                            mAmbientProp.requestTransition();
                            wakeUpScreen(3000);
                            break;
                        case ACommon.EVT_HHCFG_SET_RESPECT_BURNIN:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            //mRespectBurnIn = (1==color)?true:false;
                            //mNeedInitDrawingAssets = true;
                            //mAmbientProp.setRespectBurnIn(getApplicationContext(), (1==color)?true:false);
                            mAppPreferences.setRespectBurnIn(getApplicationContext(), (1==color)?true:false);
                            mAmbientProp.requestTransition();
                            wakeUpScreen(3000);
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_RESPECT_BURNIN = " + ((1==color)?true:false));
                            break;
                        //

                        case ACommon.EVT_WEARCFG_TOGGLE_SWEEP_SECONDS:
                            mAppPreferences.setSweepSeconds(getApplicationContext(), !mAppPreferences.getSweepSeconds());
                            sendPreferences();
                            break;
                        case ACommon.EVT_HHCFG_SET_SWEEP:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            //mIsSweep = ((1==color)?true:false);
                            mAppPreferences.setSweepSeconds(getApplicationContext(), ((1==color)?true:false));
                            if (mAppPreferences.getSweepSeconds()) {
                                if (mVisible) postInvalidate();
                            } else {
                                updateTimer();
                            }
                            //wakeUpScreen(3000);
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_SWEEP = " + ((1==color)?true:false));
                            wakeUpScreen(1000);
                            break;
                        //

                        case ACommon.EVT_WEARCFG_TOGGLE_ANIMATION:
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_ANIMATION");
                            denseAppearance.mShowRimAnimation = !denseAppearance.mShowRimAnimation;
                            if (false == denseAppearance.mShowRimAnimation) {
                                unregisterSensorsReceiver();
                                setStaticProjection180dgr();
                            } else {
                                if (true == mVisible) registerSensorsReceiver();
                            }
                            sendWearConfigBooleanToggle(event, ACommon.CFG_SHOW_RIM_ANIMATION, denseAppearance.mShowRimAnimation);
                            break;
                        case ACommon.EVT_HHCFG_SET_RIM_ANIMATION:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mShowRimAnimation = (1==color)?true:false;
                            if (false == denseAppearance.mShowRimAnimation) {
                                unregisterSensorsReceiver();
                                setStaticProjection180dgr();
                            } else {
                                if (true == mVisible) registerSensorsReceiver();
                            }
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF:
                            denseAppearance.setShowHrDigitsRelief(!denseAppearance.isShowHrDigitsRelief());
                            sendWearConfigBooleanToggle(event, ACommon.CFG_SHOW_HRDIGITS_RELIEF, denseAppearance.isShowHrDigitsRelief());
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF");
                            break;
                        case ACommon.EVT_HHCFG_SET_HRDIGITS_RELIEF:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.setShowHrDigitsRelief((1==color)?true:false);
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        //
                        case ACommon.EVT_HHCFG_SET_INSCRIPTIONS_RELIEF:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.setShowInscriptionsRelief((1 == color) ? true : false);
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        //
                        case ACommon.EVT_WEARCFG_TOGGLE_DIAL_GRADIENT:
                            denseAppearance.mShowDialGradient = !denseAppearance.mShowDialGradient;
                            changeBackgroundColor(denseAppearance);
                            changeBigAuxDialColor(denseAppearance);
                            changeSmallAuxDialColor(denseAppearance);
                            sendWearConfigBooleanToggle(event, ACommon.CFG_SHOW_DIAL_GRADIENT, denseAppearance.mShowDialGradient);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "=== EVT_WEARCFG_TOGGLE_DIAL_GRADIENT");
                            break;
                        case ACommon.EVT_HHCFG_SET_DIAL_GRADIENT:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mShowDialGradient = (1==color)?true:false;
                            changeBackgroundColor(denseAppearance);
                            changeBigAuxDialColor(denseAppearance);
                            changeSmallAuxDialColor(denseAppearance);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        case ACommon.EVT_HHCFG_SET_DIAL_GRADIENT_PACK:
                            //Log.i(TAG, "^^^^^ bundle=" + bundle);
                            pack = bundle.getBundle(ACommon.KEY_GRADIENT_PACK);
                            //Log.i(TAG, "^^^^^ pack=" + pack);
                            denseAppearance.dgEdgeAlpha = pack.getInt(ACommon.CFG_DG_EDGE_ALPHA);
                            denseAppearance.dgFirstStop = pack.getFloat(ACommon.CFG_DG_FIRST_STOP);
                            denseAppearance.dgHalfEdgeStop = pack.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP);
                            denseAppearance.dgEdgeAlpha1 = pack.getInt(ACommon.CFG_DG_EDGE_ALPHA_1);
                            denseAppearance.dgFirstStop1 = pack.getFloat(ACommon.CFG_DG_FIRST_STOP_1);
                            denseAppearance.dgHalfEdgeStop1 = pack.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1);
                            denseAppearance.dgInvert = pack.getInt(ACommon.CFG_INVERT_GRADIENT);
//                            Log.i(TAG, "^^^^^ dgEdgeAlpha=" + denseAppearance.dgEdgeAlpha + ", dgFirstStop=" +
//                                    denseAppearance.dgFirstStop + ", dgHalfEdgeStop=" + denseAppearance.dgHalfEdgeStop);
                            //
                            changeBackgroundColor(denseAppearance);
                            changeBigAuxDialColor(denseAppearance);
                            changeSmallAuxDialColor(denseAppearance);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        case ACommon.EVT_HHCFG_SET_PLATE_TEXTURE_PACK:
                            pack = bundle.getBundle(ACommon.KEY_PLATE_TEXTURE_PACK);
                            denseAppearance.mFxPlateTextureStrength = pack.getFloat(
                                    ACommon.CFG_PLATE_TEXTURE_STRENGTH, WatchAppearance.DEFAULT_PLATE_TEXTURE_STRENGTH);
                            denseAppearance.mFxAuxDialTextureStrength = pack.getFloat(
                                    ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH, WatchAppearance.DEFAULT_AUXDIAL_TEXTURE_STRENGTH);
                            denseAppearance.mFxPlateReliefStrength = pack.getInt(
                                    ACommon.CFG_PLATE_RELIEF_STRENGTH, WatchAppearance.DEFAULT_PLATE_RELIEF_STRENGTH);
                            //
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
                            break;

                        case ACommon.EVT_HHCFG_SET_INSCRIPTIONS_PACK:
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_INSCRIPTIONS_PACK, bundle=" + bundle);
                            pack = bundle.getBundle(ACommon.KEY_INSCRIPTIONS_PACK);
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_INSCRIPTIONS_PACK, pack=" + pack);
                            Inscription.unBundleInscription(pack, denseAppearance.mPrint);
                            //
                            strength = pack.getInt(ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH, -1);
                            if (-1 != strength) denseAppearance.mInscriptionsReliefStrength = strength;
                            else denseAppearance.mInscriptionsReliefStrength = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH;
                            //
                            denseAppearance.mDialPlateReady = false;
                            wakeUpScreen(1000);
                            sendCurrentConfig(null);
//                            sendActualWatchfaceValues();
                            break;

                        case ACommon.EVT_HHCFG_SET_TIMEZONE_PACK:
                            pack = bundle.getBundle(ACommon.KEY_TIMEZONE_PACK);
                            AppPreferences appPreferences = AppPreferences.unBundlePreferences(pack);
                            boolean needRedraw = mAppPreferences.getTzHemisphere() != appPreferences.getTzHemisphere();
                            mAppPreferences = appPreferences;
                            mAppPreferences.save(getApplicationContext());
                            //sendTzArray();
                            //Log.i(TAG, "#TZ POST taskSendTzArray");
                            mWakeHandler.postDelayed(taskSendTzArray, 1500);
                            sendPreferences();
                            if (needRedraw) {
                                mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                                wakeUpScreen(5000);
                            } else {
                                denseAppearance.mDialPlateReady = false;
                                appearanceModificationTimeMs = time;
                                if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                                //wakeUpScreen(1000);
                            }
                            wTime.eventTzIndicationUpdated(mAppPreferences);
                            //Log.i(TAG, "((((( TIMEZONE_PACK = " + pack);
                            break;

                        case ACommon.EVT_HHCFG_SET_TZ_HEMISPHERE:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            //needRedraw = mAppPreferences.getTzHemisphere() != color;
                            if (mAppPreferences.getTzHemisphere() != color) {
                                mAppPreferences.setTzHemisphere(color);
                                mAppPreferences.save(getApplicationContext());
                                sendPreferences();
                                mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                                wakeUpScreen(5000);
                            } else {
                                denseAppearance.mDialPlateReady = false;
                                appearanceModificationTimeMs = time;
                                if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            }
                            break;
                        case ACommon.EVT_WEARCFG_TOGGLE_TZ_HEMISPHERE:
                            mAppPreferences.toggleTzHemisphere();
                            mAppPreferences.save(getApplicationContext());
                            sendPreferences();
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
                            break;

                        case ACommon.EVT_HHCFG_SET_HOUR_MARKS:
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_HOUR_MARKS, bundle=" + bundle);
                            pack = bundle.getBundle(ACommon.KEY_HOUR_MARKS);
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_HOUR_MARKS, pack=" + pack);
                            long longArr[] = pack.getLongArray(ACommon.CFG_HOUR_MARKS);
                            if (null != longArr) denseAppearance.mHourMarksIndex = longArr;
                            long longArr2[] = pack.getLongArray(ACommon.CFG_HOUR_MARKS_RELIEF);
                            if (null != longArr2) denseAppearance.mHourMarksReliefIndex = longArr2;
                            strength = pack.getInt(ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH, -1);
                            if (-1 != strength) denseAppearance.mHourMarksReliefStrength = strength;
                            else denseAppearance.mHourMarksReliefStrength = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH;
                            if (null != longArr || null != longArr2 || -1 != strength) {
                                sendCurrentConfig(null);
//                                for (int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) {
//                                    Log.i(TAG, "((( hrmark[" + i + "] = " + denseAppearance.mHourMarksIndex[i]);
//                                }
//                                denseAppearance.mDialPlateReady = false;
//                                wakeUpScreen(1000);
                                mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                                wakeUpScreen(5000);
                            }
                            break;

                        case ACommon.EVT_HHCFG_SET_DEMO_PACK:
                            if (null == (pack = bundle.getBundle(ACommon.KEY_DEMO_PACK))) break;
                            boolean prevActive = DemoPackData.isActive(demoPackData);
                            int prevResolution = DemoPackData.getResolution(demoPackData);
                            //
                            long[] triggers = pack.getLongArray(ACommon.CFG_DEMOPACK_TRIGGERS);
                            long[] values = pack.getLongArray(ACommon.CFG_DEMOPACK_VALUES);
                            if (null != triggers || null != values) {
//                                String logMsg = String.format("Triggers=%s. Values=%s.", Arrays.toString(triggers), Arrays.toString(values));
//                                Log.i(TAG, "((( DEMO_PACK: " + logMsg);
                                for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
                                    demoPackData[i].trigger = (1 == triggers[i]) ? true : false;
                                    demoPackData[i].value = (int) values[i];
                                }
                                wTime.putDemoPack(demoPackData);
                                //
                                //if (DemoPackData.isActive(demoPackData) != prevActive) {
                                    if (DemoPackData.isActive(demoPackData) == true) {
                                        if (DemoPackData.RESOLUTION_NATURAL != DemoPackData.getResolution(demoPackData)) {
                                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                                        }
                                    } else {
                                        if (DemoPackData.RESOLUTION_NATURAL != prevResolution && prevActive) {
                                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                                        }
                                    }
                                //}
                                wakeUpScreen(5000);
                            }
                            break;


                        case ACommon.EVT_HHCFG_SET_AUX_BEVEL_COLOR:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mAuxBevelColor = color;
                            denseAppearance.mDialPlateReady = false;
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        // CFG_RESPECT_LOWBIT   EVT_HHCFG_SET_RESPECT_LOWBIT    EVT_WEARCFG_SET_RESPECT_LOWBIT
                        // CFG_SWEEP_SECONDS    EVT_HHCFG_SET_SWEEP             EVT_WEARCFG_SET_SWEEP
                        case ACommon.EVT_HHCFG_SET_RESPECT_LOWBIT:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            mAmbientProp.mRespectLowBit = ((1==color)?true:false);
                            //wakeUpScreen(3000);
                            //Log.i(TAG, "((((( EVT_HHCFG_SET_RESPECT_LOWBIT = " + ((1==color)?true:false));
                            break;

                        case ACommon.EVT_HHCFG_COLORIZE_BURNIN_MARGIN:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mColorizeBurnInMargin = ((1==color)?true:false);
                            denseAppearance.mDialPlateReady = false;
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;





                        case ACommon.EVT_WEARCFG_TOGGLE_LAYOUT:
                            //Log.i(TAG, "#TOGGLE_LAYOUT broadcast received");
                            sendToggleLayoutRequest();
                            break;






                        case ACommon.EVT_COLOR_DIGITS:
                            int colorD = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainDigitsColor = 0xff000000 | colorD;
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: DigitsColor, color=" + colorD);
                            break;

                        case ACommon.EVT_COLOR_HOURMARK_OUTLINE:
                            colorD = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainHourMarkOutlineColor = colorD;
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        case ACommon.EVT_COLOR_DOM_BACK:
                            denseAppearance.mMainDomBackColor = 0xff000000 | bundle.getInt(ACommon.KEY_COLOR);
                            //Log.i(TAG, "event: DomBackColor, color=" + denseAppearance.mMainDomBackColor);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_DOM_FRONT:
                            //mMainDomFrontColor = 0xff000000 | bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainDomFrontColor = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: DomFrontColor, color=" + denseAppearance.mMainDomFrontColor);
                            break;


                        case ACommon.EVT_COLOR_DOM_FRAME:
                            denseAppearance.mMainDomFrameColor = bundle.getInt(ACommon.KEY_COLOR);
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_DOM_FRAME:
                            denseAppearance.mAmbientDomFrameColor = bundle.getInt(ACommon.KEY_COLOR);
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
                            break;


                        case ACommon.EVT_COLOR_HOUR_HAND:
                            int colorH = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainHourHandColor = colorH;
                            //mMainMinuteHandColor = colorH;
                            appearanceModificationTimeMs = time;
                            changeHourHandDecorColor(denseAppearance.mMainHourHandColor, false);
                            //changeUpperDecorColor(denseAppearance.mMainHourHandColor, false);
                            //denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Hour Hand Color = " + colorH);
                            break;
                        case ACommon.EVT_COLOR_MINUTE_HAND:
                            int colorM = bundle.getInt(ACommon.KEY_COLOR);
                            //mMainHourHandColor = colorH;
                            denseAppearance.mMainMinuteHandColor = colorM;
                            appearanceModificationTimeMs = time;
                            changeMinuteHandDecorColor(denseAppearance.mMainMinuteHandColor, false);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Minute Hand Color = " + colorM);
                            break;

                        case ACommon.EVT_COLOR_MAIN_TICK:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainTickColor = color;
                            mTickPaint.setColor(denseAppearance.mMainTickColor);
                            //Log.i(TAG, "((( mTickPaint #2 color=" + mTickPaint.getColor());
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Minute's Ticks Color = " + color);
                            break;
                        case ACommon.EVT_COLOR_MAIN_TICKDIGIT:
                            color = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainTickDigitColor = color;
                            mTickDigitPaint.setColor(denseAppearance.mMainTickDigitColor);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Minute's Digits Color = " + color);
                            break;

                        case ACommon.EVT_COLOR_HANDS:
                            int colorB = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainHourHandColor = colorB;
                            denseAppearance.mMainMinuteHandColor = colorB;
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Main Hands Color = " + colorB);
                            break;

                        //mMainCalendarDialBackgroundColor  CFG_MAIN_BIGAUX_BACKGROUND_COLOR    EVT_COLOR_BIGAUX_BACKGROUND
                        //mMainCalendarDialDigitsColor      CFG_MAIN_BIGAUX_DIGITS_COLOR        EVT_COLOR_BIGAUX_DIGITS
                        //mMainCalendarDialTicksColor       CFG_MAIN_BIGAUX_TICKS_COLOR         EVT_COLOR_BIGAUX_TICKS
                        case ACommon.EVT_COLOR_BIGAUX_BACKGROUND:
                            denseAppearance.mMainCalendarDialBackgroundColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            changeBigAuxDialColor(denseAppearance);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_BIGAUX_DIGITS:
                            denseAppearance.mMainCalendarDialDigitsColor = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_BIGAUX_TICKS:
                            denseAppearance.mMainCalendarDialTicksColor = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        // mMainSmallAuxDialBackgroundColor     CFG_MAIN_SMALLAUX_BACKGROUND_COLOR  EVT_COLOR_SMALLAUX_BACKGROUND_COLOR
                        // mMainSmallAuxDialDigitsColor         CFG_MAIN_SMALLAUX_DIGITS_COLOR      EVT_COLOR_SMALLAUX_DIGITS_COLOR
                        // mMainSmallAuxDialTick1Color          CFG_MAIN_SMALLAUX_TICKS1_COLOR      EVT_COLOR_SMALLAUX_TICKS1_COLOR
                        // mMainSmallAuxDialTick2Color          CFG_MAIN_SMALLAUX_TICKS2_COLOR      EVT_COLOR_SMALLAUX_TICKS2_COLOR
                        case ACommon.EVT_COLOR_SMALLAUX_BACKGROUND_COLOR:
                            denseAppearance.mMainSmallAuxDialBackgroundColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            changeSmallAuxDialColor(denseAppearance);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_SMALLAUX_DIGITS_COLOR:
                            denseAppearance.mMainSmallAuxDialDigitsColor = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_SMALLAUX_TICKS1_COLOR:
                            denseAppearance.mMainSmallAuxDialTick1Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_SMALLAUX_TICKS2_COLOR:
                            denseAppearance.mMainSmallAuxDialTick2Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;

                        // mMainAuxHandWeekdayColor   CFG_MAIN_AUXHANDS_WEEKDAY_COLOR  EVT_COLOR_AUXHANDS_WEEKDAY_COLOR
                        case ACommon.EVT_COLOR_AUXHANDS_WEEKDAY_COLOR:
                            denseAppearance.mMainAuxHandWeekdayColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            changeAuxHandWeekdayColor(denseAppearance.mMainAuxHandWeekdayColor, false);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
// mMainAuxHandWearBattColor   CFG_MAIN_AUXHANDS_WEARBATT_COLOR     EVT_COLOR_AUXHANDS_WEARBATT_COLOR
// mMainAuxHandPhoneBattColor  CFG_MAIN_AUXHANDS_PHONEBATT_COLOR    EVT_COLOR_AUXHANDS_PHONEBATT_COLOR
// mMainAuxHandMonthColor      CFG_MAIN_AUXHANDS_MONTH_COLOR        EVT_COLOR_AUXHANDS_MONTH_COLOR
                        case ACommon.EVT_COLOR_AUXHANDS_WEARBATT_COLOR:
                            denseAppearance.mMainAuxHandWearBattColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            changeAuxHandWearBattColor(denseAppearance.mMainAuxHandWearBattColor, false);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AUXHANDS_PHONEBATT_COLOR:
                            denseAppearance.mMainAuxHandPhoneBattColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            changeAuxHandPhoneBattColor(denseAppearance.mMainAuxHandPhoneBattColor, false);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AUXHANDS_MONTH_COLOR:
                            denseAppearance.mMainAuxHandMonthColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            changeAuxHandMonthColor(denseAppearance.mMainAuxHandMonthColor, false);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
// mAmbientDigitsColor              CFG_AMBIENT_DIGITS_COLOR        EVT_COLOR_AMBIENT_DIGITS            ff
// mAmbientHourHandColor            CFG_AMBIENT_HOURHAND_COLOR      EVT_COLOR_AMBIENT_HOURHAND
// mAmbientMinuteHandColor          CFG_AMBIENT_MINUTEHAND_COLOR    EVT_COLOR_AMBIENT_MINUTEHAND
// mAmbientTicksColor               CFG_AMBIENT_TICK_COLOR          EVT_COLOR_AMBIENT_TICK
// mAmbientTickDigitColor           CFG_AMBIENT_TICKDIGIT_COLOR     EVT_COLOR_AMBIENT_TICKDIGIT
// mAmbientDomAndAuxHandsColor      CFG_AMBIENT_DOM_AUXHANDS_COLOR  EVT_COLOR_AMBIENT_DOM_AUXHANDS      ff
//                        private void setAppearanceColors(WatchAppearance appearance) {
//                            changeBackgroundColor(appearance.mMainBackgroundColor);
//                            changeMainHandsOutlineColor(appearance.mMainMainHandsColor);
//                            changeSecondsHandColor(appearance.mMainSecondsHandColor);
//                            mTickPaint.setColor(appearance.mMainTickColor);
//                            mTickDigitPaint.setColor(appearance.mMainTickDigitColor);
//                            //
//                            changeAuxHandWeekdayColor(appearance.mMainAuxHandWeekdayColor);
//                            //
//                            changeAuxHandWearBattColor(appearance.mMainAuxHandWearBattColor);
//                            changeAuxHandPhoneBattColor(appearance.mMainAuxHandPhoneBattColor);
//                            changeAuxHandMonthColor(appearance.mMainAuxHandMonthColor);
//                        }


                        case ACommon.EVT_COLOR_DECORUPPER:
                            colorH = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mMainDecorUpperColor = colorH;
                            appearanceModificationTimeMs = time;
                            changeUpperDecorColor(denseAppearance.mMainDecorUpperColor, false);
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            //Log.i(TAG, "event: Decor upper Color = " + colorH);
                            break;




                        case ACommon.EVT_COLOR_AMBIENT_DECORUPPER:
                            denseAppearance.mAmbientDecorUpperColor = bundle.getInt(ACommon.KEY_COLOR); // | 0xff000000;
                            //currentAppearance.mAmbientDigitsColor = denseAppearance.mAmbientDigitsColor;
                            checkAmbientAppearanceModification();
                            changeUpperDecorColor(denseAppearance.mAmbientDecorUpperColor, true);
                            break;

                        case ACommon.EVT_COLOR_AMBIENT_DIGITS:
                            denseAppearance.mAmbientDigitsColor = bundle.getInt(ACommon.KEY_COLOR); // | 0xff000000;
                            //currentAppearance.mAmbientDigitsColor = denseAppearance.mAmbientDigitsColor;
                            checkAmbientAppearanceModification();
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_HOURHAND:
                            denseAppearance.mAmbientHourHandColor = bundle.getInt(ACommon.KEY_COLOR);
                            //currentAppearance.mAmbientHourHandColor = denseAppearance.mAmbientHourHandColor;
                            checkAmbientAppearanceModification();
                            changeHourHandDecorColor(denseAppearance.mAmbientHourHandColor, true);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_MINUTEHAND:
                            denseAppearance.mAmbientMinuteHandColor = bundle.getInt(ACommon.KEY_COLOR);
                            //currentAppearance.mAmbientMinuteHandColor = denseAppearance.mAmbientMinuteHandColor;
                            checkAmbientAppearanceModification();
                            changeMinuteHandDecorColor(denseAppearance.mAmbientMinuteHandColor, true);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TICK:
                            denseAppearance.mAmbientTicksColor = bundle.getInt(ACommon.KEY_COLOR);
                            //currentAppearance.mAmbientTicksColor = denseAppearance.mAmbientTicksColor;
                            checkAmbientAppearanceModification();
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TICKDIGIT:
                            denseAppearance.mAmbientTickDigitColor = bundle.getInt(ACommon.KEY_COLOR);
                            //currentAppearance.mAmbientTickDigitColor = denseAppearance.mAmbientTickDigitColor;
                            checkAmbientAppearanceModification();
                            break;
                        //
                        case ACommon.EVT_COLOR_AMBIENT_DOM_AUXHANDS:
                            //denseAppearance.mAmbientDomAndAuxHandsColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            denseAppearance.mAmbientAuxHandsColor = bundle.getInt(ACommon.KEY_COLOR);
                            denseAppearance.mAmbientDomBackColor = denseAppearance.mAmbientAuxHandsColor;
                            denseAppearance.mAmbientDomFrontColor = 0xff000000;
                            checkAmbientAppearanceModification();
                            changeAuxHandMonthColor(denseAppearance.mAmbientAuxHandsColor, true);
                            changeAuxHandWearBattColor(denseAppearance.mAmbientAuxHandsColor, true);
                            changeAuxHandWeekdayColor(denseAppearance.mAmbientAuxHandsColor, true);
                            break;
                        //
                        case ACommon.EVT_COLOR_AMBIENT_DOMBACK:
                            denseAppearance.mAmbientDomBackColor = bundle.getInt(ACommon.KEY_COLOR);
                            checkAmbientAppearanceModification();
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_DOMFRONT:
                            denseAppearance.mAmbientDomFrontColor = bundle.getInt(ACommon.KEY_COLOR);
                            checkAmbientAppearanceModification();
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_AUXHANDS:
                            denseAppearance.mAmbientAuxHandsColor = bundle.getInt(ACommon.KEY_COLOR);
                            checkAmbientAppearanceModification();
                            changeAuxHandMonthColor(denseAppearance.mAmbientAuxHandsColor, true);
                            changeAuxHandWearBattColor(denseAppearance.mAmbientAuxHandsColor, true);
                            changeAuxHandWeekdayColor(denseAppearance.mAmbientAuxHandsColor, true);
                            break;




                        case ACommon.EVT_COLOR_INSCRIPTION_1:
                            denseAppearance.mMainInscription_1_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_2:
                            denseAppearance.mMainInscription_2_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_3:
                            denseAppearance.mMainInscription_3_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_4:
                            denseAppearance.mMainInscription_4_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_5:
                            denseAppearance.mMainInscription_5_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_6:
                            denseAppearance.mMainInscription_6_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_INSCRIPTION_7:
                            denseAppearance.mMainInscription_7_Color = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;



                        case ACommon.EVT_COLOR_BACKGROUND:
                            denseAppearance.mMainBackgroundColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            //Log.i(TAG, "event: BackgroundColor, color=" + denseAppearance.mMainBackgroundColor);
                            mAmbientProp.requestTransition(); //mAmbientProp.getBurnIn()
                            wakeUpScreen(5000);
//                            changeBackgroundColor(denseAppearance);
//                            appearanceModificationTimeMs = time;
//                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_MAINHANDS:
                            denseAppearance.mMainMainHandsColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            //Log.i(TAG, "event: MainHandsColor, color=" + denseAppearance.mMainMainHandsColor);
                            changeMainHandsOutlineColor(denseAppearance.mMainMainHandsColor);
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_SECONDSHAND:
                            denseAppearance.mMainSecondsHandColor = bundle.getInt(ACommon.KEY_COLOR) | 0xff000000;
                            appearanceModificationTimeMs = time;
                            //Log.i(TAG, "event: SecondsHandColor, color=" + denseAppearance.mMainSecondsHandColor);
                            changeSecondsHandColor(denseAppearance.mMainSecondsHandColor);
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;


                        case ACommon.EVT_COLOR_MAIN_TZ_SCRIPTS:
                            denseAppearance.mMainTzScripts = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_MAIN_TZ_CIRCLES:
                            denseAppearance.mMainTzCircles = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_MAIN_TZ_SIGN:
                            denseAppearance.mMainTzSign = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_MAIN_TZ_POINT:
                            denseAppearance.mMainTzPoint = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TZ_SCRIPTS:
                            denseAppearance.mAmbientTzScripts = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TZ_CIRCLES:
                            denseAppearance.mAmbientTzCircles = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TZ_SIGN:
                            denseAppearance.mAmbientTzSign = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_COLOR_AMBIENT_TZ_POINT:
                            denseAppearance.mAmbientTzPoint = bundle.getInt(ACommon.KEY_COLOR);
                            appearanceModificationTimeMs = time;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;


                        case ACommon.EVT_CHLAYOUT_DATE:
                            denseAppearance.watchDomIndex++;
                            appearanceModificationTimeMs = time;
                            if (denseAppearance.watchDomIndex >= ACommon.NUM_DOM_VARIANTS) denseAppearance.watchDomIndex = 0;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
//                            mAmbientProp.requestTransition(mAmbientProp.getBurnIn());
//                            wakeUpScreen(5000);
                            break;
                        case ACommon.EVT_CHLAYOUT_MAINHANDS:
                            denseAppearance.watchMainHandsIndex++;
                            appearanceModificationTimeMs = time;
                            if (denseAppearance.watchMainHandsIndex >= ACommon.NUM_MAIN_HAND_SETS) denseAppearance.watchMainHandsIndex = 0;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
                            break;
                        case ACommon.EVT_CHLAYOUT:
                            denseAppearance.watchLayoutIndex++;
                            appearanceModificationTimeMs = time;
                            ambientAppearanceModificationTimeMs = time;
                            if (denseAppearance.watchLayoutIndex >= watchLayouts.length) denseAppearance.watchLayoutIndex = 0;
                            denseAppearance.mDialPlateReady = false;
                            if (appearanceModificationTimeMs > appearanceScreenshotTimeMs) wakeUpScreen(1000);
//                            mAmbientProp.requestTransition(mAmbientProp.getBurnIn());
//                            wakeUpScreen(5000);
                            break;
                        default:
                            break;
                    }
                }
            } // mDataFromListenerServiceReceiver.onReceive
        };



        private void checkAmbientAppearanceModification() {
            //Log.i(TAG, "((( checkAmbientAppearanceModification");
            ambientAppearanceModificationTimeMs = System.currentTimeMillis();
            denseAppearance.mDialPlateReady = false;
            if (!isVisible()) {
                wakeUpScreen(1000);
            } else {
                //if (isInAmbientMode()) switchAppearance(true);
                postInvalidate();
            }
        } // checkAmbientAppearanceModification



        private final SensorEventListener mLightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int type = event.sensor.getType();
                if (type==Sensor.TYPE_LIGHT) {
                    mSensorLight = event.values[0];
                    if (mSensorLight > mSensorLightMaxValue) {
                        mSensorLightMaxValue = mSensorLight;
                        // todo: for correct calibration purpose it need to save mSensorLightMaxValue in
                        // local file + add function for users to clear accumulated data
                        //Log.i(TAG, "((((( new mSensorLightMaxValue=" + mSensorLightMaxValue);
                    }
                    //Log.i(TAG, "(( sensor light=" + mSensorLight + ", (%)=" + (mSensorLight / mSensorLightMaxValue) * 100F);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //Log.i(TAG, "((((( light sensor onAccuracyChanged, accuracy=" + accuracy);
            }
        };
        float mSensorLight = 0F;
        float mSensorLightMaxValue = 100F;

        private final SensorEventListener mOrientationSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int type = event.sensor.getType();
                if (type==Sensor.TYPE_MAGNETIC_FIELD) magFlds = event.values.clone();
                if (type==Sensor.TYPE_ACCELEROMETER) accels = event.values.clone();
                //SensorManager.getInclination(inclinationMatr);
                //SensorManager.getRotationMatrix(rotationMatr,inclinationMatr,accels,magFlds);
                SensorManager.getRotationMatrix(rotationMatr,null,accels,magFlds);
                //SensorManager.getRotationMatrix(rotationMatr,inclinationMatr,accels,magFlds);
                //mInclinationRad = SensorManager.getInclination(inclinationMatr);
                SensorManager.getOrientation(rotationMatr,attitude);
                mAzimuthRad = attitude[0];
                mPitchRad = attitude[1];
                mRollRad = attitude[2];
                mAzimuthDeg = attitude[0] * RAD2DEG;
                mPitchDeg = attitude[1] * RAD2DEG;
                mRollDeg = attitude[2] * RAD2DEG;
                calcOrientationProjection();
                //invalidate();
                //postInvalidate();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        boolean mOrientationSensorListenerRegistered = false;
        float[] magFlds = new float[3];
        float[] accels = new float[3];
        float[] rotationMatr = new float[/*9*/16];
        float[] inclinationMatr = new float[/*9*/16];
        float[] attitude = new float[3];
        final static float RAD2DEG = (float) (180f/Math.PI);
        float mAzimuthDeg, mPitchDeg, mRollDeg;
        float mAzimuthRad, mPitchRad, mRollRad;
        float mInclinationRad;

        float mShdStickHeight = 100f;//100f;
        float mPrjR, mPrjP;
        float mPrjAngle, mPrjAngleNorm;
        float mPrjLen, mPrjLenNorm;


        private void calcOrientationProjection() {
            //shadow of stick
            mPrjR = (float) (mShdStickHeight * Math.sin(mPitchRad));
            mPrjR = -mPrjR;
            mPrjP = (float) (mShdStickHeight * Math.cos(mPitchRad) * Math.sin(mRollRad));
            // Math.atan((-Math.sin(mPitchRad)) / (Math.cos(mPitchRad) * Math.sin(mRollRad)))
            mPrjAngle = (float) Math.atan(mPrjR / mPrjP);
            mPrjLen = (float) (mPrjP / Math.cos(mPrjAngle));
            mPrjLenNorm = (mPrjP<0)?-mPrjLen:mPrjLen;
            if (mPrjR < 0f) {
                if (mPrjP < 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2*3 + mPrjAngle);
                } else if (mPrjP > 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2 + mPrjAngle);
                } else {
                    mPrjAngleNorm = 0f;
                }
            } else if (mPrjR > 0f) {
                if (mPrjP < 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2*3 + mPrjAngle);
                } else if (mPrjP > 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2 + mPrjAngle);
                } else {
                    mPrjAngleNorm = (float) (Math.PI);
                }
            } else {
                if (mPrjP < 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2*3);
                } else if (mPrjP > 0f) {
                    mPrjAngleNorm = (float) (Math.PI/2);
                } else {
                    mPrjAngleNorm = 0f;
                }
            }
        } //calcOrientationProjection


        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            //Log.i(TAG, "((((( updateTimer");
//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG, "((((( updateTimer");
//            }
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            //return isVisible() && !isInAmbientMode();
            return isVisible() && !isInAmbientMode() && !mAppPreferences.getSweepSeconds();
        }


        Runnable taskSendActualWatchfaceValues = new Runnable() {
            @Override
            public void run() {
                mLockDrawingAssets.lock(); try {
                    sendActualWatchfaceValues();
                } finally {
                    mLockDrawingAssets.unlock();
                }
            }
        };

        Runnable taskSendCurrentConfig = new Runnable() {
            @Override
            public void run() {
                sendCurrentConfig(null);
            }
        };

    } // class Engine

} // class AWearFaceService
