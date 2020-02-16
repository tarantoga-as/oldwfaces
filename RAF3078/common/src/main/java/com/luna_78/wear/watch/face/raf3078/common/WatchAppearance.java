package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

/**
 * Created by buba on 06/05/15.
 */
public class WatchAppearance {

    private static final String TAG = "WAP";
    private static final boolean L = false;


    public static final int NUM_HOUR_MARKS = 12;
    public static final int HM_DIGITS = 1;
    public static final int HM_RND_TRIANGLE = 2;
    public static final int HM_RND_DASH = 3;
    public static final int DEFAULT_HOUR_MARK = HM_DIGITS;
    public static final int HM_RELIEF_DEBOSS = 0;
    public static final int HM_RELIEF_EMBOSS = 1;
    public static final int HM_RELIEF_NONE = 2;
    public static final int DEFAULT_HOUR_MARK_RELIEF = HM_RELIEF_DEBOSS;
    public static final int DEFAULT_HOUR_MARK_RELIEF_STRENGTH = 170;
    public static final int DEFAULT_HOURMARK_OUTLINE_COLOR = 0x00ffffff;
    public static final float RECOMMENDED_BURNIN_MARGIN = 10f;
    public static final float DEFAULT_PLATE_TEXTURE_STRENGTH = 0.3f;
    public static final float DEFAULT_AUXDIAL_TEXTURE_STRENGTH = 0f;
    public static final int DEFAULT_PLATE_RELIEF_STRENGTH = 0;



    public boolean mDialPlateReady = false;

    public int watchMainHandsIndex = 0;
    public int watchAuxHandsIndex = 0;
    public int watchBackgroundIndex = 0;
    public int watchDomIndex = 0;
    public int watchLayoutIndex = 0;
    //
//    public boolean mShowHandheldBattery = false;
    public boolean mShowRimAnimation = false;

    private boolean mShowHrDigitsRelief = true;
    //
    public void setShowHrDigitsRelief(boolean mShowHrDigitsRelief) {
        mDialPlateReady = false;
        this.mShowHrDigitsRelief = mShowHrDigitsRelief;
    }
    public boolean isShowHrDigitsRelief() {
        return mShowHrDigitsRelief;
    }

    private boolean mShowInscriptionsRelief = true;
    //
    public void setShowInscriptionsRelief(boolean mShowInscriptionsRelief) {
        mDialPlateReady = false;
        this.mShowInscriptionsRelief = mShowInscriptionsRelief;
    }
    public boolean isShowInscriptionsRelief() {
        return mShowInscriptionsRelief;
    }

