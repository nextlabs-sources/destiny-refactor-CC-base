/*
 * Created on Feb 19, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.serviceprovider;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/IDynamicExternalSPResponse.java#1 $
 */

public interface IDynamicExternalSPResponse extends IExternalServiceProviderResponse{
    <T> void add(T value);
    
    <T> void add(Class<? super T> clazz, T value);
}
