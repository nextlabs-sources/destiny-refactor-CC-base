/**
 * 
 */
package com.bluejungle.destiny.tools.dbinit.javaupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.version.IVersion;


/**
 * @author Hor-kan Chan
 * @date Mar 14, 2007
 */
public class ManagementJavaUpdateTask extends BaseJavaUpdateTask {
    private static final Log LOG = LogFactory.getLog(ManagementJavaUpdateTask.class);
    
    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
			IVersion toVersion) throws JavaUpdateException {
        try {
            if (fromVersion.compareTo(VERSION_1_6) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFrom1_6(connection, cm);
            }

            // The comparison and method name are at odds. I believe we only need to do this if we are
            // earlier than 4.5, but we are doing it if we are 4.5 or earlier. I think this is harmless.
            if (fromVersion.compareTo(VERSION_4_5) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFromBefore4_5(connection, cm);
            }
			
            // Likewise here...
            if (fromVersion.compareTo(VERSION_5_1) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeComponentEnumToString(connection, cm);
            }

            // Here it is definitely *not* harmless. 6.5 added a new row to the db and we don't want to
            // do that if we are upgrading from 6.5, just from *pre* 6.5
            if (fromVersion.compareTo(VERSION_6_5_0) < 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFromBefore6_5(connection, cm);
            }
            
        } catch (SQLException e) {
            throw new JavaUpdateException(e);
        }
    }

    private void upgradeFrom1_6(Connection connection, ConfigurationMod cm) throws SQLException {
        /*
         * Agent table – The column “type” has been changed from a “char(1)” to a “string” (these 
         * 		are hibernate data types, so the underlying database data types may vary) 
         * Agent table – The column “type” requires a data upgrade. 
         * 		“D” -> "DESKTOP“ 
         * 		“F” -> “FILE_SERVER” 
         * comm_profile table – The column “agent_type” has been changed from a “char(1)” to 
         * 		a “string” (these are hibernate data types, so the underlying database data types may vary) 
         * comm_profile table – The column “agent_type” requires a data upgrade. 
         * 		“D” -> "DESKTOP“ 
         * 		“F” -> “FILE_SERVER” 
         * comm_profile table – The column “curr_act_journ_settings_id” has been removed 
         * comm_profile table – The column “curr_act_journ_settings_name” has been added 
         * comm_profile table – A data upgrade must take place to replace the id’s in 
         * 		the “curr_act_journ_settings_id” column with the corresponding names in 
         * 		the “curr_act_journ_settings_name” 
         * “activity_journaling_settings” table – 
         * 		A data upgrade here is needed to remove a few records. 
         * 		More specifically, those with the names, “Extended”, “Minimum”, and “Default” 
         */
		
        connection.setAutoCommit(false);

        upgradeAgentType(connection, cm);
        upgradeCommProfileAgentType(connection, cm);
        upgradeCurrActJournIdName(connection);
        removeUnmappedActsSetting(connection);
        removeSettingRecords(connection);
        connection.commit();
        connection.setAutoCommit(true);
		
        LOG.info("done");
    }

    /**
     * remove all the records that in table "act_journ_settings_logged_acts" has refernfce to 
     * table "activity_journaling_settings
     * @param connection
     * @throws SQLException
     */
    private void removeUnmappedActsSetting(Connection connection) throws SQLException {
        LOG.info("action4");
        String tableName = DatabaseHelper.matchToDbStoreCase("act_journ_settings_logged_acts");
        String columnName = DatabaseHelper.matchToDbStoreCase("settings_id");
		
        String refTableName = DatabaseHelper.matchToDbStoreCase("activity_journaling_settings");
        String refColumnName = DatabaseHelper.matchToDbStoreCase("id");
        String sourceColumnName = DatabaseHelper.matchToDbStoreCase("name");
		
		
        PreparedStatement updateStatement = connection.prepareStatement(
            "DELETE FROM " + tableName + 
        " WHERE " + columnName + " = " + refTableName + "." + refColumnName + 
        " AND " + refTableName +"." + sourceColumnName + " = ?");
        String[] removeValues = {"Extended", "Minimum", "Default"};
        for (int i = 0; i < removeValues.length; i++) {
            updateStatement.setString(1, removeValues[i]);
            updateStatement.addBatch();
            int numUpdates = updateStatement.executeUpdate();
            LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
        }
    }
	
    /**
     * Agent table – The column “type” has been changed from a “char(1)” to a “string” 
     * (these are hibernate data types, so the underlying database data types may vary) 
     * Agent table – The column “type” requires a data upgrade. 
     * 		“D” -> "DESKTOP“ 
     * 		“F” -> “FILE_SERVER” 
     * @param connection
     * @param cm
     * @throws SQLException
     */
    private void upgradeAgentType(Connection connection, ConfigurationMod cm) throws SQLException{
        LOG.info("action1");
        String tableName = DatabaseHelper.matchToDbStoreCase("agent");
        String columnName = DatabaseHelper.matchToDbStoreCase("type");
        String tempName = DatabaseHelper.getColumnTempName(tableName, columnName, cm);
		
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName + 
                                                                        " SET "	+ columnName + " = ?" + 
                                                                        " WHERE " + tempName + " = ?");
		

        String[] newValues = { "DESKTOP", "FILE_SERVER", };
        String[] orginalValues = { "D", "F", };

        for (int i = 0; i < orginalValues.length; i++) {
            updateStatement.setString(1, newValues[i]);
            updateStatement.setString(2, orginalValues[i]);
            updateStatement.addBatch();
            int numUpdates = updateStatement.executeUpdate();
            LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
        }
    }
	
    /**
     * comm_profile table – The column “agent_type” has been changed from a “char(1)” to 
     * a “string” (these are hibernate data types, so the underlying database data types may vary) 
     * comm_profile table – The column “agent_type” requires a data upgrade. 
     * 		“D” -> "DESKTOP“ 
     * 		“F” -> “FILE_SERVER” 
     * @param connection
     * @param cm
     * @throws SQLException
     */
    private void upgradeCommProfileAgentType(Connection connection, ConfigurationMod cm)
        throws SQLException {
        LOG.info("action2");
        String tableName = DatabaseHelper.matchToDbStoreCase("comm_profile");
        String columnName = DatabaseHelper.matchToDbStoreCase("agent_type");
        String tempName = DatabaseHelper.getColumnTempName(tableName, columnName, cm);
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName + 
                                                                        " SET " + columnName + " = ?"
                                                                        + " WHERE " + tempName + " = ?");
		
        String[] newValues = {"DESKTOP", "FILE_SERVER",};
        String[] orginalValues = {"D", "F",};
		
        for (int i = 0; i < orginalValues.length; i++) {
            updateStatement.setString(1, newValues[i]);
            updateStatement.setString(2, orginalValues[i]);
            int numUpdates = updateStatement.executeUpdate();
            LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
        }
    }
	
    /**
     * comm_profile table – A data upgrade must take place to replace the id’s 
     * in the “curr_act_journ_settings_id” column with the corresponding names 
     * in the “curr_act_journ_settings_name”
     * @param connection
     * @throws SQLException
     */
    private void upgradeCurrActJournIdName(Connection connection) throws SQLException{
        LOG.info("action3");
        String targetTableName = DatabaseHelper.matchToDbStoreCase("comm_profile");
        String targetColumnName = DatabaseHelper.matchToDbStoreCase("curr_act_journ_settings_name");
        String keyRefFromColumnName = DatabaseHelper.matchToDbStoreCase("curr_act_journ_settings_id");
		
        String sourceTableName = DatabaseHelper.matchToDbStoreCase("activity_journaling_settings");
        String keyRefToColumnName = DatabaseHelper.matchToDbStoreCase("id");
        String sourceColumnName = DatabaseHelper.matchToDbStoreCase("name");
        Statement statement = connection.createStatement();
        statement.execute("UPDATE " + targetTableName + 
                          " SET " + targetColumnName + " = " + sourceTableName + "." + sourceColumnName +
                          " WHERE " + keyRefFromColumnName + " = " + sourceTableName + "."+ keyRefToColumnName);
    }
	
    private void removeSettingRecords(Connection connection) throws SQLException{
        LOG.info("action5");
        /*“activity_journaling_settings” table – 
         * 		A data upgrade here is needed to remove a few records. 
         * 		More specifically, those with the names, “Extended”, “Minimum”, and “Default”
         */ 
        String tableName = DatabaseHelper.matchToDbStoreCase("activity_journaling_settings");
        String columnName = DatabaseHelper.matchToDbStoreCase("name");
        PreparedStatement updateStatement = connection.prepareStatement("DELETE FROM " + tableName 
                                                                        + " WHERE " + columnName + " = ?");
        String[] removeValues = {"Extended", "Minimum", "Default"};
        for (int i = 0; i < removeValues.length; i++) {
            updateStatement.setString(1, removeValues[i]);
            int numUpdates = updateStatement.executeUpdate();
            LOG.info(numUpdates + " row(s) updated. " + updateStatement.toString());
        }
    }


    private void upgradeFromBefore4_5(Connection connection, ConfigurationMod cm) throws SQLException {
        String agentT = DatabaseHelper.matchToDbStoreCase("AGENT");
        String hostC = DatabaseHelper.matchToDbStoreCase("host");
        String typeC = DatabaseHelper.matchToDbStoreCase("type");
        String idC = DatabaseHelper.matchToDbStoreCase("id");
        String registeredC = DatabaseHelper.matchToDbStoreCase("registered");
        String lastHeartbeatC = DatabaseHelper.matchToDbStoreCase("lastHeartbeat");
    
        String allFiels = ArrayUtils.asString(new String[]{
            idC,
            hostC,
            typeC,
            registeredC,
            lastHeartbeatC,
        }, ",", "a.");
    
    
	  
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
            "SELECT " + allFiels+ " FROM " + agentT + " a JOIN"
        + " (SELECT " + hostC + "," + typeC + " FROM " + agentT
        + " GROUP BY " + hostC + "," + typeC
        + " HAVING COUNT(*) > 1) b"
        + " on (a."+hostC+" = b."+hostC+" and a."+typeC+" = b."+typeC+") "
        + " order by " + hostC + "," + typeC
            );
	  
        Set<AgentPart> duplicatedAgents = new HashSet<AgentPart>();
        AgentPart lastOne= null;
        while(rs.next()){
	    long id = rs.getLong(idC);
	    String host = rs.getString(hostC);
	    String type = rs.getString(typeC);
	    boolean registered = rs.getBoolean(registeredC);
	    long lastHearbeat = rs.getLong(lastHeartbeatC);
	    AgentPart duplicatedAgent = new AgentPart(id, host, type, registered, lastHearbeat);
	    
	    if(lastOne != null 
               && !(lastOne.host.equals(duplicatedAgent.host) 
	            &&lastOne.type.equals(duplicatedAgent.type)
                    )
               ) {
                //time to delete duplicated agents
	      
                deleteAgent(findDuplicated(duplicatedAgents), connection);
	      
	      
                duplicatedAgents.clear();
	    }
	    lastOne = duplicatedAgent;
	    duplicatedAgents.add(duplicatedAgent);
        }
	  
        if (!duplicatedAgents.isEmpty()) {
            deleteAgent(findDuplicated(duplicatedAgents), connection);
        }
        statement.close();
    }

    private static final String DDAC_LOCATION = "DDACLocation";

    private void upgradeFromBefore6_5(Connection connection, ConfigurationMod cm) throws SQLException {
        ComponentInfo<AgentManager> agentMgrCompInfo =
            new ComponentInfo<AgentManager>(IAgentManager.COMP_NAME, 
                                            AgentManager.class, 
                                            IAgentManager.class, 
                                            LifestyleType.SINGLETON_TYPE);
        IAgentManager agentManager = ComponentManagerFactory.getComponentManager().getComponent(agentMgrCompInfo);

        ComponentInfo<HibernateProfileManager> profileMgrCompInfo = 
            new ComponentInfo<HibernateProfileManager>(IProfileManager.COMP_NAME, 
                                                       HibernateProfileManager.class, 
                                                       IProfileManager.class, 
                                                       LifestyleType.SINGLETON_TYPE);

        HibernateProfileManager hibernateProfileManager = ComponentManagerFactory.getComponentManager().getComponent(profileMgrCompInfo);

        IConfiguration config = getConfiguration();
        Properties props = config.get(CONFIG_PROPS_CONFIG_PARAM);

        // Set the comm profile seed data
        IAgentType activeDirectoryAgentType = agentManager.getAgentType(AgentTypeEnumType.ACTIVE_DIRECTORY.getName());

        final String ddacLocation = (String) props.get(DDAC_LOCATION);

        try {
            hibernateProfileManager.createDefaultProfileForUpgrade(activeDirectoryAgentType, ddacLocation, props);
        } catch (DataSourceException e) {
            LOG.error("Got exception " + e);
            throw new SQLException("Unable to create default profile for DAC", e);
        }
        
        return;
    }


    private void deleteAgent(Set<AgentPart> badAgents, Connection connection) throws SQLException{
        StringBuilder sb = new StringBuilder();
        Iterator<AgentPart> it = badAgents.iterator();
        while (it.hasNext()) {
            sb.append(it.next().id);
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        String allBadAgentIds = sb.toString();
    
        String targetAgentT = DatabaseHelper.matchToDbStoreCase("TARGET_AGENT");
        DatabaseHelper.processSqlStatement(connection, 
                                           "DELETE FROM " + targetAgentT + " WHERE agent_id in (" + allBadAgentIds + ")");
    
        String agentRegistrationT = DatabaseHelper.matchToDbStoreCase("AGENT_REGISTRATION");
        DatabaseHelper.processSqlStatement(connection, 
                                           "DELETE FROM " + agentRegistrationT + " WHERE agent_id in (" + allBadAgentIds + ")" );
    
        String agentT = DatabaseHelper.matchToDbStoreCase("AGENT");
        DatabaseHelper.processSqlStatement(connection, 
                                           "DELETE FROM " + agentT + " WHERE id in (" + allBadAgentIds + ")" );
    
    

    }
	
    private class AgentPart{
        final long id;        // not null
        final String host;      // not null
        final String type;      // not null
        final boolean registered;  // not null
        final long lastHearbeat;   // null, null will be 0, 0 will also be 0
    
        AgentPart(long id, String host, String type, boolean registered, long lastHearbeat) {
            super();
            this.id = id;
            this.host = host;
            this.type = type;
            this.registered = registered;
            this.lastHearbeat = lastHearbeat;
        }

        @Override
        public String toString() {
            return Long.toString(id);
        }
    }
	
    /**
     * the order to identify bad duplicated entry
     * 1. not registered
     * 2. doesn't have latest heartbeat
     * 3. doesn't have the earliest id (this is the most unsafe way 
     *   since the database id doesn't have to be in order)
     * 
     * @param duplicatedAgents
     * @return all the duplicated bad agents, they should be deleted.
     */
    private Set<AgentPart> findDuplicated(Set<AgentPart> duplicatedAgents){
        Set<AgentPart> clone = new HashSet<AgentPart>(duplicatedAgents);
	  
        //at the end, there should be only one good agent
        Set<AgentPart> goodAgents = new HashSet<AgentPart>(clone);
        for (AgentPart a : clone) {
            if (!a.registered) {
                goodAgents.remove(a);
            }
        }
        if (goodAgents.size() == 1) {
            Set<AgentPart> badAgents = new HashSet<AgentPart>(duplicatedAgents);
            badAgents.removeAll(goodAgents);
            return badAgents;
        } else if (goodAgents.size() == 0) {
            //all of them are unregistered, so all of them is good
            goodAgents = new HashSet<AgentPart>(clone);
        } else if (goodAgents.size() > 1) {
            //keep checking!
        }
    
    
    
        AgentPart[] goodAgentArray = goodAgents.toArray(new AgentPart[goodAgents.size()]);
    
        //sort from latest(largest) heartbeat to earliest(smallest) heartbeat
        Arrays.sort(goodAgentArray, new Comparator<AgentPart>() {
            public int compare(AgentPart o1, AgentPart o2) {
                return o1.lastHearbeat < o2.lastHearbeat? 1 
                    : o1.lastHearbeat > o2.lastHearbeat ? -1
                    : o1.id < o2.id ? 1 : -1    
                    ;
            }
        });
    
        long latestHeartbeat = goodAgentArray[0].lastHearbeat;
    
        clone = new HashSet<AgentPart>(goodAgents);
        for (AgentPart a : clone) {
            if( a.lastHearbeat < latestHeartbeat ){
                goodAgents.remove(a);
            }
        }
    
        if (goodAgents.size() == 1) {
            Set<AgentPart> badAgents = new HashSet<AgentPart>(duplicatedAgents);
            badAgents.removeAll(goodAgents);
            return badAgents;
        } else if (goodAgents.size() == 0) {
            //all of them have same last heartbeat, so all of them is good
            goodAgents = new HashSet<AgentPart>(clone);
        } else if (goodAgents.size() > 1) {
            //keep checking!
        }
    
    
    
        // all right, nothing works perfectly
        // database id is my last resort!
    
        Set<AgentPart> badAgents = new HashSet<AgentPart>(duplicatedAgents);
        badAgents.remove(goodAgentArray[0]);
        return badAgents;
    }
	
    /**
     * also apply for 5.1
     * @param connection
     * @param cm
     * @throws SQLException
     */
    private void upgradeComponentEnumToString(Connection connection, ConfigurationMod cm) throws SQLException {
        String componentT = DatabaseHelper.matchToDbStoreCase("COMPONENT");
        String typeC = DatabaseHelper.matchToDbStoreCase("type");
        String typeCTemp = DatabaseHelper.getColumnTempName(componentT, typeC, cm);
    
        String typeDisplayNameC = DatabaseHelper.matchToDbStoreCase("typeDisplayName");

        final String updateSqlTemplate = 
            "UPDATE " + componentT 
            + " SET " + typeC + " = '%2$s', " + typeDisplayNameC + " = '%3$s'" 
            + " WHERE " + typeCTemp + " = '%1$s'";
    
        String[][] datas = new String[][]{
            {"B", "DABS", "ICENet Server"}
            , {"I", "DAC", "Intelligence Server"}
            , {"C", "DCSF", "Communication Server"}
            , {"E", "DEM", "Enrollment Manager"}
            , {"S", "DMS", "Management Server"}
            , {"P", "DPS", "Policy Management Server"}
            , {"M", "MGMT_CONSOLE", "Administrator"}
            , {"R", "REPORTER", "Reporter"}
        };

        List<String> sqls = new ArrayList<String>(datas.length + 1);
    
        for(String[] data : datas){
            LOG.info("updating component" + data[0] + " to " + data[1] + " (" + data[2] + ") ");
            String sql = String.format(updateSqlTemplate, data[0], data[1], data[2]);
            sqls.add(sql);
        }
        DialectExtended dialectX = cm.getDialect();
    
        sqls.add(dialectX.sqlDropIndex(DatabaseHelper.matchToDbStoreCase("ComponentType"), componentT));
        sqls.add("CREATE INDEX ComponentType ON COMPONENT (type)");
        DatabaseHelper.processSqlStatements(connection, sqls);
    }
}
