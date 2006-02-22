/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.weaver.AbstractReferenceTypeDelegate;
import org.aspectj.weaver.AbstractWorldTestCase;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BcweaverTests;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.EffectiveSignatureAttribute;
import org.aspectj.weaver.asm.AsmDelegate;
import org.aspectj.weaver.asm.AsmField;
import org.aspectj.weaver.asm.AsmMethod;


/**
 * This is a test case for the nameType parts of worlds.
 */
public class AsmDelegateTests extends AbstractWorldTestCase {

    private final BcelWorld world = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");

    public AsmDelegateTests(String name) {
        super(name);
    }

	protected World getWorld() {
		return world;
	}
	
	// --- testcode

    public void testWeDontGoBang() {
    	ResolvedType rt = world.resolve("SimpleAspect");
    	ReferenceTypeDelegate delegate = ((ReferenceType)rt).getDelegate();
    	assertTrue("Should be an ASM delegate but is "+delegate.getClass(),
    			   delegate.getClass().toString().equals("class org.aspectj.weaver.asm.AsmDelegate"));
    }
    
    public void testDifferentiatingBetweenAspectAndClass() {
    	ReferenceType rtAspect = (ReferenceType)world.resolve("SimpleAspect");
    	ReferenceType rtString = (ReferenceType)world.resolve("java.lang.String");
    	assertTrue("SimpleAspect should be an aspect",rtAspect.isAspect());
    	assertTrue("String should not be an aspect",!rtString.isAspect());
    	assertTrue("Should be a persingleton "+rtAspect.getPerClause(),rtAspect.getPerClause().toString().startsWith("persingleton"));
    }
    
    public void testRecognizingDifferentTypes() {
    	ResolvedType rtAnnotation = world.resolve("SimpleAnnotation");
    	ResolvedType rtEnum = world.resolve("SimpleEnum");
    	ResolvedType rtString = world.resolve("java.lang.String");
    	assertTrue("Should be an annotation type",rtAnnotation.isAnnotation());
    	assertTrue("Should be an enum type",rtEnum.isEnum());
    	assertTrue("Should not be an annotation or enum type",!(rtString.isAnnotation() || rtString.isEnum()));
    }
    
    public void testAnnotationsBehaving() {
    	ReferenceType rtAnnotation = (ReferenceType)world.resolve("SimpleAnnotation");
    	assertTrue("Should be SOURCE but is "+rtAnnotation.getRetentionPolicy(),rtAnnotation.getRetentionPolicy().equals("SOURCE"));
    	ReferenceType rtAnnotation2 = (ReferenceType)world.resolve("SimpleAnnotation2");
    	assertTrue("Should be CLASS but is "+rtAnnotation2.getRetentionPolicy(),rtAnnotation2.getRetentionPolicy().equals("CLASS"));
    	ReferenceType rtAnnotation3 = (ReferenceType)world.resolve("SimpleAnnotation3");
    	assertTrue("Should be RUNTIME but is "+rtAnnotation3.getRetentionPolicy(),rtAnnotation3.getRetentionPolicy().equals("RUNTIME"));
    	ReferenceType rtAnnotation4 = (ReferenceType)world.resolve("SimpleAnnotation4");
    	assertTrue("Should be CLASS but is "+rtAnnotation4.getRetentionPolicy(),rtAnnotation4.getRetentionPolicy().equals("CLASS"));    	
    }
    
    public void testInterfaceflag() {
    	ReferenceType rtString = (ReferenceType)world.resolve("java.lang.String");
    	assertTrue("String should not be an interface",!rtString.isInterface());  	
    	ReferenceType rtSerializable = (ReferenceType)world.resolve("java.io.Serializable");
    	assertTrue("Serializable should be an interface",rtSerializable.isInterface());  	
    }
    
