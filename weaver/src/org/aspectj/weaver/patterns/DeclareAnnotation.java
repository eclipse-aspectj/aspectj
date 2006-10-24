/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer     initial implementation 
 *      Andy Clement     got it working
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

public class DeclareAnnotation extends Declare {
		
	public static final Kind AT_TYPE        = new Kind(1,"type");
	public static final Kind AT_FIELD       = new Kind(2,"field");
	public static final Kind AT_METHOD      = new Kind(3,"method");
	public static final Kind AT_CONSTRUCTOR = new Kind(4,"constructor");
	
	private Kind kind;
	private TypePattern typePattern;     // for declare @type
	private SignaturePattern sigPattern; // for declare @field,@method,@constructor
	private String annotationMethod = "unknown";
	private String annotationString = "@<annotation>";
	private ResolvedType containingAspect;
	private AnnotationX   annotation;
	
    /**
     * Captures type of declare annotation (method/type/field/constructor)
     */
	public static class Kind {
		private final int id;
		private String s;
		
		private Kind(int n,String name) {
			id = n;
			s = name;
		}
		
		public int hashCode() {
	      return (19 + 37*id);
		}

	  	public boolean equals(Object obj) {
	  		if (!(obj instanceof Kind)) return false;
	  		Kind other = (Kind) obj;
	  		return other.id == id;
	  	}
	  	
	  	public String toString() {
	  		return "at_"+s;
	  	}
	}

	

	public DeclareAnnotation(Kind kind, TypePattern typePattern) {
		this.typePattern = typePattern;
		this.kind = kind;
	}
	
    /**
     * Returns the string, useful before the real annotation has been resolved
     */
	public String getAnnotationString() { return annotationString;}
	
	public DeclareAnnotation(Kind kind, SignaturePattern sigPattern) {
		this.sigPattern = sigPattern;
		this.kind = kind;
	}
	
	public boolean isExactPattern() {
		return typePattern instanceof ExactTypePattern;
	}
	
	public String getAnnotationMethod() { return annotationMethod;}
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append("declare @");
		ret.append(kind);
		ret.append(" : ");
		ret.append(typePattern != null ? typePattern.toString() : sigPattern.toString());
		ret.append(" : ");
		ret.append(annotationString);
		return ret.toString();
	}
	
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this,data);
	}
	
	public void resolve(IScope scope) {
		if (!scope.getWorld().isInJava5Mode()) {
			String msg = null;
			if (kind == AT_TYPE) { msg = WeaverMessages.DECLARE_ATTYPE_ONLY_SUPPORTED_AT_JAVA5_LEVEL; }
			else if (kind == AT_METHOD) { msg = WeaverMessages.DECLARE_ATMETHOD_ONLY_SUPPORTED_AT_JAVA5_LEVEL;}
			else if (kind == AT_FIELD) { msg = WeaverMessages.DECLARE_ATFIELD_ONLY_SUPPORTED_AT_JAVA5_LEVEL;}
			else if (kind == AT_CONSTRUCTOR) { msg = WeaverMessages.DECLARE_ATCONS_ONLY_SUPPORTED_AT_JAVA5_LEVEL;}
			scope.message(MessageUtil.error(WeaverMessages.format(msg),
					getSourceLocation()));
			return;
		}
		if (typePattern != null) {
			typePattern = typePattern.resolveBindings(scope,Bindings.NONE,false,false);
		}
		if (sigPattern != null) {
			sigPattern = sigPattern.resolveBindings(scope,Bindings.NONE);
		}
		this.containingAspect = scope.getEnclosingType();
	}

	public Declare parameterizeWith(Map typeVariableBindingMap,World w) {
		DeclareAnnotation ret;
		if (this.kind == AT_TYPE) {
			ret = new DeclareAnnotation(kind,this.typePattern.parameterizeWith(typeVariableBindingMap,w));
		} else {
			ret = new DeclareAnnotation(kind, this.sigPattern.parameterizeWith(typeVariableBindingMap,w));
		}
		ret.annotationMethod = this.annotationMethod;
		ret.annotationString = this.annotationString;
		ret.containingAspect = this.containingAspect;
		ret.annotation = this.annotation;
		ret.copyLocationFrom(this);
		return ret;
	}
	
	public boolean isAdviceLike() {
		return false;
	}

	public void setAnnotationString(String as) {
		this.annotationString = as;
	}
	
	public void setAnnotationMethod(String methName){
		this.annotationMethod = methName;
	}
 	
	
	
	public boolean equals(Object obj) {
		if (!(obj instanceof DeclareAnnotation)) return false;
		DeclareAnnotation other = (DeclareAnnotation) obj;
		if (!this.kind.equals(other.kind)) return false;
		if (!this.annotationString.equals(other.annotationString)) return false;
		if (!this.annotationMethod.equals(other.annotationMethod)) return false;
		if (this.typePattern != null) {
			if (!typePattern.equals(other.typePattern)) return false;
		}
		if (this.sigPattern != null) {
			if (!sigPattern.equals(other.sigPattern)) return false;
		}
		return true;
	}
	
	public int hashCode() {
      int result = 19;
      result = 37*result + kind.hashCode();
      result = 37*result + annotationString.hashCode();
      result = 37*result + annotationMethod.hashCode();
      if (typePattern != null) result = 37*result + typePattern.hashCode();
      if (sigPattern != null) result = 37*result + sigPattern.hashCode();
      return result;	
    }
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Declare.ANNOTATION);
		s.writeInt(kind.id);
		s.writeUTF(annotationString);
		s.writeUTF(annotationMethod);
		if (typePattern != null) typePattern.write(s);
		if (sigPattern != null) sigPattern.write(s);
		writeLocation(s);	
	}

	public static Declare read(VersionedDataInputStream s,
			ISourceContext context) throws IOException {
		DeclareAnnotation ret = null;
		int kind = s.readInt();
		String annotationString = s.readUTF();
		String annotationMethod = s.readUTF();
		TypePattern tp = null;
		SignaturePattern sp = null;
		switch (kind) {
		  case 1:
			tp = TypePattern.read(s,context);
			ret = new DeclareAnnotation(AT_TYPE,tp);
			break;
		  case 2:
		  	sp = SignaturePattern.read(s,context);
			ret = new DeclareAnnotation(AT_FIELD,sp);
			break;
		  case 3:
		  	sp = SignaturePattern.read(s,context);
			ret = new DeclareAnnotation(AT_METHOD,sp);
			break;
		  case 4:
		  	sp = SignaturePattern.read(s,context);
			ret = new DeclareAnnotation(AT_CONSTRUCTOR,sp);
			break;
			
		}
//		if (kind==AT_TYPE.id) {
//			tp = TypePattern.read(s,context);
//			ret = new DeclareAnnotation(AT_TYPE,tp);
//		} else {
//			sp = SignaturePattern.read(s,context);
//			ret = new DeclareAnnotation(kind,sp);
//		}
		ret.setAnnotationString(annotationString);
		ret.setAnnotationMethod(annotationMethod);
		ret.readLocation(context,s);
		return ret;
	}


