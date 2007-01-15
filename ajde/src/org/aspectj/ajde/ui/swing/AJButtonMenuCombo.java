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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.aspectj.ajde.Ajde;

public class AJButtonMenuCombo extends JPanel {
	
	private static final long serialVersionUID = -4866207530403336160L;

	private JButton mainButton;
    private JButton popupButton;
    private JPopupMenu menu;
//    private boolean depressable = false;
    private boolean isPressed = false;
    
	public AJButtonMenuCombo(String name, 
		String toolTipText, 
		Icon icon, 
		JPopupMenu menu,
		boolean depressable) {
			
		this.menu = menu;
//		this.depressable = depressable;
		mainButton = new JButton();
		mainButton.setIcon(icon);
        mainButton.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
        mainButton.setToolTipText(toolTipText);
        mainButton.setPreferredSize(new Dimension(22, 20));
        mainButton.setMinimumSize(new Dimension(22, 20));
        mainButton.setMaximumSize(new Dimension(22, 20));   
        
        popupButton = new JButton();
        popupButton.setIcon(Ajde.getDefault().getIconRegistry().getPopupIcon());
        popupButton.setBorder(BorderFactory.createEmptyBorder());
        popupButton.setToolTipText(toolTipText);
        popupButton.setPreferredSize(new Dimension(13, 20));
        popupButton.setMinimumSize(new Dimension(13, 20));
        popupButton.setMaximumSize(new Dimension(13, 20));           
    
    	PopupListener popupListener = new PopupListener(mainButton);
    	
    	if (depressable) {
    		mainButton.addActionListener(new ButtonActionListener());
    	} else {
        	mainButton.addMouseListener(popupListener); 
    	}
	
        popupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupButton.setBorder(null);
            }
        });

		BorderUpdateListener borderUpdateListner = new BorderUpdateListener();
		mainButton.addMouseListener(borderUpdateListner);
		popupButton.addMouseListener(borderUpdateListner);
		
		popupButton.addMouseListener(popupListener);
		
		this.setLayout(new BorderLayout());
        this.add(mainButton,  BorderLayout.CENTER);
        this.add(popupButton,  BorderLayout.EAST);
		
        this.setMinimumSize(new Dimension(35, 20));
        this.setMaximumSize(new Dimension(35, 20));      
	}

    class ButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (isPressed) {
            	mainButton.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
            	isPressed = false;
            } else {
            	mainButton.setBorder(AjdeWidgetStyles.LOWERED_BEVEL_BORDER);
            	isPressed = true;
            }
        }
    }
	

	class BorderUpdateListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            popupButton.setBorder(AjdeWidgetStyles.RAISED_BEVEL_BORDER);
            mainButton.setBorder(AjdeWidgetStyles.RAISED_BEVEL_BORDER);
        }
        
      	public void mouseExited(MouseEvent e) {
            popupButton.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
            if (isPressed) {
            	mainButton.setBorder(AjdeWidgetStyles.LOWERED_BEVEL_BORDER);
            } else {
            	mainButton.setBorder(AjdeWidgetStyles.DEFAULT_BORDER);
            }
        }
	}

	class PopupListener extends MouseAdapter {
		private JButton button;

		public PopupListener(JButton button) {
			this.button = button;
		}

	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	    	menu.show(e.getComponent(), button.getX(), button.getY() + popupButton.getHeight());
	    }
	}
	
	public void setEnabled(boolean enabled) {
		mainButton.setEnabled(enabled);
		popupButton.setEnabled(enabled);
	}

	public void setMenu(JPopupMenu menu) {
		this.menu = menu;
		this.repaint();
	}

}
