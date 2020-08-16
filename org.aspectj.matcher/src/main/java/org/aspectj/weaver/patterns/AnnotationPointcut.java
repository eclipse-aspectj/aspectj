/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * (at)Annotation((at)Foo) or (at)Annotation(foo)<br>
 * <p>
 * Matches any join point where the subject of the join point has an annotation matching the annotationTypePattern:
 * 
 * <br>
 * Join Point Kind - Subject <br>
 * ================================ <br>
 * method call - the target method <br>
 * method execution - the method <br>
 * constructor call - the constructor <br>
 * constructor execution - the constructor <br>
 * get - the target field <br>
 * set - the target field <br>
 * adviceexecution - the advice <br>
 * initialization - the constructor <br>
 * preinitialization - the constructor <br>
 * staticinitialization - the type being initialized <br>
 * handler - the declared type of the handled exception <br>
 */
public class AnnotationPointcut extends NameBindingPointcut {

	private ExactAnnotationTypePattern annotationTypePattern;
	private String declarationText;

	public AnnotationPointcut(ExactAnnotationTypePattern type) {
		super();
		this.annotationTypePattern = type;
		this.pointcutKind = Pointcut.ANNOTATION;
		buildDeclarationText();
	}

	public AnnotationPointcut(ExactAnnotationTypePattern type, ShadowMunger munger) {
		this(type);
		buildDeclarationText();
	}

	public ExactAnnotationTypePattern getAnnotationTypePattern() {
		return annotationTypePattern;
	}

	@Override
	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	@Override
	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		AnnotationPointcut ret = new AnnotationPointcut((ExactAnnotationTypePattern) annotationTypePattern.parameterizeWith(
				typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (info.getKind() == Shadow.StaticInitialization) {
			return annotationTypePattern.fastMatches(info.getType());
		} else {
			return FuzzyBoolean.MAYBE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		AnnotatedElement toMatchAgainst = null;
		Member member = shadow.getSignature();
		ResolvedMember rMember = member.resolve(shadow.getIWorld());

		if (rMember == null) {
			if (member.getName().startsWith(NameMangler.PREFIX)) {
				return FuzzyBoolean.NO;
			}
			shadow.getIWorld().getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return FuzzyBoolean.NO;
		}

		Shadow.Kind kind = shadow.getKind();
		if (kind == Shadow.StaticInitialization) {
			toMatchAgainst = rMember.getDeclaringType().resolve(shadow.getIWorld());
		} else if ((kind == Shadow.ExceptionHandler)) {
			toMatchAgainst = rMember.getParameterTypes()[0].resolve(shadow.getIWorld());
		} else {
			toMatchAgainst = rMember;
			// FIXME asc I'd like to get rid of this bit of logic altogether, shame ITD fields don't have an effective sig attribute
			// FIXME asc perf cache the result of discovering the member that contains the real annotations
			if (rMember.isAnnotatedElsewhere()) {
				if (kind == Shadow.FieldGet || kind == Shadow.FieldSet) {
					// FIXME asc should include supers with getInterTypeMungersIncludingSupers ?
					List mungers = rMember.getDeclaringType().resolve(shadow.getIWorld()).getInterTypeMungers();
					for (Object munger : mungers) {
						ConcreteTypeMunger typeMunger = (ConcreteTypeMunger) munger;
						if (typeMunger.getMunger() instanceof NewFieldTypeMunger) {
							ResolvedMember fakerm = typeMunger.getSignature();
							if (fakerm.equals(member)) {
								ResolvedMember ajcMethod = AjcMemberMaker.interFieldInitializer(fakerm, typeMunger.getAspectType());
								ResolvedMember rmm = findMethod(typeMunger.getAspectType(), ajcMethod);
								toMatchAgainst = rmm;
							}
						}
					}
				}
			}
		}

		annotationTypePattern.resolve(shadow.getIWorld());
		return annotationTypePattern.matches(toMatchAgainst);
	}

	private ResolvedMember findMethod(ResolvedType aspectType, ResolvedMember ajcMethod) {
		ResolvedMember decMethods[] = aspectType.getDeclaredMethods();
		for (ResolvedMember member : decMethods) {
			if (member.equals(ajcMethod)) {
				return member;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings)
	 */
	@Override
	protected void resolveBindings(IScope scope, Bindings bindings) {
		if (!scope.getWorld().isInJava5Mode()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ATANNOTATION_ONLY_SUPPORTED_AT_JAVA5_LEVEL),
					getSourceLocation()));
			return;
		}
		annotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern.resolveBindings(scope, bindings, true);
		// must be either a Var, or an annotation type pattern
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.IntMap)
	 */
	@Override
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		ExactAnnotationTypePattern newType = (ExactAnnotationTypePattern) annotationTypePattern.remapAdviceFormals(bindings);
		Pointcut ret = new AnnotationPointcut(newType, bindings.getEnclosingAdvice());
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (annotationTypePattern instanceof BindingAnnotationFieldTypePattern) {
			if (shadow.getKind() != Shadow.MethodExecution) {
				shadow.getIWorld()
						.getMessageHandler()
						.handleMessage(
								MessageUtil
										.error("Annotation field binding is only supported at method-execution join points (compiler limitation)",
												getSourceLocation()));
				return Literal.TRUE; // exit quickly, error will prevent weaving
			}
			BindingAnnotationFieldTypePattern btp = (BindingAnnotationFieldTypePattern) annotationTypePattern;
			ResolvedType formalType = btp.getFormalType().resolve(shadow.getIWorld());
			UnresolvedType annoType = btp.getAnnotationType();
			// TODO 2 need to sort out appropriate creation of the AnnotationAccessFieldVar - what happens for
			// reflective (ReflectionShadow) access to types?
			Var var = shadow.getKindedAnnotationVar(annoType);
			if (var == null) {
				throw new BCException("Unexpected problem locating annotation at join point '" + shadow + "'");
			}
			state.set(btp.getFormalIndex(), var.getAccessorForValue(formalType, btp.formalName));
		} else if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern) annotationTypePattern;
			UnresolvedType annotationType = btp.getAnnotationType();
			Var var = shadow.getKindedAnnotationVar(annotationType);

