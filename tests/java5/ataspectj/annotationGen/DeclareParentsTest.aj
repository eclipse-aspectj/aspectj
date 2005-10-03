import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

public aspect DeclareParentsTest {
	
	declare parents : B || C extends A;

    declare parents : A implements I,J;
	
	public static void main(String[] args) throws ClassNotFoundException {
		AjType<DeclareParentsTest> myType = AjTypeSystem.getAjType(DeclareParentsTest.class);
		DeclareParents[] decPs = myType.getDeclareParents();
		if (decPs.length != 2) throw new RuntimeException("Expecting 2 members, got " + decPs.length);
		if (decPs[0].isExtends()) {
			checkExtends(decPs[0]);
			checkImplements(decPs[1]);
		} else {
			checkExtends(decPs[1]);
			checkImplements(decPs[0]);
		}
	}
	
	private static void checkExtends(DeclareParents extendsDecP) throws ClassNotFoundException {
		if (!extendsDecP.isExtends()) throw new RuntimeException("Should be extends");
		AjType declaring = extendsDecP.getDeclaringType();
		if (declaring.getJavaClass() != DeclareParentsTest.class) throw new RuntimeException("wrong declaring type");
		TypePattern tp = extendsDecP.getTargetTypesPattern();
		if (!tp.asString().equals("(B || C)")) throw new RuntimeException("expecting (B || C) but got '" + tp.asString() + "'");
		Type[] parentTypes = extendsDecP.getParentTypes();
		if (parentTypes.length != 1) throw new RuntimeException("expecting 1 parent type");
		if (((AjType<?>)parentTypes[0]).getJavaClass() != A.class) throw new RuntimeException("expecting parent to be A but was '" + ((AjType<?>)parentTypes[0]).getName() + "'");
	}
	
	private static void checkImplements(DeclareParents implementsDecP) throws ClassNotFoundException {
		if (!implementsDecP.isImplements()) throw new RuntimeException("Should be implements");
		AjType declaring = implementsDecP.getDeclaringType();
		if (declaring.getJavaClass() != DeclareParentsTest.class) throw new RuntimeException("wrong declaring type");
		TypePattern tp = implementsDecP.getTargetTypesPattern();
		if (!tp.asString().equals("A")) throw new RuntimeException("expecting A but got '" + tp.asString() + "'");
		Type[] parentTypes = implementsDecP.getParentTypes();
		if (parentTypes.length != 2) throw new RuntimeException("expecting 2 parent types");
		if (((AjType<?>)parentTypes[0]).getJavaClass() != I.class) throw new RuntimeException("expecting parent to be I but was '" + ((AjType<?>)parentTypes[0]).getName() + "'");		
		if (((AjType<?>)parentTypes[1]).getJavaClass() != J.class) throw new RuntimeException("expecting parent to be J but was '" + ((AjType<?>)parentTypes[0]).getName() + "'");		
	}
}


class A {}

class B {}

class C {}

interface I {}

interface J {}