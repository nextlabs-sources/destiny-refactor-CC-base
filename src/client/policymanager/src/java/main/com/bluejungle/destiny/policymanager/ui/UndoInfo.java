/*
 * Created on Mar 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class UndoInfo {

    private List<IUndoElement> undoElementArray = new ArrayList<IUndoElement>();
    private int currentIndex = -1;

    public void addUndoElement(IUndoElement undoElement) {

        if (currentIndex < undoElementArray.size() - 1) {
            List<IUndoElement> subList = new ArrayList<IUndoElement>(undoElementArray.subList(currentIndex + 1, undoElementArray.size()));
            undoElementArray.removeAll(subList);
        }

        undoElementArray.add(undoElement);
        currentIndex++;
        if (currentIndex == 0)
            PolicyManagerActionFactory.getUndoAction().setEnabled(true);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * @param currentObject
     */
    public void undo(Object currentObject) {
        Object ug = currentObject;
        // IDCompositeSpec spec = (IDCompositeSpec) ug.elementAt(0);
        IUndoElement undoElement = undoElementArray.get(currentIndex--);
        undoElement.undo(ug, null);

        while (undoElement.isContinuation()) {
            undoElement = undoElementArray.get(currentIndex--);
            undoElement.undo(ug, null);
        }

        if (currentIndex == -1)
            PolicyManagerActionFactory.getUndoAction().setEnabled(false);

        PolicyManagerActionFactory.getRedoAction().setEnabled(true);
    }

    /**
     * @param currentObject
     */
    public void redo(Object currentObject) {
        Object ug = currentObject;
        IUndoElement undoElement = undoElementArray.get(currentIndex + 1);
        currentIndex++;
        undoElement.redo(ug, null);

        if (canRedo())
            undoElement = undoElementArray.get(currentIndex + 1);

        while (undoElement.isContinuation()) {
            currentIndex++;
            undoElement.redo(ug, null);
            if (canRedo())
                undoElement = undoElementArray.get(currentIndex + 1);
        }

        if (currentIndex >= undoElementArray.size() - 1)
            PolicyManagerActionFactory.getRedoAction().setEnabled(false);

        PolicyManagerActionFactory.getUndoAction().setEnabled(true);
    }

    /**
     * @return
     */
    public boolean canUndo() {
        return currentIndex >= 0;
    }

    /**
     * @return
     */
    public boolean canRedo() {
        return currentIndex < undoElementArray.size() - 1;
    }
}