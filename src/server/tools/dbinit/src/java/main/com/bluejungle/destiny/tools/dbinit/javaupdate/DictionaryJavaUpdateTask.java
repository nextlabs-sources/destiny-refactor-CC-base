package com.bluejungle.destiny.tools.dbinit.javaupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.version.IVersion;

import net.sf.hibernate.HibernateException;

/**
 * @author Hor-kan Chan
 * @date Mar 14, 2007
 */
public class DictionaryJavaUpdateTask extends BaseJavaUpdateTask {
    private static final Log LOG = LogFactory.getLog(DictionaryJavaUpdateTask.class);
 
    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
                        IVersion toVersion) throws JavaUpdateException {
        try {
            if (fromVersion.compareTo(VERSION_1_6) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFrom1_6(connection, cm);
            } 
   
            if (fromVersion.compareTo(VERSION_2_0) <= 0 && toVersion.compareTo(fromVersion) > 0) {
                upgradeFrom2_0(connection, cm);
            } 
    
            if (fromVersion.compareTo(VERSION_2_5) <= 0 && toVersion.compareTo(fromVersion) > 0){
                upgradeFrom2_5(connection, cm);
            } 
        } catch (SQLException e) {
            throw new JavaUpdateException(e, JavaUpdateException.Type.SQL);
        } catch (HibernateException e) {
            throw new JavaUpdateException(e, JavaUpdateException.Type.HIBERNATE);
        }
    }
 
    private void upgradeFrom2_5(Connection connection, ConfigurationMod cm) throws SQLException,
                                                                                   JavaUpdateException, HibernateException {
        final DialectExtended dialect = DialectExtended.getDialectExtended(cm.getDialect());

        final int maxLength = 900;
  
        String tableName = DatabaseHelper.matchToDbStoreCase("DICT_ELEMENTS");
        String columnName = DatabaseHelper.matchToDbStoreCase("PATH");
        int result = DatabaseHelper.checkLength(connection, dialect, tableName, columnName, " >"
                                                + maxLength);
        if (result > 0) {
            throw JavaUpdateException.reachMaxLength(tableName, columnName, maxLength, result);
        }

        tableName = DatabaseHelper.matchToDbStoreCase("DICT_ELEMENTS");
        columnName = DatabaseHelper.matchToDbStoreCase("DICTIONARY_KEY");
        result = DatabaseHelper.checkLength(connection,  dialect,  tableName, columnName," >" + maxLength);
        if (result > 0) {
            throw JavaUpdateException.reachMaxLength(tableName, columnName, maxLength, result);
        }

        tableName = DatabaseHelper.matchToDbStoreCase("DICT_ENUM_REF_MEMBERS");
        columnName = DatabaseHelper.matchToDbStoreCase("PATH");
        result = DatabaseHelper.checkLength(connection,  dialect, tableName, columnName, " >" + maxLength);
        if (result > 0) {
            throw JavaUpdateException.reachMaxLength(tableName, columnName, maxLength, result);
        }

        tableName = DatabaseHelper.matchToDbStoreCase("DICT_STRUCT_GROUPS");
        columnName = DatabaseHelper.matchToDbStoreCase("FILTER");
        result = DatabaseHelper.checkLength(connection,  dialect, tableName, columnName, " >"
                                            + (maxLength + 2));
        if (result > 0) {
            throw JavaUpdateException.reachMaxLength(tableName, columnName, maxLength + 2, result);
        }
  
  
        //crop last 2 chars in the column "filter"
        tableName = DatabaseHelper.matchToDbStoreCase("DICT_STRUCT_GROUPS");
        String idColumnName = DatabaseHelper.matchToDbStoreCase("ELEMENT_ID");
        columnName = DatabaseHelper.matchToDbStoreCase("FILTER");
  
        List<IPair<Object, Object>> records = DatabaseHelper.getColumnData(connection, tableName,
                                                                           idColumnName, columnName);
  
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + tableName
                                                                        + " SET " + columnName + " = ?" 
                                                                        + " WHERE " + idColumnName + " = ?");

        for(IPair<Object, Object> record : records) {
            String newRecord = (String)record.second();
            if(newRecord != null && newRecord.length() > 2){
                newRecord = newRecord.substring(0, newRecord.length()-2);
                updateStatement.setString(1, newRecord);
                updateStatement.setInt(2, Integer.parseInt(record.first().toString()));
                int numUpdates = updateStatement.executeUpdate();
            }
        }
  
        //update column "filterLength"
        String query = "UPDATE %s SET %s = %s(%s)";
        columnName = DatabaseHelper.matchToDbStoreCase("filterLength");
        String sourceColumnName = DatabaseHelper.matchToDbStoreCase("FILTER");
        query = String.format(query, tableName, columnName, dialect.getLengthString(),
                              sourceColumnName);

        DatabaseHelper.processSqlStatement(connection, query);
        LOG.trace("done");
    }
 
    /**
     * bugfix 6400
     *   - Remove structural group entries for childless groups:
     *   - Remove the now-orphaned dict_elements that corresponded to the deleted
     * @param connection
     * @param cm
     * @throws SQLException
     * @throws JavaUpdateException
     * @throws HibernateException
     */
    private void upgradeFrom2_0(Connection connection, ConfigurationMod cm) throws SQLException,
                                                                                   JavaUpdateException, HibernateException {
        final DialectExtended dialect = DialectExtended.getDialectExtended(cm.getDialect());
  
        // Remove structural group entries for childless groups:
        final String DICT_STRUCT_GRUOPS = DatabaseHelper.matchToDbStoreCase("dict_struct_groups");
        final String ELEMENT_ID = DatabaseHelper.matchToDbStoreCase("element_id");
        final String ID = DatabaseHelper.matchToDbStoreCase("id");
        final String DICT_ELEMENTS = DatabaseHelper.matchToDbStoreCase("dict_elements");
        final String PATH = DatabaseHelper.matchToDbStoreCase("path");
        final String FILTERLENGTH = DatabaseHelper.matchToDbStoreCase("filterlength");
        final String FILTER = DatabaseHelper.matchToDbStoreCase("filter");
  
        String query =
            "delete from "+DICT_STRUCT_GRUOPS+" where "+ELEMENT_ID+" in ( "
            + "select e."+ID+" from "+DICT_ELEMENTS+" e "
            + "join "+DICT_STRUCT_GRUOPS+" s on e."+ID+"=s."+ELEMENT_ID+" "
            + "left join "+DICT_ELEMENTS+" x on "+dialect.getSubStringString()+"(x."+PATH+",1,s."+FILTERLENGTH+")=s."+FILTER+" "
            + "left join "+DICT_ELEMENTS+" l on l."+ID+"=x."+ID+" "
            + "group by e."+ID+",e."+PATH+" "
            + "having count(l."+ID+")=0 "
            +")";
        DatabaseHelper.processSqlStatement(connection, query);
  
        // Remove the now-orphaned dict_elements that corresponded to the deleted structural groups:
        final String DICT_ENUM_GROUPS = DatabaseHelper.matchToDbStoreCase("dict_enum_groups");
        final String DICT_LEAF_ELEMENTS = DatabaseHelper.matchToDbStoreCase("dict_leaf_elements");
        query =
            "delete from "+DICT_ELEMENTS+" where "+ID+" in ( "
            +"select "+ID+" from "+DICT_ELEMENTS+" e "
            +"left join "+DICT_LEAF_ELEMENTS+" l on e."+ID+"=l."+ELEMENT_ID+" "
            +"left join "+DICT_ENUM_GROUPS+" g on e."+ID+"=g."+ELEMENT_ID+" "
            +"left join "+DICT_STRUCT_GRUOPS+" s on e."+ID+"=s."+ELEMENT_ID+" "
            +"where l."+ELEMENT_ID+" is null and g."+ELEMENT_ID+" is null and s."+ELEMENT_ID+" is null "
            +")";
        DatabaseHelper.processSqlStatement(connection, query);
        LOG.trace("done");
    }

    private void upgradeFrom1_6(Connection connection, ConfigurationMod cm)
        throws HibernateException, SQLException {
        String tableName = DatabaseHelper.matchToDbStoreCase("dict_leaf_elements");
        String tempColumnName = DatabaseHelper.matchToDbStoreCase("string_temp");
  
        //create a temp column, hopefully the temp column doesn't exist
        //FIXME check if the temp column name is not exist before create
        String sqlType = cm.getDialect().getTypeName(Types.VARCHAR, 256 );
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + tempColumnName + " " + sqlType;
        DatabaseHelper.processSqlStatement(connection, sql);

        //shift the column if the type is 1
        // bring string11 to the front(string00)
        List<String> sqls = new ArrayList<String>();
        String lastColumnName =DatabaseHelper.matchToDbStoreCase("string11");
        String type = DatabaseHelper.matchToDbStoreCase("type_id");
        sqls.add("UPDATE " + tableName + " SET " + tempColumnName + " = " + lastColumnName 
                 + " WHERE " + type + " = 1");
        for(int i=11; i >0; i--){
            String columnA =DatabaseHelper.matchToDbStoreCase("string" + (i < 10 ? "0" : "") + i );
            int j=i-1;
            String columnB =DatabaseHelper.matchToDbStoreCase("string" + (j < 10 ? "0" : "") + j );
            sqls.add("UPDATE " + tableName + " SET " + columnA + " = " + columnB 
                     + " WHERE " + type + " = 1");
        }
        String firstColumnName =DatabaseHelper.matchToDbStoreCase("string00");
        sqls.add("UPDATE " + tableName + " SET " + firstColumnName + " = " + tempColumnName 
                 + " WHERE " + type + " = 1");
  
        DatabaseHelper.processSqlStatements(connection, sqls);
  
  
        // shift the column if the type is 15
        // bring string03 to the front(string00)
        sqls = new ArrayList<String>();
        lastColumnName =DatabaseHelper.matchToDbStoreCase("string03");
        type = DatabaseHelper.matchToDbStoreCase("type_id");
        sqls.add("UPDATE " + tableName + " SET " + tempColumnName + " = " + lastColumnName 
                 + " WHERE " + type + " = 15");
        for(int i=3; i >0; i--){
            String columnA =DatabaseHelper.matchToDbStoreCase("string" + (i < 10 ? "0" : "") + i );
            int j=i-1;
            String columnB =DatabaseHelper.matchToDbStoreCase("string" + (j < 10 ? "0" : "") + j );
            sqls.add("UPDATE " + tableName + " SET " + columnA + " = " + columnB 
                     + " WHERE " + type + " = 15");
        }
        firstColumnName =DatabaseHelper.matchToDbStoreCase("string00");
        sqls.add("UPDATE " + tableName + " SET " + firstColumnName + " = " + tempColumnName 
                 + " WHERE " + type + " = 15");
  
        DatabaseHelper.processSqlStatements(connection, sqls);
        LOG.trace("done");
    }
}
