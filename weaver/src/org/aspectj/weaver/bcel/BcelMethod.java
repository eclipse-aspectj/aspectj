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

package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignature.TypeVariableSignature;
import org.aspectj.util.GenericSignatureParser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;

//public final 
class BcelMethod extends ResolvedMemberImpl {

	private Method method;

	// these fields are not set for many BcelMethods...
	private ShadowMunger associatedShadowMunger;
	private ResolvedPointcutDefinition preResolvedPointcut; // used when ajc has
	// pre-resolved the
	// pointcut of some
	// @Advice
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;

	private AjAttribute.MethodDeclarationLineNumberAttribute declarationLineNumber;
	private final BcelObjectType bcelObjectType;

	private int bitflags;
	private static final int KNOW_IF_SYNTHETIC = 0x0001;
	private static final int PARAMETER_NAMES_INITIALIZED = 0x0002;
	private static final int CAN_BE_PARAMETERIZED = 0x0004;
	private static final int UNPACKED_GENERIC_SIGNATURE = 0x0008;
	private static final int IS_AJ_SYNTHETIC = 0x0040;
	private static final int IS_SYNTHETIC = 0x0080;
	private static final int IS_SYNTHETIC_INVERSE = 0x7f7f; // all bits but
	// IS_SYNTHETIC (and
	// topmost bit)
	private static final int HAS_ANNOTATIONS = 0x0400;
	private static final int HAVE_DETERMINED_ANNOTATIONS = 0x0800;

	// genericized version of return and parameter types
	private UnresolvedType genericReturnType = null;
	private UnresolvedType[] genericParameterTypes = null;

	BcelMethod(BcelObjectType declaringType, Method method) {
		super(method.getName().equals("<init>") ? CONSTRUCTOR : (method.getName().equals("<clinit>") ? STATIC_INITIALIZATION
				: METHOD), declaringType.getResolvedTypeX(), declaringType.isInterface() ? method.getModifiers()
				| Modifier.INTERFACE : method.getModifiers(), method.getName(), method.getSignature());
		this.method = method;
		sourceContext = declaringType.getResolvedTypeX().getSourceContext();
		bcelObjectType = declaringType;
		unpackJavaAttributes();
		unpackAjAttributes(bcelObjectType.getWorld());
	}

	/**
	 * This constructor expects to be passed the attributes, rather than deserializing them.
	 */
	BcelMethod(BcelObjectType declaringType, Method method, List<AjAttribute> attributes) {
		super(method.getName().equals("<init>") ? CONSTRUCTOR : (method.getName().equals("<clinit>") ? STATIC_INITIALIZATION
				: METHOD), declaringType.getResolvedTypeX(), declaringType.isInterface() ? method.getModifiers()
				| Modifier.INTERFACE : method.getModifiers(), method.getName(), method.getSignature());
		this.method = method;
		sourceContext = declaringType.getResolvedTypeX().getSourceContext();
		bcelObjectType = declaringType;
		unpackJavaAttributes();
		processAttributes(bcelObjectType.getWorld(), attributes);
	}

	// ----

	private void unpackJavaAttributes() {
		ExceptionTable exnTable = method.getExceptionTable();
		checkedExceptions = (exnTable == null) ? UnresolvedType.NONE : UnresolvedType.forNames(exnTable.getExceptionNames());
	}

	@Override
	public String[] getParameterNames() {
		determineParameterNames();
		return super.getParameterNames();
	}

	public int getLineNumberOfFirstInstruction() {
		LineNumberTable lnt = method.getLineNumberTable();
		if (lnt == null) {
			return -1;
		}
		LineNumber[] lns = lnt.getLineNumberTable();
		if (lns == null || lns.length == 0) {
			return -1;
		}
		return lns[0].getLineNumber();
	}

