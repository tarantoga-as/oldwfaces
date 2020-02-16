/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.AppPreferences;
import com.luna_78.wear.watch.face.lightandclassics.common.BroadcastFromWearListener;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;
import com.luna_78.wear.watch.face.lightandclassics.common.DelayedOps;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearEvent;
import com.luna_78.wear.watch.face.lightandclassics.common.SynBoolean;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;
import com.luna_78.wear.watch.face.lightandclassics.common.WatchProperties;
import com.luna_78.wear.watch.face.lightandclassics.common.WatchTime;
import com.luna_78.wear.watch.face.lightandclassics.common.WearDataSender;

import org.acra.ACRA;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class AWearFaceService extends CanvasWatchFaceService {

    private static class EngineHandler extends Handler {
        private final WeakReference<AWearFaceService.Engine> mWeakReference;

        public EngineHandler(AWearFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            AWearFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "WATCH_FACE";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine
        implements
            OnWearEvent
//            GoogleApiClient.ConnectionCallbacks,
//            GoogleApiClient.OnConnectionFailedListener,
//            DataApi.DataListener
    {


                    Paint mBackgroundPaint;
                    Paint mHandPaint;
                    //Time mTime;



        final Handler mUpdateTimeHandler = new EngineHandler(this);


        Paint                   mCommonPaint        = new Paint(); //Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG
        Matrix                  mCommonMatrix       = new Matrix();
        AppPreferences          mAppPreferences;
        WatchTime               wTime;
        ElementsSVG             dialElements        = new ElementsSVG();
        boolean                 mVisible;
        boolean                 mAmbient;
        boolean                 mMute;
//        GoogleApiClient         mGoogleApiClient    = new GoogleApiClient.Builder(AWearFaceService.this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Wearable.API)
//                .build();
        long                    appearanceScreenshotTimeMs              = 0L; // dense
        long                    appearanceModificationTimeMs            = 0L; // dense
        long                    ambientAppearanceModificationTimeMs     = 0L; // ambient
        long                    ambientAppearanceScreenshotTimeMs       = 0L; // ambient
        volatile int            mFirstDayOfWeek             = 2;
        float                   mWatchesBattery             = 0f;
        float                   mPhoneBattery               = 0f;
        long                    mPhoneBatteryLastSampleTime = 0L;
        boolean mRegisteredTimeZoneReceiver = false;
        boolean mRegisteredLocaleReceiver = false;
        boolean mRegisteredBatteryReceiver = false;
        boolean mRegisteredOrientationSensorListener = false;
        Path mGearRotated = new Path();
        public String           productId;
        volatile SynBoolean     mReadyToShow        = new SynBoolean(false);
        WatchProperties         mWatchProp          = new WatchProperties();

        private String              mPeerId;
        WearDataSender              mWearPAN;
        //DelayedOps                  mBuzzer = new DelayedOps(new Handler());
        public String               productIdServiceCap, productIdWatchFaceCap;
        WearDataForWatchFace        mWearWorker;
        BroadcastFromWearListener   mWearDataFromListenerReceiver;









        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mWatchProp.setInsetsValues(insets.isRound(), insets.getSystemWindowInsetBottom());
            //if (L) Log.i(TAG, "");
        }


        private void createPaints() {
            //Resources resources = AWearFaceService.this.getResources();
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK); //resources.getColor(R.color.analog_background)
            mHandPaint = new Paint();
            mHandPaint.setColor(Color.GRAY); //resources.getColor(R.color.analog_hands)
            mHandPaint.setStrokeWidth(3f); //resources.getDimension(R.dimen.analog_hand_stroke)
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);
        }







        private Runnable taskUnlockReadyToShow = new Runnable() {
            @Override
            public void run() {
                mReadyToShow.setValue(true);
                mWearPAN.wakeUpScreen(1000, AWearFaceService.this);
                //if (L) Log.i(TAG, "<<< mReadyToShow = " + mReadyToShow.getValue());

                ACRA.getErrorReporter().reportBuilder().forceSilent().message("Test from wear.").send();


            }
        };




//        private final class ServiceHandler extends Handler {
//            public ServiceHandler(Looper looper) {
//                super(looper);
//            }
//
//            @Override // Handler
//            public void handleMessage(Message msg) {
//                //super.handleMessage(msg);
//            }
//        }
//        private Looper mServiceLooper;
//        private ServiceHandler mServiceHandler;



        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            ((CommonApplication) getApplication()).setWearListenerMode(ACommon.WEAR_LISTENER_CLIENT);




            productId = getResources().getString(R.string.product_id);
            productIdServiceCap = productId + ACommon.SERVICE_LOCAL_CAP_SUFFIX;
            //productIdWatchFaceCap = productId + ACommon.WATCHFACE_LOCAL_CAP_SUFFIX;


//            ((WearApplication) getApplication()).setGoogleApiClient(mGoogleApiClient);
//            Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
//                @Override
//                public void onResult(NodeApi.GetLocalNodeResult result) {
//                    String peerId = result.getNode().getId();
//                    //Log.i(TAG, "Local peer id: " + peerId);
//                    WearApplication application = (WearApplication) getApplication();
//                    application.setLocalPeerId(peerId);
//                    mPeerId = peerId;
//                }
//            });
            mWearWorker = new WearDataForWatchFace(AWearFaceService.this, Engine.this);
            mWearPAN = new WearDataSender(AWearFaceService.this, Engine.this, /*productIdServiceCap*/null);
            //mWearPAN.addLocalCapability(productIdWatchFaceCap);


//            HandlerThread thread = new HandlerThread("LightClassicsWF", android.os.Process.THREAD_PRIORITY_BACKGROUND);
//            thread.start();
//            mServiceLooper = thread.getLooper();
//            mServiceHandler = new ServiceHandler(mServiceLooper);
//            mServiceHandler.postDelayed(taskUnlockReadyToShow, 7000);
            new Handler().postDelayed(taskUnlockReadyToShow, 10000);





            mWearDataFromListenerReceiver = new BroadcastFromWearListener(mWearWorker);
//            LocalBroadcastManager.getInstance(AWearFaceService.this).registerReceiver(mWearDataFromListenerReceiver,
//                    new IntentFilter(ACommon.BCAST_EVENT_ACTION));
            mWearDataFromListenerReceiver.register(AWearFaceService.this, new IntentFilter(ACommon.BCAST_EVENT_ACTION));


            createPaints();

            try {
                PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo("com.google.android.wearable.app", 0);
                if (packageInfo.versionCode > 720000000) {
                    // Supports taps - cache this result to avoid calling PackageManager again
                    mWatchProp.setTapEnabled(true);
                } else {
                    // Device does not support taps yet
                    mWatchProp.setTapEnabled(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //
            if (mWatchProp.isSetTappable() && mWatchProp.isTappable()) {
                setWatchFaceStyle(new WatchFaceStyle.Builder(AWearFaceService.this)
                        .setAcceptsTapEvents(true)
                        .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                        .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                        .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                        //.setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
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



            mAppPreferences = new AppPreferences();
            wTime = new WatchTime(TimeZone.getDefault(), mAppPreferences);
            mFirstDayOfWeek = wTime.getFirstDayOfWeek();
                        //mTime = new Time();

            ((CommonApplication) getApplication()).publishSharedValues();
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            //mWearPAN.removeLocalCapability(productIdWatchFaceCap);

            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            mWearDataFromListenerReceiver.unregister(AWearFaceService.this);
            mWearPAN.disconnectDelayed(20000);
//            mServiceLooper.quitSafely();
        }


        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    wTime.eventSwapTz();
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mWatchProp.setAmbientValues(
                    properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false),
                    properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false)
            );
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            if (!(!isInAmbientMode() && mVisible)) wTime.onTimeTick(TimeZone.getDefault());
            invalidate();
        }


        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
//                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
//                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
//                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                mHandPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mWatchProp.isSetAmbient() && mWatchProp.getLowBit()) { //mLowBit
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();

//            // variant from raf3078
//            mAmbient = inAmbientMode;
//            if (mWatchProp.isSetAmbient() && mWatchProp.getLowBit()) {
//                mHandPaint.setAntiAlias(!inAmbientMode);
//            }
//            invalidate();
//            // Whether the timer should be running depends on whether we're in ambient mode (as well
//            // as whether we're visible), so we may need to start or stop the timer.
//            updateTimer();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (L) Log.i(TAG, "onVisibilityChanged() <<<<< BEGIN >>>>>");

//            if (visible) {
//                registerTimeZoneReceiver();
//                // Update time zone in case it changed while we weren't visible.
//                mTime.clear(TimeZone.getDefault().getID());
//                mTime.setToNow();
//            } else {
//                unregisterTimeZoneReceiver();
//            }
//            // Whether the timer should be running depends on whether we're visible (as well as
//            // whether we're in ambient mode), so we may need to start or stop the timer.
//            updateTimer();

            // variant from raf3078
            mVisible = visible;
            if (visible) {

                //mGoogleApiClient.connect();
//                int cc = mWearPAN.requestConnect();
//                if (L) Log.i(TAG, "onVisibilityChanged(), visible=" + mVisible + ", count=" + cc + ", connected=" + mWearPAN.isConnected());
                if (L) Log.i(TAG, "onVisibilityChanged(), visible=" + mVisible + ", connected=" + mWearPAN.isConnected());

                registerReceivers(true);
                //wTime.setTimeZone(TimeZone.getDefault());
                            //mTime.clear(TimeZone.getDefault().getID());
                            //mTime.setToNow();
                wTime.checkHandHeldTimeZone(TimeZone.getDefault());
                wTime.set(System.currentTimeMillis());
                // Let send screenshots to handheld service
                appearanceScreenshotTimeMs = 0l;
                ambientAppearanceScreenshotTimeMs = 0L;
                if (mAppPreferences.getSweepSeconds()) invalidate(); // uncomment if sweep ???
            } else {
                registerReceivers(false);

                //if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                //    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                //    mGoogleApiClient.disconnect();
                //}
//                int cc = mWearPAN.requestDisconnect();
//                if (L) Log.i(TAG, "onVisibilityChanged(), visible=" + mVisible + ", count=" + cc + ", connected=" + mWearPAN.isConnected());
                if (L) Log.i(TAG, "onVisibilityChanged(), visible=" + mVisible + ", connected=" + mWearPAN.isConnected());

            }
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            if (!mAppPreferences.getSweepSeconds()) updateTimer(); // comment if sweep ???
            if (L) Log.i(TAG, "onVisibilityChanged() <<<<< END >>>>>");
        }


        public void simpleDrawHands(Canvas canvas) {
            //mTime.setToNow();
            wTime.set(System.currentTimeMillis());

            // Draw the background.
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

            //drawFitInIcon(canvas);

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = mWatchProp.getWidth() / 2f;
            float centerY = mWatchProp.getHeight() / 2f;

            float secRot = /*mTime.second*/ wTime.getSecond() / 30f * (float) Math.PI;
            int minutes = /*mTime.minute*/ wTime.getMinute();
            float minRot = minutes / 30f * (float) Math.PI;
            float hrRot = ((/*mTime.hour*/ wTime.getHour() + (minutes / 60f)) / 6f) * (float) Math.PI;

            float secLength = centerX - 20;
            float minLength = centerX - 40;
            float hrLength = centerX - 80;

            if (!mAmbient) {
                float secX = (float) Math.sin(secRot) * secLength;
                float secY = (float) -Math.cos(secRot) * secLength;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mHandPaint);
            }

            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mHandPaint);

            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHandPaint);
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            if (!mWatchProp.isSetDimensions()) {
                mWatchProp.setDimensionsValues(bounds.width(), bounds.height());
                dialElements.scaleRobotPath((mWatchProp.getWidth() * 0.55f), mWatchProp.getWidth());
            }

            if (readyToFit()) {
                simpleDrawHands(canvas);
                //drawFitInIcon(canvas);
            } else {
                drawFitInIcon(canvas);
            }
        }


        private boolean readyToFit() {
            //return mWatchProp.isDetermined();
            //if (L) Log.i(TAG, ">>> mReadyToShow = " + mReadyToShow.getValue());
            return mReadyToShow.getValue();
        }


        private void drawFitInIcon(Canvas canvas) {
            if (isInAmbientMode()) {
                mCommonPaint.setAntiAlias(false);
                mCommonPaint.setDither(false);
                //
                mCommonPaint.setStyle(Paint.Style.FILL);
                mCommonPaint.setColor(Color.BLACK);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_ERASER], mCommonPaint);
                //
