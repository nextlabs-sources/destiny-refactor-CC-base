package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/FunctionType.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.Path;

/**
 * Instances of this class represent function types.
 *
 * @author Sergey Kalinichenko
 */
public class FunctionType extends CallableType<IFunctionType>
                          implements IFunctionType {

    /**
     * The type returned by functions described by this definition.
     */
    private final IDataType returnType;

    /**
     * Constructs a function type definition with the specified path.
     *
     * @param path the path of this function type definition.
     */
    public FunctionType(Path path, IDataType returnType) {
        super(path);
        if (returnType == null) {
            throw new NullPointerException("returnType");
        }
        this.returnType = returnType;
    }

    /**
     * @see IFunctionType#getReturnType()
     */
    public IDataType getReturnType() {
        return returnType;
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitFunctionType(this);
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
        if (!(obj instanceof FunctionType)) {
            return false;
        }
        FunctionType other = (FunctionType)obj;
        return super.equals(obj)
            && getReturnType().equals(other.getReturnType())
            && compareIterables(this, other);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res =  new StringBuffer("function ");
        res.append(getPath());
        toStringArgList(res);
        res.append(" returns ");
        res.append(returnType);
        return res.toString();
    }

}
