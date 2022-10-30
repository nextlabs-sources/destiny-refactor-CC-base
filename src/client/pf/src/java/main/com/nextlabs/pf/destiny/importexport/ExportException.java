package com.nextlabs.pf.destiny.importexport;

public class ExportException extends Exception {
    private static final long serialVersionUID = -2322816613917085398L;

	/**
     * Constructs an empty <code>ExportException</code>.
     */
    public ExportException() {
    	super();
    }

    /**
     * Constructs a <code>ExportException</code> with the
     * specified message.
     * @param message The message of the <code>ExportException</code>.
     */
    public ExportException( String message ) {
        super( message );
    }

    /**
     * Constructs a <code>ExportException</code> with the
     * specified cause.
     * @param cause The cause of this <code>ExportException</code>.
     */
    public ExportException(Exception cause) {
        super(cause);
    }

}
