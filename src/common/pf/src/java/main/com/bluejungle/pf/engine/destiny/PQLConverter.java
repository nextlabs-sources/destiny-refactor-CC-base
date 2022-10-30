/*
 * Created on May 01, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/PQLConverter.java#1 $:
 */

package com.bluejungle.pf.engine.destiny;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

class PQLConverter {
    private Cache cache = null;
    private static final Log log = LogFactory.getLog(PQLConverter.class.getName());

    public PQLConverter() {
        try {
            final CacheManager manager = CacheManager.create();

            cache = manager.getCache(PQLConverter.class.toString());

            if (cache == null) {
                // TODO make configurable
                // 50 max items, don't flush to disk, not eternal, 60 second time to live/idle
                cache = new Cache(PQLConverter.class.toString(), 50, false, false, 60, 60);

                manager.addCache(cache);
            }
        } catch (CacheException e) {
            cache = null;
            log.warn("Error creating cache - running cacheless");
        }
    }

    public IDPolicy[] convert(final String pql) {
        if (pql == null) {
            return null;
        }

        if (cache != null) {
            try {
                Element e = cache.get(pql);

                if (e != null) {
                    log.debug("Found converted pql in cache");
                    return (IDPolicy[])e.getValue();
                } else {
                    log.debug("Did not find converted pql in cache");
                    IDPolicy[] policies = parsePQL(pql);
                    cache.put(new Element(pql, policies));
                    return policies;
                }
            } catch (CacheException e) {
                log.error("Cache exception " + e);
            }
        } else {
            log.debug("Cache not found. Converting pql");
        }

        return parsePQL(pql);
    }

    private IDPolicy[] parsePQL(final String pql) {
        try {
            final ArrayList<IDPolicy> policies = new ArrayList<IDPolicy>();

            DomainObjectBuilder.processInternalPQL(pql,
                                                   new DefaultPQLVisitor() {
                                                       @Override
                                                       public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                                                           policies.add(policy);
                                                       }
                                                   });

            return policies.toArray(new IDPolicy[policies.size()]);
        } catch(PQLException e) {
            log.error("Error " + e + " when parsing pql " + pql);
        }

        return null;
    }
}