//	public boolean getAnnotationIfMatches(ResolvedType onType) {
//		return (match(onType));
//	}
	

    /**
     * For @constructor, @method, @field
     */
	public boolean matches(ResolvedMember rm,World world) {
		return sigPattern.matches(rm,world,false);
	}
	
    /**
     * For @type
     */
	public boolean matches(ResolvedType typeX) {
		if (!typePattern.matchesStatically(typeX)) return false;
		if (typeX.getWorld().getLint().typeNotExposedToWeaver.isEnabled() &&
				!typeX.isExposedToWeaver())
		{
			typeX.getWorld().getLint().typeNotExposedToWeaver.signal(typeX.getName(), getSourceLocation());
		}
		return true; 
	}

	public void setAspect(ResolvedType typeX) {
		containingAspect = typeX;
	}

	public UnresolvedType getAspect() {
		return containingAspect;
	}

	public void copyAnnotationTo(ResolvedType onType) {
		ensureAnnotationDiscovered();
		if (!onType.hasAnnotation(annotation.getSignature())) {
			onType.addAnnotation(annotation);			
		}
	}
	
	public AnnotationX getAnnotationX() {
		ensureAnnotationDiscovered();
		return annotation;
	}
	

	/**
	 * The annotation specified in the declare @type is stored against
	 * a simple method of the form "ajc$declare_<NN>", this method
	 * finds that method and retrieves the annotation
	 */
	private void ensureAnnotationDiscovered() {
		if (annotation!=null) return;
		for (Iterator iter = containingAspect.getMethods(); iter.hasNext();) {
			ResolvedMember member = (ResolvedMember) iter.next();
			if (member.getName().equals(annotationMethod)) {
				annotation = member.getAnnotations()[0];
			}			
		}
	}

	public TypePattern getTypePattern() {
		return typePattern;
	}
	
	public SignaturePattern getSignaturePattern() {
		return sigPattern;
	}
	
	public boolean isStarredAnnotationPattern() {
		if (typePattern!=null) return typePattern.isStarAnnotation();
		if (sigPattern!=null)  return sigPattern.isStarAnnotation();
		throw new RuntimeException("Impossible! what kind of deca is this: "+this);
	}


	public Kind getKind() {
		return kind;
	}

	public boolean isDeclareAtConstuctor() {
		return kind.equals(AT_CONSTRUCTOR);
	}
	public boolean isDeclareAtMethod() {
		return kind.equals(AT_METHOD);
	}
	public boolean isDeclareAtType() {
		return kind.equals(AT_TYPE);
	}
	public boolean isDeclareAtField() {
		return kind.equals(AT_FIELD);
	}

    /**
     * @return UnresolvedType for the annotation
     */
	public UnresolvedType getAnnotationTypeX() {
	   ensureAnnotationDiscovered();
	   return this.annotation.getSignature(); 
	}

	/**
	 * @return true if the annotation specified is allowed on a field
	 */
	public boolean isAnnotationAllowedOnField() {
	    ensureAnnotationDiscovered();
		return annotation.allowedOnField();
	}

	public String getPatternAsString() {
	   if (sigPattern!=null) return sigPattern.toString();
	   if (typePattern!=null) return typePattern.toString();
	   return "DONT KNOW";
	}

	/**
	 * Return true if this declare annotation could ever match something 
	 * in the specified type - only really able to make intelligent
	 * decision if a type was specified in the sig/type pattern
	 * signature. 
	 */
	public boolean couldEverMatch(ResolvedType type) {
	    // Haven't implemented variant for typePattern (doesn't seem worth it!)
	    // BUGWARNING This test might not be sufficient for funny cases relating
		// to interfaces and the use of '+' - but it seems really important to
		// do something here so we don't iterate over all fields and all methods
		// in all types exposed to the weaver!  So look out for bugs here and
		// we can update the test as appropriate.
	    if (sigPattern!=null)
	      return sigPattern.getDeclaringType().matches(type,TypePattern.STATIC).maybeTrue();
		return true;
	}
	
	/**
	 * Provide a name suffix so that we can tell the different declare annotations
	 * forms apart in the AjProblemReporter
	 */
	public String getNameSuffix() {
		  return getKind().toString();
	}
}
