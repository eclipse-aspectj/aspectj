import java.lang.annotation.*;

public aspect DeclareAnnotation {
	
	declare @type : org.xyz.model..* : @BusinessDomain;
	
	declare @method : public * BankAccount+.*(..) : @Secured(role="supervisor");
	
	declare @field : * DAO+.* : @Persisted;
	
	declare @constructor : BankAccount+.new(..) : @Secured(role="supervisor");
	
	declare warning : staticinitialization(@BusinessDomain *)
	                : "@BusinessDomain";
	
	declare warning : execution(@Secured * *(..)) : "@Secured";
	
	declare warning : set(@Persisted * *) : "@Persisted";
	
	declare warning : initialization(@Secured *.new(..)) : "@Secured";
	
	public static void main(String[] args) throws Exception {
		Class bAcc = BankAccount.class;
		java.lang.reflect.Method credit = bAcc.getDeclaredMethod("credit");
		Secured secured = credit.getAnnotation(Secured.class);
		if (!secured.role().equals("supervisor")) {
			throw new RuntimeException("BankAccount.credit should have @Secured(role=supervisor) annotation");
		}
	}
}

@interface BusinessDomain {}

@Retention(RetentionPolicy.RUNTIME)
@interface Secured {
	String role() default "";
}

@interface Persisted {}

class BankAccount {
	
	public void credit() {}
	public void debit() {}
	protected void transfer() {}
	
}

class ExecutiveBankAccount extends BankAccount {
	
	public ExecutiveBankAccount() {
		super();
	}
	
	public void interest() {}
	protected void commission() {}
	
}

class DAO {
	
	int x = 5;
	
}

class SubDAO extends DAO {
	
	int y = 6;
	
}