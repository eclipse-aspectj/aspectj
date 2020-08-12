/*******************************************************************************
 * Copyright (c) 2004 IBM All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andy Clement - initial implementation
 ******************************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationElementValue;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.LocalVariableGen;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.SyntheticRepository;

/**
 * The program that some of the tests generate looks like this:
	public class HelloWorld {
      public static void main(String[] argv) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;
        try {
          name = "Andy";
        } catch(IOException e) { return; }
        System.out.println("Hello, " + name);
      }
    }
 *
 */
public class ParameterAnnotationsTest extends BcelTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Programmatically construct a class and add an annotation to the main method parameter 'argv'
	 */
	public void testParameterAnnotations_builtOK() {
		ClassGen clg        = createClassGen("HelloWorld");
		ConstantPool cpg = clg.getConstantPool();
		InstructionList il  = new InstructionList();
		
		buildClassContentsWithAnnotatedMethods(clg,cpg,il,true);
		
		int i = clg.getMethods().length;
		assertTrue("Class should have 2 methods but has "+i,i==2);
		
		Method mainMethod = clg.getMethods()[0];
		AnnotationGen[] annos = mainMethod.getAnnotationsOnParameter(0);
		assertTrue("Should be two annotation on the 'argv' parameter to main() but there are "+annos.length,annos.length==2);
		assertTrue("This annotation should contain the string 'fruit=Apples' but it is "+annos[0].toString(),
				annos[0].toString().contains("fruit=Apples"));
		assertTrue("This annotation should contain the string 'fruit=Oranges' but it is "+annos[1].toString(),
				annos[1].toString().contains("fruit=Oranges"));
	}
	
	
	
	/**
	 * Check we can save and load a constructed class that contains parameter annotations
	 */
	public void testParameterAnnotations_savedAndLoadedOK() throws ClassNotFoundException {
		ClassGen clg        = createClassGen("HelloWorld");
		ConstantPool cpg = clg.getConstantPool();
		InstructionList il  = new InstructionList();
		
		buildClassContentsWithAnnotatedMethods(clg,cpg,il,true);
		
		dumpClass(clg,"temp5","HelloWorld.class");
		
		JavaClass jc = getClassFrom("temp5","HelloWorld");
		
		clg = new ClassGen(jc);
		
		int i = clg.getMethods().length;
		assertTrue("Class should have 2 methods but has "+i,i==2);
		
		Method mainMethod = clg.getMethods()[0];
		AnnotationGen[] annos = mainMethod.getAnnotationsOnParameter(0);
		assertTrue("Should be two annotation on the 'argv' parameter to main() but there are "+annos.length,annos.length==2);
		assertTrue("This annotation should contain the string 'fruit=Apples' but it is "+annos[0].toString(),
				annos[0].toString().contains("fruit=Apples"));
		assertTrue("This annotation should contain the string 'fruit=Oranges' but it is "+annos[1].toString(),
				annos[1].toString().contains("fruit=Oranges"));
		assertTrue(wipe("temp5","HelloWorld.class"));
		
	}

	
	
	/*
	 * Load an existing class, add new parameter annotations, save and then reload it
	 */
	public void testParameterAnnotations_loadedThenModifiedThenSavedAndLoadedOK() throws ClassNotFoundException {
		JavaClass jc = getClassFrom("testcode.jar","AnnotatedParameters");
		
		ClassGen clg = new ClassGen(jc);
		ConstantPool cpg = clg.getConstantPool();
		
		//
		// Foo method looks like this:
		//   public void foo(@SimpleAnnotation(id=2) int arg1,
        //                   @SimpleAnnotation(id=3) @AnnotationEnumElement(enumval=SimpleEnum.Red) String arg2)
		Method m = findMethod(clg,"foo");
		assertTrue("Should be able to find method foo but couldn't",m!=null);
		
		
		/////////////////////// 1. Check the right number of annotations are there
		int i = m.getAnnotationsOnParameter(1).length;
		assertTrue("Should be two annotations on the second parameter but found: "+i,i==2);
		
		
		/////////////////////// 2. Let's add a new parameter annotation, a visible one, to the first parameter.

		// Build a modifiable version of the foo method
		MethodGen mg = new MethodGen(m,clg.getClassName(),cpg);
		
		// Check the annotations survived that transform
		i = mg.getAnnotationsOnParameter(1).size();
		assertTrue("Should be two annotations on the second parameter but found: "+i,i==2);
		
		// That worked, so let's add a new parameter annotation
		mg.addParameterAnnotation(0,createFruitAnnotation(cpg,"Banana",true));
		
		// Foo method should now look like this:
		//   public void foo(@SimpleAnnotation(id=2) @SimpleStringAnnotation(fruit=Banana) int arg1,
        //                   @SimpleAnnotation(id=3) @AnnotationEnumElement(enumval=SimpleEnum.Red) String arg2)
		i = mg.getAnnotationsOnParameter(0).size();
		assertTrue("Should now be 2 parameter annotations but found "+i,i==2);
		i = mg.getAnnotationsOnParameter(0).get(1).toString().indexOf("fruit=Banana");
		assertTrue("Expected 'fruit=Banana' in the 2nd annotation on the first argument but got "+
				mg.getAnnotationsOnParameter(0).get(1).toString(),i!=-1);

		// delete the old method and add the new one
		clg.removeMethod(m);
		clg.addMethod(mg.getMethod());
	
        /////////////////////// 3. Dump it to disk
		dumpClass(clg,"temp2","AnnotatedParameters.class");
		
        /////////////////////// 4. Load it back in and verify the annotations persisted
		JavaClass jc2 = getClassFrom("temp2","AnnotatedParameters");

		m = jc2.getMethods()[2];
		AnnotationGen[] p1annotations = m.getAnnotationsOnParameter(0);
		AnnotationGen[] p2annotations = m.getAnnotationsOnParameter(1);
		
		assertTrue("Expected two annotations on the first parameter but found "+p1annotations.length,p1annotations.length==2);
		assertTrue("Expected two annotations on the second parameter but found "+p2annotations.length,p2annotations.length==2);
		String expectedString = "[@SimpleAnnotation(id=2),@SimpleStringAnnotation(fruit=Banana)]";
		assertTrue("Expected formatted short string of '"+expectedString+"' but it was '"+dumpAnnotations(p1annotations)+"'",
				dumpAnnotations(p1annotations).equals(expectedString));
		expectedString = "[@SimpleAnnotation(id=3),@AnnotationEnumElement(enumval=LSimpleEnum;Red)]";
		assertTrue("Expected formatted short string of '"+expectedString+"' but it was '"+dumpAnnotations(p2annotations)+"'",
				dumpAnnotations(p2annotations).equals(expectedString));
		
		assertTrue(wipe("temp2","AnnotatedParameters.class"));
	}
	
	
	/**
	 * same as above test but attaching invisible runtime parameter annotations
	 */ 
	public void testParameterAnnotations_loadedThenModifiedWithInvisibleAnnotationThenSavedAndLoadedOK() throws ClassNotFoundException {
		JavaClass jc = getClassFrom("testcode.jar","AnnotatedParameters");
		ClassGen clg = new ClassGen(jc);
		ConstantPool cpg = clg.getConstantPool();
		
		//
		// Foo method looks like this:
		//   public void foo(@SimpleAnnotation(id=2) int arg1,
        //                   @SimpleAnnotation(id=3) @AnnotationEnumElement(enumval=SimpleEnum.Red) String arg2)
		Method m = findMethod(clg,"foo");
		assertTrue("Should be able to find method foo but couldn't",m!=null);
		
		
		/////////////////////// 1. Check the right number of annotations are there
		int i = m.getAnnotationsOnParameter(1).length;
		assertTrue("Should be two annotations on the second parameter but found: "+i,i==2);
		
		
		/////////////////////// 2. Let's add a new parameter annotation, a visible one, to the first parameter.

		// Build a modifiable version of the foo method
		MethodGen mg = new MethodGen(m,clg.getClassName(),cpg);
		
		// Check the annotations survived that transform
		i = mg.getAnnotationsOnParameter(1).size();
		assertTrue("Should be two annotations on the second parameter but found: "+i,i==2);
		
		// That worked, so let's add a new parameter annotation
		mg.addParameterAnnotation(0,createFruitAnnotation(cpg,"Banana",false));
		
		// Foo method should now look like this:
		//   public void foo(@SimpleAnnotation(id=2) @SimpleStringAnnotation(fruit=Banana) int arg1,
        //                   @SimpleAnnotation(id=3) @AnnotationEnumElement(enumval=SimpleEnum.Red) String arg2)
		i = mg.getAnnotationsOnParameter(0).size();
		assertTrue("Should now be 2 parameter annotations but found "+i,i==2);
		i = mg.getAnnotationsOnParameter(0).get(1).toString().indexOf("fruit=Banana");
		assertTrue("Expected 'fruit=Banana' in the 2nd annotation on the first argument but got "+
				mg.getAnnotationsOnParameter(0).get(1).toString(),i!=-1);
		assertTrue("New annotation should be runtime invisible?",!((AnnotationGen)mg.getAnnotationsOnParameter(0).get(1)).isRuntimeVisible());

		// delete the old method and add the new one
		clg.removeMethod(m);
		clg.addMethod(mg.getMethod());
	
        /////////////////////// 3. Dump it to disk
		dumpClass(clg,"temp3","AnnotatedParameters.class");
		
        /////////////////////// 4. Load it back in and verify the annotations persisted

		JavaClass jc2 = getClassFrom("temp3","AnnotatedParameters");

		m = jc2.getMethods()[2];
		AnnotationGen[] p1annotations = m.getAnnotationsOnParameter(0);
		AnnotationGen[] p2annotations = m.getAnnotationsOnParameter(1);
		
		assertTrue("Expected two annotations on the first parameter but found "+p1annotations.length,p1annotations.length==2);
		assertTrue("Expected two annotations on the second parameter but found "+p2annotations.length,p2annotations.length==2);
		String expectedString = "[@SimpleAnnotation(id=2),@SimpleStringAnnotation(fruit=Banana)]";
		assertTrue("Expected formatted short string of '"+expectedString+"' but it was '"+dumpAnnotations(p1annotations)+"'",
				dumpAnnotations(p1annotations).equals(expectedString));
		expectedString = "[@SimpleAnnotation(id=3),@AnnotationEnumElement(enumval=LSimpleEnum;Red)]";
		assertTrue("Expected formatted short string of '"+expectedString+"' but it was '"+dumpAnnotations(p2annotations)+"'",
				dumpAnnotations(p2annotations).equals(expectedString));
		
		assertTrue("Second annotation on first parameter should be runtime invisible?",
				!p1annotations[1].isRuntimeVisible());
		assertTrue(wipe("temp3","AnnotatedParameters.class"));
		
		
		// 5. Verify that when annotations for parameters are unpacked from attributes, the
		//    attributes vanish !
		clg = new ClassGen(jc2);
		mg = new MethodGen(m,clg.getClassName(),clg.getConstantPool());
		List<Attribute> as = mg.getAttributes();
		assertTrue("Should be 2 (RIPA and RVPA) but there are "+mg.getAttributes().size(),mg.getAttributes().size()==2);
		List<AnnotationGen> l = mg.getAnnotationsOnParameter(0);
		assertTrue("Should be 2 annotations on first parameter but there is only "+l.size()+":"+l.toString(),
				l.size()==2);
		assertTrue("Should be 0 but there are "+mg.getAttributes().size(),mg.getAttributes().size()==0);		
	}

	
	private Method findMethod(ClassGen c,String mname) {
		Method[] ms = c.getMethods();
		for (Method m : ms) {
			if (m.getName().equals(mname)) return m;
		}
		return null;
	}	
	
	private void dumpClass(ClassGen cg, String fname) {
		try {
			File f = createTestdataFile(fname);
			cg.getJavaClass().dump(f);
		} catch (java.io.IOException e) {
			System.err.println(e);
		}
	}
	

	private void dumpClass(ClassGen cg, String dir, String fname) {
		dumpClass(cg,dir+File.separator+fname);
	}

	private void buildClassContentsWithAnnotatedMethods(ClassGen cg, ConstantPool cp, InstructionList il,boolean addParameterAnnotations) {
		// Create method 'public static void main(String[]argv)'
		MethodGen mg = createMethodGen("main",il,cp);
		InstructionFactory factory = new InstructionFactory(cg);
		mg.addAnnotation(createSimpleVisibleAnnotation(mg.getConstantPool()));
		// We now define some often used types:

		ObjectType i_stream = new ObjectType("java.io.InputStream");
		ObjectType p_stream = new ObjectType("java.io.PrintStream");

		// Create variables in and name : We call the constructors, i.e.,
		// execute BufferedReader(InputStreamReader(System.in)) . The reference
		// to the BufferedReader object stays on top of the stack and is stored
		// in the newly allocated in variable.

		il.append(factory.createNew("java.io.BufferedReader"));
		il.append(InstructionConstants.DUP); // Use predefined constant
		il.append(factory.createNew("java.io.InputStreamReader"));
		il.append(InstructionConstants.DUP);
		il.append(factory.createFieldAccess("java.lang.System", "in", i_stream,Constants.GETSTATIC));
		il.append(factory.createInvoke("java.io.InputStreamReader", "<init>",
				Type.VOID, new Type[] { i_stream }, Constants.INVOKESPECIAL));
		il.append(factory.createInvoke("java.io.BufferedReader", "<init>",
				Type.VOID, new Type[] { new ObjectType("java.io.Reader") },
				Constants.INVOKESPECIAL));

		LocalVariableGen lg = mg.addLocalVariable("in", new ObjectType(
				"java.io.BufferedReader"), null, null);
		int in = lg.getIndex();
		lg.setStart(il.append(InstructionFactory.createASTORE(in))); // "in" valid from here

		//	Create local variable name and initialize it to null

		lg = mg.addLocalVariable("name", Type.STRING, null, null);
		int name = lg.getIndex();
		il.append(InstructionConstants.ACONST_NULL);
		lg.setStart(il.append(InstructionFactory.createASTORE(name))); // "name" valid from here

		//	Create try-catch block: We remember the start of the block, read a
		// line from the standard input and store it into the variable name .

//		InstructionHandle try_start = il.append(factory.createFieldAccess(
//				"java.lang.System", "out", p_stream, Constants.GETSTATIC));

//		il.append(new PUSH(cp, "Please enter your name> "));
//		il.append(factory.createInvoke("java.io.PrintStream", "print",
//						Type.VOID, new Type[] { Type.STRING },
//						Constants.INVOKEVIRTUAL));
//		il.append(new ALOAD(in));
//		il.append(factory.createInvoke("java.io.BufferedReader", "readLine",
//				Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		InstructionHandle try_start = il.append(InstructionFactory.PUSH(cp,"Andy"));
		il.append(InstructionFactory.createASTORE(name));

		// Upon normal execution we jump behind exception handler, the target
		// address is not known yet.

		InstructionBranch g = new InstructionBranch(Constants.GOTO);
		InstructionHandle try_end = il.append(g);

		//	We add the exception handler which simply returns from the method.

		LocalVariableGen var_ex = mg.addLocalVariable("ex",Type.getType("Ljava.io.IOException;"),null,null);
		int var_ex_slot = var_ex.getIndex();
		
		InstructionHandle handler = il.append(InstructionFactory.createASTORE(var_ex_slot));
		var_ex.setStart(handler);
		var_ex.setEnd(il.append(InstructionConstants.RETURN));
		
		mg.addExceptionHandler(try_start, try_end, handler,
				new ObjectType("java.io.IOException"));

		//	"Normal" code continues, now we can set the branch target of the GOTO
		// .

		InstructionHandle ih = il.append(factory.createFieldAccess(
				"java.lang.System", "out", p_stream, Constants.GETSTATIC));
		g.setTarget(ih);

		//	Printing "Hello": String concatenation compiles to StringBuffer
		// operations.

		il.append(factory.createNew(Type.STRINGBUFFER));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.PUSH(cp, "Hello, "));
		il
				.append(factory.createInvoke("java.lang.StringBuffer",
						"<init>", Type.VOID, new Type[] { Type.STRING },
						Constants.INVOKESPECIAL));
		il.append(new InstructionLV(Constants.ALOAD,name));
		il.append(factory.createInvoke("java.lang.StringBuffer", "append",
				Type.STRINGBUFFER, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		il.append(factory.createInvoke("java.lang.StringBuffer", "toString",
				Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));

		il.append(factory.createInvoke("java.io.PrintStream", "println",
						Type.VOID, new Type[] { Type.STRING },
						Constants.INVOKEVIRTUAL));
		il.append(InstructionConstants.RETURN);

		//	Finalization: Finally, we have to set the stack size, which normally
		// would have to be computed on the fly and add a default constructor
		// method to the class, which is empty in this case.

		mg.addParameterAnnotation(0,createFruitAnnotation(cp,"Apples",true)); 
		mg.addParameterAnnotation(0,createFruitAnnotation(cp,"Oranges",true));
		mg.setMaxStack();
		mg.setMaxLocals();
		cg.addMethod(mg.getMethod());
		il.dispose(); // Allow instruction handles to be reused
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);
	}
	
	private void buildClassContents(ClassGen cg, ConstantPool cp, InstructionList il) {
		// Create method 'public static void main(String[]argv)'
		MethodGen mg = createMethodGen("main",il,cp);
		InstructionFactory factory = new InstructionFactory(cg);
		// We now define some often used types:

		ObjectType i_stream = new ObjectType("java.io.InputStream");
		ObjectType p_stream = new ObjectType("java.io.PrintStream");

		// Create variables in and name : We call the constructors, i.e.,
		// execute BufferedReader(InputStreamReader(System.in)) . The reference
		// to the BufferedReader object stays on top of the stack and is stored
		// in the newly allocated in variable.

		il.append(factory.createNew("java.io.BufferedReader"));
		il.append(InstructionConstants.DUP); // Use predefined constant
		il.append(factory.createNew("java.io.InputStreamReader"));
		il.append(InstructionConstants.DUP);
		il.append(factory.createFieldAccess("java.lang.System", "in", i_stream,Constants.GETSTATIC));
		il.append(factory.createInvoke("java.io.InputStreamReader", "<init>",
				Type.VOID, new Type[] { i_stream }, Constants.INVOKESPECIAL));
		il.append(factory.createInvoke("java.io.BufferedReader", "<init>",
				Type.VOID, new Type[] { new ObjectType("java.io.Reader") },
				Constants.INVOKESPECIAL));

		LocalVariableGen lg = mg.addLocalVariable("in", new ObjectType(
				"java.io.BufferedReader"), null, null);
		int in = lg.getIndex();
		lg.setStart(il.append(InstructionFactory.createASTORE(in))); // "in" valid from here

		//	Create local variable name and initialize it to null

		lg = mg.addLocalVariable("name", Type.STRING, null, null);
		int name = lg.getIndex();
		il.append(InstructionConstants.ACONST_NULL);
		lg.setStart(il.append(InstructionFactory.createASTORE(name))); // "name" valid from here

		//	Create try-catch block: We remember the start of the block, read a
		// line from the standard input and store it into the variable name .

//		InstructionHandle try_start = il.append(factory.createFieldAccess(
//				"java.lang.System", "out", p_stream, Constants.GETSTATIC));

//		il.append(new PUSH(cp, "Please enter your name> "));
//		il.append(factory.createInvoke("java.io.PrintStream", "print",
//						Type.VOID, new Type[] { Type.STRING },
//						Constants.INVOKEVIRTUAL));
//		il.append(new ALOAD(in));
//		il.append(factory.createInvoke("java.io.BufferedReader", "readLine",
//				Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		InstructionHandle try_start = il.append(InstructionFactory.PUSH(cp,"Andy"));
		il.append(InstructionFactory.createASTORE(name));

		// Upon normal execution we jump behind exception handler, the target
		// address is not known yet.

		InstructionBranch g = new InstructionBranch(Constants.GOTO);
		InstructionHandle try_end = il.append(g);

		//	We add the exception handler which simply returns from the method.

		LocalVariableGen var_ex = mg.addLocalVariable("ex",Type.getType("Ljava.io.IOException;"),null,null);
		int var_ex_slot = var_ex.getIndex();
		
		InstructionHandle handler = il.append(InstructionFactory.createASTORE(var_ex_slot));
		var_ex.setStart(handler);
		var_ex.setEnd(il.append(InstructionConstants.RETURN));
		
		mg.addExceptionHandler(try_start, try_end, handler,
				new ObjectType("java.io.IOException"));

		//	"Normal" code continues, now we can set the branch target of the GOTO
		// .

		InstructionHandle ih = il.append(factory.createFieldAccess(
				"java.lang.System", "out", p_stream, Constants.GETSTATIC));
		g.setTarget(ih);

		//	Printing "Hello": String concatenation compiles to StringBuffer
		// operations.

		il.append(factory.createNew(Type.STRINGBUFFER));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.PUSH(cp, "Hello, "));
		il
				.append(factory.createInvoke("java.lang.StringBuffer",
						"<init>", Type.VOID, new Type[] { Type.STRING },
						Constants.INVOKESPECIAL));
		il.append(InstructionFactory.createALOAD(name));
		il.append(factory.createInvoke("java.lang.StringBuffer", "append",
				Type.STRINGBUFFER, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		il.append(factory.createInvoke("java.lang.StringBuffer", "toString",
				Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));

		il.append(factory.createInvoke("java.io.PrintStream", "println",
						Type.VOID, new Type[] { Type.STRING },
						Constants.INVOKEVIRTUAL));
		il.append(InstructionConstants.RETURN);

		//	Finalization: Finally, we have to set the stack size, which normally
		// would have to be computed on the fly and add a default constructor
		// method to the class, which is empty in this case.

		mg.setMaxStack();
		mg.setMaxLocals();
		cg.addMethod(mg.getMethod());
		il.dispose(); // Allow instruction handles to be reused
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);
	}

	private JavaClass getClassFrom(String where,String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(where);
		return repos.loadClass(clazzname);
	}
	
	
	

	// helper methods
	

	private ClassGen createClassGen(String classname) {
		return new ClassGen(classname, "java.lang.Object",
				"<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
	}
	
	private MethodGen createMethodGen(String methodname,InstructionList il,ConstantPool cp) {
		return new MethodGen(
				Constants.ACC_STATIC | Constants.ACC_PUBLIC,  // access flags
				Type.VOID,                                    // return type
				new Type[] { new ArrayType(Type.STRING, 1) }, // argument types
				new String[] { "argv" },                      // arg names
				methodname, "HelloWorld",                     // method, class
				il, cp);
	}

	
	public AnnotationGen createSimpleVisibleAnnotation(ConstantPool cp) {
		SimpleElementValue evg = new SimpleElementValue(
				ElementValue.PRIMITIVE_INT, cp, 4);

		NameValuePair nvGen = new NameValuePair("id", evg,cp);

		ObjectType t = new ObjectType("SimpleAnnotation");

		List<NameValuePair> elements = new ArrayList<>();
		elements.add(nvGen);

		AnnotationGen a = new AnnotationGen(t, elements,true, cp);
		return a;
	}
		
	public AnnotationGen createCombinedAnnotation(ConstantPool cp) {
		// Create an annotation instance
		AnnotationGen a = createSimpleVisibleAnnotation(cp);
		ArrayElementValue array = new ArrayElementValue(cp);
		array.addElement(new AnnotationElementValue(a,cp)); 
		NameValuePair nvp = new NameValuePair("value",array,cp);
		List<NameValuePair> elements = new ArrayList<>();
		elements.add(nvp);
		return new AnnotationGen(new ObjectType("CombinedAnnotation"),elements,true,cp);
	}
	
	public AnnotationGen createSimpleInvisibleAnnotation(ConstantPool cp) {
		SimpleElementValue evg = new SimpleElementValue(
				ElementValue.PRIMITIVE_INT, cp, 4);

		NameValuePair nvGen = new NameValuePair("id", evg,cp);

		ObjectType t = new ObjectType("SimpleAnnotation");

		List<NameValuePair> elements = new ArrayList<>();
		elements.add(nvGen);

		AnnotationGen a = new AnnotationGen(t, elements,false, cp);
		return a;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}