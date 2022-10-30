package com.nextlabs.pf.destiny.importexport;

public class ExportEntity {
    private String name;
    private String type;
    private String originalPql;
    private String pql;
    private Long databaseId;
    private int databaseVersion;

    //no-argument constructor used for XML binding
    public ExportEntity() {
    }
 
    //Constructor
    public ExportEntity(String name, String type, String pql) {
        this(name, type, pql, null, 0);
    }

    public ExportEntity(String name, String type, String pql, Long databaseId, int databaseVersion) {
        this(name, type, null, pql, databaseId, databaseVersion);
    }

    /**
     * Only use thid when you want to reuse the databaseId and databaseVersion
     * @param name
     * @param type
     * @param origPql
     * @param pql
     * @param databaseId
     * @param databaseVersion
     */
    public ExportEntity(String name, String type, String originalPql, String pql, Long databaseId, int databaseVersion) {
        this.name = name;
        this.type = type;
        this.originalPql = originalPql;
        this.pql = pql;
        this.databaseId = databaseId;
        this.databaseVersion = databaseVersion;
    }

    /**
     * @return the databaseId
     */
    public final Long getDatabaseId() {
        return databaseId;
    }

    /**
     * @return the databaseVersion
     */
    public final int getDatabaseVersion() {
        return databaseVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPql() {
        return pql;
    }

    public void setPql(String pql) {
        this.pql = pql;
    }

    public String getOriginalPql() {
        return originalPql;
    }

    public void setOriginalPql(String originalPql) {
        this.originalPql = originalPql;
    }

}
