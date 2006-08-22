package org.aspectj.apache.bcel.generic;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.AccessFlags;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleAnnotations;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;

/** 
 * Template class for building up a java class. May be initialized with an
 * existing java class (file).
 *
 * @see JavaClass
 * @version $Id: ClassGen.java,v 1.8 2006/08/22 07:34:50 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 *
 * Upgraded, Andy Clement 9th Mar 06 - calculates SUID
 */
public class ClassGen extends AccessFlags implements Cloneable {
  /* Corresponds to the fields found in a JavaClass object.
   */
  private String   class_name, super_class_name, file_name;
  private int      class_name_index = -1, superclass_name_index = -1;
  private int      major = Constants.MAJOR_1_1, minor = Constants.MINOR_1_1;

  private ConstantPoolGen cp; // Template for building up constant pool

  // ArrayLists instead of arrays to gather fields, methods, etc.
  private ArrayList   field_vec     = new ArrayList();
  private ArrayList   method_vec    = new ArrayList();
  private ArrayList   attribute_vec = new ArrayList();
  private ArrayList   interface_vec = new ArrayList();
  private ArrayList   annotation_vec= new ArrayList();
  private boolean     unpackedAnnotations = false; 

  /** Convenience constructor to set up some important values initially.
   *
   * @param class_name fully qualified class name
   * @param super_class_name fully qualified superclass name
   * @param file_name source file name
   * @param access_flags access qualifiers
   * @param interfaces implemented interfaces
   * @param cp constant pool to use
   */
  public ClassGen(String class_name, String super_class_name, String file_name,
		  int access_flags, String[] interfaces, ConstantPoolGen cp) {
    this.class_name       = class_name;
    this.super_class_name = super_class_name;
    this.file_name        = file_name;
    this.access_flags     = access_flags;
    this.cp               = cp;

    // Put everything needed by default into the constant pool and the vectors
    if(file_name != null)
      addAttribute(new SourceFile(cp.addUtf8("SourceFile"), 2,
				  cp.addUtf8(file_name), cp.getConstantPool()));

    class_name_index      = cp.addClass(class_name);
    superclass_name_index = cp.addClass(super_class_name);

    if(interfaces != null)
      for(int i=0; i < interfaces.length; i++)
	addInterface(interfaces[i]);
  }

  /** Convenience constructor to set up some important values initially.
   *
   * @param class_name fully qualified class name
   * @param super_class_name fully qualified superclass name
   * @param file_name source file name
   * @param access_flags access qualifiers
   * @param interfaces implemented interfaces
   */
  public ClassGen(String class_name, String super_class_name, String file_name,
		  int access_flags, String[] interfaces) {
    this(class_name, super_class_name, file_name, access_flags, interfaces,
	 new ConstantPoolGen());
  }

  /**
   * Initialize with existing class.
   * @param clazz JavaClass object (e.g. read from file)
   */
  public ClassGen(JavaClass clazz) {
    class_name_index      = clazz.getClassNameIndex();
    superclass_name_index = clazz.getSuperclassNameIndex();
    class_name            = clazz.getClassName();
    super_class_name      = clazz.getSuperclassName();
    file_name             = clazz.getSourceFileName();
    access_flags          = clazz.getAccessFlags();
    cp                    = new ConstantPoolGen(clazz.getConstantPool());
    major                 = clazz.getMajor();
    minor                 = clazz.getMinor();

    Attribute[] attributes = clazz.getAttributes(); 
    // J5TODO: Could make unpacking lazy, done on first reference
    AnnotationGen[] annotations = unpackAnnotations(attributes);
    Method[]    methods    = clazz.getMethods();
    Field[]     fields     = clazz.getFields();
    String[]    interfaces = clazz.getInterfaceNames();
    
    for(int i=0; i < interfaces.length; i++)
      addInterface(interfaces[i]);

    for(int i=0; i < attributes.length; i++) {
      // Dont add attributes for annotations as those will have been unpacked
      if (annotations.length==0) {
        addAttribute(attributes[i]);
      } else if (!attributes[i].getName().equals("RuntimeVisibleAnnotations") &&
      	  !attributes[i].getName().equals("RuntimeInvisibleAnnotations"))  {
        addAttribute(attributes[i]);
        }
    }
    for(int i=0; i < annotations.length; i++)
        addAnnotation(annotations[i]);

    for(int i=0; i < methods.length; i++) {
    	Method m = methods[i];
        addMethod(m);
    }

    for(int i=0; i < fields.length; i++)
      addField(fields[i]);
  }
  
