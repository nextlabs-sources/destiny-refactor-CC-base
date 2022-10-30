package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/EvaluationException.java#1 $
 */

/**
 *
 * @author Sergey Kalinichenko
 */
public class EvaluationException extends RuntimeException {

    /**
     * The default serial version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new evaluation exception with the specified cause.
     *
     * @param cause the cause of this exception.
     */
    public EvaluationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new evaluation exception with the specified message
     * and the cause.
     *
     * @param message the message to put into this exception.
     * @param cause the cause of this exception.
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new evaluation exception with the specified message.
     *
     * @param message the message to put into this exception.
     */
    public EvaluationException(String message) {
        super(message);
    }

}
