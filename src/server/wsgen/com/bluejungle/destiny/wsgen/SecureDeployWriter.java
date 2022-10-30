/*
 * Created on Dec 16, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;

import org.apache.axis.constants.Use;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.JavaDeployWriter;

/**
 * This writer generates a deployment file (WSDD) that contains a handler chain
 * for security. This allows web services to be secured. Based on the parameters
 * of the WSDL2Java task, the writer insert either the certificate checker
 * handler or the API authentication handler and/or the authentication handler.
 * The API authentication handler implicitely does the certificate checking, so
 * it does not need to be placed behind the certificate checker. This also helps
 * performance.
 * 
 * @author ihanen
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/wsgen/com/bluejungle/destiny/wsgen/SecureDeployWriter.java#1 $:
 */

public class SecureDeployWriter extends JavaDeployWriter {

    private static final String REQUEST_FLOW_BEGIN_TAG = "\t<requestFlow>";
    private static final String REQUEST_FLOW_END_TAG = "\t</requestFlow>";
    private static final String RESPONSE_FLOW_BEGIN_TAG = "\t<responseFlow>";
    private static final String RESPONSE_FLOW_END_TAG = "\t</responseFlow>";

    private String trustedCertificates;
    private Map accessList;
    private String userAuthRequired;
    private String clientApplication;

    /**
     * Constructor
     * 
     * @param emitter
     *            emitter
     * @param def
     *            definition
     * @param table
     *            symbol table
     */
    public SecureDeployWriter(Emitter emitter, Definition def, SymbolTable table) {
        super(emitter, def, table);
        this.trustedCertificates = SecureWsdl2javaAntTask.getTrustedCallers();
        this.accessList = SecureWsdl2javaAntTask.getAccessList();
        this.userAuthRequired = SecureWsdl2javaAntTask.getUserAuthRequired();
        this.clientApplication = SecureWsdl2javaAntTask.getClientApplication();
    }

    /**
     * This function insert the regular deployment information, and adds a
     * special chain in the request flow. This allows to insert a few extra
     * hanlders in the regular handler chain.
     * 
     * @see org.apache.axis.wsdl.toJava.JavaDeployWriter#writeDeployTypes(java.io.PrintWriter,
     *      javax.wsdl.Binding, boolean, boolean, org.apache.axis.enum.Use)
     */
    protected void writeDeployTypes(PrintWriter pw, Binding binding, boolean hasLiteral, boolean hasMIME, Use use) throws IOException {
        super.writeDeployTypes(pw, binding, hasLiteral, hasMIME, use);
        writeHandlerChain(pw);
    }

    /**
     * Insert a handler chain in the WSDD file. The configuration parameters for
     * this handler chain are retrieved from the Ant task parameters. Based on
     * which parameters are used, different handlers are inserted in the chain.
     * 
     * @param pw
     *            printWriter to the WSDD file
     */
    protected void writeHandlerChain(PrintWriter pw) {
        pw.println(REQUEST_FLOW_BEGIN_TAG);
        if (this.trustedCertificates != null) {
            insertCertificateChecker(pw);
        } else if (this.accessList != null) {
            insertAPIAuthChecker(pw);
        }

        if ((this.userAuthRequired != null) && (this.userAuthRequired.equalsIgnoreCase("true"))) {
            insertUserAuthRequestFlowHandler(pw);
        }

        pw.println(REQUEST_FLOW_END_TAG);

        pw.println(RESPONSE_FLOW_BEGIN_TAG);
        if ((this.userAuthRequired != null) && (this.userAuthRequired.equalsIgnoreCase("true"))) {
            insertUserAuthResponseFlowHandler(pw);
        }
        pw.println(RESPONSE_FLOW_END_TAG);
    }

    /**
     * This function inserts an API authorization handler in the request flow.
     * Also, the access list given as a parameter to the Ant task is parsed and
     * processed.
     * 
     * @param pw
     *            printWriter to the WSDD file.
     */
    protected void insertAPIAuthChecker(PrintWriter pw) {
        pw.println("\t<handler type=\"java:com.bluejungle.destiny.server.security.APIAuthChecker\">");
        Iterator it = this.accessList.keySet().iterator();
        while (it.hasNext()) {
            String keyName = (String) it.next();
            String keyValue = (String) this.accessList.get(keyName);
            pw.println("\t\t<parameter name=\"" + keyName + "\" value=\"" + keyValue + "\">");
            pw.println("\t\t</parameter>");
        }
        pw.println("\t</handler>");
    }

    /**
     * This function inserts a certificate checker handler in the request flow.
     * 
     * @param pw
     *            printWriter for the WSDD file.
     */
    protected void insertCertificateChecker(PrintWriter pw) {
        pw.println("\t<handler type=\"java:com.bluejungle.destiny.server.security.CertificateChecker\">");
        pw.println("\t\t<parameter name=\"trustedCerts\" value=\"" + this.trustedCertificates + "\">");
        pw.println("\t\t</parameter>");
        pw.println("\t</handler>");
    }

    /**
     * This function inserts a user authentication handler in the request flow.
     * 
     * @param pw
     *            printWriter for the WSDD file.
     */
    protected void insertUserAuthRequestFlowHandler(PrintWriter pw) {
        pw.println("\t<handler type=\"java:com.bluejungle.destiny.container.shared.securesession.service.axis.AuthenticationRequestFlowHandler\" >");
        pw.println("\t\t<parameter name=\"clientApplication\" value=\"" + this.clientApplication + "\">");
        pw.println("\t\t</parameter>");
        pw.println("\t</handler>");
    }

    /**
     * This function inserts a user authentication handler in the response flow.
     * 
     * @param pw
     *            printWriter for the WSDD file.
     */
    protected void insertUserAuthResponseFlowHandler(PrintWriter pw) {
        pw.println("\t<handler type=\"java:com.bluejungle.destiny.container.shared.securesession.service.axis.AuthenticationResponseFlowHandler\">");
        pw.println("\t</handler>");
    }
}