    //
    public boolean mShowDialGradient = true;
    public float dgFirstStop = 0.75f;
    public float dgHalfEdgeStop = 0.97f;
    public int dgEdgeAlpha = 100;
    public float dgFirstStop1 = 0.75f;
    public float dgHalfEdgeStop1 = 0.97f;
    public int dgEdgeAlpha1 = 100;
    public int dgInvert = ACommon.GD_INVERT_NONE;
    //
    public void setDialGradientPack(boolean showDialGradient, float firstStop, float halfEdgeStop, int edgeAlpha,
                                    float firstStop1, float halfEdgeStop1, int edgeAlpha1, int invert) {

        mDialPlateReady = false;
        mShowDialGradient = showDialGradient;
        dgFirstStop = firstStop;
        dgHalfEdgeStop = halfEdgeStop;
        dgEdgeAlpha = edgeAlpha;
        dgFirstStop1 = firstStop1;
        dgHalfEdgeStop1 = halfEdgeStop1;
        dgEdgeAlpha1 = edgeAlpha1;
        dgInvert = invert;
    }
    //
    public int mAuxBevelColor = ACommon.BEVEL_FROM_AUX;
    //
    public Inscription mPrint;// = new Inscription();
    //
    public int mMainDigitsColor = 0xffffffff;
    public int mMainHourHandColor = 0xffffffff;
    public int mMainDecorUpperColor = 0xffffffff;
    public int mMainMinuteHandColor = 0xffffffff;
    public int mMainBackgroundColor = 0xff252827;
    public int mMainMainHandsColor = 0xfff0f0d2;
    public int mMainSecondsHandColor = 0xffc6514b;
    public int mMainDomBackColor = 0xffe46f69;
    public int mMainDomFrontColor = 0xdcf0f0f0;
    public int mMainTickDigitColor = 0xc8ffffff;
    public int mMainTickColor = 0xffffffff;
    //
    public int mMainCalendarDialBackgroundColor = 0xff252827;
    public int mMainCalendarDialDigitsColor = 0xffffffff;
    public int mMainCalendarDialTicksColor = 0xffffffff;
    //
    public int mMainSmallAuxDialBackgroundColor = 0xff252827;
    public int mMainSmallAuxDialDigitsColor = 0xc8ffffff;
    public int mMainSmallAuxDialTick1Color = 0xc8ffffff;
    public int mMainSmallAuxDialTick2Color = 0xc8ff0000;
    //
    public int mMainAuxHandWeekdayColor = 0xffc16161;
    //
    public int mMainAuxHandWearBattColor = 0xffffffff;
    public int mMainAuxHandPhoneBattColor = 0xffc16161;
    public int mMainAuxHandMonthColor = 0xffc16161;
    //
    public int mAmbientDigitsColor = 0xFF9DC775; //getResources().getColor(R.color.Phosphor);
    public int mAmbientHourHandColor = 0xFF9DC775; //getResources().getColor(R.color.Phosphor);
    public int mAmbientMinuteHandColor = 0xFF9DC775; //getResources().getColor(R.color.Phosphor);
    public int mAmbientTicksColor = 0xFF787878; //getResources().getColor(R.color.PureGrey);
    public int mAmbientTickDigitColor = 0xC8787878; //getResources().getColor(R.color.PureGreyC8);
    public int mAmbientDecorUpperColor = 0xFF9DC775; //getResources().getColor(R.color.Phosphor);
    //
    //int mAmbientDomAndAuxHandsColor = 0xFF98A898; //getResources().getColor(R.color.BBGreen);
    public int mAmbientDomBackColor = 0xFF98A898; //getResources().getColor(R.color.BBGreen);
    public int mAmbientDomFrontColor = 0xff000000;
    public int mAmbientAuxHandsColor = 0xFF98A898; //getResources().getColor(R.color.BBGreen);

    public int mMainInscription_1_Color = 0xffffffff;
    public int mMainInscription_2_Color = 0xffffffff;
    public int mMainInscription_3_Color = 0xffffffff;
    public int mMainInscription_4_Color = 0xffffffff;
    public int mMainInscription_5_Color = 0xffffffff;
    public int mMainInscription_6_Color = 0xffffffff;
    public int mMainInscription_7_Color = 0xffffffff;


    public int mMainHourMarkOutlineColor = DEFAULT_HOURMARK_OUTLINE_COLOR;
    public long mHourMarksIndex[];
    public long mHourMarksReliefIndex[];
    public int mHourMarksReliefStrength = DEFAULT_HOUR_MARK_RELIEF_STRENGTH;

    public float mFxPlateTextureStrength = DEFAULT_PLATE_TEXTURE_STRENGTH;
    public float mFxAuxDialTextureStrength = DEFAULT_AUXDIAL_TEXTURE_STRENGTH;
    public int mFxPlateReliefStrength = DEFAULT_PLATE_RELIEF_STRENGTH;

    public int mMainDomFrameColor = mMainTickColor;
    public int mAmbientDomFrameColor = mAmbientTicksColor;

    public boolean mColorizeBurnInMargin = true;

    public int mInscriptionsReliefStrength = DEFAULT_HOUR_MARK_RELIEF_STRENGTH;

