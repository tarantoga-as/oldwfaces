package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by buba on 30/10/15.
 */
public class PeerId {

    private static final String LOCAL_PEERID_FILE = "localpeerid";
    private static final String PEERID_FILE = "peerid";

    private String          mPeerId = null;
    //private boolean         mIsNull = true;
    private SynBoolean      mIsNull = new SynBoolean(true);
    private Object          mLockPeerId = new Object();


    public String getPeerId() {
        String peerId = null;
        synchronized (mLockPeerId) {
            if (null != mPeerId) {
                peerId = new StringBuilder(mPeerId).toString();
//                //mIsNull = false;
//                //mIsNull.setValue(false);
//            } else {
//                //mIsNull = true;
//                //mIsNull.setValue(true);
            }
        }
        return peerId;
    }


    public void setPeerId(String peerId) {
        synchronized (mLockPeerId) {
            if (null != peerId) {
                mPeerId = new StringBuilder(peerId).toString();
                //mIsNull = false;
                mIsNull.setValue(false);
            } else {
                mPeerId = null;
                //mIsNull = true;
                mIsNull.setValue(true);
            }
        }
    }


    public boolean isNull() {
        //return mIsNull;
        return mIsNull.getValue();
    }


    public void savePeerIdToFile(Context context, String fileName) {
        String peerId = getPeerId();
        if (null == peerId) return;
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter out = new OutputStreamWriter(fos);
            //Log.i(TAG, "((( savePeerIdToFile, peerId=" + peerId);
            out.write(peerId, 0, peerId.length());
            String strNew = "\n";
            out.write(strNew, 0, strNew.length());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String readPeerIdFromFile(Context context, String fileName) {
        String result = null;
        try {
            InputStream in = context.openFileInput(fileName);
            if (null == in) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String strVal;
            if ((strVal = reader.readLine()) != null) {
                result = strVal;
                //Log.i(TAG, "((( restorePeerIdFromFile, peerId=" + result);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public void restorePeerIdFromFile(Context context, String fileName) {
        setPeerId(readPeerIdFromFile(context, fileName));
    }

}