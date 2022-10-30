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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.dialect.OracleDialect;
import net.sf.hibernate.dialect.PostgreSQLDialect;
import net.sf.hibernate.dialect.SQLServerDialect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.version.IVersion;

/**
 * @author Hor-kan Chan
 * @date Mar 14, 2007
 */
@SuppressWarnings("deprecation")
public class PfJavaUpdateTask extends BaseJavaUpdateTask {
	private static final Log LOG = LogFactory.getLog(PfJavaUpdateTask.class);

	private static final Map<String, String> REPLACE_TO_CO_MAP = new TreeMap<String, String>();

	static {
		REPLACE_TO_CO_MAP.put("HO", EntityType.HOST.getName());
		REPLACE_TO_CO_MAP.put("US", EntityType.USER.getName());
		REPLACE_TO_CO_MAP.put("AP", EntityType.APPLICATION.getName());
		REPLACE_TO_CO_MAP.put("AC", EntityType.ACTION.getName());
		REPLACE_TO_CO_MAP.put("RS", EntityType.RESOURCE.getName());
	}

	public void execute(Connection connection, ConfigurationMod cm,
			IVersion fromVersion, IVersion toVersion)
			throws JavaUpdateException {
		try {
			if (fromVersion.compareTo(VERSION_1_6) <= 0
					&& toVersion.compareTo(fromVersion) > 0) {
				upgradeFrom1_6(connection);
			}

			if (fromVersion.compareTo(VERSION_2_0) <= 0
					&& toVersion.compareTo(fromVersion) > 0) {
				upgradeFrom2_0(connection, cm);
			}

			if (fromVersion.compareTo(VERSION_2_5) <= 0
					&& toVersion.compareTo(fromVersion) > 0) {
				upgradeFrom2_5(connection, cm);
			}
			upgradeTo4_5(connection);
			if (fromVersion.compareTo(VERSION_4_6) < 0) {
				bugfix10705(connection, cm);
			}

			if (fromVersion.compareTo(VERSION_7_5_1) <= 0
					&& toVersion.compareTo(fromVersion) > 0) {
				upgradeTo7_6(connection, cm);
			}
		} catch (SQLException e) {
			throw new JavaUpdateException(e, JavaUpdateException.Type.SQL);
		} catch (PQLException e) {
			throw new JavaUpdateException(e);
		} catch (HibernateException e) {
			throw new JavaUpdateException(e, JavaUpdateException.Type.HIBERNATE);
		}
	}

	/**
	 * - check deployment_entities.name length under 800 - check
	 * development_entities.name length under 800
	 * 
	 * @param connection
	 * @param cm
	 * @throws SQLException
	 * @throws HibernateException
	 * @throws JavaUpdateException
	 */
	private void upgradeFrom2_5(Connection connection, ConfigurationMod cm)
			throws SQLException, HibernateException, JavaUpdateException {
		final int maxLength = 800;
		final DialectExtended dialect = DialectExtended.getDialectExtended(cm
				.getDialect());

		String tableName = DatabaseHelper
				.matchToDbStoreCase("deployment_entities");
		String columnName = DatabaseHelper.matchToDbStoreCase("name");
		int result = DatabaseHelper.checkLength(connection, dialect, tableName,
				columnName, " >" + maxLength);
		if (result > 0) {
			throw JavaUpdateException.reachMaxLength(tableName, columnName,
					maxLength, result);
		}

		tableName = DatabaseHelper.matchToDbStoreCase("development_entities");
		columnName = DatabaseHelper.matchToDbStoreCase("name");
		result = DatabaseHelper.checkLength(connection, dialect, tableName,
				columnName, " >" + maxLength);
		if (result > 0) {
			throw JavaUpdateException.reachMaxLength(tableName, columnName,
					maxLength, result);
		}
		LOG.info("done");
	}

