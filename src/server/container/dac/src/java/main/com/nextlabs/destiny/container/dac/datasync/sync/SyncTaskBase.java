/*
 * Created on Jun 11, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.datastore.hibernate.BatchWriter;
import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTaskUpdate;
import com.nextlabs.destiny.container.dac.datasync.Constants;

import com.nextlabs.destiny.container.dac.datasync.log.AttributeColumnMappingInfoWrapper;
import com.nextlabs.destiny.container.dac.datasync.log.ReportLog;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLog;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDAOImpl;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDO;

/**
 * @author hchan
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main
 *          /com/
 *          nextlabs/destiny/container/dac/datasync/sync/SyncTaskBase.java#5 $
 */

public abstract class SyncTaskBase<T extends ReportLog> {
	private static final int BATCH_SIZE = 1000;
	private static final int CACHE_SIZE = 10000;
	private static final Log LOG = LogFactory.getLog(SyncTaskBase.class);

	/* cached user */
	private static final int CACHED_USER_ORIGINAL_ID_COLUMN;
	private static final int CACHED_USER_DISPLAY_NAME_COLUMN;
	private static final int CACHED_USER_SID_COLUMN;

	private static final String SELECT_CACHED_USER_QUERY_TEMPLATE;

	static {
		int i = 1;
		CACHED_USER_ORIGINAL_ID_COLUMN = i++;
		CACHED_USER_DISPLAY_NAME_COLUMN = i++;
		CACHED_USER_SID_COLUMN = i++;

		SELECT_CACHED_USER_QUERY_TEMPLATE = "select original_id,display_name,sid"
				+ " from "
				+ SharedLib.CACHED_USER
				+ " where active_to = "
				+ UnmodifiableDate.END_OF_TIME.getTime()
				+ " and original_id in";

	}
	/* end cached user */

	protected static final String COUNT_QUERY_TEMPLATE = "select count(*) from %s where sync_done is null";
	
	public static final int DEFAULT_EXT_ATTR_COUNT = 99;
	public static final String ATTR_PREFIX = "attr";
	
	protected static final String SEPARATOR = ", ";
	protected int numberOfAdditionalColumns = DEFAULT_EXT_ATTR_COUNT; //let default be 99

	protected IDataSyncTaskUpdate update;
	protected CacheManager cacheManager;
	protected Cache policyCache;
	protected Cache userCache;
	protected Map<Number, T> transform;
	protected Connection connection;
	protected AttributeColumnMappingInfoWrapper attrColumnMappingConfig;

	private Dialect dialect;
	private SyncTask syncTask;
	private int totalParsedCount;
	private PreparedStatement selectTotalCountStatement;

	private BatchWriter<T> writer;
	private final String selectTotalCountQuery;
	
	protected Set<String> userAttributesBlackList = new HashSet<String>();
	protected Set<String> resourceAttributesBlackList = new HashSet<String>();
	protected Set<String> policyAttributesBlackList = new HashSet<String>();
	protected Dictionary dict = null;

	public SyncTaskBase(Class<T> clazz, String selectTotalCountQuery) {
		super();
		this.selectTotalCountQuery = selectTotalCountQuery;
		/*
		 * initialize dictionary component
		 */
		dict = ComponentManagerFactory.getComponentManager()
				.getComponent(Dictionary.COMP_INFO);
	}
	
	protected abstract BatchWriter<T> getWriter();
	
	protected Dialect getDialect() {
		return dialect;
	}

	protected int getBatchSize() {
		return BATCH_SIZE;
	}