	public void determineParameterNames() {
		if ((bitflags & PARAMETER_NAMES_INITIALIZED) != 0) {
			return;
		}
		bitflags |= PARAMETER_NAMES_INITIALIZED;
		LocalVariableTable varTable = method.getLocalVariableTable();
		int len = getArity();
		if (varTable == null) {
			// do we have an annotation with the argNames value specified...
			AnnotationAJ[] annos = getAnnotations();
			if (annos != null && annos.length != 0) {
				AnnotationAJ[] axs = getAnnotations();
				for (int i = 0; i < axs.length; i++) {
					AnnotationAJ annotationX = axs[i];
					String typename = annotationX.getTypeName();
					if (typename.charAt(0) == 'o') {
						if (typename.equals("org.aspectj.lang.annotation.Pointcut")
								|| typename.equals("org.aspectj.lang.annotation.Before")
								|| typename.equals("org.aspectj.lang.annotation.Around")
								|| typename.startsWith("org.aspectj.lang.annotation.After")) {
							AnnotationGen a = ((BcelAnnotation) annotationX).getBcelAnnotation();
							if (a != null) {
								List<NameValuePair> values = a.getValues();
								for (NameValuePair nvPair : values) {
									if (nvPair.getNameString().equals("argNames")) {
										String argNames = nvPair.getValue().stringifyValue();
										StringTokenizer argNameTokenizer = new StringTokenizer(argNames, " ,");
										List<String> argsList = new ArrayList<String>();
										while (argNameTokenizer.hasMoreTokens()) {
											argsList.add(argNameTokenizer.nextToken());
										}
										int requiredCount = getParameterTypes().length;
										while (argsList.size() < requiredCount) {
											argsList.add("arg" + argsList.size());
										}
										setParameterNames(argsList.toArray(new String[] {}));
										return;
									}
								}
							}
						}
					}
				}
			}
			setParameterNames(Utility.makeArgNames(len));
		} else {
			UnresolvedType[] paramTypes = getParameterTypes();
			String[] paramNames = new String[len];
			int index = Modifier.isStatic(modifiers) ? 0 : 1;
			for (int i = 0; i < len; i++) {
				LocalVariable lv = varTable.getLocalVariable(index);
				if (lv == null) {
					paramNames[i] = "arg" + i;
				} else {
					paramNames[i] = lv.getName();
				}
				index += paramTypes[i].getSize();
			}
			setParameterNames(paramNames);
		}
	}

	private void unpackAjAttributes(World world) {
		associatedShadowMunger = null;
		ResolvedType resolvedDeclaringType = getDeclaringType().resolve(world);
		WeaverVersionInfo wvinfo = bcelObjectType.getWeaverVersionAttribute();
		List<AjAttribute> as = Utility.readAjAttributes(resolvedDeclaringType.getClassName(), method.getAttributes(),
				resolvedDeclaringType.getSourceContext(), world, wvinfo, new BcelConstantPoolReader(method.getConstantPool()));
		processAttributes(world, as);
		as = AtAjAttributes.readAj5MethodAttributes(method, this, resolvedDeclaringType, preResolvedPointcut,
				resolvedDeclaringType.getSourceContext(), world.getMessageHandler());
		processAttributes(world, as);
	}

	private void processAttributes(World world, List<AjAttribute> as) {
		for (AjAttribute attr : as) {
			if (attr instanceof AjAttribute.MethodDeclarationLineNumberAttribute) {
				declarationLineNumber = (AjAttribute.MethodDeclarationLineNumberAttribute) attr;
			} else if (attr instanceof AjAttribute.AdviceAttribute) {
				associatedShadowMunger = ((AjAttribute.AdviceAttribute) attr).reify(this, world, (ResolvedType) getDeclaringType());
			} else if (attr instanceof AjAttribute.AjSynthetic) {
				bitflags |= IS_AJ_SYNTHETIC;
			} else if (attr instanceof AjAttribute.EffectiveSignatureAttribute) {
				effectiveSignature = (AjAttribute.EffectiveSignatureAttribute) attr;
			} else if (attr instanceof AjAttribute.PointcutDeclarationAttribute) {
				// this is an @AspectJ annotated advice method, with pointcut pre-resolved by ajc
				preResolvedPointcut = ((AjAttribute.PointcutDeclarationAttribute) attr).reify();
			} else {
				throw new BCException("weird method attribute " + attr);
			}
		}
	}

