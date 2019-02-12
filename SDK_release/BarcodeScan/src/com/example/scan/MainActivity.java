package com.example.scan;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private ImageView scanButton;
    private TextView barcode_result, decode_length,decode_symbology;
    private EditText scanResult;
    private CheckBox continuousScan;
    private RadioGroup mRadioGroup;
    private ScanManager mScanManager;
    int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
    int[] idmodebuf = new int[]{PropertyID.WEDGE_KEYBOARD_ENABLE, PropertyID.TRIGGERING_MODES};
    String[] action_value_buf = new String[]{ScanManager.ACTION_DECODE, ScanManager.BARCODE_STRING_TAG};
    int[] idmode;
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            String result = intent.getStringExtra(action_value_buf[1]);
            /*if(barcodelen != 0)
                barcodeStr = new String(barcode, 0, barcodelen);
            else
                barcodeStr = intent.getStringExtra("barcode_string");*/
            if(result != null) {
                barcode_result.setText("" + result);
                decode_length.setText("" + result.length());
            }
        }

    };
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanManager = new ScanManager();
        mScanManager.openScanner();

        action_value_buf = mScanManager.getParameterString(idbuf);
        idmode = mScanManager.getParameterInts(idmodebuf);
        continuousScan = (CheckBox) findViewById(R.id.continuousScan);
        continuousScan.setChecked(idmode[1] == Triggering.CONTINUOUS.toInt());
        continuousScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mScanManager.setTriggerMode(b ? Triggering.CONTINUOUS : Triggering.HOST);
                idmode[1] = b ? Triggering.CONTINUOUS.toInt() : Triggering.HOST.toInt();
                scanButton.setBackgroundResource(R.drawable.scan_button);
            }
        });
        scanResult = (EditText) findViewById(R.id.scanResult);
        mRadioGroup = (RadioGroup) findViewById(R.id.mode_output);
        mRadioGroup.setOnCheckedChangeListener(this);
        RadioButton keyboardMode = (RadioButton) findViewById(R.id.keyboard_output);
        keyboardMode.setChecked(idmode[0] == 1);
        RadioButton intentMode = (RadioButton) findViewById(R.id.intent_output);
        if(idmode[0] == 0) {
            scanResult.setVisibility(View.GONE);
            intentMode.setChecked(true);
        }
        decode_symbology = (TextView) findViewById(R.id.symbology_result);
        decode_length = (TextView) findViewById(R.id.length_result);
        barcode_result = (TextView) findViewById(R.id.barcode_result);
        scanButton = (ImageView) findViewById(R.id.scanButton);
        scanButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        //resetStatusView();
                        if(idmode[1] == Triggering.CONTINUOUS.toInt()) {
                            scanButton.setBackgroundResource(R.drawable.scan_button_down);
                        /*if(vibrator != null)
                            vibrator.vibrate(VIBRATE_DURATION);*/
                            mScanManager.startDecode();
                        } else {
                            scanButton.setBackgroundResource(R.drawable.scan_button_down);
                        /*if(vibrator != null)
                            vibrator.vibrate(VIBRATE_DURATION);*/
                            mScanManager.startDecode();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(idmode[1] == Triggering.CONTINUOUS.toInt()) {

                        } else {
                            mScanManager.stopDecode();
                        }
                        scanButton.setBackgroundResource(R.drawable.scan_button);
                        break;
                }
                return true;
            }
        });
        scanResult.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_UP)
                {
                    barcode_result.setText("" + scanResult.getText());
                    decode_length.setText("" + scanResult.getText().length());
                    scanResult.setText("");
                }
                return false;
            }
        });
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mScanReceiver);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter filter = new IntentFilter();
        action_value_buf = mScanManager.getParameterString(idbuf);
        filter.addAction(action_value_buf[0]);
        registerReceiver(mScanReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        MenuItem settings = menu.add(0, 1, 0, R.string.menu_settings).setIcon(R.drawable.ic_action_settings);
        // 绑定到actionbar
        //SHOW_AS_ACTION_IF_ROOM 显示此项目在动作栏按钮如果系统决定有它。 可以用1来代替
        MenuItem version = menu.add(0, 2, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);;
        settings.setShowAsAction(1);
        version.setShowAsAction(0);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case 1:
                try{
                    Intent intent = new Intent("android.intent.action.SCANNER_SETTINGS");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                break;
            case 2:
                PackageManager pk = getPackageManager();
                PackageInfo pi;
                try {
                    pi = pk.getPackageInfo(getPackageName(), 0);
                    Toast.makeText(this, "V" +pi.versionName , Toast.LENGTH_SHORT).show();
                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.intent_output:
                idmode[0] = 0;
                mScanManager.setParameterInts(idmodebuf, idmode);
                scanResult.setVisibility(View.GONE);
                break;
            case R.id.keyboard_output:
                idmode[0] = 1;
                mScanManager.setParameterInts(idmodebuf, idmode);
                scanResult.setVisibility(View.VISIBLE);
                scanResult.requestFocus();
                break;
        }
    }
}
