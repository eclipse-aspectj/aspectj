/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * Represents an attempt to bind the field of an annotation within a pointcut. For example:<br>
 * <code><pre>
 * before(Level lev): execution(* *(..)) &amp;&amp; @annotation(TraceAnnotation(lev))
 * </pre></code><br>
 * This binding annotation type pattern will be for 'lev'.
 */
public class ExactAnnotationFieldTypePattern extends ExactAnnotationTypePattern {

	UnresolvedType annotationType;
	private ResolvedMember field;

	public ExactAnnotationFieldTypePattern(ExactAnnotationTypePattern p, String formalName) {
		super(formalName);
		this.annotationType = p.annotationType;
		this.copyLocationFrom(p);
	}

	public ExactAnnotationFieldTypePattern(UnresolvedType annotationType, String formalName) {
		super(formalName);
		this.annotationType = annotationType;
	}

	/**
	 * resolve one of these funky things. Need to: <br>
	 * (a) Check the formal is bound <br>
	 * (b) Check the annotation type is valid
	 */
	@Override
	public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		if (resolved) {
			return this;
		}
		resolved = true;
		FormalBinding formalBinding = scope.lookupFormal(formalName);
		if (formalBinding == null) {
			scope.message(IMessage.ERROR, this,
					"When using @annotation(<annotationType>(<annotationField>)), <annotationField> must be bound");
			return this;
		}

		annotationType = scope.getWorld().resolve(annotationType, true);

		// May not be directly found if in a package, so go looking if that is the case:
		if (ResolvedType.isMissing(annotationType)) {
			String cleanname = annotationType.getName();
			UnresolvedType type = null;
			while (ResolvedType.isMissing(type = scope.lookupType(cleanname, this))) {
				int lastDot = cleanname.lastIndexOf('.');
				if (lastDot == -1) {
					break;
				}
				cleanname = cleanname.substring(0, lastDot) + "$" + cleanname.substring(lastDot + 1);
			}
			annotationType = scope.getWorld().resolve(type, true);
			if (ResolvedType.isMissing(annotationType)) {
				// there are likely to be other errors around that have led to us being unable to
				// resolve the annotation type, let's quit now
				return this;
			}
		}

		verifyIsAnnotationType((ResolvedType) annotationType, scope);

		ResolvedType formalBindingType = formalBinding.getType().resolve(scope.getWorld());

		String bindingTypeSignature = formalBindingType.getSignature();
		if (!(formalBindingType.isEnum() || bindingTypeSignature.equals("Ljava/lang/String;") || bindingTypeSignature.equals("I"))) {
			scope.message(IMessage.ERROR, this,
					"The field within the annotation must be an enum, string or int. '" + formalBinding.getType()
							+ "' is not (compiler limitation)");
		}
		bindingPattern = true;

		// Check that the formal is bound to a type that is represented by one field in the annotation type
		ReferenceType theAnnotationType = (ReferenceType) annotationType;
		ResolvedMember[] annotationFields = theAnnotationType.getDeclaredMethods();
		field = null;
		boolean looksAmbiguous = false;
		for (int i = 0; i < annotationFields.length; i++) {
			ResolvedMember resolvedMember = annotationFields[i];
			if (resolvedMember.getReturnType().equals(formalBinding.getType())) {
				if (field != null) {
					boolean haveProblem = true;
					// use the name to differentiate
					if (field.getName().equals(formalName)) {
						// don't use this new field
						haveProblem = false;
					} else if (resolvedMember.getName().equals(formalName)) {
						// ok, let's use this one
						field = resolvedMember;
						haveProblem = false;
					}
					if (haveProblem) {
						looksAmbiguous = true;
					}
				} else {
					field = resolvedMember;
				}
			}
		}
		if (looksAmbiguous) {
			// did we find something that does match by name?
			if (field == null || !field.getName().equals(formalName)) {
				scope.message(IMessage.ERROR, this, "The field type '" + formalBinding.getType()
						+ "' is ambiguous for annotation type '" + theAnnotationType.getName() + "'");
			}
		}
		if (field == null) {
			scope.message(IMessage.ERROR, this, "No field of type '" + formalBinding.getType() + "' exists on annotation type '"
					+ theAnnotationType.getName() + "'");
		}

		BindingAnnotationFieldTypePattern binding = new BindingAnnotationFieldTypePattern(formalBinding.getType(),
				formalBinding.getIndex(), theAnnotationType);
		binding.copyLocationFrom(this);
		binding.formalName = this.formalName;
		bindings.register(binding, scope);
		binding.resolveBinding(scope.getWorld());
		return binding;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.EXACTFIELD);
		s.writeUTF(formalName);
		annotationType.write(s);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ExactAnnotationFieldTypePattern ret;
		String formalName = s.readUTF();
		UnresolvedType annotationType = UnresolvedType.read(s);
		ret = new ExactAnnotationFieldTypePattern(annotationType, formalName);
		ret.readLocation(context, s);
		return ret;
	}

	// ---

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExactAnnotationFieldTypePattern)) {
			return false;
		}
		ExactAnnotationFieldTypePattern other = (ExactAnnotationFieldTypePattern) obj;
		return (other.annotationType.equals(annotationType)) && (other.field.equals(field))
				&& (other.formalName.equals(this.formalName));
	}

	@Override
	public int hashCode() {
		int hashcode = annotationType.hashCode();
		hashcode = hashcode * 37 + field.hashCode();
		hashcode = hashcode * 37 + formalName.hashCode();
		return hashcode;
	}

	// TODO these are currently unimplemented as I believe it resolves to a Binding form *always* and so they don't get
	// called

	@Override
	public FuzzyBoolean fastMatches(AnnotatedElement annotated) {
		throw new BCException("unimplemented");
	}

	@Override
	public UnresolvedType getAnnotationType() {
		throw new BCException("unimplemented");
	}

	@Override
	public Map getAnnotationValues() {
		throw new BCException("unimplemented");
	}

	@Override
	public ResolvedType getResolvedAnnotationType() {
		throw new BCException("unimplemented");
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		throw new BCException("unimplemented");
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		throw new BCException("unimplemented");
	}

	@Override
	public FuzzyBoolean matchesRuntimeType(AnnotatedElement annotated) {
		throw new BCException("unimplemented");
	}

	@Override
	public AnnotationTypePattern parameterizeWith(Map typeVariableMap, World w) {
		throw new BCException("unimplemented");
	}

	@Override
	public void resolve(World world) {
		throw new BCException("unimplemented");
	}

	@Override
	public String toString() {
		if (!resolved && formalName != null) {
			return formalName;
		}
		StringBuffer ret = new StringBuffer();
		ret.append("@").append(annotationType.toString());
		ret.append("(").append(formalName).append(")");
		return ret.toString();
	}

}
