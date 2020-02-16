package com.luna_78.wear.watch.face.raf3078.common;

/*
import android.util.Log;

import android.support.wearable.companion.WatchFaceCompanion;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
*/


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Math;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
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
*/

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


/**
 * Created by buba on 04/02/15.
 */
public class ACommon {

    private static final String TAG = "CMN";
    private static final boolean L = false;

    //public static final String CFG_FILE_NAME = ;

    public static final String PACKAGE_NAME = "com.luna_78.airforceru";
    public static final int ALL_HANDS_BMP_WIDTH = 320;
    public static final int LAYOUT_PALETTE_ICON_SIDE_DIMENSION = 195;
    public static final int MSG_DELETION_TIMEOUT = 1;
    public static final int MSG_SCROLL_TO_END = 2;
    public static final int ALL_HANDS_BMP_HEIGHT = 40;
    public static final int TRIANGLE_BMP_DIM = ALL_HANDS_BMP_HEIGHT;
    public static final int TRIANGLE_BMP_SCALED_DIM = 40; // дб чётным!!!
    //public static final String WEARABLE_DATA_PATH = "/rus_airforce_wf/test";
    public static final int PICK_SENDER_REQUEST = 1;
    public static final String WEAR_CRASHREPORT_FILE_SUFFIX = "-acra";
    public static final String PENDING_CRASH_REPORT = "PENDING REPORT(s) FOLLOWS";
    //
    // see string-array "aux_dial_bevel_color" in strings.xml
    public static final int BEVEL_FROM_AUX = 0;
    public static final int BEVEL_FROM_MAIN_DIAL = 1;
    //
    // see string-array "invert_gradient" in strings.xml
    public static final int GD_INVERT_NONE = 0;
    public static final int GD_INVERT_MAIN_DIAL = 1;
    public static final int GD_INVERT_AUX_DIALS = 2;
    public static final int GD_INVERT_ALL = 3;
    //
    //public static final String PHONE_BATTERY_PATH = "/raf3078/phone_battery";
    public static final String ASYNC_REPLY_PATH = "/raf3078/wakeup";
    public static final String ASYNC_REPLY_PATH_2 = "/raf3078/ar2";
    public static final String ASYNC_REPLY_PATH_3 = "/raf3078/ar3";
    public static final String ASYNC_REPLY_PATH_4 = "/raf3078/ar4";
    public static final String FROM_WEAR_PATH = "/raf3078/from_wear";
    public static final String WEAR_TOGGLE_PATH = "/raf3078/toggle";
    public static final String FROM_HANDHELD_PATH = "/raf3078/from_phone";
    public static final String WEAR_CRASH_PATH = "/raf3078/crash";
    public static final float BATTERY_LEVEL_UNSPECIFIED = -1f;
    public static final String INTENT_PHONE_BATTERY = "PHB.I";
    public static final String EVENT_ACTION = "com.luna_78.raf3078.eventlistener.EVENT_NEW";
    public static final String KEY_EVENT = "event";
    public static final String KEY_TOPEER = "recv2peer";
        //public static final int TYPE_PHONE_BATTERY_SAMPLE = 1;
    public static final String KEY_LEVEL = "level";
    public static final String KEY_TIME = "time";
    public static final String KEY_DELAY = "delay";
    public static final String KEY_COLOR = "color";
    public static final String KEY_VALUE = "value";
    public static final String KEY_SCREENSHOT = "shot";
    public static final String KEY_COLOR_INDEX = "icolor"; //
    public static final String KEY_COLOR_KEY = "ckey";
    //public static final String KEY_CONFIG_PALETTE = "config_palette";
    public static final String KEY_LAYOUTS_PALETTE = "layouts_palette";
    public static final String KEY_CFGPAL_ICON = "cp_icon";
    public static final String KEY_CFGPAL_ICON_AMBIENT = "cp_aicon";
    public static final String KEY_CFGPAL_NAME = "cp_name";
    public static final String KEY_CFGPAL_CONFIG = "cp_config";
    public static final String KEY_GRADIENT_PACK = "pack_grd";
    public static final String KEY_INSCRIPTIONS_PACK = "pack_inscr";
    public static final String KEY_CRASHREPORT_CONTENT = "crashreport";
    public static final String KEY_CRASHREPORT_TIME = "crashtime";
    public static final String KEY_HOUR_MARKS = "arr_hrmarks";
    public static final String KEY_DEMO_PACK = "packdemoprm";
    public static final String KEY_PLATE_TEXTURE_PACK = "pack_plttr";
    public static final String KEY_TIMEZONE_PACK = "pack_tz";
    public static final String KEY_SERIAL_SEQUENCE = "serialSeq";
    //
    public static final String CFG_TIME = "cfg_time";
    public static final String CFG_LAYOUT_INDEX = "CFG_LAYOUT_INDEX";
    public static final String CFG_MAIN_HANDS_INDEX = "CFG_MAIN_HANDS_INDEX";
    public static final String CFG_DOM_INDEX = "CFG_DOM_INDEX";
    public static final String CFG_BACKGROUND_INDEX = "CFG_BACKGROUND_INDEX";
    public static final String CFG_AUX_HANDS_INDEX = "CFG_AUX_HANDS_INDEX";
    public static final String CFG_SHOW_HANDHELD_BATTERY = "show_hhbatt";
    public static final String CFG_SHOW_RIM_ANIMATION = "show_rimanim";
    public static final String CFG_SHOW_HRDIGITS_RELIEF = "show_hrdig_relief";
    public static final String CFG_RESPECT_BURNIN = "respect_burnin";
    public static final String CFG_RESPECT_LOWBIT = "respect_lowbit";
    public static final String CFG_SWEEP_SECONDS = "swipe_seconds";
    //public static final String CFG_TZ_HEMISPHERE = ;
    //
    // CFG_SHOW_DIAL_GRADIENT   mShowDialGradient
    // CFG_DG_FIRST_STOP        dgFirstStop
    // CFG_DG_HALF_EDGE_STOP    dgHalfEdgeStop
    // CFG_DG_EDGE_ALPHA        dgEdgeAlpha
    public static final String CFG_SHOW_DIAL_GRADIENT = "show_dial_grd";
    public static final String CFG_DG_FIRST_STOP = "dg_firststop";
    public static final String CFG_DG_HALF_EDGE_STOP = "dg_halfedgestop";
    public static final String CFG_DG_EDGE_ALPHA = "dg_edgealpha";
    public static final String CFG_DG_FIRST_STOP_1 = "dg_firststop1";
    public static final String CFG_DG_HALF_EDGE_STOP_1 = "dg_halfedgestop1";
    public static final String CFG_DG_EDGE_ALPHA_1 = "dg_edgealpha1";
    public static final String CFG_AUX_BEVEL_COLOR = "aux_bevel_color";
    public static final String CFG_INVERT_GRADIENT = "invert_main_gradient";
    public static final String CFG_HOUR_MARKS = "hourmarks";
    public static final String CFG_HOUR_MARKS_RELIEF = "hourmrks_rlf";
    public static final String CFG_HOUR_MARKS_RELIEF_STRENGTH = "hourmrks_rstr";
    public static final String CFG_DEMOPACK_TRIGGERS = "dpack_trg";
    public static final String CFG_DEMOPACK_VALUES = "dpack_val";
    public static final String CFG_PLATE_TEXTURE_STRENGTH = "fxpltstrng";
    public static final String CFG_PLATE_RELIEF_STRENGTH = "fxpltrlfstrng";
    public static final String CFG_AUXDIAL_TEXTURE_STRENGTH = "fxauxstrng";
    public static final String CFG_COLORIZE_BURNIN_MARGIN = "bimcoloriz";
    public static final String CFG_SHOW_INSCRIPTIONS_RELIEF = "show_inscr_rlf";
    public static final String CFG_INSCRIPTIONS_RELIEF_STRENGTH = "inscr_rstr";
    //
    public static final String CFG_COLORS = "cfg_color";
    //
    //
    public static final int NUM_CFG_COLORS = 65;
    public static final int CFG_AMBIENT_RANGE1_BEGIN = 21;
    public static final int CFG_AMBIENT_RANGE1_END = 27;
    public static final int CFG_AMBIENT_RANGE2_BEGIN = 31;
    public static final int CFG_AMBIENT_RANGE2_END = 49;
    //
    public static final int CFG_MAIN_DIGITS_COLOR = 0;
    public static final int CFG_MAIN_HOURHAND_COLOR = 1;
    public static final int CFG_MAIN_MINUTEHAND_COLOR = 2;
    public static final int CFG_MAIN_BACKGROUND_COLOR = 3;
    public static final int CFG_MAIN_MAINHANDS_COLOR = 4;
    public static final int CFG_MAIN_SECONDSHAND_COLOR = 5;
    public static final int CFG_MAIN_DOMBACK_COLOR = 6;
    public static final int CFG_MAIN_DOMFRONT_COLOR = 7;
    public static final int CFG_MAIN_TICK_COLOR = 8;
    public static final int CFG_MAIN_TICKDIGIT_COLOR = 9;
    public static final int CFG_MAIN_BIGAUX_BACKGROUND_COLOR = 10;
    public static final int CFG_MAIN_BIGAUX_DIGITS_COLOR = 11;
    public static final int CFG_MAIN_BIGAUX_TICKS_COLOR = 12;
    public static final int CFG_MAIN_SMALLAUX_BACKGROUND_COLOR = 13;
    public static final int CFG_MAIN_SMALLAUX_DIGITS_COLOR = 14;
    public static final int CFG_MAIN_SMALLAUX_TICKS1_COLOR = 15;
    public static final int CFG_MAIN_SMALLAUX_TICKS2_COLOR = 16;
    public static final int CFG_MAIN_AUXHANDS_WEEKDAY_COLOR = 17;
    public static final int CFG_MAIN_AUXHANDS_WEARBATT_COLOR = 18;
    public static final int CFG_MAIN_AUXHANDS_PHONEBATT_COLOR = 19;
    public static final int CFG_MAIN_AUXHANDS_MONTH_COLOR = 20;
    //
    public static final int CFG_AMBIENT_DIGITS_COLOR = 21;
    public static final int CFG_AMBIENT_HOURHAND_COLOR = 22;
    public static final int CFG_AMBIENT_MINUTEHAND_COLOR = 23;
    public static final int CFG_AMBIENT_TICK_COLOR = 24;
    public static final int CFG_AMBIENT_TICKDIGIT_COLOR = 25;
    public static final int CFG_AMBIENT_DOM_AUXHANDS_COLOR = 26;
    public static final int CFG_AMBIENT_DECORUPPER_COLOR = 27;
    //
    public static final int CFG_MAIN_DECORUPPER_COLOR = 28;
    public static final int CFG_MAIN_HOURMARK_OUTLINE_COLOR = 29;
    public static final int CFG_MAIN_DOM_FRAME_COLOR = 30;
    //
    public static final int CFG_AMBIENT_DOMBACK_COLOR = 31;
    public static final int CFG_AMBIENT_DOMFRONT_COLOR = 32;
    public static final int CFG_AMBIENT_AUXHANDS_COLOR = 33;
    public static final int CFG_AMBIENT_DOM_FRAME_COLOR = 34;
    //
    public static final int CFG_AMBIENT_TZ_SCRIPTS_COLOR = 35;
    public static final int CFG_AMBIENT_TZ_CIRCLES_COLOR = 36;
    public static final int CFG_AMBIENT_TZ_SIGN_COLOR = 37;
    public static final int CFG_AMBIENT_TZ_POINT_COLOR = 38;
    // 39-49 - free for ambient colors
    public static final int CFG_MAIN_INSCRIPTION_1_COLOR = 50;
    public static final int CFG_MAIN_INSCRIPTION_2_COLOR = 51;
    public static final int CFG_MAIN_INSCRIPTION_3_COLOR = 52;
    public static final int CFG_MAIN_INSCRIPTION_4_COLOR = 53;
    public static final int CFG_MAIN_INSCRIPTION_5_COLOR = 54;
    public static final int CFG_MAIN_INSCRIPTION_6_COLOR = 55;
    public static final int CFG_MAIN_INSCRIPTION_7_COLOR = 56;
    public static final int CFG_FIRST_INSCRIPTION_COLOR = 50;
    public static final int CFG_LAST_INSCRIPTION_COLOR = 56;
    // 57-60 - reserved for inscriptions
    //
    public static final int CFG_MAIN_TZ_SCRIPTS_COLOR = 61;
    public static final int CFG_MAIN_TZ_CIRCLES_COLOR = 62;
    public static final int CFG_MAIN_TZ_SIGN_COLOR = 63;
    public static final int CFG_MAIN_TZ_POINT_COLOR = 64;
    //
    // *** !!! on add colors:
    // 1. correct NUM_CFG_COLORS above;
    // 2. add color description to string array "layout_element_name" in strings.xml of "mobile" module
    // 3. correct CFG_AMBIENT_RANGEx_BEGIN and CFG_AMBIENT_RANGEx_END
    // 4. add string equivalent of integer index in CFG_COLOR_... below
    // !!! ***
    //

