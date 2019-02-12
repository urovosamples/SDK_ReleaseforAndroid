package com.example.magmanager;

import android.content.Context;
import android.device.MagManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MagReadService {
    
    public final static int MESSAGE_OPEN_MAG = 1;
    public final static int MESSAGE_CHECK_FAILE = 2;
    public final static int MESSAGE_READ_MAG = 3;
    public final static int MESSAGE_CHECK_OK = 4;
    public final static String CARD_TRACK1 = "track1";
    public final static String CARD_NUMBER = "number";
    public final static String CARD_TRACK2 = "track2";
    public final static String CARD_TRACK3 = "track3";
    public final static String CARD_VALIDTIME = "validtime";
    
    private Context mContext;
    private Handler mHandler;
    private MagManager magManager;
    private MagReaderThread magReaderThread;
    private static final int DEFAULT_TAG =1;
    private byte[] magBuffer = new byte[1024];
    
    public MagReadService(Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        magManager = new MagManager();
        
    }
    
    // 从字节数组到十六进制字符串转换
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
        
        if(magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
        magReaderThread = new MagReaderThread("reader--" + DEFAULT_TAG);
        magReaderThread.start();
    }
    
    public synchronized void stop() {
        if(magManager != null) {
            magManager.close();
            //magManager = null;
        }
        if(magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
        
        
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
                byte[] stripInfo = new byte[1024];
                int allLen = magManager.getAllStripInfo(stripInfo);
                if (allLen > 0) {
                    int len = stripInfo[1];
                    if (len != 0)
                        trackOne.append(" track1: " + new String(stripInfo, 2, len));
                    int len2 = stripInfo[3 + len];
                    if (len2 != 0)
                        trackOne.append(" \ntrack2: " + new String(stripInfo, 4 + len, len2));
                    int len3 = stripInfo[5 + len+len2];
                    if (len3 != 0 && len3 < 1024)
                        trackOne.append(" \ntrack3: " + new String(stripInfo, 6 + len + len2, len3));
               
                    if(!trackOne.toString().equals("")) {
                        trackOne.append("\n");
                        mHandler.removeMessages(MESSAGE_CHECK_FAILE);
                        Message msg = mHandler.obtainMessage(MESSAGE_READ_MAG);
                        Log.d("MagManager", trackOne.toString());
                        Bundle bundle = new Bundle();
                        bundle.putString(CARD_TRACK1, trackOne.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
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
