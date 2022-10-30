/*
 * Created on Mar 25, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.ElementCreator;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.ElementFieldData;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.dictionary.Order;
import com.bluejungle.dictionary.Page;
import com.bluejungle.domain.enrollment.ClientInfoFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.CollectionUtils;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/ClientInfoEnrollment.java#1 $
 */

public class ClientInfoEnrollment implements Closeable {
	private static final String DOMAIN_WILDCARD_WARNING = "\"%s\" is not supported " + ClientInfoFieldEnumType.EMAIL_TEMPLATES_LABEL + ". Wildcards '*' may be placed only at the beginning of the name.";
	private static final String POSITIVE_NUMBER_EXCEPTION = "%s must be a positive number.";
	private static final String ATTRIBUTE_MISSING_EXCEPTION = "\"%1$s\" is not present from \"%2$s\".";
	private static final String UNKNOWN_USER_EXCEPTION = "%s is not an enrolled user.";
	private static final String MISSING_CLOSE_TAG_EXCEPTION = "\"%s\" doesn't open correctly. Missing close tag.";
	private static final String UNEXPECTED_OPEN_TAG_EXCEPTION = "\"%1$s\" is not open in the \"%2$s\".";
	private static final String UNKNOWN_TAG_EXCEPTION = "Unknown tag: \"%s\"";
	private static final String INVALID_VALUE_EXCEPTION = "\"%1$s\" is not a valid value for \"%2$s\"";
	private static final String EMPTY_VALUE_EXCEPTION = "An empty string is not a valid value for \"%s\"";
	private static final String DOMAIN_NAME_TOO_LONG_WARNING = "Client (id=\"%s\") has total length more than %d, the data is truncated.";
	
	private static final int DICTIONARY_FIELD_MAX_LENGTH = 255;
	
	private static final Log LOG = LogFactory.getLog(ClientInfoEnrollment.class);

	private final XMLReader xmlReader;
	private final Reader reader;

	/**
	 * cache of uniqueName (key) to internal id (value)
	 * Case insensitive cache
	 */
	private Map<String, Long> dictoinaryUnqiueNameToInternalKeyCache;

	private final IDictionary dictionary;
	private final IEnrollmentSession session;
	private final IEnrollment enrollment;

	private final IAttribute uniqueNameAttribute;
	private final Date latestConsistentTime;
	private final String enrollmentDomainName;
	private final IMElementType clientInfoType;
	private final IMElementType userType;
	//	private final String clientInfoPrefix;

	private int totalCount;

	private final int clientFetchSize;
	private final int userFetchSize;
	
	private final boolean preCacheAllUsers;

	private Set<String> allNewUserNames;

	private Set<String> unknownUserNames;
	private Set<String> invalidForamtDomainNames;
	
	private int numOfTooLongDomains;

	private int cacheHit;
	private int cacheMiss;

