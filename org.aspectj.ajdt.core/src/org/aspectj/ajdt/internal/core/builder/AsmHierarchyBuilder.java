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
 *     Mik Kersten	revisions, added additional relationships
 *     Alexandre Vasseur        support for @AJ style
 * ******************************************************************/

 
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.StringTokenizer;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.Util;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;

/**
 * At each iteration of <CODE>processCompilationUnit</CODE> the declarations for a 
 * particular compilation unit are added to the hierarchy passed as a a parameter.
 * <p>
 * Clients who extend this class need to ensure that they do not override any of the existing
 * behavior.  If they do, the structure model will not be built properly and tools such as IDE
 * structure views and ajdoc will fail.
 * <p>
 * <b>Note:</b> this class is not considered public API and the overridable 
 * methods are subject to change.
 * 
 * @author Mik Kersten
 */
public class AsmHierarchyBuilder extends ASTVisitor {

	protected AsmElementFormatter formatter = new AsmElementFormatter();
	// pr148027 - stop generating uses pointcut/pointcut used by relationship
	// until we do it in the same way as other relationships.
	public static boolean shouldAddUsesPointcut = false;
	/**
	 * Reset for every compilation unit.
	 */
	protected AjBuildConfig buildConfig;
	
	/**
	 * Reset for every compilation unit.
	 */
	protected Stack stack;

	/**
	 * Reset for every compilation unit.
	 */
	private CompilationResult currCompilationResult;
	private String filename;
	int[] lineseps;
	/**
	 * 
	 * @param cuDeclaration
	 * @param buildConfig
	 * @param structureModel	hiearchy to add this unit's declarations to
	 */
    public void buildStructureForCompilationUnit(CompilationUnitDeclaration cuDeclaration, IHierarchy structureModel, AjBuildConfig buildConfig) {
    	currCompilationResult = cuDeclaration.compilationResult();
    	filename = new String(currCompilationResult.fileName);
    	lineseps = currCompilationResult.lineSeparatorPositions;
    	LangUtil.throwIaxIfNull(currCompilationResult, "result");
        stack = new Stack();
        this.buildConfig = buildConfig;
        internalBuild(cuDeclaration, structureModel);
        this.buildConfig = null;  // clear reference since this structure is anchored in static
        currCompilationResult=null;
        stack.clear();
//        throw new RuntimeException("not implemented");
    }
	   
    private void internalBuild(CompilationUnitDeclaration unit, IHierarchy structureModel) {
        LangUtil.throwIaxIfNull(structureModel, "structureModel");        
//        if (!currCompilationResult.equals(unit.compilationResult())) {
//            throw new IllegalArgumentException("invalid unit: " + unit);
//        }
        // ---- summary
        // add unit to package (or root if no package),
        // first removing any duplicate (XXX? removes children if 3 classes in same file?)
        // push the node on the stack
        // and traverse
        
        // -- create node to add
        final File file = new File(new String(unit.getFileName()));
        final IProgramElement cuNode;
        {
            // AMC - use the source start and end from the compilation unit decl
            int startLine = getStartLine(unit);
            int endLine = getEndLine(unit);     
            SourceLocation sourceLocation 
                = new SourceLocation(file, startLine, endLine);
            sourceLocation.setOffset(unit.sourceStart);
            cuNode = new ProgramElement(
                new String(file.getName()),
                IProgramElement.Kind.FILE_JAVA,
                sourceLocation,
                0,null,null);
        }

		cuNode.addChild(new ProgramElement(
			"import declarations",
			IProgramElement.Kind.IMPORT_REFERENCE,
			null,0,null,null));

        final IProgramElement addToNode = genAddToNode(unit, structureModel);
        
        // -- remove duplicates before adding (XXX use them instead?)
        if (addToNode!=null && addToNode.getChildren()!=null) {
          for (ListIterator itt = addToNode.getChildren().listIterator(); itt.hasNext(); ) {
            IProgramElement child = (IProgramElement)itt.next();
            ISourceLocation childLoc = child.getSourceLocation();
            if (null == childLoc) {
                // XXX ok, packages have null source locations
                // signal others?
            } else if (childLoc.getSourceFile().equals(file)) {
                itt.remove();
            }
          }
        }
        // -- add and traverse
        addToNode.addChild(cuNode);     
        stack.push(cuNode);
        unit.traverse(this, unit.scope);  
        
        // -- update file map (XXX do this before traversal?)
        try {
            structureModel.addToFileMap(file.getCanonicalPath(), cuNode);
        } catch (IOException e) { 
            System.err.println("IOException " + e.getMessage() 
                + " creating path for " + file );
            // XXX signal IOException when canonicalizing file path
        }
        
	}

