package a.b.c;

import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

public aspect ITDTest {
	
    public void A.a(String s) {}

    private void A.b(String s) {}

    int A.c(String s) { return 1; }

    public A.new(String s) {}

    private A.new(String s,String s2) {}
	
    A.new(String s, Object o) {}

    public int A.f ;

    private int A.g;

    int A.h;
    
    public static void main(String[] args) throws ClassNotFoundException {
    	AjType<ITDTest> myType = AjTypeSystem.getAjType(ITDTest.class);
    	checkITDMs(myType);
    	checkITDFs(myType);
    	checkITDCs(myType);
    	checkAnnStyle();
    }
    
    private static void checkITDMs(AjType<?> itdTest) throws ClassNotFoundException {
    	InterTypeMethodDeclaration[] itdms = itdTest.getDeclaredITDMethods();
    	assertEquals("expecting 3 declared methods, got: ",3,itdms.length);
    	assertEquals("expecting method name a, got: ","a",itdms[0].getName());
    	assertEquals("expecting method name b, got: ","b",itdms[1].getName());
    	assertEquals("expecting method name c, got: ","c",itdms[2].getName());
    	assertEquals("expecting AjType<a.b.c.A>",AjTypeSystem.getAjType(A.class),itdms[0].getTargetType());
    	assertEquals("expecting public method, got:",true,Modifier.isPublic(itdms[0].getModifiers()));
    	assertEquals("expecting private method, got:",true,Modifier.isPrivate(itdms[1].getModifiers()));
    	assertEquals("expecting non-public method, got:",false,Modifier.isPublic(itdms[2].getModifiers()));
    	assertEquals("one param, got: ",1,itdms[0].getParameterTypes().length);
    	assertEquals("expecting String, got: ",String.class,itdms[0].getParameterTypes()[0].getJavaClass());
    	assertEquals("nothing thrown, but: ",0,itdms[1].getExceptionTypes().length);
    	assertEquals("expecting int, got: ",int.class,itdms[2].getReturnType().getJavaClass());
    	itdms = itdTest.getITDMethods();
    	assertEquals("expecting 1 method, got: ",1,itdms.length);
       	assertEquals("expecting method name a, got: ","a",itdms[0].getName());
       	try {
       		InterTypeMethodDeclaration m = itdTest.getDeclaredITDMethod("b",AjTypeSystem.getAjType(A.class),AjTypeSystem.getAjType(String.class));
       		assertEquals("expecting b, got: ","b",m.getName());
       	} catch (NoSuchMethodException ex) { throw new RuntimeException("didn't find expected itdm"); }
       	try {
       		InterTypeMethodDeclaration m = itdTest.getITDMethod("d",AjTypeSystem.getAjType(A.class),AjTypeSystem.getAjType(String.class));
       		throw new RuntimeException("Expected NoSuchMethodException not thrown");
       	} catch (NoSuchMethodException ex) { }
    }
    
    private static void checkITDFs(AjType<?> itdTest) throws ClassNotFoundException {
    	InterTypeFieldDeclaration[] itdfs = itdTest.getDeclaredITDFields();
    	assertEquals("expecting 3 declared fields, got: ",3, itdfs.length);
    	assertEquals("expecting field name f, got: ","f",itdfs[0].getName());
    	assertEquals("expecting field name g, got: ","g",itdfs[1].getName());
    	assertEquals("expecting field name h, got: ","h",itdfs[2].getName());
    	assertEquals("expecting AjType<a.b.c.A>",AjTypeSystem.getAjType(A.class),itdfs[0].getTargetType());
       	assertEquals("expecting public field, got:",true,Modifier.isPublic(itdfs[0].getModifiers()));
    	assertEquals("expecting private field, got:",true,Modifier.isPrivate(itdfs[1].getModifiers()));
    	assertEquals("expecting non-public field, got:",false,Modifier.isPublic(itdfs[2].getModifiers()));
       	assertEquals("expecting int, got: ",int.class,itdfs[2].getType().getJavaClass());
        itdfs = itdTest.getITDFields();
    	assertEquals("expecting 1 field, got: ",1, itdfs.length);
    	assertEquals("expecting field name f, got: ","f",itdfs[0].getName());   
    	try {
    		InterTypeFieldDeclaration f = itdTest.getDeclaredITDField("f",AjTypeSystem.getAjType(A.class));
       		assertEquals("expecting f, got: ","f",f.getName());
    	} catch(NoSuchFieldException ex) { throw new RuntimeException("didn't find expected itdf"); }
       	try {
       		InterTypeFieldDeclaration g = itdTest.getITDField("g",AjTypeSystem.getAjType(A.class));
       		throw new RuntimeException("Expected NoSuchFieldException not thrown");
       	} catch (NoSuchFieldException ex) { }
    }
    
    private static void checkITDCs(AjType<?> itdTest) throws ClassNotFoundException {
    	InterTypeConstructorDeclaration[] itdcs = itdTest.getDeclaredITDConstructors();
    	assertEquals("expecting 3 declared constructors, got: ",3, itdcs.length);
    	InterTypeConstructorDeclaration pubDec = findPublicCons(itdcs);
    	InterTypeConstructorDeclaration privDec = findPrivateCons(itdcs);
    	InterTypeConstructorDeclaration defDec = findDefaultCons(itdcs);
    	if (pubDec == null || privDec == null || defDec == null) throw new RuntimeException("failed to find expected constructors");
       	assertEquals("two params, got: ",2,defDec.getParameterTypes().length);
    	assertEquals("expecting String, got: ",String.class,defDec.getParameterTypes()[0].getJavaClass());
    	assertEquals("expecting Object, got: ",Object.class,defDec.getParameterTypes()[1].getJavaClass());
    	assertEquals("nothing thrown, but: ",0,privDec.getExceptionTypes().length);
      	itdcs = itdTest.getITDConstructors();
    	assertEquals("expecting 1 cons, got: ",1,itdcs.length);    	
    	try {
       		InterTypeConstructorDeclaration c = itdTest.getDeclaredITDConstructor(AjTypeSystem.getAjType(A.class),AjTypeSystem.getAjType(String.class));
       	} catch (NoSuchMethodException ex) { throw new RuntimeException("didn't find expected itdm"); }
       	try {
       		InterTypeConstructorDeclaration c = itdTest.getITDConstructor(AjTypeSystem.getAjType(A.class),AjTypeSystem.getAjType(String.class),AjTypeSystem.getAjType(Object.class));
       		throw new RuntimeException("Expected NoSuchMethodException not thrown");
       	} catch (NoSuchMethodException ex) { }
    }
    
    private static InterTypeConstructorDeclaration findPublicCons(InterTypeConstructorDeclaration[] itcds) {
    	for( InterTypeConstructorDeclaration i : itcds) {
    		if (Modifier.isPublic(i.getModifiers())) {
    			return i;
    		}
    	}
    	return null;
    }
    
    private static InterTypeConstructorDeclaration findPrivateCons(InterTypeConstructorDeclaration[] itcds) {
    	for( InterTypeConstructorDeclaration i : itcds) {
    		if (Modifier.isPrivate(i.getModifiers())) {
    			return i;
    		}
    	}
    	return null;
    }
    
    private static InterTypeConstructorDeclaration findDefaultCons(InterTypeConstructorDeclaration[] itcds) {
    	for( InterTypeConstructorDeclaration i : itcds) {
    		if (!Modifier.isPublic(i.getModifiers()) && !Modifier.isPrivate(i.getModifiers())) {
    			return i;
    		}
    	}
    	return null;
    }
    
    private static void checkAnnStyle() {
    	AjType<X> x = AjTypeSystem.getAjType(X.class);
    	org.aspectj.lang.reflect.DeclareParents[] decps = x.getDeclareParents();
    	assertEquals("1 declare parents",1,decps.length);
    	assertEquals("implements",true,decps[0].isImplements());
    	assertEquals("X",x,decps[0].getDeclaringType());
    	assertEquals("org.xyz..*, got: ","org.xyz..*",decps[0].getTargetTypesPattern().asString());
    	try {
    		assertEquals("1: ",1,decps[0].getParentTypes().length);
    		assertEquals("I: ",I.class,((AjType<?>)decps[0].getParentTypes()[0]).getJavaClass());
    	} catch (ClassNotFoundException cnf) {
    		throw new RuntimeException(cnf);
    	}
    	//assertEquals("ITD field 1: ",1,x.getDeclaredITDFields().length);
    	//assertEquals("ITD filed name i: ","i",x.getDeclaredITDFields()[0].getName());
    	assertEquals("ITD method 1: ",1,x.getITDMethods().length);
    	assertEquals("getNumber: ","getNumber",x.getITDMethods()[0].getName());
    }
    
    private static void assertEquals(String msg, Object expected, Object actual) {
    	if (!expected.equals(actual)) throw new RuntimeException(msg + " " + actual.toString());
    }
}


class A {}

@Aspect
class X {
	
	@org.aspectj.lang.annotation.DeclareParents("org.xyz..*")
	public static I myMixin = new Mixin();


	public static class Mixin implements I {
		
		private int i = 0;
		
		public int getNumber() { return i; }
		
	}
	
}

interface I {
	
	int getNumber();
	
}

