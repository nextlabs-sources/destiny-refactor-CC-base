package com.nextlabs.language.parser.antlr.v1;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/main/com/nextlabs/language/parser/antlr/v1/AntlrPolicyLanguageParser.java#1 $
 */

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.language.parser.antlr.v1.PolicyLanguageParser.program_return;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinition;
import com.nextlabs.language.representation.IDefinitionVisitor;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * This is an ANTLR-based implementation of IPolicyLanguageParser.
 *
 * @author Sergey Kalinichenko
 */
public class AntlrPolicyLanguageParser implements IPolicyLanguageParser {

    /**
     * This interface defines an internal contract for parsing a single element
     * of the policy language, such as a declaration, a predicate,
     * or an expression. Implementations exist for each subtype
     * of {@link IDefinition}.
     */
    private interface IElementParser<T> {

        /**
         * The first stage parses the source into an AST.
         * @return the AST corresponding to the parsed element.
         */
        Tree parseText() throws RecognitionException;

        /**
         * The second stage parses the tree into a desired element.
         * @return the result of parsing the AST into the output element.
         */
        T parseTree() throws RecognitionException;

        /**
         * Obtains the name of element being parsed for error reporting.
         *
         * @return the name of element being parsed.
         */
        String getElementName();

    }

    /**
     * This is the element parser for policy components.
     */
    private final IElementParser<IPolicyComponent> COMPONENT_PARSER =
        new IElementParser<IPolicyComponent>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.oneComponentDeclaration().getTree();
        }
        public IPolicyComponent parseTree() throws RecognitionException {
            return treeParser.componentDeclaration();
        }
        public String getElementName() {
            return "a policy component";
        }
    };

    /**
     * This is the element parser for context types.
     */
    private final IElementParser<IContextType> CONTEXT_TYPE_PARSER =
        new IElementParser<IContextType>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.oneContextTypeDeclaration().getTree();
        }
        public IContextType parseTree() throws RecognitionException {
            return treeParser.contextTypeDeclaration();
        }
        public String getElementName() {
            return "a context type";
        }
    };

    /**
     * This is the element parser for function types.
     */
    private final IElementParser<IFunctionType> FUNCTION_TYPE_PARSER =
        new IElementParser<IFunctionType>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.oneFunctionTypeDeclaration().getTree();
        }
        public IFunctionType parseTree() throws RecognitionException {
            return treeParser.functionTypeDeclaration();
        }
        public String getElementName() {
            return "a function type";
        }
    };

    /**
     * This is the element parser for obligation types.
     */
    private final IElementParser<IObligationType> OBLIGATION_TYPE_PARSER =
        new IElementParser<IObligationType>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.oneObligationTypeDeclaration().getTree();
        }
        public IObligationType parseTree() throws RecognitionException {
            return treeParser.obligationTypeDeclaration();
        }
        public String getElementName() {
            return "an obligation type";
        }
    };

    /**
     * This is the element parser for policies.
     */
    private final IElementParser<IPolicy> POLICY_PARSER =
        new IElementParser<IPolicy>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.onePolicyDeclaration().getTree();
        }
        public IPolicy parseTree() throws RecognitionException {
            return treeParser.policyDeclaration();
        }
        public String getElementName() {
            return "a policy";
        }
    };

    /**
     * This is the element parser for policy sets.
     */
    private final IElementParser<IPolicySet> POLICY_SET_PARSER =
        new IElementParser<IPolicySet>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.onePolicySetDeclaration().getTree();
        }
        public IPolicySet parseTree() throws RecognitionException {
            return treeParser.policySetDeclaration();
        }
        public String getElementName() {
            return "a policy";
        }
    };

    /**
     * This is the element parser for policy types.
     */
    private final IElementParser<IPolicyType> POLICY_TYPE_PARSER =
        new IElementParser<IPolicyType>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.onePolicyTypeDeclaration().getTree();
        }
        public IPolicyType parseTree() throws RecognitionException {
            return treeParser.policyTypeDeclaration();
        }
        public String getElementName() {
            return "a policy type";
        }
    };

    /**
     * This is the element parser for expressions.
     */
    private final IElementParser<IExpression> EXPRESSION_PARSER =
        new IElementParser<IExpression>() {
        public Tree parseText() throws RecognitionException {
            return (Tree)parser.oneExpression().getTree();
        }
        public IExpression parseTree() throws RecognitionException {
            return treeParser.expression();
        }
        public String getElementName() {
            return "an expression";
        }
    };

    /**
     * This is the lexer for tokenizing the input.
     */
    private final PolicyLanguageLexer lexer = new PolicyLanguageLexer();

    /**
     * This is the parser used to convert the source to an AST.
     */
    private final PolicyLanguageParser parser = new PolicyLanguageParser(
        new CommonTokenStream(lexer)
    );

    /**
     * This is the tree parser used for the final stage of parsing.
     */
    private final PolicyBuilder treeParser = new PolicyBuilder(null);

    /**
     * @see IPolicyLanguageParser
     *     #parseDeclarations(Reader, IPolicyDeclarationCallback)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public void parseDeclarations(
        Reader source
    ,   final IDefinitionVisitor visitor)
        throws IOException, PolicyLanguageException {
        setInput(source);
        try {
            program_return prog = parser.program();
            checkError();
            // TODO Check the return
            for (Tree t : prog.res) {
                // TODO this should log the tree
                StringBuffer out = new StringBuffer();
                formatTree(out, t);
                //System.err.println(out);
                treeParser.reset();
                treeParser.setTreeNodeStream(new CommonTreeNodeStream(t));
                treeParser.program(visitor);
            }
        } catch (Exception exception) {
            throw new PolicyLanguageException(exception);
        }
    }

    /**
     * @see IPolicyLanguageParser#parseComponentDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IPolicyComponent parseComponentDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(COMPONENT_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parseContextTypeDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IContextType parseContextTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return (IContextType)parse(CONTEXT_TYPE_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parseFunctionTypeDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IFunctionType parseFunctionTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(FUNCTION_TYPE_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parseObligationTypeDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IObligationType parseObligationTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(OBLIGATION_TYPE_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parsePolicyDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IPolicy parsePolicyDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(POLICY_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parsePolicySetDeclaration(Reader)
     */
    public IPolicySet parsePolicySetDeclaration(Reader source)
            throws IOException, PolicyLanguageException {
        return parse(POLICY_SET_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parsePolicyTypeDeclaration(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IPolicyType parsePolicyTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(POLICY_TYPE_PARSER, source);
    }

    /**
     * @see IPolicyLanguageParser#parseExpression(Reader)
     * @param source the reader from which to take the input.
     * @throws IOException when it is not possible to parse the input.
     */
    public IExpression parseExpression(Reader source)
        throws IOException, PolicyLanguageException {
        return parse(EXPRESSION_PARSER, source);
    }

    /**
     * This method returns the keywords defined in the PolicyLanguageParser;
     * @return the keywords defined in the PolicyLanguageParser;
     */
    public Set<String> getKeywords() {
        return PolicyLanguageParser.getKeywords();
    }

    /**
     * Returns the version of the language supported by this parser.
     *
     * @return the version of the language supported by this parser.
     */
    public int getVersion() {
        return 1;
    }

    /**
     * Lets the parser factory set the reference factory for this parser.
     *
     * @param refFactory the reference factory to be used with this parser.
     */
    void setReferenceFactory(IReferenceFactory refFactory) {
        treeParser.setReferenceFactory(refFactory);
    }

    /**
     * Parse a single element, and return the result of the correct type.
     *
     * @param <T> the type of the parsed result.
     * @param elementParser An implementation of IElementParser for <T>.
     * @param source the source Reader from which to take the input.
     * @return the result of parsing the source.
     * @throws IOException when an error happens while reading the source.
     * @throws PolicyLanguageException when the input is not valid.
     */
    private <T> T parse(IElementParser<T> elementParser, Reader source)
        throws IOException, PolicyLanguageException {
        setInput(source);
        try {
            Object tree = elementParser.parseText();
            if (tree == null) {
                throw new PolicyLanguageException("Syntax error.");
            }
            if (!(tree instanceof Tree)) {
                throw new PolicyLanguageException("Internal error.");
            }
            checkError();
            Tree t = (Tree)tree;
            // TODO This should go to a logger, not to an output
            StringBuffer out = new StringBuffer();
            formatTree(out, t);
            //System.err.println(out);
            treeParser.reset();
            treeParser.setTreeNodeStream(
                new CommonTreeNodeStream(t)
            );
            return elementParser.parseTree();
        } catch (PolicyLanguageException pe) {
            throw pe;
        } catch (Exception exception) {
            throw new PolicyLanguageException(
                "Error while parsing "+elementParser.getElementName()
            ,   exception
            );
        }
    }

    /**
     * Resets the parser, and sets the input to the Reader passed in.
     *
     * @param source the new source to be parsed.
     * @throws IOException if one is thrown by the source Reader.
     */
    private void setInput(Reader source) throws IOException {
        if (source == null) {
            throw new NullPointerException("source");
        }
        lexer.clearLastError();
        lexer.reset();
        lexer.setCharStream(new ANTLRReaderStream(source) {
            @Override
            public int LA(int i) {
                if (i==0) {
                    return 0;
                } else if (i<0) {
                    i++;
                }
                if (p+i > n) {
                    return CharStream.EOF;
                } else {
                    return Character.toLowerCase(data[p+i-1]);
                }
            }
        });
        parser.clearLastError();
        parser.reset();
        parser.setTokenStream(new CommonTokenStream(lexer));
    }

    /**
     * Checks the parser and the lexer for the last error,
     * and throws an exception if an error has been reported.
     *
     * @throws RecognitionException
     */
    private void checkError() throws RecognitionException {
        if (lexer.getLastError() != null) {
            throw lexer.getLastError();
        }
        if (parser.getLastError() != null) {
            throw parser.getLastError();
        }
    }

    /**
     * This method recursively formats the tree for output to a log.
     *
     * @param out the buffer into which to format the tree.
     * @param t the tree to be formatted.
     */
    private void formatTree(StringBuffer out, Tree t) {
        if (t==null) {
            return;
        }
        if (t.getChildCount() != 0) {
            out.append('(');
            out.append(t);
            out.append(' ');
            for (int i = 0 ; i != t.getChildCount() ; i++) {
                if (i != 0) {
                    out.append(' ');
                }
                formatTree(out, t.getChild(i));
            }
            out.append(')');
        } else {
            out.append(t.getText());
        }
    }

}
