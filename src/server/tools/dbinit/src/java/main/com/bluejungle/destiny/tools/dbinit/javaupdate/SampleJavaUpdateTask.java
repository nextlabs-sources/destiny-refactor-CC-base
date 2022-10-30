package com.bluejungle.destiny.tools.dbinit.javaupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.version.IVersion;

public class SampleJavaUpdateTask extends BaseJavaUpdateTask {
    private String tableName = DatabaseHelper.matchToDbStoreCase("stored_query");
    private String idColumnName = DatabaseHelper.matchToDbStoreCase("id");

    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
                        IVersion toVersion) throws JavaUpdateException {
        System.out.println("--hi --hi --hi");
    }

    private void usePerpareStatement(Connection connection, ConfigurationMod cm) throws SQLException {
        String columnName = DatabaseHelper.matchToDbStoreCase("resultobjectname");
        String tempColumnName = "resultobjectname2";
        //how to use perpared statement
        connection.setAutoCommit(false);  
  
        PreparedStatement updateStatement = 
            connection.prepareStatement("UPDATE "+ tableName+
                                        " SET " +tempColumnName+" = ? "+
                                        "WHERE " + columnName +" = ?");
  
        String[] newValues = {"ALPHA", "BOY", "CAT", "DOG"};
        String[] orginalValues = {"A", "B", "C", "D"};
  
        for(int i=0; i< orginalValues.length; i++){
            updateStatement.setString(1, newValues[i]);
            updateStatement.setString(2, orginalValues[i]);
            updateStatement.addBatch();
            int [] numUpdates = updateStatement.executeBatch();
            for (int j=0; j < numUpdates.length; j++) {
                if (numUpdates[j] == -2)
                    System.out.println("Execution " + j + ": unknown number of rows updated");
                else
                    System.out.println("Execution " + j + "successful: " + numUpdates[j] + " rows updated");
            }
   
        }
  
        connection.commit();
        connection.setAutoCommit(true);
    }

    private void replaceValue(Connection connection) throws SQLException {
        String columnName = DatabaseHelper.matchToDbStoreCase("resultobjectname");

        //how to replace value
  
        List<IPair<Object,Object>> list = DatabaseHelper.getColumnData(connection, tableName, idColumnName, columnName);
        for(IPair<Object,Object> po : list){
            String id = po.first().toString();
            String column = po.second().toString();
            System.out.println(id+","+column);
        }
  
        DatabaseHelper.updateColumnData(connection, tableName, columnName, "1", "7");
  
        list = DatabaseHelper.getColumnData(connection, tableName, idColumnName, columnName);
        for(IPair<Object,Object> po : list){
            String id = po.first().toString();
            String column = po.second().toString();
            System.out.println(id+","+column);
        }
    }
}
