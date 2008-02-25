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


package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.Version;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.IScope;
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
	
	public static AjAttribute read(AjAttribute.WeaverVersionInfo v, String name, byte[] bytes, ISourceContext context,World w) {
		try {
			if (bytes == null) bytes = new byte[0];
			VersionedDataInputStream s = new VersionedDataInputStream(new ByteArrayInputStream(bytes));
			s.setVersion(v);
			if (name.equals(Aspect.AttributeName)) {
				return new Aspect(PerClause.readPerClause(s, context));
			} else if (name.equals(MethodDeclarationLineNumberAttribute.AttributeName)) {
			   return MethodDeclarationLineNumberAttribute.read(s);
			} else if (name.equals(WeaverState.AttributeName)) {
				return new WeaverState(WeaverStateInfo.read(s, context));
			} else if (name.equals(WeaverVersionInfo.AttributeName)) {
				return WeaverVersionInfo.read(s);
			} else if (name.equals(AdviceAttribute.AttributeName)) {
				AdviceAttribute aa = AdviceAttribute.read(s, context);
				aa.getPointcut().check(context,w);
				return aa;
			} else if (name.equals(PointcutDeclarationAttribute.AttributeName)) {
				PointcutDeclarationAttribute pda =  new PointcutDeclarationAttribute(ResolvedPointcutDefinition.read(s, context));
				pda.pointcutDef.getPointcut().check(context,w);
				return pda;
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
				// We have to tell the user about this...
				if (w == null || w.getMessageHandler()==null) throw new BCException("unknown attribute" + name);
				w.getMessageHandler().handleMessage(MessageUtil.warn("unknown attribute encountered "+name));
				return null;
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
		// private ResolvedTypeMunger munger;
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
		
		public ConcreteTypeMunger reify(World world, ResolvedType aspectType) {
			return world.concreteTypeMunger(munger, aspectType);
		}
	}

	public static class WeaverState extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.WeaverState";
		
		public String getNameString() {
			return AttributeName;
		}
		private WeaverStateInfo kind;
		public WeaverState(WeaverStateInfo kind) {
			this.kind = kind;
		}
		public void write(DataOutputStream s) throws IOException {
			kind.write(s);
		}
		
		public WeaverStateInfo reify() {
			return kind;
		}
	}
	
	public static class WeaverVersionInfo extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.WeaverVersion";
	
		// If you change the format of an AspectJ class file, you have two options:
		// - changing the minor version means you have not added anything that prevents
		//   previous versions of the weaver from operating (e.g. MethodDeclarationLineNumber attribute)
		// - changing the major version means you have added something that prevents previous
		//   versions of the weaver from operating correctly.
		//
		// The user will get a warning for any org.aspectj.weaver attributes the weaver does
		// not recognize.
		
		// When we don't know ... (i.e. pre 1.2.1)
		public static short WEAVER_VERSION_MAJOR_UNKNOWN = 0;
		public static short WEAVER_VERSION_MINOR_UNKNOWN = 0;
		
		
		// These are the weaver major/minor numbers for AspectJ 1.2.1
		public static short WEAVER_VERSION_MAJOR_AJ121 = 1;
		public static short WEAVER_VERSION_MINOR_AJ121 = 0;
		
		// These are the weaver major/minor numbers for AspectJ 1.5.0
		public static short WEAVER_VERSION_MAJOR_AJ150M4 = 3; 
		public static short WEAVER_VERSION_MAJOR_AJ150 = 2;
		public static short WEAVER_VERSION_MINOR_AJ150 = 0;

		// These are the weaver major/minor numbers for AspectJ 1.6.0
		public static short WEAVER_VERSION_MAJOR_AJ160M2 = 5;
		public static short WEAVER_VERSION_MAJOR_AJ160 = 4;
		public static short WEAVER_VERSION_MINOR_AJ160 = 0;

		
		// These are the weaver major/minor versions for *this* weaver
		private static short CURRENT_VERSION_MAJOR      = WEAVER_VERSION_MAJOR_AJ160M2;
		private static short CURRENT_VERSION_MINOR      = WEAVER_VERSION_MINOR_AJ160;
		
		public static final WeaverVersionInfo UNKNOWN = 
			new WeaverVersionInfo(WEAVER_VERSION_MAJOR_UNKNOWN,WEAVER_VERSION_MINOR_UNKNOWN);
		
		// These are the versions read in from a particular class file.
		private short major_version; 
		private short minor_version;
		
		private long buildstamp = Version.NOTIME;
		
		public String getNameString() {
			return AttributeName;
		}

		// Default ctor uses the current version numbers
		public WeaverVersionInfo() {
			this.major_version = CURRENT_VERSION_MAJOR;
			this.minor_version = CURRENT_VERSION_MINOR;
		}
		
		public WeaverVersionInfo(short major,short minor) {
			major_version = major;
			minor_version = minor;
		}
		
		public void write(DataOutputStream s) throws IOException {
			s.writeShort(CURRENT_VERSION_MAJOR);
			s.writeShort(CURRENT_VERSION_MINOR);
			s.writeLong(Version.getTime()); // build used to construct the class...
		}
		
		public static WeaverVersionInfo read(VersionedDataInputStream s) throws IOException {
			short major = s.readShort();
			short minor = s.readShort();
			WeaverVersionInfo wvi = new WeaverVersionInfo(major,minor);
			if (s.getMajorVersion()>=WEAVER_VERSION_MAJOR_AJ150M4) {
				long stamp = 0;				
				try {
					stamp = s.readLong();
					wvi.setBuildstamp(stamp);
				} catch (EOFException eof) {
					// didnt find that build stamp - its not the end of the world
				}
			}
			return wvi;
		}
		
		public short getMajorVersion() {
			return major_version;
		}
		
		public short getMinorVersion() {
			return minor_version;
		}
		
		public static short getCurrentWeaverMajorVersion() {
			return CURRENT_VERSION_MAJOR;
		}
		
		public static short getCurrentWeaverMinorVersion() {
			return CURRENT_VERSION_MINOR;
		}
		

		public void setBuildstamp(long stamp) {
			this.buildstamp = stamp;
		}
		
		public long getBuildstamp() {
			return buildstamp;
		}
		
		public String toString() {
			return major_version+"."+minor_version;
		}
		
		public static String toCurrentVersionString() {
			return CURRENT_VERSION_MAJOR+"."+CURRENT_VERSION_MINOR;
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
		
		public static SourceContextAttribute read(VersionedDataInputStream s) throws IOException {
			return new SourceContextAttribute(s.readUTF(), FileUtil.readIntArray(s));
		}
		public int[] getLineBreaks() {
			return lineBreaks;
		}

		public String getSourceFileName() {
			return sourceFileName;
		}
	}

	public static class MethodDeclarationLineNumberAttribute extends AjAttribute {

		public static final String AttributeName = "org.aspectj.weaver.MethodDeclarationLineNumber";
		
		public String getNameString() {
			return AttributeName;
		}
		
		private int lineNumber;

        // AV: added in 1.5 M3 thus handling cases where we don't have that information
        private int offset;

		public MethodDeclarationLineNumberAttribute(int line, int offset) {
			this.lineNumber = line;
            this.offset = offset;
		}
		
		public int getLineNumber() { return lineNumber; }

        public int getOffset() { return offset; }

		public void write(DataOutputStream s) throws IOException {
			s.writeInt(lineNumber);
            s.writeInt(offset);
		}
		
		public static MethodDeclarationLineNumberAttribute read(VersionedDataInputStream s) throws IOException {
            int line = s.readInt();
            int offset = 0;
            if (s.available()>0) {
                offset = s.readInt();
            }
			return new MethodDeclarationLineNumberAttribute(line, offset);
		}

		public String toString() {
			return AttributeName + ": " + lineNumber + ":" + offset;
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
		private UnresolvedType[] declaredExceptions;
		
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
								boolean[] formalsUnchangedToProceed, UnresolvedType[] declaredExceptions) {
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
		
		public static AdviceAttribute read(VersionedDataInputStream s, ISourceContext context) throws IOException {
			AdviceKind kind = AdviceKind.read(s);
			if (kind == AdviceKind.Around) {
				return new AdviceAttribute(
					kind,
					Pointcut.read(s, context),
					s.readByte(),
					s.readInt(), s.readInt(), context,
					s.readBoolean(),
					ResolvedMemberImpl.readResolvedMemberArray(s, context),
					FileUtil.readBooleanArray(s),
					UnresolvedType.readArray(s));
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
				ResolvedMemberImpl.writeArray(proceedCallSignatures, s);
				FileUtil.writeBooleanArray(formalsUnchangedToProceed, s);
				UnresolvedType.writeArray(declaredExceptions, s);
			}
		}
		
		public Advice reify(Member signature, World world) {
			return world.createAdviceMunger(this, pointcut, signature);
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

		public UnresolvedType[] getDeclaredExceptions() {
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
		private IScope resolutionScope;

		public Aspect(PerClause perClause) {
			this.perClause = perClause;
		}

	    public PerClause reify(ResolvedType inAspect) {
	    	//XXXperClause.concretize(inAspect);
	        return perClause;
	    }
	    
	    public PerClause reifyFromAtAspectJ(ResolvedType inAspect) {
	      perClause.resolve(resolutionScope);
	      return perClause;
	    }
		
		public void write(DataOutputStream s) throws IOException {
			perClause.write(s);
		}

		public void setResolutionScope(IScope binding) {
			this.resolutionScope = binding;
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
			ResolvedMemberImpl.writeArray(accessedMembers, s);
		}		
		
		public ResolvedMember[] getAccessedMembers() {
			return accessedMembers;
		}

		public static PrivilegedAttribute read(VersionedDataInputStream s, ISourceContext context) throws IOException {
			return new PrivilegedAttribute(ResolvedMemberImpl.readResolvedMemberArray(s, context));
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

		public static EffectiveSignatureAttribute read(VersionedDataInputStream s, ISourceContext context) throws IOException {
			return new EffectiveSignatureAttribute(
					ResolvedMemberImpl.readResolvedMember(s, context),
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
