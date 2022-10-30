package com.bluejungle.destiny.appdiscovery;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.ldap.tools.misc.ImportCategoryEnumType;
import com.bluejungle.ldap.tools.misc.NamespaceIDGenerator;
import com.bluejungle.ldap.tools.misc.RelativeIDException;

public class LDIFGenerator {
    private final FileSystemSelector selector;
    private final LinkedList queue = new LinkedList();
    private String exeName = "";
    private final PrintStream output;

    private IComponentManager manager = ComponentManagerFactory.getComponentManager();
    private IOSWrapper osWrapper = (IOSWrapper) manager.getComponent(OSWrapper.class);

    public LDIFGenerator( File target, FileSystemSelector selector, TreeEntry[] roots ) throws FileNotFoundException {
        this.selector = selector;
        for ( int i = 0 ; i != roots.length ; i++ ) {
            queue.addLast( roots[i] );
        }
        output = new PrintStream( new FileOutputStream( target ) );
    }

    public boolean processNext() {
        while ( !queue.isEmpty() ) {
            TreeEntry next = (TreeEntry)queue.removeFirst();
            if ( next.hasChildren() ) {
                TreeEntry[] content = next.getChildren( new FileFilter() {
                    public boolean accept( File f ) {
                        return (f != null)
                            && (f.isDirectory() || f.getName().toLowerCase().endsWith(".exe"))
                            && (selector.getChecked(new FileTreeEntry(f)).isChecked());
                    }
                } );
                if ( content == null ) {
                    continue;
                }
                for ( int i = content.length-1 ; i >= 0 ; i-- ) {
                    queue.addFirst( content[i] );
                }
            } else {
                processSingleFile( next );
                break;
            }
        }
        if ( queue.isEmpty() ) {
            output.close();
            return false;
        } else {
            return true;
        }
    }

    public String getLastProcessedPath() {
        return exeName;
    }

    private void processSingleFile( TreeEntry next ) {
        if ( next == null ) {
            return;
        }

        exeName = next.getPath();

        if ( exeName == null || exeName.length() == 0) {
            System.err.println ("Empty executable name.");
            return;
        }
        String appName = makeAppName( exeName );
        if ( appName == null || appName.length () == 0 ) {
            System.err.println ("Invalid application name for "+exeName);
            return;
        }
        String fingerPrint = osWrapper.getAppInfo(exeName);
        if ( fingerPrint == null || fingerPrint.length() == 0 ) {
            System.err.println ("Error getting application info for "+exeName);
            return;
        }
        String uniqueGlobalIdentifier = null;
        try {
            uniqueGlobalIdentifier = NamespaceIDGenerator.generateInternalImportID(ImportCategoryEnumType.APPLICATIONS, appName);
        } catch (RelativeIDException e) {
            System.err.println ("Unique id generation failed with RelativeIDException for " + exeName + "\n" + e.getMessage());
            return;
        }
        String uniqueSystemIdentifier = null;
        if (exeName.lastIndexOf (File.separatorChar) != -1) {
            uniqueSystemIdentifier = exeName.substring (exeName.lastIndexOf (File.separatorChar) + 1);
        } else {
            uniqueSystemIdentifier = exeName;
        }

        output.println("dn: cn=" + appName + ", o=Applications,dc=DestinyData,dc=Destiny,dc=com");
        output.println("uniqueGlobalIdentifier: " + uniqueGlobalIdentifier);
        output.println("fullyQualifiedName: " + appName);
        output.println("objectClass: top");
        output.println("objectClass: DestinyObject");
        output.println("objectClass: Application");
        output.println("uniqueSystemIdentifier: " + uniqueSystemIdentifier.toLowerCase());
        output.println("cn: " + appName);
        output.println("applicationFingerPrint: " + fingerPrint);
        output.println();
    }

    private String makeAppName( String path ) {
        path = path.toLowerCase();
        path = path.replaceAll("\\\\program files\\\\", "");
        path = path.replaceAll("\\\\bin\\\\", "");
        path = path.replaceAll("\\\\lib\\\\", "");
        path = path.replaceAll("[.]exe$", "");
        path = path.replaceAll("^.:(\\\\)?", "");
        path = path.replaceAll("[{}%$]", "");
        return path.replaceAll("[: \\\\/]", "_");
    }

}
