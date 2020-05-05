package com.example.iccmanager;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button mICC;
    private Button mSLE4442;
    private Button mPSAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mICC = (Button) findViewById(R.id.btn_icc);
        mICC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ICCActivity.class);
                startActivity(intent);
            }
        });

        mPSAM = (Button) findViewById(R.id.btn_psam);
        mPSAM.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PSAMActivity.class);
                startActivity(intent);
            }
        });
        
        mSLE4442 = (Button) findViewById(R.id.btn_sle4442);
        mSLE4442.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SLE4442Activity.class);
                startActivity(intent);
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
