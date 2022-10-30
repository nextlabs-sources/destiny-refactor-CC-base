package com.nextlabs.language.parser;

import com.nextlabs.util.ref.IReferenceFactory;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/main/com/nextlabs/language/parser/IPolicyParserFactory.java#1 $
 */

/**
 * This is the interface for the policy parser factory.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyParserFactory {

    /**
     * Obtains an instance of policy language parser of the specified version.
     * 
     * @param version the version of the language
     * to be supported by the parser.
     * @return an instance of policy language parser of the specified version.
     */
    IPolicyLanguageParser getParser(int version)
        throws PolicyLanguageException;

    /**
     * Obtains an instance of policy language parser of the specified version,
     * configured with the specified reference factory.
     *
     * @param version the version of the language
     * to be supported by the parser.
     * @param refFactory the reference factory to be used with the parser.
     * @return an instance of policy language parser of the specified version,
     * configured with the specified reference factory.
     */
    IPolicyLanguageParser getParser(int version, IReferenceFactory refFactory)
        throws PolicyLanguageException;

}