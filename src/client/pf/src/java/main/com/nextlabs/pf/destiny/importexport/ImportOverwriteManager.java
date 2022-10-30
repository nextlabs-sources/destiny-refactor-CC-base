/*
 * Created on Oct 16, 2013 All sources, binaries and HTML pages (C) copyright
 * 2004 by Nextlabs.Inc., San Mateo, CA, Ownership remains with Nextlabs.Inc, 
 * All rights reserved worldwide.
 */
package com.nextlabs.pf.destiny.importexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.epicenter.action.IAction;

/**
 * This class is using to put all the generic checking method for import policy
 * @author ichiang
 *
 */
public class ImportOverwriteManager {
    private static final IPolicyEditorClient client = ComponentManagerFactory.getComponentManager().getComponent(PolicyEditorClient.COMP_INFO);
    private List <DomainObjectDescriptor> topPolicies = new ArrayList<DomainObjectDescriptor>();
    private List <DomainObjectDescriptor> subPolicies = new ArrayList<DomainObjectDescriptor>();   
    private static final String CURRENT = "Current";
    private static final String PRIOR  = "Prior";
    private static final String NONE  = "None";
    private static final Log LOG = LogFactory.getLog(ImportOverwriteManager.class);
    
    /** This method is designed for "import-overwrite". 
     *  When user choose to do import- overwrite, we'll need to go through each of the import policy,
     *  group them into topPolicies and subPolicies, and compare them with the original policies later.
     * @param policyName
     */
    public void groupingPolicies (String policyName) throws ImportException {
        try {
            Collection<? extends IHasId> col =
                client.getEntitiesForNamesAndType(Collections.singletonList(policyName)
                                                  , EntityType.POLICY, false);
            if (!col.isEmpty()) {
                Policy policy = (Policy)col.iterator().next();
                if (!policy.hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE)){
                    topPolicies.addAll(getDod(policyName, EntityType.POLICY.getName()));
                }
                subPolicies.addAll(getDod(policyName, EntityType.POLICY.getName()));
            }
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }
    }
    
    /*
     *  In 7.0, we have the subpolicy in the system. Subpolicy only exists under a top policy, 
     *  and they cannot be reused, so when we do the import overwrite, we might replace/ delete the
     *  original structure, that's why we need to group all the import policies, and compare them with
     *  the original policies in the database. If the import file tends to delete the original policy,
     *  we will need to check if the original policy is in a deletable state and if the import user
     *  has the "delete" access for that policy.
     * 
     *  This method is designed to determines if any of the policy needs to be deleted and if that policy is in
     *  a deletable state. 
     *  The "deleteUnreferencedSubPolicies()" uses the "topPolicies" and "subPolicies" lists that were created by 
     *  "groupingPolices()" above, and compare them with the original policies tree structure in the 
     *  current system. 
     */   
    public void deleteUnreferencedSubPolicies(IImportState importState) throws ImportException{
        List <DomainObjectDescriptor> originalSubpolicyList = (List<DomainObjectDescriptor>) getSubpoliciesDod(topPolicies);
        for(DomainObjectDescriptor dod: subPolicies){
            if(originalSubpolicyList.contains(dod)){
                originalSubpolicyList.remove(dod);
            }
        }
        // originalSubpolicyList now contains sub-policies that currently exist, but are not in the import data. These must be removed. 
        if(!originalSubpolicyList.isEmpty()){
            boolean canBeDeleted = true;
            for(DomainObjectDescriptor dod: originalSubpolicyList){
                canBeDeleted = policyCanBeDeleted(dod)&& policyInDeletableState(dod);
            }
            if(!canBeDeleted){
                throw new ImportException("Import Overwrite cannot be completed. Unable to delete Unreferenced Policy" );
            }
        }
        try {
            Collection<? extends IHasId> shouldBeDeletedEntities = client.getEntitiesForDescriptors(originalSubpolicyList);
            for (IHasId entity: shouldBeDeletedEntities){
                ((Policy) entity).setStatus(DevelopmentStatus.DELETED);
            }
            importState.setShouldBeDeletedPolicies((Collection<IHasId>) shouldBeDeletedEntities);
        } catch (PolicyEditorException e) {
            importState.setShouldBeDeletedPolicies(new ArrayList<IHasId>());
        }
    }
    
    private static Collection<DomainObjectDescriptor> getDod(String name, String entityType) throws ImportException {
        List <DomainObjectDescriptor> dod = new ArrayList <DomainObjectDescriptor>();

        try {
            dod.addAll(client.getDescriptorsForNameAndType(name, EntityType.forName(entityType), true));  
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }
        return dod;
    }
    
    private static Collection<DomainObjectDescriptor> getSubpoliciesDod (Collection<DomainObjectDescriptor> dod) throws ImportException {
        List<DomainObjectDescriptor> allDependencies = new ArrayList<DomainObjectDescriptor>();
        List<DomainObjectDescriptor>  subpoliciesDod = new ArrayList<DomainObjectDescriptor>();
        try {
            allDependencies.addAll(client.getDependencies(dod));
            for(DomainObjectDescriptor dependency : allDependencies) {
                if(dependency.getType().equals(EntityType.POLICY)){
                    subpoliciesDod.add(dependency);
                }
            }
        } catch (CircularReferenceException e) {
            throw new ImportException(e);
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }
        return subpoliciesDod; 
    }
    
    public boolean objectCanBeModified(String name, String entityType) throws ImportException{
        List <DomainObjectDescriptor> allDod = (List<DomainObjectDescriptor>) getDod(name, entityType);

        if(allDod.isEmpty()){
            return true;
        }else{
            try {
                Collection<? extends IAction> actions = client.allowedActions(allDod);
                return (actions != null && actions.contains(DAction.WRITE) && actions.contains(DAction.READ));
            } catch (PolicyEditorException e) {
                throw new ImportException(e);
            }
        }
    }
    
    private static boolean policyCanBeDeleted (DomainObjectDescriptor dod) throws ImportException {
        List <DomainObjectDescriptor> dodList = new ArrayList<DomainObjectDescriptor>();
        dodList.add(dod);
        try {
            Collection<? extends IAction> actions = client.allowedActions(dodList);
            return (actions != null && actions.contains(DAction.DELETE));
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }
    }
    
    /*
     * This method should be rewrite.
     * When we detect the deployment status"inactive" of the DomainObjectDescriptor, we should come out with a better way
     * to handle it. What we are doing now is, we compare the pending status and the current status to see if they are 
     * "inactive". Please see DomainObjectHelper.java class, getStatusText() and getDeploymentStatusKey() for more infomation.
     */
    private static boolean policyInDeletableState(DomainObjectDescriptor dod) throws ImportException {
        List<DomainObjectDescriptor> dodList = new ArrayList<DomainObjectDescriptor>();
        dodList.add(dod);
        List<DomainObjectUsage> usage = new ArrayList<DomainObjectUsage>();
        DomainObjectUsage latestUsage;
        try{
            usage = client.getUsageList(Collections.singletonList(dod));
            latestUsage = usage.get(0);
            if (latestUsage.getCurrentlydeployedvcersion() != null ||
                latestUsage.hasFuturedeployments()) {
                return false;
            }
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }

        if(!(dod.getStatus().equals(DevelopmentStatus.NEW)||
             dod.getStatus().equals(DevelopmentStatus.EMPTY)||
             dod.getStatus().equals(DevelopmentStatus.DRAFT)||
             dod.getStatus().equals(DevelopmentStatus.OBSOLETE))){
            return false;
        }
        if (dod.getStatus().equals(DevelopmentStatus.OBSOLETE)){
            Long currentlyDeployedVersion = latestUsage.getCurrentlydeployedvcersion();
            Long pendingDeploymentVersion = latestUsage.getLatestDeployedVersion();
            if ((currentlyDeployedVersion != null)
                && (pendingDeploymentVersion != null)
                && (currentlyDeployedVersion.equals(pendingDeploymentVersion))) {
                pendingDeploymentVersion = null;
            }
            int descriptorVersion = dod.getVersion();
            String pending = getDeployedVersionStatus(descriptorVersion,pendingDeploymentVersion);
            String now = getDeployedVersionStatus(descriptorVersion,currentlyDeployedVersion);

            return ((pending.equalsIgnoreCase(NONE) && now.equalsIgnoreCase(NONE)) || now.equalsIgnoreCase(CURRENT));
        }
        return true;
    }
    
    private static String getDeployedVersionStatus(int currentDescriptorVersion, Long deployedVersion) {
        if (deployedVersion != null) {
            if (currentDescriptorVersion <= deployedVersion.intValue()) {
                return CURRENT;
            } else {
                return PRIOR;
            }
        } else {
            return NONE;
        }
    }
}
