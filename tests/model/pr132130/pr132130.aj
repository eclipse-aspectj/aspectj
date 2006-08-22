 aspect basic {

    declare @method : * debit(..) : @Secured(role="supervisor");
    declare @constructor : BankAccount+.new(..) : @Secured(role="supervisor");
}

class BankAccount {
	
	public BankAccount(String s, int i) {
	}
    public void debit(long accId,long amount) {
    }
}

@interface Secured {
    String role();
}
