/*
 * Created on Apr 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/SerializationUtils.java#1 $
 */
package com.bluejungle.framework.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Some handy functions for wrapping objects up as strings
 */
  
public class SerializationUtils {
    
    private static final Log LOG = LogFactory.getLog(SerializationUtils.class);

    private static class ObjectInputStreamWithClassLoader extends ObjectInputStream {
        private ClassLoader classLoader;

        public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
            return Class.forName(desc.getName(), false, classLoader);
        }
    }

    public static String wrapSerializable(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            return null;
        }

        return new BASE64Encoder().encodeBuffer(baos.toByteArray());
    }
    
    public static Serializable unwrapSerialized(String str) {
        Serializable ret = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(str)));
            ret = (Serializable)ois.readObject();
        } catch(IOException e) {
            LOG.info(str, e);
        } catch (ClassNotFoundException e) {
            LOG.info(str, e);
        }
                           
        return ret;
    }

    public static Serializable unwrapSerialized(String str, ClassLoader classLoader) {
        Serializable ret = null;
        try {
            ObjectInputStream ois = new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(str)), classLoader);

            ret = (Serializable)ois.readObject();
        } catch (IOException e) {
            LOG.info(str, e);
        } catch (ClassNotFoundException e) {
            LOG.info(str, e);
        }

        return ret;
    }

    public static String wrapExternalizable(Externalizable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            obj.writeExternal(oos);
            oos.flush();
        } catch (IOException e) {
            return null;
        }

        return new BASE64Encoder().encodeBuffer(baos.toByteArray());
    }

    public static void unwrapExternalized(String str, Externalizable obj) {
        Externalizable ret = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(str)));
            obj.readExternal(ois);
        } catch (IOException e) {
            LOG.info(str, e);
        } catch (ClassNotFoundException e) {
            LOG.info(str, e);
        }
    }

    public static void unwrapExternalized(String str, Externalizable obj, ClassLoader classLoader) {
        Externalizable ret = null;
        try {
            ObjectInputStream ois = new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(str)), classLoader);
            obj.readExternal(ois);
        } catch (IOException e) {
            LOG.info(str, e);
        } catch (ClassNotFoundException e) {
            LOG.info(str, e);
        }
    }
}
