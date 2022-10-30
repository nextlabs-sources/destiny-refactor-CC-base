/*
 * Created on Mar 19, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.RfcFilterEvaluator;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.patterns.EnumBase;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/common/BaseLDAPEnrollmentInitializer.java#1 $
 */

public abstract class BaseLDAPEnrollmentInitializer implements BasicLDAPEnrollmentProperties{
    
    protected static final String MATCH_ALL_LDAP_FILTER = "(objectclass=*)";
    
    private static final String INVALID_FORMAT_MESSAGE =
        "The value of the '%s' key is not in a correct %s format. The input value is '%s'.";

    
	private static final Map<String, ElementFieldType> DATA_TYPE_TO_FIELD_TYPE;
	static{
		DATA_TYPE_TO_FIELD_TYPE = new HashMap<String, ElementFieldType>();
		
		DATA_TYPE_TO_FIELD_TYPE.put(STRING_DATATYPE_TOKEN, 		ElementFieldType.STRING);
		DATA_TYPE_TO_FIELD_TYPE.put(CSSTRING_DATATYPE_TOKEN, 	ElementFieldType.CS_STRING);
		DATA_TYPE_TO_FIELD_TYPE.put(MULTISTRING_DATATYPE_TOKEN, ElementFieldType.STRING_ARRAY);
		DATA_TYPE_TO_FIELD_TYPE.put(NUMBER_DATATYPE_TOKEN, 		ElementFieldType.NUMBER);
		DATA_TYPE_TO_FIELD_TYPE.put(DATE_DATATYPE_TOKEN, 		ElementFieldType.DATE);
	}
	
	/*
	 * a map to define what is the required mapping for each LeafElementType.
	 * the value is also a map because the user can optionally map different ad field to dictionary field.
	 */
	private static final Map<ElementTypeEnumType, EnumBase[][]> REQUIRE_FIELDS_MAP;
	static{
		REQUIRE_FIELDS_MAP = new HashMap<ElementTypeEnumType, EnumBase[][]>();
		EnumBase[][] userRequireField = new EnumBase[][] {
				{UserReservedFieldEnumType.PRINCIPAL_NAME},
				//either windows sid or unix id
				{UserReservedFieldEnumType.WINDOWS_SID, UserReservedFieldEnumType.UNIX_ID}
		};
		REQUIRE_FIELDS_MAP.put(ElementTypeEnumType.USER, userRequireField);
		
		EnumBase[][] contactRequireField = new EnumBase[][] {
				{ContactReservedFieldEnumType.PRINCIPAL_NAME},
		};
		REQUIRE_FIELDS_MAP.put(ElementTypeEnumType.CONTACT, contactRequireField);
		
		
		EnumBase[][] computerRequireField = new EnumBase[][] {
				{ComputerReservedFieldEnumType.DNS_NAME},
				//either windows sid or unix id
				{ComputerReservedFieldEnumType.WINDOWS_SID, ComputerReservedFieldEnumType.UNIX_ID},
		};
		REQUIRE_FIELDS_MAP.put(ElementTypeEnumType.COMPUTER, computerRequireField);
		
		
		EnumBase[][] appRequireField = new EnumBase[][] {
				{ApplicationReservedFieldEnumType.UNIQUE_NAME},
		};
		REQUIRE_FIELDS_MAP.put(ElementTypeEnumType.APPLICATION, appRequireField);
    }
	
	protected final Log log;
	protected final IEnrollment enrollment;
	protected final Map<String, String[]> properties;
	protected final IDictionary dictionary;
	

    public BaseLDAPEnrollmentInitializer(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary, Log log) {
        super();
        this.enrollment = enrollment;
        this.properties = properties;
        this.dictionary = dictionary;
        this.log = log;
    }