  /**
   * Look for attributes representing annotations and unpack them.
   */
  private AnnotationGen[] unpackAnnotations(Attribute[] attrs) {
  	List /*AnnotationGen*/ annotationGenObjs = new ArrayList();
  	for (int i = 0; i < attrs.length; i++) {
		Attribute attr = attrs[i];
		if (attr instanceof RuntimeVisibleAnnotations) {
			RuntimeVisibleAnnotations rva = (RuntimeVisibleAnnotations)attr;
			List annos = rva.getAnnotations();
			for (Iterator iter = annos.iterator(); iter.hasNext();) {
				Annotation a = (Annotation) iter.next();
				annotationGenObjs.add(new AnnotationGen(a,getConstantPool(),false));
			}
		} else if (attr instanceof RuntimeInvisibleAnnotations) {
			RuntimeInvisibleAnnotations ria = (RuntimeInvisibleAnnotations)attr;
			List annos = ria.getAnnotations();
			for (Iterator iter = annos.iterator(); iter.hasNext();) {
				Annotation a = (Annotation) iter.next();
				annotationGenObjs.add(new AnnotationGen(a,getConstantPool(),false));
			}
		}
	}
  	return (AnnotationGen[])annotationGenObjs.toArray(new AnnotationGen[]{});
  }

  /**
   * @return the (finally) built up Java class object.
   */
  public JavaClass getJavaClass() {
    int[]        interfaces = getInterfaces();
    Field[]      fields     = getFields();
    Method[]     methods    = getMethods();
    
    Attribute[] attributes = null;
    if (annotation_vec.size()==0) {
    	attributes = getAttributes();
    } else {
    	// TODO: Sometime later, trash any attributes called 'RuntimeVisibleAnnotations' or 'RuntimeInvisibleAnnotations'
        Attribute[] annAttributes  = Utility.getAnnotationAttributes(cp,annotation_vec);
        attributes = new Attribute[attribute_vec.size()+annAttributes.length];
        attribute_vec.toArray(attributes);
        System.arraycopy(annAttributes,0,attributes,attribute_vec.size(),annAttributes.length);       
    }

    // Must be last since the above calls may still add something to it
    ConstantPool cp         = this.cp.getFinalConstantPool();
    
    return new JavaClass(class_name_index, superclass_name_index,
			 file_name, major, minor, access_flags,
			 cp, interfaces, fields, methods, attributes);
  }

  /**
   * Add an interface to this class, i.e., this class has to implement it.
   * @param name interface to implement (fully qualified class name)
   */
  public void addInterface(String name) {
    interface_vec.add(name);
  }

  /**
   * Remove an interface from this class.
   * @param name interface to remove (fully qualified name)
   */
  public void removeInterface(String name) {
    interface_vec.remove(name);
  }

  /**
   * @return major version number of class file
   */
  public int  getMajor()      { return major; }

  /** Set major version number of class file, default value is 45 (JDK 1.1)
   * @param major major version number
   */
  public void setMajor(int major) {
    this.major = major;
  }    

  /** Set minor version number of class file, default value is 3 (JDK 1.1)
   * @param minor minor version number
   */
  public void setMinor(int minor) {
    this.minor = minor;
  }    

  /**
   * @return minor version number of class file
   */
  public int  getMinor()      { return minor; }

  /**
   * Add an attribute to this class.
   * @param a attribute to add
   */
  public void addAttribute(Attribute a)    { attribute_vec.add(a); }

  public void addAnnotation(AnnotationGen a) { annotation_vec.add(a); }
  /**
   * Add a method to this class.
   * @param m method to add
   */
  public void addMethod(Method m)          { method_vec.add(m); }

  /**
   * Convenience method.
   *
   * Add an empty constructor to this class that does nothing but calling super().
   * @param access rights for constructor
   */
  public void addEmptyConstructor(int access_flags) {
    InstructionList il = new InstructionList();
    il.append(InstructionConstants.THIS); // Push `this'
    il.append(new INVOKESPECIAL(cp.addMethodref(super_class_name,
						"<init>", "()V")));
    il.append(InstructionConstants.RETURN);

    MethodGen mg = new MethodGen(access_flags, Type.VOID, Type.NO_ARGS, null,
		       "<init>", class_name, il, cp);
    mg.setMaxStack(1);
    mg.setMaxLocals();
    addMethod(mg.getMethod());
  }

