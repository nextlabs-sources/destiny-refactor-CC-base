package com.bluejungle.pf.destiny.policymap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.DictionaryHelper;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * This contains basic components that are used to resolve policy references. This
 * should typically be subclassed by any class that requires to resolve references contained
 * in a policy as a first step.
 * 
 * @author ssen
 *
 */
public class ReferenceResolver {
    private final Log log = LogFactory.getLog(ReferenceResolver.class.getName());

    // Working variables
    private final SortedMap<Long,IParsedEntity> specById = new TreeMap<Long,IParsedEntity>();
    private final SortedMap<Long,IParsedEntity> policyById = new TreeMap<Long, IParsedEntity>();
    private long[] policyIds = null;
    private IDPolicy[] parsedPolicies = null;

    private IDictionary dictionary;
    private Date consistentTime;
    private DictionaryHelper dictHelper;
    private IElementType userType;
    private IElementType contactType;
    private IElementType hostType;
    
    private final Collection<? extends IHasPQL> deployedEntities;
    private final ServerSpecManager specManager;
    private final IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    public static final Set<IExpression> userGroupAttributes = new HashSet<IExpression>();
    
    static {
        userGroupAttributes.add(ResourceAttribute.OWNER_GROUP);
        userGroupAttributes.add(ResourceAttribute.PORTAL_CREATED_BY_GROUP);
        userGroupAttributes.add(ResourceAttribute.PORTAL_MODIFIED_BY_GROUP);
    }
    
    public ReferenceResolver(Collection<? extends IHasPQL> deployedEntities) {
        if (deployedEntities == null) {
            throw new NullPointerException("deployedEntities");
        }
        this.deployedEntities = deployedEntities;
        
        specManager = componentManager.getComponent(ServerSpecManager.COMP_INFO);
        dictionary = componentManager.getComponent(Dictionary.COMP_INFO );
        dictHelper = new DictionaryHelper(specManager, dictionary);

        try {
            consistentTime = dictionary.getLatestConsistentTime();
        } catch ( DictionaryException e ) {
            throw new IllegalStateException(
                    "Unable to get a consistent time from the dictionary: " + 
                    e.getMessage());
        }    
        
        try {
            userType = dictionary.getType(ElementTypeEnumType.USER.getName());
            contactType = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
            hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        } catch (DictionaryException de) {
            throw new IllegalStateException(
                    "Dictionary does not contain a definition for a required type: " + 
                    de.getMessage());
        }
    }
    
    public IEnrollment[] initDomains(String[] domainNames) {
        IEnrollment[] domains = null;

        int i = 0;
        List<IEnrollment> knownDomains = new ArrayList<IEnrollment>();
        boolean[] notFound = new boolean[domainNames.length];
        try {
            for ( ; i != domainNames.length ; i++) {
                IEnrollment tmp = dictionary.getEnrollment(domainNames[i]);
                if (tmp != null) {
                    knownDomains.add(tmp);
                } else {
                    notFound[i] = true;
                    getLog().warn("Domain '"+domainNames[i]+"' is not enrolled.");
                }
            }
            domains = knownDomains.toArray(new IEnrollment[knownDomains.size()]);
            if (getLog().isInfoEnabled()) {
                StringBuffer msg = new StringBuffer("Initialized a PolicyReferenceResolver for the following list of domains: ");
                for ( i = 0 ; i != domainNames.length ; i++ ) {
                    if (i != 0) {
                        msg.append(", ");
                    }
                    msg.append(domainNames[i]);
                    if (notFound[i]) {
                        msg.append("(UNDEFINED)");
                    }
                }
                getLog().info(msg);
            }
            if (domains.length == 0) {
                getLog().warn("None of the specified domains are enrolled as of " +consistentTime);
            }
        } catch (DictionaryException e) {
            throw new IllegalStateException("Unable to query domain: " + 
                        domainNames[i]+"; "+e.getMessage());
        }

        return domains;
    }

    public void resolve() throws PQLException {
        resolve(null, null);
    }

    public void resolve(String partialNameMatch) throws PQLException {
        resolve(partialNameMatch, Pattern.compile(partialNameMatch, Pattern.CASE_INSENSITIVE));
    }

