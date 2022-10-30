/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/AbstractHQLFormatVisitor.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Stack;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultExpressionVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;

/**
 * This is an abstract base class for the three HQL visitors
 * used in the dictionary to format conditions on elements,
 * structural groups, and enumerated groups.
 *
 * This class contains functionality common for all three visitors.
 * Subclasses provide additional validation.
 */
abstract class AbstractHQLFormatVisitor implements IPredicateVisitor, IExpressionVisitor, IDictionaryPredicateVisitor {
    /**
     * This stack contains binary boolean operators.
     * Each composite predicate adds its operator to the stack
     * at the beginning of the visit, and removes it when the visit
     * is over.
     */
    private final Stack<String> opStack = new Stack<String>();

    /**
     * This stack contains flags indicating whether an infix operator
     * should be inserted before the next predicate. Each composite
     * predicate pushes "true" onto this stack. Once the first element
     * of the infix chain is printed, the "false" is pushed to indicate
     * that all subsequent calls to addInfixOps should print an operator.
     */
    private final Stack<Boolean> separatorStack = new Stack<Boolean>();

    /**
     * This identity map is used for detection of cycles in predicates.
     */
    private final IdentityHashMap<IPredicate,?> seen = new IdentityHashMap<IPredicate,Object>();

    /**
     * This is the output buffer of this visitor.
     */
    private final StringBuffer out = new StringBuffer();

    /**
     * This <code>List</code> holds scalar parameters of the generated
     * query (if any). Elements of this <code>List</code> are of type
     * <code>Parameter</code>; they are used for binding parameters
     * to the generated query.
     */
    private final List<Parameter> parameters = new ArrayList<Parameter>(32);

    /**
     * This <code>List</code> holds multivalued parameters
     * of the generated query (if any). Elements of this
     * <code>List</code> are of type <code>Parameter</code>;
     * they are used for binding parameters to the generated query.
     */
    private final List<Parameter> multiParameters = new ArrayList<Parameter>(8);

    /** This constant holds the hibernate type for the type field of the leaf entity. */
    private static final Type TYPE_OF_TYPE_FIELD = Hibernate.entity(ElementType.class);

    /**
     * This field represents an alias used for the table
     * against which the dictionary runs its select statement.
     */
    private final String alias;

    /**
     * This protected constructor simply sets the alias to
     * that specified as the parameter.
     *
     * @param alias the alias of the table
     * against which the dictionary runs its select statement.
     */
    protected AbstractHQLFormatVisitor(String alias) {
        this.alias = alias;
    }

    /**
     * Formats a composite predicate.
     * 
     * @param pred the composite predicate to format.
     * @param preorder true when it's a pre-order visit call, false otherwise.
     * @see IPredicateVisitor#visit(ICompositePredicate, boolean)
     */
    public void visit(ICompositePredicate pred, boolean preorder) {
        if ( preorder ) {
            String opStr = pred.getOp().toString();
            addInfixOps();
            if ( seen.containsKey( pred) ) {
                throw new IllegalStateException(
                    "Detected a circular reference in a predicate."
                );
            }
            seen.put( pred, null );
            if ( pred.predicateCount() == 1 && pred.getOp() == BooleanOp.NOT) {
                put( opStr, " (" );
            } else {
                put("(");
            }
            opStack.push(opStr);
            separatorStack.push(true);
        } else {
            seen.remove( pred );
            put(")");
            // The calls to preorder/postorder must be symmetric:
            assert !opStack.isEmpty();
            assert !separatorStack.isEmpty();
            opStack.pop();
            separatorStack.pop();
        }
    }

    /**
     * Formats a predicate reference as HQL.
     * 
     * @param pred the predicate reference to format.
     */
    public void visit(IPredicateReference pred) {
        throw new IllegalArgumentException(
            "condition may not contain predicate references: "+pred
        );
    }

