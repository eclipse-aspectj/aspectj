/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.weaver.AbstractReferenceTypeDelegate;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.SourceContextImpl;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.DeclareAttribute;
import org.aspectj.weaver.AjAttribute.PointcutDeclarationAttribute;
import org.aspectj.weaver.AjAttribute.PrivilegedAttribute;
import org.aspectj.weaver.AjAttribute.SourceContextAttribute;
import org.aspectj.weaver.AjAttribute.TypeMunger;
import org.aspectj.weaver.AjAttribute.WeaverState;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.ClassReader;
import org.aspectj.org.objectweb.asm.Opcodes;

/**
 * A lightweight fast delegate that is an alternative to a BCEL delegate.
 * The type being represented is being referenced during a compile
 * or weave but is not exposed to the weaver, for example java.lang.String.  
 * Unnecessary information is not processed - for example the linenumbertable.
 * 
 * What might need visiting that currently isnt?
 * - methods so we can get their annotations
 * 
 * Implementation:
 * The state in this type is populated by an ASM ClassVisitor, attributes and
 * annotations are mostly unpacked lazily. 
 * 
 * @author AndyClement
 */
public class AsmDelegate extends AbstractReferenceTypeDelegate {

	public static boolean careAboutMemberAnnotationsAndAttributes = true;

	private World w;
	
	int classModifiers;

	ResolvedPointcutDefinition[] pointcuts = null;
	TypeVariable[]   typeVariables; // filled in when signature is parsed

	ResolvedType[]   annotationTypes;
	AnnotationX[]    annotationXs;
	List annotations = Collections.EMPTY_LIST;

	ResolvedMember[] methods;
	ResolvedMember[] fields;
	List attributes  = Collections.EMPTY_LIST;
	Collection /*of Declare*/ declares  = null;
	Collection /*of ConcreteTypeMunger*/ typeMungers  = null;
	Collection /*of ResolvedMember*/ privilegedAccesses  = null;

	private int bitflag = 0; // see below for the relevant bits
	
	private final static int DISCOVERED_POINTCUTS           = 0x0001;// guard for lazy initialization of pointcuts
	private final static int DISCOVERED_DECLARES            = 0x0002;// guard for lazy initialization of declares
	private final static int DISCOVERED_TYPEMUNGERS         = 0x0004;// guard for lazy initialization of type mungers
	private final static int DISCOVERED_PRIVILEGEDACCESSES  = 0x0008;// guard for lazy initialization of privileged access list
	private final static int DISCOVERED_SOURCECONTEXT       = 0x0010;// Sourcecontext is actually held in supertype
	private final static int DISCOVERED_WEAVERSTATEINFO     = 0x0020;
	private final static int SIGNATURE_UNPACKED             = 0x0040;
	private final static int ANNOTATION_TYPES_CORRECT       = 0x0080;
	private final static int ANNOTATIONX_CORRECT            = 0x0100;
	private final static int SUPERSET                       = 0x0200;
	private final static int FIELDSFIXEDUP                  = 0x0400;
	private final static int METHODSFIXEDUP                 = 0x0800;

	private ResolvedType superclassType = null;	
	String                superclassName = null;
	
	private ResolvedType[] interfaceTypes = null;
	String[]                interfaceNames = null;
	
	// For the fields below, which are set based on attribute or annotation values, 
	// some are 'eager' and set as the visitor goes through the type.  Some are 
	// lazy and only set when someone requests their value
	
	// eager: populated from the 'Aspect' attribute
	boolean isAspect    = false;
	PerClause perClause = null;
	
	// eager: populated from the 'WeaverVersionInfo' attribute
	WeaverVersionInfo weaverVersion = AjAttribute.WeaverVersionInfo.UNKNOWN;
	
	// lazy: populated from the 'WeaverStateInfo' attribute
	WeaverStateInfo weaverStateInfo = null;
	
	// eager: populated from the visitInnerClass method in the TypeVisitor
	boolean isAnonymous = false;
	boolean isNested = false;
	
	// eager: populated from the visit method in the TypeVisitor
	boolean isGenericType = false;
	String declaredSignature = null;
		
	// eager: populated from the 'Retention' annotation
	boolean isRuntimeRetention = false;
	String retentionPolicy = null;
	
