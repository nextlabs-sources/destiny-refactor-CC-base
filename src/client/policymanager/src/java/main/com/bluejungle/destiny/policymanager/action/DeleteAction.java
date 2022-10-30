/*
 * Created on Jun 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author dstarke
 * 
 */
public class DeleteAction extends BaseDisableableAction {

    private static final Set<EntityType> POLICY_FOLDER_CONTAINED_ENTITY_TYPES = new HashSet<EntityType>();
    static {
        POLICY_FOLDER_CONTAINED_ENTITY_TYPES.add(EntityType.FOLDER);
        POLICY_FOLDER_CONTAINED_ENTITY_TYPES.add(EntityType.POLICY);
    }

    /**
     * 
     */
    public DeleteAction() {
        super();
    }

    /**
     * @param text
     */
    public DeleteAction(String text) {
        super(text);
    }

    /**
     * @param text
     * @param image
     */
    public DeleteAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * @param text
     * @param style
     */
    public DeleteAction(String text, int style) {
        super(text, style);
    }

    public void run() {
        Set<String> componentDeleted = new HashSet<String>();
        Set<IHasId> entitiesToUpdate = new HashSet<IHasId>();

        Set<DomainObjectDescriptor> selectedItems = getSelectedItems();

        GlobalState gs = GlobalState.getInstance();
        for (DomainObjectDescriptor nextDescriptor : selectedItems) {
            EntityType entityType = nextDescriptor.getType();

            String name = nextDescriptor.getName();
            // if object has not been deployed, or is permanently inactive, just
            // delete it
            PolicyServerProxy.ObjectVersion version = PolicyServerProxy.getLastScheduledVersion(nextDescriptor);
            // also make sure there are no objects referring to this one
            Collection<DomainObjectDescriptor> c = PolicyServerProxy.getAllReferringObjects(name);

            if (((c == null) || (c.isEmpty())) && (version == null || version.activeTo.before(new Date())) && ((!entityType.equals(EntityType.FOLDER)) || (isPolicyFolderEmpty(nextDescriptor)))) {
                IHasId domainObject = (IHasId) PolicyServerProxy.getEntityForDescriptor(nextDescriptor);
                DomainObjectHelper.setStatus(domainObject, DevelopmentStatus.DELETED);
                entitiesToUpdate.add(domainObject);
                gs.closeEditorFor(domainObject);
                int pos = name.indexOf(PQLParser.SEPARATOR);
                if (pos != -1 && entityType != EntityType.POLICY && entityType != EntityType.FOLDER) {
                    componentDeleted.add(name.substring(0, pos).toLowerCase());
                }
            }
        }

        PolicyServerProxy.saveEntities(entitiesToUpdate);

        // Update lists for deleted entity types
        for (String componentTypeName : componentDeleted) {
            EntityInfoProvider.updateComponentList(ComponentEnum.forName(componentTypeName));
        }
        EntityInfoProvider.updatePolicyTree();
    }

    /**
     * @param nextDescriptor
     * @return
     */
    private boolean isPolicyFolderEmpty(DomainObjectDescriptor nextDescriptor) {
        String folderName = nextDescriptor.getName();
        Collection entitiesInFolder = PolicyServerProxy.getEntityList(PolicyServerProxy.escape(folderName) + PQLParser.SEPARATOR + "%", POLICY_FOLDER_CONTAINED_ENTITY_TYPES);

        return ((entitiesInFolder == null) || (entitiesInFolder.isEmpty()));
    }

    /**
     * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState(java.util.Set)
     */
    protected void refreshEnabledState(Set selectedItems) {
        boolean newState = !selectedItems.isEmpty();

        Iterator selectedItemsIterator = selectedItems.iterator();
        while ((selectedItemsIterator.hasNext()) && (newState)) {
            PolicyOrComponentData nextSelectedItem = (PolicyOrComponentData) selectedItemsIterator.next();
            DomainObjectDescriptor domainObjectDescriptor = nextSelectedItem.getDescriptor();

            if (domainObjectDescriptor.getType().equals(EntityType.FOLDER)) {
                newState &= isPolicyFolderEmpty(domainObjectDescriptor);
            } else {
                IHasId domainObject = nextSelectedItem.getEntity();
                newState &= PolicyServerProxy.canPerformAction(domainObject, DAction.DELETE);

                DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);
                newState &= (status == DevelopmentStatus.OBSOLETE || status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT);

                try {
                    DomainObjectUsage domainObjectUsage = nextSelectedItem.getEntityUsage();
                    newState &= (!domainObjectUsage.hasReferringObjects());
                    newState &= (domainObjectUsage.getCurrentlydeployedvcersion() == null);
                    newState &= (!domainObjectUsage.hasFuturedeployments());
                } catch (PolicyEditorException exception) {
                    LoggingUtil.logWarning(Activator.ID, "Failed to load domain object usage.  Delete menu item may be improperly disabled.", exception);
                    newState = false;
                }
            }
        }

        setEnabled(newState);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
    }

    @Override
    public ImageDescriptor getDisabledImageDescriptor() {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED);
    }
}
