/*
 * Copyright (C) 2020 Urovo Technologies Corp.
 */

package com.example.printersample;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

public class PrinterManagerActivity extends AppCompatActivity {
    private static final String TAG = "PrinterManagerActivity";
    private Button mBtnPrnText;   //Printed text
    private Button mBtnPrnBitmap;   //print pictures
    private Button mBtnPrnBarcode;   //Print bar code
    private Button mBtnForWard;   //Forward (paper feed)
    private EditText printInfo;   //Info to print

    PrinterManager mPrinterManager;

    private FontStylePanel mFontStylePanel;   //Font formatting

    private static final String[] mBarTypeTable = {
            "3", "20", "25",
            "29", "34", "55", "58",
            "71", "84", "92",
    };

    private Spinner mBarcodeType;
    private int mBarcodeTypeValue;   //Barcode Type

    //Printer gray value 0-4
    private final static int DEF_PRINTER_HUE_VALUE = 0;
    private final static int MIN_PRINTER_HUE_VALUE = 0;
    private final static int MAX_PRINTER_HUE_VALUE = 4;

    //Print speed value 0-9
    private final static int DEF_PRINTER_SPEED_VALUE = 9;
    private final static int MIN_PRINTER_SPEED_VALUE = 0;
    private final static int MAX_PRINTER_SPEED_VALUE = 9;

    // Printer status
    private final static int PRNSTS_OK = 0;                //OK
    private final static int PRNSTS_OUT_OF_PAPER = -1;    //Out of paper
    private final static int PRNSTS_OVER_HEAT = -2;        //Over heat
    private final static int PRNSTS_UNDER_VOLTAGE = -3;    //under voltage
    private final static int PRNSTS_BUSY = -4;            //Device is busy
    private final static int PRNSTS_ERR = -256;            //Common error
    private final static int PRNSTS_ERR_DRIVER = -257;    //Printer Driver error

    private Button mBtSetHue;
    private EditText mEtHue;
    private Button mBtSetSpeed;
    private EditText mEtSpeed;
    private int mPrinterHue = DEF_PRINTER_HUE_VALUE;
    private int mPrinterSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new CustomThread().start();   //Start printer worker

        mBtnPrnText = (Button) findViewById(R.id.btn_prnBill);
        printInfo = (EditText) findViewById(R.id.printer_info);

