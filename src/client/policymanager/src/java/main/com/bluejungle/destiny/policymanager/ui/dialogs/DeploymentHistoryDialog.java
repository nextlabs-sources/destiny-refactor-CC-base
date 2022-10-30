package com.bluejungle.destiny.policymanager.ui.dialogs;

/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.framework.standardlisteners.TableColumnResizeListener;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 */

public class DeploymentHistoryDialog extends Dialog {

    private static final Point SIZE = new Point(600, 500);

    private static final int SCHEDULED_TIME = 0;
    private static final int DEPLOY_TIME = 1;
    private static final int CONTENTS = 2;
    private static final int STATUS = 3;

    private Timer autoRefresh;
    private List<DeploymentRecord> deploymentRecords;
    private TableViewer tableViewer;
    private Button cancelDeploymentButton;
    private int sortColumn = SCHEDULED_TIME;
    private boolean sortAscending = false;

    private class TableContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (deploymentRecords != null) {
                return deploymentRecords.toArray();
            } else {
                return new Object[0];
            }
        }
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            Date cutoff = new Date();
            DeploymentRecord record = (DeploymentRecord) obj;
            switch (index) {
            case SCHEDULED_TIME:
                return SimpleDateFormat.getDateTimeInstance().format(record.getWhenRequested());
            case DEPLOY_TIME:
                return SimpleDateFormat.getDateTimeInstance().format(record.getAsOf());
            case CONTENTS:
                return NLS.bind(DialogMessages.DEPLOYMENTHISTORYDIALOG_OBJECTS, record.getNumberOfDeployedEntities());
            case STATUS:
                if (record.getAsOf().after(cutoff)) {
                    return DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED;
                } else {
                    return DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYED;
                }
            }
            return null;
        }

        public Image getColumnImage(Object obj, int index) {
            return null;
        }

        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    class TableSorter extends ViewerSorter {

        public int compare(Viewer viewer, Object e1, Object e2) {
            DeploymentRecord deploymentRecord1 = (DeploymentRecord) e1;
            DeploymentRecord deploymentRecord2 = (DeploymentRecord) e2;

            switch (sortColumn) {
            case SCHEDULED_TIME:
                if (sortAscending) {
                    return deploymentRecord1.getWhenRequested().compareTo(deploymentRecord2.getWhenRequested());
                } else {
                    return deploymentRecord2.getWhenRequested().compareTo(deploymentRecord1.getWhenRequested());
                }
            case DEPLOY_TIME:
                if (sortAscending) {
                    return deploymentRecord1.getAsOf().compareTo(deploymentRecord2.getAsOf());
                } else {
                    return deploymentRecord2.getAsOf().compareTo(deploymentRecord1.getAsOf());
                }
            case STATUS:
            // FIXME return super.compare(viewer,
            // deploymentRecord1.getStatus(),
            // deploymentRecord2.getStatus());
            }
            return 0;
        }
    }

    /**
     * Constructor
     */
    public DeploymentHistoryDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(SIZE);
        newShell.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_TITLE);
        newShell.setImage(ImageBundle.POLICY_IMG);

        autoRefresh = new Timer();
        final Display display = newShell.getDisplay();
        autoRefresh.schedule(new TimerTask() {

            public void run() {
                display.asyncExec(new Runnable() {

                    public void run() {
                        updateCancelButtonState();
                        tableViewer.refresh();
                        setTableColors();
                        display.update();
                    }
                });
            }
        }, 0, 10000);
    }

    @Override
    public boolean close() {
        autoRefresh.cancel();
        return super.close();
    }

    /**
     * Gets all deployment records from eight day earlier up to the year 2020
     */
    private void getDeploymentRecords() {
        Calendar start = new GregorianCalendar();
        // Get the history for the last 8 days
        start.add(Calendar.DAY_OF_MONTH, -8);

        Calendar end = new GregorianCalendar();
        end.set(Calendar.YEAR, 2020);

        deploymentRecords = new ArrayList<DeploymentRecord>(PolicyServerProxy.getDeploymentRecords(start.getTime(), end.getTime()));
        for (int i = 0; i < deploymentRecords.size(); i++) {
            DeploymentRecord record = (DeploymentRecord) deploymentRecords.get(i);
            if (record.isCancelled() || record.isHidden()) {
                deploymentRecords.remove(i--);
            }
        }
    }

    protected Control createDialogArea(Composite parent) {
        Composite root = (Composite) super.createDialogArea(parent);

        tableViewer = new TableViewer(root, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        SelectionListener selectionListener = new SelectionAdapter() {

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
                setTableColors();
            }
        };

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_SCHEDULED_TIME);
        column.setWidth(180);
        column.addSelectionListener(selectionListener);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_DEPLOYMENT_TIME);
        column.setWidth(180);
        column.addSelectionListener(selectionListener);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_CONTENTS);
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_STATUS);
        column.setWidth(100);
        column.addSelectionListener(selectionListener);

        table.addMouseListener(new MouseAdapter() {

            public void mouseDoubleClick(MouseEvent e) {
                cancelDeployment();
            }
        });

        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setSorter(new TableSorter());
        getDeploymentRecords();
        tableViewer.setInput(deploymentRecords);
        setTableColors();

        cancelDeploymentButton = new Button(root, SWT.PUSH);
        cancelDeploymentButton.setText(DialogMessages.DEPLOYMENTHISTORYDIALOG_CANCEL_SELECTED_DEPLOYMENT);

        cancelDeploymentButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                cancelDeployment();
            }
        });

        // Enable/disable cancel button based on state.
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                updateCancelButtonState();
            }
        });

        updateCancelButtonState();

        root.addListener(SWT.Resize, new TableColumnResizeListener(table, root));

        return parent;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, DialogMessages.LABEL_CLOSE, false);
    }

    protected void setTableColors() {
        Date cutoff = new Date();
        Table table = tableViewer.getTable();
        TableItem items[] = table.getItems();
        for (TableItem item : items) {
            DeploymentRecord record = (DeploymentRecord) item.getData();
            if (!record.getAsOf().after(cutoff)) {
                item.setForeground(ResourceManager.getColor(SWT.COLOR_DARK_GRAY));
            } else {
                item.setForeground(ResourceManager.getColor(SWT.COLOR_BLACK));
            }
        }
    }

    /**
     * Opens the cancel deployment dialog for the selected deployment record
     */
    protected void cancelDeployment() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        if (selection != null) {
            DeploymentRecord dr = (DeploymentRecord) selection.getFirstElement();
            boolean canCancel = cancelDeploymentButton.getEnabled();
            CancelDeploymentDialog dlg = new CancelDeploymentDialog(getShell(), dr, canCancel);
            if (dlg.open() == Window.OK) {
                deploymentRecords.remove(dr);
                tableViewer.refresh();
                setTableColors();
            }
        }
    }

    /**
     * enable/disable cancel button depending on the current selected deployment
     * record.
     */
    private void updateCancelButtonState() {
        Calendar cutoffCal = new GregorianCalendar();
        cutoffCal.add(Calendar.MINUTE, 1);
        Date cutoff = cutoffCal.getTime();
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        if (selection != null) {
            DeploymentRecord dr = (DeploymentRecord) selection.getFirstElement();
            if (dr != null && dr.getAsOf().after(cutoff)) {
                // See if the current user is authorized to cancel the
                // deployment
                Collection<DomainObjectDescriptor> deployed = PolicyServerProxy.getObjectsInDeploymentRecord(dr);
                Collection<DomainObjectDescriptor> canUndeploy = PolicyServerProxy.filterByAllowedAction(deployed, DAction.DEPLOY);
                // The button should be enabled only when the user has
                // deployment rights for all the deployed objects:
                cancelDeploymentButton.setEnabled(deployed != null && canUndeploy != null && deployed.size() == canUndeploy.size());
            } else {
                cancelDeploymentButton.setEnabled(false);
            }
        }
    }
}