	/**
	 * 
	 * Constructor
	 * @param clientFetchSize
	 * @param enrollment
	 * @param dictionary
	 * @throws EnrollmentFailedException
	 */
	public ClientInfoEnrollment(IEnrollmentSession session, IDictionary dictionary,
			IEnrollment enrollment, boolean preCacheAllUsers, int clientFetchSize, int userFetchSize)
			throws EnrollmentValidationException, DictionaryException {
		this.dictionary = dictionary;
		this.enrollment = enrollment;
		this.clientFetchSize = clientFetchSize;
		if (clientFetchSize <= 0) {
			throw new EnrollmentValidationException(String.format(POSITIVE_NUMBER_EXCEPTION,
					"clientFetchSize"));
		}

		if (!preCacheAllUsers && userFetchSize <= 0) {
			throw new EnrollmentValidationException(String.format(POSITIVE_NUMBER_EXCEPTION,
					"userFetchSize"));
		}
		
		this.userFetchSize = userFetchSize;
		
		this.preCacheAllUsers = preCacheAllUsers;
		this.session = session;

		uniqueNameAttribute = dictionary.uniqueNameAttribute();
		latestConsistentTime = dictionary.getLatestConsistentTime();
		clientInfoType = dictionary.getType(ElementTypeEnumType.CLIENT_INFO.getName());
		userType = dictionary.getType(ElementTypeEnumType.USER.getName());

		enrollmentDomainName = enrollment.getDomainName();

		numOfTooLongDomains = 0;

		totalCount = 0;
		cacheHit = 0;
		cacheMiss = 0;

		dictoinaryUnqiueNameToInternalKeyCache = new HashMap<String, Long>();

		allNewUserNames = new HashSet<String>();
		unknownUserNames = new HashSet<String>();
		invalidForamtDomainNames = new HashSet<String>();

		reader = initReader();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			// Create a JAXP SAXParser
			SAXParser saxParser = spf.newSAXParser();
			// Get the encapsulated SAX XMLReader
			xmlReader = saxParser.getXMLReader();
		} catch (ParserConfigurationException e) {
			throw new EnrollmentValidationException(e);
		} catch (SAXException e) {
			throw new EnrollmentValidationException(e);
		}
	}
	
	protected Reader initReader() throws EnrollmentValidationException {
		//setup the xml reader
		String clientInfoFilePath = enrollment
				.getStrProperty(ClientInfoEnroller.CLIENT_INFO_FILE_KEY);
		try {
			return new FileReader(clientInfoFilePath);
		} catch (FileNotFoundException e) {
			throw new EnrollmentValidationException(e);
		}
	}

	public void startEnrollment() throws EnrollmentSyncException {

		if (preCacheAllUsers) {
			//pre-cache
			long startTime = System.currentTimeMillis();
			
			String lastEntry = null;
			
			//query all users
            try {
                IDictionaryIterator<IMElement> userIter = dictionary.query(dictionary.condition(userType),
                		latestConsistentTime, null, null);
                
              //put all of the users into the cache
                try {
                    while (userIter.hasNext()) {
                        IMElement elementFieldData = userIter.next();
                        lastEntry = elementFieldData.getUniqueName();
                        dictoinaryUnqiueNameToInternalKeyCache.put(elementFieldData.getUniqueName(),
                                elementFieldData.getInternalKey());
                    }
                } finally {
                    if (userIter != null) {
                        userIter.close();
                    }
                }
            } catch (DictionaryException e) {
                throw new EnrollmentSyncException(e, lastEntry);
            }
			
			LOG.debug("pre-cached " + dictoinaryUnqiueNameToInternalKeyCache.size() + " users in "
					+ (System.currentTimeMillis() - startTime) / 1000 + " seconds.");
		}

		//start the xmlReader
		xmlReader.setContentHandler(new ClientInfoSaxParser());
		try {
			// although xmlReader.parse() only throws SAXException
			// however, since we also save elements to a dictionary while parsing the xml
			// the parse may throws DictionaryException but wrapped by the SAXException
			xmlReader.parse(new InputSource(reader));
		} catch (SAXException e) {
			throw new EnrollmentSyncException(e);
		} catch (IOException e) {
		    throw new EnrollmentSyncException(e);
		} finally {
			LOG.debug("Totally loaded elements: " + totalCount + ", cache hit rate: " + cacheHit
					+ "/" + cacheMiss);
		}
	}

	protected void save(Collection<IElementBase> elements) throws DictionaryException {
		session.beginTransaction();
		session.saveElements(elements);
		session.commit();
		LOG.debug("Saved elements:" + elements.size());
		elements.clear();
	}

	private class ClientData {
		String shortName;
		String identifer;
		String longName;
		//using <code>Set</code> to ensure there are no duplicated names
		Set<String> domainNames;
		Set<String> userNames;

		public ClientData(String identifer, String shortName, String longName) {
			this.identifer = identifer;
			this.shortName = shortName;
			this.longName = longName;
			domainNames = new HashSet<String>();
			userNames = new HashSet<String>();
		}

		public void addDomain(String domainName) {
			domainNames.add(domainName);
		}

		public void addUser(String userName) {
			userNames.add(userName);
		}
	}

	private class ClientInfoSaxParser extends DefaultHandler implements ClientInfoTag {

		private ClientData temp;

		private List<ClientData> allClientData;

		private ClientInfoSaxParser() {
			super();
			temp = null;
			allClientData = new LinkedList<ClientData>();
		}

		@Override
		@SuppressWarnings("unused")
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (TAG_CLIENT.equalsIgnoreCase(qName)) {
				if (temp != null) {
					throw new SAXException(String.format(MISSING_CLOSE_TAG_EXCEPTION, TAG_CLIENT));
				}
				String id = getAttributeValue(qName, attributes, ATTR_IDENTIFIER);
				if (id.trim().length() == 0) {
					throw new SAXException(String.format(EMPTY_VALUE_EXCEPTION, ATTR_IDENTIFIER));
				}
				String shortName = getAttributeValue(qName, attributes, ATTR_SHORT_NAME);
				String longName = getAttributeValue(qName, attributes, ATTR_LONG_NAME);
				temp = new ClientData(id, shortName, longName);
			} else if (TAG_EMAIL_TEMPLATE.equalsIgnoreCase(qName)) {
				if (temp == null) {
					throw new SAXException(String.format(UNEXPECTED_OPEN_TAG_EXCEPTION, TAG_EMAIL_TEMPLATE,
							TAG_CLIENT));
				}
				String domain = formatDomainAndUserNameAttr(qName, attributes, ATTR_EMAIL_TEMPLATE_NAME);
				
				//domain names are case-insensitive
				// store as lowercase as instructed by Sergey
				domain = domain.toLowerCase();
				
				//check wild card
				//log warning, if the name is not already logged,
				//  and contains double ** or * but not in the beginning.
				if (!invalidForamtDomainNames.contains(domain)) {
					if (!Pattern.matches("(\\w|\\*|\\.|-|@|_)*", domain)) {
						LOG.warn(String.format(INVALID_VALUE_EXCEPTION, domain, TAG_EMAIL_TEMPLATE));
						invalidForamtDomainNames.add(domain);
					} else if (domain.contains("**")
							|| (domain.lastIndexOf("*") > 0 && domain.length() > 1)) {
						LOG.warn(String.format(DOMAIN_WILDCARD_WARNING, domain));
						invalidForamtDomainNames.add(domain);
					}
				}
				
				temp.addDomain(domain);
			} else if (TAG_USER.equalsIgnoreCase(qName)) {
				if (temp == null) {
					throw new SAXException(String.format(UNEXPECTED_OPEN_TAG_EXCEPTION, TAG_USER,
							TAG_CLIENT));
				}
				String userName = formatDomainAndUserNameAttr(qName, attributes, ATTR_EMAIL_TEMPLATE_NAME);
				temp.addUser(userName);
				
				// If the user name is not in the cache, put it into the newUserNames list. 
				// We will query all the users not in the cache when flushing 
				if (!preCacheAllUsers 
						&& !dictoinaryUnqiueNameToInternalKeyCache.containsKey(userName)
						&& !unknownUserNames.contains(userName)) {
					allNewUserNames.add(userName);
					cacheMiss++;
				} else {
					cacheHit++;
				}
			} else if (TAG_CLIENT_INFORMATION.equalsIgnoreCase(qName)) {
				//do nothing
			} else {
				throw new SAXException(String.format(UNKNOWN_TAG_EXCEPTION, qName));
			}
		}

		private String formatDomainAndUserNameAttr(String qName, Attributes attributes,
				String attribute) throws SAXException {
			String value = getAttributeValue(qName, attributes, attribute);
			int orginalValueLength = value.length();

			// TODO more checking here, throw SAXException if the format is not accepted
			
			value = value.trim();
			if(orginalValueLength > 0 && value.length() ==0 ){
				value = " ";
			}
			return value;
		}

		private String getAttributeValue(String qName, Attributes attributes, String attribute)
				throws SAXException {
			String v = attributes.getValue(attribute);
			if (v == null) {
				throw new SAXException(String.format(ATTRIBUTE_MISSING_EXCEPTION, attribute, qName));
			}
			return v;
		}

		@Override
		@SuppressWarnings("unused")
		public void endElement(String uri, String name, String qName) throws SAXException {
			if (TAG_CLIENT.equalsIgnoreCase(qName)) {
				if (temp == null) {
					throw new SAXException(String.format(MISSING_CLOSE_TAG_EXCEPTION, TAG_CLIENT));
				}

				allClientData.add(temp);
				temp = null;

				totalCount++;
				if (allClientData.size() >= clientFetchSize) {
					try {
						flush(allClientData);
					} catch (DictionaryException e) {
						throw new SAXException(e);
					}
				}
			}
		}

		@Override
		public void endDocument() throws SAXException {
			if(allClientData.size() > 0 ){
				try {
					flush(allClientData);
				} catch (DictionaryException e) {
					throw new SAXException(e);
				}
			}
		}
	}

	private void flush(List<ClientData> clientDatas) throws DictionaryException {
		//build queryFields, get a list of long(internal key) of all user
		LOG.debug("flushing " + clientDatas.size() + " clients with " + allNewUserNames.size()
				+ " new users.");

		//query all the user names that are not in the cache.
		// If all the users are pre-cached, there should be not point to look at the database again.
		if (!preCacheAllUsers && allNewUserNames.size() > 0) {
			long startTime = System.currentTimeMillis();
			int i = 0;
			List<IPredicate> queryConstants = new LinkedList<IPredicate>();
			for (String userName : allNewUserNames) {

				queryConstants.add(new Relation(RelationOp.EQUALS, uniqueNameAttribute, Constant
						.build(userName)));

				if (++i >= userFetchSize) {
					updateIdCache(dictoinaryUnqiueNameToInternalKeyCache, queryConstants);
					i = 0;
				}
			}
			updateIdCache(dictoinaryUnqiueNameToInternalKeyCache, queryConstants);

			LOG.debug("searched " + allNewUserNames.size() + " users took "
					+ (System.currentTimeMillis() - startTime) / 1000 + "s");
		}

		allNewUserNames.clear();

		List<IElementBase> clientInfoElementToSave = createClientsToSave(clientDatas);

		save(clientInfoElementToSave);

		clientDatas.clear();
	}

	private List<IElementBase> createClientsToSave(List<ClientData> clientDatas) {
		List<IElementBase> clientInfoElementToSave = new ArrayList<IElementBase>(clientDatas.size());

		for (ClientData clientData : clientDatas) {
			DictionaryPath path = new DictionaryPath(new String[] { enrollmentDomainName, "client",
					clientData.identifer });
			DictionaryKey key = new DictionaryKey((enrollmentDomainName + clientData.identifer).getBytes());

			List<Long> userInternalIds = new ArrayList<Long>(clientData.userNames.size());

			for (String username : clientData.userNames) {
				Long id = dictoinaryUnqiueNameToInternalKeyCache.get(username);
				if (id == null) {
					if (!unknownUserNames.contains(username)) {
						LOG.warn(String.format(UNKNOWN_USER_EXCEPTION, username));
						unknownUserNames.add(username);
					}
				} else {
					userInternalIds.add(id);
				}
			}
			
			//if the domain names is longer than 255 chars, the dictionary table can't handle it
			//Sergey suggests that display a warn with Client id and truncate the domain names			
			int remainingLength = DICTIONARY_FIELD_MAX_LENGTH - 1;
			boolean isWarned = false;
			List<String> formatedDomains = new ArrayList<String>();
			
			//if the size is 1ess than one, the separator, ':' won't be insert
			final int seperatorLength = clientData.domainNames.size() <= 1 ? 0 : 1 ;
			for (String domainName : clientData.domainNames) {
				//domain name would not contains ":" or "//" 
				// because they are not valid chars in the domain name spec
				// @see StringArrayAsString
				
				int domainNameLength = domainName.length() + seperatorLength;
				
				if ( domainNameLength > remainingLength && !isWarned) {
					LOG.warn(String.format(DOMAIN_NAME_TOO_LONG_WARNING, clientData.identifer,
							DICTIONARY_FIELD_MAX_LENGTH));
					isWarned = true;
					numOfTooLongDomains++;
					domainName = domainName.substring(0, remainingLength);
				}

				if (remainingLength > 0) {
					formatedDomains.add(domainName);
					remainingLength -= domainNameLength;
				}
			}

			IMElement clientInfoElement = enrollment.makeNewElement(path, clientInfoType, key);
			clientInfoElement.setValue(clientInfoType.getField(ClientInfoFieldEnumType.IDENTIFIER
					.getName()), clientData.identifer);
			clientInfoElement.setValue(clientInfoType.getField(ClientInfoFieldEnumType.SHORT_NAME
					.getName()), clientData.shortName);
			clientInfoElement.setValue(clientInfoType.getField(ClientInfoFieldEnumType.LONG_NAME
					.getName()), clientData.longName);
			clientInfoElement.setValue(clientInfoType.getField(ClientInfoFieldEnumType.EMAIL_TEMPLATES
					.getName()), formatedDomains.toArray(new String[] {}));
			clientInfoElement.setValue(clientInfoType.getField(ClientInfoFieldEnumType.USER_NAMES
					.getName()), CollectionUtils.toLong(userInternalIds));

			clientInfoElement.setDisplayName(path.toString());
			clientInfoElement.setUniqueName(clientData.shortName);
			clientInfoElementToSave.add(clientInfoElement);
		}
		return clientInfoElementToSave;
	}

	private void updateIdCache(Map<String, Long> cache, List<IPredicate> queryConstants)
			throws DictionaryException {
		if (queryConstants.size() == 0) {
			return;
		}

		//update cache
		IElementField[] fields = new IElementField[] {};
		IPredicate condition = new CompositePredicate(BooleanOp.OR, queryConstants);
		Date asOf = latestConsistentTime;
		Order[] order = null;
		Page page = null;
		IDictionaryIterator<ElementFieldData> fieldsIterator = dictionary.queryFields(fields, condition, asOf, order,
				page);

		//put the uniqueName and internalKey into the cache
		try {
			while (fieldsIterator.hasNext()) {
				ElementFieldData elementFieldData = fieldsIterator.next();
				cache.put(elementFieldData.getUniqueName().toLowerCase(), elementFieldData
						.getInternalKey());
			}
		} finally {
			if (fieldsIterator != null) {
				fieldsIterator.close();
			}
		}

		queryConstants.clear();
	}

	/**
	 * @see java.io.Closeable#close()
	 */
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.warn(e);
			}
		}

		if (dictoinaryUnqiueNameToInternalKeyCache != null) {
			dictoinaryUnqiueNameToInternalKeyCache.clear();

			//very c style, but this will help the gc
			dictoinaryUnqiueNameToInternalKeyCache = null;
		}

		if (unknownUserNames != null) {
			unknownUserNames.clear();
			unknownUserNames = null;
		}

		if (allNewUserNames != null) {
			allNewUserNames.clear();
			allNewUserNames = null;
		}

		if (invalidForamtDomainNames != null) {
			invalidForamtDomainNames.clear();
			invalidForamtDomainNames = null;
		}
	}

	/**
	 * return warning message or null if there is not warning
	 * @return
	 */
	public String getWarningMessage(){
		StringBuilder sb = new StringBuilder();
		if (unknownUserNames.size() > 0){
			sb.append(unknownUserNames.size() + " unenrolled user(s)." );
		}
		if(invalidForamtDomainNames.size() > 0) {
			if(sb.length() != 0){
				sb.append(" And ");
			}
			sb.append(invalidForamtDomainNames.size() + " unsupported format "
					+ ClientInfoFieldEnumType.EMAIL_TEMPLATES_LABEL + "(s).");
		}
		
		if(numOfTooLongDomains > 0){
			if(sb.length() != 0){
				sb.append(" And ");
			}
			sb.append(numOfTooLongDomains + " client(s) excessed the max length of "
					+ ClientInfoFieldEnumType.EMAIL_TEMPLATES_LABEL + ".");
		}
		
		return sb.length() == 0 ? null : sb.toString();
	}

	int getTotalCount() {
		return totalCount;
	}

	int getCacheHit() {
		return cacheHit;
	}

	int getCacheMiss() {
		return cacheMiss;
	}
}
