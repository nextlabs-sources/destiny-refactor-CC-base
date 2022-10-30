/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author dstarke
 * 
 */
public class PolicyUndoElementOp extends EnumBase {

    private static final long serialVersionUID = 1L;

    public static final String CHANGE_EFFECT_NAME = "CHANGE_EFFECT";
    public static final int CHANGE_EFFECT_TYPE = 1;

    public static final String CHANGE_DISPLAY_OBLIGATION_MSG_NAME = "CHANGE_DISPLAY_OBLIGATION_MSG";
    public static final int CHANGE_DISPLAY_OBLIGATION_MSG_TYPE = 2;
    // public static final String ADD_ACTION_NAME = "ADD_ACTION";
    // public static final int ADD_ACTION_TYPE = 3;
    //
    // public static final String REMOVE_ACTION_NAME = "REMOVE_ACTION";
    // public static final int REMOVE_ACTION_TYPE = 4;

    public static final String ADD_OBLIGATION_NAME = "ADD_OBLIGATION";
    public static final int ADD_OBLIGATION_TYPE = 5;

    public static final String REMOVE_OBLIGATION_NAME = "REMOVE_OBLIGATION";
    public static final int REMOVE_OBLIGATION_TYPE = 6;

    public static final String CHANGE_NOTIFY_OBLIGATION_ADDRESS_NAME = "CHANGE_NOTIFY_OBLIGATION_ADDRESS";
    public static final int CHANGE_NOTIFY_OBLIGATION_ADDRESS_TYPE = 7;

    public static final String CHANGE_NOTIFY_OBLIGATION_MSG_NAME = "CHANGE_NOTIFY_OBLIGATION_MSGE";
    public static final int CHANGE_NOTIFY_OBLIGATION_MSG_TYPE = 8;

    public static final String CHANGE_START_DATE_NAME = "CHANGE_START_DATE";
    public static final int CHANGE_START_DATE_TYPE = 9;

    public static final String CHANGE_END_DATE_NAME = "CHANGE_END_DATE";
    public static final int CHANGE_END_DATE_TYPE = 10;

    public static final String CHANGE_DAILY_SCHEDULE_NAME = "CHANGE_DAILY_SCHEDULE";
    public static final int CHANGE_DAILY_SCHEDULE_TYPE = 11;

    public static final String CHANGE_DAILY_SCHEDULE_FROM_NAME = "CHANGE_DAILY_SCHEDULE_FROM";
    public static final int CHANGE_DAILY_SCHEDULE_FROM_TYPE = 12;

    public static final String CHANGE_DAILY_SCHEDULE_TO_NAME = "CHANGE_DAILY_SCHEDULE_TO";
    public static final int CHANGE_DAILY_SCHEDULE_TO_TYPE = 13;

    public static final String ADD_WEEKDAY_NAME = "ADD_WEEKDAY";
    public static final int ADD_WEEKDAY_TYPE = 14;

    public static final String REMOVE_WEEKDAY_NAME = "REMOVE_WEEKDAY";
    public static final int REMOVE_WEEKDAY_TYPE = 15;

    public static final String CHANGE_RECURRENCE_PREDICATE_NAME = "CHANGE_RECURRENCE_PREDICATE";
    public static final int CHANGE_RECURRENCE_PREDICATE_TYPE = 16;

    public static final String CHANGE_RECURRENCE_DATE_NAME = "CHANGE_RECURRENCE_DATE";
    public static final int CHANGE_RECURRENCE_DATE_TYPE = 17;

    public static final String CHANGE_RECURRENCE_DOWIM_NAME = "CHANGE_RECURRENCE_DOWIM";
    public static final int CHANGE_RECURRENCE_DOWIM_TYPE = 18;

    public static final String CHANGE_RECURRENCE_WEEKDAY_NAME = "CHANGE_RECURRENCE_WEEKDAY";
    public static final int CHANGE_RECURRENCE_WEEKDAY_TYPE = 19;

    public static final String CHANGE_CONNECTION_TYPE_NAME = "CHANGE_CONNECTION_TYPE";
    public static final int CHANGE_CONNECTION_TYPE_TYPE = 20;

    public static final PolicyUndoElementOp CHANGE_EFFECT = new PolicyUndoElementOp(CHANGE_EFFECT_NAME, CHANGE_EFFECT_TYPE);

