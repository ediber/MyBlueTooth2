package com.example.gilharap.mybluetooth2;

/**
 * Created by Gil Harap on 05/04/2017.
 */

public class ConvertUtil {

    public static Byte intToHexByte(int num) {
        String ans =  Integer.toHexString(num);
        return Byte.parseByte(ans);
    }
}