	/**
	 * - update development_entities.pql - replace the last TRUE with (TRUE AND
	 * TRUE) - drop table pf_target_resolutions
	 * 
	 * @param connection
	 * @param cm
	 * @throws SQLException
	 * @throws HibernateException
	 */
	private void upgradeFrom2_0(Connection connection, ConfigurationMod cm)
			throws SQLException, HibernateException {
		String tableName = DatabaseHelper
				.matchToDbStoreCase("development_entities");
		String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
		String columnName = DatabaseHelper.matchToDbStoreCase("pql");

		List<IPair<Object, Object>> records = DatabaseHelper.getColumnData(
				connection, tableName, idColumnName, columnName,
				"type = 'CO' AND UPPER(name) LIKE 'APPLICATION%'");

		PreparedStatement updateStatement = connection
				.prepareStatement("UPDATE " + tableName + " SET " + columnName
						+ " = ?" + " WHERE " + idColumnName + " = ?");

		final Pattern oldSyntaxPattern = Pattern.compile(
				".+(TRUE)\\s*\\)\\s*$", Pattern.DOTALL);
		for (IPair<Object, Object> record : records) {
			if (record.second() != null) {
				final String orginalPql = record.second().toString();
				Matcher matcher = oldSyntaxPattern.matcher(orginalPql);
				if (matcher.find()) {
					LOG.debug("OLD\n" + record.second());
					StringBuffer newPql = new StringBuffer(orginalPql);
					newPql = newPql.replace(matcher.start(1), matcher.end(1),
							"(TRUE AND TRUE)");
					LOG.debug(newPql + "\n\n\n\n");
					updateStatement.setString(1, newPql.toString());
					updateStatement.setInt(2,
							Integer.parseInt(record.first().toString()));
					int numUpdates = updateStatement.executeUpdate();
					if (numUpdates != 1) {
						LOG.error(numUpdates + " row(s) updated. "
								+ updateStatement.toString());
					}
				}
			}
		}

		tableName = DatabaseHelper.matchToDbStoreCase("pf_target_resolutions");
		String statement = cm.getTable(tableName)
				.sqlDropString(cm.getDialect());
		DatabaseHelper.processSqlStatement(connection, statement);
		LOG.info("done");
	}

	/**
	 * - update pql - update Development Entities Type - from short name (2
	 * char) to long name (string)
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws PQLException
	 */
	private void upgradeFrom1_6(Connection connection) throws SQLException,
			PQLException {
		cleanUpTable(connection);

		String tableName = DatabaseHelper
				.matchToDbStoreCase("development_entities");
		List<TripleObject> triples = getDevelopmentEntitiesPqlList(connection);
		updatePql(connection, tableName, triples);

		tableName = DatabaseHelper.matchToDbStoreCase("deployment_entities");
		triples = getDepolymentEntitiesPqlList(connection);
		updatePql(connection, tableName, triples);

		updateDevelopmentEntitiesType(connection);
		LOG.info("done");
	}

	private void upgradeTo4_5(Connection connection) throws SQLException {
		cleanUpTable(connection);
	}

	private void updatePql(Connection connection, String tableName,
			List<TripleObject> triples) throws PQLException, SQLException {
		LOG.info("updating pql " + triples.size() + " rows in table "
				+ tableName);
		for (TripleObject triple : triples) {
			if (REPLACE_TO_CO_MAP.containsKey(triple.type)) {
				DomainObjectBuilder domainObjectBuilder = new DomainObjectBuilder(
						triple.pql);
				SpecBase specBase = (SpecBase) domainObjectBuilder
						.processSpec();
				String name = specBase.getName();
				// magic
				specBase.setName(REPLACE_TO_CO_MAP.get(triple.type) + "/"
						+ name);
				DomainObjectFormatter domainObjectFormatter = new DomainObjectFormatter();
				domainObjectFormatter.formatDef(specBase);
				String newPql = domainObjectFormatter.getPQL();

				String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
				String columnName = DatabaseHelper.matchToDbStoreCase("pql");

				PreparedStatement updateStatement = connection
						.prepareStatement("UPDATE " + tableName + " SET "
								+ columnName + " = ? " + " WHERE "
								+ idColumnName + " = ?");
				updateStatement.setString(1, newPql);
				updateStatement.setLong(2, triple.id);
				int numUpdates = updateStatement.executeUpdate();
				if (numUpdates != 1) {
					LOG.warn(numUpdates + " row(s) updated. "
							+ updateStatement.toString());
				}
			}
		}
	}

