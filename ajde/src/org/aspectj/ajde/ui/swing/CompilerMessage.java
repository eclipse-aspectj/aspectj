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
import org.aspectj.bridge.ISourceLocation;

/**
 * @author  Mik Kersten
 */
public class CompilerMessage {
        public String message;
        public ISourceLocation sourceLocation; 
        public IMessage.Kind kind;

        public CompilerMessage(String message, IMessage.Kind kind) {
            this.message = message;
            this.sourceLocation = null;
            this.kind = kind;  
        }

        public CompilerMessage(String message, ISourceLocation sourceLocation, IMessage.Kind kind) {
            this.message = message;
            this.sourceLocation = sourceLocation;
            this.kind = kind;  
        }

        public String toString() {
        	if (sourceLocation != null) {
	            return sourceLocation.getSourceFile().getAbsolutePath() + ":" 
	            	+ " " + message 
	            	+ " at line " + sourceLocation.getLine() 
	            	+ ", column " + sourceLocation.getColumn();
        	} else {
	            return message;        	
        	}
        }
    }
