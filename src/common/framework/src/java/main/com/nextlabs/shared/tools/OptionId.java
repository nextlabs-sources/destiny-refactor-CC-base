/*
 * Created on May 1, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import java.util.Formattable;
import java.util.Formatter;

import static java.util.FormattableFlags.*;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/OptionId.java#1 $
 */

public class OptionId<T> implements Comparable<OptionId<?>>, Formattable {
	private final String name;
	private final OptionValueType<T> valueType; 

	/**
	 * will be assigned once the <code>Option</code> is created.
	 */
	private IOptionDescriptor<T> option = null;
	
	public static <T> OptionId<T> create(String name, OptionValueType<T> valueType) {
		return new OptionId<T>(name, valueType);
	}
	
	/**
	 * 
	 * @param name The unique of this <code>OptionId</code>.
	 * @param valueType The type of this <code>OptionId</code>
	 */
	public OptionId(String name, OptionValueType<T> valueType) {
		this.name = name;
		this.valueType = valueType;
	}

	public String getName() {
		return name;
	}

	public OptionValueType<T> getValueType() {
		return valueType;
	}

	public IOptionDescriptor<T> getOption() {
		return option;
	}

	public void setOption(IOptionDescriptor<T> option) throws IllegalArgumentException {
//		if (this.option != null) {
//			throw new IllegalArgumentException("option is already set.");
//		}
		this.option = option;
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
		OptionId other = (OptionId) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int compareTo(OptionId<?> o) {
		return this.name.compareTo(o.name);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public void formatTo(Formatter formatter, int flags, int width, int precision) {
        if ((flags & ALTERNATE) == ALTERNATE) {
        	formatter.format(valueType.toString() + ": " + name);
        }else{
        	formatter.format(name);
        }
	}
}