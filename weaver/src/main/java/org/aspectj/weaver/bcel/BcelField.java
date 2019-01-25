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

import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignatureParser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;

/**
 * An AspectJ Field object that is backed by a Bcel Field object.
 * 
 * @author PARC
 * @author Andy Clement
 */
final class BcelField extends ResolvedMemberImpl {

	public static int AccSynthetic = 0x1000;

	private Field field;
	private boolean isAjSynthetic;
	private boolean isSynthetic = false;
	private AnnotationAJ[] annotations;
	private final World world;
	private final BcelObjectType bcelObjectType;
	private UnresolvedType genericFieldType = null;
	private boolean unpackedGenericSignature = false;
	private boolean annotationsOnFieldObjectAreOutOfDate = false;

	BcelField(BcelObjectType declaringType, Field field) {
		super(FIELD, declaringType.getResolvedTypeX(), field.getModifiers(), field.getName(), field.getSignature());
		this.field = field;
		this.world = declaringType.getResolvedTypeX().getWorld();
		this.bcelObjectType = declaringType;
		unpackAttributes(world);
		checkedExceptions = UnresolvedType.NONE;
	}

	/**
	 * Constructs an instance that wrappers a Field object, but where we do not (yet) have a BcelObjectType - usually because the
	 * containing type (and this field) are being constructed at runtime (so there is no .class file to retrieve).
	 */
	BcelField(String declaringTypeName, Field field, World world) {
		super(FIELD, UnresolvedType.forName(declaringTypeName), field.getModifiers(), field.getName(), field.getSignature());
		this.field = field;
		this.world = world;
		this.bcelObjectType = null;
		unpackAttributes(world);
		checkedExceptions = UnresolvedType.NONE;
	}

	private void unpackAttributes(World world) {
		Attribute[] attrs = field.getAttributes();
		if (attrs != null && attrs.length > 0) {
			ISourceContext sourceContext = getSourceContext(world);
			List<AjAttribute> as = Utility.readAjAttributes(getDeclaringType().getClassName(), attrs, sourceContext, world,
					(bcelObjectType != null ? bcelObjectType.getWeaverVersionAttribute() : WeaverVersionInfo.CURRENT),
					new BcelConstantPoolReader(field.getConstantPool()));
			as.addAll(AtAjAttributes.readAj5FieldAttributes(field, this, world.resolve(getDeclaringType()), sourceContext,
					world.getMessageHandler()));

			// FIXME this code has no effect!!!??? it is set to false immediately after the block
			// for (AjAttribute a : as) {
			// if (a instanceof AjAttribute.AjSynthetic) {
			// isAjSynthetic = true;
			// } else {
			// throw new BCException("weird field attribute " + a);
			// }
			// }
		}
		isAjSynthetic = false;

		for (int i = attrs.length - 1; i >= 0; i--) {
			if (attrs[i] instanceof Synthetic) {
				isSynthetic = true;
			}
		}
		// in 1.5, synthetic is a modifier, not an attribute
		if ((field.getModifiers() & AccSynthetic) != 0) {
			isSynthetic = true;
		}

	}

	@Override
	public boolean isAjSynthetic() {
		return isAjSynthetic;
	}

	@Override
	public boolean isSynthetic() {
		return isSynthetic;
	}

