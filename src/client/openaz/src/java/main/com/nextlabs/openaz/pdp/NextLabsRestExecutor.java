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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlabs.openaz.utils.Constants;
 
class NextLabsRestExecutor implements RestExecutor {
	
	private static final Log logger = LogFactory.getLog(NextLabsRestExecutor.class);
	
	// Some default PDP Rest endpoint Settings
	private static final String DEFAULT_PDP_REST_HOST = "localhost";
	private static final String DEFAULT_PDP_REST_PORT = "58080";
	private static final String DEFAULT_PDP_REST_RESOURCE_PATH = "/dpc/authorization/pdp";
	private static final String DEFAULT_PDP_REST_AUTH_TYPE = "NONE";
	private static final String DEFAULT_PDP_REST_HTTPS = "false";
	private static final String DEFAULT_PDP_REST_IGNORE_HTTPS_CERTIFICATE = "false";
	private static final String DEFAULT_PDP_REST_OAUTH2_HTTPS = "true";
	private static final String DEFAULT_PDP_REST_OAUTH2_PORT = "443";
	private static final String DEFAULT_PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH = "/oauth/token";
	private static final String DEFAULT_PDP_REST_OAUTH2_TOKEN_GRANT_TYPE = "client_credentials";
	private static final String DEFAULT_PDP_REST_OAUTH2_TOKEN_EXPIRES_IN = "3600";
	
	private static final String OAUTH2_TOKEN_REALM = "Bearer";
	
	public enum AuthType {
		NONE, CAS_AUTH, OAUTH2
	}
	
	private CloseableHttpClient httpClient;
	private HttpContext httpContext;
	private Map<String, String> httpHeaders;
	
	private String pdpHost;
	private Integer pdpPort;
	private String pdpResourcePath;
	private Boolean useHttps;
	private Boolean ignoreHttpsCert;
	private AuthType authType;
	// for authType CAS_AUTH only
	private String casUsername;
	private String casPassword;
	// for authType OAUTH2 only
	public enum Oauth2GrantType {
		password, client_credentials
	}
	private Oauth2GrantType oauth2GrantType;
	private String oauth2Server;
	private Integer oauth2Port;
	private Boolean oauth2UseHttps;
	private String oauth2TokenEndpointPath;
	private Integer oauth2TokenExpiresIn;
	// for grant_type == password
	private String oauth2Username;
	private String oauth2Password;
	// for grant_type == client_credentials
	private String oauth2ClientId;
	private String oauth2ClientSecret;
	
	private URI resourceURI;
	
	private void populateFields(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException("properties is null");
		}
		