    /**
    *
    * @param enrollment
    * @param properties
    * @throws EnrollmentValidationException
    * @throws DictionaryException
    */
	public void setup() throws EnrollmentValidationException, DictionaryException {
		nullCheck(properties, "properties");
		
		setStringProperty(STATIC_ID_ATTRIBUTE);

        boolean enrollUsers = setBooleanProperty(ENROLL_USERS, true);
        if (enrollUsers) {
            setLDAPProperty(USER_REQUIREMENTS);
            processExternalMapping(ElementTypeEnumType.USER, USER_SEARCHABLE_PREFIX);
        }

        boolean enrollContacts = setBooleanProperty(ENROLL_CONTACTS, true);
        if (enrollContacts) {
            setLDAPProperty(CONTACT_REQUIREMENTS);
            processExternalMapping(ElementTypeEnumType.CONTACT, CONTACT_SEARCHABLE_PREFIX);
        }

        boolean enrollComputers = setBooleanProperty(ENROLL_COMPUTERS, true);
        if (enrollComputers) {
            setLDAPProperty(COMPUTER_REQUIREMENTS);
            processExternalMapping(ElementTypeEnumType.COMPUTER, COMPUTER_SEARCHABLE_PREFIX);
        }

        boolean enrollApplications = setBooleanProperty(ENROLL_APPLICATIONS, false);
        if (enrollApplications) {
            setLDAPProperty(APPLICATION_REQUIREMENTS);
            processExternalMapping(ElementTypeEnumType.APPLICATION, APPLICATION_SEARCHABLE_PREFIX);
        }

        boolean enrollGroups = setBooleanProperty(ENROLL_GROUPS, true);
        if (enrollGroups) {
            setLDAPProperty(GROUP_REQUIREMENTS);
            setStringProperty(GROUP_ENUMERATION_ATTRIBUTE);
        }

        setBooleanProperty(STORE_MISSING_ATTRIBUTES, false);

        setLDAPProperty(STRUCTURAL_GROUP_REQUIREMNTS, null);

        setLDAPProperty(OTHER_REQUIREMNTS, null);

		setupAutoEnrollment();
		
		processFieldFormatter();
	}

    private void processExternalMapping(ElementTypeEnumType elementType, 
                                        String searchablePrefix) throws EnrollmentValidationException, DictionaryException {
		IElementType userType;
		userType = dictionary.getType(elementType.getName());
		
		for (Map.Entry<String, ElementFieldType> e : DATA_TYPE_TO_FIELD_TYPE.entrySet()) {
			addExternalSearchableAttributeMappings(userType, e.getValue(),
					searchablePrefix + e.getKey());
		}
		
		EnumBase[][] requiredFields = REQUIRE_FIELDS_MAP.get(elementType);
		for (EnumBase[] fields : requiredFields) {
			boolean meetRequirement = false;
			for (EnumBase f : fields) {
				IElementField elementField = userType.getField(f.getName());
				if (enrollment.getExternalName(elementField) != null) {
					meetRequirement = true;
					break;
				}
			}
			if (!meetRequirement) {
				throw new EnrollmentValidationException(String.format(
				        "The required %s attribute mapping, %s, %s missing. Please supply a value.", 
								elementType.getName(), 
								ArrayUtils.asString(fields, " or "),
								fields.length > 1 ? "are" : "is"
				));
			}
		}
	}
	
    private void processFieldFormatter() throws EnrollmentValidationException, DictionaryException {
        for (String property : properties.keySet()) {
            if (!property.endsWith(FORMATTER_SUFFIX)) {
                continue;
            }
            String value = getSingleValuedPropertyValue(property, false, null, false);
            enrollment.setStrProperty(property, value);
        }
    }
	