    /**
     * Formats a relation as HQL.
     * 
     * @param spec the relation spec to format.
     */
    public void visit(IRelation spec) {
        addInfixOps();
        final ElementType types[] = new ElementType[2];
        spec.getLHS().acceptVisitor( new DefaultExpressionVisitor() {
            public void visit( IAttribute attr ) {
                if (attr instanceof ElementField) {
                    types[0] = ((ElementField)attr).getParentType();
                } else if (!(attr instanceof ElementBaseAttribute)) {
                    throw new IllegalArgumentException("Unexpected attribute type");
                }
            }
        }, IExpressionVisitor.PREORDER);
        spec.getRHS().acceptVisitor( new DefaultExpressionVisitor() {
            public void visit( IAttribute attr ) {
                if (attr instanceof ElementField) {
                    types[1] = ((ElementField)attr).getParentType();
                } else if (!(attr instanceof ElementBaseAttribute)) {
                    throw new IllegalArgumentException("Unexpected attribute type");
                }
            }
        }, IExpressionVisitor.PREORDER);
        ElementType type = null;
        if ( types[0] != null ) {
            type = types[0];
        } else if ( types[1] != null ) {
            type = types[1];
        }
        if ( type != null ) {
            put("(");
            visitElementType(type);
            put(" AND ");
        }
        // Equality/inequality for certain  types requires special treatment
        if ( spec.getOp() == RelationOp.EQUALS || spec.getOp() == RelationOp.NOT_EQUALS ) {
            final boolean isNull[] = new boolean[2];
            final boolean hasWildcards[] = new boolean[2];
            final Constant strConst[] = new Constant[2];
            final IMappedElementField strField[] = new IMappedElementField[2];
            final IMappedElementField cssField[] = new IMappedElementField[2];
            final IMappedElementField saField[] = new IMappedElementField[2];
            // This visitor lets us determine if expressions referenced from
            // this relation are null constants, string constants,
            // or string fields. This information is used to format
            // the correct operator (e.g. is null / is not null). 
            class TypeInfoGatherer extends DefaultExpressionVisitor {
                private final int index;
                public TypeInfoGatherer(int index) {
                    this.index = index;
                }
                public void visit( IAttribute attr ) {
                    if ( !(attr instanceof IMappedElementField) ) {
                        throw new IllegalArgumentException("Unexpected attribute type");
                    }
                    IMappedElementField field = (IMappedElementField)attr;
                    if (field.getType() == ElementFieldType.STRING
							|| field.getType() == ElementFieldType.LONG_STRING) {
						strField[index] = field;
					}
                    if (field.getType() == ElementFieldType.CS_STRING) {
                        cssField[index] = field;
                    }
                    if (field.getType() == ElementFieldType.STRING_ARRAY) {
                        saField[index] = field;
                    }
                }
                public void visit( Constant constant ) {
                    if ( constant.getValue() == IEvalValue.NULL ) {
                        isNull[index] = true;
                    } else if ( constant.getValue().getType() == ValueType.STRING ) {
                        strConst[index] = constant;
                        String str = ((String)constant.getValue().getValue());
                        hasWildcards[index] = hasWildcards(str);
                    }
                }
            }
            spec.getLHS().acceptVisitor(new TypeInfoGatherer(0), IExpressionVisitor.PREORDER);
            spec.getRHS().acceptVisitor(new TypeInfoGatherer(1), IExpressionVisitor.PREORDER);
            boolean inverted = spec.getOp()==RelationOp.NOT_EQUALS;
            if ( isNull[0] ) {
                formatIsNull(spec.getRHS(), inverted);
            } else if ( isNull[1] ) {
                formatIsNull(spec.getLHS(), inverted);
            } else if ( strConst[0] != null && strField[1] != null ) {
                formatCaseInsensitiveCompare(strField[1], strConst[0], inverted);
            } else if ( strConst[1] != null && strField[0] != null ) {
                formatCaseInsensitiveCompare(strField[0], strConst[1], inverted);
            } else if ( hasWildcards[0] && cssField[1] != null ) {
                formatLikeCompare(cssField[1], strConst[0], inverted);
            } else if ( hasWildcards[1] && cssField[0] != null ) {
                formatLikeCompare(cssField[0], strConst[1], inverted);
            } else if (strConst[0] != null && saField[1] != null) {
                formatCaseInsensitiveCompare(
                    saField[1]
                ,   convertToMultivalueConstant(strConst[0])
                ,   inverted
                );
            } else if (strConst[1] != null && saField[0] != null) {
                formatCaseInsensitiveCompare(
                    saField[0]
                ,   convertToMultivalueConstant(strConst[1])
                ,   inverted
                );
            } else {
                spec.getLHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
                put(" ", spec.getOp(), " ");
                spec.getRHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
            }
        } else {
            spec.getLHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
            put(" ", spec.getOp(), " ");
            spec.getRHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
        }
        if ( type != null ) {
            put(")");
        }
    }

