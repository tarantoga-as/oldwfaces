package com.luna_78.wear.watch.face.raf3078.common;

/**
 * Created by buba on 23/07/15.
 */
public class DemoPackData {

    public static final int NUM_DEMOPACK_PARAMETERS = 11;

    public static final int INDEX_DAYOFMONTH = 0;
    public static final int INDEX_HOUR = 1;
    public static final int INDEX_MINUTES = 2;
    public static final int INDEX_MONTH = 3;
    public static final int INDEX_PHONEBATT = 4;
    public static final int INDEX_RESOLUTION = 5;
    public static final int INDEX_SECONDS = 6;
    public static final int INDEX_WEARBATT = 7;
    public static final int INDEX_WEEKDAY = 8;
    public static final int INDEX_CLIP_SHOT_FPS = 9;
    public static final int INDEX_CLIP_SHOT_DURATION = 10;

    public static final int TRIGGER_DEMOPACK = INDEX_DAYOFMONTH;
    public static final int TRIGGER_DATE = INDEX_MONTH;
    public static final int TRIGGER_PHONEBATT = INDEX_PHONEBATT;
    public static final int TRIGGER_RESOLUTION = INDEX_RESOLUTION;
    public static final int TRIGGER_TIME = INDEX_HOUR;
    public static final int TRIGGER_WEARBATT = INDEX_WEARBATT;
    public static final int TRIGGER_CLIP_SHOT = INDEX_CLIP_SHOT_FPS;

    public static final int RESOLUTION_NATURAL = 0;

    public boolean trigger;
    public int value;

    public DemoPackData() { trigger = false; value = 0; }



    //demoPackData[DemoPackData.TRIGGER_DEMOPACK].trigger

    public static boolean isActive(DemoPackData[] pack) {
        return pack[DemoPackData.TRIGGER_DEMOPACK].trigger;
    }

    public static boolean isTime(DemoPackData[] pack) {
        return (pack[DemoPackData.TRIGGER_DEMOPACK].trigger && pack[DemoPackData.TRIGGER_TIME].trigger);
    }

    public static boolean isDate(DemoPackData[] pack) {
        return (pack[DemoPackData.TRIGGER_DEMOPACK].trigger && pack[DemoPackData.TRIGGER_DATE].trigger);
    }

    public static boolean needStartClipShot(DemoPackData[] pack) {
        boolean result = false;
        if (isActive(pack)) {
            if (pack[DemoPackData.TRIGGER_CLIP_SHOT].trigger) {
                result = true;
                pack[DemoPackData.TRIGGER_CLIP_SHOT].trigger = false;
            }
        }
        return result;
    }

    public static int getResolution(DemoPackData[] pack) {
        int result = RESOLUTION_NATURAL;
        if (RESOLUTION_NATURAL != pack[INDEX_RESOLUTION].value) {
            switch (pack[INDEX_RESOLUTION].value) {
                case 1: result = 280; break;
                case 2: result = 320; break;
//                case 3: result = 500; break;
//                default: result = 400; break;
                default: result = 320 + 16 * pack[INDEX_RESOLUTION].value;
            }
        }
        return result;
    }

    public static int getWearBattery(DemoPackData[] pack) {
        return pack[INDEX_WEARBATT].value;
    }

    public static int getPhoneBattery(DemoPackData[] pack) {
        return pack[INDEX_PHONEBATT].value;
    }

} // class DemoPackData
