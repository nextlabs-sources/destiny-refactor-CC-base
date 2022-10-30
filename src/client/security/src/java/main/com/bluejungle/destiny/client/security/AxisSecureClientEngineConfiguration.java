/*
 * Created on Feb 26, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.security;

import org.apache.axis.AxisEngine;
import org.apache.axis.ConfigurationException;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides EngineConfiguration that searches the classpath when resolving
 * Entity references within client-config.wsdd
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/client/security/src/java/main/com/bluejungle/destiny/client/security/AxisSecureClientEngineConfiguration.java#3 $
 */

class AxisSecureClientEngineConfiguration extends BaseWSDDEngineConfiguration {

    private static final Log LOG = LogFactory.getLog(AxisSecureClientEngineConfiguration.class.getName());

    private static final String CLIENT_CONFIG_RESOURCE_NAME = "com/bluejungle/destiny/client/axis/client-config.wsdd";

    /**
     * @see org.apache.axis.EngineConfiguration#configureEngine(org.apache.axis.AxisEngine)
     */
    public void configureEngine(AxisEngine engine) throws ConfigurationException {
        InputStream clientConfigInputStream = getClientConfigInputStream();
        InputSource inputSource = new InputSource(clientConfigInputStream);
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            Document clientConfigDocument = documentBuilder.parse(inputSource);

            WSDDDocument wsddDocument = new WSDDDocument(clientConfigDocument);
            WSDDDeployment deployment = wsddDocument.getDeployment();
            deployment.configureEngine(engine);

            setDeployment(deployment);

            engine.refreshGlobalOptions();
        } catch (ParserConfigurationException exception) {
            LOG.error("Error creating XML parser.  Failed to parse client-config.wsdd and initialize Axis", exception);
            throw new ConfigurationException(exception);
        } catch (IOException exception) {
            LOG.error("Failed to read client-config.wsdd.  Axis was not initialized properly", exception);
            throw new ConfigurationException(exception);
        } catch (SAXException exception) {
            LOG.error("Failed to parse client-config.wsdd.  Axis was not initialized properly", exception);
            throw new ConfigurationException(exception);
        }

    }

    /**
     * @see org.apache.axis.EngineConfiguration#writeEngineConfig(org.apache.axis.AxisEngine)
     */
    public void writeEngineConfig(AxisEngine engine) throws ConfigurationException {
        // DO NOTHING
    }

    /**
     * Retrieve the input stream of the clieng config wsdd file. By default,
     * loads from the classpath of this classes' classloader the resource
     * {@link #CLIENT_CONFIG_RESOURCE_NAME}}. May be overidden by subclasses to
     * provide alternative behavior
     * 
     * @return the input stream of the clieng config wsdd file
     */
    protected InputStream getClientConfigInputStream() {
        return getClass().getClassLoader().getResourceAsStream(CLIENT_CONFIG_RESOURCE_NAME);
    }

    /**
     * Retrieve the document builder to parse the client config file
     * 
     * @return a new document builder
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    private DocumentBuilder getDocumentBuilder() throws FactoryConfigurationError, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new ClassPathEntityResolver());

        return documentBuilder;
    }

    /**
     * @author sgoldstein
     */
    private class ClassPathEntityResolver implements EntityResolver {

        /**
         * <code>FILE_PROTOCOL</code>
         */
        private static final String FILE_PROTOCOL = "file:";

        private ClassLoader classLoader;

        private ClassPathEntityResolver() {
            this.classLoader = getClass().getClassLoader();
        }

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
         *      java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            InputSource valueToReturn = null;
            if ((publicId == null) && (systemId != null)) {
                if (systemId.startsWith(FILE_PROTOCOL)) {
                    systemId = systemId.substring(FILE_PROTOCOL.length());
                }

                InputStream resourceInputStream = this.classLoader.getResourceAsStream(systemId);
                if (resourceInputStream == null) {
                    StringBuffer errorMessage = new StringBuffer("Failed to resolve entity, ");
                    errorMessage.append(systemId);
                    errorMessage.append(", within the current classpath.  Please verify existence of this resource.");
                    LOG.warn(errorMessage.toString());
                } else {
                    valueToReturn = new InputSource(resourceInputStream);
                }
            }

            return valueToReturn;
        }

    }
}
