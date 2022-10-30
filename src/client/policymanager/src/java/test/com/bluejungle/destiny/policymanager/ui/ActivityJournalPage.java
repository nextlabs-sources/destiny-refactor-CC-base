/*
 * Created on Nov 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author fuad
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ActivityJournalPage extends Composite {

	TableViewer listViewer;
	
	public ActivityJournalPage(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 1;
		setLayout (gridLayout);
		listViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listViewer.getTable().setLayoutData (gridData);
		TableColumn c1 = new TableColumn (listViewer.getTable(), SWT.LEFT);
		c1.setText("Event");
		TableColumn c2 = new TableColumn (listViewer.getTable(), SWT.LEFT);
		c2.setText("File");
		TableColumn c3 = new TableColumn (listViewer.getTable(), SWT.LEFT);
		c3.setText("Date");
		TableColumn c4 = new TableColumn (listViewer.getTable(), SWT.LEFT);
		c4.setText("User");
		c1.setWidth(100);
		c2.setWidth(100);
		c3.setWidth(100);
		c4.setWidth(100);
		listViewer.getTable().setHeaderVisible(true);

	}
}
