package com.bluejungle.destiny.container.dac.alert;

import java.util.Calendar;

public interface IInquiryAlertLogEntry {
 
	public abstract Long getInquiryAlertId();
	public abstract String getMessage();
	public abstract String getMessageSubject();
	public abstract Calendar getTimestamp();
}
 
