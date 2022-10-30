/**
 * Created on April 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.novell.ldap.rfc2251.RfcFilter; 
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;

/**
 * A evaluator of RFC 2251
 * @see http://www.ietf.org/rfc/rfc2251.txt
 * 
 * @author atian 
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/util/ElementTypeEvaluator.java#1 $
 */

public class RfcFilterEvaluator {
    private static final Log LOG = LogFactory.getLog(RfcFilterEvaluator.class.getName());

    public static final RfcFilterEvaluator ALWAYS_TRUE;
    public static final RfcFilterEvaluator ALWAYS_FALSE;
    static{
    	RfcFilterEvaluator evaluator;
    	
    	try {
    		evaluator = new ConstantRfcFilterEvaluator(true);
		} catch (EnrollmentValidationException e) {
			LOG.warn(e);
			evaluator = null;
		}
		ALWAYS_TRUE = evaluator;
    	
    	try {
    		evaluator = new ConstantRfcFilterEvaluator(false);
		} catch (EnrollmentValidationException e) {
			LOG.warn(e);
			evaluator = null;
		}
		ALWAYS_FALSE = evaluator;
    }
    
    public static boolean isValid(String expression){
        try {
            new RfcFilter(expression);
        } catch (LDAPException e) {
            return false;
        }
        return true;
    }
    
    private static class ConstantRfcFilterEvaluator extends RfcFilterEvaluator {
		private final boolean b;

		public ConstantRfcFilterEvaluator(boolean b) throws EnrollmentValidationException {
			super("");
			this.b = b;
		}

		@Override
		public boolean evaluate(LDAPEntry entry) {
			return b;
		}
	}

    /** The expression from configration entry to identify element type
     *  Expression should be in a LDAP Search filter format 
     *  The detail of the expression format can be found at RFC2251 
     *     An example of expression for User elment type is:
     *     "(&(objectClass=User)(!(objectClass=Computer)))"
     */
    private final String expression;

    /** 
     * We utilize the Novell LDAP Filter expression to parse identifier expression
     *
     */
    private final RfcFilter identifier; 


    /**
     * We utilize the Novell LDAP Filter expression to parse identifier expression
     *
     * http://developer.novell.com/documentation//jldap/jldapenu/api/com/novell/ldap/rfc2251/RfcFilter.html
     *
     * Retrieves an Iterator object representing the parsed filter for this search request.
     *
     *  The first object returned from the Iterator is an Integer indicating the type of filter 
     *  component. One or more values follow the component type as subsequent items in the Iterator. 
     *  The pattern of Integer component type followed by values continues until the end of the filter.
     *
     *  Values returned as a byte array may represent UTF-8 characters or may be binary values. 
     *  The possible Integer components of a search filter and the associated values that follow are:
     *
     * AND - followed by an Iterator value
     * OR - followed by an Iterator value
     * NOT - followed by an Iterator value
     * EQUALITY_MATCH - followed by the attribute name represented as a String, and by the 
     *                    attribute value represented as a byte array
     * GREATER_OR_EQUAL - followed by the attribute name represented as a String, and by the 
     *                    attribute value represented as a byte array
     * LESS_OR_EQUAL - followed by the attribute name represented as a String, and by the attribute 
     *                    value represented as a byte array
     * APPROX_MATCH - followed by the attribute name represented as a String, and by the attribute 
     *                     value represented as a byte array
     * PRESENT - followed by a attribute name respresented as a String
     *
     * EXTENSIBLE_MATCH - followed by the name of the matching rule represented as a String, 
     *             by the attribute name represented as a String, and by the attribute value represented 
     *             as a byte array.
     * SUBSTRINGS - followed by the attribute name represented as a String, by one or more SUBSTRING 
     *               components (INITIAL, ANY, or FINAL) followed by the SUBSTRING value. 
     *
     * The first object returned by an iterator is an integer indicating the type of filter components. 
     * Subseqence values are returned. If a component is of type 'AND' or 'OR' or 'NOT' then the value 
     * returned is another iterator. This iterator is used by toString.
     * 
     * @param  expression a String representing RFC filter expression 
     *
     */
    public RfcFilterEvaluator(String expression) throws EnrollmentValidationException {
        try {
            this.identifier = new RfcFilter(expression);
        } catch (LDAPException e) {
            throw new EnrollmentValidationException("Invalid RFC filter: " + expression + e);
        }

        this.expression = expression;
    }

