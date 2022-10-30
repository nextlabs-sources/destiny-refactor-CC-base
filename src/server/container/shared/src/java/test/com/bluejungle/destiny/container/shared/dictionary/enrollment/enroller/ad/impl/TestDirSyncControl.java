package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncControl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DeletedControl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncResponseControl;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

public class TestDirSyncControl extends TestCase {

	/*
     * Constants
     */
    private static final String ROOT_DN = "dc=test,dc=bluejungle,dc=com";
    private static final int PORT = 389;
    private static final String SERVER = "cuba";
    private static final String UPN = "jimmy.carter@test.bluejungle.com";
    private static final String PWD = "jimmy.carter";
    //private static final String SEARCH_SPEC = "(|(objectCategory=User)(objectCategory=Computer))";
    private static final String SEARCH_SPEC = "objectclass=*";

    private static final String PERSISTED_COOKIE_FILE = "dirsync_cookie.dat";

    private LDAPConnection connection;
    private byte[] dirSyncCookie;
	
	
	public TestDirSyncControl(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
        // Setup the LDAP connection:
        this.connection = new LDAPConnection();
        this.connection.connect(SERVER, PORT);
        this.connection.bind(3, UPN, PWD.getBytes());

        // Parse the cookie file:
        File cookieFile = new File(PERSISTED_COOKIE_FILE);
        if (!cookieFile.exists()) {
            cookieFile.createNewFile();
        }
        FileInputStream is = new FileInputStream(cookieFile);
        this.dirSyncCookie = new byte[new Long(cookieFile.length()).intValue()];
        is.read(this.dirSyncCookie);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
        // Persist the dirsync cookie:
        File cookieFile = new File(PERSISTED_COOKIE_FILE);
        FileOutputStream os = new FileOutputStream(cookieFile);        
        os.write(this.dirSyncCookie);
        os.close();
        this.connection.disconnect();
	}

    /**
     * Tests that the DirSyncControl can be sent on requests to Active Directory
     * without encountering errors from the server
     * 
     * @throws Exception
     */
    public void testControl() throws Exception {
        try {
            DirSyncControl dirSyncControl = new DirSyncControl();
            DeletedControl  deletedControl = new DeletedControl();
            dirSyncControl.updateCookie(this.dirSyncCookie);

            LDAPSearchConstraints constraints = new LDAPSearchConstraints();
            constraints.setControls(deletedControl);
            constraints.setControls(dirSyncControl);
            constraints.setMaxResults(1);
            
            String returnedAtts[]={};
            LDAPSearchResults results = this.connection.search(ROOT_DN, LDAPConnection.SCOPE_SUB, SEARCH_SPEC, returnedAtts, true, constraints);
            
            if (results.hasMore()) {
                while (results.hasMore()) {
                    LDAPEntry entry = results.next();
                    System.out.println("Entry changed: '" + entry.getDN() );
                    Iterator enums = entry.getAttributeSet().iterator();
                    while ( enums.hasNext()) {
                    	System.out.println(" Attr :" + enums.next());
                    }
                    //System.out.println("Entry changed: '" + entry.getDN() + "'");
                }
            } else {
                System.out.println("{/} - Nothing changed on this iteration");
            }
            
            // Retrieve dir-sync reponse control:
            LDAPControl[] responseControls = results.getResponseControls();
            //assertNotNull("Response controls should exist", responseControls);
            //assertTrue("Reponse controls should exist", responseControls.length > 0);
            DirSyncResponseControl dirSyncResponseControl = null;            
            if ( responseControls != null ) {
            for (int j = 0; j < responseControls.length; j++) {
                if (responseControls[j] instanceof DirSyncResponseControl) {
                    dirSyncResponseControl = (DirSyncResponseControl) responseControls[j];
                }
            }
            // Extract cookie from dir-sync control and re-use:
            this.dirSyncCookie = dirSyncResponseControl.getCookie();
            }
        } catch (Exception e) {
        	System.out.println("Error:" + e);
            throw e;
        }
    }
}
