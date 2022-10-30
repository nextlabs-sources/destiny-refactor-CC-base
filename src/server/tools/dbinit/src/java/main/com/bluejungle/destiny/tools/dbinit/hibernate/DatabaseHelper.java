package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.dialect.Oracle9Dialect;
import net.sf.hibernate.dialect.PostgreSQLDialect;
import net.sf.hibernate.dialect.SQLServerDialect;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class DatabaseHelper{
    private static final Log LOG = LogFactory.getLog(DatabaseHelper.class);

    public static final byte DB_IDENTIFIERS_STORE_UPPER_CASE = 1;
    public static final byte DB_IDENTIFIERS_STORE_LOWER_CASE = 2;
    public static final byte DB_IDENTIFIERS_STORE_MIXED_CASE = 3;

    private static byte dbStoresIdentifiersType = DB_IDENTIFIERS_STORE_MIXED_CASE;

    public static void setDbStoresIdentifiersType(byte dbStoresIdentifiersType) {
        DatabaseHelper.dbStoresIdentifiersType = dbStoresIdentifiersType;
    }

    public static String matchToDbStoreCase(String input){
        if(dbStoresIdentifiersType == DB_IDENTIFIERS_STORE_LOWER_CASE){
            return input.toLowerCase();
        }else if(dbStoresIdentifiersType == DB_IDENTIFIERS_STORE_UPPER_CASE){
            return input.toUpperCase();
        }else{
            return input;
        }
    }

    public static void print(ResultSet rs){
        try {
            int col = rs.getMetaData().getColumnCount();
            for(int i = 1; i<= col; i++){
                System.out.println(rs.getString(i));
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final String BATCH_PROCESS_SQL_FORMAT = " %3d\t%s";
    private static final String BATCH_PROCESS_SQL_ERR_FORMAT = " %3d\t%s - %s";
    
    /**
     * connection is not committed by this method.
     * @param connection
     * @param sqlstatements
     * @return the error results of the statements. If all statement is success, it will return null. Else return exceptions
     * @throws SQLException
     */
    public static List<SQLException> processSqlStatements(Connection connection, List<String> sqlstatements) {
        if(sqlstatements.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        
        List<SQLException> results = new ArrayList<SQLException>(sqlstatements.size());
        StringBuilder infoString = new StringBuilder();
        StringBuilder errorString = new StringBuilder();
        int index = 1;
        
        for (String sqlStatement : sqlstatements) {
        	Statement statement = null;
        	try {
            	statement = connection.createStatement();
                statement.execute(sqlStatement);
                infoString
                  .append(ConsoleDisplayHelper.NEWLINE)
                  .append(String.format(BATCH_PROCESS_SQL_FORMAT, index++, sqlStatement));
                results.add(null);
            } catch (SQLException e) {
                errorString
                  .append(ConsoleDisplayHelper.NEWLINE)
                  .append(String.format(BATCH_PROCESS_SQL_ERR_FORMAT, index++, e.toString(), sqlStatement));
                results.add(e);
            }finally{
                close(statement);
            }
        }
        LOG.info(infoString);
        
        if(errorString.length() > 0){
            LOG.error(errorString);
        }
        return results;
    }
    
    public static long processSqlPreparedStatementsAndReturnInsertedId(Connection connection, String sqlstatement, String keyColumn, Object[] parameters) throws SQLException {
    	PreparedStatement statement = null;
    	try {
        	statement = connection.prepareStatement(sqlstatement, new String[] {keyColumn});
        	for(int index = 0; index < parameters.length; index++) {
        		Object param = parameters[index];
        		if(null == param) continue;
        		Class<? extends Object> paramClass = param.getClass();
        		if(paramClass.equals(Integer.TYPE) || paramClass.equals(Integer.class)) {
        			statement.setInt(index + 1, (Integer)param);
        		}
        		else if(paramClass.equals(Long.TYPE) || paramClass.equals(Long.class)) {
        			statement.setLong(index + 1, (Long)param);
        		}
        		else if(paramClass.equals(Float.TYPE) || paramClass.equals(Float.class)) {
        			statement.setFloat(index + 1, (Float)param);
        		}
        		else if(paramClass.equals(Double.TYPE) || paramClass.equals(Double.class)) {
        			statement.setDouble(index + 1, (Double)param);
        		}
        		else if(param instanceof String) {
        			statement.setString(index + 1, (String)param);
        		}
        		else if(param instanceof Date) {
        			statement.setDate(index + 1, new java.sql.Date(((Date)param).getTime()));
        		}
        		else {
        			statement.setObject(index + 1, param);
        		}
        	}
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            else {
                throw new SQLException("No key generated");
            }
        }finally{
            close(statement);
        }
    }

    /**
     *
     * @param connection
     * @param sqlstatement
     * @return the error results of the statements, if all statements is success, it returns a list of null
     * @throws SQLException
     */
    public static void processSqlStatement(Connection connection, String sqlstatement)
			throws SQLException {
		SQLException exception = processSqlStatements(connection,
				Collections.singletonList(sqlstatement)).get(0);
		if (exception != null) {
			throw exception;
		}
	}

    /**
     * get column data
     * @param connection
     * @param tableName        database table name, case sensitive
     * @param idColumnName    the column must be a primary key or unique
     * @param columnName
     * @return a IPair<Object,Object> the first field is the id column, the second field is the column in parameter
     * @throws SQLException
     */
    public static List<IPair<Object, Object>> getColumnData(Connection connection,
			String tableName, String idColumnName, String columnName) throws SQLException {
		return getColumnData(connection, tableName, idColumnName, columnName, null);
	}

    public static List<IPair<Object,Object>> getColumnData(Connection connection, String tableName,
            String idColumnName, String columnName, String condition) throws SQLException {
        List<IPair<Object,Object>> returnResults = new ArrayList<IPair<Object,Object>>();
        Statement statement = null;
		try {
			statement = connection.createStatement();

			String query = "SELECT " + idColumnName + "," + columnName + " FROM " + tableName;
			if (condition != null && condition.length() > 0) {
			    query += " WHERE " + condition;
			}
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
			    returnResults.add(new Pair<Object,Object>(rs.getObject(1), rs.getObject(2)));
			}
		} finally {
			close(statement);
		}
        return returnResults;
    }

    /**
     * copy a column data to the other column
     * @param connection
     * @param tableName        database table name, case sensitive
     * @param fromColumn    the column name of the column copies from
     * @param toColumn        the column name of the destination
     * @throws SQLException
     */
    public static void copyColumnData(Connection connection, String tableName, String fromColumn,
			String toColumn) throws SQLException {
		processSqlStatement(connection, "UPDATE " + tableName + " SET " + toColumn + "="
				+ fromColumn);
	}

    private static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				LOG.warn(e);
			}
		}
	}

    /**
     * update a column data, replace a value with the other value.
     * @param connection
     * @param tableName        database table name, case sensitive.
     * @param column        datbase column name, case sensitive
     * @param orginalValue
     * @param replaceValue
     * @throws SQLException
     */
    public static void updateColumnData(Connection connection, String tableName, String column,
			String orginalValue, String replaceValue) throws SQLException {
		final String string = "UPDATE " + tableName + " SET " + column + " = " + replaceValue
				+ " WHERE " + column + " = " + orginalValue;
		processSqlStatement(connection, string);
	}

    public static String getColumnTempName(String tableName, String columnName, ConfigurationMod cm){
        TableM table = cm.getHibernateM().getTable(tableName);
        if(table == null){
            return null;
        }
        ColumnM column = table.getColumn(columnName);

        if(column == null){
            return null;
        }
        return column.getTempName();
    }
    

    /**
     * return the number of record that match the condition
     * @param connection
     * @param condition
     * @param columnName
     * @param length
     * @return
     * @throws SQLException 
     */
    public static int checkLength(Connection connection, DialectExtended dialect, String tableName,
			String columnName, String condition) throws SQLException {
		final String queryFormat = "SELECT %s(*) FROM %s WHERE %s(%s) %s";

		Statement statement = null;
		int result = 0;
		try {
			statement = connection.createStatement();

			String query = String.format(queryFormat, dialect.getCountString(), tableName, dialect
					.getLengthString(), columnName, condition);
			ResultSet rs = statement.executeQuery(query);
			rs.next();
			result = rs.getInt(1);
		} finally {
			close(statement);
		}
		return result;
	}
    
    public static void renameTable(Connection connection, String oldTableName, String newTableName)
			throws SQLException {
		final String renameTableSyntax = "alter table %s rename to %s";
		
		processSqlStatement(connection, String.format(renameTableSyntax, oldTableName, newTableName));
	}
    
    
    public static void dropTable(Connection connection, Dialect dialect, String tableName) throws SQLException{
    	TableM tableM = new TableM(null, tableName);
		
		processSqlStatement(connection, tableM.sqlDropString(dialect));
    }
    
    public static boolean isIndexExists(DatabaseMetaData meta, DialectExtended dialect,
            String tableName, String indexName) throws SQLException {
        final String tableSchema = dialect.getTableSchema(meta);
        final String catalog = null;
        
        ResultSet rs = meta.getIndexInfo(catalog, tableSchema, tableName, false, true);
        Set<String> indexes = new HashSet<String>();
        while (rs.next()) {
            String name = rs.getString(ResultSetKey.INDEX_NAME);
            if (name != null) {
                indexes.add(name);
            }
        }
        rs.close();
        
        return indexes != null 
            ? indexes.contains(indexName)
            : false;
    }
    
    /**
     * If the index name is already exist, the new index won't be created
     * 
     * @param connection
     * @param indexName
     * @param tableName
     * @param columns the order is very important
     * @return true if the index is created.
     * @throws SQLException
     */
    public static boolean createIndex(
            Connection connection, 
            DialectExtended dialectX,
            String indexName, 
            String tableName, 
            String... columns
    ) throws SQLException {
        
        if (columns == null) {
            throw new NullPointerException("columns");
        }
        
        if(dialectX.getUnwrappedDialect() instanceof Oracle9Dialect && indexName.length() > 30){
            throw new IllegalArgumentException("You hit the Oracle limit, the index name can't be more than 30 characters.");
        }else if(dialectX.getUnwrappedDialect() instanceof PostgreSQLDialect&& indexName.length() > 128){
            throw new IllegalArgumentException("You hit the PostgreSQL limit, the index name can't be more than 128 characters.");
        }else if(dialectX.getUnwrappedDialect() instanceof SQLServerDialect&& indexName.length() > 128){
            throw new IllegalArgumentException("You hit the Microsoft SQL Server limit, the index name can't be more than 128 characters.");
        }
        
        if (isIndexExists(connection.getMetaData(), dialectX, tableName, indexName)) {
            return false;
        }
            
        String sql = "CREATE INDEX " + indexName + " ON " + tableName 
                + " (" + ArrayUtils.asString(columns, ",") + ")";

        DatabaseHelper.processSqlStatement(connection, sql);
        return true;
    }
    
    public static boolean isViewExist(DatabaseMetaData dbmd, DialectExtended dialectX, String viewName)
            throws SQLException {
        String schema = dialectX.getTableSchema(dbmd);
        
        ResultSet rs = dbmd.getTables(
                null , // catalog, 
                schema, //schemaPattern, 
                viewName, 
                new String[]{"VIEW"}
                );
        try{
            return rs.next();
        } finally{
            rs.close();
        }
    }
}
