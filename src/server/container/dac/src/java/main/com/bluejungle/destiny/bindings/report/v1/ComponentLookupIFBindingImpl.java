/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.types.PositiveInteger;

import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.HostMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.HostMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.ResourceClassMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.ResourceClassMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrSortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup;
import com.bluejungle.destiny.interfaces.report.v1.ComponentLookupIF;
import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.basic_faults.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.basic_faults.v1.ServiceNotReadyFault;
import com.bluejungle.destiny.types.hosts.v1.Host;
import com.bluejungle.destiny.types.hosts.v1.HostList;
import com.bluejungle.destiny.types.hosts.v1.HostQueryFieldName;
import com.bluejungle.destiny.types.hosts.v1.HostQuerySpec;
import com.bluejungle.destiny.types.hosts.v1.HostQueryTerm;
import com.bluejungle.destiny.types.hosts.v1.HostQueryTermList;
import com.bluejungle.destiny.types.hosts.v1.HostSortFieldName;
import com.bluejungle.destiny.types.hosts.v1.HostSortTerm;
import com.bluejungle.destiny.types.hosts.v1.HostSortTermList;
import com.bluejungle.destiny.types.policies.v1.Policy;
import com.bluejungle.destiny.types.policies.v1.PolicyList;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryFieldName;
import com.bluejungle.destiny.types.policies.v1.PolicyQuerySpec;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryTerm;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryTermList;
import com.bluejungle.destiny.types.policies.v1.PolicySortFieldName;
import com.bluejungle.destiny.types.policies.v1.PolicySortTerm;
import com.bluejungle.destiny.types.policies.v1.PolicySortTermList;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryFieldName;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQuerySpec;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryTerm;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryTermList;
import com.bluejungle.destiny.types.resources.v1.ResourceClassSortFieldName;
import com.bluejungle.destiny.types.resources.v1.ResourceClassSortTerm;
import com.bluejungle.destiny.types.resources.v1.ResourceClassSortTermList;
import com.bluejungle.destiny.types.users.v1.User;
import com.bluejungle.destiny.types.users.v1.UserClass;
import com.bluejungle.destiny.types.users.v1.UserClassList;
import com.bluejungle.destiny.types.users.v1.UserClassQueryFieldName;
import com.bluejungle.destiny.types.users.v1.UserClassQuerySpec;
import com.bluejungle.destiny.types.users.v1.UserClassQueryTerm;
import com.bluejungle.destiny.types.users.v1.UserClassQueryTermList;
import com.bluejungle.destiny.types.users.v1.UserClassSortFieldName;
import com.bluejungle.destiny.types.users.v1.UserClassSortTerm;
import com.bluejungle.destiny.types.users.v1.UserClassSortTermList;
import com.bluejungle.destiny.types.users.v1.UserList;
import com.bluejungle.destiny.types.users.v1.UserQueryFieldName;
import com.bluejungle.destiny.types.users.v1.UserQuerySpec;
import com.bluejungle.destiny.types.users.v1.UserQueryTerm;
import com.bluejungle.destiny.types.users.v1.UserQueryTermList;
import com.bluejungle.destiny.types.users.v1.UserSortFieldName;
import com.bluejungle.destiny.types.users.v1.UserSortTerm;
import com.bluejungle.destiny.types.users.v1.UserSortTermList;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the implementation of the component lookup service. This service
 * allows fetching, in read-only mode, various components issued from the Policy
 * Framework.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/bindings/report/v1/ComponentLookupIFBindingImpl.java#1 $
 */

public class ComponentLookupIFBindingImpl implements ComponentLookupIF {

    private static final Log LOG = LogFactory.getLog(ComponentLookupIFBindingImpl.class.getName());

    private IComponentManager compMgr;

    /**
     * Constructor
     */
    public ComponentLookupIFBindingImpl() {
        super();
        this.compMgr = ComponentManagerFactory.getComponentManager();
    }

