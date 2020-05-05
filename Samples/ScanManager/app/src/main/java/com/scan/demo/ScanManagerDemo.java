package com.scan.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.DialogInterface.OnClickListener;
import android.device.scanner.configuration.Constants;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.device.scanner.configuration.Triggering;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.device.ScanManager;
import android.preference.CheckBoxPreference;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * ScanManagerDemo
 *
 * @author shenpidong
 * @effect Introduce the use of android.device.ScanManager
 * @date 2020-03-06
 * @description , Steps to use ScanManager:
 * 1.Obtain an instance of BarCodeReader with ScanManager scan = new ScanManager().
 * 2.Call openScanner to power on the barcode reader.
 * 3.After that, the default output mode is TextBox Mode that send barcode data to the focused text box. User can check the output mode using getOutputMode and set the output mode using switchOutputMode.
 * 4.Then, the default trigger mode is manually trigger signal. User can check the trigger mode using getTriggerMode and set the trigger mode using setTriggerMode.
 * 5.If necessary, check the current settings using getParameterInts or set the scanner configuration properties PropertyID using setParameterInts.
 * 6.To begin a decode session, call startDecode. If the configured PropertyID.WEDGE_KEYBOARD_ENABLE is 0, your registered broadcast receiver will be called when a successful decode occurs.
 * 7.If the output mode is intent mode, the captured data is sent as an implicit Intent. An application interestes in the scan data should register an action as android.intent.ACTION_DECODE_DATA broadcast listerner.
 * 8.To get a still image through an Android intent. Register the "scanner_capture_image_result" broadcast reception image, trigger the scan to listen to the result output and send the "action.scanner_capture_image" broadcast request to the scan service to output the image.
 * 9.Call stopDecode to end the decode session.
 * 10.Call closeScanner to power off the barcode reader.
 * 11.Can set parameters before closing the scan service.
 */
public class ScanManagerDemo extends AppCompatActivity {

    private static final String TAG = "ScanManagerDemo";
    private static final boolean DEBUG = true;

    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE;   // default action
    private static final String ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;
    private static final String BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG;
    private static final String BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG;
    private static final String DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG;

    private static final String DECODE_ENABLE = "decode_enable";
    private static final String DECODE_TRIGGER_MODE = "decode_trigger_mode";
    private static final String DECODE_TRIGGER_MODE_HOST = "HOST";
    private static final String DECODE_TRIGGER_MODE_CONTINUOUS = "CONTINUOUS";
    private static final String DECODE_TRIGGER_MODE_PAUSE = "PAUSE";
    private static String DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST;

    private static final int DECODE_OUTPUT_MODE_INTENT = 0;
    private static final int DECODE_OUTPUT_MODE_FOCUS = 1;
    private static int DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS;
    private static final String DECODE_OUTPUT_MODE = "decode_output_mode";
    private static final String DECODE_CAPTURE_IMAGE_KEY = "bitmapBytes";
    private static final String DECODE_CAPTURE_IMAGE_SHOW = "scan_capture_image";

    private EditText showScanResult = null;
    private Button mScan = null;
    private LinearLayout mHome = null;
    private FrameLayout mFlagment = null;
    private MenuItem settings = null;
    private ImageView mScanImage = null;

    private ScanManager mScanManager = null;
    private static boolean mScanEnable = true;
    private static boolean mScanSettingsView = false;
    private static boolean mScanCaptureImageShow = false;

    private static boolean mScanBarcodeSettingsMenuBarcodeList = false;
    private static boolean mScanBarcodeSettingsMenuBarcode = false;
    private FrameLayout mScanSettingsMenuBarcodeList = null;
    private FrameLayout mScanSettingsMenuBarcode = null;
    private ScanSettingsBarcode mScanSettingsBarcode = null;
    private SettingsBarcodeList mSettingsBarcodeList = null;
    ;
    private static Map<String, BarcodeHolder> mBarcodeMap = new HashMap<String, BarcodeHolder>();

    private static final int MSG_SHOW_SCAN_RESULT = 1;
    private static final int MSG_SHOW_SCAN_IMAGE = 2;

    private ScanSettingsFragment mScanSettingsFragment = new ScanSettingsFragment();

