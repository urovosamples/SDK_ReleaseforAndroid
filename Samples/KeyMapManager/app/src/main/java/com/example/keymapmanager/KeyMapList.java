package com.example.keymapmanager;

import android.app.Activity;
import android.content.Context;
import android.device.KeyMapManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class KeyMapList extends Activity {

    KeyMapManager mkeymap;
    private ListView mlistView;
    private List<KeyMapManager.KeyEntry> mKeyList = null;
    private List<String> stringList = null;
    private ArrayAdapter<String> mArrayAdapter;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keymaplist);
        mContext = getApplicationContext();
        mlistView = findViewById(R.id.keylist);
        mkeymap = new KeyMapManager(mContext);
        stringList = new ArrayList<String>();

        mKeyList = mkeymap.getKeyList();
        if (mKeyList.size() == 0) {
            Toast.makeText(mContext, "The list is empty", Toast.LENGTH_SHORT).show();
        } else {
            for (KeyMapManager.KeyEntry keyEntry : mKeyList) {
                String sKey = "keycode:" + keyEntry.keycode + "\n" +
                        "keycode_meta:" + keyEntry.keycode_meta + "\n" +
                        "type:" + keyEntry.type + "";
                stringList.add(sKey);
            }
            mArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, stringList);
            mlistView.setAdapter(mArrayAdapter);
        }
    }
}
