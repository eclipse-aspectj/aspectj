/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.util.*;
import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class StructureNodeFactory {

    private static Hashtable programElementNodes = new Hashtable();
    
    private static final ProgramElementNode UNRESOLVED_LINK_NODE = new ProgramElementNode("<error: unresolved link>", ProgramElementNode.Kind.ERROR, null, null, "", "", "", null, null, null, false);

    public static void clear() {
        programElementNodes.clear();
    }

    public static ProgramElementNode makeNode(List relations, List children) {
        return makeNode(relations, children, false);
    }

    public static LinkNode makeLink(boolean terminal) {
        ProgramElementNode peNode = null;
        if (terminal) {
            peNode = makeNode(null, null, false);
        } else {
        	peNode = makeNode(null, null, true);
        }
        
        if (peNode == null) {
    		return new LinkNode(UNRESOLVED_LINK_NODE);
    	} else {
    		return new LinkNode(peNode);
    	}
    }

    private static ProgramElementNode makeNode(List relations, List children, boolean resolve) {
//        if (resolve) {
//            if (astObject instanceof InitializerDec) {
//                InitializerDec initDec = (InitializerDec)astObject;
//                return (ProgramElementNode)programElementNodes.get(initDec.getDeclaringType().getTypeDec());
//            } else if (astObject instanceof Decs) {
//                Decs decs = (Decs)astObject;
//                return (ProgramElementNode)programElementNodes.get(decs.getDeclaringType().getTypeDec());
//            } else {
//                ProgramElementNode peNode = (ProgramElementNode)programElementNodes.get(astObject);
//                if (peNode == null) {
//                    return makeNode(astObject, null, null, false);
//                } else {
//                    return peNode;
//                }
//            }
//        } else {
//            String declaringType = "";
//            if (astObject.getDeclaringType() != null) {
//                declaringType = astObject.getDeclaringType().toShortString();
//            }
//            
//            org.aspectj.asm.SourceLocation sourceLocation = new org.aspectj.asm.SourceLocation(
//            	astObject.getSourceLocation().getSourceFileName(),
//            	astObject.getSourceLocation().getBeginLine(),
//            	astObject.getSourceLocation().getEndLine(),
//            	astObject.getSourceLocation().getBeginColumn());
//            
//            ProgramElementNode newNode = new ProgramElementNode(
//                genSignature(astObject).trim(),
//                genKind(astObject),
//                genModifiers(astObject),
//                genAccessibility(astObject),
//                declaringType,
//                genPackageName(astObject),
//                genFormalComment(astObject),
//                sourceLocation,
//                relations,
//                children,
//                isMemberKind(astObject),
//                astObject);
//            programElementNodes.put(astObject, newNode);
//            newNode.setRunnable(genIsRunnable(newNode));
//            setSpecifiers(astObject, newNode);
//            
//            return newNode;
//        }
		return null;
    }

//	private static void setSpecifiers(ASTObject astObject, ProgramElementNode node) {
//		if (astObject instanceof MethodDec) {
//			Method method = ((MethodDec)astObject).getMethod();
//			for (Iterator it = method.getDeclaringType().getDirectSuperTypes().iterator(); it.hasNext(); ) {
//				NameType type = (NameType)it.next();
//				SemanticObject so = type.findMatchingSemanticObject(method);
//			
//				if (so != null && so instanceof Method) {
//					
//					Method superMethod = (Method)so;
//					if (so.isAbstract()) {
//						node.setImplementor(true);
//					} else {
//						node.setOverrider(true);	
//					}
//				}
//			}
//		}
//	}
//
//	private static boolean genIsRunnable(ProgramElementNode node) {
//		if (node.getModifiers().contains(ProgramElementNode.Modifiers.STATIC)
//			&& node.getAccessibility().equals(ProgramElementNode.Accessibility.PUBLIC)
//			&& node.getSignature().equals("main(String[])")) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//    private static boolean genIsStmntKind(ASTObject astObject) {
//        return astObject instanceof CatchClause 
//        	|| astObject instanceof SOLink
//        	|| astObject instanceof BasicAssignExpr;
//    }
//
//    private static List genModifiers(ASTObject astObject) {
//        List modifiers = new ArrayList();
//        if (astObject instanceof Dec) {
//            Dec dec = (Dec)astObject;
//            if (dec.getModifiers().isStrict()) modifiers.add(ProgramElementNode.Modifiers.STRICTFP);
//            if (dec.getModifiers().isAbstract()) modifiers.add(ProgramElementNode.Modifiers.ABSTRACT);
//            if (dec.getModifiers().isSynchronized()) modifiers.add(ProgramElementNode.Modifiers.SYNCHRONIZED);
//            if (dec.getModifiers().isNative()) modifiers.add(ProgramElementNode.Modifiers.NATIVE);
//            if (dec.getModifiers().isFinal()) modifiers.add(ProgramElementNode.Modifiers.FINAL);
//            if (dec.getModifiers().isTransient()) modifiers.add(ProgramElementNode.Modifiers.TRANSIENT);
//            if (dec.getModifiers().isStatic()) modifiers.add(ProgramElementNode.Modifiers.STATIC);
//            if (dec.getModifiers().isVolatile()) modifiers.add(ProgramElementNode.Modifiers.VOLATILE);
//        }
//        return modifiers;
//    }
//
//    private static ProgramElementNode.Accessibility genAccessibility(ASTObject astObject) {
//        //List modifiers = new ArrayList();
//        if (astObject instanceof Dec) {
//            Dec dec = (Dec)astObject;
//            if (dec.getModifiers().isPublic()) return ProgramElementNode.Accessibility.PUBLIC;
//            if (dec.getModifiers().isProtected()) return ProgramElementNode.Accessibility.PROTECTED;
//            if (dec.getModifiers().isPrivileged()) return ProgramElementNode.Accessibility.PRIVILEGED;
//            if (dec.getModifiers().isPackagePrivate()) return ProgramElementNode.Accessibility.PACKAGE;
//            if (dec.getModifiers().isPrivate()) return ProgramElementNode.Accessibility.PRIVATE;
//        }
//        return ProgramElementNode.Accessibility.PUBLIC;
//    }
//
//    /**
//     * @todo	special cases should be fixes to AST nodes, this should have no instanceof tests.
//     */
//    private static ProgramElementNode.Kind genKind(ASTObject astObject) {
//        if (astObject instanceof CompilationUnit) {
//            return ProgramElementNode.Kind.FILE_JAVA;
//        } else if (genIsStmntKind(astObject)) {
//            return ProgramElementNode.Kind.CODE;
//        } else if (astObject instanceof Dec) {
//            String kindString = ((Dec)astObject).getKind();
//            return ProgramElementNode.Kind.getKindForString(kindString);
//        } else {
//            return ProgramElementNode.Kind.ERROR;
//        }
//    }
//
//    private static boolean isMemberKind(ASTObject astObject) {
//        if (astObject instanceof Dec) {
//            Dec dec = (Dec)astObject;
//            return dec.getDeclaringType() != null && !dec.getDeclaringType().equals(dec.getName());
//        } else {
//            return false;
//        }
//    }
//
//    private static String genPackageName(ASTObject astObject) {
//        if (astObject instanceof TypeDec) {
//            return ((TypeDec)astObject).getPackageName();
//        } else if (astObject instanceof CompilationUnit) {
//            return ((CompilationUnit)astObject).getPackageName();
//        } else if (astObject.getDeclaringType() != null) {
//            return astObject.getDeclaringType().getPackageName();
//        } else {
//            return "";
//        }
//    }
//
//    private static String genDeclaringType(ASTObject astObject) {
//        if (astObject != null && astObject.getDeclaringType() != null) {
//            return astObject.getDeclaringType().toShortString();
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Tries to return the ajdoc generated comment, otherwise returns the raw comment.
//     */
//    private static String genFormalComment(ASTObject astObject) {
//        try {
//            return (String)astObject.getComment().getClass().getMethod("commentText", new Class[]{}).invoke(astObject.getComment(), new Object[]{});
//        } catch (Throwable t) {
//            if (astObject != null) {
//                return astObject.getFormalComment();
//            } else {
//                return "";
//            }
//        }
//    }
//
//    /**
//     * Specialized signature generation for nodes in the structure model.
//     *
//     * @todo    the compiler should generate these names, doing it this way is atrocious
//     */
//    private static String genSignature(ASTObject astObject) {
//        String name = "";
//        if (astObject instanceof CompilationUnit) {
//            return astObject.getSourceFile().getName();
//        } else if (astObject instanceof MethodDec) {
//            Method method = ((MethodDec)astObject).getMethod();
//            return method.getName() + method.getFormals().toShortString();
//        } else if (astObject instanceof TypeDec) {
//            return ((TypeDec)astObject).getSourceExtendedId();
//        } else if (astObject instanceof FieldDec) {
//            return ((FieldDec)astObject).getName();
//        } else if (astObject instanceof ConstructorDec) {
//            ConstructorDec constructorDec = (ConstructorDec)astObject;
//            return constructorDec.getDeclaringType().getSourceExtendedId() + constructorDec.getFormals().toShortString();
//      }  else if (astObject instanceof IntroducedDec) {
//            IntroducedDec introDec = (IntroducedDec)astObject;
//            return introDec.getTargets().toShortString() + '.' + genSignature(introDec.getDec());
////            introDec.toShortString();
//        } else if (astObject instanceof PointcutDec) {
//            PointcutDec pointcutDec = (PointcutDec)astObject;
//            return pointcutDec.getName() + pointcutDec.getFormals().toShortString();
////        } else if (astObject instanceof CallExpr) {
////            CallExpr call = (CallExpr)astObject;
////            name = call.get;
//        } else if (astObject instanceof ShowErrorDec) {
//            ShowErrorDec errorDec = (ShowErrorDec)astObject;
//            return errorDec.toShortString();
//        } else if (astObject instanceof SoftThrowableDec) {
//            SoftThrowableDec softThrowableDec = (SoftThrowableDec)astObject;
//            return softThrowableDec.toShortString();
//        } else if (astObject instanceof IntroducedSuperDec) {
//            IntroducedSuperDec introducedSuperDec = (IntroducedSuperDec)astObject;
//            return introducedSuperDec.toShortString();
//        } else if (astObject instanceof AdviceDec) {
//            AdviceDec adviceDec = (AdviceDec)astObject;
//            return adviceDec.toShortString();
//        } else if (astObject instanceof SOLink) {
//            SOLink soLink = (SOLink)astObject;
//            return genSignature(soLink.getTarget().getCorrespondingDec());
//        } else if (astObject instanceof CatchClause) {
//            CatchClause catchClause = (CatchClause)astObject;
//            return catchClause.getFormal().getType().getSourceExtendedId();
//        } else if (astObject instanceof BasicAssignExpr) {
//        	return astObject.unparse();
////    	} else if (genIsStmntKind(astObject)) {
////            name = astObject.unparse();
////            name = name.replace('/', ' ');
////            name = name.replace('*', ' ');
////            name = name.replace('\n', ' ');
////            name.trim();
////            java.util.StringTokenizer st = new java.util.StringTokenizer(name, " ");
////            String s = "";
////            while (st.hasMoreElements()) {
////                s += ((String)st.nextElement()).trim() + " ";
////            }
////            name = s;
////            int endIndex = name.indexOf(')');
////            if (endIndex != -1) {
////                name = name.substring(0, endIndex+1);
////            }
////            if (name.startsWith("this.")) {
////                name = name.substring(5);
////            }
//        } else {
//            return "? " + astObject.toShortString();
//        }
//    }
}
