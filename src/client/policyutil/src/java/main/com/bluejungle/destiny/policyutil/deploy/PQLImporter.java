/*
 * Created on Mar 18, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policyutil.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.FileSystemResourceLocatorImpl;
import com.bluejungle.destiny.container.dcc.IDCCComponentConfigResourceLocator;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.TestDCCComponentConfigLocatorImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.test.HibernateTestUtils;
import com.bluejungle.framework.datastore.hibernate.test.TestOrientedHibernateRepositoryImpl;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * @author Robert Lin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policyutil/src/java/main/com/bluejungle/destiny/policyutil/deploy/PQLImporter.java#1 $
 */
public class PQLImporter implements IManagerEnabled, ILogEnabled {

    private static final String DESTINY_INSTALL_PATH_PROPERTY_NAME = "build.root.dir";
    private static final String DEFAULT_DESTINY_INSTALL_PATH = "C:/builds/destiny";
    private static final String DEFAULT_COMPONENT_RELATIVE_HOME_PATH = "/server/container/";
    private static String hostname = "localhost";
    private static String port = "5432";
    private static String dbUser = "root";
    private InputStreamReader in = new InputStreamReader(System.in);
    private BufferedReader buffer = new BufferedReader(in);
    private String input = "";
    private String pql = "";
    private int cmdPrompt = 1;
    private boolean createDevEntity = false;
    private boolean writeToPersis = false;
    private LifecycleManager lm = null;
    private static File installPath;

    public static final String CONFIG_RESOURCE_LOCATOR = "ConfigResourceLocator";

    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();

    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    public static final Map<DestinyRepository, HashMapConfiguration> DESTINY_DATA_SOURCES = new HashMap<DestinyRepository, HashMapConfiguration>();
    /*static {
        try {
            RepositoryConfiguration dataSourceToDefine = new RepositoryConfiguration();
            dataSourceToDefine.setUsername("root");
            dataSourceToDefine.setPassword("123blue!");
            dataSourceToDefine.setName(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
            dataSourceToDefine.setConnectString(new URI("jdbc:postgresql://treasure:5432/pf"));
            DESTINY_DATA_SOURCES.put(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY, dataSourceToDefine);

        } catch (MalformedURIException exception) {
            // shouldn't happen
            throw new IllegalStateException("Database URLs are invalid.");
        }
    }*/

    /** The component manager */
    private IComponentManager manager;

    private Log log = null;

