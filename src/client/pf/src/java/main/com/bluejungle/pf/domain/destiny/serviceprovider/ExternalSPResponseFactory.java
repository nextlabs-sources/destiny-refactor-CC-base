/*
 * Created on Feb 19, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.serviceprovider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/ExternalSPResponseFactory.java#1 $
 */

public class ExternalSPResponseFactory {
    private final Map<Class<?>, String> typeMap;
    
    public ExternalSPResponseFactory(){
        typeMap = new HashMap<Class<?>, String>();
        initTypeMap(typeMap);
    }
    
    protected void initTypeMap(Map<Class<?>, String> typeMap){
        typeMap.put(Integer.class,  "i");       //CEinit32
        typeMap.put(Boolean.class,  "b");       //CEBoolean
        typeMap.put(String.class,   "s");       //CEString
        typeMap.put(String[].class, "a");       //CEAttributes
        typeMap.put(String[][].class,  "[a");   //array of CEAttributes
    }
    
    protected void register(Class<?> type, String shortForm) throws IllegalArgumentException {
        String existing = typeMap.get(type);
        if (existing != null) {
            throw new IllegalArgumentException("Type '" + type
                    + "' is already registered. The exising value is '" + existing
                    + "'. The new value is '" + shortForm + "'.");
        }
                            
        typeMap.put(type, shortForm);
    }
    
    
    public IDynamicExternalSPResponse create(Object...objects){
        IDynamicExternalSPResponse response = new ExternalResponse();
        for(Object object : objects){
            response.add(object);
        }
        return response;
    }
    
    private class ExternalResponse implements IDynamicExternalSPResponse {
        private List<Object> data;
        private StringBuilder format;

        ExternalResponse() {
            data = new LinkedList<Object>();
            format = new StringBuilder();
        }

        @SuppressWarnings("unchecked")
        public <T> void add(T value) {
            this.add((Class<T>) value.getClass(), value);
        }

        public <T> void add(Class<? super T> clazz, T value) {
            String shortForm = typeMap.get(clazz);
            if (shortForm == null) {
                throw new IllegalArgumentException("Type '" + clazz + "' is not defined.");
            }

            data.add(value);
            format.append(shortForm);
        }

        public Object[] getData() {
            return data.toArray();
        }

        public String getFormatString() {
            return format.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            for(Object o : data){
                if( o != null){
                    sb.append(o.getClass().getSimpleName())
                      .append(": ")
                      .append(o.toString());
                }else{
                    sb.append("null: null");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
