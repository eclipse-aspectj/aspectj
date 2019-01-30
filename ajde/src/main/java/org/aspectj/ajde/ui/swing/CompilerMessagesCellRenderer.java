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
   
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.aspectj.ajde.Ajde;
import org.aspectj.bridge.IMessage;
import org.aspectj.util.LangUtil;

/**
 * @author  Mik Kersten
 */
public class CompilerMessagesCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -4406791252357837712L;

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {
            String label = "<no message>";
            String detail = null;
            IMessage.Kind kind = IMessage.ERROR;
            if (value instanceof IMessage) {
				IMessage cm = (IMessage) value;
                label = cm.getMessage();
                if (LangUtil.isEmpty(label)) {
                    label = cm.getMessage();
                }
                kind = cm.getKind();
                Throwable thrown = cm.getThrown();
                if (null != thrown) {
                    detail = LangUtil.renderException(thrown);
                }
            } else if (null != value) {
                label = value.toString();
            }
			setText(label);
			if (kind.equals(IMessage.WARNING)) {
				setIcon(Ajde.getDefault().getIconRegistry().getWarningIcon());
			} else if (IMessage.ERROR.isSameOrLessThan(kind)) {
                setIcon(Ajde.getDefault().getIconRegistry().getErrorIcon());
			} else {
                setIcon(Ajde.getDefault().getIconRegistry().getInfoIcon());
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
            if (null != detail) {
                setToolTipText(detail);
            }
			return this;
	}
}
