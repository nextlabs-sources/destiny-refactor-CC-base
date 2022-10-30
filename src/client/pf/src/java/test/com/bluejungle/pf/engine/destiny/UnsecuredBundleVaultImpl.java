/*
 * Created on Dec 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/engine/destiny/UnsecuredBundleVaultImpl.java#1 $
 */

public class UnsecuredBundleVaultImpl implements IBundleVault {

    private static final String BUNDLE_FILE = "bundle.bin";

    /**
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#getBundleInfo()
     */
    public IBundleVault.BundleInfo getBundleInfo() throws BundleVaultException, InvalidBundleException {
        final IDeploymentBundle deploymentBundle;

        try {
            FileInputStream bundleFileInputStream = new FileInputStream(BUNDLE_FILE);
            GZIPInputStream bundleZipFileInputStream = new GZIPInputStream(bundleFileInputStream);
            ObjectInputStream bundleObjectInputStream = new ObjectInputStream(bundleZipFileInputStream);
            deploymentBundle = (IDeploymentBundle) bundleObjectInputStream.readObject();
            bundleObjectInputStream.close();
            return new BundleInfo() {
                public IDeploymentBundleV2 getBundle() {
                    if (deploymentBundle instanceof IDeploymentBundleV2) {
                        return (IDeploymentBundleV2)deploymentBundle;
                    } else {
                        return null;
                    }
                }
                public String[] getSubjects() {
                    return null;
                }
            };
        } catch (IOException exception) {
            throw new BundleVaultException("Failed to load test bundle", exception);
        } catch (ClassNotFoundException exception) {
            throw new BundleVaultException("Failed to load test bundle", exception);
        }
    }

    /**
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#validateAndStore(com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope)
     */
    public BundleInfo validateAndStore(DeploymentBundleSignatureEnvelope signedDeploymentByndle) throws InvalidBundleException, BundleVaultException {
        throw new UnsupportedOperationException("Storing isn't supported.");
    }

    /**
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#getDeploymentBundleFilename()
     */
    public String getDeploymentBundleFilename() {
        throw new UnsupportedOperationException("Not stored in file.");
    }

    
}
