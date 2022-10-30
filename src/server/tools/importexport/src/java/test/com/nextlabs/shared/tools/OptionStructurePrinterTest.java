package com.nextlabs.shared.tools;

import com.nextlabs.shared.tools.impl.OptionMod;
import com.nextlabs.shared.tools.impl.OptionStructurePrinter;

/**
 * print option structure
 *
 * @author hchan
 * @date Apr 16, 2007
 */
public class OptionStructurePrinterTest {
	public static void main(String[] args) throws Exception {
		System.out.println("export");
		ICompoundOptionDescriptor root = new EntityExportOptionDescriptorEnum().getOptions().getRootOption();
		OptionStructurePrinter printer = new OptionStructurePrinter();
		printer.visit(root);
		OptionMod.reset();
		
		System.out.println();
		root = new EntityImportOptionDescriptorEnum().getOptions().getRootOption();
		printer = new OptionStructurePrinter();
		printer.visit(root);
	}
}