//                mCommonPaint.setColor(0xffa4c639);
//                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_HEAD], mCommonPaint);
                mCommonPaint.setStrokeWidth(2f);
                mCommonPaint.setColor(Color.WHITE);
                mCommonPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_HEAD], mCommonPaint);
                //
                mCommonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_EYES], mCommonPaint);
            } else {
                mCommonPaint.setAntiAlias(true);
                mCommonPaint.setDither(true);
                //
                mCommonPaint.setStyle(Paint.Style.FILL);
                mCommonPaint.setColor(Color.BLACK);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_ERASER], mCommonPaint);
                //
                mCommonPaint.setColor(0xffa4c639);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_HEAD], mCommonPaint);
                mCommonPaint.setStrokeWidth(2f);
                mCommonPaint.setColor(Color.WHITE);
                mCommonPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_HEAD], mCommonPaint);
                //
                mCommonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(dialElements.mRobotPathScaled[ElementsSVG.ROBOT_EYES], mCommonPaint);
                //
                mCommonMatrix.reset();
                mCommonMatrix.postRotate(((System.currentTimeMillis() / 1000) % 60) * 6, mWatchProp.getCenterX(), mWatchProp.getCenterY());
                //mGearRotated.reset();
                dialElements.mRobotPathScaled[ElementsSVG.ROBOT_GEAR].transform(mCommonMatrix, mGearRotated);
                //
                mCommonPaint.setColor(Color.DKGRAY);
                mCommonPaint.setStrokeWidth(0.5f);
                mCommonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(mGearRotated, mCommonPaint); //dialElements.mRobotPathScaled[ElementsSVG.ROBOT_GEAR]
                mCommonPaint.setColor(Color.LTGRAY);
                mCommonPaint.setStrokeWidth(1.5f);
                mCommonPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(mGearRotated, mCommonPaint); //dialElements.mRobotPathScaled[ElementsSVG.ROBOT_GEAR]
            }

        }


        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
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
            return isVisible() && !isInAmbientMode();
        }


        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }


















        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                            //mTime.clear(intent.getStringExtra("time-zone"));
                            //mTime.setToNow();
                wTime.setHandHeldTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                wTime.set(System.currentTimeMillis());
                appearanceScreenshotTimeMs = 0L;
            }
        };


        final BroadcastReceiver mLocaleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wTime.setLocale(Locale.getDefault());
                mFirstDayOfWeek = wTime.getFirstDayOfWeek();
                //mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
                //Log.i(TAG, "((( LOCALE CHANGED, FDOW=" + mFirstDayOfWeek);
            }
        };


        final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                //mWatchesBattery = level / (float) scale;
                mWatchesBattery = (float) level;
            }
        };



        private void registerReceivers(boolean register) {
            registerTimeZoneReceiver(register);
            registerBatteryReceiver(register);
            registerLocaleReceiver(register);
//            if (true==denseAppearance.mShowRimAnimation) registerSensorsReceiver(register);
//            else setStaticProjection180dgr();
        }


        private void registerTimeZoneReceiver(boolean register) {
            if (register) {
                if (!mRegisteredTimeZoneReceiver) {
                    mRegisteredTimeZoneReceiver = true;
                    IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                    AWearFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
                }
            } else {
                if (mRegisteredTimeZoneReceiver) {
                    mRegisteredTimeZoneReceiver = false;
                    AWearFaceService.this.unregisterReceiver(mTimeZoneReceiver);
                }
            }
        }


        private void registerBatteryReceiver(boolean register) {
            if (register) {
                if (!mRegisteredBatteryReceiver) {
                    mRegisteredBatteryReceiver = true;
                    AWearFaceService.this.registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                }
            } else {
                if (mRegisteredBatteryReceiver) {
                    mRegisteredBatteryReceiver = false;
                    AWearFaceService.this.unregisterReceiver(mBatteryInfoReceiver);
                }
            }
        }


        private void registerLocaleReceiver(boolean register) {
            if (register) {
                if (!mRegisteredLocaleReceiver) {
                    mRegisteredLocaleReceiver = true;
                    AWearFaceService.this.registerReceiver(mLocaleReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
                }
            } else {
                if (mRegisteredLocaleReceiver) {
                    mRegisteredLocaleReceiver = false;
                    AWearFaceService.this.unregisterReceiver(mLocaleReceiver);
                }
            }
        }


//        private void registerSensorsReceiver(boolean register) {
//            if (register) {
//                if (mSensMan!=null && !mRegisteredOrientationSensorListener) {
//                    mRegisteredOrientationSensorListener = true;
//                    mSensMan.registerListener(mOrientationSensorListener,
//                            mSensMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
//                    mSensMan.registerListener(mOrientationSensorListener,
//                            mSensMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
//                    //
//                    //mSensMan.registerListener(mLightSensorListener, mSensMan.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
//                }
//            } else {
//                if (mSensMan!=null && mRegisteredOrientationSensorListener) {
//                    mRegisteredOrientationSensorListener = false;
//                    mSensMan.unregisterListener(mOrientationSensorListener);
//                    //
//                    //mSensMan.unregisterListener(mLightSensorListener);
//                }
//            }
//        }


        @Override //OnWearEvent
        public boolean isPeerIdKnown() {
            return false;
        }

        @Override //OnWearEvent
        public void rememberPeerId(String peerId) {

        }


        @Override
        public boolean isLocalPeerIdKnown() {
            return false;
        }


        @Override //OnWearEvent
        public void rememberLocalPeerId(String peerId) {
            if (L) Log.i(TAG, "rememberLocalPeerId() = " + peerId);
            ((CommonApplication) getApplication()).setLocalPeerId(peerId); //Context
            //((WearApplication) getApplication()).setLocalPeerId(peerId);
            //mPeerId = peerId;
        }


        //Runnable taskFindLocalCapService = ;


        @Override
        public void onWearConnResult(int i) {
            if (WearDataSender.WEAR_CONN_CONNECTED == i) {
                if (L) Log.i(TAG, "Sender of watch face connect SUCCESS.");

                //done: mToPeer не должен быть null, иначе listener вторых часов может удалить этот отчёт
                (new Thread(new WearCrashSend.WearSendAllCrashReports(AWearFaceService.this, null, null, Utils.getSerialSeq()))).start();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        CapabilityApi.GetCapabilityResult result =
//                                Wearable.CapabilityApi.getCapability(
//                                        mWearPAN.getGoogleApiClient(), productIdServiceCap, CapabilityApi.FILTER_REACHABLE).await();
//                        if (result.getStatus().isSuccess()) {
//                            CapabilityInfo capabilityInfo = result.getCapability();
//                            Set<Node> connectedNodes = capabilityInfo.getNodes();
//                            for (Node node : connectedNodes) {
//                                if (node.isNearby()) {
//                                    //return node.getId();
//                                    if (L) Log.i(TAG, "SERVICE nodeId = " + node.getId());
//                                }
//                                //bestNodeId = node.getId();
//                            }
//                        }
//                    }
//                }).start();
            } else {
                if (L) Log.i(TAG, "!!! Sender of watch face connect FILED.");

            }
        }


        public void sendWatchFaceHereMsg(String toNodeId, int handshakeEvent, int handshakeAction) {
            if (L) Log.i(TAG, "sendWatchFaceHereMsg(), toNode=" + toNodeId + ", event=" + handshakeEvent + ", action=" + handshakeAction);
//        Bundle bundle = new Bundle();
//        bundle.putInt(ACommon.KEY_EVENT, ACommon.EVT_SERVICE_HERE);
//        bundle.putInt(ACommon.KEY_HANDSHAKE_ACTION, handshakeAction);
//        if (L) Log.i(TAG, "sendServiceHereMsg(), toNode=" + toNodeId + ", bundle=" + bundle);
//        (new Thread(new WearDataSender.TaskWearMessageSend(mWearPAN.getGoogleApiClient(), bundle, toNodeId, ACommon.MESSAGE_PATH))).start();
            mWearPAN.requestConnect(0);
            Utils.sendHandshakeMsg(toNodeId, handshakeEvent, handshakeAction, mWearPAN.getGoogleApiClient());
        }

    }
}
