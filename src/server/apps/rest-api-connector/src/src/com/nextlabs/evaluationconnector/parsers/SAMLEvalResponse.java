/*
 * Created on Jan 04, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/rest-api-connector/src/src/com/nextlabs/evaluationconnector/parsers/SAMLEvalResponse.java#1 $:
 */

package com.nextlabs.evaluationconnector.parsers;

import static com.nextlabs.evaluationconnector.utils.Constants.CONTENT_TYPE_SAML_XACML;
import static com.nextlabs.evaluationconnector.utils.Constants.SAML_RESPONSE_KEYSTORE_FILENAME;
import static com.nextlabs.evaluationconnector.utils.Constants.SAML_RESPONSE_KEYSTORE_ENTRY_ID;
import static com.nextlabs.evaluationconnector.utils.Constants.SAML_RESPONSE_KEYSTORE_PASSWORD;
import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.getString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.HashMap;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.ResponseType; 
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResultType; 
import org.opensaml.xacml.ctx.impl.DecisionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResponseTypeImplBuilder; 
import org.opensaml.xacml.ctx.impl.ResultTypeImplBuilder; 
import org.opensaml.xacml.policy.AttributeAssignmentType;
import org.opensaml.xacml.policy.ObligationType;
import org.opensaml.xacml.policy.ObligationsType;
import org.opensaml.xacml.policy.impl.AttributeAssignmentTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationsTypeImplBuilder;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.XMLObjectBuilderFactory;

import org.w3c.dom.Element;

import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.utils.Constants;

public class SAMLEvalResponse implements EvalResponse {
    private static final Log log = LogFactory.getLog(SAMLEvalResponse.class);
    private XMLObjectBuilderFactory builderFactory;
    private MarshallerFactory marshallerFactory;
    
    private AssertionBuilder assertionBuilder;
    private AttributeAssignmentTypeImplBuilder attributeTypeBuilder;
    private DecisionTypeImplBuilder decisionBuilder;
    private IssuerBuilder issuerBuilder;
    private KeyInfoBuilder keyInfoBuilder;
    private ObligationTypeImplBuilder obligationTypeBuilder;
    private ObligationsTypeImplBuilder obligationsTypeBuilder;
    private ResponseBuilder responseBuilder;
    private ResponseTypeImplBuilder responseTypeBuilder;
    private ResultTypeImplBuilder resultTypeBuilder;
    private SignatureBuilder signatureBuilder;
    private X509CertificateBuilder x509CertificateBuilder;
    private X509DataBuilder x509DataBuilder;
    private XACMLAuthzDecisionStatementTypeImplBuilder xacmlAuthzStatementBuilder;
    
    private X509Credential responseCredential;
    private HashMap<String, String> keyTypeToSignatureType = new HashMap<String, String>();
    
    @Override
    public void init() throws EvaluationConnectorException {
        builderFactory = Configuration.getBuilderFactory();
        marshallerFactory = Configuration.getMarshallerFactory();
        
        assertionBuilder = (AssertionBuilder)builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        attributeTypeBuilder = (AttributeAssignmentTypeImplBuilder)builderFactory.getBuilder(AttributeAssignmentType.DEFAULT_ELEMENT_NAME);
        decisionBuilder = (DecisionTypeImplBuilder)builderFactory.getBuilder(DecisionType.DEFAULT_ELEMENT_NAME);
        issuerBuilder = (IssuerBuilder)builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        keyInfoBuilder = (KeyInfoBuilder) builderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
        obligationTypeBuilder = (ObligationTypeImplBuilder)builderFactory.getBuilder(ObligationType.DEFAULT_ELEMENT_QNAME);
        obligationsTypeBuilder = (ObligationsTypeImplBuilder)builderFactory.getBuilder(ObligationsType.DEFAULT_ELEMENT_QNAME);
        responseBuilder = (ResponseBuilder)builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
        responseTypeBuilder = (ResponseTypeImplBuilder)builderFactory.getBuilder(ResponseType.DEFAULT_ELEMENT_NAME);
        resultTypeBuilder = (ResultTypeImplBuilder)builderFactory.getBuilder(ResultType.DEFAULT_ELEMENT_NAME);
        signatureBuilder = (SignatureBuilder)builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        x509CertificateBuilder = (X509CertificateBuilder) builderFactory.getBuilder(X509Certificate.DEFAULT_ELEMENT_NAME);
        x509DataBuilder = (X509DataBuilder) builderFactory.getBuilder(X509Data.DEFAULT_ELEMENT_NAME);
        xacmlAuthzStatementBuilder = (XACMLAuthzDecisionStatementTypeImplBuilder)builderFactory.getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);

