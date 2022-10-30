package com.nextlabs.language.repository;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/repository/src/java/main/com/nextlabs/language/repository/DefinitionRespository.java#1 $
 */

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.IPolicyParserFactory;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinition;
import com.nextlabs.language.representation.IDefinitionVisitor;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;
import com.nextlabs.util.ref.Reference;

/**
 * This is an in-memory implementation of IDefinitionRepository.
 *
 * @author Sergey Kalinichenko
 */
public class DefinitionRespository extends AbstractDefinitionRepository {

    private final Map<Path,IDefinition<? extends IDefinition<?>>> byPath =
        new HashMap<Path,IDefinition<? extends IDefinition<?>>>();

    private final Map<Long,IDefinition<? extends IDefinition<?>>> byId =
        new HashMap<Long,IDefinition<? extends IDefinition<?>>>();

    private final class RepositoryReference<T>
                  extends Reference<T> {
        private final Class<T> refClass;
        RepositoryReference(long id, Class<T> refClass) {
            super(id, refClass);
            this.refClass = refClass;
        }
        RepositoryReference(Path path, Class<T> refClass) {
            super(path, refClass);
            this.refClass = refClass;
        }
        @Override
        protected T resolve() {
            T res = getDefinition(this, refClass);
            if (res == null) {
                throw new IllegalStateException(
                    "Reference cannot be resolved: "
                +   this
                );
            }
            return res;
        }
        @Override
        public boolean canResolve() {
            return hasDefinition(this, refClass);
        }
    }

    @SuppressWarnings("unchecked")
    public DefinitionRespository(
        IPolicyParserFactory ppf
    ,   int langVersion
    ,   Reader source
    )   throws IOException, PolicyLanguageException {
        this(ppf, langVersion, source, Collections.EMPTY_MAP);
    }

    public DefinitionRespository(
        IPolicyParserFactory ppf
    ,   int langVersion
    ,   Reader source
    ,   final Map<Path,Long> pathIdMap
    )   throws IOException, PolicyLanguageException {
        nullCheck(pathIdMap, "pathIdMap");
        IReferenceFactory refFactory = new IReferenceFactory() {
            public <T> IReference<T> create(Path path, Class<T> refClass) {
                return new RepositoryReference<T>(path, refClass);
            }
            public <T> IReference<T> create(long id, Class<T> refClass) {
                return new RepositoryReference<T>(id, refClass);
            }
        };
        IPolicyLanguageParser parser = ppf.getParser(langVersion, refFactory);
        parser.parseDeclarations(source, new IDefinitionVisitor() {
            public void visitContextType(IContextType contextType) {
                addDefinition(
                    contextType
                ,   pathIdMap.get(contextType.getPath())
                ,   IContextType.class
                );
            }
            public void visitFunctionType(IFunctionType functionType) {
                addDefinition(
                    functionType
                ,   pathIdMap.get(functionType.getPath())
                ,   IFunctionType.class
                );
            }
            public void visitObligationType(IObligationType obligationType) {
                addDefinition(
                    obligationType
                ,   pathIdMap.get(obligationType.getPath())
                ,   IObligationType.class
                );
            }
            public void visitPolicy(IPolicy policy) {
                addDefinition(
                    policy
                ,   pathIdMap.get(policy.getPath())
                ,   IPolicy.class
                );
            }
            public void visitPolicyComponent(IPolicyComponent component) {
                addDefinition(
                    component
                ,   pathIdMap.get(component.getPath())
                ,   IPolicyComponent.class
                );
            }
            public void visitPolicySet(IPolicySet policySet) {
                addDefinition(
                    policySet
                ,   pathIdMap.get(policySet.getPath())
                ,   IPolicySet.class
                );
            }
            public void visitPolicyType(IPolicyType policyType) {
                addDefinition(
                    policyType
                ,   pathIdMap.get(policyType.getPath())
                ,   IPolicyType.class
                );
           }
        });
    }

    @Override
    protected Object findDefinition(Path path) {
        return byPath.get(path);
    }

    @Override
    protected Object findDefinition(long id) {
        return byId.get(id);
    }

    private <T extends IDefinition<T>> void addDefinition(
        IDefinition<T> def
    ,   Long id
    ,   Class<T> defClass) {
        nullCheck(def, "definition");
        nullCheck(defClass, "defClass");
        if (!defClass.isInstance(def)) {
            throw new IllegalArgumentException(
                "The type of the definition does not match "
            +   "the expected type "
            +   defClass
            );
        }
        byPath.put(def.getPath(), def);
        if (id != null) {
            byId.put(id, def);
        }
    }

    public <T> Iterable<T> getDefinitions(final Class<T> refClass) {
        return new Iterable<T>() {
            Iterator<? extends IDefinition<?>> i = byPath.values().iterator();
            T nextVal;
            {
                moveToNext();
            }
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    public boolean hasNext() {
                        return nextVal != null;
                    }
                    public T next() {
                        if (nextVal == null) {
                            throw new IllegalStateException("next");
                        }
                        T res = nextVal;
                        moveToNext();
                        return res;
                    }
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
            @SuppressWarnings("unchecked")
            private void moveToNext() {
                nextVal = null;
                while (i.hasNext()) {
                    IDefinition<? extends IDefinition<?>> next = i.next();
                    // The check below ensures that tnextVal is
                    // assignment-compatible with the desired class at runtime,
                    // making it OK to suppress the warning for the method.
                    if (refClass.isInstance(next)) {
                        nextVal = (T)next;
                        break;
                    }
                }
            }
        };
    }

}
