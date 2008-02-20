/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC                 initial implementation 
 *     Andy Clement  6Jul05 generics - signature attribute
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.generic.BasicType;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.PUSH;
import org.aspectj.apache.bcel.generic.RETURN;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.CollectionUtil;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;


/**
 * Lazy lazy lazy.
 * We don't unpack the underlying class unless necessary.  Things
 * like new methods and annotations accumulate in here until they
 * must be written out, don't add them to the underlying MethodGen!
 * Things are slightly different if this represents an Aspect.
 */
public final class LazyClassGen {
	
	private static final int ACC_SYNTHETIC    = 0x1000;
	
	int highestLineNumber = 0; // ---- JSR 45 info
	
	private SortedMap /* <String, InlinedSourceFileInfo> */ inlinedFiles = new TreeMap();
	
	private  boolean regenerateGenericSignatureAttribute = false;
	
	private BcelObjectType myType; // XXX is not set for types we create
	private ClassGen myGen;
	private ConstantPoolGen constantPoolGen;
	private World world;
    private String packageName = null;

    private List /*LazyMethodGen*/ methodGens  = new ArrayList();
    private List /*LazyClassGen*/  classGens   = new ArrayList();
    private List /*AnnotationGen*/ annotations = new ArrayList();
    private int childCounter = 0;
    
    private InstructionFactory fact;
    
	private boolean isSerializable = false;
	private boolean hasSerialVersionUIDField = false;
	private boolean serialVersionUIDRequiresInitialization = false;
	private long    calculatedSerialVersionUID;
	private boolean hasClinit = false;
	
	// ---
	
	static class InlinedSourceFileInfo {
		int highestLineNumber;
		int offset;	// calculated
		
		InlinedSourceFileInfo(int highestLineNumber) {
			this.highestLineNumber = highestLineNumber;
		}
	}	
	
	void addInlinedSourceFileInfo(String fullpath, int highestLineNumber) {
		Object o = inlinedFiles.get(fullpath);
		if (o != null) {
			InlinedSourceFileInfo info = (InlinedSourceFileInfo) o;
			if (info.highestLineNumber < highestLineNumber) {
				info.highestLineNumber = highestLineNumber;
			}
		} else {
			inlinedFiles.put(fullpath, new InlinedSourceFileInfo(highestLineNumber));
		}
	}
	
	void calculateSourceDebugExtensionOffsets() {
		int i = roundUpToHundreds(highestLineNumber);
		for (Iterator iter = inlinedFiles.values().iterator(); iter.hasNext();) {
			InlinedSourceFileInfo element = (InlinedSourceFileInfo) iter.next();
			element.offset = i;
			i = roundUpToHundreds(i + element.highestLineNumber);
		}
	}

	private static int roundUpToHundreds(int i) {
		return ((i / 100) + 1) * 100;
	}
	
	int getSourceDebugExtensionOffset(String fullpath) {
		return ((InlinedSourceFileInfo) inlinedFiles.get(fullpath)).offset;
	}
	
	private Unknown getSourceDebugExtensionAttribute() {
		int nameIndex = constantPoolGen.addUtf8("SourceDebugExtension");
		String data = getSourceDebugExtensionString();
		//System.err.println(data);
		byte[] bytes = Utility.stringToUTF(data);
		int length = bytes.length;

		return new Unknown(nameIndex, length, bytes, constantPoolGen.getConstantPool());		
	}	

//	private LazyClassGen() {}
//	public static void main(String[] args) {
//		LazyClassGen m = new LazyClassGen();
//		m.highestLineNumber = 37;
//		m.inlinedFiles.put("boo/baz/foo.java", new InlinedSourceFileInfo( 83));
//		m.inlinedFiles.put("boo/barz/foo.java", new InlinedSourceFileInfo(292));
//		m.inlinedFiles.put("boo/baz/moo.java", new InlinedSourceFileInfo(128));	
//		m.calculateSourceDebugExtensionOffsets();
//		System.err.println(m.getSourceDebugExtensionString());			
//	}
	
	// For the entire pathname, we're using package names.  This is probably wrong.
	private String getSourceDebugExtensionString() {
		StringBuffer out = new StringBuffer();
		String myFileName = getFileName();
		// header section
		out.append("SMAP\n");
		out.append(myFileName);
		out.append("\nAspectJ\n");
		// stratum section
		out.append("*S AspectJ\n");
		// file section
		out.append("*F\n");
		out.append("1 ");
		out.append(myFileName);
		out.append("\n");
		int i = 2;
		for (Iterator iter = inlinedFiles.keySet().iterator(); iter.hasNext();) {
			String element = (String) iter.next(); 
			int ii = element.lastIndexOf('/');
			if (ii == -1) {
				out.append(i++); out.append(' '); 
				out.append(element); out.append('\n');				
			} else {
				out.append("+ "); out.append(i++); out.append(' '); 
				out.append(element.substring(ii+1)); out.append('\n');
				out.append(element); out.append('\n');							
			}
		}
		// emit line section
		out.append("*L\n");
		out.append("1#1,");
		out.append(highestLineNumber);
		out.append(":1,1\n");
		i = 2;	
		for (Iterator iter = inlinedFiles.values().iterator(); iter.hasNext();) {
			InlinedSourceFileInfo element = (InlinedSourceFileInfo) iter.next();
			out.append("1#"); 
			out.append(i++); out.append(',');
			out.append(element.highestLineNumber); out.append(":");
			out.append(element.offset + 1); out.append(",1\n");
		}	
		// end section
		out.append("*E\n");
		// and finish up...
		return out.toString();
	}
	
	// ---- end JSR45-related stuff
	
    /** Emit disassembled class and newline to out */
    public static void disassemble(String path, String name, PrintStream out) 
        throws IOException {
        if (null == out) {
            return;
        }
        //out.println("classPath: " + classPath);

        BcelWorld world = new BcelWorld(path);

        UnresolvedType ut = UnresolvedType.forName(name);
        ut.setNeedsModifiableDelegate(true);
        LazyClassGen clazz = new LazyClassGen(BcelWorld.getBcelObjectType(world.resolve(ut)));
        clazz.print(out);
        out.println();
    }


