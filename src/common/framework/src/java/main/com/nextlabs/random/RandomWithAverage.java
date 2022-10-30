/*
 * Created on Jul 22, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.random;

import java.util.LinkedList;
import java.util.Random;

import com.bluejungle.framework.utils.ArrayUtils;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/random/RandomWithAverage.java#1 $
 */

public class RandomWithAverage {
    private final int min;
    private final int max;
    private final float magicMagnitude;

    private final Random random;

    private final int[] normalArray;
    
    private double total;
    private int numOfGenerated;

    public RandomWithAverage(int min, int max, int average) {
        total = 0;
        numOfGenerated = 0;
        this.min = min;
        this.max = max;
        random = new Random();

        int magnitude = max - min;

        LinkedList<Integer> normalList = new LinkedList<Integer>();

        int range = Math.min(average - min, max - average);
        int magic;
        if (range == 0) {
            range = 1;
            magic = 10;
        } else {
            magic = ((max - min) / range);
            if (magic > 10) {
                magic = 10;
            }
        }

        magicMagnitude = magnitude * magic;
        for (int i = min; i < average; i++) {
            int time = magic((float) (average - i) / range);
            for (int t = 0; t < time; t++) {
                normalList.add(i);
            }

        }
        for (int i = average; i <= max; i++) {
            int time = magic((float) (i - average) / range);
            for (int t = 0; t < time; t++) {
                normalList.add(i);
            }
        }

        normalArray = ArrayUtils.toInt(normalList.toArray(new Integer[] {}));

    }

    private int magic(double x) {
        x *= Math.E;
        return (int) Math.round(Math.exp(-(x * x) / 2) * magicMagnitude) + 1;
    }
    
    public int next(){
        numOfGenerated++;
        if(min == max){
            return min;
        }
        int r = random.nextInt(normalArray.length) ;
        total += normalArray[r];
        return normalArray[r];
    }
    
    public double getAverage(){
        return total / numOfGenerated;
    }
}
