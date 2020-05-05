package com.example.dupkttest;

import android.util.Log;

//import com.urovo.smartpos.deviceservice.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class logfile {
    public static String log = "log";
    public static int maxLength = 65535 * 50;
    public static int i = 1;
    public static int maxNum = 5;

    public static String dataFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();
        return sdf.format(date);
    }

    public static String dataFormat(String format, long currMiss) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date(currMiss);
        return sdf.format(date);
    }

    private static byte abcd_to_asc(byte ucBcd) {
        byte ucAsc;

        ucBcd &= 0x0f;
        if (ucBcd <= 9)
            ucAsc = (byte) (ucBcd + (byte) ('0'));
        else
            ucAsc = (byte) (ucBcd + (byte) ('A') - (byte) 10);
        return (ucAsc);
    }

    private static void BcdToAsc(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
        int i, j;

        j = 0;
        for (i = 0; i < iAscLen / 2; i++) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 0xf0) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            j++;
            sAscBuf[j] = (byte) (sBcdBuf[i] & 0x0f);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            j++;
        }
        if ((iAscLen % 2) != 0) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 0xf0) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
        }
    }

    public static void printLogHex(String writeStr, byte[] BCD, int len) {
        byte[] ASC = new byte[len * 2];
        BcdToAsc(ASC, BCD, len * 2);
        String str = new String(ASC);
        printLog(writeStr + str);
    }

    public static void printLog(String writeStr) {

        boolean bFlag = false;
        if (bFlag == true) {
            return;
        }
        Log.e("UROVO_printLog", "" + writeStr);
        File dir = new File(Files.logFileData);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            String lastTime = dataFormat("yyyy-MM-dd",
                    files[files.length - 1].lastModified());
            if (!lastTime.equals(dataFormat("yyyy-MM-dd"))) {
                i = 1;
            }
        }
        String logFile = Files.logFileData + dataFormat("yyyy-MM-dd")
                + "log" + i + ".txt";
        File file = new File(logFile);
        if (!file.exists()) {
            try {

                if (files != null && files.length >= maxNum) {
                    files[0].delete();
                }
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String wirteString = "[ " + dataFormat("yyyy-MM-dd HH:mm:ss") + " ] "
                + " " + writeStr;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, true);
            byte[] bytes = (wirteString + "\r\n").getBytes();

            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (file.length() >= maxLength) {
            i++;
        }
    }

    public static boolean isFirstRun() {

        boolean blRet = false;
        File dir = new File("/sdcard/CCBLog/First/");
        if (!dir.exists()) {
            dir.mkdirs();
            blRet = true;
        } else {
            blRet = false;
        }
        return blRet;
    }
}
