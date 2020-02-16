package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import com.luna_78.wear.watch.face.raf3078.common.R;
import java.util.Locale;

/**
 * Created by buba on 15/05/15.
 */
public class Inscription {

    private static final String TAG = "INSCR";

    public static final int NUM_INSCRIPTIONS = 7;

    public static final String DEFAULT_TEXT = "EMPTY";
    public static final int DEFAULT_COLOR = Color.GRAY;
    public static final String DEFAULT_FAMILY = "sans-serif";
    public static final String DEFAULT_STYLE = "NORMAL"; //Typeface.NORMAL;
    public static final float DEFAULT_ZERO = 0f;
    public static final float DEFAULT_ONE = 1.0f;
    //public static final boolean DEFAULT_FALSE = false;
    public static final long DEFAULT_NO = 0l;
    public static final long DEFAULT_YES = 1l;
    public static final int DEFAULT_LAYOUTINDEX_UNDEFINED = -1;

    //public boolean     enabled[] = new boolean[NUM_INSCRIPTIONS];
    public long        enabled[] = new long[NUM_INSCRIPTIONS];
    public String      text[] = new String[NUM_INSCRIPTIONS];
    public float       textSize[] = new float[NUM_INSCRIPTIONS];
    public float       textScaleX[] = new float[NUM_INSCRIPTIONS];
    public long        textColor[] = new long[NUM_INSCRIPTIONS];
    public float       radius[] = new float[NUM_INSCRIPTIONS]; // процент удаления точки начала надписи от центра к краю (циферблата) по радиусу
    public float       angle[] = new float[NUM_INSCRIPTIONS]; // градус угола поворота радиуса; 0 = 12 часов
    public float       incline[] = new float[NUM_INSCRIPTIONS]; // наклон базовой линии; 0 = 3 часа

    // в каком режиме создан - burnIn или полный циферблат
    public long        isBurnIn[] = new long[NUM_INSCRIPTIONS];

    // индекс вида циферблата, для которого (вида) определен набор надписей
    public int         watchLayoutIndex = DEFAULT_LAYOUTINDEX_UNDEFINED;

    // sans-serif, sans-serif-light, sans-serif-condensed, sans-serif-thin, sans-serif-medium, monospace
    public String      fontFamily[] = new String[NUM_INSCRIPTIONS];
    //
    public static String[] fontFamilyEnum;
    public static int getFamilyIndex(String family) {
        for (int i=0; i< fontFamilyEnum.length; i++) {
            //Log.i(TAG, "((( getFamilyIndex, family=" + family + ", fontFamilyEnum[" + i + "]=" + fontFamilyEnum[i]);
            if (family.equalsIgnoreCase(fontFamilyEnum[i])) return i;
        }
        return 0;
    } // getFamilyIndex

    // Typeface.create(String familyName, int style)
    // 0:NORMAL, 1:BOLD, 2:ITALIC, 3:BOLD_ITALIC
    public String      fontStyle[] = new String[NUM_INSCRIPTIONS];
    //
    public static String[] fontStyleEnum;
    public static int[] fontStyleInt = new int[] {Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC};
    public static int getStyleIndex(String style) {
        int retv = 0;
        //Log.i(TAG, "((((( getStyleIndex, fontStyleInt=" + fontStyleInt);
        for (int i=0; i< fontStyleEnum.length; i++) {
            //Log.i(TAG, "((((( getStyleIndex, style=" + style + ", fontStyleInt[" + i + "]=" + fontStyleEnum[i]);
            if (style.equals(fontStyleEnum[i])) retv = i;
        }
        return retv;
    } // getStyleIndex
    public static int getStyleValue(String style) {
        return fontStyleInt[getStyleIndex(style)];
    } // getStyleValue

    // 0:STRAIGHT, 1:ROUND_MC, 2:ROUND_AC, 3:ROUND_BC, 4:ROUND_CC
    public long         bend[] = new long[NUM_INSCRIPTIONS];
    //
    public static String[] bendEnum;
    public static int getBendIndex(String bend) {
        for (int i=0; i< bendEnum.length; i++) {
            if (bend.equals(bendEnum[i])) return i;
        }
        return 0;
    } // getBendIndex
    public static final int BEND_STRAIGHT = 0;
    public static final int BEND_ROUND_MC = 1;
    public static final int BEND_ROUND_AC = 2;
    public static final int BEND_ROUND_BC = 3;
    public static final int BEND_ROUND_CC = 4;
    public static final long DEFAULT_BEND = BEND_STRAIGHT;

    // 0:CW, 1:CCW
    public long         direction[] = new long[NUM_INSCRIPTIONS];
    //
    public static String[] directionEnum;
    public static final long DIRECTION_CW = 0l;
    public static final long DIRECTION_CCW = 1l;
    public static final long DEFAULT_DIRECTION = DIRECTION_CW;


