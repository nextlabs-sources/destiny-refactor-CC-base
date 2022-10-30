package com.bluejungle.framework.datastore.hibernate.utils;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/utils/MassDMLUtils.java#1 $
 */

import java.util.regex.Pattern;

import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;

/**
 * This class provides utilities for managing Mass DML statements
 * from mostly-Hibernate solutions.
 *
 * @author Sergey Kalinichenko
 */
public class MassDMLUtils {

    /**
     * Returns a mass DML formatter based on the provided session.
     *
     * @param session the session for which to build a mass DML formatter.
     * @return a mass DML formatter based on the provided session.
     */
    public static IMassDMLFormatter makeFormatter(Session session) {
        return new MassDMLFormatter(session);
    }

    private static class MassDMLFormatter implements IMassDMLFormatter {

        /**
         * This pattern matches patterns like #<name># and $<name>$.
         */
        private static final Pattern VAR_PATTERN =
            Pattern.compile("[$]([0-9a-zA-Z_+-:@. ]+)[$]");

        private static String FAKE_SEQUENCE = "^";

        private final String defaultSource;

        private final String replacementSourceVars;

        private final String replacementTargetVars;

        protected MassDMLFormatter(Session session) {
            if (session == null) {
                throw new NullPointerException("session");
            }
            Dialect dialect = null;
            if (session instanceof SessionImplementor) {
                SessionImplementor sessionImpl = (SessionImplementor)session;
                SessionFactoryImplementor sessionFactoryImpl = sessionImpl.getFactory();
                dialect = sessionFactoryImpl.getDialect();
            }
            if (dialect == null) {
                throw new IllegalArgumentException(
                    "Hibernate session must be based on a session factory of a known type."
                );
            }

            if (dialect.supportsIdentityColumns()) {
                replacementSourceVars =
                replacementTargetVars = "";
            } else if (dialect.supportsSequences()) {
                try {
                    String s = dialect.getSequenceNextValString(FAKE_SEQUENCE);
                    String prefix = s.substring(s.indexOf(' ')+1, s.indexOf(FAKE_SEQUENCE));
                    int from = s.indexOf("from");
                    String suffix;
                    if (from != -1) {
                        suffix = s.substring(s.indexOf(FAKE_SEQUENCE)+1, from-1);
                    } else {
                        suffix = s.substring(s.indexOf(FAKE_SEQUENCE)+1);
                    }
                    replacementSourceVars = prefix+"$1"+suffix+", ";
                } catch(MappingException me) {
                    throw new IllegalStateException("Dialect supports sequences! ", me);
                }
                replacementTargetVars = "$1, ";
            } else {
                throw new IllegalArgumentException(
                    "The dialect must either supports identity columns or supports sequences."
                );
            }
            // Figure out the default source
            String src = null;
            if (dialect.supportsSequences()) {
                try {
                    String nextVal = dialect.getSequenceNextValString(FAKE_SEQUENCE);
                    int pos = nextVal.indexOf("from");
                    if (pos != -1) {
                        src = nextVal.substring(pos+5);
                    }
                } catch (MappingException me) {
                    throw new IllegalStateException("Dialect supports sequences! ", me);
                }
            }
            defaultSource = src;
        }

        /**
         * @see IMassDMLFormatter#formatInsert(String, String, String)
         */
        public String formatInsert(String target, String sourceFields, String source) {
            StringBuffer res = new StringBuffer("insert into ");
            res.append(VAR_PATTERN.matcher(target).replaceAll(replacementTargetVars));
            if (source == null) {
                source = defaultSource;
            }
            if (source != null) {
                res.append(" (select ");
            } else {
                res.append(" values (");
            }
            res.append(VAR_PATTERN.matcher(sourceFields).replaceAll(replacementSourceVars));
            if (source != null) {
                res.append(" from ");
                res.append(source);
            }
            res.append(')');
            return res.toString();
        }

    }

}
