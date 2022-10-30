/*
 * Created on Dec 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment.ad;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.RetrievalFailedException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncControl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncDispatcher;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADEnrollmentInitializer;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADEnrollmentWrapperImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.SyncResultEnum;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrShared;
import com.bluejungle.destiny.tools.enrollment.filereader.ADConnFileReader;
import com.bluejungle.destiny.tools.enrollment.filereader.EnrollmentDefinitionReader;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.destiny.tools.enrollment.filter.FilterFileReader;
import com.bluejungle.destiny.tools.enrollment.filter.SelectiveFilterGenerator;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.IPair;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.Table;
import com.nextlabs.testtool.enrollment.BasePreviewer;
import com.nextlabs.testtool.enrollment.IEnrollmentPreviewGUI;
import com.nextlabs.testtool.enrollment.MyTreeNode;
import com.nextlabs.testtool.enrollment.StringUtils;
import com.nextlabs.testtool.enrollment.Suggestions;
import com.nextlabs.testtool.enrollment.WarningType;
import com.nextlabs.testtool.enrollment.IEnrollmentPreviewGUI.TabbedPane;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.util.Base64;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/ad/ADPreviewer.java#1 $
 */

public class ADPreviewer extends BasePreviewer{
    private static final Log LOG = LogFactory.getLog(ADPreviewer.class);
    
    private static final String KEY_PREFIX = EnrollmentTypeEnumType.DIRECTORY.getName() + ".";
    
    public static final PropertyKey<String> COOKIE_KEY = new PropertyKey<String>(KEY_PREFIX + "cookie");
    
    private final ADSuggestions suggestions;
    private final String cookie;
    
    private DirSyncDispatcher dispatcher;
    private ADEnrollmentWrapperImpl adEnrollment;
    
    public ADPreviewer(Properties configProperties){
        super();
        cookie = configProperties.getProperty(COOKIE_KEY.toString());
        suggestions = new ADSuggestions();
    }
    
    public void init(IConfiguration configuration) throws Exception {
        super.init(configuration);

        final DirSyncControl dirSyncControl;
        if (cookie == null || cookie.trim().length() == 0) {
            dirSyncControl = null;
        } else {
            dirSyncControl = new DirSyncControl();
            dirSyncControl.setControlValue(Base64.decode(cookie));
        }

        ADEnrollmentInitializer initializer = new ADEnrollmentInitializer(enrollment, properties, dictionary);
        initializer.setup();
        
        adEnrollment = new ADEnrollmentWrapperImpl(enrollment, dictionary);
        suggestions.adEnrollment = adEnrollment;
        
        Field batchSizeField = ActiveDirectoryEnroller.class.getDeclaredField("DEFAULT_BATCH_SIZE");
        batchSizeField.setAccessible(true);
        int batchSize = (Integer)batchSizeField.get(null);
        
        dispatcher = new DirSyncDispatcher(
                dirSyncControl, 
                adEnrollment.getServer(), 
                adEnrollment.getPort(), 
                adEnrollment.getLogin(),
                adEnrollment.getPassword(), 
                adEnrollment.getSubtreesToEnroll(),
                adEnrollment.getAllAttributesToRetrieve(), 
                adEnrollment.getFilter(),
                adEnrollment.isPagingEnabled(), 
                adEnrollment.getSecureTransportMode(),
                adEnrollment.getAlwaysTrustAD(),
                batchSize );
        
        if(LOG.isInfoEnabled()){
            Table dirSyncDispatcherTable = new Table(true);
            dirSyncDispatcherTable.addColumn("Name", 25);
            dirSyncDispatcherTable.addColumn("Value", 60);
            
            dirSyncDispatcherTable.addRow("ad server name", adEnrollment.getServer());
            dirSyncDispatcherTable.addRow("ad server port", adEnrollment.getPort()); 
            dirSyncDispatcherTable.addRow("ad secureTransportMode", adEnrollment.getSecureTransportMode());
            dirSyncDispatcherTable.addRow("ad alwaysTrustAD", adEnrollment.getAlwaysTrustAD());
            dirSyncDispatcherTable.addRow("ad login", adEnrollment.getLogin());
            dirSyncDispatcherTable.addRow("ad password", StringUtils.maskString(adEnrollment.getPassword()));
            dirSyncDispatcherTable.addRow("subtreesToEnroll", ArrayUtils.asString(adEnrollment.getSubtreesToEnroll()));
            dirSyncDispatcherTable.addRow("allAttributesToRetrieve", ArrayUtils.asString(adEnrollment.getAllAttributesToRetrieve())); 
            dirSyncDispatcherTable.addRow("filter", adEnrollment.getFilter());
            dirSyncDispatcherTable.addRow("isPagingEnabled", adEnrollment.isPagingEnabled()); 
            
            LOG.info("DirSyncDispatcher Properties" + ConsoleDisplayHelper.NEWLINE 
                    + dirSyncDispatcherTable.getOutput());
        }
    }
    
    public Suggestions getSuggestions() {
        return suggestions;
    }

    public void start() throws EnrollmentValidationException, IOException{
        LOG.info("start pull");
        try {
            dispatcher.pull();
        } catch (RetrievalFailedException e) {
            LOG.error("fail to call dispatcher.pull" , e);
            return;
        }
        
        
        ADElementCreatorMod elementCreator = new ADElementCreatorMod(adEnrollment, dictionary);
        
        File successedDnFile = new File("log", "successedDn.txt");
        FileWriter writer= new FileWriter(successedDnFile);
        
        try {
            while (dispatcher.hasMore()) {
                LDAPEntry entry;
                try {
                    entry = dispatcher.next();
                } catch (NoSuchElementException e) {
                    LOG.error("fail to call dispatcher.next", e);
                    continue;
                }

                writer.write(entry.getDN());
                writer.write(ConsoleDisplayHelper.NEWLINE);
                
                parseEntry(entry, adEnrollment, elementCreator, gui);
                
                entryCount++;
                
                if ((entryCount + 1) % 500 == 0) {
                    LOG.info("already parsed " + entryCount + " entries");
                }
            }
        } catch (RetrievalFailedException e) {
            LOG.error("fail to call dispatcher.hasMore", e);
        }finally{
            writer.close();
        }
            
        System.out.println();
        LOG.info("enrolled " + entryCount + " entires");
    }
    
