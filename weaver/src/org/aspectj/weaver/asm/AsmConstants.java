/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.asm;

import org.aspectj.weaver.AjAttribute.Aspect;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.org.objectweb.asm.Attribute;

public class AsmConstants {

	public static Attribute[] ajAttributes; // attributes ASM needs to know about
	
	static {
		ajAttributes = new Attribute[]{
		  new AjASMAttribute(Aspect.AttributeName),
          new AjASMAttribute(WeaverVersionInfo.AttributeName),
          new AjASMAttribute("org.aspectj.weaver.WeaverState"),
          new AjASMAttribute("org.aspectj.weaver.PointcutDeclaration"),
          new AjASMAttribute("org.aspectj.weaver.Declare"),
          new AjASMAttribute("org.aspectj.weaver.TypeMunger"),
          new AjASMAttribute("org.aspectj.weaver.Privileged"),
          new AjASMAttribute("org.aspectj.weaver.MethodDeclarationLineNumber"),
          new AjASMAttribute("org.aspectj.weaver.SourceContext"),
          new AjASMAttribute("org.aspectj.weaver.Advice"),
          new AjASMAttribute("org.aspectj.weaver.EffectiveSignature"),
          new AjASMAttribute("org.aspectj.weaver.AjSynthetic")
	    };
	}

}
