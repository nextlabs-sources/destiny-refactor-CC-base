/*
 * Created on Sep 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/GroupSearchSpecImpl.java#1 $
 */

public class GroupSearchSpecImpl implements IGroupSearchSpec {

    private String titleStartsWith;

    /**
     * Constructor
     *  
     */
    public GroupSearchSpecImpl(String titleStartsWith) {
        super();
        this.titleStartsWith = titleStartsWith;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec#getTitleStartsWith()
     */
    public String getTitleStartsWith() {
        return this.titleStartsWith;
    }
}