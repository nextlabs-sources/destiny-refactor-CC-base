/*
 * Created on Jun 15, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/DatabaseInsertable.java#1 $
 */
public interface DatabaseInsertable {
	
    void setValue(PreparedStatement statement) throws SQLException;
}
