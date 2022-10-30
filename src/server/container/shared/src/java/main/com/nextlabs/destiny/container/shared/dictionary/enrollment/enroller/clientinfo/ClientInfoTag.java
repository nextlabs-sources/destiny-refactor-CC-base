/*
 * Created on Mar 26, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/ClientInfoTag.java#1 $
 */

public interface ClientInfoTag {
	String TAG_CLIENT_INFORMATION = "client-information";
	
	String TAG_CLIENT = "client";
	String ATTR_IDENTIFIER = "identifier";
	String ATTR_SHORT_NAME = "short-name";
	String ATTR_LONG_NAME = "long-name";
	
	String TAG_EMAIL_TEMPLATE = "email-template";
	String ATTR_EMAIL_TEMPLATE_NAME = "name";
	
	String TAG_USER = "user";
	String ATTR_USER_NAME = "name";

	
}