        try {
            responseCredential = buildCredential();
        } catch (EvaluationConnectorException ece) {
            log.warn("Exception getting credentials. Responses will not be signed", ece);
        }

        // Used to convert the certificate key type into a signature type. This list should be expanded
        keyTypeToSignatureType.put("DSA", SignatureConstants.ALGO_ID_SIGNATURE_DSA);
        keyTypeToSignatureType.put("RSA", SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
    }

    @Override
    public void handleResponse(HttpServletResponse httpResponse,
                               XACMLResponse response) throws EvaluationConnectorException {
        long startCounter = System.nanoTime();
        Response samlResponse = buildSAML(response);

        Signature sig = null;
        
        if (responseCredential != null) {
            sig = signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);
            sig.setSigningCredential(responseCredential);
            sig.setSignatureAlgorithm(getSignatureAlgorithm(responseCredential));
            sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            try {
                // This must be unique for each SAML object
                KeyInfo responseKeyInfo = buildKeyInfo(responseCredential);
                
                sig.setKeyInfo(responseKeyInfo);
            } catch (CertificateEncodingException cee) {
                throw new EvaluationConnectorException("Unable to build key info from credentials", cee);
            }
            samlResponse.setSignature(sig);
        }
        
        Marshaller marshaller = marshallerFactory.getMarshaller(samlResponse);

        if (marshaller == null) {
            throw new NullPointerException("Couldn't find marshaller for samlResponse");
        }