    public int mMainTzScripts = mMainTickColor;
    public int mMainTzCircles = mMainTickColor;
    public int mMainTzSign = mMainBackgroundColor;
    public int mMainTzPoint = mMainBackgroundColor;
    public int mAmbientTzScripts = mAmbientTicksColor;
    public int mAmbientTzCircles = mAmbientTicksColor;
    public int mAmbientTzSign = Color.BLACK;
    public int mAmbientTzPoint = Color.BLACK;



    public WatchAppearance(WatchAppearance org, Context context) {

        mPrint = new Inscription(context);

        mHourMarksIndex = new long[WatchAppearance.NUM_HOUR_MARKS];
        for (int i=0; i < WatchAppearance.NUM_HOUR_MARKS; i++) mHourMarksIndex[i] = DEFAULT_HOUR_MARK;

        mHourMarksReliefIndex = new long[WatchAppearance.NUM_HOUR_MARKS];
        for (int i=0; i < WatchAppearance.NUM_HOUR_MARKS; i++) mHourMarksReliefIndex[i] = DEFAULT_HOUR_MARK_RELIEF;

        if (null != org) {
            this.mHourMarksReliefStrength = org.mHourMarksReliefStrength;
            this.mInscriptionsReliefStrength = org.mInscriptionsReliefStrength;
            this.watchMainHandsIndex = org.watchMainHandsIndex;
            this.watchAuxHandsIndex = org.watchAuxHandsIndex;
            this.watchBackgroundIndex = org.watchBackgroundIndex;
            this.watchDomIndex = org.watchDomIndex;
            this.watchLayoutIndex = org.watchLayoutIndex;
            //
            this.mShowRimAnimation = false;
//            this.mShowHandheldBattery = false;
            this.mShowHrDigitsRelief = org.mShowHrDigitsRelief;
            this.mShowInscriptionsRelief = org.mShowInscriptionsRelief;
            //
            this.mShowDialGradient = org.mShowDialGradient;
            this.dgFirstStop = org.dgFirstStop;
            this.dgHalfEdgeStop = org.dgHalfEdgeStop;
            this.dgEdgeAlpha = org.dgEdgeAlpha;
            this.dgFirstStop1 = org.dgFirstStop1;
            this.dgHalfEdgeStop1 = org.dgHalfEdgeStop1;
            this.dgEdgeAlpha1 = org.dgEdgeAlpha1;
            this.dgInvert = org.dgInvert;
            //
            this.mAuxBevelColor = org.mAuxBevelColor;
            //
            this.mMainDigitsColor = org.mAmbientDigitsColor; // getResources().getColor(R.color.Phosphor);
            this.mMainHourHandColor = org.mAmbientHourHandColor;
            this.mMainMinuteHandColor = org.mAmbientMinuteHandColor;
            //
            this.mMainBackgroundColor = Color.BLACK; //getResources().getColor(R.color.PureBlack);
            this.mMainMainHandsColor = Color.BLACK; //getResources().getColor(R.color.PureBlack); //0xff787878;
            //this.mMainSecondsHandColor = 0xffc6514b;
            //this.mMainDomBackColor = org.mAmbientDomAndAuxHandsColor;
            this.mMainDomFrontColor = Color.BLACK; //getResources().getColor(R.color.PureBlack);
            this.mMainTickDigitColor = org.mAmbientTickDigitColor;
            this.mMainTickColor = org.mAmbientTicksColor;
            //
            this.mMainCalendarDialBackgroundColor = Color.BLACK; //getResources().getColor(R.color.PureBlack);
            this.mMainCalendarDialDigitsColor = org.mAmbientTicksColor;
            this.mMainCalendarDialTicksColor = org.mAmbientTicksColor;
            //
            this.mMainSmallAuxDialBackgroundColor = Color.BLACK; //getResources().getColor(R.color.PureBlack);
            this.mMainSmallAuxDialDigitsColor = org.mAmbientTicksColor;
            this.mMainSmallAuxDialTick1Color = org.mAmbientTicksColor;
            this.mMainSmallAuxDialTick2Color = Color.RED; //getResources().getColor(R.color.PureRed);
            //
            //this.mMainAuxHandWeekdayColor = org.mAmbientDomAndAuxHandsColor;
            //
            //this.mMainAuxHandWearBattColor = org.mAmbientDomAndAuxHandsColor;
            //this.mMainAuxHandPhoneBattColor = 0xffc16161;
            //this.mMainAuxHandMonthColor = org.mAmbientDomAndAuxHandsColor; //R.color.BBGreen;
            this.mMainHourMarkOutlineColor = DEFAULT_HOURMARK_OUTLINE_COLOR;

            this.mFxPlateTextureStrength = org.mFxPlateTextureStrength;
            this.mFxAuxDialTextureStrength = org.mFxAuxDialTextureStrength;
            this.mFxPlateReliefStrength = org.mFxPlateReliefStrength;

            this.mMainDomFrameColor = org.mMainTickColor;
            this.mAmbientDomFrameColor = org.mAmbientTicksColor;

            this.mColorizeBurnInMargin = org.mColorizeBurnInMargin;

            this.mMainTzScripts = org.mMainTzScripts;
            this.mMainTzCircles = org.mMainTzCircles;
            this.mMainTzSign = org.mMainTzSign;
            this.mMainTzPoint = org.mMainTzPoint;
            this.mAmbientTzScripts = org.mAmbientTzScripts;
            this.mAmbientTzCircles = org.mAmbientTzCircles;
            this.mAmbientTzSign = org.mAmbientTzSign;
            this.mAmbientTzPoint = org.mAmbientTzPoint;
        }
    } // WatchAppearance()


