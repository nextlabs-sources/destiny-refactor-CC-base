/*
 * Created on Feb 1, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.serviceprovider;
/**
 * IExternalServiceProvider provides a generic service invocation method invoke().
 * The two interfaces are separated so that, in the future, we will be able to 
 * develop a service provider that has a different interface from invoke(), 
 * e.g., dynamic attribute provider. 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/IExternalServiceProvider.java#1 $
 */

public interface IExternalServiceProvider extends IServiceProvider {
    IExternalServiceProviderResponse invoke(Object[] objs);
}
