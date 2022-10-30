/*
 * Created on Nov 3, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/RegularExpressionProvider.java#1 $
 */
package com.bluejungle.destiny.container.dabs;

import java.io.Serializable;
import java.util.Date;

import com.bluejungle.destiny.server.shared.configuration.IRegularExpressionConfigurationDO;
import com.bluejungle.framework.heartbeat.IHeartbeatProvider;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.destiny.lib.RegularExpressionDTO;
import com.bluejungle.pf.destiny.lib.RegularExpressionRequestDTO;

/**
 * This heartbeat information provider prepares regexp information
 * for sending to the agents.
 *
 * @author Alan Morgan
 */
public class RegularExpressionProvider implements IHeartbeatProvider {
    /**
     * The DTO
     */
    private RegularExpressionDTO dto = null;
    
    public RegularExpressionProvider(IRegularExpressionConfigurationDO[] regexps) {
        dto = new RegularExpressionDTO(new Date());

        for (IRegularExpressionConfigurationDO regexp : regexps) {
            dto.addExpression(regexp.getName(),
                              regexp.getValue());
        }
    }

    public Serializable serviceHeartbeatRequest(String name, String data) {
        return serviceHeartbeatRequest(name, SerializationUtils.unwrapSerialized(data));
    }

    public Serializable serviceHeartbeatRequest(String name, Serializable requestData) {
        if (!(requestData instanceof RegularExpressionRequestDTO)
            || dto == null) {
            return null;
        }

        RegularExpressionRequestDTO req = (RegularExpressionRequestDTO)requestData;

        if (req.getTimestamp().before(dto.getBuildTime())) {
            return dto;
        }

        return null;
    }
}