    public Bundle bundleConfig(long appearanceModificationTimeMs) { //WatchAppearance appearance, 
        Bundle bundle = new Bundle();

        bundle.putLong(ACommon.CFG_TIME, appearanceModificationTimeMs);
        bundle.putInt(ACommon.CFG_LAYOUT_INDEX, this.watchLayoutIndex);                     //
        bundle.putInt(ACommon.CFG_MAIN_HANDS_INDEX, this.watchMainHandsIndex);              //
        bundle.putInt(ACommon.CFG_AUX_HANDS_INDEX, this.watchAuxHandsIndex);                //
        bundle.putInt(ACommon.CFG_DOM_INDEX, this.watchDomIndex);                           //
        bundle.putInt(ACommon.CFG_BACKGROUND_INDEX, this.watchBackgroundIndex);             //
        //
//        bundle.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, this.mShowHandheldBattery);    //
        bundle.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, this.mShowRimAnimation);          //
        bundle.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, this.isShowHrDigitsRelief());      //
        bundle.putBoolean(ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF, this.isShowInscriptionsRelief());      //
        //
        bundle.putBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, this.mShowDialGradient);
        bundle.putInt(ACommon.CFG_INVERT_GRADIENT, this.dgInvert);
        bundle.putFloat(ACommon.CFG_DG_FIRST_STOP, this.dgFirstStop);
        bundle.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP, this.dgHalfEdgeStop);
        bundle.putInt(ACommon.CFG_DG_EDGE_ALPHA, this.dgEdgeAlpha);
        bundle.putFloat(ACommon.CFG_DG_FIRST_STOP_1, this.dgFirstStop1);
        bundle.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, this.dgHalfEdgeStop1);
        bundle.putInt(ACommon.CFG_DG_EDGE_ALPHA_1, this.dgEdgeAlpha1);
        //
        bundle.putInt(ACommon.CFG_AUX_BEVEL_COLOR, this.mAuxBevelColor);
        //


        // bundle inscriptions
        Inscription.bundleInscription(bundle, this.mPrint);
        bundle.putInt(ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH, this.mInscriptionsReliefStrength);


        bundle.putLongArray(ACommon.CFG_HOUR_MARKS, this.mHourMarksIndex);
        bundle.putLongArray(ACommon.CFG_HOUR_MARKS_RELIEF, this.mHourMarksReliefIndex);
        bundle.putInt(ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH, this.mHourMarksReliefStrength);


        bundle.putInt(ACommon.CFG_COLOR_MAIN_DIGITS, this.mMainDigitsColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_HOURHAND, this.mMainHourHandColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, this.mMainMinuteHandColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, this.mMainBackgroundColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, this.mMainMainHandsColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, this.mMainSecondsHandColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_DOMBACK, this.mMainDomBackColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, this.mMainDomFrontColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_TICK, this.mMainTickColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, this.mMainTickDigitColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, this.mMainCalendarDialBackgroundColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, this.mMainCalendarDialDigitsColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, this.mMainCalendarDialTicksColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, this.mMainSmallAuxDialBackgroundColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, this.mMainSmallAuxDialDigitsColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, this.mMainSmallAuxDialTick1Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, this.mMainSmallAuxDialTick2Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, this.mMainAuxHandWeekdayColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, this.mMainAuxHandWearBattColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, this.mMainAuxHandPhoneBattColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, this.mMainAuxHandMonthColor);
        //
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, this.mAmbientDigitsColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, this.mAmbientHourHandColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, this.mAmbientMinuteHandColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TICK, this.mAmbientTicksColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, this.mAmbientTickDigitColor);

        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DECORUPPER, this.mAmbientDecorUpperColor);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_DECORUPPER, this.mMainDecorUpperColor);

        //bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, this.mAmbientDomAndAuxHandsColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DOMBACK, this.mAmbientDomBackColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DOMFRONT, this.mAmbientDomFrontColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_AUXHANDS, this.mAmbientAuxHandsColor);

        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_1 = String.valueOf(CFG_MAIN_INSCRIPTION_1_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_2 = String.valueOf(CFG_MAIN_INSCRIPTION_2_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_3 = String.valueOf(CFG_MAIN_INSCRIPTION_3_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_4 = String.valueOf(CFG_MAIN_INSCRIPTION_4_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_5 = String.valueOf(CFG_MAIN_INSCRIPTION_5_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_6 = String.valueOf(CFG_MAIN_INSCRIPTION_6_COLOR);
        //    public static final String CFG_COLOR_MAIN_INSCRIPTION_7 = String.valueOf(CFG_MAIN_INSCRIPTION_7_COLOR);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_1, this.mMainInscription_1_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_2, this.mMainInscription_2_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_3, this.mMainInscription_3_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_4, this.mMainInscription_4_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_5, this.mMainInscription_5_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_6, this.mMainInscription_6_Color);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_7, this.mMainInscription_7_Color);

        bundle.putInt(ACommon.CFG_COLOR_MAIN_HOURMARK_OUTLINE, this.mMainHourMarkOutlineColor);

        bundle.putFloat(ACommon.CFG_PLATE_TEXTURE_STRENGTH, this.mFxPlateTextureStrength);
        bundle.putFloat(ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH, this.mFxAuxDialTextureStrength);
        bundle.putInt(ACommon.CFG_PLATE_RELIEF_STRENGTH, this.mFxPlateReliefStrength);

        bundle.putInt(ACommon.CFG_COLOR_MAIN_DOM_FRAME, this.mMainDomFrameColor);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_DOM_FRAME, this.mAmbientDomFrameColor);

        bundle.putBoolean(ACommon.CFG_COLORIZE_BURNIN_MARGIN, this.mColorizeBurnInMargin);

        bundle.putInt(ACommon.CFG_COLOR_MAIN_TZ_SCRIPTS, this.mMainTzScripts);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_TZ_CIRCLES, this.mMainTzCircles);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_TZ_SIGN, this.mMainTzSign);
        bundle.putInt(ACommon.CFG_COLOR_MAIN_TZ_POINT, this.mMainTzPoint);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TZ_SCRIPTS, this.mAmbientTzScripts);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TZ_CIRCLES, this.mAmbientTzCircles);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TZ_SIGN, mAmbientTzSign);
        bundle.putInt(ACommon.CFG_COLOR_AMBIENT_TZ_POINT, mAmbientTzPoint);


