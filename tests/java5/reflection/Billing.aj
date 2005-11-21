public aspect Billing {

	public Customer Connection.payer;
    
	/**
     * Connections give the appropriate call rate
     */
    public abstract long Connection.callRate();

    public long LongDistance.callRate() { return 1; }
    public long Local.callRate() { return 2; }

    /**
     * Customers have a bill paying aspect with state
     */
    public long Customer.totalCharge = 0;

    public void Customer.addCharge(long charge){
        totalCharge += charge;
    }
}

class Customer {}

abstract class Connection {}

class LongDistance extends Connection {}

class Local extends Connection {}
