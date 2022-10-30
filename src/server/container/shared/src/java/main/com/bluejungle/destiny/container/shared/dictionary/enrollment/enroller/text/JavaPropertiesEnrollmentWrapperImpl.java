/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.SiteReservedFieldEnumType;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/text/JavaPropertiesEnrollmentWrapperImpl.java#1 $
 */

public class JavaPropertiesEnrollmentWrapperImpl extends BaseLDAPEnrollmentWrapper {

    private static final Log LOG = LogFactory.getLog(JavaPropertiesEnrollmentWrapperImpl.class.getName());

    protected final IElementType SITE_TYPE;
    
    /** LDIF file name */
    private final String siteFileName;
    
    private final int fetchSize;
    
    protected boolean endOfFile = false;
    
    
    public JavaPropertiesEnrollmentWrapperImpl(IEnrollment enrollment, IDictionary dictionary,
            int fetchSize) throws EnrollmentValidationException, DictionaryException {
        super(enrollment, dictionary);
        this.fetchSize = fetchSize;
        siteFileName = enrollment.getStrProperty(JavaPropertiesEnrollmentProperties.SITE_FILE_NAME_PROPERTY);
        if ( (siteFileName == null) || ( siteFileName.length() == 0 ) ) {
            throw new EnrollmentValidationException("Ldif file upload path is not provided");
        }

		SITE_TYPE = dictionary.getType(ElementTypeEnumType.SITE.getName());

		File siteFile = new File(siteFileName);
		if (!siteFile.exists()) {
		    //TODO_OJA
            throw new EnrollmentValidationException("Java properties file, "
                    + siteFile.getAbsolutePath() + ", does not exist.");
        }
		if (!siteFile.isFile()) {
		    //TODO_OJA
		    throw new EnrollmentValidationException("Java properties file, "
                    + siteFile.getAbsolutePath() + ", is not a file.");
        }
		if (!siteFile.canRead()) {
		    //TODO_OJA
            throw new EnrollmentValidationException("Java properties file, "
                    + siteFile.getAbsolutePath() + ", can not be read.");
        }
    }

    /**
     * fetchElements() reads Java Properties file, fetch the entries from LDIF file and convert them to elements
     * @param elements
     * @param deletes
     * @return size of elements fetched, including the elements to delete
     * @throws EnrollmentFailedException
     */
    public int fetchElements(Map<DictionaryPath, IElementBase> elements,
            Collection<IElementBase> deletes) throws EnrollmentSyncException {
        Properties textReader = new Properties();
        try {
            textReader.load(new FileInputStream(this.siteFileName));
        } catch (IOException e) {
            throw new EnrollmentSyncException(e);
        }
        
        return fetchContentElements(elements, textReader);
    }

    /**
     * fetchContentElements() fetches element from a content LDIF file
     * @param elements collection of elements fetched
     * @return size of elements fetched
     * @throws LDAPException
     * @throws IOException
     * @throws EnrollmentFailedException
     */
    private int fetchContentElements(Map<DictionaryPath, IElementBase> elements,
            Properties textReader) throws EnrollmentSyncException {

        if ( endOfFile == true ) {
            return 0;
        }
        
        IElementField nameField = SITE_TYPE.getField(SiteReservedFieldEnumType.NAME.getName());
        IElementField ipField = SITE_TYPE.getField(SiteReservedFieldEnumType.IP_ADDRESS.getName());
        
        int counter = 0;
        for (Enumeration<?> e = textReader.propertyNames() ; e.hasMoreElements() ;) {
            
            String name = (String) e.nextElement();
            String value = (String) textReader.getProperty(name);
            
            IMElement element = createContentElement(name, value);
            
            element.setValue(nameField, name);
            element.setValue(ipField, value);
            
            elements.put(element.getPath(), element);
          
            counter++;
        }
        
        endOfFile = true;
        
        return counter;
    }
    
    private IMElement createContentElement(String name, String value) throws EnrollmentSyncException {
        
        LOG.trace("Creating a site element");
 
        DictionaryPath path = new DictionaryPath( new String[] {enrollment.getDomainName(), name } );
        DictionaryKey key = new DictionaryKey(name.getBytes());
        
        IMElement element = null;
        
        if (this.isUpdate()) {
            try {
                element = enrollment.getElement(key, this.getEnrollmentStartTime());
            } catch (DictionaryException e) {
                throw new EnrollmentSyncException(e, name);
            }
        }
        if (element == null) {
            element = enrollment.makeNewElement(path, SITE_TYPE, key);
        }
        
        element.setDisplayName(name);
        element.setUniqueName(name);
        
        return element;
    }
    
}
