package com.nextlabs.pf.destiny.importexport.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;
import com.nextlabs.pf.destiny.importexport.ConflictResolution;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.ConflictResolution.ConflictType;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.impl.Importer;

/**
 * @author hchan
 */
public class ImporterTest extends ImportExportSharedTest {
    public ImporterTest() throws LoginException {
        super();
    }

    public void testInitializeImport() throws ImportException {
        File testFile = new File(srcFilesFolder, POLICY_XML_1);
        Importer importer = new Importer(testFile, Shallow.SHALLOW); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(1, fileContents.size());
        ExportEntity expectec = fileContents.iterator().next();
        assertEquals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST", expectec.getName());
        assertEquals("POLICY", expectec.getType());
    }
    
    public void testDoImport() throws ImportException, PolicyEditorException {
        //setup from testInitializeImport test
        File testFile = new File(srcFilesFolder ,POLICY_XML_1);
        Importer importer = new Importer(testFile, Shallow.SHALLOW); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        //no test conflict
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        assertTrue("Import should not have any conflict",conflicts.isEmpty());
     
        assertEquals(1, fileContents.size());
        ExportEntity entity = fileContents.iterator().next();
        assertEquals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST", entity.getName());
        assertEquals("POLICY", entity.getType());
     
        //make sure there is not should item before import
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(0, policies.size());
     
        importer.commitImport();
     
        //check if the items have been imported
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
  
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(policies);
        assertEquals(1, entities.size());
  
        IPolicy policy = ((IPolicy)entities.iterator().next());
        assertEquals("TESTPrevents that duplication of Compensation Reporting data to uncontrolled locations " +
                     "and the distibution of this information via email or instant messaging.",
                     policy.getDescription());
        assertEquals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST", policy.getName());
  
    }
  
    
    public void testCommitNew() throws ImportException, PolicyEditorException {
        //import the same data to the database first
        testDoImport();
     
        //setup from testInitializeImport and testDoImport tests
        File testFile = new File(srcFilesFolder, POLICY_XML_2);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        //should NOT be empty
        assertEquals(1, conflicts.size());
     
        IImportConflict conf = importState.getConflicts().iterator().next();
        conf.setResolution(ConflictResolution.KEEP_NEW);

        //there should already have one same policy before I import
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
     
        importer.commitImport();
     
        //check if the items have been imported
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
  
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(policies);
        assertEquals(1, entities.size());
  
        IPolicy policy = ((IPolicy)entities.iterator().next());
        assertEquals("Of course I am somewhat different than testPolicy1.xml", policy.getDescription());
        assertEquals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST", policy.getName());
    }
    
    public void testCommitOld() throws ImportException, PolicyEditorException {
        //import the same data to the database first
        testDoImport();
     
        //setup from testInitializeImport and testDoImport tests
        File testFile = new File(srcFilesFolder, POLICY_XML_2);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        //should NOT be empty
        assertEquals("It should have 1 conflict on import", 1, conflicts.size());
     
        IImportConflict conf = importState.getConflicts().iterator().next();
        conf.setResolution(ConflictResolution.KEEP_OLD);

        //there should already have one same policy before I import
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
     
        importer.commitImport();
     
        //check if the items have been imported
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
  
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(policies);
        assertEquals(1, entities.size());
  
        IPolicy policy = ((IPolicy)entities.iterator().next());
        assertEquals("TESTPrevents that duplication of Compensation Reporting data to uncontrolled locations " +
                     "and the distibution of this information via email or instant messaging.",
                     policy.getDescription());
        assertEquals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST", policy.getName());
    }

