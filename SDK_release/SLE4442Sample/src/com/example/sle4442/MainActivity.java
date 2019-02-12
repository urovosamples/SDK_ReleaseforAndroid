package com.example.sle4442;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.device.*;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	private Button mDetect;
	private Button mReset;
	private Button mReadErrCnt;
	private Button mVerifyPassword;
	private Button mChangePassword;
	private Button mReadMainMem;
	private Button mReadProctectionMem;
	
	private Button mWriteMainMem;
	private Button mWriteProcMem;
	
	private EditText mPassword;
	private EditText mMemAddr;
	private EditText mMemLen;
	private EditText mNo;
	
	private EditText mDataToWrite;

	private IccManager mIccReader;
	
	private boolean mPasswordVefied = false;
	private boolean mCardLocked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mNo = (EditText)findViewById(R.id.et_memData);
		
		mDetect = (Button)findViewById(R.id.btn_detect);
		mReset = (Button)findViewById(R.id.btn_reset);
		mReadMainMem = (Button)findViewById(R.id.btn_readMainMem);
		mReadProctectionMem = (Button)findViewById(R.id.btn_readProctectionMem);
		mWriteMainMem = (Button)findViewById(R.id.btn_writeMainMem);
		mWriteProcMem = (Button)findViewById(R.id.btn_writeProcMem);
		
		mReadErrCnt = (Button)findViewById(R.id.btn_readErrCount);
		mVerifyPassword = (Button)findViewById(R.id.btn_verifyPassword);
		mChangePassword = (Button)findViewById(R.id.btn_changePassword);
		
		mMemAddr = (EditText)findViewById(R.id.et_memAddr);
		mMemLen = (EditText)findViewById(R.id.et_memLen);
		mPassword = (EditText)findViewById(R.id.et_password);
		mDataToWrite = (EditText)findViewById(R.id.et_dataToWrite);
		
		mDetect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 1 - Open IccReader
				int status = mIccReader.open((byte)0x00, (byte)0x02, (byte)0x01);
				if(status != 0){
					mNo.append("Init 4442 Card......." + "\n");
				}
				
				// 2 - Detect SLE4442
				status = mIccReader.detect();
				if(status != 0){
					mNo.append("Please insert 4442 Card......." + "\n");
				}
			}
		});
		
		mReset.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] atr = new byte[64]; // sle4442 - 4bytes
				int resLen = mIccReader.sle4442_reset(atr);
				if(resLen < 0){
					mNo.append("Reset 4442 Card fail......." + "\n");
				}else{
					mNo.append("ATR: " + Convert.bytesToHexString(atr, 0, resLen) + "\n");
				}
			}
		});
		
		mReadErrCnt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] errorCounter = new byte[1];
				int status = mIccReader.sle4442_readErrorCounter(errorCounter);
				if(status != 0){
					mNo.append("ErrorCount: " + "Verify failed......" + "\n");
					return;
				}
				
				mNo.append("ErrorCount: " + String.valueOf(errorCounter[0]) + "\n");
				mNo.append("\n");
				android.util.Log.i("Hz", "----------->> Verify password <<- errorCounter = --------- " + errorCounter[0]);
			}
			
		});
		
		mVerifyPassword.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				android.util.Log.i("Hz", "----------->> Verify password <<----------");
				String password = mPassword.getText().toString();
				
				android.util.Log.i("Hz", "----------->> Verify password <<----------" + password);
				
				// 1 - get Error counter
				byte[] errorCounter = new byte[1];
				int status = mIccReader.sle4442_readErrorCounter(errorCounter);
				if(status != 0){
					mNo.append("Password: " + "Verify failed......" + "\n");
					return;
				}
				android.util.Log.i("Hz", "----------->> Verify password <<- errorCounter = --------- " + errorCounter[0]);
				
				if(errorCounter[0] == 0){
					mCardLocked = true;
					mNo.append("Password: " + "Verify failed, card locked......" + "\n");
					mNo.append("Password: " + "Error counter: ...... 0x" + Convert.bytesToHexString(errorCounter) + "\n");
					return;
				}
				
				if(!password.equals("")){
					if(Convert.isHex(password, MainActivity.this) && password.length() == 6){
						// 2 - perform password verify 
						status = mIccReader.sle4442_verifyPassword(Convert.hexStringToByteArray(password));
						
						if(status == 0){
							mNo.append("Password: " + "Veriy OK......" + "\n");
							mPasswordVefied = true;
						}else{
							mNo.append("Password: " + "Verify failed......" + "\n");
							mPasswordVefied = false;
						}
						
					}
				}else{
					Toast.makeText(MainActivity.this, "please input password", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		mChangePassword.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				android.util.Log.i("Hz", "----------->> Change password <<----------");
				String password = mPassword.getText().toString();
				
				// 1 - verify password
				if(!mPasswordVefied){
					mNo.append("Please verify your password......" + "\n");
					return;
				}
				
				// 2 - Get Error counter, test if the card is locked;
				if(mCardLocked){
					mNo.append("Password: " + "Your card is locked, can't do change passord operation..." + "\n");
					return;
				}
				
				
				if(!password.equals("")){
					if(Convert.isHex(password, MainActivity.this) && password.length() == 6){
						// 3 - perform password verify 
						int status = mIccReader.sle4442_changePassword(Convert.hexStringToByteArray(password));
						
						if(status == 0){
							mNo.append("Password: " + "Change OK......" + "\n");
							mPasswordVefied = true;
						}else{
							mNo.append("Password: " + "Change failed......" + "\n");
							mPasswordVefied = false;
						}
					}
				}else{
					Toast.makeText(MainActivity.this, "please input password", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		mReadMainMem.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				android.util.Log.i("Hz", "----------->> Read Main memory <<----------");
				
				int memAddress;
				int memLength;
				
				byte memData[] = new byte[512];
				
				String memAddressTmp = mMemAddr.getText().toString();
				String memLengthTmp = mMemLen.getText().toString();
								
				if(!memAddressTmp.equals("") && !memLengthTmp.equals("")){
					if(memAddressTmp.matches("[0-9]+") == false || memLengthTmp.matches("[0-9]+") == false){
						Toast.makeText(MainActivity.this, "please input number content", Toast.LENGTH_LONG).show();
						return;
					}
					
					memAddress = (int) Integer.parseInt(memAddressTmp);
					memLength = (int) Integer.parseInt(memLengthTmp);
					mMemAddr.setText(String.valueOf(memAddress));
					
					memLength = (int)(256 - memAddress) < (int)memLength  ? (int)(256 - memAddress) : (int)memLength;
					if(memLength <= 0){
						memLength = 1;
					}
					mMemLen.setText(String.valueOf(memLength));
					
//					android.util.Log.i("Hz", "mem addr = " + memAddress);
//					android.util.Log.i("Hz", "mem len = " + memLength);
					
					memData = mIccReader.sle4442_readMainMemory(memAddress, memLength);
					
					if(memData != null){
						Convert.hexDump(mNo, (byte)memAddress, memData, (int)memData.length);
//						mMemAddr.setText("");
//						mMemLen.setText("");
					}else{
						mNo.append("Read main memory data fail......." + "\n");
					}
					
				}else{
					Toast.makeText(MainActivity.this, "please input content", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		// Read Protected memory； 4-bytes
		mReadProctectionMem.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				android.util.Log.i("Hz", "----------->> Read Protected memory <<----------");
				
				int memAddress = 0;
				int memLength = 0;
				
				byte memData[] = new byte[512];
				
				String memAddressTmp = mMemAddr.getText().toString();
				String memLengthTmp = mMemLen.getText().toString();
				
				if(!memAddressTmp.equals("") && !memLengthTmp.equals("")){
					if(memAddressTmp.matches("[0-9]+") == false || memLengthTmp.matches("[0-9]+") == false){
						Toast.makeText(MainActivity.this, "please input number content", Toast.LENGTH_LONG).show();
						return;
					}
					
					memAddress = (int) Integer.parseInt(memAddressTmp) % 0xFF;
					memLength = (int) Integer.parseInt(memLengthTmp);
					mMemAddr.setText(String.valueOf(memAddress));
					
					memLength = (int)(255 - memAddress) < (int)memLength  ? (int)(255 - memAddress) : (int)memLength;
					if(memLength <= 0){
						memLength = 1;
					}
					mMemLen.setText(String.valueOf(memLength));
					
				}
				
//				android.util.Log.i("Hz", "proc mem addr = " + memAddress);
//				android.util.Log.i("Hz", "proc mem len = " + memLength);
				
				// 1 - Read protected memory
                byte[] proMemCont = mIccReader.sle4442_readProtectionMemory(memAddress, memLength);				

                if(proMemCont != null){
					mNo.append("ProctectedMem: " + Convert.bytesToHexString(proMemCont).toUpperCase() + "\n");
				}else{
					mNo.append("Read proctection memory failed......" + "\n");
				}
			}
		});
		
		mWriteMainMem.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int memAddress;
				
				String reception = mDataToWrite.getText().toString();
				android.util.Log.i("Hz", "onEditorAction:" + reception);
								
				// 1 - verify password
				if(!mPasswordVefied){
					mNo.append("Please verify your password......" + "\n");
					return;
				}
				
				// 2 - Get Error counter, test if the card is locked;
				if(mCardLocked){
					mNo.append("Password: " + "Your card is locked, can do write operation..." + "\n");
					return;
				}
					
				String memAddressTmp = mMemAddr.getText().toString();
				if(!memAddressTmp.equals("") && !reception.equals("")){
					if(memAddressTmp.matches("[0-9]+") == false ){
						Toast.makeText(MainActivity.this, "please input number content", Toast.LENGTH_LONG).show();
						return;
					}
					
					memAddress = (int) Integer.parseInt(memAddressTmp) % 256;
					mMemAddr.setText(String.valueOf(memAddress));
					
					if(Convert.isHex(reception, MainActivity.this)) {
						mNo.append("\n");
						mNo.append("SEND: " + reception + "\n");
						
						byte[] dataToWrite = Convert.hexStringToByteArray(reception);	
//						android.util.Log.i("Hz", "mem addr = " + (char)memAddress);
//						android.util.Log.i("Hz", "data to write = " + reception);
						
						if(dataToWrite != null){
							android.util.Log.i("Hz", " ---------- write main memory -------");
							// 3 - write main memory
							int status = mIccReader.sle4442_writeMainMemory(memAddress, dataToWrite, reception.length() / 2);
							if(status == 0){
								mNo.append("Write Main Memory Success..." + "\n");
								
							}else{
								mNo.append("Write Main Memory Failed..." + "\n");
							}
						}
					}
					
				}else {
                    Toast.makeText(MainActivity.this, "please input content", Toast.LENGTH_SHORT).show();
                }
			}		
		});
		
		mWriteProcMem.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int memAddress;
				
				String reception = mDataToWrite.getText().toString();
				android.util.Log.i("Hz", "onEditorAction:" + reception);
				
				String memAddressTmp = mMemAddr.getText().toString();
				
				// 1 - verify password
				if(!mPasswordVefied){
					mNo.append("\n");
					mNo.append("Please verify your password......" + "\n");
					return;
				}
				
				// 2 - Get Error counter, test if the card is locked;
				if(mCardLocked){
					mNo.append("\n");
					mNo.append("Password: " + "Your card is locked, can do write operation..." + "\n");
					return;
				}
				
				if(!memAddressTmp.equals("") && !reception.equals("")){
					if(memAddressTmp.matches("[0-9]+") == false ){
						Toast.makeText(MainActivity.this, "please input number content", Toast.LENGTH_LONG).show();
						return;
					}
					
					memAddress = (int) Integer.parseInt(memAddressTmp);
					mMemAddr.setText(String.valueOf(memAddress));
					
					if(Convert.isHex(reception, MainActivity.this)) {
						mNo.append("\n");
						mNo.append("SEND: " + reception + "\n");
						
						byte[] dataToWrite = Convert.hexStringToByteArray(reception);	
//						android.util.Log.i("Hz", "mem addr = " + (char)memAddress);
//						android.util.Log.i("Hz", "data to write = " + reception);
						
						if(dataToWrite != null){
							android.util.Log.i("Hz", " ---------- write proctecition memory -------");
							// 3 - write protection memory
							int status = mIccReader.sle4442_writeProtectionMemory(memAddress, dataToWrite, reception.length() / 2);
							if(status == 0){
								mNo.append("Write Main Memory Success..." + "\n");
								
							}else{
								mNo.append("Write Main Memory Failed..." + "\n");
							}
						}
					}
				}else {
                    Toast.makeText(MainActivity.this, "please input content", Toast.LENGTH_SHORT).show();
                }
			}
		});
	}
		
	@Override
	protected void onResume(){
		super.onResume();
		mIccReader = new IccManager();
	}
	
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //if(mIccReadeThreadr != null)
        //    mIccReadeThreadr.threadrun = false;
        if(mIccReader != null) {
            int ret = mIccReader.deactivate();
            ret = mIccReader.close();
            android.util.Log.i("mIccReader", "-----------close-----ret =" + ret);
        }
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
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
