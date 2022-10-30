/*
 * Created on Mar 3, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.nextlabs.testtool.enrollment.ad.ADElementCreatorMod.ElementType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/EnrollmentPreviewGUI.java#1 $
 */

public class EnrollmentPreviewGUI extends JFrame implements IEnrollmentPreviewGUI{
    private final Map<DefaultTreeModel, JTree> modelToTreeMap = new HashMap<DefaultTreeModel, JTree>();
    private final DefaultTreeModel parsedTreeModel, 
            warningTreeModel,
            unknownTreeModel, 
            legendModel;
	private final DefaultTableModel propertiesTableModel;
	private final JTable propertiesTable;
	private final JLabel statusBar;
	
	//Constructor
	public EnrollmentPreviewGUI() {
		super("EnrollmentPreview");
		setSize(600, 800);
//		try {
//			javax.swing.UIManager.setLookAndFeel( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//		} catch (Exception e) {
//		}
		
		ToolTipManager.sharedInstance().setInitialDelay(0);
		
		final Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		parsedTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new MyTreeNode("ROOT")));
		JTree tree = createTree(parsedTreeModel);
		tabbedPane.add("parsed", new JScrollPane(tree));
		
		warningTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new MyTreeNode("ROOT")));
        tree = createTree(warningTreeModel);
        tabbedPane.add("warning", new JScrollPane(tree));
		
		unknownTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new MyTreeNode("ROOT")));
		tree = createTree(unknownTreeModel);
		tabbedPane.add("unknown", new JScrollPane(tree));
		
		legendModel= new DefaultTreeModel(new DefaultMutableTreeNode(new MyTreeNode("ROOT")));
		tree = createTree(legendModel);
		tabbedPane.add("legend", new JScrollPane(tree));
		
		
		propertiesTableModel = new DefaultTableModel();
		propertiesTableModel.setColumnCount(2);
		propertiesTableModel.setColumnIdentifiers(new String[]{"KEY", "VALUE"});
		propertiesTable = new JTable(propertiesTableModel);
		tabbedPane.setSelectedComponent(tabbedPane.add("properties", new JScrollPane(propertiesTable)));

//		propertiesTableModel.addTableModelListener(new TableModelListener() {
//            public void tableChanged(TableModelEvent e) {
//                int row = e.getFirstRow();
//                int column = e.getColumn();
//                TableModel model = (TableModel)e.getSource();
//                String columnName = model.getColumnName(column);
//                Object data = model.getValueAt(row, column);
//
//            }
//        });
		
		statusBar = new JLabel();
		this.add(statusBar, BorderLayout.SOUTH);
		this.add(tabbedPane);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		new Thread(new Runnable() {
			public void run() {
				synchronized (parsedTreeModel) {
					parsedTreeModel.reload();
				}
				synchronized (warningTreeModel) {
				    warningTreeModel.reload();
                }
				synchronized (unknownTreeModel) {
					unknownTreeModel.reload();
				}
			}
		}).run();
	}
	
	private JTree createTree(DefaultTreeModel model){
	    JTree tree= new JTree(model);
        tree.setCellRenderer(new DefaultTreeCellRenderer(){
            public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, 
                        leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                MyTreeNode myNode = (MyTreeNode) node.getUserObject();
                if (myNode.note != null) {
                    setToolTipText("<html>" + myNode.name + "<br>" + myNode.note.replace("\n", "<br>") + "</html>");
                }
                return c;
            }
        });
        ToolTipManager.sharedInstance().registerComponent(tree);
        
        modelToTreeMap.put(model, tree);
        
        return tree;
    }
	
	public void init(){
	    DefaultMutableTreeNode root = (DefaultMutableTreeNode)legendModel.getRoot();
	    root.removeAllChildren();
	    for (ElementType elementType : ElementType.values()) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                    new MyTreeNode(elementType.name(), elementType.getColor()));
            root.add(newNode);
            ((MyTreeNode) root.getUserObject()).increment();
            legendModel.nodeChanged(root);
        }
	    modelToTreeMap.get(legendModel).expandRow(0);
	}
	
	public void setStatusMessage(String str) {
	    statusBar.setText(str);
    }

    public MyTreeNode[] convert(String[] strs){
		MyTreeNode[] nodes = new MyTreeNode[strs.length];
		for (int i = 0; i < strs.length; i++) {
			nodes[i] = new MyTreeNode(strs[i]);
		}
		return nodes;
	}
	
	protected MyTreeNode addNode(MyTreeNode[] nodes, DefaultMutableTreeNode parent, DefaultTreeModel model) {
		synchronized (model) {
			for(MyTreeNode node : nodes){
				parent = addOrCreate(parent, node, model);
			}
			return (MyTreeNode)parent.getUserObject();
		}
	}
	
	public void addProperty(String key, String value){
		propertiesTableModel.addRow(new String[]{key, value});
	}
	
	public MyTreeNode addNode(MyTreeNode[] paths, TabbedPane pane) {
		final DefaultTreeModel model;
		switch (pane) {
		case PARSED:
			model = parsedTreeModel;
			break;
		case WARNING:
            model = warningTreeModel;
            break;
		case UNKNOWN:
			model = unknownTreeModel;
			break;
		default:
			throw new IllegalArgumentException(pane.toString());
		}
		return addNode(paths, (DefaultMutableTreeNode)model.getRoot(), model);
	}
	
	public MyTreeNode addNode(String[] paths, TabbedPane pane) {
		return addNode(convert(paths), pane);
	}
	
	private DefaultMutableTreeNode addOrCreate(DefaultMutableTreeNode parent, MyTreeNode node,
			DefaultTreeModel model) {
		int childrenCount = parent.getChildCount();
		int i;
		for (i = 0; i < childrenCount; i++) {
			DefaultMutableTreeNode c = (DefaultMutableTreeNode) parent.getChildAt(i);
			MyTreeNode myTreeNode = (MyTreeNode) c.getUserObject();
			int compare = myTreeNode.name.compareToIgnoreCase(node.name);
			if (compare == 0) {
				return c;
			} else if (compare > 0) {
				break;
			}
		}

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);
		parent.insert(newNode, i);
		((MyTreeNode) parent.getUserObject()).increment();
		model.nodeChanged(parent);
		return newNode;
	}
	
}
