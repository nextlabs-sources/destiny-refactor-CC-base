package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

public class ConfigurationTestFileGenerator {
    
    static final String HEADER = 
            "<?xml version=\"1.0\"?>"
          + "<DestinyConfiguration xmlns=\"http://bluejungle.com/destiny/services/management/types\">";

    static final String FOOTER =
            "</DestinyConfiguration>";
    
    enum Part {
        ApplicationUserConfiguration(
                "<ApplicationUserConfiguration>"
              +   "<AuthenticationMode>Local</AuthenticationMode>"
              +   "<UserRepositoryConfiguration>"
              +     "<ProviderClassName>com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository</ProviderClassName>"
              +   "</UserRepositoryConfiguration>"
              + "</ApplicationUserConfiguration>"
        
        ),
        
        MessageHandlers("<MessageHandlers/>"),
        Obligations("<Obligations/>", true),
        ActionList("<ActionList/>", true),
        DMS("<DMS><HeartbeatRate>15</HeartbeatRate></DMS>"), 
        DCSF("<DCSF><HeartbeatRate>15</HeartbeatRate></DCSF>"),
        DABS(
                "<DABS>" 
              + "<HeartbeatRate>15</HeartbeatRate>"
              + "<TrustedDomainsConfiguration/>"
              + "<FileSystemLogConfiguration/>"
              + "</DABS>"
        ),
        DPS("<DPS><HeartbeatRate>15</HeartbeatRate></DPS>"),
        DAC( 
                "<DAC>"
              +   "<HeartbeatRate>15</HeartbeatRate>"
              +   "<ActivityJournalSettingConfiguration>"
              +     "<SyncOperation>"
              +       "<TimeInterval>15</TimeInterval>"
              +       "<TimeoutInMinutes>120</TimeoutInMinutes>"
              +     "</SyncOperation>"
              +     "<IndexesRebuildOperation>"
              +       "<TimeOfDay>23:00</TimeOfDay>"
              +       "<AutoRebuildIndexes>true</AutoRebuildIndexes>"
              +       "<TimeoutInMinutes>120</TimeoutInMinutes>"
              +     "</IndexesRebuildOperation>"
              +     "<ArchiveOperation>"
              +       "<TimeOfDay>01:30</TimeOfDay>"
              +       "<DaysOfDataToKeep>30</DaysOfDataToKeep>"
              +       "<AutoArchive>true</AutoArchive>"
              +       "<TimeoutInMinutes>180</TimeoutInMinutes>"
              +     "</ArchiveOperation>"
              +   "</ActivityJournalSettingConfiguration>"
              + "</DAC>"
        ),
        DEM("<DEM><HeartbeatRate>15</HeartbeatRate></DEM>"),
        ManagementConsole("<ManagementConsole><HeartbeatRate>15</HeartbeatRate></ManagementConsole>"),
        Reporter("<Reporter><HeartbeatRate>15</HeartbeatRate></Reporter>"),
        Repositories(
                "<Repositories>"
              +   "<Repository>"
              +     "<Name>management.repository</Name>"
              +     "<ConnectionPoolName>management.connection.pool</ConnectionPoolName>"
              +     "<Properties/>"
              +   "</Repository>"
              +   "<Repository>"
              +     "<Name>activity.repository</Name>"
              +     "<ConnectionPoolName>activity.connection.pool</ConnectionPoolName>"
              +     "<Properties/>"
              +   "</Repository>"
              +   "<Repository>"
              +     "<Name>policyframework.repository</Name>"
              +     "<ConnectionPoolName>policyframework.connection.pool</ConnectionPoolName>"
              +     "<Properties/>"
              +   "</Repository>"
              +   "<Repository>"
              +     "<Name>dictionary.repository</Name>"
              +     "<ConnectionPoolName>dictionary.connection.pool</ConnectionPoolName>"
              +     "<Properties/>"
              +   "</Repository>"
              +   "<ConnectionPools>"
              +     "<ConnectionPool>"
              +       "<Name>policyframework.connection.pool</Name>"
              +       "<Username>root</Username>"
              +       "<Password>514e4f7160517c4e7c4f1371026e4a3f02674465</Password>"
              +       "<ConnectString>jdbc:postgresql://DX2000-SAFDAR:5432/pf</ConnectString>"
              +       "<DriverClassName>org.postgresql.Driver</DriverClassName>"
              +       "<MaxPoolSize>30</MaxPoolSize>"
              +     "</ConnectionPool>"
              +     "<ConnectionPool>"
              +       "<Name>dictionary.connection.pool</Name>"
              +       "<Username>root</Username>"
              +       "<Password>514e4f7160517c4e7c4f1371026e4a3f02674465</Password>"
              +       "<ConnectString>jdbc:postgresql://DX2000-SAFDAR:5432/dictionary</ConnectString>"
              +       "<DriverClassName>org.postgresql.Driver</DriverClassName>"
              +       "<MaxPoolSize>30</MaxPoolSize>"
              +     "</ConnectionPool>"
              +     "<ConnectionPool>"
              +       "<Name>activity.connection.pool</Name>"
              +       "<Username>root</Username>"
              +       "<Password>514e4f7160517c4e7c4f1371026e4a3f02674465</Password>"
              +       "<ConnectString>jdbc:postgresql://DX2000-SAFDAR:5432/activity</ConnectString>"
              +       "<DriverClassName>org.postgresql.Driver</DriverClassName>"
              +       "<MaxPoolSize>30</MaxPoolSize>"
              +     "</ConnectionPool>"
              +     "<ConnectionPool>"
              +       "<Name>management.connection.pool</Name>"
              +       "<Username>root</Username>"
              +       "<Password>514e4f7160517c4e7c4f1371026e4a3f02674465</Password>"
              +       "<ConnectString>jdbc:postgresql://DX2000-SAFDAR:5432/management</ConnectString>"
              +       "<DriverClassName>org.postgresql.Driver</DriverClassName>"
              +       "<MaxPoolSize>30</MaxPoolSize>"
              +     "</ConnectionPool>"
              +   "</ConnectionPools>"
              + "</Repositories>"
        ),
        ;
        
        
        final boolean optional;
        
        final String content;
        
        Part(String content, boolean optional){
            this.content = content;
            this.optional = optional;
        }
        
        Part(String content){
            this(content, false);
        }
        
        String getContent(){
            return content;
        }
    }
    
    public static String generate() {
        StringBuilder sb = new StringBuilder(HEADER);

        for (Part p : Part.values()) {
            sb.append(p.content);
        }
        
        sb.append(FOOTER);
        
        return sb.toString();
    }
    
}
