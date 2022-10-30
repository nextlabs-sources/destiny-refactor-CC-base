package com.bluejungle.pf.destiny.policymap;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author pkeni, sergey
 * 
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/policymap/PolicyDeploymentImplTest.java#1 $
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import sun.misc.BASE64Decoder;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.services.policy.types.AgentCapability;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.RegularExpressionStringTokenizer;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.axis.IPolicyDeployment;
import com.bluejungle.pf.destiny.lib.axis.PolicyDeploymentImpl;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.policy.TestUser;
import com.bluejungle.pf.tools.MockSharedContextLocator;
import com.bluejungle.pf.utils.PQLTestUtils;
import com.bluejungle.framework.test.OutputFileBasedTestSuite;

/**
 * Tests building maps and preparing bundles.
 */
public class PolicyDeploymentImplTest extends PFTestWithDataSource {

    private final static String SMALL_EXE = System.getProperty("java.io.tmpdir") + "small.exe";
    private static final String BASE_DIR_PROPERTY_NAME = "build.root.dir";

    private static final String ENROLLMENT_NAME = "PolicyDeploymentImplTest";
    private static final String TEST_POLICY_PREFIX = "POLICY MyTestPolicy\n" + "   DESCRIPTION \"My test policy\"\n" + "   FOR resource.name = \"C:\\\\\\\\temp\\\\**\"\n" + "   ON *\n" + "   BY (\n"
            + "   PRINCIPAL.USER.NAME = \"sgoldstein@bluejungle.com\")\n" + "   DO DENY\n" + "   BY DEFAULT DO ALLOW\n" + "   ON DENY DO log\n" + "   DEPLOYED TO (((FALSE OR FALSE OR host.did = ";
    private static final String TEST_POLICY_SUFFIX = ") WITH AGENT.TYPE = \"FILE_SERVER\") , ((FALSE OR FALSE) WITH AGENT.TYPE = \"DESKTOP\"))";

    private IPolicyDeployment deploymentImpl;
    private IOSWrapper osWrapper;
    private boolean rawResults = false;

    public PolicyDeploymentImplTest() throws Exception {
        oneTimeSetUp();
    }

    public PolicyDeploymentImplTest(String testName) throws Exception {
        super(testName);
        oneTimeSetUp();
    }

    private void oneTimeSetUp() throws Exception {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        ComponentInfo locatorInfo = new ComponentInfo(
                IDestinySharedContextLocator.COMP_NAME
              , MockSharedContextLocator.class.getName()
              , IDestinySharedContextLocator.class.getName()
              , LifestyleType.SINGLETON_TYPE
        );
        componentManager.registerComponent(locatorInfo, true);
        deploymentImpl = componentManager.getComponent(PolicyDeploymentImpl.COMP_INFO);
        this.osWrapper = componentManager.getComponent(OSWrapper.class);

        rawResults = "y".equalsIgnoreCase(System.getProperty("raw-bundle"));

        oneTimeSetUpClearPFDatabase();
        oneTimeSetupTestApplication();
        oneTimeSetUpLoadPolicies();
        oneTimeSetUpBuildMaps();
    }

