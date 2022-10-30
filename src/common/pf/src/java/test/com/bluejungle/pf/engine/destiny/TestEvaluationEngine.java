/*
 * Created on Jan 25, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.TestPolicyLibrary;
import com.bluejungle.pf.domain.destiny.policy.TestUser;
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.Resource;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * @author sasha
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/etc/eclipse/destiny-code-templates.xml#3 $:
 */

public class TestEvaluationEngine extends TestCase {

    private IComponentManager manager = ComponentManagerFactory.getComponentManager();;
    private ITargetResolver targetResolver = new TestTargetResolver();
    private String pqlPolicies = TestPolicyLibrary.getPQLPolicies();
    private IDActionManager actionManager = (IDActionManager) manager.getComponent(IDActionManager.COMP_INFO);

    /**
     * Constructor for TestEvaluationEngine.
     *
     * @param arg0
     */
    public TestEvaluationEngine(String arg0) {
        super(arg0);
    }

    public final void testEvaluate() {
        /*
        // Uncomment these lines to see the list of policies
        try {
            java.lang.reflect.Field ff = engine.getClass().getDeclaredField("policies");
            ff.setAccessible(true);
            com.bluejungle.pf.domain.destiny.policy.Policy[] pp =
                (com.bluejungle.pf.domain.destiny.policy.Policy[])ff.get(engine);
            for ( int i = 0 ; i != pp.length ; i++ ) {
                com.bluejungle.pf.destiny.formatter.DomainObjectFormatter dof =
                    new com.bluejungle.pf.destiny.formatter.DomainObjectFormatter();
                dof.formatDef(pp[i]);
                System.err.println(dof.getPQL());
                dof.reset();
            }
        } catch (Exception ignore) {
        }
        */
        EvaluationEngine engine = new EvaluationEngine(targetResolver);
        EvaluationEngine emptyEngine = new EvaluationEngine(ITargetResolver.EMPTY_RESOLVER);
        long ts = System.currentTimeMillis();
        int level = 0;

        IResource resource = makeResource("file:///c:/finance/abc.txt");
        IDAction action = actionManager.getAction(IDAction.DELETE_NAME);
        IDSubject hchan = new Subject(TestUser.HCHAN.getSID(), TestUser.HCHAN.getSID(), TestUser.HCHAN.getSID(), new Long(1), SubjectType.USER);
        IDSubject amorgan = new Subject(TestUser.AMORGAN.getSID(), TestUser.AMORGAN.getSID(), TestUser.AMORGAN.getSID(), new Long(2), SubjectType.USER);
        IDSubject host = new Subject("localhost", "localhost", "localhost", new Long(3), SubjectType.HOST);
        IDSubject app = new Subject("notepad.exe", "notepad.exe", "notepad.exe", new Long(4), SubjectType.APP);
        EvaluationRequest request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   resource
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   hchan
        ,   "hchan@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        EvaluationResult result = engine.evaluate(request);

        assertEquals(EvaluationResult.DENY, result.getEffectName());

        IDSubject shmoe = new Subject("shmoe", "shmoe", "shmoe", IHasId.UNKNOWN_ID, SubjectType.USER);
        request.setUser(shmoe);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());

