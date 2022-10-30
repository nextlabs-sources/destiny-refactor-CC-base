/*
 * Created on Jun 8, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/test/com/bluejungle/destiny/container/dcc/CalendarConverter.java#1 $
 */

public class CalendarConverter implements Converter {
    
    private static final Map<String, SimpleDateFormat> POSSIBLE_FORMATS;
    static{
        POSSIBLE_FORMATS = new HashMap<String, SimpleDateFormat>();
        POSSIBLE_FORMATS.put("(([0-1][0-9])|(2[0-3])):[0-5][0-9]", new SimpleDateFormat("HH:mm"));
        POSSIBLE_FORMATS.put("(([0-1][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9]", new SimpleDateFormat("HH:mm:ss"));
    }
    
    private final Map<String, SimpleDateFormat> formats;
    
    public CalendarConverter() {
        this(POSSIBLE_FORMATS);
    }
    
    public CalendarConverter(Map<String, SimpleDateFormat> formats) {
        this.formats = formats;
    }

    public Object convert(Class type, Object value) {
        if (value == null) {
            throw new ConversionException("No value specified");
        }

        if (value instanceof Calendar) {
            return value;
        } else if (value instanceof Date) {
            Calendar c = Calendar.getInstance();
            c.setTime((Date) value);
            return c;
        } else {
            Date d = null;
            for(Map.Entry<String, SimpleDateFormat> format : formats.entrySet()){
                if (Pattern.matches(format.getKey(), value.toString())) {
                    try {
                        d = format.getValue().parse(value.toString());
                        break;
                    } catch (ParseException e) {
                        //ignore
                    }
                }
            }
            
            if (d == null) {
                throw new ConversionException("Unparseable date: " + value);
            }
            
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        }
    }

}
