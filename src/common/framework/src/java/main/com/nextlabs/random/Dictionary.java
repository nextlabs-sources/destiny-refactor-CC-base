/*
 * Created on Jul 22, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.random;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/random/Dictionary.java#1 $
 */

public class Dictionary {
    private static final String DICTIOINARY_FILENAME = "/com/nextlabs/random/dictionary.dic";
    private static final Log LOG = LogFactory.getLog(Dictionary.class);
    
    protected final List<String> dictionaryWords;
    protected final int dictionarySize;
    protected final Random r;
    
    public Dictionary(File secondaryFile) throws IOException {
        InputStream importFileIs = this.getClass().getResourceAsStream(DICTIOINARY_FILENAME);
        if (importFileIs != null) {
            dictionaryWords = loadDictionary(new InputStreamReader(importFileIs));
        } else if (secondaryFile != null) {
            LOG.warn("fail to load dictioanry from jar, try to load from a file");
            dictionaryWords = loadDictionary(new FileReader(secondaryFile));
        } else {
            throw new IOException("fail to load dictioanry.");
        }
        dictionarySize = dictionaryWords.size();
        LOG.info("Dictionary loaded successfully. Size = " + dictionarySize);
        
        r = new Random();
    }
    
    public Dictionary() throws IOException {
        this(null);
    }
    
    protected List<String> loadDictionary(Reader reader) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(reader);
            
            String line;
            while((line = bf.readLine()) != null){
                list.add(filter(line));
            }
        } finally {
            if (bf != null) {
                bf.close();
            }
        }
        list.trimToSize();
        return list;
    }
    
    protected String filter(String input){
        //TODO don't contain "'" in the dictionary at this moment since it will break the reporter
//        return input.replaceAll("\'", "");
        return input;
    }
    
    public String getRandomWord(){
        return dictionaryWords.get(r.nextInt(dictionarySize));
    }
    
    public String gernerateRandomString(int from, int to, String separator, boolean withTail) {
        StringBuilder sb = new StringBuilder();
        final int level = to == from 
                ? from 
                : from + r.nextInt(to - from);
        for (int i = 0; i < level; i++) {
            sb.append(getRandomWord());
            if (i < level - 1) {
                sb.append(separator);
            } else if (withTail) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
    
    public void clear(){
        dictionaryWords.clear();
    }
}