		pdpHost = properties.getProperty(Constants.PDP_REST_HOST);
		if(StringUtils.isEmpty(pdpHost)) {
			pdpHost = DEFAULT_PDP_REST_HOST;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_HOST, DEFAULT_PDP_REST_HOST));
		} else {
			pdpHost = pdpHost.trim();
		}
		
		String pdpPortString = properties.getProperty(Constants.PDP_REST_PORT);
		if(StringUtils.isEmpty(pdpPortString)) {
			pdpPortString = DEFAULT_PDP_REST_PORT;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_PORT, DEFAULT_PDP_REST_PORT));
		}
		try{
			pdpPort = Integer.parseInt(pdpPortString.trim());
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("pdp_port is not a valid integer: " + pdpPortString, e);
		}
		
		pdpResourcePath = properties.getProperty(Constants.PDP_REST_RESOURCE_PATH);
		if(StringUtils.isEmpty(pdpResourcePath)) {
			pdpResourcePath = DEFAULT_PDP_REST_RESOURCE_PATH;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_RESOURCE_PATH, DEFAULT_PDP_REST_RESOURCE_PATH));
		} else {
			pdpResourcePath = pdpResourcePath.trim();
		}
		
		String useHttpsString = properties.getProperty(Constants.PDP_REST_HTTPS);
		if(StringUtils.isEmpty(useHttpsString)) {
			useHttpsString = DEFAULT_PDP_REST_HTTPS;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_HTTPS, DEFAULT_PDP_REST_HTTPS));
		}
		useHttps = Boolean.parseBoolean(useHttpsString.trim());
		
		String ignoreHttpsCertString = properties.getProperty(Constants.PDP_REST_IGNORE_HTTPS_CERTIFICATE);
		if(StringUtils.isEmpty(ignoreHttpsCertString)) {
			ignoreHttpsCertString = DEFAULT_PDP_REST_IGNORE_HTTPS_CERTIFICATE;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_IGNORE_HTTPS_CERTIFICATE, DEFAULT_PDP_REST_IGNORE_HTTPS_CERTIFICATE));
		}
		ignoreHttpsCert = Boolean.parseBoolean(ignoreHttpsCertString.trim());
		
		String authTypeString = properties.getProperty(Constants.PDP_REST_AUTH_TYPE);
		if(StringUtils.isEmpty(authTypeString)) {
			authTypeString = DEFAULT_PDP_REST_AUTH_TYPE;
			logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_AUTH_TYPE, DEFAULT_PDP_REST_AUTH_TYPE));
		}
		try{
			authType = AuthType.valueOf(authTypeString.trim().toUpperCase());
		}catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("No valid AuthType can be found of value: " + authTypeString);
		}
		
		if(authType == AuthType.CAS_AUTH) {
			casUsername = properties.getProperty(Constants.PDP_REST_CAS_AUTH_USERNAME);
			if(StringUtils.isEmpty(casUsername)) {
				throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
						Constants.PDP_REST_CAS_AUTH_USERNAME));
			} else {
				casUsername = casUsername.trim();
			}
			casPassword = properties.getProperty(Constants.PDP_REST_CAS_AUTH_PASSWORD);
			if(StringUtils.isEmpty(casPassword)) {
				throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
						Constants.PDP_REST_CAS_AUTH_PASSWORD));
			} else {
				casPassword = casPassword.trim();
			}
		} else if (authType == AuthType.OAUTH2) {
			oauth2Server = properties.getProperty(Constants.PDP_REST_OAUTH2_SERVER);
			if (StringUtils.isEmpty(oauth2Server)) {
				oauth2Server = pdpHost;
				logger.debug(String.format("Value of %s not found, use PDPHost: %s",
					Constants.PDP_REST_OAUTH2_SERVER, pdpHost));
			} else {
				oauth2Server = oauth2Server.trim();
			}
			
			String oauth2PortString = properties.getProperty(Constants.PDP_REST_OAUTH2_PORT);
			if(StringUtils.isEmpty(oauth2PortString)) {
				oauth2PortString = DEFAULT_PDP_REST_OAUTH2_PORT;
				logger.debug(String.format("Value of %s not found, use default value: %s",
						Constants.PDP_REST_OAUTH2_PORT, DEFAULT_PDP_REST_OAUTH2_PORT));
			}
			try{
				oauth2Port = Integer.parseInt(oauth2PortString.trim());
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("pdp_port is not a valid integer: " + oauth2PortString, e);
			}
			
			String oauth2TokenExpiresString = properties.getProperty(Constants.PDP_REST_OAUTH2_TOKEN_EXPIRES_IN);
			if(StringUtils.isEmpty(oauth2TokenExpiresString)) {
				oauth2TokenExpiresString = DEFAULT_PDP_REST_OAUTH2_TOKEN_EXPIRES_IN;
				logger.debug(String.format("Value of %s not found, use default value: %s",
						Constants.PDP_REST_OAUTH2_TOKEN_EXPIRES_IN, DEFAULT_PDP_REST_OAUTH2_TOKEN_EXPIRES_IN));
			}
			try{
				oauth2TokenExpiresIn = Integer.parseInt(oauth2TokenExpiresString.trim());
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("token_expires is not a valid integer: "
						+ oauth2TokenExpiresString, e);
			}
			
			String oauth2UseHttpsString = properties.getProperty(Constants.PDP_REST_OAUTH2_HTTPS);
			if(StringUtils.isEmpty(oauth2UseHttpsString)) {
				oauth2UseHttpsString = DEFAULT_PDP_REST_OAUTH2_HTTPS;
				logger.debug(String.format("Value of %s not found, use default value: %s",
						Constants.PDP_REST_OAUTH2_HTTPS, DEFAULT_PDP_REST_OAUTH2_HTTPS));
			}
			oauth2UseHttps = Boolean.parseBoolean(oauth2UseHttpsString.trim());
			
			oauth2TokenEndpointPath = properties.getProperty(Constants.PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH);
			if (StringUtils.isEmpty(oauth2TokenEndpointPath)) {
				oauth2TokenEndpointPath = DEFAULT_PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH;
				logger.debug(String.format("Value of %s not found, use default value: %s",
					Constants.PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH, DEFAULT_PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH));
			} else {
				oauth2TokenEndpointPath = oauth2TokenEndpointPath.trim();
			}
			
			String oauth2GrantTypeString = properties.getProperty(Constants.PDP_REST_OAUTH2_TOKEN_GRANT_TYPE);
			if(StringUtils.isEmpty(oauth2GrantTypeString)) {
				oauth2GrantTypeString = DEFAULT_PDP_REST_OAUTH2_TOKEN_GRANT_TYPE;
				logger.debug(String.format("Value of %s not found, use default value: %s",
						Constants.PDP_REST_OAUTH2_TOKEN_GRANT_TYPE, DEFAULT_PDP_REST_OAUTH2_TOKEN_GRANT_TYPE));
			}
			try{
				oauth2GrantType = Oauth2GrantType.valueOf(oauth2GrantTypeString.trim().toLowerCase());
			}catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("No valid Oauth2GrantType can be found of value: " + oauth2GrantTypeString);
			}
			
			if (oauth2GrantType == Oauth2GrantType.password) {
				oauth2Username = properties.getProperty(Constants.PDP_REST_OAUTH2_USERNAME);
				if (StringUtils.isEmpty(oauth2Username)) {
					throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
							Constants.PDP_REST_OAUTH2_USERNAME));
				} else {
					oauth2Username = oauth2Username.trim();
				}
				
				oauth2Password= properties.getProperty(Constants.PDP_REST_OAUTH2_PASSWORD);
				if (StringUtils.isEmpty(oauth2Password)) {
					throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
							Constants.PDP_REST_OAUTH2_PASSWORD));
				} else {
					oauth2Password = oauth2Password.trim();
				}
			} else if (oauth2GrantType == Oauth2GrantType.client_credentials) {
				oauth2ClientId = properties.getProperty(Constants.PDP_REST_OAUTH2_CLIENT_ID);
				if (StringUtils.isEmpty(oauth2ClientId)) {
					throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
							Constants.PDP_REST_OAUTH2_CLIENT_ID));
				} else {
					oauth2ClientId = oauth2ClientId.trim();
				}
				
				oauth2ClientSecret = properties.getProperty(Constants.PDP_REST_OAUTH2_CLIENT_SECRET);
				if (StringUtils.isEmpty(oauth2ClientSecret)) {
					throw new IllegalArgumentException(String.format("No value of %s found in the properties.", 
							Constants.PDP_REST_OAUTH2_CLIENT_SECRET));
				} else {
					oauth2ClientSecret = oauth2ClientSecret.trim();
				}
			}
		}
	}
	
	private void init() {
		PoolingHttpClientConnectionManager cm = null;
		if (ignoreHttpsCert) {
			// ignore HTTPS SSL cert errors
			SSLContextBuilder sslCxBuilder = new SSLContextBuilder();
		    try {
				sslCxBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				        sslCxBuilder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
				        .<ConnectionSocketFactory> create()
				        .register("http", PlainConnectionSocketFactory.getSocketFactory())
				        .register("https", sslsf)
				        .build();
				cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			} catch (Exception e) {
				throw new RestExecutorException("Error load loadTrustMaterial for httpClient", e);
			}
		} else {
			cm = new PoolingHttpClientConnectionManager();
		}
		
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 100
		cm.setDefaultMaxPerRoute(100);
		
		// Build the client. (follow POST redirect)
		httpClient = HttpClients.custom()
				.setConnectionManager(cm)
				.setRedirectStrategy(new LaxRedirectStrategy())
				.build();
		
		String httpSchema = useHttps ? "https" : "http";
		// hiding default port on http and https,
		// so that calling resourceURI.toString() will return https://pdp.example.com instead of https://pdp.example.com:443
		// this is required for casAuthGetServiceTicket method
		int port = pdpPort;
		if (useHttps && pdpPort == 443) {
			port = -1;
		} else if (!useHttps && pdpPort == 80) {
			port = -1;
		}
		
		try {
			resourceURI = new URIBuilder()
					.setScheme(httpSchema)
					.setHost(pdpHost)
					.setPort(port)
					.setPath(pdpResourcePath).build();
		} catch (URISyntaxException e) {
			throw new RestExecutorException("Error create endpoint URI: ", e);
		}
		
		// Create HttpContext
		CookieStore cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		
		httpHeaders = new ConcurrentHashMap<String, String>();
	}
	
	public NextLabsRestExecutor(Properties properties) {
		populateFields(properties);
		init();
	}
	
	private String rawCall(String payload) {
		
		HttpPost httpPost = new HttpPost(resourceURI);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
		nvps.add(new BasicNameValuePair("Service", Constants.PDP_REST_FORM_PARAM_SERVICE));
		nvps.add(new BasicNameValuePair("DataType", "xml"));
		nvps.add(new BasicNameValuePair("Version", Constants.PDP_REST_FORM_PARAM_Version));
		nvps.add(new BasicNameValuePair("data", payload));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RestExecutorException("Error preparing HTTP payload", e);
		}
		
		for (Map.Entry<String, String> h: httpHeaders.entrySet()) {
			httpPost.setHeader(h.getKey(), h.getValue());
		}
		
		try {
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				HttpEntity entity = response.getEntity();
				if(entity != null) {
					return EntityUtils.toString(entity);
				} else {
					throw new RestExecutorException("Reponse entity is null");
				}
			} else if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new UnAuthorizedException();
			} else {
				throw new RestExecutorException(
						"Got unexpected HTTP status code: " + response.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			throw new RestExecutorException(e);
		} catch (IOException e) {
			throw new RestExecutorException(e);
		}
	}
	
	@Override
	public String xmlCall(String xmlRequest) throws RestExecutorException {
		try{
			String response = rawCall(xmlRequest);
			return response;
		} catch(UnAuthorizedException e) {
			if(authType == AuthType.CAS_AUTH) {
				logger.info("Got unauthorized response, try authenticate using CAS credential and retry");
				try {
					casAuth();
					return rawCall(xmlRequest);
				} catch(UnAuthorizedException e1) {
					throw new RestExecutorException(e1);
				}
			} else if (authType == AuthType.OAUTH2) {
				logger.info("Got unauthorized response, try request new Oauth2 token and retry");
				try {
					oauth2TokenAuth();
					return rawCall(xmlRequest);
				} catch (UnAuthorizedException e1) {
					throw new RestExecutorException(e1);
				}
			} else {
				throw new RestExecutorException(e);
			}
		}
	}
	
	private synchronized void casAuth() {
		String ticketGrantingTicketEndpoint = casAuthGetTicketGrantingTicket();
		String serviceTicket = casAuthGetServiceTicket(ticketGrantingTicketEndpoint);
		casAuthObtainCookie(serviceTicket);
		logger.info("Authenticated to server.");
	}
	
	/**
	 * Call CAS Rest API to get the ticket granting ticket
	 * @return Endpoint URL containing the ticket granting ticket
	 */
	private String casAuthGetTicketGrantingTicket() {
		URI authURI = null;
		try {
			String httpSchema = useHttps ? "https" : "http";
			authURI = new URIBuilder()
					.setScheme(httpSchema)
					.setHost(pdpHost)
					.setPort(pdpPort)
					.setPath(Constants.CAS_TICKET_PATH).build();
		} catch (URISyntaxException e) {
			throw new RestExecutorException("Error create CAS auth endpoint URI: ", e);
		}
		
		HttpPost tgtReq = new HttpPost(authURI);
		List<NameValuePair> tgtNvps = new ArrayList<NameValuePair>();
		tgtNvps.add(new BasicNameValuePair("username", casUsername));
		tgtNvps.add(new BasicNameValuePair("password", casPassword));
		try {
			tgtReq.setEntity(new UrlEncodedFormEntity(tgtNvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RestExecutorException("Error preparing TGT HTTP payload", e);
		}
		
		String tgtEndpoint = null;
		try {
			HttpResponse tgtRes = httpClient.execute(tgtReq);
			if (tgtRes.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
				tgtEndpoint = tgtRes.getFirstHeader("Location").getValue();
			} else {
				throw new RestExecutorException("Error calling TGT request");
			}
		} catch (ClientProtocolException e) {
			throw new RestExecutorException(e);
		} catch (IOException e) {
			throw new RestExecutorException(e);
		}
		
		return tgtEndpoint;
	}
	
	/**
	 * Call CAS Rest API to get the service ticket
	 * @param tgtEndpoint The Endpoint URL containing the ticket granting ticket
	 * @return Service ticket string
	 */
	private String casAuthGetServiceTicket(String tgtEndpoint) {
		HttpPost stReq = new HttpPost(tgtEndpoint);
		List<NameValuePair> stNvps = new ArrayList<NameValuePair>();
		stNvps.add(new BasicNameValuePair("service", resourceURI.toString()));
		
		try {
			stReq.setEntity(new UrlEncodedFormEntity(stNvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RestExecutorException("Error preparing ST HTTP payload", e);
		}
		
		String serviceTicket = null;
		try {
			HttpResponse stRes = httpClient.execute(stReq);
			if (stRes.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				serviceTicket = EntityUtils.toString(stRes.getEntity());
			} else {
				throw new RestExecutorException("Error calling ST request");
			}
		} catch (ClientProtocolException e) {
			throw new RestExecutorException(e);
		} catch (IOException e) {
			throw new RestExecutorException(e);
		}
		return serviceTicket;
	}
	
	/**
	 * Call service endpoint with service ticket to let the HttpContext store CAS Cookie
	 * @param serviceTicket
	 */
	private void casAuthObtainCookie(String serviceTicket) {
		String getCookieurl = null;
		try {
			getCookieurl = String.format("%s?ticket=%s", resourceURI.toString(),
					URLEncoder.encode(serviceTicket, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RestExecutorException("Error create url for getting cas cookie: ", e);
		}
		
		HttpGet httpGet = new HttpGet(getCookieurl);
		
		try {
			HttpResponse getCookieReq = httpClient.execute(httpGet, httpContext);
			if (getCookieReq.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new RestExecutorException("Authentication failed, wrong password?");
			}
		} catch (ClientProtocolException e) {
			throw new RestExecutorException(e);
		} catch (IOException e) {
			throw new RestExecutorException(e);
		}
	}
	
	private void oauth2TokenAuth() {
		URI tokenURI = null;
		try {
			String httpSchema = oauth2UseHttps ? "https" : "http";
			tokenURI = new URIBuilder()
					.setScheme(httpSchema)
					.setHost(oauth2Server)
					.setPort(oauth2Port)
					.setPath(oauth2TokenEndpointPath).build();
		} catch (URISyntaxException e) {
			throw new RestExecutorException("Error create Oauth2 auth endpoint URI: ", e);
		}
		
		HttpPost tokenReq = new HttpPost(tokenURI);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
		if (oauth2GrantType == Oauth2GrantType.password) {
			nvps.add(new BasicNameValuePair("grant_type", oauth2GrantType.toString()));
			nvps.add(new BasicNameValuePair("username", oauth2Username));
			nvps.add(new BasicNameValuePair("password", oauth2Password));
		} else if (oauth2GrantType == Oauth2GrantType.client_credentials) {
			nvps.add(new BasicNameValuePair("grant_type", oauth2GrantType.toString()));
			nvps.add(new BasicNameValuePair("client_id", oauth2ClientId));
			nvps.add(new BasicNameValuePair("client_secret", oauth2ClientSecret));
		} else {
			throw new IllegalStateException("Unsupport oauth2 grant type: " + oauth2GrantType);
		}
		nvps.add(new BasicNameValuePair("expires_in", oauth2TokenExpiresIn.toString()));
		
		try {
			tokenReq.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RestExecutorException("Error preparing token payload", e);
		}
		
		try {
			HttpResponse tokenRes = httpClient.execute(tokenReq);
			if (tokenRes.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> tokenData = 
						mapper.readValue(EntityUtils.toString(tokenRes.getEntity()), Map.class);
				String error = (String) tokenData.get("error"); 
				if (error != null) {
					logger.error("Error request token from server: " + error);
					throw new RestExecutorException("Authentication failed, wrong credential?");
				} else {
					String token_type = (String) tokenData.get("token_type");
					if (!OAUTH2_TOKEN_REALM.equals(token_type)) {
						throw new RestExecutorException("Unsupported token type received: " + token_type);
					}
					String token_value = (String) tokenData.get("access_token");
					if (StringUtils.isNotEmpty(token_value)) {
						httpHeaders.put("Authorization", OAUTH2_TOKEN_REALM + " " + token_value);
						logger.debug("Set Authorization header");
					}
				}
			} else {
				throw new RestExecutorException("Error calling oauth2 token request");
			}
		} catch (ClientProtocolException e) {
			throw new RestExecutorException(e);
		} catch (IOException e) {
			throw new RestExecutorException(e);
		}
	}
	
}

class UnAuthorizedException extends RuntimeException {
	private static final long serialVersionUID = 5820739064171385258L;

	public UnAuthorizedException() {
		super();
	}
	
}