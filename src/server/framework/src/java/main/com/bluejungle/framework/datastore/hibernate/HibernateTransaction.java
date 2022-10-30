package com.bluejungle.framework.datastore.hibernate;

import com.bluejungle.framework.datastore.IDataStoreSession;
import com.bluejungle.framework.datastore.ITransaction;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Transaction;


// Copyright Blue Jungle, Inc.

/**
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/HibernateTransaction.java#1 $
 */

public class HibernateTransaction 
implements ITransaction {
    private Transaction tx;
    private IDataStoreSession session;
    
    HibernateTransaction(Transaction tx, IDataStoreSession session)
    {
        this.tx = tx;
        this.session = session;
    }
    
    public void commit()
    {
        try
        {
            tx.commit();
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            throw new RuntimeException(he);
        }
    }
    
    public void rollback()
    {
        try
        {
            tx.rollback();
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            throw new RuntimeException(he);
        }
    }
    
    public IDataStoreSession getSession()
    {
        return session;
    }
    
}
