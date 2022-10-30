/*
 * Created on Aug 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.lifecycle.ObligationArgument;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/CustomObligationEditor.java#11 $
 */

public class CustomObligationEditor extends BaseObligationEditor {

    private final static int BUTTON_SIZE = 15;
    private Button check;
    private Composite container;

    public CustomObligationEditor(Composite parent, IDPolicy policy, IDEffectType effectType, boolean enabled) {
        super(parent, policy, effectType, enabled);
    }

    @Override
    protected void init() {
        super.init();

        obligationCheckBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IDPolicy policy = getPolicy();
                IDEffectType effectType = getEffectType();
                PolicyUndoElement undoElement = new PolicyUndoElement();
                boolean checkBoxIsSelected = obligationCheckBox.getSelection();
                if (checkBoxIsSelected) {
                    IObligation obligationToAdd = createObligation();
                    policy.addObligation(obligationToAdd, effectType);
                    undoElement.setOp(PolicyUndoElementOp.ADD_OBLIGATION);
                    undoElement.setNewValue(undoElement.new ObligationRecord(effectType, obligationToAdd));

                    createDetail();
                } else {
                    IObligation obligationToRemove = getObligation();
                    policy.deleteObligation(obligationToRemove, effectType);
                    undoElement.setOp(PolicyUndoElementOp.REMOVE_OBLIGATION);
                    undoElement.setOldValue(undoElement.new ObligationRecord(effectType, obligationToRemove));

                    disposeDetail();
                }

                CustomObligationEditor.this.layout();
                GlobalState.getInstance().addUndoElement(undoElement);
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        if (obligationExists()) {
            createDetail();
        }
    }

    private void createDetail() {
        check = new Button(this, SWT.CHECK);
        check.setVisible(false);

        container = new Composite(this, SWT.NONE);
        container.setEnabled(getEnabled());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        container.setLayoutData(data);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);

        List<IObligation> customObligations = findObligations();
        for (IObligation obligation : customObligations) {
            createLines((CustomObligation) obligation);
        }

