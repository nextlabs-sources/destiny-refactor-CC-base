/*
 * Created on Apr 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.util.Date;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ChangedDictionaryPredicate.java#1 $
 */

public class ChangedDictionaryPredicate extends  AbstractDictionaryPredicate {
 
    private final Date startDate;
    private final Date endDate;
    
    public ChangedDictionaryPredicate(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;
    }
    
    /**
     * @see IDictionaryPredicate#accept(IDictionaryPredicateVisitor)
     */
    public void accept(IDictionaryPredicateVisitor visitor) {
        visitor.visitChangedCondition(startDate, endDate);
    }
 
}
