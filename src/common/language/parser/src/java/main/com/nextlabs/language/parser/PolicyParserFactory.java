package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/main/com/nextlabs/language/parser/PolicyParserFactory.java#1 $
 */

import java.lang.reflect.Method;

import com.nextlabs.util.ref.IReferenceFactory;

/**
 * This class lets the users obtain instances of policy language parsers
 * without hard-coding the exact type of the parser.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyParserFactory implements IPolicyParserFactory {

    private static final String ANTLR_BASE = ".antlr.v";
    private static final String ANTLR_PARSER = ".AntlrPolicyLanguageParser";

    /**
     * @see IPolicyParserFactory#getParser(int version)
     */
    public IPolicyLanguageParser getParser( int version)
        throws PolicyLanguageException {
        return getParser(version, IReferenceFactory.DEFAULT);
    }

    /**
     * @see IPolicyParserFactory#getParser(int, IReferenceFactory)
     */
    public IPolicyLanguageParser getParser(
        int version
    ,   IReferenceFactory refFactory
    )  throws PolicyLanguageException {
        String className = getClass().getPackage().getName()
        + ANTLR_BASE
        + version
        + ANTLR_PARSER;
        try {
            Class<?> parserClass = Class.forName(className);
            Method setRF = parserClass.getDeclaredMethod(
                "setReferenceFactory"
            ,   IReferenceFactory.class
            );
            setRF.setAccessible(true);
            IPolicyLanguageParser res =
                (IPolicyLanguageParser)parserClass.newInstance();
            setRF.invoke(res, refFactory);
            return res;
        } catch (Exception e) {
            throw new PolicyLanguageException(
                "Version " + version + " not found", e
            );
        }
    }

}
