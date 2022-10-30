/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;

/**
 * CollectingPQLVisitor creates entities corresponding
 * to PQL constructs it encounters, and collects them
 * all in one collection.
 *
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/tools/CollectingPQLVisitor.java#1 $
 *
 */
public class CollectingPQLVisitor implements IPQLVisitor {

    private final Collection<DevelopmentEntity> entities = new ArrayList<DevelopmentEntity>();
    private final DomainObjectFormatter dof = new DomainObjectFormatter();

    public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
        // don't care about access policies

    }

    public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
        dof.reset();
        dof.formatLocation(descriptor, location);
        addEntity();

    }

    public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
        dof.reset();
        dof.formatPolicyDef(descriptor, policy);
        addEntity();
    }

    public void visitFolder(DomainObjectDescriptor descriptor) {
        dof.reset();
        dof.formatFolder(descriptor);
        addEntity();

    }

    public void visitComponent(DomainObjectDescriptor descriptor, IPredicate pred) {
        dof.reset();
        dof.formatDef(descriptor, pred);
        addEntity();
    }

    protected void addEntity() {
        try {
            DevelopmentEntity entity = new DevelopmentEntity(dof.getPQL());
            entities.add(entity);
        } catch (PQLException pqle) {
            throw new RuntimeException(pqle);
        }
    }

    public Collection<DevelopmentEntity> getEntities() {
        return Collections.unmodifiableCollection(entities);
    }

}
