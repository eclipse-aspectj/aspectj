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
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.reflect.Modifier;

import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

public class ResolvedMemberSignatures15TestCase extends TestCase {

	World world;
	UnresolvedType baseType;
	UnresolvedType derivedType;
	
	// STATIC METHODS
	
	public void testBaseOnlyStaticMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
								   (Modifier.PUBLIC | Modifier.STATIC),
								   UnresolvedType.forSignature("V"),
								   "onlyBase", UnresolvedType.NONE);
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.METHOD, derivedType,
				   (Modifier.PUBLIC | Modifier.STATIC),
				   UnresolvedType.forSignature("V"),
				   "onlyBase", UnresolvedType.NONE); 
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		// this looks odd but we need both because of the way calls to inherited static methods
		// are rendered in bytecode when written as obj.foo(); - the bytecode says it is a call
		// to obj.getClass().foo() even if the static method is defined in a super type.
		assertEquals("found 2 members",2,foundMembers.length);
		StringBuilder s= new StringBuilder();
		for (ResolvedMember rm: foundMembers) {
			s.append(rm.toString()+" ");
		}
		assertEquals("Expected derived but was "+foundMembers[0]+". All="+s,"Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());		
		assertEquals("Expected base but was "+foundMembers[1]+". All="+s,"Lfluffy/Base;",foundMembers[1].getDeclaringType().getSignature());
	}
	
	public void testBothStaticMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
				   (Modifier.PUBLIC | Modifier.STATIC),
				   UnresolvedType.forSignature("V"),
				   "both",
				   new UnresolvedType[0]
				   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.METHOD,derivedType,
							(Modifier.PUBLIC | Modifier.STATIC),
							UnresolvedType.forSignature("V"),
							"both",
							new UnresolvedType[0]
							);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());		
	}

	public void testDerivedStaticMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
				   (Modifier.PUBLIC | Modifier.STATIC),
				   UnresolvedType.forSignature("V"),
				   "onlyDerived",
				   new UnresolvedType[0]
				   );
		
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found nothing",0,foundMembers.length);
		
		toFind = new MemberImpl(Member.METHOD,derivedType,
							(Modifier.PUBLIC | Modifier.STATIC),
							UnresolvedType.forSignature("V"),
							"onlyDerived",
							new UnresolvedType[0]
							);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());		
	}
	
	// NON-STATIC METHODS
	
	public void testBaseOnlyMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
								   Modifier.PUBLIC,
								   UnresolvedType.forSignature("V"),
								   "onlyBaseNonStatic",
								   new UnresolvedType[0]
								   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.METHOD,derivedType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("V"),
				   "onlyBaseNonStatic",
				   new UnresolvedType[0]
				   );
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 2 members",2,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());
		assertEquals("Lfluffy/Base;",foundMembers[1].getDeclaringType().getSignature());

	}
	
	public void testBothMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
				   Modifier.PUBLIC, UnresolvedType.forSignature("V"),
				   "bothNonStatic", UnresolvedType.NONE);
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.METHOD,derivedType,
							Modifier.PUBLIC, UnresolvedType.forSignature("V"),
							"bothNonStatic", UnresolvedType.NONE);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 2 members",2,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());
		assertEquals("Lfluffy/Base;",foundMembers[1].getDeclaringType().getSignature());
	}

	public void testDerivedMethod() {
		Member toFind = new MemberImpl(Member.METHOD,baseType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("V"),
				   "onlyDerivedNonStatic",
				   new UnresolvedType[0]
				   );
		
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found nothing",0,foundMembers.length);
		
		toFind = new MemberImpl(Member.METHOD,derivedType,
							Modifier.PUBLIC,
							UnresolvedType.forSignature("V"),
							"onlyDerivedNonStatic",
							new UnresolvedType[0]
							);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());		
	}
	
	public void testChangingThrowsClause() {
		Member toFind = new MemberImpl(Member.METHOD,derivedType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("V"),
				   "m",
				   new UnresolvedType[0]
				   );
		
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 2 members",2,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());
		assertEquals("Lfluffy/Base;",foundMembers[1].getDeclaringType().getSignature());
		
		assertEquals("throws CloneNotSupported",1,foundMembers[1].getExceptions().length);
		assertEquals("doesn't throw anything",0,foundMembers[0].getExceptions().length);
	}
	
	// CONSTRUCTORS
	
	 public void testNoWalkUpMatchingConstructor() {
		Member toFind = new MemberImpl(Member.CONSTRUCTOR,derivedType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("V"),
				   "<init>",
				   new UnresolvedType[0]
				   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());			
	 }
	 
	 public void testNoWalkUpNoMatchingConstructor() {
		Member toFind = new MemberImpl(Member.CONSTRUCTOR,derivedType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("V"),
				   "<init>",
				   new UnresolvedType[] {UnresolvedType.forSignature("I")}
				   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("No matches",0,foundMembers.length);
	 }
	
	// FIELDS
	 
	public void testBaseOnlyField() {
		Member toFind = new MemberImpl(Member.FIELD,baseType,
								   Modifier.PUBLIC,
								   UnresolvedType.forSignature("I"),
								   "onlyBase",
								   new UnresolvedType[0]
								   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.FIELD,derivedType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("I"),
				   "onlyBase",
				   new UnresolvedType[0]
				   );
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 2 members",2,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());
		assertEquals("Lfluffy/Base;",foundMembers[1].getDeclaringType().getSignature());
	}
	
	public void testBothField() {
		Member toFind = new MemberImpl(Member.FIELD,baseType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("I"),
				   "both",
				   new UnresolvedType[0]
				   );
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 member",1,foundMembers.length);
		assertEquals("Lfluffy/Base;",foundMembers[0].getDeclaringType().getSignature());
		
		toFind = new MemberImpl(Member.FIELD,derivedType,
							Modifier.PUBLIC,
							UnresolvedType.forSignature("I"),
							"both",
							new UnresolvedType[0]
							);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());
	}

	public void testDerivedField() {
		Member toFind = new MemberImpl(Member.FIELD,baseType,
				   Modifier.PUBLIC,
				   UnresolvedType.forSignature("I"),
				   "onlyDerived",
				   new UnresolvedType[0]
				   );
		
		ResolvedMember[] foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found nothing",0,foundMembers.length);
		
		toFind = new MemberImpl(Member.FIELD,derivedType,
							Modifier.PUBLIC,
							UnresolvedType.forSignature("I"),
							"onlyDerived",
							new UnresolvedType[0]
							);
		foundMembers = ResolvedMemberImpl.getJoinPointSignatures(toFind, world);
		assertEquals("found 1 members",1,foundMembers.length);
		assertEquals("Lfluffy/Derived;",foundMembers[0].getDeclaringType().getSignature());		
	}	 

	protected void setUp() throws Exception {
		world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		baseType = UnresolvedType.forSignature("Lfluffy/Base;");
		derivedType = UnresolvedType.forSignature("Lfluffy/Derived;");
	}
	
}
