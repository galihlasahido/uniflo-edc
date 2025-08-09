package com.ftpos.ftappdemo.util;

public class Convection {

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    public static byte[] hexString2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        /*
         * del if (hexString.length() % 2 != 0) { hexString = "0" + hexString; }
         */
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    public static String byte2HexStr(byte[] src)
    {
        return byte2HexStr(src,src.length);
    }
    public static String byte2HexStr(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        int n = len;
        if (len > src.length)
            n = src.length;

        for (int i = 0; i < n; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }

            stringBuilder.append(hv.toUpperCase());
        }
        return stringBuilder.toString();
    }

    public static String Bytes2HexString(byte[] b) {
        String ret = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);

            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static String Bytes2HexString(byte[] b, int length) {
        String ret = "";

        for(int i = 0; i < length; ++i) {
            String hex = Integer.toHexString(b[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            ret = ret + hex.toUpperCase();
        }

        return ret;
    }

    /**
     *
     * @param sectors sectors
     * @param block block
     * @return Mifare Block
     */
    public static int m1AddrCal(int sectors, int block){
        if (sectors<0 || sectors>15) {
            return -1;
        }
        else if(block<0 || block>3) {
            return -2;
        }
        else {
            return sectors * 4 + block;
        }
    }

}
