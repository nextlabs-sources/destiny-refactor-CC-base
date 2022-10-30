/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

/**
 * A collection of useful methods that manipulate arrays
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/ArrayUtils.java#1 $:
 */

public final class ArrayUtils {
    
    /**
     * Concatenates the given arrays into one big array in the same
     * order they're provided, i.e. arrays[0] + arrays[1] + ....
     * 
     * <b> It is up to the user to make sure that the total size of the array
     * is not bigger than Integer.MAX_VALUE </b>
     * 
     * @param arrays to concatenate
     * @return result of the concatenation
     */
    public static final byte[] concatenate(byte[][] arrays) {
        int length = 0;
        for (int i = arrays.length - 1; i >= 0; i--) {
            length += arrays[i].length;
        }
        
        byte[] rv = new byte[length];
        int currPos = 0;
        for (int i = 0; i < arrays.length; i++) {
            byte[] curr = arrays[i];
            System.arraycopy(curr, 0, rv, currPos, curr.length);
            currPos += curr.length;
        }
        return rv;
    }    
    
    
    private static final String	DEFAULT_LINE_BREAKER	= System.getProperty("line.separator");

    /**
     * concatenate a string array to array, each string is separated by line.separator
     * @see com.bluejungle.framework.utils.ArrayUtils.String#asString(java.lang.String[], java.lang.String,java.lang.String)
     * @param arrays to concatenate
     * @return result of the concatenation
     */
	public static <T> String asString(T[] objs) {
		return asString(objs, DEFAULT_LINE_BREAKER);
	}

	/**
     * concatenate a string array to array, each string is separated by separator
     * @see com.bluejungle.framework.utils.ArrayUtils.String#asString(java.lang.String[], java.lang.String,java.lang.String)
     * @param arrays to concatenate
     * @return result of the concatenation
     */
	public static <T> String asString(T[] objs, String seperator) {
		return asString(objs, seperator, "");
	}

	/**
     * concatenate a string array to array, each string is separated by separator 
     * and put a prefix in front of each array
     * @param arrays to concatenate
     * @return result of the concatenation
     */
	public static <T> String asString(T[] objs, String seperator, String prefix) {
	    return asString(objs, seperator, prefix, new Formatter<T>(){
            public String toString(T t) {
                return t == null ? "null" : t.toString();
            }
	    });
	}
	
	public static <T> String asString(T[] objs, String seperator, String prefix,
            Formatter<T> formatter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objs.length; i++) {
            // beware a null object
            sb.append(prefix).append(formatter.toString(objs[i]));
            if (i != objs.length - 1) {
                sb.append(seperator);
            }
        }

        return sb.toString();
    }
	
	public static long[] toLong(Number[] longs) {
		long[] returnLongs = new long[longs.length];
		for (int i = 0; i < longs.length; i++) {
			returnLongs[i] = longs[i].longValue();
		}
		return returnLongs;
	}
	
	public static Long[] toLong(long[] longs){
	    Long[] returnLongs = new Long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            returnLongs[i] = longs[i];
        }
        return returnLongs;
    }
	
	public static int[] toInt(Number[] ints) {
		int[] returnInts = new int[ints.length];
		for (int i = 0; i < ints.length; i++) {
			returnInts[i] = ints[i].intValue();
		}
		return returnInts;
	}
	
	public static Integer[] toInt(int[] ints){
	    Integer[] returnInts = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            returnInts[i] = ints[i];
        }
        return returnInts;
	}
	
	public static byte[] toByte(Number[] bytes) {
		byte[] returnBytes = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
		    returnBytes[i] = bytes[i].byteValue();
		}
		return returnBytes;
	}
	
	public static Byte[] toByte(byte[] bytes){
	    Byte[] returnBytes = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            returnBytes[i] = bytes[i];
        }
        return returnBytes;
    }

	public static void reverse(Object[] objs) {
		int len = objs.length - 1;
		Object temp;
		for (int i = 0; i <= len / 2; i++) {
			temp = objs[i];
			objs[i] = objs[len - i];
			objs[len - i] = temp;
		}
	}
}
