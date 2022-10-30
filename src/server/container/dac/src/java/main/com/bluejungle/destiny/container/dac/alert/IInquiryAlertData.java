package com.bluejungle.destiny.container.dac.alert;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;

public interface IInquiryAlertData {

    public abstract String getTitle();

    public abstract IInquiry getInquiry();

    public abstract IInquiryResultThreshold getThreshold();

    public abstract ReportSummaryType getGroupBy();

    public abstract String getMessageTemplate();

    public abstract IMessageHandlerConfig getMessagingConfig();
}

