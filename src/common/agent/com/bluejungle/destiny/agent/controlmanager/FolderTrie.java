/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Comparator;
import com.bluejungle.oil.PathAdapter;


/**
 * 
 * FolderTrie implements a variation of the Trie data structure. It is used to
 * determine alternate paths to reach the same resource.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class FolderTrie implements Serializable {

    public static final String SEPARATOR = File.separator;
    FolderTrieNode root;
    private PathAdapter pa = new PathAdapter();


    /**
     * Constructor
     *  
     */
    public FolderTrie() {
        super();
        this.root = new FolderTrieNode();
        //this.root.prefix = "\\\\";
        this.root.prefix = pa.getRegexSeperator();
    }

    /**
     * breaks up name at separators and creates nodes (if they dont already
     * exist) for each token in the name. the last node be set to point to obj
     * 
     * @param name
     *            name corresponding to obj
     * @param list
     *            list of strings corresponding to the specified name
     */
    public void addString(String name, String[] aliasList) {

        String lowerCaseName = name.toLowerCase();
        
        String[] list  = new String[aliasList.length];
        System.arraycopy(aliasList, 0, list, 0, aliasList.length);
        
        for (int i=0; i < list.length; i++){
            if ((list[i].charAt(1) != ':' && !list[i].startsWith("\\\\\\\\")) || 
                    (list[i].charAt(1) == ':' && !list[i].startsWith("\\\\", 2))){
                list[i] = list[i].replaceAll("\\\\", "\\\\\\\\"); //escape \\ if not already escaped
                list[i] = list[i].replaceAll("\\$", "\\\\\\$"); // escape the $ to \$
            }
        }
        
        /* The first item in list is the resource name and remaining are aliases */
        /* We sort the aliases for displying purpose                             */	
        sortAliasesByLength(list);
        
        //String[] names = lowerCaseName.split("\\\\");
        String[] names = lowerCaseName.split(pa.getRegexSeperator());

        FolderTrieNode node = this.root;

        for (int i = 0; i < names.length; i++) {
            String currentName = names[i];
            if (currentName.length() == 0){
                continue;
            }
            
            FolderTrieNode next = (FolderTrieNode) node.nameToNodeMap.get(currentName);
            if (next == null) {
                next = new FolderTrieNode();
                node.nameToNodeMap.put(currentName, next);
                //if it's physical address, overwrite the prefix with currentName
                if(!pa.isLinuxPhysicalPath(name))
                {
                	if (currentName.indexOf(':') == 1) {
                		next.prefix = currentName;
                	} else {
                		//next.prefix = node.prefix + "\\\\" + currentName;
                		next.prefix = node.prefix + pa.getRegexSeperator() + currentName;
                	}
                }
                else
                {
                	//for linux world, the prefix is always heading with '/'
                	next.prefix = node.prefix + currentName + pa.getRegexSeperator();
                }
                	
            }
            node = next;
        }

        node.list = list;

    }

    /**
     * @param name
     *            name of resource with full path
     * @return collection containing all possible paths to the specified file.
     */
    public ArrayList getFolderList(String name) {

        ArrayList ret = new ArrayList();

        String lowerCaseName = name.toLowerCase();

        FolderTrieNode node = getFolderTrieNodeByName(lowerCaseName);

        if (node.list != null) {
            for (int i = 0; i < node.list.length; i++) {
                ret.add (lowerCaseName.replaceFirst(node.prefix, node.list[i]));
            }
        } else {
            ret.add(name);
        }

        return ret;
    }

    /**
     * @param name
     *            name of resource with full path
     * @return the display alias for the giving name 
     */
    public String getResourceDisplayAlias(String name) {

        String lowerCaseName = name.toLowerCase();

        FolderTrieNode node = getFolderTrieNodeByName(lowerCaseName);

        if (node.list != null) {
        	int len = node.list.length;
        	return lowerCaseName.replaceFirst(node.prefix, node.list[len-1]);
        } else {
            return lowerCaseName; 
        }
    }

    /**
     * @param name
     *            name of resource with full path
     * Get FolderTrie Node by a giving name
     * Split the name into path element and lookup the trie
     * @return collection containing all possible paths to the specified file.
     */
    private FolderTrieNode getFolderTrieNodeByName(String name) {

        String lowerCaseName = name.toLowerCase();

        //String[] names = lowerCaseName.split("\\\\");

        String[] names = lowerCaseName.split(this.pa.getRegexSeperator());
        
        FolderTrieNode node = this.root;

        for (int i = 0; i < names.length; i++) {
            String currentName = names[i];
            if (currentName.length() == 0){
                continue;
            }
                
            FolderTrieNode next = (FolderTrieNode) node.nameToNodeMap.get(currentName);
            if (next == null) {
                break;
            }
            node = next;
        }
        return node;
    }


    /**
     * @param list 
     *            list of aliases for a giving resource 
     * @return the sorted list in ascending order based on the length of aliases 
     */
    private void sortAliasesByLength(String [] list) {
	if ( list.length > 1 ) 
            Arrays.sort( list, new StringLengthComparator());
    }

    private class StringLengthComparator implements Comparator{
    	public final int compare(Object A, Object B) throws ClassCastException {
            if (!( A instanceof String ) || !(B instanceof String))
                 throw new ClassCastException("A String object expected in Folder Aliase list");
            return ((String)A).length() - ((String) B).length();
    	}
	}
	    
    private class FolderTrieNode implements Serializable {

        String prefix;
        Map nameToNodeMap = new HashMap();
        String[] list = null;

    }
}
