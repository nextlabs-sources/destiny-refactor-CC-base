/*
 * Created on Dec 11, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.profiling;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/profiling/Sample.java#1 $
 */

class Sample{
	private final int maxSize;
	private int[] sample;
	private int globalIndex = 0;
	private int compression = 1; 
	
	public Sample(int maxSize) {
		this.maxSize = maxSize;
		sample = new int[maxSize];
	}
	
	void add(int value){
		int assignedIndex = globalIndex / compression;
		if( assignedIndex >= maxSize){
			compress();
			assignedIndex = globalIndex / compression;
		}
		//System.out.println("add value " + value + "to " + assignedIndex);
		
		int slottedValue = sample[assignedIndex];
		int itemAlreadyIn = globalIndex % compression;
		sample[assignedIndex] = ((slottedValue * itemAlreadyIn) + value) / (itemAlreadyIn + 1);
		globalIndex++;
	}
	
	void compress(){
		//System.out.println("compress");
		final int magicRatio = 2;
		for (int i = 0; i < maxSize; i += magicRatio) {
			int compressedTotal = 0;
			for (int j = i; j < i + magicRatio && j < maxSize; j++) {
				compressedTotal += sample[j];
			}
			sample[i / magicRatio] = Math.round((float) compressedTotal / magicRatio);
		}

		compression *= magicRatio;
	}
	
	int getNumOfSamples(){
		return globalIndex;
	}
	
	int getCompressionRatio(){
		return compression;
	}
	
	int[] getResult() {
		int usedSlotted = Math.min(globalIndex / compression + 1, maxSize);
		int[] result = new int[usedSlotted];
		System.arraycopy(sample, 0, result, 0, usedSlotted);
		return result;
	}
	
	float getAverage(){
		double total = 0;
		for(int i : getResult()){
			total += i;
		}
		return (float)(total / (globalIndex / compression));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(globalIndex).append("/").append(compression).append("/").append(getAverage()).append("  ");
		for(int i : getResult()){
			sb.append(i).append(" ");
		}
		return sb.toString();
	}
}