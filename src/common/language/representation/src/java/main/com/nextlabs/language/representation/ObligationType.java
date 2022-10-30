package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/ObligationType.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import com.nextlabs.util.Path;

/**
 * Instances of this class represent obligation types.
 *
 * @author Sergey Kalinichenko
 */
public class ObligationType extends CallableType<IObligationType>
       implements IObligationType {

    /**
     * Constructs a obligation type definition with the specified path.
     *
     * @param path the path of this obligation type definition.
     */
    public ObligationType(Path path) {
        super(path);
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitObligationType(this);
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
        if (!(obj instanceof ObligationType)) {
            return false;
        }
        ObligationType other = (ObligationType)obj;
        return super.equals(obj)
            && compareIterables(this, other);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res =  new StringBuffer("obligation ");
        res.append(getPath());
        toStringArgList(res);
        return res.toString();
    }

}