	private List<TripleObject> getDevelopmentEntitiesPqlList(
			Connection connection) throws SQLException {
		String tableName = DatabaseHelper
				.matchToDbStoreCase("development_entities");
		String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
		String columnName = DatabaseHelper.matchToDbStoreCase("pql");
		String column2Name = DatabaseHelper.matchToDbStoreCase("type");
		List<TripleObject> returnResults = new ArrayList<TripleObject>();
		Statement statement = connection.createStatement();

		ResultSet rs;
		rs = statement.executeQuery("SELECT " + idColumnName + "," + columnName
				+ "," + column2Name + " FROM " + tableName);

		while (rs.next()) {
			returnResults.add(new TripleObject(rs.getObject(1),
					rs.getObject(2), rs.getObject(3)));
		}

		// resultset is closed the statement,
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				LOG.warn(e);
			}
		}
		return returnResults;
	}

	private List<TripleObject> getDepolymentEntitiesPqlList(
			Connection connection) throws SQLException {
		String tableName = DatabaseHelper
				.matchToDbStoreCase("deployment_entities");

		String refTableName = DatabaseHelper
				.matchToDbStoreCase("development_entities");
		String idColumnName = DatabaseHelper.matchToDbStoreCase("id");
		String columnName = DatabaseHelper.matchToDbStoreCase("pql");
		String typeColumnName = DatabaseHelper.matchToDbStoreCase("type");
		List<TripleObject> returnResults = new ArrayList<TripleObject>();
		Statement statement = connection.createStatement();

		ResultSet rs;
		rs = statement.executeQuery("SELECT " + tableName + "." + idColumnName
				+ "," + tableName + "." + columnName + "," + refTableName + "."
				+ typeColumnName + " FROM " + tableName + "," + refTableName
				+ " WHERE "
				+ DatabaseHelper.matchToDbStoreCase("development_id") + " = "
				+ refTableName + "." + idColumnName);

		while (rs.next()) {
			if (rs.getObject(2) != null) {
				returnResults.add(new TripleObject(rs.getObject(1), rs
						.getObject(2), rs.getObject(3)));
			}
		}

		// resultset is closed the statement,
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				LOG.warn(e);
			}
		}
		return returnResults;
	}

	private class TripleObject {
		public Long id;
		public String type;
		public String pql;

		public TripleObject(Object id, Object pql, Object type) {
			super();
			this.id = Long.parseLong(id.toString());
			this.type = type.toString();
			this.pql = pql.toString();
		}
	}

	private void updateDevelopmentEntitiesType(Connection connection)
			throws SQLException {
		String tableName = DatabaseHelper
				.matchToDbStoreCase("development_entities");
		String columnName = DatabaseHelper.matchToDbStoreCase("type");
		DatabaseHelper.updateColumnData(connection, tableName, columnName,
				"'PF'", "'FL'");

		for (String replaceToCO : REPLACE_TO_CO_MAP.keySet()) {
			DatabaseHelper.updateColumnData(connection, tableName, columnName,
					"'" + replaceToCO + "'", "'CO'");
		}
	}

	private void cleanUpTable(Connection connection) throws SQLException {
		String tableName = "pf_target_resolutions";
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		DatabaseHelper.processSqlStatement(connection, "DELETE FROM "
				+ tableName);
	}

	/**
	 * fail gracefully
	 * 
	 * @param connection
	 * @param cm
	 */
	private void bugfix10705(Connection connection, ConfigurationMod cm) {
		try {
			List<String> sqls = new ArrayList<String>();
			Dialect d = cm.getDialect();
			if (d instanceof PostgreSQLDialect) {
				sqls.add("update development_entities set name = UPPER(SUBSTRING(name, 1, POSITION('/' IN name))) "
						+ "|| SUBSTRING(name, POSITION('/' IN name)+ 1, CHARACTER_LENGTH(name) - POSITION('/' IN name) ) "
						+ "where type = 'CO' and name like '%/%';");
				sqls.add("update deployment_entities set name = UPPER(SUBSTRING(name, 1, POSITION('/' IN name))) "
						+ "|| SUBSTRING(name, POSITION('/' IN name)+ 1, CHARACTER_LENGTH(name) - POSITION('/' IN name) ) "
						+ "where development_id in (select id from development_entities where type = 'CO') and name like '%/%';");
			} else if (d instanceof OracleDialect) {
				sqls.add("update development_entities set name = UPPER(SUBSTR(name, 1, INSTR(name, '/'))) "
						+ "|| SUBSTR(name, INSTR(name, '/')+ 1, LENGTH(name) - INSTR(name, '/') ) "
						+ "where type = 'CO' and name like '%/%'");
				sqls.add("update deployment_entities set name = UPPER(SUBSTR(name, 1, INSTR(name, '/'))) "
						+ "|| SUBSTR(name, INSTR(name, '/')+ 1, LENGTH(name) - INSTR(name, '/') ) "
						+ "where development_id in (select id from development_entities where type = 'CO') and name like '%/%'");
			} else if (d instanceof SQLServerDialect) {
				sqls.add("update development_entities set name = UPPER(SUBSTRING(name, 1, PATINDEX('%/%', name))) "
						+ "+ SUBSTRING(name, PATINDEX('%/%', name)+ 1, LEN(name) - PATINDEX('%/%', name) ) "
						+ "where type = 'CO' and name like '%/%';");
				sqls.add("update deployment_entities set name = UPPER(SUBSTRING(name, 1, PATINDEX('%/%', name))) "
						+ "+ SUBSTRING(name, PATINDEX('%/%', name)+ 1, LEN(name) - PATINDEX('%/%', name) ) "
						+ "where development_id in (select id from development_entities where type = 'CO') and name like '%/%';");
			}
			DatabaseHelper.processSqlStatements(connection, sqls);
			connection.commit();
		} catch (Exception e) {
			LOG.error("", e);
		}
	}

	private static final String APPQL_ROLES = "\nACCESS_POLICY\nACCESS_CONTROL\nALLOWED_ENTITIES\n";

	private static final String REPORT_ADMIN_ROLE_PQL = "ID null STATUS APPROVED CREATOR \"0\"\n"
			+ "ACCESS_POLICY\n"
			+ "ACCESS_CONTROL\n"
			+ "ALLOWED_ENTITIES\n"
			+ "HIDDEN COMPONENT \"Report Administrator\" = false\n";

