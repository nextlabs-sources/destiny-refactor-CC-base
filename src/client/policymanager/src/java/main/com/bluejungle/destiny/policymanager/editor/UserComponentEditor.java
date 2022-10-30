/*
 * Created on Mar 16, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PreviewPanel;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/UserComponentEditor.java#1 $:
 */

public class UserComponentEditor extends ComponentEditor {

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public UserComponentEditor(Composite parent, int style, IDSpec domainObject) {
        super(parent, style, domainObject, true);
    }

    /**
     * 
     * returns the list of valid property attributes. this list is used to
     * populate the combo
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getPropertyList()
     */
    protected List<String> getPropertyList() {
        return null;
    }

    /**
     * returns the list of valid property attribute operators. this list is used
     * to populate the combo
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getPropertyOperatorList()
     */
    protected List<String> getPropertyOperatorList() {
        return null;
    }

    protected EntityType getEntityType() {
        return EntityType.USER;
    }

    protected SpecType getSpecType() {
        return SpecType.USER;
    }

    protected String getMemberLabel() {
        return EditorMessages.USERCOMPONENTEDITOR_USERS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#getObjectTypeLabelText()
     */
    public String getObjectTypeLabelText() {
        return EditorMessages.USERCOMPONENTEDITOR_USER_COMPONENT;
    }

    protected Class getPreviewClass() {
        return UserPreviewPanel.class;
    }

    public static class UserPreviewPanel extends PreviewPanel {

        public UserPreviewPanel() {
        }

        /**
         * @param table
         *            preview table
         */
        protected void setupPreviewTable(Table table) {
            TableColumn c1 = new TableColumn(table, SWT.LEFT);
            c1.setWidth(100);
            c1.setText(EditorMessages.USERCOMPONENTEDITOR_NAME);

            TableColumn c2 = new TableColumn(table, SWT.LEFT);
            c2.setWidth(100);
            c2.setText(EditorMessages.USERCOMPONENTEDITOR_ID);

            tableViewer.setContentProvider(new ViewContentProvider());
            tableViewer.setLabelProvider(new ViewLabelProvider());
            tableViewer.setInput(this.previewResults);
        }

        protected EntityType getEntityType() {
            return EntityType.USER;
        }

        /**
         * @see com.bluejungle.destiny.policymanager.ui.PreviewPanel#makePreviewItem(java.lang.Object)
         */
        protected PreviewItem makePreviewItem(Object data) {
            final LeafObject leaf = (LeafObject) data;
            return new PreviewItem() {

                public String getText(int index) {
                    if (index == 0) {
                        return leaf.getName();
                    } else if (index == 1) {
                        return leaf.getUniqueName();
                    }
                    return null;
                }

                public Image getImage(int index) {
                    return index == 0 ? ObjectLabelImageProvider.getImage(leaf) : null;
                }
            };
        }
    }

    @Override
    protected ComponentEnum getComponentType() {
        return ComponentEnum.USER;
    }
}