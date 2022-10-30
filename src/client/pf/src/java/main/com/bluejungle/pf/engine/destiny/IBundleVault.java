package com.bluejungle.pf.engine.destiny;

import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;

/**
 * IBundleVault is used on the client to maintain the policy bundle
 * 
 * @author sgoldstein
 */
public interface IBundleVault {

    public static final String COMPONENT_NAME = "BundleVault";

    /**
     * This interface defines a structure for getting the bundle
     * and its supporting information.
     */
    interface BundleInfo {
        /**
         * Obtain the deployment bundle.
         * 
         * @return the deployment bundle.
         */
        IDeploymentBundleV2 getBundle();
        /**
         * Obtain the subjects for which this bundle has been built.
         *
         * @return the subjects for which this bundle has been built.
         */
        String[] getSubjects();
    }

    /**
     * Validate the signed bundle and store it in the vault
     * 
     * @param signedDeploymentByndle
     *            a signed deployment bundle
     * @return the unsigned policy bundle
     * @throws InvalidBundleException
     *             if the bundle is invalid
     * @throws BundleVaultException
     *             if an unforseen error happens while validating and storing
     *             the bundle
     */
    public BundleInfo validateAndStore(DeploymentBundleSignatureEnvelope signedDeploymentByndle) throws InvalidBundleException, BundleVaultException;

    /**
     * Retrieve the currently stored policy bundle information.
     * 
     * @return the currently stored policy bundle or an empty bundle if a bundle
     *         is not currently stored
     * @throws InvalidBundleException
     *             if the bundle is invalid
     * @throws BundleVaultException
     *             if an unforseen error happens while validating and reading
     *             the bundle
     */
    public BundleInfo getBundleInfo() throws BundleVaultException, InvalidBundleException;

    /**
     * Retrieve the deployemnt bundle file name.
     * 
     * @return the deployemnt bundle file name
     * 
     * I was relucatant to put this in, as the vault could store the bundle any
     * way it chooses to. However, for the moment, it's needed to track the
     * "Policy Bundle Accesses" document activity
     */
    public String getDeploymentBundleFilename();
}
