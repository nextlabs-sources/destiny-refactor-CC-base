package com.bluejungle.destiny.appdiscovery;

/*
 * Created on Dec 8, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * @author sergey
 *
 * The Application Discovery Application
 */
public class ApplicationDiscovery extends ApplicationWindow implements FileFilter {
    private static final Image FOLDER = new Image( Display.getCurrent(), ApplicationDiscovery.class.getClassLoader().getResourceAsStream("resources/images/dir.gif") );
    private static final Image FILE = new Image( Display.getCurrent(), ApplicationDiscovery.class.getClassLoader().getResourceAsStream("resources/images/file.gif") );
    private static final File CONFIG_FILE = new File("appdiscovery.cfg");

    public static void main( String[] args ) {
        ApplicationDiscovery ae = new ApplicationDiscovery();
        ae.setBlockOnOpen( true );
        ae.open();
        Display.getCurrent().dispose();
    }

    private static final TreeEntry ROOT = new TreeEntry() {
        private final IOSWrapper osWrapper = (IOSWrapper)ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
        public TreeEntry[] getChildren(FileFilter ignored) {
            File[] tmp = File.listRoots();
            List res = new ArrayList( tmp.length );
            for ( int i = 0 ; i != tmp.length ; i++ ) {
                if ( !osWrapper.isRemovableMedia(tmp[i].getAbsolutePath()) && tmp[i].isDirectory() ) {
                    res.add( new FileTreeEntry(tmp[i]) );
                }
            }
            return (TreeEntry[])res.toArray( new TreeEntry[res.size()] );
        }
        public String getPath() {
            return "";
        }
        public boolean hasChildren() {
            return true;
        }
        public TreeEntry getParent() {
            return null;
        }
        public boolean isParentOf( TreeEntry entry ) {
            return false;
        }
        public String getName() {
            return "";
        }
    };

    FileSystemSelector fss = FileSystemSelector.fromFile( CONFIG_FILE );
    CheckboxTreeViewer tv;
    FileDialog saveDialog;

    public ApplicationDiscovery() {
        super( null );
    }

    protected Control createContents( final Composite parent ) {
        parent.getShell().setText("Application Discovery");
        parent.getShell().setSize( 360, 600 );
        tv = new CheckboxTreeViewer( parent, SWT.CHECK );
        tv.setContentProvider( new FileTreeContentProvider() );
        tv.setLabelProvider( new FileTreeLabelProvider() );
        tv.setInput( ROOT );
        setCheckedState( ROOT.getChildren(this) );
        tv.addTreeListener( new ITreeViewerListener() {
            public void treeCollapsed( TreeExpansionEvent arg0 ) {
            }
            public void treeExpanded( TreeExpansionEvent evt ) {
                setCheckedState(((TreeEntry)evt.getElement()).getChildren(ApplicationDiscovery.this));
            }
        } );
        tv.addCheckStateListener( new ICheckStateListener() {
            public void checkStateChanged( CheckStateChangedEvent evt ) {
                TreeEntry f = (TreeEntry)evt.getElement();
                CheckedState cs = fss.getChecked( f );
                fss.setChecked( f, !cs.isChecked() );
                setCheckedState( new TreeEntry[] { f } );
                f = f.getParent();
                while ( f != null ) {
                    cs = fss.getChecked(f);
                    tv.setGrayed( f, cs.isDefault() );
                    tv.setChecked( f, cs.isChecked() );
                    f = f.getParent();
                }
            }
        } );
        Menu menuBar = new Menu( parent.getShell(), SWT.BAR );
        parent.getShell().setMenuBar( menuBar );
        MenuItem fileItem = new MenuItem( menuBar, SWT.CASCADE );
        fileItem.setText( "File" );
        Menu fileMenu = new Menu( parent.getShell(), SWT.DROP_DOWN );
        fileItem.setMenu( fileMenu );
        MenuItem generate = new MenuItem( fileMenu, SWT.PUSH );
        generate.setText("Generate LDIF");
        generate.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent e ) {
                choseDestinationAndGenerate();
            }
            public void widgetDefaultSelected( SelectionEvent e ) {
                choseDestinationAndGenerate();
            }
        } );
        new MenuItem( fileMenu, SWT.SEPARATOR );
        MenuItem exit = new MenuItem( fileMenu, SWT.PUSH );
        exit.setText("Exit");
        exit.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent e ) {
                parent.getShell().close();
            }
            public void widgetDefaultSelected( SelectionEvent e ) {
                parent.getShell().close();
            }
        } );
        parent.getShell().addDisposeListener( new DisposeListener() {
            public void widgetDisposed( DisposeEvent e ) {
                fss.saveToFile( CONFIG_FILE );
            }
        } );
        Shell dialogShell = new Shell( Display.getCurrent() );
        saveDialog = new FileDialog( dialogShell, SWT.SAVE | SWT.APPLICATION_MODAL );
        saveDialog.setText("Save generated LDIF file");
        saveDialog.setFilterPath("user.home");
        saveDialog.setFilterExtensions( new String[] {"*.ldif", "*.*"} );
        saveDialog.setFilterNames( new String[] {"LDIF (*.ldif)", "All Files"} );
        return parent;
    }

    public class FileTreeContentProvider implements ITreeContentProvider {
        public Object[] getChildren( Object element ) {
            return ((TreeEntry)element).getChildren(ApplicationDiscovery.this);
        }
        public Object[] getElements( Object element ) {
            return getChildren(element);
        }
        public boolean hasChildren( Object element ) {
            return ((TreeEntry)element).hasChildren();
        }
        public Object getParent( Object element ) {
            return ((TreeEntry)element).getParent();
        }
        public void dispose() {
        }
        public void inputChanged( Viewer viewer, Object old_input, Object new_input ) {
        }
    }

    public class FileTreeLabelProvider extends LabelProvider {
        public Image getImage( Object element ) {
            return ((TreeEntry)element).hasChildren() ? FOLDER : FILE;
        }
        public String getText( Object element ) {
            TreeEntry f = (TreeEntry)element;
            String name = f.getName();
            return (name!=null&&name.length()!=0) ? name : f.getPath();
        }
    }

    private void setCheckedState( TreeEntry[] te ) {
        if ( te == null ) {
            return;
        }
        for ( int i = 0 ; i != te.length ; i++ ) {
            if ( tv.getExpandedState( te[i]) ) {
                TreeEntry[] cf = te[i].getChildren( this );
                setCheckedState( cf );
            }
            CheckedState cs = fss.getChecked( te[i] );
            tv.setGrayed( te[i], cs.isDefault() );
            tv.setChecked( te[i], cs.isChecked() );
        }
    }

    public boolean accept( File f ) {
        if ( f != null ) {
            if ( !f.isDirectory() ) {
                String n = f.getName();
                int len = n.length();
                if ( len > 4 ) {
                    n = n.substring(len-4);
                    return n.equalsIgnoreCase(".exe");
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void choseDestinationAndGenerate() {
        final String savePath = saveDialog.open();
        if ( savePath != null ) {
            LDIFGenerationDialog generationDialog = new LDIFGenerationDialog( Display.getCurrent().getActiveShell(), savePath, fss, ROOT.getChildren(this) );
            generationDialog.open();
        }
    }
}
