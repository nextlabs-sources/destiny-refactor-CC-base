package com.bluejungle.destiny.container.dac.alert.hibernateimpl;

import com.bluejungle.destiny.container.dac.alert.IInquiryAlertManager;
import com.bluejungle.destiny.container.dac.alert.IInquiryAlert;
import com.bluejungle.destiny.container.dac.alert.IInquiryAlertData;
import java.util.List;

public class HibernateInquiryAlertManager implements IInquiryAlertManager {

    /**
     * @see com.bluejungle.destiny.container.dac.alert.IInquiryAlertManager#createInquiryAlert(com.bluejungle.destiny.container.dac.alert.IInquiryAlertData)
     */
    public IInquiryAlert createInquiryAlert(IInquiryAlertData inqueryAlertData) {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.alert.IInquiryAlertManager#getInquiryAlerts(java.lang.Long)
     */
    public List getInquiryAlerts(Long userId) {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.alert.IInquiryAlertManager#getInquiryAlertById(java.lang.Long)
     */
    public IInquiryAlert getInquiryAlertById(Long id) {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.alert.IInquiryAlertManager#deleteAlertById(java.lang.Long)
     */
    public void deleteAlertById(Long id) {
    }

}
 
