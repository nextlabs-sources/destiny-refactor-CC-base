package com.nextlabs.framework.messaging.handlers;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;
import com.nextlabs.framework.messaging.MessagingException;

public abstract class BaseMessageHandler implements IMessageHandler {
    protected static final String TEMPLATE = "The value of property \"%s\" %s";
    
    protected static final String INVALID_FORMAT = "The value of property \"%s\", %s, is not in valid %s format.";
    
    private String name;
    
    protected IMessageHandlerConfig config;
    
    //have to have a default constructor
    public BaseMessageHandler(){
        
    }
    
    public String getName() {
        return name;
    }
    
    public void init(IMessageHandlerConfig config, String name) throws MessagingException {
        this.name = name;
        this.config = filter(config);
        init(this.config);
    }
    
    /**
     * remove all unnecessary config and convert the value to correct type.
     * @param config
     * @return
     */
    protected IMessageHandlerConfig filter(IMessageHandlerConfig config) throws MessagingException{
        return config;
    }
    
    protected abstract void init(IMessageHandlerConfig config) throws MessagingException;
    
    public <T> T getProperty(PropertyKey<T> key) {
        return config != null ? config.getProperty(key) : null;
    }

    protected <T> T getMustProperty(PropertyKey<T> key, IMessageHandlerConfig messageConfig)
            throws MessagingException {
        T obj = messageConfig.getProperty(key);
        if (obj == null) {
            throw new MessagingException(key, new NullPointerException("missing value of " + key));
        } 
        return obj;
    }
    
    protected Boolean getBoolean(PropertyKey<Boolean> key, IMessageHandlerConfig config)
            throws MessagingException {
        Object obj = config.getProperty(key);
    if (obj == null) {
            return null;
        }
        Boolean b;
        if (obj instanceof Boolean) {
            b = (Boolean) obj;
        } else if (obj instanceof String) {
            b = StringUtils.stringToBoolean((String)obj);
            if (b == null) {
                throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Boolean"),
                        MessagingException.Type.CONFIG);
            }
        } else {
            throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Boolean"),
                    MessagingException.Type.CONFIG);
        }
        return b;
    }
    
    protected Integer getInteger(PropertyKey<Integer> key, IMessageHandlerConfig config)
            throws MessagingException {
        Object obj = config.getProperty(key);
        if (obj == null) {
            return null;
        }
        Integer i;
        if (obj instanceof Number) {
            i = ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                i = Integer.parseInt((String)obj);
            } catch (NumberFormatException e) {
                throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Integer"),
                        MessagingException.Type.CONFIG, e);
            }
        } else {
            throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Integer"),
                    MessagingException.Type.CONFIG);
        }
        return i;
    }
}
