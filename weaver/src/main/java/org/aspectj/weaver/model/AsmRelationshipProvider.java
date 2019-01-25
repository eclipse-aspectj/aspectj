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

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.HandleProviderDelimiter;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger.Kind;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelShadow;
import org.aspectj.weaver.bcel.BcelTypeMunger;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePatternList;

public class AsmRelationshipProvider {

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

	// public static final String REMOVES_ANNOTATION = "removes annotation";
	// public static final String ANNOTATION_REMOVED_BY = "annotated removed by";

	/**
	 * Add a relationship for a declare error or declare warning
	 */
	public static void addDeclareErrorOrWarningRelationship(AsmManager model, Shadow affectedShadow, Checker deow) {
		if (model == null) {
			return;
		}
		if (affectedShadow.getSourceLocation() == null || deow.getSourceLocation() == null) {
			return;
		}

		if (World.createInjarHierarchy) {
			createHierarchyForBinaryAspect(model, deow);
		}

		IProgramElement targetNode = getNode(model, affectedShadow);
		if (targetNode == null) {
			return;
		}
		String targetHandle = targetNode.getHandleIdentifier();
		if (targetHandle == null) {
			return;
		}

		IProgramElement sourceNode = model.getHierarchy().findElementForSourceLine(deow.getSourceLocation());
		String sourceHandle = sourceNode.getHandleIdentifier();
		if (sourceHandle == null) {
			return;
		}

		IRelationshipMap relmap = model.getRelationshipMap();
		IRelationship foreward = relmap.get(sourceHandle, IRelationship.Kind.DECLARE, MATCHED_BY, false, true);
		foreward.addTarget(targetHandle);

		IRelationship back = relmap.get(targetHandle, IRelationship.Kind.DECLARE, MATCHES_DECLARE, false, true);
		if (back != null && back.getTargets() != null) {
			back.addTarget(sourceHandle);
		}
		if (sourceNode.getSourceLocation() != null) {
			model.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		}
	}

	private static boolean isMixinRelated(ResolvedTypeMunger typeTransformer) {
		Kind kind = typeTransformer.getKind();
		return kind == ResolvedTypeMunger.MethodDelegate2 || kind == ResolvedTypeMunger.FieldHost
				|| (kind == ResolvedTypeMunger.Parent && ((NewParentTypeMunger) typeTransformer).isMixin());
	}

