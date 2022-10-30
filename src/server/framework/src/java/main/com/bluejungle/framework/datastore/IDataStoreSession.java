package com.bluejungle.framework.datastore;

import java.util.List;

// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/IDataStoreSession.java#1 $
 */

public interface IDataStoreSession {
    
    ITransaction beginTransaction();    
    void save(Object o);
	Object getRepSession();

	void close();
  
    List find(String query);
    int delete(String query);
    

	

}
