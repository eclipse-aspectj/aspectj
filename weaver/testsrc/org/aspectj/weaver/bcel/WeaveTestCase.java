/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.zip.ZipFile;

import junit.framework.*;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.FileUtil;

public abstract class WeaveTestCase extends TestCase {

	public boolean regenerate = false;
	public boolean runTests = true;
	
	public BcelWorld world = new BcelWorld();

    public WeaveTestCase(String name) {
        super(name);
    }
    
	public static InstructionList getAdviceTag(BcelShadow shadow, String where) {
		String methodName =
			"ajc_" + where + "_" + shadow.getKind().toLegalJavaIdentifier();

		InstructionFactory fact = shadow.getFactory();
		InvokeInstruction il =
			fact.createInvoke("Aspect", methodName, Type.VOID, new Type[] {
		}, Constants.INVOKESTATIC);
        return new InstructionList(il);
	}
    
	public void weaveTest(String name, String outName, ShadowMunger planner) throws IOException {
        List l = new ArrayList(1);
        l.add(planner);
        weaveTest(name, outName, l);
    }
    
    static String classDir = "../weaver/bin";
    static String outDir = "out";
    
    
	public void weaveTest(String name, String outName, List planners) throws IOException {
        BcelWeaver weaver = new BcelWeaver(world);
        
        UnwovenClassFile classFile = makeUnwovenClassFile(classDir, name, outDir);
        
        weaver.addClassFile(classFile);
        weaver.setShadowMungers(planners);
        weaveTestInner(weaver, classFile, name, outName);
	}
        
        
	protected void weaveTestInner(
		BcelWeaver weaver,
		UnwovenClassFile classFile,
		String name,
		String outName)
		throws IOException 
	{
		//int preErrors = currentResult.errorCount();
		BcelObjectType classType =
			(BcelObjectType) world.resolve(classFile.getClassName());
		LazyClassGen gen = weaver.weave(classFile, classType);
		if (gen == null) {
			// we didn't do any weaving, but let's make a gen anyway
			gen = new LazyClassGen(classType);
		}
		try {
			checkClass(gen, outDir, outName + ".txt");
			if (runTests) {
				System.out.println(
					"*******RUNNING: " + outName + "  " + name + " *******");
				TestUtil.runMain(makeClassPath(outDir), name);
			}
		} catch (Error e) {
			gen.print(System.err);
			throw e;
		} catch (RuntimeException e) {
			gen.print(System.err);
			throw e;
		}
	}
   	
   	public String makeClassPath(String outDir) {
   		return outDir
			+ File.pathSeparator
			+ getTraceJar() 
			+ File.pathSeparator
			+ System.getProperty("java.class.path");
   	}
   	

	/** '/' in the name indicates the location of the class
	 */
	public static UnwovenClassFile makeUnwovenClassFile(
		String classDir,
		String name,
		String outDir) throws IOException {
		File outFile = new File(outDir, name+".class");
		if (classDir.endsWith(".jar")) {
			String fname = name+".class";
			UnwovenClassFile ret =
				 new UnwovenClassFile(outFile.getAbsolutePath(), 
				 	FileUtil.readAsByteArray(FileUtil.getStreamFromZip(classDir, fname)));
		    return ret;
		} else {
			File inFile = new File(classDir, name+".class");
			return new UnwovenClassFile(outFile.getAbsolutePath(), FileUtil.readAsByteArray(inFile));
		}
	}