  /**
   * Add a field to this class.
   * @param f field to add
   */
  public void addField(Field f)            { field_vec.add(f); }

  public boolean containsField(Field f)    { return field_vec.contains(f); }
  
  /** @return field object with given name, or null
   */
  public Field containsField(String name) {
    for(Iterator e=field_vec.iterator(); e.hasNext(); ) {
      Field f = (Field)e.next();
      if(f.getName().equals(name))
	return f;
    }

    return null;
  }

  /** @return method object with given name and signature, or null
   */
  public Method containsMethod(String name, String signature) {
    for(Iterator e=method_vec.iterator(); e.hasNext();) {
      Method m = (Method)e.next();
      if(m.getName().equals(name) && m.getSignature().equals(signature))
	return m;
    }

    return null;
  }

  /**
   * Remove an attribute from this class.
   * @param a attribute to remove
   */
  public void removeAttribute(Attribute a) { attribute_vec.remove(a); }
  public void removeAnnotation(AnnotationGen a) {annotation_vec.remove(a);}

  /**
   * Remove a method from this class.
   * @param m method to remove
   */
  public void removeMethod(Method m)       { method_vec.remove(m); }

  /** Replace given method with new one. If the old one does not exist
   * add the new_ method to the class anyway.
   */
  public void replaceMethod(Method old, Method new_) {
    if(new_ == null)
      throw new ClassGenException("Replacement method must not be null");

    int i = method_vec.indexOf(old);

    if(i < 0)
      method_vec.add(new_);
    else
      method_vec.set(i, new_);
  }

  /** Replace given field with new one. If the old one does not exist
   * add the new_ field to the class anyway.
   */
  public void replaceField(Field old, Field new_) {
    if(new_ == null)
      throw new ClassGenException("Replacement method must not be null");

    int i = field_vec.indexOf(old);

    if(i < 0)
      field_vec.add(new_);
    else
      field_vec.set(i, new_);
  }

  /**
   * Remove a field to this class.
   * @param f field to remove
   */
  public void removeField(Field f)         { field_vec.remove(f); }

  public String getClassName()      { return class_name; }
  public String getSuperclassName() { return super_class_name; }
  public String getFileName()       { return file_name; }

  public void setClassName(String name) {
    class_name = name.replace('/', '.');
    class_name_index = cp.addClass(name);
  }

  public void setSuperclassName(String name) {
    super_class_name = name.replace('/', '.');
    superclass_name_index = cp.addClass(name);
  }

  public Method[] getMethods() {
    Method[] methods = new Method[method_vec.size()];
    method_vec.toArray(methods);
    return methods;
  }

  public void setMethods(Method[] methods) {
    method_vec.clear();
    for(int m=0; m<methods.length; m++)
      addMethod(methods[m]);
  }

  public void setMethodAt(Method method, int pos) {
    method_vec.set(pos, method);
  }

  public Method getMethodAt(int pos) {
    return (Method)method_vec.get(pos);
  }

  public String[] getInterfaceNames() {
    int      size = interface_vec.size();
    String[] interfaces = new String[size];

    interface_vec.toArray(interfaces);
    return interfaces;
  }

  public int[] getInterfaces() {
    int   size = interface_vec.size();
    int[] interfaces = new int[size];

    for(int i=0; i < size; i++)
      interfaces[i] = cp.addClass((String)interface_vec.get(i));

    return interfaces;
  }

  public Field[] getFields() {
    Field[] fields = new Field[field_vec.size()];
    field_vec.toArray(fields);
    return fields;
  }

  public Attribute[] getAttributes() {
    Attribute[] attributes = new Attribute[attribute_vec.size()];
    attribute_vec.toArray(attributes);
    return attributes;
  }
  
  // J5TODO: Should we make calling unpackAnnotations() lazy and put it in here?
  public AnnotationGen[] getAnnotations() {
  	AnnotationGen[] annotations = new AnnotationGen[annotation_vec.size()];
  	annotation_vec.toArray(annotations);
  	return annotations;
  }
  
