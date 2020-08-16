/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.tools.ajbrowser.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.swing.CompilerMessagesCellRenderer;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajbrowser.ui.BrowserMessageHandler;

/**
 * Panel used to display messages from the message handler
 */
public class MessageHandlerPanel extends JPanel {

	private static final long serialVersionUID = -2251912345065588977L;
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList list;
	private DefaultListModel listModel;
	private BorderLayout borderLayout1 = new BorderLayout();

	public void showMessageHandlerPanel(BrowserMessageHandler handler, boolean showPanel) {
		if (!showPanel) {
			setVisible(false);
			return;
		}
		createList(handler.getMessages());

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		list.setModel(listModel);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 1) {
					int index = list.locationToIndex(e.getPoint());
					if (listModel.getSize() >= index && index != -1) {
						IMessage message = (IMessage) listModel
								.getElementAt(index);
						Ajde.getDefault().getEditorAdapter().showSourceLine(
								message.getSourceLocation(), true);
					}
				}
			}
		};
		list.addMouseListener(mouseListener);
		list.setCellRenderer(new CompilerMessagesCellRenderer());
		setVisible(showPanel);
	}

	private void createList(List<IMessage> messages) {
		list = new JList();
		listModel = new DefaultListModel();
		for (IMessage message : messages) {
			listModel.addElement(message);
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(list, null);
	}

}
