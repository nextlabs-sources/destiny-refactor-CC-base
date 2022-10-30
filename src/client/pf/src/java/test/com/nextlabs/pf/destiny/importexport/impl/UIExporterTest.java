package com.nextlabs.pf.destiny.importexport.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;


import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.ExportException;
import com.nextlabs.pf.destiny.importexport.ExportFile;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.impl.UIExporter;
import com.nextlabs.pf.destiny.importexport.mapping.User;

/**
 * assume import works, otherwise most of test will failed.
 * @author hchan
 */
public class UIExporterTest extends ImportExportSharedTest {
    private UIExporter exporter;
    private File exportFile;
 
    public UIExporterTest() throws LoginException{
        super();
    }
 
 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        exporter = new UIExporter();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if (exportFile != null) {
            if (!exportFile.delete()) {
                //TODO log warning
                System.out.println("test file " + exportFile + " is not deleted.");
            }
        }
    }


    public void testExportToXML() throws ExportException, IOException {
        ExportFile testFile = new ExportFile();
        exportFile = new File(testFolder, "exportToXMLtest.xml");
  
        final ExportEntity exportEntity1 = new ExportEntity("Security Policy", "POLICY", "insert policy pql here");
        testFile.addExportEntities(exportEntity1);
        final ExportEntity exportEntity2 = new ExportEntity("Illegal Actions", "ACTION", "insert action pql here");
        testFile.addExportEntities(exportEntity2);
        final User user1 = new User("JohnDoe", "jd@bluejungle.com", "JDSID-12345", 56789);
        testFile.addUser(user1);
        final User user2 = new User("JaneDoe", "janed@bluejungle.com", "JDSID-24780", 98765);
        testFile.addUser(user2);
  
        exporter.exportToXML(testFile, exportFile);
  
        final String[] expectedOutput = new String[]{
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<export-file>",
            "<export-entities name=\"" + exportEntity1.getName() + "\">",
            "<type>" + exportEntity1.getType() + "</type>",
            "<pql>" + exportEntity1.getPql() + "</pql>",
            "</export-entities>",
            "<export-entities name=\""+exportEntity2.getName()+"\">",
            "<type>" + exportEntity2.getType() + "</type>",
            "<pql>" + exportEntity2.getPql() + "</pql>",
            "</export-entities>",
            "<users>",
            "<name>" + user1.getName() + "</name>",
            "<login>" + user1.getLogin() + "</login>",
            "<sid>" + user1.getSid() + "</sid>",
            "<id>" + user1.getId() + "</id>",
            "</users>",
            "<users>",
            "<name>" + user2.getName() + "</name>",
            "<login>" + user2.getLogin() + "</login>",
            "<sid>" + user2.getSid() + "</sid>",
            "<id>" + user2.getId() + "</id>",
            "</users>",
            "</export-file>",
        };
   
        BufferedReader bis = new BufferedReader(new FileReader(exportFile));
        String line;
        int i = 0;
        while ((line = bis.readLine()) != null) {
            assertEquals(expectedOutput[i++], line.trim());
        }
        bis.close();
    }

 
    public void testExportPolicies() throws Exception {
        File testFile = new File(srcFilesFolder, POLICY_XML_3);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(30, fileContents.size());
        IImportState importState = importer.doImport(fileContents);
     
        Collection<IImportConflict> conflicts = importState.getConflicts();
        assertTrue("Import should not have any conflict",conflicts.isEmpty());
     
        importer.commitImport();
     
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType("%",
                                                                                          EntityType.POLICY, false);
        assertNotNull(policies);
        assertTrue(!policies.isEmpty());
        Collection required = exporter.prepareForExport(policies);
        assertTrue(!required.isEmpty());
        exporter.prepareForExport(policies);
  
        exportFile = new File(testFolder, "exportPolicies.xml");
        exporter.executeExport(exportFile);
  
        printFile(exportFile);
    }
 
    public void testExportComponents() throws Exception {

        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType("%",
                                                                                          EntityType.COMPONENT, false);
        assertNotNull(policies);
        assertTrue(!policies.isEmpty());
        Collection required = exporter.prepareForExport(policies);
        assertTrue(!required.isEmpty());
        exporter.prepareForExport(policies);
  
        exportFile = new File(testFolder, "exportComponents.xml");
        exporter.executeExport(exportFile);
    }
 
    public void testExport() throws Exception {

        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "%Sales Forecast Handling%", EntityType.POLICY, false);
        assertNotNull(policies);
        assertTrue(!policies.isEmpty());
        Collection required = exporter.prepareForExport(policies);
        assertTrue(!required.isEmpty());
        exporter.prepareForExport(policies);
  
        exportFile = new File(testFolder, "testPolicy4.xml");
        exporter.executeExport(exportFile);
    }
 
    private void printFile(File file) throws IOException{
        BufferedReader bis = new BufferedReader(new FileReader(file));
        String line;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
        }
        bis.close();
    }
}
