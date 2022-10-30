package com.bluejungle.destiny.tools.dbinit.javaupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.dialect.Dialect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.version.IVersion;

/**
 * @author Hor-kan Chan
 * @date Mar 14, 2007
 */
public class ActivityJavaUpdateTask extends BaseJavaUpdateTask {
    private static final Log LOG = LogFactory.getLog(ActivityJavaUpdateTask.class);
    
    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
                        IVersion toVersion)  throws JavaUpdateException {
        try {
            if (fromVersion.compareTo(VERSION_1_6) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFrom1_6(connection, cm);
            } 
    
            if (fromVersion.compareTo(VERSION_2_0) <= 0  && toVersion.compareTo(fromVersion) > 0){
                upgradeFrom2_0(connection, cm);
            }
   
            if (fromVersion.compareTo(VERSION_4_5) <= 0  && toVersion.compareTo(fromVersion) > 0){
                upgradeFrom4_5(connection, cm);
            }
            
            if (fromVersion.compareTo(VERSION_7_5_1) <= 0
					&& toVersion.compareTo(fromVersion) > 0) {
				upgradeTo7_6(connection, cm);
			}
            
        } catch (SQLException e) {
            throw new JavaUpdateException(e, JavaUpdateException.Type.SQL);
        } catch (HibernateException e) {
            throw new JavaUpdateException(e, JavaUpdateException.Type.HIBERNATE);
        }
    }
 
	private void upgradeFrom1_6(Connection connection, ConfigurationMod cm) throws SQLException {
        //change report.timestamp, report.enddate from timestamp to long
        updateChangeTimestampToLong(connection, cm, "begindate");
        updateChangeTimestampToLong(connection, cm, "enddate");

        resetLastCacheTimestamp(connection);

        //clean up all cache
        String[] removeTablesName = { "cached_user", "cached_usergroup",
                                      "cached_usergroup_member" };
        for (String removeTableName : removeTablesName) {
            removeTableName = DatabaseHelper.matchToDbStoreCase(removeTableName);
            DatabaseHelper.processSqlStatements(connection, Collections
                                                .singletonList("DELETE FROM " + removeTableName));
        }
        LOG.info("done");
    }
 
    private void upgradeFrom2_0(Connection connection, ConfigurationMod cm) throws SQLException,
                                                                                   HibernateException {
        /*
         * When you rename an Oracle table you must be aware that Oracle does not update 
         * applications (HTML-DB, PL/SQL that referenced the old table name) and PL/SQL 
         * procedures may become invalid.
         */
        Dialect dialect = cm.getDialect();
  
        String oldTableName = DatabaseHelper.matchToDbStoreCase("PolicyLogReportDetailResults");
        String newTableName = DatabaseHelper.matchToDbStoreCase("POLICY_LOG_REPORT_DETAILS");
        renameTable(connection, dialect, oldTableName, newTableName);
  
        oldTableName = DatabaseHelper.matchToDbStoreCase("TrackingLogReportDetailResults");
        newTableName = DatabaseHelper.matchToDbStoreCase("TRACKING_LOG_REPORT_DETAILS");
        renameTable(connection, dialect, oldTableName, newTableName);
  
        oldTableName = DatabaseHelper.matchToDbStoreCase("ReportSummaryResultDO");
        newTableName = DatabaseHelper.matchToDbStoreCase("REPORT_SUMMARY_RESULTS");
        renameTable(connection, dialect, oldTableName, newTableName);
        LOG.info("done");
    }

    private void renameTable(Connection connection, Dialect dialect, String oldTableName,
                             String newTableName) throws SQLException {
        //pre-schema may created the tables already
        try {
            DatabaseHelper.dropTable(connection, dialect, newTableName);
        } catch (SQLException expected) {
            LOG.debug("expected exception", expected);
        }
        DatabaseHelper.renameTable(connection, oldTableName, newTableName);
    }

    private void resetLastCacheTimestamp(Connection connection) throws SQLException {
        String tableName = DatabaseHelper.matchToDbStoreCase("resource_cache_state");
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName
                                                                        + " SET lastupdated = ?" 
                                                                        + " WHERE type = ?");

        updateStatement.setLong(1, 1);
        updateStatement.setString(2, "U");
        int numUpdates = updateStatement.executeUpdate();
        LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
    }

    private void updateChangeTimestampToLong(Connection connection, ConfigurationMod cm,
                                             String orginalColumnName) throws SQLException {
        String tableName = DatabaseHelper.matchToDbStoreCase("report");
        String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
        String columnName = DatabaseHelper.matchToDbStoreCase(orginalColumnName);
        String tempColumnName = DatabaseHelper.getColumnTempName(tableName, columnName, cm);


        List<IPair<Object,Object>> records = DatabaseHelper.getColumnData(connection, tableName,
                                                                          idColumnName, tempColumnName);

        PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName
                                                                        + " SET " + columnName + " = ?" 
                                                                        + " WHERE " + idColumnName + " = ?");

        for (IPair<Object, Object> record : records) {
            if (record.second() != null) {
                Timestamp timestamp = Timestamp.valueOf(record.second().toString());
                Long newValue = timestamp.getTime();
                updateStatement.setLong(1, newValue);
                updateStatement.setInt(2, Integer.parseInt(record.first().toString()));
                int numUpdates = updateStatement.executeUpdate();
                LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
            }
        }
    }
    
	private void upgradeTo7_6(Connection connection, ConfigurationMod cm)
			throws SQLException {
		LOG.info("Start upgrade to 7.6 changes");
		String tableName = DatabaseHelper.matchToDbStoreCase("saved_reports");
		String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
		String old_isSharedColumnName = DatabaseHelper
				.matchToDbStoreCase("is_shared");
		String new_sharedModeColumnName = DatabaseHelper
				.matchToDbStoreCase("shared_mode");

		List<IPair<Object, Object>> records = DatabaseHelper.getColumnData(
				connection, tableName, idColumnName, old_isSharedColumnName);

		PreparedStatement updateStatement = connection
				.prepareStatement("UPDATE " + tableName + " SET "
						+ new_sharedModeColumnName + " = ?" + " WHERE "
						+ idColumnName + " = ?");

		for (IPair<Object, Object> record : records) {
			if (record.second() != null) {
				String value = record.second().toString();

				if (value.equals("1") || value.equalsIgnoreCase("true")) {
					updateStatement.setString(1, "public");
				} else {
					updateStatement.setString(1, "only_me");
				}
				updateStatement.setInt(2,
						Integer.parseInt(record.first().toString()));
				int numUpdates = updateStatement.executeUpdate();
				LOG.info(numUpdates + " row(s) updated. "
						+ updateStatement.toString());
			}
		}
		
		LOG.info("Upgrade to 7.6 Done");
	}
 
    private void upgradeFrom4_5(Connection connection, ConfigurationMod cm) throws SQLException,
                                                                                   HibernateException {
        DialectExtended dialectX = DialectExtended.getDialectExtended(cm.getDialect());
     
        Map<String, String[]> indexesTable = new HashMap<String, String[]>();
     
        indexesTable.put("POLICY_ACTIVITY_LOG", new String[]{
            "policyActivityTimeIndex"
            , "policyActivityTimeMonthIndex"
            , "policyActivityTimeDayIndex"
            , "policyActivityPolicyIndex"
            , "policyActivityUserIndex"
            , "policyActivityUserNameIndex"
            , "policyActivityActionIndex"
            , "paPolicyDecisionIndex"
            , "policyActivityLevelIndex"
            , "paFromResourceNameIndex"
        });
     
        indexesTable.put("TRACKING_ACTIVITY_LOG", new String[]{
            "trackingActivityActionIndex"
            , "trackingActivityTimeIndex"
            , "trackingActivityTimeMonthIndex"
            , "trackingActivityTimeDayIndex"
            , "trackingActivityUserIndex"
            , "trackingActivityUserNameIndex"
            , "trackingActivityLevelIndex"
            , "taFromResourceNameIndex"
        });
     
        List<String> statements = new LinkedList<String>();
     
        for (Map.Entry<String, String[]> e : indexesTable.entrySet()) {
            String tableName = e.getKey();
            tableName = DatabaseHelper.matchToDbStoreCase(tableName);
            for (String indexName : e.getValue()) {
                indexName = DatabaseHelper.matchToDbStoreCase(indexName);
                if (DatabaseHelper.isIndexExists(connection.getMetaData(), dialectX, tableName,
                                                 indexName)) {
                    String sqlstatement = dialectX.sqlDropIndex(indexName, tableName);
                    statements.add(sqlstatement);
                }
            }
        }
        DatabaseHelper.processSqlStatements(connection, statements);
     
        LOG.info("done");
    }
}
