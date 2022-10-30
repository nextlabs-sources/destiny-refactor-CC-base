/*
 * Created on Nov 7, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display.graphic;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/display/graphic/AsciiImage.java#1 $
 */

public class AsciiImage {
	public static void main(String[] args) throws IOException {
		new AsciiImage().test();
	}
	
	
	public void test() throws IOException{
		URL url = new URL("http://www.nextlabs.com/img/bjlogo_196_60.gif");
		BufferedImage bufferedImage = ImageIO.read(url);
		int width = bufferedImage.getWidth();
		int height= bufferedImage.getHeight() ;
		System.out.println("bufferedImage.getWidth() = " + width);
		System.out.println("bufferedImage.getHeight() = " + height);
		int[] rgb = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
		
		for(int rgbdata : rgb){
			System.out.print(rgbdata+",");
		}
		
		System.out.println("\n\n");
		Image scaledImage = bufferedImage.getScaledInstance(width/10, height/10, Image.SCALE_DEFAULT);
//		rgb = bufferedImage.getRGB(0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null, 0, scaledImage.getWidth());
		
//		BufferedImage bufferedScaledImage = new BufferedImage(scaledImage);
//		for(int rgbdata : rgb){
//			System.out.print(rgbdata+",");
//		}
	}
}
