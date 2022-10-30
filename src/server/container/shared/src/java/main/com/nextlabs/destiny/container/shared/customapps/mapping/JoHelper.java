/*
 * Created on Mar 4, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps.mapping;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.digester.Digester;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/mapping/JoHelper.java#1 $
 */

public class JoHelper {
    @SuppressWarnings("unchecked")
    public static <T extends IamJO> T read(String xmlContent, T t) throws IOException, SAXException {
        Digester digester = new Digester();
        t.accept(digester);
        return (T) digester.parse(new StringReader(xmlContent));
    }

    
    public static Collection<SAXParseException> validateXml(
            String schemaLocation, 
            String nameSpace,
            String xml
    ) throws SAXNotRecognizedException, 
            SAXNotSupportedException, 
            SAXException,
            IOException {
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

//        String schemaLocation = JoHelper.class.getResource(schemaFilePath).toString();
        schemaLocation = schemaLocation.replaceAll(" ", "%20");
        parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                nameSpace + " " + schemaLocation);

        final Collection<SAXParseException> errors = new LinkedList<SAXParseException>();
        
        ErrorHandler errorHandler = new ErrorHandler(){
            public void error(SAXParseException exception) throws SAXException {
                errors.add(exception);
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                errors.add(exception);
            }

            public void warning(SAXParseException exception) throws SAXException {
                errors.add(exception);
            }
        };
        
        parser.setErrorHandler(errorHandler);
        InputSource is =new InputSource();
        is.setCharacterStream(new StringReader(xml));
        parser.parse(is);
        
        return errors;
    }
}