//        if (L) {
//            Writer writer = new StringWriter(); (new Exception()).printStackTrace(new PrintWriter(writer)); String sTrace = writer.toString();
//            //Log.i(TAG, "((( bundleConfig, mColorizeBurnInMargin=" + this.mColorizeBurnInMargin);
//            String colorV = String.format("%X", this.mMainDecorUpperColor);
//            Log.i(TAG, "((( bundleConfig, mMainDecorUpperColor=" + colorV);
//            Log.i(TAG, "((( bundleConfig, calling stack trace = " + sTrace);
//        }

        return bundle;
    } // bundleConfig


    public boolean unBundleConfig(Bundle bundle) { //, boolean initial WatchAppearance appearance

        //if (L) Log.i(TAG, "((( unBundleConfig");

        if (null == bundle) return false;

        //long cfgTime;
        int i;
        float f;
        boolean retVal = true, boolv;
        long longArr[];
        //ArrayList<Integer> colors;

//        cfgTime = bundle.getLong(ACommon.CFG_TIME);
//        if (cfgTime == 0L) retVal = false;
//        else appearanceModificationTimeMs = cfgTime;

        i = bundle.getInt(ACommon.CFG_LAYOUT_INDEX, -1);
        if (-1 == i) retVal = false;
        else this.watchLayoutIndex = i;
        i = bundle.getInt(ACommon.CFG_MAIN_HANDS_INDEX, -1);
        if (-1 == i) retVal = false;
        else this.watchMainHandsIndex = i;
        i = bundle.getInt(ACommon.CFG_AUX_HANDS_INDEX, -1);
        if (-1 == i) retVal = false;
        else this.watchAuxHandsIndex = i;
        i = bundle.getInt(ACommon.CFG_DOM_INDEX, -1);
        if (-1 == i) retVal = false;
        else this.watchDomIndex = i;
        i = bundle.getInt(ACommon.CFG_BACKGROUND_INDEX, -1);
        if (-1 == i) retVal = false;
        else this.watchBackgroundIndex = i;

//        boolv = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
//        this.mShowHandheldBattery = boolv;
        //
        boolv = bundle.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
        this.mShowRimAnimation = boolv;
        // mShowHrDigitsRelief CFG_SHOW_HRDIGITS_RELIEF
        boolv = bundle.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
        this.setShowHrDigitsRelief(boolv);

        boolv = bundle.getBoolean(ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF, true);
        this.setShowInscriptionsRelief(boolv);

        boolv = bundle.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
        this.mShowDialGradient = boolv;
        i = bundle.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE);
        this.dgInvert = i;
        //
        f = bundle.getFloat(ACommon.CFG_DG_FIRST_STOP, -1f);
        if (-1f == f) retVal = false;
        else this.dgFirstStop = f;
        f = bundle.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP, -1f);
        if (-1f == f) retVal = false;
        else this.dgHalfEdgeStop = f;
        i = bundle.getInt(ACommon.CFG_DG_EDGE_ALPHA, -1);
        if (-1 == i) retVal = false;
        else this.dgEdgeAlpha = i;
        //
        f = bundle.getFloat(ACommon.CFG_DG_FIRST_STOP_1, -1f);
        if (-1f == f) retVal = false;
        else this.dgFirstStop1 = f;
        f = bundle.getFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, -1f);
        if (-1f == f) retVal = false;
        else this.dgHalfEdgeStop1 = f;
        i = bundle.getInt(ACommon.CFG_DG_EDGE_ALPHA_1, -1);
        if (-1 == i) retVal = false;
        else this.dgEdgeAlpha1 = i;
        //
        i = bundle.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
        this.mAuxBevelColor = i;


        Inscription.unBundleInscription(bundle, this.mPrint);
        i = bundle.getInt(ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH, -1);
        if (-1 == i) this.mInscriptionsReliefStrength = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH;
        else this.mInscriptionsReliefStrength = i;


        longArr = bundle.getLongArray(ACommon.CFG_HOUR_MARKS);
        if (null == longArr) for(i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) mHourMarksIndex[i] = DEFAULT_HOUR_MARK;
        else mHourMarksIndex = longArr;
        //
        longArr = bundle.getLongArray(ACommon.CFG_HOUR_MARKS_RELIEF);
        if (null == longArr) for(i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) mHourMarksReliefIndex[i] = DEFAULT_HOUR_MARK_RELIEF;
        else mHourMarksReliefIndex = longArr;
        //
        i = bundle.getInt(ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH, -1);
        if (-1 == i) this.mHourMarksReliefStrength = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH;
        else this.mHourMarksReliefStrength = i;


        this.mMainDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DIGITS, 0xffffffff);
        this.mMainHourHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_HOURHAND, 0xffffffff);
        this.mMainMinuteHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_MINUTEHAND, 0xffffffff);
        this.mMainBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND, 0xff252827);
        this.mMainMainHandsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_MAINHANDS, 0xfff0f0d2);
        this.mMainSecondsHandColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SECONDSHAND, 0xffc6514b);
        this.mMainDomBackColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DOMBACK, 0xffe46f69);
        this.mMainDomFrontColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DOMFRONT, 0xdcf0f0f0);
        this.mMainTickColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_TICK, 0xffffffff);
        this.mMainTickDigitColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_TICKDIGIT, 0xc8ffffff);
        this.mMainCalendarDialBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND, 0xff252827);
        this.mMainCalendarDialDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_DIGITS, 0xffffffff);
        this.mMainCalendarDialTicksColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_TICKS, 0xffffffff);
        this.mMainSmallAuxDialBackgroundColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND, 0xff252827);
        this.mMainSmallAuxDialDigitsColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_DIGITS, 0xc8ffffff);
        this.mMainSmallAuxDialTick1Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS1, 0xc8ffffff);
        this.mMainSmallAuxDialTick2Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_TICKS2, 0xc8ff0000);
        //
        this.mMainAuxHandWeekdayColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEEKDAY, 0xffc16161);
        //
        this.mMainAuxHandWearBattColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_WEARBATT, 0xffffffff);
        this.mMainAuxHandPhoneBattColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_PHONEBATT, 0xffc16161);
        this.mMainAuxHandMonthColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_AUXHANDS_MONTH, 0xffc16161);
        //
        this.mAmbientDigitsColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DIGITS, 0xFF9DC775);
        this.mAmbientHourHandColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_HOURHAND, 0xFF9DC775);
        this.mAmbientMinuteHandColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_MINUTEHAND, 0xFF9DC775);
        this.mAmbientTicksColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TICK, 0xFF787878);
        this.mAmbientTickDigitColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TICKDIGIT, 0xC8787878);
        //
        this.mAmbientDecorUpperColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DECORUPPER, this.mAmbientHourHandColor);
        this.mMainDecorUpperColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DECORUPPER, this.mMainHourHandColor);
        //
        //this.mAmbientDomAndAuxHandsColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898);
        this.mAmbientDomBackColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOMBACK,
                bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898));
        this.mAmbientDomFrontColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOMFRONT, 0xff000000);
        this.mAmbientAuxHandsColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_AUXHANDS,
                bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_AUXHANDS, 0xFF98A898));

        this.mMainInscription_1_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_1, 0xffffffff);
        this.mMainInscription_2_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_2, 0xffffffff);
        this.mMainInscription_3_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_3, 0xffffffff);
        this.mMainInscription_4_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_4, 0xffffffff);
        this.mMainInscription_5_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_5, 0xffffffff);
        this.mMainInscription_6_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_6, 0xffffffff);
        this.mMainInscription_7_Color = bundle.getInt(ACommon.CFG_COLOR_MAIN_INSCRIPTION_7, 0xffffffff);

        this.mMainHourMarkOutlineColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_HOURMARK_OUTLINE, DEFAULT_HOURMARK_OUTLINE_COLOR);

        this.mFxPlateTextureStrength = bundle.getFloat(ACommon.CFG_PLATE_TEXTURE_STRENGTH, DEFAULT_PLATE_TEXTURE_STRENGTH);
        this.mFxAuxDialTextureStrength = bundle.getFloat(ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH, DEFAULT_AUXDIAL_TEXTURE_STRENGTH);
        this.mFxPlateReliefStrength = bundle.getInt(ACommon.CFG_PLATE_RELIEF_STRENGTH, DEFAULT_PLATE_RELIEF_STRENGTH);


        this.mMainDomFrameColor = bundle.getInt(ACommon.CFG_COLOR_MAIN_DOM_FRAME, this.mMainTickColor);
        this.mAmbientDomFrameColor = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_DOM_FRAME, this.mAmbientTicksColor);

        this.mColorizeBurnInMargin = bundle.getBoolean(ACommon.CFG_COLORIZE_BURNIN_MARGIN, true);

        this.mMainTzScripts = bundle.getInt(ACommon.CFG_COLOR_MAIN_TZ_SCRIPTS, this.mMainTickColor);
        this.mMainTzCircles = bundle.getInt(ACommon.CFG_COLOR_MAIN_TZ_CIRCLES, this.mMainTickColor);
        this.mMainTzSign = bundle.getInt(ACommon.CFG_COLOR_MAIN_TZ_SIGN, this.mMainBackgroundColor);
        this.mMainTzPoint = bundle.getInt(ACommon.CFG_COLOR_MAIN_TZ_POINT, this.mMainBackgroundColor);
        this.mAmbientTzScripts = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_SCRIPTS, this.mAmbientTicksColor);
        this.mAmbientTzCircles = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_CIRCLES, this.mAmbientTicksColor);
        this.mAmbientTzSign = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_SIGN, Color.BLACK);
        this.mAmbientTzPoint = bundle.getInt(ACommon.CFG_COLOR_AMBIENT_TZ_POINT, Color.BLACK);

//        if (L) {
//            Writer writer = new StringWriter(); (new Exception()).printStackTrace(new PrintWriter(writer)); String sTrace = writer.toString();
//            //Log.i(TAG, "((( unBundleConfig, mColorizeBurnInMargin=" + this.mColorizeBurnInMargin + ", retVal=" + retVal);
//            String colorV = String.format("%X", this.mMainDecorUpperColor);
//            Log.i(TAG, "((( unBundleConfig, mMainDecorUpperColor=" + colorV);
//            Log.i(TAG, "((( unBundleConfig, calling stack trace = " + sTrace);
//        }

        return retVal;
    } // unBundleConfig


    public static class SaveAppearance extends Thread {

        Object syncObject;
        Context context;
        String fileName;
        WatchAppearance appearance;

        public SaveAppearance(Object syncObject, Context context, String fileName, WatchAppearance appearance) {
            this.syncObject = syncObject;
            this.context = context;
            this.fileName = fileName;
            this.appearance = appearance;
        }

        public void run() {
            synchronized (syncObject) {
                Bundle config = appearance.bundleConfig(System.currentTimeMillis());
                ACommon.savePersistentDataToXmlFile(context, fileName, config);
            }
        }
    }

} // class WatchAppearance