    //
    public static final String CFG_COLOR_MAIN_DIGITS = String.valueOf(0);
    public static final String CFG_COLOR_MAIN_HOURHAND = String.valueOf(1);
    public static final String CFG_COLOR_MAIN_MINUTEHAND = String.valueOf(2);
    public static final String CFG_COLOR_MAIN_BACKGROUND = String.valueOf(3);
    public static final String CFG_COLOR_MAIN_MAINHANDS = String.valueOf(4);
    public static final String CFG_COLOR_MAIN_SECONDSHAND = String.valueOf(5);
    public static final String CFG_COLOR_MAIN_DOMBACK = String.valueOf(6);
    public static final String CFG_COLOR_MAIN_DOMFRONT = String.valueOf(7);
    public static final String CFG_COLOR_MAIN_TICK = String.valueOf(8);
    public static final String CFG_COLOR_MAIN_TICKDIGIT = String.valueOf(9);
    public static final String CFG_COLOR_MAIN_BIGAUX_BACKGROUND = String.valueOf(10);
    public static final String CFG_COLOR_MAIN_BIGAUX_DIGITS = String.valueOf(11);
    public static final String CFG_COLOR_MAIN_BIGAUX_TICKS = String.valueOf(12);
    public static final String CFG_COLOR_MAIN_SMALLAUX_BACKGROUND = String.valueOf(13);
    public static final String CFG_COLOR_MAIN_SMALLAUX_DIGITS = String.valueOf(14);
    public static final String CFG_COLOR_MAIN_SMALLAUX_TICKS1 = String.valueOf(15);
    public static final String CFG_COLOR_MAIN_SMALLAUX_TICKS2 = String.valueOf(16);
    public static final String CFG_COLOR_MAIN_AUXHANDS_WEEKDAY = String.valueOf(17);
    public static final String CFG_COLOR_MAIN_AUXHANDS_WEARBATT = String.valueOf(18);
    public static final String CFG_COLOR_MAIN_AUXHANDS_PHONEBATT = String.valueOf(19);
    public static final String CFG_COLOR_MAIN_AUXHANDS_MONTH = String.valueOf(20);
    //
    public static final String CFG_COLOR_AMBIENT_DIGITS = String.valueOf(21);
    public static final String CFG_COLOR_AMBIENT_HOURHAND = String.valueOf(22);
    public static final String CFG_COLOR_AMBIENT_MINUTEHAND = String.valueOf(23);
    public static final String CFG_COLOR_AMBIENT_TICK = String.valueOf(24);
    public static final String CFG_COLOR_AMBIENT_TICKDIGIT = String.valueOf(25);
    public static final String CFG_COLOR_AMBIENT_DOM_AUXHANDS = String.valueOf(26);
    public static final String CFG_COLOR_AMBIENT_DECORUPPER = String.valueOf(CFG_AMBIENT_DECORUPPER_COLOR);
    //
    public static final String CFG_COLOR_MAIN_DECORUPPER = String.valueOf(CFG_MAIN_DECORUPPER_COLOR);
    public static final String CFG_COLOR_MAIN_HOURMARK_OUTLINE = String.valueOf(CFG_MAIN_HOURMARK_OUTLINE_COLOR);
    public static final String CFG_COLOR_MAIN_DOM_FRAME = String.valueOf(CFG_MAIN_DOM_FRAME_COLOR);
    //
    public static final String CFG_COLOR_AMBIENT_DOM_FRAME = String.valueOf(CFG_AMBIENT_DOM_FRAME_COLOR);
    public static final String CFG_COLOR_AMBIENT_DOMBACK = String.valueOf(CFG_AMBIENT_DOMBACK_COLOR);
    public static final String CFG_COLOR_AMBIENT_DOMFRONT = String.valueOf(CFG_AMBIENT_DOMFRONT_COLOR);
    public static final String CFG_COLOR_AMBIENT_AUXHANDS = String.valueOf(CFG_AMBIENT_AUXHANDS_COLOR);
    //
    public static final String CFG_COLOR_MAIN_INSCRIPTION_1 = String.valueOf(CFG_MAIN_INSCRIPTION_1_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_2 = String.valueOf(CFG_MAIN_INSCRIPTION_2_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_3 = String.valueOf(CFG_MAIN_INSCRIPTION_3_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_4 = String.valueOf(CFG_MAIN_INSCRIPTION_4_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_5 = String.valueOf(CFG_MAIN_INSCRIPTION_5_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_6 = String.valueOf(CFG_MAIN_INSCRIPTION_6_COLOR);
    public static final String CFG_COLOR_MAIN_INSCRIPTION_7 = String.valueOf(CFG_MAIN_INSCRIPTION_7_COLOR);
    //
    public static final String CFG_COLOR_MAIN_TZ_SCRIPTS = String.valueOf(CFG_MAIN_TZ_SCRIPTS_COLOR);
    public static final String CFG_COLOR_MAIN_TZ_CIRCLES = String.valueOf(CFG_MAIN_TZ_CIRCLES_COLOR);
    public static final String CFG_COLOR_MAIN_TZ_SIGN = String.valueOf(CFG_MAIN_TZ_SIGN_COLOR);
    public static final String CFG_COLOR_MAIN_TZ_POINT = String.valueOf(CFG_MAIN_TZ_POINT_COLOR);
    public static final String CFG_COLOR_AMBIENT_TZ_SCRIPTS = String.valueOf(CFG_AMBIENT_TZ_SCRIPTS_COLOR);
    public static final String CFG_COLOR_AMBIENT_TZ_CIRCLES = String.valueOf(CFG_AMBIENT_TZ_CIRCLES_COLOR);
    public static final String CFG_COLOR_AMBIENT_TZ_SIGN = String.valueOf(CFG_AMBIENT_TZ_SIGN_COLOR);
    public static final String CFG_COLOR_AMBIENT_TZ_POINT = String.valueOf(CFG_AMBIENT_TZ_POINT_COLOR);


    //    public static ArrayList<String> colorDescription = new ArrayList<String>() {
//        {
//            //Resources resources = get;
//            add(ACommon.CFG_MAIN_DIGITS_COLOR, "Цифры индикации часов"); //Цифры индикации часов
//            //add(ACommon.CFG_MAIN_DIGITS_COLOR, new String())
//            add(ACommon.CFG_MAIN_HOURHAND_COLOR, "Часовая стрелка внутри");
//            add(ACommon.CFG_MAIN_MINUTEHAND_COLOR, "Минутная стрелка внутри");
//            add(ACommon.CFG_MAIN_BACKGROUND_COLOR, "Циферблат");
//            add(ACommon.CFG_MAIN_MAINHANDS_COLOR, "Окантовка главных стрелок");
//            add(ACommon.CFG_MAIN_SECONDSHAND_COLOR, "Секундная стрелка");
//            add(ACommon.CFG_MAIN_DOMBACK_COLOR, "Фон даты");
//            add(ACommon.CFG_MAIN_DOMFRONT_COLOR, "Дата");
//            add(ACommon.CFG_MAIN_TICK_COLOR, "Риски индикации минут");
//            add(ACommon.CFG_MAIN_TICKDIGIT_COLOR, "Цифры индикации минут");
//            //
//            add(ACommon.CFG_MAIN_BIGAUX_BACKGROUND_COLOR, "Фон б. вспом. циферблата");
//            add(ACommon.CFG_MAIN_BIGAUX_DIGITS_COLOR, "Цифры б. вспом. циферблата");
//            add(ACommon.CFG_MAIN_BIGAUX_TICKS_COLOR, "Риски б. вспом. циферблата");
//            //
//            add(ACommon.CFG_MAIN_SMALLAUX_BACKGROUND_COLOR, "Фон м. вспом. циферблата");
//            add(ACommon.CFG_MAIN_SMALLAUX_DIGITS_COLOR, "Цифры м. вспом. циферблата");
//            add(ACommon.CFG_MAIN_SMALLAUX_TICKS1_COLOR, "Риски 1 м. вспом. циферблата");
//            add(ACommon.CFG_MAIN_SMALLAUX_TICKS2_COLOR, "Риски 2 м. вспом. циферблата");
//            //
//            add(ACommon.CFG_MAIN_AUXHANDS_WEEKDAY_COLOR, "Стрелка б. всп. циферблата");
//            //
//            add(ACommon.CFG_MAIN_AUXHANDS_WEARBATT_COLOR, "Стрелка 1 м. всп. циферблата");
//            add(ACommon.CFG_MAIN_AUXHANDS_PHONEBATT_COLOR, "Стрелка 2 м. всп. циферблата");
//            add(ACommon.CFG_MAIN_AUXHANDS_MONTH_COLOR, "Стрелка 3 м. всп. циферблата");
//            //
//            add(ACommon.CFG_AMBIENT_DIGITS_COLOR, "# Цифры индикации часов");
//            add(ACommon.CFG_AMBIENT_HOURHAND_COLOR, "# Часовая стрелка внутри");
//            add(ACommon.CFG_AMBIENT_MINUTEHAND_COLOR, "# Минутная стрелка внутри");
//            add(ACommon.CFG_AMBIENT_TICK_COLOR, "# Риски и цифры индикации");
//            add(ACommon.CFG_AMBIENT_TICKDIGIT_COLOR, "# Цифры индикации минут");
//            add(ACommon.CFG_AMBIENT_DOM_AUXHANDS_COLOR, "# Фон даты и всп. стрелки");
//        }
//    };
    //
    public static final String BCAST_EXTRA_EVENT_TYPE = "BEET";
        public static final int EVT_PHONE_BATTERY_SAMPLE = 1;
        public static final int EVT_WAKEUP = 2;
        public static final int EVT_COLOR_DIGITS = 3;
        public static final int EVT_COLOR_HANDS = 4;
        public static final int EVT_COLOR_BACKGROUND = 5;
        public static final int EVT_CHLAYOUT_DATE = 6;
        public static final int EVT_CHLAYOUT_MAINHANDS = 7;
        public static final int EVT_CHLAYOUT = 8;
        public static final int EVT_COLOR_MAINHANDS = 9;
        public static final int EVT_COLOR_SECONDSHAND = 10;
        public static final int EVT_COLOR_DOM_BACK = 11;
        public static final int EVT_COLOR_DOM_FRONT = 12;
        //public static final int EVT_FRAME_SCREENSHOT = 15;
        public static final int EVT_REQUEST_CREATE_CONFIG = 16;
        public static final int EVT_REQUEST_STORE_CONFIG = 17;
        public static final int EVT_CURRENT_CONFIG = 18;
        public static final int EVT_REQUEST_CURRENT_CONFIG = 19;
        public static final int EVT_CHANGE_INDEXED_COLOR = 20;
        public static final int EVT_COLOR_HOUR_HAND = 21;
        public static final int EVT_COLOR_MINUTE_HAND = 22;
        public static final int EVT_RESET = 23;
        public static final int EVT_COLOR_MAIN_TICK = 24;
        public static final int EVT_COLOR_MAIN_TICKDIGIT = 25;
        public static final int EVT_COLOR_BIGAUX_BACKGROUND = 26;
        public static final int EVT_COLOR_BIGAUX_DIGITS = 27;
        public static final int EVT_COLOR_BIGAUX_TICKS = 28;
        public static final int EVT_COLOR_SMALLAUX_BACKGROUND_COLOR = 29;
        public static final int EVT_COLOR_SMALLAUX_DIGITS_COLOR = 30;
        public static final int EVT_COLOR_SMALLAUX_TICKS1_COLOR = 31;
        public static final int EVT_COLOR_SMALLAUX_TICKS2_COLOR = 32;
        public static final int EVT_COLOR_AUXHANDS_WEEKDAY_COLOR = 33;
        public static final int EVT_COLOR_AUXHANDS_WEARBATT_COLOR = 34;
        public static final int EVT_COLOR_AUXHANDS_PHONEBATT_COLOR = 35;
        public static final int EVT_COLOR_AUXHANDS_MONTH_COLOR = 36;
        public static final int EVT_REQUEST_CURRENT_CONFIG_FOR_FILE = 37;
        public static final int EVT_CURRENT_CONFIG_FOR_FILE = 38;
        public static final int EVT_SET_FULL_CONFIG = 39;
        public static final int EVT_CONFIG_CHANGED = 40;
        public static final int EVT_WEARCFG_TOGGLE_ANIMATION = 41;
        public static final int EVT_WEARCFG_TOGGLE_PHONE_BATTERY = 42;
        public static final int EVT_WEARCFG_TOGGLE_LAYOUT = 43;
        public static final int EVT_HHCFG_SET_PHONE_BATTERY = 44;
        public static final int EVT_HHCFG_SET_RIM_ANIMATION = 45;
        public static final int EVT_REQUEST_WEARCFG_PARAMETERTS = 46;
        public static final int EVT_REPLY_WEARCFG_PARAMETERTS = 47;
        public static final int EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF = 48;
        public static final int EVT_HHCFG_SET_HRDIGITS_RELIEF = 49;
    //
// mAmbientDigitsColor              CFG_AMBIENT_DIGITS_COLOR        EVT_COLOR_AMBIENT_DIGITS
// mAmbientHourHandColor            CFG_AMBIENT_HOURHAND_COLOR      EVT_COLOR_AMBIENT_HOURHAND
// mAmbientMinuteHandColor          CFG_AMBIENT_MINUTEHAND_COLOR    EVT_COLOR_AMBIENT_MINUTEHAND
// mAmbientTicksColor               CFG_AMBIENT_TICK_COLOR          EVT_COLOR_AMBIENT_TICK
// mAmbientTickDigitColor           CFG_AMBIENT_TICKDIGIT_COLOR     EVT_COLOR_AMBIENT_TICKDIGIT
// mAmbientDomAndAuxHandsColor      CFG_AMBIENT_DOM_AUXHANDS_COLOR  EVT_COLOR_AMBIENT_DOM_AUXHANDS
        public static final int EVT_COLOR_AMBIENT_DIGITS = 50;
        public static final int EVT_COLOR_AMBIENT_HOURHAND = 51;
        public static final int EVT_COLOR_AMBIENT_MINUTEHAND = 52;
        public static final int EVT_COLOR_AMBIENT_TICK = 53;
        public static final int EVT_COLOR_AMBIENT_TICKDIGIT = 54;
        public static final int EVT_COLOR_AMBIENT_DOM_AUXHANDS = 55;
        public static final int EVT_WEARCFG_TOGGLE_DIAL_GRADIENT = 56;
        public static final int EVT_HHCFG_SET_DIAL_GRADIENT = 57;
        public static final int EVT_HHCFG_SET_DIAL_GRADIENT_PACK = 58;
        public static final int EVT_DENSE_SCREENSHOT = 59;
        public static final int EVT_AMBIENT_SCREENSHOT = 60;
        public static final int EVT_WAKEUP_AMBIENT_ELEMENT = 61;
        public static final int EVT_HHCFG_SET_AUX_BEVEL_COLOR = 62;
        public static final int EVT_WEARCFG_SET_AUX_BEVEL_COLOR = 63;
        public static final int EVT_WEARCFG_SET_INVERT_GRADIENT = 64;
        public static final int EVT_HHCFG_SET_INVERT_GRADIENT = 65;
        //ACommon.EVT_HHCFG_SET_RESPECT_BURNIN, ACommon.CFG_RESPECT_BURNIN
        public static final int EVT_HHCFG_SET_RESPECT_BURNIN = 66;
        public static final int EVT_WEARCFG_SET_RESPECT_BURNIN = 67;
        public static final int EVT_COLOR_AMBIENT_DECORUPPER = 68;
        public static final int EVT_COLOR_DECORUPPER = 69;
        public static final int EVT_HHCFG_SET_RESPECT_LOWBIT = 70;
        public static final int EVT_WEARCFG_SET_RESPECT_LOWBIT = 71;
        public static final int EVT_HHCFG_SET_SWEEP = 72;
        public static final int EVT_WEARCFG_SET_SWEEP = 73;
        public static final int EVT_COLOR_AMBIENT_DOMBACK = 74;
        public static final int EVT_COLOR_AMBIENT_DOMFRONT = 75;
        public static final int EVT_COLOR_AMBIENT_AUXHANDS = 76;
        public static final int EVT_CURRENT_WATCHFACE_VALUES = 77;
        public static final int EVT_HHCFG_SET_INSCRIPTIONS_PACK = 78;
        public static final int EVT_REQUEST_PLATE_BITMAP = 79;
        public static final int EVT_PLATE_BITMAP = 80;
        public static final int EVT_NEW_CONFIG_SENT = 81;
        public static final int EVT_COLOR_INSCRIPTION_1 = 82;
        public static final int EVT_COLOR_INSCRIPTION_2 = 83;
        public static final int EVT_COLOR_INSCRIPTION_3 = 84;
        public static final int EVT_COLOR_INSCRIPTION_4 = 85;
        public static final int EVT_COLOR_INSCRIPTION_5 = 86;
        public static final int EVT_COLOR_INSCRIPTION_6 = 87;
        public static final int EVT_COLOR_INSCRIPTION_7 = 88;
        public static final int EVT_LAYOUTS_PALETTE_CHANGED = 89;
        public static final int EVT_SIGNAL_HOLDOFF = 90;
        public static final int EVT_SIGNAL_HOLDOFF_UPDATE = 91;
        public static final int EVT_WEAR_CRASHREPORT = 92;
        public static final int EVT_HHCFG_SET_HOUR_MARKS = 93;
        public static final int EVT_HHCFG_SET_DEMO_PACK = 94;
        public static final int EVT_COLOR_HOURMARK_OUTLINE = 95;
        public static final int EVT_HHCFG_SET_PLATE_TEXTURE_PACK = 96;
        public static final int EVT_COLOR_AMBIENT_DOM_FRAME = 97;
        public static final int EVT_COLOR_DOM_FRAME = 98;
        public static final int EVT_HHCFG_COLORIZE_BURNIN_MARGIN = 99;
        public static final int EVT_HHCFG_SET_INSCRIPTIONS_RELIEF = 100;
        public static final int EVT_CURRENT_PREFERENCES = 101;
        public static final int EVT_HHCFG_SET_TIMEZONE_PACK = 102;
        //
        public static final int EVT_COLOR_MAIN_TZ_SCRIPTS = 103;
        public static final int EVT_COLOR_MAIN_TZ_CIRCLES = 104;
        public static final int EVT_COLOR_MAIN_TZ_SIGN = 105;
        public static final int EVT_COLOR_MAIN_TZ_POINT = 106;
        public static final int EVT_COLOR_AMBIENT_TZ_SCRIPTS = 107;
        public static final int EVT_COLOR_AMBIENT_TZ_CIRCLES = 108;
        public static final int EVT_COLOR_AMBIENT_TZ_SIGN = 109;
        public static final int EVT_COLOR_AMBIENT_TZ_POINT = 110;
        //
        public static final int EVT_WEARCFG_TOGGLE_TIME_SOURCE = 111;
        public static final int EVT_WEARCFG_TOGGLE_RESPECT_BURN_IN = 112;
        public static final int EVT_WEARCFG_TOGGLE_SWEEP_SECONDS = 113;
        public static final int EVT_CURRENT_TZ_ARRAY = 114;
        public static final int EVT_HHCFG_SET_TZ_HEMISPHERE = 115;
        public static final int EVT_SIGNAL_HOLDOFF_2 = 116;
        public static final int EVT_WEARCFG_TOGGLE_TZ_HEMISPHERE = 117;


    public static final String WFVALUE_BURNIN_MARGIN = "wfv_bim";
    public static final String WFVALUE_RTL = "wfv_rtl";
    public static final String WFVALUE_SCREEN_WIDTH = "wfv_w";
    public static final String WFVALUE_SCREEN_HEIGHT = "wfv_h";
    public static final String WFVALUE_SCREEN_CENTERX = "wfv_cx";
    public static final String WFVALUE_SCREEN_CENTERY = "wfv_cy";
    public static final String WFVALUE_SCREEN_RADIUS = "wfv_sr";
    public static final String WFVALUE_DIAL_RADIUS = "wfv_dr";

    public static final String WFVALUE_AUXA_CX = "wfv_acx";
    public static final String WFVALUE_AUXA_CY = "wfv_acy";
    public static final String WFVALUE_AUXA_DIM = "wfv_adim";
    public static final String WFVALUE_AUXB_CX = "wfv_bcx";
    public static final String WFVALUE_AUXB_CY = "wfv_bcy";
    public static final String WFVALUE_AUXB_DIM = "wfv_bdim";
    public static final String WFVALUE_AUXC_CX = "wfv_ccx";
    public static final String WFVALUE_AUXC_CY = "wfv_ccy";
    public static final String WFVALUE_AUXC_DIM = "wfv_cdim";



    //
    public static final String BCAST_EXTRA_BATTERY_TIME = "PHB.T";  // time of battery level sample
    public static final String BCAST_EXTRA_BATTERY_LEVEL = "PHB.L"; // battery level

    public static final int NUM_DOM_VARIANTS = 2;
        public static final int DOM_SINGLE = 0;
        public static final int DOM_TRIPLE = 1;
    public static final int NUM_BACKGROUNDS = 4;
        public static final int DIAL_SINGLE_HOLE_VRT = 0;
        public static final int DIAL_TRIPLE_HOLE_VRT = 1;
        public static final int DIAL_SINGLE_HOLE_HRZ = 2;
        public static final int DIAL_TRIPLE_HOLE_HRZ = 3;
    public static final int NUM_MAIN_HAND_SETS = 4;
        public static final int HANDS_STRAGHT = 0;
        public static final int HANDS_RHOMB = 1;
        public static final int HANDS_CURLHEAD = 2;
        public static final int HANDS_ARROW = 3;
    public static final int NUM_AUX_HAND_SETS = 1;

/*
    public class ColorElement {
        int element;
        int color;
        String description;

        ColorElement(int elm, int cl, String dscr) { element=elm; color=cl; description=dscr; }
    } // class ColorElement
    ColorElement colorElement[] = new ColorElement[] {
            new ColorElement(EVT_COLOR_DIGITS, 0, "Цифры часов"),
    };
*/


    public static final String ARG_PAGE = "ARG_PAGE";
    public static final int TAB_PAGE_COUNT = 3;
    public static final int TAB_PAGE_LAYOUT_IND = 0;
    public static final int TAB_PAGE_SETTINGS_IND = 1;
    public static final int TAB_PAGE_COLLECTION_IND = 2;

    //public static final int TAG_ELEMENT_NAME = 0;





//    public static String getLocalNodeId(GoogleApiClient googleApiClient) {
//        NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(googleApiClient).await();
//        return nodeResult.getNode().getId();
//    }
    //
    public static class GetLocalNodeId extends Thread {

        GoogleApiClient mGoogleApiClient;
        Object mLock = new Object();
        String mLocalNodeId = null;

        public GetLocalNodeId(GoogleApiClient googleApiClient) {
            mGoogleApiClient = googleApiClient;
        }

        @Override
        public void run() {
            //super.run();
            NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
            //return nodeResult.getNode().getId();
            synchronized (mLock) {
                mLocalNodeId = nodeResult.getNode().getId();
            }
        }
    } // class GetLocalNodeId








    volatile static long mCounter = 0;
    static Object mLockCounter = new Object();

    public static class WearNetSend extends Thread {
        String              path, wearPeer;
        DataMap             dataMap;
        GoogleApiClient     mGoogleApiClient;
        long                counter;

        // Constructor for sending data objects to the data layer
        public WearNetSend(String p, DataMap data, GoogleApiClient googleApiClient, String wearPeer) {
            path = p;
            dataMap = data;
            mGoogleApiClient = googleApiClient;
            this.wearPeer = wearPeer;
            synchronized (mLockCounter) {
                mCounter++;
                counter = mCounter;
            }
        }

        public void run() {
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            for (Node node : nodes.getNodes()) {
//
//                // Construct a DataRequest and send over the data layer
//                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                putDMR.getDataMap().putAll(dataMap);
//                PutDataRequest request = putDMR.asPutDataRequest();
//                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
//                if (result.getStatus().isSuccess()) {
//                    Log.i(TAG, "SendThroughWearNetworkThread DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                } else {
//                    // Log an error
//                    Log.i(TAG, "SendThroughWearNetworkThread ERROR: failed to send DataMap");
//                }
//            }

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            List<Node> nodeList;
            int numNodes = 0;
            if (nodes != null) {
                nodeList = nodes.getNodes();
                if (nodeList != null) numNodes = nodeList.size();
            }
            if (numNodes > 0) {

                dataMap.putLong(ACommon.KEY_SERIAL_SEQUENCE, counter); //"serialSeq"

                if (null != wearPeer) {
                    dataMap.putString(ACommon.KEY_TOPEER, wearPeer);
                }
                //Log.i(TAG, "((( WearNetSend dataMap=" + dataMap + ", path=" + path);
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();

                //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
                int event = dataMap.getInt(ACommon.KEY_EVENT);
                if (ACommon.EVT_WEARCFG_TOGGLE_LAYOUT == event) {
                    boolean success = result.getStatus().isSuccess();
                    //Log.i(TAG, "#TOGGLE_LAYOUT success=" + success + ", serialSeq=" + counter); //dataMap.getLong(ACommon.KEY_SERIAL_SEQUENCE)
                }
            }
        }
    } // class WearNetSend

    public static void requestCurrentConfig(GoogleApiClient googleApiClient) {
//        DataMap dataMap = new DataMap();
//        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CURRENT_CONFIG);
//        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//        new WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, googleApiClient).start();
        requestCurrentConfig(googleApiClient, null);
    }
    public static void requestCurrentConfig(GoogleApiClient googleApiClient, String wearPeer) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CURRENT_CONFIG);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        new WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, googleApiClient, wearPeer).start();
    }



























    public static enum TextVertAlign {Top, Middle, Baseline, Bottom};

    public static void drawHvAlignedText(Canvas canvas, float x, float y, String s, Paint p, Paint.Align horizAlign, TextVertAlign vertAlign) {
        //set horizontal alignment
        p.setTextAlign(horizAlign);

        //get bounding rectangle
        Rect r = new Rect();
        p.getTextBounds(s, 0, s.length(), r);

        //compute y-coordinate we'll need for drawing text for specified vertical alignment
        float textX = x;
        float textY = y;
        switch (vertAlign) {
            case Top:
                textY = y - r.top; // recall that r is negative
                break;
            case Middle:
                textY = y - r.top - r.height()/2;
                break;
            case Baseline:
                break;
            case Bottom:
                textY = y - (r.height() + r.top);
                break;
        }
        canvas.drawText(s, textX, textY, p);
    } // drawHvAlignedText


















    // https://gist.github.com/aprock/2037883
    public static String serializeBundle(final Bundle bundle) {
        //Log.i(TAG, "*** SERIALIZE BUNDLE");
        String base64 = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            base64 = null;
        } finally {
            parcel.recycle();
        }
        return base64;
    }

    // https://gist.github.com/aprock/2037883
    public static Bundle deserializeBundle(final String base64) {
        //Log.i(TAG, "*** DESERIALIZE BUNDLE");
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            bundle = null;
        }  finally {
            parcel.recycle();
        }

        return bundle;
    }





    public static Bundle readPersistentDataFromBinaryFile(Context context, String fileName) {

        //multipurpose method; fileName can contain any kind of data !!!!!!

        //Log.i(TAG, "((( READ PERSISTENT DATA, from BINARY = " + fileName);

        Bundle bundle = null;
        Parcel parcel = Parcel.obtain();
        try {
            //String fileName = this.getString(R.string.configFileName);
            FileInputStream fis;
            try {
                fis = context.openFileInput(fileName);
            } catch (IllegalArgumentException e) {
                fis = new FileInputStream(new File(fileName));
            }
            byte[] array = new byte[(int) fis.getChannel().size()];
            fis.read(array, 0, array.length);
            fis.close();
            parcel.unmarshall(array, 0, array.length);
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
            //bundle = parcel.readBundle(ACommon.class.getClassLoader());
            //testBundle(parcel);
            //Log.i(TAG, "((( readPersistentDataFromFile, bundle=" + bundle.toString());
//            ((( readPersistentDataFromFile, bundle=Bundle[mParcelledData.dataSize=12139712]
//            W/Parcel﹕ Attempt to read object from Parcel 0x5939bf00 at offset 8 that is not in the object list
//            java.lang.RuntimeException: Parcel android.os.Parcel@42614930: Unmarshalling unknown type code 6357104 at offset 24
//            at android.os.Parcel.readValue(Parcel.java:2038)
//            at android.os.Parcel.readMapInternal(Parcel.java:2255)
//            at android.os.Bundle.unparcel(Bundle.java:223)
//            at android.os.Bundle.putAll(Bundle.java:302)
//            at com.luna_78.wear.watch.face.raf3078.common.ACommon.readPersistentDataFromFile(ACommon.java:673)
            bundle.putAll(bundle);

        } catch (FileNotFoundException fnfe) {
            bundle = null;
        } catch (Exception ioe) {
            bundle = null;
        } finally {
            parcel.recycle();
        }

        return bundle;
    } // readPersistentDataFromBinaryFile

    public static Bundle readPersistentDataFromXmlFile(Context context, String fileName) {

        //multipurpose method; fileName can contain any kind of data !!!!!!

        //Log.i(TAG, "((( READ PERSISTENT DATA from XML = " + fileName);

        Bundle bundle = null;

        //SerializerXML.traverseBundle(bundle);
        SerializerXML serializerXml = new SerializerXML(context, null);
//            String resultXml = serializerXml.bundleToXmlString(bundle);
//            Log.i(TAG, "((( XML len = " + resultXml.length());
//            //Log.i(TAG, "((( XML = " + resultXml);
//            String productId = context.getResources().getString(R.string.product_id);
//            // adb -s 0a3d818c pull /mnt/shell/emulated/0/Pictures/
//            // adb -s 0a3d818c pull /data/local/tmp/com.luna_78.airforceru
//            //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "RD-" + fileName + ".xml");
//            File file = new File("RD-" + fileName + ".xml");
//            Log.i(TAG, "((( file path = " + file.getAbsolutePath() );
//            OutputStream fos = new FileOutputStream(file);
//            //byte[] arr = new byte[resultXml.length()];
//            //resultXml.;
//            fos.write(resultXml.getBytes());
//            fos.flush();
//            fos.close();
        //
        String productId = context.getResources().getString(R.string.product_id);
        String fname = fileName;
        if (!fileName.endsWith(productId)) fname = fileName + "." + productId;
//        String fname = fileName + "." + productId;
        //String fname = "RD-" + fileName + ".xml";
        //Log.i(TAG, "((( file = " + fname );
//        boolean resultXml = serializerXml.bundleToXmlFile(bundle, fname);
//        Log.i(TAG, "((( XML serializerXml.bundleToXmlFile, result = " + resultXml);
        Bundle bundleClean = serializerXml.xmlFileToBundle(fname);
        //Log.i(TAG, "((( XML serializerXml.xmlFileToBundle, bundle = " + (bundleClean!=null));
        if (null != bundleClean) bundle = bundleClean;

        return bundle;
    } // readPersistentDataFromXmlFile

    public static Bundle readPersistentDataFromFile(Context context, String fileName) {
            Bundle bundle = null;

            //Bundle bundleBin = readPersistentDataFromBinaryFile(context, fileName);
            Bundle bundleXml = readPersistentDataFromXmlFile(context, fileName);
            bundle = bundleXml;
            //bundle = bundleBin;

            System.gc();

            return bundle;
    } // readPersistentDataFromFile



    public static boolean savePersistentDataToBinaryFile(Context context, String fileName, Bundle bundle) {

        //multipurpose method; bundle can contain any kind of data !!!!!!

        //Log.i(TAG, "((( WRITE PERSISTENT DATA to BINARY = " + fileName);

        boolean retVal = true;
        Parcel parcel = Parcel.obtain();        //creating empty parcel object

        try {
            //String fileName = context.getString(R.string.configFileName);
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            //Parcel p = Parcel.obtain();
            bundle.writeToParcel(parcel, 0);     //saving bundle as parcel
            fos.write(parcel.marshall());        //writing parcel to file
            fos.flush();
            fos.close();
        } catch (FileNotFoundException fnfe) {
            retVal = false;
        } catch (IOException ioe) {
            retVal = false;
        } finally {
            parcel.recycle();
        }

        return retVal;
    } // savePersistentDataToBinaryFile


//    public static class SaveAppearance extends Thread {
//
//        Object syncObject;
//        Context context;
//        String fileName;
//        WatchAppearance appearance;
//
//        public SaveAppearance(Object syncObject, Context context, String fileName, WatchAppearance appearance) {
//            this.syncObject = syncObject;
//            this.context = context;
//            this.fileName = fileName;
//            this.appearance = appearance;
//        }
//
//        public void run() {
//            synchronized (syncObject) {
//                Bundle config = appearance.bundleConfig(0L);
//                ACommon.savePersistentDataToXmlFile(context, fileName, config);
//            }
//        }
//    }


    // from here: http://stackoverflow.com/questions/11408154/how-to-get-file-permission-mode-programmatically-in-java
    public static void chmod(String path, int mode) throws Exception {
        Class<?> libcore = Class.forName("libcore.io.Libcore");
        Field field = libcore.getDeclaredField("os");
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object os = field.get(field);
        Method chmod = os.getClass().getMethod("chmod", String.class, int.class);
        chmod.invoke(os, path, mode);
    }



    public static boolean savePersistentDataToXmlFile(Context context, String fileName, Bundle bundle) {

        //multipurpose method; bundle can contain any kind of data !!!!!!

        boolean retVal = true;

//        if (L) {
//            Log.i(TAG, "((( WRITE PERSISTENT DATA to XML = " + fileName);
//            Writer writer = new StringWriter(); (new Exception()).printStackTrace(new PrintWriter(writer)); String s = writer.toString();
//            String value = "UNDEFINED";
//            if (bundle.containsKey(ACommon.CFG_COLORIZE_BURNIN_MARGIN)) {
//                value = "" + bundle.getBoolean(ACommon.CFG_COLORIZE_BURNIN_MARGIN);
//            }
//            Log.i(TAG, "((( savePersistentDataToXmlFile, COLORIZE_BURNIN_MARGIN = " + value + ", calling stack trace = " + s);
//        }


//        //SerializerXML.traverseBundle(bundle);
//        SerializerXML serializerXml = new SerializerXML(null);
//        String resultXml = serializerXml.bundleToXmlString(bundle);
//        Log.i(TAG, "((( XML = " + resultXml);
        SerializerXML serializerXml = new SerializerXML(context, null);
        //adb -s 0a3d818c pull /mnt/shell/emulated/0/Pictures/test.xml
        //String fname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/WR-" + fileName + ".xml";
        String productId = context.getResources().getString(R.string.product_id);
        String fname = fileName;
        if (!fileName.endsWith(productId)) fname = fileName + "." + productId;
        boolean resultXml = serializerXml.bundleToXmlFile(bundle, fname);
        //Log.i(TAG, "((( serializerXml.bundleToXmlFile, result = " + resultXml);
        retVal = resultXml;

//        Parcel parcel = Parcel.obtain();        //creating empty parcel object
//
//        try {
//            //String fileName = context.getString(R.string.configFileName);
//            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//            //Parcel p = Parcel.obtain();
//            bundle.writeToParcel(parcel, 0);     //saving bundle as parcel
//            fos.write(parcel.marshall());        //writing parcel to file
//            fos.flush();
//            fos.close();
//        } catch (FileNotFoundException fnfe) {
//            retVal = false;
//        } catch (IOException ioe) {
//            retVal = false;
//        } finally {
//            parcel.recycle();
//        }

        return retVal;
    } // savePersistentDataToXmlFile

    public static boolean savePersistentDataToFile(Context context, String fileName, Bundle bundle) {
        boolean retVal = true;

        //boolean resBin = savePersistentDataToBinaryFile(context, fileName, bundle);
        boolean resXml = savePersistentDataToXmlFile(context, fileName, bundle);
        retVal = resXml;

        System.gc();

        return retVal;
    } // savePersistentDataToFile






    public static void SaveConfigPalette(ArrayList<String> configPaletteList, String cfn, Context context) {
        if (null == configPaletteList) return;
        //Log.i(TAG, "((( SaveConfigPalette(), configPaletteList.size() = " + configPaletteList.size());
        //if (mConfigPalette.size() == 0) return;
        //String cfn = getString(R.string.configFileName);
        Bundle configPalette = new Bundle();
        //configPalette.putStringArrayList(ACommon.KEY_CONFIG_PALETTE, configPaletteList);
        configPalette.putStringArrayList(ACommon.KEY_LAYOUTS_PALETTE, configPaletteList);
        ACommon.savePersistentDataToFile(context, cfn, configPalette);
    } // SaveConfigPalette


    public static void SaveLayoutsPalette(LayoutsPalette palette, String fileName, Context context) {
        if (null == palette) return;

    } // SaveLayoutsPalette







//    FileInputStream fis;
//    try {
//        fis = context.openFileInput(fileName);
//    } catch (IllegalArgumentException e) {
//        fis = new FileInputStream(new File(fileName));
//    }


    public static void listLocalFiles(Context context) {
        String[] files = context.fileList();
        int count=0;
        for (String fileName : files) {
            count++;
            File file = context.getFileStreamPath(fileName);
            //Log.i(TAG, "((( localFiles, file[" + count + "] = " + fileName + ", size=" + file.length());

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


    public static void copyFile(File source, File destination) throws IOException {
        InputStream sourceInputStream = new FileInputStream(source);
        FileOutputStream destinationOutputStream = new FileOutputStream(destination);
        byte[] buf = new byte[1024];
        int len;

        while ((len = sourceInputStream.read(buf)) > 0) {
            destinationOutputStream.write(buf, 0, len);
        }

        sourceInputStream.close();
        destinationOutputStream.close();
    }
    public static void copyFile(InputStream sourceInputStream, File destination) throws IOException {
        FileOutputStream destinationOutputStream = new FileOutputStream(destination);
        byte[] buf = new byte[1024];
        int len;

        while ((len = sourceInputStream.read(buf)) > 0) {
            destinationOutputStream.write(buf, 0, len);
        }

        sourceInputStream.close();
        destinationOutputStream.close();
    }
    public static boolean createLayoutPaletteFile(Context context, String cfgFileName) {
        //Log.i(TAG, "createLayoutPaletteFile()");
        String productId = context.getResources().getString(R.string.product_id);
        String fname = cfgFileName;
        if (!cfgFileName.endsWith(productId)) fname = fname + "." + productId;

        File f = new File(context.getFilesDir(), fname);

        if (!f.exists()) {
            AssetManager assets = context.getResources().getAssets();

            try {
                copyFile(assets.open(fname), f);
            } catch (IOException e) {
                //Log.e("FileProvider", "Exception copying from assets", e);
                return false;
            }

            return true;
        }
        return false;
    }





    public static Bitmap loadBitmap(Context context, int drawableId) {

        Bitmap dialBitmap, dialIcon, frameIcon, frameBitmap, resultBitmap;
        final float BASE_DIMENSION = 500f;

//        dialIcon = mService.getLastDenseScreenshot();
//        if (null == dialIcon) return;

        Bitmap dummy = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888);
        int iconDensity = dummy.getDensity();

        //int density = dialIcon.getDensity();
//        float dialWidth = dialIcon.getWidth();
//        float dialHeight = dialIcon.getHeight();
//        float scale = (dialWidth / BASE_DIMENSION);

//                Drawable drawable;
//                drawable = ContextCompat.getDrawable(mActivity.getApplicationContext(), R.drawable.adv_frame);
//                frameIcon = ((BitmapDrawable) drawable).getBitmap();

        Resources resources = context.getApplicationContext().getResources();
        BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inDensity = iconDensity; // !!!!!!!
        bmfOptions.inScaled = false;
        bmfOptions.inDither = false;
        bmfOptions.inSampleSize = 1;
        bmfOptions.inJustDecodeBounds = false;
        //bmfOptions.inPreferQualityOverSpeed = true;
        //bmfOptions.inPremultiplied = false;
        bmfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                bmfOptions.outWidth = 564;
//                bmfOptions.outHeight = 564;
        frameIcon = BitmapFactory.decodeResource(resources, drawableId, bmfOptions);
//        frameIcon = Bitmap.createScaledBitmap(
//                BitmapFactory.decodeResource(resources, drawableId, bmfOptions),
//                (int) (frameIcon.getWidth() * scale) - 2, (int) (frameIcon.getHeight() * scale) - 2, true
//        );

        return frameIcon;
    } // loadBitmap





    public static Bitmap ConfigPaletteGetBitmap(String base64, boolean ambient) {
        byte byteArray[];
        Bitmap bmp = null;

        Bundle configPaletteElement = ACommon.deserializeBundle(base64);

        System.gc();

        if (!ambient) byteArray = configPaletteElement.getByteArray(ACommon.KEY_CFGPAL_ICON);
        else byteArray = configPaletteElement.getByteArray(ACommon.KEY_CFGPAL_ICON_AMBIENT);

        if (null != byteArray) {
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            bmp = BitmapFactory.decodeStream(bais);
            //mService.setLastFrameScreenshot(bmp);
        }
        return bmp;
    }
    public static String ConfigPaletteGetName(String base64) {
        Bundle configPaletteElement = ACommon.deserializeBundle(base64);

        System.gc();

        String name = configPaletteElement.getString(ACommon.KEY_CFGPAL_NAME);
        return name;
    }
    public static String ConfigPaletteSetName(String base64, String newName) {
        Bundle configPaletteElement = ACommon.deserializeBundle(base64);

        System.gc();

        configPaletteElement.putString(ACommon.KEY_CFGPAL_NAME, newName);
        return serializeBundle(configPaletteElement);
    }






    public static float pixelEquivalent(float nPixels, float screenRadius) {
        // nPixels - количество пикселей при размере экрана 320px x 320px
        // возвращает количество пикселей для целевого экрана с радиусом screenRadius, эквивалентное nPixels для экрана с радиусом 160px
        return nPixels * ((1f / 160f) * screenRadius);
    }


    public static boolean isActivityAlive(Activity activity) {
        boolean result = true;
        if (null == activity) {
            //Log.i(TAG, "((( isActivityAlive, activity=" + activity);
            return false;
        }
        if (!activity.getWindow().getDecorView().getRootView().isShown()) {
            //Log.i(TAG, "((( isActivityAlive, activity NOT SHOWN");
            return false;
        }
        if (activity.isDestroyed()) {
            //Log.i(TAG, "((( isActivityAlive, activity DESTROYED");
            return false;
        }
        if (activity.isFinishing()) {
            //Log.i(TAG, "((( isActivityAlive, activity FINISHING");
            return false;
        }
        return result;
    }








    //                        Collections.sort(mActivity.gColorPalette, new Comparator<Integer>() {
//                            @Override
//                            public int compare(Integer lhs, Integer rhs) {
//                                int result = 0;
//                                float floatDiff, hueDiff;
//
//                                float[] leftHSV = new float[3], rightHSV = new float[3];
//                                int alpha, red, green, blue;
//                                int newAlpha, newRed, newGreen, newBlue;
//                                float coeff;
//                                Integer left, right, reference;
//                                reference = Color.argb(255, 255, 255, 255);
//                                //
//                                alpha = Color.alpha(lhs); red = Color.red(lhs); green = Color.green(lhs); blue = Color.blue(lhs);
//                                coeff = (float)alpha / 255f;
//                                newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
//                                left = Color.argb(255, newRed, newGreen, newBlue);
//                                Color.colorToHSV(left, leftHSV);
//                                Log.i(TAG, "((( sort, left H=" + leftHSV[0] + " R=" + newRed + ", G=" + newGreen + ", B=" + newBlue);
//                                //
//                                alpha = Color.alpha(rhs); red = Color.red(rhs); green = Color.green(rhs); blue = Color.blue(rhs);
//                                coeff = (float)alpha / 255f;
//                                newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
//                                right = Color.argb(255, newRed, newGreen, newBlue);
//                                Color.colorToHSV(right, rightHSV);
//                                Log.i(TAG, "((( sort, right H=" + rightHSV[0] + " R=" + newRed + ", G=" + newGreen + ", B=" + newBlue);
//
//                                hueDiff = leftHSV[0] - rightHSV[0];
//                                if ((hueDiff==0) && (leftHSV[0]!=0 || rightHSV[0]!=0)) {
//                                    float valDiff = leftHSV[2] - rightHSV[2];
//                                    if (valDiff > 0f) result = 1;
//                                    if (valDiff == 0f) result = 0;
//                                    if (valDiff < 0f) result = -1;
//                                    Log.i(TAG, "((( sort, result=" + result + ", valDiff=" + valDiff);
//                                } else if (hueDiff!=0f && (hueDiff < 15f && hueDiff > -15f)) {
//                                    if (hueDiff > 0f) result = 1;
//                                    if (hueDiff == 0f) result = 0;
//                                    if (hueDiff < 0f) result = -1;
//                                    Log.i(TAG, "((( sort, result=" + result + ", hueDiff=" + hueDiff);
//                                } else {
//                                    float distanceL2REF = ACommon.colorDistance(left, reference);
//                                    float distanceR2REF = ACommon.colorDistance(right, reference);
//
//                                    floatDiff = distanceL2REF - distanceR2REF;
//                                    if (floatDiff > 0f) result = 1;
//                                    if (floatDiff == 0f) result = 0;
//                                    if (floatDiff < 0f) result = -1;
//                                    Log.i(TAG, "((( sort, result=" + result + ", distanceL2REF=" + distanceL2REF + ", distanceR2REF=" + distanceR2REF);
//                                }
//
//
//
//                                return result;
//                            }
//                        });





    public static int bubbleSort(ArrayList <Integer> list)
    {
        int count = 0;
        for (int outer = 0; outer < list.size() - 1; outer++) {
            for (int inner = 0; inner < list.size() - outer - 1; inner++) {
                if (compareEm(list.get(inner), list.get(inner + 1)) > 0) { //list.get(inner) > list.get(inner + 1)
                    swapEm(list, inner);
                    count = count + 1;
                }
            }
        }
        return count;
    }
    static void swapEm(ArrayList<Integer>list, int inner)
    {
        Integer temp = list.get(inner);
        list.set(inner, list.get(inner + 1));
        list.set(inner + 1, temp);
    }
    static int compareEm(Integer lhs, Integer rhs) {
        int result = 0;
        float floatDiff, hueDiff;

        float[] leftHSV = new float[3], rightHSV = new float[3];
        int alpha, red, green, blue;
        int newAlpha, newRed, newGreen, newBlue;
        float coeff;
        Integer left, right, reference;
        reference = Color.argb(255, 255, 255, 255);
        ColorDominant domLeft, domRight;
        //
        alpha = Color.alpha(lhs); red = Color.red(lhs); green = Color.green(lhs); blue = Color.blue(lhs);
        coeff = (float)alpha / 255f;
        newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
        left = Color.argb(255, newRed, newGreen, newBlue);
        Color.colorToHSV(left, leftHSV);
        domLeft = ACommon.colorDominant(left);
        //Log.i(TAG, "((( sort, left H=" + leftHSV[0] + " R=" + newRed + ", G=" + newGreen + ", B=" + newBlue);
        //
        alpha = Color.alpha(rhs); red = Color.red(rhs); green = Color.green(rhs); blue = Color.blue(rhs);
        coeff = (float)alpha / 255f;
        newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
        right = Color.argb(255, newRed, newGreen, newBlue);
        Color.colorToHSV(right, rightHSV);
        domRight = ACommon.colorDominant(right);
        //Log.i(TAG, "((( sort, right H=" + rightHSV[0] + " R=" + newRed + ", G=" + newGreen + ", B=" + newBlue);

        if (domLeft != domRight) {
            result = domLeft.ordinal() - domRight.ordinal();
        } else {
            hueDiff = leftHSV[0] - rightHSV[0];
            if ((hueDiff == 0)) {
                float valDiff = leftHSV[2] - rightHSV[2];
                if (valDiff == 0) {
                    float satDiff = leftHSV[1] - rightHSV[1];
                    if (satDiff > 0f) result = 1;
                    if (satDiff == 0f) result = 0;
                    if (satDiff < 0f) result = -1;
                    //Log.i(TAG, "((( sort, result=" + result + ", satDiff=" + satDiff);
                } else {
                    if (valDiff > 0f) result = 1;
                    if (valDiff == 0f) result = 0;
                    if (valDiff < 0f) result = -1;
                    //Log.i(TAG, "((( sort, result=" + result + ", valDiff=" + valDiff);
                }
            } else {
                if (hueDiff > 0f) result = 1;
                if (hueDiff == 0f) result = 0;
                if (hueDiff < 0f) result = -1;
                //Log.i(TAG, "((( sort, result=" + result + ", hueDiff=" + hueDiff);
//                float distanceL2REF = ACommon.colorDistance(left, reference);
//                float distanceR2REF = ACommon.colorDistance(right, reference);
//
//                floatDiff = distanceL2REF - distanceR2REF;
//                if (floatDiff > 0f) result = 1;
//                if (floatDiff == 0f) result = 0;
//                if (floatDiff < 0f) result = -1;
//                Log.i(TAG, "((( sort, result=" + result + ", distanceL2REF=" + distanceL2REF + ", distanceR2REF=" + distanceR2REF);
            }
        }

        return result;
    }




    public static float colorDistance(int left, int right) {
        float result = 0f;
        int leftR, rightR;
        leftR = Color.red(left);
        rightR = Color.red(right);
        //float rMean = (float)(leftR + rightR) / 2f;
        float rMean = (float)(rgbToY(left) + rgbToY(right)) / 2f;
        float deltaR = (float)(Color.red(left) - Color.red(right));
        float deltaG = (float)(Color.green(left) - Color.green(right));
        float deltaB = (float)(Color.blue(left) - Color.blue(right));
        result = (float) Math.sqrt((((512f + rMean) * deltaR * deltaR) / 256f) + (4f * deltaG * deltaG) + (((767f - rMean) * deltaB * deltaB) / 256f));
        return result;
    }

    public enum ColorDominant {NEUTRAL, COLOR, RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, MAGENTA}

    public static ColorDominant colorDominant(int color) {
        ColorDominant result = ColorDominant.COLOR;

        float r, g, b, d;
        r = (float)Color.red(color);
        g = (float)Color.green(color);
        b = (float)Color.blue(color);
        if (r == g && r == b) return ColorDominant.NEUTRAL;

        if (true) return ColorDominant.COLOR;



        float kR, nR, kG, nG, kB, nB;
        kR = (float)Color.red(color) / 255f; nR = kR * 0.21f;
        kG = (float)Color.green(color) / 255f; nG = kG * 0.72f;
        kB = (float)Color.blue(color) / 255f; nB = kB * 0.07f;
        float R = (kR + nR) / 2f, G = (kG + nG) / 2f, B = (kB + nB) / 2f;


        //float y = rgbToY(color);
        d = Math.max(Math.max(R, G), B);
        //d = Math.max(d, B);

        //Log.i(TAG, "((( colorDominant, d=" + d + "; R=" + R + " G=" + G + " B=" + B);

        if (d == R) {
            if (R == G) return ColorDominant.GREEN;
            return ColorDominant.RED;
        }
        if (d == G) {
            return ColorDominant.GREEN;
        }
        if (d == B) {
            if (B == R) return ColorDominant.RED;
            return ColorDominant.BLUE;
        }

        return result;
    }

    private static int rgbToY(int color) {
        float a, r, g, b, y, rgby;
        //a = ((color & 0xff000000) >>> 24) / 255f;
        r = ((color & 0x00ff0000) >> 16) / 255f;
        g = ((color & 0x0000ff00) >> 8) / 255f;
        b = ((color & 0x000000ff)) / 255f;
        rgby = (float) (255f * (0.21 * r + 0.72 * g + 0.07 * b));
//        if (rgby < 128f) {
//            y = (float) (255f * (0.35 * a + 0.65 * (rgby / 255f)));
//        } else {
//            y = (float) (255f * (0.65 * a + 0.35 * (rgby / 255f)));
//        }
//        //Log.i(TAG, "***** rgbToY = " + (int) y);
        return (int) rgby;
    }

    public static int argbToY(int color) {
        float a, r, g, b, y, rgby, argby;
        a = ((color & 0xff000000) >>> 24) / 255f;
        r = ((color & 0x00ff0000) >> 16) / 255f;
        g = ((color & 0x0000ff00) >> 8) / 255f;
        b = ((color & 0x000000ff)) / 255f;
        rgby = (float) (255f * (0.21 * r + 0.72 * g + 0.07 * b));
        argby = rgby * a;
//        if (rgby < 128f) {
//            y = (float) (255f * (0.35 * a + 0.65 * (rgby / 255f)));
//        } else {
//            y = (float) (255f * (0.65 * a + 0.35 * (rgby / 255f)));
//        }
//        //Log.i(TAG, "***** rgbToY = " + (int) y);
        //return (int) rgby;
        return (int) Math.min(255, ((int)argby));
    }



    // example: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
//            if (fileName.matches("^[0-9]+-approved.stacktrace") || fileName.matches("^ACRA-INSTALLATION")) {
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                File fileOut = new File(path, fileName);
//                Log.i(TAG, "((((( listLocalFiles, file=" + path + "/" + fileName);
//                //adb -s 0a3d818c shell ls -l /sdcard/Pictures/*.png
//                //adb -s 0a3d818c pull /sdcard/Pictures/*.png
//                //adb -s 0a3d818c pull /sdcard/Pictures/ACRA-INSTALLATION
//                try {
//                    ACommon.copyFile(file, fileOut);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }


    /*
        //adb -s 412KPFX0143611 shell ls -l /sdcard/Pictures/*.png
        //adb -s 412KPFX0143611 pull /sdcard/Pictures/
        //adb -s 412KPFX0143611 shell rm /sdcard/Pictures/*.png
     */
    public static void bmpToPicturesDir(Context context, Bitmap bmp) {
        String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
        bmpToPicturesDir(context, bmp, fileName);
    } // bmpToPicturesDir
    //
    /*
        names[0] - prefix for filename
        names[1] - suffix for filename
        names[2] - name body
     */
    public static String bmpToPicturesDir(Context context, Bitmap bmp, String... names) {

        //String other = names.length > n ? names[n] : null;
        String prefix = names.length > 0 ? names[0] : "";
        String suffix = names.length > 1 ? names[1] : "";
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String body = names.length > 2 ? names[2] : timeStamp;

        String fileName = prefix + body + suffix + ".png";
        bmpToPicturesDir(context, bmp, fileName);
        return timeStamp;
    }
    public static String bmpToPicturesDir(Context context, Bitmap bmp, String fileName) {
        int bmpSize = bmp.getByteCount();
//        File iconSoloFile = context.getFileStreamPath(fileName);
//        long fileSize, timeStamp = System.currentTimeMillis();
//        if (iconSoloFile.exists()) {
//            fileSize = iconSoloFile.length();
//            if (fileSize > 0L) {
//                Log.i(TAG, "((( bmpToPicturesDir, file=" + fileName + " exist, size = " + fileSize + "; skipped");
//                //return true;
//            }
//        }

        FileOutputStream fos;
        //String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File fileOut = new File(path, fileName);
        try {

            fos = new FileOutputStream(fileOut);
//            try {
//                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName);
//            } catch (FileNotFoundException e) {
//                fos = new FileOutputStream(new File(fileName));
//                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName);
//            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bmpSize);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            fos.write(byteArray);
            fos.flush();
            fos.close();
            MediaScannerConnection.scanFile(context, new String[]{fileOut.getAbsolutePath()}, null, null);
            //result = true;
        } catch (Exception e) {
            e.printStackTrace();
            //result = false;
        }

        //Log.i(TAG, "((( bmpToFile, file=" + fileName + ", written = " + result);
        return fileName;
    }




    public static Bitmap produceFramedIcon(Context context, Bitmap dialIcon) {
        Bitmap dialBitmap, frameIcon, frameBitmap, resultBitmap;
        final float BASE_DIMENSION = 500f;

        int density = dialIcon.getDensity();
        float dialWidth = dialIcon.getWidth();
        float dialHeight = dialIcon.getHeight();
        float scale = (dialWidth / BASE_DIMENSION);

        Resources resources = context.getResources();
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
        frameBitmap = BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions);
        frameIcon = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions),
                (int) (frameBitmap.getWidth() * scale) - 2, (int) (frameBitmap.getHeight() * scale) - 2, true
        );

        float resultWidth, resultHeight;
        resultWidth = frameIcon.getWidth();
        resultHeight = frameIcon.getHeight();
        resultBitmap = Bitmap.createBitmap((int) resultWidth, (int) resultHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas();
        Path clip = new Path();
        Paint paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);

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

        resultBitmap.eraseColor(Color.TRANSPARENT);
        //canvas = new Canvas();
        canvas.setBitmap(resultBitmap);
        canvas.drawBitmap(dialBitmap, (resultWidth - dialWidth) / 2f, (resultHeight - dialHeight) / 2f, null);
        canvas.drawBitmap(frameIcon, 0, 0, null);

        //String timeStamp = ACommon.bmpToPicturesDir(mActivity.getApplicationContext(), resultBitmap, "adv_", "_1");

        return resultBitmap;
    }


    public static void grantUriPermissionToMailPackages(Context context, Uri contentUri, Intent emailIntent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                //Log.i(TAG, "((((( packageName = " + packageName);
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }



} // class ACommon