    public int getNewGeneratedNameTag() {
        return childCounter++;
    }
    
    // ---- 

    public LazyClassGen(
        String class_name,
        String super_class_name,
        String file_name,
        int access_flags,
        String[] interfaces,
        World world) 
    {
        myGen = new ClassGen(class_name, super_class_name, file_name, access_flags, interfaces);
		constantPoolGen = myGen.getConstantPool();
        fact = new InstructionFactory(myGen, constantPoolGen);
        regenerateGenericSignatureAttribute = true;
        this.world = world;
    }

	//Non child type, so it comes from a real type in the world.
    public LazyClassGen(BcelObjectType myType) {
    	myGen = new ClassGen(myType.getJavaClass());
    	constantPoolGen = myGen.getConstantPool();
		fact = new InstructionFactory(myGen, constantPoolGen);        
		this.myType = myType;
		this.world = myType.getResolvedTypeX().getWorld();

		/* Does this class support serialization */
		if (implementsSerializable(getType())) {
			isSerializable = true;       

//			ResolvedMember[] fields = getType().getDeclaredFields();
//			for (int i = 0; i < fields.length; i++) {
//				ResolvedMember field = fields[i];
//				if (field.getName().equals("serialVersionUID")
//					&& field.isStatic() && field.getType().equals(ResolvedType.LONG)) {
//					hasSerialVersionUIDField = true;					
//				}
//			}
			hasSerialVersionUIDField = hasSerialVersionUIDField(getType());					

			ResolvedMember[] methods = getType().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				ResolvedMember method = methods[i];
				if (method.getName().equals("<clinit>")) {
					hasClinit = true;					
				}
			}
			
			// Do we need to calculate an SUID and add it?
			if (!hasSerialVersionUIDField && world.isAddSerialVerUID()) {
	        	calculatedSerialVersionUID = myGen.getSUID();
	        	Field fg = new FieldGen(
	        			Constants.ACC_PRIVATE|Constants.ACC_FINAL|Constants.ACC_STATIC,
	        			BasicType.LONG,"serialVersionUID",getConstantPoolGen()).getField();
	        	addField(fg);
	        	hasSerialVersionUIDField=true;
	        	serialVersionUIDRequiresInitialization=true;
	        	// warn about what we've done?
	        	if (world.getLint().calculatingSerialVersionUID.isEnabled())
	        		world.getLint().calculatingSerialVersionUID.signal(
	        				new String[]{getClassName(),Long.toString(calculatedSerialVersionUID)+"L"},null,null);
	        }
		}

        Method[] methods = myGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            addMethodGen(new LazyMethodGen(methods[i], this));
        }
        
    }

	public static boolean hasSerialVersionUIDField (ResolvedType type) {

		ResolvedMember[] fields = type.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			ResolvedMember field = fields[i];
			if (field.getName().equals("serialVersionUID")
				&& field.isStatic() && field.getType().equals(ResolvedType.LONG)) {
				return true;					
			}
		}
		
		return false;
	}

