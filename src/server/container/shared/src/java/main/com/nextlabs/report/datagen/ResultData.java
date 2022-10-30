/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.report.datagen;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/report/datagen/ResultData.java#1 $
 */

public class ResultData {
    private final Object[][] values;
    private final String[] name;
    
    public ResultData(String[] name, Object[][] values) {
        this.values = values;
        this.name = name;
    }
    
    public int getRowCount(){
        return name.length;
    }

    public int size(){
        return values.length;
    }
    
    public String getName(int index){
        return name[index];
    }
    
    public Object get(int row, int column){
        return values[row][column];
    }
}

