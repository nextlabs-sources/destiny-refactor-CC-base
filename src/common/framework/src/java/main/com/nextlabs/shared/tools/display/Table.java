/*
 * Created on Jan 31, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;

import java.util.LinkedList;
import java.util.List;

import com.nextlabs.shared.tools.StringFormatter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/Table.java#1 $
 */

public class Table implements IDisplayable{
	private static final String COLUMN_SEPARATOR = "|";
	
	protected class Column{
	    protected final String name;
	    protected final int length;
	    protected List<String[]> data;
		
		public Column(String name, int length) {
			if(name.length() > length){
				throw new IllegalArgumentException("The column name is longer than the length");
			}
			this.length = length;
			this.name = StringFormatter.fitLength(name, length);
			data = new LinkedList<String[]>();
		}
		
		protected int getLength(){
		    return length;
		}
	}
	
	protected final boolean showRowNumber;
	protected boolean lock;
	protected String headerRowSeaperator = null;
	protected final String columnSeparator;
	protected final List<Column> table;
	protected int rowCount;
	protected final boolean wrapRow;
	
	public Table(){
		this(false);
	}
	
	/**
	 * 
	 * @param wrapRow if true, the row value will be wrap.
	 *                if false, the value will be truncated
	 */
	public Table(boolean wrapRow){
		this(wrapRow, COLUMN_SEPARATOR);
	}
	
	public Table(boolean wrapRow, String columnSeparator){
        table = new LinkedList<Column>();
        showRowNumber = false;
        this.columnSeparator = columnSeparator;
        rowCount = 0;
        lock = false;
        this.wrapRow = wrapRow;
    }
	
	/**
	 * 
	 * @param name
	 * @param length
	 * @return the column index
	 */
	public int addColumn(String name, int length){
		if(lock){
			throw new IllegalArgumentException("Column can't be added after row(s) have been inserted");
		}
		Column column = new Column(name, length);
		table.add(column);
		return table.indexOf(column);
	}
	
	public int addColumn(String name){
		return addColumn(name, name.length());
	}
	
	public void addRow(Object ... data){
		lock = true;
		
		if(data.length != table.size()){
			throw new IllegalArgumentException("# of input doesn't match the # of columns");
		}
		
		for(int i=0; i< data.length; i++){
			String s = data[i] == null ? "<null>" : data[i].toString();
			Column c = table.get(i);
			
			String[] formattedData = formatDate(s, c);
			c.data.add(formattedData);
		}
		rowCount++;
	}
	
	protected String[] formatDate(String s, Column c){
	    final int length = c.getLength();
	    final String[] formattedData;
        if (wrapRow) {
            s = StringFormatter.wrap(s, length, ConsoleDisplayHelper.NEWLINE);
            formattedData = s.split(ConsoleDisplayHelper.NEWLINE);
            for (int j = 0; j < formattedData.length; j++) {
                formattedData[j] = StringFormatter.fitLength(formattedData[j], length);
            }
        }else{
            s = s.length() > length
            ? StringFormatter.fitLength(s, length - 3) + "..."
            : StringFormatter.fitLength(s, length);
            formattedData = new String[] { s };
        }
        
        return formattedData;
	    
	}
	
	public int getLength() {
		int totalLength = 0;
		for(Column c : table){
			totalLength += c.getLength();
		}
		return totalLength;
	}

	public String getOutput() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< table.size(); i++){
            sb.append(table.get(i).name);
            
            if (i != table.size() - 1) {
                sb.append(columnSeparator);
            }
        }
        
        sb.append(ConsoleDisplayHelper.NEWLINE);
        
        if(headerRowSeaperator == null){
            StringBuilder headerRowSeaperator = new StringBuilder();
            for(Column c : table){
                headerRowSeaperator.append(StringFormatter.repeat('-', c.getLength()));
                headerRowSeaperator.append('+');
            }
            headerRowSeaperator.deleteCharAt(headerRowSeaperator.length()-1);
            headerRowSeaperator.append(ConsoleDisplayHelper.NEWLINE);
            this.headerRowSeaperator = headerRowSeaperator.toString();
        }

        sb.append(headerRowSeaperator);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String[][] row = new String[table.size()][];
            int maxSubRow = Integer.MIN_VALUE;
            for (int columnIndex = 0; columnIndex < table.size(); columnIndex++) {
                row[columnIndex] = table.get(columnIndex).data.get(rowIndex);
                maxSubRow = Math.max(row[columnIndex].length, maxSubRow);
            }
            
            for (int subRow = 0; subRow < maxSubRow; subRow++) {
                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    if( subRow < row[columnIndex].length){
                        sb.append(row[columnIndex][subRow]);
                    }else{
                        sb.append(StringFormatter.repeat(' ', table.get(columnIndex).length));
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

	public boolean isUpdateable() {
		return false;
	}
	
	/**
	 * 
	 * @return the number of rows before formatting
	 */
	public int getNumberOfRows(){
	    return table.size();
	}
}
