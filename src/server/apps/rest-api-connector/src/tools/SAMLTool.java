import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.signature.XMLSignatureException;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AuthzDecisionStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.ctx.ActionType;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.EnvironmentType;
import org.opensaml.xacml.ctx.ResourceType; 
import org.opensaml.xacml.ctx.ResponseType; 
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResultType; 
import org.opensaml.xacml.ctx.SubjectType; 
import org.opensaml.xacml.ctx.impl.AttributeTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ActionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.DecisionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.EnvironmentTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResourceTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.RequestTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.SubjectTypeImplBuilder; 
import org.opensaml.xacml.ctx.impl.ResponseTypeImplBuilder; 
import org.opensaml.xacml.ctx.impl.ResultTypeImplBuilder; 
import org.opensaml.xacml.policy.AttributeAssignmentType;
import org.opensaml.xacml.policy.ObligationType;
import org.opensaml.xacml.policy.ObligationsType;
import org.opensaml.xacml.policy.impl.AttributeAssignmentTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationsTypeImplBuilder;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImpl;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImplBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SAMLTool {
    private static XMLObjectBuilderFactory builderFactory;
    
    private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    private static final String ISSUER = "http://www.nextlabs.com";
    private static final String USERNAME = "Alan Morgan";
    
    public static void main(String[] args) throws Exception {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ce) {
        }

        builderFactory = Configuration.getBuilderFactory();

        if (args.length == 0) {
            usage();
            return;
        }
        
        SAMLTool sp = new SAMLTool();

        if (args[0].equals("VALIDATE")) {
            if (args.length != 2) {
                usage();
                return;
            }
            sp.validateResponse(args[1]);
        } else if (args[0].equals("SIGN")) {
            if (args.length != 5) {
                usage();
                return;
            }
            System.out.println(readAndSignQuery(args[1], args[2], args[3], args[4]));
        } else {
            usage();
        }
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  java -classpath \"<xlib>/*;.\" SAMLTool VALIDATE <response.txt>");
        System.out.println("  java -classpath \"<xlib>/*;.\" SAMLTool SIGN <samlfile> <keystore> <cert name> <password>");
        System.out.println("\n\ne.g. java -classpath \"c:/p4/Destiny/xlib/jars/*;.\" SAMLTool VALIDATE result.txt");
        System.out.println("\n\nA barely functional tool for validating saml responses and");
        System.out.println("generating saml requests. If you want a different request you must");
        System.out.println("change this file.");
        System.out.println("\nTODO: Make this tool suck marginally less");
    }
    
    public static void validateResponse(String filename) throws Exception {
        System.out.println("Validating response file " + filename);
        validateResponse(new FileInputStream(filename));
    }

    public static void validateResponse(InputStream istream) throws Exception {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
        
        Document doc = ppMgr.parse(istream);
        Element root = doc.getDocumentElement();
        
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(root);
        
        Response response = (Response) unmarshaller.unmarshall(root);

        validateSignatureForm(response.getSignature());
        validateSignature(response.getSignature());
    }
    
    // Checks to see if the signature is a properly formatted and structured XML signature. Doesn't actually *validate* that
    // it signs the object in question
    private static void validateSignatureForm(Signature sig) throws ValidationException {
        SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
        validator.validate(sig);
    }

    // Validates the signature
    private static void validateSignature(Signature sig) throws CertificateException, ValidationException {
        KeyInfo keyInfo = sig.getKeyInfo();
        java.security.cert.X509Certificate cert = KeyInfoHelper.getCertificates(keyInfo).get(0);

        BasicX509Credential cred = new BasicX509Credential();
        cred.setEntityCertificate(cert);
        SignatureValidator signatureValidator = new SignatureValidator(cred);
        signatureValidator.validate(sig);
    }
    

    private static String readAndSignQuery(String filename, String keystore, String keyEntryID, String password) throws Exception {
        XACMLAuthzDecisionQueryType query = readQuery(new FileInputStream(filename));
        return signRequest(query, keystore, keyEntryID, password);
    }
    
    private static XACMLAuthzDecisionQueryType readQuery(InputStream istream) throws Exception {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
        
        Document doc = ppMgr.parse(istream);
        Element root = doc.getDocumentElement();
        
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(root);

        return (XACMLAuthzDecisionQueryType) unmarshaller.unmarshall(root);
    }

    private static String signRequest(XACMLAuthzDecisionQueryType query, String keystore, String keyEntryID, String password) throws Exception {
        SignatureBuilder signatureBuilder = (SignatureBuilder)builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        Signature sig = signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);
            
        X509Credential cred = readCredential(keystore, keyEntryID, password);
        sig.setSigningCredential(cred);
        sig.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_DSA);
        sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        sig.setKeyInfo(generateKeyInfo(cred));
        query.setSignature(sig);
            
        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(query);
            
        if (marshaller == null) {
            System.out.println("Can't find a marshaller");
        }

        Element queryElement = marshaller.marshall(query);
            
        Signer.signObject(sig);

        return XMLHelper.nodeToString(queryElement);
    }

    private static X509Credential readCredential(String keystoreFileName, String keyEntryID, String password) throws KeyStoreException, FileNotFoundException, IOException, SecurityException, NoSuchAlgorithmException, CertificateException {
        HashMap<String, String> passwordMap = new HashMap<String, String>();
        passwordMap.put(keyEntryID, password);

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream inputStream = new FileInputStream(keystoreFileName);
        keystore.load(inputStream, password.toCharArray());
        inputStream.close();
        
        KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

        Criteria criteria = new EntityIDCriteria(keyEntryID);
        CriteriaSet criteriaSet = new CriteriaSet(criteria);
        
        X509Credential cred= (X509Credential)resolver.resolveSingle(criteriaSet);

        if (!cred.getPrivateKey().getAlgorithm().equals("DSA")) {
            throw new NoSuchAlgorithmException("The rest of the code assumes DSA encryption for the keystore");
        }
        return cred;
    }

    private static KeyInfo generateKeyInfo(X509Credential cred) throws CertificateEncodingException {
        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        
        KeyInfoBuilder keyInfoBuilder = (KeyInfoBuilder) builderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
        KeyInfo keyInfo = keyInfoBuilder.buildObject();

        X509DataBuilder x509DataBuilder = (X509DataBuilder) builderFactory.getBuilder(X509Data.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = x509DataBuilder.buildObject();

        X509CertificateBuilder x509CertificateBuilder = (X509CertificateBuilder) builderFactory.getBuilder(X509Certificate.DEFAULT_ELEMENT_NAME);
        X509Certificate x509Certificate = x509CertificateBuilder.buildObject();
        
        String certValue = new sun.misc.BASE64Encoder().encode(cred.getEntityCertificate().getEncoded());
        x509Certificate.setValue(certValue);
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);

        return keyInfo;
    }
}

