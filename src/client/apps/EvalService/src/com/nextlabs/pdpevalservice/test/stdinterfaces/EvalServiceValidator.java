package com.nextlabs.pdpevalservice.test.stdinterfaces;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class EvalServiceValidator {
	
	public static final String JSONInputFile = "C:\\work\\EvalService\\SampleJSONRequest.txt";
	
	public static final String SEND_JSON_REQ = "sendRequest"; 
	
	public static final String GET_JSON_ENTRY_POINTS = "getJSONEntryPoints";
	
	public static final String GET_XACML_ENTRY_POINTS = "getXACMLEntryPoints";
	
	public final static void main(String[] args) throws Exception {
		if(args.length==0){
			System.out.println("Usage: EvalServiceValidator [getJSONEntryPoints|getXACMLEntryPoints|sendRequest <input_filename_with_path>]");
			return;
		}		
		if(args[0].equalsIgnoreCase(SEND_JSON_REQ)){
			if(args.length==1){
				System.out.println("Please provide the input filename with complete path");
				System.out.println("Usage: EvalServiceValidator [getJSONEntryPoints|getXACMLEntryPoints|sendRequest <input_filename_with_path>]");				
			}else{
				sendRequest(args[1]);
			}
		}else if(args[0].equalsIgnoreCase(GET_JSON_ENTRY_POINTS)){
			getJSONEndPoints();
		}else if(args[0].equalsIgnoreCase(GET_XACML_ENTRY_POINTS)){
			getXACMLEndPoints();
		}
//        sendJSONRequest(JSONInputFile);
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

	public static void sendRequest(String fileName) throws Exception {
//		String JSONInputFile = "C:\\work\\EvalService\\SampleJSONRequest.txt";
		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://vm-palawan-jpc:8443/dpc/EvalService/pdp");
            String input = getJSONInputStr(fileName);
            String contentType = "application/json";
            if(!input.startsWith("{")){
            	contentType = "application/xacml+xml";
            }
            httpPost.setEntity(new StringEntity(input, ContentType.create(contentType)));
            HttpResponse r = client.execute(httpPost);
            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = null;
            StringBuffer buff = new StringBuffer(); 
            while ((line = rd.readLine()) != null) {
            	buff.append(line);
            }
            System.out.println("Response from server:");
            System.out.println("----------------------------------------");
            System.out.println(buff.toString());
            System.out.println("----------------------------------------");
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
		
	}

	public static void getJSONEndPoints() throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget = new HttpGet("http://vm-palawan-jpc:8443/dpc/EvalService/EvalService?SERVICE=GETENTRYPOINTS&DATATYPE=JSON");
            System.out.println("executing request " + httpget.getURI());
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("Response from server:");
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
	}
	
	public static void getXACMLEndPoints() throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget = new HttpGet("http://vm-palawan-jpc:8443/dpc/EvalService/EvalService?SERVICE=GETENTRYPOINTS&DATATYPE=XACML");
            System.out.println("executing request " + httpget.getURI());
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("Response from server:");
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
	}

}
