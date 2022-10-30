/*
 * Created on May 9, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ObjectProperty;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PropertyUndoElement;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

/**
 * @author dstarke
 * 
 */
public abstract class EditorPanel extends Composite implements IEditorPanel {

    protected static final int SPACING = 5;

    // --Internal State-------
    protected IHasId domainObject;
    private boolean editable = true;
    private boolean isInitialized = false;
    private boolean ignoreRelayout = false;

    // --Widgets--------------
    protected Text descriptionText = null;
    protected Composite mainComposite = null;
    protected Composite headingComposite = null;
    protected Composite footerComposite = null;
    protected ScrolledComposite scrolledComposite = null;
    protected Label objectTypeLabel = null;
    protected Label descriptionLabel = null;

    // --Positioning Information---
    private static final Point SIZE_DESCRIPTION_TEXT = new Point(300, 120);

    // --Contstructor----------
    public EditorPanel(Composite parent, int style, IHasId domainObject) {
        super(parent, style);
        this.domainObject = domainObject;
        setBackground(ResourceManager.getColor("EDITOR_BACKGROUD", Activator.getDefault().getPluginPreferences().getString("EDITOR_BACKGROUD")));
    }

    // --Initialization--------
    public void initialize() {
        initializeFrame();
        initializeHeading();
        initializeContents();
        initializeFooter();
        isInitialized = true;
        relayout();
    }

    protected void initializeFrame() {
        setLayout(new FillLayout());

        scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);

        scrolledComposite.getVerticalBar().setIncrement(10);
        scrolledComposite.getVerticalBar().setPageIncrement(100);
        scrolledComposite.getHorizontalBar().setIncrement(10);
        scrolledComposite.getHorizontalBar().setPageIncrement(100);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        mainComposite = new Composite(scrolledComposite, SWT.NONE);
        mainComposite.setBackground(getBackground());
        GridLayout layout = new GridLayout();
        mainComposite.setLayout(layout);

        scrolledComposite.setContent(mainComposite);

