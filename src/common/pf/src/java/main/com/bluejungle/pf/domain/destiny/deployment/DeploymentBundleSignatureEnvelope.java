/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.Arrays;

/**
 * This class is a wrapper around an {@link IDeploymentBundleV2}. Its purpose is
 * to digitaly sign the bundle so that clients can ensure it was produced by the
 * server
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_1.5/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/DeploymentBundleSignatureEnvelope.java#1 $
 */

public class DeploymentBundleSignatureEnvelope implements Serializable {

    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(DeploymentBundleSignatureEnvelope.class.getName());
    private final SignedObject wrappedSignedObject;
    private final String[] subjects;

    /**
     * Create an instance of DeploymentBundleSignatureEnvelope
     * 
     * @param bundle
     * @param subjects an array of subjects for which the bundle has been requested.
     * @param privateKey
     * @throws InvalidKeyException
     *             if the provided key is invalid
     * @throws IOException
     *             if a failure occurs reading the provided bundle
     * @throws SignatureException
     *             if the signature process fails
     */
    public DeploymentBundleSignatureEnvelope(
        IDeploymentBundle bundle
    ,   String[] subjects
    ,   PrivateKey privateKey)
        throws InvalidKeyException, IOException, SignatureException {
        if (bundle == null) {
            throw new NullPointerException("bundle cannot be null.");
        }

        if (privateKey == null) {
            throw new NullPointerException("privateKey cannot be null.");
        }

        try {
            Signature signatureEngine = Signature.getInstance(getSignatureAlgorithm(privateKey));
            wrappedSignedObject = new SignedObject(bundle, privateKey, signatureEngine);
        } catch (NoSuchAlgorithmException exception) {
            throw new SignatureException("Signature algorithm not supported.");
        }
        if (subjects != null) {
            this.subjects = new String[subjects.length];
            for ( int i = 0 ; i != subjects.length ; i++ ) {
                this.subjects[i] = subjects[i].toUpperCase();
            }
            Arrays.sort(this.subjects);
        } else {
            this.subjects = null;
        }
    }

    /**
     * Verify and retrieve the deployment bundle
     * 
     * @param publicKey
     * @return the validated deployment bundle
     * @throws InvalidKeyException
     *             if the provided key is invalid
     * @throws SignatureException
     *             if the signature verification fails
     * @throws InvalidBundleException
     *             if the bundle was invalid
     * @throws IOException
     *             if a failure occurs reading the deployment bundle object
     */
    public IDeploymentBundleV2 getDeploymentBundle(PublicKey publicKey) throws InvalidKeyException, SignatureException, InvalidBundleException, IOException {
        if (publicKey == null) {
            throw new NullPointerException("publicKey cannot be null.");
        }

        IDeploymentBundleV2 bundleToReturn = null;
        try {
            Signature signatureEngine = Signature.getInstance(getSignatureAlgorithm(publicKey));
            if (!wrappedSignedObject.verify(publicKey, signatureEngine)) {
                throw new InvalidBundleException("Bundle not authentic.");
            }

            bundleToReturn = (IDeploymentBundleV2) wrappedSignedObject.getObject();
        } catch (NoSuchAlgorithmException exception) {
            throw new SignatureException("Signature algorithm not supported.");
        } catch (ClassNotFoundException exception) {
            throw new InvalidBundleException("Failed to find bundle class", exception);
        }

        return bundleToReturn;
    }

    /**
     * Returns the subjects associated with this envelope.
     *
     * @return an array of subject UIDs for which this bundle has been requested, or null.
     * The UIDs are sorted alphabetically.
     */
    public String[] getSubjects() {
        return subjects;
    }

    /**
     * Returns a signature algorithm appropriate for the key. There
     * doesn't appear to be an automatic way to do this. For DSA keys
     * we can just specify DSA, and the runtime will automatically
     * choose SHA1withDSA. If the key is RSA, however, we need to be
     * specific about which algorithm we want.
     */
    private String getSignatureAlgorithm(Key key) throws NoSuchAlgorithmException {
        if (key.getAlgorithm().equals("RSA")) {
            return "SHA1withRSA";
        } else if (key.getAlgorithm().equals("DSA")) {
            return "DSA";
        }

        // Should we try to return key.getAlgorithm() here or throw an exception?
        throw new NoSuchAlgorithmException("Can't pick signature algorithm for key " + key.getAlgorithm());
    }
}
