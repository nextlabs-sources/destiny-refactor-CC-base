package com.bluejungle.framework.securestore;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/securestore/ISecureStore.java#1 $
 */

import java.io.IOException;
import java.io.Serializable;

/**
 * This interface provides a template for storing serialized data.
 *
 * @author Sergey Kalinichenko
 */
public interface ISecureStore<T extends Serializable> {

    void save(T obj) throws IOException;

    T read() throws IOException;

}