//	public void addAttribute(Attribute i) {
//		myGen.addAttribute(i);
//	}

	// ---- 

    public String getInternalClassName() {
        return getConstantPoolGen().getConstantPool().getConstantString(
            myGen.getClassNameIndex(),
            Constants.CONSTANT_Class);

    }

	public String getInternalFileName() {
		String str = getInternalClassName();
		int index = str.lastIndexOf('/');
		if (index == -1) {
			return getFileName(); 
		} else {
			return str.substring(0, index + 1) + getFileName();
		}	
	}


    public File getPackagePath(File root) {
        String str = getInternalClassName();
        int index = str.lastIndexOf('/');
        if (index == -1)
            return root;
        return new File(root, str.substring(0, index));
    }
    
    /** Returns the packagename - if its the default package we return an empty string
     */
    public String getPackageName() {
    	if (packageName!=null) return packageName;
    	String str = getInternalClassName();
    	int index = str.indexOf("<");
    	if (index!=-1) str = str.substring(0,index); // strip off the generics guff
    	index= str.lastIndexOf("/");
    	if (index==-1) return "";
    	return str.substring(0,index).replace('/','.');
    }

    public String getClassId() {
        String str = getInternalClassName();
        int index = str.lastIndexOf('/');
        if (index == -1)
            return str;
        return str.substring(index + 1);
    }


	public void addMethodGen(LazyMethodGen gen) {
		//assert gen.getClassName() == super.getClassName();
		methodGens.add(gen);
		if (highestLineNumber < gen.highestLineNumber) highestLineNumber = gen.highestLineNumber;
	}

	public void addMethodGen(LazyMethodGen gen, ISourceLocation sourceLocation) {
		addMethodGen(gen);
		if (!gen.getMethod().isPrivate()) { 
			warnOnAddedMethod(gen.getMethod(),sourceLocation);
		}
	}
	

	public void errorOnAddedField (Field field, ISourceLocation sourceLocation) {
		if (isSerializable && !hasSerialVersionUIDField) {
			getWorld().getLint().serialVersionUIDBroken.signal(
				new String[] {
					myType.getResolvedTypeX().getName().toString(),
					field.getName()
				},
				sourceLocation,
				null);               
		}
	}

	public void warnOnAddedInterface (String name, ISourceLocation sourceLocation) {
		warnOnModifiedSerialVersionUID(sourceLocation,"added interface " + name);
	}

	public void warnOnAddedMethod (Method method, ISourceLocation sourceLocation) {
		warnOnModifiedSerialVersionUID(sourceLocation,"added non-private method " + method.getName());
	}

	public void warnOnAddedStaticInitializer (Shadow shadow, ISourceLocation sourceLocation) {
		if (!hasClinit) {
			warnOnModifiedSerialVersionUID(sourceLocation,"added static initializer");
		}
	}

	public void warnOnModifiedSerialVersionUID (ISourceLocation sourceLocation, String reason) {
		if (isSerializable && !hasSerialVersionUIDField)
		getWorld().getLint().needsSerialVersionUIDField.signal(
			new String[] {
				myType.getResolvedTypeX().getName().toString(),
				reason
			},
			sourceLocation,
			null);               
	}

	public World getWorld () {
		return world;
	}

    public List getMethodGens() {
        return methodGens; //???Collections.unmodifiableList(methodGens);
    }
    
    // FIXME asc Should be collection returned here
    public Field[] getFieldGens() {
      return myGen.getFields();
    }
    
    public Field getField(String name) {
    	Field[] allFields = myGen.getFields();
    	if (allFields==null) return null;
    	for (int i = 0; i < allFields.length; i++) {
			Field field = allFields[i];
			if (field.getName().equals(name)) return field;
		}
    	return null;
    }
    
    // FIXME asc How do the ones on the underlying class surface if this just returns new ones added?
    // FIXME asc ...although no one calls this right now !
    public List getAnnotations() {
    	return annotations;
    }

    private void writeBack(BcelWorld world) {
        if (getConstantPoolGen().getSize() > Short.MAX_VALUE) {
            reportClassTooBigProblem();
        	return;
        }
        
        if (annotations.size()>0) {
        	for (Iterator iter = annotations.iterator(); iter.hasNext();) {
				AnnotationGen element = (AnnotationGen) iter.next();
				myGen.addAnnotation(element);
			}
//	    Attribute[] annAttributes  = org.aspectj.apache.bcel.classfile.Utility.getAnnotationAttributes(getConstantPoolGen(),annotations);
//        for (int i = 0; i < annAttributes.length; i++) {
//			Attribute attribute = annAttributes[i];
//			System.err.println("Adding attribute for "+attribute);
//			myGen.addAttribute(attribute);
//		}
        }
        
        // Add a weaver version attribute to the file being produced (if necessary...)
        boolean hasVersionAttribute = false;        
        Attribute[] attrs = myGen.getAttributes();
        for (int i = 0; i < attrs.length && !hasVersionAttribute; i++) {
			Attribute attribute = attrs[i];
			if (attribute.getName().equals("org.aspectj.weaver.WeaverVersion")) hasVersionAttribute=true;
		}        
        if (!hasVersionAttribute)
        	myGen.addAttribute(BcelAttributes.bcelAttribute(new AjAttribute.WeaverVersionInfo(),getConstantPoolGen()));

        if (myType != null && myType.getWeaverState() != null) {
			myGen.addAttribute(BcelAttributes.bcelAttribute(
				new AjAttribute.WeaverState(myType.getWeaverState()), 
				getConstantPoolGen()));
    	}

        //FIXME ATAJ needed only for slow Aspects.aspectOf() - keep or remove
        //make a lot of test fail since the test compare weaved class file
        // based on some test data as text files...
//        if (!myGen.isInterface()) {
//        	addAjClassField();
//        }

    	addAjcInitializers();
    	
        int len = methodGens.size();
        myGen.setMethods(new Method[0]);
        
        calculateSourceDebugExtensionOffsets();
        for (int i = 0; i < len; i++) {
            LazyMethodGen gen = (LazyMethodGen) methodGens.get(i);
            // we skip empty clinits
            if (isEmptyClinit(gen)) continue;
            myGen.addMethod(gen.getMethod());
        }
		if (inlinedFiles.size() != 0) {
			if (hasSourceDebugExtensionAttribute(myGen)) {
				world.showMessage(
					IMessage.WARNING,
					WeaverMessages.format(WeaverMessages.OVERWRITE_JSR45,getFileName()),
					null,
					null);
			}
			// 17Feb05 - ASC - Skip this for now - it crashes IBM 1.4.2 jvms (pr80430).  Will be revisited when contents
			// of attribute are confirmed to be correct.
			// myGen.addAttribute(getSourceDebugExtensionAttribute());
		}
		
		fixupGenericSignatureAttribute();
    }

    /**
     * When working with 1.5 generics, a signature attribute is attached to the type which indicates
     * how it was declared.  This routine ensures the signature attribute for what we are about
     * to write out is correct.  Basically its responsibilities are:
     *   1. Checking whether the attribute needs changing (i.e. did weaving change the type hierarchy)
     *   2. If it did, removing the old attribute
     *   3. Check if we need an attribute at all, are we generic? are our supertypes parameterized/generic?
     *   4. Build the new attribute which includes all typevariable, supertype and superinterface information
     */
	private void fixupGenericSignatureAttribute () {
		
		if (getWorld() != null && !getWorld().isInJava5Mode()) return;
		
		// TODO asc generics Temporarily assume that types we generate dont need a signature attribute (closure/etc).. will need revisiting no doubt...
		if (myType==null) return;
			
		// 1. Has anything changed that would require us to modify this attribute?
		if (!regenerateGenericSignatureAttribute) return;
		
		// 2. Find the old attribute
		Signature sigAttr = null;
		if (myType!=null) { // if null, this is a type built from scratch, it won't already have a sig attribute
			Attribute[] as = myGen.getAttributes();
			for (int i = 0; i < as.length; i++) {
				Attribute attribute = as[i];
				if (attribute.getName().equals("Signature")) sigAttr = (Signature)attribute;
			}
		}
		
		// 3. Do we need an attribute?
		boolean needAttribute = false;
		if (sigAttr!=null) needAttribute = true; // If we had one before, we definetly still need one as types can't be 'removed' from the hierarchy
		
		// check the interfaces
		if (!needAttribute) {
			if (myType==null) {
				boolean stop = true;
			}
			ResolvedType[] interfaceRTXs = myType.getDeclaredInterfaces();
			for (int i = 0; i < interfaceRTXs.length; i++) {
				ResolvedType typeX = interfaceRTXs[i];
				if (typeX.isGenericType() || typeX.isParameterizedType())  needAttribute = true;
			}
		
			// check the supertype
			ResolvedType superclassRTX = myType.getSuperclass();
			if (superclassRTX.isGenericType() || superclassRTX.isParameterizedType()) needAttribute = true;
		}
		
		if (needAttribute) {
			StringBuffer signature = new StringBuffer();
			// first, the type variables...
			TypeVariable[] tVars = myType.getTypeVariables();
			if (tVars.length>0) {
				signature.append("<");
				for (int i = 0; i < tVars.length; i++) {
					TypeVariable variable = tVars[i];
					signature.append(variable.getSignature());
				}
				signature.append(">");
			}
			// now the supertype
			String supersig = myType.getSuperclass().getSignatureForAttribute();
			signature.append(supersig);
			ResolvedType[] interfaceRTXs = myType.getDeclaredInterfaces();
			for (int i = 0; i < interfaceRTXs.length; i++) {
				String s = interfaceRTXs[i].getSignatureForAttribute();
				signature.append(s);
			}
			if (sigAttr!=null) myGen.removeAttribute(sigAttr);
			myGen.addAttribute(createSignatureAttribute(signature.toString()));
		}
	}
	
	/** 
	 * Helper method to create a signature attribute based on a string signature:
	 *  e.g. "Ljava/lang/Object;LI<Ljava/lang/Double;>;"
	 */
	private Signature createSignatureAttribute(String signature) {
		int nameIndex = constantPoolGen.addUtf8("Signature");
		int sigIndex  = constantPoolGen.addUtf8(signature);
		return new Signature(nameIndex,2,sigIndex,constantPoolGen.getConstantPool());
	}

	/**
	 * 
	 */
	private void reportClassTooBigProblem() {
		// PR 59208
		// we've generated a class that is just toooooooooo big (you've been generating programs
		// again haven't you? come on, admit it, no-one writes classes this big by hand).
		// create an empty myGen so that we can give back a return value that doesn't upset the
		// rest of the process.
		myGen = new ClassGen(myGen.getClassName(), myGen.getSuperclassName(), 
		        myGen.getFileName(), myGen.getAccessFlags(), myGen.getInterfaceNames());
		// raise an error against this compilation unit.
		getWorld().showMessage(
				IMessage.ERROR, 
				WeaverMessages.format(WeaverMessages.CLASS_TOO_BIG,
						              this.getClassName()),
			    new SourceLocation(new File(myGen.getFileName()),0), null
			    );
	}

	private static boolean hasSourceDebugExtensionAttribute(ClassGen gen) {
		ConstantPoolGen pool = gen.getConstantPool();
		Attribute[] attrs = gen.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
			if ("SourceDebugExtension"
				.equals(((ConstantUtf8) pool.getConstant(attrs[i].getNameIndex())).getBytes())) {
				return true;
			}
		}
		return false;
	}

    public JavaClass getJavaClass(BcelWorld world) {
        writeBack(world);
        return myGen.getJavaClass();
    }
    
    public byte [] getJavaClassBytesIncludingReweavable(BcelWorld world){
        writeBack(world);
        byte [] wovenClassFileData = myGen.getJavaClass().getBytes();
        WeaverStateInfo wsi = myType.getWeaverState();//getOrCreateWeaverStateInfo();
        if(wsi != null && wsi.isReweavable()){ // && !reweavableDataInserted
            //reweavableDataInserted = true;
            return wsi.replaceKeyWithDiff(wovenClassFileData);
        } else{
            return wovenClassFileData;
        }
    }

    public void addGeneratedInner(LazyClassGen newClass) {
        classGens.add(newClass);
    }
    
    public void addInterface(UnresolvedType typeX, ISourceLocation sourceLocation) {
    	regenerateGenericSignatureAttribute = true;
    	myGen.addInterface(typeX.getRawName());
        if (!typeX.equals(UnresolvedType.SERIALIZABLE)) 
		  warnOnAddedInterface(typeX.getName(),sourceLocation);
    }
    
	public void setSuperClass(ResolvedType typeX) {
    	regenerateGenericSignatureAttribute = true;
    	myType.addParent(typeX); // used for the attribute
    	if (typeX.getGenericType()!=null) typeX = typeX.getGenericType();
		myGen.setSuperclassName(typeX.getName()); // used in the real class data
	 }
    
    public String getSuperClassname() {
        return myGen.getSuperclassName();   
    }

    // FIXME asc not great that some of these ask the gen and some ask the type ! (see the related setters too)
	public ResolvedType getSuperClass() {
		return myType.getSuperclass();
	} 
    
    public String[] getInterfaceNames() {
    	return myGen.getInterfaceNames();
    }


    // non-recursive, may be a bug, ha ha.
    private List getClassGens() {
        List ret = new ArrayList();
        ret.add(this);
        ret.addAll(classGens);

        return ret;
    }


	public List getChildClasses(BcelWorld world) {
		if (classGens.isEmpty()) return Collections.EMPTY_LIST;
		List ret = new ArrayList();
		for (Iterator i = classGens.iterator(); i.hasNext();) {
			LazyClassGen clazz = (LazyClassGen) i.next();
			byte[] bytes = clazz.getJavaClass(world).getBytes();
			String name = clazz.getName();
			int index = name.lastIndexOf('$');
			// XXX this could be bad, check use of dollar signs.
			name = name.substring(index+1);
			ret.add(new UnwovenClassFile.ChildClass(name, bytes));
		}
		return ret;
	}

	public String toString() {
		return toShortString();
	}

    public String toShortString() {
        String s =
            org.aspectj.apache.bcel.classfile.Utility.accessToString(myGen.getAccessFlags(), true);
        if (s != "")
            s += " ";
        s += org.aspectj.apache.bcel.classfile.Utility.classOrInterface(myGen.getAccessFlags());
        s += " ";
        s += myGen.getClassName();
        return s;
    }

    public String toLongString() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        print(new PrintStream(s));
        return new String(s.toByteArray());
    }

    public void print() { print(System.out); }
        
    public void print(PrintStream out) {
        List classGens = getClassGens();
        for (Iterator iter = classGens.iterator(); iter.hasNext();) {
            LazyClassGen element = (LazyClassGen) iter.next();
            element.printOne(out);
            if (iter.hasNext()) out.println();
        }
    }

    private void printOne(PrintStream out) {
        out.print(toShortString());
        out.print(" extends ");
        out.print(
            org.aspectj.apache.bcel.classfile.Utility.compactClassName(
                myGen.getSuperclassName(),
                false));

        int size = myGen.getInterfaces().length;

        if (size > 0) {
            out.print(" implements ");
            for (int i = 0; i < size; i++) {
                out.print(myGen.getInterfaceNames()[i]);
                if (i < size - 1)
                    out.print(", ");
            }
        }
        out.print(":");
        out.println();
        // XXX make sure to pass types correctly around, so this doesn't happen.
        if (myType != null) {
        	myType.printWackyStuff(out);
        }
        Field[] fields = myGen.getFields();
        for (int i = 0, len = fields.length; i < len; i++) {
            out.print("  ");
            out.println(fields[i]);
        }
        List methodGens = getMethodGens();
        for (Iterator iter = methodGens.iterator(); iter.hasNext();) {
            LazyMethodGen gen = (LazyMethodGen) iter.next();
            // we skip empty clinits
            if (isEmptyClinit(gen)) continue;
            gen.print(out, (myType != null ? myType.getWeaverVersionAttribute() : WeaverVersionInfo.UNKNOWN));
            if (iter.hasNext()) out.println();
        }
//        out.println("  ATTRIBS: " + Arrays.asList(myGen.getAttributes()));
        
        out.println("end " + toShortString());
    }
    
    private boolean isEmptyClinit(LazyMethodGen gen) {
    	if (!gen.getName().equals("<clinit>")) return false;
    	//System.err.println("checking clinig: " + gen);
    	InstructionHandle start = gen.getBody().getStart();
    	while (start != null) {
    		if (Range.isRangeHandle(start) || (start.getInstruction() instanceof RETURN)) {
    			start = start.getNext();
    		} else {
    			return false;
    		}
    	} 
    	
    	return true;
    }

    public ConstantPoolGen getConstantPoolGen() {
        return constantPoolGen;
    }
    
    public String getName() {
        return myGen.getClassName();
    }

	public boolean isWoven() {
		return myType.getWeaverState() != null;
	}
	
	public boolean isReweavable() {
		if (myType.getWeaverState()==null) return true;
        return myType.getWeaverState().isReweavable();
	}
	
	public Set getAspectsAffectingType() {
		if (myType.getWeaverState()==null) return null;
		return myType.getWeaverState().getAspectsAffectingType();
	}
		
	public WeaverStateInfo getOrCreateWeaverStateInfo(boolean inReweavableMode) {
		WeaverStateInfo ret = myType.getWeaverState();
		if (ret != null) return ret;
		ret = new WeaverStateInfo(inReweavableMode);
		myType.setWeaverState(ret);
		return ret;
	}

    public InstructionFactory getFactory() {
        return fact;
    }

    public LazyMethodGen getStaticInitializer() {
        for (Iterator i = methodGens.iterator(); i.hasNext();) {
            LazyMethodGen gen = (LazyMethodGen) i.next();
			if (gen.getName().equals("<clinit>")) return gen;
        } 
        LazyMethodGen clinit = new LazyMethodGen(
        	Modifier.STATIC,
        	Type.VOID,
        	"<clinit>",
        	new Type[0],
        	CollectionUtil.NO_STRINGS,
        	this);
       	clinit.getBody().insert(InstructionConstants.RETURN);
        methodGens.add(clinit);
        return clinit;
    }
    
    public LazyMethodGen getAjcPreClinit() {
        for (Iterator i = methodGens.iterator(); i.hasNext();) {
            LazyMethodGen gen = (LazyMethodGen) i.next();
			if (gen.getName().equals(NameMangler.AJC_PRE_CLINIT_NAME)) return gen;
        }
        LazyMethodGen ajcClinit = new LazyMethodGen(
        	Modifier.STATIC,
        	Type.VOID,
        	NameMangler.AJC_PRE_CLINIT_NAME,
        	new Type[0],
        	CollectionUtil.NO_STRINGS,
        	this);
       	ajcClinit.getBody().insert(InstructionConstants.RETURN);
        methodGens.add(ajcClinit);
        
        getStaticInitializer().getBody().insert(Utility.createInvoke(getFactory(), ajcClinit));
        return ajcClinit;
    }
    
    
    
    // reflective thisJoinPoint support
    Map/*BcelShadow, Field*/ tjpFields = new HashMap();
    public static final ObjectType proceedingTjpType =
    	new ObjectType("org.aspectj.lang.ProceedingJoinPoint");
    public static final ObjectType tjpType =
    	new ObjectType("org.aspectj.lang.JoinPoint");
    public static final ObjectType staticTjpType = 
    	new ObjectType("org.aspectj.lang.JoinPoint$StaticPart");
    public static final ObjectType enclosingStaticTjpType =
    	new ObjectType("org.aspectj.lang.JoinPoint$EnclosingStaticPart");
    private static final ObjectType sigType =
    	new ObjectType("org.aspectj.lang.Signature");
