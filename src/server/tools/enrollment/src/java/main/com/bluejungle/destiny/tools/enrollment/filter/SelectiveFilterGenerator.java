/*
 * Created on Nov 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/SelectiveFilterGenerator.java#1 $
 */

public class SelectiveFilterGenerator {

    private static final Log LOG = LogFactory.getLog(SelectiveFilterGenerator.class);

    private Set<String> userSelections;
    private Set<String> hostSelections;
    private Set<String> groupSelections;
    private String selectiveFilter;

    /**
     * Constructor
     *  
     */
    public SelectiveFilterGenerator(Set<String> userSelections, Set<String> hostSelections, 
    		Set<String> groupSelections) {
        super();
        this.userSelections = userSelections;
        this.hostSelections = hostSelections;
        this.groupSelections = groupSelections;

        String hierarchyRetainerFilter = SelectiveFilterConfiguration.SINGLETON.getHierarchyRetainerFilter();

        String selectedUsersFilter = getSelectedEntriesFilter(this.userSelections,
				SelectiveFilterConfiguration.SINGLETON.getAllValidUsersFilter(),
				new SelectionCreator() {
					public AbstractSelection create(String selectionStr) {
						return new UserSelection(selectionStr);
					}
				}
        );
        
        String selectedHostsFilter = getSelectedEntriesFilter(this.hostSelections, 
        		SelectiveFilterConfiguration.SINGLETON.getAllValidHostsFilter(),
        		new SelectionCreator() {
					public AbstractSelection create(String selectionStr) {
						return new HostSelection(selectionStr);
					}
				}
        );
        String selectedGroupsFilter = getSelectedEntriesFilter(this.groupSelections,
        		SelectiveFilterConfiguration.SINGLETON.getAllValidGroupsFilter(),
        		new SelectionCreator() {
					public AbstractSelection create(String selectionStr) {
						return new GroupSelection(selectionStr);
					}
				}
        );

        this.selectiveFilter = LDAPFilterUtils.or( 
        		hierarchyRetainerFilter, 
        		selectedUsersFilter,
        		selectedHostsFilter, 
        		selectedGroupsFilter);
        LOG.debug("Generated selective filter: '" + this.selectiveFilter + "'");
    }

    private String getSelectedEntriesFilter(Set<String> existingSelections, String allValidFilter,
			SelectionCreator selectionCreator) {
    	if(existingSelections == null || existingSelections.isEmpty()){
    		return allValidFilter;
    	}
    	
		String[] selectionStrs = new String[existingSelections.size()];
		existingSelections.toArray(selectionStrs);
		for (int i = 0; i < selectionStrs.length; i++) {
			String selectionStr = selectionStrs[i];
			AbstractSelection selection = selectionCreator.create(selectionStr);
			selectionStrs[i] = selection.getExtractedFilter();
		}
		
		return LDAPFilterUtils.and(allValidFilter, LDAPFilterUtils.or(selectionStrs));
	}
    
    private interface SelectionCreator{
    	AbstractSelection create(String selectionStr);
    }

    public String getSelectiveFilter() {
        return this.selectiveFilter;
    }
}