	/**
	 * Get/create the node (package or root) to add to.
	 */
	private IProgramElement genAddToNode(
		CompilationUnitDeclaration unit,
		IHierarchy structureModel) {
		final IProgramElement addToNode;
		{
		    ImportReference currentPackage = unit.currentPackage;
		    if (null == currentPackage) {
		        addToNode = structureModel.getRoot();
		    } else {
		        String pkgName;
		        {
		            StringBuffer nameBuffer = new StringBuffer();
		            final char[][] importName = currentPackage.getImportName();
		            final int last = importName.length-1;
		            for (int i = 0; i < importName.length; i++) {
		                nameBuffer.append(new String(importName[i]));
		                if (i < last) {
		                    nameBuffer.append('.');
		                } 
		            }
		            pkgName = nameBuffer.toString();
		        }
		    
		        IProgramElement pkgNode = null;
		        if (structureModel!=null && structureModel.getRoot()!=null && structureModel.getRoot().getChildren()!=null) {
		        	for (Iterator it = structureModel.getRoot().getChildren().iterator(); 
		            	it.hasNext(); ) {
		            	IProgramElement currNode = (IProgramElement)it.next();
		            	if (pkgName.equals(currNode.getName())) {
		                	pkgNode = currNode;
		                	break; 
		            	} 
		        	}
				}
		        if (pkgNode == null) {
		            // note packages themselves have no source location
		            pkgNode = new ProgramElement(
		                pkgName, 
		                IProgramElement.Kind.PACKAGE, 
		                new ArrayList()
		            );
		            structureModel.getRoot().addChild(pkgNode);
		        }
		        addToNode = pkgNode;
		    }
		}
		return addToNode;
	}
	
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		String name = new String(typeDeclaration.name);
		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (typeDeclaration instanceof AspectDeclaration) kind = IProgramElement.Kind.ASPECT;
		else if (TypeDeclaration.kind(typeDeclaration.modifiers) == TypeDeclaration.INTERFACE_DECL) kind = IProgramElement.Kind.INTERFACE;
		else if (TypeDeclaration.kind(typeDeclaration.modifiers) == TypeDeclaration.ENUM_DECL) kind = IProgramElement.Kind.ENUM;
		else if (TypeDeclaration.kind(typeDeclaration.modifiers) == TypeDeclaration.ANNOTATION_TYPE_DECL) kind = IProgramElement.Kind.ANNOTATION;

        //@AJ support
        if (typeDeclaration.annotations != null) {
            for (int i = 0; i < typeDeclaration.annotations.length; i++) {
                Annotation annotation = typeDeclaration.annotations[i];
                if (Arrays.equals(annotation.type.getTypeBindingPublic(scope).signature(),
                                  "Lorg/aspectj/lang/annotation/Aspect;".toCharArray())) {
                    kind = IProgramElement.Kind.ASPECT;
                }
            }
        }

        int typeModifiers = typeDeclaration.modifiers;
        if (typeDeclaration instanceof AspectDeclaration) {
        	typeModifiers = ((AspectDeclaration)typeDeclaration).getDeclaredModifiers();
        }

