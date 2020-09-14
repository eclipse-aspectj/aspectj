/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.weaver.model;

import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;

/**
 * Provides utility methods for generating details for IProgramElements used when creating the model both from source (via
 * AsmElementFormatter.visit(..)) and when filling in the model for binary aspects (via AsmRelationshipProvider bug 145963)
 */
public class AsmRelationshipUtils {

	// public static final String UNDEFINED="<undefined>";
	public static final String DECLARE_PRECEDENCE = "precedence";
	public static final String DECLARE_SOFT = "soft";
	public static final String DECLARE_PARENTS = "parents";
	public static final String DECLARE_WARNING = "warning";
	public static final String DECLARE_ERROR = "error";
	public static final String DECLARE_UNKNONWN = "<unknown declare>";
	public static final String POINTCUT_ABSTRACT = "<abstract pointcut>";
	public static final String POINTCUT_ANONYMOUS = "<anonymous pointcut>";
	public static final String DOUBLE_DOTS = "..";
	public static final int MAX_MESSAGE_LENGTH = 18;
	public static final String DEC_LABEL = "declare";

	/**
	 * Generates the declare message used in the details, for example if the declare warning statement has message
	 * "There should be no printlns" will return 'declare warning: "There should be n.."'
	 */
	public static String genDeclareMessage(String message) {
		int length = message.length();
		if (length < MAX_MESSAGE_LENGTH) {
			return message;
		} else {
			return message.substring(0, MAX_MESSAGE_LENGTH - 1) + DOUBLE_DOTS;
		}
	}

	/**
	 * Generates the pointcut details for the given pointcut, for example an anonymous pointcut will return '&lt;anonymous pointcut&gt;'
	 * and a named pointcut called p() will return 'p()..'
	 */
	public static String genPointcutDetails(Pointcut pcd) {
		StringBuffer details = new StringBuffer();
		if (pcd instanceof ReferencePointcut) {
			ReferencePointcut rp = (ReferencePointcut) pcd;
			details.append(rp.name).append(DOUBLE_DOTS);
		} else if (pcd instanceof AndPointcut) {
			AndPointcut ap = (AndPointcut) pcd;
			if (ap.getLeft() instanceof ReferencePointcut) {
				details.append(ap.getLeft().toString()).append(DOUBLE_DOTS);
			} else {
				details.append(POINTCUT_ANONYMOUS).append(DOUBLE_DOTS);
			}
		} else if (pcd instanceof OrPointcut) {
			OrPointcut op = (OrPointcut) pcd;
			if (op.getLeft() instanceof ReferencePointcut) {
				details.append(op.getLeft().toString()).append(DOUBLE_DOTS);
			} else {
				details.append(POINTCUT_ANONYMOUS).append(DOUBLE_DOTS);
			}
		} else {
			details.append(POINTCUT_ANONYMOUS);
		}
		return details.toString();
	}

}
