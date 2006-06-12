/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.bcel.BcelAdvice;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * For every shadow munger, nothing can be done with it until it is concretized.  Then...
 * 
 * (Then we call fast match.)
 * 
 * For every shadow munger, for every shadow, 
 * first match is called, 
 * then (if match returned true) the shadow munger is specialized for the shadow, 
 *     which may modify state.  
 * Then implement is called. 
 */

public abstract class ShadowMunger implements PartialOrder.PartialComparable, IHasPosition {
	protected Pointcut pointcut;
	
	// these three fields hold the source location of this munger
	protected int start, end;
	protected ISourceContext sourceContext;
	private ISourceLocation sourceLocation;
	private String handle = null;
	private ResolvedType declaringType;  // the type that declared this munger.

	
	public ShadowMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext) {
		this.pointcut = pointcut;
		this.start = start;
		this.end = end;
		this.sourceContext = sourceContext;
	}
	
	public abstract ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause);	

    public abstract void specializeOn(Shadow shadow);
    public abstract void implementOn(Shadow shadow);
	
	/**
	 * All overriding methods should call super
	 */
    public boolean match(Shadow shadow, World world) {
    	return pointcut.match(shadow).maybeTrue();
    }
    
    public abstract ShadowMunger parameterizeWith(ResolvedType declaringType,Map typeVariableMap); 
    
	public int fallbackCompareTo(Object other) {
		return toString().compareTo(toString());
	}
	
	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}
	
    public ISourceLocation getSourceLocation() {
    	if (sourceLocation == null) {
	    	if (sourceContext != null) {
				sourceLocation = sourceContext.makeSourceLocation(this);
	    	}
    	}
    	return sourceLocation;
    }

	public String getHandle() {
		if (null == handle) {
			ISourceLocation sl = getSourceLocation();
			if (sl != null) {
				if (World.createInjarHierarchy) {
					createHierarchy();
				} 
				handle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(
				            sl.getSourceFile(),
				            sl.getLine(),
				            sl.getColumn(),
							sl.getOffset());					
			}
		}
		return handle;
	}

	// ---- fields
	
    public static final ShadowMunger[] NONE = new ShadowMunger[0];



	public Pointcut getPointcut() {
		return pointcut;
	}
	
	// pointcut may be updated during rewriting...
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * Invoked when the shadow munger of a resolved type are processed.
	 * @param aType
	 */
	public void setDeclaringType(ResolvedType aType) {
		this.declaringType = aType;
	}
	
	public ResolvedType getDeclaringType() {
		return this.declaringType;
	}
	
	/**
	 * @return a Collection of ResolvedType for all checked exceptions that
	 *          might be thrown by this munger
	 */
	public abstract Collection getThrownExceptions();

    /**
     * Does the munger has to check that its exception are accepted by the shadow ?
     * ATAJ: It s not the case for @AJ around advice f.e. that can throw Throwable, even if the advised
     * method does not throw any exceptions. 
     * @return true if munger has to check that its exceptions can be throwned based on the shadow
     */
    public abstract boolean mustCheckExceptions();
    
    /**
     * Returns the ResolvedType corresponding to the aspect in which this
     * shadowMunger is declared. This is different for deow's and advice.
     */
    public abstract ResolvedType getResolvedDeclaringAspect();
    
    public void createHierarchy() {
    	IProgramElement sourceFileNode = AsmManager.getDefault().getHierarchy().findElementForSourceLine(getSourceLocation());
    	if (!sourceFileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)) {
			return;
		}
    	String name = sourceFileNode.getName();
    	sourceFileNode.setName(name + " (binary)");
    	
    	ResolvedType aspect = getResolvedDeclaringAspect();
    	
    	// create package ipe if one exists....
    	IProgramElement root = AsmManager.getDefault().getHierarchy().getRoot();
    	if (aspect.getPackageName() != null) {
    		// check that there doesn't already exist a node with this name
    		IProgramElement pkgNode = AsmManager.getDefault().getHierarchy().findElementForLabel(
    				root,IProgramElement.Kind.PACKAGE,aspect.getPackageName());
    		// note packages themselves have no source location
    		if (pkgNode == null) {
    			pkgNode = new ProgramElement(
    					aspect.getPackageName(), 
    	                IProgramElement.Kind.PACKAGE, 
    	                new ArrayList());
    			root.addChild(pkgNode);
			}
    		pkgNode.addChild(sourceFileNode);
		} else {
			root.addChild(sourceFileNode);
		}
    	
       	// remove the error child from the 'A.aj' node
    	if (sourceFileNode instanceof ProgramElement) {
			IProgramElement errorNode = (IProgramElement) sourceFileNode.getChildren().get(0);
			if (errorNode.getKind().equals(IProgramElement.Kind.ERROR)) {
				((ProgramElement)sourceFileNode).removeChild(errorNode);
			}
		}
    	
    	// add and create empty import declaration ipe
    	sourceFileNode.addChild(new ProgramElement(
    			"import declarations",
    			IProgramElement.Kind.IMPORT_REFERENCE,
    			null,0,null,null)); 

    	// add and create aspect ipe
    	IProgramElement aspectNode = new ProgramElement(
    			aspect.getSimpleName(),
    			IProgramElement.Kind.ASPECT,
    			aspect.getSourceLocation(),
    			aspect.getModifiers(),
    			null,null); 
    	sourceFileNode.addChild(aspectNode);
    	
    	addChildNodes(aspectNode,aspect.getDeclaredPointcuts());

    	addChildNodes(aspectNode,aspect.getDeclaredAdvice());
    	addChildNodes(aspectNode,aspect.getDeclares());
    }
    
    private void addChildNodes(IProgramElement parent,ResolvedMember[] children) {
       	for (int i = 0; i < children.length; i++) {
			ResolvedMember pcd = children[i];
			if (pcd instanceof ResolvedPointcutDefinition) {
				ResolvedPointcutDefinition rpcd = (ResolvedPointcutDefinition)pcd;
				parent.addChild(new ProgramElement(
						pcd.getName(),
						IProgramElement.Kind.POINTCUT,
					    rpcd.getPointcut().getSourceLocation(),
						pcd.getModifiers(),
						null,
						Collections.EMPTY_LIST));
			} 
		}
    }
    
    private void addChildNodes(IProgramElement parent, Collection children) {
    	for (Iterator iter = children.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning decl = (DeclareErrorOrWarning)element;
			   	IProgramElement deowNode = new ProgramElement(
		    			decl.isError() ? "declare error" : "declare warning",
		    			decl.isError() ? IProgramElement.Kind.DECLARE_ERROR : IProgramElement.Kind.DECLARE_WARNING,
		    			getSourceLocation(),
		    			this.getDeclaringType().getModifiers(),
		    			null,null); 
		    	deowNode.setDetails("\"" + genDeclareMessage(decl.getMessage()) + "\"");
		    	parent.addChild(deowNode);
			} else if (element instanceof BcelAdvice) {
				BcelAdvice advice = (BcelAdvice)element;
		    	parent.addChild(new ProgramElement(
				advice.kind.getName(),
				IProgramElement.Kind.ADVICE,
				getSourceLocation(),
				advice.signature.getModifiers(),null,Collections.EMPTY_LIST));
			}
		}
    }
    
	// taken from AsmElementFormatter
	private String genDeclareMessage(String message) {
		int length = message.length();
		if (length < 18) {
			return message;
		} else {
			return message.substring(0, 17) + "..";
		}
	}
}