    // public static final PolicyUndoElementOp ADD_ACTION = new
    // PolicyUndoElementOp(ADD_ACTION_NAME, ADD_ACTION_TYPE);
    // public static final PolicyUndoElementOp REMOVE_ACTION = new
    // PolicyUndoElementOp(REMOVE_ACTION_NAME, REMOVE_ACTION_TYPE);
    public static final PolicyUndoElementOp ADD_OBLIGATION = new PolicyUndoElementOp(ADD_OBLIGATION_NAME, ADD_OBLIGATION_TYPE);
    public static final PolicyUndoElementOp REMOVE_OBLIGATION = new PolicyUndoElementOp(REMOVE_OBLIGATION_NAME, REMOVE_OBLIGATION_TYPE);
    public static final PolicyUndoElementOp CHANGE_NOTIFY_OBLIGATION_ADDRESS = new PolicyUndoElementOp(CHANGE_NOTIFY_OBLIGATION_ADDRESS_NAME, CHANGE_NOTIFY_OBLIGATION_ADDRESS_TYPE);
    public static final PolicyUndoElementOp CHANGE_NOTIFY_OBLIGATION_MSG = new PolicyUndoElementOp(CHANGE_NOTIFY_OBLIGATION_MSG_NAME, CHANGE_NOTIFY_OBLIGATION_MSG_TYPE);
    public static final PolicyUndoElementOp CHANGE_DISPLAY_OBLIGATION_MSG = new PolicyUndoElementOp(CHANGE_DISPLAY_OBLIGATION_MSG_NAME, CHANGE_DISPLAY_OBLIGATION_MSG_TYPE);
    public static final PolicyUndoElementOp CHANGE_START_DATE = new PolicyUndoElementOp(CHANGE_START_DATE_NAME, CHANGE_START_DATE_TYPE);
    public static final PolicyUndoElementOp CHANGE_END_DATE = new PolicyUndoElementOp(CHANGE_END_DATE_NAME, CHANGE_END_DATE_TYPE);
    public static final PolicyUndoElementOp CHANGE_DAILY_SCHEDULE = new PolicyUndoElementOp(CHANGE_DAILY_SCHEDULE_NAME, CHANGE_DAILY_SCHEDULE_TYPE);
    public static final PolicyUndoElementOp CHANGE_DAILY_SCHEDULE_FROM = new PolicyUndoElementOp(CHANGE_DAILY_SCHEDULE_FROM_NAME, CHANGE_DAILY_SCHEDULE_FROM_TYPE);
    public static final PolicyUndoElementOp CHANGE_DAILY_SCHEDULE_TO = new PolicyUndoElementOp(CHANGE_DAILY_SCHEDULE_TO_NAME, CHANGE_DAILY_SCHEDULE_TO_TYPE);
    public static final PolicyUndoElementOp ADD_WEEKDAY = new PolicyUndoElementOp(ADD_WEEKDAY_NAME, ADD_WEEKDAY_TYPE);
    public static final PolicyUndoElementOp REMOVE_WEEKDAY = new PolicyUndoElementOp(REMOVE_WEEKDAY_NAME, REMOVE_WEEKDAY_TYPE);
    public static final PolicyUndoElementOp CHANGE_RECURRENCE_PREDICATE = new PolicyUndoElementOp(CHANGE_RECURRENCE_PREDICATE_NAME, CHANGE_RECURRENCE_PREDICATE_TYPE);
    public static final PolicyUndoElementOp CHANGE_RECURRENCE_DATE = new PolicyUndoElementOp(CHANGE_RECURRENCE_DATE_NAME, CHANGE_RECURRENCE_DATE_TYPE);
    public static final PolicyUndoElementOp CHANGE_RECURRENCE_DOWIM = new PolicyUndoElementOp(CHANGE_RECURRENCE_DOWIM_NAME, CHANGE_RECURRENCE_DOWIM_TYPE);
    public static final PolicyUndoElementOp CHANGE_RECURRENCE_WEEKDAY = new PolicyUndoElementOp(CHANGE_RECURRENCE_WEEKDAY_NAME, CHANGE_RECURRENCE_WEEKDAY_TYPE);
    public static final PolicyUndoElementOp CHANGE_CONNECTION_TYPE = new PolicyUndoElementOp(CHANGE_CONNECTION_TYPE_NAME, CHANGE_CONNECTION_TYPE_TYPE);

    private PolicyUndoElementOp(String name, int type) {
        super(name, type, PolicyUndoElementOp.class);
    }
}
