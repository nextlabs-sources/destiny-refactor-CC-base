package com.bluejungle.pf.domain.destiny.obligation;

import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;


// Copyright Blue Jungle, Inc.

/*
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/DObligationManager.java#1 $
 */

public class DObligationManager implements IDObligationManager { 

    public static final String CLASSNAME = DObligationManager.class.getName();
    
    public static final ComponentInfo<IDObligationManager> COMP_INFO =
            new ComponentInfo<IDObligationManager>(
                IDObligationManager.CLASSNAME, 
                DObligationManager.class, 
                IDObligationManager.class, 
                LifestyleType.SINGLETON_TYPE);        

    public DObligationManager() {
    }
    
    /**
     * @see com.bluejungle.pf.domain.destiny.obligation.IDObligationManager#getObligation(java.lang.String, java.lang.Long)
     */
    public LogObligation createLogObligation() {
        return new LogObligation();
    }

    /**
     * @see com.bluejungle.pf.domain.destiny.obligation.IDObligationManager#createNotifyObligation(java.util.Collection, java.lang.String)
     */
    public NotifyObligation createNotifyObligation(String emailAddresses, String body) {
        emailAddresses = sanitizeList( emailAddresses );
        emailAddresses = validateAddresses(emailAddresses);
        return new NotifyObligation( emailAddresses, body );
    }

    protected String validateAddresses(String emailAddresses) {
        StringBuffer validatedAddresses = new StringBuffer ();
        InternetAddress[] addrs;        
        StringTokenizer tokenizer = new StringTokenizer (emailAddresses, ",");
        boolean firstAddress = true;

        while ( tokenizer.hasMoreTokens() ) {
            try {
                addrs = InternetAddress.parse( (String)tokenizer.nextElement(), false );
                for ( int i = 0 ; i < addrs.length ; i++ ) {
                    addrs[i].validate();
                    if ( !firstAddress ) {
                        validatedAddresses.append( "," );
                    } else {
                        firstAddress = false;
                    }
                    validatedAddresses.append( addrs[i].getAddress() );
                }
            } catch ( AddressException e ) {
                continue;
            }
        }
        return validatedAddresses.toString ();
    }

    /**
     * E-mail list standards are too strict -- this method will give
     * users some slack in what they can enter:
     * -- whitespace characters will be treated as separators
     * -- semicolons will be accepted as separators
     * -- multiple separators will be allowed
     * @param emailList the incoming list of emails.
     * @return sanitized list of e-mails.
     */
    protected static String sanitizeList( String emailList ) {
        return emailList
              .replaceAll( "\\s|;", "," )
              .replaceAll( ",+", "," )
              .replaceAll( "^,+", "" )
              .replaceAll( ",+$", "" );
    }

    public CustomObligation createCustomObligation(String pqlName, List arguments) {
        return new CustomObligation(pqlName, arguments);
    }

    public DisplayObligation createDisplayObligation(String message) {
        return new DisplayObligation(message);
    }

}
