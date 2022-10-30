package com.nextlabs.pf.destiny.formatter;

/*
 * Created on Feb 28, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/RelationTypeIdentifier.java#1 $:
 */

import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultExpressionVisitor;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.pf.domain.destiny.environment.HeartbeatAttribute;
import com.bluejungle.pf.domain.destiny.environment.RemoteAccessAttribute;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

import java.util.HashMap;
import java.util.Map;

class RelationTypeIdentifier {

    enum XACMLType {
        UNKNOWN,
        TIME,
        DATETIME,
        STRING,
        INTEGER
    }

    public interface IRelationTypeInfo {
        String getDataType();
        String getMatchFunction();
        // This is a little clunky.  XACML doesn't have "not equals" functions.  You use the regular
        // functions and then wrap them with a "not".  We indicate this via this method.
        boolean applyNot();
    }

    public static final String NOT_FUNCTION_ID = "urn:oasis:names:tc:xacml:1.0:function:not";
    public static final String XACML_STRING_TYPE = "http://www.w3.org/2001/XMLSchema#string";
    public static final String XACML_INTEGER_TYPE = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String XACML_TIME_TYPE = "http://www.w3.org/2001/XMLSchema#time";
    public static final String XACML_DATE_TIME_TYPE = "http://www.w3.org/2001/XMLSchema#dateTime";

    private static abstract class AbstractRelationTypeInfo implements IRelationTypeInfo {
        public boolean applyNot() {
            return false;
        }
    }

    private static abstract class StrRelationTypeInfo extends AbstractRelationTypeInfo {
        public String getDataType() {
            return XACML_STRING_TYPE;
        }
    }

    private static abstract class IntegerRelationTypeInfo extends AbstractRelationTypeInfo {
        public String getDataType() {
            return XACML_INTEGER_TYPE;
        }
    }

    private static abstract class DateTimeRelationTypeInfo extends AbstractRelationTypeInfo {
        public String getDataType() {
            return XACML_DATE_TIME_TYPE;
        }
    }

    private static abstract class TimeRelationTypeInfo extends AbstractRelationTypeInfo {
        public String getDataType() {
            return XACML_TIME_TYPE;
        }
    }

    // Used by the action predicate, which is a special case, so we make it visible
    public static IRelationTypeInfo STRING_EQ = new StrRelationTypeInfo() {
        public String getMatchFunction() {
            // Our string equality is almost invariably regular expression based,
            // even when it doesn't need to be
            return XACMLDomainObjectFormatter.NEXTLABS_URN + "builtin:string-equal";
        }
    };

    private static IRelationTypeInfo STRING_NE = new StrRelationTypeInfo() {
        public String getMatchFunction() {
            return XACMLDomainObjectFormatter.NEXTLABS_URN + "builtin:string-equal";
        }

        public boolean applyNot() {
            return true;
        }
    };

