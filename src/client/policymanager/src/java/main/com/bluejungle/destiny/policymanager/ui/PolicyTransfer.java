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

import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class PolicyTransfer extends ByteArrayTransfer {

    private static final String TYPENAME = "com.bluejungle.policy";
    private static final int TYPEID = registerType(TYPENAME);
    private static PolicyTransfer _instance = new PolicyTransfer();

    /**
     * Constructor
     * 
     */
    public PolicyTransfer() {
        super();
    }

    public static PolicyTransfer getInstance() {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof DomainObjectDescriptor[]))
            return;

        if (isSupportedType(transferData)) {
            DomainObjectDescriptor[] descriptors = (DomainObjectDescriptor[]) object;
            try {
                // write data to a byte array and then ask super to convert to
                // pMedium
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream(out);
                for (int i = 0, length = descriptors.length; i < length; i++) {
                    DomainObjectDescriptor descriptor = descriptors[i];
                    byte[] buffer = descriptor.getName().getBytes();
                    writeOut.writeInt(buffer.length);
                    writeOut.write(buffer);
                    buffer = descriptor.getType().toString().getBytes();
                    writeOut.writeInt(buffer.length);
                    writeOut.write(buffer);
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

            DomainObjectDescriptor[] data = new DomainObjectDescriptor[0];
            List<DomainObjectDescriptor> descriptorList = new ArrayList<DomainObjectDescriptor>();
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);
                while (readIn.available() > 0) {
                    int size = readIn.readInt();
                    byte[] nameBytes = new byte[size];
                    readIn.read(nameBytes);
                    String name = new String(nameBytes);
                    size = readIn.readInt();
                    byte[] typeBytes = new byte[size];
                    readIn.read(typeBytes);
                    String type = new String(typeBytes);
                    EntityType entityType = EntityType.forName(type);
                    DomainObjectDescriptor descriptor;
                    if (entityType == EntityType.POLICY)
                        descriptor = EntityInfoProvider.getPolicyDescriptor(name);
                    else if (entityType == EntityType.FOLDER)
                        descriptor = EntityInfoProvider.getPolicyFolderDescriptor(name);
                    else
                        descriptor = EntityInfoProvider.getComponentDescriptor(name);
                    descriptorList.add(descriptor);
                }
                readIn.close();
                data = (DomainObjectDescriptor[]) descriptorList.toArray(new DomainObjectDescriptor[descriptorList.size()]);
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
