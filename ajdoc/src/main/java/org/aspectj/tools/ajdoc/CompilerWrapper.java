/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/
package org.aspectj.tools.ajdoc;

import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Main;
/**
 * Wrapper for ajdoc's use of the AspectJ compiler.
 *
 * @author Mik Kersten
 */
public class CompilerWrapper extends Main {

	private static CompilerWrapper INSTANCE = null;

	public static AsmManager executeMain(String[] args) {
		INSTANCE = new CompilerWrapper();
		INSTANCE.runMain(args, true);
		return AsmManager.lastActiveStructureModel;
	}

	public static boolean hasErrors() {
		return INSTANCE.ourHandler.getErrors().length > 0;
	}

	public static IMessage[] getErrors() {
		return INSTANCE.ourHandler.getErrors();
	}
}
