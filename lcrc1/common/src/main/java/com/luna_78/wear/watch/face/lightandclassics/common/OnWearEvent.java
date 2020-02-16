package com.luna_78.wear.watch.face.lightandclassics.common;

import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by buba on 01/11/15.
 */
public interface OnWearEvent {
    boolean     isPeerIdKnown();
    void        rememberPeerId(String peerId);
    boolean     isLocalPeerIdKnown();
    void        rememberLocalPeerId(String peerId);
//    void        onWearConnFailed(ConnectionResult connectionResult);
//    void        onWearConnected(Bundle bundle);
//    void        onWearConnSuspended(int i);
    void        onWearConnResult(int i);
}
