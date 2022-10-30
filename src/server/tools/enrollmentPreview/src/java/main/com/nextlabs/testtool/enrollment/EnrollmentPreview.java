/*
 * Created on Feb 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.dictionary.DictionaryGhost;
import com.bluejungle.dictionary.EnrollmentGhost;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.tools.EnrollmentSeedDataTask;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.Table;
import com.nextlabs.testtool.enrollment.ad.ADPreviewer;
import com.nextlabs.testtool.enrollment.ad.ADElementCreatorMod.ElementType;
import com.nextlabs.testtool.enrollment.sharepoint.SharepointPreviewer;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/EnrollmentPreview.java#1 $
 */
public class EnrollmentPreview extends ConsoleApplicationBase {
    private static final Log LOG = LogFactory.getLog(EnrollmentPreview.class);
    
    private static final String COLUMN_LENGTH_KEY = "database.string-column.length";
    private static final String ENCODING_KEY = "database.encoding";
    private static final String COLOR_KEY_PREFIX = "color.";
    
    private static final String NOT_DEFINED_WARNING =
            "'%s' is not defined correctly in config.properties. The checking will be disabled.";

    private final IConsoleApplicationDescriptor descriptor;
    private IEnrollmentPreviewGUI gui;
    
    public static void main(String[] args) {
        try {
            EnrollmentPreview mgr = new EnrollmentPreview();
            mgr.parseAndExecute(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EnrollmentPreview() throws InvalidOptionDescriptorException {
        descriptor = new EnrollmentPreviewOptionDescriptorEnum();
    }
    
    private class DummyGUI implements IEnrollmentPreviewGUI {
        public MyTreeNode addNode(String[] paths, TabbedPane pane) {
            return new MyTreeNode(Arrays.toString(paths));
        }

        public void addProperty(String key, String value) {
            //do nothing
        }

        public MyTreeNode[] convert(String[] strs) {
            MyTreeNode[] r = new MyTreeNode[strs.length];
            for (int i = 0; i < strs.length; i++) {
                r[i] = new MyTreeNode(strs[i]);
            }
            return r;
        }

        public void setVisible(boolean visible) {
            //do nothing
        }

        public void init() {
            //do nothing
        }

        public void setStatusMessage(String str) {
            //do nothing
        }
    }
	
	@Override
	protected void execute(ICommandLine cmd) {
	    boolean noGui = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.NO_GUI_OPTIONS_ID);
	    gui = noGui ? new DummyGUI() : new EnrollmentPreviewGUI();
	    
	    //create log folder
		File logFolder = new File("log");
		if( !logFolder.exists() && !logFolder.mkdir() ){
			System.err.println("can't create log folder: " +logFolder.getAbsolutePath());
			return;
		}
		System.out.println("Log folder is created: " + logFolder.getAbsolutePath());
		
		File configFile = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.CONFIG_FILE_OPTIONS_ID);
		Properties properties = new Properties();
		FileInputStream fis = null;
        try {
            fis = new FileInputStream(configFile);
            properties.load(fis);
        } catch (IOException e) {
            System.out.println(e);
            return;
        }finally{
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    System.out.println(e);
                    return;
                }
            }
        }
        
        for (ElementType elementType : ElementType.values()) {
            String hexColor = properties.getProperty(COLOR_KEY_PREFIX + elementType.name());
            if (hexColor != null) {
                hexColor = hexColor.trim();
                if(hexColor.toLowerCase().startsWith("0x")){
                    hexColor = hexColor.substring(2);
                }
                int i = Integer.parseInt(hexColor, 16);
                elementType.setColor(new Color(
                        (i >> 16) & 0xFF, 
                        (i >> 8) & 0xFF, 
                        (i >> 0) & 0xFF)
                );
            }
        }
        gui.init();
        