    // 0:NONE 1:DARK_SHADOW 2:LIGHT_SHADOW 3:EMBOSS 4:DEBOSS
    public long             fx[] = new long[NUM_INSCRIPTIONS];
    //
    public static String[]  fxEnum;
    //
    public static final int FX_NONE         = 0;
    public static final int FX_DARK_SHADOW  = 1;
    public static final int FX_LIGHT_SHADOW = 2;
    public static final int FX_EMBOSS       = 3;
    public static final int FX_DEBOSS       = 4;
    public static final long DEFAULT_FX = FX_DARK_SHADOW;





    private void initDefaults() {
        for (int i=0; i<NUM_INSCRIPTIONS; i++) {
            enabled[i] = DEFAULT_NO;
            text[i] = DEFAULT_TEXT;
            textSize[i] = DEFAULT_ZERO;
            textScaleX[i] = DEFAULT_ONE;
            textColor[i] = DEFAULT_COLOR;
            radius[i] = DEFAULT_ZERO;
            angle[i] = DEFAULT_ZERO;
            incline[i] = DEFAULT_ZERO;
            isBurnIn[i] = DEFAULT_NO;
            fontFamily[i] = DEFAULT_FAMILY;
            fontStyle[i] = DEFAULT_STYLE;
            bend[i] = DEFAULT_BEND;
            direction[i] = DEFAULT_DIRECTION;
            fx[i] = DEFAULT_FX;
        }
        watchLayoutIndex = DEFAULT_LAYOUTINDEX_UNDEFINED;
    }


//    public Inscription() {
//        initDefaults();
//    }

    public Inscription(Context context) {
        fontFamilyEnum = context.getResources().getStringArray(R.array.font_family);
        fontStyleEnum = context.getResources().getStringArray(R.array.font_style);
        bendEnum = context.getResources().getStringArray(R.array.bend_type);
        directionEnum = context.getResources().getStringArray(R.array.direction_type);
        fxEnum = context.getResources().getStringArray(R.array.inscription_fx);
        //
        initDefaults();
    }


    public static final String CFG_INSCR_FULLBUNDLE = "iscr_fullbndl";
    public static final String CFG_INSCR_LAYOUTINDEX = "iscr_li";
    //
    public static final String CFG_INSCR_ENABLED = "iscr_en";
    public static final String CFG_INSCR_TEXT = "iscr_txt";
    public static final String CFG_INSCR_TEXTSIZE = "iscr_txtsz";
    public static final String CFG_INSCR_TEXTSCALEX = "iscr_txtx";
    public static final String CFG_INSCR_TEXTCOLOR = "iscr_clr";
    public static final String CFG_INSCR_RADIUS = "iscr_r";
    public static final String CFG_INSCR_ANGLE = "iscr_a";
    public static final String CFG_INSCR_INCLINE = "iscr_i";
    public static final String CFG_INSCR_BURNIN = "iscr_b";
    public static final String CFG_INSCR_FONTFAMILY = "iscr_ff";
    public static final String CFG_INSCR_FONTSTYLE = "iscr_fs";
    public static final String CFG_INSCR_BEND = "iscr_bend";
    public static final String CFG_INSCR_DIRECTION = "iscr_dir";
    public static final String CFG_INSCR_FX = "iscr_fx";


