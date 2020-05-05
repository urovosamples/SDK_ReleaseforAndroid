package com.example.dupkttest;
/*
 Source code recreated from a .class file by IntelliJ IDEA
 (powered by Fernflower decompiler)
*/

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Funs {
    public static boolean BLFirstRun = false;
    public static String pinString = "";
    private static final String STR_FORMAT = "000000";
    public static PackageManager pm;
    public static Context context;
    public static String FilePath = "";
    public static String debuglogfilename = "";

    public Funs() {
    }

    public static String[] GetMagCardStr(byte[] Tracks) {
        String[] Str = new String[3];
        int len = Tracks.length;

        for (int i = 0; i < len; ++i) {
            byte[] Btrack2;
            short var6;
            if (Tracks[i] == -47) {
                ++i;
                var6 = Tracks[i];
                if (var6 > 512) {
                    var6 = 512;
                }

                Btrack2 = new byte[var6];
                ++i;
                System.arraycopy(Tracks, i, Btrack2, 0, var6);
                Str[0] = new String(Btrack2);
                i += var6;
            }

            if (Tracks[i] == -46) {
                ++i;
                var6 = Tracks[i];
                if (var6 > 512) {
                    var6 = 512;
                }

                Btrack2 = new byte[var6];
                ++i;
                System.arraycopy(Tracks, i, Btrack2, 0, var6);
                Str[1] = new String(Btrack2);
                i += var6;
            }

            if (Tracks[i] == -45) {
                ++i;
                var6 = Tracks[i];
                if (var6 > 512) {
                    var6 = 512;
                }

                Btrack2 = new byte[var6];
                ++i;
                System.arraycopy(Tracks, i, Btrack2, 0, var6);
                Str[2] = new String(Btrack2);
                int var10000 = i + var6;
                return Str;
            }
        }

        return Str;
    }

    public static byte[] getxor(byte[] a, byte[] b, int len) {
        boolean result = false;

        for (int i = 0; i < len; ++i) {
            a[i] ^= b[i];
        }

        return a;
    }

    public static byte getxor(byte[] a, int index, int len) {
        boolean result = false;
        boolean data2 = false;
        boolean data1 = false;
        int var8 = 255 & a[index];

        for (int i = index + 1; i < index + len; ++i) {
            int var7 = 255 & a[i];
            var8 ^= var7;
        }

        return (byte) (255 & var8);
    }

    public static void SetBytesValue(byte[] B, int len, byte v) {
        boolean i = false;

        for (int var4 = 0; var4 < len; ++var4) {
            Array.setByte(B, var4, v);
        }

    }

    public static void SleepMS(int MS) {
        try {
            Thread.sleep((long) MS);
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }

    }

    public static void do_xor_urovo(byte[] src1, byte[] src2, int num) {
        boolean data2 = false;
        boolean data1 = false;

        for (int i = 0; i < num; ++i) {
            int var7 = 255 & src1[i];
            int var6 = 255 & src2[i];
            var7 ^= var6;
            src1[i] = (byte) (var7 & 255);
        }

    }

    public static String haoAddOne(String serialNumber) {
        Integer intHao = Integer.valueOf(Integer.parseInt(serialNumber));
        DecimalFormat df = new DecimalFormat("000000");
        return df.format(intHao);
    }

    public static String FormatWithZero(String str, String Fmt) {
        try {
            Integer e = Integer.valueOf(Integer.parseInt(str));
            DecimalFormat df = new DecimalFormat(Fmt);
            return df.format(e);
        } catch (Exception var4) {
            return "";
        }
    }

    public static String FormatWithvalue(String str, String value) {
        try {
            int len = value.length();
            if (str.length() >= len) {
                return str;
            } else {
                String temp = "";
                temp = str + value.substring(str.length(), value.length());
                return temp;
            }
        } catch (Exception var4) {
            return str;
        }
    }

    public static String FormatWithvalueR(String str, String value) {
        try {
            int len = value.length();
            if (str.length() >= len) {
                return str;
            } else {
                String temp = "";
                temp = str + value.substring(str.length(), value.length());
                return temp;
            }
        } catch (Exception var4) {
            return str;
        }
    }

    public static String FormatvalueByLeft(String str, String value) {
        try {
            int len = value.length();
            if (str.length() >= len) {
                return str;
            } else {
                String temp = "";
                temp = value.substring(0, value.length() - str.length());
                temp = temp + str;
                return temp;
            }
        } catch (Exception var4) {
            return str;
        }
    }

    public static String GetLocalDatetime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new Date());
        String status = "";
        status = date.substring(0, 4);
        status = status + "/";
        status = status + date.substring(5, 7);
        status = status + "/";
        status = status + date.substring(8, 10);
        status = status + " ";
        status = status + date.substring(11, 13);
        status = status + ":";
        status = status + date.substring(14, 16);
        status = status + ":";
        status = status + date.substring(17, 19);
        return status;
    }

    public static byte abcd_to_asc(byte ucBcd) {
        ucBcd = (byte) (ucBcd & 15);
        byte ucAsc;
        if (ucBcd <= 9) {
            ucAsc = (byte) (ucBcd + 48);
        } else {
            ucAsc = (byte) (ucBcd + 65 - 10);
        }

        return ucAsc;
    }

    public static String IntToAmt(int amt) {
        String StrTrace = "" + amt;
        if (StrTrace.length() == 2) {
            StrTrace = "0." + StrTrace;
        } else if (StrTrace.length() == 1) {
            StrTrace = "0.0" + StrTrace;
        } else {
            String str1 = StrTrace.substring(StrTrace.length() - 2, StrTrace.length());
            String str2 = StrTrace.substring(0, StrTrace.length() - 2);
            StrTrace = str2 + "." + str1;
        }

        return StrTrace;
    }

    public static String StrAmtTOStr(String tranAmt) {
        String szAmt = (new DecimalFormat("0.00")).format(Double.parseDouble(tranAmt));
        szAmt = szAmt.replace(".", "");
        szAmt = FormatWithZero(szAmt, "000000000000");
        return szAmt;
    }

    public static void BcdToAsc(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
        int j = 0;

        int i;
        for (i = 0; i < iAscLen / 2; ++i) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 240) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            ++j;
            sAscBuf[j] = (byte) (sBcdBuf[i] & 15);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            ++j;
        }

        if (iAscLen % 2 != 0) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 240) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
        }
    }

    public static byte aasc_to_bcd(byte ucAsc) {
        byte ucBcd;
        if (ucAsc >= 48 && ucAsc <= 57) {
            ucBcd = (byte) (ucAsc - 48);
        } else if (ucAsc >= 65 && ucAsc <= 70) {
            ucBcd = (byte) (ucAsc - 65 + 10);
        } else if (ucAsc >= 97 && ucAsc <= 102) {
            ucBcd = (byte) (ucAsc - 97 + 10);
        } else if (ucAsc > 57 && ucAsc <= 63) {
            ucBcd = (byte) (ucAsc - 48);
        } else {
            ucBcd = 15;
        }

        return ucBcd;
    }

    public static void AscToBcd(byte[] sBcdBuf, byte[] sAscBuf, int iAscLen) {
        int j = 0;

        for (int i = 0; i < (iAscLen + 1) / 2; ++i) {
            sBcdBuf[i] = (byte) (aasc_to_bcd(sAscBuf[j++]) << 4);
            if (j >= iAscLen) {
                sBcdBuf[i] = (byte) (sBcdBuf[i] | 0);
            } else {
                sBcdBuf[i] |= aasc_to_bcd(sAscBuf[j++]);
            }
        }

    }

    public static void BcdToAsc0(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
        BcdToAsc(sAscBuf, sBcdBuf, iAscLen);
        sAscBuf[iAscLen] = 0;
    }

    public static void ATBByIndex(byte[] sBcdBuf, int sBcdIndex, byte[] sAscBuf, int sAscIndex, int iAscLen) {
        byte[] Asctemp = new byte[iAscLen];
        byte[] Bcdtemp = new byte[(iAscLen + 1) / 2];
        System.arraycopy(sAscBuf, sAscIndex, Asctemp, 0, iAscLen);
        AscToBcd(Bcdtemp, Asctemp, iAscLen);
        System.arraycopy(Bcdtemp, 0, sBcdBuf, sBcdIndex, (iAscLen + 1) / 2);
    }

    public static void BTAByIndex(byte[] sBcdBuf, int sBcdIndex, byte[] sAscBuf, int sAscIndex, int iAscLen) {
        byte[] Asctemp = new byte[iAscLen];
        byte[] Bcdtemp = new byte[(iAscLen + 1) / 2];
        System.arraycopy(sBcdBuf, sBcdIndex, Bcdtemp, 0, (iAscLen + 1) / 2);
        BcdToAsc(Asctemp, Bcdtemp, iAscLen);
        System.arraycopy(Asctemp, 0, sAscBuf, sAscIndex, iAscLen);
    }

    public static String SetStrTlv(String tag, String value) {
        long l = (long) value.length();
        String strlen = String.format("%04X", new Object[]{Long.valueOf(l)});
        String tempstrvalue = "";
        if (tag == "0030" || tag == "0031" || tag == "0000" || tag == "0001" || tag == "0002" || tag == "0003" || tag == "0004" || tag == "0005" || tag == "0010" || tag == "0015" || tag == "0020" || tag == "0021" || tag == "0022" || tag == "0023" || tag == "0001" || tag == "0045" || tag == "0046" || tag == "0047" || tag == "0053" || tag == "0054" || tag == "0055" || tag == "0056") {
            byte[] srcbytes = value.getBytes();
            byte[] dstbytes = new byte[value.length() * 2];
            BcdToAsc(dstbytes, srcbytes, dstbytes.length);
            tempstrvalue = new String(dstbytes);
        }

        return tag + strlen + tempstrvalue;
    }

    public static byte bcd_to_byte(byte ucBcd) {
        byte temp = (byte) (ucBcd >> 4);
        temp = (byte) (temp & 15);
        temp = (byte) (temp * 10);
        byte temp2 = (byte) (ucBcd & 15);
        return (byte) (temp + temp2);
    }

    public static int BcdToInt(byte[] sBcdBuf, int iBcdLen) {
        int iValue = 0;
        int i = 0;
        if (iBcdLen <= 0) {
            return 0;
        } else {
            while (i < iBcdLen) {
                byte temp = sBcdBuf[i];
                iValue = iValue * 100 + bcd_to_byte(temp);
                ++i;
            }

            return iValue;
        }
    }

    public static int comparabytes(byte[] temp1, byte[] temp2, int len) {
        for (int i = 0; i < len; ++i) {
            if (temp1[i] != temp2[i]) {
                return -1;
            }
        }

        return 0;
    }

    public static int memcmp(byte[] temp1, byte[] temp2, int len) {
        for (int i = 0; i < len; ++i) {
            if (temp1[i] != temp2[i]) {
                return -1;
            }
        }

        return 0;
    }

    public static String byte2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < data.length; ++i) {
            String temp = Integer.toHexString(data[i] & 255);

            for (int t = temp.length(); t < 2; ++t) {
                sb.append("0");
            }

            sb.append(temp);
        }

        return sb.toString();
    }

    public static byte[] StrToHexByte(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];

            for (int i = 0; i < len; ++i) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }

            return buffer;
        }
    }

    public static String GetTLVStr(String SrcStr, String KeyWordStr) {
        boolean i = false;
        int index = 0;
        boolean len = false;
        int lenMax = SrcStr.length();
        String tempStr = "";
        String StrLen = "";

        int len2;
        for (byte[] Blen = new byte[2]; index < lenMax - 6; index += len2) {
            tempStr = SrcStr.substring(index, 4);
            byte len1;
            if (tempStr == KeyWordStr) {
                StrLen = SrcStr.substring(index + 4, 4);
                Blen[0] = Blen[1] = 0;
                Blen = StrToHexByte(StrLen);
                len1 = Blen[0];
                len2 = len1 << 8;
                len2 |= Blen[1];
                index += 8;
                tempStr = SrcStr.substring(index, len2);
                return tempStr;
            }

            StrLen = SrcStr.substring(index + 4, 4);
            Blen[0] = Blen[1] = 0;
            Blen = StrToHexByte(StrLen);
            len1 = Blen[0];
            len2 = len1 << 8;
            len2 |= Blen[1];
            index += 8;
        }

        return "";
    }

    public static void _vTwoOne(byte[] input, int inLen, byte[] output) {
        for (int i = 0; i < inLen; i += 2) {
            byte tmp = input[i];
            if (tmp > 57) {
                tmp = (byte) (tmp - 55);
            } else {
                tmp = (byte) (tmp & 15);
            }

            tmp = (byte) (tmp << 4);
            output[i / 2] = tmp;
            if (i >= inLen - 1) {
                break;
            }

            tmp = input[i + 1];
            if (tmp > 57) {
                tmp = (byte) (tmp - 55);
            } else {
                tmp = (byte) (tmp & 15);
            }

            output[i / 2] += tmp;
        }

    }

    public static void Replace_D(byte[] track, int len) {
        for (int i = 0; i < len; ++i) {
            if (track[i] == 61) {
                track[i] = 68;
            }
        }

    }

    public static byte GetCardFromTrack(byte[] szCardNo, byte[] track2, byte[] track3) {
        int i;
        if (ByteArrayLength(track2) != 0) {
            for (i = 0; track2[i] != 61; ++i) {
                if (i > 19) {
                    return 2;
                }
            }

            if (i < 13) {
                return 2;
            }

            System.arraycopy(track2, 0, szCardNo, 0, i);
            szCardNo[i] = 0;
        } else if (ByteArrayLength(track3) != 0) {
            for (i = 0; track3[i] != 61; ++i) {
                if (i > 21) {
                    return 3;
                }
            }

            if (i < 15) {
                return 3;
            }

            System.arraycopy(track3, 2, szCardNo, 0, i - 2);
            szCardNo[i - 2] = 0;
        }

        return 0;
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);

        for (int i = 0; i < bArray.length; ++i) {
            String sTemp = Integer.toHexString(255 & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }

            sb.append(sTemp.toUpperCase());
        }

        return sb.toString();
    }

    public static byte ByteArrayLength(byte[] Array) {
        int i;
        for (i = 0; Array[i] != 0; ++i) {
            ;
        }

        return (byte) i;
    }

    public static byte LongToDec(byte[] szAmount) {
        byte[] tmp = new byte[16];
        System.arraycopy("0.00".getBytes(), 0, tmp, 0, 4);
        tmp[4] = 0;
        byte len1 = ByteArrayLength(szAmount);
        if (len1 == 0) {
            System.arraycopy(tmp, 0, szAmount, 0, 4);
            szAmount[4] = 0;
            return 4;
        } else if (len1 == 1) {
            tmp[3] = szAmount[0];
            System.arraycopy(tmp, 0, szAmount, 0, 4);
            szAmount[4] = 0;
            return 4;
        } else if (len1 == 2) {
            tmp[2] = szAmount[0];
            tmp[3] = szAmount[1];
            System.arraycopy(tmp, 0, szAmount, 0, 4);
            szAmount[4] = 0;
            return 4;
        } else {
            System.arraycopy(szAmount, 0, tmp, 0, len1 - 2);
            tmp[len1 - 2] = 46;
            System.arraycopy(szAmount, len1 - 2, tmp, len1 - 1, 2);
            tmp[len1 + 1] = 0;
            System.arraycopy(tmp, 0, szAmount, 0, len1 + 1);
            szAmount[len1 + 1] = 0;
            return (byte) (len1 + 1);
        }
    }

    public static byte ConvBcdAmount(byte[] bcdAmt, byte[] amountPtr) {
        byte[] buffer = new byte[16];
        byte[] startBuff = new byte[16];
        BcdToAsc(buffer, bcdAmt, 12);
        buffer[12] = 0;

        for (int i = 0; i < 12; ++i) {
            if (buffer[i] != 48) {
                System.arraycopy(buffer, i, startBuff, 0, 12 - i);
                byte amtLen = LongToDec(startBuff);
                System.arraycopy(startBuff, 0, amountPtr, 0, amtLen);
                return amtLen;
            }
        }

        System.arraycopy("0.00".getBytes(), 0, amountPtr, 0, 4);
        return 4;
    }

    public static int byte2int(byte[] res) {
        int targets = res[0] & 255 | res[1] << 8 & '\uff00';
        return targets;
    }

    public static int atoi(byte[] res) {
        int iRet = (res[0] - 48) * 10 + (res[1] - 48);
        return iRet;
    }

    public static short byte2short(byte[] buf) {
        boolean s1 = false;
        boolean s2 = false;
        short s11 = (short) buf[0];
        s11 = (short) (s11 << 8);
        short s21 = (short) buf[1];
        return (short) (s11 + s21);
    }

    public static int GetTlvItem(String TagStr, String buffStr, String outStr) {
        boolean index = false;
        boolean tempTag = false;
        int tag = Integer.parseInt(TagStr, 16);
        byte[] Asc = buffStr.getBytes();
        int Alen = Asc.length;
        byte[] Bcd = new byte[Alen / 2];
        AscToBcd(Bcd, Asc, Alen);
        return 0;
    }

    public static volatile int g_timerflag = 0;

    public static Timer mTimer;
    public static TimerTask task;

    public static void startTimer(long timeOut) {
        g_timerflag = 0;
        Log.i("maxlog", "timeout:" + timeOut);

        if (task != null)
            task.cancel();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        mTimer = new Timer();
        task = new TimerTask() {
            public void run() {
                Log.i("timer", "time out run");
                g_timerflag = 1;
            }
        };
        mTimer.schedule(task, timeOut);
    }

    public static int checkTimer() {
        if (g_timerflag == 0)
            return 1;
        else {
            return 0;
        }
    }

    public static String bytes2HexString(byte[] data, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            String temp = Integer.toHexString(((int) data[i]) & 0xFF);
            for (int t = temp.length(); t < 2; t++) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    public static int findSub(byte pSrc[], int nSrcStart, int nSrcLen, byte pSub[], int nSubLen) {
        int j = 0;
        for (int i = nSrcStart; i <= nSrcLen - nSubLen; ++i) {
            if (pSrc[i] == pSub[0]) {
                for (j = 1; j < nSubLen; j++) {
                    if (pSrc[i + j] != pSub[j])
                        break;
                    return i;
                }
            }
        }
        return -1;
    }


}
