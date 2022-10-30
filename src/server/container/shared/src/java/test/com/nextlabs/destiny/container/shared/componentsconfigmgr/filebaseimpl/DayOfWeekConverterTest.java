/*
 * Created on Jun 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.util.Calendar;

import junit.framework.TestCase;

import org.apache.commons.beanutils.ConversionException;

import com.bluejungle.destiny.server.shared.configuration.type.DayOfWeek;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.DayOfWeekConverter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/componentsconfigmgr/filebaseimpl/DayOfWeekConverterTest.java#1 $
 */

public class DayOfWeekConverterTest extends TestCase{
    private static final Class NOT_USING = null;
    DayOfWeekConverter converter;
    

    @Override
    protected void setUp() throws Exception {
        converter = new DayOfWeekConverter();
    }

    public void testBadTime(){
        assertFail("");
        assertFail("" + Calendar.SATURDAY);
        assertFail("abc");
        assertFail("first day of week");
        assertFail("FR");
    }
    
    public void testEEE(){
        assertGood("SUN", Calendar.SUNDAY);
        assertGood("MON", Calendar.MONDAY);
        assertGood("TUE", Calendar.TUESDAY);
        assertGood("WED", Calendar.WEDNESDAY);
        assertGood("THU", Calendar.THURSDAY);
        assertGood("FRI", Calendar.FRIDAY);
        assertGood("SAT", Calendar.SATURDAY);
    }
    
    public void testEEEMixedCase(){
        assertGood("sUN", Calendar.SUNDAY);
        assertGood("MoN", Calendar.MONDAY);
        assertGood("TUe", Calendar.TUESDAY);
        assertGood("wed", Calendar.WEDNESDAY);
        assertGood("Thu", Calendar.THURSDAY);
        assertGood("fri", Calendar.FRIDAY);
        assertGood("Sat", Calendar.SATURDAY);
    }
    
    public void testEEEE(){
        assertGood("SUNDAY", Calendar.SUNDAY);
        assertGood("MONDAY", Calendar.MONDAY);
        assertGood("TUESDAY", Calendar.TUESDAY);
        assertGood("WEDNESDAY", Calendar.WEDNESDAY);
        assertGood("THURSDAY", Calendar.THURSDAY);
        assertGood("FRIDAY", Calendar.FRIDAY);
        assertGood("SATURDAY", Calendar.SATURDAY);
    }
    
    public void testEEEEMixedCase(){
        assertGood("SuNDAY", Calendar.SUNDAY);
        assertGood("monday", Calendar.MONDAY);
        assertGood("TUesDAY", Calendar.TUESDAY);
        assertGood("wedneSDAY", Calendar.WEDNESDAY);
        assertGood("THursDAY", Calendar.THURSDAY);
        assertGood("FRIday", Calendar.FRIDAY);
        assertGood("SaTURDaY", Calendar.SATURDAY);
    }
    
    public void testEEEEWrongSpelling(){
        assertGood("SuNDAyY", Calendar.SUNDAY);
        assertGood("mondday", Calendar.MONDAY);
        assertGood("TUesDaAY", Calendar.TUESDAY);
        assertGood("wednextlabs", Calendar.WEDNESDAY);
        assertGood("THurssssDAY", Calendar.THURSDAY);
        assertGood("FRIJa vvvvvva", Calendar.FRIDAY);
        assertGood("SaTthat'sveryfunny", Calendar.SATURDAY);
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
    
    private void assertGood(String s, int c) throws ConversionException{
        assert s != null;
        Object o = converter.convert(NOT_USING, s);
        if( o instanceof DayOfWeek){
            assertEquals(o, c);
        }else{
            fail(o.getClass() + " is not a Calendar");
        }
    }
}
