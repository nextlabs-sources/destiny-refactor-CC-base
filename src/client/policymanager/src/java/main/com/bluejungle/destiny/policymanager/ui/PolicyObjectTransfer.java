/*
 * Created on Mar 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class PolicyObjectTransfer extends ByteArrayTransfer {

    private static final String TYPENAME = "com.bluejungle.policyobject";
    private static final int TYPEID = registerType(TYPENAME);
    private static final DomainObjectFormatter formatter = new DomainObjectFormatter();
    private static PolicyObjectTransfer _instance = new PolicyObjectTransfer();

    /**
     * Constructor
     * 
     */
    public PolicyObjectTransfer() {
        super();
    }

    public static PolicyObjectTransfer getInstance() {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof IPredicate[]))
            return;

        if (isSupportedType(transferData)) {
            IPredicate[] predicates = (IPredicate[]) object;
            try {
                // write data to a byte array and then ask super to convert to
                // pMedium
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream(out);
                for (int i = 0, length = predicates.length; i < length; i++) {
                    formatter.reset();
                    IPredicate predicate = predicates[i];

                    SpecType specType = PredicateHelpers.getPredicateType(predicate);
                    // if (predicate instanceof IDSpecRef){
                    // specType = ((IDSpecRef) predicate).getSpecType();
                    // }else if (predicate instanceof IRelation){
                    // Object lhs = ((IRelation) predicate).getLHS();
                    // if(lhs instanceof ResourceAttribute){
                    // specType = SpecType.RESOURCE;
                    // } else if (lhs instanceof Object ){
                    //                            
                    // }
                    // }

                    if (specType != null) {
                        formatter.formatDef(new DomainObjectDescriptor(null, "DragNDrop", null, null, EntityType.COMPONENT, "", DevelopmentStatus.EMPTY), predicate);
                        String pql = formatter.getPQL();
                        byte[] buffer = pql.getBytes();
                        writeOut.writeInt(buffer.length);
                        writeOut.write(buffer);
                    }
                }
                byte[] buffer = out.toByteArray();
                writeOut.close();

                super.javaToNative(buffer, transferData);

            } catch (IOException e) {
            }
        }
    }

    public Object nativeToJava(TransferData transferData) {

        if (isSupportedType(transferData)) {

            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;

            IPredicate[] data = new IPredicate[0];
            List<IPredicate> predicateList = new ArrayList<IPredicate>();
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);
                while (readIn.available() > 0) {
                    // IDSpec spec = new IDSpec();
                    int size = readIn.readInt();
                    byte[] pqlBytes = new byte[size];
                    readIn.read(pqlBytes);
                    String pql = new String(pqlBytes);
                    final IPredicate[] predicate = new IPredicate[1];
                    try {
                        DomainObjectBuilder.processInternalPQL(pql, new DefaultPQLVisitor() {

                            public void visitComponent(DomainObjectDescriptor descriptor, IPredicate spec) {
                                predicate[0] = spec;
                            }
                        });
                    } catch (PQLException e) {
                        // don't do anything-- Sergey says this will never
                        // happen
                    }

                    predicateList.add(predicate[0]);
                }
                data = (IPredicate[]) predicateList.toArray(new IPredicate[predicateList.size()]);
                readIn.close();
            } catch (IOException ex) {
                return null;
            }
            return data;
        }

        return null;
    }

    protected String[] getTypeNames() {
        return new String[] { TYPENAME };
    }

    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }
}
