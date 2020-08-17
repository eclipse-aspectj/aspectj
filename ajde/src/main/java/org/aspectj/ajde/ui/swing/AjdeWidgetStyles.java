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
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public interface AjdeWidgetStyles {

	Font DEFAULT_LABEL_FONT = new java.awt.Font("SansSerif", 0, 11);
	Border DEFAULT_BORDER = BorderFactory.createEmptyBorder();
    Border LOWERED_BEVEL_BORDER = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    Border RAISED_BEVEL_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	Color DEFAULT_BACKGROUND_COLOR = Color.lightGray;

	Color LINK_NODE_COLOR = new Color(0, 0, 255);
	Color LINK_NODE_NO_SOURCE_COLOR = new Color(150, 150, 255);

}