    public static void unBundleInscription(Bundle pack, Inscription inscription) {
        int i;
        //boolean boolArr[];
        String strArr[];
        float fltArr[];
        long longArr[];

        Bundle bundle = pack.getBundle(Inscription.CFG_INSCR_FULLBUNDLE);
        //Log.i(TAG, "((( unBundleInscription, 1 bundle=" + bundle);
        if (null == bundle) bundle = pack;
        //Log.i(TAG, "((( unBundleInscription, 2 bundle=" + bundle);
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((( unBundleInscription, [" + i + "]: " );
//        }


        i = bundle.getInt(Inscription.CFG_INSCR_LAYOUTINDEX, DEFAULT_LAYOUTINDEX_UNDEFINED);
        inscription.watchLayoutIndex = i;

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_ENABLED);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.enabled[i] = Inscription.DEFAULT_NO;
        else inscription.enabled = longArr;

        strArr = bundle.getStringArray(Inscription.CFG_INSCR_TEXT);
        if (null == strArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.text[i] = Inscription.DEFAULT_TEXT;
        else inscription.text = strArr;

        fltArr = bundle.getFloatArray(Inscription.CFG_INSCR_TEXTSIZE);
        if (null == fltArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.textSize[i] = Inscription.DEFAULT_ZERO;
        else inscription.textSize = fltArr;

        fltArr = bundle.getFloatArray(Inscription.CFG_INSCR_TEXTSCALEX);
        if (null == fltArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.textScaleX[i] = Inscription.DEFAULT_ONE;
        else inscription.textScaleX = fltArr;

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_TEXTCOLOR);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.textColor[i] = Inscription.DEFAULT_COLOR;
        else inscription.textColor = longArr;

        fltArr = bundle.getFloatArray(Inscription.CFG_INSCR_RADIUS);
        if (null == fltArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.radius[i] = Inscription.DEFAULT_ZERO;
        else inscription.radius = fltArr;

        fltArr = bundle.getFloatArray(Inscription.CFG_INSCR_ANGLE);
        if (null == fltArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.angle[i] = Inscription.DEFAULT_ZERO;
        else inscription.angle = fltArr;

        fltArr = bundle.getFloatArray(Inscription.CFG_INSCR_INCLINE);
        if (null == fltArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.incline[i] = Inscription.DEFAULT_ZERO;
        else inscription.incline = fltArr;

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_BURNIN);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.isBurnIn[i] = Inscription.DEFAULT_NO;
        else inscription.isBurnIn = longArr;

        strArr = bundle.getStringArray(Inscription.CFG_INSCR_FONTFAMILY);
        if (null == strArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.fontFamily[i] = Inscription.DEFAULT_FAMILY;
        else inscription.fontFamily = strArr;
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((( unBundleInscription, fontFamily[" + i + "] = " + inscription.fontFamily[i]);
//        }

        strArr = bundle.getStringArray(Inscription.CFG_INSCR_FONTSTYLE);
        if (null == strArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.fontStyle[i] = Inscription.DEFAULT_STYLE;
        else inscription.fontStyle = strArr;
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((( unBundleInscription, fontStyle[" + i + "] = " + inscription.fontStyle[i]);
//        }

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_BEND);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.bend[i] = Inscription.DEFAULT_BEND;
        else inscription.bend = longArr;
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((( unBundleInscription, bend[" + i + "] = " + inscription.bend[i]);
//        }

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_DIRECTION);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.direction[i] = Inscription.DEFAULT_DIRECTION;
        else inscription.direction = longArr;
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((( unBundleInscription, direction[" + i + "] = " + inscription.direction[i]);
//        }

//        Log.i(TAG, "(((( unBundle, bundle=" + bundle);
//        for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((((( unBundle, fontStyle[" + i + "]=" + inscription.fontStyle[i]);
//        }

        longArr = bundle.getLongArray(Inscription.CFG_INSCR_FX);
        if (null == longArr) for(i=0; i<Inscription.NUM_INSCRIPTIONS; i++) inscription.fx[i] = Inscription.DEFAULT_FX;
        else inscription.fx = longArr;

    } // unBundleInscription


    public static void bundleInscription(Bundle pack, Inscription inscription) {

        Bundle bundle = new Bundle(); //pack.getBundle(Inscription.CFG_INSCR_FULLBUNDLE);
        //
        bundle.putInt(Inscription.CFG_INSCR_LAYOUTINDEX, inscription.watchLayoutIndex);
        bundle.putLongArray(Inscription.CFG_INSCR_ENABLED, inscription.enabled);
        bundle.putStringArray(Inscription.CFG_INSCR_TEXT, inscription.text);
        bundle.putFloatArray(Inscription.CFG_INSCR_TEXTSIZE, inscription.textSize);
        bundle.putFloatArray(Inscription.CFG_INSCR_TEXTSCALEX, inscription.textScaleX);
        bundle.putLongArray(Inscription.CFG_INSCR_TEXTCOLOR, inscription.textColor);
        bundle.putFloatArray(Inscription.CFG_INSCR_RADIUS, inscription.radius);
        bundle.putFloatArray(Inscription.CFG_INSCR_ANGLE, inscription.angle);
        bundle.putFloatArray(Inscription.CFG_INSCR_INCLINE, inscription.incline);
        bundle.putLongArray(Inscription.CFG_INSCR_BURNIN, inscription.isBurnIn);
        bundle.putStringArray(Inscription.CFG_INSCR_FONTFAMILY, inscription.fontFamily);
        bundle.putStringArray(Inscription.CFG_INSCR_FONTSTYLE, inscription.fontStyle);
        bundle.putLongArray(Inscription.CFG_INSCR_BEND, inscription.bend);
        bundle.putLongArray(Inscription.CFG_INSCR_DIRECTION, inscription.direction);
        bundle.putLongArray(Inscription.CFG_INSCR_FX, inscription.fx);
        //
        pack.putBundle(Inscription.CFG_INSCR_FULLBUNDLE, bundle);

//        Log.i(TAG, "(((( bundle, bundle=" + bundle);
//        for(int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
//            Log.i(TAG, "((((( bundle, fontStyle[" + i + "]=" + inscription.fontStyle[i]);
//        }

    } // bundleInscription


    public static boolean fxRelief(WatchAppearance appearance, int index) {
        return (Inscription.BEND_STRAIGHT == ((int) appearance.mPrint.bend[index]) &&
                (Inscription.FX_DEBOSS == ((int) appearance.mPrint.fx[index]) ||
                        Inscription.FX_EMBOSS == ((int) appearance.mPrint.fx[index])));
    }

    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }

//    public static boolean isRTL(Locale locale) {
//        return Character.getDirectionality(locale.getDisplayName().charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
//    }

    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public static boolean isRTL(String txt) {
        final int directionality = Character.getDirectionality(txt.charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

} // class Inscription
