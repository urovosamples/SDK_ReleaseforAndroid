package com.example.dupkttest;

import android.content.Context;
import android.device.MagManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;

public class MagReadService {

    public final static int MESSAGE_OPEN_MAG = 1;
    public final static int MESSAGE_CHECK_FAILE = 2;
    public final static int MESSAGE_READ_MAG = 3;
    public final static int MESSAGE_CHECK_OK = 4;
    public final static String CARD_TRACK1 = "track1";
    public final static String CARD_NUMBER = "number";
    public final static String CARD_TRACK2 = "track2";
    public final static String CARD_TRACK3 = "track3";
    public final static String CARD_KSN = "KSN";

    private Context mContext;
    private Handler mHandler;
    private MagManager magManager;
    private MagReaderThread magReaderThread;
    private static final int DEFAULT_TAG = 1;
    private byte[] magBuffer = new byte[1024];

    public MagReadService(Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        magManager = new MagManager();
    }

    //Byte array conversion hex string
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        String hex = "";
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            // ret.append(hex.toUpperCase());
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public synchronized void start() {

        if (magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
        magReaderThread = new MagReaderThread("reader--" + DEFAULT_TAG);
        magReaderThread.start();
    }

    public synchronized void stop() {
        if (magManager != null) {
            magManager.close();
        }
        if (magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
    }

    public String bytes2HexString(byte[] data, int len) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; ++i) {
            String temp = Integer.toHexString(data[i] & 255);

            for (int t = temp.length(); t < 2; ++t) {
                sb.append("0");
            }

            sb.append(temp);
        }

        return sb.toString();
    }

    private class MagReaderThread extends Thread {
        private boolean running = true;

        private boolean isValid;

        public MagReaderThread(String name) {
            super(name);
            running = true;
        }


        public void stopMagReader() {
            running = false;
        }

        public void run() {
            if (magManager != null) {
                int ret = magManager.open();
                if (ret != 0) {
                    mHandler.sendEmptyMessage(MESSAGE_OPEN_MAG);
                    return;
                }
            }
            while (running) {
                int size = 0;
                if (magManager == null)
                    return;
                int ret = magManager.checkCard();
                if (ret != 0) {
                    mHandler.sendEmptyMessage(MESSAGE_CHECK_FAILE);
                    try {
                        Thread.sleep(600);
                    } catch (Exception e) {
                    }
                    continue;
                } else {
                    mHandler.sendEmptyMessage(MESSAGE_CHECK_OK);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
                StringBuffer trackOne = new StringBuffer();
                byte[] stripInfo = new byte[512];
                byte[] CardNo = new byte[20];
                byte[] KSN = new byte[10];
                byte[] chipTag = new byte[2];  //2 or 6 is ICC card
                int allLen = magManager.getEncryptStripInfo(1, 3, 0, stripInfo, CardNo, KSN, chipTag);
                //int allLen=0;
                Log.d("MagReadService", "getAllStripInfo = " + allLen);
                if (allLen > 0) {
                    mHandler.removeMessages(MESSAGE_CHECK_FAILE);
                    Message msg = mHandler.obtainMessage(MESSAGE_READ_MAG);
                    Bundle bundle = new Bundle();
                    bundle.putString(CARD_NUMBER, (new String(CardNo).trim()));
                    bundle.putByteArray(CARD_KSN, KSN);
                    Log.d("MagReadService", "getAllStripInfo = " + (new String(CardNo).trim()));
                    Log.d("MagReadService", "getAllStripInfo = " + DecodeConvert.bytesToHexString(stripInfo, 0, allLen));
                    Log.d("MagReadService", "chipTag = " + chipTag[0]);
                    Log.d("MagReadService", "KSN = " + bytes2HexString(KSN, 10));

                    byte[] CARD_TRACK_1;
                    byte[] CARD_TRACK_2;
                    byte[] CARD_TRACK_3;
                    bundle.putInt("CARD_TRACK1_L", allLen);
                    if (allLen != 0) {
                        CARD_TRACK_1 = new byte[allLen];
                        System.arraycopy(stripInfo, 0, CARD_TRACK_1, 0, allLen);
                        bundle.putByteArray(CARD_TRACK1, CARD_TRACK_1);
                    }
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    trackOne = null;
                }
                try {
                    Thread.sleep(800);
                } catch (Exception e) {
                }
            }
        }
    }
}
