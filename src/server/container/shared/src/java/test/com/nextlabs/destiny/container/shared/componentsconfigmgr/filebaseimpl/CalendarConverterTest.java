/*
 * Created on Jun 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.util.Calendar;

import org.apache.commons.beanutils.ConversionException;

import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.CalendarConverter;

import junit.framework.TestCase;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/componentsconfigmgr/filebaseimpl/CalendarConverterTest.java#1 $
 */

public class CalendarConverterTest extends TestCase{
    private static final Class NOT_USING = null;
    CalendarConverter converter;
    Calendar c = Calendar.getInstance();
    

    @Override
    protected void setUp() throws Exception {
        converter = new CalendarConverter();
    }

    public void testBadTime(){
        assertFail("");
        assertFail("abc");
        assertFail("11:00 am");
        assertFail("1 o'clock");
    }
    
    public void testHHmm(){
        // HH:mm
        c.clear();
        c.set(Calendar.HOUR_OF_DAY, 11);
        c.set(Calendar.MINUTE, 2);
        assertGood("11:02", c);
        
        c.clear();
        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 18);
        assertGood("15:18", c);
        
        
    }
    
    public void testHHmmOver(){
        assertFail("27:18");
        
        assertFail("2:91");
    }
    
    public void testHHmmss(){
        // HH:mm:ss
        c.clear();
        c.set(Calendar.HOUR_OF_DAY, 11);
        c.set(Calendar.MINUTE, 2);
        c.set(Calendar.SECOND, 1);
        assertGood("11:02:01", c);
        
        c.clear();
        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 18);
        c.set(Calendar.SECOND, 2);
        assertGood("15:18:02", c);
            
    }
    
    public void testHHmmssOver(){
        assertFail("2:91:04");
        
        assertFail("3:59:62");
    }
    
    private void assertFail(String s){
        assert s != null;
        try {
            converter.convert(NOT_USING, s);
            fail(s + " should fail");
        } catch (ConversionException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains(s));
        }
    }
    
    private void assertGood(String s, Calendar c) throws ConversionException{
        assert s != null;
        Object o = converter.convert(NOT_USING, s);
        if( o instanceof Calendar){
            String message = c.getTime().toString() + " vs " + ((Calendar)o).getTime().toString();
            assertEquals(message, c, o);
        }else{
            fail(o.getClass() + " is not a Calendar");
        }
    }
}
