/*
 * Created on Dec 5, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.pf.destiny.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.pf.destiny.services.UNCUtil;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/destiny/services/UNCUtilTest.java#1 $:
 */

public class UNCUtilTest extends TestCase {

    static final String hostName = "cayman";

    public UNCUtilTest(String arg0) {
        super(arg0);
    }

    public void testIsIpAddress() {
        System.out.println("Testing isIPAddress method");
        assertTrue(UNCUtil.isIpAddress("10.17.11.11"));
        assertTrue(UNCUtil.isIpAddress("1.2.3.4"));
        assertTrue(UNCUtil.isIpAddress("255.255.255.255"));
        assertFalse(UNCUtil.isIpAddress("255.255.2."));
        assertFalse(UNCUtil.isIpAddress("abcd"));
        assertFalse(UNCUtil.isIpAddress("mahe.bluejungle.com"));
        assertFalse(UNCUtil.isIpAddress("a.b.c.d"));
        System.out.println("END: Testing isIPAddress method");
    }
    
    public void testGetEquivalentFileNames () {
        System.out.println("Testing getEquivalentHostNames method");
        List<String> files = UNCUtil.getEquivalentHostNames("\\\\" + hostName + "\\share\\abc");

        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));
        
        files = UNCUtil.getEquivalentHostNames("\\\\" + hostName + ".bluejungle.com\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));

        //Second time should hit the cache
        files = UNCUtil.getEquivalentHostNames("\\\\" + hostName + "\\share\\abc");
        assertEquals ("Wrong number of filenames when using cache", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));

        files = UNCUtil.getEquivalentHostNames("\\\\" + hostName + ".bluejungle.com\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));

        // Illegal format - return just the original name
        files = UNCUtil.getEquivalentHostNames("http:" + hostName + ".bluejungle.com\\share\\abc");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("http:" + hostName + ".bluejungle.com\\share\\abc"));

        // Various things that don't indicate a server name
        files = UNCUtil.getEquivalentHostNames("c:/foo/bar/baz.pdf");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("c:/foo/bar/baz.pdf"));

        files = UNCUtil.getEquivalentHostNames("file:\\\\fruit\\banana.txt");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("file:\\\\fruit\\banana.txt"));

        files = UNCUtil.getEquivalentHostNames("device:\\\\usb\\banana");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("device:\\\\usb\\banana"));

        // Long UNC
        files = UNCUtil.getEquivalentHostNames("\\\\?\\c:\\foo.txt");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("c:\\foo.txt"));

        files = UNCUtil.getEquivalentHostNames("\\\\?\\unc\\" + hostName + "\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));

        // Device UNC
        files = UNCUtil.getEquivalentHostNames("\\\\.\\foo\\bar");
        assertEquals ("Wrong number of filenames", 1, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\.\\foo\\bar"));
        
        // Other schemes
        files = UNCUtil.getEquivalentHostNames("http:\\\\" + hostName + ".bluejungle.com\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("http:\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("http:\\\\" + hostName + "\\share\\abc"));

        files = UNCUtil.getEquivalentHostNames("http:\\\\" + hostName + ".bluejungle.com:8080\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("http:\\\\" + hostName + ".bluejungle.com:8080\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("http:\\\\" + hostName + ":8080\\share\\abc"));

        // Forward slashes are the same as backward slashes
        files = UNCUtil.getEquivalentHostNames("http://" + hostName + ".bluejungle.com:8080/share/abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("http://" + hostName + ".bluejungle.com:8080/share/abc"));
        assertTrue ("FileNames incorrect", files.contains("http://" + hostName + ":8080/share/abc"));

        files = UNCUtil.getEquivalentHostNames("ftp:\\\\" + hostName + ".bluejungle.com\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("ftp:\\\\" + hostName + ".bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("ftp:\\\\" + hostName + "\\share\\abc"));

        // Completely unknown schemes should work as well (see RFC 1149)
        files = UNCUtil.getEquivalentHostNames("pigeon:\\\\" + hostName + ":8080\\share\\abc");
        assertEquals ("Wrong number of filenames", 2, files.size());
        assertTrue ("FileNames incorrect", files.contains("pigeon:\\\\" + hostName + ".bluejungle.com:8080\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("pigeon:\\\\" + hostName + ":8080\\share\\abc"));

        // test accessing domain based shares. this test will fail if
        // bluejungle.com resolves to something other than one of the known domain servers
        files = UNCUtil.getEquivalentHostNames("\\\\bluejungle.com\\share\\abc");

        assertEquals ("Wrong number of filenames", 4, files.size());
        assertTrue ("FileNames incorrect", files.contains("\\\\bluejungle.com\\share\\abc"));
        assertTrue ("FileNames incorrect", files.contains("\\\\bluejungle\\share\\abc"));
        // We have several domain servers
        assertTrue ("FileNames incorrect", 
                    files.contains("\\\\pukapuka.bluejungle.com\\share\\abc") ||
                    files.contains("\\\\nxt-dc01.bluejungle.com\\share\\abc") ||
                    files.contains("\\\\nxt-dc02.bluejungle.com\\share\\abc") ||
                    files.contains("\\\\nxt-dc03.bluejungle.com\\share\\abc")   );
        assertTrue ("FileNames incorrect", 
                    files.contains("\\\\pukapuka\\share\\abc") ||
                    files.contains("\\\\nxt-dc01\\share\\abc") ||
                    files.contains("\\\\nxt-dc02\\share\\abc") ||
                    files.contains("\\\\nxt-dc03\\share\\abc")   );

        try {
            InetAddress addr = InetAddress.getByName(hostName);
            String filename = "\\\\" + addr.getHostAddress() + "\\share\\abc";
            files = UNCUtil.getEquivalentHostNames(filename);
            assertEquals ("Wrong number of filenames", 3, files.size());
            assertTrue ("FileNames incorrect", files.contains(filename));
            assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + ".bluejungle.com\\share\\abc"));
            assertTrue ("FileNames incorrect", files.contains("\\\\" + hostName + "\\share\\abc"));
                        
        } catch (UnknownHostException e) {
            fail("Cannot find " + hostName + ". This test will fail but can be ignored.");
        }
        
        
        System.out.println("END: Testing getEquivalentHostNames method");
        
    }
    
}
