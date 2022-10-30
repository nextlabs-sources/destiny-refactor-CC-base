/*
 * Created on Aug 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.util.StringTokenizer;

/**
 * This class takes care of serializing / deserializing data structures passed
 * by requests and response objects.
 * 
 * @author ihanen
 */
public class SerialUtil {

    /**
     * Separator between item length
     */
    private static final String COMMA = ",";

    /**
     * Converts a string array to a string
     * 
     * @param array
     *            array to convert
     * @return a single byte array based on the string array
     */
    public static byte[] ArrayToString(final String[] array) {
        int totalLength = 0;
        if (array != null) {
            int arrayLength = array.length;
            for (int i = 0; i < arrayLength; i++) {
                final String currentString = array[i];
                totalLength += currentString.length();
            }
        }
        StringBuffer dataBuf = new StringBuffer(totalLength);
        StringBuffer headerBuf = new StringBuffer();
        if (array != null) {
            int arrayLength = array.length;
            headerBuf.append(arrayLength + COMMA);
            for (int i = 0; i < arrayLength; i++) {
                final String currentString = array[i];
                headerBuf.append(currentString.length() + COMMA);
                dataBuf.append(currentString);
            }
        } else {
            headerBuf.append("0");
        }

        final int headerBufLength = headerBuf.length();
        final int dataBufLength = dataBuf.length();
        byte[] result = new byte[headerBufLength + dataBufLength];
        for (int i = 0; i < headerBufLength; i++) {
            result[i] = (byte) headerBuf.charAt(i);
        }
        for (int i = 0; i < dataBufLength; i++) {
            result[i + headerBufLength] = (byte) dataBuf.charAt(i);
        }
        return result;
    }

    /**
     * For test / debugging purposes only
     * 
     * @param args
     *            none
     */
    public static void main(String[] args) {
        String[] array = { "abcd", "123456", "q" };
        byte[] bResult = ArrayToString(array);
        String result = new String(bResult, 0, bResult.length);
        String[] reverse = StringToArray(result);
    }

    /**
     * Deserialize a string into a string array
     * 
     * @param serialized
     *            serialized string
     * @return a string array
     */
    public static String[] StringToArray(final String serialized) {
        final StringTokenizer tokenizer = new StringTokenizer(serialized, COMMA);
        final StringTokenizer dataTokenizer = new StringTokenizer(serialized, COMMA);
        int nbTokens = tokenizer.countTokens();
        String[] result = {};
        if (nbTokens > 0) {
            int arraySize = Integer.parseInt(tokenizer.nextToken());
            result = new String[arraySize];
            //populates the array elements
            for (int i = 0; i < nbTokens - 1; i++) {
                dataTokenizer.nextToken();
            }
            final String payload = dataTokenizer.nextToken();
            int currentIndex = 0;
            for (int i = 0; i < arraySize; i++) {
                int elementSize = Integer.parseInt(tokenizer.nextToken());
                result[i] = payload.substring(currentIndex, currentIndex + elementSize);
                currentIndex += elementSize;
            }
        }
        return result;
    }

}