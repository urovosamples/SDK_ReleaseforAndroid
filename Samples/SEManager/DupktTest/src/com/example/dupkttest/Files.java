
package com.example.dupkttest;

import android.content.Context;

import java.io.File;

public class Files {

    public static String logFile = "";

    public static String errFile = "";

    public static String logFileData = "/sdcard/BCMLog/";

    private static boolean isTrue;

    public static void createFile(Context context) {
        try {

            logFile = context.getFilesDir() + "log/";
            errFile = context.getFilesDir() + "err/";
            logFileData = context.getFilesDir() + "files/";
            File files = new File(logFile);
            File errfiles = new File(errFile);
            File logFileDatas = new File(logFileData);
            if (!files.exists()) {
                files.mkdirs();
            }
            if (!errfiles.exists()) {
                errfiles.mkdirs();
            }

            if (!logFileDatas.exists()) {
                logFileDatas.mkdirs();
            }
            String cmd = "chmod 777 " + files.getAbsolutePath();
            String cmd2 = "chmod 777 " + errfiles.getAbsolutePath();

            Runtime.getRuntime().exec(cmd);
            Runtime.getRuntime().exec(cmd2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
