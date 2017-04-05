package com.example.gilharap.mybluetooth2;

import java.util.ArrayList;

/**
 * Created by Gil Harap on 05/04/2017.
 */

public class ConvertUtil {

    public static Byte intToHexByte(int num) {
        String ans =  Integer.toHexString(num);
        return Byte.parseByte(ans);
    }

    public static String intToBinaryString(int num) {
        String ans =  Integer.toBinaryString(num);
        for(int i = ans.length(); i < 8; i++){
            ans = "0" + ans ;
        }
        return ans;
    }

    public static String bytesToHexString(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] ByteListToArray(ArrayList<Byte> list){
        Byte[] objects = list.toArray(new Byte[list.size()]);
        byte[] bytes = new byte[objects.length];
        int i = 0;
        for(Byte b: objects){
            bytes[i++] = b.byteValue();
        }
        return bytes;
    }
}
