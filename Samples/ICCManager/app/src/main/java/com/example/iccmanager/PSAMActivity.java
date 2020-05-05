package com.example.iccmanager;

import android.os.Bundle;
import android.app.Activity;
import android.device.IccManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class PSAMActivity extends Activity {
    private EditText mNo;

    private IccManager mIccManager;
    IccReaderThread mIccReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psam);

        mNo = (EditText) findViewById(R.id.editText1);
        mNo.append("Enter this interface after inserting the card, and the thread will automatically detect and print the card information... \n");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mIccReader != null)
            mIccReader.threadrun = 3;
        if (mIccManager != null) {
            mIccManager.close();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mIccManager = new IccManager();
        mIccReader = new IccReaderThread("ReaderThread");
        mIccReader.start();
    }

    private boolean onDataReceived(final String type) {
        runOnUiThread(new Runnable() {
            public void run() {
                mNo.append(type);
                mNo.append("\n");
            }
        });
        return true;
    }

    private boolean onDataReceivedF(final byte[] content, final int length, final String type) {
        Log.i("mIccManager", "-------onDataReceivedF---------------");
        runOnUiThread(new Runnable() {
            public void run() {
                if (content != null) {
                    // mNo.setText("");
                    String contentStr = Convert.bytesToHexString(content, 0, length);
                    if (contentStr != null) {
                        mNo.append(type);
                        mNo.append(contentStr);
                        mNo.append("\n");
                    }
                }
            }
        });
        return true;
    }

    private class IccReaderThread extends Thread {
        private int threadrun = 3;

        public IccReaderThread(String name) {
            super(name);

        }

        public void run() {

            while (threadrun != 0) {
                Log.i("mIccManager", "-------run---------------");
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int status = mIccManager.open((byte) 0x01, (byte) 0x01, (byte) 0x01);   // PSAM1 slot
                Log.i("mIccManager", "-------open-------------" + status);
                if (status != 0) {
                    mNo.append("Open PSAM1 slot failed! \n");
                    continue;
                }
                int ret = mIccManager.detect();
                Log.i("mIccManager", "-------detect-------------" + ret);
                if (ret != 0) {
                    mNo.append("Card not detected! \n");
                    continue;
                }
                byte[] atr = new byte[64];
                int retLen = mIccManager.activate(atr);
                Log.i("mIccManager", "-------activate-------------" + retLen);
                if (retLen == -1) {
                    mNo.append("activate failed! \n");
                    continue;
                }

                onDataReceivedF(atr, retLen, "ATR: ");

                byte[] apduUtf = {
                        0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
                        0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
                };
                int apdCount = apduUtf.length;
                byte[] rspBuf = new byte[256];
                byte[] rspStatus = new byte[2];
                retLen = mIccManager.apduTransmit(apduUtf, (char) apdCount, rspBuf, rspStatus);
                if (retLen == -1) {
                    continue;
                }
                onDataReceivedF(rspBuf, retLen, "APDU Out: ");
                onDataReceivedF(rspStatus, 2, "rspStatus Out: ");
                int retSta = mIccManager.close();
                if (retSta == 0) onDataReceived("IccClose =" + retSta);
                Log.i("mIccManager", "----------------IccClose=[" + retSta + "]");
                threadrun--;
            }
        }
    }

}
