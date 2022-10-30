package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/DataType.java#1 $
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nextlabs.util.ref.IReference;


/**
 * Instances of this define built-in and user-defined data types.
 *
 * @author Sergey Kalinichenko
 */
public abstract class DataType implements IDataType {

    /**
     * Instances of this class represent multivalued data types.
     */
    private static class MultivalueDataType
        extends DataType implements IMultivalueDataType {

        /** The inner data type of this multivalue type. */
        private final IDataType innerType;

        /**
         * The cached value of the hash code for this multivalue
         */
        private final Integer hashCode;

        /**
         * Creates the multivalue type with the corresponding values
         *
         * @param codes an Iterable with the values of the codes
         * for this code type.
         */
        public MultivalueDataType(IDataType innerType) {
            if( innerType == null ) {
                throw new NullPointerException("innerType");
            }

            this.innerType = innerType;
            hashCode = innerType.hashCode();
        }

        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitMultivalue(this);
        }

        /**
         * @see DataType#asMultivalue()
         */
        @Override
        public IMultivalueDataType asMultivalue() {
            return this;
        }

        /**
         * @see DataType#isMultivalue()
         */
        @Override
        public boolean isMultivalue() {
            return true;
        }

        /**
         * @see IMultivalueDataType#getInnerType()
         */
        public IDataType getInnerType() {
            return innerType;
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return hashCode;
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "multivalued " + innerType.toString();
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IMultivalueDataType)) {
                return false;
            }

            IMultivalueDataType objAsMultivalue = (IMultivalueDataType)obj;

            return objAsMultivalue.getInnerType().equals(innerType);
        }
    }

    /**
     * This class represents code data types.
     */
    private static class CodeDataType
        extends DataType implements ICodeDataType {

        /**
         * The list of codes associated with this code data type.
         */
        private final List<String> codes = new ArrayList<String>();

        /**
         * The cached hash code of this data type. Initially set to null,
         * this field is set to the hash code when hashCode is called
         * for the first time. After that, the value of this field
         * does not change.
         */
        private Integer hashCode;

        /**
         * Creates the code data type with the corresponding values
         * of codes.
         *
         * @param codes an Iterable with the values of the codes
         * for this code type.
         */
        public CodeDataType(Iterable<String> codes) {
            if( codes == null ) {
                throw new NullPointerException("codes");
            }
            for (String code : codes) {
                if (code == null) {
                    throw new NullPointerException("codes[i]");
                }
                this.codes.add(code);
            }
        }

        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitCode(this);
        }

        /**
         * @see DataType#asCode()
         */
        @Override
        public ICodeDataType asCode() {
            return this;
        }

        /**
         * @see DataType#isCode()
         */
        @Override
        public boolean isCode() {
            return true;
        }

        /**
         * @see Iterable#iterator()
         */
        public Iterator<String> iterator() {
            return Collections.unmodifiableCollection(codes).iterator();
        }

        /**
         * @see ICodeDataType#contains(String)
         */
        public boolean contains(String code) {
            return codes.contains(code);
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer res = new StringBuffer("code(");
            boolean first = true;
            for (String s : codes) {
                if (!first) {
                    res.append(", ");
                } else {
                    first = false;
                }
                res.append(s);
            }
            res.append(')');
            return res.toString();
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ICodeDataType)) {
                return false;
            }
            ICodeDataType cd = (ICodeDataType)obj;
            Iterator<String> a = codes.iterator();
            Iterator<String> b = cd.iterator();
            while (a.hasNext() && b.hasNext()) {
                if (!cd.contains(a.next())) {
                    return false;
                } else {
                    b.next();
                }
            }
            return a.hasNext() == b.hasNext();
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            if (hashCode == null) {
                int h = 0;
                for(String s : codes) {
                    h ^= s.hashCode();
                }
                hashCode = h;
            }
            return hashCode;
        }

    }

    /**
     * This class represents reference data types.
     */
    private static class ReferenceDataType<T>
        extends DataType implements IReferenceDataType {

        /**
         * The context type referenced by this data type.
         */
        private final IReference<T> refContext;

        /**
         * Create a reference data type for the specific context.
         *
         * @param refContext the referenced context for which to create
         * a reference data type.
         */
        public ReferenceDataType(IReference<T> refContext) {
            if (refContext == null) {
                throw new NullPointerException("refContext");
            }
            this.refContext = refContext;
        }

        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitReference(this);
        }

        /**
         * Obtains the referenced context.
         *
         * @return the referenced context.
         */
        @SuppressWarnings("unchecked")
        public <C> IReference<C> getReferencedContext() {
            return (IReference<C>)refContext;
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return refContext.toString();
        }

        /**
         * @see DataType#asReference(Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        public IReferenceDataType asReference() {
            return this;
        }

        /**
         * @see DataType#isReference()
         */
        @Override
        public boolean isReference() {
            return true;
        }

        /**
         * @see Object#equals(Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IReferenceDataType) {
                IReferenceDataType ref = (IReferenceDataType)obj;
                return refContext.equals(ref.getReferencedContext());
            } else {
                return false;
            }
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return refContext.hashCode();
        }

    }

    /**
     * The constructor is protected to restrict instantiations
     * only to subclasses of DataType.
     */
    protected DataType() {
    }

    /**
     * @see IDataType#accept(IDataTypeVisitor)
     */
    public void accept(IDataTypeVisitor visitor) {
        visitor.visitUnknown(this);
    }

    /**
     * @see IDataType#asCode()
     */
    public ICodeDataType asCode() {
        throw new UnsupportedOperationException("asCode");
    }

    /**
     * @see IDataType#asMultivalue()
     */
    public IMultivalueDataType asMultivalue() {
        throw new UnsupportedOperationException("asMultivalue");
    }

    /**
     * @see IDataType#asReference(Class)
     */
    public IReferenceDataType asReference() {
        throw new UnsupportedOperationException("asReference");
    }

    /**
     * @see IDataType#isBoolean()
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * @see IDataType#isCode()
     */
    public boolean isCode() {
        return false;
    }

    /**
     * @see IDataType#isCsString()
     */
    public boolean isCsString() {
        return false;
    }

    /**
     * @see IDataType#isDate()
     */
    public boolean isDate() {
        return false;
    }

    /**
     * @see IDataType#isMultivalue()
     */
    public boolean isMultivalue() {
        return false;
    }

    /**
     * @see IDataType#isNull()
     */
    public boolean isNull() {
        return false;
    }

    /**
     * @see IDataType#isInteger()
     */
    public boolean isInteger() {
        return false;
    }

    /**
     * @see IDataType#isDouble()
     */
    public boolean isDouble() {
        return false;
    }

    /**
     * @see IDataType#isReference()
     */
    public boolean isReference() {
        return false;
    }

    /**
     * @see IDataType#isString()
     */
    public boolean isString() {
        return false;
    }

    /**
     * Obtains a reference data type for the specific reference context.
     *
     * @param refContext the context for which to create a reference data type.
     * @return a reference data type for the specific reference context.
     */
    public static <T> IReferenceDataType makeReference(
        IReference<T> refContext
    ) {
        return new ReferenceDataType<T>(refContext);
    }

    /**
     * Makes a code data type for the specified group of codes.
     *
     * @param codes the codes from which to make a code data type.
     * @return a code data type for the specified group of codes.
     */
    public static ICodeDataType makeCode(Iterable<String> codes) {
        return new CodeDataType(codes);
    }

    /**
     * Makes a multivalue data type from the specified collection
     *
     * @param type instance of type for which we make a multivalue
     * @return a multivalue data type of this type
     */
    public static IMultivalueDataType makeMultivalue(IDataType type) {
        return new MultivalueDataType(type);
    }

}
