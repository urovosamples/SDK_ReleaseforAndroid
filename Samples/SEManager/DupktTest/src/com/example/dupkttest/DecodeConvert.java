package com.example.dupkttest;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;


public class DecodeConvert {


    //Convert to FMT format character, for example: str="1" Fmt="0000" -> 1 to 0001
    public static String FormatWithZero(String str, String Fmt) {
        try {
            Integer itr = Integer.parseInt(str);
            DecimalFormat df = new DecimalFormat(Fmt);
            return df.format(itr);
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
    }

    public static byte abcd_to_asc(byte ucBcd) {
        byte ucAsc;

        ucBcd &= 0x0f;
        if (ucBcd <= 9)
            ucAsc = (byte) (ucBcd + (byte) ('0'));
        else
            ucAsc = (byte) (ucBcd + (byte) ('A') - (byte) 10);
        return (ucAsc);
    }

    public static void BcdToAsc(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
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

    /****************************************************************************
     **      Convert a byte ASC code to BCD code
     @param  ucAsc  ASC source code
     @return BCD code
     ****************************************************************************/
    public static byte aasc_to_bcd(byte ucAsc) {
        byte ucBcd;

        if ((ucAsc >= '0') && (ucAsc <= '9'))
            ucBcd = (byte) (ucAsc - '0');
        else if ((ucAsc >= 'A') && (ucAsc <= 'F'))
            ucBcd = (byte) (ucAsc - 'A' + 10);
        else if ((ucAsc >= 'a') && (ucAsc <= 'f'))
            ucBcd = (byte) (ucAsc - 'a' + 10);
        else if ((ucAsc > 0x39) && (ucAsc <= 0x3f))
            ucBcd = (byte) (ucAsc - '0');
        else
            ucBcd = 0x0f;

        return ucBcd;
    }

    /****************************************************************************
     **      ASC code string to BCD code string
     @param  sBcdBuf   Destination BCD string head address
     @param  sAscBuf   Source ASC string initial address
     @param  iAscLen   Source ASC string length
     @param  sBcdBuf   Destination BCD string head address
     ****************************************************************************/
    public static void AscToBcd(byte[] sBcdBuf, byte[] sAscBuf, int iAscLen) {
        int i, j;

        j = 0;

        for (i = 0; i < (iAscLen + 1) / 2; i++) {
            sBcdBuf[i] = (byte) (aasc_to_bcd(sAscBuf[j++]) << 4);
            if (j >= iAscLen) {
                sBcdBuf[i] |= 0x00;
            } else {
                sBcdBuf[i] |= aasc_to_bcd(sAscBuf[j++]);
            }
        }
    }

    public static void BcdToAsc0(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
        BcdToAsc(sAscBuf, sBcdBuf, iAscLen);
        sAscBuf[iAscLen] = 0;
    }

    //SetStrTlv("0000", "99");
    public static String SetStrTlv(String tag, String value) {
        long l = value.length();
        String strlen = String.format("%04X", l);

        String tempstrvalue = "";
        if (tag == "0030" || tag == "0031" || tag == "0000" || tag == "0001"
                || tag == "0002" || tag == "0003" || tag == "0004"
                || tag == "0005" || tag == "0010" || tag == "0015"
                || tag == "0020" || tag == "0021" || tag == "0022"
                || tag == "0023" || tag == "0001" || tag == "0045"
                || tag == "0046" || tag == "0047" || tag == "0053"
                || tag == "0054" || tag == "0055" || tag == "0056") {
            byte[] srcbytes = value.getBytes();
            byte[] dstbytes = new byte[value.length() * 2];
            BcdToAsc(dstbytes, srcbytes, dstbytes.length);
            tempstrvalue = new String(dstbytes);

        }

        return tag + strlen + tempstrvalue;
    }

    // Convert to character
    public static String byte2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String temp = Integer.toHexString(((int) data[i]) & 0xFF);
            for (int t = temp.length(); t < 2; t++) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    // Convert to hex
    public static byte[] StrToHexByte(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    public static int comparabytes(byte[] temp1, byte[] temp2, int len) {
        for (int i = 0; i < len; i++) {
            if (temp1[i] == temp2[i]) {

            } else {
                return -1;
            }
        }
        return 0;
    }

    public static String getHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static byte[] hexStr2ByteArray(String hexString) {
        if ("".equals(hexString) || hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * Convert byte[] to hex
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert a string of hex data into a byte array.
     * Original author is:
     *
     * @param s The hex string to convert
     * @return An array of bytes with the values of the string.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
            Log.d("debug", "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * InputStream to string
     *
     * @throws IOException
     */
    public String inputStream2Str(InputStream is) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }


    // Hex help
    private static final byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
            (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'};

    /**
     * convert a byte arrary to hex string
     *
     * @param raw byte arrary
     * @param len lenght of the arrary.
     * @return hex string.
     */
    public static String getHexString(byte[] raw, int len) {
        byte[] hex = new byte[2 * len];
        int index = 0;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex);
    }


    // Copy file
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // Create a new file input stream and buffer it
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // Create a new file output stream and buffer it
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // Buffer array
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // Refresh this buffered output stream
            outBuff.flush();
        } finally {
            // Closed flow
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
}