    private static IRelationTypeInfo INTEGER_EQ = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
        }
    };

    private static IRelationTypeInfo INTEGER_NE = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
        }

        public boolean applyNot() {
            return true;
        }
    };

    private static IRelationTypeInfo INTEGER_LT = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-less-than";
        }
    };

    private static IRelationTypeInfo INTEGER_LE = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal";
        }
    };

    private static IRelationTypeInfo INTEGER_GT = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than";
        }
    };

    private static IRelationTypeInfo INTEGER_GE = new IntegerRelationTypeInfo() {
        public String getMatchFunction() {
            return "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal";
        }
    };

    private static IRelationTypeInfo DATE_TIME_EQ = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-equal";
        }
    };

    private static IRelationTypeInfo DATE_TIME_NE = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-equal";
        }

        public boolean applyNot() {
            return true;
        }
    };

    private static IRelationTypeInfo DATE_TIME_LT = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than";
        }
    };

    private static IRelationTypeInfo DATE_TIME_LE = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-equal-to";
        }
    };

    private static IRelationTypeInfo DATE_TIME_GT = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than";
        }
    };

    private static IRelationTypeInfo DATE_TIME_GE = new DateTimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-equal-to";
        }
    };

    private static IRelationTypeInfo TIME_EQ = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-equal";
        }
    };

    private static IRelationTypeInfo TIME_NE = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-equal";
        }

        public boolean applyNot() {
            return true;
        }
    };

    private static IRelationTypeInfo TIME_LT = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-less-than";
        }
    };

    private static IRelationTypeInfo TIME_LE = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-less-than-equal-to";
        }
    };

    private static IRelationTypeInfo TIME_GT = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-greater-than";
        }
    };

    private static IRelationTypeInfo TIME_GE = new TimeRelationTypeInfo() {
        public String getMatchFunction(){
            return "urn:oasis:names:tc:xacml:1.0:function:time-greater-than-equal-to";
        }
    };

    private static final Map<IAttribute, XACMLType> knownAttributes = new HashMap<IAttribute, XACMLType>();

    static {
        knownAttributes.put(HeartbeatAttribute.TIME_SINCE_LAST_HEARTBEAT, XACMLType.INTEGER);

        knownAttributes.put(RemoteAccessAttribute.REMOTE_ACCESS, XACMLType.INTEGER);
        knownAttributes.put(RemoteAccessAttribute.REMOTE_ADDRESS, XACMLType.INTEGER);

        knownAttributes.put(ResourceAttribute.ACCESS_DATE, XACMLType.DATETIME);
        knownAttributes.put(ResourceAttribute.CREATED_DATE, XACMLType.DATETIME);
        knownAttributes.put(ResourceAttribute.MODIFIED_DATE, XACMLType.DATETIME);
        knownAttributes.put(ResourceAttribute.PORTAL_CREATED, XACMLType.DATETIME);
        knownAttributes.put(ResourceAttribute.PORTAL_MODIFIED, XACMLType.DATETIME);
        knownAttributes.put(ResourceAttribute.SIZE, XACMLType.INTEGER);
        knownAttributes.put(ResourceAttribute.PORTAL_FILESIZE, XACMLType.INTEGER);

        knownAttributes.put(SubjectAttribute.SENT_TO_CLIENT_COUNT, XACMLType.INTEGER);

        knownAttributes.put(TimeAttribute.IDENTITY, XACMLType.DATETIME);
        knownAttributes.put(TimeAttribute.YEAR, XACMLType.INTEGER);
        knownAttributes.put(TimeAttribute.TIME, XACMLType.TIME);
        knownAttributes.put(TimeAttribute.DATE, XACMLType.INTEGER);
        knownAttributes.put(TimeAttribute.DOWIM, XACMLType.INTEGER);
    }

    private static XACMLType getExpressionType(IExpression expr) {
        if (expr == null) {
            return XACMLType.UNKNOWN;
        }

        final XACMLType[] result = new XACMLType[1];
        result[0] = XACMLType.UNKNOWN;

        IExpressionVisitor ev = new DefaultExpressionVisitor() {
            public void visit(IAttribute attr) {
                // We don't have a good way to determine the type of
                // an attribute.  We'll assume it is a string unless
                // it's one of a few exceptions that we know

                XACMLType xt = knownAttributes.get(attr);

                result[0] = (xt == null) ? XACMLType.STRING : xt;
            }

            public void visit(Constant constant) {
                ValueType vt = constant.getValue().getType();

                if (vt == ValueType.LONG) {
                    result[0] = XACMLType.INTEGER;
                } else if (vt == ValueType.DATE) {
                    result[0] = XACMLType.DATETIME;
                } else {
                    result[0] = XACMLType.STRING;
                }
            }
        };

        expr.acceptVisitor(ev, IExpressionVisitor.PREORDER);

        return result[0];
    }

    private static XACMLType getRelationType(IRelation rel) {
        /*
         * The RHS is usually a constant, and thus it's easy to deduce the type, but we
         * also lose some information (times, for example, are reported as an integer).
         * See if we can work it out from the LHS and use the right as a last resort
         */
        XACMLType xt = getExpressionType(rel.getLHS());

        if (xt == XACMLType.UNKNOWN) {
            xt = getExpressionType(rel.getRHS());
        }

        return xt;
    }


    public static IRelationTypeInfo getMatchInformation(IRelation rel) {
        XACMLType xt = getRelationType(rel);

        if (xt == XACMLType.UNKNOWN || xt == null) {
            // If all else fails, assume we are dealing with strings
            xt = XACMLType.STRING;
        }

        RelationOp op = rel.getOp();

        if (xt == XACMLType.INTEGER) {
            if (op == RelationOp.EQUALS) {
                return INTEGER_EQ;
            } else if (op == RelationOp.NOT_EQUALS) {
                return INTEGER_NE;
            } else if (op == RelationOp.LESS_THAN) {
                return INTEGER_LT;
            } else if (op == RelationOp.LESS_THAN_EQUALS) {
                return INTEGER_LE;
            } else if (op == RelationOp.GREATER_THAN) {
                return INTEGER_GT;
            } else if (op == RelationOp.GREATER_THAN_EQUALS) {
                return INTEGER_GE;
            }
        } else if (xt == XACMLType.DATETIME) {
            if (op == RelationOp.EQUALS) {
                return DATE_TIME_EQ;
            } else if (op == RelationOp.NOT_EQUALS) {
                return DATE_TIME_NE;
            } else if (op == RelationOp.LESS_THAN) {
                return DATE_TIME_LT;
            } else if (op == RelationOp.LESS_THAN_EQUALS) {
                return DATE_TIME_LE;
            } else if (op == RelationOp.GREATER_THAN) {
                return DATE_TIME_GT;
            } else if (op == RelationOp.GREATER_THAN_EQUALS) {
                return DATE_TIME_GE;
            }
        } else if (xt == XACMLType.TIME) {
            if (op == RelationOp.EQUALS) {
                return TIME_EQ;
            } else if (op == RelationOp.NOT_EQUALS) {
                return TIME_NE;
            } else if (op == RelationOp.LESS_THAN) {
                return TIME_LT;
            } else if (op == RelationOp.LESS_THAN_EQUALS) {
                return TIME_LE;
            } else if (op == RelationOp.GREATER_THAN) {
                return TIME_GT;
            } else if (op == RelationOp.GREATER_THAN_EQUALS) {
                return TIME_GE;
            }
        } else if (xt == XACMLType.STRING) {
            if (op == RelationOp.EQUALS) {
                return STRING_EQ;
            } else if (op == RelationOp.NOT_EQUALS) {
                return STRING_NE;
            }
        }

        // If all else fails, assume string equality
        return STRING_EQ;
    }
    
}
