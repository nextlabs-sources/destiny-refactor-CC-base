/*
 * Created on Jun 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.domain.destiny.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLHelper;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/domain/destiny/common/ServerSpecManager.java#1 $:
 */

public class ServerSpecManager extends DefaultSpecManager implements IDSpecManager, IManagerEnabled, IInitializable {

    public static final ComponentInfo<ServerSpecManager> COMP_INFO = 
        new ComponentInfo<ServerSpecManager>(
                IDSpecManager.COMP_NAME, 
                ServerSpecManager.class, 
                IDSpecManager.class, 
                LifestyleType.SINGLETON_TYPE);

    protected IComponentManager cm;
    protected LifecycleManager lm;

    /**
     * @see IDSpecManager#resolveSpec(String, SpecType)
     */
    public IDSpec resolveSpec(Long id) {
        Collection<Long> ids = new HashSet<Long>();
        ids.add(id);
        DevelopmentEntity dev = null;
        try {
            dev = lm.getEntitiesForIDs(ids).toArray(new DevelopmentEntity[1])[0];
        } catch (EntityManagementException eme) {
            throw new RuntimeException("Could not find entity for id " + id);
        }
        DomainObjectBuilder dob = new DomainObjectBuilder(dev.getPql());
        IDSpec spec = null;
        try {
            spec = dob.processSpec();
        } catch (PQLException e) {
            throw new RuntimeException("Could not parse definition for id " + id);
        }

        return spec;
    }

    /**
     * @see IDSpecManager#resolveSpec(String, SpecType)
     */
    public IDSpec resolveSpec(String specName) {
        try {
            DevelopmentEntity de = lm.getEntityForName(EntityType.COMPONENT, specName, LifecycleManager.MUST_EXIST);
            DomainObjectBuilder dob = new DomainObjectBuilder( de.getPql() );
            return dob.processSpec();
        } catch (EntityManagementException e) {
            throw new RuntimeException(e);
        } catch (PQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given a descriptor, returns a <code>Map</code> from ID of an object to its definition. The transitive closure of the dependencies of the given descriptor is included.
     * 
     * @param descr the descriptor for which to run the operation.
     * @return a <code>Map</code> from ID of an object to its definition.
     * @throws CircularReferenceException
     * @throws PQLException
     * @throws EntityManagementException
     * @throws PolicyServiceException
     */
    public Map<Long,IHasId> getDependentsMapById(DomainObjectDescriptor descr) throws EntityManagementException, PQLException, CircularReferenceException {
        assert descr != null;
        // Resolve all the dependencies of this descriptor
        Collection<DomainObjectDescriptor> deps = getDependencies(Arrays.asList(new DomainObjectDescriptor[] { descr }), LifecycleManager.DIRECT_CONVERTER );
        // Get all related objects
        Set<Long> ids = new HashSet<Long>(DomainObjectDescriptor.extractIds(deps));
        ids.add(descr.getId());
        Collection<IHasId> all;
        all = DTOUtils.makeCollectionOfSpecs(DTOUtils.mergeStrings(PQLHelper.extractPQL(lm.getEntitiesForIDs(ids))));

        // Build a map of IDs to specs
        final Map<Long,IHasId> byId = new HashMap<Long,IHasId>();
        for (IHasId entity : all ) {
            byId.put(entity.getId(), entity);
        }
        return byId;
    }

    public Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> digests, LifecycleManager.DigestMaker digestMaker) throws EntityManagementException, PQLException, CircularReferenceException {
        return getDependenciesDigest(digests, false, digestMaker);
    }

    /**
     * @see com.bluejungle.pf.destiny.lib.IPolicyEditorService#getDependencies(Collection)
     *
     * There is a lot of shared code between this function and the next.  TODO - factor out similarities
     */
    public Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> digests, boolean directOnly, LifecycleManager.DigestMaker digestMaker) throws EntityManagementException, PQLException, CircularReferenceException {
        assert digests != null;
        // Find the types of potential references and IDs of the initial BFS set.
        Set<String> referringTypes = new HashSet<String>();
        List<Long> initial = new LinkedList<Long>();
        List<Long> policyIDs = new LinkedList<Long>();
        for (DODDigest digest : digests) {
            initial.add(digest.getId());
            String digestType = digest.getType();

            if (digestType.equals(EntityType.POLICY.getName())) {
                policyIDs.add(digest.getId());
            } else {
                referringTypes.add(digestType);
                referringTypes.add(EntityType.COMPONENT.getName());
            }
        }
        
        if ( !policyIDs.isEmpty() ) {
            // Policies can reference anything
            referringTypes.add(EntityType.COMPONENT.getName());
            referringTypes.add(EntityType.LOCATION.getName());
            referringTypes.add(EntityType.POLICY.getName());
        }
        
        Collection<DevelopmentEntity> candidates = lm.getAllEntitiesOfType(lm.convertStringsToEntityTypes(referringTypes));
        candidates.addAll(lm.getEntitiesForIDs(policyIDs));

        // Run the parameterized BFS algorithm
        Collection<Long> resIds = lm.discoverBFS(initial, candidates, LifecycleManager.DIRECT_DEPENDENCY_BUILDER, LifecycleManager.DEVELOPMENT_ID_EXTRACTOR, directOnly);
        // Make digests for the IDs found by the BFS.
        return lm.getEntityDigestsForIDs(resIds,digestMaker);
    }