	protected int getRemainingTime() {
		return syncTask.getRemainingTime();
	}

	
	/**
	 * This method fetches the attribute-column mapping records from database
	 * and constructs lookup structures
	 * @param attrColumnMapConfig
	 */
	protected void populateAttrColumnMap() {
		List<AttributeColumnMappingDO> mappingList = null;

		try {
			mappingList = AttributeColumnMappingDAOImpl.list();
		} catch (HibernateException e) {
			LOG.error(e);
		}

		if (mappingList == null || mappingList.isEmpty()) {
			
			LOG.error("-----------------------------------------------------mapping list is empty------------------------------------------------------------");
			
			return;
		}

		Map<String, Map<String, AttributeColumnMappingDO>> map = attrColumnMappingConfig.getAttrColumnNameMapping();
		
		boolean[] columnsInUse = attrColumnMappingConfig.getColumnsInUse();
		
		for (AttributeColumnMappingDO mapping : mappingList) {
			/*
			 * lets not use static field mappings to resolve dynamic field mapping
			 */
			if (!mapping.isDynamic())
			{
				continue;
			}
			
			String type = mapping.getAttributeType();
			if (map.get(type) == null)
			{
				map.put(type, new HashMap<String, AttributeColumnMappingDO>());
			}
			
			map.get(type).put(mapping.getAttributeName().toLowerCase(), mapping);

			String attributeName = mapping.getAttributeName();
			String columnName = mapping.getColumnName();
			
			/*
			 * we have same columnName and attributeName for standard fields
			 */
			if (columnName == null || columnName.isEmpty())
			{
				continue;
			}
			
			try
			{
				int index = Integer.parseInt(columnName.replace(attrColumnMappingConfig.getAttributePrefix(), ""));
				columnsInUse[index] = true;
			}
			catch(Exception e)
			{
				LOG.error(e);
			}
		}
	}
	
	/**
	 * call this public method to start the sync process
	 * 
	 * @param session
	 * @param connection
	 * @param config
	 * @param syncTask
	 * @return
	 */
	public boolean run(Session session, Connection connection, IConfiguration config, SyncTask syncTask) {
		
		LOG.info(this.getClass().getSimpleName() + " start");
		// init
		this.syncTask = syncTask;
		
		int timeout;
        if ((timeout = getRemainingTime()) <= 0) {
            LOG.warn(this.getClass().getSimpleName()
                    + " is timeout. The task can't be started successfully with overtime, "
                    + -timeout + " seconds.");
            return false;
        }
		
		
		this.dialect = config.get(IDataSyncTask.DIALECT_CONFIG_PARAMETER);
		this.update = config.get(IDataSyncTask.TASK_UPDATE_PARAMETER);
		this.update.reset();
		this.totalParsedCount = 0;
		
		String attrCount = (String)config.get(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY);
		
		LOG.debug("Number of extended Attributes: " + attrCount);
		
		if (attrCount != null && !attrCount.isEmpty())
		{
			try
			{
				int count = Integer.parseInt(attrCount);
				numberOfAdditionalColumns = count;
			}
			catch (NumberFormatException e)
			{
				LOG.error("invalid integer given as number of extended columns. Will proceed with default of " + DEFAULT_EXT_ATTR_COUNT);
			}
		}
		
		/*
		 * need to generate INSERT query based on the number of Additional columns
		 */
		generateInsertQuery(numberOfAdditionalColumns, ATTR_PREFIX);
		
		attrColumnMappingConfig = new AttributeColumnMappingInfoWrapper(numberOfAdditionalColumns, ATTR_PREFIX);
		
		writer = getWriter();
		
		String userAttrBlackList = (String)config.get(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY);
		
		if (userAttrBlackList != null && !userAttrBlackList.isEmpty())
		{
			LOG.debug("black listed user attributes: " + userAttrBlackList);
			
			String[] fields = userAttrBlackList.split(Constants.BLACKLIST_SEPARATOR);
			userAttributesBlackList.addAll(Arrays.asList(fields));
			userAttributesBlackList.addAll(Arrays.asList(userAttrBlackList.toLowerCase().split(Constants.BLACKLIST_SEPARATOR)));
		}
		
		String resAttrBlackList = (String)config.get(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY);

		if (resAttrBlackList != null && !resAttrBlackList.isEmpty())
		{
			LOG.debug("blacklisted resource attributes: " + resAttrBlackList);
			
			String[] fields = resAttrBlackList.split(Constants.BLACKLIST_SEPARATOR);
			resourceAttributesBlackList.addAll(Arrays.asList(fields));
			resourceAttributesBlackList.addAll(Arrays.asList(resAttrBlackList.toLowerCase().split(Constants.BLACKLIST_SEPARATOR)));
		}

		String policyAttrBlackList = (String)config.get(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY);

		if (policyAttrBlackList != null && !policyAttrBlackList.isEmpty())
		{
			LOG.debug("blacklisted policy attributes: " + policyAttrBlackList);
			
			String[] fields = policyAttrBlackList.split(Constants.BLACKLIST_SEPARATOR);
			policyAttributesBlackList.addAll(Arrays.asList(fields));
			policyAttributesBlackList.addAll(Arrays.asList(policyAttrBlackList.toLowerCase().split(Constants.BLACKLIST_SEPARATOR)));
		}
		
		this.connection = connection;
		
		boolean isSuccessful = false;
		try {
			start();
			int total = getTotalCount();
			update.setTotalSize(total);

			boolean hasMore;
			do {
				// get data from source table
				int parsedCount = parse();
				totalParsedCount += parsedCount;
				if (totalParsedCount > total) {
					// the parsedCount is more than total!
					// update the total
					total = getTotalCount();
					update.setTotalSize(total);
				}
				hasMore = parsedCount > 0;
				if ((timeout = getRemainingTime()) <= 0) {
                    LOG.warn(this.getClass().getSimpleName()
                            + " is timeout. Currently the overtime is " + -timeout + " seconds.");
					hasMore = false;
				}

				// there is something
				if (hasMore) {
					insert(session);
					// next chunk
					next();
				}
			} while (hasMore);
			isSuccessful = true;
		} catch (Exception e) {
			LOG.error("Fail to run " + this.getClass().getSimpleName(), e);
		} finally {
			try {
				done();
			} catch (Exception e) {
				LOG.error("fail to cleanup", e);
			}
			dialect = null;
			LOG.info(this.getClass().getSimpleName() + " end");
		}
		return isSuccessful;
	}

