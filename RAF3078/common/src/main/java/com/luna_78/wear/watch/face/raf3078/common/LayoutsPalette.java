package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.util.Log;

import com.luna_78.wear.watch.face.raf3078.common.R;

import java.io.InputStream;
import java.util.ArrayList;


public class LayoutsPalette {

    private static final String TAG = "LP";

    Context mContext;

    enum PaletteState {READY, SWAPPING, INITIAL}

    public PaletteState mPaletteState = PaletteState.INITIAL;

    public ArrayList<Layout> mPalette;

    public LayoutsPalette(Context context) {
        mContext = context;
        mPalette = new ArrayList<>();
        mPaletteState = PaletteState.READY;
    }

    public int size() {
        int result = 0;
        if (mPaletteState==PaletteState.READY) result = mPalette.size();
        return result;
    }

    public Layout get(int position) {
        Layout result = null;
        if (mPaletteState==PaletteState.READY) result = mPalette.get(position);
        return result;
    }

    public boolean addAll(ArrayList<Layout> added) {
        boolean result = false;
        if (mPaletteState==PaletteState.READY) {
            result = mPalette.addAll(added);
        }
        return result;
    }

    public boolean add(Layout element) {
        boolean result = false;
        if (mPaletteState==PaletteState.READY) {
            result = mPalette.add(element);
        }
        return result;
    }

    public Layout remove(int position) {
        Layout result = null;
        if (mPaletteState==PaletteState.READY) {
            result = mPalette.remove(position);
        }
        return result;
    }

    public void clear(Context context) {
        int count = 0;
        for (Layout element : mPalette) {
            count++;
            if (null != element.iconAmbientFileName) {
                //Log.i(TAG, "#XML clear, count=" + count + ", delete ambient icon = " + element.iconAmbientFileName);
                context.deleteFile(element.iconAmbientFileName);
            }
            if (null != element.iconDenseFileName) {
                //Log.i(TAG, "#XML clear, count=" + count + ", delete dense icon = " + element.iconDenseFileName);
                context.deleteFile(element.iconDenseFileName);
            }
        }
        //
        mPalette.clear();
    }

    public boolean isEmpty() {
        boolean result = false;
        if (mPaletteState==PaletteState.READY) result = mPalette.isEmpty();
        return result;
    }

    public Layout set(int position, Layout element) {
        Layout result = null;
        if (mPaletteState==PaletteState.READY) result = mPalette.set(position, element);
        return result;
    }

    public void setDeletionRequestMs(int position) {
        if (mPaletteState!=PaletteState.READY) return;
        Layout element = mPalette.get(position);
        element.deleteRequestMs = System.currentTimeMillis();
        mPalette.set(position, element);
    }
    public void clearDeletionRequestMs(int position) {
        if (mPaletteState!=PaletteState.READY) return;
        Layout element = mPalette.get(position);
        element.deleteRequestMs = 0;
        mPalette.set(position, element);
    }
    public long getDeletionRequestMs(int position) {
        long result = -1;
        if (mPaletteState==PaletteState.READY) {
            Layout element = mPalette.get(position);
            result =  element.deleteRequestMs;
        }
        return result;
    }

    public int dropIcons() {
        int result = 0;
        mPaletteState = PaletteState.SWAPPING;
        for (Layout element : mPalette) {
            if (element.iconAmbient != null) { element.iconAmbient = null; result++; }
            if (element.iconDense != null) { element.iconDense = null; result++; }
        }
        mPaletteState = PaletteState.READY;
        System.gc();
        return result;
    }






    public boolean loadFromXmlFile(String fileName) {
        return loadFromXmlFile(fileName, false);
    }
    public boolean loadFromXmlFile(String fileName, boolean concatenate) {
        //Log.i(TAG, "#URI file=" + fileName);

        boolean result = false, added = false;
        ArrayList<Layout> loadedFromFile = null;

        int pid = android.os.Process.myPid();
        int tid = android.os.Process.myTid();
        //Log.i(TAG, "((( loadFromXmlFile START, PID=" + pid + ", TID=" + tid + ", concatenate=" + concatenate);

        String productId = mContext.getResources().getString(R.string.product_id);
        String fname = fileName;
        if (!fileName.endsWith(productId)) fname = fileName + "." + productId;

        SerializerXML serializerXml = new SerializerXML(mContext, null);
        loadedFromFile = serializerXml.xmlFileToLayoutsPalette(fname, concatenate);

        if (loadedFromFile != null) {
            mPaletteState = PaletteState.SWAPPING;
            //mPalette.clear();
            clear(mContext);
            System.gc();
            added = mPalette.addAll(loadedFromFile);// = loadedFromFile;
            mPaletteState = PaletteState.READY;
        }

        result = (loadedFromFile != null && added == true);
        //Log.i(TAG, "((( loadFromXmlFile STOP, file=" + fname + ", result=" + result + ", concatenate=" + concatenate);
        return result;
    }
    public boolean loadFromXmlFile(InputStream fis, boolean concatenate) {
        ArrayList<Layout> loadedFromFile = null;
        boolean result = false, added = false;

        SerializerXML serializerXml = new SerializerXML(mContext, null);
        loadedFromFile = serializerXml.xmlFileToLayoutsPalette(fis, concatenate);

        if (loadedFromFile != null) {
            mPaletteState = PaletteState.SWAPPING;
            //mPalette.clear();
            clear(mContext);
            System.gc();
            added = mPalette.addAll(loadedFromFile);// = loadedFromFile;
            mPaletteState = PaletteState.READY;
        }

        result = (loadedFromFile != null && added == true);
        //Log.i(TAG, "((( loadFromXmlFile STOP, file=" + fname + ", result=" + result + ", concatenate=" + concatenate);
        return result;
    }





    public boolean saveToXmlFile(String fileName, boolean includeIcons) {
        boolean result = false;

        int pid = android.os.Process.myPid();
        int tid = android.os.Process.myTid();
        //Log.i(TAG, "((( saveToXmlFile START, PID=" + pid + ", TID=" + tid + ", includeIcons=" + includeIcons);

        String fname = fileName;
        String productId = mContext.getResources().getString(R.string.product_id);
        if (!fileName.endsWith(productId)) fname = fileName + "." + productId;

        if (mPaletteState==PaletteState.READY) {
            SerializerXML serializerXml = new SerializerXML(mContext, null);
            mPaletteState = PaletteState.SWAPPING;
            result = serializerXml.layoutsPaletteToXmlFile(mPalette, fname, includeIcons);
            System.gc();
            mPaletteState = PaletteState.READY;
        }

        //Log.i(TAG, "((( saveToXmlFile STOP, file=" + fname + ", result=" + result + ", includeIcons=" + includeIcons);
        return result;
    } // saveToXmlFile(String fileName, boolean includeIcons)
    //
    public boolean saveToXmlFile(String fileName) {
        return saveToXmlFile(fileName, false);
    }

} // class LayoutsPalette
