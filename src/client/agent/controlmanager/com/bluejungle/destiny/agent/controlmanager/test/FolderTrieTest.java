/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.controlmanager.FolderTrie;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/test/FolderTrieTest.java#1 $:
 */

public class FolderTrieTest extends TestCase {

    /**
     * Constructor for FolderTrieTest.
     * 
     * @param arg0
     */
    public FolderTrieTest(String arg0) {
        super(arg0);
    }

    public void testFolderTrie() throws IOException, ClassNotFoundException {
        FolderTrie folderTrie = new FolderTrie();
        String[] one = new String[] { "\\\\server.test.blue\\f2", "\\\\server.test.blue\\f1\\f2", "c:\\f0\\f1\\f2" };
        String[] two = new String[] { "\\\\server\\f3", "\\\\server\\f2\\f3", "\\\\server\\f1\\f2\\f3" };

        folderTrie.addString("\\\\server\\f2", one);
        folderTrie.addString("\\\\server\\f1\\f2", one);
        folderTrie.addString("c:\\f0\\f1\\f2", one);
        folderTrie.addString("\\\\SERVER\\f1\\f2\\f3", two);
        folderTrie.addString("\\\\server\\f2\\f3", two);
        folderTrie.addString("\\\\SERVER\\f3", two);

        this.testTrieRead(folderTrie);

        ObjectOutputStream out;
        File file = File.createTempFile("folderTrieTest", "tmp");

        out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.writeObject(folderTrie);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        folderTrie = (FolderTrie) in.readObject();
        
        this.testTrieRead(folderTrie);        

    }

    /**
     * @param folderTrie
     */
    private void testTrieRead(FolderTrie folderTrie) {
        //check unknown
        List result = folderTrie.getFolderList("\\\\server\\f4\\foo.doc");
        assertTrue(result.contains("\\\\server\\f4\\foo.doc"));
        assertEquals(1, result.size());

        //check under f1
        result = folderTrie.getFolderList("\\\\server.test.blue\\f1\\f5\\f7\\foo.doc");
        assertTrue(result.contains("\\\\server.test.blue\\f1\\f5\\f7\\foo.doc"));
        assertEquals(1, result.size());

        //check under f2
        result = folderTrie.getFolderList("c:\\f0\\f1\\f2\\f5\\f7\\foo.doc");
        assertTrue(result.get(1).equals("\\\\server.test.blue\\f2\\f5\\f7\\foo.doc"));
        assertTrue(result.get(2).equals("\\\\server.test.blue\\f1\\f2\\f5\\f7\\foo.doc"));
        assertTrue(result.get(0).equals("c:\\f0\\f1\\f2\\f5\\f7\\foo.doc"));
        assertEquals(3, result.size());

        //check under f3
        result = folderTrie.getFolderList("\\\\SERVER\\F2\\f3\\f5\\f7\\foo.doc");
        assertTrue(result.contains("\\\\server\\f3\\f5\\f7\\foo.doc"));
        assertTrue(result.contains("\\\\server\\f2\\f3\\f5\\f7\\foo.doc"));
        assertTrue(result.contains("\\\\server\\f1\\f2\\f3\\f5\\f7\\foo.doc"));
        assertEquals(3, result.size());
    }

}
