/*
 * Created on May 5, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.usergroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoListener;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.IClipboardEnabled;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PolicyTransfer;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControl;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlEvent;
import com.bluejungle.destiny.policymanager.ui.controls.FilterControlListener;
import com.bluejungle.destiny.policymanager.ui.dialogs.NewPolicyDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;

/**
 * @author bmeng
 * 
 */
public class PolicyListPanel extends Composite implements IClipboardEnabled {

    private static final boolean INCLUDE_TEXT_DRAG_SUPPORT = true;
    private static final List<EntityType> POLICIES_AND_FOLDERS = Arrays.asList(new EntityType[] { EntityType.POLICY, EntityType.FOLDER });
    private static final DomainObjectFormatter formatter = new DomainObjectFormatter();

    protected boolean ignoreSelection = false;
    protected boolean restoreSelection = true;

    private FilterControl filterControl;
    private TreeViewer treeViewer;
    private TableViewer filteredViewer;
    private Composite buttonRow;
    private Button addPolicyButton;
    private Button addFolderButton;

    private List<DomainObjectDescriptor> objectList = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_byName = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_byComponent = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> searchResults_bySingleComponent = new ArrayList<DomainObjectDescriptor>();

    private boolean filtering = false;
    private boolean showingUsage = false;

    private List<Item> selectedItems = new ArrayList<Item>();
    private FilteredContentProvider filteredContentProvider;
    private boolean ignoreCurrentObjectChangedEvent = false;

    private DragSourceListener dragSourceListener = new DragSourceAdapter() {

        public void dragStart(DragSourceEvent e) {
            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                for (Iterator iter = ((IStructuredSelection) treeViewer.getSelection()).iterator(); e.doit && iter.hasNext();) {
                    Object tmp = iter.next();
                    if (!(tmp instanceof DomainObjectDescriptor)) {
                        continue;
                    }
                    e.doit &= ((DomainObjectDescriptor) tmp).isAccessible();
                }

            } else {
                e.doit = false;
            }
        }

