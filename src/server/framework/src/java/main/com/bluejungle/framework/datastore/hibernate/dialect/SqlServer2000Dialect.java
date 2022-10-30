package com.bluejungle.framework.datastore.hibernate.dialect;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/dialect/SqlServer2000Dialect.java#1 $
 */

import java.sql.Types;

import net.sf.hibernate.dialect.SQLServerDialect;

/**
 * This version of Microsoft SQL Server dialect forces creation of
 * string fields with case-sensitive compare order.
 *
 * By default, SQL Server uses case-insensitive searches on varchar
 * fields; this dialect generates DDL with case-sensitive colation order.
 *
 * @author sergey
 */
public class SqlServer2000Dialect extends SQLServerDialect {

	/**
	 * This constructor overrides the DDL generated for the varchar
	 * type in the based class.
	 */
	public SqlServer2000Dialect() {
		super();
		//To get the default server collation:
		//SELECT SERVERPROPERTY('Collation')
		
		//To get the default collation for a database
		//select databasepropertyex(db_name(), 'Collation')
		registerColumnType( Types.VARCHAR, "nvarchar($l)" );
		registerColumnType( Types.NVARCHAR, "nvarchar($l)");
		registerColumnType( Types.CLOB, "nvarchar(max)" );
	}

}