    /**
     * Returns the list of supported actions
     * 
     * @return a list of supported action types.
     */
    public ActionList getActions() throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
        ActionList actionList = new ActionList();
        Set<String> actionTypes = new HashSet<String>();
        Set<ActionEnumType> actionEnumElements = ActionEnumType.elements();
        for (ActionEnumType actionEnum : actionEnumElements) {
            try {
                String actionType = actionEnum.getName();
                actionTypes.add(actionType);
            } catch (IllegalArgumentException e) {
                LOG.warn("Unable to obtain web-service type for action named '" + actionEnum.getName() + "'", e);
            }
        }
        IHibernateRepository mgmtDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        if (mgmtDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.MANAGEMENT_REPOSITORY + " is not correctly setup for the DABS component.");
        }
        IHibernateRepository activityDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        if (activityDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.ACTIVITY_REPOSITORY + " is not correctly setup for the DABS component.");
        }

        final IDestinyConfigurationStore configMgr = compMgr.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
        IActionListConfigDO actionListConfig = configMgr.retrieveActionListConfig();
        IActionConfigDO[] actions = actionListConfig.getActions();
        for (int i=0; i<actions.length; i++) {
        	actionTypes.add(actions[i].getName());
        }
        
        String[] actionArray = new String[actionTypes.size()];
        actionTypes.toArray(actionArray);
        actionList.setActions(actionArray);
        return actionList;
    }

    /**
     * Returns the host manager component
     * 
     * @return the host manager component
     */
    protected IHostMgr getHostMgrComponent() {
        IHostMgr result = null;
        try {
            result = (IHostMgr) getManager().getComponent(IHostMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get host manager component in Component lookup service", e);
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ComponentLookupIF#getHosts()
     */
    public HostList getHosts(HostQuerySpec querySpec) throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
        IHostMgr hostMgr = getHostMgrComponent();
        if (hostMgr == null) {
            throw new ServiceNotReadyFault();
        }
        // Sets the default results
        HostList wsHostList = new HostList();
        wsHostList.setHosts(new Host[0]);

        IHostMgrQuerySpec hostMgrQuerySpec = new HostQuerySpecImpl(querySpec);
        try {
            List<IHost> hosts = hostMgr.getHosts(hostMgrQuerySpec);
            int size = hosts.size();
            if (size > 0) {
                Host[] wsHostArray = new Host[size];
                for (int index = 0; index < size; index++) {
                    IHost host = hosts.get(index);
                    Host wsHost = new Host(host.getName());
                    wsHostArray[index] = wsHost;
                }
                wsHostList.setHosts(wsHostArray);
            }
        } catch (DataSourceException e) {
            // TODO throw persistence exception
        }
        return wsHostList;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Returns the component manager instance
     * 
     * @return the component manager instance
     */
    protected IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * Returns the policy manager component
     * 
     * @return the policy manager component
     */
    protected IPolicyMgr getPolicyMgrComponent() {
        IPolicyMgr result = null;
        try {
            result = (IPolicyMgr) getManager().getComponent(IPolicyMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get policy manager component in Component lookup service", e);
        }
        return result;
    }

    /**
     * Returns the resource class manager component
     * 
     * @return the resource class manager component
     */
    protected IResourceClassMgr getResourceClassMgrComponent() {
        IResourceClassMgr result = null;
        try {
            result = (IResourceClassMgr) getManager().getComponent(IResourceClassMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get resource class manager component in Component lookup service", e);
        }
        return result;
    }

    /**
     * Returns the user manager component
     * 
     * @return the user manager component
     */
    protected IUserMgr getUserMgrComponent() {
        IUserMgr result = null;
        try {
            result = (IUserMgr) getManager().getComponent(IUserMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get user manager component in Component lookup service", e);
        }
        return result;
    }

    /**
     * Returns the user class manager component. This is the same component as
     * the user manager.
     * 
     * @return the user class manager
     */
    protected IUserClassMgr getUserClassMgrComponent() {
        return (IUserClassMgr) getUserMgrComponent();
    }

    /**
     * Returns the list of policies based on the query specification
     * 
     * @param querySpec
     *            query specification. If null, a query for all policies is
     *            done.
     * @return policy web service objects matching the query specification
     * 
     */
    public PolicyList getPolicies(PolicyQuerySpec querySpec) throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
        IPolicyMgr policyMgr = getPolicyMgrComponent();
        if (policyMgr == null) {
            throw new ServiceNotReadyFault();
        }
        // Returns some result even if the query returns nothing.
        PolicyList wsPolicyList = new PolicyList();
        wsPolicyList.setPolicies(new Policy[0]);

        IPolicyMgrQuerySpec policyMgrQuerySpec = new PolicyQuerySpecImpl(querySpec);
        try {
            List<IPolicy> policies = policyMgr.getPolicies(policyMgrQuerySpec);
            int size = policies.size();
            if (querySpec != null){
                if (querySpec.getLimit() != null){
                    if (size > querySpec.getLimit().intValue()){
                        size = querySpec.getLimit().intValue();
                    }
                }
            }
            if (size > 0) {
                Policy[] wsPolicyArray = new Policy[size];
                for (int index = 0; index < size; index++) {
                    IPolicy policy = policies.get(index);
                    Policy wsPolicy = new Policy(policy.getName(), policy.getFolderName());
                    wsPolicyArray[index] = wsPolicy;
                }
                wsPolicyList.setPolicies(wsPolicyArray);
            }
        } catch (DataSourceException e) {
            // TODO throw persistence exception
        }
        return wsPolicyList;
    }

    /**
     * Returns the list of resource classes based on the query specification
     * 
     * @param querySpec
     *            query specification. If null, a query for all resource classes
     *            is done.
     * @return resource class web service objects matching the query
     *         specification
     * 
     */
    /* NOTE: disabled per Bug 4286 */
//    public ResourceClassList getResourceClasses(ResourceClassQuerySpec querySpec) throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
//        IResourceClassMgr resourceClassMgr = getResourceClassMgrComponent();
//        if (resourceClassMgr == null) {
//            throw new ServiceNotReadyFault();
//        }
//        // Returns some result even if the query returns nothing.
//        ResourceClassList wsResourceClassList = new ResourceClassList();
//        wsResourceClassList.setClasses(new ResourceClass[0]);
//
//        IResourceClassMgrQuerySpec resourceClassMgrQuerySpec = new ResourceClassQuerySpecImpl(querySpec);
//        try {
//            Set resourceClasses = resourceClassMgr.getResourceClasses(resourceClassMgrQuerySpec);
//            Iterator it = resourceClasses.iterator();
//            int size = resourceClasses.size();
//            if (querySpec != null){
//                if (querySpec.getLimit() != null){
//                    if (size > querySpec.getLimit().intValue()){
//                        size = querySpec.getLimit().intValue();
//                    }
//                }
//            }
//            if (size > 0) {
//                ResourceClass[] wsResourceClassArray = new ResourceClass[size];
//                for (int index = 0; index < size; index++) {
//                    final String resourceClassName = (String) it.next();
//                    ResourceClass wsResourceClass = new ResourceClass(resourceClassName);
//                    wsResourceClassArray[index] = wsResourceClass;
//                }
//                wsResourceClassList.setClasses(wsResourceClassArray);
//            }
//        } catch (DataSourceException e) {
//            // TODO throw persistence exception
//        }
//        return wsResourceClassList;
//    }

    /**
     * Returns the list of users based on the query specification
     * 
     * @param querySpec
     *            query specification. If null, a query for all users is done.
     * @return users web service objects matching the query specification
     * 
     */
    public UserList getUsers(UserQuerySpec querySpec) throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
        IUserMgr userMgr = getUserMgrComponent();
        if (userMgr == null) {
            throw new ServiceNotReadyFault();
        }
        // Sets the default results
        UserList wsUserList = new UserList();
        wsUserList.setUsers(new User[0]);

        IUserMgrQuerySpec userMgrQuerySpec = new UserQuerySpecImpl(querySpec);
        try {
            List<IUser> users = userMgr.getUsers(userMgrQuerySpec);
            int size = users.size();
            if (querySpec != null){
                if (querySpec.getLimit() != null){
                    if (size > querySpec.getLimit().intValue()){
                        size = querySpec.getLimit().intValue();
                    }
                }
            }
            if (size > 0) {
                User[] wsUserArray = new User[size];
                for (int index = 0; index < size; index++) {
                    final IUser user = users.get(index);
                    wsUserArray[index] = new User(user.getFirstName(), user.getLastName(), user.getDisplayName());
                }
                wsUserList.setUsers(wsUserArray);
            }
        } catch (DataSourceException e) {
            // TODO throw persistence exception
        }
        return wsUserList;
    }

    /**
     * Returns the list of user classes based on the query specification
     * 
     * @param querySpec
     *            query specification. If null, a query for all user classes is
     *            done.
     * @return user classes web service objects matching the query specification
     * 
     */
    public UserClassList getUserClasses(UserClassQuerySpec querySpec) throws RemoteException, AccessDeniedFault, ServiceNotReadyFault {
        IUserClassMgr userMgr = (IUserClassMgr) getUserMgrComponent();
        if (userMgr == null) {
            throw new ServiceNotReadyFault();
        }
        // Sets the default results
        UserClassList wsUserClassList = new UserClassList();
        wsUserClassList.setClasses(new UserClass[0]);

        IUserClassMgrQuerySpec userMgrQuerySpec = new UserClassQuerySpecImpl(querySpec);
        try {
            List userClasses = userMgr.getUserClasses(userMgrQuerySpec);
            int size = userClasses.size();
            if (size > 0) {
                UserClass[] wsUserClassArray = new UserClass[size];
                for (int index = 0; index < size; index++) {
                    IUserGroup userClass = (IUserGroup) userClasses.get(index);
                    UserClass wsUserClass = new UserClass(userClass.getDisplayName(), userClass.getName(), userClass.getEnrollmentType());
                    wsUserClassArray[index] = wsUserClass;
                }
                wsUserClassList.setClasses(wsUserClassArray);
            }
        } catch (DataSourceException e) {
            // TODO throw persistence exception
        }
        return wsUserClassList;
    }

    /**
     * This class transforms a web service host query specification into a
     * policy manager component query specification.
     * 
     * @author ihanen
     */
    protected class HostQuerySpecImpl implements IHostMgrQuerySpec {

        private IHostMgrQueryTerm[] queryTerms;
        private IHostMgrSortTerm[] sortTerms;

        /**
         * Constructor
         * 
         * @param wsHostQuerySpec
         *            Axis host query spec object
         */
        protected HostQuerySpecImpl(HostQuerySpec wsHostQuerySpec) {
            // Always populate queryTerms and sortTerms with an empty element
            this.queryTerms = new IHostMgrQueryTerm[0];
            this.sortTerms = new IHostMgrSortTerm[0];
            if (wsHostQuerySpec != null) {
                HostQueryTermList hostQueryList = wsHostQuerySpec.getSearchSpec();
                if (hostQueryList != null) {
                    HostQueryTerm[] terms = hostQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IHostMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new HostQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
                HostSortTermList hostSortList = wsHostQuerySpec.getSortSpec();
                if (hostSortList != null) {
                    HostSortTerm[] terms = hostSortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IHostMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new HostSortTermImpl(terms[index]);
                            }
                        }
                    }
                }

            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec#getSearchSpecTerms()
         */
        public IHostMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec#getSortSpecTerms()
         */
        public IHostMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

    }

    /**
     * This is an implementation of the host query term to convert a web service
     * term into a host manager host query term.
     * 
     * @author ihanen
     */
    protected class HostQueryTermImpl implements IHostMgrQueryTerm {

        private String expression;
        private HostMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service host query term
         */
        protected HostQueryTermImpl(HostQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm#getFieldName()
         */
        public HostMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service host query field name into a host manager
         * query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(HostQueryFieldName wsFieldName) {
            if (HostQueryFieldName.name.equals(wsFieldName)) {
                this.fieldName = HostMgrQueryFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is an implementation of the host sort term to convert a web service
     * term into a host manager host sort term.
     * 
     * @author ihanen
     */
    protected class HostSortTermImpl implements IHostMgrSortTerm {

        private SortDirectionType direction;
        private HostMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service host query term
         */
        protected HostSortTermImpl(HostSortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrSortTerm#getFieldName()
         */
        public HostMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service host sort field name into a host manager sort
         * field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(HostSortFieldName wsFieldName) {
            if (HostSortFieldName.name.equals(wsFieldName)) {
                this.fieldName = HostMgrSortFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This class transforms a web service policy query specification into a
     * policy manager component query specification.
     * 
     * @author ihanen
     */
    protected class PolicyQuerySpecImpl implements IPolicyMgrQuerySpec {

        private IPolicyMgrQueryTerm[] queryTerms;
        private IPolicyMgrSortTerm[] sortTerms;

        /**
         * Constructor
         * 
         * @param wsPolicyQuerySpec
         *            Axis policy query spec object
         */
        protected PolicyQuerySpecImpl(PolicyQuerySpec wsPolicyQuerySpec) {
            // Always populate queryTerms and sortTerms with an empty element
            this.queryTerms = new IPolicyMgrQueryTerm[0];
            this.sortTerms = new IPolicyMgrSortTerm[0];
            if (wsPolicyQuerySpec != null) {
                PolicyQueryTermList policyQueryList = wsPolicyQuerySpec.getSearchSpec();
                if (policyQueryList != null) {
                    PolicyQueryTerm[] terms = policyQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IPolicyMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new PolicyQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
                PolicySortTermList policySortList = wsPolicyQuerySpec.getSortSpec();
                if (policySortList != null) {
                    PolicySortTerm[] terms = policySortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IPolicyMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new PolicySortTermImpl(terms[index]);
                            }
                        }
                    }
                }

            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec#getSearchSpecTerms()
         */
        public IPolicyMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec#getSortSpecTerms()
         */
        public IPolicyMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

    }

    /**
     * This is an implementation of the policy query term to convert a web
     * service term into a policy manager policy query term.
     * 
     * @author ihanen
     */
    protected class PolicyQueryTermImpl implements IPolicyMgrQueryTerm {

        private String expression;
        private PolicyMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service policy query term
         */
        protected PolicyQueryTermImpl(PolicyQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm#getFieldName()
         */
        public PolicyMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service policy query field name into a policy manager
         * query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(PolicyQueryFieldName wsFieldName) {
            if (PolicyQueryFieldName.Name.equals(wsFieldName)) {
                this.fieldName = PolicyMgrQueryFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is an implementation of the policy query term to convert a web
     * service term into a policy manager policy query term.
     * 
     * @author ihanen
     */
    protected class PolicySortTermImpl implements IPolicyMgrSortTerm {

        private SortDirectionType direction;
        private PolicyMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service policy query term
         */
        protected PolicySortTermImpl(PolicySortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm#getFieldName()
         */
        public PolicyMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service policy sort field name into a policy manager
         * sort field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(PolicySortFieldName wsFieldName) {
            if (PolicySortFieldName.Name.equals(wsFieldName)) {
                this.fieldName = PolicyMgrSortFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This class transforms a web service user query specification into a
     * policy manager component query specification.
     * 
     * @author ihanen
     */
    protected class UserQuerySpecImpl implements IUserMgrQuerySpec {

        private IUserMgrQueryTerm[] queryTerms;
        private IUserMgrSortTerm[] sortTerms;
        private int limit;
        private Date asOf;

        /**
         * Constructor
         * 
         * @param wsUserQuerySpec
         *            Axis user query spec object
         */
        protected UserQuerySpecImpl(UserQuerySpec wsUserQuerySpec) {
            // Always populate queryTerms and sortTerms with an empty element
            this.queryTerms = new IUserMgrQueryTerm[0];
            this.sortTerms = new IUserMgrSortTerm[0];
            if (wsUserQuerySpec != null) {
                PositiveInteger wsLimit = wsUserQuerySpec.getLimit();
                if (wsLimit != null) {
                    this.limit = wsUserQuerySpec.getLimit().intValue();
                } else {
                    this.limit = 0;
                }

                UserQueryTermList userQueryList = wsUserQuerySpec.getSearchSpec();
                if (userQueryList != null) {
                    UserQueryTerm[] terms = userQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IUserMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new UserQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
                UserSortTermList userSortList = wsUserQuerySpec.getSortSpec();
                if (userSortList != null) {
                    UserSortTerm[] terms = userSortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IUserMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new UserSortTermImpl(terms[index]);
                            }
                        }
                    }
                }
            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getAsOf()
         */
        public Date getAsOf() {
            return this.asOf;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getLimit()
         */
        public int getLimit() {
            return this.limit;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getSearchSpecTerms()
         */
        public IUserMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getSortSpecTerms()
         */
        public IUserMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }
    }

    /**
     * This is an implementation of the user query term to convert a web service
     * term into a user manager user query term.
     * 
     * @author ihanen
     */
    protected class UserQueryTermImpl implements IUserMgrQueryTerm {

        private String expression;
        private UserMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service user query term
         */
        protected UserQueryTermImpl(UserQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getFieldName()
         */
        public UserMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service user query field name into a user manager
         * query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(UserQueryFieldName wsFieldName) {
            if (UserQueryFieldName.firstName.equals(wsFieldName)) {
                this.fieldName = UserMgrQueryFieldType.FIRST_NAME;
            } else if (UserQueryFieldName.lastName.equals(wsFieldName)) {
                this.fieldName = UserMgrQueryFieldType.LAST_NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is an implementation of the user query term to convert a web service
     * term into a user manager user query term.
     * 
     * @author ihanen
     */
    protected class UserSortTermImpl implements IUserMgrSortTerm {

        private SortDirectionType direction;
        private UserMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service user query term
         */
        protected UserSortTermImpl(UserSortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getFieldName()
         */
        public UserMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service user sort field name into a user manager sort
         * field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(UserSortFieldName wsFieldName) {
            if (UserSortFieldName.firstName.equals(wsFieldName)) {
                this.fieldName = UserMgrSortFieldType.FIRST_NAME;
            } else if (UserSortFieldName.lastName.equals(wsFieldName)) {
                this.fieldName = UserMgrSortFieldType.LAST_NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This class transforms a web service user class query specification into a
     * user class manager component query specification.
     * 
     * @author ihanen
     */
    protected class UserClassQuerySpecImpl implements IUserClassMgrQuerySpec {

        private IUserClassMgrQueryTerm[] queryTerms;
        private IUserClassMgrSortTerm[] sortTerms;

        /**
         * Constructor
         * 
         * @param wsUserClassQuerySpec
         *            Axis user query spec object
         */
        protected UserClassQuerySpecImpl(UserClassQuerySpec wsUserClassQuerySpec) {
            // Always populate queryTerms and sortTerms with an empty element
            this.queryTerms = new IUserClassMgrQueryTerm[0];
            this.sortTerms = new IUserClassMgrSortTerm[0];
            if (wsUserClassQuerySpec != null) {
                UserClassQueryTermList userClassQueryList = wsUserClassQuerySpec.getSearchSpec();
                if (userClassQueryList != null) {
                    UserClassQueryTerm[] terms = userClassQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IUserClassMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new UserClassQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
                UserClassSortTermList userClassSortList = wsUserClassQuerySpec.getSortSpec();
                if (userClassSortList != null) {
                    UserClassSortTerm[] terms = userClassSortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IUserClassMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new UserClassSortTermImpl(terms[index]);
                            }
                        }
                    }
                }

            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec#getSearchSpecTerms()
         */
        public IUserClassMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec#getSortSpecTerms()
         */
        public IUserClassMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

    }

    /**
     * This is an implementation of the user class query term to convert a web
     * service term into a user class manager user query term.
     * 
     * @author ihanen
     */
    protected class UserClassQueryTermImpl implements IUserClassMgrQueryTerm {

        private String expression;
        private UserClassMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service user class query term
         */
        protected UserClassQueryTermImpl(UserClassQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQueryTerm#getFieldName()
         */
        public UserClassMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service user query field name into a user manager
         * query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(UserClassQueryFieldName wsFieldName) {
            this.fieldName = UserClassMgrQueryFieldType.getUserClassMgrQueryFieldType(wsFieldName.getValue());
        }
    }

    /**
     * This is an implementation of the user class query term to convert a web
     * service term into a user class manager user class query term.
     * 
     * @author ihanen
     */
    protected class UserClassSortTermImpl implements IUserClassMgrSortTerm {

        private SortDirectionType direction;
        private UserClassMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service user class query term
         */
        protected UserClassSortTermImpl(UserClassSortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getFieldName()
         */
        public UserClassMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service user class sort field name into a user class
         * manager sort field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(UserClassSortFieldName wsFieldName) {
            this.fieldName = UserClassMgrSortFieldType.getUserClassMgrSortFieldType(wsFieldName.getValue());
        }
    }

    /**
     * This class transforms a web service resource class query specification
     * into a resource class manager component query specification.
     * 
     * @author ihanen
     */
    protected class ResourceClassQuerySpecImpl implements IResourceClassMgrQuerySpec {

        private IResourceClassMgrQueryTerm[] queryTerms;
        private IResourceClassMgrSortTerm[] sortTerms;

        /**
         * Constructor
         * 
         * @param wsResourceClassQuerySpec
         *            Axis resource class query spec object
         */
        protected ResourceClassQuerySpecImpl(ResourceClassQuerySpec wsResourceClassQuerySpec) {
            // Place default return values
            this.queryTerms = new IResourceClassMgrQueryTerm[0];
            this.sortTerms = new IResourceClassMgrSortTerm[0];
            if (wsResourceClassQuerySpec != null) {
                ResourceClassQueryTermList resourceClassQueryList = wsResourceClassQuerySpec.getSearchSpec();
                if (resourceClassQueryList != null) {
                    ResourceClassQueryTerm[] terms = resourceClassQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IResourceClassMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new ResourceClassQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
                ResourceClassSortTermList resourceClassSortList = wsResourceClassQuerySpec.getSortSpec();
                if (resourceClassSortList != null) {
                    ResourceClassSortTerm[] terms = resourceClassSortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IResourceClassMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new ResourceClassSortTermImpl(terms[index]);
                            }
                        }
                    }
                }

            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec#getSearchSpecTerms()
         */
        public IResourceClassMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec#getSortSpecTerms()
         */
        public IResourceClassMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

    }

    /**
     * This is an implementation of the resource class query term to convert a
     * web service term into a resource class manager user query term.
     * 
     * @author ihanen
     */
    protected class ResourceClassQueryTermImpl implements IResourceClassMgrQueryTerm {

        private String expression;
        private ResourceClassMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service resource class query term
         */
        protected ResourceClassQueryTermImpl(ResourceClassQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm#getFieldName()
         */
        public ResourceClassMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service resource class query field name into a
         * resource class manager query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(ResourceClassQueryFieldName wsFieldName) {
            if (ResourceClassQueryFieldName.Name.equals(wsFieldName)) {
                this.fieldName = ResourceClassMgrQueryFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is an implementation of the resource class query term to convert a
     * web service term into a resource class manager user class query term.
     * 
     * @author ihanen
     */
    protected class ResourceClassSortTermImpl implements IResourceClassMgrSortTerm {

        private SortDirectionType direction;
        private ResourceClassMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service resource class query term
         */
        protected ResourceClassSortTermImpl(ResourceClassSortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrSortTerm#getFieldName()
         */
        public ResourceClassMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service resource class sort field name into a resource
         * class manager sort field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(ResourceClassSortFieldName wsFieldName) {
            if (ResourceClassSortFieldName.Name.equals(wsFieldName)) {
                this.fieldName = ResourceClassMgrSortFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }
}
