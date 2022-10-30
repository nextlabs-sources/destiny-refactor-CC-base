package com.bluejungle.framework.utils;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/IStreamable.java#1 $
 */

import java.io.Externalizable;
import java.io.InputStream;

/**
 * This interface defines the contract of streamable classes.
 */
public interface IStreamable extends Externalizable {

    InputStream getStream();

    int getSize();
}