	// eager: populated from the 'Target' annotation
	boolean canAnnotationTargetType = true; // true unless we learn otherwise
	AnnotationTargetKind[] targetKinds = null;

	// -----
	
	public AsmDelegate(ReferenceType rt,InputStream inputStream) {
		super(rt,false);
		w = rt.getWorld();
		try {
			  new ClassReader(inputStream).accept(new TypeVisitor(this),AsmConstants.ajAttributes,true);
			  inputStream.close();
			  // why-o-why-o-why ?
			  if ((classModifiers&4096)>0) classModifiers-=4096; // remove SYNTHETIC
			  if ((classModifiers&131072)>0) classModifiers-=131072; // remove DEPRECATED
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
        setSourceContext(new SourceContextImpl(this));
	}
	
	
	public boolean isAnnotationStyleAspect() {
		return false;
	}


	public boolean canAnnotationTargetType() {
		return canAnnotationTargetType;
	}

	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return targetKinds;
	}

	public boolean isGeneric() {
		return isGenericType;
	}

	public boolean isAnonymous() {
		return isAnonymous;
	}

	public boolean isNested() {
		return isNested;
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationsUnpacked();
		for (int i = 0; i < annotationTypes.length; i++) {
			if (annotationTypes[i].equals(ofType)) return true;
		}
		return false;
	}

	public AnnotationX[] getAnnotations() {
		ensureAnnotationXsUnpacked();
		return annotationXs;
	}

	public ResolvedType[] getAnnotationTypes() {
		ensureAnnotationsUnpacked();
		return annotationTypes;
	}
	
	private void ensureAnnotationXsUnpacked() {
		if ( (bitflag&ANNOTATION_TYPES_CORRECT)!=0 && (bitflag&ANNOTATIONX_CORRECT)!=0) return;
		ensureAnnotationsUnpacked();
		if (annotations.size()==0) {
			annotationXs = AnnotationX.NONE;
		} else {
			annotationXs = new AnnotationX[annotations.size()];
			int pos = 0;
			for (Iterator iter = annotations.iterator(); iter.hasNext();) {
				AnnotationAJ element = (AnnotationAJ) iter.next();
				annotationXs[pos++] = new AnnotationX(element,w);
			}
			annotations = null; // dont need them any more!
		}
		bitflag|=ANNOTATIONX_CORRECT;
	}
	
	private void ensureAnnotationsUnpacked() {
		if ((bitflag&ANNOTATION_TYPES_CORRECT)!=0) return;
		if (annotations.size()==0) {
			annotationTypes = ResolvedType.NONE;
		} else {
			annotationTypes = new ResolvedType[annotations.size()];
			int pos = 0;
			for (Iterator iter = annotations.iterator(); iter.hasNext();) {
				AnnotationAJ element = (AnnotationAJ) iter.next();
				annotationTypes[pos++] = w.resolve(UnresolvedType.forSignature(element.getTypeSignature()));
			}
		}
		bitflag|=ANNOTATION_TYPES_CORRECT;
	}
	
	public Signature.FormalTypeParameter[] getAllFormals() {
		ensureSignatureUnpacked();
		if (formalsForResolution == null) {
			return new Signature.FormalTypeParameter[0];
		} else {
			return formalsForResolution;
		}
	}
	
