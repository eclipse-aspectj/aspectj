/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;
import org.aspectj.util.CollectionUtil;
import org.aspectj.weaver.*;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverStateKind;

public final class LazyClassGen {

    /** Emit disassembled class and newline to out */
    public static void disassemble(String path, String name, PrintStream out) 
        throws IOException {
        if (null == out) {
            return;
        }
        //out.println("classPath: " + classPath);

        BcelWorld world = new BcelWorld(path);

        LazyClassGen clazz = new LazyClassGen(BcelWorld.getBcelObjectType(world.resolve(name)));
        clazz.print(out);
        out.println();
    }

	private BcelObjectType myType; // XXX is not set for types we create
	private ClassGen myGen;
	private ConstantPoolGen constantPoolGen;

    private List /*LazyMethodGen*/
        methodGens = new ArrayList();
    private List /*LazyClassGen*/
        classGens = new ArrayList();

    private int childCounter = 0;

    public int getNewGeneratedNameTag() {
        return childCounter++;
    }
    
    private InstructionFactory fact;
    // ---- 

    public LazyClassGen(
        String class_name,
        String super_class_name,
        String file_name,
        int access_flags,
        String[] interfaces) 
    {
        myGen = new ClassGen(class_name, super_class_name, file_name, access_flags, interfaces);
		constantPoolGen = myGen.getConstantPool();
        fact = new InstructionFactory(constantPoolGen);
    }

	//Non child type, so it comes from a real type in the world.
    public LazyClassGen(BcelObjectType myType) {
    	myGen = new ClassGen(myType.getJavaClass());
    	constantPoolGen = myGen.getConstantPool();
		fact = new InstructionFactory(constantPoolGen);        
		this.myType = myType;

        Method[] methods = myGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            addMethodGen(new LazyMethodGen(methods[i], this));
        }
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

    public File getPackagePath(File root) {
        String str = getInternalClassName();
        int index = str.lastIndexOf('/');
        if (index == -1)
            return root;
        return new File(root, str.substring(0, index));
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
    }

    public List getMethodGens() {
        return methodGens; //???Collections.unmodifiableList(methodGens);

    }

    private void writeBack() {
    	addAjcInitializers();
    	
    	
        int len = methodGens.size();
        myGen.setMethods(new Method[0]);
        for (int i = 0; i < len; i++) {
            LazyMethodGen gen = (LazyMethodGen) methodGens.get(i);
            // we skip empty clinits
            if (isEmptyClinit(gen)) continue;
            myGen.addMethod(gen.getMethod());
        }
    }

    public JavaClass getJavaClass() {
        writeBack();
        return myGen.getJavaClass();
    }

    public void addGeneratedInner(LazyClassGen newClass) {
        classGens.add(newClass);
    }
    
    public void addInterface(TypeX typeX) {
    	myGen.addInterface(typeX.getName());
    }
    
	public void setSuperClass(TypeX typeX) {
		myGen.setSuperclassName(typeX.getName());
	 }


    // non-recursive, may be a bug, ha ha.
    private List getClassGens() {
        List ret = new ArrayList();
        ret.add(this);
        ret.addAll(classGens);

        return ret;
    }