    //Individual component, and renaming of that component
    public void testCommitRename() throws ImportException, PolicyEditorException {
        final String renameSuffix = " testCommitRename";
     
        //import the same data to the database first
        testDoImport();
     
        //setup from testInitializeImport and testDoImport tests
        File testFile = new File(srcFilesFolder, POLICY_XML_2);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        //should NOT be empty
        assertEquals("It should have 1 conflict on import", 1, conflicts.size());
     
        IImportConflict conf = importState.getConflicts().iterator().next();
        conf.setResolution(new ConflictResolution(ConflictType.RENAME_NEW, conf.getName() + "very special name"));

        //there should already have one same policy before I import
        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/Compensation Data DuplicationTEST", EntityType.POLICY, false);
        assertEquals(1, policies.size());
     
        importer.commitImport();
     
        //check if the items have been imported
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Compensation Planning/%", EntityType.POLICY, false);
        assertEquals(2, policies.size());
  
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(policies);
        assertEquals(2, entities.size());
  
        //hard to tell the new import item is the first one or second one

        for(IHasId entity : entities){
            IPolicy policy = (IPolicy)entity;
            if(policy.getName().equals("Unit Test/Compensation Planning/Compensation Data DuplicationTEST")){
                assertEquals("TESTPrevents that duplication of Compensation Reporting data to uncontrolled locations " +
                             "and the distibution of this information via email or instant messaging.",
                             policy.getDescription());
            }else if(policy.getName().startsWith("Unit Test/Compensation Planning/Compensation Data DuplicationTEST")){
                assertEquals("Of course I am somewhat different than testPolicy1.xml", policy.getDescription());
            }else{
                fail();
            }
        }
    }
 
    public void testhkImport() throws ImportException, PolicyEditorException {
        File testFile = new File(srcFilesFolder, POLICY_XML_3);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(30, fileContents.size());
        int numOfPolicy = 0;
        int numOfComponent = 0;
        for(ExportEntity exportEntity : fileContents){
            if( exportEntity.getType().equals("POLICY")){
                numOfPolicy++;
            }else if(exportEntity.getType().equals("COMPONENT")){
                numOfComponent++;
            }else{
                fail();
            }
        }
        assertEquals(11, numOfPolicy);
        assertEquals(19, numOfComponent);
     
        //try to doImport
        IImportState importState = importer.doImport(fileContents);
        assertEquals(0, importState.getConflicts().size());

        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "%", EntityType.POLICY, false);
        assertEquals(0, policies.size());
     
        Collection<DomainObjectDescriptor> components = client.getDescriptorsForNameAndType(
            "%", EntityType.COMPONENT, false);
        assertEquals(0, components.size());
     
        importer.commitImport();
     
        policies = client.getDescriptorsForNameAndType("%", EntityType.POLICY, false);
        assertEquals(11, policies.size());
     
        components = client.getDescriptorsForNameAndType("%", EntityType.COMPONENT, false);
        assertEquals(19, components.size());
     
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Policy Lvl1", EntityType.POLICY, false);
        assertEquals(1, policies.size());
     
        CompositePredicate cp, cp2;
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(policies);
        assertEquals(1, entities.size());
        IPolicy policy = (IPolicy)entities.iterator().next();
        IPredicate action = policy.getTarget().getActionPred();
     
        //assertEqual to (TRUE) AND ((NAME='Action/Coopy') OR (NAME='Action/delette'))
        assertContain(action, "Action/Coopy");
        assertContain(action, "Action/delette");
  
        //(TRUE) AND ((FALSE) OR (TRUE))
        IPredicate fromResource = policy.getTarget().getFromResourcePred();
        cp = (CompositePredicate)fromResource;
        assertEquals(cp.getOp(), BooleanOp.AND );
        assertEquals(cp.predicateAt(0), PredicateConstants.TRUE);
        cp2 = (CompositePredicate)cp.predicateAt(1);
        assertEquals(cp2.getOp(), BooleanOp.OR );
        assertEquals(cp2.predicateAt(0), PredicateConstants.FALSE);
        assertEquals(cp2.predicateAt(1), PredicateConstants.TRUE);

        //((TRUE) AND ((FALSE) OR (NAME='User/Robert'))) AND ((TRUE) AND ((FALSE) OR (NAME='Host/Computer Bs'))) AND ((TRUE) AND (TRUE))
        IPredicate subject = policy.getTarget().getSubjectPred();
        assertContain(subject, "User/Robert");
        assertContain(subject, "Host/Computer Bs");