        //Set printer gray value
        mEtHue = (EditText) findViewById(R.id.et_hue);
        mBtSetHue = (Button) findViewById(R.id.bt_set_hue);
        mBtSetHue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isNumeric(mEtHue.getText().toString())) {
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

                Log.d(TAG, "---------set hue = " + mPrinterHue);
                getPrinterManager().setGrayLevel(mPrinterHue);
                mEtHue.setText(String.valueOf(mPrinterHue));
            }
        });

        //Set printer speed value
        mEtSpeed = (EditText) findViewById(R.id.et_speed);
        mBtSetSpeed = (Button) findViewById(R.id.bt_set_speed);
        mBtSetSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isNumeric(mEtSpeed.getText().toString())) {
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

                Log.d(TAG, "---------set PrinterSpeed = " + mPrinterSpeed);
                getPrinterManager().setSpeedLevel(mPrinterSpeed);
                mEtSpeed.setText(String.valueOf(mPrinterSpeed));
            }
        });

        //Printed text
        mBtnPrnText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPrnBitmap.setEnabled(false);
                mBtnPrnBarcode.setEnabled(false);
                mBtnForWard.setEnabled(false);
                mBtnPrnText.setEnabled(false);
                String content = printInfo.getText().toString();
                if (content == null || content.equals("")) {
                    content = "Print test content!\n" + "0123456789\n" + "abcdefghijklmnopqrstuvwsyz\n" + "ABCDEFGHIJKLMNOPQRSTUVWSYZ";
                }

                Message msg = mPrintHandler.obtainMessage(PRINT_TEXT);
                msg.obj = content;
                msg.sendToTarget();
            }
        });

        //Forward (paper feed)
        mBtnForWard = (Button) findViewById(R.id.btn_FORWARD);
        mBtnForWard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPrnBitmap.setEnabled(false);
                mBtnPrnBarcode.setEnabled(false);
                mBtnForWard.setEnabled(false);
                mBtnPrnText.setEnabled(false);

                mPrintHandler.obtainMessage(PRINT_FORWARD).sendToTarget();
            }
        });

        //print pictures
        mBtnPrnBitmap = (Button) findViewById(R.id.btn_prnPicture);
        mBtnPrnBitmap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PrinterManagerActivity.this)
                        .setTitle(R.string.tst_info_select_picture)
                        .setMessage(R.string.tst_info_select_picture_msg)
                        .setNegativeButton(R.string.mci_select_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnBitmap.setEnabled(false);
                                mBtnPrnBarcode.setEnabled(false);
                                mBtnForWard.setEnabled(false);
                                mBtnPrnText.setEnabled(false);

                                //Start the picture selector, select the picture and continue processing in onactivityresult
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                            }
                        })
                        .setPositiveButton(R.string.mci_select_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnBitmap.setEnabled(false);
                                mBtnPrnBarcode.setEnabled(false);
                                mBtnForWard.setEnabled(false);
                                mBtnPrnText.setEnabled(false);

                                //Print a default picture
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                opts.inDensity = getResources().getDisplayMetrics().densityDpi;
                                opts.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hcp, opts);
                                Message msg = mPrintHandler.obtainMessage(PRINT_BITMAP);
                                msg.obj = bitmap;
                                msg.sendToTarget();
                            }
                        })
                        .create()
                        .show();
            }
        });

        //Print bar code
        mBtnPrnBarcode = (Button) findViewById(R.id.btn_barcode);
        mBtnPrnBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String messgae = printInfo.getText().toString();
                if (messgae.length() > 0) {
                    mBtnPrnBitmap.setEnabled(false);
                    mBtnPrnBarcode.setEnabled(false);
                    mBtnForWard.setEnabled(false);
                    mBtnPrnText.setEnabled(false);

                    Message msg = mPrintHandler.obtainMessage(PRINT_BARCOD);
                    msg.obj = messgae;
                    msg.sendToTarget();
                } else {
                    Toast.makeText(
                            PrinterManagerActivity.this, R.string.tst_hint_content, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFontStylePanel = new FontStylePanel(this);   //Font formatting


        //Barcode format setting
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
                Log.i("mPrinterManager", "------- position -------" + position);
                mBarcodeTypeValue = Integer.parseInt(mBarTypeTable[position]);
                Log.i("mPrinterManager", "------- mBarcodeTypeValue -------" + mBarcodeTypeValue);

                switch (mBarcodeTypeValue) {
                    case 34:  // UPCA, no., UPCA needs short length of No.
                        //case 2:  // Chinese25MATRIX, no.
                    case 3:  // Chinese25INTER, no.
                    case 29:  // RSS14, no.
                        printInfo.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case 20:  // CODE128, alphabet + no.
                    case 25:  // CODE93, alphabet + no.
                    case 55:  // PDF417, setHue: 3
                    case 58:  // QRCODE
                    case 71:  // DATAMATRIX
                    case 84:  // uPDF417
                    case 92:  // AZTEC
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

    //Instantiate printermanager
    private PrinterManager getPrinterManager() {
        if (mPrinterManager == null) {
            mPrinterManager = new PrinterManager();
            mPrinterManager.open();
        }
        return mPrinterManager;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mPrinterManager != null) {
            mPrinterManager.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        mBtnPrnText.setEnabled(true);
        mBtnPrnBitmap.setEnabled(true);
        mBtnPrnBarcode.setEnabled(true);
        mBtnForWard.setEnabled(true);
    }

    private static final int PHOTO_REQUEST_CODE = 200;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_REQUEST_CODE:  //After selecting a picture from the picture selector, continue printing the picture
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //Return by URI
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Some devices may be stored directly in bundles
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            bitmap = bundleExtras.getParcelable("data");
                        }
                    }
                    if (bitmap != null) {
                        bitmap = Bitmap.createScaledBitmap(bitmap,
                                300, 300 * bitmap.getHeight() / bitmap.getWidth(), true);   //Zoom picture size
                        Message msg = mPrintHandler.obtainMessage(PRINT_BITMAP);
                        msg.obj = bitmap;
                        msg.sendToTarget();
                    }
                }
                break;
        }
    }

    /**
     * Execution printing
     * To print data with this class, use the following steps:
     * Obtain an instance of Printer with PrinterManager printer = new PrinterManager().
     * Call setupPage(int, int) to initialize the page size.
     * If necessary, append a line in the current page with drawLine(int , int , int , int , int ).
     * If necessary, append text in the current page with drawTextEx(String , int, int , int , int , String ,int , int , int , int ).
     * If necessary, append barcode data in the current page with drawBarcode(String , int , int , int ,int , int , int ).
     * If necessary, append picture data in the current page with drawBitmap(Bitmap , int , int ).
     * To begin print the current page session, call printPage(int).
     *
     * @param printerManager printerManager
     * @param type           PRINT_TEXT PRINT_BITMAP PRINT_BARCOD PRINT_FORWARD
     * @param content        content
     */
    private void doPrint(PrinterManager printerManager, int type, Object content) {

        int ret = printerManager.getStatus();   //Get printer status
        if (ret == PRNSTS_OK) {
            printerManager.setupPage(384, -1);   //Set paper size
            switch (type) {
                case PRINT_TEXT:
                    Bundle fontInfo = mFontStylePanel.getFontInfo();   //Get font format
                    int fontSize = 24;
                    int fontStyle = 0x0000;
                    String fontName = "simsun";
                    if (fontInfo != null) {
                        fontSize = fontInfo.getInt("font-size", 24);
                        fontStyle = fontInfo.getInt("font-style", 0);
                        fontName = fontInfo.getString("font-name", "simsun");
                    }

                    int height = 0;
                    String[] texts = ((String) content).split("\n");   //Split print content into multiple lines
                    for (String text : texts) {
                        height += printerManager.drawText(text, 0, height, fontName, fontSize, false, false, 0);   //Printed text
                    }
                    for (String text : texts) {
                        height += printerManager.drawTextEx(text, 5, height, 384, -1, fontName, fontSize, 0, fontStyle, 0);   ////Printed text
                    }
                    height = 0;
                    break;
                case PRINT_BARCOD:
                    String text = (String) content;
                    Log.i("mPrinterManager", "----------- text ---------- " + text);
                    //According to the printed content and barcode type
                    switch (mBarcodeTypeValue) {
                        case 20:  // CODE128, alphabet + no.
                        case 25:  // CODE93, alphabet + no.
                            if (text.toString().matches("^[A-Za-z0-9]+$")) {
                                printerManager.drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 0);   //Print bar code
                            } else {
                                Toast.makeText(
                                        this.getApplicationContext(),
                                        "Not support for Chinese code!!!",
                                        Toast.LENGTH_SHORT).show();
                                printInfo.requestFocus();
                                updatePrintStatus(ret);
                                return;
                            }
                            break;
                        case 34:  // UPCA, no., UPCA needs short length of No.
                            //case 2:// Chinese25MATRIX, no.
                            if (isNumeric(text)) {
                                printerManager.drawBarcode(text, 196, 300, mBarcodeTypeValue, 2, 70, 0);   //Print bar code
                            } else {
                                Toast.makeText(
                                        this.getApplicationContext(),
                                        "Not support for non-numeric!!!",
                                        Toast.LENGTH_SHORT).show();

                                printInfo.requestFocus();
                                updatePrintStatus(ret);
                                return;
                            }
                            break;

                        case 3:  // Chinese25INTER, no.
                        case 29:  // RSS14, no.
                            if (isNumeric(text)) {
                                printerManager.drawBarcode(text, 50, 10, mBarcodeTypeValue, 2, 40, 0);   //Print bar code
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Not support for non-numeric!!!",
                                        Toast.LENGTH_SHORT).show();

                                printInfo.requestFocus();
                                updatePrintStatus(ret);
                                return;
                            }
                            break;
                        case 55:  // PDF417, setHue: 3
                            printerManager.drawBarcode(text, 25, 5, mBarcodeTypeValue, 3, 60, 0);   //Print bar code
                            break;
                        case 58:  // QRCODE
                        case 71:  // DATAMATRIX
                            printerManager.drawBarcode(text, 50, 10, mBarcodeTypeValue, 8, 120, 0);   //Print bar code
                            break;
                        case 84:  // uPDF417
                            printerManager.drawBarcode(text, 25, 5, mBarcodeTypeValue, 4, 60, 0);   //Print bar code
                            break;
                        case 92:  // AZTEC
                            printerManager.drawBarcode(text, 50, 10, mBarcodeTypeValue, 8, 120, 0);   //Print bar code
                            break;
                    }
                    break;

                case PRINT_BITMAP:
                    Bitmap bitmap = (Bitmap) content;
                    if (bitmap != null) {
                        printerManager.drawBitmap(bitmap, 30, 0);  //print pictures
                    } else {
                        Toast.makeText(this, "Picture is null", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            ret = printerManager.printPage(0);  //Execution printing
            printerManager.paperFeed(16);  //paper feed
        }

        updatePrintStatus(ret);
    }

    private final int PRINT_TEXT = 0;   //Printed text
    private final int PRINT_BITMAP = 1;   //print pictures
    private final int PRINT_BARCOD = 2;   //Print bar code
    private final int PRINT_FORWARD = 3;   //Forward (paper feed)

    private Handler mPrintHandler;

    class CustomThread extends Thread {
        @Override
        public void run() {
            //To create a message loop
            Looper.prepare();   //1.Initialize looper
            mPrintHandler = new Handler() {   //2.Bind handler to looper object of customthread instance
                public void handleMessage(Message msg) {   //3.Define how messages are processed
                    switch (msg.what) {
                        case PRINT_TEXT:
                        case PRINT_BITMAP:
                        case PRINT_BARCOD:
                            doPrint(getPrinterManager(), msg.what, msg.obj);   //Print
                            break;
                        case PRINT_FORWARD:
                            getPrinterManager().paperFeed(20);
                            updatePrintStatus(100);
                            break;
                    }
                }
            };
            Looper.loop();   //4.Start message loop
        }
    }

    //Update printer status, toast reminder in case of exception
    private void updatePrintStatus(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status == PRNSTS_OUT_OF_PAPER) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_paper,
                            Toast.LENGTH_SHORT).show();
                } else if (status == PRNSTS_OVER_HEAT) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_temperature,
                            Toast.LENGTH_SHORT).show();
                } else if (status == PRNSTS_UNDER_VOLTAGE) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_voltage,
                            Toast.LENGTH_SHORT).show();
                } else if (status == PRNSTS_BUSY) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_busy,
                            Toast.LENGTH_SHORT).show();
                } else if (status == PRNSTS_ERR) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_error,
                            Toast.LENGTH_SHORT).show();
                } else if (status == PRNSTS_ERR_DRIVER) {
                    Toast.makeText(
                            PrinterManagerActivity.this,
                            R.string.tst_info_driver_error,
                            Toast.LENGTH_SHORT).show();
                }
                mBtnPrnBarcode.setEnabled(true);
                mBtnForWard.setEnabled(true);
                mBtnPrnText.setEnabled(true);
                mBtnPrnBitmap.setEnabled(true);
            }
        });
    }


    public static boolean isNumeric(String string) {
        if (string != null && !string.equals("") && string.matches("\\d*")) {
            if (String.valueOf(Integer.MAX_VALUE).length() < string.length()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
