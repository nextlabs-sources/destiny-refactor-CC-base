package com.nextlabs.language.processor;

import java.util.ArrayList;
import java.util.List;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/evaluation/src/java/main/com/nextlabs/language/processor/DefinitionProcessingException.java#1 $
 */

/**
 * This exception represents a collection of processing errors resulting from
 * processing a single repository of definitions. A single exception may
 * provide feedback for multiple problems, each identified by its error code.
 * In addition to the error code each problem reports an optional set of
 * data that may be useful in pinpointing the exact problem. For example,
 * a type collision error may include the names of the contexts and their
 * attributes causing the type collision.
 *
 * @author Sergey Kalinichenko
 */
public class DefinitionProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<DefinitionErrorCode> errorCodes =
        new ArrayList<DefinitionErrorCode>();

    private final List<Object[]> errorData = new ArrayList<Object[]>();

    public void addError(DefinitionErrorCode code, Object ... data) {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (data == null) {
            throw new NullPointerException("data");
        }
        errorCodes.add(code);
        errorData.add(data);
    }

    public DefinitionErrorCode getErrorCode(int i) {
        return errorCodes.get(i);
    }

    public Object[] getErrorData(int i) {
        return errorData.get(i);
    }

    public int getErrorCount() {
        return errorCodes.size();
    }

}
