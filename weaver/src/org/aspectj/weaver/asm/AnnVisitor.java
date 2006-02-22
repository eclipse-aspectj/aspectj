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

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationAnnotationValue;
import org.aspectj.weaver.AnnotationNameValuePair;
import org.aspectj.weaver.AnnotationValue;
import org.aspectj.weaver.ArrayAnnotationValue;
import org.aspectj.weaver.ClassAnnotationValue;
import org.aspectj.weaver.EnumAnnotationValue;
import org.aspectj.weaver.SimpleAnnotationValue;
import org.aspectj.org.objectweb.asm.AnnotationVisitor;
import org.aspectj.org.objectweb.asm.Type;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Constructed with an annotation to 'fill in' with the values we encounter whilst visting it.
 */
class AnnVisitor implements AnnotationVisitor {
  private AnnotationAJ a;

  public AnnVisitor(AnnotationAJ annotationToPopulate) {
	  a = annotationToPopulate;
  }

  public void visitEnum(String name, String type, String value) {
	  AnnotationValue val = new EnumAnnotationValue(type,value);
	  a.addNameValuePair(new AnnotationNameValuePair(name,val));
  }
    
  public void visit(String name, Object value) {
	  AnnotationValue val = null;
	  if (value instanceof Integer) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_INT,value);
	  if (value instanceof Boolean) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_BOOLEAN,value);
	  if (value instanceof Long) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_LONG,value);
	  if (value instanceof Short) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_SHORT,value);
	  if (value instanceof Double) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_DOUBLE,value);
	  if (value instanceof Float) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_FLOAT,value);
	  if (value instanceof Character) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_CHAR,value);
	  if (value instanceof Byte) val = new SimpleAnnotationValue(AnnotationValue.PRIMITIVE_BYTE,value);
	  if (value instanceof String) val = new SimpleAnnotationValue(AnnotationValue.STRING,value); 

	  if (val==null && value instanceof Type) {
		 String classSignature = ((Type)value).getDescriptor();
		 val = new ClassAnnotationValue(classSignature);
	  }
	  if (val!=null) {
		  a.addNameValuePair(new AnnotationNameValuePair(name,val));
	  } else {
		  System.err.println("Choking on "+name+" = "+value);
		  throw new NotImplementedException();
	  }
  }

  public AnnotationVisitor visitAnnotation(String name, String desc) {
	  AnnotationAJ annotation = new AnnotationAJ(desc,a.isRuntimeVisible());
	  AnnotationValue val = new AnnotationAnnotationValue(annotation);
	  a.addNameValuePair(new AnnotationNameValuePair(name,val));
	  return new AnnVisitor(annotation);
  }

  public AnnotationVisitor visitArray(String name) {
	  ArrayAnnotationValue val = new ArrayAnnotationValue();
	  a.addNameValuePair(new AnnotationNameValuePair(name,val));
	  return new ArrayAnnotationVisitor(val,a.isRuntimeVisible());
  }
    
  public void visitEnd() {}
}