    private void parseEntry(LDAPEntry entry, ILDAPEnrollmentWrapper enrollmentWrapper,
            ADElementCreatorMod elementCreator, IEnrollmentPreviewGUI gui){
        Collection<String> warnings = new LinkedList<String>();
        Collection<String> missingFields = new LinkedList<String>();
        
        String dn = "<UNKNOWN DN>"; 
        String[] dnPaths = null;
        try {
            //basic parsing
            dn = entry.getDN();
            dnPaths = LDAPEnrollmentHelper.getDictionaryPathFromDN(dn).getPath();
            
            LDAPAttribute objectClassAttr = entry.getAttribute("objectClass");
            String[] objectClasses;
            if(objectClassAttr != null){
                objectClasses = objectClassAttr.getStringValueArray();
            }else{
                objectClasses = new String[0];
                missingFields.add("objectClass");
            }
            
            
            //element parsing
            IPair<SyncResultEnum, ? extends IElementBase> result =
                    elementCreator.createContentElement(entry);

            //get the parsed information
            IElementBase elementBase = result.second();
            ADElementCreatorMod.ElementType type = elementCreator.getLastCreatedType();
            
            if (type==ADElementCreatorMod.ElementType.UNKNOWN) {
                for (String objectClass : objectClasses) {
                    suggestions.failObjectClasses.add(objectClass);
                }
            }else{
                for (String objectClass : objectClasses) {
                    suggestions.okObjectClasses.add(objectClass);
                }
            }

            MyTreeNode node = gui.addNode(dnPaths, TabbedPane.PARSED).setColor(type.getColor());
            switch (type) {
            case USER:
            case CONTACT:
            case HOST:
            case APPLICATION:
            case SITE:
                IMElement elment = (IMElement)elementBase;
                Set<MyTreeNode> warningNodes = new HashSet<MyTreeNode>();
                for(IElementField field : elementCreator.getLastExternalFields()){
                    Object value = elment.getValue(field);
                    if (value != null) {
                        valueChecker.checkField(dn, field, value, warnings, warningNodes);
                    } else {
                        missingFields.add(field.getName());
                        valueChecker.addWarning(WarningType.MISSING_FIELD, field, dn, warningNodes);
                    }
                }
                if (!missingFields.isEmpty()) {
                    warnings.add(WarningType.MISSING_FIELD.format(
                            CollectionUtils.asString(missingFields, ", ")));
                }
                
                if (!warnings.isEmpty()) {
                    LOG.warn("DN: " + dn + "    MSG: " + CollectionUtils.toString(warnings));
                    gui.addNode(dnPaths, TabbedPane.WARNING)
                            .setColor(type.getColor())
                            .setNote(CollectionUtils.asString(warnings, "\n"));
                    for(MyTreeNode warningNode : warningNodes){
                        warningNode.setNote(CollectionUtils.asString(warnings, "\n"));
                    }
                }
                break;
            case STRUCT_GROUP:
            case GROUP:
                LDAPAttribute memebership =
                    entry.getAttribute(enrollmentWrapper.getMembershipAttributeName());
                node.setSize(memebership != null ? memebership.size() : 0);
                break;
            case OTHER:
            case UNKNOWN:
                node = gui.addNode(dnPaths, TabbedPane.UNKNOWN)
                        .setColor(type.getColor())
                        .setNote("objectClass=" + ArrayUtils.asString(objectClasses, ", "));
                break;
            default:
                break;
            }
        } catch (Exception e) {
            //should not be there, only Dictionary Exception is possible but we don't have a database.
            LOG.error("fail to parse DN=" + dn, e);
            throw new RuntimeException(e);
        }
    }
    
    public List<EnrollmentProperty> createRealmProperties(
            File connFile, 
            File defFile, 
            File filterFile,
            File selectiveFolder
    ) throws IOException, FileFormatException {
        System.setProperty(EnrollmentMgrShared.ENROLLMENT_TOOL_HOME, selectiveFolder.getAbsolutePath());
        
        List<EnrollmentProperty> properties = new ArrayList<EnrollmentProperty>();
        // Parse the root file:
        ADConnFileReader ad_conndefReader = new ADConnFileReader(connFile);
        properties.addAll(ad_conndefReader.getProperties());
        
        if(filterFile != null){
            // Parse the filter file:
            FilterFileReader filterFileReader = new FilterFileReader(
                    filterFile);
            SelectiveFilterGenerator filterGenerator = new SelectiveFilterGenerator(
                    filterFileReader.getSelectedUsers(),
                    filterFileReader.getSelectedHosts(),
                    filterFileReader.getSelectedGroups());
            String filter = filterGenerator.getSelectiveFilter();
            properties.add(new EnrollmentProperty(
                    ActiveDirectoryEnrollmentProperties.FILTER,
                    new String[] { filter }));
        }
        
        // parseDefinitionFile
        // Parse the enrollment definition:
        EnrollmentDefinitionReader defReader = new EnrollmentDefinitionReader(defFile);
        
        properties.addAll(defReader.getProperties());

        normalize(properties);
        
        return properties;
    }
}
