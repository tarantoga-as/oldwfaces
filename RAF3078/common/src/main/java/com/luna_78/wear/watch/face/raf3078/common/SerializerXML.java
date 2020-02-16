package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by buba on 24/05/15.
 */
public class SerializerXML {

    private static final String TAG = "XML";

    private String productId;



//    private static void testBundle(Parcel parcel) {
//        Bundle bundle;
//
//        bundle = parcel.readBundle();
//        Set<String> stringSet = bundle.keySet();
//        stringSet.size();
//
//        for (String s: stringSet) {
//
//        }
//
//        //parcel.
//        //PersistableBundle persistableBundle = new PersistableBundle();
//        //persistableBundle.
//    }

//    public static void traverseBundle(Bundle bundle) {
//
//        // see here: http://stackoverflow.com/questions/6474734/how-do-i-know-what-data-is-given-in-a-bundle
//
//        Set<String> stringSet = bundle.keySet();
//        stringSet.size();
//
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append("[");
//        for (String key: stringSet) {
//
//            stringBuffer.append(" ").append(key).append("=>").append(bundle.get(key));
//
//            Bundle innerBundle;
//
//            if (key.equals(ACommon.KEY_LAYOUTS_PALETTE)) {
//                ArrayList<String> layoutsPaletteList = bundle.getStringArrayList(ACommon.KEY_LAYOUTS_PALETTE);
//                for (String layoutBase64: layoutsPaletteList) {
//                    innerBundle = ACommon.deserializeBundle(layoutBase64);
//                    SerializerXML.traverseBundle(innerBundle);
//                }
//            }
//
//            if (key.equals(ACommon.KEY_CFGPAL_CONFIG)) {
//                innerBundle = bundle.getBundle(ACommon.KEY_CFGPAL_CONFIG);
//                SerializerXML.traverseBundle(innerBundle);
//            }
//
//            if (key.equals(Inscription.CFG_INSCR_FULLBUNDLE)) {
//                innerBundle = bundle.getBundle(Inscription.CFG_INSCR_FULLBUNDLE);
//                SerializerXML.traverseBundle(innerBundle);
//            }
//
//        }
//        stringBuffer.append("]");
//
//        //Log.i(TAG, "((( traverseBundle, body = " + stringBuffer);
//    } // traverseBundle




    private static final String ATTR_PRODUCT_ID = "product_id";
    private static final String LAYOUTS_PALETTE = ACommon.KEY_LAYOUTS_PALETTE; //"layouts_palette"
    private static final String LAYOUT = "layout";
    private static final String LAYOUT_NAME = ACommon.KEY_CFGPAL_NAME; //"cp_name"
    private static final String DENSE_ICON = ACommon.KEY_CFGPAL_ICON; //"cp_icon";
    private static final String AMBIENT_ICON = ACommon.KEY_CFGPAL_ICON_AMBIENT; //"cp_aicon";
    private static final String DENSE_ICON_FILENAME = "dense_file";
    private static final String AMBIENT_ICON_FILENAME = "ambient_file";
    private static final String WATCH_APPEARANCE = ACommon.KEY_CFGPAL_CONFIG; //"cp_config"
    private static final String INSCRIPTION_BUNDLE = Inscription.CFG_INSCR_FULLBUNDLE; //"iscr_fullbndl"
    private static final String LONG_ITEM = "long";
    private static final String FLOAT_ITEM = "float";
    private static final String STRING_ITEM = "string";


    XmlSerializer mXmlSerializer;
    XmlPullParser mXmlPullParser;
    StringWriter mStringWriter;
    FileWriter mFileWriter;
    String mRootTag;
    Context mContext;


    public SerializerXML(Context context, String rootTag) {
        mContext = context;
        productId = context.getResources().getString(R.string.product_id);
        if (null == rootTag) mRootTag = "root";
        else mRootTag = rootTag;
    } // SerializerXML()


