
package com.example.pinpad;


import com.android.gridpasswordview.CustomDialog;
import com.android.gridpasswordview.CustomDialog.InputDialogListener;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.device.SEManager;
import android.device.SEManager.PedInputListener;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    SEManager mp;
    EditText textView1;
    
    private  CustomDialog customDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        mp = new SEManager();
        mp.open();
        textView1 = (EditText) findViewById(R.id.textView1);
        Button disableStatuBar= (Button) findViewById(R.id.button2);
        disableStatuBar.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] reslen = new byte[1];
                byte[] response = new byte[16];
                
                //int ret = mp.clearKey(response, reslen);
                
                byte[] mastkey = StrToHexByte("1C1C1C1C1C1C1C1C");
                byte[] wkpin = StrToHexByte("789E60DC9ED3FBCD"); 
                int ret = mp.deleteKey(4, 0,  response, reslen);
                ret = mp.downloadKey(4, 2, 1, mastkey, mastkey.length, response, reslen);//master key
                if(ret == 0) textView1.append(" \ndownload master key OK \n" + bytes2HexString(mastkey));
                mp.deleteKey(2, 10,  response, reslen);
                ret = mp.downloadKey(2, 10, 2, wkpin, wkpin.length, response, reslen);//PIN key
                if(ret == 0) textView1.append(" \ndownload PIN key OK \n" + bytes2HexString(wkpin) );
                byte[] reqBuf = "6225758326736419".getBytes();//卡号 pinblock 934025EA3A52E985
                
                 /**1--密钥类型
                 *2--pin密钥索引
                 *reqBuf //卡号
                 *自定义信息
                 *超时时间 毫秒
                 *密码支持的长度
                 *回调接口*/
                /*mp.getPinBlockEx(2, 10, reqBuf, reqBuf.length,
                        "请输入密码", 2 * 60 * 1000, "0,4,6,8,10,12", mPedInputListener);*///20160415 OS ROM version or more new
                if(param == null){
                    param = new Bundle();
                    param.putInt("KeyUsage", 2);
                    param.putInt("PINKeyNo", 10);
                    param.putInt("pinAlgMode", 1);
                    param.putString("cardNo", "6225887855370299");
                    param.putBoolean("sound", true);
                    param.putBoolean("onlinePin", true);
                    param.putBoolean("FullScreen", true);
                    param.putLong("timeOutMS", 60000);
                    param.putString("supportPinLen", "0,4,6,8,10,12");
                    param.putString("title", "Security Keyboard");
                    param.putString("message", "please input password \n 6225****0299");
                }
                mp.getPinBlockEx(param, mPedInputListener);
                /*if(Build.MODEL.equals("SQ26B")) {
                    customDialog = new CustomDialog(MainActivity.this,R.style.mystyle,R.layout.customdialog) ;
                    InputDialogListener inputDialogListener  = new  InputDialogListener() {
                        
                        @Override
                        public void onOK(String text) {
                            //mp.endPinInputEvent(0);//20160415 OS ROM version or more new
                            customDialog.dismiss();
                        }

                        @Override
                        public void onCancel() {
                            // TODO Auto-generated method stub
                          //mp.endPinInputEvent(1);//20160415 ROM version or more new
                            mp.removePedInputListener(mPedInputListener);
                            customDialog.dismiss();
                        }
                        
                    };
                    customDialog.setListener(inputDialogListener);
                    customDialog.show();
                }*/
            }
        });
        Button downLoadKey= (Button) findViewById(R.id.button1);
        downLoadKey.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                byte[] reslen = new byte[1];
                byte[] response = new byte[16];
                
                int ret = -1;//mp.clearKey(response, reslen);
                
                byte[] mastkey = StrToHexByte("11111111111111111111111111111111");
                byte[] wkpin = StrToHexByte(WORKINGKEY_DATA_PIN); 
                byte[] wkmac = StrToHexByte(WORKINGKEY_DATA_MAC); 
                byte[] wktd = StrToHexByte(WORKINGKEY_DATA_TRACK);
                ret = mp.deleteKey(4, 1, response, reslen);
                ret = mp.downloadKey(4, 1, 1, mastkey, mastkey.length, response, reslen);//master key
                if(ret == 0) textView1.append(" \ndownload master key OK \n" + bytes2HexString(mastkey));
                ret = mp.deleteKey(1, 2,  response, reslen);
                ret = mp.downloadKey(1, 2, 1, wktd, wktd.length, response, reslen);//TDK key
                if(ret == 0) textView1.append(" \ndownload TDK key OK \n" + bytes2HexString(wktd) );
                ret = mp.deleteKey(2, 3,  response, reslen);
                ret = mp.downloadKey(2, 3, 1, wkpin, wkpin.length, response, reslen);//PIN key
                if(ret == 0) textView1.append(" \ndownload PIN key OK \n" + bytes2HexString(wkpin) );
                ret = mp.deleteKey(3, 4,  response, reslen);
                ret =  mp.downloadKey(3, 4, 1, wkmac, wkmac.length, response, reslen);//mac key
                if(ret == 0) textView1.append("\ndownload mac key OK \n" + bytes2HexString(wkmac) );
            }
        });
        Button calcMac= (Button) findViewById(R.id.button3);
        calcMac.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] reslen = new byte[1];
                byte[] response = new byte[16];
                
                int ret = mp.clearKey(response, reslen);
                
                byte[] mastkey = StrToHexByte("11111111111111111111111111111111");
                byte[] wkmac = StrToHexByte(WORKINGKEY_DATA_MAC); 
                ret = mp.downloadKey(4, 1, 1, mastkey, mastkey.length, response, reslen);//master key
                if(ret == 0) textView1.append(" \ndownload master key OK \n" + bytes2HexString(mastkey));
                ret =  mp.downloadKey(3, 4, 1, wkmac, wkmac.length, response, reslen);//mac key
                if(ret == 0) textView1.append("\ndownload mac key OK \n" + bytes2HexString(wkmac) );
                byte[] getMac = new byte[8];
                calcMac(4, datas.length, datas, getMac, 0x10);//ECB 3931443142454331
                textView1.append(" \ncalcMacECB ==" + bytes2HexString(getMac));
            }
        });
        Button calcMacCBC= (Button) findViewById(R.id.button4);
        calcMacCBC.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] reslen = new byte[1];
                byte[] response = new byte[16];
                
                int ret = mp.clearKey(response, reslen);
          
                byte[] mastkey = StrToHexByte("31313131313131313131313131313131");
                byte[] wkmac = StrToHexByte("F92714E80E832EDC"); 
                ret = mp.downloadKey(4, 1, 1, mastkey, mastkey.length, response, reslen);//master key
                if(ret == 0) textView1.append("\n download master key OK \n" + bytes2HexString(mastkey));
                ret =  mp.downloadKey(3, 4, 1, wkmac, wkmac.length, response, reslen);//mac key
                if(ret == 0) textView1.append("\ndownload mac key OK \n" + bytes2HexString(wkmac) );
                byte[] getMac = new byte[8];
                calcMac(4, 8, StrToHexByte("0000000000000000"), getMac, 0x01);//CBC ADC67D8473BF2F06
                textView1.append(" \ncalcMacCBC ==" + bytes2HexString(getMac));
            }
        });
    }
    byte[] datas = StrToHexByte("11121314151617182827262524232221313233343536373844434241");//ECB result 3931443142454331
    //byte[] Mac = StrToHexByte("3646443232414636");// 事先算好的值
    
    public int calcMac(int KeyIdx, int DataInLen, byte[] DataIn, byte[] MacOut, int mode) {
        byte[] buf = new byte[17];
        byte[] tmpbuf = new byte[17];
        int i, l, k, iRet = 0;

        byte[] inbuf = new byte[DataInLen + 8];
        byte[] Macbuf = new byte[9];

        System.arraycopy(DataIn, 0, inbuf, 0, DataInLen);

        if ((DataInLen % 8) != 0) {
            l = DataInLen / 8 + 1;
        } else {
            l = DataInLen / 8;
        }

        // XOR：全部分组异或，最后做一个des。
        if (mode == 0x00) {
            Arrays.fill(buf, (byte) 0x00);
            for (i = 0; i < l; i++) {
                for (k = 0; k < 8; k++) {
                    buf[k] ^= inbuf[i * 8 + k];
                }
                buf[8] = 0x00;
            }
            byte[] plantbuf = new byte[16];
            iRet = pciDes(3, KeyIdx, 8, plantbuf, tmpbuf, 1);
            if (iRet != 0)
                return iRet;
            System.arraycopy(tmpbuf, 0, MacOut, 0, 8);
            return 0;
        } else if (mode == 0x01) {// ANSI x9.9(CBC)
            buf = new byte[8];
            tmpbuf = new byte[8];
            Arrays.fill(buf, (byte) 0x00);
            Arrays.fill(tmpbuf, (byte) 0x00);
            for (i = 0; i < l; i++) {
                System.arraycopy(tmpbuf, 0, buf, 0, 8);
                for (k = 0; k < 8; k++) {
                    buf[k] ^= inbuf[i * 8 + k];
                }
                iRet = pciDes(3, KeyIdx, 8, buf, tmpbuf, 1);
                if (iRet != 0)
                    return iRet;
            }

            System.arraycopy(tmpbuf, 0, MacOut, 0, 8);
            return (0);
        } else if (mode == 0x11) { // ANSI x9.19 ,在上面的基础上，最后的一组数据异或后不要做des加密，而是做一个3des加密
            buf = new byte[8];
            tmpbuf = new byte[8];
            Arrays.fill(buf, (byte) 0x00);
            Arrays.fill(tmpbuf, (byte) 0x00);
            for (i = 0; i < l; i++) {
                System.arraycopy(tmpbuf, 0, buf, 0, 8);
                for (k = 0; k < 8; k++) {
                    buf[k] ^= inbuf[i * 8 + k];
                }
                // buf[8] = 0x00;
                iRet = pciDes(3, KeyIdx, 8, buf, tmpbuf, 1);
                if (iRet != 0)
                    return iRet;
            }

            System.arraycopy(tmpbuf, 0, buf, 0, 8);
            iRet = pciDes(3, KeyIdx, 8, buf, tmpbuf, 0);
            if (iRet != 0)
                return iRet;
            iRet = pciDes(3, KeyIdx, 8, tmpbuf, MacOut, 1);
            if (iRet != 0)
                return iRet;
            return (0);
        } else if (mode == 0x10) {  // 直联银联POS-ECB
            Arrays.fill(buf, (byte) 0x00);
            buf = new byte[8];
            for (i = 0; i < l; i++) {
                for (k = 0; k < 8; k++) {
                    buf[k] ^= inbuf[i * 8 + k];
                }
            }
            android.util.Log.d("calcMac", bytes2HexString(buf));
            DecodeConvert.BcdToAsc(tmpbuf, buf, 16);
            android.util.Log.d("calcMac", "BcdToAsc" + bytes2HexString(tmpbuf));
            tmpbuf[16] = 0;
            System.arraycopy(tmpbuf, 0, buf, 0, 8);
            iRet = pciDes(3, KeyIdx, 8, buf, Macbuf, 1);
            if (iRet != 0)
                return iRet;
            android.util.Log.d("calcMac", "BcdToAsc des Macbuf" + bytes2HexString(Macbuf));
            Arrays.fill(buf, (byte) 0x00);
            System.arraycopy(Macbuf, 0, buf, 0, 8);
            for (k = 0; k < 8; k++) {
                buf[k] ^= tmpbuf[8 + k];
            }
            android.util.Log.d("calcMac", "^= buf" + bytes2HexString(buf));
            Arrays.fill(Macbuf, (byte) 0x00);
            iRet = pciDes(3, KeyIdx, 8, buf, Macbuf, 1);
            android.util.Log.d("calcMac", "BcdToAsc des 2 Macbuf" + bytes2HexString(Macbuf));
            if (iRet != 0)
                return iRet;
            Arrays.fill(buf, (byte) 0x00);
            System.arraycopy(Macbuf, 0, buf, 0, 8);

            Arrays.fill(tmpbuf, (byte) 0x00);
            DecodeConvert.BcdToAsc(tmpbuf, buf, 16);
            android.util.Log.d("calcMac", "BcdToAsc des result Macbuf" + bytes2HexString(tmpbuf));
            System.arraycopy(tmpbuf, 0, MacOut, 0, 8);

            return (0);
        }
        return -2;
    }
    /**
     * 指定的密钥号做运算
     * 
     * @param KeyType
     *            :密钥类型,
     * @param key_no
     *            :密钥号
     * @param inlen
     *            :被加解密的数据长度
     * @param indata
     *            :被加解密的数据
     * @param desout
     *            :结果
     * @param mode
     *            :1为加密，0为解密 返回0成功，非0不成功
     */
    private  int pciDes(int KeyType, int key_no, int inlen,
            byte[] indata, byte[] desout, int mode) {
        int iRet;
        byte[] reslen = new byte[1];
        byte[] dStartValue = new byte[8];

        if (mode == 0x01) {
            iRet = mp.encryptData(KeyType, key_no, 1, dStartValue, 8,
                    0x00, indata, inlen, desout, reslen);
        } else {
            iRet = mp.decryptData(KeyType, key_no, 1, dStartValue, 8,
                    0x00, indata, inlen, desout, reslen);
        }
        
        return iRet;
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mp.removePedInputListener(mPedInputListener);
        mp.close();
    }
    
    private PedInputListener mPedInputListener = new PedInputListener() {
        /**
         * 密码输入回调
         * 
         * @param result
         *            -1失败或超时 0 输入密码完成 1标示取消密码输入 2有密码输入
         * @param keylen
         *            已经输入的密码长度
         * @param keybuf
         *            输入完成时返回的key 数组
         */
        @Override
        public void onChanged(int result, int keylen, byte[] keybuf) {
            
            if(result == 2) {
                Toast.makeText(MainActivity.this, " keylen ==" + keylen, Toast.LENGTH_SHORT).show();
            } else if(result == 0){
                //Toast.makeText(MainActivity.this, " getPinBlock ==" , Toast.LENGTH_SHORT).show();
                textView1.append(" \ngetPinBlock =data=" + new String(keybuf) + " \ngetPinBlock =hex=" +  bytes2HexString(keybuf));
            }
            }
        };
        
        protected static final String MAINKEY = "655EA628CF62585F655EA628CF62585F";// 主密钥预设输入值，明文16个31，kcv:40826A5800608C87
        protected static final String WORKINGKEY_DATA_MAC = "F679786E2411E3DE";//"F92714E80E832EDC";// MAC秘钥预设输入值,明文16个32，kcv:ADC67D8473BF2F06
        protected static final String WORKINGKEY_DATA_TRACK = "4BF6E91B1E3A9D814BF6E91B1E3A9D81";// TRACK秘钥预设输入值,明文：16个33，kcvADC67D8473BF2F06
        protected static final String WORKINGKEY_DATA_PIN = "950973182317F80B950973182317F80B";//"5C660392B65906FC5C660392B65906FC";// PIN秘钥预设输入值,明文16个34，kcv:D2DB51F1D2013A63

     // 转成16进制
        private byte[] StrToHexByte(String str) {
            if (str == null)
                return null;
            else if (str.length() < 2)
                return null;
            else {
                int len = str.length() / 2;
                byte[] buffer = new byte[len];
                for (int i = 0; i < len; i++) {
                    buffer[i] = (byte) Integer.parseInt(
                            str.substring(i * 2, i * 2 + 2), 16);
                }
                return buffer;
            }
        }

        private String bytes2HexString(byte[] b) {
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
    Bundle param = null;
    byte[] reqBuf = "6225758326736419".getBytes();//卡号 pinblock 934025EA3A52E985
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_MENU && event.getAction()== KeyEvent.ACTION_DOWN) {

            return true;
        } else if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction()== KeyEvent.ACTION_DOWN) {
             //mp.removePedInputListener(mPedInputListener);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
}
