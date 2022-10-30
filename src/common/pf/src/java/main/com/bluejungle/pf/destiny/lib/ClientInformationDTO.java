package com.bluejungle.pf.destiny.lib;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/ClientInformationDTO.java#1 $
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a Data Transfer Object for the client information.
 *
 * @author Sergey Kalinichenko, Alan Morgan
 */
public class ClientInformationDTO implements Externalizable, Serializable, Cloneable {

    /**
     * The build time of this DTO.
     */
    private long buildTime;

    /**
     * The set of UIDs for which this DTO has been prepared.
     */
    private Set<String> preparedForUids = new HashSet<String>();

    /**
     * A list of Client Identifiers.
     */
    private final List<String> clientIds = new ArrayList<String>();

    /**
     * A list of client short names.
     */
    private final List<String> shortNames = new ArrayList<String>();

    /**
     * A list of client long names.
     */
    private final List<String> longNames = new ArrayList<String>();

    /**
     * A list of client domains.
     */
    private final List<String[]> domains = new ArrayList<String[]>();

    /**
     * A list of UIDs of requesting users.
     */
    private final List<String[]> uids = new ArrayList<String[]>();

    /**
     * Serialization constructor.
     */
    public ClientInformationDTO() {
    }

    /**
     * Constructs an empty DTO.
     *
     * @param buildTime the build time of this DTO.
     */
    public ClientInformationDTO(Date buildTime) {
        this.buildTime = buildTime.getTime();
    }

    /**
     * Add a client to this DTO.
     *
     * @param clientId the ID of the client.
     * @param shortName the short name of the client.
     * @param longName the long name of the client.
     * @param domains the domains associated with the client.
     * @param uids the UIDs of users working with this client.
     */
    public void addClient(
        String clientId
    ,   String shortName
    ,   String longName
    ,   String[] domains
    ,   String[] uids
    ) {
        this.clientIds.add(clientId);
        this.shortNames.add(shortName);
        this.longNames.add(longName);
        this.domains.add(domains);
        this.uids.add(uids);
    }

    /**
     * Access the number of clients in this DTO.
     *
     * @return the number of clients in this DTO.
     */
    public int size() {
        return clientIds.size();
    }

    /**
     * Access the client IDs in this DTO.
     *
     * @return the client IDs in this DTO.
     */
    public List<String> getClientIds() {
        return Collections.unmodifiableList(clientIds);
    }

    /**
     * Access the client short names in this DTO.
     *
     * @return the client short names in this DTO.
     */
    public List<String> getShortNames() {
        return Collections.unmodifiableList(shortNames);
    }

    /**
     * Access the client long names in this DTO.
     *
     * @return the client long names in this DTO.
     */
    public List<String> getLongNames() {
        return Collections.unmodifiableList(longNames);
    }

    /**
     * Access the client domains in this DTO.
     *
     * @return the client domains in this DTO.
     */
    public List<String[]> getDomains() {
        return Collections.unmodifiableList(domains);
    }

    /**
     * Access the client UIDs in this DTO.
     *
     * @return the client UIDs in this DTO.
     */
    public List<String[]> getUids() {
        return Collections.unmodifiableList(uids);
    }

    /**
     * Access the build time of this DTO.
     *
     * @return the build time of this DTO.
     */
    public Date getBuildTime() {
        return new Date(buildTime);
    }

    /**
     * Sets the UIDs for which this DTO has been prepared.
     *
     * @param preparedForUids the UIDs for which this DTO has been prepared.
     */
    public void setPreparedForUids(String[] preparedForUids) {
        if (preparedForUids == null) {
            throw new NullPointerException("preparedForUids");
        }
        this.preparedForUids.clear();
        this.preparedForUids.addAll(Arrays.asList(preparedForUids));
    }

    /**
     * Obtain a Set<String> of UIDs for which this DTO has been prepared.
     *
     * @return a Set<String> of UIDs for which this DTO has been prepared.
     */
    public Set<String> getPreparedForUids() {
        return Collections.unmodifiableSet(preparedForUids);
    }

