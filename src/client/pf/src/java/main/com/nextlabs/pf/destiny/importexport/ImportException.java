package com.nextlabs.pf.destiny.importexport;

public class ImportException extends Exception {
	private static final long serialVersionUID = -5837412900237320071L;

	/**
     * Constructs an empty <code>ImportException</code>.
     */
    public ImportException() {
    	super();
    }

    /**
     * Constructs a <code>ImportException</code> with the
     * specified message.
     * @param message The message of the <code>ImportException</code>.
     */
    public ImportException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>ImportException</code> with the
     * specified cause.
     * @param cause The cause of this <code>ImportException</code>.
     */
    public ImportException(Throwable cause) {
        super(cause);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
