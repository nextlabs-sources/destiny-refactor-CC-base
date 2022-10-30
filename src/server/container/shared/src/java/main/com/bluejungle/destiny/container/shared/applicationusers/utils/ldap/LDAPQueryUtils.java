/*
 * Created on Sep 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.utils.ldap;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/utils/ldap/LDAPQueryUtils.java#1 $
 */

public class LDAPQueryUtils {
	//FIXME should get this from com.bluejungle.destiny.webui.browsabledatapicker.defaultimpl.ISearchBucketBean.NO_VALUE
	private static final String OTHER_VALUE = " ";

    /**
     * Converts the given value array along with teh attribute name into a
     * compound LDAP search filter
     * 
     * @param attributeName
     * @param values
     * @return search filter
     */
    public static String generateCompoundSearchFilter(String attributeName, Object[] values) {
        if (values == null) {
            throw new NullPointerException("values is null");
        }
        if (attributeName == null) {
            throw new NullPointerException("attribute name is null");
        }
        if (values.length == 0) {
            return "";
        }

        String filter = _generateCompoundSearchFilter(attributeName, values, 0);
        return filter;
    }

    /**
     * Returns a search filter based on the provided binary value (as a byte
     * array) and attribute name
     * 
     * @param attributeName
     * @param binaryValue
     * @return filter
     */
    public static String generateBinarySearchFilter(String attributeName, byte[] binaryValue) {
        String filter = (new StringBuffer()).append("(").append(attributeName).append("=").append(convertBinaryToHexString(binaryValue)).append(")").toString();
        return filter;
    }

    /**
     * Returns a search filter based on the provided string value
     * 
     * @param attributeName
     * @param string
     *            value
     * @return filter
     */
    public static String generateStringSearchFilter(String attributeName, String value) {
        String processedValue = getEscapedValue(value);
        String filter = (new StringBuffer()).append("(").append(attributeName).append("=").append(processedValue).append(")").toString();
        return filter;
    }

    /**
     * Generates a hex search value for a binary value
     * 
     * @param binaryValue
     * @return search string
     */
    protected static String convertBinaryToHexString(byte[] binaryValue) {
        if (binaryValue == null) {
            throw new NullPointerException("binary value is null");
        }

        StringBuffer binaryFilterBuffer = new StringBuffer();
        for (int i = 0; i < binaryValue.length; i++) {
            int nextByte = binaryValue[i];

            // Since a byte is a signed type, we need to obtain the unsigned
            // magnitude:
            if (nextByte < 0) {
                nextByte &= 0xFF;
            }
            String hexStr = Integer.toHexString(nextByte);

            // For bytes that have all 0s in the most significant 4 bits, we
            // will need to explicitly add a '0' to make it a 2-char string
            // which is what LDAP requires for each byte:
            if (hexStr.length() == 1) {
                hexStr = "0" + hexStr;
            }

            binaryFilterBuffer.append("\\").append(hexStr);
        }

        String binaryFilter = binaryFilterBuffer.toString();
        return binaryFilter;
    }

    /**
     * Generates an octal search value for a binary value
     * 
     * @param binaryValue
     * @return search string
     */
    protected static String convertBinaryToOctalString(byte[] binaryValue) {
        if (binaryValue == null) {
            throw new NullPointerException("binary value is null");
        }

        StringBuffer binaryFilterBuffer = new StringBuffer();
        for (int i = 0; i < binaryValue.length; i++) {
            int nextByte = binaryValue[i];

            // Since a byte is a signed type, we need to obtain the unsigned
            // magnitude:
            if (nextByte < 0) {
                nextByte &= 0777;
            }
            String octalString = Integer.toOctalString(nextByte);

            // For bytes that have all 0s in the most significant 4 bits, we
            // will need to explicitly add a '0' to make it a 2-char string
            // which is what LDAP requires for each byte:
            int gap = 3 - octalString.length();
            for (int j = 0; j < gap; j++) {
                octalString = "0" + octalString;
            }
            //            if (octalString.length() == 1) {
            //                octalString = "0" + octalString;
            //            }

            binaryFilterBuffer.append("\\").append(octalString);
        }

        String binaryFilter = binaryFilterBuffer.toString();
        return binaryFilter;
    }
    
    private static final String[] ALPABET_A2Z;
    static{
    	ALPABET_A2Z = new String[26];
		for (int i = 0; i < 26; i++) {
			ALPABET_A2Z[i] = Character.toString((char) ('A' + i)) + "*";
		}
    }

    /**
     * Recursive method to create a compound search filter
     * If the value is a single space, it will become a query to search a entry doesn't have attributeName
     * 
     * @param attributeName
     * @param values
     * @param startIndex
     * @return search filter
     */
    protected static String _generateCompoundSearchFilter(String attributeName, Object[] values, int startIndex) {
        String compoundSearchFilter;
        if (values.length == (startIndex + 1)) {
            // This is the base case - the last id in the array:
            String value = getEscapedValue(values[startIndex].toString());
            
            compoundSearchFilter = value.equals(OTHER_VALUE) 
            		? "!" + _generateCompoundSearchFilter(attributeName, ALPABET_A2Z, 0)
            		: attributeName + "=" + value;
			
        } else {
            // Recursive case:
            String value = getEscapedValue(values[startIndex].toString());
            String subFiter = value.equals(OTHER_VALUE) 
        			? "!" + _generateCompoundSearchFilter(attributeName, ALPABET_A2Z, 0)
        			: attributeName + "=" + value;
            compoundSearchFilter = "|(" + subFiter + ")"
            		+ _generateCompoundSearchFilter(attributeName, values, startIndex + 1);
        }

        return "(" +compoundSearchFilter + ")";
    }

    protected static String getEscapedValue(String rawValue) {
        String escapedValue = null;
        if (rawValue != null) {
            escapedValue = rawValue.replaceAll("([()\\\\])", "\\\\$1");
        }
        return escapedValue;
    }

}