	public List getChildClasses() {
		if (classGens.isEmpty()) return Collections.EMPTY_LIST;
		List ret = new ArrayList();
		for (Iterator i = classGens.iterator(); i.hasNext();) {
			LazyClassGen clazz = (LazyClassGen) i.next();
			byte[] bytes = clazz.getJavaClass().getBytes();
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
            org.apache.bcel.classfile.Utility.accessToString(myGen.getAccessFlags(), true);
        if (s != "")
            s += " ";
        s += org.apache.bcel.classfile.Utility.classOrInterface(myGen.getAccessFlags());
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
            org.apache.bcel.classfile.Utility.compactClassName(
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
            gen.print(out);
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

	public WeaverStateKind getWeaverState() {
		WeaverStateKind kind = myType.getWeaverState();
		if (kind == null) return WeaverStateKind.Untouched;
		return kind;
	}

	public void setWeaverState(WeaverStateKind s) {
		Attribute[] attributes = myGen.getAttributes();
		if (attributes != null) {
			for (int i = attributes.length - 1; i >=0; i--) {
				Attribute a = attributes[i];
				if (a instanceof Unknown) {
					Unknown u = (Unknown) a;
					if (u.getName().equals(AjAttribute.WeaverState.AttributeName)) {
						myGen.removeAttribute(u);
					}
				}
			}
		}
		myGen.addAttribute(BcelAttributes.bcelAttribute(
			new AjAttribute.WeaverState(s), 
			getConstantPoolGen()));
		myType.setWeaverState(s);
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
       	clinit.getBody().insert(getFactory().RETURN);
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
       	ajcClinit.getBody().insert(getFactory().RETURN);
        methodGens.add(ajcClinit);
        
        getStaticInitializer().getBody().insert(Utility.createInvoke(getFactory(), ajcClinit));
        return ajcClinit;
    }
    
    
    
    // reflective thisJoinPoint support
    Map/*BcelShadow, Field*/ tjpFields = new HashMap();
    public static final ObjectType tjpType = 
    	new ObjectType("org.aspectj.lang.JoinPoint");
    public static final ObjectType staticTjpType = 
    	new ObjectType("org.aspectj.lang.JoinPoint$StaticPart");
    private static final ObjectType sigType = 
    	new ObjectType("org.aspectj.lang.Signature");
    private static final ObjectType slType = 
    	new ObjectType("org.aspectj.lang.reflect.SourceLocation");
    private static final ObjectType factoryType = 
    	new ObjectType("org.aspectj.runtime.reflect.Factory");
    private static final ObjectType classType = 
    	new ObjectType("java.lang.Class");
    
    public Field getTjpField(BcelShadow shadow) {
    	Field ret = (Field)tjpFields.get(shadow);
    	if (ret != null) return ret;
    	
    	ret = new FieldGen(Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL,
    		staticTjpType,
    		"ajc$tjp_" + tjpFields.size(),
    		getConstantPoolGen()).getField();
    	addField(ret);
    	tjpFields.put(shadow, ret);
    	return ret;
    }
    
    private void addAjcInitializers() {
    	if (tjpFields.size() == 0) return;
    	
    	InstructionList il = initializeAllTjps();
    	getStaticInitializer().getBody().insert(il);
    }
    
    
    private InstructionList initializeAllTjps() {
    	InstructionList list = new InstructionList();
    	InstructionFactory fact = getFactory();
    	
    	// make a new factory
    	list.append(fact.createNew(factoryType));
    	list.append(fact.createDup(1));
    	
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
    				
    	list.append(fact.createStore(factoryType, 0));
    	
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
    	list.append(fact.createLoad(factoryType, 0));
    	
    	// load the kind
    	list.append(new PUSH(getConstantPoolGen(), shadow.getKind().getName()));
    	
    	// create the signature
    	list.append(fact.createLoad(factoryType, 0));
    	list.append(new PUSH(getConstantPoolGen(), sig.getSignatureString(shadow.getWorld())));
    	list.append(fact.createInvoke(factoryType.getClassName(), 
    					sig.getSignatureMakerName(),
    					new ObjectType(sig.getSignatureType()),
    					new Type[] { Type.STRING },
    					Constants.INVOKEVIRTUAL));
    	
    	//XXX should load source location from shadow
    	list.append(Utility.createConstant(fact, shadow.getSourceLine()));
    	
    	
    	list.append(fact.createInvoke(factoryType.getClassName(),
    			"makeSJP", staticTjpType, 
    			new Type[] { Type.STRING, sigType, Type.INT},
    			Constants.INVOKEVIRTUAL));
    	
    	// put it in the field	
    	list.append(fact.createFieldAccess(getClassName(), field.getName(),
    		staticTjpType, Constants.PUTSTATIC));
    }
    

	public ResolvedTypeX getType() {
		if (myType == null) return null;
		return myType.getResolvedTypeX();
	}

	public BcelObjectType getBcelObjectType() {
		return myType;
	}

	public String getFileName() {
		return myGen.getFileName();
	}

	public void addField(Field field) {
		myGen.addField(field);
	}

	public String getClassName() {
		return myGen.getClassName();
	}

	public boolean isInterface() {
		return myGen.isInterface();
	}

	public LazyMethodGen getLazyMethodGen(Member m) {
		return getLazyMethodGen(m.getName(), m.getSignature());
	}

	public LazyMethodGen getLazyMethodGen(String name, String signature) {
		for (Iterator i = methodGens.iterator(); i.hasNext();) {
			LazyMethodGen gen = (LazyMethodGen) i.next();
			if (gen.getName().equals(name) && gen.getSignature().equals(signature))
				return gen;
		}
		
		throw new BCException("Class " + this.getName() + " does not have a method " 	
			+ name + " with signature " + signature);
	}
	
	public void forcePublic() {
		myGen.setAccessFlags(Utility.makePublic(myGen.getAccessFlags()));
	}
}