        try {
            // Can't compute the signature until after we have marhsalled the response
            Element responseElement = marshaller.marshall(samlResponse);

            if (sig != null) {
                try {
                    Signer.signObject(sig);
                } catch (SignatureException se) {
                    throw new EvaluationConnectorException("Error signing response", se);
                }
            }

            // Use this instead of XMLHelper.prettyPrintXML(). Pretty printing changes the DOM, which
            // mean that signature validation will fail on the receiving side
            String responseString = XMLHelper.nodeToString(responseElement);
            
            httpResponse.setContentType(CONTENT_TYPE_SAML_XACML);
            httpResponse.setContentLength(responseString.length());
            
            if (log.isDebugEnabled()) {
                long policyEvalTime = response.getPdpEvalTime();
                long restApiResponseTime = (System.nanoTime() - response.getRequestStartTime());
                
                httpResponse.setHeader(Constants.POLICY_EVAL_TIME, String.valueOf(policyEvalTime));
                httpResponse.setHeader(Constants.REST_API_RESPONSE_TIME, String.valueOf(restApiResponseTime));
            }
            
            if (log.isDebugEnabled()) {
                log.debug( String.format("%s, Thread Id:[%s],  SAMLEvalResponse -> Total Response creation Time : %d nano",
                                         Constants.PERF_LOG_PREFIX, "" + Thread.currentThread().getId(),
                                         (System.nanoTime() - startCounter)));
                
                log.debug("SAMLEvalResponse is written to response successfully, [Response : " + responseString + "]");
            }
        
            PrintWriter pWriter = httpResponse.getWriter();
            pWriter.write(responseString);
        } catch (MarshallingException me) {
            throw new EvaluationConnectorException("Error marshalling response", me);
        } catch (IOException ioe) {
            throw new EvaluationConnectorException("Error getting http response writer", ioe);
        }
    }

    /**
     * Convert a XACML response into an OpenSAML response object
     * @param response XACML response
     * @return the OpenSAML response
     */
    public Response buildSAML(XACMLResponse response) {
        Response samlResponse = responseBuilder.buildObject();

        // A SAML response consists of multiple Assertions. Each
        // Assertion can have multiple Statements. Statements have
        // a response. It's not entirely clear what is the best
        // way to break this down, but putting each ResultType in
        // its own Statement seems perfectly reasonable
            
        for (oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType xacmlResult : response.getResponseType().getResult()) {
            Assertion assertion = assertionBuilder.buildObject();
            assertion.setVersion(SAMLVersion.VERSION_20);
            assertion.setIssuer(buildIssuer("www.nextlabs.com"));
            // JODA
            // assertion.setIssueInstance(new DateTime());
            XACMLAuthzDecisionStatementType samlXACMLStatement = buildStatement(xacmlResult);
            assertion.getStatements().add(samlXACMLStatement);

            samlResponse.getAssertions().add(assertion);
        }
            
        return samlResponse;
    }

    /**
     * Build an OpenSAML statement containing the XACML result
     * @param xacmlResult XACML result
     * @return an OpenSAML XACMLAuthzDecisionStatementType
     */
    private XACMLAuthzDecisionStatementType buildStatement(oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType xacmlResult) {
        XACMLAuthzDecisionStatementType statement = xacmlAuthzStatementBuilder.buildObject(XACMLAuthzDecisionStatementType.DEFAULT_ELEMENT_NAME, XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);

        ResponseType samlXACMLResponse = buildResponse(xacmlResult);

        statement.setResponse(samlXACMLResponse);
            
        return statement;
    }
           
    /**
     * Build an OpenSAML ResponseType object containing the XACML ResultType
     * @param xacmlResult XACML ResultType
     * @return OpenSAML ResponseType
     */
    private ResponseType buildResponse(oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType xacmlResult) {
        ResponseType samlXACMLResponse = responseTypeBuilder.buildObject();

        samlXACMLResponse.setResult(buildResult(xacmlResult));
        return samlXACMLResponse;
    }

    /**
     * Populate an OpenSAML ResultType with the contents of the XACML ResultType
     * @param xacmlResult the XACML ResultType
     * @return OpenSAML ResultType
     */
    private ResultType buildResult(oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType xacmlResult) {
        ResultType samlXACMLResultType = resultTypeBuilder.buildObject();

        samlXACMLResultType.setDecision(buildDecisionType(xacmlResult.getDecision()));
        samlXACMLResultType.setObligations(buildObligationsType(xacmlResult.getObligations()));
        return samlXACMLResultType;
    }

    /**
     * Build an Issuer from a String
     * @param value the name of the issuer (e.g. www.nextlabs.com)
     * @return the Issuer object
     */
    private Issuer buildIssuer(String value) {
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(value);
        return issuer;
    }

    /**
     * Convert a XACML DecisionType into an OpenSAML DecisionType
     * @param xacmlDecision the XACML DecisionType
     * @return the OpenSAML DecisionType
     * @note "Indeterminate" will be returned if, for any reason, the XACML DecisionType can't be understood
     */
    private DecisionType buildDecisionType(oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType xacmlDecision) {
        DecisionTypeImplBuilder builder = (DecisionTypeImplBuilder)builderFactory.getBuilder(DecisionType.DEFAULT_ELEMENT_NAME);
        DecisionType samlDecision = decisionBuilder.buildObject();

        if (xacmlDecision == oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType.PERMIT) {
            samlDecision.setDecision(DecisionType.DECISION.Permit);
        } else if (xacmlDecision == oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType.DENY) {
            samlDecision.setDecision(DecisionType.DECISION.Deny);
        } else if (xacmlDecision == oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType.NOT_APPLICABLE) {
            samlDecision.setDecision(DecisionType.DECISION.NotApplicable);
        } else {
            samlDecision.setDecision(DecisionType.DECISION.Indeterminate);
        }

        return samlDecision;
    }

    /**
     * Convert the XACML obligations into OpenSAML ObligationsType
     * @param xacmlObligations the XACML obligations
     * @return the OpenSAML ObligationsType
     * @note if there are no obligations then <code>null</code> is returned
     */
    private ObligationsType buildObligationsType(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType xacmlObligations) {
        if (xacmlObligations == null) {
            return null;
        }
        
        ObligationsType samlXACMLObligations = obligationsTypeBuilder.buildObject();
            
        for (oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationType xacmlObligationType : xacmlObligations.getObligation()) {
            samlXACMLObligations.getObligations().add(buildObligationType(xacmlObligationType));
        }
            
        return samlXACMLObligations;
    }

    /**
     * Convert a single XACML obligation into an OpenSAML ObligationType
     * @param xacmlObligation the XACML obligation
     * @return the OpenSAML ObligationType
     */
    private ObligationType buildObligationType(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationType xacmlObligation) {
        ObligationType samlXACMLObligation = obligationTypeBuilder.buildObject();

        samlXACMLObligation.setObligationId(xacmlObligation.getObligationId());

        for (oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType xacmlAttribute : xacmlObligation.getAttributeAssignment()) {
            AttributeAssignmentType obligationAttribute = attributeTypeBuilder.buildObject();
            obligationAttribute.setAttributeId(xacmlAttribute.getAttributeId());
            obligationAttribute.setDataType(xacmlAttribute.getDataType());
            obligationAttribute.setValue(getContentAsString(xacmlAttribute));

            samlXACMLObligation.getAttributeAssignments().add(obligationAttribute);
        }
        return samlXACMLObligation;
    }

    /**
     * Get the contents of a XACML attribute as a string. The contents is a list of values, but in our case we know that the
     * list contains no more than one object, and it's a String.
     * 
     * @param xacmlAttribute the XACML AttributeAssignmentType
     * @return a String representation of the first item in the content list
     * @note "null" will be returned if the list is null. "unknown" if the size of the list is 0
     */
    private String getContentAsString(oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType xacmlAttribute) {
        List<Object> content = xacmlAttribute.getContent();
        
        if (content == null) {
            return "null";
        }

        if (content.size() == 0) {
            return "unknown";
        }

        return content.get(0).toString();
    }

    /**
     * Build a Credential from a certificate in a keystore. This will be used to build a Signature to sign
     * the response. The id of the cert and the keystore is specified in the properties file.
     * 
     * @return Credential the Credential
     */
    private X509Credential buildCredential() throws EvaluationConnectorException {
        HashMap<String, String> passwordMap = new HashMap<String, String>();

        String keyEntryId = getString(SAML_RESPONSE_KEYSTORE_ENTRY_ID);

        if (keyEntryId == null) {
            throw new EvaluationConnectorException("Unable to find " + SAML_RESPONSE_KEYSTORE_ENTRY_ID + " in property file");
        }

        String keystorePassword = getString(SAML_RESPONSE_KEYSTORE_PASSWORD);

        if (keystorePassword == null) {
            throw new EvaluationConnectorException("Unable to find " + SAML_RESPONSE_KEYSTORE_PASSWORD + " in property file");
        }

        passwordMap.put(keyEntryId, keystorePassword);

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            
            String keystoreFileName = getString(SAML_RESPONSE_KEYSTORE_FILENAME);
            
            if (keystoreFileName == null) {
                throw new EvaluationConnectorException("Unable to find " + SAML_RESPONSE_KEYSTORE_FILENAME + " in property file");
            }
            
            FileInputStream inputStream = new FileInputStream(keystoreFileName);
            keystore.load(inputStream, keystorePassword.toCharArray());
            inputStream.close();
            
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            Criteria criteria = new EntityIDCriteria(keyEntryId);
            CriteriaSet criteriaSet = new CriteriaSet(criteria);

            Credential cred = resolver.resolveSingle(criteriaSet);

            if (cred instanceof X509Credential) {
                return (X509Credential)cred;
            } else {
                throw new EvaluationConnectorException("Unable to convert credential to X509 credential");
            }
        } catch (CertificateException ce) {
            throw new EvaluationConnectorException(ce);
        } catch (NoSuchAlgorithmException nsae) {
            throw new EvaluationConnectorException(nsae);
        } catch (SecurityException se) {
            throw new EvaluationConnectorException(se);
        } catch (KeyStoreException kse) {
            throw new EvaluationConnectorException(kse);
        } catch (FileNotFoundException fnfe) {
            throw new EvaluationConnectorException(fnfe);
        } catch (IOException ioe) {
            throw new EvaluationConnectorException(ioe);
        }
    }

    /**
     * The signature algorithm type must match that of the credential private key
     * or an exception will be thrown
     */
    private String getSignatureAlgorithm(Credential cred) throws EvaluationConnectorException {
        String sigAlg = keyTypeToSignatureType.get(cred.getPrivateKey().getAlgorithm());

        if (sigAlg == null) {
            throw new EvaluationConnectorException("No signature algorithm mapping for key type " + cred.getPrivateKey().getAlgorithm());
        }

        return sigAlg;
    }

    /**
     * Build KeyInfo from the X509 credentials
     * 
     * @param cred the credentials
     * @return a KeyInfo
     */
    private KeyInfo buildKeyInfo(X509Credential cred) throws CertificateEncodingException {
        X509Certificate x509Certificate = x509CertificateBuilder.buildObject();
        String certValue = new sun.misc.BASE64Encoder().encode(cred.getEntityCertificate().getEncoded());
        x509Certificate.setValue(certValue);
        
        X509Data x509Data = x509DataBuilder.buildObject();
        x509Data.getX509Certificates().add(x509Certificate);
        
        KeyInfo keyInfo = keyInfoBuilder.buildObject();
        keyInfo.getX509Datas().add(x509Data);

        return keyInfo;
    }
    
}
