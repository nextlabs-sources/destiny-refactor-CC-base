package com.nextlabs.destiny.container.dkms.hibernateimpl;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;

public interface IKeyRingDO {
    
    String FORMAT_DELETED = "DELETED";

    String getName();

    Date getLastUpdated();

    Date getCreated();

    int getVersion();

    InputStream getKeyStoreInputStream() throws SQLException;
    
    void setKeyStoreData(byte[] keyStoreData);

    String getFormat();

}