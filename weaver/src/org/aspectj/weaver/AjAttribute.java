/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FileUtil;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * These attributes are written to and read from .class files (see the JVM spec).
 * 
 * <p>Each member or type can have a number of AjAttributes.  Each
 * such attribute is in 1-1 correspondence with an Unknown bcel attribute. 
 * Creating one of these  does NOTHING to the underlying thing, so if you really 
 * want to add an attribute to a particular thing, well, you'd better actually do that.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */ 
public abstract class AjAttribute {

	public static final String AttributePrefix = "org.aspectj.weaver";

	protected abstract void write(DataOutputStream s) throws IOException;

	public abstract String getNameString();
	public char[] getNameChars() {
		return getNameString().toCharArray();
	}
	
	/**
	 * Just writes the contents
	 */
	public byte[] getBytes() {
		try {
			ByteArrayOutputStream b0 = new ByteArrayOutputStream();
			DataOutputStream s0 = new DataOutputStream(b0);
			write(s0);
			return b0.toByteArray();	
		} catch (IOException e) {
			// shouldn't happen with ByteArrayOutputStreams
			throw new RuntimeException("sanity check");
		}
	}		

	/**
	 * Writes the full attribute, i.e. name_index, length, and contents
	 */
	public byte[] getAllBytes(short nameIndex) {
		try {
			byte[] bytes = getBytes();
			
			ByteArrayOutputStream b0 = new ByteArrayOutputStream();
			DataOutputStream s0 = new DataOutputStream(b0);
	
			s0.writeShort(nameIndex);
			s0.writeInt(bytes.length);
			s0.write(bytes);
			return b0.toByteArray();		
		} catch (IOException e) {
			// shouldn't happen with ByteArrayOutputStreams
			throw new RuntimeException("sanity check");
		}
	}

	public static AjAttribute read(String name, byte[] bytes, ISourceContext context) {
		try {
			if (bytes == null) bytes = new byte[0];
			DataInputStream s = new DataInputStream(new ByteArrayInputStream(bytes));
			if (name.equals(Aspect.AttributeName)) {
				return new Aspect(PerClause.readPerClause(s, context));
			} else if (name.equals(WeaverState.AttributeName)) {
				return new WeaverState(WeaverStateKind.read(s));
			} else if (name.equals(AdviceAttribute.AttributeName)) {
				return AdviceAttribute.read(s, context);
			} else if (name.equals(PointcutDeclarationAttribute.AttributeName)) {
				return new PointcutDeclarationAttribute(ResolvedPointcutDefinition.read(s, context));
			} else if (name.equals(TypeMunger.AttributeName)) {
				return new TypeMunger(ResolvedTypeMunger.read(s, context));
			} else if (name.equals(AjSynthetic.AttributeName)) {
				return new AjSynthetic();
			} else if (name.equals(DeclareAttribute.AttributeName)) {
				return new DeclareAttribute(Declare.read(s, context));
			} else if (name.equals(PrivilegedAttribute.AttributeName)) {
				return PrivilegedAttribute.read(s, context);
			} else if (name.equals(SourceContextAttribute.AttributeName)) {
				return SourceContextAttribute.read(s);
			} else if (name.equals(EffectiveSignatureAttribute.AttributeName)) {
				return EffectiveSignatureAttribute.read(s, context);
			} else {
				throw new BCException("unknown attribute" + name);
			}
		} catch (IOException e) {
			throw new BCException("malformed " + name + " attribute " + e);
		}
	}

	//----

	/** Synthetic members should have NO advice put on them or on their contents.
	 * This attribute is currently unused as we consider all members starting 
	 * with NameMangler.PREFIX to automatically be synthetic.  As we use this we might
	 * find that we want multiple
	 * kinds of synthetic.  In particular, if we want to treat the call to a synthetic getter
	 * (say, of an introduced field) as a field reference itself, then a method might want
	 * a particular kind of AjSynthetic attribute that also includes a signature of what
	 * it stands for.
	 */
	public static class AjSynthetic extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.AjSynthetic";
		
		public String getNameString() {
			return AttributeName;
		}
		private ResolvedTypeMunger munger;
		public AjSynthetic() {}

