/*
 * Created on Mar 25, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import junit.framework.TestCase;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/impl/TestDistinguishedName.java#1 $
 */

public class TestDistinguishedName extends TestCase {
	public void testCommaInCnFront(){
		DistinguishedName dn =
				new DistinguishedName("CN=\\,Chan Hor-kan,ou=users,dc=nextlabs,dc=com");
		
		assertTrue("CN".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("\\,Chan Hor-kan".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("CN=\\,Chan Hor-kan".equalsIgnoreCase(dn.getRDN().getAsString()));

		assertAfterCn(dn);
	}

	public void testCommaInCnMiddle(){
		DistinguishedName dn =
				new DistinguishedName("CN=Chan\\, Hor-kan,ou=users,dc=nextlabs,dc=com");
		
		assertTrue("CN".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("Chan\\, Hor-kan".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("CN=Chan\\, Hor-kan".equalsIgnoreCase(dn.getRDN().getAsString()));

		assertAfterCn(dn);
	}
	
	public void testCommaInCnEnd(){
		DistinguishedName dn =
				new DistinguishedName("CN=Chan Hor-kan\\,,ou=users,dc=nextlabs,dc=com");
		
		assertTrue("CN".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("Chan Hor-kan\\,".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("CN=Chan Hor-kan\\,".equalsIgnoreCase(dn.getRDN().getAsString()));

		assertAfterCn(dn);
	}
	
	public void testCnWithCommaOnly(){
		DistinguishedName dn =
				new DistinguishedName("CN=\\,,ou=users,dc=nextlabs,dc=com");
		
		assertTrue("CN".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("\\,".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("CN=\\,".equalsIgnoreCase(dn.getRDN().getAsString()));

		assertAfterCn(dn);
	}
	
	public void testCommaInCnKey(){
		DistinguishedName dn =
				new DistinguishedName("\\,CN=Chan Hor-kan,ou=users,dc=nextlabs,dc=com");
		
		assertTrue("\\,CN".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("Chan Hor-kan".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("\\,CN=Chan Hor-kan".equalsIgnoreCase(dn.getRDN().getAsString()));

		assertAfterCn(dn);
	}
	
	private void assertAfterCn(DistinguishedName dn) {
		dn = dn.getParentDN();
		assertNotNull(dn);
		assertTrue("ou".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("users".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("ou=users".equalsIgnoreCase(dn.getRDN().getAsString()));
		
		dn = dn.getParentDN();
		assertNotNull(dn);
		assertTrue("dc".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("nextlabs".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("dc=nextlabs".equalsIgnoreCase(dn.getRDN().getAsString()));
		
		dn = dn.getParentDN();
		assertNotNull(dn);
		assertTrue("dc".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("com".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("dc=com".equalsIgnoreCase(dn.getRDN().getAsString()));
	}
	
	public void testShortDN(){
		DistinguishedName dn = new DistinguishedName("a=b");
		
		assertTrue("a".equalsIgnoreCase(dn.getRDN().getRDNAttribute()));
		assertTrue("b".equalsIgnoreCase(dn.getRDN().getRDNValue()));
		assertTrue("a=b".equalsIgnoreCase(dn.getRDN().getAsString()));
		
		assertNull(dn.getParentDN());

	}
}
