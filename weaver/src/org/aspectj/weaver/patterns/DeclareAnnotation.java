/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.VersionedDataInputStream;

public class DeclareAnnotation extends Declare {
		
	private String kind;
	private TypePattern typePattern;
	private SignaturePattern sigPattern;
	private String annotationMethod = "unknown";
	private String annotationString = "@<annotation>";

	public DeclareAnnotation(String kind, TypePattern typePattern) {
		this.typePattern = typePattern;
		this.kind = kind;
	}
	
	public DeclareAnnotation(String kind, SignaturePattern sigPattern) {
		this.sigPattern = sigPattern;
		this.kind = kind;
	}
	
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
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Declare#resolve(org.aspectj.weaver.patterns.IScope)
	 */
	public void resolve(IScope scope) {
		if (typePattern != null) typePattern.resolve(scope.getWorld());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Declare#isAdviceLike()
	 */
	public boolean isAdviceLike() {
		return false;
	}

	public void setAnnotationString(String as) {
		this.annotationString = as;
	}
	
	public void setAnnotationMethod(String methName){
		this.annotationMethod = methName;
	}
 	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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
		s.writeUTF(kind);
		s.writeUTF(annotationString);
		s.writeUTF(annotationMethod);
		if (typePattern != null) typePattern.write(s);
		if (sigPattern != null) sigPattern.write(s);
		writeLocation(s);	
	}

	/**
	 * @param s
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public static Declare read(VersionedDataInputStream s,
			ISourceContext context) throws IOException {
		DeclareAnnotation ret = null;
		String kind = s.readUTF();
		String annotationString = s.readUTF();
		String annotationMethod = s.readUTF();
		TypePattern tp = null;
		SignaturePattern sp = null;
		if (kind.equals("type")) {
			tp = TypePattern.read(s,context);
			ret = new DeclareAnnotation(kind,tp);
		} else {
			sp = SignaturePattern.read(s,context);
			ret = new DeclareAnnotation(kind,sp);
		}
		ret.setAnnotationString(annotationString);
		ret.setAnnotationMethod(annotationMethod);
		ret.readLocation(context,s);
		return ret;
	}
}
