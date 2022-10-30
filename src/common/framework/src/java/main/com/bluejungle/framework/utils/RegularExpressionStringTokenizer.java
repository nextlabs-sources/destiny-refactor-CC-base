/*
 * Created on Sep 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A String Tokenizer which uses regular expression to denote a delimeter
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/RegularExpressionStringTokenizer.java#1 $
 */

public class RegularExpressionStringTokenizer implements Iterator {

    private String input;
    private Matcher matcher;
    private boolean returnDelims;
    private String nextDelimToReturn;
    private String nextMatchToReturn;
    private int lastEnd = 0;

    public RegularExpressionStringTokenizer(String input, String regularExpressionDelim, boolean returnDelims) {
        if (input == null) {
            throw new NullPointerException("input cannot be null.");
        }
        if (regularExpressionDelim == null) {
            throw new NullPointerException("regularExpressionDelim cannot be null.");
        }

        this.input = input;
        this.returnDelims = returnDelims;

        // Compile pattern and prepare input
        Pattern pattern = Pattern.compile(regularExpressionDelim);
        this.matcher = pattern.matcher(input);
    }

    // Returns true if there are more tokens or delimiters.
    public boolean hasNext() {
        boolean valueToReturn = false;

        if (this.matcher != null) {
            if (((this.returnDelims) && (this.nextDelimToReturn != null)) || this.nextMatchToReturn != null) {
                valueToReturn = true;
            } else if (this.matcher.find()) {
                this.nextMatchToReturn = this.input.substring(this.lastEnd, this.matcher.start());
                this.nextDelimToReturn = this.matcher.group();
                this.lastEnd = this.matcher.end();
                valueToReturn = true;
            } else if (this.lastEnd < this.input.length()) {
                this.nextMatchToReturn = this.input.subSequence(this.lastEnd, this.input.length()).toString();
                this.nextDelimToReturn = null;
                this.lastEnd = this.input.length();
                valueToReturn = true;
                
                // end of iteration
                this.matcher = null;
            }
        }

        return valueToReturn;
    }

    // Returns the next token (or delimiter if returnDelims is true).
    public Object next() {
        String result = null;

        if (this.nextMatchToReturn != null) {
            result = this.nextMatchToReturn;
            this.nextMatchToReturn = null;
        } else if ((this.nextDelimToReturn != null) && (this.returnDelims)) {
            result = this.nextDelimToReturn;
            this.nextDelimToReturn = null;
        }

        return result;
    }

    // Not supported.
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
