package com.nextlabs.language.processor;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/evaluation/src/java/main/com/nextlabs/language/processor/DefinitionProcessor.java#1 $
 */

import java.util.HashMap;
import java.util.Map;

import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.language.repository.IDefinitionRepository;
import com.nextlabs.language.representation.ContextType;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * Definition processor presents an interface for fully resolved definitions
 * prepared by processing a repository of definitions.
 *
 * A fully resolved definition has these properties:
 * - Its predicate references are fully resolved,
 * - Its expression references are either resolved or validated
 *   against a type definition,
 * - It has no base definition; instead, the content of its base has been
 *   rolled into the definition itself,
 * - Its code types are merged,
 * - Its type definition references are fully resolved,
 * - Constants in its wildcard expressions have been compiled.
 * - Each policy belongs to one or more policy sets. Policies not included in
 *   any of the user-defined policy sets are added to a surrogate
 *   policy set, which is returned among user-defined policy sets.
 *
 * The interface provides access to the elements of the collection
 * of definitions suitable for evaluating policies and policy sets.
 *
 * @author Sergey Kalinichenko
 */
public class DefinitionProcessor {

    private final Map<Path,IContextType> contextTypes =
        new HashMap<Path,IContextType>();


    public DefinitionProcessor(IDefinitionRepository defs)
        throws DefinitionProcessingException {
        DefinitionProcessingException dpe =
            new DefinitionProcessingException();

        // Flatten context types
        for (IContextType ct : defs.getDefinitions(IContextType.class)) {
            contextTypes.put(ct.getPath(), flatten(ct, dpe));
        }
        // Flatten policy types
        for (IPolicyType pt : defs.getDefinitions(IPolicyType.class)) {
            
        }
        // Flatten policy components
        for (IPolicyComponent pc : defs.getDefinitions(IPolicyComponent.class)) {
            
        }
        // Flatten policies
        for (IPolicy p : defs.getDefinitions(IPolicy.class)) {
            
        }
        if (dpe.getErrorCount() != 0) {
            throw dpe;
        }
    }

    public Iterable<IPolicySet> getPolicySets() {
        return null;
    }

    private IContextType flatten(
        IContextType ct
    ,   DefinitionProcessingException dpe
    ) {
        Path path = ct.getPath();
        IContextType res = contextTypes.get(path);
        if (res == null) {
            if (ct.hasBase()) {
                if (ct.getBase().canResolve()) {
                    res = merge(flatten(ct.getBase().get(), dpe), ct, dpe);
                } else {
                    // Reference to base cannot be resolved - report an error,
                    // and ignore the base reference.
                    dpe.addError(
                        DefinitionErrorCode.UNRESOLVED_REFERENCE
                    ,   ct.getBase()
                    ,   ct
                    );
                    res = merge(ct, ct, dpe);
                }
            } else {
                res = ct;
            }
            contextTypes.put(path, res);
        }
        return res;
    }

    private IContextType merge(
        IContextType base
    ,   IContextType derived
    ,   DefinitionProcessingException dpe
    ) {
        ContextType res = new ContextType(derived.getPath());
        
        return res;
    }

    private IDataType merge(IDataType base, IDataType derived) {
        IDataType res;
        if (!base.equals(derived)) {
            if (base.isReference() && derived.isReference()) {
                IReference<IContextType> baseCtx =
                    base.asReference().getReferencedContext();
                res = null; // TODO
            } else if (base.isCode() && derived.isCode()) {
                res = null; // TODO
            } else {
                res = null;
            }
        } else {
            res = base;
        }
        return res;
    }

}