//    private static final ObjectType slType = 
//    	new ObjectType("org.aspectj.lang.reflect.SourceLocation");
    private static final ObjectType factoryType = 
    	new ObjectType("org.aspectj.runtime.reflect.Factory");
    private static final ObjectType classType = 
    	new ObjectType("java.lang.Class");

    public Field getTjpField(BcelShadow shadow, final boolean isEnclosingJp) {
    	Field ret = (Field)tjpFields.get(shadow);
    	if (ret != null) return ret;
    	
		int modifiers = Modifier.STATIC | Modifier.FINAL;
		
		// XXX - Do we ever inline before or after advice? If we do, then we
		// better include them in the check below. (or just change it to
		// shadow.getEnclosingMethod().getCanInline())
		
		// If the enclosing method is around advice, we could inline the join point
		// that has led to this shadow.  If we do that then the TJP we are creating
		// here must be PUBLIC so it is visible to the type in which the 
		// advice is inlined. (PR71377)
		LazyMethodGen encMethod = shadow.getEnclosingMethod();
		boolean shadowIsInAroundAdvice = false;
		if (encMethod!=null && encMethod.getName().startsWith(NameMangler.PREFIX+"around")) {
			shadowIsInAroundAdvice = true;
		}
		
		if (getType().isInterface() || shadowIsInAroundAdvice) {
			modifiers |= Modifier.PUBLIC;
		}
		else {
			modifiers |= Modifier.PRIVATE;
		}
		ObjectType jpType = null;
		if (world.isTargettingAspectJRuntime12()) { // TAG:SUPPORTING12: We didn't have different staticjp types in 1.2
			jpType = staticTjpType;
		} else {
			jpType = isEnclosingJp?enclosingStaticTjpType:staticTjpType;
		}
		ret = new FieldGen(modifiers,jpType,"ajc$tjp_" + tjpFields.size(),getConstantPoolGen()).getField();
    	addField(ret);
    	tjpFields.put(shadow, ret);
    	return ret;
    }


    //FIXME ATAJ needed only for slow Aspects.aspectOf - keep or remove