	//
	// // for testing - if we have this attribute, return it - will return null
	// if
	// // it doesnt know anything
	// public AjAttribute[] getAttributes(String name) {
	// List results = new ArrayList();
	// List l = Utility.readAjAttributes(getDeclaringType().getClassName(),
	// method.getAttributes(),
	// getSourceContext(bcelObjectType.getWorld()), bcelObjectType.getWorld(),
	// bcelObjectType.getWeaverVersionAttribute());
	// for (Iterator iter = l.iterator(); iter.hasNext();) {
	// AjAttribute element = (AjAttribute) iter.next();
	// if (element.getNameString().equals(name))
	// results.add(element);
	// }
	// if (results.size() > 0) {
	// return (AjAttribute[]) results.toArray(new AjAttribute[] {});
	// }
	// return null;
	// }

	@Override
	public String getAnnotationDefaultValue() {
		Attribute[] attrs = method.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
			Attribute attribute = attrs[i];
			if (attribute.getName().equals("AnnotationDefault")) {
				AnnotationDefault def = (AnnotationDefault) attribute;
				return def.getElementValue().stringifyValue();
			}
		}
		return null;
	}

	// for testing - use with the method above
	public String[] getAttributeNames(boolean onlyIncludeAjOnes) {
		Attribute[] as = method.getAttributes();
		List<String> names = new ArrayList<String>();
		// String[] strs = new String[as.length];
		for (int j = 0; j < as.length; j++) {
			if (!onlyIncludeAjOnes || as[j].getName().startsWith(AjAttribute.AttributePrefix)) {
				names.add(as[j].getName());
			}
		}
		return names.toArray(new String[] {});
	}

	@Override
	public boolean isAjSynthetic() {
		return (bitflags & IS_AJ_SYNTHETIC) != 0;
	}

	@Override
	public ShadowMunger getAssociatedShadowMunger() {
		return associatedShadowMunger;
	}

	@Override
	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		return effectiveSignature;
	}

	public boolean hasDeclarationLineNumberInfo() {
		return declarationLineNumber != null;
	}

	public int getDeclarationLineNumber() {
		if (declarationLineNumber != null) {
			return declarationLineNumber.getLineNumber();
		} else {
			return -1;
		}
	}

	public int getDeclarationOffset() {
		if (declarationLineNumber != null) {
			return declarationLineNumber.getOffset();
		} else {
			return -1;
		}
	}

	@Override
	public ISourceLocation getSourceLocation() {
		ISourceLocation ret = super.getSourceLocation();
		if ((ret == null || ret.getLine() == 0) && hasDeclarationLineNumberInfo()) {
			// lets see if we can do better
			ISourceContext isc = getSourceContext();
			if (isc != null) {
				ret = isc.makeSourceLocation(getDeclarationLineNumber(), getDeclarationOffset());
			} else {
				ret = new SourceLocation(null, getDeclarationLineNumber());
			}
		}
		return ret;
	}

	@Override
	public MemberKind getKind() {
		if (associatedShadowMunger != null) {
			return ADVICE;
		} else {
			return super.getKind();
		}
	}

	@Override
	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationsRetrieved();
		for (ResolvedType aType : annotationTypes) {
			if (aType.equals(ofType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AnnotationAJ[] getAnnotations() {
		ensureAnnotationsRetrieved();
		if ((bitflags & HAS_ANNOTATIONS) != 0) {
			return annotations;
		} else {
			return AnnotationAJ.EMPTY_ARRAY;
		}
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		ensureAnnotationsRetrieved();
		return annotationTypes;
	}

	@Override
	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		ensureAnnotationsRetrieved();
		if ((bitflags & HAS_ANNOTATIONS) == 0) {
			return null;
		}
		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].getTypeName().equals(ofType.getName())) {
				return annotations[i];
			}
		}
		return null;
	}

	@Override
	public void addAnnotation(AnnotationAJ annotation) {
		ensureAnnotationsRetrieved();
		if ((bitflags & HAS_ANNOTATIONS) == 0) {
			annotations = new AnnotationAJ[1];
			annotations[0] = annotation;
			annotationTypes = new ResolvedType[1];
			annotationTypes[0] = annotation.getType();
		} else {
			// Add it to the set of annotations
			int len = annotations.length;
			AnnotationAJ[] ret = new AnnotationAJ[len + 1];
			System.arraycopy(annotations, 0, ret, 0, len);
			ret[len] = annotation;
			annotations = ret;
			ResolvedType[] newAnnotationTypes = new ResolvedType[len + 1];
			System.arraycopy(annotationTypes, 0, newAnnotationTypes, 0, len);
			newAnnotationTypes[len] = annotation.getType();
			annotationTypes = newAnnotationTypes;
		}
		bitflags |= HAS_ANNOTATIONS;
	}

	public void removeAnnotation(ResolvedType annotationType) {
		ensureAnnotationsRetrieved();
		if ((bitflags & HAS_ANNOTATIONS) == 0) {
			// nothing to do, why did we get called?
		} else {
			int len = annotations.length;
			if (len == 1) {
				bitflags &= ~HAS_ANNOTATIONS;
				annotations = null;
				annotationTypes = null;
				return;
			}
			AnnotationAJ[] ret = new AnnotationAJ[len - 1];
			int p = 0;
			for (AnnotationAJ annotation : annotations) {
				if (!annotation.getType().equals(annotationType)) {
					ret[p++] = annotation;
				}
			}
			annotations = ret;

			ResolvedType[] newAnnotationTypes = new ResolvedType[len - 1];
			p = 0;
			for (AnnotationAJ annotation : annotations) {
				if (!annotation.getType().equals(annotationType)) {
					newAnnotationTypes[p++] = annotationType;
				}
			}
			annotationTypes = newAnnotationTypes;
		}
		bitflags |= HAS_ANNOTATIONS;
	}

	public static final AnnotationAJ[] NO_PARAMETER_ANNOTATIONS = new AnnotationAJ[] {};

	public void addParameterAnnotation(int param, AnnotationAJ anno) {
		ensureParameterAnnotationsRetrieved();
		if (parameterAnnotations == NO_PARAMETER_ANNOTATIONXS) {
			// First time we've added any, so lets set up the array
			parameterAnnotations = new AnnotationAJ[getArity()][];
			for (int i = 0; i < getArity(); i++) {
				parameterAnnotations[i] = NO_PARAMETER_ANNOTATIONS;
			}
		}
		int existingCount = parameterAnnotations[param].length;
		if (existingCount == 0) {
			AnnotationAJ[] annoArray = new AnnotationAJ[1];
			annoArray[0] = anno;
			parameterAnnotations[param] = annoArray;
		} else {
			AnnotationAJ[] newAnnoArray = new AnnotationAJ[existingCount + 1];
			System.arraycopy(parameterAnnotations[param], 0, newAnnoArray, 0, existingCount);
			newAnnoArray[existingCount] = anno;
			parameterAnnotations[param] = newAnnoArray;
		}
	}

	private void ensureAnnotationsRetrieved() {
		if (method == null) {
			return; // must be ok, we have evicted it
		}
		if ((bitflags & HAVE_DETERMINED_ANNOTATIONS) != 0) {
			return;
		}
		bitflags |= HAVE_DETERMINED_ANNOTATIONS;
		AnnotationGen annos[] = method.getAnnotations();
		if (annos.length == 0) {
			annotationTypes = ResolvedType.NONE;
			annotations = AnnotationAJ.EMPTY_ARRAY;
		} else {
			int annoCount = annos.length;
			annotationTypes = new ResolvedType[annoCount];
			annotations = new AnnotationAJ[annoCount];
			for (int i = 0; i < annoCount; i++) {
				AnnotationGen annotation = annos[i];
				annotations[i] = new BcelAnnotation(annotation, bcelObjectType.getWorld());
				annotationTypes[i] = annotations[i].getType();
			}
			bitflags |= HAS_ANNOTATIONS;
		}
	}

	private void ensureParameterAnnotationsRetrieved() {
		if (method == null) {
			return; // must be ok, we have evicted it
		}
		AnnotationGen[][] pAnns = method.getParameterAnnotations();
		if (parameterAnnotationTypes == null || pAnns.length != parameterAnnotationTypes.length) {
			if (pAnns == Method.NO_PARAMETER_ANNOTATIONS) {
				parameterAnnotationTypes = BcelMethod.NO_PARAMETER_ANNOTATION_TYPES;
				parameterAnnotations = BcelMethod.NO_PARAMETER_ANNOTATIONXS;
			} else {
				AnnotationGen annos[][] = method.getParameterAnnotations();
				parameterAnnotations = new AnnotationAJ[annos.length][];
				parameterAnnotationTypes = new ResolvedType[annos.length][];
				for (int i = 0; i < annos.length; i++) {
					AnnotationGen[] annosOnThisParam = annos[i];
					if (annos[i].length == 0) {
						parameterAnnotations[i] = AnnotationAJ.EMPTY_ARRAY;
						parameterAnnotationTypes[i] = ResolvedType.NONE;
					} else {
						parameterAnnotations[i] = new AnnotationAJ[annosOnThisParam.length];
						parameterAnnotationTypes[i] = new ResolvedType[annosOnThisParam.length];
						for (int j = 0; j < annosOnThisParam.length; j++) {
							parameterAnnotations[i][j] = new BcelAnnotation(annosOnThisParam[j], bcelObjectType.getWorld());
							parameterAnnotationTypes[i][j] = bcelObjectType.getWorld().resolve(
									UnresolvedType.forSignature(annosOnThisParam[j].getTypeSignature()));
						}
					}
				}
			}
		}
	}

	@Override
	public AnnotationAJ[][] getParameterAnnotations() {
		ensureParameterAnnotationsRetrieved();
		return parameterAnnotations;
	}

	@Override
	public ResolvedType[][] getParameterAnnotationTypes() {
		ensureParameterAnnotationsRetrieved();
		return parameterAnnotationTypes;
	}

	/**
	 * A method can be parameterized if it has one or more generic parameters. A generic parameter (type variable parameter) is
	 * identified by the prefix "T"
	 */
	@Override
	public boolean canBeParameterized() {
		unpackGenericSignature();
		return (bitflags & CAN_BE_PARAMETERIZED) != 0;
	}

	@Override
	public UnresolvedType[] getGenericParameterTypes() {
		unpackGenericSignature();
		return genericParameterTypes;
	}

	/**
	 * Return the parameterized/generic return type or the normal return type if the method is not generic.
	 */
	@Override
	public UnresolvedType getGenericReturnType() {
		unpackGenericSignature();
		return genericReturnType;
	}

	/** For testing only */
	public Method getMethod() {
		return method;
	}

	private void unpackGenericSignature() {
		if ((bitflags & UNPACKED_GENERIC_SIGNATURE) != 0) {
			return;
		}
		bitflags |= UNPACKED_GENERIC_SIGNATURE;
		if (!bcelObjectType.getWorld().isInJava5Mode()) {
			genericReturnType = getReturnType();
			genericParameterTypes = getParameterTypes();
			return;
		}
		String gSig = method.getGenericSignature();
		if (gSig != null) {
			GenericSignature.MethodTypeSignature mSig = new GenericSignatureParser().parseAsMethodSignature(gSig);// method
			// .
			// getGenericSignature
			// ());
			if (mSig.formalTypeParameters.length > 0) {
				// generic method declaration
				bitflags |= CAN_BE_PARAMETERIZED;
			}

			typeVariables = new TypeVariable[mSig.formalTypeParameters.length];
			for (int i = 0; i < typeVariables.length; i++) {
				GenericSignature.FormalTypeParameter methodFtp = mSig.formalTypeParameters[i];
				try {
					typeVariables[i] = BcelGenericSignatureToTypeXConverter.formalTypeParameter2TypeVariable(methodFtp,
							mSig.formalTypeParameters, bcelObjectType.getWorld());
				} catch (GenericSignatureFormatException e) {
					// this is a development bug, so fail fast with good info
					throw new IllegalStateException("While getting the type variables for method " + this.toString()
							+ " with generic signature " + mSig + " the following error condition was detected: " + e.getMessage());
				}
			}

			GenericSignature.FormalTypeParameter[] parentFormals = bcelObjectType.getAllFormals();
			GenericSignature.FormalTypeParameter[] formals = new GenericSignature.FormalTypeParameter[parentFormals.length
					+ mSig.formalTypeParameters.length];
			// put method formal in front of type formals for overriding in
			// lookup
			System.arraycopy(mSig.formalTypeParameters, 0, formals, 0, mSig.formalTypeParameters.length);
			System.arraycopy(parentFormals, 0, formals, mSig.formalTypeParameters.length, parentFormals.length);
			GenericSignature.TypeSignature returnTypeSignature = mSig.returnType;
			try {
				genericReturnType = BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(returnTypeSignature, formals,
						bcelObjectType.getWorld());
			} catch (GenericSignatureFormatException e) {
				// development bug, fail fast with good info
				throw new IllegalStateException("While determing the generic return type of " + this.toString()
						+ " with generic signature " + gSig + " the following error was detected: " + e.getMessage());
			}
			GenericSignature.TypeSignature[] paramTypeSigs = mSig.parameters;
			if (paramTypeSigs.length == 0) {
				genericParameterTypes = UnresolvedType.NONE;
			} else {
				genericParameterTypes = new UnresolvedType[paramTypeSigs.length];
			}
			for (int i = 0; i < paramTypeSigs.length; i++) {
				try {
					genericParameterTypes[i] = BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(paramTypeSigs[i], formals,
							bcelObjectType.getWorld());
				} catch (GenericSignatureFormatException e) {
					// development bug, fail fast with good info
					throw new IllegalStateException("While determining the generic parameter types of " + this.toString()
							+ " with generic signature " + gSig + " the following error was detected: " + e.getMessage());
				}
				if (paramTypeSigs[i] instanceof TypeVariableSignature) {
					bitflags |= CAN_BE_PARAMETERIZED;
				}
			}
		} else {
			genericReturnType = getReturnType();
			genericParameterTypes = getParameterTypes();
		}
	}

	@Override
	public void evictWeavingState() {
		if (method != null) {
			unpackGenericSignature();
			unpackJavaAttributes();
			ensureAnnotationsRetrieved();
			ensureParameterAnnotationsRetrieved();
			determineParameterNames();
			// this.sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
			method = null;
		}
	}

	@Override
	public boolean isSynthetic() {
		if ((bitflags & KNOW_IF_SYNTHETIC) == 0) {
			workOutIfSynthetic();
		}
		return (bitflags & IS_SYNTHETIC) != 0;// isSynthetic;
	}

	// Pre Java5 synthetic is an attribute 'Synthetic', post Java5 it is a
	// modifier (4096 or 0x1000)
	private void workOutIfSynthetic() {
		if ((bitflags & KNOW_IF_SYNTHETIC) != 0) {
			return;
		}
		bitflags |= KNOW_IF_SYNTHETIC;
		JavaClass jc = bcelObjectType.getJavaClass();
		bitflags &= IS_SYNTHETIC_INVERSE; // unset the bit
		if (jc == null) {
			return; // what the hell has gone wrong?
		}
		if (jc.getMajor() < 49/* Java5 */) {
			// synthetic is an attribute
			String[] synthetics = getAttributeNames(false);
			if (synthetics != null) {
				for (int i = 0; i < synthetics.length; i++) {
					if (synthetics[i].equals("Synthetic")) {
						bitflags |= IS_SYNTHETIC;
						break;
					}
				}
			}
		} else {
			// synthetic is a modifier (4096)
			if ((modifiers & 4096) != 0) {
				bitflags |= IS_SYNTHETIC;
			}
		}
	}

	/**
	 * Returns whether or not the given object is equivalent to the current one. Returns true if
	 * getMethod().getCode().getCodeString() are equal. Allows for different line number tables.
	 */
	// bug 154054: is similar to equals(Object) however
	// doesn't require implementing equals in Method and Code
	// which proved expensive. Currently used within
	// CrosscuttingMembers.replaceWith() to decide if we need
	// to do a full build
	@Override
	public boolean isEquivalentTo(Object other) {
		if (!(other instanceof BcelMethod)) {
			return false;
		}
		BcelMethod o = (BcelMethod) other;
		return getMethod().getCode().getCodeString().equals(o.getMethod().getCode().getCodeString());
	}

	/**
	 * Return true if the method represents the default constructor. Hard to determine this from bytecode, but the existence of the
	 * MethodDeclarationLineNumber attribute should tell us.
	 * 
	 * @return true if this BcelMethod represents the default constructor
	 */
	@Override
	public boolean isDefaultConstructor() {
		boolean mightBe = !hasDeclarationLineNumberInfo() && name.equals("<init>") && parameterTypes.length == 0;
		if (mightBe) {
			// TODO would be nice to do a check to see if the file was compiled with javac or ajc?
			// maybe by checking the constant pool for aspectj strings?
			return true;
		} else {
			return false;
		}
	}

}