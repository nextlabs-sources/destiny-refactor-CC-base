/*
 * Created on May 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.version.IVersion;

/**
 * This class is responsible for inserting seed data into the policy framework
 * database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/pf/src/java/main/com/bluejungle/pf/tools/PQLSeedDataTask.java#16 $
 */

public class PQLSeedDataTask extends SeedDataTaskBase implements ISeedDataTask, ISeedUpdateTask {

    public static final String SEED_PQL_DIR_PROPERTY = "pqldirectory";
    private static final String DATA_SHARE_FILE_NAME = "shared_seed_data.properties";

    @Override
    public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
        execute();
    }

    /**
     * @see com.bluejungle.destiny.tools.dbinit.ISeedDataTask#execute()
     */
    public void execute() throws SeedDataTaskException {
        getLog().trace("Invoking the PQL seed data importer...");

        Properties props = getConfiguration().get(ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM);
        String pqlDir = props.getProperty(SEED_PQL_DIR_PROPERTY);
        System.out.println("pqlDir: " + pqlDir);
        try {
            // Load shared seed data properties. Hack to share data between two
            // database seed data tasks
            FileInputStream sharedSeedDataInputStream = new FileInputStream(DATA_SHARE_FILE_NAME);
            Properties sharedSeedDataProperties = new Properties();
            sharedSeedDataProperties.load(sharedSeedDataInputStream);
            deploy(pqlDir, sharedSeedDataProperties);
        } catch (Exception e) {
            throw new SeedDataTaskException(e);
        }
        getLog().trace("PQL seed data import completed successfully");
    }

    private void deploy(String directory, Properties sharedSeedDataProperties) throws Exception {
        LifecycleManager lm = getManager().getComponent(LifecycleManager.COMP_INFO);
        File policyDir = new File(directory);
        File[] allPQLs = policyDir.listFiles(new FilenameFilter() {

            public boolean accept(File d, String name) {
                return name.endsWith(".pql");
            }
        });

        final Map<String, String>[] pqls = new Map[EntityType.getElementCount()];
        final List<String>[] names = new ArrayList[EntityType.getElementCount()];
        for (int i = 0; i != names.length; i++) {
            pqls[i] = new HashMap<String, String>();
            names[i] = new ArrayList<String>();
        }
        final DomainObjectFormatter dof = new DomainObjectFormatter();
        for (int i = 0; i < allPQLs.length; i++) {
            File nextPQLFile = allPQLs[i];
            File file = replaceTokens(nextPQLFile, sharedSeedDataProperties);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            DomainObjectBuilder dob = new DomainObjectBuilder(reader);
            dob.processInternalPQL(new IPQLVisitor() {

                public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                    names[descr.getType().getType()].add(descr.getName());
                    dof.reset();
                    dof.formatPolicyDef(descr, policy);
                    pqls[descr.getType().getType()].put(descr.getName(), dof.getPQL());
                }

                public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                    names[descr.getType().getType()].add(descr.getName());
                    dof.reset();
                    dof.formatDef(descr, pred);
                    pqls[descr.getType().getType()].put(descr.getName(), dof.getPQL());
                }

                public void visitLocation(DomainObjectDescriptor descr, Location location) {
                    names[descr.getType().getType()].add(descr.getName());
                    dof.reset();
                    dof.formatLocation(descr, location);
                    pqls[descr.getType().getType()].put(descr.getName(), dof.getPQL());
                }

                public void visitFolder(DomainObjectDescriptor descr) {
                    names[descr.getType().getType()].add(descr.getName());
                    dof.reset();
                    dof.formatFolder(descr);
                    pqls[descr.getType().getType()].put(descr.getName(), dof.getPQL());
                }

                public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                    // Free-standing access policies are ignored
                }

            });
        }

        EntityType[] orderedTypes = new EntityType[] { EntityType.ACTION, EntityType.APPLICATION, EntityType.HOST, EntityType.RESOURCE, EntityType.USER, EntityType.LOCATION, EntityType.COMPONENT, EntityType.FOLDER // policy
                // folders
                // go
                // ahead
                // of
                // policies
                , EntityType.POLICY // policies go last
        };

        Collection<DevelopmentEntity> policiesToDeploy = new LinkedList<DevelopmentEntity>();
        Collection<DevelopmentEntity> candidates = new LinkedList<DevelopmentEntity>();

        for (int i = 0; i != orderedTypes.length; i++) {
            EntityType type = orderedTypes[i];
            Collection<DevelopmentEntity> entities = lm.getEntitiesForNames(type, names[type.getType()], new LifecycleManager.NewEntityMaker() {

                public String makeNewEntity(Long id, String name, EntityType type) throws EntityManagementException {
                    StringBuffer res = new StringBuffer(128);
                    res.append("ID ");
                    res.append(id);
                    res.append(" STATUS APPROVED CREATOR \"0\" ACCESS_POLICY " + "ACCESS_CONTROL " + "PBAC FOR * ON READ BY APPUSER.DID = \"0\"  DO ALLOW " + "ALLOWED_ENTITIES ");
                    res.append(type.emptyPql(name));
                    return res.toString();
                }
            });
            for (DevelopmentEntity dev : entities) {
                dev.setPql((String) pqls[type.getType()].get(dev.getName()));
                if (type == EntityType.POLICY) {
                    // Hidden policies are not usable if they are not deployed
                    if (dev.isHidden()) {
                        policiesToDeploy.add(dev);
                        candidates.add(dev);
                    }
                } else {
                    candidates.add(dev);
                }
            }
            lm.saveEntities(entities, LifecycleManager.MAKE_EMPTY, null);
        }

        // Extract IDs from dev entities representing hidden policies
        List<Long> policiesToDeployIds = new ArrayList<Long>(policiesToDeploy.size());
        for (DevelopmentEntity policy : policiesToDeploy) {
            policiesToDeployIds.add(policy.getId());
        }

        // Find all entities on which hidden policies depend
        Collection<Long> toDeployIds = lm.discoverBFS(policiesToDeployIds, candidates, LifecycleManager.DIRECT_DEPENDENCY_BUILDER, LifecycleManager.DEVELOPMENT_ID_EXTRACTOR, false);

        // Add the IDs of the hidden policies to the list
        toDeployIds.addAll(policiesToDeployIds);

        // This method is package-private, but this task knows what it's doing :-)
        Method privateDeploy = LifecycleManager.class.getDeclaredMethod(
                "deployEntities" 
              , Collection.class
              , Date.class
              , DeploymentType.class
              , Date.class
              , Boolean.TYPE
              , Long.class
        );
        privateDeploy.setAccessible(true);
        privateDeploy.invoke(lm, 
                lm.getEntitiesForIDs(toDeployIds)
              , new Date()
              , DeploymentType.PRODUCTION
              , UnmodifiableDate.START_OF_TIME
              , Boolean.TRUE
              , null
        );

    }

    /**
     * @param nextPQLFile
     * @param sharedSeedDataProperties
     * @return
     */
    private File replaceTokens(File nextPQLFile, Properties sharedSeedDataProperties) throws IOException {
        File fileToReturn = new File(nextPQLFile.getParent(), nextPQLFile.getName() + ".processed");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileToReturn));
        BufferedReader reader = new BufferedReader(new FileReader(nextPQLFile));
        
        String nextLine = null;
        while((nextLine = reader.readLine()) != null) {
            StringTokenizer templateTokenizer = new StringTokenizer(nextLine, "${}", true);
            while (templateTokenizer.hasMoreTokens()) {
                String nextToken = templateTokenizer.nextToken();
                if (nextToken.equals("$")) {
                    // skip {
                    nextToken = templateTokenizer.nextToken();
                    if (!nextToken.equals("{")) {
                        throw new IllegalStateException("Improper template syntax in line, " + nextLine);
                    }
                    nextToken = templateTokenizer.nextToken();
                    if (!sharedSeedDataProperties.containsKey(nextToken)) {
                        throw new IllegalStateException("Uknown template text in line, " + nextLine);
                    }
                    writer.write(sharedSeedDataProperties.getProperty(nextToken));
                    
                    // skip }
                    nextToken = templateTokenizer.nextToken();
                    if (!nextToken.equals("}")) {
                        throw new IllegalStateException("Improper template syntax in line, " + nextLine);
                    }
                } else {
                    writer.write(nextToken);
                }
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
        reader.close();
        
        return fileToReturn;
    }
}
