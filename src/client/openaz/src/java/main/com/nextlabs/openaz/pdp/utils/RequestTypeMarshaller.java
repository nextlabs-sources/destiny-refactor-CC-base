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

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;

public class RequestTypeMarshaller {
	
	private static final Log logger = LogFactory.getLog(RequestTypeMarshaller.class);
	
	private static JAXBContext jc = null;
    
	private static void init() throws JAXBException  {
        synchronized (RequestTypeMarshaller.class) {
        	if(jc == null) {
            	jc = JAXBContext.newInstance(RequestType.class);
                // see: http://stackoverflow.com/questions/6963996/can-i-replace-jaxb-properties-with-code
                // jc = JAXBContextFactory.createContext(new Class[] { RequestType.class }, null);
                if(logger.isDebugEnabled()) {
                	logger.debug("RequestTypeMarshaller initialized successfully");
                }
        	}
        }
    }
	
	public static String marshal(RequestType requestType) throws JAXBException {
		if(jc == null) {
            init();
        }
		Marshaller marshaller = jc.createMarshaller();
		// see: http://wiki.eclipse.org/EclipseLink/Release/2.4.0/JAXB_RI_Extensions/Namespace_Prefix_Mapper
        // Map<String, String> urisToPrefixes = new HashMap<String, String>();
        // urisToPrefixes.put(XACML3.XMLNS, "");
        // marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, urisToPrefixes);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
		JAXBElement<RequestType> requestTypeJAXBElement =
				new ObjectFactory().createRequest(requestType);
        StringWriter writer = new StringWriter();
        marshaller.marshal(requestTypeJAXBElement, writer);
        return writer.toString();
	}
}
