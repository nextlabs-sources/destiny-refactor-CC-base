/*
 * Created on May 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/LDIFVersioningManager.java#1 $
 */

public class LDIFVersioningManager {

    private static final Log LOG = LogFactory.getLog(LDIFVersioningManager.class);
    private static final LDIFVersioningManager SINGLETON = new LDIFVersioningManager();

    /**
     * Returns the LDIF Versioning manager object
     * 
     * @return
     */
    public static final LDIFVersioningManager getSingleton() {
        return SINGLETON;
    }

    /**
     * Constructor
     *  
     */
    protected LDIFVersioningManager() {
        super();
    }

    /**
     * Adds the "Version: 1" line into the 'outputFile' variable and copies over
     * all the contents from the 'inputFile' variable.
     * 
     * @param inputFile
     * @param outputFile
     */
    public void addVersionLineToLDIF(String inputFile, String outputFile) throws VersioningFailedException {
        try {
            String versionLine = "version: 1\n";

            PrintWriter versionedFileWriter = new PrintWriter(new FileOutputStream(outputFile));
            versionedFileWriter.println(versionLine);

            // Now copy over all contents from the intput file:
            BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = inputFileReader.readLine()) != null) {
                versionedFileWriter.println(line);
            }

            // Close all streams:
            versionedFileWriter.flush();
            versionedFileWriter.close();
            inputFileReader.close();
        } catch (FileNotFoundException e) {
            throw new VersioningFailedException(e);
        } catch (IOException e) {
            throw new VersioningFailedException(e);
        }
    }

    /**
     * Removes the "Version: 1" line from the 'inputFile' by copying over
     * everything else after that line into the 'outputFile'.
     * 
     * @param inputFile
     * @param outputFile
     */
    public void removeVersionLineFromLDIF(String inputFile, String outputFile) throws VersioningFailedException {
        try {
            String versionLine = "version: 1";

            PrintWriter fileWriter = new PrintWriter(new FileOutputStream(outputFile));
            BufferedReader versionedFileReader = new BufferedReader(new FileReader(inputFile));
            String line;

            // First skip all contents from the intput file until the first
            // "dn:" string is encountered:
            while (((line = versionedFileReader.readLine()) != null) && (!line.startsWith("dn:"))) {
                LOG.debug("Dropping following line from converted LDIF: '" + line + "'");
            }

            // If we're not at the end of the file already (hopefully), we copy
            // over the rest of the file:
            if (line != null) {
                fileWriter.println(line);
                while ((line = versionedFileReader.readLine()) != null) {
                    fileWriter.println(line);
                }
            }

            // Close all streams:
            fileWriter.flush();
            fileWriter.close();
            versionedFileReader.close();
        } catch (FileNotFoundException e) {
            throw new VersioningFailedException(e);
        } catch (IOException e) {
            throw new VersioningFailedException(e);
        }
    }
}