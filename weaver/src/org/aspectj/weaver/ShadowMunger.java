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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
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
	private ISourceLocation binarySourceLocation;
	private File binaryFile;
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
    	if (isBinary()) {
			if (binarySourceLocation == null) {
				binarySourceLocation = getBinarySourceLocation(sourceLocation);
			}
			return binarySourceLocation;
		}
    	return sourceLocation;
    }

	public String getHandle() {
		if (null == handle) {
			ISourceLocation sl = getSourceLocation();
			if (sl != null) {
				IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForSourceLine(sl);
				handle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(ipe);
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
     * Creates the hierarchy for binary aspects
     */
    public void createHierarchy() {
    	if (!isBinary()) return;
    	
    	IProgramElement sourceFileNode = AsmManager.getDefault().getHierarchy().findElementForSourceLine(getSourceLocation());
    	// the call to findElementForSourceLine(ISourceLocation) returns a file node
    	// if it can't find a node in the hierarchy for the given sourcelocation. 
    	// Therefore, if this is returned, we know we can't find one and have to
    	// continue to fault in the model.
    	if (!sourceFileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)) {
			return;
		}
    	
    	ResolvedType aspect = getDeclaringType();
    	
    	// create the class file node
    	IProgramElement classFileNode = new ProgramElement(
    			sourceFileNode.getName() + " (binary)",
    			IProgramElement.Kind.CLASS,
    			getBinarySourceLocation(aspect.getSourceLocation()),
    			0,null,null);
    	
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
    			pkgNode.addChild(classFileNode);
			} else {
				// need to add it first otherwise the handle for classFileNode
				// may not be generated correctly if it uses information from 
				// it's parent node
				pkgNode.addChild(classFileNode);
				for (Iterator iter = pkgNode.getChildren().iterator(); iter.hasNext();) {
					IProgramElement element = (IProgramElement) iter.next();
					if (!element.equals(classFileNode) && 
							element.getHandleIdentifier().equals(
							classFileNode.getHandleIdentifier())) {
						// already added the classfile so have already
						// added the structure for this aspect
						pkgNode.removeChild(classFileNode);
						return;
					}
				}
			}
		} else {
			// need to add it first otherwise the handle for classFileNode
			// may not be generated correctly if it uses information from 
			// it's parent node
			root.addChild(classFileNode);
			for (Iterator iter = root.getChildren().iterator(); iter.hasNext();) {
				IProgramElement element = (IProgramElement) iter.next();
				if (!element.equals(classFileNode) &&
						element.getHandleIdentifier().equals(
						classFileNode.getHandleIdentifier())) {
					// already added the sourcefile so have already
					// added the structure for this aspect
					root.removeChild(classFileNode);
					return;
				}				
			}
		}
    	
    	// add and create empty import declaration ipe
    	classFileNode.addChild(new ProgramElement(
    			"import declarations",
    			IProgramElement.Kind.IMPORT_REFERENCE,
    			null,0,null,null)); 

    	// add and create aspect ipe
    	IProgramElement aspectNode = new ProgramElement(
    			aspect.getSimpleName(),
    			IProgramElement.Kind.ASPECT,
    			getBinarySourceLocation(aspect.getSourceLocation()),
    			aspect.getModifiers(),
    			null,null); 
    	classFileNode.addChild(aspectNode);
    	
    	addChildNodes(aspectNode,aspect.getDeclaredPointcuts());

    	addChildNodes(aspectNode,aspect.getDeclaredAdvice());
    	addChildNodes(aspectNode,aspect.getDeclares());
    }
    
    private void addChildNodes(IProgramElement parent,ResolvedMember[] children) {
       	for (int i = 0; i < children.length; i++) {
			ResolvedMember pcd = children[i];
			if (pcd instanceof ResolvedPointcutDefinition) {
				ResolvedPointcutDefinition rpcd = (ResolvedPointcutDefinition)pcd;
				ISourceLocation sLoc = rpcd.getPointcut().getSourceLocation();
				if (sLoc == null) {
					sLoc = rpcd.getSourceLocation();
				}
				parent.addChild(new ProgramElement(
						pcd.getName(),
						IProgramElement.Kind.POINTCUT,
					    getBinarySourceLocation(sLoc),
						pcd.getModifiers(),
						null,
						Collections.EMPTY_LIST));
			} 
		}
    }
    
    private void addChildNodes(IProgramElement parent, Collection children) {
    	int afterCtr = 1;
    	int aroundCtr = 1;
    	int beforeCtr = 1;
    	int deCtr = 1;
    	int dwCtr = 1;
    	for (Iterator iter = children.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning decl = (DeclareErrorOrWarning)element;
				int counter = 0;
				if (decl.isError()) {
					counter = deCtr++;
				} else {
					counter = dwCtr++;
				}
		    	parent.addChild(createDeclareErrorOrWarningChild(decl,counter));
			} else if (element instanceof BcelAdvice) {
				BcelAdvice advice = (BcelAdvice)element;
				int counter = 0;
				if (advice.getKind().equals(AdviceKind.Before)) {
					counter = beforeCtr++;
				} else if (advice.getKind().equals(AdviceKind.Around)){
					counter = aroundCtr++;
				} else {
					counter = afterCtr++;
				}
		    	parent.addChild(createAdviceChild(advice,counter));
			}
		}
    }
    
    private IProgramElement createDeclareErrorOrWarningChild(
    		DeclareErrorOrWarning decl, int count) {
	   	IProgramElement deowNode = new ProgramElement(
    			decl.getName(),
    			decl.isError() ? IProgramElement.Kind.DECLARE_ERROR : IProgramElement.Kind.DECLARE_WARNING,
    			getBinarySourceLocation(decl.getSourceLocation()),
    			decl.getDeclaringType().getModifiers(),
    			null,null); 
    	deowNode.setDetails("\"" + AsmRelationshipUtils.genDeclareMessage(decl.getMessage()) + "\"");
    	if (count != -1) {
    		deowNode.setBytecodeName(decl.getName() + "_" + count);
    	}
    	return deowNode;
    }
    
    private IProgramElement createAdviceChild(BcelAdvice advice, int counter ) {
		IProgramElement adviceNode = new ProgramElement(
    			advice.kind.getName(),
    			IProgramElement.Kind.ADVICE,
    			getBinarySourceLocation(advice.getSourceLocation()),
    			advice.signature.getModifiers(),null,Collections.EMPTY_LIST);
    	adviceNode.setDetails(AsmRelationshipUtils.genPointcutDetails(advice.getPointcut()));
    	if (counter != 1) {
			adviceNode.setBytecodeName(advice.getKind().getName() + "$" + counter + "$");
		}
    	return adviceNode;
    }
    
    /**
     * Returns the binarySourceLocation for the given sourcelocation. This
     * isn't cached because it's used when faulting in the binary nodes
     * and is called with ISourceLocations for all advice, pointcuts and deows
     * contained within the resolvedDeclaringAspect.
     */
    private ISourceLocation getBinarySourceLocation(ISourceLocation sl) {
    	if (sl == null) return null;
    	String sourceFileName = null;
    	if (getDeclaringType() instanceof ReferenceType) {
			String s = ((ReferenceType)getDeclaringType()).getDelegate().getSourcefilename();
			int i = s.lastIndexOf('/');
			if (i != -1) {
				sourceFileName = s.substring(i+1);
			} else {
				sourceFileName = s;
			}
		}
		ISourceLocation sLoc = new SourceLocation(
				getBinaryFile(),
				sl.getLine(),
				sl.getEndLine(),
				((sl.getColumn() == 0) ? ISourceLocation.NO_COLUMN : sl.getColumn()),
				sl.getContext(),
				sourceFileName);
		return sLoc;
    }
    
    /**
     * Returns the File with pathname to the class file, for example either
     * C:\temp\ajcSandbox\workspace\ajcTest16957.tmp\simple.jar!pkg\BinaryAspect.class
     * if the class file is in a jar file, or 
     * C:\temp\ajcSandbox\workspace\ajcTest16957.tmp!pkg\BinaryAspect.class
     * if the class file is in a directory
     */
    private File getBinaryFile() {
    	if (binaryFile == null) {
    		String s = getDeclaringType().getBinaryPath();
    		File f = getDeclaringType().getSourceLocation().getSourceFile();
    		int i = f.getPath().lastIndexOf('.');
    		String path = f.getPath().substring(0,i) + ".class";
    		binaryFile =  new File(s + "!" + path);
		}
    	return binaryFile;
    }
    
    /**
     * Returns whether or not this shadow munger came from
     * a binary aspect - keep a record of whether or not we've
     * checked if we're binary otherwise we keep caluclating the 
     * same thing many times
     */
    protected boolean isBinary() {
    	if (!checkedIsBinary) {
        	ResolvedType rt = getDeclaringType();
        	if (rt != null) {
    			isBinary = ((rt.getBinaryPath() == null) ? false : true);
        	}
			checkedIsBinary = true;
		}
    	return isBinary;
    }
    
    private boolean isBinary;
    private boolean checkedIsBinary;
    
}
