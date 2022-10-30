/*
 * Created on Feb 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.ElementType;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.dictionary.Order;
import com.bluejungle.dictionary.Page;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IPredicate;

/**
 * don't touch a database at all
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/bluejungle/dictionary/DictionaryGhost.java#1 $
 */

public class DictionaryGhost implements IDictionary{
	public static final ComponentInfo<DictionaryGhost> COMP_INFO = new ComponentInfo<DictionaryGhost>(
			Dictionary.class.getName(),
			DictionaryGhost.class,
			LifestyleType.SINGLETON_TYPE);

	public IPredicate condition(IEnrollment arg0) {
		return null;
	}

	private Map<String, IMElementType> imelementTypeMap = new HashMap<String, IMElementType>();
	public IConfigurationSession createSession() throws DictionaryException {
		return new IConfigurationSession(){
			public void beginTransaction() throws DictionaryException {
				//do nothing
			}

			public void close() throws DictionaryException {
				//do nothing
			}

			public void commit() throws DictionaryException {
				//do nothing
			}

			public void deleteEnrollment(IEnrollment ienrollment) throws DictionaryException {
				throw new UnsupportedOperationException();
			}

			public void deleteType(IMElementType imelementtype) throws DictionaryException {
				throw new UnsupportedOperationException();
			}

			public boolean hasActiveTransaction() {
				return false;
			}

			public void rollback() throws DictionaryException {
				//do nothing
			}

			public void saveEnrollment(IEnrollment ienrollment) throws DictionaryException {
				throw new UnsupportedOperationException();
			}

			public void saveType(IMElementType imelementtype) throws DictionaryException {
				imelementTypeMap.put(imelementtype.getName(), imelementtype);
			}

			public Map<String, Integer> purgeHistory(IEnrollment enrollment, Date clearBeforeDate,
                    AtomicBoolean isInterrupted) throws DictionaryException, IllegalStateException,
                    IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            public Map<String, Integer> rollbackLatestFailedEnrollment(IEnrollment enrollment)
                    throws DictionaryException {
                throw new UnsupportedOperationException();
            }
		};
	}
	
	public IMElementType getType(String arg0) throws DictionaryException {
		return imelementTypeMap.get(arg0);
	}
	
	public IDictionaryIterator<IMGroup> getEnumeratedGroups(IPredicate arg0, IElementType arg1, Date arg2,
			Page arg3) throws DictionaryException {
	    return new IDictionaryIterator<IMGroup>() {
            public IMGroup next() throws DictionaryException {
                throw new UnsupportedOperationException();
            }
            
            public boolean hasNext() throws DictionaryException {
                return false;
            }
            
            public void close() throws DictionaryException {
                //do nothing
            }
        };
	}

	public IDictionaryIterator<IMElement> query(IPredicate condition,
			Date asOf, Order[] order, int limit) throws DictionaryException {
		return new IDictionaryIterator<IMElement>() {
            public IMElement next() throws DictionaryException {
                throw new UnsupportedOperationException();
            }
            
            public boolean hasNext() throws DictionaryException {
                return false;
            }
            
            public void close() throws DictionaryException {
                //do nothing
            }
        };
	}
	
	public IDictionaryIterator<IMElement> query(IPredicate arg0, Date arg1, Order[] arg2, Page arg3)
			throws DictionaryException {
		return new IDictionaryIterator<IMElement>() {
            public IMElement next() throws DictionaryException {
                throw new UnsupportedOperationException();
            }
            
            public boolean hasNext() throws DictionaryException {
                return false;
            }
            
            public void close() throws DictionaryException {
                //do nothing
            }
        };
	}
	
	public IMElementType makeNewType(String name) throws DictionaryException {
		return new ElementType( name );
	}
	

	
	
	/** the following methods throws UnsupportedOperationException because I am not using them **/
	
	public IPredicate changedCondition(Date arg0, Date arg1) {
		throw new UnsupportedOperationException();
	}

	public void close() throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IPredicate condition(IElementType arg0) {
		throw new UnsupportedOperationException();
	}
	
	public IPredicate condition(DictionaryPath arg0, boolean arg1) {
		throw new UnsupportedOperationException();
	}

	public IAttribute displayNameAttribute() {
		throw new UnsupportedOperationException();
	}

	public Collection<IElementType> getAllTypes() throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMElementBase getByKey(Long arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMElementBase getByUniqueName(String arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public Date getEarliestConsistentTimeSince(Date arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMElement getElement(Long arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMElement getElement(String arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<IMElementBase> getElementsById(Collection<Long> arg0, Date arg1)
			throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IEnrollment getEnrollment(String arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public Collection<IEnrollment> getEnrollments() throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<IMGroup> getEnumeratedGroups(String arg0, IElementType arg1, Date arg2, Page arg3)
			throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMGroup getGroup(DictionaryPath arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMGroup getGroup(Long arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IMGroup getGroup(String arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public Date getLatestConsistentTime() throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<IMGroup> getStructuralGroups(IPredicate arg0, IElementType arg1, Date arg2,
			Page arg3) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<IMGroup> getStructuralGroups(DictionaryPath arg0, String arg1, IElementType arg2,
			Date arg3, Page arg4) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public List<IUpdateRecord> getUpdateRecords(Date arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IAttribute internalKeyAttribute() {
		throw new UnsupportedOperationException();
	}

	public IEnrollment makeNewEnrollment(String arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<ElementFieldData> queryFields(IElementField[] arg0, IPredicate arg1, Date arg2,
			Order[] arg3, Page arg4) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IDictionaryIterator<Long> queryKeys(IPredicate arg0, Date arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	public IAttribute uniqueNameAttribute() {
		throw new UnsupportedOperationException();
	}

	public boolean isThereFailedEnrollment(String s) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

    public IDictionaryIterator<DictionaryPath> queryReferenceMembersByGroupId(long groupId)
            throws DictionaryException {
        throw new UnsupportedOperationException();
    }
}