  public ConstantPoolGen getConstantPool() { return cp; }
  public void setConstantPool(ConstantPoolGen constant_pool) {
    cp = constant_pool;
  }    

  public void setClassNameIndex(int class_name_index) {
    this.class_name_index = class_name_index;
    class_name = cp.getConstantPool().
      getConstantString(class_name_index, Constants.CONSTANT_Class).replace('/', '.');
  }

  public void setSuperclassNameIndex(int superclass_name_index) {
    this.superclass_name_index = superclass_name_index;
    super_class_name = cp.getConstantPool().
      getConstantString(superclass_name_index, Constants.CONSTANT_Class).replace('/', '.');
  }

  public int getSuperclassNameIndex() { return superclass_name_index; }    

  public int getClassNameIndex()   { return class_name_index; }

  private ArrayList observers;

  /** Add observer for this object.
   */
  public void addObserver(ClassObserver o) {
    if(observers == null)
      observers = new ArrayList();

    observers.add(o);
  }

  /** Remove observer for this object.
   */
  public void removeObserver(ClassObserver o) {
    if(observers != null)
      observers.remove(o);
  }

  /** Call notify() method on all observers. This method is not called
   * automatically whenever the state has changed, but has to be
   * called by the user after he has finished editing the object.
   */
  public void update() {
    if(observers != null)
      for(Iterator e = observers.iterator(); e.hasNext(); )
	((ClassObserver)e.next()).notify(this);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch(CloneNotSupportedException e) {
      System.err.println(e);
      return null;
    }
  }

  // J5SUPPORT:
  
  /**
   * Returns true if this class represents an annotation type 
   */
  public final boolean isAnnotation() {
  	return (access_flags & Constants.ACC_ANNOTATION) != 0;
  }
  
  /**
   * Returns true if this class represents an enum type
   */
  public final boolean isEnum() {
  	return (access_flags & Constants.ACC_ENUM) != 0;
  }
  