	public abstract void generateInsertQuery(int numberOfExtendedAttrColumns, String attrPrefix);

	public int getParsedCount() {
		return totalParsedCount;
	}

	/**
	 * estimated total number of rows needs to sync
	 * 
	 * @return 0 if unknown
	 */
	protected int getTotalCount() throws SQLException {
		int estimateRows;
		ResultSet r = null;
		try {
			r = selectTotalCountStatement.executeQuery();
			if (r.next()) {
				estimateRows = r.getInt(1);
			} else {
				estimateRows = 0;
			}
		} finally {
			close(r);
		}
		return estimateRows;
	}

	protected void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				LOG.error("fail to close ResultSet", e);
			}
		}
	}

    /**
     * 
     * @param connection
     * @throws Exception
     */
    synchronized protected void start() throws Exception {
        if (cacheManager == null) {
            cacheManager = CacheManager.create();
            
            policyCache = new Cache("policy id to policy name", // String name,
                                    CACHE_SIZE, // int maximumSize,
                                    false, // boolean overflowToDisk,
                                    true, // boolean eternal,
                                    0, // long timeToLiveSeconds,
                                    0 // long timeToIdleSeconds
                                    );
            cacheManager.addCache(policyCache);
            
            userCache = new Cache("user id to user sid", // String name,
                                  CACHE_SIZE, // int maximumSize,
                                  false, // boolean overflowToDisk,
                                  true, // boolean eternal,
                                  0, // long timeToLiveSeconds,
                                  0 // long timeToIdleSeconds
                                  );
            cacheManager.addCache(userCache);
            
            selectTotalCountStatement = connection.prepareStatement(selectTotalCountQuery);
        }
    }

	/**
	 * 
	 * @param connection
	 * @return number of object retrieved
	 * @throws Exception
	 */
	protected abstract int parse() throws Exception;

	protected abstract int getResultCheckIndex();

	/**
	 * 
	 * @param session
	 * @param connection
	 * @throws IllegalStateException
	 *             if I am dead
	 * @throws DataSourceException
	 *             if there is a problem of insertion
	 * @throws Exception
	 */
	protected void insert(Session session) throws IllegalStateException,
			DataSourceException, Exception {
		int[][] results = writer.log(transform.values(), session);

		logResult(results);
	}

	/**
	 * 
	 * @param results
	 * @throws IllegalStateException
	 *             if I am dead
	 */
	protected void logResult(int[][] results) throws IllegalStateException {
		if (LOG.isInfoEnabled()) {
			int successRows = 0;
			if (results != null) {
				int[] updatedPALResults = results[getResultCheckIndex()];
				boolean isGood = true;
				for (int r : updatedPALResults) {
					if (r <= 0 && r != Statement.SUCCESS_NO_INFO) {
						isGood = false;
						break;
					}
					if (isGood) {
						successRows++;
					}
				}
			} else {
				// SHOULD not happen...
			}

			int failedRows = transform.size() - successRows;

			StringBuilder sb = new StringBuilder();
			sb.append("successfully inserted ").append(successRows).append(
					" rows.");
			if (failedRows > 0) {
				sb.append(" And ").append(failedRows).append(" rows failed.");
			}
			LOG.debug(sb.toString());

			update.addSuccess(successRows);
			update.addFail(failedRows);
		} else {
			if (results != null && results[getResultCheckIndex()] != null) {
				update.addSuccess(results[getResultCheckIndex()].length);
			} else {
				update.addFail(1);
			}
		}
	}

	protected void next() throws Exception {
		LOG.trace("next");
		transform.clear();

		// help gc
		transform = null;
	}

	/**
	 * all subclass must release all resource in the done() method.
	 * 
	 * @throws Exception
	 */
	protected void done() throws Exception {
		if (LOG.isTraceEnabled()) {
			StringBuilder sb;
			sb = new StringBuilder("Summary of the task:").append(
					"\npolicy cache statistic").append("\n  hitCount = ")
					.append(policyCache.getHitCount()).append(
							"\n  missCount = ").append(
							policyCache.getMissCountNotFound()).append(
							"\nuser cache statistic").append("\n  hitCount = ")
					.append(userCache.getHitCount()).append("\n  missCount = ")
					.append(userCache.getMissCountNotFound());

			LOG.trace(sb.toString());
		}
		Exception lastException = null;
		if (cacheManager != null) {
			try {
				cacheManager.shutdown();
			} catch (RuntimeException e) {
				lastException = e;
				LOG.error("Fail to shutdown cache manager", e);
			}
		}

		if (selectTotalCountStatement != null) {
			try {
				selectTotalCountStatement.close();
			} catch (SQLException e) {
				lastException = e;
				LOG.error("Fail to close selectTotalCountStatement", e);
			}
		}

        // Can't close it, because the connection is shared among the various sync tasks
		connection = null;

		if (lastException != null) {
			throw lastException;
		}
	}

	/* the following are helper methods */

	// return true is the user is not in cache
	protected boolean setUser(ReportPolicyActivityLog log, long userId)
			throws CacheException {
		log.userId = userId;
		Element userElement = userCache.get(userId);
		if (userElement != null) {
			String[] user = (String[]) userElement.getValue();
			if (user != null) {
				log.userName = user[0];
				log.userSid = user[1];
			}
			// else{ I have seen this user before but I can't find it.}
			return false;
		} else {
			return true;
		}
	}

	protected void solveUnknownUsers(Set<Long> unknownUserIds,
			List<? extends ReportPolicyActivityLog> unknownUserEntries)
			throws SQLException, CacheException {
		// solve all unknown users
		if (!unknownUserIds.isEmpty()) {
			int size = unknownUserIds.size();
			PreparedStatement s = connection
					.prepareStatement(SELECT_CACHED_USER_QUERY_TEMPLATE
							+ SQLHelper.makeInList(size));
			ResultSet usersResult = null;
			try {

				int i = 1;
				for (long userId : unknownUserIds) {
					s.setLong(i++, userId);
				}

				usersResult = s.executeQuery();

				while (usersResult.next()) {
					long originalId = usersResult
							.getLong(CACHED_USER_ORIGINAL_ID_COLUMN);
					String displName = usersResult
							.getString(CACHED_USER_DISPLAY_NAME_COLUMN);
					String userSid = usersResult
							.getString(CACHED_USER_SID_COLUMN);

					userCache.put(new Element(originalId, new String[] {
							lower(displName), userSid }));

					// now I know the id
					unknownUserIds.remove(originalId);
				}
			} finally {
				close(usersResult);
				s.close();
			}

			// the rest in the database
			if (!unknownUserIds.isEmpty()) {
				for (long userId : unknownUserIds) {
					userCache.put(new Element(userId, null));
				}
			}

			for (ReportPolicyActivityLog unknownUserEntry : unknownUserEntries) {
				Element e = userCache.get(unknownUserEntry.userId);
				if (e != null) {
					String[] user = (String[]) e.getValue();
					if (user != null) {
						unknownUserEntry.userName = user[0];
						unknownUserEntry.userSid = user[1];
						size--;
					}
				}
			}

			if (LOG.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("unresolved ").append(unknownUserIds.size()).append(
						" users.");
				if (!unknownUserIds.isEmpty()) {
					sb.append(" They are ").append(
							CollectionUtils.asString(unknownUserIds, ","));
				}
				LOG.debug(sb.toString());
			}
		}
	}
	
	/**
	 * For fetching policy tags corresponding to the policy evaluation log
	 * @throws SQLException
	 */
	protected void solvePolicyTags() throws SQLException {
		final int size = transform.size();
		int piece;
		// chop to smaller peice if the size is more than 10;
		if (size > 10) {
			piece = size / 8;

			// don't go more than 100
			piece = Math.max(100, piece);
		} else {
			piece = size;
		}

		Set<Long> cache = new HashSet<Long>(piece);
		int j = 0;
		for (Number id : transform.keySet()) {
			cache.add(id.longValue());
			j++;
			if (j >= piece) {
				solvePolicyTags(cache);
				cache.clear();
				j = 0;
			}
		}

		if (j >= 0) {
			solvePolicyTags(cache);
			cache.clear();
		}
	}

	protected void solveCustomAttribute() throws SQLException {
		final int size = transform.size();
		int piece;
		// chop to smaller peice if the size is more than 10;
		if (size > 10) {
			piece = size / 8;

			// don't go more than 100
			piece = Math.max(100, piece);
		} else {
			piece = size;
		}

		Set<Long> cache = new HashSet<Long>(piece);
		int j = 0;
		for (Number id : transform.keySet()) {
			cache.add(id.longValue());
			j++;
			if (j >= piece) {
				solveCustomAttribute(cache);
				cache.clear();
				j = 0;
			}
		}

		if (j >= 0) {
			solveCustomAttribute(cache);
			cache.clear();
		}
	}
	
	/**
	 * This method generates IN ('', '', '') SQL query
	 * @param values
	 * @return
	 */
	protected String getINSQLQuery(Collection<String> values){
		if ( values == null || values.isEmpty())
		{
			return " IN ()";
		}
		
		StringBuilder str = new StringBuilder(" IN (");
		
		boolean first = true;
		
		for (String value : values) {
			if (!first){
				str.append(",");
			}
			str.append("'").append(value).append("'");
			first = false;
		}
		return str.append(") ").toString();
	}

	/**
 	 * This method generates NOT IN ('', '', '') SQL query
	 * @param values
	 * @return
	 */
	protected String getNOTINSQLQuery(Collection<String> values){
		return " NOT " + getINSQLQuery(values);
	}
	
	protected void solveCustomAttribute(Set<Long> ids) throws SQLException {
		// do nothing
	}
	
	protected void solvePolicyTags(Set<Long> ids) throws SQLException {
		// do nothing
	}

	private static final Pattern RESOURCE_PATTERN = Pattern
			.compile("(.+?):(?://)?(.*?)/?([^/]*)");

	protected void setFromResourceSplitedName(ReportPolicyActivityLog log) {
		if (log.fromResourceName != null) {
			Matcher m = RESOURCE_PATTERN.matcher(log.fromResourceName);
			if (m.matches()) {
				log.fromResourcePrefix = m.group(1);
				log.fromResourcePath = m.group(2);
				String name = m.group(3);
				if (name == null || name.trim().length() == 0) {
					log.fromResourceShortName = log.fromResourceName;
				} else {
					log.fromResourceShortName = name;
				}
			} else {
				log.fromResourcePrefix = null;
				log.fromResourcePath = null;
				log.fromResourceShortName = log.fromResourceName;
			}
		}
	}

	protected String lower(String s) {
		return s != null ? s.toLowerCase() : null;
	}
	
	protected void solveUserAttributes(ReportPolicyActivityLog logEntry) {
		if (logEntry == null)
		{
			return;
		}
		
		try {
			logEntry.userAttrs = getUserData(logEntry.userId, logEntry.time);
		} catch (DictionaryException e) {
			LOG.error("Error fetching User attributes from Dictionary API", e);
		}
	}
	
	protected HashMap<String, String> getUserData(long userId, Timestamp time) throws DictionaryException {
		HashMap<String, String> record = null;
			
			IMElement element = dict.getElement(userId, time);
			
            /*
             * ======================================================================
             * should we cache this or leave it to dictionary api (hibernate caching)
             * 
             * if the last consistent time of the enrollment falls outside the records that
             * are fetched in this batch, then we can cache user data based on user ids
             * 
             * ======================================================================
             */
			
			if (element == null) {
				return null;
			}

			IEnrollment enrollment = element.getEnrollment();

			record = new HashMap<String, String>();

			if (enrollment.getIsActive()) {
				IElementType userType = dict.getType("USER");

				//external names are AD or source attribute names
				String[] externalNames = enrollment.getExternalNames(userType);
				IElementField[] userAttrs = new IElementField[externalNames.length];
				for (int i = 0; i < externalNames.length; i++) {
					// Query the enrollment
					userAttrs[i] = enrollment.lookupField(userType, externalNames[i])[0];
					/*
					 * if userAttributesBlackList is not configured then use all 
					 * enrolled attributes
					 */
					String lowerCaseAttrName = userAttrs[i].getName().toLowerCase();
					if (userAttributesBlackList.isEmpty() || 
							!userAttributesBlackList.contains(lowerCaseAttrName)) {
						Object value = element.getValue(userAttrs[i]);
						if (value != null) {
							if (value.getClass().isArray())
							{
								StringBuilder strValue = new StringBuilder();
								boolean first = true;
								for (Object o : (Object[])value)
								{
									if (!first)
									{
										strValue.append(SEPARATOR);
									}
									first = false;
									strValue.append(o.toString());
								}
								record.put(lowerCaseAttrName, strValue.toString());
							}
							else
							{
								record.put(lowerCaseAttrName, value.toString());
							}
						}
					}
				}
			} else {
				/*
				 * if enrollment is not active, report activity log record corresponding to the user will not have any of the 
				 * enrolled user attributes and therefore cannot be queried based on those attributes
				 */
				LOG.error("Enrollment is inactive for User: " + userId + " as on " + time);
			}
		return record;
	}
}