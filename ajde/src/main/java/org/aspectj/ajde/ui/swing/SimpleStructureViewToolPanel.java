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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IHierarchyListener;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

public class SimpleStructureViewToolPanel extends JPanel {

	private static final long serialVersionUID = -7573973278642540506L;
	private final StructureView currentView;
	private final JButton separator_button = new JButton();
	private boolean hideNonAJEnabled = false;
	private boolean hideAssociationsEnabled = false;
	private boolean sortEnabled = false;

	Border border1;
	Border border2;
	JButton structureView_button = new JButton();
	JPanel label_panel = new JPanel();
	JLabel currConfig_field = new JLabel();
	JPanel spacer_panel = new JPanel();
	JPanel jPanel2 = new JPanel();
	JButton forward_button = new JButton();
	JPanel navigation_panel = new JPanel();
	JButton back_button = new JButton();
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel buttons_panel = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	BorderLayout borderLayout3 = new BorderLayout();
	BorderLayout borderLayout4 = new BorderLayout();

	public final IHierarchyListener MODEL_LISTENER = new IHierarchyListener() {
		public void elementsUpdated(IHierarchy model) {
			String path = Ajde.getDefault().getBuildConfigManager().getActiveConfigFile();
			String fileName = "<no active config>";
			if (path != null)
				fileName = new File(path).getName();
			updateCurrConfigLabel(fileName);
		}
	};

	JButton hideNonAJ_button = new JButton();
	JPanel navigation_panel1 = new JPanel();
	JButton hideAssociations_button = new JButton();
	BorderLayout borderLayout5 = new BorderLayout();
	JButton sort_button = new JButton();

	public SimpleStructureViewToolPanel(StructureView currentView) {
		this.currentView = currentView;
		Ajde.getDefault().getModel().addListener(MODEL_LISTENER);
		try {
			jbInit();
		} catch (Exception e) {
			Message msg = new Message("Could not initialize GUI.", IMessage.ERROR, e, null);
			Ajde.getDefault().getMessageHandler().handleMessage(msg);
		}
		updateCurrConfigLabel("<no active config>");
	}

	private void updateCurrConfigLabel(String text) {
		currConfig_field.setText("  File View (" + text + ")");
	}

	private void jbInit() throws Exception {
		border1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white, new Color(156, 156, 158),
				new Color(109, 109, 110));
		border2 = BorderFactory.createEmptyBorder(0, 1, 0, 0);

		separator_button.setPreferredSize(new Dimension(2, 16));
		separator_button.setMinimumSize(new Dimension(2, 16));
		separator_button.setEnabled(false);
		separator_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		separator_button.setMaximumSize(new Dimension(2, 16));

