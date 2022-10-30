/*
 * Created on Apr 29, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pdp/PDPEngineFactoryImpl.java#1 $:
 */

package com.nextlabs.openaz.pdp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.openaz.xacml.api.pdp.PDPEngine;
import org.apache.openaz.xacml.api.pdp.PDPEngineFactory;
import org.apache.openaz.xacml.util.FactoryException;

import static com.nextlabs.openaz.utils.Constants.ENGINE_NAME;
    
public class PDPEngineFactoryImpl extends PDPEngineFactory {
    private static final String DEFAULT_ENGINE_NAME = "com.nextlabs.openaz.pdp.EmbeddedPDPEngineImpl";
    
    @Override
    public PDPEngine newEngine() throws FactoryException {
        return newEngine(DEFAULT_ENGINE_NAME, null);
    }

    @Override
    public PDPEngine newEngine(Properties properties) throws FactoryException {
        String engineClassName = properties.getProperty(ENGINE_NAME);

        if (engineClassName == null) {
            engineClassName = DEFAULT_ENGINE_NAME;
        }

        return newEngine(engineClassName, properties);
    }

    private PDPEngine newEngine(String engineName, Properties properties) throws FactoryException {
        try {
            Class<?> clazz = Class.forName(engineName);

            Method getInstance = clazz.getMethod("getInstance", Properties.class);

            return (PDPEngine)getInstance.invoke(null, properties);
        } catch (ClassNotFoundException e) {
            throw new FactoryException("Unable to find PDP engine " + engineName, e);
        } catch (InvocationTargetException e) {
            throw new FactoryException("Error invoking method \"getInstance(Properties)\" in class " + engineName, e);
        } catch (NoSuchMethodException e) {
            throw new FactoryException("Can't find method \"getInstance(Properties)\" in class " + engineName, e);
        } catch (IllegalAccessException e) {
            throw new FactoryException("Error instantiating " + engineName, e);
        }
    }
}
