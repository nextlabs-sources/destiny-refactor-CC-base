/*
 * Created on Jan 29, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.parsers.XACMLParser;
import com.nextlabs.evaluationconnector.parsers.XACMLParserFactory;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * JSONParserTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
@RunWith(JUnit4.class)
public class JSONParserTest {

	private static final Log log = LogFactory.getLog(JSONParserTest.class);

	private String success_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_SubjectTag.txt";

	@Test
	public void parseJsonXACML1() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(success_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);

		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("OPEN", pdpRequest.getAction());
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSWord", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Payroll/Payroll.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));
	}

	private String allowed_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_AllowedFile.txt";

	@Test
	public void parseJsonXACML2() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(allowed_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);

		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("COPY", pdpRequest.getAction());
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSExcel", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Temp/Names.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));
		
		IPDPNamedAttributes[] additionalData = pdpRequest.getAdditionalData();
		assertEquals(1, additionalData.length);
		assertEquals("environment",additionalData[0].getName());
		//assertEquals("1999-05-31T13:20:00-05:00",additionalData[0].getValue("current-dateTime"));
	}

	private String multi_valued_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_MultiValues.txt";

	@Test
	public void parseJsonXACML3() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(multi_valued_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);

		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("COPY", pdpRequest.getAction());
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSWord", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Temp/Names.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));
	}
	
	private String multi_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_MultiRequest.txt";

	@Test
	public void parseJsonXACML4() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(multi_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		assertTrue("Should have two PDPRequest objects", pdpRequests.size() == 2);

		for (PDPRequest pdpRequest : pdpRequests) {
			log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));
		}
		
		assertEquals("OPEN", pdpRequests.get(0).getAction());
		assertEquals("OPEN", pdpRequests.get(1).getAction());
		
		assertEquals("aishwarya", pdpRequests.get(0).getUser().getValue("id"));
		assertEquals("duan", pdpRequests.get(1).getUser().getValue("id"));
	}
	
	private String host_recipient_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_Host_Recipient.txt";
	
	@Test
	public void parseJsonXACML5() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(host_recipient_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);
		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("COPY", pdpRequest.getAction());
		assertEquals("171901059", pdpRequest.getHost().getValue("inet_address"));
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSExcel", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Temp/Names.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));
		
		String[] recipients = pdpRequest.getRecipients();
		assertEquals(2, recipients.length);
		assertEquals("shiqiang.duan@nextlabs.com",recipients[0]);
		assertEquals("aishwarya@nextlabs.com",recipients[1]);
		
		IPDPNamedAttributes[] additionalData = pdpRequest.getAdditionalData();
		assertEquals(1, additionalData.length);
		assertEquals("environment",additionalData[0].getName());
		assertEquals("2010-01-11",additionalData[0].getValue("current-date"));
		assertEquals("2010-01-11",additionalData[0].getValue("current-date"));
		assertEquals("13:23:44",additionalData[0].getValue("current-time"));
	}
	
	private String recipient_attrs_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_Recipient_Attrs.txt";

	@Test
	public void parseJsonXACML6() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(recipient_attrs_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);
		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("EMAIL", pdpRequest.getAction());
		assertEquals("171901059", pdpRequest.getHost().getValue("inet_address"));
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSExcel", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Temp/Names.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));

		IPDPNamedAttributes[] additionalData = pdpRequest.getAdditionalData();
		assertEquals(2, additionalData.length);
		
		for (IPDPNamedAttributes namedAttribute : additionalData){
			if ("environment".equalsIgnoreCase(namedAttribute.getName())){
				assertEquals("2010-01-11", namedAttribute.getValue("current-date"));
				assertEquals("13:23:44", namedAttribute.getValue("current-time"));
			}else{
				assertEquals("sendto", namedAttribute.getName());
				assertEquals("ostone", namedAttribute.getValue("id"));
				assertEquals("US", namedAttribute.getValue("nationality"));
				assertEquals("oliver.stone@nextlabs.com", namedAttribute.getValue("email"));
			}
		}
	}
	
	private String extra_attributes_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_OtherAttributes.txt";

	@Test
	public void parseJsonXACML7() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(extra_attributes_request_file);
		List<PDPRequest> pdpRequests = xacmlParser.parseData(data);
		PDPRequest pdpRequest = pdpRequests.get(0);
		log.info("PDP Request :" + ToStringBuilder.reflectionToString(pdpRequest));

		assertEquals("COPY", pdpRequest.getAction());
		assertEquals("171901059", pdpRequest.getHost().getValue("inet_address"));
		assertEquals("Amila", pdpRequest.getUser().getValue("id"));
		assertEquals("MSExcel", pdpRequest.getApplication().getValue("name"));
		assertTrue("Should have a Resource", pdpRequest.getResourceArr().length == 1);

		IPDPResource resource = pdpRequest.getResourceArr()[0];
		assertEquals("C:/Temp/Names.txt", resource.getValue("ce::id"));
		assertEquals("fso", resource.getValue("ce::destinytype"));

		String[] recipients = pdpRequest.getRecipients();
		assertEquals(2, recipients.length);
		assertEquals("shiqiang.duan@nextlabs.com", recipients[0]);
		assertEquals("aishwarya@nextlabs.com", recipients[1]);

		IPDPNamedAttributes[] additionalData = pdpRequest.getAdditionalData();
		assertEquals(2, additionalData.length);

		for (IPDPNamedAttributes namedAttribute : additionalData) {
			if ("environment".equalsIgnoreCase(namedAttribute.getName())) {
				assertEquals("2010-01-11", namedAttribute.getValue("current-date"));
				assertEquals("13:23:44", namedAttribute.getValue("current-time"));
			} else {
				assertEquals("MyNamedAttr", namedAttribute.getName());
				assertEquals("myAttr1Val", namedAttribute.getValue("random1"));
				assertEquals("myAttr2Val", namedAttribute.getValue("random2"));	
			}
		}
	}
	
	
	private String invalid_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_NoAction.txt";

	@Test (expected = InvalidInputException.class)
	public void parseJsonXACML8() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(invalid_request_file);
		xacmlParser.parseData(data);
	}
	
	private String invalid_host_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_NoAction.txt";

	@Test (expected = InvalidInputException.class)
	public void parseJsonXACML9() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(invalid_host_file);
		xacmlParser.parseData(data);
	}
	
	private String invalid_resource_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_NoAction.txt";

	@Test (expected = InvalidInputException.class)
	public void parseJsonXACML10() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(invalid_resource_file);
		xacmlParser.parseData(data);
	}
	
	private String invalid_multi_request_file = "C:/P4/Destiny/D_Nimbus/pcv/Nimbus_REST_Sprint2/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_MultiRequest_SameId.txt";
	
	@Test (expected = InvalidInputException.class)
	public void parseJsonXACML11() throws Exception {

		XACMLParser xacmlParser = XACMLParserFactory.getInstance(Constants.JSON_DATA_TYPE);
		assertNotNull(xacmlParser);

		String data = readFileData(invalid_multi_request_file);
		xacmlParser.parseData(data);
	}

	/**
	 * <p>
	 * 
	 * </p>
	 *
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected String readFileData(String filename) throws FileNotFoundException, IOException {

		FileInputStream fileIo = new FileInputStream(filename);
		byte bytes[] = new byte[fileIo.available()];
		fileIo.read(bytes);
		String data = new String(bytes);
		fileIo.close();
		return data;
	}

}
