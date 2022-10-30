/*
 * Created on Sep 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.awt.Color;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/MyTreeNode.java#1 $
 */

public class MyTreeNode{
    final String name;
    String color;
    Integer fontSize;
    Integer size;
    String note;
    
    public MyTreeNode(String name) {
        this(name, (String)null);
    }
    
    public MyTreeNode(String name, String color) {
        this.name = name;
        this.color = color;
    }
    
    public MyTreeNode(String name, Color color) {
        this(name, convertToHex(color));
    }
    
    public MyTreeNode setColor(String color) {
        this.color = color;
        return this;
    }
    
    public MyTreeNode setColor(Color color) {
        return this.setColor(convertToHex(color));
    }

    public MyTreeNode setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public MyTreeNode setSize(int size) {
        this.size = size;
        return this;
    }
    
    public MyTreeNode setNote(String note) {
        this.note = note;
        return this;
    }
    
    void increment(){
        if (size == null) {
            size = 1;
        } else {
            size++;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><font");
        if (color != null) {
            sb.append(" color=\"").append(color).append("\"");
        }
        if (fontSize != null) {
            sb.append(" size=").append(fontSize).append("");
        }
        sb.append(">").append(name);

        if (size != null) {
            sb.append(" (").append(size).append(")");
        }

//        if (note != null) {
//            sb.append(" ").append(note);
//        }
        
        sb.append("</font></html>");
        return sb.toString();
    }
    
    private static String convertToHex(Color color){
        if (color == null) {
            return null;
        }
        //remove alpha
        return "#"+Integer.toHexString(0xFFFFFF & color.getRGB());
    }
}
