/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;
   
import java.awt.Component;

import javax.swing.*;

import org.aspectj.bridge.IMessage;

/**
 * @author  Mik Kersten
 */
public class CompilerMessagesCellRenderer extends JLabel implements ListCellRenderer {

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {
			if (value != null) {
				setText(value.toString());
			} else {
				setText("");	
			} 
			IMessage.Kind kind = ((CompilerMessage)value).kind;
			if (kind.equals(IMessage.WARNING)) {
				setIcon(AjdeUIManager.getDefault().getIconRegistry().getWarningIcon());
			} else if (kind.equals(IMessage.INFO)) {
				setIcon(null);
			} else {
				setIcon(AjdeUIManager.getDefault().getIconRegistry().getErrorIcon());
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
	}
}
