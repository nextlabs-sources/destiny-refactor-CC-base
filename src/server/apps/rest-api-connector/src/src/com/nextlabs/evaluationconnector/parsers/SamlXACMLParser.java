/*
 * Created on Dec 18, 2015
 *
 * All sources, binaries and HTML pages (C) copyright 2015 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/rest-api-connector/src/src/com/nextlabs/evaluationconnector/parsers/SamlXACMLParser.java#1 $:
 */

package com.nextlabs.evaluationconnector.parsers;

import static com.nextlabs.evaluationconnector.utils.Constants.SAML_REQUEST_SKIP_SIGNATURE_CHECK;
import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.getString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImpl;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.ctx.EnvironmentType;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResourceType;
import org.opensaml.xacml.ctx.SubjectType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImpl;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;

public class SamlXACMLParser extends AbstractXACMLParser implements XACMLParser {
    private static final Log log = LogFactory.getLog(SamlXACMLParser.class);
    private static UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
    private BasicParserPool parserPoolManager = null;
    private boolean skipSignatureCheck = false;
    
    public void init() throws EvaluationConnectorException {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ce) {
            throw new EvaluationConnectorException("Unable to initialize SAML parser", ce);
        }
        
        parserPoolManager = new BasicParserPool();
        parserPoolManager.setNamespaceAware(true);

