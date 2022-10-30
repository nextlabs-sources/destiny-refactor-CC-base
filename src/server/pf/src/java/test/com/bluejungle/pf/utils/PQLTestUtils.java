/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

package com.bluejungle.pf.utils;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;

/**
 * This class provides static methods for changing PQL,
 * hiding the process of converting it to a domain object and back. 
 */
public class PQLTestUtils {

    /**
     * Given a PQL string, returns an equivalent string
     * with the status set to the desired value.
     * @param dev the dev entity with non-empty PQL.
     * @param newStatus the status to set for the PQL.
     * @throws PQLException when the PQL cannot be parsed.
     */
    public static void setStatus( DevelopmentEntity dev, final DevelopmentStatus newStatus ) throws PQLException {
        if ( dev == null ) {
            throw new NullPointerException("dev");
        }
        if ( dev.getPql() == null ) {
            return;
        }
        if ( newStatus == null ) {
            throw new NullPointerException("newStatus");
        }
        final DomainObjectFormatter dof = new DomainObjectFormatter();
        DomainObjectBuilder.processInternalPQL( dev.getPql(), new IPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                dof.formatPolicyDef( update( descr, newStatus ), policy );
            }
            public void visitFolder(DomainObjectDescriptor descr) {
                dof.formatFolder( update( descr, newStatus ) );
            }
            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                dof.formatDef( update( descr, newStatus ), pred );
            }
            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                dof.formatLocation( update( descr, newStatus ), location );
            }
            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                throw new IllegalArgumentException("Access policy is unexpected in setStatus");
            }
            public DomainObjectDescriptor update( DomainObjectDescriptor descr, DevelopmentStatus newStatus ) {
                return new DomainObjectDescriptor(
                    descr.getId()
                ,   descr.getName()
                ,   descr.getOwner()
                ,   descr.getAccessPolicy()
                ,   descr.getType()
                ,   descr.getDescription()
                ,   newStatus
                ,   descr.getVersion()
                ,   descr.getLastUpdated()
                ,   descr.getWhenCreated()
                ,   descr.getLastModified()
                ,   descr.getModifier()
                ,   descr.getLastSubmitted()
                ,   descr.getSubmitter()
                ,   descr.isHidden()
                ,   descr.isAccessible()
                ,   descr.hasDependencies() );
            }
        });
        dev.setPql( dof.getPQL() );
    }

    /**
     * Given a PQL string, returns an equivalent string
     * with the description set to the desired value.
     * @param dev the dev entity with non-empty PQL.
     * @param newDescription the description to set for the PQL.
     * @throws PQLException when the PQL cannot be parsed.
     */
    public static void setDescription( DevelopmentEntity dev, final String newDescription ) throws PQLException {
        if ( dev == null ) {
            throw new NullPointerException("dev");
        }
        if ( dev.getPql() == null ) {
            return;
        }
        final DomainObjectFormatter dof = new DomainObjectFormatter();
        DomainObjectBuilder.processInternalPQL( dev.getPql(), new IPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                dof.formatPolicyDef( update( descr, newDescription ), policy );
            }
            public void visitFolder(DomainObjectDescriptor descr) {
                dof.formatFolder( update( descr, newDescription ) );
            }
            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                dof.formatDef( update( descr, newDescription ), pred );
            }
            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                dof.formatLocation( update( descr, newDescription ), location );
            }
            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                throw new IllegalArgumentException("Access Policy is unexpected in setDescription");
            }
            public DomainObjectDescriptor update( DomainObjectDescriptor descr, String newDescription ) {
                return new DomainObjectDescriptor(
                    descr.getId()
                ,   descr.getName()
                ,   descr.getOwner()
                ,   descr.getAccessPolicy()
                ,   descr.getType()
                ,   newDescription
                ,   descr.getStatus()
                ,   descr.getVersion()
                ,   descr.getLastUpdated()
                ,   descr.getWhenCreated()
                ,   descr.getLastModified()
                ,   descr.getModifier()
                ,   descr.getLastSubmitted()
                ,   descr.getSubmitter()
                ,   descr.isHidden()
                ,   descr.isAccessible()
                ,   descr.hasDependencies() );
            }
        });
        dev.setPql( dof.getPQL() );
    }
}
