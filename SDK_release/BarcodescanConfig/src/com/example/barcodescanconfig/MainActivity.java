package com.example.barcodescanconfig;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private ImageView scanButton;
    private TextView barcode_result, decode_length,decode_symbology;
    private EditText scanResult;
    private CheckBox enableQRcode;
    private CheckBox enableI25, enableCode39, enableENA13;
    EditText minLength;
    EditText maxLength;
    private ScanManager mScanManager;
    int[] idactionbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
    int[] idbuf = new int[]{PropertyID.CODE39_ENABLE, PropertyID.QRCODE_ENABLE, PropertyID.I25_ENABLE, PropertyID.EAN13_ENABLE, PropertyID.CODE39_LENGTH1, PropertyID.CODE39_LENGTH2};
    String[] action_value_buf = new String[]{ScanManager.ACTION_DECODE, ScanManager.BARCODE_STRING_TAG};
    int[] value_buff;
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

        action_value_buf = mScanManager.getParameterString(idactionbuf);
        value_buff = mScanManager.getParameterInts(idbuf);
        scanResult = (EditText) findViewById(R.id.scanResult);
        decode_symbology = (TextView) findViewById(R.id.symbology_result);
        decode_length = (TextView) findViewById(R.id.length_result);
        barcode_result = (TextView) findViewById(R.id.barcode_result);
        scanResult.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_UP)
                {
                    barcode_result.setText("" + scanResult.getText());
                    decode_length.setText("" + scanResult.getText().length());
                    scanResult.setText("");
                    scanResult.requestFocus();
                    return true;
                }
                return false;
            }
        });
        enableI25= (CheckBox) findViewById(R.id.checkBox1);
        enableENA13= (CheckBox) findViewById(R.id.checkBox3);
        enableCode39= (CheckBox) findViewById(R.id.checkBox4);
        minLength= (EditText) findViewById(R.id.editText1);
        maxLength= (EditText) findViewById(R.id.editText2);
        enableQRcode = (CheckBox) findViewById(R.id.checkBox0);
        if(value_buff != null) {
            enableCode39.setChecked(value_buff[0] == 1);
            enableQRcode.setChecked(value_buff[1] == 1);
            enableI25.setChecked(value_buff[2] == 1);
            minLength.setText(String.valueOf(value_buff[4]));
            maxLength.setText(String.valueOf(value_buff[5]));
            enableENA13.setChecked(value_buff[3] == 1);
        }
        Button btn = (Button) findViewById(R.id.manager);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String min = minLength.getText().toString();
                String max = maxLength.getText().toString();

                value_buff[0] = enableCode39.isChecked() ? 1 : 0;
                value_buff[1] = enableQRcode.isChecked() ? 1 : 0;
                value_buff[2] = enableI25.isChecked() ? 1 : 0;
                value_buff[3] = enableENA13.isChecked() ? 1 : 0;
                if(!min.equals("")) {
                    value_buff[4] = Integer.parseInt(min);
                } else {
                    value_buff[4] = 6;
                }
                if(!max.equals("")) {
                    value_buff[5] = Integer.parseInt(max);
                } else {
                    value_buff[5] = 20;
                }
                mScanManager.setParameterInts(idbuf, value_buff);
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
        action_value_buf = mScanManager.getParameterString(idactionbuf);
        filter.addAction(action_value_buf[0]);
        registerReceiver(mScanReceiver, filter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem version = menu.add(0, 1, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);;
        version.setShowAsAction(1);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case 1:
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
}