    private void resolve(final String partialNameMatch, final Pattern partialNameMatchPattern) throws PQLException {
        // Parse all deployment entities, build a map by ID,
        // and record all references to dictionary items by ID
        for (final IHasPQL entity : deployedEntities) {
            DomainObjectBuilder.processInternalPQL(entity.getPql(), new DefaultPQLVisitor() {
                /**
                 * @see IPQLVisitor#visitPolicy(DomainObjectDescriptor, IDPolicy)
                 */
                public void visitPolicy( DomainObjectDescriptor descriptor, final IDPolicy policy ) {
                    policy.setAccessPolicy(null);
                    IParsedEntity parsed = new IParsedEntity() {
                        /* This flag indicates that the corresponding entity has been resolved. */
                        private boolean resolved = false;
                        /**
                         * @see IParsedEntity#getParsed()
                         */
                        public IHasId getParsed() {
                            return policy;
                        }
                        /**
                         * @see IParsedEntity#getUnparsed()
                         */
                        public IHasPQL getUnparsed() {
                            return entity;
                        }
                        /**
                         * @see IParsedEntity#getName()
                         */
                        public String getName() {
                            return "POLICY "+policy.getName();
                        }
                        /**
                         * @see IParsedEntity#resolveDependencies(Set)
                         */
                        public void resolveDependencies(Set<Long> currentlyBeingResolved) {
                            if (resolved) {
                                return;
                            }
                            if (!currentlyBeingResolved.isEmpty()) {
                                throw new IllegalStateException("Policies cannot be resolved as dependents of other entities.");
                            }
                            Predicates.ITransformer transformer = new ReferenceResolvingTransformer(currentlyBeingResolved);
                            IPredicate pred;
                            pred = policy.getTarget().getActionPred();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.getTarget().setActionPred(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            pred = policy.getTarget().getFromResourcePred();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.getTarget().setFromResourcePred(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            pred = policy.getTarget().getToResourcePred();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.getTarget().setToResourcePred(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            pred = policy.getTarget().getSubjectPred();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.getTarget().setSubjectPred(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            pred = policy.getTarget().getToSubjectPred();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.getTarget().setToSubjectPred(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            pred = policy.getDeploymentTarget();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                policy.setDeploymentTarget(transformed!=null ? transformed: PredicateConstants.TRUE);
                            }
                            resolved = true;
                        }
                    };

                    // We will limit the search using the policy name fragment - 
                    // only policies with name that contains this fragment are considered
                    if (partialNameMatch == null || partialNameMatch.length() == 0) {
                        policyById.put(policy.getId(), parsed);
                    } else {
                        Matcher m = partialNameMatchPattern.matcher(policy.getName());

                        if (m.find()) {
                            policyById.put(policy.getId(), parsed);
                            
                            getLog().debug("The policy: " + policy.getName() +
                                           " is considered in the list for resolving reference " +
                                           " since it contains the filter: " +
                                           partialNameMatch);
                        } else {
                            getLog().debug("The policy: " + policy.getName() +
                                           " is NOT considered in the list for resolving reference " +
                                           " since it DOES NOT CONTAIN the filter: "+
                                           partialNameMatch);
                        }
                    }
                }

                /**
                 * @see IPQLVisitor#visitComponent(DomainObjectDescriptor, IPredicate)
                 */
                public void visitComponent(DomainObjectDescriptor dod, IPredicate pred) {
                    final SpecBase spec = new SpecBase(
                        specManager
                    ,   SpecType.ILLEGAL
                    ,   dod.getId()
                    ,   dod.getName()
                    ,   dod.getDescription()
                    ,   dod.getStatus()
                    ,   pred
                    ,   dod.isHidden()
                        );
                    IParsedEntity parsed = new IParsedEntity() {
                        /* This flag indicates that the corresponding entity has been resolved. */
                        private boolean resolved = false;
                        /**
                         * @see IParsedEntity#getParsed()
                         */
                        public IHasId getParsed() {
                            return spec;
                        }
                        /**
                         * @see IParsedEntity#getUnparsed()
                         */
                        public IHasPQL getUnparsed() {
                            return entity;
                        }
                        /**
                         * @see IParsedEntity#getName()
                         */
                        public String getName() {
                            return spec.getSpecType()+" "+spec.getName();
                        }
                        /**
                         * @see IParsedEntity#resolveDependencies(Set)
                         */
                        public void resolveDependencies(Set<Long> currentlyBeingResolved) {
                            if (resolved) {
                                return;
                            }
                            if (currentlyBeingResolved.contains(spec.getId())) {
                                throw new IllegalStateException("Detected circular reference while resolving "+spec.getName());
                            }
                            currentlyBeingResolved.add(spec.getId());
                            Predicates.ITransformer transformer = new ReferenceResolvingTransformer(currentlyBeingResolved);
                            IPredicate pred = spec.getPredicate();
                            if (pred != null) {
                                IPredicate transformed = Predicates.transform(pred, transformer);
                                spec.setPredicate(transformed != null ? transformed : PredicateConstants.TRUE);
                            }
                            currentlyBeingResolved.remove(spec.getId());
                            resolved = true;
                        }
                    };
                    specById.put( spec.getId(), parsed);
                }
            }
                                                   );
        }

        policyIds = new long[policyById.size()];
        parsedPolicies = new IDPolicy[policyById.size()];

        int i = 0;
        for ( Map.Entry<Long,IParsedEntity> entry : policyById.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalStateException("Policy with null ID is detected.");
            }

            // Policy IDs are expected to be in sorted order because they come from a sorted map.
            policyIds[i] = entry.getKey().longValue();
            assert policyIds[i] == 0 || policyIds[i] > policyIds[i-1];

            IParsedEntity parsed = entry.getValue();
            try {
                parsed.resolveDependencies(new HashSet<Long>());
                parsedPolicies[i++] = (IDPolicy)parsed.getParsed();
            } catch (ReferenceResolutionException rre) {
                getLog().error("Policy deployment is not consistent: " + parsed.getName() + " references unknown ID:" + rre.getUnresolvedId());

                throw new IllegalStateException("Map building failed: unresolved ID "+rre.getUnresolvedId());
            }
        }
    }

    /**
     * This transformer passes through all non-reference predicates,
     * and replaces all predicate references with the referenced targets.
     */
    class ReferenceResolvingTransformer extends Predicates.DefaultTransformer {
        /**
         * This <code>Set</code> contains IDs of entities currently being resolved.
         */
        private final Set<Long> currentlyBeingResolved;
        /**
         * The constructor supplies the set of entities being resolved.
         * @param currentlyBeingResolved the <code>Set</code> of IDs of
         * entities that are currently being resolved.
         */
        public ReferenceResolvingTransformer(Set<Long> currentlyBeingResolved) {
            this.currentlyBeingResolved = currentlyBeingResolved;
        }
        /**
         * @see ITransformer#transformReference(IPredicateReference)
         */
        public IPredicate transformReference(IPredicateReference pred) {
            if (pred instanceof IDSpecRef) {
                IParsedEntity referenced = resolve((IDSpecRef)pred);
                referenced.resolveDependencies(currentlyBeingResolved);
                IHasId spec = referenced.getParsed();
                assert spec != null; // spec comes from the parser; parser does not return nulls.
                if (!(spec instanceof IDSpec)) {
                    throw new IllegalStateException("Unexpected reference: "+spec.getClass());
                }
                return ((IDSpec)spec).getPredicate();
            } else {
                return super.transformReference(pred);
            }
        }
        /**
         * @see DefaultTransformer#transformRelation(IRelation)
         */
        public IPredicate transformRelation(IRelation rel) {
            IExpression refGroup = null;
            if (userGroupAttributes.contains(rel.getLHS())) {
                refGroup = rel.getRHS();
            } else if (userGroupAttributes.contains(rel.getRHS())) {
                refGroup = rel.getLHS();
            }
            IExpression ref;
            if (refGroup instanceof IDSpecRef) {
                IParsedEntity referenced = resolve((IDSpecRef)refGroup);
                referenced.resolveDependencies(currentlyBeingResolved);
                ref = new PredicateReferenceExpression(
                    ((IDSpec)referenced.getParsed()).getPredicate());
                if (userGroupAttributes.contains(rel.getLHS())) {
                    ((Relation)rel).setRHS(ref);
                } else if (userGroupAttributes.contains(rel.getRHS())) {
                    ((Relation)rel).setLHS(ref);
                }
            }
            return rel;
        }
        /**
         * Resolves spec references; throws exceptions if the reference cannot be resolved.
         * @param ref the reference to resolve.
         * @return the corresponding <code>IParsedEntity</code>.
         */
        private IParsedEntity resolve(IDSpecRef ref) {
            if (ref.isReferenceByName()) {
                throw new IllegalStateException("Reference by name " + ref.getPrintableReference() + " is unexpected.");
            }
            IParsedEntity referenced = specById.get(ref.getReferencedID());
            if (referenced == null) {
                throw new ReferenceResolutionException(ref.getReferencedID());
            }
            return referenced;
        }
    }

    /**
     * We use anonymous instances of this interface to store information
     * about policies and specs.
     */
    public static interface IParsedEntity {

        /**
         * The parsed entity - a policy or a spec.
         * @return the corresponding parsed entity, which is a policy or a spec.
         */
        IHasId getParsed();

        /**
         * The corresponding unparsed entity - a <code>DeploymentEntity</code> object.
         * @return the corresponding <code>DeploymentEntity</code> object.
         */
        IHasPQL getUnparsed();

        /**
         * The name of the parsed (and unparsed) entity.
         * @return name of the parsed (and unparsed) entity.
         */
        String getName();

        /**
         * Resolve the dependencies for this object. Specs simply resolve their dependencies
         * by ID, while policies resolve dependencies of each of their predicates.
         * @param currentlyBeingResolved a <code>Set</code> containing IDs (of type <code>Long</code>)
         * of the objects that are currently being resolved. This <code>Set</code> is used for
         * detecting circular references.
         * @throws ReferenceResolutionException when a reference cannot be resolved.
         */
        void resolveDependencies(Set<Long> currentlyBeingResolved);
    }

    /**
     * Instances of this expression class hold references to predicates.
     */
    public static class PredicateReferenceExpression implements IExpression {
        /** The referenced predicate. */
        private final IPredicate predicate;
        /**
         * Builds a reference expression for the specific predicate.
         * @param predicate the predicate to which this reference is pointing.
         */
        public PredicateReferenceExpression(IPredicate predicate) {
            if (predicate == null) {
                throw new NullPointerException("predicate");
            }
            this.predicate = predicate;
        }
        /**
         * @see IExpression#acceptVisitor(IExpressionVisitor, IExpressionVisitor.Order)
         */
        public void acceptVisitor( IExpressionVisitor visitor, IExpressionVisitor.Order order ) {
            visitor.visit(this);
        }
        /**
         * @see IExpression#buildRelation(RelationOp, IExpression)
         */
        public IRelation buildRelation( RelationOp op, IExpression rhs ) {
            throw new UnsupportedOperationException("IExpression#buildRelation(RelationOp, IExpression)");
        }
        /**
         * @see IExpression#evaluate(IArguments)
         */
        public IEvalValue evaluate( IArguments arg ) {
            throw new UnsupportedOperationException("IExpression#evaluate(IArguments)");
        }
        /**
         * Returns the referenced predicate.
         * @return the referenced predicate.
         */
        public IPredicate getReferencedPredicate() {
            return predicate;
        }
    }

    /**
     * This exception is thrown when a reference by ID cannot be resolved.
     */
    public static class ReferenceResolutionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * This field represents a referenced ID which is unresolved.
         */
        private final Long unresolvedId;

        /**
         * Creates a new <code>ReferenceResolutionException</code>.
         * @param unresolvedId the unresolved ID.
         */
        public ReferenceResolutionException(Long unresolvedId) {
            super("Unresolved ID: "+unresolvedId);
            this.unresolvedId = unresolvedId;
        }

        /**
         * Gets the unresolved ID.
         * @return the unresolved ID.
         */
        public Long getUnresolvedId() {
            return unresolvedId;
        }

        /**
         * Makes this object printable for debugging.
         */
        public String toString() {
            return getMessage();
        }

    }

    private Log getLog() {
        return log;
    }

    public SortedMap<Long, IParsedEntity> getPolicyById() {
        return policyById;
    }

    public IDPolicy[] getParsedPolicies() {
        return parsedPolicies;
    }

    public long[] getPolicyIds() {
        return policyIds;
    }
}
