/*
 * Created on Feb 6, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;

import com.google.gson.Gson;
import com.nextlabs.evaluationconnector.beans.json.ResponseWrapper;

/**
 * <p>
 * TestUtils
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public final class TestUtils {

	private static final Log log = LogFactory.getLog(TestUtils.class);

	/**
	 * <p>
	 * Unmarshall XML response
	 * </p>
	 *
	 * @param reponseString
	 * @return
	 */
	public static ResponseType unmarshallXMLResponse(String reponseString) {
		try {
			log.info("XML Response : " + reponseString);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(ResponseType.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			@SuppressWarnings("unchecked")
			JAXBElement<ResponseType> responseTypeElement = (JAXBElement<ResponseType>) jaxbUnmarshaller
					.unmarshal(new StringReader(reponseString));

			return responseTypeElement.getValue();
		} catch (JAXBException e) {
			log.error("Error in unmarshalling, ", e);
			return null;
		}
	}

	/**
	 * <p>
	 * Get Object from JSON response
	 * </p>
	 *
	 * @param reponseString
	 * @return
	 */
	public static ResponseWrapper jsonResponse(String reponseString) {
		log.info("JSON Response : " + reponseString);
		Gson gson = new Gson();
		return (ResponseWrapper) gson.fromJson(reponseString,
				ResponseWrapper.class);
	}

	public static String getDataFromEntity(HttpEntity entity) {
		StringBuffer content = new StringBuffer();
		String inputLine;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					entity.getContent()));
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
		} catch (IOException e) {
			log.error("Error in unmarshalling, ", e);
		}
		return content.toString();
	}

	public static void main(String[] a) {
		String reponseString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Response xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">"
				+ "<Result><Decision>Permit</Decision><Status><StatusCode Value=\"urn:oasis:names:tc:xacml:1.0:status:ok\"/> "
				+ "<StatusMessage>success</StatusMessage></Status><Obligations/></Result><Result><Decision>Permit</Decision><Status>"
				+ "<StatusCode Value=\"urn:oasis:names:tc:xacml:1.0:status:ok\"/><StatusMessage>success</StatusMessage>"
				+ "</Status><Obligations/></Result></Response>";

		ResponseType rt = unmarshallXMLResponse(reponseString);
		System.out.println(":::::::: Response : " + rt);

		String json = "{\"Response\":{\"Result\":[{\"Decision\":\"Permit\",\"Status\":{\"StatusMessage\":\"success\",\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:ok\"}}},{\"Decision\":\"Permit\",\"Status\":{\"StatusMessage\":\"success\",\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:ok\"}}},{\"Decision\":\"Permit\",\"Status\":{\"StatusMessage\":\"success\",\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:ok\"}}}]}}";

		ResponseWrapper response = jsonResponse(json);
		System.out.println(":::::::: Response : " + response);
	}

}
