/*
 * Created on Dec 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen.tests;

import java.io.PrintWriter;

import javax.wsdl.Definition;

import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.Emitter;

import com.bluejungle.destiny.wsgen.SecureDeployWriter;

/**
 * This is a dummy secure deploy writer used to test the real secure deploy
 * writer. It inherit the secure deploy writer in order to test some internal
 * functionnality of that class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/wsgen/com/bluejungle/destiny/wsgen/tests/MockSecureDeployWriter.java#1 $:
 */

public class MockSecureDeployWriter extends SecureDeployWriter {

    /**
     * Default Constructor
     */
    public MockSecureDeployWriter() {
        super(null, null, null);
    }

    /**
     * Constructor
     * 
     * @param emitter
     * @param def
     * @param table
     */
    public MockSecureDeployWriter(Emitter emitter, Definition def, SymbolTable table) {
        super(emitter, def, table);
    }

    /**
     * This function is called to test the custom handler chain writter
     * 
     * @param pw
     *            print writer
     */
    public void testCustomWriting(PrintWriter pw) {
        this.writeHandlerChain(pw);
    }

}