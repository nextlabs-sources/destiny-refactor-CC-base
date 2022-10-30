/*
 * Created on Jun 24, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.ResultSetKey;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/IndexesRebuild.java#1 $
 */

public class IndexesRebuild implements IDataSyncTask {
    private static final Log LOG = LogFactory.getLog(IndexesRebuild.class);
    
    private static final String TABLES[] = new String[]{
          Constants.REPORT_POLICY_ACTIVITY_LOG_TABLE
        , Constants.REPORT_POLICY_CUSTOM_ATTR_TABLE
        , SharedLib.REPORT_PA_OBLIGATION_TABLE
        , SharedLib.PA_TABLE
        , SharedLib.PA_CUST_ATTR_TABLE
        , SharedLib.PA_OBLIGATION_TABLE
    };
    
    public SyncType getType() {
        return SyncType.INDEXES_REBUILD;
    }
    
    public void run(Session session, long timeout, IConfiguration config) {
        long startTime = System.currentTimeMillis();
        final IDataSyncTaskUpdate update = config.get(IDataSyncTask.TASK_UPDATE_PARAMETER);
        final Dialect dialect = config.get(IDataSyncTask.DIALECT_CONFIG_PARAMETER);
        final DialectExtended dialectx = DialectExtended.getDialectExtended(dialect);
        
        
        try {
            final Connection c = session.connection();
            
            DatabaseMetaData meta = c.getMetaData();
            if(meta.storesUpperCaseIdentifiers()){
                DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_UPPER_CASE);
            }else if(meta.storesLowerCaseIdentifiers()){
                DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_LOWER_CASE);
            }else if(meta.storesMixedCaseIdentifiers()){
                DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_MIXED_CASE);
            }else{
                LOG.error("What type of case does the database store identifiers?");
                return;
            }
            
            // create sql for tables first.
            Map<String, String[]> sqls = new LinkedHashMap<String, String[]>(TABLES.length);
            int totalSqls = 0;
            for (String tableName : TABLES) {
                String[] sqlsPerTable =
                        createSqlPerTable(DatabaseHelper.matchToDbStoreCase(tableName), c, dialectx);

                sqls.put(tableName, sqlsPerTable);
                
                totalSqls += sqlsPerTable.length;
            }
            
            update.setTotalSize(totalSqls);
            
            if (LOG.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("rebuild index sqls\n");
                for (String tableName : TABLES) {
                    sb.append("  ")
                        .append(tableName)
                        .append("\n")
                        .append(ArrayUtils.asString(sqls.get(tableName), "\n", "    "))
                        .append("\n");
                }
                LOG.debug(sb.toString());
            }
            
            for(Map.Entry<String, String[]> entry : sqls.entrySet()){
                LOG.debug("Rebuilding table " + entry.getKey());
                
                for(String sql : entry.getValue()){
                    //change this may break the DataSyncManager command line version
                    update.setPrefix(entry.getKey() + " - " + sql);
                    
                    //check timeout
                    if (System.currentTimeMillis() - startTime > timeout) {
                        LOG.info("IndexesRebuild got timeout.");
                        return;
                    }
                    
                    LOG.trace(sql);
                    
                    Statement s = c.createStatement();
                    //start a timer for every query, in case it doesn't return in time, I don't get timeout
                    Timer timer = new Timer("IndexesRebuildUpdater", true);
                    try {
                        long delay = (long)(update.getUpdateInterval() * 0.9);
                        timer.scheduleAtFixedRate(new TimerTask(){
                            @Override
                            public void run() {
                                update.addSuccess(1);
                            }
                        }, delay, delay);
                        s.execute(sql);
                        update.addSuccess(1);
                    } catch (SQLException e) {
                        LOG.error("Fail to execute: " + sql, e);
                        update.addFail(1);
                    } finally {
                        timer.cancel();
                        s.close();
                    }
                }
            }
        } catch (HibernateException e) {
            LOG.error("Fail to rebuld indexes", e);
        } catch (SQLException e) {
            LOG.error("Fail to rebuld indexes", e);
        }
    }
    
    /**
     * 
     * @param tableName
     * @param c
     * @param dialect
     * @return an array of sql to rebuild indexes for specific table. 
     *          If no indexes needs to rebuld, an empty string array is returned.
     * @throws SQLException
     */
    protected String[] createSqlPerTable(String tableName, Connection c, DialectExtended dialect)
            throws SQLException {
        Set<String> indexesName = new HashSet<String>();
        
        final String tableSchema = dialect.getTableSchema(c.getMetaData());
        final String catalog = null;
        ResultSet rs = c.getMetaData().getIndexInfo(catalog, tableSchema, tableName, false, true);
        while (rs.next()) {
            String name = rs.getString(ResultSetKey.INDEX_NAME);
            if (name != null) {
                indexesName.add(name);
            }
        }
        
        List<String> sqls = new ArrayList<String>(indexesName.size());
        for(String indexName : indexesName){
            String sql = dialect.sqlRebuildIndex(indexName, tableName);
            sqls.add(sql);
        }
        
        return sqls.toArray(new String[sqls.size()]);
    }
}