        //(TRUE) AND (TRUE)
        IPredicate toResource = policy.getTarget().getToResourcePred();
        cp = (CompositePredicate)toResource;
        assertEquals(cp.getOp(), BooleanOp.AND );
        assertEquals(cp.predicateAt(0), PredicateConstants.TRUE);
        assertEquals(cp.predicateAt(1), PredicateConstants.TRUE);
    }
    
    public void testhkImportAgain() throws ImportException, PolicyEditorException {
        final String renameSuffix = " testhkImportAgain";
     
        //import one time first
        //and try to import the same thing again
        File testFile = new File(srcFilesFolder, POLICY_XML_3);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        importer.doImport(importer.getEntities());
        importer.commitImport();
     
        importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(30, fileContents.size());
        int numOfPolicy = 0;
        int numOfComponent = 0;
        for(ExportEntity exportEntity : fileContents){
            if( exportEntity.getType().equals("POLICY")){
                numOfPolicy++;
            }else if(exportEntity.getType().equals("COMPONENT")){
                numOfComponent++;
            }else{
                fail();
            }
        }
        assertEquals(11, numOfPolicy);
        assertEquals(19, numOfComponent);
     
        //try to doImport
        IImportState importState = importer.doImport(fileContents);
        assertEquals(30, importState.getConflicts().size());

        Collection<DomainObjectDescriptor> policies = client.getDescriptorsForNameAndType(
            "%", EntityType.POLICY, false);
        assertEquals(11, policies.size());
     
        Collection<DomainObjectDescriptor> components = client.getDescriptorsForNameAndType(
            "%", EntityType.COMPONENT, false);
        assertEquals(19, components.size());
     
        Collection<IImportConflict> conflicts = importState.getConflicts();
        assertEquals(30, conflicts.size());
     
        //should throw exception if I don;t solve all the conflict
        try {
            importer.commitImport();
            fail();
        } catch (ImportException e) {
            assertNotNull(e);
        }
        
        for(IImportConflict conflict : conflicts){
            conflict.setResolution(new ConflictResolution(ConflictType.RENAME_NEW, conflict.getName() + renameSuffix));
        }
  
        importer.commitImport();
     
        policies = client.getDescriptorsForNameAndType("%", EntityType.POLICY, false);
        assertEquals(22, policies.size());
     
        components = client.getDescriptorsForNameAndType("%", EntityType.COMPONENT, false);
        assertEquals(38, components.size());
     
        policies = client.getDescriptorsForNameAndType(
            "Unit Test/Policy Lvl1%", EntityType.POLICY, false);
        assertEquals(2, policies.size());
     
        DomainObjectDescriptor theNewPolcy = null;
        for(DomainObjectDescriptor policy : policies){
            if (policy.getName().startsWith("Unit Test/Policy Lvl1" + renameSuffix)) {
                theNewPolcy = policy;
            }
        }
     
        assertNotNull(theNewPolcy);
     
        CompositePredicate cp, cp2;
        Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(Collections.singleton(theNewPolcy));
        assertEquals(1, entities.size());
     
        IPolicy policy = (IPolicy)entities.iterator().next();
        IPredicate action = policy.getTarget().getActionPred();
        //assertEqual to (TRUE) AND ((NAME='Action/Coopy') OR (NAME='Action/delette'))
        assertNotContain(action, "Action/Coopy");
        assertNotContain(action, "Action/delette");
        assertContain(action, "Action/Coopy"+renameSuffix);
        assertContain(action, "Action/delette"+renameSuffix);
  
        //(TRUE) AND ((FALSE) OR (TRUE))
        IPredicate fromResource = policy.getTarget().getFromResourcePred();
        cp = (CompositePredicate)fromResource;
        assertEquals(cp.getOp(), BooleanOp.AND );
        assertEquals(cp.predicateAt(0), PredicateConstants.TRUE);
        cp2 = (CompositePredicate)cp.predicateAt(1);
        assertEquals(cp2.getOp(), BooleanOp.OR );
        assertEquals(cp2.predicateAt(0), PredicateConstants.FALSE);
        assertEquals(cp2.predicateAt(1), PredicateConstants.TRUE);

        //((TRUE) AND ((FALSE) OR (NAME='User/Robert'))) AND ((TRUE) AND ((FALSE) OR (NAME='Host/Computer Bs'))) AND ((TRUE) AND (TRUE))
        IPredicate subject = policy.getTarget().getSubjectPred();
        assertNotContain(subject, "User/Robert");
        assertNotContain(subject, "Host/Computer Bs");
        assertContain(subject, "User/Robert"+renameSuffix);
        assertContain(subject, "Host/Computer Bs"+renameSuffix);

        //(TRUE) AND (TRUE)
        IPredicate toResource = policy.getTarget().getToResourcePred();
        cp = (CompositePredicate)toResource;
        assertEquals(cp.getOp(), BooleanOp.AND );
        assertEquals(cp.predicateAt(0), PredicateConstants.TRUE);
        assertEquals(cp.predicateAt(1), PredicateConstants.TRUE);
    }
    
    public void testShallowImportNonShallow() throws ImportException, PolicyEditorException{
        final String renameSuffix = " testShallowImportNonShallow";
     
        File testFile = new File(srcFilesFolder, POLICY_XML_3);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        importer.doImport(importer.getEntities());
        importer.commitImport();
     
        testFile = new File(srcFilesFolder, POLICY_XML_3_MOD);
        importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(5, fileContents.size());
        int numOfPolicy = 0;
        int numOfComponent = 0;
        for(ExportEntity exportEntity : fileContents){
            if( exportEntity.getType().equals("POLICY")){
                numOfPolicy++;
            }else if(exportEntity.getType().equals("COMPONENT")){
                numOfComponent++;
            }else{
                fail();
            }
        }
        assertEquals(1, numOfPolicy);
        assertEquals(4, numOfComponent);
     
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        assertEquals(5, conflicts.size());
        for(IImportConflict conflict : conflicts){
            conflict.setResolution(new ConflictResolution(ConflictType.RENAME_NEW, conflict.getName() + renameSuffix));
        }
     
        importer.commitImport();
     
        Collection<? extends IHasId> components = client.getEntitiesForNamesAndType(Collections.singleton("User/Robert"
                                                                                                          + renameSuffix), EntityType.COMPONENT, false);
        assertEquals(1, components.size());

        SpecBase robert = (SpecBase) components.iterator().next();
        assertContain(robert.getPredicate(), -1);
     
        components = client.getEntitiesForNamesAndType(Collections.singleton("User/Robert"), EntityType.COMPONENT,
                                                       false);
        assertEquals(1, components.size());
     
        robert = (SpecBase)components.iterator().next();
        assertContain( robert.getPredicate(), -1 );
   
    }
    
    public void testHierarchy() throws ImportException, PolicyEditorException {
        final String renameSuffix = " testHierarchy";
        //import one time first
        //and try to import the same thing again
        File testFile = new File(srcFilesFolder, POLICY_XML_4);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        importer.doImport(importer.getEntities());
        importer.commitImport();
     
        importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        //try to doImport
        IImportState importState = importer.doImport(fileContents);
     
        Collection<IImportConflict> conflicts = importState.getConflicts();
        for(IImportConflict conflict : conflicts){
            conflict.setResolution(new ConflictResolution(ConflictType.RENAME_NEW, conflict.getName() + renameSuffix));
        }
        importer.commitImport();
  
        Collection<DomainObjectDescriptor> components;
        Collection<? extends IHasId> entities;
        SpecBase specBase;
        IPredicate predicate;
     
        components = client.getDescriptorsForNameAndType("User/Something mixed 2"
                                                         + renameSuffix, EntityType.COMPONENT, false);
        assertEquals(1, components.size());
        entities = client.getEntitiesForDescriptors(components);
        assertEquals(1, entities.size());   
        specBase = (SpecBase)entities.iterator().next();
        predicate = specBase.getPredicate();
        assertNotContain(predicate, "User/SomethingFlat");
        assertNotContain(predicate, "User/SomethingHier Empty");
        assertNotContain(predicate, "User/SomethingHier Flat");
        assertContain(predicate, "User/SomethingFlat"+renameSuffix);
        assertContain(predicate, "User/SomethingHier Empty"+renameSuffix);
        assertContain(predicate, "User/SomethingHier Flat"+renameSuffix);
  
  
        components = client.getDescriptorsForNameAndType("User/SomethingHier Empty"
                                                         + renameSuffix, EntityType.COMPONENT, false);
        assertEquals(1, components.size());
        entities = client.getEntitiesForDescriptors(components);
        assertEquals(1, entities.size());   
        specBase = (SpecBase)entities.iterator().next();
        predicate = specBase.getPredicate();
        assertNotContain(predicate, "User/Empty");
        assertContain(predicate, "User/Empty"+renameSuffix);
  
        components = client.getDescriptorsForNameAndType("User/SomethingHier Flat"
                                                         + renameSuffix, EntityType.COMPONENT, false);
        assertEquals(1, components.size());
        entities = client.getEntitiesForDescriptors(components);
        assertEquals(1, entities.size());   
        specBase = (SpecBase)entities.iterator().next();
        predicate = specBase.getPredicate();
        assertNotContain(predicate, "User/SomethingFlat");
        assertContain(predicate, "User/SomethingFlat"+renameSuffix);
        //  
        //  //(TRUE) AND ((FALSE) OR (TRUE))
        //  IPredicate fromResource = policy.getTarget().getFromResourcePred();
        //  cp = (CompositePredicate)fromResource;
        //  assertEquals(cp.getOp(), BooleanOp.AND );
        //  assertEquals((PredicateConstants)cp.predicateAt(0), PredicateConstants.TRUE);
        //  cp2 = (CompositePredicate)cp.predicateAt(1);
        //  assertEquals(cp2.getOp(), BooleanOp.OR );
        //  assertEquals((PredicateConstants)cp2.predicateAt(0), PredicateConstants.FALSE);
        //  assertEquals((PredicateConstants)cp2.predicateAt(1), PredicateConstants.TRUE);
        //
        //  //((TRUE) AND ((FALSE) OR (NAME='User/Robert'))) AND ((TRUE) AND ((FALSE) OR (NAME='Host/Computer Bs'))) AND ((TRUE) AND (TRUE))
        //  IPredicate subject = policy.getTarget().getSubjectPred();
        //  assertNotContain(subject, "User/Robert");
        //  assertNotContain(subject, "Host/Computer Bs");
        //  assertContain(subject, "User/Robert"+RENAME_SUFFIX);
        //  assertContain(subject, "Host/Computer Bs"+RENAME_SUFFIX);
        //
        //  //(TRUE) AND (TRUE)
        //  IPredicate toResource = policy.getTarget().getToResourcePred();
        //  cp = (CompositePredicate)toResource;
        //  assertEquals(cp.getOp(), BooleanOp.AND );
        //  assertEquals((PredicateConstants)cp.predicateAt(0), PredicateConstants.TRUE);
        //  assertEquals((PredicateConstants)cp.predicateAt(1), PredicateConstants.TRUE);
    }
    
    public void testGetCommitData(){
        //TODO
    }
    
    public void testSolveAllUnconflicted(){
        //TODO
    }
    
    public void testSolveAllImportConflicts(){
        //TODO
     
    }
    
    public void testNonShallowImport(){
        //TODO
    }
    
    public void testShallowImport(){
        //TODO
    }
    
    public void testRenameReference() throws ImportException{
        File testFile = new File(srcFilesFolder, POLICY_XML_1);
        ImporterMod importer = new ImporterMod(testFile, Shallow.FULL); 
        assertEquals(importer.getRenameMapping().size(), 0);
        SpecReference pred = new SpecReference("name");
     
     
    }
    
    public void testIncorrectNewnameToSolveConflict() throws ImportException, PolicyEditorException{
        File testFile = new File(srcFilesFolder, POLICY_XML_3);
        Importer importer = new Importer(testFile, Shallow.FULL); 
        importer.doImport(importer.getEntities());
        importer.commitImport();
     
        importer = new Importer(testFile, Shallow.FULL); 
        Collection<ExportEntity> fileContents = importer.getEntities();
     
        assertEquals(30, fileContents.size());
        int numOfPolicy = 0;
        int numOfComponent = 0;
        for(ExportEntity exportEntity : fileContents){
            if( exportEntity.getType().equals("POLICY")){
                numOfPolicy++;
            }else if(exportEntity.getType().equals("COMPONENT")){
                numOfComponent++;
            }else{
                fail();
            }
        }
        assertEquals(11, numOfPolicy);
        assertEquals(19, numOfComponent);
     
        //try to doImport
        IImportState importState = importer.doImport(fileContents);
        Collection<IImportConflict> conflicts = importState.getConflicts();
        assertEquals(30, conflicts.size());
     
        for(IImportConflict conflict : conflicts){
            try {
                conflict.setResolution(new ConflictResolution(ConflictType.RENAME_NEW, conflict.getName()));
                fail();
            } catch (ImportException e) {
                assertNotNull(e);
            }
        }
    }
    
    private void assertContain(IPredicate predicate, String value, boolean isContain){
        PredicateValueMatcher pvm = new PredicateValueMatcher(value);
        predicate.accept(pvm, IPredicateVisitor.POSTORDER);
        assertEquals(isContain, pvm.isFound());
    }
    
    private void assertContain(IPredicate predicate, String value){
        assertContain(predicate,value, true);
    }
    
    private void assertNotContain(IPredicate predicate, String value){
        assertContain(predicate,value, false);
    }
    
    private void assertContain(IPredicate predicate, long value, boolean isContain) {
        PredicateValueMatcher pvm = new PredicateValueMatcher(value);
        predicate.accept(pvm, IPredicateVisitor.POSTORDER);
        assertEquals("value=" + value, isContain, pvm.isFound());
    }
    
    private void assertContain(IPredicate predicate, long value){
        assertContain(predicate,value, true);
    }
    
    private void assertNotContain(IPredicate predicate, long value){
        assertContain(predicate,value, false);
    }
    
    
    @SuppressWarnings("unused")
        private class PredicateValueMatcher implements IPredicateVisitor{
            private final String specReferenceValue;
            private final Long relationValue;
            private boolean found;

            public final boolean isFound() {
                return found;
            }

            public PredicateValueMatcher(String specReferenceValue) {
                super();
                this.specReferenceValue = specReferenceValue;
                relationValue = null;
                found = false;
            }
  
            public PredicateValueMatcher(long relationValue) {
                super();
                this.specReferenceValue = null;
                this.relationValue = relationValue;
                found = false;
            }

            public void visit(ICompositePredicate arg0, boolean arg1) { }

            public void visit(IPredicate arg0) { }

            public void visit(IPredicateReference arg0) {
                if(specReferenceValue != null 
                   && arg0 instanceof SpecReference 
                   && ((SpecReference)arg0).getReferencedName().equals(specReferenceValue) ){
                    found = true;
                }
            }

            public void visit(IRelation arg0) { 
                if (relationValue != null 
                    && arg0.getLHS() instanceof SubjectAttribute 
                    && arg0.getRHS() instanceof Constant) {
                    if((Long)((Constant)arg0.getRHS()).getValue().getValue() == relationValue.longValue()){
                        found = true;
                    }
                }
            }
        }
    
    private class ImporterMod extends Importer {
        private HashMap<NameAndType, String> renameMapping = new HashMap<NameAndType, String>();

        public ImporterMod(File importSource, Shallow failRecovery) throws ImportException {
            super(importSource, failRecovery);
        }

        @Override
        public HashMap<NameAndType, String> getRenameMapping() {
            return renameMapping;
        }

        public void setRenameMapping(HashMap<NameAndType, String> renameMapping) {
            this.renameMapping = renameMapping;
        }
    }
}
