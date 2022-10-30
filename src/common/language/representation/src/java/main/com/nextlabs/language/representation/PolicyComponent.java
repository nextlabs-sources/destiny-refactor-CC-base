package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/PolicyComponent.java#1 $
 */

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * Instances of this class represent policy components.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyComponent extends AbstractDefinition<IPolicyComponent>
                             implements IPolicyComponent {

    /**
     * The context type for which this policy component is defined.
     */
    private final IReference<IContextType> type;

    /**
     * The predicate of this policy component.
     */
    private final IExpression predicate;

    /**
     * Constructs a policy component definition with the specified path.
     *
     * @param path the path of this policy component definition.
     */
    public PolicyComponent(
        Path path
    ,   IReference<IContextType> type
    ,   IExpression predicate
    ) {
        super(path);
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (predicate == null) {
            throw new NullPointerException("predicate");
        }
        this.type = type;
        this.predicate = predicate;
    }

    /**
     * @see IPolicyComponent#getType()
     */
    public IReference<IContextType> getType() {
        return type;
    }

    /**
     * @see IPolicyComponent#getPredicate()
     */
    public IExpression getPredicate() {
        return predicate;
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitPolicyComponent(this);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PolicyComponent)) {
            return false;
        }
        IPolicyComponent other = (IPolicyComponent)obj;
        return super.equals(obj)
            && getType().equals(other.getType())
            && getPredicate().equals(other.getPredicate());
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("component ");
        res.append(getPath());
        res.append(" : ");
        res.append(type);
        res.append(" = ");
        res.append(predicate);
        return res.toString();
    }

}
