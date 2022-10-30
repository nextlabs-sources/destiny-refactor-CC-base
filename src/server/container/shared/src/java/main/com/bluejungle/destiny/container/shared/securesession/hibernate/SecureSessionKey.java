/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;


/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/security/securesession/hibernate/SecureSessionKey.java#1 $
 */

class SecureSessionKey {

    private Long id;
    private long expirationTime;
    private long endOfLifeTime;

    SecureSessionKey(Long id, long expirationTime, long endOfLifeTime) {
        super();
        this.id = id;
        this.expirationTime = expirationTime;
        this.endOfLifeTime = endOfLifeTime;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.security.securesession.hibernate.ISecureSessionKey#getId()
     */
    Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.security.securesession.hibernate.ISecureSessionKey#getExpirationTime()
     */
    long getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.security.securesession.hibernate.ISecureSessionKey#getEndOfLifeTime()
     */
    long getEndOfLifeTime() {
        return this.endOfLifeTime;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        boolean valueToReturn = false;
        if (obj == this) {
            valueToReturn = true;
        } else if ((obj != null) && (obj instanceof SecureSessionKey)) {
            SecureSessionKey keyToTest = (SecureSessionKey) obj;
            valueToReturn = keyToTest.getId().equals(this.getId());
            valueToReturn &= keyToTest.endOfLifeTime == this.endOfLifeTime;
            valueToReturn &= keyToTest.expirationTime == this.expirationTime;
        }

        return valueToReturn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (endOfLifeTime ^ (endOfLifeTime >>> 32));
        result = prime * result + (int) (expirationTime ^ (expirationTime >>> 32));
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
