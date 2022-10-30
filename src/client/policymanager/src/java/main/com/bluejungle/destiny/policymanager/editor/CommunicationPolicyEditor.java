/*
 * Created on May 6, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.PolicyEnum;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.controls.CompositionControl;
import com.bluejungle.destiny.policymanager.ui.controls.TimeControl;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * @author dstarke
 * 
 */
public class CommunicationPolicyEditor extends EditorPanel {

    private static final int CONTROL_ID_USERS = 0;
    private static final int CONTROL_ID_HOSTS = 1;
    private static final int CONTROL_ID_APPLICATIONS = 2;
    private static final int CONTROL_ID_ACTIONS = 3;
    private static final int CONTROL_ID_DOC_SRC = 4;
    // private static final int CONTROL_ID_DOC_TARGET = 5;
    private static final int CONTROL_ID_DATE = 6;
    private static final int CONTROL_ID_SUBJECT = 7;

    // --Positioning--------
    private static final int LABEL_COLUMN_WIDTH = 80;
    private static final int BUTTON_WIDTH = 15;
    private static final int TIME_LABEL_WIDTH = 35;

    private static final String[] DAY_LABELS = { "S", "M", "T", "W", "Th", "F", "S" };
    private static final String[] DAY_NAMES = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    private static final String[] DAY_COUNT_LABELS = { "First", "Second", "Third", "Last" };

    // --Widgets--------
    private Combo effectCombo;
    private Composite subjectComposite;
    private Label subjectSectionLabel;
    private CompositionControl userComp;
    private CompositionControl hostComp;
    private CompositionControl appComp;
    private Composite bodyComposite;
    private CompositionControl actionComp;
    private CompositionControl rsrcSrcComp;
    // private CompositionControl rsrcTgtComp;
    private CompositionControl subjectComp;
    private Composite obligationsComposite;
    private Label obligationsSectionLabel;

    private EnforcementTimeRow startTime;
    private EnforcementTimeRow endTime;
    private DailyScheduleTimeRow dailySchedule;
    private RecurringScheduleTimeRow recurringEnforcement;

    private Button buttonAdd, buttonRemove;
    private Combo comboType;

    private Label labelSite;
    private Text textSite;

    private Composite denyObligations, denyLabel, allowObligations, allowLabel;
    private Label separatorObligation, dateSectionLabel, separatorCondition;
    private Composite connectionTypeSection, enforcementDateSection, recurrentDateSection, connectionTypeLabel, enfDateLabel, recDateLabel;

    private String[] effects = { EditorMessages.POLICYEDITOR_DENY, EditorMessages.POLICYEDITOR_ALLOW, EditorMessages.POLICYEDITOR_MONITOR };

    public CommunicationPolicyEditor(Composite parent, int style, IDPolicy domainObject) {
        super(parent, style, domainObject);

        final ObjectModifiedListner objectModifiedListner = new ObjectModifiedListner();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        final IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.registerListener(objectModifiedListner, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, domainObject);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                eventManager.unregisterListener(objectModifiedListner, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, CommunicationPolicyEditor.this.domainObject);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.IEditorPanel#getControlDomainObject(int,
     *      com.bluejungle.pf.domain.destiny.common.IDSpec)
     */
    public CompositePredicate getControlDomainObject(int controlId, IHasId domainObject) {

        IDPolicy policy = (IDPolicy) domainObject;
        ITarget target = policy.getTarget();
        CompositePredicate subject;

        switch (controlId) {
        case CONTROL_ID_USERS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(0);
        case CONTROL_ID_HOSTS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(1);
        case CONTROL_ID_APPLICATIONS:
            subject = (CompositePredicate) target.getSubjectPred();
            return (CompositePredicate) subject.predicateAt(2);
        case CONTROL_ID_ACTIONS:
            return (CompositePredicate) target.getActionPred();
        case CONTROL_ID_DOC_SRC:
            return (CompositePredicate) target.getFromResourcePred();
        // case CONTROL_ID_DOC_TARGET:
        // return (CompositePredicate) target.getToResourcePred();
        case CONTROL_ID_DATE:
            return (CompositePredicate) policy.getConditions();
        case CONTROL_ID_SUBJECT:
            return (CompositePredicate) target.getToSubjectPred();
        }

        return null;
    }

    public String getObjectName() {
        String name = ((IDPolicy) domainObject).getName();
        int index = name.lastIndexOf(PQLParser.SEPARATOR);
        if (index < 0) {
            return name;
        }
        return name.substring(index + 1);
    }

    public String getDescription() {
        return ((IDPolicy) domainObject).getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#getObjectTypeLabelText()
     */
    public String getObjectTypeLabelText() {
        if (isAccess()) {
            return EditorMessages.POLICYEDITOR_ACCESS_POLICY;
        } else {
            return EditorMessages.POLICYEDITOR_USAGE_POLICY;
        }
    }

    private boolean isAccess() {
        return ((IDPolicy) getDomainObject()).hasAttribute("access");
    }

    public EntityType getEntityType() {
        return EntityType.POLICY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#initializeContents()
     */
    public void initializeContents() {
        initializeSubject();
        initializeBody();
        initializeObligations();
    }

    public void updateFromDomainObject() {
        IDPolicy policy = (IDPolicy) getDomainObject();

        if (isDisposed()) {
            return;
        }

        int index = PolicyHelpers.getIndexForEffect(policy.getMainEffect(), policy.getOtherwiseEffect());
        if (index >= 0 && index != effectCombo.getSelectionIndex()) {
            effectCombo.select(index);
        }
        startTime.setupRow();
        endTime.setupRow();
        dailySchedule.setupRow();
        recurringEnforcement.setupRow();
        updateConnectionType();
        relayout();
    }

    public int getEffect() {
        IDPolicy policy = ((IDPolicy) getDomainObject());
        IEffectType effect = policy.getMainEffect();
        IEffectType otherwise = policy.getOtherwiseEffect();
        int index = PolicyHelpers.getIndexForEffect(effect, otherwise);
        return index;
    }

    public void setComboForEffect(Combo combo) {
        int index = getEffect();
        if (index >= 0) {
            combo.select(index);
        }
    }

    public void saveEffect(int index) {
        IDPolicy policy = ((IDPolicy) getDomainObject());
        PolicyHelpers.saveEffect(policy, index);
    }

    protected void initializeSubject() {
        subjectComposite = addSectionComposite();
        FormLayout subjectLayout = new FormLayout();

        Label enforcementLabel = new Label(subjectComposite, SWT.NONE);
        enforcementLabel.setText(EditorMessages.POLICYEDITOR_ENFORCEMENT);
        enforcementLabel.setBackground(getBackground());
        enforcementLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData compData = new FormData();
        compData.left = new FormAttachment(0, 100, SPACING);
        compData.top = new FormAttachment(0, 100, SPACING);
        compData.width = LABEL_COLUMN_WIDTH;
        enforcementLabel.setLayoutData(compData);

        effectCombo = new Combo(subjectComposite, SWT.READ_ONLY);
        effectCombo.setItems(effects);
        effectCombo.setEnabled(isEditable());
        FormData comboData = new FormData();
        comboData.left = new FormAttachment(enforcementLabel, SPACING);
        comboData.top = new FormAttachment(0, 100, SPACING);
        comboData.width = LABEL_COLUMN_WIDTH - 20;
        effectCombo.setLayoutData(comboData);
        setComboForEffect(effectCombo);
        effectCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Combo c = (Combo) e.getSource();
                IDPolicy policy = ((IDPolicy) getDomainObject());
                IEffectType effect = policy.getMainEffect();
                IEffectType otherwise = policy.getOtherwiseEffect();

                int oldIndex = PolicyHelpers.getIndexForEffect(effect, otherwise);
                int newIndex = c.getSelectionIndex();
                saveEffect(newIndex);

                if (newIndex == 2) {
                    PredicateHelpers.removeConnectionType((IDPolicy) getDomainObject());
                    PredicateHelpers.removeConnectionSite((IDPolicy) getDomainObject());

                    TimePredicateAccessor accessor = new StartTimeAccessor();
                    if (accessor.hasTimePredicate()) {
                        accessor.removeTimePredicate();
                    }

                    accessor = new EndTimeAccessor();
                    if (accessor.hasTimePredicate()) {
                        accessor.removeTimePredicate();
                    }

                    accessor = new DailyScheduleAccessor();
                    if (accessor.hasTimePredicate()) {
                        accessor.removeTimePredicate();
                    }

                    accessor = new RecurringScheduleAccessor();
                    if (accessor.hasTimePredicate()) {
                        accessor.removeTimePredicate();
                    }

                    removeDenyObligation();
                }

                updateObligation();
                GlobalState.getInstance().getEditorPanel().relayout();

                PolicyUndoElement undo = new PolicyUndoElement();
                undo.setOp(PolicyUndoElementOp.CHANGE_EFFECT);
                undo.setOldValue(undo.new EffectRecord(oldIndex));
                undo.setNewValue(undo.new EffectRecord(newIndex));
                GlobalState.getInstance().addUndoElement(undo);
            }
        });

