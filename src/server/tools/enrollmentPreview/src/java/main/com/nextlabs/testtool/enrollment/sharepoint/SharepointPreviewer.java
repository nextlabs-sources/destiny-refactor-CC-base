/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment.sharepoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.plaf.basic.BasicTabbedPaneUI.TabbedPaneLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.ISharePointEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.SharePointEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointElementCreator;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.filereader.EnrollmentDefinitionReader;
import com.bluejungle.destiny.tools.enrollment.filereader.PortalConnFileReader;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.framework.comp.IConfiguration;
import com.nextlabs.testtool.enrollment.BasePreviewer;
import com.nextlabs.testtool.enrollment.IEnrollmentPreviewGUI;
import com.nextlabs.testtool.enrollment.Suggestions;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/sharepoint/SharepointPreviewer.java#1 $
 */

public class SharepointPreviewer extends BasePreviewer{
    private static final Log LOG = LogFactory.getLog(SharepointPreviewer.class);
    
    private final Suggestions suggestions;
    
    private SharePointEnroller enroller;

    private class SharePointEnrollerMod extends SharePointEnroller {
        @Override
        protected SharePointElementCreator getSharePointElementCreator(
                ISharePointEnrollmentWrapper wrapper) throws DictionaryException {
            return new SharePointElementCreatorMod(wrapper);
        }
    }

    private class SharePointElementCreatorMod extends SharePointElementCreator {
        private String currentGroupName;
        
        public SharePointElementCreatorMod(ISharePointEnrollmentWrapper enrollmentWrapper)
                throws DictionaryException {
            super(enrollmentWrapper);
        }

        @Override
        public void addGroup(String id, String name, String ownerID, boolean isOwnerUser)
                throws EnrollmentSyncException {
            super.addGroup(id, name, ownerID, isOwnerUser);
            gui.addNode(new String[]{name}, IEnrollmentPreviewGUI.TabbedPane.PARSED)
               .setNote("id = " + id + "\n" 
                       + "ownerID = " + ownerID + "\n"
                       + "isOwnerUser = " + isOwnerUser
               );
            currentGroupName = name;
            System.out.println("addGroup: " + id + ", " + name + ", " + ownerID + ", " + isOwnerUser);
        }

        @Override
        public void addMemberToCurrentGroup(String id, String name, String SID,
                boolean isDomainGroup) throws EnrollmentSyncException {
            if(currentGroupName == null){
                throw new EnrollmentSyncException("Can't add member to null group", name + ", " + id);
            }
            gui.addNode(new String[]{currentGroupName, name}, IEnrollmentPreviewGUI.TabbedPane.PARSED)
               .setNote("id = " + id + "\n" 
                       + "SID = " + SID + "\n"
                       + "isDomainGroup = " + isDomainGroup
               );
            System.out.println("addMemberToCurrentGroup: " + id + ", " + name + ", " + SID + ", " + isDomainGroup);
        }
    }
    
    public SharepointPreviewer(Properties properties){
        suggestions = new Suggestions();
    }
    
    public List<EnrollmentProperty> createRealmProperties(File connFile, File defFile,
            File filterFile, File selectiveFolder) throws IOException, FileFormatException {
        
        List<EnrollmentProperty> properties = new ArrayList<EnrollmentProperty>();

        PortalConnFileReader portalConnFileReader = new PortalConnFileReader(connFile);
        properties.addAll(portalConnFileReader.getProperties());
        
        // parseDefinitionFile
        // Parse the enrollment definition:
        EnrollmentDefinitionReader defReader = new EnrollmentDefinitionReader(defFile);
        
        properties.addAll(defReader.getProperties());

        normalize(properties);
        
        return properties;
    }

    public Suggestions getSuggestions() {
        return suggestions;
    }

    public void init(IConfiguration configuration) throws Exception {
        super.init(configuration);
        
        enroller = new SharePointEnrollerMod();
        enroller.process(enrollment, properties, dictionary);
    }

    public void start() throws Exception {
        enroller.sync(enrollment, dictionary);
    }

}
