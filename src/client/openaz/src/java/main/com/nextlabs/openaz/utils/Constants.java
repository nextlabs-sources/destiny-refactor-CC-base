/*
 * Created on May 02, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/utils/Constants.java#1 $:
 */

package com.nextlabs.openaz.utils;

import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.std.IdentifierImpl;

public interface Constants {
    public static final String DPC_ROOT_PROPERTY = "nextlabs.dpc.root";
    public static final String ENGINE_NAME = "nextlabs.pdp.engine.name";
    
    public static final String URN_NEXTLABS = "urn:nextlabs:names:evalsvc:1.0";
    
    public static final String PDP_REST_EXECUTOR_NAME = "nextlabs.cloudaz.executor_name";
    public static final String PDP_REST_HOST = "nextlabs.cloudaz.host";
    public static final String PDP_REST_PORT = "nextlabs.cloudaz.port";
    public static final String PDP_REST_RESOURCE_PATH = "nextlabs.cloudaz.resource_path";
    public static final String PDP_REST_HTTPS = "nextlabs.cloudaz.https";
    public static final String PDP_REST_IGNORE_HTTPS_CERTIFICATE = "nextlabs.cloudaz.ignore_https_certificate";
    public static final String PDP_REST_AUTH_TYPE  = "nextlabs.cloudaz.auth_type";
    public static final String PDP_REST_CAS_AUTH_USERNAME = "nextlabs.cloudaz.cas_auth.username";
    public static final String PDP_REST_CAS_AUTH_PASSWORD = "nextlabs.cloudaz.cas_auth.password";
    
    public static final String PDP_REST_OAUTH2_SERVER = "nextlabs.cloudaz.oauth2.server";
    public static final String PDP_REST_OAUTH2_PORT = "nextlabs.cloudaz.oauth2.port";
    public static final String PDP_REST_OAUTH2_HTTPS = "nextlabs.cloudaz.oauth2.https";
    public static final String PDP_REST_OAUTH2_TOKEN_ENDPOINT_PATH = "nextlabs.cloudaz.oauth2.token_endpoint_path";
    public static final String PDP_REST_OAUTH2_TOKEN_GRANT_TYPE = "nextlabs.cloudaz.oauth2.grant_type";
    public static final String PDP_REST_OAUTH2_TOKEN_EXPIRES_IN = "nextlabs.cloudaz.oauth2.token_expires_in";
    // for grant_type == password
    public static final String PDP_REST_OAUTH2_USERNAME = "nextlabs.cloudaz.oauth2.username";
    public static final String PDP_REST_OAUTH2_PASSWORD = "nextlabs.cloudaz.oauth2.password";
    // for grant_type == client_credentials
    public static final String PDP_REST_OAUTH2_CLIENT_ID = "nextlabs.cloudaz.oauth2.client_id";
    public static final String PDP_REST_OAUTH2_CLIENT_SECRET = "nextlabs.cloudaz.oauth2.client_secret";
    
    public static final String CAS_TICKET_PATH = "/cas/v1/tickets";
    public static final String PDP_REST_FORM_PARAM_SERVICE = "EVAL";
    public static final String PDP_REST_FORM_PARAM_Version = "1.0";
    
    
    public static final Identifier ID_NEXTLABS = new IdentifierImpl(URN_NEXTLABS);
    public static final Identifier ID_NEXTLABS_ATTRIBUTE_CATEGORY = new IdentifierImpl(ID_NEXTLABS, "attribute-category");
    public static final Identifier ID_NEXTLABS_ATTRIBUTE_CATEGORY_APPLICATION = 
    		new IdentifierImpl(ID_NEXTLABS_ATTRIBUTE_CATEGORY, "application");
    public static final Identifier ID_NEXTLABS_ATTRIBUTE_CATEGORY_HOST = 
    		new IdentifierImpl(ID_NEXTLABS_ATTRIBUTE_CATEGORY, "host");
    public static final Identifier ID_NEXTLABS_APPLICATION_APPLICATION_ID = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "application"), "application-id");
    public static final Identifier ID_RESOURCE_RESOURCE_TYPE = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "resource"), "resource-type");
    
    public static final Identifier ID_NEXTLABS_HOST_IP_ADDR = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "host"), "inet_address");
    public static final Identifier ID_NEXTLABS_HOST_NAME = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "host"), "name");
    
    public static final Identifier ID_NEXTLABS_RECIPIENT_RECIPIENT_ID = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "recipient"), "id");
    public static final Identifier ID_NEXTLABS_RECIPIENT_RECIPIENT_EMAIL = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "recipient"), "email");
    
    public static final Identifier ID_NEXTLABS_POD_POD_ID = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "pod"), "pod-id");
    public static final Identifier ID_NEXTLABS_POD_IGNORE_BUILT_IN = new IdentifierImpl(
    		new IdentifierImpl(ID_NEXTLABS, "pod"), "pod-ignore-built-in");
    
}

