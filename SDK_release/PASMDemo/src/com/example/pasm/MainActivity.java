
package com.example.pasm;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.device.IccManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText mNo;

    private IccManager mIccManager;

    IccReaderThread mIccReader;

    private static final byte[] APDU = new byte[] {
            0x00, (byte) 0x84, 0x00, 0x00, 0x08
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNo = (EditText) findViewById(R.id.editText1);
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
            mIccReader.threadrun = false;
        if (mIccManager != null) {
            mIccManager.close();
            // mIccManager = null;
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

    /*
     * Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public static String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
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
                    String contenta = bytesToHexString(content, 0, length);
                    if (contenta != null) {
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
                // 1.select solt one
                double vol;
                for (int ii = 0; ii < 3; ii++) {
                    switch (ii) {
                    case 0:
                        vol =  3;
                        break;
                    case 1:
                        vol =  1;
                        break;
                    case 2:
                        vol =  2;
                        break;
                    default:
                        vol =  1;
                        break;
                    }

                    /*vol:
                    0x01 - 3V;
                    0x02 - 5V;
                    0x03 - 1.8V;*/

                int status = mIccManager.open( (byte)1,  (byte)1, (byte)vol);//IccSelect((char) 0);
                Log.i("mIccManager","-------open-------------" + status);
                if (status != 0) {
                    return;
                }
                //boolean ret = mIccManager.IccOpen();
                /*int ret = mIccManager.detect();
                Log.i("mIccManager","-------detect-------------" + ret);
                if (ret != 0) {
                    continue;
                }*/
                byte[] atr = new byte[64];
                int retLen = mIccManager.activate(atr);
                Log.i("mIccManager","-------activate-------------" + retLen);
                if (retLen == -1) {
                    continue;
                }
 
                    onDataReceivedF(atr, retLen, "ATR: ");
                    
                    byte[] apdu_utf = {
                            0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
                            0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
                    };
                    int apdu_count = apdu_utf.length;
                    byte[] rspBuf = new byte[256];
                    byte[] rspStatus = new byte[2];
                    retLen = mIccManager.apduTransmit(apdu_utf, (char) apdu_count, rspBuf, rspStatus);
                    if (retLen == -1) {
                        continue;
                    }
                    onDataReceivedF(rspBuf, retLen, "APDU Out: ");
                    onDataReceivedF(rspStatus, 2, "rspStatus Out: ");
                    int retsta = mIccManager.close();
                    if(retsta == 0)onDataReceived("IccClose =" + retsta );
                    android.util.Log.i("mIccManager", "----------------IccClose=[" + retsta+ "]");
                    break;
                }
            }
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