	/**
	  * @param enrollment
	  * @param dictionary
	  * @param properties
	  * @param keys
	  * @throws DictionaryException
	  * @throws EnrollmentValidationException
	  */
	protected void addExternalSearchableAttributeMappings(
			IElementType elementType, 
			ElementFieldType fieldType,
			String propertyPrefix
	) throws EnrollmentValidationException {
		for (String property : properties.keySet()) {
			if (!property.startsWith(propertyPrefix) || property.endsWith(FORMATTER_SUFFIX)) {
				continue;
			}
			final String logicalName = property.substring(propertyPrefix.length()).trim();
			final IElementField field;
			try {
				field = elementType.getField(logicalName);
			} catch (IllegalArgumentException e) {
				throw new EnrollmentValidationException("The logical field name '" + logicalName
                        + "' is not known. Each external field must map to a known logical field.");
			}
			if (field == null) {
				throw new EnrollmentValidationException(
				        "The logical field name '" + logicalName
                        + "' is missing. Each external field must map to a known logical field.");
			}
			if (field.getType() != fieldType) {
				throw new EnrollmentValidationException(
						"The type of logical field '"
								+ logicalName + "' is not match. Expected '"
								+ field.getType().getName() + "' but was '"
								+ fieldType.getName() + "'.");
			}
			
			String externalName = getSingleValuedPropertyValue(property, true, null, false);
			if (externalName == null || externalName.equals("")) {
                throw new EnrollmentValidationException("An external name for the logical field '"
                        + logicalName + "' is not specified. Please supply a value.");
            }
			externalName = externalName.trim();
			enrollment.setExternalName(field, externalName);
			enrollment.setStrProperty(property, externalName);
			
		}
	}

	/**
	 * return the first date of the auto sync
	 * @param enrollment
	 * @param properties
	 * @return
	 * @throws EnrollmentValidationException
	 */
	protected void setupAutoEnrollment() throws EnrollmentValidationException {
		int interval = (int)setLongProperty(PULL_INTERVAL, 0);
		enrollment.setNumProperty(PULL_INTERVAL, interval);
		if (interval <= 0) {
			enrollment.setIsRecurring(false);
			return;
		}
		String startTimeStr = getSingleValuedPropertyValue(
				BasicLDAPEnrollmentProperties.START_TIME, false, null, true);
		String timeFormat = getSingleValuedPropertyValue(
				BasicLDAPEnrollmentProperties.TIME_FORMAT,false,null,true);	
		// Parse the date
		nullCheck(startTimeStr, BasicLDAPEnrollmentProperties.START_TIME);
		
		DateFormat dateFormat;	
		if( null != timeFormat && !"".equals(timeFormat.trim())){
			dateFormat = new SimpleDateFormat(timeFormat);
		} else {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		}

		Date startDate;
		try {
			startDate = dateFormat.parse(startTimeStr.trim());
		} catch (ParseException e) {
            throw new EnrollmentValidationException("The autosync start time, '" + startTimeStr
                    + "', has an invalid format. Valid format example: "
                    + dateFormat.format(new Date()));
		}
		
		if (startDate.before(new Date())) {
			throw new EnrollmentValidationException(String.format(
                    "The specified autosync start time, '%tc', has already passed.", startDate));
		}
		
		log.info("auto sync will start at " + startDate);
		
		enrollment.setNumProperty(START_TIME, startDate.getTime());
		enrollment.setIsRecurring(true);
	}

	public void nullCheck(Object o, String name) throws EnrollmentValidationException {
		if (o == null) {
			throw new NullPointerException(name + " is null");
		}
	}
	
