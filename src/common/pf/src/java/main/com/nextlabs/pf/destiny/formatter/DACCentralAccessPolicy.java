/*
 * Created on Nov 16, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/DACCentralAccessPolicy.java#1 $:
 */

package com.nextlabs.pf.destiny.formatter;

import java.util.ArrayList;
import java.util.List;

public class DACCentralAccessPolicy {
    private String server;
    private String name;
    private List<String> carNames;
    private String identity;

    public DACCentralAccessPolicy() {
        carNames = new ArrayList<String>();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getADName() {
        return "CN=" + getName() + "," + getIdentity();
    }

    public List<String> getCARs() {
        return carNames;
    }

    public void addCAR(String carName) {
        carNames.add(carName);
    }
    
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