    public void testCompareDelegates() {
      BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
      slowWorld.setFastDelegateSupport(false);
      BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
      
      ReferenceType bcelList = (ReferenceType)slowWorld.resolve("java.util.List");
      ReferenceType asmList = (ReferenceType)fastWorld.resolve("java.util.List");
      
      assertTrue("Should be a bcel delegate? "+bcelList.getDelegate().getClass(),
    		  bcelList.getDelegate().getClass().toString().equals("class org.aspectj.weaver.bcel.BcelObjectType"));
      assertTrue("Should be an asm delegate? "+asmList.getDelegate().getClass(),
    		  asmList.getDelegate().getClass().toString().equals("class org.aspectj.weaver.asm.AsmDelegate"));
      
      TypeVariable[] bcelTVars = bcelList.getTypeVariables();
      
      TypeVariable[] asmTVars = asmList.getTypeVariables();
      for (int i = 0; i < asmTVars.length; i++) {
		TypeVariable variable = asmTVars[i];
	  }
      
      String bcelsig = bcelList.getSignature();
      String asmsig  = asmList.getSignature();
      
      assertTrue("Signatures should be the same but "+bcelsig+"!="+asmsig,bcelsig.equals(asmsig));
      
      String bcelerasuresig = bcelList.getErasureSignature();
      String asmerasuresig = asmList.getErasureSignature();
      assertTrue("Erasure Signatures should be the same but "+bcelerasuresig+"!="+asmerasuresig,bcelerasuresig.equals(asmerasuresig));
      
      ResolvedMember[] bcelfields = bcelList.getDeclaredFields();
      ResolvedMember[] asmfields = asmList.getDeclaredFields();
      if (bcelfields.length!=asmfields.length) {
    	  fail("Dont have the same number of fields? bcel="+bcelfields.length+" asm="+asmfields.length);
      }
      for (int i = 0; i < asmfields.length; i++) {
		ResolvedMember member = asmfields[i];
		if (!bcelfields[i].equals(asmfields[i])) {
			fail("Differing fields: "+bcelfields[i]+"  and "+asmfields[i]);
		}
	  }
    }
    
    public void testCompareDelegatesComplex() {
        BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        
        ReferenceType bComplex = (ReferenceType)slowWorld.resolve("Complex");
        ReferenceType aComplex = (ReferenceType)fastWorld.resolve("Complex");
        
        checkEquivalent("",(AbstractReferenceTypeDelegate)aComplex.getDelegate(),(AbstractReferenceTypeDelegate)bComplex.getDelegate());
    }
    
    /**
     * Methods are transformed according to generic signatures - this checks 
     * that some of the generic methods in java.lang.Class appear the same 
     * whether viewed through an ASM or a BCEL delegate.
     */
    public void testCompareGenericMethods() {
        BcelWorld slowWorld = new BcelWorld();
        slowWorld.setFastDelegateSupport(false);
        slowWorld.setBehaveInJava5Way(true);
        
        BcelWorld fastWorld = new BcelWorld();
        fastWorld.setBehaveInJava5Way(true);

        ResolvedType bcelJavaLangClass = slowWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        ResolvedType  asmJavaLangClass = fastWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        
        bcelJavaLangClass = bcelJavaLangClass.getGenericType();
        asmJavaLangClass  = asmJavaLangClass.getGenericType();
        
    	//if (bcelJavaLangClass == null) return;  // for < 1.5
    	
    	ResolvedMember[] bcelMethods = bcelJavaLangClass.getDeclaredMethods();
    	ResolvedMember[]  asmMethods = asmJavaLangClass.getDeclaredMethods();
    	
    	for (int i = 0; i < bcelMethods.length; i++) {
    		bcelMethods[i].setParameterNames(null); // forget them, asm delegates dont currently know them
    		String one = bcelMethods[i].toDebugString();
    		String two = asmMethods[i].toDebugString();
    		if (!one.equals(two)) {
    			fail("These methods look different when viewed through ASM or BCEL\nBCEL='"+bcelMethods[i].toDebugString()+
    				 "'\n ASM='"+asmMethods[i].toDebugString()+"'");
    		}
    		// If one is parameterized, check the other is...
    		if (bcelMethods[i].canBeParameterized()) {
    			assertTrue("ASM method '"+asmMethods[i].toDebugString()+"' can't be parameterized whereas its' BCEL variant could",
    				       asmMethods[i].canBeParameterized());
    		}
			
		}
    	
    	// Let's take a special look at:
    	//   public <U> Class<? extends U> asSubclass(Class<U> clazz)
    	ResolvedMember bcelSubclassMethod = null;
    	for (int i = 0; i < bcelMethods.length; i++) {
			if (bcelMethods[i].getName().equals("asSubclass")) { bcelSubclassMethod = bcelMethods[i]; break; }
		}
    	ResolvedMember asmSubclassMethod = null;
    	for (int i = 0; i < asmMethods.length; i++) {
			if (asmMethods[i].getName().equals("asSubclass")) { asmSubclassMethod = asmMethods[i];break;	}
		}
    	
    	TypeVariable[] tvs = bcelSubclassMethod.getTypeVariables();
    	assertTrue("should have one type variable on the bcel version but found: "+format(tvs),tvs!=null && tvs.length==1);
        tvs = asmSubclassMethod.getTypeVariables();
    	assertTrue("should have one type variable on the asm version but found: "+format(tvs),tvs!=null && tvs.length==1);

    }
    