		public void write(DataOutputStream s) throws IOException {}
	}
	
	public static class TypeMunger extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.TypeMunger";
		
		public String getNameString() {
			return AttributeName;
		}
		private ResolvedTypeMunger munger;
		public TypeMunger(ResolvedTypeMunger munger) {
			this.munger = munger;
		}

		public void write(DataOutputStream s) throws IOException {
			munger.write(s);
		}
		
		public ConcreteTypeMunger reify(World world, ResolvedTypeX aspectType) {
			return world.concreteTypeMunger(munger, aspectType);
		}
	}

	public static class WeaverState extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.WeaverState";
		
		public String getNameString() {
			return AttributeName;
		}
		private WeaverStateKind kind;
		public WeaverState(WeaverStateKind kind) {
			this.kind = kind;
		}
		public void write(DataOutputStream s) throws IOException {
			kind.write(s);
		}
		
		public WeaverStateKind reify() {
			return kind;
		}
	}
	
	public static class SourceContextAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.SourceContext";
		
		public String getNameString() {
			return AttributeName;
		}
		
		private String sourceFileName;
		private int[] lineBreaks;
		
		public SourceContextAttribute(String sourceFileName, int[] lineBreaks) {
			this.sourceFileName = sourceFileName;
			this.lineBreaks = lineBreaks;
		}
		public void write(DataOutputStream s) throws IOException {
			s.writeUTF(sourceFileName);
			FileUtil.writeIntArray(lineBreaks, s);
		}
		
		public static SourceContextAttribute read(DataInputStream s) throws IOException {
			return new SourceContextAttribute(s.readUTF(), FileUtil.readIntArray(s));
		}
		public int[] getLineBreaks() {
			return lineBreaks;
		}

		public String getSourceFileName() {
			return sourceFileName;
		}
	}

	public static class PointcutDeclarationAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.PointcutDeclaration";
		
		public String getNameString() {
			return AttributeName;
		}

		private ResolvedPointcutDefinition pointcutDef;
		public PointcutDeclarationAttribute(ResolvedPointcutDefinition pointcutDef) {
			this.pointcutDef = pointcutDef;
		}
		public void write(DataOutputStream s) throws IOException {
			pointcutDef.write(s);
		}
		
		public ResolvedPointcutDefinition reify() {
			return pointcutDef;
		}
	}		

	public static class DeclareAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Declare";
		
		public String getNameString() {
			return AttributeName;
		}

		private Declare declare;
		public DeclareAttribute(Declare declare) {
			this.declare = declare;
		}
		public void write(DataOutputStream s) throws IOException {
			declare.write(s);
		}		
		
		public Declare getDeclare() {
			return declare;
		}
	}		

	public static class AdviceAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Advice";
		
		public String getNameString() {
			return AttributeName;
		}
		
		private AdviceKind kind;
		private Pointcut pointcut;
		private int extraParameterFlags;
		private int start;
		private int end;
		private ISourceContext sourceContext;
		
		// these are only used by around advice
		private boolean proceedInInners;
		private ResolvedMember[] proceedCallSignatures; // size == # of proceed calls in body
		private boolean[] formalsUnchangedToProceed; // size == formals.size
		private TypeX[] declaredExceptions;
		
		/**
		 * @param lexicalPosition must be greater than the lexicalPosition 
		 * of any advice declared before this one in an aspect, otherwise,
		 * it can be any value.
		 */
		public AdviceAttribute(AdviceKind kind, Pointcut pointcut, int extraArgumentFlags, 
								int start, int end, ISourceContext sourceContext) {
			this.kind = kind;
			this.pointcut = pointcut;
			this.extraParameterFlags = extraArgumentFlags;
			this.start = start;
			this.end = end;
			this.sourceContext = sourceContext;

			//XXX put this back when testing works better (or fails better)
			//if (kind == AdviceKind.Around) throw new IllegalArgumentException("not for around");
		}
		
		public AdviceAttribute(AdviceKind kind, Pointcut pointcut, int extraArgumentFlags, 
								int start, int end, ISourceContext sourceContext,
								boolean proceedInInners, ResolvedMember[] proceedCallSignatures,
								boolean[] formalsUnchangedToProceed, TypeX[] declaredExceptions) {
			this.kind = kind;
			this.pointcut = pointcut;
			this.extraParameterFlags = extraArgumentFlags;
			this.start = start;
			this.end = end;
			this.sourceContext = sourceContext;
			
			if (kind != AdviceKind.Around) throw new IllegalArgumentException("only for around");
			
			this.proceedInInners = proceedInInners;
			this.proceedCallSignatures = proceedCallSignatures;
			this.formalsUnchangedToProceed = formalsUnchangedToProceed;
			this.declaredExceptions = declaredExceptions;
		}
		
		public static AdviceAttribute read(DataInputStream s, ISourceContext context) throws IOException {
			AdviceKind kind = AdviceKind.read(s);
			if (kind == AdviceKind.Around) {
				return new AdviceAttribute(
					kind,
					Pointcut.read(s, context),
					s.readByte(),
					s.readInt(), s.readInt(), context,
					s.readBoolean(),
					ResolvedMember.readResolvedMemberArray(s, context),
					FileUtil.readBooleanArray(s),
					TypeX.readArray(s));
			} else {
				return new AdviceAttribute(
					kind,
					Pointcut.read(s, context),
					s.readByte(),
					s.readInt(), s.readInt(), context);
			}
		}
 
		public void write(DataOutputStream s) throws IOException {
			kind.write(s);
			pointcut.write(s);
			s.writeByte(extraParameterFlags);
			s.writeInt(start);
			s.writeInt(end);
			
			if (kind == AdviceKind.Around) {
				s.writeBoolean(proceedInInners);
				ResolvedMember.writeArray(proceedCallSignatures, s);
				FileUtil.writeBooleanArray(formalsUnchangedToProceed, s);
				TypeX.writeArray(declaredExceptions, s);
			}
		}
		
		public Advice reify(Member signature, World world) {
			return world.concreteAdvice(this, pointcut, signature);
		}
		
		public String toString() {
			return "AdviceAttribute(" + kind + ", " + pointcut + ", " + 
						extraParameterFlags + ", " + start+")";
		}
		
		public int getExtraParameterFlags() {
			return extraParameterFlags;
		}

		public AdviceKind getKind() {
			return kind;
		}

		public Pointcut getPointcut() {
			return pointcut;
		}

		public TypeX[] getDeclaredExceptions() {
			return declaredExceptions;
		}

		public boolean[] getFormalsUnchangedToProceed() {
			return formalsUnchangedToProceed;
		}

		public ResolvedMember[] getProceedCallSignatures() {
			return proceedCallSignatures;
		}

		public boolean isProceedInInners() {
			return proceedInInners;
		}

		public int getEnd() {
			return end;
		}

		public ISourceContext getSourceContext() {
			return sourceContext;
		}

		public int getStart() {
			return start;
		}

	}
		
	public static class Aspect extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Aspect";
		public String getNameString() {
			return AttributeName;
		}		
		private PerClause perClause;

		public Aspect(PerClause perClause) {
			this.perClause = perClause;
		}

	    public PerClause reify(ResolvedTypeX inAspect) {
	    	//XXXperClause.concretize(inAspect);
	        return perClause;
	    }
		
		public void write(DataOutputStream s) throws IOException {
			perClause.write(s);
		}
	}
	
	public static class PrivilegedAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Privileged";
		
		public String getNameString() {
			return AttributeName;
		}

		private ResolvedMember[] accessedMembers;		
		public PrivilegedAttribute(ResolvedMember[] accessedMembers) {
			this.accessedMembers = accessedMembers;
		}
		public void write(DataOutputStream s) throws IOException {
			ResolvedMember.writeArray(accessedMembers, s);
		}		
		
		public ResolvedMember[] getAccessedMembers() {
			return accessedMembers;
		}

		public static PrivilegedAttribute read(DataInputStream s, ISourceContext context) throws IOException {
			return new PrivilegedAttribute(ResolvedMember.readResolvedMemberArray(s, context));
		}
	}	
	
	
	public static class EffectiveSignatureAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.EffectiveSignature";
		
		public String getNameString() {
			return AttributeName;
		}

		private ResolvedMember effectiveSignature;
		private Shadow.Kind shadowKind;
		private boolean weaveBody;
		public EffectiveSignatureAttribute(ResolvedMember effectiveSignature, Shadow.Kind shadowKind, boolean weaveBody) {
			this.effectiveSignature = effectiveSignature;
			this.shadowKind = shadowKind;
			this.weaveBody = weaveBody;
		}
		public void write(DataOutputStream s) throws IOException {
			effectiveSignature.write(s);
			shadowKind.write(s);
			s.writeBoolean(weaveBody);
		}		

		public static EffectiveSignatureAttribute read(DataInputStream s, ISourceContext context) throws IOException {
			return new EffectiveSignatureAttribute(
					ResolvedMember.readResolvedMember(s, context),
					Shadow.Kind.read(s),
					s.readBoolean());
		}
		
		public ResolvedMember getEffectiveSignature() {
			return effectiveSignature;
		}
		
		public String toString() {
			return "EffectiveSignatureAttribute(" + effectiveSignature + ", " + shadowKind + ")";
		}

		public Shadow.Kind getShadowKind() {
			return shadowKind;
		}

		public boolean isWeaveBody() {
			return weaveBody;
		}

	}	



}
