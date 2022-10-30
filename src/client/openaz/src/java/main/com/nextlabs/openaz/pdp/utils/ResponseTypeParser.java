/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pdp.utils;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

/**
 * Created by sduan on 17/12/2015.
 */
public class ResponseTypeParser {
	
	private static final Log logger = LogFactory.getLog(ResponseTypeParser.class);
    private static JAXBContext jc = null;
    
    private static void init() throws JAXBException {
        synchronized (ResponseTypeParser.class) {
        	if(jc == null) {
        		jc = JAXBContext.newInstance(ResponseType.class);
        		if(logger.isDebugEnabled()) {
                	logger.debug("ResponseTypeParser initialized successfully");
                }
        	}
        }
    }

    public static ResponseType parse(String stringResponse) throws JAXBException {
        if(jc == null) {
            init();
        }
        StringReader stringReader = new StringReader(stringResponse);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (ResponseType) JAXBIntrospector.getValue(unmarshaller.unmarshal(stringReader));
    }
}
