package com.nextlabs.pdpevalservice.test.stdinterfaces;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EvalServiceJSONTest{
	
//	private final static String systemName = "localhost";
//	private final static String systemName = "vm-ubin-cass";
	private static String systemName = "vm-palawan-jpc";
	
//	private final static String portNum = "8080";
	private static String portNum = "8443";
	
	private HttpClient httpClient = null;
	
	private static String contextName = "dpc";
//	private final static String contextName = "EvalService";
	
	private final static String pdpURL = "http://" +systemName+":"+portNum + "/" + contextName + "/EvalService/pdp";
	
    private final String endPtURL = "http://" +systemName+":"+ portNum + "/" + contextName + "/EvalService?SERVICE=GETENTRYPOINTS&DATATYPE=JSON";
	
	private final String fileName = "C:\\work\\EvalService\\SampleJSONRequest_DenyFile.txt";
    
    @Before
	public void initClient(){
		httpClient = new DefaultHttpClient();
//		setupJPC();
		setupLocalhost();
//		setupCass();
	}
	
	private void setupCass() {
		systemName = "vm-ubin-cass";
		portNum = "8080";
		contextName = "EvalService";
	}

	@After
	public void closeConnection(){
		httpClient.getConnectionManager().shutdown();
	}
	
	private void setupJPC() {
		systemName = "vm-palawan-jpc";
		portNum = "8443";
		contextName = "dpc";
	}

	private void setupLocalhost() {
		systemName = "localhost";
		portNum = "8080";
		contextName = "EvalService";
	}

	
	@Test
	public void checkEntryPoints() throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget = new HttpGet(endPtURL);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            String expectedOutput = "{\"link\":[{\"rel\":\"http://docs.oasis-open.org/ns/xacml/relation/pdp\",\"href\":\"/" + contextName + "/EvalService/pdp\"}]}";
            System.out.println(responseBody.trim());
            assertTrue(expectedOutput.equals(responseBody.trim()));

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
	}
	
	@Test
	public void checkInvalidContentType() throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(pdpURL);
        String input = "";
        httpPost.setEntity(new StringEntity(input, ContentType.create("application/dummy")));
        HttpResponse r = httpClient.execute(httpPost);
        assertEquals(400, r.getStatusLine().getStatusCode());
        BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
        String line = null;
        StringBuffer buff = new StringBuffer(); 
        while ((line = rd.readLine()) != null) {
        	System.out.println("Response from the server:"+line);
        	buff.append(line);
        }            
 	}

	@Test
	public void checkInvalidInput() throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(pdpURL);
        String input = "{\"id\":\"value\"";
        httpPost.setEntity(new StringEntity(input, ContentType.create("application/json")));
        HttpResponse r = httpClient.execute(httpPost);
        assertEquals(400, r.getStatusLine().getStatusCode());
	}

	@Test
	public void checkEmptyInput() throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(pdpURL);
        String input = "";
        httpPost.setEntity(new StringEntity(input, ContentType.create("application/json")));
        HttpResponse r = httpClient.execute(httpPost);
        assertEquals(400, r.getStatusLine().getStatusCode());
	}
	
	@Test
	public void checkEvaluation() throws ClientProtocolException, IOException {
        StringBuffer buff = executeRequest(fileName);
        String expectedOutput = "{\"Response\":{\"Result\":{\"Decision\":\"Deny\",\"Status\":{\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:ok\"}},\"Obligations\":[{\"Id\":\"CE::NOTIFY\",\"AttributeAssignment\":[{\"Value\":\"Sorry..You can't access payroll files..\"}]}]}}}";
        assertEquals(expectedOutput, buff.toString().trim());
	}
	
	@Test
	public void checkMissingAttributes() throws ClientProtocolException, IOException{
		String fileName = "C:\\work\\EvalService\\SampleJSONRequest_MissingID.txt";
        StringBuffer buff = executeRequest(fileName);
        String expectedOutput = "{\"Response\":{\"Result\":{\"Status\":{\"StatusMessage\":\"Unable to create instance of 'User': id is null\",\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:missing-attribute\"}}}}}";
        assertEquals(expectedOutput, buff.toString().trim());
	}
	
	@Test
	public void checkDuplicateAttributes() throws ClientProtocolException, IOException{
		String fileName = "C:\\work\\EvalService\\SampleJSONRequest_DuplicateSubjectTag.txt";
        StringBuffer buff = executeRequest(fileName);
        String expectedOutput = "{\"Response\":{\"Result\":{\"Status\":{\"StatusMessage\":\"Subject has already been specified in the 'Category' section\",\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:missing-attribute\"}}}}}";
        assertEquals(expectedOutput, buff.toString().trim());
	}
	
	@Test
	public void checkAllowedFile() throws ClientProtocolException, IOException{
		String fileName = "C:\\work\\EvalService\\SampleJSONRequest_AllowedFile.txt";
        StringBuffer buff = executeRequest(fileName);
        String expectedOutput = "{\"Response\":{\"Result\":{\"Decision\":\"Permit\",\"Status\":{\"StatusCode\":{\"Value\":\"urn:oasis:names:tc:xacml:1.0:status:ok\"}}}}}";
        assertEquals(expectedOutput, buff.toString().trim());
	}

	private StringBuffer executeRequest(String fileName) throws IOException,
			ClientProtocolException {
		String input = getJSONInputStr(fileName);
        HttpPost httpPost = new HttpPost(pdpURL);
        httpPost.setEntity(new StringEntity(input, ContentType.create("application/json")));
        HttpResponse r = httpClient.execute(httpPost); 
        BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
        String line = null;
        StringBuffer buff = new StringBuffer(); 
        while ((line = rd.readLine()) != null) {
        	buff.append(line);
        }
        System.out.println(buff.toString());
		return buff;
	}

	
	public static String getJSONInputStr(String fileName){
		BufferedReader br = null;
		StringBuffer st = new StringBuffer();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(fileName));
			while ((sCurrentLine = br.readLine()) != null) {
				st.append(sCurrentLine);
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return st.toString();
	}


}
