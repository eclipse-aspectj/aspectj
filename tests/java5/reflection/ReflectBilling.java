import org.aspectj.lang.reflect.*;

public class ReflectBilling {
	
	public static void main(String[] args) {
		AjType<Billing> billingType = AjTypeSystem.getAjType(Billing.class);
		InterTypeMethodDeclaration[] itdms = billingType.getDeclaredITDMethods();
		for(InterTypeMethodDeclaration itdm : itdms) { 
			System.out.println(itdm); 
		}
		InterTypeFieldDeclaration[] itdfs = billingType.getDeclaredITDFields();
		for(InterTypeFieldDeclaration itdf : itdfs) { 
			System.out.println(itdf); 
		}	
	}
	
	
}