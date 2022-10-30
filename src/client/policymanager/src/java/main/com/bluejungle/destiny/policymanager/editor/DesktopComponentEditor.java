/*
 * Created on Apr 19, 2005
 * 
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
 * @author dstarke
 * 
 */
public class DesktopComponentEditor extends ComponentEditor {

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public DesktopComponentEditor(Composite parent, int style, IDSpec domainObject) {
        super(parent, style, domainObject, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyOperatorList()
     */
    protected List<String> getPropertyOperatorList() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyList()
     */
    protected List<String> getPropertyList() {
        return null;
    }

    protected EntityType getEntityType() {
        return EntityType.HOST;
    }

    protected SpecType getSpecType() {
        return SpecType.HOST;
    }

    protected String getMemberLabel() {
        return EditorMessages.DESKTOPCOMPONENTEDITOR_COMPUTERS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#getObjectTypeLabelText()
     */
    public String getObjectTypeLabelText() {
        return EditorMessages.DESKTOPCOMPONENTEDITOR_COMPUTER_COMPONENT;
    }

    protected Class getPreviewClass() {
        return DesktopPreviewPanel.class;
    }

    public static class DesktopPreviewPanel extends PreviewPanel {

        public DesktopPreviewPanel() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#setupPreviewTable(org.eclipse.swt.widgets.Table)
         */
        protected void setupPreviewTable(Table table) {
            TableColumn c1 = new TableColumn(table, SWT.LEFT);
            c1.setWidth(150);
            c1.setText(EditorMessages.DESKTOPCOMPONENTEDITOR_HOST_NAME);

            TableColumn c2 = new TableColumn(table, SWT.LEFT);
            c2.setWidth(150);
            c2.setText(EditorMessages.DESKTOPCOMPONENTEDITOR_DNS_HOST_NAME);

            tableViewer.setContentProvider(new ViewContentProvider());
            tableViewer.setLabelProvider(new ViewLabelProvider());
            tableViewer.setInput(previewResults);
        }

        protected EntityType getEntityType() {
            return EntityType.HOST;
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
        return ComponentEnum.HOST;
    }
}