    public static void main(String[] args) {

        if (args.length > 0){
            for (int i = 0; i < args.length; i++){
                if (args[i].equals("-n")){
                    i++;
                    hostname = args[i];
                } else if (args[i].equals("-p")){
                    i++;
                    port = args[i];
                } else if (args[i].equals("-b")){
                    i++;
                    dbUser = args[i];
                }	
            }
        }

        ComponentInfo<PQLImporter> info = new ComponentInfo<PQLImporter>(PQLImporter.class, LifestyleType.SINGLETON_TYPE);
        PQLImporter pImporter = ComponentManagerFactory.getComponentManager().getComponent(info);
        HashMapConfiguration configuration = new HashMapConfiguration();
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.REPOSITORY_ENUM_PARAM, DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.USER_NAME_PARAM, dbUser);
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.PASSWORD_PARAM, "123blue!");
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.CONNECT_STRING_PARAM, "jdbc:postgresql://" + hostname + ":" + port + "/pf");
        configuration.setProperty( "hibernate.dialect", "net.sf.hibernate.dialect.PostgreSQLDialect" );
        configuration.setProperty( "hibernate.connection.driver_class", "org.postgresql.Driver" );
        DESTINY_DATA_SOURCES.put(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY, configuration);

        pImporter.init ();
        pImporter.setLifecycleManager((LifecycleManager) ComponentManagerFactory.getComponentManager ().getComponent (LifecycleManager.COMP_INFO));


        if (args.length > 0){
            pImporter.processCommandLine(args);
        } else {
            pImporter.importPQL();
        }
    }

    public PQLImporter(){
        //ComponentInfo info = new ComponentInfo("PQLImporter", PQLImporter.class.getName(), null, LifestyleType.SINGLETON_TYPE);
        //PQLImporter pImporter = (PQLImporter) ComponentManagerFactory.getComponentManager().getComponent(info);
        HashMapConfiguration configuration = new HashMapConfiguration();
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.REPOSITORY_ENUM_PARAM, DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.USER_NAME_PARAM, dbUser);
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.PASSWORD_PARAM, "123blue!");
        configuration.setProperty(TestOrientedHibernateRepositoryImpl.CONNECT_STRING_PARAM, "jdbc:postgresql://" + hostname + ":" + port + "/pf");
        configuration.setProperty( "hibernate.dialect", "net.sf.hibernate.dialect.PostgreSQLDialect" );
        configuration.setProperty( "hibernate.connection.driver_class", "org.postgresql.Driver" );
        DESTINY_DATA_SOURCES.put(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY, configuration);

        init ();
        setLifecycleManager((LifecycleManager) ComponentManagerFactory.getComponentManager ().getComponent (LifecycleManager.COMP_INFO));
    }

    private void processCommandLine(String[] args) {
        String flag;
        String policyName;
        String deployTime;
        String undeployTime;
        String fileName;
        DevelopmentEntity dev;
        DevelopmentEntity[] entities = new DevelopmentEntity[1];
        boolean promptFlag = true;

        for (int i = 0; i < args.length; i++){

            flag = args[i];

            try {
                if (flag.equals("-c")){ //create
                    try {
                        i = i+1;
                        pql = args[i];
                    } catch (ArrayIndexOutOfBoundsException e){
                        this.log.error(null, e);
                        System.err.println("wrong number of arguments");
                        System.exit(1);
                    }
                    policyName = checkSyntax().getName();
                    dev = createDevEntity(policyName);
                    lm.saveEntity(dev, LifecycleManager.MUST_EXIST, null);
                    promptFlag = false;
                } else if (flag.equals("-d")){ //deploy
                    try {
                        i = i+1;
                        policyName = args[i];
                        i = i+1;
                        deployTime = args[i];

                        dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
                        setStatus( dev, DevelopmentStatus.APPROVED );
                        entities[0] = dev;
                        lm.deployEntities(Arrays.asList(entities), new Date(deployTime), DeploymentType.PRODUCTION, false, null);
                    } catch (ArrayIndexOutOfBoundsException e){
                        this.log.error(null, e);
                        System.err.println("wrong number of arguments");
                        System.exit(1);
                    }
                    promptFlag = false;
                } else if (flag.equals("-u")){ //undeploy
                    try {
                        i = i+1;
                        policyName = args[i];
                        i = i+1;
                        undeployTime = args[i];

                        dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
                        entities[0] = dev;
                        lm.undeployEntities(Arrays.asList(entities), new Date(undeployTime), DeploymentType.PRODUCTION, false, null);
                    } catch (ArrayIndexOutOfBoundsException e){
                        this.log.error(null, e);
                        System.err.println("wrong number of arguments");
                        System.exit(1);
                    }
                    promptFlag = false;
                } else if (flag.equals("-v")){ //view
                    processView();
                    promptFlag = false;
                } else if (flag.equals("-f")){ //file
                    try {
                        i = i+1;
                        fileName = args[i];
                        processFile(fileName);
                    } catch (ArrayIndexOutOfBoundsException e){
                        this.log.error(null, e);
                        System.err.println("wrong number of arguments");
                        System.exit(1);
                    }
                    promptFlag = false;
                } else if (flag.equals("-h")){ //print commandline help
                    printCommandLineHelp();
                    promptFlag = false;
                } else if (flag.equals("-n") || flag.equals("-p") || flag.equals("-b")){ //print commandline help
                    i++;
                } else {
                    this.log.error("Unknown flag " + flag);
                    System.err.println("Unknown flag " + flag);
                    System.exit(1);
                }
            } catch (Exception e){
                this.log.error(null, e);
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (promptFlag){
            importPQL();
        }
    }


    /**
     *
     */
    private void importPQL() {
        //start a loop that reads input
        try {
            while (true){
                cmdPrompt = 1;
                printCommandPrompt(cmdPrompt);
                input = buffer.readLine();

                //process input
                if (input.equals("/q")){
                    System.out.println("Exiting PQLImporter");
                    System.exit(0);
                }
                if (input.equals("/h") || input.equals("/?")){
                    printHelp();
                    input = "";
                } else if (input.equals("create")){
                    cmdPrompt = 2;
                    processCreate();
                    input = "";
                } else if (input.equals("deploy")){
                    cmdPrompt = 3;
                    processDeploy();
                    input = "";
                } else if (input.equals("undeploy")){
                    cmdPrompt = 4;
                    processUndeploy();
                    input = "";
                } else if (input.equals("view")){
                    try {
                        processView();
                    } catch (Exception e){
                        this.log.error(null, e);
                    }
                    input = "";
                } else if (input.equals("file")){
                    processFile();
                    input = "";
                } else if (input.equals("cancel")){
                    processCancel();
                    input = "";
                } else {
                    System.out.println("unknown action, please try again");
                    input = "";
                }
            }
        } catch (Exception e){
            this.log.error(null, e);
        }
    }



    private void processCreate() {

        boolean finished = true;
        String policyName = "";
        DevelopmentEntity dev = null;
        input = "";
        boolean begin = true;
        String temp = "";

        //start a loop that reads PQL
        while (true){
            if (begin){
                printCommandPrompt(cmdPrompt);
            } else {
                printCommandPrompt(7);
            }
            try {
                temp = buffer.readLine();
                input = input + temp;
            } catch (Exception e){
                this.log.error(null, e);
            }

            if (temp.equals("/q")){
                cmdPrompt = 1;
                break;
            }
            if (input.endsWith(";")){
                pql = input.substring(0, input.length()-1);
                this.log.info("pql = " + pql);
                finished = true;
                input = "";

                //check PQL syntax
                if (finished){
                    try {
                        IDPolicy p = checkSyntax();
                        createDevEntity = true;
                        policyName = p.getName();
                    } catch (Exception e){
                        createDevEntity = false;
                        this.log.error(null, e);
                    }
                }	

                //create development entity
                if (createDevEntity){
                    try {
                        dev = createDevEntity(policyName);
                        writeToPersis = true;
                    } catch (Exception e){
                        writeToPersis = false;
                        this.log.error(null, e);
                    }
                    createDevEntity = false;
                    pql = "";
                }

                //write to persistence
                if (writeToPersis){
                    try {
                        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, null);
                    } catch (Exception e){
                        this.log.error(null, e);
                    }
                    writeToPersis = false;
                }

                begin = true;
                break;
            } else {
                begin = false;
                input = input + " ";
            }
        }
    }


    public String processCreate(String policyPQL) throws PQLException, EntityManagementException {

        String policyName = "";
        DevelopmentEntity dev = null;
        input = "";
        pql = policyPQL;

        this.log.info("pql = " + pql);

        //check PQL syntax
        IDPolicy p = checkSyntax();
        policyName = p.getName();

        //create development entity
        dev = createDevEntity(policyName);

        //write to persistence
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, null);

        return policyName;
    }


    private void processDeploy() throws PQLException {

        String policyName = "";
        String deployTime = "";
        DevelopmentEntity dev = null;
        DevelopmentEntity[] tmp = new DevelopmentEntity[1];
        boolean name = false;
        boolean deploy = false;

        while (true){

            // get the name of the policy
            while (true){
                printCommandPrompt(5);
                try {
                    policyName = buffer.readLine();
                    if (policyName.equals("/q")){
                        break;
                    }

                    dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
                    name = true;
                    break;
                } catch (Exception e){
                    this.log.error(null, e);
                }
            }

            // get the time of deployment
            if (name){
                deployTime = getTime();
                if (deployTime == null){
                    break;
                } else {
                    deploy = true;
                }
                name = false;
            }

            // deploy
            if (deploy){
                printCommandPrompt(cmdPrompt);
                try {
                    input = buffer.readLine();
                } catch (Exception e){
                    this.log.error(null, e);
                }

                if (input.equals("/q")){
                    cmdPrompt = 1;
                    break;
                }
                if (input.equals("yes")){
                    setStatus( dev, DevelopmentStatus.APPROVED );
                    tmp[0] = dev;
                    try {
                        lm.deployEntities( Arrays.asList(tmp), new Date(deployTime), DeploymentType.PRODUCTION, false, null);
                    } catch (Exception e){
                        this.log.error(null, e);
                    }
                } else if (input.equals("no")){
                    // do nothing
                }
                deploy = false;
            }
            break;
        }
    }

    public void processDeploy(String policyName, Date deployTime) throws EntityManagementException, PQLException {

        DevelopmentEntity dev = null;
        DevelopmentEntity[] tmp = new DevelopmentEntity[1];

        dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);

        // deploy
        setStatus( dev, DevelopmentStatus.APPROVED );
        tmp[0] = dev;
        lm.deployEntities( Arrays.asList(tmp), deployTime, DeploymentType.PRODUCTION, false, null);
    }

    private void processUndeploy() throws PQLException {

        String policyName = "";
        String undeployTime = "";
        DevelopmentEntity dev = null;
        DevelopmentEntity[] tmp = new DevelopmentEntity[1];
        boolean name = false;
        boolean undeploy = false;

        while(true){
            while (true){
                printCommandPrompt(5);
                try {
                    policyName = buffer.readLine();

                    if (policyName.equals("/q")){
                        break;
                    }

                    //check if policy exists
                    dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
                    name = true;
                    break;
                } catch (Exception e){
                    this.log.error(null, e);
                }
            }

            //get the time of deployment
            if (name){
                undeployTime = getTime();
                if (undeployTime == null){
                    break;
                } else {
                    undeploy = true;
                }
                name = false;
            }

            if (undeploy){
                printCommandPrompt(cmdPrompt);
                try {
                    input = buffer.readLine();
                } catch (Exception e){
                    this.log.error(null, e);
                }

                if (input.equals("/q")){
                    cmdPrompt = 1;
                    break;
                }
                if (input.equals("yes")){
                    setStatus( dev, DevelopmentStatus.OBSOLETE );
                    tmp[0] = dev;
                    try {
                        lm.undeployEntities(Arrays.asList(tmp), new Date(undeployTime), DeploymentType.PRODUCTION, false, null);
                    } catch (Exception e){
                        this.log.error(null, e);
                    }
                } else if (input.equals("no")){
                    // do nothing
                }
                undeploy = false;
            }
            break;
        }
    }

    public void processUndeploy(String policyName, Date undeployTime) throws EntityManagementException, PQLException {
        DevelopmentEntity dev = null;
        DevelopmentEntity[] tmp = new DevelopmentEntity[1];
        dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
        setStatus( dev, DevelopmentStatus.OBSOLETE);
        tmp[0] = dev;
        lm.undeployEntities(Arrays.asList(tmp), undeployTime, DeploymentType.PRODUCTION, false, null);	
    }

    private void processView() throws EntityManagementException {

        Collection<DomainObjectDescriptor> descriptors = lm.getEntityDescriptors(EntityType.POLICY, "%", false);
        for (DomainObjectDescriptor descriptor : descriptors) {
            System.out.println(descriptor.getName());
        }
    }

    private void processFile() {

        String fileName = "";
        boolean quit = false;
        boolean file = false;
        File pqlFile = null;
        FileInputStream fileStream = null;
        StreamTokenizer strTok = null;
        String policyName = "";
        boolean create = false;
        DevelopmentEntity devEnt = null;

        while (!quit){
            while (true){
                printCommandPrompt(8);
                try {
                    fileName = buffer.readLine();

                    if (fileName.equals("/q")){
                        break;
                    }

                    // open the file and verify that it can be read
                    pqlFile = new File(fileName);
                    if (pqlFile.canRead()){
                        file = true;
                    } else {
                        throw new Exception("Cannot open file "+ fileName);
                    }
                    break;
                } catch (Exception e){
                    this.log.error(null, e);
                }
            }

            if (file){
                // read the file, process the PQL, and create the dev entities
                try {

                    //setup
                    fileStream = new FileInputStream(pqlFile);
                    strTok = new StreamTokenizer(fileStream);
                    strTok.wordChars(32, 127);
                    strTok.whitespaceChars(32, 32);

                    while (strTok.nextToken() != StreamTokenizer.TT_EOF){

                        //read the pql
                        if (strTok.sval != null){
                            pql = strTok.sval;
                        } else {
                            pql = Integer.toString((new Double(strTok.nval)).intValue());
                        }

                        while (true){
                            if (strTok.nextToken() == StreamTokenizer.TT_EOF){
                                throw new PQLException(new Exception("File does not end with \";\""));
                            }
                            if (strTok.sval != null){
                                pql = pql + " " + strTok.sval;
                            } else {
                                pql = pql + " " + Integer.toString((new Double(strTok.nval)).intValue());
                            }
                            if (pql.endsWith(";")){
                                break;
                            }
                        }
                        pql = pql.substring(0, pql.length()-1);

                        this.log.info("pql = " + pql);

                        //check PQL syntax
                        try {
                            IDPolicy p = checkSyntax();
                            create = true;
                            policyName = p.getName();
                        } catch (Exception e){
                            create = false;
                            this.log.error(null, e);
                        }

                        //create the dev entity
                        if (create){
                            try {
                                devEnt = createDevEntity(policyName);
                                writeToPersis = true;
                            } catch (Exception e){
                                writeToPersis = false;
                                this.log.error(null, e);
                            }
                            create = false;
                        }

                        //write to persistence
                        if (writeToPersis){
                            try {
                                lm.saveEntity(devEnt, LifecycleManager.MUST_EXIST, null);
                            } catch (Exception e){
                               this.log.error(null, e);
                            }
                            writeToPersis = false;
                            this.log.info("Policy " + policyName + " created successfully");
                            System.out.println("Policy " + policyName + " created successfully");
                        }
                        pql = "";
                    }
                } catch (Exception e){
                    this.log.error(null, e);
                }

                file = false;
            }

            break;
        }
    }

    private void processFile(String fileName) throws Exception {
        boolean file = false;
        File pqlFile = null;
        FileInputStream fileStream = null;
        StreamTokenizer strTok = null;
        String policyName = "";
        boolean create = false;
        DevelopmentEntity devEnt = null;

        // open the file and verify that it can be read
        pqlFile = new File(fileName);	
        if (pqlFile.canRead()){
            file = true;
        } else {
            throw new Exception("Cannot open file "+ fileName);
        }

        if (file){
            //setup
            fileStream = new FileInputStream(pqlFile);
            strTok = new StreamTokenizer(fileStream);
            strTok.wordChars(32, 127);
            strTok.whitespaceChars(32, 32);

            while (strTok.nextToken() != StreamTokenizer.TT_EOF){

                //read the pql
                pql = strTok.sval;
                while (true){
                    if (strTok.nextToken() == StreamTokenizer.TT_EOF){
                        throw new PQLException(new Exception("File does not end with \";\""));
                    }
                    pql = pql + " " + strTok.sval;
                    if (pql.endsWith(";")){
                        break;
                    }
                }
                pql = pql.substring(0, pql.length()-1);
                this.log.info("pql = " + pql);

                //check PQL syntax
                IDPolicy p = checkSyntax();
                create = true;
                policyName = p.getName();

                //create the dev entity
                if (create){
                    devEnt = createDevEntity(policyName);
                    writeToPersis = true;
                    create = false;
                }

                //write to persistence
                if (writeToPersis){
                    lm.saveEntity(devEnt, LifecycleManager.MUST_EXIST, null);
                    writeToPersis = false;
                }
                pql = "";
                this.log.info("Policy " + policyName + " created successfully");
                System.out.println("Policy " + policyName + " created successfully");
            }
        }
    }


    private void processCancel() {

        String policyName = "";
        String cancelTime = "";
        DeploymentEntity dep = null;
        DevelopmentEntity dev = null;
        boolean cancel = false;

        while (true){
            while (true){
                printCommandPrompt(5);
                try {
                    policyName = buffer.readLine();

                    if (policyName.equals("/q")){
                        break;
                    }

                    //check if policy exists
                    dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
                    if (dev == null){
                        this.log.error("development entity is null!");
                    }
                    break;
                } catch (Exception e){
                    this.log.error(null, e);
                }
            }

            // get the interested time
            while (true){
                printCommandPrompt(6);
                try {
                    cancelTime = buffer.readLine();
                    //history = lm.getDeploymentHistoryForEntity(dev, DeploymentType.PRODUCTION);
                    dep = lm.getDeployedEntityForName(EntityType.POLICY, policyName, new Date(cancelTime), DeploymentType.PRODUCTION);

                    if (dep == null){
                        this.log.error("deployment entity is null!");
                    }

                    cancel = true;
                    break;
                } catch (Exception e){
                    this.log.error(null, e);
                }
            }

            if (cancel){
                printCommandPrompt(9);
                try {
                    input = buffer.readLine();
                } catch (Exception e){
                    this.log.error(null, e);
                }

                if (input.equals("/q")){
                    cmdPrompt = 1;
                    break;
                }
                if (input.equals("yes")){
                    try {
                        lm.cancelDeploymentOfEntities( dep.getDeploymentRecord() );
                    } catch (Exception e){
                        this.log.error(null, e);
                    }
                } else if (input.equals("no")){
                    // do nothing
                }
                cancel = false;
            }

            break;
        }
    }

    /**
     * @return
     * @throws PQLException
     */
    private IDPolicy checkSyntax() throws PQLException {
        DomainObjectBuilder builder = new DomainObjectBuilder(pql);
        IDPolicy p = builder.processPolicy();
        if (p == null){
            throw new PQLException(new Exception("could not create policy, possibly invalid syntax"));
        }
        return p;
    }

    /**
     * @param policyName
     * @return
     * @throws EntityManagementException
     */
    private DevelopmentEntity createDevEntity(String policyName) throws EntityManagementException, PQLException {
        DevelopmentEntity dev;
        dev = lm.getEntityForName(EntityType.POLICY, policyName, LifecycleManager.MAKE_EMPTY);
        dev.setPql(pql);
        return dev;
    }

    private String getTime() {

        String time;

        while (true){
            printCommandPrompt(6);
            try {
                time = buffer.readLine();

                if (time.equals("/q")){
                    break;
                }
                return time;
            } catch (Exception e){
                this.log.error(null, e);
            }
        }
        return null;
    }

    private void printCommandLineHelp() {
        System.out.println("\nCommand Line Options:\n"+
                		   "-h ..................................... Help\n"+
                		   "-c <PQL> ............................... Create a policy\n"+
                		   "-d <Policy_Name> <Deploy_Time> ......... Deploy a policy\n"+
                		   "-u <Policy_Name> <Undeploy_Time> ....... Undeploy a policy\n"+
                		   "-v ..................................... View all policies\n"+
                		   "-f <File_Name>.......................... Import PQL from a file\n"+
                		   "-n <Host_Name>.......................... Set the hostname for the database\n"+
                		   "-p <DB_Port>............................ Set the database port\n");
    }

    private void printCommandPrompt(int display) {
        if (display == 1){ //main loop	
            System.out.print("Please enter action: create, deploy, undeploy, view, file > ");
        } else if (display == 2){
            System.out.print("Create > ");
        } else if (display == 3){
            System.out.print("Would you like to deploy the policy: yes or no > ");
        } else if (display == 4){
            System.out.print("Would you like to undeploy the policy: yes or no > ");
        } else if (display == 5){
            System.out.print("Please enter name of the policy > ");
        } else if (display == 6){
            System.out.print("Please enter time (ex. 05/05/2005 05:05:05) > ");
        } else if (display == 7){
            System.out.print("       > ");
        } else if (display == 8){
            System.out.print("Please enter path to the file > ");
        } else if (display == 9){
            System.out.print("Would you like to cancel the deployment: yes or no > ");
        }
    }

    private void printHelp() {
        System.out.print("\nWelcome to PQLImporter\n"+
                	  	"create ............. To create a policy\n"+
                	  	"deploy ............. To deploy a policy\n"+
                	  	"undeploy ........... To undeploy a policy\n"+
                	  	"view ............... To view all policies\n"+
                	  	"file ............... To read PQL from a file\n"+
                	  	//"cancel ............. To undo the effect of a prior deployment\n"+
                	  	"/q ................. Quit\n"+
                	  	"/h ................. Help\n"+
                        "Note: PQL is delimited by \";\"\n\n");
    }

    private File getInstallPath() {
        if (installPath == null) {
            String destinyInstallPath = System.getProperty(DESTINY_INSTALL_PATH_PROPERTY_NAME, DEFAULT_DESTINY_INSTALL_PATH);

            installPath = new File(destinyInstallPath);
            if (!installPath.exists()) {
                throw new IllegalArgumentException("Install path specified by system property, \"destiny.install.path\", does not exists.");
            }
        }

        return installPath;
    }

    /**
     * Given a PQL string, returns an equivalent string
     * with the status set to the desired value.
     * @param dev the dev entity with non-empty PQL.
     * @param newStatus the status to set for the PQL.
     * @throws PQLException when the PQL cannot be parsed.
     */
    public static void setStatus( DevelopmentEntity dev, final DevelopmentStatus newStatus ) throws PQLException {
        if ( dev == null ) {
            throw new NullPointerException("dev");
        }
        if ( dev.getPql() == null ) {
            return;
        }
        if ( newStatus == null ) {
            throw new NullPointerException("newStatus");
        }
        final DomainObjectFormatter dof = new DomainObjectFormatter();
        DomainObjectBuilder.processInternalPQL( dev.getPql(), new IPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                dof.formatPolicyDef( update( descr, newStatus ), policy );
            }
            public void visitFolder(DomainObjectDescriptor descr) {
                dof.formatFolder( update( descr, newStatus ) );
            }
            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                dof.formatDef( update( descr, newStatus ), pred );
            }
            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                throw new IllegalArgumentException("Location is unexpected in setStatus");
            }
            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                throw new IllegalArgumentException("Access policy is unexpected in setStatus");
            }
            public DomainObjectDescriptor update( DomainObjectDescriptor descr, DevelopmentStatus newStatus ) {
                return new DomainObjectDescriptor(
                    descr.getId()
                ,   descr.getName()
                ,   descr.getOwner()
                ,   descr.getAccessPolicy()
                ,   descr.getType()
                ,   descr.getDescription()
                ,   newStatus
                ,   descr.getVersion()
                ,   descr.getLastUpdated()
                ,   descr.getWhenCreated()
                ,   descr.getLastModified()
                ,   descr.getModifier()
                ,   descr.getLastSubmitted()
                ,   descr.getSubmitter()
                ,   descr.isHidden()
                ,   true
                ,   descr.hasDependencies());
            }
        });
        dev.setPql( dof.getPQL() );
    }

    protected void setupResourceLocator() {
        /*
         * Set up component resource locator. Use the WebAppResourceLocator
         * constant, because this is what is generally used. Should it be
         * renamed to Component_Resource_Locator? - FIX ME
         */
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        String componentRootDir = getInstallPath().getAbsolutePath() + DEFAULT_COMPONENT_RELATIVE_HOME_PATH + "dabs";
        HashMapConfiguration resourceLocatorConfig = new HashMapConfiguration();
        resourceLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, componentRootDir);
        ComponentInfo<INamedResourceLocator> serverLocatorInfo = 
            new ComponentInfo<INamedResourceLocator>(
                DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR, 
                FileSystemResourceLocatorImpl.class, 
                INamedResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                resourceLocatorConfig);
        compMgr.getComponent(serverLocatorInfo);
    }

    /**
     * Set up the config resource locator
     */
    protected void setupConfigResourceLocator() {
        /*
         * Set up config file locator
         */
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration configLocatorConfig = new HashMapConfiguration();
        configLocatorConfig.setProperty(TestDCCComponentConfigLocatorImpl.CONFIG_FOLDER_RELATIVE_PATH_PROPERTY_NAME, "/WEB-INF/conf/");
        ComponentInfo<IDCCComponentConfigResourceLocator> configLocatorInfo = 
            new ComponentInfo<IDCCComponentConfigResourceLocator>(
                CONFIG_RESOURCE_LOCATOR, 
                TestDCCComponentConfigLocatorImpl.class, 
                IDCCComponentConfigResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                configLocatorConfig);
        compMgr.getComponent(configLocatorInfo);
    }

    /**
     * Set up the Hibernate Data Sources
     */
    protected void setupDataSources() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDCCComponentConfigResourceLocator configResourceLocator = (IDCCComponentConfigResourceLocator) compMgr.getComponent(CONFIG_RESOURCE_LOCATOR);

        // Setup the Hibernate Data Sources
        Set<DestinyRepository> repositories = REQUIRED_DATA_REPOSITORIES;
        if (repositories != null) {
            for (DestinyRepository nextRepository : repositories) {
                IConfiguration nextDataSource = (IConfiguration) DESTINY_DATA_SOURCES.get(nextRepository);

                // Create the HibernateDataSource object only if the configs
                InputStream commonConfigFileStream = configResourceLocator.getConfigResourceAsStream(DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
                InputStream repositoryConfigFileStream = configResourceLocator.getConfigResourceAsStream(nextRepository.getConfigFileName());
                List dynamicMappings = null;
                HibernateTestUtils.createDataSource(nextRepository, commonConfigFileStream, repositoryConfigFileStream, nextDataSource, dynamicMappings);
            }
        }
    }


    public void setLifecycleManager (LifecycleManager lm) {
        this.lm = lm;
    }

    public void init () {
        // Setup the mock shared context
        ComponentInfo<IDestinySharedContextLocator> locatorInfo = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = ComponentManagerFactory.getComponentManager ().getComponent(locatorInfo);
        setupResourceLocator();
        setupConfigResourceLocator();
        setupDataSources();
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return null;
    }

}
