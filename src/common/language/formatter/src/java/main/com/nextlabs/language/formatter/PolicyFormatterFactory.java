package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/main/com/nextlabs/language/formatter/PolicyFormatterFactory.java#1 $
 */

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.nextlabs.language.formatter.st.PolicyLanguageFormatter;
import com.nextlabs.language.parser.IPolicyParserFactory;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.language.parser.PolicyParserFactory;

/**
 * This is the implementation of the policy formatter factory.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyFormatterFactory implements IPolicyFormatterFactory {

    private final Map<Integer,Map<String,String>> keywords =
        new HashMap<Integer,Map<String,String>>();

    /**
     * @see IPolicyFormatterFactory#getFormatter(String, int)
     */
    public IPolicyLanguageFormatter getFormatter(String format, int version) {
        return new PolicyLanguageFormatter(format, version, getKeywords(version));
    }

    /**
     * Returns the map of keywords for the specified version of the language.
     *
     * @param version the version of the language for which to obtain keywords.
     * @return the keywords for the specified version of the language.
     */
    private synchronized Map<String,String> getKeywords(int version) {
        Map<String,String> res = keywords.get(version);
        if (res == null) {
            res = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            // Get the keywords from the parser, and load them into the map
            try {
                IPolicyParserFactory ppf = new PolicyParserFactory();
                for (String keyword : ppf.getParser(version).getKeywords()) {
                    res.put(keyword, keyword);
                }
            } catch (PolicyLanguageException ignored) {
                // TODO Log the exception
            }
        }
        return res;
    }

}
