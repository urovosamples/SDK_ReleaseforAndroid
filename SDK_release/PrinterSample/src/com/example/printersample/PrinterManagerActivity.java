/*
 * Copyright (C) 2014 Urovo Technologies Corp.
 */

package com.example.printersample;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class PrinterManagerActivity extends AppCompatActivity {
    private static final String TAG = "PrinterManagerActivity";
    private Button mBtnPrnBill;
    private Button mBtnPrnPic;
    private Button mBtnPrnBarcode;
    private Button mBtnForWard;
    private Button mBtnBack;
    private EditText printInfo;

    /********************** Config Start *************************************/
    // Do we a pre-install app or a 319 test one
    private boolean mIs319 = false;
    private boolean mIsTempEnable = false;
    /********************** Config End **************************************/

    private LinearLayout mLlFactoryTest;

    public final static String PRNT_ACTION = "action.printer.message";
    PrinterManager printer = new PrinterManager();

    // Temperature
    private TextView mTvTemp;

    // Product Testing
    private boolean mIsFactoryTest = false;
    private LinearLayout mPrnPicture;
    private LinearLayout mPrnBill;

    private CheckBox mCbFactoryTest;
    private CheckBox mCbPrintBarcode;

    private LinearLayout mBarcodeTypeLayout;
    private FontStylePanel mFontStylePanel;

    private Bitmap mBmpPicture;
    private boolean isPrintText = false;

    private final int DEF_TEMP_THROSHOLD = 50;
    private int mTempThresholdValue = DEF_TEMP_THROSHOLD;

    private int mVoltTempPair[][] = {
            {898, 80},
            {1008, 75},
            {1130, 70},
            {1263, 65},
            {1405, 60},
            {1555, 55},
            {1713, 50},
            {1871, 45},
            {2026, 40},
            {2185, 35},
            {2335, 30},
            {2475, 25},
            {2605, 20},
            {2722, 15},
            {2825, 10},
            {2915, 5},
            {2991, 0},
            {3054, -5},
            {3107, -10},
            {3149, -15},
            {3182, -20},
            {3209, -25},
            {3231, -30},
            {3247, -35},
            {3261, -40},
    };

    private static final String[] mTempThresholdTable = {
            "80", "75", "70", "65", "60",
            "55", "50", "45", "40", "35",
            "30", "25", "20", "15", "10",
            "5", "0", "-5", "-10", "-15",
            "-20", "-25", "-30", "-35", "-40",
    };

    private static final String[] mBarTypeTable = {
            "3", "20", "25",
            "29", "34", "55", "58",
            "71", "84", "92",
    };

    private Spinner mSpinerThreshold;
    private ArrayAdapter<String> mThresholdAdapter;

    private Spinner mBarcodeType;
    private int mBarcodeTypeValue;


    private final static String SPINNER_PREFERENCES_FILE = "SprinterPrefs";
    private final static String SPINNER_SELECT_POSITION_KEY = "spinnerPositions";
    private final static int DEF_SPINNER_SELECT_POSITION = 6;
    private final static String SPINNER_SELECT_VAULE_KEY = "spinnerValue";
    private final static String DEF_SPINNER_SELECT_VAULE = mTempThresholdTable[DEF_SPINNER_SELECT_POSITION];

    private int mSpinnerSelectPosition;
    private String mSpinnerSelectValue;

    private final static int DEF_PRINTER_HUE_VALUE = 0;
    private final static int MIN_PRINTER_HUE_VALUE = 0;
    private final static int MAX_PRINTER_HUE_VALUE = 4;

    private final static int DEF_PRINTER_SPEED_VALUE = 9;
    private final static int MIN_PRINTER_SPEED_VALUE = 0;
    private final static int MAX_PRINTER_SPEED_VALUE = 9;

    // Printer Status
    private final static int PRNSTS_OK = 0;                // OK
    private final static int PRNSTS_OUT_OF_PAPER = -1;    // Out of paper
    private final static int PRNSTS_OVER_HEAT = -2;        // Over heat
    private final static int PRNSTS_UNDER_VOLTAGE = -3;    // under voltage
    private final static int PRNSTS_BUSY = -4;            // Device is busy
    private final static int PRNSTS_ERR = -256;            // Common error
    private final static int PRNSTS_ERR_DRIVER = -257;    // Printer Driver error

    private Button mBtSetHue;
    private EditText mEtHue;
    private Button mBtSetSpeed;
    private EditText mEtSpeed;
    private int mPrinterHue = DEF_PRINTER_HUE_VALUE;
    private int mPrinterSpeed = 0;

    private final int FACTORYTEST_ON = 1;
    private final int FACTORYTEST_OFF = 0;
    private final String DEF_FACTORYTEST_CONTENT_VALUE = "0123456789";

    private final static String STR_PRNT_BILL = "prn_bill";
    private final static String STR_PRNT_TEXT = "text";
    private final static String STR_PRNT_BLCOK = "block";
    private final static String STR_PRNT_SALE = "sale";


    private final static String MODEL_MOR = "YBMoR"; // This model need "Backward".
    private boolean mIsMoR = false;

    private BroadcastReceiver mPrtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int ret = intent.getIntExtra("ret", 0);
            if (ret == PRNSTS_OUT_OF_PAPER) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_paper,
                        Toast.LENGTH_SHORT).show();
            } else if (ret == PRNSTS_OVER_HEAT) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_temperature,
                        Toast.LENGTH_SHORT).show();
            } else if (ret == PRNSTS_UNDER_VOLTAGE) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_voltage,
                        Toast.LENGTH_SHORT).show();
            } else if (ret == PRNSTS_BUSY) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_busy,
                        Toast.LENGTH_SHORT).show();
            } else if (ret == PRNSTS_ERR) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_error,
                        Toast.LENGTH_SHORT).show();
            } else if (ret == PRNSTS_ERR_DRIVER) {
                Toast.makeText(
                        PrinterManagerActivity.this,
                        R.string.tst_info_driver_error,
                        Toast.LENGTH_SHORT).show();
            }
            mBtnPrnBarcode.setEnabled(true);
            mBtnForWard.setEnabled(true);
            mBtnPrnBill.setEnabled(true);
            mBtnPrnPic.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CustomThread().start();
        // get device Model: SQ27_P3_00WE_YBMoR_AU48_418_S_0_160314_01_3af5ecf
        //	String[] model = Build.ID.split("_");
        //mIsMoR = model[3].equals(MODEL_MOR);

        mBtnPrnBill = (Button) findViewById(R.id.btn_prnBill);
        printInfo = (EditText) findViewById(R.id.printer_info);

        mTvTemp = (TextView) findViewById(R.id.tv_temp);
        mTvTemp.setText(String.valueOf(getCurrentTemp()));

        mPrnPicture = (LinearLayout) findViewById(R.id.ll_picture);
        mPrnBill = (LinearLayout) findViewById(R.id.ll_bill);

        /* Set hue: 0 ~ 4, default is 0 */
        mEtHue = (EditText) findViewById(R.id.et_hue);
        mBtSetHue = (Button) findViewById(R.id.bt_set_hue);
        mBtSetHue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Convert.isNumeric(mEtHue.getText().toString(), PrinterManagerActivity.this)) {
                    try {
                        mPrinterHue = Integer.parseInt(mEtHue.getText().toString());
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    mPrinterHue = DEF_PRINTER_HUE_VALUE;
                }

                if (mPrinterHue < 0 || mPrinterHue < MIN_PRINTER_HUE_VALUE ||
                        mPrinterHue > MAX_PRINTER_HUE_VALUE) {
                    mPrinterHue = DEF_PRINTER_HUE_VALUE;
                }

                if (printer == null) {
                    printer = new PrinterManager();
                }

                Log.d("printer", "---------set hue = " + mPrinterHue);
                printer.prn_setBlack(mPrinterHue);
                mEtHue.setText(String.valueOf(mPrinterHue));
            }

        });

        /* Set speed: 0 ~ 9, default is 9 */
        mEtSpeed = (EditText) findViewById(R.id.et_speed);
        mBtSetSpeed = (Button) findViewById(R.id.bt_set_speed);
        mBtSetSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Convert.isNumeric(mEtSpeed.getText().toString(), PrinterManagerActivity.this)) {
                    try {
                        mPrinterSpeed = Integer.parseInt(mEtSpeed.getText().toString());
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    mPrinterSpeed = DEF_PRINTER_SPEED_VALUE;
                }

                if (mPrinterSpeed < MIN_PRINTER_SPEED_VALUE || mPrinterSpeed > MAX_PRINTER_SPEED_VALUE) {
                    mPrinterSpeed = DEF_PRINTER_SPEED_VALUE;
                }

                if (printer == null) {
                    printer = new PrinterManager();
                }

                Log.d("printer", "---------set PrinterSpeed = " + mPrinterSpeed);
                printer.prn_setSpeed(mPrinterSpeed);
                mEtSpeed.setText(String.valueOf(mPrinterSpeed));
            }
        });

        mBtnPrnBill.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String messgae = printInfo.getText().toString();
                mBtnPrnPic.setEnabled(false);
                mBtnPrnBarcode.setEnabled(false);
                mBtnForWard.setEnabled(false);
                mBtnPrnBill.setEnabled(false);
                int ret = printer.prn_getStatus();
                if (ret == 0) {
                    if (isPrintText) {
                        if (messgae == null || messgae.equals("")) {
                            messgae = "Input right content, Please!!!";
                        }
                        doprintwork(messgae);// print string
                    } else {
//						if (messgae.length() > 0) {
//							doprintwork(messgae);
//						} else {
                        doprintwork(STR_PRNT_SALE);// print sale
//							doprintwork(STR_PRNT_BLCOK);
//							doprintwork(STR_PRNT_BILL);
//						}
                    }
                } else {
                    Intent intent = new Intent(PRNT_ACTION);
                    intent.putExtra("ret", ret);
                    sendBroadcast(intent);
                }
            }
        });

        mBtnForWard = (Button) findViewById(R.id.btn_FORWARD);
        mBtnForWard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTempThreshold())
                    return;
                mBtnPrnPic.setEnabled(false);
                mBtnPrnBarcode.setEnabled(false);
                mBtnForWard.setEnabled(false);
                mBtnPrnBill.setEnabled(false);

                mHandler.obtainMessage(FORWARD).sendToTarget();

            }
        });

        mBtnBack = (Button) findViewById(R.id.btn_BACK);
        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTempThreshold())
                    return;

                printer.prn_paperBack(4);
            }
        });
        if (!mIsMoR) {
            mBtnBack.setVisibility(View.GONE);
        }

        mBtnPrnPic = (Button) findViewById(R.id.btn_prnPicture);
        mBtnPrnPic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBmpPicture = null;
                new AlertDialog.Builder(PrinterManagerActivity.this)
                        .setTitle(R.string.tst_info_select_picture)
                        .setMessage(R.string.tst_info_select_picture_msg)
                        .setNegativeButton(R.string.mci_select_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnPic.setEnabled(false);
                                mBtnPrnBarcode.setEnabled(false);
                                mBtnForWard.setEnabled(false);
                                mBtnPrnBill.setEnabled(false);

                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                            }
                        })
                        .setPositiveButton(R.string.mci_select_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnPic.setEnabled(false);
                                mBtnPrnBarcode.setEnabled(false);
                                mBtnForWard.setEnabled(false);
                                mBtnPrnBill.setEnabled(false);

                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                opts.inDensity = getResources().getDisplayMetrics().densityDpi;
                                opts.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
                                mBmpPicture = BitmapFactory.decodeResource(getResources(), R.drawable.hcp, opts);
                                mHandler.obtainMessage(PRNPIC).sendToTarget();
                            }
                        })
                        .create()
                        .show();
            }
        });

        mBtnPrnBarcode = (Button) findViewById(R.id.btn_barcode);
        mBtnPrnBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String messgae = printInfo.getText().toString();
                if (messgae.length() > 0) {
                    mBtnPrnPic.setEnabled(false);
                    mBtnPrnBarcode.setEnabled(false);
                    mBtnForWard.setEnabled(false);
                    mBtnPrnBill.setEnabled(false);

                    mHandler.obtainMessage(BARCOD).sendToTarget();
                } else {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_hint_content,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCbFactoryTest = (CheckBox) findViewById(R.id.cb_factoryTest);
        mCbFactoryTest.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                mIsFactoryTest = isChecked;
                if (mIsFactoryTest) {
                    mPrnPicture.setVisibility(View.GONE);
                    mPrnBill.setVisibility(View.GONE);
                    printInfo.setText(DEF_FACTORYTEST_CONTENT_VALUE);
                } else {
                    mPrnPicture.setVisibility(View.VISIBLE);
                    mPrnBill.setVisibility(View.VISIBLE);
                    printInfo.setText("");
                }
            }

        });

        Log.v("tao.he", "Fonts style setting");
        mFontStylePanel = new FontStylePanel(this);

        mBarcodeTypeLayout = (LinearLayout) findViewById(R.id.ll_barcodeType);
        mCbPrintBarcode = (CheckBox) findViewById(R.id.cb_printBarcode);
        mCbPrintBarcode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBarcodeTypeValue = Integer.parseInt(mBarTypeTable[mBarcodeType.getSelectedItemPosition()]);
                    switch (mBarcodeTypeValue) {
                        case 34:// UPCA, no., UPCA needs short length of No.
                            //case 2:// Chinese25MATRIX, no.
                        case 3:// Chinese25INTER, no.
                        case 29:// RSS14, no.
                            printInfo.setInputType(InputType.TYPE_CLASS_NUMBER);
                            break;
                        case 20:// CODE128, alphabet + no.
                        case 25:// CODE93, alphabet + no.
                        case 55:// PDF417, setHue: 3
                        case 58:// QRCODE
                        case 71:// DATAMATRIX
                        case 84:// uPDF417
                        case 92:// AZTEC
                            printInfo.setInputType(InputType.TYPE_CLASS_TEXT);
                            break;
                    }
                    mBarcodeTypeLayout.setVisibility(View.VISIBLE);
                    mBtnPrnBarcode.setVisibility(View.VISIBLE);
                } else {
                    printInfo.setInputType(InputType.TYPE_CLASS_TEXT);
                    mBarcodeTypeLayout.setVisibility(View.GONE);
                    mBtnPrnBarcode.setVisibility(View.GONE);
                }
                isPrintText = !isChecked;
            }
        });

        mSpinerThreshold = (Spinner) findViewById(R.id.spinner_threshold);
        mThresholdAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTempThresholdTable);
        mThresholdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinerThreshold.setAdapter(mThresholdAdapter);
        mSpinerThreshold.setOnItemSelectedListener(new SpinnerSelectedListener());

        if (!mIs319) {
            mLlFactoryTest = (LinearLayout) findViewById(R.id.ll_factoryTest);
            mLlFactoryTest.setVisibility(View.GONE);
        }

        mBarcodeType = (Spinner) findViewById(R.id.spinner_barcode);
        ArrayAdapter mBarcodeTypeAdapter = ArrayAdapter.createFromResource(
                PrinterManagerActivity.this, R.array.barcode_type,
                android.R.layout.simple_spinner_item);

        mBarcodeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBarcodeType.setAdapter(mBarcodeTypeAdapter);

        mBarcodeType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                Log.i("printer", "------- position -------" + position);
                mBarcodeTypeValue = Integer.parseInt(mBarTypeTable[position]);
                Log.i("printer", "------- mBarcodeTypeValue -------" + mBarcodeTypeValue);

                switch (mBarcodeTypeValue) {
                    case 34:// UPCA, no., UPCA needs short length of No.
                        //case 2:// Chinese25MATRIX, no.
                    case 3:// Chinese25INTER, no.
                    case 29:// RSS14, no.
                        printInfo.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case 20:// CODE128, alphabet + no.
                    case 25:// CODE93, alphabet + no.
                    case 55:// PDF417, setHue: 3
                    case 58:// QRCODE
                    case 71:// DATAMATRIX
                    case 84:// uPDF417
                    case 92:// AZTEC
                        printInfo.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (printer == null) {
            printer = new PrinterManager();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPrtReceiver);
        writeSpinnerPrefsState(this);
    }

    private boolean hasChineseChar(String text) {
        boolean hasChar = false;
        int length = text.length();
        int byteSize = text.getBytes().length;

        hasChar = (length != byteSize);

        return hasChar;
    }

    void doprintwork(String msg) {
        if (checkTempThreshold())
            return;

        Intent intentService = new Intent(this, PrintBillService.class);
        intentService.putExtra("SPRT", msg);
        if (isPrintText) {
            intentService.putExtra("font-info", mFontStylePanel.getFontInfo());
        }

        startService(intentService);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PRNT_ACTION);
        registerReceiver(mPrtReceiver, filter);

        readSpinnerPrefsState(this);
        mSpinerThreshold.setSelection(mSpinnerSelectPosition);

        mBtnPrnBill.setEnabled(true);
        mBtnPrnPic.setEnabled(true);
        mBtnPrnBarcode.setEnabled(true);
        mBtnForWard.setEnabled(true);
        mBtnBack.setEnabled(true);
    }

    void doPrint(int type) {
        Intent intent = new Intent("urovo.prnt.message");
        if (checkTempThreshold())
            return;
        int ret = printer.prn_getStatus();
        if (ret == 0) {
            printer.prn_setupPage(384, -1);
            switch (type) {
                case 1:
                    String text = printInfo.getText().toString();
                    Log.i("printer", "----------- text ---------- " + text);
                    switch (mBarcodeTypeValue) {
                        case 20:// CODE128, alphabet + no.
                        case 25:// CODE93, alphabet + no.
                            if (text.toString().matches("^[A-Za-z0-9]+$")) {
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 0);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 1);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 2);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 3);
                            } else {
                                Toast.makeText(
                                        this.getApplicationContext(),
                                        "Not support for Chinese code!!!",
                                        Toast.LENGTH_SHORT).show();
                                printInfo.requestFocus();
                                intent.putExtra("ret", ret);
                                sendBroadcast(intent);
                                return;
                            }
                            break;
                        case 34:// UPCA, no., UPCA needs short length of No.
                            //case 2:// Chinese25MATRIX, no.
                            if (Convert.isNumeric(text, this.getApplicationContext())) {
                                //        		 			printer.prn_drawBarcode(text, 50, 10, mBarcodeTypeValue, 2, 70, 0);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 0);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 1);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 2);
                                printer.prn_drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 3);
                            } else {
                                Toast.makeText(
                                        this.getApplicationContext(),
                                        "Not support for non-numeric!!!",
                                        Toast.LENGTH_SHORT).show();

                                printInfo.requestFocus();
                                intent.putExtra("ret", ret);
                                sendBroadcast(intent);
                                return;
                            }
                            break;

                        case 3:// Chinese25INTER, no.
                        case 29:// RSS14, no.
                            if (Convert.isNumeric(text, this.getApplicationContext())) {
                                printer.prn_drawBarcode(text, 50, 10, mBarcodeTypeValue, 2, 40, 0);
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Not support for non-numeric!!!",
                                        Toast.LENGTH_SHORT).show();

                                printInfo.requestFocus();
                                intent.putExtra("ret", ret);
                                sendBroadcast(intent);
                                return;
                            }
                            break;
                        case 55:// PDF417, setHue: 3
                            printer.prn_drawBarcode(text, 25, 5, mBarcodeTypeValue, 3, 60, 0);
                            break;
                        case 58:// QRCODE
                        case 71:// DATAMATRIX
                            printer.prn_drawBarcode(text, 50, 10, mBarcodeTypeValue, 8, 120, 0);
                            break;
                        case 84:// uPDF417
                            printer.prn_drawBarcode(text, 25, 5, mBarcodeTypeValue, 4, 60, 0);
                            break;
                        case 92:// AZTEC
                            printer.prn_drawBarcode(text, 50, 10, mBarcodeTypeValue, 8, 120, 0);
                            break;
                    }
                    break;

                case 2:
                    if (mBmpPicture != null) {
                        printer.prn_drawBitmap(mBmpPicture, 30, 0);
                    } else {
                        Toast.makeText(this, "mBmpPicture is null", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 3:
                    printer.prn_drawLine(264, 50, 48, 50, 4);
                    printer.prn_drawLine(156, 0, 156, 120, 2);
                    printer.prn_drawLine(16, 0, 300, 100, 2);
                    printer.prn_drawLine(16, 100, 300, 0, 2);
                    break;
            }

            ret = printer.prn_printPage(0);
            printer.prn_paperForWard(16);
        }

        intent.putExtra("ret", ret);
        sendBroadcast(intent);
    }

    private static final int PHOTO_REQUEST_CODE = 200;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //通过uri的方式返回，部分手机uri可能为空
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //部分手机可能直接存放在bundle中
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            bitmap = bundleExtras.getParcelable("data");
                        }
                    }
                    if (bitmap != null) {
                        mBmpPicture = Bitmap.createScaledBitmap(bitmap,
                                300, 300 * bitmap.getHeight() / bitmap.getWidth(), true);
                    }
                    mHandler.obtainMessage(PRNPIC).sendToTarget();
                }
                break;
        }
    }


    class SpinnerSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(
                AdapterView<?> arg0,
                View arg1, int arg2,
                long arg3) {

            mTempThresholdValue = Integer.parseInt(mTempThresholdTable[arg2]);
            // prepare prefs and write it to files
            mSpinnerSelectPosition = (int) arg3;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // read prefs to restore
    private boolean readSpinnerPrefsState(Context c) {
        SharedPreferences sharedPrefs = c.getSharedPreferences(SPINNER_PREFERENCES_FILE, MODE_PRIVATE);
        mSpinnerSelectPosition = sharedPrefs.getInt(SPINNER_SELECT_POSITION_KEY, DEF_SPINNER_SELECT_POSITION);
        mSpinnerSelectValue = sharedPrefs.getString(SPINNER_SELECT_VAULE_KEY, DEF_SPINNER_SELECT_VAULE);

        return (sharedPrefs.contains(SPINNER_SELECT_POSITION_KEY));
    }

    // write prefs to file for restroing
    private boolean writeSpinnerPrefsState(Context c) {
        SharedPreferences sharedPrefs = c.getSharedPreferences(SPINNER_PREFERENCES_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt(SPINNER_SELECT_POSITION_KEY, mSpinnerSelectPosition);
        editor.putString(SPINNER_SELECT_VAULE_KEY, mSpinnerSelectValue);

        return (editor.commit());
    }

    // return ture if printer's temperature is too high
    private boolean checkTempThreshold() {
        int currentTemp = getCurrentTemp();
        if (currentTemp == PRNSTS_BUSY) {
            Log.e(TAG, "Printer is busy");
            Toast.makeText(getApplicationContext(),
                    R.string.tst_info_busy,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (currentTemp == PRNSTS_OVER_HEAT) {

            Log.e(TAG, "Printer is overheat");
            Toast.makeText(getApplicationContext(),
                    R.string.printer_temp_overheating,
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        mTvTemp.setText(String.valueOf(currentTemp));

        if (mIsTempEnable && currentTemp >= mTempThresholdValue) {
            Log.e(TAG, "Printer temperature meets the Threshold: " + mTempThresholdValue);
            Toast.makeText(getApplicationContext(),
                    R.string.printer_temp_overheating,
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private int getCurrentTemp() {
        if (printer == null) {
            printer = new PrinterManager();
        }

        int currentTempVolt = 0;//printer.prn_getTemp() ;

//		Log.d("printer", "---------currentTempVolt---------" + currentTempVolt);

        String tmp = String.valueOf(currentTempVolt);
        // get first 4# or first 3#
        if (tmp.length() >= 4) {
            if (tmp.length() == 4 || tmp.length() == 6) {        // when temperature equals 80
                currentTempVolt = Integer.parseInt(tmp.substring(0, 3));
            } else {
                currentTempVolt = Integer.parseInt(tmp.substring(0, 4));
            }
        }

//		Log.d("printer", "getCurrentTemp =============== " + currentTempVolt);
        if (currentTempVolt < 0)
            return currentTempVolt;
        return voltToTemp(mVoltTempPair, currentTempVolt);
    }

    private int voltToTemp(int[][] table, int voltValue) {
        int left_side = 0;
        int right_side = table.length - 1;
        int mid;

        int realTemp = 0;

        while (left_side <= right_side) {
            mid = (left_side + right_side) / 2;

            if (mid == 0 || mid == table.length - 1 ||
                    (table[mid][0] <= voltValue && table[mid + 1][0] > voltValue)) {
                realTemp = table[mid][1];
                break;
            } else if (voltValue - table[mid][0] > 0)
                left_side = mid + 1;
            else
                right_side = mid - 1;
        }

        return realTemp;
    }

    private final int PRNPIC = 1;
    private final int BARCOD = 2;
    private final int FORWARD = 3;

    private Handler mHandler;

    class CustomThread extends Thread {
        @Override
        public void run() {
            //建立消息循环的步骤
            Looper.prepare();//1、初始化Looper
            mHandler = new Handler() {//2、绑定handler到CustomThread实例的Looper对象
                public void handleMessage(Message msg) {//3、定义处理消息的方法
                    switch (msg.what) {
                        case PRNPIC:
                            doPrint(2);
                            break;

                        case BARCOD:
                            doPrint(1);
                            break;

                        case FORWARD:
                            printer.prn_paperForWard(4);
                            Intent intent = new Intent("urovo.prnt.message");
                            intent.putExtra("ret", 100);
                            sendBroadcast(intent);
                            break;
                    }
                }
            };
            Looper.loop();//4、启动消息循环
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
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
}
