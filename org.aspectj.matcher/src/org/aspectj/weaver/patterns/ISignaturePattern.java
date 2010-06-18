package org.aspectj.weaver.patterns;

import java.util.List;
import java.util.Map;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public interface ISignaturePattern {

	byte PATTERN = 1;
	byte NOT = 2;
	byte OR = 3;
	byte AND = 4;

	boolean matches(Member member, World world, boolean b);

	ISignaturePattern parameterizeWith(Map<String, UnresolvedType> typeVariableBindingMap, World world);

	ISignaturePattern resolveBindings(IScope scope, Bindings none);

	List<ExactTypePattern> getExactDeclaringTypes();

	boolean isMatchOnAnyName();

	boolean couldEverMatch(ResolvedType type);

	boolean isStarAnnotation();

}
