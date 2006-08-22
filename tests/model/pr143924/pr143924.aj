aspect DeclareAnnotation {
    declare @method : * debit(..) : @Secured(role="supervisor");
}

class BankAccount {

    public void debit(String accId,long amount) {
    }
}

@interface Secured {
    String role();
}
