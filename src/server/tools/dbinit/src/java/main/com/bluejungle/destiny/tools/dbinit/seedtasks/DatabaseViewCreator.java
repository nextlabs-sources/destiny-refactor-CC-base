/*
 * Created on Apr 2, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.seedtasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository.DbType;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.version.IVersion;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/seedtasks/DatabaseViewCreator.java#1 $
 */

public class DatabaseViewCreator extends SeedDataTaskBase implements ISeedUpdateTask {
    private static final String SQL_FILE_PATH = "/com/bluejungle/destiny/tools/dbinit/seedtasks/reporter_v1_and_v2_view.sql";
    private static final String PG_SQL_FILE_PATH = "/com/bluejungle/destiny/tools/dbinit/seedtasks/reporter_v1_and_v2_view_postgres.sql";
    private static final String MS_SQL_FILE_PATH = "/com/bluejungle/destiny/tools/dbinit/seedtasks/reporter_v1_and_v2_view_mssql.sql";

    private static final Pattern CREATE_VIEW_PATTERN = Pattern.compile("CREATE VIEW (\\S+) .+ FROM .+", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
    public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
        execute();
    }

    @Override
    public void execute() throws SeedDataTaskException {
        final Map<String, String> sqls = getSqls(getHibernateDataSource().getDatabaseType());
        if (sqls == null) {
            return;
        }
        
        if (sqls.isEmpty()) {
            getLog().info("No view needs to be created");
            return;
        }
        
        new MicroTask(){
            @Override
            public void run(Connection connection) throws SeedDataTaskException, SQLException,
                    HibernateException {
                createViews(connection, sqls);
            }
        }.run();
    }
    
    protected Map<String, String> getSqls(DbType dbType){
    	 InputStream is = null;
    	 if (dbType == DbType.MS_SQL) {
			is = DatabaseViewCreator.class.getResourceAsStream(MS_SQL_FILE_PATH);
		} else if (dbType == DbType.POSTGRESQL) {
			is = DatabaseViewCreator.class.getResourceAsStream(PG_SQL_FILE_PATH);
		} else if (dbType == DbType.ORACLE) {
	    	is = DatabaseViewCreator.class.getResourceAsStream(SQL_FILE_PATH);
		}
    	
        if (is == null) {
            getLog().error("Can't find file, " + SQL_FILE_PATH + ". The database views needs to create manaully.");
            return null;
        }
        try {
            return readSql(is);
        } catch (IOException e) {
            getLog().error("Can't read file, " + SQL_FILE_PATH, e);
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                getLog().warn("Can't close inputstream");
            }
        }
    }

    private Map<String, String> readSql(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        int c;
        while ((c = is.read()) != -1) {
            writer.write(c);
        }
        
        String content = writer.toString();
        String[] sqls = content.split(";");
        
        // the order may be important
        Map<String, String> output = new LinkedHashMap<String, String>();
        for (String sql : sqls) {
            Matcher m = CREATE_VIEW_PATTERN.matcher(sql);
            if (m.find()) {
                String tableName = m.group(1);
                output.put(tableName, sql);
            } else {
                getLog().warn("Invalid create view statement: " + sql);
            }
        }
        
        return output;
    }
    
    private void createViews(Connection connection, Map<String, String> sqls) throws SQLException {
        DialectExtended dialectX = (DialectExtended) getConfiguration().get(
                ISeedDataTask.DIALECT_EXTENDED_CONFIG_PARAM);
        DatabaseMetaData dbmd = connection.getMetaData();
        List<String> statements = new ArrayList<String>(sqls.size());
        for(Map.Entry<String, String> e : sqls.entrySet()){
            String viewName = e.getKey();
            viewName = DatabaseHelper.matchToDbStoreCase(viewName);
            if (DatabaseHelper.isViewExist(dbmd, dialectX, viewName)) {
                getLog().info("The view \"" + viewName + "\" already exists");
            }else{
                statements.add(e.getValue());
            }
        }
        DatabaseHelper.processSqlStatements(connection, statements);
    }
}