    /**
     * Formats a generic predicate. This is where we make a determination
     * that a predicate is an <code>IDictionaryPredicate</code> and call
     * its <code>accept</code> method.
     *
     * @param pred the generic predicate to format.
     */
    public void visit(IPredicate pred) {
        addInfixOps();
        if (pred instanceof IDictionaryPredicate) {
            ((IDictionaryPredicate)pred).accept(this);
        } else if ( pred == PredicateConstants.TRUE ) {
            put("1=1");
        } else if ( pred == PredicateConstants.FALSE ) {
            put("1=0");
        } else {
            throw new IllegalArgumentException("Unknown predicate type: "+pred);
        }
    }

    /**
     * Implements the visiting of direct and indirect dictionary paths.
     * @param path the <code>DictionaryPath</code> to visit.
     */
    public void visitDictionaryPath( DictionaryPath path, boolean direct ) {
        if (path == DictionaryPath.ROOT) {
            // Everything is a child of the ROOT.
            // We cannot leave the condition blank because
            // the HQL builder for composite predicates assume
            // that each subordinate predicate adds a non-empty condition.
            put("1=1");
        } else {
            put(getAlias(), ".path.path LIKE ");
            addParameter(path.toFilterString(direct), Hibernate.STRING);
        }
    }

    /**
     * @see IDictionaryPredicateVisitor#visitElementType(ElementType)
     */
    public void visitElementType( ElementType type ) {
        put(getAlias(), ".type = ");
        addParameter(type, TYPE_OF_TYPE_FIELD);
    }

    /**
     * @see IDictionaryPredicateVisitor#visitEnrollment(Enrollment)
     */
    public void visitEnrollment(Enrollment enrollment) {
        put(getAlias(), ".enrollment = ");
        addParameter(enrollment, Enrollment.TYPE);
    }

    /**
     * @see IDictionaryPredicateVisitor#visitChangedCondition(Date, Date)
     */
    public void visitChangedCondition( Date startDate, Date endDate ) {
        // See if this is a no-op
        if (startDate != null || endDate != null) {
            put("exists (from DictionaryElementBase b where ", getAlias(), ".originalId=b.originalId");
            if (startDate != null) {
                put(" and b.timeRelation.activeFrom>=");
                addParameter(startDate, DateToLongUserType.TYPE);
            }
            if (endDate != null) {
                put(" and b.timeRelation.activeFrom<");
                addParameter(endDate, DateToLongUserType.TYPE);
            }
            put(")");
        } else {
            put("1=1");
        }
    }

    /**
     * @see IExpressionVisitor#visit(IAttribute)
     */
    public void visit(IAttribute attr) {
        if(attr instanceof IMappedElementField) {
            put(getAlias(), ".", ((IMappedElementField)attr).getMapping());
        } else {
            throw new IllegalArgumentException("Unexpected attribute type");
        }
    }

    /**
     * @see IExpressionVisitor#visit(Constant)
     */
    public void visit(Constant constant) {
        addParameter(constant);
    }

    /**
     * @see IExpressionVisitor#visit(IFunctionApplication)
     */
    public void visit(IFunctionApplication func) {
        put(func.toString());
    }

    public void visit(IExpression expression) {
        // Not much to do here - we're accepting something
        // we don't know, so using its toString looks like
        // our best bet.
        put(expression.toString());
    }

    public void visit(IExpressionReference ref) {
        throw new IllegalArgumentException(
            "condition may not contain expression references: "+ref
        );
    }

    /**
     * Binds the collected query parameters to the query.
     * @param query the query to which to bind the parameters.
     * @throws HibernateException if the bind operation fails.
     */
    public void bindParametersToQuery( Query query ) throws HibernateException {
        for (Parameter p : parameters) {
            query.setParameter( p.getName(), p.getValue(), p.getType() );
        }
        for (Parameter p : multiParameters) {
            query.setParameterList( p.getName(), (Collection<?>)p.getValue() );
        }
    }

