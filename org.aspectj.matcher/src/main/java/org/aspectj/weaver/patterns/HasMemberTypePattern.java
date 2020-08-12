/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 *   Nieraj Singh
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * @author colyer Matches types that have a certain method / constructor / field Currently only allowed within declare parents and
 *         declare @type
 */
public class HasMemberTypePattern extends TypePattern {

	private SignaturePattern signaturePattern;

	public HasMemberTypePattern(SignaturePattern aSignaturePattern) {
		super(false, false);
		this.signaturePattern = aSignaturePattern;
	}

	@Override
	protected boolean matchesExactly(ResolvedType type) {
		if (signaturePattern.getKind() == Member.FIELD) {
			return hasField(type);
		} else {
			return hasMethod(type);
		}
	}

	public ISignaturePattern getSignaturePattern() {
		return signaturePattern;
	}

	private final static String declareAtPrefix = "ajc$declare_at";

	private boolean hasField(ResolvedType type) {
		// TODO what about ITDs
		World world = type.getWorld();
		for (Iterator iter = type.getFields(); iter.hasNext();) {
			Member field = (Member) iter.next();
			if (field.getName().startsWith(declareAtPrefix)) {
				continue;
			}
			if (signaturePattern.matches(field, type.getWorld(), false)) {
				if (field.getDeclaringType().resolve(world) != type) {
					if (Modifier.isPrivate(field.getModifiers())) {
						continue;
					}
				}
				return true;
			}
		}
		return false;
	}

	protected boolean hasMethod(ResolvedType type) {
		// TODO what about ITDs
		World world = type.getWorld();
		for (Iterator<ResolvedMember> iter = type.getMethods(true, true); iter.hasNext();) {
			Member method = (Member) iter.next();
			if (method.getName().startsWith(declareAtPrefix)) {
				continue;
			}
			if (signaturePattern.matches(method, type.getWorld(), false)) {
				ResolvedType declaringType = method.getDeclaringType().resolve(world);
				if (declaringType != type) {
					if (Modifier.isPrivate(method.getModifiers())) {
						continue;
					}
				}
				// J9: Object.finalize() is marked Deprecated it seems... triggers unhelpful messages
				if (method.getName().equals("finalize") && declaringType.equals(ResolvedType.OBJECT)
						&& (signaturePattern.getAnnotationPattern() instanceof ExactAnnotationTypePattern)
						&& ((ExactAnnotationTypePattern)signaturePattern.getAnnotationPattern()).getAnnotationType().getSignature().equals("Ljava/lang/Deprecated;")) {
					continue;
				}
				return true;
			}
		}
		// try itds before we give up (this doesnt find annotations - the signature returned may not include them)
		List<ConcreteTypeMunger> mungers = type.getInterTypeMungersIncludingSupers();
		for (ConcreteTypeMunger munger : mungers) {
			Member member = munger.getSignature();
			if (signaturePattern.matches(member, type.getWorld(), false)) {
				if (!Modifier.isPublic(member.getModifiers())) {
					continue;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return matchesExactly(type);
	}

	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		throw new UnsupportedOperationException("hasmethod/field do not support instanceof matching");
	}

	@Override
	public TypePattern parameterizeWith(Map typeVariableMap, World w) {
		HasMemberTypePattern ret = new HasMemberTypePattern(signaturePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		// check that hasmember type patterns are allowed!
		if (!scope.getWorld().isHasMemberSupportEnabled()) {
			String msg = WeaverMessages.format(WeaverMessages.HAS_MEMBER_NOT_ENABLED, this.toString());
			scope.message(IMessage.ERROR, this, msg);
		}
		signaturePattern.resolveBindings(scope, bindings);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HasMemberTypePattern)) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		return signaturePattern.equals(((HasMemberTypePattern) obj).signaturePattern);
	}

	@Override
	public int hashCode() {
		return signaturePattern.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (signaturePattern.getKind() == Member.FIELD) {
			buff.append("hasfield(");
		} else {
			buff.append("hasmethod(");
		}
		buff.append(signaturePattern.toString());
		buff.append(")");
		return buff.toString();
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(TypePattern.HAS_MEMBER);
		signaturePattern.write(s);
		writeLocation(s);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		SignaturePattern sp = SignaturePattern.read(s, context);
		HasMemberTypePattern ret = new HasMemberTypePattern(sp);
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

}
