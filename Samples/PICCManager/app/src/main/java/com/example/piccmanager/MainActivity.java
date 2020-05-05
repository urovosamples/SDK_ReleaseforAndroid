
package com.example.piccmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.device.PiccManager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "PiccCheck";

    private static final int MSG_BLOCK_NO_NONE = 0;
    private static final int MSG_BLOCK_NO_ILLEGAL = 1;
    private static final int MSG_AUTHEN_FAIL = 2;
    private static final int MSG_WRITE_SUCCESS = 3;
    private static final int MSG_WRITE_FAIL = 4;
    private static final int MSG_READ_FAIL = 5;
    private static final int MSG_SHOW_BLOCK_DATA = 6;
    private static final int MSG_ACTIVE_FAIL = 7;
    private static final int MSG_APDU_FAIL = 8;
    private static final int MSG_SHOW_APDU = 9;
    private static final int MSG_BLOCK_DATA_NONE = 10;
    private static final int MSG_AUTHEN_BEFORE = 11;
    private static final int MSG_FOUND_UID = 12;

    private Button bOpen;
    private Button bCheck;
    private Button bRead;
    private Button bAuthen;
    private Button bWrite;
    private Button bApdu;
    private EditText Emission;
    private EditText etBlockData;
    private EditText etBlockNo;
    private EditText AuthenKey;
    private TextView tvApdu;

    private PiccManager piccReader;

    private Handler handler;
    private ExecutorService exec;

    boolean hasAuthen = false;
    int blkNo;
    int scanCard = -1;
    int SNLen = -1;

    byte EMV_APDU[] = {
            0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53,
            0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
    };

    byte keyBuf[] = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    };

    byte Wbuf[] = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d,
            0x0e, 0x0f
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        piccReader = new PiccManager();
        exec = Executors.newSingleThreadExecutor();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case MSG_BLOCK_NO_NONE:
                        Toast.makeText(MainActivity.this, R.string.picc_block_no_none,
                                Toast.LENGTH_SHORT).show();

                        SoundTool.getMySound(MainActivity.this).playMusic("error");
                        break;
                    case MSG_BLOCK_NO_ILLEGAL:
                        Toast.makeText(MainActivity.this, R.string.picc_block_no_illegal,
                                Toast.LENGTH_SHORT).show();
                        SoundTool.getMySound(MainActivity.this).playMusic("error");
                        break;
                    case MSG_AUTHEN_FAIL:
                        Toast.makeText(MainActivity.this, R.string.picc_authen_fail,
                                Toast.LENGTH_SHORT).show();
                        SoundTool.getMySound(MainActivity.this).playMusic("error");
                        break;
                    case MSG_AUTHEN_BEFORE:
                        Toast.makeText(MainActivity.this, R.string.picc_authen_before,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_WRITE_SUCCESS:
                        Toast.makeText(MainActivity.this, R.string.picc_write_success,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_WRITE_FAIL:
                        Toast.makeText(MainActivity.this, R.string.picc_write_fail,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_READ_FAIL:
                        Toast.makeText(MainActivity.this, R.string.picc_read_fail,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_APDU_FAIL:
                        Toast.makeText(MainActivity.this, R.string.picc_operate_fail,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_BLOCK_DATA_NONE:
                        Toast.makeText(MainActivity.this, R.string.picc_block_data_none,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_SHOW_BLOCK_DATA:
                        SoundTool.getMySound(MainActivity.this).playMusic("success");
                        String data = (String) msg.obj;
                        tvApdu.append("\n" + data);
                        break;
                    case MSG_ACTIVE_FAIL:
                        Toast.makeText(MainActivity.this, R.string.picc_active_fail,
                                Toast.LENGTH_SHORT).show();
                        SoundTool.getMySound(MainActivity.this).playMusic("error");
                        break;
                    case MSG_SHOW_APDU:
                        String apdu = (String) msg.obj;
                        tvApdu.append("\nAPDU:" + apdu);
                        SoundTool.getMySound(MainActivity.this).playMusic("success");
                        break;
                    case MSG_FOUND_UID:
                        String uid = (String) msg.obj;
                        tvApdu.append("\nUID:" + uid);
                        SoundTool.getMySound(MainActivity.this).playMusic("success");
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        initView();
        super.onResume();
    }

    private void initView() {
        bOpen = (Button) findViewById(R.id.picc_open);
        bCheck = (Button) findViewById(R.id.picc_check);
        bAuthen = (Button) findViewById(R.id.authen);
        bRead = (Button) findViewById(R.id.r_block);
        bWrite = (Button) findViewById(R.id.w_block);
        bApdu = (Button) findViewById(R.id.def_apdu);
        bOpen.setOnClickListener(this);
        bCheck.setOnClickListener(this);
        bAuthen.setOnClickListener(this);
        bRead.setOnClickListener(this);
        bWrite.setOnClickListener(this);
        bApdu.setOnClickListener(this);
        etBlockData = (EditText) findViewById(R.id.write_data);
        etBlockNo = (EditText) findViewById(R.id.block_no);
        tvApdu = (TextView) findViewById(R.id.rev_data);
        Emission = (EditText) findViewById(R.id.EditTextEmission);
        AuthenKey = (EditText) findViewById(R.id.auth_key);
    }

    /**
     * Check if a (hex) string is pure hex (0-9, A-F, a-f) and 16 byte
     * (32 chars) long. If not show an error Toast in the context.
     */
    public static boolean isHexAnd16Byte(String hexString) {
        if (hexString.matches("[0-9A-Fa-f]+") == false) {
            // Error, not hex.
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == bOpen) {
            exec.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int ret = piccReader.open();
                    if (ret == 0) {
                        tvApdu.append("\n Open success");
                        SoundTool.getMySound(MainActivity.this).playMusic("success");
                    } else {
                        tvApdu.append("Open failed \n");
                        return;
                    }
                }
            }, "picc open"));
        } else if (view == bCheck) {
            exec.execute(new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    byte CardType[] = new byte[2];
                    byte Atq[] = new byte[14];
                    char SAK = 1;
                    byte sak[] = new byte[1];
                    sak[0] = (byte) SAK;
                    byte SN[] = new byte[10];
                    scanCard = piccReader.request(CardType, Atq);
                    if (scanCard > 0) {
                        SNLen = piccReader.antisel(SN, sak);
                        Log.d(TAG, "SNLen = " + SNLen);
                        Message msg = handler.obtainMessage(MSG_FOUND_UID);
                        msg.obj = bytesToHexString(SN, SNLen);
                        handler.sendMessage(msg);
                    }
                }
            }, "picc check"));
        } else if (view == bAuthen) {
            exec.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    String str = etBlockNo.getText().toString();
                    if (str == null || str.equals("")) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_NONE);
                        return;
                    }
                    blkNo = Integer.valueOf(str);
                    if (blkNo < 0 || blkNo > 63) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_ILLEGAL);
                        return;
                    }
                    int ret = -1;
                    byte SN[] = new byte[10];
                    String key = AuthenKey.getText().toString();
                    if (!key.equals("") && isHexAnd16Byte(key)) {
                        byte[] keyData = hexStringToBytes(key);
                        ret = piccReader.m1_keyAuth(0, blkNo, keyData.length, keyData, SNLen, SN);
                    } else {
                        ret = piccReader.m1_keyAuth(0, blkNo, 6, keyBuf, SNLen, SN);
                    }
                    if (ret == -1) {
                        handler.sendEmptyMessage(MSG_AUTHEN_FAIL);
                        return;
                    }
                    hasAuthen = true;
                }
            }, "picc authen"));
        } else if (view == bRead) {
            exec.execute(new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (!hasAuthen) {      //Mifare uitralight no to authen
                        handler.sendEmptyMessage(MSG_AUTHEN_BEFORE);
                        return;
                    }
                    String str = etBlockNo.getText().toString();
                    if (str == null || str.equals("")) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_NONE);
                        return;
                    }
                    blkNo = Integer.valueOf(str);
                    if (blkNo < 0 || blkNo > 63) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_ILLEGAL);
                        return;
                    }
                    byte pReadBuf[] = new byte[20];

                    int result = piccReader.m1_readBlock(blkNo, pReadBuf);
                    if (result == -1) {
                        handler.sendEmptyMessage(MSG_READ_FAIL);
                    } else {
                        Message msg = handler.obtainMessage(MSG_SHOW_BLOCK_DATA);
                        msg.obj = bytesToHexString(pReadBuf, result);
                        handler.sendMessage(msg);
                    }
                }
            }, "picc read"));
        } else if (view == bWrite) {
            exec.execute(new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (!hasAuthen) {
                        handler.sendEmptyMessage(MSG_AUTHEN_BEFORE);
                        return;
                    }
                    String str = etBlockNo.getText().toString();
                    if (str == null || str.equals("")) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_NONE);
                        return;
                    }
                    blkNo = Integer.valueOf(str);
                    if (blkNo < 0 || blkNo > 63) {
                        handler.sendEmptyMessage(MSG_BLOCK_NO_ILLEGAL);
                        return;
                    }
                    String data = etBlockData.getText().toString();
                    if (data == null || data.equals("") || !isHexAnd16Byte(data)) {
                        handler.sendEmptyMessage(MSG_BLOCK_DATA_NONE);
                        return;
                    }
                    byte[] writeData = hexStringToBytes(data);
                    int result = piccReader.m1_writeBlock(blkNo, writeData.length,
                            writeData);
                    if (result == 0) {
                        handler.sendEmptyMessage(MSG_WRITE_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(MSG_WRITE_FAIL);
                    }
                }
            }, "picc write"));
        } else if (view == bApdu) {
            exec.execute(new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    byte readBuf[] = new byte[512];
                    byte sw[] = new byte[2];
                    byte atr[] = new byte[32];
                    int ret = piccReader.activateEx(atr);
                    if (ret == -1) {
                        handler.sendEmptyMessage(MSG_ACTIVE_FAIL);
                        return;
                    }
                    String art = bytesToHexString(atr, ret);
                    Log.d("debug", ret + " atr " + art);
                    int result = -1;
                    String reception = Emission.getText().toString();
                    if (!reception.equals("") && isHexAnd16Byte(reception)) {
                        byte[] apdu = hexStringToByteArray(reception);
                        result = piccReader.apduTransmit(apdu, apdu.length, readBuf, sw);   //Desfire Card piccReader.apduTransmit(apdu, apdu.length, read_buf);
                    } else {
                        result = piccReader.apduTransmit(EMV_APDU, EMV_APDU.length, readBuf, sw);
                    }
                    if (result == -1) {
                        handler.sendEmptyMessage(MSG_APDU_FAIL);
                    } else {
                        Message msg = handler.obtainMessage(MSG_SHOW_APDU);
                        msg.obj = bytesToHexString(readBuf, result);
                        msg.arg1 = result;
                        handler.sendMessage(msg);
                    }
                }
            }, "def apdu"));
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        exec.shutdown();
        super.onDestroy();
    }

    /**
     * Convert a string of hex data into a byte array.
     * Original author is:
     *
     * @param s The hex string to convert
     * @return An array of bytes with the values of the string.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
            Log.d("debug", "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }

    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        SoundTool.getMySound(this).release();
        super.onPause();
    }
}
