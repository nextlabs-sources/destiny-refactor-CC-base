/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.sharedfolder.defaultimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource;
import com.bluejungle.destiny.container.shared.sharedfolder.defaultimpl.SharedFolderInformationCookieImpl;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderAliasImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderAliasesAliasImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderDataImpl;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;

/**
 * This class implements the shared folder information source interface and is
 * specific to the DMS. It is responbile for creating the initial shared folder
 * data that will be sent to the DABS and stored in the corresponding sink
 * there. This class must be thread-safe.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/sharedfolder/defaultimpl/DMSSharedFolderInformationSourceImpl.java#1 $
 */

public class DMSSharedFolderInformationSourceImpl implements ISharedFolderInformationSource, ILogEnabled, IManagerEnabled {

    private static final String ALIASES_FILE_NAME = "aliases.txt";

    private static final int CHECK_INTERVAL_MSEC = 60000; // One minute

    private ISharedFolderData info;

    private long lastCheckedSharedFolder = 0;

    private Log log;

    private IComponentManager manager;

    private boolean logFileNotFound = false;

    /**
     * Constructor
     *  
     */
    public DMSSharedFolderInformationSourceImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource#getSharedFolderInformationUpdateSince(com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie)
     */
    public synchronized ISharedFolderData getSharedFolderInformationUpdateSince(ISharedFolderCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie");
        }
        boolean infoChanged = false;
        ISharedFolderData newInfo = info;

        if (info == null || lastCheckedSharedFolder + CHECK_INTERVAL_MSEC < System.currentTimeMillis()) {
            newInfo = readFromFile();
        }
            
        if (info != newInfo) {
            info = newInfo;
            infoChanged = true;
        }
        
        if (infoChanged || info == null || !info.getCookie().getTimestamp().equals(cookie.getTimestamp())) {
            return info;
        }

        // No change.  Return a new cookie with the same timestamp (to indicate no change) but no
        // actual data (to save space)
        SharedFolderDataImpl unchangedData = new SharedFolderDataImpl();
        unchangedData.setCookie(info.getCookie());

        return unchangedData;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    private ISharedFolderData readFromFile() {
        try {
            INamedResourceLocator locator = (INamedResourceLocator) manager.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);

            String fileName = locator.getFullyQualifiedName(ServerRelativeFolders.ALIASED_SHARES_FOLDER.getPathOfContainedFile(ALIASES_FILE_NAME));

            if (fileName == null) {
                log.error("Unable to determine the location of the shared aliases file.");
                return null;
            }

            long oldLastCheckedSharedFolder = lastCheckedSharedFolder;
            lastCheckedSharedFolder = System.currentTimeMillis();
            
            File fn = new File(fileName);
            File parentDir = fn.getParentFile();

            if (!fn.exists() || parentDir == null) {
                return null;
            }
            
            // If neither file nor directory has been modified, contents of
            // file can't have changed.
            if (fn.lastModified() <= oldLastCheckedSharedFolder &&
                parentDir.lastModified() <= oldLastCheckedSharedFolder) {
                return info;
            }
            
            InputStream fis = null;
            try {
                StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(fis = new FileInputStream(fileName))));
                logFileNotFound = true;
                // Prepare the tokenizer to read the file
                st.resetSyntax();
                st.wordChars('\0', '\255');
                st.whitespaceChars('\n', '\n');
                st.whitespaceChars('\r', '\r');
                st.eolIsSignificant(false);
                // Read the file and build the equivalence sets
                Map<String, Set<ISharedFolderAliasesAlias>> same = new HashMap<String, Set<ISharedFolderAliasesAlias>>();
                List<ISharedFolderAlias> all = new ArrayList<ISharedFolderAlias>();
                boolean warned1 = false; // Unexpected header
                boolean warned2 = false; // Unclosed quotation mark
                String lastShared = null;
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    String line = st.sval.trim();
                    boolean addAndReset = false;
                    if (line.charAt(0) != '\'') {
                        // New computer
                        addAndReset = true;
                        String[] tokens = line.split(" ");
                        if (tokens != null && tokens.length == 2) {
                            if (!"0".equals(tokens[1]) && log.isErrorEnabled()) {
                                log.error("Alias finder was unable to scan " + tokens[0] + ", error code " + tokens[1]);
                            }
                        } else {
                            if (!warned1 && log.isWarnEnabled()) {
                                log.warn("File format error reading " + ALIASES_FILE_NAME + ": found an unexpected header.");
                                warned1 = true;
                            }
                        }
                    } else {
                        // the next entry
                        int pos1 = line.indexOf('\'', 1);
                        int pos2 = line.indexOf('\'', pos1 + 1);
                        if (pos1 > 2 && pos2 != -1) {
                            String key = line.substring(pos2);
                            Set<ISharedFolderAliasesAlias> have = null;
                            if (same.containsKey(key)) {
                                have = same.get(key);
                            } else {
                                same.put(key, have = new HashSet<ISharedFolderAliasesAlias>());
                                int pos3 = key.indexOf('\'', 1);
                                if (pos3 != -1) {
                                    have.add(new SharedFolderAliasesAliasImpl(key.substring(1, pos3)));
                                }
                            }
                            have.add(new SharedFolderAliasesAliasImpl(line.substring(1, pos1)));
                        } else {
                            if (!warned2 && log.isWarnEnabled()) {
                                log.warn("File format error reading " + ALIASES_FILE_NAME + ": found an unclosed quotation mark.");
                                warned2 = true;
                            }
                        }
                    }
                    if (addAndReset) {
                        if (!same.isEmpty()) {
                            for (Set<ISharedFolderAliasesAlias> tmp : same.values()) {
                                ISharedFolderAliasesAlias[] aliases = (ISharedFolderAliasesAlias[]) tmp.toArray(new SharedFolderAliasesAliasImpl[tmp.size()]);
                                int size = aliases.length;
	                            SharedFolderAliasImpl sharedFolderAlias = new SharedFolderAliasImpl();
                                for (int i = 0; i < size; i++) {
                                    sharedFolderAlias.addAlias(aliases[i]);
                                }
                                all.add(sharedFolderAlias);
                            }
                            same = new HashMap<String, Set<ISharedFolderAliasesAlias>>();
                        }
                    }
                }
                if (!same.isEmpty()) {
                    for (Set<ISharedFolderAliasesAlias> tmp : same.values()) {
                        ISharedFolderAliasesAlias[] aliases = (ISharedFolderAliasesAlias[]) tmp.toArray(new SharedFolderAliasesAliasImpl[tmp.size()]);
                        int size = aliases.length;
	                    SharedFolderAliasImpl sharedFolderAlias = new SharedFolderAliasImpl();
                        for (int i = 0; i < size; i++) {
                            sharedFolderAlias.addAlias(aliases[i]);
                        }
                        all.add(sharedFolderAlias);
                    }
                }
                ISharedFolderAlias[] allAliases = (ISharedFolderAlias[]) all.toArray(new ISharedFolderAlias[all.size()]);
                SharedFolderDataImpl result = new SharedFolderDataImpl();
                int size = allAliases.length;
                for (int i = 0; i < size; i++) {
                    result.addSharedFolderAlias(allAliases[i]);
                }
                SharedFolderInformationCookieImpl cookie = new SharedFolderInformationCookieImpl(new Date());
                result.setCookie(cookie);
                return result;
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (FileNotFoundException e) {
            if (logFileNotFound) {
                getLog().warn("Error reading share aliases: file not found ", e);
                logFileNotFound = false;
            }
            return null;
        } catch (IOException e) {
            getLog().error("Error reading share aliases: ", e);
            return null;
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }
}