	/**
	 * Add a relationship for a type transformation (declare parents, intertype method declaration, declare annotation on type).
	 */
	public static void addRelationship(AsmManager model, ResolvedType onType, ResolvedTypeMunger typeTransformer,
			ResolvedType originatingAspect) {
		if (model == null) {
			return;
		}

		if (World.createInjarHierarchy && isBinaryAspect(originatingAspect)) {
			createHierarchy(model, typeTransformer, originatingAspect);
		}

		if (originatingAspect.getSourceLocation() != null) {
			String sourceHandle = "";
			IProgramElement sourceNode = null;
			if (typeTransformer.getSourceLocation() != null && typeTransformer.getSourceLocation().getOffset() != -1
					&& !isMixinRelated(typeTransformer)) {
				sourceNode = model.getHierarchy().findElementForType(originatingAspect.getPackageName(),
						originatingAspect.getClassName());
				IProgramElement closer = model.getHierarchy().findCloserMatchForLineNumber(sourceNode,
						typeTransformer.getSourceLocation().getLine());
				if (closer != null) {
					sourceNode = closer;
				}
				if (sourceNode == null) {
					// This can be caused by the aspect defining the type munger actually being on the classpath and not the
					// inpath or aspectpath. Rather than NPE at the next line, let's have another go at faulting it in.
					// This inner loop is a small duplicate of the outer loop that attempts to find something closer than
					// the type declaration
					if (World.createInjarHierarchy) {
						createHierarchy(model, typeTransformer, originatingAspect);
						if (typeTransformer.getSourceLocation() != null && typeTransformer.getSourceLocation().getOffset() != -1
								&& !isMixinRelated(typeTransformer)) {
							sourceNode = model.getHierarchy().findElementForType(originatingAspect.getPackageName(),
									originatingAspect.getClassName());
							IProgramElement closer2 = model.getHierarchy().findCloserMatchForLineNumber(sourceNode,
									typeTransformer.getSourceLocation().getLine());
							if (closer2 != null) {
								sourceNode = closer2;
							}
						} else {
							sourceNode = model.getHierarchy().findElementForType(originatingAspect.getPackageName(),
									originatingAspect.getClassName());
						}
					}
				}
				sourceHandle = sourceNode.getHandleIdentifier();
			} else {
				sourceNode = model.getHierarchy().findElementForType(originatingAspect.getPackageName(),
						originatingAspect.getClassName());
				// sourceNode =
				// asm.getHierarchy().findElementForSourceLine(originatingAspect
				// .getSourceLocation());
				sourceHandle = sourceNode.getHandleIdentifier();
			}
			// sourceNode =
			// asm.getHierarchy().findElementForType(originatingAspect
			// .getPackageName(),
			// originatingAspect.getClassName());
			// // sourceNode =
			// asm.getHierarchy().findElementForSourceLine(munger
			// .getSourceLocation());
			// sourceHandle =
			// asm.getHandleProvider().createHandleIdentifier(sourceNode);
			if (sourceHandle == null) {
				return;
			}
			String targetHandle = findOrFakeUpNode(model, onType);
			if (targetHandle == null) {
				return;
			}
			IRelationshipMap mapper = model.getRelationshipMap();
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES, false,
					true);
			foreward.addTarget(targetHandle);

			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY, false,
					true);
			back.addTarget(sourceHandle);
			if (sourceNode != null && sourceNode.getSourceLocation() != null) {
				// May have been a bug in the compiled aspect - so it didn't get put in the model
				model.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
			}
		}
	}

	private static String findOrFakeUpNode(AsmManager model, ResolvedType onType) {
		IHierarchy hierarchy = model.getHierarchy();
		ISourceLocation sourceLocation = onType.getSourceLocation();
		String canonicalFilePath = model.getCanonicalFilePath(sourceLocation.getSourceFile());
		int lineNumber = sourceLocation.getLine();
		// Find the relevant source file node first
		IProgramElement node = hierarchy.findNodeForSourceFile(hierarchy.getRoot(), canonicalFilePath);
		if (node == null) {
			// Does not exist in the model - probably an inpath
			String bpath = onType.getBinaryPath();
			if (bpath == null) {
				return model.getHandleProvider().createHandleIdentifier(createFileStructureNode(model, canonicalFilePath));
			} else {
				IProgramElement programElement = model.getHierarchy().getRoot();
				// =Foo/,<g(G.class[G
				StringBuffer phantomHandle = new StringBuffer();

				// =Foo
				phantomHandle.append(programElement.getHandleIdentifier());

				// /, - the comma is a 'well defined char' that means inpath
				phantomHandle.append(HandleProviderDelimiter.PACKAGEFRAGMENTROOT.getDelimiter()).append(
						HandleProviderDelimiter.PHANTOM.getDelimiter());

				int pos = bpath.indexOf('!');
				if (pos != -1) {
					// jar or dir
					String jarPath = bpath.substring(0, pos);
					String element = model.getHandleElementForInpath(jarPath);
					if (element != null) {
						phantomHandle.append(element);
					}
				}

				// <g
				String packageName = onType.getPackageName();
				phantomHandle.append(HandleProviderDelimiter.PACKAGEFRAGMENT.getDelimiter()).append(packageName);

				// (G.class
				// could fix the binary path to only be blah.class bit
				int dotClassPosition = bpath.lastIndexOf(".class");// what to do if -1
				if (dotClassPosition == -1) {
					phantomHandle.append(HandleProviderDelimiter.CLASSFILE.getDelimiter()).append("UNKNOWN.class");
				} else {
					int startPosition = dotClassPosition;
					char ch;
					while (startPosition > 0 && ((ch = bpath.charAt(startPosition)) != '/' && ch != '\\' && ch != '!')) {
						startPosition--;
					}
					String classFile = bpath.substring(startPosition + 1, dotClassPosition + 6);
					phantomHandle.append(HandleProviderDelimiter.CLASSFILE.getDelimiter()).append(classFile);
				}

				// [G
				phantomHandle.append(HandleProviderDelimiter.TYPE.getDelimiter()).append(onType.getClassName());

				return phantomHandle.toString();
			}
		} else {
			// Check if there is a more accurate child node of that source file node:
			IProgramElement closernode = hierarchy.findCloserMatchForLineNumber(node, lineNumber);
			if (closernode == null) {
				return node.getHandleIdentifier();
			} else {
				return closernode.getHandleIdentifier();
			}
		}

	}

	public static IProgramElement createFileStructureNode(AsmManager asm, String sourceFilePath) {
		// SourceFilePath might have originated on windows on linux...
		int lastSlash = sourceFilePath.lastIndexOf('\\');
		if (lastSlash == -1) {
			lastSlash = sourceFilePath.lastIndexOf('/');
		}
		// '!' is used like in URLs "c:/blahblah/X.jar!a/b.class"
		int i = sourceFilePath.lastIndexOf('!');
		int j = sourceFilePath.indexOf(".class");
		if (i > lastSlash && i != -1 && j != -1) {
			// we are a binary aspect in the default package
			lastSlash = i;
		}
		String fileName = sourceFilePath.substring(lastSlash + 1);
		IProgramElement fileNode = new ProgramElement(asm, fileName, IProgramElement.Kind.FILE_JAVA, new SourceLocation(new File(
				sourceFilePath), 1, 1), 0, null, null);
		// fileNode.setSourceLocation();
		fileNode.addChild(IHierarchy.NO_STRUCTURE);
		return fileNode;
	}

	private static boolean isBinaryAspect(ResolvedType aspect) {
		return aspect.getBinaryPath() != null;
	}

	/**
	 * Returns the binarySourceLocation for the given sourcelocation. This isn't cached because it's used when faulting in the
	 * binary nodes and is called with ISourceLocations for all advice, pointcuts and deows contained within the
	 * resolvedDeclaringAspect.
	 */
	private static ISourceLocation getBinarySourceLocation(ResolvedType aspect, ISourceLocation sl) {
		if (sl == null) {
			return null;
		}
		String sourceFileName = null;
		if (aspect instanceof ReferenceType) {
			String s = ((ReferenceType) aspect).getDelegate().getSourcefilename();
			int i = s.lastIndexOf('/');
			if (i != -1) {
				sourceFileName = s.substring(i + 1);
			} else {
				sourceFileName = s;
			}
		}
		ISourceLocation sLoc = new SourceLocation(getBinaryFile(aspect), sl.getLine(), sl.getEndLine(),
				((sl.getColumn() == 0) ? ISourceLocation.NO_COLUMN : sl.getColumn()), sl.getContext(), sourceFileName);
		return sLoc;
	}

	private static ISourceLocation createSourceLocation(String sourcefilename, ResolvedType aspect, ISourceLocation sl) {
		ISourceLocation sLoc = new SourceLocation(getBinaryFile(aspect), sl.getLine(), sl.getEndLine(),
				((sl.getColumn() == 0) ? ISourceLocation.NO_COLUMN : sl.getColumn()), sl.getContext(), sourcefilename);
		return sLoc;
	}

	private static String getSourceFileName(ResolvedType aspect) {
		String sourceFileName = null;
		if (aspect instanceof ReferenceType) {
			String s = ((ReferenceType) aspect).getDelegate().getSourcefilename();
			int i = s.lastIndexOf('/');
			if (i != -1) {
				sourceFileName = s.substring(i + 1);
			} else {
				sourceFileName = s;
			}
		}
		return sourceFileName;
	}

	/**
	 * Returns the File with pathname to the class file, for example either C:\temp
	 * \ajcSandbox\workspace\ajcTest16957.tmp\simple.jar!pkg\BinaryAspect.class if the class file is in a jar file, or
	 * C:\temp\ajcSandbox\workspace\ajcTest16957.tmp!pkg\BinaryAspect.class if the class file is in a directory
	 */
	private static File getBinaryFile(ResolvedType aspect) {
		String s = aspect.getBinaryPath();
		File f = aspect.getSourceLocation().getSourceFile();
		// Replace the source file suffix with .class
		int i = f.getPath().lastIndexOf('.');
		String path = null;
		if (i != -1) {
			path = f.getPath().substring(0, i) + ".class";
		} else {
			path = f.getPath() + ".class";
		}
		return new File(s + "!" + path);
	}

	/**
	 * Create a basic hierarchy to represent an aspect only available in binary (from the aspectpath).
	 */
	private static void createHierarchy(AsmManager model, ResolvedTypeMunger typeTransformer, ResolvedType aspect) {
		// assert aspect != null;

		// Check if already defined in the model
		// IProgramElement filenode =
		// model.getHierarchy().findElementForType(aspect.getPackageName(),
		// aspect.getClassName());
		// SourceLine(typeTransformer.getSourceLocation());
		IProgramElement filenode = model.getHierarchy().findElementForSourceLine(typeTransformer.getSourceLocation());
		if (filenode == null) {
			if (typeTransformer.getKind() == ResolvedTypeMunger.MethodDelegate2
					|| typeTransformer.getKind() == ResolvedTypeMunger.FieldHost) {
				// not yet faulting these in
				return;
			}
		}
		// the call to findElementForSourceLine(ISourceLocation) returns a file
		// node
		// if it can't find a node in the hierarchy for the given
		// sourcelocation.
		// Therefore, if this is returned, we know we can't find one and have to
		// // continue to fault in the model.
		// if (filenode != null) { //
		if (!filenode.getKind().equals(IProgramElement.Kind.FILE_JAVA)) {
			return;
		}

		// create the class file node
		ISourceLocation binLocation = getBinarySourceLocation(aspect, aspect.getSourceLocation());
		String f = getBinaryFile(aspect).getName();
		IProgramElement classFileNode = new ProgramElement(model, f, IProgramElement.Kind.FILE, binLocation, 0, null, null);

		// create package ipe if one exists....
		IProgramElement root = model.getHierarchy().getRoot();
		IProgramElement binaries = model.getHierarchy().findElementForLabel(root, IProgramElement.Kind.SOURCE_FOLDER, "binaries");
		if (binaries == null) {
			binaries = new ProgramElement(model, "binaries", IProgramElement.Kind.SOURCE_FOLDER, new ArrayList<IProgramElement>());
			root.addChild(binaries);
		}
		// if (aspect.getPackageName() != null) {
		String packagename = aspect.getPackageName() == null ? "" : aspect.getPackageName();
		// check that there doesn't already exist a node with this name
		IProgramElement pkgNode = model.getHierarchy().findElementForLabel(binaries, IProgramElement.Kind.PACKAGE, packagename);
		// note packages themselves have no source location
		if (pkgNode == null) {
			pkgNode = new ProgramElement(model, packagename, IProgramElement.Kind.PACKAGE, new ArrayList<IProgramElement>());
			binaries.addChild(pkgNode);
			pkgNode.addChild(classFileNode);
		} else {
			// need to add it first otherwise the handle for classFileNode
			// may not be generated correctly if it uses information from
			// it's parent node
			pkgNode.addChild(classFileNode);
			for (IProgramElement element: pkgNode.getChildren()) {
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
		// for (Iterator iter = root.getChildren().iterator(); iter.hasNext();)
		// {
		// IProgramElement element = (IProgramElement) iter.next();
		// if (!element.equals(classFileNode) &&
		// element.getHandleIdentifier().equals
		// (classFileNode.getHandleIdentifier())) {
		// // already added the sourcefile so have already
		// // added the structure for this aspect
		// root.removeChild(classFileNode);
		// return;
		// }
		// }
		// }

		// add and create empty import declaration ipe
		// no import container for binary type - 265693
		// classFileNode.addChild(new ProgramElement(model, "import declarations", IProgramElement.Kind.IMPORT_REFERENCE, null, 0,
		// null, null));

		// add and create aspect ipe
		IProgramElement aspectNode = new ProgramElement(model, aspect.getSimpleName(), IProgramElement.Kind.ASPECT,
				getBinarySourceLocation(aspect, aspect.getSourceLocation()), aspect.getModifiers(), null, null);
		classFileNode.addChild(aspectNode);

		addChildNodes(model, aspect, aspectNode, aspect.getDeclaredPointcuts());

		addChildNodes(model, aspect, aspectNode, aspect.getDeclaredAdvice());
		addChildNodes(model, aspect, aspectNode, aspect.getDeclares());
		addChildNodes(model, aspect, aspectNode, aspect.getTypeMungers());
	}

	/**
	 * Adds a declare annotation relationship, sometimes entities don't have source locs (methods/fields) so use other variants of
	 * this method if that is the case as they will look the entities up in the structure model.
	 */
	public static void addDeclareAnnotationRelationship(AsmManager model, ISourceLocation declareAnnotationLocation,
			ISourceLocation annotatedLocation, boolean isRemove) {
		if (model == null) {
			return;
		}

		IProgramElement sourceNode = model.getHierarchy().findElementForSourceLine(declareAnnotationLocation);
		String sourceHandle = sourceNode.getHandleIdentifier();
		if (sourceHandle == null) {
			return;
		}

		IProgramElement targetNode = model.getHierarchy().findElementForSourceLine(annotatedLocation);
		String targetHandle = targetNode.getHandleIdentifier();
		if (targetHandle == null) {
			return;
		}

		IRelationshipMap mapper = model.getRelationshipMap();
		// if (isRemove) {
		// IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, REMOVES_ANNOTATION, false,
		// true);
		// foreward.addTarget(targetHandle);
		//
		// IRelationship back = mapper
		// .get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATION_REMOVED_BY, false, true);
		// back.addTarget(sourceHandle);
		// if (sourceNode.getSourceLocation() != null) {
		// model.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		// }
		// } else {
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES, false, true);
		foreward.addTarget(targetHandle);

		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY, false, true);
		back.addTarget(sourceHandle);
		if (sourceNode.getSourceLocation() != null) {
			model.addAspectInEffectThisBuild(sourceNode.getSourceLocation().getSourceFile());
		}
		// }
	}

	/**
	 * Creates the hierarchy for binary aspects
	 */
	public static void createHierarchyForBinaryAspect(AsmManager asm, ShadowMunger munger) {
		if (!munger.isBinary()) {
			return;
		}

		IProgramElement sourceFileNode = asm.getHierarchy().findElementForSourceLine(munger.getSourceLocation());
		// the call to findElementForSourceLine(ISourceLocation) returns a file
		// node if it can't find a node in the hierarchy for the given sourcelocation.
		// Therefore, if this is returned, we know we can't find one and have to
		// continue to fault in the model.
		if (!sourceFileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)) {
			return;
		}

		ResolvedType aspect = munger.getDeclaringType();

		// create the class file node
		IProgramElement classFileNode = new ProgramElement(asm, sourceFileNode.getName(), IProgramElement.Kind.FILE,
				munger.getBinarySourceLocation(aspect.getSourceLocation()), 0, null, null);

		// create package ipe if one exists....
		IProgramElement root = asm.getHierarchy().getRoot();
		IProgramElement binaries = asm.getHierarchy().findElementForLabel(root, IProgramElement.Kind.SOURCE_FOLDER, "binaries");
		if (binaries == null) {
			binaries = new ProgramElement(asm, "binaries", IProgramElement.Kind.SOURCE_FOLDER, new ArrayList<IProgramElement>());
			root.addChild(binaries);
		}
		// if (aspect.getPackageName() != null) {
		String packagename = aspect.getPackageName() == null ? "" : aspect.getPackageName();
		// check that there doesn't already exist a node with this name
		IProgramElement pkgNode = asm.getHierarchy().findElementForLabel(binaries, IProgramElement.Kind.PACKAGE, packagename);
		// note packages themselves have no source location
		if (pkgNode == null) {
			pkgNode = new ProgramElement(asm, packagename, IProgramElement.Kind.PACKAGE, new ArrayList<IProgramElement>());
			binaries.addChild(pkgNode);
			pkgNode.addChild(classFileNode);
		} else {
			// need to add it first otherwise the handle for classFileNode
			// may not be generated correctly if it uses information from
			// it's parent node
			pkgNode.addChild(classFileNode);
			for (IProgramElement element: pkgNode.getChildren()) {
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
		// for (Iterator iter = root.getChildren().iterator(); iter.hasNext();)
		// {
		// IProgramElement element = (IProgramElement) iter.next();
		// if (!element.equals(classFileNode) &&
		// element.getHandleIdentifier().equals
		// (classFileNode.getHandleIdentifier())) {
		// // already added the sourcefile so have already
		// // added the structure for this aspect
		// root.removeChild(classFileNode);
		// return;
		// }
		// }
		// }

		// add and create empty import declaration ipe
		// classFileNode.addChild(new ProgramElement(asm, "import declarations", IProgramElement.Kind.IMPORT_REFERENCE, null, 0,
		// null,
		// null));

		// add and create aspect ipe
		IProgramElement aspectNode = new ProgramElement(asm, aspect.getSimpleName(), IProgramElement.Kind.ASPECT,
				munger.getBinarySourceLocation(aspect.getSourceLocation()), aspect.getModifiers(), null, null);
		classFileNode.addChild(aspectNode);

		String sourcefilename = getSourceFileName(aspect);
		addPointcuts(asm, sourcefilename, aspect, aspectNode, aspect.getDeclaredPointcuts());
		addChildNodes(asm, aspect, aspectNode, aspect.getDeclaredAdvice());
		addChildNodes(asm, aspect, aspectNode, aspect.getDeclares());
		addChildNodes(asm, aspect, aspectNode, aspect.getTypeMungers());

	}

	private static void addPointcuts(AsmManager model, String sourcefilename, ResolvedType aspect,
			IProgramElement containingAspect, ResolvedMember[] pointcuts) {
		for (int i = 0; i < pointcuts.length; i++) {
			ResolvedMember pointcut = pointcuts[i];
			if (pointcut instanceof ResolvedPointcutDefinition) {
				ResolvedPointcutDefinition rpcd = (ResolvedPointcutDefinition) pointcut;
				Pointcut p = rpcd.getPointcut();
				ISourceLocation sLoc = (p == null ? null : p.getSourceLocation());
				if (sLoc == null) {
					sLoc = rpcd.getSourceLocation();
				}
				ISourceLocation pointcutLocation = (sLoc == null ? null : createSourceLocation(sourcefilename, aspect, sLoc));
				ProgramElement pointcutElement = new ProgramElement(model, pointcut.getName(), IProgramElement.Kind.POINTCUT,
						pointcutLocation, pointcut.getModifiers(), NO_COMMENT, Collections.<IProgramElement>emptyList());
				containingAspect.addChild(pointcutElement);
			}
		}
	}

	private static final String NO_COMMENT = null;

	private static void addChildNodes(AsmManager asm, ResolvedType aspect, IProgramElement parent, ResolvedMember[] children) {
		for (int i = 0; i < children.length; i++) {
			ResolvedMember pcd = children[i];
			if (pcd instanceof ResolvedPointcutDefinition) {
				ResolvedPointcutDefinition rpcd = (ResolvedPointcutDefinition) pcd;
				Pointcut p = rpcd.getPointcut();
				ISourceLocation sLoc = (p == null ? null : p.getSourceLocation());
				if (sLoc == null) {
					sLoc = rpcd.getSourceLocation();
				}
				parent.addChild(new ProgramElement(asm, pcd.getName(), IProgramElement.Kind.POINTCUT, getBinarySourceLocation(
						aspect, sLoc), pcd.getModifiers(), null, Collections.<IProgramElement>emptyList()));
			}
		}
	}

	private static void addChildNodes(AsmManager asm, ResolvedType aspect, IProgramElement parent, Collection<?> children) {
		int deCtr = 1;
		int dwCtr = 1;
		for (Object element: children) {
			if (element instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning decl = (DeclareErrorOrWarning) element;
				int counter = 0;
				if (decl.isError()) {
					counter = deCtr++;
				} else {
					counter = dwCtr++;
				}
				parent.addChild(createDeclareErrorOrWarningChild(asm, aspect, decl, counter));
			} else if (element instanceof Advice) {
				Advice advice = (Advice) element;
				parent.addChild(createAdviceChild(asm, advice));
			} else if (element instanceof DeclareParents) {
				parent.addChild(createDeclareParentsChild(asm, (DeclareParents) element));
			} else if (element instanceof BcelTypeMunger) {
				IProgramElement newChild = createIntertypeDeclaredChild(asm, aspect, (BcelTypeMunger) element);
				// newChild==null means it is something that could not be handled by createIntertypeDeclaredChild()
				if (newChild != null) {
					parent.addChild(newChild);
				}
			}
		}
	}

	// private static IProgramElement
	// createDeclareErrorOrWarningChild(AsmManager asm, ShadowMunger munger,
	// DeclareErrorOrWarning decl, int count) {
	// IProgramElement deowNode = new ProgramElement(asm, decl.getName(),
	// decl.isError() ? IProgramElement.Kind.DECLARE_ERROR
	// : IProgramElement.Kind.DECLARE_WARNING,
	// munger.getBinarySourceLocation(decl.getSourceLocation()), decl
	// .getDeclaringType().getModifiers(), null, null);
	// deowNode.setDetails("\"" +
	// AsmRelationshipUtils.genDeclareMessage(decl.getMessage()) + "\"");
	// if (count != -1) {
	// deowNode.setBytecodeName(decl.getName() + "_" + count);
	// }
	// return deowNode;
	// }

	private static IProgramElement createDeclareErrorOrWarningChild(AsmManager model, ResolvedType aspect,
			DeclareErrorOrWarning decl, int count) {
		IProgramElement deowNode = new ProgramElement(model, decl.getName(), decl.isError() ? IProgramElement.Kind.DECLARE_ERROR
				: IProgramElement.Kind.DECLARE_WARNING, getBinarySourceLocation(aspect, decl.getSourceLocation()), decl
				.getDeclaringType().getModifiers(), null, null);
		deowNode.setDetails("\"" + AsmRelationshipUtils.genDeclareMessage(decl.getMessage()) + "\"");
		if (count != -1) {
			deowNode.setBytecodeName(decl.getName() + "_" + count);
		}
		return deowNode;
	}

	private static IProgramElement createAdviceChild(AsmManager model, Advice advice) {
		IProgramElement adviceNode = new ProgramElement(model, advice.getKind().getName(), IProgramElement.Kind.ADVICE,
				advice.getBinarySourceLocation(advice.getSourceLocation()), advice.getSignature().getModifiers(), null,
				Collections.<IProgramElement>emptyList());
		adviceNode.setDetails(AsmRelationshipUtils.genPointcutDetails(advice.getPointcut()));
		adviceNode.setBytecodeName(advice.getSignature().getName());
		return adviceNode;
	}

	/**
	 * Half baked implementation - will need completing if we go down this route rather than replacing it all for binary aspects.
	 * Doesn't attempt to get parameter names correct - they may have been lost during (de)serialization of the munger, but the
	 * member could still be located so they might be retrievable.
	 */
	private static IProgramElement createIntertypeDeclaredChild(AsmManager model, ResolvedType aspect, BcelTypeMunger itd) {
		ResolvedTypeMunger rtMunger = itd.getMunger();

		ResolvedMember sig = rtMunger.getSignature();
		Kind kind = rtMunger.getKind();
		if (kind == ResolvedTypeMunger.Field) { // ITD FIELD
			// String name = rtMunger.getSignature().toString();
			String name = sig.getDeclaringType().getClassName() + "." + sig.getName();
			if (name.indexOf("$") != -1) {
				name = name.substring(name.indexOf("$") + 1);
			}
			IProgramElement pe = new ProgramElement(model, name, IProgramElement.Kind.INTER_TYPE_FIELD, getBinarySourceLocation(
					aspect, itd.getSourceLocation()), rtMunger.getSignature().getModifiers(), null, Collections.<IProgramElement>emptyList());
			pe.setCorrespondingType(sig.getReturnType().getName());
			return pe;
		} else if (kind == ResolvedTypeMunger.Method) { // ITD
			// METHOD
			String name = sig.getDeclaringType().getClassName() + "." + sig.getName();
			if (name.indexOf("$") != -1) {
				name = name.substring(name.indexOf("$") + 1);
			}
			IProgramElement pe = new ProgramElement(model, name, IProgramElement.Kind.INTER_TYPE_METHOD, getBinarySourceLocation(
					aspect, itd.getSourceLocation()), rtMunger.getSignature().getModifiers(), null, Collections.<IProgramElement>emptyList());
			setParams(pe, sig);
			return pe;
		} else if (kind == ResolvedTypeMunger.Constructor) {
			String name = sig.getDeclaringType().getClassName() + "." + sig.getDeclaringType().getClassName();
			if (name.indexOf("$") != -1) {
				name = name.substring(name.indexOf("$") + 1);
			}
			IProgramElement pe = new ProgramElement(model, name, IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR,
					getBinarySourceLocation(aspect, itd.getSourceLocation()), rtMunger.getSignature().getModifiers(), null,
					Collections.<IProgramElement>emptyList());
			setParams(pe, sig);
			return pe;
			// } else if (kind == ResolvedTypeMunger.MethodDelegate2) {
			// String name = sig.getDeclaringType().getClassName() + "." + sig.getName();
			// if (name.indexOf("$") != -1) {
			// name = name.substring(name.indexOf("$") + 1);
			// }
			// IProgramElement pe = new ProgramElement(model, name, IProgramElement.Kind.INTER_TYPE_METHOD, getBinarySourceLocation(
			// aspect, itd.getSourceLocation()), rtMunger.getSignature().getModifiers(), null, Collections.EMPTY_LIST);
			// setParams(pe, sig);
			// return pe;
		}
		// other cases ignored for now
		return null;
	}

	private static void setParams(IProgramElement pe, ResolvedMember sig) {
		// do it for itds too
		UnresolvedType[] ts = sig.getParameterTypes();
		pe.setParameterNames(Collections.<String>emptyList());
		// TODO should be doing param names?
		if (ts == null) {
			pe.setParameterSignatures(Collections.<char[]>emptyList(), Collections.<String>emptyList());
		} else {
			List<char[]> paramSigs = new ArrayList<char[]>();
			for (int i = 0; i < ts.length; i++) {
				paramSigs.add(ts[i].getSignature().toCharArray());
			}
			pe.setParameterSignatures(paramSigs, Collections.<String>emptyList());
		}
		pe.setCorrespondingType(sig.getReturnType().getName());
	}

	private static IProgramElement createDeclareParentsChild(AsmManager model, DeclareParents decp) {
		IProgramElement decpElement = new ProgramElement(model, "declare parents", IProgramElement.Kind.DECLARE_PARENTS,
				getBinarySourceLocation(decp.getDeclaringType(), decp.getSourceLocation()), Modifier.PUBLIC, null,
				Collections.<IProgramElement>emptyList());
		setParentTypesOnDeclareParentsNode(decp, decpElement);
		return decpElement;
	}

	private static void setParentTypesOnDeclareParentsNode(DeclareParents decp, IProgramElement decpElement) {
		TypePatternList tpl = decp.getParents();
		List<String> parents = new ArrayList<String>();
		for (int i = 0; i < tpl.size(); i++) {
			parents.add(tpl.get(i).getExactType().getName().replaceAll("\\$", "."));
		}
		decpElement.setParentTypes(parents);
	}

	public static String getHandle(AsmManager asm, Advice advice) {
		if (null == advice.handle) {
			ISourceLocation sl = advice.getSourceLocation();
			if (sl != null) {
				IProgramElement ipe = asm.getHierarchy().findElementForSourceLine(sl);
				advice.handle = ipe.getHandleIdentifier();
			}
		}
		return advice.handle;
	}

	public static void addAdvisedRelationship(AsmManager model, Shadow matchedShadow, ShadowMunger munger) {
		if (model == null) {
			return;
		}

		if (munger instanceof Advice) {
			Advice advice = (Advice) munger;

			if (advice.getKind().isPerEntry() || advice.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}

			if (World.createInjarHierarchy) {
				createHierarchyForBinaryAspect(model, advice);
			}

			IRelationshipMap mapper = model.getRelationshipMap();
			IProgramElement targetNode = getNode(model, matchedShadow);
			if (targetNode == null) {
				return;
			}
			boolean runtimeTest = advice.hasDynamicTests();

			IProgramElement.ExtraInformation extra = new IProgramElement.ExtraInformation();

			String adviceHandle = getHandle(model, advice);
			if (adviceHandle == null) {
				return;
			}

			extra.setExtraAdviceInformation(advice.getKind().getName());
			IProgramElement adviceElement = model.getHierarchy().findElementForHandle(adviceHandle);
			if (adviceElement != null) {
				adviceElement.setExtraInfo(extra);
			}
			String targetHandle = targetNode.getHandleIdentifier();
			if (advice.getKind().equals(AdviceKind.Softener)) {
				IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.DECLARE_SOFT, SOFTENS, runtimeTest, true);
				if (foreward != null) {
					foreward.addTarget(targetHandle);
				}

				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, SOFTENED_BY, runtimeTest, true);
				if (back != null) {
					back.addTarget(adviceHandle);
				}
			} else {
				IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.ADVICE, ADVISES, runtimeTest, true);
				if (foreward != null) {
					foreward.addTarget(targetHandle);
				}

				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, ADVISED_BY, runtimeTest, true);
				if (back != null) {
					back.addTarget(adviceHandle);
				}
			}
			if (adviceElement.getSourceLocation() != null) {
				model.addAspectInEffectThisBuild(adviceElement.getSourceLocation().getSourceFile());
			}
		}
	}

	protected static IProgramElement getNode(AsmManager model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();
		// This variant will not be tricked by ITDs that would report they are
		// in the target type already.
		// This enables us to discover the ITD declaration (in the aspect) and
		// advise it appropriately.

		// Have to be smart here, for a code node within an ITD we want to
		// lookup the declaration of the
		// ITD in the aspect in order to add the code node at the right place -
		// and not lookup the
		// ITD as it applies in some target type. Due to the use of
		// effectiveSignature we will find
		// that shadow.getEnclosingCodeSignature() will return a member
		// representing the ITD as it will
		// appear in the target type. So here, we do an extra bit of analysis to
		// make sure we
		// do the right thing in the ITD case.
		IProgramElement enclosingNode = null;
		if (shadow instanceof BcelShadow) {
			Member actualEnclosingMember = ((BcelShadow) shadow).getRealEnclosingCodeSignature();

			if (actualEnclosingMember == null) {
				enclosingNode = lookupMember(model.getHierarchy(), shadow.getEnclosingType(), enclosingMember);
			} else {
				UnresolvedType type = enclosingMember.getDeclaringType();
				UnresolvedType actualType = actualEnclosingMember.getDeclaringType();

				// if these are not the same, it is an ITD and we need to use
				// the latter to lookup
				if (type.equals(actualType)) {
					enclosingNode = lookupMember(model.getHierarchy(), shadow.getEnclosingType(), enclosingMember);
				} else {
					enclosingNode = lookupMember(model.getHierarchy(), shadow.getEnclosingType(), actualEnclosingMember);
				}
			}
		} else {
			enclosingNode = lookupMember(model.getHierarchy(), shadow.getEnclosingType(), enclosingMember);
		}

		if (enclosingNode == null) {
			Lint.Kind err = shadow.getIWorld().getLint().shadowNotInStructure;
			if (err.isEnabled()) {
				err.signal(shadow.toString(), shadow.getSourceLocation());
			}
			return null;
		}

		Member shadowSig = shadow.getSignature();
		// pr235204
		if (shadow.getKind() == Shadow.MethodCall || shadow.getKind() == Shadow.ConstructorCall
				|| !shadowSig.equals(enclosingMember)) {
			IProgramElement bodyNode = findOrCreateCodeNode(model, enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}

	private static boolean sourceLinesMatch(ISourceLocation location1, ISourceLocation location2) {
		return (location1.getLine() == location2.getLine());
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
		for (IProgramElement child: enclosingNode.getChildren()) {
			if (child.getName().equals(shadow.toString())) {
				numberOfChildrenWithThisName++;
			}
		}
		peNode.setBytecodeName(shadowSig.getName() + "!" + String.valueOf(numberOfChildrenWithThisName + 1));
		peNode.setBytecodeSignature(shadowSig.getSignature());
		enclosingNode.addChild(peNode);
		return peNode;
	}

	private static IProgramElement lookupMember(IHierarchy model, UnresolvedType declaringType, Member member) {
		IProgramElement typeElement = model.findElementForType(declaringType.getPackageName(), declaringType.getClassName());
		if (typeElement == null) {
			return null;
		}
		for (Iterator it = typeElement.getChildren().iterator(); it.hasNext();) {
			IProgramElement element = (IProgramElement) it.next();
			if (member.getName().equals(element.getBytecodeName()) && member.getSignature().equals(element.getBytecodeSignature())) {
				return element;
			}
		}
		// if we can't find the member, we'll just put it in the class
		return typeElement;
	}

	/**
	 * Add a relationship for a matching declare annotation method or declare annotation constructor. Locating the method is a messy
	 * (for messy read 'fragile') bit of code that could break at any moment but it's working for my simple testcase.
	 */
	public static void addDeclareAnnotationMethodRelationship(ISourceLocation sourceLocation, String affectedTypeName,
			ResolvedMember affectedMethod, AsmManager model) {
		if (model == null) {
			return;
		}

		String pkg = null;
		String type = affectedTypeName;
		int packageSeparator = affectedTypeName.lastIndexOf(".");
		if (packageSeparator != -1) {
			pkg = affectedTypeName.substring(0, packageSeparator);
			type = affectedTypeName.substring(packageSeparator + 1);
		}

		IHierarchy hierarchy = model.getHierarchy();

		IProgramElement typeElem = hierarchy.findElementForType(pkg, type);
		if (typeElem == null) {
			return;
		}
		if (!typeElem.getKind().isType()) {
			throw new IllegalStateException("Did not find a type element, found a "+typeElem.getKind()+" element");
		}

		StringBuilder parmString = new StringBuilder("(");
		UnresolvedType[] args = affectedMethod.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			parmString.append(args[i].getName());
			if ((i + 1) < args.length) {
				parmString.append(",");
			}
		}
		parmString.append(")");
		IProgramElement methodElem = null;

		if (affectedMethod.getName().startsWith("<init>")) {
			// its a ctor
			methodElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.CONSTRUCTOR, type + parmString);
			if (methodElem == null && args.length == 0) {
				methodElem = typeElem; // assume default ctor
			}
		} else {
			// its a method
			methodElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.METHOD, affectedMethod.getName()
					+ parmString);
		}

		if (methodElem == null) {
			return;
		}

		try {
			String targetHandle = methodElem.getHandleIdentifier();
			if (targetHandle == null) {
				return;
			}

			IProgramElement sourceNode = hierarchy.findElementForSourceLine(sourceLocation);
			String sourceHandle = sourceNode.getHandleIdentifier();
			if (sourceHandle == null) {
				return;
			}

			IRelationshipMap mapper = model.getRelationshipMap();
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
	 * Add a relationship for a matching declare ATfield. Locating the field is trickier than it might seem since we have no line
	 * number info for it, we have to dig through the structure model under the fields' type in order to locate it.
	 */
	public static void addDeclareAnnotationFieldRelationship(AsmManager model, ISourceLocation declareLocation,
			String affectedTypeName, ResolvedMember affectedFieldName, boolean isRemove) {
		if (model == null) {
			return;
		}

		String pkg = null;
		String type = affectedTypeName;
		int packageSeparator = affectedTypeName.lastIndexOf(".");
		if (packageSeparator != -1) {
			pkg = affectedTypeName.substring(0, packageSeparator);
			type = affectedTypeName.substring(packageSeparator + 1);
		}
		IHierarchy hierarchy = model.getHierarchy();
		IProgramElement typeElem = hierarchy.findElementForType(pkg, type);
		if (typeElem == null) {
			return;
		}

		IProgramElement fieldElem = hierarchy.findElementForSignature(typeElem, IProgramElement.Kind.FIELD,
				affectedFieldName.getName());
		if (fieldElem == null) {
			return;
		}

		String targetHandle = fieldElem.getHandleIdentifier();
		if (targetHandle == null) {
			return;
		}

		IProgramElement sourceNode = hierarchy.findElementForSourceLine(declareLocation);
		String sourceHandle = sourceNode.getHandleIdentifier();
		if (sourceHandle == null) {
			return;
		}

		IRelationshipMap relmap = model.getRelationshipMap();
		// if (isRemove) {
		// IRelationship foreward = relmap.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, REMOVES_ANNOTATION, false,
		// true);
		// foreward.addTarget(targetHandle);
		// IRelationship back = relmap
		// .get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATION_REMOVED_BY, false, true);
		// back.addTarget(sourceHandle);
		// } else {
		IRelationship foreward = relmap.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES, false, true);
		foreward.addTarget(targetHandle);
		IRelationship back = relmap.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY, false, true);
		back.addTarget(sourceHandle);
		// }
	}

}
