/*
 * Created on Mar 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.editor.IEditorPanel;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElement;
import com.bluejungle.destiny.policymanager.ui.PropertyExpressionUndoElementOp;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class PropertyExpressionControl extends Composite {

    private final String ALL_USERS = "All Users";
    private final String OWNER_USER_COMPONENT = "Owner User Component";
    private IEditorPanel editorPanel = null;
    private List<Combo> propControlArray = new ArrayList<Combo>();
    private List<Combo> opControlArray = new ArrayList<Combo>();
    private List<Widget> valueControlArray = new ArrayList<Widget>();
    private List<Button> removeButtonArray = new ArrayList<Button>();
    private List<Control> tabListArray = new ArrayList<Control>();
    private List<Label> propNameLabelArray = new ArrayList<Label>();
    private List<Label> propOpLabelArray = new ArrayList<Label>();
    private List<Label> propValueArray = new ArrayList<Label>();

    private EntityType entityType;

    private boolean editable = true;
    private boolean hasCustomProperties;
    private int controlId;

    private Button addButton = null;

    private CompositePredicate domainObject = null;

    private static final int CONTROL_HEIGHT = 20;
    private static final int VALUE_TEXT_WIDTH = 300;
    private static final int DATE_PICKER_WIDTH = 150;
    private static final int REMOVE_BUTTON_WIDTH = 20;

    private static final Map<EntityType, Map<SpecAttribute, String>> attributeNameMaps = new HashMap<EntityType, Map<SpecAttribute, String>>();
    private static final Map<EntityType, Map<String, SpecAttribute>> nameAttributeMaps = new HashMap<EntityType, Map<String, SpecAttribute>>();
    private static final Map<EntityType, Map<SpecAttribute, AttributeType>> attributeTypeMaps = new HashMap<EntityType, Map<SpecAttribute, AttributeType>>();
    private static final Map<EntityType, Set<String>> attributeLists = new HashMap<EntityType, Set<String>>();
    private static final Constant CONST_ZERO = Constant.build(0);

    private static final EntityType[] entityTypes = { EntityType.USER, EntityType.HOST, EntityType.RESOURCE, EntityType.PORTAL, EntityType.APPLICATION };
    private static final Object[] attributeTypes = { SubjectType.USER, SubjectType.HOST, ResourceAttribute.FILE_SYSTEM_SUBTYPE, ResourceAttribute.PORTAL_SUBTYPE, SubjectType.APP };

    private static class OperatorInfo {

        private Map<RelationOp, String> operatorNameMap = new HashMap<RelationOp, String>();
        private Map<String, RelationOp> nameOperatorMap = new HashMap<String, RelationOp>();
        private List<String> opList = new ArrayList<String>();

        private void storeOperatorInfo(RelationOp op, String name) {
            operatorNameMap.put(op, name);
            nameOperatorMap.put(name, op);
            opList.add(name);
        }

        public RelationOp getOperatorForName(String name) {
            return (RelationOp) nameOperatorMap.get(name);
        }

        public String getNameForOperator(RelationOp op) {
            return (String) operatorNameMap.get(op);
        }

        public List<String> getOperators() {
            return opList;
        }
    }

    private static final OperatorInfo dateOperators = new OperatorInfo();
    private static final OperatorInfo stringOperators = new OperatorInfo();
    private static final OperatorInfo numberOperators = new OperatorInfo();
    private static final OperatorInfo yesNoStates = new OperatorInfo();
    private final PredicateModifiedListener predicateModifiedListner = new PredicateModifiedListener();

    static {

        for (int i = 0; i < entityTypes.length; i++) {
            Collection<AttributeDescriptor> attributes = PolicyServerProxy.getAttributes(entityTypes[i]);
            for (AttributeDescriptor attr : attributes) {
                if (attributeTypes[i] instanceof String) {
                    storeAttributeInfo(ResourceAttribute.forNameAndType(attr.getPqlName(), (String) attributeTypes[i]), attr.getDisplayName(), entityTypes[i], attr.getType());
                } else {
                    storeAttributeInfo(SubjectAttribute.forNameAndType(attr.getPqlName(), (SubjectType) attributeTypes[i]), attr.getDisplayName(), entityTypes[i], attr.getType());
                }
            }
        }
        stringOperators.storeOperatorInfo(RelationOp.EQUALS, "is");
        stringOperators.storeOperatorInfo(RelationOp.NOT_EQUALS, "is not");

        dateOperators.storeOperatorInfo(RelationOp.GREATER_THAN_EQUALS, "on or after");
        dateOperators.storeOperatorInfo(RelationOp.LESS_THAN, "before");

        numberOperators.storeOperatorInfo(RelationOp.EQUALS, "=");
        numberOperators.storeOperatorInfo(RelationOp.GREATER_THAN, ">");
        numberOperators.storeOperatorInfo(RelationOp.GREATER_THAN_EQUALS, ">=");
        numberOperators.storeOperatorInfo(RelationOp.LESS_THAN, "<");
        numberOperators.storeOperatorInfo(RelationOp.LESS_THAN_EQUALS, "<=");
        numberOperators.storeOperatorInfo(RelationOp.NOT_EQUALS, "!=");

        // The operand in boolean expressions is hidden, and is set to zero.
        // Therefore, x>0 means "Yes", while x==0 means "No".
        yesNoStates.storeOperatorInfo(RelationOp.GREATER_THAN, "Yes");
        yesNoStates.storeOperatorInfo(RelationOp.EQUALS, "No");
    }

    private static void storeAttributeInfo(SpecAttribute attribute, String name, EntityType entityType, AttributeType type) {
        Map<SpecAttribute, String> attributeNameMap = attributeNameMaps.get(entityType);
        if (attributeNameMap == null) {
            attributeNameMap = new HashMap<SpecAttribute, String>();
            attributeNameMaps.put(entityType, attributeNameMap);
        }
        Map<String, SpecAttribute> nameAttributeMap = nameAttributeMaps.get(entityType);
        if (nameAttributeMap == null) {
            nameAttributeMap = new HashMap<String, SpecAttribute>();
            nameAttributeMaps.put(entityType, nameAttributeMap);
        }
        Map<SpecAttribute, AttributeType> attributeTypeMap = attributeTypeMaps.get(entityType);
        if (attributeTypeMap == null) {
            attributeTypeMap = new HashMap<SpecAttribute, AttributeType>();
            attributeTypeMaps.put(entityType, attributeTypeMap);
        }

        attributeNameMap.put(attribute, name);
        nameAttributeMap.put(name, attribute);
        attributeTypeMap.put(attribute, type);

        Set<String> attrList = attributeLists.get(entityType);
        if (attrList == null) {
            attrList = new TreeSet<String>();
            attributeLists.put(entityType, attrList);
        }
        attrList.add(name);
    }

    private static Set<String> getAttributeList(EntityType entityType) {
        return attributeLists.get(entityType);
    }

    private static SpecAttribute getAttributeForName(String name, EntityType entityType) {
        synchronized (nameAttributeMaps) {
            SpecAttribute r = nameAttributeMaps.get(entityType).get(name);
            if (r == null) {
                String subtype;
                if (entityType == EntityType.RESOURCE) {
                    subtype = ResourceAttribute.FILE_SYSTEM_SUBTYPE;
                } else if (entityType == EntityType.PORTAL) {
                    subtype = ResourceAttribute.PORTAL_SUBTYPE;
                } else {
                    return null;
                }
                r = ResourceAttribute.forNameAndType(name, subtype, false);
                nameAttributeMaps.get(entityType).put(name, r);
                attributeNameMaps.get(entityType).put(r, name);
            }
            return r;
        }
    }

    private static String getNameForAttribute(SpecAttribute attribute, EntityType entityType) {
        String result = attributeNameMaps.get(entityType).get(attribute);
        if (result == null)
            return attribute.getName();
        return result;
    }

    private static AttributeType getTypeForAttribute(SpecAttribute attribute, EntityType entityType) {
        return attributeTypeMaps.get(entityType).get(attribute);
    }

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public PropertyExpressionControl(Composite parent, int style, CompositePredicate domainObj, IEditorPanel editorPanel, EntityType entityType, int controlId, boolean editable, boolean hasCustomProperties) {
        super(parent, style);
        this.domainObject = domainObj;
        this.controlId = controlId;
        this.hasCustomProperties = hasCustomProperties;
        setEditable(editable);

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        final IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.registerListener(predicateModifiedListner, ContextualEventType.PREDICATE_MODIFIED_EVENT, domainObject);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                eventManager.unregisterListener(PropertyExpressionControl.this.predicateModifiedListner, ContextualEventType.PREDICATE_MODIFIED_EVENT, PropertyExpressionControl.this.domainObject);
            }
        });

        this.editorPanel = editorPanel;
        this.entityType = entityType;

        this.entityType = entityType;

        initialize();
        relayout();
    }

    /*
     * AW Note: refresh disposes UI widget and recreate them. If you call this
     * from a widget event handler, call refreshLater instead of refresh. See
     * bug 585
     */
    protected void refreshLater() {
        getDisplay().asyncExec(new Runnable() {

            public void run() {
                refresh();
            }
        });
    }

    /**
     * 
     */
    protected void refresh() {
        disposeWidgetArray(propControlArray);
        disposeWidgetArray(opControlArray);
        disposeWidgetArray(valueControlArray);
        disposeWidgetArray(removeButtonArray);
        disposeWidgetArray(propNameLabelArray);
        disposeWidgetArray(propOpLabelArray);
        disposeWidgetArray(propValueArray);

        propControlArray.clear();
        opControlArray.clear();
        valueControlArray.clear();
        removeButtonArray.clear();
        propNameLabelArray.clear();
        propOpLabelArray.clear();
        propValueArray.clear();
        tabListArray.clear();

        initialize();
        relayoutParent();
    }

    private void disposeWidgetArray(List<? extends Widget> list) {
        for (Widget widget : list) {
            widget.dispose();
        }
    }

    public void initialize() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);

        for (int i = 0; i < domainObject.predicateCount(); i++) {
            IPredicate spec = (IPredicate) domainObject.predicateAt(i);

            if (spec instanceof Relation) {
                eventManager.registerListener(predicateModifiedListner, ContextualEventType.PREDICATE_MODIFIED_EVENT, spec);
                addExpressionControls((Relation) spec);
            }
        }
        if (addButton == null && isEditable()) {
            addAddButton();
        }
    }

    /**
     * 
     */
    private void addAddButton() {
        addButton = new Button(this, SWT.FLAT | SWT.CENTER);
        addButton.setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD);
        addButton.setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_ADD_CONDITION);
        addButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // FIXME: create the right type of object and guard it properly
                SpecAttribute attribute = getAttributeForName(getAttributeList(getEntityType()).iterator().next(), getEntityType());
                OperatorInfo opers = getOperatorsForAttribute(attribute);
                Relation prop = new Relation(opers.getOperatorForName(opers.getOperators().get(0).toString()), attribute, Constant.build(""));
                if (isDateAttribute(attribute)) {
                    prop.setRHS(getDefaultDateExpression());
                } else if (isBooleanAttribute(attribute)) {
                    prop.setRHS(CONST_ZERO);
                }
                PredicateHelpers.addPredicate(domainObject, prop);
                addExpressionControls(prop);
                addUndoElement(PropertyExpressionUndoElementOp.ADD, domainObject.predicateCount() - 1, prop, null);
                relayoutParent();
            }
        });

    }

    /**
     * @param spec
     */
    private void addExpressionControls(Relation spec) {
        final IExpression lhs = spec.getLHS();
        OperatorInfo operatorInfo = getOperatorsForAttribute(lhs);
        if (isEditable()) {
            Button removeButton = new Button(this, SWT.FLAT | SWT.CENTER);
            removeButton.setText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE);
            removeButton.setToolTipText(ApplicationMessages.PROPERTYEXPRESSIONCONTROL_REMOVE_CONDITION);
            removeButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            removeButtonArray.add(removeButton);
            removeButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    Button b = (Button) e.getSource();
                    int index = removeButtonArray.indexOf(b);
                    removeExpressionControls(index);
                    IPredicate oldValue = PredicateHelpers.removePredicateAt(domainObject, index);
                    addUndoElement(PropertyExpressionUndoElementOp.REMOVE, index, null, oldValue);
                    relayoutParent();
                }
            });

            Combo combo;
            if (hasCustomProperties) {
                combo = new Combo(this, SWT.SINGLE | SWT.BORDER);
                combo.setTextLimit(128);
            } else
                combo = new Combo(this, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            propControlArray.add(combo);

            Set<String> attributeList = getAttributeList(getEntityType());
            List<String> sortedAttributeList = new ArrayList<String>(attributeList);
            Collections.sort(sortedAttributeList, String.CASE_INSENSITIVE_ORDER);
            for (String attribute : sortedAttributeList) {
                combo.add(attribute);
            }

            combo.setText(getNameForAttribute((SpecAttribute) lhs, getEntityType()));
            combo.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = propControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                    //
                    SpecAttribute newValue = getAttributeForName(newText, getEntityType());
                    Object oldValue = spec.getLHS();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE, index, newValue, oldValue);
                        IExpression newRHS;
                        RelationOp newOp;
                        if (isNumberAttribute(newValue)) {
                            newRHS = newValue.build("0");
                            newOp = RelationOp.EQUALS;
                        } else if (isDateAttribute(newValue)) {
                            newRHS = getDefaultDateExpression();
                            newOp = RelationOp.GREATER_THAN_EQUALS;
                        } else if (isBooleanAttribute(newValue)) {
                            newRHS = CONST_ZERO;
                            newOp = RelationOp.EQUALS;
                        } else if (newText.equals(OWNER_USER_COMPONENT)) {
                            newRHS = Constant.build(ComponentEnum.USER.toString() + PQLParser.SEPARATOR + ALL_USERS);
                            newOp = RelationOp.EQUALS;
                        } else {
                            newRHS = newValue.build("");
                            newOp = RelationOp.EQUALS;
                        }

                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE, index, newRHS, spec.getRHS(), true);
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_OP, index, newOp, spec.getOp(), true);
                        spec.setLHS(newValue);
                        spec.setOp(newOp);
                        spec.setRHS(newRHS);
                        refreshLater();
                    }
                }
            });
            combo.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = propControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                    //
                    SpecAttribute newValue = getAttributeForName(newText, getEntityType());
                    Object oldValue = spec.getLHS();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE, index, newValue, oldValue);
                        IExpression newRHS;
                        RelationOp newOp;
                        if (isNumberAttribute(newValue)) {
                            newRHS = newValue.build("0");
                            newOp = RelationOp.EQUALS;
                        } else if (isDateAttribute(newValue)) {
                            newRHS = getDefaultDateExpression();
                            newOp = RelationOp.GREATER_THAN_EQUALS;
                        } else if (isBooleanAttribute(newValue)) {
                            newRHS = CONST_ZERO;
                            newOp = RelationOp.EQUALS;
                        } else if (newText.equals(OWNER_USER_COMPONENT)) {
                            newRHS = Constant.build(ComponentEnum.USER.toString() + PQLParser.SEPARATOR + ALL_USERS);
                            newOp = RelationOp.EQUALS;
                        } else {
                            newRHS = newValue.build("");
                            newOp = RelationOp.EQUALS;
                        }

                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE, index, newRHS, spec.getRHS(), true);
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_OP, index, newOp, spec.getOp(), true);
                        spec.setLHS(newValue);
                        spec.setOp(newOp);
                        spec.setRHS(newRHS);
                        refreshLater();
                    }
                }
            });

            // combo.addModifyListener(new ModifyListener() {
            //
            // public void modifyText(ModifyEvent e) {
            // Combo source = (Combo) e.getSource();
            // int index = propControlArray.indexOf(source);
            // String newText = source.getText();
            // Relation spec = (Relation)
            // PredicateHelpers.getPredicateAt(domainObject, index);
            // //
            // SpecAttribute newValue = getAttributeForName(newText,
            // getEntityType());
            // Object oldValue = spec.getLHS();
            // if (!oldValue.equals(newValue)) {
            // addUndoElement(PropertyExpressionUndoElementOp.CHANGE_ATTRIBUTE,
            // index, newValue, oldValue);
            // IExpression newRHS;
            // RelationOp newOp;
            // if (isNumberAttribute(newValue)) {
            // newRHS = newValue.build("0");
            // newOp = RelationOp.EQUALS;
            // } else if (isDateAttribute(newValue)) {
            // newRHS = getDefaultDateExpression();
            // newOp = RelationOp.GREATER_THAN_EQUALS;
            // } else if (isBooleanAttribute(newValue)) {
            // newRHS = CONST_ZERO;
            // newOp = RelationOp.EQUALS;
            // } else {
            // newRHS = newValue.build("");
            // newOp = RelationOp.EQUALS;
            // }
            //
            // addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE,
            // index, newRHS, spec.getRHS(), true);
            // addUndoElement(PropertyExpressionUndoElementOp.CHANGE_OP, index,
            // newOp, spec.getOp(), true);
            // spec.setLHS(newValue);
            // spec.setOp(newOp);
            // spec.setRHS(newRHS);
            // refreshLater();
            // }
            // }
            // });

            combo = new Combo(this, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            for (int i = 0; i < operatorInfo.getOperators().size(); i++) {
                combo.add((String) operatorInfo.getOperators().get(i));
            }
            combo.setText(operatorInfo.getNameForOperator(spec.getOp()));

            opControlArray.add(combo);

            combo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    Combo source = (Combo) e.getSource();
                    int index = opControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                    RelationOp newValue = getOperatorsForAttribute(spec.getLHS()).getOperatorForName(newText);
                    Object oldValue = spec.getOp();
                    if (!oldValue.equals(newValue)) {
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_OP, index, newValue, oldValue);
                        spec.setOp(newValue);
                    }
                }
            });

            if (isDateAttribute(lhs)) {
                addDateEditor(spec.getRHS());
            } else if (lhs == ResourceAttribute.OWNER_GROUP) {
                addComponentNameEditor(ComponentEnum.USER, stringFromExpression(spec.getRHS()));
            } else {
                addStringEditor(stringFromExpression(spec.getRHS()), !isBooleanAttribute(lhs));
            }

        } else { // if not editable
            Label propName = new Label(this, SWT.NONE);
            propName.setEnabled(false);
            if (lhs instanceof SpecAttribute) {
                propName.setText(getNameForAttribute((SpecAttribute) lhs, getEntityType()));
            } else {
                // we really shouldn't get here, but just in case a new
                // type of property comes along, display something:
                propName.setText(lhs.evaluate(null).getValue().toString());
            }
            propName.setBackground(ColorBundle.LIGHT_GRAY);
            propNameLabelArray.add(propName);

            Label propOp = new Label(this, SWT.NONE);
            propOp.setEnabled(false);
            propOp.setText(operatorInfo.getNameForOperator(spec.getOp()));
            propOp.setBackground(ColorBundle.LIGHT_GRAY);
            propOpLabelArray.add(propOp);

            Label propValue = new Label(this, SWT.NONE);
            propValue.setEnabled(false);
            if (isDateAttribute(lhs)) {
                Long dateValue = (Long) ((Constant) spec.getRHS()).getValue().getValue();
                if (dateValue != null) {
                    Date d = new Date(dateValue.longValue());
                    propValue.setText(SimpleDateFormat.getDateInstance().format(d));
                }
            } else if (lhs == ResourceAttribute.OWNER_GROUP) {
                String text = stringFromExpression(spec.getRHS());
                if (text.startsWith(ComponentEnum.USER.toString() + PQLParser.SEPARATOR)) {
                    text = text.substring(5);
                }
                propValue.setText(text);
            } else if (!isBooleanAttribute(lhs)) {
                propValue.setText(stringFromExpression(spec.getRHS()));
            }
            propValue.setBackground(ColorBundle.LIGHT_GRAY);
            propValueArray.add(propValue);
        }
    }

    private String stringFromExpression(IExpression exp) {
        final String[] res = new String[] { "" };
        if (exp != null) {
            exp.acceptVisitor(new IExpressionVisitor() {

                public void visit(IAttribute attribute) {
                    // we should not get here
                }

                public void visit(Constant constant) {
                    res[0] = constant.getRepresentation();
                }

                public void visit(IExpressionReference ref) {
                    if (ref.isReferenceByName()) {
                        res[0] = ref.getReferencedName();
                    } else {
                        res[0] = ref.getPrintableReference();
                    }
                }

                public void visit(IExpression expression) {
                    // we should not get here
                }
            }, IExpressionVisitor.PREORDER);
        }

        return res[0].replaceAll("/+$", "");
    }

    private void addStringEditor(String text, boolean visible) {
        Text t = new Text(this, SWT.SINGLE | SWT.BORDER);
        t.setTextLimit(128);
        t.setText(text);
        valueControlArray.add(t);
        if (visible) {
            t.addFocusListener(new FocusAdapter() {

                public void focusLost(FocusEvent e) {
                    Text source = (Text) e.getSource();
                    int index = valueControlArray.indexOf(source);
                    String newText = source.getText();
                    Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                    Constant newValue = Constant.build(newText);
                    IExpression oldValue = spec.getRHS();
                    if (!((oldValue instanceof Constant) && ((Constant) oldValue).getValue().getValue().toString().equals(newValue.getValue().getValue().toString()))) {
                        addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE, index, newValue, oldValue);
                        spec.setRHS(newValue);
                    }
                }
            });
        } else {
            t.setVisible(false);
        }
    }

    private void addComponentNameEditor(final ComponentEnum type, String text) {
        Text t = new Text(this, SWT.SINGLE | SWT.BORDER);
        if (text.startsWith(ComponentEnum.USER.toString() + PQLParser.SEPARATOR)) {
            text = text.substring(5);
        }
        t.setText(text);
        valueControlArray.add(t);
        t.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                Text source = (Text) e.getSource();
                int index = valueControlArray.indexOf(source);
                String newText = source.getText();
                if (newText.length() == 0) {
                    newText = ALL_USERS;
                    source.setText(newText);
                }
                Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                Constant newValue = Constant.build(newText);
                Constant newUserValue = Constant.build(ComponentEnum.USER.toString() + PQLParser.SEPARATOR + newText);
                IExpression oldValue = spec.getRHS();
                if (!((oldValue instanceof Constant) && ((Constant) oldValue).getValue().getValue().toString().equals(newValue.getValue().getValue().toString()))) {
                    String name = newValue.getValue().getValue().toString();
                    if (isNewComponentName(type, name)) {
                        if (!displayCreateComponentDialog(type, name)) {
                            newText = ALL_USERS;
                            source.setText(newText);
                            newUserValue = Constant.build(ComponentEnum.USER.toString() + PQLParser.SEPARATOR + newText);
                        }
                    } else {
                        //the user may type a name that is different case, 
                        // since we do case-insensitive name in other places. 
                        // In order not to break the user experience, we just 
                        // change the case to match the server one
                        if(EntityInfoProvider.isValidComponentName(name)){
                            String existingName = EntityInfoProvider.getExistingComponentName(name, type);
                            if(existingName != null){
                                //remove the type and the PQLSeperator
                                existingName = existingName.substring(type.toString().length() + 1);
                                
                                //reset new name
                                source.setText(existingName);
                                newValue = Constant.build(existingName);
                                newUserValue = Constant.build(ComponentEnum.USER
                                    .toString()
                                + PQLParser.SEPARATOR + existingName);
                            }
                        }
                    }
                    addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE, index, newValue, oldValue);
                    spec.setRHS(newUserValue);
                }
            }
        });
    }

    private boolean isNewComponentName(ComponentEnum type, String name) {
        return EntityInfoProvider.isValidComponentName(name) && (EntityInfoProvider.getExistingComponentName(name, type) == null) && PolicyServerProxy.getAllowedEntityTypes().contains(getEntityType());
    }

    /**
     * 
     * @param name
     * @param entityType
     * @return true if a new entity was created, false otherwise
     */
    private boolean displayCreateComponentDialog(ComponentEnum type, String name) {
        StringBuffer msg = new StringBuffer();
        msg.append("There is currently no component with the name '");
        msg.append(name);
        msg.append("'. Would you like to create a new component?");
        if (MessageDialog.openConfirm(getDisplay().getActiveShell(), "Create a New Component?", msg.toString())) {
            PolicyServerProxy.createBlankComponent(name, type);
            GlobalState.getInstance().getComponentListPanel(type).populateList();
            return true;
        } else {
            return false;
        }
    }

    private void addDateEditor(IExpression exp) {
        DatePickerCombo dp = new DatePickerCombo(this, SWT.BORDER);
        String propValue = ((Constant) exp).getValue().getValue().toString();
        Calendar d = new GregorianCalendar();
        if ("".equals(propValue) || "0".equals(propValue)) {
            d.set(Calendar.HOUR, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
        } else {
            d.setTime(new Date(Long.parseLong(propValue)));
        }
        dp.setDate(d.getTime());
        valueControlArray.add(dp);
        dp.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                DatePickerCombo source = (DatePickerCombo) e.getSource();
                int index = valueControlArray.indexOf(source);
                Date controlDate = source.getDate();
                if (controlDate == null) {
                    return;
                }
                Date date = new Date(controlDate.getTime()); // copy to
                // prevent side effects
                Relation spec = (Relation) PredicateHelpers.getPredicateAt(domainObject, index);
                Constant newValue = Constant.build(date);
                Constant oldValue = (Constant) spec.getRHS();
                if (!oldValue.getValue().getValue().toString().equals(newValue.getValue().getValue().toString())) {
                    addUndoElement(PropertyExpressionUndoElementOp.CHANGE_VALUE, index, newValue, oldValue);
                    spec.setRHS(newValue);
                }
            }
        });
    }

    private static boolean isDateAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp, EntityType.forSpecType(PredicateHelpers.getExpressionType(exp)));
            return type == AttributeType.DATE;
        }
        return false;
    }

    private static boolean isNumberAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp, EntityType.forSpecType(PredicateHelpers.getExpressionType(exp)));
            return type == AttributeType.LONG;
        }
        return false;
    }

    private static boolean isBooleanAttribute(IExpression exp) {
        if (exp instanceof SpecAttribute) {
            AttributeType type = getTypeForAttribute((SpecAttribute) exp, EntityType.forSpecType(PredicateHelpers.getExpressionType(exp)));
            return type == AttributeType.BOOLEAN;
        }
        return false;
    }

    private OperatorInfo getOperatorsForAttribute(IExpression exp) {
        OperatorInfo operatorInfo;
        if (isDateAttribute(exp)) {
            operatorInfo = dateOperators;
        } else if (isNumberAttribute(exp)) {
            operatorInfo = numberOperators;
        } else if (isBooleanAttribute(exp)) {
            operatorInfo = yesNoStates;
        } else {
            operatorInfo = stringOperators;
        }
        return operatorInfo;
    }

    /**
     * remove controls corresponding to index
     * 
     * @param index
     */
    protected void removeExpressionControls(int index) {
        Control c = (Control) removeButtonArray.remove(index);
        c.dispose();
        c = (Control) propControlArray.remove(index);
        c.dispose();
        c = (Control) opControlArray.remove(index);
        c.dispose();
        c = (Control) valueControlArray.remove(index);
        c.dispose();
    }

    public void relayout() {

        if (isEditable()) {
            relayoutEditable();
        } else {
            relayoutNonEditable();
        }

        layout();

    }

    private void relayoutEditable() {
        int spacing = 5;
        int currentX = spacing;
        int currentY = spacing;

        tabListArray.clear();

        for (int i = 0; i < propControlArray.size(); i++) {
            Control t = (Control) removeButtonArray.get(i);
            t.setBounds(currentX, currentY, REMOVE_BUTTON_WIDTH, CONTROL_HEIGHT);
            tabListArray.add(t);
            currentX += CONTROL_HEIGHT + spacing;
            t = (Control) propControlArray.get(i);
            Point controlSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            t.setBounds(currentX, currentY, controlSize.x, CONTROL_HEIGHT);
            tabListArray.add(t);
            currentX += controlSize.x + spacing;
            t = (Control) opControlArray.get(i);
            controlSize = t.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            t.setBounds(currentX, currentY, controlSize.x, CONTROL_HEIGHT);
            tabListArray.add(t);
            currentX += controlSize.x;
            t = (Control) valueControlArray.get(i);
            if (t instanceof DatePickerCombo) {
                t.setBounds(currentX, currentY, DATE_PICKER_WIDTH, CONTROL_HEIGHT);
            } else {
                t.setBounds(currentX, currentY, VALUE_TEXT_WIDTH, CONTROL_HEIGHT);
            }
            tabListArray.add(t);
            currentX = spacing;
            currentY += CONTROL_HEIGHT + spacing;
        }

        Point addButtonSize = addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        addButton.setBounds(currentX, currentY, addButtonSize.x, CONTROL_HEIGHT);
        tabListArray.add(addButton);
        setTabList((Control[]) tabListArray.toArray(new Control[tabListArray.size()]));
    }

    private void relayoutNonEditable() {
        int spacing = 5;
        int currentX;
        int currentY = spacing;

        int maxPropNameLabelLength = 0;
        for (Label nextLabel : propNameLabelArray) {
            Point nextLabelSize = nextLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            maxPropNameLabelLength = nextLabelSize.x > maxPropNameLabelLength ? nextLabelSize.x : maxPropNameLabelLength;
        }

        int maxOperatorLength = 0;
        for (Control nextOperatorControl : propOpLabelArray) {
            int nextOperatorSize = nextOperatorControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            maxOperatorLength = nextOperatorSize > maxOperatorLength ? nextOperatorSize : maxOperatorLength;
        }

        for (int i = 0; i < propNameLabelArray.size(); i++) {
            currentX = spacing + REMOVE_BUTTON_WIDTH + spacing + REMOVE_BUTTON_WIDTH + spacing;

            Control t = (Control) propNameLabelArray.get(i);
            t.setBounds(currentX, currentY, maxPropNameLabelLength + 2, CONTROL_HEIGHT);
            currentX += maxPropNameLabelLength + 2 + spacing;
            t = (Control) propOpLabelArray.get(i);
            t.setBounds(currentX, currentY, maxOperatorLength, CONTROL_HEIGHT);
            currentX += maxOperatorLength + spacing;
            t = (Control) propValueArray.get(i);
            t.setBounds(currentX, currentY, VALUE_TEXT_WIDTH, CONTROL_HEIGHT);

            currentY += CONTROL_HEIGHT + spacing;
        }

    }

    /**
     * 
     */
    private void relayoutParent() {
        setRedraw(false);
        // relayout();
        editorPanel.relayout();
        setRedraw(true);
    }

    /**
     * @param undoOp
     * @param index
     * @param newValue
     * @param oldValue
     */
    private void addUndoElement(PropertyExpressionUndoElementOp undoOp, int index, Object newValue, Object oldValue) {
        addUndoElement(undoOp, index, newValue, oldValue, false);
    }

    private void addUndoElement(PropertyExpressionUndoElementOp undoOp, int index, Object newValue, Object oldValue, boolean continuation) {
        PropertyExpressionUndoElement undoElement = new PropertyExpressionUndoElement();
        undoElement.setIndex(index);
        undoElement.setOp(undoOp);
        undoElement.setOldValue(oldValue);
        undoElement.setNewValue(newValue);
        undoElement.setControlId(controlId);
        undoElement.setContinuation(continuation);
        GlobalState.getInstance().addUndoElement(undoElement);
    }

    /**
     * @return Returns the editable.
     */
    private boolean isEditable() {
        return editable;
    }

    /**
     * @param editable
     *            The editable to set.
     */
    private void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * @return Returns the entityType.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * @return
     */
    protected IExpression getDefaultDateExpression() {
        Calendar d = new GregorianCalendar();
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        return Constant.build(d.getTime());
    }

    private class PredicateModifiedListener implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            if (PropertyExpressionControl.this.domainObject.predicateCount() != PropertyExpressionControl.this.propControlArray.size()) {
                // property was added or removed (probably by undo)
                refresh();
                relayout();
            } else {
                for (int i = 0; i < PropertyExpressionControl.this.domainObject.predicateCount(); i++) {
                    IPredicate pred = PropertyExpressionControl.this.domainObject.predicateAt(i);
                    if (pred instanceof Relation) {
                        Relation spec = (Relation) pred;
                        Combo propControl = (Combo) PropertyExpressionControl.this.propControlArray.get(i);
                        SubjectAttribute lhs = (SubjectAttribute) spec.getLHS();
                        if (!lhs.getName().equals(propControl.getText())) {
                            propControl.setText((lhs).getName());
                        }
                        Combo opControl = (Combo) PropertyExpressionControl.this.opControlArray.get(i);
                        if (!spec.getOp().getName().equals(opControl.getText())) {
                            propControl.setText(spec.getOp().getName());
                        }
                        Text valueControl = (Text) PropertyExpressionControl.this.valueControlArray.get(i);
                        String newText = stringFromExpression(spec.getRHS());
                        if (!valueControl.getText().equals(newText)) {
                            valueControl.setText(newText);
                        }
                    }
                }
            }
        }
    }
}
