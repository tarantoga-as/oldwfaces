package com.luna_78.wear.watch.face.lightandclassics.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by buba on 24/07/15.
 */
public class WatchTime {
    static final String TAG = ACommon.TAG_PREFIX + "WATCH_TIME";

    TimeZone deviceTz, utcTz = TimeZone.getTimeZone("UTC");

    AppPreferences              appPreferences;
    int                         index = 0;

    GregorianCalendar calendar;
    public DemoPackData         demoPackData[] = new DemoPackData[DemoPackData.NUM_DEMOPACK_PARAMETERS];
    public boolean              clipShot = false; // true while clipshot is running


    // mTime.weekDay:                           0=вс 1=пн 2=вт 3=ср 4=чт 5=пт 6=сб
    // mFirstDayOfWeek && Calendar.DAY_OF_WEEK: 1=вс 2=пн 3=вт 4=ср 5=чт 6=пт 7=сб

    public boolean getClipShot() { return clipShot; }
    public void setClipShot(boolean value) { clipShot = value; }

    private void initDemoPack() {
        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
            demoPackData[i] = new DemoPackData();
        }
    }

    public void putDemoPack(DemoPackData demoPackData[]) {
        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
            this.demoPackData[i].trigger = demoPackData[i].trigger;
            this.demoPackData[i].value = demoPackData[i].value;
        }
    }




    public void setWatchSource(AppPreferences preferences) {
        appPreferences.wtSetWatchSource((appPreferences.isSourceUtc() ? utcTz : deviceTz));
    }

    private void initPreferences(AppPreferences preferences) {
        //Log.i(TAG, "#TIME initPreferences");
        appPreferences = preferences;
        appPreferences.wtInflateTzArray();
        //appPreferences.wtSetWatchSource((appPreferences.isSourceUtc() ? utcTz : deviceTz));
        setWatchSource(preferences);
        index = 0;
    }
    public WatchTime(TimeZone tz, AppPreferences preferences) {
        //Log.i(TAG, "#TIME constructor, tz = " + tz.getID());
        deviceTz = tz;
//        appPreferences = preferences;
//        appPreferences.wtInflateTzArray();
//        appPreferences.wtSetWatchSource((appPreferences.isSourceUtc() ? utcTz : deviceTz));
//        index = 0;
        initPreferences(preferences);
        calendar = new GregorianCalendar(appPreferences.wtGetTz(index));
        //setTz(tz);
        initDemoPack();
    }
    public void setLocale(Locale locale) {
        //Log.i(TAG, "#TIME setLocale, new calendar!!!");
        calendar = new GregorianCalendar(locale);
        setTzForCalendar(appPreferences.wtGetTz(index));
    }

    private void setTzForCalendar(TimeZone tz) {
        //Log.i(TAG, "#TIME setTzForCalendar, tz = " + tz.getID());
        calendar.setTimeZone(tz);
    }

//    public WatchTime() {
//        calendar = new GregorianCalendar();
//        initDemoPack();
//    }
//
//    public WatchTime(Locale locale) {
//        calendar = new GregorianCalendar(locale);
//        initDemoPack();
//    }
//
//    public WatchTime(TimeZone tz, Locale locale) {
//        deviceTz = tz;
//        calendar = new GregorianCalendar(deviceTz, locale);
//        //setTz(tz);
//        initDemoPack();
//    }



//    private void setTz(TimeZone tz) {
//        this.deviceTz = tz;
//        //tzOffsetFromUtc = this.deviceTz.getRawOffset();
//        //Log.i(TAG, "setTz, TZ = " + this.tz.getID() + ", UTC offset = " + tzOffsetFromUtc);
//        //Log.i(TAG, "in DST=" + tz.inDaylightTime(calendar.getTime()));
//    }

    public void setHandHeldTimeZone(TimeZone tz) {
        //Log.i(TAG, "#TIME setHandHeldTimeZone, tz = " + tz.getID());
        //setTz(tz);
        deviceTz = tz;
        if (!appPreferences.isSourceUtc()) {
            appPreferences.wtSetWatchSource(deviceTz);
            if (0==index) setTzForCalendar(deviceTz); //calendar.setTimeZone(deviceTz);
        }
    }

    public void checkHandHeldTimeZone(TimeZone tz) {
        //Log.i(TAG, "#TIME checkHandHeldTimeZone, tz = " + tz.getID());
        index = 0;
        if (!deviceTz.equals(tz)) {
            setHandHeldTimeZone(tz);
        } else {
            setTzForCalendar(appPreferences.wtGetTz(index)); //calendar.setTimeZone(appPreferences.wtGetTz(index));
        }
    }

    public void onTimeTick(TimeZone tz) {
        checkHandHeldTimeZone(tz);
    }