    /**
    * To make the evaluation process more efficient, walk through the expression tree
    * and collect all attributes names
    * 
    * @param it an Iterator which represent RFC filter parser tree
    *
    * @throws EnrollmentValidationException when parser tree is not valid or not supported
    *
    */
    private void getAttributes(Iterator<?> it, Set<String> output) throws EnrollmentValidationException {
        Object obj;
        while ( it.hasNext() ) {
            obj =  it.next();
            if ( obj instanceof Integer ) {
                int filterType = ((Integer) obj).intValue();
                switch (filterType) {
                case RfcFilter.EQUALITY_MATCH:
                    // get the attribute name 
                    obj = it.next();
                    // add attribute name in Hash Map
                    String eqAttrName = ((String) obj).toLowerCase();
                    LOG.trace("EQ match attr = " + eqAttrName);
                    output.add(eqAttrName);
                    obj = it.next(); // skip the value  
                    break;
                case RfcFilter.PRESENT:
                    // get the attribute name 
                    obj = it.next();
                    // add attribute name in Hash Map
                    String prAttrName = ((String) obj).toLowerCase();
                    LOG.trace("PRESENT match attr = " + prAttrName);
                    output.add(prAttrName);
                    break;
                case RfcFilter.AND:
                case RfcFilter.OR:
                case RfcFilter.NOT:
                    //ignore
                    break;
                default:
                    throw new EnrollmentValidationException("RFC2251 expression is not supported");
                }
            } else if (obj instanceof Iterator) {
                getAttributes((Iterator) obj, output); // walk done one more level 
            } else {
                throw new EnrollmentValidationException("Invalid RFC2251 expression"
                        + String.valueOf(obj));
            }
        }
    }
    
    public Set<String> getAttributes() throws EnrollmentValidationException{
        Set<String> names = new HashSet<String>();
        getAttributes(identifier.getFilterIterator(), names);
        return names;
    }
    
    
    /**
     * evaluate() take a LDAP Entry object to find whether it matches to the 
     * identifier, which is generated by a Rfc2251 expression.  
     *
     * @param entry a LDAP Entry
     * @return boolean true if matches, false if not
     */
    public boolean evaluate(LDAPEntry entry) throws EnrollmentSyncException {
    	
        Iterator it = this.identifier.getFilterIterator();
        
        LOG.trace(" Evaluating entry with expression: " + expression);

        return filterWalk(it, entry);
    }

    /** 
     * Use the filter identifier to evaluate the input LDAPEntry
     * walk through the attribute values and match against the parser tree  
     *
     * @param it an Iterator representing RFC parser tree
     * @return boolean value true the attribute value map matches the parser tree 
     */
    private boolean filterWalk(Iterator it, LDAPEntry entry) throws EnrollmentSyncException {

        boolean result = false;

        Object obj = it.next();

        if ( obj instanceof Integer ) {

            int filterType = ((Integer)obj).intValue(); 

            switch( filterType ) {

                case RfcFilter.AND:
                    result = true;
                    do {
                        obj = it.next();
                        if ( obj instanceof Iterator ) {
                            result = result && filterWalk( (Iterator) obj, entry ); 
                        } else {
						LOG.warn("Failed to evaluate AND filter");
						throw new EnrollmentSyncException("Invalid AND Filter encountered", entry.getDN());
					}
                    } while( it.hasNext() );
                    break;

                case RfcFilter.OR:
                    do {
                        obj = it.next();
                        if ( obj instanceof Iterator ) {
                            result = result || filterWalk( (Iterator) obj, entry ); 
                        } else {
                            LOG.warn("Failed to evaluate OR filter");
                            throw new EnrollmentSyncException("Invalid OR Filter encountered", entry.getDN());
                        }
                    } while( it.hasNext() );
                    break;

                case RfcFilter.NOT:
                    obj = it.next();
                    if ( obj instanceof Iterator ) {
                        result = ! filterWalk( (Iterator) obj, entry ); 
                    } else {
                        LOG.warn("Failed to evaluate NOT filter");
                        throw new EnrollmentSyncException("Invalid NOT Filter encountered", entry.getDN());
                    }
                    break;

                case RfcFilter.EQUALITY_MATCH:
                    Object attrName = it.next(); 
                    LOG.trace(" attrName " + attrName ); 
                    LDAPAttribute attr = entry.getAttribute( (String)attrName );
                    if ( attr != null ) { 
                    	String attrValue = new String( (byte[]) it.next() ); 
                    	Enumeration valuesEnum = attr.getStringValues();
                        while( valuesEnum.hasMoreElements() ) {
                            Object value = ((Enumeration)valuesEnum).nextElement();
                            if ( attrValue.equalsIgnoreCase( (String) value ) ) {
                                LOG.trace(" matched: " + expression );
                                return true; 
                            }
                        }
                    }
                    LOG.trace(" not matched: " + expression);
                    return false;

                case RfcFilter.PRESENT:
                    attrName = it.next();
                    LOG.trace(" attrName " + attrName ); 
                    attr = entry.getAttribute( (String)attrName );
                    return attr != null;
                    
                default:
                    LOG.warn("Filter type is not supported: " + filterType);
                    throw new EnrollmentSyncException("Invalid filter type", entry.getDN());
            }
        }
        else if ( obj instanceof Iterator ) {
            result = filterWalk( (Iterator) obj, entry ); 
        }

        return result; 
    }

}

