package Entities;

import java.util.Arrays;

public class UtilsSystem {
    public static byte[] getSliceOfArray(byte[] arr, int startIndex, int endIndex) {
        return Arrays.copyOfRange(arr,startIndex,endIndex);
    }
}