        String enrollmentType = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.ENROLLMENT_TYPE_OPTION_ID);

        IPreviewer previewer;
        if (enrollmentType.equals(EnrollmentTypeEnumType.DIRECTORY.getName())) {
            previewer = new ADPreviewer(properties);
        } else if (enrollmentType.equals(EnrollmentTypeEnumType.PORTAL.getName())) {
            previewer = new SharepointPreviewer(properties);
        } else {
            String message = "unsupported enrollment type";
            LOG.error(message);
            return;
        }
        
        try {
            HashMapConfiguration previewerConfig = new HashMapConfiguration();
            
            List<EnrollmentProperty> enrollmentProperties = createRealmProperties(cmd, previewer);
            Map<String, String[]> realmProperties = convertProperties(enrollmentProperties);            
            previewerConfig.setProperty(IPreviewer.ENROLLMENT_PROPERTIES_KEY, realmProperties);
            
            IComponentManager cm = ComponentManagerFactory.getComponentManager();
            cm.registerComponent(DictionaryGhost.COMP_INFO, true);
            IDictionary dictionary =cm.getComponent(DictionaryGhost.COMP_INFO);
            initEnrollmentSeed();
            previewerConfig.setProperty(IPreviewer.DICTIONARY_KEY, dictionary);
            
            EnrollmentGhost enrollment = new EnrollmentGhost(" ", dictionary);
            previewerConfig.setProperty(IPreviewer.ENROLLMENT_KEY, enrollment);
            
            previewerConfig.setProperty(IPreviewer.GUI_KEY, gui);
            
            EnrollmentChecker checker = createValueChecker(properties, previewer.getSuggestions());
            previewerConfig.setProperty(IPreviewer.VALUE_CHECKER_KEY, checker);
            
            gui.setVisible(true);
            
            previewer.init(previewerConfig);
            
            Timer t = createProgressBar(previewer);
            try {
                previewer.start();
                LOG.info(CollectionUtils.toString(previewer.getSuggestions().getMessages()));
            } finally {
                t.cancel();
                gui.setStatusMessage(null);
            }
            
            LOG.info("done");

            System.out.println("done, please check the log file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    private EnrollmentChecker createValueChecker(Properties properties, Suggestions suggestions)
            throws SecurityException, NoSuchMethodException {
        int dbStringLength;
        try {
            dbStringLength = Integer.parseInt(properties.getProperty(COLUMN_LENGTH_KEY));
        } catch (NumberFormatException e1) {
            dbStringLength = Integer.MAX_VALUE;
            String message = String.format(NOT_DEFINED_WARNING, COLUMN_LENGTH_KEY);
            System.err.println(message);
            LOG.warn(message);
        }
        
        String charsetName = properties.getProperty(ENCODING_KEY);
        if (charsetName == null || !Charset.isSupported(charsetName)) {
            String message = String.format(NOT_DEFINED_WARNING, ENCODING_KEY);
            System.err.println(message);
            LOG.warn(message);
            charsetName = null;
        }
	    
	    return new EnrollmentChecker(dbStringLength, charsetName, gui, suggestions);
	}
	
	private List<EnrollmentProperty> createRealmProperties(ICommandLine cmd, IPreviewer previewer) throws IOException, FileFormatException{
	    File connFile = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.ROOT_FILE_OPTIONS_ID);
        File defFile = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.DEFINITION_FILE_OPTIONS_ID);
        File filterFile = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.FILTER_FILE_OPTIONS_ID);
        File selectiveFolder = getValue(cmd, EnrollmentPreviewOptionDescriptorEnum.SELECTIVE_FILTER_FILE_OPTIONS_ID);
        
        return previewer.createRealmProperties(connFile, defFile, filterFile, selectiveFolder);
	}
	
	private void initEnrollmentSeed() throws SeedDataTaskException{
	    IComponentManager cm = ComponentManagerFactory.getComponentManager();
	    if(!cm.isComponentRegistered(DictionaryGhost.COMP_INFO.getName())){
	        throw new IllegalArgumentException("Dictionary must be initialized first");
	    }
	    
	    EnrollmentSeedDataTask enrollmentSeedDataTask = new EnrollmentSeedDataTask();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(ISeedDataTask.HIBERNATE_DATA_SOURCE_CONFIG_PARAM, new HibernateRepositoryMock());
        enrollmentSeedDataTask.setConfiguration(config);
        enrollmentSeedDataTask.setLog(LogFactory.getLog(EnrollmentSeedDataTask.class));
        enrollmentSeedDataTask.init();
        enrollmentSeedDataTask.execute();
	}
	
	private Map<String, String[]> convertProperties(List<EnrollmentProperty> properties) {
        Map<String, String[]> realmProperties = new HashMap<String, String[]>();
        
        Table enrollmentPropertiesTable = new Table();
        
        enrollmentPropertiesTable.addColumn("Name", 31);
        enrollmentPropertiesTable.addColumn("Value", 60);
        
        Collections.sort(properties, new Comparator<EnrollmentProperty>(){
            public int compare(EnrollmentProperty o1, EnrollmentProperty o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        for (EnrollmentProperty prop : properties) {
            String key = prop.getKey();
            String value = ArrayUtils.asString(prop.getValue());
            realmProperties.put(key, prop.getValue());

            if(key.equals("password")){
                value = StringUtils.maskString(value);
            }
            enrollmentPropertiesTable.addRow(key, value);
            gui.addProperty(key, value);
        }
        
        LOG.info("Enrollment Properties" + ConsoleDisplayHelper.NEWLINE
                + enrollmentPropertiesTable.getOutput());
        return realmProperties;
    }
    

	private Timer createProgressBar(final IPreviewer previewer) {
		Timer t = new Timer();
		final ConsoleStatus consoleStatus = new ConsoleStatus();
		consoleStatus.start();
		t.schedule(new TimerTask(){
			@Override
			public void run() {
				consoleStatus.set(previewer.getEntryCount());
				ConsoleDisplayHelper.redraw(consoleStatus);
				gui.setStatusMessage(consoleStatus.getOutput());
			}
			
		}, 0, 10000);
		return t;
	}

	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return descriptor;
	}
}
