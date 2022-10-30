package com.bluejungle.pf.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import net.sf.hibernate.HibernateException;

import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.tools.CollectingPQLVisitor;
import com.bluejungle.pf.tools.DeploymentToolsBase;

/**
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/utils/BatchDeployer.java#1 $
 * 
 */
public class BatchDeployer extends DeploymentToolsBase {

    public static final String USAGE = "BatchDeployer -file filename [-user user] [-password password] [-host host] [-port portnum] [-database (oracle|postgres|sqlserver|db2)] [-instance instance] [-clean]";

    public static final String FILE_ARGUMENT_NAME = "-file";
    public static final String CLEAN_ARGUMENT_NAME = "-clean";

    public static void main(String[] args) throws Exception {
        try {
            boolean clean = false;
            String filename = null;

            for (int i = 0; i < args.length; i++) {
                if (FILE_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                    filename = args[++i];
                } else if (CLEAN_ARGUMENT_NAME.equalsIgnoreCase(args[i])) {
                    clean = true;
                } else {
                    int skip = skipParameter(args[i]);
                    if (skip != -1) {
                        i += skip-1;
                    } else {
                        System.out.println("unrecognized option: " + args[i].substring(1));
                        System.out.println(USAGE);
                        System.exit(-1);
                    }
                }
            }

            if (filename == null) {
                System.out.println("file argument must be specified.");
                System.out.println(USAGE);
                System.exit(-1);
            }

            BatchDeployer deployer = new BatchDeployer(args);

            if (clean) {
                deployer.fullClean();
            }
            int status = deployer.deployEntities(
                new BufferedInputStream(
                    new FileInputStream(filename)
                )
            );
            System.exit(status);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private BatchDeployer(String[] args) throws HibernateException {
        setupDatasource(args);
    }

    private int deployEntities(InputStream is) throws Exception {
        CollectingPQLVisitor visitor = new CollectingPQLVisitor();
        DomainObjectBuilder dob = new DomainObjectBuilder(is);
        dob.processInternalPQL(visitor);
        Collection<DevelopmentEntity> entities = visitor.getEntities();
        for (DevelopmentEntity entity : entities) {
            PQLTestUtils.setStatus(entity, DevelopmentStatus.APPROVED);
        }

        lm.saveEntities(entities, LifecycleManager.MAKE_EMPTY, null);
        lm.deployEntities(entities, UnmodifiableDate.forTime(System.currentTimeMillis()), DeploymentType.PRODUCTION, false, null);
        System.out.println("Deployed " + entities.size() + " entities.");

        return entities.size();
    }
}
