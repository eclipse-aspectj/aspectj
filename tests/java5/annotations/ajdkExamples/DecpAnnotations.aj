public aspect DecpAnnotations {
	
	public String SecuredObject.getSecurityCredentials() {
		return "none";
	}
	
	declare parents : (@Secured *) implements SecuredObject;
	
	declare parents : (@Secured BankAccount+) implements SecuredObject;
	
	public static void main(String[] args) {
		Foo foo = new Foo();
		Goo goo = new Goo();
		BankAccount acc = new BankAccount();
		PrivateBankAccount pacc = new PrivateBankAccount();
		BusinessBankAccount bacc = new BusinessBankAccount();
		
		System.out.println("Test Foo is not secured: " + 
				((foo instanceof SecuredObject) ? "FAIL" : "PASS")
			);
		System.out.println("Test Goo is secured: " + 
				((goo instanceof SecuredObject) ? "PASS" : "FAIL")
			);
		System.out.println("goo credentials: " + goo.getSecurityCredentials());
		System.out.println("Test BankAccount is not secured: " + 
				((acc instanceof SecuredObject) ? "FAIL" : "PASS")
			);
		System.out.println("Test PrivateBankAccount is not secured: " + 
				((pacc instanceof SecuredObject) ? "FAIL" : "PASS")
			);
		System.out.println("Test BusinessBankAccount is secured: " + 
				((bacc instanceof SecuredObject) ? "PASS" : "FAIL")
			);		
	}
}


interface SecuredObject {}
@interface Secured {}

class Foo {}
@Secured class Goo{}

class BankAccount {}

class PrivateBankAccount extends BankAccount {}

@Secured class BusinessBankAccount extends BankAccount {}