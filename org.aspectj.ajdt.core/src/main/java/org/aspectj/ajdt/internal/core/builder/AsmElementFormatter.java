/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC                     initial implementation
 *     Alexandre Vasseur        support for @AJ style
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.asm.IProgramElement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.model.AsmRelationshipUtils;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.TypePatternList;

/**
 * @author Mik Kersten
 */
public class AsmElementFormatter {

  private final static String ASPECTJ_ANNOTATION_PACKAGE = "org.aspectj.lang.annotation";
  private final static char PACKAGE_INITIAL_CHAR = ASPECTJ_ANNOTATION_PACKAGE.charAt(0);

      public void genLabelAndKind(MethodDeclaration methodDeclaration, IProgramElement node) {

		if (methodDeclaration instanceof AdviceDeclaration) {
			AdviceDeclaration ad = (AdviceDeclaration) methodDeclaration;
			node.setKind(IProgramElement.Kind.ADVICE);

			if (ad.kind == AdviceKind.Around) {
				node.setCorrespondingType(ad.returnType.toString()); // returnTypeToString(0));
			}

			StringBuffer details = new StringBuffer();
			if (ad.pointcutDesignator != null) {
				details.append(AsmRelationshipUtils.genPointcutDetails(ad.pointcutDesignator.getPointcut()));
			} else {
				details.append(AsmRelationshipUtils.POINTCUT_ABSTRACT);
			}
			node.setName(ad.kind.toString());
			// if (details.length()!=0)
			node.setDetails(details.toString());
			setParameters(methodDeclaration, node);

		} else if (methodDeclaration instanceof PointcutDeclaration) {
			// PointcutDeclaration pd = (PointcutDeclaration) methodDeclaration;
			node.setKind(IProgramElement.Kind.POINTCUT);
			node.setName(translatePointcutName(new String(methodDeclaration.selector)));
			setParameters(methodDeclaration, node);

		} else if (methodDeclaration instanceof DeclareDeclaration) {
			DeclareDeclaration declare = (DeclareDeclaration) methodDeclaration;
			String name = AsmRelationshipUtils.DEC_LABEL + " ";
			if (declare.declareDecl instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning deow = (DeclareErrorOrWarning) declare.declareDecl;

				if (deow.isError()) {
					node.setKind(IProgramElement.Kind.DECLARE_ERROR);
					name += AsmRelationshipUtils.DECLARE_ERROR;
				} else {
					node.setKind(IProgramElement.Kind.DECLARE_WARNING);
					name += AsmRelationshipUtils.DECLARE_WARNING;
				}
				node.setName(name);
				node.setDetails("\"" + AsmRelationshipUtils.genDeclareMessage(deow.getMessage()) + "\"");

			} else if (declare.declareDecl instanceof DeclareParents) {

				node.setKind(IProgramElement.Kind.DECLARE_PARENTS);
				DeclareParents dp = (DeclareParents) declare.declareDecl;
				node.setName(name + AsmRelationshipUtils.DECLARE_PARENTS);

				String kindOfDP = null;
				StringBuffer details = new StringBuffer("");
				TypePattern[] newParents = dp.getParents().getTypePatterns();
				for (int i = 0; i < newParents.length; i++) {
					TypePattern tp = newParents[i];
					UnresolvedType tx = tp.getExactType();
					if (kindOfDP == null) {
						kindOfDP = "implements ";
						try {
							ResolvedType rtx = tx.resolve(((AjLookupEnvironment) declare.scope.environment()).factory.getWorld());
							if (!rtx.isInterface()) {
								kindOfDP = "extends ";
							}
						} catch (Throwable t) {
							// What can go wrong???? who knows!
						}

					}
					String typename = tp.toString();
					if (typename.lastIndexOf(".") != -1) {
						typename = typename.substring(typename.lastIndexOf(".") + 1);
					}
					details.append(typename);
					if ((i + 1) < newParents.length) {
						details.append(",");
					}
				}
				node.setDetails(kindOfDP + details.toString());

			} else if (declare.declareDecl instanceof DeclareSoft) {
				node.setKind(IProgramElement.Kind.DECLARE_SOFT);
				DeclareSoft ds = (DeclareSoft) declare.declareDecl;
				node.setName(name + AsmRelationshipUtils.DECLARE_SOFT);
				node.setDetails(genTypePatternLabel(ds.getException()));

			} else if (declare.declareDecl instanceof DeclarePrecedence) {
				node.setKind(IProgramElement.Kind.DECLARE_PRECEDENCE);
				DeclarePrecedence ds = (DeclarePrecedence) declare.declareDecl;
				node.setName(name + AsmRelationshipUtils.DECLARE_PRECEDENCE);
				node.setDetails(genPrecedenceListLabel(ds.getPatterns()));

			} else if (declare.declareDecl instanceof DeclareAnnotation) {
				DeclareAnnotation deca = (DeclareAnnotation) declare.declareDecl;
				String thekind = deca.getKind().toString();
				node.setName(name + "@" + thekind.substring(3));

				if (deca.getKind() == DeclareAnnotation.AT_CONSTRUCTOR) {
					node.setKind(IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR);
				} else if (deca.getKind() == DeclareAnnotation.AT_FIELD) {
					node.setKind(IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD);
				} else if (deca.getKind() == DeclareAnnotation.AT_METHOD) {
					node.setKind(IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD);
				} else if (deca.getKind() == DeclareAnnotation.AT_TYPE) {
					node.setKind(IProgramElement.Kind.DECLARE_ANNOTATION_AT_TYPE);
				}
				node.setDetails(genDecaLabel(deca));

			} else {
				node.setKind(IProgramElement.Kind.ERROR);
				node.setName(AsmRelationshipUtils.DECLARE_UNKNONWN);
			}

		} else if (methodDeclaration instanceof InterTypeDeclaration) {
			InterTypeDeclaration itd = (InterTypeDeclaration) methodDeclaration;
			String fqname = itd.getOnType().toString();
			if (fqname.contains(".")) {
				// TODO the string handling round here is embarrassing
				node.addFullyQualifiedName(fqname + "." + new String(itd.getDeclaredSelector()));
				fqname = fqname.substring(fqname.lastIndexOf(".") + 1);
			}
			String name = fqname + "." + new String(itd.getDeclaredSelector());
			if (methodDeclaration instanceof InterTypeFieldDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_FIELD);
				node.setName(name);
			} else if (methodDeclaration instanceof InterTypeMethodDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_METHOD);
				node.setName(name);
			} else if (methodDeclaration instanceof InterTypeConstructorDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR);