        headingComposite = addSectionComposite();
    }

    protected Composite addSectionComposite() {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        Composite ret = new Composite(mainComposite, SWT.NONE);
        ret.setBackground(getBackground());
        ret.setLayoutData(data);
        return ret;
    }

    protected void initializeHeading() {
        FormData data;
        FormLayout headingLayout = new FormLayout();
        headingComposite.setLayout(headingLayout);

        String type = getPolicyType();
        Label label = null;
        if (type != null) {
            label = new Label(headingComposite, SWT.NONE);
            label.setBackground(getBackground());
            label.setText(type);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            label.setAlignment(SWT.LEFT);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.right = new FormAttachment(100, -SPACING);
            data.top = new FormAttachment(0);
            label.setLayoutData(data);
        }

        if (isEditable() && !getObjectTypeLabelText().equalsIgnoreCase(EditorMessages.ACTIONCOMPONENTEDITOR_ACTION_COMPONENT)) {
            Label nameLabel = new Label(headingComposite, SWT.NONE);
            nameLabel.setBackground(getBackground());
            nameLabel.setText(EditorMessages.EDITORPANEL_HEADING_TIP);
            nameLabel.setFont(FontBundle.ATOM_FONT);
            nameLabel.setAlignment(SWT.LEFT);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.right = new FormAttachment(100, -SPACING);
            if (label == null) {
                data.top = new FormAttachment(0);
            } else {
                data.top = new FormAttachment(label, SPACING);
            }
            nameLabel.setLayoutData(data);
        }

        if (headingComposite.getChildren().length == 0) {
            label = new Label(headingComposite, SWT.NONE);
            label.setBackground(getBackground());

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.right = new FormAttachment(100, -SPACING);
            data.top = new FormAttachment(0);
            label.setLayoutData(data);
        }
    }

    protected void initializeFooter() {
        footerComposite = addSectionComposite();
        FormLayout footerLayout = new FormLayout();
        footerComposite.setLayout(footerLayout);

        Composite descriptionLabel = initializeSectionHeading(footerComposite, EditorMessages.EDITORPANEL_DESCRIPTION);
        FormData data = new FormData();
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        data.top = new FormAttachment(0);
        descriptionLabel.setLayoutData(data);

        createDescriptionText();

        data = new FormData();
        data.left = new FormAttachment(0, 100, SPACING);
        data.top = new FormAttachment(descriptionLabel, SPACING);
        data.right = new FormAttachment(100);
        // data.width = SIZE_DESCRIPTION_TEXT.x;
        data.height = SIZE_DESCRIPTION_TEXT.y;
        descriptionText.setLayoutData(data);
    }

    protected Composite initializeSectionHeading(Composite parent, String title) {
        Composite labelComposite = new Composite(parent, SWT.NONE);
        labelComposite.setBackground(getBackground());
        FormLayout layout = new FormLayout();
        labelComposite.setLayout(layout);

        Label sectionLabel = new Label(labelComposite, SWT.NONE);
        sectionLabel.setText(title);
        sectionLabel.setBackground(getBackground());
        sectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, 100, SPACING);
        sectionLabelData.top = new FormAttachment(0, 100, SPACING);
        sectionLabel.setLayoutData(sectionLabelData);

        Label separator = new Label(labelComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBackground(getBackground());
        FormData separatorData = new FormData();
        separatorData.top = new FormAttachment(sectionLabel, 2);
        separatorData.left = new FormAttachment(0, 100, SPACING);
        separatorData.right = new FormAttachment(100);
        separator.setLayoutData(separatorData);

        return labelComposite;
    }

    public abstract void initializeContents();

    /**
     * 
     */
    private void createDescriptionText() {
        int style = (isEditable()) ? SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP : SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY;
        Text text = new Text(footerComposite, style);
        text.setEnabled(isEditable());
        descriptionText = text;
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                Text t = (Text) e.getSource();
                String newDescription = t.getText();
                if (newDescription == null) {
                    newDescription = "";
                }
                String oldDescription = getDescription();
                if (oldDescription == null) {
                    oldDescription = "";
                }
                if (!newDescription.equals(oldDescription)) {
                    PropertyUndoElement undoElement = new PropertyUndoElement();
                    undoElement.setProp(ObjectProperty.DESCRIPTION);
                    undoElement.setOldValue(oldDescription);
                    undoElement.setNewValue(newDescription);
                    GlobalState.getInstance().addUndoElement(undoElement);
                    setDescription(newDescription);
                }
            }
        });

        text.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.character == '\t') {
                    if ((e.stateMask & (SWT.CONTROL | SWT.ALT | SWT.SHIFT)) == 0) {
                        e.doit = false;
                        descriptionText.traverse(SWT.TRAVERSE_TAB_NEXT);
                    }
                }
            }
        });
        String description = getDescription();
        if (description == null) {
            description = "";
        }
        text.setText(description);
    }

    // --Relayout Behavior----

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.IEditorPanel#relayout()
     */
    public void relayout() {
        if (!isInitialized || ignoreRelayout) {
            return;
        }

        relayoutHeading();
        relayoutContents();

        scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        mainComposite.layout();
        // layout may not redraw everything that needs it:
        mainComposite.redraw();
    }

    protected void relayoutHeading() {
    }

    protected abstract void relayoutContents();

    /**
     * 
     */
    protected void updateProperties() {
        // nameLabel.setText(getObjectName());
        String oldDescription = descriptionText.getText();
        String newDescription = getDescription();
        if (newDescription == null) {
            newDescription = "";
        }
        if (oldDescription == null) {
            oldDescription = "";
        }
        if (!newDescription.equals(oldDescription)) {
            descriptionText.setText(newDescription);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.IEditorPanel#getControlDomainObject(int,
     *      com.bluejungle.pf.domain.destiny.common.IDSpec)
     */
    public abstract CompositePredicate getControlDomainObject(int controlId, IHasId domainObject);

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.IEditorPanel#saveContents()
     */
    public void saveContents() {
        DevelopmentStatus status = DomainObjectHelper.getStatus(getDomainObject());
        // Scott's fix for locking - start
        // if (status == DevelopmentStatus.NEW || status ==
        // DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT) {
        if ((isEditable()) && ((status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT))) {
            // Scott's fix for locking - end
            PolicyServerProxy.saveEntity(getDomainObject());
        }
    }

    protected abstract EntityType getEntityType();

    /**
     * @return Returns the editable.
     */
    public boolean isEditable() {
        return editable;
    }

    protected boolean hasCustomProperties() {
        return false;
    }

    /**
     * @param editable
     *            The editable to set.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract String getObjectName();

    public abstract String getObjectTypeLabelText();

    protected String getPolicyType() {
        return null;
    }

    public IHasId getDomainObject() {
        return domainObject;
    }

    protected boolean canRelayout() {
        return isInitialized && !ignoreRelayout;
    }

    protected Class getPreviewClass() {
        return null;
    }
}
