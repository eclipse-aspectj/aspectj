import org.aspectj.lang.reflect.*;
import java.util.*;
import java.lang.reflect.*;

public class ReflectOnCodeStyleITDs {
	
	public static void main(String[] args) {
		new ReflectOnCodeStyleITDs().runTests();
	}
	
	public void runTests() {
	  AjType<InterTypeDeclarations> ajType = AjTypeSystem.getAjType(InterTypeDeclarations.class);
	  
	  testDeclaredInterTypeConstructors(ajType);
	  testInterTypeConstructors(ajType);
	  testDeclaredInterTypeMethods(ajType);
	  testInterTypeMethods(ajType);
	  testDeclaredInterTypeFields(ajType);
	  testInterTypeFields(ajType);
	}
	
	private void testDeclaredInterTypeConstructors(AjType<InterTypeDeclarations> ajType) {
		InterTypeConstructorDeclaration[] itdcs = ajType.getDeclaredITDConstructors();
		assertEquals(3,itdcs.length,"number of declared ITD constructors");
		InterTypeConstructorDeclaration publicITDC = null;
		InterTypeConstructorDeclaration defaultITDC = null;
		InterTypeConstructorDeclaration privateITDC = null;
		for(InterTypeConstructorDeclaration itdc : itdcs) {
			if (Modifier.isPublic(itdc.getModifiers())) {
				publicITDC = itdc;
			} else if (Modifier.isPrivate(itdc.getModifiers())) {
				privateITDC = itdc;
			} else {
				defaultITDC = itdc;
			}
		}
		System.out.println(publicITDC);
		System.out.println(defaultITDC);
		System.out.println(privateITDC);
		try {
			InterTypeConstructorDeclaration shouldFind = ajType.getDeclaredITDConstructor(AjTypeSystem.getAjType(C.class), AjTypeSystem.getAjType(int.class));
			System.out.println(shouldFind);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("getDeclaredITDConstructor failed");
		}
		try {
			ajType.getDeclaredITDConstructor(AjTypeSystem.getAjType(I.class), AjTypeSystem.getAjType(int.class));
			throw new RuntimeException("failed to fail in getting DeclaredITDConstructor #1");
		} catch (NoSuchMethodException ex) {
			// good!
		}
		try {
			ajType.getDeclaredITDConstructor(AjTypeSystem.getAjType(C.class), AjTypeSystem.getAjType(String.class));
			throw new RuntimeException("failed to fail in getting DeclaredITDConstructor #2");
		} catch (NoSuchMethodException ex) {
			// good!
		}
		
	}

	private void testInterTypeConstructors(AjType<InterTypeDeclarations> ajType) {
		InterTypeConstructorDeclaration[] itdcs = ajType.getITDConstructors();
		assertEquals(1,itdcs.length,"number of ITD constructors");
		System.out.println(itdcs[0]);
		AjType<?> intClass = AjTypeSystem.getAjType(int.class);
		try {
			InterTypeConstructorDeclaration shouldFind = ajType.getITDConstructor(AjTypeSystem.getAjType(C.class), intClass, intClass, intClass);
			System.out.println(shouldFind);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("getITDConstructor failed");
		}
		try {
			ajType.getITDConstructor(AjTypeSystem.getAjType(C.class), AjTypeSystem.getAjType(int.class));
			throw new RuntimeException("failed to fail in getting ITDConstructor");
		} catch (NoSuchMethodException ex) {
			// good!
		}
	}

	private void testDeclaredInterTypeMethods(AjType<InterTypeDeclarations> ajType) {
		InterTypeMethodDeclaration[] itdms = ajType.getDeclaredITDMethods();
		assertEquals(6,itdms.length,"number of declared ITD methods");
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
			InterTypeMethodDeclaration shouldFind = ajType.getDeclaredITDMethod("getX",AjTypeSystem.getAjType(C.class));
			System.out.println(shouldFind);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("getITDMethod failed");
		}
		try {
			ajType.getDeclaredITDMethod("getP",AjTypeSystem.getAjType(C.class));
			throw new RuntimeException("failed to fail in getting ITDMethod");
		} catch (NoSuchMethodException ex) {
			// good!
		}

	}

	private void testInterTypeMethods(AjType<InterTypeDeclarations> ajType) {
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
			InterTypeMethodDeclaration shouldFind = ajType.getITDMethod("getZ",AjTypeSystem.getAjType(C.class));
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
	
	private void testDeclaredInterTypeFields(AjType<InterTypeDeclarations> ajType) {
		InterTypeFieldDeclaration[] itdfs = ajType.getDeclaredITDFields();
		assertEquals(6,itdfs.length,"number of declared ITD fields");
		Set s = new TreeSet(new Comparator<InterTypeFieldDeclaration>() {
			public int compare(InterTypeFieldDeclaration m1, InterTypeFieldDeclaration m2) {
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
		for (InterTypeFieldDeclaration itdf : itdfs) { s.add(itdf); }
		for (Object o : s) { System.out.println(o); }
		try {
			InterTypeFieldDeclaration shouldFind = ajType.getDeclaredITDField("x",AjTypeSystem.getAjType(C.class));
			System.out.println(shouldFind);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException("getITDField failed");
		}
		try {
			ajType.getDeclaredITDField("p",AjTypeSystem.getAjType(C.class));
			throw new RuntimeException("failed to fail in getting ITDField");
		} catch (NoSuchFieldException ex) {
			// good!
		}
		
	}
	
	private void testInterTypeFields(AjType<InterTypeDeclarations> ajType) {
		InterTypeFieldDeclaration[] itdfs = ajType.getITDFields();
		assertEquals(2,itdfs.length,"number of declared ITD fields");
		Set s = new TreeSet(new Comparator<InterTypeFieldDeclaration>() {
			public int compare(InterTypeFieldDeclaration m1, InterTypeFieldDeclaration m2) {
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
		for (InterTypeFieldDeclaration itdf : itdfs) { s.add(itdf); }
		for (Object o : s) { System.out.println(o); }
		try {
			InterTypeFieldDeclaration shouldFind = ajType.getITDField("z",AjTypeSystem.getAjType(C.class));
			System.out.println(shouldFind);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException("getITDField failed");
		}
		try {
			ajType.getITDField("x",AjTypeSystem.getAjType(C.class));
			throw new RuntimeException("failed to fail in getting ITDField");
		} catch (NoSuchFieldException ex) {
			// good!
		}		
	}
	
	private void assertEquals(int x, int y, String msg) {
		if (x != y) {
			throw new RuntimeException(msg + " expecting '" + x + "' but was '" + y + "'");
		}
	}

}