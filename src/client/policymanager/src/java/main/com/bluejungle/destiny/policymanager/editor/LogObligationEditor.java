/*
 * Created on Aug 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElement;
import com.bluejungle.destiny.policymanager.ui.PolicyUndoElementOp;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.LogObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author bmeng
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/LogObligationEditor.java#1 $
 */

public class LogObligationEditor extends BaseObligationEditor {

    public LogObligationEditor(Composite parent, IDPolicy policy, IDEffectType effectType, boolean enabled) {
        super(parent, policy, effectType, enabled);
    }

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
                } else {
                    IObligation obligationToRemove = getObligation();
                    policy.deleteObligation(obligationToRemove, effectType);
                    undoElement.setOp(PolicyUndoElementOp.REMOVE_OBLIGATION);
                    undoElement.setOldValue(undoElement.new ObligationRecord(effectType, obligationToRemove));
                }
                GlobalState.getInstance().addUndoElement(undoElement);
            }
        });
    }

    protected void initData() {
        super.initData();

        boolean isLogEnabled = (isEnabled() && (getEffectType().getType() != EffectType.DENY_TYPE));
        obligationCheckBox.setEnabled(isLogEnabled);
    }

    protected IObligation createObligation() {
        return getObligationManager().createLogObligation();
    }

    @Override
    protected String getTitle() {
        return EditorMessages.POLICYEDITOR_LOG;
    }

    @Override
    protected String getObligationType() {
        return LogObligation.OBLIGATION_NAME;
    }
}