    /**
     * Sets an array of UIDs for the specified client.
     *
     * @param index the index of the client on which to set the UIDs.
     * @param uids the UIDs to set for the specified client.
     */
    public void setUids(int index, String[] uids) {
        this.uids.set(index, uids);
    }

    /**
     * Expose Object#clone() method.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Reads the data from the specified ObjectInput.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        buildTime = in.readLong();
        int size = in.readInt();
        for (int i = 0 ; i != size ; i++) {
            clientIds.add(in.readUTF());
        }
        for (int i = 0 ; i != size ; i++) {
            shortNames.add(in.readUTF());
        }
        for (int i = 0 ; i != size ; i++) {
            longNames.add(in.readUTF());
        }
        for (int i = 0 ; i != size ; i++) {
            domains.add(readStringArray(in));
        }
        for (int i = 0 ; i != size ; i++) {
            uids.add(readStringArray(in));
        }
        int uidSize = in.readInt();
        for (int i = 0 ; i != uidSize ; i++) {
            preparedForUids.add(in.readUTF());
        }
    }

    /**
     * Writes the object data to the specified ObjectOutput.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(buildTime);
        out.writeInt(clientIds.size());
        for (String s : clientIds) {
            out.writeUTF(s);
        }
        for (String s : shortNames) {
            out.writeUTF(s);
        }
        for (String s : longNames) {
            out.writeUTF(s);
        }
        for (String[] sa : domains) {
            writeStringArray(out, sa);
        }
        for (String[] sa : uids) {
            writeStringArray(out, sa);
        }
        out.writeInt(preparedForUids.size());
        for (String s : preparedForUids) {
            out.writeUTF(s);
        }
    }

    /**
     * Writes a String array to an ObjectOutput.
     *
     * @param out the ObjectOutput to which to write the array.
     * @param data the array to write to the output.
     * @throws IOException if an I/O problem prevents the operation from completion.
     */
    private static void writeStringArray(ObjectOutput out, String[] data) throws IOException {
        if (data != null) {
            out.writeInt(data.length);
            for (String s : data) {
                out.writeUTF(s);
            }
        } else {
            out.writeInt(0);
        }
    }

    /**
     * Reads a String array from an ObjectInput.
     *
     * @param in the ObjectInput from which to read the array.
     * @throws IOException if an I/O problem prevents the operation from completion.
     */
    private static String[] readStringArray(ObjectInput in) throws IOException {
        int size = in.readInt();
        if (size <= 0) {
            return null;
        }
        String[] res = new String[size];
        for (int i =0 ; i != size ; i++) {
            res[i] = in.readUTF();
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("Prepared at ");
        res.append(getBuildTime());
        if (preparedForUids != null && !preparedForUids.isEmpty()) {
            res.append(" for users ");
            boolean isFirst = true;
            for (String uid : preparedForUids) {
                if (!isFirst) {
                    res.append(", ");
                } else {
                    isFirst = false;
                }
                res.append("'");
                res.append(uid);
                res.append("'");
            }
        }
        res.append("\n");
        int size = clientIds.size();
        for (int i = 0 ; i != size ; i++) {
            res.append(clientIds.get(i));
            res.append(" - ");
            res.append(longNames.get(i));
            res.append(", ");
            res.append(shortNames.get(i));
            String[] domainArray = domains.get(i);
            if (domainArray != null && domainArray.length != 0) {
                res.append("\n    domains: ");
                boolean isFirst = true;
                for (String domain : domainArray) {
                    if (!isFirst) {
                        res.append(", ");
                    } else {
                        isFirst = false;
                    }
                    res.append("'");
                    res.append(domain);
                    res.append("'");
                }
            }
            String[] uidsArray = uids.get(i);
            if (uidsArray != null && uidsArray.length != 0) {
                res.append("\n    authorized UIDs: ");
                boolean isFirst = true;
                for (String uid : uidsArray) {
                    if (!isFirst) {
                        res.append(", ");
                    } else {
                        isFirst = false;
                    }
                    res.append("'");
                    res.append(uid);
                    res.append("'");
                }
            }
            res.append("\n\n");
        }
        return res.toString();
    }

}
