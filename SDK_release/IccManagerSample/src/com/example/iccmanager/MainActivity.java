
package com.example.iccmanager;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.device.IccManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private EditText mNo;
    private Button mSend;
    private Button mDefApdu;
    private Button mReset;
    private Button mSetApduTimeout;
    private Button mGetRsp;
    private Button mSetETU;
    private Button mStopApduRspRecv;
    private Button mDetect;
    private Button mInitIC;
    private Button mInitSle4442;
    
    EditText mEmission;
    private IccManager mIccReader;
    IccReaderThread mIccReadeThreadr;
    
    byte[] apdu_utf = {
            0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
            0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mNo = (EditText) findViewById(R.id.editText1);
        mEmission = (EditText) findViewById(R.id.emission);
        mSend = (Button) findViewById(R.id.button1);
        mSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String reception = mEmission.getText().toString();
                android.util.Log.i("debug", "onEditorAction:" + reception);
                
                if(!reception.equals("")) {
                    if(Convert.isHexAnd16Byte(reception, MainActivity.this)) {
                        mNo.append("SEND: " + reception + "\n");
                            byte[] apdu= Convert.hexStringToByteArray(reception);
                            sendCmd(apdu, 2);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "please input content", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDefApdu = (Button) findViewById(R.id.button2);
        mDefApdu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCmd(apdu_utf, 3);
            }
        });
        
        mReset = (Button) findViewById(R.id.button3);
        mReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                byte[] atr = new byte[64];
                int retLen = mIccReader.activate(atr);
                // 6.print the atr
                if (retLen == -1) {
                    mNo.append(" IC Card reset faile......." + "\n");
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
                    mNo.append("Please inster IC Card......." + "\n" + status);
                }
                
            }
        });
        
        mInitIC = (Button) findViewById(R.id.btn_init_ic);
        mInitIC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIccReader.open((byte)0, (byte)0x01, (byte)0x01);
            }
        });
        
        mInitSle4442 = (Button) findViewById(R.id.btn_init_sle);
        mInitSle4442.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIccReader.open((byte)1, (byte)0x02, (byte)0x01);
            }
        });
    }

    private void sendCmd(byte[] cmd, int type) {
        
     // 4.select solt one
        /*int status = mIccReader.SelectSlot((byte)1);
        if (status != 0) {
            return;
        }*/
        
        /*status = mIccReader.Detect();
        if (status != 0) {
            mNo.append("Please inster IC Card......." + "\n");
            return;
        }*/
        
        /*byte[] atr = new byte[64];
        int retLen = mIccReader.Reset(atr);
        // 6.print the atr
        if (retLen == -1) {
            mNo.append(" IC Card reset faile......." + "\n");
            return;
        }
        if(type == 1) {
            mNo.append("ATR: " + Convert.bytesToHexString(atr, 0, retLen) + "\n");
            return;
        } */
        
        int apdu_count = (type == 2 ) ? cmd.length : apdu_utf.length;
        byte[] rspBuf = new byte[256];
        byte[] rspStatus = new byte[2];
        int retLen = mIccReader.apduTransmit((type == 2 ) ? cmd : apdu_utf, (char) apdu_count, rspBuf, rspStatus);
        if (retLen == -1) {
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
        //if(mIccReadeThreadr != null)
        //    mIccReadeThreadr.threadrun = false;
        if(mIccReader != null) {
            int ret = mIccReader.deactivate();
            android.util.Log.i("mIccReader", "-----------Eject-----retr=" + ret);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mIccReader = new IccManager();
        //mIccReadeThreadr = new IccReaderThread("ReaderThread");
        //mIccReadeThreadr.start();
    }

    private boolean onDataReceivedF(final byte[] content, final String type) {
        Log.i("mIccManager","-------onDataReceivedF---------------");
        runOnUiThread(new Runnable() {
            public void run() {
                if (content != null) {
                    //mNo.setText("");
                    
                    String contenta = Convert.bytesToHexString(content);
                   if(contenta != null) {
                       int len = contenta.length();
                       mNo.append(type);
                       mNo.append(contenta);
                       mNo.append("\n");
                   }
                    
                 
                }
                }
            });
        return true;
    }
    
    private class IccReaderThread extends Thread {
        private boolean threadrun = true;

        public IccReaderThread(String name) {
            super(name);

        }

        public void run() {
            
            while (threadrun) {
                Log.i("mIccManager","-------run---------------");
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*boolean ret = mIccReader.IccOpen();
                if (!ret) {
                    continue;
                }

                byte[] retb = mIccManager.IccFound();
                if (retb == null && retb.length <= 0) {
                    continue;
                }
                for (int i = 0; i < 4; i++) {
                    android.util.Log.i("mIccManager", "---------------------------------retb=["
                            + retb[i] + "]");
                }
                // 3.solt one has card?
                if (retb[0] != 1) {
                    continue;
                }*/
                // 4.select solt one
               /* int status = mIccReader.SelectSlot((byte)1);
                if (status != 0) {
                    continue;
                }
                status = mIccReader.Detect();
                if (status != 0) {
                    continue;
                }
                byte[] atr = new byte[64];
                int ret = mIccReader.Reset(atr);
                // 6.print the atr
                if (ret == -1) {
                    continue;
                }
                for (int i = 0; i < atr.length && atr.length < 33; i++) {
                    android.util.Log.i("mIccManager", "----------------atr=[" + atr[i] + "]");
                }
                onDataReceivedF(atr, "ATR: ");
                
                byte[] apdu_utf = {
                        0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
                        0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
                };
                int apdu_count = apdu_utf.length;
                byte[] rspBuf = new byte[256];
                byte[] rspStatus = new byte[1];
                int retr = mIccReader.ApduTransmit(apdu_utf, (char) apdu_count, rspBuf, rspStatus);
                if (retr == -1) {
                    continue;
                }
                for (int i = 0; i < retr; i++) {
                    android.util.Log.i("mIccReader", "----------------retr=[" + rspBuf[i] + "]");
                }
                onDataReceivedF(rspBuf, "APDU Out: ");
                //mIccManager.IccClose();
*/            }
            
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        PackageManager pk = getPackageManager();
        PackageInfo pi;
        try {
            pi = pk.getPackageInfo(getPackageName(), 0);
            Toast.makeText(this, "V" +pi.versionName , Toast.LENGTH_SHORT).show();
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
}
