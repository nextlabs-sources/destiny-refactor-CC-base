package com.nextlabs.destiny.container.dkms.hibernateimpl;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import net.sf.hibernate.lob.BlobImpl;

public class KeyRingDO implements IKeyRingDO, Serializable {

    private static final long serialVersionUID = 1L;

    /** The database identifier of this entity. */
    private Long id;
    
    private String name;
    
    private Date lastUpdated;
    
    /** The date this entity was created. */
    private Date created;

    /** Internal version of this development entity (for concurrency control). */
    private int version;
    
    private Blob data;
    
    private String format;
    
    KeyRingDO() {
        // for hibernate
    }
    
    public KeyRingDO(String name, byte[] keyStoreData, String format) {
        this.name = name;
        this.format = format;
        data = keyStoreData != null ? new BlobImpl(keyStoreData) : null;
    }

    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public int getVersion() {
        return version;
    }

    void setVersion(int version) {
        this.version = version;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

    @Override
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    
    @Override
    public InputStream getKeyStoreInputStream() throws SQLException {
        if(data == null){
            return null;
        }
        return data.getBinaryStream();
    }
    
    @Override
    public void setKeyStoreData(byte[] keyStoreData) {
        data = keyStoreData != null ? new BlobImpl(keyStoreData) : null;
    }
}