    /**
     * This method checks if the infix operators need to be added,
     * and adds them if necessary. Then the method updates the stack
     * of separator flags, indicating whether or not
     * the next call should print an infix operator.
     */
    private void addInfixOps() {
        if ( separatorStack.isEmpty() ) {
            return;
        }
        if (!separatorStack.peek()) {
            // The two stacks grow and shrink at the same time:
            assert !opStack.empty();
            put(" ", opStack.peek(), " ");
        } else {
            separatorStack.pop();
            separatorStack.push(false);
        }
    }

    /**
     * This method appends objects to the output buffer.
     * @param o the objects to be appended.
     */
    protected void put(Object ... o) {
        for (Object obj : o) {
            out.append(obj);
        }
    }

    /**
     * This getter returns the value of the alias set in the constructor.
     * @return the value of the alias set in the constructor.
     */
    protected String getAlias() {
        return alias;
    }

    /**
     * Returns the number of parameters that have been added so far.
     * @return the number of parameters that have been added so far.
     */
    protected int getParameterCount() {
        return parameters.size();
    }

    private static Constant convertToMultivalueConstant(Constant from) {
        if (from == null || from.getValue() == null || from.getValue().getType() != ValueType.STRING) {
            throw new IllegalArgumentException("from");
        }
        String fromValue = (String)from.getValue().getValue();
        return Constant.build("*:"+fromValue+":*");
    }

    /**
     * This method formats an is null / is not null expression.
     *
     * @param expr the expression that must be compared to null.
     * @param inverted a flag indicating that the condition is "not null"
     */
    private void formatIsNull(IExpression expr, boolean inverted ) {
        if ( expr instanceof IMappedElementField ) {
            put(getAlias(), "."+((IMappedElementField)expr).getMapping());
        } else {
            expr.acceptVisitor(this, IExpressionVisitor.PREORDER);
        }
        put(inverted ? " is not null" : " is null");
    }

    /**
     * This method formats a case-insensitive "like" condition using
     * the specified field and the string constant.
     *
     * @param field the field that has to be compared case-insensitively.
     * @param constant the constant with which a case-insensitive compare
     * is to be performed.
     * @param inverted  a flag indicating that the condition is "not like"
     */
    private void formatCaseInsensitiveCompare(IMappedElementField field, Constant constant, boolean inverted) {
        if ( inverted ) {
            put("not (");
        }
        CaseInsensitiveLike like = new CaseInsensitiveLike(
            "<unused>"
        ,   replaceWildcards(
                (String)constant.getValue().getValue()
            )
        );
        String[] parameterNames = new String[5];
        for ( int i = 0 ; i != parameterNames.length ; i++ ) {
            parameterNames[i] = ":p"+(parameters.size()+i);
        }
        put( like.getCondition(getAlias()+"."+field.getMapping(), parameterNames, "lower") );
        String[] vals = like.getBindStrings();
        for (int i = 0; i != vals.length; i++) {
            String name = "p"+parameters.size();
            parameters.add( new Parameter( name, vals[i], Hibernate.STRING ) );
        }
        put(" and ", field.getMapping(), " is not null ");
        if ( inverted ) {
            put(")");
        }
    }

    /**
     * This method formats a case-sensitive like condition using
     * the specified field and the string constant.
     *
     * @param field the field that has to be compared using the "like" operator.
     * @param constant the constant with wildcards against which
     * the "like" compare is to be performed.
     * @param inverted  a flag indicating that the condition is "not like"
     */
    private void formatLikeCompare(IMappedElementField field, Constant constant, boolean inverted) {
        if ( inverted ) {
            put("not (");
        }
        put(getAlias(), ".", field.getMapping());
        put(" like ");
        addParameter(replaceWildcards((String)constant.getValue().getValue()), Hibernate.STRING);
        put(" and ", field.getMapping(), " is not null ");
        if ( inverted ) {
            put(")");
        }
    }

    /**
     * Obtains the result of the formatting.
     * @return the result of the formatting.
     */
    public String getResult() {
        return out.toString();
    }

    /**
     * Generates a new parameter name, adds a new parameter
     * to the parameter list, and writes the name of the parameter
     * to the output.
     * @param value the value of the parameter to add.
     * @param type the type of the parameter to add.
     */
    protected synchronized String addParameter(Object value, Type type) {
        String name = "p"+parameters.size();
        parameters.add( new Parameter( name, value, type ) );
        name = ":"+name;
        put(name);
        return name;
    }

