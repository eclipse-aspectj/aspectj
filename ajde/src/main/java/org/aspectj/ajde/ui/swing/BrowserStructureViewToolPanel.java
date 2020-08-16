/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.GlobalStructureView;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

public class BrowserStructureViewToolPanel extends JPanel {

	private static final long serialVersionUID = 7960528108612681776L;

	private StructureView currentView;
	private StructureViewPanel viewPanel;
    protected BorderLayout borderLayout1 = new BorderLayout();
    protected Border border1;
    protected Border border2;
    AJButtonMenuCombo granularityCombo;
    AJButtonMenuCombo filterCombo;
    AJButtonMenuCombo relationsCombo;
    JPanel buttons_panel = new JPanel();
    JPanel spacer_panel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel view_panel = new JPanel();
    JComboBox view_comboBox = null;
    JLabel view_label = new JLabel();
    BorderLayout borderLayout4 = new BorderLayout();

	public BrowserStructureViewToolPanel(
		java.util.List structureViews,
		StructureView currentView,
		StructureViewPanel viewPanel) {

		this.currentView = currentView;
		this.viewPanel = viewPanel;
		view_comboBox = new JComboBox();
		view_comboBox.setFont(AjdeWidgetStyles.DEFAULT_LABEL_FONT);

		for (Object structureView : structureViews) {
			view_comboBox.addItem(structureView);
		}

		try {
			jbInit();
		} catch (Exception e) {
        	Message msg = new Message("Could not initialize GUI.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
		}
		initToolBar();
	}

	private void initToolBar() {
        try {
			granularityCombo = new AJButtonMenuCombo(
				"Visible granularity",
				"Visible granularity",
				Ajde.getDefault().getIconRegistry().getGranularityIcon(),
				createGranularityMenu(),
				false);

			filterCombo = new AJButtonMenuCombo(
				"Filter members",
				"Filter members",
				Ajde.getDefault().getIconRegistry().getFilterIcon(),
				createFilterMenu(),
				false);

			relationsCombo = new AJButtonMenuCombo(
				"Filter associations",
				"Filter associations",
				Ajde.getDefault().getIconRegistry().getRelationsIcon(),
				createRelationsMenu(),
				false);

			buttons_panel.add(granularityCombo,  BorderLayout.WEST);
            buttons_panel.add(filterCombo,  BorderLayout.CENTER);
            buttons_panel.add(relationsCombo,  BorderLayout.EAST);
        } catch(Exception e) {
        	Message msg = new Message("Could not initialize GUI.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
        }
	}

	private JPopupMenu createFilterMenu() {
		JPopupMenu filterMenu = new JPopupMenu();
		IProgramElement.Accessibility[] accessibility = IProgramElement.Accessibility.ALL;
		for (IProgramElement.Accessibility value : accessibility) {
			CheckBoxSelectionMenuButton menuItem = new CheckBoxSelectionMenuButton(value);
			menuItem.setIcon(Ajde.getDefault().getIconRegistry().getAccessibilitySwingIcon(value));
			filterMenu.add(menuItem);
		}
		filterMenu.add(new JSeparator());

		IProgramElement.Kind[] kinds = IProgramElement.Kind.ALL;
		for (IProgramElement.Kind kind : kinds) {
			if (kind.isMember()) {
				CheckBoxSelectionMenuButton menuItem = new CheckBoxSelectionMenuButton(kind);
				menuItem.setIcon((Icon) Ajde.getDefault().getIconRegistry().getIcon(kind).getIconResource());
				filterMenu.add(menuItem);
			}
		}
		filterMenu.add(new JSeparator());

		IProgramElement.Modifiers[] modifiers = IProgramElement.Modifiers.ALL;
		for (IProgramElement.Modifiers modifier : modifiers) {
			CheckBoxSelectionMenuButton menuItem = new CheckBoxSelectionMenuButton(modifier);
			filterMenu.add(menuItem);
		}
		return filterMenu;
	}

	private JPopupMenu createRelationsMenu() {
		JPopupMenu relationsMenu = new JPopupMenu();

		java.util.List relations = Ajde.getDefault().getStructureViewManager().getAvailableRelations();
		for (Object o : relations) {
			IRelationship.Kind relation = (IRelationship.Kind) o;
			CheckBoxSelectionMenuButton menuItem = new CheckBoxSelectionMenuButton(relation);
			menuItem.setIcon((Icon) Ajde.getDefault().getIconRegistry().getIcon(relation).getIconResource());
			relationsMenu.add(menuItem);
		}

		return relationsMenu;
	}

	private JPopupMenu createGranularityMenu() {
		JPopupMenu orderMenu = new JPopupMenu();

		StructureViewProperties.Granularity[] granularity = StructureViewProperties.Granularity.ALL;
		ButtonGroup group = new ButtonGroup();
		for (StructureViewProperties.Granularity value : granularity) {
			RadioSelectionMenuButton menuItem = new RadioSelectionMenuButton(value, group);
			orderMenu.add(menuItem);
			if (value.equals(StructureViewProperties.Granularity.MEMBER)) {
				menuItem.setSelected(true);
			}
		}
		return orderMenu;
	}

	private class RadioSelectionMenuButton extends JRadioButtonMenuItem {

		private static final long serialVersionUID = -879644981405801807L;

		public RadioSelectionMenuButton(StructureViewProperties.Granularity granularity, ButtonGroup group) {
			super(granularity.toString());
			super.setFont(AjdeWidgetStyles.DEFAULT_LABEL_FONT);
			group.add(this);
			this.addActionListener(new RadioSelectionMenuActionListener(granularity));
		}
	}

	private class RadioSelectionMenuActionListener implements ActionListener {
		private StructureViewProperties.Granularity granularity;

		public RadioSelectionMenuActionListener(StructureViewProperties.Granularity granularity) {
			this.granularity = granularity;
		}

		public void actionPerformed(ActionEvent e) {
			currentView.getViewProperties().setGranularity(granularity);
			Ajde.getDefault().getStructureViewManager().refreshView(currentView);
		}
	}

	private class CheckBoxSelectionMenuButton extends JCheckBoxMenuItem {

		private static final long serialVersionUID = -4555502313984854787L;

		public CheckBoxSelectionMenuButton(String name) {
			super(name);
			this.setFont(AjdeWidgetStyles.DEFAULT_LABEL_FONT);
			this.setBackground(AjdeWidgetStyles.DEFAULT_BACKGROUND_COLOR);
			//super.setSelected(true);
		}

		public CheckBoxSelectionMenuButton(IProgramElement.Accessibility accessibility) {
			this(accessibility.toString());
			this.addActionListener(new CheckBoxSelectionMenuActionListener(accessibility));
		}

		public CheckBoxSelectionMenuButton(IProgramElement.Kind kind) {
			this(kind.toString());
			this.addActionListener(new CheckBoxSelectionMenuActionListener(kind));
		}

		public CheckBoxSelectionMenuButton(IProgramElement.Modifiers modifiers) {
			this(modifiers.toString());
			this.addActionListener(new CheckBoxSelectionMenuActionListener(modifiers));
		}

		public CheckBoxSelectionMenuButton(StructureViewProperties.Sorting sorting) {
			this(sorting.toString());
			this.addActionListener(new CheckBoxSelectionMenuActionListener(sorting));
		}

		public CheckBoxSelectionMenuButton(IRelationship.Kind relation) {
			this(relation.toString());
			this.addActionListener(new CheckBoxSelectionMenuActionListener(relation));
		}
	}

	/**
	 * Ewwwwww!
	 */
	private class CheckBoxSelectionMenuActionListener implements ActionListener {
		private IProgramElement.Accessibility accessibility = null;
		private IProgramElement.Kind kind = null;
		private IProgramElement.Modifiers modifiers = null;
		private StructureViewProperties.Sorting sorting = null;
		private IRelationship.Kind relation = null;

		public CheckBoxSelectionMenuActionListener(IProgramElement.Accessibility accessibility) {
			this.accessibility = accessibility;
		}

		public CheckBoxSelectionMenuActionListener(IProgramElement.Kind kind) {
			this.kind = kind;
		}

		public CheckBoxSelectionMenuActionListener(IProgramElement.Modifiers modifiers) {
			this.modifiers = modifiers;
		}

		public CheckBoxSelectionMenuActionListener(StructureViewProperties.Sorting sorting) {
			this.sorting = sorting;
		}

		public CheckBoxSelectionMenuActionListener(IRelationship.Kind relationKind) {
			this.relation = relationKind;
		}

		public void actionPerformed(ActionEvent e) {
			if (!(e.getSource() instanceof CheckBoxSelectionMenuButton)) return;
			CheckBoxSelectionMenuButton checkMenu = (CheckBoxSelectionMenuButton)e.getSource();
			if (accessibility != null) {
				if (checkMenu.isSelected()) {
					currentView.getViewProperties().addFilteredMemberAccessibility(accessibility);
				} else {
					currentView.getViewProperties().removeFilteredMemberAccessibility(accessibility);
				}
			} else if (kind != null) {
				if (checkMenu.isSelected()) {
					currentView.getViewProperties().addFilteredMemberKind(kind);
				} else {
					currentView.getViewProperties().removeFilteredMemberKind(kind);
				}
			} else if (modifiers != null) {
				if (checkMenu.isSelected()) {
					currentView.getViewProperties().addFilteredMemberModifiers(modifiers);
				} else {
					currentView.getViewProperties().removeFilteredMemberModifiers(modifiers);
				}
			} else if (sorting != null) {
				if (checkMenu.isSelected()) {
					currentView.getViewProperties().setSorting(sorting);
				} else {
					currentView.getViewProperties().setSorting(StructureViewProperties.Sorting.DECLARATIONAL);
				}
			} else if (relation != null) {
				if (checkMenu.isSelected()) {
					currentView.getViewProperties().removeRelation(relation);
				} else {
					currentView.getViewProperties().addRelation(relation);
				}
			}
			Ajde.getDefault().getStructureViewManager().refreshView(currentView);
		}
	}

//    public void highlightNode(ProgramElementNode node) {
//        treeManager.navigationAction(node, true, true);
//    }

//    private void order_comboBox_actionPerformed(ActionEvent e) {
//		Ajde.getDefault().getStructureViewManager().refreshView(
//			currentView
//		);
//    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout2);
        buttons_panel.setLayout(borderLayout3);
        buttons_panel.setMinimumSize(new Dimension(105, 10));
        buttons_panel.setPreferredSize(new Dimension(105, 10));
        view_comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view_comboBox_actionPerformed(e);
            }
        });
        view_label.setFont(new java.awt.Font("Dialog", 0, 11));
        view_label.setText("  Global View   ");
        view_comboBox.setFont(new java.awt.Font("SansSerif", 0, 11));
        view_comboBox.setPreferredSize(new Dimension(125, 22));
        view_panel.setLayout(borderLayout4);
        view_panel.add(view_label, BorderLayout.WEST);
        this.add(buttons_panel,  BorderLayout.EAST);
        this.add(spacer_panel,  BorderLayout.CENTER);
        this.add(view_panel,  BorderLayout.WEST);
        view_panel.add(view_comboBox, BorderLayout.CENTER);

    }

//    private void order_button_actionPerformed(ActionEvent e) {
//
//    }
//
//    private void orderPopup_button_actionPerformed(ActionEvent e) {
//
//    }

    void separator_button_actionPerformed(ActionEvent e) {

    }

    void view_comboBox_actionPerformed(ActionEvent e) {
    	StructureView view = (StructureView)view_comboBox.getSelectedItem();
		viewPanel.setCurrentView(view);
		if (((GlobalStructureView)view).getGlobalViewProperties().getHierarchy()
			== StructureViewProperties.Hierarchy.DECLARATION) {
			granularityCombo.setEnabled(true);
			relationsCombo.setEnabled(true);
			filterCombo.setEnabled(true);
		} else {
			granularityCombo.setEnabled(false);
			relationsCombo.setEnabled(false);
			filterCombo.setEnabled(false);
		}
    }
}
