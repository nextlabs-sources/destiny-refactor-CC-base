package com.bluejungle.pf.destiny.lib;

/*
 * Created on Nov 03, 2008
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/RegularExpressionDTO.java#1 $
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a Data Transfer Object for the regular expression information
 *
 * @author Alan Morgan
 */
public class RegularExpressionDTO implements Externalizable, Serializable, Cloneable {
    /**
     * The build time of this DTO.
     */
    long buildTime;

    /**
     * Mapping from regexp names to the actual expressions
     */
    private Map<String, String> expressions = new HashMap<String, String>();

    /**
     * Serialization constructor
     */
    public RegularExpressionDTO() {
    }

    /**
     * Constructs an empty DTO
     *
     * @param buildTime the build time of the DTO
     */
    public RegularExpressionDTO(Date buildTime) {
        this.buildTime = buildTime.getTime();
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
     * Obtain a Map<String, String> of the names and regular
     * expressions
     */
    public Map<String, String> getMapOfExpressions() {
        return Collections.unmodifiableMap(expressions);
    }

    /**
     * Add a new expression to this dto
     *
     * @param name the name of the regular expression (e.g. ccn)
     * @param expression the regular expression (e.g. \d{16})
     */
    public void addExpression(String name, String expression) {
        expressions.put(name, expression);
    }

    /**
     * Access the number of expression in this DTO.
     *
     * @return the number of expression in this DTO.
     */
    public int size() {
        return expressions.size();
    }

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
        for (int i = 0; i != size; i++) {
            String name = in.readUTF();
            String expr = in.readUTF();

            addExpression(name, expr);
        }
    }

    /**
     * Writes the object data to the specified ObjectOutput
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(buildTime);
        int size = this.size();
        out.writeInt(size);
        for (Map.Entry<String, String> entry : expressions.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Prepared at ");
        res.append(getBuildTime());
        res.append("\n");

        for (Map.Entry<String, String> entry : expressions.entrySet()) {
            res.append(entry.getKey());
            res.append(": ");
            res.append(entry.getValue());
            res.append("\n");
        }

        return res.toString();
    }
}
