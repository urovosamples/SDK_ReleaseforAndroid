package com.example.magmanager;

import android.device.MagManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public final static int MESSAGE_OPEN_MAG = 1;
    public final static int MESSAGE_CHECK_FAILE = 2;
    public final static int MESSAGE_READ_MAG = 3;
    public final static int MESSAGE_CHECK_OK = 4;

    private EditText mNo;
    private ToneGenerator tg = null;
    private TextView mAlertTv;
    private MagReaderThread magReaderThread;
    private MagManager magManager;
    private boolean encryptStrip = false;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ_MAG:
                    updateAlert("Read the card successed!", 1);
                    String track1 = msg.getData().getString("track");
                    if (!track1.equals(""))
                        beep();
                    mNo.append(track1);
                    break;
                case MESSAGE_OPEN_MAG:
                    updateAlert("Init Mag Reader failed!", 2);
                    break;
                case MESSAGE_CHECK_FAILE:
                    updateAlert("Please Pay by card!", 2);
                    break;
                case MESSAGE_CHECK_OK:
                    updateAlert("Pay by card successed!", 1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mNo = (EditText) findViewById(R.id.editText1);
        mAlertTv = (TextView) findViewById(R.id.textView1);
        tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        magManager = new MagManager();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
        //Initialize and start the swipe thread
        magReaderThread = new MagReaderThread();
        magReaderThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (magManager != null) {
            /*
                4:close() Turn off MSR card reading
             */
            magManager.close();
        }
        if (magReaderThread != null) {
            magReaderThread.stopMagReader();
            magReaderThread = null;
        }
    }

    //Page update status display
    private void updateAlert(String mesg, int type) {
        if (type == 2)
            mAlertTv.setBackgroundColor(Color.RED);
        else
            mAlertTv.setBackgroundColor(Color.GREEN);
        mAlertTv.setText(mesg);

    }

    //Prompt tone
    private void beep() {
        if (tg != null)
            tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
    }

    /*
    Credit card flow:  1:open() Turn on MSR card reading
                       2:checkCard()  Check whether the card swiping operation occurs
                       3：getAllStripInfo(stripInfo)  Read track information
                       4:close() Turn off MSR card reading
    */
    private class MagReaderThread extends Thread {

        //Flag
        private boolean running = false;

        @Override
        public synchronized void start() {
            super.start();
            running = true;
        }

        public void stopMagReader() {
            running = false;
        }

        @Override
        public void run() {
            super.run();
            /*
                1: Open MSR card reading and return 0 to open successfully
            */
            if (magManager != null) {
                int ret = magManager.open();
                if (ret != 0) {
                    mHandler.sendEmptyMessage(MESSAGE_OPEN_MAG);
                    return;
                }
            }
            while (running) {
                if (magManager == null)
                    return;
                /*
                    2:  Check whether the card swiping operation occurs. This action needs to be performed in a circular manner
                        If 0 is returned, swipe card is detected
                */
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
                //Storing track information
                byte[] stripInfo = new byte[1024];

                /*
                    3: Read track information
                        stripInfo:Information is stored in t-l-v (tag length value) format
                              Mark as 1 byte, meaning as follows:
                                            01:track1；
                                            02:track2；
                                            03:track3；
                              The length is 1 byte.
                */
                int allLen = magManager.getAllStripInfo(stripInfo);

                //To use dukpt to encrypt track information, you need to download the dukpt secret key first
                if (encryptStrip) {
                    byte[] cardNo = new byte[1024];
                    int AlgMode = 1;
                    int keyIndex = 101;
                    byte[] KSN = new byte[10];
                    allLen = magManager.getEncryptStripInfo(AlgMode, keyIndex, stripInfo, cardNo, KSN);
                }
                //Analyzing track information
                if (allLen > 0) {
                    //track1
                    int len = stripInfo[1];
                    if (len != 0)
                        trackOne.append(" track1: " + new String(stripInfo, 2, len));
                    //track2
                    int len2 = stripInfo[3 + len];
                    if (len2 != 0)
                        trackOne.append(" \ntrack2: " + new String(stripInfo, 4 + len, len2));
                    //track3
                    int len3 = stripInfo[5 + len + len2];
                    if (len3 != 0 && len3 < 1024)
                        trackOne.append(" \ntrack3: " + new String(stripInfo, 6 + len + len2, len3));

                    //Notify main thread to read information
                    if (!trackOne.toString().equals("")) {
                        trackOne.append("\n");
                        mHandler.removeMessages(MESSAGE_CHECK_FAILE);
                        Message msg = mHandler.obtainMessage(MESSAGE_READ_MAG);
                        Bundle bundle = new Bundle();
                        bundle.putString("track", trackOne.toString());
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
