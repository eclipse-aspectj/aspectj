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
 * <p>
 * Each member or type can have a number of AjAttributes. Each such attribute is in 1-1 correspondence with an Unknown bcel
 * attribute. Creating one of these does NOTHING to the underlying thing, so if you really want to add an attribute to a particular
 * thing, well, you'd better actually do that.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public abstract class AjAttribute {

	public static final String AttributePrefix = "org.aspectj.weaver";

	protected abstract void write(CompressingDataOutputStream s) throws IOException;

	public abstract String getNameString();

	public char[] getNameChars() {
		return getNameString().toCharArray();
	}

	/**
	 * Just writes the contents
	 */
	public byte[] getBytes(ConstantPoolWriter compressor) {
		try {
			ByteArrayOutputStream b0 = new ByteArrayOutputStream();
			CompressingDataOutputStream s0 = new CompressingDataOutputStream(b0, compressor);
			write(s0);
			s0.close();
			return b0.toByteArray();
		} catch (IOException e) {
			// shouldn't happen with ByteArrayOutputStreams
			throw new RuntimeException("sanity check");
		}
	}

	/**
	 * Writes the full attribute, i.e. name_index, length, and contents
	 * 
	 * @param constantPool
	 */
	public byte[] getAllBytes(short nameIndex, ConstantPoolWriter dataCompressor) {
		try {
			byte[] bytes = getBytes(dataCompressor);

			ByteArrayOutputStream b0 = new ByteArrayOutputStream();
			DataOutputStream s0 = new DataOutputStream(b0);

			s0.writeShort(nameIndex);
			s0.writeInt(bytes.length);
			s0.write(bytes);
			s0.close();
			return b0.toByteArray();
		} catch (IOException e) {
			// shouldn't happen with ByteArrayOutputStreams
			throw new RuntimeException("sanity check");
		}
	}

	public static AjAttribute read(AjAttribute.WeaverVersionInfo v, String name, byte[] bytes, ISourceContext context, World w,
			ConstantPoolReader dataDecompressor) {
		try {
			if (bytes == null) {
				bytes = new byte[0];
			}

			VersionedDataInputStream s = new VersionedDataInputStream(new ByteArrayInputStream(bytes), dataDecompressor);
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
				aa.getPointcut().check(context, w);
				return aa;
			} else if (name.equals(PointcutDeclarationAttribute.AttributeName)) {
				PointcutDeclarationAttribute pda = new PointcutDeclarationAttribute(ResolvedPointcutDefinition.read(s, context));
				pda.pointcutDef.getPointcut().check(context, w);
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
				if (w == null || w.getMessageHandler() == null) {
					throw new BCException("unknown attribute" + name);
				}
				w.getMessageHandler().handleMessage(MessageUtil.warn("unknown attribute encountered " + name));
				return null;
			}
		} catch (BCException e) {
			throw new BCException("malformed " + name + " attribute (length:" + bytes.length + ")" + e);
		} catch (IOException e) {
			throw new BCException("malformed " + name + " attribute (length:" + bytes.length + ")" + e);
		}
	}

	// ----

	/**
	 * Synthetic members should have NO advice put on them or on their contents. This attribute is currently unused as we consider
	 * all members starting with NameMangler.PREFIX to automatically be synthetic. As we use this we might find that we want
	 * multiple kinds of synthetic. In particular, if we want to treat the call to a synthetic getter (say, of an introduced field)
	 * as a field reference itself, then a method might want a particular kind of AjSynthetic attribute that also includes a
	 * signature of what it stands for.
	 */
	public static class AjSynthetic extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.AjSynthetic";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		// private ResolvedTypeMunger munger;
		public AjSynthetic() {
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
		}
	}

	public static class TypeMunger extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.TypeMunger";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final ResolvedTypeMunger munger;

		public TypeMunger(ResolvedTypeMunger munger) {
			this.munger = munger;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			munger.write(s);
		}

		public ConcreteTypeMunger reify(World world, ResolvedType aspectType) {
			return world.getWeavingSupport().concreteTypeMunger(munger, aspectType);
		}
	}

	public static class WeaverState extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.WeaverState";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final WeaverStateInfo kind;

		public WeaverState(WeaverStateInfo kind) {
			this.kind = kind;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			kind.write(s);
		}

		public WeaverStateInfo reify() {
			return kind;
		}
	}

	public static class WeaverVersionInfo extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.WeaverVersion";

		// If you change the format of an AspectJ class file, you have two
		// options:
		// - changing the minor version means you have not added anything that
		// prevents
		// previous versions of the weaver from operating (e.g.
		// MethodDeclarationLineNumber attribute)
		// - changing the major version means you have added something that
		// prevents previous
		// versions of the weaver from operating correctly.
		//
		// The user will get a warning for any org.aspectj.weaver attributes the
		// weaver does
		// not recognize.

		// When we don't know ... (i.e. pre 1.2.1)
		public final static short WEAVER_VERSION_MAJOR_UNKNOWN = 0;
		public final static short WEAVER_VERSION_MINOR_UNKNOWN = 0;

		// These are the weaver major/minor numbers for AspectJ 1.2.1
		public final static short WEAVER_VERSION_MAJOR_AJ121 = 1;
		public final static short WEAVER_VERSION_MINOR_AJ121 = 0;

		// These are the weaver major/minor numbers for AspectJ 1.5.0
		public final static short WEAVER_VERSION_MAJOR_AJ150M4 = 3;
		public final static short WEAVER_VERSION_MAJOR_AJ150 = 2;
		public final static short WEAVER_VERSION_MINOR_AJ150 = 0;

		// These are the weaver major/minor numbers for AspectJ 1.6.0
		public final static short WEAVER_VERSION_MAJOR_AJ160M2 = 5;
		public final static short WEAVER_VERSION_MAJOR_AJ160 = 4;
		public final static short WEAVER_VERSION_MINOR_AJ160 = 0;

		// These are the weaver major/minor numbers for AspectJ 1.6.1
		// added annotation value binding
		public final static short WEAVER_VERSION_MAJOR_AJ161 = 6;
		public final static short WEAVER_VERSION_MINOR_AJ161 = 0;

		// 1.6.9 adds new style ITDs. This is used to see what version of AJ was used to
		// build the ITDs so we know id the generated get/set dispatchers are using old
		// or new style (new style will be get/setters for private ITD fields)
		public final static short WEAVER_VERSION_AJ169 = 7;

		// These are the weaver major/minor versions for *this* weaver
		private final static short CURRENT_VERSION_MAJOR = WEAVER_VERSION_AJ169;
		private final static short CURRENT_VERSION_MINOR = 0;

		public final static WeaverVersionInfo UNKNOWN = new WeaverVersionInfo(WEAVER_VERSION_MAJOR_UNKNOWN,
				WEAVER_VERSION_MINOR_UNKNOWN);
		public final static WeaverVersionInfo CURRENT = new WeaverVersionInfo(CURRENT_VERSION_MAJOR, CURRENT_VERSION_MINOR);

		// These are the versions read in from a particular class file.
		private final short major_version;
		private final short minor_version;

		private long buildstamp = Version.NOTIME;

		@Override
		public String getNameString() {
			return AttributeName;
		}

		// Default ctor uses the current version numbers
		public WeaverVersionInfo() {
			major_version = CURRENT_VERSION_MAJOR;
			minor_version = CURRENT_VERSION_MINOR;
		}

		public WeaverVersionInfo(short major, short minor) {
			major_version = major;
			minor_version = minor;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			s.writeShort(CURRENT_VERSION_MAJOR);
			s.writeShort(CURRENT_VERSION_MINOR);
			s.writeLong(Version.getTime()); // build used to construct the
			// class...
		}

		public static WeaverVersionInfo read(VersionedDataInputStream s) throws IOException {
			short major = s.readShort();
			short minor = s.readShort();
			WeaverVersionInfo wvi = new WeaverVersionInfo(major, minor);
			if (s.getMajorVersion() >= WEAVER_VERSION_MAJOR_AJ150M4) {
				long stamp = 0;
				try {
					stamp = s.readLong();
					wvi.setBuildstamp(stamp);
				} catch (EOFException eof) {
					// didnt find that build stamp - its not the end of the
					// world
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
			buildstamp = stamp;
		}

		public long getBuildstamp() {
			return buildstamp;
		}

		@Override
		public String toString() {
			return major_version + "." + minor_version;
		}

		public static String toCurrentVersionString() {
			return CURRENT_VERSION_MAJOR + "." + CURRENT_VERSION_MINOR;
		}

	}

	public static class SourceContextAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.SourceContext";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final String sourceFileName;
		private final int[] lineBreaks;

		public SourceContextAttribute(String sourceFileName, int[] lineBreaks) {
			this.sourceFileName = sourceFileName;
			this.lineBreaks = lineBreaks;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			if (s.canCompress()) {
				s.writeCompressedPath(sourceFileName);
			} else {
				s.writeUTF(sourceFileName);
			}
			s.writeInt(lineBreaks.length);
			int previous = 0;
			for (int i = 0, max = lineBreaks.length; i < max; i++) {
				s.writeShort(lineBreaks[i] - previous);
				previous = lineBreaks[i];
			}
		}

		public static SourceContextAttribute read(VersionedDataInputStream s) throws IOException {
			String sourceFileName = s.isAtLeast169() ? s.readPath() : s.readUTF();
			int lineBreaks = s.readInt();
			int[] lines = new int[lineBreaks];
			int previous = 0;
			for (int i = 0; i < lineBreaks; i++) {
				if (s.isAtLeast169()) {
					lines[i] = s.readShort() + previous;
					previous = lines[i];
				} else {
					lines[i] = s.readInt();
				}
			}
			return new SourceContextAttribute(sourceFileName, lines);
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

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final int lineNumber;

		// AV: added in 1.5 M3 thus handling cases where we don't have that
		// information
		private final int offset;

		public MethodDeclarationLineNumberAttribute(int line, int offset) {
			lineNumber = line;
			this.offset = offset;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public int getOffset() {
			return offset;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			s.writeInt(lineNumber);
			s.writeInt(offset);
		}

		public static MethodDeclarationLineNumberAttribute read(VersionedDataInputStream s) throws IOException {
			int line = s.readInt();
			int offset = 0;
			if (s.available() > 0) {
				offset = s.readInt();
			}
			return new MethodDeclarationLineNumberAttribute(line, offset);
		}

		@Override
		public String toString() {
			return AttributeName + ": " + lineNumber + ":" + offset;
		}
	}

	public static class PointcutDeclarationAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.PointcutDeclaration";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final ResolvedPointcutDefinition pointcutDef;

		public PointcutDeclarationAttribute(ResolvedPointcutDefinition pointcutDef) {
			this.pointcutDef = pointcutDef;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			pointcutDef.write(s);
		}

		public ResolvedPointcutDefinition reify() {
			return pointcutDef;
		}
	}

	public static class DeclareAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Declare";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final Declare declare;

		public DeclareAttribute(Declare declare) {
			this.declare = declare;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			declare.write(s);
		}

		public Declare getDeclare() {
			return declare;
		}
	}

	public static class AdviceAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.Advice";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final AdviceKind kind;
		private final Pointcut pointcut;
		private final int extraParameterFlags;
		private final int start;
		private final int end;
		private final ISourceContext sourceContext;

		// these are only used by around advice
		private boolean proceedInInners;
		private ResolvedMember[] proceedCallSignatures; // size == # of proceed
		// calls in body
		private boolean[] formalsUnchangedToProceed; // size == formals.size
		private UnresolvedType[] declaredExceptions;

		/**
		 * @param lexicalPosition must be greater than the lexicalPosition of any advice declared before this one in an aspect,
		 *        otherwise, it can be any value.
		 */
		public AdviceAttribute(AdviceKind kind, Pointcut pointcut, int extraArgumentFlags, int start, int end,
				ISourceContext sourceContext) {
			this.kind = kind;
			this.pointcut = pointcut;
			extraParameterFlags = extraArgumentFlags;
			this.start = start;
			this.end = end;
			this.sourceContext = sourceContext;

			// XXX put this back when testing works better (or fails better)
			// if (kind == AdviceKind.Around) throw new
			// IllegalArgumentException("not for around");
		}

		public AdviceAttribute(AdviceKind kind, Pointcut pointcut, int extraArgumentFlags, int start, int end,
				ISourceContext sourceContext, boolean proceedInInners, ResolvedMember[] proceedCallSignatures,
				boolean[] formalsUnchangedToProceed, UnresolvedType[] declaredExceptions) {
			this.kind = kind;
			this.pointcut = pointcut;
			extraParameterFlags = extraArgumentFlags;
			this.start = start;
			this.end = end;
			this.sourceContext = sourceContext;

			if (kind != AdviceKind.Around) {
				throw new IllegalArgumentException("only for around");
			}

			this.proceedInInners = proceedInInners;
			this.proceedCallSignatures = proceedCallSignatures;
			this.formalsUnchangedToProceed = formalsUnchangedToProceed;
			this.declaredExceptions = declaredExceptions;
		}

		public static AdviceAttribute read(VersionedDataInputStream s, ISourceContext context) throws IOException {
			AdviceKind kind = AdviceKind.read(s);
			if (kind == AdviceKind.Around) {
				return new AdviceAttribute(kind, Pointcut.read(s, context), s.readByte(), s.readInt(), s.readInt(), context,
						s.readBoolean(), ResolvedMemberImpl.readResolvedMemberArray(s, context), FileUtil.readBooleanArray(s),
						UnresolvedType.readArray(s));
			} else {
				return new AdviceAttribute(kind, Pointcut.read(s, context), s.readByte(), s.readInt(), s.readInt(), context);
			}
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
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

		public Advice reify(Member signature, World world, ResolvedType concreteAspect) {
			return world.getWeavingSupport().createAdviceMunger(this, pointcut, signature, concreteAspect);
		}

		@Override
		public String toString() {
			return "AdviceAttribute(" + kind + ", " + pointcut + ", " + extraParameterFlags + ", " + start + ")";
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

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final PerClause perClause;
		private IScope resolutionScope;

		public Aspect(PerClause perClause) {
			this.perClause = perClause;
		}

		public PerClause reify(ResolvedType inAspect) {
			// XXXperClause.concretize(inAspect);
			return perClause;
		}

		public PerClause reifyFromAtAspectJ(ResolvedType inAspect) {
			perClause.resolve(resolutionScope);
			return perClause;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			perClause.write(s);
		}

		public void setResolutionScope(IScope binding) {
			resolutionScope = binding;
		}
	}

	public static class PrivilegedAttribute extends AjAttribute {

		public static final String AttributeName = "org.aspectj.weaver.Privileged";

		private final ResolvedMember[] accessedMembers;

		public PrivilegedAttribute(ResolvedMember[] accessedMembers) {
			this.accessedMembers = accessedMembers;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			ResolvedMemberImpl.writeArray(accessedMembers, s);
		}

		public ResolvedMember[] getAccessedMembers() {
			return accessedMembers;
		}

		public static PrivilegedAttribute read(VersionedDataInputStream stream, ISourceContext context) throws IOException {
			PrivilegedAttribute pa = new PrivilegedAttribute(ResolvedMemberImpl.readResolvedMemberArray(stream, context));
			return pa;
		}

		@Override
		public String getNameString() {
			return AttributeName;
		}
	}

	public static class EffectiveSignatureAttribute extends AjAttribute {
		public static final String AttributeName = "org.aspectj.weaver.EffectiveSignature";

		@Override
		public String getNameString() {
			return AttributeName;
		}

		private final ResolvedMember effectiveSignature;
		private final Shadow.Kind shadowKind;
		private final boolean weaveBody;

		public EffectiveSignatureAttribute(ResolvedMember effectiveSignature, Shadow.Kind shadowKind, boolean weaveBody) {
			this.effectiveSignature = effectiveSignature;
			this.shadowKind = shadowKind;
			this.weaveBody = weaveBody;
		}

		@Override
		public void write(CompressingDataOutputStream s) throws IOException {
			effectiveSignature.write(s);
			shadowKind.write(s);
			s.writeBoolean(weaveBody);
		}

		public static EffectiveSignatureAttribute read(VersionedDataInputStream s, ISourceContext context) throws IOException {
			ResolvedMember member = ResolvedMemberImpl.readResolvedMember(s, context);
			return new EffectiveSignatureAttribute(member, Shadow.Kind.read(s), s.readBoolean());
		}

		public ResolvedMember getEffectiveSignature() {
			return effectiveSignature;
		}

		@Override
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
