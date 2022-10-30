/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.editor.ComponentDetailsFactory;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/ui/dialogs/SubmitCheckDependenciesDialog.java#1 $
 */

public class VersionHistoryDialog extends Dialog {

    private static final Point SIZE = new Point(800, 600);

    private static final int DEPLOY_TIME = 0;
    private static final int STATUS = 1;

    private static final int HOST_NAME = 0;
    private static final int AGENT_TYPE = 1;

    private TableViewer tableViewer = null;
    private TabFolder tabFolder = null;
    private Composite objectViewerComposite = null;
    private TableViewer agentTableViewer = null;

    private int sortColumn = DEPLOY_TIME;
    private boolean sortAscending = false;

    private IHasId domainObject = null;
    protected DomainObjectDescriptor descriptor = null;
    private Date cutoff = new GregorianCalendar().getTime();

    private ArrayList timeRelationList = new ArrayList();

    private class TableContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (timeRelationList != null) {
                return timeRelationList.toArray();
            } else {
                return new Object[0];
            }
        }
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            TimeRelation timeRelation = (TimeRelation) obj;
            switch (index) {

            case DEPLOY_TIME:
                return (SimpleDateFormat.getDateTimeInstance().format(timeRelation.getActiveFrom()));
            case STATUS:
                Date asof = timeRelation.getActiveFrom();
                if (asof.after(cutoff)) {
                    return DialogMessages.VERSIONHISTORYDIALOG_SCHEDULED;
                } else if (timeRelation.getActiveTo().after(cutoff)) {
                    if (PolicyServerProxy.getDeployedObject(descriptor, timeRelation.getActiveFrom()) != null) {
                        return DialogMessages.VERSIONHISTORYDIALOG_ACTIVE;
                    } else {
                        return DialogMessages.VERSIONHISTORYDIALOG_INACTIVE;
                    }
                } else {
                    return DialogMessages.VERSIONHISTORYDIALOG_OBSOLETE;
                }
            }
            return null;
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

    }

    private class TableSorter extends ViewerSorter {

        public int compare(Viewer viewer, Object e1, Object e2) {
            TimeRelation timeRelation1 = (TimeRelation) e1;
            TimeRelation timeRelation2 = (TimeRelation) e2;

            switch (sortColumn) {
            case DEPLOY_TIME:
                if (sortAscending) {
                    return timeRelation1.getActiveFrom().compareTo(timeRelation2.getActiveFrom());
                } else {
                    return timeRelation2.getActiveFrom().compareTo(timeRelation1.getActiveFrom());
                }
            case STATUS:
            // FIXME return super.compare(viewer, deploymentRecord1.getStatus(),
            // deploymentRecord2.getStatus());
            }
            return 0;
        }
    }

    private class AgentTableContentProvider implements IStructuredContentProvider {

        private Object[] input = null;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null) {
                input = ((Collection) newInput).toArray();
            } else {
                input = null;
            }

        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (input != null) {
                return input;
            } else {
                return new Object[0];
            }
        }
    }

    private class AgentTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            AgentStatusDescriptor agentStatusDescriptor = (AgentStatusDescriptor) obj;
            switch (index) {

            case HOST_NAME:
                return (agentStatusDescriptor.getHostName());
            case AGENT_TYPE:
                if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.FILE_SERVER) {
                    return DialogMessages.VERSIONHISTORYDIALOG_FILE_SERVER_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.DESKTOP) {
                    return DialogMessages.VERSIONHISTORYDIALOG_DESKTOP_ENFORCER;
                } else if (agentStatusDescriptor.getAgentType() == AgentTypeEnumType.PORTAL) {
                    return DialogMessages.VERSIONHISTORYDIALOG_PORTAL_ENFORCER;
                } else {
                    return "Unknown";
                }
            }
            return null;
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

    }

    /**
     * Constructor
     */
    public VersionHistoryDialog(Shell parent, IHasId domainObject) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.domainObject = domainObject;
        EntityType entityType = DomainObjectHelper.getEntityType(domainObject);
        String name = DomainObjectHelper.getName(domainObject);
        if (entityType == EntityType.POLICY || entityType == EntityType.FOLDER) {
            descriptor = EntityInfoProvider.getPolicyDescriptor(name);
        } else {
            descriptor = EntityInfoProvider.getComponentDescriptor(name);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.VERSIONHISTORYDIALOG_TITLE + DomainObjectHelper.getName(domainObject));
        newShell.setSize(SIZE);
        newShell.setImage(ImageBundle.POLICY_IMG);

        getTimeRelations();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite root = (Composite) super.createDialogArea(parent);

        initialize(root);

        return parent;
    }

    /**
     * Gets the deployment history of the domainObject
     */
    private void getTimeRelations() {
        DomainObjectDescriptor descriptor;
        EntityType entityType = DomainObjectHelper.getEntityType(domainObject);
        String name = DomainObjectHelper.getName(domainObject);
        if (entityType == EntityType.POLICY) {
            descriptor = EntityInfoProvider.getPolicyDescriptor(name);
        } else {
            descriptor = EntityInfoProvider.getComponentDescriptor(name);
        }
        Collection<TimeRelation> records = PolicyServerProxy.getDeploymentRecords(descriptor);

        if (records != null) {
            timeRelationList = new ArrayList<TimeRelation>(records);
        }
    }

    /**
     * 
     */
    private void initialize(Composite root) {
        GridLayout layout = new GridLayout(2, true);
        root.setLayout(layout);

        tableViewer = new TableViewer(root, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);

        SelectionAdapter selectionAdapter = new SelectionAdapter() {

            /**
             * sets the sort column to the clicked column if the current sort
             * column is clicked, toggle the sort order
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                TableColumn column = (TableColumn) e.getSource();
                if (sortColumn != tableViewer.getTable().indexOf(column)) {
                    sortColumn = tableViewer.getTable().indexOf(column);
                    sortAscending = true;
                } else {
                    sortAscending = !sortAscending;
                }
                tableViewer.refresh();
            }
        };

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYMENT_TIME);
        column.setWidth(120);
        column.addSelectionListener(selectionAdapter);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYMENT_STATUS);
        column.setWidth(120);
        column.addSelectionListener(selectionAdapter);

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setSorter(new TableSorter());
        tableViewer.setInput(timeRelationList);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                if (selection != null) {
                    TimeRelation tr = (TimeRelation) selection.getFirstElement();
                    IHasId entity = (IHasId) PolicyServerProxy.getDeployedObject(descriptor, tr.getActiveFrom());

                    // remove previous panel before adding new one.
                    Control[] controls = objectViewerComposite.getChildren();
                    for (int i = 0; i < controls.length; i++) {
                        controls[i].dispose();
                    }

                    if (entity != null) {
                        ScrolledComposite componentViewerComposite = new ScrolledComposite(objectViewerComposite, SWT.V_SCROLL | SWT.H_SCROLL);
                        componentViewerComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                        componentViewerComposite.setLayout(new GridLayout());
                        GridData data = new GridData(GridData.FILL_BOTH);
                        componentViewerComposite.setLayoutData(data);
                        componentViewerComposite.setExpandHorizontal(true);
                        componentViewerComposite.setExpandVertical(true);
                        componentViewerComposite.getVerticalBar().setIncrement(10);
                        componentViewerComposite.getVerticalBar().setPageIncrement(100);
                        componentViewerComposite.getHorizontalBar().setIncrement(10);
                        componentViewerComposite.getHorizontalBar().setPageIncrement(100);

                        Composite detail = ComponentDetailsFactory.getEditorPanel(componentViewerComposite, SWT.NONE, entity);
                        data = new GridData(GridData.FILL_BOTH);
                        detail.setLayoutData(data);

                        componentViewerComposite.setContent(detail);
                        componentViewerComposite.setMinSize(detail.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                    } else {
                        Label emptyData = new Label(objectViewerComposite, SWT.NONE);
                        String typeStr;
                        if (descriptor.getType() == EntityType.POLICY) {
                            typeStr = DialogMessages.VERSIONHISTORYDIALOG_POLICY;
                        } else {
                            typeStr = DialogMessages.VERSIONHISTORYDIALOG_COMPONENT;
                        }
                        emptyData.setText(DialogMessages.VERSIONHISTORYDIALOG_NO_ACTIVE_VERSION + typeStr + DialogMessages.VERSIONHISTORYDIALOG_IS_DEFINED);
                        emptyData.setAlignment(SWT.CENTER);
                        emptyData.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    }
                    objectViewerComposite.layout(true, true);

                    Collection<AgentStatusDescriptor> agentStatusList = PolicyServerProxy.getAgentsForDeployedObject(descriptor, tr.getActiveFrom());
                    agentTableViewer.setInput(agentStatusList);
                }
            }
        });

        addTabs(root);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, DialogMessages.LABEL_CLOSE, true);
    }

    /**
     * 
     */
    private void addTabs(Composite root) {
        tabFolder = new TabFolder(root, SWT.NONE);
        GridLayout layout = new GridLayout();
        tabFolder.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        tabFolder.setLayoutData(data);

        TabItem definitionTabItem = new TabItem(tabFolder, SWT.NONE);
        definitionTabItem.setText(DialogMessages.VERSIONHISTORYDIALOG_DEFINITION);
        objectViewerComposite = new Composite(tabFolder, SWT.BORDER);
        objectViewerComposite.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        objectViewerComposite.setLayout(new GridLayout());
        definitionTabItem.setControl(objectViewerComposite);

        TabItem agentTabItem = new TabItem(tabFolder, SWT.NONE);
        agentTabItem.setText(DialogMessages.VERSIONHISTORYDIALOG_DEPLOYED_POLICY_ENFORCERS);
        Composite c = new Composite(tabFolder, SWT.NONE);
        c.setLayout(new FillLayout());
        agentTabItem.setControl(c);

        agentTableViewer = new TableViewer(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = agentTableViewer.getTable();
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_HOST_NAME);
        column.setWidth(150);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.VERSIONHISTORYDIALOG_TYPE);
        column.setWidth(100);

        agentTableViewer.setContentProvider(new AgentTableContentProvider());
        agentTableViewer.setLabelProvider(new AgentTableLabelProvider());
    }
}
