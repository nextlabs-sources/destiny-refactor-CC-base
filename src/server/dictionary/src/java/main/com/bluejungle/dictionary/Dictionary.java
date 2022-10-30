package com.bluejungle.dictionary;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.type.Type;

import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;

public class Dictionary implements IDictionary, IInitializable, IConfigurable, IManagerEnabled {
    private static final String HQL_LOG_PREFIX = Dictionary.class.getName() + ".hql.";
    
    /** The component manager */
    private IComponentManager manager;

    /** Configuration */
    private IConfiguration config;

    /** Hibernate repository */
    private IHibernateRepository hds;

    /** Dictionary Event Manager */
    private IDictionaryEventManager eventManager;

    private Date latestConsistentTime = null;
    public static final ComponentInfo<Dictionary> COMP_INFO = new ComponentInfo<Dictionary>(
                Dictionary.class, 
                LifestyleType.SINGLETON_TYPE);

    /** This constant holds the hibernate type for the type field of the leaf entity. */
    private static final Type TYPE_OF_TYPE_FIELD = Hibernate.entity(ElementType.class);

    private static final String[] UNIQUE_KEY_PARAMETERS = new String[] {":ukUU", ":ukUL", ":ukLU", ":ukLL", ":uk"};

    private static final String[] UNIQUE_KEY_PARAM_NAMES = new String[] {"ukUU", "ukUL", "ukLU", "ukLL", "uk"};

    private static final String SUBSTR_FUNCTION = "substr";

    private static final String SUBSTR_FUNCTION_SQLSERVER = "substring";
    
    @SuppressWarnings( { "unchecked", "deprecation" })
    private class QueryWithLog implements Query {
        final Query original;
        final StringBuilder sb;
        final String prefix;
        final String suffix;
        
        QueryWithLog(Query original, StringBuilder sb, String prefix,
                String suffix) {
            assert original != null;
            assert sb != null;
            
            this.original = original;
            this.sb = sb;
            this.prefix = prefix;
            this.suffix = suffix;
        }
        
        QueryWithLog(Query original, StringBuilder sb) {
            this(original, sb, "\n\t", "");
        }
        
        public Query setParameter(String name, Object val, Type type) {
            sb.append(prefix)
              .append(String.format("setParameter: %s = %s (%s)"
                    , String.valueOf(name)
                    , String.valueOf(val)
                    , type != null ?  type.getName() : "null"
            )).append(suffix);
            return original.setParameter(name, val, type);
        }
        
        public Query setParameter(String name, Object val) throws HibernateException {
            sb.append(prefix)
            .append(String.format("setParameter: %s = %s"
                  , String.valueOf(name)
                  , String.valueOf(val)
          )).append(suffix);
            return original.setParameter(name, val);
        }
        
        public Query setParameterList(String name, Collection vals) throws HibernateException {
            sb.append(prefix)
              .append(String.format("setParameterList: %s = [%d] %s"
                    , String.valueOf(name)
                    , vals != null ? vals.size() : -1
                    , vals != null ? CollectionUtils.asString(vals, ", ") : "null"
            )).append(suffix);
            return original.setParameterList(name, vals);
        }
        
        public Query setFirstResult(int firstResult) {
            sb.append(prefix)
              .append("setFirstResult = ").append(firstResult)
              .append(suffix);
            return original.setFirstResult(firstResult);
        }

        public Query setMaxResults(int maxResults) {
            sb.append(prefix)
              .append("setMaxResults = ").append(maxResults)
              .append(suffix);
            return original.setMaxResults(maxResults);
        }

        
        public String[] getNamedParameters() throws HibernateException {
            return original.getNamedParameters();
        }

        public String getQueryString() {
            return original.getQueryString();
        }

        public Type[] getReturnTypes() throws HibernateException {
            return original.getReturnTypes();
        }

        public Iterator iterate() throws HibernateException {
            return original.iterate();
        }

        public List list() throws HibernateException {
            return original.list();
        }

        public ScrollableResults scroll() throws HibernateException {
            return original.scroll();
        }

        public Query setBigDecimal(int position, BigDecimal number) {
            return original.setBigDecimal(position, number);
        }

        public Query setBigDecimal(String name, BigDecimal number) {
            return original.setBigDecimal(name, number);
        }

        public Query setBinary(int position, byte[] val) {
            return original.setBinary(position, val);
        }

        public Query setBinary(String name, byte[] val) {
            return original.setBinary(name, val);
        }

        public Query setBoolean(int position, boolean val) {
            return original.setBoolean(position, val);
        }

        public Query setBoolean(String name, boolean val) {
            return original.setBoolean(name, val);
        }

        public Query setByte(int position, byte val) {
            return original.setByte(position, val);
        }

        public Query setByte(String name, byte val) {
            return original.setByte(name, val);
        }

        public Query setCacheable(boolean cacheable) {
            return original.setCacheable(cacheable);
        }

        public Query setCacheRegion(String cacheRegion) {
            return original.setCacheRegion(cacheRegion);
        }

        public Query setCalendar(int position, Calendar calendar) {
            return original.setCalendar(position, calendar);
        }

        public Query setCalendar(String name, Calendar calendar) {
            return original.setCalendar(name, calendar);
        }

        public Query setCalendarDate(int position, Calendar calendar) {
            return original.setCalendarDate(position, calendar);
        }

        public Query setCalendarDate(String name, Calendar calendar) {
            return original.setCalendarDate(name, calendar);
        }

        public Query setCharacter(int position, char val) {
            return original.setCharacter(position, val);
        }

        public Query setCharacter(String name, char val) {
            return original.setCharacter(name, val);
        }

        public Query setDate(int position, Date date) {
            return original.setDate(position, date);
        }

        public Query setDate(String name, Date date) {
            return original.setDate(name, date);
        }

        public Query setDouble(int position, double val) {
            return original.setDouble(position, val);
        }

        public Query setDouble(String name, double val) {
            return original.setDouble(name, val);
        }

        public Query setEntity(int position, Object val) {
            return original.setEntity(position, val);
        }

        public Query setEntity(String name, Object val) {
            return original.setEntity(name, val);
        }

        public Query setEnum(int position, Object val) throws MappingException {
            return original.setEnum(position, val);
        }

        public Query setEnum(String name, Object val) throws MappingException {
            return original.setEnum(name, val);
        }

        public Query setFetchSize(int fetchSize) {
            return original.setFetchSize(fetchSize);
        }

        public Query setFloat(int position, float val) {
            return original.setFloat(position, val);
        }

        public Query setFloat(String name, float val) {
            return original.setFloat(name, val);
        }

        public Query setForceCacheRefresh(boolean forceCacheRefresh) {
            return original.setForceCacheRefresh(forceCacheRefresh);
        }

        public Query setInteger(int position, int val) {
            return original.setInteger(position, val);
        }

        public Query setInteger(String name, int val) {
            return original.setInteger(name, val);
        }

        public Query setLocale(int position, Locale locale) {
            return original.setLocale(position, locale);
        }

        public Query setLocale(String name, Locale locale) {
            return original.setLocale(name, locale);
        }

        public void setLockMode(String alias, LockMode lockMode) {
            original.setLockMode(alias, lockMode);
        }

        public Query setLong(int position, long val) {
            return original.setLong(position, val);
        }

        public Query setLong(String name, long val) {
            return original.setLong(name, val);
        }

        public Query setParameter(int position, Object val, Type type) {
            return original.setParameter(position, val, type);
        }

        public Query setParameter(int position, Object val) throws HibernateException {
            return original.setParameter(position, val);
        }

        public Query setParameterList(String name, Collection vals, Type type)
                throws HibernateException {
            return original.setParameterList(name, vals, type);
        }

        public Query setParameterList(String name, Object[] vals, Type type)
                throws HibernateException {
            return original.setParameterList(name, vals, type);
        }

