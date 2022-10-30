/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

/**
 * This interface specifies APIs to be used to create the Destiny schema(s)
 * during installation of the Destiny Application. To be implemented by a DBA.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IHibernateSchemaBuilder {

}

/*
 * This code may be useful in the implementation of this interface: if
 * (createSchemaFlag) { SchemaExport schemaExport = new
 * SchemaExport(hibernateCfg); schemaExport.create(true, true); }
 */