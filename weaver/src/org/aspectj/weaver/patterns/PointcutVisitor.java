/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.Member;

import org.aspectj.weaver.patterns.TypePattern.*;
import org.aspectj.weaver.patterns.AnnotationTypePattern.*;

/**
 * A Pointcut or TypePattern visitor
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface PointcutVisitor {

    Object visit(AnyTypePattern node, Object data);

    Object visit(NoTypePattern node, Object data);

    Object visit(EllipsisTypePattern node, Object data);

    Object visit(AnyWithAnnotationTypePattern node, Object data);

    Object visit(AnyAnnotationTypePattern node, Object data);

    Object visit(EllipsisAnnotationTypePattern node, Object data);

    Object visit(AndAnnotationTypePattern node, Object data);

    Object visit(AndPointcut node, Object data);

    Object visit(AndTypePattern node, Object data);

    Object visit(AnnotationPatternList node, Object data);

    Object visit(AnnotationPointcut node, Object data);

    Object visit(ArgsAnnotationPointcut node, Object data);

    Object visit(ArgsPointcut node, Object data);

    Object visit(BindingAnnotationTypePattern node, Object data);

    Object visit(BindingTypePattern node, Object data);

    Object visit(CflowPointcut node, Object data);

    Object visit(ConcreteCflowPointcut node, Object data);

    Object visit(DeclareAnnotation node, Object data);

    Object visit(DeclareErrorOrWarning node, Object data);

    Object visit(DeclareParents node, Object data);

    Object visit(DeclarePrecedence node, Object data);

    Object visit(DeclareSoft node, Object data);

    Object visit(ExactAnnotationTypePattern node, Object data);

    Object visit(ExactTypePattern node, Object data);

    Object visit(HandlerPointcut node, Object data);

    Object visit(IfPointcut node, Object data);

    Object visit(KindedPointcut node, Object data);

    Object visit(ModifiersPattern node, Object data);

    Object visit(NamePattern node, Object data);

    Object visit(NotAnnotationTypePattern node, Object data);

    Object visit(NotPointcut node, Object data);

    Object visit(NotTypePattern node, Object data);

    Object visit(OrAnnotationTypePattern node, Object data);

    Object visit(OrPointcut node, Object data);

    Object visit(OrTypePattern node, Object data);

    Object visit(PerCflow node, Object data);

    Object visit(PerFromSuper node, Object data);

    Object visit(PerObject node, Object data);

    Object visit(PerSingleton node, Object data);

    Object visit(PerTypeWithin node, Object data);

    Object visit(PatternNode node, Object data);

    Object visit(ReferencePointcut node, Object data);

    Object visit(SignaturePattern node, Object data);

    Object visit(ThisOrTargetAnnotationPointcut node, Object data);

    Object visit(ThisOrTargetPointcut node, Object data);

    Object visit(ThrowsPattern node, Object data);

    Object visit(TypePatternList node, Object data);

    Object visit(WildAnnotationTypePattern node, Object data);

    Object visit(WildTypePattern node, Object data);

    Object visit(WithinAnnotationPointcut node, Object data);

    Object visit(WithinCodeAnnotationPointcut node, Object data);

    Object visit(WithinPointcut node, Object data);

    Object visit(WithincodePointcut node, Object data);

    Object visit(Pointcut.MatchesNothingPointcut node, Object data);

    /**
     * A sample toString like visitor that helps understanding the AST tree structure organization
     * TODO: not yet complete
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    static class DumpPointcutVisitor implements PointcutVisitor {

        private StringBuffer sb = new StringBuffer();
        public String get() {
            return sb.toString();
        }

        private void p(Object o) {
            sb.append(o.toString());
        }
		
		private void p(char c) {
			sb.append(c);
		}

        /**
         * This method helps maintaining the API and raises warning when PatternNode subclasses do not
         * implement the visitor pattern
         *
         * @param node
         * @param data
         * @return
         */
        public Object visit(PatternNode node, Object data) {
            System.err.println("Should implement: "  + node.getClass());
            return null;
        }

        public Object visit(AnyTypePattern node, Object data) {
            p('*');
            return null;
        }

        public Object visit(NoTypePattern node, Object data) {
            p(node.toString());//TODO no idea when this one is used
            return null;
        }

        public Object visit(EllipsisTypePattern node, Object data) {
            p(node.toString());
            return null;
        }

        public Object visit(AnyWithAnnotationTypePattern node, Object data) {
            node.annotationPattern.accept(this, data);
            p(" *");
            return null;
        }

        public Object visit(AnyAnnotationTypePattern node, Object data) {
            //@ANY : ignore
            p('*');
            return null;
        }

        public Object visit(EllipsisAnnotationTypePattern node, Object data) {
            p("..");
            return null;
        }

        public Object visit(AndAnnotationTypePattern node, Object data) {
            //p('(');
            node.getLeft().accept(this, data);
            //p(" && ");
            p(' ');
            node.getRight().accept(this, data);
            //p(')');
            return null;
        }

        public Object visit(AndPointcut node, Object data) {
            p('(');
            node.getLeft().accept(this, data);
            p(" && ");
            node.getRight().accept(this, data);
            p(')');
            return null;
        }

        public Object visit(AndTypePattern node, Object data) {
            p('(');
            node.left.accept(this, data);
            p(" && ");
            node.right.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(AnnotationPatternList node, Object data) {
            AnnotationTypePattern[] annotations = node.getAnnotationPatterns();
            for (int i = 0; i < annotations.length; i++) {
                if (i>0) p(", ");//FIXME AV should that be here?
                annotations[i].accept(this, data);
            }
            return null;
        }

        public Object visit(AnnotationPointcut node, Object data) {
            p("@annotation(");
            node.annotationTypePattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(ArgsAnnotationPointcut node, Object data) {
            p("@args(");
            node.arguments.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(ArgsPointcut node, Object data) {
            p("args(");
            node.arguments.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(BindingAnnotationTypePattern node, Object data) {
            p(node);
            return null;
        }

        public Object visit(BindingTypePattern node, Object data) {
            p(node);
            return null;
        }

        public Object visit(CflowPointcut node, Object data) {
            p(node.isBelow?"cflowbelow(":"cflow(");
            node.getEntry().accept(this, data);
            p(')');
            return null;
        }

        public Object visit(ExactAnnotationTypePattern node, Object data) {
            //p('@'); // since @annotation(@someAnno) cannot be parsed anymore
            p(node.annotationType.getName());
            return null;
        }

        public Object visit(ExactTypePattern node, Object data) {
            if (node.annotationPattern != AnnotationTypePattern.ANY) {
                p('(');
                node.annotationPattern.accept(this, data);
                p(' ');
            }

            String typeString = node.type.toString();
            if (node.isVarArgs) typeString = typeString.substring(0, typeString.lastIndexOf('['));//TODO AV - ugly
            p(typeString);
            if (node.includeSubtypes) p('+');
            if (node.isVarArgs) p("...");
            if (node.annotationPattern != AnnotationTypePattern.ANY) {
                p(')');
            }
            return null;
        }

        public Object visit(KindedPointcut node, Object data) {
            p(node.getKind().getSimpleName());
            p('(');
            node.signature.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(ModifiersPattern node, Object data) {
            p(node.toString());//note: node takes care of forbidden mods
            return null;
        }

        public Object visit(NamePattern node, Object data) {
            p(node.toString());
            return null;
        }

        public Object visit(NotAnnotationTypePattern node, Object data) {
            p("!");
            node.negatedPattern.accept(this, data);
            return null;
        }

        public Object visit(NotPointcut node, Object data) {
            p("!(");
            node.getNegatedPointcut().accept(this, data);
            p(')');
            return null;
        }

        public Object visit(NotTypePattern node, Object data) {
            p("!(");
            node.pattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(OrAnnotationTypePattern node, Object data) {
            p('(');
            node.getLeft().accept(this, data);
            p(" || ");
            node.getRight().accept(this, data);
            p(')');
            return null;
        }

        public Object visit(OrPointcut node, Object data) {
            p('(');
            node.getLeft().accept(this, data);
            p(" || ");
            node.getRight().accept(this, data);
            p(')');
            return null;
        }

        public Object visit(OrTypePattern node, Object data) {
            p('(');
            node.left.accept(this, data);
            p(" || ");
            node.right.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(ReferencePointcut node, Object data) {
            p(node.toString());
            return null;
        }

        public Object visit(SignaturePattern node, Object data) {
            if (node.getAnnotationPattern() != AnnotationTypePattern.ANY) {
                node.getAnnotationPattern().accept(this, data);
                p(' ');
            }

            if (node.getModifiers() != ModifiersPattern.ANY) {
                node.getModifiers().accept(this, data);
                p(' ');
            }

            if (node.getKind() == Member.STATIC_INITIALIZATION) {
                node.getDeclaringType().accept(this, data);
                //p(".<clinit>()");
            } else if (node.getKind() == Member.HANDLER) {
                p("handler(");
                node.getParameterTypes().get(0).accept(this, data);//Note: we know we have 1 child
                p(')');
            } else {
                if (!(node.getKind() == Member.CONSTRUCTOR)) {
                    node.getReturnType().accept(this, data);
                    p(' ');
                }
                if (node.getDeclaringType() != TypePattern.ANY) {
                    node.getDeclaringType().accept(this, data);
                    p('.');
                }
                if (node.getKind() == Member.CONSTRUCTOR) {
                    p("new");
                } else {
                    node.getName().accept(this, data);
                }
                if (node.getKind() == Member.METHOD || node.getKind() == Member.CONSTRUCTOR) {
                    p('(');
                    node.getParameterTypes().accept(this, data);
                    p(')');
                }
                if (node.getThrowsPattern() != null) {
                    p(' ');
                    node.getThrowsPattern().accept(this, data);
                }
            }
            return null;
        }

        public Object visit(ThisOrTargetAnnotationPointcut node, Object data) {
            p(node.isThis() ? "@this(" : "@target(");
            node.annotationTypePattern.accept(this, data);
            //buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
            p(')');
            return null;
        }

        public Object visit(ThisOrTargetPointcut node, Object data) {
            p(node.isThis() ? "this(" : "target(");
            node.type.accept(this, data);
            p(')');
            return null;
        }

        private boolean inThrowsForbidden = false;

        public Object visit(ThrowsPattern node, Object data) {
            if (node == ThrowsPattern.ANY) return null;

            p("throws ");
            node.required.accept(this, data);
            if (node.forbidden.size() > 0) {
                // a hack since throws !(A, B) cannot be parsed
                inThrowsForbidden = true;
                node.forbidden.accept(this, data);
                inThrowsForbidden = false;
            }
            return null;
        }

        public Object visit(TypePatternList node, Object data) {
            if (node.getTypePatterns().length == 0) return null;

            TypePattern[] typePatterns = node.getTypePatterns();
            for (int i = 0; i < typePatterns.length; i++) {
                TypePattern typePattern = typePatterns[i];
                if (i > 0) p(", ");
                if (inThrowsForbidden) p('!');
                typePattern.accept(this, data);
            }
            return null;
        }

        public Object visit(WildAnnotationTypePattern node, Object data) {
            p("@(");
            node.typePattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(WildTypePattern node, Object data) {
            if (node.annotationPattern != AnnotationTypePattern.ANY) {
                p('(');
                node.annotationPattern.accept(this, data);
                p(' ');
            }
            for (int i=0, len=node.namePatterns.length; i < len; i++) {
                NamePattern name = node.namePatterns[i];
                if (name == null) {
                    p('.');//FIXME mh, error prone, can't we have a nullNamePattern ?
                } else {
                    if (i > 0) p('.');
                    name.accept(this, data);
                }
            }
            if (node.includeSubtypes) p('+');
            if (node.isVarArgs) p("...");//FIXME ? in type pattern
            if (node.annotationPattern != AnnotationTypePattern.ANY) {
                p(')');
            }
            return null;
        }

        public Object visit(WithinAnnotationPointcut node, Object data) {
            p("@within(");
            node.annotationTypePattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(WithinCodeAnnotationPointcut node, Object data) {
            p("@withincode(");
            node.annotationTypePattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(WithinPointcut node, Object data) {
            p("within(");
            node.typePattern.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(WithincodePointcut node, Object data) {
            p("withincode(");
            node.signature.accept(this, data);
            p(')');
            return null;
        }

        public Object visit(Pointcut.MatchesNothingPointcut node, Object data) {
            p("");//TODO shouldn't that be a "false" ?
            return null;
        }


        //-------------- perX

        public Object visit(PerCflow node, Object data) {
            p(node);
            return null;
        }

        public Object visit(PerFromSuper node, Object data) {
            p(node);
            return null;
        }

        public Object visit(PerObject node, Object data) {
            p(node);
            return null;
        }

        public Object visit(PerSingleton node, Object data) {
            p(node);
            return null;
        }

        public Object visit(PerTypeWithin node, Object data) {
            p(node);
            return null;
        }

        // ------------- declare X

        public Object visit(DeclareAnnotation node, Object data) {
            p(node);
            return null;
        }

        public Object visit(DeclareErrorOrWarning node, Object data) {
            p(node);
            return null;
        }

        public Object visit(DeclareParents node, Object data) {
            p(node);
            return null;
        }

        public Object visit(DeclarePrecedence node, Object data) {
            p(node);
            return null;
        }

        public Object visit(DeclareSoft node, Object data) {
            p(node);
            return null;
        }

        // ----------- misc

        public Object visit(ConcreteCflowPointcut node, Object data) {
            p(node);
            return null;
        }

        public Object visit(HandlerPointcut node, Object data) {
            p(node);
            return null;
        }

        public Object visit(IfPointcut node, Object data) {
            p(node);
            return null;
        }

        public static void check(String s) {
            check(Pointcut.fromString(s), false);
        }

        public static void check(PatternNode pc, boolean isTypePattern) {
            DumpPointcutVisitor v1 = new DumpPointcutVisitor();
            pc.accept(v1, null);

            DumpPointcutVisitor v2 = new DumpPointcutVisitor();
            final PatternNode pc2;
            if (isTypePattern) {
                pc2 = new PatternParser(v1.get()).parseTypePattern();
            } else {
                pc2 = Pointcut.fromString(v1.get());
            }
            pc2.accept(v2, null);

            // at second parsing, the String form stay stable when parsed and parsed again
            if (! v1.get().equals(v2.get())) {
                throw new ParserException("Unstable back parsing for '"+pc+"', got '" + v1.get() + "' and '" + v2.get() + "'", null);
            }
        }

        public static void main(String args[]) throws Throwable {
            String[] s = new String[]{
                //"@args(Foo, Goo, *, .., Moo)",
                //"execution(* *())",
                //"call(* *(int, Integer...))",
                //"staticinitialization(@(Foo) @(Boo) @(Goo) Moo)",
                "staticinitialization(!@(Immutable) *)"

            };
            for (int i = 0; i < s.length; i++) {
                check(s[i]);
            }
        }

    }

}
