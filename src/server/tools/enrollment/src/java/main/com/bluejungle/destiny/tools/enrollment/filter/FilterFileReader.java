/*
 * Created on Nov 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/FilterFileReader.java#1 $
 */

public class FilterFileReader {

    private static final Log LOG = LogFactory.getLog(FilterFileReader.class);

    private static final int N_TYPES = 3;
    private static final Set<String> ACCEPTABLE_COLUMNS;
    static {
        ACCEPTABLE_COLUMNS = new HashSet<String>();
        ACCEPTABLE_COLUMNS.add(SelectiveFilterConfiguration.SINGLETON.getUsersColumnHeader());
        ACCEPTABLE_COLUMNS.add(SelectiveFilterConfiguration.SINGLETON.getHostsColumnHeader());
        ACCEPTABLE_COLUMNS.add(SelectiveFilterConfiguration.SINGLETON.getGroupsColumnHeader());

        if (ACCEPTABLE_COLUMNS.size() != N_TYPES) {
            throw new IllegalArgumentException("Some of the configured user/host/group column headers are redundant.");
        }
    }

    private BufferedReader filterFileReader;
    private Set<String> selectedUsers;
    private Set<String> selectedHosts;
    private Set<String> selectedGroups;
    private List<String> columnSequence = new ArrayList<String>();
    private Map<String, Set<String>> columnValues = new HashMap<String, Set<String>>();

    /**
     * Constructor
     *  
     */
    public FilterFileReader(File file) throws IOException, FileFormatException {
        super();
        this.filterFileReader = new BufferedReader(new FileReader(file));

        parseFile();
    }

    private void parseFile() throws IOException, FileFormatException {
        String line = this.filterFileReader.readLine();

        if (line == null) {
            throw new FileFormatException("First line should be a header line");
        }

        // Parse header line to determine columns:
        LOG.debug("Reading header line...");
        String[] columns = line.split(SelectiveFilterConfiguration.SINGLETON.getCellDelimiter());
        for (int i = 0; i < columns.length; i++) {
            String header = columns[i].trim();
            if (!ACCEPTABLE_COLUMNS.contains(header)) {
                throw new FileFormatException("Invalid column header: '" + header + "'");
            }

            this.columnSequence.add(header);

            // Whenever we encounter a value, we will put it into the set for
            // the corresponding column. WE also support multiple columns per
            // type.
            if (!this.columnValues.containsKey(header)) {
                this.columnValues.put(header, new HashSet<String>());
            }
        }

        // Parse the rest of the lines:
        line = this.filterFileReader.readLine();
        while (line != null) {
            LOG.debug("Processing line: '" + line + "'");

            String[] values = line.split(SelectiveFilterConfiguration.SINGLETON.getCellDelimiter());

            // Make sure that all values are under a column header:
            if (values.length > this.columnSequence.size()) {
                throw new FileFormatException("Values specified are more than the provided column headers in line: '" + line + "'");
            }

            // Now retrieve the values and put them under teh corresopnding
            // header:
            for (int i = 0; i < values.length; i++) {
                String selection = values[i].trim();

                // Don't process an empty string:
                if (!selection.equals("")) {
                    String correspondingHeader = (String) this.columnSequence.get(i);

                    // Add value to the set of values for the corresponding
                    // type:
                    Set<String> existingValues = this.columnValues.get(correspondingHeader);
                    existingValues.add(selection);
                }
            }

            line = this.filterFileReader.readLine();
        }

        // Now retrieve the selections:
        String userColumnHeader = SelectiveFilterConfiguration.SINGLETON.getUsersColumnHeader();
        if (this.columnValues.containsKey(userColumnHeader)) {
            this.selectedUsers = this.columnValues.get(userColumnHeader);
        }
        LOG.debug("Selected user list: '" + this.selectedUsers + "'");

        String hostColumnHeader = SelectiveFilterConfiguration.SINGLETON.getHostsColumnHeader();
        if (this.columnValues.containsKey(hostColumnHeader)) {
            this.selectedHosts = this.columnValues.get(hostColumnHeader);
        }
        LOG.debug("Selected host list: '" + this.selectedHosts + "'");

        String groupsColumnHeader = SelectiveFilterConfiguration.SINGLETON.getGroupsColumnHeader();
        if (this.columnValues.containsKey(groupsColumnHeader)) {
            this.selectedGroups = this.columnValues.get(groupsColumnHeader);
        }
        LOG.debug("Selected groups list: '" + this.selectedGroups + "'");
    }

    public Set<String> getSelectedUsers() {
        return this.selectedUsers;
    }

    public Set<String> getSelectedHosts() {
        return this.selectedHosts;
    }

    public Set<String> getSelectedGroups() {
        return this.selectedGroups;
    }
}