		IProgramElement peNode = new ProgramElement(
			name,
			kind,
			makeLocation(typeDeclaration),
			typeModifiers,		null,null);	
		peNode.setSourceSignature(genSourceSignature(typeDeclaration));
		peNode.setFormalComment(generateJavadocComment(typeDeclaration));
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		stack.pop();
	}
	
	// ??? share impl with visit(TypeDeclaration, ..) ?
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		String name = new String(memberTypeDeclaration.name);
		
        // OPTIMIZE dont call kind 3 times
		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (memberTypeDeclaration instanceof AspectDeclaration) kind = IProgramElement.Kind.ASPECT;
		else if (TypeDeclaration.kind(memberTypeDeclaration.modifiers) == TypeDeclaration.INTERFACE_DECL) kind = IProgramElement.Kind.INTERFACE;
		else if (TypeDeclaration.kind(memberTypeDeclaration.modifiers)  == TypeDeclaration.ENUM_DECL) kind = IProgramElement.Kind.ENUM;
		else if (TypeDeclaration.kind(memberTypeDeclaration.modifiers)  == TypeDeclaration.ANNOTATION_TYPE_DECL) kind = IProgramElement.Kind.ANNOTATION;

        //@AJ support
        if (memberTypeDeclaration.annotations != null) {
            for (int i = 0; i < memberTypeDeclaration.annotations.length; i++) {
                Annotation annotation = memberTypeDeclaration.annotations[i];
                if (Arrays.equals(annotation.type.getTypeBindingPublic(scope).signature(),
                                  "Lorg/aspectj/lang/annotation/Aspect;".toCharArray())) {
                    kind = IProgramElement.Kind.ASPECT;
                }
            }
        }
        
        int typeModifiers = memberTypeDeclaration.modifiers;
        if (memberTypeDeclaration instanceof AspectDeclaration) {
        	typeModifiers = ((AspectDeclaration)memberTypeDeclaration).getDeclaredModifiers();
        }

		IProgramElement peNode = new ProgramElement(
			name,
			kind,
			makeLocation(memberTypeDeclaration),
			typeModifiers,null,null);
		peNode.setSourceSignature(genSourceSignature(memberTypeDeclaration));
		peNode.setFormalComment(generateJavadocComment(memberTypeDeclaration));
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		stack.pop();
	}
	
	public boolean visit(TypeDeclaration memberTypeDeclaration, BlockScope scope) {		
		String fullName = "<undefined>";
		if (memberTypeDeclaration.allocation != null
			&& memberTypeDeclaration.allocation.type != null) {
			// Create a name something like 'new Runnable() {..}'
			fullName = "new "+memberTypeDeclaration.allocation.type.toString()+"() {..}";
		} else if (memberTypeDeclaration.binding != null
			&& memberTypeDeclaration.binding.constantPoolName() != null) {
			// If we couldn't find a nice name like 'new Runnable() {..}' then use the number after the $
			fullName = new String(memberTypeDeclaration.binding.constantPoolName());
			 
			int dollar = fullName.indexOf('$');
			fullName = fullName.substring(dollar+1);
		}

		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (TypeDeclaration.kind(memberTypeDeclaration.modifiers) == TypeDeclaration.INTERFACE_DECL) kind = IProgramElement.Kind.INTERFACE;
		else if (TypeDeclaration.kind(memberTypeDeclaration.modifiers) == TypeDeclaration.ENUM_DECL) kind = IProgramElement.Kind.ENUM;
		else if (TypeDeclaration.kind(memberTypeDeclaration.modifiers) == TypeDeclaration.ANNOTATION_TYPE_DECL) kind = IProgramElement.Kind.ANNOTATION;

        //@AJ support
        if (memberTypeDeclaration.annotations != null) {
            for (int i = 0; i < memberTypeDeclaration.annotations.length; i++) {
                Annotation annotation = memberTypeDeclaration.annotations[i];
                if (Arrays.equals(annotation.type.getTypeBindingPublic(scope).signature(),
                                  "Lorg/aspectj/lang/annotation/Aspect;".toCharArray())) {
                    kind = IProgramElement.Kind.ASPECT;
                    break;
                }
            }
        }
        
		IProgramElement peNode = new ProgramElement(
			fullName,
			kind,
			makeLocation(memberTypeDeclaration),
			memberTypeDeclaration.modifiers,null,null);
		peNode.setSourceSignature(genSourceSignature(memberTypeDeclaration));
		peNode.setFormalComment(generateJavadocComment(memberTypeDeclaration));
		// if we're something like 'new Runnable(){..}' then set the 
		// bytecodeSignature to be the typename so we can match it later 
		// when creating the structure model
		if (peNode.getBytecodeSignature() == null 
				&& memberTypeDeclaration.binding != null
				&& memberTypeDeclaration.binding.constantPoolName() != null) {
			StringTokenizer st = new StringTokenizer(
					new String(memberTypeDeclaration.binding.constantPoolName()),"/");
			while(st.hasMoreTokens()) {
				String s = st.nextToken();
				if (!st.hasMoreTokens()) {
					peNode.setBytecodeSignature(s);
				}
			}
		}

		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(TypeDeclaration memberTypeDeclaration, BlockScope scope) {
		stack.pop();
	}
	
	private String genSourceSignature(TypeDeclaration typeDeclaration) {
		StringBuffer output = new StringBuffer();
		typeDeclaration.printHeader(0, output);
		return output.toString();
	}
	
	private IProgramElement findEnclosingClass(Stack stack) {
		for (int i = stack.size()-1; i >= 0; i--) {
			IProgramElement pe = (IProgramElement)stack.get(i);
			if (pe.getKind() == IProgramElement.Kind.CLASS) {
				return pe;
			}
			
		}
		return (IProgramElement)stack.peek();
	}	
	
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {			
		IProgramElement peNode = null;
		// For intertype decls, use the modifiers from the original signature, not the generated method

		if (methodDeclaration instanceof InterTypeDeclaration) {
			InterTypeDeclaration itd = (InterTypeDeclaration) methodDeclaration;
			ResolvedMember sig = itd.getSignature();
			peNode = new ProgramElement(
						null,
						IProgramElement.Kind.ERROR,
						makeLocation(methodDeclaration),
						(sig!=null?sig.getModifiers():0),null,null);
		
		} else {
			peNode = new ProgramElement(
				null,
				IProgramElement.Kind.ERROR,
				makeLocation(methodDeclaration),
				methodDeclaration.modifiers,null,null); 
		}
		formatter.genLabelAndKind(methodDeclaration, peNode); // will set the name
		genBytecodeInfo(methodDeclaration, peNode);
		List namedPointcuts = genNamedPointcuts(methodDeclaration);
		if (shouldAddUsesPointcut) addUsesPointcutRelationsForNode(peNode, namedPointcuts, methodDeclaration);
		
		if (methodDeclaration.returnType!=null) {
			// if we don't make the distinction between ITD fields and other
			// methods, then we loose the type, for example int, for the field
			// and instead get "void".
			if (peNode.getKind().equals(IProgramElement.Kind.INTER_TYPE_FIELD)) {
				peNode.setCorrespondingType(methodDeclaration.returnType.toString());
			} else {
				if (methodDeclaration.returnType.resolvedType!=null) 
					peNode.setCorrespondingType(methodDeclaration.returnType.resolvedType.debugName());
				else 
					peNode.setCorrespondingType(null);
			}
		} else {
		  peNode.setCorrespondingType(null);	
		}
		peNode.setSourceSignature(genSourceSignature(methodDeclaration));
		peNode.setFormalComment(generateJavadocComment(methodDeclaration));
		
		// TODO: add return type test
		if (peNode.getKind().equals(IProgramElement.Kind.METHOD)) {
			if ((peNode.getName().charAt(0)=='m') && 
			       (peNode.toLabelString().equals("main(String[])")
					|| peNode.toLabelString().equals("main(java.lang.String[])"))
				&& peNode.getModifiers().contains(IProgramElement.Modifiers.STATIC)
				&& peNode.getAccessibility().equals(IProgramElement.Accessibility.PUBLIC)) {
				((IProgramElement)stack.peek()).setRunnable(true);
			}	
		}
		
		stack.push(peNode);
		return true;
	}

    private void addUsesPointcutRelationsForNode(IProgramElement peNode, List namedPointcuts, MethodDeclaration declaration) {
        for (Iterator it = namedPointcuts.iterator(); it.hasNext();) {
            ReferencePointcut rp = (ReferencePointcut) it.next();
            ResolvedMember member = getPointcutDeclaration(rp, declaration);
            if (member != null) {
                IRelationship foreward = AsmManager.getDefault().getRelationshipMap()
                		.get(peNode.getHandleIdentifier(), 
                				IRelationship.Kind.USES_POINTCUT, "uses pointcut", false, true);
                IProgramElement forwardIPE = AsmManager.getDefault().getHierarchy()
                		.findElementForSourceLine(member.getSourceLocation());
                foreward.addTarget(AsmManager.getDefault().getHandleProvider()
                		.createHandleIdentifier(forwardIPE));            
                
                IRelationship back = AsmManager.getDefault().getRelationshipMap()
                		.get(AsmManager.getDefault().getHandleProvider()
                				.createHandleIdentifier(forwardIPE), 
                				IRelationship.Kind.USES_POINTCUT, "pointcut used by", false, true);
                back.addTarget(peNode.getHandleIdentifier());             
            } 
        }        
    }
    
    private ResolvedMember getPointcutDeclaration(ReferencePointcut rp, MethodDeclaration declaration) {
    	EclipseFactory factory = ((AjLookupEnvironment)declaration.scope.environment()).factory;
		World world = factory.getWorld();
		UnresolvedType onType = rp.onType;
		if (onType == null) {
			if (declaration.binding != null) {
			    Member member = factory.makeResolvedMember(declaration.binding);
				onType = member.getDeclaringType();
			} else {
				return null;
			}
		}
		ResolvedMember[] members = onType.resolve(world).getDeclaredPointcuts();
		if (members != null) {
			for (int i = 0; i < members.length; i++) {
			    if (members[i].getName().equals(rp.name)) {
			        return members[i];
			    }
			}
		}
		return null;
    }

    /**
     * @param methodDeclaration 
     * @return	all of the named pointcuts referenced by the PCD of this declaration
     */
    private List genNamedPointcuts(MethodDeclaration methodDeclaration) {
        List pointcuts = new ArrayList();
        if (methodDeclaration instanceof AdviceDeclaration) {
            if (((AdviceDeclaration)methodDeclaration).pointcutDesignator != null) 
                addAllNamed(((AdviceDeclaration)methodDeclaration).pointcutDesignator.getPointcut(), pointcuts);
		} else if (methodDeclaration instanceof PointcutDeclaration) { 
		    if (((PointcutDeclaration)methodDeclaration).pointcutDesignator != null)
		        addAllNamed(((PointcutDeclaration)methodDeclaration).pointcutDesignator.getPointcut(), pointcuts);	
		} 
		return pointcuts;
    }

    /**
     * @param left
     * @param pointcuts	accumulator for named pointcuts
     */
    private void addAllNamed(Pointcut pointcut, List pointcuts) {
        if (pointcut == null) return;
        if (pointcut instanceof ReferencePointcut) {
			ReferencePointcut rp = (ReferencePointcut)pointcut;
			pointcuts.add(rp);
		} else if (pointcut instanceof AndPointcut) {
		    AndPointcut ap = (AndPointcut)pointcut;
		    addAllNamed(ap.getLeft(), pointcuts);
		    addAllNamed(ap.getRight(), pointcuts);
		} else if (pointcut instanceof OrPointcut) {
			OrPointcut op = (OrPointcut)pointcut;
			addAllNamed(op.getLeft(), pointcuts);
		    addAllNamed(op.getRight(), pointcuts);
		} 
    }

    private String genSourceSignature(MethodDeclaration methodDeclaration) {
		StringBuffer output = new StringBuffer();
		ASTNode.printModifiers(methodDeclaration.modifiers, output);
		methodDeclaration.printReturnType(0, output).append(methodDeclaration.selector).append('(');
		if (methodDeclaration.arguments != null) {
			for (int i = 0; i < methodDeclaration.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				methodDeclaration.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (methodDeclaration.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < methodDeclaration.thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				methodDeclaration.thrownExceptions[i].print(0, output);
			}
		}
		return output.toString();
	}
//	protected void genBytecodeInfo(MethodDeclaration methodDeclaration, IProgramElement peNode) {
//		if (methodDeclaration.binding != null) {
//			String memberName = "";
//			String memberBytecodeSignature = "";
//			try { 
//		    	EclipseFactory factory = ((AjLookupEnvironment)methodDeclaration.scope.environment()).factory;
//				Member member = factory.makeResolvedMember(methodDeclaration.binding);
//				memberName = member.getName();
//				memberBytecodeSignature = member.getSignature();
//			} catch (BCException bce) {  // bad type name 
//				memberName = "<undefined>";
//			} catch (NullPointerException npe) {
//				memberName = "<undefined>";
//			} 
//			
//			peNode.setBytecodeName(memberName);
//			peNode.setBytecodeSignature(memberBytecodeSignature);
//		}
//		((IProgramElement)stack.peek()).addChild(peNode);
//	}
	protected void genBytecodeInfo(MethodDeclaration methodDeclaration, IProgramElement peNode) {
		if (methodDeclaration.binding != null) {
			try { 
		    	    EclipseFactory factory = ((AjLookupEnvironment)methodDeclaration.scope.environment()).factory;
				Member member = factory.makeResolvedMember(methodDeclaration.binding);
				peNode.setBytecodeName(member.getName());
				peNode.setBytecodeSignature(member.getSignature());
			} catch (BCException bce) {  // bad type name 
				bce.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} 			
		}
		((IProgramElement)stack.peek()).addChild(peNode);
	}

	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
		stack.pop();
	}

	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
		int dotIndex = importRef.toString().lastIndexOf('.');
		String currPackageImport = "";
		if (dotIndex != -1) {
			currPackageImport = importRef.toString().substring(0, dotIndex);
		}
		if (!((ProgramElement)stack.peek()).getPackageName().equals(currPackageImport)) {
		
			IProgramElement peNode = new ProgramElement(
				new String(importRef.toString()),
				IProgramElement.Kind.IMPORT_REFERENCE,	
				makeLocation(importRef),
				0,
				null,null);
			
			ProgramElement imports = (ProgramElement)((ProgramElement)stack.peek()).getChildren().get(0);
			imports.addChild(0, peNode);
			stack.push(peNode);
		}
		return true;	 
	}
	public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
		int dotIndex = importRef.toString().lastIndexOf('.');
		String currPackageImport = "";
		if (dotIndex != -1) {
			currPackageImport = importRef.toString().substring(0, dotIndex);
		}
		if (!((ProgramElement)stack.peek()).getPackageName().equals(currPackageImport)) {
			stack.pop();
		}
	}

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
	    IProgramElement peNode = null;
	    if (fieldDeclaration.type == null) { // The field represents an enum value
	      peNode = new ProgramElement(
			new String(fieldDeclaration.name),IProgramElement.Kind.ENUM_VALUE,	
			makeLocation(fieldDeclaration),   fieldDeclaration.modifiers,
			null,null);
		  peNode.setCorrespondingType(fieldDeclaration.binding.type.debugName());
		} else {		
		  peNode = new ProgramElement(
			new String(fieldDeclaration.name),IProgramElement.Kind.FIELD,	
			makeLocation(fieldDeclaration),   fieldDeclaration.modifiers,
			null,null);
		  peNode.setCorrespondingType(fieldDeclaration.type.toString());
		}
		peNode.setSourceSignature(genSourceSignature(fieldDeclaration));
		peNode.setFormalComment(generateJavadocComment(fieldDeclaration));
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;		
	}

	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		stack.pop();
	}

	/**
	 * Checks if comments should be added to the model before generating.
	 */
	protected String generateJavadocComment(ASTNode astNode) {
		if (buildConfig != null && !buildConfig.isGenerateJavadocsInModelMode()) return null;
		
		StringBuffer sb = new StringBuffer(); // !!! specify length?
		boolean completed = false;
		int startIndex = -1;
		if (astNode instanceof MethodDeclaration) {
			startIndex = ((MethodDeclaration)astNode).declarationSourceStart;
		} else if (astNode instanceof FieldDeclaration) {
			startIndex = ((FieldDeclaration)astNode).declarationSourceStart;
		} else if (astNode instanceof TypeDeclaration) {
			startIndex = ((TypeDeclaration)astNode).declarationSourceStart;
		} 
		
		if (startIndex == -1) {
			return null;
		} else if (currCompilationResult.compilationUnit.getContents()[startIndex] == '/'  // look for /**
			&& currCompilationResult.compilationUnit.getContents()[startIndex+1] == '*'
			&& currCompilationResult.compilationUnit.getContents()[startIndex+2] == '*') {
			
			for (int i = startIndex; i < astNode.sourceStart && !completed; i++) {
				char curr = currCompilationResult.compilationUnit.getContents()[i];
				if (curr == '/' && sb.length() > 2 && sb.charAt(sb.length()-1) == '*') completed = true; // found */
				sb.append(currCompilationResult.compilationUnit.getContents()[i]);
			} 
			return sb.toString();
		} else {
			return null;
		}
		
	}
	
	/**
	 * 
	 */
	protected String genSourceSignature(FieldDeclaration fieldDeclaration) {	
		StringBuffer output = new StringBuffer();
		if (fieldDeclaration.type == null) { // This is an enum value
			output.append(fieldDeclaration.name); // the "," or ";" has to be put on by whatever uses the sourceSignature
			return output.toString();
		} else {
			FieldDeclaration.printModifiers(fieldDeclaration.modifiers, output);
			fieldDeclaration.type.print(0, output).append(' ').append(fieldDeclaration.name);
		}
		
		if (fieldDeclaration.initialization != null
			&& !(fieldDeclaration.initialization instanceof QualifiedAllocationExpression)) {
			output.append(" = "); //$NON-NLS-1$
			if (fieldDeclaration.initialization instanceof ExtendedStringLiteral) {
				output.append("\"<extended string literal>\"");
			} else {
				fieldDeclaration.initialization.printExpression(0, output);
			}
		} else if (fieldDeclaration.initialization instanceof QualifiedAllocationExpression) {
			output.append(" = "); //$NON-NLS-1$
			QualifiedAllocationExpression qae = (QualifiedAllocationExpression)fieldDeclaration.initialization;
			StringBuffer sb = new StringBuffer();
			qae.printExpression(0,sb);
			// if the source is of the form 'static I MY_I = new I() {};' calling 
			// printExpression on the qae returns
			//
			// new I() {
			//	   x() {
			//	     super();
			//	   }
			// }
			//
			// We want to remove the x() {super();} call. Assuming that this
			// is the first entry in the expression we can do this by finding
			// the position of the "{" and "}" - bug 148908
			int i = sb.toString().indexOf("{");
			output.append(sb.substring(0,i+1));
			int j = sb.toString().indexOf("}");
			output.append(sb.substring(j+1));
		}
		
		output.append(';');
		return output.toString();
	}


