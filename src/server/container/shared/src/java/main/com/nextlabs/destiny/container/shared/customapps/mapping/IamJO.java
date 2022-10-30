/*
 * Created on Mar 2, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps.mapping;

import org.apache.commons.digester.Digester;

/**
 * An interface to describe this is a java object used in digester. 
 * change the any name may need to modify the digester/mapping.
 * A public default constructor is required in each subclass
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/mapping/IamJO.java#1 $
 */

public abstract class IamJO {
    
    protected abstract String getCurrentNodeName();
    
    public String accept(Digester digester){
        return this.accept(digester, null);
    }
    
    public String accept(Digester digester, String parent){
        String node = getCurrentNodeName();
        if (parent != null) {
            node = parent + "/" + node;
        }
        addRule(digester, parent, node);
        return node;
    }
    
    protected abstract void addRule(Digester digester, String parent, String current);
    
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * for display purpose only
     * @param prefix
     * @return
     */
    protected abstract String toString(String prefix);
}