    public void checkClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
        if (regenerate) genClass(gen, outDir, expectedFile);
        else realCheckClass(gen, outDir, expectedFile);
    }
    				
    void genClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
    	//ClassGen b = getJavaClass(outDir, className);
    	FileOutputStream out = new FileOutputStream(new File("testdata", expectedFile));
    	PrintStream ps = new PrintStream(out);
    	gen.print(ps);
    	ps.flush();
				
    }

    void realCheckClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
    	TestUtil.assertMultiLineStringEquals("classes", 
    	             FileUtil.readAsString(new File("testdata", expectedFile)),
    	             gen.toLongString());
    }


	// ----
    public ShadowMunger makeConcreteAdvice(String mungerString) {
    	return makeConcreteAdvice(mungerString, 0, null);
    }

    public ShadowMunger makeConcreteAdvice(String mungerString, int extraArgFlag) {
		return makeConcreteAdvice(mungerString, extraArgFlag, null);
    }

    protected ShadowMunger makeConcreteAdvice(String mungerString, int extraArgFlag, PerClause perClause) {
        Advice myMunger = 
            world.shadowMunger(mungerString, extraArgFlag);
            
//        PerSingleton s = new PerSingleton();
//        s.concretize(world.resolve("Aspect"));
        //System.err.println(((KindedPointcut)myMunger.getPointcut().getPointcut()).getKind());
        Advice cm = (Advice) myMunger.concretize(myMunger.getDeclaringAspect().resolve(world), 
        						world, perClause);
        return cm;
    }

    public ShadowMunger makeAdviceField(String kind, String extraArgType) {
        return makeConcreteAdvice(
            kind
                + "(): get(* *.*) -> static void Aspect.ajc_"
                + kind
                + "_field_get("
                + extraArgType
                + ")",
            1);
    }

    public List makeAdviceAll(String kind, boolean matchOnlyPrintln) {
        List ret = new ArrayList();
        if (matchOnlyPrintln) {
            ret.add(
                makeConcreteAdvice(
                    kind
                        + "(): call(* *.println(..)) -> static void Aspect.ajc_"
                        + kind
                        + "_method_execution()"));
        } else {
            ret.add(
                makeConcreteAdvice(
                    kind
                        + "(): call(* *.*(..)) -> static void Aspect.ajc_"
                        + kind
                        + "_method_call()"));
            ret.add(
                makeConcreteAdvice(
                    kind
                        + "(): call(*.new(..)) -> static void Aspect.ajc_"
                        + kind
                        + "_constructor_call()"));
            ret.add(
                makeConcreteAdvice(
                    kind
                        + "(): execution(* *.*(..)) -> static void Aspect.ajc_"
                        + kind
                        + "_method_execution()"));
            ret.add(
                makeConcreteAdvice(
                    kind
                        + "(): execution(*.new(..)) -> static void Aspect.ajc_"
                        + kind
                        + "_constructor_execution()"));
//            ret.add(
//                makeConcreteMunger(
//                    kind
//                        + "(): staticinitialization(*) -> static void Aspect.ajc_"
//                        + kind
//                        + "_staticinitialization()"));
            ret.add(
                makeConcreteAdvice(
                    kind + "(): get(* *.*) -> static void Aspect.ajc_" + kind + "_field_get()"));
//            ret.add(
//                makeConcreteMunger(
//                    kind + "(): set(* *.*) -> static void Aspect.ajc_" + kind + "_field_set()"));
			// XXX no test for advice execution, staticInitialization or (god help us) preInitialization
        }
        return ret;
    }
    
    public List makeAdviceAll(final String kind) {
        return makeAdviceAll(kind, false);
    }

	public Pointcut makePointcutAll() {
		return makeConcretePointcut("get(* *.*) || call(* *.*(..)) || execution(* *.*(..)) || call(*.new(..)) || execution(*.new(..))");
	}
	public Pointcut makePointcutNoZeroArg() {
		return makeConcretePointcut("call(* *.*(*, ..)) || execution(* *.*(*, ..)) || call(*.new(*, ..)) || execution(*.new(*, ..))");
	}

	public Pointcut makePointcutPrintln() {
		return makeConcretePointcut("call(* *.println(..))");
	}
	
	
	public Pointcut makeConcretePointcut(String s) {
		return makeResolvedPointcut(s).concretize(null, 0);
	}
	
	public Pointcut makeResolvedPointcut(String s) {
		Pointcut pointcut0 = Pointcut.fromString(s);
		return pointcut0.resolve(new SimpleScope(world, FormalBinding.NONE));
	}


	// ----

	public String[] getStandardTargets() {
		return new String[] {"HelloWorld", "FancyHelloWorld"};
	}

	public String getTraceJar() {
		return "testdata/tracing.jar";
	}

	// ----

	protected void weaveTest(
    		String[] inClassNames,
    		String outKind,
    		ShadowMunger patternMunger) throws IOException {
		for (int i = 0; i < inClassNames.length; i++) {
			String inFileName = inClassNames[i];
			weaveTest(inFileName, outKind + inFileName, patternMunger);
		}
	}
    protected void weaveTest(
            String[] inClassNames,
            String outKind,
            List patternMungers) throws IOException {
        for (int i = 0; i < inClassNames.length; i++) {
            String inFileName = inClassNames[i];
            weaveTest(inFileName, outKind + inFileName, patternMungers);
        }
    }

	protected List addLexicalOrder(List l) {
		int i = 10;
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Advice element = (Advice) iter.next();
			element.setLexicalPosition(i+=10);
		}
		return l;
	}

	//XXX cut-and-paster from IdWeaveTestCase
    public void checkShadowSet(List l, String[] ss) {
    	outer:
    	for (int i = 0, len = ss.length; i < len; i++) {
    		inner:
    		for (Iterator j = l.iterator(); j.hasNext(); ) {
    			BcelShadow shadow = (BcelShadow) j.next();
    			String shadowString = shadow.toString();
    			if (shadowString.equals(ss[i])) {
    				j.remove();
    				continue outer;
    			}
    		}
    		assertTrue("didn't find " + ss[i] + " in " + l, false);
    	}
    	assertTrue("too many things in " + l, l.size() ==  0);
    }



    

}