//	private static final String REPORT_ANALYST_ROLE_PQL = "ID null STATUS APPROVED CREATOR \"0\"\n"
//			+ "ACCESS_POLICY\n"
//			+ "ACCESS_CONTROL\n"
//			+ "ALLOWED_ENTITIES\n"
//			+ "HIDDEN COMPONENT \"Report Analyst\" = false\n";

	private static final String REPORTER_ADMIN_COMPONENT_APPQL = "\nACCESS_POLICY\n"
			+ "ACCESS_CONTROL\n"
			+ "\tPBAC\n"
			+ "\t\tFOR TRUE\n"
			+ "\t\tON READ\n"
			+ "\t\tBY (GROUP=\"ADMIN\" OR GROUP=\"Report Administrator\" OR GROUP=\"System Administrator\")\n"
			+ "\t\tDO allow\n"
			+ "\tPBAC\n"
			+ "\t\tFOR TRUE\n"
			+ "\t\tON WRITE\n"
			+ "\t\tBY GROUP=\"ADMIN\"\n"
			+ "\t\tDO allow\n"
			+ "ALLOWED_ENTITIES\n";

	private static final String REPORTER_ADMIN_COMPONENT_PQL = "ID null STATUS APPROVED CREATOR \"0\""
			+ REPORTER_ADMIN_COMPONENT_APPQL
			+ "HIDDEN COMPONENT \"Inquiry Center Admin\" = application.name = \"Inquiry Center Admin\"";

	private static final String REPORTER_ANALYST_COMPONENT_APPQL = "\nACCESS_POLICY\n"
			+ "ACCESS_CONTROL\n"
			+ "\tPBAC\n"
			+ "\t\tFOR TRUE\n"
			+ "\t\tON READ\n"
			+ "\t\tBY (GROUP=\"ADMIN\" OR GROUP=\"Business Analyst\" OR GROUP=\"System Administrator\" OR GROUP=\"Policy Administrator\" OR GROUP=\"Policy Analyst\")\n"
			+ "\t\tDO allow\n"
			+ "\tPBAC\n"
			+ "\t\tFOR TRUE\n"
			+ "\t\tON WRITE\n"
			+ "\t\tBY GROUP=\"ADMIN\"\n"
			+ "\t\tDO allow\n"
			+ "ALLOWED_ENTITIES\n";

	private static final String REPORTER_ANALYST_COMPONENT_PQL = "ID null STATUS APPROVED CREATOR \"0\""
			+ REPORTER_ANALYST_COMPONENT_APPQL
			+ "HIDDEN COMPONENT \"Inquiry Center User\" = application.name = \"Inquiry Center User\"";

	private void upgradeTo7_6(Connection connection, ConfigurationMod cm) {
		LOG.info("Upgrade to 7.6 version PQL changes");
		try {
			Dialect d = cm.getDialect();
			LOG.info(":::::: Dialect :" + d);
			
			StringBuilder adminRoleInsertSQL = new StringBuilder(
					"INSERT INTO development_entities");
			adminRoleInsertSQL
					.append("( id, version, owner, appql, name, pql, status, type, last_updated, created, last_modified, hidden, has_dependencies, is_sub_policy) VALUES (")
					.append(" 100000, 0, 0, \'")
					.append(APPQL_ROLES)
					.append("\', \'Report Administrator\', \'")
					.append(REPORT_ADMIN_ROLE_PQL)
					.append("\', \'AP\', \'CO\', 1426736227250, 1426736227250, 1426736227250, 'Y', 'F', 'F')");
			
//			StringBuilder analystRoleInsertSQL = new StringBuilder(
//					"INSERT INTO development_entities");
//			analystRoleInsertSQL
//					.append("( id, version, owner, appql, name, pql, status, type, last_updated, created, last_modified, hidden, has_dependencies, is_sub_policy) VALUES (")
//					.append(" 100001, 0, 0, \'")
//					.append(APPQL_ROLES)
//					.append("\', \'Report Analyst\', \'")
//					.append(REPORT_ANALYST_ROLE_PQL)
//					.append("\', \'AP\', \'CO\', 1426736227250, 1426736227250, 1426736227250, 'Y', 'F', 'F')");
			
			StringBuilder reporterAdminComponentInsertSQL = new StringBuilder(
					"INSERT INTO development_entities");
			reporterAdminComponentInsertSQL
					.append("( id, version, owner, appql, name, pql, status, type, last_updated, created, last_modified, hidden, has_dependencies, is_sub_policy) VALUES (")
					.append(" 100002, 0, 0, \'")
					.append(REPORTER_ADMIN_COMPONENT_APPQL)
					.append("\', \'Inquiry Center Admin\', \'")
					.append(REPORTER_ADMIN_COMPONENT_PQL)
					.append("\', \'AP\', \'CO\', 1426736227250, 1426736227250, 1426736227250, 'Y', 'F', 'F')");
			
			StringBuilder reporterAnalystComponentInsertSQL = new StringBuilder(
					"INSERT INTO development_entities");
			reporterAnalystComponentInsertSQL
					.append("( id, version, owner, appql, name, pql, status, type, last_updated, created, last_modified, hidden, has_dependencies, is_sub_policy) VALUES (")
					.append(" 100003, 0, 0, \'")
					.append(REPORTER_ANALYST_COMPONENT_APPQL)
					.append("\', \'Inquiry Center User\', \'")
					.append(REPORTER_ANALYST_COMPONENT_PQL)
					.append("\', \'AP\', \'CO\', 1426736227250, 1426736227250, 1426736227250, 'Y', 'F', 'F')");

			
			StringBuilder deleteInquiryCenterComponentSQL = new StringBuilder("DELETE FROM development_entities WHERE name = 'Inquiry Center'");
			
			if (d.getClass().getName().contains("SqlServer")) {
				DatabaseHelper.processSqlStatement(connection, "SET IDENTITY_INSERT development_entities ON;");
				DatabaseHelper.processSqlStatement(connection, adminRoleInsertSQL.toString() + ";");
//				DatabaseHelper.processSqlStatement(connection, analystRoleInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, reporterAdminComponentInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, reporterAnalystComponentInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, deleteInquiryCenterComponentSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, "SET IDENTITY_INSERT development_entities OFF;");
				
				LOG.info(":::::: SQL Data migration done");
			} else if (d.getClass().getName().contains("Oracle")) {
				DatabaseHelper.processSqlStatement(connection, adminRoleInsertSQL.toString());
//				DatabaseHelper.processSqlStatement(connection, analystRoleInsertSQL.toString());
				DatabaseHelper.processSqlStatement(connection, reporterAdminComponentInsertSQL.toString());
				DatabaseHelper.processSqlStatement(connection, reporterAnalystComponentInsertSQL.toString());
				DatabaseHelper.processSqlStatement(connection, deleteInquiryCenterComponentSQL.toString());
				LOG.info(":::::: Oracle Data migration done");
			} else {
				DatabaseHelper.processSqlStatement(connection, adminRoleInsertSQL.toString() + ";");
//				DatabaseHelper.processSqlStatement(connection, analystRoleInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, reporterAdminComponentInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, reporterAnalystComponentInsertSQL.toString() + ";");
				DatabaseHelper.processSqlStatement(connection, deleteInquiryCenterComponentSQL.toString() + ";");
				LOG.info(":::::: PostGre Data migration done");
			}
			
			connection.commit();
			
		} catch (Exception e) {
			LOG.error("Error occurred in upgrading PQL changes to 7.6, ", e);
		}
	}
}