    /**
     * Generates a new parameter name, adds a new parameter
     * to the parameter list, and writes the name of the parameter
     * to the output.
     * @param constant the <code>Constant</code> object
     * from which to create a parameter.
     */
    protected synchronized String addParameter(Constant constant) {
        if ( constant == null ) {
            throw new NullPointerException("constant");
        }
        IEvalValue val = constant.getValue();
        if ( val == null ) {
            throw new NullPointerException("constant's value");
        }
        String name;
        if ( val.getType() == ValueType.MULTIVAL ) {
            name = "m"+multiParameters.size();
            IMultivalue mv = (IMultivalue)val.getValue();
            Collection<Object> vals = new ArrayList<Object>();
            for (IEvalValue v : mv) {
                vals.add(v.getValue());
            }
            multiParameters.add( new Parameter(name, vals, Hibernate.OBJECT) );
        } else {
            name = "p"+parameters.size();
            parameters.add( new Parameter( name, constant ) );
        }
        name = ":"+name;
        put(name);
        return name;
    }

    /**
     * This method determines if the incoming string contains
     * unescaped wildcard characters '*' or '?'.
     * @param str the <code>String</code> to check for wildcards.
     * @return <code>true</code> if the input contains unescaped
     * wildcard characters, <code>false</code> otherwise.
     */
    private static boolean hasWildcards(String str) {
        int len = str.length();
        for ( int i = 0 ; i < len ; i++ ) {
            char c = str.charAt(i);
            if (c != '\\') {
                if (c == '*' || c == '?') {
                    return true;
                }
            } else {
                i++;
            }
        }
        return false;
    }

    /**
     * Replaces all unescaped wildcards in the original
     * <code>Srting</code> ('*' becomes '%', '?' becomes '_'), and
     * escapes all database wildcards ('%' and '_'). 
     * @param original the <code>String</code> with wildcards.
     * @return the <code>original</code> with all wildcards replaced
     * and the SQL wildcard characters escaped.
     */
    private static String replaceWildcards(String original) {
        StringBuffer res = new StringBuffer(original.length()+4);
        int len = original.length();
        for ( int i = 0 ; i < len ; i++ ) {
            char ch = original.charAt(i);
            switch (ch) {
            case '\\':
                i++;
                if (i<len) {
                    char c = original.charAt(i);
                    // SQL wildcards are always escaped
                    if (c=='%' || c=='_') {
                        res.append('\\');
                    }
                    res.append(c);
                }
                break;
            case '*':
                res.append('%');
                break;
            case '?':
                res.append('_');
                break;
            case '%':
            case '_':
                res.append('\\');
                // Fall-through
            default:
                res.append(ch);
                break;
            }
        }
        return res.toString();
    }

    /**
     * This class holds parameter information
     * for binding to HQL-based Hibernate queries.
     */
    private static class Parameter {

        /** This field holds the name of the parameter. */
        private final String name;

        /** This field holds the value of the parameter. */
        private final Object value;

        /** This field holds the type of the parameter. */
        private final Type type;

        /**
         * Constructs a <code>Parameter</code> with the given
         * name, value, and type.
         * @param name the name of this parameter.
         * @param value the value of this parameter.
         * @param type the type of this parameter.
         */
        public Parameter( String name, Object value, Type type ) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        /**
         * Constructs a parameter for the given <code>Constant</code>.
         * @param name the name of this parameter.
         * @param constant the constant from which to create the parameter.
         */
        public Parameter( String name, Constant constant ) {
            this.name = name;
            IEvalValue val = constant.getValue();
            value = val.getValue();
            if ( val.getType() == ValueType.STRING ) {
                this.type = Hibernate.STRING;
            } else if ( val.getType() == ValueType.LONG ) {
                this.type = Hibernate.LONG;
            } else if ( val.getType() == ValueType.DATE ) {
                this.type = Hibernate.LONG;
            } else {
                throw new IllegalArgumentException(
                    "Constant of unexpected type: "+val.getType()
                );
            }
        }

        /**
         * Obtains the name of the parameter.
         * @return the name of this parameter.
         */
        public String getName() {
            return name;
        }

        /**
         * Obtains the value of this parameter.
         * @return the value of this parameter.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Obtains the type of this parameter.
         * @return the type of this parameter.
         */
        public Type getType() {
            return type;
        }

    }

}
