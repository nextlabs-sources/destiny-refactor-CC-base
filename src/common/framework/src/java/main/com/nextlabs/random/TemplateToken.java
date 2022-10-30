/*
 * Created on Jul 22, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.random;

import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token examples: 
 * - RANDOM_WORD(1-2)
 * - ID
 * 
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/random/TemplateToken.java#1 $
 */
public class TemplateToken {
    
    public enum TemplateTokenType{
        RANDOM_INTEGER,
        RANDOM_CHAR,
        RANDOM_WORD,
        RANDOM_FOLDER,
        RANDOM_TIME,
        RANDOM_FLOAT,
        RANDOM_STRING_FROM,
        ID,
        DATABASE_ID,
        OTHER,
    }
    
    // all fields are public, no method calls. Try to squeeze performance
    protected final TemplateTokenType type;
    protected final int rangeFrom;
    protected final int rangeTo;
    protected final String[] listedValues;
    
    private static Pattern TOKEN_PATTERN = Pattern.compile("(\\S+?)(\\((\\S+)\\))?");
    private static Pattern RANGE_VALUE_PATTERN = Pattern.compile("(\\d+)\\-(\\d+)");
    
    public TemplateToken(String str, Object ... optionals) {
        Matcher m = TOKEN_PATTERN.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException("Patten doesn't match. Input: " + str);
        }
        this.type = TemplateTokenType.valueOf(m.group(1));

        String value = m.group(3);
        if (value == null) {
            this.rangeFrom = Integer.MIN_VALUE;
            this.rangeTo = Integer.MIN_VALUE;
            this.listedValues = null;
        } else {
            Matcher m2 = RANGE_VALUE_PATTERN.matcher(value);
            if (m2.matches()) {
                this.rangeFrom = Integer.parseInt(m2.group(1));
                this.rangeTo = Integer.parseInt(m2.group(2));
                this.listedValues = null;
            } else {
                this.rangeFrom = Integer.MIN_VALUE;
                this.rangeTo = Integer.MIN_VALUE;
                this.listedValues = getListedValues(value, optionals);
            }
        }
    }
    
    protected String[] getListedValues(String value, Object... optionals){
        throw new IllegalArgumentException("Unknown value: " + value);
    }
    
    /**
     * you must override this method if you have type <code>OTHER</code>,
     * otherwise you will get <code>IllegalArgumentException</code>
     * @param id
     * @return
     * @throws IllegalArgumentException
     */
    public Object getTokenValues(Dictionary dictionary, Random random, Object id) throws IllegalArgumentException{
        switch (type) {
        case RANDOM_INTEGER:
            return rangeFrom + random.nextInt(rangeTo - rangeFrom);
        case RANDOM_CHAR:
            return (char)(rangeFrom + random.nextInt(rangeTo - rangeFrom));
        case RANDOM_WORD:
            return dictionary.gernerateRandomString(rangeFrom, rangeTo, " ", false);
        case RANDOM_TIME:
            return new Date(rangeFrom + random.nextInt(rangeTo - rangeFrom));
        case RANDOM_FLOAT:
            return random.nextFloat();
        case DATABASE_ID:
        case ID:
            return id;
        case RANDOM_STRING_FROM:
            return listedValues[random.nextInt(listedValues.length)];
        case RANDOM_FOLDER:
            return dictionary.gernerateRandomString(rangeFrom, rangeTo, "/", true);
        default:
            throw new IllegalArgumentException("unknown template token: " + type.name());
        }
    }
}
