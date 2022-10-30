/*
 * Created on Dec 16, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen;

import javax.wsdl.Definition;

import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.JavaBuildFileWriter;
import org.apache.axis.wsdl.toJava.JavaDefinitionWriter;
import org.apache.axis.wsdl.toJava.JavaGeneratorFactory;
import org.apache.axis.wsdl.toJava.JavaUndeployWriter;

/**
 * This is the class for the custom WSDD generator. This generator class
 * registers custom writers for the WSDD file. The custom writers insert the
 * relevant handlers and their configuration based on the ant task parameters.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/wsgen/com/bluejungle/destiny/wsgen/SecureServiceGenerator.java#1 $:
 */

public class SecureServiceGenerator extends JavaGeneratorFactory {

    /**
     * Constructor
     *  
     */
    public SecureServiceGenerator() {
        super();
    }

    /**
     * Constructor
     * 
     * @param emit
     *            emitter for the service generator
     */
    public SecureServiceGenerator(Emitter emit) {
        super(emit);
    }

    /**
     * This function registers the various writer for the java and WSDD file
     * 
     * @see org.apache.axis.wsdl.toJava.JavaGeneratorFactory#addDefinitionGenerators()
     */
    protected void addDefinitionGenerators() {
        addGenerator(Definition.class, JavaDefinitionWriter.class);
        //Use our class here...
        addGenerator(Definition.class, SecureDeployWriter.class);
        addGenerator(Definition.class, JavaUndeployWriter.class);
        addGenerator(Definition.class, JavaBuildFileWriter.class);
    }
}