//	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
//		ProgramElementNode peNode = new ProgramElementNode(
//			new String(importRef.toString()),
//			ProgramElementNode.Kind.,	
//			makeLocation(importRef),
//			0,
//			"",
//			new ArrayList());	
//		((IProgramElement)stack.peek()).addChild(0, peNode);
//		stack.push(peNode);
//		return true;	
//	}
//	public void endVisit(ImportReference importRef,CompilationUnitScope scope) {
//		stack.pop();		
//	}

	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		if ((constructorDeclaration.bits & ASTNode.IsDefaultConstructor)!=0) {
			stack.push(null); // a little weird but does the job
			return true;	
		}
		StringBuffer argumentsSignature = new StringBuffer();
		argumentsSignature.append("(");
		if (constructorDeclaration.arguments!=null) {
		  for (int i = 0;i<constructorDeclaration.arguments.length;i++) {
			argumentsSignature.append(constructorDeclaration.arguments[i].type);
			if (i+1<constructorDeclaration.arguments.length) argumentsSignature.append(",");
		  }
		}
		argumentsSignature.append(")");
		IProgramElement peNode = new ProgramElement(
			new String(constructorDeclaration.selector),
			IProgramElement.Kind.CONSTRUCTOR,	
			makeLocation(constructorDeclaration),
			constructorDeclaration.modifiers,
			null,null);
		formatter.setParameters(constructorDeclaration, peNode);
		peNode.setModifiers(constructorDeclaration.modifiers);
		peNode.setSourceSignature(genSourceSignature(constructorDeclaration));
		
		// Fix to enable us to anchor things from ctor nodes
		if (constructorDeclaration.binding != null) {
			String memberName = "";
			String memberBytecodeSignature = "";
			try {
		    	EclipseFactory factory = ((AjLookupEnvironment)constructorDeclaration.scope.environment()).factory;
				Member member = factory.makeResolvedMember(constructorDeclaration.binding);
				memberName = member.getName();
				memberBytecodeSignature = member.getSignature();
			} catch (BCException bce) {  // bad type name 
				memberName = "<undefined>";
			} catch (NullPointerException npe) {
				memberName = "<undefined>";
			} 
			peNode.setBytecodeName(memberName);
			peNode.setBytecodeSignature(memberBytecodeSignature);
		}
		
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;	
	}
	public void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		stack.pop();
	}
	private String genSourceSignature(ConstructorDeclaration constructorDeclaration) {
		StringBuffer output = new StringBuffer();
		ASTNode.printModifiers(constructorDeclaration.modifiers, output);
		output.append(constructorDeclaration.selector).append('(');  
		if (constructorDeclaration.arguments != null) {
			for (int i = 0; i < constructorDeclaration.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				constructorDeclaration.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (constructorDeclaration.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < constructorDeclaration.thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				constructorDeclaration.thrownExceptions[i].print(0, output);
			}
		}
		return output.toString();
	}

//	public boolean visit(Clinit clinit, ClassScope scope) {
//		ProgramElementNode peNode = new ProgramElementNode(
//			"<clinit>",
//			ProgramElementNode.Kind.INITIALIZER,	
//			makeLocation(clinit),
//			clinit.modifiers,
//			"",
//			new ArrayList());	
//		((IProgramElement)stack.peek()).addChild(peNode);
//		stack.push(peNode);  
//		return false;	
//	}
//	public void endVisit(Clinit clinit, ClassScope scope) {
//		stack.pop();
//	}

	/** This method works-around an odd traverse implementation on Initializer
	 */
	private Initializer inInitializer = null;
	public boolean visit(Initializer initializer, MethodScope scope) {
		if (initializer == inInitializer) return false;
		inInitializer = initializer;
		
		IProgramElement peNode = new ProgramElement(
			"...",
			IProgramElement.Kind.INITIALIZER,	
			makeLocation(initializer),
			initializer.modifiers,null,null);
//			"",
//			new ArrayList());	
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		initializer.block.traverse(this, scope);
		stack.pop();
		inInitializer=null;
		return false;	
	}

	// ??? handle non-existant files
	protected ISourceLocation makeLocation(ASTNode node) {		
		String fileName = "";
		if (filename != null) {
			fileName = this.filename;
		}
		// AMC - different strategies based on node kind
		int startLine = getStartLine(node);
		int endLine = getEndLine(node);
		SourceLocation loc = null;
		if ( startLine <= endLine ) {
			// found a valid end line for this node...
			loc = new SourceLocation(new File(fileName), startLine, endLine);	
			loc.setOffset(node.sourceStart);
		} else {
			loc = new SourceLocation(new File(fileName), startLine);
			loc.setOffset(node.sourceStart);
		}
		return loc;
	}
  

	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	protected int getStartLine( ASTNode n){
//		if (  n instanceof AbstractVariableDeclaration ) return getStartLine( (AbstractVariableDeclaration)n);
//		if (  n instanceof AbstractMethodDeclaration ) return getStartLine( (AbstractMethodDeclaration)n);
//		if (  n instanceof TypeDeclaration ) return getStartLine( (TypeDeclaration)n);
		return Util.getLineNumber(n.sourceStart,lineseps,0,lineseps.length-1);
//		return ProblemHandler.searchLineNumber(lineseps,
//			currCompilationResult.lineSeparatorPositions,
//			n.sourceStart);		
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	protected int getEndLine( ASTNode n){
		if (  n instanceof AbstractVariableDeclaration ) return getEndLine( (AbstractVariableDeclaration)n);
		if (  n instanceof AbstractMethodDeclaration ) return getEndLine( (AbstractMethodDeclaration)n);
		if (  n instanceof TypeDeclaration ) return getEndLine( (TypeDeclaration)n);	
		return Util.getLineNumber(n.sourceEnd,lineseps,0,lineseps.length-1);
//		return ProblemHandler.searchLineNumber(lineseps,
//			currCompilationResult.lineSeparatorPositions,
//			n.sourceEnd);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
//	private int getStartLine( AbstractVariableDeclaration avd ) {
//		return ProblemHandler.searchLineNumber(
//			currCompilationResult.lineSeparatorPositions,
//			avd.declarationSourceStart);
//	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( AbstractVariableDeclaration avd ){
		return Util.getLineNumber(avd.declarationSourceEnd,lineseps,0,lineseps.length-1);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
//	private int getStartLine( AbstractMethodDeclaration amd ){
//		return ProblemHandler.searchLineNumber(
//			currCompilationResult.lineSeparatorPositions,
//			amd.declarationSourceStart);
//	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( AbstractMethodDeclaration amd) {
		return Util.getLineNumber(amd.declarationSourceEnd,lineseps,0,lineseps.length-1);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
//	private int getStartLine( TypeDeclaration td ){
//		return ProblemHandler.searchLineNumber(
//			currCompilationResult.lineSeparatorPositions,
//			td.declarationSourceStart);
//	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( TypeDeclaration td){
		return Util.getLineNumber(td.declarationSourceEnd,lineseps,0,lineseps.length-1);
	}


}
