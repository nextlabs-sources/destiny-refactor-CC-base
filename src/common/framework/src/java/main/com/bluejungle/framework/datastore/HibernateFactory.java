/*
 * Created on Nov 11, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.framework.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import net.sf.hibernate.HibernateException;

import com.bluejungle.framework.datastore.HibernateFactoryException;

/**
 * @author rbelkin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/datastore/HibernateFactory.java#1 $
 * 
 * This class creates Hibernate sessions
 */
public class HibernateFactory {

    private static HashMap factories = new HashMap();
    private static String defCfgDir;

    /**
     * Initialization
     * 
     * @param cfgDir
     *            directory where the Hibernate configuration file is created
     * @throws HibernateFactoryException
     *             if init fails
     */
    public static void init(String cfgDir) throws HibernateFactoryException {

        defineFactory("default", cfgDir);
        defCfgDir = cfgDir;
    }

    /**
     * Defines a factory object
     * 
     * @param name
     *            name of the factory
     * @param cfgDir
     *            directory containing the configuration file
     * @throws HibernateFactoryException
     */
    public static void defineFactory(String name, String cfgDir) throws HibernateFactoryException {
        defineFactory(name, cfgDir, null, null);
    }

    /**
     * Defines a factory
     * 
     * @param name
     *            factory name
     * @param cfgDir
     *            directory containing the configuration file
     * @param cfgFile
     *            configuration file name
     * @param propFile
     *            property file name
     * @throws HibernateFactoryException
     */
    public static synchronized void defineFactory(String name, String cfgDir, String cfgFile, String propFile) throws HibernateFactoryException {
        File cfgDirFile = null;

        if (cfgDir != null) {
            cfgDirFile = new File(cfgDir);
            if (!cfgDirFile.isDirectory() || !cfgDirFile.exists()) {
                throw new HibernateFactoryException();
            }
        }

        File cfgFileFile = null;

        if (cfgFile == null) {
            if (cfgDirFile == null) {
                if (defCfgDir == null) {
                    throw new HibernateFactoryException();
                }
                cfgFileFile = new File(defCfgDir + File.separatorChar + "hibernate.cfg.xml");
            } else {
                cfgFileFile = new File(cfgDirFile.getAbsolutePath() + File.separatorChar + "hibernate.cfg.xml");
            }

            if (!cfgFileFile.canRead()) {
                throw new HibernateFactoryException();
            }
        } else {
            cfgFileFile = new File(cfgFile);
            if (!cfgFileFile.canRead()) {
                if (cfgDirFile != null) {
                    cfgFileFile = new File(cfgDirFile.getAbsolutePath() + File.separator + cfgFile);
                } else if (defCfgDir != null) {
                    cfgFileFile = new File(defCfgDir + File.separator + cfgFile);
                } else {
                    throw new HibernateFactoryException();
                }
                if (!cfgFileFile.canRead()) {
                    throw new HibernateFactoryException();
                }
            }
        }

        File propFileFile = null;

        if (propFile == null) {
            if (cfgDirFile == null) {
                if (defCfgDir == null) {
                    throw new HibernateFactoryException();
                }
                propFileFile = new File(defCfgDir + File.separatorChar + "hibernate.properties");
            } else {
                propFileFile = new File(cfgDirFile.getAbsolutePath() + File.separatorChar + "hibernate.properties");
            }

            if (!propFileFile.canRead()) {
                throw new HibernateFactoryException();
            }
        } else {
            propFileFile = new File(propFile);
            if (!propFileFile.canRead()) {
                if (cfgDirFile != null) {
                    propFileFile = new File(cfgDirFile.getAbsolutePath() + File.separator + propFile);
                } else if (defCfgDir != null) {
                    propFileFile = new File(defCfgDir + File.separator + cfgFile);
                } else {
                    throw new HibernateFactoryException();
                }
                if (!cfgFileFile.canRead()) {
                    throw new HibernateFactoryException();
                }
            }
        }

        Properties hibProps = null;

        if (propFileFile != null) {

            hibProps = new Properties();

            try {
                hibProps.load(new FileInputStream(propFileFile));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new HibernateFactoryException();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new HibernateFactoryException();
            }
        }

        HibernateSessionFactory sf;
        try {
            sf = new HibernateSessionFactory(cfgFileFile, hibProps);
            factories.put(name, sf);
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            HibernateFactoryException fe = new HibernateFactoryException();
            fe.initCause(e);
            e.printStackTrace();
            throw fe;
        }
    }

    /**
     * Return the default Hibernate session
     * 
     * @return the default Hibernate session
     */
    public static HibernateSessionFactory getHibernateSessionFactory() {

        return getHibernateSessionFactory("default");
    }

    /**
     * Return an Hibernate session factory
     * 
     * @param type
     *            name of the factory
     * @return the Hibernate session factory
     */
    public static synchronized HibernateSessionFactory getHibernateSessionFactory(String type) {
        return (HibernateSessionFactory) factories.get(type);
    }
}