/*
 * Created on Jun 13, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author dstarke
 * 
 */
public class DeactivateAction extends BaseDisableableAction {

    private static final IEventManager EVENT_MANAGER;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        EVENT_MANAGER = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
    }

    /**
     * @param text
     */
    public DeactivateAction(String text) {
        super(text);
    }

    /**
     * @param text
     * @param style
     */
    public DeactivateAction(String text, int style) {
        super(text, style);
    }

    /**
     * @param text
     * @param image
     */
    public DeactivateAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        IHasId domainObject = (IHasId) gs.getCurrentObject();

        String name = DomainObjectHelper.getName(domainObject);
        EntityType entityType = DomainObjectHelper.getEntityType(domainObject);

        Collection<DomainObjectDescriptor> c = PolicyServerProxy.getAllReferringObjects(name);
        if (c.isEmpty() || removeReferences(name, entityType, c)) {
            DomainObjectHelper.setStatus(domainObject, DevelopmentStatus.OBSOLETE);
            PolicyServerProxy.saveEntity(domainObject);

            PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(gs.getCurrentObject());
            EVENT_MANAGER.fireEvent(objectModifiedEvent);
        }
    }

    /**
     * Asks the user if they would like to remove all references to an object.
     * If the user so elects, actually performs the removal.
     * 
     * @return whether all the dependencies have been successfully removed
     */
    private boolean removeReferences(String name, EntityType entityType, Collection<DomainObjectDescriptor> referringObjects) {
        // TODO: replace with a better dialog later
        boolean doRemove = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Remove references from other objects?", "The object \"" + name + "\" is currently used in other policies or components.  Deactivating \"" + name
                + "\" will automatically remove all references to this object.");

        if (doRemove) {
            Collection<? extends IHasId> c = PolicyServerProxy.getEntitiesForDescriptor(referringObjects);
            Iterator iter = c.iterator();
            while (iter.hasNext()) {
                PredicateHelpers.removeReferences((IHasId) iter.next(), name, entityType);
            }
            PolicyServerProxy.saveEntities(c);

            Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
            Iterator changedEntities = c.iterator();
            while (changedEntities.hasNext()) {
                PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent((IHasId) changedEntities.next());
                eventsToFire.add(objectModifiedEvent);
            }
            EVENT_MANAGER.fireEvent(eventsToFire);
        }
        return doRemove;
    }

    public void refreshEnabledState(Set selectedItems) {
        boolean newState = false;
        if (selectedItems.size() == 1) {
            IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems.iterator().next();
            IHasId entity = selectedItem.getEntity();

            // can only deactivate draft or approved objects
            DevelopmentStatus status = DomainObjectHelper.getStatus(entity);
            newState = (status == DevelopmentStatus.DRAFT || status == DevelopmentStatus.APPROVED);
            newState &= PolicyServerProxy.canPerformAction(entity, DAction.APPROVE);

            String name = DomainObjectHelper.getName(entity);
            Collection referringObjects = PolicyServerProxy.getAllReferringObjects(name);
            Iterator referringObjectsIterator = referringObjects.iterator();
            while ((referringObjectsIterator.hasNext()) && (newState)) {
                DomainObjectDescriptor nextReferringObject = (DomainObjectDescriptor) referringObjectsIterator.next();
                newState &= !nextReferringObject.isHidden();
            }

            try {
                DomainObjectUsage entityUsage = selectedItem.getEntityUsage();
                newState &= entityUsage.hasBeenDeployed();
            } catch (PolicyEditorException exception) {
                LoggingUtil.logWarning(Activator.ID, "Failed to load deployment history for selected domain object.  Deactivate menu item may be improperly disabled.", exception);
            }
        }
        setEnabled(newState);
    }
}
