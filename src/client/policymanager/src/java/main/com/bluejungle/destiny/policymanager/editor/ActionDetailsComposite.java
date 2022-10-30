/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/ActionDetailsComposite.java#4 $
 */

public class ActionDetailsComposite extends Composite {

    private static Map<IDAction, String> actionsMap = new HashMap<IDAction, String>();
    static {
        actionsMap.put(DAction.OPEN, EditorMessages.ACTIONCOMPONENTEDITOR_OPEN);
        actionsMap.put(DAction.DELETE, EditorMessages.ACTIONCOMPONENTEDITOR_DELETE);
        actionsMap.put(DAction.MOVE, EditorMessages.ACTIONCOMPONENTEDITOR_MOVE);
        actionsMap.put(DAction.CHANGE_PROPERTIES, EditorMessages.ACTIONCOMPONENTEDITOR_CHANGE_ATTRIBUTES);
        actionsMap.put(DAction.CHANGE_SECURITY, EditorMessages.ACTIONCOMPONENTEDITOR_CHANGE_FILE_PERMISSIONS);
        actionsMap.put(DAction.RENAME, EditorMessages.ACTIONCOMPONENTEDITOR_RENAME);
        actionsMap.put(DAction.RUN, EditorMessages.ACTIONCOMPONENTEDITOR_RUN);

        actionsMap.put(DAction.CREATE_NEW, EditorMessages.ACTIONCOMPONENTEDITOR_CREATE);
        actionsMap.put(DAction.EDIT, EditorMessages.ACTIONCOMPONENTEDITOR_EDIT);
        actionsMap.put(DAction.COPY, EditorMessages.ACTIONCOMPONENTEDITOR_COPY);
        actionsMap.put(DAction.COPY_PASTE, EditorMessages.ACTIONCOMPONENTEDITOR_PASTE);

        actionsMap.put(DAction.PRINT, EditorMessages.ACTIONCOMPONENTEDITOR_PRINT);
        actionsMap.put(DAction.EXPORT, EditorMessages.ACTIONCOMPONENTEDITOR_EXPORT);
        actionsMap.put(DAction.ATTACH, EditorMessages.ACTIONCOMPONENTEDITOR_ATTACH_TO_ITEM);
        actionsMap.put(DAction.EMAIL, EditorMessages.ACTIONCOMPONENTEDITOR_EMAIL);
        actionsMap.put(DAction.IM, EditorMessages.ACTIONCOMPONENTEDITOR_INSTANT_MESSAGE);
        actionsMap.put(DAction.MEETING, EditorMessages.ACTIONCOMPONENTEDITOR_MEET);
        actionsMap.put(DAction.AVD, EditorMessages.ACTIONCOMPONENTEDITOR_CALL);
    }

    private IDSpec component;

    public ActionDetailsComposite(Composite parent, int style, IDSpec component) {
        super(parent, style);
        this.component = component;

        initialize();
    }

    private void initialize() {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);

        Label labelDetails = new Label(this, SWT.LEFT | SWT.WRAP);
        labelDetails.setFont(FontBundle.TWELVE_POINT_ARIAL);
        labelDetails.setText("Details");
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        labelDetails.setLayoutData(data);

        displayComponentName();

        new Label(this, SWT.NONE);

        displayActions();

        new Label(this, SWT.NONE);

        displayDescription();

        setBackgroud(this);
    }

    private void displayDescription() {
        String description = component.getDescription();

        if (description != null && description.length() != 0) {
            GridData data;
            Label label = new Label(this, SWT.LEFT | SWT.WRAP);
            label.setText("Description: " + description);
            label.setFont(FontBundle.ARIAL_9_NORMAL);
            data = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);
        }
    }

    private void displayActions() {
        GridLayout layout;
        GridData data;
        Label label = new Label(this, SWT.LEFT | SWT.WRAP);
        label.setText("Basic Action(s): ");
        label.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(data);

        Composite composite = new Composite(this, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Set<IDAction> actionSet = PredicateHelpers.getActionSet((CompositePredicate) component.getPredicate());
        for (IDAction action : actionSet) {
            String text = actionsMap.get(action);
            if (text != null) {
                label = new Label(composite, SWT.LEFT | SWT.WRAP);
                label.setText(text);
                label.setFont(FontBundle.ARIAL_9_BOLD);
                data = new GridData(GridData.FILL_HORIZONTAL);
                label.setLayoutData(data);
            }
        }
    }

    private void setBackgroud(Control parent) {
        parent.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        if (parent instanceof Composite) {

            for (Control control : ((Composite) parent).getChildren()) {
                setBackgroud(control);
            }
        }
    }

    private void displayComponentName() {
        GridLayout layout;
        GridData data;
        Composite composite = new Composite(this, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);

        Label labelPolicyImage = new Label(composite, SWT.NONE);
        labelPolicyImage.setImage(ImageBundle.ACTION_COMPONENT_IMG);
        data = new GridData(GridData.BEGINNING);
        labelPolicyImage.setLayoutData(data);

        Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelPolicyName.setFont(FontBundle.ARIAL_9_BOLD);
        labelPolicyName.setText(getCompnentName() + ", Action Component");
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelPolicyName.setLayoutData(data);
    }

    private String getCompnentName() {
        return DomainObjectHelper.getDisplayName(component);
    }
}
