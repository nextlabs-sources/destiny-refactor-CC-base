/*
 * Created on Jun 22, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;

import com.nextlabs.shared.tools.StringFormatter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/BufferedTable.java#1 $
 */

/**
 * This is similar to <code>Table</code> except the column width is flexible.
 * It will put the data within the column and don't go over the maxLength
 */
public class BufferedTable extends Table {
    
    public BufferedTable(){
        super(true);
    }
    
    protected class FlexibleColumn extends Column{
        protected final int maxLength;
        protected int currentMaxlength;
        
        public FlexibleColumn(String name, int maxLength) {
            super(name, name.length());
            this.maxLength = maxLength;
            currentMaxlength = 0;
        }
        
        protected int getLength(){
            int m = Math.min(maxLength, currentMaxlength);
            if( m < name.length()){
                m = name.length();
            }
            return m + 1;
        }
    }

    @Override
    public int addColumn(String name, int maxLength) {
        if(lock){
            throw new IllegalArgumentException("Column can't be added after row(s) have been inserted");
        }
        Column column = new FlexibleColumn(name, maxLength);
        table.add(column);
        return table.indexOf(column);
    }

    @Override
    protected String[] formatDate(String s, Column c) {
        ((FlexibleColumn)c).currentMaxlength = Math.max(((FlexibleColumn)c).currentMaxlength, s.length());
        return new String[]{s};
    }
    
    @Override
    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.size(); i++) {
            FlexibleColumn c = (FlexibleColumn) table.get(i);
            sb.append(StringFormatter.fitLength(c.name, c.getLength()));

            if (i != table.size() - 1) {
                sb.append(columnSeparator);
            }
        }
        
        sb.append(ConsoleDisplayHelper.NEWLINE);
        
        if (headerRowSeaperator == null) {
            StringBuilder headerRowSeaperator = new StringBuilder();
            for (Column c : table) {
                headerRowSeaperator.append(StringFormatter.repeat('-', ((FlexibleColumn)c).getLength()));
                headerRowSeaperator.append('+');
            }
            headerRowSeaperator.deleteCharAt(headerRowSeaperator.length() - 1);
            headerRowSeaperator.append(ConsoleDisplayHelper.NEWLINE);
            this.headerRowSeaperator = headerRowSeaperator.toString();
        }

        sb.append(headerRowSeaperator);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String[][] row = new String[table.size()][];
            int maxSubRow = Integer.MIN_VALUE;
            for (int columnIndex = 0; columnIndex < table.size(); columnIndex++) {
                Column c = table.get(columnIndex);
                String unformattedRow = c.data.get(rowIndex)[0];
                row[columnIndex] = super.formatDate(unformattedRow, c);
                maxSubRow = Math.max(row[columnIndex].length, maxSubRow);
            }
            
            for (int subRow = 0; subRow < maxSubRow; subRow++) {
                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    if (subRow < row[columnIndex].length) {
                        sb.append(row[columnIndex][subRow]);
                    } else {
                        sb.append(StringFormatter.repeat(' ', table.get(columnIndex).getLength()));
                    }

                    if (columnIndex != table.size() - 1) {
                        sb.append(columnSeparator);
                    }
                }
                sb.append(ConsoleDisplayHelper.NEWLINE);
            }
        }

        return sb.toString();
    }
}
