/*
 * Created on May 5, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class TimeControl extends Composite {

    final static private String AM = "AM";
    final static private String PM = "PM";

    private Text hourText = null;
    private Text minuteText = null;
    private Text ampmText = null;
    private Button upButton = null;
    private Button downButton = null;
    private Control currentFocus = null;
    private boolean disableEvents = false;
    private List<ModifyListener> modifyListenerArray = new ArrayList<ModifyListener>();

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public TimeControl(Composite parent, int style) {
        super(parent, style);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        initialize();
    }

    private String formatHour12(int number) {
        number %= 12;
        if (number <= 0) {
            return "12";
        } else if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    /**
     * @param number
     *            number to format
     * 
     * @return returns 2 character string reprsenting number
     */
    private String formatMinute60(int number) {
        number %= 60;
        if (number <= 0) {
            return "00";
        } else if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    /**
     * 
     */
    private void initialize() {
        Calendar currentTime = new GregorianCalendar();
        hourText = new Text(this, SWT.SINGLE);
        minuteText = new Text(this, SWT.SINGLE);
        ampmText = new Text(this, SWT.SINGLE);
        hourText.setTextLimit(2);
        minuteText.setTextLimit(2);
        ampmText.setTextLimit(2);

        FocusAdapter focusAdaptor = new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                currentFocus = (Control) e.getSource();
                Text text = (Text) currentFocus;
                text.selectAll();
            }

            public void focusLost(FocusEvent e) {
                Text text = (Text) e.getSource();
                if (text == hourText) {
                    String textToSet;
                    try {
                        textToSet = formatHour12(Integer.parseInt(text.getText()));
                    } catch (NumberFormatException nfe) {
                        textToSet = "12";
                    }
                    disableEvents = true;
                    text.setText(textToSet);
                    disableEvents = false;
                } else if (text == minuteText) {
                    String textToSet;
                    try {
                        textToSet = formatMinute60(Integer.parseInt(text.getText()));
                    } catch (NumberFormatException nfe) {
                        textToSet = "00";
                    }
                    disableEvents = true;
                    text.setText(textToSet);
                    disableEvents = false;
                } else if (text == ampmText) {
                    String ampm = text.getText();
                    if (!ampm.equals(AM) && !ampm.equals(PM)) {
                        ampmText.setText(AM);
                    }
                }
            }
        };

        hourText.addFocusListener(focusAdaptor);
        minuteText.addFocusListener(focusAdaptor);
        ampmText.addFocusListener(focusAdaptor);
        hourText.setText(formatHour12(currentTime.get(Calendar.HOUR)));
        minuteText.setText(formatMinute60(currentTime.get(Calendar.MINUTE)));
        int ampm = currentTime.get(Calendar.AM_PM);
        if (ampm == Calendar.AM) {
            ampmText.setText(AM);
        } else {
            ampmText.setText(PM);
        }

        Label colonLabel = new Label(this, SWT.NONE);
        colonLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        colonLabel.setText(":");
        upButton = new Button(this, SWT.ARROW);
        upButton.setAlignment(SWT.UP);
        downButton = new Button(this, SWT.ARROW);
        downButton.setAlignment(SWT.DOWN);

        upButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (currentFocus == hourText) {
                    int hour = Integer.parseInt(hourText.getText());
                    hour = hour % 12 + 1;
                    hourText.setText(formatHour12(hour));
                } else if (currentFocus == minuteText) {
                    int minute = Integer.parseInt(minuteText.getText());
                    minute = (minute + 1) % 60;
                    minuteText.setText(formatMinute60(minute));
                } else if (currentFocus == ampmText) {
                    toggleAMPM();
                }
                currentFocus.setFocus();
            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (currentFocus == hourText) {
                    int hour = Integer.parseInt(hourText.getText());
                    hour = hour - 1;
                    if (hour == 0) {
                        hour = 12;
                    }
                    hourText.setText(formatHour12(hour));
                } else if (currentFocus == minuteText) {
                    int minute = Integer.parseInt(minuteText.getText());
                    minute = minute - 1;
                    if (minute == -1) {
                        minute = 59;
                    }
                    minuteText.setText(formatMinute60(minute));
                } else if (currentFocus == ampmText) {
                    toggleAMPM();
                }
                currentFocus.setFocus();
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.marginHeight = 0;
        formLayout.marginWidth = 0;
        formLayout.spacing = 0;
        setLayout(formLayout);

        FormData formData = new FormData();
        formData.width = 16;
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(0);
        formData.bottom = new FormAttachment(100);
        hourText.setLayoutData(formData);

        formData = new FormData();
        formData.width = 5;
        formData.left = new FormAttachment(hourText, 0, SWT.RIGHT);
        formData.top = new FormAttachment(0);
        formData.bottom = new FormAttachment(100);
        colonLabel.setLayoutData(formData);

        formData = new FormData();
        formData.width = 16;
        formData.left = new FormAttachment(colonLabel, 0, SWT.RIGHT);
        formData.top = new FormAttachment(0);
        formData.bottom = new FormAttachment(100);
        minuteText.setLayoutData(formData);

        formData = new FormData();
        formData.width = 20;
        formData.left = new FormAttachment(minuteText, 0, SWT.RIGHT);
        formData.top = new FormAttachment(0);
        formData.bottom = new FormAttachment(100);
        ampmText.setLayoutData(formData);

        formData = new FormData();
        formData.height = 9;
        formData.width = 15;
        // formData.left = new FormAttachment(ampmText, 0, SWT.RIGHT);
        formData.top = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        upButton.setLayoutData(formData);

        formData = new FormData();
        formData.height = 9;
        formData.width = 15;
        // formData.left = new FormAttachment(ampmText, 0, SWT.RIGHT);
        formData.top = new FormAttachment(upButton, 0, SWT.BOTTOM);
        formData.bottom = new FormAttachment(ampmText, 0, SWT.BOTTOM);
        formData.right = new FormAttachment(100);
        downButton.setLayoutData(formData);

        // Add modify listeners when everything is ready.
        ModifyListener modifyListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                fireModifiedListener();
            }
        };

        hourText.addModifyListener(modifyListener);
        minuteText.addModifyListener(modifyListener);
        ampmText.addModifyListener(modifyListener);

        currentFocus = hourText;
    }

    /**
     * 
     */
    protected void toggleAMPM() {
        if (ampmText.getText().equals(AM)) {
            ampmText.setText(PM);
        } else if (ampmText.getText().equals(PM)) {
            ampmText.setText(AM);
        }
    }

    /**
     * Sets the time on the time control
     * 
     * @param hours
     *            hours in 24 hour format
     * @param minutes
     */
    public void setTime(int hours, int minutes) {

        disableEvents = true;

        int hour = hours % 12;
        if (hour == 0) {
            hour += 12;
        }

        hourText.setText(formatHour12(hour));
        minuteText.setText(formatMinute60(minutes));
        if (hours < 12) {
            ampmText.setText(AM);
        } else {
            ampmText.setText(PM);
        }

        disableEvents = false;
        fireModifiedListener();

    }

    /**
     * Sets the time on the time control
     * 
     * @param hours
     *            hours in 12 hour format
     * @param minutes
     * @param ampm
     *            Calendar.AM or Calendar.PM
     */
    public void setTime(int hours, int minutes, int ampm) {
        disableEvents = true;

        hourText.setText(formatHour12(hours));
        minuteText.setText(formatMinute60(minutes));
        if (ampm == Calendar.AM) {
            ampmText.setText(AM);
        } else {
            ampmText.setText(PM);
        }

        disableEvents = false;
        fireModifiedListener();
    }

    /**
     * @return hours in 12 hour format
     */
    public int getHours12() {
        return Integer.parseInt(hourText.getText());
    }

    /**
     * @return hours in 24 hour format
     */
    public int getHours() {
        int hour = Integer.parseInt(hourText.getText());
        int ampm = getAMPM();
        if (hour == 12) {
            hour -= 12;
        }
        if (ampm == Calendar.PM) {
            hour += 12;
        }

        return hour;
    }

    /**
     * @return
     */
    public int getMinutes() {
        return Integer.parseInt(minuteText.getText());
    }

    /**
     * @return current value of AMPM as Calendar.AM or Calendar.PM
     */
    public int getAMPM() {
        if (ampmText.getText().equals(AM)) {
            return Calendar.AM;
        } else {
            return Calendar.PM;
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hourText.setEnabled(enabled);
        hourText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        minuteText.setEnabled(enabled);
        minuteText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        ampmText.setEnabled(enabled);
        ampmText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        upButton.setEnabled(enabled);
        downButton.setEnabled(enabled);
    }

    public void addModifyListener(ModifyListener listener) {
        if (!modifyListenerArray.contains(listener)) {
            modifyListenerArray.add(listener);
        }
    }

    public void removeModifyListener(ModifyListener listener) {
        if (modifyListenerArray.contains(listener)) {
            modifyListenerArray.remove(listener);
        }
    }

    private void fireModifiedListener() {
        if (disableEvents) {
            return;
        }

        Event e = new Event();
        e.widget = this;
        ModifyEvent modifyEvent = new ModifyEvent(e);

        for (ModifyListener listener : modifyListenerArray) {
            listener.modifyText(modifyEvent);
        }
    }

    /**
     * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
     */
    public Point computeSize(int wHint, int hHint) {
        return new Point(Math.max(100, wHint), Math.max(20, hHint));
    }

    /**
     * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
     */
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return computeSize(wHint, hHint);
    }
}