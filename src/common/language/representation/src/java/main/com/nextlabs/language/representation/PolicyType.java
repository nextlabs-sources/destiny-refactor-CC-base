package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/PolicyType.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nextlabs.util.Path;
import com.nextlabs.util.Strings;
import com.nextlabs.util.ref.IReference;

/**
 * Instances of this class represent policy types.
 * Policy type is a named collection of contexts and obligation references,
 * with additional data defining which contexts and obligations are required.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyType extends AbstractDefinition<IPolicyType>
                        implements IPolicyType {

    /**
     * An optional base policy type of this policy type.
     */
    private IReference<IPolicyType> base;

    /**
     * A Set of context names for which predicates must be defined.
     */
    private final Set<String> required = new LinkedHashSet<String>();

    /**
     * A multi-map that maps section names to references
     * of context definitions.
     */
    private final Map<String,List<IReference<IContextType>>> sections =
        new LinkedHashMap<String,List<IReference<IContextType>>>();

    /**
     * A list of references to "detached" contexts.
     */
    private final List<IReference<IContextType>> detached =
        new ArrayList<IReference<IContextType>>();

    /**
     * A list of references to obligations that can be added
     * to a policy of the corresponding type.
     */
    private final List<IReference<IObligationType>> obligations =
        new ArrayList<IReference<IObligationType>>();

    /**
     * Constructs a policy type definition with the specified path.
     *
     * @param path the path of this policy type definition.
     */
    public PolicyType(Path path) {
        super(path);
    }

    /**
     * Sets a reference to the base policy type for this policy type.
     *
     * @param base a reference to the base policy type for this policy type.
     */
    public void setBase(IReference<IPolicyType> base) {
        nullCheck(base, "base");
        this.base = base;
    }

    /**
     * Removes the reference to the base type of this policy type.
     */
    public void removeBase() {
        base = null;
    }

    /**
     * @see IPolicyType#hasBase()
     */
    public boolean hasBase() {
        return base != null;
    }

    /**
     * @see IPolicyType#getBase()
     */
    public IReference<IPolicyType> getBase() {
        return base;
    }

    /**
     * @see IPolicyType#getContextSections()
     */
    public Iterable<IContextSection> getContextSections() {
        return new Iterable<IContextSection>() {
            public Iterator<IContextSection> iterator() {
                return new Iterator<IContextSection>() {
                    Iterator<Map.Entry<String,List<IReference<IContextType>>>>
                    iter = sections.entrySet().iterator();
                    /**
                     * @see Iterator#hasNext()
                     */
                    public boolean hasNext() {
                        return iter.hasNext();
                    }
                    /**
                     * @see =Iterator#next()
                     */
                    public IContextSection next() {
                        Map.Entry<String,List<IReference<IContextType>>>
                        entry = iter.next();
                        return new ContextSection(
                            entry.getKey()
                        ,   entry.getValue()
                        );
                    }
                    /**
                     * @see Iterator#remove()
                     */
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }

        };
    }

    /**
     * Adds all references from the given group of references to the specified
     * section of this policy type.
     *
     * @param sectionName the name of the section to which to add references.
     * @param references an Iterable of references to be added to the section.
     */
    public void addContexts(
        String sectionName
    ,   Iterable<IReference<IContextType>> references) {
        checkSectionName(sectionName, false);
        nullCheck(references, "references");
        List<IReference<IContextType>> context = contextForName(sectionName);
        for (IReference<IContextType> reference : references) {
            nullCheck(reference, "reference[]");
            context.add(reference);
        }
    }

    /**
     * Adds a context reference to the section identified by name
     * at the end of the list.
     *
     * @param sectionName the name of the section
     * to which to add the reference.
     * @param reference the reference to add to the section.
     */
    public void addContext(
        String sectionName
    ,   IReference<IContextType> reference
    ) {
        addContexts(sectionName, Collections.singleton(reference));
    }

    /**
     * Adds a context reference to the section identified by name
     * at the specified index.
     *
     * @param sectionName the name of the section
     * to which to add the reference.
     * @param index the index at which to add the reference.
     * @param reference the reference to add to the section.
     */
    public void addContext(
        String sectionName
    ,   int index
    ,   IReference<IContextType> reference) {
        checkSectionName(sectionName, false);
        nullCheck(reference, "reference");
        contextForName(sectionName).add(index, reference);
    }

    /**
     * Sets a context reference in the section identified by name
     * at the specified index.
     *
     * @param sectionName the name of the section
     * in which to set the reference.
     * @param index the index at which to set the reference.
     * @param reference the reference to set in the section.
     */
    public void setContext(
        String sectionName
    ,   int index
    ,   IReference<IContextType> reference) {
        checkSectionName(sectionName, true);
        nullCheck(reference, "reference");
        contextForName(sectionName).set(index, reference);
    }

    /**
     * @see IPolicyType#getContext(String, int)
     */
    public IReference<IContextType> getContext(String sectionName, int index) {
        checkSectionName(sectionName, true);
        return sections.get(sectionName).get(index);
    }

    /**
     * @see IPolicyType#getContexts(String)
     */
    public Iterable<IReference<IContextType>> getContexts(String sectionName) {
        checkSectionName(sectionName, true);
        return Collections.unmodifiableList(sections.get(sectionName));
    }

    /**
     * Removes the context at the specified index from the given section
     * of this policy type.
     *
     * @param sectionName the name of the section from which to remove
     * the reference.
     * @param index the index of the reference to remove.
     * @return the removed reference.
     */
    public IReference<IContextType> removeContext(
        String sectionName
    ,   int index
    ) {
        checkSectionName(sectionName, true);
        IReference<IContextType> res = sections.get(sectionName).remove(index);
        if (sections.get(sectionName).isEmpty()) {
            sections.remove(sectionName);
        }
        return res;
    }

    /**
     * Removes all copies of the specified context from the given section
     * of this policy type.
     *
     * @param sectionName the name of the section from which to remove
     * the reference.
     * @param reference the reference to be removed.
     * @return true if one or more reference has been removed; false otherwise.
     */
    public boolean removeContext(
        String sectionName
    ,   IReference<IContextType> reference
    ) {
        checkSectionName(sectionName, true);
        nullCheck(reference, "reference");
        boolean res = sections.get(sectionName).removeAll(
            Collections.singleton(reference)
        );
        if (sections.get(sectionName).isEmpty()) {
            sections.remove(sectionName);
        }
        return res;
    }

    /**
     * @see IPolicyType#hasContext(String, IReference)
     */
    public boolean hasContext(
        String sectionName
    ,   IReference<IContextType> reference
    ) {
        checkSectionName(sectionName, false);
        nullCheck(reference, "reference");
        return sections.containsKey(sectionName)
            && sections.get(sectionName).contains(reference);
    }

    /**
     * @see IPolicyType#getContextCount(String)
     */
    public int getContextCount(String sectionName) {
        checkSectionName(sectionName, false);
        if (sections.containsKey(sectionName)) {
            return sections.get(sectionName).size();
        } else {
            return 0;
        }
    }

    /**
     * Sets the required status of the specified section to the desired value.
     * Note that simply setting the status to required does not create
     * a section: settings for sections with no references are invisible
     * until at least one reference is added to the corresponding section.
     *
     * @param sectionName the name of the section for which to set
     * the required flag.
     * @param required the new value of the required flag.
     */
    public void setSectionRequired(String sectionName, boolean required) {
        checkSectionName(sectionName, false);
        if (required) {
            this.required.add(sectionName);
        } else {
            this.required.remove(sectionName);
        }
    }

    /**
     * @see IPolicyType#getRequiredSections()
     */
    public Iterable<String> getRequiredSections() {
        List<String> res = new ArrayList<String>();
        for (String s : required) {
            if (sections.containsKey(s) && !sections.get(s).isEmpty()) {
                res.add(s);
            }
        }
        return Collections.unmodifiableList(res);
    }

    /**
     * @see IPolicyType#isSectionRequired(String)
     */
    public boolean isSectionRequired(String sectionName) {
        checkSectionName(sectionName, false);
        return required.contains(sectionName)
            && sections.containsKey(sectionName)
            && !sections.get(sectionName).isEmpty();
    }

    /**
     * Adds contexts to the list of contexts associated with this policy type.
     *
     * @param references a group of context references to be added
     * to this policy type.
     */
    public void addContexts(Iterable<IReference<IContextType>> references) {
        nullCheck(references, "references");
        for (IReference<IContextType> reference : references) {
            nullCheck(reference, "references[]");
            detached.add(reference);
        }
    }

    /**
     * Adds a context to the list of contexts associated with this policy type
     * at the specified index.
     *
     * @param index the index at which to add the context.
     * @param reference the context reference to add to this policy type.
     */
    public void addContext(int index, IReference<IContextType> reference) {
        nullCheck(reference, "reference");
        detached.add(index, reference);
    }

    /**
     * Adds a context to the end of the list of contexts
     * associated with this policy type.
     *
     * @param reference the reference to add to this policy type.
     */
    public void addContext(IReference<IContextType> reference) {
        addContext(detached.size(), reference);
    }

    /**
     * @see IPolicyType#getContext(int)
     */
    public IReference<IContextType> getContext(int index) {
        return detached.get(index);
    }

    /**
     * @see IPolicyType#getContexts()
     */
    public Iterable<IReference<IContextType>> getContexts() {
        return Collections.unmodifiableList(detached);
    }

    /**
     * Sets the context associated with this policy at the specified index
     * to the given value.
     *
     * @param index the index at which to set the reference.
     * @param reference the reference to set to the list of the contexts
     * associated with this policy type.
     */
    public void setContext(int index, IReference<IContextType> reference) {
        nullCheck(reference, "reference");
        detached.set(index, reference);
    }

    /**
     * Removes the context at the specified index from the list of contexts
     * associated with this policy type.
     *
     * @param index the index of the context to remove.
     * @return the reference removed from the list of contexts associated with
     * this policy type.
     */
    public IReference<IContextType> removeContext(int index) {
        return detached.remove(index);
    }

    /**
     * Removes all copies of the specified reference from this policy type.
     *
     * @param reference the reference to remove from this policy type.
     * @return true if one or more references were removed; false otherwise.
     */
    public boolean removeContext(IReference<IContextType> reference) {
        nullCheck(reference, "reference");
        return detached.removeAll(Collections.singleton(reference));
    }

    /**
     * @see IPolicyType#hasContext(IReference)
     */
    public boolean hasContext(IReference<IContextType> reference) {
        nullCheck(reference, "reference");
        return detached.contains(reference);
    }

    /**
     * @see IPolicyType#getContextCount()
     */
    public int getContextCount() {
        return detached.size();
    }

    /**
     * @see IPolicyType#getSectionNames()
     */
    public Iterable<String> getSectionNames() {
        return sections.keySet();
    }

    /**
     * @see IPolicyType#hasSection(String)
     */
    public boolean hasSection(String sectionName) {
        checkSectionName(sectionName, false);
        return sections.containsKey(sectionName);
    }

    /**
     * Adds an obligation at the specified index.
     *
     * @param index the index of the obligation to add.
     * @param reference the reference of the obligation to add
     * to this policy type.
     */
    public void addObligation(
        int index
    ,   IReference<IObligationType> reference
    ) {
        nullCheck(reference, "obligation.reference");
        obligations.add(index, reference);
    }

    /**
     * Adds the specified obligation to the end of the list of obligations
     * associated with this policy type.
     *
     * @param reference a reference to the obligation to add
     * to this policy type.
     */
    public void addObligation(IReference<IObligationType> reference) {
        addObligation(obligations.size(), reference);
    }

    /**
     * Adds the specified obligations to the end of the list of obligations
     * associated with this policy type.
     *
     * @param references an Iterable of IReference objects to be added
     * to the list of obligations.
     */
    public void addObligations(
        Iterable<IReference<IObligationType>> references
    ) {
        nullCheck(references, "obligation.references");
        for (IReference<IObligationType> reference : references) {
            nullCheck(reference, "obligation.reference[]");
            obligations.add(reference);
        }
    }

    /**
     * Sets the obligation at the specified index.
     *
     * @param index the index of the obligation to set.
     * @param reference the reference to the obligation to set.
     */
    public void setObligation(
        int index
    ,   IReference<IObligationType> reference
    ) {
        nullCheck(reference, "obligation.reference");
        obligations.set(index, reference);
    }

    /**
     * @see IPolicyType#hasObligation(IReference)
     */
    public boolean hasObligation(IReference<IObligationType> reference) {
        nullCheck(reference, "obligation.reference");
        return obligations.contains(reference);
    }

    /**
     * @see IPolicyType#getObligation(int)
     */
    public IReference<IObligationType> getObligation(int index) {
        return obligations.get(index);
    }

    /**
     * Removes the obligation at the specified index.
     *
     * @param index the index of the obligation to remove.
     * @return the removed obligation.
     */
    public IReference<IObligationType> removeObligation(int index) {
        return obligations.remove(index);
    }

    /**
     * Removes the obligation from the list of obligations
     * associated with this policy type. If multiple copies of the reference
     * exist, all copies are be removed.
     *
     * @param reference a reference to the obligation to remove.
     * @return true if at least one obligation has been removed;
     * false otherwise.
     */
    public boolean removeObligation(IReference<IObligationType> reference) {
        nullCheck(reference, "reference");
        return obligations.remove(reference);
    }

    /**
     * @see IPolicyType#getObligationCount()
     */
    public int getObligationCount() {
        return obligations.size();
    }

    /**
     * @see IPolicyType#getObligations()
     */
    public Iterable<IReference<IObligationType>> getObligations() {
        return Collections.unmodifiableList(obligations);
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitPolicyType(this);
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
        if (this == obj) {
            return true;
        }
        if (obj instanceof PolicyType) {
            PolicyType other = (PolicyType)obj;
            return super.equals(obj)
                && (base==null ?
                       !other.hasBase()
                   :   base.equals(other.getBase()))
                && compareIterables(required, other.required)
                && compareIterables(
                       getContextSections()
                   ,   other.getContextSections())
                && compareIterables(
                       getContexts()
                   ,   other.getContexts())
                && compareIterables(
                       getObligations()
                   ,   other.getObligations());
        } else {
            return false;
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("policy type ");
        res.append(getPath());
        if (base != null) {
            res.append(" extends ");
            res.append(base);
        }
        for (IContextSection attachment : getContextSections()) {
            res.append("\n    ");
            res.append(attachment);
        }
        boolean firstDetached = true;
        for (IReference<IContextType> ref : detached) {
            if (firstDetached) {
                res.append("\nwith");
                firstDetached = false;
            }
            res.append("\n    ");
            res.append(ref);
        }
        if (!obligations.isEmpty()) {
            res.append("\nwith obligation ");
            boolean first = true;
            for (IReference<IObligationType> obligation : getObligations()) {
                if (!first) {
                    res.append(", ");
                } else {
                    first = false;
                }
                res.append(obligation);
            }
        }
        return res.toString();
    }

    /**
     * Given the name of the section, obtains a list for it from the sections
     * Map. If the section does not exist, this method creates a new list, and
     * adds it to the map for the corresponding section.
     *
     * @param sectionName the name of the section the list for which is needed.
     * @return a list for the corresponding section name, or a new list if
     * one does not exist.
     */
    private List<IReference<IContextType>> contextForName(String sectionName) {
        List<IReference<IContextType>> res = sections.get(sectionName);
        if (res == null) {
            res = new ArrayList<IReference<IContextType>>();
            sections.put(sectionName, res);
        }
        return res;
    }

    /**
     * Checks the validity of the section name parameter,
     * and optionally verifies that the section with the specified name
     * exists in this policy type. If the section is not valid or
     * does not exist, an exception is thrown.
     *
     * @param sectionName the name of the section to check.
     * @param mustExist a flag indicating that a section with the corresponding
     * name must exist in this policy type.
     */
    private void checkSectionName(String sectionName, boolean mustExist) {
        nullCheck(sectionName, "sectionName");
        if (Strings.isEmpty(sectionName) || !Strings.isTrimmed(sectionName)) {
            throw new IllegalArgumentException("sectionName");
        }
        if (mustExist && !sections.containsKey(sectionName)) {
            throw new IllegalArgumentException("sectionName");
        }
    }

    /**
     * Null-checks the specified object, and throws a null pointer exception
     * with the specified message if that object is null.
     *
     * @param object the object to check for null.
     * @param message the message for the null pointer exception.
     */
    private static void nullCheck(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * This is a private implementation of IContextSection interface.
     */
    private class ContextSection implements IContextSection {

        /**
         * The name of this context section.
         */
        private final String name;

        /**
         * This is the list of references associated with this section.
         */
        private final List<IReference<IContextType>> list;

        public ContextSection(
            String name
        ,   List<IReference<IContextType>> list
        ) {
            this.name = name;
            this.list = list;
        }

        /**
         * @see Iterable#iterator()
         */
        public Iterator<IReference<IContextType>> iterator() {
            return Collections.unmodifiableList(list).iterator();
        }

        /**
         * @see PolicyType.IContextSection#getSection()
         */
        public String getSection() {
            return name;
        }

        /**
         * @see PolicyType.IContextSection#isRequired()
         */
        public boolean isRequired() {
            return required.contains(name);
        }

        /**
         * @see PolicyType.IContextSection#getPolicyType()
         */
        public PolicyType getPolicyType() {
            return PolicyType.this;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof IContextSection)) {
                return false;
            }
            IContextSection other = (IContextSection)obj;
            return getSection().equals(other.getSection())
                && isRequired() == other.isRequired()
                && compareIterables(this, other);
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return getSection().hashCode();
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer res = new StringBuffer();
            if (isRequired()) {
                res.append('+');
            }
            res.append(getSection());
            res.append(' ');
            boolean first = true;
            for (IReference<IContextType> ref : this) {
                if (!first) {
                    res.append(", ");
                } else {
                    first = false;
                }
                res.append(ref);
            }
            return res.toString();
        }

    };

}