  /**
   * Calculate the SerialVersionUID for a class.
   */  
  public long getSUID() {
  	try {
        Field[] fields   = getFields();
        Method[] methods = getMethods();
        
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	DataOutputStream dos = new DataOutputStream(baos);
    	
    	// 1. classname
    	dos.writeUTF(getClassName());
    	
    	// 2. classmodifiers: ACC_PUBLIC, ACC_FINAL, ACC_INTERFACE, and ACC_ABSTRACT
    	int classmods = 0; 
    	classmods|=(isPublic()?Constants.ACC_PUBLIC:0); 
    	classmods|=(isFinal()?Constants.ACC_FINAL:0);
    	classmods|=(isInterface()?Constants.ACC_INTERFACE:0);
    	
    	if (isAbstract()) {
    		// if an interface then abstract is only set if it has methods
    		if (isInterface()) {
    			if (methods.length>0) classmods|=Constants.ACC_ABSTRACT;
    		} else {
    			classmods|=Constants.ACC_ABSTRACT;
    		}
    	}
    	
    	dos.writeInt(classmods);
    	
    	// 3. ordered list of interfaces
    	List list = new ArrayList();
        String[] names = getInterfaceNames();
        if (names!=null) {
        	Arrays.sort(names);
        	for (int i = 0; i < names.length; i++) dos.writeUTF(names[i]);
        }
    
        // 4. ordered list of fields (ignoring private static and private transient fields):
        //  (relevant modifiers are ACC_PUBLIC, ACC_PRIVATE, 
        //   ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_VOLATILE, 
        //   ACC_TRANSIENT)
        list.clear();
        for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!(field.isPrivate() && field.isStatic()) && 
				!(field.isPrivate() && field.isTransient())) list.add(field);
		}
        Collections.sort(list,new FieldComparator());
        int relevantFlags = Constants.ACC_PUBLIC | Constants.ACC_PRIVATE | Constants.ACC_PROTECTED |
        					Constants.ACC_STATIC | Constants.ACC_FINAL | Constants.ACC_VOLATILE | Constants.ACC_TRANSIENT;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
			Field f = (Field) iter.next();
			dos.writeUTF(f.getName());
	    	dos.writeInt(relevantFlags&f.getModifiers());
	    	dos.writeUTF(f.getType().getSignature());
		}

        // some up front method processing: discover clinit, init and ordinary methods of interest:
        list.clear(); // now used for methods
        List ctors = new ArrayList();
        boolean hasClinit = false;
        for (int i = 0; i < methods.length; i++) {
        	Method m = methods[i];
        	boolean couldBeInitializer = m.getName().charAt(0)=='<';
        	if (couldBeInitializer && m.getName().equals("<clinit>")) {
        		hasClinit=true;
        	} else if (couldBeInitializer && m.getName().equals("<init>")) {
        		if (!m.isPrivate()) ctors.add(m);
        	} else {
        	    if (!m.isPrivate()) list.add(m);
        	}
		}
        Collections.sort(ctors, new ConstructorComparator());
        Collections.sort(list, new MethodComparator());
        
        
		//      5. If a class initializer exists, write out the following:
		//            1. The name of the method, <clinit>.
		//            2. The modifier of the method, java.lang.reflect.Modifier.STATIC, written as a 32-bit integer.
		//            3. The descriptor of the method, ()V. 
        if (hasClinit) {
        	dos.writeUTF("<clinit>");
        	dos.writeInt(Modifier.STATIC);
        	dos.writeUTF("()V");
        }
        
        // for methods and constructors: 
        //               ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, 
        //               ACC_NATIVE, ACC_ABSTRACT and ACC_STRICT
        relevantFlags = 	
        	Constants.ACC_PUBLIC | Constants.ACC_PRIVATE | Constants.ACC_PROTECTED |
        	Constants.ACC_STATIC | Constants.ACC_FINAL | Constants.ACC_SYNCHRONIZED | 
        	Constants.ACC_NATIVE | Constants.ACC_ABSTRACT | Constants.ACC_STRICT;
        
		// 6. sorted non-private constructors
        for (Iterator iter = ctors.iterator(); iter.hasNext();) {
			Method m = (Method) iter.next();
			dos.writeUTF(m.getName()); // <init>
			dos.writeInt(relevantFlags & m.getModifiers());
			dos.writeUTF(m.getSignature().replace('/','.'));
		}

        // 7. sorted non-private methods 
        for (Iterator iter = list.iterator(); iter.hasNext();) {
			Method m = (Method) iter.next();
			dos.writeUTF(m.getName());
			dos.writeInt(relevantFlags & m.getModifiers());
			dos.writeUTF(m.getSignature().replace('/','.'));
		}
        dos.flush();
        dos.close();
        byte[] bs = baos.toByteArray();
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte[] result = md.digest(bs);
                
        long suid = 0L;
        int pos = result.length>8?7:result.length-1; // use the bytes we have
        while (pos>=0) {
        	suid = suid<<8 | ((long)result[pos--]&0xff);
        }

        // if it was definetly 8 everytime...
        //	    long suid = ((long)(sha[0]&0xff) | (long)(sha[1]&0xff) << 8  |
		//	                 (long)(sha[2]&0xff) << 16 | (long)(sha[3]&0xff) << 24 |
		//	                 (long)(sha[4]&0xff) << 32 | (long)(sha[5]&0xff) << 40 |
		//	                 (long)(sha[6]&0xff) << 48 | (long)(sha[7]&0xff) << 56);
	    return suid;
  	} catch (Exception e) {
  		System.err.println("Unable to calculate suid for "+getClassName());
  		e.printStackTrace();
  		throw new RuntimeException("Unable to calculate suid for "+getClassName()+": "+e.toString());
  	}
  }
  
  private static class FieldComparator implements Comparator {
		public int compare(Object arg0, Object arg1) { 
			return ((Field)arg0).getName().compareTo(((Field)arg1).getName());
		}
  }
  private static class ConstructorComparator implements Comparator {
		public int compare(Object arg0, Object arg1) { 
			// can ignore the name...
			return ((Method)arg0).getSignature().compareTo(((Method)arg1).getSignature());
		}
  }
  private static class MethodComparator implements Comparator {
		public int compare(Object arg0, Object arg1) { 
			Method m1 = (Method)arg0;
			Method m2 = (Method)arg1;
			int result = m1.getName().compareTo(m2.getName());
			if (result!=0) return result;
			return m1.getSignature().compareTo(m2.getSignature());
		}
  }
}