        subjectSectionLabel = new Label(subjectComposite, SWT.NONE);
        subjectSectionLabel.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_COMMUNICATION_BETWEEN_SENDER);
        subjectSectionLabel.setBackground(getBackground());
        subjectSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, 100, SPACING);
        sectionLabelData.top = new FormAttachment(effectCombo, SPACING);
        subjectSectionLabel.setLayoutData(sectionLabelData);

        Label subjectSectionSeparator = new Label(subjectComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        subjectSectionSeparator.setBackground(getBackground());
        FormData separatorData = new FormData();
        separatorData.top = new FormAttachment(subjectSectionLabel, 2);
        separatorData.left = new FormAttachment(0, 100, SPACING);
        separatorData.right = new FormAttachment(100);
        subjectSectionSeparator.setLayoutData(separatorData);

        userComp = new CompositionControl(subjectComposite, SWT.NONE, EditorMessages.POLICYEDITOR_USER_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_USERS, getDomainObject()), this, CONTROL_ID_USERS, isEditable(),
                false, SpecType.USER, ComponentEnum.USER, null);
        userComp.setBackground(getBackground());
        Composite userLabel = getPartLabel(EditorMessages.POLICYEDITOR_USER, subjectComposite, subjectSectionSeparator, userComp);
        addSectionFormData(userComp, userLabel, subjectSectionSeparator);

        hostComp = new CompositionControl(subjectComposite, SWT.NONE, EditorMessages.POLICYEDITOR_COMPUTER_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_HOSTS, getDomainObject()), this, CONTROL_ID_HOSTS, isEditable(),
                false, SpecType.HOST, ComponentEnum.HOST, null);
        hostComp.setBackground(getBackground());
        Composite desktopLabel = getPartLabel(EditorMessages.COMMUNICATIONPOLICYEDITOR_COMPUTER, subjectComposite, userLabel, hostComp);
        addSectionFormData(hostComp, desktopLabel, userComp);

        if (!isAccess()) {
            // Applications are not used in access policies
            appComp = new CompositionControl(subjectComposite, SWT.NONE, EditorMessages.POLICYEDITOR_APPLICATION_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_APPLICATIONS, getDomainObject()), this,
                    CONTROL_ID_APPLICATIONS, isEditable(), false, SpecType.APPLICATION, ComponentEnum.APPLICATION, null);
            appComp.setBackground(getBackground());
            Composite appLabel = getPartLabel(EditorMessages.COMMUNICATIONPOLICYEDITOR_APPLICATION, subjectComposite, desktopLabel, appComp);
            addSectionFormData(appComp, appLabel, hostComp);
        }

        subjectComposite.setLayout(subjectLayout);
    }

    private Composite getPartLabel(String labelName, Composite parent, Control topAttachment, Control bottomAttachment) {
        FormLayout labelLayout = new FormLayout();
        Composite labelBackground = new Composite(parent, SWT.NONE);
        labelBackground.setLayout(labelLayout);
        labelBackground.setBackground(ResourceManager.getColor("EDITOR_PART_BACKGROUD", Activator.getDefault().getPluginPreferences().getString("EDITOR_PART_BACKGROUD")));

        Label label = new Label(labelBackground, SWT.RIGHT);
        label.setText(labelName);
        label.setBackground(ResourceManager.getColor("EDITOR_PART_BACKGROUD", Activator.getDefault().getPluginPreferences().getString("EDITOR_PART_BACKGROUD")));
        label.setForeground(ColorBundle.DARK_GRAY);
        FormData labelData = new FormData();
        labelData.right = new FormAttachment(100, 100, -SPACING);
        labelData.top = new FormAttachment(0, 100, SPACING);
        label.setLayoutData(labelData);

        FormData compData = new FormData();
        compData.left = new FormAttachment(0, 100, SPACING);
        compData.top = new FormAttachment(topAttachment, SPACING);
        compData.width = LABEL_COLUMN_WIDTH;
        compData.bottom = new FormAttachment(bottomAttachment, 0, SWT.BOTTOM);
        compData.height = 100;
        labelBackground.setLayoutData(compData);

        return labelBackground;
    }

    private void addSectionFormData(Control control, Control leftAttachment, Control topAttachment) {
        FormData compData = new FormData();
        compData.left = new FormAttachment(leftAttachment, SPACING);
        compData.top = new FormAttachment(topAttachment, SPACING);
        control.setLayoutData(compData);
    }

    protected void initializeBody() {
        bodyComposite = addSectionComposite();
        FormLayout bodyLayout = new FormLayout();
        bodyComposite.setLayout(bodyLayout);

        // And Recipients
        Label label = new Label(bodyComposite, SWT.NONE);
        label.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_AND_RECIPIENTS);
        label.setBackground(getBackground());
        label.setForeground(ColorBundle.CE_MED_BLUE);
        FormData data = new FormData();
        data.left = new FormAttachment(0, 100, SPACING);
        data.top = new FormAttachment(0, 100, SPACING);
        label.setLayoutData(data);

        Label separator = new Label(bodyComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBackground(getBackground());
        data = new FormData();
        data.top = new FormAttachment(label, 2);
        data.left = new FormAttachment(0, 100, SPACING);
        data.right = new FormAttachment(100);
        separator.setLayoutData(data);

        subjectComp = new CompositionControl(bodyComposite, SWT.NONE, EditorMessages.POLICYEDITOR_USER_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_SUBJECT, getDomainObject()), this, CONTROL_ID_SUBJECT, isEditable(),
                false, SpecType.USER, ComponentEnum.USER, null);
        subjectComp.setBackground(getBackground());
        Composite subjectLabel = getPartLabel(EditorMessages.POLICYEDITOR_USER, bodyComposite, separator, subjectComp);
        addSectionFormData(subjectComp, subjectLabel, separator);

        // Using Channel
        label = new Label(bodyComposite, SWT.NONE);
        label.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_USING_CHANNEL);
        label.setBackground(getBackground());
        label.setForeground(ColorBundle.CE_MED_BLUE);
        data = new FormData();
        data.left = new FormAttachment(0, 100, SPACING);
        data.top = new FormAttachment(subjectComp, SPACING);
        label.setLayoutData(data);

        separator = new Label(bodyComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBackground(getBackground());
        data = new FormData();
        data.top = new FormAttachment(label, 2);
        data.left = new FormAttachment(0, 100, SPACING);
        data.right = new FormAttachment(100);
        separator.setLayoutData(data);

        actionComp = new CompositionControl(bodyComposite, SWT.NONE, EditorMessages.POLICYEDITOR_ACTION_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_ACTIONS, getDomainObject()), this, CONTROL_ID_ACTIONS, isEditable(),
                false, true, new String[] {}, SpecType.ACTION, ComponentEnum.ACTION, null);
        actionComp.setBackground(getBackground());
        Composite actionLabel = getPartLabel(EditorMessages.POLICYEDITOR_ACTION, bodyComposite, separator, actionComp);
        addSectionFormData(actionComp, actionLabel, separator);

        // With Attachement
        label = new Label(bodyComposite, SWT.NONE);
        label.setText(EditorMessages.COMMUNICATIONPOLICYEDITOR_WITH_ATTACHMENTS);
        label.setBackground(getBackground());
        label.setForeground(ColorBundle.CE_MED_BLUE);
        data = new FormData();
        data.left = new FormAttachment(0, 100, SPACING);
        data.top = new FormAttachment(actionComp, SPACING);
        label.setLayoutData(data);

        separator = new Label(bodyComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBackground(getBackground());
        data = new FormData();
        data.top = new FormAttachment(label, 2);
        data.left = new FormAttachment(0, 100, SPACING);
        data.right = new FormAttachment(100);
        separator.setLayoutData(data);

        rsrcSrcComp = new CompositionControl(bodyComposite, SWT.NONE, EditorMessages.POLICYEDITOR_RESOURCE_COMPONENT, "" /* getObjectTypeLabelText() */, getControlDomainObject(CONTROL_ID_DOC_SRC, getDomainObject()), this, CONTROL_ID_DOC_SRC,
                isEditable(), false, SpecType.RESOURCE, ComponentEnum.RESOURCE, null);
        rsrcSrcComp.setBackground(getBackground());
        Composite resourceLabel = getPartLabel(EditorMessages.COMMUNICATIONPOLICYEDITOR_DOCUMENT, bodyComposite, separator, rsrcSrcComp);
        addSectionFormData(rsrcSrcComp, resourceLabel, separator);

        // Conditions
        dateSectionLabel = new Label(bodyComposite, SWT.NONE);
        dateSectionLabel.setText(EditorMessages.POLICYEDITOR_CONDITIONS);
        dateSectionLabel.setBackground(getBackground());
        dateSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        data = new FormData();
        data.left = new FormAttachment(0, 100, SPACING);
        data.top = new FormAttachment(rsrcSrcComp, SPACING);
        dateSectionLabel.setLayoutData(data);

        separatorCondition = new Label(bodyComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separatorCondition.setBackground(getBackground());
        data = new FormData();
        data.top = new FormAttachment(dateSectionLabel, 2);
        data.left = new FormAttachment(0, 100, SPACING);
        data.right = new FormAttachment(100);
        separatorCondition.setLayoutData(data);

        connectionTypeSection = new Composite(bodyComposite, SWT.NONE);
        connectionTypeLabel = getPartLabel(EditorMessages.POLICYEDITOR_CONNECTION_TYPE, bodyComposite, separatorCondition, connectionTypeSection);
        addSectionFormData(connectionTypeSection, connectionTypeLabel, separatorCondition);

        initializeConnectionTypeSection(connectionTypeSection);

        enforcementDateSection = new Composite(bodyComposite, SWT.NONE);
        enfDateLabel = getPartLabel(EditorMessages.POLICYEDITOR_DATETIME, bodyComposite, connectionTypeLabel, enforcementDateSection);
        addSectionFormData(enforcementDateSection, enfDateLabel, connectionTypeLabel);

        initializeEnforcementDateSection(enforcementDateSection);

        recurrentDateSection = new Composite(bodyComposite, SWT.NONE);
        recDateLabel = getPartLabel(EditorMessages.POLICYEDITOR_RECURRENCE, bodyComposite, enfDateLabel, recurrentDateSection);
        addSectionFormData(recurrentDateSection, recDateLabel, enfDateLabel);

        initializeRecurrentDateSection(recurrentDateSection);
    }

    private void initializeConnectionTypeSection(Composite section) {
        Color background = getBackground();
        section.setBackground(background);

        FormLayout layout = new FormLayout();
        section.setLayout(layout);

        buttonRemove = new Button(section, SWT.FLAT);
        buttonRemove.setText(EditorMessages.POLICYEDITOR_MINUS);
        buttonRemove.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        buttonRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // get the old value
                labelSite.setVisible(false);
                textSite.setVisible(false);
                textSite.setText("");

                Relation rel = (Relation) PredicateHelpers.getConnectionType(((IDPolicy) getDomainObject()).getConditions());
                Long oldValue = null;
                if (rel != null) {
                    oldValue = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                }
                // remove the old value
                PredicateHelpers.removeConnectionType((IDPolicy) getDomainObject());
                PredicateHelpers.removeConnectionSite((IDPolicy) getDomainObject());
                // create undo info
                PolicyUndoElement undo = new PolicyUndoElement();
                undo.setOp(PolicyUndoElementOp.CHANGE_CONNECTION_TYPE);
                undo.setOldValue(oldValue);
                undo.setNewValue(null);
                GlobalState.getInstance().addUndoElement(undo);

                updateConnectionType();
            }
        });

        buttonAdd = new Button(section, SWT.FLAT);
        buttonAdd.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        buttonAdd.setText(EditorMessages.POLICYEDITOR_PLUS);
        buttonAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonRemove.setVisible(true);
                comboType.setVisible(true);
                comboType.select(1);
                // labelSite.setVisible(true);
                // textSite.setVisible(true);
                labelSite.setVisible(false);
                textSite.setVisible(false);
                textSite.setText("");
                comboType.setFocus();
                buttonAdd.setVisible(false);
                addConnectionType();

                PolicyUndoElement undo = new PolicyUndoElement();
                undo.setOp(PolicyUndoElementOp.CHANGE_CONNECTION_TYPE);
                undo.setOldValue(null);
                undo.setNewValue(Long.valueOf(1));
                GlobalState.getInstance().addUndoElement(undo);
            }
        });

        FormData data = new FormData();
        data.left = new FormAttachment(buttonRemove, SPACING);
        data.top = new FormAttachment(0, SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        buttonAdd.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, SPACING);
        data.top = new FormAttachment(0, SPACING);
        data.width = BUTTON_WIDTH;
        data.height = BUTTON_WIDTH;
        buttonRemove.setLayoutData(data);

        comboType = new Combo(section, SWT.READ_ONLY);
        comboType.setEnabled(isEditable());
        comboType.add(EditorMessages.POLICYEDITOR_LOCAL);
        comboType.add(EditorMessages.POLICYEDITOR_REMOTE);
        comboType.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = comboType.getSelectionIndex();
                if (index == 1) {
                    // labelSite.setVisible(true);
                    // textSite.setVisible(true);
                    labelSite.setVisible(false);
                    textSite.setVisible(false);
                } else {
                    labelSite.setVisible(false);
                    textSite.setVisible(false);
                }
                addConnectionType();
            }
        });

        labelSite = new Label(section, SWT.NONE);
        labelSite.setVisible(false);
        labelSite.setBackground(background);
        labelSite.setText(EditorMessages.POLICYEDITOR_SITE);
        data = new FormData();
        data.left = new FormAttachment(0, SPACING);
        data.top = new FormAttachment(comboType, SPACING);
        labelSite.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(labelSite, SPACING);
        data.top = new FormAttachment(0, SPACING);
        data.width = 100;
        comboType.setLayoutData(data);

        textSite = new Text(section, SWT.BORDER);
        textSite.setVisible(false);
        data = new FormData();
        data.left = new FormAttachment(labelSite, SPACING);
        data.top = new FormAttachment(comboType, SPACING);
        data.right = new FormAttachment(comboType, 0, SWT.RIGHT);
        textSite.setLayoutData(data);
        textSite.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                addConnectionType();
            }
        });

        updateConnectionType();
    }

    private void addConnectionType() {
        int index = comboType.getSelectionIndex();
        IExpression exp = Constant.build(index);
        PredicateHelpers.setConnectionType((IDPolicy) getDomainObject(), exp);
        if (index == 1) {
            String text = textSite.getText();
            if (text.trim().length() == 0) {
                PredicateHelpers.removeConnectionSite((IDPolicy) getDomainObject());
            } else {
                IExpression exp1 = Constant.build(text);
                PredicateHelpers.setConnectionSite((IDPolicy) getDomainObject(), exp1);
            }
        } else {
            PredicateHelpers.removeConnectionSite((IDPolicy) getDomainObject());
        }
    }

    private void updateConnectionType() {
        Relation rel = (Relation) PredicateHelpers.getConnectionType(((IDPolicy) getDomainObject()).getConditions());
        Long val = null;
        if (rel != null) {
            val = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
        }

        if (val == null) {
            buttonAdd.setVisible(true && isEditable());
            buttonRemove.setVisible(false);
            comboType.setVisible(false);
            labelSite.setVisible(false);
            textSite.setVisible(false);
        } else {
            buttonAdd.setVisible(false);
            buttonRemove.setVisible(true && isEditable());
            comboType.setVisible(true);
            int index = val.intValue();
            comboType.select(index);
            if (index == 1) {
                // labelSite.setVisible(true);
                labelSite.setVisible(false);
                labelSite.setEnabled(isEditable());
                // textSite.setVisible(true);
                textSite.setVisible(false);
                textSite.setEditable(isEditable());
                textSite.setEnabled(isEditable());

                Relation rel1 = (Relation) PredicateHelpers.getConnectionSite(((IDPolicy) getDomainObject()).getConditions());
                String val1 = "";
                if (rel1 != null) {
                    val1 = (String) ((IRelation) rel1).getRHS().evaluate(null).getValue();
                }
                textSite.setText(val1);
            } else {
                labelSite.setVisible(false);
                textSite.setVisible(false);
            }
        }
    }

    private void initializeEnforcementDateSection(Composite dateSection) {
        Color background = getBackground();
        dateSection.setBackground(background);

        dateSection.setLayout(new RowLayout(SWT.VERTICAL));

        startTime = new EnforcementTimeRow(dateSection, EditorMessages.POLICYEDITOR_START, new StartTimeAccessor(), PolicyUndoElementOp.CHANGE_START_DATE);
        startTime.setupRow();
        endTime = new EnforcementTimeRow(dateSection, EditorMessages.POLICYEDITOR_END, new EndTimeAccessor(), PolicyUndoElementOp.CHANGE_END_DATE);
        endTime.setupRow();
    }

    private void initializeRecurrentDateSection(Composite dateSection) {
        Color background = getBackground();
        dateSection.setBackground(background);

        dateSection.setLayout(new RowLayout(SWT.VERTICAL));

        dailySchedule = new DailyScheduleTimeRow(dateSection, EditorMessages.POLICYEDITOR_TIME);
        dailySchedule.setupRow();
        recurringEnforcement = new RecurringScheduleTimeRow(dateSection, EditorMessages.POLICYEDITOR_DAY);
        recurringEnforcement.setupRow();
    }

    protected void initializeObligations() {
        obligationsComposite = addSectionComposite();
        FormLayout oblLayout = new FormLayout();
        obligationsComposite.setLayout(oblLayout);

        obligationsSectionLabel = new Label(obligationsComposite, SWT.NONE);
        obligationsSectionLabel.setText(EditorMessages.POLICYEDITOR_OBLIGATIONS);
        obligationsSectionLabel.setBackground(getBackground());
        obligationsSectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
        FormData sectionLabelData = new FormData();
        sectionLabelData.left = new FormAttachment(0, 100, SPACING);
        sectionLabelData.top = new FormAttachment(0, 100, SPACING);
        obligationsSectionLabel.setLayoutData(sectionLabelData);

        separatorObligation = new Label(obligationsComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separatorObligation.setBackground(getBackground());
        FormData separatorData = new FormData();
        separatorData.top = new FormAttachment(obligationsSectionLabel, 2);
        separatorData.left = new FormAttachment(0, 100, SPACING);
        separatorData.right = new FormAttachment(100);
        separatorObligation.setLayoutData(separatorData);

        denyObligations = new Composite(obligationsComposite, SWT.NONE);
        denyLabel = getPartLabel(EditorMessages.POLICYEDITOR_ON_DENY, obligationsComposite, separatorObligation, denyObligations);
        addSectionFormData(denyObligations, denyLabel, separatorObligation);

        initializeOneObligation(denyObligations, EffectType.DENY);

        allowObligations = new Composite(obligationsComposite, SWT.NONE);
        allowLabel = getPartLabel(EditorMessages.POLICYEDITOR_ON_ALLOW, obligationsComposite, denyLabel, allowObligations);
        addSectionFormData(allowObligations, allowLabel, denyLabel);

        initializeOneObligation(allowObligations, EffectType.ALLOW);

        updateObligation();
    }

    private void initializeOneObligation(Composite container, final IDEffectType type) {
        Color background = getBackground();
        container.setBackground(background);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);

        LogObligationEditor editor = new LogObligationEditor(container, (IDPolicy) getDomainObject(), type, isEditable());
        editor.setBackground(background);

        SendMessageObligationEditor editor1 = new SendMessageObligationEditor(container, (IDPolicy) getDomainObject(), type, isEditable());
        editor1.setBackground(background);

        SendEmailObligationEditor editor2 = new SendEmailObligationEditor(container, (IDPolicy) getDomainObject(), type, isEditable());
        editor2.setBackground(background);

        CustomObligationEditor editor3 = new CustomObligationEditor(container, (IDPolicy) getDomainObject(), type, isEditable());
        editor3.setBackground(background);
    }

    private void removeDenyObligation() {
        IDPolicy policy = (IDPolicy) getDomainObject();
        IDEffectType effectType = EffectType.DENY;
        Collection<IObligation> obligations = policy.getObligations(effectType);

        List<IObligation> obligationsToBeDeleted = new ArrayList<IObligation>();
        List<IObligation> obligationsToReturn = new ArrayList<IObligation>();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(DisplayObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }
        obligationsToReturn.clear();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(NotifyObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }
        obligationsToReturn.clear();
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(CustomObligation.OBLIGATION_NAME)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        if (!obligationsToReturn.isEmpty()) {
            obligationsToBeDeleted.add(obligationsToReturn.get(0));
        }

        for (IObligation nextObligation : obligationsToBeDeleted) {
            policy.deleteObligation(nextObligation, effectType);
        }
    }

    public void relayout() {
        if (!canRelayout()) {
            return;
        }

        super.relayout();
        subjectComposite.redraw();
        bodyComposite.redraw();
        Control[] controls = bodyComposite.getChildren();
        for (Control control : controls) {
            control.redraw();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#relayoutContents()
     */
    protected void relayoutContents() {
        userComp.relayout();
        hostComp.relayout();
        if (!isAccess()) {
            appComp.relayout();
        }
        actionComp.relayout();
        rsrcSrcComp.relayout();
        // rsrcTgtComp.relayout();
        subjectComp.relayout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        ((IDPolicy) domainObject).setDescription(description);
    }

    protected boolean showApplicationSection() {
        return true;
    }

    protected boolean showUsageActions() {
        return true;
    }

    /**
     * A class for abstracting access of Time information from the domain
     * object. This class is used with TimeRows which implement the control
     * logic for UI widgets in the Date and Time section.
     */
    private static abstract class TimePredicateAccessor {

        public abstract IPredicate getTimePredicate();

        public abstract boolean hasTimePredicate();

        public abstract void addNewTimePredicate();

        public abstract void removeTimePredicate();

        public abstract void modifyTime(Object timeInfo);
    }

    /**
     * Helper class that represents a row in the "Date and Time" section. This
     * abstract class handles the wrapper mechanics that all rows in this
     * section share.
     */
    private abstract class TimeRow extends Composite {

        public TimePredicateAccessor accessor;
        public String labelString;
        public Label labelComp;
        public Button removeButton;
        public Button addButton;
        public Composite contents;

        public TimeRow(Composite parent, String label, TimePredicateAccessor accessor) {
            super(parent, SWT.NONE);
            this.labelString = label;
            this.accessor = accessor;
            setLayout(new FormLayout());
            setBackground(parent.getBackground());
            addLabel();
        }

        /**
         * Initializes or refreshes the row.
         */
        public void setupRow() {
            if (!accessor.hasTimePredicate()) {
                if (isEditable() && addButton == null) {
                    addAddButton();
                }
                if (removeButton != null) {
                    removeButton.dispose();
                    removeButton = null;
                }
                if (contents != null) {
                    contents.dispose();
                    contents = null;
                }
            } else {
                if (isEditable() && removeButton == null) {
                    addRemoveButton();
                }
                if (contents == null) {
                    addContents();
                }
                if (addButton != null) {
                    addButton.dispose();
                    addButton = null;
                }
            }
            if (contents != null) {
                setStateFromDomainObject();
            }
        }

        public void addAddButton() {
            addButton = new Button(this, SWT.FLAT);
            addButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            addButton.setText(EditorMessages.POLICYEDITOR_PLUS);
            addButton.setToolTipText(EditorMessages.POLICYEDITOR_ADD_CONDITION);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 2 * SPACING + TIME_LABEL_WIDTH);
            data.top = new FormAttachment(0, SPACING);
            data.width = BUTTON_WIDTH;
            data.height = BUTTON_WIDTH;

            addButton.setLayoutData(data);

            addButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    accessor.addNewTimePredicate();
                    setupRow();
                    TimeRow.this.layout();
                    GlobalState.getInstance().getEditorPanel().relayout();
                }
            });
        }

        public void addRemoveButton() {
            removeButton = new Button(this, SWT.FLAT);
            removeButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
            removeButton.setText(EditorMessages.POLICYEDITOR_MINUS);
            removeButton.setToolTipText(EditorMessages.POLICYEDITOR_REMOVE_CONDITION);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 2 * SPACING + TIME_LABEL_WIDTH);
            data.top = new FormAttachment(0, SPACING);
            data.width = BUTTON_WIDTH;
            data.height = BUTTON_WIDTH;

            removeButton.setLayoutData(data);

            removeButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    accessor.removeTimePredicate();
                    setupRow();
                    TimeRow.this.layout();
                    GlobalState.getInstance().getEditorPanel().relayout();
                }
            });
        }

        public void addLabel() {
            labelComp = new Label(this, SWT.NONE);
            labelComp.setText(labelString);
            labelComp.setEnabled(isEditable());
            labelComp.setBackground(getParent().getBackground());
            FormData data = new FormData();
            data.left = new FormAttachment(0);
            data.top = new FormAttachment(0, SPACING + 1);
            labelComp.setLayoutData(data);
        }

        public void addContents() {
            contents = new Composite(this, SWT.NONE);
            contents.setBackground(getParent().getBackground());
            populateContents(contents);
            FormData data = new FormData();
            data.left = new FormAttachment(0, TIME_LABEL_WIDTH + BUTTON_WIDTH + 3 * SPACING);
            data.right = new FormAttachment(100);
            contents.setLayoutData(data);
        }

        /**
         * Subclasses should implement this to populate the contents composite.
         * This method is called whenever a new contents composite is created.
         * 
         * @param contents
         */
        public abstract void populateContents(Composite contents);

        /**
         * Subclasses should implement this to populate information from the
         * domain object into this control. This method will be called whenever
         * The object needs to be refreshed. It will not perform any necessary
         * setup, however, so if you want to refresh the row, you should call
         * SetupRow instead.
         */
        public abstract void setStateFromDomainObject();
    }

    private class EnforcementTimeRow extends TimeRow {

        private DatePickerCombo dateControl;
        private TimeControl timeControl;
        private PolicyUndoElementOp modifyUndoOp;

        public EnforcementTimeRow(Composite parent, String label, TimePredicateAccessor accessor, PolicyUndoElementOp modifyUndoOp) {
            super(parent, label, accessor);
            this.modifyUndoOp = modifyUndoOp;
        }

        public void populateContents(Composite contents) {
            RowLayout layout = new RowLayout();
            contents.setLayout(layout);
            dateControl = new DatePickerCombo(contents, SWT.BORDER);
            timeControl = new TimeControl(contents, SWT.BORDER);

            dateControl.setEnabled(isEditable());
            timeControl.setEnabled(isEditable());

            dateControl.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    Date controlDate = dateControl.getDate();
                    Calendar date = new GregorianCalendar();
                    date.setTimeInMillis(controlDate.getTime());
                    date.set(Calendar.HOUR_OF_DAY, timeControl.getHours());
                    date.set(Calendar.MINUTE, timeControl.getMinutes());
                    Relation rel = (Relation) accessor.getTimePredicate();
                    if (rel != null) {
                        Long dateVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(dateVal.longValue());
                        if (!date.equals(oldDate)) {
                            accessor.modifyTime(date.getTime());
                            createUndoModify(oldDate.getTime(), date.getTime());
                        }
                    }
                }
            });

            timeControl.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    Date controlDate = dateControl.getDate();
                    Calendar date = new GregorianCalendar();
                    date.setTimeInMillis(controlDate.getTime());
                    date.set(Calendar.HOUR_OF_DAY, timeControl.getHours());
                    date.set(Calendar.MINUTE, timeControl.getMinutes());
                    Relation rel = (Relation) accessor.getTimePredicate();
                    if (rel != null) {
                        Long dateVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(dateVal.longValue());
                        if (!date.equals(oldDate)) {
                            accessor.modifyTime(date.getTime());
                            createUndoModify(oldDate.getTime(), date.getTime());
                        }
                    }
                }
            });
        }

        public void setStateFromDomainObject() {
            IPredicate pred = accessor.getTimePredicate();
            Long dateVal = (Long) ((IRelation) pred).getRHS().evaluate(null).getValue();
            Calendar date = new GregorianCalendar();
            date.setTimeInMillis(dateVal.longValue());
            Date oldDate = dateControl.getDate();
            Calendar oldCalendar = new GregorianCalendar();
            if (oldDate != null) {
                oldCalendar.setTime(oldDate);
            } else {
                oldCalendar = null;
            }

            if (dateControl != null
                    && (oldCalendar == null || oldCalendar.get(Calendar.YEAR) != date.get(Calendar.YEAR) || oldCalendar.get(Calendar.MONTH) != date.get(Calendar.MONTH) || oldCalendar.get(Calendar.DAY_OF_MONTH) != date.get(Calendar.DAY_OF_MONTH))) {
                dateControl.setDate(date.getTime());
            }
            if (timeControl != null && (timeControl.getHours() != date.get(Calendar.HOUR_OF_DAY) || timeControl.getMinutes() != date.get(Calendar.MINUTE))) {
                timeControl.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
            }
        }

        public void createUndoModify(Date oldDate, Date newDate) {
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(modifyUndoOp);
            undo.setOldValue(oldDate);
            undo.setNewValue(newDate);
            // GlobalState.getInstance().addUndoElement(undo);
        }
    }

    private class StartTimeAccessor extends TimePredicateAccessor {

        public IPredicate getTimePredicate() {
            return PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions());
        }

        public boolean hasTimePredicate() {
            return PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions()) != null;
        }

        public void addNewTimePredicate() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.SECOND, 0);
            String dateString = DateFormat.getDateTimeInstance().format(calendar.getTime());
            IExpression exp = TimeAttribute.IDENTITY.build(dateString);
            // set the value
            PredicateHelpers.setStartTime((IDPolicy) getDomainObject(), exp);
            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_START_DATE);
            undo.setOldValue(null);
            undo.setNewValue(calendar.getTime());
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void removeTimePredicate() {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }
            // remove the old value
            PredicateHelpers.removeStartTime((IDPolicy) getDomainObject());
            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_START_DATE);
            undo.setOldValue(oldDate);
            undo.setNewValue(null);
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void modifyTime(Object timeInfo) {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            Date newDate = (Date) timeInfo;
            if (!newDate.equals(oldDate)) {
                // set the new value
                String dateString = DateFormat.getDateTimeInstance().format(newDate);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setStartTime((IDPolicy) getDomainObject(), exp);
            }
        }
    }

    private class EndTimeAccessor extends TimePredicateAccessor {

        public IPredicate getTimePredicate() {
            return PredicateHelpers.getEndTime(((IDPolicy) getDomainObject()).getConditions());
        }

        public boolean hasTimePredicate() {
            return PredicateHelpers.getEndTime(((IDPolicy) getDomainObject()).getConditions()) != null;
        }

        public void addNewTimePredicate() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.SECOND, 0);
            String dateString = DateFormat.getDateTimeInstance().format(calendar.getTime());
            IExpression exp = TimeAttribute.IDENTITY.build(dateString);
            // set the new value
            PredicateHelpers.setEndTime((IDPolicy) getDomainObject(), exp);
            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_END_DATE);
            undo.setOldValue(null);
            undo.setNewValue(calendar.getTime());
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void removeTimePredicate() {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            // remove the old value
            PredicateHelpers.removeEndTime((IDPolicy) getDomainObject());

            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_END_DATE);
            undo.setOldValue(oldDate);
            undo.setNewValue(null);
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void modifyTime(Object timeInfo) {
            // get the old value
            Relation rel = (Relation) PredicateHelpers.getStartTime(((IDPolicy) getDomainObject()).getConditions());
            Date oldDate = null;
            if (rel != null) {
                Long dateVal = (Long) ((IRelation) rel).getRHS().evaluate(null).getValue();
                oldDate = new Date(dateVal.longValue());
            }

            Date newDate = (Date) timeInfo;
            if (!newDate.equals(oldDate)) {
                // set the new value
                String dateString = DateFormat.getDateTimeInstance().format(newDate);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setEndTime((IDPolicy) getDomainObject(), exp);
            }
        }
    }

    private class DailyScheduleTimeRow extends TimeRow {

        private TimeControl from;
        private TimeControl to;

        public DailyScheduleTimeRow(Composite parent, String label) {
            super(parent, label, new DailyScheduleAccessor());
        }

        public void populateContents(Composite contents) {
            FormLayout layout = new FormLayout();
            contents.setLayout(layout);
            Label fromLabel = new Label(contents, SWT.NONE);
            fromLabel.setText(EditorMessages.POLICYEDITOR_FROM);
            fromLabel.setBackground(getBackground());
            FormData fromData = new FormData();
            fromData.left = new FormAttachment(0, 2 * SPACING);
            fromData.top = new FormAttachment(0, SPACING + 1);
            fromLabel.setLayoutData(fromData);

            from = new TimeControl(contents, SWT.BORDER);
            FormData fcData = new FormData();
            fcData.left = new FormAttachment(fromLabel);
            fcData.top = new FormAttachment(fromLabel, 0, SWT.CENTER);
            from.setLayoutData(fcData);
            from.setEnabled(isEditable());

            from.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    int hours = from.getHours();
                    int minutes = from.getMinutes();

                    Relation rel = (Relation) ((CompositePredicate) accessor.getTimePredicate()).predicateAt(0);
                    if (rel != null) {
                        Long oldVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(oldVal.longValue());

                        if (oldDate.get(Calendar.HOUR_OF_DAY) != hours || oldDate.get(Calendar.MINUTE) != minutes) {
                            Calendar fd = new GregorianCalendar();
                            fd.set(Calendar.YEAR, 0);
                            fd.set(Calendar.MONTH, 0);
                            fd.set(Calendar.DAY_OF_MONTH, 0);
                            fd.set(Calendar.HOUR_OF_DAY, hours);
                            fd.set(Calendar.MINUTE, minutes);

                            Date td = null;
                            accessor.modifyTime(new Date[] { fd.getTime(), td });
                        }
                    }
                }
            });

            Label toLabel = new Label(contents, SWT.NONE);
            toLabel.setText(EditorMessages.POLICYEDITOR_TO);
            toLabel.setBackground(getBackground());
            FormData tlData = new FormData();
            tlData.left = new FormAttachment(from, SPACING);
            tlData.top = new FormAttachment(0, SPACING + 1);
            toLabel.setLayoutData(tlData);

            to = new TimeControl(contents, SWT.BORDER);
            FormData tcData = new FormData();
            tcData.left = new FormAttachment(toLabel);
            tcData.top = new FormAttachment(toLabel, 0, SWT.CENTER);
            to.setLayoutData(tcData);
            to.setEnabled(isEditable());

            to.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    int hours = to.getHours();
                    int minutes = to.getMinutes();

                    Relation rel = (Relation) ((CompositePredicate) accessor.getTimePredicate()).predicateAt(1);
                    if (rel != null) {
                        Long oldVal = (Long) rel.getRHS().evaluate(null).getValue();
                        Calendar oldDate = new GregorianCalendar();
                        oldDate.setTimeInMillis(oldVal.longValue());

                        if (oldDate.get(Calendar.HOUR_OF_DAY) != hours || oldDate.get(Calendar.MINUTE) != minutes) {
                            Date fd = null;
                            Calendar td = new GregorianCalendar();
                            td.set(Calendar.YEAR, 0);
                            td.set(Calendar.MONTH, 0);
                            td.set(Calendar.DAY_OF_MONTH, 0);
                            td.set(Calendar.HOUR_OF_DAY, hours);
                            td.set(Calendar.MINUTE, minutes);
                            accessor.modifyTime(new Date[] { fd, td.getTime() });
                        }
                    }
                }
            });
        }

        public void setStateFromDomainObject() {
            // The Time attribute uses the local time. This code parses the
            // representation instead of relying on the Long value stored
            // in the internal representation to avoid correcting for time zone
            // twice.
            try {
                IPredicate pred = accessor.getTimePredicate();
                Relation fromRel = (Relation) ((CompositePredicate) pred).predicateAt(0);
                Constant fromConst = (Constant) fromRel.getRHS();
                Calendar fromDate = new GregorianCalendar();
                fromDate.setTime(DateFormat.getTimeInstance().parse(unquote(fromConst.getRepresentation())));

                if (fromDate.get(Calendar.HOUR_OF_DAY) != from.getHours() || fromDate.get(Calendar.MINUTE) != from.getMinutes()) {
                    from.setTime(fromDate.get(Calendar.HOUR_OF_DAY), fromDate.get(Calendar.MINUTE));
                }
                Relation toRel = (Relation) ((CompositePredicate) pred).predicateAt(1);
                Constant toConst = (Constant) toRel.getRHS();
                Calendar toDate = new GregorianCalendar();
                toDate.setTime(DateFormat.getTimeInstance().parse(unquote(toConst.getRepresentation())));
                if (toDate.get(Calendar.HOUR_OF_DAY) != to.getHours() || toDate.get(Calendar.MINUTE) != to.getMinutes()) {
                    to.setTime(toDate.get(Calendar.HOUR_OF_DAY), toDate.get(Calendar.MINUTE));
                }
            } catch (ParseException pe) {
                // This will not happen because PQL parsed this successfully
            }
        }

        private String unquote(String s) {
            if (s == null) {
                return null;
            }
            if (s.length() < 2 || s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                return s;
            } else {
                return s.substring(1, s.length() - 1);
            }
        }
    }

    private class DailyScheduleAccessor extends TimePredicateAccessor {

        public IPredicate getTimePredicate() {
            IPredicate condition = ((IDPolicy) getDomainObject()).getConditions();
            IPredicate from = PredicateHelpers.getDailyFromTime(condition);
            IPredicate to = PredicateHelpers.getDailyToTime(condition);

            List<IPredicate> parts = new ArrayList<IPredicate>();
            parts.add(from);
            parts.add(to);
            return new CompositePredicate(BooleanOp.AND, parts);
        }

        public boolean hasTimePredicate() {
            IPredicate condition = ((IDPolicy) getDomainObject()).getConditions();
            IPredicate from = PredicateHelpers.getDailyFromTime(condition);
            IPredicate to = PredicateHelpers.getDailyToTime(condition);
            return (from != null && to != null);
        }

        public void addNewTimePredicate() {
            Calendar d = new GregorianCalendar();
            d.set(Calendar.SECOND, 0);
            String timeString = DateFormat.getTimeInstance().format(d.getTime());
            IExpression fromExp = TimeAttribute.TIME.build(timeString);
            IExpression toExp = TimeAttribute.TIME.build(timeString);
            IDPolicy policy = (IDPolicy) getDomainObject();
            PredicateHelpers.setDailyFromTime(policy, fromExp);
            PredicateHelpers.setDailyToTime(policy, toExp);

            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE);
            undo.setOldValue(null);
            undo.setNewValue(new IExpression[] { fromExp, toExp });
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void removeTimePredicate() {
            IDPolicy policy = (IDPolicy) getDomainObject();
            Relation from = (Relation) PredicateHelpers.getDailyFromTime(policy.getConditions());
            Relation to = (Relation) PredicateHelpers.getDailyToTime(policy.getConditions());

            PredicateHelpers.removeDailyFromTime(policy);
            PredicateHelpers.removeDailyToTime(policy);

            // create undo info
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE);
            undo.setOldValue(new IExpression[] { from.getRHS(), to.getRHS() });
            undo.setNewValue(null);
            GlobalState.getInstance().addUndoElement(undo);
        }

        public void modifyTime(Object timeInfo) {
            // get the old values
            IDPolicy policy = (IDPolicy) getDomainObject();
            Relation from = (Relation) PredicateHelpers.getDailyFromTime(policy.getConditions());
            Relation to = (Relation) PredicateHelpers.getDailyToTime(policy.getConditions());
            IExpression oldFrom = null;
            IExpression oldTo = null;
            if (from != null) {
                oldFrom = from.getRHS();
            }
            if (to != null) {
                oldTo = to.getRHS();
            }

            // prepare new values to set if we have them
            DateFormat format = DateFormat.getTimeInstance();
            Date[] times = (Date[]) timeInfo;
            IExpression fromExp = null;
            if (times[0] != null) {
                String fromString = format.format(times[0]);
                fromExp = TimeAttribute.TIME.build(fromString);
            }
            IExpression toExp = null;
            if (times[1] != null) {
                String toString = format.format(times[1]);
                toExp = TimeAttribute.TIME.build(toString);
            }

            if ((times[0] != null && !fromExp.equals(oldFrom)) || (times[1] != null && !toExp.equals(oldTo))) {
                PolicyUndoElement undo = new PolicyUndoElement();
                // set the new values we have
                if (times[0] != null) {
                    PredicateHelpers.setDailyFromTime(policy, fromExp);
                    undo.setOldValue(oldFrom);
                    undo.setNewValue(fromExp);
                    undo.setOp(PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_FROM);
                }
                if (times[1] != null) {
                    PredicateHelpers.setDailyToTime(policy, toExp);
                    undo.setOldValue(oldTo);
                    undo.setNewValue(toExp);
                    undo.setOp(PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_TO);
                }
                // GlobalState.getInstance().addUndoElement(undo);
            }

        }
    }

    private class RecurringScheduleTimeRow extends TimeRow {

        private Button weekRadio;
        private Button dayNumRadio;
        private Button dowimRadio;
        private Combo dayNum;
        private Combo dayCountCombo;
        private Combo daysCombo;
        private List<Button> weekdayButtonList;

        public RecurringScheduleTimeRow(Composite parent, String label) {
            super(parent, label, new RecurringScheduleAccessor());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimeRow#populateContents(org.eclipse.swt.widgets.Composite)
         */
        public void populateContents(Composite contents) {
            Color background = getBackground();
            contents.setLayout(new FillLayout());

            Group group = new Group(contents, SWT.NONE);
            group.setBackground(background);

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            group.setLayout(layout);

            weekRadio = new Button(group, SWT.RADIO);
            weekRadio.setText("");
            weekRadio.setBackground(background);
            weekRadio.setEnabled(isEditable());
            weekRadio.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    if (weekRadio.getSelection()) {
                        IDPolicy policy = (IDPolicy) getDomainObject();
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new weekday predicate
                        ((RecurringScheduleAccessor) accessor).addNewWeekdayPredicate();
                        // save undo information
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE, oldValue, accessor.getTimePredicate());
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite weekPanel = new Composite(group, SWT.NONE);

            dayNumRadio = new Button(group, SWT.RADIO);
            dayNumRadio.setText("");
            dayNumRadio.setBackground(background);
            dayNumRadio.setEnabled(isEditable());
            dayNumRadio.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    if (dayNumRadio.getSelection()) {
                        IDPolicy policy = (IDPolicy) getDomainObject();
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new day number predicate
                        String dayNumText = dayNum.getText();
                        if ("".equals(dayNumText)) {
                            dayNumText = "1";
                        }
                        Relation rel = new Relation(RelationOp.EQUALS, TimeAttribute.DATE, TimeAttribute.DATE.build(dayNumText));
                        PredicateHelpers.addPredicateToConditions(policy, rel);
                        // add undo info
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE, oldValue, accessor.getTimePredicate());
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite dayPanel = new Composite(group, SWT.NONE);
            dayPanel.setBackground(background);

            dowimRadio = new Button(group, SWT.RADIO);
            dowimRadio.setText("");
            dowimRadio.setBackground(background);
            dowimRadio.setEnabled(isEditable());
            dowimRadio.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    if (dowimRadio.getSelection()) {
                        IDPolicy policy = (IDPolicy) getDomainObject();
                        // get the old time predicate
                        IPredicate oldValue = accessor.getTimePredicate();
                        // remove it
                        if (oldValue != null) {
                            PredicateHelpers.removeDayOfMonthPredicate(policy);
                            PredicateHelpers.removeWeekdayPredicate(policy);
                            PredicateHelpers.removeDOWIMPredicate(policy);
                        }
                        // add a new dowim predicate
                        int count = dayCountCombo.getSelectionIndex();
                        if (count == -1) {
                            count = 1;
                        } else if (count == DAY_COUNT_LABELS.length - 1) {
                            count = -1;
                        } else {
                            count++;
                        }
                        Relation countRel = new Relation(RelationOp.EQUALS, TimeAttribute.DOWIM, TimeAttribute.DOWIM.build("" + count));

                        int dayIndex = daysCombo.getSelectionIndex();
                        if (dayIndex == -1) {
                            dayIndex = 0;
                        }
                        Relation dayRel = new Relation(RelationOp.EQUALS, TimeAttribute.WEEKDAY, TimeAttribute.WEEKDAY.build(DAY_NAMES[dayIndex]));

                        PredicateHelpers.addPredicateToConditions(policy, countRel);
                        PredicateHelpers.addPredicateToConditions(policy, dayRel);
                        // add undo info
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE, oldValue, accessor.getTimePredicate());
                        // refresh the control
                        setStateFromDomainObject();
                    }
                }
            });

            Composite dowimPanel = new Composite(group, SWT.NONE);
            dowimPanel.setBackground(background);

            populateWeekPanel(weekPanel);
            populateDayPanel(dayPanel);
            populateDowimPanel(dowimPanel);
        }

        private void populateWeekPanel(Composite weekPanel) {
            Color background = getBackground();

            GridLayout layout = new GridLayout();
            layout.numColumns = 7;
            weekPanel.setLayout(layout);
            weekPanel.setBackground(background);

            for (String label : DAY_LABELS) {
                Label l = new Label(weekPanel, SWT.NONE);
                l.setText(label);
                l.setBackground(background);
            }

            if (weekdayButtonList == null) {
                weekdayButtonList = new ArrayList<Button>();
            } else {
                weekdayButtonList.clear();
            }
            for (int i = 0; i < 7; i++) {
                Button b = new Button(weekPanel, SWT.CHECK);
                weekdayButtonList.add(b);
                b.addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent e) {
                        Button b = (Button) e.getSource();
                        int index = weekdayButtonList.indexOf(b);
                        IDPolicy policy = (IDPolicy) getDomainObject();
                        String name = DAY_NAMES[index].toLowerCase();
                        if (b.getSelection()) {
                            // add a new weekday relation
                            PredicateHelpers.addWeekdayExpressionToConditions(policy, name);
                            PolicyUndoElement undo = new PolicyUndoElement();
                            undo.setNewValue(name);
                            undo.setOp(PolicyUndoElementOp.ADD_WEEKDAY);
                            GlobalState.getInstance().addUndoElement(undo);
                        } else {
                            // remove a weekday relation and resave
                            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, name);
                            PolicyUndoElement undo = new PolicyUndoElement();
                            undo.setOldValue(name);
                            undo.setOp(PolicyUndoElementOp.REMOVE_WEEKDAY);
                            GlobalState.getInstance().addUndoElement(undo);
                        }
                        setStateFromDomainObject();
                    }
                });
            }
        }

        private void populateDayPanel(Composite dayPanel) {
            Color background = getBackground();
            FormLayout layout = new FormLayout();
            dayPanel.setLayout(layout);

            Label t1 = new Label(dayPanel, SWT.NONE);
            t1.setText(EditorMessages.POLICYEDITOR_DAYS);
            t1.setBackground(background);
            FormData t1Data = new FormData();
            t1Data.left = new FormAttachment(0, SPACING);
            t1.setLayoutData(t1Data);

            dayNum = new Combo(dayPanel, SWT.READ_ONLY);
            String[] list = new String[31];
            for (int i = 0; i < 31; i++) {
                list[i] = String.valueOf(i + 1);
            }
            dayNum.setItems(list);
            dayNum.select(0);
            FormData dnData = new FormData();
            dnData.left = new FormAttachment(t1, SPACING);
            dayNum.setLayoutData(dnData);
            dayNum.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = (IDPolicy) getDomainObject();
                    Relation pred = (Relation) PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
                    if (!pred.getRHS().toString().equals(dayNum.getText())) {
                        IExpression oldValue = pred.getRHS();
                        IExpression newValue = TimeAttribute.DATE.build(dayNum.getText());
                        pred.setRHS(newValue);
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_DATE, oldValue, newValue);
                    }
                }
            });

            Label t2 = new Label(dayPanel, SWT.NONE);
            t2.setText(EditorMessages.POLICYEDITOR_OF_EVERY_MONTH);
            t2.setBackground(background);
            FormData t2Data = new FormData();
            t2Data.left = new FormAttachment(dayNum, SPACING);
            t2.setLayoutData(t2Data);
        }

        private void populateDowimPanel(Composite dowimPanel) {
            Color background = getBackground();
            FormLayout layout = new FormLayout();
            dowimPanel.setLayout(layout);

            Label t1 = new Label(dowimPanel, SWT.NONE);
            t1.setText(EditorMessages.POLICYEDITOR_THE);
            t1.setBackground(background);
            FormData t1Data = new FormData();
            t1Data.left = new FormAttachment(0, SPACING);
            t1.setLayoutData(t1Data);

            dayCountCombo = new Combo(dowimPanel, SWT.READ_ONLY);
            dayCountCombo.setItems(DAY_COUNT_LABELS);
            dayCountCombo.select(0);
            FormData dcData = new FormData();
            dcData.left = new FormAttachment(t1, SPACING);
            dayCountCombo.setLayoutData(dcData);
            dayCountCombo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = (IDPolicy) getDomainObject();
                    Relation rel = (Relation) PredicateHelpers.getDOWIMPredicate(policy.getConditions());
                    int count = dayCountCombo.getSelectionIndex();
                    if (count == -1) {
                        count = 1;
                    } else if (count == DAY_COUNT_LABELS.length - 1) {
                        count = -1;
                    } else {
                        count++;
                    }
                    if (!rel.getRHS().toString().equals("" + count)) {
                        IExpression oldValue = rel.getRHS();
                        IExpression newValue = TimeAttribute.DOWIM.build("" + count);
                        rel.setRHS(newValue);
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_DOWIM, oldValue, newValue);
                    }
                }
            });

            daysCombo = new Combo(dowimPanel, SWT.READ_ONLY);
            daysCombo.setItems(DAY_NAMES);
            daysCombo.select(0);
            FormData dData = new FormData();
            dData.left = new FormAttachment(dayCountCombo, SPACING);
            daysCombo.setLayoutData(dData);
            daysCombo.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    IDPolicy policy = (IDPolicy) getDomainObject();
                    Relation rel = (Relation) PredicateHelpers.getWeekDayPredicate(policy.getConditions());
                    String oldValue = rel.getRHS().toString();
                    // remove the quotes that this always seems to have:
                    oldValue = oldValue.substring(1, oldValue.length() - 1);
                    if (!oldValue.toLowerCase().equals(daysCombo.getText().toLowerCase())) {
                        IExpression oldExp = rel.getRHS();
                        IExpression newExp = TimeAttribute.WEEKDAY.build(daysCombo.getText());
                        rel.setRHS(newExp);
                        addConditionUndoElement(PolicyUndoElementOp.CHANGE_RECURRENCE_WEEKDAY, oldExp, newExp);
                    }
                }
            });

            Label t2 = new Label(dowimPanel, SWT.NONE);
            t2.setText(EditorMessages.POLICYEDITOR_OF_EVERY_MONTH);
            t2.setBackground(background);
            FormData t2Data = new FormData();
            t2Data.left = new FormAttachment(daysCombo, SPACING);
            t2.setLayoutData(t2Data);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimeRow#setStateFromDomainObject()
         */
        public void setStateFromDomainObject() {
            for (Button button : weekdayButtonList) {
                button.setSelection(false);
            }

            IDPolicy policy = (IDPolicy) getDomainObject();
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(policy.getConditions());
            IPredicate weekday = PredicateHelpers.getWeekDayPredicate(policy.getConditions());
            if (dowim != null) {
                // setup dowim section
                weekRadio.setSelection(false);
                dayNumRadio.setSelection(false);
                dowimRadio.setSelection(true);

                IExpression exp = ((Relation) dowim).getRHS();
                Long storedCount = (Long) exp.evaluate(null).getValue();
                int count = storedCount.intValue();
                if (count == -1) {
                    // assuming the last index is "last"
                    count = DAY_COUNT_LABELS.length;
                }
                count--; // subtract 1 to get the array index;

                dayCountCombo.select(count);

                IExpression weekdayExp = ((Relation) weekday).getRHS();
                String savedValue = weekdayExp.toString();
                // remove the quotes that this always seems to have:
                savedValue = savedValue.substring(1, savedValue.length() - 1);

                for (int i = 0; i < DAY_NAMES.length; i++) {
                    if (DAY_NAMES[i].toLowerCase().equals(savedValue.toLowerCase())) {
                        daysCombo.select(i);
                        break;
                    }
                }

                daysCombo.setEnabled(true && isEditable());
                dayCountCombo.setEnabled(true && isEditable());
                dayNum.setEnabled(false);
                for (Button button : weekdayButtonList) {
                    button.setEnabled(false);
                }

            } else if (weekday != null) {
                // setup weekday section
                weekRadio.setSelection(true);
                dayNumRadio.setSelection(false);
                dowimRadio.setSelection(false);

                for (Button button : weekdayButtonList) {
                    button.setEnabled(true);
                }

                if (weekday instanceof Relation) {
                    Button b = getButtonForExpression(((Relation) weekday).getRHS());
                    b.setSelection(true);
                    b.setEnabled(false); // don't let people remove the last
                    // day
                    // because we have no way to track an empty weekday section.
                } else if (weekday instanceof CompositePredicate) {
                    List<IPredicate> list = ((CompositePredicate) weekday).predicates();
                    for (IPredicate predicate : list) {
                        IExpression exp = ((Relation) predicate).getRHS();
                        Button b = getButtonForExpression(exp);
                        b.setSelection(true);
                        b.setEnabled(true && isEditable());
                    }
                }

                daysCombo.setEnabled(false);
                dayCountCombo.setEnabled(false);
                dayNum.setEnabled(false);

            } else {
                IPredicate day = PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
                if (day != null) {
                    // setup day section
                    weekRadio.setSelection(false);
                    dayNumRadio.setSelection(true);
                    dowimRadio.setSelection(false);

                    String dayText = ((Relation) day).getRHS().toString();
                    dayNum.setText(dayText);
                }

                daysCombo.setEnabled(false);
                dayCountCombo.setEnabled(false);
                dayNum.setEnabled(true && isEditable());
                for (Button button : weekdayButtonList) {
                    button.setEnabled(false);
                }
            }
        }

        private Button getButtonForExpression(IExpression exp) {
            Long i = (Long) exp.evaluate(null).getValue();
            return (Button) weekdayButtonList.get(i.intValue() - 1);
        }

        private void addConditionUndoElement(PolicyUndoElementOp op, Object oldCondition, Object newCondition) {
            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOldValue(oldCondition);
            undo.setNewValue(newCondition);
            undo.setOp(op);
            GlobalState.getInstance().addUndoElement(undo);
        }
    }

    private class RecurringScheduleAccessor extends TimePredicateAccessor {

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimePredicateAccessor#addNewTimePredicate()
         */
        public void addNewTimePredicate() {
            addNewWeekdayPredicate();
        }

        public void addNewWeekdayPredicate() {
            IDPolicy policy = (IDPolicy) getDomainObject();
            List<Relation> days = new ArrayList<Relation>();
            String[] weekdays = new DateFormatSymbols().getWeekdays();
            for (String weekday : weekdays) {
                // symbol list includes ""
                if (weekday.length() == 0)
                    continue;
                Relation rel = new Relation(RelationOp.EQUALS, TimeAttribute.WEEKDAY, TimeAttribute.WEEKDAY.build(weekday.toLowerCase()));
                days.add(rel);
            }
            CompositePredicate pred = new CompositePredicate(BooleanOp.OR, days);
            PredicateHelpers.setWeekdayPredicate(policy, pred);

            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE);
            undo.setOldValue(null);
            undo.setNewValue(getTimePredicate());
            GlobalState.getInstance().addUndoElement(undo);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimePredicateAccessor#getTimePredicate()
         */
        public IPredicate getTimePredicate() {
            IDPolicy policy = (IDPolicy) getDomainObject();
            IPredicate conditions = policy.getConditions();
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            if (dowim != null) {
                return PredicateHelpers.getFullDOWIMInfoPredicates(conditions);
            }
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            if (wdp != null) {
                return wdp;
            }
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            if (dom != null) {
                return dom;
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimePredicateAccessor#hasTimePredicate()
         */
        public boolean hasTimePredicate() {
            IDPolicy policy = (IDPolicy) getDomainObject();
            IPredicate conditions = policy.getConditions();
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            return (dowim != null || dom != null || wdp != null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimePredicateAccessor#modifyTime(java.lang.Object)
         */
        public void modifyTime(Object timeInfo) {
            // not using this method here, since all the modification code is
            // specified in the row class implementation
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bluejungle.destiny.policymanager.editor.PolicyEditor.TimePredicateAccessor#removeTimePredicate()
         */
        public void removeTimePredicate() {

            IPredicate oldValue = getTimePredicate();
            doRemoveExistingTimePredicate();

            PolicyUndoElement undo = new PolicyUndoElement();
            undo.setOp(PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE);
            undo.setOldValue(oldValue);
            undo.setNewValue(null);
            GlobalState.getInstance().addUndoElement(undo);

        }

        public void doRemoveExistingTimePredicate() {
            IDPolicy policy = (IDPolicy) getDomainObject();
            IPredicate conditions = policy.getConditions();
            IPredicate dowim = PredicateHelpers.getDOWIMPredicate(conditions);
            if (dowim != null) {
                // policy will have a day of week in month (ie. First) and a
                // weekday (ie. Monday)
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            }
            IPredicate wdp = PredicateHelpers.getWeekDayPredicate(conditions);
            if (wdp != null) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            }
            IPredicate dom = PredicateHelpers.getDayOfMonthPredicate(conditions);
            if (dom != null) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
        }
    }

    /**
     * @author sgoldstein
     */
    public class ObjectModifiedListner implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            updateFromDomainObject();
        }
    }

    protected String getPolicyType() {
        return PolicyEnum.COMMUNICATION_POLICY.toString();
    }

    private void updateObligation() {
        int effect = getEffect();

        if (effect == 2) {
            updateConnectionType();
            startTime.setupRow();
            endTime.setupRow();
            dailySchedule.setupRow();
            recurringEnforcement.setupRow();

            for (Control control : denyObligations.getChildren()) {
                control.dispose();
            }
            initializeOneObligation(denyObligations, EffectType.DENY);

            dateSectionLabel.setVisible(false);
            separatorCondition.setVisible(false);
            connectionTypeSection.setVisible(false);
            enforcementDateSection.setVisible(false);
            recurrentDateSection.setVisible(false);
            connectionTypeLabel.setVisible(false);
            enfDateLabel.setVisible(false);
            recDateLabel.setVisible(false);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(rsrcSrcComp, SPACING);
            data.height = 0;
            data.width = 0;
            dateSectionLabel.setLayoutData(data);
            separatorCondition.setLayoutData(data);
            connectionTypeSection.setLayoutData(data);
            enforcementDateSection.setLayoutData(data);
            recurrentDateSection.setLayoutData(data);
            connectionTypeLabel.setLayoutData(data);
            enfDateLabel.setLayoutData(data);
            recDateLabel.setLayoutData(data);
            denyLabel.setLayoutData(data);
            denyObligations.setLayoutData(data);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(separatorObligation, SPACING);
            data.height = 0;
            data.width = 0;
            denyLabel.setLayoutData(data);
            denyObligations.setLayoutData(data);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(separatorObligation, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
            allowLabel.setLayoutData(data);

            addSectionFormData(allowObligations, allowLabel, separatorObligation);
        } else {
            dateSectionLabel.setVisible(true);
            separatorCondition.setVisible(true);
            connectionTypeSection.setVisible(true);
            enforcementDateSection.setVisible(true);
            recurrentDateSection.setVisible(true);
            connectionTypeLabel.setVisible(true);
            enfDateLabel.setVisible(true);
            recDateLabel.setVisible(true);

            FormData data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(rsrcSrcComp, SPACING);
            dateSectionLabel.setLayoutData(data);

            data = new FormData();
            data.top = new FormAttachment(dateSectionLabel, 2);
            data.left = new FormAttachment(0, 100, SPACING);
            data.right = new FormAttachment(100);
            separatorCondition.setLayoutData(data);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(separatorCondition, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(connectionTypeSection, 0, SWT.BOTTOM);
            connectionTypeLabel.setLayoutData(data);

            addSectionFormData(connectionTypeSection, connectionTypeLabel, separatorCondition);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(connectionTypeLabel, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(enforcementDateSection, 0, SWT.BOTTOM);
            enfDateLabel.setLayoutData(data);

            addSectionFormData(enforcementDateSection, enfDateLabel, connectionTypeLabel);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(enfDateLabel, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(recurrentDateSection, 0, SWT.BOTTOM);
            recDateLabel.setLayoutData(data);

            addSectionFormData(recurrentDateSection, recDateLabel, enfDateLabel);

            denyLabel.setVisible(true);
            denyObligations.setVisible(true);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(separatorObligation, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(denyObligations, 0, SWT.BOTTOM);
            denyLabel.setLayoutData(data);

            addSectionFormData(denyObligations, denyLabel, separatorObligation);

            data = new FormData();
            data.left = new FormAttachment(0, 100, SPACING);
            data.top = new FormAttachment(denyLabel, SPACING);
            data.width = LABEL_COLUMN_WIDTH;
            data.bottom = new FormAttachment(allowObligations, 0, SWT.BOTTOM);
            allowLabel.setLayoutData(data);

            addSectionFormData(allowObligations, allowLabel, denyLabel);
        }
    }
}