//    public void swapTzOffset() {
//        int offset = this.tz.getRawOffset();
//        if (0 == offset) {
//            this.tz.setRawOffset(tzOffsetFromUtc);
//        } else {
//            this.tz.setRawOffset(0);
//        }
//
//        calendar.setTimeZone(this.tz);
//    }

    public void eventSwapTz() {
        //Log.i(TAG, "#TIME eventSwapTz");
        int i = appPreferences.wtGetNextTz(index);
        //Log.i(TAG, "#TZ eventSwapTz");
        if (i != index) {
            index = i;
            setTzForCalendar(appPreferences.wtGetTz(index)); //calendar.setTimeZone(appPreferences.wtGetTz(index));
        }
    }

    public void eventTzIndicationUpdated(AppPreferences preferences) {
        //Log.i(TAG, "#TIME eventTzIndicationUpdated");
        //appPreferences = preferences;
        initPreferences(preferences);
        setTzForCalendar(appPreferences.wtGetTz(0)); //calendar.setTimeZone(appPreferences.wtGetTz(0)); //index
    }



    public String getEffectiveTzLabel() {
        return appPreferences.wtGetLabel(index);
    }
    public int getEffectiveTzOffset() {
        TimeZone tz = appPreferences.wtGetTz(index);
        return tz.getOffset(getRawNow());//-3600000;
    }
    public boolean isDstActiveInEffectiveTz() {
        TimeZone tz = appPreferences.wtGetTz(index);
        return tz.inDaylightTime(new Date(getRawNow()));
    }
    public boolean isDeviceTz() {
        if (0 != index) return false;
        return appPreferences.wtGetTz(index).equals(deviceTz);
    }
    public boolean isUtcTz() {
        if (0 != index) return false;
        return !appPreferences.wtGetTz(index).equals(deviceTz);
    }











    public int getFirstDayOfWeek() {
        return calendar.getFirstDayOfWeek();
    }

    public void set(long millis) {
        //Log.i(TAG, "#TIME set(long millis)");
        //rawNow = millis;
        calendar.setTimeInMillis(millis);
    }

    public void set(int year, int month, int day, int hourOfDay, int minute, int second) {
        //Log.i(TAG, "#TIME set(...)");
        calendar.set(year, month, day, hourOfDay, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public long getRawNow() { return calendar.getTimeInMillis(); }

    public long getNowInSeconds() { return calendar.getTimeInMillis() / 1000; }

    public int getWeekday() {
        if (DemoPackData.isDate(demoPackData)) return demoPackData[DemoPackData.INDEX_WEEKDAY].value;
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }
    public int getHour() {
        if (DemoPackData.isTime(demoPackData) && !clipShot) return demoPackData[DemoPackData.INDEX_HOUR].value;
        //return calendar.get(Calendar.HOUR);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    public int getMinute() {
        if (DemoPackData.isTime(demoPackData) && !clipShot) return demoPackData[DemoPackData.INDEX_MINUTES].value;
        return calendar.get(Calendar.MINUTE);
    }
    public int getSecond() {
        if (DemoPackData.isTime(demoPackData) && !clipShot) return demoPackData[DemoPackData.INDEX_SECONDS].value;
        return calendar.get(Calendar.SECOND);
    }
    public int getMilliSecond() {
        if (DemoPackData.isTime(demoPackData) && !clipShot) return 0;
        return calendar.get(Calendar.MILLISECOND);
    }
    public int getMonth() {
        if (DemoPackData.isDate(demoPackData) && !clipShot) return demoPackData[DemoPackData.INDEX_MONTH].value;
        return calendar.get(Calendar.MONTH);
    }
    public int getDayOfMonth() {
        if (DemoPackData.isDate(demoPackData) && !clipShot) return demoPackData[DemoPackData.INDEX_DAYOFMONTH].value;
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    public static final int TODAY = 1;
    public static final int TOMORROW = 2;
    public static final int YESTERDAY = 0;
    public void get3DayOfMonth(int[] triplet) {
        if (DemoPackData.isDate(demoPackData) && !clipShot) {
            GregorianCalendar demoCal;
            int year = calendar.get(Calendar.YEAR);
            int month = demoPackData[DemoPackData.INDEX_MONTH].value;
            int day = demoPackData[DemoPackData.INDEX_DAYOFMONTH].value;
            demoCal = new GregorianCalendar(year, month, day);

            demoCal.roll(Calendar.DAY_OF_MONTH, -1);
            triplet[YESTERDAY] = demoCal.get(Calendar.DAY_OF_MONTH);
            demoCal.roll(Calendar.DAY_OF_MONTH, +2);
            triplet[TOMORROW] = demoCal.get(Calendar.DAY_OF_MONTH);
            demoCal.roll(Calendar.DAY_OF_MONTH, -1);
            triplet[TODAY] = demoCal.get(Calendar.DAY_OF_MONTH);
        } else {
            calendar.roll(Calendar.DAY_OF_MONTH, -1);
            triplet[YESTERDAY] = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.roll(Calendar.DAY_OF_MONTH, +2);
            triplet[TOMORROW] = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.roll(Calendar.DAY_OF_MONTH, -1);
            triplet[TODAY] = calendar.get(Calendar.DAY_OF_MONTH);
        }
    }
    public int getMaxDaysInMonth() {
        if (DemoPackData.isDate(demoPackData) && !clipShot) {
            GregorianCalendar demoCal;
            int year = calendar.get(Calendar.YEAR);
            int month = demoPackData[DemoPackData.INDEX_MONTH].value;
            int day = demoPackData[DemoPackData.INDEX_DAYOFMONTH].value;
            demoCal = new GregorianCalendar(year, month, day);
            return demoCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }




//    public int getDayOfYear(int offset) {
//        int result;
//        if (0 != offset) calendar.roll(Calendar.DAY_OF_YEAR, offset);
//        result = calendar.get(Calendar.DAY_OF_YEAR);
//        if (0 != offset) calendar.roll(Calendar.DAY_OF_YEAR, -offset);
//        return result;
//    }


} // class WatchTime
