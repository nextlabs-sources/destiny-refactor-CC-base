/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.Action;
import com.novell.ldap.LDAPEntry;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/BullseyeException.java#1 $
 */

class BullseyeException extends Exception{
    final LDAPEntry entry;
    final Action action;
    
    BullseyeException(LDAPEntry entry, Action action, Exception e){
        super(e);
        this.entry = entry;
        this.action = action;
    }
}