        public Query setParameterList(String name, Object[] vals) throws HibernateException {
            return original.setParameterList(name, vals);
        }

        public Query setProperties(Object bean) throws HibernateException {
            return original.setProperties(bean);
        }

        public Query setSerializable(int position, Serializable val) {
            return original.setSerializable(position, val);
        }

        public Query setSerializable(String name, Serializable val) {
            return original.setSerializable(name, val);
        }

        public Query setShort(int position, short val) {
            return original.setShort(position, val);
        }

        public Query setShort(String name, short val) {
            return original.setShort(name, val);
        }

        public Query setString(int position, String val) {
            return original.setString(position, val);
        }

        public Query setString(String name, String val) {
            return original.setString(name, val);
        }

        public Query setText(int position, String val) {
            return original.setText(position, val);
        }

        public Query setText(String name, String val) {
            return original.setText(name, val);
        }

        public Query setTime(int position, Date date) {
            return original.setTime(position, date);
        }

        public Query setTime(String name, Date date) {
            return original.setTime(name, date);
        }

        public Query setTimeout(int timeout) {
            return original.setTimeout(timeout);
        }

        public Query setTimestamp(int position, Date date) {
            return original.setTimestamp(position, date);
        }

        public Query setTimestamp(String name, Date date) {
            return original.setTimestamp(name, date);
        }

        public Object uniqueResult() throws HibernateException {
            return original.uniqueResult();
        }
    }
    
    /**
     * the method signature can be done by reflection by the name may be obfuscated.
     * It is better to have a hard code method signature.
     * @author hchan
     *
     * @param <T>
     */
    private abstract class Work<T> {
        final String hql;
        final String methodName;
        final String methodParameters;
        final String exceptionMessage;
        
        Transaction tx = null;
        
        //TOOD the exceptionMessage should be consistent
        Work(
                String methodName,
                String methodParameters,
                String exceptionMessage,
                String hql
        ) {
            super();
            this.hql = hql;
            this.methodName = methodName;
            this.methodParameters = methodParameters;
            this.exceptionMessage = exceptionMessage;
        }

        T run() throws DictionaryException {
            final Log log = LogFactory.getLog(HQL_LOG_PREFIX + methodName);
            
            //beware the flag may change during runtime, a local variable is a must.
            final boolean isDebug = log.isDebugEnabled();
            
            long startTime = 0;
            StringBuilder parameterSb = null;
            if (isDebug) {
                startTime = System.currentTimeMillis();
                parameterSb = new StringBuilder();
            }
            
            try {
                Session hs = getCountedSession();
                tx = hs.beginTransaction();
                Query query = hs.createQuery(hql.toString());
                
                if (isDebug) {
                    // wrap the query so we can log what parameter we have set.
                    query = new QueryWithLog(query, parameterSb);
                }
                
                return execute(hs, tx, query);
            } catch (HibernateException cause) {
                throw handleHibernateException(cause);
            } finally {
                doFinally();
                if (isDebug) {
                    long timeTaken = System.currentTimeMillis() - startTime;
                    StringBuilder sb = new StringBuilder();
                    sb.append(methodName).append("(").append(methodParameters).append(") ")
                      .append(timeTaken).append("ms")
                      .append("\n\thql = ").append(hql)
                      .append(parameterSb);
                    log.debug(sb);
                }
            }
        }
        
        /**
         * a lazy method to set the asOf time
         * @param query
         * @param asOf
         */
        void setAsOf(Query query, Date asOf){
            query.setParameter("asOf", asOf != null ? asOf : new Date(), DateToLongUserType.TYPE);
        }
        
        /**
         * 
         * @param hs
         * @param tx
         * @param query 
         * @return
         * @throws HibernateException
         * @throws DictionaryException
         */
        abstract T execute(Session hs, Transaction tx, Query query) throws HibernateException,
                DictionaryException;
        
        /**
         * must return a DictionaryException
         * @param cause
         * @return
         */
        abstract DictionaryException handleHibernateException(HibernateException cause); 
        
        /**
         * something to do in the finally block, no RuntimeimeException please
         */
        abstract void doFinally();
    }
    
    private abstract class IterableWork<T> extends Work<T> {
        IterableWork(
                String methodName,
                String methodParameters,
                String exceptionMessage,
                String hql
        ) {
            super(methodName, methodParameters, exceptionMessage, hql);
        }

        abstract T execute(Session hs, Transaction tx, Query query) throws HibernateException,
                DictionaryException;
        
        DictionaryException handleHibernateException(HibernateException cause) {
            closeInterruptedSession(tx);
            return new DictionaryException(exceptionMessage, cause);
        }
        
        void doFinally() {
            //nothing to do
        }
        
    }
    
    private abstract class NonIterableWork<T> extends Work<T> {
        NonIterableWork(
                String methodName,
                String methodParameters,
                String exceptionMessage,
                String hql
        ) {
            super(methodName, methodParameters, exceptionMessage, hql);
        }

        final T execute(Session hs, Transaction tx, Query query) throws HibernateException,
                DictionaryException {
            return execute(hs, query);
        }
        
        abstract T execute(Session hs, Query query) throws HibernateException, DictionaryException;
        
        DictionaryException handleHibernateException(HibernateException cause) {
            abortTransaction(tx);
            tx = null;
            return new DictionaryException(exceptionMessage, cause);
        }
        
        void doFinally() {
            tryCommitAbortOnError(tx);
        }
    }