        skipSignatureCheck = getString(SAML_REQUEST_SKIP_SIGNATURE_CHECK).equals("true");
    }

    public List<PDPRequest> parseData(String data) throws InvalidInputException, EvaluationConnectorException {
        InputStream dataAsStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        try {
            Document doc = parserPoolManager.parse(dataAsStream);
            
            if (doc == null) {
                log.error("Invalid SAML query: " + data);
                throw new InvalidInputException("Unable to retrieve document from SAML query");
            }
        
            Element root = doc.getDocumentElement();
            
            if (root == null) {
                log.error("Invalid SAML query: " + data);
                throw new InvalidInputException("Unable to get root from SAML query document");
            }
            
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(root);

            try {
                XMLObject obj = unmarshaller.unmarshall(root);
                
                if (obj instanceof XACMLAuthzDecisionQueryTypeImpl) {
                    XACMLAuthzDecisionQueryTypeImpl query = (XACMLAuthzDecisionQueryTypeImpl)obj;

                    if (!skipSignatureCheck) {
                        validateSignature(query);
                    }
                    
                    return buildPDPRequest(query.getRequest());
                } else {
                    log.error("Data doesn't produce object of type XACMLAuthzDecisionQueryTypeImpl: " + data);
                    throw new InvalidInputException("Object derived from XML not of type XACMLAuthzDecisionQueryTypeImpl");
                }
            } catch (UnmarshallingException ue) {
                throw new EvaluationConnectorException("Unable to unmarshall SAML information", ue);
            } catch (ValidationException ve) {
                throw new EvaluationConnectorException("Unable to validate SAML signature", ve);
            } catch (CertificateException ce) {
                throw new EvaluationConnectorException("Unable to validate SAML signature", ce);
            }
        } catch (XMLParserException xmlpe) {
            throw new EvaluationConnectorException("Unable to retrieve document from SAML query", xmlpe);
        }
    }

    private List<PDPRequest> buildPDPRequest(RequestType request) throws InvalidInputException {
        PDPRequest pdpRequest = new PDPRequest();

        setActionInPDPRequest(pdpRequest, request);

        setResourcesInPDPRequest(pdpRequest, request);

        setUserInPDPRequest(pdpRequest, request);

        setEnvironmentInPDPRequest(pdpRequest, request);
        
        createDummyApplication(pdpRequest);
        
        return Collections.singletonList(pdpRequest);
    }

    private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    
    private void setActionInPDPRequest(PDPRequest pdpRequest, RequestType req) throws InvalidInputException {
        for (AttributeType at: req.getAction().getAttributes()) {
            if (at.getAttributeID().equals(ACTION_ID)) {
                pdpRequest.setAction(getSingleAttributeValue(at));
                return;
            }
        }

        throw new InvalidInputException("Unable to find action in request");
    }

    private void setResourcesInPDPRequest(PDPRequest pdpRequest, RequestType req) throws InvalidInputException {
        List<ResourceType> resources = req.getResources();

        if (resources == null || resources.size() == 0) {
            throw new InvalidInputException("No resources specified in SAML request");
        }

        ArrayList<IPDPResource> pdpResources = new ArrayList<IPDPResource>();

        
        for (ResourceType resource : resources) {
            pdpResources.add(createResource(getAttributeMap(resource.getAttributes())));
        }

        pdpRequest.setResourceArr(pdpResources.toArray(new IPDPResource[pdpResources.size()]));
    }

    private void setUserInPDPRequest(PDPRequest pdpRequest, RequestType req) throws InvalidInputException {
        List<SubjectType> subjects = req.getSubjects();

        if (subjects == null || subjects.size() == 0) {
            throw new InvalidInputException("No subjects (user/host/application) specified in SAML request");
        }

        // We have a number of different subjects and don't really know which ones are which. Until
        // someone comes up with a better idea, we will smoosh all of the attributes together and then
        // create a user based on that.

        Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
        
        for (SubjectType subject : subjects) {
            fillAttributeMap(subject.getAttributes(), attributeMap);
        }

        pdpRequest.setUser(createUser(attributeMap));
    }

    private void setEnvironmentInPDPRequest(PDPRequest pdpRequest, RequestType req) throws InvalidInputException {
        EnvironmentType env = req.getEnvironment();

        if (env == null) {
            // No biggie
            return;
        }

        Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
        
        fillAttributeMap(env.getAttributes(), attributeMap);

        pdpRequest.setAdditionalData(new IPDPNamedAttributes[] { createPDPNamedAttributes(attributeMap , "environment") });;
    }
    
    private void createDummyApplication(PDPRequest pdpRequest) {
        Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();

        attributeMap.put(ATTRIBUTE_APPLICATION_ID, Collections.singletonList("<UNKNOWN>"));

        pdpRequest.setApplication(createApplication(attributeMap));
    }
    
    private static Map<String, List<String>> getAttributeMap(List<AttributeType> attributes) throws InvalidInputException {
        return fillAttributeMap(attributes, new HashMap<String, List<String>>());
    }
    
    /**
     * Convert a list of AttributeTypes into a Map from attribute names (IDs) to values
     *
     * @param attributes the list of AttributeType objects
     * @param attributeMap the map to fill
     * @return attributeMap
     */
    private static Map<String, List<String>> fillAttributeMap(List<AttributeType> attributes, Map<String, List<String>> attributeMap) throws InvalidInputException {
        for (AttributeType at : attributes) {
            attributeMap.put(at.getAttributeID(), getAttributeValues(at));
        }

        return attributeMap;
    }
    
    /**
     * Get a single value from an AttributeType. It is an error if the AttributeType
     * does not contain exactly one value
     *
     * @param attr the AttributeType
     * @return the value
     */
    private static String getSingleAttributeValue(AttributeType attr) throws InvalidInputException {
        return getSingleAttributeValue(attr, true);
    }
    
    /**
     * Get a single value from an AttributeType. The caller can specify if multiple values
     * existing should be treated as an error
     *
     * @param attr the AttributeType
     * @param errorIfMultipleValues true if multiple values should be considered an error
     * @return the value
     */
    private static String getSingleAttributeValue(AttributeType attr, boolean errorIfMultipleValues) throws InvalidInputException {
        List<AttributeValueType> avts = attr.getAttributeValues();

        if (avts == null) {
            throw new InvalidInputException("No attribute values found in AttributeType");
        }

        if (avts.size() == 0 || (errorIfMultipleValues && avts.size() != 1)) {
            throw new InvalidInputException("Attribute contains " + avts.size() + " attribute values");
        }

        return avts.get(0).getValue();
    }

    /**
     * Return all the values (as Strings) from an AttributeType.
     *
     * @param attr the AttributeType
     * @return a List of String
     * @note if the type has no values then the list will be empty
     */
    private static List<String> getAttributeValues(AttributeType attr) throws InvalidInputException {
        List<AttributeValueType> avts = attr.getAttributeValues();

        ArrayList<String> values = new ArrayList<String>();

        if (avts != null) {
            for (AttributeValueType avt : avts) {
                values.add(avt.getValue());
            }
        }

        return values;
    }

    /**
     * Validate the signature in the SAML request
     */
    private static void validateSignature(XACMLAuthzDecisionQueryTypeImpl query) throws CertificateException, ValidationException {
        if (!query.isSigned()) {
            log.debug("SAML query is unsigned. Proceeding...");
        }

        Signature sig = query.getSignature();
        
        // Validate the form. Is this a legit SAML signature?
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(sig);

        // Validate the actual signature itself.
        KeyInfo keyInfo = sig.getKeyInfo();
        java.security.cert.X509Certificate pubKey = KeyInfoHelper.getCertificates(keyInfo).get(0);

        BasicX509Credential cred = new BasicX509Credential();
        cred.setEntityCertificate(pubKey);
        SignatureValidator signatureValidator = new SignatureValidator(cred);
        signatureValidator.validate(sig);
    }
}