//    private void addAjClassField() {
//    // Andy: Why build it again??
//        Field ajClassField = new FieldGen(
//                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
//                classType,
//                "aj$class",
//                getConstantPoolGen()).getField();
//        addField(ajClassField);
//
//        InstructionList il = new InstructionList();
//        il.append(new PUSH(getConstantPoolGen(), getClassName()));
//        il.append(fact.createInvoke("java.lang.Class", "forName", classType,
//                    new Type[] {Type.STRING}, Constants.INVOKESTATIC));
//        il.append(fact.createFieldAccess(getClassName(), ajClassField.getName(),
//            classType, Constants.PUTSTATIC));
//
//        getStaticInitializer().getBody().insert(il);
//    }

    private void addAjcInitializers() {
    	if (tjpFields.size() == 0 && !serialVersionUIDRequiresInitialization) return;
    	InstructionList il = null;
    	
    	if (tjpFields.size()>0) {
    	    il = initializeAllTjps();
    	}
    	
    	if (serialVersionUIDRequiresInitialization) {
    		if (il==null) {
    			il= new InstructionList();
    		}
    	    il.append(new PUSH(getConstantPoolGen(),calculatedSerialVersionUID));
    	    il.append(getFactory().createFieldAccess(getClassName(), "serialVersionUID", BasicType.LONG, Constants.PUTSTATIC));
    	}
    	
    	getStaticInitializer().getBody().insert(il);
    }
    
    
    private InstructionList initializeAllTjps() {
    	InstructionList list = new InstructionList();
    	InstructionFactory fact = getFactory();
    	
    	// make a new factory
    	list.append(fact.createNew(factoryType));
    	list.append(InstructionFactory.createDup(1));
    	
    	list.append(new PUSH(getConstantPoolGen(), getFileName()));
    	
    	// load the current Class object
    	//XXX check that this works correctly for inners/anonymous
    	list.append(new PUSH(getConstantPoolGen(), getClassName()));
    	//XXX do we need to worry about the fact the theorectically this could throw
    	//a ClassNotFoundException
    	list.append(fact.createInvoke("java.lang.Class", "forName", classType,
    				new Type[] {Type.STRING}, Constants.INVOKESTATIC));
    	
    	list.append(fact.createInvoke(factoryType.getClassName(), "<init>",
    				Type.VOID, new Type[] {Type.STRING, classType},
    				Constants.INVOKESPECIAL));
    				
    	list.append(InstructionFactory.createStore(factoryType, 0));
    	
    	List entries = new ArrayList(tjpFields.entrySet());
    	Collections.sort(entries, new Comparator() {
    		public int compare(Object a, Object b) {
    			Map.Entry ae = (Map.Entry) a;
    			Map.Entry be = (Map.Entry) b;
    			return ((Field) ae.getValue())
    				.getName()
    				.compareTo(((Field)be.getValue()).getName());
    		}
    	});

    	for (Iterator i = entries.iterator(); i.hasNext(); ) {
    		Map.Entry entry = (Map.Entry)i.next();
    		initializeTjp(fact, list, (Field)entry.getValue(), (BcelShadow)entry.getKey());
    	}
    	
    	return list;
    }
    
    
    private void initializeTjp(InstructionFactory fact, InstructionList list,
    							 Field field, BcelShadow shadow)
    {
    	Member sig = shadow.getSignature();
    	//ResolvedMember mem = shadow.getSignature().resolve(shadow.getWorld());
    	
    	// load the factory
    	list.append(InstructionFactory.createLoad(factoryType, 0));
    	
    	// load the kind
    	list.append(new PUSH(getConstantPoolGen(), shadow.getKind().getName()));
    	
    	// create the signature
    	list.append(InstructionFactory.createLoad(factoryType, 0));
    	
    	if (world.isTargettingAspectJRuntime12()) { // TAG:SUPPORTING12: We didn't have optimized factory methods in 1.2
        	list.append(new PUSH(getConstantPoolGen(), sig.getSignatureString(shadow.getWorld())));
    		list.append(fact.createInvoke(factoryType.getClassName(), 
    					sig.getSignatureMakerName(),
    					new ObjectType(sig.getSignatureType()),
    					new Type[] { Type.STRING },
    					Constants.INVOKEVIRTUAL));
    	} else 	if (sig.getKind().equals(Member.METHOD)) {
    		BcelWorld w = shadow.getWorld();
    		// For methods, push the parts of the signature on.
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getModifiers(w))));
    		list.append(new PUSH(getConstantPoolGen(),sig.getName()));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterTypes())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterNames(w))));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getExceptions(w))));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getReturnType())));
    		// And generate a call to the variant of makeMethodSig() that takes 7 strings
    		list.append(fact.createInvoke(factoryType.getClassName(), 
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING,Type.STRING,Type.STRING,Type.STRING,Type.STRING,Type.STRING,Type.STRING },
    				Constants.INVOKEVIRTUAL));
   	    } else if (sig.getKind().equals(Member.MONITORENTER)) {
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(fact.createInvoke(factoryType.getClassName(), 
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING},
    				Constants.INVOKEVIRTUAL));
    	} else if (sig.getKind().equals(Member.MONITOREXIT)) {
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(fact.createInvoke(factoryType.getClassName(), 
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING},
    				Constants.INVOKEVIRTUAL));
     	} else if (sig.getKind().equals(Member.HANDLER)) {
    		BcelWorld w = shadow.getWorld();
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterTypes())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterNames(w))));
    		list.append(fact.createInvoke(factoryType.getClassName(),
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING, Type.STRING, Type.STRING },
    				Constants.INVOKEVIRTUAL));    	
    	} else if(sig.getKind().equals(Member.CONSTRUCTOR)) {
    		BcelWorld w = shadow.getWorld();
    		if (w.isJoinpointArrayConstructionEnabled() && sig.getDeclaringType().isArray()) {
    			// its the magical new jp
    			list.append(new PUSH(getConstantPoolGen(),makeString(Modifier.PUBLIC)));
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterTypes())));
	    		list.append(new PUSH(getConstantPoolGen(),""));//makeString("")));//sig.getParameterNames(w))));
	    		list.append(new PUSH(getConstantPoolGen(),""));//makeString("")));//sig.getExceptions(w))));
	    		list.append(fact.createInvoke(factoryType.getClassName(),
	    				sig.getSignatureMakerName(),
	    				new ObjectType(sig.getSignatureType()),
	    				new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING },
	    				Constants.INVOKEVIRTUAL));    	
    		} else {
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getModifiers(w))));	
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterTypes())));
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterNames(w))));
	    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getExceptions(w))));
	    		list.append(fact.createInvoke(factoryType.getClassName(),
	    				sig.getSignatureMakerName(),
	    				new ObjectType(sig.getSignatureType()),
	    				new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING },
	    				Constants.INVOKEVIRTUAL));    	
    		}
    	} else if(sig.getKind().equals(Member.FIELD)) {
    		BcelWorld w = shadow.getWorld();
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getModifiers(w))));
    		list.append(new PUSH(getConstantPoolGen(),sig.getName()));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getReturnType())));
    		list.append(fact.createInvoke(factoryType.getClassName(),
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING },
    				Constants.INVOKEVIRTUAL));    	
    	} else if(sig.getKind().equals(Member.ADVICE)) {
    		BcelWorld w = shadow.getWorld();
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getModifiers(w))));
    		list.append(new PUSH(getConstantPoolGen(),sig.getName()));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterTypes())));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getParameterNames(w))));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getExceptions(w))));
    		list.append(new PUSH(getConstantPoolGen(),makeString((sig.getReturnType()))));    		
    		list.append(fact.createInvoke(factoryType.getClassName(),
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING },
    				Constants.INVOKEVIRTUAL));    	
    	} else if(sig.getKind().equals(Member.STATIC_INITIALIZATION)) {
    		BcelWorld w = shadow.getWorld();
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getModifiers(w))));
    		list.append(new PUSH(getConstantPoolGen(),makeString(sig.getDeclaringType())));
    		list.append(fact.createInvoke(factoryType.getClassName(),
    				sig.getSignatureMakerName(),
    				new ObjectType(sig.getSignatureType()),
    				new Type[] { Type.STRING, Type.STRING },
    				Constants.INVOKEVIRTUAL));
    	} else {
    	  list.append(new PUSH(getConstantPoolGen(), sig.getSignatureString(shadow.getWorld())));
    	  list.append(fact.createInvoke(factoryType.getClassName(), 
			  	   sig.getSignatureMakerName(),
			  	   new ObjectType(sig.getSignatureType()),
			  	   new Type[] { Type.STRING },
			  	   Constants.INVOKEVIRTUAL));
    	}   	
    	
    	//XXX should load source location from shadow
    	list.append(Utility.createConstant(fact, shadow.getSourceLine()));

        final String factoryMethod;
        
       	if (world.isTargettingAspectJRuntime12()) { // TAG:SUPPORTING12: We didn't have makeESJP() in 1.2    	
        	list.append(fact.createInvoke(factoryType.getClassName(),
        			"makeSJP", staticTjpType, 
        			new Type[] { Type.STRING, sigType, Type.INT},
        			Constants.INVOKEVIRTUAL));
        	
        	// put it in the field	
        	list.append(fact.createFieldAccess(getClassName(), field.getName(),staticTjpType, Constants.PUTSTATIC));
        
        } else {
	        if (staticTjpType.equals(field.getType())) {
	            factoryMethod = "makeSJP";
	        } else if (enclosingStaticTjpType.equals(field.getType())) {
	            factoryMethod = "makeESJP";
	        } else {
	            throw new Error("should not happen");
	        }
	    	list.append(fact.createInvoke(factoryType.getClassName(),
	    			factoryMethod, field.getType(),
	    			new Type[] { Type.STRING, sigType, Type.INT},
	    			Constants.INVOKEVIRTUAL));
	    	// put it in the field	
	    	list.append(fact.createFieldAccess(getClassName(), field.getName(), field.getType(), Constants.PUTSTATIC));
    	}
    }
    
    
    protected String makeString(int i) {
    	return Integer.toString(i, 16);  //??? expensive
    }
    
    
    protected String makeString(UnresolvedType t) {
    	// this is the inverse of the odd behavior for Class.forName w/ arrays
    	if (t.isArray()) {
    		// this behavior matches the string used by the eclipse compiler for Foo.class literals
    		return t.getSignature().replace('/', '.');
    	} else {
    		return t.getName();
    	}
    }
          
    protected String makeString(UnresolvedType[] types) {
    	if (types == null) return "";
    	StringBuffer buf = new StringBuffer();
    	for (int i = 0, len=types.length; i < len; i++) {
    		buf.append(makeString(types[i]));
    		buf.append(':');
    	}
    	return buf.toString();
    }
       
    protected String makeString(String[] names) {
    	if (names == null) return "";
    	StringBuffer buf = new StringBuffer();
    	for (int i = 0, len=names.length; i < len; i++) {
    		buf.append(names[i]);
    		buf.append(':');
    	}
    	return buf.toString();
    } 
       
	public ResolvedType getType() {
		if (myType == null) return null;
		return myType.getResolvedTypeX();
	}

	public BcelObjectType getBcelObjectType() {
		return myType;
	}

	public String getFileName() {
		return myGen.getFileName();
	}

	private void addField(Field field) {
		myGen.addField(field);
		makeSyntheticAndTransientIfNeeded(field);
	}
	
	private void makeSyntheticAndTransientIfNeeded(Field field) {
		if (field.getName().startsWith(NameMangler.PREFIX) &&
			!field.getName().startsWith("ajc$interField$") &&
			!field.getName().startsWith("ajc$instance$")) {
			// it's an aj added field
			// first do transient
			if (!field.isStatic()) {
				field.setModifiers(field.getModifiers() | Constants.ACC_TRANSIENT);
			}
			// then do synthetic
			if (getWorld().isInJava5Mode()) {
				// add the synthetic modifier flag
				field.setModifiers(field.getModifiers() | ACC_SYNTHETIC);
			}
			if (!hasSyntheticAttribute(field.getAttributes())) {
	       	  // belt and braces, do the attribute even on Java 5 in addition to the modifier flag
			  Attribute[] oldAttrs = field.getAttributes();
			  Attribute[] newAttrs = new Attribute[oldAttrs.length + 1];
			  System.arraycopy(oldAttrs, 0, newAttrs, 0, oldAttrs.length);
   			  ConstantPoolGen cpg = myGen.getConstantPool();
   			  int index = cpg.addUtf8("Synthetic");
   			  Attribute synthetic  = new Synthetic(index, 0, new byte[0], cpg.getConstantPool());
   			  newAttrs[newAttrs.length - 1] = synthetic;
   			  field.setAttributes(newAttrs);
			}
		}
	}
	
	private boolean hasSyntheticAttribute(Attribute[] attributes) {
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].getName().equals("Synthetic")) {
				return true;
			}
		}
		return false;
	}

	public void replaceField(Field oldF, Field newF){
		myGen.removeField(oldF);
		myGen.addField(newF);
	}
	
	public void addField(Field field, ISourceLocation sourceLocation) {
		addField(field);
		if (!(field.isPrivate() 
			&& (field.isStatic() || field.isTransient()))) {
			errorOnAddedField(field,sourceLocation);
		}
	}

	public String getClassName() {
		return myGen.getClassName();
	}

	public boolean isInterface() {
		return myGen.isInterface();
	}
    
    public boolean isAbstract() {
      return myGen.isAbstract();   
    }

	public LazyMethodGen getLazyMethodGen(Member m) {
		return getLazyMethodGen(m.getName(), m.getSignature(),false);
	}

	public LazyMethodGen getLazyMethodGen(String name, String signature) {
		return getLazyMethodGen(name,signature,false);
	}
	
	public LazyMethodGen getLazyMethodGen(String name, String signature,boolean allowMissing) {
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen gen = (LazyMethodGen) i.next();
			if (gen.getName().equals(name) && gen.getSignature().equals(signature))
				return gen;
		}
		
		if (!allowMissing) {
			throw new BCException("Class " + this.getName() + " does not have a method " 	
				+ name + " with signature " + signature);
		} 
		
		return null;
	}
	
	
	public boolean forcePublic() {
		if (myGen.isPublic()) return false;
		myGen.setAccessFlags(Utility.makePublic(myGen.getAccessFlags()));
		return true;
	}

	
	public boolean hasAnnotation(UnresolvedType t) {
		
		// annotations on the real thing
		AnnotationGen agens[] = myGen.getAnnotations();
		if (agens==null) return false;
		for (int i = 0; i < agens.length; i++) {
			AnnotationGen gen = agens[i];
			if (t.equals(UnresolvedType.forSignature(gen.getTypeSignature()))) return true;
		}
		
		// annotations added during this weave
		
		return false;
	}
	
	public void addAnnotation(Annotation a) {
		if (!hasAnnotation(UnresolvedType.forSignature(a.getTypeSignature()))) {
		  annotations.add(new AnnotationGen(a,getConstantPoolGen(),true));
		}
	}
	
	// this test is like asking:
	// if (UnresolvedType.SERIALIZABLE.resolve(getType().getWorld()).isAssignableFrom(getType())) {
    // only we don't do that because this forces us to find all the supertypes of the type,
	// and if one of them is missing we fail, and it's not worth failing just to put out
	// a warning message!
	private boolean implementsSerializable(ResolvedType aType) {
		if (aType.getSignature().equals(UnresolvedType.SERIALIZABLE.getSignature())) return true;
		
		ResolvedType[] interfaces = aType.getDeclaredInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].isMissing()) continue;
			if (implementsSerializable(interfaces[i])) return true;
		}
		ResolvedType superType = aType.getSuperclass();
		if (superType != null && !superType.isMissing()) {
			return implementsSerializable(superType);
		}
		return false;
	}
	
	public boolean isAtLeastJava5() {
		return (myGen.getMajor()>=Constants.MAJOR_1_5);
	}

	/**
	 * Return the next available field name with the specified 'prefix', e.g.
	 * for prefix 'class$' where class$0, class$1 exist then return class$2
	 */
	public String allocateField(String prefix) {
		int highestAllocated = -1;
		Field[] fs = getFieldGens();
		for (int i = 0; i < fs.length; i++) {
			Field field = fs[i];
			if (field.getName().startsWith(prefix)) {
				try {
					int num = Integer.parseInt(field.getName().substring(prefix.length()));
					if (num>highestAllocated) highestAllocated = num;
				} catch (NumberFormatException nfe) {
					// something wrong with the number on the end of that field...
				}
			}
		}
		return prefix+Integer.toString(highestAllocated+1);
	}

}
