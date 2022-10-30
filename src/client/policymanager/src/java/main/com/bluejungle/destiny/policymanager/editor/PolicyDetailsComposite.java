/*
 * Created on Mar 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/PolicyDetailsComposite.java#2 $
 */

public class PolicyDetailsComposite extends Composite {

    public static final String[] EFFECTS = { EditorMessages.POLICYEDITOR_DENY, EditorMessages.POLICYEDITOR_ALLOW, EditorMessages.POLICYEDITOR_MONITOR };
    private static final String[] DAY_NAMES = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    private static final String[] DAY_COUNT_LABELS = { "First", "Second", "Third", "Last" };
    private static Map<SubjectAttribute, LeafObjectType> DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP = new HashMap<SubjectAttribute, LeafObjectType>();
    static {
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_ID, LeafObjectType.USER);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_ID, LeafObjectType.HOST);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.APP_ID, LeafObjectType.APPLICATION);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.USER_LDAP_GROUP_ID, LeafObjectType.USER_GROUP);
        DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.put(SubjectAttribute.HOST_LDAP_GROUP_ID, LeafObjectType.HOST_GROUP);
    }

    private String[] operators1 = new String[] { " in ", " not in " };
    private String[] operators21 = new String[] { " into ", " outside " };
    private String[] operators22 = new String[] { " And into ", " And outside " };

    private IDPolicy policy;

    public PolicyDetailsComposite(Composite parent, int style, IDPolicy policy) {
        super(parent, style);
        this.policy = policy;

        initialize();
    }

    private void initialize() {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);

        // Label labelDetails = new Label(this, SWT.LEFT | SWT.WRAP);
        // labelDetails.setFont(FontBundle.TWELVE_POINT_ARIAL);
        // labelDetails.setText("Details");
        // GridData data = new GridData(GridData.FILL_HORIZONTAL);
        // labelDetails.setLayoutData(data);

        displayPolicyName();
        displayEnforcement();

        new Label(this, SWT.NONE);

        diaplaySubjects();

        new Label(this, SWT.NONE);

        displayActions();

        new Label(this, SWT.NONE);

        displayResources();

        new Label(this, SWT.NONE);

        if (!getEnforcement().equals(EFFECTS[2])) {
            displayDateTime();
            new Label(this, SWT.NONE);
        }

        displayDescription();

        Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        separator.setLayoutData(data);

        displayObligations();

        setBackgroud(this);
    }

    private void displayDescription() {
        String description = policy.getDescription();

        if (description != null && description.length() != 0) {
            Composite composite = new Composite(this, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);

            Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setText("Description:");
            label.setFont(FontBundle.ARIAL_9_NORMAL);
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);

            label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setText(description);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            data = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);

            new Label(this, SWT.NONE);
        }
    }

    private void displayObligations() {
        GridData data;
        Label label = new Label(this, SWT.LEFT | SWT.WRAP);
        label.setText("Obligations:");
        label.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(data);

        if (!getEnforcement().equals(EFFECTS[2])) {
            displayObligationForType(EffectType.DENY);
        }
        displayObligationForType(EffectType.ALLOW);
    }

    @SuppressWarnings("unchecked")
    private void displayObligationForType(IDEffectType effectType) {
        GridLayout layout;
        GridData data;
        Label label;
        List<IObligation> logList = findObligations("log", effectType);
        List<IObligation> displayList = findObligations("display", effectType);
        List<IObligation> notifyList = findObligations("notify", effectType);
        List<IObligation> customList = findObligations("custom", effectType);
        if (!logList.isEmpty() || !displayList.isEmpty() || !notifyList.isEmpty() || !customList.isEmpty()) {
            Composite composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);

            label = new Label(composite, SWT.NONE);
            if (effectType.getType() == EffectType.ALLOW_TYPE) {
                label.setText("On Allow, Monitor:");
            } else if (effectType.getType() == EffectType.DENY_TYPE) {
                label.setText("On Deny:");
            }
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);

            StringBuffer result = new StringBuffer();
            if (!logList.isEmpty()) {
                result.append("Log");
            }
            if (!displayList.isEmpty()) {
                if (result.length() == 0) {
                    result.append("Display user alert");
                } else {
                    result.append(", display user alert");
                }
            }
            if (!notifyList.isEmpty()) {
                if (result.length() == 0) {
                    result.append("Send email");
                } else {
                    result.append(", send email");
                }
            }
            if (!customList.isEmpty()) {
                if (result.length() == 0) {
                    result.append("Custom obligation");
                } else {
                    result.append(", custom obligation");
                }
            }

            label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            label.setText(result.toString());
            data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);

            if (!displayList.isEmpty()) {
                composite = new Composite(this, SWT.NONE);
                data = new GridData(GridData.FILL_HORIZONTAL);
                composite.setLayoutData(data);
                layout = new GridLayout(2, false);
                layout.marginHeight = 0;
                layout.verticalSpacing = 0;
                layout.horizontalSpacing = 5;
                composite.setLayout(layout);

                label = new Label(composite, SWT.NONE);
                label.setText("Send Message:");
                label.setFont(FontBundle.ARIAL_9_NORMAL);
                data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
                label.setLayoutData(data);

                DisplayObligation displayObligation = (DisplayObligation) displayList.get(0);
                label = new Label(composite, SWT.WRAP);
                label.setFont(FontBundle.ARIAL_9_BOLD);
                data = new GridData(GridData.FILL_HORIZONTAL);
                label.setLayoutData(data);
                label.setText(displayObligation.getMessage());
            }

            if (!notifyList.isEmpty()) {
                composite = new Composite(this, SWT.NONE);
                data = new GridData(GridData.FILL_HORIZONTAL);
                composite.setLayoutData(data);
                layout = new GridLayout(2, false);
                layout.marginHeight = 0;
                layout.verticalSpacing = 0;
                layout.horizontalSpacing = 5;
                composite.setLayout(layout);

                NotifyObligation notifyObligation = (NotifyObligation) notifyList.get(0);

                label = new Label(composite, SWT.NONE);
                label.setText("Email To:");
                label.setFont(FontBundle.ARIAL_9_NORMAL);
                data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
                label.setLayoutData(data);

                label = new Label(composite, SWT.WRAP);
                label.setFont(FontBundle.ARIAL_9_BOLD);
                data = new GridData(GridData.FILL_HORIZONTAL);
                label.setLayoutData(data);
                label.setText(notifyObligation.getEmailAddresses());

                composite = new Composite(this, SWT.NONE);
                data = new GridData(GridData.FILL_HORIZONTAL);
                composite.setLayoutData(data);
                layout = new GridLayout(2, false);
                layout.marginHeight = 0;
                layout.verticalSpacing = 0;
                layout.horizontalSpacing = 5;
                composite.setLayout(layout);

                label = new Label(composite, SWT.NONE);
                label.setText("Email Message:");
                label.setFont(FontBundle.ARIAL_9_NORMAL);
                data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
                label.setLayoutData(data);

                label = new Label(composite, SWT.WRAP);
                label.setFont(FontBundle.ARIAL_9_BOLD);
                data = new GridData(GridData.FILL_HORIZONTAL);
                label.setLayoutData(data);
                label.setText(notifyObligation.getBody());
            }

            if (!customList.isEmpty()) {
                composite = new Composite(this, SWT.NONE);
                data = new GridData(GridData.FILL_HORIZONTAL);
                composite.setLayoutData(data);
                layout = new GridLayout(1, false);
                layout.marginHeight = 0;
                layout.verticalSpacing = 0;
                layout.horizontalSpacing = 5;
                composite.setLayout(layout);

                label = new Label(composite, SWT.NONE);
                label.setText("Custom Obligation:");
                label.setFont(FontBundle.ARIAL_9_NORMAL);
                data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
                label.setLayoutData(data);

                for (Object object : customList) {
                    result = new StringBuffer();
                    CustomObligation obligation = (CustomObligation) object;
                    result.append(obligation.getCustomObligationName());

                    List<String> args = (List<String>) obligation.getCustomObligationArgs();
                    if (args.size() > 0) {
                        result.append("[ ");
                    }
                    for (int i = 0, n = args.size(); i < n; i++) {
                        String arg = args.get(i);
                        if (i > 0 && i & 1 == 1) {
                            result.append("=");
                        }
                        result.append(arg);
                        if (i > 0 && i & 1 == 1) {
                            result.append(" ");
                        }
                    }
                    if (args.size() > 0) {
                        result.append("]");
                    }

                    label = new Label(composite, SWT.WRAP);
                    label.setFont(FontBundle.ARIAL_9_BOLD);
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    label.setLayoutData(data);
                    label.setText(result.toString());
                }
            }
        }
    }

    private List<IObligation> findObligations(String obligationType, IDEffectType effectType) {
        List<IObligation> obligationsToReturn = new LinkedList<IObligation>();

        Collection<IObligation> obligations = policy.getObligations(effectType);
        for (IObligation nextObligation : obligations) {
            String nextObligationType = nextObligation.getType();
            if (nextObligationType.equals(obligationType)) {
                obligationsToReturn.add(nextObligation);
            }
        }
        return obligationsToReturn;
    }

    private void displayDateTime() {
        GridLayout layout;
        GridData data;
        Composite composite;
        Label label;
        Label labelDateTime = new Label(this, SWT.LEFT | SWT.WRAP);
        labelDateTime.setText("Under Conditions:");
        labelDateTime.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelDateTime.setLayoutData(data);

        IPredicate type = getConnectionTypePredicate();
        if (type != null) {
            composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);

            label = new Label(composite, SWT.NONE);
            label.setText("Connection Type:");
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);

            label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            Long val = (Long) ((IRelation) type).getRHS().evaluate(null).getValue();
            int index = val.intValue();
            if (index == 0) {
                label.setText("Local");
            } else if (index == 1) {
                Relation rel1 = (Relation) PredicateHelpers.getConnectionSite(policy.getConditions());
                String val1 = "";
                if (rel1 != null) {
                    val1 = (String) ((IRelation) rel1).getRHS().evaluate(null).getValue();
                }
                label.setText("Remote [" + val1 + "]");
            }
        }

        composite = new Composite(this, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        label = new Label(composite, SWT.NONE);
        label.setText("Date/Time:");
        label.setFont(FontBundle.ARIAL_9_ITALIC);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(data);

        IPredicate start = getStartTimePredicate();
        if (start != null) {
            Label labelStart = new Label(composite, SWT.LEFT | SWT.WRAP);
            String text = "Starting " + getDateTime(start);
            labelStart.setFont(FontBundle.ARIAL_9_BOLD);
            labelStart.setText(text);
            data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
            labelStart.setLayoutData(data);
        } else {
            Label labelAll = new Label(composite, SWT.NONE);
            labelAll.setText("All");
            labelAll.setFont(FontBundle.ARIAL_9_BOLD);
        }

        IPredicate end = getEndTimePredicate();
        if (end != null) {
            Label labelEnd = new Label(composite, SWT.NONE);
            labelEnd.setFont(FontBundle.ARIAL_9_BOLD);
            String text = "Ending " + getDateTime(end);
            labelEnd.setText(text);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            labelEnd.setLayoutData(data);
        }

        composite = new Composite(this, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        label = new Label(composite, SWT.NONE);
        label.setText("Recurrence:");
        label.setFont(FontBundle.ARIAL_9_ITALIC);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(data);

        IPredicate scheduleFrom = getScheduleFromPredicate();
        IPredicate scheduleTo = getScheduleToPredicate();
        label = new Label(composite, SWT.WRAP);
        label.setFont(FontBundle.ARIAL_9_BOLD);
        if (scheduleFrom != null && scheduleTo != null) {
            StringBuffer buffer = new StringBuffer("Time ");
            buffer.append(getTime(scheduleFrom));
            buffer.append(" to ");
            buffer.append(getTime(scheduleTo));
            label.setText(buffer.toString());
            data = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);
        } else {
            label.setText("24/7");
        }

        StringBuffer result = new StringBuffer("Days - ");
        IPredicate dowim = PredicateHelpers.getDOWIMPredicate(policy.getConditions());
        IPredicate weekday = PredicateHelpers.getWeekDayPredicate(policy.getConditions());
        if (dowim != null) {
            IExpression exp = ((Relation) dowim).getRHS();
            Long storedCount = (Long) exp.evaluate(null).getValue();
            int count = storedCount.intValue();
            if (count == -1) {
                // assuming the last index is "last"
                count = DAY_COUNT_LABELS.length;
            }
            count--; // subtract 1 to get the array index;

            result.append("The ");
            result.append(DAY_COUNT_LABELS[count]);
            result.append(" ");

            IExpression weekdayExp = ((Relation) weekday).getRHS();
            String savedValue = weekdayExp.toString();
            // remove the quotes that this always seems to have:
            savedValue = savedValue.substring(1, savedValue.length() - 1);

            for (int i = 0; i < DAY_NAMES.length; i++) {
                if (DAY_NAMES[i].toLowerCase().equals(savedValue.toLowerCase())) {
                    result.append(DAY_NAMES[i]);
                    break;
                }
            }

            result.append(" of every month");
        } else if (weekday != null) {
            if (weekday instanceof Relation) {
                result.append(getWeekdayForExpression(((Relation) weekday).getRHS()));
            } else if (weekday instanceof CompositePredicate) {
                Iterator iter = ((CompositePredicate) weekday).predicates().iterator();
                boolean firsttime = true;
                while (iter.hasNext()) {
                    IExpression exp = ((Relation) iter.next()).getRHS();
                    if (!firsttime) {
                        result.append(", ");
                    }
                    result.append(getWeekdayForExpression(exp));
                    firsttime = false;
                }
            }
        } else {
            IPredicate day = PredicateHelpers.getDayOfMonthPredicate(policy.getConditions());
            if (day != null) { // setup day section
                String dayText = ((Relation) day).getRHS().toString();
                result.append("Day ");
                result.append(dayText);
                result.append(" of every month");
            }
        }

        if (result.length() > 7) {
            composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);

            label = new Label(composite, SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            data = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);
            label.setText(result.toString());
        }
    }

    private String getWeekdayForExpression(IExpression exp) {
        Long i = (Long) exp.evaluate(null).getValue();
        return DAY_NAMES[i.intValue() - 1];
    }

    private void displayResources() {
        GridData data;
        GridLayout layout;
        Label labelOnResource = new Label(this, SWT.LEFT | SWT.WRAP);
        labelOnResource.setText("On Resource(s):");
        labelOnResource.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelOnResource.setLayoutData(data);

        CompositePredicate fromResources = getFromSources();
        List<CompositePredicate> fromList = filterPredicate(fromResources);
        boolean isEmpty = true;
        if (!fromList.isEmpty()) {
            Composite composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            composite.setLayout(layout);

            getUserSubjectLabel(composite, operators1, fromList);
            isEmpty = false;
        }

        CompositePredicate toSources = getToSources();
        List<CompositePredicate> toList = filterPredicate(toSources);
        if (!toList.isEmpty()) {
            Composite composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            composite.setLayout(layout);

            if (isEmpty) {
                getUserSubjectLabel(composite, operators21, toList);
            } else {
                getUserSubjectLabel(composite, operators22, toList);
            }
        }

    }

    private void displayActions() {
        GridData data;

        Label labelPerform = new Label(this, SWT.LEFT | SWT.WRAP);
        labelPerform.setText("From performing the Action(s):");
        labelPerform.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelPerform.setLayoutData(data);

        CompositePredicate actions = getActions();
        List<CompositePredicate> actionList = filterPredicate(actions);
        if (!actionList.isEmpty()) {
            Label labelAction = new Label(this, SWT.LEFT | SWT.WRAP);
            labelAction.setText(getActionLabel("", actionList));
            labelAction.setFont(FontBundle.ARIAL_9_BOLD);
            data = new GridData(GridData.FILL_HORIZONTAL);
            labelAction.setLayoutData(data);
        }
    }

    private void diaplaySubjects() {
        GridLayout layout;
        GridData data;
        Composite composite = new Composite(this, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Label labelSubjects = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelSubjects.setText("Subject(s): ");
        labelSubjects.setFont(FontBundle.ARIAL_9_NORMAL);
        data = new GridData();
        labelSubjects.setLayoutData(data);

        CompositePredicate users = getUsers();
        List<CompositePredicate> userList = filterPredicate(users);
        boolean isEmpty = true;
        if (!userList.isEmpty()) {
            Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setText("Users");
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            isEmpty = false;
            data = new GridData();
            label.setLayoutData(data);

            getUserSubjectLabel(composite, operators1, userList);
        }

        CompositePredicate computers = getComputers();
        List<CompositePredicate> computerList = filterPredicate(computers);
        if (!computerList.isEmpty()) {
            composite = new Composite(this, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(data);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            composite.setLayout(layout);

            Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            if (isEmpty) {
                label.setText("Computers");
                isEmpty = false;
            } else {
                label.setText("And Computers");
            }
            data = new GridData();
            label.setLayoutData(data);

            getUserSubjectLabel(composite, operators1, computerList);
        }

        if (!policy.hasAttribute("access")) {
            CompositePredicate applications = getApplications();
            List<CompositePredicate> applicationList = filterPredicate(applications);
            if (!applicationList.isEmpty()) {
                composite = new Composite(this, SWT.NONE);
                data = new GridData(GridData.FILL_HORIZONTAL);
                composite.setLayoutData(data);
                layout = new GridLayout(2, false);
                layout.marginHeight = 0;
                layout.verticalSpacing = 0;
                layout.horizontalSpacing = 0;
                composite.setLayout(layout);

                Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
                label.setFont(FontBundle.ARIAL_9_ITALIC);
                if (isEmpty) {
                    label.setText("Applications");
                    isEmpty = false;
                } else {
                    label.setText("And Applications");
                }

                data = new GridData();
                label.setLayoutData(data);

                getUserSubjectLabel(composite, operators1, applicationList);
            }
        }
    }

    private void displayPolicyName() {
        GridLayout layout;
        GridData data;
        Composite composite = new Composite(this, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);

        Label labelPolicyImage = new Label(composite, SWT.NONE);
        labelPolicyImage.setImage(ImageBundle.POLICY_IMG);
        data = new GridData(GridData.BEGINNING);
        labelPolicyImage.setLayoutData(data);

        Label labelPolicyName = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelPolicyName.setFont(FontBundle.ARIAL_9_ITALIC);
        labelPolicyName.setText(getPolicyName() + " [Document Policy]");
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelPolicyName.setLayoutData(data);
    }

    private void displayEnforcement() {
        GridLayout layout;
        GridData data;
        Composite composite = new Composite(this, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Enforcement:");
        label.setFont(FontBundle.ARIAL_9_NORMAL);

        Label labelEnforcement = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelEnforcement.setFont(FontBundle.ARIAL_9_BOLD);
        labelEnforcement.setText(getEnforcement());
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelEnforcement.setLayoutData(data);
    }

    private String getTime(IPredicate predicate) {
        Constant fromConst = (Constant) ((Relation) predicate).getRHS();

        Date fromDate = null;
        try {
            fromDate = DateFormat.getTimeInstance().parse(unquote(fromConst.getRepresentation()));
            Format formatter = new SimpleDateFormat("hh:mm a");
            return formatter.format(fromDate);
        } catch (ParseException e) {
            LoggingUtil.logError(Activator.ID, "error get time", e);
            return "";
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

    private String getDateTime(IPredicate predicate) {
        Long dateVal = (Long) ((IRelation) predicate).getRHS().evaluate(null).getValue();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateVal.longValue());

        Format formatter = new SimpleDateFormat("MM/dd/yy hh:mm a");
        return formatter.format(calendar.getTime());
    }

    private void getUserSubjectLabel(Composite parent, String[] operators, List<CompositePredicate> predicates) {
        boolean firsttime = true;
        for (CompositePredicate predicate : predicates) {
            Composite container = new Composite(parent, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            container.setLayoutData(data);
            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;
            container.setLayout(layout);

            String operator = getOperator(operators, predicate);
            if (PredicateHelpers.isNegationPredicate(predicate)) {
                predicate = (CompositePredicate) predicate.predicateAt(0);
            }
            Label label = new Label(container, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_ITALIC);
            if (firsttime) {
                label.setText(operator.trim());
                firsttime = false;
            } else {
                label.setText(("And" + operator).trim());
            }
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            label.setLayoutData(data);

            StringBuffer result = new StringBuffer();
            boolean inner_firsttime = true;
            for (int i = 0; i < predicate.predicateCount(); i++) {
                Object predicateElement = predicate.predicateAt(i);
                if (!(predicateElement instanceof PredicateConstants)) {
                    if (inner_firsttime) {
                        inner_firsttime = false;
                    } else {
                        result.append(" OR ");
                    }
                    result.append(findLabel((IPredicate) predicateElement));
                }
            }
            label = new Label(container, SWT.LEFT | SWT.WRAP);
            label.setFont(FontBundle.ARIAL_9_BOLD);
            if (result.indexOf(" OR ") != -1) {
                label.setText("(" + result.toString() + ")");
            } else {
                label.setText(result.toString());
            }
            data = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(data);
        }
    }

    private String getActionLabel(String name, List<CompositePredicate> predicates) {
        StringBuffer result = new StringBuffer(name);

        for (CompositePredicate predicate : predicates) {
            for (int i = 0; i < predicate.predicateCount(); i++) {
                Object predicateElement = predicate.predicateAt(i);
                if (!(predicateElement instanceof PredicateConstants)) {
                    result.append(findLabel((IPredicate) predicateElement));
                    result.append(", ");
                }
            }
        }

        if (result.length() > 2) {
            result.deleteCharAt(result.length() - 1);
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    private String findLabel(IPredicate spec) {
        String labelName = "";
        if (spec instanceof IDSpecRef) {
            IDSpecRef specRef = (IDSpecRef) spec;
            labelName = specRef.getReferencedName();
            labelName = labelName.substring(labelName.indexOf(PQLParser.SEPARATOR) + 1);
        } else if (spec instanceof Relation) {
            IExpression lhs = ((Relation) spec).getLHS();
            IExpression rhs = ((Relation) spec).getRHS();
            Object rhsValue = rhs.evaluate(null).getValue();
            if (lhs instanceof IDSubjectAttribute) {
                if (DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.containsKey(lhs) && rhsValue instanceof Long) {
                    LeafObjectType associatedLeafObjectType = (LeafObjectType) DESTINY_ID_SUBJECT_ATTR_TO_LEAF_OBJECT_TYPE_MAP.get(lhs);
                    LeafObject leafObject = EntityInfoProvider.getLeafObjectByID((Long) rhsValue, associatedLeafObjectType);
                    if (leafObject != null) {
                        labelName = leafObject.getName();
                    } else {
                        labelName = "Entry Not Found - " + rhsValue;
                    }
                } else {
                    // Done for consistency with earlier implement to reduce
                    // risk
                    labelName = "" + rhsValue;
                }
            } else if (lhs instanceof ResourceAttribute && rhs instanceof Constant) { /* PATH */
                String val = ((Constant) rhs).getRepresentation();
                String res = val.replaceAll("[\\\\]+", "\\\\");
                labelName = val.startsWith("\\\\") ? "\\" + res : res;
            } else {
                // Catch all
                if (rhs instanceof Constant) {
                    labelName = ((Constant) rhs).getRepresentation();
                } else {
                    labelName = "" + rhsValue;
                }
            }
        }

        return labelName;
    }

    private List<CompositePredicate> filterPredicate(CompositePredicate predicate) {
        List<CompositePredicate> filteredPredicate = new ArrayList<CompositePredicate>();
        for (int i = 0; i < predicate.predicateCount(); i++) {
            IPredicate spec = (IPredicate) predicate.predicateAt(i);
            if (spec instanceof CompositePredicate) {
                filteredPredicate.add((CompositePredicate) spec);
            }
        }
        return filteredPredicate;
    }

    private String getOperator(String[] operators, CompositePredicate predicate) {
        if (PredicateHelpers.isNegationPredicate(predicate)) {
            return operators[1];
        } else {
            return operators[0];
        }
    }

    private String getEnforcement() {
        IEffectType effect = policy.getMainEffect();
        IEffectType otherwise = policy.getOtherwiseEffect();

        int index = PolicyHelpers.getIndexForEffect(effect, otherwise);
        if (index >= 0) {
            return EFFECTS[index];
        }

        return "";
    }

    private String getPolicyName() {
        String name = policy.getName();
        int index = name.lastIndexOf(PQLParser.SEPARATOR);
        if (index < 0) {
            return name;
        }
        return name.substring(index + 1);
    }

    private void setBackgroud(Control parent) {
        parent.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        if (parent instanceof Composite) {

            for (Control control : ((Composite) parent).getChildren()) {
                setBackgroud(control);
            }
        }
    }

    private CompositePredicate getUsers() {
        ITarget target = policy.getTarget();
        CompositePredicate subject = (CompositePredicate) target.getSubjectPred();
        return (CompositePredicate) subject.predicateAt(0);
    }

    private CompositePredicate getComputers() {
        ITarget target = policy.getTarget();
        CompositePredicate subject = (CompositePredicate) target.getSubjectPred();
        return (CompositePredicate) subject.predicateAt(1);
    }

    private CompositePredicate getApplications() {
        ITarget target = policy.getTarget();
        CompositePredicate subject = (CompositePredicate) target.getSubjectPred();
        return (CompositePredicate) subject.predicateAt(2);
    }

    private CompositePredicate getActions() {
        ITarget target = policy.getTarget();
        return (CompositePredicate) target.getActionPred();
    }

    private CompositePredicate getFromSources() {
        ITarget target = policy.getTarget();
        return (CompositePredicate) target.getFromResourcePred();
    }

    private CompositePredicate getToSources() {
        ITarget target = policy.getTarget();
        return (CompositePredicate) target.getToResourcePred();
    }

    private IPredicate getConnectionTypePredicate() {
        return PredicateHelpers.getConnectionType(policy.getConditions());
    }

    private IPredicate getStartTimePredicate() {
        return PredicateHelpers.getStartTime(policy.getConditions());
    }

    private IPredicate getEndTimePredicate() {
        return PredicateHelpers.getEndTime(policy.getConditions());
    }

    private IPredicate getScheduleFromPredicate() {
        IPredicate condition = policy.getConditions();
        return PredicateHelpers.getDailyFromTime(condition);
    }

    private IPredicate getScheduleToPredicate() {
        IPredicate condition = policy.getConditions();
        return PredicateHelpers.getDailyToTime(condition);
    }
}
