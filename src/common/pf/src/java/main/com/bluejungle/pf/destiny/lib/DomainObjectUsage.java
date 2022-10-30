package com.bluejungle.pf.destiny.lib;

/**
 * Instances of this class describe the usage of an object
 * in the system, i.e. its deployment status, expected deployment
 * version, referencing objects, etc.
 */
public class DomainObjectUsage {
    private final Long currentlyDeployedVersion;
    private final Long latestDeployedVersion;
    private final boolean hasReferringObjects;
    private final boolean hasBeenDeployed;
    private final boolean hasFutureDeployments;
    /**
     * Constructs the usage object with the specified parameters.
     * 
     * @param hasReferringObjects indicates that the object has referring objects.
     * @param hasBeendeployed indicates that the object has been deployed or scheduled for deployment.
     * @param currentlyDeployedVersion the version deployed at the time of request.
     * @param latestDeployedVersion the version deployed at the END_OF_TIME.
     */
    public DomainObjectUsage(
        boolean hasReferringObjects
    ,   boolean hasBeenDeployed
    ,   boolean hasFutureDeployments
    ,   Long currentlyDeployedVersion
    ,   Long latestDeployedVersion ) {
        this.currentlyDeployedVersion = currentlyDeployedVersion;
        this.latestDeployedVersion = latestDeployedVersion;
        this.hasReferringObjects = hasReferringObjects;
        this.hasBeenDeployed = hasBeenDeployed;
        this.hasFutureDeployments = hasFutureDeployments;
    }

    public boolean hasBeenDeployed() {
        return hasBeenDeployed;
    }

    @Deprecated
    public boolean hasFuturedeployments() {
        return hasFutureDeployments;
    }

    public boolean hasFutureDeployments() {
        return hasFutureDeployments;
    }

    public boolean hasReferringObjects() {
        return hasReferringObjects;
    }

    @Deprecated
    public Long getCurrentlydeployedvcersion() {
        return currentlyDeployedVersion;
    }

    public Long getCurrentlyDeployedVersion() {
        return currentlyDeployedVersion;
    }

    public Long getLatestDeployedVersion() {
        return latestDeployedVersion;
    }

}
