package com.nextlabs.shared.tools;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.utils.StringUtils;

/**
 * Define all the possible value type
 *
 * @author hchan
 * @date Apr 5, 2007
 */
public abstract class OptionValueType<T> {
	
	public static abstract class OptionValueTypePrimitive<T> extends OptionValueType<T>{
		public OptionValueTypePrimitive(String name) {
			super(name);
		}
		
		public boolean contains(Object value, Object inputValue){
			return value.equals(inputValue);
		}
	}
	
	
	public static final OptionValueType<String> STRING = new OptionValueTypePrimitive<String>("STRING"){
		public boolean isValid(Object object) {
			return true;
		}

		public String getValue(OptionId<String> optionId, String value) {
			return value;
		}
	};
	
	public static final OptionValueType<Long> LONG = new OptionValueTypePrimitive<Long>("LONG"){
        public boolean isValid(Object object) {
            try {
                Long.parseLong(object.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public Long getValue(OptionId<Long> optionId, String value) {
            return Long.parseLong(value);
        }
    };
	
	public static final OptionValueType<Integer> INTEGER = new OptionValueTypePrimitive<Integer>("INTEGER"){
		public boolean isValid(Object object) {
			try {
				Integer.parseInt(object.toString());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		public Integer getValue(OptionId<Integer> optionId, String value) {
			return Integer.parseInt(value);
		}
	};
	
	public static final OptionValueType<Float> FLOAT = new OptionValueTypePrimitive<Float>("FLOAT"){
		public boolean isValid(Object object) {
			try {
				Float.parseFloat(object.toString());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		public Float getValue(OptionId<Float> optionId, String value) {
			return Float.parseFloat(value);
		}
	};
	
	public static final OptionValueType<Double> DOUBLE = new OptionValueTypePrimitive<Double>("DOUBLE"){
		public boolean isValid(Object object) {
			try {
				Double.parseDouble(object.toString());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		public Double getValue(OptionId<Double> optionId, String value) {
			return Double.parseDouble(value);
		}
	};
	
	public static abstract class OptionValueNoValueLabel<T> extends OptionValueTypePrimitive<T>{
		public OptionValueNoValueLabel(String name) {
			super(name);
		}
	}
	
	//the different between BOOLEAN and ON_OFF is ON_OFF has default value but doesn't have value input
	//so you won't see any difference here
	/**
	 * require input is true or false
	 */
	public static final OptionValueType<Boolean> BOOLEAN = new OptionValueNoValueLabel<Boolean>("BOOLEAN"){
		public boolean isValid(Object object) {
			return StringUtils.stringToBoolean(object.toString()) != null;
		}

		public Boolean getValue(OptionId<Boolean> optionId, String value) {
			return StringUtils.stringToBoolean(value);
		}
	};
	
	/**
	 * the input could be true/false or nothing, 
	 * present mean true, no present means false
	 * this value can't be required.
	 * this is a hybrid type of BOOLEAN and NONE
	 * default value is required.
	 */
	public static final OptionValueType<Boolean> ON_OFF = new OptionValueNoValueLabel<Boolean>("ON_OFF"){
		public boolean isValid(Object object) {
			return StringUtils.stringToBoolean(object.toString()) != null;
		}

		public Boolean getValue(OptionId<Boolean> optionId, String value) {
			return StringUtils.stringToBoolean(value);
		}
	};
	
	/**
	 * any type is ok, the application should check it by itself
	 */
	public static final OptionValueType<Object> ANY = new OptionValueTypePrimitive<Object>("ANY"){
		public boolean isValid(Object object) {
			return true;
		}

		public Object getValue(OptionId<Object> optionId, String value) {
			return value;
		}
	};
	
	public static abstract class OptionValueTypeList<T> extends OptionValueType<T>{
		public OptionValueTypeList(String name) {
			super(name);
		}
		
		public boolean isValid(Object object) {
			throw new UnsupportedOperationException("Please call isValid(OptionId, Object)");
		}
		
		private static Map<OptionId, Set<Object>> valueMap =
				new HashMap<OptionId, Set<Object>>();

		public void addCustomValue(OptionId<T> optionId, T... objs) throws InvalidOptionDescriptorException {
			for(Object obj : objs ){
				if (this == CASE_INSENSITIVE_STRING_LIST) {
					if (!(obj instanceof String)) {
						throw new InvalidOptionDescriptorException("value '" + obj + "' is not a string.");
					}
				}
				Set<Object> values = valueMap.get(optionId);
				if (values == null) {
					values = new HashSet<Object>();
					valueMap.put(optionId, values);
				}
				values.add(obj);
			}
		}

		/**
		 * return null if not found
		 * @param option
		 * @return
		 */
		public Set<Object> getCustomList(OptionId<?> option){
			Set<Object> set = valueMap.get(option);
			return set != null ? set : new HashSet<Object>();
		}
		
		protected T getValueFromCustomList(OptionId<T> optionId, Object value){
			Set<Object> values = getCustomList(optionId);
			for (Object v : values) {
				if (v instanceof Enum) {
					Enum<?> enumV = (Enum<?>) v;
					try {
						Enum<?> enumObject = enumV.valueOf(enumV.getClass(), value.toString());
						if (enumV.equals(enumObject)) {
							return (T)enumV;
						}
					} catch (IllegalArgumentException e) {
						//do nothing, continue
					}
				} else {
					if (v.equals(value)) {
						return (T)v;
					}
				}
			}
			return null;
		}
		
		protected String getValueFromCaseInsensitiveStringList(OptionId<String> optionId, Object object){
			Set<Object> values = getCustomList(optionId);
			for (Object value : values) {
				if (((String) value).equalsIgnoreCase(object.toString())) {
					return (String)value;
				}
			}
			return null;
		}
	}
	
	/**
	 * custom list should only contain strings only
	 * otherwise it will be confusing what getValue() will return
	 */
	public static final OptionValueTypeList CUSTOM_LIST = new OptionValueTypeList("CUSTOM_LIST"){

		public boolean contains(Object value, Object inputValue) {
			return value.equals(inputValue);
		}

		public Object getValue(OptionId optionId, String value) {
			Object obj = getValueFromCustomList(optionId, value);
			if(obj == null){
				// should not be here since the value is checked
				throw new IllegalArgumentException("Can't find any matching value");
			}
			return obj;
		}

		public boolean isValid(OptionId optionId, Object object) {
			Object obj = getValueFromCustomList(optionId, object);
			return obj != null;
		}
	};
	
	/**
	 * custom predefined string list
	 * if case sensitive, use CUSTOM_LIST
	 */
	public static final OptionValueTypeList<String> CASE_INSENSITIVE_STRING_LIST = new OptionValueTypeList<String>("CASE_INSENSITIVE_STRING_LIST"){

		public boolean contains(Object value, Object inputValue) {
			if (value instanceof String && inputValue instanceof String) {
				return ((String) value).equalsIgnoreCase((String) inputValue);
			}else{
				return false;
			}
		}

		public String getValue(OptionId<String> optionId, String value) {
			String obj = getValueFromCaseInsensitiveStringList(optionId, value);
			if(obj == null){
				// should not be here since the value is checked
				throw new IllegalArgumentException("Can't find any matching value");
			}
			return obj;
		}

		public boolean isValid(OptionId<String> optionId, Object object) {
			Object obj = getValueFromCaseInsensitiveStringList(optionId, object);
			return obj != null;
		}
	};
	
	public static abstract class OptionValueTypeFile extends OptionValueType<File>{
		public OptionValueTypeFile(String name) {
			super(name);
		}
		
		public boolean contains(Object value, Object inputValue) {
			throw new UnsupportedOperationException("I don't know how to do this yet");
		}
		
		public File getValue(OptionId<File> optionId, String value) {
			return new File(value);
		}
	}
	
	/**
     * only check if the path exists
     */
    public static final OptionValueTypeFile EXIST_FILE_FOLDER = new OptionValueTypeFile("EXISITING FILE/FOLDER"){
        public boolean isValid(Object object) {
            File file = new File(object.toString());
            return file.exists(); 
        }
    };
	
	/**
	 * only check if it is a file
	 */
	public static final OptionValueTypeFile FILE = new OptionValueTypeFile("FILE"){
		public boolean isValid(Object object) {
			File file = new File(object.toString());
			return file.exists() ? file.isFile() : true; 
		}
	};
	
	/**
	 * check existence must be able to read
	 */
	public static final OptionValueTypeFile EXIST_FILE = new OptionValueTypeFile("EXISTING READABLE FILE"){
		public boolean isValid(Object object) {
			File file = new File(object.toString());
			return file.isFile() && file.exists() && file.canRead();
		}
	};
	
	/**
	 * check existence
	 */
	public static final OptionValueTypeFile NON_EXIST_FILE = new OptionValueTypeFile("NON-EXISTING FILE"){
		public boolean isValid(Object object) {
			File file = new File(object.toString());
			return !file.exists();
		}
	};
	
	/**
	 * check existence
	 */
	public static final OptionValueTypeFile NON_EXIST_FOLDER = new OptionValueTypeFile("NON-EXISTING FOLDER"){
		public boolean isValid(Object object) {
			File file = new File(object.toString());
			return !file.exists();
		}
	};
	
	/**
	 * only check if it is a folder
	 */
	public static final OptionValueTypeFile FOLDER = new OptionValueTypeFile("FOLDER"){
		public boolean isValid(Object object) {
			File folder = new File(object.toString());
			return folder.exists() ? folder.isDirectory() : true;
		}
	};
	
	/**
	 * check existence, must be able to read
	 */
	public static final OptionValueTypeFile EXIST_FOLDER = new OptionValueTypeFile("EXISTING READABLE FOLDER"){
		public boolean isValid(Object object) {
			File folder = new File(object.toString());
			return folder.isDirectory() && folder.exists() && folder.canRead();
		}
	};
	
	/**
	 * I think that is most powerful one but may be too complicated
	 * Introduce this type later.
	 */
//	public static final OptionValueTypeFile REGEX = new OptionValueTypeFile("REGEX")
	
	private final String name;
	
	public OptionValueType(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString(){
		return name;
	}
	
	/**
	 * check <code>this</code> is a list of value
	 * @param optionId
	 * @param object
	 * @return true if the <code>object</code> is acceptable by <code>this</code> and the <code>optionId</code>.
	 */
	public boolean isValid(OptionId<T> optionId, Object object){
		return this.isValid(object);
	}
	
	/**
	 * 
	 * @param object
	 * @return true if the <code>object</code> is acceptable by this and the optionId.
	 */
	protected abstract boolean isValid(Object object);
	
	/**
	 * 
	 * @param optionId
	 * @param value
	 * @return a casted value
	 */
	public abstract T getValue(OptionId<T> optionId, String value);
	
	/**
	 * 
	 * @param value
	 * @param inputValue
	 * @return
	 */
	public abstract boolean contains(Object value, Object inputValue);
	
}