    public Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> descriptors, LifecycleManager.DescriptorMaker descriptorMaker ) throws EntityManagementException, PQLException, CircularReferenceException {
        return getDependencies(descriptors, false, descriptorMaker);
    }

    /**
     * @see com.bluejungle.pf.destiny.lib.IPolicyEditorService#getDependencies(Collection)
     */
    public Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> descriptors, boolean directOnly, LifecycleManager.DescriptorMaker descriptorMaker ) throws EntityManagementException, PQLException, CircularReferenceException {
        assert descriptors != null;
        // Find the types of potential references and IDs of the initial BFS set.
        Set<EntityType> referringTypes = new HashSet<EntityType>();
        List<Long> initial = new LinkedList<Long>();
        List<Long> policyIDs = new LinkedList<Long>();
        for ( Iterator<DomainObjectDescriptor> iter = descriptors.iterator() ; iter.hasNext() ; ) {
            DomainObjectDescriptor dod = iter.next();
            initial.add( dod.getId() );
            EntityType dodType = dod.getType(); 
            if (dodType == EntityType.POLICY) {
                policyIDs.add(dod.getId());
            } else {
                referringTypes.add(dodType);
                referringTypes.add(EntityType.COMPONENT);
            }
        }                

        if ( !policyIDs.isEmpty() ) {
            // Policies can reference anything
            referringTypes.add(EntityType.COMPONENT);
            referringTypes.add(EntityType.LOCATION );
            referringTypes.add(EntityType.POLICY);
        }
        Collection<DevelopmentEntity> candidates = lm.getAllEntitiesOfType(referringTypes);
        candidates.addAll(lm.getEntitiesForIDs(policyIDs));
        // Run the parameterized BFS algorithm
        Collection<Long> resIds = lm.discoverBFS(initial, candidates, LifecycleManager.DIRECT_DEPENDENCY_BUILDER, LifecycleManager.DEVELOPMENT_ID_EXTRACTOR, directOnly);
        // Make descriptors for the IDs found by the BFS.
        return lm.getEntityDescriptorsForIDs(resIds,descriptorMaker);
    }

    /**
     * @see IManagerEnabled#setManager(IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.cm = manager;
    }

    /**
     * @see IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return cm;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        lm = (LifecycleManager) cm.getComponent(LifecycleManager.COMP_INFO);
    }

}