	// for testing - if we have this attribute, return it - will return null if it doesnt know anything 
	public AjAttribute[] getAttributes(String name) {
		List results = new ArrayList();
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute element = (Attribute) iter.next();
			if (element.type.equals(name) && (element instanceof AjASMAttribute)) {
				results.add(((AjASMAttribute)element).unpack(this));
			}
		}
		if (results.size()>0) {
			return (AjAttribute[])results.toArray(new AjAttribute[]{});
		}
		return null;
	}
	
	// for testing - use with the method above
	public String[] getAttributeNames() {
		String[] strs = new String[attributes.size()];
		int i = 0;
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute element = (Attribute) iter.next();
			strs[i++] = element.type;
		}
		return strs;
	}
	


	public ISourceContext getSourceContext() {
		if ((bitflag&DISCOVERED_SOURCECONTEXT)==0) {
			bitflag|=DISCOVERED_SOURCECONTEXT;
			Attribute foundIt = null;
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.SourceContextAttribute.AttributeName)) {
						foundIt = element;
						SourceContextAttribute sca = (SourceContextAttribute)((AjASMAttribute)element).unpack(this);
						if (super.getSourceContext()==SourceContextImpl.UNKNOWN_SOURCE_CONTEXT) {
							super.setSourceContext(new SourceContextImpl(this));
						}
						((SourceContextImpl)super.getSourceContext()).configureFromAttribute(sca.getSourceFileName(),sca.getLineBreaks());
						break;
					}
				}
			}
			if (foundIt!=null) attributes.remove(foundIt); // Save space
		}
		return super.getSourceContext();
	}
	
	public WeaverStateInfo getWeaverState() {
		if ((bitflag&DISCOVERED_WEAVERSTATEINFO)==0) {
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.WeaverState.AttributeName)) {
						WeaverState wsInfo = (WeaverState)((AjASMAttribute)element).unpack(this);
						weaverStateInfo = wsInfo.reify();
						break;
					}
				}
			}
			bitflag|=DISCOVERED_WEAVERSTATEINFO;
		}
		return weaverStateInfo;
	}

	public String getDeclaredGenericSignature() {
		return declaredSignature;
	}
	
	public Collection getTypeMungers() {
		if ((bitflag&DISCOVERED_TYPEMUNGERS)==0) {
			typeMungers = new ArrayList();
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.TypeMunger.AttributeName)) {
						TypeMunger typeMunger = (TypeMunger)((AjASMAttribute)element).unpack(this);
						typeMungers.add(typeMunger.reify(w,getResolvedTypeX()));
					}
				}
			}
			bitflag|=DISCOVERED_TYPEMUNGERS;
		}
		return typeMungers;
	}

	public Collection getPrivilegedAccesses() {
		if ((bitflag&DISCOVERED_PRIVILEGEDACCESSES)==0) {
			privilegedAccesses = new ArrayList();
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.PrivilegedAttribute.AttributeName)) {
						PrivilegedAttribute privilegedAttribute = (PrivilegedAttribute)((AjASMAttribute)element).unpack(this);
						ResolvedMember[] pas =privilegedAttribute.getAccessedMembers();
						for (int i = 0; i < pas.length; i++) {
							privilegedAccesses.add(pas[i]);
						}
					}
				}
			}
			bitflag|=DISCOVERED_PRIVILEGEDACCESSES;
		}
		return privilegedAccesses;
	}
	
	public TypeVariable[] getTypeVariables() {
		ensureSignatureUnpacked();
		return typeVariables;
	}
	
	private Signature.FormalTypeParameter[] formalsForResolution = null;

	private void ensureSignatureUnpacked() {
		if ((bitflag&SIGNATURE_UNPACKED)!=0) return;
		typeVariables=TypeVariable.NONE;
		if (!getResolvedTypeX().getWorld().isInJava5Mode()) {
			bitflag|=SIGNATURE_UNPACKED;
			return;
		}
		if (declaredSignature!=null) {
		  GenericSignatureParser parser = new GenericSignatureParser();
		  Signature.ClassSignature cSig = parser.parseAsClassSignature(declaredSignature);
		  typeVariables = new TypeVariable[cSig.formalTypeParameters.length];
    	  for (int i = 0; i < typeVariables.length; i++) {
			Signature.FormalTypeParameter ftp = cSig.formalTypeParameters[i];
			try {
				typeVariables[i] = BcelGenericSignatureToTypeXConverter.formalTypeParameter2TypeVariable(
						ftp, 
						cSig.formalTypeParameters,
						getResolvedTypeX().getWorld());
			} catch (GenericSignatureFormatException e) {
				// this is a development bug, so fail fast with good info
				throw new IllegalStateException(
						"While getting the type variables for type " + this.toString()
						+ " with generic signature " + cSig + 
						" the following error condition was detected: " + e.getMessage());
			}
		  }
    	  if (cSig != null) {
  			formalsForResolution = cSig.formalTypeParameters;
  			if (isNested()) {
  				// we have to find any type variables from the outer type before proceeding with resolution.
  				Signature.FormalTypeParameter[] extraFormals = getFormalTypeParametersFromOuterClass();
  				if (extraFormals.length > 0) {
  					List allFormals = new ArrayList();
  					for (int i = 0; i < formalsForResolution.length; i++) {
  						allFormals.add(formalsForResolution[i]);
  					}
  					for (int i = 0; i < extraFormals.length; i++) {
  						allFormals.add(extraFormals[i]);
  					}
  					formalsForResolution = new Signature.FormalTypeParameter[allFormals.size()];
  					allFormals.toArray(formalsForResolution);
  				}
  			}
  			Signature.ClassTypeSignature superSig = cSig.superclassSignature;
  			try {
  				this.superclassType = 
  					BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
  							superSig, formalsForResolution, getResolvedTypeX().getWorld());
  				bitflag|=SUPERSET;
  			} catch (GenericSignatureFormatException e) {
  				// development bug, fail fast with good info
  				throw new IllegalStateException(
  						"While determing the generic superclass of " + getResolvedTypeX()
  						+ " with generic signature " + declaredSignature + " the following error was detected: "
  						+ e.getMessage());
  			}
  			this.interfaceTypes = new ResolvedType[cSig.superInterfaceSignatures.length];
  			for (int i = 0; i < cSig.superInterfaceSignatures.length; i++) {
  				try {
  					this.interfaceTypes[i] = 
  						BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
  								cSig.superInterfaceSignatures[i],
  								formalsForResolution, 
  								getResolvedTypeX().getWorld());
  					
  				} catch (GenericSignatureFormatException e) {
  					// development bug, fail fast with good info
  					throw new IllegalStateException(
  							"While determing the generic superinterfaces of " + getResolvedTypeX()
  							+ " with generic signature " + declaredSignature + " the following error was detected: "
  							+ e.getMessage());
  				}
  			}
  			if (isGeneric()) {
  				// update resolved typex to point at generic type not raw type.
				ReferenceType genericType = (ReferenceType) this.resolvedTypeX.getGenericType();
				//genericType.setSourceContext(this.resolvedTypeX.getSourceContext());
				genericType.setStartPos(this.resolvedTypeX.getStartPos());
				this.resolvedTypeX = genericType;
  			}
    	  }
    	  
		} 
		bitflag|=SIGNATURE_UNPACKED;
	}


	private ReferenceType getOuterClass() {
		if (!isNested()) throw new IllegalStateException("Can't get the outer class of a non-nested type");
		int lastDollar = getResolvedTypeX().getName().lastIndexOf('$');
		String superClassName = getResolvedTypeX().getName().substring(0,lastDollar);
		UnresolvedType outer = UnresolvedType.forName(superClassName);
		return (ReferenceType) outer.resolve(getResolvedTypeX().getWorld());
	}
	
	private Signature.FormalTypeParameter[] getFormalTypeParametersFromOuterClass() {
		List typeParameters = new ArrayList();
		ReferenceType outer = getOuterClass();
		ReferenceTypeDelegate outerDelegate = outer.getDelegate();
		if (!(outerDelegate instanceof AsmDelegate)) {
			throw new IllegalStateException("How come we're in AsmObjectType resolving an inner type of something that is NOT a AsmObjectType??");
		}
		AsmDelegate outerObjectType = (AsmDelegate) outerDelegate;
		if (outerObjectType.isNested()) {
			Signature.FormalTypeParameter[] parentParams = outerObjectType.getFormalTypeParametersFromOuterClass();
			for (int i = 0; i < parentParams.length; i++) {
				typeParameters.add(parentParams[i]);
			}
		}
		  GenericSignatureParser parser = new GenericSignatureParser();
		  String sig = outerObjectType.getDeclaredGenericSignature();
		  if (sig!=null) {
		  Signature.ClassSignature outerSig = parser.parseAsClassSignature(sig);
		if (outerSig != null) {
			for (int i = 0; i < outerSig.formalTypeParameters .length; i++) {
				typeParameters.add(outerSig.formalTypeParameters[i]);
			}
		} 
	  }
		
		Signature.FormalTypeParameter[] ret = new Signature.FormalTypeParameter[typeParameters.size()];
		typeParameters.toArray(ret);
		return ret;
	}
	// ---

	public boolean isInterface() {
		return (classModifiers & Opcodes.ACC_INTERFACE)!=0;
	}

	public String getRetentionPolicy() {
		return retentionPolicy;
	}
	
	public boolean isAnnotationWithRuntimeRetention() {
		return isRuntimeRetention;
	}

	public boolean isAnnotation() {
		return (classModifiers & Opcodes.ACC_ANNOTATION)!=0;
	}
	
	public boolean isEnum() {
		return(classModifiers & Opcodes.ACC_ENUM)!=0;
	}
	
	public int getModifiers() {
		return classModifiers;
	}

	
	public ResolvedMember[] getDeclaredFields() {
		ensureSignatureUnpacked();
		if ((bitflag&FIELDSFIXEDUP)==0) {
			for (int i = 0; i < fields.length; i++) {
				((ResolvedMemberImpl)fields[i]).setDeclaringType(getResolvedTypeX());
			}
			bitflag|=FIELDSFIXEDUP;
		}
		return fields;
	}

	public ResolvedType[] getDeclaredInterfaces() {
		if (interfaceTypes == null) {
			if (interfaceNames==null || interfaceNames.length==0) {
				interfaceTypes = new ResolvedType[0];
			} else {
				interfaceTypes = new ResolvedType[interfaceNames.length];
				for (int i = 0; i < interfaceNames.length; i++) {
					interfaceTypes[i] = w.resolve(interfaceNames[i].replace('/','.'));
				}
			}
			interfaceNames=null;
			ensureSignatureUnpacked();
		}
		return interfaceTypes;
	}

	
	
	public ResolvedMember[] getDeclaredMethods() {
		ensureSignatureUnpacked();
		if ((bitflag&METHODSFIXEDUP)==0) {
			for (int i = 0; i < methods.length; i++) {
				((ResolvedMemberImpl)methods[i]).setDeclaringType(getResolvedTypeX());
			}
			bitflag|=METHODSFIXEDUP;
		}
		return methods;
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if ((bitflag & DISCOVERED_POINTCUTS)==0) {
			List pcts = new ArrayList();
			List forRemoval = new ArrayList();
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.PointcutDeclarationAttribute.AttributeName)) {
						PointcutDeclarationAttribute pointcut = (PointcutDeclarationAttribute)((AjASMAttribute)element).unpack(this);
						pcts.add(pointcut.reify());
						forRemoval.add(element);
					}
				}
			}
			pointcuts = (ResolvedPointcutDefinition[])pcts.toArray(new ResolvedPointcutDefinition[]{});
			attributes.removeAll(forRemoval);
			bitflag|=DISCOVERED_POINTCUTS;
		}
		return pointcuts;
	}

	public Collection getDeclares() {
		if ((bitflag & DISCOVERED_DECLARES)==0) {
			declares = new ArrayList();
			List forRemoval = new ArrayList();
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof AjASMAttribute) {
					AjASMAttribute element = (AjASMAttribute) o;
					if (element.type.equals(AjAttribute.DeclareAttribute.AttributeName)) {
						DeclareAttribute declare = (DeclareAttribute)((AjASMAttribute)element).unpack(this);
						declares.add(declare.getDeclare());
						forRemoval.add(element);
					}
				}
			}
			attributes.removeAll(forRemoval);
			bitflag|=DISCOVERED_DECLARES;//discoveredDeclares=true;
		}
		return declares;
	}

	public ResolvedType getSuperclass() {
		if ((bitflag&SUPERSET)==0) {
			if (superclassName == null) {
				// this type must be jlObject
				superclassType = null;
			} else {
				superclassType = w.resolve(superclassName.replace('/','.'));
			}
			ensureSignatureUnpacked();
			superclassName=null;
			bitflag|=SUPERSET;
		}
		return superclassType;
	}
	
	public PerClause getPerClause() {
		return perClause;
	}

	public boolean isAspect() {
		return isAspect;
	}
	
	World getWorld() { return w; }

	// ---
	// 14-Feb-06 the AsmDelegate is only for types that won't be 'woven', so they can't be a target 
	//           to have new annotations added
	public void addAnnotation(AnnotationX annotationX) { /* this method left blank on purpose*/ }

	public void ensureDelegateConsistent() {
		// doesnt need to do anything until methods like addAnnotation() are implemented (i.e. methods that 
		// modify the delegate such that it differs from the on-disk contents)
	}
	
}
