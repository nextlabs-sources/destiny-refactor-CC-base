/*
 * Created on Sep 29, 2004
 * 
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.internal.win32.INITCOMMONCONTROLSEX;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.SelectedItemsModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.dialogs.LoginDialog;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * 
 */
public class RcpApplication implements IPlatformRunnable {

    /*
     * This is a workaround for a very mysterious bug we have when the policy
     * manager is started and the desktop agent is running. Somehow SWT fails
     * when calling OS.CreateWindowEx with a toubling "No more handles". It is
     * actually in the SWT native implementation of CreateWindowEx, the call to
     * CreateWindowExA() fails (the error is Class could not be found). Somehow,
     * some well known window classes are not found. Explicitely calling
     * InitCommonControlsEx() with 0x7FF ensures we are loading most of them
     * (refer to commctrl.h for the values), and from there everything seems to
     * be OK. Note that even if the agent does not inject anything (so it is
     * just creating a remote thread in the policy manager process), the problem
     * happens. Our best guess is when we inject our DLL, it depends on
     * comictl.dll and gets it loaded earlier than when Eclipse starts by
     * itself, causing the problem somehow.
     */
    private void bugWorkaroundInit() {
        INITCOMMONCONTROLSEX icex = new INITCOMMONCONTROLSEX();
        icex.dwSize = INITCOMMONCONTROLSEX.sizeof;
        icex.dwICC = 0x7FF;
        OS.InitCommonControlsEx(icex);
    }

    public Object run(Object args) {
        bugWorkaroundInit();

        setupServices();

        Display display = PlatformUI.createDisplay();
        try {
            Shell shell = display.getActiveShell();
            LoginDialog loginDialog = new LoginDialog(shell);
            if (loginDialog.open() != Window.OK)
                return IPlatformRunnable.EXIT_OK;

            GlobalState.user = loginDialog.getUsername();
            GlobalState.server = loginDialog.getPolicyServer();

            int returnCode = PlatformUI.createAndRunWorkbench(display, new RcpWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IPlatformRunnable.EXIT_RESTART;
            } else {
                return IPlatformRunnable.EXIT_OK;
            }
        } finally {
            display.dispose();
            ResourceManager.dispose();
        }
    }

    /**
     * Set up the services used by policy author
     */
    private void setupServices() {
        /*
         * Setup the Event Manager
         */
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(EventManagerImpl.class);

        // Setup event framework event listener
        eventManager.registerListener(new ObjectModifiedEventPropogationListener(eventManager), ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT);
    }

    /**
     * @author sgoldstein
     */
    public class ObjectModifiedEventPropogationListener implements IMultiContextualEventListener {

        private IEventManager eventManager;

        /**
         * Create an instance of ObjectModifiedEventPropogationListener
         * 
         * @param eventManager
         */
        public ObjectModifiedEventPropogationListener(IEventManager eventManager) {
            if (eventManager == null) {
                throw new NullPointerException("eventManager cannot be null.");
            }

            this.eventManager = eventManager;
        }

        /**
         * @see com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener#onEvents(java.util.Set)
         */
        public void onEvents(Set events) {
            Map contextObjectIdsToEventsMap = new HashMap();

            Iterator eventIterator = events.iterator();
            while (eventIterator.hasNext()) {
                IContextualEvent nextEvent = (IContextualEvent) eventIterator.next();
                Long nextEventContextId = ((IHasId) nextEvent.getEventContext()).getId();
                contextObjectIdsToEventsMap.put(nextEventContextId, nextEvent);
            }

            GlobalState globalState = GlobalState.getInstance();
            IHasId currentObject = globalState.getCurrentObject();
            if (currentObject != null) {
                Long currentObjectId = currentObject.getId();
                if (contextObjectIdsToEventsMap.containsKey(currentObjectId)) {
                    PolicyOrComponentModifiedEvent objectModifiedEvent = (PolicyOrComponentModifiedEvent) contextObjectIdsToEventsMap.get(currentObjectId);
                    IPolicyOrComponentData objectModifiedEventData = objectModifiedEvent.getEventContextAsPolicyOrComponentData();
                    CurrentPolicyOrComponentModifiedEvent currentObjectModifiedEvent = new CurrentPolicyOrComponentModifiedEvent(objectModifiedEventData);
                    this.eventManager.fireEvent(currentObjectModifiedEvent);
                }
            }

            Set selectedItems = globalState.getCurrentSelection();
            Iterator selectedItemsIterator = selectedItems.iterator();
            boolean selectedItemFound = false;
            while ((selectedItemsIterator.hasNext()) && (!selectedItemFound)) {
                DomainObjectDescriptor nextSelectedItem = (DomainObjectDescriptor) selectedItemsIterator.next();
                if (contextObjectIdsToEventsMap.containsKey(nextSelectedItem.getId())) {
                    SelectedItemsModifiedEvent selectedObjectsModifiedEvent = new SelectedItemsModifiedEvent(selectedItems);
                    this.eventManager.fireEvent(selectedObjectsModifiedEvent);
                    selectedItemFound = true;
                }
            }
        }
    }
}