    public String bundleToXmlString(Bundle bundle) {
        String result = null;

        mXmlSerializer = Xml.newSerializer();
        mStringWriter = new StringWriter();

        try {
            mXmlSerializer.setOutput(mStringWriter);
            mXmlSerializer.startDocument("UTF-8", true);
            mXmlSerializer.startTag("", mRootTag);
            mXmlSerializer.attribute("", ATTR_PRODUCT_ID, productId);

            toXML(bundle, mXmlSerializer, mStringWriter);

            mXmlSerializer.endTag("", mRootTag);
            mXmlSerializer.endDocument();
            result = mStringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    } // bundleToXmlString

    public boolean bundleToXmlFile(Bundle bundle, String fileName) {
        boolean result = false;
        BufferedWriter bufferedWriter;

        mXmlSerializer = Xml.newSerializer();
        FileOutputStream fos;

        try {
//            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            try {
                fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                //Log.i(TAG, "((( bundleToXmlFile, mContext.openFileOutput=" + fileName);
            } catch (Exception e) {
                fos = new FileOutputStream(new File(fileName));
                //Log.i(TAG, "((( bundleToXmlFile, FileOutputStream=" + fileName);
            }
            mFileWriter = new FileWriter(fos.getFD());
            bufferedWriter = new BufferedWriter(mFileWriter);
            mXmlSerializer.setOutput(bufferedWriter);
            mXmlSerializer.startDocument("UTF-8", true);
            mXmlSerializer.startTag("", mRootTag);
            mXmlSerializer.attribute("", ATTR_PRODUCT_ID, productId);

            toXML(bundle, mXmlSerializer, bufferedWriter);

            mXmlSerializer.endTag("", mRootTag);
            mXmlSerializer.endDocument();

            bufferedWriter.flush();
            bufferedWriter.close();
//            mFileWriter.flush();
//            mFileWriter.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    } // bundleToXmlFile

    private void longArrayToXml(Bundle bundle, String key, String itemTag, XmlSerializer xmlSerializer) {
        for (long longValue: bundle.getLongArray(key)) {
            try {
                xmlSerializer.startTag("", itemTag);
                xmlSerializer.text(String.valueOf(longValue));
                xmlSerializer.endTag("", itemTag);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    } // longArrayToXml
    private void floatArrayToXml(Bundle bundle, String key, String itemTag, XmlSerializer xmlSerializer) {
        for (float floatValue: bundle.getFloatArray(key)) {
            try {
                xmlSerializer.startTag("", itemTag);
                xmlSerializer.text(String.valueOf(floatValue));
                xmlSerializer.endTag("", itemTag);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    } // floatArrayToXml
    private void stringArrayToXml(Bundle bundle, String key, String itemTag, XmlSerializer xmlSerializer) {
        for (String stringValue: bundle.getStringArray(key)) {
            try {
                xmlSerializer.startTag("", itemTag);
                xmlSerializer.text(stringValue);
                xmlSerializer.endTag("", itemTag);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    } // stringArrayToXml

    public void toXML(Bundle bundle, XmlSerializer xmlSerializer, Writer writer) {
        String itemLongTag = LONG_ITEM;
        String itemFloatTag = FLOAT_ITEM;
        String itemStringTag = STRING_ITEM;
        //String itemLayoutTag = "layout";

        for (String key: bundle.keySet()) {
            Object value = bundle.get(key);
            //value.
            try {

                if (key.matches("^[0-9]+$")) key = "I_C_" + key; // "only digits" keys are not allowed in XML
                xmlSerializer.startTag("", key);


                if (key.equals(Inscription.CFG_INSCR_FULLBUNDLE)) {
                    Bundle innerBundle = bundle.getBundle(Inscription.CFG_INSCR_FULLBUNDLE);
                    toXML(innerBundle, xmlSerializer, writer);
                } else if (key.equals(Inscription.CFG_INSCR_BEND)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_FX)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_TEXTSCALEX)) {
                    floatArrayToXml(bundle, key, itemFloatTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_ANGLE)) {
                    floatArrayToXml(bundle, key, itemFloatTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_BURNIN)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_INCLINE)) {
                    floatArrayToXml(bundle, key, itemFloatTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_RADIUS)) {
                    floatArrayToXml(bundle, key, itemFloatTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_TEXTCOLOR)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_DIRECTION)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_TEXT)) {
                    stringArrayToXml(bundle, key, itemStringTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_TEXTSIZE)) {
                    floatArrayToXml(bundle, key, itemFloatTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_ENABLED)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_FONTFAMILY)) {
                    stringArrayToXml(bundle, key, itemStringTag, xmlSerializer);
                } else if (key.equals(Inscription.CFG_INSCR_FONTSTYLE)) {
                    stringArrayToXml(bundle, key, itemStringTag, xmlSerializer);
                    //iscr_bend=>[J@dd38f90
                    //iscr_txtx=>[F@32323089
                    //iscr_a=>[F@3d1e988e
                    //iscr_b=>[J@245668af
                    //iscr_i=>[F@330a14bc
                    //iscr_r=>[F@13044c45
                    //iscr_clr=>[J@2a6faf9a
                    //iscr_dir=>[J@42ae0cb
                    //iscr_txt=>[Ljava.lang.String;@3ec1c0a8
                    //iscr_txtsz=>[F@2b2267c1
                    //iscr_en=>[J@19892b66
                    //iscr_ff=>[Ljava.lang.String;@2184bea7
                    //iscr_fs=>[Ljava.lang.String;@13c63f54
                    //iscr_li=>2]

                } else if (key.equals(ACommon.CFG_HOUR_MARKS)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);

                } else if (key.equals(ACommon.CFG_HOUR_MARKS_RELIEF)) {
                    longArrayToXml(bundle, key, itemLongTag, xmlSerializer);

                } else if (key.equals(LAYOUTS_PALETTE)) {
                    ArrayList<String> layoutsPaletteList = bundle.getStringArrayList(LAYOUTS_PALETTE);
                    //xmlSerializer.startTag("", "layouts");
                    for (String base64Layout : layoutsPaletteList) {
                        xmlSerializer.startTag("", LAYOUT);
                        Bundle innerBundle = ACommon.deserializeBundle(base64Layout);
                        toXML(innerBundle, xmlSerializer, writer);
                        xmlSerializer.endTag("", LAYOUT);
                    }
                    //xmlSerializer.endTag("", "layouts");

                } else if (key.equals(WATCH_APPEARANCE)) {
                    Bundle innerBundle = bundle.getBundle(key);
                    toXML(innerBundle, xmlSerializer, writer);

                } else if (key.equals(AMBIENT_ICON) || key.equals(DENSE_ICON)) {
//                //<cp_icon>[B@42931910</cp_icon>
//                baos = new ByteArrayOutputStream(bmpSize);
//                iconAmbient.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                byteArray = baos.toByteArray();
//                configPaletteElement.putByteArray(ACommon.KEY_CFGPAL_ICON_AMBIENT, byteArray);
//
//                if (!ambient) byteArray = configPaletteElement.getByteArray(ACommon.KEY_CFGPAL_ICON);
//                else byteArray = configPaletteElement.getByteArray(ACommon.KEY_CFGPAL_ICON_AMBIENT);
//                if (null != byteArray) {
//                    ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
//                    bmp = BitmapFactory.decodeStream(bais);
//                    //mService.setLastFrameScreenshot(bmp);
//                }
//
//                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
//                zos.write(parcel.marshall());
//                zos.close();
//                base64 = Base64.encodeToString(bos.toByteArray(), 0);
                    byte byteArray[] = bundle.getByteArray(key);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
                    zos.write(byteArray);
                    zos.close();
                    xmlSerializer.text(Base64.encodeToString(bos.toByteArray(), 0));

                } else {
                    xmlSerializer.text(value.toString());
                }








                xmlSerializer.endTag("", key);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // toXML Bundle


    private void bmpToXML(Bitmap bmp, XmlSerializer xmlSerializer, Writer writer) throws Exception {
        int bmpSize = bmp.getByteCount();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bmpSize);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
        zos.write(byteArray);
        zos.close();
        String base64 = Base64.encodeToString(bos.toByteArray(), 0);
        xmlSerializer.text(base64);
    } // bmpToXML

    private boolean bmpToFile(Bitmap bmp, String fileName) {
        boolean result = false;
        int bmpSize = bmp.getByteCount();
        File iconSoloFile = mContext.getFileStreamPath(fileName);
        long fileSize;
        if (iconSoloFile.exists()) {
            fileSize = iconSoloFile.length();
            //Log.i(TAG, "#XML, bmpToFile, file=" + iconSoloFile.getAbsolutePath() + " exist, size=" + fileSize);
            if (fileSize > 0L) {
                return true;
            }
        }

        FileOutputStream fos;
        try {
            try {
                fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName);
            } catch (FileNotFoundException e) {
                fos = new FileOutputStream(new File(fileName));
                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName);
            }
            //FileDescriptor fd = fos.getFD();
            //FileChannel fc = fos.getChannel();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bmpSize);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            fos.write(byteArray);
            fos.flush();
            fos.close();
            result = true;
        } catch (Exception e) {
            //e.printStackTrace();
            result = false;
        }

        //Log.i(TAG, "#XML, bmpToFile, file=" + fileName + ", written=" + result);
        return result;
    } // bmpToFile

    static public Bitmap bmpFromFile(Context context, String fileName) {
        Bitmap result = null;

        File iconSoloFile = context.getFileStreamPath(fileName);
        long fileSize;
        if (iconSoloFile.exists()) {
            fileSize = iconSoloFile.length();
            if (fileSize > 0L) {
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = 1;
                try {
                    FileInputStream stream2 = new FileInputStream(iconSoloFile);
                    Bitmap bitmap= BitmapFactory.decodeStream(stream2, null, o2);
                    stream2.close();
                    result = bitmap;
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            //Log.i(TAG, "#XML, bmpFromFile, file=" + fileName + " exist, size = " + fileSize + ", result=" + result);
        }

        return result;
    } // bmpFromFile

    public void toXML(ArrayList<Layout> palette, XmlSerializer xmlSerializer, boolean includeIcons, Writer writer) {
        long timeStampMs = System.currentTimeMillis();
        String denseFileName, ambientFileName;

        try {
            xmlSerializer.startTag("", LAYOUTS_PALETTE);
            for (Layout layoutElement: palette) {

                timeStampMs++;
                ambientFileName = (null == layoutElement.iconAmbientFileName) ? String.valueOf(timeStampMs) + "_A" : layoutElement.iconAmbientFileName;
                denseFileName = (null == layoutElement.iconDenseFileName) ? String.valueOf(timeStampMs) + "_D" : layoutElement.iconDenseFileName;

                xmlSerializer.startTag("", LAYOUT);

                if (layoutElement.iconDense != null) { //#XML2PAL
//                    xmlSerializer.startTag("", DENSE_ICON);
//                    bmpToXML(layoutElement.iconDense, xmlSerializer, writer);
//                    xmlSerializer.endTag("", DENSE_ICON);
                    //
                    bmpToFile(layoutElement.iconDense, denseFileName);
                    layoutElement.iconDense = null;
                }

                if (layoutElement.iconAmbient != null) {
//                    xmlSerializer.startTag("", AMBIENT_ICON);
//                    bmpToXML(layoutElement.iconAmbient, xmlSerializer, writer);
//                    xmlSerializer.endTag("", AMBIENT_ICON);
                    //
                    bmpToFile(layoutElement.iconAmbient, ambientFileName);
                    layoutElement.iconAmbient = null;
                }

                xmlSerializer.startTag("", DENSE_ICON_FILENAME);
                xmlSerializer.text(denseFileName);
                xmlSerializer.endTag("", DENSE_ICON_FILENAME);

                xmlSerializer.startTag("", AMBIENT_ICON_FILENAME);
                xmlSerializer.text(ambientFileName);
                xmlSerializer.endTag("", AMBIENT_ICON_FILENAME);

                //Log.i(TAG, "#XML, toXML, iconA=" + layoutElement.iconAmbient + ", fname=" + ambientFileName);
                //Log.i(TAG, "#XML, toXML, iconD=" + layoutElement.iconDense + ", fname=" + denseFileName);

                if (includeIcons) {
                    long fileSize;
                    File iconSoloFile;

                    iconSoloFile = mContext.getFileStreamPath(denseFileName);
                    if (iconSoloFile.exists()) {
                        fileSize = iconSoloFile.length();
                        if (fileSize > 0L) {
                            Bitmap icon = bmpFromFile(mContext, denseFileName);
                            xmlSerializer.startTag("", DENSE_ICON);
                            bmpToXML(icon, xmlSerializer, writer);
                            xmlSerializer.endTag("", DENSE_ICON);
                        }
                    }

                    iconSoloFile = mContext.getFileStreamPath(ambientFileName);
                    if (iconSoloFile.exists()) {
                        fileSize = iconSoloFile.length();
                        if (fileSize > 0L) {
                            Bitmap icon = bmpFromFile(mContext, ambientFileName);
                            xmlSerializer.startTag("", AMBIENT_ICON);
                            bmpToXML(icon, xmlSerializer, writer);
                            xmlSerializer.endTag("", AMBIENT_ICON);
                        }
                    }
                }

                xmlSerializer.startTag("", LAYOUT_NAME);
                xmlSerializer.text(layoutElement.name);
                xmlSerializer.endTag("", LAYOUT_NAME);

                xmlSerializer.startTag("", WATCH_APPEARANCE);
                toXML(layoutElement.config, xmlSerializer, writer);
                xmlSerializer.endTag("", WATCH_APPEARANCE);

                xmlSerializer.endTag("", LAYOUT);
            } // for
            xmlSerializer.endTag("", LAYOUTS_PALETTE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // toXML ArrayList<Layout>

    public boolean layoutsPaletteToXmlFile(ArrayList<Layout> palette, String fileName) {
        return layoutsPaletteToXmlFile(palette, fileName, false);
    }
    //
    public boolean layoutsPaletteToXmlFile(ArrayList<Layout> palette, String fileName, boolean includeIcons) {
        boolean result = false;
        BufferedWriter bufferedWriter;

        mXmlSerializer = Xml.newSerializer();
        FileOutputStream fos;

        try {
//            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            try {
                fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName + ", includeIcons=" + includeIcons);
            } catch (Exception e) {
                fos = new FileOutputStream(new File(fileName));
                //Log.i(TAG, "((( layoutsPaletteToXmlFile START, file=" + fileName + ", includeIcons=" + includeIcons);
            }
            mFileWriter = new FileWriter(fos.getFD());
            bufferedWriter = new BufferedWriter(mFileWriter);
            mXmlSerializer.setOutput(bufferedWriter);
            mXmlSerializer.startDocument("UTF-8", true);
            mXmlSerializer.startTag("", mRootTag);
            mXmlSerializer.attribute("", ATTR_PRODUCT_ID, productId);

            toXML(palette, mXmlSerializer, includeIcons, bufferedWriter);

            mXmlSerializer.endTag("", mRootTag);
            mXmlSerializer.endDocument();

            bufferedWriter.flush();
            bufferedWriter.close();
            result = true;
        } catch (Exception e) {
            //e.printStackTrace();
            result = false;
        }

        //Log.i(TAG, "((( layoutsPaletteToXmlFile STOP, file=" + fileName + ", result=" + result + ", includeIcons=" + includeIcons);
        return result;
    } // layoutsPaletteToXmlFile












    public void toXML(Layout layoutElement, XmlSerializer xmlSerializer, Writer writer) {
        long timeStampMs = System.currentTimeMillis();
        String denseFileName, ambientFileName;

        try {
            xmlSerializer.startTag("", LAYOUTS_PALETTE);

//            for (Layout layoutElement: palette) {
//            } // for

            xmlSerializer.startTag("", LAYOUT);

            xmlSerializer.startTag("", DENSE_ICON_FILENAME);
            xmlSerializer.text(layoutElement.iconDenseFileName);
            xmlSerializer.endTag("", DENSE_ICON_FILENAME);

            xmlSerializer.startTag("", AMBIENT_ICON_FILENAME);
            xmlSerializer.text(layoutElement.iconAmbientFileName);
            xmlSerializer.endTag("", AMBIENT_ICON_FILENAME);

            xmlSerializer.startTag("", DENSE_ICON);
            bmpToXML(layoutElement.iconDense, xmlSerializer, writer);
            xmlSerializer.endTag("", DENSE_ICON);

            xmlSerializer.startTag("", AMBIENT_ICON);
            bmpToXML(layoutElement.iconAmbient, xmlSerializer, writer);
            xmlSerializer.endTag("", AMBIENT_ICON);



//            timeStampMs++;
//            ambientFileName = (null == layoutElement.iconAmbientFileName) ? String.valueOf(timeStampMs) + "_A" : layoutElement.iconAmbientFileName;
//            denseFileName = (null == layoutElement.iconDenseFileName) ? String.valueOf(timeStampMs) + "_D" : layoutElement.iconDenseFileName;

//            if (layoutElement.iconDense != null) {
//                bmpToFile(layoutElement.iconDense, denseFileName);
//                layoutElement.iconDense = null;
//            }
//
//            if (layoutElement.iconAmbient != null) {
//                bmpToFile(layoutElement.iconAmbient, ambientFileName);
//                layoutElement.iconAmbient = null;
//            }
//
//            if (includeIcons) {
//                long fileSize;
//                File iconSoloFile;
//
//                iconSoloFile = mContext.getFileStreamPath(denseFileName);
//                if (iconSoloFile.exists()) {
//                    fileSize = iconSoloFile.length();
//                    if (fileSize > 0L) {
//                        Bitmap icon = bmpFromFile(mContext, denseFileName);
//                        xmlSerializer.startTag("", DENSE_ICON);
//                        bmpToXML(icon, xmlSerializer, writer);
//                        xmlSerializer.endTag("", DENSE_ICON);
//                    }
//                }
//
//                iconSoloFile = mContext.getFileStreamPath(ambientFileName);
//                if (iconSoloFile.exists()) {
//                    fileSize = iconSoloFile.length();
//                    if (fileSize > 0L) {
//                        Bitmap icon = bmpFromFile(mContext, ambientFileName);
//                        xmlSerializer.startTag("", AMBIENT_ICON);
//                        bmpToXML(icon, xmlSerializer, writer);
//                        xmlSerializer.endTag("", AMBIENT_ICON);
//                    }
//                }
//            }

            xmlSerializer.startTag("", LAYOUT_NAME);
            xmlSerializer.text(layoutElement.name);
            xmlSerializer.endTag("", LAYOUT_NAME);

            xmlSerializer.startTag("", WATCH_APPEARANCE);
            toXML(layoutElement.config, xmlSerializer, writer);
            xmlSerializer.endTag("", WATCH_APPEARANCE);

            xmlSerializer.endTag("", LAYOUT);

            xmlSerializer.endTag("", LAYOUTS_PALETTE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // toXML Layout


    public boolean layoutToXmlFile(Layout layout, String fileName) {
        boolean result = false;
        BufferedWriter bufferedWriter;

        mXmlSerializer = Xml.newSerializer();
        FileOutputStream fos;

        try {
//            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            try {
                fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                //Log.i(TAG, "((( layoutToXmlFile START, file=" + fileName);
            } catch (Exception e) {
                fos = new FileOutputStream(new File(fileName));
                //Log.i(TAG, "((( layoutToXmlFile START, file=" + fileName);
            }
            mFileWriter = new FileWriter(fos.getFD());
            bufferedWriter = new BufferedWriter(mFileWriter);
            mXmlSerializer.setOutput(bufferedWriter);
            mXmlSerializer.startDocument("UTF-8", true);
            mXmlSerializer.startTag("", mRootTag);
            mXmlSerializer.attribute("", ATTR_PRODUCT_ID, productId);

            toXML(layout, mXmlSerializer, bufferedWriter);

            mXmlSerializer.endTag("", mRootTag);
            mXmlSerializer.endDocument();

            bufferedWriter.flush();
            bufferedWriter.close();
            result = true;
        } catch (Exception e) {
            //e.printStackTrace();
            result = false;
        }

        //Log.i(TAG, "((( layoutToXmlFile STOP, file=" + fileName + ", result=" + result);
        return result;
    } // layoutToXmlFile









    private int fromXml(long[] array, String value, int index) {
        array[index] = Long.parseLong(value);
        return index + 1;
    } // fromXml long[]
    //
    private int fromXml(float[] array, String value, int index) {
        //String stringVal = String.format(Locale.US, "%.2f", value);
        array[index] = Float.parseFloat(value);
        return index + 1;
    } // fromXml float[]
    //
    private int fromXml(String[] array, String value, int index) {
        array[index] = value;
        return index + 1;
    } // fromXml String[]
    //
    private void fromXML(Bundle bundle, String key, String value) {

        if (key.matches("^I_C_[0-9]+$")) {
            String colorIndex = key.substring(4);
            int i = Integer.parseInt(value);
            bundle.putInt(colorIndex, i);
            return;
        }

//        Object obj = bundle.get(key);
//        Class c = obj.getClass();

        switch (key) {
            case ACommon.CFG_DG_EDGE_ALPHA:
            case ACommon.CFG_DG_EDGE_ALPHA_1:
            case ACommon.BCAST_EXTRA_EVENT_TYPE:
            case ACommon.CFG_DOM_INDEX:
            case ACommon.CFG_AUX_HANDS_INDEX:
            case ACommon.CFG_LAYOUT_INDEX:
            case ACommon.KEY_EVENT:
            case ACommon.CFG_BACKGROUND_INDEX:
            case ACommon.CFG_AUX_BEVEL_COLOR:
            case ACommon.CFG_MAIN_HANDS_INDEX:
            case ACommon.CFG_INVERT_GRADIENT:
            case Inscription.CFG_INSCR_LAYOUTINDEX:
            case ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH:
            case ACommon.CFG_PLATE_RELIEF_STRENGTH:
            case ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH:
                int i = Integer.parseInt(value);
                bundle.putInt(key, i);
                break;

            case ACommon.CFG_SHOW_RIM_ANIMATION:
            case ACommon.CFG_SHOW_HANDHELD_BATTERY:
            case ACommon.CFG_SHOW_DIAL_GRADIENT:
            case ACommon.CFG_SHOW_HRDIGITS_RELIEF:
            case ACommon.CFG_COLORIZE_BURNIN_MARGIN:
            case ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF:
                boolean b = Boolean.parseBoolean(value);
                bundle.putBoolean(key, b);
                break;

            case ACommon.CFG_DG_FIRST_STOP:
            case ACommon.CFG_DG_HALF_EDGE_STOP_1:
            case ACommon.CFG_DG_HALF_EDGE_STOP:
            case ACommon.CFG_DG_FIRST_STOP_1:
            case ACommon.CFG_PLATE_TEXTURE_STRENGTH:
            case ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH:
                //String stringVal = String.format(Locale.US, "%.2f", value);
                float f = Float.parseFloat(value);
                bundle.putFloat(key, f);
                break;

            case ACommon.KEY_TIME:
            case ACommon.CFG_TIME:
            case ACommon.BCAST_EXTRA_BATTERY_TIME:
                long l = Long.parseLong(value);
                bundle.putLong(key, l);
                break;

            default:
                break;
        }

    } // fromXML Bundle


    public Bundle xmlFileToBundle(String fileName) {
        Bundle result = null, bundle = null;
        ArrayList<String> layoutsPalette = null; //new ArrayList<>(); //null;
        Stack<Bundle> bundleStack = new Stack<>();
        long arrayLong[] = null; //new long[Inscription.NUM_INSCRIPTIONS]; //null;
        float arrayFloat[] = null; //new float[Inscription.NUM_INSCRIPTIONS]; //null;
        String arrayString[] = null; //new String[Inscription.NUM_INSCRIPTIONS]; //null;
        int arrayIndex = 0;
        boolean needGC = false;

        mXmlPullParser = Xml.newPullParser();

        try {
            FileInputStream fis;
            try {
                fis = mContext.openFileInput(fileName);
            } catch (IllegalArgumentException e) {
                fis = new FileInputStream(new File(fileName));
            }

            mXmlPullParser.setInput(fis, null);
            int eventType = mXmlPullParser.getEventType();
            //Message currentMessage = null;
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {

                String name = null;

                if(needGC) { System.gc(); needGC = false; }

                switch (eventType) {

                    case XmlPullParser.START_DOCUMENT:
                        bundle = new Bundle();
                        break;

                    case XmlPullParser.START_TAG:
                        name = mXmlPullParser.getName();

                        if (name.equalsIgnoreCase(mRootTag)) {
                            String prodid = mXmlPullParser.getAttributeValue(null, ATTR_PRODUCT_ID);
                            if (null == prodid || !prodid.equalsIgnoreCase(productId)) {
                                done = true;
                                //break;
                            }
                        } else if (name.equalsIgnoreCase(LAYOUTS_PALETTE)) {
                            layoutsPalette = new ArrayList<>();
                            //layoutsPalette.clear();
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LAYOUT)) {
                            bundleStack.push(bundle);
                            bundle = new Bundle();

                        } else if (name.equalsIgnoreCase(LAYOUT_NAME)) {
                            String lp_name = mXmlPullParser.nextText();
                            bundle.putString(name, lp_name);

                        } else if (name.equalsIgnoreCase(DENSE_ICON) || name.equalsIgnoreCase(AMBIENT_ICON)) {
                            String base64gzip = mXmlPullParser.nextText();
                            GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64gzip, 0)));
                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int len; while ((len = zis.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
                            zis.close();
                            bundle.putByteArray(name, byteBuffer.toByteArray());
                            needGC = true;

                        } else if (name.equalsIgnoreCase(WATCH_APPEARANCE)) {
                            bundleStack.push(bundle);
                            bundle = new Bundle();

                        } else if (name.equalsIgnoreCase(INSCRIPTION_BUNDLE)) {
                            bundleStack.push(bundle);
                            bundle = new Bundle();

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_DIRECTION) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BEND) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BURNIN) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_ENABLED) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTCOLOR) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FX)) {
                            arrayIndex = 0;
                            arrayLong = new long[Inscription.NUM_INSCRIPTIONS];
                            for (int i = 0; i < Inscription.NUM_INSCRIPTIONS; i++) arrayLong[i] = 0;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS)) {
                            arrayIndex = 0;
                            arrayLong = new long[WatchAppearance.NUM_HOUR_MARKS];
                            for (int i = 0; i < WatchAppearance.NUM_HOUR_MARKS; i++) arrayLong[i] = WatchAppearance.DEFAULT_HOUR_MARK; //0;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS_RELIEF)) {
                            arrayIndex = 0;
                            arrayLong = new long[WatchAppearance.NUM_HOUR_MARKS];
                            for (int i = 0; i < WatchAppearance.NUM_HOUR_MARKS; i++) arrayLong[i] = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF;
                            needGC = true;


                        } else if (name.equalsIgnoreCase(LONG_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayLong, value, arrayIndex);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_ANGLE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSIZE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSCALEX) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_RADIUS) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_INCLINE)) {
                            arrayIndex = 0;
                            arrayFloat = new float[Inscription.NUM_INSCRIPTIONS];
                            for(int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) arrayFloat[i] = 0.0f;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(FLOAT_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayFloat, value, arrayIndex);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTFAMILY) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXT) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTSTYLE)) {
                            arrayIndex = 0;
                            arrayString = new String[Inscription.NUM_INSCRIPTIONS];
                            for(int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) arrayString[i] = "";
                            needGC = true;

                        } else if (name.equalsIgnoreCase(STRING_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayString, value, arrayIndex);


                        } else {
                            String value = mXmlPullParser.nextText();
                            fromXML(bundle, name, value);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        name = mXmlPullParser.getName();

                        if (name.equalsIgnoreCase(mRootTag)) {
                            done = true;
                            result = bundle;
                        } else if (name.equalsIgnoreCase(LAYOUTS_PALETTE)) {
                            bundle.putStringArrayList(LAYOUTS_PALETTE, layoutsPalette);
                            //layoutsPalette.clear();
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LAYOUT)) {
                            String base64 = ACommon.serializeBundle(bundle);
                            layoutsPalette.add(base64);
                            bundle = bundleStack.pop();
                            needGC = true;

                        } else if (name.equalsIgnoreCase(WATCH_APPEARANCE)) {
                            Bundle poped = bundleStack.pop();
                            poped.putBundle(WATCH_APPEARANCE, bundle);
                            bundle = poped;

                        } else if (name.equalsIgnoreCase(INSCRIPTION_BUNDLE)) {
                            Bundle poped = bundleStack.pop();
                            poped.putBundle(INSCRIPTION_BUNDLE, bundle);
                            bundle = poped;


                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_DIRECTION) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BEND) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BURNIN) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_ENABLED) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTCOLOR) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FX)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS_RELIEF)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_ANGLE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSIZE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSCALEX) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_RADIUS) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_INCLINE)) {
                            bundle.putFloatArray(name, arrayFloat);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTFAMILY) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXT) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTSTYLE)) {
                            bundle.putStringArray(name, arrayString);


                        }
                        break;

                } // switch
                eventType = mXmlPullParser.next();
            } // while

        } catch (Exception e) {
            result = null;
        } finally {
            System.gc();
        }

        return result;
    } // xmlFileToBundle



    public ArrayList<Layout> xmlFileToLayoutsPalette(String fileName) {
        return xmlFileToLayoutsPalette(fileName, false);
    }
    public ArrayList<Layout> xmlFileToLayoutsPalette(String fileName, boolean concatenate) {
        //Log.i(TAG, "#URI_0 xmlFileToLayoutsPalette file=" + fileName + ", concatenate=" + concatenate);

        InputStream fis;
        try {
            try {
                fis = mContext.openFileInput(fileName);
                //Log.i(TAG, "#URI_1 xmlFileToLayoutsPalette START, file=" + fileName + ", concatenate=" + concatenate);
            } catch (IllegalArgumentException e) {
                fis = new FileInputStream(new File(fileName));
                //Log.i(TAG, "#URI_2 xmlFileToLayoutsPalette START, file=" + fileName + ", concatenate=" + concatenate);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return xmlFileToLayoutsPalette(fis, concatenate);
    }
    public ArrayList<Layout> xmlFileToLayoutsPalette(InputStream fis, boolean concatenate) {
        //Log.i(TAG, "#URI_! xmlFileToLayoutsPalette stream=" + fis + ", concatenate=" + concatenate);

        ArrayList<Layout> result = null;

        Bundle bundle = null;
        ArrayList<Layout> layoutsPalette = null;
        Layout layoutElement = null;
        Stack<Bundle> bundleStack = new Stack<>();
        long arrayLong[] = null;
        float arrayFloat[] = null;
        String arrayString[] = null;
        int arrayIndex = 0;
        boolean needGC = false;

        mXmlPullParser = Xml.newPullParser();

        try {
            //FileInputStream fis;

            mXmlPullParser.setInput(fis, null);
            int eventType = mXmlPullParser.getEventType();
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {

                String name = null;
                Bitmap denseIcon = null, ambientIcon = null;

                if(needGC) { System.gc(); needGC = false; }

                switch (eventType) {

                    case XmlPullParser.START_DOCUMENT:
//                        bundle = new Bundle();
                        break;

                    case XmlPullParser.START_TAG:

                        name = mXmlPullParser.getName();

                        if (name.equalsIgnoreCase(mRootTag)) {
                            String prodid = mXmlPullParser.getAttributeValue(null, ATTR_PRODUCT_ID);
                            if (null == prodid || !prodid.equalsIgnoreCase(productId)) {
                                done = true;
                            }

                        } else if (name.equalsIgnoreCase(LAYOUTS_PALETTE)) {
                            layoutsPalette = new ArrayList<>();
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LAYOUT)) {
//                            bundleStack.push(bundle);
//                            bundle = new Bundle();
                            layoutElement = new Layout();
                            layoutElement.iconAmbientFileName = null;
                            layoutElement.iconDenseFileName = null;
                            denseIcon = null;
                            ambientIcon = null;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LAYOUT_NAME)) {
                            String lp_name = mXmlPullParser.nextText();
//                            bundle.putString(name, lp_name);
                            layoutElement.name = lp_name;

//                        } else if (name.equalsIgnoreCase(DENSE_ICON) || name.equalsIgnoreCase(AMBIENT_ICON)) {
//                            String base64gzip = mXmlPullParser.nextText();
//                            GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64gzip, 0)));
//                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//                            byte[] buffer = new byte[1024];
//                            int len; while ((len = zis.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
//                            zis.close();
////                            bundle.putByteArray(name, byteBuffer.toByteArray());
//                            ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer.toByteArray());
//                            Bitmap bmp = BitmapFactory.decodeStream(bais);
//                            Bitmap sb = Bitmap.createScaledBitmap(bmp, ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION,
//                                    ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION, true);
//                            if (name.equalsIgnoreCase(DENSE_ICON)) {
//                                layoutElement.iconDense = sb;
//                            } else if (name.equalsIgnoreCase(AMBIENT_ICON)) {
//                                layoutElement.iconAmbient = sb;
//                            }
//                            needGC = true;
                        } else if (name.equalsIgnoreCase(DENSE_ICON) || name.equalsIgnoreCase(AMBIENT_ICON)) {
                            String base64gzip = mXmlPullParser.nextText();
                            GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64gzip, 0)));
                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int len; while ((len = zis.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
                            zis.close();
//                            bundle.putByteArray(name, byteBuffer.toByteArray());
                            ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer.toByteArray());
                            Bitmap bmp = BitmapFactory.decodeStream(bais);
                            Bitmap sb = Bitmap.createScaledBitmap(bmp, ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION,
                                    ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION, true);
                            if (name.equalsIgnoreCase(DENSE_ICON)) {
                                denseIcon = sb;
                                //Log.i(TAG, "#XML, xmlFileToLayoutsPalette, ICON, iconD=" + denseIcon + ", fname=" + layoutElement.iconDenseFileName);
                                if (null != layoutElement.iconDenseFileName) {
                                    bmpToFile(denseIcon, layoutElement.iconDenseFileName);
                                    denseIcon = null;
                                }
                            } else if (name.equalsIgnoreCase(AMBIENT_ICON)) {
                                ambientIcon = sb;
                                //Log.i(TAG, "#XML, xmlFileToLayoutsPalette, ICON, iconA=" + ambientIcon + ", fname=" + layoutElement.iconAmbientFileName);
                                if (null != layoutElement.iconAmbientFileName) {
                                    bmpToFile(ambientIcon, layoutElement.iconAmbientFileName);
                                    ambientIcon = null;
                                }
                            }
                            needGC = true;

                        } else if (name.equalsIgnoreCase(DENSE_ICON_FILENAME) || name.equalsIgnoreCase(AMBIENT_ICON_FILENAME)) {
                            String iconFileName = mXmlPullParser.nextText();
                            if (name.equalsIgnoreCase(DENSE_ICON_FILENAME)) {
                                if (!concatenate) layoutElement.iconDenseFileName = iconFileName;
                                else layoutElement.iconDenseFileName = String.valueOf(System.currentTimeMillis()) + "_D";
                                //Log.i(TAG, "#XML, xmlFileToLayoutsPalette, FNAME, iconD=" + denseIcon + ", fname=" + layoutElement.iconDenseFileName);
                                if (null != denseIcon) {
                                    bmpToFile(denseIcon, layoutElement.iconDenseFileName);
                                    denseIcon = null;
                                }
                            } else if (name.equalsIgnoreCase(AMBIENT_ICON_FILENAME)) {
                                if (!concatenate) layoutElement.iconAmbientFileName = iconFileName;
                                else layoutElement.iconAmbientFileName = String.valueOf(System.currentTimeMillis()) + "_A";
                                //Log.i(TAG, "#XML, xmlFileToLayoutsPalette, FNAME, iconA=" + ambientIcon + ", fname=" + layoutElement.iconAmbientFileName);
                                if (null != ambientIcon) {
                                    bmpToFile(ambientIcon, layoutElement.iconAmbientFileName);
                                    ambientIcon = null;
                                }
                            }
                            needGC = true;

                        } else if (name.equalsIgnoreCase(WATCH_APPEARANCE)) {
//                            bundleStack.push(bundle);
                            bundle = new Bundle();

                        } else if (name.equalsIgnoreCase(INSCRIPTION_BUNDLE)) {
                            bundleStack.push(bundle);
                            bundle = new Bundle();

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_DIRECTION) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BEND) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BURNIN) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_ENABLED) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTCOLOR) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FX)) {
                            arrayIndex = 0;
                            arrayLong = new long[Inscription.NUM_INSCRIPTIONS];
                            for (int i = 0; i < Inscription.NUM_INSCRIPTIONS; i++) arrayLong[i] = 0;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS)) {
                            arrayIndex = 0;
                            arrayLong = new long[WatchAppearance.NUM_HOUR_MARKS];
                            for (int i = 0; i < WatchAppearance.NUM_HOUR_MARKS; i++) arrayLong[i] = WatchAppearance.DEFAULT_HOUR_MARK; //0;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS_RELIEF)) {
                            arrayIndex = 0;
                            arrayLong = new long[WatchAppearance.NUM_HOUR_MARKS];
                            for (int i = 0; i < WatchAppearance.NUM_HOUR_MARKS; i++) arrayLong[i] = WatchAppearance.DEFAULT_HOUR_MARK_RELIEF;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LONG_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayLong, value, arrayIndex);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_ANGLE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSIZE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSCALEX) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_RADIUS) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_INCLINE)) {
                            arrayIndex = 0;
                            arrayFloat = new float[Inscription.NUM_INSCRIPTIONS];
                            for(int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) arrayFloat[i] = 0.0f;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(FLOAT_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayFloat, value, arrayIndex);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTFAMILY) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXT) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTSTYLE)) {
                            arrayIndex = 0;
                            arrayString = new String[Inscription.NUM_INSCRIPTIONS];
                            for(int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) arrayString[i] = "";
                            needGC = true;

                        } else if (name.equalsIgnoreCase(STRING_ITEM)) {
                            String value = mXmlPullParser.nextText();
                            arrayIndex = fromXml(arrayString, value, arrayIndex);


                        } else {
                            String value = mXmlPullParser.nextText();
                            fromXML(bundle, name, value);
                        }
                        break;


                    case XmlPullParser.END_TAG:

                        name = mXmlPullParser.getName();

                        if (name.equalsIgnoreCase(mRootTag)) {
                            done = true;

                        } else if (name.equalsIgnoreCase(LAYOUTS_PALETTE)) {
//                            bundle.putStringArrayList(LAYOUTS_PALETTE, layoutsPalette);
                            result = layoutsPalette;
                            needGC = true;

                        } else if (name.equalsIgnoreCase(LAYOUT)) {
//                            String base64 = ACommon.serializeBundle(bundle);
//                            layoutsPalette.add(base64);
//                            bundle = bundleStack.pop();
                            layoutsPalette.add(layoutElement);
                            needGC = true;

                        } else if (name.equalsIgnoreCase(WATCH_APPEARANCE)) {
//                            Bundle poped = bundleStack.pop();
//                            poped.putBundle(WATCH_APPEARANCE, bundle);
//                            bundle = poped;
                            layoutElement.config = bundle;
                            bundle = null;

                        } else if (name.equalsIgnoreCase(INSCRIPTION_BUNDLE)) {
                            Bundle popped = bundleStack.pop();
                            popped.putBundle(INSCRIPTION_BUNDLE, bundle);
                            bundle = popped;


                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_DIRECTION) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BEND) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_BURNIN) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_ENABLED) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTCOLOR) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FX)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(ACommon.CFG_HOUR_MARKS_RELIEF)) {
                            bundle.putLongArray(name, arrayLong);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_ANGLE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSIZE) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXTSCALEX) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_RADIUS) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_INCLINE)) {
                            bundle.putFloatArray(name, arrayFloat);

                        } else if (name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTFAMILY) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_TEXT) ||
                                name.equalsIgnoreCase(Inscription.CFG_INSCR_FONTSTYLE)) {
                            bundle.putStringArray(name, arrayString);


                        }
                        break;

                } // switch
                eventType = mXmlPullParser.next();
            } // while

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            System.gc();
        }

//        Log.i(TAG, "((( xmlFileToLayoutsPalette STOP, file=" + fileName +
//                ", elements count=" + ((null==result) ? 0 : result.size()) + ", concatenate=" + concatenate);
        return result;
    } // xmlFileToLayoutsPalette

} // class SerializerXML
