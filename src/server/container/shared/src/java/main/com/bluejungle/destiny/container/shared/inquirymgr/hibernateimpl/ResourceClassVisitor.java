/*
 * Created on Apr 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * The resource class visitor allows building an HQL statement based on a
 * resource class definition.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ResourceClassVisitor.java#3 $
 */

public class ResourceClassVisitor extends DefaultPredicateVisitor implements IPQLVisitor, IResourceClassVisitor {

    private static final Map RELATION_OP_2_HQL = new HashMap();
    private static final Map RES_ATTR_2_PROP_NAME = new HashMap();
    private static final Map BOOLEAN_OP_2_HQL = new HashMap();

    /**
     * Predefined constants
     */
    private static final String ALWAYS_FALSE = "1=0";
    private static final String ALWAYS_TRUE = "1=1";

    private final Stack operationStack = new Stack();
    private final Stack expressionCounterStack = new Stack();
    private StringBuffer hql = new StringBuffer(50);
    private List selects = new ArrayList();
    private Long resourceId;
    private String variableName;
    private final Map args;

    static {
        //Boolean operator
        BOOLEAN_OP_2_HQL.clear();
        BOOLEAN_OP_2_HQL.put(BooleanOp.AND, HQLConstants.AND);
        BOOLEAN_OP_2_HQL.put(BooleanOp.OR, HQLConstants.OR);
        BOOLEAN_OP_2_HQL.put(BooleanOp.NOT, HQLConstants.NOT);
        //relation operator mapping
        RELATION_OP_2_HQL.clear();
        RELATION_OP_2_HQL.put(RelationOp.GREATER_THAN, HQLConstants.GREATER_THAN);
        RELATION_OP_2_HQL.put(RelationOp.GREATER_THAN_EQUALS, HQLConstants.GREATER_THAN_OR_EQUAL);
        RELATION_OP_2_HQL.put(RelationOp.LESS_THAN, HQLConstants.LESS_THAN);
        RELATION_OP_2_HQL.put(RelationOp.LESS_THAN_EQUALS, HQLConstants.LESS_THAN_OR_EQUAL);
        //Resource attribute to HQL data object field mapping
        RES_ATTR_2_PROP_NAME.clear();
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.DIRECTORY, "name");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.TYPE, "name");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.NAME, "name");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.OWNER, "ownerId");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.SIZE, "size");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.TYPE, "name");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.CREATED_DATE, "createdDate");
        RES_ATTR_2_PROP_NAME.put(ResourceAttribute.MODIFIED_DATE, "modifiedDate");
    }

    /**
     * Default Constructor
     */
    public ResourceClassVisitor() {
        this(null);
    }

    /**
     * 
     * Constructor
     * 
     * @param argumentsMap
     *            a <code>Map</code> of argument names to argument values.
     */
    public ResourceClassVisitor(Map argumentsMap) {
        super();
        Map mapToUse = argumentsMap;
        if (mapToUse == null) {
            mapToUse = new HashMap();
        }
        this.args = mapToUse;
    }

    /**
     * Adds a new element to the map of arguments.
     * 
     * @param value
     *            the value to be added.
     * @return the name of the newly added argument.
     */
    private String addArgument(String value) {
        String name = "resource" + getArguments().size();
        getArguments().put(name, value);
        return HQLConstants.COLON + name;
    }

    /**
     * Adds some new expression to the HQL output
     * 
     * @param expr
     *            expression to add
     */
    protected void addHQL(final String expr) {
        this.hql.append(expr);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceClassVisitor#addNewEntityToVisit(boolean)
     */
    public void addNewEntityToVisit(boolean first) {
        if (!first) {
            addHQL(HQLConstants.SPACE + HQLConstants.OR + HQLConstants.SPACE);
        }
    }

    /**
     * Returns an HQL expression of a variable and its attribute.
     * 
     * @param attrName
     *            name of the attribute
     * @return the variable name and the attribute properly appended.
     */
    protected String getAttributeExpression(String attrName) {
        return this.variableName + "." + attrName;
    }

    /**
     * returns the HQL expression associated with the resource definition
     * 
     * @return the HQL expression associated with the resource definition
     */
    public String getHQLExpression() {
        return this.hql.toString();
    }

    /**
     * Returns the list of select elements
     * 
     * @return the list of select elements
     */
    public List getSelects() {
        return this.selects;
    }

    /**
     * Returns a <code>Map</code> of argument names (keys) to argument values.
     * If the result has no arguments, an empty map is returned.
     * 
     * @return a <code>Map</code> of argument names (keys) to argument values.
     */
    public Map getArguments() {
        return this.args;
    }

    /**
     * Returns the id of the resource associated with this visitor instance
     * 
     * @return the id of the resource associated with this visitor instance
     */
    public Long getResourceId() {
        return this.resourceId;
    }

    /**
     * Adjusts the expression wildcards
     * 
     * @param inputValue
     *            value to be processed
     * @return an HQL expression that contains correct HQL wildcards
     */
    protected String processWildcards(final String inputValue) {
        return inputValue.replaceAll("[*][*][\\.][*][*]", HQLConstants.STAR_WILDCARD).replaceAll("[*][*][/.][*][*]", HQLConstants.STAR_WILDCARD).replaceAll("[*][*][/.][*]", HQLConstants.STAR_WILDCARD).replaceAll("[*][*]", HQLConstants.STAR_WILDCARD)
                .replaceAll("[*]", HQLConstants.STAR_WILDCARD).replaceAll("[?]", HQLConstants.SINGLE_CHAR_WILDCARD);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceClassVisitor#setVariableName(java.lang.String)
     */
    public void setVariableName(String varName) {
        if (varName == null || varName.length() == 0) {
            throw new NullPointerException("Variable name cannot be null or empty");
        }
        this.variableName = varName;
    }

    /**
     * Updates the state stacks
     */
    private void updateVisitingState() {
        while (!expressionCounterStack.isEmpty()) {
            int n = ((Integer) expressionCounterStack.peek()).intValue();
            if (n > 1) {
                expressionCounterStack.pop();
                expressionCounterStack.push(new Integer(n - 1));
                addHQL(HQLConstants.SPACE);
                addHQL(operationStack.peek().toString());
                addHQL(HQLConstants.SPACE);
                break;
            } else {
                //Last elements in the stacks
                addHQL(HQLConstants.CLOSE_PARENTHESE);
                expressionCounterStack.pop();
                operationStack.pop();
            }
        }
    }

    /**
     * This function populates the HQL maps. These maps are used for lookup
     * between operators and HQL expressions.
     */

    /**
     * @see IPQLVisitor#visitPolicy(DomainObjectDescriptor,
     *      com.bluejungle.pf.domain.destiny.policy.IDPolicy)
     */
    public void visitPolicy(DomainObjectDescriptor ignored, IDPolicy arg2) {
        //Should never happen
        throw new UnsupportedOperationException();
    }

    /**
     * @see IPQLVisitor#visitFolder(DomainObjectDescriptor)
     */
    public void visitFolder(DomainObjectDescriptor ignored) {
        //Should never happen
        throw new UnsupportedOperationException();
    }

    /**
     * @see IPQLVisitor#visitAccessPolicy(DomainObjectDescriptor,
     *      com.bluejungle.pf.domain.destiny.common.IAccessPolicy)
     */
    public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
        // Not relevant in this context
        throw new UnsupportedOperationException();
    }

    /**
     * @see IPQLVisitor#visitComponent(DomainObjectDescriptor, IPredicate)
     */
    public void visitComponent(DomainObjectDescriptor ignored, IPredicate resourcePred) {
        if (resourcePred == null) {
            return;
        }
        addHQL(HQLConstants.OPEN_PARENTHESE);
        resourcePred.accept(this, IPredicateVisitor.PREORDER);
        addHQL(HQLConstants.CLOSE_PARENTHESE);
    }

    /**
     * Visits a composite predicate. The appropriate HQL operator is taken from
     * the mapping table.
     * 
     * @see com.bluejungle.framework.expressions.IPredicateVisitor#visit(com.bluejungle.framework.expressions.ICompositePredicate)
     */
    public void visit(ICompositePredicate pred, boolean preorder) {
        int n = pred.predicateCount();
        if (n == 1) {
            addHQL((String) BOOLEAN_OP_2_HQL.get(pred.getOp()));
            addHQL(HQLConstants.SPACE);
        } else if (n > 1) {
            operationStack.push(pred.getOp());
            expressionCounterStack.push(new Integer(n));
            addHQL(HQLConstants.OPEN_PARENTHESE);
        }
    }

    public void visitLocation(DomainObjectDescriptor dod, Location loc) {

    }

    /**
     * @see com.bluejungle.framework.expressions.IPredicateVisitor#visit(com.bluejungle.framework.expressions.IPredicate)
     */
    public void visit(IPredicate predicate) {
        if (PredicateConstants.TRUE.equals(predicate)) {
            addHQL(HQLConstants.SPACE + ALWAYS_TRUE);
            updateVisitingState();
        } else if (PredicateConstants.FALSE.equals(predicate)) {
            addHQL(HQLConstants.SPACE + ALWAYS_FALSE);
            updateVisitingState();
        }
    }

    /**
     * @throws EntityManagementException
     * @see com.bluejungle.framework.expressions.IPredicateVisitor#visit(com.bluejungle.framework.expressions.IPredicateReference)
     */
    public void visit(IPredicateReference predRef) {
        throw new UnsupportedOperationException("Visit predicate reference by name is not supported");
    }

    /**
     * @see com.bluejungle.framework.expressions.IPredicateVisitor#visit(com.bluejungle.framework.expressions.IRelation)
     */
    public void visit(IRelation relation) {
        if (relation == null) {
            throw new NullPointerException("relation cannot be null");
        }
        RelationOp op = relation.getOp();
        ResourceAttribute lhs = (ResourceAttribute) relation.getLHS();

        //Equal or not equal need to be treated specifically based on the type
        // of values that are used.
        if (RelationOp.NOT_EQUALS.equals(op)) {
            Constant rhs = (Constant) relation.getRHS();
            IEvalValue evalValue = rhs.getValue();
            if (ValueType.STRING.equals(evalValue.getType())) {
                //if (ResourceAttribute.OWNER_LDAP_GROUP != lhs) {
                addHQL(HQLConstants.LOWER + HQLConstants.OPEN_PARENTHESE + this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs) + HQLConstants.CLOSE_PARENTHESE);
                String output = HQLConstants.SPACE + HQLConstants.NOT + HQLConstants.SPACE + HQLConstants.LIKE;
                output += HQLConstants.SPACE + addArgument(processWildcards(((String) evalValue.getValue()).toLowerCase()));
                addHQL(output);
                /*
                 * } else { SelectElementImpl resourceOwnerSelect = new
                 * SelectElementImpl();
                 * resourceOwnerSelect.setDOVarName("resourceOwner");
                 * resourceOwnerSelect.setDOClassName("UserDO");
                 * this.selects.add(resourceOwnerSelect);
                 * 
                 * List groupNames = new ArrayList(1); groupNames.add(0,
                 * evalValue.getValue()); addHQL(this.variableName + "." +
                 * RES_ATTR_2_PROP_NAME.get(lhs)); addHQL(HQLConstants.SPACE +
                 * HQLConstants.EQUAL + HQLConstants.SPACE);
                 * addHQL("resourceOwner.sid AND ");
                 * addHQL(HQLHelper.getGroupNameCondition("resourceOwner",
                 * "ownerGroup", "UserGroupDO", "users", groupNames,
                 * "resourceOwner", getArguments())); }
                 */
            } else if (ValueType.LONG.equals(evalValue.getType())) {
                addHQL(this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs));
                addHQL(HQLConstants.SPACE + HQLConstants.NOT_EQUAL + HQLConstants.SPACE + evalValue.getValue());
            } else if (ValueType.NULL.equals(evalValue.getType())) {
                addHQL(this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs));
                addHQL(HQLConstants.SPACE + HQLConstants.IS_NOT_NULL);
            }
        } else if (RelationOp.EQUALS.equals(op)) {
            Constant rhs = (Constant) relation.getRHS();
            IEvalValue value = rhs.getValue();
            if (ValueType.STRING.equals(value.getType())) {
                //if (ResourceAttribute.OWNER_LDAP_GROUP != lhs) {
                    addHQL(HQLConstants.LOWER + HQLConstants.OPEN_PARENTHESE + this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs) + HQLConstants.CLOSE_PARENTHESE);
                    addHQL(HQLConstants.SPACE + HQLConstants.LIKE + HQLConstants.SPACE + addArgument(processWildcards(((String) value.getValue()).toLowerCase())));
                /*} else {
                    SelectElementImpl resourceOwnerSelect = new SelectElementImpl();
                    resourceOwnerSelect.setDOVarName("resourceOwner");
                    resourceOwnerSelect.setDOClassName("UserDO");
                    this.selects.add(resourceOwnerSelect);

                    List groupNames = new ArrayList(1);
                    groupNames.add(0, value.getValue());
                    addHQL(this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs));
                    addHQL(HQLConstants.SPACE + HQLConstants.EQUAL + HQLConstants.SPACE);
                    addHQL("resourceOwner.sid AND ");
                    addHQL(HQLHelper.getGroupNameCondition("resourceOwner", "ownerGroup", "UserGroupDO", "users", groupNames, "resourceOwner", getArguments()));
                }*/
            } else if (ValueType.LONG.equals(value.getType())) {
                addHQL(this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs));
                addHQL(HQLConstants.SPACE + HQLConstants.EQUAL + HQLConstants.SPACE + value.getValue());
            } else if (ValueType.NULL.equals(value.getType())) {
                addHQL(this.variableName + "." + RES_ATTR_2_PROP_NAME.get(lhs));
                addHQL(HQLConstants.SPACE + HQLConstants.IS_NULL);
            }
        } else {
            //Other operator are directly mapped
            Constant rhs = (Constant) relation.getRHS();
            IEvalValue evalValue = rhs.getValue();
            final String fieldName = (String) RES_ATTR_2_PROP_NAME.get(lhs);
            if (fieldName != null) {
                addHQL(this.variableName + "." + fieldName);
                addHQL(HQLConstants.SPACE + RELATION_OP_2_HQL.get(op) + HQLConstants.SPACE + evalValue.getValue());
            } else {
                addHQL(ALWAYS_TRUE);
            }
        }
        updateVisitingState();
    }
}