        setBackground(getBackground());
    }

    private void showHideButton() {
        int count = 0;
        Control[] controls = container.getChildren();
        Button firstMinusButton = null;
        boolean foundLastPlusButton = false;
        for (int i = controls.length - 1; i >= 0; i--) {
            if (controls[i] instanceof Button) {
                Button button = (Button) controls[i];
                if (button.getText().equals(EditorMessages.POLICYEDITOR_PLUS)) {
                    count++;
                    if (!foundLastPlusButton) {
                        button.setVisible(true);
                        foundLastPlusButton = true;
                    } else {
                        button.setVisible(false);
                    }
                }
                if (button.getText().equals(EditorMessages.POLICYEDITOR_MINUS)) {
                    firstMinusButton = button;
                }
            }
        }
        if (count == 1) {
            firstMinusButton.setVisible(false);
        } else {
            firstMinusButton.setVisible(true);
        }

        // if is disabled, all the buttons should be invisible
        if (!getEnabled()) {
            for (int i = controls.length - 1; i >= 0; i--) {
                if (controls[i] instanceof Button) {
                    controls[i].setVisible(getEnabled());
                }
            }
        }
    }

    private CustomObligationComposite createLines(CustomObligation obligation) {
        GridData data;
        Button minusButton = new Button(container, SWT.FLAT);
        minusButton.setEnabled(getEnabled());
        minusButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        minusButton.setText(EditorMessages.POLICYEDITOR_MINUS);
        minusButton.setToolTipText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_REMOVE_BUTTON_TOOLTIP);
        data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.widthHint = BUTTON_SIZE;
        data.heightHint = BUTTON_SIZE;
        minusButton.setLayoutData(data);
        minusButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Button internalMinusButton = (Button) e.getSource();
                Button internalPlusButton = (Button) internalMinusButton.getData();
                CustomObligationComposite internalComposite = (CustomObligationComposite) internalPlusButton.getData();
                IDEffectType effectType = getEffectType();
                CustomObligation obligation = internalComposite.getObligation();
                getPolicy().deleteObligation(obligation, effectType);

                internalComposite.dispose();
                internalPlusButton.dispose();
                internalMinusButton.dispose();

                showHideButton();
                CustomObligationEditor.this.layout();
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        Button plusButton = new Button(container, SWT.FLAT);
        plusButton.setEnabled(getEnabled());
        plusButton.setText(EditorMessages.POLICYEDITOR_PLUS);
        plusButton.setCursor(ResourceManager.getCursor(SWT.CURSOR_HAND));
        plusButton.setToolTipText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_ADD_BUTTON_TOOLTIP);
        data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.widthHint = BUTTON_SIZE;
        data.heightHint = BUTTON_SIZE;
        plusButton.setLayoutData(data);
        plusButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CustomObligation newObligation = (CustomObligation) createObligation();
                IDEffectType effectType = getEffectType();
                getPolicy().addObligation(newObligation, effectType);

                createLines(newObligation);

                showHideButton();
                CustomObligationEditor.this.layout();
                GlobalState.getInstance().getEditorPanel().relayout();
            }
        });

        CustomObligationComposite customObligationComposite = new CustomObligationComposite(container, obligation, getEnabled());
        data = new GridData(GridData.FILL_HORIZONTAL);
        customObligationComposite.setLayoutData(data);

        minusButton.setData(plusButton);
        plusButton.setData(customObligationComposite);

        showHideButton();
        CustomObligationEditor.this.layout();

        setBackground(getBackground());
        return customObligationComposite;
    }

    private void disposeDetail() {
        check.dispose();
        check = null;
        container.dispose();
        container = null;
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected String getTitle() {
        return EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION;
    }

    @Override
    protected String getObligationType() {
        return CustomObligation.OBLIGATION_NAME;
    }

    protected IObligation createObligation() {
        return getObligationManager().createCustomObligation("", Collections.EMPTY_LIST);
    }

    public class CustomObligationComposite extends Composite {

        private CustomObligation obligation;
        private Combo comboObligationName;
        private Composite subContainer;

        private ModifyListener modifyListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                List<String> args = new ArrayList<String>();
                for (Control control : subContainer.getChildren()) {
                    if (control instanceof Label) {
                        Label label = (Label) control;
                        args.add(label.getText());
                    }
                    if (control instanceof Text) {
                        Text text = (Text) control;
                        args.add(text.getText());
                    }
                    if (control instanceof Combo) {
                        Combo combo = (Combo) control;
                        args.add(combo.getText());
                    }
                }
                obligation.setCustomArgs(args);
            }
        };

        /**
         * Create an instance of CustomObligationCommandComposite
         * 
         * @param editor
         * @param customObligation
         */
        public CustomObligationComposite(Composite parent, IObligation customObligation, boolean isEnabled) {
            super(parent, SWT.None);
            setEnabled(isEnabled);
            if (customObligation == null) {
                throw new NullPointerException("nextCustomObligation cannot be null.");
            }
            obligation = (CustomObligation) customObligation;
            init();
        }

        private void init() {
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            setLayout(layout);
            Group group = new Group(this, SWT.NONE);
            GridData data = new GridData(GridData.FILL_BOTH);
            group.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 5;
            layout.marginWidth = 5;
            group.setLayout(layout);

            Label label = new Label(group, SWT.NONE);
            label.setEnabled(getEnabled());
            label.setText(EditorMessages.POLICYEDITOR_CUSTOM_OBLIGATION_COMMAND);
            data = new GridData();
            label.setLayoutData(data);

            comboObligationName = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
            comboObligationName.setEnabled(getEnabled());
            data = new GridData(GridData.FILL_HORIZONTAL);
            comboObligationName.setLayoutData(data);
            Collection<ObligationDescriptor> obligationDescriptors = new ArrayList<ObligationDescriptor>();
            try {
                obligationDescriptors = PolicyServerProxy.client.getObligationDescriptors();
            } catch (PolicyEditorException e1) {
                e1.printStackTrace();
            }
            for (ObligationDescriptor descriptor : obligationDescriptors) {
                comboObligationName.add(descriptor.getDisplayName());
            }

            comboObligationName.addModifyListener(new ModifyListener() {

                /**
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                public void modifyText(ModifyEvent e) {
                    String newText = comboObligationName.getText();
                    ObligationDescriptor obligationDescriptor = null;
                    Collection<ObligationDescriptor> obligationDescriptors = new ArrayList<ObligationDescriptor>();
                    try {
                        obligationDescriptors = PolicyServerProxy.client.getObligationDescriptors();
                    } catch (PolicyEditorException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    for (ObligationDescriptor descriptor : obligationDescriptors) {
                        if (descriptor.getDisplayName().equals(newText)) {
                            obligationDescriptor = descriptor;
                            break;
                        }
                    }
                    if (!obligation.getCustomObligationName().equals(newText)) {
                        obligation.setCustomObligationName(newText);

                        ObligationArgument[] arguments = obligationDescriptor.getObligationArguments();
                        List<String> presetArguments = new ArrayList<String>();
                        if (arguments == null) {
                            arguments = new ObligationArgument[0];
                        }
                        for (int i = 0, n = arguments.length; i < n; i++) {
                            ObligationArgument argument = arguments[i];
                            presetArguments.add(argument.getDisplayName());
                            if (argument.getDefaultValue() != null) {
                                presetArguments.add(argument.getDefaultValue());
                            } else {
                                String values[] = argument.getValues();
                                if (values == null || values.length == 0) {
                                    presetArguments.add("");
                                } else {
                                    presetArguments.add(values[0]);
                                }
                            }
                        }
                        obligation.setCustomArgs(presetArguments);
                    }

                    for (Control control : subContainer.getChildren()) {
                        control.dispose();
                    }

                    for (String item : comboObligationName.getItems()) {
                        if (item.equals(newText)) {
                            addArgumentsControl(subContainer, obligationDescriptor);
                            GridData data = new GridData(GridData.FILL_HORIZONTAL);
                            data.horizontalSpan = 2;
                            data.heightHint = subContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                            subContainer.setLayoutData(data);
                            subContainer.layout(true, true);
                        }
                    }
                    try {
                        GlobalState.getInstance().getEditorPanel().relayout();
                    } catch (Exception ex) {
                    }
                }
            });

            subContainer = new Composite(group, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            data.heightHint = 0;
            subContainer.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            subContainer.setLayout(layout);

            if (obligation != null) {
                comboObligationName.setText(obligation.getCustomObligationName());
            }
        }

        private void addArgumentsControl(final Composite root, ObligationDescriptor descriptor) {
            for (Control control : root.getChildren()) {
                control.dispose();
            }
            ObligationArgument[] arguments = descriptor.getObligationArguments();
            if (arguments == null) {
                arguments = new ObligationArgument[0];
            }
            for (int i = 0, n = arguments.length; i < n; i++) {
                ObligationArgument argument = arguments[i];
                Label label = new Label(root, SWT.NONE);
                label.setEnabled(getEnabled());
                label.setText(argument.getDisplayName());

                String[] values = argument.getValues();
                String value = (String) obligation.getCustomObligationArgs().get(i * 2 + 1);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                if (values == null || values.length == 0) {
                    Text text = new Text(root, SWT.BORDER);
                    text.setEnabled(getEnabled());
                    text.setLayoutData(data);
                    text.setText(value);
                    text.addModifyListener(modifyListener);
                } else {
                    boolean isUserEditable = argument.isUserEditable();
                    Combo combo = null;
                    if (isUserEditable) {
                        combo = new Combo(root, SWT.BORDER);
                    } else {
                        combo = new Combo(root, SWT.BORDER | SWT.READ_ONLY);
                    }
                    combo.setEnabled(getEnabled());
                    combo.setLayoutData(data);
                    combo.setItems(values);
                    combo.setText(value);
                    combo.addModifyListener(modifyListener);
                }
            }
            setBackground(getBackground());
        }

        public CustomObligation getObligation() {
            return obligation;
        }

        /**
         * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
         */
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            for (Control child : getChildren()) {
                setControlBackground(child, color);
            }
        }

        private void setControlBackground(Control control, Color color) {
            control.setBackground(color);
            if (control instanceof Composite) {
                Composite composite = (Composite) control;
                for (Control child : composite.getChildren()) {
                    setControlBackground(child, color);
                }
            }
        }
    }
}
