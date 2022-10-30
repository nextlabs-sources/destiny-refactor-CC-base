package com.bluejungle.destiny.container.dac.alert;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;

public interface IInquiryAlert {
	public abstract Long getId();
	public abstract String getTitle();
	public abstract void setTitle(String titleToSet);
	public abstract IInquiry getInquery();
	public abstract IInquiry setInquery(IInquiry inqueryToSet);
	public abstract IInquiryResultThreshold getThreshold();
	public abstract void setThreshold(IInquiryResultThreshold threshold);
	public abstract ReportSummaryType getGroupBy();
	public abstract void setGroupBy(ReportSummaryType summaryType);
	public abstract String getMessageTemplate();
	public abstract void setMessageTemplate(String messageTemplateToSet);
	public abstract IMessageHandlerConfig getMessagingConfig();
	public abstract void setMessagingConfig(IMessageHandlerConfig config);
	public abstract void save();
	public abstract void delete();
}
 
