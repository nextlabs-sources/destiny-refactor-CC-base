package com.nextlabs.language.formatter.st;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/main/com/nextlabs/language/formatter/st/PolicyLanguageFormatter.java#1 $
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.antlr.stringtemplate.PathGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.language.formatter.IPolicyLanguageFormatter;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinition;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;

/**
 * This is a Policy Language Formatter based on StringTemplate component.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyLanguageFormatter implements IPolicyLanguageFormatter {

    /**
     * This error listener ignores all errors/warnings.
     */
    private static final StringTemplateErrorListener NULL_LISTENER =
        new StringTemplateErrorListener() {
            public void error(String msg, Throwable e) {
            }
            public void warning(String msg) {
            }
        };

    /**
     * This group loader calculates the position of the resource in the path,
     * and loads the group from the resulting stream.
     */
    private static final class ResourceGroupLoader extends PathGroupLoader {

        /**
         * The default constructor defines an error listener
         * that hides all errors. This is OK because the string template
         * that we are loading is essentially a part of the code.
         */
        public ResourceGroupLoader() {
            super(NULL_LISTENER);
        }

        /**
         * Overrides the locator method by prepending
         * the package name string to the name of the group.
         */
        @Override
        protected BufferedReader locate(String name)
            throws IOException {
            ClassLoader cl =
                Thread.currentThread().getContextClassLoader();
            name = getClass().getPackage().getName().replace('.', '/')
            +   '/' + name;
            InputStream is = cl.getResourceAsStream(name);
            if (is != null) {
                return new BufferedReader(getInputStreamReader(is));
            } else {
                String message = "Unable to find template: '"+name+"'";
                error(message);
                throw new IOException(message);
            }
        }

    }

    /**
     * This data-only class holds string templates
     * required for formatting elements of policy language.
     */
    private static final class Templates {

        /**
         * The template used for formatting expressions.
         */
        private final StringTemplate expressionFormatter;

        /**
         * The template used for formatting of any definition.
         */
        private final StringTemplate definitionFormatter;

        /**
         * The template for formatting context types.
         */
        private final StringTemplate contextTypeFormatter;

        /**
         * The template for formatting function types.
         */
        private final StringTemplate functionTypeFormatter;

        /**
         * The template for formatting obligation types.
         */
        private final StringTemplate obligationTypeFormatter;

        /**
         * The template for formatting policies.
         */
        private final StringTemplate policyFormatter;

        /**
         * The template for formatting policy components.
         */
        private final StringTemplate policyComponentFormatter;

        /**
         * The template for formatting policy sets.
         */
        private final StringTemplate policySetFormatter;

        /**
         * The template for formatting policy types.
         */
        private final StringTemplate policyTypeFormatter;

        /**
         * Initializes the templates from the given template group.
         *
         * @param templates the template group from which to extract templates.
         * @param keywords the keyword map to set into the template group.
         */
        public Templates(StringTemplateGroup templates) {
            expressionFormatter = templates.getInstanceOf("expression");
            definitionFormatter = templates.getInstanceOf("definition");
            contextTypeFormatter = templates.getInstanceOf("contextType");
            functionTypeFormatter = templates.getInstanceOf("functionType");
            obligationTypeFormatter =
                templates.getInstanceOf("obligationType");
            policyFormatter = templates.getInstanceOf("policy");
            policyComponentFormatter =
                templates.getInstanceOf("policyComponent");
            policySetFormatter = templates.getInstanceOf("policySet");
            policyTypeFormatter = templates.getInstanceOf("policyType");
        }

        /**
         * @return the expressionFormatter
         */
        public StringTemplate getExpressionFormatter() {
            return expressionFormatter.getInstanceOf();
        }

        /**
         * @return the definitionFormatter
         */
        public StringTemplate getDefinitionFormatter() {
            return definitionFormatter.getInstanceOf();
        }

        /**
         * @return the contextTypeFormatter
         */
        public StringTemplate getContextTypeFormatter() {
            return contextTypeFormatter.getInstanceOf();
        }

        /**
         * @return the functionTypeFormatter
         */
        public StringTemplate getFunctionTypeFormatter() {
            return functionTypeFormatter.getInstanceOf();
        }

        /**
         * @return the obligationTypeFormatter
         */
        public StringTemplate getObligationTypeFormatter() {
            return obligationTypeFormatter.getInstanceOf();
        }

        /**
         * @return the policyFormatter
         */
        public StringTemplate getPolicyFormatter() {
            return policyFormatter.getInstanceOf();
        }

        /**
         * @return the policyComponentFormatter
         */
        public StringTemplate getPolicyComponentFormatter() {
            return policyComponentFormatter.getInstanceOf();
        }

        /**
         * @return the policySetFormatter
         */
        public StringTemplate getPolicySetFormatter() {
            return policySetFormatter.getInstanceOf();
        }

        /**
         * @return the policyTypeFormatter
         */
        public StringTemplate getPolicyTypeFormatter() {
            return policyTypeFormatter.getInstanceOf();
        }

    }

    /**
     * The name of the argument to pass to templates. All templates
     * referenced from code must use the same argument name (which is "it").
     */
    private static final String ARG_NAME = "it";

    static {
        StringTemplateGroup.registerGroupLoader(
            new ResourceGroupLoader()
        );
    }

    private static final Map<String,Templates> templateCache =
        new HashMap<String, Templates>();

    private final Templates templates;

    public PolicyLanguageFormatter(
        String format
    ,   int version
    ,   Map<String,String> keywords
    ) {
        templates = getTemplates(format, version, keywords);
    }

    private static synchronized Templates getTemplates(
        String format
    ,   int version
    ,   Map<String,String> keywords) {
        String location = "v" + version + "/" + format;
        Templates res = templateCache.get(location);
        if (res == null) {
            StringTemplateGroup group = StringTemplateGroup.loadGroup(location);
            if (group == null) {
                throw new IllegalArgumentException(
                    "Unsupported format/version: " + format + ", " + version
                );
            }
            group.defineMap("keywords", keywords);
            res = new Templates(group);
            templateCache.put(location, res);
        }
        return res;
    }

    /**
     * @see IPolicyLanguageFormatter#format(Writer, String,IDefinition[])
     */
    public void format(Writer out, String separator, IDefinition<?> ... defs)
        throws IOException {
        for ( int i = 0 ; i != defs.length ; i++ ) {
            if (i != 0) {
                out.append(separator);
            }
            StringTemplate template = templates.getDefinitionFormatter();
            template.setAttribute(ARG_NAME, defs[i]);
            out.write(template.toString());
        }
    }

    /**
     * @see IPolicyLanguageFormatter
     * #format(Writer, String, Iterable<IDefinition>)
     */
    public void format(
        Writer out
    ,   String separator
    ,   Iterable<IDefinition<?>> defs
    )   throws IOException {
        if (defs == null) {
            throw new NullPointerException("defs");
        }
        Iterator<IDefinition<?>> iter = defs.iterator();
        while (iter.hasNext()) {
            StringTemplate template = templates.getDefinitionFormatter();
            template.setAttribute(ARG_NAME, iter.next());
            out.write(template.toString());
            if (iter.hasNext()) {
                out.append(separator);
            }
        }
    }

    /**
     * @see IPolicyLanguageFormatter#formatContextType(Writer,IContextType)
     */
    public void formatContextType(Writer out, IContextType contextType)
        throws IOException {
        StringTemplate template = templates.getContextTypeFormatter();
        template.setAttribute(ARG_NAME, contextType);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter#formatFunctionType(Writer,IFunctionType)
     */
    public void formatFunctionType(Writer out, IFunctionType functionType)
        throws IOException {
        StringTemplate template = templates.getFunctionTypeFormatter();
        template.setAttribute(ARG_NAME, functionType);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter
     * #formatObligationType(Writer,IObligationType)
     */
    public void formatObligationType(
        Writer out
    ,   IObligationType obligationType
    )   throws IOException {
        StringTemplate template = templates.getObligationTypeFormatter();
        template.setAttribute(ARG_NAME, obligationType);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter#formatPolicy(Writer,IPolicy)
     */
    public void formatPolicy(Writer out, IPolicy policy) throws IOException {
        StringTemplate template = templates.getPolicyFormatter();
        template.setAttribute(ARG_NAME, policy);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter
     * #formatPolicyComponent(Writer, IPolicyComponent)
     */
    public void formatPolicyComponent(
        Writer out
    ,   IPolicyComponent policyComponent
    ) throws IOException {
        StringTemplate template = templates.getPolicyComponentFormatter();
        template.setAttribute(ARG_NAME, policyComponent);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter#formatPolicySet(Writer,IPolicySet)
     */
    public void formatPolicySet(Writer out, IPolicySet policySet)
        throws IOException {
        StringTemplate template = templates.getPolicySetFormatter();
        template.setAttribute(ARG_NAME, policySet);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter#formatPolicyType(Writer,IPolicyType)
     */
    public void formatPolicyType(Writer out, IPolicyType policyType)
        throws IOException {
        StringTemplate template = templates.getPolicyTypeFormatter();
        template.setAttribute(ARG_NAME, policyType);
        out.write(template.toString());
    }

    /**
     * @see IPolicyLanguageFormatter#formatExpression(Writer, IExpression)
     */
    public void formatExpression(Writer out, IExpression expression)
        throws IOException {
        StringTemplate template = templates.getExpressionFormatter();
        template.setAttribute(ARG_NAME, expression);
        out.write(template.toString());
    }

}