    /* XXX will concatenate
	 * @see IDictionary#query(IPredicate, Date, Order[], int)
	 */
	@Override
    public IDictionaryIterator<IMElement> query(
            final IPredicate condition, 
            final Date asOf, 
            final Order[] orderIn, 
			final int limit
    ) throws DictionaryException {
		/* 
		 * This method is similar to the Paged query, except that we are using Page to limit the amount of rows 
		 * to return, and don't care about stable sort order by ID, which is faster than ordering by ID 
		 * when we are only fetching a few records of a large table.
		 */
		if (limit <= 0) {
			throw new IllegalArgumentException("limit must be a positive value.");
		}

        if (condition == null) {
            throw new NullPointerException("condition");
        }
        
		final Page page = new Page(0, limit); // used to set a limit of values to return.
		
        final ElementHQLFormatVisitor fv = new ElementHQLFormatVisitor("le");
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
        StringBuffer hql = new StringBuffer(
            "from LeafElement le where "
        );
        hql.append(fv.getResult());
        hql.append(" and le.timeRelation.activeTo > :asOf and le.timeRelation.activeFrom <= :asOf ");
        // Order is specified with an array, which is always mutable.
        // To avoid threading issues if the order is modified in one thread
        // while this thread is working, make a copy and use it from now on:
        Order[] order;
        if ( orderIn != null ) {
            order = new Order[orderIn.length];
            System.arraycopy(orderIn, 0, order, 0, orderIn.length);
        } else {
            order = null;
        }
        if (order != null && order.length != 0 ) {
            // Fields by which we order must be non-null
            for( int i = 0 ; i != order.length ; i++ ) {
                hql.append(" and le.");
                IElementField field = order[i].getField();
                if ( field == null ) {
                    throw new NullPointerException("order["+i+"].field");
                }
                if (field == PATH_FIELD) {
                    hql.append("path");
                } else {
                    if ( !(field instanceof ElementField) ) {
                        throw new IllegalArgumentException("order["+i+"].field");
                    }
                    ElementField ef = (ElementField)field;
                    hql.append(ef.getMapping());
                }
                hql.append(" is not null");
            }
            hql.append(" ORDER BY ");
            for( int i = 0 ; i != order.length ; i++ ) {
                if ( i != 0 ) {
                    hql.append(", ");
                }
                hql.append("le.");
                IElementField field = order[i].getField();
                if ( field == null ) {
                    throw new NullPointerException("order["+i+"].field");
                }
                if (field == PATH_FIELD) {
                    hql.append("path");
                } else {
                    if ( !(field instanceof ElementField) ) {
                        throw new IllegalArgumentException("order["+i+"].field");
                    }
                    ElementField ef = (ElementField)field;
                    hql.append(ef.getMapping());
                }
                hql.append(order[i].isAscending() ? " ASC" : " DESC");
            }
            hql.append(", le.id");
        }
        
        
        return new IterableWork<IDictionaryIterator<IMElement>>(
                "query",                            //String methodName,
                "IPredicate, Date, Order[], Page",  //String methodParameters
                "Unable to query elements",         //String exceptionMessage,
                hql.toString()                      //String hql
        ){
            @Override
            IDictionaryIterator<IMElement> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                fv.bindParametersToQuery(query);
                addPaging(query, page);
                return new ElementBaseIterator<IMElement>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    
	}

	/**
     * XXX will concatenate
     * @see IDictionary#query(IPredicate, Date, Order[], Page)
     */
    public IDictionaryIterator<IMElement> query(
            final IPredicate condition, 
            final Date asOf, 
            final Order[] orderIn, 
            final Page page
    ) throws DictionaryException {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        final ElementHQLFormatVisitor fv = new ElementHQLFormatVisitor("le");
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
        StringBuffer hql = new StringBuffer(
            "from LeafElement le where "
        );
        hql.append(fv.getResult());
        hql.append(" and le.timeRelation.activeTo > :asOf and le.timeRelation.activeFrom <= :asOf ");
        // Order is specified with an array, which is always mutable.
        // To avoid threading issues if the order is modified in one thread
        // while this thread is working, make a copy and use it from now on:
        Order[] order;
        if ( orderIn != null ) {
            order = new Order[orderIn.length];
            System.arraycopy(orderIn, 0, order, 0, orderIn.length);
        } else {
            order = null;
        }
        if (order != null && order.length != 0 ) {
            // Fields by which we order must be non-null
            for( int i = 0 ; i != order.length ; i++ ) {
                hql.append(" and le.");
                IElementField field = order[i].getField();
                if ( field == null ) {
                    throw new NullPointerException("order["+i+"].field");
                }
                if (field == PATH_FIELD) {
                    hql.append("path");
                } else {
                    if ( !(field instanceof ElementField) ) {
                        throw new IllegalArgumentException("order["+i+"].field");
                    }
                    ElementField ef = (ElementField)field;
                    hql.append(ef.getMapping());
                }
                hql.append(" is not null");
            }
            hql.append(" ORDER BY ");
            for( int i = 0 ; i != order.length ; i++ ) {
                if ( i != 0 ) {
                    hql.append(", ");
                }
                hql.append("le.");
                IElementField field = order[i].getField();
                if ( field == null ) {
                    throw new NullPointerException("order["+i+"].field");
                }
                if (field == PATH_FIELD) {
                    hql.append("path");
                } else {
                    if ( !(field instanceof ElementField) ) {
                        throw new IllegalArgumentException("order["+i+"].field");
                    }
                    ElementField ef = (ElementField)field;
                    hql.append(ef.getMapping());
                }
                hql.append(order[i].isAscending() ? " ASC" : " DESC");
            }
            hql.append(", le.id");
        }else{
        	if (page != null) { 
        		// We need a stable sort order for Paging across results, 
        		// but don't need it when no paging and no order are specified. 
        		hql.append(" ORDER BY le.id");
        	}
        }
        
        
        return new IterableWork<IDictionaryIterator<IMElement>>(
                "query",                            //String methodName,
                "IPredicate, Date, Order[], Page",  //String methodParameters
                "Unable to query elements",         //String exceptionMessage,
                hql.toString()                      //String hql
        ){
            @Override
            IDictionaryIterator<IMElement> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                fv.bindParametersToQuery(query);
                addPaging(query, page);
                return new ElementBaseIterator<IMElement>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * @see IDictionary#queryFields(IElementField[], IPredicate, Date, Order[], Page)
     */
    public IDictionaryIterator<ElementFieldData> queryFields(
            final IElementField[] fields, 
            final IPredicate condition, 
            final Date asOf, 
            final Order[] orderIn, 
            final Page page
    ) throws DictionaryException {
        if (fields == null) {
            throw new NullPointerException("fields");
        }
        if ( condition == null ) {
            throw new NullPointerException("condition");
        }
        final ElementHQLFormatVisitor fv = new ElementHQLFormatVisitor("le");
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
        StringBuffer hql = new StringBuffer("select ");
        for (int i = 0; i != fields.length; i++) {
            hql.append("le.");
            hql.append(((ElementField)fields[i]).getMapping());
            hql.append(", ");
        }
        hql.append("le.originalId, le.type, le.uniqueName from LeafElement le where ")
           .append(fv.getResult())
           .append(" and le.timeRelation.activeTo > :asOf and le.timeRelation.activeFrom <= :asOf ");
        // Order is specified with an array, which is always mutable.
        // To avoid threading issues if the order is modified in one thread
        // while this thread is working, make a copy and use it from now on:
        Order[] order;
        if ( orderIn != null ) {
            order = new Order[orderIn.length];
            System.arraycopy(orderIn, 0, order, 0, orderIn.length);
        } else {
            order = null;
        }
        if (order != null && order.length != 0 ) {
            hql.append(" ORDER BY ");
            for( int i = 0 ; i != order.length ; i++ ) {
                if ( i != 0 ) {
                    hql.append(", ");
                }
                hql.append("le.");
                IElementField field = order[i].getField();
                if ( field == null ) {
                    throw new NullPointerException("order["+i+"].field");
                }
                if (field == PATH_FIELD) {
                    hql.append("path");
                } else {
                    if ( !(field instanceof ElementField) ) {
                        throw new IllegalArgumentException("order["+i+"].field");
                    }
                    ElementField ef = (ElementField)field;
                    hql.append(ef.getMapping());
                }
                hql.append(order[i].isAscending() ? " ASC" : " DESC");
            }
            hql.append(", le.id");
        }else{
        	if (page != null) { 
        		// We need a stable sort order for Paging across results, 
        		// but don't need it when no paging and no order are specified. 
        		hql.append(" ORDER BY le.id");
        	}
        }
        
        return new IterableWork<IDictionaryIterator<ElementFieldData>>(
                "queryFields",
                "IElementField[], IPredicate, Date, Order[], Page",
                "Unable to query fields",  //String exceptionMessage,
                hql.toString()             //String hql
        ){
            @Override
            IDictionaryIterator<ElementFieldData> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                fv.bindParametersToQuery(query);
                addPaging(query, page);
                return DictionaryIterator.forFields(fields, query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * the condition should only contains one type
     * XXX will concatenate
     * @see IDictionary#queryKeys(IPredicate, Date)
     */
    public IDictionaryIterator<Long> queryKeys(
            final IPredicate condition, 
            final Date asOf
    ) throws DictionaryException {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        final ElementHQLFormatVisitor fv = new ElementHQLFormatVisitor("le");
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
    
        final String hql = "select le.originalId from LeafElement le where "
            +   fv.getResult()
            +   " and le.timeRelation.activeTo > :asOf"
            +   " and le.timeRelation.activeFrom <= :asOf"
            +   " order by le.originalId asc";
        
        return new IterableWork<IDictionaryIterator<Long>>(
                "queryKeys",
                "IPredicate, Date",
                "Unable to query element keys",  //String exceptionMessage,
                hql                              //String hql
        ){
            @Override
            IDictionaryIterator<Long> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                fv.bindParametersToQuery(query);
                return new DictionaryIterator<Long>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * @see IDictionary#getStructuralGroups(DictionaryPath, String, IElementType, Date, Page)
     */
    public IDictionaryIterator<IMGroup> getStructuralGroups(
            final DictionaryPath path, 
            final String template, 
            final IElementType type, 
            final Date asOf, 
            final Page page
    ) throws DictionaryException {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (template == null) {
            throw new NullPointerException("template");
        }
        CompositePredicate cond = new CompositePredicate(
            BooleanOp.AND
        ,   condition(path, false));
        cond.addPredicate( condition(
            new DictionaryPath( new String[] {"%", template} ), true
        ));
        return getStructuralGroups(cond, type, asOf, page);
    }

    /**
     * @see IDictionary#getEnumeratedGroups(String, IElementType, Date, Page)
     */
    public IDictionaryIterator<IMGroup> getEnumeratedGroups(
            final String template,
            final IElementType type, 
            final Date asOf, 
            final Page page 
    ) throws DictionaryException {
        if (template == null) {
            throw new NullPointerException("template");
        }
        return getEnumeratedGroups(condition(
            new DictionaryPath( new String[] {"%", template} ), true
        ), type, asOf, page);
    }

    /**
     * @see IDictionary#getEnumeratedGroups(IPredicate, IElementType, Date, Page)
     */
    public IDictionaryIterator<IMGroup> getEnumeratedGroups(
            final IPredicate condition,
            final IElementType type, 
            final Date asOf, 
            final Page page
    ) throws DictionaryException {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        final GroupHQLFormatVisitor fv = new GroupHQLFormatVisitor("eg", type);
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
        StringBuffer hql = new StringBuffer();
        hql.append("from EnumeratedGroup eg,where ")
           .append(fv.getResult())
           .append(" and ( eg.timeRelation.activeTo > :asOf and eg.timeRelation.activeFrom <= :asOf ) ");

        if ( (type != null) ) {
            if ( !( condition instanceof ChangedDictionaryPredicate ) ) {
                hql.append(
                    " and ( eg.originalId in ("
                    + " (select em.groupId from EnumerationMember em "
                    +   " where em.elementTypeId=:typeId"
                    +     " and em.timeRelation.activeTo > :asOf"
                    +     " and em.timeRelation.activeFrom <= :asOf)"
                    + " union "
                    + " (select xm.fromId from EnumerationGroupMember xm, EnumerationMember em"
                    +   " where xm.toId = em.groupId"
                    +     " and em.elementTypeId=:typeId"
                    +     " and xm.timeRelation.activeTo > :asOf"
                    +     " and xm.timeRelation.activeFrom <= :asOf)"
                    + " ) ) order by eg.id"
                );
            }
        }
        
        return new IterableWork<IDictionaryIterator<IMGroup>>(
                "getEnumeratedGroups",
                "IPredicate, IElementType, Date, Page",
                "Unable to get enumerated groups",  //String exceptionMessage,
                hql.toString()                      //String hql
        ){
            @Override
            IDictionaryIterator<IMGroup> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                if (type != null) {
                    query.setParameter("typeId", ((ElementType)type).getId());
                }
                fv.bindParametersToQuery(query);
                addPaging(query, page);
                return new ElementBaseIterator<IMGroup>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * @see IDictionary#getStructuralGroups(IPredicate, IElementType, Date, Page)
     */
    public IDictionaryIterator<IMGroup> getStructuralGroups(
            final IPredicate condition,
            final IElementType type, 
            final Date asOf, 
            final Page page
    ) throws DictionaryException {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        final GroupHQLFormatVisitor fv = new GroupHQLFormatVisitor("sg");
        condition.accept(fv, IPredicateVisitor.PREPOSTORDER );
        StringBuffer hql = new StringBuffer();
        hql.append("from StructuralGroup sg where ")
           .append(fv.getResult())
           .append(" and sg.timeRelation.activeTo > :asOf and sg.timeRelation.activeFrom <= :asOf ");
        if (type != null) {
            hql.append(" and exists (from LeafElement le where ")
               .append(isMsSql() ? SUBSTR_FUNCTION_SQLSERVER : SUBSTR_FUNCTION)
               .append(  " (le.path.path,1,sg.filterLength) = sg.filter")
               .append(  " and le.type = :type")
               .append(  " and le.timeRelation.activeTo > :asOf")
               .append(  " and le.timeRelation.activeFrom <= :asOf")
               .append(" ) order by sg.id");
        }
        
        
        return new IterableWork<IDictionaryIterator<IMGroup>>(
                "getStructuralGroups",
                "IPredicate, IElementType, Date, Page",
                "Unable to get structural groups",  //String exceptionMessage,
                hql.toString()                      //String hql
        ){
            @Override
            IDictionaryIterator<IMGroup> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                if (type != null) {
                    query.setParameter("type", type, TYPE_OF_TYPE_FIELD);
                }
                fv.bindParametersToQuery(query);
                addPaging(query, page);
                return new ElementBaseIterator<IMGroup>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * XXX will concatenate
     * @see IDictionary#getByKey(Long, Date)
     */
    public IMElementBase getByKey(
            final Long key, 
            final Date asOf
    ) throws DictionaryException {
        if ( key == null ) {
            throw new NullPointerException("key");
        }
        
        final String hql = "from DictionaryElementBase b where"
        	+	    " b.originalId=:key"
            +   " and b.timeRelation.activeTo > :asOf"
            +   " and b.timeRelation.activeFrom <= :asOf";
        
        
        IMElementBase res = new NonIterableWork<IMElementBase>(
                "getByKey",
                "Long, Date",
                "Unable to retrieve an element by key: " + key,
                hql
        ){
            @Override
            IMElementBase execute(Session hs, Query query) throws HibernateException {
                setAsOf(query, asOf);
                query.setParameter("key", key);
                DictionaryElementBase res = (DictionaryElementBase) query.uniqueResult();
                if (res != null) {
                    reparentAndDisconnect(hs, res.getEnrollment());
                }
                return res;
            }
        }.run();
        
        concatenate(res);
        
        return res;
    }

    /**
     * @see IDictionary#getGroup(DictionaryPath)
     */
    public IMGroup getGroup(
            final DictionaryPath path,
            final Date asOf
    ) throws DictionaryException {
        if (path == null) {
            throw new NullPointerException("path");
        }
        
        final String hql = "from DictionaryElementBase eb where"
            +       " eb.path.path = :path"
            +   " and eb.timeRelation.activeTo > :asOf"
            +   " and eb.timeRelation.activeFrom <= :asOf";
        
        
        return new NonIterableWork<IMGroup>(
                "getGroup",
                "DictionaryPath, Date",
                "Unable to get group " + path,
                hql
        ){
            @Override
            IMGroup execute(Session hs, Query query) throws HibernateException {
                setAsOf(query, asOf);
                query.setParameter("path", PathUserType.escapeDictionaryPath(path));
                Collection<?> vals = query.list();
                if (vals.isEmpty()) {
                    return null;
                }
                Object res = vals.iterator().next();
                if (res != null && !(res instanceof IMGroup)) {
                    throw new IllegalArgumentException("Path " + path + " does not represent a group.");
                }

                IMGroup group = (IMGroup) res;
                reparentAndDisconnect(hs, group.getEnrollment());
                hs.evict(group);
                return group;
            }
        }.run();
    }

    /**
     * XXX will concatenate
     * @see IDictionary#getElement(Long, Date)
     */
    public IMElement getElement( Long key, Date asOf ) throws DictionaryException {
        IMElementBase res = getByKey(key, asOf);
        if (res == null || !(res instanceof IMElement)) {
            return null;
        }
        if (!(res instanceof IMElement)) {
            throw new DictionaryException("Internal key does not correspond to an element: " + key);
        }
        return (IMElement) res;
    }

    /**
     * @see IDictionary#getGroup(Long, Date)
     */
    public IMGroup getGroup(Long key, Date asOf) throws DictionaryException {
        IMElementBase res = getByKey(key, asOf);
        if (res == null || !(res instanceof IMGroup)) {
            return null;
        }
        return (IMGroup) res;
    }


    /**
     * XXX will concatenate
     * @see IDictionary#getByUniqueName(String, Date)
     */
    public IMElementBase getByUniqueName(
            String uniqueName,
            Date asOf
    ) throws DictionaryException {
        Collection<IMElementBase> vals = getElementsByUniqueName(uniqueName, asOf);
        if (vals == null || vals.isEmpty()) {
            return null;
        }
        Iterator<IMElementBase> iter = vals.iterator();
        IMElementBase res = iter.next();
        if (iter.hasNext()) {
            throw new DictionaryException("Name " + uniqueName
                    + " does not identify an element uniquely.");
        }
        return res;
    }

    /**
     * XXX will concatenate
     * @param uniqueName
     * @param asOf
     * @return
     * @throws DictionaryException
     */
    Collection<IMElementBase> getElementsByUniqueName(
            String uniqueName
          , Date asOf
    ) throws DictionaryException {
        return getElementsByUniqueName(uniqueName, asOf, true);
    }
    
    /**
     * XXX may concatenate
     * @param uniqueName
     * @param asOf
     * @return
     * @throws DictionaryException
     */
    Collection<IMElementBase> getElementsByUniqueName(
            final String uniqueName
          , final Date asOf
          , final boolean concatenate 
    ) throws DictionaryException {
        if (uniqueName == null) {
            throw new NullPointerException("uniqueName");
        }
        StringBuffer hql = new StringBuffer();
        hql.append("from DictionaryElementBase eb where")
           .append(    " eb.timeRelation.activeTo > :asOf")
           .append(" and eb.timeRelation.activeFrom <= :asOf")
           .append(" and ");
        final CaseInsensitiveLike cond = new CaseInsensitiveLike(
            "<unused>", uniqueName
        );
        hql.append(
            cond.getCondition(
                "eb.uniqueName", UNIQUE_KEY_PARAMETERS, "lower"
            )
        );
        
        return new NonIterableWork<List<IMElementBase>>(
                "getElementsByUniqueName",
                "String, Date",
                "Unable to get element " + uniqueName,
                hql.toString()
        ){
            @Override
            List<IMElementBase> execute(Session hs, Query query) throws HibernateException,
                    DictionaryException {
                setAsOf(query, asOf);
                String[] bindings = cond.getBindStrings();
                for (int i = 0 ; i != bindings.length ; i++) {
                    query.setParameter(UNIQUE_KEY_PARAM_NAMES[i], bindings[i]);
                }
                @SuppressWarnings("unchecked")
                List<IMElementBase> vals = query.list();
                if (vals.isEmpty()) {
                    return null;
                }
                for (IMElementBase val : vals) {
                    reparentAndDisconnect(hs, val.getEnrollment());
                    hs.evict(val);
                    if (concatenate) {
                        concatenate(val);
                    }
                }
                return vals;
            }
        }.run();
    }

    /**
     * XXX will concatenate
     * @see IDictionary#getElement(String, Date)
     */
    public IMElement getElement(String uniqueName, Date asOf) throws DictionaryException {
        IMElementBase res = getByUniqueName(uniqueName, asOf);
        if (res == null) {
            return null;
        }
        if (!(res instanceof IMElement)) {
            throw new DictionaryException("Unique name does not correspond to an element: " + uniqueName);
        }
        return (IMElement)res;
    }

    /**
     * @see IDictionary#getGroup(String, Date)
     */
    public IMGroup getGroup(String uniqueName, Date asOf) throws DictionaryException {
        IMElementBase res = getByUniqueName(uniqueName, asOf);
        if (res == null) {
            return null;
        }
        if (!(res instanceof IMGroup)) {
            throw new DictionaryException("Unique name does not correspond to a group: " + uniqueName);
        }
        return (IMGroup) res;
    }

    /**
     * XXX will concatenate
     * @see IDictionary#getElementsById(Collection, Date)
     */
    public IDictionaryIterator<IMElementBase> getElementsById(
            final Collection<Long> ids, 
            final Date asOf
    ) throws DictionaryException {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        
        final String hql = 
                "from DictionaryElementBase e where"
            +       " e.originalId in (:ids)"
            +   " and e.timeRelation.activeFrom <= :asOf"
            +   " and e.timeRelation.activeTo > :asOf";
        
        return new IterableWork<IDictionaryIterator<IMElementBase>>(
                "getElementsById",
                "Collection<Long>, Date",
                "Unable to get elements",
                hql.toString()
        ){
            @Override
            IDictionaryIterator<IMElementBase> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                setAsOf(query, asOf);
                query.setParameterList("ids", ids);
                return new ElementBaseIterator<IMElementBase>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }

    /**
     * @see IDictionary#getUpdateRecords(Date, Date)
     */
    public List<IUpdateRecord> getUpdateRecords(
            final Date startTime, 
            final Date endTime
    ) throws DictionaryException {
        StringBuffer hql = new StringBuffer("from UpdateRecord ur");
        if (startTime != null || endTime != null) {
            hql.append(" where ");
            
            if (startTime != null) {
                hql.append("ur.timeRelation.activeFrom <= :startTime and ")
                   .append("ur.timeRelation.activeTo > :startTime");
                if(endTime != null){
                    hql.append(" and ");
                }
            }
            if (endTime != null) {
                hql.append("ur.timeRelation.activeFrom <= :endTime and ")
                   .append("ur.timeRelation.activeTo > :endTime");
            }
        }
        
        return new NonIterableWork<List<IUpdateRecord>>(
                "getUpdateRecords",
                "Date, Date",
                "Unable to get update records",
                hql.toString()
        ){
            @Override
            List<IUpdateRecord> execute(Session hs, Query query) throws HibernateException,
                    DictionaryException {
                if (startTime != null) {
                    query.setParameter("startTime", startTime, DateToLongUserType.TYPE);
                }
                if (endTime != null) {
                    query.setParameter("endTime", endTime, DateToLongUserType.TYPE);
                }
                @SuppressWarnings("unchecked")
                List<IUpdateRecord> res = query.list();
                for (Object record : res) {
                    hs.evict(record);
                }
                return res;
            }
        }.run();
    }

    /**
     * @see IDictionary#getEarliestConsistentTimeSince(Date)
     */
    public Date getEarliestConsistentTimeSince(final Date startTime) throws DictionaryException {
        StringBuilder hql = new StringBuilder();
        hql.append("select min(ur.endTime)")
            .append(" from UpdateRecord ur where")
            .append(" not ( exists (")
            .append(    " from UpdateRecord u where")
            .append(        " u.timeRelation.activeFrom <= ur.timeRelation.activeFrom")
            .append(    " and u.timeRelation.activeTo > ur.timeRelation.activeFrom")
            .append(    " and u.successful='N' ) )")
            .append(" and ur.successful='Y'");
        
        if (startTime != null) {
            hql.append(" and ur.timeRelation.activeFrom > :startTime ");
        }
        
        return new NonIterableWork<Date>(
                "getEarliestConsistentTimeSince",
                "Date",
                "Unable to get earliest consistent time since " + startTime,
                hql.toString()
        ){
            @Override
            Date execute(Session hs, Query query) throws HibernateException,
                    DictionaryException {
                if (startTime != null) {
                    query.setParameter("startTime", startTime, DateToLongUserType.TYPE);
                }
                return (Date)query.uniqueResult();
            }
        }.run();
    }

    /**
     * This package-private method actually gets the latest consistent time,
     * as opposed to the public one that uses the cached value.
     */
    synchronized Date fetchLatestConsistentTime() throws DictionaryException {
        StringBuilder hql = new StringBuilder();
        hql.append("select max(ur.endTime)")
            .append(" from UpdateRecord ur where")
            .append(" not ( exists (")
            .append(    " from UpdateRecord u where")
            .append(        " u.timeRelation.activeFrom <= ur.timeRelation.activeFrom")
            .append(    " and u.timeRelation.activeTo > ur.timeRelation.activeFrom")
            .append(    " and u.successful='N' ) )")
            .append(" and ur.successful='Y'");
        
        return new NonIterableWork<Date>(
                "fetchLatestConsistentTime",
                " ",
                "Unable to fetch latest consistent time since",
                hql.toString()
        ){
            @Override
            Date execute(Session hs, Query query) throws HibernateException,
                    DictionaryException {
                return (Date) query.uniqueResult();
            }
        }.run();
    }

    /**
     * @see IDictionary#getEnrollment(String)
     */
    public IEnrollment getEnrollment( String domainName ) throws DictionaryException {
        Transaction tx = null;
        try {
            Session hs = getCountedSession();
            tx = hs.beginTransaction();
            IEnrollment res = (IEnrollment)hs
                .createCriteria(Enrollment.class)
                .add(Expression.eq("domainName", domainName))
                .add(Expression.eq("isActive", Boolean.TRUE))
                .uniqueResult();
            return reparentAndDisconnect(hs, res);
        } catch ( HibernateException cause ) {
            abortTransaction( tx );
            tx = null;
            throw new DictionaryException( "Unable to get enrollment for the domain: "+domainName, cause );
        } finally {
            tryCommitAbortOnError( tx );
        }
    }

    /**
     * @see IDictionary#makeNewEnrollment(String)
     */
    public IEnrollment makeNewEnrollment( String domainName ) throws DictionaryException {
        Transaction tx = null;
        try {
            // if there is an inactive enrollment, activate and return
            Session hs = getCountedSession();
            tx = hs.beginTransaction();
            Enrollment existingEnrollment = (Enrollment) hs.createCriteria(Enrollment.class)
                  .add(Expression.eq("domainName", domainName))
                  .uniqueResult();
            if ( ( existingEnrollment != null ) &&
                 ( existingEnrollment.getIsActive() == false ) ) {
                existingEnrollment.setIsActive(true);
                return reparentAndDisconnect(hs, existingEnrollment);
            }
            
            return new Enrollment(domainName, this);
        }
        catch (HibernateException e) {
            abortTransaction(tx);
            tx = null;
            throw new DictionaryException("failed to create enrollment", e);
        } finally {
            tryCommitAbortOnError(tx);
        }

    }

    /**
     * @see IDictionary#getEnrollments()
     */
    public Collection<IEnrollment> getEnrollments() throws DictionaryException {
        Transaction tx = null;
        try {
            Session hs = getCountedSession();
            tx = hs.beginTransaction();
            @SuppressWarnings("unchecked")
            List<IEnrollment> res = hs.createCriteria(Enrollment.class)
                .add(Expression.eq("isActive", Boolean.TRUE))
                .list();
            for ( IEnrollment enrollment : res ) {
                reparentAndDisconnect(hs, enrollment);
            }
            return res;
        } catch (HibernateException cause) {
            abortTransaction(tx);
            tx = null;
            throw new DictionaryException("Unable to retrieve enrollments", cause);
        } finally {
            tryCommitAbortOnError(tx);
        }
    }

    /**
     * @see IDictionary#createSession()
     */
    public IConfigurationSession createSession() throws DictionaryException {
        return new ConfigurationSession( this, getCountedSession() );
    }

    /**
     * @see IDictionary#getAllTypes()
     */
    public Collection<IElementType> getAllTypes() throws DictionaryException {
        Transaction tx = null;
        try {
            Session hs = getCountedSession();
            tx = hs.beginTransaction();
            @SuppressWarnings("unchecked")
            List<IElementType> res = hs.createCriteria(ElementType.class).list();
            for (IElementType type : res) {
                hs.evict(type);
            }
            return res;
        } catch (HibernateException cause) {
            abortTransaction(tx);
            tx = null;
            throw new DictionaryException("Unable to get element types", cause);
        } finally {
            tryCommitAbortOnError(tx);
        }
    }

    /**
     * @see IDictionary#getType(String)
     */
    public IMElementType getType( String name ) throws DictionaryException {
        Transaction tx = null;
        try {
            Session hs = getCountedSession();
            tx = hs.beginTransaction();
            IMElementType res = (IMElementType)hs
                .createCriteria(ElementType.class)
                .add(Expression.eq("name", name))
                .uniqueResult();
            if ( res == null ) {
                throw new DictionaryException("Unknown type: "+name);
            }
            hs.evict(res);
            return res;
        } catch (HibernateException cause) {
            abortTransaction(tx);
            tx = null;
            throw new DictionaryException("Unable to get element type: " + name, cause);
        } finally {
            tryCommitAbortOnError(tx);
        }
    }

    /**
     * @see IDictionary#makeNewType(String)
     */
    public IMElementType makeNewType( String name ) throws DictionaryException {
        return new ElementType( name );
    }

    /**
     * @see IDictionary#close()
     */
    public void close() throws DictionaryException {
        try {
            hds.close();
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        } finally {
            hds = null;
        }
    }

    /**
     * @see IDictionary#condition(DictionaryPath)
     */
    public IPredicate condition(final DictionaryPath path, final boolean direct) {
        return new AbstractDictionaryPredicate() {
            /**
             * @see IDictionaryPredicate#accept(IDictionaryPredicateVisitor)
             */
            public void accept(IDictionaryPredicateVisitor visitor) {
                visitor.visitDictionaryPath(path, direct);
            }
        };
    }
    
    private static class ElementTypeConditionPredicate extends AbstractDictionaryPredicate {

        private final ElementType type;
        
        ElementTypeConditionPredicate(ElementType type) {
            this.type = type;
        }
        
        @Override
        public void accept(IDictionaryPredicateVisitor visitor) {
            visitor.visitElementType(type);
        }
    }

    /**
     * @see IDictionary#condition(IElementType)
     */
    public IPredicate condition(IElementType elementType) {
        if (!(elementType instanceof ElementType)) {
            throw new IllegalArgumentException("elementType must come from this dictionary.");
        }
        return new ElementTypeConditionPredicate((ElementType)elementType);
    }

    /**
     * @see IDictionary#condition(IEnrollment)
     */
    public IPredicate condition(final IEnrollment enrollmentObj) {
        if (!(enrollmentObj instanceof Enrollment)) {
            throw new IllegalArgumentException("enrollment must come from this dictionary.");
        }
        final Enrollment enrollment = (Enrollment)enrollmentObj;
        return new AbstractDictionaryPredicate() {
            /**
             * @see IDictionaryPredicate#accept(IDictionaryPredicateVisitor)
             */
            public void accept(IDictionaryPredicateVisitor visitor) {
                visitor.visitEnrollment(enrollment);
            }
        };
    }

    /**
     * @see IDictionary#changedCondition(Date, Date)
     */
    public IPredicate changedCondition( final Date startDate, final Date endDate ) {
        return new ChangedDictionaryPredicate(startDate, endDate);
    }

    /**
     * @see IDictionary#displayNameAttribute()
     */
    public IAttribute displayNameAttribute() {
        return ElementBaseAttribute.DISPLAY_NAME;
    }

    /**
     * @see IDictionary#internalKeyAttribute()
     */
    public IAttribute internalKeyAttribute() {
        return ElementBaseAttribute.INTERNAK_KEY;
    }

    /**
     * @see IDictionary#uniqueNameAttribute()
     */
    public IAttribute uniqueNameAttribute() {
        return ElementBaseAttribute.UNIQUE_NAME;
    }

    @SuppressWarnings("deprecation")
    public void init() {
        hds = (IHibernateRepository) manager.getComponent(DestinyRepository.DICTIONARY_REPOSITORY.getName());
        eventManager = new DictionaryEventManager(this, manager);
    }

    public void setConfiguration( IConfiguration config ) {
        this.config = config;
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public void setManager( IComponentManager manager ) {
        this.manager = manager;
    }

    public IComponentManager getManager() {
        return manager;
    }

    /**
     * This is a package-private method for <code>Enrollment</code>
     * to open enrollment sessions.
     * @return a new counted session created from Dictionary's session factory.
     */
    Session getCountedSession() throws DictionaryException {
        if ( hds == null ) {
            throw new IllegalStateException("dictionary repository must be active");
        }
        try {
            Session res = hds.getCountedSession();
            res.setFlushMode(FlushMode.COMMIT);
            return res;
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }
    
    int getCountedSessionCount() {
        return hds.getCountedSessionCount();
    }

    /**
     * This is a package-private method for <code>Enrollment</code>
     * to close counted sessions.
     */
    void closeCurrentSession() throws DictionaryException {
        try {
            hds.closeCurrentSession();
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }

    /**
     * Tries to commit the given transaction. If the commit fails,
     * the transaction is aborted.
     * @param tx the transaction to be committed.
     */
    void tryCommitAbortOnError(Transaction tx) {
        if ( tx != null ) {
            try {
                tx.commit();
            } catch ( HibernateException ignored ) {
                try {
                    tx.rollback();
                } catch ( HibernateException alsoIgnored ) {
                }
            }
        }
        try {
            closeCurrentSession();
        } catch ( DictionaryException ignored ) {
        }
    }

    /**
     * Other methods of this class call this method when they catch
     * a <code>HibernateException</code> to abort the current transaction
     * if it is currently in progress.
     *
     * @param tx the <code>Transaction</code> to be aborted.
     */
    private void abortTransaction( Transaction tx ) {
        if ( tx != null ) {
            try {
                tx.rollback();
            } catch ( HibernateException ignored ) {
            }
        }
    }

    /**
     * Rolls back the transaction if not null, and closes the current session
     * ignoring possible errors. This method should be called from catch blocks
     * to perform the cleanup after catching an exception.
     *
     * @param tx the transaction to rollback.
     */
    private void closeInterruptedSession(Transaction tx) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (HibernateException ignored) {
            }
        }
        try {
            closeCurrentSession();
        } catch (DictionaryException ignored) {
        }
    }

    /**
     * This method sets optional paging parameters to the query.
     * @param query the query to which we need to set the paging
     * parameters. Query argument may not be null.
     * @param page the page to set to the query. Page may be null.
     */
    private static void addPaging(Query query, Page page) {
        if ( page != null ) {
            query.setFirstResult(page.getFrom());
            query.setMaxResults(page.getCount());
        }
    }

    /**
     * This class formats HQL queries for elements.
     */
    private static class ElementHQLFormatVisitor extends AbstractHQLFormatVisitor {
        protected ElementHQLFormatVisitor(String alias) {
            super( alias );
        }

        /**
         * Converts an <code>EnumeratedGroup</code> to an HQL condition
         * testing direct membership or an element.
         *
         * @param group the enumerated group to convert to HQL.
         */
        public void visitDirectMembership( EnumeratedGroup group ) {
            String memberAlias = "em"+getParameterCount();
            put("(");
            addParameter(group.getOriginalId(), Hibernate.LONG);
            put(" in ( select " + memberAlias + ".groupId from EnumerationMember ", memberAlias, " where ");
            put(memberAlias, ".memberId=");
            put(getAlias(), ".originalId and ");
            put(memberAlias, ".timeRelation.activeTo > :asOf and ", memberAlias);
            put(".timeRelation.activeFrom <= :asOf))");
        }

        /**
         * Converts an <code>EnumeratedGroup</code> to an HQL condition
         * testing transitive membership of an element.
         * (
         * :gp1 in (
         *     ( select em1.groupId
         *       from EnumerationMember em1
         *       where em1.memberId=ee.originalId
         *         and em1.timeRelation.activeTo > :asOf
         *         and em1.timeRelation.activeFrom <= :asOf
         *     )
         *     UNION
         *     ( select xm1.fromID  from EnumerationGroupMember xm1, EnumerationMember em1
         *        where xm1.toId = em1.groupId
         *          AND xm1.timeRelation.activeTo > :asOf
         *          and xm1.timeRelation.activeFrom <= :asOf
         *          and em1.memberId=ee.originalId
         *          and em1.timeRelation.activeTo > :asOf
         *          and em1.timeRelation.activeFrom <= :asOf
         *     )
         *  )
         * )
         *
         * @param group the enumerated group to convert to HQL.
         */
        public void visitTransitiveMembership( EnumeratedGroup group ) {
            String memberAlias = "em"+getParameterCount();
            String crossAlias = "xm"+getParameterCount();
            put("(");
            addParameter(group.getOriginalId(), Hibernate.LONG);
            put(" in ( ( select ");
            put(memberAlias, ".groupId ");
            put(" from EnumerationMember ", memberAlias);
            put(" where ");
            put(memberAlias, ".memberId=", getAlias());
            put(".originalId and ", memberAlias, ".timeRelation.activeTo > :asOf and ");
            put(memberAlias, ".timeRelation.activeFrom <= :asOf");

            put(") UNION ( select ", crossAlias);
            put(".fromId from EnumerationGroupMember ", crossAlias);
            put(", EnumerationMember ", memberAlias);
            put(" where ", crossAlias);
            put(".toId = ", memberAlias, ".groupId AND ");
            put(crossAlias, ".timeRelation.activeTo > :asOf and ");
            put(crossAlias, ".timeRelation.activeFrom <= :asOf and ");
            put(memberAlias, ".memberId=", getAlias());
            put(".originalId and ", memberAlias, ".timeRelation.activeTo > :asOf and ");
            put(memberAlias, ".timeRelation.activeFrom <= :asOf)))");
        }
    }

    /**
     * This class formats HQL queries for enumerated groups.
     */
    private static class GroupHQLFormatVisitor extends AbstractHQLFormatVisitor {

        private IElementType type = null;

        protected GroupHQLFormatVisitor(String alias) {
            super( alias );
        }

        protected GroupHQLFormatVisitor(String alias, IElementType type ) {
            super( alias );
            this.type = type;
        }

        /**
         * @see AbstractHQLFormatVisitor#visitElementType(ElementType)
         */
        public void visitElementType( ElementType type ) {
            throw new IllegalArgumentException(
                "Conditions on groups may not refer to elememt type."
            );
        }

        /**
         * @see AbstractHQLFormatVisitor#visit(IAttribute)
         */
        public void visit(IAttribute attr) {
            if (attr instanceof ElementBaseAttribute) {
                super.visit( attr );
            } else {
                throw new IllegalArgumentException(
                    "Conditions on groups may reference only fields of "
                +   "the DictionaryElementBase."
                );
            }
        }

        /**
         * Converts an <code>EnumeratedGroup</code> to an HQL condition
         * testing direct membership of a group.
         *
         * @param group the enumerated group to convert to HQL.
         */
        public void visitDirectMembership( EnumeratedGroup group ) {
            formatGroupMembership(group, true);
        }

        /**
         * Converts an <code>EnumeratedGroup</code> to an HQL condition
         * testing transitive membership of a group.
         *
         * @param group the enumerated group to convert to HQL.
         */
        public void visitTransitiveMembership( EnumeratedGroup group ) {
            formatGroupMembership(group, false);
        }

        private void formatGroupMembership(EnumeratedGroup group, boolean onlyDirect) {
            String memberAlias = "gm"+getParameterCount();
            put("(");
            addParameter(group.getOriginalId(), Hibernate.LONG);
            put(" in ( select " + memberAlias + ".fromId from EnumerationGroupMember ", memberAlias, " where ");
            put(memberAlias, ".toId=");
            put(getAlias(), ".originalId and ");
            if (onlyDirect) {
                put(memberAlias, ".isDirect = 'Y' and ");
            }
            put(memberAlias, ".timeRelation.activeTo > :asOf and ", memberAlias);
            put(".timeRelation.activeFrom <= :asOf))");
        }

        /**
         * @see IDictionaryPredicateVisitor#visitChangedCondition(Date, Date)
         */
        public void visitChangedCondition( Date startDate, Date endDate ) {
            // See if this is a no-op
            if (startDate != null || endDate != null) {
                put("(");
                put(getAlias(), ".originalId in ( ( select b.originalId from DictionaryElementBase b where (");
                if (startDate != null) {
                    put(" b.timeRelation.activeFrom>=");
                    addParameter(startDate, DateToLongUserType.TYPE);
                }
                if (endDate != null) {
                    if ( startDate != null ) {
                        put(" and ");
                    }
                    put(" b.timeRelation.activeFrom<");
                    addParameter(endDate, DateToLongUserType.TYPE);
                }

                /* Groups has been deleted
                put(") OR (");
                if (startDate != null) {
                    put(" b.timeRelation.activeTo>=");
                    addParameter(startDate, DateToLongUserType.TYPE);
                }
                if (endDate != null) {
                    if ( startDate != null ) {
                        put(" and ");
                    }
                    put(" b.timeRelation.activeTo<");
                    addParameter(endDate, DateToLongUserType.TYPE);
                }
                */

                if ( (startDate != null ) ||  (endDate != null) || ( type != null) )  {
                    put( " )) UNION ( select em.groupId from EnumerationMember em where " );
                    if ( type != null ) {
                        put( " em.elementTypeId=:typeId ");
                    }
                    if ( (startDate != null ) ||  (endDate != null) ) {
                        if ( type != null ) {
                            put(" and ");
                        }
                        put ("(");
                        if ( startDate != null ) {
                            put("em.timeRelation.activeFrom>=");
                            addParameter(startDate, DateToLongUserType.TYPE);
                        }
                        if ( endDate != null ) {
                            if ( startDate != null ) {
                                put(" and ");
                            }
                            put("em.timeRelation.activeFrom<");
                            addParameter(endDate, DateToLongUserType.TYPE);
                        }
                        put(" ) OR (");
                        if ( startDate != null ) {
                            put("em.timeRelation.activeTo>=");
                            addParameter(startDate, DateToLongUserType.TYPE);
                        }
                        if ( endDate != null ) {
                            if ( startDate != null ) {
                                put(" and ");
                            }
                            put("em.timeRelation.activeTo<");
                            addParameter(endDate, DateToLongUserType.TYPE);
                        }
                    }
                }
                put("))))");

            } else {
                put("1=1");
            }
        }
    }

    /**
     * Get the latest consistent time in memory
     * @return the latest consistent time
     */
    public Date getLatestConsistentTime() throws DictionaryException {
        if ( this.latestConsistentTime == null ) {
            this.latestConsistentTime = fetchLatestConsistentTime();
            if ( this.latestConsistentTime == null ) {
                this.latestConsistentTime = UnmodifiableDate.START_OF_TIME;
            }
        }
        return this.latestConsistentTime;
    }

    /**
     * Fire dictionary change event.
     * This package-private method is for use by dictionary sessions.
     * @see IDictionary#fireDictionaryChangeEvent()
     */
    void fireDictionaryChangeEvent() {
        this.eventManager.fireRemoteDictionaryChangeEvent();
    }

    /**
     * Check whether dictionary has been changed by looking at the latest time
     * from database update records and the time from local memory
     *
     * @return boolean true if dictionary changed
     */
    boolean isDictionaryChanged() throws DictionaryException {
        boolean result = false;
        Date newLatestConsistantTime = fetchLatestConsistentTime();

        if ((newLatestConsistantTime == null)
                || (newLatestConsistantTime == UnmodifiableDate.START_OF_TIME)) {
            result = false;
        } else if (this.latestConsistentTime == null) {
            result = true;
        } else {
            result = newLatestConsistantTime.after(this.latestConsistentTime);
        }
        this.latestConsistentTime = newLatestConsistantTime;
        return result;
    }

    //TODO using <code>com.bluejungle.destiny.tools.dbinit.hibernateMod.dialect.DialectExtended</code>
    private boolean isMsSql() {
        IHibernateRepository.DbType dbType = this.hds.getDatabaseType();
        return dbType == IHibernateRepository.DbType.MS_SQL;
    }

    /**
     * set the dictionary in the enrollment then disconnect from hibernate session
     * @param hs
     * @param enrollment, null is accepted
     * @return <code>enrollment</code> 
     * @throws HibernateException if the <code>enrollment</code> can't be disconnected 
     */
    private IEnrollment reparentAndDisconnect(Session hs, IEnrollment enrollment)
        throws HibernateException {
        if (enrollment instanceof Enrollment) {
            Enrollment e = (Enrollment)enrollment;
            e.setDictionary(this);
            hs.evict(e);
        }
        return enrollment;
    }

    /**
     * Are there at least one failed enrollment attempts in the dict_updates database?
     * Special case: if the input domainName matches with the domain name of the failed enrollment, 
     * that enrollment is excluded from this query.
     * 
     * @param domainName Domain name to be excluded from query.  If this is null, no domain name is excluded from query.
     * @return true if there is a failed enrollment that is still active.  false if not.
     */
    public boolean isThereFailedEnrollment(final String domainName) throws DictionaryException {
        
        // join dict_updates and dict_enrollments.
        // Count the number of enrollments that are: 
        //   failed, active, and domain name doesn't match the input 
        StringBuilder hql = new StringBuilder();
        hql.append("select count (*)")
           .append(" from UpdateRecord ur, Enrollment e")
           .append(" where ur.enrollment = e.id")
           .append(  " and ur.successful = 'N'")
           .append(  " and ur.timeRelation.activeTo > :asOf");
        if (domainName != null) {
            hql.append(" and e.domainName != :domainName");
        }
        
        return new NonIterableWork<Boolean>(
                "isThereFailedEnrollment",
                "String",
                "Unable to check if there is any failed enrollments for domain " + domainName,
                hql.toString()
        ){
            @Override
            Boolean execute(Session hs, Query query) throws HibernateException {
                setAsOf(query, null);
                if (domainName != null) {
                    query.setParameter("domainName", domainName);
                }
                Integer count = (Integer) query.uniqueResult();
                return (count > 0);
            }
        }.run();
    }

    public IDictionaryIterator<DictionaryPath> queryReferenceMembersByGroupId(final long groupId)
            throws DictionaryException {
        //This doesn't need to 
        String hql = "select pm.path from EnumerationProvisionalMember pm where"
                +   " pm.groupId = :groupId";
        
        return new IterableWork<IDictionaryIterator<DictionaryPath>>(
                "queryReferenceMembersByGroupId",
                "long",
                "Unable to query a dictionarypath of a group " + groupId,
                hql
        ){
            @Override
            IDictionaryIterator<DictionaryPath> execute(Session hs, Transaction tx, Query query)
                    throws HibernateException {
                query.setParameter("groupId", groupId);
                return new DictionaryIterator<DictionaryPath>(query.scroll(), hs, tx, Dictionary.this);
            }
        }.run();
    }
    
    /**
     * the link between the enrollment are not configurable yet.
     * I always use unique name to link between enrollment.
     * This method provides the minimal functionality for KLA.
     * 
     * 
     * @param element
     * @throws DictionaryException
     */
    void concatenate(IMElementBase element) throws DictionaryException {
        // stubbed out. Never worked. Do proper removal later
    }
}

