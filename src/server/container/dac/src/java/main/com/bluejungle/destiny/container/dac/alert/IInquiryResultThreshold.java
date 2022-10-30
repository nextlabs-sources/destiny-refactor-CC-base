package com.bluejungle.destiny.container.dac.alert;

import com.bluejungle.framework.utils.TimeInterval;

public interface IInquiryResultThreshold {

	public abstract TimeInterval getTimeInterval();
	public abstract void setTimeInterval(TimeInterval timeInterval);
	public abstract Integer getLimit();
	public abstract void setLimit(Integer limitToSet);
}
 
