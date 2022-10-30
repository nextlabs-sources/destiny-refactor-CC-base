/*
 * Created on Aug 15, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan & sduan
 */
package com.nextlabs.openaz.pdp.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bluejungle.destiny.agent.pdpapi.*;

public class PDPRequest {
    public static final String DESTINY_TYPE_KEY = "ce::destinytype";
    
    private String action = null;
    private IPDPUser user = null;
    private IPDPHost host = null;
    private IPDPApplication application = null;
    private IPDPResource resource = null; // "To" resource not supported
    private List<IPDPNamedAttributes> additionalData = new ArrayList<IPDPNamedAttributes>();
    
    public PDPRequest() {}
    
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public IPDPUser getUser() {
		return user;
	}
	public void setUser(IPDPUser user) {
		this.user = user;
	}
	public IPDPHost getHost() {
		return host;
	}
	public void setHost(IPDPHost host) {
		this.host = host;
	}
	public IPDPApplication getApplication() {
		return application;
	}
	public void setApplication(IPDPApplication application) {
		this.application = application;
	}
	public IPDPResource getResource() {
		return resource;
	}
	public void setResource(IPDPResource resource) {
		this.resource = resource;
	}
	public List<IPDPNamedAttributes> getAllNamedAttributes() {
		return additionalData;
	}

	public IPDPNamedAttributes getNamedAttributes(String name) {
        for (IPDPNamedAttributes attr : additionalData) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }

        return null;
    }

    public void addNamedAttributes(IPDPNamedAttributes namedAttr) {
        // Remove if it's there already
        for (Iterator<IPDPNamedAttributes> iter = additionalData.listIterator(); iter.hasNext(); ) {
            IPDPNamedAttributes item = iter.next();
            if (item.getName().equals(namedAttr.getName())) {
                iter.remove();
                break;
            }
        }

        additionalData.add(namedAttr);
    }

}
