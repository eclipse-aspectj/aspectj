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
   
import org.aspectj.bridge.IMessage;
import org.aspectj.util.LangUtil;

/**
 * @author  Mik Kersten
 */
public class CompilerMessage {
    public final IMessage message;

        public CompilerMessage(IMessage message) {
            LangUtil.throwIaxIfNull(message, "message");
            this.message = message;
        }

        public String toString() {
            return message.toString();
        }
    }
