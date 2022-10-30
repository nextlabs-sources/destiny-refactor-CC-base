	package com.bluejungle.pf.destiny.parser;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/parser/TestDomainObjectBuilder.java#1 $
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.LocationReference;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

public class TestDomainObjectBuilder extends TestCase {

    public static Test suite() {
        return new TestSuite(TestDomainObjectBuilder.class);
    }

    public void testSimplePolicy() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "policy first_policy\n"+
            "    description \"this policy is trivial\"\n"+
            "    attribute governance\n"+
            "    DO ALLOW \n"+
            "    FOR * ON * TO * BY * // an all-inclusive target\n"+
            "    ON DENY DO LOG \"Access denied\"\n"+
            "    ON ALLOW DO LOG \"Access granted\""
        );

        IDPolicy p = dob.processPolicy();
        // The policy and its attributes
        assertNotNull( "Parser returned a null policy", p );
        assertEquals( "first_policy", p.getName() );
        assertEquals( "this policy is trivial", p.getDescription() );
        assertFalse( "This is not an enforcement policy", p.hasAttribute("enforcement") );
        assertFalse( "This is not a monitoring policy", p.hasAttribute("monitoring"));
        assertFalse( "This is not a tracking policy", p.hasAttribute("tracking"));
        assertTrue( "This is a governance policy", p.hasAttribute("governance"));
        // The main effect
        IDEffectType e = (IDEffectType)p.getMainEffect();
        assertEquals( EffectType.ALLOW, e );
        // The target
        ITarget t = p.getTarget();
        assertNotNull( "Policy must have a non-null target", t );
        // The resources
        IPredicate fr = t.getFromResourcePred();
        assertNotNull( "The 'from' resource of the policy's target must not be null", fr );
        assertSame( "The 'from' resource of the policy's target must be <TRUE>", PredicateConstants.TRUE, fr );
        IPredicate tr = t.getToResourcePred();
        assertNotNull( "The 'to' resource of the policy's target must not be null", tr );
        assertSame( "The 'to' resource of the policy's target must be <TRUE>", PredicateConstants.TRUE, tr );
        // The action
        IPredicate a = t.getActionPred();
        assertNotNull( "The action of the policy's target must not be null", a );
        assertSame( "The action of the policy's target must be <TRUE>", PredicateConstants.TRUE, a);
        // The subject
        IPredicate subj = t.getSubjectPred();
        assertNotNull( "The subject of the policy's target must not be null", subj );
        assertSame( "The subject of the policy's target must be *", PredicateConstants.TRUE, subj );
        // The obligations
        IObligation onAllow[] = p.getObligationArray(EffectType.ALLOW);
        assertNotNull(onAllow);
        assertEquals( 1, onAllow.length );
        IObligation onDeny[] = p.getObligationArray(EffectType.DENY);
        assertNotNull(onDeny);
        assertEquals( 1, onDeny.length );
        IObligation onDontcare[] = p.getObligationArray(EffectType.DONT_CARE);
        assertNotNull(onDontcare);
        assertEquals( 0, onDontcare.length );
    }

    public void testAtomicResourceExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "policy second_policy\n"+
            "    DO DENY\n"+
            "    FOR\n"+
            "        ALL OR  RESOURCE.GROUP IN EXCEL_DOCS"+
            "    ON * TO"+
            "         NOT * AND NOT LEGAL_DOCS\n"+
            "    BY *"
        );

        IDPolicy p = dob.processPolicy();
        // The policy and its attributes
        assertNotNull( "Parser returned a null policy", p );
        // Get the target...
        ITarget t = p.getTarget();
        assertNotNull( "Policy must have a non-null target", t );
        // The resources
        IPredicate fr = t.getFromResourcePred();
        assertNotNull( "The 'from' resource of the policy's target must not be null", fr );

        // Check the 'from' resource
        assertTrue( "Top-level 'from' resource must be a group", fr instanceof ICompositePredicate );
        ICompositePredicate g = (ICompositePredicate)fr;
        assertEquals("Group must be connect with OR", BooleanOp.OR, g.getOp());
        assertEquals("OR must have two elements", 2, g.predicateCount());
        assertSame("First element must be <TRUE>", PredicateConstants.TRUE, g.predicateAt(0));
        assertTrue("Second element must be a ResourceSpecRef", g.predicateAt(1) instanceof IPredicateReference );
        g.predicateAt(1).accept( new DefaultPredicateVisitor() {
            public void visit( IPredicateReference pred ) {
                assertEquals("The referenced spec must be named EXCEL_DOCS", "EXCEL_DOCS", ((IDSpecRef)pred).getReferencedName());
            }
        }, IPredicateVisitor.PREORDER );

        // Check the 'to' resource
        IPredicate tr = t.getToResourcePred();
        assertNotNull( "The 'to' resource of the policy's target must not be null", tr );
        assertTrue( "Top-level 'to' resource must be a group", tr instanceof ICompositePredicate );
        g = (ICompositePredicate)tr;
        assertEquals( "Group must be connect with AND", BooleanOp.AND, g.getOp() );
        assertEquals( "AND must have two elements", 2, g.predicateCount() );
        ICompositePredicate g1 = (ICompositePredicate) g.predicateAt(0);
        assertTrue( "First element must be a group", g1 instanceof ICompositePredicate );
        ICompositePredicate g2 = (ICompositePredicate) g.predicateAt(1);
        assertTrue( "Second element must be a group", g2 instanceof ICompositePredicate );
        assertEquals( "First group must be connected with a NOT", BooleanOp.NOT, g1.getOp() );
        assertEquals( "Second group must be connected with a NOT", BooleanOp.NOT, g2.getOp() );
        assertSame( "First element of first group must be <TRUE>", PredicateConstants.TRUE, g1.predicateAt(0) );
        assertTrue( "First element of second group must be a ResourceSpecRef", g2.predicateAt(0) instanceof IPredicateReference );
        g2.predicateAt(0).accept( new DefaultPredicateVisitor() {
            public void visit( IPredicateReference pred ) {
                assertEquals( "The referenced spec must be named LEGAL_DOCS", "LEGAL_DOCS", ((IDSpecRef)pred).getReferencedName() );
            }
        }, IPredicateVisitor.PREORDER );

    }

    public void testConditionalResourceExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder (
            "policy second_policy\n"+
            "    DO DENY\n" +
            "    FOR\n"+
            "        NAME != \"foo.txt\" and NAME = \"bar.doc\""+
            "    OR  TYPE != \"PERL\" and TYPE == \"JAVA\""+
            "    OR  DIRECTORY != \"FOO\" and DIRECTORY = \"BAR\""+
            "    OR  SIZE = 100 AND SIZE == 100 AND SIZE < 1000 AND SIZE > 10 AND SIZE <= 100 and SIZE >= 100" +
            "    OR  MODIFIED_DATE = \"01/01/1955\" AND MODIFIED_DATE == \"05/18/1999\" AND MODIFIED_DATE < \"12/31/2005\" AND MODIFIED_DATE > \"01/02/3000\"" +
            "    OR  ACCESS_DATE = \"01/02/2005\""+
            "    OR  CREATED_DATE = \"01/02/2005\""+
            "    ON * BY *"
        );

        IDPolicy p = dob.processPolicy();
        // The policy and its attributes
        assertNotNull( "Parser returned a null policy", p );
        // Get the target...
        ITarget t = p.getTarget();
        assertNotNull( "Policy must have a non-null target", t );
        // The resources
        IPredicate fr = t.getFromResourcePred();
        assertNotNull( "The 'from' resource of the policy's target must not be null", fr );
        assertTrue( "Top-level 'from' resource must be a group", fr instanceof ICompositePredicate );
        ICompositePredicate g = (ICompositePredicate)fr;
        assertTrue( "Top-level 'from' resource must be an OR group", g.getOp() == BooleanOp.OR );
        assertEquals( "Top-level group must have 7 elements", 7, g.predicateCount() );

        // NAMEs...
        assertTrue( "Element 0 must be an AND", g.predicateAt(0) instanceof ICompositePredicate );
        ICompositePredicate names = (ICompositePredicate) g.predicateAt(0);
        assertEquals( "Name subexpression must have 2 elements", 2, names.predicateCount() );
        IPredicate name = names.predicateAt(0);
        assertTrue("First elmement must be a relation",  name instanceof IRelation );
        assertSame("Op must be !=", RelationOp.NOT_EQUALS, ((IRelation)name).getOp());
        IEvalValue rhs = ((Constant)((IRelation)name).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals("The value of RHS must be file://**/foo.txt", "file://**/foo.txt",  (String)rhs.getValue());
        SpecAttribute lhs = (SpecAttribute)((IRelation)name).getLHS();
        assertSame("LHS must be a NAME attribute", ResourceAttribute.NAME, lhs);

        // TYPEs...
        ICompositePredicate types = (ICompositePredicate)g.predicateAt( 1 );
        assertEquals( "Type subexpression must have 2 elements", 2, types.predicateCount() );
        IPredicate type = types.predicateAt(1);
        assertTrue( "First element must be a relation",  type instanceof IRelation );
        assertSame( "Op must be ==", RelationOp.EQUALS, ((IRelation)type).getOp());
        rhs = ((Constant)((IRelation) type).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals( "The value of RHS must be file:/**.java", "file:/**.java", rhs.getValue());
        lhs = (SpecAttribute)((IRelation) type).getLHS();
        assertSame("LHS must be a TYPE attribute", ResourceAttribute.TYPE, lhs);

        // DIRs...
        ICompositePredicate dirs = (ICompositePredicate)g.predicateAt( 2 );
        assertEquals( "Dir subexpression must have 2 elements", 2, dirs.predicateCount() );
        IPredicate dir = dirs.predicateAt(1);
        assertTrue( dir instanceof IRelation );
        assertSame("Op must be =", RelationOp.EQUALS, ((IRelation)dir).getOp());
        rhs = ((Constant)((IRelation)dir).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals("The value of RHS must be file:/**/bar/*", "file:/**/bar/*", rhs.getValue());
        lhs = (SpecAttribute)((IRelation)dir).getLHS();
        assertSame("LHS must be a DIRECTORY attribute", ResourceAttribute.DIRECTORY, lhs);

        // Sizes...
        ICompositePredicate szs = (ICompositePredicate)g.predicateAt( 3 );
        assertEquals( "Size subexpression must have 6 elements", 6, szs.predicateCount() );
        IPredicate sz = szs.predicateAt(0);
        assertTrue( sz instanceof IRelation);
        IRelation r = (IRelation) sz;
        assertSame("Op of the first size subexpression must be ==", RelationOp.EQUALS, r.getOp());
        rhs = ((Constant)r.getRHS()).getValue();
        assertSame("The type of RHS must be LONG", ValueType.LONG, rhs.getType());
        assertEquals("The value of RHS must be 100", 100, ((Long) rhs.getValue()).longValue());
        assertSame("LHS must be a SIZE attribute", ResourceAttribute.SIZE, r.getLHS());
        r = (IRelation) szs.predicateAt(1);
        assertSame("Op of the second size subexpression must be =", RelationOp.EQUALS, r.getOp());
        r = (IRelation) szs.predicateAt(2);
        assertSame("Op of the third size subexpression must be <", RelationOp.LESS_THAN, r.getOp());
        r = (IRelation) szs.predicateAt(3);
        assertSame("Op of the forth size subexpression must be >", RelationOp.GREATER_THAN, r.getOp());
        r = (IRelation) szs.predicateAt(4);
        assertSame("Op of the fifth size subexpression must be <=", RelationOp.LESS_THAN_EQUALS, r.getOp());
        r = (IRelation) szs.predicateAt(5);
        assertSame("Op of the sixth size subexpression must be >=", RelationOp.GREATER_THAN_EQUALS, r.getOp());

        // Modification dates...
        ICompositePredicate mds = (ICompositePredicate)g.predicateAt( 4 );
        assertEquals( "Modification Date subexpression must have 4 elements", 4, mds.predicateCount() );
        IPredicate md = mds.predicateAt(0);
        assertTrue( md instanceof IRelation );
        r = (IRelation) md;
        assertSame("First element LHS must be a MODIFIED_DATE attribute", ResourceAttribute.MODIFIED_DATE, r.getLHS());
        rhs = ((Constant)r.getRHS()).getValue();
        assertSame("The type of RHS must be DATE", ValueType.DATE, rhs.getType());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date expected = sdf.parse("01/01/1955");
        Date actual = new Date();
        actual.setTime(((Long)rhs.getValue()).longValue());
        assertEquals("The date must be 1/1/1955", expected, actual );

        md = mds.predicateAt(1);
        assertTrue(md instanceof IRelation);
        r = (IRelation) md;
        rhs = ((Constant)r.getRHS()).getValue();
        expected = sdf.parse("05/18/1999");
        actual.setTime(((Long)rhs.getValue()).longValue());
        assertEquals("The date of the second modified date predicate must be 05/18/1999", expected, actual);

        md = mds.predicateAt(2);
        assertTrue(md instanceof IRelation);
        r = (IRelation) md;
        rhs = ((Constant)r.getRHS()).getValue();
        expected = sdf.parse("12/31/2005");
        actual.setTime(((Long)rhs.getValue()).longValue());
        assertEquals("The date of the third modified date predicate must be 12/31/2005", expected, actual);

        md = mds.predicateAt(3);
        assertTrue(md instanceof IRelation);
        r = (IRelation) md;
        rhs = ((Constant)r.getRHS()).getValue();
        expected = sdf.parse("01/02/3000");
        actual.setTime(((Long)rhs.getValue()).longValue());
        assertEquals("The date of the forth modified date predicate must be 01/02/3000", expected, actual);

        // access dates...
        IPredicate ad = g.predicateAt(5);
        assertTrue( ad instanceof IRelation );
        r = (IRelation) ad;
        assertSame("LHS must be a ACCESS_DATE attribute", ResourceAttribute.ACCESS_DATE, r.getLHS());


        // created dates...
        IPredicate cd = g.predicateAt(6);
        assertTrue( cd instanceof IRelation );
        r = (IRelation) cd;
        assertSame("LHS must be a CREATED_DATE attribute", ResourceAttribute.CREATED_DATE, r.getLHS());

        // Check the 'to' resource - it must be empty
        IPredicate tr = t.getToResourcePred();
        assertNull( "The 'to' resource of the policy's target must not null", tr );
    }

    public void testAtomicActionExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "policy second_policy\n"+
            "    DO DENY\n"+
            "    FOR *\n"+
            "    ON\n"+
            "        CREATE_NEW OR OPEN OR WRITE OR DELETE OR CHANGE_ATTRIBUTES\n"+
            "    OR  CHANGE_SECURITY OR EDIT OR EDIT_COPY OR SENDTO OR CUT_PASTE\n"+
            "    OR  PASTE OR BATCH OR BURN OR PRINT OR COPY OR EMAIL OR IM\n"+
            "    OR  WEBMAIL OR RENAME OR MOVE OR SHARE"+
            "    BY *"
        );

        IDPolicy p = dob.processPolicy();
        String[] actionNames = new String[] {
            IDAction.CREATE_NEW_NAME
        ,   IDAction.OPEN_NAME
        ,   null
        ,   IDAction.DELETE_NAME
        ,   IDAction.CHANGE_PROPERTIES_NAME
        ,   IDAction.CHANGE_SECURITY_NAME
        ,   IDAction.EDIT_NAME
        ,   IDAction.EDIT_COPY_NAME
        ,   IDAction.SENDTO_NAME
        ,   IDAction.CUT_PASTE_NAME
        ,   IDAction.COPY_PASTE_NAME
        ,   IDAction.BATCH_NAME
        ,   IDAction.BURN_NAME
        ,   IDAction.PRINT_NAME
        ,   IDAction.COPY_NAME
        ,   IDAction.EMAIL_NAME
        ,   IDAction.IM_NAME
        ,   IDAction.WEBMAIL_NAME
        ,   IDAction.RENAME_NAME
        ,   IDAction.MOVE_NAME
        ,   IDAction.SHARE_NAME
        };

        // The policy and its attributes
        assertNotNull( "Parser returned a null policy", p );
        // Get the target...
        ITarget t = p.getTarget();
        assertNotNull( "Policy must have a non-null target", t );
        // The actions
        IPredicate as = t.getActionPred();

        assertNotNull( "Action must not be null", as );
        assertTrue( "Actions must be OR-ed together", as instanceof ICompositePredicate );

        ICompositePredicate g = (ICompositePredicate)as;
        IPredicate[] args = (IPredicate[]) g.predicates().toArray(new IPredicate[g.predicateCount()]);
        assertEquals( actionNames.length, args.length );
        // Parser goes right to left:
        for ( int i = 0 ; i != actionNames.length ; i++ ) {
            assertTrue( "Element "+i, args[i] instanceof DAction);
            if ( actionNames[i] != null ) {
                final String mustHave = actionNames[i];
                final String errorMsg = "Element "+i;
                args[i].accept( new DefaultPredicateVisitor() {
                    public void visit( IPredicateReference pred ) {
                        assertTrue( errorMsg, mustHave.equalsIgnoreCase(((IDSpecRef)pred).getPrintableReference()));
                    }
                }, IPredicateVisitor.PREORDER );
            }
        }
    }

    public void testAtomicSubjectExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder (
            "policy second_policy\n"+
            "    DO DENY\n"+
            "    FOR * ON * BY\n"+
            "    ALL_USERS OR ALL_HOSTS OR ALL_APPLICATIONS OR ALL_SUBJECTS"
        );

        IDPolicy p = dob.processPolicy();
        IDSubjectSpec[] subjects = new IDSubjectSpec[] {
            IDSubjectSpec.ALL_USERS
        ,   IDSubjectSpec.ALL_HOSTS
        ,   IDSubjectSpec.ALL_APPS
        ,   IDSubjectSpec.ALL_SUBJECTS
        };

        // The policy and its attributes
        assertNotNull( "Parser returned a null policy", p );
        // Get the target...
        ITarget t = p.getTarget();
        assertNotNull( "Policy must have a non-null target", t );
        // The subject
        IPredicate ss = t.getSubjectPred();

        assertNotNull( "Subject must not be null", ss );
        assertTrue( "Subjects must be OR-ed together", ss instanceof ICompositePredicate );

        ICompositePredicate g = (ICompositePredicate)ss;
        IPredicate[] args = (IPredicate[]) g.predicates().toArray(new IPredicate[g.predicateCount()]);
        assertEquals( subjects.length, args.length );

        // Parser goes right to left:
        for ( int i = 0 ; i != subjects.length ; i++ ) {
            assertTrue( "Element "+i, args[i] instanceof IDSubjectSpec );
            assertEquals( "Element "+i, args[i], subjects[i] );
        }
    }

    public void testNamedSubjectExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder (
            "principal abcdef = ALL_USERS OR ALL_HOSTS OR ALL_APPLICATIONS OR ALL_SUBJECTS"
        );

        IDSubjectSpec[] subjects = new IDSubjectSpec[] {
            IDSubjectSpec.ALL_USERS
        ,   IDSubjectSpec.ALL_HOSTS
        ,   IDSubjectSpec.ALL_APPS
        ,   IDSubjectSpec.ALL_SUBJECTS
        };

        IDSpec ss = dob.processSpec();

        assertNotNull( "Subject must not be null", ss );
        assertEquals("Spec must be called 'abcdef'", "abcdef", ss.getName());
        IPredicate pred = ss.getPredicate();
        assertNotNull("Spec must contain a non-null predicate", pred);
        assertTrue( "Subjects must be OR-ed together", pred instanceof ICompositePredicate );

        ICompositePredicate g = (ICompositePredicate)pred;
        IPredicate[] args = g.predicates().toArray(new IPredicate[g.predicateCount()]);
        assertEquals( subjects.length, args.length );

        // Parser goes right to left:
        for ( int i = 0 ; i != subjects.length ; i++ ) {
            assertTrue( "Element "+i, args[i] instanceof IDSubjectSpec );
            assertEquals( "Element "+i, ((IDSubjectSpec)args[i]), subjects[i] );
        }
    }

    public void testInternalActionExpression() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder ("id 1234 creator empty access_policy empty action onetwo = CREATE_NEW OR OPEN");

        IPQLVisitor visitor = new DefaultPQLVisitor() {
            public void visitComponent(DomainObjectDescriptor descr, IPredicate a) {
                assertNotNull( "Internal action expression must not be null", a);
                assertNotNull( "Id must not be null", descr.getId() );
                assertEquals( "Id must equal 1234", descr.getId(), new Long(1234));
                assertNotNull( "Name must not be null", descr.getName() );
                assertEquals( "Name must be onetwo", descr.getName(), "onetwo");
            }
        };
        dob.processInternalPQL(visitor);

    }

    public void testNamedicActionExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder (
            "action abcdef = CREATE_NEW OR OPEN OR WRITE OR DELETE OR CHANGE_ATTRIBUTES\n"+
            "    OR  CHANGE_SECURITY OR EDIT OR EDIT_COPY OR SENDTO OR CUT_PASTE\n"+
            "    OR  PASTE OR BATCH OR BURN OR PRINT OR COPY OR EMAIL OR IM\n"+
            "    OR  WEBMAIL OR RENAME OR MOVE OR SHARE"
        );

        String[] actionNames = new String[] {
            IDAction.CREATE_NEW_NAME
        ,   IDAction.OPEN_NAME
        ,   null
        ,   IDAction.DELETE_NAME
        ,   IDAction.CHANGE_PROPERTIES_NAME
        ,   IDAction.CHANGE_SECURITY_NAME
        ,   IDAction.EDIT_NAME
        ,   IDAction.EDIT_COPY_NAME
        ,   IDAction.SENDTO_NAME
        ,   IDAction.CUT_PASTE_NAME
        ,   IDAction.COPY_PASTE_NAME
        ,   IDAction.BATCH_NAME
        ,   IDAction.BURN_NAME
        ,   IDAction.PRINT_NAME
        ,   IDAction.COPY_NAME
        ,   IDAction.EMAIL_NAME
        ,   IDAction.IM_NAME
        ,   IDAction.WEBMAIL_NAME
        ,   IDAction.RENAME_NAME
        ,   IDAction.MOVE_NAME
        ,   IDAction.SHARE_NAME
        };

        // The actions
        IDSpec as = dob.processSpec();

        assertNotNull( "Action must not be null", as );
        assertEquals("Spec must be called 'abcdef'", "abcdef", as.getName());
        IPredicate pred = as.getPredicate();
        assertTrue( "Actions must be OR-ed together", pred instanceof ICompositePredicate );

        ICompositePredicate g = (ICompositePredicate)pred;
        IPredicate[] args = (IPredicate[]) g.predicates().toArray(new IPredicate[g.predicateCount()]);
        assertEquals( actionNames.length, args.length );
        // Parser goes right to left:
        for ( int i = 0 ; i != actionNames.length ; i++ ) {
            assertTrue( "Element "+i, args[i] instanceof DAction );
            if ( actionNames[i] != null ) {
                final String mustHave = actionNames[i];
                final String errorMsg = "Element "+i;
                args[i].accept( new DefaultPredicateVisitor() {
                    public void visit( IPredicateReference pred ) {
                        assertTrue( errorMsg, mustHave.equalsIgnoreCase(((IDSpecRef)pred).getPrintableReference()));
                    }
                }, IPredicateVisitor.PREORDER );
            }
        }
    }

    public void testNamedResourceExpressions() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "resource abcdef = NAME != \"foo.txt\" and NAME = \"bar.doc\""+
            "    OR  TYPE != \"PERL\" and TYPE == \"JAVA\""+
            "    OR  DIRECTORY != \"FOO\" and DIRECTORY = \"BAR\""+
            "    OR  SIZE = 100 AND SIZE == 100 AND SIZE < 1000 AND SIZE > 10 AND SIZE <= 100 and SIZE >= 100"
        );

        // The resources
        IDSpec fr = dob.processSpec();
        assertNotNull( "The 'from' resource of the policy's target must not be null", fr );

        assertEquals("Spec must be called 'abcdef'", "abcdef", fr.getName());
        IPredicate pred = fr.getPredicate();
        assertTrue( "Top-level 'from' resource must be a group", pred instanceof ICompositePredicate );
        ICompositePredicate g = (ICompositePredicate)pred;
        assertTrue( "Top-level 'from' resource must be an OR group", g.getOp() == BooleanOp.OR );
        assertEquals( "Top-level group must have 4 elements", 4, g.predicateCount() );

        // NAMEs...
        assertTrue( "Element 0 must be an AND", g.predicateAt(0) instanceof ICompositePredicate );
        ICompositePredicate names = (ICompositePredicate) g.predicateAt(0);
        assertEquals( "Name subexpression must have 2 elements", 2, names.predicateCount() );
        IPredicate name = (IPredicate) names.predicateAt(0);
        assertTrue("First elmement must be a relation",  name instanceof IRelation );
        assertSame("Op must be !=", RelationOp.NOT_EQUALS, ((IRelation)name).getOp());
        IEvalValue rhs = ((Constant)((IRelation)name).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals("The value of RHS must be file://**/foo.txt", "file://**/foo.txt",  (String)rhs.getValue());
        SpecAttribute lhs = (SpecAttribute)((IRelation)name).getLHS();
        assertSame("LHS must be a NAME attribute", ResourceAttribute.NAME, lhs);

        // TYPEs...
        ICompositePredicate types = (ICompositePredicate)g.predicateAt( 1 );
        assertEquals( "Type subexpression must have 2 elements", 2, types.predicateCount() );
        IPredicate type = (IPredicate) types.predicateAt(1);
        assertTrue( "First element must be a relation",  type instanceof IRelation );
        assertSame( "Op must be ==", RelationOp.EQUALS, ((IRelation)type).getOp());
        rhs = ((Constant)((IRelation) type).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals( "The value of RHS must be file:/**.java", "file:/**.java", rhs.getValue());
        lhs = (SpecAttribute)((IRelation) type).getLHS();
        assertSame("LHS must be a TYPE attribute", ResourceAttribute.TYPE, lhs);

        // DIRs...
        ICompositePredicate dirs = (ICompositePredicate)g.predicateAt( 2 );
        assertEquals( "Dir subexpression must have 2 elements", 2, dirs.predicateCount() );
        IPredicate dir = (IPredicate) dirs.predicateAt(1);
        assertTrue( dir instanceof IRelation );
        assertSame("Op must be =", RelationOp.EQUALS, ((IRelation)dir).getOp());
        rhs = ((Constant)((IRelation)dir).getRHS()).getValue();
        assertSame("The type of the RHS must be STRING", ValueType.STRING, rhs.getType());
        assertEquals("The value of RHS must be file:/**/bar/*", "file:/**/bar/*", rhs.getValue());
        lhs = (SpecAttribute)((IRelation)dir).getLHS();
        assertSame("LHS must be a DIRECTORY attribute", ResourceAttribute.DIRECTORY, lhs);

        // Sizes...
        ICompositePredicate szs = (ICompositePredicate)g.predicateAt( 3 );
        assertEquals( "Size subexpression must have 6 elements", 6, szs.predicateCount() );
        IPredicate sz = (IPredicate) szs.predicateAt(0);
        assertTrue( sz instanceof IRelation);
        IRelation r = (IRelation) sz;
        assertSame("Op of the first size subexpression must be ==", RelationOp.EQUALS, r.getOp());
        rhs = ((Constant)r.getRHS()).getValue();
        assertSame("The type of RHS must be LONG", ValueType.LONG, rhs.getType());
        assertEquals("The value of RHS must be 100", 100, ((Long) rhs.getValue()).longValue());
        assertSame("LHS must be a SIZE attribute", ResourceAttribute.SIZE, r.getLHS());
        r = (IRelation) szs.predicateAt(1);
        assertSame("Op of the second size subexpression must be =", RelationOp.EQUALS, r.getOp());
        r = (IRelation) szs.predicateAt(2);
        assertSame("Op of the third size subexpression must be <", RelationOp.LESS_THAN, r.getOp());
        r = (IRelation) szs.predicateAt(3);
        assertSame("Op of the forth size subexpression must be >", RelationOp.GREATER_THAN, r.getOp());
        r = (IRelation) szs.predicateAt(4);
        assertSame("Op of the fifth size subexpression must be <=", RelationOp.LESS_THAN_EQUALS, r.getOp());
        r = (IRelation) szs.predicateAt(5);
        assertSame("Op of the sixth size subexpression must be >=", RelationOp.GREATER_THAN_EQUALS, r.getOp());
    }

    public void  testNotifyObligation() throws Exception {

        DomainObjectBuilder dob = new DomainObjectBuilder (
                "policy notify_policy " +
                "for * on * by * " +
                "do allow " +
                "on allow do notify \"your@mama.com, your@papa.com\" \"batteries are not included\"");

        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IObligation[] obligations = policy.getObligationArray(EffectType.ALLOW);
        assertNotNull("obligations should not be null", obligations);
        assertEquals("there should be 1 obligation", 1, obligations.length);
        assertNotNull("obligation should not be null", obligations[0]);
        assertTrue("obligation should be of type NotificatinObligation", obligations[0] instanceof NotifyObligation );
    }

    public void testDateCondition() throws Exception  {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where environment.current_time.date > 28 " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"date\"", TimeAttribute.DATE, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be > ", RelationOp.GREATER_THAN, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be long", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 28", 28, ((Long)actVal).longValue());
    }

    public void testCurrentTimeCondition() throws Exception  {
        DateFormat format = DateFormat.getDateTimeInstance();
        String time = "Jan 1, 2002 1:42:37 pm";
        long timeL = format.parse(time).getTime();

        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where environment.current_time = \"" + time + "\"" +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"identity\"", TimeAttribute.IDENTITY, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.EQUALS, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be DATE", ValueType.DATE, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be " + timeL, timeL, ((Long)actVal).longValue());
    }

    public void testWeekdayStringCondition() throws Exception  {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where current_time.weekday = wednesday " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"weekday\"", TimeAttribute.WEEKDAY, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.EQUALS, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be DATE", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 4", 4, ((Long)actVal).longValue());
    }

    public void testWeekdayLongCondition() throws Exception  {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where environment.current_time.weekday = 4 " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"weekday\"", TimeAttribute.WEEKDAY, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.EQUALS, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be LONG", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 4", 4, ((Long)actVal).longValue());
    }

    public void testTimeCondition() throws Exception  {
        String time = "11:12:13 am";
        long timeL = DateFormat.getTimeInstance().parse(time).getTime() +
        DateFormat.getTimeInstance().getTimeZone().getRawOffset();
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where current_time.time < \"" + time + "\" " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"time\"", TimeAttribute.TIME, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.LESS_THAN, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be LONG", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be " + timeL, timeL, ((Long)actVal).longValue());
    }

    public void testDOWIMCondition() throws Exception  {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where current_time.day_in_month <= 3 " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"day_in_month\"", TimeAttribute.DOWIM, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.LESS_THAN_EQUALS, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be LONG", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 3", 3, ((Long)actVal).longValue());
    }

    public void testYearCondition() throws Exception  {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy where_policy " +
                "for * on * by * " +
                "where current_time.year >= 1945 " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type TimeAttribute", lhs instanceof TimeAttribute);
        TimeAttribute ta = (TimeAttribute) lhs;
        assertSame("Time attribute should be \"year\"", TimeAttribute.YEAR, ta);
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.GREATER_THAN_EQUALS, op);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        IEvalValue val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be LONG", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 1945", 1945, ((Long)actVal).longValue());
    }

    public void testInetAddress() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy ip_policy " +
                "for * on * " +
                "by (host.inet_address = \"15.63.127.255/26\", host.inet_address = \"1.3.7.15/32\", host.inet_address = \"2.4.8.16/0\") " +
                "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        ITarget target = policy.getTarget();
        assertNotNull("target should not be null", target);
        IPredicate subject = target.getSubjectPred();
        assertNotNull("subject should not be null", subject);
        assertTrue("subject should be CompositePredicate", subject instanceof CompositePredicate);
        CompositePredicate comp = (CompositePredicate) subject;
        BooleanOp op = comp.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be OR", BooleanOp.OR, op);
        assertEquals("there should be 3 predicates", 3, comp.predicateCount());
        IPredicate pred = comp.predicateAt(0);
        assertNotNull("first predicate should not be null", pred);
        assertTrue("first predicate should be a Relation", pred instanceof Relation);
        Relation rel = (Relation) pred;
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be a Constant", rhs instanceof Constant);
        Constant c = (Constant) rhs;
        assertEquals("printed value of rhs should be ", "\"15.63.127.255/26\"", c.toString());
    }

    public void testLocationDef() throws Exception {
        String locName = "xanadu";
        String locValue = "1.2.3.4/16";
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "location " + locName + " = " + '"' + locValue + '"');
        Location location = dob.processLocation();
        assertNotNull("location should not be null", location);
        String name = location.getName();
        assertNotNull("location name should not be null", name);
        assertEquals(locName, name);
        String value = location.getValue();
        assertNotNull("location value should not be null", value);
        assertEquals(locValue, value);
    }

    public void testLocationAttribute() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "principal lalaland = host.location = lalaland"
        );
        IDSpec spec = dob.processSpec();
        assertNotNull("subject spec should not be null", spec);
        assertEquals("lalaland", spec.getName());
        IPredicate pred = spec.getPredicate();
        assertTrue("subject spec should be a relation", pred instanceof Relation);
        Relation rel = (Relation) pred;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs should not be null", lhs);
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be a location ref", rhs instanceof LocationReference);
        LocationReference ref = (LocationReference) rhs;
        String refName = ref.getRefLocationName();
        assertEquals("ref name should be lalaland", "lalaland", refName);
    }

    public void testDeploymentClause() throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(
                "policy foo " +
                "for * on * by * " +
                "do allow " +
        /* 0 */ "deployed to * with agent.type = desktop, " +
        /* 1 */ "agent.id = 2347 OR " +
        /* 2 */ "(host.name = bobo with agent.id = 42), " +
        /* 3 */ "host.name = fofo with agent.type = file_server");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate dt = policy.getDeploymentTarget();
        assertNotNull("deployment target should not be null", dt);
        assertTrue("deployment target should be a composite predicate", dt instanceof CompositePredicate);
        CompositePredicate cp = (CompositePredicate) dt;
        assertEquals("deployment target should have 4 members", 4, cp.predicateCount());
        assertEquals("deployment target BooleanOp should be OR", BooleanOp.OR, cp.getOp());
        List<IPredicate> predicates = cp.predicates();

        // first predicate
        IPredicate first = (IPredicate) predicates.get(0);
        assertTrue("predicate 1 should be a composite", first instanceof CompositePredicate);
        cp = (CompositePredicate) first;
        assertEquals("predicate 1 should have 2 members", 2, cp.predicateCount());
        assertEquals("predicate 1 should be AND", BooleanOp.AND, cp.getOp());

        // second predicate
        IPredicate second = (IPredicate) predicates.get(1);
        assertTrue("predicate 2 should be a relation", second instanceof Relation);

        // third predicate
        IPredicate third = (IPredicate) predicates.get(2);
        assertTrue("predicate 3 should be a composite", third instanceof CompositePredicate);
        cp = (CompositePredicate) first;
        assertEquals("predicate 3 should have 2 members", 2, cp.predicateCount());
        assertEquals("predicate 3 should be AND", BooleanOp.AND, cp.getOp());

        // fourth predicate
        IPredicate fourth = (IPredicate) predicates.get(3);
        assertTrue("predicate 4 should be a composite", fourth instanceof CompositePredicate);
        cp = (CompositePredicate) first;
        assertEquals("predicate 4 should have 2 members", 2, cp.predicateCount());
        assertEquals("predicate 4 should be AND", BooleanOp.AND, cp.getOp());
    }

    public void testEmptyAccessPolicy() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "ACCESS_POLICY EMPTY"
        );
        IAccessPolicy ap = dob.processAccessPolicy();
        assertNull(ap);
    }

    public void testMinimalAccessPolicy() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "ACCESS_POLICY ACCESS_CONTROL ALLOWED_ENTITIES"
        );
        IAccessPolicy ap = dob.processAccessPolicy();
        assertNotNull(ap);
        Collection allowed = ap.getAllowedEntities();
        assertNotNull(allowed);
        assertTrue(allowed.isEmpty());
        Collection control = ap.getAccessControlPolicies();
        assertNotNull(control);
        assertTrue(control.isEmpty());
    }

    public void testAccessPolicyWithAllowedEntities() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "ACCESS_POLICY ACCESS_CONTROL ALLOWED_ENTITIES "
        +   "POLICY_ENTITY, USER_ENTITY, RESOURCE_ENTITY,"
        +   "HOST_ENTITY, APPLICATION_ENTITY, LOCATION_ENTITY,"
        +   "ACTION_ENTITY"
        );
        IAccessPolicy ap = dob.processAccessPolicy();
        assertNotNull(ap);
        Collection allowed = ap.getAllowedEntities();
        assertNotNull(allowed);
        assertEquals(7, allowed.size());
        Collection control = ap.getAccessControlPolicies();
        assertNotNull(control);
        assertTrue(control.isEmpty());
    }

    public void testSimpleAccessPolicy() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder(
            "ACCESS_POLICY ACCESS_CONTROL "
        +   "PBAC FOR * ON READ BY "
        +   "    PRINCIPAL.USER.GROUP = \"ADMIN\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"Policy Analyst\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"Policy Administrator\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"System Administrator\" "
        +   "DO ALLOW " 
        +   "PBAC FOR * ON WRITE BY"
        +   "    PRINCIPAL.USER.GROUP = \"ADMIN\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"Policy Analyst\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"Policy Administrator\" "
        +   "OR  PRINCIPAL.USER.GROUP = \"System Administrator\" "
        +   "DO  ALLOW "
        +   "ALLOWED_ENTITIES POLICY_ENTITY, USER_ENTITY,"
        +   "RESOURCE_ENTITY, HOST_ENTITY, APPLICATION_ENTITY,"
        +   "LOCATION_ENTITY, ACTION_ENTITY"
        );
        IAccessPolicy ap = dob.processAccessPolicy();
        assertNotNull(ap);
        Collection allowed = ap.getAllowedEntities();
        assertNotNull(allowed);
        assertEquals(7, allowed.size());
        Collection control = ap.getAccessControlPolicies();
        assertNotNull(control);
        assertEquals(2, control.size());
    }

    public void testSimpleFunction() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder("policy with_function " +
                                                          "for * on * by * " +
                                                          "where call_function (\"servicename\", \"functionname\", resource.fso.name, 3, \"hello\") <= 0 " +
                                                          "do allow");
        
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type IFunctionApplication", lhs instanceof IFunctionApplication);

        // Just some spot checks here
        IFunctionApplication func = (IFunctionApplication)lhs;
        assertEquals("servicename", func.getServiceName());
        assertEquals("functionname", func.getFunctionName());

        List<IExpression> exprs = func.getArguments();
        assertNotNull("Arguments should not be null", exprs);
        assertEquals("There should be three function arguments", exprs.size(), 3);

        IExpression expr = exprs.get(0);
        assertNotNull("First argument should not be null", expr);
        assertSame("First argument must be a name attribute", ResourceAttribute.NAME, expr);

        expr = exprs.get(1);
        assertNotNull("Second argument should not be null", expr);
        assertTrue("Second argument should be Constant", expr instanceof Constant);
        Constant c = (Constant) expr;
        IEvalValue val = c.getValue();
        assertNotNull("Second argument value should not be null", val);
        assertSame("type of second argument should be LONG", ValueType.LONG, val.getType());
        Object actVal = val.getValue();
        assertNotNull("value of second argument EvalValue should not be null", actVal);
        assertTrue("type of second argument EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of second argument EvalValue should be 3", 3, ((Long)actVal).longValue());

        
        // OP
        RelationOp op = rel.getOp();
        assertNotNull("op should not be null", op);
        assertSame("op should be = ", RelationOp.LESS_THAN_EQUALS, op);
        
        
        // RHS
        IExpression rhs = rel.getRHS();
        assertNotNull("rhs should not be null", rhs);
        assertTrue("rhs should be Constant", rhs instanceof Constant);
        c = (Constant) rhs;
        val = c.getValue();
        assertNotNull("rhs value should not be null", val);
        assertSame("type of rhs should be LONG", ValueType.LONG, val.getType());
        actVal = val.getValue();
        assertNotNull("value of rhs EvalValue should not be null", actVal);
        assertTrue("type of rhs EvalValue should be Long", actVal instanceof Long);
        assertEquals("value of rhs EvalValue should be 0", 0, ((Long)actVal).longValue());
    }


    public void testCompoundFunction() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder("policy with_nested_function " +
                                                          "for * on * by * " +
                                                          "where call_function (\"servicename\", \"functionname\", call_function (\"otherservice\", \"otherfunction\", 3), 2) <= 0 " +
                                                          "do allow");
        IDPolicy policy = dob.processPolicy();
        assertNotNull("policy should not be null", policy);
        IPredicate conditions = policy.getConditions();
        assertNotNull("policy conditions should not be null", conditions);
        assertTrue("conditions should be a relation", conditions instanceof Relation);
        Relation rel = (Relation) conditions;
        IExpression lhs = rel.getLHS();
        assertNotNull("lhs of condition should not be null", lhs);
        assertTrue("lhs should be of type IFunctionApplication", lhs instanceof IFunctionApplication);

        // Quick spot checks here
        IFunctionApplication func = (IFunctionApplication)lhs;
        assertEquals("servicename", func.getServiceName());
        assertEquals("functionname", func.getFunctionName());

        List<IExpression> exprs = func.getArguments();
        assertNotNull("Arguments should not be null", exprs);
        assertEquals("There should be two function arguments", exprs.size(), 2);

        IExpression expr = exprs.get(0);
        assertNotNull("First argument should not be null", expr);
        assertTrue("First argument should be function application", expr instanceof IFunctionApplication);

        // Okay, that's enough for now.
    }
}
