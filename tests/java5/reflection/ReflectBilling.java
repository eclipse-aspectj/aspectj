import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.aspectj.lang.reflect.*;

public class ReflectBilling {
	
	public static void main(String[] args) {
		AjType<Billing> billingType = AjTypeSystem.getAjType(Billing.class);
		InterTypeMethodDeclaration[] itdms = billingType.getDeclaredITDMethods();

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
		
		InterTypeFieldDeclaration[] itdfs = billingType.getDeclaredITDFields();

		Set s2 = new TreeSet(new Comparator<InterTypeFieldDeclaration>() {
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
		for (InterTypeFieldDeclaration itdf : itdfs) { s2.add(itdf); }
		for (Object o : s2) { System.out.println(o); }	}
	
	
}