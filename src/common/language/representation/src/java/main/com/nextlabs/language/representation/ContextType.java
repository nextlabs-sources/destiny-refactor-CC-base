package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/ContextType.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import java.util.LinkedHashMap;
import java.util.Map;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.Path;
import com.nextlabs.util.Strings;
import com.nextlabs.util.ref.IReference;

/**
 * Instances of this class represent context types.
 *
 * @author Sergey Kalinichenko
 */
public class ContextType extends AbstractDefinition<IContextType>
                         implements IContextType {

    /**
     * This is a read-only implementation of the IAttribute interface.
     */
    private static class Attribute implements IAttribute {

        /**
         * The name of the attribute or a prefix of the template attribute.
         */
        private final String name;

        /**
         * The data type of the attribute.
         */
        private final IDataType type;

        /**
         * The flag that determines if the attribute is unique or not.
         */
        private final boolean unique;

        /**
         * The flag that determines if the attribute is template or not.
         */
        private final boolean template;

        /**
         * Creates the attribute with the specified set of parameters.
         *
         * @param name the name of the attribute or the prefix of the template
         * attribute.
         * @param type the data type of the attribute.
         * @param unique the flag that defines that the attribute is unique.
         * @param template the flag that determines that the attribute is
         * a template.
         */
        public Attribute(
            String name
        ,   IDataType type
        ,   boolean unique
        ,   boolean template) {
            if(name == null) {
                throw new NullPointerException("name");
            }
            if (type == null) {
                throw new NullPointerException("type");
            }
            this.name = name;
            this.type = type;
            this.unique = unique;
            this.template = template;
        }

        /**
         * @see IAttribute#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see IAttribute#getType()
         */
        public IDataType getType() {
            return type;
        }

        /**
         * @see IAttribute#isUnique()
         */
        public boolean isUnique() {
            return unique;
        }

        /**
         * @see IAttribute#isTemplate()
         */
        public boolean isTemplate() {
            return template;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Attribute) {
                Attribute other = (Attribute)obj;
                return name.equals(other.getName())
                    && type.equals(other.getType())
                    && unique == other.isUnique()
                    && template == other.isTemplate();
            } else {
                return false;
            }
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return name.hashCode()
                 ^ type.hashCode()
                 ^ (unique ? 0xc33cc33c : 0x3cc33cc3)
                 ^ (template ? 0xa5a5a5a5 : 0x5a5a5a5a);
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer res = new StringBuffer(name);
            if (template) {
                res.append('*');
            }
            if (unique) {
                res.append('@');
            }
            res.append(" : ");
            res.append(type);
            return res.toString();
        }
    }

    /**
     * A reference to the base context type, or null if none is specified.
     */
    private IReference<IContextType> base;

    /**
     * The List of actual attributes.
     */
    private final  Map<String,IAttribute> attributes =
        new LinkedHashMap<String,IAttribute>();

    /**
     * The List of template attributes.
     */
    private final Map<String,IAttribute> attributeTemplates =
        new LinkedHashMap<String,IAttribute>();

    /**
     * Constructs a Context Type definition with the specified path.
     *
     * @param path the path of this Component Type definition.
     */
    public ContextType(Path path) {
        super(path);
    }

    /**
     * @see IContextType#hasBase()
     */
    public boolean hasBase() {
        return base != null;
    }

    /**
     * @see IContextType#getBase()
     */
    public IReference<IContextType> getBase() {
        return base;
    }

    /**
     * Sets the new base context type for this context type.
     *
     * @param baseContextType the new base context type, or null.
     */
    public void setBase(IReference<IContextType> baseContextType) {
        this.base = baseContextType;
    }

    /**
     * Adds an actual attribute to this context definition.
     *
     * @param name the name of the attribute to add.
     * @param type the data type of the attribute to add.
     * @param unique the flag indicating that the attribute is unique.
     * @return the newly added attribute.
     */
    public IAttribute addAttribute(
        String name
    ,   IDataType type
    ,   boolean unique
    ) {
        return addAttribute(getAttributeCount(), name, type, unique);
    }

    /**
     * @see IContextType#getAttributeCount()
     */
    public int getAttributeCount() {
        return attributes.size();
    }

    /**
     * @see IContextType#getAttributeTemplateCount()
     */
    public int getAttributeTemplateCount() {
        return attributeTemplates.size();
    }

    /**
     * Adds an actual attribute to this context definition.
     *
     * @param index the index at which the attribute is to be added.
     * @param name the name of the attribute to add.
     * @param type the data type of the attribute to add.
     * @param unique the flag indicating that the attribute is unique.
     * @return the newly added attribute.
     */
    public IAttribute addAttribute(
        int index
    ,   String name
    ,   IDataType type
    ,   boolean unique
    ) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (Strings.isEmpty(name) || !Strings.isTrimmed(name)) {
            throw new IllegalArgumentException("name");
        }
        IAttribute a = new Attribute(name, type, unique, false);
        attributes.put(name, a);
        return a;
    }

    /**
     * @see IContextType#getAttributes()
     */
    public Iterable<IAttribute> getAttributes() {
        return attributes.values();
    }

    /**
     * Removes the specified attribute from this context.
     *
     * @param attribute the attribute to be removed.
     */
    public void removeAttribute(IAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        if (attribute.isTemplate()) {
            throw new IllegalArgumentException("attribute");
        }
        if (!attributes.containsKey(attribute.getName())
         || !attribute.equals(attributes.get(attribute.getName()))) {
            throw new IllegalArgumentException(
                "attribute context mismatch"
            );
        }
        attributes.remove(attribute.getName());
    }

    /**
     * Removes the attribute with the specified name from this context.
     *
     * @param name the name of the attribute to be removed.
     * @return the removed attribute or null if the named attribute
     * does not exist.
     */
    public IAttribute removeAttribute(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return attributes.remove(name);
    }

    /**
     * Removes the specified attribute template from this context.
     *
     * @param attribute the attribute template to be removed.
     */
    public void removeAttributeTemplate(IAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        if (!attribute.isTemplate()) {
            throw new IllegalArgumentException("attribute");
        }
        if (!attributeTemplates.containsKey(attribute.getName())
         || !attribute.equals(attributeTemplates.get(attribute.getName()))) {
            throw new IllegalArgumentException(
                "attribute template context mismatch"
            );
        }
        attributeTemplates.remove(attribute.getName());
    }

    /**
     * Removes the attribute template with the specified name from
     * this context.
     *
     * @param name the name of the attribute template to be removed.
     * @return the removed attribute template or null if the named attribute
     * does not exist.
     */
    public IAttribute removeAttributeTemplate(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return attributeTemplates.remove(name);
    }

    /**
     * Adds a template attribute to this context definition.
     *
     * @param name the prefix of this template attribute.
     * @param type the data type of the attribute to add.
     * @param unique the flag indicating that the attribute is unique.
     * @return the added attribute template.
     */
    public IAttribute addAttributeTemplate(
            String name
        ,   IDataType type
        ,   boolean unique
    ) {
        if (!Strings.isTrimmed(name)) {
            throw new IllegalArgumentException("name");
        }
        IAttribute res;
        attributeTemplates.put(
            name
        ,   res = new Attribute(name, type, unique, true)
        );
        return res;
    }

    /**
     * @see IContextType#getAttributeTemplates()
     */
    public Iterable<IAttribute> getAttributeTemplates() {
        return attributeTemplates.values();
    }

    /**
     * @see IContextType#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String name) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        if (attributes.containsKey(name)) {
            return true;
        }
        // TODO Suffix trees could make this faster and more predictable
        for (String attrName : attributeTemplates.keySet()) {
            if (name.startsWith(attrName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see IContextType#getAttribute(java.lang.String)
     */
    public IAttribute getAttribute(String name) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        IAttribute res = attributes.get(name);
        if (res != null) {
            return res;
        }
        for (Map.Entry<String,IAttribute> e : attributeTemplates.entrySet()) {
            if (name.startsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitContextType(this);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("context ");
        res.append(getPath());
        if (base != null) {
            res.append(" : ");
            res.append(base.toString());
        }
        res.append(" {");
        for (IAttribute attr : getAttributes()) {
            res.append("\n    ");
            res.append(attr);
        }
        for (IAttribute attr : getAttributeTemplates()) {
            res.append("\n    ");
            res.append(attr);
        }
        res.append("\n}");
        return res.toString();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextType)) {
            return false;
        }
        IContextType rhs = (IContextType)other;
        return super.equals(other)
            && (base == rhs.getBase()
            || (base != null && base.equals(rhs.getBase())))
            && compareIterables(getAttributes(), rhs.getAttributes())
            && compareIterables(
                   getAttributeTemplates()
               ,   rhs.getAttributeTemplates()
               );
    }

}
