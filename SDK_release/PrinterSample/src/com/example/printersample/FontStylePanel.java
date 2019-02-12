package com.example.printersample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

public class FontStylePanel {

    private static final String FONT_NAME  = "font-name";
    private static final String FONT_SIZE  = "font-size";
    private static final String FONT_STYLE = "font-style";

    private static final int FONT_STYLE_NULL      = 0x0000;
    private static final int FONT_STYLE_BOLD      = 0x0001;
    private static final int FONT_STYLE_ITALIC    = 0x0002;
    private static final int FONT_STYLE_UNDERLINE = 0x0004;
    private static final int FONT_STYLE_REVERSE   = 0x0008;
    private static final int FONT_STYLE_STRIKEOUT = 0x0010;

    private Activity mContext;
    private Spinner mNameSpinner;
    private Spinner mSizeSpinner;
    private ImageButton mBoldButton;
    private ImageButton mItalicButton;
    private ImageButton mUnderlineButton;
    private ImageButton mStrikeoutButton;

    private String mFontName = "simsun";
    private int mFontSize = 24;

    private boolean isTextBold = false;
    private boolean isTextItalic = false;
    private boolean isTextUnderline = false;
    private boolean isTextStrikeout = false;
    private int mFontStyle = FONT_STYLE_NULL;

    private String[] mFontNames;
    private String[] mFontSizes;

    public FontStylePanel(Activity context) {
        mContext = context;

        mFontNames = mContext.getResources().getStringArray(R.array.font_name);
        mFontSizes = mContext.getResources().getStringArray(R.array.font_size);

        mNameSpinner = (Spinner) mContext.findViewById(R.id.spinner_font);
        ArrayAdapter mNameAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mFontNames);
        mNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNameSpinner.setAdapter(mNameAdapter);
        mNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    mFontName = mFontNames[i];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSizeSpinner = (Spinner) mContext.findViewById(R.id.spinner_size);
        ArrayAdapter mSizeAdapter = new  ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mFontSizes);
        mSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSizeSpinner.setAdapter(mSizeAdapter);
        mSizeSpinner.setSelection(4);
        mSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    mFontSize = Integer.parseInt(mFontSizes[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mBoldButton = (ImageButton) mContext.findViewById(R.id.btn_Bold);
        mBoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextBold) {
                    mBoldButton.setBackgroundResource(R.drawable.bold);
                } else {
                    mBoldButton.setBackgroundResource(R.drawable.bold_);
                }
                isTextBold = !isTextBold;
            }
        });

        mItalicButton = (ImageButton) mContext.findViewById(R.id.btn_Italic);
        mItalicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextItalic) {
                    mItalicButton.setBackgroundResource(R.drawable.italic);
                } else {
                    mItalicButton.setBackgroundResource(R.drawable.italic_);
                }
                isTextItalic = !isTextItalic;
            }
        });

        mUnderlineButton = (ImageButton) mContext.findViewById(R.id.btn_Underline);
        mUnderlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextUnderline) {
                    mUnderlineButton.setBackgroundResource(R.drawable.underline);
                } else {
                    mUnderlineButton.setBackgroundResource(R.drawable.underline_);
                }
                isTextUnderline = !isTextUnderline;
            }
        });

        mStrikeoutButton = (ImageButton) mContext.findViewById(R.id.btn_Strikeout);
        mStrikeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextStrikeout) {
                    mStrikeoutButton.setBackgroundResource(R.drawable.strikeout);
                } else {
                    mStrikeoutButton.setBackgroundResource(R.drawable.strikeout_);
                }
                isTextStrikeout = !isTextStrikeout;
            }
        });
    }

    public Bundle getFontInfo() {
        mFontStyle = FONT_STYLE_NULL;
        if (isTextBold) {
            mFontStyle |= FONT_STYLE_BOLD;
        }
        if (isTextItalic) {
            mFontStyle |= FONT_STYLE_ITALIC;
        }
        if (isTextUnderline) {
            mFontStyle |= FONT_STYLE_UNDERLINE;
        }
        if (isTextStrikeout) {
            mFontStyle |= FONT_STYLE_STRIKEOUT;
        }

        Bundle fontInfo = new Bundle();
        fontInfo.putString(FONT_NAME, mFontName);
        fontInfo.putInt(FONT_SIZE, mFontSize);
        fontInfo.putInt(FONT_STYLE, mFontStyle);

        return fontInfo;
    }

}
