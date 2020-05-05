package com.example.iccmanager;

import android.os.Bundle;
import android.app.Activity;
import android.device.IccManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ICCActivity extends Activity {
    private EditText mNo;
    private Button mSend;
    private Button mDefApdu;
    private Button mReset;
    private Button mDetect;
    private Button mInitIC;

    EditText mEmission;
    private IccManager mIccReader;

    byte[] apduUtf = {
            0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
            0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icc);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mNo = (EditText) findViewById(R.id.editText1);
        mEmission = (EditText) findViewById(R.id.emission);
        mSend = (Button) findViewById(R.id.button1);
        mSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String reception = mEmission.getText().toString();
                Log.i("debug", "onEditorAction:" + reception);

                if (!reception.equals("")) {
                    if (Convert.isHexAnd16Byte(reception, ICCActivity.this)) {
                        mNo.append("SEND: " + reception + "\n");
                        byte[] apdu = Convert.hexStringToByteArray(reception);
                        sendCmd(apdu, 1);
                    }
                } else {
                    Toast.makeText(ICCActivity.this, "please input content", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDefApdu = (Button) findViewById(R.id.button2);
        mDefApdu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCmd(apduUtf, 0);
            }
        });

        mReset = (Button) findViewById(R.id.button3);
        mReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                byte[] atr = new byte[64];
                int retLen = mIccReader.activate(atr);
                // print the atr
                if (retLen == -1) {
                    mNo.append(" IC Card reset failed......." + "\n");
                } else {
                    mNo.append("ATR: " + Convert.bytesToHexString(atr, 0, retLen) + "\n");
                }
            }
        });

        mDetect = (Button) findViewById(R.id.btn_detect);
        mDetect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int status = mIccReader.detect();
                if (status != 0) {
                    mNo.append("Please inster IC Card....... : " + status + "\n");
                } else {
                    mNo.append("Card inserted successfully... : " + status + "\n");
                }
            }
        });

        mInitIC = (Button) findViewById(R.id.btn_init_ic);
        mInitIC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int ret = mIccReader.open((byte) 0, (byte) 0x01, (byte) 0x01);
                if (ret == 0) {
                    mNo.append("init success : " + ret + "\n");
                } else {
                    mNo.append("init failed : " + ret + "\n");
                }
            }
        });
    }

    private void sendCmd(byte[] cmd, int type) {
        int apduCount = (type == 1) ? cmd.length : apduUtf.length;
        byte[] rspBuf = new byte[256];
        byte[] rspStatus = new byte[2];
        int retLen = mIccReader.apduTransmit((type == 1) ? cmd : apduUtf, (char) apduCount, rspBuf, rspStatus);
        if (retLen == -1) {
            mNo.append("APDU RSP REVC: failed  " + retLen + "\n");
            return;
        }
        mNo.append("APDU RSP REVC: " + Convert.bytesToHexString(rspBuf, 0, retLen) + "\n");
        mNo.append("APDU RSP REVC Status : " + Convert.bytesToHexString(rspStatus, 0, 2) + "\n");
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
        if (mIccReader != null) {
            int ret = mIccReader.deactivate();
            Log.i("mIccReader", "-----------Eject-----retr=" + ret);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mIccReader = new IccManager();
    }
}