    private void oneTimeSetUpClearPFDatabase() throws HibernateException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        Session session = dataSource.getSession();
        try {
            Transaction tx = session.beginTransaction();
            session.delete("from DeploymentEntity");
            session.delete("from DevelopmentEntity");
            session.delete("from DeploymentRecord");
            tx.commit();
        } finally {
            session.close();
        }
    }

    public void oneTimeSetupTestApplication() throws DictionaryException, IOException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IDictionary dictionary = (IDictionary) componentManager.getComponent(Dictionary.COMP_INFO);
        assertNotNull(dictionary);

        IEnrollment enrollment = dictionary.getEnrollment(ENROLLMENT_NAME);
        if (enrollment != null) {
            IConfigurationSession session = dictionary.createSession();
            try {
                session.beginTransaction();
                session.deleteEnrollment(enrollment);
                session.commit();
            } finally {
                session.close();
            }
        }

        enrollment = dictionary.makeNewEnrollment(ENROLLMENT_NAME);

        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }

        assertNotNull(enrollment);
        IEnrollmentSession es = enrollment.createSession();
        assertNotNull(es);
        IElementType appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        assertNotNull(appType);
        IElementField displayNameField = appType.getField(ApplicationReservedFieldEnumType.DISPLAY_NAME.getName());
        IElementField uniqueNameField = appType.getField(ApplicationReservedFieldEnumType.UNIQUE_NAME.getName());
        IElementField fingerPrintField = appType.getField(ApplicationReservedFieldEnumType.APP_FINGER_PRINT.getName());
        IElementField sysIDField = appType.getField(ApplicationReservedFieldEnumType.SYSTEM_REFERENCE.getName());


        File exeFile = new File(SMALL_EXE);
        FileOutputStream fos = new FileOutputStream(exeFile);
        BASE64Decoder dec = new BASE64Decoder();
        dec.decodeBuffer(new ByteArrayInputStream(SMALL_EXE_UUE.getBytes()), fos);
        fos.close();

        IMElement smallApp = enrollment.makeNewElement(new DictionaryPath(new String[] { "dc=com", "dc=destiny", "dc=destinydata", "o=applications", "cn=small" }), appType, new DictionaryKey("Destiny\\Applications\\small".getBytes()));

        assertNotNull(smallApp);
        smallApp.setValue(displayNameField, "small");
        smallApp.setDisplayName("small");
        smallApp.setValue(uniqueNameField, "small");
        smallApp.setUniqueName("small");
        smallApp.setValue(sysIDField, "small.exe");

        smallApp.setValue(fingerPrintField, osWrapper.getAppInfo(SMALL_EXE));

        Collection<IElementBase> elements = new ArrayList<IElementBase>();
        elements.add(smallApp);
        try {
            es.beginTransaction();
            es.saveElements(elements);
            es.commit();
        } catch (DictionaryException problem) {
            es.rollback();
            fail(problem.getMessage());
        } finally {
            es.close(true, null);
            exeFile.delete();
        }
    }

    public void oneTimeSetUpLoadPolicies() throws Exception {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IDictionary dictionary = (IDictionary) componentManager.getComponent(Dictionary.COMP_INFO);
        assertNotNull(dictionary);
        // Load up the right policies
        String buildRoot = System.getProperty("src.root.dir");
        String policyDirName = buildRoot + File.separator + "test_files" + File.separator + "com" + File.separator + "bluejungle" + File.separator + "pf" + File.separator + "engine" + File.separator + "destiny" + File.separator + "policies";
        File policyDir = new File(policyDirName);
        File[] allPQLs = policyDir.listFiles(new FilenameFilter() {
            public boolean accept(File d, String name) {
                return name.endsWith(".pql");
            }
        });

        final List<DevelopmentEntity> des = new ArrayList<DevelopmentEntity>();
        for (int i = 0; i < allPQLs.length; i++) {
            File file = allPQLs[i];
            final String pql = PFTestUtils.replaceDictionaryVariables(
                readFile(file)
            ,   dictionary
            );

            DevelopmentEntity de = new DevelopmentEntity(pql);
            des.add(de);
        }

        deployEntities(des);
    }

    /**
     * Deploy the entities provided
     * 
     * @param des the entities to deploy
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws PQLException
     */
    private void deployEntities(List<DevelopmentEntity> des) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, PQLException {
        for (DevelopmentEntity de : des) {
            PQLTestUtils.setStatus(de, DevelopmentStatus.APPROVED);
        }

        // Get the lifecycle manager
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        LifecycleManager lm = (LifecycleManager) componentManager.getComponent(LifecycleManager.COMP_INFO);
        Method deployEntities = lm.getClass().getDeclaredMethod(
                "deployEntities"
              , Collection.class
              , Date.class
              , DeploymentType.class
              , Date.class
              , Boolean.TYPE
              , Long.class
        );
        deployEntities.setAccessible(true);
        // Deploy as of two minutes ago
        long time = System.currentTimeMillis();
        deployEntities.invoke(
                lm
              , des
              , UnmodifiableDate.forTime(time)
              , DeploymentType.PRODUCTION
              , UnmodifiableDate.forTime(time - 120 * 1000)
              , Boolean.FALSE
              , null
        );
    }

    /**
     * Undeploy the entities provided
     * 
     * @param des
     *            the entities to deploy
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws PQLException
     */
    private void undeployEntities(List<DevelopmentEntity> des) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, PQLException {
        for (DevelopmentEntity de : des) {
            PQLTestUtils.setStatus(de, DevelopmentStatus.OBSOLETE);
        }

        // Get the lifecycle manager
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        LifecycleManager lm = (LifecycleManager) componentManager.getComponent(LifecycleManager.COMP_INFO);
        Method undeployEntities = lm.getClass().getDeclaredMethod(
                "undeployEntities"
              , Collection.class
              , Date.class
              , DeploymentType.class
              , Date.class
              , Boolean.TYPE
              , Long.class
        );
        undeployEntities.setAccessible(true);
        // Deploy as of two minutes ago
        long time = System.currentTimeMillis();
        undeployEntities.invoke(
                lm
              , des
              , UnmodifiableDate.forTime(time)
              , DeploymentType.PRODUCTION
              , UnmodifiableDate.forTime(time - 120 * 1000)
              , Boolean.FALSE
              , null
        );
    }

    public void oneTimeSetUpBuildMaps() throws Exception {
        IDeploymentBundle db = buildBundleForAgentTesting();
        String baseDir = System.getProperty(BASE_DIR_PROPERTY_NAME);
        if (baseDir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY_NAME + " system property must be specified for unit test to function");
        }
        // Store the bundle on the file system for later use
        FileOutputStream bundleFileOutputStream = new FileOutputStream("bundle.bin");
        GZIPOutputStream bundleZipFileOutputStream = new GZIPOutputStream(bundleFileOutputStream);
        ObjectOutputStream bundleObjectOutputStream = new ObjectOutputStream(bundleZipFileOutputStream);
        bundleObjectOutputStream.writeObject(db);
        bundleObjectOutputStream.flush();
        bundleObjectOutputStream.close();
    }

    private IDeploymentBundle buildBundleForAgentTesting() throws Exception {
        HashMapConfiguration strConfig = new HashMapConfiguration();
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        ServerTargetResolver str = cm.getComponent(ServerTargetResolver.COMP_INFO, strConfig);

        Method buildMaps = str.getClass().getDeclaredMethod(
                "buildMaps"
              , String[].class
              , Date.class
              , Long.TYPE
        );
        // Build maps as of one second from now
        buildMaps.setAccessible(true);
        // Max wait for the tests is two minutes
        buildMaps.invoke(
                str
              , new String[] { "bluejungle.com", "test.bluejungle.com" }
                , UnmodifiableDate.forTime(System.currentTimeMillis() + 1000)
                , new Long(120 * 1000)
        );

        List<SystemUser> systemUsers = new ArrayList<SystemUser>();
        Set<TestUser> allUsers = TestUser.getAllUsers();
        for (TestUser element : allUsers) {
            systemUsers.add(new SystemUser("windowsSid", element.getSID()));
        }

        String host = "savaii.bluejungle.com";

        DeploymentRequest request = new DeploymentRequest(systemUsers.toArray(new SystemUser[systemUsers.size()]), null, null, host, AgentTypeEnum.DESKTOP, null);
        IDeploymentBundle res = deploymentImpl.getDeploymentBundle(request, new Long(37), "bluejungle.com", null);

        return res;
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            // If there's an argument, prepare the approved bundle files
            OutputFileBasedTestSuite outputFileBasedTestSuite = getOutputFileBasedTestSuite();
            try {
                outputFileBasedTestSuite.generateApprovedFiles();
            } catch (Exception exception) {
                System.out.println("Failed to generate approved files:");
                exception.printStackTrace();
            }
        } else {
            junit.textui.TestRunner.run(suite());
        }
    }

    public static TestSuite suite() {
        return getOutputFileBasedTestSuite();
    }

    /**
     * Retrieve the file based test suite for this test class
     * 
     * @return the file based test suite for this test class
     */
    private static OutputFileBasedTestSuite getOutputFileBasedTestSuite() {
        return new OutputFileBasedTestSuite(PolicyDeploymentImplTest.class);
    }

    /**
     * @see com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test basic File Server Agent deployment
     * 
     * @param out
     *            the bundle output stream
     */
    public void testFSADeployment(OutputStream out) throws Exception {
        // Test FSA deployment
        String host = "savaii.bluejungle.com";
        String[] userSubjectTypes = { UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName() };
        IDeploymentBundle db = getFSADeploymentBundle(host, userSubjectTypes);
        assertNotNull("file server deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test File Server Agent deployment in second domain
     * 
     * @param out
     *            the bundle output stream
     */
    public void testFSADeploymentInSecondDomain(OutputStream out) throws Exception {
        // Test FSA deployment
        String host = "grande1.test.bluejungle.com";
        String[] userSubjectTypes = { UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName() };
        IDeploymentBundle db = getFSADeploymentBundle(host, userSubjectTypes);
        assertNotNull("file server deployment bundle for test domain should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test File Server Agent Deployment with multiple subject types
     * 
     * @param out
     *            the bundle output stream
     */
    public void testFSADeploymentWithMultipleSubjectTypes(OutputStream out) throws Exception {
        // Test FSA deployment
        String host = "savaii.bluejungle.com";
        String[] userSubjectTypes = { UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName(), UserSubjectTypeEnumType.LINUX_USER_SUBJECT_TYPE.getName() };
        IDeploymentBundle db = getFSADeploymentBundle(host, userSubjectTypes);
        assertNotNull("file server deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test File Server Agent targeted deployment
     * 
     * @param out
     *            the bundle output stream
     */
    public void testFSATargetedDeployment(OutputStream out) throws Exception {
        // Test FSA deployment
        String host = "savaii.bluejungle.com";
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        Dictionary dictionary = (Dictionary) componentManager.getComponent(Dictionary.COMP_INFO);
        IElementBase hostElement = dictionary.getByUniqueName(host, dictionary.getLatestConsistentTime());
        Long id = hostElement.getInternalKey();
        String policyToDeploy = TEST_POLICY_PREFIX + id + TEST_POLICY_SUFFIX;
        DevelopmentEntity devEntity = new DevelopmentEntity(policyToDeploy);
        deployEntities(Collections.singletonList(devEntity));

        String[] userSubjectTypes = { UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName() };
        IDeploymentBundle db = getFSADeploymentBundle(host, userSubjectTypes);
        assertNotNull("file server targeted deployment bundle should not be null", db);
        writeBundleToFile(db, out);

        // Now, try with a different host. Should not get policy
        String anotherHost = "sand.bluejungle.com";
        db = getFSADeploymentBundle(anotherHost, userSubjectTypes);
        assertNotNull("second file server targeted deployment bundle should not be null", db);
        writeBundleToFile(db, out);

        // Undeploy the policy
        undeployEntities(Collections.singletonList(devEntity));
        db = getFSADeploymentBundle(host, userSubjectTypes);
        assertNotNull("file server targeted deployment bundle should not be null after policy undeployment", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test Desktop Agent deployment with a single user
     * 
     * @param out
     *            the bundle output stream
     */
    public void testSingleUserDesktopDeployment(OutputStream out) throws Exception {
        SystemUser[] users = { new SystemUser(UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName(), "S-1-5-21-668023798-3031861066-1043980994-3481") };
        IDeploymentBundle db = getDesktopDeploymentBundle("nevis.bluejungle.com", users);
        assertNotNull("single user deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test Desktop Agent deployment with a multiple users
     * 
     * @param out
     *            the bundle output stream
     */
    public void testMultiUserDesktopDeployment(OutputStream out) throws Exception {
        SystemUser[] users = { new SystemUser(UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName(), "S-1-5-21-668023798-3031861066-1043980994-3481"),
                new SystemUser(UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName(), "S-1-5-21-668023798-3031861066-1043980994-3455") };
        IDeploymentBundle db = getDesktopDeploymentBundle("nevis.bluejungle.com", users);
        assertNotNull("single user deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test Desktop Agent deployment in second domain
     * 
     * @param out
     *            the bundle output stream
     */
    public void testDesktopDeploymentInTestDomain(OutputStream out) throws Exception {
        SystemUser[] users = { new SystemUser(UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName(), "S-1-5-21-830805687-550985140-3285839444-1168") };
        IDeploymentBundle db = getDesktopDeploymentBundle("grande1.test.bluejungle.com", users);
        assertNotNull("desktop deployment bundle in second domain should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test Desktop Agent deployment with an unknown user and host
     * 
     * @param out
     *            out the bundle output stream
     */
    public void testUnknownUsersHostsDesktopDeployment(OutputStream out) throws Exception {
        SystemUser[] users = { new SystemUser("windowsSid", "bad-sid-1"), new SystemUser("windowsSid", "bad-sid-2") };
        IDeploymentBundle db = getDesktopDeploymentBundle("nevis.bluejungle.com", users);
        assertNotNull("unknown host/user deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    /**
     * Test Desktop Agent deployment with a multiple users
     * 
     * @param out the bundle output stream
     */
    public void testTestUsersDesktopDeployment(OutputStream out) throws Exception {
        List<SystemUser> systemUsers = new ArrayList<SystemUser>();
        Set<TestUser> allUsers = TestUser.getAllUsers();
        for (TestUser element : allUsers) {
            systemUsers.add(new SystemUser("windowsSid", element.getSID()));
        }
        IDeploymentBundle db = getDesktopDeploymentBundle(
            "savaii.bluejungle.com"
        ,   systemUsers.toArray(new SystemUser[systemUsers.size()])
        );
        assertNotNull("single user deployment bundle should not be null", db);
        writeBundleToFile(db, out);
    }

    public void testAgentBundle(OutputStream out) throws Exception {
        writeBundleToFile(buildBundleForAgentTesting(), out);
    }

    /**
     * Utility method to create a file server agent bundle deployment request
     * and retrieve the associated bundle
     * 
     * @param host
     * @param userSubjectTypes
     * @return
     */
    private IDeploymentBundle getFSADeploymentBundle(String host, String[] userSubjectTypes) {
        DeploymentRequest request = new DeploymentRequest(null, userSubjectTypes, null, host, AgentTypeEnum.FILE_SERVER, null);

        return this.deploymentImpl.getDeploymentBundle(request, new Long(37), host.split("[.]", 2)[1], null);
    }

    /**
     * Utility method to create a desktop agent bundle deployment request and
     * retrieve the associated bundle
     * 
     * @param hostName
     *            TODO
     * @param users
     * 
     * @return
     */
    private IDeploymentBundle getDesktopDeploymentBundle(String hostName, SystemUser[] users) {
        DeploymentRequest request = new DeploymentRequest(
            users
        ,   null
        ,   new AgentCapability[] {AgentCapability.FILESYSTEM, AgentCapability.EMAIL}
        ,   hostName
        ,   AgentTypeEnum.DESKTOP
        ,   null);

        return this.deploymentImpl.getDeploymentBundle(request, new Long(38), hostName.split("[.]", 2)[1], null);
    }

    /**
     * Write a deployment bundle to a file which can be diff'd against an
     * approved test data file
     * 
     * @param db
     *            the bundle to write
     * @param outStream
     *            the output stream to write the data to
     * @throws PQLException
     *             if an error occurs while writing the data
     */
    private void writeBundleToFile(IDeploymentBundle db, OutputStream outStream) throws PQLException {
        String bundleAsString = PFTestUtils.toString(db, rawResults);

        // IDs might be different between bundle creations. Therefore, we
        // normalize them
        bundleAsString = replaceIds(bundleAsString);

        PrintStream printStream = new PrintStream(outStream);
        printStream.print(bundleAsString);
    }

    /**
     * Replace the IDs in the bundle. Currently, it just replaces them with a
     * sequence. Doing this, leads to some data loss, of course. However, it's
     * assumed that for most errors, the content of the file itself will be
     * incorrect (e.g. missing policy, one subject has too many groups, wrong
     * number of subjects, etc). So, this should still catch most errors
     * 
     * @param stringToProcess
     * @return
     */
    private String replaceIds(String stringToProcess) {
        StringBuffer processedStringBuffer = new StringBuffer();

        long nextId = 0;
        RegularExpressionStringTokenizer reTokenizer = new RegularExpressionStringTokenizer(stringToProcess, "ID [0-9]+", true);
        for (int i = 0; reTokenizer.hasNext(); i++) {
            String nextToken = (String) reTokenizer.next();
            if (!nextToken.startsWith("ID ")) {
                processedStringBuffer.append(nextToken);
            } else {
                processedStringBuffer.append("ID " + nextId++);
            }
        }

        return processedStringBuffer.toString();
    }

    private String readFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer rv = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                rv.append(line).append('\n');
            }
            reader.close();
            return rv.toString();
        } catch (IOException e) {
            TestCase.fail(e.getMessage());
            return null;
        }
    }

    private static final String SMALL_EXE_UUE = "TVqQAAMAAAAEAAAA//8AALgAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAA4AAAAA4fug4AtAnNIbgBTM0hVGhpcyBwcm9ncmFtIGNhbm5vdCBiZSBydW4gaW4gRE9TIG1v\r\n"
            + "ZGUuDQ0KJAAAAAAAAADU1xAjkLZ+cJC2fnCQtn5wE74jcJK2fnCQtn9wg7Z+cJW6I3CTtn5wlbpx\r\n" + "cJG2fnCVuiFwnbZ+cJW6HnCStn5wlbokcJG2fnBSaWNokLZ+cAAAAAAAAAAAAAAAAAAAAABQRQAA\r\n"
            + "TAEDAMB59EQAAAAAAAAAAOAADwELAQcKAAQAAAAGAAAAAAAAAxAAAAAQAAAAIAAAAABAAAAQAAAA\r\n" + "AgAABAAAAAAAAAAEAAAAAAAAAABAAAAABAAAAAAAAAMAAAAAABAAABAAAAAAEAAAEAAAAAAAABAA\r\n"
            + "AAAAAAAAAAAAAOQgAAA8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIggAABIAAAAAAAAAAAAAAAAIAAAVAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAC50ZXh0AAAA4gIAAAAQAAAABAAAAAQAAAAAAAAAAAAAAAAAACAAAGAu\r\n" + "cmRhdGEAAJwCAAAAIAAAAAQAAAAIAAAAAAAAAAAAAAAAAABAAABALmRhdGEAAAA0AAAAADAAAAAC\r\n"
            + "AAAADAAAAAAAAAAAAAAAAAAAQAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADPA\r\n"
            + "w2ooaFggQADodQIAADP/V/8VACBAAGaBOE1adR+LSDwDyIE5UEUAAHUSD7dBGD0LAQAAdB89CwIA\r\n" + "AHQFiX3k6yeDuYQAAAAOdvIzwDm5+AAAAOsOg3l0DnbiM8A5uegAAAAPlcCJReSJffxqAf8VNCBA\r\n"
            + "AFmDDSwwQAD/gw0wMEAA//8VMCBAAIsNJDBAAIkI/xUsIEAAiw0gMEAAiQihSCBAAIsAoygwQADo\r\n" + "LwEAAOjKAQAAOT0QMEAAdQxogBJAAP8VJCBAAFnonwEAAGgMMEAAaAgwQADoigEAAGgkEkAA6OYA\r\n"
            + "AAChHDBAAIlF3I1F3FD/NRgwQACNReBQjUXYUI1F1FD/FRwgQACDxCCJRcw7x30IagjohQAAAFlo\r\n" + "BDBAAGgAMEAA6DsBAAD/FRQgQACLTeCJCP914P912P911Oi6/v//g8QUi/CJdcg5feR1B1b/FRAg\r\n"
            + "QAD/FQwgQADrLYtF7IsIiwmJTdBQUegpAAAAWVnDi2Xoi3XQg33kAHUHVv8VKCBAAP8VTCBAAINN\r\n" + "/P+LxuglAQAAw8z/JQggQAD/JRggQACDPTAwQAD/dQb/JUAgQABoLDBAAGgwMEAA/3QkDOgMAQAA\r\n"
            + "g8QMw/90JATo0f////fYG8D32FlIw2oMaGggQADomAAAAMdF5NggQACBfeTYIEAAcyKDZfwAi0Xk\r\n" + "iwCFwHQL/9DrBzPAQMOLZeiDTfz/g0XkBOvV6JwAAADDagxoeCBAAOhUAAAAx0Xk4CBAAIF95OAg\r\n"
            + "QABzIoNl/ACLReSLAIXAdAv/0OsHM8BAw4tl6INN/P+DReQE69XoWAAAAMP/JSAgQABoAAADAGgA\r\n" + "AAEA6F8AAABZWcMzwMPMaNASQABkoQAAAABQi0QkEIlsJBCNbCQQK+BTVleLRfiJZehQi0X8x0X8\r\n"
            + "/////4lF+I1F8GSjAAAAAMOLTfBkiQ0AAAAAWV9eW8lRw/8lOCBAAP8lPCBAAP8lRCBAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB6IgAA\r\n"
            + "AAAAAIYhAACUIQAAniEAAKYhAAC2IQAAxCEAANQhAADgIQAAfiEAAAQiAAAUIgAAIiIAADQiAABU\r\n" + "IgAAYiIAAGwiAAD0IQAAdCEAAAAAAAAAAAAA/////2IRQAB2EUAAAAAAAP////8NEkAAERJAAAAA\r\n"
            + "AAD/////URJAAFUSQAAAAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAFDBAANAgQAABAAAA0BIAAAAAAAAAAAAAAAAAAAAAAAAoIQAA\r\n"
            + "AAAAAAAAAABIIgAACCAAACAhAAAAAAAAAAAAAI4iAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB6\r\n" + "IgAAAAAAAIYhAACUIQAAniEAAKYhAAC2IQAAxCEAANQhAADgIQAAfiEAAAQiAAAUIgAAIiIAADQi\r\n"
            + "AABUIgAAYiIAAGwiAAD0IQAAdCEAAAAAAADKAF9jX2V4aXQA+gBfZXhpdABLAF9YY3B0RmlsdGVy\r\n" + "AM0AX2NleGl0AACXAmV4aXQAAHwAX19wX19faW5pdGVudgDCAF9hbXNnX2V4aXQAAG4AX19nZXRt\r\n"
            + "YWluYXJncwA/AV9pbml0dGVybQCfAF9fc2V0dXNlcm1hdGhlcnIAALsAX2FkanVzdF9mZGl2AACC\r\n" + "AF9fcF9fY29tbW9kZQAAhwBfX3BfX2Ztb2RlAACcAF9fc2V0X2FwcF90eXBlAADxAF9leGNlcHRf\r\n"
            + "aGFuZGxlcjMAAE1TVkNSNzEuZGxsAGsAX19kbGxvbmV4aXQAuAFfb25leGl0ANsAX2NvbnRyb2xm\r\n" + "cAAAdwFHZXRNb2R1bGVIYW5kbGVBAABLRVJORUwzMi5kbGwAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAEAAABO5kC7AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
}
