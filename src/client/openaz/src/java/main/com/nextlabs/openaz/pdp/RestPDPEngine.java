/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pdp;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.Response;
import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPException;
import org.apache.openaz.xacml.std.jaxp.JaxpResponse;

import com.nextlabs.openaz.pdp.utils.RequestTypeMarshaller;
import com.nextlabs.openaz.pdp.utils.RequestTypeUtil;
import com.nextlabs.openaz.pdp.utils.ResponseTypeParser;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

public class RestPDPEngine implements PDPEngine {

	private static final Log logger = LogFactory.getLog(RestPDPEngine.class);

	private static final List<URI> EMPTY_PROFILES = new ArrayList<URI>();
	private volatile static RestPDPEngine engine = null;
	
	private RestExecutor restExecutor;
	
	private RestPDPEngine(Properties properties) throws PDPException {
		if (properties == null) {
			throw new PDPException("properties is null");
		}
		restExecutor = new NextLabsRestExecutor(properties);
	}
	
	public static RestPDPEngine getInstance(Properties properties) throws PDPException {
		if (engine == null) {
			synchronized (RestPDPEngine.class) {
				if (engine == null) {
					engine = new RestPDPEngine(properties);
				}
			}
		}

		return engine;
	}

	@Override
	public Response decide(Request request) throws PDPException {
		RequestType requestType = RequestTypeUtil.convert(request);
		
		String xmlRequestString = null;
		try {
			xmlRequestString = RequestTypeMarshaller.marshal(requestType);
		} catch (JAXBException e) {
			throw new PDPException("Error marshal request to XML payload: ", e);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Created xacml request xml string:\n" + xmlRequestString);
		}
		
		String xmlResponseString = null;
		try{
			xmlResponseString = this.restExecutor.xmlCall(xmlRequestString);
		} catch (RestExecutorException e) {
			throw new PDPException("Error making REST call to PDP: ", e);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Received xacml response xml string:\n" + xmlResponseString);
		}
		
		ResponseType responseType = null;
		try {
			responseType = ResponseTypeParser.parse(xmlResponseString);
		} catch (JAXBException e) {
			throw new PDPException("Error parse response body: ", e);
		}
		
		return JaxpResponse.newInstance(responseType);
	}

	@Override
	public Collection<URI> getProfiles() {
		return EMPTY_PROFILES;
	}

	@Override
	public boolean hasProfile(URI uriProfile) {
		return false;
	}

	public RestExecutor getRestExecutor() {
		return restExecutor;
	}

	public void setRestExecutor(RestExecutor restExecutor) {
		this.restExecutor = restExecutor;
	}

}