    protected File setReadableFileStringProperty(String key) throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, true, null);
        File file = new File(value);
        if (!file.exists()) {
            //TODO_OJA
            throw new EnrollmentValidationException("The file, " + file.getAbsolutePath()
                    + ", does not exist.");
        }
        
        if (!file.canRead()) {
            //TODO_OJA
            throw new EnrollmentValidationException("The file, " + file.getAbsolutePath()
                    + ", can not be read.");
        }
        enrollment.setStrProperty(key, value);
        return file;
    }
	
	/**
	 * the key must exist in the properties
	 * 
	 * @param enrollmentToSetup
	 * @param properties
	 * @param key
	 * @return
	 * @throws EnrollmentValidationException
	 */
	protected String setStringProperty(String key) throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, true, null);
        enrollment.setStrProperty(key, value);
        return value;
    }
	
	/**
	 * the key may exist in the properties, if it doesn't, use defaultValue
	 * @param enrollmentToSetup
	 * @param properties
	 * @param key
	 * @param defaultValue
	 * @return
	 * @throws EnrollmentValidationException
	 */
	protected String setStringProperty(String key, String defaultValue)
            throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, false, defaultValue);
        enrollment.setStrProperty(key, value);
        return value;
    }
	
    protected String setLDAPProperty(String key)
            throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, true, null);
        if (!RfcFilterEvaluator.isValid(value)) {
            throw new EnrollmentValidationException(String.format(INVALID_FORMAT_MESSAGE, key,
                    "LDAP", value));
        }
        enrollment.setStrProperty(key, value);
        return value;
    }
	
	protected String setLDAPProperty(String key, String defaultValue)
            throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, false, defaultValue);
        if (!RfcFilterEvaluator.isValid(value)) {
            throw new EnrollmentValidationException(String.format(INVALID_FORMAT_MESSAGE, key, 
                    "LDAP", value));
        }
        enrollment.setStrProperty(key, value);
        return value;
    }
	
	protected boolean setBooleanProperty(String key) throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, true, null);
        return setBooleanProperty(key, value);
    }

    protected boolean setBooleanProperty(String key, boolean defaultValue)
            throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, false, Boolean.toString(defaultValue));
        return setBooleanProperty(key, value);
    }
	
	private boolean setBooleanProperty(String key, String value)
            throws EnrollmentValidationException {
        Boolean b = StringUtils.stringToBoolean(value);
        if (b == null) {
            throw new EnrollmentValidationException(String.format(INVALID_FORMAT_MESSAGE, key,
                    "Boolean", value));
        }
        enrollment.setStrProperty(key, Boolean.toString(b));
        return b;
    }
	
	
	protected long setLongProperty(String key) throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, true, null);
        return setLongProperty(key, value);
    }

    protected long setLongProperty(String key, long defaultValue)
            throws EnrollmentValidationException {
        String value = getSingleValuedPropertyValue(key, false, Long.toString(defaultValue));
        return setLongProperty(key, value);
    }
	
	private long setLongProperty(String key, String value)
			throws EnrollmentValidationException {
		long i;
		try {
			i = Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new EnrollmentValidationException(String.format(INVALID_FORMAT_MESSAGE, key,
					"Long", value));
		}
		enrollment.setNumProperty(key, i);
		return i;
	}
	
	private String getSingleValuedPropertyValue(
			String key, boolean mustExist, String defaultValue)
			throws EnrollmentValidationException {
		return getSingleValuedPropertyValue(key, mustExist, defaultValue, true);
	}

	/**
	  * Utility method to return a single value for a property
	  *
	  * @param properties
	  * @param key
	  * @param mustExist
	  * @param defaultValue
	  * @return
	  * @throws EnrollmentValidationException
	  */
	protected String getSingleValuedPropertyValue(
			String key, boolean mustExist, String defaultValue, boolean removeAfterRead)
			throws EnrollmentValidationException {
		nullCheck(properties, "properties");
		nullCheck(key, "key");

		String[] strs = properties.get(key);
		if (strs == null) {
			if (mustExist) {
				throw new EnrollmentValidationException("'" + key + "' property is required.");
			}
			return defaultValue;
		}
		
		if( strs.length != 1){
			throw new EnrollmentValidationException("'" + key
					+ "' property is not configured properly: '" + properties.get(key)
					+ "'");
		}
		
		if (removeAfterRead) {
			properties.remove(key);
		}
		
		return strs[0];
	}
}