    private String format(TypeVariable[] tvs) {
    	if (tvs==null) return "null";
    	StringBuffer s = new StringBuffer();
    	s.append("[");
    	for (int i = 0; i < tvs.length; i++) {
			s.append(tvs[i]);
			if ((i+1)<tvs.length) s.append(",");
		}
    	s.append("]");
    	return s.toString();
    }
    
    public void testCompareGenericFields() {
        BcelWorld slowWorld = new BcelWorld();
        slowWorld.setFastDelegateSupport(false);
        slowWorld.setBehaveInJava5Way(true);
        
        BcelWorld fastWorld = new BcelWorld();
        fastWorld.setBehaveInJava5Way(true);

        ResolvedType bcelJavaLangClass = slowWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        ResolvedType  asmJavaLangClass = fastWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        
        bcelJavaLangClass = bcelJavaLangClass.getGenericType();
        asmJavaLangClass = asmJavaLangClass.getGenericType();
        
    	if (bcelJavaLangClass == null) return;  // for < 1.5
    	
    	ResolvedMember[] bcelFields = bcelJavaLangClass.getDeclaredFields();
    	ResolvedMember[]  asmFields = asmJavaLangClass.getDeclaredFields();
    	
    	for (int i = 0; i < bcelFields.length; i++) {
    		UnresolvedType bcelFieldType = bcelFields[i].getGenericReturnType();
    		UnresolvedType asmFieldType = asmFields[i].getGenericReturnType();
    		if (!bcelFields[i].getGenericReturnType().toDebugString().equals(asmFields[i].getGenericReturnType().toDebugString())) {
    			fail("These fields look different when viewed through ASM or BCEL\nBCEL='"+bcelFieldType.toDebugString()+
    				 "'\n ASM='"+asmFieldType.toDebugString()+"'");
    		}
		}
    }
    