	@Override
	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationTypesRetrieved();
		for (ResolvedType aType : annotationTypes) {
			if (aType.equals(ofType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		ensureAnnotationTypesRetrieved();
		return annotationTypes;
	}

	@Override
	public AnnotationAJ[] getAnnotations() {
		ensureAnnotationTypesRetrieved();
		return annotations;
	}

	@Override
	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		ensureAnnotationTypesRetrieved();
		for (AnnotationAJ annotation : annotations) {
			if (annotation.getTypeName().equals(ofType.getName())) {
				return annotation;
			}
		}
		return null;
	}

	private void ensureAnnotationTypesRetrieved() {
		if (annotationTypes == null) {
			AnnotationGen annos[] = field.getAnnotations();
			if (annos.length == 0) {
				annotationTypes = ResolvedType.EMPTY_ARRAY;
				annotations = AnnotationAJ.EMPTY_ARRAY;
			} else {
				int annosCount = annos.length;
				annotationTypes = new ResolvedType[annosCount];
				annotations = new AnnotationAJ[annosCount];
				for (int i = 0; i < annosCount; i++) {
					AnnotationGen anno = annos[i];
					annotations[i] = new BcelAnnotation(anno, world);
					annotationTypes[i] = annotations[i].getType();
				}
			}
		}
	}

	@Override
	public void addAnnotation(AnnotationAJ annotation) {
		ensureAnnotationTypesRetrieved();
		int len = annotations.length;
		AnnotationAJ[] ret = new AnnotationAJ[len + 1];
		System.arraycopy(annotations, 0, ret, 0, len);
		ret[len] = annotation;
		annotations = ret;

		ResolvedType[] newAnnotationTypes = new ResolvedType[len + 1];
		System.arraycopy(annotationTypes, 0, newAnnotationTypes, 0, len);
		newAnnotationTypes[len] = annotation.getType();
		annotationTypes = newAnnotationTypes;

		annotationsOnFieldObjectAreOutOfDate = true;
	}

	public void removeAnnotation(AnnotationAJ annotation) {
		ensureAnnotationTypesRetrieved();

		int len = annotations.length;
		AnnotationAJ[] ret = new AnnotationAJ[len - 1];
		int p = 0;
		for (AnnotationAJ anno : annotations) {
			if (!anno.getType().equals(annotation.getType())) {
				ret[p++] = anno;
			}
		}
		annotations = ret;

		ResolvedType[] newAnnotationTypes = new ResolvedType[len - 1];
		p = 0;
		for (ResolvedType anno : annotationTypes) {
			if (!anno.equals(annotation.getType())) {
				newAnnotationTypes[p++] = anno;
			}
		}
		annotationTypes = newAnnotationTypes;

		annotationsOnFieldObjectAreOutOfDate = true;
	}

	/**
	 * Unpack the generic signature attribute if there is one and we haven't already done so, then find the true field type of this
	 * field (eg. List<String>).
	 */
	@Override
	public UnresolvedType getGenericReturnType() {
		unpackGenericSignature();
		return genericFieldType;
	}

	public Field getFieldAsIs() {
		return field;
	}

	public Field getField(ConstantPool cpool) {
		if (!annotationsOnFieldObjectAreOutOfDate) {
			return field;
		}
		FieldGen newFieldGen = new FieldGen(field, cpool);
		newFieldGen.removeAnnotations();
		// List<AnnotationGen> alreadyHas = fg.getAnnotations();
		// if (annotations != null) {
		// fg.removeAnnotations();
		for (AnnotationAJ annotation : annotations) {
			newFieldGen.addAnnotation(new AnnotationGen(((BcelAnnotation) annotation).getBcelAnnotation(), cpool, true));
		}
		// for (int i = 0; i < annotations.length; i++) {
		// AnnotationAJ array_element = annotations[i];
		// boolean alreadyHasIt = false;
		// for (AnnotationGen gen : alreadyHas) {
		// if (gen.getTypeName().equals(array_element.getTypeName())) {
		// alreadyHasIt = true;
		// break;
		// }
		// }
		// if (!alreadyHasIt) {
		// fg.addAnnotation(new AnnotationGen(((BcelAnnotation) array_element).getBcelAnnotation(), cpg, true));
		// // }
		// // }
		// }
		field = newFieldGen.getField();
		annotationsOnFieldObjectAreOutOfDate = false; // we are now correct again
		return field;
	}

	private void unpackGenericSignature() {
		if (unpackedGenericSignature) {
			return;
		}
		if (!world.isInJava5Mode()) {
			this.genericFieldType = getReturnType();
			return;
		}
		unpackedGenericSignature = true;
		String gSig = field.getGenericSignature();
		if (gSig != null) {
			// get from generic
			GenericSignature.FieldTypeSignature fts = new GenericSignatureParser().parseAsFieldSignature(gSig);
			GenericSignature.ClassSignature genericTypeSig = bcelObjectType.getGenericClassTypeSignature();

			GenericSignature.FormalTypeParameter[] parentFormals = bcelObjectType.getAllFormals();
			GenericSignature.FormalTypeParameter[] typeVars = ((genericTypeSig == null) ? new GenericSignature.FormalTypeParameter[0]
					: genericTypeSig.formalTypeParameters);
			GenericSignature.FormalTypeParameter[] formals = new GenericSignature.FormalTypeParameter[parentFormals.length
					+ typeVars.length];
			// put method formal in front of type formals for overriding in
			// lookup
			System.arraycopy(typeVars, 0, formals, 0, typeVars.length);
			System.arraycopy(parentFormals, 0, formals, typeVars.length, parentFormals.length);

			try {
				genericFieldType = BcelGenericSignatureToTypeXConverter.fieldTypeSignature2TypeX(fts, formals, world);
			} catch (GenericSignatureFormatException e) {
				// development bug, fail fast with good info
				throw new IllegalStateException("While determing the generic field type of " + this.toString()
						+ " with generic signature " + gSig + " the following error was detected: " + e.getMessage());
			}
		} else {
			genericFieldType = getReturnType();
		}
	}

	@Override
	public void evictWeavingState() {
		if (field != null) {
			unpackGenericSignature();
			unpackAttributes(world);
			ensureAnnotationTypesRetrieved();
			// this.sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
			field = null;
		}
	}
}