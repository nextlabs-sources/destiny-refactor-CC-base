/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Control;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author dstarke
 * 
 * This class represents policy UNDO/REDO actions.
 */
public class PolicyUndoElement extends BaseUndoElement {

    private static final IEventManager EVENT_MANAGER;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        EVENT_MANAGER = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
    }

    private PolicyUndoElementOp op = null;
    private Object oldValue = null;
    private Object newValue = null;

    public class EffectRecord {

        public int index;

        public EffectRecord(int index) {
            this.index = index;
        }
    }

    public class ObligationRecord {

        public IDEffectType type;
        public IObligation obligation;
        public String message = "";

        public ObligationRecord(IDEffectType type, IObligation obligation) {
            this.type = type;
            this.obligation = obligation;
        }

        public ObligationRecord(IDEffectType type, String message) {
            this.type = type;
            this.message = message;
        }
    }

    public PolicyUndoElement() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#undo(java.lang.Object,
     *      org.eclipse.swt.widgets.Control)
     */
    public boolean undo(Object spec, Control control) {
        IDPolicy policy = (IDPolicy) spec;
        if (op == PolicyUndoElementOp.CHANGE_EFFECT) {
            EffectRecord oldRec = (EffectRecord) oldValue;
            PolicyHelpers.saveEffect(policy, oldRec.index);
        } else if (op == PolicyUndoElementOp.ADD_OBLIGATION) {
            ObligationRecord rec = (ObligationRecord) newValue;
            policy.deleteObligation(rec.obligation, rec.type);
        } else if (op == PolicyUndoElementOp.REMOVE_OBLIGATION) {
            ObligationRecord rec = (ObligationRecord) oldValue;
            policy.addObligation(rec.obligation, rec.type);
        } else if (op == PolicyUndoElementOp.CHANGE_NOTIFY_OBLIGATION_ADDRESS) {
            ObligationRecord rec = (ObligationRecord) oldValue;
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (int i = 0; i < obligations.length; i++) {
                if (obligations[i] instanceof NotifyObligation) {
                    ((NotifyObligation) obligations[i]).setEmailAddresses(rec.message);
                    break;
                }
            }
        } else if (op == PolicyUndoElementOp.CHANGE_NOTIFY_OBLIGATION_MSG) {
            ObligationRecord rec = (ObligationRecord) oldValue;
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (int i = 0; i < obligations.length; i++) {
                if (obligations[i] instanceof NotifyObligation) {
                    ((NotifyObligation) obligations[i]).setBody(rec.message);
                    break;
                }
            }
        } else if (op == PolicyUndoElementOp.CHANGE_START_DATE) {
            Date d = (Date) oldValue;
            if (d == null) {
                PredicateHelpers.removeStartTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setStartTime(policy, exp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_END_DATE) {
            Date d = (Date) oldValue;
            if (d == null) {
                PredicateHelpers.removeEndTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setEndTime(policy, exp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE) {
            IExpression[] times = (IExpression[]) oldValue;
            if (times == null) {
                PredicateHelpers.removeDailyFromTime(policy);
                PredicateHelpers.removeDailyToTime(policy);
            } else {
                PredicateHelpers.setDailyFromTime(policy, times[0]);
                PredicateHelpers.setDailyToTime(policy, times[1]);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_FROM) {
            PredicateHelpers.setDailyFromTime(policy, (IExpression) oldValue);
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_TO) {
            PredicateHelpers.setDailyToTime(policy, (IExpression) oldValue);
        } else if (op == PolicyUndoElementOp.ADD_WEEKDAY) {
            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, (String) newValue);
        } else if (op == PolicyUndoElementOp.REMOVE_WEEKDAY) {
            PredicateHelpers.addWeekdayExpressionToConditions(policy, (String) oldValue);
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE) {
            IPredicate oldPred = (IPredicate) oldValue;
            IPredicate newPred = (IPredicate) newValue;
            // first remove the new value
            if (null != PredicateHelpers.getDOWIMPredicate(newPred)) {
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getWeekDayPredicate(newPred)) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(newPred)) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
            // then add the old value
            if (null != PredicateHelpers.getDOWIMPredicate(oldPred)) {
                // the old predicate will have a DOWIM and a Weekday
                // predicate... add both back
                PredicateHelpers.addPredicateToConditions(policy, PredicateHelpers.getDOWIMPredicate(oldPred));
                PredicateHelpers.addPredicateToConditions(policy, PredicateHelpers.getWeekDayPredicate(oldPred));
            } else if (null != PredicateHelpers.getWeekDayPredicate(oldPred)) {
                PredicateHelpers.addPredicateToConditions(policy, oldPred);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(oldPred)) {
                PredicateHelpers.addPredicateToConditions(policy, oldPred);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_DATE) {
            Relation rel = (Relation) PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression oldExp = (IExpression) oldValue;
            if (!actualExp.evaluate(null).equals(oldExp.evaluate(null))) {
                rel.setRHS(oldExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_DOWIM) {
            Relation rel = (Relation) PredicateHelpers.getDOWIMPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression oldExp = (IExpression) oldValue;
            if (!actualExp.evaluate(null).equals(oldExp.evaluate(null))) {
                rel.setRHS(oldExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_WEEKDAY) {
            Relation rel = (Relation) PredicateHelpers.getWeekDayPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression oldExp = (IExpression) oldValue;
            if (!actualExp.evaluate(null).equals(oldExp.evaluate(null))) {
                rel.setRHS(oldExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_CONNECTION_TYPE) {
            Long value = (Long) oldValue;
            if (value == null) {
                PredicateHelpers.removeConnectionType(policy);
            } else {
                long index = value.longValue();
                IExpression exp = Constant.build(index);
                PredicateHelpers.setConnectionType(policy, exp);
            }
        }

        PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(policy);
        EVENT_MANAGER.fireEvent(objectModifiedEvent);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.IUndoElement#redo(java.lang.Object,
     *      org.eclipse.swt.widgets.Control)
     */
    public boolean redo(Object spec, Control control) {
        IDPolicy policy = (IDPolicy) spec;
        if (op == PolicyUndoElementOp.CHANGE_EFFECT) {
            EffectRecord newRec = (EffectRecord) newValue;
            PolicyHelpers.saveEffect(policy, newRec.index);
        } else if (op == PolicyUndoElementOp.ADD_OBLIGATION) {
            ObligationRecord rec = (ObligationRecord) newValue;
            policy.addObligation(rec.obligation, rec.type);
        } else if (op == PolicyUndoElementOp.REMOVE_OBLIGATION) {
            ObligationRecord rec = (ObligationRecord) oldValue;
            policy.deleteObligation(rec.obligation, rec.type);
        } else if (op == PolicyUndoElementOp.CHANGE_NOTIFY_OBLIGATION_ADDRESS) {
            ObligationRecord rec = (ObligationRecord) newValue;
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (int i = 0; i < obligations.length; i++) {
                if (obligations[i] instanceof NotifyObligation) {
                    ((NotifyObligation) obligations[i]).setEmailAddresses(rec.message);
                    break;
                }
            }
        } else if (op == PolicyUndoElementOp.CHANGE_NOTIFY_OBLIGATION_MSG) {
            ObligationRecord rec = (ObligationRecord) newValue;
            IObligation[] obligations = policy.getObligationArray(rec.type);
            for (int i = 0; i < obligations.length; i++) {
                if (obligations[i] instanceof NotifyObligation) {
                    ((NotifyObligation) obligations[i]).setBody(rec.message);
                    break;
                }
            }
        } else if (op == PolicyUndoElementOp.CHANGE_START_DATE) {
            Date d = (Date) newValue;
            if (d == null) {
                PredicateHelpers.removeStartTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setStartTime(policy, exp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_END_DATE) {
            Date d = (Date) newValue;
            if (d == null) {
                PredicateHelpers.removeEndTime(policy);
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(d);
                IExpression exp = TimeAttribute.IDENTITY.build(dateString);
                PredicateHelpers.setEndTime(policy, exp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE) {
            IExpression[] times = (IExpression[]) newValue;
            if (times == null) {
                PredicateHelpers.removeDailyFromTime(policy);
                PredicateHelpers.removeDailyToTime(policy);
            } else {
                PredicateHelpers.setDailyFromTime(policy, times[0]);
                PredicateHelpers.setDailyToTime(policy, times[1]);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_FROM) {
            PredicateHelpers.setDailyFromTime(policy, (IExpression) newValue);
        } else if (op == PolicyUndoElementOp.CHANGE_DAILY_SCHEDULE_TO) {
            PredicateHelpers.setDailyToTime(policy, (IExpression) newValue);
        } else if (op == PolicyUndoElementOp.ADD_WEEKDAY) {
            PredicateHelpers.addWeekdayExpressionToConditions(policy, (String) newValue);
        } else if (op == PolicyUndoElementOp.REMOVE_WEEKDAY) {
            PredicateHelpers.removeWeekdayExpressionFromConditions(policy, (String) oldValue);
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_PREDICATE) {
            IPredicate oldPred = (IPredicate) oldValue;
            IPredicate newPred = (IPredicate) newValue;
            // first remove the old value
            if (null != PredicateHelpers.getDOWIMPredicate(oldPred)) {
                PredicateHelpers.removeDOWIMPredicate(policy);
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getWeekDayPredicate(oldPred)) {
                PredicateHelpers.removeWeekdayPredicate(policy);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(oldPred)) {
                PredicateHelpers.removeDayOfMonthPredicate(policy);
            }
            // then add the new value
            if (null != PredicateHelpers.getDOWIMPredicate(newPred)) {
                // the old predicate will have a DOWIM and a Weekday
                // predicate... add both back
                PredicateHelpers.addPredicateToConditions(policy, PredicateHelpers.getDOWIMPredicate(newPred));
                PredicateHelpers.addPredicateToConditions(policy, PredicateHelpers.getWeekDayPredicate(newPred));
            } else if (null != PredicateHelpers.getWeekDayPredicate(newPred)) {
                PredicateHelpers.addPredicateToConditions(policy, newPred);
            } else if (null != PredicateHelpers.getDayOfMonthPredicate(newPred)) {
                PredicateHelpers.addPredicateToConditions(policy, newPred);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_DATE) {
            Relation rel = (Relation) PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression newExp = (IExpression) newValue;
            if (!actualExp.evaluate(null).equals(newExp.evaluate(null))) {
                rel.setRHS(newExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_DOWIM) {
            Relation rel = (Relation) PredicateHelpers.getDOWIMPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression newExp = (IExpression) newValue;
            if (!actualExp.evaluate(null).equals(newExp.evaluate(null))) {
                rel.setRHS(newExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_RECURRENCE_WEEKDAY) {
            Relation rel = (Relation) PredicateHelpers.getWeekDayPredicate(policy.getConditions());
            IExpression actualExp = rel.getRHS();
            IExpression newExp = (IExpression) newValue;
            if (!actualExp.evaluate(null).equals(newExp.evaluate(null))) {
                rel.setRHS(newExp);
            }
        } else if (op == PolicyUndoElementOp.CHANGE_CONNECTION_TYPE) {
            Long d = (Long) newValue;
            if (d == null) {
                PredicateHelpers.removeConnectionType(policy);
            } else {
                long index = d.longValue();
                IExpression exp = Constant.build(index);
                PredicateHelpers.setConnectionType(policy, exp);
            }
        }

        PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(policy);
        EVENT_MANAGER.fireEvent(objectModifiedEvent);

        return true;
    }

    /**
     * @return Returns the newValue.
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * @param newValue
     *            The newValue to set.
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * @return Returns the oldValue.
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * @param oldValue
     *            The oldValue to set.
     */
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * @return Returns the op.
     */
    public PolicyUndoElementOp getOp() {
        return op;
    }

    /**
     * @param op
     *            The op to set.
     */
    public void setOp(PolicyUndoElementOp op) {
        this.op = op;
    }
}