			// At this point, var *could* be null. The only reason this could happen (if we aren't failing...)
			// is if another binding annotation designator elsewhere in the pointcut is going to expose the annotation
			// eg. (execution(* a*(..)) && @annotation(foo)) || (execution(* b*(..)) && @this(foo))
			// where sometimes @annotation will be providing the value, and sometimes
			// @this will be providing the value (see pr138223)

			// If we are here for other indecipherable reasons (it's not the case above...) then
			// you might want to uncomment this next bit of code to collect the diagnostics
			// if (var == null) throw new BCException("Impossible! annotation=["+annotationType+
			// "]  shadow=["+shadow+" at "+shadow.getSourceLocation()+
			// "]    pointcut is at ["+getSourceLocation()+"]");
			if (var == null) {
				if (matchInternal(shadow).alwaysTrue()) {
					return Literal.TRUE;
				} else {
					return Literal.FALSE;
				}
			}
			state.set(btp.getFormalIndex(), var);
		}

		if (matchInternal(shadow).alwaysTrue()) {
			return Literal.TRUE;
		} else {
			return Literal.FALSE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	@Override
	public List<BindingPattern> getBindingAnnotationTypePatterns() {
		if (annotationTypePattern instanceof BindingPattern) { // BindingAnnotationTypePattern) {
			List<BindingPattern> l = new ArrayList<>();
			l.add((BindingPattern)annotationTypePattern);
			return l;
		} else {
			return Collections.emptyList();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingTypePatterns()
	 */
	@Override
	public List<BindingTypePattern> getBindingTypePatterns() {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ANNOTATION);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		AnnotationPointcut ret = new AnnotationPointcut((ExactAnnotationTypePattern) type);
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof AnnotationPointcut)) {
			return false;
		}
		AnnotationPointcut o = (AnnotationPointcut) other;
		return o.annotationTypePattern.equals(this.annotationTypePattern);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + annotationTypePattern.hashCode();
		return result;
	}

	public void buildDeclarationText() {
		StringBuffer buf = new StringBuffer();
		buf.append("@annotation(");
		String annPatt = annotationTypePattern.toString();
		buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
		buf.append(")");
		this.declarationText = buf.toString();
	}

	@Override
	public String toString() {
		return this.declarationText;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

}
