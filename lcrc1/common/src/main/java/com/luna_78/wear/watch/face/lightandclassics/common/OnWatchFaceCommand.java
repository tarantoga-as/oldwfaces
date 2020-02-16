package com.luna_78.wear.watch.face.lightandclassics.common;

/**
 * Created by buba on 09/11/15.
 */
public interface OnWatchFaceCommand {
    public void setHandheldBatteryTrigger(boolean value);
    public boolean getHandheldBatteryTrigger();
    public void setConsumeDataTrigger(boolean value);
    public boolean getConsumeDataTrigger();
}
