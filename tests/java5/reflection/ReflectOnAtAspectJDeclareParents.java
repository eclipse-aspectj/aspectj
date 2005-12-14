import org.aspectj.lang.reflect.*;
import java.util.*;
import java.lang.reflect.*;

public class ReflectOnAtAspectJDeclareParents {
	
	public static void main(String[] args) {
		new ReflectOnAtAspectJDeclareParents().runTests();
	}
	
	public void runTests() {
	  AjType<AtAspectJDeclareParents> ajType = AjTypeSystem.getAjType(AtAspectJDeclareParents.class);

	  testDeclareParents(ajType);
	  testDeclaredInterTypeMethods(ajType);
	  testInterTypeMethods(ajType);
	  testDeclaredInterTypeFields(ajType);
	  testInterTypeFields(ajType);
	}
	
	
	private void testDeclareParents(AjType<AtAspectJDeclareParents> ajType) {
		DeclareParents[] dps = ajType.getDeclareParents();
		assertEquals(1,dps.length,"number of declare parents");
		System.out.println(dps[0]);
	}


	private void testDeclaredInterTypeMethods(AjType<AtAspectJDeclareParents> ajType) {
		InterTypeMethodDeclaration[] itdms = ajType.getDeclaredITDMethods();
		assertEquals(2,itdms.length,"number of declared ITD methods");
		Set s = new TreeSet(new Comparator<InterTypeMethodDeclaration>() {
			public int compare(InterTypeMethodDeclaration m1, InterTypeMethodDeclaration m2) {
				if (m1 == m2) return 0;
				int vis = (m1.getModifiers() - m2.getModifiers());
				if (vis != 0) return vis;
				int name = (m1.getName().compareTo(m2.getName()));
				if (name != 0) return name;
				try {
					return (
							m1.getTargetType().getJavaClass().getName().compareTo(
									m2.getTargetType().getJavaClass().getName())
							);
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		for (InterTypeMethodDeclaration itdm : itdms) { s.add(itdm); }
		for (Object o : s) { System.out.println(o); }
		try {
			InterTypeMethodDeclaration shouldFind = ajType.getDeclaredITDMethod("getX",AjTypeSystem.getAjType(I.class));
			System.out.println(shouldFind);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("getITDMethod failed");
		}
		try {
			ajType.getDeclaredITDMethod("getP",AjTypeSystem.getAjType(I.class));
			throw new RuntimeException("failed to fail in getting ITDMethod");
		} catch (NoSuchMethodException ex) {
			// good!
		}

	}

	private void testInterTypeMethods(AjType<AtAspectJDeclareParents> ajType) {
		InterTypeMethodDeclaration[] itdms = ajType.getITDMethods();
		assertEquals(2,itdms.length,"number of ITD methods");
		Set s = new TreeSet(new Comparator<InterTypeMethodDeclaration>() {
			public int compare(InterTypeMethodDeclaration m1, InterTypeMethodDeclaration m2) {
				if (m1 == m2) return 0;
				int vis = (m1.getModifiers() - m2.getModifiers());
				if (vis != 0) return vis;
				int name = (m1.getName().compareTo(m2.getName()));
				if (name != 0) return name;
				try {
					return (
							m1.getTargetType().getJavaClass().getName().compareTo(
									m2.getTargetType().getJavaClass().getName())
							);
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		for (InterTypeMethodDeclaration itdm : itdms) { s.add(itdm); }
		for (Object o : s) { System.out.println(o); }
		try {
			InterTypeMethodDeclaration shouldFind = ajType.getITDMethod("getX",AjTypeSystem.getAjType(I.class));
			System.out.println(shouldFind);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("getITDMethod failed");
		}
		try {
			ajType.getITDMethod("getX",AjTypeSystem.getAjType(C.class));
			throw new RuntimeException("failed to fail in getting ITDMethod");
		} catch (NoSuchMethodException ex) {
			// good!
		}		
	}
	
	private void testDeclaredInterTypeFields(AjType<AtAspectJDeclareParents> ajType) {
		InterTypeFieldDeclaration[] itdfs = ajType.getDeclaredITDFields();
		assertEquals(0,itdfs.length,"number of declared ITD fields");
	}
	
	private void testInterTypeFields(AjType<AtAspectJDeclareParents> ajType) {
		InterTypeFieldDeclaration[] itdfs = ajType.getITDFields();
		assertEquals(0,itdfs.length,"number of declared ITD fields");
	}
	
	private void assertEquals(int x, int y, String msg) {
		if (x != y) {
			throw new RuntimeException(msg + " expecting '" + x + "' but was '" + y + "'");
		}
	}

}