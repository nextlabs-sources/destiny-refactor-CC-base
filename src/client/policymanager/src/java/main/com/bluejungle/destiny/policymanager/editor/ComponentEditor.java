/*
 * Created on Mar 16, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.ui.controls.PropertyExpressionControl;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.deployment.AgentAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/ComponentEditor.java#1 $:
 */

public abstract class ComponentEditor extends EditorPanel implements IEditorPanel {

    protected static final Point SIZE_MEMBER_LABEL = new Point(100, 20);

    protected static final int CONTROL_ID_COMPOSITION = 0;
    protected static final int CONTROL_ID_PROPERTIES = 1;

    protected Composite membersComposite = null;
    protected Composite propertiesComposite = null;

    protected Label propertiesNameLabel = null;

    protected CompositionControl compControl = null;
    protected boolean showPropertyExpressions;
    protected PropertyExpressionControl propertyExpressionControl = null;

    /**
     * Constructor Note that objects must be initialized separately by calling
     * initialize() before use
     * 
     * @param parent
     * @param style
     */
    public ComponentEditor(Composite parent, int style, IDSpec domainObject, boolean showPropertyExpressions) {
        super(parent, style, domainObject);
        this.showPropertyExpressions = showPropertyExpressions;
    }

    public void initializeContents() {
        membersComposite = addSectionComposite();
        initializeMembers();
        if (showPropertyExpressions) {
            propertiesComposite = addSectionComposite();
            initializePropertyExpressions();
        }
    }

    protected void initializeMembers() {
        FormLayout layout = new FormLayout();
        membersComposite.setLayout(layout);

        Composite membersLabel = initializeSectionHeading(membersComposite, EditorMessages.COMPONENTEDITOR_MEMBERS);
        FormData data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        membersLabel.setLayoutData(data);

        compControl = new CompositionControl(membersComposite, SWT.NONE, getObjectTypeLabelText(), getMemberLabel(), getControlDomainObject(CONTROL_ID_COMPOSITION, domainObject), this, CONTROL_ID_COMPOSITION, isEditable(), true, getSpecType(),
                getComponentType(), getLookupLabel());
        compControl.setBackground(getBackground());
        data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(membersLabel, SPACING);
        compControl.setLayoutData(data);
    }

    protected void initializePropertyExpressions() {
        FormLayout layout = new FormLayout();
        propertiesComposite.setLayout(layout);

        Composite propertiesLabel = initializeSectionHeading(propertiesComposite, EditorMessages.COMPONENTEDITOR_WITH_PROPERTIES);
        FormData data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        propertiesLabel.setLayoutData(data);

        propertiesNameLabel = new Label(propertiesComposite, SWT.NONE);
        propertiesNameLabel.setText(EditorMessages.COMPONENTEDITOR_PROPERTY_NAME);
        propertiesNameLabel.setBackground(getBackground());
        data = new FormData();
        data.left = new FormAttachment(0, 35);
        data.top = new FormAttachment(propertiesLabel, SPACING);
        propertiesNameLabel.setLayoutData(data);

        propertyExpressionControl = new PropertyExpressionControl(propertiesComposite, SWT.NONE, getControlDomainObject(CONTROL_ID_PROPERTIES, domainObject), this, getEntityType(), CONTROL_ID_PROPERTIES, isEditable(), hasCustomProperties());
        propertyExpressionControl.setBackground(getBackground());
        data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(propertiesNameLabel, SPACING);
        propertyExpressionControl.setLayoutData(data);
    }

    public CompositePredicate getControlDomainObject(int controlId, IHasId domainObject) {
        switch (controlId) {
        case CONTROL_ID_COMPOSITION:
            return (CompositePredicate) ((ICompositePredicate) ((IDSpec) domainObject).getPredicate()).predicateAt(0);
        case CONTROL_ID_PROPERTIES:
            IPredicate predicate = ((ICompositePredicate) ((IDSpec) domainObject).getPredicate()).predicateAt(1);
            if (predicate instanceof PredicateConstants) {
                CompositePredicate result = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
                result.addPredicate(PredicateConstants.TRUE);
                result.addPredicate(PredicateConstants.TRUE);
                return result;
            }
            return (CompositePredicate) ((ICompositePredicate) ((IDSpec) domainObject).getPredicate()).predicateAt(1);
        }
        return null;
    }

    /**
     * @return
     */
    protected abstract List<String> getPropertyOperatorList();

    /**
     * @return
     */
    protected abstract List<String> getPropertyList();

    public void relayout() {
        if (!canRelayout()) {
            return;
        }

        super.relayout();
        membersComposite.redraw();
        if (showPropertyExpressions) {
            propertiesComposite.redraw();
            propertiesComposite.layout(true);
        }
    }

    public void relayoutContents() {
        relayoutMembers();
        relayoutProperties();
    }

    protected void relayoutMembers() {
        compControl.relayout();
    }

    protected void relayoutProperties() {
        if (propertyExpressionControl != null) {
            propertyExpressionControl.relayout();
        }
    }

    public String getDescription() {
        return ((IDSpec) domainObject).getDescription();
    }

    public String getObjectName() {
        return DomainObjectHelper.getDisplayName(domainObject);
    }

    public void setDescription(String description) {
        ((IDSpec) domainObject).setDescription(description);
    }

    protected abstract SpecType getSpecType();

    protected abstract String getMemberLabel();

    protected abstract ComponentEnum getComponentType();

    protected String getLookupLabel() {
        return null;
    }
}