        public void dragFinished(DragSourceEvent e) {
            getDisplay().readAndDispatch();

            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                restoreTreeSelection();
            } else {
                restoreTableSelection();
            }
            ignoreSelection = false;
        }

        public void dragSetData(DragSourceEvent e) {
            DragSource source = (DragSource) e.getSource();
            if (source.getControl() instanceof Tree) {
                PolicyTransfer policyTransfer = PolicyTransfer.getInstance();
                if (policyTransfer.isSupportedType(e.dataType)) {
                    IStructuredSelection selection = null;
                    selection = (IStructuredSelection) treeViewer.getSelection();
                    DomainObjectDescriptor[] descriptors = new DomainObjectDescriptor[selection.size()];
                    int i = 0;
                    for (Iterator iter = selection.iterator(); iter.hasNext();) {
                        Object tmp = iter.next();
                        if (!(tmp instanceof DomainObjectDescriptor)) {
                            continue;
                        }
                        descriptors[i++] = (DomainObjectDescriptor) tmp;
                    }
                    e.data = descriptors;
                    return;
                }

                TextTransfer transfer = TextTransfer.getInstance();
                if (transfer.isSupportedType(e.dataType)) {
                    String pql = getPQLForSelection();
                    e.data = pql;
                }
            }
        }
    };

    private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {

        public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection selection;
            if (event.getSource() instanceof TreeViewer) {
                selection = (IStructuredSelection) treeViewer.getSelection();
            } else {
                selection = (IStructuredSelection) filteredViewer.getSelection();
            }
            Object selected = selection.getFirstElement();
            if (selected instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor descriptor = (DomainObjectDescriptor) selected;
                loadPolicyInEditPanel(descriptor);
            }
        }
    };

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (!ignoreSelection) {
                if (event.getSource() instanceof TreeViewer) {
                    setupNewTreeSelection();
                } else {
                    setupNewTableSelection();
                }
            }
        }
    };

    private class MouseAndTreeListener implements MouseListener, TreeListener {

        private Item clickedItem = null;
        private boolean treeChanged = false;

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
            ignoreSelection = true;
            if (e.getSource() instanceof Tree) {
                clickedItem = treeViewer.getTree().getItem(new Point(e.x, e.y));
            } else {
                clickedItem = filteredViewer.getTable().getItem(new Point(e.x, e.y));
            }
        }

        public void mouseUp(MouseEvent e) {
            Item item = null;
            boolean treeEvent = false;
            if (e.getSource() instanceof Tree) {
                treeEvent = true;
                item = treeViewer.getTree().getItem(new Point(e.x, e.y));
            } else {
                item = filteredViewer.getTable().getItem(new Point(e.x, e.y));
            }
            if (clickedItem != null || treeChanged) {
                if (item == clickedItem) {
                    boolean enabled = true;
                    if (item != null) {
                        Object data = item.getData();
                        if (data instanceof DomainObjectDescriptor) {
                            DomainObjectDescriptor dod = (DomainObjectDescriptor) data;
                            enabled = dod.isAccessible();
                        }
                    }
                    if (enabled) {
                        if (treeEvent) {
                            setupNewTreeSelection();
                            if ((e.button == 3) && (shouldShowContextMenu((TreeItem) item))) {
                                showContextMenu(e);
                            }
                        } else {
                            setupNewTableSelection();
                            if ((e.button == 3) && (shouldShowContextMenu((TableItem) item))) {
                                showContextMenu(e);
                            }
                        }
                    } else {
                        if (treeEvent) {
                            restoreTreeSelection();
                        } else {
                            restoreTableSelection();
                        }
                    }
                } else {
                    if (treeEvent) {
                        restoreTreeSelection();
                    } else {
                        restoreTableSelection();
                    }
                }
            } else {
                if (treeEvent) {
                    clearTreeSelection();
                } else {
                    clearTableSelection();
                }
            }
            ignoreSelection = false;
            treeChanged = false;
        }

        public void treeCollapsed(TreeEvent e) {
            changed(e);
            TreeItem item = (TreeItem) e.item;
            item.setImage(ImageBundle.FOLDER_IMG);
        }

        public void treeExpanded(TreeEvent e) {
            changed(e);
            TreeItem item = (TreeItem) e.item;
            item.setImage(ImageBundle.FOLDER_OPEN_IMG);
        }

        private void changed(TreeEvent e) {
            setTreeItemColors(((Tree) e.getSource()).getItems());
            treeChanged = true;
        }
    };

    private MouseAndTreeListener mouseAndTreeListener = new MouseAndTreeListener();

    private DropTargetListener dropTargetListener = new DropTargetAdapter() {

        /**
         * Enable dropping only if dragging over a folder or an empty space.
         */
        public void dragOver(DropTargetEvent event) {
            event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
            Tree tree = treeViewer.getTree();
            TreeItem item = tree.getItem(tree.toControl(new Point(event.x, event.y)));
            if (item != null) {
                DomainObjectDescriptor descriptor = (DomainObjectDescriptor) item.getData();
                if (descriptor.getType() == EntityType.FOLDER && descriptor.isAccessible()) {
                    event.detail = DND.DROP_MOVE;
                } else {
                    event.detail = DND.DROP_NONE;
                }
            } else {
                // Top level
                event.detail = DND.DROP_MOVE;
            }
        }

        /**
         * Renames all dropped policies to move them to the dropped folder
         * 
         * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
         */
        public void drop(DropTargetEvent event) {
            GlobalState gs = GlobalState.getInstance();
            gs.saveEditorPanel();

            DomainObjectDescriptor[] descriptors = (DomainObjectDescriptor[]) PolicyTransfer.getInstance().nativeToJava(event.currentDataType);
            Set<DomainObjectDescriptor> allMoved = new HashSet<DomainObjectDescriptor>();
            String parentFolder = null;
            for (DomainObjectDescriptor descriptor : descriptors) {
                if (descriptor == null)
                    return;
                allMoved.add(descriptor);
                String objectName = descriptor.getName();
                // Add the parent name to movedFolders
                int fsIndex = objectName.lastIndexOf(PQLParser.SEPARATOR);
                String parentFolderOfThisObject = objectName.substring(0, fsIndex + 1);
                if (parentFolder == null) {
                    parentFolder = parentFolderOfThisObject;
                } else if (!parentFolder.equals(parentFolderOfThisObject)) {
                    // All moved objects must be from the same folder
                    event.detail = DND.DROP_NONE;
                    return;
                }
                if (descriptor.getType() == EntityType.FOLDER) {
                    allMoved.addAll(PolicyServerProxy.getEntityList(PolicyServerProxy.escape(descriptor.getName()) + PQLParser.SEPARATOR + "%", POLICIES_AND_FOLDERS));
                }
            }

            // Do not restore selection since objects are being moved around.
            restoreSelection = false;

            IHasId currentObj = (IHasId) gs.getCurrentObject();
            Long currentObjId = (currentObj != null) ? currentObj.getId() : null;
            String targetFolderName;
            Tree tree = treeViewer.getTree();
            TreeItem item = tree.getItem(tree.toControl(new Point(event.x, event.y)));
            if (item != null) {
                DomainObjectDescriptor folderDescriptor = (DomainObjectDescriptor) item.getData();
                // This has been checked in the dropAccept,
                // but we double-check it here to avoid nasty consequences.
                if (folderDescriptor.getType() != EntityType.FOLDER) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                targetFolderName = folderDescriptor.getName() + PQLParser.SEPARATOR;
            } else {
                targetFolderName = "";
            }

            // Dropping to the original location is a no-op
            if (parentFolder.equalsIgnoreCase(targetFolderName)) {
                event.detail = DND.DROP_NONE;
                return;
            }

            int topFolderLength = parentFolder.length();

            Collection<? extends IHasId> policiesAndFolders = PolicyServerProxy.getEntitiesForDescriptor(allMoved);
            Map<String, String> policyMap = new HashMap<String, String>();
            Map<String, String> folderMap = new HashMap<String, String>();
            for (IHasId next : policiesAndFolders) {
                if (next instanceof IDPolicy) {
                    IDPolicy p = (IDPolicy) next;
                    String oldName = p.getName();
                    String newName = targetFolderName + oldName.substring(topFolderLength);
                    Collection<DomainObjectDescriptor> existings = PolicyServerProxy.getEntityList(PolicyServerProxy.escape(newName), POLICIES_AND_FOLDERS);
                    for (DomainObjectDescriptor dod : existings) {
                        if (dod.getName().equalsIgnoreCase(newName) && dod.getType() == EntityType.POLICY) {
                            showError(newName.replace(PQLParser.SEPARATOR, '/') + " already exists.");
                            return;
                        } else {
                            continue;
                        }
                    }
                    if (newName.indexOf(PQLParser.SEPARATOR) == -1) {
                        showError("Policies may not be moved outside of folders.");
                        return;
                    }
                } else if (next instanceof PolicyFolder) {
                    PolicyFolder f = (PolicyFolder) next;
                    String oldName = f.getName();
                    String newName = targetFolderName + oldName.substring(topFolderLength);
                    if (targetFolderName.startsWith(oldName + PQLParser.SEPARATOR)) {
                        showError("A policy folder may not be moved to one of its descendant folders.");
                        return;
                    }
                    Collection<DomainObjectDescriptor> existings = PolicyServerProxy.getEntityList(newName, POLICIES_AND_FOLDERS);
                    if (existings != null && !existings.isEmpty()) {
                        DomainObjectDescriptor dod = (DomainObjectDescriptor) existings.iterator().next();
                        if (!dod.getName().equalsIgnoreCase(f.getName()) || dod.getType() != EntityType.FOLDER) {
                            showError(newName.replace(PQLParser.SEPARATOR, '/') + " already exists.");
                            return;
                        } else {
                            continue;
                        }
                    }
                } else {
                    throw new IllegalStateException("The list contains items other than Policies/Policy Folders");
                }
            }
            for (IHasId next : policiesAndFolders) {
                if (next == null) {
                    continue;
                }
                if (next instanceof Policy) {
                    Policy p = (Policy) next;
                    String oldName = p.getName();
                    String newName = targetFolderName + oldName.substring(topFolderLength);
                    policyMap.put(newName, oldName);
                    p.setName(newName);
                } else if (next instanceof PolicyFolder) {
                    PolicyFolder f = (PolicyFolder) next;
                    String oldName = f.getName();
                    String newName = targetFolderName + oldName.substring(topFolderLength);
                    folderMap.put(newName, oldName);
                    f.setName(newName);
                }
                if (next.getId().equals(currentObjId)) {
                    gs.closeEditorFor(next);
                }
            }
            Collection<DomainObjectDescriptor> newDescriptors = PolicyServerProxy.saveEntities(policiesAndFolders);
            for (DomainObjectDescriptor descriptor : newDescriptors) {
                String oldName;
                if (descriptor.getType() == EntityType.POLICY) {
                    oldName = (String) policyMap.get(descriptor.getName());
                } else if (descriptor.getType() == EntityType.FOLDER) {
                    oldName = (String) folderMap.get(descriptor.getName());
                } else {
                    throw new IllegalStateException("The list contains items other than Policies/Policy Folders");
                }
                EntityInfoProvider.replacePolicyDescriptor(oldName, descriptor);
                if (descriptor.getId().equals(currentObjId)) {
                    treeViewer.setSelection(new StructuredSelection(descriptor), true);
                    gs.setCurrentlySelection(Arrays.asList(new DomainObjectDescriptor[] { descriptor }));
                    gs.forceLoadObjectInEditorPanel(descriptor);
                }
            }
        }

        public void dropAccept(DropTargetEvent event) {
            DomainObjectDescriptor[] descriptors = (DomainObjectDescriptor[]) PolicyTransfer.getInstance().nativeToJava(event.currentDataType);

            // Verify that the collection of dropped items contains only
            // policies and policy folders
            for (DomainObjectDescriptor descriptor : descriptors) {
                if (descriptor != null && descriptor.getType() != EntityType.POLICY && descriptor.getType() != EntityType.FOLDER) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
            }
        }

        private void showError(String error) {
            MessageDialog.openError(getShell(), "Error moving objects", error);
        }
    };

    private class ViewContentProvider implements ITreeContentProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            List<DomainObjectDescriptor> elements = new ArrayList<DomainObjectDescriptor>();
            DomainObjectDescriptor parentDescriptor = (DomainObjectDescriptor) parentElement;
            if (!parentDescriptor.isAccessible() || parentDescriptor.getType() == EntityType.POLICY) {
                return new Object[0];
            }
            String parentName = parentDescriptor.getName() + PQLParser.SEPARATOR;

            for (DomainObjectDescriptor descriptor : objectList) {
                String name = descriptor.getName();
                // children names start with the name of the parent
                if (name.startsWith(parentName)) {
                    String nameEnding = name.substring(parentName.length(), name.length());

                    int index = nameEnding.indexOf(PQLParser.SEPARATOR);
                    // child objects have no further separators
                    if (index < 0) {
                        elements.add(descriptor);
                    }
                }
            }
            DomainObjectDescriptor[] res = elements.toArray(new DomainObjectDescriptor[elements.size()]);
            Arrays.sort(res, DomainObjectDescriptor.CASE_INSENSITIVE_COMPARATOR);
            return res;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            DomainObjectDescriptor descriptor = (DomainObjectDescriptor) element;
            String name = descriptor.getName();

            int index = name.lastIndexOf(PQLParser.SEPARATOR);
            if (index < 0) {
                return null;
            }
            if (index == name.length()) {
                index = name.substring(name.length() - 1).lastIndexOf(PQLParser.SEPARATOR);
            }
            String parentName = name.substring(0, index);
            return EntityInfoProvider.getPolicyDescriptor(parentName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            if (element instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor dod = (DomainObjectDescriptor) element;
                if (!dod.isAccessible()) {
                    return false;
                }
                EntityType type = dod.getType();
                String elemName = dod.getName();
                if (type == EntityType.FOLDER) {
                    String childPrefix = elemName + PQLParser.SEPARATOR;
                    for (DomainObjectDescriptor desc : objectList) {
                        if (desc.getName().startsWith(childPrefix)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            // TODO fix this later when real API is available
            List<DomainObjectDescriptor> elements = new ArrayList<DomainObjectDescriptor>();

            for (DomainObjectDescriptor descriptor : objectList) {
                String name = descriptor.getName();
                int index = name.indexOf(PQLParser.SEPARATOR);
                // root level objects either have no separators, or have their
                // first separator at the end of their name
                if (index < 0 || index == name.length() - 1) {
                    elements.add(descriptor);
                }
            }

            return elements.toArray();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class FilteredContentProvider implements IStructuredContentProvider {

        private boolean inProgress = true;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            String text = filterControl.getText();
            List<Object> list = new ArrayList<Object>();
            if (showingUsage) {
                list.add(Messages.POLICYLISTPANEL_FILTER_POLICIES_USING_THE_COMPONENT + filterControl.getBoxText() + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_bySingleComponent.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_bySingleComponent);
                }
            } else {
                list.add(Messages.POLICYLISTPANEL_FILTER_POLICIES_NAMED + text + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_byName.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byName);
                }
                list.add(Messages.POLICYLISTPANEL_FILTER_POLICIES_USING_THE_COMPONENT + text + Messages.POLICYLISTPANEL_FILTER_END);
                if (searchResults_byComponent.isEmpty()) {
                    list.add(emptyString());
                } else {
                    list.addAll(searchResults_byComponent);
                }
            }
            return list.toArray();
        }

        private String emptyString() {
            return inProgress ? Messages.IN_PROGRESS_STRING : Messages.EMPTY_LIST_STRING;
        }

        public void setInProgress(boolean val) {
            inProgress = val;
        }

    }

    private class ViewLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            if (element instanceof String) {
                return null;
            }
            return ObjectLabelImageProvider.getImage(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor dod = (DomainObjectDescriptor) element;
                String fullName = dod.getName();
                int index = fullName.lastIndexOf(PQLParser.SEPARATOR);
                if (index < 0) {
                    return fullName;
                } else {
                    return fullName.substring(index + 1, fullName.length());
                }
            }
            return "";
        }
    }

    private class FilteredLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            if (element instanceof String) {
                return null;
            }
            return ObjectLabelImageProvider.getImage(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            if (element instanceof DomainObjectDescriptor) {
                String fullName = ((DomainObjectDescriptor) element).getName();
                return fullName;
            }
            return "";
        }
    }

    private class FilterWorker implements Runnable {

        private String nameFilter;
        private FilterControlListener.EndOfSearch endOfSearch;

        public FilterWorker(String filter, FilterControlListener.EndOfSearch endOfSearch) {
            this.nameFilter = filter;
            this.endOfSearch = endOfSearch;
        }

        public void run() {
            String filterStringPolicies = "%" + PolicyServerProxy.escape(nameFilter) + "%";
            Collection<DomainObjectDescriptor> entities = PolicyServerProxy.getPolicyList(filterStringPolicies);
            searchResults_byName.clear();
            searchResults_byName.addAll(entities);

            searchResults_byComponent.clear();
            String filterStringComponents = "%" + PQLParser.SEPARATOR + "%" + PolicyServerProxy.escape(nameFilter) + "%";
            entities = PolicyServerProxy.getPoliciesUsingComponent(filterStringComponents);
            searchResults_byComponent.addAll(entities);
            if (filteredContentProvider != null) {
                filteredContentProvider.setInProgress(false);
            }

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    if (filteredViewer != null) {
                        filteredViewer.refresh();
                        resetTableItems();
                        // format special lines
                        int offset = 0;
                        formatTitleRow(offset++);
                        if (searchResults_byName.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                        offset += searchResults_byName.size();
                        formatTitleRow(offset++);
                        if (searchResults_byComponent.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                    }
                }
            });
            endOfSearch.endOfSearch();
        }
    }

    private class PolicyUsageWorker implements Runnable {

        private String nameFilter;
        private EntityType entityType;

        public PolicyUsageWorker(EntityType entityType, String filter) {
            this.entityType = entityType;
            this.nameFilter = filter;
        }

        public void run() {
            Collection<DomainObjectDescriptor> entities = PolicyServerProxy.getPoliciesUsingComponent(nameFilter, entityType);
            searchResults_bySingleComponent.clear();
            searchResults_bySingleComponent.addAll(entities);

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    if (filteredViewer != null) {
                        filteredViewer.refresh();
                        resetTableItems();
                        // format special lines
                        int offset = 0;
                        formatTitleRow(offset++);
                        if (searchResults_bySingleComponent.isEmpty()) {
                            formatEmptyMessage(offset++);
                        }
                    }
                }
            });
        }
    }

    private void formatTitleRow(int index) {
        TableItem item = filteredViewer.getTable().getItem(index);
        item.setBackground(ColorBundle.LIGHT_GRAY);
    }

    private void formatEmptyMessage(int index) {
        TableItem item = filteredViewer.getTable().getItem(index);
        item.setForeground(ColorBundle.LIGHT_GRAY);
    }

    private void resetTableItems() {
        TableItem[] items = filteredViewer.getTable().getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].setBackground(null);
            items[i].setForeground(null);
        }
        setTableItemColors(items);
    }

    public PolicyListPanel(Composite parent, int style) {
        super(parent, style);

        initialize();
        final EntityInfoListener infoListener = new EntityInfoListener() {

            boolean firstTime = true;

            public void EntityInfoUpdated() {
                if (firstTime) {
                    firstTime = false;
                    boolean policiesAreAllowed = PolicyServerProxy.getAllowedEntityTypes().contains(EntityType.POLICY);
                    addPolicyButton.setEnabled(policiesAreAllowed);
                    // addUsagePolicyButton.setEnabled(policiesAreAllowed);
                    addFolderButton.setEnabled(policiesAreAllowed);
                }
                if (treeViewer != null && !filtering && !showingUsage) {
                    objectList.clear();
                    objectList.addAll(EntityInfoProvider.getPolicyList());
                    treeViewer.refresh();
                    getDisplay().readAndDispatch();
                    setSelectionToEditorObject();
                    setTreeItemColors(treeViewer.getTree().getItems());
                } else if ((filteredViewer != null) & (filtering) && (!showingUsage)) {
                    Thread t = new Thread(new FilterWorker(filterControl.getText(), FilterControlListener.EMPTY_END_OF_SEARCH));
                    t.start();
                }
            }
        };
        EntityInfoProvider.addPolicyInfoListener(infoListener);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                EntityInfoProvider.removePolicyInfoListener(infoListener);
            }
        });

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.registerListener(new CurrentObjectChangedListener(), EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
    }

    /**
     * select tree item corresponding to object in editor
     */
    private void setSelectionToEditorObject() {
        GlobalState gs = GlobalState.getInstance();
        IHasId currentObject = (IHasId) gs.getCurrentObject();
        EntityType entityType = DomainObjectHelper.getEntityType(currentObject);
        if (currentObject != null && (entityType == EntityType.POLICY || entityType == EntityType.FOLDER)) {

            DomainObjectDescriptor descriptor = EntityInfoProvider.getPolicyDescriptor(DomainObjectHelper.getName(currentObject));
            if ((descriptor != null) && (treeViewer != null)) {
                ignoreSelection = true;
                treeViewer.setSelection(new StructuredSelection(descriptor), true);
                gs.setCurrentlySelection(Collections.singleton(descriptor));
                memorizeTreeSelection();
            }
        }
    }

    protected void initialize() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        CLabel button = new CLabel(this, SWT.LEFT);
        Color color = ResourceManager.getColor(163, 178, 204);
        Color lightColor = ResourceManager.getColor(177, 190, 212);
        Color[] colorGradient = new Color[] { ResourceManager.getColor(SWT.COLOR_DARK_GRAY), color, lightColor, ResourceManager.getColor(SWT.COLOR_WHITE) };
        button.setText(Messages.POLICYLISTPANEL_POLICIES);
        button.setBackground(colorGradient, new int[] { 5, 95, 100 }, true);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(data);

        setupAddControl();

        setupTreeViewer();
    }

    private void setupFilterControl(Composite container) {
        filterControl = new FilterControl(container, SWT.NONE, Messages.FIND_STRING, Messages.FIND_INSTRUCTIONS);
        filterControl.addFilterControlListener(new FilterControlListener() {

            public void search(FilterControlEvent e, FilterControlListener.EndOfSearch endOfSearch) {
                // TODO: populate filtered list
                filtering = true;
                if (filteredViewer == null) {
                    treeViewer.getTree().dispose();
                    treeViewer = null;
                    setupFilteredViewer();
                    filteredViewer.getTable().moveBelow(filterControl);
                    layout();
                } else {
                    if (filteredContentProvider != null) {
                        filteredContentProvider.setInProgress(true);
                    }
                    filteredViewer.refresh();
                }
                Thread t = new Thread(new FilterWorker(filterControl.getText(), endOfSearch));
                t.start();
            }

            public void cancel(FilterControlEvent e) {
                filtering = false;
                if (treeViewer == null) {
                    filteredViewer.getTable().dispose();
                    filteredViewer = null;
                    setupTreeViewer();
                    treeViewer.getTree().moveBelow(filterControl);
                    layout();
                }
                populateList();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        filterControl.setLayoutData(data);
    }

    public void setupPolicyUsageFilterControl(String componentName, EntityType type) {
        if (filterControl != null) {
            filterControl.dispose();
        }
        showingUsage = true;
        filterControl = new FilterControl(this, SWT.NONE, "Show policies using ", componentName);
        filterControl.setEditable(false);
        filterControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (filteredViewer == null) {
            treeViewer.getTree().dispose();
            treeViewer = null;
            setupFilteredViewer();
            filteredViewer.getTable().moveBelow(filterControl);
        }
        filterControl.moveAbove(filteredViewer.getTable());

        layout();

        // populate filtered list here
        Thread t = new Thread(new PolicyUsageWorker(type, componentName));
        t.start();

        filterControl.addFilterControlListener(new FilterControlListener() {

            public void search(FilterControlEvent e, FilterControlListener.EndOfSearch eos) {
            }

            public void cancel(FilterControlEvent e) {
                if (treeViewer == null) {
                    showingUsage = false;
                    filteredViewer.getTable().dispose();
                    filteredViewer = null;
                    setupTreeViewer();
                    treeViewer.getTree().moveBelow(filterControl);
                    filterControl.dispose();
                    setupFilterControl(buttonRow);
                    // setupFilterControl();
                    filterControl.moveBelow(addFolderButton);
                    layout(true, true);
                }
                populateList();
            }
        });
    }

    private void setupTreeViewer() {
        treeViewer = new TreeViewer(this, SWT.MULTI | SWT.BORDER);

        /*
         * User hash table lookup of tree items. Increased performance at the
         * cost of increased memory usage
         */
        treeViewer.setUseHashlookup(true);

        /*
         * User custom comparer for hash lookup
         */
        treeViewer.setComparer(new TreeItemComparer());

        GridData gridData = new GridData(GridData.FILL_BOTH);
        treeViewer.getTree().setLayoutData(gridData);
        treeViewer.setContentProvider(new ViewContentProvider());
        treeViewer.setLabelProvider(new ViewLabelProvider());
        treeViewer.setInput(objectList);

        // treeViewer.addDoubleClickListener(doubleClickListener);
        treeViewer.getTree().addMouseListener(mouseAndTreeListener);
        treeViewer.getTree().addTreeListener(mouseAndTreeListener);
        treeViewer.addSelectionChangedListener(selectionChangedListener);

        if (INCLUDE_TEXT_DRAG_SUPPORT) {
            treeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { PolicyTransfer.getInstance(), TextTransfer.getInstance() }, dragSourceListener);
        }

        int operations = DND.DROP_MOVE;
        Transfer[] types = new Transfer[] { PolicyTransfer.getInstance() };

        treeViewer.addDropSupport(operations, types, dropTargetListener);
        setTreeItemColors(treeViewer.getTree().getItems());
    }

    private void setTreeItemColors(TreeItem[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i != items.length; i++) {
            Object data = items[i].getData();
            if (data instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor dod = (DomainObjectDescriptor) data;
                if (dod.isAccessible()) {
                    items[i].setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
                } else {
                    items[i].setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
                }
                setTreeItemColors(items[i].getItems());
            }
        }
    }

    private void setTableItemColors(TableItem[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i != items.length; i++) {
            Object data = items[i].getData();
            if (data instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor dod = (DomainObjectDescriptor) data;
                if (dod.isAccessible()) {
                    items[i].setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
                } else {
                    items[i].setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
                }
            }
        }
    }

    private void setupFilteredViewer() {
        filteredViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        Table table = filteredViewer.getTable();
        GridData gridData = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);

        TableColumn c1 = new TableColumn(table, SWT.LEFT);
        c1.setWidth(1000);
        table.setHeaderVisible(false);

        filteredViewer.setContentProvider(filteredContentProvider = new FilteredContentProvider());
        filteredViewer.setLabelProvider(new FilteredLabelProvider());
        filteredViewer.setInput(objectList);
        setTableItemColors(filteredViewer.getTable().getItems());
        // filteredViewer.addDoubleClickListener(doubleClickListener);
        filteredViewer.addSelectionChangedListener(selectionChangedListener);
        filteredViewer.getTable().addMouseListener(mouseAndTreeListener);
        if (INCLUDE_TEXT_DRAG_SUPPORT) {
            filteredViewer.addDragSupport(DND.DROP_COPY, new Transfer[] { TextTransfer.getInstance() }, dragSourceListener);
        }
    }

    private void setupAddControl() {
        buttonRow = new Composite(this, SWT.NONE);
        buttonRow.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        buttonRow.setLayoutData(data);

        GridLayout layout = new GridLayout(3, false);
        buttonRow.setLayout(layout);

        addPolicyButton = new Button(buttonRow, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        addPolicyButton.setText(Messages.POLICYLISTPANEL_NEW_POLICY);
        addPolicyButton.setToolTipText(Messages.POLICYLISTPANEL_NEW_POLICY);
        data = new GridData();
        data.heightHint = 20;
        addPolicyButton.setLayoutData(data);
        addPolicyButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String prefix = getNewEntityPrefix();
                if (prefix.equals("")) {
                    MessageDialog.openError(getShell(), Messages.POLICYLISTPANEL_NO_FOLDER_TITLE, Messages.POLICYLISTPANEL_NO_FOLDER_MSG);
                    return;
                }

                NewPolicyDialog dlg = new NewPolicyDialog(getShell(), Messages.POLICYLISTPANEL_POLICY_TITLE, Messages.POLICYLISTPANEL_POLICY_MSG, Messages.POLICYLISTPANEL_POLICY_NAME, getNewPolicyNameValidator());
                if (dlg.open() == Dialog.OK) {
                    String policyName = dlg.getValue();
                    String policyPurpose = dlg.getPolicyPurpose();
                    createPolicy(prefix + policyName.trim(), policyPurpose);
                }
            }
        });

        addFolderButton = new Button(buttonRow, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        addFolderButton.setText(Messages.POLICYLISTPANEL_NEW_FOLDER);
        addFolderButton.setToolTipText(Messages.POLICYLISTPANEL_NEW_FOLDER);
        data = new GridData();
        data.heightHint = 20;
        addFolderButton.setLayoutData(data);
        addFolderButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                TitleAreaDialogEx dlg = new TitleAreaDialogEx(getShell(), Messages.POLICYLISTPANEL_FOLDER_TITLE, Messages.POLICYLISTPANEL_FOLDER_MSG, Messages.POLICYLISTPANEL_FOLDER_NAME, getNewPolicyFolderNameValidator());
                if (dlg.open() == Window.OK) {
                    String prefix = getNewEntityPrefix();
                    createFolder(prefix + dlg.getValue().trim());
                }
            }
        });

        setupFilterControl(buttonRow);
    }

    public void loadPolicyInEditPanel(DomainObjectDescriptor descriptor) {
        GlobalState.getInstance().loadObjectInEditorPanel(descriptor);
    }

    protected String getNewEntityPrefix() {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        if (selection == null || selection.isEmpty()) {
            return "";
        }
        DomainObjectDescriptor descriptor = (DomainObjectDescriptor) selection.getFirstElement();
        EntityType type = descriptor.getType();
        String name = descriptor.getName();
        if (type == EntityType.FOLDER) {
            return name + PQLParser.SEPARATOR;
        }
        int index = name.lastIndexOf(PQLParser.SEPARATOR);
        if (index < 0) {
            return "";
        }
        return name.substring(0, index + 1);
    }

    protected void createPolicy(String name, String type) {
        IDPolicy policy = PolicyServerProxy.createBlankPolicy(name, type);
        GlobalState.getInstance().loadObjectInEditorPanel(policy);
        populateList();
    }

    protected void createFolder(String name) {
        PolicyServerProxy.createBlankPolicyFolder(name);
        populateList();
    }

    public void populateList() {
        EntityInfoProvider.updatePolicyTreeAsync();
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#copy()
     */
    public void copy() {
        Clipboard clipboard = new Clipboard(getDisplay());
        clipboard.setContents(new Object[] { getPQLForSelection() }, new Transfer[] { TextTransfer.getInstance() });
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#cut()
     */
    public void cut() {
        copy();
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.IClipboardEnabled#paste()
     */
    public void paste() {
    }

    /**
     * @return
     */
    private String getPQLForSelection() {
        IStructuredSelection selection;
        if (!filtering && !showingUsage) {
            selection = (IStructuredSelection) treeViewer.getSelection();
        } else {
            selection = (IStructuredSelection) filteredViewer.getSelection();
        }
        StringBuffer output = new StringBuffer();
        Iterator iter = selection.iterator();
        while (iter.hasNext()) {
            Object tmp = iter.next();
            if (!(tmp instanceof DomainObjectDescriptor)) {
                continue;
            }
            DomainObjectDescriptor desc = (DomainObjectDescriptor) tmp;
            if (desc.getType() == EntityType.POLICY) {
                IEditorPanel panel = GlobalState.getInstance().getEditorPanel();
                IDPolicy policy;
                if (panel != null) {
                    IHasId domainObj = panel.getDomainObject();
                    if (domainObj instanceof IDPolicy && domainObj.getId().equals(desc.getId())) {
                        policy = (IDPolicy) panel.getDomainObject();
                    } else {
                        policy = (IDPolicy) PolicyServerProxy.getEntityForDescriptor(desc);
                    }
                } else {
                    policy = (IDPolicy) PolicyServerProxy.getEntityForDescriptor(desc);
                }
                formatter.reset();
                formatter.formatPolicyDef(policy.getId(), policy);
                output.append(formatter.getPQL());
            }
        }
        return output.toString();
    }

    public IInputValidator getNewPolicyNameValidator() {
        return new IInputValidator() {

            public String isValid(String s) {
                String stringToCheck = s.trim();
                if (EntityInfoProvider.isValidComponentName(stringToCheck)) {
                    String existingName = EntityInfoProvider.getExistingPolicyName(getNewEntityPrefix() + stringToCheck);
                    if (existingName != null) {
                        return NLS.bind(Messages.POLICYLISTPANEL_ERROR_POLICY_EXIST, existingName);
                    } else {
                        return null;
                    }
                } else {
                    return Messages.POLICYLISTPANEL_ERROR_POLICY_INVALID;
                }
            }
        };
    }

    public IInputValidator getNewPolicyFolderNameValidator() {
        return new IInputValidator() {

            public String isValid(String s) {
                String stringToCheck = s.trim();
                if (EntityInfoProvider.isValidComponentName(stringToCheck)) {
                    String existingName = EntityInfoProvider.getExistingPolicyFolderName(getNewEntityPrefix() + stringToCheck);
                    if (existingName != null) {
                        return NLS.bind(Messages.POLICYLISTPANEL_ERROR_FOLDER_EXIST, existingName);
                    } else {
                        return null;
                    }
                }
                return Messages.POLICYLISTPANEL_ERROR_FOLDER_INVALID;
            }
        };
    }

    /**
     * Restore selection to previous known state. This is used to restore
     * selection back to the currently selected object after a drag is complete
     */
    private void restoreTreeSelection() {
        if (!restoreSelection) {
            restoreSelection = true;
            return;
        }

        Tree tree = treeViewer.getTree();
        boolean needSelection = selectedItems.size() != tree.getSelectionCount();
        // boolean needDeselection = selectedItems.size() <
        // tree.getSelectionCount();
        if (!needSelection) {
            Set<Item> set = new HashSet<Item>(selectedItems);
            TreeItem[] selection = tree.getSelection();
            for (int i = 0; i < selection.length; i++) {
                if (!set.contains(selection[i])) {
                    needSelection = true;
                    break;
                }
            }
        }
        if (needSelection) {
            // tree.deselectAll();
            boolean valid = true;
            for (Item item : selectedItems) {
                if (item.isDisposed()) {
                    valid = false;
                    break;
                }
            }
            if (!valid) {
                tree.deselectAll();
            } else {
                tree.setSelection((TreeItem[]) selectedItems.toArray(new TreeItem[selectedItems.size()]));
            }
        }
    }

    /**
     * Restore selection to previous known state. This is used to restore
     * selection back to the currently selected object after a drag is complete
     */
    private void restoreTableSelection() {
        Table table = filteredViewer.getTable();
        int[] indices = new int[selectedItems.size()];
        boolean needSelection = selectedItems.size() != table.getSelectionCount();

        for (int i = 0; i < indices.length; i++) {
            Item item = selectedItems.get(i);
            if ((item instanceof TableItem) && !item.isDisposed()) {
                int index = table.indexOf(((TableItem) selectedItems.get(i)));
                indices[i] = index;
                needSelection |= !table.isSelected(index);
            }
        }
        table.deselectAll();
        if (needSelection) {
            table.select(indices);
        }
    }

    /**
     * Setup new tree selection
     */
    private void setupNewTreeSelection() {
        memorizeTreeSelection();

        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

        handleNewSelection(selection);
    }

    /**
     * Remeber the current selectation state for later restoration
     */
    private void memorizeTreeSelection() {
        Tree tree = treeViewer.getTree();
        selectedItems.clear();
        TreeItem[] items = tree.getSelection();
        for (int i = 0; i < items.length; i++) {
            selectedItems.add(items[i]);
        }
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void setupNewTableSelection() {
        Table table = filteredViewer.getTable();
        selectedItems.clear();
        TableItem[] items = table.getSelection();
        for (int i = 0; i < items.length; i++) {
            selectedItems.add(items[i]);
        }

        IStructuredSelection selection = (IStructuredSelection) filteredViewer.getSelection();
        handleNewSelection(selection);
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void clearTreeSelection() {
        Tree tree = treeViewer.getTree();
        selectedItems.clear();
        tree.deselectAll();
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        handleNewSelection(selection);
    }

    /**
     * remember the current selection state for later restoration. Open new
     * selection in an editor if not already open.
     */
    private void clearTableSelection() {
        Table table = filteredViewer.getTable();
        selectedItems.clear();
        table.deselectAll();
        IStructuredSelection selection = (IStructuredSelection) filteredViewer.getSelection();
        handleNewSelection(selection);
    }

    /**
     * open the editor corresponding to the selected item.
     * 
     * @param selection
     */
    private void handleNewSelection(IStructuredSelection selection) {
        List<DomainObjectDescriptor> selectedItems = null;
        GlobalState gs = GlobalState.getInstance();
        if (!selection.isEmpty()) {
            selectedItems = selection.toList();

            Object selected = selectedItems.get(0);
            if (selected instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor descriptor = (DomainObjectDescriptor) selected;
                IHasId currentObject = (IHasId) gs.getCurrentObject();
                if (currentObject == null || currentObject.getId() != descriptor.getId() || !DomainObjectHelper.getEntityType(currentObject).equals(descriptor.getType())) {
                    ignoreCurrentObjectChangedEvent = true;
                    gs.loadObjectInEditorPanel(descriptor);
                    ignoreCurrentObjectChangedEvent = false;
                }
            } else {
                selectedItems = Collections.EMPTY_LIST;
            }
        } else {
            selectedItems = Collections.EMPTY_LIST;
        }

        gs.setCurrentlySelection(selectedItems);
    }

    /**
     * Determine if a context menu should be shown for the following menu item
     * 
     * @param item
     *            the item to test
     * @return true if a context menu should be shown; false otherwise
     */
    public boolean shouldShowContextMenu(TreeItem item) {
        Object data = item.getData();
        return shouldShowContextMenu(data);

    }

    /**
     * Determine if a context menu should be shown for the following menu item
     * 
     * @param item
     *            the item to test
     * @return true if a context menu should be shown; false otherwise
     */
    public boolean shouldShowContextMenu(TableItem item) {
        Object data = item.getData();
        return shouldShowContextMenu(data);
    }

    /**
     * Determine if a context menu should be shown for the menu item with the
     * specified data
     * 
     * @param data
     *            the data of the menu item to test
     * @return true if a context menu should be shown; false otherwise
     */
    private boolean shouldShowContextMenu(Object data) {
        boolean valueToReturn = false;
        if (data instanceof DomainObjectDescriptor) {
            DomainObjectDescriptor domainObjectDescriptorData = (DomainObjectDescriptor) data;
            // if (domainObjectDescriptorData.getType() != EntityType.FOLDER) {
            valueToReturn = true;
            // }
        }

        return valueToReturn;
    }

    /**
     * Display a context menu item for the menu item
     * 
     * @param me
     */
    private void showContextMenu(MouseEvent me) {
        DomainObjectDescriptor descriptor = null;
        Control control = (Control) me.getSource();
        Point l = control.toDisplay(me.x, me.y);
        Shell shell = getShell();
        Display display = shell.getDisplay();
        if (control instanceof Tree) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            descriptor = (DomainObjectDescriptor) selection.getFirstElement();
            if (descriptor.getType() == EntityType.FOLDER) {
                MenuManager menu = new MenuManager();
                menu.add(PolicyManagerActionFactory.getDeleteAction());
                menu.add(new Separator());
                menu.add(PolicyManagerActionFactory.getObjectPropertiesAction());
                Menu contextMenu = menu.createContextMenu(shell);
                contextMenu.setLocation(l.x, l.y);
                contextMenu.setVisible(true);
                while (!contextMenu.isDisposed() && contextMenu.isVisible()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
                contextMenu.dispose();

                return;
            }
        }

        IWorkbenchWindow iww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        /*
         * The following should not happen
         */
        if (iww == null) {
            LoggingUtil.logWarning(Activator.ID, "Failed to display context menu.  Active Workbench is null.", null);
            return;
        }

        MenuManager cmm = createContextMenu();
        Menu contextMenu = cmm.createContextMenu(getShell());

        contextMenu.setLocation(l.x, l.y);
        contextMenu.setVisible(true);
        while (!contextMenu.isDisposed() && contextMenu.isVisible()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        contextMenu.dispose();
    }

    private MenuManager createContextMenu() {
        MenuManager menu = new MenuManager();
        menu.add(PolicyManagerActionFactory.getShowPolicyUsageAction());
        menu.add(PolicyManagerActionFactory.getShowDeployedVersionAction());
        menu.add(PolicyManagerActionFactory.getVersionHistoryAction());
        menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getCheckDependenciesAction());
        menu.add(PolicyManagerActionFactory.getSetTargetsAction());
        menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getModifyAction());
        menu.add(PolicyManagerActionFactory.getSubmitForDeploymentAction());
        menu.add(PolicyManagerActionFactory.getScheduleDeploymentAction());
        menu.add(PolicyManagerActionFactory.getDeactivateAction());
        menu.add(PolicyManagerActionFactory.getDeleteAction());
        menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getObjectPropertiesAction());
        return menu;
    }

    private class TreeItemComparer implements IElementComparer {

        /**
         * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object,
         *      java.lang.Object)
         */
        public boolean equals(Object a, Object b) {
            boolean valueToReturn = false;

            if ((a instanceof DomainObjectDescriptor) && (b instanceof DomainObjectDescriptor)) {
                DomainObjectDescriptor elementA = (DomainObjectDescriptor) a;
                DomainObjectDescriptor elementB = (DomainObjectDescriptor) b;
                valueToReturn = elementA.getId().equals(elementB.getId());
            } else {
                /*
                 * For some strange reason which I haven't looked deep enough to
                 * determine, this method is sometimes passed ArrayList
                 * instances. Perhaps for folder? In any case, rather than
                 * spending a lot of time looking into it, simply using the
                 * default behavior
                 */
                valueToReturn = a.equals(b);
            }

            return valueToReturn;
        }

        /**
         * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
         */
        public int hashCode(Object element) {
            int valueToReturn = 0;

            if (element instanceof DomainObjectDescriptor) {
                DomainObjectDescriptor descriptorElement = (DomainObjectDescriptor) element;
                valueToReturn = descriptorElement.getId().hashCode();
            } else {
                /*
                 * For some strange reason which I haven't looked deep enough to
                 * determine, this method is sometimes passed ArrayList
                 * instances. Perhaps for folder? In any case, rather than
                 * spending a lot of time looking into it, simply using the
                 * default behavior
                 */
                valueToReturn = element.hashCode();
            }

            return valueToReturn;
        }

    }

    private final class CurrentObjectChangedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.ui.ObjectChangeListener#objectChanged()
         */
        public void onEvent(IEvent event) {
            if (!PolicyListPanel.this.ignoreCurrentObjectChangedEvent) {
                IHasId currentObject = (IHasId) GlobalState.getInstance().getCurrentObject();
                if (DomainObjectHelper.getEntityType(currentObject) == EntityType.POLICY) {
                    PolicyListPanel.this.setSelectionToEditorObject();
                }
            }
        }
    }
}