        resource = makeResource("file:///C:/blah/shmoe/boo.blah.foo");

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   resource
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());


        IResource javaSource = makeResource("file:///c:/projects/mainline/src/foo.java");
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   javaSource
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   hchan
        ,   "hchan@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        request.setUser(shmoe);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        IResource wherePQL = makeResource("file:///c:/where.pql");
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   wherePQL
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        IResource nowherePQL = makeResource("file:///c:/nowhere.pql");
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   nowherePQL
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());

        IResource inetPQL = makeResource("file:///c:/inet_address.pql");
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   inetPQL
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        IResource isITAR1 = makeResource("file:////c:/itar/foo.txt", "itar", "yes");
        IResource isITAR2 = makeResource("file:////c:/itar/foo.doc", "itar", "yes");

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   isITAR1
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   isITAR2
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );

        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,   isITAR1
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   amorgan
        ,   "amorgan@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());


        // Tests where we pass our own policies in addition to the ones target resolver
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.txt")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );

        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.pdf")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.gif")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());

        // Now we pass our own policies with no built-in ones at all
        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.txt")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   true
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = emptyEngine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.txt")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   true
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = emptyEngine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        request = new EvaluationRequest(
            (long)1234
        ,   action
        ,    makeResource("file:///c:/pod/boop.gif")
        ,   new EngineResourceInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   shmoe
        ,   "shmoe@bluejungle.com"
        ,   null
        ,   app
        ,   null
        ,   host
        ,   "10.17.11.130"
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   level
        ,   pqlPolicies
        ,   true
        ,   IContentAnalysisManager.DEFAULT
        ,   testSubjectResolver
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        result = emptyEngine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());


        // These are a bunch of requests designed to check if allow only/allow/deny/true allow
        // policies return the rigth result with every combination of subject and condition
        // matching and not matching (we should probably check that resource matches/doesn't
        // match for ultra-completeness, but that's tested elsewhere)
        
        // These should match the policy "allow_only"
        // Allow only. Everything matches
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow_only", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "hchan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        // Now test with the condition not matching
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow_only", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "hchan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        // User doesn't match, but condition does
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow_only", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());
        
        // Neither user nor condition match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow_only", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());


        // True allow policy. Everything matches
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_true_allow", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        // Condition doesn't match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_true_allow", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());
        
        // User doesn't match, but condition does. Note that this differs from the AO result
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_true_allow", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());
        
        // Neither user nor condition match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_true_allow", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());

        
        // Old-fashioned allow policy (has no "otherwise" effect. Everything matches
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());

        // Condition doesn't match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());
        
        // User doesn't match, but condition does.
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());
        
        // Neither user nor condition match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_allow", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());

        
        // Deny policy. Everything matches
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_deny", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DENY, result.getEffectName());

        // Condition doesn't match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_deny", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , hchan
            , "horkan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());
        
        // User doesn't match, but condition does.
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_deny", "yes", "cond_attr", "yes", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());
        
        // Neither user nor condition match
        request = new EvaluationRequest(
            (long)1234
            , actionManager.getAction("BLOOP")
            , makeResource("file:///c:/ignoreme.pdf", "for_deny", "yes", "cond_attr", "no", SpecAttribute.NOCACHE_NAME, "yes")
            , new EngineResourceInformation()
            , null
            , new EngineResourceInformation()
            , amorgan
            , "amorgan@nextlabs.com"
            , null
            , app
            , null
            , host
            , "10.17.11.130"
            , null
            , null
            , (long)0
            , false
            , ts
            , level
            , null
            , false
            , IContentAnalysisManager.DEFAULT
            , testSubjectResolver
            , IClientInformationManager.DEFAULT
            , IServiceProviderManager.DEFAULT);
        result = engine.evaluate(request);
        assertEquals(EvaluationResult.DONT_CARE, result.getEffectName());
    }
    
    private static IResource makeResource(String name, String ... attrs) {
        IMResource res = new Resource(name);
        res.setAttribute("name", Arrays.asList(new String[]{name}));
        res.setAttribute("size", 0);
        res.setAttribute("created_date", new Date());
        res.setAttribute("modified_date", new Date());

        for (int i = 0; i < attrs.length; i+=2) {
            res.setAttribute(attrs[i], attrs[i+1]);
        }

        return res;
    }

    private static class TestTargetResolver implements ITargetResolver {

        private final IDPolicy[] policies;
        private final BitSet applicables;
        private final TestPolicyLibrary library;

        public TestTargetResolver() {
            library = new TestPolicyLibrary();
            policies = library.getPolicies();
            applicables = library.getApplicables();
        }

        /**
         * @see ITargetResolver#getApplicablePolicies(EvaluationRequest)
         */
        @Override
        public BitSet getApplicablePolicies(EvaluationRequest request) {
            return (BitSet) applicables.clone();
        }

        /**
         * @see ITargetResolver#getPolicies()
         */
        @Override
        public IDPolicy[] getPolicies() {
            return policies;
        }
    }

    private static final IEngineSubjectResolver testSubjectResolver = new IEngineSubjectResolver() {

        public boolean existsSubject( String uid, ISubjectType type ) {
            return false;
        }

        public IEvalValue getGroupsForSubject(String uid, ISubjectType type) {
            return IEvalValue.EMPTY;
        }
    };

}