				// StringBuffer argumentsSignature = new StringBuffer("fubar");
				// argumentsSignature.append("(");
				// if (methodDeclaration.arguments!=null && methodDeclaration.arguments.length>1) {
				//
				// for (int i = 1;i<methodDeclaration.arguments.length;i++) {
				// argumentsSignature.append(methodDeclaration.arguments[i]);
				// if (i+1<methodDeclaration.arguments.length) argumentsSignature.append(",");
				// }
				// }
				// argumentsSignature.append(")");
				// InterTypeConstructorDeclaration itcd = (InterTypeConstructorDeclaration)methodDeclaration;
				node.setName(itd.getOnType().toString() + "." + itd.getOnType().toString().replace('.', '_'));
			} else {
				node.setKind(IProgramElement.Kind.ERROR);
				node.setName(name);
			}
			node.setCorrespondingType(new String(itd.returnType.resolvedType.readableName()));
			if (node.getKind() != IProgramElement.Kind.INTER_TYPE_FIELD) {
				setParameters(methodDeclaration, node);
			}
		} else {
			if (methodDeclaration.isConstructor()) {
				node.setKind(IProgramElement.Kind.CONSTRUCTOR);
			} else {
				node.setKind(IProgramElement.Kind.METHOD);

				// TODO AV - could speed up if we could dig only for @Aspect declaring types (or aspect if mixed style allowed)
				// ??? how to : node.getParent().getKind().equals(IProgramElement.Kind.ASPECT)) {
				if (true && methodDeclaration != null && methodDeclaration.annotations != null && methodDeclaration.scope != null) {
					for (int i = 0; i < methodDeclaration.annotations.length; i++) {
						// Note: AV: implicit single advice type support here (should be enforced somewhere as well (APT etc))
						Annotation annotation = methodDeclaration.annotations[i];
						String annotationSig = new String(annotation.type.getTypeBindingPublic(methodDeclaration.scope).signature());
						if (annotationSig.charAt(1) == PACKAGE_INITIAL_CHAR) {
							if ("Lorg/aspectj/lang/annotation/Pointcut;".equals(annotationSig)) {
								node.setKind(IProgramElement.Kind.POINTCUT);
								node.setAnnotationStyleDeclaration(true); // pointcuts don't seem to get handled quite right...
								break;
							} else if ("Lorg/aspectj/lang/annotation/Before;".equals(annotationSig)
									|| "Lorg/aspectj/lang/annotation/After;".equals(annotationSig)
									|| "Lorg/aspectj/lang/annotation/AfterReturning;".equals(annotationSig)
									|| "Lorg/aspectj/lang/annotation/AfterThrowing;".equals(annotationSig)
									|| "Lorg/aspectj/lang/annotation/Around;".equals(annotationSig)) {
								node.setKind(IProgramElement.Kind.ADVICE);
								node.setAnnotationStyleDeclaration(true);
								// TODO AV - all are considered anonymous - is that ok?
								node.setDetails(AsmRelationshipUtils.POINTCUT_ANONYMOUS);
								break;
							}
						}
					}
				}
			}
			node.setName(new String(methodDeclaration.selector));
			setParameters(methodDeclaration, node);
		}
	}

	private String genDecaLabel(DeclareAnnotation deca) {
		StringBuffer sb = new StringBuffer("");
		sb.append(deca.getPatternAsString());
		sb.append(" : ");
		sb.append(deca.getAnnotationString());
		return sb.toString();
	}

	private String genPrecedenceListLabel(TypePatternList list) {
		String tpList = "";
		for (int i = 0; i < list.size(); i++) {
			tpList += genTypePatternLabel(list.get(i));
			if (i < list.size() - 1) {
				tpList += ", ";
			}
		}
		return tpList;
	}

	// private String genArguments(MethodDeclaration md) {
	// String args = "";
	// Argument[] argArray = md.arguments;
	// if (argArray == null) return args;
	// for (int i = 0; i < argArray.length; i++) {
	// String argName = new String(argArray[i].name);
	// String argType = argArray[i].type.toString();
	// if (acceptArgument(argName, argType)) {
	// args += argType + ", ";
	// }
	// }
	// int lastSepIndex = args.lastIndexOf(',');
	// if (lastSepIndex != -1 && args.endsWith(", ")) args = args.substring(0, lastSepIndex);
	// return args;
	// }

	private String handleSigForReference(TypeReference ref, TypeBinding tb, MethodScope scope) {
		try {
			StringBuffer sb = new StringBuffer();
			createHandleSigForReference(ref, tb, scope, sb);
			return sb.toString();
		} catch (Throwable t) {
			System.err.println("Problem creating handle sig for this type reference " + ref);
			t.printStackTrace(System.err);
			return null;
		}
	}

	/**
	 * Aim of this method is create the signature for a parameter that can be used in a handle such that JDT can interpret the
	 * handle. Whether a type is qualified or unqualified in its source reference is actually reflected in the handle and this code
	 * allows for that.
	 */
	private void createHandleSigForReference(TypeReference ref, TypeBinding tb, MethodScope scope, StringBuffer handleSig) {
		if (ref instanceof Wildcard) {
			Wildcard w = (Wildcard) ref;
			if (w.bound == null) {
				handleSig.append('*');
			} else {
				handleSig.append('+');
				TypeBinding typeB = w.bound.resolvedType;
				if (typeB == null) {
					typeB = w.bound.resolveType(scope);
				}
				createHandleSigForReference(w.bound, typeB, scope, handleSig);
			}
		} else if (ref instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference) ref;
			for (int i = pstr.dimensions(); i > 0; i--) {
				handleSig.append("\\[");
			}
			handleSig.append('Q').append(pstr.token);
			TypeReference[] typeRefs = pstr.typeArguments;
			if (typeRefs != null && typeRefs.length > 0) {
				handleSig.append("\\<");
				for (TypeReference typeR : typeRefs) {
					TypeBinding typeB = typeR.resolvedType;
					if (typeB == null) {
						typeB = typeR.resolveType(scope);
					}
					createHandleSigForReference(typeR, typeB, scope, handleSig);
				}
				handleSig.append('>');
			}
			handleSig.append(';');
		} else if (ref instanceof ArrayTypeReference) {
			ArrayTypeReference atr = (ArrayTypeReference) ref;
			for (int i = 0; i < atr.dimensions; i++) {
				handleSig.append("\\[");
			}
			TypeBinding typeB = atr.resolvedType;
			if (typeB == null) {
				typeB = atr.resolveType(scope);
			}
			if (typeB.leafComponentType().isBaseType()) {
				handleSig.append(tb.leafComponentType().signature());
			} else {
				handleSig.append('Q').append(atr.token).append(';');
			}
		} else if (ref instanceof SingleTypeReference) {
			SingleTypeReference str = (SingleTypeReference) ref;
			if (tb.isBaseType()) {
				handleSig.append(tb.signature());
			} else {
				handleSig.append('Q').append(str.token).append(';');
			}
		} else if (ref instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pstr = (ParameterizedQualifiedTypeReference) ref;
			char[][] tokens = pstr.tokens;
			for (int i = pstr.dimensions(); i > 0; i--) {
				handleSig.append("\\[");
			}
			handleSig.append('Q');
			for (int i = 0; i < tokens.length; i++) {
				if (i > 0) {
					handleSig.append('.');
				}
				handleSig.append(tokens[i]);
				TypeReference[] typeRefs = pstr.typeArguments[i];
				if (typeRefs != null && typeRefs.length > 0) {
					handleSig.append("\\<");
					for (TypeReference typeR : typeRefs) {
						TypeBinding typeB = typeR.resolvedType;
						if (typeB == null) {
							typeB = typeR.resolveType(scope);
						}
						createHandleSigForReference(typeR, typeB, scope, handleSig);
					}
					handleSig.append('>');
				}
			}
			handleSig.append(';');
		} else if (ref instanceof ArrayQualifiedTypeReference) {
			ArrayQualifiedTypeReference atr = (ArrayQualifiedTypeReference) ref;
			for (int i = 0; i < atr.dimensions(); i++) {
				handleSig.append("\\[");
			}
			TypeBinding typeB = atr.resolvedType;
			if (typeB == null) {
				typeB = atr.resolveType(scope);
			}
			if (typeB.leafComponentType().isBaseType()) {
				handleSig.append(tb.leafComponentType().signature());
			} else {
				char[][] tokens = atr.tokens;
				handleSig.append('Q');
				for (int i = 0; i < tokens.length; i++) {
					if (i > 0) {
						handleSig.append('.');
					}
					handleSig.append(tokens[i]);
				}
				handleSig.append(';');
			}
		} else if (ref instanceof QualifiedTypeReference) {
			QualifiedTypeReference qtr = (QualifiedTypeReference) ref;
			char[][] tokens = qtr.tokens;
			handleSig.append('Q');
			for (int i = 0; i < tokens.length; i++) {
				if (i > 0) {
					handleSig.append('.');
				}
				handleSig.append(tokens[i]);
			}
			handleSig.append(';');
		} else {
			throw new RuntimeException("Cant handle " + ref.getClass());
		}
	}

	public void setParameters(AbstractMethodDeclaration md, IProgramElement pe) {
		Argument[] argArray = md.arguments;
		if (argArray == null) {
			pe.setParameterNames(Collections.<String>emptyList());
			pe.setParameterSignatures(Collections.<char[]>emptyList(), Collections.<String>emptyList());
		} else {
			List<String> names = new ArrayList<>();
			List<char[]> paramSigs = new ArrayList<>();
			List<String> paramSourceRefs = new ArrayList<>();
			boolean problemWithSourceRefs = false;
			for (Argument argument : argArray) {
				String argName = new String(argument.name);
				// String argType = "<UnknownType>"; // pr135052
				if (acceptArgument(argName, argument.type.toString())) {
					TypeReference typeR = argument.type;
					if (typeR != null && md.scope != null) {
						TypeBinding typeB = typeR.resolvedType;
						if (typeB == null) {
							typeB = typeR.resolveType(md.scope);
						}
						// This code will conjure up a 'P' style signature:
						// EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(md.scope);
						// UnresolvedType ut = factory.fromBinding(typeB);
						// paramSigs.add(ut.getSignature().toCharArray());
						paramSigs.add(typeB.genericTypeSignature());
						String hsig = handleSigForReference(typeR, typeB, md.scope);
						if (hsig == null) {
							problemWithSourceRefs = true;
						} else {
							paramSourceRefs.add(hsig);
						}
					}
					names.add(argName);
				}
			}
			pe.setParameterNames(names);
			if (!paramSigs.isEmpty()) {
				pe.setParameterSignatures(paramSigs, (problemWithSourceRefs ? null : paramSourceRefs));
			}
		}
	}

	// TODO: fix this way of determing ajc-added arguments, make subtype of Argument with extra info
	private boolean acceptArgument(String name, String type) {
		if (name.charAt(0) != 'a' && type.charAt(0) != PACKAGE_INITIAL_CHAR) {
			return true;
		}
		return !name.startsWith("ajc$this_") && !type.equals("org.aspectj.lang.JoinPoint.StaticPart")
				&& !type.equals("org.aspectj.lang.JoinPoint") && !type.equals("org.aspectj.runtime.internal.AroundClosure");
	}

	public String genTypePatternLabel(TypePattern tp) {
		final String TYPE_PATTERN_LITERAL = "<type pattern>";
		String label;
		UnresolvedType typeX = tp.getExactType();

		if (!ResolvedType.isMissing(typeX)) {
			label = typeX.getName();
			if (tp.isIncludeSubtypes()) {
				label += "+";
			}
		} else {
			label = TYPE_PATTERN_LITERAL;
		}
		return label;

	}

	// // TODO:
	// private String translateAdviceName(String label) {
	// if (label.indexOf("before") != -1) return "before";
	// if (label.indexOf("returning") != -1) return "after returning";
	// if (label.indexOf("after") != -1) return "after";
	// if (label.indexOf("around") != -1) return "around";
	// else return "<advice>";
	// }

	// // !!! move or replace
	// private String translateDeclareName(String name) {
	// int colonIndex = name.indexOf(":");
	// if (colonIndex != -1) {
	// return name.substring(0, colonIndex);
	// } else {
	// return name;
	// }
	// }

	// !!! move or replace
	// private String translateInterTypeDecName(String name) {
	// int index = name.lastIndexOf('$');
	// if (index != -1) {
	// return name.substring(index+1);
	// } else {
	// return name;
	// }
	// }

	// !!! move or replace
	private String translatePointcutName(String name) {
		int index = name.indexOf("$$") + 2;
		int endIndex = name.lastIndexOf('$');
		if (index != -1 && endIndex != -1) {
			return name.substring(index, endIndex);
		} else {
			return name;
		}
	}

}
