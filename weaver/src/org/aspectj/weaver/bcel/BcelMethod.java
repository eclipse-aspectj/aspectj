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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.TypeVariableSignature;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;

public final class BcelMethod extends ResolvedMemberImpl {


	
	
	private Method method;
	private boolean isAjSynthetic;
	private boolean isSynthetic;
	private boolean knowIfSynthetic = false;
	private ShadowMunger associatedShadowMunger;
	private ResolvedPointcutDefinition preResolvedPointcut;  // used when ajc has pre-resolved the pointcut of some @Advice
	
//    private ResolvedType[] annotationTypes = null;
    private AnnotationX[] annotations = null;
    private AnnotationX[][] parameterAnnotations = null;
	
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;
	private AjAttribute.MethodDeclarationLineNumberAttribute declarationLineNumber;
	private World world;
	private BcelObjectType bcelObjectType;
	
	private boolean parameterNamesInitialized = false;

	 private boolean canBeParameterized = false;
	 // genericized version of return and parameter types
	 private boolean unpackedGenericSignature = false;
	 private UnresolvedType genericReturnType = null;
	 private UnresolvedType[] genericParameterTypes = null;

	BcelMethod(BcelObjectType declaringType, Method method) {
		super(
			method.getName().equals("<init>") ? CONSTRUCTOR : 
				(method.getName().equals("<clinit>") ? STATIC_INITIALIZATION : METHOD), 
			declaringType.getResolvedTypeX(),
			declaringType.isInterface() 
				? method.getAccessFlags() | Modifier.INTERFACE
				: method.getAccessFlags(),
			method.getName(), 
			method.getSignature());
		this.method = method;
		this.sourceContext = declaringType.getResolvedTypeX().getSourceContext();
		this.world = declaringType.getResolvedTypeX().getWorld();
		this.bcelObjectType = declaringType;
		unpackJavaAttributes();
		unpackAjAttributes(world);
	}

	// ----

	private void unpackJavaAttributes() {
		ExceptionTable exnTable = method.getExceptionTable();
		checkedExceptions = (exnTable == null) 
			? UnresolvedType.NONE
			: UnresolvedType.forNames(exnTable.getExceptionNames());
			
	}
	
	public String[] getParameterNames() {
		determineParameterNames();
		return super.getParameterNames();
	}

    public int getLineNumberOfFirstInstruction() {
    	LineNumberTable lnt = method.getLineNumberTable();
    	if (lnt==null) return -1;
    	LineNumber[] lns = lnt.getLineNumberTable();
    	if (lns==null || lns.length==0) return -1;
    	return lns[0].getLineNumber();
    }
	