    public void testCompareDelegatesMonster() {
        BcelWorld slowWorld = new BcelWorld("../lib/aspectj/lib/aspectjtools.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld("../lib/aspectj/lib/aspectjtools.jar");
        
        ResolvedMemberImpl.showParameterNames=false;
        try {
        	File f = new File("../lib/aspectj/lib/aspectjtools.jar");
        	assertTrue("Couldnt find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
			ZipFile zf = new ZipFile(f);
			int i = 0;
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zfe = (ZipEntry)entries.nextElement();
				String classfileName = zfe.getName();
				if (classfileName.endsWith(".class")) {
					String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
			        ReferenceType b = (ReferenceType)slowWorld.resolve(clazzname);
			        ReferenceType a = (ReferenceType)fastWorld.resolve(clazzname);
			        checkEquivalent("Comparison number #"+(i++)+" ",(AbstractReferenceTypeDelegate)a.getDelegate(),(AbstractReferenceTypeDelegate)b.getDelegate());
				}
			}
			//System.err.println();("Successfully compared "+i+" entries!!");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    public void testCompareDelegatesLoadingPerformance() {
        BcelWorld slowWorld = new BcelWorld("../lib/aspectj/lib/aspectjtools.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld("../lib/aspectj/lib/aspectjtools.jar");
        
        ResolvedMemberImpl.showParameterNames=false;
        try {
        	File f = new File("../lib/aspectj/lib/aspectjtools.jar");
        	assertTrue("Couldnt find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
			ZipFile zf = new ZipFile(f);
			int i = 0;
			long stime = System.currentTimeMillis();
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zfe = (ZipEntry)entries.nextElement();
				String classfileName = zfe.getName();
				if (classfileName.endsWith(".class")) {
					String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
			        ReferenceType b = (ReferenceType)slowWorld.resolve(clazzname);
			        i++;
				}
			}
			long etime = System.currentTimeMillis();
			System.err.println("Time taken to load "+i+" entries with BCEL="+(etime-stime)+"ms");
			//System.err.println();("Successfully compared "+i+" entries!!");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
        try {
        	File f = new File("../lib/aspectj/lib/aspectjtools.jar");
        	assertTrue("Couldnt find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
			ZipFile zf = new ZipFile(f);
			int i = 0;
			long stime = System.currentTimeMillis();
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zfe = (ZipEntry)entries.nextElement();
				String classfileName = zfe.getName();
				if (classfileName.endsWith(".class")) {
					String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
			        ReferenceType b = (ReferenceType)fastWorld.resolve(clazzname);
			        i++;
				}
			}
			long etime = System.currentTimeMillis();
			System.err.println("Time taken to load "+i+" entries with  ASM="+(etime-stime)+"ms");
			//System.err.println();("Successfully compared "+i+" entries!!");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    private void checkEquivalent(String prefix,AbstractReferenceTypeDelegate asmType,AbstractReferenceTypeDelegate bcelType) {
        assertTrue("Should be a bcel delegate? "+bcelType.getClass(),
      		  bcelType.getClass().toString().equals("class org.aspectj.weaver.bcel.BcelObjectType"));
        assertTrue("Should be an asm delegate? "+asmType.getClass(),
      		  asmType.getClass().toString().equals("class org.aspectj.weaver.asm.AsmDelegate"));
        
        String asmString = asmType.stringifyDelegate();
        String bcelString= bcelType.stringifyDelegate();

        if (!asmString.equals(bcelString)) {
        	fail(prefix+"Delegates don't match for "+bcelType.getResolvedTypeX()+"\n  ASM=\n"+asmString+"\n  BCEL=\n"+bcelString);
        }
      }
    
    private void compareAnnotations(String n,World bcelWorld,World asmWorld) {
    	ReferenceType bcelT = (ReferenceType)bcelWorld.resolve(n);
    	ReferenceType  asmT = (ReferenceType)asmWorld.resolve(n);
    	ensureTheSame(bcelT.getAnnotations(),asmT.getAnnotations());
    }
    
    private void ensureTheSame(AnnotationX[] bcelSet,AnnotationX[] asmSet) {
    	String bcelString = stringify(bcelSet);
    	String  asmString = stringify(asmSet);
    	if (bcelSet.length!=asmSet.length) {
    		fail("Lengths are different!!! Not a good start. \nBcel reports: \n"+bcelString+" Asm reports: \n"+asmString);
    	}
    	assertTrue("Different answers. \nBcel reports: \n"+bcelString+" Asm reports: \n"+asmString,bcelString.equals(asmString));
    }
    
    public String stringify(AnnotationX[] annotations) {
    	if (annotations==null) return "";
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < annotations.length; i++) {
			AnnotationX annotationX = annotations[i];
			sb.append(i+") "+annotationX.toString());
			sb.append("\n");
		}
    	return sb.toString();
    }

    public void testDifferentAnnotationKinds() {
        BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
    	compareAnnotations("AnnotatedClass",slowWorld,fastWorld);
    	compareAnnotations("AnnotatedFields",slowWorld,fastWorld);
    	compareAnnotations("AnnotatedMethods",slowWorld,fastWorld);
    	compareAnnotations("AnnotatedWithClassClass",slowWorld,fastWorld);
    	compareAnnotations("AnnotatedWithCombinedAnnotation",slowWorld,fastWorld);
    	compareAnnotations("AnnotatedWithEnumClass",slowWorld,fastWorld);
    	compareAnnotations("AnnotationClassElement",slowWorld,fastWorld);
    	compareAnnotations("AnnotationEnumElement",slowWorld,fastWorld);
    	compareAnnotations("ComplexAnnotation",slowWorld,fastWorld);
    	compareAnnotations("CombinedAnnotation",slowWorld,fastWorld);
    	compareAnnotations("ComplexAnnotatedClass",slowWorld,fastWorld);
    }
    
    /**
     * Load up the AspectFromHell and take it apart...
     */
    public void testLoadingAttributesForTypes() {
    	BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        
        ReferenceType bcelT = (ReferenceType)slowWorld.resolve("AspectFromHell");
        ReferenceType asmT  = (ReferenceType)fastWorld.resolve("AspectFromHell");
        
        AsmDelegate asmD = (AsmDelegate)asmT.getDelegate();
        String [] asmAttributeNames = asmD.getAttributeNames();
        BcelObjectType bcelD = (BcelObjectType)bcelT.getDelegate();
        String [] bcelAttributeNames = bcelD.getAttributeNames();
        
        // Won't be exactly the same number as ASM currently processes some and then discards them - effectively those stored in the delegate
        // are the 'not yet processed' ones
        
        // should be 6 type mungers
        AjAttribute[] asmTypeMungers = asmD.getAttributes("org.aspectj.weaver.TypeMunger");
        AjAttribute[] bcelTypeMungers = bcelD.getAttributes("org.aspectj.weaver.TypeMunger");
        assertTrue("Should be 6 type mungers but asm="+asmTypeMungers.length+" bcel="+bcelTypeMungers.length,asmTypeMungers.length==6 && bcelTypeMungers.length==6);
    }
    
    public void testLoadingAttributesForMethods() {
    	boolean debug = false;
    	BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        
        ReferenceType bcelT = (ReferenceType)slowWorld.resolve("AspectFromHell");
        ReferenceType asmT  = (ReferenceType)fastWorld.resolve("AspectFromHell");
        
        ResolvedMember[] bcelMethods = bcelT.getDeclaredMethods();
        ResolvedMember[] asmMethods = asmT.getDeclaredMethods(); 
        for (int i = 0; i < bcelMethods.length; i++) {
			BcelMethod bmember = (BcelMethod)bcelMethods[i];
			AsmMethod  amember = (AsmMethod)asmMethods[i];
			assertTrue("Seem to be in a muddle. a="+amember.getName()+" b="+bmember.getName(),
					   amember.getName().equals(bmember.getName()));
			if (debug) System.err.println("Looking at "+bmember);
			String[] bcelMemberAttributes = bmember.getAttributeNames(true);
			String[] asmMemberAttributes  = amember.getAttributeNames(true);
//			System.err.println("BCEL=>\n"+stringifyStringArray(bcelMemberAttributes));
//			System.err.println(" ASM=>\n"+stringifyStringArray(asmMemberAttributes));
			compareAttributeNames(bcelMemberAttributes,asmMemberAttributes);
			
			// Let's check the member ones of interest:
			
			// org.aspectj.weaver.AjSynthetic
			if (bmember.isAjSynthetic()) {
				assertTrue("Why isnt the ASM method ajsynthetic? "+amember.toDebugString(),amember.isAjSynthetic());
			} else {
				assertTrue("Why is the ASM method ajsynthetic? "+amember.toDebugString(),!amember.isAjSynthetic());
			}
			
			// org.aspectj.weaver.EffectiveSignature
			EffectiveSignatureAttribute bcelEsa = bmember.getEffectiveSignature();
			EffectiveSignatureAttribute asmEsa = amember.getEffectiveSignature();
			if (bcelEsa==null) {
				assertTrue("Why isnt the ASM effective signature null? "+asmEsa,asmEsa==null);
			} else {
				if (asmEsa==null) fail("ASM effective signature is null, when BCEL effective signature is "+bcelEsa.toString());
				assertTrue("Should be the same?? b="+bcelEsa.toString()+" a="+asmEsa.toString(),bcelEsa.toString().equals(asmEsa.toString()));
			}
			
			// org.aspectj.weaver.MethodDeclarationLineNumber
			int bLine = bmember.getDeclarationLineNumber();
			int aLine = amember.getDeclarationLineNumber();
			assertTrue("Should be the same number: "+bLine+" "+aLine,bLine==aLine);


			// org.aspectj.weaver.Advice
			ShadowMunger bcelSM = bmember.getAssociatedShadowMunger();
			ShadowMunger asmSM = amember.getAssociatedShadowMunger();
			if (bcelSM==null) {
				assertTrue("Why isnt the ASM effective signature null? "+asmSM,asmSM==null);
			} else {
				if (asmSM==null) fail("ASM effective signature is null, when BCEL effective signature is "+bcelSM.toString());
				assertTrue("Should be the same?? b="+bcelSM.toString()+" a="+asmSM.toString(),bcelSM.toString().equals(asmSM.toString()));
			}
//	          new AjASMAttribute("org.aspectj.weaver.SourceContext"),
		}
        
    }

    // @SimpleAnnotation(id=1) int i;
    // @SimpleAnnotation(id=2) String s;
    public void testLoadingAnnotationsForFields() {
    	boolean debug = false;
    	BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        
        ReferenceType bcelT = (ReferenceType)slowWorld.resolve("AnnotatedFields");
        ReferenceType asmT  = (ReferenceType)fastWorld.resolve("AnnotatedFields");
        
        ResolvedMember[] bcelFields  = bcelT.getDeclaredFields();
        ResolvedMember[] asmFields = asmT.getDeclaredFields();
        for (int i = 0; i < bcelFields.length; i++) {
			BcelField bmember = (BcelField)bcelFields[i];
			AsmField  amember = (AsmField)asmFields[i];
			assertTrue("Seem to be in a muddle. a="+amember.getName()+" b="+bmember.getName(),
					   amember.getName().equals(bmember.getName()));
			if (debug) System.err.println("Looking at "+bmember);
			
			ResolvedType[] bAnns = bmember.getAnnotationTypes();
			ResolvedType[] aAnns = amember.getAnnotationTypes();
			assertTrue("Should have found an annotation on the bcel field?",bAnns!=null && bAnns.length==1);
			assertTrue("Should have found an annotation on the asm field?",aAnns!=null && aAnns.length==1);
			
			assertTrue("BCEL annotation should be 'SimpleAnnotation3' but is "+bAnns[0].toString(),bAnns[0].toString().equals("SimpleAnnotation3"));
			assertTrue("ASM annotation should be 'SimpleAnnotation3' but is "+aAnns[0].toString(),aAnns[0].toString().equals("SimpleAnnotation3"));
			
			AnnotationX[] bXs = bmember.getAnnotations();
			AnnotationX[] aXs = amember.getAnnotations();
			assertTrue("Should have found an annotation on the bcel field?",bXs!=null && bXs.length==1);
			assertTrue("Should have found an annotation on the asm field?",aXs!=null && aXs.length==1);

			String exp = null;
			if (i==0) exp =	"ANNOTATION [LSimpleAnnotation3;] [runtimeVisible] [id=1]";
			else if (i==1) exp="ANNOTATION [LSimpleAnnotation3;] [runtimeVisible] [id=2]";
			assertTrue("BCEL annotation should be '"+exp+"' but is "+bXs[0].toString(),bXs[0].toString().equals(exp));
			assertTrue("ASM annotation should be '"+exp+"' but is "+aXs[0].toString(),aXs[0].toString().equals(exp));
		}
    }
    
	//    @SimpleAnnotation(id=1)
	//    public void method1() {    }
	//
	//    @SimpleAnnotation(id=2)
	//    public void method2() {    }
    public void testLoadingAnnotationsForMethods() {
    
    	boolean debug = false;
    	BcelWorld slowWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");slowWorld.setFastDelegateSupport(false);
        BcelWorld fastWorld = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");
        
        ReferenceType bcelT = (ReferenceType)slowWorld.resolve("AnnotatedMethods");
        ReferenceType asmT  = (ReferenceType)fastWorld.resolve("AnnotatedMethods");
        
        ResolvedMember[] bcelMethods  = bcelT.getDeclaredMethods();
        ResolvedMember[] asmMethods = asmT.getDeclaredMethods();
        for (int i = 0; i < bcelMethods.length; i++) {
        	
			BcelMethod bmember = (BcelMethod)bcelMethods[i];
			AsmMethod  amember = (AsmMethod)asmMethods[i];
			if (!bmember.getName().startsWith("method")) continue;
			assertTrue("Seem to be in a muddle. a="+amember.getName()+" b="+bmember.getName(),
					   amember.getName().equals(bmember.getName()));
			if (debug) System.err.println("Looking at "+bmember);
			
			ResolvedType[] bAnns = bmember.getAnnotationTypes();
			ResolvedType[] aAnns = amember.getAnnotationTypes();
			assertTrue("Should have found an annotation on the bcel method?",bAnns!=null && bAnns.length==1);
			assertTrue("Should have found an annotation on the asm method?",aAnns!=null && aAnns.length==1);
			
			assertTrue("BCEL annotation should be 'SimpleAnnotation3' but is "+bAnns[0].toString(),bAnns[0].toString().equals("SimpleAnnotation3"));
			assertTrue("ASM annotation should be 'SimpleAnnotation3' but is "+aAnns[0].toString(),aAnns[0].toString().equals("SimpleAnnotation3"));
			
			AnnotationX[] bXs = bmember.getAnnotations();
			AnnotationX[] aXs = amember.getAnnotations();
			assertTrue("Should have found an annotation on the bcel method?",bXs!=null && bXs.length==1);
			assertTrue("Should have found an annotation on the asm method?",aXs!=null && aXs.length==1);

			String exp = null;
			if (i==1) exp =	"ANNOTATION [LSimpleAnnotation3;] [runtimeVisible] [id=1]";
			else if (i==2) exp="ANNOTATION [LSimpleAnnotation3;] [runtimeVisible] [id=2]";
			assertTrue("BCEL annotation should be '"+exp+"' but is "+bXs[0].toString(),bXs[0].toString().equals(exp));
			assertTrue("ASM annotation should be '"+exp+"' but is "+aXs[0].toString(),aXs[0].toString().equals(exp));
		}
    }
    
    
    
    private void compareAttributeNames(String[] asmlist,String[] bcellist) {
      String astring = stringifyStringArray(asmlist);
      String bstring = stringifyStringArray(bcellist);
      if (asmlist.length!=bcellist.length) {
    	  fail("Differing lengths.\nBCEL=>\n"+bstring+" ASM=>\n"+astring);
      }
    }
    
    private String stringifyStringArray(String[] s) {
    	StringBuffer r = new StringBuffer();
    	for (int i = 0; i < s.length; i++) {
    		r.append(s[i]).append("\n");
		}
    	return r.toString();
    }
    
    
}
