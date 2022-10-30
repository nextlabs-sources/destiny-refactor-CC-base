package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/Target.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import java.util.LinkedHashMap;
import java.util.Map;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Strings;

/**
 * This is an implementation of the ITargeted interface.
 * Policy and PolicySet inherit this class for implementation.
 *
 * @author Sergey Kalinichenko
 */
public class Target implements ITarget {

    /**
     * Instances of this class represent contexts of this policy.
     */
    private class Context implements IContext {

        /**
         * The name of the section for this context.
         */
        private final String section;

        /**
         * The condition of this context attachment.
         */
        private final IExpression condition;

        /**
         * Creates a ContextAttachment with the specified section
         * and the set predicate.
         *
         * @param section the name of the section attachment.
         * @param condition the set predicate for this context attachment.
         */
        public Context(
            String section
        ,   IExpression condition) {
            checkSection(section);
            if (condition == null) {
                throw new NullPointerException("condition");
            }
            this.section = section;
            this.condition = condition;
        }

        /**
         * Retrieves the section for which this context is defined.
         *
         * @return the section for which this context is defined.
         */
        public String getSection() {
            return section;
        }

        /**
         * Retrieves the condition of this context attachment.
         *
         * @return the condition of this context attachment.
         */
        public IExpression getCondition() {
            return condition;
        }

        /**
         * Retrieves the target for which this context is defined.
         *
         * @return the target for which this context is defined.
         */
        public ITarget getTarget() {
            return Target.this;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Context)) {
                return false;
            }
            Context other = (Context)obj;
            // Note that targets are not compared - two identical contexts
            // of different targets are considered equal.
            return section.equals(other.getSection())
                && condition.equals(other.getCondition());
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return section.hashCode() ^ condition.hashCode();
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return section + " " + condition;
        }

    }

    /**
     * A Map of sections to their corresponding contexts.
     */
    private final Map<String,IContext> contexts =
        new LinkedHashMap<String,IContext>();

    private boolean hashCodeSet = false;

    private int hashCodeCache;

    /**
     * Adds a context for the specified section to the policy.
     *
     * @param section the section the context for which is being added.
     * @param predicate the set predicate for the section.
     * @return the context preciously defined for this section, or null
     * if no context was associated with the given section.
     */
    public IContext addContext(String section, IExpression predicate) {
        return contexts.put(section, new Context(section, predicate));
    }

    /**
     * Removes the context defined for the given section.
     *
     * @param section the section for which to remove the context.
     * @return the context that has been removed, or null if none were defined.
     */
    public IContext removeContext(String section) {
        checkSection(section);
        return contexts.remove(section);
    }

    /**
     * @see ITarget#getContexts()
     */
    public Iterable<IContext> getContexts() {
        return contexts.values();
    }

    /**
     * @see ITarget#getContext(java.lang.String)
     */
    public IContext getContext(String section) {
        checkSection(section);
        return contexts.get(section);
    }

    /**
     * @see ITarget#hasContext(java.lang.String)
     */
    public boolean hasContext(String section) {
        checkSection(section);
        return contexts.containsKey(section);
    }

    /**
     * @see ITarget#getSections()
     */
    public Iterable<String> getSections() {
        return contexts.keySet();
    }

    /**
     * @see ITarget#isEmpty()
     */
    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Target)) {
            return false;
        }
        Target other = (Target)obj;
        return compareIterables(contexts.values(), other.contexts.values());
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (!hashCodeSet) {
            hashCodeCache = 0;
            for (IContext context : contexts.values()) {
                hashCodeCache ^= context.hashCode();
            }
            hashCodeSet = true;
        }
        return hashCodeCache;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        for (IContext ctx : getContexts()) {
            res.append("\n    ");
            res.append(ctx);
        }
        return res.toString();
    }

    /**
     * Checks the section name for validity. A section name must be non-null,
     * trimmed, and non-empty.
     *
     * @param section the name of the section to check.
     */
    private static void checkSection( String section) {
        if (section == null) {
            throw new NullPointerException("section");
        }
        if (Strings.isEmpty(section) || !Strings.isTrimmed(section)) {
            throw new IllegalArgumentException("section");
        }
    }

}
