package com.example.dupkttest;

import android.app.Activity;
import android.content.Context;
import android.device.SEManager;
import android.os.Handler;
import android.os.IInputActionListener;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    private MagReadService mReadService;
    SEManager mSEManager;
    private static final String TAG = "MainActivity";

    private boolean flag = false;
    static TextView tv;
    static Context mContext;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    EditText mEdtext;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MagReadService.MESSAGE_READ_MAG:
                    int track1Length = msg.getData().getInt("CARD_TRACK1_L");
                    byte[] outTrack1 = msg.getData().getByteArray(MagReadService.CARD_TRACK1);
                    byte[] KSN = msg.getData().getByteArray(MagReadService.CARD_KSN);

                    String CardNo = msg.getData().getString(MagReadService.CARD_NUMBER);
                    StringBuffer trackOne = new StringBuffer();
                    if (track1Length > 0) {
                        tv.append("ECCTrack_1_2=" + track1Length + "\n");
                        tv.append(DecodeConvert.bytesToHexString(outTrack1, 0, track1Length));
                        tv.append("\n");
                        trackOne.append("ECCTrack1=" + DecodeConvert.bytesToHexString(outTrack1, 0, track1Length));
                    }
                    trackOne.append("CardNo=" + CardNo);
                    Log.d(TAG, "trackOne: " + trackOne.toString());
                    tv.append("CardNo=");
                    tv.append(CardNo);
                    tv.append("\n");
                    tv.append("KSN=");
                    tv.append(DecodeConvert.bytesToHexString(KSN, 0, KSN.length));
                    tv.append("\n");
                    break;
                case MagReadService.MESSAGE_OPEN_MAG:
                    break;
                case MagReadService.MESSAGE_CHECK_FAILE:
                    break;
                case MagReadService.MESSAGE_CHECK_OK:
                    break;
            }
        }
    };

    /*
        data encryption
    */
    void my_ecrypt_01() {
        String strPlain = "000102030405060708090A0B0C0D0E0F";  //Raw data

        byte[] rawData = DecodeConvert.hexStringToByteArray(strPlain);
        int rawDataLen = strPlain.length() / 2;
        byte[] outData = new byte[strPlain.length() / 2];
        int[] outLen = new int[2];
        byte[] bsKsn = new byte[10];
        int[] KsnLen = new int[2];

        tv.setText("");
        tv.append("rawData:" + Funs.bytesToHexString(rawData) + "\n");
        Log.d(TAG, "button4-------TEST ENCRYPT DATA my_ecrypt_01:" + "\n");
        int ret = mSEManager.encryptWithPEK(0x03, 0x01, rawData, rawDataLen, outData, outLen, bsKsn, KsnLen);
        if (ret == 0) {
            tv.append("KSN1:" + Funs.bytesToHexString(bsKsn) + "\n");
            tv.append("outdata1:" + Funs.bytesToHexString(outData) + "\n");

            Log.d(TAG, "rawData:" + Funs.bytesToHexString(rawData) + "\n");
            Log.d(TAG, "outData:" + Funs.bytesToHexString(outData) + "\n");
            Log.d(TAG, "KSN:" + Funs.bytesToHexString(bsKsn) + "\n");

            int ret2 = checkEncryptResult(Funs.bytes2HexString(bsKsn, 10), Funs.bytes2HexString(outData, outLen[0]));
            if (0 == ret2) {
                tv.append("dukpt Encrypt is right\n");
            } else if (1 == ret2) {
                tv.append("KSN is big, please init key again, or use bp-tool verify \n");
            } else {
                tv.append("dukpt Encrypt is error\n");
            }
        } else {
            tv.append("failed,  encryptWithPEK ret:" + ret + " \n");
        }
    }

    int checkEncryptResult(String ksn, String outData) {
        Log.d(TAG, "KSN:" + ksn + " outdata:" + outData + "\n");
        if (ksn.equals("11111746011bede00002")) {
            if (outData.equals("c21ce893fc7abf345a2112bcc31b0cdc"))
                return 0;
            else
                return -1;
        } else if (ksn.equals("11111746011bede00003")) {
            if (outData.equals("f158e4bda19e6db81ba10daac7e47a07"))
                return 0;
            else
                return -1;
        }
        return 1;
    }

    /*
        calculate mac 3des
     */
    void my_ecrypt_mac_3des() {
        String str = "111231323334454637383132333445461112313233344546373831323334454622";  //Raw data

        byte[] rawData = DecodeConvert.hexStringToByteArray(str);
        int rawDataLen = str.length() / 2;
        byte[] outData = new byte[mEdtext.getText().length() / 2];
        int[] outLen = new int[2];
        byte[] bsKsn = new byte[10];
        int[] KsnLen = new int[2];

        int ret = mSEManager.calculateMACOfDUKPTExtend(0x04, rawData, rawDataLen, outData, outLen, bsKsn, KsnLen);
        Log.d(TAG, "button4------- calculateMACOfDUKPTExtend ret:" + ret + "\n");
        tv.setText("");
        if (ret == 0 && outData.length != 0) {
            Log.d(TAG, "rawData:" + Funs.bytesToHexString(rawData) + "\n");
            Log.d(TAG, "outdata:" + Funs.bytesToHexString(outData) + "\n");
            Log.d(TAG, "KSN:" + Funs.bytesToHexString(bsKsn) + "\n");

            int ret2 = checkCalMacResult(Funs.bytes2HexString(bsKsn, 10), Funs.bytes2HexString(outData, 8));
            if (0 == ret2) {
                tv.append("dukpt mac is right\n");
            } else if (1 == ret2) {
                tv.append("KSN is big, please init key again, or use bp-tool verify \n");
            } else {
                tv.append("dukpt mac is error\n");
            }
        } else {
            tv.append("error calculateMACOfDUKPTExtend ret:" + ret + " outdata.length:" + outData.length + "\n");
        }
    }

    public int checkCalMacResult(String ksn, String mac) {
        Log.d(TAG, "KSN:" + ksn + " mac:" + mac + "\n");
        if (ksn.equals("44111746011bede00002")) {
            if (mac.equals("45e09ea7fbbb0d22"))
                return 0;
            else
                return -1;
        } else if (ksn.equals("44111746011bede00003")) {
            if (mac.equals("1df82352d5d74d91"))
                return 0;
            else
                return -1;
        } else if (ksn.equals("44111746011bede00004")) {
            if (mac.equals("2696863e7a629bbe"))
                return 0;
            else
                return -1;
        } else if (ksn.equals("44111746011bede00005")) {
            if (mac.equals("723054b2a8471416"))
                return 0;
            else
                return -1;
        } else if (ksn.equals("44111746011bede00006")) {
            if (mac.equals("01e7227dbe2598e5"))
                return 0;
            else
                return -1;
        }
        return 1;
    }

    void init_test_dukpt() {
        if (true) {
            byte[] bsBdk = DecodeConvert.hexStringToByteArray("33333333333333333333333333333333");
            byte[] bsIpek = DecodeConvert.hexStringToByteArray("703DCF7100C7C516F7E08D5D85CE6D74");
            byte[] bsKsn = DecodeConvert.hexStringToByteArray("11111746011BEDE00001");
            tv.setText("");

            bsKsn[0] = 0x11;
            int ret = mSEManager.downloadKeyDukpt(0x01, null, 0, bsKsn, bsKsn.length, bsIpek, bsIpek.length);
            Log.d(TAG, "downloadKeyDukpt 0x01:" + ret + "\n");
            tv.append("downloadKeyDukpt index 0x01 ret:" + ret + "\n");

            bsKsn[0] = 0x22;
            ret = mSEManager.downloadKeyDukpt(0x02, bsBdk, bsBdk.length, bsKsn, bsKsn.length, null, 0);
            Log.d(TAG, "downloadKeyDukpt 0x02:" + ret + "\n");
            tv.append("downloadKeyDukpt index 0x02 ret:" + ret + "\n");

            bsKsn[0] = 0x33;
            ret = mSEManager.downloadKeyDukpt(0x03, bsBdk, bsBdk.length, bsKsn, bsKsn.length, null, 0);
            Log.d(TAG, "downloadKeyDukpt 0x03:" + ret + "\n");
            tv.append("downloadKeyDukpt index 0x03 ret:" + ret + "\n");

            bsKsn[0] = 0x44;
            ret = mSEManager.downloadKeyDukpt(0x04, bsBdk, bsBdk.length, bsKsn, bsKsn.length, null, 0);
            Log.d(TAG, "downloadKeyDukpt 0x04:" + ret + "\n");
            tv.append("downloadKeyDukpt index 0x04 ret:" + ret + "\n");
        } else {
            tv.append("fail\n");
        }
        button2.setEnabled(true);
        button3.setEnabled(true);
        button4.setEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermisionUtils mPermisionUtils = new PermisionUtils();
        mPermisionUtils.verifyStoragePermissions(MainActivity.this);

        mContext = MainActivity.this;
        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        mEdtext = (EditText) findViewById(R.id.sample_EditText);
        mEdtext.setText("");
        mSEManager = new SEManager();
        mReadService = new MagReadService(this, mHandler);

        button1 = (Button) findViewById(R.id.button);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View view) {
                // download 4 dukpt, use different KSN
                Log.d(TAG, "button1------- onClick" + "\n");
                init_test_dukpt();
            }
        });

        button2 = (Button) findViewById(R.id.button4);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View view) {
                Log.d(TAG, "button2------- onClick" + "\n");
                my_ecrypt_01();
            }
        });

        button3 = (Button) findViewById(R.id.button2);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View view) {
                Log.d(TAG, "button3------- onClick" + "\n");
                GetDukptPinBlock();
            }
        });

        button4 = (Button) findViewById(R.id.button5);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View view) {
                Log.d(TAG, "button4------- onClick" + "\n");
                my_ecrypt_mac_3des();
            }
        });

        button2.setEnabled(false);
        button3.setEnabled(false);
        button4.setEnabled(false);
    }


    public void GetDukptPinBlock() {

        String cardNumStr = new String("1234567890123456");  //Card number
        Log.i(TAG, " cardno=" + cardNumStr);

        Bundle paramVar = new Bundle();
        paramVar.putInt("inputType", 0);
        paramVar.putInt("KeyUsage", 2);
        paramVar.putInt("PINKeyNo", 1);
        paramVar.putBoolean("bypass", false);
        paramVar.putInt("pinAlgMode", 5);  //
        paramVar.putString("cardNo", cardNumStr);
        paramVar.putBoolean("sound", true);
        paramVar.putBoolean("onlinePin", true);
        paramVar.putBoolean("FullScreen", true);
        paramVar.putLong("timeOutMS", 60000);
        //paramVar.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12");
        paramVar.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12");
        paramVar.putString("title", "Security Keyboard");
        paramVar.putString("message", "Please input pin and cover by hand\n");

        tv.setText("");
        SEManager mSEManager = new SEManager();
        mSEManager.getPinBlockEx(paramVar, new IInputActionListener.Stub() {
            @Override
            public void onInputChanged(int result, int arg1, Bundle bundle) throws RemoteException {
                Log.i(TAG, "bundle.getByteArray bundle:" + bundle.toString());
                Log.i(TAG, "bundle.getByteArray result:" + result);
                if (arg1 < 6) {
                    Log.i(TAG, "arg1 : " + arg1);
                    tv.append("Please enter at least 4 digits, PINPAD input 123456");
                } else if (result == 0) {
                    final byte[] pinBlock = bundle.getByteArray("pinBlock");
                    final byte[] ksn = bundle.getByteArray("ksn");

                    Log.i(TAG, "bundle.getByteArray pinBlock:" + Funs.bytes2HexString(pinBlock, pinBlock.length));
                    Log.i(TAG, "bundle.getByteArray ksn:" + Funs.bytes2HexString(ksn, ksn.length));

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            int ret = compareGetPinResult(Funs.bytes2HexString(ksn, ksn.length), Funs.bytes2HexString(pinBlock, pinBlock.length));
                            if (ret == 0) {
                                tv.append("getPinBlockEx pinblock is right");
                            } else {
                                tv.append("please INIT TEST KEY again, PINPAD input 123456");
                            }
                        }
                    });
                } else {
                    //tv.setText("getPinBlockEx result="+result);
                }
            }
        });
    }

    public int compareGetPinResult(String ksn, String PinBlock) {
        Log.i(TAG, "ksn:" + ksn + "  Pinblock:" + PinBlock);

        if (ksn.equals("11111746011bede00002")) {
            if (PinBlock.equals("b2b1d98a09154698"))
                return 0;
        }
        if (ksn.equals("11111746011bede00003")) {
            if (PinBlock.equals("d08d21cfc6d6ca95"))
                return 0;
        }
        if (ksn.equals("11111746011bede00004")) {
            if (PinBlock.equals("7d91417e5d441417"))
                return 0;
        }

        return -1;
    }


    private static String bytes2HexString(byte[] b) {
        String ret = "";
        for (byte element : b) {
            String hex = Integer.toHexString(element & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }


    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mReadService.stop();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mReadService.start();
    }
}
