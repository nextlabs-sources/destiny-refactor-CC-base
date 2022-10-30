package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/main/com/nextlabs/language/formatter/IPolicyFormatterFactory.java#1 $
 */


/**
 * This is the interface for the policy formatter factory.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyFormatterFactory {

    /**
     * Obtains an instance of policy language formatter.
     *
     * @param format the format code describing the type of the formatter.
     * @param version the version of the language to be produced.
     * @return an instance of policy language formatter.
     */
    IPolicyLanguageFormatter getFormatter(String format, int version);

}