    private static final int[] SCAN_KEYCODE = {520, 521, 522, 523};

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogI("onReceive , action:" + action);
            // Get Scan Image . Make sure to make a request before getting a scanned image
            if (ACTION_CAPTURE_IMAGE.equals(action)) {
                byte[] imagedata = intent.getByteArrayExtra(DECODE_CAPTURE_IMAGE_KEY);
                if (imagedata != null && imagedata.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                    Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_IMAGE);
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                } else {
                    LogI("onReceive , ignore imagedata:" + imagedata);
                }
            } else {
                // Get scan results, including string and byte data etc.
                byte[] barcode = intent.getByteArrayExtra(DECODE_DATA_TAG);
                int barcodeLen = intent.getIntExtra(BARCODE_LENGTH_TAG, 0);
                byte temp = intent.getByteExtra(BARCODE_TYPE_TAG, (byte) 0);
                String barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG);
                if (mScanCaptureImageShow) {
                    // Request images of this scan
                    context.sendBroadcast(new Intent(ACTION_DECODE_IMAGE_REQUEST));
                }
                LogI("barcode type:" + temp);
                String scanResult = new String(barcode, 0, barcodeLen);
                // print scan results.
                scanResult = " length：" + barcodeLen + "\nbarcode：" + scanResult + "\nbytesToHexString：" + bytesToHexString(barcode) + "\nbarcodeStr:" + barcodeStr;
                Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_RESULT);
                msg.obj = scanResult;
                mHandler.sendMessage(msg);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_SCAN_RESULT:
                    String scanResult = (String) msg.obj;
                    printScanResult(scanResult);
                    break;
                case MSG_SHOW_SCAN_IMAGE:
                    if (mScanImage != null && mScanCaptureImageShow) {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        mScanImage.setImageBitmap(bitmap);
                        mScanImage.setVisibility(View.VISIBLE);
                    } else {
                        mScanCaptureImageShow = false;
                        mScanImage.setVisibility(View.INVISIBLE);
                        LogI("handleMessage , MSG_SHOW_SCAN_IMAGE scan image:" + mScanImage);
                    }
                    break;
            }
        }
    };

    /**
     * Button helper
     */
    class ButtonListener implements View.OnClickListener, View.OnTouchListener {
        public void onClick(View v) {
            LogD("ButtonListener onClick");
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.scan_trigger) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    LogD("onTouch button Up");
                    mScan.setText(R.string.scan_trigger_start);
                    if (getTriggerMode() == Triggering.HOST) {
                        stopDecode();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    LogD("onTouch button Down");
                    mScan.setText(R.string.scan_trigger_end);
                    startDecode();
                }
            }
            return false;
        }

    }

    /**
     * @param register , ture register , false unregister
     */
    private void registerReceiver(boolean register) {
        if (register && mScanManager != null) {
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(ACTION_DECODE);
            }
            filter.addAction(ACTION_CAPTURE_IMAGE);

            registerReceiver(mReceiver, filter);
        } else if (mScanManager != null) {
            mScanManager.stopDecode();
            unregisterReceiver(mReceiver);
        }
    }

    /**
     * byte[] toHex String
     *
     * @param src
     * @return String
     */
    public static String bytesToHexString(byte[] src) {
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

    /**
     * Intent Output Mode print scan results.
     *
     * @param msg
     */
    private void printScanResult(String msg) {
        if (msg == null || showScanResult == null) {
            LogI("printScanResult , ignore to show msg:" + msg + ",showScanResult" + showScanResult);
            return;
        }
        showScanResult.setText(msg);
    }

    private void initView() {
        boolean enable = getDecodeScanShared(DECODE_ENABLE);
        mScanEnable = enable;
        showScanResult = (EditText) findViewById(R.id.scan_result);
        mScan = (Button) findViewById(R.id.scan_trigger);
        ButtonListener listener = new ButtonListener();
        mScan.setOnTouchListener(listener);
        mScan.setOnClickListener(listener);

        mScanImage = (ImageView) findViewById(R.id.scan_image);
        mScanCaptureImageShow = getDecodeScanShared(DECODE_CAPTURE_IMAGE_SHOW);
        updateCaptureImage();
        mFlagment = (FrameLayout) findViewById(R.id.fl);
        mScanSettingsMenuBarcodeList = (FrameLayout) findViewById(R.id.flagment_menu_barcode_list);
        mScanSettingsMenuBarcode = (FrameLayout) findViewById(R.id.flagment_menu_barcode);
        mHome = (LinearLayout) findViewById(R.id.homeshow);
    }

    private void updateCaptureImage() {
        if (mScanImage == null) {
            LogI("updateCaptureImage ignore.");
            return;
        }
        if (mScanCaptureImageShow) {
            mScanImage.setVisibility(View.VISIBLE);
        } else {
            mScanImage.setVisibility(View.INVISIBLE);
        }
    }

    private void scanSettingsUpdate() {
        LogD("scanSettingsUpdate");
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mScanSettingsFragment.setScanManagerDemo(this);
        ft.replace(R.id.fl, mScanSettingsFragment);
        ft.commit();
        mScanSettingsView = true;
        mHome.setVisibility(View.GONE);

        mScanBarcodeSettingsMenuBarcodeList = false;
        mScanBarcodeSettingsMenuBarcode = false;
        mScanSettingsMenuBarcode.setVisibility(View.GONE);
        mScanSettingsMenuBarcodeList.setVisibility(View.GONE);

        if (settings != null) {
            settings.setVisible(false);
        }
        mFlagment.setVisibility(View.VISIBLE);
    }

    /**
     * helper
     */
    private void scanSettingsBarcodeList() {
        LogD("scanSettingsBarcodeList");
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mSettingsBarcodeList = new SettingsBarcodeList();
        mSettingsBarcodeList.setScanManagerDemo(this);
        ft.replace(R.id.flagment_menu_barcode_list, mSettingsBarcodeList);
        ft.commit();
        mScanSettingsView = true;
        mScanBarcodeSettingsMenuBarcodeList = true;
        mScanBarcodeSettingsMenuBarcode = false;
        mFlagment.setVisibility(View.GONE);
        mHome.setVisibility(View.GONE);
        mScanSettingsMenuBarcode.setVisibility(View.GONE);
        mScanSettingsMenuBarcodeList.setVisibility(View.VISIBLE);
        if (settings != null) {
            settings.setVisible(false);
        }
    }

    /**
     * helper
     */
    private void updateScanSettingsBarcode(String key) {
        mScanSettingsView = true;
        mScanBarcodeSettingsMenuBarcodeList = true;
        mScanBarcodeSettingsMenuBarcode = true;
        android.util.Log.d(TAG, "updateScanSettingsBarcode , key:" + key);
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        android.util.Log.d(TAG, "updateScanSettingsBarcode , isEmpty:");
        mScanSettingsBarcode = new ScanSettingsBarcode();
        mScanSettingsBarcode.setScanManagerDemo(this, key);
        ft.replace(R.id.flagment_menu_barcode, mScanSettingsBarcode);
        ft.commit();
        mHome.setVisibility(View.GONE);
        mFlagment.setVisibility(View.GONE);
        mScanSettingsMenuBarcodeList.setVisibility(View.GONE);
        mScanSettingsMenuBarcode.setVisibility(View.VISIBLE);
        if (settings != null) {
            settings.setVisible(false);
        }
    }

    private int getDecodeIntShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int value = sharedPrefs.getInt(key, 1);
        return value;
    }

    /**
     * Attribute helper
     *
     * @param key
     * @param value
     */
    private void updateIntShared(String key, int value) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateIntShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (value == getDecodeIntShared(key)) {
            LogI("updateIntShared ,ignore key:" + key + " update.");
            return;
        }
        Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
        editor.commit();
    }

    private String getDecodeStringShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPrefs.getString(key, "");
        return value;
    }

    /**
     * Attribute helper
     *
     * @param key
     * @param value
     */
    private void updateStringShared(String key, String value) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateStringShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (value == getDecodeStringShared(key) || "".equals(value.trim())) {
            LogI("updateStringShared ,ignore key:" + key + " update.");
            return;
        }
        Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    private boolean getDecodeScanShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enable = sharedPrefs.getBoolean(key, true);
        return enable;
    }

    /**
     * Attribute helper
     *
     * @param key
     * @param enable
     */
    private void updateScanShared(String key, boolean enable) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateScanShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (enable == getDecodeScanShared(key)) {
            LogI("updateScanShared ,ignore key:" + key + " update.");
            return;
        }
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(key, enable);
        editor.apply();
        editor.commit();
    }

    private void initScan() {
        mScanManager = new ScanManager();
        boolean powerOn = mScanManager.getScannerState();
        if (!powerOn) {
            powerOn = mScanManager.openScanner();
            if (!powerOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Scanner cannot be turned on!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
        initBarcodeParameters();
    }

    /**
     * ScanManager.getTriggerMode
     *
     * @return
     */
    private Triggering getTriggerMode() {
        Triggering mode = mScanManager.getTriggerMode();
        return mode;
    }

    /**
     * ScanManager.setTriggerMode
     *
     * @param mode value : Triggering.HOST, Triggering.CONTINUOUS, or Triggering.PULSE.
     */
    private void setTrigger(Triggering mode) {
        Triggering currentMode = getTriggerMode();
        LogD("setTrigger , mode;" + mode + ",currentMode:" + currentMode);
        if (mode != currentMode) {
            mScanManager.setTriggerMode(mode);
            if (mode == Triggering.HOST) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_HOST);
            } else if (mode == Triggering.CONTINUOUS) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_CONTINUOUS;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_CONTINUOUS);
            } else if (mode == Triggering.PULSE) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_PAUSE;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_PAUSE);
            }
        } else {
            LogI("setTrigger , ignore update Trigger mode:" + mode);
        }
    }

    /**
     * ScanManager.getOutputMode
     *
     * @return
     */
    private int getScanOutputMode() {
        int mode = mScanManager.getOutputMode();
        return mode;
    }

    /**
     * ScanManager.switchOutputMode
     *
     * @param mode
     */
    private void setScanOutputMode(int mode) {
        int currentMode = getScanOutputMode();
        if (mode != currentMode && (mode == DECODE_OUTPUT_MODE_FOCUS ||
                mode == DECODE_OUTPUT_MODE_INTENT)) {
            mScanManager.switchOutputMode(mode);
            if (mode == DECODE_OUTPUT_MODE_FOCUS) {
                DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS;
                updateIntShared(DECODE_OUTPUT_MODE, DECODE_OUTPUT_MODE_FOCUS);
            } else if (mode == DECODE_OUTPUT_MODE_INTENT) {
                DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_INTENT;
                updateIntShared(DECODE_OUTPUT_MODE, DECODE_OUTPUT_MODE_INTENT);
            }
        } else {
            LogI("setScanOutputMode , ignore update Output mode:" + mode);
        }
    }

    private void resetScanner() {
        showResetDialog();
    }

    /**
     * ScanManager.getTriggerLockState
     *
     * @return
     */
    private boolean getlockTriggerState() {
        boolean state = mScanManager.getTriggerLockState();
        return state;
    }

    /**
     * ScanManager.lockTrigger and ScanManager.unlockTrigger
     *
     * @param state value ture or false
     */
    private void updateLockTriggerState(boolean state) {
        boolean currentState = getlockTriggerState();
        if (state != currentState) {
            if (state) {
                mScanManager.lockTrigger();
            } else {
                mScanManager.unlockTrigger();
            }
        } else {
            LogI("updateLockTriggerState , ignore update lockTrigger state:" + state);
        }
    }

    /**
     * ScanManager.startDecode
     */
    private void startDecode() {
        if (!mScanEnable) {
            LogI("startDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        boolean lockState = getlockTriggerState();
        if (lockState) {
            LogI("startDecode ignore, Scan lockTrigger state:" + lockState);
            return;
        }
        if (mScanManager != null) {
            mScanManager.startDecode();
        }
    }

    /**
     * ScanManager.stopDecode
     */
    private void stopDecode() {
        if (!mScanEnable) {
            LogI("stopDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
    }

    /**
     * ScanManager.closeScanner
     *
     * @return
     */
    private boolean closeScanner() {
        boolean state = false;
        if (mScanManager != null) {
            mScanManager.stopDecode();
            state = mScanManager.closeScanner();
        }
        return state;
    }

    /**
     * Obtain an instance of BarCodeReader with ScanManager
     * ScanManager.getScannerState
     * ScanManager.openScanner
     * ScanManager.enableAllSymbologies
     *
     * @return
     */
    private boolean openScanner() {
        mScanManager = new ScanManager();
        boolean powerOn = mScanManager.getScannerState();
        if (!powerOn) {
            powerOn = mScanManager.openScanner();
            if (!powerOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Scanner cannot be turned on!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
        mScanManager.enableAllSymbologies(true);   // or execute enableSymbologyDemo() || enableSymbologyDemo2() is the same.
        setTrigger(getTriggerMode());
        setScanOutputMode(getScanOutputMode());
        return powerOn;
    }

    /**
     * ScanManager.enableSymbology
     *
     * @param symbology
     * @param enable
     * @return
     */
    private boolean enableSymbology(Symbology symbology, boolean enable) {
        boolean result = false;
        boolean isSupportBarcode = mScanManager.isSymbologySupported(symbology);
        if (isSupportBarcode) {
            boolean isEnableBarcode = mScanManager.isSymbologyEnabled(symbology);
            if (!isEnableBarcode) {
                mScanManager.enableSymbology(symbology, enable);
                result = true;
            } else {
                result = isEnableBarcode;
                LogI("enableSymbology , ignore " + symbology + " barcode is enable.");
            }
        } else {
            LogI("enableSymbology , ignore " + symbology + " barcode not Support.");
        }
        return result;
    }

    /**
     * ScanManager.getParameterInts
     *
     * @param ids
     * @return
     */
    private int[] getParameterInts(int[] ids) {
        return mScanManager.getParameterInts(ids);
    }

    /**
     * ScanManager.setParameterInts
     *
     * @param ids
     * @param values
     */
    private void setParameterInts(int[] ids, int[] values) {
        mScanManager.setParameterInts(ids, values);
    }

    /**
     * ScanManager.getParameterString
     *
     * @param ids
     * @return
     */
    private String[] getParameterString(int[] ids) {
        return mScanManager.getParameterString(ids);
    }

    /**
     * ScanManager.setParameterString
     *
     * @param ids
     * @param values
     * @return
     */
    private boolean setParameterString(int[] ids, String[] values) {
        return mScanManager.setParameterString(ids, values);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan_manager_demo);
        initView();
    }

    @Override
    public void onBackPressed() {
        if (mScanBarcodeSettingsMenuBarcode) {
            mScanBarcodeSettingsMenuBarcode = false;
            mHome.setVisibility(View.GONE);
            mFlagment.setVisibility(View.GONE);
            mScanSettingsMenuBarcode.setVisibility(View.GONE);
            mScanSettingsMenuBarcodeList.setVisibility(View.VISIBLE);
            if (settings != null) {
                settings.setVisible(false);
            }
        } else if (mScanBarcodeSettingsMenuBarcodeList) {
            mScanBarcodeSettingsMenuBarcodeList = false;
            mHome.setVisibility(View.GONE);
            mFlagment.setVisibility(View.VISIBLE);
            mScanSettingsMenuBarcode.setVisibility(View.GONE);
            mScanSettingsMenuBarcodeList.setVisibility(View.GONE);
            if (settings != null) {
                settings.setVisible(false);
            }
        } else if (mScanSettingsView) {
            mHome.setVisibility(View.VISIBLE);
            mFlagment.setVisibility(View.GONE);
            if (settings != null) {
                settings.setVisible(true);
            }
            mScanSettingsView = false;
        } else {
            super.onBackPressed();
        }
        LogI("onBackPressed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        registerReceiver(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initScan();
        registerReceiver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogD("onKeyUp, keyCode:" + keyCode);
        if (keyCode >= SCAN_KEYCODE[0] && keyCode <= SCAN_KEYCODE[SCAN_KEYCODE.length - 1]) {
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogD("onKeyDown, keyCode:" + keyCode);
        if (keyCode >= SCAN_KEYCODE[0] && keyCode <= SCAN_KEYCODE[SCAN_KEYCODE.length - 1]) {
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Top right corner setting button
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        settings = menu.add(0, 1, 0, R.string.scan_settings).setIcon(R.drawable.ic_action_settings);
        settings.setShowAsAction(1);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Set the button monitor in the upper right corner
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogD("onOptionsItemSelected, item:" + item.getItemId());
        switch (item.getItemId()) {
            case 1:
                scanSettingsUpdate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void LogD(String msg) {
        if (DEBUG) {
            android.util.Log.d(TAG, msg);
        }
    }

    private void LogI(String msg) {
        android.util.Log.i(TAG, msg);
    }

    /**
     * Reset Auxiliary dialog
     */
    private void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.scan_reset_def_alert);
        builder.setTitle(R.string.scan_reset_def);
        builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ResetAsyncTask task = new ResetAsyncTask(ScanManagerDemo.this);
                task.execute("reset");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Perform reset operation class
     */
    class ResetAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pdialog;

        public ResetAsyncTask(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pdialog = new ProgressDialog(mContext);
            pdialog.setMessage(mContext.getResources().getString(R.string.scan_reset_progress));
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.setCancelable(false);
            pdialog.show();
        }

        /**
         * ScanManager.resetScannerParameters
         *
         * @param params
         * @return
         */
        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                mScanManager.resetScannerParameters();
                setScanOutputMode(DECODE_OUTPUT_MODE_FOCUS);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (pdialog != null) pdialog.dismiss();
            mScanSettingsFragment.resetScan();
            Toast.makeText(mContext,
                    R.string.scanner_toast, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * mBarcodeMap helper
     */
    private void initBarcodeParameters() {
        mBarcodeMap.clear();
        BarcodeHolder holder = new BarcodeHolder();
        // Symbology.AZTEC
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.AZTEC_ENABLE};
        holder.mParaKeys = new String[]{"AZTEC_ENABLE"};
        mBarcodeMap.put(Symbology.AZTEC + "", holder);
        // Symbology.CHINESE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.C25_ENABLE};
        holder.mParaKeys = new String[]{"C25_ENABLE"};
        mBarcodeMap.put(Symbology.CHINESE25 + "", holder);
        // Symbology.CODABAR
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeNOTIS = new CheckBoxPreference(this);
        holder.mBarcodeCLSI = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODABAR_ENABLE, PropertyID.CODABAR_LENGTH1, PropertyID.CODABAR_LENGTH2, PropertyID.CODABAR_NOTIS, PropertyID.CODABAR_CLSI};
        holder.mParaKeys = new String[]{"CODABAR_ENABLE", "CODABAR_LENGTH1", "CODABAR_LENGTH2", "CODABAR_NOTIS", "CODABAR_CLSI"};
        mBarcodeMap.put(Symbology.CODABAR + "", holder);
        // Symbology.CODE11
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeCheckDigit = new ListPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE11_ENABLE, PropertyID.CODE11_LENGTH1, PropertyID.CODE11_LENGTH2, PropertyID.CODE11_SEND_CHECK};
        holder.mParaKeys = new String[]{"CODE11_ENABLE", "CODE11_LENGTH1", "CODE11_LENGTH2", "CODE11_SEND_CHECK"};
        mBarcodeMap.put(Symbology.CODE11 + "", holder);
        // Symbology.CODE32
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE32_ENABLE};
        holder.mParaKeys = new String[]{"CODE32_ENABLE"};
        mBarcodeMap.put(Symbology.CODE32 + "", holder);
        // Symbology.CODE39
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mBarcodeFullASCII = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE39_ENABLE, PropertyID.CODE39_LENGTH1, PropertyID.CODE39_LENGTH2, PropertyID.CODE39_ENABLE_CHECK, PropertyID.CODE39_SEND_CHECK, PropertyID.CODE39_FULL_ASCII};
        holder.mParaKeys = new String[]{"CODE39_ENABLE", "CODE39_LENGTH1", "CODE39_LENGTH2", "CODE39_ENABLE_CHECK", "CODE39_SEND_CHECK", "CODE39_FULL_ASCII"};
        mBarcodeMap.put(Symbology.CODE39 + "", holder);
        // Symbology.CODE93
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE93_ENABLE, PropertyID.CODE93_LENGTH1, PropertyID.CODE93_LENGTH2};
        holder.mParaKeys = new String[]{"CODE93_ENABLE", "CODE93_LENGTH1", "CODE93_LENGTH2"};
        mBarcodeMap.put(Symbology.CODE93 + "", holder);
        // Symbology.CODE128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeISBT = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE128_ENABLE, PropertyID.CODE128_LENGTH1, PropertyID.CODE128_LENGTH2, PropertyID.CODE128_CHECK_ISBT_TABLE};
        holder.mParaKeys = new String[]{"CODE128_ENABLE", "CODE128_LENGTH1", "CODE128_LENGTH2", "CODE128_CHECK_ISBT_TABLE"};
        mBarcodeMap.put(Symbology.CODE128 + "", holder);
        // Symbology.COMPOSITE_CC_AB
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.COMPOSITE_CC_AB_ENABLE};
        holder.mParaKeys = new String[]{"COMPOSITE_CC_AB_ENABLE"};
        mBarcodeMap.put(Symbology.COMPOSITE_CC_AB + "", holder);
        // Symbology.COMPOSITE_CC_C
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.COMPOSITE_CC_C_ENABLE};
        holder.mParaKeys = new String[]{"COMPOSITE_CC_C_ENABLE"};
        mBarcodeMap.put(Symbology.COMPOSITE_CC_C + "", holder);
        // Symbology.DATAMATRIX
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.DATAMATRIX_ENABLE};
        holder.mParaKeys = new String[]{"DATAMATRIX_ENABLE"};
        mBarcodeMap.put(Symbology.DATAMATRIX + "", holder);
        // Symbology.DISCRETE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.D25_ENABLE};
        holder.mParaKeys = new String[]{"D25_ENABLE"};
        mBarcodeMap.put(Symbology.DISCRETE25 + "", holder);
        // Symbology.EAN8
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.EAN8_ENABLE};
        holder.mParaKeys = new String[]{"EAN8_ENABLE"};
        mBarcodeMap.put(Symbology.EAN8 + "", holder);
        // Symbology.EAN13
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeBookland = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.EAN13_ENABLE, PropertyID.EAN13_BOOKLANDEAN};
        holder.mParaKeys = new String[]{"EAN13_ENABLE", "EAN13_BOOKLANDEAN"};
        mBarcodeMap.put(Symbology.EAN13 + "", holder);
        // Symbology.GS1_14
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_14_ENABLE};
        holder.mParaKeys = new String[]{"GS1_14_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_14 + "", holder);
        // Symbology.GS1_128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE128_GS1_ENABLE};
        holder.mParaKeys = new String[]{"CODE128_GS1_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_128 + "", holder);
        // Symbology.GS1_EXP
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_EXP_ENABLE, PropertyID.GS1_EXP_LENGTH1, PropertyID.GS1_EXP_LENGTH2};
        holder.mParaKeys = new String[]{"GS1_EXP_ENABLE", "GS1_EXP_LENGTH1", "GS1_EXP_LENGTH2"};
        mBarcodeMap.put(Symbology.GS1_EXP + "", holder);
        // Symbology.GS1_LIMIT
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_LIMIT_ENABLE};
        holder.mParaKeys = new String[]{"GS1_LIMIT_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_LIMIT + "", holder);
        // Symbology.INTERLEAVED25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.I25_ENABLE, PropertyID.I25_LENGTH1, PropertyID.I25_LENGTH2, PropertyID.I25_ENABLE_CHECK, PropertyID.I25_SEND_CHECK};
        holder.mParaKeys = new String[]{"I25_ENABLE", "I25_LENGTH1", "I25_LENGTH2", "I25_ENABLE_CHECK", "I25_SEND_CHECK"};
        mBarcodeMap.put(Symbology.INTERLEAVED25 + "", holder);
        // Symbology.MATRIX25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.M25_ENABLE};
        holder.mParaKeys = new String[]{"M25_ENABLE"};
        mBarcodeMap.put(Symbology.MATRIX25 + "", holder);
        // Symbology.MAXICODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MAXICODE_ENABLE};
        holder.mParaKeys = new String[]{"MAXICODE_ENABLE"};
        mBarcodeMap.put(Symbology.MAXICODE + "", holder);
        // Symbology.MICROPDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MICROPDF417_ENABLE};
        holder.mParaKeys = new String[]{"MICROPDF417_ENABLE"};
        mBarcodeMap.put(Symbology.MICROPDF417 + "", holder);
        // Symbology.MSI
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeSecondChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mBarcodeSecondChecksumMode = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MSI_ENABLE, PropertyID.MSI_LENGTH1, PropertyID.MSI_LENGTH2, PropertyID.MSI_REQUIRE_2_CHECK, PropertyID.MSI_SEND_CHECK, PropertyID.MSI_CHECK_2_MOD_11};
        holder.mParaKeys = new String[]{"MSI_ENABLE", "MSI_LENGTH1", "MSI_LENGTH2", "MSI_REQUIRE_2_CHECK", "MSI_SEND_CHECK", "MSI_CHECK_2_MOD_11"};
        mBarcodeMap.put(Symbology.MSI + "", holder);
        // Symbology.PDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.PDF417_ENABLE};
        holder.mParaKeys = new String[]{"PDF417_ENABLE"};
        mBarcodeMap.put(Symbology.PDF417 + "", holder);
        // Symbology.QRCODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.QRCODE_ENABLE};
        holder.mParaKeys = new String[]{"QRCODE_ENABLE"};
        mBarcodeMap.put(Symbology.QRCODE + "", holder);
        // Symbology.TRIOPTIC
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.TRIOPTIC_ENABLE};
        holder.mParaKeys = new String[]{"TRIOPTIC_ENABLE"};
        mBarcodeMap.put(Symbology.TRIOPTIC + "", holder);
        // Symbology.UPCA
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(this);
        holder.mBarcodeConvertEAN13 = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCA_ENABLE, PropertyID.UPCA_SEND_CHECK, PropertyID.UPCA_SEND_SYS, PropertyID.UPCA_TO_EAN13};
        holder.mParaKeys = new String[]{"UPCA_ENABLE", "UPCA_SEND_CHECK", "UPCA_SEND_SYS", "UPCA_TO_EAN13"};
        mBarcodeMap.put(Symbology.UPCA + "", holder);
        // Symbology.UPCE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(this);
        holder.mBarcodeConvertUPCA = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCE_ENABLE, PropertyID.UPCE_SEND_CHECK, PropertyID.UPCE_SEND_SYS, PropertyID.UPCE_TO_UPCA};
        holder.mParaKeys = new String[]{"UPCE_ENABLE", "UPCE_SEND_CHECK", "UPCE_SEND_SYS", "UPCE_TO_UPCA"};
        mBarcodeMap.put(Symbology.UPCE + "", holder);
        // Symbology.UPCE1
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCE1_ENABLE};
        holder.mParaKeys = new String[]{"UPCE1_ENABLE"};
        mBarcodeMap.put(Symbology.UPCE1 + "", holder);
    }


    /**
     * ScanSettingsBarcode helper
     */
    public static class ScanSettingsBarcode extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private PreferenceScreen root = null;
        private ScanManagerDemo mScanDemo = null;
        private String mBarcodeKey = null;
        CharSequence[] entries = new CharSequence[]{"Two check digits", "One check digits", "Two check digits and stripped", "One check digits and stripped"};
        CharSequence[] entryValues = new CharSequence[]{"0", "1", "2", "3"};

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.scan_settings_pro);
            android.util.Log.d(TAG, "onCreate , Barcode ,root:" + root);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            android.util.Log.d(TAG, "onDestroyView , Barcode ");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            root = this.getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            android.util.Log.d(TAG, "onDestroy , Barcode ,root:" + root);
        }

        @Override
        public void onStart() {
            super.onStart();
            android.util.Log.d(TAG, "onStart , Barcode ");
        }

        @Override
        public void onResume() {
            android.util.Log.d(TAG, "onResume , Barcode ");
            super.onResume();
            initSymbology();
        }

        @Override
        public void onStop() {
            super.onStop();
            android.util.Log.d(TAG, "onStop , Barcode ");
        }

        /**
         * Use mBarcodeMap, key:Symbology enums toString , value:BarcodeHolder class
         */
        private void initSymbology() {
            android.util.Log.d(TAG, "initSymbology , Barcode mBarcodeKey:" + mBarcodeKey);
            if (mBarcodeKey != null) {
                BarcodeHolder barcodeHolder = mBarcodeMap.get(mBarcodeKey);
                if (barcodeHolder != null) {
                    // barcodeHolder.mParaIds are PropertyID Attributes , Example: PropertyID.QRCODE_ENABLE/PropertyID.EAN13_ENABLE/PropertyID.CODE128_ENABLE etc.
                    int[] values = mScanDemo.getParameterInts(barcodeHolder.mParaIds);
                    int valuesLength = 0;
                    if (values != null) {
                        valuesLength = values.length;
                    }
                    if (barcodeHolder.mBarcodeEnable == null || valuesLength <= 0) {
                        android.util.Log.d(TAG, "initSymbology , ignore barcode enable:" + barcodeHolder.mBarcodeEnable + ",para value:" + valuesLength);
                        return;
                    }
                    int indexCount = valuesLength;
                    android.util.Log.d(TAG, "initSymbology , Barcode initSymbology ,indexCount:" + indexCount);
                    if (barcodeHolder.mBarcodeEnable != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeEnable.setTitle(mBarcodeKey);
                        barcodeHolder.mBarcodeEnable.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeEnable.setSummary(mBarcodeKey);
                        barcodeHolder.mBarcodeEnable.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeEnable.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeEnable);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeLength1 != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeLength1.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeLength1.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeLength1.setSummary(values[valuesLength - indexCount] + "");
                        barcodeHolder.mBarcodeLength1.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeLength1);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeLength2 != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeLength2.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeLength2.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeLength2.setSummary(values[valuesLength - indexCount] + "");
                        barcodeHolder.mBarcodeLength2.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeLength2);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeNOTIS != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeNOTIS.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeNOTIS.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeNOTIS.setSummary(mBarcodeKey + " NOTIS");
                        barcodeHolder.mBarcodeNOTIS.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeNOTIS.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeNOTIS);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeCLSI != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeCLSI.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeCLSI.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeCLSI.setSummary(mBarcodeKey + " CLSI");
                        barcodeHolder.mBarcodeCLSI.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeCLSI.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeCLSI);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeISBT != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeISBT.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeISBT.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeISBT.setSummary(mBarcodeKey + " CLSI128");
                        barcodeHolder.mBarcodeISBT.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeISBT.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeISBT);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeChecksum != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeChecksum.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeChecksum.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeChecksum.setSummary(mBarcodeKey + " Checksum");
                        barcodeHolder.mBarcodeChecksum.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeChecksum.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeChecksum);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeSendCheck != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeSendCheck.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSendCheck.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSendCheck.setSummary(mBarcodeKey + " SendCheck");
                        barcodeHolder.mBarcodeSendCheck.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeSendCheck.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeSendCheck);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeFullASCII != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeFullASCII.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeFullASCII.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeFullASCII.setSummary(mBarcodeKey + " Full ASCII");
                        barcodeHolder.mBarcodeFullASCII.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeFullASCII.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeFullASCII);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeCheckDigit != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeCheckDigit.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeCheckDigit.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeCheckDigit.setEntries(entries);
                        barcodeHolder.mBarcodeCheckDigit.setEntryValues(entryValues);
                        barcodeHolder.mBarcodeCheckDigit.setValue(entryValues[values[valuesLength - indexCount]].toString());
                        barcodeHolder.mBarcodeCheckDigit.setSummary(entries[values[valuesLength - indexCount]]);
                        barcodeHolder.mBarcodeCheckDigit.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeCheckDigit);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeBookland != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeBookland.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeBookland.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeBookland.setSummary(mBarcodeKey + " Bookland");
                        barcodeHolder.mBarcodeBookland.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeBookland.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeBookland);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeSecondChecksum != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeSecondChecksum.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSecondChecksum.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSecondChecksum.setSummary(mBarcodeKey + " Second Checksum");
                        barcodeHolder.mBarcodeSecondChecksum.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeSecondChecksum.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeSecondChecksum);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeSecondChecksumMode != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeSecondChecksumMode.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSecondChecksumMode.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSecondChecksumMode.setSummary(mBarcodeKey + " Second Checksum Mode 11");
                        barcodeHolder.mBarcodeSecondChecksumMode.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeSecondChecksumMode.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeSecondChecksumMode);
                        indexCount--;
                    }
                    // PostalCode
                    /*if(barcodeHolder.mBarcodePostalCode!=null && (indexCount > 0)) {
                        barcodeHolder.mBarcodePostalCode.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodePostalCode.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodePostalCode.setSummary(values[valuesLength - indexCount]);
                        barcodeHolder.mBarcodePostalCode.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodePostalCode);
                        indexCount--;
                    }*/
                    if (barcodeHolder.mBarcodeSystemDigit != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeSystemDigit.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSystemDigit.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeSystemDigit.setSummary(mBarcodeKey + " System Digit");
                        barcodeHolder.mBarcodeSystemDigit.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeSystemDigit.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeSystemDigit);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeConvertEAN13 != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeConvertEAN13.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeConvertEAN13.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeConvertEAN13.setSummary(mBarcodeKey + " Convert to EAN13");
                        barcodeHolder.mBarcodeConvertEAN13.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeConvertEAN13.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeConvertEAN13);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeConvertUPCA != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeConvertUPCA.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeConvertUPCA.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeConvertUPCA.setSummary(mBarcodeKey + " Convert to UPCA");
                        barcodeHolder.mBarcodeConvertUPCA.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeConvertUPCA.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeConvertUPCA);
                        indexCount--;
                    }
                    if (barcodeHolder.mBarcodeEanble25DigitExtensions != null && (indexCount > 0)) {
                        barcodeHolder.mBarcodeEanble25DigitExtensions.setTitle(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeEanble25DigitExtensions.setKey(barcodeHolder.mParaKeys[valuesLength - indexCount]);
                        barcodeHolder.mBarcodeEanble25DigitExtensions.setSummary(mBarcodeKey + " Enable 2-5 Digit Extensions");
                        barcodeHolder.mBarcodeEanble25DigitExtensions.setChecked(values[valuesLength - indexCount] == 1);
                        barcodeHolder.mBarcodeEanble25DigitExtensions.setOnPreferenceChangeListener(this);
                        this.getPreferenceScreen().addPreference(barcodeHolder.mBarcodeEanble25DigitExtensions);
                        indexCount--;
                    }
                }
            }
        }

        public void setScanManagerDemo(ScanManagerDemo demo, String key) {
            mScanDemo = demo;
            mBarcodeKey = key;
        }

        /**
         * helper
         */
        private void updateParameter(String key, Object obj) {
            if (mBarcodeKey != null) {
                BarcodeHolder barcodeHolder = mBarcodeMap.get(mBarcodeKey);
                if (barcodeHolder != null) {
                    if (barcodeHolder.mParaKeys != null && barcodeHolder.mParaKeys.length > 0) {
                        int index = 0;
                        for (int i = 0; i < barcodeHolder.mParaKeys.length; i++) {
                            if (key.equals(barcodeHolder.mParaKeys[i])) {
                                android.util.Log.d(TAG, "onPreferenceChange , index:" + index + ",key:" + key);
                                break;
                            }
                            index++;
                        }
                        int[] idBuff = new int[]{barcodeHolder.mParaIds[index]};
                        int[] valueBuff = new int[1];
                        if (obj instanceof Boolean) {
                            boolean value = (boolean) obj;
                            valueBuff[0] = value ? 1 : 0;
                        } else if (obj instanceof String) {
                            int value = Integer.valueOf((String) obj);
                            valueBuff[0] = value;
                        }
                        mScanDemo.setParameterInts(idBuff, valueBuff);
                    }
                }
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            String key = preference.getKey();
            android.util.Log.d(TAG, "onPreferenceTreeClick preference:" + preference + ",key:" + key);
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
                if (editTextPreference != null) {
                    editTextPreference.getEditText().setText(editTextPreference.getSummary());
                }
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            android.util.Log.d(TAG, "onPreferenceChange ,preference:" + preference + ",key:" + key + ",mBarcodeKey:" + mBarcodeKey + ",newValue:" + newValue);
            if (preference instanceof CheckBoxPreference) {
                boolean value = (Boolean) newValue;
                CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(key);
                checkBox.setChecked(value);
                updateParameter(key, newValue);
            } else if (preference instanceof EditTextPreference) {
                String value = (String) newValue;
                EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
                if (editTextPreference != null) {
                    editTextPreference.setSummary(value);
                }
                updateParameter(key, newValue);
            } else if (preference instanceof ListPreference) {
                String value = (String) newValue;
                int val = Integer.parseInt(value);
                ListPreference listPreference = (ListPreference) findPreference(key);
                if (listPreference != null) {
                    listPreference.setValue(value);
                    listPreference.setSummary(entries[val]);
                }
                updateParameter(key, newValue);
                android.util.Log.d(TAG, "onPreferenceChange ------------ preference:ListPreference");
            }
            return false;
        }
    }

    /**
     * SettingsBarcodeList helper
     */
    public static class SettingsBarcodeList extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private PreferenceScreen root = null;
        private Preference mBarcode = null;
        private ScanManagerDemo mScanDemo = null;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            root = this.getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            addPreferencesFromResource(R.xml.scan_settings_pro);
            android.util.Log.d(TAG, "onCreate , ,root:" + root);   //Symbology s = BARCODE_SUPPORT_SYMBOLOGY[9];
            initSymbology();
        }

        /**
         * Use Symbology enumeration
         */
        private void initSymbology() {
            if (mScanDemo != null) {
                int length = BARCODE_SUPPORT_SYMBOLOGY.length;
                android.util.Log.d(TAG, "initSymbology  length : " + length);
                for (int i = 0; i < length; i++) {
                    if (mScanDemo != null && mScanDemo.isSymbologySupported(BARCODE_SUPPORT_SYMBOLOGY[i])) {
                        mBarcode = new Preference(mScanDemo);
                        mBarcode.setTitle(BARCODE_SUPPORT_SYMBOLOGY[i] + "");
                        mBarcode.setKey(BARCODE_SUPPORT_SYMBOLOGY[i] + "");
                        this.getPreferenceScreen().addPreference(mBarcode);
                    } else {
                        android.util.Log.d(TAG, "initSymbology , Not Support Barcode " + BARCODE_SUPPORT_SYMBOLOGY[i]);
                    }
                }
            }
        }

        public void setScanManagerDemo(ScanManagerDemo demo) {
            mScanDemo = demo;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            android.util.Log.d(TAG, "onPreferenceTreeClick preference:" + preference);
            String key = preference.getKey();
            if (key != null) {
                mScanDemo.updateScanSettingsBarcode(key);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    }

    private boolean isSymbologySupported(Symbology symbology) {
        boolean isSupport = false;
        if (mScanManager != null) {
            isSupport = mScanManager.isSymbologySupported(symbology);
        }
        return isSupport;
    }


    /**
     * Menu helper
     */
    public static class ScanSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        private static final String SCAN_SERVICE_ONOFF = "scan_turnon_switch";
        private static final String SCAN_TRIGGER_LOCK = "scan_trigger_lock";
        private static final String SCAN_TRIGGER_MODE = "scan_trigger_mode";
        private static final String SCAN_RESET = "scan_reset_def";
        private static final String SCAN_OUTPUT_MODE = "scan_output_mode";
        private static final String SCAN_KEYBOARD_OUTPUT_TYPE = "scan_keyboard_output_type";
        private static final String SCAN_SOUND_MODE = "scan_sound_mode";
        private static final String SCAN_INTENT_ACTION = "scan_intent_action";
        private static final String SCAN_INTENT_LABEL = "scan_intent_stringlabel";
        private static final String SCAN_INTENT_BARCODE_TYPE = "scan_intent_barcode_type";
        private static final String SCAN_CAPTURE_IMAGE = "scan_cupture_image";
        private static final String SCAN_BARCODE_SYMBOLOGY_LIST_KEY = "scan_barcode_symbology_list";

        private static final String SCAN_PULSE = "0";
        private static final String SCAN_CONTINUOUS = "1";
        private static final String SCAN_HOST = "2";

        private PreferenceScreen root = null;
        private SwitchPreference mScanServiceOnOff = null;
        private CheckBoxPreference mCheckBoxScanTriggerLock = null;
        private CheckBoxPreference mCheckBoxScanCaptureImage = null;
        private ListPreference mScanTriggerMode = null;
        private ListPreference mScanOutputMode = null;
        private ListPreference mScanKeyboardOutputType = null;
        private Preference mScanReset = null;
        private ListPreference mScanSendSounds = null;
        private EditTextPreference mIntentAction = null;
        private EditTextPreference mIntentLabel = null;
        private EditTextPreference mIntentBarcodeType = null;
        private Preference mBarcodeSymbologyList = null;
        private ScanManagerDemo mScanDemo = null;

        public void setScanManagerDemo(ScanManagerDemo demo) {
            mScanDemo = demo;
        }

        public void resetScan() {
            mScanServiceOnOff.setChecked(true);
            mScanEnable = true;
            android.util.Log.d(TAG, "resetScan , :");
            mScanDemo.updateScanShared(DECODE_ENABLE, true);
            mScanDemo.openScanner();

            mCheckBoxScanTriggerLock.setChecked(false);
            mScanDemo.updateLockTriggerState(false);

            mScanTriggerMode.setValue(SCAN_HOST);
            mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
            mScanDemo.setTrigger(Triggering.HOST);

            mScanOutputMode.setValue(DECODE_OUTPUT_MODE_FOCUS + "");
            mScanOutputMode.setSummary(mScanOutputMode.getEntry());
            mScanDemo.setScanOutputMode(DECODE_OUTPUT_MODE_FOCUS);

            int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE};
            int[] value = mScanDemo.getParameterInts(id);
            mScanKeyboardOutputType.setValue(value[0] + "");
            mScanKeyboardOutputType.setSummary(mScanKeyboardOutputType.getEntry());

            id = new int[]{PropertyID.GOOD_READ_BEEP_ENABLE};
            int[] valueBuff = new int[]{1};
            mScanDemo.setParameterInts(id, valueBuff);
            mScanSendSounds.setValue(valueBuff[0] + "");
            mScanSendSounds.setSummary(mScanSendSounds.getEntry());

            id = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG, PropertyID.WEDGE_INTENT_LABEL_TYPE_TAG};
            String[] valueBuffStr = new String[]{"android.intent.ACTION_DECODE_DATA", "barcode_string", "barcodeType"};
            mIntentAction.setSummary(valueBuffStr[0]);
            mIntentLabel.setSummary(valueBuffStr[1]);
            mIntentBarcodeType.setSummary(valueBuffStr[2]);
            mScanDemo.setParameterString(id, valueBuffStr);

            mScanCaptureImageShow = false;
            mCheckBoxScanCaptureImage.setChecked(false);
            mScanDemo.updateScanShared(DECODE_CAPTURE_IMAGE_SHOW, false);
            mScanDemo.updateCaptureImage();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            root = this.getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            addPreferencesFromResource(R.xml.scan_settings);
            mScanServiceOnOff = (SwitchPreference) findPreference(SCAN_SERVICE_ONOFF);
            mScanServiceOnOff.setOnPreferenceChangeListener(this);
            mCheckBoxScanTriggerLock = (CheckBoxPreference) findPreference(SCAN_TRIGGER_LOCK);
            mCheckBoxScanTriggerLock.setOnPreferenceChangeListener(this);
            mScanTriggerMode = (ListPreference) findPreference(SCAN_TRIGGER_MODE);
            mScanTriggerMode.setOnPreferenceChangeListener(this);
            mScanOutputMode = (ListPreference) findPreference(SCAN_OUTPUT_MODE);
            mScanOutputMode.setOnPreferenceChangeListener(this);
            mScanKeyboardOutputType = (ListPreference) findPreference(SCAN_KEYBOARD_OUTPUT_TYPE);
            mScanKeyboardOutputType.setOnPreferenceChangeListener(this);
            mScanSendSounds = (ListPreference) findPreference(SCAN_SOUND_MODE);
            mScanSendSounds.setOnPreferenceChangeListener(this);
            mIntentAction = (EditTextPreference) findPreference(SCAN_INTENT_ACTION);
            mIntentAction.setOnPreferenceChangeListener(this);
            mIntentLabel = (EditTextPreference) findPreference(SCAN_INTENT_LABEL);
            mIntentLabel.setOnPreferenceChangeListener(this);
            mIntentBarcodeType = (EditTextPreference) findPreference(SCAN_INTENT_BARCODE_TYPE);
            mIntentBarcodeType.setOnPreferenceChangeListener(this);
            mCheckBoxScanCaptureImage = (CheckBoxPreference) findPreference(SCAN_CAPTURE_IMAGE);
            mCheckBoxScanCaptureImage.setOnPreferenceChangeListener(this);
            mBarcodeSymbologyList = (Preference) findPreference(SCAN_BARCODE_SYMBOLOGY_LIST_KEY);
            mBarcodeSymbologyList.setOnPreferenceChangeListener(this);

            mScanReset = (Preference) findPreference(SCAN_RESET);
            initFlagment();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            android.util.Log.d(TAG, "onPreferenceTreeClick preference:" + preference);
            if (mScanReset == preference) {
                android.util.Log.d(TAG, "onPreferenceTreeClick mScanReset:" + mScanReset);
                mScanDemo.resetScanner();
            } else if (preference == mIntentAction) {
                mIntentAction.getEditText().setText(mIntentAction.getSummary());
            } else if (preference == mIntentLabel) {
                mIntentLabel.getEditText().setText(mIntentLabel.getSummary());
            } else if (preference == mBarcodeSymbologyList) {
                android.util.Log.d(TAG, "onPreferenceChange scanSettingsBarcodeList()");
                mScanDemo.scanSettingsBarcodeList();
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            android.util.Log.d(TAG, "onPreferenceChange preference:" + preference + ",newValue" + newValue + ",key:" + key);
            if (SCAN_SERVICE_ONOFF.equals(key)) {
                boolean value = (Boolean) newValue;
                if (mScanServiceOnOff.isChecked() != value) {
                    mScanServiceOnOff.setChecked(value);
                    mScanEnable = value;
                    android.util.Log.d(TAG, "initView , Switch:" + value);
                    mScanDemo.updateScanShared(DECODE_ENABLE, value);
                    if (value) {
                        mScanDemo.openScanner();
                    } else {
                        mScanDemo.closeScanner();
                    }
                }
                android.util.Log.d(TAG, "onPreferenceChange mScanServiceOnOff preference:" + preference + ",newValue:" + newValue + ",value:" + value);
            } else if (SCAN_TRIGGER_LOCK.equals(key)) {
                boolean value = (Boolean) newValue;
                android.util.Log.d(TAG, "onPreferenceChange mScanServiceOnOff preference:" + preference + ",newValue:" + newValue);
                mCheckBoxScanTriggerLock.setChecked(value);
                mScanDemo.updateLockTriggerState(value);
            } else if (SCAN_TRIGGER_MODE.equals(key)) {
                String mode = (String) newValue;
                android.util.Log.d(TAG, "onPreferenceChange mScanServiceOnOff2 preference:" + preference + ",newValue:" + newValue);
                if (SCAN_HOST.equals(mode)) {
                    mScanTriggerMode.setValue(SCAN_HOST);
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
                    mScanDemo.setTrigger(Triggering.HOST);
                } else if (SCAN_CONTINUOUS.equals(mode)) {
                    mScanTriggerMode.setValue(SCAN_CONTINUOUS);
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
                    mScanDemo.setTrigger(Triggering.CONTINUOUS);
                } else if (SCAN_PULSE.equals(mode)) {
                    mScanTriggerMode.setValue(SCAN_PULSE);
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
                    mScanDemo.setTrigger(Triggering.PULSE);
                }
            } else if (SCAN_OUTPUT_MODE.equals(key)) {
                String mode = (String) newValue;
                int outputMode = mode != null ? Integer.valueOf(mode) : DECODE_OUTPUT_MODE_FOCUS;
                android.util.Log.d(TAG, "onPreferenceChange SCAN_OUTPUT_MODE preference:" + preference + ",mode:" + mode);
                mScanOutputMode.setValue(mode);
                mScanOutputMode.setSummary(mScanOutputMode.getEntry());
                mScanDemo.setScanOutputMode(outputMode);
            } else if (SCAN_KEYBOARD_OUTPUT_TYPE.equals(key)) {
                int value = Integer.valueOf((String) newValue);
                int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE};
                int[] values = mScanDemo.getParameterInts(id);
                if (values[0] != value) {
                    mScanKeyboardOutputType.setValue(value + "");
                    mScanKeyboardOutputType.setSummary(mScanKeyboardOutputType.getEntry());
                    int[] valueBuff = new int[]{value};
                    mScanDemo.setParameterInts(id, valueBuff);
                }
                android.util.Log.d(TAG, "onPreferenceChange SCAN_KEYBOARD_OUTPUT_TYPE value:" + value + ",values[0]:" + values[0]);
            } else if (SCAN_SOUND_MODE.equals(key)) {
                int value = Integer.valueOf((String) newValue);
                int[] id = new int[]{PropertyID.GOOD_READ_BEEP_ENABLE};
                int[] values = mScanDemo.getParameterInts(id);
                if (values[0] != value) {
                    mScanSendSounds.setValue(value + "");
                    mScanSendSounds.setSummary(mScanSendSounds.getEntry());
                    int[] valueBuff = new int[]{value};
                    mScanDemo.setParameterInts(id, valueBuff);
                }
                android.util.Log.d(TAG, "onPreferenceChange SCAN_SOUND_MODE preference:" + preference + ",newValue:" + newValue);
            } else if (SCAN_INTENT_ACTION.equals(key)) {
                String newAction = (String) newValue;
                int[] id = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME};
                String[] value = mScanDemo.getParameterString(id);
                if (newAction != null && !newAction.equals(value[0])) {
                    mIntentAction.setSummary(newAction);
                    String[] valueBuff = new String[]{newAction};
                    mScanDemo.setParameterString(id, valueBuff);
                }
            } else if (SCAN_INTENT_LABEL.equals(key)) {
                String newAction = (String) newValue;
                int[] id = new int[]{PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
                String[] value = mScanDemo.getParameterString(id);
                if (newAction != null && !newAction.equals(value[0])) {
                    mIntentLabel.setSummary(newAction);
                    String[] valueBuff = new String[]{newAction};
                    mScanDemo.setParameterString(id, valueBuff);
                }
            } else if (SCAN_INTENT_BARCODE_TYPE.equals(key)) {
                String newAction = (String) newValue;
                int[] id = new int[]{PropertyID.WEDGE_INTENT_LABEL_TYPE_TAG};
                String[] value = mScanDemo.getParameterString(id);
                if (newAction != null && !newAction.equals(value[0])) {
                    mIntentLabel.setSummary(newAction);
                    String[] valueBuff = new String[]{newAction};
                    mScanDemo.setParameterString(id, valueBuff);
                }
            } else if (SCAN_CAPTURE_IMAGE.equals(key)) {
                boolean value = (Boolean) newValue;
                int outputMode = mScanDemo.getScanOutputMode();
                if (value && outputMode != DECODE_OUTPUT_MODE_INTENT) {
                    Toast.makeText(mScanDemo,
                            R.string.scan_cupture_image_prompt, Toast.LENGTH_LONG).show();
                }
                mScanCaptureImageShow = value;
                android.util.Log.d(TAG, "onPreferenceChange SCAN_CAPTURE_IMAGE preference:" + preference + ",newValue:" + newValue);
                mCheckBoxScanCaptureImage.setChecked(value);
                mScanDemo.updateScanShared(DECODE_CAPTURE_IMAGE_SHOW, value);
                mScanDemo.updateCaptureImage();
            }
            return false;
        }

        private void initFlagment() {
            Triggering mode = mScanDemo.getTriggerMode();
            if (mode == Triggering.HOST) {
                mScanTriggerMode.setValue(SCAN_HOST);
            } else if (mode == Triggering.CONTINUOUS) {
                mScanTriggerMode.setValue(SCAN_CONTINUOUS);
            } else if (mode == Triggering.PULSE) {
                mScanTriggerMode.setValue(SCAN_PULSE);
            }
            mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());

            int outputMode = mScanDemo.getScanOutputMode();
            mScanOutputMode.setValue(outputMode + "");
            mScanOutputMode.setSummary(mScanOutputMode.getEntry());

            int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE};
            int[] valueType = mScanDemo.getParameterInts(id);
            mScanKeyboardOutputType.setValue(valueType[0] + "");
            mScanKeyboardOutputType.setSummary(mScanKeyboardOutputType.getEntry());

            id = new int[]{PropertyID.GOOD_READ_BEEP_ENABLE};
            int[] values = mScanDemo.getParameterInts(id);
            mScanSendSounds.setValue(values[0] + "");
            mScanSendSounds.setSummary(mScanSendSounds.getEntry());

            id = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG, PropertyID.WEDGE_INTENT_LABEL_TYPE_TAG};
            String[] value = mScanDemo.getParameterString(id);
            mIntentAction.setSummary(value[0]);
            mIntentLabel.setSummary(value[1]);
            mIntentBarcodeType.setSummary(value[2]);
            mCheckBoxScanCaptureImage.setChecked(mScanDemo.getDecodeScanShared(DECODE_CAPTURE_IMAGE_SHOW));
        }
    }

    /**
     * BarcodeHolder helper
     */
    static class BarcodeHolder {
        CheckBoxPreference mBarcodeEnable = null;
        EditTextPreference mBarcodeLength1 = null;
        EditTextPreference mBarcodeLength2 = null;

        CheckBoxPreference mBarcodeNOTIS = null;
        CheckBoxPreference mBarcodeCLSI = null;

        CheckBoxPreference mBarcodeISBT = null;
        CheckBoxPreference mBarcodeChecksum = null;
        CheckBoxPreference mBarcodeSendCheck = null;
        CheckBoxPreference mBarcodeFullASCII = null;
        ListPreference mBarcodeCheckDigit = null;
        CheckBoxPreference mBarcodeBookland = null;
        CheckBoxPreference mBarcodeSecondChecksum = null;
        CheckBoxPreference mBarcodeSecondChecksumMode = null;
        ListPreference mBarcodePostalCode = null;
        CheckBoxPreference mBarcodeSystemDigit = null;
        CheckBoxPreference mBarcodeConvertEAN13 = null;
        CheckBoxPreference mBarcodeConvertUPCA = null;
        CheckBoxPreference mBarcodeEanble25DigitExtensions = null;
        CheckBoxPreference mBarcodeDPM = null;
        int[] mParaIds = null;
        String[] mParaKeys = null;
    }

    /**
     * Use of android.device.scanner.configuration.Constants.Symbology Class
     */
    private int[] BARCODE_SYMBOLOGY = new int[]{
            Constants.Symbology.AZTEC,
            Constants.Symbology.CHINESE25,
            Constants.Symbology.CODABAR,
            Constants.Symbology.CODE11,
            Constants.Symbology.CODE32,
            Constants.Symbology.CODE39,
            Constants.Symbology.CODE93,
            Constants.Symbology.CODE128,
            Constants.Symbology.COMPOSITE_CC_AB,
            Constants.Symbology.COMPOSITE_CC_C,
            Constants.Symbology.COMPOSITE_TLC39,
            Constants.Symbology.DATAMATRIX,
            Constants.Symbology.DISCRETE25,
            Constants.Symbology.EAN8,
            Constants.Symbology.EAN13,
            Constants.Symbology.GS1_14,
            Constants.Symbology.GS1_128,
            Constants.Symbology.GS1_EXP,
            Constants.Symbology.GS1_LIMIT,
            Constants.Symbology.HANXIN,
            Constants.Symbology.INTERLEAVED25,
            Constants.Symbology.MATRIX25,
            Constants.Symbology.MAXICODE,
            Constants.Symbology.MICROPDF417,
            Constants.Symbology.MSI,
            Constants.Symbology.PDF417,
            Constants.Symbology.POSTAL_4STATE,
            Constants.Symbology.POSTAL_AUSTRALIAN,
            Constants.Symbology.POSTAL_JAPAN,
            Constants.Symbology.POSTAL_KIX,
            Constants.Symbology.POSTAL_PLANET,
            Constants.Symbology.POSTAL_POSTNET,
            Constants.Symbology.POSTAL_ROYALMAIL,
            Constants.Symbology.POSTAL_UPUFICS,
            Constants.Symbology.QRCODE,
            Constants.Symbology.TRIOPTIC,
            Constants.Symbology.UPCA,
            Constants.Symbology.UPCE,
            Constants.Symbology.UPCE1,
            Constants.Symbology.NONE,
            Constants.Symbology.RESERVED_6,
            Constants.Symbology.RESERVED_13,
            Constants.Symbology.RESERVED_15,
            Constants.Symbology.RESERVED_16,
            Constants.Symbology.RESERVED_20,
            Constants.Symbology.RESERVED_21,
            Constants.Symbology.RESERVED_27,
            Constants.Symbology.RESERVED_28,
            Constants.Symbology.RESERVED_30,
            Constants.Symbology.RESERVED_33
    };

    /**
     * Use of android.device.scanner.configuration.Symbology enums
     */
    private static Symbology[] BARCODE_SUPPORT_SYMBOLOGY = new Symbology[]{
            Symbology.AZTEC,
            Symbology.CHINESE25,
            Symbology.CODABAR,
            Symbology.CODE11,
            Symbology.CODE32,
            Symbology.CODE39,
            Symbology.CODE93,
            Symbology.CODE128,
            Symbology.COMPOSITE_CC_AB,
            Symbology.COMPOSITE_CC_C,
            Symbology.DATAMATRIX,
            Symbology.DISCRETE25,
            Symbology.EAN8,
            Symbology.EAN13,
            Symbology.GS1_14,
            Symbology.GS1_128,
            Symbology.GS1_EXP,
            Symbology.GS1_LIMIT,
            Symbology.INTERLEAVED25,
            Symbology.MATRIX25,
            Symbology.MAXICODE,
            Symbology.MICROPDF417,
            Symbology.MSI,
            Symbology.PDF417,
            Symbology.POSTAL_4STATE,
            Symbology.POSTAL_AUSTRALIAN,
            Symbology.POSTAL_JAPAN,
            Symbology.POSTAL_KIX,
            Symbology.POSTAL_PLANET,
            Symbology.POSTAL_POSTNET,
            Symbology.POSTAL_ROYALMAIL,
            Symbology.POSTAL_UPUFICS,
            Symbology.QRCODE,
            Symbology.TRIOPTIC,
            Symbology.UPCA,
            Symbology.UPCE,
            Symbology.UPCE1,
            Symbology.NONE
    };

}
