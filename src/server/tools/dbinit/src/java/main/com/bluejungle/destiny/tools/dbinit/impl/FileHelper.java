/*
 * Created on Apr 7, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/impl/FileHelper.java#1 $
 */

class FileHelper {
    private static final Log LOG = LogFactory.getLog(FileHelper.class);
    
    private static final String NEWLINE = System.getProperty("line.separator");
    
    static void writeToFile(File file, List<String> dataToOut) throws IOException {
        LOG.info("Writing to a file " + file);
        
        FileWriter fstream = null;
        BufferedWriter out = null;
        try{
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
    
            for (String data : dataToOut) {
                LOG.trace(data);
                out.write(data + NEWLINE);
            }
        }finally{
            close(out);
            close(fstream);
        }
    }
    
    static List<String> readFromFile(File file) throws IOException {
        LOG.trace("reading from a file " + file);
        List<String> output = new ArrayList<String>();
        FileReader fstream = null;
        BufferedReader in = null;
        try {
            fstream = new FileReader(file);
            in = new BufferedReader(fstream);

            String input;
            while ((input = in.readLine()) != null) { // while loop begins here
                output.add(input);
            }
        } finally{
            close(in);
            close(fstream);
        }
        return output;
    }
    
    private static void close(Closeable res) {
        if (res != null) {
            try {
                res.close();
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
    }
    
    /**
     * Loads the configuration file and returns a property bundle
     * 
     * @param fileInputStream
     *            input stream to the configuration file
     * @return the list of properties contained in the file
     * @throws IOException
     *             if reading the file failed.
     */
    static Properties loadProperties(File file) throws IOException {
        Properties configProps = new Properties();
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            configProps.load(fileInputStream);
        } finally {
            close(fileInputStream);
        }
        return configProps;
    }
}
