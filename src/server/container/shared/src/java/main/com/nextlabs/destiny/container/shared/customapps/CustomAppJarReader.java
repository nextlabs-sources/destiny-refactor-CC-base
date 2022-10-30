/*
 * Created on Mar 4, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.jar.JarFile;

import org.xml.sax.SAXParseException;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.destiny.container.shared.customapps.mapping.IamJO;
import com.nextlabs.destiny.container.shared.customapps.mapping.JoHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/CustomAppJarReader.java#1 $
 */

public class CustomAppJarReader extends JarReader implements IExternalApplication {
    private static final String MSG_THE_ENTRY_DOESN_T_EXIST = "The entry \"{1}\" doesn''t exist in \"{0}\".";
    private static final String MSG_UNABLE_TO_DO = "Unable to {2} entry, \"{1}\", in custom app \"{0}\".";
    private static final String MSG_INVALID_CUSTOM_APP_FILE = "The customApp file, \"{0}\", {1}.";

    protected final String customAppPath;
    
    protected CustomAppJarReader(JarFile jarFile, String customAppPath) {
        super(jarFile);
        this.customAppPath = customAppPath;
    }
    
    public static CustomAppJarReader create(File customAppFile) throws InvalidCustomAppException {
        String customAppPath = customAppFile.getAbsolutePath();
        
        if (!customAppFile.exists()) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_INVALID_CUSTOM_APP_FILE,
                    customAppPath, "doesn't exists"));
        }
        if (!customAppFile.isFile()) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_INVALID_CUSTOM_APP_FILE,
                    customAppPath, "is not a file"));
        }
        if (!customAppFile.canRead()) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_INVALID_CUSTOM_APP_FILE,
                    customAppPath, "is not readable."));
        }
        
        CustomAppJarReader appReader;
        try {
            appReader = new CustomAppJarReader(new JarFile(customAppFile), customAppPath);
        } catch (IOException e) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_INVALID_CUSTOM_APP_FILE,
                    customAppPath, "is not a valid jar file."), e);
        }
        return appReader;
    }
    
    protected <T extends IamJO> T read(
            String pathToXml,
            String xsdFilePath,
            T t) throws InvalidCustomAppException{
        String xmlContent = getXml(pathToXml, xsdFilePath);
        try {
            return JoHelper.read(xmlContent, t);
        } catch (Exception e) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_UNABLE_TO_DO, "parse",
                    pathToXml, customAppPath), e);
        }
    }
    
    protected String getXml(String pathToXml, String xsdFile) throws InvalidCustomAppException {
        String xmlContent;
        try {
            xmlContent = this.getString(pathToXml);
        } catch (IOException e) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_UNABLE_TO_DO, "read",
                    pathToXml, customAppPath), e);
        }
        
        String schemaLocation = this.getClass().getResource(xsdFile).toString();
        
        Collection<SAXParseException> errors;
        try {
            errors = JoHelper.validateXml(schemaLocation, NAMESPACE, xmlContent);
        } catch (Exception e) {
            throw new InvalidCustomAppException(MessageFormat.format(MSG_UNABLE_TO_DO, "validate",
                    pathToXml, customAppPath), e);
        }
        
        if (!errors.isEmpty()) {
            throw new InvalidCustomAppException("The " + pathToXml
                    + " in '" + customAppPath + "' doesn't match the schema."
                    + CollectionUtils.toString(errors));
        }
        return xmlContent;
    }
    
    @Override
    public String getString(String entryName) throws IOException, InvalidCustomAppException {
        try {
            return super.getString(entryName);
        } catch (NullPointerException e) {
            // use == instead of equals.
            if (e.getMessage() == entryName) {
                throw new InvalidCustomAppException(MessageFormat.format(
                        MSG_THE_ENTRY_DOESN_T_EXIST, customAppPath, entryName));
            }
            throw e;
        }
    }
}