		structureView_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				structureView_button_actionPerformed(e);
			}
		});
		structureView_button.setIcon(Ajde.getDefault().getIconRegistry().getStructureViewIcon());
		structureView_button.setBorder(border2);
		structureView_button.setToolTipText("Navigate back");
		structureView_button.setPreferredSize(new Dimension(20, 20));
		structureView_button.setMinimumSize(new Dimension(20, 20));
		structureView_button.setMaximumSize(new Dimension(24, 20));
		currConfig_field.setBackground(SystemColor.control);
		currConfig_field.setFont(new java.awt.Font("SansSerif", 0, 11));
		currConfig_field.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		// currConfig_field.setEditable(false);
		currConfig_field.setText("     ");

		forward_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forward_button_actionPerformed(e);
			}
		});
		forward_button.setIcon(Ajde.getDefault().getIconRegistry().getForwardIcon());
		forward_button.setToolTipText("Navigate forward");
		forward_button.setPreferredSize(new Dimension(20, 20));
		forward_button.setMinimumSize(new Dimension(20, 20));
		forward_button.setMaximumSize(new Dimension(24, 20));
		forward_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		navigation_panel.setLayout(borderLayout1);
		back_button.setMaximumSize(new Dimension(24, 20));
		back_button.setMinimumSize(new Dimension(20, 20));
		back_button.setPreferredSize(new Dimension(20, 20));
		back_button.setToolTipText("Navigate back");
		back_button.setIcon(Ajde.getDefault().getIconRegistry().getBackIcon());
		back_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				back_button_actionPerformed(e);
			}
		});
		back_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		this.setLayout(borderLayout2);
		buttons_panel.setLayout(borderLayout3);
		label_panel.setLayout(borderLayout4);
		hideNonAJ_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		hideNonAJ_button.setMaximumSize(new Dimension(24, 20));
		hideNonAJ_button.setMinimumSize(new Dimension(20, 20));
		hideNonAJ_button.setPreferredSize(new Dimension(20, 20));
		hideNonAJ_button.setToolTipText("Hide non-AspectJ members");
		hideNonAJ_button.setIcon(Ajde.getDefault().getIconRegistry().getHideNonAJIcon());
		hideNonAJ_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hideNonAJ_button_actionPerformed(e);
			}
		});
		navigation_panel1.setLayout(borderLayout5);
		hideAssociations_button.setMaximumSize(new Dimension(24, 20));
		hideAssociations_button.setMinimumSize(new Dimension(20, 20));
		hideAssociations_button.setPreferredSize(new Dimension(20, 20));
		hideAssociations_button.setToolTipText("Hide associations");
		hideAssociations_button.setIcon(Ajde.getDefault().getIconRegistry().getHideAssociationsIcon());
		hideAssociations_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hideAssociations_button_actionPerformed(e);
			}
		});
		hideAssociations_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		sort_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
		sort_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sort_button_actionPerformed(e);
			}
		});
		sort_button.setIcon(Ajde.getDefault().getIconRegistry().getOrderIcon());
		sort_button.setToolTipText("Sort member");
		sort_button.setPreferredSize(new Dimension(20, 20));
		sort_button.setMinimumSize(new Dimension(20, 20));
		sort_button.setMaximumSize(new Dimension(24, 20));
		label_panel.add(currConfig_field, BorderLayout.CENTER);
		// label_panel.add(structureView_button, BorderLayout.WEST);
		this.add(spacer_panel, BorderLayout.CENTER);
		this.add(buttons_panel, BorderLayout.EAST);
		buttons_panel.add(navigation_panel, BorderLayout.CENTER);
		navigation_panel.add(back_button, BorderLayout.CENTER);
		navigation_panel.add(forward_button, BorderLayout.EAST);
		navigation_panel.add(jPanel2, BorderLayout.WEST);
		buttons_panel.add(navigation_panel1, BorderLayout.WEST);
		navigation_panel1.add(hideAssociations_button, BorderLayout.EAST);
		navigation_panel1.add(hideNonAJ_button, BorderLayout.CENTER);
		navigation_panel1.add(sort_button, BorderLayout.WEST);
		this.add(label_panel, BorderLayout.WEST);

	}

	private void forward_button_actionPerformed(ActionEvent e) {
		Ajde.getDefault().getStructureViewManager().fireNavigateForwardAction(currentView);
	}

	private void back_button_actionPerformed(ActionEvent e) {
		Ajde.getDefault().getStructureViewManager().fireNavigateBackAction(currentView);
	}

	void structureView_button_actionPerformed(ActionEvent e) {

	}

	private void hideNonAJ_button_actionPerformed(ActionEvent e) {
		if (hideNonAJEnabled) {
			hideNonAJ_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
			hideNonAJEnabled = false;
			currentView.getViewProperties().setFilteredMemberKinds(new ArrayList());
		} else {
			hideNonAJ_button.setBorder(AjdeWidgetStyles.LOWERED_BEVEL_BORDER);
			hideNonAJEnabled = true;
			currentView.getViewProperties().setFilteredMemberKinds(IProgramElement.Kind.getNonAJMemberKinds());
		}
		Ajde.getDefault().getStructureViewManager().refreshView(currentView);
	}

	private void hideAssociations_button_actionPerformed(ActionEvent e) {
		if (hideAssociationsEnabled) {
			hideAssociations_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
			hideAssociationsEnabled = false;
			currentView.getViewProperties().setRelations(Ajde.getDefault().getStructureViewManager().getAvailableRelations());
		} else {
			hideAssociations_button.setBorder(AjdeWidgetStyles.LOWERED_BEVEL_BORDER);
			hideAssociationsEnabled = true;
			currentView.getViewProperties().setRelations(new ArrayList());
		}
		Ajde.getDefault().getStructureViewManager().refreshView(currentView);
	}

	private void sort_button_actionPerformed(ActionEvent e) {
		if (sortEnabled) {
			sort_button.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
			sortEnabled = false;
			currentView.getViewProperties().setSorting(StructureViewProperties.Sorting.DECLARATIONAL);
		} else {
			sort_button.setBorder(AjdeWidgetStyles.LOWERED_BEVEL_BORDER);
			sortEnabled = true;
			currentView.getViewProperties().setSorting(StructureViewProperties.Sorting.ALPHABETICAL);
		}
		Ajde.getDefault().getStructureViewManager().refreshView(currentView);
	}
}
