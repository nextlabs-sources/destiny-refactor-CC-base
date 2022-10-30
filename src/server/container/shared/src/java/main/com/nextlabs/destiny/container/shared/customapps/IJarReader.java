/*
 * Created on Mar 4, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/IJarReader.java#1 $
 */

public interface IJarReader {

    byte[] read(String entryName) throws IOException;

    String getString(String entryName) throws IOException;

    void read(String entryName, OutputStream output) throws IOException;

}
