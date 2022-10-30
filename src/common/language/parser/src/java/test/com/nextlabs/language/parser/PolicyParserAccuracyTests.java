package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/PolicyParserAccuracyTests.java#1 $
 */


import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinitionVisitor;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;

/**
 * File-based tests for the policy language parser.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={PolicyParserAccuracyTests.class})

public class PolicyParserAccuracyTests {

    private static final String[] languages = new String[] {
        "policy-type", "policy"
    };

    private final IPolicyLanguageParser parser;
    private final Reader source;

    //private final File approved;

    @Parameters
    public static Collection<Object[]> readTests() {
        // FIXME
        String baseDirectoryPath = "C:/PersonalBranch/"
        +   "main/test_files/com/nextlabs/language/parser/syntax";
        File baseDirectory = new File(baseDirectoryPath);
        List<Object[]> res = new ArrayList<Object[]>();
        for (String language : languages) {
            res.addAll(readSingleTest(new File(baseDirectory, language)));
        }
        return res;
    }

    public PolicyParserAccuracyTests(File input, File approved)
    throws IOException, PolicyLanguageException {
        IPolicyParserFactory ppf = new PolicyParserFactory();
        parser = ppf.getParser(1);
        source = new FileReader(input);
        //this.approved = approved;
    }

    @Test
    public void parseAndCompare()
    throws RecognitionException, IOException, PolicyLanguageException {
        parser.parseDeclarations(source, new IDefinitionVisitor() {
            public void visitContextType(IContextType contextType) {
                assertNotNull(contextType);
            }
            public void visitFunctionType(IFunctionType functionType) {
                assertNotNull(functionType);
            }
            public void visitObligationType(IObligationType obligationType) {
                assertNotNull(obligationType);
            }
            public void visitPolicy(IPolicy policy) {
                assertNotNull(policy);
            }
            public void visitPolicyComponent(
                IPolicyComponent policyComponent
            ) {
                assertNotNull(policyComponent);
            }
            public void visitPolicySet(IPolicySet policySet) {
                assertNotNull(policySet);
            }
            public void visitPolicyType(IPolicyType policyType) {
                assertNotNull(policyType);
            }
        });
    }

    private static Collection<Object[]> readSingleTest(File baseDirectory) {
        List<Object[]> res = new ArrayList<Object[]>();
        if (baseDirectory.isDirectory()) {
            File input = new File(baseDirectory, "input");
            File approved = new File(baseDirectory, "approved");
            if (input.isDirectory() && approved.isDirectory()) {
                Map<String,File> inByName = mapByName(input.listFiles());
                Map<String,File> appByName = mapByName(approved.listFiles());
                for (Map.Entry<String,File> in : inByName.entrySet()) {
                    if (appByName.containsKey(in.getKey())) {
                        res.add(new Object[] {
                            in.getValue(), appByName.get(in.getKey())
                        });
                    }
                }
            }
        }
        return res;
    }

    private static Map<String,File> mapByName(File[] files) {
        Map<String,File> res =
            new TreeMap<String,File>(String.CASE_INSENSITIVE_ORDER);
        for (File f : files) {
            // Remove the extension
            res.put(f.getName().replaceFirst("^(.+)[.].*$", "$1"), f);
        }
        return res;
    }

    /*
    private static String readFile(File f) {
        StringBuffer res = null;
        try {
            BufferedReader reader = new BufferedReader(
                new FileReader(f)
            );
            while(true) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                res.append(s);
                res.append("\n");
            }
        } catch (IOException ignored) {
            // Ignore exceptions = return null
        }
        System.err.println(res);
        return res.toString();
    }
    */
}
