/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.weaver.*;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.patterns.*;
import org.aspectj.weaver.patterns.Declare;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class DeclareDeclaration extends MethodDeclaration {
	public Declare declare;

	/**
	 * Constructor for IntraTypeDeclaration.
	 */
	static int counter = 0; //XXX evil
	public DeclareDeclaration(CompilationResult result, Declare symbolicDeclare) {
		super(result);
		this.declare = symbolicDeclare;
		if (declare != null) {
			sourceStart = declare.getStart();
			sourceEnd = declare.getEnd();
		}
		//??? we might need to set parameters to be empty
		this.returnType = TypeReference.baseTypeReference(T_void, 0);
		this.selector = ("ajc$declare_"+counter++).toCharArray(); //??? performance
	}


	/**
	 * A pointcut declaration exists in a classfile only as an attibute on the
	 * class.  Unlike advice and inter-type declarations, it has no corresponding
	 * method.
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.DeclareAttribute(declare)));
		return;
	}

	public void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit) {
			// do nothing
	}
	
	public void resolveStatements(ClassScope upperScope) {
		// do nothing 
	}
	
//	public boolean finishResolveTypes(SourceTypeBinding sourceTypeBinding) {
//		// there's nothing for our super to resolve usefully
//		//if (!super.finishResolveTypes(sourceTypeBinding)) return false;
////		if (declare == null) return true;
////        
////        EclipseScope scope = new EclipseScope(new FormalBinding[0], this.scope);
////
////        declare.resolve(scope);
////        return true;
//	}

	
	public void build(ClassScope classScope, CrosscuttingMembers xcut) {
		if (declare == null) return;
        
        EclipseScope scope = new EclipseScope(new FormalBinding[0], classScope);

        declare.resolve(scope);
        xcut.addDeclare(declare);

		//EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		//XXX need to work out the eclipse side of all this state
//XXX		world.addDeclare(world.resolve(EclipseWorld.fromBinding(classScope.referenceContext.binding)),
//XXX						declare, false);




//		binding = makeMethodBinding(classScope);
//		world.addTypeMunger(new EclipseNewMethodTypeMunger(binding));
//		//??? what do we need to know
//		munger = new NewMethodTypeMunger(
//			EclipseWorld.makeResolvedMember(binding.introducedMethod),
//			EclipseWorld.makeResolvedMember(super.binding), null);
	}






    public String toString(int tab) {
    	if (declare == null) return tabString(tab) + "<declare>";
    	else return tabString(tab) + declare.toString();
    }
}
