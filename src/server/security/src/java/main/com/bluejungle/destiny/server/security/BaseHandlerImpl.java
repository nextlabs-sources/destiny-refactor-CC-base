/*
 * Created on Jan 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.util.Hashtable;
import java.util.List;

/**
 * This is the base Axis handler implementation. This class is abstract, and
 * fills up the basic APIs for Axis handlers.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/security/src/java/main/com/bluejungle/destiny/server/security/BaseHandlerImpl.java#1 $
 */

public abstract class BaseHandlerImpl implements Handler {

    /**
     * Log object
     */
    private static final Log LOG = LogFactory.getLog(BaseHandlerImpl.class);
    /**
     * Request attribute for the X509 certificate
     */
    protected static final String HTTP_REQUEST_CERT_ATTR = "javax.servlet.request.X509Certificate";

    /**
     * Name of the attribute for the servlet request attribute in the Message
     * context.
     */
    protected static final String HTTP_REQUEST_ATTR = "transport.http.servletRequest";

    /**
     * SSL system property values
     */
    protected static final String SPACE = " ";
    protected static final String TRUSTSTORE_PROP_NAME = "nextlabs.javax.net.ssl.trustStore";
    protected static final String TRUSTSTORE_PASS_PROP_NAME = "nextlabs.javax.net.ssl.trustStorePassword";

    private String name;
    private Hashtable options;

    /**
     * This method is called when the handler is destroyed.
     * 
     * @see org.apache.axis.Handler#cleanup()
     */
    public void cleanup() {
        this.options.clear();
        this.name = null;
    }

    /**
     * This function initializes the security handler. It sets up the trust
     * store and loads it based on the current security settings. It is the
     * caller's responsibility to check that the truststore is not null before
     * accessing it.
     * 
     * @see org.apache.axis.Handler#init()
     */
    public void init() {
    }

    /**
     * Called when a subsequent handler throws a fault.
     * 
     * @param context
     *            message context
     * @see org.apache.axis.Handler#onFault(org.apache.axis.MessageContext)
     */
    public void onFault(MessageContext context) {
    }

    /**
     * Can this Handler process this QName?
     * 
     * @param qName
     *            qName type object
     * @return true if the qName can be handled, false otherwise.
     * @see org.apache.axis.Handler#canHandleBlock(javax.xml.namespace.QName)
     */
    public boolean canHandleBlock(QName qName) {
        return false;
    }

    /**
     * Return a list of QNames which this Handler understands In this
     * implementation, there are none
     * 
     * @return the list of understood handlers.
     * @see org.apache.axis.Handler#getUnderstoodHeaders()
     */
    public List getUnderstoodHeaders() {
        return null;
    }

    /**
     * Returns the value of a given option
     * 
     * @param name
     *            option name
     * 
     * @return the value of a given option
     * @see org.apache.axis.Handler#getOption(java.lang.String)
     */
    public Object getOption(String name) {
        return this.options.get(name);
    }

    /**
     * Sets the handler name
     * 
     * @param name
     *            handler name
     * @see org.apache.axis.Handler#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Returns the handler name
     * 
     * @return the handler name
     * @see org.apache.axis.Handler#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the options map. Not implemented since we do not need this.
     * 
     * @return the options map
     * @see org.apache.axis.Handler#getOptions()
     */
    public Hashtable getOptions() {
        return this.options;
    }

    /**
     * @see org.apache.axis.Handler#setOption(java.lang.String,
     *      java.lang.Object)
     */
    public void setOption(String name, Object value) {
        this.options.put(name, value);
    }

    /**
     * Sets the handler options
     * 
     * @param options
     *            map of options
     * @see org.apache.axis.Handler#setOptions(java.util.Hashtable)
     */
    public void setOptions(Hashtable options) {
        this.options = options;
    }

    /**
     * This will return the root element of an XML doc that describes the
     * deployment information about this handler
     * 
     * @param doc
     *            document object
     * @return the root element
     * @see org.apache.axis.Handler#getDeploymentData(org.w3c.dom.Document)
     */
    public Element getDeploymentData(Document doc) {
        return null;
    }

    /**
     * Obtain WSDL information. Some Handlers will implement this by merely
     * setting properties in the MessageContext, others (providers) will take
     * responsibility for doing the "real work" of generating WSDL for a given
     * service.
     * 
     * @param context
     *            message context
     * @throws AxisFault
     *             ia the WSDL cannot be generated
     * @see org.apache.axis.Handler#generateWSDL(org.apache.axis.MessageContext)
     */
    public void generateWSDL(MessageContext context) throws AxisFault {
    }
}