	public void determineParameterNames() {
		if (parameterNamesInitialized) return;
		parameterNamesInitialized=true;
		LocalVariableTable varTable = method.getLocalVariableTable();
		int len = getArity();
		if (varTable == null) {
			// do we have an annotation with the argNames value specified...
			if (hasAnnotations()) {
				AnnotationX[] axs = getAnnotations();
			    for (int i = 0; i < axs.length; i++) {
					AnnotationX annotationX = axs[i];
					String typename = annotationX.getTypeName();
					if (typename.equals("org.aspectj.lang.annotation.Pointcut") ||
						typename.equals("org.aspectj.lang.annotation.Before") ||
						typename.equals("org.aspectj.lang.annotation.Around") ||
						typename.startsWith("org.aspectj.lang.annotation.After")) {
						Annotation a = annotationX.getBcelAnnotation();
						if (a!=null) {
							List values = a.getValues();
							for (Iterator iterator = values.iterator(); iterator
									.hasNext();) {
								ElementNameValuePair nvPair = (ElementNameValuePair) iterator.next();
								if (nvPair.getNameString().equals("argNames")) {
									String argNames = nvPair.getValue().stringifyValue();
									StringTokenizer argNameTokenizer = new StringTokenizer(argNames," ,");
									List argsList = new ArrayList();
									while (argNameTokenizer.hasMoreTokens()) {
										argsList.add(argNameTokenizer.nextToken());
									}
									int requiredCount = getParameterTypes().length;
									while (argsList.size()<requiredCount) {
										argsList.add("arg"+argsList.size());
									}
									setParameterNames((String[])argsList.toArray(new String[]{}));
									return;
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
			int index = isStatic() ? 0 : 1;
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
        List as = BcelAttributes.readAjAttributes(getDeclaringType().getClassName(),method.getAttributes(), getSourceContext(world),world,bcelObjectType.getWeaverVersionAttribute());
		processAttributes(world, as);
		as = AtAjAttributes.readAj5MethodAttributes(method, this, world.resolve(getDeclaringType()), preResolvedPointcut,getSourceContext(world), world.getMessageHandler());
		processAttributes(world,as);
	}

	private void processAttributes(World world, List as) {
		for (Iterator iter = as.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			if (a instanceof AjAttribute.MethodDeclarationLineNumberAttribute) {
				declarationLineNumber = (AjAttribute.MethodDeclarationLineNumberAttribute)a;
			} else if (a instanceof AjAttribute.AdviceAttribute) {
				associatedShadowMunger = ((AjAttribute.AdviceAttribute)a).reify(this, world);
				// return;
			} else if (a instanceof AjAttribute.AjSynthetic) {
				isAjSynthetic = true;
			} else if (a instanceof AjAttribute.EffectiveSignatureAttribute) {
				//System.out.println("found effective: " + this);
				effectiveSignature = (AjAttribute.EffectiveSignatureAttribute)a;
			} else if (a instanceof AjAttribute.PointcutDeclarationAttribute) {
				// this is an @AspectJ annotated advice method, with pointcut pre-resolved by ajc
				preResolvedPointcut = ((AjAttribute.PointcutDeclarationAttribute)a).reify();
			} else {
				throw new BCException("weird method attribute " + a);
			}
		}
	}
	
	// for testing - if we have this attribute, return it - will return null if it doesnt know anything 
	public AjAttribute[] getAttributes(String name) {
		List results = new ArrayList();
		List l = BcelAttributes.readAjAttributes(getDeclaringType().getClassName(),method.getAttributes(), getSourceContext(world),world,bcelObjectType.getWeaverVersionAttribute());
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			AjAttribute element = (AjAttribute) iter.next();		
			if (element.getNameString().equals(name)) results.add(element);
		}
		if (results.size()>0) {
			return (AjAttribute[])results.toArray(new AjAttribute[]{});
		}
		return null;
	}
	
	public String getAnnotationDefaultValue() {
		Attribute[] attrs = method.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
			Attribute attribute = attrs[i];			
			if (attribute.getName().equals("AnnotationDefault")) {
				AnnotationDefault def = (AnnotationDefault)attribute;
				return def.getElementValue().stringifyValue();
			}
		}
		return null;
	}
	
	// for testing - use with the method above
	public String[] getAttributeNames(boolean onlyIncludeAjOnes) {
		Attribute[] as = method.getAttributes();
		List names = new ArrayList();
		String[] strs = new String[as.length];
		for (int j = 0; j < as.length; j++) {
			if (!onlyIncludeAjOnes || as[j].getName().startsWith(AjAttribute.AttributePrefix))
			  names.add(as[j].getName());
		}
		return (String[])names.toArray(new String[]{});
	}

	public boolean isAjSynthetic() {
		return isAjSynthetic; // || getName().startsWith(NameMangler.PREFIX);
	}
	
	//FIXME ??? needs an isSynthetic method
	
	public ShadowMunger getAssociatedShadowMunger() {
		return associatedShadowMunger;
	}
	
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

    public ISourceLocation getSourceLocation() {
      ISourceLocation ret = super.getSourceLocation(); 
      if ((ret == null || ret.getLine()==0) && hasDeclarationLineNumberInfo()) {
        // lets see if we can do better
        ISourceContext isc = getSourceContext();
        if (isc !=null) ret = isc.makeSourceLocation(getDeclarationLineNumber(), getDeclarationOffset());
        else            ret = new SourceLocation(null,getDeclarationLineNumber());
      }
      return ret;
    }
	
	public Kind getKind() {
		if (associatedShadowMunger != null) {
			return ADVICE;
		} else {
			return super.getKind();
		}
	}
	
	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationsRetrieved();
		for (Iterator iter = annotationTypes.iterator(); iter.hasNext();) {
			ResolvedType aType = (ResolvedType) iter.next();
			if (aType.equals(ofType)) return true;		
		}
		return false;
	}
	
	public AnnotationX[] getAnnotations() {
		ensureAnnotationsRetrieved();
		return annotations;
	}
	
	 public ResolvedType[] getAnnotationTypes() {
	    ensureAnnotationsRetrieved();
	    ResolvedType[] ret = new ResolvedType[annotationTypes.size()];
	    annotationTypes.toArray(ret);
	    return ret;
     }
	 

	 public AnnotationX getAnnotationOfType(UnresolvedType ofType) {
		 ensureAnnotationsRetrieved();
		 for (int i=0; i<annotations.length; i++) {
			 if (annotations[i].getTypeName().equals(ofType.getName())) return annotations[i];
		 }
		 return null;
	 }
	 
	 public void addAnnotation(AnnotationX annotation) {
	    ensureAnnotationsRetrieved();	
		// Add it to the set of annotations
		int len = annotations.length;
		AnnotationX[] ret = new AnnotationX[len+1];
		System.arraycopy(annotations, 0, ret, 0, len);
		ret[len] = annotation;
		annotations = ret;
		
		// Add it to the set of annotation types
		annotationTypes.add(UnresolvedType.forName(annotation.getTypeName()).resolve(world));
		// FIXME asc looks like we are managing two 'bunches' of annotations, one
		// here and one in the real 'method' - should we reduce it to one layer?
		method.addAnnotation(annotation.getBcelAnnotation());
	 }
	 
	 private void ensureAnnotationsRetrieved() {
		if (method == null) return; // must be ok, we have evicted it
		if (annotationTypes == null || method.getAnnotations().length!=annotations.length) { // sometimes the list changes underneath us!
    		Annotation annos[] = method.getAnnotations();
    		annotationTypes = new HashSet();
    		annotations = new AnnotationX[annos.length];
    		for (int i = 0; i < annos.length; i++) {
				Annotation annotation = annos[i];
				annotationTypes.add(world.resolve(UnresolvedType.forSignature(annotation.getTypeSignature())));
				annotations[i] = new AnnotationX(annotation,world);
			}
    	}
    }
	
	private void ensureParameterAnnotationsRetrieved() {
		if (method == null) return; // must be ok, we have evicted it
		Annotation[][] pAnns = method.getParameterAnnotations();
		if (parameterAnnotationTypes==null || pAnns.length!=parameterAnnotationTypes.length) {
			if (pAnns == Method.NO_PARAMETER_ANNOTATIONS) {
				parameterAnnotationTypes = BcelMethod.NO_PARAMETER_ANNOTATION_TYPES;
				parameterAnnotations     = BcelMethod.NO_PARAMETER_ANNOTATIONXS;
			} else {
				Annotation annos[][] = method.getParameterAnnotations();
				parameterAnnotations = new AnnotationX[annos.length][];
				parameterAnnotationTypes = new ResolvedType[annos.length][];
				for (int i=0;i<annos.length;i++) {
					parameterAnnotations[i] = new AnnotationX[annos[i].length];
					parameterAnnotationTypes[i] = new ResolvedType[annos[i].length];
					for (int j=0;j<annos[i].length;j++) {
						parameterAnnotations[i][j] = new AnnotationX(annos[i][j],world);
						parameterAnnotationTypes[i][j] = world.resolve(UnresolvedType.forSignature(annos[i][j].getTypeSignature()));
					}
				}
			}
		}
	}

	public AnnotationX[][] getParameterAnnotations() { 
		ensureParameterAnnotationsRetrieved();
		return parameterAnnotations;
	}
	
	public ResolvedType[][] getParameterAnnotationTypes() { 
		ensureParameterAnnotationsRetrieved();
		return parameterAnnotationTypes;
	}
	 

	 /**
	  * A method can be parameterized if it has one or more generic
	  * parameters. A generic parameter (type variable parameter) is
	  * identified by the prefix "T"
	  */
	 public boolean canBeParameterized() {
		 unpackGenericSignature();
		return canBeParameterized;
	}
	 
	 
	 public UnresolvedType[] getGenericParameterTypes() {
		 unpackGenericSignature();
		 return genericParameterTypes;
	 }
	 
	 public UnresolvedType getGenericReturnType() {
		 unpackGenericSignature();
		 return genericReturnType;
	 }
	 
	 /** For testing only */
	 public Method getMethod() { return method; }
	 
	 private void unpackGenericSignature() {
		 if (unpackedGenericSignature) return;
		 unpackedGenericSignature = true;
 		 if (!world.isInJava5Mode()) { 
 			 this.genericReturnType = getReturnType();
 			 this.genericParameterTypes = getParameterTypes();
 			 return;
 		 }
		 String gSig = method.getGenericSignature();
		 if (gSig != null) {
			 Signature.MethodTypeSignature mSig = new GenericSignatureParser().parseAsMethodSignature(gSig);//method.getGenericSignature());
 			 if (mSig.formalTypeParameters.length > 0) {
				// generic method declaration
				canBeParameterized = true;
			 }
 			 
 			typeVariables = new TypeVariable[mSig.formalTypeParameters.length];
	    	  for (int i = 0; i < typeVariables.length; i++) {
				Signature.FormalTypeParameter methodFtp = mSig.formalTypeParameters[i];
				try {
					typeVariables[i] = BcelGenericSignatureToTypeXConverter.formalTypeParameter2TypeVariable(
							methodFtp, 
							mSig.formalTypeParameters,
							world);
				} catch (GenericSignatureFormatException e) {
					// this is a development bug, so fail fast with good info
					throw new IllegalStateException(
							"While getting the type variables for method " + this.toString()
							+ " with generic signature " + mSig + 
							" the following error condition was detected: " + e.getMessage());
				}
			  }
 			 
 			 Signature.FormalTypeParameter[] parentFormals = bcelObjectType.getAllFormals();
 			 Signature.FormalTypeParameter[] formals = new
 			 	Signature.FormalTypeParameter[parentFormals.length + mSig.formalTypeParameters.length];
 			 // put method formal in front of type formals for overriding in lookup
 			 System.arraycopy(mSig.formalTypeParameters,0,formals,0,mSig.formalTypeParameters.length);
 			 System.arraycopy(parentFormals,0,formals,mSig.formalTypeParameters.length,parentFormals.length);
 			 Signature.TypeSignature returnTypeSignature = mSig.returnType;
			 try {
				genericReturnType = BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
						 returnTypeSignature, formals,
						 world);
			} catch (GenericSignatureFormatException e) {
//				 development bug, fail fast with good info
				throw new IllegalStateException(
						"While determing the generic return type of " + this.toString()
						+ " with generic signature " + gSig + " the following error was detected: "
						+ e.getMessage());
			}
			 Signature.TypeSignature[] paramTypeSigs = mSig.parameters;
			 genericParameterTypes = new UnresolvedType[paramTypeSigs.length];
			 for (int i = 0; i < paramTypeSigs.length; i++) {
				try {
					genericParameterTypes[i] = 
						BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
								paramTypeSigs[i],formals,world);
				} catch (GenericSignatureFormatException e) {
//					 development bug, fail fast with good info
					throw new IllegalStateException(
							"While determining the generic parameter types of " + this.toString()
							+ " with generic signature " + gSig + " the following error was detected: "
							+ e.getMessage());
				}
				if (paramTypeSigs[i] instanceof TypeVariableSignature) {
					canBeParameterized = true;
				}
			 }
		 } else {
			 genericReturnType = getReturnType();
			 genericParameterTypes = getParameterTypes();
		 }
	 }
	 
	 public void evictWeavingState() {
		 if (method != null) {
			 unpackGenericSignature();
			 unpackJavaAttributes();
			 ensureAnnotationsRetrieved();
			 ensureParameterAnnotationsRetrieved();
			 determineParameterNames();
// 			 this.sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
			 method = null;
		 }
	 }

	public boolean isSynthetic() {
		if (!knowIfSynthetic) workOutIfSynthetic();
		return isSynthetic;
	}

	// Pre Java5 synthetic is an attribute 'Synthetic', post Java5 it is a modifier (4096 or 0x1000)
	private void workOutIfSynthetic() {
		knowIfSynthetic=true;
		JavaClass jc = bcelObjectType.getJavaClass();
		isSynthetic=false;
		if (jc==null) return; // what the hell has gone wrong?
		if (jc.getMajor()<49/*Java5*/) {
			// synthetic is an attribute
			String[] synthetics =  getAttributeNames(false);
			if (synthetics!=null) {
				for (int i = 0; i < synthetics.length; i++) {
					if (synthetics[i].equals("Synthetic")) {isSynthetic=true;break;}
				}
			}
		} else {
			// synthetic is a modifier (4096)
			isSynthetic = (modifiers&4096)!=0;
		}
	}

	/**
	 * Returns whether or not the given object is equivalent to the
	 * current one. Returns true if getMethod().getCode().getCodeString()
	 * are equal. Allows for different line number tables.
	 */
	// bug 154054: is similar to equals(Object) however
	// doesn't require implementing equals in Method and Code
	// which proved expensive. Currently used within 
	// CrosscuttingMembers.replaceWith() to decide if we need
	// to do a full build
	public boolean isEquivalentTo(Object other) {	
		if(! (other instanceof BcelMethod)) return false;
		BcelMethod o = (BcelMethod)other;
		return getMethod().getCode().getCodeString().equals(
				o.getMethod().getCode().getCodeString());
	}

}
