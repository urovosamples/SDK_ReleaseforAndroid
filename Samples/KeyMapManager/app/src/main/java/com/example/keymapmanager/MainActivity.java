package com.example.keymapmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.device.KeyMapManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private Context mContext;
    private KeyEvent mCurrentKeyEvent;
    EditText editText;
    String editResult = "";
    KeyMapManager mkeyMap;
    CheckBox interception = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mkeyMap = new KeyMapManager(mContext);
        Button hasKeyEntry = (Button) findViewById(R.id.button1);
        Button delKeyEntry = (Button) findViewById(R.id.button2);
        Button mapKeyEntry = (Button) findViewById(R.id.button3);
        interception = (CheckBox) findViewById(R.id.checkbox1);
        Button isInterception = (Button) findViewById(R.id.button4);
        Button getKeyCode = (Button) findViewById(R.id.button5);
        Button getKeyMeta = (Button) findViewById(R.id.button6);
        Button getkeyaction = (Button) findViewById(R.id.button7);
        Button getKeyType = (Button) findViewById(R.id.button8);
        Button getKeyList = (Button) findViewById(R.id.button9);

        //KeyMapManager.hasKeyEntry()
        hasKeyEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {   //Judge whether the value entered in the dialog box is empty
                            return;
                        } else {
                            boolean result = mkeyMap.hasKeyEntry(Integer.parseInt(editResult));
                            if (result == true) {
                                Toast.makeText(mContext, "Key mapped", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Key not mapped", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        //KeyMapManager.delKeyEntry()
        delKeyEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {
                            return;
                        } else {
                            mkeyMap.delKeyEntry(Integer.parseInt(editResult));
                            Toast.makeText(mContext, "The mapping for this key has been removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //KeyMapManager.mapKeyEntry()
        mapKeyEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentKeyEvent == null) {
                    Toast.makeText(mContext, "Please press the button！", Toast.LENGTH_SHORT).show();
                } else {
                    mkeyMap.mapKeyEntry(mCurrentKeyEvent, KeyMapManager.KEY_TYPE_KEYCODE, String.valueOf(KeyEvent.KEYCODE_VOLUME_DOWN));
                    Toast.makeText(mContext, "Mapping success", Toast.LENGTH_SHORT).show();
                }
            }
        });

        interception.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.DEVICE_KEYBOARD_SETTINGS");
                startActivity(intent);
            }
        });

        //KeyMapManager.isInterception()
        isInterception.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mkeyMap.isInterception()) {
                    Toast.makeText(mContext, "System remapping started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "System remapping is not started", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //KeyMapManager.getKeyCode()
        getKeyCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {
                            return;
                        } else {
                            try {
                                int result = mkeyMap.getKeyCode(Integer.parseInt(editResult));
                                Toast.makeText(mContext, "" + result, Toast.LENGTH_SHORT).show();
                            } catch (NoSuchMethodError e) {
                                Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        //KeyMapManager.getKeyMeta()
        getKeyMeta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {
                            return;
                        } else {
                            try {
                                int result = mkeyMap.getKeyMeta(Integer.parseInt(editResult));
                                Toast.makeText(mContext, "" + result, Toast.LENGTH_SHORT).show();
                            } catch (NoSuchMethodError e) {
                                Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        //KeyMapManager.getKeyAction()
        getkeyaction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {
                            return;
                        } else {
                            try {
                                String result = mkeyMap.getKeyAction(Integer.parseInt(editResult));
                                Toast.makeText(mContext, "" + result, Toast.LENGTH_SHORT).show();
                            } catch (NoSuchMethodError e) {
                                Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        //KeyMapManager.getKeyType()
        getKeyType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editResult = editText.getText().toString();
                        if (editResult.equals("")) {
                            return;
                        } else {
                            try {
                                int result = mkeyMap.getKeytype(Integer.parseInt(editResult));
                                Toast.makeText(mContext, "" + result, Toast.LENGTH_SHORT).show();
                            } catch (NoSuchMethodError e) {
                                Toast.makeText(MainActivity.this, "OS does not implement this method!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        //KeyMapManager.getKeyList()
        getKeyList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.Intent.action.KEY_REMAP_LIST");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mCurrentKeyEvent = event;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mCurrentKeyEvent = event;
        return super.onKeyUp(keyCode, event);
    }

    public void showEditDialog(DialogInterface.OnClickListener listener) {
        editResult = "";
        editText = new EditText(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Please enter the key value").setView(editText)
                .setPositiveButton("OK", listener);
        builder.create().show();
    }

    @Override
    public void onResume() {
        interception.setChecked(mkeyMap.isInterception());
        super.onResume();
    }
}
