package com.bluejungle.destiny.appdiscovery;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows "work in progress" dialog with a [Cancel] button,
 * and generates the LDIF file while listening for cancellation requests.
 */
public class LDIFGenerationDialog extends Dialog {

    private static final Point SIZE = new Point(600, 120);
    private Shell shell = null;
    private boolean stopWorker = false;
    private Label progress;
    private LDIFGenerator generator = null;

    public LDIFGenerationDialog( Shell parent, String savePath, FileSystemSelector selector, TreeEntry[] roots ) {
        super( parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL );
        File target = new File( savePath );
        if ( target.exists() ) {
            MessageBox areYouSure = new MessageBox( parent, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
            areYouSure.setText("File overwrite confirmation");
            areYouSure.setMessage("File exists. Overwrite?");
            if ( areYouSure.open() == SWT.NO ) {
                return;
            }
        }
        try {
            generator = new LDIFGenerator( target, selector, roots );
        } catch ( FileNotFoundException e ) {
        }
    }

    private void initialize() {
        shell.setSize(SIZE);
        RowLayout layout = new RowLayout( SWT.VERTICAL );
        layout.fill = true;
        layout.spacing = 10;
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        shell.setLayout( layout );
        Composite top = new Composite( shell, SWT.NONE );
        RowLayout topLayout = new RowLayout( SWT.HORIZONTAL );
        topLayout.pack = true;
        topLayout.fill = false;
        topLayout.justify = false;
        top.setLayout( topLayout );
        Label currentFile = new Label( top, SWT.NONE );
        currentFile.setText( "Current file: " );
        currentFile.setAlignment( SWT.LEFT );
        progress = new Label( top, SWT.NONE );
        progress.setText("##################################");
        progress.setAlignment( SWT.LEFT );
        Composite bottom = new Composite( shell, SWT.NONE );
        RowLayout bottomLayout = new RowLayout( SWT.HORIZONTAL );
        bottomLayout.pack = false;
        bottomLayout.fill = false;
        bottomLayout.justify = false;
        bottom.setLayout( bottomLayout );
        Button cancel = new Button( bottom, SWT.PUSH );
        cancel.setText("Cancel");
        Label rightFill = new Label( bottom, SWT.NONE );
        rightFill.setLayoutData( new RowData( 80, 24 ) );
        cancel.setLayoutData( new RowData( 80, 24 ) );
        shell.layout(true);
        progress.setText("");
        cancel.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent e ) {
                close();
            }
            public void widgetDefaultSelected( SelectionEvent e ) {
                close();
            }
            private void close() {
                if ( !shell.isDisposed() ) {
                    shell.close();
                }
            }
        } );
    }

    public void open() {
        if ( generator == null ) {
            return;
        }
        Shell parent = getParent();
        this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        this.shell.setText("LDIF Generation In Progress");
        this.initialize();
        this.shell.open();
        stopWorker = false;
        final Display display = parent.getDisplay();
        Thread workerThread = new Thread() {
            public void run() {
                while ( generator.processNext() && !stopWorker )
                    ;
                display.asyncExec( new Runnable() {
                    public void run() {
                        if ( !shell.isDisposed() ) {
                            shell.close();
                        }
                    }
                } );
            }
        };
        workerThread.setName("LDIF Worker Thread");
        workerThread.start();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                String str = generator.getLastProcessedPath();
                progress.setText( str.substring( 0, Math.min( 60, str.length() ) ) );
                shell.layout( true );
                shell.redraw();
                display.sleep();
            }
        }
        stopWorker = true;
    }
}
