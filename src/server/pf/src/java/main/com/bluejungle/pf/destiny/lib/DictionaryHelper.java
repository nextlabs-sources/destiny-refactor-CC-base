package com.bluejungle.pf.destiny.lib;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IGroup;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.InvalidGroupException;
import com.bluejungle.dictionary.Order;
import com.bluejungle.dictionary.Page;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.subject.LocationReference;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectAttribute;

public class DictionaryHelper {

    private IDictionary dictionary;
    private IElementType userType;
    private IElementType contactType;
    private IElementType hostType;
    private IElementType appType;
    private IElementField userSysId;
    private IElementField userUnixId;
    private IElementField hostSysId;
    private IElementField hostUnixId;
    private IElementField appSysId;
    private Log log = LogFactory.getLog(DictionaryHelper.class.getName());

    private final Map<ISubjectAttribute,IElementField> builtinFields = new HashMap<ISubjectAttribute,IElementField>();

    public class IncompleteMatchException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IncompleteMatchException( String message ) {
            super( message );
        }
    }

    /**
     * Retrieves subjects from LDAP for an <code>IPredicate</code>
     * and an <code>EntityType</code>.
     * @param pred
     * @param entityType
     * @param byId
     * @param limit the upper limit of the number of items to return.
     * Negative values mean that there is no limit.
     * @return
     * @throws EntityManagementException
     * @throws PQLException
     * @throws CircularReferenceException
     * @throws InterruptedException
     */
    public List<LeafObject> getMatchingSubjects( IPredicate pred, LeafObjectType leafType, final Map<Long,? extends IHasId> byId, int limit ) throws EntityManagementException, PQLException, CircularReferenceException, DictionaryException {
        if (pred == null) {
            throw new NullPointerException("predicate");
        }

        final SubjectType subjectType;
        final IPredicate typeCondition;
        if (leafType != null) {
            if (leafType == LeafObjectType.USER) {
                subjectType = SubjectType.USER;
                CompositePredicate userTypeCond = new CompositePredicate(
                    BooleanOp.OR
                ,   dictionary.condition(userType)
                );
                userTypeCond.addPredicate(dictionary.condition(contactType));
                typeCondition = userTypeCond;
            } else if (leafType == LeafObjectType.HOST) {
                subjectType = SubjectType.HOST;
                typeCondition = dictionary.condition(hostType);
            } else if (leafType == LeafObjectType.APPLICATION) {
                subjectType = SubjectType.APP;
                typeCondition = dictionary.condition(appType);
            } else {
                throw new IllegalArgumentException("Unexpected subject type.");
            }
        } else {        
            class TypeFinder extends DefaultPredicateVisitor implements IExpressionVisitor {
                private SubjectType subjectType;
                private IElementType dictionaryElementType;
                private LeafObjectType leafType;
                @Override
                public void visit(IRelation pred) {
                    pred.getLHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
                    pred.getRHS().acceptVisitor(this, IExpressionVisitor.PREORDER);
                }
                @Override
                public void visit(IPredicateReference pred) {
                    if (pred instanceof SpecReference) {
                        SpecReference ref = (SpecReference)pred;
                        if (!ref.isReferenceByName()) {
                            Long id = ref.getReferencedID();
                            IHasId obj = byId.get(id);
                            if (obj instanceof IDSpec) {
                                ((IDSpec)obj).getPredicate().accept(this, IPredicateVisitor.PREORDER);
                            }
                        }
                    }
                }

                public void visit(IAttribute attribute) {
                    if (subjectType != null) {
                        return;
                    }
                    String typeName = attribute.getObjectTypeName();
                    if (SpecType.USER.getName().equals(typeName)) {
                        subjectType = SubjectType.USER;
                        dictionaryElementType = userType;
                        leafType = LeafObjectType.USER;
                    } else if (SpecType.HOST.getName().equals(typeName)) {
                        subjectType = SubjectType.HOST;
                        dictionaryElementType = hostType;
                        leafType = LeafObjectType.HOST;
                    } else if (SpecType.APPLICATION.getName().equals(typeName)) {
                        subjectType = SubjectType.APP;
                        dictionaryElementType = appType;
                        leafType = LeafObjectType.APPLICATION;
                    }
                }
                public void visit(Constant constant) {
                }
                public void visit(IFunctionApplication constant) {
                }
                public void visit(IExpressionReference ref) {
                }
                public void visit(IExpression expression) {
                }
                public SubjectType getSubjectType() {
                    return subjectType;
                }
                public IElementType getDictionaryElementType() {
                    return dictionaryElementType;
                }
                public LeafObjectType getLeafType() {
                    return leafType;
                }
            }
            TypeFinder typeFinder = new TypeFinder();
            pred.accept(typeFinder, IPredicateVisitor.PREORDER);

            if (typeFinder.getSubjectType() == null) {
                throw new IllegalArgumentException("Unexpected subject type.");
            }
            subjectType = typeFinder.getSubjectType();
            typeCondition = dictionary.condition(typeFinder.getDictionaryElementType());
            leafType = typeFinder.getLeafType();
        }
        CompositePredicate subj = new CompositePredicate(
            BooleanOp.AND
        ,   toDictionaryPredicate(pred, byId, subjectType)
        );
        subj.addPredicate(typeCondition);
        return processTerminalObjects(subj, leafType, limit);
    }
    
    public IPredicate toDictionaryPredicate(IPredicate pred, final Map<Long,? extends IHasId> byId, final SubjectType subjectType) throws DictionaryException {
        final IElementType dictionaryElementType;

        if ( subjectType == SubjectType.USER ) {
            userType = dictionary.getType(ElementTypeEnumType.USER.getName());
            dictionaryElementType = userType;
        } else if ( subjectType == SubjectType.HOST ) {
            hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            dictionaryElementType = hostType;
        } else if ( subjectType == SubjectType.APP ) {
            appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
            dictionaryElementType = appType;
        } else {
            // Failure of this assertion indicates an introduction of a new subject type.
            assert false; // The code above verifies that we hava a subject type.
            throw new IllegalStateException("Unexpected subject type.");
        }

        // Walk the predicate and construct the corresponding dictionary predicate
        DictionaryHelperPredicateVisitor v = new DictionaryHelperPredicateVisitor(
            dictionaryElementType, byId, false
        );
        pred.accept(v , IPredicateVisitor.POSTORDER );
        return v.getResult();
    }
    
    public List<LeafObject> getMatchingContacts(IPredicate pred, int limit) throws DictionaryException {
        // Walk the predicate and construct the corresponding dictionary predicate
        DictionaryHelperPredicateVisitor v = new DictionaryHelperPredicateVisitor(
            contactType, null, true
        );
        pred.accept(v, IPredicateVisitor.POSTORDER );
        CompositePredicate subj = new CompositePredicate(
            BooleanOp.AND
        ,   v.getResult()
        );
        subj.addPredicate(dictionary.condition(contactType));
        return processTerminalObjects(subj, LeafObjectType.CONTACT, limit);
    }
    
    class DictionaryHelperPredicateVisitor implements IPredicateVisitor {
        private final IElementType dictionaryElementType;
        private final Map<Long,? extends IHasId> byId;
        boolean allowEmail;
        private final Stack<IPredicate> stack = new Stack<IPredicate>();
        
        DictionaryHelperPredicateVisitor(
            IElementType dictionaryElementType
        ,   Map<Long,? extends IHasId> byId
        ,   boolean allowEmail) {
            this.dictionaryElementType = dictionaryElementType;
            this.byId = byId;
            this.allowEmail = allowEmail;
        }
        public IPredicate getResult() {
            if (stack.size() != 1) {
                throw new IllegalStateException("Stack must have one element for getResult operation");
            }
            return stack.pop();
        }
        public void visit(ICompositePredicate pred, boolean preorder) {
            assert stack.size() >= pred.predicateCount();
            LinkedList<IPredicate> tmp = new LinkedList<IPredicate>();
            // reverse the top predicateCount() elements of the stack
            for ( int i = 0 ; i != pred.predicateCount() ; i++ ) {
                tmp.addFirst( stack.pop() );
            }
            if ( tmp.isEmpty() ) {
                throw new NoSuchElementException("Detected a composite predicate with zero elements.");
            }
            stack.push( new CompositePredicate(pred.getOp(), tmp));
        }
        public void visit(IPredicateReference pred) {
            SpecReference ref = (SpecReference)pred;
            if ( ref.isReferenceByName() ) {
                throw new IllegalArgumentException("reference by name is unexpected.");
            }
            if (byId == null) {
                throw new IllegalArgumentException("Predicate needs to be fully resolved when byId is null.");
            }
            SpecBase spec = (SpecBase)byId.get( ref.getReferencedID() );
            spec.getPredicate().accept( this, POSTORDER );
        }
        public void visit(IRelation pred) {
            SubjectAttribute attrLHS = null;
            if ( pred.getLHS() instanceof SubjectAttribute ) {
                attrLHS = (SubjectAttribute) pred.getLHS();
            }
            SubjectAttribute attrRHS = null;
            if (pred.getRHS () instanceof SubjectAttribute) {
                attrRHS = (SubjectAttribute) pred.getRHS ();
            }
            if ( attrLHS != null && attrRHS != null ) {
                throw new IllegalStateException("attr =?= attr construct detected.");
            }
            SubjectAttribute subjAttr;
            String subjVal;
            if ( attrLHS != null ) {
                subjAttr = attrLHS;
                if ( pred.getRHS() instanceof LocationReference ) {
                    throw new IncompleteMatchException("Location-based property is found - cannot preview.");
                }
                if (!(pred.getRHS() instanceof Constant)) {
                    stack.push(PredicateConstants.TRUE);
                    return;
                }
                subjVal = "" + ((Constant)pred.getRHS()).getValue().getValue();
            } else {
                subjAttr = attrRHS;
                if ( pred.getLHS() instanceof LocationReference ) {
                    throw new IncompleteMatchException("Location-based property is found - cannot preview.");
                }
                if (!(pred.getLHS() instanceof Constant)) {
                    stack.push(PredicateConstants.TRUE);
                    return;
                }
                 subjVal  = "" + ((Constant)pred.getLHS()).getValue().getValue();
            }
            try {
                stack.push( makeSubject( subjAttr, pred.getOp(), subjVal ) );
            } catch (InvalidGroupException e) {
            	if (pred.getOp() == RelationOp.EQUALS) {
            		stack.push(PredicateConstants.FALSE);
            		log.error ("Invalid group \'" + subjVal + "\' in predicate \'" + attrLHS + " = " + subjVal + "\'.  This predicate will be resolved to FALSE.");
            	} else if (pred.getOp() == RelationOp.NOT_EQUALS) {
            		stack.push(PredicateConstants.TRUE);
            		log.error ("Invalid group \'" + subjVal + "\' in predicate \'" + attrLHS + " != " + subjVal + "\'.  This predicate will be resolved to TRUE.");       	
            	} else {
            		log.error ("Invalid group \'" + subjVal + "\' in predicate \'" + attrLHS + " " + pred.getOp() + " " + subjVal + "\'.");
            		throw new IllegalArgumentException ("Invalid group \'" + subjVal + "\' in predicate \'" + attrLHS + " " + pred.getOp() + " " + subjVal + "\'.", e);
            	}
            } catch (DictionaryException e) {
                throw new IllegalArgumentException("Visit predicate failed", e );
            }
        }
        public void visit(IPredicate pred) {
            stack.push( pred );
        }
        private void ensureEqNeq( RelationOp op ) {
            if ( op != RelationOp.EQUALS && op != RelationOp.NOT_EQUALS ) {
                throw new IllegalStateException("Illegal operation detected: "+op );
            }
        }
        private IPredicate groupBasedPredicate(IGroup group, RelationOp op) throws DictionaryException {
            IPredicate pqlSubj = null;
            if ( group != null ) {
                pqlSubj = group.getTransitiveMembershipPredicate();
            } else {
            	throw new InvalidGroupException("Subject predicate references an unknown group.");
            }
            if (op == RelationOp.EQUALS) {
                return pqlSubj;
            } else {
                return new CompositePredicate(BooleanOp.NOT, pqlSubj);
            }
        }
        private IPredicate makeSubject(SubjectAttribute attr, RelationOp op, String value) throws DictionaryException {
            if (attr == null) {
                throw new NullPointerException("attr");
            }
            if (value == null) {
                throw new NullPointerException("value");
            }
            if (op == null) {
                throw new NullPointerException("operator");
            }
            if (builtinFields.containsKey(attr)) {
                ensureEqNeq( op );
                IElementField field = (IElementField)builtinFields.get(attr);
                return new Relation(op, field, Constant.build(value) );
            } else if ( attr == SubjectAttribute.USER_ID
                     || attr == SubjectAttribute.HOST_ID
                     || attr == SubjectAttribute.APP_ID
                     || attr == SubjectAttribute.CONTACT_ID) {
               ensureEqNeq( op );
               IAttribute idRef = dictionary.internalKeyAttribute();
               return new Relation(op, idRef, Constant.build( Long.valueOf(value).longValue() ) );
            } else if (attr == SubjectAttribute.USER_LDAP_GROUP || attr == SubjectAttribute.HOST_LDAP_GROUP) {
                ensureEqNeq( op );
                return groupBasedPredicate(dictionary.getGroup(value, dictionary.getLatestConsistentTime()), op);
            } else if (attr == SubjectAttribute.USER_LDAP_GROUP_ID || attr == SubjectAttribute.HOST_LDAP_GROUP_ID) {
                ensureEqNeq( op );
                return groupBasedPredicate(dictionary.getGroup(Long.valueOf(value), dictionary.getLatestConsistentTime()), op);
            } else if ( attr == SubjectAttribute.INET_ADDRESS || attr == SubjectAttribute.LOCATION ) {
                throw new IncompleteMatchException( "IP-based property is found - cannot preview." );
            } else if ( !allowEmail && (attr == SubjectAttribute.USER_EMAIL || attr == SubjectAttribute.USER_EMAIL_DOMAIN)) {
                throw new IncompleteMatchException( "email-based property is found - cannot preview." );
            } else {
                try {
                    return new Relation(op, dictionaryElementType.getField(attr.getName()), Constant.build(value));
                } catch (IllegalArgumentException e) {
                    throw new DictionaryException("Invalid attribute: " + attr.getName(), e);
                }
            }
        }
    }

    /**
     * Gets a <code>Collection</code> of <code>LeafObject</code>s
     * for groups matching an <code>IPredicate</code>. 
     * @param pred the predicate defining groups to match.
     * @param type the type of objects that must be present in the groups.
     * @param limit the max number of items to return.
     * @return a <code>List</code> of <code>LeafObject</code>s
     * corresponding to groups matching the predicate.
     * @throws DictionaryException when the operation cannot complete.
     */
    public List<LeafObject> getMatchingGroups(IPredicate pred, LeafObjectType type, int limit) throws DictionaryException {
        final Stack<IPredicate> stack = new Stack<IPredicate>();
        final IElementType dictionaryElementType;

        if ( type == LeafObjectType.USER_GROUP ) {
            dictionaryElementType = userType;
        } else if ( type == LeafObjectType.HOST_GROUP ) {
            dictionaryElementType = hostType;
        } else {
            throw new IllegalStateException("Unexpected group type.");
        }

        pred.accept( new IPredicateVisitor() {
            /**
             * @see IPredicateVisitor#visit(ICompositePredicate, boolean)
             */
            public void visit( ICompositePredicate pred, boolean preorder ) {
                assert stack.size() >= pred.predicateCount();
                LinkedList<IPredicate> tmp = new LinkedList<IPredicate>();
                // reverse the top predicateCount() elements of the stack
                for ( int i = 0 ; i != pred.predicateCount() ; i++ ) {
                    tmp.addFirst( stack.pop() );
                }
                if ( tmp.isEmpty() ) {
                    throw new NoSuchElementException("Detected a composite predicate with zero elements.");
                }
                stack.push( new CompositePredicate(pred.getOp(), tmp));
            }
            /**
             * @see IPredicateVisitor#visit(IPredicateReference)
             */
            public void visit( IPredicateReference pred ) {
                throw new IllegalArgumentException(
                    "Getting dictionary groups for predicates with references is not allowed."
                );
            }
            /**
             * @see IPredicateVisitor#visit(IRelation)
             */
            public void visit( IRelation rel ) {
                IExpression lhs = rel.getLHS();
                IExpression rhs = rel.getRHS();
                boolean lhsConst = lhs instanceof Constant;
                boolean rhsConst = rhs instanceof Constant;
                if (lhsConst && rhsConst) {
                    stack.push(rel);
                } else if (lhsConst && !rhsConst) {
                    processAttrConstRelation(rel.getOp(), rhs, lhs);
                } else if (!lhsConst && rhsConst) {
                    processAttrConstRelation(rel.getOp(), lhs, rhs);
                } else {
                    throw new IllegalArgumentException(
                        "Group predicates must contain only 'attribute==constant' relations."
                    );
                }
            }
            private void processAttrConstRelation(RelationOp op, IExpression attr, IExpression constant) {
                if (attr == SubjectAttribute.HOST_LDAP_GROUP
                 || attr == SubjectAttribute.USER_LDAP_GROUP) {
                    stack.push(
                        dictionary.uniqueNameAttribute().buildRelation(op, constant)
                    );
                } else if (attr == SubjectAttribute.HOST_LDAP_GROUP_ID
                        || attr == SubjectAttribute.USER_LDAP_GROUP_ID) {
                    stack.push(
                        dictionary.internalKeyAttribute().buildRelation(op, constant)
                    );
                } else if (attr == SubjectAttribute.HOST_LDAP_GROUP_DISPLAY_NAME
                 || attr == SubjectAttribute.USER_LDAP_GROUP_DISPLAY_NAME ) {
                    stack.push(
                        dictionary.displayNameAttribute().buildRelation(op, constant)
                    );
                }
                else {
                    throw new IllegalArgumentException(
                        "Grup predicates must contain references only to group attributes."
                    );
                }
            }
            /**
             * @see IPredicateVisitor#visit(IPredicate)
             */
            public void visit( IPredicate pred ) {
                stack.push(pred);
            }
        }, IPredicateVisitor.POSTORDER);

        assert stack.size() == 1;
        // Get the fully resolved predicate
        IPredicate groupPred = stack.pop();

        // Query the groups; first try enumerated, then structural groups.
        Page page = new Page(0, limit);
        List<LeafObject> res = new ArrayList<LeafObject>(limit);
        IDictionaryIterator<IMGroup> iter = dictionary.getEnumeratedGroups(
            groupPred, dictionaryElementType, dictionary.getLatestConsistentTime(), page
        );
        try {
            while (iter.hasNext()) {
                res.add(convertToLeaf(iter.next(), type));
            }
        } finally {
            iter.close();
        }
        
        iter = dictionary.getStructuralGroups(
            groupPred, dictionaryElementType, dictionary.getLatestConsistentTime(), page
        );
        try {
            while (iter.hasNext()) {
                res.add(convertToLeaf(iter.next(), type));
            }
        } finally {
            iter.close();
        }
        return res;
    }

    /**
     * Extracts <code>LeafObject</code>s from a <code>Collection</code>
     * of <code>IElement</code> objects.
     * @param tos a <code>Collection</code> of
     * <code>IElement</code> objects to be processed.
     * @param type the type to be created.
     * @param limit the upper limit of the number of items to return.
     * Negative values mean that there is no limit.
     * @return a <code>List</code> of <code>LeafObject</code>s.
     */
    public List<LeafObject> processTerminalObjects( IPredicate subj, LeafObjectType type, int limit ) throws DictionaryException {
        if ( subj == null ) {
            return null;
        }
        if ( ( type == null ) ||
             ( type != LeafObjectType.APPLICATION && type != LeafObjectType.USER && 
               type != LeafObjectType.HOST && type != LeafObjectType.CONTACT ) ) {
            throw new IllegalArgumentException("type");
        }        
        IDictionaryIterator<IMElement> iter;
        if (limit >= 0) {
 	        iter= dictionary.query(subj, dictionary.getLatestConsistentTime(), (Order[])null, limit);
        } else {
	        iter= dictionary.query(subj, dictionary.getLatestConsistentTime(), (Order[])null, (Page)null);
        }

        List<LeafObject> res = new ArrayList<LeafObject>();
        try {
            while( iter.hasNext() ) {
                res.add(convertToLeaf(iter.next()));
            }
        } finally {
            iter.close();
        }
        Collections.sort( res, new Comparator<LeafObject>() {
            private final Collator collator = Collator.getInstance();
            public int compare( LeafObject lhs, LeafObject rhs ) {
                return collator.compare( lhs.getName(), rhs.getName() );
            }
        });
        return res;
    }

    public List<LeafObject> getLeafObjectsForIds(long[] elementIds, long[] userGroupIds, long[] hostGroupIds) throws DictionaryException {
        Map<Long,LeafObjectType> ids = new HashMap<Long,LeafObjectType>();
        for (int i = 0 ; i != elementIds.length ; i++) {
            ids.put(elementIds[i], null);
        }
        for (int i = 0 ; i != userGroupIds.length ; i++) {
            ids.put(userGroupIds[i], LeafObjectType.USER_GROUP);
        }
        for (int i = 0 ; i != hostGroupIds.length ; i++) {
            ids.put(hostGroupIds[i], LeafObjectType.HOST_GROUP);
        }
        List<LeafObject> res = new ArrayList<LeafObject>(ids.size());
        IDictionaryIterator<IMElementBase> iter = dictionary.getElementsById(ids.keySet(), dictionary.getLatestConsistentTime());
        try {
            while (iter.hasNext()) {
                IElementBase element = iter.next();
                Long id = element.getInternalKey();
                assert ids.containsKey(id); // Otherwise the query is wrong!
                LeafObjectType type = (LeafObjectType)ids.get(id);
                if (type == null) {
                    if (!(element instanceof IElement)) {
                        throw new IllegalStateException(
                            "An ID of a non-element is passed for element ID query"
                        );
                    }
                    res.add(convertToLeaf((IElement)element));
                } else {
                    if (!(element instanceof IGroup)) {
                        throw new IllegalStateException(
                            "An ID of a non-group element is passed for group ID query"
                        );
                    }
                    res.add(convertToLeaf( (IGroup)element, type));
                }
            }
        } finally {
            iter.close();
        }
        return res;
    }

    private LeafObject convertToLeaf(IElement element) {
        IElementType elementType = element.getType();
        LeafObject res;
        if (elementType.equals(userType)) {
            res = new LeafObject(LeafObjectType.USER);
            String uid = (String)element.getValue(userSysId);
            if(uid == null){
            	uid = (String)element.getValue(userUnixId);	
            }
            res.setUid(uid);
        } else if (elementType.equals(contactType)) {
            res = new LeafObject(LeafObjectType.CONTACT);
//            String id = (String)element.getValue(contactEmail);
//            res.setUid(id);
        } else if (elementType.equals(hostType)) {
            res = new LeafObject(LeafObjectType.HOST);
            String id = (String)element.getValue(hostSysId);
            if(id == null){
            	id = (String)element.getValue(hostUnixId);	
            }
            res.setUid(id);
        } else if (elementType.equals(appType)) {
            res = new LeafObject(LeafObjectType.APPLICATION);
            res.setUid((String)element.getValue(appSysId));
        } else {
            throw new IllegalArgumentException("Element of unknown type: "+elementType);
        }
        res.setName( element.getDisplayName() );
        res.setId( element.getInternalKey() );
        res.setUniqueName( element.getUniqueName() );
        res.setDomainName( element.getEnrollment().getDomainName() );
        return res;
    }

    private LeafObject convertToLeaf(IGroup group, LeafObjectType type) {
        LeafObject res = new LeafObject(type);
        res.setName(group.getDisplayName());
        res.setId(group.getInternalKey());
        res.setUniqueName(group.getUniqueName());
        res.setUid(group.getUniqueName());
        res.setDomainName( group.getEnrollment().getDomainName() );
        return res;
    }

    public DictionaryHelper(ServerSpecManager specManager, IDictionary dictionary) {
        this.dictionary = dictionary;
        try {
            // Initialize types
            userType = dictionary.getType(ElementTypeEnumType.USER.getName());
            contactType = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
            hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
            // Initialize the map of built-in attributes
            builtinFields.put(SubjectAttribute.USER_NAME, userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName()));
            builtinFields.put( SubjectAttribute.USER_UID, userSysId = userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName()));
            userUnixId = userType.getField(UserReservedFieldEnumType.UNIX_ID.getName());
            builtinFields.put( SubjectAttribute.HOST_NAME, hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName()));
            builtinFields.put( SubjectAttribute.HOST_UID, hostSysId = hostType.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName()));
            hostUnixId = hostType.getField(ComputerReservedFieldEnumType.UNIX_ID.getName());
            builtinFields.put( SubjectAttribute.APP_NAME, appType.getField(ApplicationReservedFieldEnumType.UNIQUE_NAME.getName()));
            builtinFields.put( SubjectAttribute.APP_UID, appSysId = appType.getField(ApplicationReservedFieldEnumType.SYSTEM_REFERENCE.getName()));
        } catch ( DictionaryException e ) {
            throw new IllegalArgumentException("Failed getting dictionary type", e);
        }
    }

}
