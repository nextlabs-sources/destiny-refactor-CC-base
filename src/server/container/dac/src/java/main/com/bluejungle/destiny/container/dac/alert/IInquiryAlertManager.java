package com.bluejungle.destiny.container.dac.alert;

import java.util.List;

public interface IInquiryAlertManager {
	public abstract IInquiryAlert createInquiryAlert(IInquiryAlertData inqueryAlertData);
	public abstract List getInquiryAlerts(Long userId);
	public abstract IInquiryAlert getInquiryAlertById(Long id);
	public abstract void deleteAlertById(Long id);
}
 
