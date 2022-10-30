/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

/**
 * Wrapper of LDAP Exception.
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/RetrievalFailedException.java#1 $
 */
public class RetrievalFailedException extends Exception {
    private final String entry;
    
    /**
     * Constructor
     * @param arg0
     */
    public RetrievalFailedException(String arg0) {
        this(arg0, (String)null);
    }
    
    public RetrievalFailedException(String arg0, String entry) {
       super(arg0);
       this.entry = entry;
    }

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public RetrievalFailedException(String arg0, Throwable arg1) {
        this(arg0, arg1, null);
    }
    
    public RetrievalFailedException(String arg0, Throwable arg1, String entry) {
        super(arg0, arg1);
        this.entry = entry;
    }

    /**
     * Constructor
     * @param arg0
     */
    public RetrievalFailedException(Throwable arg0) {
        this(arg0, null);
    }
    
    public RetrievalFailedException(Throwable arg0, String entry) {
        super(arg0);
        this.entry = entry;
    }

    String getEntry() {
        return entry;
    }
}
