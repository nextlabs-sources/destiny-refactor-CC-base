package com.bluejungle.destiny.appdiscovery;

/*
 * Created on Dec 9, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author sergey
 *
 * Instances of this class store information about a file system tree,
 * letting you pick files and directories, store the selection,
 * and restore the selection from a file.
 *
 * This class implements the following check/uncheck logic:
 */
public class FileSystemSelector {

    /**
     * This <code>Map</code>'s keys are of type <code>File</code>
     * and values are of type <code>Boolean</code>. The map contains
     * entries for all files or directories for which the user has
     * explicitly checked or unchecked a checkbox. The value stored
     * for the corresponding key is the state of that checkbox.
     */
    private Map checkedFiles = new HashMap();

    /**
     * This <code>Map</code>'s keys are of type <code>File</code>
     * and values are of type <code>Integer</code>. Each entry contains
     * the number of direct or indirect children of the given directory
     * for which an explicitly checked checkbox exists. All values are
     * positive, because when a value reaches zero it is removed.
     *
     * When the state is read from the configuration, the initialization
     * code calculates the state of this map. The code that changes the state
     * incrementally maintains this map as new checked/unchecked information
     * comes in.
     */
    private Map checkedChildrenCount = new HashMap();

    /**
     * This <code>Map</code> is a cache for the hasCheckedParents method.
     * Keys of this <code>Map</code> are of type <code>File</code>;
     * the values are of type <code>Boolean</code>. When a value is present
     * in the <code>Map</code>, true means that there is a checked parent
     * for the given key; false means that the path from the given key
     * to the root does not have checked items; missing key means that the
     * corresponding value has not been calculated yet.
     */
    private Map checkedParents = new HashMap();

    public CheckedState getChecked( TreeEntry f ) {
        Boolean checked = (Boolean)checkedFiles.get(f);
        if ( checked == null ) {
            if ( checkedChildrenCount.containsKey(f) ) {
                return CheckedState.CHECKED_DEF;
            } else if (hasCheckedParents(f)) {
                return CheckedState.CHECKED;
            } else {
                return CheckedState.UNCHECKED;
            }
        } else {
            if( checked.booleanValue() ) {
                return CheckedState.CHECKED;
            } else if ( checkedChildrenCount.containsKey(f) ) {
                return CheckedState.CHECKED_DEF;
            } else {
                return CheckedState.UNCHECKED;
            }
        }
    }

    public void setChecked( TreeEntry f, boolean state ) {
        Boolean was = (Boolean)checkedFiles.get(f);
        checkedFiles.put(f, Boolean.valueOf(state));
        if ( state ) {
            checkAllParents(f);
        } else {
            uncheckAllParentsAndChildren(f, was!=null && was.booleanValue());
        }
        clearCheckedParentCache(f);
    }

    private void clearCheckedParentCache( TreeEntry f ) {
        for ( Iterator iter = checkedParents.entrySet().iterator() ; iter.hasNext() ; ) {
            Map.Entry item = (Map.Entry)iter.next();
            if ( f.isParentOf((TreeEntry)item.getKey())) {
                iter.remove();
            }
        }
    }

    private boolean hasCheckedParents(TreeEntry f) {
        if ( f == null ) {
            return false;
        }
        Boolean res = (Boolean)checkedParents.get(f);
        if ( res == null ) {
            TreeEntry parent = f.getParent();
            res = (Boolean)checkedFiles.get(parent);
            if ( res == null ) {
                res = Boolean.valueOf( hasCheckedParents( parent ) );
            }
            checkedParents.put(f, res);
        }
        return res.booleanValue();
    }

    private void checkAllParents(TreeEntry f) {
        if ( f == null ) {
            return;
        }
        f = f.getParent();
        while ( f != null ) {
            if (!checkedChildrenCount.containsKey(f) ) {
                checkedChildrenCount.put(f, new Integer(1));
            } else {
                int newVal = ((Integer)checkedChildrenCount.get(f)).intValue()+1;
                checkedChildrenCount.put(f, new Integer( newVal ));
            }
            f = f.getParent();
        }
    }

    private void uncheckAllParentsAndChildren(TreeEntry f, boolean wasChecked) {
        if ( f == null ) {
            return;
        }
        for ( Iterator iter = checkedFiles.entrySet().iterator() ; iter.hasNext() ; ) {
            Map.Entry item = (Map.Entry)iter.next();
            if ( f.isParentOf((TreeEntry)item.getKey()) ) {
                iter.remove();
            }
        }
        for ( Iterator iter = checkedChildrenCount.entrySet().iterator() ; iter.hasNext() ; ) {
            Map.Entry item = (Map.Entry)iter.next();
            if ( f.isParentOf((TreeEntry)item.getKey()) ) {
                iter.remove();
            }
        }
        Integer tmp = (Integer)checkedChildrenCount.get(f);
        int toSubtract = (tmp==null) ? 0 : tmp.intValue();
        if ( wasChecked ) {
            toSubtract++;
        }
        checkedChildrenCount.remove(f);
        f = f.getParent();
        while ( f != null ) {
            if ( checkedChildrenCount.containsKey(f) ) {
                int newVal = ((Integer)checkedChildrenCount.get(f)).intValue()-toSubtract;
                if ( newVal == 0 ) {
                    checkedChildrenCount.remove(f);
                } else {
                    checkedChildrenCount.put(f, new Integer(newVal));
                }
            }
            f = f.getParent();
        }
    }

    public void saveToFile( File f ) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream( new FileOutputStream( f ) );
            oos.writeObject( checkedFiles );
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
        } finally {
            if ( oos != null ) {
                try { oos.close(); } catch (IOException ignored ) {}
            }
        }
    }

    public static FileSystemSelector fromFile( File f ) {
        FileSystemSelector res = new FileSystemSelector();
        if ( f.exists() ) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream( new FileInputStream( f ) );
                res.checkedFiles = (Map)ois.readObject();
                for ( Iterator iter = res.checkedFiles.entrySet().iterator() ; iter.hasNext() ; ) {
                    Map.Entry item = (Map.Entry)iter.next();
                    if ( ((Boolean)item.getValue()).booleanValue() ) {
                        res.checkAllParents((TreeEntry)item.getKey());
                    }
                }
            } catch ( FileNotFoundException e ) {
                e.printStackTrace();
            } catch ( IOException e ) {
                e.printStackTrace();
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            } catch ( ClassCastException e ) {
                e.printStackTrace();
            } finally {
                if ( ois != null ) {
                    try { ois.close(); } catch (IOException ignored ) {}
                }
            }
        }
        return res;
    }

}
