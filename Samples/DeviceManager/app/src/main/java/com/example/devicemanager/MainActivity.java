package com.example.devicemanager;

import android.app.Activity;
import android.device.DeviceManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    DeviceManager mDevice;
    TextView SNTextView;
    TextView TUSNTextView;
    TextView DockerStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDevice = new DeviceManager();
        SNTextView = (TextView) findViewById(R.id.TextView1);
        TUSNTextView = (TextView) findViewById(R.id.TextView2);
        DockerStateTextView = (TextView) findViewById(R.id.TextView9);
        Button SNKey = (Button) findViewById(R.id.button1);
        SNKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    String SNNumber = mDevice.getDeviceId();  //Get SN
                    if (SNNumber.equals("")) {
                        Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                    } else {
                        SNTextView.setText("SN:" + SNNumber);  //Display SN
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button TUSNKey = (Button) findViewById(R.id.button2);
        TUSNKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    String TUSNNumber = mDevice.getTIDSN();  //Get TUSN
                    if (TUSNNumber.equals("")) {
                        Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                    } else {
                        TUSNTextView.setText("TUSN:" + TUSNNumber);  //Display TUSN
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button setTimeKey = (Button) findViewById(R.id.button3);
        setTimeKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    long cur = System.currentTimeMillis();  //Get current time
                    boolean ret = mDevice.setCurrentTime(cur + 60 * 60 * 1000 + 1000 * 60);  //Add 1 hour and 1 minute to the current time
                    if (!ret) {
                        Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button enableHomeKey = (Button) findViewById(R.id.button4);
        enableHomeKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    mDevice.enableHomeKey(true);  //Enable home key
                    Toast.makeText(MainActivity.this, "Enable success", Toast.LENGTH_SHORT).show();
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button disableHomeKey = (Button) findViewById(R.id.button4_1);
        disableHomeKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    mDevice.enableHomeKey(false);  //Disable home key
                    Toast.makeText(MainActivity.this, "Disable success", Toast.LENGTH_SHORT).show();
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button enableStatusBarKey = (Button) findViewById(R.id.button5);
        enableStatusBarKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    mDevice.enableStatusBar(true);  //Enable status bar
                    Toast.makeText(MainActivity.this, "Enable success", Toast.LENGTH_SHORT).show();
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button disableStatusBarKey = (Button) findViewById(R.id.button5_1);
        disableStatusBarKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    mDevice.enableStatusBar(false);  //Disable status bar
                    Toast.makeText(MainActivity.this, "Disable success", Toast.LENGTH_SHORT).show();
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button setAPNKey = (Button) findViewById(R.id.button6);
        setAPNKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    /*
                    mDevice.setAPN("中国联通 3g 网络", "3gnet", null, 0, "none",null, "none", null, "460", "01", null,0, 0, "default,supl,dun", null, 0, null, true);
                    Toast.makeText(MainActivity.this, "Setting up China Unicom APN", Toast.LENGTH_SHORT).show();
                    */
                    /*
                    mDevice.setAPN("Telecom", "ctnet", null, 0, "ctnet@mycdma.cn",null, "vnet.mobi", null, "460", "03", null,0, 3, "default,hipri", "IP", 0, "IP", true);
                    Toast.makeText(MainActivity.this, "Set up China Telecom APN", Toast.LENGTH_SHORT).show();
                    */
                    boolean ret = mDevice.setAPN("中国移动NET设置", "cmnet", null, 0, "none", null, "none", null, "460", "02", null, 0, 0, "default,supl,dun", null, 0, null, true);
                    if (ret) {
                        Toast.makeText(MainActivity.this, "Set up China Mobile APN", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Set failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button installApplicationKey = (Button) findViewById(R.id.button7);
        installApplicationKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    String apkFile = "";   //For example:"/sdcard/demo.apk"
                    if (apkFile.equals("")) {
                        Toast.makeText(MainActivity.this, "Installation package not found", Toast.LENGTH_SHORT).show();
                    } else {
                        mDevice.installApplication(apkFile);
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button uninstallApplicationKey = (Button) findViewById(R.id.button8);
        uninstallApplicationKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    String packageName = "";  //For example:"com.example.key"
                    if (packageName.equals("")) {
                        Toast.makeText(MainActivity.this, "PackageName not found", Toast.LENGTH_SHORT).show();
                    } else {
                        mDevice.uninstallApplication(packageName);
                    }
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button getDockerStateKey = (Button) findViewById(R.id.button9);
        getDockerStateKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // TODO Auto-generated method stub
                    boolean state = mDevice.getDockerState();  //Get the current USB host status
                    DockerStateTextView.setText("DockerState:" + state);  //Show get results
                } catch (NoSuchMethodError e) {
                    Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                }
            }
        });


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
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

}