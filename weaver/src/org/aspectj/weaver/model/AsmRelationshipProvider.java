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

package org.aspectj.weaver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;

public class AsmRelationshipProvider {

	protected static AsmRelationshipProvider INSTANCE = new AsmRelationshipProvider();

	public static final String ADVISES = "advises";
	public static final String ADVISED_BY = "advised by";
	public static final String DECLARES_ON = "declares on";
	public static final String DECLAREDY_BY = "declared by";
	public static final String SOFTENS = "softens";
	public static final String SOFTENED_BY = "softened by";
	public static final String MATCHED_BY = "matched by";
	public static final String MATCHES_DECLARE = "matches declare";
	public static final String INTER_TYPE_DECLARES = "declared on";
	public static final String INTER_TYPE_DECLARED_BY = "aspect declarations";

	public static final String ANNOTATES = "annotates";
	public static final String ANNOTATED_BY = "annotated by";

	public static void checkerMunger(AsmManager asm, Shadow shadow, Checker checker) {
		if (asm == null) // !AsmManager.isCreatingModel())
			return;
		if (shadow.getSourceLocation() == null || checker.getSourceLocation() == null)
			return;

		if (World.createInjarHierarchy) {
			createHierarchy(asm, checker);
		}

		// Ensure a node for the target exists
		IProgramElement targetNode = getNode(asm, shadow);
		if (targetNode == null)
			return;
		String targetHandle = asm.getHandleProvider().createHandleIdentifier(targetNode);
		if (targetHandle == null)
			return;

		IProgramElement sourceNode = asm.getHierarchy().findElementForSourceLine(checker.getSourceLocation());
		String sourceHandle = asm.getHandleProvider().createHandleIdentifier(sourceNode);
		if (sourceHandle == null)
			return;

		IRelationshipMap mapper = asm.getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE, MATCHED_BY, false, true);
		foreward.addTarget(targetHandle);

		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, MATCHES_DECLARE, false, true);
		if (back != null && back.getTargets() != null) {
			back.addTarget(sourceHandle);
		}
		if (sourceNode.getSourceLocation() != null) {
			asm.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		}

	}

	// For ITDs
	public void addRelationship(AsmManager asm, ResolvedType onType, ResolvedTypeMunger munger, ResolvedType originatingAspect) {

		if (asm == null)// !AsmManager.isCreatingModel())
			return;
		if (originatingAspect.getSourceLocation() != null) {
			String sourceHandle = "";
			IProgramElement sourceNode = null;
			if (munger.getSourceLocation() != null && munger.getSourceLocation().getOffset() != -1) {
				sourceNode = asm.getHierarchy().findElementForSourceLine(munger.getSourceLocation());
				sourceHandle = asm.getHandleProvider().createHandleIdentifier(sourceNode);
			} else {
				sourceNode = asm.getHierarchy().findElementForSourceLine(originatingAspect.getSourceLocation());
				sourceHandle = asm.getHandleProvider().createHandleIdentifier(sourceNode);
			}
			if (sourceHandle == null)
				return;
			IProgramElement targetNode = asm.getHierarchy().findElementForSourceLine(onType.getSourceLocation());
			String targetHandle = asm.getHandleProvider().createHandleIdentifier(targetNode);
			if (targetHandle == null)
				return;

			IRelationshipMap mapper = asm.getRelationshipMap();
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES, false,
					true);
			foreward.addTarget(targetHandle);

			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY, false,
					true);
			back.addTarget(sourceHandle);
			asm.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		}
	}

	// public void addDeclareParentsRelationship(ISourceLocation decp,
	// ResolvedType targetType, List newParents) {
	// if (!AsmManager.isCreatingModel())
	// return;
	//
	// IProgramElement sourceNode =
	// AsmManager.getDefault().getHierarchy().findElementForSourceLine(decp);
	// String sourceHandle =
	// AsmManager.getDefault().getHandleProvider().createHandleIdentifier
	// (sourceNode);
	// if (sourceHandle == null)
	// return;
	//
	// IProgramElement targetNode = AsmManager.getDefault().getHierarchy()
	// .findElementForSourceLine(targetType.getSourceLocation());
	// String targetHandle =
	// AsmManager.getDefault().getHandleProvider().createHandleIdentifier
	// (targetNode);
	// if (targetHandle == null)
	// return;
	//
	// IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
	// IRelationship foreward = mapper.get(sourceHandle,
	// IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES, false, true);
	// foreward.addTarget(targetHandle);
	//
	// IRelationship back = mapper.get(targetHandle,
	// IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY, false,
	// true);
	// back.addTarget(sourceHandle);
	// }

	/**
	 * Adds a declare annotation relationship, sometimes entities don't have source locs (methods/fields) so use other variants of
	 * this method if that is the case as they will look the entities up in the structure model.
	 */
	public void addDeclareAnnotationRelationship(AsmManager asm, ISourceLocation declareAnnotationLocation,
			ISourceLocation annotatedLocation) {
		if (asm == null) // !AsmManager.isCreatingModel())
			return;

		IProgramElement sourceNode = asm.getHierarchy().findElementForSourceLine(declareAnnotationLocation);
		String sourceHandle = asm.getHandleProvider().createHandleIdentifier(sourceNode);
		if (sourceHandle == null)
			return;

		IProgramElement targetNode = asm.getHierarchy().findElementForSourceLine(annotatedLocation);
		String targetHandle = asm.getHandleProvider().createHandleIdentifier(targetNode);
		if (targetHandle == null)
			return;

		IRelationshipMap mapper = asm.getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES, false, true);
		foreward.addTarget(targetHandle);

		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY, false, true);
		back.addTarget(sourceHandle);
		if (sourceNode.getSourceLocation() != null) {
			asm.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		}
	}

	/**
	 * Creates the hierarchy for binary aspects
	 */
	public static void createHierarchy(AsmManager asm, ShadowMunger munger) {
		if (!munger.isBinary())
			return;

		IProgramElement sourceFileNode = asm.getHierarchy().findElementForSourceLine(munger.getSourceLocation());
		// the call to findElementForSourceLine(ISourceLocation) returns a file
		// node
		// if it can't find a node in the hierarchy for the given
		// sourcelocation.
		// Therefore, if this is returned, we know we can't find one and have to
		// continue to fault in the model.
		if (!sourceFileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)) {
			return;
		}

		ResolvedType aspect = munger.getDeclaringType();

		// create the class file node
		IProgramElement classFileNode = new ProgramElement(asm, sourceFileNode.getName(), IProgramElement.Kind.FILE, munger
				.getBinarySourceLocation(aspect.getSourceLocation()), 0, null, null);

		// create package ipe if one exists....
		IProgramElement root = asm.getHierarchy().getRoot();
		IProgramElement binaries = asm.getHierarchy().findElementForLabel(root, IProgramElement.Kind.SOURCE_FOLDER, "binaries");
		if (binaries == null) {
			binaries = new ProgramElement(asm, "binaries", IProgramElement.Kind.SOURCE_FOLDER, new ArrayList());
			root.addChild(binaries);
		}
		// if (aspect.getPackageName() != null) {
		String packagename = aspect.getPackageName() == null ? "" : aspect.getPackageName();
		// check that there doesn't already exist a node with this name
		IProgramElement pkgNode = asm.getHierarchy().findElementForLabel(binaries, IProgramElement.Kind.PACKAGE, packagename);
		// note packages themselves have no source location
		if (pkgNode == null) {
			pkgNode = new ProgramElement(asm, packagename, IProgramElement.Kind.PACKAGE, new ArrayList());
			binaries.addChild(pkgNode);
			pkgNode.addChild(classFileNode);
		} else {
			// need to add it first otherwise the handle for classFileNode
			// may not be generated correctly if it uses information from
			// it's parent node
			pkgNode.addChild(classFileNode);
			for (Iterator iter = pkgNode.getChildren().iterator(); iter.hasNext();) {
				IProgramElement element = (IProgramElement) iter.next();
				if (!element.equals(classFileNode) && element.getHandleIdentifier().equals(classFileNode.getHandleIdentifier())) {
					// already added the classfile so have already
					// added the structure for this aspect
					pkgNode.removeChild(classFileNode);
					return;
				}
			}
		}
		// } else {
		// // need to add it first otherwise the handle for classFileNode
		// // may not be generated correctly if it uses information from
		// // it's parent node
		// root.addChild(classFileNode);
		// for (Iterator iter = root.getChildren().iterator(); iter.hasNext();) {
		// IProgramElement element = (IProgramElement) iter.next();
		// if (!element.equals(classFileNode) && element.getHandleIdentifier().equals(classFileNode.getHandleIdentifier())) {
		// // already added the sourcefile so have already
		// // added the structure for this aspect
		// root.removeChild(classFileNode);
		// return;
		// }
		// }
		// }

		// add and create empty import declaration ipe
		classFileNode.addChild(new ProgramElement(asm, "import declarations", IProgramElement.Kind.IMPORT_REFERENCE, null, 0, null,
				null));

		// add and create aspect ipe
		IProgramElement aspectNode = new ProgramElement(asm, aspect.getSimpleName(), IProgramElement.Kind.ASPECT, munger
				.getBinarySourceLocation(aspect.getSourceLocation()), aspect.getModifiers(), null, null);
		classFileNode.addChild(aspectNode);

		addChildNodes(asm, munger, aspectNode, aspect.getDeclaredPointcuts());

		addChildNodes(asm, munger, aspectNode, aspect.getDeclaredAdvice());
		addChildNodes(asm, munger, aspectNode, aspect.getDeclares());
	}

	private static void addChildNodes(AsmManager asm, ShadowMunger munger, IProgramElement parent, ResolvedMember[] children) {
		for (int i = 0; i < children.length; i++) {
			ResolvedMember pcd = children[i];
			if (pcd instanceof ResolvedPointcutDefinition) {
				ResolvedPointcutDefinition rpcd = (ResolvedPointcutDefinition) pcd;
				ISourceLocation sLoc = rpcd.getPointcut().getSourceLocation();
				if (sLoc == null) {
					sLoc = rpcd.getSourceLocation();
				}
				parent.addChild(new ProgramElement(asm, pcd.getName(), IProgramElement.Kind.POINTCUT, munger
						.getBinarySourceLocation(sLoc), pcd.getModifiers(), null, Collections.EMPTY_LIST));
			}
		}
	}

	private static void addChildNodes(AsmManager asm, ShadowMunger munger, IProgramElement parent, Collection children) {
		int deCtr = 1;
		int dwCtr = 1;
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning decl = (DeclareErrorOrWarning) element;
				int counter = 0;
				if (decl.isError()) {
					counter = deCtr++;
				} else {
					counter = dwCtr++;
				}
				parent.addChild(createDeclareErrorOrWarningChild(asm, munger, decl, counter));
			} else if (element instanceof Advice) {
				Advice advice = (Advice) element;
				parent.addChild(createAdviceChild(asm, advice));
			}
		}
	}

	private static IProgramElement createDeclareErrorOrWarningChild(AsmManager asm, ShadowMunger munger,
			DeclareErrorOrWarning decl, int count) {
		IProgramElement deowNode = new ProgramElement(asm, decl.getName(), decl.isError() ? IProgramElement.Kind.DECLARE_ERROR
				: IProgramElement.Kind.DECLARE_WARNING, munger.getBinarySourceLocation(decl.getSourceLocation()), decl
				.getDeclaringType().getModifiers(), null, null);
		deowNode.setDetails("\"" + AsmRelationshipUtils.genDeclareMessage(decl.getMessage()) + "\"");
		if (count != -1) {
			deowNode.setBytecodeName(decl.getName() + "_" + count);
		}
		return deowNode;
	}

	private static IProgramElement createAdviceChild(AsmManager asm, Advice advice) {
		IProgramElement adviceNode = new ProgramElement(asm, advice.getKind().getName(), IProgramElement.Kind.ADVICE, advice
				.getBinarySourceLocation(advice.getSourceLocation()), advice.getSignature().getModifiers(), null,
				Collections.EMPTY_LIST);
		adviceNode.setDetails(AsmRelationshipUtils.genPointcutDetails(advice.getPointcut()));
		adviceNode.setBytecodeName(advice.getSignature().getName());
		// String nn = advice.getSignature().getName();
		// if (counter != 1) {
		// adviceNode.setBytecodeName(advice.getKind().getName() + "$"
		// + counter + "$");
		// }
		return adviceNode;
	}

	public static String getHandle(Advice advice, AsmManager asm) {
		if (null == advice.handle) {
			ISourceLocation sl = advice.getSourceLocation();
			if (sl != null) {
				IProgramElement ipe = asm.getHierarchy().findElementForSourceLine(sl);
				advice.handle = asm.getHandleProvider().createHandleIdentifier(ipe);
			}
		}
		return advice.handle;
	}

	public static void adviceMunger(AsmManager asm, Shadow shadow, ShadowMunger munger) {
		if (asm == null) // !AsmManager.isCreatingModel())
			return;
		if (munger instanceof Advice) {
			Advice advice = (Advice) munger;

			if (advice.getKind().isPerEntry() || advice.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}

			if (World.createInjarHierarchy) {
				createHierarchy(asm, advice);
			}

			IRelationshipMap mapper = asm.getRelationshipMap();
			IProgramElement targetNode = getNode(asm, shadow);
			if (targetNode == null)
				return;
			boolean runtimeTest = advice.hasDynamicTests();

			// Work out extra info to inform interested UIs !
			IProgramElement.ExtraInformation ai = new IProgramElement.ExtraInformation();

			String adviceHandle = getHandle(advice, asm);
			if (adviceHandle == null)
				return;

			// What kind of advice is it?
			// TODO: Prob a better way to do this but I just want to
			// get it into CVS !!!
			AdviceKind ak = ((Advice) munger).getKind();
			ai.setExtraAdviceInformation(ak.getName());
			IProgramElement adviceElement = asm.getHierarchy().findElementForHandle(adviceHandle);
			if (adviceElement != null) {
				adviceElement.setExtraInfo(ai);
			}
			String targetHandle = targetNode.getHandleIdentifier();
			if (advice.getKind().equals(AdviceKind.Softener)) {
				IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.DECLARE_SOFT, SOFTENS, runtimeTest, true);
				if (foreward != null)
					foreward.addTarget(targetHandle);// foreward.getTargets().add
				// (targetHandle);

				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, SOFTENED_BY, runtimeTest, true);
				if (back != null)
					back.addTarget(adviceHandle);// back.getTargets().add(
				// adviceHandle);
			} else {
				IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.ADVICE, ADVISES, runtimeTest, true);
				if (foreward != null)
					foreward.addTarget(targetHandle);// foreward.getTargets().add
				// (targetHandle);

				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, ADVISED_BY, runtimeTest, true);
				if (back != null)
					back.addTarget(adviceHandle);// back.getTargets().add(
				// adviceHandle);
			}
			if (adviceElement.getSourceLocation() != null) {
				asm.addAspectInEffectThisBuild(adviceElement.getSourceLocation().getSourceFile());
			}
		}
	}

	protected static IProgramElement getNode(AsmManager model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();

		IProgramElement enclosingNode = lookupMember(model.getHierarchy(), enclosingMember);
		if (enclosingNode == null) {
			Lint.Kind err = shadow.getIWorld().getLint().shadowNotInStructure;
			if (err.isEnabled()) {
				err.signal(shadow.toString(), shadow.getSourceLocation());
			}
			return null;
		}

		Member shadowSig = shadow.getSignature();
		// pr235204
		if (shadow.getKind() == Shadow.MethodCall || !shadowSig.equals(enclosingMember)) {
			IProgramElement bodyNode = findOrCreateCodeNode(model, enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}

	private static boolean sourceLinesMatch(ISourceLocation loc1, ISourceLocation loc2) {
		if (loc1.getLine() != loc2.getLine())
			return false;
		return true;
	}

	/**
	 * Finds or creates a code IProgramElement for the given shadow.
	 * 
	 * The byteCodeName of the created node is set to 'shadowSig.getName() + "!" + counter', eg "println!3". The counter is the
	 * occurence count of children within the enclosingNode which have the same name. So, for example, if a method contains two
	 * System.out.println statements, the first one will have byteCodeName 'println!1' and the second will have byteCodeName
	 * 'println!2'. This is to ensure the two nodes have unique handles when the handles do not depend on sourcelocations.
	 * 
	 * Currently the shadows are examined in the sequence they appear in the source file. This means that the counters are
	 * consistent over incremental builds. All aspects are compiled up front and any new aspect created will force a full build.
	 * Moreover, if the body of the enclosingShadow is changed, then the model for this is rebuilt from scratch.
	 */
	private static IProgramElement findOrCreateCodeNode(AsmManager asm, IProgramElement enclosingNode, Member shadowSig,
			Shadow shadow) {
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext();) {
			IProgramElement node = (IProgramElement) it.next();
			int excl = node.getBytecodeName().lastIndexOf('!');
			if (((excl != -1 && shadowSig.getName().equals(node.getBytecodeName().substring(0, excl))) || shadowSig.getName()
					.equals(node.getBytecodeName()))
					&& shadowSig.getSignature().equals(node.getBytecodeSignature())
					&& sourceLinesMatch(node.getSourceLocation(), shadow.getSourceLocation())) {
				return node;
			}
		}

		ISourceLocation sl = shadow.getSourceLocation();

		// XXX why not use shadow file? new SourceLocation(sl.getSourceFile(),
		// sl.getLine()),
		SourceLocation peLoc = new SourceLocation(enclosingNode.getSourceLocation().getSourceFile(), sl.getLine());
		peLoc.setOffset(sl.getOffset());
		IProgramElement peNode = new ProgramElement(asm, shadow.toString(), IProgramElement.Kind.CODE, peLoc, 0, null, null);

		// check to see if the enclosing shadow already has children with the
		// same name. If so we want to add a counter to the byteCodeName
		// otherwise
		// we wont get unique handles
		int numberOfChildrenWithThisName = 0;
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext();) {
			IProgramElement child = (IProgramElement) it.next();
			if (child.getName().equals(shadow.toString())) {
				numberOfChildrenWithThisName++;
			}
		}
		peNode.setBytecodeName(shadowSig.getName() + "!" + String.valueOf(numberOfChildrenWithThisName + 1));
		peNode.setBytecodeSignature(shadowSig.getSignature());
		enclosingNode.addChild(peNode);
		return peNode;
	}

	protected static IProgramElement lookupMember(IHierarchy model, Member member) {
		UnresolvedType declaringType = member.getDeclaringType();
		IProgramElement classNode = model.findElementForType(declaringType.getPackageName(), declaringType.getClassName());
		return findMemberInClass(classNode, member);
	}

	protected static IProgramElement findMemberInClass(IProgramElement classNode, Member member) {
		if (classNode == null)
			return null; // XXX remove this check
		for (Iterator it = classNode.getChildren().iterator(); it.hasNext();) {
			IProgramElement node = (IProgramElement) it.next();
			if (member.getName().equals(node.getBytecodeName()) && member.getSignature().equals(node.getBytecodeSignature())) {
				return node;
			}
		}
		// if we can't find the member, we'll just put it in the class
		return classNode;
	}

	// private static IProgramElement.Kind genShadowKind(Shadow shadow) {
	// IProgramElement.Kind shadowKind;
	// if (shadow.getKind() == Shadow.MethodCall
	// || shadow.getKind() == Shadow.ConstructorCall
	// || shadow.getKind() == Shadow.FieldGet
	// || shadow.getKind() == Shadow.FieldSet
	// || shadow.getKind() == Shadow.ExceptionHandler) {
	// return IProgramElement.Kind.CODE;
	//			
	// } else if (shadow.getKind() == Shadow.MethodExecution) {
	// return IProgramElement.Kind.METHOD;
	//			
	// } else if (shadow.getKind() == Shadow.ConstructorExecution) {
	// return IProgramElement.Kind.CONSTRUCTOR;
	//			
	// } else if (shadow.getKind() == Shadow.PreInitialization
	// || shadow.getKind() == Shadow.Initialization) {
	// return IProgramElement.Kind.CLASS;
	//			
	// } else if (shadow.getKind() == Shadow.AdviceExecution) {
	// return IProgramElement.Kind.ADVICE;
	//			
	// } else {
	// return IProgramElement.Kind.ERROR;
	// }
	// }

	public static AsmRelationshipProvider getDefault() {
		return INSTANCE;
	}

	/**
	 * Add a relationship to the known set for a declare @method/@constructor construct. Locating the method is a messy (for messy
	 * read 'fragile') bit of code that could break at any moment but it's working for my simple testcase. Currently just fails
	 * silently if any of the lookup code doesn't find anything...
	 * 
	 * @param hierarchy
	 */
	public void addDeclareAnnotationMethodRelationship(ISourceLocation sourceLocation, String typename, ResolvedMember method,
			AsmManager structureModel) {
		if (structureModel == null) // !AsmManager.isCreatingModel())
			return;

		String pkg = null;
		String type = typename;
		int packageSeparator = typename.lastIndexOf(".");
		if (packageSeparator != -1) {
			pkg = typename.substring(0, packageSeparator);
			type = typename.substring(packageSeparator + 1);
		}

		IHierarchy hierarchy = structureModel.getHierarchy();

		IProgramElement typeElem = hierarchy.findElementForType(pkg, type);
		if (typeElem == null)
			return;

		StringBuffer parmString = new StringBuffer("(");
		UnresolvedType[] args = method.getParameterTypes();
		// Type[] args = method.getArgumentTypes();
		for (int i = 0; i < args.length; i++) {
			String s = args[i].getName();// Utility.signatureToString(args[i].
			// getName()getSignature(), false);
			parmString.append(s);
			if ((i + 1) < args.length)
				parmString.append(",");
		}
		parmString.append(")");
		IProgramElement methodElem = null;

		if (method.getName().startsWith("<init>")) {
			// its a ctor
			methodElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.CONSTRUCTOR, type + parmString);
			if (methodElem == null && args.length == 0)
				methodElem = typeElem; // assume default ctor
		} else {
			// its a method
			methodElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.METHOD, method.getName() + parmString);
		}

		if (methodElem == null)
			return;

		try {
			String targetHandle = methodElem.getHandleIdentifier();
			if (targetHandle == null)
				return;

			IProgramElement sourceNode = hierarchy.findElementForSourceLine(sourceLocation);
			String sourceHandle = structureModel.getHandleProvider().createHandleIdentifier(sourceNode);
			if (sourceHandle == null)
				return;

			IRelationshipMap mapper = structureModel.getRelationshipMap();
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES, false, true);
			foreward.addTarget(targetHandle);

			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY, false, true);
			back.addTarget(sourceHandle);
		} catch (Throwable t) { // I'm worried about that code above, this will
			// make sure we don't explode if it plays up
			t.printStackTrace(); // I know I know .. but I don't want to lose
			// it!
		}
	}

	/**
	 * Add a relationship to the known set for a declare @field construct. Locating the field is trickier than it might seem since
	 * we have no line number info for it, we have to dig through the structure model under the fields' type in order to locate it.
	 * Currently just fails silently if any of the lookup code doesn't find anything...
	 */
	public void addDeclareAnnotationFieldRelationship(AsmManager asm, ISourceLocation sourceLocation, String typename,
			ResolvedMember field) {
		if (asm == null) // !AsmManager.isCreatingModel())
			return;

		String pkg = null;
		String type = typename;
		int packageSeparator = typename.lastIndexOf(".");
		if (packageSeparator != -1) {
			pkg = typename.substring(0, packageSeparator);
			type = typename.substring(packageSeparator + 1);
		}
		IHierarchy hierarchy = asm.getHierarchy();
		IProgramElement typeElem = hierarchy.findElementForType(pkg, type);
		if (typeElem == null)
			return;

		IProgramElement fieldElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.FIELD, field.getName());
		if (fieldElem == null)
			return;

		String targetHandle = fieldElem.getHandleIdentifier();
		if (targetHandle == null)
			return;

		IProgramElement sourceNode = hierarchy.findElementForSourceLine(sourceLocation);
		String sourceHandle = asm.getHandleProvider().createHandleIdentifier(sourceNode);
		if (sourceHandle == null)
			return;

		IRelationshipMap mapper = asm.getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES, false, true);
		foreward.addTarget(targetHandle);

		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY, false, true);
		back.addTarget(sourceHandle);
	}

}
