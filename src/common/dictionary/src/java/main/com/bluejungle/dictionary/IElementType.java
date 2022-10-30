package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IElementType.java#1 $
 */

import java.util.Collection;

/**
 * This interface defines contract for dictionary element types.
 * 
 * @author sergey
 */

public interface IElementType {
	class InMemoryElementType implements IElementType{
		private final String name;
		
		public InMemoryElementType(String name) {
			this.name = name;
		}

		public IElementField getField(String name) {
			throw new UnsupportedOperationException("getField");
		}

		public String[] getFieldNames() {
			throw new UnsupportedOperationException();
		}

		public Collection<IElementField> getAllFields() {
			throw new UnsupportedOperationException("getAllFields");
		}
            
		public Collection<IElementField> getFields() {
			throw new UnsupportedOperationException("getFields");
		}
		
		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InMemoryElementType other = (InMemoryElementType) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	IElementType ENUM_GROUP_TYPE = new InMemoryElementType("ENUM_GROUP");
	
	IElementType STRUCT_GROUP_TYPE = new InMemoryElementType("STRUCT_GROUP");

    /**
     * Returns the name of this element.
     * @return the name of this element.
     */
    String getName();

    /**
     * Returns a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     * @return a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     */
    Collection<IElementField> getFields();
    
    /**
     * Returns a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element, including deleted fields 
     * @return a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     */
    Collection<IElementField> getAllFields();

    /**
     * Returns an array of <code>String</code> objects representing
     * the names of all fields defined for this type sorted alphabetically.
     * @return an array of <code>String</code> objects representing
     * the names of all fields defined for this type sorted alphabetically.
     */
    String[] getFieldNames();

    /**
     * Obtains a field specified by the given name.
     * @param name the name of the field.
     * @return the field specified by the given name.
     */
    IElementField getField( String name );

}
