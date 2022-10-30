/*
 * Created on Mar 5, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.File;

import junit.framework.TestCase;

import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/customapps/ExternalApplicationLoaderTest.java#1 $
 */

public class ExternalApplicationLoaderTest extends TestCase{
    static File testFolder;
    static File customAppsFolder;
    
    ExternalApplicationLoader loader;
    
    @Override
    protected void setUp() throws Exception {
        if(testFolder == null){
            String srcRootDir = System.getProperty("src.root.dir");
            if (srcRootDir == null) {
                throw new IllegalArgumentException("src.root.dir is required");
            }
            testFolder = new File(srcRootDir, "server/container/shared/test_files");
            customAppsFolder = new File(testFolder, "customApps");
        }
        
        loader = new ExternalApplicationLoader();
    }
    
    public void test1(){
        loader.read(new File(customAppsFolder, "app1"));
    }
    
    public void testNormal() throws Exception {
        CustomAppDO customAppDO = loader.read(new File(customAppsFolder, "app1/testApp.jar"));
        assertNotNull(customAppDO);
    }
    
    public void testFileNotFound() {
        try {
            loader.read(new File(customAppsFolder, "app1/testAppDNE.jar"));
            fail();
        } catch (InvalidCustomAppException e) {
            System.out.println(e);
            assertNotNull(e);
        }
    }
    
    public void testInvalidJar() {
        try {
            loader.read(new File(customAppsFolder, "app1/invalidJar.jar"));
            fail();
        } catch (InvalidCustomAppException e) {
            System.out.println(e);
            assertNotNull(e);
        }
    }
    
    public void testIncompleteReportJar() {
        try {
            loader.read(new File(customAppsFolder, "app1/incompleteReportApp.jar"));
            fail();
        } catch (InvalidCustomAppException e) {
            System.out.println(e);
            assertNotNull(e);
        }
    }
}
