/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/ApplicationDetailsComposite.java#1 $
 */

public class ApplicationDetailsComposite extends Composite {

    private static Map<SubjectAttribute, LeafObjectType> DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap<SubjectAttribute, LeafObjectType>();
    static {
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_ID, LeafObjectType.USER);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_ID, LeafObjectType.HOST);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.APP_ID, LeafObjectType.APPLICATION);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_LDAP_GROUP_ID, LeafObjectType.USER_GROUP);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_LDAP_GROUP_ID, LeafObjectType.HOST_GROUP);

    }
    private String[] operators1 = new String[] { " in ", " not in " };
    private IDSpec component;

    public ApplicationDetailsComposite(Composite parent, int style, IDSpec component) {
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

        displayMembers();

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

    private void displayMembers() {
        GridLayout layout;
        GridData data;
        Composite composite = new Composite(this, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);

        Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label.setFont(FontBundle.ARIAL_9_NORMAL);
        label.setText("Members:");
        data = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(data);

        CompositePredicate predicate = (CompositePredicate) ((ICompositePredicate) component.getPredicate()).predicateAt(0);
        List<CompositePredicate> filteredList = filterPredicate(predicate);
        if (!filteredList.isEmpty()) {
            Composite sub = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            sub.setLayoutData(data);
            layout = new GridLayout();
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            sub.setLayout(layout);

            getUserSubjectLabel(sub, operators1, filteredList);
        }
    }

    private void getUserSubjectLabel(Composite parent, String[] operators, List<CompositePredicate> predicates) {
        boolean firsttime = true;
        GridData data;
        for (CompositePredicate predicate : predicates) {
            String operator = getOperator(operators, predicate);
            if (PredicateHelpers.isNegationPredicate(predicate)) {
                predicate = (CompositePredicate) predicate.predicateAt(0);
            }
            Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            if (firsttime) {
                label.setText(operator.trim());
                firsttime = false;
            } else {
                label.setText(("And" + operator).trim());
            }
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);

            for (int i = 0; i < predicate.predicateCount(); i++) {
                Object predicateElement = predicate.predicateAt(i);
                if (!(predicateElement instanceof PredicateConstants)) {
                    label = new Label(parent, SWT.LEFT | SWT.WRAP);
                    label.setText(findLabel((IPredicate) predicateElement));
                    label.setFont(FontBundle.ARIAL_9_BOLD);
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    label.setLayoutData(data);
                }
            }
        }
    }

    private String getOperator(String[] operators, CompositePredicate predicate) {
        if (PredicateHelpers.isNegationPredicate(predicate)) {
            return operators[1];
        } else {
            return operators[0];
        }
    }

    private String findLabel(IPredicate spec) {
        String labelName = "";
        if (spec instanceof IDSpecRef) {
            IDSpecRef specRef = (IDSpecRef) spec;
            labelName = specRef.getReferencedName();
            labelName = labelName.substring(labelName.indexOf(PQLParser.SEPARATOR) + 1);
        } else if (spec instanceof Relation) {
            IExpression lhs = ((Relation) spec).getLHS();
            IExpression rhs = ((Relation) spec).getRHS();
            Object rhsValue = rhs.evaluate(null).getValue();
            if (lhs instanceof IDSubjectAttribute) {
                if (DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.containsKey(lhs) && rhsValue instanceof Long) {
                    LeafObjectType associatedLeafObjectType = (LeafObjectType) DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.get(lhs);
                    LeafObject leafObject = EntityInfoProvider.getLeafObjectByID((Long) rhsValue, associatedLeafObjectType);
                    if (leafObject != null) {
                        labelName = leafObject.getName();
                    } else {
                        labelName = "Entry Not Found - " + rhsValue;
                    }
                } else {
                    // Done for consistency with earlier implement to reduce
                    // risk
                    labelName = "" + rhsValue;
                }
            } else if (lhs instanceof ResourceAttribute && rhs instanceof Constant) { /* PATH */
                String val = ((Constant) rhs).getRepresentation();
                String res = val.replaceAll("[\\\\]+", "\\\\");
                labelName = val.startsWith("\\\\") ? "\\" + res : res;
            } else {
                // Catch all
                if (rhs instanceof Constant) {
                    labelName = ((Constant) rhs).getRepresentation();
                } else {
                    labelName = "" + rhsValue;
                }
            }
        }

        return labelName;
    }

    private List<CompositePredicate> filterPredicate(CompositePredicate predicate) {
        List<CompositePredicate> filteredPredicate = new ArrayList<CompositePredicate>();
        for (int i = 0; i < predicate.predicateCount(); i++) {
            IPredicate spec = (IPredicate) predicate.predicateAt(i);
            if (spec instanceof CompositePredicate) {
                filteredPredicate.add((CompositePredicate) spec);
            }
        }
        return filteredPredicate;
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
        labelPolicyImage.setImage(ImageBundle.APPLICATION_COMPONENT_IMG);
        data = new GridData(GridData.BEGINNING);
        labelPolicyImage.setLayoutData(data);

        Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelPolicyName.setFont(FontBundle.ARIAL_9_BOLD);
        labelPolicyName.setText(getCompnentName() + ", Application Component");
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelPolicyName.setLayoutData(data);
    }

    private String getCompnentName() {
        return DomainObjectHelper.getDisplayName(component);
    }
}
