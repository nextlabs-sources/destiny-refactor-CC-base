/*
 * Created on Jul 20, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.nextlabs.shared.tools.MaskedConsolePrompt;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filereader/BaseFileReader.java#1 $
 */

public abstract class BaseFileReader {
    protected final List<EnrollmentProperty> propertyList;
    protected final File file;
    
    protected abstract String getFileDescription();
    
    @Deprecated
    public BaseFileReader(String filePath) throws FileNotFoundException, IOException,
            FileFormatException {
        this(new File(filePath));
    }
    
    /**
     * 
     * @param file
     * @throws FileNotFoundException if the file doesn't not exist
     * @throws IOException if any occur during reading the file
     * @throws FileFormatException if the file contains invalid format or missing key
     */
    public BaseFileReader(File file) throws FileNotFoundException, IOException, FileFormatException {
        if (!file.exists()) {
            throw new FileNotFoundException(getFileDescription() + ", \"" + file.getAbsolutePath()
                    + "\", doesn't exist.");
        }
        
        if (!file.canRead()) {
            throw new IOException(getFileDescription() + ", \"" + file.getAbsolutePath()
                    + "\", is not readable.");
        }
        
        this.file = file;
        
        Properties properties = new CaseInsensitiveProperties();
        properties.load(new FileInputStream(file));
        
        validate(properties);
        
        propertyList = convert(properties);
    }
    
    private class CaseInsensitiveProperties extends Properties{
        CaseInsensitiveProperties() {
            super();
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            return super.put(((String)key).toLowerCase(), value);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return super.getProperty(key.toLowerCase(), defaultValue);
        }

        @Override
        public String getProperty(String key) {
            return super.getProperty(key.toLowerCase());
        }

        @Override
        public synchronized boolean containsKey(Object key) {
            return super.containsKey(((String) key).toLowerCase());
        }

        @Override
        public synchronized Object remove(Object key) {
            return super.remove(((String) key).toLowerCase());
        }
    }
    
    protected abstract void validate(Properties properties) throws FileFormatException;
    
    protected List<EnrollmentProperty> convert(Properties properties){
        List<EnrollmentProperty> list = new ArrayList<EnrollmentProperty>(properties.size());
        for (Map.Entry<?, ?> e : properties.entrySet()) {
            String key = ((String)e.getKey()).toLowerCase();
            String value = (String)e.getValue();
            list.add(new EnrollmentProperty(key, new String[] {value}));
        }
        
        return list;
    }
    
    public List<EnrollmentProperty> getProperties() {
        return this.propertyList;
    }
    
    /**
     * 
     * @param properties
     * @param key
     * @return the value that is corresponding to the key
     * @throws FileFormatException if <code>properties</code> doesn't not contain <code>key</code>
     */
    protected String getRequireString(Properties properties, String key) throws FileFormatException{
        String value = properties.getProperty(key);
        if ((value == null) || (value.trim().length() == 0)) {
            throw new FileFormatException(String.format("\"%s\" is required in file, \""
                    + file.getAbsolutePath() + "\"", key));
        }
        return value;
    }
    
    protected String getPasswordFromUser(String user) throws FileFormatException {
        char[] passwordIn = null;
        try {
            String prompt = "Enter password of " + user + " : ";
            passwordIn = MaskedConsolePrompt.getPassword(System.in, prompt);
        } catch (IOException e) {
            throw new FileFormatException("Can not read password from input");
        }
        if ((passwordIn == null) || (passwordIn.length == 0)) {
            throw new FileFormatException("No password entered");
        }
        return String.valueOf(passwordIn);
    }
}
